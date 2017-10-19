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
 * This class represents the array type nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class ArrayTypeName extends TypeName {
  /**
   * The type of the elements of the arrays represented by this type
   */
  private TypeName elementType;
  
  private boolean vararg;
  
  /**
   * Initializes the type
   * @param et    the element type
   * @param dim   the dimension of the arrays represented by this type (> 0)
   * @exception IllegalArgumentException if et is null or dim < 1
   */
  public ArrayTypeName(TypeName et, int dim, boolean varg) {
    this(et, dim, varg, SourceInfo.NONE);
  }
  
  /**
   * Initializes the type
   * @param et    the element type
   * @param dim   the dimension of the arrays represented by this type (> 0)
   * @exception IllegalArgumentException if et is null or dim < 1
   */
  public ArrayTypeName(TypeName et, int dim, boolean varg, SourceInfo si) {
    super(si);
    
    if (et == null) throw new IllegalArgumentException("et == null");
    if (dim < 1)    throw new IllegalArgumentException("dim < 1");
    
    elementType = (dim > 1) ? new ArrayTypeName(et, dim - 1, false, si) : et;
    vararg = varg;
  }
  
  /**
   * Returns the type of the elements of the arrays represented by this type
   */
  public TypeName getElementType() {
    return elementType;
  }
  
  /**
   * Sets the type of the elements of the arrays represented by this type
   * @exception IllegalArgumentException if t is null
   */
  public void setElementType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    elementType = t;
  }
  
  public boolean isVararg() { return vararg; }
  
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
    return "("+getClass().getName()+": "+getElementType()+")";
  }
}
