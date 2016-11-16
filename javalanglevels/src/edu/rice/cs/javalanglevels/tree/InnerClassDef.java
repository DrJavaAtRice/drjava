package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class InnerClassDef, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class InnerClassDef extends ClassDef implements BodyItemI {

  /**
   * Constructs a InnerClassDef.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public InnerClassDef(SourceInfo in_sourceInfo, ModifiersAndVisibility in_mav, Word in_name, TypeParameter[] in_typeParameters, ReferenceType in_superclass, ReferenceType[] in_interfaces, BracedBody in_body) {
    super(in_sourceInfo, in_mav, in_name, in_typeParameters, in_superclass, in_interfaces, in_body);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forInnerClassDef(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forInnerClassDef(this); }

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
    writer.print("InnerClassDef" + ":");
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
    writer.print("name = ");
    Word temp_name = getName();
    if (temp_name == null) {
      writer.print("null");
    } else {
      temp_name.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("typeParameters = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getTypeParameters().length; i++) {
      TypeParameter temp_typeParameters = getTypeParameters()[i];
      writer.startLine("#" + i + ": ");
      if (temp_typeParameters == null) {
        writer.print("null");
      } else {
        temp_typeParameters.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getTypeParameters().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("superclass = ");
    ReferenceType temp_superclass = getSuperclass();
    if (temp_superclass == null) {
      writer.print("null");
    } else {
      temp_superclass.outputHelp(writer);
    }

    writer.startLine("");
    writer.print("interfaces = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getInterfaces().length; i++) {
      ReferenceType temp_interfaces = getInterfaces()[i];
      writer.startLine("#" + i + ": ");
      if (temp_interfaces == null) {
        writer.print("null");
      } else {
        temp_interfaces.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getInterfaces().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("body = ");
    BracedBody temp_body = getBody();
    if (temp_body == null) {
      writer.print("null");
    } else {
      temp_body.outputHelp(writer);
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
      InnerClassDef casted = (InnerClassDef) obj;
      if (! (getMav().equals(casted.getMav()))) return false;
      if (! (getName().equals(casted.getName()))) return false;
      if (this.getTypeParameters().length != casted.getTypeParameters().length) return false;
      for (int i = 0; i < getTypeParameters().length; i++) if (! getTypeParameters()[i].equals(casted.getTypeParameters()[i])) return false;
      if (! (getSuperclass().equals(casted.getSuperclass()))) return false;
      if (this.getInterfaces().length != casted.getInterfaces().length) return false;
      for (int i = 0; i < getInterfaces().length; i++) if (! getInterfaces()[i].equals(casted.getInterfaces()[i])) return false;
      if (! (getBody().equals(casted.getBody()))) return false;
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
    code ^= getName().hashCode();
    for (int i = 0; i < getTypeParameters().length; i++) code ^= getTypeParameters()[i].hashCode();
    code ^= getSuperclass().hashCode();
    for (int i = 0; i < getInterfaces().length; i++) code ^= getInterfaces()[i].hashCode();
    code ^= getBody().hashCode();
    return code;
  }
}
