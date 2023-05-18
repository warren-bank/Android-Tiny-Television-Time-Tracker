package com.github.warren_bank.tiny_television_time_tracker;

import com.github.warren_bank.tiny_television_time_tracker.database.Update;
import com.github.warren_bank.tiny_television_time_tracker.database.Update.DatabaseUpdateResult;
import com.github.warren_bank.tiny_television_time_tracker.utils.FileUtils;

import java.io.File;

public class DroidShowsDatabaseMigrationTool {
  public static File db_droidshows;
  public static File db_tv_tracker;
  public static File log_errors;

  public static void main(String[] args) {
    if (args.length < 2) {
      die("Usage: java -jar DroidShowsDatabaseMigrationTool.jar '/path/to/input/DroidShows.db' '/path/to/output/TV-Tracker.db'", 1);
    }

    try {
      db_droidshows = new File(args[0]);
      db_tv_tracker = new File(args[1]);
      log_errors    = new File(args[1] + ".log");

      if (!db_droidshows.exists()) {
        die("Input database does not exist: '" + args[0] + "'", 1);
      }

      if (db_tv_tracker.exists()) {
        die("Output database already exists: '" + args[1] + "'", 1);
      }

      if (log_errors.exists()) {
        log_errors.delete();
      }

      FileUtils.copyFile(db_droidshows, db_tv_tracker);

      process();
    }
    catch(Exception e) {
      die("Error: " + e.getMessage(), 1);
    }
  }

  protected static void die(String msg, int exitcode) {
    if (msg != null)
      System.out.println(msg);

    System.exit(exitcode);
  }

  private static void process() throws Exception {
    Update updateDS = new Update();
    Update.DatabaseUpdateListener listener = new MyDatabaseUpdateListener();
    updateDS.updateDatabase(listener);
  }

  private static class MyDatabaseUpdateListener implements Update.DatabaseUpdateListener {
    public MyDatabaseUpdateListener() {}

    public boolean preDatabaseUpdate(int oldVersion, boolean willUpdate) {
      System.out.println("DroidShows database version: " + oldVersion);
      System.out.println("TV-Tracker database version: " + Update.VERSION_TMDB_MIGRATION);

      if (willUpdate)
        System.out.println("Database migration from DroidShows to TV-Tracker is running...");
      else
        die("Database migration from DroidShows to TV-Tracker is not required.", 0);

      return willUpdate;
    }

    public void postDatabaseUpdate(int oldVersion, DatabaseUpdateResult result) {
      Update.handleDatabaseUpdateResultErrors(oldVersion, result);
      die("Database migration from DroidShows to TV-Tracker is complete.", 0);
    }
  }
}
