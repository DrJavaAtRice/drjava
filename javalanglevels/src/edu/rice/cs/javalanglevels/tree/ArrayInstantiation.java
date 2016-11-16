package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ArrayInstantiation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class ArrayInstantiation extends Instantiation {
  private final Type _type;

  /**
   * Constructs a ArrayInstantiation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ArrayInstantiation(SourceInfo in_sourceInfo, Type in_type) {
    super(in_sourceInfo);

    if (in_type == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'type' to the ArrayInstantiation constructor was null. This class may not have null field values.");
    }
    _type = in_type;
  }

  public Type getType() { return _type; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
