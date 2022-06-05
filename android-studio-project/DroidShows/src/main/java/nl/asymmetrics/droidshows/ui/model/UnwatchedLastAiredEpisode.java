package nl.asymmetrics.droidshows.ui.model;

import java.util.Date;

public class UnwatchedLastAiredEpisode extends BaseEpisode {
  public UnwatchedLastAiredEpisode(int serieId, int season, int episode, String firstAired, Date firstAiredDate) {
    super(serieId, season, episode, firstAired, firstAiredDate);
  }
}
