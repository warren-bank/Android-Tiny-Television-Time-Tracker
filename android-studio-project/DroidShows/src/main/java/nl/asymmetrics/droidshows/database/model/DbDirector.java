package nl.asymmetrics.droidshows.database.model;

public class DbDirector extends MapToEpisode {
  public String director;

  public DbDirector(int serieId, int episodeId, String director) {
    super(serieId, episodeId);
    this.director = director;
  }
}
