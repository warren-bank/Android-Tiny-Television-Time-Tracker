package nl.asymmetrics.droidshows.database.model;

public class DbSeason extends MapToSeries {
  public int    seasonNumber;
  public String name;
  public int    episodeCount;

  public DbSeason(int serieId, int seasonNumber, String name, int episodeCount) {
    super(serieId);
    this.seasonNumber = seasonNumber;
    this.name         = name;
    this.episodeCount = episodeCount;
  }
}
