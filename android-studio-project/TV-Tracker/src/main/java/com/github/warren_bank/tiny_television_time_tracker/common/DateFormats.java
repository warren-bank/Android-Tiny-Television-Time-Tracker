package com.github.warren_bank.tiny_television_time_tracker.common;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Calendar;
import java.util.Date;

public class DateFormats {
  public static final FastDateFormat NORMALIZE_DATE      = FastDateFormat.getInstance("yyyy-MM-dd");                                          // new SimpleDateFormat("yyyy-MM-dd")
  public static final FastDateFormat NORMALIZE_DATE_TIME = FastDateFormat.getInstance("yyyy-MM-dd-HH-mm-ss");                                 // new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
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

  public static String getNormalizedDate() {
    Date date = Calendar.getInstance().getTime();
    return DateFormats.getNormalizedDate(date);
  }

  public static String getNormalizedDate(Date date) {
    return DateFormats.NORMALIZE_DATE.format(date);
  }

  public static String getNormalizedDateTime() {
    Date date = Calendar.getInstance().getTime();
    return DateFormats.getNormalizedDateTime(date);
  }

  public static String getNormalizedDateTime(Date date) {
    return DateFormats.NORMALIZE_DATE_TIME.format(date);
  }
}
