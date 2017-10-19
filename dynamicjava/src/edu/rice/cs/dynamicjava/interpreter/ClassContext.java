package edu.rice.cs.dynamicjava.interpreter;

import java.util.Iterator;
import edu.rice.cs.plt.iter.SequenceIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * The context of a class body, including all members of the class (both declared and inherited).
 */
public class ClassContext extends DelegatingContext {
  
  private final DJClass _c;
  private final ClassType _thisType;
  private final Iterator<Integer> _anonymousCounter;
  
  public ClassContext(TypeContext next, DJClass c) {
    super(next);
    _c = c;
    _thisType = SymbolUtil.thisType(c);
    _anonymousCounter = new SequenceIterator<Integer>(1, LambdaUtil.INCREMENT_INT);
  }
  
  private ClassContext(TypeContext next, DJClass c, Iterator<Integer> anonymousCounter) {
    super(next);
    _c = c;
    _thisType = SymbolUtil.thisType(c);
    _anonymousCounter = anonymousCounter;
  }
  
  protected ClassContext duplicate(TypeContext next) {
    return new ClassContext(next, _c, _anonymousCounter);
  }
  
  // Class and type variable names:
  
  @Override public boolean typeExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) || super.typeExists(name, ts);
  }
  
  @Override public boolean memberClassExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) || super.memberClassExists(name, ts);
  }
  
  @Override public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    debug.logStart(new String[]{"class","name"}, _c, name); try {
      
    if (hasMemberClass(name, ts)) { return _thisType; }
    else { return super.typeContainingMemberClass(name, ts); }
    
    } finally { debug.logEnd(); }
  }
  
  @Override public boolean topLevelClassExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) ? false : super.topLevelClassExists(name, ts);
  }
  
  @Override public DJClass getTopLevelClass(String name, TypeSystem ts) throws AmbiguousNameException {
    return hasMemberClass(name, ts) ? null : super.getTopLevelClass(name, ts);
  }
  
  @Override public boolean typeVariableExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) ? false : super.typeVariableExists(name, ts);
  }
  
  @Override public VariableType getTypeVariable(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) ? null : super.getTypeVariable(name, ts);
  }
  
  private boolean hasMemberClass(String name, TypeSystem ts) {
    return ts.containsClass(_thisType, name, accessModule());
  }
  
  // Variable and field names:

  @Override public boolean variableExists(String name, TypeSystem ts) {
    return hasField(name, ts) || super.variableExists(name, ts);
  }
  
  @Override public boolean fieldExists(String name, TypeSystem ts) {
    return hasField(name, ts) || super.fieldExists(name, ts);
  }
  
  @Override public ClassType typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException {
    if (hasField(name, ts)) { return _thisType; }
    else { return super.typeContainingField(name, ts); }
  }
  
  @Override public boolean localVariableExists(String name, TypeSystem ts) {
    return hasField(name, ts) ? false : super.localVariableExists(name, ts); 
  }
  
  @Override public LocalVariable getLocalVariable(String name, TypeSystem ts) {
    return hasField(name, ts) ? null : super.getLocalVariable(name, ts);
  }
  
  private boolean hasField(String name, TypeSystem ts) {
    return ts.containsField(_thisType, name, accessModule());
  }
  
  // Method and local function names:
  
  @Override public boolean functionExists(String name, TypeSystem ts) {
    return hasMethod(name, ts) || super.functionExists(name, ts);
  }
  
  @Override public boolean methodExists(String name, TypeSystem ts) {
    return hasMethod(name, ts) || super.methodExists(name, ts);
  }
  
  @Override public Type typeContainingMethod(String name, TypeSystem ts) {
    return hasMethod(name, ts) ? _thisType : super.typeContainingMethod(name, ts);
  }
  
  @Override public boolean localFunctionExists(String name, TypeSystem ts) {
    return hasMethod(name, ts) ? false : super.localFunctionExists(name, ts);
  }
  
  @Override public Iterable<LocalFunction> getLocalFunctions(String name, TypeSystem ts,
                                                              Iterable<LocalFunction> partial) {
    return !IterUtil.isEmpty(partial) || hasMethod(name, ts) ? partial : super.getLocalFunctions(name, ts, partial);
  }
  
  
  
  
  private boolean hasMethod(String name, TypeSystem ts) {
    return ts.containsMethod(_thisType, name, accessModule());
  }
  

  /** Return a full name for a class with the given name declared here. */
  @Override public String makeClassName(String n) {
    return _c.fullName() + "$" + n;
  }
  
  /** Return a full name for an anonymous class declared here. */
  @Override public String makeAnonymousClassName() {
    return makeClassName(_anonymousCounter.next().toString());
  }
  
  /**
   * Return the class of {@code this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  @Override public DJClass getThis() { return _c; }
  
  /**
   * Return the class of {@code className.this} in the current context, or {@code null}
   * if there is no such value (for example, in a static context).
   */
  @Override public DJClass getThis(String className) {
    if (!_c.isAnonymous() && className.equals(_c.declaredName())) { return _c; }
    else { return super.getThis(className); }
  }
  
  @Override public DJClass getThis(Type expected, TypeSystem ts) {
    if (ts.isSubtype(_thisType, expected)) { return _c; }
    else { return super.getThis(expected, ts); }
  }
  
  @Override public DJClass initializingClass() { return null; }
  
  /**
   * The expected type of a {@code return} statement in the given context, or {@code null}
   * if {@code return} statements should not appear here.
   */
  @Override public Type getReturnType() { return null; }
  
  /**
   * The types that are allowed to be thrown in the current context.  If there is no
   * such declaration, the list will be empty.
   */
  @Override public Iterable<Type> getDeclaredThrownTypes() { return IterUtil.empty(); }
  
}
