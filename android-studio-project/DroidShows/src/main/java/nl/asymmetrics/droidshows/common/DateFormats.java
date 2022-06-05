package nl.asymmetrics.droidshows.common;

import org.apache.commons.lang3.time.FastDateFormat;

public class DateFormats {
  public static final FastDateFormat NORMALIZE_DATE      = FastDateFormat.getInstance("yyyy-MM-dd");                                          // new SimpleDateFormat("yyyy-MM-dd")
  public static final FastDateFormat DISPLAY_DATE        = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM);                             // SimpleDateFormat.getDateInstance()
  public static final FastDateFormat DISPLAY_DATE_TIME   = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM);  // SimpleDateFormat.getDateTimeInstance()
  public static final FastDateFormat DISPLAY_FILE_PICKER = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.SHORT);   // SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT)

  public static long convertSecondsToMs(int seconds) {
    long ms = ((long) seconds) * 1000l;
    return ms;
  }

  public static int convertMsToSeconds(long ms) {
    int seconds = (int) (ms / 1000l);
    return seconds;
  }
}
