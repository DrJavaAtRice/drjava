package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class Body, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class Body extends JExpression {
  private final BodyItemI[] _statements;

  /**
   * Constructs a Body.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public Body(SourceInfo in_sourceInfo, BodyItemI[] in_statements) {
    super(in_sourceInfo);

    if (in_statements == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'statements' to the Body constructor was null. This class may not have null field values.");
    }
    _statements = in_statements;
  }

  public BodyItemI[] getStatements() { return _statements; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
