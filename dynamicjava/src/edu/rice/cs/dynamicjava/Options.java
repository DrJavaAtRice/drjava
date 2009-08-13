package edu.rice.cs.dynamicjava;

import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;

public class Options {
  
  public static final Options DEFAULT = new Options();
  
  /** For default options, use {@link #DEFAULT}; for custom options, create a subclass. */
  protected Options() {}
  
  public TypeSystem typeSystem() { return ExtendedTypeSystem.INSTANCE; }
  /** Require a semicolon at the end of statements. */
  public boolean requireSemicolon() { return false; }
  /** Require variable declarations to include an explicit type. */
  public boolean requireVariableType() { return false; }
  /** Check that all access of class members is permitted by accessibility controls. */
  public boolean enforceAllAccess() { return false; }
  /** Check that access of private class members is permitted (irrelevant if enforceAllAccess() is true). */
  public boolean enforcePrivateAccess() { return false; }
}
