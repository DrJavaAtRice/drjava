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
 * This class represents the array initializer nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class ArrayInitializer extends Expression {
  /**
   * The cells property name
   */
  public final static String CELLS = "cells";
  
  /**
   * The element type property name
   */
  public final static String ELEMENT_TYPE = "elementType";
  
  /**
   * The list of initialized cells
   */
  private List<Expression> cells;
  
  /**
   * The element type
   */
  private TypeName elementType;
  
  /**
   * Initializes the expression
   * @param cells the list of initialized cells
   * @exception IllegalArgumentException if cells is null
   */
  public ArrayInitializer(List<Expression> cells) {
    this(cells, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param cells the list of initialized cells
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if cells is null
   */
  public ArrayInitializer(List<Expression> cells,
                          String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (cells == null) throw new IllegalArgumentException("cells == null");
    
    this.cells = cells;
  }
  
  /**
   * Returns the list of cell initialization expressions
   */
  public List<Expression> getCells() {
    return cells;
  }
  
  /**
   * Sets the list of cell initialization expressions
   * @exception IllegalArgumentException if l is null
   */
  public void setCells(List<Expression> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    
    firePropertyChange(CELLS, cells, cells = l);
  }
  
  /**
   * Returns the element type
   * @exception IllegalStateException if elementType is null
   */
  public TypeName getElementType() {
    if (elementType == null) throw new IllegalStateException("elementType == null");
    
    return elementType;
  }
  
  /**
   * Sets the element type
   * @exception IllegalArgumentException if t is null
   */
  public void setElementType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    
    firePropertyChange(ELEMENT_TYPE, elementType, elementType = t);
    if (t instanceof ArrayTypeName) {
      ArrayTypeName at = (ArrayTypeName)t;
      Iterator  it = cells.iterator();
      while (it.hasNext()) {
        Object init = it.next();
        if (init instanceof ArrayInitializer) {
          ((ArrayInitializer)init).setElementType(at.getElementType());
        }
      }
    }
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
    return "("+getClass().getName()+": "+getCells()+" "+getElementType()+")";
  }
}
