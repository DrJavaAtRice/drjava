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

import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import javax.swing.text.BadLocationException;

/**
 * Indents the current line in the document to the indent level of the
 * start of the contract or statement of the brace enclosing the start of the
 * current line, plus the given suffix.
 * @version $Id$
 */
public class ActionStartStmtOfBracePlus extends IndentRuleAction {
  private String _suffix;

  /**
   * Constructs a new rule with the given suffix string.
   * @param prefix String to append to indent level of brace
   */
  public ActionStartStmtOfBracePlus(String suffix) {
    super();
    _suffix = suffix;
  }

  /**
   * Properly indents the line that the caret is currently on.
   * Replaces all whitespace characters at the beginning of the
   * line with the appropriate spacing or characters.
   * @param doc AbstractDJDocument containing the line to be indented.
   * @return true if the caller should update the current location itself,
   * false if the indenter has already handled this
   */
  public boolean indentLine(AbstractDJDocument doc, int reason){
    boolean supResult = super.indentLine(doc, reason);
    int pos = doc.getCurrentLocation();

    // Get distance to brace
    IndentInfo info = doc.getIndentInformation();
    int distToBrace = info.distToBrace;

    // If there is no brace, align to left margin
    if (distToBrace == -1) {
      doc.setTab(_suffix, pos);
      return supResult;
    }

    // Get the absolute position of the brace
    int bracePos = pos - distToBrace;

    String indent = "";
    try {
      indent = doc.getIndentOfCurrStmt(bracePos);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    indent = indent + _suffix;

    doc.setTab(indent, pos);
    
    return supResult;
  }

}
