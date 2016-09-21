/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/** Indents the current line in the document to the indent level of the start of the brace enclosing the start of the
  * current line, plus the given suffix.
  * @version $Id$
  */
public class ActionStartStmtOfBracePlus extends IndentRuleAction {
  private int _suffix;
  
  /** Constructs a new rule with the given suffix string.
    * @param suffix String to append to indent level of brace
    */
  public ActionStartStmtOfBracePlus(int suffix) {
    super();
    _suffix = suffix;
  }

  /** Properly indents the line that the caret is currently on. Replaces all whitespace characters at the beginning of the
    * line with the appropriate spacing or characters.   Assumes read lock is already held.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    */
  public void indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {

    super.indentLine(doc, reason); // This call does nothing other than record some indent tracing
    int pos = doc.getCurrentLocation();
//    Utilities.show("indentLine in ActionStartStmtOfBracePlus called on doc:\n" + doc.getText() + "'\nat location " + pos);
    // Get distance to brace
    int lineStart = doc._getLineStartPos(pos);
    if (lineStart < 0) lineStart = 0;
    BraceInfo info = doc._getLineEnclosingBrace();
    int distToLineEnclosingBrace = info.distance();
//    System.err.println("dist to brace = " + distToLineEnclosingBrace);

    // If there is no brace, align to left margin; can't happen when called from rule 19
    if (distToLineEnclosingBrace == -1) {
      doc.setTab(_suffix, pos);
      return;
    }

    // Get the absolute position of (the left edge of) the line enclosing brace
    final int bracePos = lineStart - distToLineEnclosingBrace;
//    System.err.println("bracePos = " + bracePos);
    final int indent = doc._getIndentOfCurrStmt(bracePos) + _suffix;
//    System.err.println("indent = " + doc._getIndentOfCurrStmt(bracePos));

    doc.setTab(indent, pos);
  }

}
