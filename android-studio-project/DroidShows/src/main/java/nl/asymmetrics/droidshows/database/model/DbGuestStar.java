package nl.asymmetrics.droidshows.database.model;

public class DbGuestStar extends MapToEpisode {
  public String guestStar;

  public DbGuestStar(int serieId, int episodeId, String guestStar) {
    super(serieId, episodeId);
    this.guestStar = guestStar;
  }
}
