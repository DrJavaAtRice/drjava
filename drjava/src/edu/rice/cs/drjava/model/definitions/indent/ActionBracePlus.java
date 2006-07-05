/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;

/** Aligns indentation of the current line to the character that opened the enclosing block or expression list.  
  * Optional additional whitespaces can be passed through the constructor.
  * @version $Id$
  */
public class ActionBracePlus extends IndentRuleAction {
  /** String holding the additional whitespaces to be inserted. */
  private String _suffix;

  /** @param suffix The additional whitespaces to be inserted. */
  public ActionBracePlus(String suffix) { _suffix = suffix; }

  /** Properly indents the line that the caret is currently on.  Replaces all whitespace characters at the beginning of
    * the line with the appropriate spacing or characters.<p>
    * Preconditions: must be inside a brace.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @return true if the caller should update the current location, false if the indenter has already done this.
    */
  public boolean indentLine(AbstractDJDocument doc, int reason) {
    boolean supResult = super.indentLine(doc, reason);
    int here = doc.getCurrentLocation();
    int startLine = doc.getLineStartPos(here);
    doc.setCurrentLocation(startLine);
    IndentInfo ii = doc.getIndentInformation();

    // Check preconditions
    if ((ii.braceType.equals("")) ||
        (ii.distToBrace < 0)) {
      // Can't find brace, so do nothing.
      return supResult;
    }

    // Find length to brace
    int bracePos = startLine - ii.distToBrace;
    int braceNewLine = 0;
    if (ii.distToNewline >=0) {
      braceNewLine = startLine - ii.distToNewline;
    }
    int braceLen = bracePos - braceNewLine;

    // Create tab string
    final StringBuilder tab = new StringBuilder(_suffix.length() + braceLen);
    for (int i=0; i < braceLen; i++) {
      tab.append(" ");
    }
    tab.append(_suffix);

    if (here > doc.getLength()) {
      here = doc.getLength() - 1;
    }
    doc.setCurrentLocation(here);

    doc.setTab(tab.toString(), here);
    
    return supResult;
  }
}
