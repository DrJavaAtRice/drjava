package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda3;

/**
 * An Iterable containing the results of some ternary operation on three input lists 
 * (assumed to always have the same size)
 * 
 * @param T1  The element type of the first input list
 * @param T2  The element type of the second input list
 * @param T3  The element type of the third input list
 * @param R  The element type of the result list
 */
public class TernaryMappedIterable<T1, T2, T3, R> extends AbstractIterable<R>
                                                  implements SizedIterable<R> {
  
  private final Iterable<? extends T1> _source1;
  private final Iterable<? extends T2> _source2;
  private final Iterable<? extends T3> _source3;
  private final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> _map;
  
  public TernaryMappedIterable(Iterable<? extends T1> source1, Iterable<? extends T2> source2,
                               Iterable<? extends T3> source3,
                               Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _map = map;
  }
  
  public TernaryMappedIterator<T1, T2, T3, R> iterator() { 
    return new TernaryMappedIterator<T1, T2, T3, R>(_source1.iterator(), _source2.iterator(), 
                                                    _source3.iterator(), _map);
  }
  
  public int size() { return IterUtil.sizeOf(_source1); }
  public boolean isFixed() { return IterUtil.isFixed(_source1); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, R> TernaryMappedIterable<T1, T2, T3, R> 
    make(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
         Iterable<? extends T3> source3, 
         Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    return new TernaryMappedIterable<T1, T2, T3, R>(source1, source2, source3, map);
  }
  
}
