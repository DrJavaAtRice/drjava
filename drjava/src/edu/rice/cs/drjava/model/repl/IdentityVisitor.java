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

import java.util.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * This class visits each node and each node's members, recursively
 * walking the syntax tree, returning the identical node back for
 * each case. Note that Try, Catch, and ArrayAllocation statements
 * return a new Node with the same fields because they did not appear
 * to have all of the necessary setters.
 *
 * @version $Id$
 */

public class IdentityVisitor implements Visitor {
  /**
   * Visits a PackageDeclaration
   * @param node the node to visit
   * @return node
   */
  public Object visit(PackageDeclaration node) {
    return node;
  }

  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   * @return node
   */
  public Object visit(ImportDeclaration node) {
    return node;
  }

  /**
   * Visits an EmptyStatement
   * @param node the node to visit
   */
  public Object visit(EmptyStatement node) {
    return node;
  }

  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  public Object visit(WhileStatement node) {
    node.setCondition((Expression)node.getCondition().acceptVisitor(this));
    node.setBody((Node)node.getBody().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Object visit(ForStatement node) {
    LinkedList init = null;
    if (node.getInitialization() != null) {
      init = new LinkedList();
      Iterator it = node.getInitialization().iterator();
      while (it.hasNext()) {
        init.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setInitialization(init);
    Expression cond = null;
    if (node.getCondition() != null) {
      cond = (Expression)node.getCondition().acceptVisitor(this);
    }
    node.setCondition(cond);
    LinkedList updt = null;
    if (node.getUpdate() != null) {
      updt = new LinkedList();
      Iterator it = node.getUpdate().iterator();
      while (it.hasNext()) {
        updt.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setUpdate(updt);
    node.setBody((Node)node.getBody().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Object visit(DoStatement node) {
    Expression cond = (Expression)node.getCondition().acceptVisitor(this);
    node.setCondition(cond);
    Node body = (Node)node.getBody().acceptVisitor(this);
    node.setBody(body);
    return node;
  }

  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Object visit(SwitchStatement node) {
    Expression sel = (Expression)node.getSelector().acceptVisitor(this);
    node.setSelector(sel);
    LinkedList cases = new LinkedList();
    Iterator it = node.getBindings().iterator();
    while (it.hasNext()) {
      cases.add((Node)((Node)it.next()).acceptVisitor(this));
    }
    node.setBindings(cases);
    return node;
  }

  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public Object visit(SwitchBlock node) {
    Expression e = null;
    if (node.getExpression() != null) {
      e = (Expression)node.getExpression().acceptVisitor(this);
    }
    node.setExpression(e);
    LinkedList statements = null;
    if (node.getStatements() != null) {
      statements = new LinkedList();
      Iterator it = node.getStatements().iterator();
      while (it.hasNext()) {
        statements.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setStatements(statements);
    return node;
  }

  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Object visit(LabeledStatement node) {
    node.setStatement((Node)node.getStatement().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a BreakStatement
   * @param node the node to visit
   */
  public Object visit(BreakStatement node) {
    return node;
  }

  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Object visit(TryStatement node) {
    Node tryBlock = (Node)node.getTryBlock().acceptVisitor(this);
    LinkedList catchStatements = new LinkedList();
    Iterator it = node.getCatchStatements().iterator();
    while (it.hasNext()) {
      catchStatements.add((Node)((Node)it.next()).acceptVisitor(this));
    }
    Node finallyBlock = null;
    if (node.getFinallyBlock() != null) {
      finallyBlock = (Node)node.getFinallyBlock().acceptVisitor(this);
    }
    node = new TryStatement(tryBlock, catchStatements, finallyBlock, null, 0, 0, 0, 0);
    return node;
  }

  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public Object visit(CatchStatement node) {
    FormalParameter exp = (FormalParameter)node.getException().acceptVisitor(this);
    Node block = (Node) node.getBlock().acceptVisitor(this);
    node = new CatchStatement(exp, block, null, 0, 0, 0, 0);
    return node;
  }

  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Object visit(ThrowStatement node) {
    node.setExpression((Expression)node.getExpression().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Object visit(ReturnStatement node) {
    node.setExpression((Expression)node.getExpression().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Object visit(SynchronizedStatement node) {
    node.setLock((Expression)node.getLock().acceptVisitor(this));
    node.setBody((Node)node.getBody().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a ContinueStatement
   * @param node the node to visit
   */
  public Object visit(ContinueStatement node) {
    return node;
  }

  /**
   * Visits a IfThenStatement
   * @param node the node to visit
   */
  public Object visit(IfThenStatement node) {
    node.setCondition((Expression)node.getCondition().acceptVisitor(this));
    node.setThenStatement((Node)node.getThenStatement().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a IfThenElseStatement
   * @param node the node to visit
   */
  public Object visit(IfThenElseStatement node) {
    node.setCondition((Expression)node.getCondition().acceptVisitor(this));
    node.setThenStatement((Node)node.getThenStatement().acceptVisitor(this));
    node.setElseStatement((Node)node.getElseStatement().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public Object visit(Literal node) {
    return node;
  }

  /**
   * Visits a ThisExpression
   * @param node the node to visit
   */
  public Object visit(ThisExpression node) {
    return node;
  }

  /**
   * Visits a QualifiedName
   * @param node the node to visit
   */
  public Object visit(QualifiedName node) {
    return node;
  }

  /**
   * Visits a ObjectFieldAccess
   * @param node the node to visit
   */
  public Object visit(ObjectFieldAccess node) {
    node.setExpression((Expression)node.getExpression().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public Object visit(StaticFieldAccess node) {
    node.setFieldType((ReferenceType)node.getFieldType().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a ArrayAccess
   * @param node the node to visit
   */
  public Object visit(ArrayAccess node) {
    node.setExpression((Expression)node.getExpression().acceptVisitor(this));
    node.setCellNumber((Expression)node.getCellNumber().acceptVisitor(this));
    return node;
  }

  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public Object visit(SuperFieldAccess node) {
    return node;
  }

  /**
   * Visits a ObjectMethodCall
   * @param node the node to visit
   */
  public Object visit(ObjectMethodCall node) {
    if (node.getExpression() != null) {
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
    }
    LinkedList arguments = null;
    if (node.getArguments() != null) {
      arguments = new LinkedList();
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        arguments.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setArguments(arguments);
    return node;
  }

  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  public Object visit(FunctionCall node) {
    LinkedList arguments = null;
    if (node.getArguments() != null) {
      arguments = new LinkedList();
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        arguments.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setArguments(arguments);
    return node;
  }

  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public Object visit(StaticMethodCall node) {
    node.setMethodType((ReferenceType)node.getMethodType().acceptVisitor(this));
    LinkedList arguments = null;
    if (node.getArguments() != null) {
      arguments = new LinkedList();
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        arguments.add((Node)((Node)it.next()).acceptVisitor(this));
      }
    }
    node.setArguments(arguments);
    return node;
  }

    /**
     * Visits a ConstructorInvocation
     * @param node the node to visit
     */
    public Object visit(ConstructorInvocation node) {
      if (node.getExpression() != null) {
        node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      }
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      return node;
    }

    /**
     * Visits a SuperMethodCall
     * @param node the node to visit
     */
    public Object visit(SuperMethodCall node) {
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      return node;
    }

    /**
     * Visits a PrimitiveType
     * @param node the node to visit
     */
    public Object visit(PrimitiveType node) {
      return node;
    }

    /**
     * Visits a ReferenceType
     * @param node the node to visit
     */
    public Object visit(ReferenceType node) {
      return node;
    }

    /**
     * Visits a ArrayType
     * @param node the node to visit
     */
    public Object visit(ArrayType node) {
      if (node.getElementType() != null) {
        node.setElementType((Type)node.getElementType().acceptVisitor(this));
      }
      return node;
    }

    /**
     * Visits a TypeExpression
     * @param node the node to visit
     */
    public Object visit(TypeExpression node) {
      // For some reason, the setType expression in node only takes in
      // ReferenceTypes so we have to create a new TypeExpression in
      // case the visitor returns a PrimitiveType (e.g. int.class used
      // to cause a ClassCastException).
      node = new TypeExpression((Type)node.getType().acceptVisitor(this));
      return node;
    }

    /**
     * Visits a PostIncrement
     * @param node the node to visit
     */
    public Object visit(PostIncrement node) {
      return _visitUnary(node);
    }

    /**
     * Visits a PostDecrement
     * @param node the node to visit
     */
    public Object visit(PostDecrement node) {
      return _visitUnary(node);
    }

    /**
     * Visits a PreIncrement
     * @param node the node to visit
     */
    public Object visit(PreIncrement node) {
      return _visitUnary(node);
    }

    /**
     * Visits a PreDecrement
     * @param node the node to visit
     */
    public Object visit(PreDecrement node) {
      return _visitUnary(node);
    }

    /**
     * Visits a ArrayInitializer
     * @param node the node to visit
     */
    public Object visit(ArrayInitializer node) {
      LinkedList cells = new LinkedList();
      Iterator it = node.getCells().iterator();
      while (it.hasNext()) {
        cells.add((Expression)((Expression)it.next()).acceptVisitor(this));
      }
      node.setCells(cells);
      node.setElementType((Type)node.getElementType().acceptVisitor(this));
      return node;
    }

    /**
     * Visits an ArrayAllocation, check me on this one.
     * @param node the node to visit
     */
    public Object visit(ArrayAllocation node) {
      int dim = node.getDimension();
      Type creationType = (Type)node.getCreationType().acceptVisitor(this);
      LinkedList sizes = new LinkedList();
      Iterator it = node.getSizes().iterator();
      while (it.hasNext()) {
        sizes.add((Expression)((Expression)it.next()).acceptVisitor(this));
      }
      ArrayInitializer ai = null;
      if (node.getInitialization() != null) {
        ai = (ArrayInitializer)node.getInitialization().acceptVisitor(this);
      }
      node = new ArrayAllocation(creationType,
                                 new ArrayAllocation.TypeDescriptor(sizes, dim, ai, 0, 0));
      return node;
    }

    /**
     * Visits an SimpleAllocation
     * @param node the node to visit
     */
    public Object visit(SimpleAllocation node) {
      node.setCreationType((Type)node.getCreationType().acceptVisitor(this));
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      return node;
    }

    /**
     * Visits an ClassAllocation
     * @param node the node to visit
     */
    public Object visit(ClassAllocation node) {
      node.setCreationType((Type)node.getCreationType().acceptVisitor(this));
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      LinkedList members = new LinkedList();
      Iterator it = node.getMembers().iterator();
      while (it.hasNext()) {
        members.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setMembers(members);
      return node;
    }

    /**
     * Visits an InnerAllocation
     * @param node the node to visit
     */
    public Object visit(InnerAllocation node) {
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      node.setCreationType((Type)node.getCreationType().acceptVisitor(this));
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      return node;
    }

    /**
     * Visits an InnerClassAllocation
     * @param node the node to visit
     */
    public Object visit(InnerClassAllocation node) {
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      node.setCreationType((Type)node.getCreationType().acceptVisitor(this));
      LinkedList arguments = null;
      if (node.getArguments() != null) {
        arguments = new LinkedList();
        Iterator it = node.getArguments().iterator();
        while (it.hasNext()) {
          arguments.add((Expression)((Expression)it.next()).acceptVisitor(this));
        }
      }
      node.setArguments(arguments);
      LinkedList members = new LinkedList();
      Iterator it = node.getMembers().iterator();
      while (it.hasNext()) {
        members.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setMembers(members);
      return node;
    }

    /**
     * Visits a CastExpression
     * @param node the node to visit
     */
    public Object visit(CastExpression node) {
      node.setTargetType((Type)node.getTargetType().acceptVisitor(this));
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      return node;
    }

    /**
     * Visits a NotExpression
     * @param node the node to visit
     */
    public Object visit(NotExpression node) {
      return _visitUnary(node);
    }

    /**
     * Visits a ComplementExpression
     * @param node the node to visit
     */
    public Object visit(ComplementExpression node) {
      return _visitUnary(node);
    }

    /**
     * Visits a PlusExpression
     * @param node the node to visit
     */
    public Object visit(PlusExpression node) {
      return _visitUnary(node);
    }

    /**
     * Visits a MinusExpression
     * @param node the node to visit
     */
    public Object visit(MinusExpression node) {
      return _visitUnary(node);
    }

    /**
     * Visits a MultiplyExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a DivideExpression
     * @param node the node to visit
     */
    public Object visit(DivideExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a RemainderExpression
     * @param node the node to visit
     */
    public Object visit(RemainderExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a AddExpression
     * @param node the node to visit
     */
    public Object visit(AddExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a SubtractExpression
     * @param node the node to visit
     */
    public Object visit(SubtractExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a ShiftLeftExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a ShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a UnsignedShiftRightExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a LessExpression
     * @param node the node to visit
     */
    public Object visit(LessExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a GreaterExpression
     * @param node the node to visit
     */
    public Object visit(GreaterExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a LessOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(LessOrEqualExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a GreaterOrEqualExpression
     * @param node the node to visit
     */
    public Object visit(GreaterOrEqualExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a InstanceOfExpression
     * @param node the node to visit
     */
    public Object visit(InstanceOfExpression node) {
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      node.setReferenceType((Type)node.getReferenceType().acceptVisitor(this));
      return node;
    }

    /**
     * Visits a EqualExpression
     * @param node the node to visit
     */
    public Object visit(EqualExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a NotEqualExpression
     * @param node the node to visit
     */
    public Object visit(NotEqualExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a BitAndExpression
     * @param node the node to visit
     */
    public Object visit(BitAndExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a ExclusiveOrExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a BitOrExpression
     * @param node the node to visit
     */
    public Object visit(BitOrExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an AndExpression
     * @param node the node to visit
     */
    public Object visit(AndExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an OrExpression
     * @param node the node to visit
     */
    public Object visit(OrExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a ConditionalExpression
     * @param node the node to visit
     */
    public Object visit(ConditionalExpression node) {
      node.setConditionExpression((Expression)node.getConditionExpression().acceptVisitor(this));
      node.setIfTrueExpression((Expression)node.getIfTrueExpression().acceptVisitor(this));
      node.setIfFalseExpression((Expression)node.getIfFalseExpression().acceptVisitor(this));
      return node;
    }

    /**
     * Visits an SimpleAssignExpression
     * @param node the node to visit
     */
    public Object visit(SimpleAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an MultiplyAssignExpression
     * @param node the node to visit
     */
    public Object visit(MultiplyAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an DivideAssignExpression
     * @param node the node to visit
     */
    public Object visit(DivideAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an RemainderAssignExpression
     * @param node the node to visit
     */
    public Object visit(RemainderAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an AddAssignExpression
     * @param node the node to visit
     */
    public Object visit(AddAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an SubtractAssignExpression
     * @param node the node to visit
     */
    public Object visit(SubtractAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an ShiftLeftAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftLeftAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an ShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(ShiftRightAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits an UnsignedShiftRightAssignExpression
     * @param node the node to visit
     */
    public Object visit(UnsignedShiftRightAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a BitAndAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitAndAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a ExclusiveOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(ExclusiveOrAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a BitOrAssignExpression
     * @param node the node to visit
     */
    public Object visit(BitOrAssignExpression node) {
      return _visitBinary(node);
    }

    /**
     * Visits a BlockStatement
     * @param node the node to visit
     */
    public Object visit(BlockStatement node) {
      LinkedList statements = new LinkedList();
      Iterator it = node.getStatements().iterator();
      while (it.hasNext()) {
        statements.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setStatements(statements);
      return node;
    }

    /**
     * Visits a ClassDeclaration
     * @param node the node to visit
     */
    public Object visit(ClassDeclaration node) {
      LinkedList members = new LinkedList();
      Iterator it = node.getMembers().iterator();
      while (it.hasNext()) {
        members.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setMembers(members);
      return node;
    }

    /**
     * Visits an InterfaceDeclaration
     * @param node the node to visit
     */
    public Object visit(InterfaceDeclaration node) {
      LinkedList members = new LinkedList();
      Iterator it = node.getMembers().iterator();
      while (it.hasNext()) {
        members.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setMembers(members);
      return node;
    }

    /**
     * Visits a ConstructorDeclaration
     * @param node the node to visit
     */
    public Object visit(ConstructorDeclaration node) {
      LinkedList parameters = new LinkedList();
      Iterator it = node.getParameters().iterator();
      while (it.hasNext()) {
        parameters.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setParameters(parameters);
      if (node.getConstructorInvocation() != null) {
        node.setConstructorInvocation((ConstructorInvocation)node.getConstructorInvocation().acceptVisitor(this));
      }
      LinkedList statements = new LinkedList();
      it = node.getStatements().iterator();
      while (it.hasNext()) {
        statements.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setStatements(statements);
      return node;
    }

    /**
     * Visits a MethodDeclaration
     * @param node the node to visit
     */
    public Object visit(MethodDeclaration node) {
      node.setReturnType((Type)node.getReturnType().acceptVisitor(this));
      LinkedList parameters = new LinkedList();
      Iterator it = node.getParameters().iterator();
      while (it.hasNext()) {
        parameters.add((Node)((Node)it.next()).acceptVisitor(this));
      }
      node.setParameters(parameters);
      if (node.getBody() != null) {
        node.setBody((BlockStatement)node.getBody().acceptVisitor(this));
      }
      return node;
    }

    /**
     * Visits a FormalParameter
     * @param node the node to visit
     */
    public Object visit(FormalParameter node) {
      node.setType((Type)node.getType().acceptVisitor(this));
      return node;
    }

    /**
     * Visits a FieldDeclaration
     * @param node the node to visit
     */
    public Object visit(FieldDeclaration node) {
      node.setType((Type)node.getType().acceptVisitor(this));
      if (node.getInitializer() != null) {
        node.setInitializer((Expression)node.getInitializer().acceptVisitor(this));
      }
      return node;
    }

    /**
     * Visits a VariableDeclaration
     * @param node the node to visit
     */
    public Object visit(VariableDeclaration node) {
      node.setType((Type)node.getType().acceptVisitor(this));
      if (node.getInitializer() != null) {
        node.setInitializer((Expression)node.getInitializer().acceptVisitor(this));
      }
      return node;
    }

    /**
     * Visits a ClassInitializer
     * @param node the node to visit
     */
    public Object visit(ClassInitializer node) {
      node.setBlock((BlockStatement)node.getBlock().acceptVisitor(this));
      return node;
    }

    /**
     * Visits a InstanceInitializer
     * @param node the node to visit
     */
    public Object visit(InstanceInitializer node) {
      node.setBlock((BlockStatement)node.getBlock().acceptVisitor(this));
      return node;
    }

    private Object _visitUnary(UnaryExpression node) {
      node.setExpression((Expression)node.getExpression().acceptVisitor(this));
      return node;
    }

    private Object _visitBinary(BinaryExpression node) {
      node.setLeftExpression((Expression)node.getLeftExpression().acceptVisitor(this));
      node.setRightExpression((Expression)node.getRightExpression().acceptVisitor(this));
      return node;
    }
}
