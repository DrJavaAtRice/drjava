package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ClassInstantiation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class ClassInstantiation extends Instantiation {
  private final Type _type;
  private final ParenthesizedExpressionList _arguments;

  /**
   * Constructs a ClassInstantiation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ClassInstantiation(SourceInfo in_sourceInfo, Type in_type, ParenthesizedExpressionList in_arguments) {
    super(in_sourceInfo);

    if (in_type == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'type' to the ClassInstantiation constructor was null. This class may not have null field values.");
    }
    _type = in_type;

    if (in_arguments == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'arguments' to the ClassInstantiation constructor was null. This class may not have null field values.");
    }
    _arguments = in_arguments;
  }

  public Type getType() { return _type; }
  public ParenthesizedExpressionList getArguments() { return _arguments; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
