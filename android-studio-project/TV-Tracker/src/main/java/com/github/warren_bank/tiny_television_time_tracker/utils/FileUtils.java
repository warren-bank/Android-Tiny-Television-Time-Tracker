package com.github.warren_bank.tiny_television_time_tracker.utils;

import com.github.warren_bank.tiny_television_time_tracker.R;
import com.github.warren_bank.tiny_television_time_tracker.common.DateFormats;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

  // --------------------------------------------------------------------------- common:

  private static File getFile(String path) {
    if (TextUtils.isEmpty(path)) return null;

    File file = new File(path);
    return file;
  }

  public static String getFileName(String path) {
    String fileName;
    File file = FileUtils.getFile(path);
    fileName = (file != null) ? file.getName() : "";
    fileName = !TextUtils.isEmpty(fileName) ? fileName : "";
    fileName = fileName.replace("/", "");
    return fileName;
  }

  public static String getFileName(URL url) {
    String path     = (url == null) ? null : url.getPath();
    String fileName = FileUtils.getFileName(path);
    return fileName;
  }

  public static boolean exists(String path) {
    File file = FileUtils.getFile(path);
    return ((file != null) && file.exists());
  }

  // --------------------------------------------------------------------------- get paths: for DB

  public static String getDatabaseFileName(Context context) {
    boolean auto = true;
    return FileUtils.getDatabaseFileName(context, auto);
  }

  public static String getDatabaseFileName(Context context, boolean auto) {
    boolean isPreUpdate = false;
    int     oldVersion  = -1;
    return FileUtils.getDatabaseFileName(context, auto, isPreUpdate, oldVersion);
  }

  public static String getDatabaseFileName(Context context, boolean auto, boolean isPreUpdate, int oldVersion) {
    context = context.getApplicationContext();
    String fileName = context.getString(R.string.database_file_name);
    if (!auto) {
      fileName += "." + DateFormats.getNormalizedDateTime();
    }
    if (isPreUpdate) {
      fileName += ".preupdate";
      if (oldVersion > 0) {
        fileName += "-v" + oldVersion;
      }
    }
    fileName += ".db";
    return fileName;
  }

  public static String getDatabaseFileNameForBackupVersion(String fileName, int backupVersion) {
    return fileName + backupVersion; // .db0, .db1, etc
  }

  public static String getDatabaseDirectoryPath(Context context) {
    context = context.getApplicationContext();
    String dirPath = context.getApplicationInfo().dataDir + "/" + context.getString(R.string.databases_directory_name);
    return dirPath;
  }

  public static String getDatabaseFilePath(Context context) {
    String dirPath  = FileUtils.getDatabaseDirectoryPath(context);
    String fileName = FileUtils.getDatabaseFileName(context);

    return dirPath + "/" + fileName;
  }

  // --------------------------------------------------------------------------- get paths: for resized images

  public static String getImageDirectoryPath(Context context, int resId_dirName) {
    String dirName = context.getString(resId_dirName);
    return FileUtils.getImageDirectoryPath(context, dirName);
  }

  public static String getImageDirectoryPath(Context context, String dirName) {
    context = context.getApplicationContext();
    String dirPath = context.getFilesDir().getAbsolutePath() + "/" + dirName;
    return dirPath;
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

  // --------------------------------------------------------------------------- copy: delegating to 'org.apache.commons.io.FileUtils'

  public static void copyURLToFile(URL source, File destination) throws IOException {
    org.apache.commons.io.FileUtils.copyURLToFile(source, destination);
  }

  // --------------------------------------------------------------------------- deletion: generic files and directories

  public static boolean deleteFile(String filePath) {
    File file = FileUtils.getFile(filePath);
    return ((file != null) && file.exists() && file.delete());
  }

  public static boolean deleteDirectoryContents(String dirPath, boolean useRecursion) {
    List<String> keepFileNames = null;
    return FileUtils.deleteDirectoryContents(dirPath, useRecursion, keepFileNames);
  }

  public static boolean deleteDirectoryContents(String dirPath, boolean useRecursion, List<String> keepFileNames) {
    File dir = FileUtils.getFile(dirPath);
    return ((dir != null) && dir.exists())
      ? FileUtils.deleteDirectoryContents(dir, useRecursion, keepFileNames)
      : false;
  }

  // delete contents of a directory with optional recursion
  public static boolean deleteDirectoryContents(File dir, boolean useRecursion, List<String> keepFileNames) {
    if (!dir.exists() || !dir.isDirectory()) return false;

    File[] files    = dir.listFiles();
    boolean success = true;
    boolean keep, is_empty;

    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          keep = (keepFileNames != null) && keepFileNames.contains(file.getName());

          if (!keep)
            success &= file.delete();
        }
        else if (file.isDirectory() && useRecursion) {
          success &= FileUtils.deleteDirectoryContents(file, useRecursion, keepFileNames);

          File[] children = file.listFiles();
          is_empty = (children == null) || (children.length == 0);

          if (is_empty)
            success &= file.delete();
        }
      }
    }
    return success;
  }

  // --------------------------------------------------------------------------- deletion: for DB

  public static boolean cleanDatabaseDirectory(Context context) {
    String dirPath             = FileUtils.getDatabaseDirectoryPath(context);
    String fileName            = FileUtils.getDatabaseFileName(context);
    boolean useRecursion       = false;
    List<String> keepFileNames = new ArrayList<String>();
    keepFileNames.add(fileName);

    return FileUtils.deleteDirectoryContents(dirPath, useRecursion, keepFileNames);
  }

  // --------------------------------------------------------------------------- deletion: for resized images

  public static boolean cleanImageDirectory(Context context, int resId_dirName) {
    String dirPath       = FileUtils.getImageDirectoryPath(context, resId_dirName);
    boolean useRecursion = true;

    return FileUtils.deleteDirectoryContents(dirPath, useRecursion);
  }

  public static boolean cleanImageDirectory(Context context, String dirName) {
    String dirPath       = FileUtils.getImageDirectoryPath(context, dirName);
    boolean useRecursion = true;

    return FileUtils.deleteDirectoryContents(dirPath, useRecursion);
  }

  public static boolean cleanAllImageDirectories(Context context) {
    boolean result = true;

    result &= FileUtils.cleanImageDirectory(context, R.string.images_small_directory_name);
    result &= FileUtils.cleanImageDirectory(context, R.string.images_medium_directory_name);

    return result;
  }

  // ---------------------------------------------------------------------------

}
