package edu.rice.cs.drjava.model.repl;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * A subclass of EvaluationVisitor to do two new things.
 * <OL>
 *   <LI>Check thread interrupted status and throw InterruptedException
 *       if the thread was interrupted.</LI>
 *   <LI>Returns JavaInterpreter.NO_RESULT if the computation
 *       had no result. (This is instead of returning null, which
 *       DynamicJava does.</LI>
 * </OL>     
 *
 * @version $Id$
 */

public class EvaluationVisitorExtension extends EvaluationVisitor {
  public EvaluationVisitorExtension(Context ctx) {
    super(ctx);
  }

  private void _checkInterrupted(Node node) {
    String className = node.getClass().getName();

    // An interesting and arcane Thread fact: There are two different
    // methods to check if a Thread is interrupted. (See the javadocs.)
    // Thread.isInterrupted() gets the status but doesn't reset it,
    // while Thread.interrupted() gets the status and resets it.
    // This code did not work when I used isInterrupted.
    if (Thread.currentThread().interrupted()) {
      throw new InterpreterInterruptedException(node.getBeginLine(),
                                                node.getBeginColumn(),
                                                node.getEndLine(),
                                                node.getEndColumn());
    }
  }

  public Object visit(WhileStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(ForStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(DoStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(SwitchStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(LabeledStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(SynchronizedStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(TryStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(IfThenStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(IfThenElseStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(BlockStatement node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(Literal node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(VariableDeclaration node) {
    _checkInterrupted(node);
    super.visit(node);
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(ObjectFieldAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ObjectMethodCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(StaticFieldAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }
  
  public Object visit(SuperFieldAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SuperMethodCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(StaticMethodCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SimpleAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(QualifiedName node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(TypeExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SimpleAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayInitializer node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ArrayAccess node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(InnerAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ClassAllocation node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(NotExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ComplementExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PlusExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MinusExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AddExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AddAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SubtractExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(SubtractAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MultiplyExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(MultiplyAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(DivideExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(DivideAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(RemainderExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(RemainderAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(EqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(NotEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(LessExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(LessOrEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(GreaterExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(GreaterOrEqualExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(InstanceOfExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ConditionalExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PostIncrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PreIncrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PostDecrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PreDecrement node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(CastExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitAndExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitAndAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ExclusiveOrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ExclusiveOrAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitOrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(BitOrAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftLeftExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftLeftAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftRightExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(ShiftRightAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(UnsignedShiftRightExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(UnsignedShiftRightAssignExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(AndExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(OrExpression node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(FunctionCall node) {
    _checkInterrupted(node);
    return super.visit(node);
  }

  public Object visit(PackageDeclaration node) {
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(ImportDeclaration node) {
    return JavaInterpreter.NO_RESULT;
  }

  public Object visit(EmptyStatement node) {
    return JavaInterpreter.NO_RESULT;
  }
}
