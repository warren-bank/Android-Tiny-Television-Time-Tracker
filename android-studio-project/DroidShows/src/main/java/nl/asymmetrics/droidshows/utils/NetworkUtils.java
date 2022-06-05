package nl.asymmetrics.droidshows.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

  public static boolean isNetworkAvailable(Activity mActivity) {
    Context context = mActivity.getApplicationContext();
    ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      return false;
    }
    else {
      NetworkInfo[] info = connectivity.getAllNetworkInfo();
      if (info != null) {
        for (int i = 0; i < info.length; i++) {
          if (info[i].getState() == NetworkInfo.State.CONNECTED) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
