package edu.rice.cs.dynamicjava.interpreter;

import java.util.*;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SequenceIterator;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** The context at the top level.  Manages package and import statements, and
  * loading external classes.
  */
public class TopLevelContext implements TypeContext {

  private final ClassLoader _loader;
  private final String _currentPackage;
  private final Iterator<Integer> _anonymousCounter;
    
  // The following fields refer to specific implementation classes in order to use the clone() method.
  
  /** Packages whose top-level classes are all imported */
  private final HashSet<String> _onDemandPackages;
  /** Classes whose member classes are all imported */
  private final HashSet<DJClass> _onDemandClasses;
  /** Classes whose static members (fields, methods, and classes) are all imported */
  private final HashSet<DJClass> _staticOnDemandClasses;
  
  /** Top-level classes that are individually imported */
  private final HashMap<String, DJClass> _importedTopLevelClasses;
  /** Classes containing an individually-imported member class */
  private final HashMap<String, DJClass> _importedMemberClasses;
  /** Classes containing an individually-imported field */
  private final HashMap<String, DJClass> _importedFields;
  /** Classes containing an individually-imported method */
  private final HashMap<String, DJClass> _importedMethods;
  
  /** The context is initialized with an on-demand import of "java.lang"  */
  public TopLevelContext(ClassLoader loader) {
    _loader = loader;
    _currentPackage = "";
    _anonymousCounter = new SequenceIterator<Integer>(1, LambdaUtil.INCREMENT_INT);
    _onDemandPackages = new HashSet<String>();
    _onDemandClasses = new HashSet<DJClass>();
    _staticOnDemandClasses = new HashSet<DJClass>();
    _importedTopLevelClasses = new HashMap<String, DJClass>();
    _importedMemberClasses = new HashMap<String, DJClass>();
    _importedFields = new HashMap<String, DJClass>();
    _importedMethods = new HashMap<String, DJClass>();
    
    _onDemandPackages.add("java.lang");
  }
  
  private TopLevelContext(TopLevelContext copy) {
    this(copy._loader, copy._currentPackage, copy);
  }
  
  @SuppressWarnings("unchecked")
  private TopLevelContext(ClassLoader loader, String currentPackage, TopLevelContext bindings) {
    _loader = loader;
    _currentPackage = currentPackage;
    _anonymousCounter = bindings._anonymousCounter;
    _onDemandPackages = (HashSet<String>) bindings._onDemandPackages.clone();
    _onDemandClasses = (HashSet<DJClass>) bindings._onDemandClasses.clone();
    _staticOnDemandClasses = (HashSet<DJClass>) bindings._staticOnDemandClasses.clone();
    _importedTopLevelClasses = (HashMap<String, DJClass>) bindings._importedTopLevelClasses.clone();
    _importedMemberClasses = (HashMap<String, DJClass>) bindings._importedMemberClasses.clone();
    _importedFields = (HashMap<String, DJClass>) bindings._importedFields.clone();
    _importedMethods = (HashMap<String, DJClass>) bindings._importedMethods.clone();
  }
  
  /* PACKAGE AND IMPORT MANAGEMENT */
  
  /** Set the current package to the given package name */
  public TypeContext setPackage(String name) { return new TopLevelContext(_loader, name, this); }
  
  /** Import on demand all top-level classes in the given package */
  public TypeContext importTopLevelClasses(String pkg) {
    TopLevelContext result = new TopLevelContext(this);
    result._onDemandPackages.add(pkg);
    return result;
  }
  
  /** Import on demand all member classes of the given class */
  public TypeContext importMemberClasses(DJClass outer) {
    TopLevelContext result = new TopLevelContext(this);
    result._onDemandClasses.add(outer);
    return result;
  }    
  
  /** Import on demand all static members of the given class */
  public TypeContext importStaticMembers(DJClass c) {
    TopLevelContext result = new TopLevelContext(this);
    result._staticOnDemandClasses.add(c);
    return result;
  }
  
  /** Import the given top-level class */
  public TypeContext importTopLevelClass(DJClass c) {
    TopLevelContext result = new TopLevelContext(this);
    String name = c.declaredName();
    // Under strict circumstances, a duplicate import for a name is illegal, but DynamicJava allows it
    result._importedMemberClasses.remove(name);
    result._importedTopLevelClasses.put(name, c);
    return result;
  }
  
  /** Import the member class(es) of {@code outer} with the given name */
  public TypeContext importMemberClass(DJClass outer, String name) {
    TopLevelContext result = new TopLevelContext(this);
    // Under strict circumstances, a duplicate import for a name is illegal, but DynamicJava allows it
    result._importedTopLevelClasses.remove(name);
    result._importedMemberClasses.put(name, outer);
    return result;
  }
  
  /** Import the field(s) of {@code c} with the given name */
  public TypeContext importField(DJClass c, String name) {
    TopLevelContext result = new TopLevelContext(this);
    // Under strict circumstances, a duplicate import for a name is illegal, but DynamicJava allows it
    result._importedFields.put(name, c);
    return result;
  }
  
  /** Import the method(s) of {@code c} with the given name */
  public TypeContext importMethod(DJClass c, String name) {
    TopLevelContext result = new TopLevelContext(this);
    // Under strict circumstances, a duplicate import for a name is illegal, but DynamicJava allows it
    result._importedMethods.put(name, c);
    return result;
  }
    
  
  /* TYPES: TOP-LEVEL CLASSES, MEMBER CLASSES, AND TYPE VARIABLES */
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  public boolean typeExists(String name, TypeSystem ts) {
    return topLevelClassExists(name, ts) || memberClassExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope top-level class */
  public boolean topLevelClassExists(String name, TypeSystem ts) {
    try { return getTopLevelClass(name, ts) != null; }
    catch (AmbiguousNameException e) { return true; }
  }
  
  /**
   * Return the top-level class with the given name, or {@code null} if it does not exist.  If the name 
   * contains a {@code '.'}, it is assumed to be a fully-qualified name; otherwise, the result relies on 
   * the current import and package settings.  Note that a member class may shadow a top-level class, 
   * resulting in a null result here.
   */
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    if (TextUtil.contains(name, '.')) {
      try { return SymbolUtil.wrapClass(_loader.loadClass(name)); }
      catch (ClassNotFoundException e) { return null; }
    }
    else {
      DJClass result = _importedTopLevelClasses.get(name);
      if (result == null) {
        try { result = SymbolUtil.wrapClass(_loader.loadClass(makeClassName(name))); }
        catch (ClassNotFoundException e) {
          LinkedList<Class<?>> onDemandMatches = new LinkedList<Class<?>>();
          for (String p : _onDemandPackages) {
            try { onDemandMatches.add(_loader.loadClass(p + "." + name)); }
            catch (ClassNotFoundException e2) { /* ignore -- class is not in this package */ }
          }
          if (onDemandMatches.size() > 1) { throw new AmbiguousNameException(); }
          else if (onDemandMatches.size() == 1) { result = SymbolUtil.wrapClass(onDemandMatches.getFirst()); }
        }
      }
      return result;
    }
  }
  
  /** Test whether {@code name} is an in-scope member class */
  public boolean memberClassExists(String name, TypeSystem ts) {
    try { return typeContainingMemberClass(name, ts) != null; }
    catch (AmbiguousNameException e) { return true; }
  }
  
  /**
   * Return the most inner type containing a class with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    DJClass explicitImport = _importedMemberClasses.get(name);
    Type result = explicitImport == null ? null : ts.makeClassType(explicitImport);
    if (result == null) {
      LinkedList<Type> onDemandMatches = new LinkedList<Type>();
      for (DJClass c : _onDemandClasses) {
        Type t = ts.makeClassType(c);
        if (ts.containsClass(t, name)) { onDemandMatches.add(t); }
      }
      for (DJClass c : _staticOnDemandClasses) {
        Type t = ts.makeClassType(c);
        if (ts.containsStaticClass(t, name)) { onDemandMatches.add(t); }
      }
      if (onDemandMatches.size() > 1) { throw new AmbiguousNameException(); }
      else if (onDemandMatches.size() == 1) { result = onDemandMatches.getFirst(); }
    }
    return result;
  }
  
  /** Test whether {@code name} is an in-scope type variable. */
  public boolean typeVariableExists(String name, TypeSystem ts) {
    return false;
  }
  
  /** Return the type variable with the given name, or {@code null} if it does not exist. */
  public Type getTypeVariable(String name, TypeSystem ts) {
    return null;
  }
  

  /* VARIABLES: FIELDS AND LOCAL VARIABLES */  
  
  /** Test whether {@code name} is an in-scope field or local variable */
  public boolean variableExists(String name, TypeSystem ts) {
    return fieldExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope field */
  public boolean fieldExists(String name, TypeSystem ts) {
    try { return typeContainingField(name, ts) != null; }
    catch (AmbiguousNameException e) { return true; }
  }
    
  
  /**
   * Return the most inner type containing a field with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException {
    DJClass explicitImport = _importedFields.get(name);
    Type result = explicitImport == null ? null : ts.makeClassType(explicitImport);
    if (result == null) {
      LinkedList<Type> onDemandMatches = new LinkedList<Type>();
      for (DJClass c : _staticOnDemandClasses) {
        Type t = ts.makeClassType(c);
        if (ts.containsStaticField(t, name)) { onDemandMatches.add(t); }
      }
      if (onDemandMatches.size() > 1) { throw new AmbiguousNameException(); }
      else if (onDemandMatches.size() == 1) { result = onDemandMatches.getFirst(); }
    }
    return result;
  }
  
  /** Test whether {@code name} is an in-scope local variable */
  public boolean localVariableExists(String name, TypeSystem ts) {
    return false;
  }
  
  /** Return the variable object for the given name, or {@code null} if it does not exist. */
  public LocalVariable getLocalVariable(String name, TypeSystem ts) {
    return null;
  }
  
  
  /* METHODS */
  
  /** Test whether {@code name} is an in-scope method or local function */
  public boolean functionExists(String name, TypeSystem ts) {
    return methodExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope method */
  public boolean methodExists(String name, TypeSystem ts) {
    try { return typeContainingMethod(name, ts) != null; }
    catch (AmbiguousNameException e) { return true; }
  }
  
  /**
   * Return the most inner type containing a method with the given name, or {@code null}
   * if there is no such type.
   */
  public Type typeContainingMethod(String name, TypeSystem ts) throws AmbiguousNameException {
    DJClass explicitImport = _importedMethods.get(name);
    Type result = explicitImport == null ? null : ts.makeClassType(explicitImport);
    if (result == null) {
      LinkedList<Type> onDemandMatches = new LinkedList<Type>();
      for (DJClass c : _staticOnDemandClasses) {
        Type t = ts.makeClassType(c);
        if (ts.containsStaticMethod(t, name)) { onDemandMatches.add(t); }
      }
      if (onDemandMatches.size() > 1) { throw new AmbiguousNameException(); }
      else if (onDemandMatches.size() == 1) { result = onDemandMatches.getFirst(); }
    }
    return result;
  }
  
  public boolean localFunctionExists(String name, TypeSystem ts) {
    return false;
  }
  
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts) {
    return IterUtil.empty();
  }
  
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts, Iterable<LocalFunction> partial) {
    return partial;
  }
  
  
  /* MISC CONTEXTUAL INFORMATION */
  
  /** Return a full name for a class with the given name declared here. */
  public String makeClassName(String n) { return _currentPackage.equals("") ? n : _currentPackage + "." + n; }
  
  /** Return a full name for an anonymous class declared here. */
  public String makeAnonymousClassName() { return makeClassName("$" + _anonymousCounter.next().toString()); }
  
  /**
   * Return the type of {@code this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis() { return null; }
  
  /**
   * Return the type of {@code className.this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  public DJClass getThis(String className) { return null; }
  
  /**
   * Return the type referenced by {@code super} in the current context, or {@code null}
   * if there is no such type (for example, in a static context).
   */
  public Type getSuperType(TypeSystem ts) { return null; }
  
  /**
   * The expected type of a {@code return} statement in the given context, or {@code null}
   * if {@code return} statements should not appear here.
   */
  public Type getReturnType() { return null; }
  
  /**
   * The types that are allowed to be thrown in the current context.  If there is no
   * such declaration, the list will be empty.
   */
  public Iterable<Type> getDeclaredThrownTypes() {
    // the top level "catches" anything that is thrown.
    return IterUtil.<Type>singleton(TypeSystem.THROWABLE);
  }
  
  public ClassLoader getClassLoader() { return _loader; }
  
}
