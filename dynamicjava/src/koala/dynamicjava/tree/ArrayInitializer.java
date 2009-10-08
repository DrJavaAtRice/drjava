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
  public ArrayInitializer(List<? extends Expression> cells) {
    this(cells, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param cells the list of initialized cells
   * @exception IllegalArgumentException if cells is null
   */
  public ArrayInitializer(List<? extends Expression> cells,
                          SourceInfo si) {
    super(si);
    
    if (cells == null) throw new IllegalArgumentException("cells == null");
    
    this.cells = new ArrayList<Expression>(cells);
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
  public void setCells(List<? extends Expression> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    cells = new ArrayList<Expression>(l);
  }
  
  /**
   * Returns the element type, or {@code null} if it's not set
   */
  public TypeName getElementType() {
    return elementType;
  }
  
  /**
   * Sets the element type.  Also recursively sets the element type of any
   * cells that are ArrayInitializers.
   * @exception IllegalArgumentException if t is null
   */
  public void setElementType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    elementType = t;
    if (t instanceof ArrayTypeName) {
      ArrayTypeName at = (ArrayTypeName)t;
      for (Expression init : cells) {
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
