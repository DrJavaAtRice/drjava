/* $Id$ */

/* If click on combobox, move cursor to error location.
 * If move cursor onto line with error, select error in combobox but do
 * not move the cursor.
 */

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
import javax.swing.text.DefaultHighlighter;

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

public class CompilerErrorPanel extends JPanel {
  private static final String NEWLINE = System.getProperty("line.separator");

  /** Highlight painter for selected errors in the defs doc. */
  private static final DefaultHighlighter.DefaultHighlightPainter
    _documentHighlightPainter
      = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  /** Highlight painter for selected list items. */
  private static final DefaultHighlighter.DefaultHighlightPainter
    _listHighlightPainter
      = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

  // when we create a highlight we get back a tag we can use to remove it
  private Object _previousHighlightTag = null;

  private CompilerError[] _errors;
  private Position[] _errorPositions;

  private DefinitionsView _definitionsView;

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
            _definitionsView.getDocument().getText(errPos, curPos - errPos);

          if (betweenDotAndErr.indexOf(NEWLINE) == -1) {
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
            _definitionsView.getDocument().getText(curPos, errPos - curPos);

          if (betweenDotAndErr.indexOf(NEWLINE) == -1) {
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
        _selectError(shouldSelect);
      }
    }
  };

  private void _addHighlight(int from, int to) throws BadLocationException {
    _previousHighlightTag =
      _definitionsView.getHighlighter().addHighlight(from,
                                                     to,
                                                     _documentHighlightPainter);
  }

  private void _removePreviousHighlight() {
    if (_previousHighlightTag != null) {
      _definitionsView.getHighlighter().removeHighlight(_previousHighlightTag);
      _previousHighlightTag = null;
    }
  }

  public CompilerErrorPanel(DefinitionsView view) {
    setLayout(new BorderLayout());
        
    _definitionsView = view;
    _definitionsView.addCaretListener(_listener);

    _showAllButton = new JButton("Show all");
    _showAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _showAllErrors();
        }
    });

    _nextButton = new JButton("Next");
    _nextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _gotoError(_errorListPane.getSelectedIndex() + 1);
        }
    });

    _previousButton = new JButton("Previous");
    _previousButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _gotoError(_errorListPane.getSelectedIndex() - 1);
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

  private void _gotoError(int newIndex) {
    if (newIndex < 0) return;

    // move caret to that position
    final int idx = newIndex; // final so it's accessible inside inner class

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        _selectError(idx);

        int errPos = _errorPositions[idx].getOffset();
        _definitionsView.setCaretPosition(errPos);
        _definitionsView.grabFocus();
      }
    });
  }

  private void _selectError(int newIndex) {
    _errorListPane.selectItem(newIndex);

    int errPos = _errorPositions[newIndex].getOffset();

    try {
      Document doc = _definitionsView.getDocument();
      String text = doc.getText(0, doc.getLength());
     
      // Look for the previous newline BEFORE this character. Thus start looking
      // on the character one before this character. If this is not the case,
      // if the error is at a newline character, both prev and next newlines
      // will be set to that place, resulting in nothing being highlighted.
      int prevNewline = text.lastIndexOf(NEWLINE, errPos - 1);
      if (prevNewline == -1) {
        prevNewline = 0;
      }
      
      int nextNewline = text.indexOf(NEWLINE, errPos);
      if (nextNewline == -1) {
        nextNewline = doc.getLength();
      }

      _removePreviousHighlight();
      _addHighlight(prevNewline, nextNewline);
    }
    catch (BadLocationException stupidMachineItWontHappen) {}

    // now display that line with highlight
    _resetEnabledStatus();
  }

  private void _showAllErrors() {
  }

  private void _resetEnabledStatus() {
    _nextButton.setEnabled(_errorListPane.getSelectedIndex() < _errors.length);
    _previousButton.setEnabled(_errorListPane.getSelectedIndex() > 0);
    _showAllButton.setEnabled(_errors.length != 0);
  }

  public void resetErrors(CompilerError[] errors) {
    // Get rid of any old highlights
    _removePreviousHighlight();

    _errors = errors;

    // sort the errors by location
    Arrays.sort(_errors);

    _createPositionsArray();
    _resetEnabledStatus();

    _errorListPane.updateListPane();
  }

  private void _createPositionsArray() {
    // Create array of positions where each error occurred
    _errorPositions = new Position[_errors.length];

    // don't bother with anything else if there are no errors
    if (_errorPositions.length == 0) 
      return;

    Document defsDoc = _definitionsView.getDocument();
    try {
      String defsText = defsDoc.getText(0, defsDoc.getLength());

      int curLine = 0;
      int offset = 0; // offset is number of chars from beginning of file
      int numProcessed = 0;

      // offset is always pointing to the first character in a line
      // at the top of the loop
      while ((numProcessed < _errors.length) &&
          (offset < defsText.length())) {
        // first figure out if we need to create any new positions on this line
        for (int i = numProcessed;
            (i < _errors.length) && (_errors[i].lineNumber() == curLine);
            i++) {
          _errorPositions[i] = defsDoc.createPosition(offset +
              _errors[i].startColumn());
          numProcessed++;
        }

        int nextNewline = defsText.indexOf(NEWLINE, offset);
        if (nextNewline == -1) {
          break;
        }
        else {
          curLine++;
          offset = nextNewline + 1;
        }
      }
    }
    catch (BadLocationException willNeverEverEverHappen) {}
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
          // This jumps the caret in the defs doc to the error and
          // also highlights the error in the defs. The caret movement
          // then results in the error being highlighted in the list.
          _gotoError(errorNum);
        }
      }
    };

    public ErrorListPane() {
      // If we set this pane to be of type text/rtf, it wraps based on words
      // as opposed to based on characters.
      super("text/rtf", "");
      addMouseListener(_mouseListener);

      // We set the editor pane disabled so it won't get keyboard focus,
      // which makes it uneditable, and so you can't select text inside it.
      setEnabled(false);
    }

    /**
     * Returns error number associated with the given visual coordinates.
     * Returns -1 if none.
     */
    private int _errorAtPoint(Point p) {
      int modelPos = viewToModel(p);

      //System.err.println("model pos=" + modelPos);

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

      //System.err.println("error num=" + errorNum);

      return errorNum;
    }

    public void updateListPane() {
      try {
        _errorListPositions = new Position[_errors.length];

        if (_errors.length == 0) {
          _updateNoErrors();
        }
        else {
          _updateWithErrors();
        }
      }
      catch (BadLocationException impossible) {}

      revalidate();
    }

    private void _updateNoErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      /*
      doc.insertString(0,
                       "Last compilation completed successfully.",
                       null);
      */
      setDocument(doc);

      selectNothing();
    }

    private void _updateWithErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      doc.insertString(0,
                       "Last compilation returned the following errors:\n",
                       null);

      for (int i = 0; i < _errors.length; i++) {
        int startPos = doc.getLength();
        doc.insertString(startPos, _errorText(i), null);
        _errorListPositions[i] = doc.createPosition(startPos);
      }

      setDocument(doc);

      // Select the first error
      _gotoError(0);
    }

    private String _errorText(int i) {
      CompilerError error = _errors[i];

      StringBuffer buf = new StringBuffer();

      if (error.isWarning()) {
        buf.append("Warning: ");
      }
      else {
        buf.append("Error: ");
      }

      buf.append(error.message());
      buf.append("\n");

      return buf.toString();
    }

    private void _removeListHighlight() {
      if (_listHighlightTag != null) {
        getHighlighter().removeHighlight(_listHighlightTag);
        _listHighlightTag = null;
      }
    }

    public void selectNothing() {
      _selectedIndex = -1;
      _removeListHighlight();
      _removePreviousHighlight();
    }

    public void selectItem(int i) {
      _selectedIndex = i;
      _removeListHighlight();

      int startPos = _errorListPositions[i].getOffset();

      // end pos is either the end of the document (if this is the last error)
      // or the char where the next error starts
      int endPos;
      if (i + 1 == _errors.length) {
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

    public int getSelectedIndex() { return _selectedIndex; }
  }
}
