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

package koala.dynamicjava.parser.wrapper;

import koala.dynamicjava.parser.impl.ParseException;
import koala.dynamicjava.parser.impl.Token;

public class CustomParseException extends ParseException {
  
  private final String _message;
  private final boolean _customMessage;
  
  public CustomParseException(String message, Token currentTokenVal,
      int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
    super(currentTokenVal, expectedTokenSequencesVal, tokenImageVal);
    _message = message;
    _customMessage = true;
  }
  
  public CustomParseException(ParseException e) {
    super(e.currentToken, e.expectedTokenSequences, e.tokenImage);
    _message = e.getMessage();
    _customMessage = (e instanceof CustomParseException && ((CustomParseException)e)._customMessage) ||
                     e.currentToken == null;
  }
  
  public String getMessage() { return _message; }
  
  public String getShortMessage() {
    if (_customMessage) {
      return getMessage();
    }
    else {
      String expected = "";
      int maxSize = 0;
      for (int i = 0; i < expectedTokenSequences.length; i++) {
        if (maxSize < expectedTokenSequences[i].length) {
          maxSize = expectedTokenSequences[i].length;
        }
      }
      String retval = "Syntax Error: \"";
      Token tok = currentToken.next;
      
      for (int i = 0; i < maxSize; i++) {
        if (i != 0) retval += " ";
        if (tok.kind == 0) {
          retval += tokenImage[0];
          break;
        }
        retval += add_escapes(tok.image);
        tok = tok.next; 
      }
      // retval += "\" at line " + currentToken.next.beginLine + ", column " + currentToken.next.beginColumn + "." + eol;
      retval += "\"";
      return retval;
    }
  }
  
  /**
   * Returns starting line of syntax error.
   */
  public int getBeginLine() {
    if(currentToken.next!=null)
      return currentToken.next.beginLine;
    return currentToken.beginLine;
  }
  
  /**
   * Returns starting column of syntax error.
   */
  public int getBeginColumn() {
    if(currentToken.next!=null)
      return currentToken.next.beginColumn;
    return currentToken.beginColumn;
  }
  
  /**
   * Returns ending line of syntax error.
   */
  public int getEndLine() {
    if(currentToken.next!=null)
      return currentToken.next.endLine;
    return currentToken.endLine;
  }
  
  /**
   * Returns ending column of syntax error.
   */
  public int getEndColumn() {
    if(currentToken.next!=null)
      return currentToken.next.endColumn;
    return currentToken.endColumn;
  }
  
  public static CustomParseException makeCustom(ParseException e) {
    if (e instanceof CustomParseException) { return (CustomParseException) e; }
    else { return new CustomParseException(e); }
  }
  
}
