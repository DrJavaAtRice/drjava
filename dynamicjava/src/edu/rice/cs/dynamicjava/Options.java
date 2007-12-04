package edu.rice.cs.dynamicjava;

import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;

public class Options {
  
  public static final Options DEFAULT = new Options();
  
  public Options() {}
  
  public boolean semicolonIsOptional() { return true; }
  public TypeSystem typeSystem() { return ExtendedTypeSystem.INSTANCE; }
  
}
