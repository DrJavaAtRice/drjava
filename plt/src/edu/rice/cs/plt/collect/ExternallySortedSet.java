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
import edu.rice.cs.plt.iter.SizedIterable;

/** <p>A container class that <em>almost</em> implements the {@link java.util.SortedSet} interface;
  * the difference is that {@code add()} takes two parameters -- an element, along with a 
  * corresponding {@link Comparable} that will be used to sort the set.  Thus, the set is not 
  * sorted based on any operations defined for the element type; rather, it is sorted according 
  * to an external {@code orderBy} value.  The {@code subSet()}, etc., methods are also adjusted 
  * to take in {@code orderBy} values rather than set elements.</p>
  * 
  * <p>The result is a set that works essentially like a {@link java.util.TreeSet}
  * of {@link edu.rice.cs.plt.tuple.Pair}s in which one of the Pair elements is a Comparable, 
  * and for which a Comparator is correctly defined.  One significant difference is that this
  * implementation does not return {@code true} for the expression {@code add(1, 1) && add(1, 2)},
  * while the explicit Pair implementation would (assuming the Set were initially empty).</p>
  * 
  * <p>Note: the implementation relies heavily on hashing, so good performance is
  * dependent on elements of the set having an efficient {@code hashCode()} implementation.</p>
  */
public class ExternallySortedSet<T, C extends Comparable<? super C>> implements SizedIterable<T> {

  /** The sorted set of elements.  Note that operations involving elements
    * <em>not</em> in {@ink #_orderByMap} should not be allowed, since the comparator
    * will not be able to handle such elements.
    */
  private final SortedSet<T> _set;
  
  /** Maps elements to their corresponding {@code orderBy} value. Guaranteed to be defined
    * for <em>at least</em> every element of {@link #_set}.
    */
  private final Map<T, C> _orderByMap;
  
  /** May be {@code null}, indicating no bound. */
  private final C _lowerBound;
  
  /** May be {@code null}, indicating no bound. */
  private final C _upperBound;
  
  /** Creates an empty set. */
  public ExternallySortedSet() {
    _set = new TreeSet<T>(new Comparator<T>() {
      // Assumes that t1 and t2 are keys in _orderByMap.
      public int compare(T t1, T t2) { 
        return _orderByMap.get(t1).compareTo(_orderByMap.get(t2));
      }
    });
    
    _orderByMap = new HashMap<T, C>();
    _lowerBound = null;
    _upperBound = null;
  }
  
  /** Creates a set with the specified fields. */
  private ExternallySortedSet(SortedSet<T> set, Map<T, C> orderByMap, C lowerBound, 
                              C upperBound) {
    _set = set;
    _orderByMap = orderByMap;
    _lowerBound = lowerBound;
    _upperBound = upperBound;
  }
  
  public boolean isEmpty() { return _set.isEmpty(); }
  
  public int size() { return _set.size(); }
  
  public int size(int bound) {
    int result = _set.size();
    return result <= bound ? result : bound;
  }
 
  public boolean isInfinite() { return false; }
  
  public boolean hasFixedSize() { return false; }
  
  public boolean isStatic() { return false; }
  
  public boolean contains(Object element) {
    // We can only call _set.contains() if a mapping is defined.
    return _orderByMap.containsKey(element) && _set.contains(element);
  }
  
  /**
   * @return  An Iterator over the elements of the set in their sorted order.  The
   *          iterator supports {@link Iterator#remove()}.
   */
  public Iterator<T> iterator() {
    final Iterator<T> setI = _set.iterator();
    return new Iterator<T>() {
      private T _last = null;
      
      public boolean hasNext() { return setI.hasNext(); }
      
      public T next() { 
        _last = setI.next();
        return _last;
      }
      
      public void remove() { 
        if (_last == null) { throw new IllegalStateException(); }
        else {
          setI.remove();
          _orderByMap.remove(_last);
          _last = null;
        }
      }
      
    };
  }
  
  /** 
   * @return  An array of the set elements in their sorted order.  As in the {@link Set} interface,
   *          changes to the array will not be reflected in the set.
   */
  public Object[] toArray() { return _set.toArray(); }
  
  /** 
   * @return  An array of the set elements in their sorted order.  As in the {@link Set} interface,
   *          changes to the array will not be reflected in the set.
   */
  public <S> S[] toArray(S[] a) { return _set.toArray(a); }
  
  /** Add {@code element} to the set, sorted by {@code orderBy}.
    * @return  {@code true} iff {@code element} was not already in the set and could be added.
    * @throws IllegalArgumentException  if {@code this} is a subset of a larger set, and the
    *                                  element being added is outside the specified bounds.
    * @throws NullPointerException  if {@code element} is {@code null}
    */
  public boolean add(T element, C orderBy) {
    if (element == null) { throw new NullPointerException(); }
    else {
      assertInBounds(orderBy);
      if (contains(element)) { return false; }
      else {
        _orderByMap.put(element, orderBy);
        _set.add(element);
        return true;
      }
    }
  }
  
  /**
   * Removes the element specified from the set.
   * @return  {@code true} iff the set contained {@code element}.
   */
  public boolean remove(Object element) {
    if (contains(element)) {
      _set.remove(element);
      _orderByMap.remove(element);
      return true;
    }
    else { return false; }
  }
  
  /**
   * @return  {@code true} iff the set contains each of the elements in {@code i}.
   */
  public boolean containsAll(Iterable<?> i) {
    for (Object o : i) { if (! contains(o)) { return false; } }
    return true;
  }
  
  /**
   * Add every element of {@code s} to this set (if it is not already present).
   * @return  {@code true} iff the operation modified this set.
   */
  public boolean addAll(ExternallySortedSet<? extends T, ? extends C> s) {
    if ((_lowerBound == null || 
         (s._lowerBound != null && _lowerBound.compareTo(s._lowerBound) <= 0)) &&
        (_upperBound == null ||
         (s._upperBound != null && _upperBound.compareTo(s._upperBound) >= 0))) {
      
      return uncheckedAddAll(s);
    }
    else {
      return checkedAddAll(s);
    }
  }
  
  /** Add the elements by invoking {@link #add} on each one. */
  private boolean checkedAddAll(ExternallySortedSet<? extends T, ? extends C> s) {
    boolean result = false;
    for (T t : s) {
      // "|" instead of "||" to avoid short-circuit
      result = result | add(t, s._orderByMap.get(t));
    }
    return result;
  }
  
  /** 
   * Add the elements without calling {@link #add} on each one, by simply taking the union 
   * of the two data structures.  This avoids a bounds check on each element.  Assumes that each 
   * element of {@code s} is within this set's bounds.
   */
  private boolean uncheckedAddAll(ExternallySortedSet<? extends T, ? extends C> s) {
    for (Map.Entry<? extends T, ? extends C> e : s._orderByMap.entrySet()) {
      if (! _orderByMap.containsKey(e.getKey())) { _orderByMap.put(e.getKey(), e.getValue()); }
    }
    boolean result = _set.addAll(s._set);
    return result;
  }
  
  /**
   * Removes every element of this set that is not in {@code c}.
   * @return  {@code true} iff the operation modified this set.
   */
  public boolean retainAll(Collection<?> c) {
    boolean result = false;
    for (Iterator<T> i = iterator(); i.hasNext(); ) {
      T t = i.next();
      if (! c.contains(t)) { i.remove(); result = true; }
    }
    return result;
  }
  
  /**
   * Removes every element of this set that is not in {@code s}.
   * @return  {@code true} iff the operation modified this set.
   */
  public boolean retainAll(ExternallySortedSet<?, ?> s) {
    boolean result = false;
    for (Iterator<T> i = iterator(); i.hasNext(); ) {
      T t = i.next();
      if (! s.contains(t)) { i.remove(); result = true; }
    }
    return result;
  }
    
  /**
   * Removes every element of this set that is in {@code c}.
   * @return  {@code true} iff the operation modified this set.
   */
  public boolean removeAll(Iterable<?> i) {
    boolean result = false;
    // "|" instead of "||" to avoid short-circuit
    for (Object o : i) { result = result | remove(o); }
    return result;
  }
  
  /**
   * Removes every element of this set.
   */
  public void clear() {
    // We can't clear the map, because it might contain mappings outside the range of this set.
    for (T t : _set) { _orderByMap.remove(t); }
    _set.clear();
  }
  
  /**
   * @return  A set containing every element in this set sorted from {@code from} (inclusive)
   *          to {@code to} (exclusive).  The result is backed by this set and bounded by 
   *          {@code from} and {@code to}; changes to either {@code this} or the result are 
   *          reflected by both.
   * @throws IllegalArgumentException  if {@code from} or {@code to} is outside this set's bounds
   */
  public ExternallySortedSet<T, C> subSet(C from, C to) {
    assertInBounds(from);
    assertInBounds(to);
    T fromElement = firstAt(from);
    T toElement = firstAt(to);

    SortedSet<T> subSet;
    if (fromElement == null) {
      if (toElement == null) { subSet = _set; }
      else { subSet = _set.headSet(toElement); }
    }
    else if (toElement == null) { subSet = _set.tailSet(fromElement); }
    else { subSet = _set.subSet(fromElement, toElement); }
      
    return new ExternallySortedSet<T, C>(subSet, _orderByMap, from, to);
  }
  
  /**
   * @return  A set containing every element in this set sorted before {@code to} (exclusive).
   *          The result is backed by this set, so changes to either are reflected by both.
   * @throws IllegalArgumentException  if {@code to} is outside this set's bounds
   */
  public ExternallySortedSet<T, C> headSet(C to) {
    assertInBounds(to);
    T toElement = firstAt(to);
    SortedSet<T> subSet;
    if (toElement == null) { subSet = _set; }
    else { subSet = _set.headSet(toElement); }
    return new ExternallySortedSet<T, C>(subSet, _orderByMap, null, to);
  }
  
  /**
   * @return  A set containing every element in this set sorted after {@code from} (inclusive).
   *          The result is backed by this set, so changes to either are reflected by both.
   * @throws IllegalArgumentException  if {@code from} is outside this set's bounds
   */
  public ExternallySortedSet<T, C> tailSet(C from) {
    assertInBounds(from);
    T fromElement = firstAt(from);
    SortedSet<T> subSet;
    if (fromElement == null) { subSet = _set; }
    else { subSet = _set.tailSet(fromElement); }
    return new ExternallySortedSet<T, C>(subSet, _orderByMap, from, null);
  }
  
  public T first() { return _set.first(); }
  
  public T last() { return _set.last(); }
  
  /**
   * @return  The first element in the set ordered at or after {@code c}, or {@code null} if 
   *          there is no such element.
   */
  private T firstAt(C c) {
    _orderByMap.put(null, c); // We *don't* put null in _set.
    SortedSet<T> resultSet = _set.tailSet(null);
    T result = null;
    if (! resultSet.isEmpty()) { result = resultSet.first(); }
    _orderByMap.remove(null);
    return result;
  }
  
  private void assertInBounds(C c) {
    if (_lowerBound != null && c.compareTo(_lowerBound) < 0) {
      throw new IllegalArgumentException(c + " is < this set's lower bound: " + _lowerBound);
    }
    if (_upperBound != null && c.compareTo(_upperBound) >= 0) {
      throw new IllegalArgumentException(c + " is >= this set's upper bound: " + _upperBound);
    }
  }
  
}
