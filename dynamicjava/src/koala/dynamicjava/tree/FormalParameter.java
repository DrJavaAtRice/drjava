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
 * This class represents the method parameters in the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/11
 */

public class FormalParameter extends Declaration {
  /**
   * The type of this parameter
   */
  private TypeName type;
  
  /**
   * The name of this parameter
   */
  private String name;
  
  /**
   * Initializes the node
   * @param mods  the modifiers
   * @param t     the type of the parameter
   * @param n     the name of the parameter
   * @exception IllegalArgumentException if t is null or n is null
   */
  public FormalParameter(ModifierSet mods, TypeName t, String n) {
    this(mods, t, n, SourceInfo.NONE);
  }
  
  /**
   * Initializes the node
   * @param mods  the modifiers
   * @param t     the type of the parameter
   * @param n     the name of the parameter
   * @exception IllegalArgumentException if t is null or n is null
   */
  public FormalParameter(ModifierSet mods, TypeName t, String n,
                         SourceInfo si) {
    super(mods, si);
    
    if (t == null) throw new IllegalArgumentException("t == null");
    if (n == null) throw new IllegalArgumentException("n == null");
    type           = t;
    name           = n;
  }
  
  /**
   * Returns the declaring type of this parameter
   */
  public TypeName getType() {
    return type;
  }
  
  /**
   * Sets the type of this parameter
   * @exception IllegalArgumentException if t is null
   */
  public void setType(TypeName t) {
    if (t == null) throw new IllegalArgumentException("t == null");
    type = t;
  }
  
  /**
   * The name of this parameter
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets this parameter's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    name = s;
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
    return "("+getClass().getName()+": "+getModifiers()+" "+getType()+" "+getName()+")";
  }
}
