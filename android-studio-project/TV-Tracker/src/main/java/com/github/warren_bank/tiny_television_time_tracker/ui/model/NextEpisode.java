package com.github.warren_bank.tiny_television_time_tracker.ui.model;

import java.util.Date;

public class NextEpisode extends BaseEpisode {
  public NextEpisode(int serieId, int season, int episode, String firstAired, Date firstAiredDate) {
    super(serieId, season, episode, firstAired, firstAiredDate);
  }
}
