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
 * This class represents the package declarations
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/13
 */

public class PackageDeclaration extends Declaration {
  private String name;
  
  /**
   * Creates a new package declaration node
   * @param ident a list of tokens that represents a package name.
   *              The list can be null.
   */
  public PackageDeclaration(ModifierSet mods, List<IdentifierToken> ident) {
    this(mods, ident, SourceInfo.NONE);
  }
  
  /**
   * Creates a new package declaration node
   * @param ident a list of tokens that represents a package name.
   *              The list can be null.
   */
  public PackageDeclaration(ModifierSet mods, List<IdentifierToken> ident, SourceInfo si) {
    super(mods, si);
    name = TreeUtilities.listToName(ident);
  }
  
  /**
   * Creates a new package declaration node
   * @param nm    a string that represents a package name.
   */
  public PackageDeclaration(ModifierSet mods, String nm, SourceInfo si) {
    super(mods, si);
    name = nm;
  }
  
  /**
   * Returns the name of the imported class or package
   */
  public String getName() {
    return name;
  }
  
  /**
   * Sets the name
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
    return "("+getClass().getName()+": "+getName()+")";
  }
}
