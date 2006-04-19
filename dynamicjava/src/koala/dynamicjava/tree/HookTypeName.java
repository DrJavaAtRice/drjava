
package koala.dynamicjava.tree;

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the HookTypeName (?) nodes of the syntax tree
 *
 */

public class HookTypeName extends ReferenceTypeName {
  
  ReferenceTypeName hookedType;
  boolean supered;
  
  /**
   * Initializes the type
   * @param type the hooked type
   * @exception IllegalArgumentException if type is null
   */
  public HookTypeName(ReferenceTypeName type, boolean supered) {
    this(type, supered, null, 0, 0, 0, 0);
  }

  /**
   * Initializes the type
   * @param type the hooked type
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if type is null
   */
  public HookTypeName(ReferenceTypeName type, boolean _supered, String fn, int bl, int bc, int el, int ec) {
    super("?", fn, bl, bc, el, ec);

    if (type == null) throw new IllegalArgumentException("type == null");
    hookedType = type;
    supered = _supered;
  }

  /**
   * Returns the representation of this type
   */
  public String getRepresentation() {
    if(supered) return "java.lang.Object";
    return hookedType.getRepresentation();
  }

  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
    /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+toStringHelper()+")";
  }

  protected String toStringHelper() {
   return getRepresentation();
  }
}
