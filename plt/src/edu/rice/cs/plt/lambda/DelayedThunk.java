package edu.rice.cs.plt.lambda;

/**
 * A thunk whose value is set <em>once</em> after creation, but before the first 
 * invocation of {@link #value}.
 */
public class DelayedThunk<T> implements Thunk<T> {
  
  private T _val;
  private boolean _initialized;
  
  public DelayedThunk() {
    _initialized = false;
    // the value of _val doesn't matter
  }
  
  public T value() {
    if (!_initialized) { throw new IllegalStateException("DelayedThunk is not initialized"); }
    return _val;
  }
  
  public void set(T val) {
    if (_initialized) { throw new IllegalStateException("DelayedThunk is already initialized"); }
    _val = val;
    _initialized = true;
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> DelayedThunk<T> make() { return new DelayedThunk<T>(); }
  
}
