/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import javax.swing.event.UndoableEditListener;
import javax.swing.event.UndoableEditEvent;

import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;

public class DefinitionsView extends JEditorPane
{
	/** Keep track of the name of the file currently associated
   *  with the document we're editing. If we've never saved this file
   *  then this String is "". */
	private String _currentFileName = "";
  private MainFrame _mainFrame;

  private UndoManager _undoManager;
  private UndoAction _undoAction;
  private RedoAction _redoAction;

  private UndoableEditListener _undoListener = new UndoableEditListener() {
    public void undoableEditHappened(UndoableEditEvent e) {
      //Remember the edit and update the menus
      _undoManager.addEdit(e.getEdit());
      _undoAction.updateUndoState();
      _redoAction.updateRedoState();
    }
  };

  public DefinitionsView(MainFrame mf)
  {
    _mainFrame = mf;
    _resetDocument("");
    _resetUndo();
  }

  public Action getUndoAction() { return _undoAction; }
  public Action getRedoAction() { return _redoAction; }

  /** Reset undo machinery on setDocument. */
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

  private void _resetUndo() {
    _undoManager = new UndoManager();
    getDocument().addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }

  /** Gets current file name, or "" if it was never saved. */
  public String getCurrentFileName() {
    return _currentFileName;
  }

  /** Overriding this method ensures that all new documents created in this
   *  editor pane use our editor kit (and thus our model). */
  protected EditorKit createDefaultEditorKit()
  {
    return new DefinitionsEditorKit();
  }

  /** Save the current document over the old version of the document.
   *  If the current document is unsaved, call save as. */
  public boolean save()
  {
    if (_currentFileName == "")
      return saveAs();
    else
      return _saveToFile(_currentFileName);
  }

  public boolean modifiedSinceSave()
  {
    return _doc().modifiedSinceSave();
  }

  /** Prompt the user to select a place to save the file, then save it. */
  public boolean saveAs()
  {
    JFileChooser fc = new JFileChooser();
    int rc = fc.showSaveDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        return false;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        return _saveToFile(chosen.getAbsolutePath());
      default: // impossible since rc must be one of these
        throw new RuntimeException("filechooser returned bad rc " + rc);
    }
  }

  private void _resetDocument(String path)
  {
    String titlebarName;

    if (path == "")
    {
      titlebarName = "Untitled";
    }
    else
    {
      File f = new File(path);
      titlebarName = f.getName();
    }

    _currentFileName = path;
    _doc().resetModification();
    _mainFrame.updateFileTitle(titlebarName);
  }

  /** Save the current document to the given path.
   *  Inform the user if there was a problem.
   */
  private boolean _saveToFile(String path)
  {
    try
    {
      FileWriter writer = new FileWriter(path);
      write(writer);
      writer.close(); // This flushes the buffer!
      // Update file name if the read succeeds.
      _resetDocument(path);
      return true;
    }
    catch (IOException ioe)
    {
      String msg = "There was an error saving to the file " + path + "\n\n" +
                   ioe.getMessage();
         
      // Tell the user it failed and move on.
      JOptionPane.showMessageDialog(this,
                                    "Error saving file",
                                    msg,
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  /** Create a new, empty file in this view. */
  public boolean newFile()
  {
    boolean isOK = checkAbandoningChanges();
    if (!isOK)
      return false;

    setDocument(getEditorKit().createDefaultDocument());
    _resetDocument("");
    return true;
  }

  /** Prompt the user to select a place to open a file from, then load it.
   *  Ask the user if they'd like to save previous changes (if the current
   *  document has been modified) before opening.
   */
  public boolean open()
  {
    boolean isOK = checkAbandoningChanges();
    if (!isOK) return false;

    JFileChooser fc = new JFileChooser();
    int rc = fc.showOpenDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
		  case JFileChooser.ERROR_OPTION:
        return false;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();

        try
        {
          FileReader reader = new FileReader(chosen);
          read(reader, null);
          // Update file name if the read succeeds.
          _resetDocument(chosen.getAbsolutePath());
          return true;
        }
        catch (IOException ioe)
        {
          String msg = "There was an error opening the file.\n\n" +
                       ioe.getMessage();
             
          // Tell the user it failed and move on.
          JOptionPane.showMessageDialog(this,
                                        "Error opening file",
                                        msg,
                                        JOptionPane.ERROR_MESSAGE);
          return false;
        }
      default: // impossible since rc must be one of these
        throw new RuntimeException("filechooser returned bad rc " + rc);
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
  public boolean checkAbandoningChanges()
  {
    boolean retVal = true;

    if (_doc().modifiedSinceSave())
    {
      String fname = _currentFileName;
      if (fname == "")
        fname = "untitled file";

      String text = fname + " has been modified. Would you like to " +
                    "save?";

      int rc = JOptionPane.showConfirmDialog(
                this, 
                "Would you like to save " + fname + "?",
                text,
                JOptionPane.YES_NO_CANCEL_OPTION);

      switch (rc)
      {
        case JOptionPane.YES_OPTION:
          save();
          retVal = true;
          break;
        case JOptionPane.NO_OPTION:
          retVal = true;
          break;
        case JOptionPane.CANCEL_OPTION:
          retVal = false;
          break;
      }

    }

    return retVal;
  }

  private DefinitionsDocument _doc()
  {
    return (DefinitionsDocument) getDocument();
  }


  private class UndoAction extends AbstractAction {
    private UndoAction() {
      super("Undo");
      setEnabled(false);
    }

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

    protected void updateUndoState() {
      if (_undoManager.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getUndoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }      
  }    

  private class RedoAction extends AbstractAction {
    private RedoAction() {
      super("Redo");
      setEnabled(false);
    }

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

    protected void updateRedoState() {
      if (_undoManager.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, _undoManager.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }

		public void findReplace () {
				FindReplaceDialog box = new FindReplaceDialog(_mainFrame, this);
		}

	public boolean findNextText(String fWord)
		{
			return findNextTextHelper(fWord, true, false);
		}
	
	boolean findNextTextHelper(String fWord, boolean interactive,
														 boolean confirm) {
		int currentPosition = getCaretPosition();
		boolean found = _findNextText(fWord, currentPosition, _doc().getLength());
		if ((!found) && (currentPosition > 0)) {
			if (interactive)
				confirm = (JOptionPane.showConfirmDialog(null,
																		 "Continue searching from start of file?",
																		 "Continue search?",
																		 JOptionPane.YES_NO_OPTION) ==
									 JOptionPane.YES_OPTION);
			if(confirm)
				found = _findNextText(fWord, 0, currentPosition);
		}
		return found;
	}
	
	private boolean _findNextText (String fWord, int start, int end) {
		String text = "";
		try {
			text = _doc().getText(start, end-start);
		} catch (BadLocationException WillNeverHappen){}
		
		
		int place = text.indexOf(fWord);
		if (place == -1) {
			return false;
		} else {
			_selectWord(start + place, fWord.length());
			return true;
		}
	}
	
	/** Replaces first word that matches fWord.  Invariant: word has been found.
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
			return true;
		} else 
			return false;
	}
	
	public boolean replaceFindText(String fWord, String rWord) {
		return replaceFindTextHelper(fWord, rWord, true, false);
	}				

	boolean replaceFindTextHelper(String fWord, String rWord,
																boolean interactive, boolean confirm) {
		boolean good = replaceText(fWord, rWord);
		
		if (good)
			good = findNextTextHelper(fWord, interactive, confirm);
		
		return good;
	}

	public int replaceAllText(String fWord, String rWord) {
		return replaceAllTextHelper(fWord, rWord, true, false);
	}
	
	int replaceAllTextHelper(String fWord, String rWord,
														boolean interactive, boolean confirm) {
		int currentPosition = getCaretPosition();
		int count = 0;
		
		count += _replaceAllText(fWord, rWord, _doc().getLength());
		//insert confirm
		if (currentPosition > 0)
			{
				if (interactive)
				confirm = (JOptionPane.showConfirmDialog(null,
																		 "Continue searching from start of file?",
																		 "Continue search?",
																		 JOptionPane.YES_NO_OPTION) ==
									 JOptionPane.YES_OPTION);
				if (confirm) {
					setCaretPosition(0);
					count += _replaceAllText(fWord, rWord, currentPosition);
				}
				
			}
		// unselect
		setCaretPosition(getSelectionEnd());
		return count;
	}

	private int _replaceAllText(String fWord, String rWord, int end) {
		int position = getCaretPosition();
		int count = 0;
		while (_findNextText(fWord, position, end)) {
			replaceText(fWord, rWord);
			end += rWord.length() - fWord.length();
			position = getCaretPosition();
			count++;
		}
		
		return count;
	}
	
	private void _selectWord(int place, int wordLength) {
		setCaretPosition(place);
		moveCaretPosition(place + wordLength);
		return;
	}
	
}
