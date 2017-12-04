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

import koala.dynamicjava.tree.tiger.TypeParameter;

import edu.rice.cs.plt.tuple.Option;

/**
 * This class represents a type declaration
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/10
 */

public abstract class TypeDeclaration extends Declaration {
  private String name;
  private Option<List<TypeParameter>> typeParams;
  private List<? extends ReferenceTypeName> interfaces; // implements clause
  private List<Node> members;

  /**
   * Creates a new class declaration
   * @param mods    the modifiers
   * @param name    the name of the class to declare
   * @param tparams declared type parameters
   * @param impl    the list of implemented interfaces. Can be null.
   * @param body    the list of fields declarations
   * @exception IllegalArgumentException if name is null or body is null
   */
  protected TypeDeclaration(ModifierSet mods, String name, Option<List<TypeParameter>> tparams,
                             List<? extends ReferenceTypeName> impl, List<Node> body,
                             SourceInfo si) {
    super(mods, si);
    if (name == null || tparams == null || body == null) throw new IllegalArgumentException();
    this.name = name;
    typeParams = tparams;
    interfaces = impl;
    members = body;
  }

  /**
   * Returns the name of this class
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the type's name
   * @exception IllegalArgumentException if s is null
   */
  public void setName(String s) {
    if (s == null) throw new IllegalArgumentException("s == null");
    name = s;
  }

  public Option<List<TypeParameter>> getTypeParams() { return typeParams; }
  public void setTypeArgs(List<TypeParameter> tparams) { typeParams = Option.wrap(tparams); }
  public void setTypeArgs(Option<List<TypeParameter>> tparams) {
    if (tparams == null) throw new IllegalArgumentException();
    typeParams = tparams;
  }
  
  /**
   * Returns a list that contains the names (String) of the implemented interfaces.
   * Can be null.
   */
  public List<? extends ReferenceTypeName> getInterfaces() {
    return interfaces;
  }

  /**
   * Sets the interfaces (a list of strings)
   */
  public void setInterfaces(List<? extends ReferenceTypeName> l) {
    interfaces = l;
  }

  /**
   * Returns the list of the declared members
   */
  public List<Node> getMembers() {
    return members;
  }

  /**
   * Sets the members
   * @exception IllegalArgumentException if l is null
   */
  public void setMembers(List<Node> l) {
    if (l == null) throw new IllegalArgumentException("l == null");
    members = l;
  }
}
