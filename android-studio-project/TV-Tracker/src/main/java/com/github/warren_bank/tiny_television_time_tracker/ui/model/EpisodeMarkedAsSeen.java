package com.github.warren_bank.tiny_television_time_tracker.ui.model;

public class EpisodeMarkedAsSeen {
  public int    serieId;
  public int    episodeId;
  public String serieName;

  public EpisodeMarkedAsSeen(int serieId, int episodeId, String serieName) {
    this.serieId   = serieId;
    this.episodeId = episodeId;
    this.serieName = serieName;
  }
}
