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

package koala.dynamicjava.interpreter.error;

import koala.dynamicjava.tree.*;

/**
 * An exception that is thrown by the interpreted program
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/26
 */

public class ThrownException extends ExecutionError {
  
  /**
   * Constructs an <code>ThrownExceptionError</code> with no detail message. 
   */
  public ThrownException(Throwable e) {
    super("uncaught.exception");
    thrown = e;
  }
  
  /**
   * Constructs an <code>ThrownExceptionError</code> with no detail message.
   * Filters the stack trace to remove the provided elements (in continuous order;
   * if they don't match or don't correspond to the last elements of e's stack trace, 
   * no filtering will occur)
   */
  public ThrownException(Throwable e, StackTraceElement[] filteredElements) {
    super("uncaught.exception");
    StackTraceElement[] original = e.getStackTrace();
    int startMatch = original.length - filteredElements.length;
    boolean matches = startMatch >= 0;
    for (int i = 0; matches && i < filteredElements.length; i++) {
      matches &= original[startMatch+i].equals(filteredElements[i]);
    }
    if (matches) {
      StackTraceElement[] newStackTrace = new StackTraceElement[startMatch];
      for (int i = 0; i < startMatch; i++) { newStackTrace[i] = original[i]; }
      e.setStackTrace(newStackTrace);
    }
    thrown = e;    
  }
  
  /**
   * Constructs an <code>ThrownExceptionError</code> with the specified 
   * detail message, filename, line, column and exception.
   * @param e  the thrown exception
   * @param n  the node in the syntax tree where the error occurs
   */
  public ThrownException(Throwable e, Node n) {
    super("uncaught.exception", n);
    thrown = e;
  }
  
  /**
   * Returns the exception that causes this error throwing
   */
  public Throwable getException() {
    return thrown;
  }
}
