package edu.rice.cs.plt.lambda;

import java.io.Serializable;

/** 
 * <p>A box that freely supports reads and writes.</p>
 * 
 * <p>As a wrapper for arbitrary objects, instances of this class will serialize without error
 * only if the wrapped object is serializable.</p>
 */
public class SimpleBox<T> implements Box<T>, Serializable {
  
  private T _val;
  
  /** Create a box initialized with {@code val} */
  public SimpleBox(T val) { _val = val; }
  
  /** Create a box initialized with {@code null} */
  public SimpleBox() { _val = null; }
  
  public void set(T val) { _val = val; }
  public T value() { return _val; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Box<T> make(T val) { return new SimpleBox<T>(val); }

  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Box<T> make() { return new SimpleBox<T>(); }
  
}
