package edu.rice.cs.javalanglevels.tree;

import edu.rice.cs.javalanglevels.SourceInfo;

/**
 * Class MethodDef, a component of the JExpressionIF composite hierarchy.
 * Note: null is not allowed as a value for any field.
 * @version  Generated automatically by ASTGen at Wed Oct 26 13:40:50 CDT 2016
 */
public abstract class MethodDef extends JExpression implements BodyItemI {
  private final ModifiersAndVisibility _mav;
  private final TypeParameter[] _typeParams;
  private final ReturnTypeI _result;
  private final Word _name;
  private final FormalParameter[] _params;
  private final ReferenceType[] _throws;

  /**
   * Constructs a MethodDef.
   * @throws java.lang.IllegalArgumentException  If any parameter to the constructor is null.
   */
  public MethodDef(SourceInfo in_sourceInfo, ModifiersAndVisibility in_mav, TypeParameter[] in_typeParams, ReturnTypeI in_result, Word in_name, FormalParameter[] in_params, ReferenceType[] in_throws) {
    super(in_sourceInfo);

    if (in_mav == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'mav' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _mav = in_mav;

    if (in_typeParams == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'typeParams' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _typeParams = in_typeParams;

    if (in_result == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'result' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _result = in_result;

    if (in_name == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'name' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _name = in_name;

    if (in_params == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'params' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _params = in_params;

    if (in_throws == null) {
      throw new java.lang.IllegalArgumentException("Parameter 'throws' to the MethodDef constructor was null. This class may not have null field values.");
    }
    _throws = in_throws;
  }

  public ModifiersAndVisibility getMav() { return _mav; }
  public TypeParameter[] getTypeParams() { return _typeParams; }
  public ReturnTypeI getResult() { return _result; }
  public Word getName() { return _name; }
  public FormalParameter[] getParams() { return _params; }
  public ReferenceType[] getThrows() { return _throws; }

  public abstract <RetType> RetType visit(JExpressionIFVisitor<RetType> visitor);
  public abstract void visit(JExpressionIFVisitor_void visitor);
  public abstract void outputHelp(TabPrintWriter writer);
  protected abstract int generateHashCode();
}
