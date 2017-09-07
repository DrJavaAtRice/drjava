package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class BooleanType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class BooleanType extends PrimitiveType {

  /**
   * Constructs a BooleanType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public BooleanType() {
    super();
  }


  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forBooleanType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forBooleanType(this);
  }

  /**
   * Implementation of toString that uses
   * {@link #output} to generate a nicely tabbed tree.
   */
  public java.lang.String toString() {
    java.io.StringWriter w = new java.io.StringWriter();
    walk(new ToStringWalker(w, 2));
    return w.toString();
  }

  /**
   * Prints this object out as a nicely tabbed tree.
   */
  public void output(java.io.Writer writer) {
    walk(new ToStringWalker(writer, 2));
  }

  /**
   * Implementation of equals that is based on the values of the fields of the
   * object. Thus, two objects created with identical parameters will be equal.
   */
  public boolean equals(java.lang.Object obj) {
    if (obj == null) return false;
    if ((obj.getClass() != this.getClass()) || (obj.hashCode() != this.hashCode())) {
      return false;
    }
    else {
      BooleanType casted = (BooleanType) obj;
      return true;
    }
  }


  /**
   * Implementation of hashCode that is consistent with equals.  The value of
   * the hashCode is formed by XORing the hashcode of the class object with
   * the hashcodes of all the fields of the object.
   */
  public int generateHashCode() {
    int code = getClass().hashCode();
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "BooleanType", 0)) {
      w.endNode(this, "BooleanType", 0);
    }
  }

}
