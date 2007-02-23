package edu.rice.cs.plt.lambda;

/**
 * An arbitrary predicate for values of type T.  Implementations should return {@code true}
 * iff some property holds for {@code arg}.
 */
public interface Predicate<T> extends Lambda<T, Boolean> {
  public Boolean value(T arg);
}
