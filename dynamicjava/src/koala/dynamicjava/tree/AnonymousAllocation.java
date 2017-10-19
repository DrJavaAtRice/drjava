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
 * This class represents the anonymous class allocation nodes of the syntax tree.
 * Nodes are derived from syntax like "new Foo(x, y+3, z.method()) { void bar() {} }"
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class AnonymousAllocation extends SimpleAllocation implements StatementExpression {
  /**
   * The members of the anonymous class
   */
  private List<Node> members;
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor. Can be null.
   * @param memb  the members of the class
   * @exception IllegalArgumentException if tp is null or memb is null
   */
  public AnonymousAllocation(ReferenceTypeName tp, List<? extends Expression> args, List<Node> memb) {
    this(Option.<List<TypeName>>none(), tp, args, memb, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor. Can be null.
   * @param memb  the members of the class
   * @exception IllegalArgumentException if tp is null or memb is null
   */
  public AnonymousAllocation(Option<List<TypeName>> targs, ReferenceTypeName tp, List<? extends Expression> args,
                              List<Node> memb) {
    this(targs, tp, args, memb, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor. null if no arguments.
   * @param memb  the members of the class
   * @exception IllegalArgumentException if tp is null or memb is null
   */
  public AnonymousAllocation(ReferenceTypeName tp, List<? extends Expression> args, List<Node> memb, SourceInfo si) {
    this(Option.<List<TypeName>>none(), tp, args, memb, si);
  }
  
  /**
   * Initializes the expression
   * @param tp    the type prefix
   * @param args  the arguments of the constructor. null if no arguments.
   * @param memb  the members of the class
   * @exception IllegalArgumentException if tp is null or memb is null
   */
  public AnonymousAllocation(Option<List<TypeName>> targs, ReferenceTypeName tp, List<? extends Expression> args,
                              List<Node> memb, SourceInfo si) {
    super(targs, tp, args, si);
    if (memb == null) throw new IllegalArgumentException("memb == null");
    members = memb;
  }
  
  /**
   * Returns the members of the anonymous class
   */
  public List<Node> getMembers() {
    return members;
  }
  
  /**
   * Sets the members of the anonymous class
   * @exception IllegalArgumentException if t is null
   */
  public void setMembers(List<Node> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    members = l;
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
    return "("+getTypeArgs()+" "+getClass().getName()+": "+getCreationType()+" "+getArguments()+" "+getMembers()+")";
  }
}
