package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class VariableDeclarator, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class VariableDeclarator extends JExpression {
  private final Type _type;
  private final Word _name;

  /**
   * Constructs a VariableDeclarator.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public VariableDeclarator(SourceInfo in_sourceInfo, Type in_type, Word in_name) {
    super(in_sourceInfo);

    if (in_type == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'type' to the VariableDeclarator constructor was null. This class may not have null field values.");
    }
    _type = in_type;

    if (in_name == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'name' to the VariableDeclarator constructor was null. This class may not have null field values.");
    }
    _name = in_name;
  }

  public Type getType() { return _type; }
  public Word getName() { return _name; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
