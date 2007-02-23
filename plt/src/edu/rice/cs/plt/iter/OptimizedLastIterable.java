package edu.rice.cs.plt.iter;

/**
 * An iterable that supports a {@code last} operation that executes more quickly than traversing the entire
 * contents of the list.
 */
public interface OptimizedLastIterable<T> extends Iterable<T> {
  /** Get the last element of the list.  Assumed to execute more quickly than a traversal over all elements. */
  public T last();
}
