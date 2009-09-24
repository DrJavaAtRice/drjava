package edu.rice.cs.dynamicjava.interpreter;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

import static koala.dynamicjava.interpreter.NodeProperties.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Checks the members of a class declaration.
 * The following are set:<ul>
 * <li>ERASED_TYPE on all {@link MethodDeclaration}s</li>
 * </ul>
 */
public class ClassChecker {
  
  private final DJClass _c;
  private final ClassLoader _loader;
  private final TypeContext _context;
  private final Options _opt;
  
  public ClassChecker(DJClass c, ClassLoader loader, TypeContext context, Options opt) {
    _c = c;
    _loader = loader;
    _context = context;
    _opt = opt;
  }
  
  /**
   * Initialize and check the structure of types in the signatures of the given class and any nested
   * classes.  Requires the {@code DJ_CLASS} property to be set on all referenced classes; after this
   * step, the type hierarchy may safely be traversed.  Types aren't guaranteed to be well-formed.
   */
  public void initializeClassSignatures(TypeDeclaration ast) {
    TypeContext sigContext = new ClassSignatureContext(_context, _c, _loader);
    TypeNameChecker sigChecker = new TypeNameChecker(sigContext, _opt);
    debug.logStart("Initializing type parameters", "class", ast.getName());
    try { sigChecker.checkStructureForTypeParameters(typeParameters(ast)); }
    finally { debug.logEnd(); }
    debug.logStart("Initializing supertypes", "class", ast.getName());
    try {
      if (ast instanceof ClassDeclaration) {
        sigChecker.checkStructure(((ClassDeclaration) ast).getSuperclass());
      }
      if (ast.getInterfaces() != null) {
        for (TypeName tn : ast.getInterfaces()) { sigChecker.checkStructure(tn); }
      }
    }
    finally { debug.logEnd(); }
    initializeNestedClassSignatures(ast.getMembers(), sigContext);
  }
  
  /**
   * Initialize and check the structure of types in the signatures of the given class and any nested
   * classes.  Requires the {@code DJ_CLASS} property to be set on all referenced classes; after this
   * step, the type hierarchy may safely be traversed.  Types aren't guaranteed to be well-formed.
   */
  public void initializeClassSignatures(AnonymousAllocation ast) {
    initializeNestedClassSignatures(ast.getMembers(), new ClassSignatureContext(_context, _c, _loader));
  }
   
  /**
   * Initialize and check the structure of types in the signatures of the given class and any nested
   * classes.  Requires the {@code DJ_CLASS} property to be set on all referenced classes; after this
   * step, the type hierarchy may safely be traversed.  Types aren't guaranteed to be well-formed.
   */
  public void initializeClassSignatures(AnonymousInnerAllocation ast) {
    initializeNestedClassSignatures(ast.getMembers(), new ClassSignatureContext(_context, _c, _loader));
  }
   
  private void initializeNestedClassSignatures(Iterable<? extends Node> members, TypeContext sigContext) {
    TypeContext bodyContext = new ClassContext(sigContext, _c); 
    for (Node member : members) {
      if (member instanceof TypeDeclaration) {
        ClassChecker nestedChecker = new ClassChecker(getDJClass(member), _loader, bodyContext, _opt);
        nestedChecker.initializeClassSignatures((TypeDeclaration) member);
      }
    }
  }
  
  /**
   * Check the class's signature declarations.  Verifies that the class's signature is well formed,
   * checks the types in field and method signatures, and recurs on nested classes.
   */
  public void checkSignatures(TypeDeclaration ast) {
    TypeContext sigContext = new ClassSignatureContext(_context, _c, _loader);
    TypeNameChecker sigChecker = new TypeNameChecker(sigContext, _opt);
    debug.logStart("Check type parameters");
    try { sigChecker.ensureWellFormedTypeParameters(typeParameters(ast)); }
    finally { debug.logEnd(); }
    debug.logStart("Check supertypes");
    try {
      if (ast instanceof ClassDeclaration) {
        sigChecker.ensureWellFormed(((ClassDeclaration) ast).getSuperclass());
      }
      if (ast.getInterfaces() != null) {
        for (TypeName tn : ast.getInterfaces()) { sigChecker.ensureWellFormed(tn); }
      }
    }
    finally { debug.logEnd(); }
    
    if (ast instanceof InterfaceDeclaration) { checkInterfaceMemberSignatures(ast.getMembers(), sigContext); }
    else { checkClassMemberSignatures(ast.getMembers(), sigContext); }
  }
  
  /**
   * Check the class's signature declarations.  Verifies that the class's signature is well formed,
   * checks the types in field and method signatures, and recurs on nested classes.
   */
  public void checkSignatures(AnonymousAllocation ast) {
    checkClassMemberSignatures(ast.getMembers(), new ClassSignatureContext(_context, _c, _loader));
  }
  
  /**
   * Check the class's signature declarations.  Verifies that the class's signature is well formed,
   * checks the types in field and method signatures, and recurs on nested classes.
   */
  public void checkSignatures(AnonymousInnerAllocation ast) {
    checkClassMemberSignatures(ast.getMembers(), new ClassSignatureContext(_context, _c, _loader));
  }
  
  private void checkClassMemberSignatures(Iterable<? extends Node> members, TypeContext sigContext) {
    TypeContext bodyContext = new ClassContext(sigContext, _c); 
    Visitor<Void> v = new ClassMemberSignatureVisitor(bodyContext);
    for (Node n : members) {
      debug.logStart();
      try { n.acceptVisitor(v); }
      finally { debug.logEnd(); }
    }
  }

  private void checkInterfaceMemberSignatures(Iterable<? extends Node> members, TypeContext sigContext) {
    TypeContext bodyContext = new ClassContext(sigContext, _c); 
    Visitor<Void> v = new InterfaceMemberSignatureVisitor(bodyContext);
    for (Node n : members) {
      debug.logStart();
      try { n.acceptVisitor(v); }
      finally { debug.logEnd(); }
    }
  }
  
  /**
   * Check the field initializers, method bodies, nested class bodies, etc., of a class or interface declaration.
   * Should be called <em>after</em> the signatures of all relevant classes and interfaces have been checked.
   */
  public void checkBodies(TypeDeclaration ast) {
    checkBodies(ast.getMembers());
  }
    
  /**
   * Check the field initializers, method bodies, nested class bodies, etc., of a class or interface declaration.
   * Should be called <em>after</em> the signatures of all relevant classes and interfaces have been checked.
   */
  public void checkBodies(AnonymousAllocation ast) {
    checkBodies(ast.getMembers());
  }
  
  /**
   * Check the field initializers, method bodies, nested class bodies, etc., of a class or interface declaration.
   * Should be called <em>after</em> the signatures of all relevant classes and interfaces have been checked.
   */
  public void checkBodies(AnonymousInnerAllocation ast) {
    checkBodies(ast.getMembers());
  }
  
  private void checkBodies(Iterable<? extends Node> members) {
    TypeContext sigContext = new ClassSignatureContext(_context, _c, _loader);
    TypeContext bodyContext = new ClassContext(sigContext, _c); 
    MemberBodyVisitor bod = new MemberBodyVisitor(bodyContext);
    for (Node n : members) {
      debug.logStart();
      try { n.acceptVisitor(bod); }
      finally { debug.logEnd(); }
    }
  }
  
  private static TypeParameter[] typeParameters(TypeDeclaration ast) {
    if (ast instanceof GenericClassDeclaration) {
      return ((GenericClassDeclaration) ast).getTypeParameters();
    }
    else if (ast instanceof GenericInterfaceDeclaration) {
      return ((GenericInterfaceDeclaration) ast).getTypeParameters();
    }
    else { return new TypeParameter[0]; }
  }
  
  
  private abstract class MemberSignatureVisitor extends AbstractVisitor<Void> {
    
    protected final TypeContext _bodyContext;
    
    protected MemberSignatureVisitor(TypeContext bodyContext) { _bodyContext = bodyContext; }
    
    @Override public Void visit(ClassDeclaration node) {
      new ClassChecker(getDJClass(node), _loader, _bodyContext, _opt).checkSignatures(node);
      return null;
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      new ClassChecker(getDJClass(node), _loader, _bodyContext, _opt).checkSignatures(node);
      return null;
    }
    
    @Override public Void visit(MethodDeclaration node) {
      DJMethod m = getMethod(node);
      
      TypeContext sigContext = new FunctionSignatureContext(_bodyContext, m);
      TypeNameChecker sigChecker = new TypeNameChecker(sigContext, _opt);

      TypeParameter[] tparams;
      if (node instanceof PolymorphicMethodDeclaration) {
        tparams = ((PolymorphicMethodDeclaration) node).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      sigChecker.checkTypeParameters(tparams);
      
      Type returnT = sigChecker.check(node.getReturnType());
      setErasedType(node, _opt.typeSystem().erasedClass(returnT));
      
      for (FormalParameter param : node.getParameters()) {
        Type t = sigChecker.check(param.getType());
        setVariable(param, new LocalVariable(param.getName(), t, param.getModifiers().isFinal()));
      }
      
      for (TypeName tn : node.getExceptions()) { sigChecker.check(tn); }
      return null;
    }
    
    @Override public Void visit(FieldDeclaration node) {
      new TypeNameChecker(_bodyContext, _opt).check(node.getType());
      return null;
    }
    
    @Override public abstract Void visit(ConstructorDeclaration node);
    @Override public abstract Void visit(ClassInitializer node);
    @Override public abstract Void visit(InstanceInitializer node);
  }
  
  private class ClassMemberSignatureVisitor extends MemberSignatureVisitor {
    
    public ClassMemberSignatureVisitor(TypeContext bodyContext) { super(bodyContext); }
    
    @Override public Void visit(MethodDeclaration node) {
      super.visit(node);
      ModifierSet mods = node.getModifiers();
      if (mods.isAbstract() && node.getBody() != null) {
        setErrorStrings(node, node.getName());
        throw new ExecutionError("abstract.method.body", node);
      }
      else if (!mods.isAbstract() && node.getBody() == null) {
        setErrorStrings(node, node.getName());
        throw new ExecutionError("missing.method.body", node);
      }
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      DJConstructor k = getConstructor(node);
      if (_c.isAnonymous() || !_c.declaredName().equals(node.getName())) {
        setErrorStrings(node, node.getName());
        throw new ExecutionError("constructor.name", node);
      }
      
      TypeContext sigContext = new FunctionSignatureContext(_bodyContext, k);
      TypeNameChecker sigChecker = new TypeNameChecker(sigContext, _opt);

      TypeParameter[] tparams;
      if (node instanceof PolymorphicConstructorDeclaration) {
        tparams = ((PolymorphicConstructorDeclaration) node).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      sigChecker.checkTypeParameters(tparams);
      
      for (FormalParameter param : node.getParameters()) {
        Type t = sigChecker.check(param.getType());
        setVariable(param, new LocalVariable(param.getName(), t, param.getModifiers().isFinal()));
      }
      
      for (TypeName tn : node.getExceptions()) { sigChecker.check(tn); }
      return null;
    }
    
    @Override public Void visit(ClassInitializer node) {
      return null;
    }
    
    @Override public Void visit(InstanceInitializer node) {
      return null;
    }
    
  }
  
  private class InterfaceMemberSignatureVisitor extends MemberSignatureVisitor {
    
    public InterfaceMemberSignatureVisitor(TypeContext bodyContext) { super(bodyContext); }

    @Override public Void visit(MethodDeclaration node) {
      super.visit(node);
      if (node.getBody() != null) {
        setErrorStrings(node, node.getName());
        throw new ExecutionError("abstract.method.body", node);
      }
      return null;
    }
    
    @Override public Void visit(FieldDeclaration node) {
      super.visit(node);
      if (node.getInitializer() == null) {
        setErrorStrings(node, node.getName());
        throw new ExecutionError("uninitialized.variable", node);
      }
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      throw new ExecutionError("interface.member", node);
    }
    @Override public Void visit(ClassInitializer node) {
      throw new ExecutionError("interface.member", node);
    }
    @Override public Void visit(InstanceInitializer node) {
      throw new ExecutionError("interface.member", node);
    }
    
  }
  
  private class MemberBodyVisitor extends AbstractVisitor<Void> {
    
    private final TypeContext _bodyContext;
    
    public MemberBodyVisitor(TypeContext bodyContext) { _bodyContext = bodyContext; }
    
    @Override public Void visit(ClassDeclaration node) {
      new ClassChecker(getDJClass(node), _loader, _bodyContext, _opt).checkBodies(node);
      return null;
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      new ClassChecker(getDJClass(node), _loader, _bodyContext, _opt).checkBodies(node);
      return null;
    }
    
    @Override public Void visit(MethodDeclaration node) {
      if (node.getBody() != null) {
        DJMethod m = getMethod(node);
        TypeContext sigContext = new FunctionSignatureContext(_bodyContext, m);
        TypeContext bodyContext = new FunctionContext(sigContext, m);
        node.getBody().acceptVisitor(new StatementChecker(bodyContext, _opt));
      }
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      DJConstructor k = getConstructor(node);
      TypeContext sigContext = new FunctionSignatureContext(_bodyContext, k);
      TypeContext bodyContext = new FunctionContext(sigContext, k);
      ExpressionChecker callChecker = new ExpressionChecker(bodyContext, _opt);
      ConstructorCall call = node.getConstructorCall();
      if (call != null) { callChecker.checkConstructorCall(call); }
      for (Node n : node.getStatements()) {
        bodyContext = n.acceptVisitor(new StatementChecker(bodyContext, _opt));
      }
      // if the call is implicit, check it *after* checking the body (better error messages this way) 
      if (call == null) { callChecker.checkConstructorCall(new ConstructorCall(null, null, true)); }
      return null;
    }
    
    @Override public Void visit(FieldDeclaration node) {
      // TODO: static context
      Expression init = node.getInitializer();
      if (init != null) {
        Type expectedT = getType(node.getType());
        Type initT = new ExpressionChecker(_bodyContext, _opt).check(init, expectedT);
        TypeSystem ts = _opt.typeSystem();
        try {
          Expression newInit = ts.assign(expectedT, init);
          node.setInitializer(newInit);
        }
        catch (TypeSystem.UnsupportedConversionException e) {
          TypeSystem.TypePrinter printer = ts.typePrinter();
          setErrorStrings(node, printer.print(initT), printer.print(expectedT));
          throw new ExecutionError("assignment.types", node);
        }
      }
      return null;
    }
    
    @Override public Void visit(ClassInitializer node) {
      // TODO: static context
      node.getBlock().acceptVisitor(new StatementChecker(_bodyContext, _opt));
      return null;
    }
    
    @Override public Void visit(InstanceInitializer node) {
      node.getBlock().acceptVisitor(new StatementChecker(_bodyContext, _opt));
      return null;
    }
  }
  
}
