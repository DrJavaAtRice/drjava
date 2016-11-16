package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class InitializedArrayInstantiation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class InitializedArrayInstantiation extends ArrayInstantiation {
  private final ArrayInitializer _initializer;

  /**
   * Constructs a InitializedArrayInstantiation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public InitializedArrayInstantiation(SourceInfo in_sourceInfo, Type in_type, ArrayInitializer in_initializer) {
    super(in_sourceInfo, in_type);

    if (in_initializer == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'initializer' to the InitializedArrayInstantiation constructor was null. This class may not have null field values.");
    }
    _initializer = in_initializer;
  }

  public ArrayInitializer getInitializer() { return _initializer; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
