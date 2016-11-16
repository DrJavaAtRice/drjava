package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ForStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ForStatement extends Statement {
  private final ForInitI _init;
  private final ForConditionI _condition;
  private final UnparenthesizedExpressionList _update;
  private final Statement _code;

  /**
   * Constructs a ForStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ForStatement(SourceInfo in_sourceInfo, ForInitI in_init, ForConditionI in_condition, UnparenthesizedExpressionList in_update, Statement in_code) {
    super(in_sourceInfo);

    if (in_init == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'init' to the ForStatement constructor was null. This class may not have null field values.");
    }
    _init = in_init;

    if (in_condition == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'condition' to the ForStatement constructor was null. This class may not have null field values.");
    }
    _condition = in_condition;

    if (in_update == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'update' to the ForStatement constructor was null. This class may not have null field values.");
    }
    _update = in_update;

    if (in_code == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'code' to the ForStatement constructor was null. This class may not have null field values.");
    }
    _code = in_code;
  }

  final public ForInitI getInit() { return _init; }
  final public ForConditionI getCondition() { return _condition; }
  final public UnparenthesizedExpressionList getUpdate() { return _update; }
  final public Statement getCode() { return _code; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forForStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forForStatement(this); }

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
    writer.print("ForStatement" + ":");
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
    writer.print("init = ");
    ForInitI temp_init = getInit();
    if (temp_init == null) {
      writer.print("null");
    } else {
      temp_init.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("condition = ");
    ForConditionI temp_condition = getCondition();
    if (temp_condition == null) {
      writer.print("null");
    } else {
      temp_condition.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("update = ");
    UnparenthesizedExpressionList temp_update = getUpdate();
    if (temp_update == null) {
      writer.print("null");
    } else {
      temp_update.outputHelp(writer);
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
      ForStatement casted = (ForStatement) obj;
      if (! (getInit().equals(casted.getInit()))) return false;
      if (! (getCondition().equals(casted.getCondition()))) return false;
      if (! (getUpdate().equals(casted.getUpdate()))) return false;
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
    code ^= getInit().hashCode();
    code ^= getCondition().hashCode();
    code ^= getUpdate().hashCode();
    code ^= getCode().hashCode();
    return code;
  }
}
