package nl.asymmetrics.droidshows.database.model;

public class DbActor extends MapToSeries {
  public String actor;

  public DbActor(int serieId, String actor) {
    super(serieId);
    this.actor = actor;
  }
}
