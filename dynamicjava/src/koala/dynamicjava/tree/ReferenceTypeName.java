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
 * This class represents the reference type nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class ReferenceTypeName extends TypeName {
  /**
   * The representation property name
   */
  public final static String REPRESENTATION = "representation";

  /**
   * The representation of this type
   */
  private String representation;

  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @exception IllegalArgumentException if ids is null
   */
  public ReferenceTypeName(List<IdentifierToken> ids) {
    this(ids, null, 0, 0, 0, 0);
  }

  /**
   * Initializes the type
   * @param rep   the type name
   * @exception IllegalArgumentException if rep is null
   */
  public ReferenceTypeName(String rep) {
    this(rep, null, 0, 0, 0, 0);
  }

  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if ids is null
   */
  public ReferenceTypeName(List<IdentifierToken> ids, String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);

    if (ids == null) throw new IllegalArgumentException("ids == null");

    representation = TreeUtilities.listToName(ids);
  }

  /**
   * Initializes the type
   * @param rep   the type name
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if rep is null
   */
  public ReferenceTypeName(String rep, String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);

    if (rep == null) throw new IllegalArgumentException("rep == null");

    representation = rep;
  }

  /**
   * Returns the representation of this type
   */
  public String getRepresentation() {
    return representation;
  }

  /**
   * Sets the representation of this type
   * @exception IllegalArgumentException if s is null
   */
  public void setRepresentation(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");

    firePropertyChange(REPRESENTATION, representation, representation = s);
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

  protected String toStringHelper() {
   return getRepresentation();
  }
}
