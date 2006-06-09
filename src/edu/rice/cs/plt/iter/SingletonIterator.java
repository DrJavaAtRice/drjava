package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** An Iterator over a 1-length list */
public class SingletonIterator<T> implements Iterator<T> {
  
  private T _element;
  private boolean _hasNext;
  
  public SingletonIterator(T element) { 
    _element = element;
    _hasNext = true;
  }
  
  public boolean hasNext() { return _hasNext; }
  
  public T next() {
    if (_hasNext) { _hasNext = false; return _element; }
    else { throw new NoSuchElementException(); }
  }
  
  public void remove() { throw new UnsupportedOperationException(); }  
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SingletonIterator<T> make(T element) { 
    return new SingletonIterator<T>(element);
  }
}
