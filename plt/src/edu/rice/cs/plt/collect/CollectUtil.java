package edu.rice.cs.plt.collect;

import java.util.*;
import edu.rice.cs.plt.lambda.*;
import java.io.Serializable;
import edu.rice.cs.plt.iter.SizedIterable;

public final class CollectUtil {
  
  /** Prevents instance creation */
  private CollectUtil() {}
  
  public static <T> Set<T> makeSet(T... members) {
    return new HashSet<T>(Arrays.asList(members));
  }
  
  public static Predicate2<Set<?>, Object> containsPredicate() {
    return ContainsPredicate.INSTANCE;
  }
  
  public static Predicate<Object> containsPredicate(Set<?> set) {
    return LambdaUtil.bindFirst(ContainsPredicate.INSTANCE, set);
  }
  
  private static class ContainsPredicate implements Predicate2<Set<?>, Object> {
    public static final ContainsPredicate INSTANCE = new ContainsPredicate();
    private ContainsPredicate() {}
    public Boolean value(Set<?> set, Object val) { return set.contains(val); }
  }
  
  public static <T> Set<? extends T> asSet(Iterable<? extends T> iter) {
    if (iter instanceof Set<?>) {
      @SuppressWarnings("unchecked") Set<? extends T> set = (Set<? extends T>) iter;
      return set;
    }
    else if (iter instanceof Collection<?>) {
      @SuppressWarnings("unchecked") Collection<? extends T> coll = (Collection<? extends T>) iter;
      return new HashSet<T>(coll);
    }
    else if (iter instanceof SizedIterable<?>) {
      @SuppressWarnings("unchecked") SizedIterable<? extends T> si = (SizedIterable<? extends T>) iter;
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
  

  public static <T> Set<T> functionClosure(T base, Lambda<? super T, ? extends T> function) {
    return functionClosure(Collections.singleton(base), function);
  }
  
  public static <T> Set<T> functionClosure(Set<T> base, Lambda<? super T, ? extends T> function) {
    @SuppressWarnings("unchecked")
    SingletonSetLambda<T> factory = (SingletonSetLambda<T>) SingletonSetLambda.INSTANCE;
    return graphClosure(base, LambdaUtil.<T, T, Set<T>>compose(function, factory));
  }
  
  private static class SingletonSetLambda<T> implements Lambda<T, Set<T>>, Serializable {
    public static final SingletonSetLambda<Object> INSTANCE = new SingletonSetLambda<Object>();
    private SingletonSetLambda() {}
    public Set<T> value(T arg) { return Collections.singleton(arg); }
  }
  
  public static <T> Set<T> graphClosure(T base, Lambda<? super T, ? extends Set<? extends T>> neighbors) {
    return graphClosure(Collections.singleton(base), neighbors);
  }
  
  public static <T> Set<T> graphClosure(Set<T> base, Lambda<? super T, ? extends Iterable<? extends T>> neighbors) {
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
