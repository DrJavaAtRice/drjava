package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class CatchBlock, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class CatchBlock extends JExpression {
  private final FormalParameter _exception;
  private final Block _block;

  /**
   * Constructs a CatchBlock.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public CatchBlock(SourceInfo in_sourceInfo, FormalParameter in_exception, Block in_block) {
    super(in_sourceInfo);

    if (in_exception == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'exception' to the CatchBlock constructor was null. This class may not have null field values.");
    }
    _exception = in_exception;

    if (in_block == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'block' to the CatchBlock constructor was null. This class may not have null field values.");
    }
    _block = in_block;
  }

  final public FormalParameter getException() { return _exception; }
  final public Block getBlock() { return _block; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forCatchBlock(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forCatchBlock(this); }

  /**
   * Implementation of toString that uses
   * {@link #output} to generated nicely tabbed tree.
   */
  public java.lang.String toString() {
    java.io.StringWriter w = new java.io.StringWriter();
    output(w);
    return w.toString();
  }

  /**
   * Prints this object out as a nicely tabbed tree.
   */
  public void output(java.io.Writer writer) {
    outputHelp(new TabPrintWriter(writer, 2));
  }

  public void outputHelp(TabPrintWriter writer) {
    writer.print("CatchBlock" + ":");
    writer.indent();

    writer.startLine("");
    writer.print("sourceInfo = ");
    SourceInfo temp_sourceInfo = getSourceInfo();
    if (temp_sourceInfo == null) {
      writer.print("null");
    } else {
      writer.print(temp_sourceInfo);
    }

    writer.startLine("");
    writer.print("exception = ");
    FormalParameter temp_exception = getException();
    if (temp_exception == null) {
      writer.print("null");
    } else {
      temp_exception.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("block = ");
    Block temp_block = getBlock();
    if (temp_block == null) {
      writer.print("null");
    } else {
      temp_block.outputHelp(writer);
    }
    writer.unindent();
  }

  /**
   * Implementation of equals that is based on the values
   * of the fields of the object. Thus, two objects 
   * created with identical parameters will be equal.
   */
  public boolean equals(java.lang.Object obj) {
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass()) || (obj.hashCode() != this.hashCode())) {
      return false;
    } else {
      CatchBlock casted = (CatchBlock) obj;
      if (! (getException().equals(casted.getException()))) return false;
      if (! (getBlock().equals(casted.getBlock()))) return false;
      return true;
    }
  }

  /**
   * Implementation of hashCode that is consistent with
   * equals. The value of the hashCode is formed by
   * XORing the hashcode of the class object with
   * the hashcodes of all the fields of the object.
   */
  protected int generateHashCode() {
    int code = getClass().hashCode();
    code ^= 0;
    code ^= getException().hashCode();
    code ^= getBlock().hashCode();
    return code;
  }
}
