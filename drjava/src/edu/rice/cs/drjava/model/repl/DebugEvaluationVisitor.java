/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;

/** Extension of EvaluationVisitorExtension that notifies InterpreterJVM every time a variable assignment is made.
 *  (But this functionality has been commented out.)
 *  This class is loaded in the Interpreter JVM, not the Main JVM.
 *  (Do not use DrJava's config framework here.)
 *
 *  @version $Id$
 */
public class DebugEvaluationVisitor extends EvaluationVisitorExtension {
  
  /** The context associated with this visitor. */
  protected Context<Object> _context;

  /** The name of the interpreter enclosing this visitor. */
  protected final String _name;

  /** Creates a new debug visitor.
   *  @param ctx the context
   *  @param name the name of the enclosing interpreter
   */
  public DebugEvaluationVisitor(Context<Object> ctx, String name) {
    super(ctx);
    _context = ctx;
    _name = name;
  }

//  /** Visits the node, as per the superclass, then notifies the interpreter that something has been assigned.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   */
//  protected void _notifyAssigned(Expression e) {
//    InterpreterJVM.ONLY.notifyInterpreterAssignment(_name);
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(SimpleAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(AddAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(SubtractAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(MultiplyAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied bac until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(DivideAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(RemainderAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(BitAndAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(ExclusiveOrAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(BitOrAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(ShiftLeftAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(ShiftRightAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }

//  /** Notifies the InterpreterJVM that an assignment has been made and delegates to the superclass.
//   *  Not currently necessary, since variables aren't copied back until the thread is resumed.
//   *
//   *  @param node the node to visit
//   *  @return the result of calling this method on the superclass
//   */
//  public Object visit(UnsignedShiftRightAssignExpression node) {
//    Object result = super.visit(node);
//    _notifyAssigned(node.getLeftExpression());
//    return result;
//  }
}