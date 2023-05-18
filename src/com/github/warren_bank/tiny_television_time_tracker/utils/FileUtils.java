package com.github.warren_bank.tiny_television_time_tracker.utils;

import com.github.warren_bank.tiny_television_time_tracker.common.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

  // --------------------------------------------------------------------------- common:

  private static File getFile(String path) {
    if (TextUtils.isEmpty(path)) return null;

    File file = new File(path);
    return file;
  }

  public static boolean exists(String path) {
    File file = FileUtils.getFile(path);
    return ((file != null) && file.exists());
  }

  // --------------------------------------------------------------------------- copy: generic file

  public static void copyFile(File source, File destination) throws IOException {
    if (source == null)
      throw new IOException("Source file is null.");

    if (destination == null)
      throw new IOException("Destination file is null.");

    if (source.equals(destination))
      throw new IOException("Source and Destination files refer to the same path.");

    FileChannel sourceCh = null, destinationCh = null;
    try {
      sourceCh = new FileInputStream(source).getChannel();
      if (destination.exists()) destination.delete();
      destination.createNewFile();
      destinationCh = new FileOutputStream(destination).getChannel();
      destinationCh.transferFrom(sourceCh, 0, sourceCh.size());
      destination.setLastModified(source.lastModified());
    }
    finally {
      if (sourceCh != null) {
        sourceCh.close();
      }
      if (destinationCh != null) {
        destinationCh.close();
      }
    }
  }

  // --------------------------------------------------------------------------- output: generic

  public static boolean writeToFile(String content, File file) {
    boolean append = false;
    return FileUtils.writeToFile(content, file, append);
  }

  public static boolean writeToFile(String content, File file, boolean append) {
    boolean result = true;

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, append);
      fos.write(content.getBytes());
    }
    catch(Exception e) {
      result = false;
    }

    try {
      if (fos != null) fos.close();
    }
    catch(Exception e) {}

    return result;
  }

  // ---------------------------------------------------------------------------

}
