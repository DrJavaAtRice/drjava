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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.AbstractDJDocument;

/** Indents the current line in the document to the indent level of the
  * start of the previous line, preserving any text on the current line,
  * and adds several lines of text at that indent level,
  * and moves the cursor to a particular line and position.
  * @version $Id: ActionStartPrevLinePlusMultilinePreserve.java 5668 2012-08-15 04:58:30Z rcartwright $
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

  /** Forwards the call to the enclosed ActionStartPrevLinePlusMultiline.  Only runs in event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return this is always false, since we are updating the cursor location
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {
    super.indentLine(doc, reason); // This call does nothing other than record some indent tracing
    try {
      // copy it so any changes are not remembered
      String[] suffices = new String[_suffices.length];
      for(int i = 0; i < _suffices.length; i++) suffices[i] = _suffices[i];
      
      // get the absolute boundaries of the text on this line
      int here = doc.getCurrentLocation();
      int lineStart = doc._getLineStartPos(here);
      int lineEnd = doc._getLineEndPos(here);

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
      //  javax.swing.JOptionPane.showMessageDialog(null, "\"" + suffices[i] + "\"", "suffices[" + i + "]", 
      //    javax.swing.JOptionPane.PLAIN_MESSAGE);
      a = new ActionStartPrevLinePlusMultiline(suffices, _cursorLine, _cursorPos);
      return a.indentLine(doc, reason);
    }
    catch (BadLocationException e) {
      // Shouldn't happen
      throw new UnexpectedException(e);
    }
  }
}
