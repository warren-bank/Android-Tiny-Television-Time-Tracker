package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.common.Constants;
import nl.asymmetrics.droidshows.database.DbGateway;
import nl.asymmetrics.droidshows.ui.model.NextEpisode;
import nl.asymmetrics.droidshows.ui.model.SeasonRow;
import nl.asymmetrics.droidshows.utils.SwipeDetect;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SerieSeasons extends ListActivity
{
  private static boolean isUpdating = false;

  private DbGateway db;
  private int serieId;
  private String serieName;
  private List<SeasonRow> seasons;
  private SeriesSeasonsAdapter seasonsAdapter;
  private SwipeDetect swipeDetect;
  private ListView listView;

  // Context Menus
  private static final int ALLEPSEEN_CONTEXT   = Menu.FIRST;
  private static final int ALLUPTOTHIS_CONTEXT = ALLEPSEEN_CONTEXT   + 1;
  private static final int ALLEPUNSEEN_CONTEXT = ALLUPTOTHIS_CONTEXT + 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.serie_seasons);

    db = DbGateway.getInstance(this);

    serieId   = getIntent().getIntExtra("serieId", -1);
    serieName = db.getSerieName(serieId);

    // sanity check
    if (serieId <= 0) {
      finish();
      return;
    }

    setTitle(serieName);

    seasons        = db.getSeasonRows(serieId);
    seasonsAdapter = new SeriesSeasonsAdapter(this, R.layout.row_serie_seasons, seasons);
    setListAdapter(seasonsAdapter);

    swipeDetect = new SwipeDetect();
    listView    = getListView();
    listView.getViewTreeObserver().addOnGlobalLayoutListener(listDone);
    registerForContextMenu(listView);
    listView.setOnTouchListener(swipeDetect);
    listView.setSelection(getIntent().getIntExtra("season", 1) -1);
    if (getIntent().getBooleanExtra("nextEpisode", false))
      listView.setSelection(db.getNextEpisode(serieId).season -1);
  }

  /* context menu */
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
    SeasonRow season            = seasonsAdapter.getItem(info.position);

    menu.add(0, ALLEPSEEN_CONTEXT,   0, getString(R.string.messages_context_mark_seasonseen));
    menu.add(0, ALLUPTOTHIS_CONTEXT, 0, getString(R.string.messages_context_mark_asseenuptothis));
    menu.add(0, ALLEPUNSEEN_CONTEXT, 0, getString(R.string.messages_context_mark_seasonunseen));
    menu.setHeaderTitle(season.name);
  }

  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    SeasonRow season            = seasonsAdapter.getItem(info.position);

    switch (item.getItemId()) {
      case ALLEPSEEN_CONTEXT :
        db.updateUnwatchedSeason(serieId, season.seasonNumber);
        getInfo();
        return true;
      case ALLEPUNSEEN_CONTEXT :
        db.updateWatchedSeason(serieId, season.seasonNumber);
        getInfo();
        return true;
      case ALLUPTOTHIS_CONTEXT :
        for (int i = 1; i <= season.seasonNumber; i++) {
          db.updateUnwatchedSeason(serieId, i);
        }
        getInfo();
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
    SeasonRow season = seasonsAdapter.getItem(position);

    if (swipeDetect.value != 0) return;
    Intent serieEpisode = new Intent(SerieSeasons.this, SerieEpisodes.class);
    serieEpisode.putExtra("serieId", serieId);
    serieEpisode.putExtra("seasonNumber", season.seasonNumber);
    if (season.unwatched > 0)
      serieEpisode.putExtra("nextEpisode", true);
    startActivity(serieEpisode);
  }

  private final OnGlobalLayoutListener listDone = new OnGlobalLayoutListener() {
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public void onGlobalLayout() {
      if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        listView.getViewTreeObserver().removeGlobalOnLayoutListener(listDone);
      else
        listView.getViewTreeObserver().removeOnGlobalLayoutListener(listDone);
      getInfo();
    }
  };

  private void getInfo() {
    if (!isUpdating) {
      isUpdating = true;
      new AsyncInfo().execute();
    }
  }

  private class AsyncInfo extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
      try {
        for (int i = 0; i < seasons.size(); i++) {
          SeasonRow season = seasons.get(i);

          int serieId           = season.serieId;
          int seasonNumber      = season.seasonNumber;
          int unwatched         = db.getEpsUnwatched(serieId, seasonNumber);
          int unwatchedAired    = db.getEpsUnwatchedAired(serieId, seasonNumber);

          season.unwatched      = unwatched;
          season.unwatchedAired = unwatchedAired;

          if (unwatched > 0) {
            NextEpisode nextEpisode = db.getNextEpisode(serieId, seasonNumber);

            season.nextAir      = nextEpisode.firstAiredDate;
            season.nextEpisode  = db.getNextEpisodeString(nextEpisode);
          }
        }
      } catch (Exception e) {
        Log.e(Constants.LOG_TAG, e.getMessage());
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      isUpdating = false;
      seasonsAdapter.notifyDataSetChanged();
      super.onPostExecute(result);
    }
  }

  @Override
  public void onRestart() {
    super.onRestart();
    getInfo();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
  }

  // ---------------------------------------------------------------------------
  // SeriesSeasonsAdapter
  // ---------------------------------------------------------------------------

  private class SeriesSeasonsAdapter extends ArrayAdapter<SeasonRow> {
    private List<SeasonRow> items;
    private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    private final String strEpAired       = getString(R.string.messages_ep_aired);
    private final String strEps           = getString(R.string.messages_episodes);
    private final String strNewEp         = getString(R.string.messages_new_episode);
    private final String strNewEps        = getString(R.string.messages_new_episodes);
    private final String strNextEp        = getString(R.string.messages_next_episode);
    private final String strOf            = getString(R.string.messages_of);
    private final String strOn            = getString(R.string.messages_on);
    private final String strSeasonWatched = getString(R.string.messages_season_completely_watched);
    private final String strToBeAired     = getString(R.string.messages_to_be_aired);
    private final String strToBeAiredPl   = getString(R.string.messages_to_be_aired_pl);

    public SeriesSeasonsAdapter(Context context, int textViewResourceId, List<SeasonRow> items) {
      super(context, textViewResourceId, items);
      this.items = items;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;
      if (convertView == null) {
        convertView = vi.inflate(R.layout.row_serie_seasons, parent, false);
        holder             = new ViewHolder();
        holder.season      = (TextView)  convertView.findViewById(R.id.serieseason);
        holder.unwatched   = (TextView)  convertView.findViewById(R.id.unwatched);
        holder.nextEpisode = (TextView)  convertView.findViewById(R.id.nextepisode);
        holder.context     = (ImageView) convertView.findViewById(R.id.seriecontext);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
        holder.unwatched.setText("");
        holder.nextEpisode.setText("");
      }

      SeasonRow season    = items.get(position);
      int nunwatched      = season.unwatched;
      int nunwatchedAired = season.unwatchedAired;

      if (holder.season != null) {
        holder.season.setText(season.name);
      }
      if (holder.unwatched != null) {
        String unwatchedText = season.episodeCount + " " + strEps;
        if (nunwatched > 0) {
          String unwatched = "";
          unwatched = nunwatched + " " + ((nunwatched > 1) ? strNewEps : strNewEp) + " ";
          if (nunwatchedAired > 0)
            unwatched = ((nunwatchedAired == nunwatched) ? "" : (nunwatchedAired + " " + strOf + " ")) + unwatched + strEpAired;
          else
            unwatched += ((nunwatched > 1) ? strToBeAiredPl : strToBeAired);
          unwatchedText += " | "+ unwatched;
        }
        holder.unwatched.setText(unwatchedText);
      }
      if (holder.nextEpisode != null) {
        if (nunwatched == 0) {
          holder.nextEpisode.setText(strSeasonWatched);
          holder.nextEpisode.setEnabled(false);
        } else if (nunwatched > 0) {
          holder.nextEpisode.setText(
            TextUtils.isEmpty(season.nextEpisode)
              ? ""
              : season.nextEpisode
                  .replace("[ne]", strNextEp)
                  .replace("[on]", strOn)
          );
          holder.nextEpisode.setEnabled((season.nextAir != null) && season.nextAir.compareTo(Calendar.getInstance().getTime()) <= 0);
        }
      }
      if (holder.context != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
          holder.context.setImageResource(R.drawable.context_material);
        holder.context.setVisibility(View.VISIBLE);
      }
      return convertView;
    }
  }

  private class ViewHolder {
    TextView  season;
    TextView  unwatched;
    TextView  nextEpisode;
    ImageView context;
  }

}
