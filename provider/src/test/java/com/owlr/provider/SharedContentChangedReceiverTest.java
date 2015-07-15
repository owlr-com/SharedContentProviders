package com.owlr.provider;

import android.os.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by chris on 15/07/15.
 * For SharedContentProviders.
 */
public class SharedContentChangedReceiverTest {

  @Mock Bundle context;

  @Before public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test public void testSendBroadcast() throws Exception {

  }

  @Test public void testOnReceive() throws Exception {

  }
}