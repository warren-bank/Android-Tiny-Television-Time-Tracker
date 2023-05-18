package com.github.warren_bank.tiny_television_time_tracker.database;

import com.github.warren_bank.tiny_television_time_tracker.DroidShowsDatabaseMigrationTool;
import com.github.warren_bank.tiny_television_time_tracker.utils.FileUtils;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.util.List;

public class SQLiteStore {
  private static SQLiteStore instance = null;
  private static String      DB_PATH  = null;

  private SQLiteDatabase db;

  public static SQLiteStore getInstance() throws Exception {
    if (instance == null) {
      DB_PATH = DroidShowsDatabaseMigrationTool.db_tv_tracker.getPath();

      instance = new SQLiteStore();
    }

    return instance;
  }

  private SQLiteStore() throws Exception {
    try {
      openDataBase();
    }
    catch (SQLException sqle) {
      throw new Exception("Unable to create TV-Tracker database");
    }
  }

  public void openDataBase() throws SQLException {
    db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
  }

  public Cursor query(String query) {
    boolean skipVersionCheck = false;
    return query(query, skipVersionCheck);
  }

  public Cursor query(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return null;

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
    boolean skipVersionCheck = false;
    return execQuery(query, skipVersionCheck);
  }

  public boolean execQuery(String query, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

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
    boolean skipVersionCheck = false;
    return execTransaction(queries, skipVersionCheck);
  }

  public boolean execTransaction(List<String> queries, boolean skipVersionCheck) {
    // only run queries against the current DB schema
    if (!skipVersionCheck && Update.needsUpdate(this)) return false;

    // validate input
    if ((queries == null) || queries.isEmpty()) return false;

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

  public synchronized void close() {
    if (db != null) db.close();
  }

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
          "INSERT INTO droidseries (version) VALUES ('" + Update.VERSION_STRING_CURRENT + "');"
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
          "CREATE TABLE IF NOT EXISTS unavailableEpisodes ("
        +   "serieId              INTEGER NOT NULL"                       + ", "
        +   "seasonNumber         INTEGER NOT NULL"                       + ", "
        +   "episodeNumber        INTEGER NOT NULL"                       + ", "
        +   "PRIMARY KEY (serieId, seasonNumber, episodeNumber)"          + ", "
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
      System.out.println(e.getMessage());
    }
    Update.resetVersionCache();
  }
}
