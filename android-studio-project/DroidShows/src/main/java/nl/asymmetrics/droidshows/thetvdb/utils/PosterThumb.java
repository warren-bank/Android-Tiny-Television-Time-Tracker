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
   * post-condition: sToAdd.getPosterThumb() returns a String containing the filepath to a 100x100 Bitmap image, or null (if creation of the Bitmap failed)
   *
   * notes:
   *   * instances of TVShowItem are constructed in 2x places:
   *       - .ui.AddSerie
   *         * the constructor is passed both: (String) icon, (Bitmap) dicon
   *       - .utils.SQLiteStore
   *         * the constructor is passed only: (String) icon
   *         * getDIcon() return null
   *   * for all instances:
   *       - getIcon() returns a String containing the filepath to a 100x100 Bitmap image, or null (if creation of the Bitmap failed)
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

    // convert 100dp to pixels based on density of screen
    int show_icon_px = (int) (context.getResources().getInteger(R.integer.show_icon_dp) * context.getResources().getDisplayMetrics().density);
    Bitmap resizedBitmap = PosterThumb.decodeSampledBitmapFromFile(posterThumbPath, show_icon_px, show_icon_px);
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

  public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
    File file = new File(pathName);
    if (!file.exists()) return null;

    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(pathName, options);

    // Calculate inSampleSize
    options.inSampleSize = PosterThumb.calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeFile(pathName, options);

    // Crop bitmap to desired dimensions
    bm = PosterThumb.centerCropBitmap(bm, reqWidth, reqHeight);

    return bm;
  }

  public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if ((height > reqHeight) || (width > reqWidth)) {
      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while (((halfHeight / inSampleSize) > reqHeight) && ((halfWidth / inSampleSize) > reqWidth)) {
        inSampleSize *= 2;
      }
    }
    return inSampleSize;
  }

  public static Bitmap centerCropBitmap(Bitmap source, int reqWidth, int reqHeight) {
    int sourceWidth = source.getWidth();
    int sourceHeight = source.getHeight();

    if (sourceWidth < reqWidth)
      reqWidth = sourceWidth;

    if (sourceHeight < reqHeight)
      reqHeight = sourceHeight;

    if ((sourceWidth == reqWidth) && (sourceHeight == reqHeight))
      return source;

    int offset_x = (int) (sourceWidth  - reqWidth )/2;
    int offset_y = (int) (sourceHeight - reqHeight)/2;

    return Bitmap.createBitmap(source, offset_x, offset_y, reqWidth, reqHeight);
  }

}
