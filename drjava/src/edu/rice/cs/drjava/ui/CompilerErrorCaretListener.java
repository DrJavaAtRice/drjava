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

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * Listens to the caret in a particular DefinitionsPane and
 * highlights the source containing CompilerErrors as appropriate.
 *
 * @version $Id$
 */
public class CompilerErrorCaretListener implements CaretListener {
  private final OpenDefinitionsDocument _openDoc;
  private final ErrorPanel.ErrorListPane _errorListPane;
  private final DefinitionsPane _definitionsPane;
  protected final MainFrame _frame;
  private final Document _document;

  /**
   * Constructs a new caret listener to highlight compiler errors.
   */
  public CompilerErrorCaretListener(OpenDefinitionsDocument doc,
                                    ErrorPanel.ErrorListPane errorListPane,
                                    DefinitionsPane defPane,
                                    MainFrame frame) {
    _openDoc = doc;
    _errorListPane = errorListPane;
    _definitionsPane = defPane;
    _frame = frame;
    _document = doc.getDocument();
  }

  /**
   * Gets the OpenDefinitionsDocument corresponding to this listener.
   */
  public OpenDefinitionsDocument getOpenDefDoc() {
    return _openDoc;
  }

  /**
   * After each update to the caret, determine if changes in
   * highlighting need to be made.  Highlights the line if the
   * compiler output tab is showing.
   */
  public void caretUpdate(CaretEvent evt) {
    if (!getErrorModel().hasErrorsWithPositions(_openDoc)) {
      return;
    }
    // Now we can assume at least one error.
    updateHighlight(evt.getDot());
  }

  /**
   * Update the highlight appropriately.
   */
  public void updateHighlight(int curPos) {
    // Don't highlight unless compiler tab selected
    if (!tabSelected()) {
      _errorListPane.selectNothing();
      return;
    }

    CompilerErrorModel model =  getErrorModel();

    CompilerError error = model.getErrorAtOffset(_openDoc, curPos);

    // if no error is on this line, select the (none) item
    if (error == null) {
      _errorListPane.selectNothing();
    } else {
      
      if (_errorListPane.shouldShowHighlightsInSource()) {
        // No need to move the caret since it's already here!
        _highlightErrorInSource(model.getPosition(error));
      }
       
      // Select item wants the CompilerError
      _errorListPane.selectItem(error);
    }
  }
  
  /**
   * Hides the error highlight in the document.
   */
  public void removeHighlight() {
    if (_errorListPane.getLastDefPane() != null) {
      _errorListPane.getLastDefPane().removeCompilerErrorHighlight();
    }
  }
  
  protected CompilerErrorModel getErrorModel(){
    return _frame.getModel().getCompilerErrorModel();
  }

  protected boolean tabSelected(){
    return _frame.isCompilerTabSelected();
  }

  /**
   * Highlights the given error in the source.
   * @param pos the position of the error
   */
  private void _highlightErrorInSource(Position pos) {
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

      removeHighlight();
      
      //Add 1 if not the first line of the file, so that the highlight range
      // will match the range chosen for the highlight manager.
      if (prevNewline>0) prevNewline++;      
      
      if (prevNewline <= nextNewline) {
        _definitionsPane.addCompilerErrorHighlight(prevNewline, nextNewline);
      }
      _errorListPane.setLastDefPane(_definitionsPane);
    }
    catch (BadLocationException impossible) {
      throw new UnexpectedException(impossible);
    }
  }
}
