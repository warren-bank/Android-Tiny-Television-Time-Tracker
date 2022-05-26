package nl.asymmetrics.droidshows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.thetvdb.TheTVDB;
import nl.asymmetrics.droidshows.thetvdb.model.Serie;
import nl.asymmetrics.droidshows.thetvdb.model.TVShowItem;
import nl.asymmetrics.droidshows.thetvdb.utils.PosterThumb;
import nl.asymmetrics.droidshows.ui.AddSerie;
import nl.asymmetrics.droidshows.ui.BounceListView;
import nl.asymmetrics.droidshows.ui.IconView;
import nl.asymmetrics.droidshows.ui.SerieSeasons;
import nl.asymmetrics.droidshows.ui.ViewEpisode;
import nl.asymmetrics.droidshows.ui.ViewSerie;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import nl.asymmetrics.droidshows.utils.Update;
import nl.asymmetrics.droidshows.utils.Utils;
import nl.asymmetrics.droidshows.utils.SQLiteStore.NextEpisode;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ToggleButton;

public class DroidShows extends ListActivity
{
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
  private static ProgressDialog updateAllSeriesPD = null;
  public static SeriesAdapter seriesAdapter;
  private static BounceListView listView = null;
  private static String backFromSeasonSerieId;
  private static TheTVDB theTVDB;
  private Update updateDS;

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

  private static final String PINNED_SHOWS_NAME = "pinned_shows";
  private static List<String> pinnedShows = new ArrayList<String>();

  // ==============
  // ActionBar menu
  // ==============

  // Exclude seen?
  private static final String EXCLUDE_SEEN_PREF_NAME = "exclude_seen";
  private static boolean excludeSeen;

  // Sort shows...
  private static final String SORT_PREF_NAME = "sort";
  private static final int SORT_BY_NAME = 0;
  private static final int SORT_BY_UNSEEN_COUNT = 1;
  private static final int SORT_BY_UNSEEN_DATE = 2;
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

  // Update shows' latest season only?
  private static final String LATEST_SEASON_PREF_NAME = "last_season";
  private static final int UPDATE_ALL_SEASONS = 0;
  private static final int UPDATE_LATEST_SEASON_ONLY = 1;
  private static int latestSeasonOption;

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

  // Use TheTVDB mirror (try this if updates are too slow)?
  private static final String USE_MIRROR = "use_mirror";
  public static boolean useMirror;

  // Synopsis language...
  private static final String LANGUAGE_CODE_NAME = "language";
  public static String langCode;

  // -----

  private static Bitmap defaultIcon = null;
  public static Thread deleteTh = null;
  public static Thread updateShowTh = null;
  public static Thread updateAllShowsTh = null;
  private String dialogMsg;
  public static SQLiteStore db;
  public static List<TVShowItem> series;
  private static List<String[]> undo = new ArrayList<String[]>();
  private SwipeDetect swipeDetect = new SwipeDetect();
  private static AsyncInfo asyncInfo;
  private static EditText searchV;
  private InputMethodManager keyboard;
  private int padding;
  public static int showArchive;
  private Vibrator vib = null;
  private TVShowItem lastSerie;
  private static View main;
  public static boolean logMode = false;
  public static String removeEpisodeFromLog = "";
  private File[] dirList;
  private String[] dirNamesList;
  private Spinner spinner = null;

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
    db = SQLiteStore.getInstance(this);

    // Preferences

    sharedPrefs = getSharedPreferences(PREF_NAME, 0);

    // ==============
    // Hidden
    // ==============

    backupFolder           = sharedPrefs.getString(BACKUP_FOLDER_PREF_NAME, Environment.getExternalStorageDirectory() +"/DroidShows");
    lastStatsUpdateCurrent = sharedPrefs.getString(LAST_STATS_UPDATE_NAME, "");
    lastStatsUpdateArchive = sharedPrefs.getString(LAST_STATS_UPDATE_ARCHIVE_NAME, "");
    filterNetworks         = sharedPrefs.getBoolean(FILTER_NETWORKS_NAME, false);
    String networksStr     = sharedPrefs.getString(NETWORKS_NAME, "");
    String pinnedShowsStr  = sharedPrefs.getString(PINNED_SHOWS_NAME, "");

    if (!pinnedShowsStr.isEmpty())
      pinnedShows = new ArrayList<String>(Arrays.asList(pinnedShowsStr.replace("[", "").replace("]", "").split(", ")));

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
    latestSeasonOption     = sharedPrefs.getInt(LATEST_SEASON_PREF_NAME, UPDATE_LATEST_SEASON_ONLY);
    showIcons              = sharedPrefs.getBoolean(SHOW_ICONS, true);
    showNextAiring         = sharedPrefs.getBoolean(SHOW_NEXT_AIRING, false);
    markFromLastWatched    = sharedPrefs.getBoolean(MARK_FROM_LAST_WATCHED, false);
    includeSpecialsOption  = sharedPrefs.getBoolean(INCLUDE_SPECIALS_NAME, false);
    fullLineCheckOption    = sharedPrefs.getBoolean(FULL_LINE_CHECK_NAME, false);
    switchSwipeDirection   = sharedPrefs.getBoolean(SWITCH_SWIPE_DIRECTION, false);
    useMirror              = sharedPrefs.getBoolean(USE_MIRROR, false);
    langCode               = sharedPrefs.getString(LANGUAGE_CODE_NAME, getString(R.string.lang_code));

    // -----

    defaultIcon = PosterThumb.decodeSampledBitmapFromResource(this, R.drawable.noposter, PosterThumb.getIconWidthPx(this));

    updateDatabase();

    if (!networksStr.isEmpty())
      networks = new ArrayList<String>(Arrays.asList(networksStr.replace("[", "").replace("]", "").split(", ")));
    series = new ArrayList<TVShowItem>();
    seriesAdapter = new SeriesAdapter(this, R.layout.row, series);
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

  private void updateDatabase() {
    updateDS = new Update(db);
    Log.d(SQLiteStore.TAG, "Database update routine");
    if (updateDS.needsUpdate()) {
      Log.d(SQLiteStore.TAG, "Database needs update");
      backup(false, backupFolder);
      if (updateDS.updateDroidShows()) {
        db.updateShowStats();
        Log.d(SQLiteStore.TAG, "Database updated");
      } else {
        String error = getString(R.string.messages_error_dbupdate);
        Log.e(SQLiteStore.TAG, error);
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
      }
    }
  }

  private void setFastScroll() {
    listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    listView.setVerticalScrollBarEnabled(!excludeSeen || logMode);
/*    listView.setFastScrollEnabled(!excludeSeen || logMode);
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
*/  }

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
    spinner = new Spinner(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
      spinner.setPopupBackgroundResource(R.drawable.menu_dropdown_panel);
    spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
      new String[] {
        getString(R.string.layout_app_name),
        getString(R.string.archive),
        getString(R.string.menu_log),
      }));
    listView.postDelayed(new Runnable() {
      public void run() {
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            logMode = position == 2;
            showArchive = (position == 2 ? showArchive : position);
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
        db.close();
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
    removeEpisodeFromLog = "";
    listView.setSelection(0);
  }

  private void sortDialog() {
    if (m_AlertDlg != null) {
      m_AlertDlg.dismiss();
    }
    m_AlertDlg = new AlertDialog.Builder(this)
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
    final View filterV = View.inflate(this, R.layout.alert_filter, null);
    ((CheckBox) filterV.findViewById(R.id.exclude_seen)).setChecked(excludeSeen);
    List<String> allNetworks = db.getNetworks();
    final LinearLayout networksFilterV = (LinearLayout) filterV.findViewById(R.id.networks_filter);
    for (String network : allNetworks) {
      CheckBox networkCheckBox = new CheckBox(this);
      networkCheckBox.setText(network);
      if (!networks.isEmpty())
        networkCheckBox.setChecked(networks.contains(network));
      networksFilterV.addView(networkCheckBox);
    }
    ToggleButton networksFilter = (ToggleButton) filterV.findViewById(R.id.toggle_networks_filter);
    networksFilter.setChecked(filterNetworks);
    toggleNetworksFilter(networksFilter);
    m_AlertDlg = new AlertDialog.Builder(this)
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
    View about = View.inflate(this, R.layout.alert_about, null);
    TextView changelog = (TextView) about.findViewById(R.id.copyright);
    try {
      changelog.setText(getString(R.string.copyright)
        .replace("{v}", getPackageManager().getPackageInfo(getPackageName(), 0).versionName)
        .replace("{y}", Calendar.getInstance().get(Calendar.YEAR) +""));
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
    ((CheckBox) about.findViewById(R.id.latest_season)).setChecked(latestSeasonOption == UPDATE_LATEST_SEASON_ONLY);
    ((CheckBox) about.findViewById(R.id.show_icons)).setChecked(showIcons);
    ((CheckBox) about.findViewById(R.id.show_next_airing)).setChecked(showNextAiring);
    ((CheckBox) about.findViewById(R.id.mark_from_last_watched)).setChecked(markFromLastWatched);
    ((CheckBox) about.findViewById(R.id.include_specials)).setChecked(includeSpecialsOption);
    ((CheckBox) about.findViewById(R.id.full_line_check)).setChecked(fullLineCheckOption);
    ((CheckBox) about.findViewById(R.id.switch_swipe_direction)).setChecked(switchSwipeDirection);
    ((CheckBox) about.findViewById(R.id.use_mirror)).setChecked(useMirror);
    ((TextView) about.findViewById(R.id.change_language)).setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");

    // -----

    m_AlertDlg = new AlertDialog.Builder(this)
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
      case R.id.latest_season:
        latestSeasonOption ^= 1;
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
      case R.id.use_mirror:
        useMirror ^= true;
        break;
      case R.id.change_language:
        AlertDialog.Builder changeLang = new AlertDialog.Builder(this);
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
      backup(auto, backupFolder);
    } else {
      File folder = new File(backupFolder);
      if (!folder.isDirectory())
        folder.mkdir();
      filePicker(backupFolder, false);
    }
  }

  private void restore() {
    filePicker(backupFolder, true);
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
        dirNamesList[i + showParent] += " ("+ SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT).format(tempDirList[i].lastModified()) +")";
    }
    AlertDialog.Builder filePicker = new AlertDialog.Builder(this)
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
    File source = new File(getApplicationInfo().dataDir +"/databases/DroidShows.db");
    File destination = new File(backupFolder, "DroidShows.db");
    if (auto && (!autoBackup ||
        new SimpleDateFormat("yyyy-MM-dd")
          .format(destination.lastModified()).equals(lastStatsUpdateCurrent) ||
        source.lastModified() == destination.lastModified()))
      return;
    if (backupVersioning && destination.exists()) {
      File previous0 = new File(backupFolder, "DroidShows.db0");
      if (previous0.exists()) {
        File previous1 = new File(backupFolder, "DroidShows.db1");
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
      Toast.makeText(getApplicationContext(), getString(toastTxt) + " ("+ backupFolder +")", Toast.LENGTH_LONG).show();
      asyncInfo = new AsyncInfo();
      asyncInfo.execute();
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
    Runnable restoreRunnable = new Runnable() {
      public void run() {
        Looper.prepare();
        String toastTxt = getString(R.string.dialog_restore_done);
        File source = new File(backupFile);
        if (source.exists()) {
          File destination = new File(getApplicationInfo().dataDir +"/databases", "DroidShows.db");
          try {
            copy(source, destination);
            updateDatabase();
            File thumbs[] = new File(getApplicationContext().getFilesDir().getAbsolutePath() +"/thumbs/banners/posters").listFiles();
            if (thumbs != null)
              for (File thumb : thumbs)
                thumb.delete();
            for (File file : new File(getApplicationInfo().dataDir +"/databases").listFiles())
              if (!file.getName().equalsIgnoreCase("DroidShows.db")) file.delete();
            updateRestoredDB();
            undo.clear();
            toastTxt += " ("+ source.getPath() +")";
          } catch (IOException e) {
            toastTxt = getString(R.string.dialog_restore_failed);
            e.printStackTrace();
          }
        } else {
          toastTxt = getString(R.string.dialog_restore_notfound);
        }
        Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_LONG).show();
        Looper.loop();
      }

      private void updateRestoredDB() {
        runOnUiThread(new Runnable() {
          public void run() {
            if (!BuildConfig.DEBUG)
              updateAllSeries(2);  // 2 = update archive and current shows
          }
        });
      }
    };
    Thread restoreThread = new Thread(restoreRunnable);
    restoreThread.start();
  }

  private void copy(File source, File destination) throws IOException {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      if (asyncInfo != null)
        asyncInfo.cancel(true);
      db.close();
      FileChannel sourceCh = null, destinationCh = null;
      try {
        sourceCh = new FileInputStream(source).getChannel();
        if (destination.exists()) destination.delete();
        destination.createNewFile();
        destinationCh = new FileOutputStream(destination).getChannel();
        destinationCh.transferFrom(sourceCh, 0, sourceCh.size());
        destination.setLastModified(source.lastModified());
      } finally {
        if (sourceCh != null) {
          sourceCh.close();
        }
        if (destinationCh != null) {
          destinationCh.close();
        }
      }
      db.openDataBase();
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
        if (serie.getPassiveStatus())
          menu.findItem(TOGGLE_ARCHIVED_CONTEXT).setTitle(R.string.menu_unarchive);
        if (pinnedShows.contains(serie.getSerieId()))
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
        CharSequence[] langList = Arrays.copyOfRange(getResources().getStringArray(R.array.languages), 1, getResources().getStringArray(R.array.languages).length);
        AlertDialog.Builder changeLang = new AlertDialog.Builder(this);
        changeLang.setTitle(R.string.dialog_change_language)
          .setItems(langList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
              String langCode = getResources().getStringArray(R.array.langcodes)[item + 1];
              updateSerie(serie, langCode, info.position);
            }
          })
          .show();
        return true;
      case TOGGLE_ARCHIVED_CONTEXT :
        asyncInfo.cancel(true);
        boolean passiveStatus = serie.getPassiveStatus();
        db.updateSerieStatus(serie.getSerieId(), (passiveStatus ? 0 : 1));
        String message = serie.getName() +" "+
          (passiveStatus ? getString(R.string.messages_context_unarchived) : getString(R.string.messages_context_archived));
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        if (!searching())
          series.remove(serie);
        else
          serie.setPassiveStatus(!passiveStatus);
        listView.post(updateListView);
        asyncInfo = new AsyncInfo();
        asyncInfo.execute();
        return true;
      case PIN_CONTEXT :
        String serieId = serie.getSerieId();
        if (pinnedShows.contains(serieId))
          pinnedShows.remove(serieId);
        else
          pinnedShows.add(serieId);
        listView.post(updateListView);
        return true;
      case DELETE_CONTEXT :
        asyncInfo.cancel(true);
        final Runnable deleteserie = new Runnable() {
          public void run() {
            String sname = serie.getName();
            String toastMsg = getString(R.string.messages_deleted);
            if (!db.deleteSerie(serie.getSerieId()))
              toastMsg = "Database error while deleting show";
            series.remove(series.indexOf(serie));
            listView.post(updateListView);
            Looper.prepare();  // Threads don't have a message loop
            Toast.makeText(getApplicationContext(), sname +" "+ toastMsg, Toast.LENGTH_LONG).show();
            asyncInfo = new AsyncInfo();
            asyncInfo.execute();
            Looper.loop();
          }
        };
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
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
    String serieId = serie.getSerieId();
    String nextEpisode = db.getNextEpisodeId(serieId, true);
    if (!nextEpisode.equals("-1")) {
      String episodeMarked = db.updateUnwatchedEpisode(serieId, nextEpisode);
      Toast.makeText(getApplicationContext(), serie.getName() +" "+ episodeMarked +" "+ getString(R.string.messages_marked_seen), Toast.LENGTH_SHORT).show();
      undo.add(new String[] {serieId, nextEpisode, serie.getName()});
      updateShowView(serie);
    }
  }

  private void markLastEpUnseen() {
    String[] episodeInfo = undo.get(undo.size()-1);
    String serieId = episodeInfo[0];
    String episodeId = episodeInfo[1];
    String serieName = episodeInfo[2];
    String episodeMarked = db.updateUnwatchedEpisode(serieId, episodeId);
    undo.remove(undo.size()-1);
    Toast.makeText(getApplicationContext(), serieName +" "+ episodeMarked +" "+ getString(R.string.messages_marked_unseen), Toast.LENGTH_SHORT).show();
    listView.post(updateShowView(serieId));
  }

  private void serieSeasons(int position) {
    backFromSeasonSerieId = seriesAdapter.getItem(position).getSerieId();
    Intent serieSeasons = new Intent(DroidShows.this, SerieSeasons.class);
    serieSeasons.putExtra("serieId", backFromSeasonSerieId);
    serieSeasons.putExtra("nextEpisode", seriesAdapter.getItem(position).getUnwatched() > 0);
    startActivity(serieSeasons);
  }

  private Runnable updateShowView(final String serieId) {
    Runnable updateView = new Runnable(){
      public void run() {
        for (TVShowItem serie : series) {
          if (serie.getSerieId().equals(serieId)) {
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

  private void showDetails(String serieId) {
    Intent viewSerie = new Intent(DroidShows.this, ViewSerie.class);
    viewSerie.putExtra("serieId", serieId);
    startActivity(viewSerie);
  }

  private void episodeDetails(int position) {
    String serieId = seriesAdapter.getItem(position).getSerieId();
    String episodeId = "-1";
    if (!logMode)
      episodeId = db.getNextEpisodeId(serieId);
    else
      episodeId = seriesAdapter.getItem(position).getEpisodeId();
    if (!episodeId.equals("-1")) {
      backFromSeasonSerieId = serieId;
      Intent viewEpisode = new Intent(DroidShows.this, ViewEpisode.class);
      viewEpisode.putExtra("serieName", seriesAdapter.getItem(position).getName());
      viewEpisode.putExtra("serieId", serieId);
      viewEpisode.putExtra("episodeId", episodeId);
      startActivity(viewEpisode);
    }
  }

  private void Search(String url, String serieName) {
    serieName = serieName.replaceAll(" \\(....\\)", "");
    Intent rt = new Intent(Intent.ACTION_VIEW, Uri.parse(url + serieName));
    rt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(rt);
  }

  private void WikiDetails(String serieName) {
    serieName = serieName.replaceAll(" \\(....\\)", "");
    Intent wiki;
    String wikiApp = null;
      if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia") != null)
        wikiApp = "org.wikipedia";
      else if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia.beta") != null)
        wikiApp = "org.wikipedia.beta";
      if (wikiApp == null) {
        String uri = "https://"+ (langCode.equals("all") ? "" : langCode +".") +"m.wikipedia.org/wiki/index.php?search="+ serieName
          + (langCode.equals("en") ? " (TV series)" : "");
        wiki = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
      } else {
        wiki = new Intent(Intent.ACTION_SEND)
          .putExtra(Intent.EXTRA_TEXT, serieName)
          .setType("text/plain")
          .setPackage(wikiApp);
      }
      wiki.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(wiki);
  }

  private void IMDbDetails(String serieId, String serieName, String episode) {
    String query;
    if (episode != null)
      query = "SELECT imdbId, episodeName FROM episodes WHERE id = '"+ episode +"' AND serieId='"+ serieId +"'";
    else
      query = "SELECT imdbId, serieName FROM series WHERE id = '" + serieId + "'";
    Cursor c = db.Query(query);
    c.moveToFirst();
    if (c != null && c.isFirst()) {
      String imdbId = c.getString(0);
      if (episode != null && imdbId.equals(db.getSerieIMDbId(serieId)))  // Sometimes the given episode's IMDb id is that of the show's
        imdbId = "";  // So we want to search for the episode instead of go to the show's page
      String name = c.getString(1);
      c.close();
      String uri = "imdb:///";
      Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
      if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
        uri = "https://m.imdb.com/";
      if (imdbId.startsWith("tt"))
        uri += "title/"+ imdbId;
      else
        uri += "find?q="+ (episode != null ? serieName.replaceAll(" \\(....\\)", "") +" " : "") + name;
      Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
      imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(imdb);
    }
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
    final EditText input = new EditText(this);
    final String extResourcesInput = extResourcesString;
    final TVShowItem serie = seriesAdapter.getItem(position);
    input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_VARIATION_URI);
    new AlertDialog.Builder(this)
      .setTitle(serie.getName())
      .setItems(extResources, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          switch(item) {
            case 0 :
              IMDbDetails(serie.getSerieId(), serie.getName(), null);
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
              WikiDetails(serie.getName());
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
    if (!Utils.isNetworkAvailable(DroidShows.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else {
      final String serieId = serie.getSerieId();
      final String serieName = serie.getName();
      final String currentLang = serie.getLanguage();
      Runnable updateserierun = new Runnable() {
        public void run() {
          if (theTVDB == null)
            theTVDB = new TheTVDB("8AC675886350B3C3", useMirror);
          Serie sToUpdate = theTVDB.getSerie(serieId, langCode == null ? currentLang : langCode);
          if (sToUpdate == null) {
            errorNotify(serieName);
            m_ProgressDialog.dismiss();
          } else {
            dialogMsg = getString(R.string.messages_title_updating_db) + " - " + serieName;
            runOnUiThread(changeMessage);
            String toastMsg = getString(R.string.menu_context_updated);
            if (!db.updateSerie(sToUpdate, langCode == null ? latestSeasonOption == UPDATE_LATEST_SEASON_ONLY : false))
              toastMsg = "Database error while updating show";
            updatePosterThumb(serieId, sToUpdate);
            m_ProgressDialog.dismiss();
            Looper.prepare();
            Toast.makeText(
              getApplicationContext(),
              sToUpdate.getSerieName() +" "+ toastMsg,
              Toast.LENGTH_SHORT
            ).show();
            listView.post(updateShowView(serieId));
            Looper.loop();
          }
          theTVDB = null;
        }
      };
      m_ProgressDialog = ProgressDialog.show(DroidShows.this, serie.getName(), getString(R.string.messages_update_serie), true, false);
      updateShowTh = new Thread(updateserierun);
      updateShowTh.start();
    }
  }

  @SuppressWarnings("deprecation")
  public void updatePosterThumb(String serieId, Serie sToUpdate) {
    Cursor c = DroidShows.db.Query("SELECT posterInCache, poster, posterThumb FROM series WHERE id='"+ serieId +"'");
    c.moveToFirst();
    if (c != null && c.isFirst()) {
      String posterInCache   = c.getString(0);
      String poster          = c.getString(1);
      String posterThumbPath = c.getString(2);
      File posterThumbFile   = new File(posterThumbPath);
      if (!posterInCache.equals("true") || !posterThumbFile.exists()) {
        if (posterThumbFile.exists())
          posterThumbFile.delete();

        PosterThumb.save(getApplicationContext(), sToUpdate, SQLiteStore.TAG);

        posterInCache   = sToUpdate.getPosterInCache();
        posterThumbPath = sToUpdate.getPosterThumb();

        db.execQuery("UPDATE series SET posterInCache='" + posterInCache + "', poster='"+ poster +"', posterThumb='"+ posterThumbPath +"' WHERE id='"+ serieId +"'");
        Log.d(SQLiteStore.TAG, "Updated poster thumb for "+ sToUpdate.getSerieName());
      }
    }
    c.close();
  }

  private Runnable changeMessage = new Runnable() {
    public void run() {
      m_ProgressDialog.setMessage(dialogMsg);
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
    String updateMessageAD = getString(R.string.dialog_update_series) + (latestSeasonOption == UPDATE_ALL_SEASONS ? getString(R.string.dialog_update_speedup) : "");
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
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
    if (!Utils.isNetworkAvailable(DroidShows.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else if (updateAllSeriesPD == null || !updateAllSeriesPD.isShowing()) {
      final List<TVShowItem> seriesToUpdate = new ArrayList<TVShowItem>();
      List<String> ids = db.getSeries(searching() ? 2 : showArchive, false, null);
      for (String id : ids)
        seriesToUpdate.add(db.createTVShowItem(id));
      final Runnable updateMessage = new Runnable() {
        public void run() {
          updateAllSeriesPD.setMessage(dialogMsg);
          updateAllSeriesPD.show();
        }
      };
      final Runnable updateallseries = new Runnable() {
        public void run() {
          if (theTVDB == null)
            theTVDB = new TheTVDB("8AC675886350B3C3", useMirror);
          String updatesFailed = "";
          for (int i = 0; i < seriesToUpdate.size(); i++) {
            Log.d(SQLiteStore.TAG, "Getting updated info from TheTVDB "+ (useMirror ? "MIRROR " : "")
              +"for TV show " + seriesToUpdate.get(i).getName() +" ["+ (i+1) +"/"+ (seriesToUpdate.size()) +"]");
            dialogMsg = seriesToUpdate.get(i).getName() + "\u2026";
            updateAllSeriesPD.incrementProgressBy(1);
            runOnUiThread(updateMessage);
            Serie sToUpdate = theTVDB.getSerie(seriesToUpdate.get(i).getSerieId(), seriesToUpdate.get(i).getLanguage());
            if (sToUpdate == null) {
              updatesFailed += dialogMsg +" ";
            } else {
              try {
                if (!db.updateSerie(sToUpdate, latestSeasonOption == UPDATE_LATEST_SEASON_ONLY)) {
                  Looper.prepare();  // Threads don't have a message loop
                  String error = getString(R.string.messages_error_dbupdate) +" "+ sToUpdate.getSerieName();
                  Log.e(SQLiteStore.TAG, error);
                  Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                  Looper.loop();
                }
                updatePosterThumb(seriesToUpdate.get(i).getSerieId(), sToUpdate);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          if (updatesFailed.length() > 0) {
            final String updatesFailedResult = updatesFailed;
            runOnUiThread(new Runnable() {
              public void run() {errorNotify(updatesFailedResult);}
            });
          }
          updateShowStats();
          updateAllSeriesPD.dismiss();
          theTVDB = null;
        }
      };
      updateAllSeriesPD = new ProgressDialog(this);
      updateAllSeriesPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      updateAllSeriesPD.setTitle(R.string.messages_title_updating_series);
      updateAllSeriesPD.setMessage(getString(R.string.messages_update_series));
      updateAllSeriesPD.setCancelable(false);
      updateAllSeriesPD.setMax(seriesToUpdate.size());
      updateAllSeriesPD.setProgress(0);
      updateAllSeriesPD.show();
      updateAllShowsTh = new Thread(updateallseries);
      updateAllShowsTh.start();
    }
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("deprecation")
  private void errorNotify(String error) {
    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    PendingIntent appIntent = PendingIntent.getActivity(DroidShows.this, 0, new Intent(), 0);

    Notification notification = null;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      notification = new Notification(R.drawable.noposter,
          getString(R.string.messages_thetvdb_con_error), System.currentTimeMillis());
      try {
        Method deprecatedMethod = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
        deprecatedMethod.invoke(notification, getApplicationContext(), getString(R.string.messages_thetvdb_con_error), error, appIntent);
      } catch (Exception e) {
        Log.e(SQLiteStore.TAG, "Method setLatestEventInfo not found", e);
      }
    } else {
      Notification.Builder builder = new Notification.Builder(getApplicationContext())
        .setContentIntent(appIntent)
        .setSmallIcon(R.drawable.noposter)
        .setContentTitle(getString(R.string.messages_thetvdb_con_error))
        .setContentText(error);
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
          notification = builder.getNotification();
      else
          notification = builder.build();
    }

    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    mNotificationManager.notify(0, notification);
  }

  private void getSeries() {
    getSeries(showArchive, filterNetworks);
  }

  private void getSeries(int showArchive) {
    getSeries(showArchive, filterNetworks);
  }

  private void getSeries(int showArchive, boolean filterNetworks) {
    main.setVisibility(View.INVISIBLE);
    if (asyncInfo != null)
      asyncInfo.cancel(true);
    try {
      if (!logMode) {
        List<String> ids = db.getSeries(showArchive, filterNetworks, networks);
        series.clear();
        seriesAdapter.notifyDataSetChanged();
        for (int i = 0; i < ids.size(); i++)
          series.add(db.createTVShowItem(ids.get(i)));
      } else {
        List<TVShowItem> episodes = db.getLog();
        series.clear();
        seriesAdapter.notifyDataSetChanged();
        for (int i = 0; i < episodes.size(); i++)
          series.add(episodes.get(i));
      }
      setTitle(getString(R.string.layout_app_name)
          + (!logMode ? (showArchive == 1 ? " - "+ getString(R.string.archive) : "") :
            " - "+ getString(R.string.menu_log)));
      runOnUiThread(updateListView);
    } catch (Exception e) {
      Log.e(SQLiteStore.TAG, "Error populating TVShowItems or no shows added yet");
      e.printStackTrace();
    }
    setFastScroll();
    findViewById(R.id.add_show).setVisibility(!logMode ? View.VISIBLE : View.GONE);
    main.setVisibility(View.VISIBLE);
    asyncInfo = new AsyncInfo();
    asyncInfo.execute();
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
      if (pinnedShows.contains(object1.getSerieId()) && !pinnedShows.contains(object2.getSerieId()))
        return -1;
      else if (pinnedShows.contains(object2.getSerieId()) && !pinnedShows.contains(object1.getSerieId()))
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

        case SORT_BY_UNSEEN_DATE: {
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
    ed.putString(PINNED_SHOWS_NAME, pinnedShows.toString());

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
    ed.putInt(LATEST_SEASON_PREF_NAME, latestSeasonOption);
    ed.putBoolean(SHOW_ICONS, showIcons);
    ed.putBoolean(SHOW_NEXT_AIRING, showNextAiring);
    ed.putBoolean(MARK_FROM_LAST_WATCHED, markFromLastWatched);
    ed.putBoolean(INCLUDE_SPECIALS_NAME, includeSpecialsOption);
    ed.putBoolean(FULL_LINE_CHECK_NAME, fullLineCheckOption);
    ed.putBoolean(SWITCH_SWIPE_DIRECTION, switchSwipeDirection);
    ed.putBoolean(USE_MIRROR, useMirror);
    ed.putString(LANGUAGE_CODE_NAME, langCode);

    // Preferences: commit changes

    ed.commit();
  }

  @Override
  protected void onStop() {
    if (autoBackup && theTVDB == null && asyncInfo.getStatus() != AsyncTask.Status.RUNNING)  // not updating
      backup(true);
    super.onStop();
  }

  @Override
  public void onRestart() {
    super.onRestart();
    if (!logMode) {
      listView.post(updateShowView(backFromSeasonSerieId));
      backFromSeasonSerieId = null;
    } else {
      if (!removeEpisodeFromLog.isEmpty()) {
        for (int i = 0; i < series.size(); i++)
          if (series.get(i).getEpisodeId().equals(removeEpisodeFromLog)) {
            series.remove(i);
            listView.post(updateListView);
          }
        removeEpisodeFromLog = "";
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
    if (!logMode && (asyncInfo == null || asyncInfo.getStatus() != AsyncTask.Status.RUNNING)) {
      asyncInfo = new AsyncInfo();
      asyncInfo.execute();
    }
  }

  private static class AsyncInfo extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
//      Log.d(SQLiteStore.TAG, "AsyncInfo Initializing");
      try {
        int showArchiveTmp = showArchive;
        String newToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());  // thread needs own SimpleDateFormat to prevent collisions in formatting of other dates
        String lastStatsUpdate = (showArchiveTmp == 0 ? lastStatsUpdateCurrent : lastStatsUpdateArchive);
        if (!lastStatsUpdate.equals(newToday)) {
          db.updateToday(newToday);
//          Log.d(SQLiteStore.TAG, "AsyncInfo RUNNING | Today = "+ newToday);
          for (int i = 0; i < series.size(); i++) {
            TVShowItem serie = series.get(i);
            if (isCancelled()) return null;
            String serieId = serie.getSerieId();
            int unwatched = db.getEpsUnwatched(serieId);
            int unwatchedAired = db.getEpsUnwatchedAired(serieId);
            if (unwatched != serie.getUnwatched() || unwatchedAired != serie.getUnwatchedAired()) {
              if (isCancelled()) return null;
              serie.setUnwatched(unwatched);
              serie.setUnwatchedAired(unwatchedAired);
              if (showNextAiring && unwatchedAired > 0) {
                NextEpisode nextEpisode = db.getNextEpisode(serieId);
                String nextEpisodeString = db.getNextEpisodeString(nextEpisode, true);
                serie.setNextEpisode(nextEpisodeString);
                if (isCancelled()) return null;
                db.execQuery("UPDATE series SET unwatched="+ unwatched +", unwatchedAired="+ unwatchedAired +", nextEpisode='"+ nextEpisodeString +"' WHERE id="+ serieId);
              } else {
                if (isCancelled()) return null;
                db.execQuery("UPDATE series SET unwatched="+ unwatched +", unwatchedAired="+ unwatchedAired +" WHERE id="+ serieId);
              }
            }
          }
          if (isCancelled()) return null;
          listView.post(updateListView);
          if (showArchiveTmp == 0 || showArchiveTmp == 2)
            lastStatsUpdateCurrent = newToday;
          if (showArchiveTmp > 0)
            lastStatsUpdateArchive = newToday;
//        Log.d(SQLiteStore.TAG, "Updated show stats for "+ (showArchiveTmp == 0 ? "current" : "archive") +" on "+ newToday);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
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
        convertView = vi.inflate(R.layout.row, parent, false);
        holder = new ViewHolder();
        holder.sn = (TextView) convertView.findViewById(R.id.seriename);
        holder.si = (TextView) convertView.findViewById(R.id.serieinfo);
        holder.sle = (TextView) convertView.findViewById(R.id.serielastepisode);
        holder.sne = (TextView) convertView.findViewById(R.id.serienextepisode);
        holder.icon = (IconView) convertView.findViewById(R.id.serieicon);
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
          holder.sn.setText((pinnedShows.contains(serie.getSerieId()) ? "\u2022 " : "") + serie.getName() + ended);
          holder.sn.setEnabled(!searching() || !serie.getPassiveStatus());
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
    TextView sn;
    TextView si;
    TextView sle;
    TextView sne;
    IconView icon;
    ImageView context;
  }
}
