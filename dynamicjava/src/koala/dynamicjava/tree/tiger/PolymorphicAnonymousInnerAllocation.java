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

package koala.dynamicjava.tree.tiger;

import koala.dynamicjava.tree.*;

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

public class PolymorphicAnonymousInnerAllocation extends AnonymousInnerAllocation {
  /**
   * The type arguments of the anonymous class
   */
  private List<TypeName> _typeArguments;
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param args  the arguments of the constructor. Can be null.
   * @param memb  the members of the class
   * @param targs the type arguments of the class
   * @exception IllegalArgumentException if exp is null or memb is null or
   *            tp is null
   */
  public PolymorphicAnonymousInnerAllocation(Expression exp, String cn, List<? extends TypeName> ctargs,
                                             List<? extends Expression> args, List<Node> memb,
                                             List<TypeName> targs) {
    this(exp, cn, ctargs, args, memb, targs, null, 0, 0, 0, 0);
  }
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param args  the arguments of the constructor. Can be null.
   * @param memb  the members of the class
   * @param targs the type arguments of the class
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if exp is null or memb is null or
   *            tp is null
   */
  public PolymorphicAnonymousInnerAllocation(Expression exp, String cn, List<? extends TypeName> ctargs,
                                             List<? extends Expression> args, List<Node> memb, 
                                             List<TypeName> targs,
                                             String fn, int bl, int bc, int el, int ec) {
    super(exp, cn, ctargs, args, memb, fn, bl, bc, el, ec);
    _typeArguments = (targs == null) ? null : new ArrayList<TypeName>(targs);
  }
  
  /** Returns the type arguments of the anonymous class */
  public List<TypeName> getTypeArguments() { return _typeArguments; }
  
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
    return "("+getClass().getName()+": "+getClassName()+" "+getExpression()+" "+getArguments()+" "+getMembers()+" "+getTypeArguments()+")";
  }
}
