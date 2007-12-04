package edu.rice.cs.dynamicjava.interpreter;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;
import edu.rice.cs.dynamicjava.symbol.type.VariableType;

/** An environment used for evaluation. All keys in the environment are global -- there are no
  * shadowing concerns.  Thus, instances can be used very flexibly during evaluation, given that
  * a program is statically safe.  For example, the environment used to call a function can extend
  * the calling context.  Without this flexibility, it would have to be determined at what point
  * (if any) in the calling context's chain of environments the function declaration and the calling
  * context share a common lexical scope.
  */
public class RuntimeBindings {
  
  public static final RuntimeBindings EMPTY = new RuntimeBindings();
  
  private final RuntimeBindings _parent;
  private final Map<LocalVariable, Object> _vars;
  private final Map<VariableType, Type> _tvars; // might be useful someday...
  private final Map<DJClass, Object> _thisVals;
  
  public RuntimeBindings(RuntimeBindings parent, Map<LocalVariable, Object> vars,
                         Map<VariableType, Type> tvars, Map<DJClass, Object> thisVals) {
    _parent = parent;
    _vars = new HashMap<LocalVariable, Object>(vars);
    _tvars = new HashMap<VariableType, Type>(tvars);
    _thisVals = new HashMap<DJClass, Object>(thisVals);
  }
  
  private RuntimeBindings() {
    this(null, Collections.<LocalVariable, Object>emptyMap(),
         Collections.<VariableType, Type>emptyMap(), Collections.<DJClass, Object>emptyMap());
  }
  
  public RuntimeBindings(RuntimeBindings parent, LocalVariable var, Object val) {
    this(parent, Collections.singletonMap(var, val), Collections.<VariableType, Type>emptyMap(),
         Collections.<DJClass, Object>emptyMap());
  }
  
  public RuntimeBindings(RuntimeBindings parent, Iterable<LocalVariable> vars, Iterable<Object> vals) {
    this(parent, makeMap(vars, vals), Collections.<VariableType, Type>emptyMap(),
         Collections.<DJClass, Object>emptyMap());
  }
  
  private static <K, V> Map<K, V> makeMap(Iterable<? extends K> keys, Iterable<? extends V> vals) {
    Map<K, V> result = new HashMap<K, V>();
    for (Pair<K, V> entry : IterUtil.zip(keys, vals)) { result.put(entry.first(), entry.second()); }
    return result;
  }
  
  public RuntimeBindings(RuntimeBindings parent, DJClass thisClass, Object thisObj) {
      this(parent, Collections.<LocalVariable, Object>emptyMap(),
           Collections.<VariableType, Type>emptyMap(), Collections.singletonMap(thisClass, thisObj));
  }
  
  public Object get(LocalVariable v) {
    if (_vars.containsKey(v)) { return _vars.get(v); }
    else if (_parent != null) { return _parent.get(v); }
    else { throw new IllegalArgumentException("Variable " + v + " is undefined"); }
  }
  
  public void set(LocalVariable v, Object val) {
    if (_vars.containsKey(v)) { _vars.put(v, val); }
    else if (_parent != null) { _parent.set(v, val); }
    else { throw new IllegalArgumentException("Variable " + v + " is undefined"); }
  }

  public Type get(VariableType v) {
    if (_tvars.containsKey(v)) { return _tvars.get(v); }
    else if (_parent != null) { return _parent.get(v); }
    else { throw new IllegalArgumentException("Type variable " + v + " is undefined"); }
  }
  
  public Object getThis(DJClass c) {
    if (_thisVals.containsKey(c)) { return _thisVals.get(c); }
    else if (_parent != null) { return _parent.getThis(c); }
    else { throw new IllegalArgumentException("This value " + c + " is undefined"); }
  }

}
