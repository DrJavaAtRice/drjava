package edu.rice.cs.plt.iter;

import java.io.Serializable;

/** A 0-length iterable */
public class EmptyIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  public static final EmptyIterable<Void> INSTANCE = new EmptyIterable<Void>();

  private EmptyIterable() {}
  
  @SuppressWarnings("unchecked")
  public EmptyIterator<T> iterator() { return (EmptyIterator<T>) EmptyIterator.INSTANCE; }
  
  public int size() { return 0; }
  public int size(int bound) { return 0; }
  public boolean isFixed() { return true; }
  
  
  /** @return  A singleton, cast (unsafe formally, but safe in practice) to the appropriate type */
  @SuppressWarnings("unchecked")
  public static <T> EmptyIterable<T> make() { return (EmptyIterable<T>) INSTANCE; }
}
