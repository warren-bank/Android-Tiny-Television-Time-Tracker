package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbWriter extends MapToEpisode {
  public String writer;

  public DbWriter(int serieId, int episodeId, String writer) {
    super(serieId, episodeId);
    this.writer = writer;
  }
}
