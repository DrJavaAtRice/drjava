package edu.rice.cs.javalanglevels.tree;

/** A parametric abstract implementation of a visitor over JExpressionIF that return a value.
 ** This visitor implements the visitor interface with methods that 
 ** return the value of the defaultCase.  These methods can be overriden 
 ** in order to achieve different behavior for particular cases.
 **/
public abstract class JExpressionIFAbstractVisitor<RetType> implements JExpressionIFVisitor<RetType> {
  /**
   * This method is run for all cases by default, unless they are overridden by subclasses.
  **/
  protected abstract RetType defaultCase(JExpressionIF that);

  /* Methods to visit an item. */
  public RetType forJExpression(JExpression that) {
    return defaultCase(that);
  }

  public RetType forSourceFile(SourceFile that) {
    return forJExpression(that);
  }

  public RetType forModifiersAndVisibility(ModifiersAndVisibility that) {
    return forJExpression(that);
  }

  public RetType forCompoundWord(CompoundWord that) {
    return forJExpression(that);
  }

  public RetType forWord(Word that) {
    return forJExpression(that);
  }

  public RetType forTypeDefBase(TypeDefBase that) {
    return forJExpression(that);
  }

  public RetType forClassDef(ClassDef that) {
    return forTypeDefBase(that);
  }

  public RetType forInnerClassDef(InnerClassDef that) {
    return forClassDef(that);
  }

  public RetType forInterfaceDef(InterfaceDef that) {
    return forTypeDefBase(that);
  }

  public RetType forInnerInterfaceDef(InnerInterfaceDef that) {
    return forInterfaceDef(that);
  }

  public RetType forConstructorDef(ConstructorDef that) {
    return forJExpression(that);
  }

  public RetType forInitializer(Initializer that) {
    return forJExpression(that);
  }

  public RetType forInstanceInitializer(InstanceInitializer that) {
    return forInitializer(that);
  }

  public RetType forStaticInitializer(StaticInitializer that) {
    return forInitializer(that);
  }

  public RetType forPackageStatement(PackageStatement that) {
    return forJExpression(that);
  }

  public RetType forImportStatement(ImportStatement that) {
    return forJExpression(that);
  }

  public RetType forClassImportStatement(ClassImportStatement that) {
    return forImportStatement(that);
  }

  public RetType forPackageImportStatement(PackageImportStatement that) {
    return forImportStatement(that);
  }

  public RetType forStatement(Statement that) {
    return forJExpression(that);
  }

  public RetType forLabeledStatement(LabeledStatement that) {
    return forStatement(that);
  }

  public RetType forBlock(Block that) {
    return forStatement(that);
  }

  public RetType forExpressionStatement(ExpressionStatement that) {
    return forStatement(that);
  }

  public RetType forSwitchStatement(SwitchStatement that) {
    return forStatement(that);
  }

  public RetType forIfThenStatement(IfThenStatement that) {
    return forStatement(that);
  }

  public RetType forIfThenElseStatement(IfThenElseStatement that) {
    return forIfThenStatement(that);
  }

  public RetType forWhileStatement(WhileStatement that) {
    return forStatement(that);
  }

  public RetType forDoStatement(DoStatement that) {
    return forStatement(that);
  }

  public RetType forForStatement(ForStatement that) {
    return forStatement(that);
  }

  public RetType forBreakStatement(BreakStatement that) {
    return forStatement(that);
  }

  public RetType forLabeledBreakStatement(LabeledBreakStatement that) {
    return forBreakStatement(that);
  }

  public RetType forUnlabeledBreakStatement(UnlabeledBreakStatement that) {
    return forBreakStatement(that);
  }

  public RetType forContinueStatement(ContinueStatement that) {
    return forStatement(that);
  }

  public RetType forLabeledContinueStatement(LabeledContinueStatement that) {
    return forContinueStatement(that);
  }

  public RetType forUnlabeledContinueStatement(UnlabeledContinueStatement that) {
    return forContinueStatement(that);
  }

  public RetType forReturnStatement(ReturnStatement that) {
    return forStatement(that);
  }

  public RetType forVoidReturnStatement(VoidReturnStatement that) {
    return forReturnStatement(that);
  }

  public RetType forValueReturnStatement(ValueReturnStatement that) {
    return forReturnStatement(that);
  }

  public RetType forThrowStatement(ThrowStatement that) {
    return forStatement(that);
  }

  public RetType forSynchronizedStatement(SynchronizedStatement that) {
    return forStatement(that);
  }

  public RetType forTryCatchStatement(TryCatchStatement that) {
    return forStatement(that);
  }

  public RetType forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    return forTryCatchStatement(that);
  }

  public RetType forNormalTryCatchStatement(NormalTryCatchStatement that) {
    return forTryCatchStatement(that);
  }

  public RetType forEmptyStatement(EmptyStatement that) {
    return forStatement(that);
  }

  public RetType forMethodDef(MethodDef that) {
    return forJExpression(that);
  }

  public RetType forConcreteMethodDef(ConcreteMethodDef that) {
    return forMethodDef(that);
  }

  public RetType forAbstractMethodDef(AbstractMethodDef that) {
    return forMethodDef(that);
  }

  public RetType forFormalParameter(FormalParameter that) {
    return forJExpression(that);
  }

  public RetType forVariableDeclaration(VariableDeclaration that) {
    return forJExpression(that);
  }

  public RetType forVariableDeclarator(VariableDeclarator that) {
    return forJExpression(that);
  }

  public RetType forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    return forVariableDeclarator(that);
  }

  public RetType forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    return forVariableDeclarator(that);
  }

  public RetType forTypeParameter(TypeParameter that) {
    return forJExpression(that);
  }

  public RetType forArrayInitializer(ArrayInitializer that) {
    return forJExpression(that);
  }

  public RetType forType(Type that) {
    return forJExpression(that);
  }

  public RetType forPrimitiveType(PrimitiveType that) {
    return forType(that);
  }

  public RetType forArrayType(ArrayType that) {
    return forType(that);
  }

  public RetType forReferenceType(ReferenceType that) {
    return forType(that);
  }

  public RetType forMemberType(MemberType that) {
    return forReferenceType(that);
  }

  public RetType forClassOrInterfaceType(ClassOrInterfaceType that) {
    return forReferenceType(that);
  }

  public RetType forTypeVariable(TypeVariable that) {
    return forReferenceType(that);
  }

  public RetType forVoidReturn(VoidReturn that) {
    return forJExpression(that);
  }

  public RetType forSwitchCase(SwitchCase that) {
    return forJExpression(that);
  }

  public RetType forLabeledCase(LabeledCase that) {
    return forSwitchCase(that);
  }

  public RetType forDefaultCase(DefaultCase that) {
    return forSwitchCase(that);
  }

  public RetType forCatchBlock(CatchBlock that) {
    return forJExpression(that);
  }

  public RetType forExpression(Expression that) {
    return forJExpression(that);
  }

  public RetType forAssignmentExpression(AssignmentExpression that) {
    return forExpression(that);
  }

  public RetType forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    return forAssignmentExpression(that);
  }

  public RetType forPlusAssignmentExpression(PlusAssignmentExpression that) {
    return forAssignmentExpression(that);
  }

  public RetType forNumericAssignmentExpression(NumericAssignmentExpression that) {
    return forAssignmentExpression(that);
  }

  public RetType forMinusAssignmentExpression(MinusAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }

  public RetType forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }

  public RetType forDivideAssignmentExpression(DivideAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }

  public RetType forModAssignmentExpression(ModAssignmentExpression that) {
    return forNumericAssignmentExpression(that);
  }

  public RetType forShiftAssignmentExpression(ShiftAssignmentExpression that) {
    return forAssignmentExpression(that);
  }

  public RetType forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that) {
    return forShiftAssignmentExpression(that);
  }

  public RetType forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that) {
    return forShiftAssignmentExpression(that);
  }

  public RetType forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that) {
    return forShiftAssignmentExpression(that);
  }

  public RetType forBitwiseAssignmentExpression(BitwiseAssignmentExpression that) {
    return forAssignmentExpression(that);
  }

  public RetType forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that) {
    return forBitwiseAssignmentExpression(that);
  }

  public RetType forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that) {
    return forBitwiseAssignmentExpression(that);
  }

  public RetType forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that) {
    return forBitwiseAssignmentExpression(that);
  }

  public RetType forBinaryExpression(BinaryExpression that) {
    return forExpression(that);
  }

  public RetType forBooleanExpression(BooleanExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forOrExpression(OrExpression that) {
    return forBooleanExpression(that);
  }

  public RetType forAndExpression(AndExpression that) {
    return forBooleanExpression(that);
  }

  public RetType forBitwiseBinaryExpression(BitwiseBinaryExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forBitwiseOrExpression(BitwiseOrExpression that) {
    return forBitwiseBinaryExpression(that);
  }

  public RetType forBitwiseXorExpression(BitwiseXorExpression that) {
    return forBitwiseBinaryExpression(that);
  }

  public RetType forBitwiseAndExpression(BitwiseAndExpression that) {
    return forBitwiseBinaryExpression(that);
  }

  public RetType forEqualityExpression(EqualityExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forEqualsExpression(EqualsExpression that) {
    return forEqualityExpression(that);
  }

  public RetType forNotEqualExpression(NotEqualExpression that) {
    return forEqualityExpression(that);
  }

  public RetType forComparisonExpression(ComparisonExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forLessThanExpression(LessThanExpression that) {
    return forComparisonExpression(that);
  }

  public RetType forLessThanOrEqualExpression(LessThanOrEqualExpression that) {
    return forComparisonExpression(that);
  }

  public RetType forGreaterThanExpression(GreaterThanExpression that) {
    return forComparisonExpression(that);
  }

  public RetType forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that) {
    return forComparisonExpression(that);
  }

  public RetType forShiftBinaryExpression(ShiftBinaryExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forLeftShiftExpression(LeftShiftExpression that) {
    return forShiftBinaryExpression(that);
  }

  public RetType forRightSignedShiftExpression(RightSignedShiftExpression that) {
    return forShiftBinaryExpression(that);
  }

  public RetType forRightUnsignedShiftExpression(RightUnsignedShiftExpression that) {
    return forShiftBinaryExpression(that);
  }

  public RetType forPlusExpression(PlusExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forNumericBinaryExpression(NumericBinaryExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forMinusExpression(MinusExpression that) {
    return forNumericBinaryExpression(that);
  }

  public RetType forMultiplyExpression(MultiplyExpression that) {
    return forNumericBinaryExpression(that);
  }

  public RetType forDivideExpression(DivideExpression that) {
    return forNumericBinaryExpression(that);
  }

  public RetType forModExpression(ModExpression that) {
    return forNumericBinaryExpression(that);
  }

  public RetType forNoOpExpression(NoOpExpression that) {
    return forBinaryExpression(that);
  }

  public RetType forUnaryExpression(UnaryExpression that) {
    return forExpression(that);
  }

  public RetType forIncrementExpression(IncrementExpression that) {
    return forUnaryExpression(that);
  }

  public RetType forPrefixIncrementExpression(PrefixIncrementExpression that) {
    return forIncrementExpression(that);
  }

  public RetType forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    return forPrefixIncrementExpression(that);
  }

  public RetType forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    return forPrefixIncrementExpression(that);
  }

  public RetType forPostfixIncrementExpression(PostfixIncrementExpression that) {
    return forIncrementExpression(that);
  }

  public RetType forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    return forPostfixIncrementExpression(that);
  }

  public RetType forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    return forPostfixIncrementExpression(that);
  }

  public RetType forNumericUnaryExpression(NumericUnaryExpression that) {
    return forUnaryExpression(that);
  }

  public RetType forPositiveExpression(PositiveExpression that) {
    return forNumericUnaryExpression(that);
  }

  public RetType forNegativeExpression(NegativeExpression that) {
    return forNumericUnaryExpression(that);
  }

  public RetType forBitwiseNotExpression(BitwiseNotExpression that) {
    return forUnaryExpression(that);
  }

  public RetType forNotExpression(NotExpression that) {
    return forUnaryExpression(that);
  }

  public RetType forConditionalExpression(ConditionalExpression that) {
    return forExpression(that);
  }

  public RetType forInstanceofExpression(InstanceofExpression that) {
    return forExpression(that);
  }

  public RetType forCastExpression(CastExpression that) {
    return forExpression(that);
  }

  public RetType forPrimary(Primary that) {
    return forExpression(that);
  }

  public RetType forLexicalLiteral(LexicalLiteral that) {
    return forPrimary(that);
  }

  public RetType forIntegerLiteral(IntegerLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forLongLiteral(LongLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forDoubleLiteral(DoubleLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forFloatLiteral(FloatLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forBooleanLiteral(BooleanLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forCharLiteral(CharLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forStringLiteral(StringLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forNullLiteral(NullLiteral that) {
    return forLexicalLiteral(that);
  }

  public RetType forInstantiation(Instantiation that) {
    return forPrimary(that);
  }

  public RetType forClassInstantiation(ClassInstantiation that) {
    return forInstantiation(that);
  }

  public RetType forNamedClassInstantiation(NamedClassInstantiation that) {
    return forClassInstantiation(that);
  }

  public RetType forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    return forNamedClassInstantiation(that);
  }

  public RetType forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    return forNamedClassInstantiation(that);
  }

  public RetType forAnonymousClassInstantiation(AnonymousClassInstantiation that) {
    return forClassInstantiation(that);
  }

  public RetType forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    return forAnonymousClassInstantiation(that);
  }

  public RetType forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    return forAnonymousClassInstantiation(that);
  }

  public RetType forArrayInstantiation(ArrayInstantiation that) {
    return forInstantiation(that);
  }

  public RetType forUninitializedArrayInstantiation(UninitializedArrayInstantiation that) {
    return forArrayInstantiation(that);
  }

  public RetType forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    return forUninitializedArrayInstantiation(that);
  }

  public RetType forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    return forUninitializedArrayInstantiation(that);
  }

  public RetType forInitializedArrayInstantiation(InitializedArrayInstantiation that) {
    return forArrayInstantiation(that);
  }

  public RetType forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    return forInitializedArrayInstantiation(that);
  }

  public RetType forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    return forInitializedArrayInstantiation(that);
  }

  public RetType forVariableReference(VariableReference that) {
    return forPrimary(that);
  }

  public RetType forNameReference(NameReference that) {
    return forVariableReference(that);
  }

  public RetType forSimpleNameReference(SimpleNameReference that) {
    return forNameReference(that);
  }

  public RetType forComplexNameReference(ComplexNameReference that) {
    return forNameReference(that);
  }

  public RetType forThisReference(ThisReference that) {
    return forVariableReference(that);
  }

  public RetType forSimpleThisReference(SimpleThisReference that) {
    return forThisReference(that);
  }

  public RetType forComplexThisReference(ComplexThisReference that) {
    return forThisReference(that);
  }

  public RetType forSuperReference(SuperReference that) {
    return forVariableReference(that);
  }

  public RetType forSimpleSuperReference(SimpleSuperReference that) {
    return forSuperReference(that);
  }

  public RetType forComplexSuperReference(ComplexSuperReference that) {
    return forSuperReference(that);
  }

  public RetType forFunctionInvocation(FunctionInvocation that) {
    return forPrimary(that);
  }

  public RetType forMethodInvocation(MethodInvocation that) {
    return forFunctionInvocation(that);
  }

  public RetType forSimpleMethodInvocation(SimpleMethodInvocation that) {
    return forMethodInvocation(that);
  }

  public RetType forComplexMethodInvocation(ComplexMethodInvocation that) {
    return forMethodInvocation(that);
  }

  public RetType forThisConstructorInvocation(ThisConstructorInvocation that) {
    return forFunctionInvocation(that);
  }

  public RetType forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    return forThisConstructorInvocation(that);
  }

  public RetType forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    return forThisConstructorInvocation(that);
  }

  public RetType forSuperConstructorInvocation(SuperConstructorInvocation that) {
    return forFunctionInvocation(that);
  }

  public RetType forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that) {
    return forSuperConstructorInvocation(that);
  }

  public RetType forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that) {
    return forSuperConstructorInvocation(that);
  }

  public RetType forClassLiteral(ClassLiteral that) {
    return forPrimary(that);
  }

  public RetType forArrayAccess(ArrayAccess that) {
    return forPrimary(that);
  }

  public RetType forParenthesized(Parenthesized that) {
    return forPrimary(that);
  }

  public RetType forEmptyExpression(EmptyExpression that) {
    return forPrimary(that);
  }

  public RetType forBody(Body that) {
    return forJExpression(that);
  }

  public RetType forBracedBody(BracedBody that) {
    return forBody(that);
  }

  public RetType forUnbracedBody(UnbracedBody that) {
    return forBody(that);
  }

  public RetType forExpressionList(ExpressionList that) {
    return forJExpression(that);
  }

  public RetType forParenthesizedExpressionList(ParenthesizedExpressionList that) {
    return forExpressionList(that);
  }

  public RetType forUnparenthesizedExpressionList(UnparenthesizedExpressionList that) {
    return forExpressionList(that);
  }

  public RetType forDimensionExpressionList(DimensionExpressionList that) {
    return forExpressionList(that);
  }

  public RetType forEmptyForCondition(EmptyForCondition that) {
    return forJExpression(that);
  }


}
