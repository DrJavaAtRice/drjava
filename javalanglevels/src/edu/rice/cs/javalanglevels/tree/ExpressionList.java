package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ExpressionList, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class ExpressionList extends JExpression {
  private final Expression[] _expressions;

  /**
   * Constructs a ExpressionList.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ExpressionList(SourceInfo in_sourceInfo, Expression[] in_expressions) {
    super(in_sourceInfo);

    if (in_expressions == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'expressions' to the ExpressionList constructor was null. This class may not have null field values.");
    }
    _expressions = in_expressions;
  }

  public Expression[] getExpressions() { return _expressions; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
