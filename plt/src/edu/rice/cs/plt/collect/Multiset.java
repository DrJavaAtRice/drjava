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

import java.util.Collection;
import java.util.Iterator;
import edu.rice.cs.plt.iter.SizedIterable;

/**
 * A set-like collection that allows multiple instances of a value to be represented
 * in the collection.
 */
public interface Multiset<T> extends Collection<T>, SizedIterable<T> {
  
  /** The size of the multiset. */
  public int size();
  
  /** The size of the multiset bounded by the given value. */
  public int size(int bound);
  
  /** Whether the iterator has an infinite number of elements. */
  public boolean isInfinite();
  
  /** Whether the multiset can change size. */
  public boolean hasFixedSize();
  
  /** Whether the multiset will always contain the same values. */
  public boolean isStatic();

  /** Whether the multiset is empty. */
  public boolean isEmpty();
  
  /** Whether the multiset contains at least one instance of {@code obj}. */
  public boolean contains(Object obj);
  
  /**
   * The number of times {@code value} appears in the multiset (if it does not appear,
   * the result is {@code 0}).  If the number is too large, {@code Integer.MAX_VALUE} may
   * be returned.
   */
  public int count(Object value);
  
  /**
   * Produce a set view of the multiset (with only one entry for each unique instance).  The 
   * set will change as subsequent modifications are made to the multiset; implementations may
   * choose to allow external modifications to the set as well.
   */
  public PredicateSet<T> asSet();
  
  /**
   * An iterator for the multiset.  If the set contains {@code n} instances of a 
   * value, that value will appear {@code n} times during iteration.  Invoking 
   * {@code remove} on the iterator removes one instance.
   */
  public Iterator<T> iterator();
  
  /** Fill an array with the contents of the multiset. */
  public Object[] toArray();
  
  /** Fill an array with the contents of the multiset. */
  public <S> S[] toArray(S[] fill);
  
  /**
   * Add a single instance of {@code val} to the multiset.
   * @return  {@code true} (indicating that the multiset was modified)
   */
  public boolean add(T val);
  
  /**
   * Add the given number of instances of {@code val} to the multiset.
   * @return  {@code true} (indicating that the multiset was modified)
   */
  public boolean add(T val, int instances);
  
  /**
   * Remove a single instance of {@code obj} from the multiset, if one exists.
   * @return  {@code true} iff the multiset was modified
   */
  public boolean remove(Object obj);
  
  /**
   * Remove the given number of instances of {@code obj} from the multiset.  If 
   * {@code count(obj) <= instances}, removes all instances of the given value.
   * @return  {@code true} iff the multiset was modified
   */
  public boolean remove(Object obj, int instances);
  
  /**
   * Remove all instances of {@code obj} from the multiset
   * @return  {@code true} iff the multiset was modified
   */
  public boolean removeAllInstances(Object obj);

  /** Whether each element of the collection appears at least once in this multiset. */
  public boolean containsAll(Collection<?> c);
  
  /**
   * Whether each element ({@code elt}) of {@code s} appears at least {@code s.count(elt)} times
   * in this multiset.
   */
  public boolean isSupersetOf(Multiset<?> s);
  
  /** 
   * Add all the elements of {@code coll} to this multiset.  If the same value appears multiple
   * times in {@code coll}, it will appear multiple times in this multiset.
   * @return  {@code true} iff the multiset was successfully modified
   */
  public boolean addAll(Collection<? extends T> coll);
  
  /**
   * Remove all the elements of {@code coll} from this multiset.  If the same value appears
   * multiple times in {@code coll}, the same number of instances will be removed from
   * this multiset.
   * @return  {@code true} iff the multiset was modified
   */
  public boolean removeAll(Collection<?> coll);

  /**
   * Remove all the elements of this multiset except those contained in {@code coll}.  If
   * the same value appears multiple times in {@code coll}, at most that number of instances
   * will not be removed from this multiset.
   * @return  {@code true} iff the multiset was modified
   */
  public boolean retainAll(Collection<?> coll);
  
  /** Remove all elements from the multiset */
  public void clear();
  
  /**
   * Compares two multisets.  Two multisets are equal if they contain all the same values
   * and the same number of instances of those values.
   * @return  {@code true} iff {@code obj} is a multiset with the same elements as {@code this}
   */
  public boolean equals(Object obj);
  
  /**
   * A hash code for the multiset, computed by summing the hash codes of each element in the iterator.
   * (An empty multiset has hash code {@code 0}; each {@code null} element has hash code 1.)
   */
  public int hashCode();
  
}
