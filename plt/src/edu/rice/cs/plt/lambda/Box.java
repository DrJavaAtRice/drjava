package edu.rice.cs.plt.lambda;

/** 
 * A thunk with a mutable value.  This is useful in situations in which a "pass-by-reference"
 * semantics is required.  For example, where a local class requires access to a mutable (but final) 
 * value, a box could be used.
 */
public interface Box<T> extends Thunk<T> {
  public void set(T val);
}
