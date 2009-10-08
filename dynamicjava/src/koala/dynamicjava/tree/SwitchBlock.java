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
 * This class represents the switch expression-statement bindings
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/06/30
 */

public class SwitchBlock  extends Node implements ExpressionContainer {
  /**
   * The expression; null for a {@code default} block
   */
  private Expression expression;
  
  /**
   * The statements
   */
  private List<Node> statements;
  
  /**
   * Creates a new binding
   */
  public SwitchBlock(Expression exp, List<Node> stmts) {
    this(exp, stmts, SourceInfo.NONE);
  }
  
  /**
   * Creates a new binding
   * @param exp   the case expression, or {@code null} for a {@code default} block
   * @param stmts the body
   */
  public SwitchBlock(Expression exp, List<Node> stmts,
                     SourceInfo si) {
    super(si);
    
    expression = exp;
    statements = stmts;
  }
  
  /**
   * Returns the 'case' expression
   */
  public Expression getExpression() {
    return expression;
  }
  
  /**
   * Sets the 'case' expression
   */
  public void setExpression(Expression e) {
    expression = e;
  }
  
  /**
   * Returns the statements
   */
  public List<Node> getStatements() {
    return statements;
  }
  
  /**
   * Sets the statements
   */
  public void setStatements(List<Node> l) {
    statements = l;
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
    return "("+getClass().getName()+": "+getExpression()+" "+getStatements()+")";
  }
}
