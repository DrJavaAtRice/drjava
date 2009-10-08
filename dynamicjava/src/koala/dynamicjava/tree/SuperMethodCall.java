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
 * This class represents the super method call nodes of the syntax tree.
 * For example: "super.foo(x, y+3)"
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class SuperMethodCall extends MethodCall {
  /**
   * The class that qualify that object
   */
  private Option<String> className;
  
  public SuperMethodCall(Option<String> cn, String mn, List<? extends Expression> args, SourceInfo si) {
    super(mn, args, si);
    if (cn == null) throw new IllegalArgumentException("cn == null");
    className = cn;
  }
  
  public SuperMethodCall(Option<String> cn, String mn, List<? extends Expression> args) {
    this(cn, mn, args, SourceInfo.NONE);
  }
  
  /**
   * Returns the name of the class that qualify that object
   */
  public Option<String> getClassName() {
    return className;
  }
  
  /**
   * Sets the name of the class that qualifies that object
   * @exception IllegalArgumentException if s is null or body is null
   */
  public void setClassName(Option<String> cn) {
    if (cn == null) throw new IllegalArgumentException("cn == null");
    className = cn;
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
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }
    /**
   * Implementation of toString for use in unit testing
   */
  public String toStringHelper() {
    return getClassName()+" "+getMethodName()+" "+getArguments();
  }
}
