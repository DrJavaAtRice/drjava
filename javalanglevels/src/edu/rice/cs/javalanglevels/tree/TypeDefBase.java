package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class TypeDefBase, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class TypeDefBase extends JExpression {
  private final ModifiersAndVisibility _mav;
  private final Word _name;
  private final TypeParameter[] _typeParameters;
  private final ReferenceType[] _interfaces;
  private final BracedBody _body;

  /**
   * Constructs a TypeDefBase.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public TypeDefBase(SourceInfo in_sourceInfo, ModifiersAndVisibility in_mav, Word in_name, TypeParameter[] in_typeParameters, ReferenceType[] in_interfaces, BracedBody in_body) {
    super(in_sourceInfo);

    if (in_mav == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'mav' to the TypeDefBase constructor was null. This class may not have null field values.");
    }
    _mav = in_mav;

    if (in_name == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'name' to the TypeDefBase constructor was null. This class may not have null field values.");
    }
    _name = in_name;

    if (in_typeParameters == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'typeParameters' to the TypeDefBase constructor was null. This class may not have null field values.");
    }
    _typeParameters = in_typeParameters;

    if (in_interfaces == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'interfaces' to the TypeDefBase constructor was null. This class may not have null field values.");
    }
    _interfaces = in_interfaces;

    if (in_body == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'body' to the TypeDefBase constructor was null. This class may not have null field values.");
    }
    _body = in_body;
  }

  public ModifiersAndVisibility getMav() { return _mav; }
  public Word getName() { return _name; }
  public TypeParameter[] getTypeParameters() { return _typeParameters; }
  public ReferenceType[] getInterfaces() { return _interfaces; }
  public BracedBody getBody() { return _body; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
