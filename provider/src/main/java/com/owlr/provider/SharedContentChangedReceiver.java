package com.owlr.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SharedContentChangedReceiver extends BroadcastReceiver implements Types {

  public static final String SENDER_AUTHORITY_KEY = "sender_authority";

  /**
   * Send a broadcast with the dump of the master SharedPreference to do a dump of the masters
   * SharedPreference store to the slaves.
   */
  public static void sendBroadcast(@NonNull Context context, @NonNull Map<String, ?> data) {
    //We use the shared permission as the Action as they are both linked.
    String sharedPermission = MetaDataUtils.getSharedPermission(context);
    String senderAuthority = MetaDataUtils.getAppAuthority(context);
    Intent intent = new Intent(sharedPermission);
    intent.putExtra(SENDER_AUTHORITY_KEY, senderAuthority);
    putDataIntoIntent(intent, data);
    context.sendBroadcast(intent, sharedPermission);
  }

  public SharedContentChangedReceiver() {
  }

  @Override public void onReceive(Context context, Intent intent) {
    final String appAuthority = MetaDataUtils.getAppAuthority(context);
    Log.d("SharedProvider", "Received Data Changed Event, SentByAuth [" + appAuthority + "]");

    // Ignore me as I sent this!
    if (TextUtils.isEmpty(appAuthority) || appAuthority.equalsIgnoreCase(
        intent.getStringExtra(SENDER_AUTHORITY_KEY))) {
      Log.v("SharedProvider", "Skipped DataChange, SentByAuth [" + appAuthority + "]");
      return;
    }
    putIntentIntoSharedPreferences(context, intent.getExtras());
  }

  private static void putDataIntoIntent(Intent intent, Map<String, ?> data) {
    Set<? extends Map.Entry<String, ?>> entries = data.entrySet();
    for (Map.Entry<String, ?> entry : entries) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String) {
        intent.putExtra(key, (String) value);
      } else if (value instanceof Boolean) {
        intent.putExtra(key, (Boolean) value);
      } else if (value instanceof Long) {
        intent.putExtra(key, (Long) value);
      } else if (value instanceof Integer) {
        intent.putExtra(key, (Integer) value);
      } else if (value instanceof Float) {
        intent.putExtra(key, (Float) value);
      }
    }
  }

  private static void putIntentIntoSharedPreferences(Context context, Bundle bundle) {
    final Iterator<String> keysIter = bundle.keySet().iterator();
    final String appAuthority = MetaDataUtils.getAppAuthority(context);
    if (TextUtils.isEmpty(appAuthority)) return;
    final SharedSharedPreferences localPrefs = new SharedSharedPreferences(context, appAuthority);
    SharedSharedPreferences.SharedEditor edit = localPrefs.edit();

    Object value;
    String key;
    while (keysIter.hasNext()) {
      key = keysIter.next();
      switch (key) {
        case MASTER_KEY:
        case SENDER_AUTHORITY_KEY:
          continue;
      }
      value = bundle.get(key);
      if (value instanceof String) {
        edit.putString(key, (String) value);
      } else if (value instanceof Boolean) {
        edit.putBoolean(key, (Boolean) value);
      } else if (value instanceof Long) {
        edit.putLong(key, (Long) value);
      } else if (value instanceof Integer) {
        edit.putInt(key, (Integer) value);
      } else if (value instanceof Float) {
        edit.putFloat(key, (Float) value);
      }
    }
    edit.apply();
  }
}
