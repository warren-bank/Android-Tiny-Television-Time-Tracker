package nl.asymmetrics.droidshows.ui.model;

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
