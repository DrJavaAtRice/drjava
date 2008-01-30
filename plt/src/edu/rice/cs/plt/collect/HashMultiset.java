/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Array;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A {@link HashMap}-based implementation of {@link Multiset}.  Values in the set
 * must be valid as keys in a {@code HashMap}.  Care is taken so that most operations
 * are either performed in constant time or time linear to the number of <em>unique</em>
 * elements in the multiset (as opposed to time linear in the size of the multiset, which
 * may be significantly larger).
 */
public class HashMultiset<T> implements Multiset<T> {
  
  private HashMap<T, Integer> _counts;
  private int _size;
  
  /** Create an empty {@code HashMultiset} */
  public HashMultiset() { _counts = new HashMap<T, Integer>(); }
  
  /** Create a {@code HashMultiset} containing all the elements of {@code coll} */
  public HashMultiset(Collection<? extends T> coll) {
    this();
    addAll(coll);
  }
  
  public int size() { return _size; }

  public boolean isEmpty() { return _size == 0; }
  
  public boolean contains(Object obj) { return _counts.containsKey(obj); }
  
  public int count(Object value) { 
    if (_counts.containsKey(value)) { return _counts.get(value); }
    else { return 0; }
  }
  
  /** 
   * @return  A set view of the multiset.  Removing a value from the set is the equivalent
   *          of invoking {@link #removeAllInstances} for that value.  Adding to the set
   *          is not supported.
   */
  public Set<T> asSet() { return _counts.keySet(); }
  
  public Iterator<T> iterator() {
    final Iterator<Map.Entry<T, Integer>> mapIter = _counts.entrySet().iterator();
    return new Iterator<T>() {
      
      private Map.Entry<T, Integer> currentEntry = null;
      private int i;
      private boolean removed = false;

      public boolean hasNext() { 
        return mapIter.hasNext() || (currentEntry != null && i < currentEntry.getValue());
      }
      
      public T next() {
        if (currentEntry == null || i >= currentEntry.getValue()) { 
          currentEntry = mapIter.next(); 
          i = 0;
        }
        removed = false;
        i++; // i will never be 0 at this point
        return currentEntry.getKey();
      }
      
      public void remove() {
        if (currentEntry == null || removed) { throw new IllegalStateException(); }
        else {
          removed = true;
          _size--;
          int oldCount = currentEntry.getValue();
          if (oldCount == 1) {
            currentEntry = null;
            mapIter.remove();
          }
          else {
            currentEntry.setValue(oldCount - 1);
            i--; // guaranteed to be at least 1 before decrementing
          }
        }
      }
      
    };
  }
  
  public Object[] toArray() {
    Object[] result = new Object[_size];
    int i = 0;
    for (Object obj : this) { result[i] = obj; i++; }
    return result;
  }
  
  public <E> E[] toArray(E[] fill) {
    if (fill.length < _size) { 
      @SuppressWarnings("unchecked") E[] newFill = 
        (E[]) Array.newInstance(fill.getClass().getComponentType(), _size);
      fill = newFill;
    }
    int i = 0;
    for (T elt : this) {
      @SuppressWarnings("unchecked") E asE = (E) elt;
      fill[i] = asE;
      i++;
    }
    if (i < fill.length) { fill[i] = null; i++; }
    return fill;
  }
  
  public boolean add(T val) { 
    _counts.put(val, count(val) + 1);
    _size++;
    return true;
  }
  
  public boolean add(T val, int instances) { 
    _counts.put(val, count(val) + instances);
    _size += instances;
    return true;
  }
  
  public boolean remove(Object obj) {
    if (!contains(obj)) { return false; }
    else { doRemove(obj, 1); return true; }
  }
  
  public boolean remove(Object obj, int instances) {
    if (!contains(obj)) { return false; }
    else { doRemove(obj, instances); return true; }
  }
  
  public boolean removeAllInstances(Object obj) {
    if (!contains(obj)) { return false; }
    else { doRemove(obj, count(obj)); return true; }
  }
  
  private void doRemove(Object key, int instances) {
    int newCount = count(key) - instances;
    if (newCount <= 0) {
      _counts.remove(key);
      int actualInstances = instances - newCount;
      _size -= actualInstances;
    }
    else {
      @SuppressWarnings("unchecked") T keyAsT = (T) key;
      // guaranteed safe, because _counts contains key
      _counts.put(keyAsT, newCount);
      _size -= instances;
    }
  }
  
  public boolean containsAll(Collection<?> c) {
    for (Object obj : asMultiset(c).asSet()) { if (!contains(c)) { return false; } }
    return true;
  }
  
  public boolean addAll(Collection<? extends T> coll) {
    boolean result = false;
    Multiset<? extends T> collMultiset = asMultiset(coll);
    for (T entry : collMultiset.asSet()) {
      result |= add(entry, collMultiset.count(entry));
    }
    return result;
  }
  
  public boolean removeAll(Collection<?> coll) {
    if (coll instanceof Multiset<?> && coll.size() > _size) { return removeAllByThis(coll); }
    else { return removeAllByThat(coll); }
  }
  
  /** 
   * Remove all by iterating over {@code this}.  Linear in the size of {@code asSet()}, plus the 
   * time required for {@code asMultiset(coll)}.
   */
  private boolean removeAllByThis(Collection<?> coll) {
    boolean result = false;
    Multiset<?> collMultiset = asMultiset(coll);
    Iterator<Map.Entry<T, Integer>> iter = _counts.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<T, Integer> entry = iter.next();
      if (collMultiset.contains(entry.getKey())) {
        int delta = collMultiset.count(entry.getKey());
        int newCount = entry.getValue() - delta;
        if (newCount <= 0) {
          iter.remove();
          int actualDelta = delta - newCount;
          _size -= actualDelta;
          result = true;
        }
      }
    }
    return result;
  }
  
  /**
   * Remove all by iterating over {@code coll}.  Linear in the the number of unique elements in, 
   * {@code coll}, plus the time required for {@code asMultiset(coll)}.
   */
  private boolean removeAllByThat(Collection<?> coll) {
    boolean result = false;
    Multiset<?> collMultiset = asMultiset(coll);
    for (Object obj : collMultiset.asSet()) { result |= remove(obj, collMultiset.count(obj)); }
    return result;
  }

  
  public boolean retainAll(Collection<?> coll) {
    boolean result = false;
    Multiset<?> collMultiset = asMultiset(coll);
    Iterator<Map.Entry<T, Integer>> iter = _counts.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<T, Integer> entry = iter.next();
      if (collMultiset.contains(entry.getKey())) {
        int newCount = collMultiset.count(entry.getKey());
        if (entry.getValue() > newCount) {
          _size -= (entry.getValue() - newCount);
          entry.setValue(newCount);
          result = true;
        }
      }
      else { _size -= entry.getValue(); iter.remove(); result = true; }
    }
    return result;
  }
  
  public void clear() { _counts.clear(); }
  
  /** 
   * @return  A string representation of the multimap, as generated by 
   *          {@link IterUtil#toString}
   */
  public String toString() { return IterUtil.toString(this); }
  
  public boolean equals(Object obj) {
    if (this == obj) { return true; }
    else if (!(obj instanceof Multiset<?>)) { return false; }
    else {
      Multiset<?> cast = (Multiset<?>) obj;
      if (_size == cast.size()) {
        for (Object elt : cast.asSet()) {
          if (count(elt) != cast.count(elt)) { return false; }
        }
        return true;
      }
      else { return false; }
    }
  }
  
  public int hashCode() {
    int result = 0;
    for (T elt : asSet()) { result ^= (elt == null ? 0 : elt.hashCode()) ^ count(elt); }
    return result;
  }
  
  /** Convert {@code coll} to a multiset by casting or, where necessary, allocation */
  private <T> Multiset<T> asMultiset(Collection<T> coll) {
    if (coll instanceof Multiset<?>) { return (Multiset<T>) coll; }
    else { return new HashMultiset<T>(coll); }
  }
  
}
