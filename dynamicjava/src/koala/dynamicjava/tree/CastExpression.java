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
 * This class represents the cast expression nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class CastExpression extends UnaryExpression {
  /**
   * The targetType property name
   */
  public final static String TARGET_TYPE = "targetType";
  
  /**
   * The target type
   */
  private Type targetType;
  
  /**
   * Initializes the expression
   * @param tt    the target type
   * @param exp   the casted expression
   * @exception IllegalArgumentException if tt is null or exp is null
   */
  public CastExpression(Type tt, Expression exp) {
    this(tt, exp, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param tt    the target type
   * @param exp   the casted expression
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if tt is null or exp is null
   */
  public CastExpression(Type tt, Expression exp,
                        String fn, int bl, int bc, int el, int ec) {
    super(exp, fn, bl, bc, el, ec);
    
    if (tt == null) throw new IllegalArgumentException("tt == null");
    
    targetType = tt;
  }
  
  /**
   * Returns the target type
   */
  public Type getTargetType() {
    return targetType;
  }
  
  /**
   * Sets the target type
   * @exception IllegalArgumentException if t is null
   */
  public void setTargetType(Type t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    
    firePropertyChange(TARGET_TYPE, targetType, targetType = t);
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
    return "("+getClass().getName()+": "+getExpression()+" "+getTargetType()+")";
  }
}
