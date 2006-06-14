package edu.rice.cs.plt.collect;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;

/**
 * A set-like collection that allows multiple "instances" of a value to be represented
 * in the collection.
 */
public interface Multiset<T> extends Collection<T> {
  
  /** @return  The size of the multiset */
  public int size();

  /** @return  {@code true} iff the multiset is empty */
  public boolean isEmpty();
  
  /** @return  {@code true} iff the multiset contains at least one instance of {@code obj} */
  public boolean contains(Object obj);
  
  /**
   * @return  The number of times {@code value} appears in the multiset (if it does not appear,
   *          the result is {@code 0})
   */
  public int count(Object value);
  
  /**
   * @return  A set view of the multiset (with only one entry for each unique instance).  The 
   *          set will change as subsequent modifications are made to the multiset; 
   *          implementations may choose to allow external modifications to the set as well.
   */
  public Set<T> asSet();
  
  /**
   * @return  An iterator for the multiset.  If the set contains {@code n} instances of a 
   *          value, that value will appear {@code n} times during iteration.  Invoking 
   *          {@code remove} on the iterator removes one instance.
   */
  public Iterator<T> iterator();
  
  /** @return  An array view of the multiset */
  public Object[] toArray();
  
  /** @return  An array view of the multiset */
  public <T> T[] toArray(T[] fill);
  
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
   * 
   * @return  {@code true} iff the multiset was modified
   */
  public boolean remove(Object obj, int instances);
  
  /**
   * Remove all instances of {@code obj} from the multiset
   * 
   * @return  {@code true} iff the multiset was modified
   */
  public boolean removeAllInstances(Object obj);

  /** @return  {@code true} iff each element of the collection is contained by this multiset */
  public boolean containsAll(Collection<?> c);
  
  /** 
   * Add all the elements of {@code coll} to this multiset.  If the same value appears multiple
   * times in {@code coll}, it will appear multiple times in this multiset.
   * 
   * @return  {@code true} iff the multiset was successfully modified
   */
  public boolean addAll(Collection<? extends T> coll);
  
  /**
   * Remove all the elements of {@code coll} from this multiset.  If the same value appears
   * multiple times in {@code coll}, the same number of instances will be removed from
   * this multiset.
   * 
   * @return  {@code true} iff the multiset was modified
   */
  public boolean removeAll(Collection<?> coll);

  /**
   * Remove all the elements of this multiset except those contained in {@code coll}.  If
   * the same value appears multiple times in {@code coll}, at most that number of instances
   * will not be removed from this multiset.
   * 
   * @return  {@code true} iff the multiset was modified
   */
  public boolean retainAll(Collection<?> coll);
  
  /** Remove all elements from the multiset */
  public void clear();
  
  /**
   * Compares two multisets.  Two multisets are equal if they contain all the same values
   * (and the same number of instances of those values).
   * 
   * @return  {@code true} iff {@code obj} is a multiset with the same elements as {@code this}
   */
  public boolean equals(Object obj);
  
  /**
   * @return  A hash code for the multiset, computed by xoring the hash code of each unique 
   *          element xored with the count of that element (an empty multiset has hash code 
   *          {@code 0}; a {@code null} element also has hash code 0)
   */
  public int hashCode();
  
}
