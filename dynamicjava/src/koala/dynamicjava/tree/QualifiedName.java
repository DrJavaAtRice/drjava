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
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the qualified name nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class QualifiedName extends PrimaryExpression implements LeftHandSide {
  /**
   * The identifiers property name
   */
  public final static String IDENTIFIERS = "identifiers";

  /**
   * The representation property name
   */
  public final static String REPRESENTATION = "representation";

  /**
   * The identifiers (tokens) that compose this name
   */
  private List<IdentifierToken> identifiers;

  /**
   * The representation of this object
   */
  private String representation;

  /**
   * Creates a new qualified name
   * @param ids  the identifiers (IdentifierTokens) that compose this name
   * @exception IllegalArgumentException if ids is null
   */
  public QualifiedName(List<IdentifierToken> ids) {
    this(ids, null, 0, 0, 0, 0);
  }

  /**
   * Creates a new qualified name
   * @param ids  the identifiers (IdentifierTokens) that compose this name
   * @param fn   the filename
   * @param bl   the begin line
   * @param bc   the begin column
   * @param el   the end line
   * @param ec   the end column
   * @exception IllegalArgumentException if ids is null
   */
  public QualifiedName(List<IdentifierToken> ids, String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);

    if (ids == null) throw new IllegalArgumentException("ids == null");

    identifiers    = ids;
    representation = TreeUtilities.listToName(ids);
  }

  /**
   * Returns the representation of this object
   */
  public String getRepresentation() {
    return representation;
  }

  /**
   * Returns the identifiers that compose this name
   */
  public List<IdentifierToken> getIdentifiers() {
    return identifiers;
  }

  /**
   * Sets the identifiers that compose this name. Update representation
   * @exception IllegalArgumentException if l is null
   */
  public void setIdentifier(List<IdentifierToken> l) {
    if (l == null) throw new IllegalArgumentException("l == null");

    firePropertyChange(IDENTIFIERS, identifiers, identifiers = l);
    firePropertyChange(REPRESENTATION,
                       representation,
                       representation = TreeUtilities.listToName(l));
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
    return "("+getClass().getName()+": "+getRepresentation()+")";
  }
}
