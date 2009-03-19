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
public class ClassMemberChecker {
  
  private final TypeContext _context;
  private final Options _opt;
  
  public ClassMemberChecker(TypeContext context, Options opt) {
    _context = context;
    _opt = opt;
  }
  
  /** Validate the type signature of a class declaration: field types, method signatures, nested classes, etc. */
  public void checkClassSignatures(Iterable<Node> nodes) {
    ClassMemberSignatureVisitor sig = new ClassMemberSignatureVisitor();
    for (Node n : nodes) { n.acceptVisitor(sig); }
  }
  
  /** Validate the type signature of an interface declaration: field types, method signatures, nested classes, etc. */
  public void checkInterfaceSignatures(Iterable<Node> nodes) {
    InterfaceMemberSignatureVisitor sig = new InterfaceMemberSignatureVisitor();
    for (Node n : nodes) { n.acceptVisitor(sig); }
  }
  
  /**
   * Check the field initializers, method bodies, nested class bodies, etc., of a class or interface declaration.
   * Should be called <em>after</em> the signatures of all relevant classes and interfaces have been checked.
   */
  public void checkBodies(Iterable<Node> nodes) {
    BodyVisitor bod = new BodyVisitor();
    for (Node n : nodes) { n.acceptVisitor(bod); }
  }
  
  
  private abstract class SignatureVisitor extends AbstractVisitor<Void> {
    
    @Override public Void visit(ClassDeclaration node) {
      throw new ExecutionError("not.implemented", node);
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      throw new ExecutionError("not.implemented", node);
    }
    
    @Override public Void visit(MethodDeclaration node) {
      DJMethod m = getMethod(node);
      
      TypeContext sigContext = new FunctionSignatureContext(_context, m);
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
      new TypeNameChecker(_context, _opt).check(node.getType());
      return null;
    }
    
    @Override public abstract Void visit(ConstructorDeclaration node);
    @Override public abstract Void visit(ClassInitializer node);
    @Override public abstract Void visit(InstanceInitializer node);
  }
  
  private class ClassMemberSignatureVisitor extends SignatureVisitor {
    
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
      DJClass thisClass = _context.getThis();
      DJConstructor k = getConstructor(node);
      if (thisClass.isAnonymous() || !thisClass.declaredName().equals(node.getName())) {
        setErrorStrings(node, SymbolUtil.shortName(thisClass));
        throw new ExecutionError("constructor.name");
      }
      
      TypeContext sigContext = new FunctionSignatureContext(_context, k);
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
  
  private class InterfaceMemberSignatureVisitor extends SignatureVisitor {

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
  
  private class BodyVisitor extends AbstractVisitor<Void> {
    @Override public Void visit(ClassDeclaration node) {
      throw new ExecutionError("not.implemented", node);
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      throw new ExecutionError("not.implemented", node);
    }
    
    @Override public Void visit(MethodDeclaration node) {
      if (node.getBody() != null) {
        DJMethod m = getMethod(node);
        TypeContext sigContext = new FunctionSignatureContext(_context, m);
        TypeContext bodyContext = new FunctionContext(sigContext, m);
        node.getBody().acceptVisitor(new StatementChecker(bodyContext, _opt));
      }
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      DJConstructor k = getConstructor(node);
      TypeContext sigContext = new FunctionSignatureContext(_context, k);
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
        Type initT = new ExpressionChecker(_context, _opt).check(init, expectedT);
        TypeSystem ts = _opt.typeSystem();
        try {
          Expression newInit = ts.assign(expectedT, init);
          node.setInitializer(newInit);
        }
        catch (TypeSystem.UnsupportedConversionException e) {
          setErrorStrings(node, ts.userRepresentation(initT), ts.userRepresentation(expectedT));
          throw new ExecutionError("assignment.types", node);
        }
      }
      return null;
    }
    
    @Override public Void visit(ClassInitializer node) {
      // TODO: static context
      node.getBlock().acceptVisitor(new StatementChecker(_context, _opt));
      return null;
    }
    
    @Override public Void visit(InstanceInitializer node) {
      node.getBlock().acceptVisitor(new StatementChecker(_context, _opt));
      return null;
    }
  }
  
}
