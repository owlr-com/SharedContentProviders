package com.owlr.provider;

import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chris on 14/07/15.
 * For SharedContentProviders.
 */
@RunWith(PowerMockRunner.class) @PrepareForTest({ TextUtils.class, Log.class })
public class SharedProviderFinderTest {

  @Mock SharedProviderFinder sharedProviderFinder;
  @Mock ContentResolver contentResolver;
  @Mock Cursor cursor;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    PowerMockito.mockStatic(TextUtils.class);
    PowerMockito.mockStatic(Log.class);
    //when(TextUtils.isEmpty(null)).thenReturn(true);
    when(TextUtils.isEmpty(anyString())).thenAnswer(new Answer<Boolean>() {
      @Override public Boolean answer(InvocationOnMock invocation) throws Throwable {
        final String s = invocation.getArgumentAt(0, String.class);
        return s == null || "".equalsIgnoreCase(s);
      }
    });
    when(Log.d(anyString(), anyString())).thenReturn(0);
    //new SharedProviderFinder(mock(Context.class), Pattern.compile("com\\.test\\.provider"),
    //    "com.test.PERMISSION", contentResolver);

    when(sharedProviderFinder.getContentUri(any(String.class))).thenReturn(Uri.EMPTY);
    when(sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver)).thenCallRealMethod();
  }

  @After public void tearDown() throws Exception {
    sharedProviderFinder = null;
    contentResolver = null;
    cursor = null;
  }

  @Test public void testFindProviders_nullProvider() throws Exception {
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    providerInfo.authority = null;
    // Pass in a bad ProviderInfo
    when(sharedProviderFinder.getInstalledProviders()).thenReturn(
        Collections.singletonList(providerInfo));
    when(sharedProviderFinder.getAuthorityMatcherPattern()).thenReturn(
        Pattern.compile("com\\.owlr\\.test"));
    when(sharedProviderFinder.findProviders()).thenCallRealMethod();

    List<ProviderInfo> results = sharedProviderFinder.findProviders();
    assertThat(results).isNotNull().isEmpty();
  }

  @Test public void testFindProviders_findTestProvider() throws Exception {
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    providerInfo.authority = "com.owlr.test";
    providerInfo.writePermission = "com.owlr.test.PERMISSION";

    // Pass in a normal ProviderInfo
    when(sharedProviderFinder.getInstalledProviders()).thenReturn(
        Collections.singletonList(providerInfo));
    when(sharedProviderFinder.getAuthorityMatcherPattern()).thenReturn(
        Pattern.compile("com\\.owlr\\.test"));
    when(sharedProviderFinder.getSharedPermission()).thenReturn("com.owlr.test.PERMISSION");
    when(sharedProviderFinder.findProviders()).thenCallRealMethod();

    List<ProviderInfo> results = sharedProviderFinder.findProviders();
    assertThat(results).isNotNull().hasSize(1);
  }

  @Test public void testFindProviders_badProviderDifferentPermission() throws Exception {
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    providerInfo.authority = "com.owlr.test";
    providerInfo.writePermission = "com.owlr.test.OTHER_PERMISSION";

    // Pass in a normal ProviderInfo
    when(sharedProviderFinder.getInstalledProviders()).thenReturn(
        Collections.singletonList(providerInfo));
    when(sharedProviderFinder.getAuthorityMatcherPattern()).thenReturn(
        Pattern.compile("com\\.owlr\\.test"));
    when(sharedProviderFinder.getSharedPermission()).thenReturn("com.owlr.test.PERMISSION");
    when(sharedProviderFinder.findProviders()).thenCallRealMethod();

    List<ProviderInfo> results = sharedProviderFinder.findProviders();
    assertThat(results).isNotNull().hasSize(0);
  }

  @Test public void testFindMasterProvider_throwException() throws Exception {
    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    try {
      sharedProviderFinder.findMasterProvider(null);
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class);
      return;
    }
    fail("Should throw IllegalStateException");
  }

  @Test public void testFindMasterProvider_throwException2() throws Exception {
    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    try {
      sharedProviderFinder.findMasterProvider(Collections.<ProviderInfo>emptyList());
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class);
      return;
    }
    fail("Should throw IllegalStateException");
  }

  @Test public void testFindMasterProvider_throwException3_nullProviders() throws Exception {
    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    providerInfo.authority = null;

    // Passed in provider is not Master it should get delegated.
    //when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(null);

    try {
      sharedProviderFinder.findMasterProvider(Collections.singletonList(providerInfo));
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class);
      return;
    }
    fail("Should throw IllegalStateException");
  }

  @Test public void testFindMasterProvider_assignThisProvider() throws Exception {
    final String auth = "com.owlr.test.provider";

    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    providerInfo.authority = auth;

    // Passed in provider is not Master it should get delegated.
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(null);
    when(sharedProviderFinder.delegateMaster(auth, true)).thenReturn(auth);
    final String masterProvider =
        sharedProviderFinder.findMasterProvider(Collections.singletonList(providerInfo));

    //Should of tried to delegate this.
    verify(sharedProviderFinder, never()).delegateMaster(auth, false);
    verify(sharedProviderFinder).delegateMaster(auth, true);
    assertThat(masterProvider).isEqualTo(auth);
  }

  @Test public void testFindMasterProvider_multipleProviders_assignThisProvider() throws Exception {
    final String auth = "com.owlr.test.provider";
    final String auth2 = "com.owlr.test2.provider";

    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    final ProviderInfo providerInfo2 = mock(ProviderInfo.class);
    providerInfo.authority = auth;
    providerInfo2.authority = auth2;

    // Passed in providers are not set to master.
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(null);
    when(sharedProviderFinder.delegateMaster(auth, true)).thenReturn(auth);
    final String masterProvider =
        sharedProviderFinder.findMasterProvider(Arrays.asList(providerInfo, providerInfo2));

    //Should of tried to delegate this.
    verify(sharedProviderFinder, never()).delegateMaster(auth, false);
    verify(sharedProviderFinder, never()).delegateMaster(auth2, false);
    verify(sharedProviderFinder).delegateMaster(auth, true);
    assertThat(masterProvider).isEqualTo(auth);
  }

  @Test public void testFindMasterProvider_secondIsAlreadyMaster() throws Exception {
    final String auth = "com.owlr.test.provider";
    final String auth2 = "com.owlr.test2.provider";

    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    final ProviderInfo providerInfo2 = mock(ProviderInfo.class);
    providerInfo.authority = auth;
    providerInfo2.authority = auth2;

    // Auth1 is not master, auth2 is.
    when(sharedProviderFinder.isProviderMaster(any(Uri.class),
        any(ContentResolver.class))).thenReturn(false, true);
    final String masterProvider =
        sharedProviderFinder.findMasterProvider(Arrays.asList(providerInfo, providerInfo2));

    //Should of tried to delegate this.
    verify(sharedProviderFinder, never()).delegateMaster(auth, false);
    verify(sharedProviderFinder, never()).delegateMaster(auth2, false);
    verify(sharedProviderFinder, never()).delegateMaster(auth2, true);
    assertThat(masterProvider).isEqualTo(auth2);
  }

  @Test public void testFindMasterProvider_bothAreMaster() throws Exception {
    final String auth = "com.owlr.test.provider";
    final String auth2 = "com.owlr.test2.provider";

    when(sharedProviderFinder.findMasterProvider(
        anyListOf(ProviderInfo.class))).thenCallRealMethod();
    final ProviderInfo providerInfo = mock(ProviderInfo.class);
    final ProviderInfo providerInfo2 = mock(ProviderInfo.class);
    providerInfo.authority = auth;
    providerInfo2.authority = auth2;

    // Auth1 is not master, auth2 is.
    when(sharedProviderFinder.isProviderMaster(any(Uri.class),
        any(ContentResolver.class))).thenReturn(true);
    final String masterProvider =
        sharedProviderFinder.findMasterProvider(Arrays.asList(providerInfo, providerInfo2));

    //Should of tried to delegate this.
    verify(sharedProviderFinder, never()).delegateMaster(auth, false);
    verify(sharedProviderFinder).delegateMaster(auth2, false);
    verify(sharedProviderFinder, never()).delegateMaster(auth, true);
    verify(sharedProviderFinder, never()).delegateMaster(auth2, true);
    assertThat(masterProvider).isEqualTo(auth);
  }

  @Test public void testIsProviderMaster_notSet_returnFalse() throws Exception {
    when(cursor.moveToFirst()).thenReturn(false);
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(cursor);
    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);
    assertThat(master).isFalse();
  }

  @Test public void testIsProviderMaster_isFalse_returnFalse() throws Exception {
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getInt(0)).thenReturn(0);
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(cursor);
    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);
    assertThat(master).isFalse();
  }

  @Test public void testIsProviderMaster_isTrue_returnTrue() throws Exception {
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getInt(0)).thenReturn(1);
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(cursor);
    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);
    assertThat(master).isTrue();
  }
}