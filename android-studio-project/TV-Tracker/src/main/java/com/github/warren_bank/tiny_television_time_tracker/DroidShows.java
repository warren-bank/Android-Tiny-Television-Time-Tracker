package com.github.warren_bank.tiny_television_time_tracker;

import com.github.warren_bank.tiny_television_time_tracker.R;
import com.github.warren_bank.tiny_television_time_tracker.api.ApiGateway;
import com.github.warren_bank.tiny_television_time_tracker.common.Constants;
import com.github.warren_bank.tiny_television_time_tracker.common.DateFormats;
import com.github.warren_bank.tiny_television_time_tracker.database.DbGateway;
import com.github.warren_bank.tiny_television_time_tracker.database.Update;
import com.github.warren_bank.tiny_television_time_tracker.ui.AddSerie;
import com.github.warren_bank.tiny_television_time_tracker.ui.BounceListView;
import com.github.warren_bank.tiny_television_time_tracker.ui.SerieSeasons;
import com.github.warren_bank.tiny_television_time_tracker.ui.ViewEpisode;
import com.github.warren_bank.tiny_television_time_tracker.ui.ViewSerie;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.EpisodeMarkedAsSeen;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.NextEpisode;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.TVShowItem;
import com.github.warren_bank.tiny_television_time_tracker.utils.FileUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.HardwareUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.ImageUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.NetworkUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.RuntimePermissionUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.SwipeDetect;
import com.github.warren_bank.tiny_television_time_tracker.utils.UrlUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.WakeLockMgr;
import com.github.warren_bank.tiny_television_time_tracker.utils.WifiLockMgr;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DroidShows extends ListActivity implements RuntimePermissionUtils.RuntimePermissionListener, Update.DatabaseUpdateListener {
  /* Menus */
  private static final int UNDO_MENU_ITEM = Menu.FIRST;
  private static final int FILTER_MENU_ITEM = UNDO_MENU_ITEM + 1;
  private static final int SEEN_MENU_ITEM = FILTER_MENU_ITEM + 1;
  private static final int SORT_MENU_ITEM = SEEN_MENU_ITEM + 1;
  private static final int TOGGLE_ARCHIVE_MENU_ITEM = SORT_MENU_ITEM + 1;
  private static final int LOG_MODE_ITEM = TOGGLE_ARCHIVE_MENU_ITEM + 1;
  private static final int SEARCH_MENU_ITEM = LOG_MODE_ITEM + 1;
  private static final int ADD_SERIE_MENU_ITEM = SEARCH_MENU_ITEM + 1;
  private static final int UPDATEALL_MENU_ITEM = ADD_SERIE_MENU_ITEM + 1;
  private static final int OPTIONS_MENU_ITEM = UPDATEALL_MENU_ITEM + 1;
  private static final int EXIT_MENU_ITEM = OPTIONS_MENU_ITEM + 1;
  /* Context Menus */
  private static final int VIEW_SEASONS_CONTEXT = Menu.FIRST;
  private static final int VIEW_SERIEDETAILS_CONTEXT = VIEW_SEASONS_CONTEXT + 1;
  private static final int VIEW_EPISODEDETAILS_CONTEXT = VIEW_SERIEDETAILS_CONTEXT + 1;
  private static final int EXT_RESOURCES_CONTEXT = VIEW_EPISODEDETAILS_CONTEXT + 1;
  private static final int MARK_NEXT_EPISODE_AS_SEEN_CONTEXT = EXT_RESOURCES_CONTEXT + 1;
  private static final int TOGGLE_ARCHIVED_CONTEXT = MARK_NEXT_EPISODE_AS_SEEN_CONTEXT + 1;
  private static final int PIN_CONTEXT = TOGGLE_ARCHIVED_CONTEXT + 1;
  private static final int UPDATE_CONTEXT = PIN_CONTEXT + 1;
  private static final int SYNOPSIS_LANGUAGE = UPDATE_CONTEXT + 1;
  private static final int DELETE_CONTEXT = SYNOPSIS_LANGUAGE + 1;
  private static AlertDialog m_AlertDlg;
  private static ProgressDialog m_ProgressDialog = null;
  public static SeriesAdapter seriesAdapter;
  private static BounceListView listView = null;
  private static int backFromSeasonSerieId = 0;

  // Preferences

  private static final String PREF_NAME = "DroidShowsPref";
  private SharedPreferences sharedPrefs;

  // ==============
  // Hidden
  // ==============

  private static final String BACKUP_FOLDER_PREF_NAME = "backup_folder";
  private static String backupFolder;

  private static final String LAST_STATS_UPDATE_NAME = "last_stats_update";
  private static String lastStatsUpdateCurrent;

  private static final String LAST_STATS_UPDATE_ARCHIVE_NAME = "last_stats_update_archive";
  private static String lastStatsUpdateArchive;

  private static final String FILTER_NETWORKS_NAME = "filter_networks";
  private static boolean filterNetworks;

  private static final String NETWORKS_NAME = "networks";
  private static List<String> networks = new ArrayList<String>();

  // ==============
  // ActionBar menu
  // ==============

  // Exclude seen?
  private static final String EXCLUDE_SEEN_PREF_NAME = "exclude_seen";
  private static boolean excludeSeen;

  // Sort shows...
  private static final String SORT_PREF_NAME        = "sort";
  private static final int SORT_BY_NAME             = 0;
  private static final int SORT_BY_UNSEEN_COUNT     = 1;
  private static final int SORT_BY_NEXT_UNSEEN_DATE = 2;
  private static final int SORT_BY_LAST_UNSEEN_DATE = 3;
  private static int sortOption;

  // ==============
  // Options Dialog
  // ==============

  // Automatically perform daily backups?
  private static final String AUTO_BACKUP_PREF_NAME = "auto_backup";
  private static boolean autoBackup;

  // Use versioning for backups?
  private static final String BACKUP_VERSIONING_PREF_NAME = "backup_versioning";
  private static boolean backupVersioning;

  // Enable "Pull-To-Update" gesture?
  private static final String ENABLE_PULL_TO_UPDATE_PREF_NAME = "pull_to_update";
  private static boolean enablePullToUpdate;

  // Show images in list?
  private static final String SHOW_ICONS = "show_icons";
  public static boolean showIcons;

  // Show date of next airing episode, instead of next episode's date?
  private static final String SHOW_NEXT_AIRING = "show_next_airing";
  public static boolean showNextAiring;

  // Mark next episode starting from the most recently watched?
  private static final String MARK_FROM_LAST_WATCHED = "mark_from_last_watched";
  public static boolean markFromLastWatched;

  // Include specials in unwatched count?
  private static final String INCLUDE_SPECIALS_NAME = "include_specials";
  public static boolean includeSpecialsOption;

  // Use entire line to mark episode?
  private static final String FULL_LINE_CHECK_NAME = "full_line";
  public static boolean fullLineCheckOption;

  // Swipe right-to-left to go back?
  private static final String SWITCH_SWIPE_DIRECTION = "switch_swipe_direction";
  public static boolean switchSwipeDirection;

  // Synopsis language...
  private static final String LANGUAGE_CODE_NAME = "language";
  public static String langCode;

  // -----

  private static Bitmap defaultIcon               = null;
  private static List<EpisodeMarkedAsSeen> undo   = new ArrayList<EpisodeMarkedAsSeen>();
  private static AsyncInfo asyncInfo;
  private static EditText searchV;
  private static View main;

  public static Thread deleteTh                   = null;
  public static Thread updateShowTh               = null;
  public static Thread updateAllShowsTh           = null;
  public static boolean logMode                   = false;
  public static boolean isUpdating                = false;
  public static int removeEpisodeIdFromLog        = 0;
  public static ApiGateway api;
  public static DbGateway db;
  public static List<TVShowItem> series;
  public static int showArchive;

  private SwipeDetect swipeDetect;
  private Vibrator vib;
  private Spinner spinner;
  private String dialogMsg;
  private InputMethodManager keyboard;
  private int padding;
  private TVShowItem lastSerie;
  private File[] dirList;
  private String[] dirNamesList;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!isTaskRoot()) {  // Prevent multiple instances: https://stackoverflow.com/a/11042163
      final Intent intent = getIntent();
      if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
        finish();
        return;
      }
    }
    setContentView(R.layout.main);
    main = findViewById(R.id.main);

    // Data Stores

    connectAPI();
    db = DbGateway.getInstance(DroidShows.this);

    // Preferences

    sharedPrefs = getSharedPreferences(PREF_NAME, 0);

    // ==============
    // Hidden
    // ==============

    backupFolder           = sharedPrefs.getString(BACKUP_FOLDER_PREF_NAME, (Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name_legacy)));
    lastStatsUpdateCurrent = sharedPrefs.getString(LAST_STATS_UPDATE_NAME, "");
    lastStatsUpdateArchive = sharedPrefs.getString(LAST_STATS_UPDATE_ARCHIVE_NAME, "");
    filterNetworks         = sharedPrefs.getBoolean(FILTER_NETWORKS_NAME, false);
    String networksStr     = sharedPrefs.getString(NETWORKS_NAME, "");

    // ==============
    // ActionBar menu
    // ==============

    excludeSeen            = sharedPrefs.getBoolean(EXCLUDE_SEEN_PREF_NAME, false);
    sortOption             = sharedPrefs.getInt(SORT_PREF_NAME, SORT_BY_NAME);

    // ==============
    // Options Dialog
    // ==============

    autoBackup             = sharedPrefs.getBoolean(AUTO_BACKUP_PREF_NAME, false);
    backupVersioning       = sharedPrefs.getBoolean(BACKUP_VERSIONING_PREF_NAME, true);
    enablePullToUpdate     = sharedPrefs.getBoolean(ENABLE_PULL_TO_UPDATE_PREF_NAME, true);
    showIcons              = sharedPrefs.getBoolean(SHOW_ICONS, true);
    showNextAiring         = sharedPrefs.getBoolean(SHOW_NEXT_AIRING, false);
    markFromLastWatched    = sharedPrefs.getBoolean(MARK_FROM_LAST_WATCHED, false);
    includeSpecialsOption  = sharedPrefs.getBoolean(INCLUDE_SPECIALS_NAME, false);
    fullLineCheckOption    = sharedPrefs.getBoolean(FULL_LINE_CHECK_NAME, false);
    switchSwipeDirection   = sharedPrefs.getBoolean(SWITCH_SWIPE_DIRECTION, false);
    langCode               = sharedPrefs.getString(LANGUAGE_CODE_NAME, getDefaultLangCode());

    // -----

    int maxWidthPx = HardwareUtils.getFractionOfScreenWidthPx(DroidShows.this, 0.20f); // 1/5th
    defaultIcon    = ImageUtils.decodeSampledBitmapFromResource(DroidShows.this, R.drawable.noposter, ImageUtils.getIconWidthPx(DroidShows.this, R.integer.images_small_width_dp, maxWidthPx));

    updateDatabase(Update.MODE_INSTALL);

    if (!networksStr.isEmpty())
      networks = new ArrayList<String>(Arrays.asList(networksStr.replace("[", "").replace("]", "").split(", ")));
    series = new ArrayList<TVShowItem>();
    seriesAdapter = new SeriesAdapter(DroidShows.this, R.layout.row, series);
    setListAdapter(seriesAdapter);
    listView = (BounceListView) getListView();
    listView.setEnablePullToUpdate(enablePullToUpdate);
    listView.setDivider(null);
    listView.setOverscrollHeader(getResources().getDrawable(R.drawable.shape_gradient_ring));
    if (savedInstanceState != null) {
      showArchive = savedInstanceState.getInt("showArchive");
      getSeries((savedInstanceState.getBoolean("searching") ? 2 : showArchive));
    } else {
      getSeries();
    }
    registerForContextMenu(listView);
    swipeDetect = new SwipeDetect();
    listView.setOnTouchListener(swipeDetect);
    searchV = (EditText) findViewById(R.id.search_text);
    searchV.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        seriesAdapter.getFilter().filter(s);
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      public void afterTextChanged(Editable s) {}
    });
    keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
    vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
  }

  private boolean connectAPI() {
    if (api != null) return true;

    try {
      api = ApiGateway.getInstance(DroidShows.this);
      return true;
    }
    catch(Exception e) {
      api = null;
      Toast.makeText(getApplicationContext(), R.string.menu_context_updated, Toast.LENGTH_LONG).show();
      return false;
    }
  }

  private String getDefaultLangCode() {
    String defaultLangCode = getString(R.string.lang_code);

    List<String> allLangCodes = Arrays.asList(
      getResources().getStringArray(R.array.langcodes)
    );

    if (allLangCodes.contains(defaultLangCode))
      return defaultLangCode;

    int index = defaultLangCode.indexOf("-");
    if (index > 1) {
      defaultLangCode = defaultLangCode.substring(0, index);

      if (allLangCodes.contains(defaultLangCode))
        return defaultLangCode;
    }

    defaultLangCode = getString(R.string.default_lang_code);
    return defaultLangCode;
  }

  private void updateDatabase(int mode) {
    Object passthrough = null;
    updateDatabase(mode, passthrough);
  }

  private void updateDatabase(int mode, Object passthrough) {
    boolean skipPreDatabaseUpdateCallback = false;
    updateDatabase(mode, passthrough, skipPreDatabaseUpdateCallback);
  }

  private void updateDatabase(int mode, Object passthrough, boolean skipPreDatabaseUpdateCallback) {
    cancelAsyncInfo();

    Update updateDS = new Update(DroidShows.this);
    updateDS.updateDatabase(DroidShows.this, mode, passthrough, skipPreDatabaseUpdateCallback);
  }

  @Override // Update.DatabaseUpdateListener
  public boolean preDatabaseUpdate(int mode, int oldVersion, boolean willUpdate) {
    cancelAsyncInfo();
    isUpdating = true;

    switch (mode) {
      case Update.MODE_INSTALL: {
        if (willUpdate) {
          boolean isPreUpdate = true;
          backupPermissionCheck(false, backupFolder, isPreUpdate);
          return false;
        }
        break;
      }

      case Update.MODE_RESTORE: {
        FileUtils.cleanAllImageDirectories(DroidShows.this);
        break;
      }
    }

    if (willUpdate && (oldVersion < 8)) {
      WakeLockMgr.acquire(DroidShows.this);
      WifiLockMgr.acquire(DroidShows.this);
    }

    return true;
  }

  @Override // Update.DatabaseUpdateListener
  public void postDatabaseUpdate(int mode, int oldVersion, boolean didUpdateFail, boolean didUpdateSucceed, boolean didUpdateAllSeries, Object passthrough) {
    if (didUpdateFail) {
      String error = getString(R.string.messages_db_error_update);
      Toast.makeText(DroidShows.this, error, Toast.LENGTH_LONG).show();
    }

    switch (mode) {
      case Update.MODE_INSTALL: {
        if (didUpdateSucceed) {
          updateShowStats();
        }
        break;
      }

      case Update.MODE_RESTORE: {
        if (!didUpdateFail) {
          String backupFile = (String) passthrough;
          String toastTxt   = getString(R.string.dialog_restore_done) + "\n(" + backupFile + ")";
          Toast.makeText(DroidShows.this, toastTxt, Toast.LENGTH_LONG).show();

          FileUtils.cleanDatabaseDirectory(DroidShows.this);
          undo.clear();

          if (!didUpdateAllSeries)
            db.updateSerieRemoveAllImageFilePaths();

          if (BuildConfig.DEBUG || didUpdateAllSeries) {
            updateShowStats();
          }
          else {
            // release build: perform a full update of all series in restored DB.
            // which calls updateShowStats() after it completes.

            runOnUiThread(new Runnable() {
              public void run() {
                updateAllSeries(2); // 2 = update archive and current shows
              }
            });
          }
        }
        break;
      }
    }

    if (oldVersion < 8) {
      WakeLockMgr.release();
      WifiLockMgr.release();
    }

    isUpdating = false;
  }

  private void setFastScroll() {
    listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    listView.setVerticalScrollBarEnabled(!excludeSeen || logMode);

  /*
    listView.setFastScrollEnabled(!excludeSeen || logMode);
    if (!excludeSeen || logMode) {
      if (seriesAdapter.getCount() > 20) {
        try {  // https://stackoverflow.com/a/26447004
          java.lang.reflect.Field fieldFastScroller = AbsListView.class.getDeclaredField(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "mFastScroll" : "mFastScroller");
          fieldFastScroller.setAccessible(true);
          Object thisFastScroller = fieldFastScroller.get(listView);
          Drawable thumb = getResources().getDrawable(R.drawable.thumb);
          java.lang.reflect.Field i;

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            i = fieldFastScroller.getType().getDeclaredField("mThumbImage");
            i.setAccessible(true);
            ImageView iv = (ImageView) i.get(thisFastScroller);
            iv.setImageDrawable(thumb);

            i = fieldFastScroller.getType().getDeclaredField("mThumbWidth");
            i.setAccessible(true);
            i.setInt(thisFastScroller, thumb.getIntrinsicWidth());

            i = fieldFastScroller.getType().getDeclaredField("mTrackImage");
            i.setAccessible(true);
            i.set(thisFastScroller, null);
          } else {
            i = fieldFastScroller.getType().getDeclaredField("mThumbDrawable");
            i.setAccessible(true);
            i.set(thisFastScroller, thumb);

            i = fieldFastScroller.getType().getDeclaredField("mThumbW");
            i.setAccessible(true);
            i.setInt(thisFastScroller, thumb.getIntrinsicWidth());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
              i = fieldFastScroller.getType().getDeclaredField("mTrackDrawable");
              i.setAccessible(true);
              i.set(thisFastScroller, null);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  */

  }

  /* Options Menu */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, UNDO_MENU_ITEM, 0, getString(R.string.menu_undo)).setIcon(android.R.drawable.ic_menu_revert);
    menu.add(0, FILTER_MENU_ITEM, 0, getString(R.string.menu_filter)).setIcon(android.R.drawable.ic_menu_view);
    menu.add(0, SEEN_MENU_ITEM, 0, "").setIcon(android.R.drawable.ic_menu_myplaces);
    menu.add(0, SORT_MENU_ITEM, 0, getString(R.string.menu_sort)).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
    menu.add(0, TOGGLE_ARCHIVE_MENU_ITEM, 0, "");
    menu.add(0, LOG_MODE_ITEM, 0, getString(R.string.menu_log)).setIcon(android.R.drawable.ic_menu_agenda);
    menu.add(0, SEARCH_MENU_ITEM, 0, getString(R.string.menu_search)).setIcon(android.R.drawable.ic_menu_search);
    menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
    menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update)).setIcon(android.R.drawable.ic_menu_upload);
    menu.add(0, OPTIONS_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_manage);
    menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
      arrangeActionBar(menu);
    return super.onCreateOptionsMenu(menu);
  }

  @SuppressLint("NewApi")
  private void arrangeActionBar(Menu menu) {
    menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM).setVisible(false);
    menu.findItem(LOG_MODE_ITEM).setVisible(false);
    menu.findItem(SEARCH_MENU_ITEM).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    spinner = new Spinner(DroidShows.this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
      spinner.setPopupBackgroundResource(R.drawable.menu_dropdown_panel);
    spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
      new String[] {
        getString(R.string.active),
        getString(R.string.archive),
        getString(R.string.menu_log),
      }));
    listView.postDelayed(new Runnable() {
      public void run() {
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            logMode = position == 2;
            showArchive = ((position == 2) ? showArchive : position);
            if (logMode)
              clearFilter(null);
            getSeries();
          }
          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
      }
    }, 1000);
    ActionBar actionBar = getActionBar();
    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
    actionBar.setCustomView(spinner);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
      actionBar.setIcon(R.drawable.actionbar);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(UNDO_MENU_ITEM)
      .setVisible(undo.size() > 0);
    menu.findItem(FILTER_MENU_ITEM)
      .setEnabled(!logMode && !searching());
    menu.findItem(SEEN_MENU_ITEM)
      .setEnabled(!logMode && !searching());
    menu.findItem(SORT_MENU_ITEM)
      .setEnabled(!logMode);
    menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
      .setEnabled(!logMode && !searching());
    menu.findItem(LOG_MODE_ITEM)
      .setEnabled(!searching())
      .setTitle((!logMode ? R.string.menu_log : R.string.menu_close_log));
    menu.findItem(UPDATEALL_MENU_ITEM)
      .setEnabled(!logMode);

    if (showArchive == 1) {
      menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
        .setIcon(android.R.drawable.ic_menu_today)
        .setTitle(R.string.menu_show_current);
    } else {
      menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
        .setIcon(android.R.drawable.ic_menu_recent_history)
        .setTitle(R.string.menu_show_archive);
    }
    menu.findItem(SEEN_MENU_ITEM).setTitle(excludeSeen ? R.string.menu_include_seen : R.string.menu_exclude_seen);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    // ==============
    // ActionBar menu
    // ==============

    switch (item.getItemId()) {
      case ADD_SERIE_MENU_ITEM :
        super.onSearchRequested();
        break;
      case SEARCH_MENU_ITEM :
        onSearchRequested();
        break;
      case TOGGLE_ARCHIVE_MENU_ITEM :
        toggleArchive();
        break;
      case SEEN_MENU_ITEM :
        toggleSeen();
        break;
      case SORT_MENU_ITEM :
        sortDialog();
        break;
      case FILTER_MENU_ITEM :
        filterDialog();
        break;
      case UPDATEALL_MENU_ITEM :
        updateAllSeriesDialog();
        break;
      case OPTIONS_MENU_ITEM :
        aboutDialog();
        break;
      case UNDO_MENU_ITEM :
        markLastEpUnseen();
        break;
      case LOG_MODE_ITEM :
        toggleLogMode();
        break;
      case EXIT_MENU_ITEM :
        onPause();  // save options
        backup(true);
        db.getSQLiteStore().close();
        this.finish();
        System.gc();
        System.exit(0);  // kill process
    }
    return super.onOptionsItemSelected(item);
  }

  private void toggleArchive() {
    showArchive = (showArchive + 1) % 2;
    getSeries();
    listView.setSelection(0);
  }

  private void toggleSeen() {
    excludeSeen ^= true;
    listView.post(updateListView);
  }

  private void toggleLogMode() {
    logMode ^= true;
    getSeries();
    removeEpisodeIdFromLog = 0;
    listView.setSelection(0);
  }

  private void sortDialog() {
    if (m_AlertDlg != null) {
      m_AlertDlg.dismiss();
    }
    m_AlertDlg = new AlertDialog.Builder(DroidShows.this)
      .setTitle(R.string.menu_sort)
      .setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? R.drawable.icon : 0)
      .setSingleChoiceItems(R.array.menu_sort_options, sortOption, null)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          m_AlertDlg.dismiss();

          try {
            int selected_option_index = m_AlertDlg.getListView().getCheckedItemPosition();
            if ((sortOption != selected_option_index) && (selected_option_index >= 0)) {
              sortOption = selected_option_index;
              listView.post(updateListView);
            }
          }
          catch(Exception e) {}
        }
      })
      .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          m_AlertDlg.dismiss();
        }
      })
      .show();
  }

  private void filterDialog() {
    if (m_AlertDlg != null) {
      m_AlertDlg.dismiss();
    }
    final View filterV = View.inflate(DroidShows.this, R.layout.alert_filter, null);
    ((CheckBox) filterV.findViewById(R.id.exclude_seen)).setChecked(excludeSeen);
    List<String> allNetworks = db.getNetworks();
    final LinearLayout networksFilterV = (LinearLayout) filterV.findViewById(R.id.networks_filter);
    for (String network : allNetworks) {
      CheckBox networkCheckBox = new CheckBox(DroidShows.this);
      networkCheckBox.setText(network);
      if (!networks.isEmpty())
        networkCheckBox.setChecked(networks.contains(network));
      networksFilterV.addView(networkCheckBox);
    }
    ToggleButton networksFilter = (ToggleButton) filterV.findViewById(R.id.toggle_networks_filter);
    networksFilter.setChecked(filterNetworks);
    toggleNetworksFilter(networksFilter);
    m_AlertDlg = new AlertDialog.Builder(DroidShows.this)
      .setView(filterV)
      .setTitle(R.string.menu_filter)
      .setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? R.drawable.icon : 0)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          applyFilters((ScrollView) filterV, networksFilterV);
        }
      })
      .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          m_AlertDlg.dismiss();
        }
      })
      .show();
  }

  public void toggleNetworksFilter(View v) {
    boolean enabled = (((ToggleButton) v).isChecked());
    LinearLayout networksFilterV = (LinearLayout) ((View) v.getParent().getParent()).findViewById(R.id.networks_filter);
    for (int i = 0; i < networksFilterV.getChildCount(); i++) {
      networksFilterV.getChildAt(i).setEnabled(enabled);
    }
  }

  private void applyFilters(ScrollView filterV, LinearLayout networksFilterV) {
    excludeSeen = (((CheckBox) filterV.findViewById(R.id.exclude_seen)).isChecked() ? true : false);
    filterNetworks = (((ToggleButton) filterV.findViewById(R.id.toggle_networks_filter)).isChecked() == true);
    for (int i = 0; i < networksFilterV.getChildCount(); i++) {
      CheckBox networkCheckBox = (CheckBox) networksFilterV.getChildAt(i);
      String network = (String) networkCheckBox.getText();
      if (networkCheckBox.isChecked()) {
        if (!networks.contains(network))
          networks.add(network);
      } else {
        if (networks.contains(network))
          networks.remove(network);
      }
    }
    getSeries();
  }

  private void aboutDialog() {
    if (m_AlertDlg != null) {
      m_AlertDlg.dismiss();
    }
    View about = View.inflate(DroidShows.this, R.layout.alert_about, null);
    TextView changelog = (TextView) about.findViewById(R.id.copyright);
    try {
      changelog.setText(getString(R.string.copyright)
        .replace("{app-name}", getString(R.string.app_name_long))
        .replace("{version}",  getPackageManager().getPackageInfo(getPackageName(), 0).versionName)
        .replace("{year}",     Calendar.getInstance().get(Calendar.YEAR) +""));
      changelog.setTextColor(changelog.getTextColors().getDefaultColor());
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }

    // ==============
    // Options Dialog
    // ==============

    ((CheckBox) about.findViewById(R.id.auto_backup)).setChecked(autoBackup);
    ((CheckBox) about.findViewById(R.id.backup_versioning)).setChecked(backupVersioning);
    ((CheckBox) about.findViewById(R.id.pull_to_update)).setChecked(enablePullToUpdate);
    ((CheckBox) about.findViewById(R.id.show_icons)).setChecked(showIcons);
    ((CheckBox) about.findViewById(R.id.show_next_airing)).setChecked(showNextAiring);
    ((CheckBox) about.findViewById(R.id.mark_from_last_watched)).setChecked(markFromLastWatched);
    ((CheckBox) about.findViewById(R.id.include_specials)).setChecked(includeSpecialsOption);
    ((CheckBox) about.findViewById(R.id.full_line_check)).setChecked(fullLineCheckOption);
    ((CheckBox) about.findViewById(R.id.switch_swipe_direction)).setChecked(switchSwipeDirection);
    ((TextView) about.findViewById(R.id.change_language)).setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");

    // -----

    m_AlertDlg = new AlertDialog.Builder(DroidShows.this)
      .setView(about)
      .setTitle(R.string.menu_about)
      .setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? R.drawable.icon : 0)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          m_AlertDlg.dismiss();
        }
      })
      .show();
  }

  public void dialogOptions(View v) {

    // ==============
    // Options Dialog
    // ==============

    switch(v.getId()) {
      case R.id.backup:
        m_AlertDlg.dismiss();
        backup(false);
        break;
      case R.id.restore:
        m_AlertDlg.dismiss();
        restore();
        break;
      case R.id.auto_backup:
        autoBackup ^= true;
        break;
      case R.id.backup_versioning:
        backupVersioning ^= true;
        break;
      case R.id.pull_to_update:
        enablePullToUpdate ^= true;
        listView.setEnablePullToUpdate(enablePullToUpdate);
        break;
      case R.id.show_icons:
        showIcons ^= true;
        if (!showIcons)
          deleteCachedIcons();
        listView.post(updateListView);
        break;
      case R.id.show_next_airing:
        showNextAiring ^= true;
        updateShowStats();
        break;
      case R.id.mark_from_last_watched:
        markFromLastWatched ^= true;
        updateShowStats();
        break;
      case R.id.include_specials:
        includeSpecialsOption ^= true;
        updateShowStats();
        break;
      case R.id.full_line_check:
        fullLineCheckOption ^= true;
        break;
      case R.id.switch_swipe_direction:
        switchSwipeDirection ^= true;
        break;
      case R.id.change_language:
        AlertDialog.Builder changeLang = new AlertDialog.Builder(DroidShows.this);
        changeLang.setTitle(R.string.dialog_change_language)
          .setItems(R.array.languages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
              langCode = getResources().getStringArray(R.array.langcodes)[item];
              TextView changeLangB = (TextView) m_AlertDlg.findViewById(R.id.change_language);
              changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
            }
          })
          .show();
      break;
    }
  }

  private void deleteCachedIcons() {
    for (TVShowItem serie : series) {
      serie.setDIcon(null);
    }
  }

  private void updateShowStats() {
    Runnable updateShowStats = new Runnable() {
      public void run() {
        db.updateShowStats();
        listView.post(new Runnable() {
          public void run() {getSeries((searching() ? 2 : showArchive));}
        });
      }
    };
    Thread updateShowStatsTh = new Thread(updateShowStats);
    updateShowStatsTh.start();
  }

  private void backup(boolean auto) {
    if (auto) {
      backupPermissionCheck(auto, backupFolder);
    }
    else {
      File folder = new File(backupFolder);
      if (!folder.isDirectory())
        folder.mkdir();
      filePickerPermissionCheck(backupFolder, false);
    }
  }

  private void restore() {
    filePickerPermissionCheck(backupFolder, true);
  }

  private void filePickerPermissionCheck(final String folderString, final boolean restoring) {
    String[] allRequestedPermissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};

    int requestCode = restoring
      ? Constants.PERMISSION_CHECK_REQUEST_CODE_RESTORE_DATABASE_FILEPICKER
      : Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_FILEPICKER;

    Object passthrough = (Object) folderString;

    RuntimePermissionUtils.requestPermissions(DroidShows.this, DroidShows.this, allRequestedPermissions, requestCode, passthrough);
  }

  private class PassthroughBackup {
    protected boolean auto;
    protected String  backupFolder;

    protected PassthroughBackup(boolean auto, String backupFolder) {
      this.auto = auto;
      this.backupFolder = backupFolder;
    }
  }

  private void backupPermissionCheck(boolean auto, String backupFolder) {
    boolean isPreUpdate = false;
    backupPermissionCheck(auto, backupFolder, isPreUpdate);
  }

  private void backupPermissionCheck(boolean auto, String backupFolder, boolean isPreUpdate) {
    if (auto && !autoBackup) return;

    String[] allRequestedPermissions = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};

    int requestCode = isPreUpdate
      ? Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE
      : Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE;

    Object passthrough = (Object) new PassthroughBackup(auto, backupFolder);

    RuntimePermissionUtils.requestPermissions(DroidShows.this, DroidShows.this, allRequestedPermissions, requestCode, passthrough);
  }

  @Override
  public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.onRequestPermissionsResult(DroidShows.this, DroidShows.this, requestCode, permissions, grantResults);
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsGranted(int requestCode, Object passthrough) {
    switch(requestCode) {
      case Constants.PERMISSION_CHECK_REQUEST_CODE_RESTORE_DATABASE_FILEPICKER: {
          String folderString = (String) passthrough;
          boolean restoring   = true;
          filePicker(folderString, restoring);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_FILEPICKER: {
          String folderString = (String) passthrough;
          boolean restoring   = false;
          filePicker(folderString, restoring);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE: {
          // the user has granted the permission required to save a backup of the DB.. before updating the version of the database schema

          PassthroughBackup pb = (PassthroughBackup) passthrough;
          backup(pb.auto, pb.backupFolder);

          updateDatabase(/* mode */ Update.MODE_INSTALL, /* passthrough */ (Object) null, /* skipPreDatabaseUpdateCallback */ true);
        }
        break;
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE: {
          PassthroughBackup pb = (PassthroughBackup) passthrough;
          backup(pb.auto, pb.backupFolder);
        }
        break;
    }
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsDenied(int requestCode, Object passthrough, String[] missingPermissions) {
    switch(requestCode) {
      case Constants.PERMISSION_CHECK_REQUEST_CODE_BACKUP_DATABASE_PREUPDATE: {
          // the user has denied the permission required to save a backup of the DB.. before updating the version of the database schema;
          // explain the implications and offer one final chance to do so.

          new AlertDialog.Builder(DroidShows.this)
            .setTitle(R.string.dialog_backup_preupdate_title)
            .setMessage(R.string.dialog_backup_preupdate_message)
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // ask to grant the permissions again

                PassthroughBackup pb = (PassthroughBackup) passthrough;
                backupPermissionCheck(pb.auto, pb.backupFolder, /* isPreUpdate */ true);
              }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // perform update without backup

                updateDatabase(/* mode */ Update.MODE_INSTALL, /* passthrough */ (Object) null, /* skipPreDatabaseUpdateCallback */ true);
              }
            })
            .show();
        }
        break;
      default: {
          Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
        }
        break;
    }
  }

  private void filePicker(final String folderString, final boolean restoring) {
    File folder = new File(folderString);
    File[] tempDirList = dirContents(folder, restoring);
    int showParent = (folderString.equals(Environment.getExternalStorageDirectory().getPath()) ? 0 : 1);
    dirList = new File[tempDirList.length + showParent];
    dirNamesList = new String[tempDirList.length + showParent];
    if (showParent == 1) {
      dirList[0] = folder.getParentFile();
      dirNamesList[0] = "..";
    }
    for(int i = 0; i < tempDirList.length; i++) {
      dirList[i + showParent] = tempDirList[i];
      dirNamesList[i + showParent] = tempDirList[i].getName();
      if (restoring && tempDirList[i].isFile())
        dirNamesList[i + showParent] += " ("+ DateFormats.DISPLAY_FILE_PICKER.format(tempDirList[i].lastModified()) +")";
    }
    AlertDialog.Builder filePicker = new AlertDialog.Builder(DroidShows.this)
      .setTitle(folder.toString())
      .setItems(dirNamesList, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          File chosenFile = dirList[which];
          if (chosenFile.isDirectory()) {
            filePicker(chosenFile.toString(), restoring);
          } else if (restoring) {
            confirmRestore(chosenFile.toString());
          }
        }
      })
      .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });

    if (!restoring)
      filePicker.setPositiveButton(R.string.dialog_backup, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          backup(false, folderString);
        }
      });
    filePicker.show();
  }

  private File[] dirContents(File folder, final boolean showFiles)  {
    if (folder.exists()) {
      FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String filename) {
          File file = new File(dir.getAbsolutePath() + File.separator + filename);
          if (showFiles)
            return file.isDirectory()
              || file.isFile() && file.getName().toLowerCase().indexOf("droidshows.db") == 0;
          else
            return file.isDirectory();
        }
      };
      File[] list = folder.listFiles(filter);
      if (list != null)
        Arrays.sort(list, filesComperator);
      return list == null ? new File[0] : list;
    } else {
      return new File[0];
    }
  }

  private static Comparator<File> filesComperator = new Comparator<File>() {
    public int compare(File f1, File f2) {
      if (f1.isDirectory() && !f2.isDirectory())
        return 1;
      if (f2.isDirectory() && !f1.isDirectory())
        return -1;
      return f1.getName().compareToIgnoreCase(f2.getName());
    }
  };

  private void backup(boolean auto, final String backupFolder) {
    File source = new File(FileUtils.getDatabaseFilePath(DroidShows.this));
    File destination = new File(backupFolder, FileUtils.getDatabaseFileName(DroidShows.this));
    if (auto && (!autoBackup ||
        DateFormats.NORMALIZE_DATE.format(destination.lastModified()).equals(lastStatsUpdateCurrent) ||
        source.lastModified() == destination.lastModified()))
      return;
    if (backupVersioning && destination.exists()) {
      File previous0 = new File(backupFolder, FileUtils.getDatabaseFileNamePreviousBackup(DroidShows.this, 0));
      if (previous0.exists()) {
        File previous1 = new File(backupFolder, FileUtils.getDatabaseFileNamePreviousBackup(DroidShows.this, 1));
        if (previous1.exists())
          previous1.delete();
        previous0.renameTo(previous1);
      }
      destination.renameTo(previous0);
    } else
      destination.delete();
    File folder = new File(backupFolder);
    if (!folder.isDirectory())
      folder.mkdir();
    int toastTxt = R.string.dialog_backup_done;
    try {
      copy(source, destination);
    } catch (IOException e) {
      toastTxt = R.string.dialog_backup_failed;
      e.printStackTrace();
    }
    if (!auto && toastTxt == R.string.dialog_backup_done && !backupFolder.equals(DroidShows.backupFolder)) {
      final CharSequence[] backupFolders = {backupFolder, DroidShows.backupFolder};
      new AlertDialog.Builder(DroidShows.this)
        .setTitle(toastTxt)
        .setSingleChoiceItems(backupFolders, 1, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            DroidShows.backupFolder = backupFolders[which].toString();
          }
        })
        .setPositiveButton(R.string.dialog_backup_usefolder, null)
        .show();
    }
    if (!auto && listView != null) {
      Toast.makeText(getApplicationContext(), getString(toastTxt) + "\n("+ backupFolder +")", Toast.LENGTH_LONG).show();
      startAsyncInfo();
    }
  }

  private void confirmRestore(final String backupFile) {
    new AlertDialog.Builder(DroidShows.this)
      .setTitle(R.string.dialog_restore)
      .setMessage(R.string.dialog_restore_now)
      .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          restore(backupFile);
        }
      })
      .setNegativeButton(R.string.dialog_cancel, null)
      .show();
  }

  private void restore(String backupFile) {
    File source      = new File(backupFile);
    File destination = new File(FileUtils.getDatabaseFilePath(DroidShows.this));

    if (!source.exists()) {
      Toast.makeText(DroidShows.this, getString(R.string.dialog_restore_notfound), Toast.LENGTH_LONG).show();
      return;
    }

    try {
      copy(source, destination);
    }
    catch (IOException e) {
      Toast.makeText(DroidShows.this, getString(R.string.dialog_restore_failed), Toast.LENGTH_LONG).show();
      return;
    }

    updateDatabase(Update.MODE_RESTORE, (Object) backupFile);
  }

  private void copy(File source, File destination) throws IOException {
    if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
      throw new IOException("External Storage is not mounted.");

    if (asyncInfo != null)
      asyncInfo.cancel(true);

    db.getSQLiteStore().close();
    try {
      FileUtils.copyFile(source, destination);
    }
    finally {
      db.getSQLiteStore().openDataBase();
    }
  }

  /* context menu */
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    TVShowItem serie = seriesAdapter.getItem(info.position);
    if (logMode)
      menu.add(0, VIEW_SEASONS_CONTEXT, VIEW_SEASONS_CONTEXT, getString(R.string.messages_seasons));
    menu.add(0, VIEW_SERIEDETAILS_CONTEXT, VIEW_SERIEDETAILS_CONTEXT, getString(R.string.menu_context_view_serie_details));
    if (!logMode && serie.getUnwatched() > 0)
      menu.add(0, VIEW_EPISODEDETAILS_CONTEXT, VIEW_EPISODEDETAILS_CONTEXT, getString(R.string.messsages_view_ep_details));
    menu.add(0, EXT_RESOURCES_CONTEXT, EXT_RESOURCES_CONTEXT, getString(R.string.menu_context_ext_resources));
    if (!logMode && serie.getUnwatchedAired() > 0)
      menu.add(0, MARK_NEXT_EPISODE_AS_SEEN_CONTEXT, MARK_NEXT_EPISODE_AS_SEEN_CONTEXT, getString(R.string.menu_context_mark_next_episode_as_seen));
    if (!logMode) {
      menu.add(0, TOGGLE_ARCHIVED_CONTEXT, TOGGLE_ARCHIVED_CONTEXT, getString(R.string.menu_archive));
      menu.add(0, PIN_CONTEXT, PIN_CONTEXT, getString(R.string.menu_context_pin));
      menu.add(0, DELETE_CONTEXT, DELETE_CONTEXT, getString(R.string.menu_context_delete));
      menu.add(0, UPDATE_CONTEXT, UPDATE_CONTEXT, getString(R.string.menu_context_update));
      menu.add(0, SYNOPSIS_LANGUAGE, SYNOPSIS_LANGUAGE, getString(R.string.dialog_change_language) +" ("+ serie.getLanguage() +")");
        if (serie.getArchived())
          menu.findItem(TOGGLE_ARCHIVED_CONTEXT).setTitle(R.string.menu_unarchive);
        if (serie.getPinned())
          menu.findItem(PIN_CONTEXT).setTitle(R.string.menu_context_unpin);
    }
    menu.setHeaderTitle(!logMode ? serie.getName() : serie.getEpisodeName());
  }

  public boolean onContextItemSelected(MenuItem item) {
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    final TVShowItem serie = seriesAdapter.getItem(info.position);
    switch(item.getItemId()) {
      case MARK_NEXT_EPISODE_AS_SEEN_CONTEXT :
        markNextEpSeen(info.position);
        return true;
      case VIEW_SEASONS_CONTEXT :
        serieSeasons(info.position);
        return true;
      case VIEW_SERIEDETAILS_CONTEXT :
        showDetails(serie.getSerieId());
        return true;
      case VIEW_EPISODEDETAILS_CONTEXT :
        episodeDetails(info.position);
        return true;
      case EXT_RESOURCES_CONTEXT :
        extResources(serie.getExtResources(), info.position);
        return true;
      case UPDATE_CONTEXT :
        updateSerie(serie, info.position);
        return true;
      case SYNOPSIS_LANGUAGE :
        AlertDialog.Builder changeLang = new AlertDialog.Builder(DroidShows.this);
        changeLang.setTitle(R.string.dialog_change_language)
          .setItems(R.array.languages, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
              String langCode = getResources().getStringArray(R.array.langcodes)[item];
              updateSerie(serie, langCode, info.position);
            }
          })
          .show();
        return true;
      case TOGGLE_ARCHIVED_CONTEXT :
        cancelAsyncInfo();
        boolean archived = !serie.getArchived();
        db.updateSerieArchived(serie.getSerieId(), archived);
        String message = serie.getName() + " " +
          (archived ? getString(R.string.messages_context_archived) : getString(R.string.messages_context_unarchived));
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        if (!searching())
          series.remove(serie);
        else
          serie.setArchived(archived);
        listView.post(updateListView);
        startAsyncInfo();
        return true;
      case PIN_CONTEXT :
        boolean pinned = !serie.getPinned();
        db.updateSeriePinned(serie.getSerieId(), pinned);
        serie.setPinned(pinned);
        listView.post(updateListView);
        return true;
      case DELETE_CONTEXT :
        cancelAsyncInfo();
        final Runnable deleteserie = new Runnable() {
          public void run() {
            String sname = serie.getName();
            String toastMsg = getString(R.string.messages_deleted);
            if (!db.deleteSerie(serie.getSerieId()))
              toastMsg = getString(R.string.messages_db_error_delete_serie);
            series.remove(series.indexOf(serie));
            listView.post(updateListView);
            Looper.prepare();  // Threads don't have a message loop
            Toast.makeText(getApplicationContext(), sname +" "+ toastMsg, Toast.LENGTH_LONG).show();
            startAsyncInfo();
            Looper.loop();
          }
        };
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DroidShows.this)
          .setTitle(R.string.dialog_title_delete)
          .setMessage(String.format(getString(R.string.dialog_delete), serie.getName()))
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setCancelable(false)
          .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              deleteTh = new Thread(deleteserie);
              deleteTh.start();
              return;
            }
          })
          .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
              return;
            }
          });
        alertDialog.show();
        return true;
      default :
        return super.onContextItemSelected(item);
    }
  }

  @SuppressLint("NewApi")
  public void openContext(View v) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
      listView.showContextMenuForChild(v, v.getX(), v.getY());
    else
      openContextMenu(v);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
    if (swipeDetect.value == 1 && (seriesAdapter.getItem(position).getUnwatchedAired() > 0 ||
         seriesAdapter.getItem(position).getNextAir() != null &&
        !seriesAdapter.getItem(position).getNextAir().after(Calendar.getInstance().getTime()))) {
      vib.vibrate(150);
      markNextEpSeen(position);
    } else if (swipeDetect.value == 0) {
      if (!logMode) {
        serieSeasons(position);
      } else {
        episodeDetails(position);
      }
    }
  }

  private void markNextEpSeen(int position) {
    TVShowItem serie = seriesAdapter.getItem(position);
    int serieId = serie.getSerieId();
    int nextEpisode = db.getNextEpisodeId(serieId, true);
    if (nextEpisode != -1) {
      String episodeMarked = db.updateUnwatchedEpisode(serieId, nextEpisode);
      Toast.makeText(getApplicationContext(), serie.getName() +" "+ episodeMarked +" "+ getString(R.string.messages_marked_seen), Toast.LENGTH_SHORT).show();
      undo.add(new EpisodeMarkedAsSeen(serieId, nextEpisode, serie.getName()));
      updateShowView(serie);
    }
  }

  private void markLastEpUnseen() {
    EpisodeMarkedAsSeen episodeInfo = undo.get(undo.size()-1);
    int    serieId   = episodeInfo.serieId;
    int    episodeId = episodeInfo.episodeId;
    String serieName = episodeInfo.serieName;
    String episodeMarked = db.updateUnwatchedEpisode(serieId, episodeId);
    undo.remove(undo.size()-1);
    Toast.makeText(getApplicationContext(), serieName +" "+ episodeMarked +" "+ getString(R.string.messages_marked_unseen), Toast.LENGTH_SHORT).show();
    listView.post(updateShowView(serieId));
  }

  private void serieSeasons(int position) {
    backFromSeasonSerieId = seriesAdapter.getItem(position).getSerieId();
    Intent serieSeasons   = new Intent(DroidShows.this, SerieSeasons.class);
    serieSeasons.putExtra("serieId",     backFromSeasonSerieId);
    serieSeasons.putExtra("nextEpisode", seriesAdapter.getItem(position).getUnwatched() > 0);
    startActivity(serieSeasons);
  }

  private Runnable updateShowView(final int serieId) {
    Runnable updateView = new Runnable(){
      public void run() {
        for (TVShowItem serie : series) {
          if (serie.getSerieId() == serieId) {
            updateShowView(serie);
            break;
          }
        }
      }
    };
    return updateView;
  }

  private void updateShowView(final TVShowItem serie) {
    final int position = seriesAdapter.getPosition(serie);
    final TVShowItem newSerie = db.createTVShowItem(serie.getSerieId());
    lastSerie = newSerie;
    series.set(series.indexOf(serie), newSerie);
    listView.post(updateListView);
    listView.post(new Runnable() {
      public void run() {
        int newPosition = seriesAdapter.getPosition(newSerie);
        if (newPosition != position) {
          listView.setSelection(newPosition);
          if (listView.getLastVisiblePosition() > newPosition)
            listView.smoothScrollBy(-padding, 400);
        }
      }
    });
  }

  private void showDetails(int serieId) {
    Intent viewSerie = new Intent(DroidShows.this, ViewSerie.class);
    viewSerie.putExtra("serieId", serieId);
    startActivity(viewSerie);
  }

  private void episodeDetails(int position) {
    int serieId = seriesAdapter.getItem(position).getSerieId();
    int episodeId = -1;
    if (!logMode)
      episodeId = db.getNextEpisodeId(serieId);
    else
      episodeId = seriesAdapter.getItem(position).getEpisodeId();
    if (episodeId != -1) {
      backFromSeasonSerieId = serieId;
      Intent viewEpisode    = new Intent(DroidShows.this, ViewEpisode.class);
      viewEpisode.putExtra("serieName", seriesAdapter.getItem(position).getName());
      viewEpisode.putExtra("serieId",   serieId);
      viewEpisode.putExtra("episodeId", episodeId);
      startActivity(viewEpisode);
    }
  }

  private void Search(String url, String serieName) {
    serieName = UrlUtils.encodeSerieNameForQuerystringValue(serieName);
    Intent rt = new Intent(Intent.ACTION_VIEW, Uri.parse(url + serieName));
    rt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(rt);
  }

  private void WikiDetails(String serieName, String serieLangCode) {
    Intent wiki;
    String wikiApp = null;
      if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia") != null)
        wikiApp = "org.wikipedia";
      else if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia.beta") != null)
        wikiApp = "org.wikipedia.beta";
      if (wikiApp == null) {
        serieName = serieName + " (" + getString(R.string.wiki_search_query_suffix) + ")";
        serieName = UrlUtils.encodeSerieNameForQuerystringValue(serieName);
        String uri = "https://" + serieLangCode + ".m.wikipedia.org/w/index.php?search=" + serieName;
        wiki = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
      } else {
        serieName = UrlUtils.encodeSerieNameForIntentExtra(serieName);
        wiki = new Intent(Intent.ACTION_SEND)
          .putExtra(Intent.EXTRA_TEXT, serieName)
          .setType("text/plain")
          .setPackage(wikiApp);
      }
      wiki.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(wiki);
  }

  private void IMDbDetails(int serieId, String serieName, int episodeId) {
    String imdbId = null, uri;

    if (serieId > 0) {
      if (episodeId > 0) {
        imdbId = db.getEpisodeIMDbId(serieId, episodeId);
      }
      if (imdbId == null) {
        imdbId = db.getSerieIMDbId(serieId);
      }
    }

    Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
    uri = (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
      ? "https://m.imdb.com/"
      : "imdb:///";

    uri += ((imdbId != null) && imdbId.startsWith("tt"))
      ? ("title/"  + imdbId)
      : ("find?q=" + UrlUtils.encodeSerieNameForQuerystringValue(serieName));

    Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(imdb);
  }

  private void extResources(String extResourcesString, final int position) {
    if (extResourcesString.length() > 0) {
      String[] tmpResources = extResourcesString.trim().split("\\n");
      extResourcesString = "";
      for (int i = 0; i < tmpResources.length; i++) {
        String url = tmpResources[i].trim();
        if (url.length() > 0)
          extResourcesString += url +"\n";
      }
    }
    final String[] extResources = (
        getString(R.string.menu_context_view_imdb) +"\n"+
        getString(R.string.menu_context_view_ep_imdb) +"\n"+
        getString(R.string.menu_context_search_on) +" FANDOM (Wikia)\n"+
        getString(R.string.menu_context_search_on) +" Rotten Tomatoes\n"+
        getString(R.string.menu_context_search_on) +" Wikipedia\n"+
        extResourcesString
        +"\u2026").split("\\n");
    final EditText input = new EditText(DroidShows.this);
    final String extResourcesInput = extResourcesString;
    final TVShowItem serie = seriesAdapter.getItem(position);
    input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_VARIATION_URI);
    new AlertDialog.Builder(DroidShows.this)
      .setTitle(serie.getName())
      .setItems(extResources, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          switch(item) {
            case 0 :
              IMDbDetails(serie.getSerieId(), serie.getName(), -1);
              break;
            case 1 :
              IMDbDetails(serie.getSerieId(), serie.getName(), logMode ? serie.getEpisodeId() : db.getNextEpisodeId(serie.getSerieId()));
              break;
            case 2 :
              Search("https://www.fandom.com/?s=", serie.getName());
              break;
            case 3 :
              Search("https://www.rottentomatoes.com/search/?search=", serie.getName());
              break;
            case 4 :
              WikiDetails(serie.getName(), serie.getLanguage());
              break;
            default :
              if (item == extResources.length-1) {
                input.setText(extResourcesInput);
                new AlertDialog.Builder(DroidShows.this)
                  .setTitle(serie.getName())
                  .setView(input)
                  .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
                      String resources = input.getText().toString().trim();
                      serie.setExtResources(resources);
                      db.updateExtResources(serie.getSerieId(), resources);
                      return;
                    }
                  })
                  .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
                      return;
                    }
                  })
                  .show();
                if (extResourcesInput.length() == 0) {
                  input.setText("Examples:\ntvshow.wikia.com\n*tvshow.blogspot.com\nLong-press show poster to directly open the starred url");
                  input.selectAll();
                }
                input.requestFocus();
                keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
              } else {
                browseExtResource(extResources[item]);
              }
          }
        }
      })
      .show();
  }

  private void browseExtResource(String url) {
    url = url.trim();
    if (url.startsWith("*"))
      url = url.substring(1).trim();
    if (!url.startsWith("http"))
      url = "https://"+ url;
    Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    browse.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(browse);
  }

  private void updateSerie(final TVShowItem serie, int position) {
    updateSerie(serie, null, position);
  }

  private void updateSerie(TVShowItem serie, final String langCode, int position) {
    if (!NetworkUtils.isNetworkAvailable(DroidShows.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else if ((m_ProgressDialog == null) || !m_ProgressDialog.isShowing()) {
      if (!connectAPI()) return;

      final int serieId        = serie.getSerieId();
      final String serieName   = serie.getName();
      final String currentLang = serie.getLanguage();
      Runnable updateserierun = new Runnable() {
        public void run() {
          boolean result = ((langCode == null) || langCode.equals(currentLang))
            ? api.updateSeries(serieId)
            : api.translateSeries(serieId, langCode);

          String toastMsg = result
            ? (serieName + " " + getString(R.string.menu_context_updated))
            : getString(R.string.messages_db_error_update);

          m_ProgressDialog.dismiss();

          Looper.prepare();
          Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
          listView.post(updateShowView(serieId));
          Looper.loop();
        }
      };
      m_ProgressDialog = ProgressDialog.show(DroidShows.this, getString(R.string.messages_title_updating_series), serie.getName() + "\u2026", true, false);
      updateShowTh = new Thread(updateserierun);
      updateShowTh.start();
    }
  }

  private Runnable changeMessage = new Runnable() {
    public void run() {
      m_ProgressDialog.setMessage(dialogMsg);
      m_ProgressDialog.show();
    }
  };

  public void clearFilter(View v) {
    main.setVisibility(View.INVISIBLE);
    keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
    searchV.setText("");
    findViewById(R.id.search).setVisibility(View.GONE);
    getSeries();
  }

  public void searchForShow(View v) {
    keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
    Intent startSearch = new Intent(DroidShows.this, AddSerie.class);
    startSearch.putExtra(SearchManager.QUERY, searchV.getText().toString());
    startSearch.setAction(Intent.ACTION_SEARCH);
    startActivity(startSearch);
  }

  public void updateAllSeriesDialog() {
    String updateMessageAD = getString(R.string.dialog_update_series);
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DroidShows.this)
      .setTitle(R.string.messages_title_update_series)
      .setMessage(updateMessageAD)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setCancelable(false)
      .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          updateAllSeries(showArchive);
          return;
        }
      })
      .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          return;
        }
      });
    alertDialog.show();
  }

  public void updateAllSeries(final int showArchive) {
    if (!NetworkUtils.isNetworkAvailable(DroidShows.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else if ((m_ProgressDialog == null) || !m_ProgressDialog.isShowing()) {
      if (!connectAPI()) return;
      if (isUpdating)    return;

      isUpdating = true;

      List<TVShowItem> items = db.createTVShowItems((searching() ? 2 : showArchive), false, null);
      int series_count = items.size();
      if (series_count <= 0) return;

      final Runnable updateallseries = new Runnable() {
        public void run() {
          List<String> failedSerieNames = new ArrayList<String>();
          int          serieId;
          String       serieName;
          boolean      result;

          WakeLockMgr.acquire(DroidShows.this);
          WifiLockMgr.acquire(DroidShows.this);

          for (TVShowItem serie : items) {
            serieId   = serie.getSerieId();
            serieName = serie.getName();

            dialogMsg = serieName + "\u2026";
            runOnUiThread(changeMessage);

            result = api.updateSeries(serieId);

            if (!result)
              failedSerieNames.add(serieName);

            m_ProgressDialog.incrementProgressBy(1);
          }

          m_ProgressDialog.dismiss();
          updateShowStats();

          WakeLockMgr.release();
          WifiLockMgr.release();
          isUpdating = false;

          if (!failedSerieNames.isEmpty()) {
            String toastMsg = getString(R.string.messages_db_error_update) + ((failedSerieNames.size() > 1) ? ":\n" : ": ") + TextUtils.join(", ", failedSerieNames);
            Log.d(Constants.LOG_TAG, toastMsg);

            Looper.prepare();
            Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            Looper.loop();
          }
        }
      };

      m_ProgressDialog = new ProgressDialog(DroidShows.this);
      m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      m_ProgressDialog.setTitle(R.string.messages_title_updating_series);
      m_ProgressDialog.setMessage(getString(R.string.messages_adding_serie));
      m_ProgressDialog.setCancelable(false);
      m_ProgressDialog.setMax(series_count);
      m_ProgressDialog.setProgress(0);
      m_ProgressDialog.show();

      updateAllShowsTh = new Thread(updateallseries);
      updateAllShowsTh.start();
    }
  }

  private void getSeries() {
    getSeries(showArchive, filterNetworks);
  }

  private void getSeries(int showArchive) {
    getSeries(showArchive, filterNetworks);
  }

  private void getSeries(int showArchive, boolean filterNetworks) {
    main.setVisibility(View.INVISIBLE);
    cancelAsyncInfo();
    try {
      List<TVShowItem> items = logMode
        ? db.getLog()
        : db.createTVShowItems(showArchive, filterNetworks, networks);

      series.clear();
      series.addAll(items);

      seriesAdapter.notifyDataSetChanged();

      setTitle(getString(R.string.app_name_short)
          + (!logMode ? (showArchive == 1 ? " - "+ getString(R.string.archive) : "") :
            " - "+ getString(R.string.menu_log)));
      runOnUiThread(updateListView);
    } catch (Exception e) {
      Log.e(Constants.LOG_TAG, "Error populating TVShowItems or no shows added yet");
      e.printStackTrace();
    }
    setFastScroll();
    findViewById(R.id.add_show).setVisibility(!logMode ? View.VISIBLE : View.GONE);
    main.setVisibility(View.VISIBLE);
    startAsyncInfo();
  }

  public void getNextLogged() {
    List<TVShowItem> episodes = db.getLog(series.size());
    for (int i = 0; i < episodes.size(); i++)
      series.add(episodes.get(i));
    seriesAdapter.notifyDataSetChanged();
    listView.gettingNextLogged = false;
  }

  public static Runnable updateListView = new Runnable() {
    public void run() {
      seriesAdapter.notifyDataSetChanged();
      if (!logMode) seriesAdapter.sort(showsComperator);
      if (seriesAdapter.isFiltered)
        seriesAdapter.getFilter().filter(searchV.getText());
    }
  };

  private static Comparator<TVShowItem> showsComperator = new Comparator<TVShowItem>() {
    public int compare(TVShowItem object1, TVShowItem object2) {
      if (object1.getPinned() && !object2.getPinned())
        return -1;
      else if (object2.getPinned() && !object1.getPinned())
        return 1;

      switch(sortOption) {
        case SORT_BY_UNSEEN_COUNT: {
          int unwatchedAired1 = object1.getUnwatchedAired();
          int unwatchedAired2 = object2.getUnwatchedAired();
          int compared;

          compared = ((Integer) unwatchedAired2).compareTo(unwatchedAired1); // descending

          if (compared == 0) {
            // counts of unseen episodes are equal.
            // perform a secondary sort that is ordered by the next air date (ascending: closest to air are shown first)

            Date nextAir1 = object1.getNextAir();
            Date nextAir2 = object2.getNextAir();

            if ((nextAir1 == null) && (nextAir2 == null))
              compared = 0;
            else if (nextAir1 == null)
              compared = 1;
            else if (nextAir2 == null)
              compared = -1;
            else
              compared = nextAir1.compareTo(nextAir2); // ascending

            if (compared == 0) {
              // dates for airing of next episode are equal.
              // perform a third (final) sort that is ordered by the name of the show (ascending: alphabetic order)

              compared = object1.getName().compareToIgnoreCase(object2.getName());
            }
          }

          return compared;
        }

        case SORT_BY_NEXT_UNSEEN_DATE: {
          int compared;

          Date nextAir1 = object1.getNextAir();
          Date nextAir2 = object2.getNextAir();

          if ((nextAir1 == null) && (nextAir2 == null))
            compared = 0;
          else if (nextAir1 == null)
            compared = 1;
          else if (nextAir2 == null)
            compared = -1;
          else
            compared = nextAir1.compareTo(nextAir2); // ascending

          if (compared == 0) {
            // dates for airing of next episode are equal.
            // perform a secondary (final) sort that is ordered by the name of the show (ascending: alphabetic order)

            compared = object1.getName().compareToIgnoreCase(object2.getName());
          }

          return compared;
        }

        case SORT_BY_LAST_UNSEEN_DATE: {
          Date unwatchedLastAired1 = object1.getUnwatchedLastAired();
          Date unwatchedLastAired2 = object2.getUnwatchedLastAired();
          int compared;

          if ((unwatchedLastAired1 == null) && (unwatchedLastAired2 == null))
            compared = 0;
          else if (unwatchedLastAired1 == null)
            compared = 1;
          else if (unwatchedLastAired2 == null)
            compared = -1;
          else
            compared = unwatchedLastAired2.compareTo(unwatchedLastAired1); // descending

          if (compared == 0) {
            // dates of most recently aired unwatched episode are equal.
            // perform a secondary sort that is ordered by the next air date (ascending: closest to air are shown first)

            Date nextAir1 = object1.getNextAir();
            Date nextAir2 = object2.getNextAir();

            if ((nextAir1 == null) && (nextAir2 == null))
              compared = 0;
            else if (nextAir1 == null)
              compared = 1;
            else if (nextAir2 == null)
              compared = -1;
            else
              compared = nextAir1.compareTo(nextAir2); // ascending

            if (compared == 0) {
              // dates for airing of next episode are equal.
              // perform a third (final) sort that is ordered by the name of the show (ascending: alphabetic order)

              compared = object1.getName().compareToIgnoreCase(object2.getName());
            }
          }

          return compared;
        }

        case SORT_BY_NAME:
        default: {
          return object1.getName().compareToIgnoreCase(object2.getName());
        }
      }
    }
  };

  @Override
  public void onPause() {
    super.onPause();

    // Preferences

    SharedPreferences.Editor ed = sharedPrefs.edit();

    // ==============
    // Hidden
    // ==============

    ed.putString(BACKUP_FOLDER_PREF_NAME, backupFolder);
    ed.putString(LAST_STATS_UPDATE_NAME, lastStatsUpdateCurrent);
    ed.putString(LAST_STATS_UPDATE_ARCHIVE_NAME, lastStatsUpdateArchive);
    ed.putBoolean(FILTER_NETWORKS_NAME, filterNetworks);
    ed.putString(NETWORKS_NAME, networks.toString());

    // ==============
    // ActionBar menu
    // ==============

    ed.putBoolean(EXCLUDE_SEEN_PREF_NAME, excludeSeen);
    ed.putInt(SORT_PREF_NAME, sortOption);

    // ==============
    // Options Dialog
    // ==============

    ed.putBoolean(AUTO_BACKUP_PREF_NAME, autoBackup);
    ed.putBoolean(BACKUP_VERSIONING_PREF_NAME, backupVersioning);
    ed.putBoolean(ENABLE_PULL_TO_UPDATE_PREF_NAME, enablePullToUpdate);
    ed.putBoolean(SHOW_ICONS, showIcons);
    ed.putBoolean(SHOW_NEXT_AIRING, showNextAiring);
    ed.putBoolean(MARK_FROM_LAST_WATCHED, markFromLastWatched);
    ed.putBoolean(INCLUDE_SPECIALS_NAME, includeSpecialsOption);
    ed.putBoolean(FULL_LINE_CHECK_NAME, fullLineCheckOption);
    ed.putBoolean(SWITCH_SWIPE_DIRECTION, switchSwipeDirection);
    ed.putString(LANGUAGE_CODE_NAME, langCode);

    // Preferences: commit changes

    ed.commit();
  }

  @Override
  protected void onStop() {
    if (!isUpdating) {
      WakeLockMgr.release();
      WifiLockMgr.release();

      if (autoBackup)
        backup(true);
    }

    super.onStop();
  }

  @Override
  public void onRestart() {
    super.onRestart();
    if (!logMode) {
      if (backFromSeasonSerieId > 0) {
        listView.post(updateShowView(backFromSeasonSerieId));
        backFromSeasonSerieId = 0;
      }
    } else {
      if (removeEpisodeIdFromLog > 0) {
        for (int i = 0; i < series.size(); i++) {
          if (series.get(i).getEpisodeId() == removeEpisodeIdFromLog) {
            series.remove(i);
            listView.post(updateListView);
          }
        }
        removeEpisodeIdFromLog = 0;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (searchV.getText().length() > 0) {
      findViewById(R.id.search).setVisibility(View.VISIBLE);
      listView.requestFocus();
    }
    if (!logMode) {
      startAsyncInfo();
    }
  }

  private void startAsyncInfo() {
    if (!isUpdating && ((asyncInfo == null) || (asyncInfo.getStatus() != AsyncTask.Status.RUNNING))) {
      String newToday        = DateFormats.NORMALIZE_DATE.format(Calendar.getInstance().getTime());
      String lastStatsUpdate = (showArchive == 0) ? lastStatsUpdateCurrent : lastStatsUpdateArchive;
      if (!lastStatsUpdate.equals(newToday)) {
        Log.d(Constants.LOG_TAG, "AsyncInfo RUNNING | Today = " + newToday);

        isUpdating = true;
        WakeLockMgr.acquire(DroidShows.this);
        WifiLockMgr.acquire(DroidShows.this);

        asyncInfo = new AsyncInfo();
        asyncInfo.execute(newToday);
      }
    }
  }

  private void cancelAsyncInfo() {
    if (asyncInfo != null) {
      asyncInfo.cancel(true);
      asyncInfo = null;

      WakeLockMgr.release();
      WifiLockMgr.release();
    }
  }

  private static class AsyncInfo extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
      try {
        String newToday    = params[0];
        int showArchiveTmp = showArchive;

        db.updateToday(newToday);

        for (int i = 0; i < series.size(); i++) {
          TVShowItem serie = series.get(i);
          if (isCancelled()) return null;
          int serieId        = serie.getSerieId();
          int unwatched      = db.getEpsUnwatched(serieId);
          int unwatchedAired = db.getEpsUnwatchedAired(serieId);
          if ((unwatched != serie.getUnwatched()) || (unwatchedAired != serie.getUnwatchedAired())) {
            if (isCancelled()) return null;
            serie.setUnwatched(unwatched);
            serie.setUnwatchedAired(unwatchedAired);
            String nextEpisodeString = null;
            if (showNextAiring && (unwatchedAired > 0)) {
              NextEpisode nextEpisode = db.getNextEpisode(serieId);
              nextEpisodeString       = db.getNextEpisodeString(nextEpisode, true);
              serie.setNextEpisode(nextEpisodeString);
            }
            if (isCancelled()) return null;
            db.updateShowStats(serieId, unwatched, unwatchedAired, nextEpisodeString);
          }
        }
        if (isCancelled()) return null;
        listView.post(updateListView);
        if (showArchiveTmp == 0 || showArchiveTmp == 2)
          lastStatsUpdateCurrent = newToday;
        if (showArchiveTmp > 0)
          lastStatsUpdateArchive = newToday;
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);

      DroidShows.isUpdating = false;
    }

    @Override
    protected void onCancelled(Void result) {
      this.onPostExecute(result);
      super.onCancelled();
    }
  }

  @Override
  public boolean onSearchRequested() {
    if (logMode)
      return false;
    if (findViewById(R.id.search).getVisibility() != View.VISIBLE) {
      findViewById(R.id.search).setVisibility(View.VISIBLE);
      getSeries(2, false);  // 2 = archive and current shows, false = don't filter networks
    }
    searchV.requestFocus();
    searchV.selectAll();
    keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
    return true;
  }

  @Override
  public void onBackPressed() {
    if (searching())
      clearFilter(null);
    else {
      if (logMode)
        toggleLogMode();
      else if (showArchive == 1)
        toggleArchive();
      else
        super.onBackPressed();
      if (spinner != null)
        spinner.setSelection(showArchive);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putBoolean("searching", searching());
    outState.putInt("showArchive", showArchive);
    if (m_ProgressDialog != null)
      m_ProgressDialog.dismiss();
    super.onSaveInstanceState(outState);
  }

  public String translateStatus(String statusValue) {
    if (statusValue.equalsIgnoreCase("Continuing")) {
      return getString(R.string.showstatus_continuing);
    } else if (statusValue.equalsIgnoreCase("Ended")) {
      return getString(R.string.showstatus_ended);
    } else {
      return statusValue.toLowerCase();
    }
  }

  private boolean searching() {
    return (seriesAdapter.isFiltered || findViewById(R.id.search).getVisibility() == View.VISIBLE);
  }

  public class SeriesAdapter extends ArrayAdapter<TVShowItem> {
    private List<TVShowItem> items;
    private ShowsFilter filter;
    private boolean isFiltered;
    private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    private int iconListPosition;
    private ColorStateList textViewColors = new TextView(getContext()).getTextColors();

    private final String strEpAired = getString(R.string.messages_ep_aired);
    private final String strNewEp = getString(R.string.messages_new_episode);
    private final String strNewEps = getString(R.string.messages_new_episodes);
    private final String strNextEp = getString(R.string.messages_next_episode);
    private final String strLastEp = getString(R.string.messages_last_episode);
    private final String strNextAiring = getString(R.string.messages_next_airing);
    private final String strNoNewEps = getString(R.string.messages_no_new_eps);
    private final String strOf = getString(R.string.messages_of);
    private final String strOn = getString(R.string.messages_on);
    private final String strSeason = getString(R.string.messages_season);
    private final String strSeasons = getString(R.string.messages_seasons);
    private final String strToBeAired = getString(R.string.messages_to_be_aired);
    private final String strToBeAiredPl = getString(R.string.messages_to_be_aired_pl);

    public SeriesAdapter(Context context, int textViewResourceId, List<TVShowItem> series) {
      super(context, textViewResourceId, series);
      items = series;
      isFiltered = false;
    }

    @Override
    public int getCount() {
      return items.size();
    }

    @Override
    public Filter getFilter() {
      if (filter == null)
        filter = new ShowsFilter();
      return filter;
    }

    @Override
    public TVShowItem getItem(int position) {
      return items.get(position);
    }

    public void setItem(int location, TVShowItem serie) {
      items.set(location, serie);
      notifyDataSetChanged();
    }

    private class ShowsFilter extends Filter {
      @SuppressLint("DefaultLocale")
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint == null || constraint.length() == 0) {
          results.count = series.size();
          results.values = series;
          isFiltered = false;
        } else {
          constraint = constraint.toString().toLowerCase();
          ArrayList<TVShowItem> filteredSeries = new ArrayList<TVShowItem>();
          for (TVShowItem serie : series) {
            if (serie.getName().toLowerCase().contains(constraint))
              filteredSeries.add(serie);
          }
          results.count = filteredSeries.size();
          results.values = filteredSeries;
          isFiltered = true;
        }
        return results;
      }

      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        items = (List<TVShowItem>) results.values;
        notifyDataSetChanged();
      }
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      TVShowItem serie = items.get(position);
      ViewHolder holder;
      if (!logMode &&  excludeSeen && !isFiltered && serie != lastSerie && serie.getUnwatchedAired() == 0 && (serie.getNextAir() == null || serie.getNextAir().after(Calendar.getInstance().getTime()))) {
        if (convertView == null || convertView.isEnabled()) {
          convertView = vi.inflate(R.layout.row_excluded, parent, false);
          convertView.setEnabled(false);
        }
        return convertView;
      } else if (convertView == null || !convertView.isEnabled()) {
        convertView    = vi.inflate(R.layout.row, parent, false);
        holder         = new ViewHolder();
        holder.sn      = (TextView) convertView.findViewById(R.id.seriename);
        holder.si      = (TextView) convertView.findViewById(R.id.serieinfo);
        holder.sle     = (TextView) convertView.findViewById(R.id.serielastepisode);
        holder.sne     = (TextView) convertView.findViewById(R.id.serienextepisode);
        holder.icon    = (ImageView) convertView.findViewById(R.id.serieicon);
        holder.context = (ImageView) convertView.findViewById(R.id.seriecontext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
          holder.context.setImageResource(R.drawable.context_material);
        convertView.setEnabled(true);
        convertView.setTag(holder);
        holder.icon.setOnTouchListener(iconTouchListener);
      } else {
        holder = (ViewHolder) convertView.getTag();
        holder.icon.setOnClickListener(null);
      }
      if (!logMode) {
        int nunwatched = serie.getUnwatched();
        int nunwatchedAired = serie.getUnwatchedAired();
        String ended = (serie.getShowStatus().equalsIgnoreCase("Ended") ? " \u2020" : "");
        boolean sneAired = false;
        if (holder.sn != null) {
          holder.sn.setText((serie.getPinned() ? "\u2022 " : "") + serie.getName() + ended);
          holder.sn.setEnabled(!searching() || !serie.getArchived());
          holder.sn.setVisibility(View.VISIBLE);
        }
        if (holder.si != null) {
          String siText = "";
          int sNumber = serie.getSNumber();
          if (sNumber == 1) {
            siText = sNumber +" "+ strSeason;
          } else {
            siText = sNumber +" "+ strSeasons;
          }
          String unwatched = "";
          if (nunwatched == 0) {
            unwatched = strNoNewEps;
            if (!serie.getShowStatus().equalsIgnoreCase("null"))
              unwatched += " ("+ translateStatus(serie.getShowStatus()) +")";
            holder.si.setEnabled(false);
          } else {
            unwatched = nunwatched +" "+ (nunwatched > 1 ? strNewEps : strNewEp) +" ";
            if (nunwatchedAired > 0) {
              unwatched = (nunwatchedAired == nunwatched ? "" : nunwatchedAired +" "+ strOf +" ") + unwatched + strEpAired + (nunwatchedAired == nunwatched && ended.isEmpty() ? " \u00b7" : "");
              holder.si.setEnabled(true);
            } else {
              unwatched += (nunwatched > 1 ? strToBeAiredPl : strToBeAired);
              holder.si.setEnabled(false);
            }
          }
          holder.si.setText(siText +" | "+ unwatched);
          holder.si.setVisibility(View.VISIBLE);
        }
        if (holder.sle != null) {
          String sleString = serie.getUnwatchedLastEpisode();
          if ((sleString != null) && !sleString.isEmpty()) {
            holder.sle.setText(
              sleString
                .replace("[ne]", strLastEp)
                .replace("[na]", "")
                .replace("[on]", strOn)
            );
            holder.sle.setEnabled(true);
            holder.sle.setVisibility(View.VISIBLE);
          } else {
            holder.sle.setVisibility(View.GONE);
          }
        }
        if (holder.sne != null) {
          String sneString = serie.getNextEpisode();
          if ((nunwatched > 0) && (sneString != null) && !sneString.isEmpty()) {
            if (sneString.contains("[na]")) {
              sneAired = false;
            }
            else {
              if (!sneAired) {
                sneAired = (nunwatchedAired > 0);
              }

              if (!sneAired) {
                Date nextAirDate = serie.getNextAir();

                if ((nextAirDate != null)) {
                  sneAired = (nextAirDate.compareTo(Calendar.getInstance().getTime()) <= 0);
                }
              }
            }

            holder.sne.setText(
              sneString
                .replace("[ne]", (sneAired ? strNextEp : strNextAiring))
                .replace("[na]", strNextAiring)
                .replace("[on]", strOn)
            );
            holder.sne.setEnabled(sneAired);
            holder.sne.setVisibility(View.VISIBLE);
          } else {
            holder.sne.setVisibility(View.GONE);
          }
        }
        if ((holder.sle != null) && (holder.sne != null) && (holder.sle.getVisibility() == View.VISIBLE) && (holder.sne.getVisibility() == View.VISIBLE) && sneAired) {
          // next episode (sne) is older than last episode (sle), because the user has a backlog of unwatched episodes.
          // swap the text these 2x fields, so the presentation makes more sense for the user.
          // ie: 2x of the 3x will always be shown in the following order:
          //   1) Next unseen (sne: reverse position)
          //   2) Last unseen
          //   3) Next airing (sne: default position)

          CharSequence sleString = holder.sle.getText();
          CharSequence sneString = holder.sne.getText();

          holder.sle.setText(sneString);
          holder.sne.setText(sleString);
        }
        if (holder.icon != null) {
          try {
            if (!showIcons) throw new Exception("");

            Bitmap icon = serie.getDIcon();
            if (icon != null) {
              holder.icon.setImageBitmap(icon);
            }
            else {
              String iconPath = serie.getIcon();
              if ((iconPath != null) && !iconPath.isEmpty()) {
                icon = BitmapFactory.decodeFile(iconPath);
                serie.setDIcon(icon);
                holder.icon.setImageBitmap(icon);
              }
              else {
                holder.icon.setImageBitmap(defaultIcon);
              }
            }
            holder.icon.setVisibility(View.VISIBLE);
          }
          catch(Exception e) {
            holder.icon.setVisibility(View.GONE);
          }
        }
      } else {
        if (holder.sn != null) {
          holder.sn.setText(serie.getName());
          holder.sn.setTextColor(textViewColors);
          holder.sn.setVisibility(View.VISIBLE);
        }
        if (holder.si != null) {
          holder.si.setEnabled(true);
          holder.si.setText(serie.getEpisodeName());
          holder.si.setVisibility(View.VISIBLE);
        }
        if (holder.sle != null) {
          holder.sle.setVisibility(View.GONE);
        }
        if (holder.sne != null) {
          holder.sne.setEnabled(true);
          holder.sne.setText(serie.getEpisodeSeen());
          holder.sne.setVisibility(View.VISIBLE);
        }
        if (holder.icon != null) {
          try {
            if (!showIcons) throw new Exception("");

            Bitmap icon = serie.getDIcon();
            if (icon != null) {
              holder.icon.setImageBitmap(icon);
            }
            else {
              String iconPath = serie.getIcon();
              if ((iconPath != null) && !iconPath.isEmpty()) {
                icon = BitmapFactory.decodeFile(iconPath);
                serie.setDIcon(icon);
                holder.icon.setImageBitmap(icon);
              }
              else {
                holder.icon.setImageBitmap(defaultIcon);
              }
            }
            holder.icon.setVisibility(View.VISIBLE);
          }
          catch(Exception e) {
            holder.icon.setVisibility(View.GONE);
          }
        }
      }
      return convertView;
    }

    private OnTouchListener iconTouchListener = new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        iconListPosition = listView.getPositionForView(v);
        iconGestureDetector.onTouchEvent(event);
        return true;
      }
    };

    private final SimpleOnGestureListener iconGestureListener = new SimpleOnGestureListener() {
      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {
        keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
        if (!logMode)
          episodeDetails(iconListPosition);
        else
          serieSeasons(iconListPosition);
        return true;
      }

      @Override
      public boolean onDoubleTap(MotionEvent e) {
        keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
        showDetails(seriesAdapter.getItem(iconListPosition).getSerieId());
        return true;
      }

      @Override
      public void onLongPress(MotionEvent e) {
        keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
        String[] extResources = seriesAdapter.getItem(iconListPosition).getExtResources().trim().split("\\n");
        boolean foundResources = false;
        for (int i = 0; i < extResources.length; i++) {
          if (extResources[i].startsWith("*")) {
            browseExtResource(extResources[i]);
            foundResources = true;
          }
        }
        if (!foundResources)
          extResources(seriesAdapter.getItem(iconListPosition).getExtResources(), iconListPosition);
      }
    };

    private GestureDetector iconGestureDetector = new GestureDetector(getApplicationContext(), iconGestureListener);
  }

  static class ViewHolder
  {
    TextView  sn;
    TextView  si;
    TextView  sle;
    TextView  sne;
    ImageView icon;
    ImageView context;
  }
}
