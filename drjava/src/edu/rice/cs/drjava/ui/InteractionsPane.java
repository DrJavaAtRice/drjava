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
import java.awt.Toolkit;
import java.awt.event.*;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.*;

/**
 * The view component for repl interaction.
 *
 * @version $Id$
 */
public class InteractionsPane extends JTextPane {
  private static final EditorKit EDITOR_KIT = new InteractionsEditorKit();

  private final GlobalModel _model;

  // The fields below were made package private for testing purposes.
  
  AbstractAction _evalAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.interpretCurrentInteraction();
    }
  };
  
  Runnable BEEP = new Runnable() {
    public void run() {
        Toolkit.getDefaultToolkit().beep();      
    }
  };
  
  AbstractAction _historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.recallPreviousInteractionInHistory(BEEP);
      moveToEnd();
    }
  };

  AbstractAction _historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.recallNextInteractionInHistory(BEEP);
      moveToEnd();
    }
  };

  AbstractAction _clearCurrentAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _model.clearCurrentInteraction();
    }
  };

  AbstractAction _gotoFrozenPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      moveToPrompt();
    }
  };
  
  AbstractAction _selectToFrozenPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      selectToPrompt();
    }
  };
  
  AbstractAction _moveLeft = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (getCaretPosition() < _model.getInteractionsFrozenPos()) {
        moveToPrompt();
      }
      else if (getCaretPosition() == _model.getInteractionsFrozenPos()) {
        moveToEnd();
      }
      else { // getCaretPosition() > _model.getInteractionsFrozenPos()
        moveLeft();
      }
    }
  };

  AbstractAction _moveRight = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = getCaretPosition();
      if (position < _model.getInteractionsFrozenPos()) { 
        moveToEnd();
      }
      else if (position >= _model.getInteractionsDocument().getLength())
      {
        moveToPrompt();
      }
      else { // getCaretPosition() between prompt and end
        moveRight();
      }
    }
  };

  /**
   * Overriding this method ensures that all new documents created in this
   * editor pane use our editor kit (and thus our model).
   */
  protected EditorKit createDefaultEditorKit() {
    return EDITOR_KIT;
  }

  public InteractionsPane(GlobalModel model) {
    super(model.getInteractionsDocument());
    _model = model;
    
    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    //add actions for enter key, etc.
    Keymap ourMap = addKeymap("INTERACTIONS_KEYMAP", getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
                                 _evalAction);

    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), 
                                 _clearCurrentAction);

    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), 
                                 _gotoFrozenPosAction);
    
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                                                        java.awt.Event.SHIFT_MASK), 
                                 _selectToFrozenPosAction);

    // Up and down need to be bound both for keypad and not
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), 
                                 _historyPrevAction);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 
                                 _historyPrevAction);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), 
                                 _historyNextAction);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
                                 _historyNextAction);
    
    // Left needs to be prevented from rolling cursor back before the prompt.
    // Both left and right should lock when caret is before the prompt.
    // Caret is allowed before the prompt for the purposes of mouse-based copy-
    // and-paste.
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0),
                                 _moveLeft);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
                                 _moveLeft);
    
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0),
                                 _moveRight);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                                 _moveRight);
    
    setKeymap(ourMap);
  }
  
  private void moveToEnd() {
    setCaretPosition(_model.getInteractionsDocument().getLength());
  }
  
  private void moveToPrompt() {
    setCaretPosition(_model.getInteractionsFrozenPos());
  }
  
  private void selectToPrompt() {
    // Selects the text between the old pos and the prompt
    moveCaretPosition(_model.getInteractionsFrozenPos());
  }
  
  private void moveLeft() {
    setCaretPosition(getCaretPosition() - 1);
  }
  
  private void moveRight() {
    setCaretPosition(getCaretPosition() + 1);
  }
}



