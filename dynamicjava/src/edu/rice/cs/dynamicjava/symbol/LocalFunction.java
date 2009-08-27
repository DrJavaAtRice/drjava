package edu.rice.cs.dynamicjava.symbol;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.WrappedException;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.interpreter.StatementEvaluator;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

import koala.dynamicjava.tree.MethodDeclaration;
import koala.dynamicjava.tree.tiger.TypeParameter;
import koala.dynamicjava.tree.tiger.PolymorphicMethodDeclaration;
import koala.dynamicjava.interpreter.NodeProperties;

/** Represents a local function declaration. */
public class LocalFunction implements Function {
  
  private MethodDeclaration _ast;
  
  public LocalFunction(MethodDeclaration ast) {
    _ast = ast;
  }
  
  public String declaredName() { return _ast.getName(); }
  
  public Type returnType() { return NodeProperties.getType(_ast.getReturnType()); }
  
  public Iterable<VariableType> typeParameters() {
    if (_ast instanceof PolymorphicMethodDeclaration) {
      TypeParameter[] ps = ((PolymorphicMethodDeclaration)_ast).getTypeParameters();
      return IterUtil.mapSnapshot(IterUtil.asIterable(ps), NodeProperties.NODE_TYPE_VARIABLE);
    }
    else { return IterUtil.empty(); }
  }
  
  public Iterable<LocalVariable> parameters() {
    return IterUtil.mapSnapshot(_ast.getParameters(), NodeProperties.NODE_VARIABLE);
  }
  
  public Iterable<Type> thrownTypes() {
    return IterUtil.mapSnapshot(_ast.getExceptions(), NodeProperties.NODE_TYPE);
  }
  
  public Object evaluate(Iterable<Object> args, RuntimeBindings bindings, Options options)
    throws EvaluatorException {
    RuntimeBindings bodyBindings = new RuntimeBindings(bindings, parameters(), args);
    try {
      _ast.getBody().acceptVisitor(new StatementEvaluator(bodyBindings, options));
      // if there was no return, return null or an appropriate zero primitive
      return SymbolUtil.initialValue(NodeProperties.getErasedType(_ast).value());
    }
    catch (StatementEvaluator.ReturnException e) {
      return e.value().unwrap(null);
    }
    catch (WrappedException e) {
      if (e.getCause() instanceof EvaluatorException) { throw (EvaluatorException) e.getCause(); }
      else { throw e; }
    }
  }
  
}
