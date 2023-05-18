package com.github.warren_bank.tiny_television_time_tracker.database;

import com.github.warren_bank.tiny_television_time_tracker.database.model.DbActor;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbDirector;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGenre;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGuestStar;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeason;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeries;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbUnavailableEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbWriter;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;

public class DbGateway {
  private static DbGateway instance = null;

  private SQLiteStore db;

  public static DbGateway getInstance() throws Exception {
    if (instance == null) {
      instance = new DbGateway();
    }
    return instance;
  }

  private DbGateway() throws Exception {
    this.db = SQLiteStore.getInstance();
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

}
