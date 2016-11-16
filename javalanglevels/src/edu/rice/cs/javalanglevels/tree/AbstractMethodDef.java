package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class AbstractMethodDef, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class AbstractMethodDef extends MethodDef {

  /**
   * Constructs a AbstractMethodDef.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public AbstractMethodDef(SourceInfo in_sourceInfo, ModifiersAndVisibility in_mav, TypeParameter[] in_typeParams, ReturnTypeI in_result, Word in_name, FormalParameter[] in_params, ReferenceType[] in_throws) {
    super(in_sourceInfo, in_mav, in_typeParams, in_result, in_name, in_params, in_throws);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forAbstractMethodDef(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forAbstractMethodDef(this); }

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
    writer.print("AbstractMethodDef" + ":");
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
    writer.print("typeParams = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getTypeParams().length; i++) {
      TypeParameter temp_typeParams = getTypeParams()[i];
      writer.startLine("#" + i + ": ");
      if (temp_typeParams == null) {
        writer.print("null");
      } else {
        temp_typeParams.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getTypeParams().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("result = ");
    ReturnTypeI temp_result = getResult();
    if (temp_result == null) {
      writer.print("null");
    } else {
      temp_result.outputHelp(writer);
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
    writer.print("params = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getParams().length; i++) {
      FormalParameter temp_params = getParams()[i];
      writer.startLine("#" + i + ": ");
      if (temp_params == null) {
        writer.print("null");
      } else {
        temp_params.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getParams().length > 0) {
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
      AbstractMethodDef casted = (AbstractMethodDef) obj;
      if (! (getMav().equals(casted.getMav()))) return false;
      if (this.getTypeParams().length != casted.getTypeParams().length) return false;
      for (int i = 0; i < getTypeParams().length; i++) if (! getTypeParams()[i].equals(casted.getTypeParams()[i])) return false;
      if (! (getResult().equals(casted.getResult()))) return false;
      if (! (getName().equals(casted.getName()))) return false;
      if (this.getParams().length != casted.getParams().length) return false;
      for (int i = 0; i < getParams().length; i++) if (! getParams()[i].equals(casted.getParams()[i])) return false;
      if (this.getThrows().length != casted.getThrows().length) return false;
      for (int i = 0; i < getThrows().length; i++) if (! getThrows()[i].equals(casted.getThrows()[i])) return false;
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
    for (int i = 0; i < getTypeParams().length; i++) code ^= getTypeParams()[i].hashCode();
    code ^= getResult().hashCode();
    code ^= getName().hashCode();
    for (int i = 0; i < getParams().length; i++) code ^= getParams()[i].hashCode();
    for (int i = 0; i < getThrows().length; i++) code ^= getThrows()[i].hashCode();
    return code;
  }
}
