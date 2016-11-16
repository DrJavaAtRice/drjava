package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ArrayInitializer, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ArrayInitializer extends JExpression implements VariableInitializerI {
  private final VariableInitializerI[] _items;

  /**
   * Constructs a ArrayInitializer.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ArrayInitializer(SourceInfo in_sourceInfo, VariableInitializerI[] in_items) {
    super(in_sourceInfo);

    if (in_items == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'items' to the ArrayInitializer constructor was null. This class may not have null field values.");
    }
    _items = in_items;
  }

  final public VariableInitializerI[] getItems() { return _items; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forArrayInitializer(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forArrayInitializer(this); }

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
    writer.print("ArrayInitializer" + ":");
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
    writer.print("items = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getItems().length; i++) {
      VariableInitializerI temp_items = getItems()[i];
      writer.startLine("#" + i + ": ");
      if (temp_items == null) {
        writer.print("null");
      } else {
        temp_items.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getItems().length > 0) {
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
      ArrayInitializer casted = (ArrayInitializer) obj;
      if (this.getItems().length != casted.getItems().length) return false;
      for (int i = 0; i < getItems().length; i++) if (! getItems()[i].equals(casted.getItems()[i])) return false;
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
    for (int i = 0; i < getItems().length; i++) code ^= getItems()[i].hashCode();
    return code;
  }
}
