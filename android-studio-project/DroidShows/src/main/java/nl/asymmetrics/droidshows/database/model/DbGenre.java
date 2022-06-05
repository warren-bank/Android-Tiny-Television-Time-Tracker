package nl.asymmetrics.droidshows.database.model;

public class DbGenre extends MapToSeries {
  public String genre;

  public DbGenre(int serieId, String genre) {
    super(serieId);
    this.genre = genre;
  }
}
