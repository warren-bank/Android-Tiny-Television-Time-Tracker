package nl.asymmetrics.droidshows.database.model;

public class MapToEpisode extends MapToSeries {
  public int episodeId;

  protected MapToEpisode(int serieId, int episodeId) {
    super(serieId);
    this.episodeId = episodeId;
  }
}
