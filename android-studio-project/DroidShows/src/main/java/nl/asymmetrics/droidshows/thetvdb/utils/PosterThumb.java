package nl.asymmetrics.droidshows.thetvdb.utils;

import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.thetvdb.model.Serie;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class PosterThumb {

  /*
   * post-condition: sToAdd.getPosterThumb() returns a String containing the filepath to a Bitmap image
   *                 that is 100dp wide with a height that preserves its original aspect ratio,
   *                 or null (if creation of the Bitmap failed)
   *
   * notes:
   *   * instances of TVShowItem are constructed in 2x places:
   *       - .ui.AddSerie
   *         * the constructor is passed both: (String) icon, (Bitmap) dicon
   *       - .utils.SQLiteStore
   *         * the constructor is passed only: (String) icon
   *         * getDIcon() return null
   *   * for all instances:
   *       - getIcon() returns a String containing the filepath to a Bitmap image,
   *         that is 100dp wide with a height that preserves its original aspect ratio,
   *         or null (if creation of the Bitmap failed)
   */

  public static void save(Context context, Serie sToAdd, String TAG) {
    if (TAG == null) TAG = "DroidShows";

    Log.d(TAG, "Saving thumbnail of poster for: "+ sToAdd.getSerieName());

    // initialize fields
    sToAdd.setPosterInCache("false");
    sToAdd.setPosterThumb("");

    // get the poster and save it in cache
    String poster = sToAdd.getPoster();
    URL posterURL = null;
    String posterThumbPath = null;
    try {
      posterURL = new URL(poster);
      posterThumbPath = context.getFilesDir().getAbsolutePath() +"/thumbs"+ posterURL.getFile().toString();
    } catch (MalformedURLException e) {
      Log.e(TAG, sToAdd.getSerieName() +" doesn't have a poster URL");
      e.printStackTrace();
      return;
    }

    File posterThumbFile = null;
    try {
      posterThumbFile = new File(posterThumbPath);
      FileUtils.copyURLToFile(posterURL, posterThumbFile);
    } catch (IOException e) {
      Log.e(TAG, "Could not download poster: "+ posterURL);
      e.printStackTrace();
      return;
    }

    Bitmap resizedBitmap = PosterThumb.decodeSampledBitmapFromFile(posterThumbPath, PosterThumb.getIconWidthPx(context));
    if (resizedBitmap == null) {
      Log.e(TAG, "Corrupt or unknown poster file type: "+ posterThumbPath);
      return;
    }

    OutputStream fOut = null;
    try {
      fOut = new FileOutputStream(posterThumbFile, false);
      resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
      fOut.flush();
      fOut.close();
      sToAdd.setPosterInCache("true");
      sToAdd.setPosterThumb(posterThumbPath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    resizedBitmap.recycle();
    System.gc();
    resizedBitmap = null;
    return;
  }

  // ----- public, but only used privately -----

  public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth) {
    File file = new File(pathName);
    if (!file.exists()) return null;

    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(pathName, options);

    // Calculate inSampleSize
    options.inSampleSize = PosterThumb.calculateInSampleSize(options, reqWidth);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeFile(pathName, options);

    // Resize bitmap to desired width while maintaining original aspect ratio
    bm = PosterThumb.resizeBitmap(bm, options, reqWidth);

    return bm;
  }

  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
    // width of original image
    final int width = options.outWidth;

    int inSampleSize = 1;

    if (width > reqWidth) {
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps width larger than the requested width.
      while ((halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2;
      }
    }
    return inSampleSize;
  }

  public static Bitmap resizeBitmap(Bitmap source, BitmapFactory.Options options, int reqWidth) {
    // width of subsampled image
    int widthIcon = source.getWidth();

    if (widthIcon <= reqWidth)
      return source;

    // dimensions of original image
    final int   widthPoster  = options.outWidth;
    final int   heightPoster = options.outHeight;
    final float aspectRatio  = heightPoster / widthPoster;

    // height of resized image
    final int reqHeight = (int) (aspectRatio * reqWidth);

    return Bitmap.createScaledBitmap(source, reqWidth, reqHeight, true);
  }

  // ----- public -----

  public static int getIconWidthPx(Context context) {
    // convert 100dp to pixels based on density of screen
    int series_icon_width_px = (int) (context.getResources().getInteger(R.integer.series_icon_width_dp) * context.getResources().getDisplayMetrics().density);
    return series_icon_width_px;
  }

  public static Bitmap decodeSampledBitmapFromResource(Context context, int resId, int reqWidth) {
    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(context.getResources(), resId, options);

    // Calculate inSampleSize
    options.inSampleSize = PosterThumb.calculateInSampleSize(options, reqWidth);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeResource(context.getResources(), resId, options);

    // Resize bitmap to desired width while maintaining original aspect ratio
    bm = PosterThumb.resizeBitmap(bm, options, reqWidth);

    return bm;
  }

}
