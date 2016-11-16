package edu.rice.cs.javalanglevels.tree;

/** An abstract implementation of a visitor over JExpressionIF that does not return a value.
 ** This visitor implements the visitor interface with methods that 
 ** first visit children, and then call visitCASEOnly().
 ** (CASE is replaced by the case name.)
 ** The default implementation of the forCASEOnly methods call 
 ** protected method defaultCase(). This method defaults to no-op.
 **/
public class JExpressionIFDepthFirstVisitor_void implements JExpressionIFVisitor_void {
  /* Methods to visit an item. */
  public void forJExpressionDoFirst(JExpression that) {
    defaultDoFirst(that);
  }

  public void forJExpressionOnly(JExpression that) {
    defaultCase(that);
  }

  public void forSourceFileDoFirst(SourceFile that) {
    forJExpressionDoFirst(that);
  }

  public void forSourceFileOnly(SourceFile that) {
    forJExpressionOnly(that);
  }

  public void forModifiersAndVisibilityDoFirst(ModifiersAndVisibility that) {
    forJExpressionDoFirst(that);
  }

  public void forModifiersAndVisibilityOnly(ModifiersAndVisibility that) {
    forJExpressionOnly(that);
  }

  public void forCompoundWordDoFirst(CompoundWord that) {
    forJExpressionDoFirst(that);
  }

  public void forCompoundWordOnly(CompoundWord that) {
    forJExpressionOnly(that);
  }

  public void forWordDoFirst(Word that) {
    forJExpressionDoFirst(that);
  }

  public void forWordOnly(Word that) {
    forJExpressionOnly(that);
  }

  public void forTypeDefBaseDoFirst(TypeDefBase that) {
    forJExpressionDoFirst(that);
  }

  public void forTypeDefBaseOnly(TypeDefBase that) {
    forJExpressionOnly(that);
  }

  public void forClassDefDoFirst(ClassDef that) {
    forTypeDefBaseDoFirst(that);
  }

  public void forClassDefOnly(ClassDef that) {
    forTypeDefBaseOnly(that);
  }

  public void forInnerClassDefDoFirst(InnerClassDef that) {
    forClassDefDoFirst(that);
  }

  public void forInnerClassDefOnly(InnerClassDef that) {
    forClassDefOnly(that);
  }

  public void forInterfaceDefDoFirst(InterfaceDef that) {
    forTypeDefBaseDoFirst(that);
  }

  public void forInterfaceDefOnly(InterfaceDef that) {
    forTypeDefBaseOnly(that);
  }

  public void forInnerInterfaceDefDoFirst(InnerInterfaceDef that) {
    forInterfaceDefDoFirst(that);
  }

  public void forInnerInterfaceDefOnly(InnerInterfaceDef that) {
    forInterfaceDefOnly(that);
  }

  public void forConstructorDefDoFirst(ConstructorDef that) {
    forJExpressionDoFirst(that);
  }

  public void forConstructorDefOnly(ConstructorDef that) {
    forJExpressionOnly(that);
  }

  public void forInitializerDoFirst(Initializer that) {
    forJExpressionDoFirst(that);
  }

  public void forInitializerOnly(Initializer that) {
    forJExpressionOnly(that);
  }

  public void forInstanceInitializerDoFirst(InstanceInitializer that) {
    forInitializerDoFirst(that);
  }

  public void forInstanceInitializerOnly(InstanceInitializer that) {
    forInitializerOnly(that);
  }

  public void forStaticInitializerDoFirst(StaticInitializer that) {
    forInitializerDoFirst(that);
  }

  public void forStaticInitializerOnly(StaticInitializer that) {
    forInitializerOnly(that);
  }

  public void forPackageStatementDoFirst(PackageStatement that) {
    forJExpressionDoFirst(that);
  }

  public void forPackageStatementOnly(PackageStatement that) {
    forJExpressionOnly(that);
  }

  public void forImportStatementDoFirst(ImportStatement that) {
    forJExpressionDoFirst(that);
  }

  public void forImportStatementOnly(ImportStatement that) {
    forJExpressionOnly(that);
  }

  public void forClassImportStatementDoFirst(ClassImportStatement that) {
    forImportStatementDoFirst(that);
  }

  public void forClassImportStatementOnly(ClassImportStatement that) {
    forImportStatementOnly(that);
  }

  public void forPackageImportStatementDoFirst(PackageImportStatement that) {
    forImportStatementDoFirst(that);
  }

  public void forPackageImportStatementOnly(PackageImportStatement that) {
    forImportStatementOnly(that);
  }

  public void forStatementDoFirst(Statement that) {
    forJExpressionDoFirst(that);
  }

  public void forStatementOnly(Statement that) {
    forJExpressionOnly(that);
  }

  public void forLabeledStatementDoFirst(LabeledStatement that) {
    forStatementDoFirst(that);
  }

  public void forLabeledStatementOnly(LabeledStatement that) {
    forStatementOnly(that);
  }

  public void forBlockDoFirst(Block that) {
    forStatementDoFirst(that);
  }

  public void forBlockOnly(Block that) {
    forStatementOnly(that);
  }

  public void forExpressionStatementDoFirst(ExpressionStatement that) {
    forStatementDoFirst(that);
  }

  public void forExpressionStatementOnly(ExpressionStatement that) {
    forStatementOnly(that);
  }

  public void forSwitchStatementDoFirst(SwitchStatement that) {
    forStatementDoFirst(that);
  }

  public void forSwitchStatementOnly(SwitchStatement that) {
    forStatementOnly(that);
  }

  public void forIfThenStatementDoFirst(IfThenStatement that) {
    forStatementDoFirst(that);
  }

  public void forIfThenStatementOnly(IfThenStatement that) {
    forStatementOnly(that);
  }

  public void forIfThenElseStatementDoFirst(IfThenElseStatement that) {
    forIfThenStatementDoFirst(that);
  }

  public void forIfThenElseStatementOnly(IfThenElseStatement that) {
    forIfThenStatementOnly(that);
  }

  public void forWhileStatementDoFirst(WhileStatement that) {
    forStatementDoFirst(that);
  }

  public void forWhileStatementOnly(WhileStatement that) {
    forStatementOnly(that);
  }

  public void forDoStatementDoFirst(DoStatement that) {
    forStatementDoFirst(that);
  }

  public void forDoStatementOnly(DoStatement that) {
    forStatementOnly(that);
  }

  public void forForStatementDoFirst(ForStatement that) {
    forStatementDoFirst(that);
  }

  public void forForStatementOnly(ForStatement that) {
    forStatementOnly(that);
  }

  public void forBreakStatementDoFirst(BreakStatement that) {
    forStatementDoFirst(that);
  }

  public void forBreakStatementOnly(BreakStatement that) {
    forStatementOnly(that);
  }

  public void forLabeledBreakStatementDoFirst(LabeledBreakStatement that) {
    forBreakStatementDoFirst(that);
  }

  public void forLabeledBreakStatementOnly(LabeledBreakStatement that) {
    forBreakStatementOnly(that);
  }

  public void forUnlabeledBreakStatementDoFirst(UnlabeledBreakStatement that) {
    forBreakStatementDoFirst(that);
  }

  public void forUnlabeledBreakStatementOnly(UnlabeledBreakStatement that) {
    forBreakStatementOnly(that);
  }

  public void forContinueStatementDoFirst(ContinueStatement that) {
    forStatementDoFirst(that);
  }

  public void forContinueStatementOnly(ContinueStatement that) {
    forStatementOnly(that);
  }

  public void forLabeledContinueStatementDoFirst(LabeledContinueStatement that) {
    forContinueStatementDoFirst(that);
  }

  public void forLabeledContinueStatementOnly(LabeledContinueStatement that) {
    forContinueStatementOnly(that);
  }

  public void forUnlabeledContinueStatementDoFirst(UnlabeledContinueStatement that) {
    forContinueStatementDoFirst(that);
  }

  public void forUnlabeledContinueStatementOnly(UnlabeledContinueStatement that) {
    forContinueStatementOnly(that);
  }

  public void forReturnStatementDoFirst(ReturnStatement that) {
    forStatementDoFirst(that);
  }

  public void forReturnStatementOnly(ReturnStatement that) {
    forStatementOnly(that);
  }

  public void forVoidReturnStatementDoFirst(VoidReturnStatement that) {
    forReturnStatementDoFirst(that);
  }

  public void forVoidReturnStatementOnly(VoidReturnStatement that) {
    forReturnStatementOnly(that);
  }

  public void forValueReturnStatementDoFirst(ValueReturnStatement that) {
    forReturnStatementDoFirst(that);
  }

  public void forValueReturnStatementOnly(ValueReturnStatement that) {
    forReturnStatementOnly(that);
  }

  public void forThrowStatementDoFirst(ThrowStatement that) {
    forStatementDoFirst(that);
  }

  public void forThrowStatementOnly(ThrowStatement that) {
    forStatementOnly(that);
  }

  public void forSynchronizedStatementDoFirst(SynchronizedStatement that) {
    forStatementDoFirst(that);
  }

  public void forSynchronizedStatementOnly(SynchronizedStatement that) {
    forStatementOnly(that);
  }

  public void forTryCatchStatementDoFirst(TryCatchStatement that) {
    forStatementDoFirst(that);
  }

  public void forTryCatchStatementOnly(TryCatchStatement that) {
    forStatementOnly(that);
  }

  public void forTryCatchFinallyStatementDoFirst(TryCatchFinallyStatement that) {
    forTryCatchStatementDoFirst(that);
  }

  public void forTryCatchFinallyStatementOnly(TryCatchFinallyStatement that) {
    forTryCatchStatementOnly(that);
  }

  public void forNormalTryCatchStatementDoFirst(NormalTryCatchStatement that) {
    forTryCatchStatementDoFirst(that);
  }

  public void forNormalTryCatchStatementOnly(NormalTryCatchStatement that) {
    forTryCatchStatementOnly(that);
  }

  public void forEmptyStatementDoFirst(EmptyStatement that) {
    forStatementDoFirst(that);
  }

  public void forEmptyStatementOnly(EmptyStatement that) {
    forStatementOnly(that);
  }

  public void forMethodDefDoFirst(MethodDef that) {
    forJExpressionDoFirst(that);
  }

  public void forMethodDefOnly(MethodDef that) {
    forJExpressionOnly(that);
  }

  public void forConcreteMethodDefDoFirst(ConcreteMethodDef that) {
    forMethodDefDoFirst(that);
  }

  public void forConcreteMethodDefOnly(ConcreteMethodDef that) {
    forMethodDefOnly(that);
  }

  public void forAbstractMethodDefDoFirst(AbstractMethodDef that) {
    forMethodDefDoFirst(that);
  }

  public void forAbstractMethodDefOnly(AbstractMethodDef that) {
    forMethodDefOnly(that);
  }

  public void forFormalParameterDoFirst(FormalParameter that) {
    forJExpressionDoFirst(that);
  }

  public void forFormalParameterOnly(FormalParameter that) {
    forJExpressionOnly(that);
  }

  public void forVariableDeclarationDoFirst(VariableDeclaration that) {
    forJExpressionDoFirst(that);
  }

  public void forVariableDeclarationOnly(VariableDeclaration that) {
    forJExpressionOnly(that);
  }

  public void forVariableDeclaratorDoFirst(VariableDeclarator that) {
    forJExpressionDoFirst(that);
  }

  public void forVariableDeclaratorOnly(VariableDeclarator that) {
    forJExpressionOnly(that);
  }

  public void forUninitializedVariableDeclaratorDoFirst(UninitializedVariableDeclarator that) {
    forVariableDeclaratorDoFirst(that);
  }

  public void forUninitializedVariableDeclaratorOnly(UninitializedVariableDeclarator that) {
    forVariableDeclaratorOnly(that);
  }

  public void forInitializedVariableDeclaratorDoFirst(InitializedVariableDeclarator that) {
    forVariableDeclaratorDoFirst(that);
  }

  public void forInitializedVariableDeclaratorOnly(InitializedVariableDeclarator that) {
    forVariableDeclaratorOnly(that);
  }

  public void forTypeParameterDoFirst(TypeParameter that) {
    forJExpressionDoFirst(that);
  }

  public void forTypeParameterOnly(TypeParameter that) {
    forJExpressionOnly(that);
  }

  public void forArrayInitializerDoFirst(ArrayInitializer that) {
    forJExpressionDoFirst(that);
  }

  public void forArrayInitializerOnly(ArrayInitializer that) {
    forJExpressionOnly(that);
  }

  public void forTypeDoFirst(Type that) {
    forJExpressionDoFirst(that);
  }

  public void forTypeOnly(Type that) {
    forJExpressionOnly(that);
  }

  public void forPrimitiveTypeDoFirst(PrimitiveType that) {
    forTypeDoFirst(that);
  }

  public void forPrimitiveTypeOnly(PrimitiveType that) {
    forTypeOnly(that);
  }

  public void forArrayTypeDoFirst(ArrayType that) {
    forTypeDoFirst(that);
  }

  public void forArrayTypeOnly(ArrayType that) {
    forTypeOnly(that);
  }

  public void forReferenceTypeDoFirst(ReferenceType that) {
    forTypeDoFirst(that);
  }

  public void forReferenceTypeOnly(ReferenceType that) {
    forTypeOnly(that);
  }

  public void forMemberTypeDoFirst(MemberType that) {
    forReferenceTypeDoFirst(that);
  }

  public void forMemberTypeOnly(MemberType that) {
    forReferenceTypeOnly(that);
  }

  public void forClassOrInterfaceTypeDoFirst(ClassOrInterfaceType that) {
    forReferenceTypeDoFirst(that);
  }

  public void forClassOrInterfaceTypeOnly(ClassOrInterfaceType that) {
    forReferenceTypeOnly(that);
  }

  public void forTypeVariableDoFirst(TypeVariable that) {
    forReferenceTypeDoFirst(that);
  }

  public void forTypeVariableOnly(TypeVariable that) {
    forReferenceTypeOnly(that);
  }

  public void forVoidReturnDoFirst(VoidReturn that) {
    forJExpressionDoFirst(that);
  }

  public void forVoidReturnOnly(VoidReturn that) {
    forJExpressionOnly(that);
  }

  public void forSwitchCaseDoFirst(SwitchCase that) {
    forJExpressionDoFirst(that);
  }

  public void forSwitchCaseOnly(SwitchCase that) {
    forJExpressionOnly(that);
  }

  public void forLabeledCaseDoFirst(LabeledCase that) {
    forSwitchCaseDoFirst(that);
  }

  public void forLabeledCaseOnly(LabeledCase that) {
    forSwitchCaseOnly(that);
  }

  public void forDefaultCaseDoFirst(DefaultCase that) {
    forSwitchCaseDoFirst(that);
  }

  public void forDefaultCaseOnly(DefaultCase that) {
    forSwitchCaseOnly(that);
  }

  public void forCatchBlockDoFirst(CatchBlock that) {
    forJExpressionDoFirst(that);
  }

  public void forCatchBlockOnly(CatchBlock that) {
    forJExpressionOnly(that);
  }

  public void forExpressionDoFirst(Expression that) {
    forJExpressionDoFirst(that);
  }

  public void forExpressionOnly(Expression that) {
    forJExpressionOnly(that);
  }

  public void forAssignmentExpressionDoFirst(AssignmentExpression that) {
    forExpressionDoFirst(that);
  }

  public void forAssignmentExpressionOnly(AssignmentExpression that) {
    forExpressionOnly(that);
  }

  public void forSimpleAssignmentExpressionDoFirst(SimpleAssignmentExpression that) {
    forAssignmentExpressionDoFirst(that);
  }

  public void forSimpleAssignmentExpressionOnly(SimpleAssignmentExpression that) {
    forAssignmentExpressionOnly(that);
  }

  public void forPlusAssignmentExpressionDoFirst(PlusAssignmentExpression that) {
    forAssignmentExpressionDoFirst(that);
  }

  public void forPlusAssignmentExpressionOnly(PlusAssignmentExpression that) {
    forAssignmentExpressionOnly(that);
  }

  public void forNumericAssignmentExpressionDoFirst(NumericAssignmentExpression that) {
    forAssignmentExpressionDoFirst(that);
  }

  public void forNumericAssignmentExpressionOnly(NumericAssignmentExpression that) {
    forAssignmentExpressionOnly(that);
  }

  public void forMinusAssignmentExpressionDoFirst(MinusAssignmentExpression that) {
    forNumericAssignmentExpressionDoFirst(that);
  }

  public void forMinusAssignmentExpressionOnly(MinusAssignmentExpression that) {
    forNumericAssignmentExpressionOnly(that);
  }

  public void forMultiplyAssignmentExpressionDoFirst(MultiplyAssignmentExpression that) {
    forNumericAssignmentExpressionDoFirst(that);
  }

  public void forMultiplyAssignmentExpressionOnly(MultiplyAssignmentExpression that) {
    forNumericAssignmentExpressionOnly(that);
  }

  public void forDivideAssignmentExpressionDoFirst(DivideAssignmentExpression that) {
    forNumericAssignmentExpressionDoFirst(that);
  }

  public void forDivideAssignmentExpressionOnly(DivideAssignmentExpression that) {
    forNumericAssignmentExpressionOnly(that);
  }

  public void forModAssignmentExpressionDoFirst(ModAssignmentExpression that) {
    forNumericAssignmentExpressionDoFirst(that);
  }

  public void forModAssignmentExpressionOnly(ModAssignmentExpression that) {
    forNumericAssignmentExpressionOnly(that);
  }

  public void forShiftAssignmentExpressionDoFirst(ShiftAssignmentExpression that) {
    forAssignmentExpressionDoFirst(that);
  }

  public void forShiftAssignmentExpressionOnly(ShiftAssignmentExpression that) {
    forAssignmentExpressionOnly(that);
  }

  public void forLeftShiftAssignmentExpressionDoFirst(LeftShiftAssignmentExpression that) {
    forShiftAssignmentExpressionDoFirst(that);
  }

  public void forLeftShiftAssignmentExpressionOnly(LeftShiftAssignmentExpression that) {
    forShiftAssignmentExpressionOnly(that);
  }

  public void forRightSignedShiftAssignmentExpressionDoFirst(RightSignedShiftAssignmentExpression that) {
    forShiftAssignmentExpressionDoFirst(that);
  }

  public void forRightSignedShiftAssignmentExpressionOnly(RightSignedShiftAssignmentExpression that) {
    forShiftAssignmentExpressionOnly(that);
  }

  public void forRightUnsignedShiftAssignmentExpressionDoFirst(RightUnsignedShiftAssignmentExpression that) {
    forShiftAssignmentExpressionDoFirst(that);
  }

  public void forRightUnsignedShiftAssignmentExpressionOnly(RightUnsignedShiftAssignmentExpression that) {
    forShiftAssignmentExpressionOnly(that);
  }

  public void forBitwiseAssignmentExpressionDoFirst(BitwiseAssignmentExpression that) {
    forAssignmentExpressionDoFirst(that);
  }

  public void forBitwiseAssignmentExpressionOnly(BitwiseAssignmentExpression that) {
    forAssignmentExpressionOnly(that);
  }

  public void forBitwiseAndAssignmentExpressionDoFirst(BitwiseAndAssignmentExpression that) {
    forBitwiseAssignmentExpressionDoFirst(that);
  }

  public void forBitwiseAndAssignmentExpressionOnly(BitwiseAndAssignmentExpression that) {
    forBitwiseAssignmentExpressionOnly(that);
  }

  public void forBitwiseOrAssignmentExpressionDoFirst(BitwiseOrAssignmentExpression that) {
    forBitwiseAssignmentExpressionDoFirst(that);
  }

  public void forBitwiseOrAssignmentExpressionOnly(BitwiseOrAssignmentExpression that) {
    forBitwiseAssignmentExpressionOnly(that);
  }

  public void forBitwiseXorAssignmentExpressionDoFirst(BitwiseXorAssignmentExpression that) {
    forBitwiseAssignmentExpressionDoFirst(that);
  }

  public void forBitwiseXorAssignmentExpressionOnly(BitwiseXorAssignmentExpression that) {
    forBitwiseAssignmentExpressionOnly(that);
  }

  public void forBinaryExpressionDoFirst(BinaryExpression that) {
    forExpressionDoFirst(that);
  }

  public void forBinaryExpressionOnly(BinaryExpression that) {
    forExpressionOnly(that);
  }

  public void forBooleanExpressionDoFirst(BooleanExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forBooleanExpressionOnly(BooleanExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forOrExpressionDoFirst(OrExpression that) {
    forBooleanExpressionDoFirst(that);
  }

  public void forOrExpressionOnly(OrExpression that) {
    forBooleanExpressionOnly(that);
  }

  public void forAndExpressionDoFirst(AndExpression that) {
    forBooleanExpressionDoFirst(that);
  }

  public void forAndExpressionOnly(AndExpression that) {
    forBooleanExpressionOnly(that);
  }

  public void forBitwiseBinaryExpressionDoFirst(BitwiseBinaryExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forBitwiseBinaryExpressionOnly(BitwiseBinaryExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forBitwiseOrExpressionDoFirst(BitwiseOrExpression that) {
    forBitwiseBinaryExpressionDoFirst(that);
  }

  public void forBitwiseOrExpressionOnly(BitwiseOrExpression that) {
    forBitwiseBinaryExpressionOnly(that);
  }

  public void forBitwiseXorExpressionDoFirst(BitwiseXorExpression that) {
    forBitwiseBinaryExpressionDoFirst(that);
  }

  public void forBitwiseXorExpressionOnly(BitwiseXorExpression that) {
    forBitwiseBinaryExpressionOnly(that);
  }

  public void forBitwiseAndExpressionDoFirst(BitwiseAndExpression that) {
    forBitwiseBinaryExpressionDoFirst(that);
  }

  public void forBitwiseAndExpressionOnly(BitwiseAndExpression that) {
    forBitwiseBinaryExpressionOnly(that);
  }

  public void forEqualityExpressionDoFirst(EqualityExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forEqualityExpressionOnly(EqualityExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forEqualsExpressionDoFirst(EqualsExpression that) {
    forEqualityExpressionDoFirst(that);
  }

  public void forEqualsExpressionOnly(EqualsExpression that) {
    forEqualityExpressionOnly(that);
  }

  public void forNotEqualExpressionDoFirst(NotEqualExpression that) {
    forEqualityExpressionDoFirst(that);
  }

  public void forNotEqualExpressionOnly(NotEqualExpression that) {
    forEqualityExpressionOnly(that);
  }

  public void forComparisonExpressionDoFirst(ComparisonExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forComparisonExpressionOnly(ComparisonExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forLessThanExpressionDoFirst(LessThanExpression that) {
    forComparisonExpressionDoFirst(that);
  }

  public void forLessThanExpressionOnly(LessThanExpression that) {
    forComparisonExpressionOnly(that);
  }

  public void forLessThanOrEqualExpressionDoFirst(LessThanOrEqualExpression that) {
    forComparisonExpressionDoFirst(that);
  }

  public void forLessThanOrEqualExpressionOnly(LessThanOrEqualExpression that) {
    forComparisonExpressionOnly(that);
  }

  public void forGreaterThanExpressionDoFirst(GreaterThanExpression that) {
    forComparisonExpressionDoFirst(that);
  }

  public void forGreaterThanExpressionOnly(GreaterThanExpression that) {
    forComparisonExpressionOnly(that);
  }

  public void forGreaterThanOrEqualExpressionDoFirst(GreaterThanOrEqualExpression that) {
    forComparisonExpressionDoFirst(that);
  }

  public void forGreaterThanOrEqualExpressionOnly(GreaterThanOrEqualExpression that) {
    forComparisonExpressionOnly(that);
  }

  public void forShiftBinaryExpressionDoFirst(ShiftBinaryExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forShiftBinaryExpressionOnly(ShiftBinaryExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forLeftShiftExpressionDoFirst(LeftShiftExpression that) {
    forShiftBinaryExpressionDoFirst(that);
  }

  public void forLeftShiftExpressionOnly(LeftShiftExpression that) {
    forShiftBinaryExpressionOnly(that);
  }

  public void forRightSignedShiftExpressionDoFirst(RightSignedShiftExpression that) {
    forShiftBinaryExpressionDoFirst(that);
  }

  public void forRightSignedShiftExpressionOnly(RightSignedShiftExpression that) {
    forShiftBinaryExpressionOnly(that);
  }

  public void forRightUnsignedShiftExpressionDoFirst(RightUnsignedShiftExpression that) {
    forShiftBinaryExpressionDoFirst(that);
  }

  public void forRightUnsignedShiftExpressionOnly(RightUnsignedShiftExpression that) {
    forShiftBinaryExpressionOnly(that);
  }

  public void forPlusExpressionDoFirst(PlusExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forPlusExpressionOnly(PlusExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forNumericBinaryExpressionDoFirst(NumericBinaryExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forNumericBinaryExpressionOnly(NumericBinaryExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forMinusExpressionDoFirst(MinusExpression that) {
    forNumericBinaryExpressionDoFirst(that);
  }

  public void forMinusExpressionOnly(MinusExpression that) {
    forNumericBinaryExpressionOnly(that);
  }

  public void forMultiplyExpressionDoFirst(MultiplyExpression that) {
    forNumericBinaryExpressionDoFirst(that);
  }

  public void forMultiplyExpressionOnly(MultiplyExpression that) {
    forNumericBinaryExpressionOnly(that);
  }

  public void forDivideExpressionDoFirst(DivideExpression that) {
    forNumericBinaryExpressionDoFirst(that);
  }

  public void forDivideExpressionOnly(DivideExpression that) {
    forNumericBinaryExpressionOnly(that);
  }

  public void forModExpressionDoFirst(ModExpression that) {
    forNumericBinaryExpressionDoFirst(that);
  }

  public void forModExpressionOnly(ModExpression that) {
    forNumericBinaryExpressionOnly(that);
  }

  public void forNoOpExpressionDoFirst(NoOpExpression that) {
    forBinaryExpressionDoFirst(that);
  }

  public void forNoOpExpressionOnly(NoOpExpression that) {
    forBinaryExpressionOnly(that);
  }

  public void forUnaryExpressionDoFirst(UnaryExpression that) {
    forExpressionDoFirst(that);
  }

  public void forUnaryExpressionOnly(UnaryExpression that) {
    forExpressionOnly(that);
  }

  public void forIncrementExpressionDoFirst(IncrementExpression that) {
    forUnaryExpressionDoFirst(that);
  }

  public void forIncrementExpressionOnly(IncrementExpression that) {
    forUnaryExpressionOnly(that);
  }

  public void forPrefixIncrementExpressionDoFirst(PrefixIncrementExpression that) {
    forIncrementExpressionDoFirst(that);
  }

  public void forPrefixIncrementExpressionOnly(PrefixIncrementExpression that) {
    forIncrementExpressionOnly(that);
  }

  public void forPositivePrefixIncrementExpressionDoFirst(PositivePrefixIncrementExpression that) {
    forPrefixIncrementExpressionDoFirst(that);
  }

  public void forPositivePrefixIncrementExpressionOnly(PositivePrefixIncrementExpression that) {
    forPrefixIncrementExpressionOnly(that);
  }

  public void forNegativePrefixIncrementExpressionDoFirst(NegativePrefixIncrementExpression that) {
    forPrefixIncrementExpressionDoFirst(that);
  }

  public void forNegativePrefixIncrementExpressionOnly(NegativePrefixIncrementExpression that) {
    forPrefixIncrementExpressionOnly(that);
  }

  public void forPostfixIncrementExpressionDoFirst(PostfixIncrementExpression that) {
    forIncrementExpressionDoFirst(that);
  }

  public void forPostfixIncrementExpressionOnly(PostfixIncrementExpression that) {
    forIncrementExpressionOnly(that);
  }

  public void forPositivePostfixIncrementExpressionDoFirst(PositivePostfixIncrementExpression that) {
    forPostfixIncrementExpressionDoFirst(that);
  }

  public void forPositivePostfixIncrementExpressionOnly(PositivePostfixIncrementExpression that) {
    forPostfixIncrementExpressionOnly(that);
  }

  public void forNegativePostfixIncrementExpressionDoFirst(NegativePostfixIncrementExpression that) {
    forPostfixIncrementExpressionDoFirst(that);
  }

  public void forNegativePostfixIncrementExpressionOnly(NegativePostfixIncrementExpression that) {
    forPostfixIncrementExpressionOnly(that);
  }

  public void forNumericUnaryExpressionDoFirst(NumericUnaryExpression that) {
    forUnaryExpressionDoFirst(that);
  }

  public void forNumericUnaryExpressionOnly(NumericUnaryExpression that) {
    forUnaryExpressionOnly(that);
  }

  public void forPositiveExpressionDoFirst(PositiveExpression that) {
    forNumericUnaryExpressionDoFirst(that);
  }

  public void forPositiveExpressionOnly(PositiveExpression that) {
    forNumericUnaryExpressionOnly(that);
  }

  public void forNegativeExpressionDoFirst(NegativeExpression that) {
    forNumericUnaryExpressionDoFirst(that);
  }

  public void forNegativeExpressionOnly(NegativeExpression that) {
    forNumericUnaryExpressionOnly(that);
  }

  public void forBitwiseNotExpressionDoFirst(BitwiseNotExpression that) {
    forUnaryExpressionDoFirst(that);
  }

  public void forBitwiseNotExpressionOnly(BitwiseNotExpression that) {
    forUnaryExpressionOnly(that);
  }

  public void forNotExpressionDoFirst(NotExpression that) {
    forUnaryExpressionDoFirst(that);
  }

  public void forNotExpressionOnly(NotExpression that) {
    forUnaryExpressionOnly(that);
  }

  public void forConditionalExpressionDoFirst(ConditionalExpression that) {
    forExpressionDoFirst(that);
  }

  public void forConditionalExpressionOnly(ConditionalExpression that) {
    forExpressionOnly(that);
  }

  public void forInstanceofExpressionDoFirst(InstanceofExpression that) {
    forExpressionDoFirst(that);
  }

  public void forInstanceofExpressionOnly(InstanceofExpression that) {
    forExpressionOnly(that);
  }

  public void forCastExpressionDoFirst(CastExpression that) {
    forExpressionDoFirst(that);
  }

  public void forCastExpressionOnly(CastExpression that) {
    forExpressionOnly(that);
  }

  public void forPrimaryDoFirst(Primary that) {
    forExpressionDoFirst(that);
  }

  public void forPrimaryOnly(Primary that) {
    forExpressionOnly(that);
  }

  public void forLexicalLiteralDoFirst(LexicalLiteral that) {
    forPrimaryDoFirst(that);
  }

  public void forLexicalLiteralOnly(LexicalLiteral that) {
    forPrimaryOnly(that);
  }

  public void forIntegerLiteralDoFirst(IntegerLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forIntegerLiteralOnly(IntegerLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forLongLiteralDoFirst(LongLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forLongLiteralOnly(LongLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forDoubleLiteralDoFirst(DoubleLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forDoubleLiteralOnly(DoubleLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forFloatLiteralDoFirst(FloatLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forFloatLiteralOnly(FloatLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forBooleanLiteralDoFirst(BooleanLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forBooleanLiteralOnly(BooleanLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forCharLiteralDoFirst(CharLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forCharLiteralOnly(CharLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forStringLiteralDoFirst(StringLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forStringLiteralOnly(StringLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forNullLiteralDoFirst(NullLiteral that) {
    forLexicalLiteralDoFirst(that);
  }

  public void forNullLiteralOnly(NullLiteral that) {
    forLexicalLiteralOnly(that);
  }

  public void forInstantiationDoFirst(Instantiation that) {
    forPrimaryDoFirst(that);
  }

  public void forInstantiationOnly(Instantiation that) {
    forPrimaryOnly(that);
  }

  public void forClassInstantiationDoFirst(ClassInstantiation that) {
    forInstantiationDoFirst(that);
  }

  public void forClassInstantiationOnly(ClassInstantiation that) {
    forInstantiationOnly(that);
  }

  public void forNamedClassInstantiationDoFirst(NamedClassInstantiation that) {
    forClassInstantiationDoFirst(that);
  }

  public void forNamedClassInstantiationOnly(NamedClassInstantiation that) {
    forClassInstantiationOnly(that);
  }

  public void forSimpleNamedClassInstantiationDoFirst(SimpleNamedClassInstantiation that) {
    forNamedClassInstantiationDoFirst(that);
  }

  public void forSimpleNamedClassInstantiationOnly(SimpleNamedClassInstantiation that) {
    forNamedClassInstantiationOnly(that);
  }

  public void forComplexNamedClassInstantiationDoFirst(ComplexNamedClassInstantiation that) {
    forNamedClassInstantiationDoFirst(that);
  }

  public void forComplexNamedClassInstantiationOnly(ComplexNamedClassInstantiation that) {
    forNamedClassInstantiationOnly(that);
  }

  public void forAnonymousClassInstantiationDoFirst(AnonymousClassInstantiation that) {
    forClassInstantiationDoFirst(that);
  }

  public void forAnonymousClassInstantiationOnly(AnonymousClassInstantiation that) {
    forClassInstantiationOnly(that);
  }

  public void forSimpleAnonymousClassInstantiationDoFirst(SimpleAnonymousClassInstantiation that) {
    forAnonymousClassInstantiationDoFirst(that);
  }

  public void forSimpleAnonymousClassInstantiationOnly(SimpleAnonymousClassInstantiation that) {
    forAnonymousClassInstantiationOnly(that);
  }

  public void forComplexAnonymousClassInstantiationDoFirst(ComplexAnonymousClassInstantiation that) {
    forAnonymousClassInstantiationDoFirst(that);
  }

  public void forComplexAnonymousClassInstantiationOnly(ComplexAnonymousClassInstantiation that) {
    forAnonymousClassInstantiationOnly(that);
  }

  public void forArrayInstantiationDoFirst(ArrayInstantiation that) {
    forInstantiationDoFirst(that);
  }

  public void forArrayInstantiationOnly(ArrayInstantiation that) {
    forInstantiationOnly(that);
  }

  public void forUninitializedArrayInstantiationDoFirst(UninitializedArrayInstantiation that) {
    forArrayInstantiationDoFirst(that);
  }

  public void forUninitializedArrayInstantiationOnly(UninitializedArrayInstantiation that) {
    forArrayInstantiationOnly(that);
  }

  public void forSimpleUninitializedArrayInstantiationDoFirst(SimpleUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiationDoFirst(that);
  }

  public void forSimpleUninitializedArrayInstantiationOnly(SimpleUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiationOnly(that);
  }

  public void forComplexUninitializedArrayInstantiationDoFirst(ComplexUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiationDoFirst(that);
  }

  public void forComplexUninitializedArrayInstantiationOnly(ComplexUninitializedArrayInstantiation that) {
    forUninitializedArrayInstantiationOnly(that);
  }

  public void forInitializedArrayInstantiationDoFirst(InitializedArrayInstantiation that) {
    forArrayInstantiationDoFirst(that);
  }

  public void forInitializedArrayInstantiationOnly(InitializedArrayInstantiation that) {
    forArrayInstantiationOnly(that);
  }

  public void forSimpleInitializedArrayInstantiationDoFirst(SimpleInitializedArrayInstantiation that) {
    forInitializedArrayInstantiationDoFirst(that);
  }

  public void forSimpleInitializedArrayInstantiationOnly(SimpleInitializedArrayInstantiation that) {
    forInitializedArrayInstantiationOnly(that);
  }

  public void forComplexInitializedArrayInstantiationDoFirst(ComplexInitializedArrayInstantiation that) {
    forInitializedArrayInstantiationDoFirst(that);
  }

  public void forComplexInitializedArrayInstantiationOnly(ComplexInitializedArrayInstantiation that) {
    forInitializedArrayInstantiationOnly(that);
  }

  public void forVariableReferenceDoFirst(VariableReference that) {
    forPrimaryDoFirst(that);
  }

  public void forVariableReferenceOnly(VariableReference that) {
    forPrimaryOnly(that);
  }

  public void forNameReferenceDoFirst(NameReference that) {
    forVariableReferenceDoFirst(that);
  }

  public void forNameReferenceOnly(NameReference that) {
    forVariableReferenceOnly(that);
  }

  public void forSimpleNameReferenceDoFirst(SimpleNameReference that) {
    forNameReferenceDoFirst(that);
  }

  public void forSimpleNameReferenceOnly(SimpleNameReference that) {
    forNameReferenceOnly(that);
  }

  public void forComplexNameReferenceDoFirst(ComplexNameReference that) {
    forNameReferenceDoFirst(that);
  }

  public void forComplexNameReferenceOnly(ComplexNameReference that) {
    forNameReferenceOnly(that);
  }

  public void forThisReferenceDoFirst(ThisReference that) {
    forVariableReferenceDoFirst(that);
  }

  public void forThisReferenceOnly(ThisReference that) {
    forVariableReferenceOnly(that);
  }

  public void forSimpleThisReferenceDoFirst(SimpleThisReference that) {
    forThisReferenceDoFirst(that);
  }

  public void forSimpleThisReferenceOnly(SimpleThisReference that) {
    forThisReferenceOnly(that);
  }

  public void forComplexThisReferenceDoFirst(ComplexThisReference that) {
    forThisReferenceDoFirst(that);
  }

  public void forComplexThisReferenceOnly(ComplexThisReference that) {
    forThisReferenceOnly(that);
  }

  public void forSuperReferenceDoFirst(SuperReference that) {
    forVariableReferenceDoFirst(that);
  }

  public void forSuperReferenceOnly(SuperReference that) {
    forVariableReferenceOnly(that);
  }

  public void forSimpleSuperReferenceDoFirst(SimpleSuperReference that) {
    forSuperReferenceDoFirst(that);
  }

  public void forSimpleSuperReferenceOnly(SimpleSuperReference that) {
    forSuperReferenceOnly(that);
  }

  public void forComplexSuperReferenceDoFirst(ComplexSuperReference that) {
    forSuperReferenceDoFirst(that);
  }

  public void forComplexSuperReferenceOnly(ComplexSuperReference that) {
    forSuperReferenceOnly(that);
  }

  public void forFunctionInvocationDoFirst(FunctionInvocation that) {
    forPrimaryDoFirst(that);
  }

  public void forFunctionInvocationOnly(FunctionInvocation that) {
    forPrimaryOnly(that);
  }

  public void forMethodInvocationDoFirst(MethodInvocation that) {
    forFunctionInvocationDoFirst(that);
  }

  public void forMethodInvocationOnly(MethodInvocation that) {
    forFunctionInvocationOnly(that);
  }

  public void forSimpleMethodInvocationDoFirst(SimpleMethodInvocation that) {
    forMethodInvocationDoFirst(that);
  }

  public void forSimpleMethodInvocationOnly(SimpleMethodInvocation that) {
    forMethodInvocationOnly(that);
  }

  public void forComplexMethodInvocationDoFirst(ComplexMethodInvocation that) {
    forMethodInvocationDoFirst(that);
  }

  public void forComplexMethodInvocationOnly(ComplexMethodInvocation that) {
    forMethodInvocationOnly(that);
  }

  public void forThisConstructorInvocationDoFirst(ThisConstructorInvocation that) {
    forFunctionInvocationDoFirst(that);
  }

  public void forThisConstructorInvocationOnly(ThisConstructorInvocation that) {
    forFunctionInvocationOnly(that);
  }

  public void forSimpleThisConstructorInvocationDoFirst(SimpleThisConstructorInvocation that) {
    forThisConstructorInvocationDoFirst(that);
  }

  public void forSimpleThisConstructorInvocationOnly(SimpleThisConstructorInvocation that) {
    forThisConstructorInvocationOnly(that);
  }

  public void forComplexThisConstructorInvocationDoFirst(ComplexThisConstructorInvocation that) {
    forThisConstructorInvocationDoFirst(that);
  }

  public void forComplexThisConstructorInvocationOnly(ComplexThisConstructorInvocation that) {
    forThisConstructorInvocationOnly(that);
  }

  public void forSuperConstructorInvocationDoFirst(SuperConstructorInvocation that) {
    forFunctionInvocationDoFirst(that);
  }

  public void forSuperConstructorInvocationOnly(SuperConstructorInvocation that) {
    forFunctionInvocationOnly(that);
  }

  public void forSimpleSuperConstructorInvocationDoFirst(SimpleSuperConstructorInvocation that) {
    forSuperConstructorInvocationDoFirst(that);
  }

  public void forSimpleSuperConstructorInvocationOnly(SimpleSuperConstructorInvocation that) {
    forSuperConstructorInvocationOnly(that);
  }

  public void forComplexSuperConstructorInvocationDoFirst(ComplexSuperConstructorInvocation that) {
    forSuperConstructorInvocationDoFirst(that);
  }

  public void forComplexSuperConstructorInvocationOnly(ComplexSuperConstructorInvocation that) {
    forSuperConstructorInvocationOnly(that);
  }

  public void forClassLiteralDoFirst(ClassLiteral that) {
    forPrimaryDoFirst(that);
  }

  public void forClassLiteralOnly(ClassLiteral that) {
    forPrimaryOnly(that);
  }

  public void forArrayAccessDoFirst(ArrayAccess that) {
    forPrimaryDoFirst(that);
  }

  public void forArrayAccessOnly(ArrayAccess that) {
    forPrimaryOnly(that);
  }

  public void forParenthesizedDoFirst(Parenthesized that) {
    forPrimaryDoFirst(that);
  }

  public void forParenthesizedOnly(Parenthesized that) {
    forPrimaryOnly(that);
  }

  public void forEmptyExpressionDoFirst(EmptyExpression that) {
    forPrimaryDoFirst(that);
  }

  public void forEmptyExpressionOnly(EmptyExpression that) {
    forPrimaryOnly(that);
  }

  public void forBodyDoFirst(Body that) {
    forJExpressionDoFirst(that);
  }

  public void forBodyOnly(Body that) {
    forJExpressionOnly(that);
  }

  public void forBracedBodyDoFirst(BracedBody that) {
    forBodyDoFirst(that);
  }

  public void forBracedBodyOnly(BracedBody that) {
    forBodyOnly(that);
  }

  public void forUnbracedBodyDoFirst(UnbracedBody that) {
    forBodyDoFirst(that);
  }

  public void forUnbracedBodyOnly(UnbracedBody that) {
    forBodyOnly(that);
  }

  public void forExpressionListDoFirst(ExpressionList that) {
    forJExpressionDoFirst(that);
  }

  public void forExpressionListOnly(ExpressionList that) {
    forJExpressionOnly(that);
  }

  public void forParenthesizedExpressionListDoFirst(ParenthesizedExpressionList that) {
    forExpressionListDoFirst(that);
  }

  public void forParenthesizedExpressionListOnly(ParenthesizedExpressionList that) {
    forExpressionListOnly(that);
  }

  public void forUnparenthesizedExpressionListDoFirst(UnparenthesizedExpressionList that) {
    forExpressionListDoFirst(that);
  }

  public void forUnparenthesizedExpressionListOnly(UnparenthesizedExpressionList that) {
    forExpressionListOnly(that);
  }

  public void forDimensionExpressionListDoFirst(DimensionExpressionList that) {
    forExpressionListDoFirst(that);
  }

  public void forDimensionExpressionListOnly(DimensionExpressionList that) {
    forExpressionListOnly(that);
  }

  public void forEmptyForConditionDoFirst(EmptyForCondition that) {
    forJExpressionDoFirst(that);
  }

  public void forEmptyForConditionOnly(EmptyForCondition that) {
    forJExpressionOnly(that);
  }

  /* Implementation of JExpressionIFVisitor_void methods to implement depth-first traversal. */
  public void forSourceFile(SourceFile that) {
    forSourceFileDoFirst(that);
    for (int i = 0; i < that.getPackageStatements().length; i++) that.getPackageStatements()[i].visit(this);
    for (int i = 0; i < that.getImportStatements().length; i++) that.getImportStatements()[i].visit(this);
    for (int i = 0; i < that.getTypes().length; i++) that.getTypes()[i].visit(this);
    forSourceFileOnly(that);
  }

  public void forModifiersAndVisibility(ModifiersAndVisibility that) {
    forModifiersAndVisibilityDoFirst(that);
    forModifiersAndVisibilityOnly(that);
  }

  public void forCompoundWord(CompoundWord that) {
    forCompoundWordDoFirst(that);
    for (int i = 0; i < that.getWords().length; i++) that.getWords()[i].visit(this);
    forCompoundWordOnly(that);
  }

  public void forWord(Word that) {
    forWordDoFirst(that);
    forWordOnly(that);
  }

  public void forClassDef(ClassDef that) {
    forClassDefDoFirst(that);
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    that.getBody().visit(this);
    forClassDefOnly(that);
  }

  public void forInnerClassDef(InnerClassDef that) {
    forInnerClassDefDoFirst(that);
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    that.getSuperclass().visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    that.getBody().visit(this);
    forInnerClassDefOnly(that);
  }

  public void forInterfaceDef(InterfaceDef that) {
    forInterfaceDefDoFirst(that);
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    that.getBody().visit(this);
    forInterfaceDefOnly(that);
  }

  public void forInnerInterfaceDef(InnerInterfaceDef that) {
    forInnerInterfaceDefDoFirst(that);
    that.getMav().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getTypeParameters().length; i++) that.getTypeParameters()[i].visit(this);
    for (int i = 0; i < that.getInterfaces().length; i++) that.getInterfaces()[i].visit(this);
    that.getBody().visit(this);
    forInnerInterfaceDefOnly(that);
  }

  public void forConstructorDef(ConstructorDef that) {
    forConstructorDefDoFirst(that);
    that.getName().visit(this);
    that.getMav().visit(this);
    for (int i = 0; i < that.getParameters().length; i++) that.getParameters()[i].visit(this);
    for (int i = 0; i < that.getThrows().length; i++) that.getThrows()[i].visit(this);
    that.getStatements().visit(this);
    forConstructorDefOnly(that);
  }

  public void forInstanceInitializer(InstanceInitializer that) {
    forInstanceInitializerDoFirst(that);
    that.getCode().visit(this);
    forInstanceInitializerOnly(that);
  }

  public void forStaticInitializer(StaticInitializer that) {
    forStaticInitializerDoFirst(that);
    that.getCode().visit(this);
    forStaticInitializerOnly(that);
  }

  public void forPackageStatement(PackageStatement that) {
    forPackageStatementDoFirst(that);
    that.getCWord().visit(this);
    forPackageStatementOnly(that);
  }

  public void forClassImportStatement(ClassImportStatement that) {
    forClassImportStatementDoFirst(that);
    that.getCWord().visit(this);
    forClassImportStatementOnly(that);
  }

  public void forPackageImportStatement(PackageImportStatement that) {
    forPackageImportStatementDoFirst(that);
    that.getCWord().visit(this);
    forPackageImportStatementOnly(that);
  }

  public void forLabeledStatement(LabeledStatement that) {
    forLabeledStatementDoFirst(that);
    that.getLabel().visit(this);
    that.getStatement().visit(this);
    forLabeledStatementOnly(that);
  }

  public void forBlock(Block that) {
    forBlockDoFirst(that);
    that.getStatements().visit(this);
    forBlockOnly(that);
  }

  public void forExpressionStatement(ExpressionStatement that) {
    forExpressionStatementDoFirst(that);
    that.getExpression().visit(this);
    forExpressionStatementOnly(that);
  }

  public void forSwitchStatement(SwitchStatement that) {
    forSwitchStatementDoFirst(that);
    that.getTest().visit(this);
    for (int i = 0; i < that.getCases().length; i++) that.getCases()[i].visit(this);
    forSwitchStatementOnly(that);
  }

  public void forIfThenStatement(IfThenStatement that) {
    forIfThenStatementDoFirst(that);
    that.getTestExpression().visit(this);
    that.getThenStatement().visit(this);
    forIfThenStatementOnly(that);
  }

  public void forIfThenElseStatement(IfThenElseStatement that) {
    forIfThenElseStatementDoFirst(that);
    that.getTestExpression().visit(this);
    that.getThenStatement().visit(this);
    that.getElseStatement().visit(this);
    forIfThenElseStatementOnly(that);
  }

  public void forWhileStatement(WhileStatement that) {
    forWhileStatementDoFirst(that);
    that.getCondition().visit(this);
    that.getCode().visit(this);
    forWhileStatementOnly(that);
  }

  public void forDoStatement(DoStatement that) {
    forDoStatementDoFirst(that);
    that.getCode().visit(this);
    that.getCondition().visit(this);
    forDoStatementOnly(that);
  }

  public void forForStatement(ForStatement that) {
    forForStatementDoFirst(that);
    that.getInit().visit(this);
    that.getCondition().visit(this);
    that.getUpdate().visit(this);
    that.getCode().visit(this);
    forForStatementOnly(that);
  }

  public void forLabeledBreakStatement(LabeledBreakStatement that) {
    forLabeledBreakStatementDoFirst(that);
    that.getLabel().visit(this);
    forLabeledBreakStatementOnly(that);
  }

  public void forUnlabeledBreakStatement(UnlabeledBreakStatement that) {
    forUnlabeledBreakStatementDoFirst(that);
    forUnlabeledBreakStatementOnly(that);
  }

  public void forLabeledContinueStatement(LabeledContinueStatement that) {
    forLabeledContinueStatementDoFirst(that);
    that.getLabel().visit(this);
    forLabeledContinueStatementOnly(that);
  }

  public void forUnlabeledContinueStatement(UnlabeledContinueStatement that) {
    forUnlabeledContinueStatementDoFirst(that);
    forUnlabeledContinueStatementOnly(that);
  }

  public void forVoidReturnStatement(VoidReturnStatement that) {
    forVoidReturnStatementDoFirst(that);
    forVoidReturnStatementOnly(that);
  }

  public void forValueReturnStatement(ValueReturnStatement that) {
    forValueReturnStatementDoFirst(that);
    that.getValue().visit(this);
    forValueReturnStatementOnly(that);
  }

  public void forThrowStatement(ThrowStatement that) {
    forThrowStatementDoFirst(that);
    that.getThrown().visit(this);
    forThrowStatementOnly(that);
  }

  public void forSynchronizedStatement(SynchronizedStatement that) {
    forSynchronizedStatementDoFirst(that);
    that.getLockExpr().visit(this);
    that.getBlock().visit(this);
    forSynchronizedStatementOnly(that);
  }

  public void forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    forTryCatchFinallyStatementDoFirst(that);
    that.getTryBlock().visit(this);
    for (int i = 0; i < that.getCatchBlocks().length; i++) that.getCatchBlocks()[i].visit(this);
    that.getFinallyBlock().visit(this);
    forTryCatchFinallyStatementOnly(that);
  }

  public void forNormalTryCatchStatement(NormalTryCatchStatement that) {
    forNormalTryCatchStatementDoFirst(that);
    that.getTryBlock().visit(this);
    for (int i = 0; i < that.getCatchBlocks().length; i++) that.getCatchBlocks()[i].visit(this);
    forNormalTryCatchStatementOnly(that);
  }

  public void forEmptyStatement(EmptyStatement that) {
    forEmptyStatementDoFirst(that);
    forEmptyStatementOnly(that);
  }

  public void forConcreteMethodDef(ConcreteMethodDef that) {
    forConcreteMethodDefDoFirst(that);
    that.getMav().visit(this);
    for (int i = 0; i < that.getTypeParams().length; i++) that.getTypeParams()[i].visit(this);
    that.getResult().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getParams().length; i++) that.getParams()[i].visit(this);
    for (int i = 0; i < that.getThrows().length; i++) that.getThrows()[i].visit(this);
    that.getBody().visit(this);
    forConcreteMethodDefOnly(that);
  }

  public void forAbstractMethodDef(AbstractMethodDef that) {
    forAbstractMethodDefDoFirst(that);
    that.getMav().visit(this);
    for (int i = 0; i < that.getTypeParams().length; i++) that.getTypeParams()[i].visit(this);
    that.getResult().visit(this);
    that.getName().visit(this);
    for (int i = 0; i < that.getParams().length; i++) that.getParams()[i].visit(this);
    for (int i = 0; i < that.getThrows().length; i++) that.getThrows()[i].visit(this);
    forAbstractMethodDefOnly(that);
  }

  public void forFormalParameter(FormalParameter that) {
    forFormalParameterDoFirst(that);
    that.getDeclarator().visit(this);
    forFormalParameterOnly(that);
  }

  public void forVariableDeclaration(VariableDeclaration that) {
    forVariableDeclarationDoFirst(that);
    that.getMav().visit(this);
    for (int i = 0; i < that.getDeclarators().length; i++) that.getDeclarators()[i].visit(this);
    forVariableDeclarationOnly(that);
  }

  public void forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    forUninitializedVariableDeclaratorDoFirst(that);
    that.getType().visit(this);
    that.getName().visit(this);
    forUninitializedVariableDeclaratorOnly(that);
  }

  public void forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    forInitializedVariableDeclaratorDoFirst(that);
    that.getType().visit(this);
    that.getName().visit(this);
    that.getInitializer().visit(this);
    forInitializedVariableDeclaratorOnly(that);
  }

  public void forTypeParameter(TypeParameter that) {
    forTypeParameterDoFirst(that);
    that.getVariable().visit(this);
    that.getBound().visit(this);
    forTypeParameterOnly(that);
  }

  public void forArrayInitializer(ArrayInitializer that) {
    forArrayInitializerDoFirst(that);
    for (int i = 0; i < that.getItems().length; i++) that.getItems()[i].visit(this);
    forArrayInitializerOnly(that);
  }

  public void forPrimitiveType(PrimitiveType that) {
    forPrimitiveTypeDoFirst(that);
    forPrimitiveTypeOnly(that);
  }

  public void forArrayType(ArrayType that) {
    forArrayTypeDoFirst(that);
    that.getElementType().visit(this);
    forArrayTypeOnly(that);
  }

  public void forMemberType(MemberType that) {
    forMemberTypeDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forMemberTypeOnly(that);
  }

  public void forClassOrInterfaceType(ClassOrInterfaceType that) {
    forClassOrInterfaceTypeDoFirst(that);
    for (int i = 0; i < that.getTypeArguments().length; i++) that.getTypeArguments()[i].visit(this);
    forClassOrInterfaceTypeOnly(that);
  }

  public void forTypeVariable(TypeVariable that) {
    forTypeVariableDoFirst(that);
    forTypeVariableOnly(that);
  }

  public void forVoidReturn(VoidReturn that) {
    forVoidReturnDoFirst(that);
    forVoidReturnOnly(that);
  }

  public void forLabeledCase(LabeledCase that) {
    forLabeledCaseDoFirst(that);
    that.getLabel().visit(this);
    that.getCode().visit(this);
    forLabeledCaseOnly(that);
  }

  public void forDefaultCase(DefaultCase that) {
    forDefaultCaseDoFirst(that);
    that.getCode().visit(this);
    forDefaultCaseOnly(that);
  }

  public void forCatchBlock(CatchBlock that) {
    forCatchBlockDoFirst(that);
    that.getException().visit(this);
    that.getBlock().visit(this);
    forCatchBlockOnly(that);
  }

  public void forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    forSimpleAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forSimpleAssignmentExpressionOnly(that);
  }

  public void forPlusAssignmentExpression(PlusAssignmentExpression that) {
    forPlusAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forPlusAssignmentExpressionOnly(that);
  }

  public void forMinusAssignmentExpression(MinusAssignmentExpression that) {
    forMinusAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forMinusAssignmentExpressionOnly(that);
  }

  public void forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    forMultiplyAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forMultiplyAssignmentExpressionOnly(that);
  }

  public void forDivideAssignmentExpression(DivideAssignmentExpression that) {
    forDivideAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forDivideAssignmentExpressionOnly(that);
  }

  public void forModAssignmentExpression(ModAssignmentExpression that) {
    forModAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forModAssignmentExpressionOnly(that);
  }

  public void forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that) {
    forLeftShiftAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forLeftShiftAssignmentExpressionOnly(that);
  }

  public void forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that) {
    forRightSignedShiftAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forRightSignedShiftAssignmentExpressionOnly(that);
  }

  public void forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that) {
    forRightUnsignedShiftAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forRightUnsignedShiftAssignmentExpressionOnly(that);
  }

  public void forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that) {
    forBitwiseAndAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forBitwiseAndAssignmentExpressionOnly(that);
  }

  public void forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that) {
    forBitwiseOrAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forBitwiseOrAssignmentExpressionOnly(that);
  }

  public void forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that) {
    forBitwiseXorAssignmentExpressionDoFirst(that);
    that.getName().visit(this);
    that.getValue().visit(this);
    forBitwiseXorAssignmentExpressionOnly(that);
  }

  public void forOrExpression(OrExpression that) {
    forOrExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forOrExpressionOnly(that);
  }

  public void forAndExpression(AndExpression that) {
    forAndExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forAndExpressionOnly(that);
  }

  public void forBitwiseOrExpression(BitwiseOrExpression that) {
    forBitwiseOrExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forBitwiseOrExpressionOnly(that);
  }

  public void forBitwiseXorExpression(BitwiseXorExpression that) {
    forBitwiseXorExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forBitwiseXorExpressionOnly(that);
  }

  public void forBitwiseAndExpression(BitwiseAndExpression that) {
    forBitwiseAndExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forBitwiseAndExpressionOnly(that);
  }

  public void forEqualsExpression(EqualsExpression that) {
    forEqualsExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forEqualsExpressionOnly(that);
  }

  public void forNotEqualExpression(NotEqualExpression that) {
    forNotEqualExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forNotEqualExpressionOnly(that);
  }

  public void forLessThanExpression(LessThanExpression that) {
    forLessThanExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forLessThanExpressionOnly(that);
  }

  public void forLessThanOrEqualExpression(LessThanOrEqualExpression that) {
    forLessThanOrEqualExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forLessThanOrEqualExpressionOnly(that);
  }

  public void forGreaterThanExpression(GreaterThanExpression that) {
    forGreaterThanExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forGreaterThanExpressionOnly(that);
  }

  public void forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that) {
    forGreaterThanOrEqualExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forGreaterThanOrEqualExpressionOnly(that);
  }

  public void forLeftShiftExpression(LeftShiftExpression that) {
    forLeftShiftExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forLeftShiftExpressionOnly(that);
  }

  public void forRightSignedShiftExpression(RightSignedShiftExpression that) {
    forRightSignedShiftExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forRightSignedShiftExpressionOnly(that);
  }

  public void forRightUnsignedShiftExpression(RightUnsignedShiftExpression that) {
    forRightUnsignedShiftExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forRightUnsignedShiftExpressionOnly(that);
  }

  public void forPlusExpression(PlusExpression that) {
    forPlusExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forPlusExpressionOnly(that);
  }

  public void forMinusExpression(MinusExpression that) {
    forMinusExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forMinusExpressionOnly(that);
  }

  public void forMultiplyExpression(MultiplyExpression that) {
    forMultiplyExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forMultiplyExpressionOnly(that);
  }

  public void forDivideExpression(DivideExpression that) {
    forDivideExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forDivideExpressionOnly(that);
  }

  public void forModExpression(ModExpression that) {
    forModExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forModExpressionOnly(that);
  }

  public void forNoOpExpression(NoOpExpression that) {
    forNoOpExpressionDoFirst(that);
    that.getLeft().visit(this);
    that.getRight().visit(this);
    forNoOpExpressionOnly(that);
  }

  public void forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    forPositivePrefixIncrementExpressionDoFirst(that);
    that.getValue().visit(this);
    forPositivePrefixIncrementExpressionOnly(that);
  }

  public void forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    forNegativePrefixIncrementExpressionDoFirst(that);
    that.getValue().visit(this);
    forNegativePrefixIncrementExpressionOnly(that);
  }

  public void forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    forPositivePostfixIncrementExpressionDoFirst(that);
    that.getValue().visit(this);
    forPositivePostfixIncrementExpressionOnly(that);
  }

  public void forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    forNegativePostfixIncrementExpressionDoFirst(that);
    that.getValue().visit(this);
    forNegativePostfixIncrementExpressionOnly(that);
  }

  public void forPositiveExpression(PositiveExpression that) {
    forPositiveExpressionDoFirst(that);
    that.getValue().visit(this);
    forPositiveExpressionOnly(that);
  }

  public void forNegativeExpression(NegativeExpression that) {
    forNegativeExpressionDoFirst(that);
    that.getValue().visit(this);
    forNegativeExpressionOnly(that);
  }

  public void forBitwiseNotExpression(BitwiseNotExpression that) {
    forBitwiseNotExpressionDoFirst(that);
    that.getValue().visit(this);
    forBitwiseNotExpressionOnly(that);
  }

  public void forNotExpression(NotExpression that) {
    forNotExpressionDoFirst(that);
    that.getValue().visit(this);
    forNotExpressionOnly(that);
  }

  public void forConditionalExpression(ConditionalExpression that) {
    forConditionalExpressionDoFirst(that);
    that.getCondition().visit(this);
    that.getForTrue().visit(this);
    that.getForFalse().visit(this);
    forConditionalExpressionOnly(that);
  }

  public void forInstanceofExpression(InstanceofExpression that) {
    forInstanceofExpressionDoFirst(that);
    that.getValue().visit(this);
    that.getType().visit(this);
    forInstanceofExpressionOnly(that);
  }

  public void forCastExpression(CastExpression that) {
    forCastExpressionDoFirst(that);
    that.getType().visit(this);
    that.getValue().visit(this);
    forCastExpressionOnly(that);
  }

  public void forIntegerLiteral(IntegerLiteral that) {
    forIntegerLiteralDoFirst(that);
    forIntegerLiteralOnly(that);
  }

  public void forLongLiteral(LongLiteral that) {
    forLongLiteralDoFirst(that);
    forLongLiteralOnly(that);
  }

  public void forDoubleLiteral(DoubleLiteral that) {
    forDoubleLiteralDoFirst(that);
    forDoubleLiteralOnly(that);
  }

  public void forFloatLiteral(FloatLiteral that) {
    forFloatLiteralDoFirst(that);
    forFloatLiteralOnly(that);
  }

  public void forBooleanLiteral(BooleanLiteral that) {
    forBooleanLiteralDoFirst(that);
    forBooleanLiteralOnly(that);
  }

  public void forCharLiteral(CharLiteral that) {
    forCharLiteralDoFirst(that);
    forCharLiteralOnly(that);
  }

  public void forStringLiteral(StringLiteral that) {
    forStringLiteralDoFirst(that);
    forStringLiteralOnly(that);
  }

  public void forNullLiteral(NullLiteral that) {
    forNullLiteralDoFirst(that);
    forNullLiteralOnly(that);
  }

  public void forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    forSimpleNamedClassInstantiationDoFirst(that);
    that.getType().visit(this);
    that.getArguments().visit(this);
    forSimpleNamedClassInstantiationOnly(that);
  }

  public void forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    forComplexNamedClassInstantiationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getType().visit(this);
    that.getArguments().visit(this);
    forComplexNamedClassInstantiationOnly(that);
  }

  public void forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    forSimpleAnonymousClassInstantiationDoFirst(that);
    that.getType().visit(this);
    that.getArguments().visit(this);
    that.getBody().visit(this);
    forSimpleAnonymousClassInstantiationOnly(that);
  }

  public void forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    forComplexAnonymousClassInstantiationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getType().visit(this);
    that.getArguments().visit(this);
    that.getBody().visit(this);
    forComplexAnonymousClassInstantiationOnly(that);
  }

  public void forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    forSimpleUninitializedArrayInstantiationDoFirst(that);
    that.getType().visit(this);
    that.getDimensionSizes().visit(this);
    forSimpleUninitializedArrayInstantiationOnly(that);
  }

  public void forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    forComplexUninitializedArrayInstantiationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getType().visit(this);
    that.getDimensionSizes().visit(this);
    forComplexUninitializedArrayInstantiationOnly(that);
  }

  public void forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    forSimpleInitializedArrayInstantiationDoFirst(that);
    that.getType().visit(this);
    that.getInitializer().visit(this);
    forSimpleInitializedArrayInstantiationOnly(that);
  }

  public void forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    forComplexInitializedArrayInstantiationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getType().visit(this);
    that.getInitializer().visit(this);
    forComplexInitializedArrayInstantiationOnly(that);
  }

  public void forSimpleNameReference(SimpleNameReference that) {
    forSimpleNameReferenceDoFirst(that);
    that.getName().visit(this);
    forSimpleNameReferenceOnly(that);
  }

  public void forComplexNameReference(ComplexNameReference that) {
    forComplexNameReferenceDoFirst(that);
    that.getEnclosing().visit(this);
    that.getName().visit(this);
    forComplexNameReferenceOnly(that);
  }

  public void forSimpleThisReference(SimpleThisReference that) {
    forSimpleThisReferenceDoFirst(that);
    forSimpleThisReferenceOnly(that);
  }

  public void forComplexThisReference(ComplexThisReference that) {
    forComplexThisReferenceDoFirst(that);
    that.getEnclosing().visit(this);
    forComplexThisReferenceOnly(that);
  }

  public void forSimpleSuperReference(SimpleSuperReference that) {
    forSimpleSuperReferenceDoFirst(that);
    forSimpleSuperReferenceOnly(that);
  }

  public void forComplexSuperReference(ComplexSuperReference that) {
    forComplexSuperReferenceDoFirst(that);
    that.getEnclosing().visit(this);
    forComplexSuperReferenceOnly(that);
  }

  public void forSimpleMethodInvocation(SimpleMethodInvocation that) {
    forSimpleMethodInvocationDoFirst(that);
    that.getName().visit(this);
    that.getArguments().visit(this);
    forSimpleMethodInvocationOnly(that);
  }

  public void forComplexMethodInvocation(ComplexMethodInvocation that) {
    forComplexMethodInvocationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getName().visit(this);
    that.getArguments().visit(this);
    forComplexMethodInvocationOnly(that);
  }

  public void forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    forSimpleThisConstructorInvocationDoFirst(that);
    that.getArguments().visit(this);
    forSimpleThisConstructorInvocationOnly(that);
  }

  public void forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    forComplexThisConstructorInvocationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getArguments().visit(this);
    forComplexThisConstructorInvocationOnly(that);
  }

  public void forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that) {
    forSimpleSuperConstructorInvocationDoFirst(that);
    that.getArguments().visit(this);
    forSimpleSuperConstructorInvocationOnly(that);
  }

  public void forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that) {
    forComplexSuperConstructorInvocationDoFirst(that);
    that.getEnclosing().visit(this);
    that.getArguments().visit(this);
    forComplexSuperConstructorInvocationOnly(that);
  }

  public void forClassLiteral(ClassLiteral that) {
    forClassLiteralDoFirst(that);
    that.getType().visit(this);
    forClassLiteralOnly(that);
  }

  public void forArrayAccess(ArrayAccess that) {
    forArrayAccessDoFirst(that);
    that.getArray().visit(this);
    that.getIndex().visit(this);
    forArrayAccessOnly(that);
  }

  public void forParenthesized(Parenthesized that) {
    forParenthesizedDoFirst(that);
    that.getValue().visit(this);
    forParenthesizedOnly(that);
  }

  public void forEmptyExpression(EmptyExpression that) {
    forEmptyExpressionDoFirst(that);
    forEmptyExpressionOnly(that);
  }

  public void forBracedBody(BracedBody that) {
    forBracedBodyDoFirst(that);
    for (int i = 0; i < that.getStatements().length; i++) that.getStatements()[i].visit(this);
    forBracedBodyOnly(that);
  }

  public void forUnbracedBody(UnbracedBody that) {
    forUnbracedBodyDoFirst(that);
    for (int i = 0; i < that.getStatements().length; i++) that.getStatements()[i].visit(this);
    forUnbracedBodyOnly(that);
  }

  public void forParenthesizedExpressionList(ParenthesizedExpressionList that) {
    forParenthesizedExpressionListDoFirst(that);
    for (int i = 0; i < that.getExpressions().length; i++) that.getExpressions()[i].visit(this);
    forParenthesizedExpressionListOnly(that);
  }

  public void forUnparenthesizedExpressionList(UnparenthesizedExpressionList that) {
    forUnparenthesizedExpressionListDoFirst(that);
    for (int i = 0; i < that.getExpressions().length; i++) that.getExpressions()[i].visit(this);
    forUnparenthesizedExpressionListOnly(that);
  }

  public void forDimensionExpressionList(DimensionExpressionList that) {
    forDimensionExpressionListDoFirst(that);
    for (int i = 0; i < that.getExpressions().length; i++) that.getExpressions()[i].visit(this);
    forDimensionExpressionListOnly(that);
  }

  public void forEmptyForCondition(EmptyForCondition that) {
    forEmptyForConditionDoFirst(that);
    forEmptyForConditionOnly(that);
  }

  /** This method is called by default from cases that do not 
   ** override forCASEDoFirst.
  **/
  protected void defaultDoFirst(JExpressionIF that) {}
  /** This method is called by default from cases that do not 
   ** override forCASEOnly.
  **/
  protected void defaultCase(JExpressionIF that) {}
}
