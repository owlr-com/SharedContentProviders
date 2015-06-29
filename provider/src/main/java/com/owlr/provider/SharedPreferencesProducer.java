package com.owlr.provider;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

/**
 * Implement this on the Application class to provide a custom shared prefs to the content
 * provider.
 *
 * Created by chris on 29/06/15.
 * For project SharedProviders
 */
public interface SharedPreferencesProducer {

  /**
   * Due to the nature of SharedPrefs, its difficult to pass your custom shared prefs to the
   * content provider. It's safer for it to request if from you.
   *
   * @return SharedPreference, can be null
   */
  @Nullable SharedPreferences provideSharedPreferences();
}
