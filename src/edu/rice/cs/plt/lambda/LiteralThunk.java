package edu.rice.cs.plt.lambda;

/** A thunk without side-effects whose value is already determined at creation time */
public class LiteralThunk<T> implements Thunk<T> {
  
  private final T _val;
  
  public LiteralThunk(T val) { _val = val; }
  public T value() { return _val; }
  
  public static <T> LiteralThunk<T> make(T value) { return new LiteralThunk<T>(value); }
  
}
