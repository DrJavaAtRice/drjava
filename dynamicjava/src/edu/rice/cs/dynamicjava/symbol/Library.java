package edu.rice.cs.dynamicjava.symbol;

/**
 * A collection of class and interface declarations.  These declarations may contain mutually recursive
 * references to each other.
 */
public interface Library {
  
  /**
   * Get any top-level classes with the given fully-qualified name.  Typically, there should 
   * be exactly one result.  If the class is not defined, returns an empty list.  If multiple
   * classes have the given name, returns all of them.
   */
  public Iterable<DJClass> declaredClasses(String fullName);

}
