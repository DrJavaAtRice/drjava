package edu.rice.cs.dynamicjava.symbol;

import java.lang.reflect.Method;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.dynamicjava.symbol.type.ArrayType;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

/**
 * Provides a DJMethod interface for accessing an array's implicit "clone" method.
 * This overrides {@link Object#clone}, giving it public access, eliminating the
 * CloneNotSupported {@code throws} clause, and refining the return type.
 */
public class ArrayCloneMethod extends SpecialMethod {
  
  private final ArrayType _type;
  
  public ArrayCloneMethod(ArrayType t) {_type = t; }
  
  public String declaredName() { return "clone"; }
  public DJClass declaringClass() { return null; }
  
  public Iterable<VariableType> typeParameters() { return IterUtil.empty(); }
  public Iterable<LocalVariable> parameters() { return IterUtil.empty(); }
  public Type returnType() { return _type; }
  public Iterable<Type> thrownTypes() { return IterUtil.empty(); }
  
  public boolean isStatic() { return false; }
  public boolean isAbstract() { return false; }
  public boolean isFinal() { return false; }
  public Access accessibility() { return Access.PUBLIC; }
  public Access.Module accessModule() { return new TopLevelAccessModule("java.lang"); }
  
  protected Method implementation() throws NoSuchMethodException {
    return Object.class.getDeclaredMethod("clone");
  }
  
}
