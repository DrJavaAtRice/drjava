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

package koala.dynamicjava.classinfo;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.classfile.JVMUtilities;
import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * The instances of this class provides informations about
 * classes not yet compiled to JVM bytecode and represented
 * by a syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/10/25
 */

public class TreeClassInfo implements ClassInfo {
  /**
   * The declaringClass property is defined for each inner
   * class/interface declaration
   * It contains a TypeDeclaration
   */
  private final static String DECLARING_CLASS = "declaringClass";

  /**
   * The declaringClass property is defined for each anonymous inner
   * class/interface declaration
   * It contains a TypeDeclaration
   */
  public final static String ANONYMOUS_DECLARING_CLASS = "anonymousDeclaringClass";

  /**
   * This property is used to ensure that the modifications on
   * the tree are not done twice
   */
  private final static String TREE_VISITED = "treeVisited";

  /**
   * The abstract syntax tree of this class
   */
  private TypeDeclaration classTree;

  /**
   * The class finder for this class
   */
  private ClassFinder classFinder;

  /**
   * The dimension of this type
   */
  private int dimension;

  /**
   * The full class name
   */
  private String name;

  /**
   * The class info of the superclass of the class represented
   * by this field
   */
  private ClassInfo superclass;

  /**
   * Whether this class is an interface
   */
  private boolean interfaceInfo;

  /**
   * The interfaces
   */
  private ClassInfo[] interfaces;

  /**
   * The fields
   */
  private Map<String,FieldInfo> fields = new HashMap<String,FieldInfo>();

  /**
   * The methods
   */
  private Map<String,List<MethodInfo>> methods = new HashMap<String,List<MethodInfo>>();

  /**
   * The constructors
   */
  private List<ConstructorInfo> constructors = new LinkedList<ConstructorInfo>();

  /**
   * The declared classes
   */
  private List<ClassInfo> classes = new LinkedList<ClassInfo>();

  /**
   * The compilable property value
   */
  private boolean compilable = true;

  /**
   * The method count
   */
  private int methodCount;

  /**
   * Creates a new class info
   * @param cd the class declaration
   * @param cf the class finder
   */
  public TreeClassInfo(TypeDeclaration cd, ClassFinder cf) {
    classFinder    = cf;
    classTree      = cd;
    name           = fullName();
    interfaceInfo  = cd instanceof InterfaceDeclaration;
    new MembersVisitor();
    classTree.setProperty(TREE_VISITED, null);
  }

  /**
   * Creates a new array class info
   * @param ci  the class info
   */
  public TreeClassInfo(TreeClassInfo ci) {
    classFinder = ci.classFinder;
    classTree   = ci.classTree;
    dimension   = ci.dimension + 1;
    name        = "[" + ((ci.isArray()) ? ci.getName() : "L" + ci.getName() + ";");
    new MembersVisitor();
  }

  /**
   * Returns the underlying class
   */
  public Class getJavaClass() {
    throw new IllegalStateException();
  }

  /**
   * Returns the abstract syntax tree
   */
  public TypeDeclaration getTypeDeclaration() {
    return classTree;
  }

  /**
   * Returns the class finder
   */
  public ClassFinder getClassFinder() {
    return classFinder;
  }

  /**
   * Whether the underlying class needs compilation
   */
  public boolean isCompilable() {
    return compilable;
  }

  /**
   * Sets the compilable property
   */
  public void setCompilable(boolean b) {
    compilable = b;
  }

  /**
   * Returns the declaring class or null
   */
  public ClassInfo getDeclaringClass() {
    return (dimension == 0)
      ? (ClassInfo)classTree.getProperty(DECLARING_CLASS)
      : null;
  }

  /**
   * Returns the declaring class of an anonymous class or null
   */
  public ClassInfo getAnonymousDeclaringClass() {
    return (dimension == 0)
      ? (ClassInfo)classTree.getProperty(ANONYMOUS_DECLARING_CLASS)
      : null;
  }

  /**
   * Returns the modifiers flags
   */
  public int getModifiers() {
    return (dimension == 0) ? classTree.getAccessFlags() : Modifier.PUBLIC;
  }

  /**
   * Returns the fully qualified name of the underlying class
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the class info of the superclass of the class
   * represented by this class
   * @exception NoClassDefFoundError if the class cannot be loaded
   */
  public ClassInfo getSuperclass() {
    if (superclass == null) {
      if (interfaceInfo) {
        superclass = lookupClass("java.lang.Object");
      } else {
        ClassDeclaration cd = (ClassDeclaration)classTree;
        superclass = lookupClass(cd.getSuperclass(), getDeclaringClass());
      }
    }
    return superclass;
  }

  /**
   * Returns the class infos of the interfaces implemented by
   * the class this info represents
   * @exception NoClassDefFoundError if an interface cannot be loaded
   */
  public ClassInfo[] getInterfaces() {
    if (interfaces == null) {
      if (dimension > 0) {
        interfaces = new ClassInfo[] {
          lookupClass("java.lang.Cloneable"),
            lookupClass("java.io.Serializable")
        };
      } else {
        List l = classTree.getInterfaces();
        if (l != null) {
          interfaces = new ClassInfo[l.size()];
          Iterator it = l.iterator();
          int i = 0;
          while (it.hasNext()) {
            String s = (String)it.next();
            interfaces[i++] = lookupClass(s, getDeclaringClass());
          }
        } else {
          interfaces = new ClassInfo[0];
        }
      }
    }
    return interfaces.clone();
  }

  /**
   * Returns the field represented by the given node
   * @param node the node that represents the field
   */
  public FieldInfo getField(FieldDeclaration node) {
    return (TreeFieldInfo)fields.get(node.getName());
  }

  /**
   * Returns the field infos for the current class
   */
  public FieldInfo[] getFields() {
    if (dimension == 0) {
      Set         keys   = fields.keySet();
      Iterator    it     = keys.iterator();

      FieldInfo[] result = new FieldInfo[keys.size()];
      int i = 0;
      while (it.hasNext()) {
        result[i++] = fields.get(it.next());
      }
      return result;
    } else {
      return new FieldInfo[0];
    }
  }

  /**
   * Returns the constructor infos for the current class
   */
  public ConstructorInfo[] getConstructors() {
    if (dimension == 0) {
      Iterator          it     = constructors.iterator();
      ConstructorInfo[] result = new ConstructorInfo[constructors.size()];
      int i = 0;
      while (it.hasNext()) {
        result[i++] = (ConstructorInfo)it.next();
      }
      return result;
    } else {
      return new ConstructorInfo[0];
    }
  }

  /**
   * Returns the method represented by the given node
   * @param node the node that represents the method
   */
  public MethodInfo getMethod(MethodDeclaration node) {
    Set         keys   = methods.keySet();
    Iterator    it     = keys.iterator();

    while (it.hasNext()) {
      List l = methods.get(it.next());
      Iterator lit = l.iterator();
      while (lit.hasNext()) {
        TreeMethodInfo mi = (TreeMethodInfo)lit.next();
        if (mi.getMethodDeclaration() == node) {
          return mi;
        }
      }
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns the method infos for the current class
   */
  public MethodInfo[] getMethods() {
    if (dimension == 0) {
      MethodInfo[] result = new MethodInfo[methodCount];
      Iterator     it     = methods.values().iterator();
      int i = 0;
      while (it.hasNext()) {
        Iterator lit = ((List)it.next()).iterator();
        while (lit.hasNext()) {
          result[i++] = (MethodInfo)lit.next();
        }
      }
      return result;
    } else {
      return new MethodInfo[0];
    }
  }

  /**
   * Returns the classes and interfaces declared as members
   * of the class represented by this ClassInfo object.
   */
  public ClassInfo[] getDeclaredClasses() {
    if (dimension == 0) {
      Iterator    it = classes.iterator();
      ClassInfo[] result = new ClassInfo[classes.size()];
      int i = 0;
      while (it.hasNext()) {
        result[i++] = (ClassInfo)it.next();
      }
      return result;
    } else {
      return new ClassInfo[0];
    }
  }

  /**
   * Returns the array type that contains elements of this class
   */
  public ClassInfo getArrayType() {
    return new TreeClassInfo(this);
  }

  /**
   * Whether this object represents an interface
   */
  public boolean isInterface() {
    return classTree instanceof InterfaceDeclaration;
  }

  /**
   * Whether this object represents an array
   */
  public boolean isArray() {
    return dimension > 0;
  }

  /**
   * Whether this object represents a primitive type
   */
  public boolean isPrimitive() {
    return false;
  }

  /**
   * Returns the component type of this array type
   * @exception IllegalStateException if this type do not represent an array
   */
  public ClassInfo getComponentType() {
    if (!isArray()) throw new IllegalStateException();

    TreeClassInfo bt = new TreeClassInfo(classTree, classFinder);
    for (int i = 0; i < dimension - 1; i++) {
      bt = new TreeClassInfo(bt);
    }
    return bt;
  }

  /**
   * Indicates whether some other object is "equal to" this one
   */
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ClassInfo)) {
      return false;
    }
    return getName().equals(((ClassInfo)obj).getName());
  }

  /**
   * Returns the full name of this class
   */
  private String fullName() {
    String    s;
    ClassInfo ci = (ClassInfo)classTree.getProperty(DECLARING_CLASS);
    if (ci != null) {
      s = ci.getName() + "$";
    } else {
      s = classFinder.getCurrentPackage();
      if (!s.equals("")) {
        s += ".";
      }
    }
    return s + classTree.getName();
  }

  /**
   * Looks for a class from its name
   * @param s the name of the class to find
   * @exception NoClassDefFoundError if the class cannot be loaded
   */
  private ClassInfo lookupClass(String s) {
    try {
      return classFinder.lookupClass(s);
    } catch (ClassNotFoundException e) {
      throw new NoClassDefFoundError(e.getMessage());
    }
  }

  /**
   * Looks for a class from its name
   * @param s the name of the class to find
   * @param c the context
   * @exception NoClassDefFoundError if the class cannot be loaded
   */
  private ClassInfo lookupClass(String s, ClassInfo c) {
    try {
      if (c != null) {
        return classFinder.lookupClass(s, c);
      } else {
        return classFinder.lookupClass(s);
      }
    } catch (ClassNotFoundException e) {
      throw new NoClassDefFoundError(e.getMessage());
    }
  }

  /**
   * Returns the nesting level of the class
   */
  private int getNestingLevel() {
    int result = -1;
    ClassInfo ci = this;
    while (!Modifier.isStatic(ci.getModifiers()) &&
           (ci = ci.getDeclaringClass()) != null) {
      result++;
    }
    return result;
  }

  /**
   * To initialize the ClassInfo
   */
  private class MembersVisitor extends VisitorObject<Void> {
    /**
     * Creates a new members visitor and iterate over the members
     * of the class represented by this ClassInfo
     */
    MembersVisitor() {
      if (!isArray()) {
        Iterator<Node> it = classTree.getMembers().iterator();
        while (it.hasNext()) {
          it.next().acceptVisitor(this);
        }

        if (!classTree.hasProperty(TREE_VISITED)) {
          ClassInfo dc = getDeclaringClass();
          if (dc != null && !Modifier.isStatic(getModifiers())) {
            // Add a reference to the outer instance
            FieldDeclaration fd;
            fd = new FieldDeclaration(Modifier.PUBLIC,
                                      new ReferenceType(dc.getName()),
                                      "this$" + getNestingLevel(),
                                      null);
            fd.acceptVisitor(this);
            classTree.getMembers().add(fd);
          }

          if (constructors.size() == 0 &&
              !isInterface() &&
              !isPrimitive()) {

            // Add a default constructor
            ConstructorInvocation  ci;
            ci = new ConstructorInvocation(null, null, true);
            ConstructorDeclaration cd;
            cd = new ConstructorDeclaration(Modifier.PUBLIC,
                                            classTree.getName(),
                                            new LinkedList<FormalParameter>(),
                                            new LinkedList<ReferenceType>(),
                                            ci,
                                            new LinkedList<Node>(), false);
            cd.acceptVisitor(this);
            classTree.getMembers().add(cd);
          }
        }
      }
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Void visit(ClassDeclaration node) {
      node.setProperty(DECLARING_CLASS, TreeClassInfo.this);
      classes.add(classFinder.addClassInfo(getName()+"$"+node.getName(), node));
      return null;
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Void visit(InterfaceDeclaration node) {
      node.setProperty(DECLARING_CLASS, TreeClassInfo.this);
      classes.add(classFinder.addClassInfo(getName()+"$"+node.getName(), node));
      return null;
    }

    /**
     * Visits a FieldDeclaration
     * @param node the node to visit
     */
    public Void visit(FieldDeclaration node) {
      fields.put(node.getName(), new TreeFieldInfo(node,
                                                   classFinder,
                                                   TreeClassInfo.this));
      return null;
    }

    /**
     * Visits a ConstructorDeclaration
     * @param node the node to visit
     */
    public Void visit(ConstructorDeclaration node) {
      if (node.getConstructorInvocation() == null) {
        ConstructorInvocation ci;
        ci = new ConstructorInvocation(null, null, true);
        node.setConstructorInvocation(ci);
      }

      // Add the outer parameter if needed
      ClassInfo dc = getDeclaringClass();
      if (!classTree.hasProperty(TREE_VISITED)) {
        if (dc != null && !Modifier.isStatic(getModifiers())) {
          ReferenceType t = new ReferenceType(dc.getName());
          node.getParameters().add(0,
                                   new FormalParameter(false, t, "param$0"));
        }
      }

      if (dc != null && !Modifier.isStatic(getModifiers())) {
        // Add the initialization of the outer instance reference
        SimpleAssignExpression sae;
        List<IdentifierToken> l1 = new LinkedList<IdentifierToken>();
        l1.add(new Identifier("this$" + getNestingLevel()));
        List<IdentifierToken> l2 = new LinkedList<IdentifierToken>();
        l2.add(new Identifier("param$0"));
        sae = new SimpleAssignExpression
          (new QualifiedName(l1),
           new QualifiedName(l2));
        node.getStatements().add(0, sae);
      }

      constructors.add(new TreeConstructorInfo(node,
                                               classFinder,
                                               TreeClassInfo.this));
      return null;
    }

    /**
     * Visits a MethodDeclaration
     * @param node the node to visit
     */
    public Void visit(MethodDeclaration node) {
      List<MethodInfo> l = methods.get(node.getName());
      if (l == null) {
        l = new LinkedList<MethodInfo>();
      }

      l.add(new TreeMethodInfo(node, classFinder, TreeClassInfo.this));
      methods.put(node.getName(), l);
      methodCount++;
      return null;
    }
  }
}
