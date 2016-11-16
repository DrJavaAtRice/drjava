package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class SimpleSuperConstructorInvocation, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public class SimpleSuperConstructorInvocation extends SuperConstructorInvocation {

  /**
   * Constructs a SimpleSuperConstructorInvocation.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public SimpleSuperConstructorInvocation(SourceInfo in_sourceInfo, ParenthesizedExpressionList in_arguments) {
    super(in_sourceInfo, in_arguments);
  }


  public <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor) { return visitor.forSimpleSuperConstructorInvocation(this); }
  public void visit(JExpressionIFVisitor_void visitor) { visitor.forSimpleSuperConstructorInvocation(this); }

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
    writer.print("SimpleSuperConstructorInvocation" + ":");
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
    writer.print("arguments = ");
    ParenthesizedExpressionList temp_arguments = getArguments();
    if (temp_arguments == null) {
      writer.print("null");
    } else {
      temp_arguments.outputHelp(writer);
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
      SimpleSuperConstructorInvocation casted = (SimpleSuperConstructorInvocation) obj;
      if (! (getArguments().equals(casted.getArguments()))) return false;
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
    code ^= getArguments().hashCode();
    return code;
  }
}
