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

import edu.rice.cs.plt.tuple.Option;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the regular allocation nodes of the syntax tree.
 * Nodes are derived from syntax like "new Foo(x, y+3, z.method())"
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class SimpleAllocation extends PrimaryExpression implements StatementExpression {
  private Option<List<TypeName>> typeArgs;
  private ReferenceTypeName creationType;
  private List<Expression> arguments;
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(Option<List<TypeName>> targs, ReferenceTypeName tp, List<? extends Expression> args) {
    this(targs, tp, args, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(ReferenceTypeName tp, List<? extends Expression> args) {
    this(Option.<List<TypeName>>none(), tp, args, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(ReferenceTypeName tp, List<? extends Expression> args, SourceInfo si) {
    this(Option.<List<TypeName>>none(), tp, args, si);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor
   * @exception IllegalArgumentException if tp is null
   */
  public SimpleAllocation(Option<List<TypeName>> targs, ReferenceTypeName tp, List<? extends Expression> args,
                           SourceInfo si) {
    super(si);
    if (tp == null || targs == null) throw new IllegalArgumentException();
    typeArgs = targs;
    creationType = tp;
    arguments = (args == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(args);
  }
  
  public Option<List<TypeName>> getTypeArgs() { return typeArgs; }
  public void setTypeArgs(List<TypeName> targs) { typeArgs = Option.wrap(targs); }
  public void setTypeArgs(Option<List<TypeName>> targs) {
    if (targs == null) throw new IllegalArgumentException();
    typeArgs = targs;
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
    creationType = t;
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
    arguments = (l == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(l);
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
    return "("+getTypeArgs()+" "+getClass().getName()+": "+getCreationType()+" "+getArguments()+")";
  }
}
