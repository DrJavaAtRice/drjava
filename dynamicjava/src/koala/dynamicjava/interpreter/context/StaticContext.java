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

package koala.dynamicjava.interpreter.context;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.util.*;

/**
 * A static method context.
 *
 * @author  Stephane Hillion
 * @version 1.1 - 1999/11/28
 */

public class StaticContext extends GlobalContext {
  /**
   * The declaring class of the method
   */
  protected Class declaringClass;
  
  /**
   * The default qualifier
   */
  protected Node defaultQualifier;
  
  /**
   * Creates a new context
   * @param i  the interpreter
   * @param c  the declaring class of the method
   * @param im the importation manager
   */
  public StaticContext(Interpreter i, Class c, ImportationManager im) {
    super(i);
    declaringClass     = c;
    importationManager = im;
    defaultQualifier   = new ReferenceType(c.getName());
  }
  
  /**
   * Creates a new context
   * @param i  the interpreter
   * @param c  the declaring class of the method
   * @param fp the formal parameters
   */
  public StaticContext(Interpreter i, Class c, Set fp) {
    super(i, fp);
    declaringClass   = c;
    defaultQualifier = new ReferenceType(c.getName());
  }
  
  /**
   * Tests whether a variable is defined in this context
   * @param name the name of the entry
   * @return false if the variable is undefined
   */
  public boolean isDefined(String name) {
    return isDefinedVariable(name) || fieldExists(name);
  }
  
  /**
   * Looks for a field
   * @param fc the field class
   * @param fn the field name
   * @exception NoSuchFieldException if the field cannot be found
   * @exception AmbiguousFieldException if the field is ambiguous
   */
  public Field getField(Class fc, String fn) throws NoSuchFieldException,
    AmbiguousFieldException {
    Field f;
    try {
      f =  ReflectionUtilities.getField(fc, fn);
    } catch (Exception e) {
      f = InterpreterUtilities.getOuterField(fc, fn);
    }
    setAccessFlag(f);
    return f;
  }
  
  /**
   * Creates the tree that is associated with the given name
   * @param node the current node
   * @param name the variable name
   * @exception IllegalStateException if the variable is not defined
   */
  public Expression createName(Node node, IdentifierToken name) {
    if (isDefinedVariable(name.image())) {
      return super.createName(node, name);
    } else {
      String fname = name.image();
      try {
        ReflectionUtilities.getField(declaringClass, fname);
        return new StaticFieldAccess((ReferenceType)defaultQualifier, fname);
      } catch (Exception e) {
        try {
          Field f = InterpreterUtilities.getOuterField(declaringClass, fname);
          Class c = f.getDeclaringClass();
          return new StaticFieldAccess(new ReferenceType(c.getName()), fname);
        } catch (Exception ex) {
          throw new CatchedExceptionError(ex, node);
        }
      }
    }
  }
  
  /**
   * Returns the default qualifier for this context
   * @param node the current node
   */
  public Node getDefaultQualifier(Node node) {
    return defaultQualifier;
  }
  
  /**
   * Returns the modifier that match the given node
   * @param node a tree node
   */
  public LeftHandSideModifier getModifier(SuperFieldAccess node) {
    return new SuperFieldModifier((Field)node.getProperty(NodeProperties.FIELD),
                                  node);
  }
  
  /**
   * Looks for a method
   * @param prefix the method prefix
   * @param mname  the method name
   * @param params the parameter types
   * @exception NoSuchMethodException if the method cannot be found
   */
  public Method lookupMethod(Node prefix, String mname, Class[] params)
    throws NoSuchMethodException {
    Class  c = (Class)NodeProperties.getType(prefix);
    Method m = null;
    try {
      m = ReflectionUtilities.lookupMethod(c, mname, params);
      setAccessFlag(m);
      if (m.getName().equals("clone")) {
        m.setAccessible(true);
      }
      return m;
    } catch (NoSuchMethodException e) {
      if ((prefix instanceof ReferenceType) && c == declaringClass) {
        m = InterpreterUtilities.lookupOuterMethod(c, mname, params);
        m.setAccessible(true);
        return m;
      } else {
        throw e;
      }
    }
  }
  
  /**
   * Looks for a field in the super class
   * @param node the current node
   * @param fn the field name
   * @exception NoSuchFieldException if the field cannot be found
   * @exception AmbiguousFieldException if the field is ambiguous
   */
  public Field getSuperField(Node node, String fn) throws NoSuchFieldException,
    AmbiguousFieldException {
    Class sc = declaringClass.getSuperclass();
    Field result = ReflectionUtilities.getField(sc, fn);
    setAccessFlag(result);
    return result;
  }
  
  /**
   * Looks for a class
   * @param cname the class name
   * @exception ClassNotFoundException if the class cannot be found
   */
  public Class lookupClass(String cname) throws ClassNotFoundException {
    try {
      return importationManager.lookupClass(cname, declaringClass.getName());
    } catch (ClassNotFoundException e) {
      Class dc = declaringClass.getDeclaringClass();
      if (dc != null) {
        try {
          return importationManager.lookupClass(cname, dc.getName());
        } catch (Exception ex) {
        }
      }
      dc = declaringClass.getSuperclass();
      while (dc != null) {
        try {
          return importationManager.lookupClass(cname, dc.getName());
        } catch (Exception ex) {
        }
        dc = dc.getSuperclass();
      }
      throw e;
    }
  }
  
  /**
   * Defines a MethodDeclaration as a function
   * @param node the function declaration
   */
  public void defineFunction(MethodDeclaration node) {
    throw new IllegalStateException("internal.error");
  }
  
  /**
   * Defines a class from its syntax tree
   * @param node the class declaration
   */
  public void defineClass(TypeDeclaration node) {
    throw new ExecutionError("not.implemented");
  }
  
  /**
   * Sets the properties of a ClassAllocation node
   * @param node the allocation node
   * @param c the class of the constructor
   * @param args the classes of the arguments of the constructor
   * @param memb the class members
   */
  public Class setProperties(ClassAllocation node, Class c, Class[] args, List<Node> memb) {
    String dname = declaringClass.getName();
    String cname = dname + "$" + classCount++;
    FieldDeclaration fd;
    ConstructorDeclaration csd;
    
    // Create the reference to the declaring class
    fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC,
                              CLASS_TYPE,
                              "declaring$Class$Reference$0",
                              new TypeExpression(new ReferenceType(dname)));
    memb.add(fd);
    
    // Add the reference to the final local variables map
    memb.add(LOCALS);
    
    // Create the reference to the final local variables map
    fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC,
                              OBJECT_ARRAY_ARRAY,
                              "local$Variables$Class$0",
                              createClassArrayInitializer());
    memb.add(fd);
    
    // Create the constructor
    List<FormalParameter> params = new LinkedList<FormalParameter>();
    List<Node> stmts = new LinkedList<Node>();
    
    // Add the final local variables map parameter
    params.add(new FormalParameter(false, MAP_TYPE, "param$0"));
    
    // Add the other parameters
    List<Expression> superArgs = new LinkedList<Expression>();
    for (int i = 0; i < args.length; i++) {
      params.add(new FormalParameter(false, 
                                     TreeUtilities.classToType(args[i]),
                                     "param$" + (i + 1)));
      List<IdentifierToken> l = new LinkedList<IdentifierToken>();
      l.add(new Identifier("param$" + (i + 1)));
      superArgs.add(new QualifiedName(l));
    }
    
    // Create the explicit constructor invocation
    ConstructorInvocation ci = null;
    if (superArgs.size() > 0) {
      ci = new ConstructorInvocation(null, superArgs, true);
    }
    
    // Add the outer instance reference initialization statement
    List<IdentifierToken> p1 = new LinkedList<IdentifierToken>();
    p1.add(new Identifier("local$Variables$Reference$0"));
    List<IdentifierToken> p2 = new LinkedList<IdentifierToken>();
    p2.add(new Identifier("param$0"));
    stmts.add(new SimpleAssignExpression(new QualifiedName(p1),
                                         new QualifiedName(p2)));
    
    csd = new ConstructorDeclaration(Modifier.PUBLIC,
                                     cname,
                                     params,
                                     new LinkedList<List<IdentifierToken>>(),
                                     ci,
                                     stmts);
    memb.add(csd);
    
    // Set the inheritance
    List<IdentifierToken> ext = null;
    List<List<IdentifierToken>> impl = null;
    if (c.isInterface()) {
      impl = new LinkedList<List<IdentifierToken>>();
      List<IdentifierToken> intf = new LinkedList<IdentifierToken>();
      intf.add(new Identifier(c.getName()));
      impl.add(intf);
    } else {
      ext = new LinkedList<IdentifierToken>();
      ext.add(new Identifier(c.getName()));
    }
    
    // Create the class
    TypeDeclaration type = new ClassDeclaration(Modifier.PUBLIC,
                                                cname,
                                                ext,
                                                impl,
                                                memb);
    
    type.setProperty(TreeClassInfo.ANONYMOUS_DECLARING_CLASS,
                     new JavaClassInfo(declaringClass));
    
    Class cl = new TreeCompiler(interpreter).compileTree(this, type);
    
    // Update the argument types
    Class[] tmp = new Class[args.length+1];
    tmp[0] = Map.class;
    for (int i = 1; i < tmp.length; i++) {
      tmp[i] = args[i - 1];
    }
    args = tmp;
    try {
      node.setProperty(NodeProperties.CONSTRUCTOR, lookupConstructor(cl, args));
    } catch (NoSuchMethodException e) {
      // Never get here
      e.printStackTrace();
    }
    node.setProperty(NodeProperties.TYPE, cl);
    return cl;
  }
  
  /**
   * Looks for a super method
   * @param node the current node
   * @param mname  the method name
   * @param params the parameter types
   * @exception NoSuchMethodException if the method cannot be found
   */
  public Method lookupSuperMethod(Node node, String mname, Class[] params)
    throws NoSuchMethodException {
    Method m = null;
    try {
      m = ReflectionUtilities.lookupMethod(declaringClass,
                                           "super$" + mname,
                                           params);
    } catch (NoSuchMethodException e) {
      m = ReflectionUtilities.lookupMethod(declaringClass, mname, params); 
    }
    setAccessFlag(m);
    return m;
  }
  
  /**
   * Whether a simple identifier is a class
   * @param name the identifier
   */
  public boolean classExists(String name) {
    boolean result = false;
    importationManager.setClassLoader(new PseudoClassLoader());
    try {
      importationManager.lookupClass(name, declaringClass.getName());
      result = true;
    } catch (ClassNotFoundException e) {
    } catch (PseudoError e) {
      result = true;
    } finally {
      if (classLoader == null) {
        importationManager.setClassLoader(interpreter.getClassLoader());
      } else {
        importationManager.setClassLoader(classLoader);
      }
    }
    return result;
  }
  
  /**
   * Sets the access flag of a member
   */
  protected void setAccessFlag(Member m) {
    int     mods    = m.getModifiers();
    Class   c       = m.getDeclaringClass();
    int     cmods   = c.getModifiers();
    String  pkg     = importationManager.getCurrentPackage();
    String  mp      = getPackageName(c);
    boolean samePkg = pkg.equals(mp);
    
    if (Modifier.isPublic(cmods) || samePkg) {
      if (Modifier.isPublic(mods)) {
        ((AccessibleObject)m).setAccessible(true);
      } else if (Modifier.isProtected(mods)) {
        if (c.isAssignableFrom(declaringClass.getSuperclass()) || samePkg) {
          ((AccessibleObject)m).setAccessible(true);
        }
      } else if (!Modifier.isPrivate(mods)) {
        if (samePkg) {
          ((AccessibleObject)m).setAccessible(true);
        }               
      } else {
        if (declaringClass == c || isInnerClass(declaringClass, c)) {
          ((AccessibleObject)m).setAccessible(true);
        }
      }
    }
  }
  
  /**
   * Is c1 an inner class of c2?
   */
  protected boolean isInnerClass(Class c1, Class c2) {
    Class c = c1.getDeclaringClass();
    if (c == null) {
      try {
        Field f = c1.getField("declaring$Class$Reference$0");
        c = (Class)f.get(null);
      } catch (Exception e) {
      }
    }
    c1 = c;
    while (c != null) {
      if (c == c2) {
        return true;
      }
      c = c.getDeclaringClass();
      if (c == null) {
        try {
          Field f = c1.getField("declaring$Class$Reference$0");
          c = (Class)f.get(null);
        } catch (Exception e) {
        }
      }
      c1 = c;
    }
    return false;
  }
  
  /**
   * Whether the given name represents a field in this context
   * @param name the field name
   */
  protected boolean fieldExists(String name) {
    boolean result = false;
    try {
      ReflectionUtilities.getField(declaringClass, name);
      result = true;
    } catch (NoSuchFieldException e) {
      try {
        InterpreterUtilities.getOuterField(declaringClass, name);
        result = true;
      } catch (NoSuchFieldException ex) {
      } catch (AmbiguousFieldException ex) {
        result = true;
      }
    } catch (AmbiguousFieldException e) {
      result = true;
    }
    return result;
  }
}
