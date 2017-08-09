/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

/** Indents the current line in the document to the indent level of the start of the statement previous to the one the
  * cursor is currently on, plus the given suffix string.
  *
  * @version $Id$
  */
public class ActionStartPrevStmtPlus extends IndentRuleAction {
  private int _suffix;  // number of blanks in suffix
  private boolean _useColon;

  /** Constructs a new rule with the given suffix string.
    * @param suffix String to append to indent level of brace
    * @param colonIsDelim whether to include colons as statement delimiters
    */
  public ActionStartPrevStmtPlus(int suffix, boolean colonIsDelim) {
    super();
    _suffix = suffix;
    _useColon = colonIsDelim;
  }

  /** Properly indents the line that the caret is currently on. Replaces all whitespace characters at the beginning of
    * the line with the appropriate spacing or characters.  Assumes reduced lock is alread held [Archaic].  Only
    * runs in the event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    */
  public void indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    super.indentLine(doc, reason);
    int here = doc.getCurrentLocation();
    
    // Find end of previous statement, immediately enclosing brace, or end of case statement
    char[] delims = {';', '{', '}'};
    int lineStart = doc._getLineStartPos(here);  // find start of current line
    int prevDelimiterPos;
    try { prevDelimiterPos = doc.findPrevDelimiter(lineStart, delims); }  // find pos of delimiter preceding line start
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // If no preceding delimiter found, align to left margin
    if (prevDelimiterPos <= 0) {
      doc.setTab(_suffix, here);
      return;
    }
    
    try {
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);    // get delimiter char
      char[] ws = {' ', '\t', '\n', ';'};  // Note that ';' is whitespace here
      if (delim == ';') {
        int testPos = doc._findPrevCharPos(prevDelimiterPos, ws);  // find char preceding ';' delimiter
        char testDelim = doc.getText(testPos,1).charAt(0);
        if ( testDelim == '}' || testDelim == ')') {
          prevDelimiterPos = testPos;                             // if this char is '}' or ')', use it as delimiter
        }
      }
    } catch (BadLocationException e) { throw new UnexpectedException(e); /* Should never happen */ }
    
    try {
      // Jump over {-} region if delimiter was a close brace.
      char delim = doc.getText(prevDelimiterPos, 1).charAt(0);
      
      if (delim == '}' || delim == ')') {
        //BraceReduction reduced = doc.getReduced();
        //we're pretty sure the doc is in sync.
//        doc.resetReducedModelLocation();  // why reset the reduced model comment walker?
        
        assert doc.getCurrentLocation() == here;
        doc.setCurrentLocation(prevDelimiterPos + 1);   // move cursor to right of '}' or ')' delim
        int delta = doc.balanceBackward(); // Number of chars backward to matching '{' or '('
        if (delta < 0) { // no matching delimiter!
          // No matching '{' or '(' preceding this delimiter here
          // but throwing an unexpected exception is not right, because the
          // user may be trying to indent code that is not balanced!
          return;
        }
        prevDelimiterPos -= (delta - 1);  // Position just to right of matching '{' or '('
        doc.setCurrentLocation(here);
        
        assert doc.getText(prevDelimiterPos, 1).charAt(0) == '{' || 
          doc.getText(prevDelimiterPos, 1).charAt(0) == '(';
      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // Get indent of prev statement
    // Include colons as end of statement (ie. "case")
    char[] indentDelims;
    char[] indentDelimsWithColon = {';', '{', '}', ':'};
    char[] indentDelimsWithoutColon = {';', '{', '}'};
    if (_useColon) indentDelims = indentDelimsWithColon;
    else indentDelims = indentDelimsWithoutColon;
    
    int indent = doc._getIndentOfCurrStmt(prevDelimiterPos, indentDelims);
    
    indent = indent + _suffix;
    doc.setTab(indent, here);
  }
}

