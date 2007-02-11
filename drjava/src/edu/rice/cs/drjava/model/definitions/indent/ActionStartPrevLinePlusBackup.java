/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/**
 * Indents the current line in the document to the indent level of the
 * start of the previous line, plus the given suffix, then backs up to a
 * specific cursor position.
 * @version $Id$
 */
class ActionStartPrevLinePlusBackup extends IndentRuleAction {
  private String _suffix;
  private int _position = 0;

  /**
   * Rule that repeats the indentation from the previous line, plus a string,
   * then moves the cursor to a specified location.
   * @param suffix The string to be added
   * @param position the character within the suffix string before which to
   * place the cursor
   * @throws IllegalArgumentException if the position is negative or
   * outside the bounds of the suffix string
   */
  public ActionStartPrevLinePlusBackup(String suffix, int position) {
    _suffix = suffix;
    
    if ((position >= 0) && (position <= suffix.length())) {
      _position = position;
    }
    else {
      throw new IllegalArgumentException
        ("The specified position was not within the bounds of the suffix.");
    }
  }

  /**
   * Indents the line according to the previous line, with the suffix string added,
   * then backs up the cursor position a number of characters.
   * If on the first line, indent is set to 0.
   * @param doc AbstractDJDocument containing the line to be indented.
   * @param The reason that the indentation is taking place
   * @return this is always false, since we are updating the cursor location
   */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    super.indentLine(doc, reason);
    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc.getLineStartPos(here);

      if (startLine > AbstractDJDocument.DOCSTART) {
        // Find prefix of previous line
        int startPrevLine = doc.getLineStartPos(startLine - 1);
        int firstChar = doc.getLineFirstCharPos(startPrevLine);
        String prefix = doc.getText(startPrevLine, firstChar - startPrevLine);
        
        // indent and add the suffix
        doc.setTab(prefix + _suffix, here);
        
        // move the cursor to the new position
        doc.setCurrentLocation(startLine + prefix.length() + _position);
      }
      else {
        // On first line
        doc.setTab(_suffix, here);
        
        // move the cursor to the new position
        doc.setCurrentLocation(here + _position);
      }
      
      return false;
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
