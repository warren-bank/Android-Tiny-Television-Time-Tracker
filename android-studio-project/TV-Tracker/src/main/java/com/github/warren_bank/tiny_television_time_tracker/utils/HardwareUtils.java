package com.github.warren_bank.tiny_television_time_tracker.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class HardwareUtils {

  public static DisplayMetrics getScreenDisplayMetrics(Context context) {
    DisplayMetrics metrics      = new DisplayMetrics();
    WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    windowManager.getDefaultDisplay().getMetrics(metrics);
    return metrics;
  }

  public static float getScreenDensity(Context context) {
    DisplayMetrics metrics = HardwareUtils.getScreenDisplayMetrics(context);
    return metrics.density;
  }

  public static int convertDpToPx(Context context, int dp) {
    float density = HardwareUtils.getScreenDensity(context);
    int px = (int) (dp * density);
    return px;
  }

  public static int getScreenWidthPx(Context context) {
    boolean landscape = false; // default to portrait orientation
    return HardwareUtils.getScreenWidthPx(context, landscape);
  }

  public static int getScreenWidthPx(Context context, boolean landscape) {
    DisplayMetrics metrics = HardwareUtils.getScreenDisplayMetrics(context);

    int currentWidth  = metrics.widthPixels;
    int currentHeight = metrics.heightPixels;

    return landscape
      ? Math.max(currentWidth, currentHeight)
      : Math.min(currentWidth, currentHeight);
  }

  public static int getFractionOfScreenWidthPx(Context context, float fraction) {
    boolean landscape = false; // default to portrait orientation
    return HardwareUtils.getFractionOfScreenWidthPx(context, fraction, landscape);
  }

  public static int getFractionOfScreenWidthPx(Context context, float fraction, boolean landscape) {
    int widthPx    = HardwareUtils.getScreenWidthPx(context, landscape);
    int fractionPx = (int) (widthPx * fraction);
    return fractionPx;
  }

}
