package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class LabeledCase, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class LabeledCase extends SwitchCase {
  private final Expression _label;

  /**
   * Constructs a LabeledCase.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public LabeledCase(SourceInfo in_sourceInfo, Expression in_label, UnbracedBody in_code) {
    super(in_sourceInfo, in_code);

    if (in_label == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'label' to the LabeledCase constructor was null. This class may not have null field values.");
    }
    _label = in_label;
  }

  final public Expression getLabel() { return _label; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forLabeledCase(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forLabeledCase(this); }

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
    writer.print("LabeledCase" + ":");
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
    Expression temp_label = getLabel();
    if (temp_label == null) {
      writer.print("null");
    } else {
      temp_label.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("code = ");
    UnbracedBody temp_code = getCode();
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
      LabeledCase casted = (LabeledCase) obj;
      if (! (getLabel().equals(casted.getLabel()))) return false;
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
    code ^= getLabel().hashCode();
    code ^= getCode().hashCode();
    return code;
  }
}
