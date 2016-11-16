package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class IfThenElseStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class IfThenElseStatement extends IfThenStatement {
  private final Statement _elseStatement;

  /**
   * Constructs a IfThenElseStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public IfThenElseStatement(SourceInfo in_sourceInfo, Expression in_testExpression, Statement in_thenStatement, Statement in_elseStatement) {
    super(in_sourceInfo, in_testExpression, in_thenStatement);

    if (in_elseStatement == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'elseStatement' to the IfThenElseStatement constructor was null. This class may not have null field values.");
    }
    _elseStatement = in_elseStatement;
  }

  final public Statement getElseStatement() { return _elseStatement; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forIfThenElseStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forIfThenElseStatement(this); }

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
    writer.print("IfThenElseStatement" + ":");
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
    writer.print("testExpression = ");
    Expression temp_testExpression = getTestExpression();
    if (temp_testExpression == null) {
      writer.print("null");
    } else {
      temp_testExpression.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("thenStatement = ");
    Statement temp_thenStatement = getThenStatement();
    if (temp_thenStatement == null) {
      writer.print("null");
    } else {
      temp_thenStatement.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("elseStatement = ");
    Statement temp_elseStatement = getElseStatement();
    if (temp_elseStatement == null) {
      writer.print("null");
    } else {
      temp_elseStatement.outputHelp(writer);
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
      IfThenElseStatement casted = (IfThenElseStatement) obj;
      if (! (getTestExpression().equals(casted.getTestExpression()))) return false;
      if (! (getThenStatement().equals(casted.getThenStatement()))) return false;
      if (! (getElseStatement().equals(casted.getElseStatement()))) return false;
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
    code ^= getTestExpression().hashCode();
    code ^= getThenStatement().hashCode();
    code ^= getElseStatement().hashCode();
    return code;
  }
}
