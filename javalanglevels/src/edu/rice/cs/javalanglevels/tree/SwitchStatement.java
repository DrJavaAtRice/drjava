package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class SwitchStatement, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class SwitchStatement extends Statement {
  private final Expression _test;
  private final SwitchCase[] _cases;

  /**
   * Constructs a SwitchStatement.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public SwitchStatement(SourceInfo in_sourceInfo, Expression in_test, SwitchCase[] in_cases) {
    super(in_sourceInfo);

    if (in_test == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'test' to the SwitchStatement constructor was null. This class may not have null field values.");
    }
    _test = in_test;

    if (in_cases == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'cases' to the SwitchStatement constructor was null. This class may not have null field values.");
    }
    _cases = in_cases;
  }

  final public Expression getTest() { return _test; }
  final public SwitchCase[] getCases() { return _cases; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forSwitchStatement(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forSwitchStatement(this); }

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
    writer.print("SwitchStatement" + ":");
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
    writer.print("test = ");
    Expression temp_test = getTest();
    if (temp_test == null) {
      writer.print("null");
    } else {
      temp_test.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("cases = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getCases().length; i++) {
      SwitchCase temp_cases = getCases()[i];
      writer.startLine("#" + i + ": ");
      if (temp_cases == null) {
        writer.print("null");
      } else {
        temp_cases.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getCases().length > 0) {
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
      SwitchStatement casted = (SwitchStatement) obj;
      if (! (getTest().equals(casted.getTest()))) return false;
      if (this.getCases().length != casted.getCases().length) return false;
      for (int i = 0; i < getCases().length; i++) if (! getCases()[i].equals(casted.getCases()[i])) return false;
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
    code ^= getTest().hashCode();
    for (int i = 0; i < getCases().length; i++) code ^= getCases()[i].hashCode();
    return code;
  }
}
