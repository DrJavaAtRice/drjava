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

import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import static edu.rice.cs.drjava.model.AbstractDJDocument.*;

/** Indents the current line in the document to the indent level of the start of the brace ('{', '('} (and optionally
  * {'=', "=>"} at line end) enclosing the start of the current line, plus the given suffix.
  * @version $Id: ActionStartStmtOfBracePlus.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class ActionStartStmtOfBracePlus extends IndentRuleAction {
  private final int _suffix;
  private final boolean _includeScalaBraces;  // includes local Scala brace forms "=>" and "="
  
  /** Constructs a new rule with the given suffix string.
    * @param suffix String to append to indent level of brace
    */
  public ActionStartStmtOfBracePlus(int suffix, boolean includeScalaBraces) {
    super();
    _suffix = suffix;
    _includeScalaBraces = includeScalaBraces;
  }

  /** Properly indents the line that the caret is currently on. Replaces all whitespace characters at the beginning of the
    * line with the appropriate spacing or characters.   Only runs in event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return true if the caller should update the current location itself, false if the indenter has already handled it
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {

    boolean supResult = super.indentLine(doc, reason); // This call does nothing other than record some indent tracing
    int pos = doc.getCurrentLocation();
//    System.err.println("***** ActionStartStmtOfBracePlus.indentLine called at location " + pos + "  line = '" + 
//                       doc._getCurrentLine() + "'" + " includeScalaBraces = " + _includeScalaBraces);
    try {
      // Get bracePos
      int bracePos = (_includeScalaBraces) ? doc.findEnclosingScalaBracePosWithEquals(pos) : 
        doc.findPrevDelimiter(pos, STRICT_OPENING_BRACES);
      
      if (bracePos == ERROR_INDEX) return supResult;  // will never happen if pos has an enclosing (Scala) brace 
//      System.err.println("[ASSOBP] bracePos = " + bracePos + "; brace = '" + doc.getText(bracePos,1).charAt(0) + "'");
      final int indent = doc._getIndentOfRestrictedStmt(bracePos) + _suffix;  // ignore any '=' prelude
//      System.err.println("[ASSOBP] indent = " + indent + " _suffix = " + _suffix);
      
      doc.setTab(indent, pos);
    }
    catch(BadLocationException ble) { /* do nothing */ }
    return supResult;
  }
}
