/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import javax.swing.event.UndoableEditListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;

import javax.swing.text.Document;
import javax.swing.text.Keymap;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import java.beans.PropertyChangeListener;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;

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
	private FindReplaceDialog _findReplace;
	private JFileChooser _openChooser;
	private JFileChooser _saveChooser;
	private Object _matchHighlight = null;
	private static DefaultHighlighter.DefaultHighlightPainter _highlightPainter
  = new DefaultHighlighter.DefaultHighlightPainter(Color.lightGray);

	private CaretListener _matchListener = new CaretListener() {
		public void caretUpdate(CaretEvent e) {
			_doc().setCurrentLocation(getCaretPosition());
			_removePreviousHighlight();

			try {
				_updateMatchHighlight();
			}
			catch (BadLocationException ex) {}
		}
	};

	private void _updateMatchHighlight() throws BadLocationException {
		int to = getCaretPosition();
		int from = _doc()._reduced.balanceBackward();
		if (from == -1) {
		}
		else {		
			from = to - from;
			_addHighlight(from, to);
			Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
		}
	}	

	private void _addHighlight(int from, int to) throws
		BadLocationException {
		_matchHighlight = 		
			getHighlighter().addHighlight(from,
																		to,
																		_highlightPainter);
	}
	
	private void _removePreviousHighlight() {
		if (_matchHighlight != null) {
			getHighlighter().removeHighlight(_matchHighlight);
			_matchHighlight = null;
		}
	}
	
  private UndoableEditListener _undoListener = new UndoableEditListener() {
    public void undoableEditHappened(UndoableEditEvent e) {
      //Remember the edit and update the menus
      _undoManager.addEdit(e.getEdit());
      _undoAction.updateUndoState();
      _redoAction.updateRedoState();
    }
  };
	
	private class IndentKeyActionTab extends AbstractAction {
		/** Handle the key typed event from the text field. */
		public void actionPerformed(ActionEvent e) {
			int pos = getCaretPosition();
			_doc().setCurrentLocation(pos);

			int selStart = getSelectionStart();
			int selEnd = getSelectionEnd();
			
			if(selStart == selEnd){
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

	private class IndentKeyActionSquiggly extends AbstractAction {
		/** Handle the key typed event from the text field. */
		public void actionPerformed(ActionEvent e) {
			int pos = getCaretPosition();
			_doc().setCurrentLocation(pos);
			try{
				_doc().insertString(pos, "}", null);
			}
			catch(BadLocationException be){throw new IllegalArgumentException
																			 (be.toString());}

			_doc().indentLine();
		}
	}
	
	private class IndentKeyActionOpenSquiggly extends AbstractAction {
		/** Handle the key typed event from the text field. */
		public void actionPerformed(ActionEvent e) {
			int pos = getCaretPosition();
			_doc().setCurrentLocation(pos);
			try{
				_doc().insertString(pos, "{", null);
			}
			catch(BadLocationException be){throw new IllegalArgumentException
																			 (be.toString());}

			_doc().indentLine();
		}
	}
	
	private class IndentKeyActionLine extends AbstractAction {
		/** Handle the key typed event from the text field. */
		public void actionPerformed(ActionEvent e) {
			int pos = getCaretPosition();
			_doc().setCurrentLocation(pos);
			try{
				_doc().insertString(pos, "\n", null);
			}
			catch(BadLocationException be){throw new IllegalArgumentException
																			 (be.toString());}
			
			_doc().indentLine();
		}
	}
	
	
	private Action _indentKeyActionTab = new IndentKeyActionTab();
	private Action _indentKeyActionLine = new IndentKeyActionLine();
	private Action _indentKeyActionSquiggly = new IndentKeyActionSquiggly();
	private Action _indentKeyActionOpenSquiggly = new IndentKeyActionOpenSquiggly();
	
// Constructor
	
	public DefinitionsView(MainFrame mf)
  {
    _mainFrame = mf;
    _resetDocument("");
    _resetUndo();
		_findReplace = new FindReplaceDialog(mf, this);
			
    setContentType("text/java");
    setBackground(Color.white);
    setFont(new Font("Courier", 0, 12));
    setEditable(true);

		//????KEEP??????
		_openChooser = new JFileChooser(System.getProperty("user.dir"));
		_openChooser.setFileFilter(new JavaSourceFilter());
		_saveChooser = new JFileChooser(System.getProperty("user.dir"));
				

		Keymap ourMap = addKeymap("INDENT_KEYMAP", getKeymap());
		
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
																 (Action) _indentKeyActionLine);
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
																 (Action) _indentKeyActionTab);
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('}'),
																 (Action) _indentKeyActionSquiggly);
		ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('{'),
																 (Action) _indentKeyActionOpenSquiggly);


		setKeymap(ourMap);
		
		this.addCaretListener(_matchListener);
		_mainFrame.installNewDocumentListener(_doc());
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
      return saveToFile(_currentFileName);
  }

  public boolean modifiedSinceSave()
  {
    return _doc().modifiedSinceSave();
  }

  /** Prompt the user to select a place to save the file, then save it. */
  public boolean saveAs()
  {
    JFileChooser fc = _saveChooser;
		fc.setSelectedFile(null);
    int rc = fc.showSaveDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        return false;
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
				if (chosen != null)
					return saveToFile(chosen.getAbsolutePath());
				else return false;
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
		_mainFrame.installNewDocumentListener(_doc());

    // On all open/new operations reset focus to this
    // But do it in the Swing thread to be safe.
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          DefinitionsView.this.requestFocus();
        }
    });
  }

  /** Save the current document to the given path.
   *  Inform the user if there was a problem.
   */
  boolean saveToFile(String path)
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

    JFileChooser fc = _openChooser;
		fc.setSelectedFile(null);
    int rc = fc.showOpenDialog(this);

    switch(rc)
    {
      case JFileChooser.CANCEL_OPTION:
		  case JFileChooser.ERROR_OPTION:
        return false;
      case JFileChooser.APPROVE_OPTION:
				_mainFrame.hourglassOn();
        File chosen = fc.getSelectedFile();
				if (chosen == null)
					return false;
        try
        {
          FileReader reader = new FileReader(chosen);
          read(reader, null);
          // Update file name if the read succeeds.
          _resetDocument(chosen.getAbsolutePath());
					_mainFrame.hourglassOff();
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
					_mainFrame.hourglassOff();
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

    return retVal;
  }

 DefinitionsDocument _doc()
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
			_findReplace.show();
		}

		public boolean findText (String fWord) {
				return findNextText(fWord);
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
