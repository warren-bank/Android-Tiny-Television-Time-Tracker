package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbUnavailableEpisode extends MapToSeries {
  public int seasonNumber;
  public int episodeNumber;

  public DbUnavailableEpisode(int serieId, int seasonNumber, int episodeNumber) {
    super(serieId);
    this.seasonNumber  = seasonNumber;
    this.episodeNumber = episodeNumber;
  }
}
