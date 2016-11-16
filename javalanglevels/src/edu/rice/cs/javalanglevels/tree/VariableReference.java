package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class VariableReference, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class VariableReference extends Primary {

  /**
   * Constructs a VariableReference.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public VariableReference(SourceInfo in_sourceInfo) {
    super(in_sourceInfo);
  }


  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
