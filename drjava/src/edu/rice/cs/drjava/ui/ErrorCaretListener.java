/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
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
 * highlights the source containing Errors as appropriate.
 *
 * @version $Id$
 */
public class ErrorCaretListener implements CaretListener {
  private final OpenDefinitionsDocument _openDoc;
  private final DefinitionsPane _definitionsPane;
  protected final MainFrame _frame;

  /**
   * Constructs a new caret listener to highlight errors.
   */
  public ErrorCaretListener(OpenDefinitionsDocument doc,
                            DefinitionsPane defPane,
                            MainFrame frame) {
    _openDoc = doc;
    _definitionsPane = defPane;
    _frame = frame;
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
    // Now we can assume at least one error.
    updateHighlight(evt.getDot());
  }

  /**
   * Update the highlight appropriately.
   */
  public void updateHighlight(int curPos) {
    // Don't highlight unless compiler tab selected
//    if (!tabSelected()) {
//      _errorListPane.selectNothing();
//      return;
//    }

    ErrorPanel panel = _frame.getSelectedErrorPanel();
    if (panel == null) {
      // no error panel is currently selected
      return;
    }
    CompilerErrorModel model =  panel.getErrorModel();
    
    if (!model.hasErrorsWithPositions(_openDoc)) {
      return;
    }

    CompilerError error = model.getErrorAtOffset(_openDoc, curPos);

    ErrorPanel.ErrorListPane errorListPane = panel.getErrorListPane();
    // if no error is on this line, select the (none) item
    if (error == null) {
      errorListPane.selectNothing();
    } 
    else {      
      if (errorListPane.shouldShowHighlightsInSource()) {
        // No need to move the caret since it's already here!
        _highlightErrorInSource(model.getPosition(error));
      }
       
      // Select item wants the CompilerError
      errorListPane.selectItem(error);
    }
  }
  
  /**
   * Hides the error highlight in the document.
   */
  public void removeHighlight() {
    _definitionsPane.removeErrorHighlight();
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
      String text = _openDoc.getText(0, _openDoc.getLength());

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
        nextNewline = _openDoc.getLength();
      }

      removeHighlight();
      
      //Add 1 if not the first line of the file, so that the highlight range
      // will match the range chosen for the highlight manager.
      if (prevNewline>0) prevNewline++;      
      
      if (prevNewline <= nextNewline) {
        _definitionsPane.addErrorHighlight(prevNewline, nextNewline);
      }
    }
    catch (BadLocationException impossible) {
      throw new UnexpectedException(impossible);
    }
  }
}
