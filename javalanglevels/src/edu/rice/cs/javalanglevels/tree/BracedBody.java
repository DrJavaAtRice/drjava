package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class BracedBody, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class BracedBody extends Body {

  /**
   * Constructs a BracedBody.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public BracedBody(SourceInfo in_sourceInfo, BodyItemI[] in_statements) {
    super(in_sourceInfo, in_statements);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forBracedBody(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forBracedBody(this); }

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
    writer.print("BracedBody" + ":");
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
    writer.print("statements = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getStatements().length; i++) {
      BodyItemI temp_statements = getStatements()[i];
      writer.startLine("#" + i + ": ");
      if (temp_statements == null) {
        writer.print("null");
      } else {
        temp_statements.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getStatements().length > 0) {
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
      BracedBody casted = (BracedBody) obj;
      if (this.getStatements().length != casted.getStatements().length) return false;
      for (int i = 0; i < getStatements().length; i++) if (! getStatements()[i].equals(casted.getStatements()[i])) return false;
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
    for (int i = 0; i < getStatements().length; i++) code ^= getStatements()[i].hashCode();
    return code;
  }
}
