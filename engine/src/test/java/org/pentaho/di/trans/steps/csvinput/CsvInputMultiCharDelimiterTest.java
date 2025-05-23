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


package org.pentaho.di.trans.steps.csvinput;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CsvInputMultiCharDelimiterTest extends CsvInputUnitTestBase {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private CsvInput csvInput;
  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper =
      StepMockUtil.getStepMockHelper( CsvInputMeta.class, "CsvInputMultiCharDelimiterTest" );
    csvInput = new CsvInput(
      stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"\r\n" );
  }

  @Test
  public void multiChar_hasEnclosures_HasNotNewLine() throws Exception {
    doTest( "\"value1\"delimiter\"value2\"delimiter\"value3\"" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNewLineDoubleEnd() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3\r\n" );
  }

  @Test
  public void multiChar_hasNotEnclosures_HasNotNewLine() throws Exception {
    doTest( "value1delimitervalue2delimitervalue3" );
  }

  private void doTest( String content ) throws Exception {
    RowSet output = new QueueRowSet();

    File tmp = createTestFile( ENCODING, content );
    try {
      CsvInputMeta meta = createMeta( tmp, createInputFileFields( "f1", "f2", "f3" ) );
      CsvInputData data = new CsvInputData();
      csvInput.init( meta, data );

      csvInput.addRowSetToOutputRowSets( output );

      try {
        csvInput.processRow( meta, data );
      } finally {
        csvInput.dispose( meta, data );
      }

    } finally {
      tmp.delete();
    }

    Object[] row = output.getRowImmediate();
    assertNotNull( row );
    assertEquals( "value1", row[ 0 ] );
    assertEquals( "value2", row[ 1 ] );
    assertEquals( "value3", row[ 2 ] );

    assertNull( output.getRowImmediate() );
  }

  @Override
  CsvInputMeta createMeta( File file, TextFileInputField[] fields ) {
    CsvInputMeta meta = super.createMeta( file, fields );
    meta.setDelimiter( "delimiter" );
    //Buffer cannot be less than ( delimiter length - 1) * 2 ) due to buffer resize logic in readBufferFromFile method
    //This buffer size causes special case where the delimiter spans past the end of the buffer.
    meta.setBufferSize( "16" );
    return meta;
  }
}
