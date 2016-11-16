package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class WhileStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class WhileStatement extends Statement {
  private final Expression _condition;
  private final Statement _code;

  /**
   * Constructs a WhileStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public WhileStatement(SourceInfo in_sourceInfo, Expression in_condition, Statement in_code) {
    super(in_sourceInfo);

    if (in_condition == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'condition' to the WhileStatement constructor was null. This class may not have null field values.");
    }
    _condition = in_condition;

    if (in_code == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'code' to the WhileStatement constructor was null. This class may not have null field values.");
    }
    _code = in_code;
  }

  final public Expression getCondition() { return _condition; }
  final public Statement getCode() { return _code; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forWhileStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forWhileStatement(this); }

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
    writer.print("WhileStatement" + ":");
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
    writer.print("condition = ");
    Expression temp_condition = getCondition();
    if (temp_condition == null) {
      writer.print("null");
    } else {
      temp_condition.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("code = ");
    Statement temp_code = getCode();
    if (temp_code == null) {
      writer.print("null");
    } else {
      temp_code.outputHelp(writer);
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
      WhileStatement casted = (WhileStatement) obj;
      if (! (getCondition().equals(casted.getCondition()))) return false;
      if (! (getCode().equals(casted.getCode()))) return false;
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
    code ^= getCondition().hashCode();
    code ^= getCode().hashCode();
    return code;
  }
}
