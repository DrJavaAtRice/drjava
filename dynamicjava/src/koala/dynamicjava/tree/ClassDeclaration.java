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

import edu.rice.cs.plt.tuple.Option;

import koala.dynamicjava.tree.tiger.TypeParameter;
import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents a class declaration
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/10
 */

public class ClassDeclaration extends TypeDeclaration {
  /** Default supertype */
  private static final ReferenceTypeName OBJECT = new ReferenceTypeName("java", "lang", "Object");

  /**
   * The superclass of this class
   */
  private ReferenceTypeName superclass;

  /**
   * Creates a new class declaration
   * @param mods  the modifiers
   * @param name  the name of the class to declare
   * @param ext   the tokens that compose the name of the parent class.
   *              The list can be null. The superclass property is then
   *              set to "java.lang.Object".
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public ClassDeclaration(ModifierSet mods, String name, ReferenceTypeName ext, List<? extends ReferenceTypeName> impl, List<Node> body) {
    this(mods, name, Option.<List<TypeParameter>>none(), ext, impl, body, SourceInfo.NONE);
  }

  /**
   * Creates a new class declaration
   * @param mods  the modifiers
   * @param name  the name of the class to declare
   * @param tparams the type parameters
   * @param ext   the tokens that compose the name of the parent class.
   *              The list can be null. The superclass property is then
   *              set to "java.lang.Object".
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public ClassDeclaration(ModifierSet mods, String name, Option<List<TypeParameter>> tparams, ReferenceTypeName ext,
                           List<? extends ReferenceTypeName> impl, List<Node> body) {
    this(mods, name, tparams, ext, impl, body, SourceInfo.NONE);
  }

  /**
   * Creates a new class declaration
   * @param mods  the modifiers
   * @param name  the name of the class to declare
   * @param ext   the tokens that compose the name of the parent class.
   *              The list can be null. The superclass property is then
   *              set to "java.lang.Object".
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public ClassDeclaration(ModifierSet mods, String name, ReferenceTypeName ext,
                           List<? extends ReferenceTypeName> impl, List<Node> body, SourceInfo si) {
    this(mods, name, Option.<List<TypeParameter>>none(), ext, impl, body, si);
  }

  /**
   * Creates a new class declaration
   * @param mods  the modifiers
   * @param name  the name of the class to declare
   * @param tparams the type parameters
   * @param ext   the tokens that compose the name of the parent class.
   *              The list can be null. The superclass property is then
   *              set to "java.lang.Object".
   * @param impl  the list of implemented interfaces (a list of list of
   *              Token). Can be null.
   * @param body  the list of members declarations
   */
  public ClassDeclaration(ModifierSet mods, String name, Option<List<TypeParameter>> tparams, ReferenceTypeName ext,
                           List<? extends ReferenceTypeName> impl, List<Node> body, SourceInfo si) {
    super(mods, name, tparams, impl, body, si);
    superclass = (ext == null) ? OBJECT : ext;
  }
  

  /**
   * Returns the name of the superclass of this class
   */
  public ReferenceTypeName getSuperclass() {
    return superclass;
  }

  /**
   * Sets the superclass name
   * @exception IllegalArgumentException if s is null
   */
  public void setSuperclass(ReferenceTypeName s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    superclass = s;
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
    return getModifiers()+" "+getName()+" "+getTypeParams()+" "+getSuperclass()+" "+getInterfaces()+" "+getMembers();
  }
}
