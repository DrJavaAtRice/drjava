package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class JExpression, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class JExpression extends Object implements JExpressionIF {
  private final SourceInfo _sourceInfo;
  private int _hashCode;
  private boolean _hasHashCode = false;

  /**
   * Constructs a JExpression.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public JExpression(SourceInfo in_sourceInfo) {
    super();

    if (in_sourceInfo == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'sourceInfo' to the JExpression constructor was null. This class may not have null field values.");
    }
    _sourceInfo = in_sourceInfo;
  }

  public SourceInfo getSourceInfo() { return _sourceInfo; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
  public final int hashCode() {
    if (! _hasHashCode) { _hashCode = generateHashCode(); _hasHashCode = true; }
    return _hashCode;
  }
}
