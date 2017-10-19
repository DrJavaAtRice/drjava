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

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the method call nodes of the syntax tree.
 * For example: "obj.foo(x, y+3)"
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class ObjectMethodCall extends MethodCall implements ExpressionContainer {
  /**
   * The expression on which this method call applies
   */
  private Expression expression;

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param targs type arguments
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   */
  public ObjectMethodCall(Expression exp, Option<List<TypeName>> targs, String mn,
                           List<? extends Expression> args, SourceInfo si) {
    super(targs, mn, args, si);
    if (exp == null) { throw new IllegalArgumentException("exp == null"); }
    expression = exp;
  }

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   */
  public ObjectMethodCall(Expression exp, String mn, List<? extends Expression> args, SourceInfo si) {
    this(exp, Option.<List<TypeName>>none(), mn, args, si);
  }

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param targs type arguments
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   */
  public ObjectMethodCall(Expression exp, Option<List<TypeName>> targs, String mn,
                           List<? extends Expression> args) {
    this(exp, targs, mn, args, SourceInfo.NONE);
  }

  /**
   * Creates a new node
   * @param exp   the expression on which this method call applies
   * @param mn    the field name
   * @param args  the arguments. Can be null.
   */
  public ObjectMethodCall(Expression exp, String mn, List<? extends Expression> args) {
    this(exp, Option.<List<TypeName>>none(), mn, args, SourceInfo.NONE);
  }

  /**
   * Returns the expression on which this method call applies
   */
  public Expression getExpression() {
    return expression;
  }

  /**
   * Sets the expression on which this method call applies
   */
  public void setExpression(Expression e) {
    if (e == null) { throw new IllegalArgumentException("e == null"); }
    expression = e;
  }

  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }

  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }

  /**
   * Implementation of toString for use in unit testing
   */
  public String toStringHelper() {
    return getTypeArgs()+" "+getMethodName()+" "+getArguments()+" "+getExpression();
  }
}
