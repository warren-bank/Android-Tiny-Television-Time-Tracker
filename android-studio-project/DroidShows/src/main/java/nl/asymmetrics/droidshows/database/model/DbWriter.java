package nl.asymmetrics.droidshows.database.model;

public class DbWriter extends MapToEpisode {
  public String writer;

  public DbWriter(int serieId, int episodeId, String writer) {
    super(serieId, episodeId);
    this.writer = writer;
  }
}
