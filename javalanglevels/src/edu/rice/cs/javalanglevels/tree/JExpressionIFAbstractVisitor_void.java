package edu.rice.cs.javalanglevels.tree;

/** An abstract implementation of a visitor over JExpressionIF that does not return a value.
 ** This visitor implements the visitor interface with methods that 
 ** execute defaultCase.  These methods can be overriden 
 ** in order to achieve different behavior for particular cases.
 **/
public class JExpressionIFAbstractVisitor_void implements JExpressionIFVisitor_void {
  /* Methods to visit an item. */
  public void forJExpression(JExpression that) {
    defaultCase(that);
  }

  public void forSourceFile(SourceFile that) {
    forJExpression(that);
  }

  public void forModifiersAndVisibility(ModifiersAndVisibility that) {
    forJExpression(that);
  }

  public void forCompoundWord(CompoundWord that) {
    forJExpression(that);
  }

  public void forWord(Word that) {
    forJExpression(that);
  }

  public void forTypeDefBase(TypeDefBase that) {
    forJExpression(that);
  }

  public void forClassDef(ClassDef that) {
    forTypeDefBase(that);
  }

  public void forInnerClassDef(InnerClassDef that) {
    forClassDef(that);
  }

  public void forInterfaceDef(InterfaceDef that) {
    forTypeDefBase(that);
  }

  public void forInnerInterfaceDef(InnerInterfaceDef that) {
    forInterfaceDef(that);
  }

  public void forConstructorDef(ConstructorDef that) {
    forJExpression(that);
  }

  public void forInitializer(Initializer that) {
    forJExpression(that);
  }

  public void forInstanceInitializer(InstanceInitializer that) {
    forInitializer(that);
  }

  public void forStaticInitializer(StaticInitializer that) {
    forInitializer(that);
  }

  public void forPackageStatement(PackageStatement that) {
    forJExpression(that);
  }

  public void forImportStatement(ImportStatement that) {
    forJExpression(that);
  }

  public void forClassImportStatement(ClassImportStatement that) {
    forImportStatement(that);
  }

  public void forPackageImportStatement(PackageImportStatement that) {
    forImportStatement(that);
  }

  public void forStatement(Statement that) {
    forJExpression(that);
  }

  public void forLabeledStatement(LabeledStatement that) {
    forStatement(that);
  }

  public void forBlock(Block that) {
    forStatement(that);
  }

  public void forExpressionStatement(ExpressionStatement that) {
    forStatement(that);
  }

  public void forSwitchStatement(SwitchStatement that) {
    forStatement(that);
  }

  public void forIfThenStatement(IfThenStatement that) {
    forStatement(that);
  }

  public void forIfThenElseStatement(IfThenElseStatement that) {
    forIfThenStatement(that);
  }

  public void forWhileStatement(WhileStatement that) {
    forStatement(that);
  }

  public void forDoStatement(DoStatement that) {
    forStatement(that);
  }

  public void forForStatement(ForStatement that) {
    forStatement(that);
  }

  public void forBreakStatement(BreakStatement that) {
    forStatement(that);
  }

  public void forLabeledBreakStatement(LabeledBreakStatement that) {
    forBreakStatement(that);
  }

  public void forUnlabeledBreakStatement(UnlabeledBreakStatement that) {
    forBreakStatement(that);
  }

  public void forContinueStatement(ContinueStatement that) {
    forStatement(that);
  }

  public void forLabeledContinueStatement(LabeledContinueStatement that) {
    forContinueStatement(that);
  }

  public void forUnlabeledContinueStatement(UnlabeledContinueStatement that) {
    forContinueStatement(that);
  }

  public void forReturnStatement(ReturnStatement that) {
    forStatement(that);
  }

  public void forVoidReturnStatement(VoidReturnStatement that) {
    forReturnStatement(that);
  }

  public void forValueReturnStatement(ValueReturnStatement that) {
    forReturnStatement(that);
  }

  public void forThrowStatement(ThrowStatement that) {
    forStatement(that);
  }

  public void forSynchronizedStatement(SynchronizedStatement that) {
    forStatement(that);
  }

  public void forTryCatchStatement(TryCatchStatement that) {
    forStatement(that);
  }

  public void forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    forTryCatchStatement(that);
  }

  public void forNormalTryCatchStatement(NormalTryCatchStatement that) {
    forTryCatchStatement(that);
  }

  public void forEmptyStatement(EmptyStatement that) {
    forStatement(that);
  }

  public void forMethodDef(MethodDef that) {
    forJExpression(that);
  }

  public void forConcreteMethodDef(ConcreteMethodDef that) {
    forMethodDef(that);
  }

  public void forAbstractMethodDef(AbstractMethodDef that) {
    forMethodDef(that);
  }

  public void forFormalParameter(FormalParameter that) {
    forJExpression(that);
  }

  public void forVariableDeclaration(VariableDeclaration that) {
    forJExpression(that);
  }

  public void forVariableDeclarator(VariableDeclarator that) {
    forJExpression(that);
  }

  public void forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    forVariableDeclarator(that);
  }

  public void forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    forVariableDeclarator(that);
  }

  public void forTypeParameter(TypeParameter that) {
    forJExpression(that);
  }

  public void forArrayInitializer(ArrayInitializer that) {
    forJExpression(that);
  }

  public void forType(Type that) {
    forJExpression(that);
  }

  public void forPrimitiveType(PrimitiveType that) {
    forType(that);
  }

  public void forArrayType(ArrayType that) {
    forType(that);
  }

  public void forReferenceType(ReferenceType that) {
    forType(that);
  }

  public void forMemberType(MemberType that) {
    forReferenceType(that);
  }

  public void forClassOrInterfaceType(ClassOrInterfaceType that) {
    forReferenceType(that);
  }

  public void forTypeVariable(TypeVariable that) {
    forReferenceType(that);
  }

  public void forVoidReturn(VoidReturn that) {
    forJExpression(that);
  }

  public void forSwitchCase(SwitchCase that) {
    forJExpression(that);
  }

  public void forLabeledCase(LabeledCase that) {
    forSwitchCase(that);
  }

  public void forDefaultCase(DefaultCase that) {
    forSwitchCase(that);
  }

  public void forCatchBlock(CatchBlock that) {
    forJExpression(that);
  }

  public void forExpression(Expression that) {
    forJExpression(that);
  }

  public void forAssignmentExpression(AssignmentExpression that) {
    forExpression(that);
  }

  public void forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    forAssignmentExpression(that);
  }

  public void forPlusAssignmentExpression(PlusAssignmentExpression that) {
    forAssignmentExpression(that);
  }

  public void forNumericAssignmentExpression(NumericAssignmentExpression that) {
    forAssignmentExpression(that);
  }

  public void forMinusAssignmentExpression(MinusAssignmentExpression that) {
    forNumericAssignmentExpression(that);
  }

  public void forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    forNumericAssignmentExpression(that);
  }

  public void forDivideAssignmentExpression(DivideAssignmentExpression that) {
    forNumericAssignmentExpression(that);
  }

  public void forModAssignmentExpression(ModAssignmentExpression that) {
    forNumericAssignmentExpression(that);
  }

  public void forShiftAssignmentExpression(ShiftAssignmentExpression that) {
    forAssignmentExpression(that);
  }

  public void forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that) {
    forShiftAssignmentExpression(that);
  }

  public void forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that) {
    forShiftAssignmentExpression(that);
  }

  public void forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that) {
    forShiftAssignmentExpression(that);
  }

  public void forBitwiseAssignmentExpression(BitwiseAssignmentExpression that) {
    forAssignmentExpression(that);
  }

  public void forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that) {
    forBitwiseAssignmentExpression(that);
  }

  public void forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that) {
    forBitwiseAssignmentExpression(that);
  }

  public void forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that) {
    forBitwiseAssignmentExpression(that);
  }

  public void forBinaryExpression(BinaryExpression that) {
    forExpression(that);
  }

  public void forBooleanExpression(BooleanExpression that) {
    forBinaryExpression(that);
  }

  public void forOrExpression(OrExpression that) {
    forBooleanExpression(that);
  }

  public void forAndExpression(AndExpression that) {
    forBooleanExpression(that);
  }

  public void forBitwiseBinaryExpression(BitwiseBinaryExpression that) {
    forBinaryExpression(that);
  }

  public void forBitwiseOrExpression(BitwiseOrExpression that) {
    forBitwiseBinaryExpression(that);
  }

  public void forBitwiseXorExpression(BitwiseXorExpression that) {
    forBitwiseBinaryExpression(that);
  }

  public void forBitwiseAndExpression(BitwiseAndExpression that) {
    forBitwiseBinaryExpression(that);
  }

  public void forEqualityExpression(EqualityExpression that) {
    forBinaryExpression(that);
  }

  public void forEqualsExpression(EqualsExpression that) {
    forEqualityExpression(that);
  }

  public void forNotEqualExpression(NotEqualExpression that) {
    forEqualityExpression(that);
  }

  public void forComparisonExpression(ComparisonExpression that) {
    forBinaryExpression(that);
  }

  public void forLessThanExpression(LessThanExpression that) {
    forComparisonExpression(that);
  }

  public void forLessThanOrEqualExpression(LessThanOrEqualExpression that) {
    forComparisonExpression(that);
  }

  public void forGreaterThanExpression(GreaterThanExpression that) {
    forComparisonExpression(that);
  }

  public void forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that) {
    forComparisonExpression(that);
  }

  public void forShiftBinaryExpression(ShiftBinaryExpression that) {
    forBinaryExpression(that);
  }

  public void forLeftShiftExpression(LeftShiftExpression that) {
    forShiftBinaryExpression(that);
  }

  public void forRightSignedShiftExpression(RightSignedShiftExpression that) {
    forShiftBinaryExpression(that);
  }

  public void forRightUnsignedShiftExpression(RightUnsignedShiftExpression that) {
    forShiftBinaryExpression(that);
  }

  public void forPlusExpression(PlusExpression that) {
    forBinaryExpression(that);
  }

  public void forNumericBinaryExpression(NumericBinaryExpression that) {
    forBinaryExpression(that);
  }

  public void forMinusExpression(MinusExpression that) {
    forNumericBinaryExpression(that);
  }

  public void forMultiplyExpression(MultiplyExpression that) {
    forNumericBinaryExpression(that);
  }

  public void forDivideExpression(DivideExpression that) {
    forNumericBinaryExpression(that);
  }

  public void forModExpression(ModExpression that) {
    forNumericBinaryExpression(that);
  }

  public void forNoOpExpression(NoOpExpression that) {
    forBinaryExpression(that);
  }

  public void forUnaryExpression(UnaryExpression that) {
    forExpression(that);
  }

  public void forIncrementExpression(IncrementExpression that) {
    forUnaryExpression(that);
  }

  public void forPrefixIncrementExpression(PrefixIncrementExpression that) {
    forIncrementExpression(that);
  }

  public void forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    forPrefixIncrementExpression(that);
  }

  public void forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    forPrefixIncrementExpression(that);
  }

  public void forPostfixIncrementExpression(PostfixIncrementExpression that) {
    forIncrementExpression(that);
  }

  public void forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    forPostfixIncrementExpression(that);
  }

  public void forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    forPostfixIncrementExpression(that);
  }

  public void forNumericUnaryExpression(NumericUnaryExpression that) {
    forUnaryExpression(that);
  }

  public void forPositiveExpression(PositiveExpression that) {
    forNumericUnaryExpression(that);
  }

  public void forNegativeExpression(NegativeExpression that) {
    forNumericUnaryExpression(that);
  }

  public void forBitwiseNotExpression(BitwiseNotExpression that) {
    forUnaryExpression(that);
  }

  public void forNotExpression(NotExpression that) {
    forUnaryExpression(that);
  }

  public void forConditionalExpression(ConditionalExpression that) {
    forExpression(that);
  }

  public void forInstanceofExpression(InstanceofExpression that) {
    forExpression(that);
  }

  public void forCastExpression(CastExpression that) {
    forExpression(that);
  }

  public void forPrimary(Primary that) {
    forExpression(that);
  }

  public void forLexicalLiteral(LexicalLiteral that) {
    forPrimary(that);
  }

  public void forIntegerLiteral(IntegerLiteral that) {
    forLexicalLiteral(that);
  }

  public void forLongLiteral(LongLiteral that) {
    forLexicalLiteral(that);
  }

  public void forDoubleLiteral(DoubleLiteral that) {
    forLexicalLiteral(that);
  }

  public void forFloatLiteral(FloatLiteral that) {
    forLexicalLiteral(that);
  }

  public void forBooleanLiteral(BooleanLiteral that) {
    forLexicalLiteral(that);
  }

  public void forCharLiteral(CharLiteral that) {
    forLexicalLiteral(that);
  }

  public void forStringLiteral(StringLiteral that) {
    forLexicalLiteral(that);
  }

  public void forNullLiteral(NullLiteral that) {
    forLexicalLiteral(that);
  }

  public void forInstantiation(Instantiation that) {
    forPrimary(that);
  }

  public void forClassInstantiation(ClassInstantiation that) {
    forInstantiation(that);
  }

  public void forNamedClassInstantiation(NamedClassInstantiation that) {
    forClassInstantiation(that);
  }

  public void forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    forNamedClassInstantiation(that);
  }

  public void forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    forNamedClassInstantiation(that);
  }

  public void forAnonymousClassInstantiation(AnonymousClassInstantiation that) {
    forClassInstantiation(that);
  }

  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    forAnonymousClassInstantiation(that);
  }

  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    forAnonymousClassInstantiation(that);
  }

  public void forArrayInstantiation(ArrayInstantiation that) {
    forInstantiation(that);
  }

  public void forUninitializedArrayInstantiation(UninitializedArrayInstantiation that) {
    forArrayInstantiation(that);
  }

  public void forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiation(that);
  }

  public void forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiation(that);
  }

  public void forInitializedArrayInstantiation(InitializedArrayInstantiation that) {
    forArrayInstantiation(that);
  }

  public void forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    forInitializedArrayInstantiation(that);
  }

  public void forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    forInitializedArrayInstantiation(that);
  }

  public void forVariableReference(VariableReference that) {
    forPrimary(that);
  }

  public void forNameReference(NameReference that) {
    forVariableReference(that);
  }

  public void forSimpleNameReference(SimpleNameReference that) {
    forNameReference(that);
  }

  public void forComplexNameReference(ComplexNameReference that) {
    forNameReference(that);
  }

  public void forThisReference(ThisReference that) {
    forVariableReference(that);
  }

  public void forSimpleThisReference(SimpleThisReference that) {
    forThisReference(that);
  }

  public void forComplexThisReference(ComplexThisReference that) {
    forThisReference(that);
  }

  public void forSuperReference(SuperReference that) {
    forVariableReference(that);
  }

  public void forSimpleSuperReference(SimpleSuperReference that) {
    forSuperReference(that);
  }

  public void forComplexSuperReference(ComplexSuperReference that) {
    forSuperReference(that);
  }

  public void forFunctionInvocation(FunctionInvocation that) {
    forPrimary(that);
  }

  public void forMethodInvocation(MethodInvocation that) {
    forFunctionInvocation(that);
  }

  public void forSimpleMethodInvocation(SimpleMethodInvocation that) {
    forMethodInvocation(that);
  }

  public void forComplexMethodInvocation(ComplexMethodInvocation that) {
    forMethodInvocation(that);
  }

  public void forThisConstructorInvocation(ThisConstructorInvocation that) {
    forFunctionInvocation(that);
  }

  public void forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    forThisConstructorInvocation(that);
  }

  public void forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    forThisConstructorInvocation(that);
  }

  public void forSuperConstructorInvocation(SuperConstructorInvocation that) {
    forFunctionInvocation(that);
  }

  public void forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that) {
    forSuperConstructorInvocation(that);
  }

  public void forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that) {
    forSuperConstructorInvocation(that);
  }

  public void forClassLiteral(ClassLiteral that) {
    forPrimary(that);
  }

  public void forArrayAccess(ArrayAccess that) {
    forPrimary(that);
  }

  public void forParenthesized(Parenthesized that) {
    forPrimary(that);
  }

  public void forEmptyExpression(EmptyExpression that) {
    forPrimary(that);
  }

  public void forBody(Body that) {
    forJExpression(that);
  }

  public void forBracedBody(BracedBody that) {
    forBody(that);
  }

  public void forUnbracedBody(UnbracedBody that) {
    forBody(that);
  }

  public void forExpressionList(ExpressionList that) {
    forJExpression(that);
  }

  public void forParenthesizedExpressionList(ParenthesizedExpressionList that) {
    forExpressionList(that);
  }

  public void forUnparenthesizedExpressionList(UnparenthesizedExpressionList that) {
    forExpressionList(that);
  }

  public void forDimensionExpressionList(DimensionExpressionList that) {
    forExpressionList(that);
  }

  public void forEmptyForCondition(EmptyForCondition that) {
    forJExpression(that);
  }

  /** This method is called by default from cases that do not 
   ** override forCASEOnly.
  **/
  protected void defaultCase(JExpressionIF that) {}
}
