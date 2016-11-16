package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class SourceFile, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class SourceFile extends JExpression {
  private final PackageStatement[] _packageStatements;
  private final ImportStatement[] _importStatements;
  private final TypeDefBase[] _types;

  /**
   * Constructs a SourceFile.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public SourceFile(SourceInfo in_sourceInfo, PackageStatement[] in_packageStatements, ImportStatement[] in_importStatements, TypeDefBase[] in_types) {
    super(in_sourceInfo);

    if (in_packageStatements == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'packageStatements' to the SourceFile constructor was null. This class may not have null field values.");
    }
    _packageStatements = in_packageStatements;

    if (in_importStatements == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'importStatements' to the SourceFile constructor was null. This class may not have null field values.");
    }
    _importStatements = in_importStatements;

    if (in_types == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'types' to the SourceFile constructor was null. This class may not have null field values.");
    }
    _types = in_types;
  }

  final public PackageStatement[] getPackageStatements() { return _packageStatements; }
  final public ImportStatement[] getImportStatements() { return _importStatements; }
  final public TypeDefBase[] getTypes() { return _types; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forSourceFile(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forSourceFile(this); }

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
    writer.print("SourceFile" + ":");
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
    writer.print("packageStatements = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getPackageStatements().length; i++) {
      PackageStatement temp_packageStatements = getPackageStatements()[i];
      writer.startLine("#" + i + ": ");
      if (temp_packageStatements == null) {
        writer.print("null");
      } else {
        temp_packageStatements.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getPackageStatements().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("importStatements = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getImportStatements().length; i++) {
      ImportStatement temp_importStatements = getImportStatements()[i];
      writer.startLine("#" + i + ": ");
      if (temp_importStatements == null) {
        writer.print("null");
      } else {
        temp_importStatements.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getImportStatements().length > 0) {
      writer.startLine("");
    }
    writer.print("}");

    writer.startLine("");
    writer.print("types = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getTypes().length; i++) {
      TypeDefBase temp_types = getTypes()[i];
      writer.startLine("#" + i + ": ");
      if (temp_types == null) {
        writer.print("null");
      } else {
        temp_types.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getTypes().length > 0) {
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
      SourceFile casted = (SourceFile) obj;
      if (this.getPackageStatements().length != casted.getPackageStatements().length) return false;
      for (int i = 0; i < getPackageStatements().length; i++) if (! getPackageStatements()[i].equals(casted.getPackageStatements()[i])) return false;
      if (this.getImportStatements().length != casted.getImportStatements().length) return false;
      for (int i = 0; i < getImportStatements().length; i++) if (! getImportStatements()[i].equals(casted.getImportStatements()[i])) return false;
      if (this.getTypes().length != casted.getTypes().length) return false;
      for (int i = 0; i < getTypes().length; i++) if (! getTypes()[i].equals(casted.getTypes()[i])) return false;
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
    for (int i = 0; i < getPackageStatements().length; i++) code ^= getPackageStatements()[i].hashCode();
    for (int i = 0; i < getImportStatements().length; i++) code ^= getImportStatements()[i].hashCode();
    for (int i = 0; i < getTypes().length; i++) code ^= getTypes()[i].hashCode();
    return code;
  }
}
