package  edu.rice.cs.drjava;

import  javax.swing.*;
import  javax.swing.undo.*;
import  javax.swing.event.*;
import  javax.swing.text.*;
import  java.awt.*;
import  java.awt.event.*;
import  java.io.*;
import  java.util.*;


/**
 * The pane in which work on the current document occurs.
 * @version $Id$
 */
public class DefinitionsPane extends JEditorPane {
  /**
   * Our parent window.
   */
  private MainFrame _mainFrame;
  private GlobalModel _model;
  private UndoManager _undoManager;
  private UndoAction _undoAction;
  private RedoAction _redoAction;
  /** 
   * For find and replace. 
   * We have a persistent dialog so it keeps track of our last find criterion.
   */
  private FindReplaceDialog _findReplace;
  /**
   * Our current paren/brace/bracket matching highlight.
   */
  private Object _matchHighlight = null;
  /**
   * Paren/brace/bracket matching highlight color.
   */
  private static DefaultHighlighter.DefaultHighlightPainter _highlightPainter = 
      new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);
  
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
      _doc().setCurrentLocation(getCaretPosition());
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
    int from = _doc()._reduced.balanceBackward();
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
      int pos = getCaretPosition();
      _doc().setCurrentLocation(pos);
      int selStart = getSelectionStart();
      int selEnd = getSelectionEnd();
      if (selStart == selEnd) {
        _doc().indentLine();
        int caretPos = getCaretPosition();
        _doc().setCurrentLocation(caretPos);
        int space = _doc().getWhiteSpace();
        _doc().move(space);
        setCaretPosition(caretPos + space);
      } 
      else 
        _doc().indentBlock(selStart, selEnd);
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
    private String _key;
    /**
     * The default action to take when the specified key is pressed.
     */
    private Action _defaultAction;    
    /**
     * Constructor.
     */
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
      _doc().indentLine();
    }
  }
  
  /**
   * Special action to take care of case when tab key is pressed.
   */
  private Action _indentKeyActionTab = new IndentKeyActionTab();
  /**
   * Because the "default" action for the enter key is special, it must be grabbed
   * from the Keymap using getAction(KeyStroke), which returns the "default" actions
   * for all keys which have behavior extending beyond regular text keys.
   */
  private Action _indentKeyActionLine =
    new IndentKeyAction("\n",
                        getKeymap().getAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
  
  /**
   * Likewise, regular text keys like '{' and '}' do not have special actions
   * that are returned by getAction(KeyStroke).  To make sure these behave right,
   * we use getDefaultAction() instead.
   */
  private Action _indentKeyActionSquiggly =
    new IndentKeyAction("}", getKeymap().getDefaultAction());
  
  private Action _indentKeyActionOpenSquiggly =
    new IndentKeyAction("{", getKeymap().getDefaultAction());

  /**
   * Constructor.  Sets up all the defaults.
   * @param mf the parent window
   */
  public DefinitionsPane(MainFrame mf, GlobalModel model) {
    _mainFrame = mf;
    _model = model;
//    _resetDocument("");
    _resetUndo();
    _findReplace = new FindReplaceDialog(mf, this);
    setContentType("text/java");
    setBackground(Color.white);
    setFont(new Font("Courier", 0, 12));
    setEditable(true);
    setDocument(_model.getDefinitionsDocument());

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
    setKeymap(ourMap);

    // Add listener that checks if position in the document has changed.
    // If it has changed, check and see if we should be highlighting matching braces.
    this.addCaretListener(_matchListener);
    _mainFrame.installNewDocumentListener(_doc());
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
   * Reset undo machinery on setDocument. 
   */
  public void setDocument(Document doc) {
    //DrJava.consoleErr().println("Reset doc: " + doc);

    if (_undoAction == null) {
      _undoAction = new UndoAction();
    }
    if (_redoAction == null) {
      _redoAction = new RedoAction();
    }
    super.setDocument(doc);
    _resetUndo();
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
    return new DefinitionsEditorKit();
  }

  /**
   * Gets the pane's document with a stronger return type.
   * @return a DefinitionsDocument
   */
  DefinitionsDocument _doc() {
    return  (DefinitionsDocument)getDocument();
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

  /**
   * Opens up the find/replace dialog.
   */
  public void findReplace() {
    _findReplace.show();
  }

  /**
   * Finds some text.
   * @param fWord the word to find
   * @return true if fWord was found
   */
  public boolean findText(String fWord) {
    return  findNextText(fWord);
  }

  /**
   * Finds the next instance of said text.
   * @param fWord the word to find
   * @return true if fWord was found
   */
  public boolean findNextText(String fWord) {
    return  findNextTextHelper(fWord, true, false);
  }

  /**
   * A helper function for findNextText so testing didn't have to open dialog boxes.
   * @param fWord word to find
   * @param interactive is this an interactive run?  false for testing
   * @param confirm whether or not the user confirms to start over from the top of the file.
   * Relevant only in testing.
   * @return true if fWord was found
   */
  boolean findNextTextHelper(String fWord, boolean interactive, boolean confirm) {
    int currentPosition = getCaretPosition();
    boolean found = _findNextText(fWord, currentPosition, _doc().getLength());
    if ((!found) && (currentPosition > 0)) {
      if (interactive)
        confirm = (JOptionPane.showConfirmDialog(null, "Continue searching from start of file?", 
            "Continue search?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
      if (confirm)
        found = _findNextText(fWord, 0, currentPosition);
    }
    return  found;
  }

  /**
   * Looks for some text in a particular bound of the document.
   * @param fWord the word to find
   * @param start the starting position
   * @param end the ending position
   * @return true if fWord was found between start and end
   */
  private boolean _findNextText(String fWord, int start, int end) {
    String text = "";
    try {
      text = _doc().getText(start, end - start);
    } catch (BadLocationException WillNeverHappen) {}
    int place = text.indexOf(fWord);
    if (place == -1) {
      return  false;
    } 
    else {
      _selectWord(start + place, fWord.length());
      return  true;
    }
  }

  /** 
   * Replaces first word that matches fWord.  Invariant: word has been found.
   */
  public boolean replaceText(String fWord, String rWord) {
    // getSelectedText could return null, so fWord must call the equals
    if (fWord.equals(getSelectedText())) {
      int start = getSelectionStart();
      int length = getSelectionEnd() - start;
      try {
        _doc().remove(start, length);
        _doc().insertString(start, rWord, null);
        _selectWord(start, rWord.length());
      } catch (BadLocationException e) {}
      return  true;
    } 
    else 
      return  false;
  }

  /**
   * Replaces the current selected word with rWord as long as it matches fWord and finds the
   * next instance of fWord.
   * @param fWord word to find and be replaced
   * @param rWord word to replace with
   * @return 
   */
  public boolean replaceFindText(String fWord, String rWord) {
    return  replaceFindTextHelper(fWord, rWord, true, false);
  }

  /**
   * Helper function to aid testing.
   * @param fWord word to find
   * @param rWord word to replace
   * @param interactive false for testing, true for users
   * @param confirm only relevant if interactive is false, true if test wants to start from
   * the top when the end has been reached and no text found
   * @return true if the replace AND the find next worked
   */
  boolean replaceFindTextHelper(String fWord, String rWord, boolean interactive, 
      boolean confirm) {
    boolean good = replaceText(fWord, rWord);
    if (good)
      good = findNextTextHelper(fWord, interactive, confirm);
    return  good;
  }

  /**
   * Replace all instances of fWord with rWord
   * @param fWord word to be replaced
   * @param rWord new word to replace with
   * @return the number of times fWord was replaced with rWord
   */
  public int replaceAllText(String fWord, String rWord) {
    return  replaceAllTextHelper(fWord, rWord, true, false);
  }

  /**
   * Helper function to assist testing.
   * @param fWord word to find
   * @param rWord word to replace
   * @param interactive true if user, false if testing
   * @param confirm valid only if interactive is false; true means start searching from the
   * top if the end has been reached
   * @return number of replaces that occurred
   */
  int replaceAllTextHelper(String fWord, String rWord, boolean interactive, boolean confirm) {
    int currentPosition = getCaretPosition();
    int count = 0;
    count += _replaceAllText(fWord, rWord, _doc().getLength());
    //insert confirm
    if (currentPosition > 0) {
      if (interactive)
        confirm = (JOptionPane.showConfirmDialog(null, "Continue searching from start of file?", 
            "Continue search?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
      if (confirm) {
        setCaretPosition(0);
        count += _replaceAllText(fWord, rWord, currentPosition);
      }
    }
    // unselect
    setCaretPosition(getSelectionEnd());
    return  count;
  }

  /**
   * Replace all text within a certain block.
   * @param fWord word to find
   * @param rWord word to replace
   * @param end stopping point of replacements; starting point is cursor position
   * @return number of replacements made within the given block
   */
  private int _replaceAllText(String fWord, String rWord, int end) {
    int position = getCaretPosition();
    int count = 0;
    while (_findNextText(fWord, position, end)) {
      replaceText(fWord, rWord);
      end += rWord.length() - fWord.length();
      position = getCaretPosition();
      count++;
    }
    return  count;
  }

  /**
   * Selects a word given a starting place and length.
   * @param place start of selection
   * @param wordLength distance from start to end of selection
   */
  private void _selectWord(int place, int wordLength) {
    setCaretPosition(place);
    moveCaretPosition(place + wordLength);
    return;
  }

}
