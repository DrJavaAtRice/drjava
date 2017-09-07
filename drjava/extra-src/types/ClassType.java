package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class ClassType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public abstract class ClassType extends ReferenceType {
  private final DJClass _ofClass;

  /**
   * Constructs a ClassType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ClassType(DJClass in_ofClass) {
    super();
    if (in_ofClass == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'ofClass' to the ClassType constructor was null");
    }
    _ofClass = in_ofClass;
  }

  public DJClass ofClass() { return _ofClass; }

  public abstract int generateHashCode();
}
