package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class ValidType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public abstract class ValidType extends Type {

  /**
   * Constructs a ValidType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ValidType() {
    super();
  }


  public abstract int generateHashCode();
}
