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
 * This class represents the binary expression nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class ConditionalExpression extends Expression {
  /**
   * The conditionExpression property name
   */
  public final static String CONDITION_EXPRESSION = "conditionExpression";
  
  /**
   * The ifTrueExpression property name
   */
  public final static String IF_TRUE_EXPRESSION = "ifTrueExpression";
  
  /**
   * The ifFalseExpression property name
   */
  public final static String IF_FALSE_EXPRESSION = "ifFalseExpression";
  
  /**
   * The condition expression
   */
  private Expression conditionExpression;
  
  /**
   * The if true expression
   */
  private Expression ifTrueExpression;
  
  /**
   * The if false expression
   */
  private Expression ifFalseExpression;
  
  /**
   * Initializes the expression
   * @param cexp  the condition expression
   * @param texp  the if true expression
   * @param fexp  the if false expression
   * @exception IllegalArgumentException if cexp is null or texp is null or
   *            fexp is null
   */
  public ConditionalExpression(Expression cexp, Expression texp, Expression fexp) {
    this(cexp, texp, fexp, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param cexp  the condition expression
   * @param texp  the if true expression
   * @param fexp  the if false expression
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if cexp is null or texp is null or
   *            fexp is null
   */
  public ConditionalExpression(Expression cexp, Expression texp, Expression fexp,
                               String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (cexp == null) throw new IllegalArgumentException("cexp == null");
    if (texp == null) throw new IllegalArgumentException("texp == null");
    if (fexp == null) throw new IllegalArgumentException("fexp == null");
    
    conditionExpression  = cexp;
    ifTrueExpression     = texp;
    ifFalseExpression    = fexp;
  }
  
  /**
   * Returns the condition expression
   */
  public Expression getConditionExpression() {
    return conditionExpression;
  }
  
  /**
   * Sets the condition expression
   * @exception IllegalArgumentException if e is null
   */
  public void setConditionExpression(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    
    firePropertyChange(CONDITION_EXPRESSION,
                       conditionExpression,
                       conditionExpression = e);
  }
  
  /**
   * Returns the if true expression
   */
  public Expression getIfTrueExpression() {
    return ifTrueExpression;
  }
  
  /**
   * Sets the if true expression
   * @exception IllegalArgumentException if e is null
   */
  public void setIfTrueExpression(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    
    firePropertyChange(IF_TRUE_EXPRESSION, ifTrueExpression, ifTrueExpression = e);
  }
  
  /**
   * Returns the if false expression
   */
  public Expression getIfFalseExpression() {
    return ifFalseExpression;
  }
  
  /**
   * Sets the if false expression
   * @exception IllegalArgumentException if e is null
   */
  public void setIfFalseExpression(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    
    firePropertyChange(IF_FALSE_EXPRESSION,
                       ifFalseExpression,
                       ifFalseExpression = e);
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
    return "("+getClass().getName()+": "+getConditionExpression()+" "+getIfTrueExpression()+" "+getIfFalseExpression()+")";
  }
}
