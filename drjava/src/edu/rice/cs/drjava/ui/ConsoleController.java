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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Event;
import java.io.Serializable;

import edu.rice.cs.drjava.model.repl.*;

/**
 * @version $Id$
 */
public class ConsoleController extends AbstractConsoleController {
  protected ConsoleDocument _doc;

  /** Object to wait on for input from System.in. */
  private Object _inputWaitObject = new Object();

  /** State so that the Enter action will only take place if the console is actually
   *  waiting for input. */
  private boolean _blockedForConsoleInput;

  public ConsoleController(final ConsoleDocument doc, InteractionsDocumentAdapter adapter) {
    super(adapter, new InteractionsPane("CONSOLE_KEYMAP", adapter) {
      public int getPromptPos() { return doc.getPromptPos(); }
    });
    _doc = doc;
    _blockedForConsoleInput = false;
    _pane.setEditable(false);
//    _pane.getCaret().setVisible(false);

    _init();
  }

  /** Gets the ConsoleDocument. */
  public ConsoleDocument getConsoleDoc() { return _doc; }

  /** Allows the main controller to install the input listener into the model. */
  public InputListener getInputListener() { return _inputListener; }

  protected void _setupModel() {
    _adapter.addDocumentListener(new CaretUpdateListener());
    _doc.setBeep(_pane.getBeep());
  }

  /** Listens for input from System.in. */
  protected InputListener _inputListener = new InputListener() {
    public String getConsoleInput() {
//     return JOptionPane.showInputDialog(MainFrame.this, "Please enter System.in:",
//                                        "System.in", JOptionPane.QUESTION_MESSAGE);
      _pane.setEditable(true);
      //_pane.getCaret().setVisible(true);
      _waitForInput();
      String s = _doc.getCurrentInput();
      _doc.disablePrompt();
      return s;
    }
  };

  /** @return the Object that the console waits on. */
  Object getInputWaitObject() { return _inputWaitObject; }

  /** Waits for _inputWaitObject to be notified. */
  protected void _waitForInput() {
    synchronized(_inputWaitObject) {
      try {
        _blockedForConsoleInput = true;
        while (_blockedForConsoleInput) _inputWaitObject.wait();
      }
      catch (InterruptedException ie) { 
        /* do nothing */
      }
    }
  }

  /** Adds actions to the view. */
  protected void _setupView() {
    super._setupView();

    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                enterAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                                                       Event.SHIFT_MASK),
                                newLineAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask),
                                clearCurrentAction);

    // Left needs to be prevented from rolling cursor back before the prompt.
    // Both left and right should lock when caret is before the prompt.
    // Caret is allowed before the prompt for the purposes of mouse-based copy-
    // and-paste.
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
                                moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                                moveLeftAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
                                moveRightAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                                moveRightAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),
                                moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
                                moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0),
                                moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                                moveUpDownAction);
  }

  AbstractAction enterAction = new EnterAction();
  private class EnterAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      synchronized(_inputWaitObject) {
        if (_blockedForConsoleInput) {
          _pane.setEditable(false);
          _pane.getCaret().setVisible(false);
          _doc.insertNewLine(_doc.getDocLength());
          _blockedForConsoleInput = false; 
          _inputWaitObject.notify();  // notify waiting thread that input is available
        }
      }
    }
  }

  /** Moves the caret left or beeps at the edge. */
  AbstractAction moveLeftAction = new LeftAction();
  private class LeftAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) moveToPrompt();
      else if (position == _doc.getPromptPos())_pane.getBeep().run();
      else // position > _doc.getPromptPos()
        _pane.setCaretPosition(position - 1);
    }
  }

  /** Moves the caret right or beeps at the edge. */
  AbstractAction moveRightAction = new RightAction();
  
  private class RightAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) moveToEnd();
      else if (position >= _doc.getDocLength()) _pane.getBeep().run();
      else // position between prompt and end
        _pane.setCaretPosition(position + 1);
    }
  }


  /**
   * Cannot move up or down at console.  Just move to the prompt if not in editable
   * area, or beep if already after the prompt.
   */
  AbstractAction moveUpDownAction = new UpDownAction();
  private class UpDownAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) moveToPrompt();
      else _pane.getBeep().run();
    }
  }
}

