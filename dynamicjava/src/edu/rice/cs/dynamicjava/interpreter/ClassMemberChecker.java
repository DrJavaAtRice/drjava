package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.plt.iter.IterUtil;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;
import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;
import edu.rice.cs.dynamicjava.symbol.type.IntersectionType;

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
  
  public void checkMembers(Iterable<Node> nodes) {
    checkSignatures(nodes);
    checkBodies(nodes);
  }
  
  private void checkSignatures(Iterable<Node> nodes) {
    SignatureVisitor sig = new SignatureVisitor();
    for (Node n : nodes) { n.acceptVisitor(sig); }
  }
  
  private void checkBodies(Iterable<Node> nodes) {
    BodyVisitor bod = new BodyVisitor();
    for (Node n : nodes) { n.acceptVisitor(bod); }
  }
  
  private Type checkType(Expression exp) { return new ExpressionChecker(_context, _opt).value(exp); }
  
  private Type checkType(Expression exp, Type expected) {
    return new ExpressionChecker(_context, _opt, expected).value(exp);
  }
  
  private Iterable<Type> checkTypes(Iterable<? extends Expression> l) {
    return IterUtil.mapSnapshot(l, new ExpressionChecker(_context, _opt));
  }
  
  private Type checkTypeName(TypeName t) {
    // It would be nice to separate TypeName handling into a different visitor,
    // but this works for now.
    return t.acceptVisitor(new ExpressionChecker(_context, _opt));
  }
  
  private class SignatureVisitor extends AbstractVisitor<Void> {
    
    @Override public Void visit(ClassDeclaration node) {
      throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override public Void visit(MethodDeclaration node) {
      DJMethod m = getMethod(node);
      
      TypeParameter[] tparams;
      if (node instanceof PolymorphicMethodDeclaration) {
        tparams = ((PolymorphicMethodDeclaration) node).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      for (TypeParameter tparam : tparams) {
        setTypeVariable(tparam, new VariableType(new BoundedSymbol(tparam, tparam.getRepresentation())));
      }
      
      TypeContext sigContext = new FunctionSignatureContext(_context, m);
      ExpressionChecker sigChecker = new ExpressionChecker(sigContext, _opt);
      sigChecker.setTypeParameterBounds(tparams);
      
      Type returnT = node.getReturnType().acceptVisitor(sigChecker);
      setErasedType(node, _opt.typeSystem().erasedClass(returnT));
      
      for (FormalParameter param : node.getParameters()) {
        Type t = param.getType().acceptVisitor(sigChecker);
        setVariable(param, new LocalVariable(param.getName(), t, param.isFinal()));
      }
      
      for (TypeName tn : node.getExceptions()) { tn.acceptVisitor(sigChecker); }
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      DJClass thisClass = _context.getThis();
      DJConstructor k = getConstructor(node);
      if (thisClass.isAnonymous() || !thisClass.declaredName().equals(node.getName())) {
        setErrorStrings(node, SymbolUtil.shortName(thisClass));
        throw new ExecutionError("constructor.name");
      }
      
      TypeParameter[] tparams;
      if (node instanceof PolymorphicConstructorDeclaration) {
        tparams = ((PolymorphicConstructorDeclaration) node).getTypeParameters();
      }
      else { tparams = new TypeParameter[0]; }
      for (TypeParameter tparam : tparams) {
        setTypeVariable(tparam, new VariableType(new BoundedSymbol(tparam, tparam.getRepresentation())));
      }
      
      TypeContext sigContext = new FunctionSignatureContext(_context, k);
      ExpressionChecker sigChecker = new ExpressionChecker(sigContext, _opt);
      sigChecker.setTypeParameterBounds(tparams);
      
      for (FormalParameter param : node.getParameters()) {
        Type t = param.getType().acceptVisitor(sigChecker);
        setVariable(param, new LocalVariable(param.getName(), t, param.isFinal()));
      }
      
      for (TypeName tn : node.getExceptions()) { tn.acceptVisitor(sigChecker); }
      return null;
    }
    
    @Override public Void visit(FieldDeclaration node) {
      checkTypeName(node.getType());
      return null;
    }
    
  }
  
  private class BodyVisitor extends AbstractVisitor<Void> {
    @Override public Void visit(ClassDeclaration node) {
      throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override public Void visit(InterfaceDeclaration node) {
      throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override public Void visit(MethodDeclaration node) {
      DJMethod m = getMethod(node);
      TypeContext sigContext = new FunctionSignatureContext(_context, m);
      TypeContext bodyContext = new FunctionContext(sigContext, m);
      node.getBody().acceptVisitor(new StatementChecker(bodyContext, _opt));
      return null;
    }
    
    @Override public Void visit(ConstructorDeclaration node) {
      DJConstructor k = getConstructor(node);
      TypeContext sigContext = new FunctionSignatureContext(_context, k);
      TypeContext bodyContext = new FunctionContext(sigContext, k);
      ConstructorCall call = node.getConstructorCall();
      if (call == null) { call = new ConstructorCall(null, null, true); }
      call.acceptVisitor(new ExpressionChecker(bodyContext, _opt));
      for (Node n : node.getStatements()) {
        bodyContext = n.acceptVisitor(new StatementChecker(bodyContext, _opt));
      }
      return null;
    }
    
    @Override public Void visit(FieldDeclaration node) {
      // TODO: static context
      Type expectedT = getType(node.getType());
      Expression init = node.getInitializer();
      if (init != null) {
        Type initT = checkType(init, expectedT);
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
