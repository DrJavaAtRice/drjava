package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class TryCatchStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class TryCatchStatement extends Statement {
  private final Block _tryBlock;
  private final CatchBlock[] _catchBlocks;

  /**
   * Constructs a TryCatchStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public TryCatchStatement(SourceInfo in_sourceInfo, Block in_tryBlock, CatchBlock[] in_catchBlocks) {
    super(in_sourceInfo);

    if (in_tryBlock == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'tryBlock' to the TryCatchStatement constructor was null. This class may not have null field values.");
    }
    _tryBlock = in_tryBlock;

    if (in_catchBlocks == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'catchBlocks' to the TryCatchStatement constructor was null. This class may not have null field values.");
    }
    _catchBlocks = in_catchBlocks;
  }

  public Block getTryBlock() { return _tryBlock; }
  public CatchBlock[] getCatchBlocks() { return _catchBlocks; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
