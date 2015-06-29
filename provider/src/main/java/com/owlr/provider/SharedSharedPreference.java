package com.owlr.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static com.owlr.provider.SharedCursorUtils.getBooleanValue;
import static com.owlr.provider.SharedCursorUtils.getFloatValue;
import static com.owlr.provider.SharedCursorUtils.getIntValue;
import static com.owlr.provider.SharedCursorUtils.getLongValue;
import static com.owlr.provider.SharedCursorUtils.getStringValue;

/**
 * Created by chris on 18/06/15.
 */
public class SharedSharedPreference implements SharedPreferences, Types {

  /**
   * Builds a Uri for the any provider authority content you are trying to access, this makes the
   * entire content provider flexible around different key/value/types at runtime.
   *
   * This allows other shared providers to no care about what they store, just just do.
   */
  static Uri getContentUri(String authority, String key, String type) {
    return Uri.parse("content://" + authority).buildUpon().appendPath(key).appendPath(type).build();
  }

  private final Context context;
  private String authority;

  public SharedSharedPreference(Context context) {
    this.context = context.getApplicationContext();
    refreshAuthority();
  }

  public void refreshAuthority() {
    SharedProviderFinder finder = SharedProviderFinder.get(context);
    authority = finder.findMasterProvider(finder.findProviders());
  }

  @Override public Map<String, ?> getAll() {
    return null;
  }

  @Nullable @Override public Set<String> getStringSet(String key, Set<String> defValues) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override public String getString(String key, String def) {
    Cursor cursor = context.getContentResolver()
        .query(getContentUri(authority, key, STRING_TYPE), null, null, null, null);
    return getStringValue(cursor, def);
  }

  public long getLong(String key, long def) {
    Cursor cursor = context.getContentResolver()
        .query(getContentUri(authority, key, LONG_TYPE), null, null, null, null);
    return getLongValue(cursor, def);
  }

  public float getFloat(String key, float def) {
    Cursor cursor = context.getContentResolver()
        .query(getContentUri(authority, key, FLOAT_TYPE), null, null, null, null);
    return getFloatValue(cursor, def);
  }

  public boolean getBoolean(String key, boolean def) {
    Cursor cursor = context.getContentResolver()
        .query(getContentUri(authority, key, BOOLEAN_TYPE), null, null, null, null);
    return getBooleanValue(cursor, def);
  }

  public int getInt(String key, int def) {
    Cursor cursor = context.getContentResolver()
        .query(getContentUri(authority, key, INT_TYPE), null, null, null, null);
    return getIntValue(cursor, def);
  }

  @Override public boolean contains(String key) {
    return false;
  }

  @Override public SharedEditor edit() {
    return new SharedEditor(context, authority);
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
  }

  @Override public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
  }

  public static class SharedEditor implements SharedPreferences.Editor {

    private final Context context;
    private final String authority;

    private SharedEditor(Context context, String authority) {
      this.context = context;
      this.authority = authority;
    }

    private ContentValues values = new ContentValues();

    @Override public void apply() {
      context.getContentResolver().insert(getContentUri(authority, KEY, TYPE), values);
      values.clear();
    }

    @Override public boolean commit() {
      apply();
      return true;
    }

    @Override public SharedEditor putString(String key, String value) {
      values.put(key, value);
      return this;
    }

    @Override public SharedEditor putStringSet(String key, Set<String> values) {
      throw new UnsupportedOperationException("Not implemented.");
    }

    @Override public SharedEditor putLong(String key, long value) {
      values.put(key, value);
      return this;
    }

    @Override public SharedEditor putBoolean(String key, boolean value) {
      values.put(key, value);
      return this;
    }

    @Override public SharedEditor putInt(String key, int value) {
      values.put(key, value);
      return this;
    }

    @Override public SharedEditor putFloat(String key, float value) {
      values.put(key, value);
      return this;
    }

    @Override public SharedEditor remove(String key) {
      values.putNull(key);
      return this;
    }

    /**
     * Call content provider method immediately. apply or commit is not required for this case
     * So it's sync method.
     */
    @Override public SharedEditor clear() {
      context.getContentResolver().delete(getContentUri(authority, KEY, TYPE), null, null);
      return this;
    }
  }
}
