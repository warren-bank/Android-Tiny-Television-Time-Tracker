package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.common.Constants;
import nl.asymmetrics.droidshows.common.DateFormats;
import nl.asymmetrics.droidshows.database.DbGateway;
import nl.asymmetrics.droidshows.ui.model.EpisodeRow;
import nl.asymmetrics.droidshows.utils.SwipeDetect;

import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class SerieEpisodes extends ListActivity {
  private DbGateway db;
  private int serieId;
  private int seasonNumber;
  private String serieName;
  private List<EpisodeRow> episodes;
  private EpisodesAdapter episodesAdapter;
  private SwipeDetect swipeDetect;
  private ListView listView;
  private DatePickerDialog dateDialog;
  private TimePickerDialog timeDialog;
  private int backFromEpisode;
  private Calendar cal;

  /* Context Menus */
  private static final int VIEWEP_CONTEXT        = Menu.FIRST;
  private static final int SEENTIMESTAMP_CONTEXT = VIEWEP_CONTEXT        + 1;
  private static final int DELEP_CONTEXT         = SEENTIMESTAMP_CONTEXT + 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.serie_episodes);

    db = DbGateway.getInstance(this);

    Intent intent = getIntent();
    serieId       = intent.getIntExtra("serieId",      -1);
    seasonNumber  = intent.getIntExtra("seasonNumber", -1);
    serieName     = db.getSerieName(serieId);

    // sanity check
    if ((serieId <= 0) || (seasonNumber < 0)) {
      finish();
      return;
    }

    setTitle(serieName + " - " + ((seasonNumber == 0) ? getString(R.string.messages_specials) : (getString(R.string.messages_season) + " " + seasonNumber)));

    episodes        = db.getEpisodeRows(serieId, seasonNumber);
    episodesAdapter = new EpisodesAdapter(this, R.layout.row_serie_episodes, episodes);
    setListAdapter(episodesAdapter);

    swipeDetect = new SwipeDetect();
    listView    = getListView();
    listView.setOnTouchListener(swipeDetect);
    registerForContextMenu(listView);

    if (intent.getBooleanExtra("nextEpisode", false))
      listView.setSelection(db.getNextEpisode(serieId, seasonNumber).episode -3);

    backFromEpisode = -1;
    cal = Calendar.getInstance();
  }

  /* context menu */
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, VIEWEP_CONTEXT,        0, getString(R.string.messsages_view_ep_details));
    menu.add(0, SEENTIMESTAMP_CONTEXT, 0, getString(R.string.messsages_edit_seen_timestamp));
    menu.add(0, DELEP_CONTEXT,         0, getString(R.string.menu_context_delete));
  }

  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    switch (item.getItemId()) {
      case VIEWEP_CONTEXT:
        startViewEpisode(info.position);
        return true;
      case SEENTIMESTAMP_CONTEXT:
        seenTimestamp(info.position);
        return true;
      case DELEP_CONTEXT:
        if (!db.deleteEpisode(serieId, episodes.get(info.position).id))
          Toast.makeText(getApplicationContext(), R.string.messages_db_error_delete_episode, Toast.LENGTH_LONG).show();
        episodes.remove(info.position);
        episodesAdapter.notifyDataSetChanged();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  public void openContext(View v) {
    this.openContextMenu(v);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    if (swipeDetect.value != 0) return;
    if (DroidShows.fullLineCheckOption) {
      try {
        CheckBox c = (CheckBox) v.findViewById(R.id.seen);
        c.setChecked(!c.isChecked());
        check(position, v, -1);
      } catch (Exception e) {
        Log.e(Constants.LOG_TAG, "Could not set episode seen state: "+ e.getMessage());
      }
    } else {
      try {
        startViewEpisode(position);
      } catch (Exception e) {
        Log.e(Constants.LOG_TAG, e.getMessage());
      }
    }
  }

  public class EpisodesAdapter extends ArrayAdapter<EpisodeRow> {
    private List<EpisodeRow> items;
    private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    private ColorStateList textViewColors = new TextView(getContext()).getTextColors();

    private final String strAired = getString(R.string.messages_aired);
    private final String strEp    = (getString(R.string.messages_ep).isEmpty() ? "" : getString(R.string.messages_ep) + " ");

    public EpisodesAdapter(Context context, int textViewResourceId, List<EpisodeRow> episodes) {
      super(context, textViewResourceId, episodes);
      this.items = episodes;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      final ViewHolder holder;

      if (convertView == null) {
        convertView = vi.inflate(R.layout.row_serie_episodes, parent, false);

        holder               = new ViewHolder();
        holder.name          = (TextView) convertView.findViewById(R.id.name);
        holder.aired         = (TextView) convertView.findViewById(R.id.aired);
        holder.seenTimestamp = (TextView) convertView.findViewById(R.id.seenTimestamp);
        holder.seen          = (CheckBox) convertView.findViewById(R.id.seen);
        holder.seen.setOnLongClickListener(new OnLongClickListener() {
          public boolean onLongClick(View v) {
            seenTimestamp(listView.getPositionForView(v));
            return true;
          }
        });

        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      EpisodeRow ep = items.get(position);

      if (holder.name != null) {
        String name = strEp + ep.name;
        holder.name.setText(name);
      }

      if (holder.aired != null) {
        if (!TextUtils.isEmpty(ep.aired))
          holder.aired.setText(strAired + " " + ep.aired);
        else
          holder.aired.setText("");

        holder.aired.setEnabled((ep.airedDate != null) && ep.airedDate.compareTo(Calendar.getInstance().getTime()) <= 0);
      }

      holder.seen.setChecked(ep.seen > 0);
      if (ep.seen > 0) {
        long seen_ms = DateFormats.convertSecondsToMs(ep.seen);
        holder.seenTimestamp.setTextColor(textViewColors);
        holder.seenTimestamp.setText(DateFormats.DISPLAY_DATE.format(new Date(seen_ms)));
      } else
        holder.seenTimestamp.setText("");

      return convertView;
    }
  }

  static class ViewHolder {
    TextView name, aired, seenTimestamp;
    CheckBox seen;
  }

  private View getViewByPosition(int position) {
    try {
      final int firstListItemPosition = listView.getFirstVisiblePosition();
      final int lastListItemPosition  = firstListItemPosition + listView.getChildCount() - 1;
      if ((position < firstListItemPosition) || (position > lastListItemPosition))
        return listView.getAdapter().getView(position, null, listView);
      else
        return listView.getChildAt(position - firstListItemPosition);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void check(View v) {
    int position = listView.getPositionForView(v);
    check(position, v, -1);
  }

  private void check(int position, int seen) {
    check(position, getViewByPosition(position), seen);
  }

  private void check(int position, View v, int seen) {
    if (v != null) {
      CheckBox c = (CheckBox) v.findViewById(R.id.seen);
      TextView d = (TextView) getViewByPosition(position).findViewById(R.id.seenTimestamp);
      if (seen > -1)
        c.setChecked(true);
      if (c.isChecked()) {
        d.setTextColor(getResources().getColor(android.R.color.white));
        if (seen == -1)
          seen = DateFormats.convertMsToSeconds(System.currentTimeMillis());
        episodes.get(position).seen = seen;
        d.setText(DateFormats.DISPLAY_DATE.format(new Date(DateFormats.convertSecondsToMs(seen))));
      } else {
        d.setText("");
        episodes.get(position).seen = 0;
      }
    }
    db.updateUnwatchedEpisode(serieId, episodes.get(position).id, seen);
  }

  private void seenTimestamp(final int position) {
    int  seen    = episodes.get(position).seen;
    long seen_ms = DateFormats.convertSecondsToMs(seen);
    
    if (seen > 0)
      cal.setTimeInMillis(seen_ms);
    else
      cal.setTimeInMillis(System.currentTimeMillis());

    int sYear         = cal.get(Calendar.YEAR);
    int sMonth        = cal.get(Calendar.MONTH);
    int sDay          = cal.get(Calendar.DAY_OF_MONTH);
    final int sHour   = cal.get(Calendar.HOUR_OF_DAY);
    final int sMinute = cal.get(Calendar.MINUTE);

    dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
      public void onDateSet(DatePicker view, int year, int month, int day) {
        cal.set(year, month, day);
        timeDialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
          public void onTimeSet(TimePicker view, int hour, int minute) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            check(position, DateFormats.convertMsToSeconds(cal.getTimeInMillis()));
          }
        }, sHour, sMinute, true);
        timeDialog.show();
      }
    }, sYear, sMonth, sDay);
    dateDialog.show();
  }

  private void startViewEpisode(int position) {
    backFromEpisode = position;
    Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
    viewEpisode.putExtra("serieId",   serieId);
    viewEpisode.putExtra("serieName", serieName);
    viewEpisode.putExtra("episodeId", episodes.get(position).id);
    startActivity(viewEpisode);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
  }

  @Override
  public void onRestart() {
    super.onRestart();
    if (backFromEpisode != -1) {
      episodes.set(backFromEpisode, db.getEpisodeRow(serieId, seasonNumber, episodes.get(backFromEpisode).id));
      episodesAdapter.notifyDataSetChanged();
      backFromEpisode = -1;
    }
  }
}
