package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator over a 0-length list.
 */
public class EmptyIterator<T> implements Iterator<T> {
  public boolean hasNext() { return false; }
  public T next() { throw new NoSuchElementException(); }
  public void remove() { throw new IllegalStateException(); }
  
  private static final EmptyIterator<?> INSTANCE = new EmptyIterator<Object>();
  
  /** @return  A singleton, cast (unsafe formally, but safe in practice) to the appropriate type */
  @SuppressWarnings("unchecked")
  public static <T> EmptyIterator<T> make() { return (EmptyIterator<T>) INSTANCE; }
}
