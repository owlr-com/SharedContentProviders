package com.owlr.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SharedProviderBootReceiver extends BroadcastReceiver {
  public SharedProviderBootReceiver() {
  }

  @Override public void onReceive(Context context, Intent intent) {
    SharedProviderFinder finder = SharedProviderFinder.get(context);
    String authority = finder.findMasterProvider();

  }
}
