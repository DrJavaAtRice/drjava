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

/** This class represents the array allocation nodes of the syntax tree
  * @author  Stephane Hillion
  * @version 1.0 - 1999/04/25
  */

public class ArrayAllocation extends PrimaryExpression {
  /** The creationType */
  private TypeName elementType;
  
  /** The type descriptor */
  private TypeDescriptor typeDescriptor;
  
  /** Initializes the expression
    * @param tp    the type prefix
    * @param td    the type descriptor
    * @exception IllegalArgumentException if tp is null or td is null
    */
  public ArrayAllocation(TypeName tp, TypeDescriptor td) { this(tp, td, SourceInfo.NONE); }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param td    the type descriptor.  The element type of the enclosed initializer will
   *              be automatically and recursively set.
   * @exception IllegalArgumentException if tp is null or td is null
   */
  public ArrayAllocation(TypeName tp, TypeDescriptor td,
                         SourceInfo si) {
    super(si);
    
    if (tp == null) throw new IllegalArgumentException("tp == null");
    if (td == null) throw new IllegalArgumentException("td == null");
    elementType = tp;
    typeDescriptor = td;
    td.initialize(tp);
  }
  
  /**
   * Returns the creation type
   */
  public TypeName getElementType() {
    return elementType;
  }
  
  /**
   * Sets the creation type
   * @exception IllegalArgumentException if t is null
   */
  public void setElementType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    elementType = t;
  }

  /**
   * Returns the dimension of the array
   */
  public int getDimension() { return typeDescriptor.dimension; }
  
  /**
   * Note: This method <em>doesn't</em> follow the usual convention of
   * firing a property change.  If that functionality is needed, the code should
   * be fixed.
   */
  public void setDimension(int dim) { typeDescriptor.dimension = dim; }
  
  /** Returns the size expressions */
  public List<Expression> getSizes() {
    return typeDescriptor.sizes;
  }
  
  /**
   * Note: This method <em>doesn't</em> follow the usual convention of
   * firing a property change.  If that functionality is needed, the code should
   * be fixed.
   */
  public void setSizes(List<? extends Expression> sz) { 
    typeDescriptor.sizes = (sz == null) ? null : new ArrayList<Expression>(sz);
  }

  /**
   * Returns the initialization expression
   */
  public ArrayInitializer getInitialization() {
    return typeDescriptor.initialization;
  }
  
  /**
   * Note: This method <em>doesn't</em> follow the usual convention of
   * firing a property change.  If that functionality is needed, the code should
   * be fixed.
   * 
   * @param init  An initializer, assumed to already be set up with a valid element type.
   *              (The ArrayInitializer constructor will set up the element type automatically,
   *               but this method does not.)
   */
  public void setInitialization(ArrayInitializer init) { typeDescriptor.initialization = init; }

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
    return "(" + getClass().getName() + ": " + getElementType() + " " + getDimension() + " " + getSizes() + ")";
  }
  
  /**
   * This class contains informations about the array to create
   */
  public static class TypeDescriptor implements SourceInfo.Wrapper {
    /** The array dimension sizes */
    List<Expression> sizes;
    
    /** The array dimension */
    int dimension;
    
    /** The initialization expression */
    ArrayInitializer initialization;
    
    SourceInfo sourceInfo;
    
    /** Creates a new type descriptor */
    public TypeDescriptor(List<? extends Expression> sizes, int dim, ArrayInitializer init, SourceInfo si) {
      this.sizes     = (sizes == null) ? null : new ArrayList<Expression>(sizes);
      dimension      = dim;
      initialization = init;
      sourceInfo     = si;
    }
    
    public SourceInfo getSourceInfo() { return sourceInfo; }
    
    /** Initializes the type descriptor */
    void initialize(TypeName t) {
      if (initialization != null) {
        TypeName et;
        if (dimension > 1)
          et = new ArrayTypeName(t, dimension-1, false, SourceInfo.span(t, this));
        else
          et = t; 

        initialization.setElementType(et);
      }
    }
  }
}
