package edu.rice.cs.plt.recur;

/** A continuation that is resolved at creation-time */
public class ValueContinuation<T> implements Continuation<T> {
  
  private final T _val;
  
  /** Wrap the given value as a continuation */
  public ValueContinuation(T val) { _val = val; }
  
  /** @return  The wrapped value */
  public T value() { return _val; }
  
  /** @return  {@code true} */
  public boolean isResolved() { return true; }
  
  /**
   * Throw an exception
   * @throws IllegalStateException  Because {@code isResolved} is always {@code true}
   */
  public Continuation<? extends T> step() { throw new IllegalStateException(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ValueContinuation<T> make(T val) { return new ValueContinuation<T>(val); }
  
}
