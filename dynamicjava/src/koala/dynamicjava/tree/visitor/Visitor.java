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

public interface Visitor {
    /**
     * Visits an PackageDeclaration
     * @param node the node to visit
     */
    Object visit(PackageDeclaration node);

    /**
     * Visits an ImportDeclaration
     * @param node the node to visit
     */
    Object visit(ImportDeclaration node);

    /**
     * Visits an EmptyStatement
     * @param node the node to visit
     */
    Object visit(EmptyStatement node);

    /**
     * Visits a WhileStatement
     * @param node the node to visit
     */
    Object visit(WhileStatement node);

    /**
     * Visits a ForStatement
     * @param node the node to visit
     */
    Object visit(ForStatement node);

    /**
     * Visits a DoStatement
     * @param node the node to visit
     */
    Object visit(DoStatement node);

    /**
     * Visits a SwitchStatement
     * @param node the node to visit
     */
    Object visit(SwitchStatement node);

    /**
     * Visits a SwitchBlock
     * @param node the node to visit
     */
    Object visit(SwitchBlock node);

    /**
     * Visits a LabeledStatement
     * @param node the node to visit
     */
    Object visit(LabeledStatement node);

    /**
     * Visits a BreakStatement
     * @param node the node to visit
     */
    Object visit(BreakStatement node);

    /**
     * Visits a TryStatement
     * @param node the node to visit
     */
    Object visit(TryStatement node);

    /**
     * Visits a CatchStatement
     * @param node the node to visit
     */
    Object visit(CatchStatement node);

    /**
     * Visits a ThrowStatement
     * @param node the node to visit
     */
    Object visit(ThrowStatement node);

    /**
     * Visits a ReturnStatement
     * @param node the node to visit
     */
    Object visit(ReturnStatement node);

    /**
     * Visits a SynchronizedStatement
     * @param node the node to visit
     */
    Object visit(SynchronizedStatement node);

    /**
     * Visits a ContinueStatement
     * @param node the node to visit
     */
    Object visit(ContinueStatement node);

    /**
     * Visits a IfThenStatement
     * @param node the node to visit
     */
    Object visit(IfThenStatement node);

    /**
     * Visits a IfThenElseStatement
     * @param node the node to visit
     */
    Object visit(IfThenElseStatement node);

    /**
     * Visits a Literal
     * @param node the node to visit
     */
    Object visit(Literal node);

    /**
     * Visits a ThisExpression
     * @param node the node to visit
     */
    Object visit(ThisExpression node);

    /**
     * Visits a QualifiedName
     * @param node the node to visit
     */
    Object visit(QualifiedName node);

    /**
     * Visits an ObjectFieldAccess
     * @param node the node to visit
     */
    Object visit(ObjectFieldAccess node);

    /**
     * Visits a StaticFieldAccess
     * @param node the node to visit
     */
    Object visit(StaticFieldAccess node);

    /**
     * Visits a ArrayAccess
     * @param node the node to visit
     */
    Object visit(ArrayAccess node);

    /**
     * Visits a SuperFieldAccess
     * @param node the node to visit
     */
    Object visit(SuperFieldAccess node);

    /**
     * Visits an ObjectMethodCall
     * @param node the node to visit
     */
    Object visit(ObjectMethodCall node);

    /**
     * Visits a FunctionCall
     * @param node the node to visit
     */
    Object visit(FunctionCall node);

    /**
     * Visits a StaticMethodCall
     * @param node the node to visit
     */
    Object visit(StaticMethodCall node);

    /**
     * Visits a ConstructorInvocation
     * @param node the node to visit
     */
    Object visit(ConstructorInvocation node);

    /**
     * Visits a SuperMethodCall
     * @param node the node to visit
     */
    Object visit(SuperMethodCall node);

    /**
     * Visits a PrimitiveType
     * @param node the node to visit
     */
    Object visit(PrimitiveType node);

    /**
     * Visits a ReferenceType
     * @param node the node to visit
     */
    Object visit(ReferenceType node);

    /**
     * Visits a ArrayType
     * @param node the node to visit
     */
    Object visit(ArrayType node);

    /**
     * Visits a TypeExpression
     * @param node the node to visit
     */
    Object visit(TypeExpression node);

    /**
     * Visits a PostIncrement
     * @param node the node to visit
     */
    Object visit(PostIncrement node);

    /**
     * Visits a PostDecrement
     * @param node the node to visit
     */
    Object visit(PostDecrement node);

    /**
     * Visits a PreIncrement
     * @param node the node to visit
     */
    Object visit(PreIncrement node);

    /**
     * Visits a PreDecrement
     * @param node the node to visit
     */
    Object visit(PreDecrement node);

    /**
     * Visits an ArrayInitializer
     * @param node the node to visit
     */
    Object visit(ArrayInitializer node);

    /**
     * Visits an ArrayAllocation
     * @param node the node to visit
     */
    Object visit(ArrayAllocation node);

    /**
     * Visits a SimpleAllocation
     * @param node the node to visit
     */
    Object visit(SimpleAllocation node);

    /**
     * Visits a ClassAllocation
     * @param node the node to visit
     */
    Object visit(ClassAllocation node);

    /**
     * Visits an InnerAllocation
     * @param node the node to visit
     */
    Object visit(InnerAllocation node);

    /**
     * Visits an InnerClassAllocation
     * @param node the node to visit
     */
    Object visit(InnerClassAllocation node);

    /**
     * Visits a CastExpression
     * @param node the node to visit
     */
    Object visit(CastExpression node);

    /**
     * Visits a NotExpression
     * @param node the node to visit
     */
    Object visit(NotExpression node);

    /**
     * Visits a ComplementExpression
     * @param node the node to visit
     */
    Object visit(ComplementExpression node);

    /**
     * Visits a PlusExpression
     * @param node the node to visit
     */
    Object visit(PlusExpression node);

    /**
     * Visits a MinusExpression
     * @param node the node to visit
     */
    Object visit(MinusExpression node);

    /**
     * Visits a MultiplyExpression
     * @param node the node to visit
     */
    Object visit(MultiplyExpression node);

    /**
     * Visits a DivideExpression
     * @param node the node to visit
     */
    Object visit(DivideExpression node);

    /**
     * Visits a RemainderExpression
     * @param node the node to visit
     */
    Object visit(RemainderExpression node);

    /**
     * Visits a AddExpression
     * @param node the node to visit
     */
    Object visit(AddExpression node);

    /**
     * Visits a SubtractExpression
     * @param node the node to visit
     */
    Object visit(SubtractExpression node);

    /**
     * Visits a ShiftLeftExpression
     * @param node the node to visit
     */
    Object visit(ShiftLeftExpression node);

    /**
     * Visits a ShiftRightExpression
     * @param node the node to visit
     */
    Object visit(ShiftRightExpression node);

    /**
     * Visits a UnsignedShiftRightExpression
     * @param node the node to visit
     */
    Object visit(UnsignedShiftRightExpression node);

    /**
     * Visits a LessExpression
     * @param node the node to visit
     */
    Object visit(LessExpression node);

    /**
     * Visits a GreaterExpression
     * @param node the node to visit
     */
    Object visit(GreaterExpression node);

    /**
     * Visits a LessOrEqualExpression
     * @param node the node to visit
     */
    Object visit(LessOrEqualExpression node);

    /**
     * Visits a GreaterOrEqualExpression
     * @param node the node to visit
     */
    Object visit(GreaterOrEqualExpression node);

    /**
     * Visits an InstanceOfExpression
     * @param node the node to visit
     */
    Object visit(InstanceOfExpression node);

    /**
     * Visits a EqualExpression
     * @param node the node to visit
     */
    Object visit(EqualExpression node);

    /**
     * Visits a NotEqualExpression
     * @param node the node to visit
     */
    Object visit(NotEqualExpression node);

    /**
     * Visits a BitAndExpression
     * @param node the node to visit
     */
    Object visit(BitAndExpression node);

    /**
     * Visits a ExclusiveOrExpression
     * @param node the node to visit
     */
    Object visit(ExclusiveOrExpression node);

    /**
     * Visits a BitOrExpression
     * @param node the node to visit
     */
    Object visit(BitOrExpression node);

    /**
     * Visits a AndExpression
     * @param node the node to visit
     */
    Object visit(AndExpression node);

    /**
     * Visits a OrExpression
     * @param node the node to visit
     */
    Object visit(OrExpression node);

    /**
     * Visits a ConditionalExpression
     * @param node the node to visit
     */
    Object visit(ConditionalExpression node);

    /**
     * Visits a SimpleAssignExpression
     * @param node the node to visit
     */
    Object visit(SimpleAssignExpression node);

    /**
     * Visits a MultiplyAssignExpression
     * @param node the node to visit
     */
    Object visit(MultiplyAssignExpression node);

    /**
     * Visits a DivideAssignExpression
     * @param node the node to visit
     */
    Object visit(DivideAssignExpression node);

    /**
     * Visits a RemainderAssignExpression
     * @param node the node to visit
     */
    Object visit(RemainderAssignExpression node);

    /**
     * Visits a AddAssignExpression
     * @param node the node to visit
     */
    Object visit(AddAssignExpression node);

    /**
     * Visits a SubtractAssignExpression
     * @param node the node to visit
     */
    Object visit(SubtractAssignExpression node);

    /**
     * Visits a ShiftLeftAssignExpression
     * @param node the node to visit
     */
    Object visit(ShiftLeftAssignExpression node);

    /**
     * Visits a ShiftRightAssignExpression
     * @param node the node to visit
     */
    Object visit(ShiftRightAssignExpression node);

    /**
     * Visits a UnsignedShiftRightAssignExpression
     * @param node the node to visit
     */
    Object visit(UnsignedShiftRightAssignExpression node);

    /**
     * Visits a BitAndAssignExpression
     * @param node the node to visit
     */
    Object visit(BitAndAssignExpression node);

    /**
     * Visits a ExclusiveOrAssignExpression
     * @param node the node to visit
     */
    Object visit(ExclusiveOrAssignExpression node);

    /**
     * Visits a BitOrAssignExpression
     * @param node the node to visit
     */
    Object visit(BitOrAssignExpression node);

    /**
     * Visits a BlockStatement
     * @param node the node to visit
     */
    Object visit(BlockStatement node);

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    Object visit(ClassDeclaration node);

    /**
     * Visits a InterfaceDeclaration
     * @param node the node to visit
     */
    Object visit(InterfaceDeclaration node);

    /**
     * Visits a ConstructorDeclaration
     * @param node the node to visit
     */
    Object visit(ConstructorDeclaration node);

    /**
     * Visits a MethodDeclaration
     * @param node the node to visit
     */
    Object visit(MethodDeclaration node);

    /**
     * Visits a FormalParameter
     * @param node the node to visit
     */
    Object visit(FormalParameter node);

    /**
     * Visits a FieldDeclaration
     * @param node the node to visit
     */
    Object visit(FieldDeclaration node);

    /**
     * Visits a VariableDeclaration
     * @param node the node to visit
     */
    Object visit(VariableDeclaration node);

    /**
     * Visits a ClassInitializer
     * @param node the node to visit
     */
    Object visit(ClassInitializer node);

    /**
     * Visits a InstanceInitializer
     * @param node the node to visit
     */
    Object visit(InstanceInitializer node);

}
