package edu.rice.cs.plt.lambda;

/**
 * A thunk providing call-by-need evaluation of the nested Thunk, {@code value}.  The first 
 * invocation of {@link #value()} evaluates the thunk; subsequent invocations return the same 
 * value as returned previously.
 */
public class LazyThunk<T> implements Thunk<T> {

  private T _val;
  private Thunk<T> _thunk;
  
  public LazyThunk(Thunk<T> value) {
    _thunk = value;
    // the value of _val doesn't matter
  }
  
  public T value() {
    if (_thunk != null) {
      _val = _thunk.value();
      _thunk = null;
    }
    return _val;
  }
  
  public static <T> LazyThunk<T> make(Thunk<T> value) { return new LazyThunk<T>(value); }
  
}
