package edu.rice.cs.plt.tuple;

/**
 * An empty tuple.  The choice of {@code Void} as the type argument for the {@code Option}
 * supertype is arbitrary ({@code Option<null>} would make more sense, but is not expressible).
 * Clients needing a specific kind of {@code Option} can perform an unsafe cast on the singleton
 * to produce the desired type (this is done in {@link Option#none}).
 */
public class Null extends Option<Void> {
  
  /** Forces access through the singleton */
  private Null() {}
  
  /** A singleton null tuple */
  public static final Null INSTANCE = new Null();
  
  /** Invokes {@code visitor.forNone()} */
  public <Ret> Ret apply(OptionVisitor<? super Void, ? extends Ret> visitor) {
    return visitor.forNone();
  }
  
  /** Produces {@code "()"} */
  public String toString() { return "()"; }
  
  /** Defined in terms of identity (since the singleton is the only accessible instance) */
  public boolean equals(Object o) { return this == o; }
  
  /** Defined in terms of identity (since the singleton is the only accessible instance) */
  protected int generateHashCode() { return System.identityHashCode(this); }
  
}  
