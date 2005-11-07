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

import javax.swing.*;
import java.awt.Font;
import java.awt.event.*;

import edu.rice.cs.drjava.model.repl.*;

/** A standalone Interactions Window that provides the functionality of DrJava's Interactions Pane in a single JVM.
 *  Useful for quickly testing small pieces of code if DrJava is not running.
 *  @version $Id$
 */
public class SimpleInteractionsWindow extends JFrame {
  //private final SimpleRMIInteractionsModel _rmiModel;
  private final SimpleInteractionsModel _model;
  private final InteractionsDJDocument _adapter;
  private final InteractionsPane _pane;
  private final InteractionsController _controller;

  public SimpleInteractionsWindow() { this("Interactions Window"); }

  public SimpleInteractionsWindow(String title) {
    super(title);
    setSize(600, 400);

    _adapter = new InteractionsDJDocument();
    //_rmiModel = new SimpleRMIInteractionsModel(_adapter);
    _model = new SimpleInteractionsModel(_adapter);
    _pane = new InteractionsPane(_adapter) {
      public int getPromptPos() {
        return _model.getDocument().getPromptPos();
      }
    };
    _controller = new InteractionsController(_model, _adapter, _pane);

    _pane.setFont(Font.decode("monospaced"));

    _model.addListener(new InteractionsListener() {
      public void interactionStarted() { _pane.setEditable(false); }
      public void interactionEnded() {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterResetting() { _pane.setEditable(false); }
      public void interactionErrorOccurred(int offset, int length) { _pane.highlightError(offset, length); }
      public void interpreterReady() {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterExited(int status) { }
      public void interpreterChanged(boolean inProgress) { _pane.setEditable(inProgress); }
      public void interpreterResetFailed(Throwable t) { interpreterReady(); }
      public void interactionIncomplete() {
        int caretPos = _pane.getCaretPosition();
        _controller.getConsoleDoc().insertNewLine(caretPos);
      }
    });

    JScrollPane scroll = new JScrollPane(_pane);
    getContentPane().add(scroll);

    // Add listener to quit if window is closed
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) { close(); }
    });
  }

  /** Terminates this process. This is overridden in DrJava so that is disposes of itself instead of calling 
   *  System.exit(0).
   */
  protected void close() { System.exit(0); }

  /** Accessor for the controller. */
  public InteractionsController getController() { return _controller; }

  /** Defines a variable in this window to the given value. */
  public void defineVariable(String name, Object value) { _model.defineVariable(name, value); }

  /** Defines a final variable in this window to the given value. */
  public void defineConstant(String name, Object value) { _model.defineConstant(name, value); }

  /** Sets whether protected and private variables and methods can be accessed from within the interpreter. */
  public void setInterpreterPrivateAccessible(boolean accessible) {
    _model.setInterpreterPrivateAccessible(accessible);
  }

  /** Main method to create a SimpleInteractionsWindow from the console. Doesn't take any command line arguments. */
  public static void main(String[] args) {
    SimpleInteractionsWindow w = new SimpleInteractionsWindow();
    if (args.length > 0 && args[0].equals("-debug")) {
      w.defineVariable("FRAME", w);
      w.defineVariable("CONTROLLER", w.getController());
      w.setInterpreterPrivateAccessible(true);
    }
    w.setVisible(true);
  }
}
