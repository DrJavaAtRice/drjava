package edu.rice.cs.plt.collect;

import java.util.Set;
import edu.rice.cs.plt.tuple.Pair;

/**
 * A set of pairs representing a binary relation.  Additional methods provide a convenient
 * (and potentially optimized) interface for accessing sets of firsts (values of type {@code T1})
 * and seconds (values of type {@code T2}) based on a given key.  Thus, a relation can also
 * be viewed as a generalization of a map in which keys map to sets of values, and this mapping
 * occurs in both directions.
 */
public interface Relation<T1, T2> extends Set<Pair<T1, T2>> {

  public boolean contains(T1 first, T2 second);
  public boolean add(T1 first, T2 second);
  public boolean remove(T1 first, T2 second);

  /** The set of firsts.  Does not allow mutation. */
  public Set<T1> firstSet();
  public boolean containsFirst(T1 first);
  /** The set of seconds corresponding to a specific first.  Does not allow mutation. */
  public Set<T2> getSeconds(T1 first);

  /** The set of seconds.  Does not allow mutation. */
  public Set<T2> secondSet();
  public boolean containsSecond(T2 second);
  /** The set of firsts corresponding to a specific second.  Does not allow mutation. */
  public Set<T1> getFirsts(T2 second);
  
}
