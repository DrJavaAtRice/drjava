package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ModifiersAndVisibility, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ModifiersAndVisibility extends JExpression {
  private final String[] _modifiers;

  /**
   * Constructs a ModifiersAndVisibility.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ModifiersAndVisibility(SourceInfo in_sourceInfo, String[] in_modifiers) {
    super(in_sourceInfo);

    if (in_modifiers == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'modifiers' to the ModifiersAndVisibility constructor was null. This class may not have null field values.");
    }
    _modifiers = in_modifiers;
  }

  final public String[] getModifiers() { return _modifiers; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forModifiersAndVisibility(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forModifiersAndVisibility(this); }

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
    writer.print("ModifiersAndVisibility" + ":");
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
    writer.print("modifiers = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getModifiers().length; i++) {
      String temp_modifiers = getModifiers()[i];
      writer.startLine("#" + i + ": ");
      if (temp_modifiers == null) {
        writer.print("null");
      } else {
        writer.print(temp_modifiers);
      }
    }
    writer.unindent();
    if (getModifiers().length > 0) {
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
      ModifiersAndVisibility casted = (ModifiersAndVisibility) obj;
      if (this.getModifiers().length != casted.getModifiers().length) return false;
      for (int i = 0; i < getModifiers().length; i++) if (! (getModifiers()[i] == casted.getModifiers()[i])) return false;
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
    for (int i = 0; i < getModifiers().length; i++) code ^= getModifiers()[i].hashCode();
    return code;
  }
}
