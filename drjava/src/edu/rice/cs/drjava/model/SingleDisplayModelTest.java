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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.io.*;

import java.util.Vector;
import javax.swing.text.BadLocationException;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.ListModel;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * Test functions of the single display model.
 *
 * @version $Id$
 */
public final class SingleDisplayModelTest extends GlobalModelTestCase {
  /**
   * Constructor.
   * @param  String name
   */
  public SingleDisplayModelTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(SingleDisplayModelTest.class);
  }
  
  /**
   * Instantiates the SingleDisplayModel to be used in the test cases.
   */
  protected void createModel() {
    //_model = new SingleDisplayModel(_originalModel);
    _model = new DefaultSingleDisplayModel();
  }
  
  /**
   * Get the instance of the SingleDisplayModel.
   */
  protected DefaultSingleDisplayModel getSDModel() {
    return (DefaultSingleDisplayModel) _model;
  }

  protected void assertNotEmpty()
    throws BadLocationException
  {
    assertTrue("number of documents",
               getSDModel().getDefinitionsDocuments().size() > 0);
  }

  protected void assertActiveDocument(OpenDefinitionsDocument doc)
    throws BadLocationException
  {
    assertEquals("active document",
                 doc,
                 getSDModel().getActiveDocument());
  }
  
  /**
   * Creates and returns a new document, makes sure newFile and
   * activeDocumentChanged events are fired, and then adds some text.
   * @return the new modified document
   */
  protected OpenDefinitionsDocument setupDocument(String text)
    throws BadLocationException
  {
    assertNotEmpty();
    SDTestListener listener = new SDTestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };

    getSDModel().addListener(listener);

    // Open a new document
    int numOpen = getSDModel().getDefinitionsDocuments().size();
    OpenDefinitionsDocument doc = getSDModel().newFile();
    assertNumOpenDocs(numOpen + 1);

    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    assertLength(0, doc);
    assertModified(false, doc);

    changeDocumentText(text, doc);
    getSDModel().removeListener(listener);

    return doc;
  }
  
  /**
   * A SingleDisplayModelListener for testing.
   * By default it expects no events to be fired. To customize,
   * subclass and override one or more methods.
   */
  public static class SDTestListener extends TestListener
    implements SingleDisplayModelListener
  {
    protected int switchCount;


    public void resetCounts() {
      super.resetCounts();
      switchCount = 0;
    }
    
    public void assertSwitchCount(int i) {
      assertEquals("number of active document switches", i, switchCount);
    }
    
    public void activeDocumentChanged(OpenDefinitionsDocument doc) {
      fail("activeDocumentChanged fired unexpectedly");
    }
  }

  /**
   * Custom setup for SingleDisplayModel tests.
   */
  public void setUp() throws IOException {
    super.setUp();
  }
  
  
  /**
   * Tests the invariant that at least one document is open
   * at time of creation.
   */
  public void testNotEmptyOnStartup() throws BadLocationException {
    // Should be one empty document after creation
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = getSDModel().getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);
  }
  
  /**
   * Tests the setNext and setPrevious functions, making
   * sure that the activeDocumentChanged event is called.
   */
  public void testDocumentSwitching() throws BadLocationException {
    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    getSDModel().addListener(listener);
    
    // Set up first document
    OpenDefinitionsDocument doc1 = getSDModel().getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    
    // Set up two more documents
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    assertActiveDocument(doc2);

    OpenDefinitionsDocument doc3 = setupDocument("");
    assertNumOpenDocs(3);
    listener.assertNewCount(2);
    listener.assertSwitchCount(2);
    assertActiveDocument(doc3);
    
    // Make sure setNext doesn't move (at end of list)
    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(2);
    assertActiveDocument(doc3);
    
    // Test setPrevious
    getSDModel().setActivePreviousDocument();
    listener.assertSwitchCount(3);
    assertActiveDocument(doc2);
    
    getSDModel().setActivePreviousDocument();
    listener.assertSwitchCount(4);
    assertActiveDocument(doc1);
    
    // Make sure setPrevious doesn't move (at start of list)
    getSDModel().setActivePreviousDocument();
    listener.assertSwitchCount(4);
    assertActiveDocument(doc1);
    
    // Test setNext
    getSDModel().setActiveNextDocument();
    listener.assertSwitchCount(5);
    assertActiveDocument(doc2);
    
    // Test setActive
    getSDModel().setActiveDocument(0);
    listener.assertSwitchCount(6);
    assertActiveDocument(doc1);
    
    // Make sure number of docs hasn't changed
    assertNumOpenDocs(3);
    getSDModel().removeListener(listener);
  }

  /**
   * Ensures that an unmodified, empty document is closed
   * after a file is opened, while a modified document
   * is left open.
   */
  public void testCloseUnmodifiedAutomatically()
    throws BadLocationException, IOException,
      OperationCanceledException, AlreadyOpenException
  {
    assertNumOpenDocs(1);
    OpenDefinitionsDocument doc = getSDModel().getActiveDocument();
    assertModified(false, doc);
    assertLength(0, doc);
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);

    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public void fileOpened(OpenDefinitionsDocument doc) {
        File file = null;
        try {
          file = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          fail("file does not exist");
        }
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        try {
          assertEquals("file to open", tempFile.getCanonicalFile(),
                       file.getCanonicalFile());
          openCount++;
        }
        catch (IOException ioe) {
          fail("could not get canonical file");
        }
      }
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    getSDModel().addListener(listener);
    
    // Open file, should replace the old
    doc = getSDModel().openFile(new FileSelector(tempFile));
    listener.assertOpenCount(1);
    listener.assertCloseCount(1);
    listener.assertSwitchCount(1);
    assertNumOpenDocs(1);
    assertModified(false, doc);
    assertContents(BAR_TEXT, doc);
    
    getSDModel().removeListener(listener);
  }
  
  /**
   * Tests that active document is switched on close, and that
   * a new file is created after the last one is closed.
   */
  public void testCloseFiles() throws BadLocationException {
    // Check for proper events
    SDTestListener listener = new SDTestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }
      
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
      public void activeDocumentChanged(OpenDefinitionsDocument doc) {
        switchCount++;
      }
    };
    getSDModel().addListener(listener);
    
    // Set up two documents
    OpenDefinitionsDocument doc1 = getSDModel().getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    assertActiveDocument(doc2);
    assertNumOpenDocs(2);
    listener.assertNewCount(1);
    listener.assertSwitchCount(1);
    
    // Close one
    getSDModel().closeFile(getSDModel().getActiveDocument());
    assertNumOpenDocs(1);
    listener.assertCloseCount(1);
    listener.assertAbandonCount(1);
    listener.assertSwitchCount(2);
    assertActiveDocument(doc1);
    assertContents(FOO_TEXT, getSDModel().getActiveDocument());
    
    // Close the other
    getSDModel().closeFile(getSDModel().getActiveDocument());
    listener.assertCloseCount(2);
    listener.assertAbandonCount(2);
    
    // Ensure a new document was created
    assertNumOpenDocs(1);
    listener.assertNewCount(2);
    listener.assertSwitchCount(3);
    assertLength(0, getSDModel().getActiveDocument());
    
    // Set up two documents
    doc1 = getSDModel().getActiveDocument();
    changeDocumentText(FOO_TEXT, doc1);
    doc2 = setupDocument(BAR_TEXT);
    assertNumOpenDocs(2);
    listener.assertNewCount(3);
    
    // Close all files, ensure new one was created
    getSDModel().closeAllFiles();
    assertNumOpenDocs(1);
    assertLength(0, getSDModel().getActiveDocument());
    listener.assertNewCount(4);
    listener.assertCloseCount(4);
    listener.assertAbandonCount(4);
    
    getSDModel().removeListener(listener);
  }
  
  /**
   * Tests that active document is switched on close, and that
   * a new file is created after the last one is closed.
   */
  public void testDisplayFilename()
    throws BadLocationException, IOException,
      OperationCanceledException, AlreadyOpenException
  {
    DefaultSingleDisplayModel sdm = getSDModel();
    
    // Untitled
    OpenDefinitionsDocument doc = sdm.getActiveDocument();
    assertEquals("untitled display filename", "(Untitled)",
                 sdm.getDisplayFilename(doc));
    
    // Ends in ".java"
    File file = File.createTempFile("DrJava-filename-test", ".java", _tempDir);
    file.deleteOnExit();
    String name = file.getName();
    doc = sdm.openFile(new FileSelector(file));
    assertEquals(".java display filename",
                 name.substring(0, name.length()-5),
                 sdm.getDisplayFilename(doc));
    
    // Doesn't contain ".java"
    file = File.createTempFile("DrJava-filename-test", ".txt", _tempDir);
    file.deleteOnExit();
    name = file.getName();
    doc = sdm.openFile(new FileSelector(file));
    assertEquals(".txt display filename",
                 name,
                 sdm.getDisplayFilename(doc));
    
    // Doesn't end in ".java"
    file = File.createTempFile("DrJava-filename-test", ".java.txt", _tempDir);
    file.deleteOnExit();
    name = file.getName();
    doc = sdm.openFile(new FileSelector(file));
    assertEquals(".java.txt display filename",
                 name,
                 sdm.getDisplayFilename(doc));
    
  }
}
