package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbActor extends MapToSeries {
  public String actor;

  public DbActor(int serieId, String actor) {
    super(serieId);
    this.actor = actor;
  }
}
