/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

/** This class tests the internal functionality of CompilerErrorModel using a dummy implementation of the 
 *  IGetDocuments interface.
 *
 * @version $Id: CompilerErrorModelTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;
import javax.swing.text.Position;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.swing.Utilities;

/** Tests the CompilerErrorModel.
 *  @version $Id: CompilerErrorModelTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class CompilerErrorModelTest extends DrJavaTestCase {
  private File[] files;
  private String[] texts;
  private TestDocGetter getter;       // subclass of DummyGlobalModel
  private DJError[] errors;
  private CompilerErrorModel model;
  
  /** Tests CompilerErrorModel setup code with no compiler errors. */
  public void testConstructNoErrors() {
    getter = new TestDocGetter();
    model = new CompilerErrorModel(new DJError[0], getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    // We successfully built the model, now test the basics.
    assertEquals("Should have no errors.", 0, model.getNumErrors());
    assertEquals("Should have 0 warnings" , 0, model.getNumWarnings());
    assertEquals("Should have 0 compiler errors" , 0, model.getNumCompilerErrors());
    assertTrue("hasOnlyWarnings should return true.", model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with only warnings without files. Also tests hasOnlyWarnings logic. */
  public void testConstructOnlyWarnings() {
    getter = new TestDocGetter();
    errors = new DJError[] { 
      new DJError("Test warning without File", true),
      new DJError("Test warning without File", true) 
    };
    model = new CompilerErrorModel(errors, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    // We successfully built the model, now test the basics.
    assertEquals("Should have 2 errors.", 2, model.getNumErrors());
    assertEquals("Should have 2 warnings" , 2, model.getNumWarnings());
    assertEquals("Should have 0 compiler errors" , 0, model.getNumCompilerErrors());
    assertTrue("hasOnlyWarnings should return true.", model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with only errors without files. */
  public void testConstructDoclessErrors() {
    getter = new TestDocGetter();
    errors = new DJError[] { 
      new DJError("Test error without File", false),
      new DJError("Test warning without File", true),
      new DJError("Test error without File", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    // We successfully built the model, now test the basics.
    assertEquals("Should have 3 compiler errors.", 3, model.getNumErrors());
    assertEquals("Should have 1 warning" , 1, model.getNumWarnings());
    assertEquals("Should have 2 compiler errors" , 2, model.getNumCompilerErrors());
//    System.out.println(model.getError(0) + "\n" + model.getError(1) + "\n" + model.getError(2));
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with one file and only errors without line numbers. */
  public void testConstructOneDocWithoutLineNums() {
    setupDoc();
    errors = new DJError[] { 
      new DJError(files[0], "Test error with File", false),
      new DJError(files[0], "Test warning with File", true),
      new DJError(files[0], "Test error with File", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++)  copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 3 compiler errors.", 3, model.getNumErrors());
    assertEquals("Should have 1 warning" , 1, model.getNumWarnings());
    assertEquals("Should have 2 compiler errors" , 2, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with one file and only errors with line numbers. */
  public void testConstructOneDocWithLineNums() {
    setupDoc();
    errors = new DJError[] { 
      new DJError(files[0], 2, 0, "Test error with File and line", false),
      new DJError(files[0], 1, 0, "Test warning with File and line", true),
      new DJError(files[0], 3, 0, "Test error with File and line", false),
      new DJError(files[0], 1, 0, "Test error with File and line", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
        Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
        
    // We successfully built the model, now test the basics.
    assertEquals("Should have 4 compiler errors.", 4, model.getNumErrors());
    assertEquals("Should have 1 warning" , 1, model.getNumWarnings());
    assertEquals("Should have  compiler errors" , 3, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[3], model.getError(0));
    assertEquals("Errors should be sorted.", errors[1], model.getError(1));
    assertEquals("Errors should be sorted.", errors[0], model.getError(2));
    assertEquals("Errors should be sorted.", errors[2], model.getError(3));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with one file and errors both with and without line numbers. */
  public void testConstructOneDocWithBoth() {
    setupDoc();
    errors = new DJError[] { 
      new DJError(files[0], 2, 0, "Test error with File and line", false),
      new DJError(files[0], "Test warning with File (no line)", true),
      new DJError(files[0], 3, 0, "Test error with File and line", false),
      new DJError("Test error without File or line", false),
      new DJError(files[0], 3, 0, "Test warning with File and line", true),
      new DJError(files[0], "Test error with File (no line)", false),
      new DJError(files[0], 1, 0, "Test error with File and line", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
        
    // We successfully built the model, now test the basics.
    assertEquals("Should have 7 compiler errors.", 7, model.getNumErrors());
    assertEquals("Should have 2 warnings" , 2, model.getNumWarnings());
    assertEquals("Should have 5 compiler errors" , 5, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[3], model.getError(0));
    assertEquals("Errors should be sorted.", errors[5], model.getError(1));
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertEquals("Errors should be sorted.", errors[6], model.getError(3));
    assertEquals("Errors should be sorted.", errors[0], model.getError(4));
    assertEquals("Errors should be sorted.", errors[2], model.getError(5));
    assertEquals("Errors should be sorted.", errors[4], model.getError(6));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with several files and only errors without line numbers. */
  public void testConstructManyDocsWithoutLineNums() {
    setupDocs();
    errors = new DJError[] { 
      new DJError(files[0], "Test error with File", false),
      new DJError(files[2], "Test warning with File", true),
      new DJError(files[4], "Test warning with File", true),
      new DJError(files[1], "Test error with File", false),
      new DJError(files[3], "Test warning with File", true),
      new DJError(files[3], "Test error with File", false),
      new DJError(files[4], "Test error with File", false),
      new DJError(files[0], "Test error with File", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
        
    // We successfully built the model, now test the basics.
    assertEquals("Should have 8 compiler errors.", 8, model.getNumErrors());
    assertEquals("Should have 3 warnings" , 3, model.getNumWarnings());
    assertEquals("Should have 5 compiler errors" , 5, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[0], model.getError(0));
    assertEquals("Errors should be sorted.", errors[7], model.getError(1));
    assertEquals("Errors should be sorted.", errors[3], model.getError(2));
    assertEquals("Errors should be sorted.", errors[1], model.getError(3));
    assertEquals("Errors should be sorted.", errors[5], model.getError(4));
    assertEquals("Errors should be sorted.", errors[4], model.getError(5));
    assertEquals("Errors should be sorted.", errors[6], model.getError(6));
    assertEquals("Errors should be sorted.", errors[2], model.getError(7));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with several files and only errors with line numbers. */
  public void testConstructManyDocsWithLineNums() {
    setupDocs();
    errors = new DJError[] { 
      new DJError(files[0], 2, 0, "Test error with File", false),
      new DJError(files[2], 3, 0, "Test warning with File", true),
      new DJError(files[4], 1, 0, "Test warning with File", true),
      new DJError(files[1], 2, 0, "Test error with File", false),
      new DJError(files[2], 2, 0, "Test warning with File", true),
      new DJError(files[3], 3, 0, "Test error with File", false),
      new DJError(files[4], 3, 0, "Test error with File", false),
      new DJError(files[0], 1, 0, "Test error with File", false) 
    };
    
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 8 compiler errors.", 8, model.getNumErrors());
    assertEquals("Should have 3 warnings" , 3, model.getNumWarnings());
    assertEquals("Should have 5 compiler errors" , 5, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[7], model.getError(0));
    assertEquals("Errors should be sorted.", errors[0], model.getError(1));
    assertEquals("Errors should be sorted.", errors[3], model.getError(2));
    assertEquals("Errors should be sorted.", errors[4], model.getError(3));
    assertEquals("Errors should be sorted.", errors[1], model.getError(4));
    assertEquals("Errors should be sorted.", errors[5], model.getError(5));
    assertEquals("Errors should be sorted.", errors[2], model.getError(6));
    assertEquals("Errors should be sorted.", errors[6], model.getError(7));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel setup code with several files and errors both with and without line numbers. */
  public void testConstructManyDocsWithBoth() {
    fullSetup();
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 15 compiler errors.", 15, model.getNumErrors());
    assertEquals("Should have 6 warnings" , 6, model.getNumWarnings());
    assertEquals("Should have 9 compiler errors" , 9, model.getNumCompilerErrors());
    assertEquals("Errors should be sorted.", errors[0], model.getError(0));
    assertEquals("Errors should be sorted.", errors[14], model.getError(1));
    assertEquals("Errors should be sorted.", errors[12], model.getError(2));
    assertEquals("Errors should be sorted.", errors[7], model.getError(3));
    assertEquals("Errors should be sorted.", errors[6], model.getError(4));
    assertEquals("Errors should be sorted.", errors[8], model.getError(5));
    assertEquals("Errors should be sorted.", errors[2], model.getError(6));
    assertEquals("Errors should be sorted.", errors[13], model.getError(7));
    assertEquals("Errors should be sorted.", errors[4], model.getError(8));
    assertEquals("Errors should be sorted.", errors[9], model.getError(9));
    assertEquals("Errors should be sorted.", errors[10], model.getError(10));
    assertEquals("Errors should be sorted.", errors[11], model.getError(11));
    assertEquals("Errors should be sorted.", errors[3], model.getError(12));
    assertEquals("Errors should be sorted.", errors[5], model.getError(13));
    assertEquals("Errors should be sorted.", errors[1], model.getError(14));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /** Tests CompilerErrorModel.getPosition(DJError). */
  public void testGetPosition() {
    fullSetup();

    Position pos = model.getPosition(errors[1]);
    assertEquals("Incorrect error Position.", 125, pos.getOffset());
    pos = model.getPosition(errors[5]);
    assertEquals("Incorrect error Position.", 38, pos.getOffset());
  }
  
  /** Tests CompilerErrorModel.getErrorAtOffset(int). */
  public void testGetErrorAtOffset() throws IOException, OperationCanceledException {
    fullSetup();
    
    OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
    assertEquals("Wrong error at given offset.", errors[1],
                 model.getErrorAtOffset(doc, 125));
    doc = getter.getDocumentForFile(files[4]);
    assertEquals("Wrong error at given offset.", errors[5],
                 model.getErrorAtOffset(doc, 38));
  }
  
  /** Tests CompilerErrorModel.hasErrorsWithPositions(OpenDefinitionsDocument). */
  public void testHasErrorsWithPositions() throws IOException, OperationCanceledException {
    fullSetup();
    
    // Doc with errors
    OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
    assertTrue("File should have errors with lines.", model.hasErrorsWithPositions(doc));
    
    // Same doc with a different (but equivalent) file name
    doc.setFile(new File("/tmp/./nowhere5"));
    assertTrue("Same file should have errors with lines.", model.hasErrorsWithPositions(doc));
    
    // Doc without errors
    doc = getter.getDocumentForFile(files[1]);
    assertTrue("File shouldn't have errors with lines.", !model.hasErrorsWithPositions(doc));
  }
  
  public void testErrorsInMultipleDocuments() throws IOException, OperationCanceledException {
    files = new File[] { 
      new File("/tmp/nowhere1"),
      new File("/tmp/nowhere2") 
    };
    texts = new String[] { 
      "kfgkasjg\n" + "faijskgisgj\n" + "sifjsidgjsd\n",
      "isdjfdi\n" + "jfa" 
    };
    getter = new TestDocGetter(files, texts);
    
    errors = new DJError[] { 
      new DJError(files[1], 0, 0, "Test error with File", false),
      new DJError(files[0], 0, 0, "Test error with File", false) 
    };
    model = new CompilerErrorModel(errors, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    
    model.getErrorAtOffset(getter.getDocumentForFile(files[0]), 25);
    String temp = texts[0];
    texts[0] = texts[1];
    texts[1] = temp;
    getter = new TestDocGetter(files, texts);
    errors = new DJError[] { 
      new DJError(files[0], 0, 0, "Test error with File", false),
      new DJError(files[1], 2, 0, "Test error with File", false)
    };
    model = new CompilerErrorModel(errors, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
    
    model.getErrorAtOffset(getter.getDocumentForFile(files[0]), 10);
  }
  
  /** Setup for test cases with one document. */
  private void setupDoc() {
    files = new File[] { new File("/tmp/nowhere") };
    texts = new String[] { 
      "This is a block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n"};
    getter = new TestDocGetter(files, texts);
  }
  
  /** Setup for test cases with several documents. */
  private void setupDocs() {
    files = new File[] { 
      new File("/tmp/nowhere1"),
      new File("/tmp/nowhere2"),
      new File("/tmp/nowhere3"),
      new File("/tmp/nowhere4"),
      new File("/tmp/nowhere5") 
    };
    texts = new String[] { 
      "This is the first block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n",
      "This is the second block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n",
      "This is the third block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n",
      "This is the fourth block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n",
      "This is the fifth block of test text.\n" + "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n" };
    getter = new TestDocGetter(files, texts);
  }
  
  /** Extra setup for test cases with several documents. */
  private void fullSetup() {
    setupDocs();
    errors = new DJError[] { 
      new DJError(files[0], "Test error with File (no line)", false),
      new DJError(files[4], 3, 0, "Test error with File", false),
      new DJError(files[2], "Test warning with File (no line)", true),
      new DJError(files[4], "Test warning with File (no line)", true),
      new DJError(files[2], 3, 0, "Test warning with File", true),
      new DJError(files[4], 1, 0, "Test warning with File", true),
      new DJError(files[1], "Test warning with File (no line)", true),
      new DJError(files[1], "Test error with File (no line)", false),
      new DJError(files[2], "Test error with File (no line)", false),
      new DJError(files[3], "Test error with File (no line)", false),
      new DJError(files[3], 3, 0, "Test error with File", false),
      new DJError(files[4], "Test error with File (no line)", false),
      new DJError(files[0], 2, 0, "Test error with File", false),
      new DJError(files[2], 2, 0, "Test warning with File", true),
      new DJError(files[0], 1, 0, "Test error with File", false) 
    };
        
    DJError[] copy = new DJError[errors.length];
    for (int i = 0; i < errors.length; i++) copy[i] = errors[i];
    model = new CompilerErrorModel(copy, getter);
    Utilities.clearEventQueue();  // constructor for CompilerErrorModel calls invokeLater
  }
}
