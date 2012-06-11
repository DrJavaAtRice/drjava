/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.collect;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.ObjectUtil;

public final class CollectUtil {
  
  /** Prevents instance creation */
  private CollectUtil() {}
  
  /** A predicate that accepts Set-Object pairs such that the given object is contained by the set. */
  public static final Predicate2<Set<?>, Object> SET_CONTENTS_PREDICATE = new SetContentsPredicate();
  
  private static final class SetContentsPredicate implements Predicate2<Set<?>, Object>, Serializable {
    private SetContentsPredicate() {}
    public boolean contains(Set<?> set, Object val) { return set.contains(val); }
  }
  
  /**
   * Get a factory that produces HashSets by invoking the empty HashSet constructor.
   * @see HashSet#HashSet()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<Set<T>> hashSetFactory() {
    return (Thunk<Set<T>>) (Thunk<?>) DefaultHashSetFactory.INSTANCE;
  }
  
  private static final class DefaultHashSetFactory<T> implements Thunk<Set<T>>, Serializable {
    public static final DefaultHashSetFactory<Object> INSTANCE = new DefaultHashSetFactory<Object>();
    private DefaultHashSetFactory() {}
    public Set<T> value() { return new HashSet<T>(); }
  }
  
  /**
   * Get a factory that produces HashSets with the given initial capacity.
   * @see HashSet#HashSet(int)
   */
  public static <T> Thunk<Set<T>> hashSetFactory(int initialCapacity) {
    return new CustomHashSetFactory<T>(initialCapacity);
  }
  
  private static final class CustomHashSetFactory<T> implements Thunk<Set<T>>, Serializable {
    private final int _initialCapacity;
    public CustomHashSetFactory(int initialCapacity) { _initialCapacity = initialCapacity; }
    public Set<T> value() { return new HashSet<T>(_initialCapacity); }
  }
  
  /**
   * Get a factory that produces LinkedHashSets by invoking the empty LinkedHashSet constructor.
   * @see LinkedHashSet#LinkedHashSet()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<Set<T>> linkedHashSetFactory() {
    return (Thunk<Set<T>>) (Thunk<?>) DefaultLinkedHashSetFactory.INSTANCE;
  }
  
  private static final class DefaultLinkedHashSetFactory<T> implements Thunk<Set<T>>, Serializable {
    public static final DefaultLinkedHashSetFactory<Object> INSTANCE = new DefaultLinkedHashSetFactory<Object>();
    private DefaultLinkedHashSetFactory() {}
    public Set<T> value() { return new LinkedHashSet<T>(); }
  }
  
  /**
   * Get a factory that produces LinkedHashSets with the given initial capacity.
   * @see LinkedHashSet#LinkedHashSet(int)
   */
  public static <T> Thunk<Set<T>> linkedHashSetFactory(int initialCapacity) {
    return new CustomLinkedHashSetFactory<T>(initialCapacity);
  }
  
  private static final class CustomLinkedHashSetFactory<T> implements Thunk<Set<T>>, Serializable {
    private final int _initialCapacity;
    public CustomLinkedHashSetFactory(int initialCapacity) { _initialCapacity = initialCapacity; }
    public Set<T> value() { return new LinkedHashSet<T>(_initialCapacity); }
  }
  
  /**
   * Get a factory that produces TreeSets sorted according to the elements' natural order.
   * @see TreeSet#TreeSet()
   */
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<? super T>> Thunk<Set<T>> treeSetFactory() {
    // not sure why the weakening cast is necessary here but not elsewhere
    return (Thunk<Set<T>>) (Thunk<? extends Set<?>>) DefaultTreeSetFactory.INSTANCE;
  }
  
  private static final class DefaultTreeSetFactory<T extends Comparable<? super T>>
      implements Thunk<Set<T>>, Serializable {
    public static final DefaultTreeSetFactory<String> INSTANCE = new DefaultTreeSetFactory<String>();
    private DefaultTreeSetFactory() {}
    public Set<T> value() { return new TreeSet<T>(); }
  }
  
  /**
   * Get a factory that produces TreeSets sorted according to the given comparator.
   * @see TreeSet#TreeSet(Comparator)
   */
  public static <T> Thunk<Set<T>> treeSetFactory(Comparator<? super T> comparator) {
    return new CustomTreeSetFactory<T>(comparator);
  }
  
  private static final class CustomTreeSetFactory<T> implements Thunk<Set<T>>, Serializable {
    private final Comparator<? super T> _comp;
    public CustomTreeSetFactory(Comparator<? super T> comp) { _comp = comp; }
    public Set<T> value() { return new TreeSet<T>(_comp); }
  }
  
  /**
   * Get a factory that produces CopyOnWriteArraySets by invoking the empty CopyOnWriteArraySet constructor.
   * @see CopyOnWriteArraySet#CopyOnWriteArraySet()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<Set<T>> copyOnWriteArraySetFactory() {
    return (Thunk<Set<T>>) (Thunk<?>) CopyOnWriteArraySetFactory.INSTANCE;
  }
  
  private static final class CopyOnWriteArraySetFactory<T> implements Thunk<Set<T>>, Serializable {
    public static final CopyOnWriteArraySetFactory<Object> INSTANCE = new CopyOnWriteArraySetFactory<Object>();
    private CopyOnWriteArraySetFactory() {}
    public Set<T> value() { return new CopyOnWriteArraySet<T>(); }
  }
  
  /**
   * Get a factory that produces HashMaps by invoking the empty HashMap constructor.
   * @see HashMap#HashMap()
   */
  @SuppressWarnings("unchecked") public static <K, V> Thunk<Map<K, V>> hashMapFactory() {
    return (Thunk<Map<K, V>>) (Thunk<?>) DefaultHashMapFactory.INSTANCE;
  }
  
  private static final class DefaultHashMapFactory<K, V> implements Thunk<Map<K, V>>, Serializable {
    public static final DefaultHashMapFactory<Object, Object> INSTANCE =
      new DefaultHashMapFactory<Object, Object>();
    private DefaultHashMapFactory() {}
    public Map<K, V> value() { return new HashMap<K, V>(); }
  }
  
  /**
   * Get a factory that produces HashMaps with the given initial capacity.
   * @see HashMap#HashMap(int)
   */
  public static <K, V> Thunk<Map<K, V>> hashMapFactory(int initialCapacity) {
    return new CustomHashMapFactory<K, V>(initialCapacity);
  }
  
  private static final class CustomHashMapFactory<K, V> implements Thunk<Map<K, V>>, Serializable {
    private final int _initialCapacity;
    public CustomHashMapFactory(int initialCapacity) { _initialCapacity = initialCapacity; }
    public Map<K, V> value() { return new HashMap<K, V>(_initialCapacity); }
  }
  
  /**
   * Get a factory that produces LinkedHashMaps by invoking the empty LinkedHashMap constructor.
   * @see LinkedHashMap#LinkedHashMap()
   */
  @SuppressWarnings("unchecked") public static <K, V> Thunk<Map<K, V>> linkedHashMapFactory() {
    return (Thunk<Map<K, V>>) (Thunk<?>) DefaultLinkedHashMapFactory.INSTANCE;
  }
  
  private static final class DefaultLinkedHashMapFactory<K, V> implements Thunk<Map<K, V>>, Serializable {
    public static final DefaultLinkedHashMapFactory<Object, Object> INSTANCE =
      new DefaultLinkedHashMapFactory<Object, Object>();
    private DefaultLinkedHashMapFactory() {}
    public Map<K, V> value() { return new LinkedHashMap<K, V>(); }
  }
  
  /**
   * Get a factory that produces LinkedHashMaps with the given initial capacity.
   * @see LinkedHashMap#LinkedHashMap(int)
   */
  public static <K, V> Thunk<Map<K, V>> linkedHashMapFactory(int initialCapacity) {
    return new CustomLinkedHashMapFactory<K, V>(initialCapacity);
  }
  
  private static final class CustomLinkedHashMapFactory<K, V> implements Thunk<Map<K, V>>, Serializable {
    private final int _initialCapacity;
    public CustomLinkedHashMapFactory(int initialCapacity) { _initialCapacity = initialCapacity; }
    public Map<K, V> value() { return new LinkedHashMap<K, V>(_initialCapacity); }
  }
  
  /**
   * Get a factory that produces TreeMaps sorted according to the elements' natural order.
   * @see TreeMap#TreeMap()
   */
  @SuppressWarnings("unchecked")
  public static <K extends Comparable<? super K>, V> Thunk<Map<K, V>> treeMapFactory() {
    // not sure why the weakening cast is necessary here but not elsewhere
    return (Thunk<Map<K, V>>) (Thunk<? extends Map<?, ?>>) DefaultTreeMapFactory.INSTANCE;
  }
  
  private static final class DefaultTreeMapFactory<K extends Comparable<? super K>, V>
      implements Thunk<Map<K, V>>, Serializable {
    public static final DefaultTreeMapFactory<String, Object> INSTANCE =
      new DefaultTreeMapFactory<String, Object>();
    private DefaultTreeMapFactory() {}
    public Map<K, V> value() { return new TreeMap<K, V>(); }
  }
  
  /**
   * Get a factory that produces TreeMaps sorted according to the given comparator.
   * @see TreeMap#TreeMap(Comparator)
   */
  public static <K, V> Thunk<Map<K, V>> treeMapFactory(Comparator<? super K> comparator) {
    return new CustomTreeMapFactory<K, V>(comparator);
  }
  
  private static final class CustomTreeMapFactory<K, V> implements Thunk<Map<K, V>>, Serializable {
    private final Comparator<? super K> _comp;
    public CustomTreeMapFactory(Comparator<? super K> comp) { _comp = comp; }
    public Map<K, V> value() { return new TreeMap<K, V>(_comp); }
  }
  
  /**
   * Get a factory that produces ArrayLists by invoking the empty ArrayList constructor.
   * @see ArrayList#ArrayList()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<List<T>> arrayListFactory() {
    return (Thunk<List<T>>) (Thunk<?>) DefaultArrayListFactory.INSTANCE;
  }
  
  private static final class DefaultArrayListFactory<T> implements Thunk<List<T>>, Serializable {
    public static final DefaultArrayListFactory<Object> INSTANCE = new DefaultArrayListFactory<Object>();
    private DefaultArrayListFactory() {}
    public List<T> value() { return new ArrayList<T>(); }
  }
  
  /**
   * Get a factory that produces ArrayLists with the given initial capacity.
   * @see ArrayList#ArrayList(int)
   */
  public static <T> Thunk<List<T>> arrayListFactory(int initialCapacity) {
    return new CustomArrayListFactory<T>(initialCapacity);
  }
  
  private static final class CustomArrayListFactory<T> implements Thunk<List<T>>, Serializable {
    private final int _initialCapacity;
    public CustomArrayListFactory(int initialCapacity) { _initialCapacity = initialCapacity; }
    public List<T> value() { return new ArrayList<T>(_initialCapacity); }
  }
  
  /**
   * Get a factory that produces LinkedLists by invoking the empty LinkedList constructor.
   * @see LinkedList#LinkedList()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<List<T>> linkedListFactory() {
    return (Thunk<List<T>>) (Thunk<?>) DefaultLinkedListFactory.INSTANCE;
  }
  
  private static final class DefaultLinkedListFactory<T> implements Thunk<List<T>>, Serializable {
    public static final DefaultLinkedListFactory<Object> INSTANCE = new DefaultLinkedListFactory<Object>();
    private DefaultLinkedListFactory() {}
    public List<T> value() { return new LinkedList<T>(); }
  }
  
  /**
   * Get a factory that produces CopyOnWriteArraySets by invoking the empty CopyOnWriteArraySet constructor.
   * @see CopyOnWriteArraySet#CopyOnWriteArraySet()
   */
  @SuppressWarnings("unchecked") public static <T> Thunk<List<T>> copyOnWriteArrayListFactory() {
    return (Thunk<List<T>>) (Thunk<?>) CopyOnWriteArrayListFactory.INSTANCE;
  }
  
  private static final class CopyOnWriteArrayListFactory<T> implements Thunk<List<T>>, Serializable {
    public static final CopyOnWriteArrayListFactory<Object> INSTANCE = new CopyOnWriteArrayListFactory<Object>();
    private CopyOnWriteArrayListFactory() {}
    public List<T> value() { return new CopyOnWriteArrayList<T>(); }
  }
  
  /**
   * Create an immutable {@code PredicateSet} based on the given elements.  May depend on a valid
   * {@code hashCode()} implementation.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> PredicateSet<T> makeSet(T... elements) {
    return makeSet(IterUtil.asIterable(elements));
  }
  
  /**
   * Create an immutable {@code PredicateSet} based on the given elements.  May depend on a valid
   * {@code hashCode()} implementation.
   */
  public static <T> PredicateSet<T> makeSet(Iterable<? extends T> elements) {
    if (IterUtil.isEmpty(elements)) { 
      return EmptySet.make();
    }
    else if (IterUtil.sizeOf(elements, 2) == 1) {
      return new SingletonSet<T>(IterUtil.first(elements));
    }
    else {
      Set<T> result = new LinkedHashSet<T>(asCollection(elements));
      return new ImmutableSet<T>(result) {
        @Override public boolean hasFixedSize() { return true; }
        @Override public boolean isStatic() { return true; }
      };
    }
  }
  
  /** Produce an empty or singleton set based on the given Option. */
  public static <T> PredicateSet<T> makeSet(Option<? extends T> opt) {
    if (opt.isSome()) { return new SingletonSet<T>(opt.unwrap()); }
    else { return EmptySet.make(); }
  }
  
  /**
   * Create an immutable {@code Relation} based on the given elements.  May depend on a valid
   * {@code hashCode()} implementation.
   */
  public static <T1, T2> Relation<T1, T2> makeRelation(Iterable<? extends Pair<? extends T1, ? extends T2>> pairs) {
    if (IterUtil.isEmpty(pairs)) { 
      return EmptyRelation.make();
    }
    else if (IterUtil.sizeOf(pairs, 2) == 1) {
      Pair<? extends T1, ? extends T2> elt = IterUtil.first(pairs);
      return new SingletonRelation<T1, T2>(elt.first(), elt.second());
    }
    else {
      Relation<T1, T2> result = IndexedRelation.makeLinkedHashBased();
      for (Pair<? extends T1, ? extends T2> elt : pairs) {
        result.add(elt.first(), elt.second());
      }
      return new ImmutableRelation<T1, T2>(result) {
        @Override public boolean hasFixedSize() { return true; }
        @Override public boolean isStatic() { return true; }
      };
    }
  }
  
  /** Make an {@code ArrayList} with the given elements. */
  public static <T> List<T> makeList(Iterable<? extends T> iter) {
    return makeArrayList(iter);
  }

  /** Make an {@code ArrayList} with the given elements. */
  public static <T> ArrayList<T> makeArrayList(Iterable<? extends T> iter) {
    if (iter instanceof Collection<?>) {
      @SuppressWarnings("unchecked") // should be legal, but javac 6 doesn't like it
      Collection<? extends T> cast = (Collection<? extends T>) iter;
      return new ArrayList<T>(cast);
    }
    else if (iter instanceof SizedIterable<?>) {
      ArrayList<T> result = new ArrayList<T>(((SizedIterable<?>) iter).size());
      for (T e : iter) { result.add(e); }
      return result;
    }
    else {
      ArrayList<T> result = new ArrayList<T>();
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /** Make a {@code LinkedList} with the given elements. */
  public static <T> LinkedList<T> makeLinkedList(Iterable<? extends T> iter) {
    if (iter instanceof Collection<?>) {
      @SuppressWarnings("unchecked") // should be legal, but javac 6 doesn't like it
      Collection<? extends T> cast = (Collection<? extends T>) iter;
      return new LinkedList<T>(cast);
    }
    else {
      LinkedList<T> result = new LinkedList<T>();
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /** Make a {@link ConsList} with the given elements. */
  public static <T> ConsList<T> makeConsList(Iterable<? extends T> iter) {
    ConsList<T> result = ConsList.empty();
    for (T elt : IterUtil.reverse(iter)) { result = ConsList.cons(elt, result); }
    return result;
  }
  
  /**
   * Produce an immutable empty list.  Equivalent to {@link Collections#emptyList}; defined here for
   * Java 1.4 compatibility.
   */
  @SuppressWarnings("unchecked") public static <T> List<T> emptyList() {
    return (List<T>) Collections.EMPTY_LIST;
  }
  
  /**
   * Produce an immutable empty set.  Similar to {@link Collections#emptySet}, but the result here is
   * a {@link PredicateSet}.  Also defined for Java 1.4 compatibility.
   */
  @SuppressWarnings("unchecked") public static <T> EmptySet<T> emptySet() {
    return (EmptySet<T>) EmptySet.INSTANCE;
  }
  
  /**
   * Produce an immutable empty map.  Similar to {@link Collections#emptyMap}, but the result here is
   * a {@link LambdaMap}.  Also defined for Java 1.4 compatibility.
   */
  @SuppressWarnings("unchecked") public static <K, V> EmptyMap<K, V> emptyMap() {
    return (EmptyMap<K, V>) EmptyMap.INSTANCE;
  }

  /** Produce an immutable empty relation. */
  @SuppressWarnings("unchecked") public static <T1, T2> EmptyRelation<T1, T2> emptyRelation() {
    return (EmptyRelation<T1, T2>) EmptyRelation.INSTANCE;
  }
  
  /** Create an immutable singleton set.  Similar to {@link Collections#singleton}, but produces a PredicateSet. */
  public static <T> SingletonSet<T> singleton(T elt) {
    return new SingletonSet<T>(elt);
  }
  
  /** Create an immutable singleton relation. */
  public static <T1, T2> SingletonRelation<T1, T2> singleton(T1 first, T2 second) {
    return new SingletonRelation<T1, T2>(first, second);
  }
  
  /** Create an immutable singleton map. Similar to {@link Collections#singletonMap}, but produces a LambdaMap. */
  public static <K, V> SingletonMap<K, V> singletonMap(K key, V value) {
    return new SingletonMap<K, V>(key, value);
  }
  
  /** 
   * Convert the given {@code Iterable} to a {@code Set}.  If it already <em>is</em>
   * a {@code Set}, cast it as such.  Otherwise, create an {@link IterableSet}.
   */
  public static <T> Set<T> asSet(Iterable<T> iter) {
    if (iter instanceof Set<?>) { return (Set<T>) iter; }
    else { return new IterableSet<T>(iter); }
  }
  
  /** 
   * Convert the given {@code Iterable} to a {@code PredicateSet}.  If it already <em>is</em>
   * a {@code PredicateSet}, cast it as such.  If it is a {@code Set}, produce a {@link DelegatingSet}.
   * Otherwise, create an {@link IterableSet}.
   */
  public static <T> PredicateSet<T> asPredicateSet(Iterable<T> iter) {
    if (iter instanceof PredicateSet<?>) { return (PredicateSet<T>) iter; }
    else if (iter instanceof Set<?>) { return new DelegatingSet<T>((Set<T>) iter); }
    else { return new IterableSet<T>(iter); }
  }
  
  /**
   * Convert the given {@code Iterable} to a {@code Collection}.  If it already <em>is</em>
   * a {@code Collection}, cast it as such.  Otherwise, create an {@link IterableCollection}.
   */
  public static <T> Collection<T> asCollection(Iterable<T> iter) {
    if (iter instanceof Collection<?>) { return (Collection<T>) iter; }
    else { return new IterableCollection<T>(iter); }
  }
  
  /** 
   * Convert the given {@code Map} to a {@code LambdaMap}.  If it already <em>is</em>
   * a {@code LambdaMap}, cast it as such.  Otherwise, create a {@link DelegatingMap}.
   */
  public static <K, V> LambdaMap<K, V> asLambdaMap(Map<K, V> m) {
    if (m instanceof LambdaMap<?, ?>) { return (LambdaMap<K, V>) m; }
    else { return new DelegatingMap<K, V>(m); }
  }
  
  /**
   * Convert a Dictionary to a Map.  If it is a {@link Hashtable}, cast it as a Map.
   * Otherwise, create a {@link DictionaryMap}.
   */
  public static <K, V> Map<K, V> asMap(Dictionary<K, V> d) {
    // can't cast arbitrary Dictionaries because the dictionary type parameters may
    // be unrelated to map parameters -- it might be a Dictionary<K, V> and a Map<K, Foo>
    if (d instanceof Hashtable<?, ?>) { return (Hashtable<K, V>) d; }
    return new DictionaryMap<K, V>(d);
  }
  
  /**
   * Wrap a set in an immutable wrapper.  Similar to {@link Collections#unmodifiableSet},
   * but produces a {@code PredicateSet}.
   */
  public static <T> PredicateSet<T> immutable(Set<? extends T> set) {
    return new ImmutableSet<T>(set);
  }
  
  /**
   * Wrap a map in an immutable wrapper.  Similar to {@link Collections#unmodifiableMap},
   * but produces a {@code LambdaMap}.
   */
  public static <K, V> Map<K, V> immutable(Map<? extends K, ? extends V> map) {
    return new ImmutableMap<K, V>(map);
  }
  
  /** Wrap a relation in an immutable wrapper.  Analogous to {@link Collections#unmodifiableSet}. */
  public static <T1, T2> ImmutableRelation<T1, T2> immutable(Relation<T1, T2> r) {
    return new ImmutableRelation<T1, T2>(r);
  }
  
  /** Alias for {@link #makeSet}. */
  public static <T> PredicateSet<T> snapshot(Set<? extends T> set) {
    return makeSet(set);
  }
  
  /**
   * Produce a snapshot of {@code set} if its composite size is greater than the given threshold.
   * @see ObjectUtil#compositeSize
   */
  public static <T> Iterable<T> conditionalSnapshot(Set<T> set, int threshold) {
    if (ObjectUtil.compositeSize(set) > threshold) { return makeSet(set); }
    else { return set; }
  }
  
  /** Alias for {@link #makeRelation}. */
  public static <T1, T2> Relation<T1, T2> snapshot(Relation<? extends T1, ? extends T2> relation) {
    return makeRelation(relation);
  }
  
  /**
   * Produce a snapshot of {@code set} if its composite size is greater than the given threshold.
   * @see ObjectUtil#compositeSize
   */
  public static <T1, T2> Relation<T1, T2> conditionalSnapshot(Relation<T1, T2> rel, int threshold) {
    if (ObjectUtil.compositeSize(rel) > threshold) { return makeRelation(rel); }
    else { return rel; }
  }
  
  /** Invoke the {@code HashMap#HashMap(Map)} constructor, and wrap the result as a LambdaMap. */
  public static <K, V> LambdaMap<K, V> snapshot(Map<? extends K, ? extends V> map) {
    return new DelegatingMap<K, V>(new HashMap<K, V>(map));
  }
  
  /**
   * Produce a snapshot of {@code set} if its composite size is greater than the given threshold.
   * @see ObjectUtil#compositeSize
   */
  public static <K, V> Map<K, V> conditionalSnapshot(Map<K, V> map, int threshold) {
    if (ObjectUtil.compositeSize(map) > threshold) { return snapshot(map); }
    else { return map; }
  }
  
  /** Alias for {@link #makeArrayList}. */
  public static <T> List<T> snapshot(List<? extends T> list) {
    return makeArrayList(list);
  }

  /**
   * Wrap {@code s} as a thread-safe set that produces snapshots to support concurrent iteration.
   * @see SnapshotSynchronizedSet
   */
  public static <T> SnapshotSynchronizedSet<T> snapshotSynchronized(Set<T> s) {
    return new SnapshotSynchronizedSet<T>(s);
  }
  
  /**
   * Wrap {@code l} as a thread-safe list that produces snapshots to support concurrent iteration.
   * @see SnapshotSynchronizedList
   */
  public static <T> SnapshotSynchronizedList<T> snapshotSynchronized(List<T> l) {
    return new SnapshotSynchronizedList<T>(l);
  }
  
  /** Produce a lazy union of two sets.  Size-related operations have poor performance. */
  public static <T> PredicateSet<T> union(Set<? extends T> s1, Set<? extends T> s2) {
    return new UnionSet<T>(s1, s2);
  }
  
  /** Produce a lazy union of a set with an additional singleton element. */
  public static <T> PredicateSet<T> union(Set<? extends T> set, T elt) {
    return new UnionSet<T>(set, new SingletonSet<T>(elt));
  }
  
  /** Produce a lazy intersection of two sets.  Size-related operations have poor performance. */
  public static <T> PredicateSet<T> intersection(Set<?> s1, Set<? extends T> s2) {
    return new IntersectionSet<T>(s1, s2);
  }
  
  /**
   * Produce the complement of a set in a domain, or, equivalently, the difference of two sets.
   * Size-related operations have poor performance.
   */
  public static <T> PredicateSet<T> complement(Set<? extends T> domain, Set<?> excluded) {
    return new ComplementSet<T>(domain, excluded);
  }
  
  /**
   * Produce the complement of a singleton in a domain set, or, equivalently, a set with
   * a certain element removed.
   */
  public static <T> PredicateSet<T> complement(Set<? extends T> domain, T excluded) {
    return new ComplementSet<T>(domain, new SingletonSet<T>(excluded));
  }
  
  /** Lazily filter the given set.  Size-related operations have poor performance. */
  public static <T> PredicateSet<T> filter(Set<? extends T> set, Predicate<? super T> predicate) {
    return new FilteredSet<T>(set, predicate);
  }
  
  /** Produce the lazy cartesian (or cross) product of two sets. */
  public static <T1, T2> Relation<T1, T2> cross(Set<? extends T1> left, Set<? extends T2> right) {
    return new CartesianRelation<T1, T2>(left, right);
  }
  
  /** Produce a lazy union of two relations.  Size-related operations have poor performance. */
  public static <T1, T2> Relation<T1, T2> union(Relation<T1, T2> r1, Relation<T1, T2> r2) {
    return new UnionRelation<T1, T2>(r1, r2);
  }
  
  /** Produce a lazy union of a relation with an additional singleton entry. */
  public static <T1, T2> Relation<T1, T2> union(Relation<T1, T2> rel, T1 first, T2 second) {
    return new UnionRelation<T1, T2>(rel, new SingletonRelation<T1, T2>(first, second));
  }
  
  /** Produce a lazy intersection of two relations.  Size-related operations have poor performance. */
  public static <T1, T2> Relation<T1, T2> intersection(Relation<T1, T2> r1, Relation<T1, T2> r2) {
    return new IntersectionRelation<T1, T2>(r1, r2);
  }
  
  /**
   * Produce the complement of a relation in a domain, or, equivalently, the difference of two
   * relations.  Size-related operations have poor performance.
   */
  public static <T1, T2> Relation<T1, T2> complement(Relation<T1, T2> domain,
                                                     Relation<? super T1, ? super T2> excluded) {
    return new ComplementRelation<T1, T2>(domain, excluded);
  }
  
  /**
   * Produce the complement of a singleton in a domain relation, or, equivalently, a relation with
   * a certain entry removed.
   */
  public static <T1, T2> Relation<T1, T2> complement(Relation<T1, T2> domain, T1 first, T2 second) {
    return new ComplementRelation<T1, T2>(domain, new SingletonRelation<T1, T2>(first, second));
  }
  
  /** Produce a lazy transitive composition of two relations.  Size-related operations have poor performance. */
  public static <T1, T2, T3> Relation<T1, T3> compose(Relation<T1, T2> left, Relation<T2, T3> right) {
    return new ComposedRelation<T1, T2, T3>(left, right);
  }
  
  /** Lazily filter the given relation.  Size-related operations have poor performance. */
  public static <T1, T2> Relation<T1, T2> filter(Relation<T1, T2> relation,
                                                 Predicate2<? super T1, ? super T2> pred) {
    return new FilteredRelation<T1, T2>(relation, pred);
  }
  
  /**
   * Produce a lazy union of two maps, with mappings in {@code child} shadowing those in {@code parent}.
   * Size-related operations have poor performance.
   */
  public static <K, V> LambdaMap<K, V> union(Map<? extends K, ? extends V> parent,
                                             Map<? extends K, ? extends V> child) {
    return new UnionMap<K, V>(parent, child);
  }
  
  /** Produce a lazy transitive composition of two maps.  Size-related operations have poor performance. */
  public static <K, X, V> LambdaMap<K, V> compose(Map<? extends K, ? extends X> left,
                                                  Map<? super X, ? extends V> right) {
    return new ComposedMap<K, X, V>(left, right);
  }
  
  /**
   * Cast the given object to a collection element type if that object is contained by the collection.
   * Assumes the collection is faithful to its specification: an object is contained by the collection
   * if and only if it appears as an element of the {@code Iterator<T>} produced by its {@code iterator()}
   * method (the implication being that the object must have type {@code T}).
   */
  @SuppressWarnings("unchecked")
  public static <T> Option<T> castIfContains(Collection<? extends T> c, Object obj) {
    if (c.contains(obj)) { return Option.some((T) obj); }
    else { return Option.none(); }
  }

  /**
   * Test whether a collection contains some element of a list.
   * @see Collection#containsAll
   * @see IterUtil#or
   */
  public static boolean containsAny(Collection<?> c, Iterable<?> candidates) {
    for (Object o : candidates) {
      if (c.contains(o)) { return true; }
    }
    return false;
  }

  /**
   * Test whether a collection contains all the elements of a list.  Unlike {@link Collection#containsAll},
   * defined for arbitrary {@code Iterable}s.  When possible, delegates to {@code c.containsAll()}.  
   * @see IterUtil#and
   * @see IterUtil#containsAll
   */
  public static boolean containsAll(Collection<?> c, Iterable<?> subset) {
    if (subset instanceof Collection<?>) {
      return c.containsAll((Collection<?>) subset);
    }
    else {
      for (Object o : subset) {
        if (!c.contains(o)) { return false; }
      }
      return true;
    }
  }

  /**
   * Add the given elements to a collection.  Unlike {@link Collection#addAll}, defined for arbitrary
   * {@code Iterable}s.  When possible, delegates to {@code c.addAll()}.
   * @return  {@code true} if {@code c} changed as a result of the call
   */
  public static <E> boolean addAll(Collection<E> c, Iterable<? extends E> elts) {
    if (elts instanceof Collection<?>) {
      @SuppressWarnings("unchecked")  // should be legal, but javac 6 doesn't like it
      Collection<? extends E> eltsColl = (Collection<? extends E>) elts;
      return c.addAll(eltsColl);
    }
    else {
      boolean result = false;
      for (E elt : elts) { result |= c.add(elt); }
      return result;
    }
  }

  /**
   * Remove the given elements from a collection.  Unlike {@link Collection#removeAll}, defined for arbitrary
   * {@code Iterable}s.  When possible, delegates to {@code c.removeAll()}.
   * @return  {@code true} if {@code c} changed as a result of the call
   */
  public static boolean removeAll(Collection<?> c, Iterable<?> elts) {
    if (elts instanceof Collection<?>) {
      return c.removeAll((Collection<?>) elts);
    }
    else {
      boolean result = false;
      for (Object elt : elts) { result |= c.remove(elt); }
      return result;
    }
  }

  /**
   * Remove all but the given elements from a collection.  Unlike {@link Collection#retainAll}, defined for
   * arbitrary {@code Iterable}s.  When possible, delegates to {@code c.retainAll()}.
   * @return  {@code true} if {@code c} changed as a result of the call
   */
  public static boolean retainAll(Collection<?> c, Iterable<?> elts) {
    if (elts instanceof Collection<?>) {
      return c.retainAll((Collection<?>) elts);
    }
    else { return c.retainAll(makeSet(elts)); }
  }

  /**
   * Produce the set containing {@code base} and all values produced an arbitrary number of applications
   * of {@code function} to {@code base}.
   */
  public static <T> Set<T> functionClosure(T base, Lambda<? super T, ? extends T> function) {
    return functionClosure(Collections.singleton(base), function);
  }
  
  /**
   * Produce the set containing the elements in {@code base} and all values produced an arbitrary number
   * of applications of {@code function} to one of these elements.
   */
  public static <T> Set<T> functionClosure(Set<? extends T> base, final Lambda<? super T, ? extends T> function) {
    Lambda<T, Set<T>> neighbors = new Lambda<T, Set<T>>() {
      public Set<T> value(T node) { return Collections.<T>singleton(function.value(node)); }
    };
    return graphClosure(base, neighbors);
  }
  
  /**
   * Produce the set containing {@code base} and all values produced an arbitrary number of applications
   * of {@code function} to {@code base}.
   */
  public static <T> Set<T> partialFunctionClosure(T base, Lambda<? super T, ? extends Option<? extends T>> function) {
    return partialFunctionClosure(Collections.singleton(base), function);
  }
  
  /**
   * Produce the set containing the elements in {@code base} and all values produced an arbitrary number
   * of applications of {@code function} to one of these elements.
   */
  public static <T> Set<T> partialFunctionClosure(Set<? extends T> base,
                                                  final Lambda<? super T, ? extends Option<? extends T>> function) {
    Lambda<T, Set<T>> neighbors = new Lambda<T, Set<T>>() {
      public Set<T> value(T node) { return makeSet(function.value(node)); }
    };
    return graphClosure(base, neighbors);
  }
  
  /** Produce the set of all nodes reachable from {@code base} in a directed graph defined by {@code neighbors}. */
  public static <T> Set<T> graphClosure(T base, Lambda<? super T, ? extends Iterable<? extends T>> neighbors) {
    return graphClosure(Collections.singleton(base), neighbors);
  }
  
  /**
   * Produce the set of all nodes reachable from the elements in {@code base} in a directed graph defined by
   * {@code neighbors}.
   */
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
  
  /**
   * Get the maximal elements of the given list, based on the given order.  All elements in
   * {@code vals} either appear in the result or precede some element in the result.  Where
   * two elements are equivalent (each precedes the other), the second will always be
   * discarded.  
   */
  public static <T> List<T> maxList(Iterable<? extends T> vals, Order<? super T> order) {
    switch (IterUtil.sizeOf(vals, 2)) {
      case 0: return Collections.emptyList();
      case 1: return Collections.singletonList(IterUtil.first(vals));
      default:
        LinkedList<? extends T> workList = makeLinkedList(vals);
        LinkedList<T> result = new LinkedList<T>();
        Iterable<T> remainingTs = IterUtil.compose(workList, result);
        while (!workList.isEmpty()) {
          // prefer discarding later elements when two are equivalent
          T t = workList.removeLast();
          boolean discard = IterUtil.or(remainingTs, LambdaUtil.bindFirst(order, t));
          if (!discard) { result.addFirst(t); }
        }
        return result;
    }
  }
  
  /**
   * Get the maximal elements of the given lists, each known to be a list of maximal elements, based
   * on the given order.  The result is the same as {@code maxList(IterUtil.compose(vals1, vals2), order)},
   * but the implementation is more efficient, because it can avoid performing redundant comparisons.
   */
  public static <T> List<T> composeMaxLists(Iterable<? extends T> vals1, Iterable<? extends T> vals2,
                                            Order<? super T> order) {
    List<T> results2 = new LinkedList<T>();
    for (T t : vals2) {
      // does t precede anything in vals1?
      boolean discard = IterUtil.or(vals1, LambdaUtil.bindFirst(order, t));
      if (!discard) { results2.add(t); }
    }
    List<T> results1 = new LinkedList<T>();
    for (T t : vals1) {
      // does t precede anything in (what's left of) vals2?
      boolean discard = IterUtil.or(results2, LambdaUtil.bindFirst(order, t));
      if (!discard) { results1.add(t); }
    }
    results1.addAll(results2);
    return results1;
  }
  
  /**
   * Get the minimal elements of the given list, based on the given order.  All elements in
   * {@code vals} either appear in the result or are preceded by some element in the result.
   * Where two elements are equivalent (each precedes the other), the second will always be
   * discarded.
   */
  public static <T> List<T> minList(Iterable<? extends T> vals, Order<? super T> order) {
    return maxList(vals, inverse(order));
  }
  
  /**
   * Get the minimal elements of the given lists, each known to be a list of minimal elements, based
   * on the given order.  The result is the same as {@code minList(IterUtil.compose(vals1, vals2), order)},
   * but the implementation is more efficient, because it can avoid performing redundant comparisons.
   */
  public static <T> List<T> composeMinLists(Iterable<? extends T> vals1, Iterable<? extends T> vals2,
                                            Order<? super T> order) {
    return composeMaxLists(vals1, vals2, inverse(order));
  }
  
  /** Get a TotalOrder based on the natural (compareTo-based) order associated with the given type. */
  @SuppressWarnings("unchecked") public static <T extends Comparable<? super T>> TotalOrder<T> naturalOrder() {
    return (TotalOrder<T>) NaturalOrder.INSTANCE;
  }
    
  private static final class NaturalOrder<T extends Comparable<? super T>>
      extends TotalOrder<T> implements Serializable {
    private static final NaturalOrder<Comparable<Object>> INSTANCE = new NaturalOrder<Comparable<Object>>();
    private NaturalOrder() {}
    public int compare(T arg1, T arg2) { return arg1.compareTo(arg2); }
  }
  
  /** Wrap a Comparator as a TotalOrder. */
  public static <T> TotalOrder<T> asTotalOrder(Comparator<? super T> comp) {
    return new ComparatorTotalOrder<T>(comp);
  }
  
  private static final class ComparatorTotalOrder<T> extends TotalOrder<T> {
    private final Comparator<? super T> _comp;
    public ComparatorTotalOrder(Comparator<? super T> comp) { _comp = comp; }
    public int compare(T arg1, T arg2) { return _comp.compare(arg1, arg2); }
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof ComparatorTotalOrder<?>)) { return false; }
      else { return _comp.equals(((ComparatorTotalOrder<?>) o)._comp); }
    }
    public int hashCode() { return ObjectUtil.hash(ComparatorTotalOrder.class, _comp); }
  }
  
  /** Create an inverse of the given order. */
  public static <T> Order<T> inverse(Order<? super T> ord) {
    return new InverseOrder<T>(ord);
  }
  
  private static final class InverseOrder<T> implements Order<T> {
    private final Order<? super T> _ord;
    public InverseOrder(Order<? super T> ord) { _ord = ord; }
    public boolean contains(T arg1, T arg2) { return _ord.contains(arg2, arg1); }
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof InverseOrder<?>)) { return false; }
      else { return _ord.equals(((InverseOrder<?>) o)._ord); }
    }
    public int hashCode() { return ObjectUtil.hash(InverseOrder.class, _ord); }
  }

  /** Create an inverse of the given comparator (or TotalOrder). */
  public static <T> TotalOrder<T> inverse(Comparator<? super T> ord) {
    return new InverseTotalOrder<T>(ord);
  }
  
  private static final class InverseTotalOrder<T> extends TotalOrder<T> {
    private final Comparator<? super T> _ord;
    public InverseTotalOrder(Comparator<? super T> ord) { _ord = ord; }
    public int compare(T arg1, T arg2) { return _ord.compare(arg2, arg1); }
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof InverseTotalOrder<?>)) { return false; }
      else { return _ord.equals(((InverseTotalOrder<?>) o)._ord); }
    }
    public int hashCode() { return ObjectUtil.hash(InverseTotalOrder.class, _ord); }
  }
  
  /**
   * A partial order checking whether the first element is a subset of the second; implemented with
   * {@link IterUtil#containsAll} and {@link Collection#containsAll}.
   */
  public static final Order<Iterable<?>> SUBSET_ORDER = new SubsetOrder();
  
  private static final class SubsetOrder implements Order<Iterable<?>>, Serializable {
    private SubsetOrder() {}
    public boolean contains(Iterable<?> sub, Iterable<?> sup) { return IterUtil.containsAll(sup, sub); }
  }
  
  /**
   * A partial order checking whether the first element is a substring of the second; implemented with
   * {@link String#contains}.
   */
  public static final Order<String> SUBSTRING_ORDER = new SubstringOrder();
  
  private static final class SubstringOrder implements Order<String>, Serializable {
    private SubstringOrder() {}
    public boolean contains(String sub, String sup) { return sup.contains(sub); }
  }
  
  /**
   * A partial order checking whether the first element is a prefix of the second; implemented with
   * {@link String#contains}.
   */
  public static final Order<String> STRING_PREFIX_ORDER = new StringPrefixOrder();
  
  private static final class StringPrefixOrder implements Order<String>, Serializable {
    private StringPrefixOrder() {}
    public boolean contains(String pre, String s) { return s.startsWith(pre); }
  }

}
