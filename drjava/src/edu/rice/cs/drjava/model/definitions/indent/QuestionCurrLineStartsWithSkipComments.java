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

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** Determines whether the current line in the document starts with a specific character sequence, skipping over any 
  * comments and leading whitespace on that line. The character sequence is passed to the constructor of the class as
  * a String argument.
  * @version $Id$
  */
public class QuestionCurrLineStartsWithSkipComments extends IndentRuleQuestion {
  /** The String to be matched. This String may not contain whitespace characters or comment-delimiting characters. */
  private String _prefix;
  
  /** 
   * @param prefix the String to be matched
   * @param yesRule The decision subtree for the case that this rule applies 
   *                in the current context.
   * @param noRule  The decision subtree for the case that this rule does not 
   *                apply in the current context.
   */
  public QuestionCurrLineStartsWithSkipComments(String prefix, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /** Determines whether or not the current line in the document starts with the character sequence specified by the
    * String field _prefix, skipping over any comments and leading whitespace on that line.  Will not match prefixes
    * that begin with "//" or "/*" or whitespace.  Will not match empty string unless line has some uncommented nonWS
    * text.  Assumes that write lock and reduced lock are already held.
    * @param doc The AbstractDJDocument containing the current line.
    * @return True iff the current line in the document starts with the
    * character sequence specified by the String field _prefix.
    */
  boolean applyRule(AbstractDJDocument doc, Indenter.IndentReason reason) {
    // Find the first non-whitespace character on the current line.
    
    int origPos = doc.getCurrentLocation();
    int startPos   = doc._getLineFirstCharPos(origPos);
    int endPos     = doc._getLineEndPos(origPos);
    int lineLength = endPos - startPos;
    
    char prevChar = '\0';
    String text = doc._getText(startPos, lineLength);
//      System.err.println("line is: '" + text + "'");
    
    doc.setCurrentLocation(startPos);
    try { 
      for (int i = 0; i < lineLength; i++, doc.move(1)) {
        
        ReducedModelState state = doc.getStateAtCurrent();
        
        if (state.equals(INSIDE_BLOCK_COMMENT)) {  // Handle case: ...*/*
          assert prevChar == '\0'; 
          continue;
        }
        char currentChar = text.charAt(i);
//          System.err.println("Iteration " + i + ": ch = " + currentChar + " prevCh = " + prevChar);
        
        if (currentChar == '/') {
          if (prevChar == '/') return false;  // opened a LINE_COMMENT
          if (prevChar == '\0') {
            prevChar = currentChar;
            continue;     // leading char in line is '/'
          }
        }
        else if (currentChar == '*' && prevChar == '/') { // opened a BLOCK_COMMENT, subsequent chars will be inside
          prevChar = '\0';
          continue;      
        }
        else if (currentChar == ' ' || currentChar == '\t') {  
          if (prevChar == '\0') {
            continue;  // consume opening whitespace
          }
        }
        return text.startsWith(_prefix, i);   // special cases have already been eliminated
      }
    }
    finally { doc.setCurrentLocation(origPos); }
    return false;
  }
}
