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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.io.*;

import java.util.Vector;
import javax.swing.text.BadLocationException;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.*;

/**
 * Test cases on the GlobalModel that ensure that quit
 * behaves appropriately.
 *
 * @version $Id$
 */
public class GlobalModelQuitTest extends GlobalModelTestCase {
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelQuitTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(GlobalModelQuitTest.class);

    // wrapper allows these methods to be run once before/after all tests
    // respectively
    TestSetup wrapper = new TestSetup(suite) {
      public void setUp() {
        DrJava.enableSecurityManager();
      }

      public void tearDown() {
        DrJava.disableSecurityManager();
        // ensure interpreter is started up, since the quit calls in
        // these test cases bring it down.
        // This complication is caused by the fact that we reuse MainJVM
        // instances across multiple test cases due to the overhead of
        // starting it up.
        _originalModel._interpreterControl.startInterpreterJVM();
      }
    };

    return wrapper;
  }

  /**
   * Make all test cases run with exit blocking on.
   */
  protected void setUp() throws IOException {
    super.setUp();
    _manager().setBlockExit(true);
    // reset exit attempted status
    _manager().exitAttempted();
  }

  /**
   * Overridden to create a clean new model each time, not disturbing the
   * standard model used by other global model test cases.
   * This is important because each of these tests kills the interpreter JVM,
   * so we need a fresh one each time. Sadly, this is slow, but it does test
   * the behavior well.
   */
  protected void createModel() {
    _model = new DefaultGlobalModel();
  }

  /**
   * Reset exit blocking.
   */
  protected void tearDown() throws IOException {
    _manager().setBlockExit(false);

    super.tearDown();
  }

  private void _assertInterpreterStatus(boolean value)
    throws InterruptedException
  {
    // we wait to ensure that the quit has registered, if we were expected it
    // to quit.
    if (! value) {
      Thread.currentThread().sleep(500);
    }

    assertEquals("is interpreter running after quit?",
                 value,
                 _model._interpreterControl.isInterpreterRunning());
  }


  private static PreventExitSecurityManager _manager() {
    return DrJava.getSecurityManager();
  }

  /**
   * Exits the program without opening any documents.
   */
  public void testQuitNoDocuments() throws InterruptedException {
    assertNumOpenDocs(0);

    // Ensure no events get fired (except for the interactions exited one)
    _model.addListener(new QuitTestListener());

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (ExitingNotAllowedException e) {
      // Good, the security manager saved us from exiting.
      assertTrue("attempted to quit", _manager().exitAttempted());
      _assertInterpreterStatus(false);
    }
  }

  /**
   * Exits the program without having written anything to the open documents.
   */
  public void testQuitEmptyDocuments() throws InterruptedException {
    _model.newFile();
    _model.newFile();

    assertNumOpenDocs(2);

    // Check for proper events
    TestListener listener = new QuitTestListener() {
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };
    _model.addListener(listener);

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (ExitingNotAllowedException e) {
      // Good, the security manager saved us from exiting.
      assertTrue("attempted to quit", _manager().exitAttempted());
      listener.assertCloseCount(2);
      _assertInterpreterStatus(false);
    }
  }


  /**
   * Exits the program without saving any changes made to the current document.
   * Loses the changes.
   */
  public void testQuitUnsavedDocumentsAllowAbandon()
    throws BadLocationException, InterruptedException
  {
    TestListener listener = new QuitTestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };

    _model.newFile();
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);

    assertNumOpenDocs(3);

    _model.addListener(listener);

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (ExitingNotAllowedException e) {
      // Good, the security manager saved us from exiting.
      assertTrue("attempted to quit", _manager().exitAttempted());

      // Only the changed files should prompt an event
      listener.assertAbandonCount(2);
      listener.assertCloseCount(3);
      _assertInterpreterStatus(false);
    }
  }

  /**
   * Attempts to exit with unsaved changes, but doesn't allow the quit.
   */
  public void testQuitUnsavedDocumentDisallowAbandon()
    throws BadLocationException, InterruptedException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    assertNumOpenDocs(1);

    // Ensure canAbandonChanges is called
    TestListener listener = new QuitTestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return false; // no, don't quit on me!
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();
      listener.assertAbandonCount(1);
      assertTrue("did not attempt to quit", !_manager().exitAttempted());
      _assertInterpreterStatus(true);
    }
    catch (ExitingNotAllowedException e) {
      fail("Quit succeeded despite canAbandon returning no!");
    }
  }

  /**
   * Attempts to exit with unsaved changes, but doesn't allow the quit.
   */
  public void testQuitMultipleDocumentsDisallowAbandon()
    throws BadLocationException, InterruptedException
  {
    _model.newFile();
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    _model.newFile();
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);

    assertNumOpenDocs(4);

    // Ensure canAbandonChanges is called
    TestListener listener = new QuitTestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return false; // no, don't quit on me!
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();

      // Should close first new file, stop trying after doc1
      listener.assertCloseCount(1);
      listener.assertAbandonCount(1);
      assertNumOpenDocs(3);

      assertTrue("did not attempt to quit", !_manager().exitAttempted());
      _assertInterpreterStatus(true);
    }
    catch (ExitingNotAllowedException e) {
      fail("Quit succeeded despite canAbandon returning no!");
    }
  }

  class QuitTestListener extends TestListener {
    private boolean _interactionsExited = false;

    public void interactionsExited(int status) {
      _interactionsExited = true;
    }

    public void interactionsReset() {
      assertTrue("interactions exited should have been called",
                 _interactionsExited);

      interactionsResetCount++;
    }

    public void assertExited(boolean value) {
      assertEquals("value of whether interactions exited was called",
                   value,
                   _interactionsExited);

      if (value) {
        assertInteractionsResetCount(1);
      }
    }
  }
}
