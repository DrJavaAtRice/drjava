package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda4;

/**
 * An Iterator that applies a provided mapping lambda to each corresponding member of four 
 * source iterators.  {@link #remove()} delegates to each of the source iterators.  Assumes
 * the iterators have the same length.
 */
public class QuaternaryMappedIterator<T1, T2, T3, T4, R> implements Iterator<R> {

  private final Iterator<? extends T1> _source1;
  private final Iterator<? extends T2> _source2;
  private final Iterator<? extends T3> _source3;
  private final Iterator<? extends T4> _source4;
  private final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> _map;
  
  public QuaternaryMappedIterator(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
                                  Iterator<? extends T3> source3, Iterator<? extends T4> source4,
                                  Lambda4<? super T1, ? super T2, 
                                          ? super T3, ? super T4, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _source4 = source4;
    _map = map;
  }
  
  public boolean hasNext() { return _source1.hasNext(); }
  
  public R next() { 
    return _map.value(_source1.next(), _source2.next(), _source3.next(), _source4.next());
  }
  
  public void remove() { 
    _source1.remove(); 
    _source2.remove(); 
    _source3.remove(); 
    _source4.remove(); 
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T1, T2, T3, T4, R> QuaternaryMappedIterator<T1, T2, T3, T4, R> 
    make(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
         Iterator<? extends T3> source3, Iterator<? extends T4> source4,
         Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    return new QuaternaryMappedIterator<T1, T2, T3, T4, R>(source1, source2, source3, source4, map);
  }
  
}
