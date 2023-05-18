package com.github.warren_bank.tiny_television_time_tracker.database;

import com.github.warren_bank.tiny_television_time_tracker.DroidShowsDatabaseMigrationTool;
import com.github.warren_bank.tiny_television_time_tracker.api.ApiGateway;
import com.github.warren_bank.tiny_television_time_tracker.common.DateFormats;
import com.github.warren_bank.tiny_television_time_tracker.common.TextUtils;
import com.github.warren_bank.tiny_television_time_tracker.constants.Strings;
import com.github.warren_bank.tiny_television_time_tracker.utils.FileUtils;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Update {

  // ---------------------------------------------------------------------------
  // static
  // ---------------------------------------------------------------------------

  protected static final String VERSION_STRING_CURRENT = "0000000009";
  private   static       String VERSION_STRING_ACTUAL  = null;

  public static final int VERSION_TMDB_MIGRATION = 8;

  // ---------------------------------------------------------------------------

  private static Cursor query(SQLiteStore db, String query) {
    boolean skipVersionCheck = true;
    return db.query(query, skipVersionCheck);
  }

  private static boolean execQuery(SQLiteStore db, String query) {
    boolean skipVersionCheck = true;
    return db.execQuery(query, skipVersionCheck);
  }

  private static boolean execTransaction(SQLiteStore db, List<String> queries) {
    boolean skipVersionCheck = true;
    return db.execTransaction(queries, skipVersionCheck);
  }

  // ---------------------------------------------------------------------------

  protected static void resetVersionCache() {
    Update.VERSION_STRING_ACTUAL = null;
  }

  protected static boolean needsUpdate(SQLiteStore db) {
    return !Update.getVersion(db, /* resetCache */ false).equals(Update.VERSION_STRING_CURRENT);
  }

  private static String getVersion(SQLiteStore db, boolean resetCache) {
    if (resetCache)
      Update.resetVersionCache();

    if (TextUtils.isEmpty(Update.VERSION_STRING_ACTUAL)) {
      try {
        Cursor c = Update.query(db, "SELECT version FROM droidseries");
        if (c != null && c.moveToFirst()) {
          String version = c.getString(0);
          if (!TextUtils.isEmpty(version)) {
            Update.VERSION_STRING_ACTUAL = version;
          }
        }
        c.close();
      } catch (SQLiteException e) {
      }
    }

    if (TextUtils.isEmpty(Update.VERSION_STRING_ACTUAL)) {
      Update.VERSION_STRING_ACTUAL = "0";
      Update.execQuery(db, "INSERT INTO droidseries (version) VALUES ('" + Update.VERSION_STRING_ACTUAL + "')");
    }

    return Update.VERSION_STRING_ACTUAL;
  }

  // ---------------------------------------------------------------------------
  // inner classes and interfaces
  // ---------------------------------------------------------------------------

  public class DatabaseUpdateResult {
    public boolean didUpdateFail;
    public boolean didUpdateSucceed;
    public boolean didUpdateAllSeries;
    public TmdbApiMigrationResult tmdbApiMigrationResult;

    public DatabaseUpdateResult() {
      this.didUpdateFail          = false;
      this.didUpdateSucceed       = false;
      this.didUpdateAllSeries     = false;
      this.tmdbApiMigrationResult = new TmdbApiMigrationResult();
    }
  }

  public class TmdbApiMigrationResult {
    public List<String>  tvdbSeriesIdsThatFailedToResolve;
    public List<String>  seriesNamesThatFailedToResolve;
    public List<Integer> tmdbSeriesIdsThatFailedToAdd;
    public List<String>  seriesNamesThatFailedToAdd;

    public TmdbApiMigrationResult() {
      this.tvdbSeriesIdsThatFailedToResolve = new ArrayList<String>();
      this.seriesNamesThatFailedToResolve   = new ArrayList<String>();
      this.tmdbSeriesIdsThatFailedToAdd     = new ArrayList<Integer>();
      this.seriesNamesThatFailedToAdd       = new ArrayList<String>();
    }
  }

  public interface DatabaseUpdateListener {
    public boolean preDatabaseUpdate (int oldVersion, boolean willUpdate);
    public void    postDatabaseUpdate(int oldVersion, DatabaseUpdateResult result);
  }

  // ---------------------------------------------------------------------------
  // instance
  // ---------------------------------------------------------------------------

  private SQLiteStore db;
  private DatabaseUpdateResult databaseUpdateResult;

  public Update() throws Exception {
    this.db = SQLiteStore.getInstance();
  }

  private Cursor query(String query) {
    return Update.query(db, query);
  }

  private boolean execQuery(String query) {
    return Update.execQuery(db, query);
  }

  private boolean execTransaction(List<String> queries) {
    return Update.execTransaction(db, queries);
  }

  public void updateDatabase(DatabaseUpdateListener listener) {
    databaseUpdateResult = new DatabaseUpdateResult();

    int     oldVersion = getVersionNumber();
    boolean willUpdate = Update.needsUpdate(db);

    if (listener != null) {
      boolean proceed = listener.preDatabaseUpdate(oldVersion, willUpdate);
      if (!proceed) {
        return;
      }
    }

    if (willUpdate)
      updateDatabaseVersion();

    if (listener != null)
      listener.postDatabaseUpdate(oldVersion, databaseUpdateResult);
  }

  private int getVersionNumber() {
    String version = Update.getVersion(db, /* resetCache */ true);

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
      case "0000000009":
        return 9;
    }
    return -1;
  }

  private void updateDatabaseVersion() {
    boolean didUpdate, result;
    int version;

    databaseUpdateResult.didUpdateSucceed = true;
    didUpdate                             = false;
    version                               = getVersionNumber();

    if (!databaseUpdateResult.didUpdateFail && (version == 1)) {
      didUpdate                                = true;
      result                                   = update_version_001();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 2)) {
      didUpdate                                = true;
      result                                   = update_version_002();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 3)) {
      didUpdate                                = true;
      result                                   = update_version_003();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 4)) {
      didUpdate                                = true;
      result                                   = update_version_004();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 5)) {
      didUpdate                                = true;
      result                                   = update_version_005();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 6)) {
      didUpdate                                = true;
      result                                   = update_version_006();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 7)) {
      didUpdate                                = true;
      result                                   = update_version_007();
      databaseUpdateResult.didUpdateAllSeries |= result;
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 8)) {
      didUpdate                                = true;
      result                                   = update_version_008();
      databaseUpdateResult.didUpdateSucceed   &= result;
      databaseUpdateResult.didUpdateFail      |= !result;
      version                                  = getVersionNumber();
    }
    if (!databaseUpdateResult.didUpdateFail && (version == 9)) {
      // noop: version of schema is up-to-date
    }

    databaseUpdateResult.didUpdateSucceed &= didUpdate;
  }

  private boolean update_version_001() {
    System.out.println("  Updating to version: 0.1.5-7");
    try {
      execQuery("ALTER TABLE series ADD COLUMN passiveStatus INTEGER DEFAULT 0");
      execQuery("UPDATE droidseries SET version='0.1.5-7'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_002() {
    System.out.println("  Updating to version: 0.1.5-7G");
    try {
      execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER DEFAULT -1");
      execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER DEFAULT -1");
      execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER DEFAULT -1");
      execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR DEFAULT '-1'");
      execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR DEFAULT '-1'");
      execQuery("UPDATE droidseries SET version='0.1.5-7G'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_003() {
    System.out.println("  Updating to version: 0.1.5-7G2");
    try {
      execQuery("ALTER TABLE series ADD COLUMN extResources VARCHAR NOT NULL DEFAULT ''");
      execQuery("UPDATE droidseries SET version='0.1.5-7G2'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_004() {
    System.out.println("  Updating to version: 0.1.5-7G3");
    try {
      if (!convertSeenTimestamps()) return false;
      execQuery("UPDATE droidseries SET version='0.1.5-7G3'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_005() {
    System.out.println("  Updating to version: 0.1.5-7G4");
    try {
      execQuery("ALTER TABLE series ADD COLUMN unwatchedLastAired VARCHAR");
      execQuery("UPDATE droidseries SET version='0.1.5-7G4'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_006() {
    System.out.println("  Updating to version: 0.1.5-7G5");
    try {
      execQuery("ALTER TABLE series ADD COLUMN unwatchedLastEpisode VARCHAR");
      execQuery("UPDATE droidseries SET version='0.1.5-7G5'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_007() {
    System.out.println("  Updating to version: 0.1.5-7G6");
    try {
      if (!doTmdbApiMigration()) return false;
      execQuery("UPDATE droidseries SET version='0.1.5-7G6'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
      e.printStackTrace();
      return false;
    }
  }

  private boolean update_version_008() {
    System.out.println("  Updating to version: 0000000009");
    try {
      execQuery(
          "CREATE TABLE IF NOT EXISTS unavailableEpisodes ("
        +   "serieId              INTEGER NOT NULL"                       + ", "
        +   "seasonNumber         INTEGER NOT NULL"                       + ", "
        +   "episodeNumber        INTEGER NOT NULL"                       + ", "
        +   "PRIMARY KEY (serieId, seasonNumber, episodeNumber)"          + ", "
        +   "FOREIGN KEY (serieId) REFERENCES series (id)"
        + ");"
      );
      execQuery("UPDATE droidseries SET version='0000000009'");
      return true;
    } catch (Exception e) {
      System.out.println("Error updating database");
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
      c = query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          episodesSeen.add(new EpisodeSeenConvertSeenTimestamps(c.getString(0), c.getInt(1)));
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      result = false;
      System.out.println(e.getMessage());
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
        result = execTransaction(queries);
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

  private boolean doTmdbApiMigration() {
    boolean abortOnMissingSeriesId   = false;
    boolean abortOnErrorAddingSeries = false;

    return doTmdbApiMigration(abortOnMissingSeriesId, abortOnErrorAddingSeries);
  }

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
  private boolean doTmdbApiMigration(boolean abortOnMissingSeriesId, boolean abortOnErrorAddingSeries) {
      boolean result = true;

      ApiGateway api = null;
      List<String> queries;

      // initialize ApiGateway
      if (result) {
        try {
          api = ApiGateway.getInstance();
        }
        catch(Exception e) {
          System.out.println("Error initializing TMDB API library.");
          result = false;
        }
      }

      // backup user metadata to temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

          String defaultLangCode   = Strings.lang_code;
          List<String> pinnedShows = null;

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

          queries.add("INSERT into tmdb_migration_series   SELECT NULL as tmdbid, cast(id      as INTEGER) as thetvdbid, serieName as name, language, passiveStatus as archived, 0 as pinned, extResources FROM series");
          queries.add("INSERT into tmdb_migration_episodes SELECT NULL as tmdbid, cast(serieId as INTEGER) as thetvdbid, seasonNumber, episodeNumber, MAX(seen) as seen FROM episodes GROUP BY thetvdbid, seasonNumber, episodeNumber");

          if (!TextUtils.isEmpty(defaultLangCode))
            queries.add("UPDATE tmdb_migration_series SET language = '" + defaultLangCode + "' WHERE language=NULL");

          if ((pinnedShows != null) && !pinnedShows.isEmpty())
            queries.add("UPDATE tmdb_migration_series SET pinned = 1 WHERE thetvdbid IN (" + TextUtils.join(", ", pinnedShows) + ")");

          List<String> thetvdbids = new ArrayList<String>();
          List<String> imdbids    = new ArrayList<String>();
          List<String> serieNames = new ArrayList<String>();
          {
            String query = "SELECT id, imdbId, serieName FROM series";

            Cursor c = null;
            try {
              c = query(query);

              if ((c != null) && c.moveToFirst() && c.isFirst()) {
                do {
                  thetvdbids.add(c.getString(0));
                  imdbids.add   (c.isNull(1) ? "" : c.getString(1));
                  serieNames.add(c.isNull(2) ? "" : c.getString(2));
                } while (c.moveToNext());
              }
            }
            catch (SQLiteException e) {
              System.out.println(e.getMessage());
            }
            if (c != null) c.close();
          }

          for (int i=0; i < thetvdbids.size(); i++) {
            String thetvdbid = thetvdbids.get(i);
            String imdbid    = imdbids.get(i);
            int tmdbid;

            Map<String,String> origins = new HashMap<String, String>();
            origins.put("thetvdb", thetvdbid);
            origins.put("imdb",    imdbid);

            tmdbid = api.findSeriesByExternalId(origins);

            if (tmdbid > 0) {
              queries.add("UPDATE tmdb_migration_series   SET tmdbid = " + tmdbid + " WHERE thetvdbid = " + thetvdbid);
              queries.add("UPDATE tmdb_migration_episodes SET tmdbid = " + tmdbid + " WHERE thetvdbid = " + thetvdbid);
            }
            else if (abortOnMissingSeriesId) {
              throw new Exception("TMDB API failed to return the (internal) TMDB ID for a TV series that is identified by the (external) TVDB ID: " + thetvdbid);
            }
            else {
              databaseUpdateResult.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.add(thetvdbid);
              databaseUpdateResult.tmdbApiMigrationResult.seriesNamesThatFailedToResolve.add(serieNames.get(i));
            }
          }

          result = execTransaction(queries);
        }
        catch(Exception e) {
          System.out.println("Error creating a backup of user metadata to temporary tables.");
          result = false;
        }

        // cleanup: remove temporary tables
        if (!result) {
          try {
            queries = new ArrayList<String>();

            queries.add("DROP TABLE IF EXISTS tmdb_migration_series");
            queries.add("DROP TABLE IF EXISTS tmdb_migration_episodes");

            execTransaction(queries);
          }
          catch(Exception e) {
            System.out.println("Error deleting temporary tables.");
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
          queries.add("DROP TABLE droidseries");

          result = execTransaction(queries);
        }
        catch(Exception e) {
          System.out.println("Error deleting old schema.");
          result = false;
        }
      }

      // construct an entirely new schema
      // -----------------------------------------
      // notes:
      //   * afterward:
      //      needsUpdate(...) == false
      //   * which is required by api.addSeries(...),
      //     because methods in ApiGateway call methods in DbGateway that don't skipVersionCheck
      // -----------------------------------------
      if (result) {
        try {
          db.onCreate(/* SQLiteDatabase dbase */ null);
        }
        catch(Exception e) {
          System.out.println("Error creating new schema.");
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

          Cursor c = null;
          try {
            c = query(query);

            if ((c != null) && c.moveToFirst() && c.isFirst()) {
              do {
                try {
                  serieId  = c.getInt(0);
                  name     = c.getString(1);
                  language = c.getString(2);
                  archived = c.getInt(3);

                  System.out.println("    Downloading series: " + name);

                  // at this stage, don't halt reimport
                  oneResult = api.addSeries(serieId, language, (archived == 1));

                  if (!oneResult) {
                    databaseUpdateResult.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.add(serieId);
                    databaseUpdateResult.tmdbApiMigrationResult.seriesNamesThatFailedToAdd.add(name);

                    throw new Exception("Failed to re-add series: serieId=" + serieId + ", name='" + name + "', langCode=" + language + ", archived=" + ((archived == 1) ? "true" : "false"));
                  }
                }
                catch (Exception e) {
                  if (abortOnErrorAddingSeries)
                    throw e;
                  else
                    System.out.println(e.getMessage());
                }
              } while (c.moveToNext());
            }
          }
          finally {
            if (c != null) c.close();
          }
        }
        catch(Exception e) {
          System.out.println("Error re-adding TV series to new schema.");
          result = false;
        }
      }

      // rehydrate the DB: import user metadata from temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

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

          result = execTransaction(queries);
        }
        catch(Exception e) {
          System.out.println("Error importing user metadata for TV series from temporary tables to new schema.");
          result = false;
        }
      }

      // remove temporary tables
      if (result) {
        try {
          queries = new ArrayList<String>();

          queries.add("DROP TABLE IF EXISTS tmdb_migration_series");
          queries.add("DROP TABLE IF EXISTS tmdb_migration_episodes");

          execTransaction(queries);
        }
        catch(Exception e) {
          System.out.println("Error deleting temporary tables.");
        }
      }

      return result;
  }

  // ---------------------------------------------------------------------------

  public static void handleDatabaseUpdateResultErrors(int oldVersion, DatabaseUpdateResult result) {
    if (
      (oldVersion < 8) &&
      (
        !result.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.isEmpty() ||
        !result.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.isEmpty()
      )
    ) {
      StringBuilder content = new StringBuilder(256);
      String divider = (new String(new char[50]).replace("\0", "-")) + "\n";

      if (
        !result.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.isEmpty() &&
        (result.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.size() == result.tmdbApiMigrationResult.seriesNamesThatFailedToResolve.size())
      ) {
        content.append(divider);
        content.append(Strings.db_migration_log_file_heading_tvdb_series_ids_that_failed_to_resolve);
        content.append("\n\n");

        for (int i=0; i < result.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.size(); i++) {
          content.append("  ");
          content.append(result.tmdbApiMigrationResult.tvdbSeriesIdsThatFailedToResolve.get(i));
          content.append(", ");
          content.append(result.tmdbApiMigrationResult.seriesNamesThatFailedToResolve.get(i));
          content.append("\n");
        }
      }

      if (
        !result.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.isEmpty() &&
        (result.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.size() == result.tmdbApiMigrationResult.seriesNamesThatFailedToAdd.size())
      ) {
        content.append(divider);
        content.append(Strings.db_migration_log_file_heading_tmdb_series_ids_that_failed_to_add);
        content.append("\n\n");

        for (int i=0; i < result.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.size(); i++) {
          content.append("  ");
          content.append(result.tmdbApiMigrationResult.tmdbSeriesIdsThatFailedToAdd.get(i));
          content.append(", ");
          content.append(result.tmdbApiMigrationResult.seriesNamesThatFailedToAdd.get(i));
          content.append("\n");
        }
      }

      content.append(divider);

      FileUtils.writeToFile(content.toString(), DroidShowsDatabaseMigrationTool.log_errors);
    }
  }

  // ---------------------------------------------------------------------------

}
