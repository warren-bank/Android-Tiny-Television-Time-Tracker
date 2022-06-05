package nl.asymmetrics.droidshows.database;

import nl.asymmetrics.droidshows.common.Constants;
import nl.asymmetrics.droidshows.utils.FileUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class SQLiteStore extends SQLiteOpenHelper {
  private static SQLiteStore instance = null;
  private static String      DB_PATH  = null;
  private static String      DB_NAME  = null;

  private SQLiteDatabase db;

  public static SQLiteStore getInstance(Context context) {
    if (instance == null) {
      DB_PATH = FileUtils.getDatabaseDirectoryPath(context) + "/";
      DB_NAME = FileUtils.getDatabaseFileName(context);

      instance = new SQLiteStore(context.getApplicationContext());
    }
    return instance;
  }

  private SQLiteStore(Context context) {
    super(context, DB_NAME, null, 1);

    try {
      openDataBase();
    }
    catch (SQLException sqle) {
      try {
        createDataBase();
        close();
        try {
          openDataBase();
        }
        catch (SQLException sqle2) {
          Log.e(Constants.LOG_TAG, sqle2.getMessage());
        }
      }
      catch (IOException e) {
        Log.e(Constants.LOG_TAG, "Unable to create database");
      }
    }
  }

  public void createDataBase() throws IOException {
    boolean dbExist = checkDataBase();
    if (!dbExist) {
      this.getWritableDatabase();
    }
  }

  private boolean checkDataBase() {
    SQLiteDatabase checkDB = null;
    try {
      String myPath = DB_PATH + DB_NAME;
      if (!FileUtils.exists(myPath)) throw new Exception("");

      checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
    catch (Exception e) {
      Log.d(Constants.LOG_TAG, "Database does't exist yet.");
    }
    if (checkDB != null) {
      checkDB.close();
    }
    return checkDB != null ? true : false;
  }

  @SuppressLint("NewApi")
  public void openDataBase() throws SQLException {
    String myPath = DB_PATH + DB_NAME;
    db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      db.disableWriteAheadLogging();
    }
  }

  public Cursor query(String query) {
    Cursor c = null;
    try {
      c = db.rawQuery(query, null);
    }
    catch (SQLiteException e) {
      return null;
    }
    return c;
  }

  public boolean execQuery(String query) {
    try {
      db.execSQL(query);
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public boolean execTransaction(List<String> queries) {
    boolean result = true;
    try {
      db.beginTransaction();
      for (String query : queries) {
        db.execSQL(query);
      }
      db.setTransactionSuccessful();
    }
    catch (SQLiteException e) {
      e.printStackTrace();
      result = false;
    }
    finally {
      db.endTransaction();
    }
    return result;
  }

  @Override
  public synchronized void close() {
    if (db != null) db.close();
    super.close();
  }

  @Override
  public void onCreate(SQLiteDatabase dbase) {
    if (dbase == null)
      dbase = db;
    if (dbase == null)
      return;

    try {
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS droidseries ("
        +   "version VARCHAR"
        + ");"
      );
      dbase.execSQL(
          "INSERT INTO droidseries (version) VALUES ('" + Update.currentVersion + "');"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS series ("
        +   "id                   INTEGER NOT NULL PRIMARY KEY"           + ", "
        +   "name                 VARCHAR"                                + ", " // formerly: serieName
        +   "overview             TEXT"                                   + ", "
        +   "seasonCount          INTEGER"                                + ", "
        +   "status               VARCHAR"                                + ", "
        +   "firstAired           VARCHAR"                                + ", "
        +   "imdbId               VARCHAR"                                + ", "
        +   "reviewRating         FLOAT"                                  + ", "  // formerly: rating
        +   "network              VARCHAR"                                + ", "
        +   "runtime              VARCHAR"                                + ", "
        +   "contentRating        VARCHAR"                                + ", "
        +   "language             VARCHAR"                                + ", "
        +   "largeImageUrl        VARCHAR"                                + ", "  // formerly: poster
        +   "smallImageFilePath   VARCHAR"                                + ", "  // formerly: posterThumb
        +   "mediumImageFilePath  VARCHAR"                                + ", "
        +   "archived             INTEGER NOT NULL DEFAULT 0"             + ", "  // formerly: passiveStatus
        +   "pinned               INTEGER NOT NULL DEFAULT 0"             + ", "
        +   "extResources         VARCHAR NOT NULL DEFAULT ''"            + ", "
        +   "unwatched            INTEGER"                                + ", "
        +   "unwatchedAired       INTEGER"                                + ", "
        +   "nextAir              VARCHAR"                                + ", "
        +   "nextEpisode          VARCHAR"                                + ", "
        +   "unwatchedLastAired   VARCHAR"                                + ", "
        +   "unwatchedLastEpisode VARCHAR"                                + ", "
        +   "lastUpdated          INTEGER NOT NULL DEFAULT (cast(strftime('%s','now') as INTEGER))"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS episodes ("
        +   "id                   INTEGER NOT NULL"                       + ", "
        +   "serieId              INTEGER NOT NULL"                       + ", "
        +   "seasonNumber         INTEGER NOT NULL"                       + ", "
        +   "episodeNumber        INTEGER NOT NULL"                       + ", "
        +   "name                 VARCHAR"                                + ", " // formerly: episodeName
        +   "overview             TEXT"                                   + ", "
        +   "firstAired           VARCHAR"                                + ", "
        +   "imdbId               VARCHAR"                                + ", "
        +   "reviewRating         FLOAT"                                  + ", "
        +   "seen                 INTEGER NOT NULL DEFAULT 0"             + ", "
        +   "PRIMARY KEY (serieId, id)"                                   + ", "
        +   "FOREIGN KEY (serieId) REFERENCES series (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_episodes_serieid_seasonnumber ON episodes (serieId, seasonNumber);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS seasons ("
        +   "serieId              INTEGER NOT NULL"                       + ", "
        +   "seasonNumber         INTEGER NOT NULL"                       + ", "
        +   "name                 VARCHAR"                                + ", "
        +   "episodeCount         INTEGER"                                + ", "
        +   "PRIMARY KEY (serieId, seasonNumber)"                         + ", "
        +   "FOREIGN KEY (serieId) REFERENCES series (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS genres ("
        +   "serieId INTEGER NOT NULL"                                    + ", "
        +   "genre   VARCHAR NOT NULL"                                    + ", "
        +   "FOREIGN KEY (serieId) REFERENCES series (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_genres_serieid ON genres (serieId);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS actors ("
        +   "serieId INTEGER NOT NULL"                                    + ", "
        +   "actor   VARCHAR NOT NULL"                                    + ", "
        +   "FOREIGN KEY (serieId) REFERENCES series (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_actors_serieid ON actors (serieId);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS writers ("
        +   "serieId   INTEGER NOT NULL"                                  + ", "
        +   "episodeId INTEGER NOT NULL"                                  + ", "
        +   "writer    VARCHAR NOT NULL"                                  + ", "
        +   "FOREIGN KEY (serieId)   REFERENCES series   (id)"            + ", "
        +   "FOREIGN KEY (episodeId) REFERENCES episodes (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_writers_serieid_episodeid ON writers (serieId, episodeId);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS directors ("
        +   "serieId   INTEGER NOT NULL"                                  + ", "
        +   "episodeId INTEGER NOT NULL"                                  + ", "
        +   "director  VARCHAR NOT NULL"                                  + ", "
        +   "FOREIGN KEY (serieId)   REFERENCES series   (id)"            + ", "
        +   "FOREIGN KEY (episodeId) REFERENCES episodes (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_directors_serieid_episodeid ON directors (serieId, episodeId);"
      );
      dbase.execSQL(
          "CREATE TABLE IF NOT EXISTS guestStars ("
        +   "serieId   INTEGER NOT NULL"                                  + ", "
        +   "episodeId INTEGER NOT NULL"                                  + ", "
        +   "guestStar VARCHAR NOT NULL"                                  + ", "
        +   "FOREIGN KEY (serieId)   REFERENCES series   (id)"            + ", "
        +   "FOREIGN KEY (episodeId) REFERENCES episodes (id)"
        + ");"
      );
      dbase.execSQL(
          "CREATE INDEX idx_gueststars_serieid_episodeid ON guestStars (serieId, episodeId);"
      );
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // TODO Auto-generated method stub
  }
}
