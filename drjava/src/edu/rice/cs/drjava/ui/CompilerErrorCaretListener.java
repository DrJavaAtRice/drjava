package edu.rice.cs.drjava.ui;

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
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;

/**
 * Listens to the caret in a particular DefinitionsPane and
 * highlights the source containing CompilerErrors as appropriate.
 *
 * @version $Id$
 */
public class CompilerErrorCaretListener implements CaretListener {
  private final OpenDefinitionsDocument _openDoc;
  private final ErrorListPane _errorListPane;
  private final DefinitionsPane _definitionsPane;
  private final Document _document;

  private CompilerErrorModel _model;
  private Position[] _positions;


  /**
   * Constructs a new caret listener to highlight compiler errors.
   */
  public CompilerErrorCaretListener(OpenDefinitionsDocument doc,
                                    ErrorListPane errorListPane,
                                    DefinitionsPane defPane) {
    _openDoc = doc;
    _errorListPane = errorListPane;
    _definitionsPane = defPane;
    _document = doc.getDocument();

    resetErrorModel();
  }

  /**
   * Gets the OpenDefinitionsDocument corresponding to this listener.
   */
  public OpenDefinitionsDocument getOpenDefDoc() {
    return _openDoc;
  }

  /**
   * Resets the CompilerErrorModel after a new compile.
   */
  public void resetErrorModel() {
    _model = _openDoc.getCompilerErrorModel();
    _positions = _model.getPositions();
  }

  /**
   * After each update to the caret, determine if changes in
   * highlighting need to be made.
   */
  public void caretUpdate(CaretEvent evt) {
    if (_positions.length == 0) {
      return;
    }

    // Now we can assume at least one error.
    updateHighlight(evt.getDot());
  }

  /**
   * Update the highlight appropriately.
   */
  public void updateHighlight(int curPos) {

    // check if the dot is on a line with an error.
    // Find the first error that is on or after the dot. If this comes
    // before the newline after the dot, it's on the same line.
    int errorAfter; // index of the first error after the dot
    for (errorAfter = 0; errorAfter < _positions.length; errorAfter++) {
      if (_positions[errorAfter].getOffset() >= curPos) {
        break;
      }
    }

    // index of the first error before the dot
    int errorBefore = errorAfter - 1;

    // this will be set to what we want to select, or -1 if nothing
    int shouldSelect = -1;

    if (errorBefore >= 0) { // there's an error before the dot
      int errPos = _positions[errorBefore].getOffset();
      try {
        String betweenDotAndErr = _document.getText(errPos, curPos - errPos);

        if (betweenDotAndErr.indexOf('\n') == -1) {
          shouldSelect = errorBefore;
        }
      }
      catch (BadLocationException willNeverHappen) {}
    }

    if ((shouldSelect == -1) && (errorAfter != _positions.length)) {
      // we found an error on/after the dot
      // if there's a newline between dot and error,
      // then it's not on this line
      int errPos = _positions[errorAfter].getOffset();
      try {
        String betweenDotAndErr = _document.getText(curPos, errPos - curPos);

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

      // Select item wants the CompilerError
      CompilerError[] errors = _model.getErrorsWithPositions();
      _errorListPane.selectItem(errors[shouldSelect]);
    }
  }

  /**
   * Highlights the given error in the source.
   * @param newIndex Index into _errors array
   */
  private void _highlightErrorInSource(int newIndex) {
    int errPos = _positions[newIndex].getOffset();

    try {
      String text = _document.getText(0, _document.getLength());

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
        nextNewline = _document.getLength();
      }

      _errorListPane.getLastDefPane().removeErrorHighlight();
      _definitionsPane.addErrorHighlight(prevNewline, nextNewline);
      _errorListPane.setLastDefPane(_definitionsPane);
    }
    catch (BadLocationException impossible) {
      throw new UnexpectedException(impossible);
    }
  }
}
