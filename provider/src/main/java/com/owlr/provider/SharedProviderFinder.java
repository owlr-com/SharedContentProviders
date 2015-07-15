package com.owlr.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.owlr.provider.SharedCursorUtils.getBooleanValue;

/**
 * Handles app interaction, you should never need to use this manually, more if you want to pull
 * out providers yourself.
 *
 * Created by chris on 11/06/15.
 */
public class SharedProviderFinder implements Types {

  private static SharedProviderFinder sharedProviderFinder;

  private static SharedProviderFinder initDefaultFinder(Context context) {
    String sharedPermission = MetaDataUtils.getSharedPermission(context);
    final String matcherPattern = MetaDataUtils.getSharedAuthorityMatcher(context);
    if (TextUtils.isEmpty(matcherPattern)) {
      throw new IllegalStateException(
          "You need to define the \"app_authority_matcher\" meta-data in your ApplicationManifest.xml");
    }
    Pattern pattern = Pattern.compile(matcherPattern);
    return new SharedProviderFinder(context, pattern, sharedPermission,
        context.getContentResolver());
  }

  /**
   * Get a pre-configured finder instance.
   */
  public static SharedProviderFinder get(Context context) {
    if (sharedProviderFinder == null) {
      sharedProviderFinder = initDefaultFinder(context);
    }
    return sharedProviderFinder;
  }

  private final Context context;
  private final Pattern authorityMatcherPattern;
  private final String sharedPermission;
  private final ContentResolver contentResolver;

  /**
   * Generally used for testing. You should use {@link #get(Context)}
   */
  protected SharedProviderFinder(Context context, Pattern authorityMatcherPattern,
      String sharedPermission, ContentResolver contentResolver) {
    this.context = context;
    this.authorityMatcherPattern = authorityMatcherPattern;
    this.sharedPermission = sharedPermission;
    this.contentResolver = contentResolver;
  }

  /**
   * Finds providers that can work with this app.
   *
   * @return this could be empty.
   */
  public List<ProviderInfo> findProviders() {
    final Pattern authorityMatcherPattern = getAuthorityMatcherPattern();
    Log.i("SharedProviders",
        "Find Authorities using: " + authorityMatcherPattern.pattern() + " Permission: "
            + getSharedPermission());

    final List<ProviderInfo> installedProviders = getInstalledProviders();
    final int count = installedProviders.size();
    final List<ProviderInfo> matchedProviders = new ArrayList<>(count);
    final String sharedPermission = getSharedPermission();

    Matcher matcher;
    ProviderInfo provider;
    for (int i = 0; i < count; i++) {
      provider = installedProviders.get(i);
      //#4 Fixes null auths (Facebook).
      if (TextUtils.isEmpty(provider.authority)) {
        continue;
      }
      matcher = authorityMatcherPattern.matcher(provider.authority);
      // #2/3 Skip if authority is null. Skip as we can't match it.
      if (matcher.matches() && sharedPermission.equalsIgnoreCase(provider.writePermission)) {
        Log.d("SharedProviders",
            "provider: " + provider.authority + " Matches: " + matcher.matches());
        // It's matched add this.
        matchedProviders.add(provider);
      }
    }
    Log.d("SharedProviders", "Found " + matchedProviders.size() + " providers.");
    return matchedProviders;
  }

  /**
   * Finds the master authority.
   *
   * @param providerInfos Non-null list of found providers to check
   * @return authority to use for the Uri for the master. That could be you as in we can delegate
   * ourselves as master.
   */
  public String findMasterProvider(List<ProviderInfo> providerInfos) {
    if (providerInfos == null || providerInfos.size() < 1) {
      throw new IllegalStateException(
          "There should be at least one Provider registered for this to work.");
    }
    ProviderInfo providerInfo;
    String authority;
    boolean isMaster;
    String masterAuthority = null;
    for (int i = 0, size = providerInfos.size(); i < size; i++) {
      providerInfo = providerInfos.get(i);
      authority = providerInfo.authority;
      //#3 Some shitty apps produce providers with null (or the user match is wrong)
      if (TextUtils.isEmpty(authority)) continue;

      isMaster = isProviderMaster(getContentUri(authority), contentResolver);
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
    masterAuthority = providerInfos.get(0).authority;
    if (TextUtils.isEmpty(masterAuthority)) {
      throw new IllegalStateException("There are no valid providers to delegate. "
          + "Are you sure you have your permissions and authorityMatcher set correctly");
    }
    return delegateMaster(masterAuthority, true);
  }

  /**
   * @see SharedProviderFinder#findMasterProvider(List)
   */
  public String findMasterProvider() {
    return findMasterProvider(findProviders());
  }

  /**
   * Calls the content provider and asks the remote provider if it has been assigned master.
   */
  boolean isProviderMaster(Uri contentUri, ContentResolver resolver) {
    return getBooleanValue(resolver.query(contentUri, null, null, null, null), false);
  }

  Uri getContentUri(String authority) {
    return SharedSharedPreferences.getContentUri(authority, MASTER_KEY, BOOLEAN_TYPE);
  }

  String getSharedPermission() {
    return sharedPermission;
  }

  Pattern getAuthorityMatcherPattern() {
    return authorityMatcherPattern;
  }

  /**
   * Tell this Provider if it should be master or not.
   */
  String delegateMaster(String authority, boolean isMaster) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(MASTER_KEY, isMaster);
    context.getContentResolver()
        .insert(SharedSharedPreferences.getContentUri(authority, KEY, TYPE), contentValues);
    return authority;
  }

  /**
   * Finds ALL installed Providers on the Device
   */
  List<ProviderInfo> getInstalledProviders() {
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
