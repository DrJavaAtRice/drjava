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

package edu.rice.cs.drjava.model.repl;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * Tests the functionality of the AbstractInteractionsDocument.
 * Most history functionality is tested in HistoryTest.
 * @version $Id$
 */
public class InteractionsDocumentTest extends TestCase {
  protected InteractionsDocument _doc;
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public InteractionsDocumentTest(String name) {
    super(name);
  }
  
  /**
   * Initialize fields for each test.
   */
  protected void setUp() {
    _doc = new TestInteractionsDocument(new SwingDocumentAdapter());
  }
  
  /**
   * Return a new TestSuite for this class.
   * @return Test
   */
  public static Test suite() {
    return new TestSuite(InteractionsDocumentTest.class);
  }
  
  
  /**
   * Tests that the document prevents editing before the
   * prompt, and beeps if you try.
   */
  public void testCannotEditBeforePrompt() throws DocumentAdapterException {
    TestBeep testBeep = new TestBeep();
    _doc.setBeep(testBeep);
    int origLength = _doc.getDocLength();
    
    // Try to insert into the banner
    _doc.insertText(1, "text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Number of beeps", 1, testBeep.numBeeps);
    assertEquals("Doc length", origLength, _doc.getDocLength());
  }
  
  /**
   * Tests that clear current interaction works.
   */
  public void testClearCurrent() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(origLength, "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Length after inserts", 
                 origLength + 10,  // orig + "before" + "text"
                 _doc.getDocLength());
    _doc.clearCurrentInteraction();
    assertEquals("Length after clear", 
                 origLength + 6,  // orig + "before"
                 _doc.getDocLength());
  }
  
  /**
   * Tests that reset works.
   */
  public void testReset() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(origLength, "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    _doc.reset();
    assertEquals("Length after reset", origLength, _doc.getDocLength());
  }
  
  /**
   * Tests that inserting a newline works.
   */
  public void testInsertNewLine() throws DocumentAdapterException {
    int origLength = _doc.getDocLength();
    _doc.insertText(origLength, "command", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("current interaction before newline",
                 "command",
                 _doc.getCurrentInteraction());
    _doc.insertNewLine(origLength + 2);
    assertEquals("current interaction after newline",
                 "co\nmmand",
                 _doc.getCurrentInteraction());
  }
  
  /**
   * Dummy InteractionsDocument which cannot interpret anything.
   * Uses a Swing document for its model.
   */
  public static class TestInteractionsDocument extends AbstractInteractionsDocument {
    public TestInteractionsDocument(DocumentAdapter adapter) {
      super(adapter);
    }
    public void interpretCurrentInteraction() {
      fail("interpretCurrentInteraction called unexpectedly");
    }
  }
  
  /**
   * Silent beep for a test class.
   */
  public static class TestBeep implements Runnable {
    int numBeeps = 0;
    public void run() {
      numBeeps++;
    }
  }
}



