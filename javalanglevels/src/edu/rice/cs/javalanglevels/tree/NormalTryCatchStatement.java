package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class NormalTryCatchStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class NormalTryCatchStatement extends TryCatchStatement {

  /**
   * Constructs a NormalTryCatchStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public NormalTryCatchStatement(SourceInfo in_sourceInfo, Block in_tryBlock, CatchBlock[] in_catchBlocks) {
    super(in_sourceInfo, in_tryBlock, in_catchBlocks);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forNormalTryCatchStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forNormalTryCatchStatement(this); }

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
    writer.print("NormalTryCatchStatement" + ":");
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
    writer.print("tryBlock = ");
    Block temp_tryBlock = getTryBlock();
    if (temp_tryBlock == null) {
      writer.print("null");
    } else {
      temp_tryBlock.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("catchBlocks = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getCatchBlocks().length; i++) {
      CatchBlock temp_catchBlocks = getCatchBlocks()[i];
      writer.startLine("#" + i + ": ");
      if (temp_catchBlocks == null) {
        writer.print("null");
      } else {
        temp_catchBlocks.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getCatchBlocks().length > 0) {
      writer.startLine("");
    }
    writer.print("}");
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
      NormalTryCatchStatement casted = (NormalTryCatchStatement) obj;
      if (! (getTryBlock().equals(casted.getTryBlock()))) return false;
      if (this.getCatchBlocks().length != casted.getCatchBlocks().length) return false;
      for (int i = 0; i < getCatchBlocks().length; i++) if (! getCatchBlocks()[i].equals(casted.getCatchBlocks()[i])) return false;
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
    code ^= getTryBlock().hashCode();
    for (int i = 0; i < getCatchBlocks().length; i++) code ^= getCatchBlocks()[i].hashCode();
    return code;
  }
}
