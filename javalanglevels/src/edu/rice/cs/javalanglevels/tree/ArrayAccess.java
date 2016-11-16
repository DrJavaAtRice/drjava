package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ArrayAccess, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ArrayAccess extends Primary {
  private final Expression _array;
  private final Expression _index;

  /**
   * Constructs a ArrayAccess.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ArrayAccess(SourceInfo in_sourceInfo, Expression in_array, Expression in_index) {
    super(in_sourceInfo);

    if (in_array == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'array' to the ArrayAccess constructor was null. This class may not have null field values.");
    }
    _array = in_array;

    if (in_index == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'index' to the ArrayAccess constructor was null. This class may not have null field values.");
    }
    _index = in_index;
  }

  final public Expression getArray() { return _array; }
  final public Expression getIndex() { return _index; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forArrayAccess(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forArrayAccess(this); }

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
    writer.print("ArrayAccess" + ":");
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
    writer.print("array = ");
    Expression temp_array = getArray();
    if (temp_array == null) {
      writer.print("null");
    } else {
      temp_array.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("index = ");
    Expression temp_index = getIndex();
    if (temp_index == null) {
      writer.print("null");
    } else {
      temp_index.outputHelp(writer);
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
      ArrayAccess casted = (ArrayAccess) obj;
      if (! (getArray().equals(casted.getArray()))) return false;
      if (! (getIndex().equals(casted.getIndex()))) return false;
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
    code ^= getArray().hashCode();
    code ^= getIndex().hashCode();
    return code;
  }
}
