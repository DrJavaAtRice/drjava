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

import  javax.swing.*;
import  javax.swing.undo.*;
import  javax.swing.event.*;
import  javax.swing.text.*;
import  java.awt.*;
import  java.awt.event.*;
import  java.io.*;
import  java.util.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;

/**
 * The pane in which work on a given OpenDefinitionsDocument occurs.
 * A DefinitionsPane is tied to a single document, which cannot be
 * changed.
 * @version $Id$
 */
public class DefinitionsPane extends JEditorPane {
  private static final EditorKit EDITOR_KIT = new DefinitionsEditorKit();

  /**
   * Our parent window.
   */
  private MainFrame _mainFrame;
  private GlobalModel _model;
  private final OpenDefinitionsDocument _doc;
  private UndoManager _undoManager;
  private UndoAction _undoAction;
  private RedoAction _redoAction;
  /**
   * Our current paren/brace/bracket matching highlight.
   */
  private Object _matchHighlight = null;
  /**
   * Paren/brace/bracket matching highlight color.
   */
  private static DefaultHighlighter.DefaultHighlightPainter
    _highlightPainter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);

  /**
   * Our current error matching highlight.
   */
  private Object _errorHighlightTag = null;
  /**
   * Highlight painter for selected errors in the defs doc.
   */
  private static final DefaultHighlighter.DefaultHighlightPainter
    _errorHighlightPainter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  /**
   * Listens to caret to highlight errors as appropriate.
   */
  private CompilerErrorCaretListener _errorListener;

  /**
   * Looks for changes in the caret position to see if a paren/brace/bracket
   * highlight is needed.
  */
  private CaretListener _matchListener = new CaretListener() {
    /**
     * Checks caret position to see if it needs to set or remove a highlight
     * from the document.
     * When the cursor is immediately right of ')', '}', or ']', it highlights
     * up to the matching open paren/brace/bracket.
     * @param e the event fired by the caret position change
     */
    public void caretUpdate(CaretEvent e) {
      //_doc().setCurrentLocation(getCaretPosition());
      _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
      _removePreviousHighlight();
      try {
        _updateMatchHighlight();
      } catch (BadLocationException ex) {}
    }
  };

  /**
   * Updates the highlight if there is any.
   * @exception BadLocationException
   */
  private void _updateMatchHighlight() throws BadLocationException {
    int to = getCaretPosition();
    int from = _doc.balanceBackward(); //_doc()._reduced.balanceBackward();
    if (from == -1) {}
    else {
      from = to - from;
      _addHighlight(from, to);
      Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
    }
  }

  /**
   * Adds a highlight to the document.  Called by _updateMatchHighlight().
   * @param from start of highlight
   * @param to end of highlight
   * @exception BadLocationException
   */
  private void _addHighlight(int from, int to) throws BadLocationException {
    _matchHighlight = getHighlighter().addHighlight(from, to, _highlightPainter);
  }

  /**
   * Removes the previous highlight so document is cleared when caret position changes.
   */
  private void _removePreviousHighlight() {
    if (_matchHighlight != null) {
      getHighlighter().removeHighlight(_matchHighlight);
      _matchHighlight = null;
    }
  }
  private UndoableEditListener _undoListener = new UndoableEditListener() {

    /**
     * The function to handle what happens when an UndoableEditEvent occurs.
     * @param e
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      //Remember the edit and update the menus
      _undoManager.addEdit(e.getEdit());
      _undoAction.updateUndoState();
      _redoAction.updateRedoState();
    }
  };

  /**
   * An action to handle indentation spawned by pressing the tab key.
   */
  private class IndentKeyActionTab extends AbstractAction {

    /** Handle the key typed event from the text field. */
    public void actionPerformed(ActionEvent e) {
      //int pos = getCaretPosition();
      //_doc().setCurrentLocation(pos);
      _indent();
    }
  }


  /**
   * Used for indent action spawned by pressing the enter key, '{', or '}'.
   */
  private class IndentKeyAction extends AbstractAction {
    /**
     * The key string ("\n"|"{"|"}") for the key pressed that invokes this
     * instance. Not used currently, but there for readability and possible
     * future use, e.g., debugging add-ons or the rewrite of the indention code.
     */
    private final String _key;

    /**
     * The default action to take when the specified key is pressed.
     */
    private final Action _defaultAction;

    IndentKeyAction(String key, Action defaultAction) {
      _key = key;
      _defaultAction = defaultAction;
    }

    /**
     * Handle the "key typed" event from the text field.
     * Calls the default action to make sure the right things happen, then makes
     * a call to indentLine().
     */
    public void actionPerformed(ActionEvent e) {
      _defaultAction.actionPerformed(e);
      _indent();
    }
  }

  /**
   * Special action to take care of case when tab key is pressed.
   */
  private Action _indentKeyActionTab = new IndentKeyActionTab();
  /**
   * Because the "default" action for the enter key is special, it must be
   * grabbed from the Keymap using getAction(KeyStroke), which returns the
   * "default" action for all keys which have behavior extending beyond
   * regular text keys.
   */
  private Action _indentKeyActionLine =
    new IndentKeyAction("\n",
                        getKeymap().getAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));

  /**
   * Likewise, regular text keys like '{', '}', and ':' do not have special actions
   * that are returned by getAction(KeyStroke). To make sure these behave right,
   * we use getDefaultAction() instead.
   */
  private Action _indentKeyActionSquiggly =
    new IndentKeyAction("}", getKeymap().getDefaultAction());

  private Action _indentKeyActionOpenSquiggly =
    new IndentKeyAction("{", getKeymap().getDefaultAction());
  
  private Action _indentKeyActionColon =
    new IndentKeyAction(":", getKeymap().getDefaultAction());

  /**
   * Constructor.  Sets up all the defaults.
   * @param mf the parent window
   */
  public DefinitionsPane(MainFrame mf,
                         GlobalModel model,
                         OpenDefinitionsDocument doc)
  {
    _mainFrame = mf;
    _model = model;
    _doc = doc;
    setDocument(_doc);
    setContentType("text/java");
    setBackground(Color.white);
    setFont(new Font("Courier", 0, 12));
    setEditable(true);

    //add actions for indent key
    Keymap ourMap = addKeymap("INDENT_KEYMAP", getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                 (Action)_indentKeyActionLine);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                                 (Action)_indentKeyActionTab);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('}'),
      (Action)_indentKeyActionSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('{'),
      (Action)_indentKeyActionOpenSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(':'),
      (Action)_indentKeyActionColon);
    setKeymap(ourMap);

    // Add listener that checks if position in the document has changed.
    // If it has changed, check and see if we should be highlighting matching braces.
    this.addCaretListener(_matchListener);
  }

  /**
   * @return the undo action
   */
  public Action getUndoAction() {
    return  _undoAction;
  }

  /**
   * @return the redo action
   */
  public Action getRedoAction() {
    return  _redoAction;
  }

  /**
   * Get the OpenDefinitionsDocument contained in this DefinitionsPane.
   */
  public OpenDefinitionsDocument getOpenDocument() {
    return _doc;
  }

  /**
   * Set the caret position and also scroll to make sure the location is
   * visible.
   * @param pos Location to scroll to.
   */
  public void setPositionAndScroll(int pos) {
    try {
      setCaretPosition(pos);
      scrollRectToVisible(modelToView(pos));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }


  /**
   * Override JEditorPane's setDocument to make sure only
   * the Document in our final OpenDefinitionsDocument can
   * be used.
   */
  public void setDocument(Document doc) {
    if (_doc != null) {
      if ((doc == null) || (!doc.equals(_doc.getDocument()))) {
        throw new IllegalStateException("Cannot set the document of " +
                                        "a DefinitionsPane to a " +
                                        "different document.");
      }
    }
    super.setDocument(doc);
  }

  /**
   * Add a CompilerErrorCaretListener to this pane, keeping it
   * accessible so its error model can be updated later.
   */
  public void addErrorCaretListener(CompilerErrorCaretListener listener) {
    _errorListener = listener;
    addCaretListener(listener);
  }

  /**
   * Gets the CompilerErrorCaretListener for this pane.
   */
  public CompilerErrorCaretListener getErrorCaretListener() {
    return _errorListener;
  }

  /**
   * Adds an error highlight to the document.
   * @exception BadLocationException
   */
  public void addErrorHighlight(int from, int to)
    throws BadLocationException
  {
    removeErrorHighlight();
    _errorHighlightTag =
      getHighlighter().addHighlight(from, to, _errorHighlightPainter);
  }

  /**
   * Removes the previous error highlight from the document after the cursor
   * has moved.
   */
  public void removeErrorHighlight() {
    if (_errorHighlightTag != null) {
      getHighlighter().removeHighlight(_errorHighlightTag);
      _errorHighlightTag = null;
    }
  }



  /**
   * Reset undo machinery on setDocument.
   */
  private void setDocument(OpenDefinitionsDocument doc) {
    //DrJava.consoleErr().println("Reset doc: " + doc);
    super.setDocument(doc.getDocument());

    if (_undoAction == null) {
      _undoAction = new UndoAction();
    }
    if (_redoAction == null) {
      _redoAction = new RedoAction();
    }
    _resetUndo();
  }

  /**
   * Reset the document Undo list.
   */
  private void _resetUndo() {
    _undoManager = new UndoManager();
    getDocument().addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }


  /**
   * Overriding this method ensures that all new documents created in this
   * editor pane use our editor kit (and thus our model).
   */
  protected EditorKit createDefaultEditorKit() {
    return EDITOR_KIT;
  }


  /**
   * Perform an indent either on the current line or on the given
   * selected box of text.  Calls are sent to GlobalModel which are then
   * forwarded on to DefinitionsDocument.  Hopefully the indent code
   * will be fixed and corrected so this doesn't look so ugly.
   * The purpose is to divorce the pane from the document so we can just
   * pass a document to DefinitionsPane and that's all it cares about.
   */
  private void _indent() {
    // because _indent() is a function called directly by the Keymap
    // it does not go through the regular insertString channels and thus
    // it may not be in sync with the document's position.  For that
    // reason, we must sync the document with the pane before we go
    // ahead and indent.
    // old: _doc().setCurrentLocation(getCaretPosition());
    // new:
    _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
    int selStart = getSelectionStart();
    int selEnd = getSelectionEnd();
    
    // Show a wait cursor for reasonable sized blocks
    boolean showWaitCursor = selEnd > (selStart + 100);

    // Temporary hack because of slow indent...
    //  Prompt if more than 2000 characters to be indented
    boolean doIndent = true;
    if (selEnd > (selStart + 2000)) {
      Object[] options = {"Yes","No"};
      int n = JOptionPane.showOptionDialog
        (_mainFrame,
         "Re-indenting this block may take a very long time.  Are you sure?",
         "Confirm Re-indent",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         options,
         options[1]);
      if (n==JOptionPane.NO_OPTION) { doIndent = false; }
    }
    
    // Do the indent
    if (doIndent) {
      if (showWaitCursor) {
        _mainFrame.hourglassOn();
      }
      _doc.indentLinesInDefinitions(selStart, selEnd);
      setCaretPosition(_doc.getCurrentDefinitionsLocation());
      if (showWaitCursor) {
        _mainFrame.hourglassOff();
      }
    }
  }


  /**
   * The undo action.
   */
  private class UndoAction extends AbstractAction {

    /**
     * Constructor.
    */
    private UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    /**
     * What to do when user chooses to undo.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        _undoManager.undo();
      } catch (CannotUndoException ex) {
        System.out.println("Unable to undo: " + ex);
        ex.printStackTrace();
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }


    /**
     * Updates the undo list, i.e., where we are as regards undo and redo.
     */
    protected void updateUndoState() {
      if (_undoManager.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getUndoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }

  /**
   * Redo action.
   */
  private class RedoAction extends AbstractAction {

    /**
     * Constructor.
     */
    private RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    /**
     * In the event that the user chooses to redo something, this is what's called.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        _undoManager.redo();
      } catch (CannotRedoException ex) {
        System.out.println("Unable to redo: " + ex);
        ex.printStackTrace();
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    /**
     * Updates the redo state, i.e., where we are as regards undo and redo.
     */
    protected void updateRedoState() {
      if (_undoManager.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getRedoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }

}
