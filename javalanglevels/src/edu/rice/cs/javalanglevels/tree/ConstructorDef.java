package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ConstructorDef, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ConstructorDef extends JExpression implements BodyItemI {
  private final Word _name;
  private final ModifiersAndVisibility _mav;
  private final FormalParameter[] _parameters;
  private final ReferenceType[] _throws;
  private final BracedBody _statements;

  /**
   * Constructs a ConstructorDef.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ConstructorDef(SourceInfo in_sourceInfo, Word in_name, ModifiersAndVisibility in_mav, FormalParameter[] in_parameters, ReferenceType[] in_throws, BracedBody in_statements) {
    super(in_sourceInfo);

    if (in_name == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'name' to the ConstructorDef constructor was null. This class may not have null field values.");
    }
    _name = in_name;

    if (in_mav == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'mav' to the ConstructorDef constructor was null. This class may not have null field values.");
    }
    _mav = in_mav;

    if (in_parameters == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'parameters' to the ConstructorDef constructor was null. This class may not have null field values.");
    }
    _parameters = in_parameters;

    if (in_throws == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'throws' to the ConstructorDef constructor was null. This class may not have null field values.");
    }
    _throws = in_throws;

    if (in_statements == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'statements' to the ConstructorDef constructor was null. This class may not have null field values.");
    }
    _statements = in_statements;
  }

  final public Word getName() { return _name; }
  final public ModifiersAndVisibility getMav() { return _mav; }
  final public FormalParameter[] getParameters() { return _parameters; }
  final public ReferenceType[] getThrows() { return _throws; }
  final public BracedBody getStatements() { return _statements; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forConstructorDef(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forConstructorDef(this); }

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
    writer.print("ConstructorDef" + ":");
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
    Word temp_name = getName();
    if (temp_name == null) {
      writer.print("null");
    } else {
      temp_name.outputHelp(writer);
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
    writer.print("parameters = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getParameters().length; i++) {
      FormalParameter temp_parameters = getParameters()[i];
      writer.startLine("#" + i + ": ");
      if (temp_parameters == null) {
        writer.print("null");
      } else {
        temp_parameters.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getParameters().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("throws = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getThrows().length; i++) {
      ReferenceType temp_throws = getThrows()[i];
      writer.startLine("#" + i + ": ");
      if (temp_throws == null) {
        writer.print("null");
      } else {
        temp_throws.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getThrows().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("statements = ");
    BracedBody temp_statements = getStatements();
    if (temp_statements == null) {
      writer.print("null");
    } else {
      temp_statements.outputHelp(writer);
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
      ConstructorDef casted = (ConstructorDef) obj;
      if (! (getName().equals(casted.getName()))) return false;
      if (! (getMav().equals(casted.getMav()))) return false;
      if (this.getParameters().length != casted.getParameters().length) return false;
      for (int i = 0; i < getParameters().length; i++) if (! getParameters()[i].equals(casted.getParameters()[i])) return false;
      if (this.getThrows().length != casted.getThrows().length) return false;
      for (int i = 0; i < getThrows().length; i++) if (! getThrows()[i].equals(casted.getThrows()[i])) return false;
      if (! (getStatements().equals(casted.getStatements()))) return false;
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
    code ^= getMav().hashCode();
    for (int i = 0; i < getParameters().length; i++) code ^= getParameters()[i].hashCode();
    for (int i = 0; i < getThrows().length; i++) code ^= getThrows()[i].hashCode();
    code ^= getStatements().hashCode();
    return code;
  }
}
