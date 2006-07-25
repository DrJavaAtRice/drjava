package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.recur.RecurUtil;

/**
 * <p>An iterable representing an infinite sequence.  The sequence is defined by an initial
 * value and a successor function (described by a {@link Lambda}).</p>
 * 
 * <p>Note that the infinite nature of this list makes it impossible to implement 
 * {@link SizedIterable} or to use the standard {@code toString}, {@code equals}, and 
 * {@code hashCode} implementations (in {@link AbstractIterable}).  Care must
 * also be taken in invoking many iterable-handling methods that assume finite length, 
 * such as those in {@code IterUtil}.</p>
 */
public class SequenceIterable<T> implements Iterable<T> {
  
  private final T _initial;
  private final Lambda<? super T, ? extends T> _successor;
  
  /**
   * @param initial  The first value in the sequence
   * @param successor  A function that, given the nth sequence value, produces the n+1st value
   */
  public SequenceIterable(T initial, Lambda<? super T, ? extends T> successor) {
    _initial = initial;
    _successor = successor;
  }
  
  /** Create a new {@link SequenceIterator} based on this iterable's parameters */
  public SequenceIterator<T> iterator() { return new SequenceIterator<T>(_initial, _successor); }

  /** @return  A string listing the first few elements of the sequence */
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("[");
    for (T obj : new FiniteSequenceIterable<T>(_initial, _successor, 5)) {
      result.append(RecurUtil.safeToString(obj));
      result.append(", ");
    }
    result.append("...]");
    return result.toString();
  }
  
  /**
   * @return {@code true} iff {@code o} is a SequenceIterable with the same initial value
   *         and successor function (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (!getClass().equals(o.getClass())) { return false; }
    else {
      SequenceIterable<?> cast = (SequenceIterable<?>) o;
      return _initial.equals(cast._initial) && _successor.equals(cast._successor);
    }
  }
  
  public int hashCode() {
    return getClass().hashCode() ^ (_initial.hashCode() << 1) ^ (_successor.hashCode() << 2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SequenceIterable<T> make(T initial, Lambda<? super T, ? extends T> successor) {
    return new SequenceIterable<T>(initial, successor);
  }

}
