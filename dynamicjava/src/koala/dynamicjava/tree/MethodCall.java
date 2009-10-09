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

import edu.rice.cs.plt.tuple.Option;

/**
 * An abstract parent for all method calls.  Concrete implementations include:<ul>
 * <li>{@link SimpleMethodCall}</li>
 * <li>{@link ObjectMethodCall}</li>
 * <li>{@link StaticMethodCall}</li>
 * <li>{@link SuperMethodCall}</li>
 * </ul>
 */

public abstract class MethodCall extends PrimaryExpression
  implements StatementExpression {
  
  private Option<List<TypeName>> typeArgs;
  private String methodName;
  private List<Expression> arguments;
  
  /**
   * Creates a new node
   * @param mn    the field name
   * @param args  the arguments. null if no arguments.
   * @exception IllegalArgumentException if mn is null
   */
  protected MethodCall(Option<List<TypeName>> targs, String mn, List<? extends Expression> args,
                       SourceInfo si) {
    super(si);
    if (mn == null || targs == null) throw new IllegalArgumentException();
    typeArgs = targs;
    methodName = mn;
    arguments  = (args == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(args);
  }
  
  public Option<List<TypeName>> getTypeArgs() { return typeArgs; }
  public void setTypeArgs(List<TypeName> targs) { typeArgs = Option.wrap(targs); }
  public void setTypeArgs(Option<List<TypeName>> targs) {
    if (targs == null) throw new IllegalArgumentException();
    typeArgs = targs;
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
    methodName = s;
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
  public void setArguments(List<? extends Expression> l) {
    arguments = (l == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(l);
  }
}
