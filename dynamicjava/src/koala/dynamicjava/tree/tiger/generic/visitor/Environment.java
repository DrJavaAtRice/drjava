package koala.dynamicjava.tree.tiger.generic.visitor;

import java.util.Map;

public interface Environment<Key, Value> { 
  public Environment<Key, Value> extend(Map<Key, Value> level); 
  public Value lookup(Key k);
  public boolean contains(Key k);
}

class EmptyEnv<Key, Value> implements Environment<Key, Value> {
  public Environment<Key, Value> extend(Map<Key, Value> level) { return new ConsEnv<Key, Value>(level, this); }
  public Value lookup(Key k) { 
    throw new IllegalArgumentException("Attempt to call lookup on an EmptyEnv"); 
  }
  public boolean contains(Key k) { return false; }
}

class ConsEnv<Key, Value> implements Environment<Key, Value> {
  private Map<Key, Value> _level;
  private Environment<Key, Value> _next;
  
  ConsEnv(Map<Key, Value> level, Environment<Key, Value> next) {
    _level = level;
    _next = next;
  }
  
  public Environment<Key, Value> extend(Map<Key, Value> level) { return new ConsEnv<Key, Value>(level, this); } 
  public Value lookup(Key k) {
    if (_level.containsKey(k)) { return _level.get(k); }
    else { return _next.lookup(k); }
  }
  public boolean contains(Key k) { 
    return _level.containsKey(k) || _next.contains(k);
  }
}