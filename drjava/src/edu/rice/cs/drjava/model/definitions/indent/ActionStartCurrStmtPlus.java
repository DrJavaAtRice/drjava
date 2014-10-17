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

/** Assumes that the current line is NOT the start of the current statement and preceding lines are indented correctly. 
  * Indents the current line to the indent level of the start of the containing statement plus the given suffix string.
  * @version $Id: ActionStartCurrStmtPlus.java 5710 2012-09-04 04:45:09Z rcartwright $
  */
public class ActionStartCurrStmtPlus extends IndentRuleAction {
  private int _suffix;

  /** Constructs a new rule with the given suffix string.
    * @param suffix String to append to indent level of brace
    */
  public ActionStartCurrStmtPlus(int suffix) {
    super();
    _suffix = suffix;
  }

  /** Properly indents the line containing the caret assuming current statement starts on a preceding (!) line.
    * Replaces all whitespace characters at the beginning of the line with the appropriate spacing or characters.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentition is taking place
    * @return true if the caller should update the current location itself,
    * false if the indenter has already handled this
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    boolean supResult = super.indentLine(doc, reason);

    /** This method is simply a call to _getIndentOfStmt for the preceding line.  This method is fully tested in 
      * IndentHelperTest, so no additional tests are provided for this class.
      */

    int indent = 0;
    
    int prevLineEnd = doc._getLineStartPos() - 1;
    
    if (prevLineEnd < 0) return supResult;  // start of prev line was 0 or ERROR_INDEX

    indent = doc._getIndentOfStmt(prevLineEnd, new char[] {';','{','}'}, new char[] {' ', '\t','\n'});

    indent = indent + _suffix;
    doc.setTab(indent, doc.getCurrentLocation());
    
    return supResult;
  }
}
