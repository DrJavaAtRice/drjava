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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.ConsoleDocument;
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
public class InteractionsController extends AbstractConsoleController {
  /**
   * InteractionsModel to handle interpretation
   */
  protected InteractionsModel _model;
  
  /**
   * Document from the model.
   */
  protected InteractionsDocument _doc;
  
  /**
   * Style to use for error messages.
   */
  protected SimpleAttributeSet _errStyle;
  
  /**
   * Style to use for debug messages.
   */
  protected final SimpleAttributeSet _debugStyle;
  
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
    super(adapter, pane);
    _model = model;
    _doc = model.getDocument();
    _errStyle = new SimpleAttributeSet();
    _debugStyle = new SimpleAttributeSet();
    
    _init();
  }
  
  /**
   * Accessor method for the InteractionsModel.
   */
  public InteractionsModel getInteractionsModel() {
    return _model;
  }
  
  /**
   * Allows the abstract superclass to use the document.
   * @return the InteractionsDocument
   */
  public ConsoleDocument getConsoleDoc() {
    return _doc;
  }

  /**
   * Accessor method for the InteractionsDocument.
   */
  public InteractionsDocument getDocument() {
    return _doc;
  }
  
  /**
   * Adds AttributeSets as named styles to the document adapter.
   */
  protected void _addDocumentStyles() {
    // Add AbstractConsoleController styles
    super._addDocumentStyles();

    // Error
    _errStyle.addAttributes(_defaultStyle);
    _errStyle.addAttribute(StyleConstants.Foreground, Color.red.darker());
    _errStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _adapter.setDocStyle(InteractionsDocument.ERROR_STYLE, _errStyle);
    
    // Debug
    _debugStyle.addAttributes(_defaultStyle);
    _debugStyle.addAttribute(StyleConstants.Foreground, Color.blue.darker());
    _debugStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _adapter.setDocStyle(InteractionsDocument.DEBUGGER_STYLE, _debugStyle);
  }
  
  /**
   * Updates all document styles with the attributes contained in newSet.
   * This behavior is only used in Mac OS X, JDK 1.4.1, since
   * setFont() works fine on JTextPane on all other tested platforms.
   * @param newSet Style containing new attributes to use.
   */
  protected void _updateStyles(AttributeSet newSet) {
    super._updateStyles(newSet);
    _errStyle.addAttributes(newSet);
    StyleConstants.setBold(_errStyle, true);  // ensure err is always bold
    _debugStyle.addAttributes(newSet);
    StyleConstants.setBold(_debugStyle, true);  // ensure debug is always bold
  }
  
  /**
   * Adds listeners to the model.
   */
  protected void _setupModel() {
    _adapter.addDocumentListener(new CaretUpdateListener());
    _doc.setBeep(_pane.getBeep());
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
    // Prevent previous word action from going past the prompt
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD),
                                prevWordAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD),
                                nextWordAction);
  }

  /**
   * determines if the interactions pane is currently computing or resetting.
   * @return true iff the interactions pane is currently busy
   */
  protected boolean _busy() {
    // should also check to see if we are resetting the interactions pane
    return _doc.inProgress();
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
  
  /** Recalls the previous command from the history. */
  AbstractAction historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.recallPreviousInteractionInHistory();
        moveToEnd();
      }
    }
  };

  /** Recalls the next command from the history. */
  AbstractAction historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
        _doc.recallNextInteractionInHistory();
        moveToEnd();
      }
    }
  };

  /** Moves the caret left or wraps around. */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
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

  AbstractAction prevWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) {
        moveToPrompt();
      }
      else if (position == promptPos) {
        // Wrap around to the end
        moveToEnd();
      }
     else {
        _pane.getActionMap().get(DefaultEditorKit.previousWordAction).actionPerformed(e);
      }
    }
  };

  AbstractAction nextWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) {
        moveToEnd();
      }
      else if (position >= _doc.getDocLength()) {
        // Wrap around to the start
        moveToPrompt();
      }
      else {
        _pane.getActionMap().get(DefaultEditorKit.nextWordAction).actionPerformed(e);
      }
    }
  };
}
