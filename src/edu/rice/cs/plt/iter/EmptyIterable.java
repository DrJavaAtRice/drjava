package edu.rice.cs.plt.iter;

/** A 0-length iterable */
public class EmptyIterable<T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private EmptyIterator<T> _iterator;
  
  public EmptyIterable() { _iterator = EmptyIterator.make(); }
  public EmptyIterator<T> iterator() { return _iterator; }
  public int size() { return 0; }
  public boolean isFixed() { return true; }
  
  private static final EmptyIterable<?> INSTANCE = new EmptyIterable<Object>();
  
  /** @return  A singleton, cast (unsafe formally, but safe in practice) to the appropriate type */
  @SuppressWarnings("unchecked")
  public static <T> EmptyIterable<T> make() { return (EmptyIterable<T>) INSTANCE; }
}
