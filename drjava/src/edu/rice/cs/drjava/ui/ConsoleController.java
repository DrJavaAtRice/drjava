/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Event;
import javax.swing.text.DefaultEditorKit;
import java.io.Serializable;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.swing.Utilities;

/** @version $Id$ */
public class ConsoleController extends AbstractConsoleController implements Serializable {
  protected ConsoleDocument _doc;

  /** Object to wait on for input from System.in. */
  private Object _inputWaitObject = new Object();

  /** State so that the Enter action will only take place if the console is actually waiting for input. */
  private volatile boolean _blockedForConsoleInput;

  public ConsoleController(final ConsoleDocument doc, InteractionsDJDocument adapter) {
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
      Utilities.invokeAndWait(new Runnable() {
        public void run() { _pane.setEditable(true); }
      });
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

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), enterAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.SHIFT_MASK), newLineAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), clearCurrentAction);

    // Left needs to be prevented from rolling cursor back before the prompt.
    // Both left and right should lock when caret is before the prompt.
    // Caret is allowed before the prompt for the purposes of mouse-based copy-
    // and-paste.
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), moveLeftAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), moveRightAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), moveRightAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), moveUpDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), moveUpDownAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PASTE_FROM_HISTORY), pasteAction);
  }
      
  /** No-op paste action. */
  Action pasteAction = new DefaultEditorKit.PasteAction() {
    public void actionPerformed(ActionEvent e) { }
  };

  AbstractAction enterAction = new EnterAction();
  private class EnterAction extends AbstractAction implements Serializable {
    public void actionPerformed(ActionEvent e) {
      synchronized(_inputWaitObject) {
        if (_blockedForConsoleInput) {
          _pane.setEditable(false);
          _pane.getCaret().setVisible(false);
          _doc.insertNewline(_doc.getLength());
          _blockedForConsoleInput = false; 
          _inputWaitObject.notify();  // notify waiting thread that input is available
        }
      }
    }
  }

  /** Moves the caret left or beeps at the edge. */
  AbstractAction moveLeftAction = new LeftAction();
  private class LeftAction extends AbstractAction implements Serializable {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock(); 
      try {
        int position = _pane.getCaretPosition();
        if (position < _doc.getPromptPos()) moveToPrompt();
        else if (position == _doc.getPromptPos())_pane.getBeep().run();
        else // position > _doc.getPromptPos()
          _pane.setCaretPosition(position - 1);
      }
      finally { _doc.releaseReadLock(); }
    }
  }

  /** Moves the caret right or beeps at the edge. */
  AbstractAction moveRightAction = new RightAction();
  
  private class RightAction extends AbstractAction implements Serializable {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock();
      try {
        int position = _pane.getCaretPosition();
        if (position < _doc.getPromptPos()) moveToEnd();
        else if (position >= _doc.getLength()) _pane.getBeep().run();
        else // position between prompt and end
          _pane.setCaretPosition(position + 1);
      }
      finally { _doc.releaseReadLock(); }
    }
  }


  /** Cannot move up or down at console.  Just move to the prompt if not in editable area, or beep if already after 
    * the prompt.
    */
  AbstractAction moveUpDownAction = new UpDownAction();
  private class UpDownAction extends AbstractAction implements Serializable {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock();
      try {
        int position = _pane.getCaretPosition();
        if (position < _doc.getPromptPos()) moveToPrompt();
        else _pane.getBeep().run();
      }
      finally { _doc.releaseReadLock(); }
    }
  }
}

