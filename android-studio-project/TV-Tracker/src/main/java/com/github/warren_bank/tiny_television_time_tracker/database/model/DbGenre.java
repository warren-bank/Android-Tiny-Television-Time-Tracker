package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbGenre extends MapToSeries {
  public String genre;

  public DbGenre(int serieId, String genre) {
    super(serieId);
    this.genre = genre;
  }
}
