package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbDirector extends MapToEpisode {
  public String director;

  public DbDirector(int serieId, int episodeId, String director) {
    super(serieId, episodeId);
    this.director = director;
  }
}
