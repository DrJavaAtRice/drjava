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
 * This class represents the *inner class* allocation nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/25
 */

public class InnerAllocation extends PrimaryExpression implements StatementExpression, ExpressionContainer {

  /**
   * The className property name
   */
  public final static String CLASS_NAME = "className";
  
  /**
   * The classTypeArguments property name
   */
  public final static String CLASS_TYPE_ARGUMENTS = "classTypeArguments";
  
  /**
   * The arguments property name
   */
  public final static String ARGUMENTS = "arguments";
    
  /**
   * The outer object expression
   */
  private Expression expression;
  
  /**
   * The inner class name
   */
  private String className;
  
  /**
   * Type arguments to apply to the inner class, or null if none are provided
   */
  private List<TypeName> classTypeArguments;
  
  /**
   * The arguments to pass to the constructor; may be null if none are provided
   */
  private List<Expression> arguments;
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or tp is null
   */
  public InnerAllocation(Expression exp, String cn, List<? extends TypeName> ctargs, List<? extends Expression> args) {
    this(exp, cn, ctargs, args, SourceInfo.NONE);
  }
  
  /**
   * Initializes the expression
   * @param exp   the outer object
   * @param cn    the inner class name
   * @param ctargs the inner class's type arguments
   * @param args  the arguments of the constructor. null if no arguments.
   * @exception IllegalArgumentException if exp is null or cn is null
   */
  public InnerAllocation(Expression exp, String cn, List<? extends TypeName> ctargs, List<? extends Expression> args,
                         SourceInfo si) {
    super(si);
    
    if (cn == null) throw new IllegalArgumentException("cn == null");
    if (exp == null) throw new IllegalArgumentException("exp == null");
    
    expression = exp;
    className = cn;
    classTypeArguments = (ctargs == null) ? new ArrayList<TypeName>(0) : new ArrayList<TypeName>(ctargs);
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
    firePropertyChange(EXPRESSION, expression, expression = e);
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
    firePropertyChange(CLASS_NAME, className, className = cn);
  }
  
  /**
   * Returns the inner class type arguments.
   * @return null if there is no argument.
   */
  public List<TypeName> getClassTypeArguments() {
    return classTypeArguments;
  }
  
  /**
   * Sets the inner class type arguments.
   */
  public void setClassTypeArguments(List<TypeName> l) {
    firePropertyChange(CLASS_TYPE_ARGUMENTS, classTypeArguments,
                       (l == null) ? new ArrayList<TypeName>(0) : new ArrayList<TypeName>(l));
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
    firePropertyChange(ARGUMENTS, arguments, 
                       arguments = (l == null) ? new ArrayList<Expression>(0) : new ArrayList<Expression>(l));
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
    return "("+getClass().getName()+": "+getClassName()+" "+getExpression()+" "+getArguments()+")";
  }
}
