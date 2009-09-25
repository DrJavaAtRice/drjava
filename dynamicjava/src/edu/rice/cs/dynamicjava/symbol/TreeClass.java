package edu.rice.cs.dynamicjava.symbol;

import java.util.List;
import java.util.LinkedList;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.NodeProperties;

import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;
import edu.rice.cs.dynamicjava.symbol.type.SimpleClassType;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.TreeClassLoader;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** 
 * <p>A DJClass wrapper for a parsed class or interface declaration.</p>
 * <p>A DJClass object must be available before any types can be created in terms of the class.
 * Thus all class declarations introduced in some scope must have corresponding DJClasses before 
 * processing the supertypes and type parameters, etc., of those declarations.  Here we handle
 * much of this process by recursively creating all members of the given class at the time of
 * creation, and tagging the declarations with these new objects.  The members of the resulting DJClass 
 * are immediately available; type parameters and supertypes are initialized with stub Object types
 * until the actual types can be resolved (note that, until actual types are resolved, the results
 * of a member lookup on a TreeClass will be incorrect; but, as complex dependencies sometimes exist
 * between name and supertype resolution, this stub information is sometimes useful).
 */
public class TreeClass implements DJClass {
  
  private static final Type RUNTIME_BINDINGS_TYPE = 
    new SimpleClassType(SymbolUtil.wrapClass(RuntimeBindings.class));
  
  private final String _fullName;
  private final DJClass _declaring; // may be null
  private final Access.Module _accessModule;
  private final Node _ast;
  private final ModifierSet _mods;
  private final Thunk<Class<?>> _loaded;
  private final List<TreeConstructor> _constructors;
  private final List<TreeField> _fields;
  private final List<TreeMethod> _methods;
  private final List<TreeClass> _classes;
  private final Options _opt;
  
  /**
   * All the class's declared members are indexed, and the FIELD, METHOD, and DJ_CLASS properties are set
   * (applied recursively). 
   * @param fullName  The fully-qualified name of the class, as in {@link Class#getName}.
   * @param declaring  The declaring class of this class, or null if it appears at a top level or local
   *                   scope.
   * @param accessModule  The access module for this class, or null if it is its own access module
   * @param ast  The parsed declaration of this class.  Must be a TypeDeclaration, AnonymousAllocation,
   *             or AnonymousInnerAllocation.
   * @param loader  A class loader for compiling and loading this class.  Note that the loader must
   *                be defined to allow transitive loading of all referenced classes.
   */
  public TreeClass(String fullName, DJClass declaring, Access.Module accessModule, Node ast,
                   final TreeClassLoader loader, Options opt) {
    _fullName = fullName;
    _declaring = declaring;
    _accessModule = (accessModule == null) ? this : accessModule;
    _ast = ast;
    if (_ast instanceof TypeDeclaration) { _mods = ((TypeDeclaration) _ast).getModifiers(); }
    else { _mods = ModifierSet.make(); }
    _loaded = LazyThunk.make(new Thunk<Class<?>>() {
      public Class<?> value() {
        try { return loader.loadClass(_fullName); }
        catch (ClassNotFoundException e) { throw new RuntimeException("Error loading class", e); }
        // LinkageError indicates there's something wrong with the compiled class
        catch (LinkageError e) { throw new RuntimeException("Error loading class", e); }
      }
    });
    _constructors = new LinkedList<TreeConstructor>();
    _fields = new LinkedList<TreeField>();
    _methods = new LinkedList<TreeMethod>();
    _classes = new LinkedList<TreeClass>();
    _opt = opt;
    loader.registerTree(this);
    tagSignature();
    extractMembers(loader);
  }
  
  /** Set the TYPE and TYPE_VARIABLE properties of of non-anonymous classes' signatures to stub Object types. */
  private void tagSignature() {
    if (_ast instanceof TypeDeclaration) {
      TypeDeclaration td = (TypeDeclaration) _ast;

      TypeParameter[] tparams;
      if (td instanceof GenericClassDeclaration) {
        tparams = ((GenericClassDeclaration) td).getTypeParameters();
      }
      else if (td instanceof GenericInterfaceDeclaration) {
        tparams = ((GenericInterfaceDeclaration) td).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      for (TypeParameter p : tparams) {
        BoundedSymbol tempBounds = new BoundedSymbol(new Object(), p.getRepresentation(),
                                                     TypeSystem.OBJECT, TypeSystem.NULL);
        NodeProperties.setTypeVariable(p, new VariableType(tempBounds));
      }

      if (td instanceof ClassDeclaration) {
        NodeProperties.setType(((ClassDeclaration) td).getSuperclass(), TypeSystem.OBJECT);
      }
      if (td.getInterfaces() != null) {
        for (ReferenceTypeName tn : td.getInterfaces()) { NodeProperties.setType(tn, TypeSystem.OBJECT); }
      }
    }
  }
  
  private void extractMembers(final TreeClassLoader loader) {
    Iterable<Node> members = IterUtil.empty();
    if (_ast instanceof TypeDeclaration) { members = ((TypeDeclaration) _ast).getMembers(); }
    else if (_ast instanceof AnonymousAllocation) {
      members = ((AnonymousAllocation) _ast).getMembers();
    }
    else if (_ast instanceof AnonymousInnerAllocation) {
      members = ((AnonymousInnerAllocation) _ast).getMembers();
    }
    for (Node n : members) {
      n.acceptVisitor(new AbstractVisitor<Void>() {
        @Override public Void defaultCase(Node n) { return null; /* ignore other declarations */ }
        @Override public Void visit(ClassDeclaration d) {
          TreeClass c = new TreeClass(_fullName + "$" + d.getName(), TreeClass.this, _accessModule, d, loader, _opt);
          NodeProperties.setDJClass(d, c);
          _classes.add(c);
          return null;
        }
        @Override public Void visit(InterfaceDeclaration d) {
          TreeClass c = new TreeClass(_fullName + "$" + d.getName(), TreeClass.this, _accessModule, d, loader, _opt);
          NodeProperties.setDJClass(d, c);
          _classes.add(c);
          return null;
        }
        @Override public Void visit(ConstructorDeclaration d) {
          TreeConstructor k = new ExplicitTreeConstructor(d);
          NodeProperties.setConstructor(d, k);
          _constructors.add(k);
          return null;
        }
        @Override public Void visit(MethodDeclaration d) {
          TreeMethod m = new TreeMethod(d);
          NodeProperties.setMethod(d, m);
          _methods.add(m);
          return null;
        }
        @Override public Void visit(FieldDeclaration d) {
          TreeField f = new TreeField(d);
          NodeProperties.setField(d, f);
          _fields.add(f);
          return null;
        }
      });
    }
    if (_constructors.isEmpty() && !(_ast instanceof InterfaceDeclaration)) {
      _constructors.add(new DefaultTreeConstructor());
    }
  }
  
  public Node declaration() { return _ast; }
  
  public String packageName() {
    // can't delegate to _accessModule, because it may be this
    int dot = _fullName.lastIndexOf('.');
    if (dot == -1) { return ""; }
    else { return _fullName.substring(0, dot); }
  }
  
  /** Produces the binary name for the given class (as in {@link Class#getName}) */
  public String fullName() { return _fullName; }
  
  public boolean isAnonymous() { return !(_ast instanceof TypeDeclaration); }
  
  public String declaredName() {
    if (_ast instanceof TypeDeclaration) {
      return ((TypeDeclaration) _ast).getName();
    }
    else { throw new IllegalArgumentException("Anonymous class has no declared name"); }
  }
  
  public boolean isInterface() { return _ast instanceof InterfaceDeclaration; }
  
  public boolean isStatic() {
    if (_declaring == null) { return false; }
    else { return _declaring.isInterface() || isInterface() || _ast instanceof EnumDeclaration || _mods.isStatic(); }
  }
  public boolean isAbstract() { return _mods.isAbstract(); }
  public boolean isFinal() { return _mods.isFinal(); }
  public Access accessibility() { return extractAccessibility(_mods); }
  public Access.Module accessModule() { return _accessModule; }
  public boolean hasRuntimeBindingsParams() { return true; }
  
  /** The class that declares this class, or {@code null} if this is declared at a top-level or local scope */
  public DJClass declaringClass() { return _declaring; }
  
  /** List all type variables declared by this class */
  public Iterable<VariableType> declaredTypeParameters() {
    Iterable<TypeParameter> paramAsts = IterUtil.empty();
    if (_ast instanceof GenericClassDeclaration) {
      paramAsts = IterUtil.asIterable(((GenericClassDeclaration)_ast).getTypeParameters());
    }
    else if (_ast instanceof GenericInterfaceDeclaration) {
      paramAsts = IterUtil.asIterable(((GenericInterfaceDeclaration)_ast).getTypeParameters());
    }
    return IterUtil.mapSnapshot(paramAsts, NodeProperties.NODE_TYPE_VARIABLE);
  }
  
  /** List the declared supertypes of this class */
  public Iterable<Type> declaredSupertypes() {
    if (_ast instanceof ClassDeclaration) {
      ClassDeclaration cd = (ClassDeclaration) _ast;
      Iterable<Type> superIs;
      if (cd.getInterfaces() == null) { superIs = IterUtil.empty(); }
      else { superIs = IterUtil.mapSnapshot(cd.getInterfaces(), NodeProperties.NODE_TYPE); }
      return IterUtil.compose(NodeProperties.getType(cd.getSuperclass()), superIs);
    }
    else if (_ast instanceof InterfaceDeclaration) {
      InterfaceDeclaration id = (InterfaceDeclaration) _ast;
      if (id.getInterfaces() == null) { return IterUtil.empty(); }
      else { return IterUtil.mapSnapshot(id.getInterfaces(), NodeProperties.NODE_TYPE); }
    }
    else if (_ast instanceof AnonymousAllocation) {
      return IterUtil.singleton(NodeProperties.getType(((AnonymousAllocation) _ast).getCreationType()));
    }
    else if (_ast instanceof AnonymousInnerAllocation) {
      return IterUtil.singleton(NodeProperties.getSuperType(_ast));
    }
    else { throw new IllegalArgumentException("Unsupported class AST type"); }
  }
  
  public Iterable<DJField> declaredFields() { return IterUtil.<DJField>immutable(_fields); }
  
  public Iterable<DJConstructor> declaredConstructors() { return IterUtil.<DJConstructor>immutable(_constructors); }
  
  public Iterable<DJMethod> declaredMethods() { return IterUtil.<DJMethod>immutable(_methods); }
  
  public Iterable<DJClass> declaredClasses() { return IterUtil.<DJClass>immutable(_classes); }
  
  /**
   * @return  The type bound to {@code super} in the context of this class, or 
   *          {@code null} if {@code super} is not defined
   */
  public Type immediateSuperclass() {
    if (_ast instanceof ClassDeclaration) {
      // ClassDeclaration provides the default java.lang.Object where there's no extends clause
      return NodeProperties.getType(((ClassDeclaration)_ast).getSuperclass());
    }
    else if (_ast instanceof InterfaceDeclaration) {
      return TypeSystem.OBJECT;
    }
    else if (_ast instanceof AnonymousAllocation) {
      Type result = NodeProperties.getType(((AnonymousAllocation) _ast).getCreationType());
      if (result instanceof ClassType && !((ClassType) result).ofClass().isInterface()) {
        return result;
      }
      else { return TypeSystem.OBJECT; }
    }
    else if (_ast instanceof AnonymousInnerAllocation) {
      return NodeProperties.getSuperType(_ast);
    }
    else { throw new IllegalArgumentException("Unsupported class AST type"); }
  }
  
  /**
   * Produce the runtime representation of the class (as in {@link ClassLoader#loadClass},
   * repeated invocations should produce the same object).
   */
  public Class<?> load() { return _loaded.value(); }
  
  public String toString() { return "TreeClass(" + _fullName + ")"; }
  
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (!o.getClass().equals(getClass())) { return false; }
    else { return _ast == ((TreeClass) o)._ast; }
  }
  
  public int hashCode() { return getClass().hashCode() ^ System.identityHashCode(_ast); }
  
  
  private class TreeField implements DJField {
    private final FieldDeclaration _f;
    private final Thunk<DJField> _loaded;
    public TreeField(FieldDeclaration f) {
      _f = f;
      _loaded = LazyThunk.make(new Thunk<DJField>() {
        public DJField value() {
          DJClass c = SymbolUtil.wrapClass(TreeClass.this.load());
          for (DJField candidate : c.declaredFields()) {
            if (TreeField.this.declaredName().equals(candidate.declaredName())) {
              return candidate;
            }
          }
          // error: can't find it
          debug.logValues(new String[]{"name", "candidates"},
                          TreeField.this.declaredName(), c.declaredFields());
          throw new RuntimeException("Can't find field in loaded class");
        }
      });
    }
    public String declaredName() { return _f.getName(); }
    public DJClass declaringClass() { return TreeClass.this; }
    public Type type() { return NodeProperties.getType(_f.getType()); }
    public boolean isFinal() { return _f.getModifiers().isFinal() || isInterface(); }
    public boolean isStatic() { return _f.getModifiers().isStatic() || isInterface(); }
    public Access accessibility() {
      return isInterface() ? Access.PUBLIC : extractAccessibility(_f.getModifiers());
    }
    public Access.Module accessModule() { return _accessModule; }
    public Option<Object> constantValue() {
      if (isFinal() && isStatic()) {
        Expression init = _f.getInitializer();
        if (init != null) {
          if (NodeProperties.hasValue(init)) { return Option.some(NodeProperties.getValue(init)); }
          // Since hasValue depends on the order of type checking, and the current order is
          // naive, also allow values for literals that haven't yet been checked
          // (this still produces incorrect results for non-literal, not-yet-checked constant expressions)
          else if (init instanceof Literal) { return Option.some(((Literal) init).getValue()); }
        }
      }
      return Option.none();
    }
    public Box<Object> boxForReceiver(Object receiver) {
      return _loaded.value().boxForReceiver(receiver);
    }
    public String toString() { return "TreeField(" + declaredName() + ")"; }
  }
  
  
  private abstract class TreeConstructor implements DJConstructor {
    private final Thunk<DJConstructor> _loaded;
    private final DJClass _outerClass;
    
    public TreeConstructor() {
      _loaded = LazyThunk.make(new Thunk<DJConstructor>() {
        public DJConstructor value() {
          Iterable<LocalVariable> params = TreeConstructor.this.parameters();
          if (_outerClass == null) {
            params = IterUtil.compose(new LocalVariable("", RUNTIME_BINDINGS_TYPE, false), params);
          }
          DJClass c = SymbolUtil.wrapClass(TreeClass.this.load());
          for (DJConstructor candidate : c.declaredConstructors()) {
            if (paramsMatch(params, candidate.parameters())) {
              return candidate;
            }
          }
          // error: can't find it
          debug.logValues(new String[]{"params", "candidates"}, params, c.declaredConstructors());
          throw new RuntimeException("Can't find constructor in loaded class");
        }
      });
      _outerClass = SymbolUtil.dynamicOuterClass(TreeClass.this);
    }
    
    public String declaredName() { return TreeClass.this.declaredName(); }
    public DJClass declaringClass() { return TreeClass.this; }
    public Access.Module accessModule() { return _accessModule; }
    public Type returnType() { return SymbolUtil.thisType(TreeClass.this); }
    
    public DJConstructor declaredSignature() { return this; }
    public Object evaluate(Object outer, Iterable<Object> args, RuntimeBindings bindings, Options options) 
      throws EvaluatorException {
      if (_outerClass == null) { args = IterUtil.compose(bindings, args); }
      return _loaded.value().evaluate(outer, args, bindings, options);
    }
    public String toString() { return "TreeConstructor(" + declaredName() + ")"; }
  }
  
  
  private class DefaultTreeConstructor extends TreeConstructor {
    public Access accessibility() { return Access.PUBLIC; }
    public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
    public Iterable<LocalVariable> parameters() { return IterUtil.empty(); }
    public Iterable<Type> thrownTypes() { return IterUtil.empty(); }
  }
  
  
  private class ExplicitTreeConstructor extends TreeConstructor {
    private final ConstructorDeclaration _k;
    public ExplicitTreeConstructor(ConstructorDeclaration k) {
      _k = k;
    }
    public Access accessibility() { return extractAccessibility(_k.getModifiers()); }
    public Iterable<VariableType> typeParameters() {
      if (_k instanceof PolymorphicConstructorDeclaration) {
        TypeParameter[] ps = ((PolymorphicConstructorDeclaration)_k).getTypeParameters();
        return IterUtil.mapSnapshot(IterUtil.asIterable(ps), NodeProperties.NODE_TYPE_VARIABLE);
      }
      else { return IterUtil.empty(); }
    }
    public Iterable<LocalVariable> parameters() {
      return IterUtil.mapSnapshot(_k.getParameters(), NodeProperties.NODE_VARIABLE);
    }
    public Iterable<Type> thrownTypes() {
      return IterUtil.mapSnapshot(_k.getExceptions(), NodeProperties.NODE_TYPE);
    }
  }

  
  private class TreeMethod implements DJMethod {
    private MethodDeclaration _m;
    private Thunk<DJMethod> _loaded;
    
    public TreeMethod(MethodDeclaration m) {
      _m = m;
      _loaded = LazyThunk.make(new Thunk<DJMethod>() {
        public DJMethod value() {
          Iterable<LocalVariable> params = TreeMethod.this.parameters();
          if (TreeMethod.this.isStatic()) {
            params = IterUtil.compose(new LocalVariable("", RUNTIME_BINDINGS_TYPE, false), params);
          }
          DJClass c = SymbolUtil.wrapClass(TreeClass.this.load());
          for (DJMethod candidate : c.declaredMethods()) {
            if (TreeMethod.this.declaredName().equals(candidate.declaredName()) &&
                paramsMatch(params, candidate.parameters())) {
              return candidate;
            }
          }
          // error: can't find it
          debug.logValues(new String[]{"name", "params", "candidates"},
                          TreeMethod.this.declaredName(), params, c.declaredMethods());
          throw new RuntimeException("Can't find method in loaded class");
        }
      });
    }
    
    public String declaredName() { return _m.getName(); }
    public DJClass declaringClass() { return TreeClass.this; }
    public boolean isStatic() { return _m.getModifiers().isStatic(); }
    public boolean isAbstract() { return _m.getModifiers().isAbstract() || isInterface(); }
    public boolean isFinal() { return _m.getModifiers().isFinal(); }
    public Access accessibility() {
      return isInterface() ? Access.PUBLIC : extractAccessibility(_m.getModifiers());
    }
    public Access.Module accessModule() { return _accessModule; }
    
    public Type returnType() { return NodeProperties.getType(_m.getReturnType()); }
      
    public Iterable<VariableType> typeParameters() {
      if (_m instanceof PolymorphicMethodDeclaration) {
        TypeParameter[] ps = ((PolymorphicMethodDeclaration)_m).getTypeParameters();
        return IterUtil.mapSnapshot(IterUtil.asIterable(ps), NodeProperties.NODE_TYPE_VARIABLE);
      }
      else { return IterUtil.empty(); }
    }
    
    public Iterable<LocalVariable> parameters() {
      return IterUtil.mapSnapshot(_m.getParameters(), NodeProperties.NODE_VARIABLE);
    }
    
    public Iterable<Type> thrownTypes() {
      return IterUtil.mapSnapshot(_m.getExceptions(), NodeProperties.NODE_TYPE);
    }
    
    public DJMethod declaredSignature() { return this; }
    
    public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, Options options) 
      throws EvaluatorException {
      if (isStatic()) { args = IterUtil.compose(bindings, args); }
      return _loaded.value().evaluate(receiver, args, bindings, options);
    }
    
    public String toString() { return "TreeMethod(" + declaredName() + ")"; }
  }
    
  
  /** Convert a reflection modifier int to an appropriate Access object */
  private static Access extractAccessibility(ModifierSet mods) {
    if (mods.isPublic()) { return Access.PUBLIC; }
    else if (mods.isProtected()) { return Access.PROTECTED; }
    else if (mods.isPrivate()) { return Access.PRIVATE; }
    else { return Access.PACKAGE; }
  }
  
  /**
   * Assumes the classes corresponding to the types of the local variables can be loaded.
   * Non-static because it depends on _opt.
   */
  private boolean paramsMatch(Iterable<LocalVariable> p1, Iterable<LocalVariable> p2) {
    if (IterUtil.sizeOf(p1) == IterUtil.sizeOf(p2)) {
      TypeSystem ts = _opt.typeSystem();
      for (Pair<LocalVariable, LocalVariable> vars : IterUtil.zip(p1, p2)) {
        Thunk<Class<?>> c1 = ts.erasedClass(vars.first().type());
        Thunk<Class<?>> c2 = ts.erasedClass(vars.second().type());
        if (!c1.value().equals(c2.value())) {
          return false;
        }
      }
      return true;
    }
    else { return false; }
  }
  
}
