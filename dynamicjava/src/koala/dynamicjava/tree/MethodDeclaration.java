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

public class MethodDeclaration extends Node {
  /**
   * The accessFlags property name
   */
  public final static String ACCESS_FLAGS = "accessFlags";

  /**
   * The type property name
   */
  public final static String RETURN_TYPE = "returnType";

  /**
   * The name property name
   */
  public final static String NAME = "name";

  /**
   * The parameters property name
   */
  public final static String PARAMETERS = "parameters";

  /**
   * The exceptions property name
   */
  public final static String EXCEPTIONS = "exceptions";

  /**
   * The body property name
   */
  public final static String BODY = "body";

  /**
   * The access flags
   */
  private int accessFlags;

  /**
   * The return type of this method
   */
  private Type returnType;

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
  private List<String> exceptions;

  /**
   * The body of the method
   */
  private BlockStatement body;

  /**
   * Creates a new method declaration
   * @param flags   the access flags
   * @param type    the return type of this method
   * @param name    the name of the method to declare
   * @param params  the parameters list
   * @param excepts the exception list
   * @param body    the body statement
   * @exception IllegalArgumentException if name is null or type is null or
   *            params is null or excepts is null
   */
  public MethodDeclaration(int flags, Type type, String name,
                           List<FormalParameter> params, List<? extends ReferenceType> excepts, BlockStatement body) {
    this(flags, type, name, params, excepts, body, null, 0, 0, 0, 0);
  }

  /**
   * Creates a new method declaration
   * @param flags   the access flags
   * @param type    the return type of this method
   * @param name    the name of the method to declare
   * @param params  the parameters list
   * @param excepts the exception list
   * @param body    the body statement
   * @param fn      the filename
   * @param bl      the begin line
   * @param bc      the begin column
   * @param el      the end line
   * @param ec      the end column
   * @exception IllegalArgumentException if name is null or type is null or
   *            params is null or excepts is null
   */
  public MethodDeclaration(int flags, Type type, String name,
                           List<FormalParameter> params, List<? extends ReferenceType> excepts, BlockStatement body,
                           String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);

    if (type == null)    throw new IllegalArgumentException("type == null");
    if (name == null)    throw new IllegalArgumentException("name == null");
    if (params == null)  throw new IllegalArgumentException("params == null");
    if (excepts == null) throw new IllegalArgumentException("excepts == null");

    accessFlags = flags;
    returnType  = type;
    this.name   = name;
    parameters  = params;
    this.body   = body;

    exceptions            = new LinkedList<String>();

    ListIterator<? extends ReferenceType> it = excepts.listIterator();
    while (it.hasNext()) {
      exceptions.add(it.next().getRepresentation());
    }
  }

  /**
   * Returns the access flags for this method
   */
  public int getAccessFlags() {
    return accessFlags;
  }

  /**
   * Sets the access flags for this constructor
   */
  public void setAccessFlags(int f) {
    firePropertyChange(ACCESS_FLAGS, accessFlags, accessFlags = f);
  }

  /**
   * Gets the return type of this method
   */
  public Type getReturnType() {
    return returnType;
  }

  /**
   * Sets the return type of this method
   * @exception IllegalArgumentException if t is null
   */
  public void setReturnType(Type t) {
    if (t == null) throw new IllegalArgumentException("t == null");

    firePropertyChange(RETURN_TYPE, returnType, returnType = t);
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

    firePropertyChange(NAME, name, name = s);
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

    firePropertyChange(PARAMETERS, parameters, parameters = l);
  }

  /**
   * Returns the list of the exception thrown by this method
   * @return a list of string
   */
  public List getExceptions() {
    return exceptions;
  }

  /**
   * Sets the exceptions list
   * @exception IllegalArgumentException if l is null
   */
  public void setExceptions(List<String> l) {
    if (l == null) throw new IllegalArgumentException("l == null");

    firePropertyChange(EXCEPTIONS, exceptions, exceptions = l);
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
    firePropertyChange(BODY, body, body = bs);
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
	return java.lang.reflect.Modifier.toString(getAccessFlags())+" "+getReturnType()+" "+getName()+" "+getParameters()+" "+getExceptions()+" "+getBody();
  }
}
