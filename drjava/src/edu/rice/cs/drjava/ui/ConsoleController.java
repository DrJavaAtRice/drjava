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

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.Color;

import edu.rice.cs.drjava.model.InputListener;
import edu.rice.cs.drjava.model.repl.ConsoleDocument;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * @version $Id$
 */
public class ConsoleController extends AbstractConsoleController {
  protected ConsoleDocument _doc;
  
  /**
   * Object to wait on for input from System.in.
   */
  private Object _inputWaitObject = new Object();
  
  /**
   * State so that the Enter action will only take place if the console is actually
   * waiting for input.
   */
  private boolean _waiting;

  public ConsoleController(ConsoleDocument doc, SwingDocumentAdapter adapter) {
    super(adapter, new InteractionsPane("CONSOLE_KEYMAP", adapter));
    _doc = doc;
    _waiting = false;
    _pane.setEditable(false);
//    _pane.getCaret().setVisible(false);

    _init();
  }
  
  /**
   * Gets the ConsoleDocument.
   */
  public ConsoleDocument getConsoleDoc() {
    return _doc;
  }

  /**
   * Allows the main controller to install the input listener into the model.
   */
  public InputListener getInputListener() {
    return _inputListener;
  }
  
  protected void _setupModel() {
    _adapter.addDocumentListener(new CaretUpdateListener());
    _doc.setBeep(_pane.getBeep());
  }

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

  /**
   * @return the Object that the console waits on
   */
  Object getInputWaitObject() {
    return _inputWaitObject;
  }

  /**
   * Waits for _inputWaitObject to be notified.
   */
  protected void _waitForInput() {
    synchronized(_inputWaitObject) {
      try {
        _waiting = true;
        _inputWaitObject.wait();
      }
      catch (InterruptedException ie) {
      }
    }
  }

  /**
   * Adds actions to the view.
   */
  protected void _setupView() {
    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                enterAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
                                                       java.awt.Event.SHIFT_MASK), 
                                newLineAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), 
                                clearCurrentAction);

    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), 
                                gotoPromptPosAction);
    
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                                                       java.awt.Event.SHIFT_MASK), 
                                selectToPromptPosAction);

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

  AbstractAction enterAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (_waiting) {
        _pane.setEditable(false);
        _pane.getCaret().setVisible(false);
        _doc.insertNewLine(_doc.getDocLength());
        synchronized(_inputWaitObject) {
          _inputWaitObject.notify();
          _waiting = false;
        }
      }
    }
  };

  /** Moves the caret left or beeps at the edge. */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) {
        moveToPrompt();
      }
      else if (position == _doc.getPromptPos()) {
        _pane.getBeep().run();
      }
      else { // position > _doc.getPromptPos()
        _pane.setCaretPosition(position - 1);
      }
    }
  };

  /** Moves the caret right or beeps at the edge. */
  AbstractAction moveRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) { 
        moveToEnd();
      }
      else if (position >= _doc.getDocLength()) {
        _pane.getBeep().run();
      }
      else { // position between prompt and end
        _pane.setCaretPosition(position + 1);
      }
    }
  };

  /**
   * Cannot move up or down at console.  Just move to the prompt if not in editable
   * area, or beep if already after the prompt.
   */
  AbstractAction moveUpDownAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) { 
        moveToPrompt();
      }
      else {
        _pane.getBeep().run();
      }
    }
  };
}