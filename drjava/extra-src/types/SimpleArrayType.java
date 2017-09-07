package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class SimpleArrayType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class SimpleArrayType extends ArrayType {

  /**
   * Constructs a SimpleArrayType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public SimpleArrayType(Type in_ofType) {
    super(in_ofType);
  }


  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forSimpleArrayType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forSimpleArrayType(this);
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
      SimpleArrayType casted = (SimpleArrayType) obj;
      Type temp_ofType = ofType();
      Type casted_ofType = casted.ofType();
      if (!(temp_ofType == casted_ofType || temp_ofType.equals(casted_ofType))) return false;
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
    Type temp_ofType = ofType();
    code ^= temp_ofType.hashCode();
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "SimpleArrayType", 1)) {
      Type temp_ofType = ofType();
      if (w.visitNodeField("ofType", temp_ofType)) {
        temp_ofType.walk(w);
        w.endNodeField("ofType", temp_ofType);
      }
      w.endNode(this, "SimpleArrayType", 1);
    }
  }

}
