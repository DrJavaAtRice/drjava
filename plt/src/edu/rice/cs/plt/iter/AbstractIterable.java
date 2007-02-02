package edu.rice.cs.plt.iter;

/**
 * A parent class for iterables that implements {@code toString}, {@code hashCode},
 * and {@code equals}
 */
public abstract class AbstractIterable<T> implements Iterable<T> {
  
  /** Defers to {@link IterUtil#toString} */
  public String toString() { return IterUtil.toString(this); }
  
  /**
   * Defers to {@link IterUtil#isEqual} (unless {@code obj} is not an {@code AbstractIterable}).  It's tempting
   * to check for equality whenever {@code obj} is <em>any</em> iterable, but that would break the contract of
   * {@code equals()}, since it would not necessarily be symmetric.  See {@link Collection#equals} for further
   * discussion.
   */
  public boolean equals(Object obj) {
    if (obj instanceof AbstractIterable<?>) { return IterUtil.isEqual(this, (AbstractIterable<?>) obj); }
    else { return false; }
  }
  
  /** Defers to {@link IterUtil#hashCode} */
  public int hashCode() { return IterUtil.hashCode(this); }
}
