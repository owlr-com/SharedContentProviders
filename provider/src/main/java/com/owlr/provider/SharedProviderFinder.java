package com.owlr.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.owlr.provider.SharedCursorUtils.getBooleanValue;
import static com.owlr.provider.SharedSharedPreferences.getContentUri;

/**
 * Handles app interaction, you should never need to use this manually, more if you want to pull
 * out providers yourself.
 *
 * Created by chris on 11/06/15.
 */
public class SharedProviderFinder implements Types {

  private static SharedProviderFinder sharedProviderFinder;

  public static SharedProviderFinder get(Context context) {
    if (sharedProviderFinder == null) {
      sharedProviderFinder = new SharedProviderFinder(context);
    }
    return sharedProviderFinder;
  }

  private final Pattern mPattern;
  private final Context context;

  private SharedProviderFinder(Context context) {
    this.context = context;
    final String matcherPattern = MetaDataUtils.getSharedAuthorityMatcher(context);
    if (TextUtils.isEmpty(matcherPattern)) {
      throw new IllegalStateException(
          "You need to define the \"app_authority_matcher\" meta-data in your ApplicationManifest.xml");
    }
    mPattern = Pattern.compile(matcherPattern);
  }

  /**
   * Finds providers that can work with this app.
   *
   * @return this could be empty.
   */
  public List<ProviderInfo> findProviders() {
    Log.i("SharedProviders", "Find Authorities using: " + mPattern.pattern());

    final List<ProviderInfo> installedProviders = getInstalledProviders();
    final ArrayList<ProviderInfo> sharedProviders = new ArrayList<>(installedProviders);
    for (ProviderInfo provider : installedProviders) {
      final Matcher matcher = mPattern.matcher(provider.authority);
      // #2/3 Skip if authority is null. Skip as we can't match it.
      if (!matcher.matches() || TextUtils.isEmpty(provider.authority)) {
        // No match, remove it from sharedProviders.
        sharedProviders.remove(provider);
      } else {
        Log.d("SharedProviders",
            "provider: " + provider.authority + " Matches: " + matcher.matches());
      }
    }
    Log.d("SharedProviders", "Found " + sharedProviders.size() + " providers.");
    return sharedProviders;
  }

  /**
   * Finds the master authority.
   *
   * @return authority to use for the Uri for the master. That could be you as in we can delegate
   * ourselves as master.
   */
  public String findMasterProvider(List<ProviderInfo> providerInfos) {
    if (providerInfos.size() < 1) {
      throw new IllegalStateException(
          "There should be at least one Provider registered for this to work.");
    }
    String authority;
    boolean isMaster;
    ProviderInfo providerInfo;
    String masterAuthority = null;
    for (int i = 0; i < providerInfos.size(); i++) {
      providerInfo = providerInfos.get(i);
      authority = providerInfo.authority;
      //#3 Some shitty apps produce providers with null (or the user match is wrong)
      if (TextUtils.isEmpty(authority)) continue;
      isMaster = getBooleanValue(context.getContentResolver()
              .query(getContentUri(authority, MASTER_KEY, BOOLEAN_TYPE), null, null, null, null),
          false);
      Log.d("SharedProviders", "Auth " + authority + " isMaster: " + isMaster);
      //Select the first Master Auth then we un delegate the rest.
      if (isMaster && TextUtils.isEmpty(masterAuthority)) {
        masterAuthority = authority;
      } else if (isMaster && !TextUtils.isEmpty(masterAuthority)) {
        // We un-delegate other masters. This can be if for some reason other masters were delegated
        // by themselves..
        Log.d("SharedProviders", "Un-Delegate Auth: " + authority);
        delegateMaster(authority, false);
      }
    }
    if (!TextUtils.isEmpty(masterAuthority)) {
      return masterAuthority;
    }
    // If we reach here, then there are no masters so we delegate one. (Top of the list in the current impl)
    return delegateMaster(providerInfos.get(0).authority, true);
  }

  /**
   * @see SharedProviderFinder#findMasterProvider(List)
   */
  public String findMasterProvider() {
    return findMasterProvider(findProviders());
  }

  /**
   * Tell this Provider if it should be master or not.
   */
  private String delegateMaster(String authority, boolean isMaster) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(MASTER_KEY, isMaster);
    context.getContentResolver().insert(getContentUri(authority, KEY, TYPE), contentValues);
    return authority;
  }

  /**
   * Finds ALL installed Providers on the Device
   */
  private List<ProviderInfo> getInstalledProviders() {
    final List<PackageInfo> installedPackages =
        context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
    final List<ProviderInfo> providerInfoList = new ArrayList<>();
    PackageInfo packageInfo;
    for (int i = 0; i < installedPackages.size(); i++) {
      packageInfo = installedPackages.get(i);
      if (packageInfo.providers != null) {
        Collections.addAll(providerInfoList, packageInfo.providers);
      }
    }
    return providerInfoList;
  }
}
