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

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the constructor call nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class ConstructorInvocation extends    PrimaryExpression
  implements ExpressionContainer {
  /**
   * The arguments property name
   */
  public final static String ARGUMENTS = "arguments";
  
  /**
   * The super property name
   */
  public final static String SUPER = "super";
  
  /**
   * The prefix expression
   */
  private Expression expression;
  
  /**
   * The arguments
   */
  private List<Expression> arguments;
  
  /**
   * Whether this invocation is 'super' or 'this'
   */
  private boolean superCall;
  
  /**
   * Creates a new node
   * @param exp  the prefix expression
   * @param args the arguments. null if there are no argument.
   * @param sup  whether this invocation is 'super' or 'this'
   */
  public ConstructorInvocation(Expression exp, List<Expression> args, boolean sup) {
    this(exp, args, sup, null, 0, 0, 0, 0);
  }
  
  /**
   * Creates a new node
   * @param exp  the prefix expression
   * @param args the arguments. null if there are no argument.
   * @param sup  whether this invocation is 'super' or 'this'
   * @param fn   the filename
   * @param bl   the begin line
   * @param bc   the begin column
   * @param el   the end line
   * @param ec   the end column
   */
  public ConstructorInvocation(Expression exp, List<Expression> args, boolean sup,
                               String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    expression = exp;
    arguments  = args;
    superCall  = sup;
  }
  
  /**
   * Returns the prefix expression if one, or null otherwise
   */
  public Expression getExpression() {
    return expression;
  }
  
  /**
   * Sets the prefix expression
   */
  public void setExpression(Expression e) {
    firePropertyChange(EXPRESSION, expression, expression = e);
  }
  
  /**
   * Returns the arguments
   */
  public List<Expression> getArguments() {
    return arguments;
  }
  
  /**
   * Sets the arguments
   */
  public void setArguments(List<Expression> l) {
    firePropertyChange(ARGUMENTS, arguments, arguments = l);
  }
  
  /**
   * Returns true is this invocation is a 'super' or a 'this' invocation
   */
  public boolean isSuper() {
    return superCall;
  }
  
  /**
   * Sets the super property
   */
  public void setSuper(boolean b) {
    firePropertyChange(SUPER, superCall, superCall = b);
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
   /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+getExpression()+" "+getArguments()+" "+isSuper()+")";
  }
}
