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

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceInfo;
import edu.rice.cs.util.UnexpectedException;
import javax.swing.text.BadLocationException;

import static edu.rice.cs.drjava.model.AbstractDJDocument.ERROR_INDEX;

/** Indents the current line in the document to the indent level of the start of the statement preceding the one the
  * cursor is currently on, plus the given suffix padding (a number of spaces).  The preceding statement may be a 
  * statement prelude (like "if (...) {") of the current line in which case an indentLevel prefix must be added.
  * NOTE: this method assumes that the previous line is part of the preceding statement.
  * 
  * TO DO: eliminate _suffix argument; no longer used.
  *
  * @version $Id: ActionStartPrevStmtPlus.java 5711 2012-09-11 19:42:33Z rcartwright $
  */
public class ActionStartPrevStmtPlus extends IndentRuleAction {
  private final int _suffix;  // number of spaces in suffix
  private final boolean _useColon;
  private final int _indentLevel;
  
  /** Constructs a new rule with the given suffix string.
    * @param suffix String to append to indent level of brace
    * @param colonIsDelim whether to include colons as statement delimiters   NOTE: always false in Scala
    */
  public ActionStartPrevStmtPlus(int suffix, boolean colonIsDelim, int indentLevel) {
    super();
    _suffix = suffix;
    _useColon = colonIsDelim;
    _indentLevel = indentLevel;
  }
  
  public ActionStartPrevStmtPlus(int suffix, int indentLevel) { this(suffix, false, indentLevel); }
  
  /** Properly indents the line that the caret is currently on, assuming previous/enclosing statement is properly indented. 
    * Replaces all whitespace characters at the beginning of the line with the approprate spaces.  
    * Only runs in the event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return true if the caller should update the current location itself, false if the indenter has already handled it
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    boolean supResult = super.indentLine(doc, reason);
    int orig = doc.getCurrentLocation();
//    System.err.println("**** [ASPSP]indentline called on line: '" + doc._getCurrentLine() + "'" + " origPos = " + orig);
    
    // Find start of current line
    int lineStart = doc._getLineStartPos(orig);  
    if (lineStart <= 0) {  // No preceding line (with valid program text) exists; indent is 0
      doc.setTab(0, orig);
      return supResult;
    }
    int indent = doc._getIndentOfStmt(lineStart - 1);  // ASSUMES PREV LINE IS PART OF PREV STMT
    
//    System.err.println("[ASPSP]Indent of prev stmt '" + doc._getCurrentLine(lineStart - 1) + "' = " + indent + 
//                       "\n       suffix = " + _suffix);
//    System.err.println("In ActionStartPrevStmtPlus, setting indent at pos " + indent);
    doc.setTab(indent, orig);
    return supResult;
  }
}

