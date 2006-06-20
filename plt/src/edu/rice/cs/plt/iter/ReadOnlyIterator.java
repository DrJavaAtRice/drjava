package edu.rice.cs.plt.iter;

import java.util.Iterator;

/**
 * An abstract implementation of {@code Iterator} that implements {@link Iterator#remove} 
 * by throwing an {@link UnsupportedOperationException}.  (This simplifies the declaration
 * of anonymous iterators.)
 */
public abstract class ReadOnlyIterator<T> implements Iterator<T> {
  
  /** @throws  UnsupportedOperationException */
  public void remove() { throw new UnsupportedOperationException(); }
  
}
