package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * An Iterator that applies a provided mapping lambda to each member of a source iterator.
 * {@link #remove()} simply delegates to the source iterator.
 */
public class MappedIterator<S, T> implements Iterator<T> {

  private final Iterator<? extends S> _source;
  private final Lambda<? super S, ? extends T> _map;
  
  public MappedIterator(Iterator<? extends S> source, Lambda<? super S, ? extends T> map) {
    _source = source;
    _map = map;
  }
  
  public boolean hasNext() { return _source.hasNext(); }
  public T next() { return _map.value(_source.next()); }
  public void remove() { _source.remove(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <S, T> MappedIterator<S, T> make(Iterator<? extends S> source, 
                                                 Lambda<? super S, ? extends T> map) {
    return new MappedIterator<S, T>(source, map);
  }
  
}
