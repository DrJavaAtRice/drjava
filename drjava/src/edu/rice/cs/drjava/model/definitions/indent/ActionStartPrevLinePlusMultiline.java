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

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Indents the current line in the document to the indent level of the
 * start of the previous line, adds several lines of text at that indent level,
 * and moves the cursor to a particular line and position.
 * @version $Id$
 */
class ActionStartPrevLinePlusMultiline extends IndentRuleAction {
  private String[] _suffices;
  private int _line = 0;
  private int _position = 0;
  private int _offset = 0;

  /**
   * Creates a multiline insert rule.  It should be noted that although the suffices
   * are referred to as "lines", this class simply appends the strings with a
   * number of spaces for padding.  Any newline characters you intend to place
   * in the document must be explicitly placed within the input strings.
   * Typically, all but the last "line" will have a '\n' character at the end.
   * @param suffices the new lines to be added
   * @param line the line on which to place the cursor 
   * @param position the character within the line string before which to place
   * the cursor
   * @throws IllegalArgumentException if the integer params are negative or
   * outside the appropriate bounds
   */
  public ActionStartPrevLinePlusMultiline(String suffices[],
                                          int line, int position) {
    _suffices = suffices;
    
    // do bounds checking up-front
    if ((line >= 0) && (line < suffices.length)) {
      _line = line;
    }
    else {
      throw new IllegalArgumentException
        ("The specified line was outside the bounds of the specified array.");
    }
    
    if ((position >= 0) && (position <= suffices[line].length())) {
      _position = position;
    }
    else {
      throw new IllegalArgumentException
        ("The specified position was not within the bounds of the specified line.");
    }
    
    // pre-compute the relative offset (without indents) of the new position
    for (int i = 0; i < line; i++) {
      _offset += _suffices[i].length();
    }
    _offset += position;
  }
  
  /**
   * Indents the line according to the previous line, with the suffix lines added
   * and the cursor moved to a specific location.
   * If on the first line, indent is set to 0.
   * @param doc AbstractDJDocument containing the line to be indented.
   * @return this is always false, since we are updating the cursor location
   */
  public boolean indentLine(AbstractDJDocument doc, int reason){
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
        
        // indent and add the suffices
        for (int i = 0; i < _suffices.length; i++) {
          doc.setTab(prefix + _suffices[i], here);
          here += prefix.length() + _suffices[i].length();
        }
        
        // move the cursor to the appropriate position
        int newPos = startLine + _offset + (prefix.length() * (_line + 1));
        doc.setCurrentLocation(newPos);
      }
      else {
        // On first line
        for (int i = 0; i < _suffices.length; i++) {
          doc.setTab(_suffices[i], here);
          here += _suffices[i].length();
        }
      }
      return false;
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
