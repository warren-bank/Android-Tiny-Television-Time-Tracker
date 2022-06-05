package nl.asymmetrics.droidshows.database.model;

public class DbSeries {
  public int    id;
  public String name;
  public String overview;
  public int    seasonCount;
  public String status;
  public String firstAired;
  public String imdbId;
  public float  reviewRating;
  public String network;
  public String runtime;
  public String contentRating;
  public String language;
  public String largeImageUrl;
  public String smallImageFilePath;
  public String mediumImageFilePath;
  public int    archived;
  public int    pinned;
  public String extResources;
  public int    unwatched;
  public int    unwatchedAired;
  public String nextAir;
  public String nextEpisode;
  public String unwatchedLastAired;
  public String unwatchedLastEpisode;
  public int    lastUpdated;

  public DbSeries() {}

  public DbSeries(int id) {
    this.id = id;
  }

  public DbSeries(int id, String name, String overview, int seasonCount, String status, String firstAired, String imdbId, float reviewRating, String network, String runtime, String contentRating, String language, String largeImageUrl, String smallImageFilePath, String mediumImageFilePath, int archived, int pinned, String extResources, int unwatched, int unwatchedAired, String nextAir, String nextEpisode, String unwatchedLastAired, String unwatchedLastEpisode, int lastUpdated) {
    this.id                   = id;
    this.name                 = name;
    this.overview             = overview;
    this.seasonCount          = seasonCount;
    this.status               = status;
    this.firstAired           = firstAired;
    this.imdbId               = imdbId;
    this.reviewRating         = reviewRating;
    this.network              = network;
    this.runtime              = runtime;
    this.contentRating        = contentRating;
    this.language             = language;
    this.largeImageUrl        = largeImageUrl;
    this.smallImageFilePath   = smallImageFilePath;
    this.mediumImageFilePath  = mediumImageFilePath;
    this.archived             = archived;
    this.pinned               = pinned;
    this.extResources         = extResources;
    this.unwatched            = unwatched;
    this.unwatchedAired       = unwatchedAired;
    this.nextAir              = nextAir;
    this.nextEpisode          = nextEpisode;
    this.unwatchedLastAired   = unwatchedLastAired;
    this.unwatchedLastEpisode = unwatchedLastEpisode;
    this.lastUpdated          = lastUpdated;
  }
}
