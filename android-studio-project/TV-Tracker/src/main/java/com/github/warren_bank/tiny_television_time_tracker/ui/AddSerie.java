package com.github.warren_bank.tiny_television_time_tracker.ui;

import com.github.warren_bank.tiny_television_time_tracker.DroidShows;
import com.github.warren_bank.tiny_television_time_tracker.R;
import com.github.warren_bank.tiny_television_time_tracker.api.ApiGateway;
import com.github.warren_bank.tiny_television_time_tracker.common.Constants;
import com.github.warren_bank.tiny_television_time_tracker.database.DbGateway;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.SearchResult;
import com.github.warren_bank.tiny_television_time_tracker.ui.model.TVShowItem;
import com.github.warren_bank.tiny_television_time_tracker.utils.NetworkUtils;
import com.github.warren_bank.tiny_television_time_tracker.utils.SwipeDetect;
import com.github.warren_bank.tiny_television_time_tracker.utils.WakeLockMgr;
import com.github.warren_bank.tiny_television_time_tracker.utils.WifiLockMgr;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddSerie extends ListActivity
{
  private static final int ADD_SERIE_MENU_ITEM    = Menu.FIRST;
  private static final int ADD_CONTEXT            = Menu.FIRST;

  private ApiGateway api;
  private DbGateway db;
  private List<Integer> serieIds;
  private String langCode;
  private String searchQuery;
  private List<SearchResult> searchResults;
  private SeriesSearchAdapter searchAdapter;
  private AsyncAddSerie addSerieTask;
  private ProgressDialog m_ProgressDialog;
  private ListView listView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.add_serie);

    connectAPI();
    this.db               = DbGateway.getInstance(this);
    this.serieIds         = db.getSerieIds(2, false, null); // all series
    this.langCode         = DroidShows.langCode;
    this.searchQuery      = "";
    this.searchResults    = new ArrayList<SearchResult>();
    this.searchAdapter    = new SeriesSearchAdapter(this, R.layout.row_search_series, this.searchResults);
    this.addSerieTask     = null;
    this.m_ProgressDialog = null;
    this.listView         = null;

    setListAdapter(this.searchAdapter);
    ((TextView) findViewById(R.id.change_language)).setText(getString(R.string.dialog_change_language) + " (" + langCode + ")");
    getSearchResults(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    getSearchResults(intent);
  }

  /* Options Menu */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case ADD_SERIE_MENU_ITEM :
        onSearchRequested();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private boolean connectAPI() {
    if (api != null) return true;

    try {
      api = ApiGateway.getInstance(this);
      return true;
    }
    catch(Exception e) {
      api = null;
      Toast.makeText(getApplicationContext(), R.string.menu_context_updated, Toast.LENGTH_LONG).show();
      return false;
    }
  }

  // ---------------------------------------------------------------------------
  // perform search and display the results
  // ---------------------------------------------------------------------------

  private void getSearchResults(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      searchQuery = intent.getStringExtra(SearchManager.QUERY);
      if (searchQuery.length() == 0) {
        onSearchRequested();
        return;
      }
      TextView title = (TextView) findViewById(R.id.add_serie_title);
      title.setText(getString(R.string.dialog_search) + " " + searchQuery);
      doSearch();
    }
    listView = getListView();
    listView.setOnTouchListener(new SwipeDetect());
    registerForContextMenu(listView);
  }

  public void changeLanguage(View v) {
    AlertDialog.Builder changeLang = new AlertDialog.Builder(this);
    changeLang.setTitle(R.string.dialog_change_language)
      .setItems(R.array.languages, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          langCode = getResources().getStringArray(R.array.langcodes)[item];
          TextView changeLangB = (TextView) findViewById(R.id.change_language);
          changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
          doSearch();
        }
      })
      .show();
  }

  private void doSearch() {
    if (!NetworkUtils.isNetworkAvailable(AddSerie.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else if ((m_ProgressDialog == null) || !m_ProgressDialog.isShowing()) {
      if (!connectAPI()) return;

      m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_search_series), getString(R.string.messages_search_series), true, true);

      new Thread(new Runnable() {
        public void run() {
          try {
            searchResults = api.searchSeries(searchQuery, langCode);
            runOnUiThread(updateSearchResults);
          } catch (Exception e) {
            Log.e(Constants.LOG_TAG, e.getMessage());
          }
        }
      }).start();
    }
  }

  private Runnable updateSearchResults = new Runnable() {
    public void run() {
      searchAdapter.clear();
      if ((searchResults != null) && !searchResults.isEmpty()) {
        for (SearchResult searchResult : searchResults) {
          searchAdapter.add(searchResult);
        }
        searchResults = null;
      }
      searchAdapter.notifyDataSetChanged();
      m_ProgressDialog.dismiss();
    }
  };

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    m_ProgressDialog.dismiss();
    super.onSaveInstanceState(outState);
  }

  // ---------------------------------------------------------------------------
  // handle clicks to ListView => on one search result
  // ---------------------------------------------------------------------------

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    final SearchResult searchResult  = searchAdapter.getItem(position);
    final boolean      alreadyExists = serieExists(searchResult.serieId);

    AlertDialog sOverview = new AlertDialog.Builder(this)
    .setIcon(R.drawable.icon)
    .setTitle(searchResult.name)
    .setMessage(searchResult.overview)
    .setPositiveButton(getString(R.string.menu_context_add_serie), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.dismiss();
        addSerie(searchResult);
      }
    })
    .setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    })
    .show();

    if (alreadyExists)
      sOverview.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
  }

  // ---------------------------------------------------------------------------
  // handle clicks to ContextMenu => for one search result
  // ---------------------------------------------------------------------------

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, ADD_CONTEXT, 0, getString(R.string.menu_context_add_serie));
  }

  public boolean onContextItemSelected(MenuItem item) {
    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    final ListView searchResultsList = getListView();
    switch (item.getItemId()) {
      case ADD_CONTEXT :
        final SearchResult searchResult = (SearchResult) searchResultsList.getAdapter().getItem(info.position);
        addSerie(searchResult);
        return true;
      default :
        return super.onContextItemSelected(item);
    }
  }

  // ---------------------------------------------------------------------------
  // add Series
  // ---------------------------------------------------------------------------

  private void addSerie(SearchResult searchResult) {
    boolean isBusy = false;

    if (!NetworkUtils.isNetworkAvailable(AddSerie.this)) {
      Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
    } else if ((m_ProgressDialog == null) || !m_ProgressDialog.isShowing()) {
      if (!connectAPI()) return;

      if ((addSerieTask == null) || (addSerieTask.getStatus() != AsyncTask.Status.RUNNING)) {
        addSerieTask = new AsyncAddSerie();
        addSerieTask.execute(searchResult);
      } else {
        isBusy = true;
      }
    }
    else {
      isBusy = true;
    }

    if (isBusy) {
      Log.d(Constants.LOG_TAG, "Still busy, not adding " + searchResult.name);
      Toast.makeText(getApplicationContext(), R.string.messages_db_error_update, Toast.LENGTH_SHORT).show();
    }
  }

  private class AsyncAddSerie extends AsyncTask<SearchResult, Void, Boolean> {
    String msg = null;

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_adding_serie), getString(R.string.messages_adding_serie), true, false);
    }

    protected Boolean doInBackground(SearchResult... params) {
      SearchResult searchResult = params[0];

      boolean alreadyExists = serieExists(searchResult.serieId);
      if (alreadyExists) return false;

      WakeLockMgr.acquire(AddSerie.this);
      WifiLockMgr.acquire(AddSerie.this);

      boolean archived = (DroidShows.showArchive == 1);
      boolean result   = api.addSeries(searchResult.serieId, langCode, archived);

      if (result) {
        serieIds.add(searchResult.serieId);

        TVShowItem tvsi = db.createTVShowItem(searchResult.serieId);
        DroidShows.series.add(tvsi);
        runOnUiThread(DroidShows.updateListView);

        msg = getString(R.string.messages_series_success, searchResult.name)
          + (archived ? (" (" + getString(R.string.messages_context_archived) + ")") : "");
      }

      return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      super.onPostExecute(result);
      searchAdapter.notifyDataSetChanged();
      m_ProgressDialog.dismiss();

      WakeLockMgr.release();
      WifiLockMgr.release();

      if (!TextUtils.isEmpty(msg))
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCancelled(Boolean result) {
      this.onPostExecute(result);
      super.onCancelled();
    }
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private boolean serieExists(int needle) {
    boolean alreadyExists = false;
    for (Integer serieId : serieIds) {
      if (serieId == needle) {
        alreadyExists = true;
        break;
      }
    }
    return alreadyExists;
  }

  // ---------------------------------------------------------------------------
  // SeriesSearchAdapter
  // ---------------------------------------------------------------------------

  private class SeriesSearchAdapter extends ArrayAdapter<SearchResult> {
    private List<SearchResult> searchResults;

    public SeriesSearchAdapter(Context context, int textViewResourceId, List<SearchResult> searchResults) {
      super(context, textViewResourceId, searchResults);
      this.searchResults = searchResults;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      View v = convertView;
      if (v == null) {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.row_search_series, parent, false);
      }
      final SearchResult searchResult = searchResults.get(position);

      if (searchResult != null) {
        TextView  sn  = (TextView)  v.findViewById(R.id.seriename);
        ImageView btn = (ImageView) v.findViewById(R.id.addserieBtn);

        if (sn != null) {
          String lang = TextUtils.isEmpty(searchResult.language)
            ? ""
            : (" (" + searchResult.language + ")");

          sn.setText(searchResult.name + lang);
        }
        if (btn != null) {
          boolean alreadyExists = serieExists(searchResult.serieId);

          if (alreadyExists) {
            // btn.setVisibility(View.GONE);
            btn.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
            btn.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                // no op
                return;
              }
            });
          }
          else {
            // btn.setVisibility(View.VISIBLE);
            btn.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_add));
            btn.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                addSerie(searchResult);
              }
            });
          }
        }
      }
      return v;
    }
  }

  // ---------------------------------------------------------------------------
  // release resources when stopped
  // ---------------------------------------------------------------------------

  @Override
  protected void onStop() {
    if ((addSerieTask == null) || (addSerieTask.getStatus() != AsyncTask.Status.RUNNING)) {
      WakeLockMgr.release();
      WifiLockMgr.release();
    }

    super.onStop();
  }

}
