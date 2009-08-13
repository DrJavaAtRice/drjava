package edu.rice.cs.dynamicjava.interpreter;

import java.util.Iterator;
import edu.rice.cs.plt.iter.SequenceIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.ClassType;

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
  
  /** Test whether {@code name} is an in-scope top-level class, member class, or type variable */
  @Override public boolean typeExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) || super.typeExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope member class */
  @Override public boolean memberClassExists(String name, TypeSystem ts) {
    return hasMemberClass(name, ts) || super.memberClassExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a class with the given name, or {@code null}
   * if there is no such type.
   */
  @Override public ClassType typeContainingMemberClass(String name, TypeSystem ts) throws AmbiguousNameException {
    debug.logStart(new String[]{"class","name"}, _c, name); try {
      
    if (hasMemberClass(name, ts)) { return _thisType; }
    else { return super.typeContainingMemberClass(name, ts); }
    
    } finally { debug.logEnd(); }
  }
  
  private boolean hasMemberClass(String name, TypeSystem ts) {
    return ts.containsClass(_thisType, name);
  }

  /** Test whether {@code name} is an in-scope field or local variable */
  @Override public boolean variableExists(String name, TypeSystem ts) {
    return hasField(name, ts) || super.variableExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope field */
  @Override public boolean fieldExists(String name, TypeSystem ts) {
    return hasField(name, ts) || super.fieldExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a field with the given name, or {@code null}
   * if there is no such type.
   */
  @Override public ClassType typeContainingField(String name, TypeSystem ts) throws AmbiguousNameException {
    if (hasField(name, ts)) { return _thisType; }
    else { return super.typeContainingField(name, ts); }
  }
  
  private boolean hasField(String name, TypeSystem ts) {
    return ts.containsField(_thisType, name);
  }
  
  /** Test whether {@code name} is an in-scope method or local function */
  @Override public boolean functionExists(String name, TypeSystem ts) {
    return hasMethod(name, ts) || super.functionExists(name, ts);
  }
  
  /** Test whether {@code name} is an in-scope method */
  @Override public boolean methodExists(String name, TypeSystem ts) {
    return hasMethod(name, ts) || super.methodExists(name, ts);
  }
  
  /**
   * Return the most inner type containing a method with the given name, or {@code null}
   * if there is no such type.
   */
  @Override public ClassType typeContainingMethod(String name, TypeSystem ts) throws AmbiguousNameException {
    if (hasMethod(name, ts)) { return _thisType; }
    else { return super.typeContainingMethod(name, ts); }
  }
  
  private boolean hasMethod(String name, TypeSystem ts) {
    return ts.containsMethod(_thisType, name);
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
    if (className.equals(_c.declaredName())) { return _c; }
    else { return super.getThis(className); }
  }
  
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
