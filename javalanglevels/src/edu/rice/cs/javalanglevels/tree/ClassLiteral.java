package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ClassLiteral, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ClassLiteral extends Primary {
  private final ReturnTypeI _type;

  /**
   * Constructs a ClassLiteral.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ClassLiteral(SourceInfo in_sourceInfo, ReturnTypeI in_type) {
    super(in_sourceInfo);

    if (in_type == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'type' to the ClassLiteral constructor was null. This class may not have null field values.");
    }
    _type = in_type;
  }

  final public ReturnTypeI getType() { return _type; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forClassLiteral(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forClassLiteral(this); }

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
    writer.print("ClassLiteral" + ":");
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
    writer.print("type = ");
    ReturnTypeI temp_type = getType();
    if (temp_type == null) {
      writer.print("null");
    } else {
      temp_type.outputHelp(writer);
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
      ClassLiteral casted = (ClassLiteral) obj;
      if (! (getType().equals(casted.getType()))) return false;
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
    code ^= getType().hashCode();
    return code;
  }
}
