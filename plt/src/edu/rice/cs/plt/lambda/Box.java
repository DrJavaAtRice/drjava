package edu.rice.cs.plt.lambda;

/** 
 * A thunk with a mutable value; also useful where a local class requires access
 * to a mutable (but final) value
 */
public class Box<T> implements Thunk<T> {
  
  private T _val;
  
  /** Create a box initialized with {@code val} */
  public Box(T val) { _val = val; }
  
  /** Create a box initialized with {@code null} */
  public Box() { _val = null; }
  
  public void set(T val) { _val = val; }
  public T value() { return _val; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Box<T> make(T val) { return new Box<T>(val); }

  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Box<T> make() { return new Box<T>(); }
  
}
