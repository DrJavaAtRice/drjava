package edu.rice.cs.plt.tuple;

/**
 * A visitor for {@link Option}s.  Implementations must handle the "some" and "none"
 * cases, corresponding to the presence and absence of a value.
 */
public interface OptionVisitor<T, Ret> {
  public Ret forSome(T value);
  public Ret forNone();
}
