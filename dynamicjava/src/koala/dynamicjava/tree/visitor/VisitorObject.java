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
 * This class implements all the methods of Visitor but do nothing
 * (it returns null at each call to 'visit'). This class exists as
 * convenience for creating visitor objects
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class VisitorObject implements Visitor {
    /**
     * Visits an PackageDeclaration
     * @param node the node to visit
     */
    public Object visit(PackageDeclaration node) {
	return null;
    }

    /**
     * Visits an ImportDeclaration
     * @param node the node to visit
     */
    public Object visit(ImportDeclaration node) {
	return null;
    }

    /**
     * Visits an EmptyStatement
     * @param node the node to visit
     */
    public Object visit(EmptyStatement node) {
	return null;
    }

    /**
     * Visits a WhileStatement
     * @param node the node to visit
     */
    public Object visit(WhileStatement node) {
	return null;
    }

    /**
     * Visits a ForStatement
     * @param node the node to visit
     */
    public Object visit(ForStatement node) {
	return null;
    }

    /**
     * Visits a DoStatement
     * @param node the node to visit
     */
    public Object visit(DoStatement node) {
	return null;
    }

    /**
     * Visits a SwitchStatement
     * @param node the node to visit
     */
    public Object visit(SwitchStatement node) {
	return null;
    }

    /**
     * Visits a SwitchBlock
     * @param node the node to visit
     */
    public Object visit(SwitchBlock node) {
	return null;
    }

    /**
     * Visits a LabeledStatement
     * @param node the node to visit
     */
    public Object visit(LabeledStatement node) {
	return null;
    }

    /**
     * Visits a BreakStatement
     * @param node the node to visit
     */
    public Object visit(BreakStatement node) {
	return null;
    }

    /**
     * Visits a TryStatement
     * @param node the node to visit
     */
    public Object visit(TryStatement node) {
	return null;
    }

    /**
     * Visits a CatchStatement
     * @param node the node to visit
     */
    public Object visit(CatchStatement node) {
	return null;
    }

    /**
     * Visits a ThrowStatement
     * @param node the node to visit
     */
    public Object visit(ThrowStatement node) {
	return null;
    }

    /**
     * Visits a ReturnStatement
     * @param node the node to visit
     */
    public Object visit(ReturnStatement node) {
	return null;
    }

    /**
     * Visits a SynchronizedStatement
     * @param node the node to visit
     */
    public Object visit(SynchronizedStatement node) {
	return null;
    }

    /**
     * Visits a ContinueStatement
     * @param node the node to visit
     */
    public Object visit(ContinueStatement node) {
	return null;
    }

    /**
     * Visits a IfThenStatement
     * @param node the node to visit
     */
    public Object visit(IfThenStatement node) {
	return null;
    }

    /**
     * Visits a IfThenElseStatement
     * @param node the node to visit
     */
    public Object visit(IfThenElseStatement node) {
	return null;
    }

    /**
     * Visits a Literal
     * @param node the node to visit
     */
    public Object visit(Literal node) {
	return null;
    }

    /**
     * Visits a ThisExpression
     * @param node the node to visit
     */
    public Object visit(ThisExpression node) {
	return null;
    }

    /**
     * Visits a QualifiedName
     * @param node the node to visit
     */
    public Object visit(QualifiedName node) {
	return null;
    }

    /**
     * Visits a ObjectFieldAccess
     * @param node the node to visit
     */
    public Object visit(ObjectFieldAccess node) {
	return null;
    }

    /**
     * Visits a StaticFieldAccess
     * @param node the node to visit
     */
    public Object visit(StaticFieldAccess node) {
	return null;
    }

    /**
     * Visits a ArrayAccess
     * @param node the node to visit
     */
    public Object visit(ArrayAccess node) {
	return null;
    }

    /**
     * Visits a SuperFieldAccess
     * @param node the node to visit
     */
    public Object visit(SuperFieldAccess node) {
	return null;
    }

    /**
     * Visits a ObjectMethodCall
     * @param node the node to visit
     */
    public Object visit(ObjectMethodCall node) {
	return null;
    }

    /**
     * Visits a FunctionCall
     * @param node the node to visit
     */
    public Object visit(FunctionCall node) {
	return null;
    }

    /**
     * Visits a StaticMethodCall
     * @param node the node to visit
     */
    public Object visit(StaticMethodCall node) {
	return null;
    }

    /**
     * Visits a ConstructorInvocation
     * @param node the node to visit
     */
    public Object visit(ConstructorInvocation node) {
	return null;
    }

    /**
     * Visits a SuperMethodCall
     * @param node the node to visit
     */
    public Object visit(SuperMethodCall node) {
	return null;
    }

    /**
     * Visits a PrimitiveType
     * @param node the node to visit
     */
    public Object visit(PrimitiveType node) {
	return null;
    }

    /**
     * Visits a ReferenceType
     * @param node the node to visit
     */
    public Object visit(ReferenceType node) {
	return null;
    }

    /**
     * Visits a ArrayType
     * @param node the node to visit
     */
    public Object visit(ArrayType node) {
	return null;
    }

    /**
     * Visits a TypeExpression
     * @param node the node to visit
     */
    public Object visit(TypeExpression node) {
	return null;
    }

    /**
     * Visits a PostIncrement
     * @param node the node to visit
     */
    public Object visit(PostIncrement node) {
	return null;
    }

    /**
     * Visits a PostDecrement
     * @param node the node to visit
     */
    public Object visit(PostDecrement node) {
	return null;
    }

    /**
     * Visits a PreIncrement
     * @param node the node to visit
     */
    public Object visit(PreIncrement node) {
	return null;
    }

    /**
     * Visits a PreDecrement
     * @param node the node to visit
     */
    public Object visit(PreDecrement node) {
	return null;
    }

    /**
     * Visits an ArrayInitializer
     * @param node the node to visit
     */
    public Object visit(ArrayInitializer node) {
	return null;
    }

    /**
     * Visits an ArrayAllocation
     * @param node the node to visit
     */
    public Object visit(ArrayAllocation node) {
	return null;
    }

    /**
     * Visits an SimpleAllocation
     * @param node the node to visit
     */
    public Object visit(SimpleAllocation node) {
	return null;
    }

    /**
     * Visits an ClassAllocation
     * @param node the node to visit
     */
    public Object visit(ClassAllocation node) {
	return null;
    }

    /**
     * Visits an InnerAllocation
     * @param node the node to visit
     */
    public Object visit(InnerAllocation node) {
	return null;
    }

    /**
     * Visits an InnerClassAllocation
     * @param node the node to visit
     */
    public Object visit(InnerClassAllocation node) {
	return null;
    }

    /**
     * Visits a CastExpression
     * @param node the node to visit
     */
    public Object visit(CastExpression node) {
	return null;
    }

    /**
     * Visits a NotExpression
     * @param node the node to visit
     */
    public Object visit(NotExpression node) {
	return null;
    }

    /**
     * Visits a ComplementExpression
     * @param node the node to visit
     */
    public Object visit(ComplementExpression node) {
	return null;
    }

    /**
     * Visits a PlusExpression
     * @param node the node to visit
     */
    public Object visit(PlusExpression node) {
	return null;
    }

    /**
     * Visits a MinusExpression
     * @param node the node to visit
     */
    public Object visit(MinusExpression node) {
	return null;
    }

    /**
     * Visits a MultiplyExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyExpression node) {
	return null;
    }

    /**
     * Visits a DivideExpression
     * @param node the node to visit
     */
    public Object visit(DivideExpression node) {
	return null;
    }

    /**
     * Visits a RemainderExpression
     * @param node the node to visit
     */
    public Object visit(RemainderExpression node) {
	return null;
    }

    /**
     * Visits a AddExpression
     * @param node the node to visit
     */
    public Object visit(AddExpression node) {
	return null;
    }

    /**
     * Visits a SubtractExpression
     * @param node the node to visit
     */
    public Object visit(SubtractExpression node) {
	return null;
    }

    /**
     * Visits a ShiftLeftExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftExpression node) {
	return null;
    }

    /**
     * Visits a ShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightExpression node) {
	return null;
    }

    /**
     * Visits a UnsignedShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightExpression node) {
	return null;
    }

    /**
     * Visits a LessExpression
     * @param node the node to visit
     */
    public Object visit(LessExpression node) {
	return null;
    }

    /**
     * Visits a GreaterExpression
     * @param node the node to visit
     */
    public Object visit(GreaterExpression node) {
	return null;
    }

    /**
     * Visits a LessOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(LessOrEqualExpression node) {
	return null;
    }

    /**
     * Visits a GreaterOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(GreaterOrEqualExpression node) {
	return null;
    }

    /**
     * Visits an InstanceOfExpression
     * @param node the node to visit
     */
    public Object visit(InstanceOfExpression node) {
	return null;
    }

    /**
     * Visits a EqualExpression
     * @param node the node to visit
     */
    public Object visit(EqualExpression node) {
	return null;
    }

    /**
     * Visits a NotEqualExpression
     * @param node the node to visit
     */
    public Object visit(NotEqualExpression node) {
	return null;
    }

    /**
     * Visits a BitAndExpression
     * @param node the node to visit
     */
    public Object visit(BitAndExpression node) {
	return null;
    }

    /**
     * Visits a ExclusiveOrExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrExpression node) {
	return null;
    }

    /**
     * Visits a BitOrExpression
     * @param node the node to visit
     */
    public Object visit(BitOrExpression node) {
	return null;
    }

    /**
     * Visits a AndExpression
     * @param node the node to visit
     */
    public Object visit(AndExpression node) {
	return null;
    }

    /**
     * Visits a OrExpression
     * @param node the node to visit
     */
    public Object visit(OrExpression node) {
	return null;
    }

    /**
     * Visits a ConditionalExpression
     * @param node the node to visit
     */
    public Object visit(ConditionalExpression node) {
	return null;
    }

    /**
     * Visits an SimpleAssignExpression
     * @param node the node to visit
     */
    public Object visit(SimpleAssignExpression node) {
	return null;
    }

    /**
     * Visits an MultiplyAssignExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyAssignExpression node) {
	return null;
    }

    /**
     * Visits an DivideAssignExpression
     * @param node the node to visit
     */
    public Object visit(DivideAssignExpression node) {
	return null;
    }

    /**
     * Visits an RemainderAssignExpression
     * @param node the node to visit
     */
    public Object visit(RemainderAssignExpression node) {
	return null;
    }

    /**
     * Visits an AddAssignExpression
     * @param node the node to visit
     */
    public Object visit(AddAssignExpression node) {
	return null;
    }

    /**
     * Visits an SubtractAssignExpression
     * @param node the node to visit
     */
    public Object visit(SubtractAssignExpression node) {
	return null;
    }

    /**
     * Visits an ShiftLeftAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftAssignExpression node) {
	return null;
    }

    /**
     * Visits an ShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightAssignExpression node) {
	return null;
    }

    /**
     * Visits an UnsignedShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightAssignExpression node) {
	return null;
    }

    /**
     * Visits a BitAndAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitAndAssignExpression node) {
	return null;
    }

    /**
     * Visits a ExclusiveOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrAssignExpression node) {
	return null;
    }

    /**
     * Visits a BitOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitOrAssignExpression node) {
	return null;
    }

    /**
     * Visits a BlockStatement
     * @param node the node to visit
     */
    public Object visit(BlockStatement node) {
	return null;
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Object visit(ClassDeclaration node) {
	return null;
    }

    /**
     * Visits a InterfaceDeclaration
     * @param node the node to visit
     */
    public Object visit(InterfaceDeclaration node) {
	return null;
    }

    /**
     * Visits a ConstructorDeclaration
     * @param node the node to visit
     */
    public Object visit(ConstructorDeclaration node) {
	return null;
    }

    /**
     * Visits a MethodDeclaration
     * @param node the node to visit
     */
    public Object visit(MethodDeclaration node) {
	return null;
    }

    /**
     * Visits a FormalParameter
     * @param node the node to visit
     */
    public Object visit(FormalParameter node) {
	return null;
    }

    /**
     * Visits a FieldDeclaration
     * @param node the node to visit
     */
    public Object visit(FieldDeclaration node) {
	return null;
    }

    /**
     * Visits a VariableDeclaration
     * @param node the node to visit
     */
    public Object visit(VariableDeclaration node) {
	return null;
    }

    /**
     * Visits a ClassInitializer
     * @param node the node to visit
     */
    public Object visit(ClassInitializer node) {
	return null;
    }

    /**
     * Visits a InstanceInitializer
     * @param node the node to visit
     */
    public Object visit(InstanceInitializer node) {
	return null;
    }

}
