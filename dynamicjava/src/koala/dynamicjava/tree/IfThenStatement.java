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
 * This class represents the if-then statement nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/16
 */

public class IfThenStatement extends Statement {
  /**
   * The condition
   */
  private Expression condition;
  
  /**
   * The then-statement of this statement
   */
  private Node thenStatement;
  
  /**
   * Creates a new while statement
   * @param cond  the condition
   * @param tstmt the statement
   * @exception IllegalArgumentException if cond is null or tstmt is null
   */
  public IfThenStatement(Expression cond, Node tstmt) {
    this(cond, tstmt, SourceInfo.NONE);
  }
  
  /**
   * Creates a new while statement
   * @param cond  the condition
   * @param tstmt the statement
   * @exception IllegalArgumentException if cond is null or tstmt is null
   */
  public IfThenStatement(Expression cond, Node tstmt,
                         SourceInfo si) {
    super(si);
    
    if (cond == null)  throw new IllegalArgumentException("cond == null");
    if (tstmt == null) throw new IllegalArgumentException("tstmt == null");
    
    condition     = cond;
    thenStatement = tstmt;
  }
  
  /**
   * Gets the condition to evaluate at each loop
   */
  public Expression getCondition() {
    return condition;
  }
  
  /**
   * Sets the condition to evaluate
   * @exception IllegalArgumentException if e is null
   */
  public void setCondition(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    condition = e;
  }
  
  /**
   * Returns the then statement of this statement
   */
  public Node getThenStatement() {
    return thenStatement;
  }
  
  /**
   * Sets the then statement of this statement
   * @exception IllegalArgumentException if node is null
   */
  public void setThenStatement(Node node) {
    if (node == null) throw new IllegalArgumentException("node == null");
    thenStatement = node;
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
    return "("+getClass().getName()+": "+getCondition()+" "+getThenStatement()+")";
  }
}
