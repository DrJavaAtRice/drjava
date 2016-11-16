package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class FunctionInvocation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class FunctionInvocation extends Primary {
  private final ParenthesizedExpressionList _arguments;

  /**
   * Constructs a FunctionInvocation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public FunctionInvocation(SourceInfo in_sourceInfo, ParenthesizedExpressionList in_arguments) {
    super(in_sourceInfo);

    if (in_arguments == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'arguments' to the FunctionInvocation constructor was null. This class may not have null field values.");
    }
    _arguments = in_arguments;
  }

  public ParenthesizedExpressionList getArguments() { return _arguments; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
