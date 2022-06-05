package nl.asymmetrics.droidshows.ui.model;

import nl.asymmetrics.droidshows.database.model.DbSeason;

import java.util.Date;

public class SeasonRow {
  public int    serieId;
  public int    seasonNumber;
  public String name;
  public int    episodeCount;
  public int    unwatched;
  public int    unwatchedAired;
  public Date   nextAir;
  public String nextEpisode;

  public SeasonRow(int serieId, int seasonNumber, String name, int episodeCount, int unwatched, int unwatchedAired, Date nextAir, String nextEpisode) {
    this.serieId        = serieId;
    this.seasonNumber   = seasonNumber;
    this.name           = name;
    this.episodeCount   = episodeCount;
    this.unwatched      = unwatched;
    this.unwatchedAired = unwatchedAired;
    this.nextAir        = nextAir;
    this.nextEpisode    = nextEpisode;
  }

  public SeasonRow(DbSeason dbSeason) {
    this.serieId        = dbSeason.serieId;
    this.seasonNumber   = dbSeason.seasonNumber;
    this.name           = dbSeason.name;
    this.episodeCount   = dbSeason.episodeCount;
    this.unwatched      = -1;
    this.unwatchedAired = -1;
    this.nextAir        = null;
    this.nextEpisode    = null;
  }
}
