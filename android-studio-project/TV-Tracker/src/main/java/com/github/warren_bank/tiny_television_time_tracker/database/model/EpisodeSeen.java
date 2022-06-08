package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class EpisodeSeen {
  public int episodeId;
  public int seasonNumber;
  public int episodeNumber;
  public int seen;

  public EpisodeSeen(int episodeId, int seasonNumber, int episodeNumber, int seen) {
    this.episodeId     = episodeId;
    this.seasonNumber  = seasonNumber;
    this.episodeNumber = episodeNumber;
    this.seen          = seen;
  }
}
