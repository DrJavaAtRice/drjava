package edu.rice.cs.drjava.model.repl.types;

import edu.rice.cs.drjava.model.repl.newjvm.*;

/**
 * Class VariableType, a component of the ASTGen-generated composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Thu Oct 16 08:57:12 CDT 2014
 */
//@SuppressWarnings("unused")
public class VariableType extends ValidType {
  private final BoundedSymbol _symbol;

  /**
   * Constructs a VariableType.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public VariableType(BoundedSymbol in_symbol) {
    super();
    if (in_symbol == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'symbol' to the VariableType constructor was null");
    }
    _symbol = in_symbol;
  }

  final public BoundedSymbol symbol() { return _symbol; }

  public <RetType> RetType apply(TypeVisitor<RetType> visitor) {
    return visitor.forVariableType(this);
  }

  public void apply(TypeVisitor_void visitor) {
    visitor.forVariableType(this);
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
      VariableType casted = (VariableType) obj;
      BoundedSymbol temp_symbol = symbol();
      BoundedSymbol casted_symbol = casted.symbol();
      if (!(temp_symbol == casted_symbol || temp_symbol.equals(casted_symbol))) return false;
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
    BoundedSymbol temp_symbol = symbol();
    code ^= temp_symbol.hashCode();
    return code;
  }

  public void walk(TreeWalker w) {
    if (w.visitNode(this, "VariableType", 1)) {
      BoundedSymbol temp_symbol = symbol();
      if (w.visitNodeField("symbol", temp_symbol)) {
        w.visitUnknownObject(temp_symbol);
        w.endNodeField("symbol", temp_symbol);
      }
      w.endNode(this, "VariableType", 1);
    }
  }

}
