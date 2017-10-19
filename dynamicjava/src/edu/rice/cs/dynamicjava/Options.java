package edu.rice.cs.dynamicjava;

import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Thunk;

public class Options {
  
  public static final Options DEFAULT = new Options();
  
  private final Thunk<? extends TypeSystem> _tsFactory;
  
  /** For default options, use {@link #DEFAULT}; for custom options, create a subclass. */
  protected Options() { _tsFactory = typeSystemFactory(); }
  
  /**
   * Provide a factory for the type system.  Allows subclasses to control the caching of the
   * result (for a TypeSystem that should be allocated only once, use a LazyThunk or LambdaUtil.valueLambda).
   * Note that this method is called from the constructor, before subclass constructors are run.
   */
  protected Thunk<? extends TypeSystem> typeSystemFactory() {
    return LambdaUtil.valueLambda(new ExtendedTypeSystem(this));
  }
  
  public final TypeSystem typeSystem() { return _tsFactory.value(); }
  /** Require a semicolon at the end of statements. */
  public boolean requireSemicolon() { return false; }
  /** Require variable declarations to include an explicit type. */
  public boolean requireVariableType() { return false; }
  /** Check that all access of class members is permitted by accessibility controls. */
  public boolean enforceAllAccess() { return false; }
  /** Check that access of private class members is permitted (irrelevant if enforceAllAccess() is true). */
  public boolean enforcePrivateAccess() { return false; }
  /** Disallow boxing conversion. */
  public boolean prohibitBoxing() { return false; }
  /** Disallow unchecked casting conversion. */
  public boolean prohibitUncheckedCasts() { return true; }
}
