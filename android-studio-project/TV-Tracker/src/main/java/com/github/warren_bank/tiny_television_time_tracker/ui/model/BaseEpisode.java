package com.github.warren_bank.tiny_television_time_tracker.ui.model;

import java.util.Date;

public class BaseEpisode {
  public int    serieId;
  public int    season;
  public int    episode;
  public String firstAired;
  public Date   firstAiredDate;

  public BaseEpisode(int serieId, int season, int episode, String firstAired, Date firstAiredDate) {
    this.serieId        = serieId;
    this.season         = season;
    this.episode        = episode;
    this.firstAired     = firstAired;
    this.firstAiredDate = firstAiredDate;
  }

  public boolean equals(BaseEpisode that) {
    return (
         (this.serieId == that.serieId)
      && (this.season  == that.season )
      && (this.episode == that.episode)
    );
  }
}
