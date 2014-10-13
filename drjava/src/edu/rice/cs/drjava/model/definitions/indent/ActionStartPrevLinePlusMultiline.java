/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Indents the current line in the document to the indent level of the
  * start of the previous line, adds several lines of text at that indent level,
  * and moves the cursor to a particular line and position.
  * @version $Id: ActionStartPrevLinePlusMultiline.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
class ActionStartPrevLinePlusMultiline extends IndentRuleAction {
  private String[] _suffices;
  private int _line = 0;
  // private int _position = 0;
  private int _offset = 0;

  /** Creates a multiline insert rule.  It should be noted that although the suffices
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
    
    if ((position < 0) || (position > suffices[line].length())) {
      throw new IllegalArgumentException
        ("The specified position was not within the bounds of the specified line.");
    }
    
    // pre-compute the relative offset (without indents) of the new position
    for (int i = 0; i < line; i++) {
      _offset += _suffices[i].length();
    }
    _offset += position;
  }
  
  /** Indents the line according to the previous line, with the suffix lines added and the cursor moved to a specific 
    * location.  If on the first line, indent is set to 0.  Only runs in event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return this is always false, since we are updating the cursor location
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    super.indentLine(doc, reason);
    try {
      // Find start of line
      int here = doc.getCurrentLocation();
      int startLine = doc._getLineStartPos(here);

      if (startLine > 0) {
        // Find prefix of previous line
        int startPrevLine = doc._getLineStartPos(startLine - 1);
        int firstChar = doc._getLineFirstCharPos(startPrevLine);
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
