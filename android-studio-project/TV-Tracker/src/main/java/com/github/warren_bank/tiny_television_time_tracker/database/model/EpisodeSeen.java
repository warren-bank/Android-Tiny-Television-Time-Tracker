package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class EpisodeSeen implements Comparable {
  public static int UNAVAILABLE_EPISODE_ID = -1;

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

  public boolean isUnavailable() {
    return (this.episodeId == EpisodeSeen.UNAVAILABLE_EPISODE_ID);
  }

  @Override
  public int compareTo(Object o) {
    EpisodeSeen that = (EpisodeSeen) o;

    if (this.seasonNumber < that.seasonNumber) return -1;
    if (this.seasonNumber > that.seasonNumber) return  1;

    // seasons are equal
    if (this.episodeNumber < that.episodeNumber) return -1;
    if (this.episodeNumber > that.episodeNumber) return  1;
    return 0;
  }
}
