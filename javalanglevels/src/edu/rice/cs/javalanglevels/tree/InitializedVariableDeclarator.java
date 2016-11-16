package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class InitializedVariableDeclarator, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class InitializedVariableDeclarator extends VariableDeclarator {
  private final VariableInitializerI _initializer;

  /**
   * Constructs a InitializedVariableDeclarator.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public InitializedVariableDeclarator(SourceInfo in_sourceInfo, Type in_type, Word in_name, VariableInitializerI in_initializer) {
    super(in_sourceInfo, in_type, in_name);

    if (in_initializer == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'initializer' to the InitializedVariableDeclarator constructor was null. This class may not have null field values.");
    }
    _initializer = in_initializer;
  }

  final public VariableInitializerI getInitializer() { return _initializer; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forInitializedVariableDeclarator(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forInitializedVariableDeclarator(this); }

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
    writer.print("InitializedVariableDeclarator" + ":");
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
    Type temp_type = getType();
    if (temp_type == null) {
      writer.print("null");
    } else {
      temp_type.outputHelp(writer);
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
    writer.print("initializer = ");
    VariableInitializerI temp_initializer = getInitializer();
    if (temp_initializer == null) {
      writer.print("null");
    } else {
      temp_initializer.outputHelp(writer);
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
      InitializedVariableDeclarator casted = (InitializedVariableDeclarator) obj;
      if (! (getType().equals(casted.getType()))) return false;
      if (! (getName().equals(casted.getName()))) return false;
      if (! (getInitializer().equals(casted.getInitializer()))) return false;
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
    code ^= getName().hashCode();
    code ^= getInitializer().hashCode();
    return code;
  }
}
