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

package koala.dynamicjava.interpreter;

import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.SourceInfo;

/**
 * This exception is thrown when an error append while
 * interpreting a statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/11/14
 */

public class InterpreterException extends ThrownException {
  /**
   * The source code information
   */
  protected SourceInfo sourceInfo;
  
  /**
   * The detailed message
   */
  protected String message;
  
  /**
   * Constructs an <code>InterpreterException</code> from a ParseError
   */
  public InterpreterException(ParseError e) {
    super(e);
    if (e.getLine() != -1) {
      //SourceInfo si = e.getSourceInfo();
      message = "L"+e.getLine()+", C"+e.getColumn()+" ("+e.getFilename()+"):\n"+/**/// use si
        e.getMessage(); 
    } else {
      message = e.getMessage();
    }
  }
  
  /**
   * Constructs an <code>InterpreterException</code> from a ExecutionError
   */
  public InterpreterException(ExecutionError e) {
    super(e);
    Node n = e.getNode();
    if (n != null && n.getFilename() != null) {
      SourceInfo si = n.getSourceInfo();
      message = "BL"+si.getStartLine()+", BC"+si.getStartColumn()+"EL"+si.getEndLine()+", EC"+si.getEndColumn()+
        " ("+si.getFile().getName()+"):\n";
    } else {
      message = "";
    }
    if (e instanceof CatchedExceptionError) {
      message += ((CatchedExceptionError)e).getException();
    } else if (e instanceof ThrownException) {
      message += ((ThrownException)e).getException();
    } else {
      message += e.getMessage();
    }
  }
  
  public Throwable getError() {
    return thrown;
  }
  
  /**
   * Returns the source code information if available, or null
   */
  public SourceInfo getSourceInfo() {
    return sourceInfo;
  }
  
  
  /**
   * Returns the detailed message
   */
  public String getMessage() {
    return message;
  }
}
