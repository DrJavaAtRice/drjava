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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.GlobalModelTestCase;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

import junit.framework.*;

import java.io.IOException;

/**
 * bugs:
 * DocumentAdapterExceptions in the interactions from trying to print new prompts or System.out
 * 
 * verify that input requests go through the console
 * 
 * @version $Id$
 */
public final class ConsoleControllerTest extends GlobalModelTestCase {
  protected SwingDocumentAdapter _adapter;
  protected ConsoleDocument _doc;
  protected InteractionsPane _pane;
  protected ConsoleController _controller;
  protected Object _lock;

  public ConsoleControllerTest(String name) {
    super(name);
  }

  /**
   * Sets up the fields for the test methods.
   */
  public void setUp() throws IOException {
    super.setUp();
    _adapter = _model.getSwingConsoleDocument();
    _doc = _model.getConsoleDocument();
    _controller = new TestConsoleController(_doc, _adapter);
    _pane = _controller.getPane();
    _model.setInputListener(_controller.getInputListener());
    _lock = _controller.getInputWaitObject();  // convenience alias for use in this test
  }

  /**
   * Cleans up the fields after the test methods.
   */
  public void tearDown() throws IOException {
    _adapter = null;
    _doc = null;
    _controller = null;
    _pane = null;
    super.tearDown();
  }

  /**
   * Tests that basic input to the console is correctly redirected to System.in.
   * We start up an InputGeneratorThread, which waits until input is requested from
   * the console.  It then generates input and puts it into the console, where it is
   * returned to the interpreter.
   */
  public void testBasicConsoleInput()
    throws DocumentAdapterException, InterruptedException {
    Thread inputGenerator = new InputGeneratorThread("a");
    String result;
    synchronized(_lock) {
      inputGenerator.start();
      _lock.wait();  // wait to be notified by inputGenerator
      // must reacquire _lock before it can proceed, i.e. inputGenerator must be waiting
    }
    result = interpret("System.in.read()");

    String expected = DefaultInteractionsModel.INPUT_REQUIRED_MESSAGE +
      String.valueOf((int) 'a');
    assertEquals("read() returns the correct character", expected, result);
    result = interpret("System.in.read()");
    assertEquals("second read() should get the end-of-line character",
                 String.valueOf((int) System.getProperty("line.separator").charAt(0)), result);
  }

  /**
   * Class to insert text into the console document after input is requested.
   */
  protected class InputGeneratorThread extends Thread {
    private String _text;
    /**
     * @param text the text to be written to the console
     */
    public InputGeneratorThread(String text) {
      _text = text;
    }
    public void run() {
      try {
        synchronized(_lock) {
          _lock.notify(); // notifies main test thread that this thread has started
          _lock.wait();   // wait to be notified by RMI thread that input is needed
          // cannot proceed until RMI thread starts waiting on _lock (_controller._inputWaitObject)
          _doc.insertText(_doc.getDocLength(), _text, ConsoleDocument.DEFAULT_STYLE);
          _controller.enterAction.actionPerformed(null);
        }
      }
      catch (Exception e) {
        listenerFail("InputGenerator failed:  " + e);
      }
    }
  }

  /**
   * Overrides the _waitForInput() so that it notifies the test
   * when input is requested.
   */
  protected class TestConsoleController extends ConsoleController {
    public TestConsoleController(ConsoleDocument doc, SwingDocumentAdapter adapter) {
      super(doc, adapter);
    }

    protected void _waitForInput() {
      synchronized(_lock) {
        _lock.notify();  // notify inputGenerator that input is needed
        super._waitForInput();
      }
    }
  }
}
