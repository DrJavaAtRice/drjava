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
  public final static String TARGET_TYPE = "targetType";
  
  /**
   * The target type
   */
  private TypeName targetType;
  
  /**
   * Initializes the expression
   * @param tt    the target type
   * @param exp   the casted expression
   * @exception IllegalArgumentException if tt is null or exp is null
   */
  public CastExpression(TypeName tt, Expression exp) {
    this(tt, exp, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param tt    the target type
   * @param exp   the casted expression
   * @exception IllegalArgumentException if tt is null or exp is null
   */
  public CastExpression(TypeName tt, Expression exp,
                        SourceInfo si) {
    super(exp, si);
    
    //if (tt == null) throw new IllegalArgumentException("tt == null");
    // tt can be null in a generated cast
    targetType = tt;
  }
  
  /**
   * Returns the target type
   */
  public TypeName getTargetType() {
    return targetType;
  }
  
  /**
   * Sets the target type
   * @exception IllegalArgumentException if t is null
   */
  public void setTargetType(TypeName t) {
    //if (t == null) throw new IllegalArgumentException("t == null");
    targetType = t;
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
