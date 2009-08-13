package edu.rice.cs.dynamicjava.symbol;

/** An access specifier. */
public enum Access {
  PUBLIC, PROTECTED, PACKAGE, PRIVATE;
  
  /** A symbol that is given an accessibility level. */
  public static interface Limited {
    /** The symbol's access level. */
    public Access accessibility();
    /** Get the module enclosing this symbol's declaration. */
    public Module accessModule();
    /** The name used to access this symbol. */
    public String declaredName();
  }
  
  /** An enclosing context (typically a top-level class) used as a basis for accessibility checks. */
  public static interface Module {
    public String packageName();
  }
}
