package edu.rice.cs.drjava;

import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;

import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.StyleConstants;

import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * The panel which houses the list of errors after an unsuccessful compilation.
 * If the user clicks on the combobox, move the definitions cursor to the error in
 * the source.
 * If the cursor is moved onto a line with an error, select the appropriate error 
 * in the list but do not move the cursor.
 * @version $Id$
 */
public class CompilerErrorPanel extends JPanel {
  /** Highlight painter for selected errors in the defs doc. */
  private static final DefaultHighlighter.DefaultHighlightPainter
    _documentHighlightPainter
      = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  /** Highlight painter for selected list items. */
  private static final DefaultHighlighter.DefaultHighlightPainter
    _listHighlightPainter
      = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  private static final SimpleAttributeSet NORMAL_ATTRIBUTES = _getNormalAttributes();
  private static final SimpleAttributeSet BOLD_ATTRIBUTES = _getBoldAttributes();

  private static final SimpleAttributeSet _getBoldAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    StyleConstants.setBold(s, true);
    return s;
  }

  private static final SimpleAttributeSet _getNormalAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    return s;
  }

  public void setListFont(Font f) {
    StyleConstants.setFontFamily(NORMAL_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(NORMAL_ATTRIBUTES, f.getSize());

    StyleConstants.setFontFamily(BOLD_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(BOLD_ATTRIBUTES, f.getSize());
  }

  // when we create a highlight we get back a tag we can use to remove it
  private Object _previousHighlightTag = null;

  /**
   * Errors with source info have an entry in both _errors and _errorPositions.
   */
  private CompilerError[] _errors;
  private Position[] _errorPositions;

  /**
   * Here's where we put errors that have no source information.
   */
  private CompilerError[] _errorsWithoutPositions;

  private DefinitionsPane _definitionsPane;

  private JButton _showAllButton;
  private JButton _nextButton;
  private JButton _previousButton;
  private ErrorListPane _errorListPane;

  /**
   * A caret listener to watch the Defs Doc and highlight the error on the
   * current line.
   */
  private CaretListener _listener = new CaretListener() {
    public void caretUpdate(CaretEvent evt) {
      if (_errorPositions.length == 0) {
        return;
      }

      // Now we can assume at least one error.

      int curPos = evt.getDot();
      // check if the dot is on a line with an error.
      // Find the first error that is on or after the dot. If this comes
      // before the newline after the dot, it's on the same line.
      int errorAfter; // index of the first error after the dot
      for (errorAfter = 0; errorAfter < _errorPositions.length; errorAfter++) {
        if (_errorPositions[errorAfter].getOffset() >= curPos) {
          break;
        }
      }

      // index of the first error before the dot
      int errorBefore = errorAfter - 1;

      // this will be set to what we want to select, or -1 if nothing
      int shouldSelect = -1;

      if (errorBefore >= 0) { // there's an error before the dot
        int errPos = _errorPositions[errorBefore].getOffset();
        try {
          String betweenDotAndErr =
            _definitionsPane.getDocument().getText(errPos, curPos - errPos);

          if (betweenDotAndErr.indexOf('\n') == -1) {
            shouldSelect = errorBefore;
          }
        } 
        catch (BadLocationException willNeverHappen) {}
      }

      if ((shouldSelect == -1) && (errorAfter != _errorPositions.length)) {
        // we found an error on/after the dot
        // if there's a newline between dot and error,
        // then it's not on this line
        int errPos = _errorPositions[errorAfter].getOffset();
        try {
          String betweenDotAndErr =
            _definitionsPane.getDocument().getText(curPos, errPos - curPos);

          if (betweenDotAndErr.indexOf('\n') == -1) {
            shouldSelect = errorAfter;
          }
        } 
        catch (BadLocationException willNeverHappen) {}
      }

      // if no error is on this line, select the (none) item
      if (shouldSelect == -1) {
        _errorListPane.selectNothing();
      }
      else {
        // No need to move the caret since it's already here!
        _highlightErrorInSource(shouldSelect);

        // Select item wants the error number overall, including
        // accounting for errors with no source location
        _errorListPane.selectItem(shouldSelect +_errorsWithoutPositions.length);
      }
    }
  };

  /**
   * Adds an error highlight to the document.
   * @exception BadLocationException
   */
  private void _addHighlight(int from, int to) throws BadLocationException {
    _previousHighlightTag =
      _definitionsPane.getHighlighter().addHighlight(from,
                                                     to,
                                                     _documentHighlightPainter);
  }

  /**
   * Removes the previous error highlight from the document after the cursor
   * has moved.
   */
  private void _removePreviousHighlight() {
    if (_previousHighlightTag != null) {
      _definitionsPane.getHighlighter().removeHighlight(_previousHighlightTag);
      _previousHighlightTag = null;
    }
  }

  /**
   * Constructor.
   * @param defPane the definitions pane which holds the source to be compiled
   */
  public CompilerErrorPanel(DefinitionsPane defPane) {
    setLayout(new BorderLayout());
        
    _definitionsPane = defPane;
    _definitionsPane.addCaretListener(_listener);

    _showAllButton = new JButton("Show all");
    _showAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _showAllErrors();
        }
    });

    _nextButton = new JButton("Next");
    _nextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _switchToError(_errorListPane.getSelectedIndex() + 1);
        }
    });

    _previousButton = new JButton("Previous");
    _previousButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _switchToError(_errorListPane.getSelectedIndex() - 1);
        }
    });
    
    _errorListPane = new ErrorListPane();

    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the 
    // text. Very bad.
    JScrollPane scroller =
      new JScrollPane(_errorListPane,
                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    resetErrors(new CompilerError[0]);

    // Disable buttons. They don't totally work and who needs em.
    /*
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    buttonPanel.add(_previousButton);
    buttonPanel.add(_nextButton);
    // Show all not yet implemented.
    // buttonPanel.add(_showAllButton);
    add(buttonPanel, BorderLayout.EAST);
    */

    add(scroller, BorderLayout.CENTER);
  }

  /**
   * Change all state to select a new error.
   * @param errorNum Error number, which is either in _errorsWithoutPositions
   * (if errorNum < _errorsWithoutPositions.length) or in _errors (otherwise).
   * If it's in _errors, we need to subtract _errorsWithoutPositions.length
   * to get the index into the array.
   */
  private void _switchToError(final int errorNum) {
    // errorNum is an error number. Because errors without source info 
    // come first, check and see if this error is without source info, and
    // if so don't try to highlight source info!
    boolean errorHasLocation = (errorNum >= _errorsWithoutPositions.length);

    if (errorHasLocation) {
      // Index into _errors array
      int idx = errorNum - _errorsWithoutPositions.length;

      _highlightErrorInSource(idx);
      _gotoErrorSourceLocation(idx);
    }
    else {
      // Get rid of old highlight, since the error we have no has
      // no source location
      _removePreviousHighlight();
    }

    // Select item wants the error number, which what we were passed
    _errorListPane.selectItem(errorNum);
  }

  /**
   * Jumps to error location in source
   * @param idx Index into _errors array
   */
  private void _gotoErrorSourceLocation(final int idx) {
    if (idx < 0) return;

    _highlightErrorInSource(idx);

    int errPos = _errorPositions[idx].getOffset();
    // move caret to that position
    _definitionsPane.setCaretPosition(errPos);
    _definitionsPane.grabFocus();
  }

  /**
   * Highlights the given error in the source.
   * @param newIndex Index into _errors array
   */
  private void _highlightErrorInSource(int newIndex) {
    int errPos = _errorPositions[newIndex].getOffset();

    try {
      Document doc = _definitionsPane.getDocument();
      String text = doc.getText(0, doc.getLength());
     
      // Look for the previous newline BEFORE this character. Thus start looking
      // on the character one before this character. If this is not the case,
      // if the error is at a newline character, both prev and next newlines
      // will be set to that place, resulting in nothing being highlighted.
      int prevNewline = text.lastIndexOf('\n', errPos - 1);
      if (prevNewline == -1) {
        prevNewline = 0;
      }
      
      int nextNewline = text.indexOf('\n', errPos);
      if (nextNewline == -1) {
        nextNewline = doc.getLength();
      }

      _removePreviousHighlight();
      _addHighlight(prevNewline, nextNewline);
    }
    catch (BadLocationException imposssible) { }

    _resetEnabledStatus();
  }

  private void _showAllErrors() {
  }

  /**
   * Reset the enabled status of the "next", "previous", and "show all" 
   * buttons in the compiler error panel.
   */
  private void _resetEnabledStatus() {
    int numErrors = _errorsWithoutPositions.length + _errors.length;

    _nextButton.setEnabled(_errorListPane.getSelectedIndex() < numErrors);
    _previousButton.setEnabled(_errorListPane.getSelectedIndex() > 0);
    _showAllButton.setEnabled(numErrors != 0);
  }

  /** Called when compilation begins. */
  public void setCompilationInProgress() {
    _errorListPane.setCompilationInProgress();
  }

  /**
   * Reset the errors to the current error information.
   * @param errors the current error information
   */
  public void resetErrors(CompilerError[] errors) {
    for (int i = 0; i < errors.length; i++) {
      DrJava.consoleErr().println("#" + i + ": " + errors[i]);
    }

    // Get rid of any old highlights
    _removePreviousHighlight();

    // sort the errors by location
    Arrays.sort(errors);

    // Filter out those with invalid source info.
    // They will be first since errors are sorted by line number,
    // and invalid source info is for negative line numbers.
    int numInvalid = 0;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i].lineNumber() < 0) {
        numInvalid++;
      }
      else {
        // Since they were sorted, we must be done looking 
        // for invalid source coordinates, since we found this valid one.
        break;
      }
    }

    _errorsWithoutPositions = new CompilerError[numInvalid];
    System.arraycopy(errors,
                     0,
                     _errorsWithoutPositions,
                     0,
                     numInvalid);

    int numValid = errors.length - numInvalid;
    _errors = new CompilerError[numValid];
    System.arraycopy(errors,
                     numInvalid,
                     _errors,
                     0,
                     numValid);

    _createPositionsArray();
    _resetEnabledStatus();

    _errorListPane.updateListPane();
  }

  /**
   * Create array of positions where each error occurred.
   */
  private void _createPositionsArray() {
    _errorPositions = new Position[_errors.length];

    // don't bother with anything else if there are no errors
    if (_errorPositions.length == 0) 
      return;

    Document defsDoc = _definitionsPane.getDocument();
    try {
      String defsText = defsDoc.getText(0, defsDoc.getLength());

      int curLine = 0;
      int offset = 0; // offset is number of chars from beginning of file
      int numProcessed = 0;

      // offset is always pointing to the first character in a line
      // at the top of the loop
      while ((numProcessed < _errors.length) &&
             (offset < defsText.length()))
      {
        // first figure out if we need to create any new positions on this line
        for (int i = numProcessed;
             (i < _errors.length) && (_errors[i].lineNumber() == curLine);
             i++)
        {
          _errorPositions[i] = defsDoc.createPosition(offset +
                                                      _errors[i].startColumn());
          numProcessed++;
        }

        int nextNewline = defsText.indexOf('\n', offset);
        if (nextNewline == -1) {
          break;
        }
        else {
          curLine++;
          offset = nextNewline + 1;
        }
      }
    }
    catch (BadLocationException willNeverEverEverHappen) {
    }
  }

  /**
   * A pane to show compiler errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  private class ErrorListPane extends JEditorPane {
    private int _selectedIndex;

    /**
     * The start position of each error in the list. This position is the place
     * where the error starts in the error list, as opposed to the place where
     * the error exists in the source.
     */
    private Position[] _errorListPositions;

    // when we create a highlight we get back a tag we can use to remove it
    private Object _listHighlightTag = null;

    // on mouse click, highlight the error in the list and also in the source
    private MouseAdapter _mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        int errorNum = _errorAtPoint(e.getPoint());

        if (errorNum == -1) {
          selectNothing();
        }
        else {
          _switchToError(errorNum);
        }
      }
    };

    public ErrorListPane() {
      // If we set this pane to be of type text/rtf, it wraps based on words
      // as opposed to based on characters.
      super("text/rtf", "");
      addMouseListener(_mouseListener);

      ErrorListPane.this.setFont(new Font("Courier", 0, 20));

      // We set the editor pane disabled so it won't get keyboard focus,
      // which makes it uneditable, and so you can't select text inside it.
      setEnabled(false);
    }

    /*
    public void setFont(Font f) {
      super.setFont(f);
      System.err.println(new java.util.Date() + ": Set font: " + f);
    }
    */

    /**
     * Returns error number associated with the given visual coordinates.
     * Returns -1 if none.
     */
    private int _errorAtPoint(Point p) {
      int modelPos = viewToModel(p);

      if (modelPos == -1)
        return -1;

      // Find the first error whose position preceeds this model position
      int errorNum = -1;
      for (int i = 0; i < _errorListPositions.length; i++) {
        if (_errorListPositions[i].getOffset() <= modelPos) {
          errorNum = i;
        }
        else { // we've gone past the correct error; the last value was right
          break;
        }
      }

      return errorNum;
    }

    /** 
     * Update the pane which holds the list of errors for the viewer.
     */
    public void updateListPane() {
      try {
        int numErrors = _errorsWithoutPositions.length + _errors.length;
        _errorListPositions = new Position[numErrors];

        if ((_errors.length == 0) && (_errorsWithoutPositions.length == 0)) {
          _updateNoErrors();
        }
        else {
          _updateWithErrors();
        }
      }
      catch (BadLocationException e) {
        throw new UnexpectedException(e);
      }

      // Force UI to redraw
      revalidate();
    }

    /** Puts the error pane into "compilation in progress" state. */
    public void setCompilationInProgress() {
      _errorListPositions = new Position[0];

      DefaultStyledDocument doc = new DefaultStyledDocument();

      try {
        doc.insertString(0,
                       "Compilation in progress, please wait ...",
                       NORMAL_ATTRIBUTES);
      }
      catch (BadLocationException ble) {
        throw new UnexpectedException(ble);
      }

      setDocument(doc);

      selectNothing();
    }

    /**
     * Used to show that the last compile was successful.
     */
    private void _updateNoErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      doc.insertString(0,
                       "Last compilation completed successfully.",
                       NORMAL_ATTRIBUTES);
      setDocument(doc);

      selectNothing();
    }

    /**
     * Used to show that the last compile was unsuccessful.
     */
    private void _updateWithErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();

      // Show errors without source locations
      int errorNum = 0;
      for (int i = 0; i < _errorsWithoutPositions.length; i++, errorNum++) {
        int startPos = doc.getLength();
        _insertErrorText(_errorsWithoutPositions, i, doc);

        // Note to user that there is no source info for this error
        doc.insertString(doc.getLength(),
                         " (no source location)",
                         NORMAL_ATTRIBUTES);
        doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);

        _errorListPositions[errorNum] = doc.createPosition(startPos);
      }

      // Show errors with source locations
      for (int i = 0; i < _errors.length; i++, errorNum++) {
        int startPos = doc.getLength();
        _insertErrorText(_errors, i, doc);
        doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);
        _errorListPositions[errorNum] = doc.createPosition(startPos);
      }

      setDocument(doc);

      // Select the first error
      _switchToError(0);
    }

    /**
     * Puts an error message into the array of errors at the specified index.
     * @param array the array of errors
     * @param i the index at which the message will be inserted
     * @param doc the document in the error pane
     */
    private void _insertErrorText(CompilerError[] array, int i, Document doc)
      throws BadLocationException
    {
      CompilerError error = array[i];

      if (error.isWarning()) {
        doc.insertString(doc.getLength(), "Warning: ", BOLD_ATTRIBUTES);
      }
      else {
        doc.insertString(doc.getLength(), "Error: ", BOLD_ATTRIBUTES);
      }

      doc.insertString(doc.getLength(), error.message(), NORMAL_ATTRIBUTES);
    }

    /**
     * When the selection of the current error changes, remove
     * the highlight in the error pane.
     */
    private void _removeListHighlight() {
      if (_listHighlightTag != null) {
        getHighlighter().removeHighlight(_listHighlightTag);
        _listHighlightTag = null;
      }
    }

    /**
     * Don't select any errors in the error pane.
     */
    public void selectNothing() {
      _selectedIndex = -1;
      _removeListHighlight();
      _removePreviousHighlight();
    }

    /**
     * Selects the given error inside the error list pane. 
     */
    public void selectItem(int i) {
      _selectedIndex = i;
      _removeListHighlight();

      int startPos = _errorListPositions[i].getOffset();

      // end pos is either the end of the document (if this is the last error)
      // or the char where the next error starts
      int endPos;
      if (i + 1 == (_errorsWithoutPositions.length + _errors.length)) {
        endPos = getDocument().getLength();
      }
      else {
        endPos = _errorListPositions[i + 1].getOffset();
      }

      try {
        _listHighlightTag =
          getHighlighter().addHighlight(startPos,
                                        endPos,
                                        _listHighlightPainter);

        // Scroll to make sure this item is visible
        Rectangle startRect = modelToView(startPos);
        Rectangle endRect = modelToView(endPos - 1);

        // Add the end rect onto the start rect to make a rectangle
        // that encompasses the entire error
        startRect.add(endRect);

        //System.err.println("scrll vis: " + startRect);

        scrollRectToVisible(startRect);

      } 
      catch (BadLocationException badBadLocation) {}
    }

    /**
     * Get the index of the current error in the error array.
     */
    public int getSelectedIndex() { return _selectedIndex; }
  }
}
