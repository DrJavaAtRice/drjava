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
 * This class represents method declarations in an AST
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/11
 */

public class MethodDeclaration extends Declaration {
  /**
   * The return type of this method
   */
  private TypeName returnType;

  /**
   * The name of this method
   */
  private String name;

  /**
   * The parameters
   */
  private List<FormalParameter> parameters;

  /**
   * The exceptions
   */
  private List<? extends ReferenceTypeName> exceptions;

  /**
   * The body of the method
   */
  private BlockStatement body;

  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param type    the return type of this method
   * @param name    the name of the method to declare
   * @param params  the parameters list
   * @param excepts the exception list
   * @param body    the body statement
   * @exception IllegalArgumentException if name is null or type is null or
   *            params is null or excepts is null
   */
  public MethodDeclaration(ModifierSet mods, TypeName type, String name,
                           List<FormalParameter> params, List<? extends ReferenceTypeName> excepts, BlockStatement body) {
    this(mods, type, name, params, excepts, body, SourceInfo.NONE);
  }

  /**
   * Creates a new method declaration
   * @param mods    the modifiers
   * @param type    the return type of this method
   * @param name    the name of the method to declare
   * @param params  the parameters list
   * @param excepts the exception list
   * @param body    the body statement
   * @exception IllegalArgumentException if name is null or type is null or
   *            params is null or excepts is null
   */
  public MethodDeclaration(ModifierSet mods, TypeName type, String name,
                           List<FormalParameter> params, List<? extends ReferenceTypeName> excepts, BlockStatement body,
                           SourceInfo si) {
    super(mods, si);

    if (type == null)    throw new IllegalArgumentException("type == null");
    if (name == null)    throw new IllegalArgumentException("name == null");
    if (params == null)  throw new IllegalArgumentException("params == null");
    if (excepts == null) throw new IllegalArgumentException("excepts == null");

    returnType  = type;
    this.name   = name;
    parameters  = params;
    this.body   = body;
    exceptions = excepts;
  }

  /**
   * Gets the return type of this method
   */
  public TypeName getReturnType() {
    return returnType;
  }

  /**
   * Sets the return type of this method
   * @exception IllegalArgumentException if t is null
   */
  public void setReturnType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    returnType = t;
  }

  /**
   * Returns the name of this method
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the method's name
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
   * Sets the parameters list
   * @exception IllegalArgumentException if l is null
   */
  public void setParameters(List<FormalParameter> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
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
   * Sets the exceptions list
   * @exception IllegalArgumentException if l is null
   */
  public void setExceptions(List<? extends ReferenceTypeName> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    exceptions = l;
  }

  /**
   * Returns the body of the method, null if the method is abstract
   */
  public BlockStatement getBody() {
    return body;
  }

  /**
   * Sets the body
   */
  public void setBody(BlockStatement bs) {
    body = bs;
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
 return getModifiers()+" "+getReturnType()+" "+getName()+" "+getParameters()+" "+getExceptions()+" "+getBody();
  }
}
