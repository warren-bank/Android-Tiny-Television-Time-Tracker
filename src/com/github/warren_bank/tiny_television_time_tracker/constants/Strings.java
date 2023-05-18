package com.github.warren_bank.tiny_television_time_tracker.constants;

public class Strings {
  public static final String api_key   = "c9eb196aaf70baf91d4ce4f0fea6a360";
  public static final String lang_code = "en";

  public static final String showstatus_continuing = "continuing";
  public static final String showstatus_ended      = "ended";
  public static final String messages_specials     = "Specials";
  public static final String messages_season       = "Season";

  public static final String db_migration_log_file_heading_tvdb_series_ids_that_failed_to_resolve = "The following is a list of TVDB ID values\nalong with the name of each corresponding series\nthat were present in the database prior to upgrade,\nbut could not be resolved to a corresponding TMDB ID value:";
  public static final String db_migration_log_file_heading_tmdb_series_ids_that_failed_to_add     = "The following is a list of TMDB ID values\nalong with the name of each corresponding series\nthat were present in the database prior to upgrade,\nbut could not be added using the TMDB API:";
}
