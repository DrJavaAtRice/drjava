package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda4;

/**
 * An Iterable containing the results of some quaternary operation on four input lists 
 * (assumed to always have the same size)
 * 
 * @param T1  The element type of the first input list
 * @param T2  The element type of the second input list
 * @param T3  The element type of the third input list
 * @param T4  The element type of the fourth input list
 * @param R  The element type of the result list
 */
public class QuaternaryMappedIterable<T1, T2, T3, T4, R>  extends AbstractIterable<R>
                                                          implements SizedIterable<R> {
  
  private final Iterable<? extends T1> _source1;
  private final Iterable<? extends T2> _source2;
  private final Iterable<? extends T3> _source3;
  private final Iterable<? extends T4> _source4;
  private final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> _map;
  
  public QuaternaryMappedIterable(Iterable<? extends T1> source1, Iterable<? extends T2> source2,
                                  Iterable<? extends T3> source3, Iterable<? extends T4> source4,
                                  Lambda4<? super T1, ? super T2, 
                                          ? super T3, ? super T4, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _source4 = source4;
    _map = map;
  }
  
  public QuaternaryMappedIterator<T1, T2, T3, T4, R> iterator() { 
    return new QuaternaryMappedIterator<T1, T2, T3, T4, R>(_source1.iterator(), _source2.iterator(), 
                                                           _source3.iterator(), _source4.iterator(),
                                                           _map);
  }
  
  public int size() { return IterUtil.sizeOf(_source1); }
  public boolean isFixed() { return IterUtil.isFixed(_source1); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, R> QuaternaryMappedIterable<T1, T2, T3, T4, R> 
    make(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
         Iterable<? extends T3> source3, Iterable<? extends T4> source4,
         Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    return new QuaternaryMappedIterable<T1, T2, T3, T4, R>(source1, source2, source3, source4, map);
  }
  
}
