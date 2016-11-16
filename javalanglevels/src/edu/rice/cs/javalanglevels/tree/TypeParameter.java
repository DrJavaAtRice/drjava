package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class TypeParameter, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class TypeParameter extends JExpression {
  private final TypeVariable _variable;
  private final ReferenceType _bound;

  /**
   * Constructs a TypeParameter.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public TypeParameter(SourceInfo in_sourceInfo, TypeVariable in_variable, ReferenceType in_bound) {
    super(in_sourceInfo);

    if (in_variable == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'variable' to the TypeParameter constructor was null. This class may not have null field values.");
    }
    _variable = in_variable;

    if (in_bound == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'bound' to the TypeParameter constructor was null. This class may not have null field values.");
    }
    _bound = in_bound;
  }

  final public TypeVariable getVariable() { return _variable; }
  final public ReferenceType getBound() { return _bound; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forTypeParameter(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forTypeParameter(this); }

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
    writer.print("TypeParameter" + ":");
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
    writer.print("variable = ");
    TypeVariable temp_variable = getVariable();
    if (temp_variable == null) {
      writer.print("null");
    } else {
      temp_variable.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("bound = ");
    ReferenceType temp_bound = getBound();
    if (temp_bound == null) {
      writer.print("null");
    } else {
      temp_bound.outputHelp(writer);
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
      TypeParameter casted = (TypeParameter) obj;
      if (! (getVariable().equals(casted.getVariable()))) return false;
      if (! (getBound().equals(casted.getBound()))) return false;
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
    code ^= getVariable().hashCode();
    code ^= getBound().hashCode();
    return code;
  }
}
