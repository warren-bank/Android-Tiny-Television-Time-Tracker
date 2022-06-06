package nl.asymmetrics.droidshows.utils;

import android.net.Uri;

public class UrlUtils {

  public static String encodeURIComponent(String str) {
    return Uri.encode(str);
  }

  public static String decodeURIComponent(String str) {
    return Uri.decode(str);
  }

  public static String encodeSerieNameForQuerystringValue(String str) {
    boolean forQuery = true;
    return UrlUtils.encodeSerieName(str, forQuery);
  }

  public static String encodeSerieNameForIntentExtra(String str) {
    boolean forQuery = false;
    return UrlUtils.encodeSerieName(str, forQuery);
  }

  private static String encodeSerieName(String str, boolean forQuery) {
    if (str == null) return "";

    // strip off a descriptive year of release, if present: /\(\d{4}\)/
    str = str.replaceAll("\\(\\d{4}\\)", "").trim().replaceAll("\\s{2,}", " ");

    if (forQuery) {
      str = UrlUtils.encodeURIComponent(str);
    }

    return str;
  }

}
