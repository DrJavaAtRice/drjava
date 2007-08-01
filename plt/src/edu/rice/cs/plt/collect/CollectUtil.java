package edu.rice.cs.plt.collect;

import java.util.*;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.iter.SizedIterable;

public final class CollectUtil {
  
  /** Prevents instance creation */
  private CollectUtil() {}
  
  public static <T> Set<T> makeSet(T... members) {
    return new HashSet<T>(Arrays.asList(members));
  }
  
  /** Produce an empty set.  Equivalent to {@link Collections#emptySet}; defined here for Java 1.4 compatibility. */
  @SuppressWarnings("unchecked") public static <T> Set<T> emptySet() {
    return (Set<T>) Collections.EMPTY_SET;
  }
  
  /** Produce an empty map.  Equivalent to {@link Collections#emptyMap}; defined here for Java 1.4 compatibility. */
  @SuppressWarnings("unchecked") public static <K, V> Map<K, V> emptyMap() {
    return (Map<K, V>) Collections.EMPTY_MAP;
  }
  
  public static <T> Set<T> union(Set<? extends T> s1, Set<? extends T> s2) {
    return new UnionSet<T>(s1, s2);
  }
  
  public static <T> Set<T> intersection(Set<?> s1, Set<? extends T> s2) {
    return new IntersectionSet<T>(s1, s2);
  }
  
  public static <T> Set<T> complement(Set<? extends T> domain, Set<?> excluded) {
    return new ComplementSet<T>(domain, excluded);
  }
  
  public static <K, V> Map<K, V> compose(Map<? extends K, ? extends V> parent, Map<? extends K, ? extends V> child) {
    return new ComposedMap<K, V>(parent, child);
  }
  
  public static final Predicate2<Set<?>, Object> CONTAINS_PREDICATE = new ContainsPredicate();
  
  private static final class ContainsPredicate implements Predicate2<Set<?>, Object>, Serializable {
    private ContainsPredicate() {}
    public Boolean value(Set<?> set, Object val) { return set.contains(val); }
  }

  public static Predicate<Object> containsPredicate(Set<?> set) {
    return LambdaUtil.bindFirst(CONTAINS_PREDICATE, set);
  }
  
  public static <T> Set<T> asSet(Iterable<T> iter) {
    if (iter instanceof Set<?>) { return (Set<T>) iter; }
    else if (iter instanceof Collection<?>) { return new HashSet<T>((Collection<T>) iter); }
    else if (iter instanceof SizedIterable<?>) {
      SizedIterable<T> si = (SizedIterable<T>) iter;
      Set<T> result = new HashSet<T>(si.size());
      for (T elt : si) { result.add(elt); }
      return result;
    }
    else {
      Set<T> result = new HashSet<T>();
      for (T elt : iter) { result.add(elt); }
      return result;
    }
  }
  
  /** Convert an Option to an empty or singleton set. */
  public static <T> Set<T> asSet(Option<? extends T> opt) {
    return opt.apply(new OptionVisitor<T, Set<T>>() {
      public Set<T> forSome(T val) { return Collections.singleton(val); }
      public Set<T> forNone() { return emptySet(); }
    });
  }
  

  public static <T> Set<T> functionClosure(T base, Lambda<? super T, ? extends T> function) {
    return functionClosure(Collections.singleton(base), function);
  }
  
  public static <T> Set<T> functionClosure(Set<? extends T> base, final Lambda<? super T, ? extends T> function) {
    Lambda<T, Set<T>> neighbors = new Lambda<T, Set<T>>() {
      public Set<T> value(T node) { return Collections.singleton(function.value(node)); }
    };
    return graphClosure(base, neighbors);
  }
  
  public static <T> Set<T> partialFunctionClosure(T base, Lambda<? super T, ? extends Option<? extends T>> function) {
    return partialFunctionClosure(Collections.singleton(base), function);
  }
  
  public static <T> Set<T> partialFunctionClosure(Set<? extends T> base,
                                                  final Lambda<? super T, ? extends Option<? extends T>> function) {
    Lambda<T, Set<T>> neighbors = new Lambda<T, Set<T>>() {
      public Set<T> value(T node) { return asSet(function.value(node)); }
    };
    return graphClosure(base, neighbors);
  }
  
  public static <T> Set<T> graphClosure(T base, Lambda<? super T, ? extends Set<? extends T>> neighbors) {
    return graphClosure(Collections.singleton(base), neighbors);
  }
  
  public static <T> Set<T> graphClosure(Set<? extends T> base,
                                        Lambda<? super T, ? extends Iterable<? extends T>> neighbors) {
    Set<T> result = new LinkedHashSet<T>(base);
    LinkedList<T> workList = new LinkedList<T>(base); // can't iterate over result because it mutates
    while (!workList.isEmpty())  {
      for (T newElt : neighbors.value(workList.removeFirst())) {
        if (!result.contains(newElt)) {
          result.add(newElt);
          workList.addLast(newElt);
        }
      }
    }
    return result;
  }

  public static <K, V> Map.Entry<K, V> mapEntryForKey(final Map<K, V> map, final K key) {
    return new Map.Entry<K, V>() {
      public K getKey() { return key; }
      public V getValue() { return map.get(key); }
      public V setValue(V value) { return map.put(key, value); }
      public boolean equals(Object o) {
        if (this == o) { return true; }
        else if (!(o instanceof Map.Entry<?, ?>)) { return false; }
        else {
          Map.Entry<?, ?> cast = (Map.Entry<?, ?>) o;
          if (key == null ? cast.getKey() == null : key.equals(cast.getKey())) {
            V val = map.get(key);
            return val == null ? cast.getValue() == null : val.equals(cast.getValue());
          }
          else { return false; }
        }
      }
      public int hashCode() {
        V val = map.get(key);
        return (key == null ? 0 : key.hashCode()) ^ (val == null ? 0 : val.hashCode());
      }
    };
  }
    
}
