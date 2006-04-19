package koala.dynamicjava.tree.tiger;

import java.util.Map;

public class EmptyEnv<Key, Value> implements Environment<Key, Value> {
  public Environment<Key, Value> extend(Map<Key, Value> level) { return new ConsEnv<Key, Value>(level, this); }
  public Value lookup(Key k) {
    throw new IllegalArgumentException("Attempt to call lookup on an EmptyEnv");
  }
  public boolean contains(Key k) { return false; }
  public Environment<Key, Value> getRest() {
    throw new IllegalArgumentException("Attempt to call getRest on an EmptyEnv");
  }
}

