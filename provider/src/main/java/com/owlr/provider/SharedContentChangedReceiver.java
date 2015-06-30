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

  public static void sendBroadcast(Context context, @NonNull Map<String, ?> data) {
    //We use the shared permission as the Action as they are both linked.
    String sharedPermission = MetaDataUtils.getSharedPermission(context);
    Intent intent = new Intent(sharedPermission);
    putDataIntoIntent(intent, data);
    context.sendBroadcast(intent, sharedPermission);
  }

  public SharedContentChangedReceiver() {
  }

  @Override public void onReceive(Context context, Intent intent) {
    Log.d("SharedProvider", "Received Data Changed Event");
    putIntentIntoSharedPreferences(context, intent.getExtras());
  }

  private static void putDataIntoIntent(Intent intent, Map<String, ?> data) {
    Set<? extends Map.Entry<String, ?>> entries = data.entrySet();
    for (Map.Entry<String, ?> entry : entries) {
      String key = entry.getKey();
      if (MASTER_KEY.equalsIgnoreCase(key)) {
        continue;
      }
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
    final SharedSharedPreference localPrefs = new SharedSharedPreference(context, appAuthority);
    SharedSharedPreference.SharedEditor edit = localPrefs.edit();

    Object value;
    String key;
    while (keysIter.hasNext()) {
      key = keysIter.next();
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
