package edu.rice.cs.javalanglevels.tree;

/** A parametric interface for visitors over JExpressionIF that return a value. */
public interface JExpressionIFVisitor<RetType> {

  /** Process an instance of SourceFile. */
  public RetType forSourceFile(SourceFile that);

  /** Process an instance of ModifiersAndVisibility. */
  public RetType forModifiersAndVisibility(ModifiersAndVisibility that);

  /** Process an instance of CompoundWord. */
  public RetType forCompoundWord(CompoundWord that);

  /** Process an instance of Word. */
  public RetType forWord(Word that);

  /** Process an instance of ClassDef. */
  public RetType forClassDef(ClassDef that);

  /** Process an instance of InnerClassDef. */
  public RetType forInnerClassDef(InnerClassDef that);

  /** Process an instance of InterfaceDef. */
  public RetType forInterfaceDef(InterfaceDef that);

  /** Process an instance of InnerInterfaceDef. */
  public RetType forInnerInterfaceDef(InnerInterfaceDef that);

  /** Process an instance of ConstructorDef. */
  public RetType forConstructorDef(ConstructorDef that);

  /** Process an instance of InstanceInitializer. */
  public RetType forInstanceInitializer(InstanceInitializer that);

  /** Process an instance of StaticInitializer. */
  public RetType forStaticInitializer(StaticInitializer that);

  /** Process an instance of PackageStatement. */
  public RetType forPackageStatement(PackageStatement that);

  /** Process an instance of ClassImportStatement. */
  public RetType forClassImportStatement(ClassImportStatement that);

  /** Process an instance of PackageImportStatement. */
  public RetType forPackageImportStatement(PackageImportStatement that);

  /** Process an instance of LabeledStatement. */
  public RetType forLabeledStatement(LabeledStatement that);

  /** Process an instance of Block. */
  public RetType forBlock(Block that);

  /** Process an instance of ExpressionStatement. */
  public RetType forExpressionStatement(ExpressionStatement that);

  /** Process an instance of SwitchStatement. */
  public RetType forSwitchStatement(SwitchStatement that);

  /** Process an instance of IfThenStatement. */
  public RetType forIfThenStatement(IfThenStatement that);

  /** Process an instance of IfThenElseStatement. */
  public RetType forIfThenElseStatement(IfThenElseStatement that);

  /** Process an instance of WhileStatement. */
  public RetType forWhileStatement(WhileStatement that);

  /** Process an instance of DoStatement. */
  public RetType forDoStatement(DoStatement that);

  /** Process an instance of ForStatement. */
  public RetType forForStatement(ForStatement that);

  /** Process an instance of LabeledBreakStatement. */
  public RetType forLabeledBreakStatement(LabeledBreakStatement that);

  /** Process an instance of UnlabeledBreakStatement. */
  public RetType forUnlabeledBreakStatement(UnlabeledBreakStatement that);

  /** Process an instance of LabeledContinueStatement. */
  public RetType forLabeledContinueStatement(LabeledContinueStatement that);

  /** Process an instance of UnlabeledContinueStatement. */
  public RetType forUnlabeledContinueStatement(UnlabeledContinueStatement that);

  /** Process an instance of VoidReturnStatement. */
  public RetType forVoidReturnStatement(VoidReturnStatement that);

  /** Process an instance of ValueReturnStatement. */
  public RetType forValueReturnStatement(ValueReturnStatement that);

  /** Process an instance of ThrowStatement. */
  public RetType forThrowStatement(ThrowStatement that);

  /** Process an instance of SynchronizedStatement. */
  public RetType forSynchronizedStatement(SynchronizedStatement that);

  /** Process an instance of TryCatchFinallyStatement. */
  public RetType forTryCatchFinallyStatement(TryCatchFinallyStatement that);

  /** Process an instance of NormalTryCatchStatement. */
  public RetType forNormalTryCatchStatement(NormalTryCatchStatement that);

  /** Process an instance of EmptyStatement. */
  public RetType forEmptyStatement(EmptyStatement that);

  /** Process an instance of ConcreteMethodDef. */
  public RetType forConcreteMethodDef(ConcreteMethodDef that);

  /** Process an instance of AbstractMethodDef. */
  public RetType forAbstractMethodDef(AbstractMethodDef that);

  /** Process an instance of FormalParameter. */
  public RetType forFormalParameter(FormalParameter that);

  /** Process an instance of VariableDeclaration. */
  public RetType forVariableDeclaration(VariableDeclaration that);

  /** Process an instance of UninitializedVariableDeclarator. */
  public RetType forUninitializedVariableDeclarator(UninitializedVariableDeclarator that);

  /** Process an instance of InitializedVariableDeclarator. */
  public RetType forInitializedVariableDeclarator(InitializedVariableDeclarator that);

  /** Process an instance of TypeParameter. */
  public RetType forTypeParameter(TypeParameter that);

  /** Process an instance of ArrayInitializer. */
  public RetType forArrayInitializer(ArrayInitializer that);

  /** Process an instance of PrimitiveType. */
  public RetType forPrimitiveType(PrimitiveType that);

  /** Process an instance of ArrayType. */
  public RetType forArrayType(ArrayType that);

  /** Process an instance of MemberType. */
  public RetType forMemberType(MemberType that);

  /** Process an instance of ClassOrInterfaceType. */
  public RetType forClassOrInterfaceType(ClassOrInterfaceType that);

  /** Process an instance of TypeVariable. */
  public RetType forTypeVariable(TypeVariable that);

  /** Process an instance of VoidReturn. */
  public RetType forVoidReturn(VoidReturn that);

  /** Process an instance of LabeledCase. */
  public RetType forLabeledCase(LabeledCase that);

  /** Process an instance of DefaultCase. */
  public RetType forDefaultCase(DefaultCase that);

  /** Process an instance of CatchBlock. */
  public RetType forCatchBlock(CatchBlock that);

  /** Process an instance of SimpleAssignmentExpression. */
  public RetType forSimpleAssignmentExpression(SimpleAssignmentExpression that);

  /** Process an instance of PlusAssignmentExpression. */
  public RetType forPlusAssignmentExpression(PlusAssignmentExpression that);

  /** Process an instance of MinusAssignmentExpression. */
  public RetType forMinusAssignmentExpression(MinusAssignmentExpression that);

  /** Process an instance of MultiplyAssignmentExpression. */
  public RetType forMultiplyAssignmentExpression(MultiplyAssignmentExpression that);

  /** Process an instance of DivideAssignmentExpression. */
  public RetType forDivideAssignmentExpression(DivideAssignmentExpression that);

  /** Process an instance of ModAssignmentExpression. */
  public RetType forModAssignmentExpression(ModAssignmentExpression that);

  /** Process an instance of LeftShiftAssignmentExpression. */
  public RetType forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that);

  /** Process an instance of RightSignedShiftAssignmentExpression. */
  public RetType forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that);

  /** Process an instance of RightUnsignedShiftAssignmentExpression. */
  public RetType forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that);

  /** Process an instance of BitwiseAndAssignmentExpression. */
  public RetType forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that);

  /** Process an instance of BitwiseOrAssignmentExpression. */
  public RetType forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that);

  /** Process an instance of BitwiseXorAssignmentExpression. */
  public RetType forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that);

  /** Process an instance of OrExpression. */
  public RetType forOrExpression(OrExpression that);

  /** Process an instance of AndExpression. */
  public RetType forAndExpression(AndExpression that);

  /** Process an instance of BitwiseOrExpression. */
  public RetType forBitwiseOrExpression(BitwiseOrExpression that);

  /** Process an instance of BitwiseXorExpression. */
  public RetType forBitwiseXorExpression(BitwiseXorExpression that);

  /** Process an instance of BitwiseAndExpression. */
  public RetType forBitwiseAndExpression(BitwiseAndExpression that);

  /** Process an instance of EqualsExpression. */
  public RetType forEqualsExpression(EqualsExpression that);

  /** Process an instance of NotEqualExpression. */
  public RetType forNotEqualExpression(NotEqualExpression that);

  /** Process an instance of LessThanExpression. */
  public RetType forLessThanExpression(LessThanExpression that);

  /** Process an instance of LessThanOrEqualExpression. */
  public RetType forLessThanOrEqualExpression(LessThanOrEqualExpression that);

  /** Process an instance of GreaterThanExpression. */
  public RetType forGreaterThanExpression(GreaterThanExpression that);

  /** Process an instance of GreaterThanOrEqualExpression. */
  public RetType forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that);

  /** Process an instance of LeftShiftExpression. */
  public RetType forLeftShiftExpression(LeftShiftExpression that);

  /** Process an instance of RightSignedShiftExpression. */
  public RetType forRightSignedShiftExpression(RightSignedShiftExpression that);

  /** Process an instance of RightUnsignedShiftExpression. */
  public RetType forRightUnsignedShiftExpression(RightUnsignedShiftExpression that);

  /** Process an instance of PlusExpression. */
  public RetType forPlusExpression(PlusExpression that);

  /** Process an instance of MinusExpression. */
  public RetType forMinusExpression(MinusExpression that);

  /** Process an instance of MultiplyExpression. */
  public RetType forMultiplyExpression(MultiplyExpression that);

  /** Process an instance of DivideExpression. */
  public RetType forDivideExpression(DivideExpression that);

  /** Process an instance of ModExpression. */
  public RetType forModExpression(ModExpression that);

  /** Process an instance of NoOpExpression. */
  public RetType forNoOpExpression(NoOpExpression that);

  /** Process an instance of PositivePrefixIncrementExpression. */
  public RetType forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that);

  /** Process an instance of NegativePrefixIncrementExpression. */
  public RetType forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that);

  /** Process an instance of PositivePostfixIncrementExpression. */
  public RetType forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that);

  /** Process an instance of NegativePostfixIncrementExpression. */
  public RetType forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that);

  /** Process an instance of PositiveExpression. */
  public RetType forPositiveExpression(PositiveExpression that);

  /** Process an instance of NegativeExpression. */
  public RetType forNegativeExpression(NegativeExpression that);

  /** Process an instance of BitwiseNotExpression. */
  public RetType forBitwiseNotExpression(BitwiseNotExpression that);

  /** Process an instance of NotExpression. */
  public RetType forNotExpression(NotExpression that);

  /** Process an instance of ConditionalExpression. */
  public RetType forConditionalExpression(ConditionalExpression that);

  /** Process an instance of InstanceofExpression. */
  public RetType forInstanceofExpression(InstanceofExpression that);

  /** Process an instance of CastExpression. */
  public RetType forCastExpression(CastExpression that);

  /** Process an instance of IntegerLiteral. */
  public RetType forIntegerLiteral(IntegerLiteral that);

  /** Process an instance of LongLiteral. */
  public RetType forLongLiteral(LongLiteral that);

  /** Process an instance of DoubleLiteral. */
  public RetType forDoubleLiteral(DoubleLiteral that);

  /** Process an instance of FloatLiteral. */
  public RetType forFloatLiteral(FloatLiteral that);

  /** Process an instance of BooleanLiteral. */
  public RetType forBooleanLiteral(BooleanLiteral that);

  /** Process an instance of CharLiteral. */
  public RetType forCharLiteral(CharLiteral that);

  /** Process an instance of StringLiteral. */
  public RetType forStringLiteral(StringLiteral that);

  /** Process an instance of NullLiteral. */
  public RetType forNullLiteral(NullLiteral that);

  /** Process an instance of SimpleNamedClassInstantiation. */
  public RetType forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that);

  /** Process an instance of ComplexNamedClassInstantiation. */
  public RetType forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that);

  /** Process an instance of SimpleAnonymousClassInstantiation. */
  public RetType forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that);

  /** Process an instance of ComplexAnonymousClassInstantiation. */
  public RetType forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that);

  /** Process an instance of SimpleUninitializedArrayInstantiation. */
  public RetType forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that);

  /** Process an instance of ComplexUninitializedArrayInstantiation. */
  public RetType forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that);

  /** Process an instance of SimpleInitializedArrayInstantiation. */
  public RetType forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that);

  /** Process an instance of ComplexInitializedArrayInstantiation. */
  public RetType forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that);

  /** Process an instance of SimpleNameReference. */
  public RetType forSimpleNameReference(SimpleNameReference that);

  /** Process an instance of ComplexNameReference. */
  public RetType forComplexNameReference(ComplexNameReference that);

  /** Process an instance of SimpleThisReference. */
  public RetType forSimpleThisReference(SimpleThisReference that);

  /** Process an instance of ComplexThisReference. */
  public RetType forComplexThisReference(ComplexThisReference that);

  /** Process an instance of SimpleSuperReference. */
  public RetType forSimpleSuperReference(SimpleSuperReference that);

  /** Process an instance of ComplexSuperReference. */
  public RetType forComplexSuperReference(ComplexSuperReference that);

  /** Process an instance of SimpleMethodInvocation. */
  public RetType forSimpleMethodInvocation(SimpleMethodInvocation that);

  /** Process an instance of ComplexMethodInvocation. */
  public RetType forComplexMethodInvocation(ComplexMethodInvocation that);

  /** Process an instance of SimpleThisConstructorInvocation. */
  public RetType forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that);

  /** Process an instance of ComplexThisConstructorInvocation. */
  public RetType forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that);

  /** Process an instance of SimpleSuperConstructorInvocation. */
  public RetType forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that);

  /** Process an instance of ComplexSuperConstructorInvocation. */
  public RetType forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that);

  /** Process an instance of ClassLiteral. */
  public RetType forClassLiteral(ClassLiteral that);

  /** Process an instance of ArrayAccess. */
  public RetType forArrayAccess(ArrayAccess that);

  /** Process an instance of Parenthesized. */
  public RetType forParenthesized(Parenthesized that);

  /** Process an instance of EmptyExpression. */
  public RetType forEmptyExpression(EmptyExpression that);

  /** Process an instance of BracedBody. */
  public RetType forBracedBody(BracedBody that);

  /** Process an instance of UnbracedBody. */
  public RetType forUnbracedBody(UnbracedBody that);

  /** Process an instance of ParenthesizedExpressionList. */
  public RetType forParenthesizedExpressionList(ParenthesizedExpressionList that);

  /** Process an instance of UnparenthesizedExpressionList. */
  public RetType forUnparenthesizedExpressionList(UnparenthesizedExpressionList that);

  /** Process an instance of DimensionExpressionList. */
  public RetType forDimensionExpressionList(DimensionExpressionList that);

  /** Process an instance of EmptyForCondition. */
  public RetType forEmptyForCondition(EmptyForCondition that);
}
