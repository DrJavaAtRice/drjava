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

/**
 * This interface contains the methods a visitor of the AST must implement
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public interface Visitor<T> {
  /**
   * Visits an PackageDeclaration
   * @param node the node to visit
   */
  T visit(PackageDeclaration node);
  
  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   */
  T visit(ImportDeclaration node);
  
  /**
   * Visits an EmptyStatement
   * @param node the node to visit
   */
  T visit(EmptyStatement node);
  
  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  T visit(WhileStatement node);
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  T visit(ForStatement node);
  
  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  T visit(DoStatement node);
  
  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  T visit(SwitchStatement node);
  
  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  T visit(SwitchBlock node);
  
  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  T visit(LabeledStatement node);
  
  /**
   * Visits a BreakStatement
   * @param node the node to visit
   */
  T visit(BreakStatement node);
  
  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  T visit(TryStatement node);
  
  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  T visit(CatchStatement node);
  
  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  T visit(ThrowStatement node);
  
  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  T visit(ReturnStatement node);
  
  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  T visit(SynchronizedStatement node);
  
  /**
   * Visits a ContinueStatement
   * @param node the node to visit
   */
  T visit(ContinueStatement node);
  
  /**
   * Visits a IfThenStatement
   * @param node the node to visit
   */
  T visit(IfThenStatement node);
  
  /**
   * Visits a IfThenElseStatement
   * @param node the node to visit
   */
  T visit(IfThenElseStatement node);
  
  /**
   * Visits a Literal
   * @param node the node to visit
   */
  T visit(Literal node);
  
  /**
   * Visits a ThisExpression
   * @param node the node to visit
   */
  T visit(ThisExpression node);
  
  /**
   * Visits a QualifiedName
   * @param node the node to visit
   */
  T visit(QualifiedName node);
  
  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  T visit(ObjectFieldAccess node);
  
  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  T visit(StaticFieldAccess node);
  
  /**
   * Visits a ArrayAccess
   * @param node the node to visit
   */
  T visit(ArrayAccess node);
  
  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  T visit(SuperFieldAccess node);
  
  /**
   * Visits an ObjectMethodCall
   * @param node the node to visit
   */
  T visit(ObjectMethodCall node);
  
  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  T visit(FunctionCall node);
  
  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  T visit(StaticMethodCall node);
  
  /**
   * Visits a ConstructorInvocation
   * @param node the node to visit
   */
  T visit(ConstructorInvocation node);
  
  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  T visit(SuperMethodCall node);
  
  /**
   * Visits a PrimitiveType
   * @param node the node to visit
   */
  T visit(PrimitiveType node);
  
  /**
   * Visits a ReferenceType
   * @param node the node to visit
   */
  T visit(ReferenceType node);
  
  /**
   * Visits a ArrayType
   * @param node the node to visit
   */
  T visit(ArrayType node);
  
  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  T visit(TypeExpression node);
  
  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  T visit(PostIncrement node);
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  T visit(PostDecrement node);
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  T visit(PreIncrement node);
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  T visit(PreDecrement node);
  
  /**
   * Visits an ArrayInitializer
   * @param node the node to visit
   */
  T visit(ArrayInitializer node);
  
  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  T visit(ArrayAllocation node);
  
  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  T visit(SimpleAllocation node);
  
  /**
   * Visits a ClassAllocation
   * @param node the node to visit
   */
  T visit(ClassAllocation node);
  
  /**
   * Visits an InnerAllocation
   * @param node the node to visit
   */
  T visit(InnerAllocation node);
  
  /**
   * Visits an InnerClassAllocation
   * @param node the node to visit
   */
  T visit(InnerClassAllocation node);
  
  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  T visit(CastExpression node);
  
  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  T visit(NotExpression node);
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  T visit(ComplementExpression node);
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  T visit(PlusExpression node);
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  T visit(MinusExpression node);
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  T visit(MultiplyExpression node);
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  T visit(DivideExpression node);
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  T visit(RemainderExpression node);
  
  /**
   * Visits a AddExpression
   * @param node the node to visit
   */
  T visit(AddExpression node);
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  T visit(SubtractExpression node);
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  T visit(ShiftLeftExpression node);
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  T visit(ShiftRightExpression node);
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  T visit(UnsignedShiftRightExpression node);
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  T visit(LessExpression node);
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  T visit(GreaterExpression node);
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  T visit(LessOrEqualExpression node);
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  T visit(GreaterOrEqualExpression node);
  
  /**
   * Visits an InstanceOfExpression
   * @param node the node to visit
   */
  T visit(InstanceOfExpression node);
  
  /**
   * Visits a EqualExpression
   * @param node the node to visit
   */
  T visit(EqualExpression node);
  
  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  T visit(NotEqualExpression node);
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  T visit(BitAndExpression node);
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  T visit(ExclusiveOrExpression node);
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  T visit(BitOrExpression node);
  
  /**
   * Visits a AndExpression
   * @param node the node to visit
   */
  T visit(AndExpression node);
  
  /**
   * Visits a OrExpression
   * @param node the node to visit
   */
  T visit(OrExpression node);
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  T visit(ConditionalExpression node);
  
  /**
   * Visits a SimpleAssignExpression
   * @param node the node to visit
   */
  T visit(SimpleAssignExpression node);
  
  /**
   * Visits a MultiplyAssignExpression
   * @param node the node to visit
   */
  T visit(MultiplyAssignExpression node);
  
  /**
   * Visits a DivideAssignExpression
   * @param node the node to visit
   */
  T visit(DivideAssignExpression node);
  
  /**
   * Visits a RemainderAssignExpression
   * @param node the node to visit
   */
  T visit(RemainderAssignExpression node);
  
  /**
   * Visits a AddAssignExpression
   * @param node the node to visit
   */
  T visit(AddAssignExpression node);
  
  /**
   * Visits a SubtractAssignExpression
   * @param node the node to visit
   */
  T visit(SubtractAssignExpression node);
  
  /**
   * Visits a ShiftLeftAssignExpression
   * @param node the node to visit
   */
  T visit(ShiftLeftAssignExpression node);
  
  /**
   * Visits a ShiftRightAssignExpression
   * @param node the node to visit
   */
  T visit(ShiftRightAssignExpression node);
  
  /**
   * Visits a UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  T visit(UnsignedShiftRightAssignExpression node);
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  T visit(BitAndAssignExpression node);
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  T visit(ExclusiveOrAssignExpression node);
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  T visit(BitOrAssignExpression node);
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  T visit(BlockStatement node);
  
  /**
   * Visits a ClassDeclaration
   * @param node the node to visit
   */
  T visit(ClassDeclaration node);
  
  /**
   * Visits a InterfaceDeclaration
   * @param node the node to visit
   */
  T visit(InterfaceDeclaration node);
  
  /**
   * Visits a ConstructorDeclaration
   * @param node the node to visit
   */
  T visit(ConstructorDeclaration node);
  
  /**
   * Visits a MethodDeclaration
   * @param node the node to visit
   */
  T visit(MethodDeclaration node);
  
  /**
   * Visits a FormalParameter
   * @param node the node to visit
   */
  T visit(FormalParameter node);
  
  /**
   * Visits a FieldDeclaration
   * @param node the node to visit
   */
  T visit(FieldDeclaration node);
  
  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  T visit(VariableDeclaration node);
  
  /**
   * Visits a ClassInitializer
   * @param node the node to visit
   */
  T visit(ClassInitializer node);
  
  /**
   * Visits a InstanceInitializer
   * @param node the node to visit
   */
  T visit(InstanceInitializer node);
  
}
