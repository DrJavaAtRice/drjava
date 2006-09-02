/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2004 JavaPLT group at Rice University (javaplt@rice.edu)
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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Indents the current line in the document to the indent level of the
  * start of the previous line, preserving any text on the current line,
  * and adds several lines of text at that indent level,
  * and moves the cursor to a particular line and position.
  * @version $Id$
  */
class ActionStartPrevLinePlusMultilinePreserve extends IndentRuleAction {
  private String[] _suffices;
  private int _cursorLine, _cursorPos, _psrvLine, _psrvPos;

  /** Creates a multiline insert rule, properly preserving any text on current line.
    * @param suffices the new lines to be added
    * @param cursorLine the line on which to place the cursor
    * @param cursorPos the character within the line string before which to place
    * the cursor
    * @param psrvLine the line in suffices on which to place the preserved text
    * @param psrvPos the character within the line string in suffices before which
    * to place the preserved text
    * @throws IllegalArgumentException if the integer params are negative or
    * outside the appropriate bounds
    */
  public ActionStartPrevLinePlusMultilinePreserve(String suffices[],
                                                  int cursorLine, int cursorPos,
                                                  int psrvLine, int psrvPos) {
    _suffices = suffices;
    _cursorLine = cursorLine;
    _cursorPos = cursorPos;
    _psrvLine = psrvLine;
    _psrvPos = psrvPos;
  }

  /**
   * Forwards the call to the enclosed ActionStartPrevLinePlusMultiline _a
   * @param doc AbstractDJDocument containing the line to be indented.
   * @return this is always false, since we are updating the cursor location
   */
  public boolean indentLine(AbstractDJDocument doc, int reason) {
    try {
      // copy it so any changes are not remembered
      String[] suffices = new String[_suffices.length];
      for(int i = 0; i < _suffices.length; i++)
        suffices[i] = _suffices[i];
      
      // get the absolute boundaries of the text on this line
      int here = doc.getCurrentLocation();
      int lineStart = doc.getLineStartPos(here);
      int lineEnd = doc.getLineEndPos(here);

      // cut the original text out of the current line
      int lineLength = lineEnd-lineStart;
      String preserved = doc.getText(lineStart, lineLength);
      doc.remove(lineStart, lineLength);

      // Paste the cut text in the correct place in the suffices array
      String prefix = suffices[_psrvLine].substring(0,_psrvPos);
      String suffix = suffices[_psrvLine].substring(_psrvPos);
      suffices[_psrvLine] = prefix + preserved + suffix;

      // forward the rest of the work to the other rule
      ActionStartPrevLinePlusMultiline a;
      //for(int i = 0; i < _suffices.length; i++)
      //  javax.swing.JOptionPane.showMessageDialog(null, "\""+suffices[i]+"\"", "suffices["+i+"]",javax.swing.JOptionPane.PLAIN_MESSAGE);;
      a = new ActionStartPrevLinePlusMultiline(suffices, _cursorLine, _cursorPos);
      return a.indentLine(doc, reason);
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
