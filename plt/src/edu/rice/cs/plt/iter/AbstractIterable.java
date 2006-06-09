package edu.rice.cs.plt.iter;

/**
 * A parent class for iterables that implements {@code toString}, {@code hashCode},
 * and {@code equals}
 */
public abstract class AbstractIterable<T> implements Iterable<T> {
  
  /** Defers to {@link IterUtil#toString} */
  public String toString() { return IterUtil.toString(this); }
  
  /** Defers to {@link IterUtil#isEqual} (unless {@code obj} is not an {@code Iterable}) */
  public boolean equals(Object obj) {
    if (obj instanceof Iterable<?>) { return IterUtil.isEqual(this, (Iterable<?>) obj); }
    else { return false; }
  }
  
  /** Defers to {@link IterUtil#hashCode} */
  public int hashCode() { return IterUtil.hashCode(this); }
}
