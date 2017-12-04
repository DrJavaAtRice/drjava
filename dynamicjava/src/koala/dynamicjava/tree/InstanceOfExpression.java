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

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the instanceof expression nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class InstanceOfExpression extends Expression implements ExpressionContainer {
  /**
   * The expression to check
   */
  private Expression expression;
  
  /**
   * The type to check
   */
  private TypeName referenceType;
  
  /**
   * Initializes the expression
   * @param exp   the expression to test
   * @param t     the type to check
   * @exception IllegalArgumentException if exp is null or t is null
   */
  public InstanceOfExpression(Expression exp, TypeName t) {
    this(exp, t, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param exp   the expression to test
   * @param t     the type to check
   * @exception IllegalArgumentException if exp is null or t is null
   */
  public InstanceOfExpression(Expression exp, TypeName t,
                              SourceInfo si) {
    super(si);
    
    if (exp == null) throw new IllegalArgumentException("exp == null");
    if (t == null)   throw new IllegalArgumentException("t == null");
    
    expression    = exp;
    referenceType = t;
  }
  
  /**
   * Returns the expression to check
   */
  public Expression getExpression() {
    return expression;
  }
  
  /**
   * Sets the expression to check
   * @exception IllegalArgumentException if e is null
   */
  public void setExpression(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    expression = e;
  }
  
  /**
   * Returns the type to check
   */
  public TypeName getReferenceType() {
    return referenceType;
  }
  
  /**
   * Sets the type to check
   * @exception IllegalArgumentException if t is null
   */
  public void setReferenceType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    referenceType = t;
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
    return "("+getClass().getName()+": "+getExpression()+" "+getReferenceType()+")";
  }
}
