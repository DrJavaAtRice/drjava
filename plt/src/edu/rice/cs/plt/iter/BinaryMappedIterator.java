package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda2;

/**
 * An Iterator that applies a provided mapping lambda to each corresponding member of two 
 * source iterators.  {@link #remove()} delegates to each of the source iterators.  Assumes
 * the iterators have the same length.
 */
public class BinaryMappedIterator<T1, T2, R> implements Iterator<R> {

  private final Iterator<? extends T1> _source1;
  private final Iterator<? extends T2> _source2;
  private final Lambda2<? super T1, ? super T2, ? extends R> _map;
  
  public BinaryMappedIterator(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
                              Lambda2<? super T1, ? super T2, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _map = map;
  }
  
  public boolean hasNext() { return _source1.hasNext(); }
  public R next() { return _map.value(_source1.next(), _source2.next()); }
  public void remove() { _source1.remove(); _source2.remove(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R> BinaryMappedIterator<T1, T2, R> 
    make(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
         Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new BinaryMappedIterator<T1, T2, R>(source1, source2, map);
  }
  
}
