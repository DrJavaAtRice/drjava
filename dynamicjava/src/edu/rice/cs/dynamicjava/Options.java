package edu.rice.cs.dynamicjava;

import edu.rice.cs.dynamicjava.symbol.TypeSystem;
import edu.rice.cs.dynamicjava.symbol.ExtendedTypeSystem;

public class Options {
  
  public static final Options DEFAULT = new Options(ExtendedTypeSystem.INSTANCE);
  
  private final TypeSystem _ts;
  
  public Options(TypeSystem ts) { _ts = ts; }
  
  public boolean semicolonIsOptional() { return true; }
  public TypeSystem typeSystem() { return _ts; }
  
}
