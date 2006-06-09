package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Predicate;

/**
 * An Iterable containing all the values in the provided Iterable for which the provided
 * Predicate holds.  Because the size cannot be determined without traversing the list,
 * does not implement {@code SizedIterable}.
 */
public class FilteredIterable<T> extends AbstractIterable<T> implements Iterable<T> {
  
  private Iterable<? extends T> _iterable;
  private Predicate<? super T> _predicate;
  
  public FilteredIterable(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
    _iterable = iterable;
    _predicate = predicate;
  }
  
  public FilteredIterator<T> iterator() { 
    return new FilteredIterator<T>(_iterable.iterator(), _predicate);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> FilteredIterable<T> make(Iterable<? extends T> iterable, 
                                             Predicate<? super T> predicate) {
    return new FilteredIterable<T>(iterable, predicate);
  }
  
}
