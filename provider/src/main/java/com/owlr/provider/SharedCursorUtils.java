package com.owlr.provider;

import android.database.Cursor;

/**
 * Created by chris on 29/06/15.
 * For project SharedProviders
 */
public final class SharedCursorUtils {
  private SharedCursorUtils() {
  }

  public static String getStringValue(Cursor cursor, String def) {
    if (cursor == null) return def;
    String value = def;
    if (cursor.moveToFirst()) {
      value = cursor.getString(0);
    }
    cursor.close();
    return value;
  }

  public static boolean getBooleanValue(Cursor cursor, boolean def) {
    if (cursor == null) return def;
    boolean value = def;
    if (cursor.moveToFirst()) {
      value = cursor.getInt(0) > 0;
    }
    cursor.close();
    return value;
  }

  public static int getIntValue(Cursor cursor, int def) {
    if (cursor == null) return def;
    int value = def;
    if (cursor.moveToFirst()) {
      value = cursor.getInt(0);
    }
    cursor.close();
    return value;
  }

  public static long getLongValue(Cursor cursor, long def) {
    if (cursor == null) return def;
    long value = def;
    if (cursor.moveToFirst()) {
      value = cursor.getLong(0);
    }
    cursor.close();
    return value;
  }

  public static float getFloatValue(Cursor cursor, float def) {
    if (cursor == null) return def;
    float value = def;
    if (cursor.moveToFirst()) {
      value = cursor.getFloat(0);
    }
    cursor.close();
    return value;
  }
}
