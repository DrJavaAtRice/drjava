package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class IfThenStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class IfThenStatement extends Statement {
  private final Expression _testExpression;
  private final Statement _thenStatement;

  /**
   * Constructs a IfThenStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public IfThenStatement(SourceInfo in_sourceInfo, Expression in_testExpression, Statement in_thenStatement) {
    super(in_sourceInfo);

    if (in_testExpression == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'testExpression' to the IfThenStatement constructor was null. This class may not have null field values.");
    }
    _testExpression = in_testExpression;

    if (in_thenStatement == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'thenStatement' to the IfThenStatement constructor was null. This class may not have null field values.");
    }
    _thenStatement = in_thenStatement;
  }

  final public Expression getTestExpression() { return _testExpression; }
  final public Statement getThenStatement() { return _thenStatement; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forIfThenStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forIfThenStatement(this); }

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
    writer.print("IfThenStatement" + ":");
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
      IfThenStatement casted = (IfThenStatement) obj;
      if (! (getTestExpression().equals(casted.getTestExpression()))) return false;
      if (! (getThenStatement().equals(casted.getThenStatement()))) return false;
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
    return code;
  }
}
