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
 * This class represents variable declarations in an AST
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/11
 */

public class VariableDeclaration extends Node {
  /**
   * The final property name
   */
  public final static String FINAL = "final";
  
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
   * Whether this variable is final
   */
  private boolean finalVariable;
  
  /**
   * The type of this variable
   */
  private Type type;
  
  /**
   * The name of this variable
   */
  private String name;
  
  /**
   * The initializer
   */
  private Expression initializer;
  
  /**
   * Creates a new variable declaration
   * @param fin    is this variable final?
   * @param type   the type of this variable
   * @param name   the name of this variable
   * @param init   the initializer
   * @exception IllegalArgumentException if name is null or type is null
   */
  public VariableDeclaration(boolean fin, Type type, String name, Expression init) {
    this(fin, type, name, init, null, 0, 0, 0, 0);
  }
  
  /**
   * Creates a new variable declaration
   * @param fin    is this variable final?
   * @param type   the type of this variable
   * @param name   the name of this variable
   * @param init   the initializer
   * @param fn     the filename
   * @param bl     the begin line
   * @param bc     the begin column
   * @param el     the end line
   * @param ec     the end column
   * @exception IllegalArgumentException if name is null or type is null
   */
  public VariableDeclaration(boolean fin, Type type, String name, Expression init,
                             String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (type == null) throw new IllegalArgumentException("type == null");
    if (name == null) throw new IllegalArgumentException("name == null");
    
    finalVariable = fin;
    this.type     = type;
    this.name     = name;
    initializer   = init;
    
    if (type instanceof ArrayType) {
      if (initializer instanceof ArrayInitializer) {
        ((ArrayInitializer)initializer).setElementType
          (((ArrayType)type).getElementType());
      }
    }
  }
  
  /**
   * Returns true if this variable is final
   */
  public boolean isFinal() {
    return finalVariable;
  }
  
  /**
   * Sets the final flag
   */
  public void setFinal(boolean b) {
    firePropertyChange(FINAL, finalVariable, finalVariable = b);
  }
  
  /**
   * Gets the declared type for this variable
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
   * Returns the name of this variable
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the variable's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    
    firePropertyChange(NAME, name, name = s);
  }
  
  /**
   * Returns the initializer for this variable
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
    /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+isFinal()+" "+getType()+" "+getName()+" "+getInitializer()+")";
  }
}
