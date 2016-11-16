package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ImportStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class ImportStatement extends JExpression {
  private final CompoundWord _cWord;

  /**
   * Constructs a ImportStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ImportStatement(SourceInfo in_sourceInfo, CompoundWord in_cWord) {
    super(in_sourceInfo);

    if (in_cWord == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'cWord' to the ImportStatement constructor was null. This class may not have null field values.");
    }
    _cWord = in_cWord;
  }

  public CompoundWord getCWord() { return _cWord; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
