package com.github.warren_bank.tiny_television_time_tracker.utils;

import com.github.warren_bank.tiny_television_time_tracker.common.Constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {

  // ---------------------------------------------------------------------------
  // Download image from URL.
  // Resize by width and maintain aspect ratio.
  // Save to filesystem.
  // Return filepath.
  // ---------------------------------------------------------------------------

  public static String saveImage(Context context, String largeImageUrl, int resId_dirName, int resId_widthDp, int maxWidthPx) {
    URL    imageURL;
    String imageFilePath;
    File   imageFile;

    try {
      imageURL      = new URL(largeImageUrl);
      imageFilePath = FileUtils.getImageDirectoryPath(context, resId_dirName) + "/" + FileUtils.getFileName(imageURL);
    }
    catch (MalformedURLException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
      return "";
    }

    try {
      imageFile = new File(imageFilePath);
      FileUtils.copyURLToFile(imageURL, imageFile);
    }
    catch (IOException e) {
      Log.e(Constants.LOG_TAG, e.getMessage());
      return "";
    }

    Bitmap resizedBitmap = ImageUtils.decodeSampledBitmapFromFile(imageFilePath, ImageUtils.getIconWidthPx(context, resId_widthDp, maxWidthPx));
    if (resizedBitmap == null) {
      imageFile.delete();
      return "";
    }

    OutputStream fOut = null;
    try {
      fOut = new FileOutputStream(imageFile, false);
      resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
      fOut.flush();
      fOut.close();
    }
    catch (IOException e) {
      imageFile.delete();
      Log.e(Constants.LOG_TAG, e.getMessage());
      return "";
    }
    resizedBitmap.recycle();
    System.gc();
    resizedBitmap = null;
    return imageFilePath;
  }

  // ---------------------------------------------------------------------------
  // Load integer from resource ID to represent the width of an image in "dp" units.
  // Convert to "px" units using density of display screen
  // ---------------------------------------------------------------------------

  public static int getIconWidthPx(Context context, int resId_widthDp) {
    int maxWidthPx = 0;
    return ImageUtils.getIconWidthPx(context, resId_widthDp, maxWidthPx);
  }

  public static int getIconWidthPx(Context context, int resId_widthDp, int maxWidthPx) {
    int widthDp = context.getResources().getInteger(resId_widthDp);
    int widthPx = HardwareUtils.convertDpToPx(context, widthDp);

    if ((maxWidthPx > 0) && (widthPx > maxWidthPx)) {
      widthPx = maxWidthPx;
    }

    return widthPx;
  }

  // ---------------------------------------------------------------------------
  // Load image from resource ID.
  // Resize by width and maintain aspect ratio.
  // Return as Bitmap.
  // ---------------------------------------------------------------------------

  public static Bitmap decodeSampledBitmapFromResource(Context context, int resId, int reqWidth) {
    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(context.getResources(), resId, options);

    // Calculate inSampleSize
    options.inSampleSize = ImageUtils.calculateInSampleSize(options, reqWidth);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeResource(context.getResources(), resId, options);

    // Resize bitmap to desired width while maintaining original aspect ratio
    bm = ImageUtils.resizeBitmap(bm, options, reqWidth);

    return bm;
  }

  // ---------------------------------------------------------------------------
  // Load image from filepath.
  // Resize by width and maintain aspect ratio.
  // Return as Bitmap.
  // ---------------------------------------------------------------------------

  private static Bitmap decodeSampledBitmapFromFile(String imageFilePath, int reqWidth) {
    File file = new File(imageFilePath);
    if (!file.exists()) return null;

    Bitmap bm;

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imageFilePath, options);

    // Calculate inSampleSize
    options.inSampleSize = ImageUtils.calculateInSampleSize(options, reqWidth);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    bm = BitmapFactory.decodeFile(imageFilePath, options);

    // Resize bitmap to desired width while maintaining original aspect ratio
    bm = ImageUtils.resizeBitmap(bm, options, reqWidth);

    return bm;
  }

  // ---------------------------------------------------------------------------
  // Calculate the largest inSampleSize value,
  // which is a power of 2,
  // which will produce a subsampled image having a width >= reqWidth.
  // ---------------------------------------------------------------------------

  private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
    // width of original image
    final int width = options.outWidth;

    int inSampleSize = 1;

    if (width > reqWidth) {
      final int halfWidth = width / 2;

      while ((halfWidth / inSampleSize) > reqWidth) {
        inSampleSize *= 2;
      }
    }
    return inSampleSize;
  }

  // ---------------------------------------------------------------------------
  // Use bilinear filtering to scale down an image to an exact width.
  // Maintain aspect ratio.
  // Return as Bitmap.
  // ---------------------------------------------------------------------------

  private static Bitmap resizeBitmap(Bitmap source, BitmapFactory.Options options, int reqWidth) {
    // width of subsampled image
    int widthIcon = source.getWidth();

    if (widthIcon <= reqWidth)
      return source;

    // dimensions of subsampled image
    final float widthPoster  = (float) options.outWidth;
    final float heightPoster = (float) options.outHeight;
    final float aspectRatio  = heightPoster / widthPoster;

    // height of resized image
    final int reqHeight = (int) (aspectRatio * reqWidth);

    return Bitmap.createScaledBitmap(source, reqWidth, reqHeight, true);
  }

}
