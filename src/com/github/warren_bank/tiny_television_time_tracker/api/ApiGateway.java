package com.github.warren_bank.tiny_television_time_tracker.api;

import com.github.warren_bank.tiny_television_time_tracker.common.TextUtils;
import com.github.warren_bank.tiny_television_time_tracker.constants.Strings;
import com.github.warren_bank.tiny_television_time_tracker.database.DbGateway;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbActor;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbDirector;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGenre;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGuestStar;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeason;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeries;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbUnavailableEpisode;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbWriter;
import com.github.warren_bank.tiny_television_time_tracker.database.model.EpisodeSeen;

import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.enumeration.ExternalSource;
import com.omertron.themoviedbapi.model.FindResults;
import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.credits.MediaCreditCast;
import com.omertron.themoviedbapi.model.credits.MediaCreditCrew;
import com.omertron.themoviedbapi.model.network.Network;
import com.omertron.themoviedbapi.model.person.ContentRating;
import com.omertron.themoviedbapi.model.tv.TVBasic;
import com.omertron.themoviedbapi.model.tv.TVEpisodeInfo;
import com.omertron.themoviedbapi.model.tv.TVInfo;
import com.omertron.themoviedbapi.model.tv.TVSeasonBasic;
import com.omertron.themoviedbapi.model.tv.TVSeasonInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// -----------------------------------------------------------------------------
// API library references:
// -----------------------
// https://omertron.github.io/api-themoviedb/apidocs/index.html
// https://omertron.github.io/api-themoviedb/apidocs/com/omertron/themoviedbapi/TheMovieDbApi.html
// https://omertron.github.io/api-themoviedb/apidocs/com/omertron/themoviedbapi/model/tv/TVInfo.html
// https://omertron.github.io/api-themoviedb/apidocs/com/omertron/themoviedbapi/model/tv/TVSeasonInfo.html
// https://omertron.github.io/api-themoviedb/apidocs/com/omertron/themoviedbapi/model/tv/TVEpisodeInfo.html
// -----------------------------------------------------------------------------

public class ApiGateway {
  private static ApiGateway instance = null;

  private TheMovieDbApi api     = null;
  private DbGateway     db      = null;

  public static ApiGateway getInstance() throws Exception {
    if (instance == null) {
      instance = new ApiGateway();
    }
    return instance;
  }

  private ApiGateway() throws Exception {
    this.api = new TheMovieDbApi(Strings.api_key);
    this.db  = DbGateway.getInstance();
  }

  // --------------------------------------------------------------------------- Find:

  public int findSeriesByExternalId(Map<String,String> origins) {
    String language = null;
    return findSeriesByExternalId(origins, language);
  }

  public int findSeriesByExternalId(Map<String,String> origins, String language) {
    int internalId = -1;

    if (origins != null) {
      String origin, externalId;

      for(Map.Entry<String, String> entry : origins.entrySet()){
        origin     = entry.getKey();
        externalId = entry.getValue();
        internalId = findSeriesByExternalId(origin, externalId, language);

        if (internalId > 0) break;
      }
    }

    return internalId;
  }

  public int findSeriesByExternalId(String origin, String externalId, String language) {
    if (TextUtils.isEmpty(origin) || TextUtils.isEmpty(externalId)) return -1;

    switch(origin.toLowerCase()) {
      case "tvdb":
      case "thetvdb":
        return findSeriesByExternalId(ExternalSource.TVDB_ID, externalId, language);
      case "imdb":
        return findSeriesByExternalId(ExternalSource.IMDB_ID, externalId, language);
    }
    return -1;
  }

  public int findSeriesByExternalId(ExternalSource origin, String externalId, String language) {
    if (TextUtils.isEmpty(externalId)) return -1;

    try {
      FindResults response = api.find(externalId, origin, language);
      if (response == null)
        throw new Exception("no results");

      List<TVBasic> tvResults = response.getTvResults();
      if ((tvResults == null) || tvResults.isEmpty())
        throw new Exception("no results");

      for (TVBasic tv : tvResults) {
        int internalId = tv.getId();
        if (internalId > 0) {
          return internalId;
        }
      }
      throw new Exception("no results");
    }
    catch(Exception e) {
      System.out.println(e.getMessage());
      return -1;
    }
  }

  // --------------------------------------------------------------------------- Series:

  public boolean addSeries(int serieId, String language, boolean archived) {
    boolean ignoreMissingEpisodes = true;
    return addSeries(serieId, language, archived, ignoreMissingEpisodes);
  }

  public boolean addSeries(int serieId, String language, boolean archived, boolean ignoreMissingEpisodes) {
    TVInfo apiSeries = getApiSeries(serieId, language);
    if (apiSeries == null) return false;

    DbSeries       dbSeries  = getDbSeries (apiSeries);
    List<DbSeason> dbSeasons = getDbSeasons(apiSeries);
    List<DbGenre>  dbGenres  = getDbGenres (apiSeries);
    List<DbActor>  dbActors  = getDbActors (apiSeries);

    dbSeries.language = language;
    dbSeries.archived = archived ? 1 : 0;

    boolean result = true;
    if (result) {
      result &= db.saveDbSeries(dbSeries);
    }
    if (result) {
      for (DbSeason dbSeason : dbSeasons) {
        result &= db.saveDbSeason(dbSeason);
        if (!result) break;
      }
    }
    if (result) {
      for (DbGenre dbGenre : dbGenres) {
        result &= db.saveDbGenre(dbGenre);
        if (!result) break;
      }
    }
    if (result) {
      for (DbActor dbActor : dbActors) {
        result &= db.saveDbActor(dbActor);
        if (!result) break;
      }
    }
    if (result) {
      List<Integer> episodeNumbers;
      for (DbSeason dbSeason : dbSeasons) {
        episodeNumbers = getApiEpisodeNumbers(serieId, dbSeason.seasonNumber, language);
        if ((episodeNumbers == null) || episodeNumbers.isEmpty()) continue;

        for (int episodeNumber : episodeNumbers) {
          result &= addEpisode(dbSeason.serieId, dbSeason.seasonNumber, episodeNumber, language, ignoreMissingEpisodes);
          //if (!result) break;
        }
        //if (!result) break;
      }
    }
    return result;
  }

  // ---------------------------------------------------------------------------

  private TVInfo getApiSeries(int serieId, String language) {
    try {
      String[] appendToResponse = new String[]{"external_ids", "credits", "content_ratings"};
      TVInfo apiSeries          = api.getTVInfo(serieId, language, appendToResponse);
      return apiSeries;
    }
    catch(Exception e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  // --------------------------------------------------------------------------- Episode:

  private boolean addEpisode(int serieId, int seasonNumber, int episodeNumber, String language) {
    boolean ignoreMissingEpisodes = true;
    return addEpisode(serieId, seasonNumber, episodeNumber, language, ignoreMissingEpisodes);
  }

  private boolean addEpisode(int serieId, int seasonNumber, int episodeNumber, String language, boolean ignoreMissingEpisodes) {
    int seen = 0;
    return addEpisode(serieId, seasonNumber, episodeNumber, language, ignoreMissingEpisodes, seen);
  }

  private boolean addEpisode(int serieId, int seasonNumber, int episodeNumber, String language, boolean ignoreMissingEpisodes, int seen) {
    TVEpisodeInfo apiEpisode = getApiEpisode(serieId, seasonNumber, episodeNumber, language);
    if (apiEpisode == null) {
      DbUnavailableEpisode dbUnavailableEpisode = new DbUnavailableEpisode(serieId, seasonNumber, episodeNumber);
      db.saveDbUnavailableEpisode(dbUnavailableEpisode);
      return ignoreMissingEpisodes;
    }

    DbEpisode         dbEpisode    = getDbEpisode   (serieId, apiEpisode);
    List<DbWriter>    dbWriters    = getDbWriters   (serieId, apiEpisode);
    List<DbDirector>  dbDirectors  = getDbDirectors (serieId, apiEpisode);
    List<DbGuestStar> dbGuestStars = getDbGuestStars(serieId, apiEpisode);

    dbEpisode.seen = seen;

    boolean result = true;
    if (result) {
      result &= db.saveDbEpisode(dbEpisode);
    }
    if (result) {
      for (DbWriter dbWriter : dbWriters) {
        result &= db.saveDbWriter(dbWriter);
        if (!result) break;
      }
    }
    if (result) {
      for (DbDirector dbDirector : dbDirectors) {
        result &= db.saveDbDirector(dbDirector);
        if (!result) break;
      }
    }
    if (result) {
      for (DbGuestStar dbGuestStar : dbGuestStars) {
        result &= db.saveDbGuestStar(dbGuestStar);
        if (!result) break;
      }
    }
    return result;
  }

  // ---------------------------------------------------------------------------

  private List<Integer> getApiEpisodeNumbers(int serieId, int seasonNumber, String language) {
    List<EpisodeSeen> episodes = getApiEpisodesInSeason(serieId, seasonNumber, language);

    if ((episodes == null) || episodes.isEmpty())
      return null;

    List<Integer> episodeNumbers = new ArrayList<Integer>();
    for (EpisodeSeen episode : episodes) {
      episodeNumbers.add(
        episode.episodeNumber
      );
    }
    return episodeNumbers;
  }

  private List<EpisodeSeen> getApiEpisodesInSeason(int serieId, int seasonNumber, String language) {
    try {
      String[] appendToResponse = new String[0];
      TVSeasonInfo apiSeason = api.getSeasonInfo(serieId, seasonNumber, language, appendToResponse);
      if (apiSeason == null)
        throw new Exception("no results");

      List<TVEpisodeInfo> allEpisodes = apiSeason.getEpisodes();
      if ((allEpisodes == null) || allEpisodes.isEmpty())
        throw new Exception("no results");

      List<EpisodeSeen> episodes = new ArrayList<EpisodeSeen>();
      for (TVEpisodeInfo apiEpisode : allEpisodes) {
        episodes.add(
          new EpisodeSeen(
            /* episodeId=     */ apiEpisode.getId(),
            seasonNumber,
            /* episodeNumber= */ apiEpisode.getEpisodeNumber(),
            /* seen=          */ 0
          )
        );
      }
      return episodes;
    }
    catch(Exception e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  // ---------------------------------------------------------------------------

  private TVEpisodeInfo getApiEpisode(int serieId, int seasonNumber, int episodeNumber, String language) {
    try {
      String[] appendToResponse = new String[]{"external_ids"};
      TVEpisodeInfo apiEpisode  = api.getEpisodeInfo(serieId, seasonNumber, episodeNumber, language, appendToResponse);
      return apiEpisode;
    }
    catch(Exception e) {
      System.out.println(e.getMessage());
      return null;
    }
  }

  // --------------------------------------------------------------------------- Conversion to DB Models:

  private DbSeries getDbSeries(TVInfo apiSeries) {
    return new DbSeries(
      /* int id                      */ apiSeries.getId(),
      /* String name                 */ apiSeries.getName(),
      /* String overview             */ apiSeries.getOverview(),
      /* int seasonCount             */ apiSeries.getNumberOfSeasons(),
      /* String status               */ getDbSeries_status(apiSeries),
      /* String firstAired           */ apiSeries.getFirstAirDate(),
      /* String imdbId               */ apiSeries.getExternalIDs().getImdbId(),
      /* float reviewRating          */ apiSeries.getRating(),
      /* String network              */ getDbSeries_network(apiSeries),
      /* String runtime              */ getDbSeries_runtime(apiSeries),
      /* String contentRating        */ getDbSeries_contentRating(apiSeries),
      /* String language             */ apiSeries.getOriginalLanguage(),
      /* String largeImageUrl        */ getDbSeries_largeImageUrl(apiSeries),
      /* String smallImageFilePath   */ "",
      /* String mediumImageFilePath  */ "",
      /* int archived                */ 0,
      /* int pinned                  */ 0,
      /* String extResources         */ "",
      /* int unwatched               */ 0,
      /* int unwatchedAired          */ 0,
      /* String nextAir              */ "",
      /* String nextEpisode          */ "",
      /* String unwatchedLastAired   */ "",
      /* String unwatchedLastEpisode */ "",
      /* int lastUpdated             */ 0
    );
  }

  private String getDbSeries_status(TVInfo apiSeries) {
    String status = apiSeries.getStatus();

    switch (status.toLowerCase()) {
      case "continuing":
      case "in production":
      case "returning series":
        status = Strings.showstatus_continuing;
        break;

      case "cancelled":
      case "ended":
        status = Strings.showstatus_ended;
        break;
    }

    return status;
  }

  private String getDbSeries_network(TVInfo apiSeries) {
    List<Network> networks = apiSeries.getNetworks();
    return networks.isEmpty() ? "" : networks.get(0).getName();
  }

  private String getDbSeries_runtime(TVInfo apiSeries) {
    List<Integer> runtimes = apiSeries.getEpisodeRunTime();
    return runtimes.isEmpty() ? "" : runtimes.get(0).toString();
  }

  private String getDbSeries_contentRating(TVInfo apiSeries) {
    List<ContentRating> ratings = apiSeries.getContentRatings();
    if ((ratings == null) || ratings.isEmpty()) return "";

    // search for preferred content-ratings system
    String preferred_country = "US";
    for (ContentRating rating : ratings) {
      if (preferred_country.equals(rating.getCountry())) {
        return rating.getRating();
      }
    }

    // default to first in list
    return ratings.get(0).getRating();
  }

  private String getDbSeries_largeImageUrl(TVInfo apiSeries) {
    String path = apiSeries.getPosterPath();
    return TextUtils.isEmpty(path)
      ? ""
      : ("http://image.tmdb.org/t/p/original/" + path);
  }

  // ---------------------------------------------------------------------------

  private List<DbSeason> getDbSeasons(TVInfo apiSeries) {
    List<DbSeason> dbSeasons = new ArrayList<DbSeason>();

    List<TVSeasonBasic> apiSeasons = apiSeries.getSeasons();
    if ((apiSeasons != null) && !apiSeasons.isEmpty()) {
      for (TVSeasonBasic apiSeason : apiSeasons) {
        DbSeason dbSeason = new DbSeason(
          /* int serieId      */ apiSeries.getId(),
          /* int seasonNumber */ apiSeason.getSeasonNumber(),
          /* String name      */ getDbSeason_name(apiSeason),
          /* int episodeCount */ apiSeason.getEpisodeCount()
        );
        dbSeasons.add(dbSeason);
      }
    }

    return dbSeasons;
  }

  private String getDbSeason_name(TVSeasonBasic apiSeason) {
    int seasonNumber = apiSeason.getSeasonNumber();
    return (seasonNumber < 1)
      ?  Strings.messages_specials
      : (Strings.messages_season + " " + seasonNumber);
  }

  // ---------------------------------------------------------------------------

  private List<DbGenre> getDbGenres(TVInfo apiSeries) {
    List<DbGenre> dbGenres = new ArrayList<DbGenre>();

    List<Genre> apiGenres = apiSeries.getGenres();
    if ((apiGenres != null) && !apiGenres.isEmpty()) {
      for (Genre apiGenre : apiGenres) {
        DbGenre dbGenre = new DbGenre(
          /* int serieId  */ apiSeries.getId(),
          /* String genre */ apiGenre.getName()
        );
        dbGenres.add(dbGenre);
      }
    }

    return dbGenres;
  }

  // ---------------------------------------------------------------------------

  private List<DbActor> getDbActors(TVInfo apiSeries) {
    List<DbActor> dbActors = new ArrayList<DbActor>();

    List<MediaCreditCast> apiCast = apiSeries.getCredits().getCast();
    if ((apiCast != null) && !apiCast.isEmpty()) {
      for (MediaCreditCast apiCastMember : apiCast) {
        DbActor dbActor = new DbActor(
          /* int serieId  */ apiSeries.getId(),
          /* String actor */ apiCastMember.getName()
        );
        dbActors.add(dbActor);
      }
    }

    return dbActors;
  }

  // ---------------------------------------------------------------------------

  private DbEpisode getDbEpisode(int serieId, TVEpisodeInfo apiEpisode) {
    return new DbEpisode(
      /* int id             */ apiEpisode.getId(),
      /* int serieId        */ serieId,
      /* int seasonNumber   */ apiEpisode.getSeasonNumber(),
      /* int episodeNumber  */ apiEpisode.getEpisodeNumber(),
      /* String name        */ apiEpisode.getName(),
      /* String overview    */ apiEpisode.getOverview(),
      /* String firstAired  */ apiEpisode.getAirDate(),
      /* String imdbId      */ apiEpisode.getExternalIDs().getImdbId(),
      /* float reviewRating */ apiEpisode.getVoteAverage(),
      /* int seen           */ 0
    );
  }

  // ---------------------------------------------------------------------------

  private List<DbWriter> getDbWriters(int serieId, TVEpisodeInfo apiEpisode) {
    List<DbWriter> dbWriters = new ArrayList<DbWriter>();

    String job_title = "Writer";

    List<MediaCreditCrew> apiCrew = apiEpisode.getCrew();
    if ((apiCrew != null) && !apiCrew.isEmpty()) {
      for (MediaCreditCrew apiCrewMember : apiCrew) {
        if (job_title.equals(apiCrewMember.getJob())) {
          DbWriter dbWriter = new DbWriter(
            /* int serieId   */ serieId,
            /* int episodeId */ apiEpisode.getId(),
            /* String writer */ apiCrewMember.getName()
          );
          dbWriters.add(dbWriter);
        }
      }
    }

    return dbWriters;
  }

  // ---------------------------------------------------------------------------

  private List<DbDirector> getDbDirectors(int serieId, TVEpisodeInfo apiEpisode) {
    List<DbDirector> dbDirectors = new ArrayList<DbDirector>();

    String job_title = "Director";

    List<MediaCreditCrew> apiCrew = apiEpisode.getCrew();
    if ((apiCrew != null) && !apiCrew.isEmpty()) {
      for (MediaCreditCrew apiCrewMember : apiCrew) {
        if (job_title.equals(apiCrewMember.getJob())) {
          DbDirector dbDirector = new DbDirector(
            /* int serieId     */ serieId,
            /* int episodeId   */ apiEpisode.getId(),
            /* String director */ apiCrewMember.getName()
          );
          dbDirectors.add(dbDirector);
        }
      }
    }

    return dbDirectors;
  }

  // ---------------------------------------------------------------------------

  private List<DbGuestStar> getDbGuestStars(int serieId, TVEpisodeInfo apiEpisode) {
    List<DbGuestStar> dbGuestStars = new ArrayList<DbGuestStar>();

    List<MediaCreditCast> apiCast = apiEpisode.getGuestStars();
    if ((apiCast != null) && !apiCast.isEmpty()) {
      for (MediaCreditCast apiCastMember : apiCast) {
        DbGuestStar dbGuestStar = new DbGuestStar(
          /* int serieId      */ serieId,
          /* int episodeId    */ apiEpisode.getId(),
          /* String guestStar */ apiCastMember.getName()
        );
        dbGuestStars.add(dbGuestStar);
      }
    }

    return dbGuestStars;
  }

  // ---------------------------------------------------------------------------

}
