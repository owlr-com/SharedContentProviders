package com.owlr.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chris on 14/07/15.
 * For SharedContentProviders.
 */
public class SharedProviderFinderTest {

  private final String authority = "com.test.app1.provider";
  @Mock SharedProviderFinder sharedProviderFinder;
  @Mock ContentResolver contentResolver;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    //new SharedProviderFinder(mock(Context.class), Pattern.compile("com\\.test\\.provider"),
    //    "com.test.PERMISSION", contentResolver);

    when(sharedProviderFinder.getContentUri(any(String.class))).thenReturn(Uri.EMPTY);
    when(sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver)).thenCallRealMethod();
  }

  @After public void tearDown() throws Exception {
    sharedProviderFinder = null;
    contentResolver = null;
  }

  @Test public void testFindProviders() throws Exception {

  }

  @Test public void testFindMasterProvider() throws Exception {

  }

  @Test public void testFindMasterProvider1() throws Exception {

  }

  @Test public void testIsProviderMaster_notSet_returnFalse() throws Exception {
    final Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    setupContentProviderMock(cursor);

    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);

    assertThat(master).isFalse();
  }

  @Test public void testIsProviderMaster_isFalse_returnFalse() throws Exception {
    final Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getInt(0)).thenReturn(0);
    setupContentProviderMock(cursor);

    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);

    assertThat(master).isFalse();
  }

  @Test public void testIsProviderMaster_isTrue_returnTrue() throws Exception {
    final Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getInt(0)).thenReturn(1);
    setupContentProviderMock(cursor);

    final boolean master = sharedProviderFinder.isProviderMaster(Uri.EMPTY, contentResolver);
    verify(contentResolver).query(Uri.EMPTY, null, null, null, null);
    verify(cursor).moveToFirst();

    assertThat(master).isTrue();
  }

  private void setupContentProviderMock(Cursor cursor) {
    when(contentResolver.query(Uri.EMPTY, null, null, null, null)).thenReturn(cursor);
  }
}