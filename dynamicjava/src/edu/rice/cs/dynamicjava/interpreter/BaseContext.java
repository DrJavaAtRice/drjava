package edu.rice.cs.dynamicjava.interpreter;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.*;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A non-delegating context to place and the top of a delegation chain.  All methods return null, false,
 * or an empty list, or (where none of these are valid) throw an UnsupportedOperationException.
 */
public class BaseContext implements TypeContext {
  
  public static final BaseContext INSTANCE = new BaseContext();
  
  private BaseContext() {}
  
  public boolean fieldExists(String name, TypeSystem ts) { return false; }
  public boolean functionExists(String name, TypeSystem ts) { return false; }
  public ClassLoader getClassLoader() { throw new UnsupportedOperationException(); }
  public Iterable<Type> getDeclaredThrownTypes() { return IterUtil.empty(); }
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts) { return IterUtil.empty(); }
  public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts, Iterable<LocalFunction> partial) {
    return partial;
  }
  public LocalVariable getLocalVariable(String name, TypeSystem ts) { return null; }
  public Access.Module accessModule() { return new TopLevelAccessModule(""); }
  public Type getReturnType() { return null; }
  public DJClass getThis() { return null; }
  public DJClass getThis(String className) { return null; }
  public boolean hasThis(DJClass c) { return false; }
  public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException { return null; }
  public VariableType getTypeVariable(String name, TypeSystem ts) { return null; }
  public TypeContext importField(DJClass c, String name) { throw new UnsupportedOperationException(); }
  public TypeContext importMemberClass(DJClass outer, String name) { throw new UnsupportedOperationException(); }
  public TypeContext importMemberClasses(DJClass outer) { throw new UnsupportedOperationException(); }
  public TypeContext importMethod(DJClass c, String name) { throw new UnsupportedOperationException(); }
  public TypeContext importStaticMembers(DJClass c) { throw new UnsupportedOperationException(); }
  public TypeContext importTopLevelClass(DJClass c) { throw new UnsupportedOperationException(); }
  public TypeContext importTopLevelClasses(String pkg) { throw new UnsupportedOperationException(); }
  public boolean localFunctionExists(String name, TypeSystem ts) { return false; }
  public boolean localVariableExists(String name, TypeSystem ts) { return false; }
  public String makeAnonymousClassName() { throw new UnsupportedOperationException(); }
  public String makeClassName(String n) { throw new UnsupportedOperationException(); }
  public boolean memberClassExists(String name, TypeSystem ts) { return false; }
  public boolean methodExists(String name, TypeSystem ts) { return false; }
  public TypeContext setPackage(String name) { throw new UnsupportedOperationException(); }
  public boolean topLevelClassExists(String name, TypeSystem ts) { return false; }
  public ClassType typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException { return null; }
  public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException { return null; }
  public ClassType typeContainingMethod(String name, TypeSystem ts) throws AmbiguousNameException { return null; }
  public boolean typeExists(String name, TypeSystem ts) { return false; }
  public boolean typeVariableExists(String name, TypeSystem ts) { return false; }
  public boolean variableExists(String name, TypeSystem ts) { return false; }
}
