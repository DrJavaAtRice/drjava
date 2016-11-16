package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class AnonymousClassInstantiation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class AnonymousClassInstantiation extends ClassInstantiation {
  private final BracedBody _body;

  /**
   * Constructs a AnonymousClassInstantiation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public AnonymousClassInstantiation(SourceInfo in_sourceInfo, Type in_type, ParenthesizedExpressionList in_arguments, BracedBody in_body) {
    super(in_sourceInfo, in_type, in_arguments);

    if (in_body == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'body' to the AnonymousClassInstantiation constructor was null. This class may not have null field values.");
    }
    _body = in_body;
  }

  public BracedBody getBody() { return _body; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
