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

import java.io.*;
//import java.lang.reflect.*; //  Broken into All classes imported, to avoid importing the class Type
import java.lang.reflect.Modifier;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

import java.util.*;

import koala.dynamicjava.classinfo.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * A global context.
 *
 * @author  Stephane Hillion
 * @version 1.4 - 2001/01/28
 */

public class GlobalContext extends VariableContext implements Context {
  // Constant objects
  protected final static ReferenceType CLASS_TYPE  = new ReferenceType("java.lang.Class");
  protected final static ReferenceType MAP_TYPE    = new ReferenceType("java.util.Map");
  protected final static ReferenceType OBJECT_TYPE = new ReferenceType("java.lang.Object");
  protected final static ArrayType OBJECT_ARRAY_ARRAY = new ArrayType(OBJECT_TYPE, 2);
  protected final static TypeExpression OBJECT_CLASS = new TypeExpression(OBJECT_TYPE);

  protected final static String LOCALS_NAME = "local$Variables$Reference$0";
  protected final static FieldDeclaration LOCALS =
    new FieldDeclaration(Modifier.PUBLIC, MAP_TYPE, LOCALS_NAME, null);

  /**
   * To generate an unique name for the generated classes
   */
  protected static int classCount = 0;

  /**
   * The interpreter
   */
  protected Interpreter interpreter;

  /**
   * The class loader
   */
  protected ClassLoader classLoader;

  /**
   * The class loader container
   */
  protected ClassLoaderContainer clc;

  /**
   * The functions
   */
  protected List<MethodDeclaration> functions = new LinkedList<MethodDeclaration>();

  protected boolean accessible = false;

  /**
   * Creates a new context
   * @param i the interpreter
   */
  public GlobalContext(Interpreter i) {
    importationManager = new BufferedImportationManager(i.getClassLoader());
    interpreter        = i;
  }

  /**
   * Creates a new context
   * @param i   the interpreter
   * @param cl  the classloader to use
   * @param cl2 the additional classloader
   */
  public GlobalContext(Interpreter i, ClassLoader cl) {
    super(new BufferedImportationManager(cl));
    interpreter        = i;
    classLoader        = cl;
  }

  /**
   * Creates a new context initialized with the given entries defined
   * in the initial scope.
   * @param i the interpreter
   * @param entries a set of string
   */
  public GlobalContext(Interpreter i, Set entries) {
    super(entries);
    interpreter = i;
  }

  /**
   * Sets the additional class loader container
   */
  public void setAdditionalClassLoaderContainer(ClassLoaderContainer clc) {
    this.clc = clc;
  }

  /**
   * Allows the scripts to access private fields.
   */
  public void setAccessible(boolean accessible) {
    this.accessible = accessible;
  }

  /**
   * Returns the accessibility state of this context.
   */
  public boolean getAccessible() {
    return this.accessible;
  }

  /**
   * Gets the additional class loader
   */
  protected ClassLoader getAdditionalClassLoader() {
    if (clc != null) {
      return clc.getClassLoader();
    }
    return null;
  }

  /**
   * Sets the defined functions
   */
  public void setFunctions(List<MethodDeclaration> l) {
    functions = l;
  }

  /**
   * Returns the defined functions
   */
  public List<MethodDeclaration> getFunctions() {
    return functions;
  }

  /**
   * Returns the current interpreter
   */
  public Interpreter getInterpreter() {
    return interpreter;
  }

  /**
   * Returns the importation manager
   */
  public ImportationManager getImportationManager() {
    return importationManager;
  }

  /**
   * Sets the importation manager
   */
  public void setImportationManager(ImportationManager im) {
    importationManager = im;
  }

  /**
   * Whether a simple identifier represents an existing
   * variable or field or type in this context.
   * @param name the identifier
   */
  public boolean exists(String name) {
    return isDefined(name) || classExists(name);
  }

  /**
   * Whether a simple identifier is a class
   * @param name the identifier
   */
  public boolean classExists(String name) {
    boolean result = false;
    importationManager.setClassLoader(new PseudoClassLoader());
    try {
      lookupClass(name);
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
   * Defines a MethodDeclaration as a function
   * @param node the function declaration
   */
  public void defineFunction(MethodDeclaration node) {
    functions.add(0, node);
  }
  
  /**
   * Defines a class from its syntax tree
   * @param node the class declaration
   */
  public void defineClass(TypeDeclaration node) {
    new TreeCompiler(interpreter).compileTree(this, node);
  }

  /**
   * Tests whether a variable is defined in this context
   * @param name the name of the entry
   * @return false if the variable is undefined
   */
  public boolean isDefined(String name) {
    return isDefinedVariable(name);
  }

  /**
   * Sets the current package
   * @param pkg the package name
   */
  public void setCurrentPackage(String pkg) {
    importationManager.setCurrentPackage(pkg);
  }

  /**
   * Returns the current package
   */
  public String getCurrentPackage() {
    return importationManager.getCurrentPackage();
  }

  /**
   * Declares a new import-on-demand clause
   * @param pkg the package name
   */
  public void declarePackageImport(String pkg) {
    importationManager.declarePackageImport(pkg);
  }
  
  /**
   * Declares a new single-type-import clause
   * @param cname the fully qualified class name
   * @exception ClassNotFoundException if the class cannot be found
   */
  public void declareClassImport(String cname) throws ClassNotFoundException {
    importationManager.setClassLoader(new PseudoClassLoader());
    try {
      importationManager.declareClassImport(cname);
    } catch (PseudoError e) {
    } finally {
      if (classLoader == null) {
        importationManager.setClassLoader(interpreter.getClassLoader());
      } else {
        importationManager.setClassLoader(classLoader);
      }
    }
  }
  
  /**
   * Declares a new import-on-demand clause for the importation of the static methods and fields of a class
   * @param cname the fully qualified class name
   */
  public void declareClassStaticImport(String cname) throws ClassNotFoundException {
    importationManager.declareClassStaticImport(cname);
  }
  
  /**
   * Declares a new single-type-import clause for the importation of a static member
   * @param member the method or field name
   */
  public void declareMemberStaticImport(String member) {
    importationManager.declareMemberStaticImport(member);
  }
  
  /**
   * Returns the default qualifier for this context
   * @param node the current node
   */
  public Node getDefaultQualifier(Node node) {
    return getDefaultQualifier(node, "");
  }

  /**
   * Returns the default qualifier for this context
   * @param node the current node
   * @param tname the qualifier of 'this'
   */
  public Node getDefaultQualifier(Node node, String tname) {
    return null;
  }
  
  /**
     * Returns the fully qualified class name that wraps the given staticly imported method
     * @param methodName the method name
     * @param args the argument list for the method
     */
  public List<IdentifierToken> getQualifiedName(String methodName, Class[] args) throws NoSuchMethodException{
   return importationManager.getQualifiedName(methodName, args);    
  }

  /**
   * Returns the modifier that match the given node
   * @param node a tree node
   */
  public LeftHandSideModifier getModifier(QualifiedName node) {
    if (isFinal(node.getRepresentation())) {
      return new FinalVariableModifier(node, NodeProperties.getType(node));
    } else {
      return new VariableModifier(node, NodeProperties.getType(node));
    }
  }

  /**
   * Returns the modifier that match the given node
   * @param node a tree node
   */
  public LeftHandSideModifier getModifier(ObjectFieldAccess node) {
    Field f = (Field)node.getProperty(NodeProperties.FIELD);
    if (f.isAccessible()) {
      return new ObjectFieldModifier(f, node);
    } else {
      return new InvalidModifier(node);
    }
  }

  /**
   * Returns the modifier that match the given node
   * @param node a tree node
   */
  public LeftHandSideModifier getModifier(StaticFieldAccess node) {
    Field f = (Field)node.getProperty(NodeProperties.FIELD);
    if (f.isAccessible()) {
      return new StaticFieldModifier(f, node);
    } else {
      return new InvalidModifier(node);
    }
  }

  /**
   * Returns the modifier that match the given node
   * @param node a tree node
   */
  public LeftHandSideModifier getModifier(SuperFieldAccess node) {
    throw new IllegalStateException("internal.error");
  }

  /**
   * Returns the default argument to pass to methods in this context
   */
  public Object getHiddenArgument() {
    return null;
  }

  /**
   * Creates the tree that is associated with the given name
   * @param node the current node
   * @param name the variable name
   * @exception IllegalStateException if the variable is not defined
   */
  public Expression createName(Node node, IdentifierToken name) {

    if (!isDefined(name.image())) throw new IllegalStateException();

    List<IdentifierToken> l = new LinkedList<IdentifierToken>();
    l.add(name);
    return new QualifiedName(l);
  }

  /**
   * Looks for a class
   * @param cname the class name
   * @exception ClassNotFoundException if the class cannot be found
   */
  public Class lookupClass(String cname) throws ClassNotFoundException {
    return importationManager.lookupClass(cname, null);
  }

  /**
   * Looks for a class (context-free lookup)
   * @param cname the class name
   * @param ccname the fully qualified name of the context class
   * @exception ClassNotFoundException if the class cannot be found
   */
  public Class lookupClass(String cname, String ccname) throws ClassNotFoundException {
    return importationManager.lookupClass(cname, ccname);
  }

  /**
   * Sets the properties of a SimpleAllocation node
   * @param node  the allocation node
   * @param c the class of the constructor
   * @param cargs the classes of the arguments of the constructor
   */
  public Class setProperties(SimpleAllocation node, Class c, Class[] cargs) {
    Constructor cons = null;
    try {
      cons = lookupConstructor(c, cargs);
    }
    catch (WrongVersionException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }

    // Set the properties of this node
    node.setProperty(NodeProperties.TYPE,        c);
    node.setProperty(NodeProperties.CONSTRUCTOR, cons);
    return c;
  }

  /**
   * Sets the properties of a ClassAllocation node
   * @param node the allocation node
   * @param c the class of the constructor
   * @param args the classes of the arguments of the constructor
   * @param memb the class members
   */
  public Class setProperties(ClassAllocation node, Class c, Class[] args, List<Node> memb) {
    String cname = "TopLevel" + "$" + classCount++;
    FieldDeclaration fd;
    ConstructorDeclaration csd;

    // Create the reference to the declaring class
    fd = new FieldDeclaration(Modifier.PUBLIC | Modifier.STATIC,
                              CLASS_TYPE,
                              "declaring$Class$Reference$0",
                              OBJECT_CLASS);
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
                                     new LinkedList<ReferenceType>(),
                                     ci,
                                     stmts);
    memb.add(csd);

    // Set the inheritance
    ReferenceType ext = null;
    List<ReferenceType> impl = null;
    if (c.isInterface()) {
      impl = new LinkedList<ReferenceType>();
      List<IdentifierToken> intf = new LinkedList<IdentifierToken>();
      intf.add(new Identifier(c.getName()));
      impl.add(new ReferenceType(intf));
    } else {
      List<IdentifierToken> l = new LinkedList<IdentifierToken>();
      l.add(new Identifier(c.getName()));
      ext = new ReferenceType(l);
    }

    // Create the class
    TypeDeclaration type = new ClassDeclaration(Modifier.PUBLIC,
                                                cname,
                                                ext,
                                                impl,
                                                memb);

    type.setProperty(TreeClassInfo.ANONYMOUS_DECLARING_CLASS,
                     new JavaClassInfo(Object.class));

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
   * Creates an initializer for the variable class array used to implement
   * inner classes
   */
  protected ArrayInitializer createClassArrayInitializer() {
    List<Expression> cells = new LinkedList<Expression>();
    ArrayInitializer cell;

    Type tp = new ReferenceType(Object.class.getName());
    Map m = getConstants();
    Iterator it = m.keySet().iterator();
    while (it.hasNext()) {
      String s = (String)it.next();
      List<Expression> pair = new LinkedList<Expression>();
      pair.add(new StringLiteral('\"' + s + '\"'));
      Class c = (Class)m.get(s);
      pair.add(new TypeExpression(TreeUtilities.classToType(c)));

      cell = new ArrayInitializer(pair);
      cell.setElementType(tp);
      cells.add(cell);
    }
    tp = new ArrayType(tp, 1);
    ArrayInitializer ai = new ArrayInitializer(cells);
    ai.setElementType(tp);
    return ai;
  }

  /**
   * Looks for a constructor
   * @param c  the class of the constructor
   * @param params the parameter types
   * @exception NoSuchMethodException if the constructor cannot be found
   */
  public Constructor lookupConstructor(Class c, Class[] params)
    throws NoSuchMethodException {
    Constructor cons = ReflectionUtilities.lookupConstructor(c, params);
    setAccessFlag(cons);
    return cons;
  }

  /**
   * Invokes a constructor
   * @param node the SimpleAllocation node
   * @param args the arguments
   */
  public Object invokeConstructor(SimpleAllocation node, Object[] args) {
    Constructor cons = (Constructor) node.getProperty(NodeProperties.CONSTRUCTOR);

    try {
      return cons.newInstance(args);
    } 
    catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof Error) {
        throw (Error)e.getTargetException();
      } 
      else if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)e.getTargetException();
      }
      throw new ThrownException(e.getTargetException());
    } 
    catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Invokes a constructor
   * @param node the ClassAllocation node
   * @param args the arguments
   */
  public Object invokeConstructor(ClassAllocation node, Object[] args) {
    Constructor cons = (Constructor)node.getProperty(NodeProperties.CONSTRUCTOR);

    Object[] t = new Object[args.length + 1];
    t[0] = getConstants();
    for (int i = 1; i < t.length; i++) {
      t[i] = args[i - 1];
    }
    args = t;

    try {
      return cons.newInstance(args);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof Error) {
        throw (Error)e.getTargetException();
      } else if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)e.getTargetException();
      }
      throw new ThrownException(e.getTargetException());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
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
    Class  c = NodeProperties.getType(prefix);
    Method m = ReflectionUtilities.lookupMethod(c, mname, params);
    setAccessFlag(m);
    if (m.getName().equals("clone")) {
      m.setAccessible(true);
    }
    return m;
  }
  
  /**
   * Looks for a function
   * @param mname  the function name
   * @param params the parameter types
   * @exception NoSuchFunctionException if the function cannot be found
   */
  public MethodDeclaration lookupFunction(String mname, Class[] params)
    throws NoSuchFunctionException {
    Iterator<MethodDeclaration> it = functions.iterator();
    List<MethodDeclaration> f = new LinkedList<MethodDeclaration>();

    while (it.hasNext()) {
      MethodDeclaration md = it.next();
      if (md.getName().equals(mname)) {
        f.add(md);
      }
    }

    it = f.iterator();
    while (it.hasNext()) {
      MethodDeclaration md = it.next();
      List<FormalParameter> l =  md.getParameters();

      if (l.size() != params.length) {
        continue;
      }

      Class[] p = new Class[l.size()];
      Iterator<FormalParameter> it2 = l.iterator();
      int i = 0;
      while (it2.hasNext()) {
        p[i++] = NodeProperties.getType(it2.next());
      }

      if (ReflectionUtilities.hasCompatibleSignatures(p, params)) {
        return md;
      }
    }
       
    
    throw new NoSuchFunctionException(mname);
  }

  /**
   * Looks for a super method
   * @param node the current node
   * @param mname  the method name
   * @param params the parameter types
   * @exception NoSuchMethodException if the method cannot be find
   */
  public Method lookupSuperMethod(Node node, String mname, Class[] params)
    throws NoSuchMethodException {
    throw new ExecutionError("super.method", node);
  }

  /**
   * Looks for a field
   * @param fc the field class
   * @param fn the field name
   * @exception NoSuchFieldException if the field cannot be find
   * @exception AmbiguousFieldException if the field is ambiguous
   */
  public Field getField(Class fc, String fn) throws NoSuchFieldException,
    AmbiguousFieldException {
    Field f =  ReflectionUtilities.getField(fc, fn);
    setAccessFlag(f);
    return f;
  }

  /**
   * Looks for a field in the super class
   * @param node the current node
   * @param fn the field name
   * @exception NoSuchFieldException if the field cannot be find
   * @exception AmbiguousFieldException if the field is ambiguous
   */
  public Field getSuperField(Node node, String fn) throws NoSuchFieldException,
    AmbiguousFieldException {
    throw new ExecutionError("super.field", node);
  }

  /**
   * To test the existance of a class without loading it
   */
  protected class PseudoClassLoader extends ClassLoader {
    /**
     * Finds the specified class.
     * @param  name the name of the class
     * @return the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be find
     */
    protected Class findClass(String name) throws ClassNotFoundException {
      try {
        if (getAdditionalClassLoader() != null) {
          return Class.forName(name, true, getAdditionalClassLoader());
        }
      } catch (ClassNotFoundException e) {
      } catch (NoClassDefFoundError e) {
      }


      ClassLoader cl = (classLoader == null)
        ? interpreter.getClassLoader()
        : classLoader;
      // Was this class previously defined ?
      if ((cl instanceof TreeClassLoader) &&
          ((TreeClassLoader)cl).hasDefined(name)) {
        throw new PseudoError();
      }

      // Is there a tree associated with this name ?
      TreeClassLoader cld = (TreeClassLoader)interpreter.getClassLoader();
      TypeDeclaration td = cld.getTree(name);
      if (td != null) {
        ImportationManager im = (ImportationManager)td.getProperty
          (NodeProperties.IMPORTATION_MANAGER);

        CompilationUnitVisitor v  = new CompilationUnitVisitor(name, im);
        if (td.acceptVisitor(v).equals(Boolean.TRUE)) {
          throw new PseudoError();
        }
      }

      // Is the class tree already loaded ?
      LibraryFinder lf = interpreter.getLibraryFinder();
      try {
        String cun = lf.findCompilationUnitName(name);
        td = cld.getTree(cun);
        if (td != null) {
          ImportationManager im = (ImportationManager)td.getProperty
            (NodeProperties.IMPORTATION_MANAGER);

          CompilationUnitVisitor v  = new CompilationUnitVisitor(name, im);
          if (td.acceptVisitor(v).equals(Boolean.TRUE)) {
            throw new PseudoError();
          }
        }
      } catch (ClassNotFoundException e) {
      }

      // Load the tree
      try {
        File f = lf.findCompilationUnit(name);
        FileInputStream fis = new FileInputStream(f);

        ParserFactory pf = interpreter.getParserFactory();
        SourceCodeParser p = pf.createParser(fis, f.getCanonicalPath());
        List stmts = p.parseCompilationUnit();

        Iterator it = stmts.iterator();
        CompilationUnitVisitor v  = new CompilationUnitVisitor(name);
        boolean classFound = false;
        while (it.hasNext()) {
          if (Boolean.TRUE.equals(((Node)it.next()).acceptVisitor(v))) {
            classFound = true;
          }
        }

        if (classFound) {
          throw new PseudoError();
        }
      } catch (IOException e) {
      }
      throw new ClassNotFoundException(name);
    }
  }

  /**
   * To test the existance of a class without loading it
   */
  protected class PseudoError extends Error {
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

    // Relax the protection for members.
    if (getAccessible()) {
      ((AccessibleObject)m).setAccessible(true);
    }

    if (Modifier.isPublic(cmods) || samePkg) {
      if (Modifier.isPublic(mods)) {
        ((AccessibleObject)m).setAccessible(true);
      } else if (Modifier.isProtected(mods)) {
        if (samePkg) {
          ((AccessibleObject)m).setAccessible(true);
        }
      } else if (!Modifier.isPrivate(mods)) {
        if (samePkg) {
          ((AccessibleObject)m).setAccessible(true);
        }
      }
    }
  }

  /**
   * Gets the package name for the given class
   */
  protected String getPackageName(Class c) {
    String s = c.getName();
    int    i = s.lastIndexOf('.');
    return (i == -1) ? "" : s.substring(0, i);
  }

  /**
   * To find a class in a compilation unit
   */
  private class CompilationUnitVisitor extends VisitorObject<Boolean> {
    /**
     * The class to find
     */
    private String className;

    /**
     * The current package
     */
    private String currentPackage;

    /**
     * The importation manager
     */
    private ImportationManager importationManager;

    /**
     * The current class loader
     */
    private TreeClassLoader classLoader;

    /**
     * Creates a new visitor
     */
    public CompilationUnitVisitor(String cname) {
      className = cname;
      importationManager = new BufferedImportationManager(new PseudoClassLoader());
      classLoader = (TreeClassLoader)interpreter.getClassLoader();
    }

    /**
     * Creates a new visitor
     */
    public CompilationUnitVisitor(String cname, ImportationManager im) {
      className = cname;
      importationManager = im;
      importationManager.setClassLoader(new PseudoClassLoader());
      classLoader = (TreeClassLoader)interpreter.getClassLoader();
    }

    /**
     * Visits a PackageDeclaration
     * @param node the node to visit
     * @return null
     */
    public Boolean visit(PackageDeclaration node) {
      importationManager.setCurrentPackage(node.getName());
      return null;
    }

    /**
     * Visits an ImportDeclaration
     * @param node the node to visit
     */
    public Boolean visit(ImportDeclaration node) {
      if(node.isStatic()) {
        try {
          TigerUtilities.assertTigerEnabled("Static Import is not supported before Java 1.5");
          if(node.isStaticImportClass()) 
            importationManager.declareClassStaticImport(node.getName());
          else 
            importationManager.declareMemberStaticImport(node.getName());
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Uncaught ClassNotFoundException has been thrown: " + e.toString());
        }
      }        
      else {
        // Declare the package or class importation
        if (node.isPackage()) {
          importationManager.declarePackageImport(node.getName());
        } else {
          try {
            importationManager.declareClassImport(node.getName());
          } catch (ClassNotFoundException e) {
            throw new CatchedExceptionError(e, node);
          } catch (PseudoError e) {
          }
        }
      }
      return null;
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Boolean visit(ClassDeclaration node) {
      return visitType(node);
    }

    /**
     * Visits an InterfaceDeclaration
     * @param node the node to visit
     */
    public Boolean visit(InterfaceDeclaration node) {
      return visitType(node);
    }

    /**
     * visits a TypeDeclaration
     */
    private Boolean visitType(TypeDeclaration node) {
      String cname = importationManager.getCurrentPackage();
      cname = ((cname.equals("")) ? "" : cname + "." ) + node.getName();
      classLoader.addTree(cname, node);
      node.setProperty(NodeProperties.IMPORTATION_MANAGER, importationManager);
      if (className.equals(cname)) {
        return Boolean.TRUE;
      } else {
        MembersVisitor v = new MembersVisitor(cname);
        Iterator<Node> it = node.getMembers().iterator();
        while (it.hasNext()) {
          Boolean b = it.next().acceptVisitor(v);
          if (b.booleanValue()) return Boolean.TRUE;
        }
        return Boolean.FALSE;
      }
    }

    /**
     * To find a class in a compilation unit
     */
    private class MembersVisitor extends VisitorObject<Boolean> {
      /**
       * The outer class
       */
      private String outerName;

      /**
       * Creates a new visitor
       */
      public MembersVisitor(String cname) {
        outerName = cname;
      }

      /**
       * Visits a ClassDeclaration
       * @param node the node to visit
       */
      public Boolean visit(ClassDeclaration node) {
        return visitType(node);
      }

      /**
       * Visits an InterfaceDeclaration
       * @param node the node to visit
       */
      public Boolean visit(InterfaceDeclaration node) {
        return visitType(node);
      }

      /**
       * visits a TypeDeclaration
       */
      private Boolean visitType(TypeDeclaration node) {
        if (className.equals(outerName + "$" + node.getName())) {
          return Boolean.TRUE;
        } else {
          MembersVisitor v = new MembersVisitor(outerName + "$" + node.getName());
          Iterator<Node> it = node.getMembers().iterator();
          while (it.hasNext()) {
            Boolean b = it.next().acceptVisitor(v);
            if (b.booleanValue()) return Boolean.TRUE;
          }
          return Boolean.FALSE;
        }
      }
    }
  }
}
