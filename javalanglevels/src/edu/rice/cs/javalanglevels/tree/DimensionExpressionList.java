package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class DimensionExpressionList, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class DimensionExpressionList extends ExpressionList {

  /**
   * Constructs a DimensionExpressionList.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public DimensionExpressionList(SourceInfo in_sourceInfo, Expression[] in_expressions) {
    super(in_sourceInfo, in_expressions);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forDimensionExpressionList(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forDimensionExpressionList(this); }

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
    writer.print("DimensionExpressionList" + ":");
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
    writer.print("expressions = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getExpressions().length; i++) {
      Expression temp_expressions = getExpressions()[i];
      writer.startLine("#" + i + ": ");
      if (temp_expressions == null) {
        writer.print("null");
      } else {
        temp_expressions.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getExpressions().length > 0) {
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
      DimensionExpressionList casted = (DimensionExpressionList) obj;
      if (this.getExpressions().length != casted.getExpressions().length) return false;
      for (int i = 0; i < getExpressions().length; i++) if (! getExpressions()[i].equals(casted.getExpressions()[i])) return false;
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
    for (int i = 0; i < getExpressions().length; i++) code ^= getExpressions()[i].hashCode();
    return code;
  }
}
