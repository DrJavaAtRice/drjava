package edu.rice.cs.plt.collect;

import java.util.Map;
import java.util.AbstractSet;
import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.iter.MappedIterator;

public class KeyDrivenEntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {
  private final Map<K, V> _map;

  public KeyDrivenEntrySet(Map<K, V> map) {
    _map = map;
  }
  
  public int size() { return _map.size(); }
  
  public boolean isEmpty() { return _map.isEmpty(); }
  
  public boolean contains(Object o) {
    if (o instanceof Map.Entry<?, ?>) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
      Object key = entry.getKey();
      if (_map.containsKey(key)) {
        Object mappedVal = _map.get(key);
        return mappedVal == null ? entry.getValue() == null : mappedVal.equals(entry.getValue());
      }
      else { return false; }
    }
    else { return false; }
  }
  
  public Iterator<Map.Entry<K, V>> iterator() {
    Lambda<K, Map.Entry<K, V>> mapper = new Lambda<K, Map.Entry<K, V>>() {
      public Map.Entry<K, V> value(K key) { return CollectUtil.mapEntryForKey(_map, key); }
    };
    return new MappedIterator<K, Map.Entry<K, V>>(_map.keySet().iterator(), mapper);
  }
  
}
