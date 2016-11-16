package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class BinaryExpression, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class BinaryExpression extends Expression {
  private final Expression _left;
  private final Expression _right;

  /**
   * Constructs a BinaryExpression.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public BinaryExpression(SourceInfo in_sourceInfo, Expression in_left, Expression in_right) {
    super(in_sourceInfo);

    if (in_left == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'left' to the BinaryExpression constructor was null. This class may not have null field values.");
    }
    _left = in_left;

    if (in_right == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'right' to the BinaryExpression constructor was null. This class may not have null field values.");
    }
    _right = in_right;
  }

  public Expression getLeft() { return _left; }
  public Expression getRight() { return _right; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
