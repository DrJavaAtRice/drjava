package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class VariableDeclaration, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class VariableDeclaration extends JExpression implements BodyItemI, ForInitI {
  private final ModifiersAndVisibility _mav;
  private final VariableDeclarator[] _declarators;

  /**
   * Constructs a VariableDeclaration.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public VariableDeclaration(SourceInfo in_sourceInfo, ModifiersAndVisibility in_mav, VariableDeclarator[] in_declarators) {
    super(in_sourceInfo);

    if (in_mav == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'mav' to the VariableDeclaration constructor was null. This class may not have null field values.");
    }
    _mav = in_mav;

    if (in_declarators == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'declarators' to the VariableDeclaration constructor was null. This class may not have null field values.");
    }
    _declarators = in_declarators;
  }

  final public ModifiersAndVisibility getMav() { return _mav; }
  final public VariableDeclarator[] getDeclarators() { return _declarators; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forVariableDeclaration(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forVariableDeclaration(this); }

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
    writer.print("VariableDeclaration" + ":");
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
    writer.print("mav = ");
    ModifiersAndVisibility temp_mav = getMav();
    if (temp_mav == null) {
      writer.print("null");
    } else {
      temp_mav.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("declarators = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getDeclarators().length; i++) {
      VariableDeclarator temp_declarators = getDeclarators()[i];
      writer.startLine("#" + i + ": ");
      if (temp_declarators == null) {
        writer.print("null");
      } else {
        temp_declarators.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getDeclarators().length > 0) {
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
      VariableDeclaration casted = (VariableDeclaration) obj;
      if (! (getMav().equals(casted.getMav()))) return false;
      if (this.getDeclarators().length != casted.getDeclarators().length) return false;
      for (int i = 0; i < getDeclarators().length; i++) if (! getDeclarators()[i].equals(casted.getDeclarators()[i])) return false;
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
    code ^= getMav().hashCode();
    for (int i = 0; i < getDeclarators().length; i++) code ^= getDeclarators()[i].hashCode();
    return code;
  }
}
