package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class DbGuestStar extends MapToEpisode {
  public String guestStar;

  public DbGuestStar(int serieId, int episodeId, String guestStar) {
    super(serieId, episodeId);
    this.guestStar = guestStar;
  }
}
