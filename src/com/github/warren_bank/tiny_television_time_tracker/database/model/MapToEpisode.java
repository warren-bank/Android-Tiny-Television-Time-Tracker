package com.github.warren_bank.tiny_television_time_tracker.database.model;

public class MapToEpisode extends MapToSeries {
  public int episodeId;

  protected MapToEpisode(int serieId, int episodeId) {
    super(serieId);
    this.episodeId = episodeId;
  }
}
