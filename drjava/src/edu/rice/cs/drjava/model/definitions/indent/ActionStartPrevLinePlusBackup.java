/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

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
