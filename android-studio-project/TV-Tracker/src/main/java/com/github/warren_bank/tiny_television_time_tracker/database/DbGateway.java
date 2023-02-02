package com.github.warren_bank.tiny_television_time_tracker.database;

import com.github.warren_bank.tiny_television_time_tracker.DroidShows;
import com.github.warren_bank.tiny_television_time_tracker.common.Constants;
import com.github.warren_bank.tiny_television_time_tracker.common.DateFormats;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbActor;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbDirector;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGenre;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGuestStar;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeason;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeries;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbUnavailableEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbWriter;
import com.github.warren_bank.tiny_television_time_tracker.database.model.EpisodeSeen;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.BaseEpisode;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.EpisodeRow;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.NextEpisode;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.SeasonRow;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.TVShowItem;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.UnwatchedLastAiredEpisode;
import com.github.warren_bank.tiny_television_time_tracker.utils.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DbGateway {
  private static DbGateway instance = null;

  private Context     context;
  private SQLiteStore db;
  private String      today;

  public static DbGateway getInstance(Context context) {
    if (instance == null) {
      instance = new DbGateway(context.getApplicationContext());
    }
    return instance;
  }

  private DbGateway(Context context) {
    this.context = context;
    this.db      = SQLiteStore.getInstance(context);
    this.today   = DateFormats.getNormalizedDate();
  }

  public SQLiteStore getSQLiteStore() {
    return this.db;
  }

  public void updateToday(String newToday) {
    this.today = newToday;
  }

  // ---------------------------------------------------------------------------
  // Helpers:
  // ---------------------------------------------------------------------------

  private Date getDateFromEpoch(int seconds) {
    long ms = DateFormats.convertSecondsToMs(seconds);
    return new Date(ms);
  }

  private String normalizeEmptyString(String value) {
    String defaultValue = "";
    return normalizeEmptyString(value, defaultValue);
  }

  private String normalizeEmptyString(String value, String defaultValue) {
    List<String> blacklist = new ArrayList<String>();
    blacklist.add("-1");
    return normalizeEmptyString(value, defaultValue, blacklist);
  }

  private String normalizeEmptyString(String value, String defaultValue, List<String> blacklist) {
    return ((value == null) || value.isEmpty() || value.toLowerCase().equals("null"))
      ? defaultValue
      : ((blacklist != null) && blacklist.contains(value))
        ? defaultValue
        : value;
  }

  private Date parseNormalizedDate(String value) {
    value = normalizeEmptyString(value, "");
    if (value.isEmpty()) return null;

    try {
      return DateFormats.NORMALIZE_DATE.parse(value);
    }
    catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String getColumnString(Cursor c, String columnName) {
    String defaultValue = "";
    return getColumnString(c, columnName, defaultValue);
  }

  private String getColumnString(Cursor c, String columnName, String defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    return normalizeEmptyString(
      c.getString(columnIndex),
      defaultValue
    );
  }

  private int getColumnInteger(Cursor c, String columnName) {
    int defaultValue = -1;
    return getColumnInteger(c, columnName, defaultValue);
  }

  private int getColumnInteger(Cursor c, String columnName, int defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = c.getInt(columnIndex);
    return value;
  }

  private float getColumnFloat(Cursor c, String columnName) {
    float defaultValue = -1.0f;
    return getColumnFloat(c, columnName, defaultValue);
  }

  private float getColumnFloat(Cursor c, String columnName, float defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    float value = c.getFloat(columnIndex);
    return value;
  }

  private boolean getColumnBoolean(Cursor c, String columnName) {
    boolean defaultValue = false;
    return getColumnBoolean(c, columnName, defaultValue);
  }

  private boolean getColumnBoolean(Cursor c, String columnName, boolean defaultValue) {
    int columnIndex = c.getColumnIndex(columnName);

    if (c.isNull(columnIndex)) return defaultValue;

    int value = getColumnInteger(c, columnName, 0);
    return (value == 1);
  }

  private String combineSelectFields(String[] fields) {
    return ((fields == null) || (fields.length == 0))
      ? "*"
      : TextUtils.join(", ", fields);
  }

  // ---------------------------------------------------------------------------
  // Read: DB Models
  // ---------------------------------------------------------------------------

  public DbSeries getDbSeries(int serieId) {
    return getDbSeries(serieId, null);
  }

  public DbSeries getDbSeries(int serieId, String[] fields) {
    DbSeries series = new DbSeries(serieId);

    String query = "SELECT " + combineSelectFields(fields)
      + " FROM"
      + "   series"
      + " WHERE"
      + "   id = " + serieId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        String[] columnNames = c.getColumnNames();
        String   columnType  = null;

        for (String columnName : columnNames) {
          switch(columnName) {
            case "name":
            case "overview":
            case "status":
            case "firstAired":
            case "imdbId":
            case "network":
            case "runtime":
            case "contentRating":
            case "language":
            case "largeImageUrl":
            case "smallImageFilePath":
            case "mediumImageFilePath":
            case "extResources":
            case "nextAir":
            case "nextEpisode":
            case "unwatchedLastAired":
            case "unwatchedLastEpisode":
              columnType = "string";
              break;

            case "id":
            case "seasonCount":
            case "archived":
            case "pinned":
            case "unwatched":
            case "unwatchedAired":
            case "lastUpdated":
              columnType = "int";
              break;

            case "reviewRating":
              columnType = "float";
              break;

            default:
              columnType = "";
              Log.d(Constants.LOG_TAG, "Unexpected column in DB. Table: 'series'. Field: '" + columnName + "'.");
              break;
          }

          try {
            // use reflection to update field in object of class: DbSeries

            switch(columnType) {
              case "string":
                String columnValue1 = getColumnString(c, columnName);
                (DbSeries.class).getDeclaredField(columnName).set(series, columnValue1);
                break;
              case "int":
                int columnValue2 = getColumnInteger(c, columnName);
                (DbSeries.class).getDeclaredField(columnName).set(series, columnValue2);
                break;
              case "float":
                float columnValue3 = getColumnFloat(c, columnName);
                (DbSeries.class).getDeclaredField(columnName).set(series, columnValue3);
                break;
              case "boolean":
                boolean columnValue4 = getColumnBoolean(c, columnName);
                (DbSeries.class).getDeclaredField(columnName).set(series, columnValue4);
                break;
            }
          }
          catch(Exception e) {}
        }
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return series;
  }

  // ---------------------------------------------------------------------------

  public DbEpisode getDbEpisode(int serieId, int episodeId) {
    return getDbEpisode(serieId, episodeId, null);
  }

  public DbEpisode getDbEpisode(int serieId, int episodeId, String[] fields) {
    DbEpisode episode = new DbEpisode(episodeId, serieId);

    String query = "SELECT " + combineSelectFields(fields)
      + " FROM"
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   id = " + episodeId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        String[] columnNames = c.getColumnNames();
        String   columnType  = null;

        for (String columnName : columnNames) {
          switch(columnName) {
            case "name":
            case "overview":
            case "firstAired":
            case "imdbId":
              columnType = "string";
              break;

            case "id":
            case "serieId":
            case "seasonNumber":
            case "episodeNumber":
            case "seen":
              columnType = "int";
              break;

            case "reviewRating":
              columnType = "float";
              break;

            default:
              columnType = "";
              Log.d(Constants.LOG_TAG, "Unexpected column in DB. Table: 'episodes'. Field: '" + columnName + "'.");
              break;
          }

          try {
            // use reflection to update field in object of class: DbEpisode

            switch(columnType) {
              case "string":
                String columnValue1 = getColumnString(c, columnName);
                (DbEpisode.class).getDeclaredField(columnName).set(episode, columnValue1);
                break;
              case "int":
                int columnValue2 = getColumnInteger(c, columnName);
                (DbEpisode.class).getDeclaredField(columnName).set(episode, columnValue2);
                break;
              case "float":
                float columnValue3 = getColumnFloat(c, columnName);
                (DbEpisode.class).getDeclaredField(columnName).set(episode, columnValue3);
                break;
              case "boolean":
                boolean columnValue4 = getColumnBoolean(c, columnName);
                (DbEpisode.class).getDeclaredField(columnName).set(episode, columnValue4);
                break;
            }
          }
          catch(Exception e) {}
        }
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return episode;
  }

  // ---------------------------------------------------------------------------

  public List<DbSeason> getDbSeason(int serieId) {
    List<DbSeason> seasons = new ArrayList<DbSeason>();

    int    seasonNumber, episodeCount;
    String name;

    String query = "SELECT seasonNumber, name, episodeCount"
      + " FROM "
      + "   seasons"
      + " WHERE"
      + "   serieId = " + serieId
      + " ORDER BY"
      + "   seasonNumber ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          seasonNumber = getColumnInteger(c, "seasonNumber");
          name         = getColumnString (c, "name");
          episodeCount = getColumnInteger(c, "episodeCount");

          DbSeason season = new DbSeason(serieId, seasonNumber, name, episodeCount);
          seasons.add(season);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return seasons;
  }

  public DbSeason getDbSeason(int serieId, int seasonNumber) {
    List<DbSeason> seasons = getDbSeason(serieId);

    for (DbSeason season : seasons) {
      if (season.seasonNumber == seasonNumber) {
        return season;
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------

  public List<DbGenre> getDbGenre(int serieId) {
    List<DbGenre> genres = new ArrayList<DbGenre>();

    String value;

    String query = "SELECT genre"
      + " FROM "
      + "   genres"
      + " WHERE"
      + "   serieId = " + serieId
      + " ORDER BY"
      + "   genre ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "genre");

          DbGenre genre = new DbGenre(serieId, value);
          genres.add(genre);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return genres;
  }

  // ---------------------------------------------------------------------------

  public List<DbActor> getDbActor(int serieId) {
    List<DbActor> actors = new ArrayList<DbActor>();

    String value;

    String query = "SELECT actor"
      + " FROM "
      + "   actors"
      + " WHERE"
      + "   serieId = " + serieId
      + " ORDER BY"
      + "   actor ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "actor");

          DbActor actor = new DbActor(serieId, value);
          actors.add(actor);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return actors;
  }

  // ---------------------------------------------------------------------------

  public List<DbWriter> getDbWriter(int serieId, int episodeId) {
    List<DbWriter> writers = new ArrayList<DbWriter>();

    String value;

    String query = "SELECT writer"
      + " FROM "
      + "   writers"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   episodeId = " + episodeId
      + " ORDER BY"
      + "   writer ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "writer");

          DbWriter writer = new DbWriter(serieId, episodeId, value);
          writers.add(writer);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return writers;
  }

  // ---------------------------------------------------------------------------

  public List<DbDirector> getDbDirector(int serieId, int episodeId) {
    List<DbDirector> directors = new ArrayList<DbDirector>();

    String value;

    String query = "SELECT director"
      + " FROM "
      + "   directors"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   episodeId = " + episodeId
      + " ORDER BY"
      + "   director ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "director");

          DbDirector director = new DbDirector(serieId, episodeId, value);
          directors.add(director);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return directors;
  }

  // ---------------------------------------------------------------------------

  public List<DbGuestStar> getDbGuestStar(int serieId, int episodeId) {
    List<DbGuestStar> guestStars = new ArrayList<DbGuestStar>();

    String value;

    String query = "SELECT guestStar"
      + " FROM "
      + "   guestStars"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   episodeId = " + episodeId
      + " ORDER BY"
      + "   guestStar ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          value = getColumnString(c, "guestStar");

          DbGuestStar guestStar = new DbGuestStar(serieId, episodeId, value);
          guestStars.add(guestStar);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return guestStars;
  }

  // ---------------------------------------------------------------------------

  public List<EpisodeSeen> getEpisodeSeen(int serieId) {
    boolean includeUnseen = false;
    return getEpisodeSeen(serieId, includeUnseen);
  }

  public List<EpisodeSeen> getEpisodeSeen(int serieId, boolean includeUnseen) {
    boolean includeUnavailable = false;
    return getEpisodeSeen(serieId, includeUnseen, includeUnavailable);
  }

  public List<EpisodeSeen> getEpisodeSeen(int serieId, boolean includeUnseen, boolean includeUnavailable) {
    List<EpisodeSeen> episodes = new ArrayList<EpisodeSeen>();

    int episodeId, seasonNumber, episodeNumber, seen;

    String query = "SELECT id, seasonNumber, episodeNumber, seen"
      + " FROM "
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + (includeUnseen
          ? ""
          : " AND seen>0"
        )
      + " ORDER BY"
      + "   seasonNumber ASC, episodeNumber ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          episodeId     = getColumnInteger(c, "id");
          seasonNumber  = getColumnInteger(c, "seasonNumber");
          episodeNumber = getColumnInteger(c, "episodeNumber");
          seen          = getColumnInteger(c, "seen");

          EpisodeSeen episode = new EpisodeSeen(episodeId, seasonNumber, episodeNumber, seen);
          episodes.add(episode);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    if (includeUnavailable) {
      episodes.addAll(
        getUnavailableEpisodes(serieId)
      );
      Collections.sort(episodes);
    }

    return episodes;
  }

  public List<EpisodeSeen> getUnavailableEpisodes(int serieId) {
    List<EpisodeSeen> episodes = new ArrayList<EpisodeSeen>();

    int episodeId = EpisodeSeen.UNAVAILABLE_EPISODE_ID;
    int seen      = 0;
    int seasonNumber, episodeNumber;

    String query = "SELECT seasonNumber, episodeNumber"
      + " FROM "
      + "   unavailableEpisodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " ORDER BY"
      + "   seasonNumber ASC, episodeNumber ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          seasonNumber  = getColumnInteger(c, "seasonNumber");
          episodeNumber = getColumnInteger(c, "episodeNumber");

          EpisodeSeen episode = new EpisodeSeen(episodeId, seasonNumber, episodeNumber, seen);
          episodes.add(episode);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return episodes;
  }

  // ---------------------------------------------------------------------------
  // related utilities

  public boolean hasEpisodeSeen(List<EpisodeSeen> episodes, int episodeId) {
    EpisodeSeen found = findEpisodeSeen(episodes, episodeId);
    return (found != null);
  }

  public EpisodeSeen findEpisodeSeen(List<EpisodeSeen> episodes, int episodeId) {
    EpisodeSeen found = null;
    if ((episodes != null) && !episodes.isEmpty() && (episodeId != EpisodeSeen.UNAVAILABLE_EPISODE_ID)) {
      for (EpisodeSeen episode : episodes) {
        if (episode.episodeId == episodeId) {
          found = episode;
          break;
        }
      }
    }
    return found;
  }

  // ---------------------------------------------------------------------------

  public boolean hasEpisodeSeen(List<EpisodeSeen> episodes, int seasonNumber, int episodeNumber) {
    EpisodeSeen found = findEpisodeSeen(episodes, seasonNumber, episodeNumber);
    return (found != null);
  }

  public EpisodeSeen findEpisodeSeen(List<EpisodeSeen> episodes, int seasonNumber, int episodeNumber) {
    EpisodeSeen found = null;
    if ((episodes != null) && !episodes.isEmpty()) {
      for (EpisodeSeen episode : episodes) {
        if ((episode.seasonNumber == seasonNumber) && (episode.episodeNumber == episodeNumber)) {
          found = episode;
          break;
        }
        if (episode.seasonNumber > seasonNumber) {
          // episodes are sorted in ascending order; short-circuit loop after passing the desired season.
          break;
        }
      }
    }
    return found;
  }

  // ---------------------------------------------------------------------------
  // Read: UI Models
  // ---------------------------------------------------------------------------

  public TVShowItem createTVShowItem(int serieId) {
    TVShowItem tvsi = null;

    String  name, status, language, smallImageFilePath, extResources, nextEpisode, unwatchedLastEpisode;
    int     seasonCount, unwatched, unwatchedAired;
    boolean archived, pinned;
    Date    nextAir, unwatchedLastAired;

    String query = "SELECT name, seasonCount, status, language, smallImageFilePath, archived, pinned, extResources, unwatched, unwatchedAired, nextAir, nextEpisode, unwatchedLastAired, unwatchedLastEpisode"
      + " FROM"
      + "   series"
      + " WHERE"
      + "   id = " + serieId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        name                 = getColumnString (c, "name");
        seasonCount          = getColumnInteger(c, "seasonCount");
        status               = getColumnString (c, "status");
        language             = getColumnString (c, "language");
        smallImageFilePath   = getColumnString (c, "smallImageFilePath");
        archived             = getColumnBoolean(c, "archived");
        pinned               = getColumnBoolean(c, "pinned");
        extResources         = getColumnString (c, "extResources");
        unwatched            = getColumnInteger(c, "unwatched");
        unwatchedAired       = getColumnInteger(c, "unwatchedAired");
        nextAir              = parseNormalizedDate( getColumnString (c, "nextAir") );
        nextEpisode          = getColumnString (c, "nextEpisode");
        unwatchedLastAired   = parseNormalizedDate( getColumnString (c, "unwatchedLastAired") );
        unwatchedLastEpisode = getColumnString (c, "unwatchedLastEpisode");

        tvsi = new TVShowItem(serieId, name, seasonCount, status, language, /* String iconFilePath */ smallImageFilePath, /* Bitmap iconBitmap= */ null, archived, pinned, extResources, unwatched, unwatchedAired, nextAir, nextEpisode, unwatchedLastAired, unwatchedLastEpisode);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return tvsi;
  }

  public List<TVShowItem> createTVShowItems(int showArchive, boolean filterNetworks, List<String> showNetworks) {
    List<TVShowItem> items = new ArrayList<TVShowItem>();

    List<Integer> ids = getSerieIds(showArchive, filterNetworks, showNetworks);
    if ((ids != null) && !ids.isEmpty()) {
      for (int serieId : ids) {
        TVShowItem tvsi = createTVShowItem(serieId);
        items.add(tvsi);
      }
    }

    return items;
  }

  // ---------------------------------------------------------------------------

  public List<TVShowItem> getLog() {
    return getLog(0);
  }

  public List<TVShowItem> getLog(int offset) {
    List<TVShowItem> episodes = new ArrayList<TVShowItem>();

    int    episodeId, serieId, seasonNumber, episodeNumber, seen;
    String episodeName, episodeSeen;

    String query = "SELECT id, serieId, seasonNumber, episodeNumber, name, seen"
      + " FROM "
      + "   episodes"
      + " WHERE"
      + "   seen>1"
      + " ORDER BY"
      + "   seen DESC, serieId DESC, episodeNumber DESC"
      + " LIMIT 25"
      + " OFFSET " + offset;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          episodeId     = getColumnInteger(c, "id");
          serieId       = getColumnInteger(c, "serieId");
          seasonNumber  = getColumnInteger(c, "seasonNumber");
          episodeNumber = getColumnInteger(c, "episodeNumber");
          episodeName   = getColumnString (c, "name");
          seen          = getColumnInteger(c, "seen", 0);

          episodeName = seasonNumber + (episodeNumber < 10 ? "x0" : "x") + episodeNumber + " " + episodeName;
          episodeSeen = DateFormats.DISPLAY_DATE_TIME.format(getDateFromEpoch(seen));

          TVShowItem episode = createTVShowItem(serieId);
          episode.setEpisodeId(episodeId);
          episode.setEpisodeName(episodeName);
          episode.setEpisodeSeen(episodeSeen);
          episodes.add(episode);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return episodes;
  }

  // ---------------------------------------------------------------------------

  public EpisodeRow getEpisodeRow(int serieId, int seasonNumber, int episodeId) {
    return getEpisodeRows(serieId, seasonNumber, episodeId).get(0);
  }

  public List<EpisodeRow> getEpisodeRows(int serieId, int seasonNumber) {
    return getEpisodeRows(serieId, seasonNumber, -1);
  }

  private List<EpisodeRow> getEpisodeRows(int serieId, int seasonNumber, int episodeId) {
    List<EpisodeRow> episodes = new ArrayList<EpisodeRow>();

    String name, firstAired;
    int    episodeNumber, seen;
    Date   firstAiredDate;

    String query = "SELECT id, episodeNumber, name, firstAired, seen"
      + " FROM "
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seasonNumber = " + seasonNumber
      +     ((episodeId <= 0) ? "" : (" AND id = " + episodeId))
      + " ORDER BY"
      + "   episodeNumber ASC";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          episodeId      = getColumnInteger(c, "id");
          episodeNumber  = getColumnInteger(c, "episodeNumber");
          name           = getColumnString (c, "name");
          name           = episodeNumber + ". " + name;
          firstAired     = getColumnString (c, "firstAired", "");
          firstAiredDate = null;
          seen           = getColumnInteger(c, "seen", 0);

          if (!firstAired.isEmpty()) {
            try {
              firstAiredDate = DateFormats.NORMALIZE_DATE.parse(firstAired);
              firstAired     = DateFormats.DISPLAY_DATE.format(firstAiredDate);
            }
            catch(Exception e) {
              Log.e(Constants.LOG_TAG, "Failed to parse Date string: '" + firstAired + "'");
            }
          }

          EpisodeRow episode = new EpisodeRow(episodeId, name, firstAired, firstAiredDate, seen);
          episodes.add(episode);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return episodes;
  }

  // ---------------------------------------------------------------------------

  public List<SeasonRow> getSeasonRows(int serieId) {
    List<SeasonRow> seasons = new ArrayList<SeasonRow>();

    List<DbSeason> dbSeasons = getDbSeason(serieId);
    for (DbSeason dbSeason : dbSeasons) {
      SeasonRow season = new SeasonRow(dbSeason);
      seasons.add(season);
    }
    return seasons;
  }

  // ---------------------------------------------------------------------------

  public NextEpisode getNextEpisode(int serieId) {
    int     seasonNumber   = -1;
    boolean showNextAiring = false;
    return getNextEpisode(serieId, seasonNumber, showNextAiring);
  }

  public NextEpisode getNextEpisode(int serieId, int seasonNumber) {
    boolean showNextAiring = false;
    return getNextEpisode(serieId, seasonNumber, showNextAiring);
  }

  public NextEpisode getNextEpisode(int serieId, boolean showNextAiring) {
    int seasonNumber = -1;
    return getNextEpisode(serieId, seasonNumber, showNextAiring);
  }

  private NextEpisode getNextEpisode(int serieId, int seasonNumber, boolean showNextAiring) {
    NextEpisode nextEpisode = null;

    int    episodeNumber;
    String firstAired;
    Date   firstAiredDate;
    String query = "";

    if (seasonNumber < 0) {
      int[] lastWatched = null;

      if (!showNextAiring && DroidShows.markFromLastWatched) {
        lastWatched = getLastWatchedEpisode(serieId);
      }

      query = "SELECT seasonNumber, episodeNumber, firstAired"
        + " FROM"
        + "   episodes"
        + " WHERE"
        + "   serieId = " + serieId
        + " AND"
        + "   seen=0"
        + ((lastWatched != null)
            ? (" AND ((seasonNumber = " + lastWatched[0] + " AND episodeNumber > " + lastWatched[1] + ") OR (seasonNumber > " + lastWatched[0] + "))")
            : ""
          )
        + (DroidShows.includeSpecialsOption
            ? ""
            : " AND seasonNumber <> 0"
          )
        + (showNextAiring
            ? (" AND firstAired >= " + "'" + today + "'")
            : ""
          )
        + " ORDER BY"
        + "   seasonNumber ASC, episodeNumber ASC"
        + " LIMIT 1";
    }
    else {
      query = "SELECT seasonNumber, episodeNumber, firstAired"
        + " FROM"
        + "   episodes"
        + " WHERE"
        + "   serieId = " + serieId
        + " AND"
        + "   seasonNumber = " + seasonNumber
        + " AND"
        + "   seen=0"
        + " ORDER BY"
        + "   episodeNumber ASC"
        + " LIMIT 1";
    }

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        seasonNumber   = getColumnInteger(c, "seasonNumber");
        episodeNumber  = getColumnInteger(c, "episodeNumber");
        firstAired     = getColumnString (c, "firstAired", "");
        firstAiredDate = parseNormalizedDate(firstAired);

        nextEpisode = new NextEpisode(serieId, seasonNumber, episodeNumber, firstAired, firstAiredDate);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    if ((nextEpisode != null) && showNextAiring && nextEpisode.firstAired.isEmpty()) {
      nextEpisode = null;
    }

    if (nextEpisode == null) {
      nextEpisode = new NextEpisode(serieId, -1, -1, "", null);
    }

    return nextEpisode;
  }

  // ---------------------------------------------------------------------------

  public UnwatchedLastAiredEpisode getUnwatchedLastAiredEpisode(int serieId) {
    UnwatchedLastAiredEpisode unwatchedLastAiredEpisode = null;

    int    seasonNumber, episodeNumber;
    String firstAired;
    Date   firstAiredDate;

    String query = "SELECT seasonNumber, episodeNumber, firstAired"
      + " FROM"
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seen=0"
      + " AND"
      + "   firstAired < " + "'" + today + "'"
      + " AND"
      + "   firstAired <> ''"
      + (DroidShows.includeSpecialsOption
          ? ""
          : " AND seasonNumber <> 0"
        )
      + " ORDER BY"
      + "   firstAired DESC"
      + " LIMIT 1";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        seasonNumber   = getColumnInteger(c, "seasonNumber");
        episodeNumber  = getColumnInteger(c, "episodeNumber");
        firstAired     = getColumnString (c, "firstAired");
        firstAiredDate = parseNormalizedDate(firstAired);

        unwatchedLastAiredEpisode = new UnwatchedLastAiredEpisode(serieId, seasonNumber, episodeNumber, firstAired, firstAiredDate);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    if (unwatchedLastAiredEpisode == null) {
      unwatchedLastAiredEpisode = new UnwatchedLastAiredEpisode(serieId, -1, -1, "", null);
    }

    return unwatchedLastAiredEpisode;
  }

  // ---------------------------------------------------------------------------

  public String getNextEpisodeString(BaseEpisode nextEpisode) {
    boolean showNextAiring = false;
    return getNextEpisodeString(nextEpisode, showNextAiring);
  }

  public String getNextEpisodeString(BaseEpisode nextEpisode, boolean showNextAiring) {
    boolean requireAiredDate = false;
    return getEpisodeString(nextEpisode, showNextAiring, requireAiredDate);
  }

  private String getEpisodeString(BaseEpisode baseEpisode, boolean showNextAiring, boolean requireAiredDate) {
    if ((baseEpisode == null) || (baseEpisode.episode == -1))
      return "";

    if (!showNextAiring && requireAiredDate && (baseEpisode.firstAiredDate == null))
      return "";

    String baseEpisodeString = baseEpisode.season
      + ((baseEpisode.episode < 10) ? "x0" : "x")
      + baseEpisode.episode;

    if (showNextAiring) {
      NextEpisode nextEpisodeAiring = getNextEpisode(baseEpisode.serieId, true);
      if (nextEpisodeAiring != null) {
        return baseEpisodeString
          + " | [na] "
          + nextEpisodeAiring.season
          + ((nextEpisodeAiring.episode < 10) ? "x0" : "x")
          + nextEpisodeAiring.episode
          + ((nextEpisodeAiring.firstAiredDate != null)
              ? (" [on] " + DateFormats.DISPLAY_DATE.format(nextEpisodeAiring.firstAiredDate))
              : ""
            );
      }
    }

    return "[ne] "+ baseEpisodeString
      + ((baseEpisode.firstAiredDate != null)
          ? (" [on] " + DateFormats.DISPLAY_DATE.format(baseEpisode.firstAiredDate))
          : ""
        );
  }

  // ---------------------------------------------------------------------------
  // Read: Assorted columns from table 'series'
  // ---------------------------------------------------------------------------

  // note: 'showArchive' (0: not archived, 1: is archived, >=2: all series)
  public List<Integer> getSerieIds(int showArchive, boolean filterNetworks, List<String> showNetworks) {
    List<Integer> ids = new ArrayList<Integer>();

    int id;

    String networks, showArchiveString, showNetworksString, query;

    networks = (filterNetworks && (showNetworks != null) && !showNetworks.isEmpty())
      ? ("('" + TextUtils.join("', '", showNetworks) + "')")
      : null;

    showArchiveString = ((showArchive < 2)
      ? (
            " WHERE (archived"
          + ((showArchive == 0)
              ? "=0 OR archived IS NULL)"
              : ">=1)"
            )
        )
      : ""
    );

    showNetworksString = ((networks != null)
      ? ((showArchiveString.isEmpty() ? " WHERE" : " AND") + " network IN " + networks)
      : ""
    );

    query = "SELECT id FROM series" + showArchiveString + showNetworksString;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          id = getColumnInteger(c, "id");

          ids.add(id);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return ids;
  }

  // ---------------------------------------------------------------------------

  public List<String> getNetworks() {
    List<String> networks = new ArrayList<String>();

    String network;

    String query = "SELECT DISTINCT network FROM series";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        do {
          network = getColumnString(c, "network");

          networks.add(network);
        } while (c.moveToNext());
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    Collections.sort(networks, String.CASE_INSENSITIVE_ORDER);
    return networks;
  }

  // ---------------------------------------------------------------------------

  public String getSerieName(int serieId) {
    String value = null;

    String query = "SELECT name FROM series WHERE id=" + serieId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnString(c, "name", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------

  public String getSerieIMDbId(int serieId) {
    String value = null;

    String query = "SELECT imdbId FROM series WHERE id=" + serieId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnString(c, "imdbId", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------
  // Read: Assorted columns from table 'episodes'
  // ---------------------------------------------------------------------------

  public int getSeasonEpisodeCount(int serieId, int seasonNumber) {
    int value = 0;

    String query = "SELECT count(id) AS episodes_in_season FROM episodes"
      + " WHERE"
      + "   serieId=" + serieId
      + " AND"
      + "   seasonNumber=" + seasonNumber;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnInteger(c, "episodes_in_season", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------

  public int getEpsWatched(int serieId) {
    int value = 0;

    String query = "SELECT count(id) AS watched FROM episodes"
      + " WHERE"
      + "   serieId=" + serieId
      + " AND"
      + "   seen>0"
      + (DroidShows.includeSpecialsOption
          ? ""
          : " AND seasonNumber <> 0"
        );

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnInteger(c, "watched", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------

  public int getEpsUnwatchedAired(int serieId) {
    int seasonNumber = -1;
    return getEpsUnwatchedAired(serieId, seasonNumber);
  }

  public int getEpsUnwatchedAired(int serieId, int seasonNumber) {
    boolean onlyHasAired = true;
    return getEpsUnwatched(serieId, seasonNumber, onlyHasAired);
  }

  public int getEpsUnwatched(int serieId) {
    int seasonNumber = -1;
    return getEpsUnwatched(serieId, seasonNumber);
  }

  public int getEpsUnwatched(int serieId, int seasonNumber) {
    boolean onlyHasAired = false;
    return getEpsUnwatched(serieId, seasonNumber, onlyHasAired);
  }

  private int getEpsUnwatched(int serieId, int seasonNumber, boolean onlyHasAired) {
    int value = 0;

    String query = "SELECT count(id) AS unwatched FROM episodes"
      + " WHERE"
      + "   serieId=" + serieId
      + " AND"
      + "   seen=0"
      + (onlyHasAired
          ? (
                " AND"
              + "   firstAired < " + "'" + today + "'"
              + " AND"
              + "   firstAired <> ''"
            )
          : ""
        )
      + ((seasonNumber >= 0)
          ? (" AND seasonNumber=" + seasonNumber)
          : (DroidShows.includeSpecialsOption
              ? ""
              : " AND seasonNumber <> 0"
            )
        );

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnInteger(c, "unwatched", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------

  public int getNextEpisodeId(int serieId) {
    return getNextEpisodeId(serieId, false);
  }

  public int getNextEpisodeId(int serieId, boolean noFutureEp) {
    int   id = -1;
    int[] lastWatched = null;

    if (DroidShows.markFromLastWatched) {
      lastWatched = getLastWatchedEpisode(serieId);
    }

    String query = "SELECT id, seasonNumber, episodeNumber"
      + " FROM"
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seen=0"
      + ((lastWatched != null)
          ? (" AND ((seasonNumber = " + lastWatched[0] + " AND episodeNumber > " + lastWatched[1] + ") OR (seasonNumber > " + lastWatched[0] + "))")
          : ""
        )
      + (DroidShows.includeSpecialsOption
          ? ""
          : " AND seasonNumber <> 0"
        )
      + (noFutureEp
          ? (
                " AND"
              + "   firstAired <= " + "'" + today + "'"
              + " AND"
              + "   firstAired <> ''"
            )
          : ""
        )
      + " ORDER BY"
      + "   seasonNumber ASC, episodeNumber ASC"
      + " LIMIT 1";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        id = getColumnInteger(c, "id");
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return id;
  }

  // ---------------------------------------------------------------------------

  private int[] getLastWatchedEpisode(int serieId) {
    int[] lastSeen = null;

    int seasonNumber, episodeNumber;

    String query = "SELECT seasonNumber, episodeNumber FROM episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seen>=1"
      + " ORDER BY"
      + "   seen DESC, seasonNumber DESC, episodeNumber DESC"
      + " LIMIT 1";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        seasonNumber  = getColumnInteger(c, "seasonNumber");
        episodeNumber = getColumnInteger(c, "episodeNumber");

        lastSeen = new int[]{seasonNumber, episodeNumber};
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return lastSeen;
  }

  // ---------------------------------------------------------------------------

  public String getEpisodeIMDbId(int serieId, int episodeId) {
    String value = null;

    String query = "SELECT imdbId FROM episodes WHERE serieId=" + serieId + " AND id=" + episodeId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnString(c, "imdbId", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------
  // Read: Assorted columns from table 'seasons'
  // ---------------------------------------------------------------------------

  public int getSeasonCount(int serieId) {
    int value = 0;

    String query = "SELECT count(seasonNumber) AS seasons_in_series FROM seasons"
      + " WHERE"
      + "   serieId=" + serieId
      + " AND"
      + "   seasonNumber <> 0";

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        value = getColumnInteger(c, "seasons_in_series", value);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return value;
  }

  // ---------------------------------------------------------------------------
  // Write: Insert from DB Model
  // ---------------------------------------------------------------------------

  public boolean saveDbSeries(DbSeries series) {
    boolean useCurrentTimestamp = true;
    return saveDbSeries(series, useCurrentTimestamp);
  }

  public boolean saveDbSeries(DbSeries series, boolean useCurrentTimestamp) {
    String query = "INSERT INTO series"
      + "   (id, name, overview, seasonCount, status, firstAired, imdbId, reviewRating, network, runtime, contentRating, language, largeImageUrl, smallImageFilePath, mediumImageFilePath, archived, pinned, extResources, unwatched, unwatchedAired, nextAir, nextEpisode, unwatchedLastAired, unwatchedLastEpisode"
      +               ((useCurrentTimestamp || (series.lastUpdated <= 0))
                        ? ""
                        : ", lastUpdated"
                      )
      + "   )"
      + " VALUES"
      + "   ("
      +               series.id                                                + ", "
      +               DatabaseUtils.sqlEscapeString(series.name)               + ", "
      +               DatabaseUtils.sqlEscapeString(series.overview)           + ", "
      +               series.seasonCount                                       + ", "
      +         "'" + series.status                                      + "'" + ", "
      +         "'" + series.firstAired                                  + "'" + ", "
      +         "'" + series.imdbId                                      + "'" + ", "
      +               series.reviewRating                                      + ", "
      +         "'" + series.network                                     + "'" + ", "
      +         "'" + series.runtime                                     + "'" + ", "
      +         "'" + series.contentRating                               + "'" + ", "
      +         "'" + series.language                                    + "'" + ", "
      +         "'" + series.largeImageUrl                               + "'" + ", "
      +         "'" + series.smallImageFilePath                          + "'" + ", "
      +         "'" + series.mediumImageFilePath                         + "'" + ", "
      +               series.archived                                          + ", "
      +               series.pinned                                            + ", "
      +               DatabaseUtils.sqlEscapeString(series.extResources)       + ", "
      +               series.unwatched                                         + ", "
      +               series.unwatchedAired                                    + ", "
      +         "'" + series.nextAir                                     + "'" + ", "
      +         "'" + series.nextEpisode                                 + "'" + ", "
      +         "'" + series.unwatchedLastAired                          + "'" + ", "
      +         "'" + series.unwatchedLastEpisode                        + "'"
      +               ((useCurrentTimestamp || (series.lastUpdated <= 0))
                        ? ""
                        : (", " + series.lastUpdated)
                      )
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbEpisode(DbEpisode episode) {
    String query = "INSERT INTO episodes"
      + "   (id, serieId, seasonNumber, episodeNumber, name, overview, firstAired, imdbId, reviewRating, seen)"
      + " VALUES"
      + "   ("
      +               episode.id                                               + ", "
      +               episode.serieId                                          + ", "
      +               episode.seasonNumber                                     + ", "
      +               episode.episodeNumber                                    + ", "
      +               DatabaseUtils.sqlEscapeString(episode.name)              + ", "
      +               DatabaseUtils.sqlEscapeString(episode.overview)          + ", "
      +         "'" + episode.firstAired                                 + "'" + ", "
      +         "'" + episode.imdbId                                     + "'" + ", "
      +               episode.reviewRating                                     + ", "
      +               episode.seen
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbUnavailableEpisode(DbUnavailableEpisode episode) {
    String query = "INSERT INTO unavailableEpisodes"
      + "   (serieId, seasonNumber, episodeNumber)"
      + " VALUES"
      + "   ("
      +               episode.serieId                                          + ", "
      +               episode.seasonNumber                                     + ", "
      +               episode.episodeNumber
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbSeason(DbSeason season) {
    String query = "INSERT INTO seasons"
      + "   (serieId, seasonNumber, name, episodeCount)"
      + " VALUES"
      + "   ("
      +               season.serieId                                           + ", "
      +               season.seasonNumber                                      + ", "
      +               DatabaseUtils.sqlEscapeString(season.name)               + ", "
      +               season.episodeCount
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbGenre(DbGenre genre) {
    String query = "INSERT INTO genres"
      + "   (serieId, genre)"
      + " VALUES"
      + "   ("
      +               genre.serieId                                            + ", "
      +               DatabaseUtils.sqlEscapeString(genre.genre)
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbActor(DbActor actor) {
    String query = "INSERT INTO actors"
      + "   (serieId, actor)"
      + " VALUES"
      + "   ("
      +               actor.serieId                                            + ", "
      +               DatabaseUtils.sqlEscapeString(actor.actor)
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbWriter(DbWriter writer) {
    String query = "INSERT INTO writers"
      + "   (serieId, episodeId, writer)"
      + " VALUES"
      + "   ("
      +               writer.serieId                                            + ", "
      +               writer.episodeId                                          + ", "
      +               DatabaseUtils.sqlEscapeString(writer.writer)
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbDirector(DbDirector director) {
    String query = "INSERT INTO directors"
      + "   (serieId, episodeId, director)"
      + " VALUES"
      + "   ("
      +               director.serieId                                          + ", "
      +               director.episodeId                                        + ", "
      +               DatabaseUtils.sqlEscapeString(director.director)
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean saveDbGuestStar(DbGuestStar guestStar) {
    String query = "INSERT INTO guestStars"
      + "   (serieId, episodeId, guestStar)"
      + " VALUES"
      + "   ("
      +               guestStar.serieId                                         + ", "
      +               guestStar.episodeId                                       + ", "
      +               DatabaseUtils.sqlEscapeString(guestStar.guestStar)
      + "   )";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------
  // Write: Updates to table 'series'
  // ---------------------------------------------------------------------------

  public boolean updateDbSeries(DbSeries series) {
    boolean useCurrentTimestamp = true;
    return updateDbSeries(series, useCurrentTimestamp);
  }

  public boolean updateDbSeries(DbSeries series, boolean useCurrentTimestamp) {
    String query = "UPDATE series"
      + " SET"
      + "   name                 = "       + DatabaseUtils.sqlEscapeString(series.name)               + ", "
      + "   overview             = "       + DatabaseUtils.sqlEscapeString(series.overview)           + ", "
      + "   seasonCount          = "       + series.seasonCount                                       + ", "
      + "   status               = " + "'" + series.status                                      + "'" + ", "
      + "   firstAired           = " + "'" + series.firstAired                                  + "'" + ", "
      + "   imdbId               = " + "'" + series.imdbId                                      + "'" + ", "
      + "   reviewRating         = "       + series.reviewRating                                      + ", "
      + "   network              = " + "'" + series.network                                     + "'" + ", "
      + "   runtime              = " + "'" + series.runtime                                     + "'" + ", "
      + "   contentRating        = " + "'" + series.contentRating                               + "'" + ", "
      + "   language             = " + "'" + series.language                                    + "'" + ", "
      + "   largeImageUrl        = " + "'" + series.largeImageUrl                               + "'" + ", "
      + "   smallImageFilePath   = " + "'" + series.smallImageFilePath                          + "'" + ", "
      + "   mediumImageFilePath  = " + "'" + series.mediumImageFilePath                         + "'" + ", "
      + "   archived             = "       + series.archived                                          + ", "
      + "   pinned               = "       + series.pinned                                            + ", "
      + "   extResources         = "       + DatabaseUtils.sqlEscapeString(series.extResources)       + ", "
      + "   unwatched            = "       + series.unwatched                                         + ", "
      + "   unwatchedAired       = "       + series.unwatchedAired                                    + ", "
      + "   nextAir              = " + "'" + series.nextAir                                     + "'" + ", "
      + "   nextEpisode          = " + "'" + series.nextEpisode                                 + "'" + ", "
      + "   unwatchedLastAired   = " + "'" + series.unwatchedLastAired                          + "'" + ", "
      + "   unwatchedLastEpisode = " + "'" + series.unwatchedLastEpisode                        + "'"
      + ((useCurrentTimestamp || (series.lastUpdated <= 0))
          ? ""
          : (", " + "lastUpdated = " +       series.lastUpdated)
        )
      + " WHERE"
      + "   id = " + series.id;

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean updateSerieRemoveAllImageFilePaths() {
    String query = "UPDATE series"
      + " SET"
      + "   smallImageFilePath  = NULL,"
      + "   mediumImageFilePath = NULL";

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean updateSerieArchived(int serieId, boolean archived) {
    String query = "UPDATE series"
      + " SET"
      + "   archived = " + (archived ? 1 : 0)
      + " WHERE"
      + "   id = " + serieId;

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean updateSeriePinned(int serieId, boolean pinned) {
    String query = "UPDATE series"
      + " SET"
      + "   pinned = " + (pinned ? 1 : 0)
      + " WHERE"
      + "   id = " + serieId;

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean updateExtResources(int serieId, String extResources) {
    String query = "UPDATE series"
      + " SET"
      + "   extResources = " + DatabaseUtils.sqlEscapeString(extResources)
      + " WHERE"
      + "   id = " + serieId;

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------

  public boolean updateShowStats() {
    boolean result = true;

    List<Integer> serieIds = getSerieIds(2, false, null); // all series

    for (int serieId : serieIds) {
      result &= updateShowStats(serieId);
    }
    return result;
  }

  public boolean updateShowStats(int serieId) {
    int seasonCount    = getSeasonCount(serieId);
    int unwatched      = getEpsUnwatched(serieId);
    int unwatchedAired = getEpsUnwatchedAired(serieId);

    NextEpisode nextEpisode  = getNextEpisode(serieId);
    String nextEpisodeString = getNextEpisodeString(nextEpisode, /* boolean showNextAiring= */ (DroidShows.showNextAiring && (0 < unwatchedAired) && (unwatchedAired < unwatched)));

    UnwatchedLastAiredEpisode newestEpisode = getUnwatchedLastAiredEpisode(serieId);
    String newestEpisodeStr = getEpisodeString(newestEpisode, /* boolean showNextAiring= */ false, /* boolean requireAiredDate= */ true);

    String query = "UPDATE series"
      + " SET"
      + "   seasonCount="                + seasonCount                    + ","
      + "   unwatched="                  + unwatched                      + ","
      + "   unwatchedAired="             + unwatchedAired                 + ","
      + "   nextAir="              + "'" + nextEpisode.firstAired   + "'" + ","
      + "   nextEpisode="          + "'" + nextEpisodeString        + "'" + ","
      + "   unwatchedLastAired="   + "'" + newestEpisode.firstAired + "'" + ","
      + "   unwatchedLastEpisode=" + "'" + newestEpisodeStr         + "'"
      + " WHERE"
      + "   id = " + serieId;

    return db.execQuery(query);
  }

  public boolean updateShowStats(int serieId, int unwatched, int unwatchedAired, String nextEpisodeString) {
    String query = "UPDATE series"
      + " SET"
      + (!TextUtils.isEmpty(nextEpisodeString)
          ? (" nextEpisode="       + "'" + nextEpisodeString        + "'" + ",")
          : ""
        )
      + "   unwatched="                  + unwatched                      + ","
      + "   unwatchedAired="             + unwatchedAired
      + " WHERE"
      + "   id = " + serieId;

    return db.execQuery(query);
  }

  // ---------------------------------------------------------------------------
  // Write: Updates to table 'episodes'
  // ---------------------------------------------------------------------------

  public boolean updateUnwatchedSeason(int serieId, int seasonNumber) {
    boolean result = true;

    int seen = DateFormats.convertMsToSeconds(System.currentTimeMillis());

    String query = "UPDATE episodes"
      + " SET"
      + "   seen=" + seen
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seasonNumber = " + seasonNumber
      + " AND"
      + "   firstAired < " + "'" + today + "'"
      + " AND"
      + "   firstAired <> ''"
      + " AND"
      + "   seen < 1";

    result = db.execQuery(query);
    if (result) {
      updateShowStats(serieId);
    }
    return result;
  }

  // ---------------------------------------------------------------------------

  public boolean updateWatchedSeason(int serieId, int seasonNumber) {
    boolean result = true;

    String query = "UPDATE episodes"
      + " SET"
      + "   seen=0"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   seasonNumber = " + seasonNumber;

    result = db.execQuery(query);
    if (result) {
      updateShowStats(serieId);
    }
    return result;
  }

  // ---------------------------------------------------------------------------

  public String updateUnwatchedEpisode(int serieId, int episodeId) {
    int newSeen = -1;
    return updateUnwatchedEpisode(serieId, episodeId, newSeen);
  }

  // note: when 'newSeen' <= -1, the current value of 'seen' is toggled. (0 => timestamp || timestamp => 0)
  public String updateUnwatchedEpisode(int serieId, int episodeId, int newSeen) {
    String episodeMarked = "";

    String query;
    int seen, seasonNumber, episodeNumber;

    query = "SELECT seen, seasonNumber, episodeNumber"
      + " FROM"
      + "   episodes"
      + " WHERE"
      + "   serieId = " + serieId
      + " AND"
      + "   id = " + episodeId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        seen          = getColumnInteger(c, "seen");
        seasonNumber  = getColumnInteger(c, "seasonNumber");
        episodeNumber = getColumnInteger(c, "episodeNumber");
        episodeMarked = seasonNumber + ((episodeNumber < 10) ? "x0" : "x") + episodeNumber;

        if (newSeen <= -1) {
          newSeen = (seen > 0)
            ? 0
            : DateFormats.convertMsToSeconds(System.currentTimeMillis());
        }

        query = "UPDATE episodes"
          + " SET"
          + "   seen=" + newSeen
          + " WHERE"
          + "   serieId = " + serieId
          + " AND"
          + "   id = " + episodeId;

        if (db.execQuery(query)) {
          updateShowStats(serieId);
        }
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();
    return episodeMarked;
  }

  // ---------------------------------------------------------------------------
  // Delete:
  // ---------------------------------------------------------------------------

  public boolean deleteSerie(int serieId) {
    // ==================
    // delete image files
    // ==================

    String query = "SELECT smallImageFilePath, mediumImageFilePath FROM series WHERE id=" + serieId;

    Cursor c = null;
    try {
      c = db.query(query);

      if ((c != null) && c.moveToFirst() && c.isFirst()) {
        String smallImageFilePath  = getColumnString(c, "smallImageFilePath");
        String mediumImageFilePath = getColumnString(c, "mediumImageFilePath");

        FileUtils.deleteFile(smallImageFilePath);
        FileUtils.deleteFile(mediumImageFilePath);
      }
    }
    catch (SQLiteException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    catch (Exception e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
    if (c != null) c.close();

    // ==================
    // delete DB records
    // ==================

    List<String> queries = new ArrayList<String>();
    queries.add("DELETE FROM guestStars WHERE serieId = " + serieId);
    queries.add("DELETE FROM directors  WHERE serieId = " + serieId);
    queries.add("DELETE FROM writers    WHERE serieId = " + serieId);
    queries.add("DELETE FROM actors     WHERE serieId = " + serieId);
    queries.add("DELETE FROM genres     WHERE serieId = " + serieId);
    queries.add("DELETE FROM seasons    WHERE serieId = " + serieId);
    queries.add("DELETE FROM episodes   WHERE serieId = " + serieId);
    queries.add("DELETE FROM series     WHERE      id = " + serieId);

    return db.execTransaction(queries);
  }

  public boolean deleteSeasons(int serieId) {
    String query = "DELETE FROM seasons WHERE serieId = " + serieId;

    return db.execQuery(query);
  }

  public boolean deleteEpisodes(int serieId) {
    boolean refreshStats = true;
    return deleteEpisodes(serieId, refreshStats);
  }

  public boolean deleteEpisodes(int serieId, boolean refreshStats) {
    List<String> queries = new ArrayList<String>();
    queries.add("DELETE FROM guestStars WHERE serieId = " + serieId);
    queries.add("DELETE FROM directors  WHERE serieId = " + serieId);
    queries.add("DELETE FROM writers    WHERE serieId = " + serieId);
    queries.add("DELETE FROM episodes   WHERE serieId = " + serieId);

    boolean result = db.execTransaction(queries);
    if (result && refreshStats) {
      updateShowStats(serieId);
    }
    return result;
  }

  public boolean deleteEpisode(int serieId, int episodeId) {
    boolean refreshStats = true;
    return deleteEpisode(serieId, episodeId, refreshStats);
  }

  public boolean deleteEpisode(int serieId, int episodeId, boolean refreshStats) {
    List<String> queries = new ArrayList<String>();
    queries.add("DELETE FROM guestStars WHERE serieId = " + serieId + " AND episodeId = " + episodeId);
    queries.add("DELETE FROM directors  WHERE serieId = " + serieId + " AND episodeId = " + episodeId);
    queries.add("DELETE FROM writers    WHERE serieId = " + serieId + " AND episodeId = " + episodeId);
    queries.add("DELETE FROM episodes   WHERE serieId = " + serieId + " AND        id = " + episodeId);

    boolean result = db.execTransaction(queries);
    if (result && refreshStats) {
      updateShowStats(serieId);
    }
    return result;
  }

}
