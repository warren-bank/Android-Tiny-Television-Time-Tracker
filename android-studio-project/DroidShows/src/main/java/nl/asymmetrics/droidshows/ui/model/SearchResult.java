package nl.asymmetrics.droidshows.ui.model;

public class SearchResult {
  public int    serieId;
  public String name;
  public String language;
  public String overview;

  public SearchResult(int serieId, String name, String language, String overview) {
    this.serieId  = serieId;
    this.name     = name;
    this.language = language;
    this.overview = overview;
  }
}
