/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** Determines whether the current line in the document starts with a specific character sequence, skipping over any 
  * comments on that line. The character sequence is passed to the constructor of the class as a String argument.
  * @version $Id$
  */
public class QuestionCurrLineStartsWithSkipComments extends IndentRuleQuestion {
  /** The String to be matched. This String may not contain whitespace characters or comment-delimiting characters. */
  private String _prefix;
  
  /** @param yesRule The decision subtree for the case that this rule applies in the current context.
    * @param noRule The decision subtree for the case that this rule does not apply in the current context.
    */
  public QuestionCurrLineStartsWithSkipComments(String prefix, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /** Determines whether or not the current line in the document starts with the character sequence specified by the
    * String field _prefix, skipping over any comments on that line.  Assumes that write lock and reduced lock are
    * already held.
    * @param doc The AbstractDJDocument containing the current line.
    * @return True iff the current line in the document starts with the
    * character sequence specified by the String field _prefix.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    try {
      // Find the first non-whitespace character on the current line.
      
      int currentPos = doc.getCurrentLocation(),
        startPos   = doc.getLineFirstCharPos(currentPos),
        endPos     = doc.getLineEndPos(currentPos),
        lineLength = endPos - startPos;
      
      char currentChar, previousChar = '\0';
      String text = doc.getText(startPos, lineLength);
      
      for (int i = 0; i < lineLength; i++) {
        // Get state for walker position.
        
        doc.move( startPos - currentPos + i);
        ReducedModelState state = doc.getStateAtCurrent();
        doc.move(-startPos + currentPos - i);
        
        
        currentChar = text.charAt(i);
        
        if (state.equals(INSIDE_LINE_COMMENT)) return false;
        if (state.equals(INSIDE_BLOCK_COMMENT)) {  // Handle case: ...*/*
          previousChar = '\0'; 
          continue;
        }
        if (state.equals(FREE)) { // Can prefix still fit on the current line?
          if (_prefix.length() > lineLength - i) return false;
          else if (text.substring(i, i+_prefix.length()).equals(_prefix) && previousChar != '/') {
            // '/' is the only non-WS character that we consume without
            // immediately returning false. When we try to match the prefix,
            // we also need to reflect this implicit lookahead mechanism.
            return true;
          }
          else if (currentChar == '/') {
            if (previousChar == '/') return false;
          }
          else if (currentChar == ' ' || currentChar == '\t') {  }
          else if (!(currentChar == '*' && previousChar == '/')) return false;
        }
        if (previousChar == '/' && currentChar != '*') return false;
        previousChar = currentChar;
      }
      return false;
    }
    catch (BadLocationException e) {
      // Control flow should never reach this point!
      throw new UnexpectedException(new RuntimeException("Bug in QuestionCurrLineStartsWithSkipComments"));
    }
  }
}
