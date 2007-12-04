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
 * A visitor that, by default, invokes the method defaultCase().  If defaultCase is not overriden,
 * this results in an IllegalArgumentException.
 * 
 * TODO: Add GenericReferenceTypeName and HookTypeName cases
 */

public abstract class AbstractVisitor<T> implements Visitor<T> {
  
  public T defaultCase(Node node) {
    throw new IllegalArgumentException("Visitor is undefined for " + node.getClass().getName());
  }
  
  /**
   * Visits an PackageDeclaration
   * @param node the node to visit
   */
  public T visit(PackageDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   */
  public T visit(ImportDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an EmptyStatement
   * @param node the node to visit
   */
  public T visit(EmptyStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  public T visit(WhileStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public T visit(ForStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   */
  public T visit(ForEachStatement node) {
    return defaultCase(node);
  }

  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public T visit(DoStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public T visit(SwitchStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public T visit(SwitchBlock node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public T visit(LabeledStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BreakStatement
   * @param node the node to visit
   */
  public T visit(BreakStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public T visit(TryStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public T visit(CatchStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public T visit(ThrowStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public T visit(ReturnStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public T visit(SynchronizedStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ContinueStatement
   * @param node the node to visit
   */
  public T visit(ContinueStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a IfThenStatement
   * @param node the node to visit
   */
  public T visit(IfThenStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a IfThenElseStatement
   * @param node the node to visit
   */
  public T visit(IfThenElseStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an AssertStatement
   * @param node the node to visit
   */
  public T visit(AssertStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public T visit(Literal node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ThisExpression
   * @param node the node to visit
   */
  public T visit(ThisExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a AmbiguousName
   * @param node the node to visit
   */
  public T visit(AmbiguousName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a VariableAccess
   * @param node the node to visit
   */
  public T visit(VariableAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SimpleFieldAccess
   * @param node the node to visit
   */
  public T visit(SimpleFieldAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ObjectFieldAccess
   * @param node the node to visit
   */
  public T visit(ObjectFieldAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public T visit(StaticFieldAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public T visit(SuperFieldAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ArrayAccess
   * @param node the node to visit
   */
  public T visit(ArrayAccess node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ObjectMethodCall
   * @param node the node to visit
   */
  public T visit(ObjectMethodCall node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SimpleMethodCall
   * @param node the node to visit
   */
  public T visit(SimpleMethodCall node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public T visit(StaticMethodCall node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ConstructorCall
   * @param node the node to visit
   */
  public T visit(ConstructorCall node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public T visit(SuperMethodCall node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BooleanTypeName
   * @param node the node to visit
   */
  public T visit(BooleanTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ByteTypeName
   * @param node the node to visit
   */
  public T visit(ByteTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ShortTypeName
   * @param node the node to visit
   */
  public T visit(ShortTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a CharTypeName
   * @param node the node to visit
   */
  public T visit(CharTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a IntTypeName
   * @param node the node to visit
   */
  public T visit(IntTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a LongTypeName
   * @param node the node to visit
   */
  public T visit(LongTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a FloatTypeName
   * @param node the node to visit
   */
  public T visit(FloatTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a DoubleTypeName
   * @param node the node to visit
   */
  public T visit(DoubleTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a VoidTypeName
   * @param node the node to visit
   */
  public T visit(VoidTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ReferenceTypeName
   * @param node the node to visit
   */
  public T visit(ReferenceTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a GenericReferenceTypeName
   * @param node the node to visit
   */
  public T visit(GenericReferenceTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ArrayTypeName
   * @param node the node to visit
   */
  public T visit(ArrayTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a HookTypeName
   * @param node the node to visit
   */
  public T visit(HookTypeName node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  public T visit(TypeExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public T visit(PostIncrement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public T visit(PostDecrement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public T visit(PreIncrement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public T visit(PreDecrement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an ArrayInitializer
   * @param node the node to visit
   */
  public T visit(ArrayInitializer node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public T visit(ArrayAllocation node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  public T visit(SimpleAllocation node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an AnonymousAllocation
   * @param node the node to visit
   */
  public T visit(AnonymousAllocation node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an InnerAllocation
   * @param node the node to visit
   */
  public T visit(InnerAllocation node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an AnonymousInnerAllocation
   * @param node the node to visit
   */
  public T visit(AnonymousInnerAllocation node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public T visit(CastExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public T visit(NotExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public T visit(ComplementExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public T visit(PlusExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public T visit(MinusExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public T visit(MultiplyExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public T visit(DivideExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public T visit(RemainderExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a AddExpression
   * @param node the node to visit
   */
  public T visit(AddExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public T visit(SubtractExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public T visit(ShiftLeftExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public T visit(ShiftRightExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public T visit(UnsignedShiftRightExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public T visit(LessExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public T visit(GreaterExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public T visit(LessOrEqualExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public T visit(GreaterOrEqualExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an InstanceOfExpression
   * @param node the node to visit
   */
  public T visit(InstanceOfExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a EqualExpression
   * @param node the node to visit
   */
  public T visit(EqualExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  public T visit(NotEqualExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public T visit(BitAndExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public T visit(ExclusiveOrExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public T visit(BitOrExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a AndExpression
   * @param node the node to visit
   */
  public T visit(AndExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a OrExpression
   * @param node the node to visit
   */
  public T visit(OrExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public T visit(ConditionalExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an SimpleAssignExpression
   * @param node the node to visit
   */
  public T visit(SimpleAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public T visit(MultiplyAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public T visit(DivideAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  public T visit(RemainderAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public T visit(AddAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public T visit(SubtractAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public T visit(ShiftLeftAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an ShiftRightAssignExpression
   * @param node the node to visit
   */
  public T visit(ShiftRightAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits an UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public T visit(UnsignedShiftRightAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public T visit(BitAndAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public T visit(ExclusiveOrAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public T visit(BitOrAssignExpression node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public T visit(BlockStatement node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ClassDeclaration
   * @param node the node to visit
   */
  public T visit(ClassDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a InterfaceDeclaration
   * @param node the node to visit
   */
  public T visit(InterfaceDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ConstructorDeclaration
   * @param node the node to visit
   */
  public T visit(ConstructorDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a MethodDeclaration
   * @param node the node to visit
   */
  public T visit(MethodDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a FormalParameter
   * @param node the node to visit
   */
  public T visit(FormalParameter node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a FieldDeclaration
   * @param node the node to visit
   */
  public T visit(FieldDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  public T visit(VariableDeclaration node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a ClassInitializer
   * @param node the node to visit
   */
  public T visit(ClassInitializer node) {
    return defaultCase(node);
  }
  
  /**
   * Visits a InstanceInitializer
   * @param node the node to visit
   */
  public T visit(InstanceInitializer node) {
    return defaultCase(node);
  }
  
}
