package com.owlr.provider;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by chris on 15/07/15.
 * For SharedContentProviders.
 */
public class MetaDataUtilsTest {

  @Mock Bundle bundle;
  @Mock Context context;
  @Mock PackageManager packageManager;
  @Mock ApplicationInfo applicationInfo;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    setupContext();
  }

  /**
   * Lets you use the mock bundle.
   *
   * @throws PackageManager.NameNotFoundException
   */
  private void setupContext() throws PackageManager.NameNotFoundException {
    when(context.getPackageName()).thenReturn("com.owlr.test");
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getApplicationInfo("com.owlr.test",
        PackageManager.GET_META_DATA)).thenReturn(applicationInfo);
    applicationInfo.metaData = bundle;
  }

  @Test public void testGetSharedAuthorityMatcher() throws Exception {
    String expected = "com\\.owlr";
    when(bundle.getString(MetaDataUtils.APP_AUTHORITY_MATCHER)).thenReturn(expected);
    assertThat(MetaDataUtils.getSharedAuthorityMatcher(context)).isEqualTo(expected);
  }

  @Test public void testGetSharedPermission() throws Exception {
    String expected = "com.owlr.PERMISSION";
    when(bundle.getString(MetaDataUtils.APP_SHARED_PERMISSION)).thenReturn(expected);
    assertThat(MetaDataUtils.getSharedPermission(context)).isEqualTo(expected);
  }

  @Test public void testGetAppAuthority() throws Exception {
    String expected = "com.owlr";
    when(bundle.getString(MetaDataUtils.APP_AUTHORITY)).thenReturn(expected);
    assertThat(MetaDataUtils.getAppAuthority(context)).isEqualTo(expected);
  }

  @Test public void testGetMetaValue() throws Exception {
    when(bundle.getString("app_key")).thenReturn("result");
    final String result = MetaDataUtils.getMetaValue(bundle, "app_key");
    assertThat(result).isEqualTo("result");
  }

  @Test public void testGetMetaData() throws Exception {
    assertThat(MetaDataUtils.getMetaData(context)).isEqualTo(bundle);
  }
}