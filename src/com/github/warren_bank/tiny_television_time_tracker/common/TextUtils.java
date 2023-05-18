package com.github.warren_bank.tiny_television_time_tracker.common;

import java.util.Iterator;

public class TextUtils {
  public static boolean isEmpty(CharSequence str) {
    return str == null || str.length() == 0;
  }

  public static String join(CharSequence delimiter, Iterable tokens) {
    final Iterator<?> it = tokens.iterator();
    if (!it.hasNext()) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    sb.append(it.next());
    while (it.hasNext()) {
      sb.append(delimiter);
      sb.append(it.next());
    }
    return sb.toString();
  }
}
