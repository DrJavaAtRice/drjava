/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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
  public void testGetErrorAtOffset() {
    fullSetup();
    
    try {
      OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
      assertEquals("Wrong error at given offset.", errors[1],
                   model.getErrorAtOffset(doc, 125));
      doc = getter.getDocumentForFile(files[4]);
      assertEquals("Wrong error at given offset.", errors[5],
                   model.getErrorAtOffset(doc, 38));
    }
    catch (IOException e) {
      fail("Unexpected IOException.");
    }
    catch (OperationCanceledException e) {
      fail("Unexpected OperationCanceledException.");
    }
  }
  
  /**
   * Tests CompilerErrorModel.hasErrorsWithPositions(OpenDefinitionsDocument).
   */
  public void testHasErrorsWithPositions() {
    fullSetup();
    
    try {
      OpenDefinitionsDocument doc = getter.getDocumentForFile(files[4]);
      assertTrue("File should have errors with lines.", model.hasErrorsWithPositions(doc));
      doc = getter.getDocumentForFile(files[1]);
      assertTrue("File shouldn't have errors with lines.", !model.hasErrorsWithPositions(doc));
    }
    catch (IOException e) {
      fail("Unexpected IOException.");
    }
    catch (OperationCanceledException e) {
      fail("Unexpected OperationCanceledException.");
    }
  }
  
  /**
   * Setup for test cases with one document.
   */
  private void setupDoc() {
    files = new File[] { new File("//nowhere") };
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
    { new File("//nowhere1"),
      new File("//nowhere2"),
      new File("//nowhere3"),
      new File("//nowhere4"),
      new File("//nowhere5") };
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
  
  /**
   * Test implementation of IGetDocuments interface.
   */
  private static class TestDocGetter extends DummyGetDocuments {
    /**
     * Storage for documents and File keys.
     */
    HashMap docs;  // TODO: <File, OpenDefinitionsDocument>
    
    /**
     * Convenience constructor for no-documents case.
     */
    private TestDocGetter() {
      this(new File[0], new String[0]);
    }
    
    /**
     * Primary constructor, builds OpenDefDocs from Strings.
     * @param files the keys to use when getting OpenDefDocs
     * @param texts the text to put in the OpenDefDocs
     */
    private TestDocGetter(File[] files, String[] texts) {
      if (files.length != texts.length) {
        throw new IllegalArgumentException("Argument arrays must match in size.");
      }
      
      docs = new HashMap(texts.length * 2);
      
      EventNotifier en = new EventNotifier();
      for (int i = 0; i < texts.length; i++) {
        DefinitionsDocument doc = new DefinitionsDocument(en);
        doc.setFile(files[i]);
        try {
          doc.insertString(0, texts[i], null);
        }
        catch (BadLocationException e) {
          throw new UnexpectedException(e);
        }
        docs.put(files[i], new TestOpenDoc(doc));
      }
    }
    
    public OpenDefinitionsDocument getDocumentForFile(File file)
      throws IOException, OperationCanceledException {
      // Try to find the key in docs.
      if (docs.containsKey(file)) {
        return (OpenDefinitionsDocument) docs.get(file);
      }
      else {
        throw new IllegalStateException("TestDocGetter can't open new files!");
      }
    }
  }
  
  /**
   * Test implementation of OpenDefinitionsDocument interface.
   */
  private static class TestOpenDoc extends DummyOpenDefDoc {
    DefinitionsDocument _doc;
    TestOpenDoc(DefinitionsDocument doc) {
      _doc = doc;
    }
    
    /**
     * This is the only method that we care about.
     */
    public DefinitionsDocument getDocument() {
      return _doc;
    }
    
    /**
     * Okay, I lied.  We need this one, too.
     */
    public File getFile() throws IllegalStateException, FileMovedException  {
      return _doc.getFile();
    }
  }
}
