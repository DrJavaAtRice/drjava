package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class SwitchCase, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class SwitchCase extends JExpression {
  private final UnbracedBody _code;

  /**
   * Constructs a SwitchCase.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public SwitchCase(SourceInfo in_sourceInfo, UnbracedBody in_code) {
    super(in_sourceInfo);

    if (in_code == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'code' to the SwitchCase constructor was null. This class may not have null field values.");
    }
    _code = in_code;
  }

  public UnbracedBody getCode() { return _code; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
