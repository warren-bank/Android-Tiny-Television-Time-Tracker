package nl.asymmetrics.droidshows.ui;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.common.Constants;
import nl.asymmetrics.droidshows.common.DateFormats;
import nl.asymmetrics.droidshows.database.DbGateway;
import nl.asymmetrics.droidshows.database.model.DbDirector;
import nl.asymmetrics.droidshows.database.model.DbEpisode;
import nl.asymmetrics.droidshows.database.model.DbGuestStar;
import nl.asymmetrics.droidshows.database.model.DbWriter;
import nl.asymmetrics.droidshows.utils.RuntimePermissionUtils;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import nl.asymmetrics.droidshows.utils.UrlUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewEpisode extends Activity implements RuntimePermissionUtils.RuntimePermissionListener {
  private DbGateway db;
  private DbEpisode dbEpisode;

  private List<String> writers;
  private List<String> directors;
  private List<String> guestStars;

  private int serieId, episodeId;
  private String serieName, imdbUri;

  private SwipeDetect swipeDetect;
  private DatePickerDialog dateDialog;
  private TimePickerDialog timeDialog;
  private Calendar cal;
  private Date epDate;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_episode);

    Intent intent = getIntent();
    serieId       = intent.getIntExtra("serieId",   -1);
    episodeId     = intent.getIntExtra("episodeId", -1);
    serieName     = intent.getStringExtra("serieName");

    // sanity check
    if ((serieId <= 0) || (episodeId <= 0)) {
      finish();
      return;
    }

    db        = DbGateway.getInstance(this);
    dbEpisode = db.getDbEpisode(serieId, episodeId);

    List<DbWriter>    dbWriters    = db.getDbWriter   (serieId, episodeId);
    List<DbDirector>  dbDirectors  = db.getDbDirector (serieId, episodeId);
    List<DbGuestStar> dbGuestStars = db.getDbGuestStar(serieId, episodeId);

    writers    = new ArrayList<String>();
    directors  = new ArrayList<String>();
    guestStars = new ArrayList<String>();

    if ((dbWriters != null) && !dbWriters.isEmpty()) {
      for (DbWriter dbWriter : dbWriters) {
        writers.add(dbWriter.writer);
      }
    }
    if ((dbDirectors != null) && !dbDirectors.isEmpty()) {
      for (DbDirector dbDirector : dbDirectors) {
        directors.add(dbDirector.director);
      }
    }
    if ((dbGuestStars != null) && !dbGuestStars.isEmpty()) {
      for (DbGuestStar dbGuestStar : dbGuestStars) {
        guestStars.add(dbGuestStar.guestStar);
      }
    }

    swipeDetect = new SwipeDetect();
    View view   = findViewById(R.id.viewEpisodes);
    view.setOnTouchListener(swipeDetect);

    cal = Calendar.getInstance();

    if (!TextUtils.isEmpty(dbEpisode.firstAired)) {
      try {
        epDate               = DateFormats.NORMALIZE_DATE.parse(dbEpisode.firstAired);
        dbEpisode.firstAired = DateFormats.DISPLAY_DATE.format(epDate);
      } catch (ParseException e) {
        Log.e(Constants.LOG_TAG, e.getMessage());
      }
    } else {
      epDate               = null;
      dbEpisode.firstAired = "";
    }

    setTitle(serieName
      + " - "
      + (getString(R.string.messages_ep).isEmpty() ? "" : (getString(R.string.messages_ep) + " "))
      + dbEpisode.seasonNumber
      + ((dbEpisode.episodeNumber < 10) ? "x0" : "x")
      + dbEpisode.episodeNumber
    );

    TextView episodeNameV = (TextView) findViewById(R.id.episodeName);
    episodeNameV.setText(dbEpisode.name);

    TextView ratingV = (TextView) findViewById(R.id.rating);
    if (dbEpisode.reviewRating > 0)
      ratingV.setText("IMDb: "
        + String.format("%.02f", dbEpisode.reviewRating)
        + " \u00b7 "
        + ((!TextUtils.isEmpty(dbEpisode.imdbId) && dbEpisode.imdbId.startsWith("tt"))
            ? getString(R.string.menu_context_view_ep_imdb)
            : getString(R.string.menu_search)
          )
      );
    else if (!TextUtils.isEmpty(dbEpisode.imdbId) && dbEpisode.imdbId.startsWith("tt"))
      ratingV.setText(getString(R.string.menu_context_view_ep_imdb));
    else
      ratingV.setText(getString(R.string.menu_context_search_on) + " IMDb");
    ratingV.setOnTouchListener(swipeDetect);

    final CheckBox seenCheckBox = (CheckBox) findViewById(R.id.seen);
    seenCheckBox.setChecked(dbEpisode.seen > 0);
    check(seenCheckBox);
    seenCheckBox.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View arg0) {
        if (dbEpisode.seen > 0)
          cal.setTimeInMillis(DateFormats.convertSecondsToMs(dbEpisode.seen));
        else
          cal.setTimeInMillis(System.currentTimeMillis());

        int sYear         = cal.get(Calendar.YEAR);
        int sMonth        = cal.get(Calendar.MONTH);
        int sDay          = cal.get(Calendar.DAY_OF_MONTH);
        final int sHour   = cal.get(Calendar.HOUR_OF_DAY);
        final int sMinute = cal.get(Calendar.MINUTE);

        dateDialog = new DatePickerDialog(seenCheckBox.getContext(), new DatePickerDialog.OnDateSetListener() {
          public void onDateSet(DatePicker view, int year, int month, int day) {
            cal.set(year, month, day);
            timeDialog = new TimePickerDialog(seenCheckBox.getContext(), new TimePickerDialog.OnTimeSetListener() {
              public void onTimeSet(TimePicker view, int hour, int minute) {
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                dbEpisode.seen = DateFormats.convertMsToSeconds(cal.getTimeInMillis());
                seenCheckBox.setChecked(dbEpisode.seen > 0);
                check(seenCheckBox);
              }
            }, sHour, sMinute, true);
            timeDialog.show();
          }
        }, sYear, sMonth, sDay);

        dateDialog.show();
        return true;
      }
    });

    if (!TextUtils.isEmpty(dbEpisode.firstAired)) {
      TextView firstAiredV = (TextView) findViewById(R.id.firstAired);
      firstAiredV.setText(dbEpisode.firstAired);
      firstAiredV.setVisibility(View.VISIBLE);

      // only allow click events to trigger calendarEvent() when the episode will air at a future date
      if ((epDate == null) || (epDate.compareTo(Calendar.getInstance().getTime()) <= 0)) {
        firstAiredV.setEnabled(false);
      }
    }

    if (!TextUtils.isEmpty(dbEpisode.overview)) {
      TextView overviewV = (TextView) findViewById(R.id.overview);
      overviewV.setText(dbEpisode.overview);
      findViewById(R.id.overviewField).setVisibility(View.VISIBLE);
    }

    if (!writers.isEmpty()) {
      TextView writersV = (TextView) findViewById(R.id.writer);
      writersV.setText(writers.toString().replace("]", "").replace("[", ""));
      writersV.setOnTouchListener(swipeDetect);
      View writerField = (View) findViewById(R.id.writerField);
      writerField.setOnTouchListener(swipeDetect);
      writerField.setVisibility(View.VISIBLE);
    }

    if (!directors.isEmpty()) {
      TextView directorsV = (TextView) findViewById(R.id.director);
      directorsV.setText(directors.toString().replace("]", "").replace("[", ""));
      directorsV.setOnTouchListener(swipeDetect);
      View directorField = (View) findViewById(R.id.directorField);
      directorField.setOnTouchListener(swipeDetect);
      directorField.setVisibility(View.VISIBLE);
    }

    if (!guestStars.isEmpty()) {
      TextView guestStarsV = (TextView) findViewById(R.id.guestStars);
      guestStarsV.setText(guestStars.toString().replace("]", "").replace("[", ""));
      guestStarsV.setOnTouchListener(swipeDetect);
      View guestStarsField = (View) findViewById(R.id.guestStarsField);
      guestStarsField.setOnTouchListener(swipeDetect);
      guestStarsField.setVisibility(View.VISIBLE);
    }

    Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
    imdbUri = (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
      ? "https://m.imdb.com/"
      : "imdb:///";
  }

  public void addCalendarEventPermissionCheck(View v) {
    String[] allRequestedPermissions = new String[]{"android.permission.WRITE_CALENDAR"};

    int requestCode = Constants.PERMISSION_CHECK_REQUEST_CODE_ADD_CALENDAR_EVENT;

    RuntimePermissionUtils.requestPermissions(ViewEpisode.this, ViewEpisode.this, allRequestedPermissions, requestCode);
  }

  @Override
  public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
    RuntimePermissionUtils.onRequestPermissionsResult(ViewEpisode.this, ViewEpisode.this, requestCode, permissions, grantResults);
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsGranted(int requestCode, Object passthrough) {
    if (requestCode == Constants.PERMISSION_CHECK_REQUEST_CODE_ADD_CALENDAR_EVENT)
      addCalendarEvent();
  }

  @Override // RuntimePermissionUtils.RuntimePermissionListener
  public void onRequestPermissionsDenied(int requestCode, Object passthrough, String[] missingPermissions) {
    Toast.makeText(getApplicationContext(), R.string.messages_no_permission, Toast.LENGTH_LONG).show();
  }

  private void addCalendarEvent() {
    Intent intent = new Intent(Intent.ACTION_EDIT);
    intent.setType("vnd.android.cursor.item/event");
    intent.putExtra("title",       serieName + " " + dbEpisode.seasonNumber + ((dbEpisode.episodeNumber < 10) ? "x0" : "x") + dbEpisode.episodeNumber);
    intent.putExtra("description", dbEpisode.name);
    intent.putExtra("beginTime",   epDate.getTime());
    intent.putExtra("endTime",     epDate.getTime());
    intent.putExtra("allDay",      true);
    try {
      startActivity(intent);
    } catch (Exception e) {
      Toast.makeText(getApplicationContext(), R.string.messages_calendar_app_error, Toast.LENGTH_LONG).show();
      Log.e(Constants.LOG_TAG, e.getMessage());
    }
  }

  public void IMDbDetails(View v) {
    if (swipeDetect.value != 0) return;

    String uri = imdbUri
      + ((!TextUtils.isEmpty(dbEpisode.imdbId) && dbEpisode.imdbId.startsWith("tt"))
          ? ("title/"  + dbEpisode.imdbId)
          : ("find?q=" + UrlUtils.encodeSerieNameForQuerystringValue(serieName + " " + dbEpisode.name))
        );

    Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(imdb);
  }

  public void IMDbNames(View v) {
    if (swipeDetect.value != 0) return;

    final List<String> names;
    int id = v.getId();
    if ((id == R.id.writer) || (id == R.id.writerField))
      names = writers;
    else if ((id == R.id.director) || (id == R.id.directorField))
      names = directors;
    else if ((id == R.id.guestStars) || (id == R.id.guestStarsField))
      names = guestStars;
    else
      return;

    new AlertDialog.Builder(this)
      .setTitle(R.string.menu_search)
      .setItems(names.toArray(new CharSequence[names.size()]), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(imdbUri + "find?q=" + UrlUtils.encodeURIComponent(names.get(item))));
          imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(imdb);
        }
      })
      .show();
  }

  public void check(View v) {
    if (v != null) {
      CheckBox c = (CheckBox) findViewById(R.id.seen);
      TextView d = (TextView) findViewById(R.id.seenTimestamp);
      if (c.isChecked()) {
        d.setTextColor(getResources().getColor(android.R.color.white));
        if (dbEpisode.seen == 0)
          dbEpisode.seen = DateFormats.convertMsToSeconds(System.currentTimeMillis());
        d.setText(DateFormats.DISPLAY_DATE_TIME.format(new Date(DateFormats.convertSecondsToMs(dbEpisode.seen))));
        DroidShows.removeEpisodeIdFromLog = 0;
      } else {
        d.setText("");
        dbEpisode.seen = 0;
        DroidShows.removeEpisodeIdFromLog = episodeId;
      }
    }
    db.updateUnwatchedEpisode(serieId, episodeId, dbEpisode.seen);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
  }
}
