package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class UninitializedArrayInstantiation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class UninitializedArrayInstantiation extends ArrayInstantiation {
  private final DimensionExpressionList _dimensionSizes;

  /**
   * Constructs a UninitializedArrayInstantiation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public UninitializedArrayInstantiation(SourceInfo in_sourceInfo, Type in_type, DimensionExpressionList in_dimensionSizes) {
    super(in_sourceInfo, in_type);

    if (in_dimensionSizes == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'dimensionSizes' to the UninitializedArrayInstantiation constructor was null. This class may not have null field values.");
    }
    _dimensionSizes = in_dimensionSizes;
  }

  public DimensionExpressionList getDimensionSizes() { return _dimensionSizes; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
