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

import koala.dynamicjava.tree.tiger.TypeParameter;
import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents constructor declarations in an AST
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/23
 */

public class ConstructorDeclaration extends Declaration {
  private Option<List<TypeParameter>> typeParams;
  private String name;
  private List<FormalParameter> parameters;
  private List<? extends ReferenceTypeName> exceptions;
  private ConstructorCall constructorInvocation; // may be null
  private List<Node> statements;

  private boolean varargs;

  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param name    the name of this constructor
   * @param params  the parameters list
   * @param excepts the exception list (a list of list of token)
   * @param eci     the explicit constructor invocation
   * @param stmts   the statements
   * @exception IllegalArgumentException if name is null or params is null or
   *            excepts is null or stmts is null
   */
  public ConstructorDeclaration(ModifierSet mods, String name,
                                List<FormalParameter> params, List<? extends ReferenceTypeName> excepts,
                                ConstructorCall eci, List<Node> stmts) {
    this(mods, Option.<List<TypeParameter>>none(), name, params, excepts, eci, stmts, SourceInfo.NONE);
  }

  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param tparams the type parameters
   * @param name    the name of this constructor
   * @param params  the parameters list
   * @param excepts the exception list (a list of list of token)
   * @param eci     the explicit constructor invocation
   * @param stmts   the statements
   * @exception IllegalArgumentException if name is null or params is null or
   *            excepts is null or stmts is null
   */
  public ConstructorDeclaration(ModifierSet mods, Option<List<TypeParameter>> tparams, String name,
                                List<FormalParameter> params, List<? extends ReferenceTypeName> excepts,
                                ConstructorCall eci, List<Node> stmts) {
    this(mods, tparams, name, params, excepts, eci, stmts, SourceInfo.NONE);
  }

  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param name    the name of this constructor
   * @param params  the parameters list
   * @param excepts the exception list (a list of list of token)
   * @param eci     the explicit constructor invocation
   * @param stmts   the statements
   * @exception IllegalArgumentException if name is null or params is null or
   *            excepts is null or stmts is null
   */
  public ConstructorDeclaration(ModifierSet mods, String name,
                                List<FormalParameter> params, List<? extends ReferenceTypeName> excepts,
                                ConstructorCall eci, List<Node> stmts,
                                SourceInfo si) {
    this(mods, Option.<List<TypeParameter>>none(), name, params, excepts, eci, stmts, si);
  }
  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param tparams the type parameters
   * @param name    the name of this constructor
   * @param params  the parameters list
   * @param excepts the exception list (a list of list of token)
   * @param eci     the explicit constructor invocation
   * @param stmts   the statements
   * @exception IllegalArgumentException if name is null or params is null or
   *            excepts is null or stmts is null
   */
  public ConstructorDeclaration(ModifierSet mods, Option<List<TypeParameter>> tparams, String name,
                                List<FormalParameter> params, List<? extends ReferenceTypeName> excepts,
                                ConstructorCall eci, List<Node> stmts,
                                SourceInfo si) {
    super(mods, si);

    if (tparams == null || name == null || params == null || excepts == null || stmts == null) {
      throw new IllegalArgumentException();
    }
    typeParams            = tparams;
    this.name             = name;
    parameters            = params;
    constructorInvocation = eci;
    statements            = stmts;
    exceptions            = excepts;
  }

  public Option<List<TypeParameter>> getTypeParams() { return typeParams; }
  public void setTypeArgs(List<TypeParameter> tparams) { typeParams = Option.wrap(tparams); }
  public void setTypeArgs(Option<List<TypeParameter>> tparams) {
    if (tparams == null) throw new IllegalArgumentException();
    typeParams = tparams;
  }
  
  /**
   * Returns the name of this constructor
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the constructor's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    name = s;
  }

  /**
   * Returns the parameters list
   */
  public List<FormalParameter> getParameters() {
    return parameters;
  }

  /**
   * Sets the parameters
   */
  public void setParameters(List<FormalParameter> l) {
    parameters = l;
  }

  /**
   * Returns the list of the exception thrown by this method
   * @return a list of string
   */
  public List<? extends ReferenceTypeName> getExceptions() {
    return exceptions;
  }

  /**
   * Sets the exceptions thrown by this method
   * @exception IllegalArgumentException if l is null
   */
  public void setExceptions(List<? extends ReferenceTypeName> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    exceptions = l;
  }

  /**
   * The explicit constructor invocation if one or null
   */
  public ConstructorCall getConstructorCall() {
    return constructorInvocation;
  }

  /**
   * Sets the constructor invocation
   */
  public void setConstructorCall(ConstructorCall ci) {
    constructorInvocation = ci;
  }

  /**
   * Returns the statements
   */
  public List<Node> getStatements() {
    return statements;
  }

  /**
   * Sets the statements
   * @exception IllegalArgumentException if l is null
   */
  public void setStatements(List<Node> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    statements = l;
  }

  public boolean isVarArgs(){
    return varargs;
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
  
  public String toStringHelper() {
    return getModifiers()+" "+getName()+" "+getParameters()+" "+getExceptions()+" "+getConstructorCall()+" "+getStatements();
  }
}
