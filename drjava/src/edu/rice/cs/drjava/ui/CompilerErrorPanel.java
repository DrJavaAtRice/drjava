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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import java.util.Arrays;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * The panel which houses the list of errors after an unsuccessful compilation.
 * If the user clicks on the combobox, move the definitions cursor to the
 * error in the source.
 * If the cursor is moved onto a line with an error, select the appropriate
 * error in the list but do not move the cursor.
 *
 * @version $Id$
 */
public class CompilerErrorPanel extends JPanel {

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


  /** The total number of errors in the list */
  private int _numErrors;

  private final SingleDisplayModel _model;
  private final MainFrame _frame;

  private final JButton _showAllButton;
  private final JButton _nextButton;
  private final JButton _previousButton;
  private final ErrorListPane _errorListPane;
  private final JComboBox _compilerChoiceBox;

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public CompilerErrorPanel(SingleDisplayModel model, MainFrame frame) {
    _model = model;
    _frame = frame;

    _showAllButton = new JButton("Show all");
    _showAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _showAllErrors();
        }
    });

    _nextButton = new JButton("Next");
    _nextButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int index = _errorListPane.getSelectedIndex() + 1;
          if (index < _numErrors) {
            _errorListPane.switchToError(index);
          }
        }
    });

    _previousButton = new JButton("Previous");
    _previousButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int index = _errorListPane.getSelectedIndex() -1 ;
          if (index >= 0) {
            _errorListPane.switchToError(index);
          }
        }
    });

    _errorListPane = new ErrorListPane();


    // Limitation: Only compiler choices are those that were available
    // at the time this box was created.
    // Also: The UI will go out of sync with reality if the active compiler
    // is later changed somewhere else. This is because there is no way
    // to listen on the active compiler.
    _compilerChoiceBox = new JComboBox(_model.getAvailableCompilers());
    _compilerChoiceBox.setEditable(false);
    _compilerChoiceBox.setSelectedItem(_model.getActiveCompiler());
    _compilerChoiceBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        _model.setActiveCompiler((CompilerInterface)
                                 _compilerChoiceBox.getSelectedItem());
      }
    });

    setLayout(new BorderLayout());

    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the
    // text. Very bad.
    JScrollPane scroller =
      new JScrollPane(_errorListPane,
                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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

    Box uiBox = Box.createVerticalBox();
    uiBox.add(new JLabel("Compiler", SwingConstants.CENTER));
    uiBox.add(_compilerChoiceBox);
    uiBox.add(Box.createVerticalGlue());

    add(scroller, BorderLayout.CENTER);
    add(uiBox, BorderLayout.EAST);
  }

  /**
   * Returns the ErrorListPane that this panel manages.
   */
  public ErrorListPane getErrorListPane() {
    return _errorListPane;
  }

  /** Changes the font of the error list. */
  public void setListFont(Font f) {
    StyleConstants.setFontFamily(NORMAL_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(NORMAL_ATTRIBUTES, f.getSize());

    StyleConstants.setFontFamily(BOLD_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(BOLD_ATTRIBUTES, f.getSize());
  }

  /** Called when compilation begins. */
  public void setCompilationInProgress() {
    _errorListPane.setCompilationInProgress();
  }

  /**
   * Reset the errors to the current error information.
   * @param errors the current error information
   */
  public void reset() {
    _numErrors = _model.getNumErrors();

    _errorListPane.updateListPane();
    _resetEnabledStatus();
  }


  private void _showAllErrors() {
  }

  /**
   * Reset the enabled status of the "next", "previous", and "show all"
   * buttons in the compiler error panel.
   */
  private void _resetEnabledStatus() {
    _nextButton.setEnabled(_errorListPane.getSelectedIndex() < _numErrors - 1);
    _previousButton.setEnabled(_errorListPane.getSelectedIndex() > 0);
    _showAllButton.setEnabled(_numErrors != 0);
  }



  /**
   * A pane to show compiler errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class ErrorListPane extends JEditorPane {

    /**
     * Index into _errorListPositions of the currently selected error.
     */
    private int _selectedIndex;

    /**
     * The start position of each error in the list. This position is the place
     * where the error starts in the error list, as opposed to the place where
     * the error exists in the source.
     */
    private Position[] _errorListPositions;

    /**
     * Table mapping Positions in the error list to CompilerErrors.
     */
    private final Hashtable _errorTable;

    /**
     * The DefinitionsPane with the current error highlight.
     * (Initialized to the current pane.)
     */
    private DefinitionsPane _lastDefPane;

    // when we create a highlight we get back a tag we can use to remove it
    private Object _listHighlightTag = null;

    // on mouse click, highlight the error in the list and also in the source
    private MouseAdapter _mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        CompilerError error = _errorAtPoint(e.getPoint());

        if (error == null) {
          selectNothing();
        }
        else {
          _errorListPane.switchToError(error);
        }
      }
    };

    /**
     * Constructs the ErrorListPane.
     */
    public ErrorListPane() {
      // If we set this pane to be of type text/rtf, it wraps based on words
      // as opposed to based on characters.
      super("text/rtf", "");
      addMouseListener(_mouseListener);

      _selectedIndex = 0;
      _errorListPositions = new Position[0];
      _errorTable = new Hashtable();
      _lastDefPane = _frame.getCurrentDefPane();

      ErrorListPane.this.setFont(new Font("Courier", 0, 20));

      // We set the editor pane disabled so it won't get keyboard focus,
      // which makes it uneditable, and so you can't select text inside it.
      setEnabled(false);
    }

    /**
     * Get the index of the current error in the error array.
     */
    public int getSelectedIndex() { return _selectedIndex; }

    /**
     * Allows the ErrorListPane to remember which DefinitionsPane
     * currently has an error highlight.
     */
    public void setLastDefPane(DefinitionsPane pane) {
      _lastDefPane = pane;
    }

    /**
     * Gets the last DefinitionsPane with an error highlight.
     */
    public DefinitionsPane getLastDefPane() {
      return _lastDefPane;
    }

    /**
     * Returns CompilerError associated with the given visual coordinates.
     * Returns null if none.
     */
    private CompilerError _errorAtPoint(Point p) {
      int modelPos = viewToModel(p);

      if (modelPos == -1)
        return null;

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

      if (errorNum >= 0) {
        return (CompilerError) _errorTable.get(_errorListPositions[errorNum]);
      }
      else {
        return null;
      }
    }

    /**
     * Returns the index into _errorListPositions corresponding
     * to the given CompilerError.
     */
    private int _getIndexForError(CompilerError error) {
      if (error == null) {
        throw new IllegalArgumentException("Couldn't find index for null error");
      }

      for (int i = 0; i < _errorListPositions.length; i++) {
        CompilerError e = (CompilerError)
          _errorTable.get(_errorListPositions[i]);

        if (error.equals(e)) {
          return i;
        }
      }

      throw new IllegalArgumentException("Couldn't find index for error " + error);
    }

    /**
     * Update the pane which holds the list of errors for the viewer.
     */
    public void updateListPane() {
      try {
        _errorListPositions = new Position[_numErrors];
        _errorTable.clear();

        if (_numErrors == 0) {
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
      int errorNum = 0;

      // Show errors without files
      CompilerError[] errorsNoFiles = _model.getCompilerErrorsWithoutFiles();
      for (int i = 0; i < errorsNoFiles.length; i++, errorNum++) {
        int startPos = doc.getLength();
        _insertErrorText(errorsNoFiles, i, doc);

        // Note to user that there is no file for this error
        doc.insertString(doc.getLength(),
                         "(no associated file)",
                         NORMAL_ATTRIBUTES);
        doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);
        Position pos = doc.createPosition(startPos);
        _errorListPositions[errorNum] = pos;
        _errorTable.put(pos, errorsNoFiles[i]);
      }

      // Show errors for each file
      ListModel defDocs = _model.getDefinitionsDocuments();
      for (int i = 0; i < defDocs.getSize(); i++) {
        // Get errors for this file
        OpenDefinitionsDocument openDoc = (OpenDefinitionsDocument)
          defDocs.getElementAt(i);
        CompilerErrorModel errorModel = openDoc.getCompilerErrorModel();
        CompilerError[] errorsWithoutPositions =
          errorModel.getErrorsWithoutPositions();
        CompilerError[] errorsWithPositions =
          errorModel.getErrorsWithPositions();

        if ((errorsWithoutPositions.length > 0) ||
            (errorsWithPositions.length > 0)) {

          // Grab filename for this set of errors
          String filename = "(Untitled)";
          try {
            File file = openDoc.getFile();
            filename = file.getAbsolutePath();
          }
          catch (IllegalStateException ise) {
            // Not possible: compiled documents must have files
            throw new UnexpectedException(ise);
          }

          // Show errors without source locations
          for (int j = 0; j < errorsWithoutPositions.length; j++, errorNum++) {
            int startPos = doc.getLength();
            _insertErrorText(errorsWithoutPositions, j, doc);

            // Note to user that there is no source info for this error
            doc.insertString(doc.getLength(),
                             " (no source location)",
                             NORMAL_ATTRIBUTES);
            doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);

            Position pos = doc.createPosition(startPos);
            _errorListPositions[errorNum] = pos;
            _errorTable.put(pos, errorsWithoutPositions[j]);
          }

          // Show errors with source locations
          for (int j = 0; j < errorsWithPositions.length; j++, errorNum++) {
            int startPos = doc.getLength();

            // Show file
            doc.insertString(doc.getLength(), "File: ", BOLD_ATTRIBUTES);
            doc.insertString(doc.getLength(), filename + "\n", NORMAL_ATTRIBUTES);

            // Show error
            _insertErrorText(errorsWithPositions, j, doc);
            doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);
            Position pos = doc.createPosition(startPos);
            _errorListPositions[errorNum] = pos;
            _errorTable.put(pos, errorsWithPositions[j]);
          }
        }
      }

      setDocument(doc);

      // Select the first error
      _errorListPane.switchToError(0);
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
      _resetEnabledStatus();

      // Remove highlight from the defPane that has it
      _lastDefPane.removeErrorHighlight();
    }

    /**
     * Selects the given error inside the error list pane.
     */
    public void selectItem(CompilerError error) {
      // Find corresponding index
      int i = _getIndexForError(error);

      _selectedIndex = i;
      _removeListHighlight();

      int startPos = _errorListPositions[i].getOffset();

      // end pos is either the end of the document (if this is the last error)
      // or the char where the next error starts
      int endPos;
      if (i + 1 >= (_numErrors)) {
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

      _resetEnabledStatus();
    }

    /**
     * Change all state to select a new error, including moving the
     * caret to the error, if a corresponding position exists.
     * @param doc OpenDefinitionsDocument containing this error
     * @param errorNum Error number, which is either in _errorsWithoutPositions
     * (if errorNum < _errorsWithoutPositions.length) or in _errors (otherwise).
     * If it's in _errors, we need to subtract _errorsWithoutPositions.length
     * to get the index into the array.
     */
    void switchToError(CompilerError error) {
      if (error == null) return;

      // check and see if this error is without source info, and
      // if so don't try to highlight source info!
      boolean errorHasLocation = (error.lineNumber() > -1);

      if (errorHasLocation) {
        try {
          OpenDefinitionsDocument doc = _model.getDocumentForFile(error.file());
          CompilerErrorModel errorModel = doc.getCompilerErrorModel();
          CompilerError[] errorsWithPositions =
            errorModel.getErrorsWithPositions();

          int index = Arrays.binarySearch(errorsWithPositions, error);
          if (index >= 0) {
            _gotoErrorSourceLocation(doc, index);
          }
        }
        catch (IOException ioe) {
          // Don't highlight the source if file can't be opened
        }
      }
      else {
        // Remove last highlight
        _lastDefPane.removeErrorHighlight();
      }

      // Select item wants the error, which is what we were passed
      _errorListPane.selectItem(error);
    }

    /**
     * Another interface to switchToError.
     * @param index Index into the array of positions in the ErrorListPane
     */
    void switchToError(int index) {
      if ((index >= 0) && (index < _errorListPositions.length)) {
        Position pos = _errorListPositions[index];
        CompilerError error = (CompilerError) _errorTable.get(pos);
        switchToError(error);
      }
    }

    /**
     * Jumps to error location in source
     * @param doc OpenDefinitionsDocument containing the error
     * @param idx Index into _errors array
     */
    private void _gotoErrorSourceLocation(OpenDefinitionsDocument doc,
                                          final int idx) {
      CompilerErrorModel errorModel = doc.getCompilerErrorModel();
      Position[] positions = errorModel.getPositions();


      if ((idx < 0) || (idx >= positions.length)) return;

      int errPos = positions[idx].getOffset();

      // switch to correct def pane
      _model.setActiveDocument(doc);

      // move caret to that position
      DefinitionsPane defPane = _frame.getCurrentDefPane();
      defPane.setCaretPosition(errPos);
      defPane.grabFocus();
    }

  }

}
