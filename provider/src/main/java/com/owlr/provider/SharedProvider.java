package com.owlr.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import java.util.Map;

/**
 * Created by chris on 11/06/15.
 */
public class SharedProvider extends ContentProvider implements Types {

  public static String AUTHORITY;
  public static Uri BASE_URI;
  private static UriMatcher sUriMatcher = null;

  private static final int MATCH_DATA = UriMatcher.NO_MATCH + 1;

  /**
   * Sets up this content provider with this this apps AUTHORITY pulled from the meta tag.
   * This will do nothing if {@link #BASE_URI} is not null. It is safe to assume everything as been
   * initilized if {@link #BASE_URI} is not null.
   */
  static void init(Context context) {
    if (BASE_URI != null) return;
    AUTHORITY = MetaDataUtils.getAppAuthority(context);
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sUriMatcher.addURI(AUTHORITY, "*/*", MATCH_DATA);
    BASE_URI = Uri.parse("content://" + AUTHORITY);
  }

  /**
   * This is a normal shared prefs actually related locally to the app. We use this as our
   * DataStorage.
   * //TODO allow overriding with BackedSharedPrefs.
   */
  private SharedPreferences mSharedPrefs;

  @Override public boolean onCreate() {
    init(getContext());
    mSharedPrefs = getSharedPreferences(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    MatrixCursor cursor = null;
    switch (sUriMatcher.match(uri)) {
      case MATCH_DATA:
        final String key = uri.getPathSegments().get(0);
        final String type = uri.getPathSegments().get(1);
        cursor = new MatrixCursor(new String[] { key });
        if (!mSharedPrefs.contains(key)) return cursor;
        MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
        Object object = null;
        if (STRING_TYPE.equals(type)) {
          object = mSharedPrefs.getString(key, null);
        } else if (BOOLEAN_TYPE.equals(type)) {
          object = mSharedPrefs.getBoolean(key, false) ? 1 : 0;
        } else if (LONG_TYPE.equals(type)) {
          object = mSharedPrefs.getLong(key, 0l);
        } else if (INT_TYPE.equals(type)) {
          object = mSharedPrefs.getInt(key, 0);
        } else if (FLOAT_TYPE.equals(type)) {
          object = mSharedPrefs.getFloat(key, 0f);
        } else {
          throw new IllegalArgumentException("Unsupported type " + uri);
        }
        rowBuilder.add(object);
        break;
      default:
        throw new IllegalArgumentException("Unsupported uri " + uri);
    }
    return cursor;
  }

  @Override public String getType(Uri uri) {
    //This could be the authority, but thought a constant package name makes more sense.
    return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + BuildConfig.APPLICATION_ID + ".item";
  }

  @Override public Uri insert(Uri uri, ContentValues values) {
    switch (sUriMatcher.match(uri)) {
      case MATCH_DATA:
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        for (Map.Entry<String, Object> entry : values.valueSet()) {
          final Object value = entry.getValue();
          final String key = entry.getKey();
          if (value == null) {
            editor.remove(key);
          } else if (value instanceof String) {
            editor.putString(key, (String) value);
          } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
          } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
          } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
          } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
          } else {
            throw new IllegalArgumentException("Unsupported type " + uri);
          }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
          editor.apply();
        } else {
          editor.commit();
        }
        updateSlaves();
        break;
      default:
        throw new IllegalArgumentException("Unsupported uri " + uri);
    }
    return null;
  }

  @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
    switch (sUriMatcher.match(uri)) {
      case MATCH_DATA:
        mSharedPrefs.edit().clear().commit();
        updateSlaves();
        break;
      default:
        throw new IllegalArgumentException("Unsupported uri " + uri);
    }
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  /**
   * Asks your App's local Application object for a SharedPreferences you want to provide one.
   */
  private SharedPreferences getSharedPreferences(Context context) {
    SharedPreferences sharedPreferences = null;
    if (context.getApplicationContext() instanceof SharedPreferencesProducer) {
      sharedPreferences =
          ((SharedPreferencesProducer) context.getApplicationContext()).provideSharedPreferences();
    }
    if (sharedPreferences == null) {
      sharedPreferences = getDefaultSharedPreferences(context);
    }
    if (!sharedPreferences.contains("created")) {
      sharedPreferences.edit().putLong("created", System.currentTimeMillis()).apply();
    }
    return sharedPreferences;
  }

  private SharedPreferences getDefaultSharedPreferences(Context context) {
    return context.getSharedPreferences("local_shared_prefs", Context.MODE_PRIVATE);
  }

  private void updateSlaves() {
    SharedContentChangedReceiver.sendBroadcast(getContext(), mSharedPrefs.getAll());
  }
}
