package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class LessThanExpression, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class LessThanExpression extends ComparisonExpression {

  /**
   * Constructs a LessThanExpression.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public LessThanExpression(SourceInfo in_sourceInfo, Expression in_left, Expression in_right) {
    super(in_sourceInfo, in_left, in_right);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forLessThanExpression(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forLessThanExpression(this); }

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
    writer.print("LessThanExpression" + ":");
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
    writer.print("left = ");
    Expression temp_left = getLeft();
    if (temp_left == null) {
      writer.print("null");
    } else {
      temp_left.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("right = ");
    Expression temp_right = getRight();
    if (temp_right == null) {
      writer.print("null");
    } else {
      temp_right.outputHelp(writer);
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
      LessThanExpression casted = (LessThanExpression) obj;
      if (! (getLeft().equals(casted.getLeft()))) return false;
      if (! (getRight().equals(casted.getRight()))) return false;
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
    code ^= getLeft().hashCode();
    code ^= getRight().hashCode();
    return code;
  }
}
