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
 * This class represents an interface declaration
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/27
 */

public class InterfaceDeclaration extends TypeDeclaration {

  private boolean annotation;
  
  /**
   * Creates a new interface declaration
   * @param mods    the modifiers
   * @param ann     whether this is an annotation
   * @param name    the name of the interface to declare
   * @param tparams type parameters
   * @param impl    the list of implemented interfaces. Can be null.
   * @param body    the list of fields declarations
   */
  public InterfaceDeclaration(ModifierSet mods, boolean ann, String name, Option<List<TypeParameter>> tparams,
                               List<? extends ReferenceTypeName> impl, List<Node> body) {
    this(mods, ann, name, tparams, impl, body, SourceInfo.NONE);
  }

  /**
   * Creates a new interface declaration
   * @param mods  the modifiers
   * @param ann     whether this is an annotation
   * @param name  the name of the interface to declare
   * @param impl  the list of implemented interfaces. Can be null.
   * @param body  the list of fields declarations
   */
  public InterfaceDeclaration(ModifierSet mods, boolean ann, String name,
                               List<? extends ReferenceTypeName> impl, List<Node> body) {
    this(mods, ann, name, Option.<List<TypeParameter>>none(), impl, body, SourceInfo.NONE);
  }

  /**
   * Creates a new interface declaration
   * @param mods    the modifiers
   * @param ann     whether this is an annotation
   * @param name    the name of the interface to declare
   * @param tparams type parameters
   * @param impl    the list of implemented interfaces. Can be null.
   * @param body    the list of fields declarations
   */
  public InterfaceDeclaration(ModifierSet mods, boolean ann, String name, Option<List<TypeParameter>> tparams,
                               List<? extends ReferenceTypeName> impl, List<Node> body, SourceInfo si) {
    super(mods, name, tparams, ann ? addAnnotationType(impl, si) : impl, body, si);
    annotation = ann;
  }
  
  private static List<ReferenceTypeName> addAnnotationType(List<? extends ReferenceTypeName> impl, SourceInfo si) {
    List<ReferenceTypeName> result = new ArrayList<ReferenceTypeName>();
    result.add(new ReferenceTypeName(new String[]{ "java", "lang", "annotation", "Annotation" }, si));
    if (impl != null) { result.addAll(impl); }
    return result;
  }

  /**
   * Creates a new interface declaration
   * @param mods  the modifiers
   * @param ann     whether this is an annotation
   * @param name  the name of the interface to declare
   * @param impl  the list of implemented interfaces. Can be null.
   * @param body  the list of fields declarations
   */
  public InterfaceDeclaration(ModifierSet mods, boolean ann, String name,
                               List<? extends ReferenceTypeName> impl, List<Node> body, SourceInfo si) {
    this(mods, ann, name, Option.<List<TypeParameter>>none(), impl, body, si);
  }
  
  /**
   * Whether this is an annotation declaration.
   */
  public boolean isAnnotation() {
    return annotation;
  }

  /**
   * Sets whether this is an annotation declaration.
   * @exception IllegalArgumentException if s is null
   */
  public void setIsAnnotation(boolean ann) {
    annotation = ann;
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
    return getModifiers()+" "+isAnnotation()+" "+getName()+" "+getTypeParams()+" "+getInterfaces()+" "+getMembers();
  }
}
