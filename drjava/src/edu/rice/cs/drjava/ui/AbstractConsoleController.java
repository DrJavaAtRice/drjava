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

import edu.rice.cs.drjava.model.repl.ConsoleDocument;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * Abstract class to handle hooking up a console document with its pane.
 * @version $Id$
 */
public abstract class AbstractConsoleController {
  /**
   * Adapter for the Swing document used by the model.
   */
  protected SwingDocumentAdapter _adapter;
  
  /**
   * Pane from the view.
   */
  protected InteractionsPane _pane;

  /**
   * Initializes the document adapter and interactions pane.
   * Subclasses *must* call _init() at the end of their constructors.
   */
  protected AbstractConsoleController(SwingDocumentAdapter adapter,
                                      InteractionsPane pane) {
    _adapter = adapter;
    _pane = pane;
  }
  
  /**
   * Gets the console document for this console.
   */
  public abstract ConsoleDocument getConsoleDoc();

  /**
   * Initialization method.  *Must* be called in constructor by all subclasses.
   */
  protected void _init() {
    _addDocumentStyles();
    _setupModel();
    _setupView();
  }

  /**
   * Adds AttributeSets as named styles to the document adapter.
   */
  protected void _addDocumentStyles() {
    // Default
    SimpleAttributeSet defaultS = new SimpleAttributeSet();
    _adapter.addDocStyle(ConsoleDocument.DEFAULT_STYLE, defaultS);

    // System.out
    SimpleAttributeSet s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.green.darker().darker());
    _adapter.addDocStyle(ConsoleDocument.SYSTEM_OUT_STYLE, s);
   
    // System.err
    s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.red);
    _adapter.addDocStyle(ConsoleDocument.SYSTEM_ERR_STYLE, s);
  }

  /**
   * Sets up the model.
   */
  protected abstract void _setupModel();

  /**
   * Listener to ensure that the caret always stays on or after the
   * prompt, so that output is always scrolled to the bottom.
   * (The prompt is always at the bottom.)
   */
  class CaretUpdateListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      ConsoleDocument doc = getConsoleDoc();
      int caretPos = _pane.getCaretPosition();
      int promptPos = doc.getPromptPos();
      int length = doc.getDocLength();
      
      // Figure out where the prompt was before the update
      int prevPromptPos = promptPos;
      if (e.getOffset() < promptPos) {
        // Insert happened before prompt,
        //  so previous position was further back
        prevPromptPos = promptPos - e.getLength();
      }

      if (!doc.hasPrompt()) {
        // Scroll to the end of the document, since output has been
        // inserted after the prompt.
        moveToEnd();
      }
      // (Be careful not to move caret during a reset, when the
      //  prompt pos is temporarily far greater than the length.)
      else if (promptPos <= length) {
        if (caretPos < prevPromptPos) {
          // Caret has fallen behind prompt, so make it catch up so
          //  the new input is visible.
          moveToPrompt();
        }
        else {
          // Caret was on or after prompt, so move it right by the size
          //  of the insert.
          int size = promptPos - prevPromptPos;
          if (size > 0) {
            moveTo(caretPos + size);
          }
        }
      }
    }

    public void removeUpdate(DocumentEvent e) {
      _ensureLegalCaretPos();
    }
    public void changedUpdate(DocumentEvent e) {
      _ensureLegalCaretPos();
    }
    protected void _ensureLegalCaretPos() {
      int length = getConsoleDoc().getDocLength();
      if (_pane.getCaretPosition() > length) {
        _pane.setCaretPosition(length);
      }
    }
  }
  
  
  /**
   * Sets up the view.
   */
  protected abstract void _setupView();

  /**
   * Accessor method for the SwingDocumentAdapter.
   */
  public SwingDocumentAdapter getDocumentAdapter() {
    return _adapter;
  }
  
  /**
   * Accessor method for the InteractionsPane.
   */
  public InteractionsPane getPane() {
    return _pane;
  }
  
  /** Inserts a new line at the caret position. */
  AbstractAction newLineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      getConsoleDoc().insertNewLine(_pane.getCaretPosition());
    }
  };
  
  /** Removes all text after the prompt. */
  AbstractAction clearCurrentAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      getConsoleDoc().clearCurrentInput();
    }
  };

  /** Moves the caret to the prompt. */
  AbstractAction gotoPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      moveToPrompt();
    }
  };
  
  AbstractAction selectToPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      // Selects the text between the old pos and the prompt
      _pane.moveCaretPosition(getConsoleDoc().getPromptPos());
    }
  };
  
  /** Moves the pane's caret to the end of the document. */
  void moveToEnd() {
    moveTo(getConsoleDoc().getDocLength());
  }
  
  /** Moves the pane's caret to the document's prompt. */
  void moveToPrompt() {
    moveTo(getConsoleDoc().getPromptPos());
  }
  
  /** Moves the pane's caret to the given position, as long as it's legal. */
  void moveTo(int pos) {
    // Sanity check
    if (pos < 0) pos = 0;
    if (pos > getConsoleDoc().getDocLength()) pos = getConsoleDoc().getDocLength();
    
    _pane.setCaretPosition(pos);
  }
}