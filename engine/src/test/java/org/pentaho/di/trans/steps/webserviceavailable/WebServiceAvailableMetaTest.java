/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.webserviceavailable;

import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;

public class WebServiceAvailableMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testLoadSave() throws KettleException {
    List<String> attributes = Arrays.asList( "URLField", "ConnectTimeOut", "ReadTimeOut", "ResultFieldName" );

    LoadSaveTester loadSaveTester = new LoadSaveTester( WebServiceAvailableMeta.class, attributes );

    loadSaveTester.testSerialization();
  }
}
