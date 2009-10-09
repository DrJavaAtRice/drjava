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
 * This class represents the *inner class* allocation nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class InnerAllocation extends PrimaryExpression implements StatementExpression, ExpressionContainer {
  private Expression expression;
  private Option<List<TypeName>> typeArgs;
  private String className;
  private Option<List<TypeName>> classTypeArgs;
  private List<Expression> arguments;
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param targs the constructor's type arguments
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or tp is null
   */
  public InnerAllocation(Expression exp, Option<List<TypeName>> targs, String cn,
                          Option<List<TypeName>> ctargs, List<? extends Expression> args) {
    this(exp, targs, cn, ctargs, args, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or tp is null
   */
  public InnerAllocation(Expression exp, String cn, Option<List<TypeName>> ctargs, List<? extends Expression> args) {
    this(exp, Option.<List<TypeName>>none(), cn, ctargs, args, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or cn is null
   */
  public InnerAllocation(Expression exp, String cn, Option<List<TypeName>> ctargs, List<? extends Expression> args,
                         SourceInfo si) {
    this(exp, Option.<List<TypeName>>none(), cn, ctargs, args, si);
  }
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param targs the constructor type arguments
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or cn is null
   */
  public InnerAllocation(Expression exp, Option<List<TypeName>> targs, String cn,
                         Option<List<TypeName>> ctargs, List<? extends Expression> args,
                         SourceInfo si) {
    super(si);
    if (targs == null || cn == null || ctargs == null || exp == null) throw new IllegalArgumentException();
    expression = exp;
    typeArgs = targs;
    className = cn;
    classTypeArgs = ctargs;
    arguments  = (args == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(args);
  }
  
  /**
   * Returns the outer class instance expression
   */
  public Expression getExpression() {
    return expression;
  }
  
  /**
   * Sets the outer class instance expression
   * @exception IllegalArgumentException if e is null
   */
  public void setExpression(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    expression = e;
  }
  
  public Option<List<TypeName>> getTypeArgs() { return typeArgs; }
  public void setTypeArgs(List<TypeName> targs) { typeArgs = Option.wrap(targs); }
  public void setTypeArgs(Option<List<TypeName>> targs) {
    if (targs == null) throw new IllegalArgumentException();
    typeArgs = targs;
  }
  
  /**
   * Returns the inner class name
   */
  public String getClassName() {
    return className;
  }
  
  /**
   * Sets the inner class name
   * @exception IllegalArgumentException if cn is null
   */
  public void setClassName(String cn) {
    if (cn == null) throw new IllegalArgumentException("cn == null");
    className = cn;
  }
  
  public Option<List<TypeName>> getClassTypeArgs() { return classTypeArgs; }
  public void setClassTypeArgs(List<TypeName> ctargs) { classTypeArgs = Option.wrap(ctargs); }
  public void setClassTypeArgs(Option<List<TypeName>> ctargs) {
    if (ctargs == null) throw new IllegalArgumentException();
    classTypeArgs = ctargs;
  }
  
  /**
   * Returns the constructor arguments.
   * @return null if there is no argument.
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
    return "("+getClass().getName()+": "+getTypeArgs()+" "+getClassName()+" "+getClassTypeArgs()+" "+
             getExpression()+" "+getArguments()+")";
  }
}
