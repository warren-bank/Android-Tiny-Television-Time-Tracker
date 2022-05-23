package nl.asymmetrics.droidshows.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//import android.util.Log;
public class Utils
{
  public static boolean isNetworkAvailable(Activity mActivity) {
    Context context = mActivity.getApplicationContext();
    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      // Log.d(TAG," connectivity is null");
      return false;
    } else {
      // Log.d(TAG," connectivity is not null");
      NetworkInfo[] info = connectivity.getAllNetworkInfo();
      if (info != null) {
        // Log.d(TAG," info is not null");
        for (int i = 0; i < info.length; i++) {
          if (info[i].getState() == NetworkInfo.State.CONNECTED) {
            return true;
          } else {
            // Log.d(TAG," info["+i+"] is not connected");
          }
        }
      } else {
        // Log.d(TAG," info is null");
      }
    }
    return false;
  }

  public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
    File file = new File(pathName);
    if (!file.exists()) return null;

    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(pathName, options);

    // Calculate inSampleSize
    options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeFile(pathName, options);

    // Crop bitmap to desired dimensions
    bm = Utils.centerCropBitmap(bm, reqWidth, reqHeight);

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
