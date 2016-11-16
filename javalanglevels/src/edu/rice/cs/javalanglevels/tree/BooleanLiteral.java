package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class BooleanLiteral, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class BooleanLiteral extends LexicalLiteral {
  private final boolean _value;

  /**
   * Constructs a BooleanLiteral.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public BooleanLiteral(SourceInfo in_sourceInfo, boolean in_value) {
    super(in_sourceInfo);
    _value = in_value;
  }

  final public boolean isValue() { return _value; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forBooleanLiteral(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forBooleanLiteral(this); }

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
    writer.print("BooleanLiteral" + ":");
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
    writer.print("value = ");
    boolean temp_value = isValue();
    writer.print(temp_value);
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
      BooleanLiteral casted = (BooleanLiteral) obj;
      if (! (isValue() == casted.isValue())) return false;
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
    code ^= (isValue() ? 1231 : 1237);
    return code;
  }
}
