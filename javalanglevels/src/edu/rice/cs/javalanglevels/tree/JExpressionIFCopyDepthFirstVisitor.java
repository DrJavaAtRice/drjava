package edu.rice.cs.javalanglevels.tree;

/** 
 * An extension of DF visitors that copies as it visits (by default).
 * Override forCASE if you want to transform an AST subtree.
 */
public abstract class JExpressionIFCopyDepthFirstVisitor extends JExpressionIFDepthFirstVisitor<JExpressionIF> {

  protected JExpressionIF[] makeArrayOfRetType(int size) {
    return new JExpressionIF[size];
  }

  /* Methods to visit an item. */
  public JExpressionIF forJExpressionOnly(JExpression that) {
    return defaultCase(that);
  }

  public JExpressionIF forSourceFileOnly(SourceFile that, JExpressionIF[] packageStatements_result, JExpressionIF[] importStatements_result, JExpressionIF[] types_result) {
    return new SourceFile(that.getSourceInfo(), (PackageStatement[])packageStatements_result, (ImportStatement[])importStatements_result, (TypeDefBase[])types_result);
  }

  public JExpressionIF forModifiersAndVisibilityOnly(ModifiersAndVisibility that) {
    return new ModifiersAndVisibility(that.getSourceInfo(), that.getModifiers());
  }

  public JExpressionIF forCompoundWordOnly(CompoundWord that, JExpressionIF[] words_result) {
    return new CompoundWord(that.getSourceInfo(), (Word[])words_result);
  }

  public JExpressionIF forWordOnly(Word that) {
    return new Word(that.getSourceInfo(), that.getText());
  }

  public JExpressionIF forTypeDefBaseOnly(TypeDefBase that, JExpressionIF mav_result, JExpressionIF name_result, JExpressionIF[] typeParameters_result, JExpressionIF[] interfaces_result, JExpressionIF body_result) {
    return defaultCase(that);
  }

  public JExpressionIF forClassDefOnly(ClassDef that, JExpressionIF mav_result, JExpressionIF name_result, JExpressionIF[] typeParameters_result, JExpressionIF superclass_result, JExpressionIF[] interfaces_result, JExpressionIF body_result) {
    return new ClassDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (Word)name_result, (TypeParameter[])typeParameters_result, (ReferenceType)superclass_result, (ReferenceType[])interfaces_result, (BracedBody)body_result);
  }

  public JExpressionIF forInnerClassDefOnly(InnerClassDef that, JExpressionIF mav_result, JExpressionIF name_result, JExpressionIF[] typeParameters_result, JExpressionIF superclass_result, JExpressionIF[] interfaces_result, JExpressionIF body_result) {
    return new InnerClassDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (Word)name_result, (TypeParameter[])typeParameters_result, (ReferenceType)superclass_result, (ReferenceType[])interfaces_result, (BracedBody)body_result);
  }

  public JExpressionIF forInterfaceDefOnly(InterfaceDef that, JExpressionIF mav_result, JExpressionIF name_result, JExpressionIF[] typeParameters_result, JExpressionIF[] interfaces_result, JExpressionIF body_result) {
    return new InterfaceDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (Word)name_result, (TypeParameter[])typeParameters_result, (ReferenceType[])interfaces_result, (BracedBody)body_result);
  }

  public JExpressionIF forInnerInterfaceDefOnly(InnerInterfaceDef that, JExpressionIF mav_result, JExpressionIF name_result, JExpressionIF[] typeParameters_result, JExpressionIF[] interfaces_result, JExpressionIF body_result) {
    return new InnerInterfaceDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (Word)name_result, (TypeParameter[])typeParameters_result, (ReferenceType[])interfaces_result, (BracedBody)body_result);
  }

  public JExpressionIF forConstructorDefOnly(ConstructorDef that, JExpressionIF name_result, JExpressionIF mav_result, JExpressionIF[] parameters_result, JExpressionIF[] throws_result, JExpressionIF statements_result) {
    return new ConstructorDef(that.getSourceInfo(), (Word)name_result, (ModifiersAndVisibility)mav_result, (FormalParameter[])parameters_result, (ReferenceType[])throws_result, (BracedBody)statements_result);
  }

  public JExpressionIF forInitializerOnly(Initializer that, JExpressionIF code_result) {
    return defaultCase(that);
  }

  public JExpressionIF forInstanceInitializerOnly(InstanceInitializer that, JExpressionIF code_result) {
    return new InstanceInitializer(that.getSourceInfo(), (Block)code_result);
  }

  public JExpressionIF forStaticInitializerOnly(StaticInitializer that, JExpressionIF code_result) {
    return new StaticInitializer(that.getSourceInfo(), (Block)code_result);
  }

  public JExpressionIF forPackageStatementOnly(PackageStatement that, JExpressionIF cWord_result) {
    return new PackageStatement(that.getSourceInfo(), (CompoundWord)cWord_result);
  }

  public JExpressionIF forImportStatementOnly(ImportStatement that, JExpressionIF cWord_result) {
    return defaultCase(that);
  }

  public JExpressionIF forClassImportStatementOnly(ClassImportStatement that, JExpressionIF cWord_result) {
    return new ClassImportStatement(that.getSourceInfo(), (CompoundWord)cWord_result);
  }

  public JExpressionIF forPackageImportStatementOnly(PackageImportStatement that, JExpressionIF cWord_result) {
    return new PackageImportStatement(that.getSourceInfo(), (CompoundWord)cWord_result);
  }

  public JExpressionIF forStatementOnly(Statement that) {
    return defaultCase(that);
  }

  public JExpressionIF forLabeledStatementOnly(LabeledStatement that, JExpressionIF label_result, JExpressionIF statement_result) {
    return new LabeledStatement(that.getSourceInfo(), (Word)label_result, (Statement)statement_result);
  }

  public JExpressionIF forBlockOnly(Block that, JExpressionIF statements_result) {
    return new Block(that.getSourceInfo(), (BracedBody)statements_result);
  }

  public JExpressionIF forExpressionStatementOnly(ExpressionStatement that, JExpressionIF expression_result) {
    return new ExpressionStatement(that.getSourceInfo(), (Expression)expression_result);
  }

  public JExpressionIF forSwitchStatementOnly(SwitchStatement that, JExpressionIF test_result, JExpressionIF[] cases_result) {
    return new SwitchStatement(that.getSourceInfo(), (Expression)test_result, (SwitchCase[])cases_result);
  }

  public JExpressionIF forIfThenStatementOnly(IfThenStatement that, JExpressionIF testExpression_result, JExpressionIF thenStatement_result) {
    return new IfThenStatement(that.getSourceInfo(), (Expression)testExpression_result, (Statement)thenStatement_result);
  }

  public JExpressionIF forIfThenElseStatementOnly(IfThenElseStatement that, JExpressionIF testExpression_result, JExpressionIF thenStatement_result, JExpressionIF elseStatement_result) {
    return new IfThenElseStatement(that.getSourceInfo(), (Expression)testExpression_result, (Statement)thenStatement_result, (Statement)elseStatement_result);
  }

  public JExpressionIF forWhileStatementOnly(WhileStatement that, JExpressionIF condition_result, JExpressionIF code_result) {
    return new WhileStatement(that.getSourceInfo(), (Expression)condition_result, (Statement)code_result);
  }

  public JExpressionIF forDoStatementOnly(DoStatement that, JExpressionIF code_result, JExpressionIF condition_result) {
    return new DoStatement(that.getSourceInfo(), (Statement)code_result, (Expression)condition_result);
  }

  public JExpressionIF forForStatementOnly(ForStatement that, JExpressionIF init_result, JExpressionIF condition_result, JExpressionIF update_result, JExpressionIF code_result) {
    return new ForStatement(that.getSourceInfo(), (ForInitI)init_result, (ForConditionI)condition_result, (UnparenthesizedExpressionList)update_result, (Statement)code_result);
  }

  public JExpressionIF forBreakStatementOnly(BreakStatement that) {
    return defaultCase(that);
  }

  public JExpressionIF forLabeledBreakStatementOnly(LabeledBreakStatement that, JExpressionIF label_result) {
    return new LabeledBreakStatement(that.getSourceInfo(), (Word)label_result);
  }

  public JExpressionIF forUnlabeledBreakStatementOnly(UnlabeledBreakStatement that) {
    return new UnlabeledBreakStatement(that.getSourceInfo());
  }

  public JExpressionIF forContinueStatementOnly(ContinueStatement that) {
    return defaultCase(that);
  }

  public JExpressionIF forLabeledContinueStatementOnly(LabeledContinueStatement that, JExpressionIF label_result) {
    return new LabeledContinueStatement(that.getSourceInfo(), (Word)label_result);
  }

  public JExpressionIF forUnlabeledContinueStatementOnly(UnlabeledContinueStatement that) {
    return new UnlabeledContinueStatement(that.getSourceInfo());
  }

  public JExpressionIF forReturnStatementOnly(ReturnStatement that) {
    return defaultCase(that);
  }

  public JExpressionIF forVoidReturnStatementOnly(VoidReturnStatement that) {
    return new VoidReturnStatement(that.getSourceInfo());
  }

  public JExpressionIF forValueReturnStatementOnly(ValueReturnStatement that, JExpressionIF value_result) {
    return new ValueReturnStatement(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forThrowStatementOnly(ThrowStatement that, JExpressionIF thrown_result) {
    return new ThrowStatement(that.getSourceInfo(), (Expression)thrown_result);
  }

  public JExpressionIF forSynchronizedStatementOnly(SynchronizedStatement that, JExpressionIF lockExpr_result, JExpressionIF block_result) {
    return new SynchronizedStatement(that.getSourceInfo(), (Expression)lockExpr_result, (Block)block_result);
  }

  public JExpressionIF forTryCatchStatementOnly(TryCatchStatement that, JExpressionIF tryBlock_result, JExpressionIF[] catchBlocks_result) {
    return defaultCase(that);
  }

  public JExpressionIF forTryCatchFinallyStatementOnly(TryCatchFinallyStatement that, JExpressionIF tryBlock_result, JExpressionIF[] catchBlocks_result, JExpressionIF finallyBlock_result) {
    return new TryCatchFinallyStatement(that.getSourceInfo(), (Block)tryBlock_result, (CatchBlock[])catchBlocks_result, (Block)finallyBlock_result);
  }

  public JExpressionIF forNormalTryCatchStatementOnly(NormalTryCatchStatement that, JExpressionIF tryBlock_result, JExpressionIF[] catchBlocks_result) {
    return new NormalTryCatchStatement(that.getSourceInfo(), (Block)tryBlock_result, (CatchBlock[])catchBlocks_result);
  }

  public JExpressionIF forEmptyStatementOnly(EmptyStatement that) {
    return new EmptyStatement(that.getSourceInfo());
  }

  public JExpressionIF forMethodDefOnly(MethodDef that, JExpressionIF mav_result, JExpressionIF[] typeParams_result, JExpressionIF result_result, JExpressionIF name_result, JExpressionIF[] params_result, JExpressionIF[] throws_result) {
    return defaultCase(that);
  }

  public JExpressionIF forConcreteMethodDefOnly(ConcreteMethodDef that, JExpressionIF mav_result, JExpressionIF[] typeParams_result, JExpressionIF result_result, JExpressionIF name_result, JExpressionIF[] params_result, JExpressionIF[] throws_result, JExpressionIF body_result) {
    return new ConcreteMethodDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (TypeParameter[])typeParams_result, (ReturnTypeI)result_result, (Word)name_result, (FormalParameter[])params_result, (ReferenceType[])throws_result, (BracedBody)body_result);
  }

  public JExpressionIF forAbstractMethodDefOnly(AbstractMethodDef that, JExpressionIF mav_result, JExpressionIF[] typeParams_result, JExpressionIF result_result, JExpressionIF name_result, JExpressionIF[] params_result, JExpressionIF[] throws_result) {
    return new AbstractMethodDef(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (TypeParameter[])typeParams_result, (ReturnTypeI)result_result, (Word)name_result, (FormalParameter[])params_result, (ReferenceType[])throws_result);
  }

  public JExpressionIF forFormalParameterOnly(FormalParameter that, JExpressionIF declarator_result) {
    return new FormalParameter(that.getSourceInfo(), (VariableDeclarator)declarator_result, that.isIsFinal());
  }

  public JExpressionIF forVariableDeclarationOnly(VariableDeclaration that, JExpressionIF mav_result, JExpressionIF[] declarators_result) {
    return new VariableDeclaration(that.getSourceInfo(), (ModifiersAndVisibility)mav_result, (VariableDeclarator[])declarators_result);
  }

  public JExpressionIF forVariableDeclaratorOnly(VariableDeclarator that, JExpressionIF type_result, JExpressionIF name_result) {
    return defaultCase(that);
  }

  public JExpressionIF forUninitializedVariableDeclaratorOnly(UninitializedVariableDeclarator that, JExpressionIF type_result, JExpressionIF name_result) {
    return new UninitializedVariableDeclarator(that.getSourceInfo(), (Type)type_result, (Word)name_result);
  }

  public JExpressionIF forInitializedVariableDeclaratorOnly(InitializedVariableDeclarator that, JExpressionIF type_result, JExpressionIF name_result, JExpressionIF initializer_result) {
    return new InitializedVariableDeclarator(that.getSourceInfo(), (Type)type_result, (Word)name_result, (VariableInitializerI)initializer_result);
  }

  public JExpressionIF forTypeParameterOnly(TypeParameter that, JExpressionIF variable_result, JExpressionIF bound_result) {
    return new TypeParameter(that.getSourceInfo(), (TypeVariable)variable_result, (ReferenceType)bound_result);
  }

  public JExpressionIF forArrayInitializerOnly(ArrayInitializer that, JExpressionIF[] items_result) {
    return new ArrayInitializer(that.getSourceInfo(), (VariableInitializerI[])items_result);
  }

  public JExpressionIF forTypeOnly(Type that) {
    return defaultCase(that);
  }

  public JExpressionIF forPrimitiveTypeOnly(PrimitiveType that) {
    return new PrimitiveType(that.getSourceInfo(), that.getName());
  }

  public JExpressionIF forArrayTypeOnly(ArrayType that, JExpressionIF elementType_result) {
    return new ArrayType(that.getSourceInfo(), that.getName(), (Type)elementType_result);
  }

  public JExpressionIF forReferenceTypeOnly(ReferenceType that) {
    return defaultCase(that);
  }

  public JExpressionIF forMemberTypeOnly(MemberType that, JExpressionIF left_result, JExpressionIF right_result) {
    return new MemberType(that.getSourceInfo(), that.getName(), (ReferenceType)left_result, (ReferenceType)right_result);
  }

  public JExpressionIF forClassOrInterfaceTypeOnly(ClassOrInterfaceType that, JExpressionIF[] typeArguments_result) {
    return new ClassOrInterfaceType(that.getSourceInfo(), that.getName(), (Type[])typeArguments_result);
  }

  public JExpressionIF forTypeVariableOnly(TypeVariable that) {
    return new TypeVariable(that.getSourceInfo(), that.getName());
  }

  public JExpressionIF forVoidReturnOnly(VoidReturn that) {
    return new VoidReturn(that.getSourceInfo(), that.getName());
  }

  public JExpressionIF forSwitchCaseOnly(SwitchCase that, JExpressionIF code_result) {
    return defaultCase(that);
  }

  public JExpressionIF forLabeledCaseOnly(LabeledCase that, JExpressionIF label_result, JExpressionIF code_result) {
    return new LabeledCase(that.getSourceInfo(), (Expression)label_result, (UnbracedBody)code_result);
  }

  public JExpressionIF forDefaultCaseOnly(DefaultCase that, JExpressionIF code_result) {
    return new DefaultCase(that.getSourceInfo(), (UnbracedBody)code_result);
  }

  public JExpressionIF forCatchBlockOnly(CatchBlock that, JExpressionIF exception_result, JExpressionIF block_result) {
    return new CatchBlock(that.getSourceInfo(), (FormalParameter)exception_result, (Block)block_result);
  }

  public JExpressionIF forExpressionOnly(Expression that) {
    return defaultCase(that);
  }

  public JExpressionIF forAssignmentExpressionOnly(AssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleAssignmentExpressionOnly(SimpleAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new SimpleAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forPlusAssignmentExpressionOnly(PlusAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new PlusAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forNumericAssignmentExpressionOnly(NumericAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forMinusAssignmentExpressionOnly(MinusAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new MinusAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forMultiplyAssignmentExpressionOnly(MultiplyAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new MultiplyAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forDivideAssignmentExpressionOnly(DivideAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new DivideAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forModAssignmentExpressionOnly(ModAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new ModAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forShiftAssignmentExpressionOnly(ShiftAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forLeftShiftAssignmentExpressionOnly(LeftShiftAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new LeftShiftAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forRightSignedShiftAssignmentExpressionOnly(RightSignedShiftAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new RightSignedShiftAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forRightUnsignedShiftAssignmentExpressionOnly(RightUnsignedShiftAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new RightUnsignedShiftAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forBitwiseAssignmentExpressionOnly(BitwiseAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forBitwiseAndAssignmentExpressionOnly(BitwiseAndAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new BitwiseAndAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forBitwiseOrAssignmentExpressionOnly(BitwiseOrAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new BitwiseOrAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forBitwiseXorAssignmentExpressionOnly(BitwiseXorAssignmentExpression that, JExpressionIF name_result, JExpressionIF value_result) {
    return new BitwiseXorAssignmentExpression(that.getSourceInfo(), (Expression)name_result, (Expression)value_result);
  }

  public JExpressionIF forBinaryExpressionOnly(BinaryExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forBooleanExpressionOnly(BooleanExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forOrExpressionOnly(OrExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new OrExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forAndExpressionOnly(AndExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new AndExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forBitwiseBinaryExpressionOnly(BitwiseBinaryExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forBitwiseOrExpressionOnly(BitwiseOrExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new BitwiseOrExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forBitwiseXorExpressionOnly(BitwiseXorExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new BitwiseXorExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forBitwiseAndExpressionOnly(BitwiseAndExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new BitwiseAndExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forEqualityExpressionOnly(EqualityExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forEqualsExpressionOnly(EqualsExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new EqualsExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forNotEqualExpressionOnly(NotEqualExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new NotEqualExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forComparisonExpressionOnly(ComparisonExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forLessThanExpressionOnly(LessThanExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new LessThanExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forLessThanOrEqualExpressionOnly(LessThanOrEqualExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new LessThanOrEqualExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forGreaterThanExpressionOnly(GreaterThanExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new GreaterThanExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forGreaterThanOrEqualExpressionOnly(GreaterThanOrEqualExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new GreaterThanOrEqualExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forShiftBinaryExpressionOnly(ShiftBinaryExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forLeftShiftExpressionOnly(LeftShiftExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new LeftShiftExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forRightSignedShiftExpressionOnly(RightSignedShiftExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new RightSignedShiftExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forRightUnsignedShiftExpressionOnly(RightUnsignedShiftExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new RightUnsignedShiftExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forPlusExpressionOnly(PlusExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new PlusExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forNumericBinaryExpressionOnly(NumericBinaryExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return defaultCase(that);
  }

  public JExpressionIF forMinusExpressionOnly(MinusExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new MinusExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forMultiplyExpressionOnly(MultiplyExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new MultiplyExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forDivideExpressionOnly(DivideExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new DivideExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forModExpressionOnly(ModExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new ModExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forNoOpExpressionOnly(NoOpExpression that, JExpressionIF left_result, JExpressionIF right_result) {
    return new NoOpExpression(that.getSourceInfo(), (Expression)left_result, (Expression)right_result);
  }

  public JExpressionIF forUnaryExpressionOnly(UnaryExpression that, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forIncrementExpressionOnly(IncrementExpression that, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forPrefixIncrementExpressionOnly(PrefixIncrementExpression that, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forPositivePrefixIncrementExpressionOnly(PositivePrefixIncrementExpression that, JExpressionIF value_result) {
    return new PositivePrefixIncrementExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forNegativePrefixIncrementExpressionOnly(NegativePrefixIncrementExpression that, JExpressionIF value_result) {
    return new NegativePrefixIncrementExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forPostfixIncrementExpressionOnly(PostfixIncrementExpression that, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forPositivePostfixIncrementExpressionOnly(PositivePostfixIncrementExpression that, JExpressionIF value_result) {
    return new PositivePostfixIncrementExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forNegativePostfixIncrementExpressionOnly(NegativePostfixIncrementExpression that, JExpressionIF value_result) {
    return new NegativePostfixIncrementExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forNumericUnaryExpressionOnly(NumericUnaryExpression that, JExpressionIF value_result) {
    return defaultCase(that);
  }

  public JExpressionIF forPositiveExpressionOnly(PositiveExpression that, JExpressionIF value_result) {
    return new PositiveExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forNegativeExpressionOnly(NegativeExpression that, JExpressionIF value_result) {
    return new NegativeExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forBitwiseNotExpressionOnly(BitwiseNotExpression that, JExpressionIF value_result) {
    return new BitwiseNotExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forNotExpressionOnly(NotExpression that, JExpressionIF value_result) {
    return new NotExpression(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forConditionalExpressionOnly(ConditionalExpression that, JExpressionIF condition_result, JExpressionIF forTrue_result, JExpressionIF forFalse_result) {
    return new ConditionalExpression(that.getSourceInfo(), (Expression)condition_result, (Expression)forTrue_result, (Expression)forFalse_result);
  }

  public JExpressionIF forInstanceofExpressionOnly(InstanceofExpression that, JExpressionIF value_result, JExpressionIF type_result) {
    return new InstanceofExpression(that.getSourceInfo(), (Expression)value_result, (Type)type_result);
  }

  public JExpressionIF forCastExpressionOnly(CastExpression that, JExpressionIF type_result, JExpressionIF value_result) {
    return new CastExpression(that.getSourceInfo(), (Type)type_result, (Expression)value_result);
  }

  public JExpressionIF forPrimaryOnly(Primary that) {
    return defaultCase(that);
  }

  public JExpressionIF forLexicalLiteralOnly(LexicalLiteral that) {
    return defaultCase(that);
  }

  public JExpressionIF forIntegerLiteralOnly(IntegerLiteral that) {
    return new IntegerLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forLongLiteralOnly(LongLiteral that) {
    return new LongLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forDoubleLiteralOnly(DoubleLiteral that) {
    return new DoubleLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forFloatLiteralOnly(FloatLiteral that) {
    return new FloatLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forBooleanLiteralOnly(BooleanLiteral that) {
    return new BooleanLiteral(that.getSourceInfo(), that.isValue());
  }

  public JExpressionIF forCharLiteralOnly(CharLiteral that) {
    return new CharLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forStringLiteralOnly(StringLiteral that) {
    return new StringLiteral(that.getSourceInfo(), that.getValue());
  }

  public JExpressionIF forNullLiteralOnly(NullLiteral that) {
    return new NullLiteral(that.getSourceInfo());
  }

  public JExpressionIF forInstantiationOnly(Instantiation that) {
    return defaultCase(that);
  }

  public JExpressionIF forClassInstantiationOnly(ClassInstantiation that, JExpressionIF type_result, JExpressionIF arguments_result) {
    return defaultCase(that);
  }

  public JExpressionIF forNamedClassInstantiationOnly(NamedClassInstantiation that, JExpressionIF type_result, JExpressionIF arguments_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleNamedClassInstantiationOnly(SimpleNamedClassInstantiation that, JExpressionIF type_result, JExpressionIF arguments_result) {
    return new SimpleNamedClassInstantiation(that.getSourceInfo(), (Type)type_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forComplexNamedClassInstantiationOnly(ComplexNamedClassInstantiation that, JExpressionIF enclosing_result, JExpressionIF type_result, JExpressionIF arguments_result) {
    return new ComplexNamedClassInstantiation(that.getSourceInfo(), (Expression)enclosing_result, (Type)type_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forAnonymousClassInstantiationOnly(AnonymousClassInstantiation that, JExpressionIF type_result, JExpressionIF arguments_result, JExpressionIF body_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleAnonymousClassInstantiationOnly(SimpleAnonymousClassInstantiation that, JExpressionIF type_result, JExpressionIF arguments_result, JExpressionIF body_result) {
    return new SimpleAnonymousClassInstantiation(that.getSourceInfo(), (Type)type_result, (ParenthesizedExpressionList)arguments_result, (BracedBody)body_result);
  }

  public JExpressionIF forComplexAnonymousClassInstantiationOnly(ComplexAnonymousClassInstantiation that, JExpressionIF enclosing_result, JExpressionIF type_result, JExpressionIF arguments_result, JExpressionIF body_result) {
    return new ComplexAnonymousClassInstantiation(that.getSourceInfo(), (Expression)enclosing_result, (Type)type_result, (ParenthesizedExpressionList)arguments_result, (BracedBody)body_result);
  }

  public JExpressionIF forArrayInstantiationOnly(ArrayInstantiation that, JExpressionIF type_result) {
    return defaultCase(that);
  }

  public JExpressionIF forUninitializedArrayInstantiationOnly(UninitializedArrayInstantiation that, JExpressionIF type_result, JExpressionIF dimensionSizes_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleUninitializedArrayInstantiationOnly(SimpleUninitializedArrayInstantiation that, JExpressionIF type_result, JExpressionIF dimensionSizes_result) {
    return new SimpleUninitializedArrayInstantiation(that.getSourceInfo(), (Type)type_result, (DimensionExpressionList)dimensionSizes_result);
  }

  public JExpressionIF forComplexUninitializedArrayInstantiationOnly(ComplexUninitializedArrayInstantiation that, JExpressionIF enclosing_result, JExpressionIF type_result, JExpressionIF dimensionSizes_result) {
    return new ComplexUninitializedArrayInstantiation(that.getSourceInfo(), (Expression)enclosing_result, (Type)type_result, (DimensionExpressionList)dimensionSizes_result);
  }

  public JExpressionIF forInitializedArrayInstantiationOnly(InitializedArrayInstantiation that, JExpressionIF type_result, JExpressionIF initializer_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleInitializedArrayInstantiationOnly(SimpleInitializedArrayInstantiation that, JExpressionIF type_result, JExpressionIF initializer_result) {
    return new SimpleInitializedArrayInstantiation(that.getSourceInfo(), (Type)type_result, (ArrayInitializer)initializer_result);
  }

  public JExpressionIF forComplexInitializedArrayInstantiationOnly(ComplexInitializedArrayInstantiation that, JExpressionIF enclosing_result, JExpressionIF type_result, JExpressionIF initializer_result) {
    return new ComplexInitializedArrayInstantiation(that.getSourceInfo(), (Expression)enclosing_result, (Type)type_result, (ArrayInitializer)initializer_result);
  }

  public JExpressionIF forVariableReferenceOnly(VariableReference that) {
    return defaultCase(that);
  }

  public JExpressionIF forNameReferenceOnly(NameReference that, JExpressionIF name_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleNameReferenceOnly(SimpleNameReference that, JExpressionIF name_result) {
    return new SimpleNameReference(that.getSourceInfo(), (Word)name_result);
  }

  public JExpressionIF forComplexNameReferenceOnly(ComplexNameReference that, JExpressionIF enclosing_result, JExpressionIF name_result) {
    return new ComplexNameReference(that.getSourceInfo(), (Expression)enclosing_result, (Word)name_result);
  }

  public JExpressionIF forThisReferenceOnly(ThisReference that) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleThisReferenceOnly(SimpleThisReference that) {
    return new SimpleThisReference(that.getSourceInfo());
  }

  public JExpressionIF forComplexThisReferenceOnly(ComplexThisReference that, JExpressionIF enclosing_result) {
    return new ComplexThisReference(that.getSourceInfo(), (Expression)enclosing_result);
  }

  public JExpressionIF forSuperReferenceOnly(SuperReference that) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleSuperReferenceOnly(SimpleSuperReference that) {
    return new SimpleSuperReference(that.getSourceInfo());
  }

  public JExpressionIF forComplexSuperReferenceOnly(ComplexSuperReference that, JExpressionIF enclosing_result) {
    return new ComplexSuperReference(that.getSourceInfo(), (Expression)enclosing_result);
  }

  public JExpressionIF forFunctionInvocationOnly(FunctionInvocation that, JExpressionIF arguments_result) {
    return defaultCase(that);
  }

  public JExpressionIF forMethodInvocationOnly(MethodInvocation that, JExpressionIF arguments_result, JExpressionIF name_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleMethodInvocationOnly(SimpleMethodInvocation that, JExpressionIF name_result, JExpressionIF arguments_result) {
    return new SimpleMethodInvocation(that.getSourceInfo(), (Word)name_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forComplexMethodInvocationOnly(ComplexMethodInvocation that, JExpressionIF enclosing_result, JExpressionIF name_result, JExpressionIF arguments_result) {
    return new ComplexMethodInvocation(that.getSourceInfo(), (Expression)enclosing_result, (Word)name_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forThisConstructorInvocationOnly(ThisConstructorInvocation that, JExpressionIF arguments_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleThisConstructorInvocationOnly(SimpleThisConstructorInvocation that, JExpressionIF arguments_result) {
    return new SimpleThisConstructorInvocation(that.getSourceInfo(), (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forComplexThisConstructorInvocationOnly(ComplexThisConstructorInvocation that, JExpressionIF enclosing_result, JExpressionIF arguments_result) {
    return new ComplexThisConstructorInvocation(that.getSourceInfo(), (Expression)enclosing_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forSuperConstructorInvocationOnly(SuperConstructorInvocation that, JExpressionIF arguments_result) {
    return defaultCase(that);
  }

  public JExpressionIF forSimpleSuperConstructorInvocationOnly(SimpleSuperConstructorInvocation that, JExpressionIF arguments_result) {
    return new SimpleSuperConstructorInvocation(that.getSourceInfo(), (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forComplexSuperConstructorInvocationOnly(ComplexSuperConstructorInvocation that, JExpressionIF enclosing_result, JExpressionIF arguments_result) {
    return new ComplexSuperConstructorInvocation(that.getSourceInfo(), (Expression)enclosing_result, (ParenthesizedExpressionList)arguments_result);
  }

  public JExpressionIF forClassLiteralOnly(ClassLiteral that, JExpressionIF type_result) {
    return new ClassLiteral(that.getSourceInfo(), (ReturnTypeI)type_result);
  }

  public JExpressionIF forArrayAccessOnly(ArrayAccess that, JExpressionIF array_result, JExpressionIF index_result) {
    return new ArrayAccess(that.getSourceInfo(), (Expression)array_result, (Expression)index_result);
  }

  public JExpressionIF forParenthesizedOnly(Parenthesized that, JExpressionIF value_result) {
    return new Parenthesized(that.getSourceInfo(), (Expression)value_result);
  }

  public JExpressionIF forEmptyExpressionOnly(EmptyExpression that) {
    return new EmptyExpression(that.getSourceInfo());
  }

  public JExpressionIF forBodyOnly(Body that, JExpressionIF[] statements_result) {
    return defaultCase(that);
  }

  public JExpressionIF forBracedBodyOnly(BracedBody that, JExpressionIF[] statements_result) {
    return new BracedBody(that.getSourceInfo(), (BodyItemI[])statements_result);
  }

  public JExpressionIF forUnbracedBodyOnly(UnbracedBody that, JExpressionIF[] statements_result) {
    return new UnbracedBody(that.getSourceInfo(), (BodyItemI[])statements_result);
  }

  public JExpressionIF forExpressionListOnly(ExpressionList that, JExpressionIF[] expressions_result) {
    return defaultCase(that);
  }

  public JExpressionIF forParenthesizedExpressionListOnly(ParenthesizedExpressionList that, JExpressionIF[] expressions_result) {
    return new ParenthesizedExpressionList(that.getSourceInfo(), (Expression[])expressions_result);
  }

  public JExpressionIF forUnparenthesizedExpressionListOnly(UnparenthesizedExpressionList that, JExpressionIF[] expressions_result) {
    return new UnparenthesizedExpressionList(that.getSourceInfo(), (Expression[])expressions_result);
  }

  public JExpressionIF forDimensionExpressionListOnly(DimensionExpressionList that, JExpressionIF[] expressions_result) {
    return new DimensionExpressionList(that.getSourceInfo(), (Expression[])expressions_result);
  }

  public JExpressionIF forEmptyForConditionOnly(EmptyForCondition that) {
    return new EmptyForCondition(that.getSourceInfo());
  }


  /** Implementation of JExpressionIFDepthFirstVisitor methods to implement depth-first traversal. */
  public JExpressionIF forSourceFile(SourceFile that) {
    final JExpressionIF[] packageStatements_result = new PackageStatement[that.getPackageStatements().length];
    for (int i = 0; i < that.getPackageStatements().length; i++) {
      packageStatements_result[i] = (PackageStatement)that.getPackageStatements()[i].visit(this);
    }
    final JExpressionIF[] importStatements_result = new ImportStatement[that.getImportStatements().length];
    for (int i = 0; i < that.getImportStatements().length; i++) {
      importStatements_result[i] = (ImportStatement)that.getImportStatements()[i].visit(this);
    }
    final JExpressionIF[] types_result = new TypeDefBase[that.getTypes().length];
    for (int i = 0; i < that.getTypes().length; i++) {
      types_result[i] = (TypeDefBase)that.getTypes()[i].visit(this);
    }
    return forSourceFileOnly(that, packageStatements_result, importStatements_result, types_result);
  }
  
  public JExpressionIF forModifiersAndVisibility(ModifiersAndVisibility that) {
    return forModifiersAndVisibilityOnly(that);
  }
  
  public JExpressionIF forCompoundWord(CompoundWord that) {
    final JExpressionIF[] words_result = new Word[that.getWords().length];
    for (int i = 0; i < that.getWords().length; i++) {
      words_result[i] = (Word)that.getWords()[i].visit(this);
    }
    return forCompoundWordOnly(that, words_result);
  }
  
  public JExpressionIF forWord(Word that) {
    return forWordOnly(that);
  }
  
  public JExpressionIF forClassDef(ClassDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] typeParameters_result = new TypeParameter[that.getTypeParameters().length];
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = (TypeParameter)that.getTypeParameters()[i].visit(this);
    }
    final JExpressionIF superclass_result = that.getSuperclass().visit(this);
    final JExpressionIF[] interfaces_result = new ReferenceType[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = (ReferenceType)that.getInterfaces()[i].visit(this);
    }
    final JExpressionIF body_result = that.getBody().visit(this);
    return forClassDefOnly(that, mav_result, name_result, typeParameters_result, superclass_result, interfaces_result, body_result);
  }
  
  public JExpressionIF forInnerClassDef(InnerClassDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] typeParameters_result = new TypeParameter[that.getTypeParameters().length];
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = (TypeParameter)that.getTypeParameters()[i].visit(this);
    }
    final JExpressionIF superclass_result = that.getSuperclass().visit(this);
    final JExpressionIF[] interfaces_result = new ReferenceType[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = (ReferenceType)that.getInterfaces()[i].visit(this);
    }
    final JExpressionIF body_result = that.getBody().visit(this);
    return forInnerClassDefOnly(that, mav_result, name_result, typeParameters_result, superclass_result, interfaces_result, body_result);
  }
  
  public JExpressionIF forInterfaceDef(InterfaceDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] typeParameters_result = new TypeParameter[that.getTypeParameters().length];
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = (TypeParameter)that.getTypeParameters()[i].visit(this);
    }
    final JExpressionIF[] interfaces_result = new ReferenceType[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = (ReferenceType)that.getInterfaces()[i].visit(this);
    }
    final JExpressionIF body_result = that.getBody().visit(this);
    return forInterfaceDefOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }
  
  public JExpressionIF forInnerInterfaceDef(InnerInterfaceDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] typeParameters_result = new TypeParameter[that.getTypeParameters().length];
    for (int i = 0; i < that.getTypeParameters().length; i++) {
      typeParameters_result[i] = (TypeParameter)that.getTypeParameters()[i].visit(this);
    }
    final JExpressionIF[] interfaces_result = new ReferenceType[that.getInterfaces().length];
    for (int i = 0; i < that.getInterfaces().length; i++) {
      interfaces_result[i] = (ReferenceType)that.getInterfaces()[i].visit(this);
    }
    final JExpressionIF body_result = that.getBody().visit(this);
    return forInnerInterfaceDefOnly(that, mav_result, name_result, typeParameters_result, interfaces_result, body_result);
  }
  
  public JExpressionIF forConstructorDef(ConstructorDef that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF[] parameters_result = new FormalParameter[that.getParameters().length];
    for (int i = 0; i < that.getParameters().length; i++) {
      parameters_result[i] = (FormalParameter)that.getParameters()[i].visit(this);
    }
    final JExpressionIF[] throws_result = new ReferenceType[that.getThrows().length];
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = (ReferenceType)that.getThrows()[i].visit(this);
    }
    final JExpressionIF statements_result = that.getStatements().visit(this);
    return forConstructorDefOnly(that, name_result, mav_result, parameters_result, throws_result, statements_result);
  }
  
  public JExpressionIF forInstanceInitializer(InstanceInitializer that) {
    final JExpressionIF code_result = that.getCode().visit(this);
    return forInstanceInitializerOnly(that, code_result);
  }
  
  public JExpressionIF forStaticInitializer(StaticInitializer that) {
    final JExpressionIF code_result = that.getCode().visit(this);
    return forStaticInitializerOnly(that, code_result);
  }
  
  public JExpressionIF forPackageStatement(PackageStatement that) {
    final JExpressionIF cWord_result = that.getCWord().visit(this);
    return forPackageStatementOnly(that, cWord_result);
  }
  
  public JExpressionIF forClassImportStatement(ClassImportStatement that) {
    final JExpressionIF cWord_result = that.getCWord().visit(this);
    return forClassImportStatementOnly(that, cWord_result);
  }
  
  public JExpressionIF forPackageImportStatement(PackageImportStatement that) {
    final JExpressionIF cWord_result = that.getCWord().visit(this);
    return forPackageImportStatementOnly(that, cWord_result);
  }
  
  public JExpressionIF forLabeledStatement(LabeledStatement that) {
    final JExpressionIF label_result = that.getLabel().visit(this);
    final JExpressionIF statement_result = that.getStatement().visit(this);
    return forLabeledStatementOnly(that, label_result, statement_result);
  }
  
  public JExpressionIF forBlock(Block that) {
    final JExpressionIF statements_result = that.getStatements().visit(this);
    return forBlockOnly(that, statements_result);
  }
  
  public JExpressionIF forExpressionStatement(ExpressionStatement that) {
    final JExpressionIF expression_result = that.getExpression().visit(this);
    return forExpressionStatementOnly(that, expression_result);
  }
  
  public JExpressionIF forSwitchStatement(SwitchStatement that) {
    final JExpressionIF test_result = that.getTest().visit(this);
    final JExpressionIF[] cases_result = new SwitchCase[that.getCases().length];
    for (int i = 0; i < that.getCases().length; i++) {
      cases_result[i] = (SwitchCase)that.getCases()[i].visit(this);
    }
    return forSwitchStatementOnly(that, test_result, cases_result);
  }
  
  public JExpressionIF forIfThenStatement(IfThenStatement that) {
    final JExpressionIF testExpression_result = that.getTestExpression().visit(this);
    final JExpressionIF thenStatement_result = that.getThenStatement().visit(this);
    return forIfThenStatementOnly(that, testExpression_result, thenStatement_result);
  }
  
  public JExpressionIF forIfThenElseStatement(IfThenElseStatement that) {
    final JExpressionIF testExpression_result = that.getTestExpression().visit(this);
    final JExpressionIF thenStatement_result = that.getThenStatement().visit(this);
    final JExpressionIF elseStatement_result = that.getElseStatement().visit(this);
    return forIfThenElseStatementOnly(that, testExpression_result, thenStatement_result, elseStatement_result);
  }
  
  public JExpressionIF forWhileStatement(WhileStatement that) {
    final JExpressionIF condition_result = that.getCondition().visit(this);
    final JExpressionIF code_result = that.getCode().visit(this);
    return forWhileStatementOnly(that, condition_result, code_result);
  }
  
  public JExpressionIF forDoStatement(DoStatement that) {
    final JExpressionIF code_result = that.getCode().visit(this);
    final JExpressionIF condition_result = that.getCondition().visit(this);
    return forDoStatementOnly(that, code_result, condition_result);
  }
  
  public JExpressionIF forForStatement(ForStatement that) {
    final JExpressionIF init_result = that.getInit().visit(this);
    final JExpressionIF condition_result = that.getCondition().visit(this);
    final JExpressionIF update_result = that.getUpdate().visit(this);
    final JExpressionIF code_result = that.getCode().visit(this);
    return forForStatementOnly(that, init_result, condition_result, update_result, code_result);
  }
  
  public JExpressionIF forLabeledBreakStatement(LabeledBreakStatement that) {
    final JExpressionIF label_result = that.getLabel().visit(this);
    return forLabeledBreakStatementOnly(that, label_result);
  }
  
  public JExpressionIF forUnlabeledBreakStatement(UnlabeledBreakStatement that) {
    return forUnlabeledBreakStatementOnly(that);
  }
  
  public JExpressionIF forLabeledContinueStatement(LabeledContinueStatement that) {
    final JExpressionIF label_result = that.getLabel().visit(this);
    return forLabeledContinueStatementOnly(that, label_result);
  }
  
  public JExpressionIF forUnlabeledContinueStatement(UnlabeledContinueStatement that) {
    return forUnlabeledContinueStatementOnly(that);
  }
  
  public JExpressionIF forVoidReturnStatement(VoidReturnStatement that) {
    return forVoidReturnStatementOnly(that);
  }
  
  public JExpressionIF forValueReturnStatement(ValueReturnStatement that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forValueReturnStatementOnly(that, value_result);
  }
  
  public JExpressionIF forThrowStatement(ThrowStatement that) {
    final JExpressionIF thrown_result = that.getThrown().visit(this);
    return forThrowStatementOnly(that, thrown_result);
  }
  
  public JExpressionIF forSynchronizedStatement(SynchronizedStatement that) {
    final JExpressionIF lockExpr_result = that.getLockExpr().visit(this);
    final JExpressionIF block_result = that.getBlock().visit(this);
    return forSynchronizedStatementOnly(that, lockExpr_result, block_result);
  }
  
  public JExpressionIF forTryCatchFinallyStatement(TryCatchFinallyStatement that) {
    final JExpressionIF tryBlock_result = that.getTryBlock().visit(this);
    final JExpressionIF[] catchBlocks_result = new CatchBlock[that.getCatchBlocks().length];
    for (int i = 0; i < that.getCatchBlocks().length; i++) {
      catchBlocks_result[i] = (CatchBlock)that.getCatchBlocks()[i].visit(this);
    }
    final JExpressionIF finallyBlock_result = that.getFinallyBlock().visit(this);
    return forTryCatchFinallyStatementOnly(that, tryBlock_result, catchBlocks_result, finallyBlock_result);
  }
  
  public JExpressionIF forNormalTryCatchStatement(NormalTryCatchStatement that) {
    final JExpressionIF tryBlock_result = that.getTryBlock().visit(this);
    final JExpressionIF[] catchBlocks_result = new CatchBlock[that.getCatchBlocks().length];
    for (int i = 0; i < that.getCatchBlocks().length; i++) {
      catchBlocks_result[i] = (CatchBlock)that.getCatchBlocks()[i].visit(this);
    }
    return forNormalTryCatchStatementOnly(that, tryBlock_result, catchBlocks_result);
  }
  
  public JExpressionIF forEmptyStatement(EmptyStatement that) {
    return forEmptyStatementOnly(that);
  }
  
  public JExpressionIF forConcreteMethodDef(ConcreteMethodDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF[] typeParams_result = new TypeParameter[that.getTypeParams().length];
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = (TypeParameter)that.getTypeParams()[i].visit(this);
    }
    final JExpressionIF result_result = that.getResult().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] params_result = new FormalParameter[that.getParams().length];
    for (int i = 0; i < that.getParams().length; i++) {
      params_result[i] = (FormalParameter)that.getParams()[i].visit(this);
    }
    final JExpressionIF[] throws_result = new ReferenceType[that.getThrows().length];
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = (ReferenceType)that.getThrows()[i].visit(this);
    }
    final JExpressionIF body_result = that.getBody().visit(this);
    return forConcreteMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result, body_result);
  }
  
  public JExpressionIF forAbstractMethodDef(AbstractMethodDef that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF[] typeParams_result = new TypeParameter[that.getTypeParams().length];
    for (int i = 0; i < that.getTypeParams().length; i++) {
      typeParams_result[i] = (TypeParameter)that.getTypeParams()[i].visit(this);
    }
    final JExpressionIF result_result = that.getResult().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF[] params_result = new FormalParameter[that.getParams().length];
    for (int i = 0; i < that.getParams().length; i++) {
      params_result[i] = (FormalParameter)that.getParams()[i].visit(this);
    }
    final JExpressionIF[] throws_result = new ReferenceType[that.getThrows().length];
    for (int i = 0; i < that.getThrows().length; i++) {
      throws_result[i] = (ReferenceType)that.getThrows()[i].visit(this);
    }
    return forAbstractMethodDefOnly(that, mav_result, typeParams_result, result_result, name_result, params_result, throws_result);
  }
  
  public JExpressionIF forFormalParameter(FormalParameter that) {
    final JExpressionIF declarator_result = that.getDeclarator().visit(this);
    return forFormalParameterOnly(that, declarator_result);
  }
  
  public JExpressionIF forVariableDeclaration(VariableDeclaration that) {
    final JExpressionIF mav_result = that.getMav().visit(this);
    final JExpressionIF[] declarators_result = new VariableDeclarator[that.getDeclarators().length];
    for (int i = 0; i < that.getDeclarators().length; i++) {
      declarators_result[i] = (VariableDeclarator)that.getDeclarators()[i].visit(this);
    }
    return forVariableDeclarationOnly(that, mav_result, declarators_result);
  }
  
  public JExpressionIF forUninitializedVariableDeclarator(UninitializedVariableDeclarator that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    return forUninitializedVariableDeclaratorOnly(that, type_result, name_result);
  }
  
  public JExpressionIF forInitializedVariableDeclarator(InitializedVariableDeclarator that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF initializer_result = that.getInitializer().visit(this);
    return forInitializedVariableDeclaratorOnly(that, type_result, name_result, initializer_result);
  }
  
  public JExpressionIF forTypeParameter(TypeParameter that) {
    final JExpressionIF variable_result = that.getVariable().visit(this);
    final JExpressionIF bound_result = that.getBound().visit(this);
    return forTypeParameterOnly(that, variable_result, bound_result);
  }
  
  public JExpressionIF forArrayInitializer(ArrayInitializer that) {
    final JExpressionIF[] items_result = new VariableInitializerI[that.getItems().length];
    for (int i = 0; i < that.getItems().length; i++) {
      items_result[i] = (VariableInitializerI)that.getItems()[i].visit(this);
    }
    return forArrayInitializerOnly(that, items_result);
  }
  
  public JExpressionIF forPrimitiveType(PrimitiveType that) {
    return forPrimitiveTypeOnly(that);
  }
  
  public JExpressionIF forArrayType(ArrayType that) {
    final JExpressionIF elementType_result = that.getElementType().visit(this);
    return forArrayTypeOnly(that, elementType_result);
  }
  
  public JExpressionIF forMemberType(MemberType that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forMemberTypeOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forClassOrInterfaceType(ClassOrInterfaceType that) {
    final JExpressionIF[] typeArguments_result = new Type[that.getTypeArguments().length];
    for (int i = 0; i < that.getTypeArguments().length; i++) {
      typeArguments_result[i] = (Type)that.getTypeArguments()[i].visit(this);
    }
    return forClassOrInterfaceTypeOnly(that, typeArguments_result);
  }
  
  public JExpressionIF forTypeVariable(TypeVariable that) {
    return forTypeVariableOnly(that);
  }
  
  public JExpressionIF forVoidReturn(VoidReturn that) {
    return forVoidReturnOnly(that);
  }
  
  public JExpressionIF forLabeledCase(LabeledCase that) {
    final JExpressionIF label_result = that.getLabel().visit(this);
    final JExpressionIF code_result = that.getCode().visit(this);
    return forLabeledCaseOnly(that, label_result, code_result);
  }
  
  public JExpressionIF forDefaultCase(DefaultCase that) {
    final JExpressionIF code_result = that.getCode().visit(this);
    return forDefaultCaseOnly(that, code_result);
  }
  
  public JExpressionIF forCatchBlock(CatchBlock that) {
    final JExpressionIF exception_result = that.getException().visit(this);
    final JExpressionIF block_result = that.getBlock().visit(this);
    return forCatchBlockOnly(that, exception_result, block_result);
  }
  
  public JExpressionIF forSimpleAssignmentExpression(SimpleAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forSimpleAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forPlusAssignmentExpression(PlusAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forPlusAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forMinusAssignmentExpression(MinusAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forMinusAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forMultiplyAssignmentExpression(MultiplyAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forMultiplyAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forDivideAssignmentExpression(DivideAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forDivideAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forModAssignmentExpression(ModAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forModAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forLeftShiftAssignmentExpression(LeftShiftAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forLeftShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forRightSignedShiftAssignmentExpression(RightSignedShiftAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forRightSignedShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forRightUnsignedShiftAssignmentExpression(RightUnsignedShiftAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forRightUnsignedShiftAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forBitwiseAndAssignmentExpression(BitwiseAndAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forBitwiseAndAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forBitwiseOrAssignmentExpression(BitwiseOrAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forBitwiseOrAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forBitwiseXorAssignmentExpression(BitwiseXorAssignmentExpression that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forBitwiseXorAssignmentExpressionOnly(that, name_result, value_result);
  }
  
  public JExpressionIF forOrExpression(OrExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forOrExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forAndExpression(AndExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forAndExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forBitwiseOrExpression(BitwiseOrExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forBitwiseOrExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forBitwiseXorExpression(BitwiseXorExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forBitwiseXorExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forBitwiseAndExpression(BitwiseAndExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forBitwiseAndExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forEqualsExpression(EqualsExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forEqualsExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forNotEqualExpression(NotEqualExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forNotEqualExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forLessThanExpression(LessThanExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forLessThanExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forLessThanOrEqualExpression(LessThanOrEqualExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forLessThanOrEqualExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forGreaterThanExpression(GreaterThanExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forGreaterThanExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forGreaterThanOrEqualExpression(GreaterThanOrEqualExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forGreaterThanOrEqualExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forLeftShiftExpression(LeftShiftExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forLeftShiftExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forRightSignedShiftExpression(RightSignedShiftExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forRightSignedShiftExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forRightUnsignedShiftExpression(RightUnsignedShiftExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forRightUnsignedShiftExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forPlusExpression(PlusExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forPlusExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forMinusExpression(MinusExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forMinusExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forMultiplyExpression(MultiplyExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forMultiplyExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forDivideExpression(DivideExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forDivideExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forModExpression(ModExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forModExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forNoOpExpression(NoOpExpression that) {
    final JExpressionIF left_result = that.getLeft().visit(this);
    final JExpressionIF right_result = that.getRight().visit(this);
    return forNoOpExpressionOnly(that, left_result, right_result);
  }
  
  public JExpressionIF forPositivePrefixIncrementExpression(PositivePrefixIncrementExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forPositivePrefixIncrementExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forNegativePrefixIncrementExpression(NegativePrefixIncrementExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forNegativePrefixIncrementExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forPositivePostfixIncrementExpression(PositivePostfixIncrementExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forPositivePostfixIncrementExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forNegativePostfixIncrementExpression(NegativePostfixIncrementExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forNegativePostfixIncrementExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forPositiveExpression(PositiveExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forPositiveExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forNegativeExpression(NegativeExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forNegativeExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forBitwiseNotExpression(BitwiseNotExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forBitwiseNotExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forNotExpression(NotExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forNotExpressionOnly(that, value_result);
  }
  
  public JExpressionIF forConditionalExpression(ConditionalExpression that) {
    final JExpressionIF condition_result = that.getCondition().visit(this);
    final JExpressionIF forTrue_result = that.getForTrue().visit(this);
    final JExpressionIF forFalse_result = that.getForFalse().visit(this);
    return forConditionalExpressionOnly(that, condition_result, forTrue_result, forFalse_result);
  }
  
  public JExpressionIF forInstanceofExpression(InstanceofExpression that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    final JExpressionIF type_result = that.getType().visit(this);
    return forInstanceofExpressionOnly(that, value_result, type_result);
  }
  
  public JExpressionIF forCastExpression(CastExpression that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF value_result = that.getValue().visit(this);
    return forCastExpressionOnly(that, type_result, value_result);
  }
  
  public JExpressionIF forIntegerLiteral(IntegerLiteral that) {
    return forIntegerLiteralOnly(that);
  }
  
  public JExpressionIF forLongLiteral(LongLiteral that) {
    return forLongLiteralOnly(that);
  }
  
  public JExpressionIF forDoubleLiteral(DoubleLiteral that) {
    return forDoubleLiteralOnly(that);
  }
  
  public JExpressionIF forFloatLiteral(FloatLiteral that) {
    return forFloatLiteralOnly(that);
  }
  
  public JExpressionIF forBooleanLiteral(BooleanLiteral that) {
    return forBooleanLiteralOnly(that);
  }
  
  public JExpressionIF forCharLiteral(CharLiteral that) {
    return forCharLiteralOnly(that);
  }
  
  public JExpressionIF forStringLiteral(StringLiteral that) {
    return forStringLiteralOnly(that);
  }
  
  public JExpressionIF forNullLiteral(NullLiteral that) {
    return forNullLiteralOnly(that);
  }
  
  public JExpressionIF forSimpleNamedClassInstantiation(SimpleNamedClassInstantiation that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forSimpleNamedClassInstantiationOnly(that, type_result, arguments_result);
  }
  
  public JExpressionIF forComplexNamedClassInstantiation(ComplexNamedClassInstantiation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forComplexNamedClassInstantiationOnly(that, enclosing_result, type_result, arguments_result);
  }
  
  public JExpressionIF forSimpleAnonymousClassInstantiation(SimpleAnonymousClassInstantiation that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    final JExpressionIF body_result = that.getBody().visit(this);
    return forSimpleAnonymousClassInstantiationOnly(that, type_result, arguments_result, body_result);
  }
  
  public JExpressionIF forComplexAnonymousClassInstantiation(ComplexAnonymousClassInstantiation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    final JExpressionIF body_result = that.getBody().visit(this);
    return forComplexAnonymousClassInstantiationOnly(that, enclosing_result, type_result, arguments_result, body_result);
  }
  
  public JExpressionIF forSimpleUninitializedArrayInstantiation(SimpleUninitializedArrayInstantiation that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF dimensionSizes_result = that.getDimensionSizes().visit(this);
    return forSimpleUninitializedArrayInstantiationOnly(that, type_result, dimensionSizes_result);
  }
  
  public JExpressionIF forComplexUninitializedArrayInstantiation(ComplexUninitializedArrayInstantiation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF dimensionSizes_result = that.getDimensionSizes().visit(this);
    return forComplexUninitializedArrayInstantiationOnly(that, enclosing_result, type_result, dimensionSizes_result);
  }
  
  public JExpressionIF forSimpleInitializedArrayInstantiation(SimpleInitializedArrayInstantiation that) {
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF initializer_result = that.getInitializer().visit(this);
    return forSimpleInitializedArrayInstantiationOnly(that, type_result, initializer_result);
  }
  
  public JExpressionIF forComplexInitializedArrayInstantiation(ComplexInitializedArrayInstantiation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF type_result = that.getType().visit(this);
    final JExpressionIF initializer_result = that.getInitializer().visit(this);
    return forComplexInitializedArrayInstantiationOnly(that, enclosing_result, type_result, initializer_result);
  }
  
  public JExpressionIF forSimpleNameReference(SimpleNameReference that) {
    final JExpressionIF name_result = that.getName().visit(this);
    return forSimpleNameReferenceOnly(that, name_result);
  }
  
  public JExpressionIF forComplexNameReference(ComplexNameReference that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    return forComplexNameReferenceOnly(that, enclosing_result, name_result);
  }
  
  public JExpressionIF forSimpleThisReference(SimpleThisReference that) {
    return forSimpleThisReferenceOnly(that);
  }
  
  public JExpressionIF forComplexThisReference(ComplexThisReference that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    return forComplexThisReferenceOnly(that, enclosing_result);
  }
  
  public JExpressionIF forSimpleSuperReference(SimpleSuperReference that) {
    return forSimpleSuperReferenceOnly(that);
  }
  
  public JExpressionIF forComplexSuperReference(ComplexSuperReference that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    return forComplexSuperReferenceOnly(that, enclosing_result);
  }
  
  public JExpressionIF forSimpleMethodInvocation(SimpleMethodInvocation that) {
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forSimpleMethodInvocationOnly(that, name_result, arguments_result);
  }
  
  public JExpressionIF forComplexMethodInvocation(ComplexMethodInvocation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF name_result = that.getName().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forComplexMethodInvocationOnly(that, enclosing_result, name_result, arguments_result);
  }
  
  public JExpressionIF forSimpleThisConstructorInvocation(SimpleThisConstructorInvocation that) {
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forSimpleThisConstructorInvocationOnly(that, arguments_result);
  }
  
  public JExpressionIF forComplexThisConstructorInvocation(ComplexThisConstructorInvocation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forComplexThisConstructorInvocationOnly(that, enclosing_result, arguments_result);
  }
  
  public JExpressionIF forSimpleSuperConstructorInvocation(SimpleSuperConstructorInvocation that) {
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forSimpleSuperConstructorInvocationOnly(that, arguments_result);
  }
  
  public JExpressionIF forComplexSuperConstructorInvocation(ComplexSuperConstructorInvocation that) {
    final JExpressionIF enclosing_result = that.getEnclosing().visit(this);
    final JExpressionIF arguments_result = that.getArguments().visit(this);
    return forComplexSuperConstructorInvocationOnly(that, enclosing_result, arguments_result);
  }
  
  public JExpressionIF forClassLiteral(ClassLiteral that) {
    final JExpressionIF type_result = that.getType().visit(this);
    return forClassLiteralOnly(that, type_result);
  }
  
  public JExpressionIF forArrayAccess(ArrayAccess that) {
    final JExpressionIF array_result = that.getArray().visit(this);
    final JExpressionIF index_result = that.getIndex().visit(this);
    return forArrayAccessOnly(that, array_result, index_result);
  }
  
  public JExpressionIF forParenthesized(Parenthesized that) {
    final JExpressionIF value_result = that.getValue().visit(this);
    return forParenthesizedOnly(that, value_result);
  }
  
  public JExpressionIF forEmptyExpression(EmptyExpression that) {
    return forEmptyExpressionOnly(that);
  }
  
  public JExpressionIF forBracedBody(BracedBody that) {
    final JExpressionIF[] statements_result = new BodyItemI[that.getStatements().length];
    for (int i = 0; i < that.getStatements().length; i++) {
      statements_result[i] = (BodyItemI)that.getStatements()[i].visit(this);
    }
    return forBracedBodyOnly(that, statements_result);
  }
  
  public JExpressionIF forUnbracedBody(UnbracedBody that) {
    final JExpressionIF[] statements_result = new BodyItemI[that.getStatements().length];
    for (int i = 0; i < that.getStatements().length; i++) {
      statements_result[i] = (BodyItemI)that.getStatements()[i].visit(this);
    }
    return forUnbracedBodyOnly(that, statements_result);
  }
  
  public JExpressionIF forParenthesizedExpressionList(ParenthesizedExpressionList that) {
    final JExpressionIF[] expressions_result = new Expression[that.getExpressions().length];
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = (Expression)that.getExpressions()[i].visit(this);
    }
    return forParenthesizedExpressionListOnly(that, expressions_result);
  }
  
  public JExpressionIF forUnparenthesizedExpressionList(UnparenthesizedExpressionList that) {
    final JExpressionIF[] expressions_result = new Expression[that.getExpressions().length];
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = (Expression)that.getExpressions()[i].visit(this);
    }
    return forUnparenthesizedExpressionListOnly(that, expressions_result);
  }
  
  public JExpressionIF forDimensionExpressionList(DimensionExpressionList that) {
    final JExpressionIF[] expressions_result = new Expression[that.getExpressions().length];
    for (int i = 0; i < that.getExpressions().length; i++) {
      expressions_result[i] = (Expression)that.getExpressions()[i].visit(this);
    }
    return forDimensionExpressionListOnly(that, expressions_result);
  }
  
  public JExpressionIF forEmptyForCondition(EmptyForCondition that) {
    return forEmptyForConditionOnly(that);
  }
  
}
