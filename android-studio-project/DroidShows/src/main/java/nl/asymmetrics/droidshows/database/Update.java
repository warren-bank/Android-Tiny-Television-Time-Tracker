package nl.asymmetrics.droidshows.database;

import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.api.ApiGateway;
import nl.asymmetrics.droidshows.common.Constants;
import nl.asymmetrics.droidshows.common.DateFormats;
import nl.asymmetrics.droidshows.utils.FileUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Update {
  protected static final String currentVersion = "0.1.5-7G6";

  public static final int MODE_INSTALL = 1;
  public static final int MODE_RESTORE = 2;

  public interface DatabaseUpdateListener {
    public void preDatabaseUpdate (int mode, int oldVersion);
    public void postDatabaseUpdate(int mode, int oldVersion, boolean result, boolean didUpdateAllSeries, Object passthrough);
  }

  private Context context;
  private SQLiteStore db;
  private boolean didUpdateAllSeries;

  public Update(Context context) {
    // IMPORTANT: Do NOT use "context.getApplicationContext()". Context must be for an Activity (NOT an Application) to create a ProgressDialog.
    this.context = context;
    this.db      = SQLiteStore.getInstance(context);
  }

  public void updateDatabase(DatabaseUpdateListener listener, int mode, Object passthrough) {
    Runnable updateDatabase = new Runnable() {
      public void run() {
        Log.d(Constants.LOG_TAG, "Database update routine");
        if (needsUpdate()) {
          Log.d(Constants.LOG_TAG, "Database needs update");
          Looper.prepare();

          int oldVersion = getVersionNumber();

          if (listener != null)
            listener.preDatabaseUpdate(mode, oldVersion);

          didUpdateAllSeries = false;
          boolean result = updateDatabaseVersion();

          if (result)
            Log.d(Constants.LOG_TAG, "Database updated");
          else
            Log.e(Constants.LOG_TAG, "Attempt to update version of database schema failed");

          if (listener != null)
            listener.postDatabaseUpdate(mode, oldVersion, result, didUpdateAllSeries, passthrough);

          Looper.loop();
        }
      }
    };
    Thread updateDatabaseTh = new Thread(updateDatabase);
    updateDatabaseTh.start();
  }

  private boolean needsUpdate() {
    return !getVersion().equals(currentVersion);
  }

  private String getVersion() {
    String version = "";
    try {
      Cursor c = db.query("SELECT version FROM droidseries");
      if (c != null && c.moveToFirst()) {
        version = c.getString(0);
        Log.d(Constants.LOG_TAG, "Current database version: "+ version);
        return version;
      }
      c.close();
    } catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    db.execQuery("INSERT INTO droidseries (version) VALUES ('0');");
    Log.d(Constants.LOG_TAG, "DB version blank. All updates will be run; please ignore errors.");
    return "0";
  }

  private int getVersionNumber() {
    String version = getVersion();

    switch(version) {
      case "0":
      case "0.1.5-6":
        return 1;
      case "0.1.5-7":
        return 2;
      case "0.1.5-7G":
        return 3;
      case "0.1.5-7G2":
        return 4;
      case "0.1.5-7G3":
        return 5;
      case "0.1.5-7G4":
        return 6;
      case "0.1.5-7G5":
        return 7;
      case "0.1.5-7G6":
        return 8;
    }
    return -1;
  }

  private boolean updateDatabaseVersion() {
    String version = getVersion();
    boolean done = false;
    if (version.equals("0.1.5-6") || version.equals("0")) {
      done = u0156To0157();
      version = getVersion();
    }
    if (version.equals("0.1.5-7")) {
      done = u0157To0157G();
      version = getVersion();
    }
    if (version.equals("0.1.5-7G")) {
      done = u0157GTo0157G2();
      version = getVersion();
    }
    if (version.equals("0.1.5-7G2")) {
      done = u0157GTo0157G3();
      version = getVersion();
    }
    if (version.equals("0.1.5-7G3")) {
      done = u0157GTo0157G4();
      version = getVersion();
    }
    if (version.equals("0.1.5-7G4")) {
      done = u0157GTo0157G5();
      version = getVersion();
    }
    if (version.equals("0.1.5-7G5")) {
      done = u0157GTo0157G6();
      didUpdateAllSeries |= done;
      version = getVersion();
    }
    return done;
  }

  private boolean u0156To0157() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7");
    try {
      db.execQuery("ALTER TABLE series ADD COLUMN passiveStatus INTEGER DEFAULT 0");
      db.execQuery("UPDATE droidseries SET version='0.1.5-7'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157To0157G() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G");
    try {
      db.execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER DEFAULT -1");
      db.execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER DEFAULT -1");
      db.execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER DEFAULT -1");
      db.execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR DEFAULT '-1'");
      db.execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR DEFAULT '-1'");
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157GTo0157G2() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G2");
    try {
      db.execQuery("ALTER TABLE series ADD COLUMN extResources VARCHAR NOT NULL DEFAULT ''");
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G2'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157GTo0157G3() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G3");
    try {
      if (!convertSeenTimestamps()) return false;
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G3'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157GTo0157G4() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G4");
    try {
      db.execQuery("ALTER TABLE series ADD COLUMN unwatchedLastAired VARCHAR");
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G4'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157GTo0157G5() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G5");
    try {
      db.execQuery("ALTER TABLE series ADD COLUMN unwatchedLastEpisode VARCHAR");
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G5'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean u0157GTo0157G6() {
    Log.d(Constants.LOG_TAG, "UPDATING TO VERSION 0.1.5-7G6");
    try {
      if (!doTmdbApiMigration()) return false;
      db.execQuery("UPDATE droidseries SET version='0.1.5-7G6'");
      return true;
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  // ---------------------------------------------------------------------------
  // Used by: VERSION 0.1.5-7G3
  // ---------------------------------------------------------------------------

  // convert value of integers in 'episode.seen' from 'yyyyMMdd' to unix epoch
  // ex: from: 19700101 => to: 0
  private boolean convertSeenTimestamps() {
    boolean result = true;
    List<EpisodeSeenConvertSeenTimestamps> episodesSeen = new ArrayList<EpisodeSeenConvertSeenTimestamps>();

    String query = "SELECT id, seen FROM episodes WHERE seen>1";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          episodesSeen.add(new EpisodeSeenConvertSeenTimestamps(c.getString(0), c.getInt(1)));
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      result = false;
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    if (result) {
      SimpleDateFormat dateFormatSeen = new SimpleDateFormat("yyyyMMdd");
      Date seenTimestamp;
      int seen;
      List<String> queries = new ArrayList<String>();

      for (EpisodeSeenConvertSeenTimestamps ep : episodesSeen) {
        if (ep.seen > 1) {
          try {
            seenTimestamp = dateFormatSeen.parse("" + ep.seen);
            seen = DateFormats.convertMsToSeconds(seenTimestamp.getTime());
            queries.add("UPDATE episodes SET seen = " + seen + " WHERE id='" + ep.episodeId + "'");
          }
          catch (ParseException e) {
            e.printStackTrace();
            result = false;
            break;
          }
        }
      }

      if (result && !queries.isEmpty()) {
        result = db.execTransaction(queries);
      }

      if (result) {
        Log.d(Constants.LOG_TAG, "Converted seen dates (done) = " + queries.size());
      }
    }

    return result;
  }

  private class EpisodeSeenConvertSeenTimestamps {
    public String episodeId;
    public int    seen;

    public EpisodeSeenConvertSeenTimestamps(String episodeId, int seen) {
      this.episodeId = episodeId;
      this.seen      = seen;
    }
  }

  // ---------------------------------------------------------------------------
  // Used by: VERSION 0.1.5-7G6
  // ---------------------------------------------------------------------------

  // 1. backup user metadata to temporary tables
  //    a) this includes migrating "pinned" series from app preferences to the DB
  // 2. delete old schema
  //    a) delete app preferences that will no-longer be used
  //    b) delete resized images stored in filesystem
  // 3. construct an entirely new schema
  // 4. rehydrate the DB by:
  //    a) re-adding series
  //    b) importing user metadata from temporary tables
  // 5. remove temporary tables
  private boolean doTmdbApiMigration() {
      boolean result = true;

      ApiGateway api = null;
      List<String> queries;

      // initialize ApiGateway
      if (result) {
        try {
          api = ApiGateway.getInstance(context);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error initializing TMDB API library.", e);
          result = false;
        }
      }

      // backup user metadata to temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

          String defaultLangCode = context.getString(R.string.lang_code);

          SharedPreferences sharedPrefs = context.getSharedPreferences("DroidShowsPref", 0);
          String pinnedShowsStr         = sharedPrefs.getString("pinned_shows", "");
          pinnedShowsStr                = pinnedShowsStr.replace("[", "").replace("]", "").replace(" ", "");
          List<String> pinnedShows      = TextUtils.isEmpty(pinnedShowsStr) ? null : new ArrayList<String>(Arrays.asList(pinnedShowsStr.split(",")));

          queries.add("DROP TABLE IF EXISTS tmdb_migration_series");
          queries.add("DROP TABLE IF EXISTS tmdb_migration_episodes");

          queries.add("CREATE TABLE tmdb_migration_series ("
            + "   tmdbid        INTEGER,"  // result of API query
            + "   thetvdbid     INTEGER NOT NULL PRIMARY KEY,"
            + "   name          VARCHAR,"  // only included for display in ProgressDialog
            + "   language      VARCHAR,"
            + "   archived      INTEGER,"  // passiveStatus
            + "   pinned        INTEGER,"  // occurs in "pinned_shows"
            + "   extResources  VARCHAR"
            + ")"
          );

          queries.add("CREATE TABLE tmdb_migration_episodes ("
            + "   tmdbid        INTEGER,"  // result of API query
            + "   thetvdbid     INTEGER NOT NULL,"
            + "   seasonNumber  INTEGER NOT NULL,"
            + "   episodeNumber INTEGER NOT NULL,"
            + "   seen          INTEGER,"
            + "   PRIMARY KEY (thetvdbid, seasonNumber, episodeNumber)"
            + ")"
          );

          queries.add("INSERT into tmdb_migration_series   SELECT NULL as tmdbid, cast(id      as INTEGER) as thetvdbid, serieName as name, language, passiveStatus as archived, 0 as pinned, extResources from series");
          queries.add("INSERT into tmdb_migration_episodes SELECT NULL as tmdbid, cast(serieId as INTEGER) as thetvdbid, seasonNumber, episodeNumber, seen from episodes");

          if (!TextUtils.isEmpty(defaultLangCode))
            queries.add("UPDATE tmdb_migration_series SET language = '" + defaultLangCode + "' WHERE language=NULL");

          if ((pinnedShows != null) && !pinnedShows.isEmpty())
            queries.add("UPDATE tmdb_migration_series SET pinned = 1 WHERE thetvdbid IN (" + TextUtils.join(", ", pinnedShows) + ")");

          List<String> thetvdbids = new ArrayList<String>();
          {
            String query = "SELECT id FROM series";

            Cursor c = null;
            try {
              c = db.query(query);

              if ((c != null) && c.moveToFirst() && c.isFirst()) {
                do {
                  thetvdbids.add(c.getString(0));
                } while (c.moveToNext());
              }
            }
            catch (SQLiteException e) {
              Log.e(Constants.LOG_TAG, e.getMessage());
            }
            if (c != null) c.close();
          }

          for (String thetvdbid : thetvdbids) {
            int tmdbid = api.findSeriesByExternalId(thetvdbid);

            if (tmdbid > 0) {
              queries.add("UPDATE tmdb_migration_series   SET tmdbid = " + tmdbid + " WHERE thetvdbid = " + thetvdbid);
              queries.add("UPDATE tmdb_migration_episodes SET tmdbid = " + tmdbid + " WHERE thetvdbid = " + thetvdbid);
            }
            else {
              throw new Exception("TMDB API failed to return the (internal) TMDB ID for a TV series that is identified by the (external) TVDB ID: " + thetvdbid);
            }
          }

          result = db.execTransaction(queries);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error creating a backup of user metadata to temporary tables.", e);
          result = false;
        }

        // cleanup: remove temporary tables
        if (!result) {
          try {
            queries = new ArrayList<String>();

            queries.add("DROP TABLE IF EXISTS tmdb_migration_series");
            queries.add("DROP TABLE IF EXISTS tmdb_migration_episodes");

            db.execTransaction(queries);
          }
          catch(Exception e) {
            Log.e(Constants.LOG_TAG, "Error deleting temporary tables.", e);
          }
        }
      }

      // delete old schema
      if (result) {
        try {
          queries = new ArrayList<String>();

          queries.add("DROP TABLE writers");
          queries.add("DROP TABLE directors");
          queries.add("DROP TABLE guestStars");
          queries.add("DROP TABLE actors");
          queries.add("DROP TABLE genres");
          queries.add("DROP TABLE serie_seasons");
          queries.add("DROP TABLE episodes");
          queries.add("DROP TABLE series");

          result = db.execTransaction(queries);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error deleting old schema.", e);
          result = false;
        }
      }

      // delete app preferences that will no-longer be used
      if (result) {
        try {
          SharedPreferences sharedPrefs = context.getSharedPreferences("DroidShowsPref", 0);
          SharedPreferences.Editor ed = sharedPrefs.edit();
          ed.remove("pinned_shows");
          ed.remove("last_season");
          ed.remove("use_mirror");
          ed.commit();
        }
        catch(Exception e) {}
      }

      // delete resized images stored in filesystem
      if (result) {
        try {
          String  dirPath      = FileUtils.getImageDirectoryPath(context, "thumbs");
          boolean useRecursion = true;

          FileUtils.deleteDirectoryContents(dirPath, useRecursion);

          // also delete the "thumbs" directory, since resized images will now be saved to "images_small" and "images_medium", based on relative size
          FileUtils.deleteFile(dirPath);
        }
        catch(Exception e) {}
      }

      // construct an entirely new schema
      if (result) {
        try {
          db.onCreate(null);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error creating new schema.", e);
          result = false;
        }
      }

      // rehydrate the DB: re-add series
      if (result) {
        try {
          String query = "SELECT tmdbid, name, language, archived FROM tmdb_migration_series";
          int serieId, archived;
          String name, language;
          boolean oneResult;

          FullUpdateProgressViewer progressViewer = null;
          Cursor c = null;
          try {
            c = db.query(query);

            if ((c != null) && c.moveToFirst() && c.isFirst()) {
              progressViewer = new FullUpdateProgressViewer(context);
              progressViewer.startProgressDialog(
                context.getString(R.string.messages_title_updating_series),
                context.getString(R.string.messages_adding_serie),
                c.getCount()
              );

              do {
                serieId  = c.getInt(0);
                name     = c.getString(1);
                language = c.getString(2);
                archived = c.getInt(3);

                progressViewer.updateProgressDialog(/* title */ null, /* message */ (name + "\u2026"), /* increment */ false);

                // at this stage, don't halt reimport
                oneResult = api.addSeries(serieId, language, (archived == 1));

                if (!oneResult) {
                  Toast.makeText(context, "Failed to re-add series: '" + name + "'.\nNot all data was loaded from API.", Toast.LENGTH_LONG).show();
                  Log.e(Constants.LOG_TAG, "Failed to re-add series: serieId=" + serieId + ", name='" + name + "', langCode=" + language + ", archived=" + ((archived == 1) ? "true" : "false"));
                }

                progressViewer.updateProgressDialog(/* title */ null, /* message */ null, /* increment */ true);
              } while (c.moveToNext());
            }
          }
          catch (SQLiteException e) {
            Log.e(Constants.LOG_TAG, e.getMessage());
          }
          if (c != null) c.close();
          if (progressViewer != null) progressViewer.dismissProgressDialog();
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error re-adding TV series to new schema.", e);
          result = false;
        }
      }

      // rehydrate the DB: import user metadata from temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

          if (Build.VERSION.SDK_INT >= 31) {
            // use UPDATE-FROM syntax that was added to SQLite v3.33.0 and is only supported by Android 12 and higher

            queries.add("UPDATE series"
              + " SET"
              + "   pinned       = migrate.pinned,"
              + "   extResources = migrate.extResources"
              + " FROM"
              + "   ("
              +      " SELECT tmdbid AS id, pinned, extResources FROM tmdb_migration_series"
              + "   ) AS migrate"
              + " WHERE"
              + "   series.id = migrate.id"
            );

            queries.add("UPDATE episodes"
              + " SET"
              + "   seen = migrate.seen"
              + " FROM"
              + "   ("
              +      " SELECT tmdbid AS serieId, seasonNumber, episodeNumber, seen FROM tmdb_migration_episodes"
              + "   ) AS migrate"
              + " WHERE"
              + "   episodes.serieId = migrate.serieId"
              + " AND"
              + "   episodes.seasonNumber = migrate.seasonNumber"
              + " AND"
              + "   episodes.episodeNumber = migrate.episodeNumber"
            );
          }
          else {
            // use a more verbose syntax that is required by older versions

            queries.add("UPDATE series"
              + " SET"
              + "   pinned       = (SELECT migrate.pinned       FROM tmdb_migration_series AS migrate WHERE migrate.tmdbid = series.id),"
              + "   extResources = (SELECT migrate.extResources FROM tmdb_migration_series AS migrate WHERE migrate.tmdbid = series.id)"
              + " WHERE"
              + "   id IN          (SELECT migrate.tmdbid       FROM tmdb_migration_series AS migrate WHERE migrate.tmdbid = series.id)"
            );

            queries.add("UPDATE episodes"
              + " SET"
              + "   seen =     (SELECT migrate.seen   FROM tmdb_migration_episodes AS migrate WHERE migrate.tmdbid = episodes.serieId AND migrate.seasonNumber = episodes.seasonNumber AND migrate.episodeNumber = episodes.episodeNumber)"
              + " WHERE"
              + "   serieId IN (SELECT migrate.tmdbid FROM tmdb_migration_episodes AS migrate WHERE migrate.tmdbid = episodes.serieId AND migrate.seasonNumber = episodes.seasonNumber AND migrate.episodeNumber = episodes.episodeNumber)"
            );
          }

          result = db.execTransaction(queries);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error importing user metadata for TV series from temporary tables to new schema.", e);
          result = false;
        }
      }

      // remove temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

          queries.add("DROP TABLE IF EXISTS tmdb_migration_series");
          queries.add("DROP TABLE IF EXISTS tmdb_migration_episodes");

          db.execTransaction(queries);
        }
        catch(Exception e) {
          Log.e(Constants.LOG_TAG, "Error deleting temporary tables.", e);
        }
      }

      if (result) {
        Log.d(Constants.LOG_TAG, "Migrated DB Schema (v0.1.5-7G6, TMDB)");
      }
      return result;
  }

  private class FullUpdateProgressViewer {
    private Context        context;
    private ProgressDialog progressDialog;
    private Handler        uiHandler;
    private Runnable       startProgressDialogRunnable;
    private Runnable       updateProgressDialogTitleRunnable;
    private Runnable       updateProgressDialogMessageRunnable;
    private Runnable       updateProgressDialogIncrementRunnable;
    private Runnable       dismissProgressDialogRunnable;

    private String title;
    private String message;
    private int max;

    public FullUpdateProgressViewer(Context context) {
      this.context = context;

      progressDialog = null;
      uiHandler      = new Handler(Looper.getMainLooper());

      startProgressDialogRunnable = new Runnable() {
        public void run() {
          progressDialog = new ProgressDialog(context);
          progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
          progressDialog.setTitle(title);
          progressDialog.setMessage(message);
          progressDialog.setCancelable(false);
          progressDialog.setMax(max);
          progressDialog.setProgress(0);
          progressDialog.show();
        }
      };

      updateProgressDialogTitleRunnable = new Runnable() {
        public void run() {
          progressDialog.setTitle(title);
        }
      };

      updateProgressDialogMessageRunnable = new Runnable() {
        public void run() {
          progressDialog.setMessage(message);
        }
      };

      updateProgressDialogIncrementRunnable = new Runnable() {
        public void run() {
          progressDialog.incrementProgressBy(1);
        }
      };

      dismissProgressDialogRunnable = new Runnable() {
        public void run() {
          progressDialog.dismiss();
        }
      };
    }

    public void startProgressDialog(String title, String message, int max) {
      this.title   = title;
      this.message = message;
      this.max     = max;

      uiHandler.post(startProgressDialogRunnable);
    }

    public void updateProgressDialog(String title, String message, boolean increment) {
      if (!TextUtils.isEmpty(title)) {
        this.title = title;
        uiHandler.post(updateProgressDialogTitleRunnable);
      }
      if (!TextUtils.isEmpty(message)) {
        this.message = message;
        uiHandler.post(updateProgressDialogMessageRunnable);
      }
      if (increment) {
        uiHandler.post(updateProgressDialogIncrementRunnable);
      }
    }

    public void dismissProgressDialog() {
      uiHandler.post(dismissProgressDialogRunnable);
    }
  }

  // ---------------------------------------------------------------------------

}