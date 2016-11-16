package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class CompoundWord, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class CompoundWord extends JExpression {
  private final Word[] _words;

  /**
   * Constructs a CompoundWord.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public CompoundWord(SourceInfo in_sourceInfo, Word[] in_words) {
    super(in_sourceInfo);

    if (in_words == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'words' to the CompoundWord constructor was null. This class may not have null field values.");
    }
    _words = in_words;
  }

  final public Word[] getWords() { return _words; }

  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forCompoundWord(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forCompoundWord(this); }

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
    writer.print("CompoundWord" + ":");
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
    writer.print("words = ");
    writer.print("{");
    writer.indent();
    for (int i = 0; i < getWords().length; i++) {
      Word temp_words = getWords()[i];
      writer.startLine("#" + i + ": ");
      if (temp_words == null) {
        writer.print("null");
      } else {
        temp_words.outputHelp(writer);
      }
    }
    writer.unindent();
    if (getWords().length > 0) {
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
      CompoundWord casted = (CompoundWord) obj;
      if (this.getWords().length != casted.getWords().length) return false;
      for (int i = 0; i < getWords().length; i++) if (! getWords()[i].equals(casted.getWords()[i])) return false;
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
    for (int i = 0; i < getWords().length; i++) code ^= getWords()[i].hashCode();
    return code;
  }
}
