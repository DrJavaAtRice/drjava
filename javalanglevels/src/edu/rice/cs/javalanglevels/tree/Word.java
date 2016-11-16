package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class Word, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class Word extends JExpression {
  private final String _text;

  /**
   * Constructs a Word.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public Word(SourceInfo in_sourceInfo, String in_text) {
    super(in_sourceInfo);

    if (in_text == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'text' to the Word constructor was null. This class may not have null field values.");
    }
    _text = ((in_text == null) ? null : in_text.intern());
  }

  final public String getText() { return _text; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forWord(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forWord(this); }

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
    writer.print("Word" + ":");
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
    writer.print("text = ");
    String temp_text = getText();
    if (temp_text == null) {
      writer.print("null");
    } else {
      writer.print(temp_text);
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
      Word casted = (Word) obj;
      if (! (getText() == casted.getText())) return false;
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
    code ^= getText().hashCode();
    return code;
  }
}
