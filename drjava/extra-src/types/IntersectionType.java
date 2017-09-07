package edu.rice.cs.drjava.model.repl.types;
import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class IntersectionType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class IntersectionType extends BoundType {

  /**
   * Constructs a IntersectionType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public IntersectionType(Iterable<? extends Type> in_ofTypes) {
    super(in_ofTypes);
  }


  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forIntersectionType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forIntersectionType(this);
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
      IntersectionType casted = (IntersectionType) obj;
      Iterable<? extends Type> temp_ofTypes = ofTypes();
      Iterable<? extends Type> casted_ofTypes = casted.ofTypes();
      if (temp_ofTypes != casted_ofTypes) {
        java.util.Iterator<? extends Type> iter_temp_ofTypes = temp_ofTypes.iterator();
        java.util.Iterator<? extends Type> iter_casted_ofTypes = casted_ofTypes.iterator();
        while (iter_temp_ofTypes.hasNext() && iter_casted_ofTypes.hasNext()) {
          Type elt_temp_ofTypes = iter_temp_ofTypes.next();
          Type elt_casted_ofTypes = iter_casted_ofTypes.next();
          if (!(elt_temp_ofTypes == elt_casted_ofTypes || elt_temp_ofTypes != null && elt_casted_ofTypes!= null && elt_temp_ofTypes.equals(elt_casted_ofTypes))) return false;
        }
        if (iter_temp_ofTypes.hasNext() || iter_casted_ofTypes.hasNext()) return false;
      }
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
    Iterable<? extends Type> temp_ofTypes = ofTypes();
    code ^= temp_ofTypes.getClass().hashCode();
    int index_temp_ofTypes = 0;
    for (Type elt_temp_ofTypes : temp_ofTypes) {
      code ^= index_temp_ofTypes++;
      code ^= (elt_temp_ofTypes == null) ? 0 : elt_temp_ofTypes.hashCode();
    }
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "IntersectionType", 1)) {
      Iterable<? extends Type> temp_ofTypes = ofTypes();
      if (w.visitNodeField("ofTypes", temp_ofTypes)) {
        if (w.visitIterated(temp_ofTypes)) {
          int i_temp_ofTypes = 0;
          for (Type elt_temp_ofTypes : temp_ofTypes) {
            if (w.visitIteratedElement(i_temp_ofTypes, elt_temp_ofTypes)) {
              if (elt_temp_ofTypes == null) w.visitNull();
              else {
                elt_temp_ofTypes.walk(w);
              }
            }
            i_temp_ofTypes++;
          }
          w.endIterated(temp_ofTypes, i_temp_ofTypes);
        }
        w.endNodeField("ofTypes", temp_ofTypes);
      }
      w.endNode(this, "IntersectionType", 1);
    }
  }

}
