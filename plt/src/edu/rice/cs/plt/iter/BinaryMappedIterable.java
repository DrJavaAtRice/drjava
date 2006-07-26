package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda2;

/**
 * An Iterable containing the results of some binary operation on two input lists 
 * (assumed to always have the same size)
 * 
 * @param T1  The element type of the first input list
 * @param T2  The element type of the second input list
 * @param R  The element type of the result list
 */
public class BinaryMappedIterable<T1, T2, R> extends AbstractIterable<R> 
                                             implements SizedIterable<R> {
  
  private final Iterable<? extends T1> _source1;
  private final Iterable<? extends T2> _source2;
  private final Lambda2<? super T1, ? super T2, ? extends R> _map;
  
  public BinaryMappedIterable(Iterable<? extends T1> source1, Iterable<? extends T2> source2,
                              Lambda2<? super T1, ? super T2, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _map = map;
  }
  
  public BinaryMappedIterator<T1, T2, R> iterator() { 
    return new BinaryMappedIterator<T1, T2, R>(_source1.iterator(), _source2.iterator(), _map);
  }
  
  public int size() { return IterUtil.sizeOf(_source1); }
  public boolean isFixed() { return IterUtil.isFixed(_source1); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R> BinaryMappedIterable<T1, T2, R> 
    make(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
         Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new BinaryMappedIterable<T1, T2, R>(source1, source2, map);
  }
  
  /**
   * Create a {@code BinaryMappedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the mapping.
   */
  public static <T1, T2, R> SnapshotIterable<R> 
    makeSnapshot(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
                 Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new SnapshotIterable<R>(new BinaryMappedIterable<T1, T2, R>(source1, source2, map));
  }
  
}
