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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.GlobalModelTestCase;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

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
  /** Document adapter used in the console document. */
  protected SwingDocumentAdapter _adapter;

  /** The console document for the current console controller. */
  protected ConsoleDocument _doc;

  /** The console pane in use by the current console controller. */
  protected InteractionsPane _pane;

  /** The current console controller. */
  protected ConsoleController _controller;

  /** Synchronization object. */
  protected Object _lock;

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

    String expected = /*DefaultInteractionsModel.INPUT_REQUIRED_MESSAGE + */
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
