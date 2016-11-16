package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class MemberType, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class MemberType extends ReferenceType {
  private final ReferenceType _left;
  private final ReferenceType _right;

  /**
   * Constructs a MemberType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public MemberType(SourceInfo in_sourceInfo, String in_name, ReferenceType in_left, ReferenceType in_right) {
    super(in_sourceInfo, in_name);

    if (in_left == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'left' to the MemberType constructor was null. This class may not have null field values.");
    }
    _left = in_left;

    if (in_right == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'right' to the MemberType constructor was null. This class may not have null field values.");
    }
    _right = in_right;
  }

  final public ReferenceType getLeft() { return _left; }
  final public ReferenceType getRight() { return _right; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forMemberType(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forMemberType(this); }

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
    writer.print("MemberType" + ":");
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
    writer.print("name = ");
    String temp_name = getName();
    if (temp_name == null) {
      writer.print("null");
    } else {
      writer.print(temp_name);
    }

    writer.startLine("");
    writer.print("left = ");
    ReferenceType temp_left = getLeft();
    if (temp_left == null) {
      writer.print("null");
    } else {
      temp_left.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("right = ");
    ReferenceType temp_right = getRight();
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
      MemberType casted = (MemberType) obj;
      if (! (getName() == casted.getName())) return false;
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
    code ^= getName().hashCode();
    code ^= getLeft().hashCode();
    code ^= getRight().hashCode();
    return code;
  }
}
