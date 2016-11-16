package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class ClassOrInterfaceType, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class ClassOrInterfaceType extends ReferenceType {
  private final Type[] _typeArguments;

  /**
   * Constructs a ClassOrInterfaceType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ClassOrInterfaceType(SourceInfo in_sourceInfo, String in_name, Type[] in_typeArguments) {
    super(in_sourceInfo, in_name);

    if (in_typeArguments == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'typeArguments' to the ClassOrInterfaceType constructor was null. This class may not have null field values.");
    }
    _typeArguments = in_typeArguments;
  }

  final public Type[] getTypeArguments() { return _typeArguments; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forClassOrInterfaceType(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forClassOrInterfaceType(this); }

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
    writer.print("ClassOrInterfaceType" + ":");
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
    writer.print("typeArguments = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getTypeArguments().length; i++) {
      Type temp_typeArguments = getTypeArguments()[i];
      writer.startLine("#" + i + ": ");
      if (temp_typeArguments == null) {
        writer.print("null");
      } else {
        temp_typeArguments.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getTypeArguments().length > 0) {
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
      ClassOrInterfaceType casted = (ClassOrInterfaceType) obj;
      if (! (getName() == casted.getName())) return false;
      if (this.getTypeArguments().length != casted.getTypeArguments().length) return false;
      for (int i = 0; i < getTypeArguments().length; i++) if (! getTypeArguments()[i].equals(casted.getTypeArguments()[i])) return false;
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
    for (int i = 0; i < getTypeArguments().length; i++) code ^= getTypeArguments()[i].hashCode();
    return code;
  }
}
