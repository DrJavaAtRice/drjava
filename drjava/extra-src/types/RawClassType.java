package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class RawClassType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class RawClassType extends ClassType {

  /**
   * Constructs a RawClassType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public RawClassType(DJClass in_ofClass) {
    super(in_ofClass);
  }


  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forRawClassType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forRawClassType(this);
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
      RawClassType casted = (RawClassType) obj;
      DJClass temp_ofClass = ofClass();
      DJClass casted_ofClass = casted.ofClass();
      if (!(temp_ofClass == casted_ofClass || temp_ofClass.equals(casted_ofClass))) return false;
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
    DJClass temp_ofClass = ofClass();
    code ^= temp_ofClass.hashCode();
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "RawClassType", 1)) {
      DJClass temp_ofClass = ofClass();
      if (w.visitNodeField("ofClass", temp_ofClass)) {
        w.visitUnknownObject(temp_ofClass);
        w.endNodeField("ofClass", temp_ofClass);
      }
      w.endNode(this, "RawClassType", 1);
    }
  }

}
