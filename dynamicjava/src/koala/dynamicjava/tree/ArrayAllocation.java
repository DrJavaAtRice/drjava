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
 * This class represents the array allocation nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class ArrayAllocation extends Allocation {
  /**
   * The type descriptor
   */
  private TypeDescriptor typeDescriptor;
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param td    the type descriptor
   * @exception IllegalArgumentException if tp is null or td is null
   */
  public ArrayAllocation(Type tp, TypeDescriptor td) {
    this(tp, td, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param td    the type descriptor
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if tp is null or td is null
   */
  public ArrayAllocation(Type tp, TypeDescriptor td,
                         String fn, int bl, int bc, int el, int ec) {
    super(tp, fn, bl, bc, el, ec);
    
    if (td == null) throw new IllegalArgumentException("td == null");
    
    typeDescriptor = td;
    td.initialize(tp);
  }
  
  /**
   * Returns the dimension of the array
   */
  public int getDimension() {
    return typeDescriptor.dimension;
  }
  
  /**
   * Returns the size expressions
   */
  public List<Expression> getSizes() {
    return typeDescriptor.sizes;
  }
  
  /**
   * Returns the initialization expression
   */
  public ArrayInitializer getInitialization() {
    return typeDescriptor.initialization;
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
    return "("+getClass().getName()+": "+getCreationType()+" "+getDimension()+" "+getSizes()+")";
  }
  
  /**
   * This class contains informations about the array to create
   */
  public static class TypeDescriptor {
    /**
     * The array dimension sizes
     */
    List<Expression> sizes;
    
    /**
     * The array dimension
     */
    int dimension;
    
    /**
     * The initialization expression
     */
    ArrayInitializer initialization;
    
    /**
     * The end line
     */
    public int endLine;
    
    /**
     * The end column
     */
    public int endColumn;
    
    /**
     * Creates a new type descriptor
     */
    public TypeDescriptor(List<Expression> sizes, int dim,
                          ArrayInitializer init, int el, int ec) {
      this.sizes     = sizes;
      dimension      = dim;
      initialization = init;
      endLine        = el;
      endColumn      = ec;
    }
    
    /**
     * Initializes the type descriptor
     */
    void initialize(Type t) {
      if (initialization != null) {
        Type et = (dimension > 1)
          ? new ArrayType(t, dimension - 1,
                          t.getFilename(),
                          t.getBeginLine(), t.getBeginColumn(),
                          endLine, endColumn)
          : t;
        initialization.setElementType(et);
      }
    }
  }
}
