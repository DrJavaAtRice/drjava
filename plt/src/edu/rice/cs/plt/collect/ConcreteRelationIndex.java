/*BEGIN_COPYRIGHT_BLOCK*
 * 
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

import java.io.Serializable;
import java.util.*;
import java.lang.ref.WeakReference;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * <p>A RelationIndex implementation that maintains concrete data structures to index the contents of
 * the relation.  To support mutation in <em>other</em> indices to reflect changes made directly to
 * sets and iterators produced by this index, clients should override the {@link #addToRelation},
 * {@link #removeFromRelation}, and {@link #clearRelation} methods.</p>
 * 
 * <p>Keys must have a valid {@code hashCode()} implementation.</p>
 */
public class ConcreteRelationIndex<K, V> implements RelationIndex<K, V>, Serializable {
  // hold on to (possibly-empty) sets as long as they are referenced, but discard them
  // if that's not the case
  private final Map<K, WeakReference<ValueSet>> _valueSets;
  private final Map<K, PredicateSet<V>> _nonEmptyValueSets;
  private final Thunk<? extends Set<V>> _setFactory;
  private int _size;
  
  /**
   * Create an empty ConcreteRelationIndex.
   * @param mapFactory  Produces an empty map for mapping keys to sets of values.  The mutability
   *                    of {@code keys()} corresponds to this map's {@code keySet()}'s mutability.
   * @param setFactory  Produces an initial set to hold values associated with a key.
   */
  public ConcreteRelationIndex(Thunk<? extends Map<K, PredicateSet<V>>> mapFactory,
                               Thunk<? extends Set<V>> setFactory) {
    _valueSets = new HashMap<K, WeakReference<ValueSet>>();
    _nonEmptyValueSets = mapFactory.value();
    _setFactory = setFactory;
    _size = 0;
  }
  
  /**
   * Checks that the given pair, which does not appear in this index, can be added to the relation.
   * Called when a client attempts to directly mutate a match set, but before any mutation has
   * occurred.  If the add is not permitted, subclasses may throw an exception.  By default, does
   * nothing.
   */
  protected void validateAdd(K key, V value) {}
  
  /**
   * Add the given entry to the relation.  Called when a client directly mutates a match set, and 
   * after this index has been mutated.  By default, does nothing.
   */
  protected void addToRelation(K key, V value) {}
  
  /**
   * Checks that the given pair, which appears in this index, can be removed from the relation.
   * Called when a client attempts to directly mutate a match set, but before any mutation has
   * occurred.  If the removal is not permitted, subclasses may throw an exception.  By 
   * default, does nothing.
   */
  protected void validateRemove(K key, V value) {}
  
  /**
   * Checks that the given key, which appears with at least one value in this index, can be removed 
   * with all associated values from the relation.  Called when a client attempts to directly mutate
   * the key set or a match set, but before any mutation has occurred.  If the removal is not
   * permitted, subclasses may throw an exception.  By default, does nothing.
   */
  protected void validateRemoveKey(K key, PredicateSet<V> vals) {}
  
  /**
   * Remove the given entry from the relation.  Called when a client directly mutates the key set or a 
   * match set, and after this index has been mutated.  By default, does nothing.
   */
  protected void removeFromRelation(K key, V value) {}
  
  /**
   * Checks that the relation can be cleared.  Called when a client attempts to directly clear the key 
   * set, but before any mutation has occurred.  If clearing is not permitted, subclasses may throw 
   * an exception.  By  default, does nothing.
   */
  protected void validateClear() {}
  
  /**
   * Clear the relation.  Called when a client directly clears the key set, and after this index has
   * been mutated.  By default, does nothing.
   */
  protected void clearRelation() {}
  
  public boolean contains(Object key, Object value) {
    PredicateSet<V> vals = _nonEmptyValueSets.get(key);
    return (vals != null) && vals.contains(value);
  }
  
  public PredicateSet<K> keys() { return new KeySet(); }
  public PredicateSet<V> match(K key) { return findMatch(key); }
  public Iterator<Pair<K, V>> iterator() { return new EntryIterator(); }
  
  public boolean isEmpty() { return _nonEmptyValueSets.isEmpty(); }
  public int size() { return _size; }
  public int size(int bound) { return _size < bound ? _size : bound; }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return false; }
  
  public void added(K key, V value) { findMatch(key).doAdd(value); }
  public void removed(K key, V value) { findMatch(key).doRemove(value); }
  
  public void cleared() {
    for (K key : _nonEmptyValueSets.keySet()) {
      // the ValueSet is in _nonEmptyValueSets, so the reference must be defined
      _valueSets.get(key).get().doClear(false);
    }
    _nonEmptyValueSets.clear();
  }
  
  /**
   * Implementation of match.  Allows subclasses to override match while still making this
   * functionality available to internal opeerations.
   */
  private ValueSet findMatch(K key) {
    WeakReference<ValueSet> ref = _valueSets.get(key);
    if (ref != null) {
      ValueSet result = ref.get();
      if (result != null) { return result; }
    }
    return new ValueSet(key);
  }
  
  /**
   * Supports removal by clearing the mapped-to value sets after delegating to the wrapped set's
   * mutation methods.  Clients can prevent mutation by providing a map factory that produces
   * maps with immutable key sets.
   */
  private class KeySet extends DelegatingSet<K> {
    public KeySet() { super(_nonEmptyValueSets.keySet()); }
    
    @Override public boolean add(K obj) { throw new UnsupportedOperationException(); }
    @Override public boolean addAll(Collection<? extends K> c) {
      throw new UnsupportedOperationException();
    }
    
    @Override public Iterator<K> iterator() {
      return new Iterator<K>() {
        final Iterator<K> _i = _delegate.iterator();
        K _last = null;
        public boolean hasNext() { return _i.hasNext(); }
        public K next() { _last = _i.next(); return _last; }
        public void remove() {
          if (_last == null) { throw new IllegalStateException(); }
          // get a reference in order to prevent garbage collection
          ValueSet vals = _valueSets.get(_last).get();
          validateRemoveKey(_last, new ImmutableSet<V>(vals));
          _i.remove();
          vals.clearAndNotifyAfterMapRemoval();
        }
      };
    }
    
    @Override public boolean remove(Object o) {
      Option<K> cast = CollectUtil.castIfContains(this, o);
      if (cast.isSome()) {
        K key = cast.unwrap();
        // get a reference in order to prevent garbage collection
        ValueSet vals = _valueSets.get(key).get();
        validateRemoveKey(key, new ImmutableSet<V>(vals));
        _delegate.remove(key);
        vals.clearAndNotifyAfterMapRemoval();
        return true;
      }
      else { return false; }
    }
    
    @Override public void clear() {
      validateClear();
      // get references to ValueSets to prevent garbage collection
      List<ValueSet> toClear = new ArrayList<ValueSet>(_delegate.size());
      for (K key : _delegate) { toClear.add(_valueSets.get(key).get()); }
      // attempt clear before performing any other mutation (it might fail)
      _delegate.clear();
      for (ValueSet v : toClear) { v.clearAfterMapRemoval(); }
      // notify in one operation, rather than for each element:
      clearRelation();
    }
    
    @Override public boolean removeAll(Collection<?> c) {
      return abstractCollectionRemoveAll(c);
    }
    
    @Override public boolean retainAll(Collection<?> c) {
      return abstractCollectionRetainAll(c);
    }
  }
  
  /**
   * Adds/removes itself from the non-empty map as it changes, and removes the emptyMap entry
   * when garbage-collected.  Notifies the relation after changes are made.
   */
  private class ValueSet extends DelegatingSet<V> implements Serializable {
    private final K _key;
    private int _size;
    
    public ValueSet(K key) {
      super(_setFactory.value());
      _key = key;
      _size = _delegate.size();
      _valueSets.put(key, new WeakReference<ValueSet>(this));
      // though unusual, the set factory might produce non-empty sets:
      if (!_delegate.isEmpty()) { _nonEmptyValueSets.put(_key, this); }
    }
    
    @Override protected void finalize() throws Throwable {
      try { _valueSets.remove(_key); }
      finally { super.finalize(); }
    }
    
    @Override public boolean isEmpty() { return _size == 0; }
    @Override public int size() { return _size; }
    @Override public int size(int bound) { return _size < bound ? _size : bound; }
    
    @Override public ValueSetIterator iterator() { return new ValueSetIterator(); }
    
    @Override public boolean add(V val) {
      boolean result = !_delegate.contains(val);
      if (result) {
        validateAdd(_key, val);
        _delegate.add(val);
        finishAdd();
        addToRelation(_key, val);
      }
      return result;
    }
    
    @Override public boolean remove(Object o) {
      Option<V> cast = CollectUtil.castIfContains(_delegate, o);
      if (cast.isSome()) {
        V val = cast.unwrap();
        validateRemove(_key, val);
        _delegate.remove(val);
        finishRemove();
        removeFromRelation(_key, val);
        return true;
      }
      else { return false; }
    }
    
    @Override public void clear() {
      if (_size != 0) {
        validateRemoveKey(_key, new ImmutableSet<V>(this));
        Iterable<V> vals = IterUtil.snapshot(this);
        _delegate.clear();
        finishClear(true);
        for (V val : vals) { removeFromRelation(_key, val); }
      }
    }
    
    @Override public boolean addAll(Collection<? extends V> c) {
      return abstractCollectionAddAll(c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return abstractCollectionRetainAll(c);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return abstractCollectionRemoveAll(c);
    }
    
    /** Clear the set and notify the relation of the change.  nonEmptyValueSets is already updated. */
    public void clearAndNotifyAfterMapRemoval() {
      if (_size != 0) {
        Iterable<V> vals = IterUtil.snapshot(this);
        try { _delegate.clear(); }
        catch (RuntimeException e) {
          // couldn't successfully clear
          _nonEmptyValueSets.put(_key, this);
          throw e;
        }
        finishClear(false);
        for (V val : vals) { removeFromRelation(_key, val); }
      }
    }
    
    /** Clear the set.  Do not notify the relation.  nonEmptyValueSets is already updated. */
    public void clearAfterMapRemoval() {
      if (_size != 0) {
        try { _delegate.clear(); }
        catch (RuntimeException e) {
          // couldn't successfully clear
          _nonEmptyValueSets.put(_key, this);
          throw e;
        }
        finishClear(false);
      }
    }
    
    /** Add an element without notifying the relation.  Automatically update {@code nonEmptyValueSets}. */
    public void doAdd(V val) {
      boolean changed = _delegate.add(val);
      // changed should always be true, but we'll verify as a sanity check
      if (changed) { finishAdd(); }
    }
    
    /** Remove an element without notifying the relation.  Automatically update {@code nonEmptyValueSets}. */
    public void doRemove(V val) {
      boolean changed = _delegate.remove(val);
      // changed should always be true, but we'll verify as a sanity check
      if (changed) { finishRemove(); }
    }
    
    /** Clear the set without notifying the relation.  Update {@code nonEmptyValueSets} iff the flag is true. */
    public void doClear(boolean removeFromMap) {
      if (_size != 0) {
        _delegate.clear();
        finishClear(removeFromMap);
      }
    }
    
    /** Bookkeeping after an add mutates the set. */
    private void finishAdd() {
      if (_size == 0) { _nonEmptyValueSets.put(_key, this); }
      _size++;
      ConcreteRelationIndex.this._size++;
    }
    
    /** Bookkeeping after a remove mutates the set. */
    private void finishRemove() {
      _size--;
      ConcreteRelationIndex.this._size--;
      if (_size == 0) { _nonEmptyValueSets.remove(_key); }
    }
    
    /**
     * Bookkeeping after a remove mutates the set.
     * @param nonEmptyMapIterator  An iterator that can be used to remove this set from nonEmptyValueSets.
     */
    private void finishRemove(Iterator<Map.Entry<K, PredicateSet<V>>> nonEmptyMapIterator) {
      _size--;
      ConcreteRelationIndex.this._size--;
      if (_size == 0) { nonEmptyMapIterator.remove(); }
    }
    
    /** Bookkeeping after a clear mutates the set. */
    private void finishClear(boolean removeFromMap) {
      ConcreteRelationIndex.this._size -= _size;
      _size = 0;
      if (removeFromMap) { _nonEmptyValueSets.remove(_key); }
    }
    
    /** An iterator that supports removal, and allows a nonEmptyMapIterator removal parameter. */
    public final class ValueSetIterator implements Iterator<V> {
      private final Iterator<? extends V> _i;
      private V _last; // null before iteration starts
      public ValueSetIterator() { _i = _delegate.iterator(); _last = null; }
      public boolean hasNext() { return _i.hasNext(); }
      public V next() { _last = _i.next(); return _last; }
      public void remove() {
        if (_last == null) { throw new IllegalStateException(); }
        validateRemove(_key, _last);
        _i.remove();
        finishRemove();
        removeFromRelation(_key, _last);
      }
      public void remove(Iterator<Map.Entry<K, PredicateSet<V>>> nonEmptyMapIterator) {
        if (_last == null) { throw new IllegalStateException(); }
        validateRemove(_key, _last);
        _i.remove();
        finishRemove(nonEmptyMapIterator);
        removeFromRelation(_key, _last);
      }
    }
  }
  
  private class EntryIterator implements Iterator<Pair<K, V>> {
    private final Iterator<Map.Entry<K, PredicateSet<V>>> _entries;
    private K _currentKey;
    private PredicateSet<V> _currentValues;
    private Iterator<V> _valuesIter;
    
    public EntryIterator() {
      _entries = _nonEmptyValueSets.entrySet().iterator();
      _currentKey = null;
      _currentValues = null;
      _valuesIter = null;
    }
    
    public boolean hasNext() {
      return (_valuesIter != null && _valuesIter.hasNext()) || _entries.hasNext();
    }
    
    public Pair<K, V> next() {
      if (_valuesIter == null || !_valuesIter.hasNext()) {
        Map.Entry<K, PredicateSet<V>> entry = _entries.next();
        _currentKey = entry.getKey();
        _currentValues = entry.getValue();
        _valuesIter = _currentValues.iterator();
      }
      return new Pair<K, V>(_currentKey, _valuesIter.next());
    }
    
    @SuppressWarnings("unchecked")
    public void remove() {
      // javac 5 has a bug which rejects the following instanceof type:
      if (_valuesIter instanceof ConcreteRelationIndex<?, ?>.ValueSet.ValueSetIterator) {
        // The following statement does not type check:
//        ((ConcreteRelationIndex<?, ?>.ValueSet.ValueSetIterator) _valuesIter).remove(_entries);
        // workaround: (which lies about types)
        ((ConcreteRelationIndex<K, V>.ValueSet.ValueSetIterator) _valuesIter).remove(_entries);
      }
      else { throw new UnsupportedOperationException(); }
    }
  }
  
}
