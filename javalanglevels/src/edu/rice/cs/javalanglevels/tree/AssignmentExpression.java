package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class AssignmentExpression, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class AssignmentExpression extends Expression {
  private final Expression _name;
  private final Expression _value;

  /**
   * Constructs a AssignmentExpression.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public AssignmentExpression(SourceInfo in_sourceInfo, Expression in_name, Expression in_value) {
    super(in_sourceInfo);

    if (in_name == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'name' to the AssignmentExpression constructor was null. This class may not have null field values.");
    }
    _name = in_name;

    if (in_value == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'value' to the AssignmentExpression constructor was null. This class may not have null field values.");
    }
    _value = in_value;
  }

  public Expression getName() { return _name; }
  public Expression getValue() { return _value; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
