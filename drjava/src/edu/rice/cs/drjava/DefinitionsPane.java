package  edu.rice.cs.drjava;

import  javax.swing.Action;
import  javax.swing.AbstractAction;
import  javax.swing.JFileChooser;
import  javax.swing.JOptionPane;
import  javax.swing.JTextArea;
import  javax.swing.JEditorPane;
import  javax.swing.KeyStroke;
import  javax.swing.SwingUtilities;

import  javax.swing.undo.CannotRedoException;
import  javax.swing.undo.CannotUndoException;
import  javax.swing.undo.UndoManager;

import  javax.swing.event.UndoableEditListener;
import  javax.swing.event.UndoableEditEvent;
import  javax.swing.event.DocumentListener;
import  javax.swing.event.DocumentEvent;
import  javax.swing.event.CaretListener;
import  javax.swing.event.CaretEvent;

import  javax.swing.text.Document;
import  javax.swing.text.Keymap;
import  javax.swing.text.StyledEditorKit;
import  javax.swing.text.DefaultEditorKit;
import  javax.swing.text.EditorKit;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.Highlighter;
import  javax.swing.text.DefaultHighlighter;

import  java.io.File;
import  java.io.IOException;
import  java.io.FileReader;
import  java.io.FileWriter;

import  java.awt.Rectangle;
import  java.awt.Color;
import  java.awt.Font;
import  java.awt.Toolkit;
import  java.awt.event.ActionEvent;
import  java.awt.event.MouseListener;
import  java.awt.event.MouseEvent;
import  java.awt.event.KeyListener;
import  java.awt.event.KeyEvent;
import  java.awt.event.ActionListener;
import  java.awt.event.*;


/**
 * The pane in which work on the current document occurs.
 * @version $Id$
 */
public class DefinitionsPane extends JEditorPane {
  /**
   * Keep track of the file name associated with the current document.
   * For a new file that we haven't saved yet, the value is "".
   */
  private String _currentFileName = "";
  /**
   * Our parent window.
   */
  private MainFrame _mainFrame;
  private UndoManager _undoManager;
  private UndoAction _undoAction;
  private RedoAction _redoAction;
  /** 
   * For find and replace. 
   * We have a persistent dialog so it keeps track of our last find criterion.
   */
  private FindReplaceDialog _findReplace;
  /** 
   * For opening files.
   * We have a persistent dialog to keep track of the last directory
   * from which we opened.
   */  
  private JFileChooser _openChooser;
  /** 
   * For saving files.
   * We have a persistent dialog to keep track of the last directory
   * from which we saved.
   */  
  private JFileChooser _saveChooser;
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
   * Looks for changes in the caret position to see if a paren/brace/bracket highlight
   * is needed.
  */
  private CaretListener _matchListener = new CaretListener() {

    /**
     * Checks caret position to see if it needs to set or remove a highlight from the
     * document.  When the cursor is immediately right of ')', '}', or ']', it highlights
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
     * The key string ("\n"|"{"|"}") for the key pressed that invokes this instance.
     * Not used currently, but there for readability and possible future use, e.g.,
     * debugging add-ons or the rewrite of the indention code.
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
  public DefinitionsPane(MainFrame mf) {
    _mainFrame = mf;
    _resetDocument("");
    _resetUndo();
    _findReplace = new FindReplaceDialog(mf, this);
    setContentType("text/java");
    setBackground(Color.white);
    setFont(new Font("Courier", 0, 12));
    setEditable(true);
    _openChooser = new JFileChooser(System.getProperty("user.dir"));
    _openChooser.setFileFilter(new JavaSourceFilter());
    _saveChooser = new JFileChooser(System.getProperty("user.dir"));

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

  /** Gets current file name, or "" if it was never saved. */
  public String getCurrentFileName() {
    return  _currentFileName;
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
    return  new DefinitionsEditorKit();
  }

  /**
   * Ask the user what line they'd like to jump to, then go there.
   */
  public void gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Jump to line";
    String lineStr = JOptionPane.showInputDialog(this, 
                                                 msg, 
                                                 title, 
                                                 JOptionPane.QUESTION_MESSAGE);
    try {
      int lineNum = Integer.parseInt(lineStr);
      // Move the defs document to the right spot
      _doc().gotoLine(lineNum);
      // Now move the caret to the same place
      int pos = _doc().getCurrentLocation();
      setCaretPosition(pos);
      // Finally, scroll the window to make this line visible.
      Rectangle rect = modelToView(pos);
      scrollRectToVisible(rect);
      // And make sure the defs view has focus!
      grabFocus();
    } catch (BadLocationException impossible) {
      // we got the location from defs doc. it is valid, i swear.
    } catch (NumberFormatException nfe) {       // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
  }

  /** 
   * Save the current document over the old version of the document.
   * If the current document is unsaved, call save as. 
   */
  public boolean save() {
    if (_currentFileName == "")
      return  saveAs(); 
    else 
      return  saveToFile(_currentFileName);
  }

  /**
   * @return true if the document was modified since the last save
   */
  public boolean modifiedSinceSave() {
    return  _doc().modifiedSinceSave();
  }

  /** 
   * Prompt the user to select a place to save the file, then save it. 
   */
  public boolean saveAs() {
    JFileChooser fc = _saveChooser;
    fc.setSelectedFile(null);
    int rc = fc.showSaveDialog(this);
    switch (rc) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        return  false;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        if (chosen != null)
          return  saveToFile(chosen.getAbsolutePath()); 
        else 
          return  false;
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + rc);
    }
  }

  /**
   * Reset the document.
   * Change the title of the file in the mainframe and the menu bar, open up a file.
   * @param path the path of the file being opened.
   */
  private void _resetDocument(String path) {
    String titlebarName;
    if (path == "") {
      titlebarName = "Untitled";
    } 
    else {
      File f = new File(path);
      titlebarName = f.getName();
    }
    _currentFileName = path;
    _doc().resetModification();
    _mainFrame.updateFileTitle(titlebarName);
    _mainFrame.installNewDocumentListener(_doc());
    // On all open/new operations reset focus to this
    // But do it in the Swing thread to be safe.
    SwingUtilities.invokeLater(new Runnable() {
      /**
       * Reset the focus to the DefinitionsPane.
       */
      public void run() {
        DefinitionsPane.this.requestFocus();
      }
    });
  }

  /** Save the current document to the given path.
   *  Inform the user if there was a problem.
   */
  boolean saveToFile(String path) {
    try {
      FileWriter writer = new FileWriter(path);
      write(writer);
      writer.close();           // This flushes the buffer!
      // Update file name if the write succeeds.
      _resetDocument(path);
      return  true;
    } catch (IOException ioe) {
      String msg = "There was an error saving to the file " + path + "\n\n" + ioe.getMessage();
      // Tell the user it failed and move on.
      JOptionPane.showMessageDialog(this, "Error saving file", msg, JOptionPane.ERROR_MESSAGE);
      return  false;
    }
  }

  /** Create a new, empty file in this view. */
  public boolean newFile() {
    boolean isOK = checkAbandoningChanges();
    if (!isOK)
      return  false;
    setDocument(getEditorKit().createDefaultDocument());
    _resetDocument("");
    return  true;
  }

  /** Prompt the user to select a place to open a file from, then load it.
   *  Ask the user if they'd like to save previous changes (if the current
   *  document has been modified) before opening.
   */
  public boolean open() {
    boolean isOK = checkAbandoningChanges();
    if (!isOK)
      return  false;
    JFileChooser fc = _openChooser;
    fc.setSelectedFile(null);
    int rc = fc.showOpenDialog(this);
    switch (rc) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        return  false;
      case JFileChooser.APPROVE_OPTION:
        try {
          _mainFrame.hourglassOn();
          File chosen = fc.getSelectedFile();
          if (chosen == null) {
            return  false;
          }
          FileReader reader = new FileReader(chosen);
          read(reader, null);
          // Update file name if the read succeeds.
          _resetDocument(chosen.getAbsolutePath());
          return  true;
        } catch (IOException ioe) {
          String msg = "There was an error opening the file.\n\n" + ioe.getMessage();
          // Tell the user it failed and move on.
          JOptionPane.showMessageDialog(this, "Error opening file", msg, JOptionPane.ERROR_MESSAGE);
          return  false;
        } finally {
          // Make sure we always turn off the hourglass!
          _mainFrame.hourglassOff();
        }
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + rc);
    }
  }

  /**
   * Check if the current document has been modified. If it has, ask the user
   * if he would like to save or not, and save the document if yes. Also
   * give the user a "cancel" option to cancel doing the operation that got
   * us here in the first place.
   *
   * @return A boolean, if true means the user is OK with the file being saved
   *         or not as they chose. If false, the user wishes to cancel.
   */
  public boolean checkAbandoningChanges() {
    boolean retVal = true;
     if (_doc().modifiedSinceSave()) {
       String fname = _currentFileName;
       if (fname == "")
        fname = "untitled file";
      String text = fname + " has been modified. Would you like to " + "save?";
      int rc = JOptionPane.showConfirmDialog(this, "Would you like to save " + fname
          + "?", text, JOptionPane.YES_NO_CANCEL_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          retVal = save();
          //retVal = true;
          break;
        case JOptionPane.NO_OPTION:
          retVal = true;
          break;
        case JOptionPane.CANCEL_OPTION:
          retVal = false;
          break;
      }
    }
    return  retVal;
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



