/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

/**
 * This class tests the internal functionality of CompilerErrorModel using
 * a dummy implementation of the IGetDocuments interface.
 *
 * @version $Id$
 */
package edu.rice.cs.drjava.model.compiler;

import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import junit.framework.TestCase;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.util.UnexpectedException;

/**
 * Tests the CompilerErrorModel.
 * @version $Id$
 */
public final class CompilerErrorModelTest extends TestCase {
  private File[] files;
  private String[] texts;
  private TestDocGetter getter;
  private CompilerError[] errors;
  private CompilerErrorModel<CompilerError> model;
  
  /**
   * Tests CompilerErrorModel setup code with no compiler errors.
   */
  public void testConstructNoErrors() {
    getter = new TestDocGetter();
    model = new CompilerErrorModel<CompilerError>(new CompilerError[0], getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have no compiler errors.", 0, model.getNumErrors());
    assertTrue("hasOnlyWarnings should return true.", model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with only warnings without files.
   * Also tests hasOnlyWarnings logic.
   */
  public void testConstructOnlyWarnings() {
    getter = new TestDocGetter();
    errors = new CompilerError[]
    { new CompilerError("Test warning without File", true),
      new CompilerError("Test warning without File", true) };
    model = new CompilerErrorModel<CompilerError>(errors, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 2 compiler errors.", 2, model.getNumErrors());
    assertTrue("hasOnlyWarnings should return true.", model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with only errors without files.
   */
  public void testConstructDoclessErrors() {
    getter = new TestDocGetter();
    errors = new CompilerError[]
    { new CompilerError("Test error without File", false),
      new CompilerError("Test warning without File", true),
      new CompilerError("Test error without File", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 3 compiler errors.", 3, model.getNumErrors());
//    System.out.println(model.getError(0) + "\n" + model.getError(1) + "\n" + model.getError(2));
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with one file and only errors
   * without line numbers.
   */
  public void testConstructOneDocWithoutLineNums() {
    setupDoc();
    errors = new CompilerError[]
    { new CompilerError(files[0], "Test error with File", false),
      new CompilerError(files[0], "Test warning with File", true),
      new CompilerError(files[0], "Test error with File", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 3 compiler errors.", 3, model.getNumErrors());
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with one file and only errors
   * with line numbers.
   */
  public void testConstructOneDocWithLineNums() {
    setupDoc();
    errors = new CompilerError[]
    { new CompilerError(files[0], 2, 0, "Test error with File and line", false),
      new CompilerError(files[0], 1, 0, "Test warning with File and line", true),
      new CompilerError(files[0], 3, 0, "Test error with File and line", false),
      new CompilerError(files[0], 1, 0, "Test error with File and line", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 4 compiler errors.", 4, model.getNumErrors());
    assertEquals("Errors should be sorted.", errors[3], model.getError(0));
    assertEquals("Errors should be sorted.", errors[1], model.getError(1));
    assertEquals("Errors should be sorted.", errors[0], model.getError(2));
    assertEquals("Errors should be sorted.", errors[2], model.getError(3));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with one file and errors both
   * with and without line numbers.
   */
  public void testConstructOneDocWithBoth() {
    setupDoc();
    errors = new CompilerError[]
    { new CompilerError(files[0], 2, 0, "Test error with File and line", false),
      new CompilerError(files[0], "Test warning with File", true),
      new CompilerError(files[0], 3, 0, "Test error with File and line", false),
      new CompilerError("Test error without File", false),
      new CompilerError(files[0], 3, 0, "Test warning with File and line", true),
      new CompilerError(files[0], "Test error with File", false),
      new CompilerError(files[0], 1, 0, "Test error with File and line", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 7 compiler errors.", 7, model.getNumErrors());
    assertEquals("Errors should be sorted.", errors[3], model.getError(0));
    assertEquals("Errors should be sorted.", errors[5], model.getError(1));
    assertEquals("Errors should be sorted.", errors[1], model.getError(2));
    assertEquals("Errors should be sorted.", errors[6], model.getError(3));
    assertEquals("Errors should be sorted.", errors[0], model.getError(4));
    assertEquals("Errors should be sorted.", errors[2], model.getError(5));
    assertEquals("Errors should be sorted.", errors[4], model.getError(6));
    assertTrue("hasOnlyWarnings should return false.", !model.hasOnlyWarnings());
  }
  
  /**
   * Tests CompilerErrorModel setup code with several files and only errors
   * without line numbers.
   */
  public void testConstructManyDocsWithoutLineNums() {
    setupDocs();
    errors = new CompilerError[]
    { new CompilerError(files[0], "Test error with File", false),
      new CompilerError(files[2], "Test warning with File", true),
      new CompilerError(files[4], "Test warning with File", true),
      new CompilerError(files[1], "Test error with File", false),
      new CompilerError(files[3], "Test warning with File", true),
      new CompilerError(files[3], "Test error with File", false),
      new CompilerError(files[4], "Test error with File", false),
      new CompilerError(files[0], "Test error with File", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 8 compiler errors.", 8, model.getNumErrors());
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
  
  /**
   * Tests CompilerErrorModel setup code with several files and only errors
   * with line numbers.
   */
  public void testConstructManyDocsWithLineNums() {
    setupDocs();
    errors = new CompilerError[]
    { new CompilerError(files[0], 2, 0, "Test error with File", false),
      new CompilerError(files[2], 3, 0, "Test warning with File", true),
      new CompilerError(files[4], 1, 0, "Test warning with File", true),
      new CompilerError(files[1], 2, 0, "Test error with File", false),
      new CompilerError(files[2], 2, 0, "Test warning with File", true),
      new CompilerError(files[3], 3, 0, "Test error with File", false),
      new CompilerError(files[4], 3, 0, "Test error with File", false),
      new CompilerError(files[0], 1, 0, "Test error with File", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 8 compiler errors.", 8, model.getNumErrors());
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
  
  /**
   * Tests CompilerErrorModel setup code with several files and errors both
   * with and without line numbers.
   */
  public void testConstructManyDocsWithBoth() {
    fullSetup();
    
    // We successfully built the model, now test the basics.
    assertEquals("Should have 15 compiler errors.", 15, model.getNumErrors());
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
  
  /**
   * Tests CompilerErrorModel.getPosition(CompilerError).
   */
  public void testGetPosition() {
    fullSetup();
    
    Position pos = model.getPosition(errors[1]);
    assertEquals("Incorrect error Position.", 125, pos.getOffset());
    pos = model.getPosition(errors[5]);
    assertEquals("Incorrect error Position.", 38, pos.getOffset());
  }
  
  /**
   * Tests CompilerErrorModel.getErrorAtOffset(int).
   */
  public void testGetErrorAtOffset() throws IOException, OperationCanceledException {
    fullSetup();
    
    OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
    assertEquals("Wrong error at given offset.", errors[1],
                 model.getErrorAtOffset(doc, 125));
    doc = getter.getDocumentForFile(files[4]);
    assertEquals("Wrong error at given offset.", errors[5],
                 model.getErrorAtOffset(doc, 38));
  }
  
  /**
   * Tests CompilerErrorModel.hasErrorsWithPositions(OpenDefinitionsDocument).
   */
  public void testHasErrorsWithPositions() throws IOException, OperationCanceledException {
    fullSetup();
    
    // Doc with errors
    OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
    assertTrue("File should have errors with lines.", model.hasErrorsWithPositions(doc));
    
    // Same doc with a different (but equivalent) file name
    doc.getDocument().setFile(new File("/tmp/./nowhere5"));
    assertTrue("Same file should have errors with lines.", model.hasErrorsWithPositions(doc));
    
    // Doc without errors
    doc = getter.getDocumentForFile(files[1]);
    assertTrue("File shouldn't have errors with lines.", !model.hasErrorsWithPositions(doc));
  }
  
  public void testErrorsInMultipleDocuments() throws IOException, OperationCanceledException {
    files = new File[]
    { new File("/tmp/nowhere1"),
      new File("/tmp/nowhere2") };
    texts = new String[]
    { new String("kfgkasjg\n" +
                 "faijskgisgj\n" +
                 "sifjsidgjsd\n"),
      new String("isdjfdi\n" +
                 "jfa") };
    getter = new TestDocGetter(files, texts);
    
    errors = new CompilerError[]
    { new CompilerError(files[1], 0, 0, "Test error with File", false),
      new CompilerError(files[0], 0, 0, "Test error with File", false) };
    model = new CompilerErrorModel<CompilerError>(errors, getter);
    model.getErrorAtOffset(getter.getDocumentForFile(files[0]), 25);
    String temp = texts[0];
    texts[0] = texts[1];
    texts[1] = temp;
    getter = new TestDocGetter(files, texts);
    errors = new CompilerError[]
    { new CompilerError(files[0], 0, 0, "Test error with File", false),
      new CompilerError(files[1], 2, 0, "Test error with File", false)};
    model = new CompilerErrorModel<CompilerError>(errors, getter);
    model.getErrorAtOffset(getter.getDocumentForFile(files[0]), 10);
  }
  
  /**
   * Setup for test cases with one document.
   */
  private void setupDoc() {
    files = new File[] { new File("/tmp/nowhere") };
    texts = new String[]
    { new String("This is a block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n") };
    getter = new TestDocGetter(files, texts);
  }
  
  /**
   * Setup for test cases with several documents.
   */
  private void setupDocs() {
    files = new File[]
    { new File("/tmp/nowhere1"),
      new File("/tmp/nowhere2"),
      new File("/tmp/nowhere3"),
      new File("/tmp/nowhere4"),
      new File("/tmp/nowhere5") };
    texts = new String[]
    { new String("This is the first block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n"),
      new String("This is the second block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n"),
      new String("This is the third block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n"),
      new String("This is the fourth block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n"),
      new String("This is the fifth block of test text.\n" +
                 "It doesn't matter what goes in here.\n" +
                 "But it does matter if it is manipulated properly!\n") };
    getter = new TestDocGetter(files, texts);
  }
  
  /**
   * Extra setup for test cases with several documents.
   */
  private void fullSetup() {
    setupDocs();
    errors = new CompilerError[]
    { new CompilerError(files[0], "Test error with File", false),
      new CompilerError(files[4], 3, 0, "Test error with File", false),
      new CompilerError(files[2], "Test warning with File", true),
      new CompilerError(files[4], "Test warning with File", true),
      new CompilerError(files[2], 3, 0, "Test warning with File", true),
      new CompilerError(files[4], 1, 0, "Test warning with File", true),
      new CompilerError(files[1], "Test warning with File", true),
      new CompilerError(files[1], "Test error with File", false),
      new CompilerError(files[2], "Test error with File", false),
      new CompilerError(files[3], "Test error with File", false),
      new CompilerError(files[3], 3, 0, "Test error with File", false),
      new CompilerError(files[4], "Test error with File", false),
      new CompilerError(files[0], 2, 0, "Test error with File", false),
      new CompilerError(files[2], 2, 0, "Test warning with File", true),
      new CompilerError(files[0], 1, 0, "Test error with File", false) };
    
    CompilerError[] copy = new CompilerError[errors.length];
    for(int i = 0; i < errors.length; i++) {
      copy[i] = errors[i];
    }
    model = new CompilerErrorModel<CompilerError>(copy, getter);
  }
}
