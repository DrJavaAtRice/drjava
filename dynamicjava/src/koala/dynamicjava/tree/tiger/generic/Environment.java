package koala.dynamicjava.tree.tiger.generic;

import java.util.Map;

public interface Environment<Key, Value> {
  public Environment<Key, Value> extend(Map<Key, Value> level);
  public Value lookup(Key k);
  public boolean contains(Key k);
  public Environment<Key, Value> getRest();
}
