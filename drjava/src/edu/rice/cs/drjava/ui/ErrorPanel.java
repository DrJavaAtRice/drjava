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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.HighlightManager;
import gj.util.Hashtable;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * This class takes common code and interfaces from CompilerErrorPanel, JUnitPanel,
 * and JavadocErrorPanel
 * @version $Id$
 */
public abstract class ErrorPanel extends TabbedPanel implements OptionConstants {

  protected static final SimpleAttributeSet NORMAL_ATTRIBUTES = _getNormalAttributes();
  protected static final SimpleAttributeSet BOLD_ATTRIBUTES = _getBoldAttributes();

  /** The total number of errors in the list */
  protected int _numErrors;
  protected JCheckBox _showHighlightsCheckBox;
  protected SingleDisplayModel _model;

  /** Highlight painter for selected list items. */
  protected static DefaultHighlighter.DefaultHighlightPainter
    _listHighlightPainter
      =  new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));

  protected static final SimpleAttributeSet _getBoldAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    StyleConstants.setBold(s, true);
    return s;
  }

  protected static final SimpleAttributeSet _getNormalAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    return s;
  }

  public ErrorPanel(SingleDisplayModel model, MainFrame frame, String s){
    super(frame, s);
    _model = model;
  }

  /** Changes the font of the error list. */
  public void setListFont(Font f) {
    StyleConstants.setFontFamily(NORMAL_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(NORMAL_ATTRIBUTES, f.getSize());

    StyleConstants.setFontFamily(BOLD_ATTRIBUTES, f.getFamily());
    StyleConstants.setFontSize(BOLD_ATTRIBUTES, f.getSize());
  }

  abstract protected ErrorListPane getErrorListPane();

  protected SingleDisplayModel getModel() {
    return _model;
  }

  /**
   * This function returns the correct error model
   */
  abstract protected CompilerErrorModel getErrorModel();

  /**
   * A pane to show compiler errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  abstract public class ErrorListPane extends JEditorPane {

    /**
     * Index into _errorListPositions of the currently selected error.
     */
    private int _selectedIndex;

    /**
     * The start position of each error in the list. This position is the place
     * where the error starts in the error list, as opposed to the place where
     * the error exists in the source.
     */
    protected Position[] _errorListPositions;

    /**
     * Table mapping Positions in the error list to CompilerErrors.
     */
    protected final Hashtable<Position, CompilerError> _errorTable = new Hashtable<Position, CompilerError>();

    /**
     * The DefinitionsPane with the current error highlight.
     * (Initialized to the current pane.)
     */
    private DefinitionsPane _lastDefPane;

    // when we create a highlight we get back a tag we can use to remove it
    private HighlightManager.HighlightInfo _listHighlightTag = null;

    private HighlightManager _highlightManager = new HighlightManager(this);

    protected MouseAdapter defaultMouseListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        selectNothing();
      }
      public void mouseReleased(MouseEvent e) {
        CompilerError error = _errorAtPoint(e.getPoint());

        if (_isEmptySelection() && error != null) {
          getErrorListPane().switchToError(error);
        }
        else {
          selectNothing();
        }
      }
    };

    /**
     * Constructs the CompilerErrorListPane.
     */
    public ErrorListPane() {
      // If we set this pane to be of type text/rtf, it wraps based on words
      // as opposed to based on characters.
      super("text/rtf", "");

      addMouseListener(defaultMouseListener);

      _selectedIndex = 0;
      _errorListPositions = new Position[0];
      _lastDefPane = _frame.getCurrentDefPane();

      this.setFont(new Font("Courier", 0, 20));

      // We set the editor pane disabled so it won't get keyboard focus,
      // which makes it uneditable, and so you can't select text inside it.
      //setEnabled(false);

      // Set the editor pane to be uneditable, but allow selecting text.
      setEditable(false);

      DrJava.getConfig().addOptionListener( OptionConstants.COMPILER_ERROR_COLOR, new CompilerErrorColorOptionListener());

    }

    /**
     * Returns true if the errors should be highlighted in the source
     * @return the status of the JCheckBox _showHighlightsCheckBox
     */
    public boolean shouldShowHighlightsInSource() {
      return _showHighlightsCheckBox.isSelected();
    }

    /**
     * Get the index of the current error in the error array.
     */
    public int getSelectedIndex() { return _selectedIndex; }

    /**
     * Allows the CompilerErrorListPane to remember which DefinitionsPane
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
    protected CompilerError _errorAtPoint(Point p) {
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
        return _errorTable.get(_errorListPositions[errorNum]);
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
        CompilerError e = _errorTable.get(_errorListPositions[i]);

        if (error.equals(e)) {
          return i;
        }
      }

      throw new IllegalArgumentException("Couldn't find index for error " + error);
    }

    /**
     * Returns true if the text selection interval is empty.
     */
    protected boolean _isEmptySelection() {
      return getSelectionStart() == getSelectionEnd();
    }

    /**
     * Update the pane which holds the list of errors for the viewer.
     */
    public void updateListPane(boolean done) {
      try {
        _errorListPositions = new Position[_numErrors];
        _errorTable.clear();

        if (_numErrors == 0) {
          _updateNoErrors(done);
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

    abstract protected void _updateNoErrors(boolean done) throws BadLocationException;

    abstract protected void _updateWithErrors() throws BadLocationException;

    /**
     * Used to show that the last compile was unsuccessful.
     */
    protected void _updateWithErrors(String failureName, String failureMeaning, DefaultStyledDocument doc)
      throws BadLocationException {

      // Print how many errors
      StringBuffer numErrMsg = new StringBuffer("" + _numErrors);
      numErrMsg.append(" " + failureName);
      if (_numErrors > 1) {
        numErrMsg.append("s");
      }
      numErrMsg.append(" " + failureMeaning + ":\n");
      doc.insertString(doc.getLength(), numErrMsg.toString(), BOLD_ATTRIBUTES);

      int errorNum = 0;
      CompilerErrorModel cem = getErrorModel();
      int numErrors = cem.getNumErrors();
      // Show errors
      for (int i = 0; i < numErrors; i++, errorNum++) {
        int startPos = doc.getLength();
        CompilerError err = cem.getError(i);
        
        _insertErrorText(err, doc);

        Position pos = doc.createPosition(startPos);
        _errorListPositions[errorNum] = pos;
        _errorTable.put(pos, err);
      }

      setDocument(doc);

      // Select the first error
      getErrorListPane().switchToError(0);
    }

    /**
     * Prints a message for the given error
     * @param error the error to print
     * @param doc the document in the error pane
     */
    protected void _insertErrorText(CompilerError error, Document doc)
      throws BadLocationException {
        // Show file and line number
        doc.insertString(doc.getLength(), "File: ", BOLD_ATTRIBUTES);
        String fileAndLineNumber = error.getFileMessage() + "  [line: " + error.getLineMessage() + "]";
        doc.insertString(doc.getLength(), fileAndLineNumber + "\n", NORMAL_ATTRIBUTES);


        if (error.isWarning()) {
          doc.insertString(doc.getLength(), "Warning: ", BOLD_ATTRIBUTES);
        }
        else {
          doc.insertString(doc.getLength(), "Error: ", BOLD_ATTRIBUTES);
        }

        doc.insertString(doc.getLength(), error.message(), NORMAL_ATTRIBUTES);
        doc.insertString(doc.getLength(), "\n", NORMAL_ATTRIBUTES);
      }

    /**
     * When the selection of the current error changes, remove
     * the highlight in the error pane.
     */
    protected void _removeListHighlight() {
      if (_listHighlightTag != null) {
        _listHighlightTag.remove();
        _listHighlightTag = null;
      }
    }

    /**
     * Don't select any errors in the error pane.
     */
    public void selectNothing() {
      _selectedIndex = -1;
      _removeListHighlight();

      // Remove highlight from the defPane that has it
      _lastDefPane.removeCompilerErrorHighlight();
    }

    /**
     * Selects the given error inside the error list pane.
     */
    public void selectItem(CompilerError error) {
      try {
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
            _highlightManager.addHighlight(startPos,
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
      catch (IllegalArgumentException iae) {
        // This shouldn't be happening, but it was reported in bug 704006.
        // (_getIndexForError throws it.)
        // We'll at least fail a little more gracefully.
        _removeListHighlight();
      }
    }

    /**
     * Change all state to select a new error, including moving the
     * caret to the error, if a corresponding position exists.
     * @param error The error to switch to
     */
    void switchToError(CompilerError error) {
      if (error == null) return;

      if (error.file() != null) {
        try {
          OpenDefinitionsDocument doc = getModel().getDocumentForFile(error.file());
          // switch to correct def pane
          getModel().setActiveDocument(doc);

          // check and see if this error is without source info, and
          // if so don't try to highlight source info!
          if (!error.hasNoLocation()) {
            CompilerErrorModel<CompilerError> errorModel = getErrorModel();
            Position pos = errorModel.getPosition(error);

            // move caret to that position
            DefinitionsPane defPane = _frame.getCurrentDefPane();
            if (pos != null) {
              int errPos = pos.getOffset();
              if (errPos >= 0 && errPos <= defPane.getText().length()) {
                defPane.centerViewOnOffset(errPos);
              }
            }
            defPane.requestFocus();
            defPane.getCaret().setVisible(true);

          } else {
            // Remove last highlight if we had an error with no position
            _lastDefPane.removeCompilerErrorHighlight();
          }
        } catch (IOException ioe) {
          // Don't highlight the source if file can't be opened
        }
      } else {
        //Remove last highlight if we had an error with no file
        _lastDefPane.removeCompilerErrorHighlight();
      }

      // Select item wants the error, which is what we were passed
      getErrorListPane().selectItem(error);
    }

    /**
     * Another interface to switchToError.
     * @param index Index into the array of positions in the CompilerErrorListPane
     */
    void switchToError(int index) {
      if ((index >= 0) && (index < _errorListPositions.length)) {
        Position pos = _errorListPositions[index];
        CompilerError error = _errorTable.get(pos);
        switchToError(error);
      }
    }

    /**
     * The OptionListener for compiler COMPILER_ERROR_COLOR
     */
    private class CompilerErrorColorOptionListener implements OptionListener<Color> {

      public void optionChanged(OptionEvent<Color> oce) {

        _listHighlightPainter
          =  new DefaultHighlighter.DefaultHighlightPainter(oce.value);

        if (_listHighlightTag != null) {
          _listHighlightTag.refresh(_listHighlightPainter);
        }
      }
    }

  }


}
