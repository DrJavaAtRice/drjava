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

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
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
   * @return true if the caller should update the current location itself,
   * false if the indenter has already handled this
   */
  public boolean indentLine(AbstractDJDocument doc, int reason) {
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

  private boolean _isPrevNonWSCharEqualTo(AbstractDJDocument doc,int pos,char c) {
    try {
      int prevPos = doc.findPrevNonWSCharPos(pos);
      if (prevPos < 0) return false;
      return (doc.getText(prevPos,1).charAt(0) == c);
    }
    catch (BadLocationException e) {
      return false;
    }
  }
}

