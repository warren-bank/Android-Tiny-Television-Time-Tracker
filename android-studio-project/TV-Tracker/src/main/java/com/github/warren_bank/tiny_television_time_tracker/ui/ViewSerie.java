package com.github.warren_bank.tiny_television_time_tracker.ui;

import com.github.warren_bank.tiny_television_time_tracker.R;
import com.github.warren_bank.tiny_television_time_tracker.common.Constants;
import com.github.warren_bank.tiny_television_time_tracker.common.DateFormats;
import com.github.warren_bank.tiny_television_time_tracker.database.DbGateway;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbActor;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbGenre;
import com.github.warren_bank.tiny_television_time_tracker.database.model.DbSeries;
import com.github.warren_bank.tiny_television_time_tracker.utils.SwipeDetect;
import com.github.warren_bank.tiny_television_time_tracker.utils.UrlUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewSerie extends Activity {
  private DbGateway db;
  private DbSeries dbSeries;

  private List<String> actors;

  private int serieId;
  private String imdbUri;

  private SwipeDetect swipeDetect;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.overridePendingTransition(R.anim.left_enter, R.anim.left_exit);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_serie);

    Intent intent = getIntent();
    serieId       = intent.getIntExtra("serieId", -1);

    // sanity check
    if (serieId <= 0) {
      finish();
      return;
    }

    db       = DbGateway.getInstance(this);
    dbSeries = db.getDbSeries(serieId);

    List<DbActor> dbActors = db.getDbActor(serieId);
    List<DbGenre> dbGenres = db.getDbGenre(serieId);

    List<String> genres;

    actors = new ArrayList<String>();
    genres = new ArrayList<String>();

    if ((dbActors != null) && !dbActors.isEmpty()) {
      for (DbActor dbActor : dbActors) {
        actors.add(dbActor.actor);
      }
    }
    if ((dbGenres != null) && !dbGenres.isEmpty()) {
      for (DbGenre dbGenre : dbGenres) {
        genres.add(dbGenre.genre);
      }
    }

    swipeDetect = new SwipeDetect();
    View view   = findViewById(R.id.viewSerie);
    view.setOnTouchListener(swipeDetect);

    if (!TextUtils.isEmpty(dbSeries.network)) {
      TextView networkV = (TextView) findViewById(R.id.network);
      networkV.setText(dbSeries.network);
    }

    if (!TextUtils.isEmpty(dbSeries.contentRating)) {
      TextView contentRatingV = (TextView) findViewById(R.id.contentRating);
      contentRatingV.setText(dbSeries.contentRating);
    }

    TextView serieNameV = (TextView) findViewById(R.id.serieName);
    serieNameV.setText(dbSeries.name);

    ImageView posterThumbV = (ImageView) findViewById(R.id.posterThumb);
    try {
      String imageFilePath = (!TextUtils.isEmpty(dbSeries.mediumImageFilePath))
        ? dbSeries.mediumImageFilePath
        : (!TextUtils.isEmpty(dbSeries.smallImageFilePath))
            ? dbSeries.smallImageFilePath
            : null;

      if (!TextUtils.isEmpty(imageFilePath)) {
        Bitmap icon = BitmapFactory.decodeFile(imageFilePath);
        posterThumbV.setImageBitmap(icon);
      }
    }
    catch (Exception e) {}

    if (!genres.isEmpty()) {
      TextView genreV = (TextView) findViewById(R.id.genre);
      genreV.setText(genres.toString().replace("]", "").replace("[", ""));
      genreV.setVisibility(View.VISIBLE);
    }

    TextView ratingV = (TextView) findViewById(R.id.rating);
    if (dbSeries.reviewRating > 0)
      ratingV.setText("IMDb: " + String.format("%.02f", dbSeries.reviewRating));
    else
      ratingV.setText("IMDb Info");
    ratingV.setOnTouchListener(swipeDetect);

    if (!TextUtils.isEmpty(dbSeries.firstAired)) {
      TextView firstAiredV = (TextView) findViewById(R.id.firstAired);
      try {
        Date epDate = DateFormats.NORMALIZE_DATE.parse(dbSeries.firstAired);
        dbSeries.firstAired = DateFormats.DISPLAY_DATE.format(epDate);
      } catch (ParseException e) {
        Log.e(Constants.LOG_TAG, e.getMessage());
      }
      dbSeries.status = (!TextUtils.isEmpty(dbSeries.status))
        ? (" (" + dbSeries.status + ")")
        : "";
      firstAiredV.setText(dbSeries.firstAired + dbSeries.status);
      firstAiredV.setVisibility(View.VISIBLE);
    }

    if (!TextUtils.isEmpty(dbSeries.runtime)) {
      TextView runtimeV = (TextView) findViewById(R.id.runtime);
      runtimeV.setText(dbSeries.runtime + " " + getString(R.string.series_runtime_minutes));
      runtimeV.setVisibility(View.VISIBLE);
    }

    try {
      int epCount = db.getEpsWatched(serieId);
      if (epCount > 0) {
        TextView epCountV = (TextView) findViewById(R.id.countEpsWatched);
        epCountV.setText(epCount + " " + getString(R.string.messages_episodes) + " " + getString(R.string.messages_marked_seen));
        epCountV.setVisibility(View.VISIBLE);
      }
    } catch (Exception e) { e.printStackTrace(); }

    TextView serieOverviewV = (TextView) findViewById(R.id.serieOverview);
    serieOverviewV.setText(dbSeries.overview);

    if (!actors.isEmpty()) {
      TextView serieActorsV = (TextView) findViewById(R.id.actors);
      serieActorsV.setText(actors.toString().replace("]", "").replace("[", ""));
      serieActorsV.setOnTouchListener(swipeDetect);
      View actorsField = (View) findViewById(R.id.actorsField);
      actorsField.setOnTouchListener(swipeDetect);
      actorsField.setVisibility(View.VISIBLE);
    }

    Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
    imdbUri = (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
      ? "https://m.imdb.com/"
      : "imdb:///";
  }

  public void IMDbDetails(View v) {
    if (swipeDetect.value != 0) return;

    String uri = imdbUri
      + ((!TextUtils.isEmpty(dbSeries.imdbId) && dbSeries.imdbId.startsWith("tt"))
          ? ("title/"  + dbSeries.imdbId)
          : ("find?q=" + UrlUtils.encodeSerieNameForQuerystringValue(dbSeries.name))
        );

    Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
    imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(imdb);
  }

  public void IMDbNames(View v) {
    if (swipeDetect.value != 0) return;

    new AlertDialog.Builder(this)
      .setTitle(R.string.menu_search)
      .setItems(actors.toArray(new CharSequence[actors.size()]), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(imdbUri + "find?q=" + UrlUtils.encodeURIComponent(actors.get(item))));
          imdb.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(imdb);
        }
      })
      .show();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
  }
}
