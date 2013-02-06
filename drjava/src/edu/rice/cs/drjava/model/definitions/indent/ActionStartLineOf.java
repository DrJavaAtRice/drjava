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

import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.drjava.model.AbstractDJDocument.ERROR_INDEX;

/** Indents the current line in the document to the indent level of the nearest preceding line starting with the 
  * specified prefix (ignoring comment-shadowed text).  Assumes prefix occurs before enclosing brace of current line.
  * @version $Id: ActionStartLineOfBracePlus.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ActionStartLineOf extends IndentRuleAction {
  private final String _prefix;
  private final int _length;
  private final char _lastChar;
  private final char[] _lastCharDelimArray;
  
  
  /** Constructs a new rule with the given prefix string.
    * @param prefix String that starts line to match
    */
  public ActionStartLineOf(String prefix) {
    super();
    if (prefix == null || prefix.equals("")) 
      throw new UnexpectedException("Attempted to construct ActionStartLineOf indenting rule with a degenerate prefix");
    _prefix = prefix;
    _length = prefix.length();
    _lastChar = prefix.charAt(_length - 1);
    _lastCharDelimArray = new char[] { _lastChar };
  }

  /** Properly indents the line that the caret is currently on.  Replaces all whitespace characters at the beginning of 
    * the line with the appropriate spacing or characters.   Only runs in event thread.
    * @param doc AbstractDJDocument containing the line to be indented.
    * @param reason The reason that the indentation is taking place
    * @return true if the caller should update the current location itself, false if the indenter has already handled it
    */
  public boolean indentLine(AbstractDJDocument doc, Indenter.IndentReason reason) {

    boolean supResult = super.indentLine(doc, reason); // This call does nothing other than record some indent tracing
    int orig = doc.getCurrentLocation();
    
//    System.err.println("In ActionStartLineOf, indenting line: '" + doc._getCurrentLine() + "'\n" + 
//                       "*** origPos = " + orig);
    
    // Get distance to enclosing brace
//    int lineStart = doc._getLineStartPos(orig);
//    if (lineStart < 0) lineStart = 0;
    
//    BraceInfo info = doc._getLineEnclosingBrace();
//    
    int startPos = doc._getLineStartPos(orig) - 1;  // starting position for searching backwards
    if (startPos <= 0) return supResult;
//    
//    int distToLineEnclosingBrace = info.distance();
////    System.err.println("dist to brace = " + distToLineEnclosingBrace);
//
//    int minPos; // beginning of earliest possible matching line
//    // If there is no enclosing brace, use start of preceding line
//    if (distToLineEnclosingBrace == -1) 
//      minPos = doc._getLineStartPos(startPos);
//    else minPos = doc._getLineEndPos(orig - distToLineEnclosingBrace) + 1;
    
    try {
      int minPos = doc.findEnclosingScalaBracePos(startPos);  // may throw a BadLocationException or return ERROR_INDEX
      
//      System.err.println("In search within ActionStartLineOf, start position is " + startPos + 
//                         " position of enclosing Scala brace is " + minPos);
//      System.err.println("Inteventing text is '" + doc._getText(minPos, startPos - minPos) + "'");
//      
//      System.err.println("Brace is '" + doc._getText(minPos, 1).charAt(0) + "'");
    
      // Starting with startPos, search backwards for an unshadowed match for _prefix
      int pos = startPos;
      int prefixStartPos = ERROR_INDEX;
      
//      System.err.println("Starting search for '" + _lastChar + "'");
                          
      while (pos >= minPos) {
        int matchPos = doc.findPrevDelimiter(pos, _lastCharDelimArray);
        if (matchPos == ERROR_INDEX) {
//          System.err.println("Matching char '" + _lastChar + "' NOT FOUND");
          break;
        }
        char matchChar = doc._getText(matchPos, 1).charAt(0);
        prefixStartPos = matchPos - _length + 1;
        if (prefixStartPos < minPos) /* do nothing */ return supResult;
        if (doc._getText(prefixStartPos, _length).equals(_prefix)) break;  
        pos = matchPos - 1;  // could be optimized prefixStartPos if lastChar does not occur elsewhere in prefix
      }
      
      if (prefixStartPos >= minPos) {
        final int indent = doc._getIndentOfLine(prefixStartPos);
//        System.err.println("Found a match for '" + _prefix + "' at pos " + prefixStartPos);
//        System.err.println("In ActionStartLineOf, indent = " + indent);
        doc.setTab(indent, orig);
      }
      else {
//        System.err.println("No match for '" + _prefix+ "' found");
      }
    }
    catch(BadLocationException ble) { /* do nothing */ }
    
    // do nothing if prefixStartPos < minPos
    return supResult;
  }
}
