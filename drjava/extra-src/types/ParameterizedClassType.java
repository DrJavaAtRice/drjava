package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class ParameterizedClassType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class ParameterizedClassType extends ClassType {
  private final Iterable<? extends Type> _typeArguments;

  /**
   * Constructs a ParameterizedClassType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public ParameterizedClassType(DJClass in_ofClass, Iterable<? extends Type> in_typeArguments) {
    super(in_ofClass);
    if (in_typeArguments == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'typeArguments' to the ParameterizedClassType constructor was null");
    }
    _typeArguments = in_typeArguments;
  }

  final public Iterable<? extends Type> typeArguments() { return _typeArguments; }

  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forParameterizedClassType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forParameterizedClassType(this);
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
      ParameterizedClassType casted = (ParameterizedClassType) obj;
      DJClass temp_ofClass = ofClass();
      DJClass casted_ofClass = casted.ofClass();
      if (!(temp_ofClass == casted_ofClass || temp_ofClass.equals(casted_ofClass))) return false;
      Iterable<? extends Type> temp_typeArguments = typeArguments();
      Iterable<? extends Type> casted_typeArguments = casted.typeArguments();
      if (temp_typeArguments != casted_typeArguments) {
        java.util.Iterator<? extends Type> iter_temp_typeArguments = temp_typeArguments.iterator();
        java.util.Iterator<? extends Type> iter_casted_typeArguments = casted_typeArguments.iterator();
        while (iter_temp_typeArguments.hasNext() && iter_casted_typeArguments.hasNext()) {
          Type elt_temp_typeArguments = iter_temp_typeArguments.next();
          Type elt_casted_typeArguments = iter_casted_typeArguments.next();
          if (!(elt_temp_typeArguments == elt_casted_typeArguments || elt_temp_typeArguments != null && elt_casted_typeArguments!= null && elt_temp_typeArguments.equals(elt_casted_typeArguments))) return false;
        }
        if (iter_temp_typeArguments.hasNext() || iter_casted_typeArguments.hasNext()) return false;
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
    DJClass temp_ofClass = ofClass();
    code ^= temp_ofClass.hashCode();
    Iterable<? extends Type> temp_typeArguments = typeArguments();
    code ^= temp_typeArguments.getClass().hashCode();
    int index_temp_typeArguments = 0;
    for (Type elt_temp_typeArguments : temp_typeArguments) {
      code ^= index_temp_typeArguments++;
      code ^= (elt_temp_typeArguments == null) ? 0 : elt_temp_typeArguments.hashCode();
    }
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "ParameterizedClassType", 2)) {
      DJClass temp_ofClass = ofClass();
      if (w.visitNodeField("ofClass", temp_ofClass)) {
        w.visitUnknownObject(temp_ofClass);
        w.endNodeField("ofClass", temp_ofClass);
      }
      Iterable<? extends Type> temp_typeArguments = typeArguments();
      if (w.visitNodeField("typeArguments", temp_typeArguments)) {
        if (w.visitIterated(temp_typeArguments)) {
          int i_temp_typeArguments = 0;
          for (Type elt_temp_typeArguments : temp_typeArguments) {
            if (w.visitIteratedElement(i_temp_typeArguments, elt_temp_typeArguments)) {
              if (elt_temp_typeArguments == null) w.visitNull();
              else {
                elt_temp_typeArguments.walk(w);
              }
            }
            i_temp_typeArguments++;
          }
          w.endIterated(temp_typeArguments, i_temp_typeArguments);
        }
        w.endNodeField("typeArguments", temp_typeArguments);
      }
      w.endNode(this, "ParameterizedClassType", 2);
    }
  }

}
