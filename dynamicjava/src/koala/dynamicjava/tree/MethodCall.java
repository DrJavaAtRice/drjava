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

package koala.dynamicjava.tree;

import java.util.*;

/**
 * This class represents the method call nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public abstract class MethodCall extends    PrimaryExpression
  implements ExpressionStatement {
  /**
   * The methodName property name
   */
  public final static String METHOD_NAME = "methodName";
  
  /**
   * The arguments property name
   */
  public final static String ARGUMENTS = "arguments";
  
  /**
   * The method name
   */
  private String methodName;
  
  /**
   * The arguments
   */
  private List<Expression> arguments;
  
  /**
   * Creates a new node
   * @param mn    the field name
   * @param args  the arguments. null if no arguments.
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if mn is null
   */
  protected MethodCall(String mn, List<Expression> args,
                       String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (mn == null) throw new IllegalArgumentException("mn == null");
    
    methodName = mn;
    arguments  = args;
  }
  
  /**
   * Returns the method name
   */
  public String getMethodName() {
    return methodName;
  }
  
  /**
   * Sets the method name
   * @exception IllegalArgumentException if s is null
   */
  public void setMethodName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    
    firePropertyChange(METHOD_NAME, methodName, methodName = s);
  }
  
  /**
   * Returns the arguments.
   * @return null if there is no argument
   */
  public List<Expression> getArguments() {
    return arguments;
  }
  
  /**
   * Sets the constructor arguments.
   */
  public void setArguments(List<Expression> l) {
    firePropertyChange(ARGUMENTS, arguments, arguments = l);
  }
}
