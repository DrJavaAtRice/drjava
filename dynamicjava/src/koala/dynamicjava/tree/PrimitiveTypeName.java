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

/**
 * This class represents the primitive type nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public abstract class PrimitiveTypeName extends TypeName {
  /**
   * The value of the node
   */
  private Class<?> value;
  
  /**
   * Initializes the type
   * @param val   the value of this type
   * @exception IllegalArgumentException if val is null
   */
  protected PrimitiveTypeName(Class<?> val, SourceInfo si) {
    super(si);
    
    if (val == null) throw new IllegalArgumentException("val == null");
    
    value = val;
  }
  
  /**
   * Returns the value of this node
   */
  @Deprecated public Class<?> getValue() {
    return value;
  }
  
  /**
   * Sets the value of this node
   * @exception IllegalArgumentException if c is null
   */
  public void setValue(Class<?> c) {
    if (c == null) throw new IllegalArgumentException("c == null");
    value = c;
  }
  
  /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+getValue()+")";
  }
}
