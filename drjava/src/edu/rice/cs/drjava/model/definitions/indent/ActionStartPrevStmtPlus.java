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

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.BadLocationException;

/**
 * Indents the current line in the document to the indent level of the
 * start of the statement previous to the one the cursor is currently on,
 * plus the given suffix string.
 *
 * @version $Id$
 */
public class ActionStartPrevStmtPlus extends IndentRuleAction {
  private String _suffix;
  private boolean _useColon;

  /**
   * Constructs a new rule with the given suffix string.
   * @param suffix String to append to indent level of brace
   * @param colonIsDelim whether to include colons as statement delimiters
   */
  public ActionStartPrevStmtPlus(String suffix, boolean colonIsDelim) {
    super();
    _suffix = suffix;
    _useColon = colonIsDelim;
  }

  /**
   * Properly indents the line that the caret is currently on.
   * Replaces all whitespace characters at the beginning of the
   * line with the appropriate spacing or characters.
   *
   * @param doc AbstractDJDocument containing the line to be indented.
   * @param reason The reason that the indentation is taking place
   * @return true if the caller should update the current location itself,
   * false if the indenter has already handled this
   */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    boolean supResult = super.indentLine(doc, reason);
    String indent = "";
    int here = doc.getCurrentLocation();
    
    // Find end of previous statement (or end of case statement)
    char[] delims = {';', '{', '}'};
    int lineStart = doc.getLineStartPos(here);
    int prevDelimiterPos;
    try {
      prevDelimiterPos = doc.findPrevDelimiter(lineStart, delims);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    
    // For DOCSTART, align to left margin
    if (prevDelimiterPos <= AbstractDJDocument.DOCSTART) {
      doc.setTab(_suffix, here);
      return supResult;
    }
    
    try {
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);
      char[] ws = {' ', '\t', '\n', ';'};
      if (delim == ';') {
        int testPos = doc.findPrevCharPos(prevDelimiterPos, ws);
        if (doc.getText(testPos,1).charAt(0) == '}') {
          prevDelimiterPos = testPos;
        }
      }
    } catch (BadLocationException e) {
      //do nothing
    }
    
    try {
      // Jump over {-} region if delimiter was a close brace.
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);
      
      if (delim == '}') {
        //BraceReduction reduced = doc.getReduced();
        //we're pretty sure the doc is in sync.
        doc.resetReducedModelLocation();
        
        int dist = prevDelimiterPos - here + 1;
        
        doc.move(dist);
        prevDelimiterPos -= doc.balanceBackward() - 1;
        doc.move(-dist);
        
      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    
    // Get indent of prev statement
    try {
      // Include colons as end of statement (ie. "case")
      char[] indentDelims;
      char[] indentDelimsWithColon = {';', '{', '}', ':'};
      char[] indentDelimsWithoutColon = {';', '{', '}'};
      if (_useColon) indentDelims = indentDelimsWithColon;
      else indentDelims = indentDelimsWithoutColon;
      
      indent = doc.getIndentOfCurrStmt(prevDelimiterPos, indentDelims);
      
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
    
    indent = indent + _suffix;
    doc.setTab(indent, here);
    return supResult;
  }

//  private boolean _isPrevNonWSCharEqualTo(AbstractDJDocument doc,int pos,char c) {
//    try {
//      int prevPos = doc.findPrevNonWSCharPos(pos);
//      if (prevPos < 0) return false;
//      return (doc.getText(prevPos,1).charAt(0) == c);
//    }
//    catch (BadLocationException e) {
//      return false;
//    }
//  }
}

