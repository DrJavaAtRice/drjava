package edu.rice.cs.plt.collect;

import java.util.Iterator;
import java.util.Set;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.tuple.Pair;

public class UnindexedRelation<T1, T2> extends AbstractRelation<T1, T2> {
  
  private final Set<Pair<T1, T2>> _pairs;
  
  public UnindexedRelation() { this(CollectUtil.<Pair<T1, T2>>hashSetFactory()); }
  public UnindexedRelation(Thunk<Set<Pair<T1, T2>>> setFactory) { _pairs = setFactory.value(); }

  @Override public boolean isEmpty() { return _pairs.isEmpty(); }
  @Override public int size() { return _pairs.size(); }
  public boolean hasFixedSize() { return false; }
  public boolean isInfinite() { return false; }
  public boolean isStatic() { return false; }

  public boolean contains(T1 first, T2 second) { return _pairs.contains(new Pair<T1, T2>(first, second)); }
  public boolean contains(Object obj) { return _pairs.contains(obj); }
  public Iterator<Pair<T1, T2>> iterator() { return _pairs.iterator(); }
  
  @Override public boolean add(T1 first, T2 second) { return _pairs.add(new Pair<T1, T2>(first, second)); }
  @Override public boolean remove(T1 first, T2 second) { return _pairs.remove(new Pair<T1, T2>(first, second)); }
  @Override public void clear() { _pairs.clear(); }

  public PredicateSet<T1> firstSet() { return new LazyRelationIndex<T1, T2>(_pairs).keys(); }
  public PredicateSet<T2> matchFirst(T1 first) { return new LazyRelationIndex<T1, T2>(_pairs).match(first); }
  public PredicateSet<T2> secondSet() {
    return new LazyRelationIndex<T2, T1>(IterUtil.map(_pairs, Pair.<T1, T2>inverter())).keys();
  }
  public PredicateSet<T1> matchSecond(T2 second) {
    return new LazyRelationIndex<T2, T1>(IterUtil.map(_pairs, Pair.<T1, T2>inverter())).match(second);
  }

  /** Make an IndexedRelation indexed by {@link java.util.HashMap}s and {@link java.util.HashSet}s. */
  public static <T1, T2> UnindexedRelation<T1, T2> makeHashBased() {
    return new UnindexedRelation<T1, T2>(CollectUtil.<Pair<T1, T2>>hashSetFactory());
  }
  
  /** Make an IndexedRelation indexed by {@link java.util.LinkedHashMap}s and {@link java.util.LinkedHashSet}s. */
  public static <T1, T2> UnindexedRelation<T1, T2> makeLinkedHashBased() {
    return new UnindexedRelation<T1, T2>(CollectUtil.<Pair<T1, T2>>linkedHashSetFactory());
  }
  
  /** Make an IndexedRelation indexed by {@link java.util.TreeMap}s and {@link java.util.TreeSet}s. */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
  UnindexedRelation<T1, T2> makeTreeBased() {
    return new UnindexedRelation<T1, T2>(CollectUtil.<Pair<T1, T2>>treeSetFactory(Pair.<T1, T2>comparator()));
  }
  
}
