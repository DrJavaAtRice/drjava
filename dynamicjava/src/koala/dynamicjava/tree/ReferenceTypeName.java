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
  
  // TODO: Develop a better representation with more structure (resolving package/class ambiguities).
  
  /**
   * The representation property name
   */
  public final static String REPRESENTATION = "representation";

  /**
   * The representation of this type
   */
  private String representation;
  
  /**
   * The representation property name
   */
  public final static String IDENTIFIERS = "identifiers";

  /**
   * The representation of this type
   */
  private List<? extends IdentifierToken> identifiers;
  

  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @exception IllegalArgumentException if ids is null or empty
   */
  public ReferenceTypeName(List<? extends IdentifierToken> ids) {
    this(ids, SourceInfo.NONE);
  }
  
  public ReferenceTypeName(IdentifierToken... ids) {
    this(Arrays.asList(ids));
  }

  public ReferenceTypeName(String... names) {
    this(stringsToIdentifiers(names));
  }
  
  private static IdentifierToken[] stringsToIdentifiers(String[] names) {
    IdentifierToken[] ids = new IdentifierToken[names.length];
    for (int i = 0; i < names.length; i++) {
      ids[i] = new Identifier(names[i]);
    }
    return ids;
  }

  /**
   * Initializes the type
   * @param ids   the list of the tokens that compose the type name
   * @exception IllegalArgumentException if ids is null or empty
   */
  public ReferenceTypeName(List<? extends IdentifierToken> ids, SourceInfo si) {
    super(si);

    if (ids == null) throw new IllegalArgumentException("ids == null");
    if (ids.size() == 0) throw new IllegalArgumentException("ids.size() == 0");
    identifiers = ids;
    representation = TreeUtilities.listToName(ids);
  }

  /**
   * Returns the representation of this type
   */
  public String getRepresentation() {
    return representation;
  }
  
  /**
   * Returns the list of identifiers that make up this type
   */
  public List<? extends IdentifierToken> getIdentifiers() {
    return identifiers;
  }

  /**
   * Sets the identifiers of this type
   * @exception IllegalArgumentException if ids is null or empty
   */
  public void setIdentifiers(List<? extends IdentifierToken> ids) {
    if (ids == null) throw new IllegalArgumentException("ids == null");
    if (ids.size() == 0) throw new IllegalArgumentException("ids.size() == 0");
    firePropertyChange(IDENTIFIERS, identifiers, identifiers = ids);
    firePropertyChange(REPRESENTATION, representation, representation = TreeUtilities.listToName(ids));
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
