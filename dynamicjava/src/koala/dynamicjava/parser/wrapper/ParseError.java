/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.parser.wrapper;


import java.io.File;

import edu.rice.cs.plt.text.TextUtil;
import koala.dynamicjava.parser.impl.ParseException;
import koala.dynamicjava.parser.impl.Token;
import koala.dynamicjava.tree.SourceInfo;
  
/**
 * This error is thrown when an unexpected error append while
 * parsing a statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/03
 */

public class ParseError extends Error implements SourceInfo.Wrapper {
    private SourceInfo _si;
    
    
    /**
     * Constructs an <code>ExecutionError</code> with the specified 
     * detail message. 
     * @param s the detail message.
     */
    public ParseError(String s, SourceInfo si) {
      super(s);
      _si = si;
    }
    
    /**
     * Constructs a ParseError based on a ParseException.
     * @param e  the ParseException.
     * @param f  the source file, or {@code null} if it is unknown.
     */
    public ParseError(ParseException e, File f) {
      super(parseExceptionMessage(e), e);
      _si = parseExceptionLocation(e, f);
    }
    
    public ParseError(Throwable t, SourceInfo si) {
      super(t.getMessage(), t);
      _si = si;
    }
    
    public SourceInfo getSourceInfo() { return _si; }
    
    
    private static String parseExceptionMessage(ParseException e) {
      if (e.expectedTokenSequences == null) { return e.getMessage(); }
      else {
        int maxSize = 0;
        for (int i = 0; i < e.expectedTokenSequences.length; i++) {
          if (maxSize < e.expectedTokenSequences[i].length) {
            maxSize = e.expectedTokenSequences[i].length;
          }
        }
        String retval = "Syntax Error: \"";
        Token tok = e.currentToken.next;
        
        for (int i = 0; i < maxSize; i++) {
          if (i != 0) retval += " ";
          if (tok.kind == 0) {
            retval += e.tokenImage[0];
            break;
          }
          retval += TextUtil.javaEscape(tok.image);
          tok = tok.next; 
        }
        retval += "\"";
        return retval;
      }
    }
    
    private static SourceInfo parseExceptionLocation(ParseException e, File f) {
      Token t = e.currentToken;
      if (t == null) { return SourceInfo.point(f, 0, 0); }
      else {
        if (t.next != null) { t = t.next; }
        return SourceInfo.range(f, t.beginLine, t.beginColumn, t.endLine, t.endColumn);
      }
    }
    
}
