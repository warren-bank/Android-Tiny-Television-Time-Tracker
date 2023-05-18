package com.github.warren_bank.tiny_television_time_tracker.common;

public class DateFormats {
  public static int convertMsToSeconds(long ms) {
    int seconds = (int) (ms / 1000l);
    return seconds;
  }
}
