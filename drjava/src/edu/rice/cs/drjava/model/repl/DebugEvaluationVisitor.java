/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;

import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;

/**
 * Extension of EvaluationVisitorExtension that notifies InterpreterJVM
 * every time a variable assignment is made.
 * @version $Id$
 */
public class DebugEvaluationVisitor extends EvaluationVisitorExtension {
  /**
   * The context associated with this visitor.
   */
  protected Context _context;
  
  /**
   * The name of the interpreter enclosing this visitor.
   */
  protected final String _name;

  /**
   * Creates a new debug visitor.
   * @param ctx the context 
   * @param name the name of the enclosing interpreter
   */
  public DebugEvaluationVisitor(Context ctx, String name) {
    super(ctx);
    _context = ctx;
    _name = name;
  }

  /**
   * Visits the node, as per the superclass, then notifies the interpreter
   * that something has been assigned.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   *
  protected void _notifyAssigned(Expression e) {
    InterpreterJVM.ONLY.notifyInterpreterAssignment(_name);
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(SimpleAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(AddAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(SubtractAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(MultiplyAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(DivideAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(RemainderAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(BitAndAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(ExclusiveOrAssignExpression node) {
    Object result = super.visit(node);  
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(BitOrAssignExpression node) {
    Object result = super.visit(node); 
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(ShiftLeftAssignExpression node) {
    Object result = super.visit(node); 
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(ShiftRightAssignExpression node) {
    Object result = super.visit(node); 
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }

  /**
   * Notifies the InterpreterJVM that an assignment has been made
   * and delegates to the superclass.
   * 
   * Not currently necessary, since variables aren't copied back
   * until the thread is resumed.
   * 
   * @param node the node to visit
   * @return the result of calling this method on the superclass
   *
  public Object visit(UnsignedShiftRightAssignExpression node) {
    Object result = super.visit(node); 
    _notifyAssigned(node.getLeftExpression()); 
    return result; 
  }*/
}