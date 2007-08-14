package edu.rice.cs.plt.collect;

import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A hash code-based implementation of the Relation interface.  By default, hash-based indices are
 * created mapping firsts to seconds and vice-versa; this functionality may be turned off where it
 * is not needed by setting the appropriate flags.
 */
public class HashRelation<T1, T2> extends AbstractSet<Pair<T1, T2>>
                                  implements Relation<T1, T2>, Serializable {
  private HashSet<Pair<T1, T2>> _pairs;
  private HashMap<T1, HashSet<T2>> _firstIndex; // may be null; only maps to nonempty sets
  private HashMap<T2, HashSet<T1>> _secondIndex; // may be null; only maps to nonempty sets
  
  public HashRelation() { this(true, true); }
  
  public HashRelation(boolean indexFirst, boolean indexSecond) {
    _pairs = new HashSet<Pair<T1, T2>>();
    if (indexFirst) { _firstIndex = new HashMap<T1, HashSet<T2>>(); }
    else { _firstIndex = null; }
    if (indexSecond) { _secondIndex = new HashMap<T2, HashSet<T1>>(); }
    else { _secondIndex = null; }
  }
  
  public int size() { return _pairs.size(); }
  
  public boolean isEmpty() { return _pairs.isEmpty(); }
  
  public boolean contains(Pair<T1, T2> pair) { return _pairs.contains(pair); }
  
  public boolean contains(T1 first, T2 second) { return contains(Pair.make(first, second)); }
  
  public Iterator<Pair<T1, T2>> iterator() {
    return new Iterator<Pair<T1, T2>>() {
      private final Iterator<Pair<T1, T2>> _iter = _pairs.iterator();
      private Pair<T1, T2> _last;
      public boolean hasNext() { return _iter.hasNext(); }
      public Pair<T1, T2> next() { _last = _iter.next(); return _last; }
      public void remove() {
        _iter.remove(); // if this succeeds, _last is valid
        removeFromIndex(_last.first(), _last.second());
      }
    };
  }
  
  public boolean add(Pair<T1, T2> pair) {
    boolean result = _pairs.add(pair);
    if (result) { addToIndex(pair.first(), pair.second()); }
    return result;
  }
  
  public boolean add(T1 first, T2 second) {
    boolean result = _pairs.add(Pair.make(first, second));
    if (result) { addToIndex(first, second); }
    return result;
  }
  
  private void addToIndex(T1 first, T2 second) {
    if (_firstIndex != null) {
      HashSet<T2> seconds = _firstIndex.get(first);
      if (seconds == null) { seconds = new HashSet<T2>(); _firstIndex.put(first, seconds); }
      seconds.add(second);
    }
    if (_secondIndex != null) {
      HashSet<T1> firsts = _secondIndex.get(second);
      if (firsts == null) { firsts = new HashSet<T1>(); _secondIndex.put(second, firsts); }
      firsts.add(first);
    }
  }
  
  public boolean remove(Object o) {
    boolean result = _pairs.remove(o);
    if (result) {
      // if it's in the set, it must have the right type
      @SuppressWarnings("unchecked") Pair<T1, T2> pair = (Pair<T1, T2>) o;
      removeFromIndex(pair.first(), pair.second());
    }
    return result;
  }
  
  public boolean remove(T1 first, T2 second) {
    boolean result = _pairs.remove(Pair.make(first, second));
    if (result) { removeFromIndex(first, second); }
    return result;
  }
  
  private void removeFromIndex(T1 first, T2 second) {
    if (_firstIndex != null) {
      Set<T2> seconds = _firstIndex.get(first);
      seconds.remove(second);
      if (seconds.isEmpty()) { _firstIndex.remove(first); }
    }
    if (_secondIndex != null) {
      Set<T1> firsts = _secondIndex.get(second);
      firsts.remove(first);
      if (firsts.isEmpty()) { _secondIndex.remove(second); }
    }
  }
  
  public void clear() {
    _pairs.clear();
    if (_firstIndex != null) { _firstIndex.clear(); }
    if (_secondIndex != null) { _secondIndex.clear(); }
  }

  public Set<T1> firstSet() {
    if (_firstIndex != null) { return Collections.unmodifiableSet(_firstIndex.keySet()); }
    else { return CollectUtil.asSet(IterUtil.pairFirsts(_pairs)); }
  }
  
  public boolean containsFirst(T1 first) {
    if (_firstIndex != null) { return _firstIndex.containsKey(first); }
    else { return IterUtil.contains(IterUtil.pairFirsts(_pairs), first); }
  }
  
  public Set<T2> getSeconds(T1 first) {
    if (_firstIndex != null) {
      Set<T2> result = _firstIndex.get(first);
      return result == null ? CollectUtil.<T2>emptySet() : Collections.unmodifiableSet(result);
    }
    else {
      Set<T2> result = new HashSet<T2>();
      for (Pair<T1, T2> p : _pairs) {
        if (first == null ? p.first() == null : first.equals(p.first())) { result.add(p.second()); }
      }
      return result;
    }
  }

  public Set<T2> secondSet() {
    if (_secondIndex != null) { return Collections.unmodifiableSet(_secondIndex.keySet()); }
    else { return CollectUtil.asSet(IterUtil.pairSeconds(_pairs)); }
  }
  
  public boolean containsSecond(T2 second) {
    if (_secondIndex != null) { return _secondIndex.containsKey(second); }
    else { return IterUtil.contains(IterUtil.pairSeconds(_pairs), second); }
  }
  
  public Set<T1> getFirsts(T2 second) {
    if (_secondIndex != null) {
      Set<T1> result = _secondIndex.get(second);
      return result == null ? CollectUtil.<T1>emptySet() : Collections.unmodifiableSet(result);
    }
    else {
      Set<T1> result = new HashSet<T1>();
      for (Pair<T1, T2> p : _pairs) {
        if (second == null ? p.second() == null : second.equals(p.second())) { result.add(p.first()); }
      }
      return result;
    }
  }
  
}
