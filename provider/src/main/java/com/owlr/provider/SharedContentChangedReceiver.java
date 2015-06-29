package com.owlr.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
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
    intent.getExtras();
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
}
