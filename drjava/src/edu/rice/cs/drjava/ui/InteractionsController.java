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

import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * This class installs listeners and actions between an InteractionsDocument
 * in the model and an InteractionsPane in the view.
 * 
 * We may want to refactor this class into a different package.
 * 
 * @version $Id$
 */
public class InteractionsController {
  /** InteractionsModel to handle interpretation */
  protected InteractionsModel _model;
  
  /** Document from the model */
  protected InteractionsDocument _doc;
  
  /** Adapter for the Swing document used by the model. */
  protected SwingDocumentAdapter _adapter;
  
  /** Pane from the view */
  protected InteractionsPane _pane;
  
  /**
   * Glue together the given model and a new view.
   * @param model An InteractionsModel
   * @param adapter SwingDocumentAdapter being used by the model's doc
   */
  public InteractionsController(InteractionsModel model, 
                                SwingDocumentAdapter adapter)
  {
    this(model, adapter, new InteractionsPane(adapter));
  }
  
  /**
   * Glue together the given model and view.
   * @param model An InteractionsModel
   * @param adapter SwingDocumentAdapter being used by the model's doc
   * @param pane An InteractionsPane
   */
  public InteractionsController(InteractionsModel model, 
                                SwingDocumentAdapter adapter, 
                                InteractionsPane pane)
  {
    _model = model;
    _doc = model.getDocument();
    _adapter = adapter;
    _pane = pane;
    
    _addDocumentStyles();
    _setupModel();
    _setupView();
  }
  
  /**
   * Accessor method for the InteractionsModel.
   */
  public InteractionsModel getInteractionsModel() {
    return _model;
  }
  
  /**
   * Accessor method for the InteractionsDocument.
   */
  public InteractionsDocument getDocument() {
    return _doc;
  }
  
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
  
  /**
   * Adds AttributeSets as named styles to the document adapter.
   */
  protected void _addDocumentStyles() {
    // Default
    SimpleAttributeSet defaultS = new SimpleAttributeSet();
    _adapter.addDocStyle(InteractionsDocument.DEFAULT_STYLE, defaultS);

    // System.out
    SimpleAttributeSet s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.green.darker().darker());
    _adapter.addDocStyle(InteractionsDocument.SYSTEM_OUT_STYLE, s);
   
    // System.err
    s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.red);
    _adapter.addDocStyle(InteractionsDocument.SYSTEM_ERR_STYLE, s);
    
    // Error
    s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    s.addAttribute(StyleConstants.Bold, new Boolean(true));
    _adapter.addDocStyle(InteractionsDocument.ERROR_STYLE, s);
    
    // Debug
    s = new SimpleAttributeSet(defaultS);
    s.addAttribute(StyleConstants.Foreground, Color.blue.darker());
    s.addAttribute(StyleConstants.Bold, new Boolean(true));
    _adapter.addDocStyle(InteractionsDocument.DEBUGGER_STYLE, s);
    
  }
  
  
  /**
   * Adds listeners to the model.
   */
  protected void _setupModel() {
    _adapter.addDocumentListener(new CaretUpdateListener());
    _doc.setBeep(_pane.getBeep());
  }
  
  /**
   * Listener to ensure that the caret always stays on or after the
   * prompt, so that output is always scrolled to the bottom.
   * (The prompt is always at the bottom.)
   */
  class CaretUpdateListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      int caretPos = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      int length = _doc.getDocLength();
      
      // Figure out where the prompt was before the update
      int prevPromptPos = promptPos;
      if (e.getOffset() < promptPos) {
        // Insert happened before prompt,
        //  so previous position was further back
        prevPromptPos = promptPos - e.getLength();
      }

      if (_doc.inProgress()) {
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
      int length = _doc.getDocLength();
      if (_pane.getCaretPosition() > length) {
        _pane.setCaretPosition(length);
      }
    }
  }
  
  
  
  /**
   * Adds actions to the view.
   */
  protected void _setupView() {
    
    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    // Add actions with keystrokes
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
                                evalAction);
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

    // Up and down need to be bound both for keypad and not
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), 
                                historyPrevAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 
                                historyPrevAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), 
                                historyNextAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
                                historyNextAction);
    
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
  }
  
  
  
  
  // The fields below were made package private for testing purposes.
  
  /** Evaluates the interaction on the current line. */
  AbstractAction evalAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      SwingWorker worker = new SwingWorker() {
        public Object construct() {
          _model.interpretCurrentInteraction();
          return null;
        }
      };
      worker.start();
    }
  };
  
  /** Inserts a new line at the caret position. */
  AbstractAction newLineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.insertNewLine(_pane.getCaretPosition());
    }
  };
  
  /** Recalls the previous command from the history. */
  AbstractAction historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.recallPreviousInteractionInHistory();
      moveToEnd();
    }
  };

  /** Recalls the next command from the history. */
  AbstractAction historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.recallNextInteractionInHistory();
      moveToEnd();
    }
  };

  /** Removes all text after the prompt. */
  AbstractAction clearCurrentAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.clearCurrentInteraction();
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
      _pane.moveCaretPosition(_doc.getPromptPos());
    }
  };
  
  /** Moves the caret left or wraps around. */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) {
        moveToPrompt();
      }
      else if (position == _doc.getPromptPos()) {
        // Wrap around to the end
        moveToEnd();
      }
      else { // position > _doc.getPromptPos()
        _pane.setCaretPosition(position - 1);
      }
    }
  };

  /** Moves the caret right or wraps around. */
  AbstractAction moveRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      if (position < _doc.getPromptPos()) { 
        moveToEnd();
      }
      else if (position >= _doc.getDocLength()) {
        // Wrap around to the start
        moveToPrompt();
      }
      else { // position between prompt and end
        _pane.setCaretPosition(position + 1);
      }
    }
  };
  
  /** Moves the pane's caret to the end of the document. */
  void moveToEnd() {
    moveTo(_doc.getDocLength());
  }
  
  /** Moves the pane's caret to the document's prompt. */
  void moveToPrompt() {
    moveTo(_doc.getPromptPos());
  }
  
  /** Moves the pane's caret to the given position, as long as it's legal. */
  void moveTo(int pos) {
    // Sanity check
    if (pos < 0) pos = 0;
    if (pos > _doc.getDocLength()) pos = _doc.getDocLength();
    
    _pane.setCaretPosition(pos);
  }
}
