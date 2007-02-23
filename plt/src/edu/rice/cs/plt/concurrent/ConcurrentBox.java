package edu.rice.cs.plt.concurrent;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Box;

/** 
 * <p>A thread-safe box implementation.</p>
 * 
 * <p>As a wrapper for arbitrary objects, instances of this class will serialize without error
 * only if the wrapped object is serializable.</p>
 */
public class ConcurrentBox<T> implements Box<T>, Serializable {
  
  private volatile T _val;
  
  /** Create a box initialized with {@code val} */
  public ConcurrentBox(T val) { _val = val; }
  
  /** Create a box initialized with {@code null} */
  public ConcurrentBox() { _val = null; }
  
  public void set(T val) { _val = val; }
  public T value() { return _val; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ConcurrentBox<T> make(T val) { return new ConcurrentBox<T>(val); }

  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ConcurrentBox<T> make() { return new ConcurrentBox<T>(); }
  
}
