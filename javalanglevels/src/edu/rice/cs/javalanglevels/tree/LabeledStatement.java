package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class LabeledStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class LabeledStatement extends Statement {
  private final Word _label;
  private final Statement _statement;

  /**
   * Constructs a LabeledStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public LabeledStatement(SourceInfo in_sourceInfo, Word in_label, Statement in_statement) {
    super(in_sourceInfo);

    if (in_label == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'label' to the LabeledStatement constructor was null. This class may not have null field values.");
    }
    _label = in_label;

    if (in_statement == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'statement' to the LabeledStatement constructor was null. This class may not have null field values.");
    }
    _statement = in_statement;
  }

  final public Word getLabel() { return _label; }
  final public Statement getStatement() { return _statement; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forLabeledStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forLabeledStatement(this); }

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
    writer.print("LabeledStatement" + ":");
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
    writer.print("label = ");
    Word temp_label = getLabel();
    if (temp_label == null) {
      writer.print("null");
    } else {
      temp_label.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("statement = ");
    Statement temp_statement = getStatement();
    if (temp_statement == null) {
      writer.print("null");
    } else {
      temp_statement.outputHelp(writer);
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
      LabeledStatement casted = (LabeledStatement) obj;
      if (! (getLabel().equals(casted.getLabel()))) return false;
      if (! (getStatement().equals(casted.getStatement()))) return false;
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
    code ^= getLabel().hashCode();
    code ^= getStatement().hashCode();
    return code;
  }
}
