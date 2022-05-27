package demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.tv.TVInfo;
import com.omertron.themoviedbapi.model.tv.TVEpisodeInfo;

public class App {
  public static void printSeries(TheMovieDbApi api, ObjectMapper om) throws Exception {
    int tvID                  = 4614;
    String language           = "en-US";
    String[] appendToResponse = new String[]{"external_ids", "credits", "content_ratings"};
    TVInfo series             = api.getTVInfo(tvID, language, appendToResponse);

    System.out.println("TV series:");
    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(series));
  }

  public static void printEpisode(TheMovieDbApi api, ObjectMapper om) throws Exception {
    int tvID                  = 4614;
    int seasonNumber          = 1;
    int episodeNumber         = 1;
    String language           = "en-US";
    String[] appendToResponse = new String[]{"external_ids"};
    TVEpisodeInfo episode     = api.getEpisodeInfo(tvID, seasonNumber, episodeNumber, language, appendToResponse);

    System.out.println("TV episode:");
    System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(episode));
  }

  public static void main(String[] args) {
    try {
      String apiKey     = "1553d2e4fa2912fc0953305d4d3e7c44";
      TheMovieDbApi api = new TheMovieDbApi(apiKey);

      ObjectMapper   om = new ObjectMapper();
      String divider    = new String(new char[70]).replace("\0", "-");

      System.out.println(divider);
      printSeries(api, om);
      System.out.println(divider);
      printEpisode(api, om);
      System.out.println(divider);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
}
