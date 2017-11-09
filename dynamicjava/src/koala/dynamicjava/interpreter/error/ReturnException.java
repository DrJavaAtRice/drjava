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
 * This error is thrown by an interpreted throw statement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/05/26
 */

public class ReturnException extends ExecutionError {
  /**
   * Whether the return has a value
   * @serial
   */
  private boolean withValue;
  
  /**
   * The returned object
   * @serial
   */
  private Object value;
  
  /** A ReturnException with no value. */
  public ReturnException(String s, Node n) {
    super(s, n);
    withValue = false;
  }
  
  /** A ReturnException with a value. */
  public ReturnException(String s, Object o, Node n) {
    super(s, n);
    withValue = true;
    value = o;
  }
  
  /**
   * Returns the value returned
   */
  public Object getValue() {
    return value;
  }
  
  /**
   * Whether or not the return statement had a value
   */
  public boolean hasValue() {
    return withValue;
  }
}
