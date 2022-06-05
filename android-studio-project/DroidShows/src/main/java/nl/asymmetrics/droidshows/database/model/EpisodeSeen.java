package nl.asymmetrics.droidshows.database.model;

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
