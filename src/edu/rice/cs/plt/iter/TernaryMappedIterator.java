package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda3;

/**
 * An Iterator that applies a provided mapping lambda to each corresponding member of three 
 * source iterators.  {@link #remove()} delegates to each of the source iterators.  Assumes
 * the iterators have the same length.
 */
public class TernaryMappedIterator<T1, T2, T3, R> implements Iterator<R> {

  private final Iterator<? extends T1> _source1;
  private final Iterator<? extends T2> _source2;
  private final Iterator<? extends T3> _source3;
  private final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> _map;
  
  public TernaryMappedIterator(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
                               Iterator<? extends T3> source3,
                               Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _map = map;
  }
  
  public boolean hasNext() { return _source1.hasNext(); }
  public R next() { return _map.value(_source1.next(), _source2.next(), _source3.next()); }
  public void remove() { _source1.remove(); _source2.remove(); _source3.remove(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, R> TernaryMappedIterator<T1, T2, T3, R> 
    make(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
         Iterator<? extends T3> source3,
         Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    return new TernaryMappedIterator<T1, T2, T3, R>(source1, source2, source3, map);
  }
  
}
