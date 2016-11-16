package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ConditionalExpression, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ConditionalExpression extends Expression {
  private final Expression _condition;
  private final Expression _forTrue;
  private final Expression _forFalse;

  /**
   * Constructs a ConditionalExpression.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ConditionalExpression(SourceInfo in_sourceInfo, Expression in_condition, Expression in_forTrue, Expression in_forFalse) {
    super(in_sourceInfo);

    if (in_condition == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'condition' to the ConditionalExpression constructor was null. This class may not have null field values.");
    }
    _condition = in_condition;

    if (in_forTrue == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'forTrue' to the ConditionalExpression constructor was null. This class may not have null field values.");
    }
    _forTrue = in_forTrue;

    if (in_forFalse == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'forFalse' to the ConditionalExpression constructor was null. This class may not have null field values.");
    }
    _forFalse = in_forFalse;
  }

  final public Expression getCondition() { return _condition; }
  final public Expression getForTrue() { return _forTrue; }
  final public Expression getForFalse() { return _forFalse; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forConditionalExpression(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forConditionalExpression(this); }

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
    writer.print("ConditionalExpression" + ":");
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
    writer.print("forTrue = ");
    Expression temp_forTrue = getForTrue();
    if (temp_forTrue == null) {
      writer.print("null");
    } else {
      temp_forTrue.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("forFalse = ");
    Expression temp_forFalse = getForFalse();
    if (temp_forFalse == null) {
      writer.print("null");
    } else {
      temp_forFalse.outputHelp(writer);
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
      ConditionalExpression casted = (ConditionalExpression) obj;
      if (! (getCondition().equals(casted.getCondition()))) return false;
      if (! (getForTrue().equals(casted.getForTrue()))) return false;
      if (! (getForFalse().equals(casted.getForFalse()))) return false;
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
    code ^= getForTrue().hashCode();
    code ^= getForFalse().hashCode();
    return code;
  }
}
