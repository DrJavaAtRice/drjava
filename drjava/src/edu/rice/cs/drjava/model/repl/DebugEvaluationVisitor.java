/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

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
  protected Context _context;

  /** The name of the interpreter enclosing this visitor. */
  protected final String _name;

  /** Creates a new debug visitor.
   *  @param ctx the context
   *  @param name the name of the enclosing interpreter
   */
  public DebugEvaluationVisitor(Context ctx, String name) {
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