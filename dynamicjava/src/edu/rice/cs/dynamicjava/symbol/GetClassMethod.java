package edu.rice.cs.dynamicjava.symbol;

import java.lang.reflect.Method;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;
import edu.rice.cs.dynamicjava.symbol.type.Wildcard;

/**
 * Provides a DJMethod interface for accessing a class's implicit "getClass()" method.
 * This overrides {@link Object#getClass}, refining the return type.
 */
public class GetClassMethod extends SpecialMethod {
  
  private final DJClass CLASS = SymbolUtil.wrapClass(Class.class);

  private final DJClass _c;
  private final Type _returnType;
  
  public GetClassMethod(ClassType t, TypeSystem ts) {
    _c = t.ofClass();
    Wildcard w = new Wildcard(new BoundedSymbol(new Object(), ts.erase(t), TypeSystem.NULL));
    Type returnType;
    try { returnType = ts.makeClassType(CLASS, IterUtil.singleton(w)); }
    catch (TypeSystem.InvalidTypeArgumentException e) { returnType = ts.makeClassType(CLASS); }
    _returnType = returnType;
  }
  
  public String declaredName() { return "getClass"; }
  public DJClass declaringClass() { return _c; }
  
  public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
  public Iterable<LocalVariable> parameters() { return IterUtil.empty(); }
  public Type returnType() { return _returnType; }
  public Iterable<Type> thrownTypes() { return IterUtil.empty(); }
  
  public boolean isStatic() { return false; }
  public boolean isAbstract() { return false; }
  public boolean isFinal() { return false; }
  public Access accessibility() { return Access.PUBLIC; }
  public Access.Module accessModule() { return _c.accessModule(); }
  
  protected Method implementation() throws NoSuchMethodException {
    return Object.class.getDeclaredMethod("getClass");
  }
  
}
