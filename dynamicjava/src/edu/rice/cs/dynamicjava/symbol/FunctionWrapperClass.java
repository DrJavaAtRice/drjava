package edu.rice.cs.dynamicjava.symbol;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SequenceIterator;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.RuntimeBindings;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * DJClass implementation that treats a collection of in-scope local functions
 * as a class containing equivalent static methods.
 */
public class FunctionWrapperClass implements DJClass {

  private static final Iterator<Integer> ID_COUNTER =
    new SequenceIterator<Integer>(1, LambdaUtil.INCREMENT_INT);
  
  private final Iterable<DJMethod> _methods;
  private final String _name;
  
  public FunctionWrapperClass(Iterable<? extends LocalFunction> functions) {
    _methods = IterUtil.mapSnapshot(functions, FUNCTION_AS_METHOD);
    _name = "Overload" + ID_COUNTER.next();
  }
  
  private static final Lambda<LocalFunction, DJMethod> FUNCTION_AS_METHOD =
    new Lambda<LocalFunction, DJMethod>() {
    public DJMethod value(LocalFunction f) { return new FunctionWrapperMethod(f); }
  };
  
  
  /** Produces the binary name for the given class (as in {@link Class#getName}) */
  public String fullName() {
    return FunctionWrapperClass.class.getName() + "$" + _name;
  }
  
  public boolean isAnonymous() { return false; }
  
  public String declaredName() { return _name; }
  
  public boolean isInterface() { return false; }
  
  public boolean isStatic() { return false; }
  
  public boolean isAbstract() { return false; }
  
  public boolean isFinal() { return true; }
  
  public Access accessibility() { return Access.PUBLIC; }
  
  public boolean hasRuntimeBindingsParams() { return false; }
  
  /** The class that declares this class, or {@code null} if this is declared at a top-level or local scope */
  public DJClass declaringClass() { return SymbolUtil.wrapClass(FunctionWrapperClass.class); }
  
  /** List all type variables declared by this class */
  public Iterable<VariableType> declaredTypeParameters() { return IterUtil.empty(); }
  
  /** List the declared supertypes of this class */
  public Iterable<Type> declaredSupertypes() { return IterUtil.empty(); }
  
  public Iterable<DJField> declaredFields() { return IterUtil.empty(); }
  
  public Iterable<DJConstructor> declaredConstructors() { return IterUtil.empty(); }
  
  public Iterable<DJMethod> declaredMethods() { return _methods; }
  
  public Iterable<DJClass> declaredClasses() { return IterUtil.empty(); }

  /**
   * @return  The type bound to {@code super} in the context of this class, or 
   *          {@code null} if {@code super} is not defined
   */
  public Type immediateSuperclass() { return null; }
  
  /**
   * Produce the runtime representation of the class (as in {@link ClassLoader#loadClass},
   * repeated invocations should produce the same object).
   */
  public Class<?> load() { throw new UnsupportedOperationException(); }
  
  /** We intentionally only allow two FunctionWrapperClasses to be equal if they are identical. */
  public boolean equals(Object o) { return this == o; }
  
  public int hashCode() { return System.identityHashCode(this); }
  
  
  private static class FunctionWrapperMethod implements DJMethod {
    private final LocalFunction _f;
    public FunctionWrapperMethod(LocalFunction f) { _f = f; }
    public Iterable<VariableType> declaredTypeParameters() { return _f.declaredTypeParameters(); }
    public Iterable<LocalVariable> declaredParameters() { return _f.declaredParameters(); }
    public Iterable<Type> thrownTypes() { return _f.thrownTypes(); }
    public String declaredName() { return _f.declaredName(); }
    public Type returnType() { return _f.returnType(); }
    public boolean isStatic() { return true; }
    public boolean isAbstract() { return false; }
    public boolean isFinal() { return false; }
    public Access accessibility() { return Access.PUBLIC; }
    public boolean hasRuntimeBindingsParams() { return false; }
    public Object evaluate(Object receiver, Iterable<Object> args, RuntimeBindings bindings, Options options) 
      throws EvaluatorException {
      return _f.evaluate(args, bindings, options);
    }
    
  }

} 
