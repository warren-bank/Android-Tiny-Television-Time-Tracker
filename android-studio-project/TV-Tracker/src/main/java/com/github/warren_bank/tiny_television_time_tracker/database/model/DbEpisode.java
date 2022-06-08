package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbEpisode extends MapToSeries {
  public int    id;
  public int    seasonNumber;
  public int    episodeNumber;
  public String name;
  public String overview;
  public String firstAired;
  public String imdbId;
  public float  reviewRating;
  public int    seen;

  public DbEpisode(int serieId) {
    super(serieId);
  }

  public DbEpisode(int id, int serieId) {
    super(serieId);
    this.id = id;
  }

  public DbEpisode(int id, int serieId, int seasonNumber, int episodeNumber, String name, String overview, String firstAired, String imdbId, float reviewRating, int seen) {
    super(serieId);
    this.id            = id;
    this.seasonNumber  = seasonNumber;
    this.episodeNumber = episodeNumber;
    this.name          = name;
    this.overview      = overview;
    this.firstAired    = firstAired;
    this.imdbId        = imdbId;
    this.reviewRating  = reviewRating;
    this.seen          = seen;
  }
}
