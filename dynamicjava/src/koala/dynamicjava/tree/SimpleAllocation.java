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
 * This class represents the regular allocation nodes of the syntax tree.
 * Nodes are derived from syntax like "new Foo(x, y+3, z.method())"
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class SimpleAllocation extends PrimaryExpression implements ExpressionStatement {

  /**
   * The creationType property name
   */
  public final static String CREATION_TYPE = "creationType";
  
  /**
   * The arguments property name
   */
  public final static String ARGUMENTS = "arguments";
  
  /**
   * The creationType
   */
  private ReferenceTypeName creationType;
  
  /**
   * The arguments to pass to the constructor
   */
  private List<Expression> arguments;
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(ReferenceTypeName tp, List<? extends Expression> args) {
    this(tp, args, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(ReferenceTypeName tp, List<? extends Expression> args,
                          String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);

    if (tp == null) throw new IllegalArgumentException("tp == null");
    creationType = tp;
    arguments = (args == null) ? null : new ArrayList<Expression>(args);
  }
  
  /**
   * Returns the creation type
   */
  public ReferenceTypeName getCreationType() {
    return creationType;
  }
  
  /**
   * Sets the creation type
   * @exception IllegalArgumentException if t is null
   */
  public void setCreationType(ReferenceTypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    
    firePropertyChange(CREATION_TYPE, creationType, creationType = t);
  }

  /**
   * Returns the constructor arguments
   */
  public List<Expression> getArguments() {
    return arguments;
  }
  
  /**
   * Sets the constructor arguments.
   */
  public void setArguments(List<? extends Expression> l) {
    firePropertyChange(ARGUMENTS, arguments, 
                       arguments = (l == null) ? null : new ArrayList<Expression>(l));
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
    return "("+getClass().getName()+": "+getCreationType()+" "+getArguments()+")";
  }
}
