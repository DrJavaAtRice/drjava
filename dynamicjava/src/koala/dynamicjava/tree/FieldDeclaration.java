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

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents field declarations in an AST
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/11
 */

public class FieldDeclaration extends Node {
  /**
   * The accessFlags property name
   */
  public final static String ACCESS_FLAGS = "accessFlags";
  
  /**
   * The type property name
   */
  public final static String TYPE = "type";
  
  /**
   * The name property name
   */
  public final static String NAME = "name";
  
  /**
   * The initializer property name
   */
  public final static String INITIALIZER = "initializer";
  
  /**
   * The access flags
   */
  private int accessFlags;
  
  /**
   * The type of this field
   */
  private Type type;
  
  /**
   * The name of this field
   */
  private String name;
  
  /**
   * The initializer
   */
  private Expression initializer;
  
  /**
   * Creates a new field declaration
   * @param flags  the access flags
   * @param type   the type of this field
   * @param name   the name of this field
   * @param init   the initializer. Can be null
   * @exception IllegalArgumentException if name is null or type is null
   */
  public FieldDeclaration(int flags, Type type, String name, Expression init) {
    this(flags, type, name, init, null, 0, 0 ,0 ,0);
  }
  
  /**
   * Creates a new field declaration
   * @param flags  the access flags
   * @param type   the type of this field
   * @param name   the name of this field
   * @param init   the initializer. Can be null
   * @param fn     the filename
   * @param bl     the begin line
   * @param bc     the begin column
   * @param el     the end line
   * @param ec     the end column
   * @exception IllegalArgumentException if name is null or type is null
   */
  public FieldDeclaration(int flags, Type type, String name, Expression init,
                          String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (type == null) throw new IllegalArgumentException("type == null");
    if (name == null) throw new IllegalArgumentException("name == null");
    
    accessFlags = flags;
    this.type   = type;
    this.name   = name;
    initializer = init;
    
    if (type instanceof ArrayType) {
      if (initializer instanceof ArrayInitializer) {
        ((ArrayInitializer)initializer).setElementType
          (((ArrayType)type).getElementType());
      }
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
   * Gets the declared type for this field
   */
  public Type getType() {
    return type;
  }
  
  /**
   * Sets the type of this field
   * @exception IllegalArgumentException if t is null
   */
  public void setType(Type t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    
    firePropertyChange(TYPE, type, type = t);
  }
  
  /**
   * Returns the name of this field
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the field's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    
    firePropertyChange(NAME, name, name = s);
  }
  
  /**
   * Returns the initializer for this field
   */
  public Expression getInitializer() {
    return initializer;
  }
  
  /**
   * Sets the initializer
   */
  public void setInitializer(Expression e) {
    firePropertyChange(INITIALIZER, initializer, initializer = e);
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }    
}
