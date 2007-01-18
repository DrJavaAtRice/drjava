package edu.rice.cs.plt.lambda;

/**
 * A thunk providing call-by-need evaluation of the nested Thunk, {@code value}.  The first 
 * invocation of {@link #value()} evaluates the thunk; subsequent invocations return the same 
 * value as returned previously.
 */
public class LazyThunk<R> implements Thunk<R> {

  private R _val;
  private Thunk<R> _thunk;
  
  public LazyThunk(Thunk<R> value) {
    _thunk = value;
    // the value of _val doesn't matter
  }
  
  public R value() {
    if (_thunk != null) {
      _val = _thunk.value();
      _thunk = null;
    }
    return _val;
  }
  
  public static <R> LazyThunk<R> make(Thunk<R> value) { return new LazyThunk<R>(value); }
  
}
