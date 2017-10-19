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
 * This class represents the import declarations
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class ImportDeclaration extends Node {
  /**
   * The name of the imported class or package
   */
  private String name;

  /**
   * Is this declaration import a class or a package
   */
  private boolean pckage;

  private boolean sttic;
  
  /**
   * Creates a new import declaration node
   * @param ident a list of tokens that represents a package or a class name
   * @param pkg   true if this declaration imports a package
   * @param sttc  true if this declaration is a static import
   * @exception IllegalArgumentException if ident is null
   */
  public ImportDeclaration(List<IdentifierToken> ident, boolean pkg, boolean sttc) {
    this(ident, pkg, sttc, SourceInfo.NONE);
  }

  /**
   * Creates a new import declaration node
   * @param ident a list of tokens that represents a package or a class name
   * @param pkg   true if this declaration imports a package
   * @param sttc  true if this declaration is a static import
   * @exception IllegalArgumentException if ident is null
   */
  public ImportDeclaration(List<IdentifierToken> ident, boolean pkg, boolean sttc,
                           SourceInfo si) {
    super(si);

    if (ident == null) throw new IllegalArgumentException("ident == null");
    pckage     = pkg;
    sttic      = sttc;
    name       = TreeUtilities.listToName(ident);
  }

  /**
   * Creates a new import declaration node
   * @param nm    a string that represents a package or a class name
   * @param pkg   true if this declaration imports a package
   * @param sttc  true if this declaration is a static import
   * @exception IllegalArgumentException if ident is null
   */
  public ImportDeclaration(String nm, boolean pkg, boolean sttc,
                           SourceInfo si) {
    super(si);

    if (nm == null) throw new IllegalArgumentException("name == null");

    pckage     = pkg;
    sttic      = sttc;
    name       = nm;
  }

  /**
   * Returns the name of the imported class or package
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the package name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    name = s;
  }

  /**
   * Returns true if the identifier represents a package, false
   * if it represents a class
   */
  public boolean isPackage() {
    return pckage;
  }

  /**
   * Returns true if the identifier represents a class whose methods are being statically imported,
   * false if it represents a single method or if it is not a static import
   */
  public boolean isStaticImportClass() {
    return sttic && pckage;
  }
  
  /**
   * Sets the package property
   */
  public void setPackage(boolean b) {
    pckage = b;
  }

  /**
   * Returns true if the identifier represents a static import, false otherwise
   */
  public boolean isStatic() {
    return sttic;
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
  
  public String toStringHelper(){
    return getName()+" "+isPackage()+" "+isStatic();
  }
}
