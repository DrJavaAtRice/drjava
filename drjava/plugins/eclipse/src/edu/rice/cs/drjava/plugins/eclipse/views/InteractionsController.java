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

package edu.rice.cs.drjava.plugins.eclipse.views;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;

import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.SimpleInteractionsDocument;
import edu.rice.cs.drjava.model.repl.SimpleInteractionsListener;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.text.SWTDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * This class installs listeners and actions between an InteractionsDocument
 * in the model and an InteractionsPane in the view.
 * 
 * We may want to refactor this class into a different package.
 * 
 * @version $Id$
 */
public class InteractionsController {
  
  // TO DO:
  //  - What to do with unexpected exceptions?
  
  /** Adapter for an SWT document */
  protected SWTDocumentAdapter _adapter;
  
  /** Document from the model */
  protected InteractionsDocument _doc;
  
  /** Pane from the SWT view */
  protected InteractionsView _view;
  
  
  /**
   * Glue together the given model and view.
   * @param adapter DocumentAdapter that the document uses
   * @param doc InteractionsDocument from the model
   * @param pane InteractionsPane in the view
   */
  public InteractionsController(SWTDocumentAdapter adapter,
                                InteractionsDocument doc,
                                InteractionsView view) {
    _adapter = adapter;
    _doc = doc;
    _view = view;
    
    // Put the caret at the end
    _view.getTextPane().setCaretOffset(_doc.getDocLength());
    
    _setupModel();
    _setupView();
  }
  
  /**
   * Accessor method for the DocumentAdapter.
   */
  public DocumentAdapter getDocumentAdapter() {
    return _adapter;
  }
  
  /**
   * Accessor method for the InteractionsDocument.
   */
  public InteractionsDocument getDocument() {
    return _doc;
  }
  
  /**
   * Accessor method for the InteractionsPane.
   */
  public InteractionsView getView() {
    return _view;
  }
  
  /**
   * Adds listeners to the model.
   */
  protected void _setupModel() {
    _adapter.addVerifyListener(new DocumentUpdateListener());
    _doc.setBeep(_view.getBeep());
    
    // Temporary... 
    //  we will need to change this when using a real InteractionsDocument
    ((SimpleInteractionsDocument)_doc).addInteractionListener(new SimpleInteractionsListener() {
      public void interactionStarted() {
        _view.setEditable(false);
      }
      public void interactionEnded() {
        moveToPrompt();
        _view.setEditable(true);
      }
    });
  }
  
  /**
   * Listener to ensure that the document cannot be edited before the prompt
   * from the view.  Also ensures that the caret always stays on or after the
   * prompt, so that output is always scrolled to the bottom.
   */
  class DocumentUpdateListener implements VerifyListener {
    public void verifyText(VerifyEvent e) {
      // Ensure document cannot be edited before the prompt.
      DocumentEditCondition cond = _adapter.getEditCondition();
      if (!cond.canRemoveText(e.start, e.end - e.start) ||
          !cond.canInsertText(e.start, e.text, InteractionsDocument.DEFAULT_STYLE)) {
        // EditCondition says we can't
        e.doit = false;
        return;
      }
      
      // Update the caret position
      StyledText pane = _view.getTextPane();
      int caretPos = pane.getCaretOffset();
      int promptPos = _doc.getPromptPos();
      int docLength = _doc.getDocLength();

      if (_doc.inProgress()) {
        // Scroll to the end of the document, since output has been
        // inserted after the prompt.
        moveToEnd();
      }
      else {
        // Only update caret if it has fallen behind the prompt.
        // (And be careful not to move it during a reset, when the
        //  prompt pos is temporarily far greater than the length.)
        if ((caretPos < promptPos) && (promptPos <= docLength)) {
          moveToPrompt();
        }
      }
    }
  }
  
  
  /**
   * Assigns key bindings to the view.
   */
  protected void _setupView() {
    _view.getTextPane().addVerifyKeyListener(new KeyUpdateListener());
  }
  
  /**
   * Listener to perform the correct action when a key is pressed.
   */
  class KeyUpdateListener implements VerifyKeyListener {
    public void verifyKey(VerifyEvent event) {
      StyledText pane = _view.getTextPane();
      int caretPos = pane.getCaretOffset();
      //System.out.println("event consumer: keycode: " + event.keyCode);
      
      // -- Branch to an action on certain keystrokes --
      //  (needs to be refactored for better OO code)
      
      // enter
      if (event.keyCode == 13 && event.stateMask == 0) {
        event.doit = evalAction();
      }
      // shift + enter
      else if (event.keyCode == 13 && (event.stateMask & SWT.SHIFT) == 1) {
        event.doit = newLineAction();
      }
      // up
      else if (event.keyCode == SWT.ARROW_UP) {
        event.doit = historyPrevAction();
      }
      // down
      else if (event.keyCode == SWT.ARROW_DOWN) {
        event.doit = historyNextAction();
      }
      // left
      else if (event.keyCode == SWT.ARROW_LEFT) {
        event.doit = moveLeftAction();
      }
      // right
      else if (event.keyCode == SWT.ARROW_RIGHT) {
        event.doit = moveRightAction();
      }
    }
  }
  
  
//     // Get proper cross-platform mask.
//     int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
//     
//     // Add actions with keystrokes
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
//                                 evalAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 
//                                                        java.awt.Event.SHIFT_MASK), 
//                                 newLineAction);
// 
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), 
//                                 clearCurrentAction);
// 
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), 
//                                 gotoPromptPosAction);
//     
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
//                                                        java.awt.Event.SHIFT_MASK), 
//                                 selectToPromptPosAction);
// 
//     // Up and down need to be bound both for keypad and not
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), 
//                                 historyPrevAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 
//                                 historyPrevAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), 
//                                 historyNextAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
//                                 historyNextAction);
//     
//     // Left needs to be prevented from rolling cursor back before the prompt.
//     // Both left and right should lock when caret is before the prompt.
//     // Caret is allowed before the prompt for the purposes of mouse-based copy-
//     // and-paste.
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
//                                 moveLeftAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
//                                 moveLeftAction);
//     
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
//                                 moveRightAction);
//     _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
//                                 moveRightAction);
  
  
  
  /** Evaluates the interaction on the current line.
   *  (this version will be used when two JVMs are in place)
  boolean evalAction() {
    SwingWorker worker = new SwingWorker() {
      public Object construct() {
        _doc.interpretCurrentInteraction();
        return null;
      }
    };
    worker.start();
    return false;
  }*/
  
  /**
   * Submits the text in the view to the model, and appends the
   * result to the view.
   */
  boolean evalAction() {
    _doc.interpretCurrentInteraction();
    return false;
  }
  
  /** Inserts a new line at the caret position. */
  boolean newLineAction() {
    StyledText pane = _view.getTextPane();
    pane.replaceTextRange(pane.getCaretOffset(), 0, "\n");
    return false;
  }
  
  /** Recalls the previous command from the history. */
  boolean historyPrevAction() {
    _doc.recallPreviousInteractionInHistory(_view.getBeep());
    moveToEnd();
    return false;
  }

  /** Recalls the next command from the history. */
  boolean historyNextAction() {
    _doc.recallNextInteractionInHistory(_view.getBeep());
    moveToEnd();
    return false;
  }

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
      StyledText pane = _view.getTextPane();
      int start = _doc.getPromptPos();
      int end = pane.getCaretOffset();
      if (end < start) {
        int t = start;
        start = end;
        end = t;
      }
      
      pane.setSelection(start, end);
    }
  };
  
  /** Moves the caret left or wraps around. */
  boolean moveLeftAction() {
    int position = _view.getTextPane().getCaretOffset();
    if (position < _doc.getPromptPos()) {
      moveToPrompt();
    }
    else if (position == _doc.getPromptPos()) {
      // Wrap around to the end
      moveToEnd();
    }
    else { // position > _doc.getPromptPos()
      _view.getTextPane().setCaretOffset(position - 1);
    }
    return false;
  }
  
  /** Moves the caret right or wraps around. */
  boolean moveRightAction() {
    int position = _view.getTextPane().getCaretOffset();
    if (position < _doc.getPromptPos()) { 
      moveToEnd();
    }
    else if (position >= _doc.getDocLength()) {
      // Wrap around to the start
      moveToPrompt();
    }
    else { // position between prompt and end
      _view.getTextPane().setCaretOffset(position + 1);
    }
    return false;
  }
  
  /** Moves the pane's caret to the end of the document. */
  void moveToEnd() {
    _view.getTextPane().setCaretOffset(_doc.getDocLength());
  }
  
  /** Moves the pane's caret to the document's prompt. */
  void moveToPrompt() {
    _view.getTextPane().setCaretOffset(_doc.getPromptPos());
  }
}