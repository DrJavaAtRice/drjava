/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.tree.visitor;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.GenericReferenceTypeName;
import koala.dynamicjava.tree.tiger.HookTypeName;

/**
 * This interface contains the methods a visitor of the AST must implement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public interface Visitor<T> {
  T visit(CompilationUnit node);
  T visit(PackageDeclaration node);
  T visit(ImportDeclaration node);
  T visit(EmptyStatement node);
  T visit(ExpressionStatement node);
  T visit(WhileStatement node);
  T visit(ForStatement node);
  T visit(ForEachStatement node);
  T visit(DoStatement node);
  T visit(SwitchStatement node);
  T visit(SwitchBlock node);
  T visit(LabeledStatement node);
  T visit(BreakStatement node);
  T visit(TryStatement node);
  T visit(CatchStatement node);
  T visit(ThrowStatement node);
  T visit(ReturnStatement node);
  T visit(SynchronizedStatement node);
  T visit(ContinueStatement node);
  T visit(IfThenStatement node);
  T visit(IfThenElseStatement node);
  T visit(AssertStatement node);
  T visit(Literal node);
  T visit(ThisExpression node);
  T visit(AmbiguousName node);
  T visit(VariableAccess node);
  T visit(SimpleFieldAccess node);
  T visit(ObjectFieldAccess node);
  T visit(StaticFieldAccess node);
  T visit(SuperFieldAccess node);
  T visit(ArrayAccess node);
  T visit(ObjectMethodCall node);
  T visit(SimpleMethodCall node);
  T visit(StaticMethodCall node);
  T visit(ConstructorCall node);
  T visit(SuperMethodCall node);
  T visit(BooleanTypeName node);
  T visit(ByteTypeName node);
  T visit(ShortTypeName node);
  T visit(CharTypeName node);
  T visit(IntTypeName node);
  T visit(LongTypeName node);
  T visit(FloatTypeName node);
  T visit(DoubleTypeName node);
  T visit(VoidTypeName node);
  T visit(ReferenceTypeName node);
  T visit(GenericReferenceTypeName node);
  T visit(ArrayTypeName node);
  T visit(HookTypeName node);
  T visit(TypeExpression node);
  T visit(PostIncrement node);
  T visit(PostDecrement node);
  T visit(PreIncrement node);
  T visit(PreDecrement node);
  T visit(ArrayInitializer node);
  T visit(ArrayAllocation node);
  T visit(SimpleAllocation node);
  T visit(AnonymousAllocation node);
  T visit(InnerAllocation node);
  T visit(AnonymousInnerAllocation node);
  T visit(CastExpression node);
  T visit(NotExpression node);
  T visit(ComplementExpression node);
  T visit(PlusExpression node);
  T visit(MinusExpression node);
  T visit(MultiplyExpression node);
  T visit(DivideExpression node);
  T visit(RemainderExpression node);
  T visit(AddExpression node);
  T visit(SubtractExpression node);
  T visit(ShiftLeftExpression node);
  T visit(ShiftRightExpression node);
  T visit(UnsignedShiftRightExpression node);
  T visit(LessExpression node);
  T visit(GreaterExpression node);
  T visit(LessOrEqualExpression node);
  T visit(GreaterOrEqualExpression node);
  T visit(InstanceOfExpression node);
  T visit(EqualExpression node);
  T visit(NotEqualExpression node);
  T visit(BitAndExpression node);
  T visit(ExclusiveOrExpression node);
  T visit(BitOrExpression node);
  T visit(AndExpression node);
  T visit(OrExpression node);
  T visit(ConditionalExpression node);
  T visit(SimpleAssignExpression node);
  T visit(MultiplyAssignExpression node);
  T visit(DivideAssignExpression node);
  T visit(RemainderAssignExpression node);
  T visit(AddAssignExpression node);
  T visit(SubtractAssignExpression node);
  T visit(ShiftLeftAssignExpression node);
  T visit(ShiftRightAssignExpression node);
  T visit(UnsignedShiftRightAssignExpression node);
  T visit(BitAndAssignExpression node);
  T visit(ExclusiveOrAssignExpression node);
  T visit(BitOrAssignExpression node);
  T visit(BlockStatement node);
  T visit(ClassDeclaration node);
  T visit(InterfaceDeclaration node);
  T visit(ConstructorDeclaration node);
  T visit(MethodDeclaration node);
  T visit(FormalParameter node);
  T visit(FieldDeclaration node);
  T visit(VariableDeclaration node);
  T visit(EnumDeclaration.EnumConstant node);
  T visit(ClassInitializer node);
  T visit(InstanceInitializer node);
  T visit(ModifierSet node);
  T visit(Annotation node);
}
