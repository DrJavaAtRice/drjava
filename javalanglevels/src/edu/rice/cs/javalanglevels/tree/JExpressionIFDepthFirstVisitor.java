package edu.rice.cs.javalanglevels.tree;

/** A parametric abstract implementation of a visitor over JExpressionIF that return a value.
 ** This visitor implements the visitor interface with methods that 
 ** first visit children, and then call visitCASEOnly(), passing in 
 ** the values of the visits of the children. (CASE is replaced by the case name.)
 **/
public abstract class JExpressionIFDepthFirstVisitor<RetType> implements JExpressionIFVisitor<RetType> {
  protected abstract RetType[] makeArrayOfRetType(int len);

  /**
   * This method is called by default from cases that do not 
   * override forCASEOnly.
  **/
  protected abstract RetType defaultCase(JExpressionIF that);

  /* Methods to visit an item. */
  public RetType forJExpressionOnly(JExpression that) {
    return defaultCase(that);
  }

  public RetType forSourceFileOnly(SourceFile that, RetType[] packageStatements_result, RetType[] importStatements_result, RetType[] types_result) {
    return forJExpressionOnly(that);
  }

  public RetType forModifiersAndVisibilityOnly(ModifiersAndVisibility that) {
    return forJExpressionOnly(that);
  }

  public RetType forCompoundWordOnly(CompoundWord that, RetType[] words_result) {
    return forJExpressionOnly(that);
  }

  public RetType forWordOnly(Word that) {
    return forJExpressionOnly(that);
  }

  public RetType forTypeDefBaseOnly(TypeDefBase that, RetType mav_result, RetType name_result, RetType[] typeParameters_result, RetType[] interfaces_result, RetType body_result) {
    return forJExpressionOnly(that);
  }

  public RetType forClassDefOnly(ClassDef that, RetType mav_result, RetType name_result, RetType[] typeParameters_result, RetType superclass_result, RetType[] interfaces_result, RetType body_result) {
    return forTypeDefBaseOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }

  public RetType forInnerClassDefOnly(InnerClassDef that, RetType mav_result, RetType name_result, RetType[] typeParameters_result, RetType superclass_result, RetType[] interfaces_result, RetType body_result) {
    return forClassDefOnly(that, mav_result, name_result, typeParameters_result, superclass_result, interfaces_result, body_result);
  }

  public RetType forInterfaceDefOnly(InterfaceDef that, RetType mav_result, RetType name_result, RetType[] typeParameters_result, RetType[] interfaces_result, RetType body_result) {
    return forTypeDefBaseOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }

  public RetType forInnerInterfaceDefOnly(InnerInterfaceDef that, RetType mav_result, RetType name_result, RetType[] typeParameters_result, RetType[] interfaces_result, RetType body_result) {
    return forInterfaceDefOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }

  public RetType forConstructorDefOnly(ConstructorDef that, RetType name_result, RetType mav_result, RetType[] parameters_result, RetType[] throws_result, RetType statements_result) {
    return forJExpressionOnly(that);
  }

  public RetType forInitializerOnly(Initializer that, RetType code_result) {
    return forJExpressionOnly(that);
  }

  public RetType forInstanceInitializerOnly(InstanceInitializer that, RetType code_result) {
    return forInitializerOnly(that, code_result);
  }

  public RetType forStaticInitializerOnly(StaticInitializer that, RetType code_result) {
    return forInitializerOnly(that, code_result);
  }

  public RetType forPackageStatementOnly(PackageStatement that, RetType cWord_result) {
    return forJExpressionOnly(that);
  }

  public RetType forImportStatementOnly(ImportStatement that, RetType cWord_result) {
    return forJExpressionOnly(that);
  }

  public RetType forClassImportStatementOnly(ClassImportStatement that, RetType cWord_result) {
    return forImportStatementOnly(that, cWord_result);
  }

  public RetType forPackageImportStatementOnly(PackageImportStatement that, RetType cWord_result) {
    return forImportStatementOnly(that, cWord_result);
  }

  public RetType forStatementOnly(Statement that) {
    return forJExpressionOnly(that);
  }

  public RetType forLabeledStatementOnly(LabeledStatement that, RetType label_result, RetType statement_result) {
    return forStatementOnly(that);
  }

  public RetType forBlockOnly(Block that, RetType statements_result) {
    return forStatementOnly(that);
  }

  public RetType forExpressionStatementOnly(ExpressionStatement that, RetType expression_result) {
    return forStatementOnly(that);
  }

  public RetType forSwitchStatementOnly(SwitchStatement that, RetType test_result, RetType[] cases_result) {
    return forStatementOnly(that);
  }

  public RetType forIfThenStatementOnly(IfThenStatement that, RetType testExpression_result, RetType thenStatement_result) {
    return forStatementOnly(that);
  }

  public RetType forIfThenElseStatementOnly(IfThenElseStatement that, RetType testExpression_result, RetType thenStatement_result, RetType elseStatement_result) {
    return forIfThenStatementOnly(that, testExpression_result, thenStatement_result);
  }

  public RetType forWhileStatementOnly(WhileStatement that, RetType condition_result, RetType code_result) {
    return forStatementOnly(that);
  }

  public RetType forDoStatementOnly(DoStatement that, RetType code_result, RetType condition_result) {
    return forStatementOnly(that);
  }

  public RetType forForStatementOnly(ForStatement that, RetType init_result, RetType condition_result, RetType update_result, RetType code_result) {
    return forStatementOnly(that);
  }

  public RetType forBreakStatementOnly(BreakStatement that) {
    return forStatementOnly(that);
  }

  public RetType forLabeledBreakStatementOnly(LabeledBreakStatement that, RetType label_result) {
    return forBreakStatementOnly(that);
  }

  public RetType forUnlabeledBreakStatementOnly(UnlabeledBreakStatement that) {
    return forBreakStatementOnly(that);
  }

  public RetType forContinueStatementOnly(ContinueStatement that) {
    return forStatementOnly(that);
  }

  public RetType forLabeledContinueStatementOnly(LabeledContinueStatement that, RetType label_result) {
    return forContinueStatementOnly(that);
  }

  public RetType forUnlabeledContinueStatementOnly(UnlabeledContinueStatement that) {
    return forContinueStatementOnly(that);
  }

  public RetType forReturnStatementOnly(ReturnStatement that) {
    return forStatementOnly(that);
  }

  public RetType forVoidReturnStatementOnly(VoidReturnStatement that) {
    return forReturnStatementOnly(that);
  }

  public RetType forValueReturnStatementOnly(ValueReturnStatement that, RetType value_result) {
    return forReturnStatementOnly(that);
  }

  public RetType forThrowStatementOnly(ThrowStatement that, RetType thrown_result) {
    return forStatementOnly(that);
  }

  public RetType forSynchronizedStatementOnly(SynchronizedStatement that, RetType lockExpr_result, RetType block_result) {
    return forStatementOnly(that);
  }

  public RetType forTryCatchStatementOnly(TryCatchStatement that, RetType tryBlock_result, RetType[] catchBlocks_result) {
    return forStatementOnly(that);
  }

  public RetType forTryCatchFinallyStatementOnly(TryCatchFinallyStatement that, RetType tryBlock_result, RetType[] catchBlocks_result, RetType finallyBlock_result) {
    return forTryCatchStatementOnly(that, tryBlock_result, catchBlocks_result);
  }

  public RetType forNormalTryCatchStatementOnly(NormalTryCatchStatement that, RetType tryBlock_result, RetType[] catchBlocks_result) {
    return forTryCatchStatementOnly(that, tryBlock_result, catchBlocks_result);
  }

  public RetType forEmptyStatementOnly(EmptyStatement that) {
    return forStatementOnly(that);
  }

  public RetType forMethodDefOnly(MethodDef that, RetType mav_result, RetType[] typeParams_result, RetType result_result, RetType name_result, RetType[] params_result, RetType[] throws_result) {
    return forJExpressionOnly(that);
  }

  public RetType forConcreteMethodDefOnly(ConcreteMethodDef that, RetType mav_result, RetType[] typeParams_result, RetType result_result, RetType name_result, RetType[] params_result, RetType[] throws_result, RetType body_result) {
    return forMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result);
  }

  public RetType forAbstractMethodDefOnly(AbstractMethodDef that, RetType mav_result, RetType[] typeParams_result, RetType result_result, RetType name_result, RetType[] params_result, RetType[] throws_result) {
    return forMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result);
  }

  public RetType forFormalParameterOnly(FormalParameter that, RetType declarator_result) {
    return forJExpressionOnly(that);
  }

  public RetType forVariableDeclarationOnly(VariableDeclaration that, RetType mav_result, RetType[] declarators_result) {
    return forJExpressionOnly(that);
  }

  public RetType forVariableDeclaratorOnly(VariableDeclarator that, RetType type_result, RetType name_result) {
    return forJExpressionOnly(that);
  }

  public RetType forUninitializedVariableDeclaratorOnly(UninitializedVariableDeclarator that, RetType type_result, RetType name_result) {
    return forVariableDeclaratorOnly(that, type_result, name_result);
  }

  public RetType forInitializedVariableDeclaratorOnly(InitializedVariableDeclarator that, RetType type_result, RetType name_result, RetType initializer_result) {
    return forVariableDeclaratorOnly(that, type_result, name_result);
  }

  public RetType forTypeParameterOnly(TypeParameter that, RetType variable_result, RetType bound_result) {
    return forJExpressionOnly(that);
  }

  public RetType forArrayInitializerOnly(ArrayInitializer that, RetType[] items_result) {
    return forJExpressionOnly(that);
  }

  public RetType forTypeOnly(Type that) {
    return forJExpressionOnly(that);
  }

  public RetType forPrimitiveTypeOnly(PrimitiveType that) {
    return forTypeOnly(that);
  }

  public RetType forArrayTypeOnly(ArrayType that, RetType elementType_result) {
    return forTypeOnly(that);
  }

  public RetType forReferenceTypeOnly(ReferenceType that) {
    return forTypeOnly(that);
  }

  public RetType forMemberTypeOnly(MemberType that, RetType left_result, RetType right_result) {
    return forReferenceTypeOnly(that);
  }

  public RetType forClassOrInterfaceTypeOnly(ClassOrInterfaceType that, RetType[] typeArguments_result) {
    return forReferenceTypeOnly(that);
  }

  public RetType forTypeVariableOnly(TypeVariable that) {
    return forReferenceTypeOnly(that);
  }

  public RetType forVoidReturnOnly(VoidReturn that) {
    return forJExpressionOnly(that);
  }

  public RetType forSwitchCaseOnly(SwitchCase that, RetType code_result) {
    return forJExpressionOnly(that);
  }

  public RetType forLabeledCaseOnly(LabeledCase that, RetType label_result, RetType code_result) {
    return forSwitchCaseOnly(that, code_result);
  }

  public RetType forDefaultCaseOnly(DefaultCase that, RetType code_result) {
    return forSwitchCaseOnly(that, code_result);
  }

  public RetType forCatchBlockOnly(CatchBlock that, RetType exception_result, RetType block_result) {
    return forJExpressionOnly(that);
  }

  public RetType forExpressionOnly(Expression that) {
    return forJExpressionOnly(that);
  }

  public RetType forAssignmentExpressionOnly(AssignmentExpression that, RetType name_result, RetType value_result) {
    return forExpressionOnly(that);
  }

  public RetType forSimpleAssignmentExpressionOnly(SimpleAssignmentExpression that, RetType name_result, RetType value_result) {
    return forAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forPlusAssignmentExpressionOnly(PlusAssignmentExpression that, RetType name_result, RetType value_result) {
    return forAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forNumericAssignmentExpressionOnly(NumericAssignmentExpression that, RetType name_result, RetType value_result) {
    return forAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forMinusAssignmentExpressionOnly(MinusAssignmentExpression that, RetType name_result, RetType value_result) {
    return forNumericAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forMultiplyAssignmentExpressionOnly(MultiplyAssignmentExpression that, RetType name_result, RetType value_result) {
    return forNumericAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forDivideAssignmentExpressionOnly(DivideAssignmentExpression that, RetType name_result, RetType value_result) {
    return forNumericAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forModAssignmentExpressionOnly(ModAssignmentExpression that, RetType name_result, RetType value_result) {
    return forNumericAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forShiftAssignmentExpressionOnly(ShiftAssignmentExpression that, RetType name_result, RetType value_result) {
    return forAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forLeftShiftAssignmentExpressionOnly(LeftShiftAssignmentExpression that, RetType name_result, RetType value_result) {
    return forShiftAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forRightSignedShiftAssignmentExpressionOnly(RightSignedShiftAssignmentExpression that, RetType name_result, RetType value_result) {
    return forShiftAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forRightUnsignedShiftAssignmentExpressionOnly(RightUnsignedShiftAssignmentExpression that, RetType name_result, RetType value_result) {
    return forShiftAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forBitwiseAssignmentExpressionOnly(BitwiseAssignmentExpression that, RetType name_result, RetType value_result) {
    return forAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forBitwiseAndAssignmentExpressionOnly(BitwiseAndAssignmentExpression that, RetType name_result, RetType value_result) {
    return forBitwiseAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forBitwiseOrAssignmentExpressionOnly(BitwiseOrAssignmentExpression that, RetType name_result, RetType value_result) {
    return forBitwiseAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forBitwiseXorAssignmentExpressionOnly(BitwiseXorAssignmentExpression that, RetType name_result, RetType value_result) {
    return forBitwiseAssignmentExpressionOnly(that, name_result, value_result);
  }

  public RetType forBinaryExpressionOnly(BinaryExpression that, RetType left_result, RetType right_result) {
    return forExpressionOnly(that);
  }

  public RetType forBooleanExpressionOnly(BooleanExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forOrExpressionOnly(OrExpression that, RetType left_result, RetType right_result) {
    return forBooleanExpressionOnly(that, left_result, right_result);
  }

  public RetType forAndExpressionOnly(AndExpression that, RetType left_result, RetType right_result) {
    return forBooleanExpressionOnly(that, left_result, right_result);
  }

  public RetType forBitwiseBinaryExpressionOnly(BitwiseBinaryExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forBitwiseOrExpressionOnly(BitwiseOrExpression that, RetType left_result, RetType right_result) {
    return forBitwiseBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forBitwiseXorExpressionOnly(BitwiseXorExpression that, RetType left_result, RetType right_result) {
    return forBitwiseBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forBitwiseAndExpressionOnly(BitwiseAndExpression that, RetType left_result, RetType right_result) {
    return forBitwiseBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forEqualityExpressionOnly(EqualityExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forEqualsExpressionOnly(EqualsExpression that, RetType left_result, RetType right_result) {
    return forEqualityExpressionOnly(that, left_result, right_result);
  }

  public RetType forNotEqualExpressionOnly(NotEqualExpression that, RetType left_result, RetType right_result) {
    return forEqualityExpressionOnly(that, left_result, right_result);
  }

  public RetType forComparisonExpressionOnly(ComparisonExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forLessThanExpressionOnly(LessThanExpression that, RetType left_result, RetType right_result) {
    return forComparisonExpressionOnly(that, left_result, right_result);
  }

  public RetType forLessThanOrEqualExpressionOnly(LessThanOrEqualExpression that, RetType left_result, RetType right_result) {
    return forComparisonExpressionOnly(that, left_result, right_result);
  }

  public RetType forGreaterThanExpressionOnly(GreaterThanExpression that, RetType left_result, RetType right_result) {
    return forComparisonExpressionOnly(that, left_result, right_result);
  }

  public RetType forGreaterThanOrEqualExpressionOnly(GreaterThanOrEqualExpression that, RetType left_result, RetType right_result) {
    return forComparisonExpressionOnly(that, left_result, right_result);
  }

  public RetType forShiftBinaryExpressionOnly(ShiftBinaryExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forLeftShiftExpressionOnly(LeftShiftExpression that, RetType left_result, RetType right_result) {
    return forShiftBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forRightSignedShiftExpressionOnly(RightSignedShiftExpression that, RetType left_result, RetType right_result) {
    return forShiftBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forRightUnsignedShiftExpressionOnly(RightUnsignedShiftExpression that, RetType left_result, RetType right_result) {
    return forShiftBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forPlusExpressionOnly(PlusExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forNumericBinaryExpressionOnly(NumericBinaryExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forMinusExpressionOnly(MinusExpression that, RetType left_result, RetType right_result) {
    return forNumericBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forMultiplyExpressionOnly(MultiplyExpression that, RetType left_result, RetType right_result) {
    return forNumericBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forDivideExpressionOnly(DivideExpression that, RetType left_result, RetType right_result) {
    return forNumericBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forModExpressionOnly(ModExpression that, RetType left_result, RetType right_result) {
    return forNumericBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forNoOpExpressionOnly(NoOpExpression that, RetType left_result, RetType right_result) {
    return forBinaryExpressionOnly(that, left_result, right_result);
  }

  public RetType forUnaryExpressionOnly(UnaryExpression that, RetType value_result) {
    return forExpressionOnly(that);
  }

  public RetType forIncrementExpressionOnly(IncrementExpression that, RetType value_result) {
    return forUnaryExpressionOnly(that, value_result);
  }

  public RetType forPrefixIncrementExpressionOnly(PrefixIncrementExpression that, RetType value_result) {
    return forIncrementExpressionOnly(that, value_result);
  }

  public RetType forPositivePrefixIncrementExpressionOnly(PositivePrefixIncrementExpression that, RetType value_result) {
    return forPrefixIncrementExpressionOnly(that, value_result);
  }

  public RetType forNegativePrefixIncrementExpressionOnly(NegativePrefixIncrementExpression that, RetType value_result) {
    return forPrefixIncrementExpressionOnly(that, value_result);
  }

  public RetType forPostfixIncrementExpressionOnly(PostfixIncrementExpression that, RetType value_result) {
    return forIncrementExpressionOnly(that, value_result);
  }

  public RetType forPositivePostfixIncrementExpressionOnly(PositivePostfixIncrementExpression that, RetType value_result) {
    return forPostfixIncrementExpressionOnly(that, value_result);
  }

  public RetType forNegativePostfixIncrementExpressionOnly(NegativePostfixIncrementExpression that, RetType value_result) {
    return forPostfixIncrementExpressionOnly(that, value_result);
  }

  public RetType forNumericUnaryExpressionOnly(NumericUnaryExpression that, RetType value_result) {
    return forUnaryExpressionOnly(that, value_result);
  }

  public RetType forPositiveExpressionOnly(PositiveExpression that, RetType value_result) {
    return forNumericUnaryExpressionOnly(that, value_result);
  }

  public RetType forNegativeExpressionOnly(NegativeExpression that, RetType value_result) {
    return forNumericUnaryExpressionOnly(that, value_result);
  }

  public RetType forBitwiseNotExpressionOnly(BitwiseNotExpression that, RetType value_result) {
    return forUnaryExpressionOnly(that, value_result);
  }

  public RetType forNotExpressionOnly(NotExpression that, RetType value_result) {
    return forUnaryExpressionOnly(that, value_result);
  }

  public RetType forConditionalExpressionOnly(ConditionalExpression that, RetType condition_result, RetType forTrue_result, RetType forFalse_result) {
    return forExpressionOnly(that);
  }

  public RetType forInstanceofExpressionOnly(InstanceofExpression that, RetType value_result, RetType type_result) {
    return forExpressionOnly(that);
  }

  public RetType forCastExpressionOnly(CastExpression that, RetType type_result, RetType value_result) {
    return forExpressionOnly(that);
  }

  public RetType forPrimaryOnly(Primary that) {
    return forExpressionOnly(that);
  }

  public RetType forLexicalLiteralOnly(LexicalLiteral that) {
    return forPrimaryOnly(that);
  }

  public RetType forIntegerLiteralOnly(IntegerLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forLongLiteralOnly(LongLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forDoubleLiteralOnly(DoubleLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forFloatLiteralOnly(FloatLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forBooleanLiteralOnly(BooleanLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forCharLiteralOnly(CharLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forStringLiteralOnly(StringLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forNullLiteralOnly(NullLiteral that) {
    return forLexicalLiteralOnly(that);
  }

  public RetType forInstantiationOnly(Instantiation that) {
    return forPrimaryOnly(that);
  }

  public RetType forClassInstantiationOnly(ClassInstantiation that, RetType type_result, RetType arguments_result) {
    return forInstantiationOnly(that);
  }

  public RetType forNamedClassInstantiationOnly(NamedClassInstantiation that, RetType type_result, RetType arguments_result) {
    return forClassInstantiationOnly(that, type_result, arguments_result);
  }

  public RetType forSimpleNamedClassInstantiationOnly(SimpleNamedClassInstantiation that, RetType type_result, RetType arguments_result) {
    return forNamedClassInstantiationOnly(that, type_result, arguments_result);
  }

  public RetType forComplexNamedClassInstantiationOnly(ComplexNamedClassInstantiation that, RetType enclosing_result, RetType type_result, RetType arguments_result) {
    return forNamedClassInstantiationOnly(that, type_result, arguments_result);
  }

  public RetType forAnonymousClassInstantiationOnly(AnonymousClassInstantiation that, RetType type_result, RetType arguments_result, RetType body_result) {
    return forClassInstantiationOnly(that, type_result, arguments_result);
  }

  public RetType forSimpleAnonymousClassInstantiationOnly(SimpleAnonymousClassInstantiation that, RetType type_result, RetType arguments_result, RetType body_result) {
    return forAnonymousClassInstantiationOnly(that, type_result, arguments_result, body_result);
  }

  public RetType forComplexAnonymousClassInstantiationOnly(ComplexAnonymousClassInstantiation that, RetType enclosing_result, RetType type_result, RetType arguments_result, RetType body_result) {
    return forAnonymousClassInstantiationOnly(that, type_result, arguments_result, body_result);
  }

  public RetType forArrayInstantiationOnly(ArrayInstantiation that, RetType type_result) {
    return forInstantiationOnly(that);
  }

  public RetType forUninitializedArrayInstantiationOnly(UninitializedArrayInstantiation that, RetType type_result, RetType dimensionSizes_result) {
    return forArrayInstantiationOnly(that, type_result);
  }

  public RetType forSimpleUninitializedArrayInstantiationOnly(SimpleUninitializedArrayInstantiation that, RetType type_result, RetType dimensionSizes_result) {
    return forUninitializedArrayInstantiationOnly(that, type_result, dimensionSizes_result);
  }

  public RetType forComplexUninitializedArrayInstantiationOnly(ComplexUninitializedArrayInstantiation that, RetType enclosing_result, RetType type_result, RetType dimensionSizes_result) {
    return forUninitializedArrayInstantiationOnly(that, type_result, dimensionSizes_result);
  }

  public RetType forInitializedArrayInstantiationOnly(InitializedArrayInstantiation that, RetType type_result, RetType initializer_result) {
    return forArrayInstantiationOnly(that, type_result);
  }

  public RetType forSimpleInitializedArrayInstantiationOnly(SimpleInitializedArrayInstantiation that, RetType type_result, RetType initializer_result) {
    return forInitializedArrayInstantiationOnly(that, type_result, initializer_result);
  }

  public RetType forComplexInitializedArrayInstantiationOnly(ComplexInitializedArrayInstantiation that, RetType enclosing_result, RetType type_result, RetType initializer_result) {
    return forInitializedArrayInstantiationOnly(that, type_result, initializer_result);
  }

  public RetType forVariableReferenceOnly(VariableReference that) {
    return forPrimaryOnly(that);
  }

  public RetType forNameReferenceOnly(NameReference that, RetType name_result) {
    return forVariableReferenceOnly(that);
  }

  public RetType forSimpleNameReferenceOnly(SimpleNameReference that, RetType name_result) {
    return forNameReferenceOnly(that, name_result);
  }

  public RetType forComplexNameReferenceOnly(ComplexNameReference that, RetType enclosing_result, RetType name_result) {
    return forNameReferenceOnly(that, name_result);
  }

  public RetType forThisReferenceOnly(ThisReference that) {
    return forVariableReferenceOnly(that);
  }

  public RetType forSimpleThisReferenceOnly(SimpleThisReference that) {
    return forThisReferenceOnly(that);
  }

  public RetType forComplexThisReferenceOnly(ComplexThisReference that, RetType enclosing_result) {
    return forThisReferenceOnly(that);
  }

  public RetType forSuperReferenceOnly(SuperReference that) {
    return forVariableReferenceOnly(that);
  }

  public RetType forSimpleSuperReferenceOnly(SimpleSuperReference that) {
    return forSuperReferenceOnly(that);
  }

  public RetType forComplexSuperReferenceOnly(ComplexSuperReference that, RetType enclosing_result) {
    return forSuperReferenceOnly(that);
  }

  public RetType forFunctionInvocationOnly(FunctionInvocation that, RetType arguments_result) {
    return forPrimaryOnly(that);
  }

  public RetType forMethodInvocationOnly(MethodInvocation that, RetType arguments_result, RetType name_result) {
    return forFunctionInvocationOnly(that, arguments_result);
  }

  public RetType forSimpleMethodInvocationOnly(SimpleMethodInvocation that, RetType name_result, RetType arguments_result) {
    return forMethodInvocationOnly(that, arguments_result, name_result);
  }

  public RetType forComplexMethodInvocationOnly(ComplexMethodInvocation that, RetType enclosing_result, RetType name_result, RetType arguments_result) {
    return forMethodInvocationOnly(that, arguments_result, name_result);
  }

  public RetType forThisConstructorInvocationOnly(ThisConstructorInvocation that, RetType arguments_result) {
    return forFunctionInvocationOnly(that, arguments_result);
  }

  public RetType forSimpleThisConstructorInvocationOnly(SimpleThisConstructorInvocation that, RetType arguments_result) {
    return forThisConstructorInvocationOnly(that, arguments_result);
  }

  public RetType forComplexThisConstructorInvocationOnly(ComplexThisConstructorInvocation that, RetType enclosing_result, RetType arguments_result) {
    return forThisConstructorInvocationOnly(that, arguments_result);
  }

  public RetType forSuperConstructorInvocationOnly(SuperConstructorInvocation that, RetType arguments_result) {
    return forFunctionInvocationOnly(that, arguments_result);
  }

  public RetType forSimpleSuperConstructorInvocationOnly(SimpleSuperConstructorInvocation that, RetType arguments_result) {
    return forSuperConstructorInvocationOnly(that, arguments_result);
  }

  public RetType forComplexSuperConstructorInvocationOnly(ComplexSuperConstructorInvocation that, RetType enclosing_result, RetType arguments_result) {
    return forSuperConstructorInvocationOnly(that, arguments_result);
  }

  public RetType forClassLiteralOnly(ClassLiteral that, RetType type_result) {
    return forPrimaryOnly(that);
  }

  public RetType forArrayAccessOnly(ArrayAccess that, RetType array_result, RetType index_result) {
    return forPrimaryOnly(that);
  }

  public RetType forParenthesizedOnly(Parenthesized that, RetType value_result) {
    return forPrimaryOnly(that);
  }

  public RetType forEmptyExpressionOnly(EmptyExpression that) {
    return forPrimaryOnly(that);
  }

  public RetType forBodyOnly(Body that, RetType[] statements_result) {
    return forJExpressionOnly(that);
  }

  public RetType forBracedBodyOnly(BracedBody that, RetType[] statements_result) {
    return forBodyOnly(that, statements_result);
  }

  public RetType forUnbracedBodyOnly(UnbracedBody that, RetType[] statements_result) {
    return forBodyOnly(that, statements_result);
  }

  public RetType forExpressionListOnly(ExpressionList that, RetType[] expressions_result) {
    return forJExpressionOnly(that);
  }

  public RetType forParenthesizedExpressionListOnly(ParenthesizedExpressionList that, RetType[] expressions_result) {
    return forExpressionListOnly(that, expressions_result);
  }

  public RetType forUnparenthesizedExpressionListOnly(UnparenthesizedExpressionList that, RetType[] expressions_result) {
    return forExpressionListOnly(that, expressions_result);
  }

  public RetType forDimensionExpressionListOnly(DimensionExpressionList that, RetType[] expressions_result) {
    return forExpressionListOnly(that, expressions_result);
  }

  public RetType forEmptyForConditionOnly(EmptyForCondition that) {
    return forJExpressionOnly(that);
  }


  /** Implementation of JExpressionIFVisitor methods to implement depth-first traversal. */
  public RetType forSourceFile(SourceFile that) {
    final RetType[] packageStatements_result = makeArrayOfRetType(that.getPackageStatements().length);
    for (int i = 0; i < that.getPackageStatements().length; i++) {
      packageStatements_result[i] = that.getPackageStatements()[i].visit(this);
    }
    final RetType[] importStatements_result = makeArrayOfRetType(that.getImportStatements().length);
    for (int i = 0; i < that.getImportStatements().length; i++) {
      importStatements_result[i] = that.getImportStatements()[i].visit(this);
    }
    final RetType[] types_result = makeArrayOfRetType(that.getTypes().length);
    for (int i = 0; i < that.getTypes().length; i++) {
      types_result[i] = that.getTypes()[i].visit(this);
    }
    return forSourceFileOnly(that, packageStatements_result, importStatements_result, types_result);
  }
  
  public RetType forModifiersAndVisibility(ModifiersAndVisibility that) {
    return forModifiersAndVisibilityOnly(that);
  }
  
  public RetType forCompoundWord(CompoundWord that) {
    final RetType[] words_result = makeArrayOfRetType(that.getWords().length);
    for (int i = 0; i < that.getWords().length; i++) {
      words_result[i] = that.getWords()[i].visit(this);
    }
    return forCompoundWordOnly(that, words_result);
  }
  
  public RetType forWord(Word that) {
    return forWordOnly(that);
  }
  
  public RetType forClassDef(ClassDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] typeParameters_result = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = that.getTypeParameters()[i].visit(this);
    }
    final RetType superclass_result = that.getSuperclass().visit(this);
    final RetType[] interfaces_result = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = that.getInterfaces()[i].visit(this);
    }
    final RetType body_result = that.getBody().visit(this);
    return forClassDefOnly(that, mav_result, name_result, typeParameters_result, superclass_result, interfaces_result, body_result);
  }
  
  public RetType forInnerClassDef(InnerClassDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] typeParameters_result = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = that.getTypeParameters()[i].visit(this);
    }
    final RetType superclass_result = that.getSuperclass().visit(this);
    final RetType[] interfaces_result = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = that.getInterfaces()[i].visit(this);
    }
    final RetType body_result = that.getBody().visit(this);
    return forInnerClassDefOnly(that, mav_result, name_result, typeParameters_result, superclass_result, interfaces_result, body_result);
  }
  
  public RetType forInterfaceDef(InterfaceDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] typeParameters_result = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = that.getTypeParameters()[i].visit(this);
    }
    final RetType[] interfaces_result = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = that.getInterfaces()[i].visit(this);
    }
    final RetType body_result = that.getBody().visit(this);
    return forInterfaceDefOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }
  
  public RetType forInnerInterfaceDef(InnerInterfaceDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] typeParameters_result = makeArrayOfRetType(that.getTypeParameters().length);
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = that.getTypeParameters()[i].visit(this);
    }
    final RetType[] interfaces_result = makeArrayOfRetType(that.getInterfaces().length);
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = that.getInterfaces()[i].visit(this);
    }
    final RetType body_result = that.getBody().visit(this);
    return forInnerInterfaceDefOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }
  
  public RetType forConstructorDef(ConstructorDef that) {
    final RetType name_result = that.getName().visit(this);
    final RetType mav_result = that.getMav().visit(this);
    final RetType[] parameters_result = makeArrayOfRetType(that.getParameters().length);
    for (int i = 0; i < that.getParameters().length; i++) {
      parameters_result[i] = that.getParameters()[i].visit(this);
    }
    final RetType[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = that.getThrows()[i].visit(this);
    }
    final RetType statements_result = that.getStatements().visit(this);
    return forConstructorDefOnly(that, name_result, mav_result, parameters_result, throws_result, statements_result);
  }
  
  public RetType forInstanceInitializer(InstanceInitializer that) {
    final RetType code_result = that.getCode().visit(this);
    return forInstanceInitializerOnly(that, code_result);
  }
  
  public RetType forStaticInitializer(StaticInitializer that) {
    final RetType code_result = that.getCode().visit(this);
    return forStaticInitializerOnly(that, code_result);
  }
  
  public RetType forPackageStatement(PackageStatement that) {
    final RetType cWord_result = that.getCWord().visit(this);
    return forPackageStatementOnly(that, cWord_result);
  }
  
  public RetType forClassImportStatement(ClassImportStatement that) {
    final RetType cWord_result = that.getCWord().visit(this);
    return forClassImportStatementOnly(that, cWord_result);
  }
  
  public RetType forPackageImportStatement(PackageImportStatement that) {
    final RetType cWord_result = that.getCWord().visit(this);
    return forPackageImportStatementOnly(that, cWord_result);
  }
  
  public RetType forLabeledStatement(LabeledStatement that) {
    final RetType label_result = that.getLabel().visit(this);
    final RetType statement_result = that.getStatement().visit(this);
    return forLabeledStatementOnly(that, label_result, statement_result);
  }
  
  public RetType forBlock(Block that) {
    final RetType statements_result = that.getStatements().visit(this);
    return forBlockOnly(that, statements_result);
  }
  
  public RetType forExpressionStatement(ExpressionStatement that) {
    final RetType expression_result = that.getExpression().visit(this);
    return forExpressionStatementOnly(that, expression_result);
  }
  
  public RetType forSwitchStatement(SwitchStatement that) {
    final RetType test_result = that.getTest().visit(this);
    final RetType[] cases_result = makeArrayOfRetType(that.getCases().length);
    for (int i = 0; i < that.getCases().length; i++) {
      cases_result[i] = that.getCases()[i].visit(this);
    }
    return forSwitchStatementOnly(that, test_result, cases_result);
  }
  
  public RetType forIfThenStatement(IfThenStatement that) {
    final RetType testExpression_result = that.getTestExpression().visit(this);
    final RetType thenStatement_result = that.getThenStatement().visit(this);
    return forIfThenStatementOnly(that, testExpression_result, thenStatement_result);
  }
  
  public RetType forIfThenElseStatement(IfThenElseStatement that) {
    final RetType testExpression_result = that.getTestExpression().visit(this);
    final RetType thenStatement_result = that.getThenStatement().visit(this);
    final RetType elseStatement_result = that.getElseStatement().visit(this);
    return forIfThenElseStatementOnly(that, testExpression_result, thenStatement_result, elseStatement_result);
  }
  
  public RetType forWhileStatement(WhileStatement that) {
    final RetType condition_result = that.getCondition().visit(this);
    final RetType code_result = that.getCode().visit(this);
    return forWhileStatementOnly(that, condition_result, code_result);
  }
  
  public RetType forDoStatement(DoStatement that) {
    final RetType code_result = that.getCode().visit(this);
    final RetType condition_result = that.getCondition().visit(this);
    return forDoStatementOnly(that, code_result, condition_result);
  }
  
  public RetType forForStatement(ForStatement that) {
    final RetType init_result = that.getInit().visit(this);
    final RetType condition_result = that.getCondition().visit(this);
    final RetType update_result = that.getUpdate().visit(this);
    final RetType code_result = that.getCode().visit(this);
    return forForStatementOnly(that, init_result, condition_result, update_result, code_result);
  }
  
  public RetType forLabeledBreakStatement(LabeledBreakStatement that) {
    final RetType label_result = that.getLabel().visit(this);
    return forLabeledBreakStatementOnly(that, label_result);
  }
  
  public RetType forUnlabeledBreakStatement(UnlabeledBreakStatement that) {
    return forUnlabeledBreakStatementOnly(that);
  }
  
  public RetType forLabeledContinueStatement(LabeledContinueStatement that) {
    final RetType label_result = that.getLabel().visit(this);
    return forLabeledContinueStatementOnly(that, label_result);
  }
  
  public RetType forUnlabeledContinueStatement(UnlabeledContinueStatement that) {
    return forUnlabeledContinueStatementOnly(that);
  }
  
  public RetType forVoidReturnStatement(VoidReturnStatement that) {
    return forVoidReturnStatementOnly(that);
  }
  
  public RetType forValueReturnStatement(ValueReturnStatement that) {
    final RetType value_result = that.getValue().visit(this);
    return forValueReturnStatementOnly(that, value_result);
  }
  
  public RetType forThrowStatement(ThrowStatement that) {
    final RetType thrown_result = that.getThrown().visit(this);
    return forThrowStatementOnly(that, thrown_result);
  }
  
  public RetType forSynchronizedStatement(SynchronizedStatement that) {
    final RetType lockExpr_result = that.getLockExpr().visit(this);
    final RetType block_result = that.getBlock().visit(this);
    return forSynchronizedStatementOnly(that, lockExpr_result, block_result);
  }
  
  public RetType forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    final RetType tryBlock_result = that.getTryBlock().visit(this);
    final RetType[] catchBlocks_result = makeArrayOfRetType(that.getCatchBlocks().length);
    for (int i = 0; i < that.getCatchBlocks().length; i++) {
      catchBlocks_result[i] = that.getCatchBlocks()[i].visit(this);
    }
    final RetType finallyBlock_result = that.getFinallyBlock().visit(this);
    return forTryCatchFinallyStatementOnly(that, tryBlock_result, catchBlocks_result, finallyBlock_result);
  }
  
  public RetType forNormalTryCatchStatement(NormalTryCatchStatement that) {
    final RetType tryBlock_result = that.getTryBlock().visit(this);
    final RetType[] catchBlocks_result = makeArrayOfRetType(that.getCatchBlocks().length);
    for (int i = 0; i < that.getCatchBlocks().length; i++) {
      catchBlocks_result[i] = that.getCatchBlocks()[i].visit(this);
    }
    return forNormalTryCatchStatementOnly(that, tryBlock_result, catchBlocks_result);
  }
  
  public RetType forEmptyStatement(EmptyStatement that) {
    return forEmptyStatementOnly(that);
  }
  
  public RetType forConcreteMethodDef(ConcreteMethodDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType[] typeParams_result = makeArrayOfRetType(that.getTypeParams().length);
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = that.getTypeParams()[i].visit(this);
    }
    final RetType result_result = that.getResult().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] params_result = makeArrayOfRetType(that.getParams().length);
    for (int i = 0; i < that.getParams().length; i++) {
      params_result[i] = that.getParams()[i].visit(this);
    }
    final RetType[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = that.getThrows()[i].visit(this);
    }
    final RetType body_result = that.getBody().visit(this);
    return forConcreteMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result, body_result);
  }
  
  public RetType forAbstractMethodDef(AbstractMethodDef that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType[] typeParams_result = makeArrayOfRetType(that.getTypeParams().length);
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = that.getTypeParams()[i].visit(this);
    }
    final RetType result_result = that.getResult().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType[] params_result = makeArrayOfRetType(that.getParams().length);
    for (int i = 0; i < that.getParams().length; i++) {
      params_result[i] = that.getParams()[i].visit(this);
    }
    final RetType[] throws_result = makeArrayOfRetType(that.getThrows().length);
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = that.getThrows()[i].visit(this);
    }
    return forAbstractMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result);
  }
  
  public RetType forFormalParameter(FormalParameter that) {
    final RetType declarator_result = that.getDeclarator().visit(this);
    return forFormalParameterOnly(that, declarator_result);
  }
  
  public RetType forVariableDeclaration(VariableDeclaration that) {
    final RetType mav_result = that.getMav().visit(this);
    final RetType[] declarators_result = makeArrayOfRetType(that.getDeclarators().length);
    for (int i = 0; i < that.getDeclarators().length; i++) {
      declarators_result[i] = that.getDeclarators()[i].visit(this);
    }
    return forVariableDeclarationOnly(that, mav_result, declarators_result);
  }
  
  public RetType forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    final RetType type_result = that.getType().visit(this);
    final RetType name_result = that.getName().visit(this);
    return forUninitializedVariableDeclaratorOnly(that, type_result, name_result);
  }
  
  public RetType forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    final RetType type_result = that.getType().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType initializer_result = that.getInitializer().visit(this);
    return forInitializedVariableDeclaratorOnly(that, type_result, name_result, initializer_result);
  }
  
  public RetType forTypeParameter(TypeParameter that) {
    final RetType variable_result = that.getVariable().visit(this);
    final RetType bound_result = that.getBound().visit(this);
    return forTypeParameterOnly(that, variable_result, bound_result);
  }
  
  public RetType forArrayInitializer(ArrayInitializer that) {
    final RetType[] items_result = makeArrayOfRetType(that.getItems().length);
    for (int i = 0; i < that.getItems().length; i++) {
      items_result[i] = that.getItems()[i].visit(this);
    }
    return forArrayInitializerOnly(that, items_result);
  }
  
  public RetType forPrimitiveType(PrimitiveType that) {
    return forPrimitiveTypeOnly(that);
  }
  
  public RetType forArrayType(ArrayType that) {
    final RetType elementType_result = that.getElementType().visit(this);
    return forArrayTypeOnly(that, elementType_result);
  }
  
  public RetType forMemberType(MemberType that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forMemberTypeOnly(that, left_result, right_result);
  }
  
  public RetType forClassOrInterfaceType(ClassOrInterfaceType that) {
    final RetType[] typeArguments_result = makeArrayOfRetType(that.getTypeArguments().length);
    for (int i = 0; i < that.getTypeArguments().length; i++) {
      typeArguments_result[i] = that.getTypeArguments()[i].visit(this);
    }
    return forClassOrInterfaceTypeOnly(that, typeArguments_result);
  }
  
  public RetType forTypeVariable(TypeVariable that) {
    return forTypeVariableOnly(that);
  }
  
  public RetType forVoidReturn(VoidReturn that) {
    return forVoidReturnOnly(that);
  }
  
  public RetType forLabeledCase(LabeledCase that) {
    final RetType label_result = that.getLabel().visit(this);
    final RetType code_result = that.getCode().visit(this);
    return forLabeledCaseOnly(that, label_result, code_result);
  }
  
  public RetType forDefaultCase(DefaultCase that) {
    final RetType code_result = that.getCode().visit(this);
    return forDefaultCaseOnly(that, code_result);
  }
  
  public RetType forCatchBlock(CatchBlock that) {
    final RetType exception_result = that.getException().visit(this);
    final RetType block_result = that.getBlock().visit(this);
    return forCatchBlockOnly(that, exception_result, block_result);
  }
  
  public RetType forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forSimpleAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forPlusAssignmentExpression(PlusAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forPlusAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forMinusAssignmentExpression(MinusAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forMinusAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forMultiplyAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forDivideAssignmentExpression(DivideAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forDivideAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forModAssignmentExpression(ModAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forModAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forLeftShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forRightSignedShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forRightUnsignedShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forBitwiseAndAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forBitwiseOrAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that) {
    final RetType name_result = that.getName().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forBitwiseXorAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public RetType forOrExpression(OrExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forOrExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forAndExpression(AndExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forAndExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forBitwiseOrExpression(BitwiseOrExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forBitwiseOrExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forBitwiseXorExpression(BitwiseXorExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forBitwiseXorExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forBitwiseAndExpression(BitwiseAndExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forBitwiseAndExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forEqualsExpression(EqualsExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forEqualsExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forNotEqualExpression(NotEqualExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forNotEqualExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forLessThanExpression(LessThanExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forLessThanExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forLessThanOrEqualExpression(LessThanOrEqualExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forLessThanOrEqualExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forGreaterThanExpression(GreaterThanExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forGreaterThanExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forGreaterThanOrEqualExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forLeftShiftExpression(LeftShiftExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forLeftShiftExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forRightSignedShiftExpression(RightSignedShiftExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forRightSignedShiftExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forRightUnsignedShiftExpression(RightUnsignedShiftExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forRightUnsignedShiftExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forPlusExpression(PlusExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forPlusExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forMinusExpression(MinusExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forMinusExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forMultiplyExpression(MultiplyExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forMultiplyExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forDivideExpression(DivideExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forDivideExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forModExpression(ModExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forModExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forNoOpExpression(NoOpExpression that) {
    final RetType left_result = that.getLeft().visit(this);
    final RetType right_result = that.getRight().visit(this);
    return forNoOpExpressionOnly(that, left_result, right_result);
  }
  
  public RetType forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forPositivePrefixIncrementExpressionOnly(that, value_result);
  }
  
  public RetType forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forNegativePrefixIncrementExpressionOnly(that, value_result);
  }
  
  public RetType forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forPositivePostfixIncrementExpressionOnly(that, value_result);
  }
  
  public RetType forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forNegativePostfixIncrementExpressionOnly(that, value_result);
  }
  
  public RetType forPositiveExpression(PositiveExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forPositiveExpressionOnly(that, value_result);
  }
  
  public RetType forNegativeExpression(NegativeExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forNegativeExpressionOnly(that, value_result);
  }
  
  public RetType forBitwiseNotExpression(BitwiseNotExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forBitwiseNotExpressionOnly(that, value_result);
  }
  
  public RetType forNotExpression(NotExpression that) {
    final RetType value_result = that.getValue().visit(this);
    return forNotExpressionOnly(that, value_result);
  }
  
  public RetType forConditionalExpression(ConditionalExpression that) {
    final RetType condition_result = that.getCondition().visit(this);
    final RetType forTrue_result = that.getForTrue().visit(this);
    final RetType forFalse_result = that.getForFalse().visit(this);
    return forConditionalExpressionOnly(that, condition_result, forTrue_result, forFalse_result);
  }
  
  public RetType forInstanceofExpression(InstanceofExpression that) {
    final RetType value_result = that.getValue().visit(this);
    final RetType type_result = that.getType().visit(this);
    return forInstanceofExpressionOnly(that, value_result, type_result);
  }
  
  public RetType forCastExpression(CastExpression that) {
    final RetType type_result = that.getType().visit(this);
    final RetType value_result = that.getValue().visit(this);
    return forCastExpressionOnly(that, type_result, value_result);
  }
  
  public RetType forIntegerLiteral(IntegerLiteral that) {
    return forIntegerLiteralOnly(that);
  }
  
  public RetType forLongLiteral(LongLiteral that) {
    return forLongLiteralOnly(that);
  }
  
  public RetType forDoubleLiteral(DoubleLiteral that) {
    return forDoubleLiteralOnly(that);
  }
  
  public RetType forFloatLiteral(FloatLiteral that) {
    return forFloatLiteralOnly(that);
  }
  
  public RetType forBooleanLiteral(BooleanLiteral that) {
    return forBooleanLiteralOnly(that);
  }
  
  public RetType forCharLiteral(CharLiteral that) {
    return forCharLiteralOnly(that);
  }
  
  public RetType forStringLiteral(StringLiteral that) {
    return forStringLiteralOnly(that);
  }
  
  public RetType forNullLiteral(NullLiteral that) {
    return forNullLiteralOnly(that);
  }
  
  public RetType forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    final RetType type_result = that.getType().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forSimpleNamedClassInstantiationOnly(that, type_result, arguments_result);
  }
  
  public RetType forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType type_result = that.getType().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forComplexNamedClassInstantiationOnly(that, enclosing_result, type_result, arguments_result);
  }
  
  public RetType forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    final RetType type_result = that.getType().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    final RetType body_result = that.getBody().visit(this);
    return forSimpleAnonymousClassInstantiationOnly(that, type_result, arguments_result, body_result);
  }
  
  public RetType forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType type_result = that.getType().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    final RetType body_result = that.getBody().visit(this);
    return forComplexAnonymousClassInstantiationOnly(that, enclosing_result, type_result, arguments_result, body_result);
  }
  
  public RetType forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    final RetType type_result = that.getType().visit(this);
    final RetType dimensionSizes_result = that.getDimensionSizes().visit(this);
    return forSimpleUninitializedArrayInstantiationOnly(that, type_result, dimensionSizes_result);
  }
  
  public RetType forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType type_result = that.getType().visit(this);
    final RetType dimensionSizes_result = that.getDimensionSizes().visit(this);
    return forComplexUninitializedArrayInstantiationOnly(that, enclosing_result, type_result, dimensionSizes_result);
  }
  
  public RetType forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    final RetType type_result = that.getType().visit(this);
    final RetType initializer_result = that.getInitializer().visit(this);
    return forSimpleInitializedArrayInstantiationOnly(that, type_result, initializer_result);
  }
  
  public RetType forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType type_result = that.getType().visit(this);
    final RetType initializer_result = that.getInitializer().visit(this);
    return forComplexInitializedArrayInstantiationOnly(that, enclosing_result, type_result, initializer_result);
  }
  
  public RetType forSimpleNameReference(SimpleNameReference that) {
    final RetType name_result = that.getName().visit(this);
    return forSimpleNameReferenceOnly(that, name_result);
  }
  
  public RetType forComplexNameReference(ComplexNameReference that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType name_result = that.getName().visit(this);
    return forComplexNameReferenceOnly(that, enclosing_result, name_result);
  }
  
  public RetType forSimpleThisReference(SimpleThisReference that) {
    return forSimpleThisReferenceOnly(that);
  }
  
  public RetType forComplexThisReference(ComplexThisReference that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    return forComplexThisReferenceOnly(that, enclosing_result);
  }
  
  public RetType forSimpleSuperReference(SimpleSuperReference that) {
    return forSimpleSuperReferenceOnly(that);
  }
  
  public RetType forComplexSuperReference(ComplexSuperReference that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    return forComplexSuperReferenceOnly(that, enclosing_result);
  }
  
  public RetType forSimpleMethodInvocation(SimpleMethodInvocation that) {
    final RetType name_result = that.getName().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forSimpleMethodInvocationOnly(that, name_result, arguments_result);
  }
  
  public RetType forComplexMethodInvocation(ComplexMethodInvocation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType name_result = that.getName().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forComplexMethodInvocationOnly(that, enclosing_result, name_result, arguments_result);
  }
  
  public RetType forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    final RetType arguments_result = that.getArguments().visit(this);
    return forSimpleThisConstructorInvocationOnly(that, arguments_result);
  }
  
  public RetType forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forComplexThisConstructorInvocationOnly(that, enclosing_result, arguments_result);
  }
  
  public RetType forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that) {
    final RetType arguments_result = that.getArguments().visit(this);
    return forSimpleSuperConstructorInvocationOnly(that, arguments_result);
  }
  
  public RetType forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that) {
    final RetType enclosing_result = that.getEnclosing().visit(this);
    final RetType arguments_result = that.getArguments().visit(this);
    return forComplexSuperConstructorInvocationOnly(that, enclosing_result, arguments_result);
  }
  
  public RetType forClassLiteral(ClassLiteral that) {
    final RetType type_result = that.getType().visit(this);
    return forClassLiteralOnly(that, type_result);
  }
  
  public RetType forArrayAccess(ArrayAccess that) {
    final RetType array_result = that.getArray().visit(this);
    final RetType index_result = that.getIndex().visit(this);
    return forArrayAccessOnly(that, array_result, index_result);
  }
  
  public RetType forParenthesized(Parenthesized that) {
    final RetType value_result = that.getValue().visit(this);
    return forParenthesizedOnly(that, value_result);
  }
  
  public RetType forEmptyExpression(EmptyExpression that) {
    return forEmptyExpressionOnly(that);
  }
  
  public RetType forBracedBody(BracedBody that) {
    final RetType[] statements_result = makeArrayOfRetType(that.getStatements().length);
    for (int i = 0; i < that.getStatements().length; i++) {
      statements_result[i] = that.getStatements()[i].visit(this);
    }
    return forBracedBodyOnly(that, statements_result);
  }
  
  public RetType forUnbracedBody(UnbracedBody that) {
    final RetType[] statements_result = makeArrayOfRetType(that.getStatements().length);
    for (int i = 0; i < that.getStatements().length; i++) {
      statements_result[i] = that.getStatements()[i].visit(this);
    }
    return forUnbracedBodyOnly(that, statements_result);
  }
  
  public RetType forParenthesizedExpressionList(ParenthesizedExpressionList that) {
    final RetType[] expressions_result = makeArrayOfRetType(that.getExpressions().length);
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = that.getExpressions()[i].visit(this);
    }
    return forParenthesizedExpressionListOnly(that, expressions_result);
  }
  
  public RetType forUnparenthesizedExpressionList(UnparenthesizedExpressionList that) {
    final RetType[] expressions_result = makeArrayOfRetType(that.getExpressions().length);
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = that.getExpressions()[i].visit(this);
    }
    return forUnparenthesizedExpressionListOnly(that, expressions_result);
  }
  
  public RetType forDimensionExpressionList(DimensionExpressionList that) {
    final RetType[] expressions_result = makeArrayOfRetType(that.getExpressions().length);
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = that.getExpressions()[i].visit(this);
    }
    return forDimensionExpressionListOnly(that, expressions_result);
  }
  
  public RetType forEmptyForCondition(EmptyForCondition that) {
    return forEmptyForConditionOnly(that);
  }
  
}
