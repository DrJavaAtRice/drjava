package edu.rice.cs.javalanglevels.tree;

/** An interface for visitors over JExpressionIF that do not return a value. */
public interface JExpressionIFVisitor_void {

  /** Process an instance of SourceFile. */
  public void forSourceFile(SourceFile that);

  /** Process an instance of ModifiersAndVisibility. */
  public void forModifiersAndVisibility(ModifiersAndVisibility that);

  /** Process an instance of CompoundWord. */
  public void forCompoundWord(CompoundWord that);

  /** Process an instance of Word. */
  public void forWord(Word that);

  /** Process an instance of ClassDef. */
  public void forClassDef(ClassDef that);

  /** Process an instance of InnerClassDef. */
  public void forInnerClassDef(InnerClassDef that);

  /** Process an instance of InterfaceDef. */
  public void forInterfaceDef(InterfaceDef that);

  /** Process an instance of InnerInterfaceDef. */
  public void forInnerInterfaceDef(InnerInterfaceDef that);

  /** Process an instance of ConstructorDef. */
  public void forConstructorDef(ConstructorDef that);

  /** Process an instance of InstanceInitializer. */
  public void forInstanceInitializer(InstanceInitializer that);

  /** Process an instance of StaticInitializer. */
  public void forStaticInitializer(StaticInitializer that);

  /** Process an instance of PackageStatement. */
  public void forPackageStatement(PackageStatement that);

  /** Process an instance of ClassImportStatement. */
  public void forClassImportStatement(ClassImportStatement that);

  /** Process an instance of PackageImportStatement. */
  public void forPackageImportStatement(PackageImportStatement that);

  /** Process an instance of LabeledStatement. */
  public void forLabeledStatement(LabeledStatement that);

  /** Process an instance of Block. */
  public void forBlock(Block that);

  /** Process an instance of ExpressionStatement. */
  public void forExpressionStatement(ExpressionStatement that);

  /** Process an instance of SwitchStatement. */
  public void forSwitchStatement(SwitchStatement that);

  /** Process an instance of IfThenStatement. */
  public void forIfThenStatement(IfThenStatement that);

  /** Process an instance of IfThenElseStatement. */
  public void forIfThenElseStatement(IfThenElseStatement that);

  /** Process an instance of WhileStatement. */
  public void forWhileStatement(WhileStatement that);

  /** Process an instance of DoStatement. */
  public void forDoStatement(DoStatement that);

  /** Process an instance of ForStatement. */
  public void forForStatement(ForStatement that);

  /** Process an instance of LabeledBreakStatement. */
  public void forLabeledBreakStatement(LabeledBreakStatement that);

  /** Process an instance of UnlabeledBreakStatement. */
  public void forUnlabeledBreakStatement(UnlabeledBreakStatement that);

  /** Process an instance of LabeledContinueStatement. */
  public void forLabeledContinueStatement(LabeledContinueStatement that);

  /** Process an instance of UnlabeledContinueStatement. */
  public void forUnlabeledContinueStatement(UnlabeledContinueStatement that);

  /** Process an instance of VoidReturnStatement. */
  public void forVoidReturnStatement(VoidReturnStatement that);

  /** Process an instance of ValueReturnStatement. */
  public void forValueReturnStatement(ValueReturnStatement that);

  /** Process an instance of ThrowStatement. */
  public void forThrowStatement(ThrowStatement that);

  /** Process an instance of SynchronizedStatement. */
  public void forSynchronizedStatement(SynchronizedStatement that);

  /** Process an instance of TryCatchFinallyStatement. */
  public void forTryCatchFinallyStatement(TryCatchFinallyStatement that);

  /** Process an instance of NormalTryCatchStatement. */
  public void forNormalTryCatchStatement(NormalTryCatchStatement that);

  /** Process an instance of EmptyStatement. */
  public void forEmptyStatement(EmptyStatement that);

  /** Process an instance of ConcreteMethodDef. */
  public void forConcreteMethodDef(ConcreteMethodDef that);

  /** Process an instance of AbstractMethodDef. */
  public void forAbstractMethodDef(AbstractMethodDef that);

  /** Process an instance of FormalParameter. */
  public void forFormalParameter(FormalParameter that);

  /** Process an instance of VariableDeclaration. */
  public void forVariableDeclaration(VariableDeclaration that);

  /** Process an instance of UninitializedVariableDeclarator. */
  public void forUninitializedVariableDeclarator(UninitializedVariableDeclarator that);

  /** Process an instance of InitializedVariableDeclarator. */
  public void forInitializedVariableDeclarator(InitializedVariableDeclarator that);

  /** Process an instance of TypeParameter. */
  public void forTypeParameter(TypeParameter that);

  /** Process an instance of ArrayInitializer. */
  public void forArrayInitializer(ArrayInitializer that);

  /** Process an instance of PrimitiveType. */
  public void forPrimitiveType(PrimitiveType that);

  /** Process an instance of ArrayType. */
  public void forArrayType(ArrayType that);

  /** Process an instance of MemberType. */
  public void forMemberType(MemberType that);

  /** Process an instance of ClassOrInterfaceType. */
  public void forClassOrInterfaceType(ClassOrInterfaceType that);

  /** Process an instance of TypeVariable. */
  public void forTypeVariable(TypeVariable that);

  /** Process an instance of VoidReturn. */
  public void forVoidReturn(VoidReturn that);

  /** Process an instance of LabeledCase. */
  public void forLabeledCase(LabeledCase that);

  /** Process an instance of DefaultCase. */
  public void forDefaultCase(DefaultCase that);

  /** Process an instance of CatchBlock. */
  public void forCatchBlock(CatchBlock that);

  /** Process an instance of SimpleAssignmentExpression. */
  public void forSimpleAssignmentExpression(SimpleAssignmentExpression that);

  /** Process an instance of PlusAssignmentExpression. */
  public void forPlusAssignmentExpression(PlusAssignmentExpression that);

  /** Process an instance of MinusAssignmentExpression. */
  public void forMinusAssignmentExpression(MinusAssignmentExpression that);

  /** Process an instance of MultiplyAssignmentExpression. */
  public void forMultiplyAssignmentExpression(MultiplyAssignmentExpression that);

  /** Process an instance of DivideAssignmentExpression. */
  public void forDivideAssignmentExpression(DivideAssignmentExpression that);

  /** Process an instance of ModAssignmentExpression. */
  public void forModAssignmentExpression(ModAssignmentExpression that);

  /** Process an instance of LeftShiftAssignmentExpression. */
  public void forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that);

  /** Process an instance of RightSignedShiftAssignmentExpression. */
  public void forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that);

  /** Process an instance of RightUnsignedShiftAssignmentExpression. */
  public void forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that);

  /** Process an instance of BitwiseAndAssignmentExpression. */
  public void forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that);

  /** Process an instance of BitwiseOrAssignmentExpression. */
  public void forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that);

  /** Process an instance of BitwiseXorAssignmentExpression. */
  public void forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that);

  /** Process an instance of OrExpression. */
  public void forOrExpression(OrExpression that);

  /** Process an instance of AndExpression. */
  public void forAndExpression(AndExpression that);

  /** Process an instance of BitwiseOrExpression. */
  public void forBitwiseOrExpression(BitwiseOrExpression that);

  /** Process an instance of BitwiseXorExpression. */
  public void forBitwiseXorExpression(BitwiseXorExpression that);

  /** Process an instance of BitwiseAndExpression. */
  public void forBitwiseAndExpression(BitwiseAndExpression that);

  /** Process an instance of EqualsExpression. */
  public void forEqualsExpression(EqualsExpression that);

  /** Process an instance of NotEqualExpression. */
  public void forNotEqualExpression(NotEqualExpression that);

  /** Process an instance of LessThanExpression. */
  public void forLessThanExpression(LessThanExpression that);

  /** Process an instance of LessThanOrEqualExpression. */
  public void forLessThanOrEqualExpression(LessThanOrEqualExpression that);

  /** Process an instance of GreaterThanExpression. */
  public void forGreaterThanExpression(GreaterThanExpression that);

  /** Process an instance of GreaterThanOrEqualExpression. */
  public void forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that);

  /** Process an instance of LeftShiftExpression. */
  public void forLeftShiftExpression(LeftShiftExpression that);

  /** Process an instance of RightSignedShiftExpression. */
  public void forRightSignedShiftExpression(RightSignedShiftExpression that);

  /** Process an instance of RightUnsignedShiftExpression. */
  public void forRightUnsignedShiftExpression(RightUnsignedShiftExpression that);

  /** Process an instance of PlusExpression. */
  public void forPlusExpression(PlusExpression that);

  /** Process an instance of MinusExpression. */
  public void forMinusExpression(MinusExpression that);

  /** Process an instance of MultiplyExpression. */
  public void forMultiplyExpression(MultiplyExpression that);

  /** Process an instance of DivideExpression. */
  public void forDivideExpression(DivideExpression that);

  /** Process an instance of ModExpression. */
  public void forModExpression(ModExpression that);

  /** Process an instance of NoOpExpression. */
  public void forNoOpExpression(NoOpExpression that);

  /** Process an instance of PositivePrefixIncrementExpression. */
  public void forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that);

  /** Process an instance of NegativePrefixIncrementExpression. */
  public void forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that);

  /** Process an instance of PositivePostfixIncrementExpression. */
  public void forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that);

  /** Process an instance of NegativePostfixIncrementExpression. */
  public void forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that);

  /** Process an instance of PositiveExpression. */
  public void forPositiveExpression(PositiveExpression that);

  /** Process an instance of NegativeExpression. */
  public void forNegativeExpression(NegativeExpression that);

  /** Process an instance of BitwiseNotExpression. */
  public void forBitwiseNotExpression(BitwiseNotExpression that);

  /** Process an instance of NotExpression. */
  public void forNotExpression(NotExpression that);

  /** Process an instance of ConditionalExpression. */
  public void forConditionalExpression(ConditionalExpression that);

  /** Process an instance of InstanceofExpression. */
  public void forInstanceofExpression(InstanceofExpression that);

  /** Process an instance of CastExpression. */
  public void forCastExpression(CastExpression that);

  /** Process an instance of IntegerLiteral. */
  public void forIntegerLiteral(IntegerLiteral that);

  /** Process an instance of LongLiteral. */
  public void forLongLiteral(LongLiteral that);

  /** Process an instance of DoubleLiteral. */
  public void forDoubleLiteral(DoubleLiteral that);

  /** Process an instance of FloatLiteral. */
  public void forFloatLiteral(FloatLiteral that);

  /** Process an instance of BooleanLiteral. */
  public void forBooleanLiteral(BooleanLiteral that);

  /** Process an instance of CharLiteral. */
  public void forCharLiteral(CharLiteral that);

  /** Process an instance of StringLiteral. */
  public void forStringLiteral(StringLiteral that);

  /** Process an instance of NullLiteral. */
  public void forNullLiteral(NullLiteral that);

  /** Process an instance of SimpleNamedClassInstantiation. */
  public void forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that);

  /** Process an instance of ComplexNamedClassInstantiation. */
  public void forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that);

  /** Process an instance of SimpleAnonymousClassInstantiation. */
  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that);

  /** Process an instance of ComplexAnonymousClassInstantiation. */
  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that);

  /** Process an instance of SimpleUninitializedArrayInstantiation. */
  public void forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that);

  /** Process an instance of ComplexUninitializedArrayInstantiation. */
  public void forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that);

  /** Process an instance of SimpleInitializedArrayInstantiation. */
  public void forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that);

  /** Process an instance of ComplexInitializedArrayInstantiation. */
  public void forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that);

  /** Process an instance of SimpleNameReference. */
  public void forSimpleNameReference(SimpleNameReference that);

  /** Process an instance of ComplexNameReference. */
  public void forComplexNameReference(ComplexNameReference that);

  /** Process an instance of SimpleThisReference. */
  public void forSimpleThisReference(SimpleThisReference that);

  /** Process an instance of ComplexThisReference. */
  public void forComplexThisReference(ComplexThisReference that);

  /** Process an instance of SimpleSuperReference. */
  public void forSimpleSuperReference(SimpleSuperReference that);

  /** Process an instance of ComplexSuperReference. */
  public void forComplexSuperReference(ComplexSuperReference that);

  /** Process an instance of SimpleMethodInvocation. */
  public void forSimpleMethodInvocation(SimpleMethodInvocation that);

  /** Process an instance of ComplexMethodInvocation. */
  public void forComplexMethodInvocation(ComplexMethodInvocation that);

  /** Process an instance of SimpleThisConstructorInvocation. */
  public void forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that);

  /** Process an instance of ComplexThisConstructorInvocation. */
  public void forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that);

  /** Process an instance of SimpleSuperConstructorInvocation. */
  public void forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that);

  /** Process an instance of ComplexSuperConstructorInvocation. */
  public void forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that);

  /** Process an instance of ClassLiteral. */
  public void forClassLiteral(ClassLiteral that);

  /** Process an instance of ArrayAccess. */
  public void forArrayAccess(ArrayAccess that);

  /** Process an instance of Parenthesized. */
  public void forParenthesized(Parenthesized that);

  /** Process an instance of EmptyExpression. */
  public void forEmptyExpression(EmptyExpression that);

  /** Process an instance of BracedBody. */
  public void forBracedBody(BracedBody that);

  /** Process an instance of UnbracedBody. */
  public void forUnbracedBody(UnbracedBody that);

  /** Process an instance of ParenthesizedExpressionList. */
  public void forParenthesizedExpressionList(ParenthesizedExpressionList that);

  /** Process an instance of UnparenthesizedExpressionList. */
  public void forUnparenthesizedExpressionList(UnparenthesizedExpressionList that);

  /** Process an instance of DimensionExpressionList. */
  public void forDimensionExpressionList(DimensionExpressionList that);

  /** Process an instance of EmptyForCondition. */
  public void forEmptyForCondition(EmptyForCondition that);
}
