package com.owlr.provider;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by chris on 29/06/15.
 * For project SharedProviders
 */
public final class MetaDataUtils {

  public static final String APP_AUTHORITY_MATCHER = "app_authority_matcher";
  public static final String APP_SHARED_PERMISSION = "app_shared_permission";
  public static final String APP_AUTHORITY = "app_authority";

  private MetaDataUtils() {
  }

  @Nullable public static String getSharedAuthorityMatcher(Context context) {
    Bundle metaData = getMetaData(context);
    return getMetaValue(metaData, APP_AUTHORITY_MATCHER);
  }

  @Nullable public static String getSharedPermission(Context context) {
    Bundle metaData = getMetaData(context);
    return getMetaValue(metaData, APP_SHARED_PERMISSION);
  }

  @Nullable public static String getAppAuthority(Context context) {
    Bundle metaData = getMetaData(context);
    if (metaData != null) {
      return metaData.getString(APP_AUTHORITY);
    }
    return null;
  }

  @Nullable protected static String getMetaValue(Bundle metaData, String appSharedPermission) {
    if (metaData != null) {
      return metaData.getString(appSharedPermission);
    }
    return null;
  }

  @Nullable protected static Bundle getMetaData(Context context) {
    try {
      ApplicationInfo applicationInfo = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      return applicationInfo.metaData;
    } catch (PackageManager.NameNotFoundException ignored) {
    }
    return null;
  }
}
