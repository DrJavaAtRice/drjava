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
      
      if (_errorListPane.shouldShowHighlightsInSource()) {
        // No need to move the caret since it's already here!
        _highlightErrorInSource(shouldSelect);
      }
       
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
    Position pos = _positions[newIndex];
    if (pos == null) {
      return;
    }
    int errPos = pos.getOffset();

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

      if (_errorListPane.getLastDefPane() != null) {
        _errorListPane.getLastDefPane().removeErrorHighlight();
      }
      //Add 1 if not the first line of the file, so that the highlight range
      // will match the range chosen for the highlight manager.
      if (prevNewline>0) prevNewline++;      
      
      if (prevNewline <= nextNewline) {
        _definitionsPane.addErrorHighlight(prevNewline, nextNewline);
      }
      _errorListPane.setLastDefPane(_definitionsPane);
    }
    catch (BadLocationException impossible) {
      throw new UnexpectedException(impossible);
    }
  }
}
