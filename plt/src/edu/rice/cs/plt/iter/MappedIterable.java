package edu.rice.cs.plt.iter;

import edu.rice.cs.plt.lambda.Lambda;

/**
 * An Iterable containing all the values in the provided Iterable after applying some 
 * specified transformation.
 * 
 * @param S  The element type of the original list
 * @param T  The element type of the transformed list
 */
public class MappedIterable<S, T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private final Iterable<? extends S> _source;
  private final Lambda<? super S, ? extends T> _map;
  
  public MappedIterable(Iterable<? extends S> source, Lambda<? super S, ? extends T> map) {
    _source = source;
    _map = map;
  }
  
  public MappedIterator<S, T> iterator() { 
    return new MappedIterator<S, T>(_source.iterator(), _map);
  }
  
  public int size() { return IterUtil.sizeOf(_source); }
  public boolean isFixed() { return IterUtil.isFixed(_source); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <S, T> MappedIterable<S, T> make(Iterable<? extends S> source, 
                                                 Lambda<? super S, ? extends T> map) {
    return new MappedIterable<S, T>(source, map);
  }
  
}
