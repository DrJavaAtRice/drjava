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

package koala.dynamicjava.util;

import java.io.*;
import java.util.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * This tree visitor displays the nodes of the tree on
 * a given stream
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class DisplayVisitor extends VisitorObject<Void> {
  /**
   * The output stream
   */
  private PrintStream out;
  
  /**
   * The current indentation
   */
  private String indentation;
  
  /**
   * Creates a new display visitor
   * @param os the output tree
   */
  public DisplayVisitor(OutputStream os) {
    out         = new PrintStream(os);
    indentation = "";
  }
  
  /**
   * Visits a PackageDeclaration
   * @param node the node to visit
   * @return null
   */
  public Void visit(PackageDeclaration node) {
    print("l."+node.getBeginLine()+" PackageDeclaration "+node.getName()+" {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   * @return null
   */
  public Void visit(ImportDeclaration node) {
    print("l."+node.getBeginLine()+" ImportDeclaration "+node.getName()
            +(node.isPackage() ? ".*" : "")+" {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an EmptyStatement
   * @param node the node to visit
   */
  public Void visit(EmptyStatement node) {
    print("l."+node.getBeginLine()+" EmptyStatement {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  public Void visit(WhileStatement node) {
    print("l."+node.getBeginLine()+" WhileStatement {");
    print("condition:");
    indent();
    node.getCondition().acceptVisitor(this);
    unindent();
    print("body:");
    indent();
    node.getBody().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Void visit(ForStatement node) {
    print("l."+node.getBeginLine()+" ForStatement {");
    print("initialization:");
    if (node.getInitialization() != null) {
      indent();
      Iterator it = node.getInitialization().iterator();
      while (it.hasNext()) {
        ((Node)it.next()).acceptVisitor(this);
      }
      unindent();
    }
    print("condition:");
    if (node.getCondition() != null) {
      indent();
      node.getCondition().acceptVisitor(this);
      unindent();
    }
    print("update:");
    if (node.getUpdate() != null) {
      indent();
      Iterator it = node.getUpdate().iterator();
      while (it.hasNext()) {
        ((Node)it.next()).acceptVisitor(this);
      }
      unindent();
    }
    print("body:");
    indent();
    node.getBody().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Void visit(DoStatement node) {
    print("l."+node.getBeginLine()+" DoStatement {");
    print("condition:");
    indent();
    node.getCondition().acceptVisitor(this);
    unindent();
    print("body:");
    indent();
    node.getBody().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Void visit(SwitchStatement node) {
    print("l."+node.getBeginLine()+" SwitchStatement {");
    print("selector:");
    indent();
    node.getSelector().acceptVisitor(this);
    unindent();
    print("bindings:");
    indent();
    Iterator it = node.getBindings().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public Void visit(SwitchBlock node) {
    print("l."+node.getBeginLine()+" SwitchBlock {");
    print("expression:");
    indent();
    if (node.getExpression() != null) {
      node.getExpression().acceptVisitor(this);
    } else {
      print("default");
    }
    unindent();
    print("statements:");
    indent();
    if (node.getStatements() != null) {
      Iterator it = node.getStatements().iterator();
      while (it.hasNext()) {
        ((Node)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Void visit(LabeledStatement node) {
    print("l."+node.getBeginLine()+" LabeledStatement {");
    print("label:");
    indent();
    print(node.getLabel());
    unindent();
    print("statement:");
    indent();
    node.getStatement().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a BreakStatement
   * @param node the node to visit
   */
  public Void visit(BreakStatement node) {
    print("l."+node.getBeginLine()+" BreakStatement {");
    print("label:");
    indent();
    print(node.getLabel());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Void visit(TryStatement node) {
    print("l."+node.getBeginLine()+" TryStatement {");
    print("tryBlock:");
    indent();
    node.getTryBlock().acceptVisitor(this);
    unindent();
    print("catchStatements:");
    indent();
    Iterator it = node.getCatchStatements().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    print("finallyBlock:");
    indent();
    if (node.getFinallyBlock() != null) {
      node.getFinallyBlock().acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public Void visit(CatchStatement node) {
    print("l."+node.getBeginLine()+" CatchStatement {");
    print("exception:");
    indent();
    node.getException().acceptVisitor(this);
    unindent();
    print("block:");
    indent();
    node.getBlock().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Void visit(ThrowStatement node) {
    print("l."+node.getBeginLine()+" ThrowStatement {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Void visit(ReturnStatement node) {
    print("l."+node.getBeginLine()+" ReturnStatement {");
    print("expression:");
    indent();
    //Bug fix to allow for "return;"
    if( node.getExpression() != null )
      node.getExpression().acceptVisitor(this);
    else
      print("null");
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Void visit(SynchronizedStatement node) {
    print("l."+node.getBeginLine()+" SynchronizedStatement {");
    print("lock:");
    indent();
    node.getLock().acceptVisitor(this);
    unindent();
    print("body:");
    indent();
    node.getBody().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ContinueStatement
   * @param node the node to visit
   */
  public Void visit(ContinueStatement node) {
    print("l."+node.getBeginLine()+" ContinueStatement {");
    print("label:");
    indent();
    print(node.getLabel());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an IfThenStatement
   * @param node the node to visit
   */
  public Void visit(IfThenStatement node) {
    print("l."+node.getBeginLine()+" IfThenStatement {");
    print("condition:");
    indent();
    node.getCondition().acceptVisitor(this);
    unindent();
    print("thenStatement:");
    indent();
    node.getThenStatement().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an IfThenElseStatement
   * @param node the node to visit
   */
  public Void visit(IfThenElseStatement node) {
    print("l."+node.getBeginLine()+" IfThenElseStatement {");
    print("condition:");
    indent();
    node.getCondition().acceptVisitor(this);
    unindent();
    print("thenStatement:");
    indent();
    node.getThenStatement().acceptVisitor(this);
    unindent();
    print("elseStatement:");
    indent();
    node.getElseStatement().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public Void visit(Literal node) {
    print("l."+node.getBeginLine()+" Literal ("+
          node.getType()+") <"+node.getValue()+"> {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ThisExpression
   * @param node the node to visit
   */
  public Void visit(ThisExpression node) {
    print("l."+node.getBeginLine()+" ThisExpression {");
    print("className:");
    indent();
    print(node.getClassName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a QualifiedName
   * @param node the node to visit
   */
  public Void visit(QualifiedName node) {
    print("l."+node.getBeginLine()+" QualifiedName {");
    print("representation:");
    indent();
    print(node.getRepresentation());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  public Void visit(ObjectFieldAccess node) {
    print("l."+node.getBeginLine()+" ObjectFieldAccess {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("fieldName:");
    indent();
    print(node.getFieldName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public Void visit(StaticFieldAccess node) {
    print("l."+node.getBeginLine()+" StaticFieldAccess {");
    print("fieldType:");
    indent();
    node.getFieldType().acceptVisitor(this);
    unindent();
    print("fieldName:");
    indent();
    print(node.getFieldName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ArrayAccess
   * @param node the node to visit
   */
  public Void visit(ArrayAccess node) {
    print("l."+node.getBeginLine()+" ArrayAccess {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("cellNumber:");
    indent();
    node.getCellNumber().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public Void visit(SuperFieldAccess node) {
    print(indentation+"l."+node.getBeginLine()+" SuperFieldAccess {");
    print("fieldName:");
    indent();
    print(node.getFieldName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ObjectMethodCall
   * @param node the node to visit
   */
  public Void visit(ObjectMethodCall node) {
    print("l."+node.getBeginLine()+" ObjectMethodCall {");
    print("expression:");
    indent();
    if (node.getExpression() != null) {
      node.getExpression().acceptVisitor(this);
    } else {
      print("null");
    }
    unindent();
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  public Void visit(FunctionCall node) {
    print("l."+node.getBeginLine()+" FunctionCall {");
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public Void visit(StaticMethodCall node) {
    print("l."+node.getBeginLine()+" StaticMethodCall {");
    print("methodType:");
    indent();
    node.getMethodType().acceptVisitor(this);
    unindent();
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ConstructorInvocation
   * @param node the node to visit
   */
  public Void visit(ConstructorInvocation node) {
    print("l."+node.getBeginLine()+" ConstructorInvocation {");
    print("expression:");
    indent();
    if (node.getExpression() != null) {
      node.getExpression().acceptVisitor(this);
    }
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    print("isSuper:");
    indent();
    print((node.isSuper()) ? "true" : "false");
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public Void visit(SuperMethodCall node) {
    print("l."+node.getBeginLine()+" SuperMethodCall {");
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a PrimitiveTypeName
   * @param node the node to visit
   */
  public Void visit(PrimitiveTypeName node) {
    print("l."+node.getBeginLine()+" PrimitiveTypeName <"+node.getValue()+">");
    displayProperties(node);
    return null;
  }
  
  /**
   * Visits a ReferenceTypeName
   * @param node the node to visit
   */
  public Void visit(ReferenceTypeName node) {
    print("l."+node.getBeginLine()+" ReferenceTypeName {");
    print("representation:");
    indent();
    print(node.getRepresentation());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ArrayTypeName
   * @param node the node to visit
   */
  public Void visit(ArrayTypeName node) {
    print("l."+node.getBeginLine()+" ArrayTypeName {");
    if (node.getElementType() != null) {
      print("elementType:");
      node.getElementType().acceptVisitor(this);
    }
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  public Void visit(TypeExpression node) {
    print("l."+node.getBeginLine()+" TypeExpression {");
    print("type:");
    indent();
    node.getType().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public Void visit(PostIncrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public Void visit(PostDecrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public Void visit(PreIncrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public Void visit(PreDecrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  public Void visit(ArrayInitializer node) {
    print("l."+node.getBeginLine()+" ArrayInitializer {");
    print("cells:");
    indent();
    Iterator it = node.getCells().iterator();
    while (it.hasNext()) {
      ((Expression)it.next()).acceptVisitor(this);
    }
    unindent();
    print("elementType:");
    indent();
    node.getElementType().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public Void visit(ArrayAllocation node) {
    print("l."+node.getBeginLine()+" ArrayAllocation {");
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("dimension:");
    indent();
    print(""+node.getDimension());
    unindent();
    print("sizes:");
    indent();
    Iterator it = node.getSizes().iterator();
    while (it.hasNext()) {
      ((Expression)it.next()).acceptVisitor(this);
    }
    unindent();
    print("initialization:");
    indent();
    if (node.getInitialization() != null) {
      node.getInitialization().acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an SimpleAllocation
   * @param node the node to visit
   */
  public Void visit(SimpleAllocation node) {
    print("l."+node.getBeginLine()+" SimpleAllocation {");
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an ClassAllocation
   * @param node the node to visit
   */
  public Void visit(ClassAllocation node) {
    print("l."+node.getBeginLine()+" ClassAllocation {");
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    Iterator it = node.getMembers().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an InnerAllocation
   * @param node the node to visit
   */
  public Void visit(InnerAllocation node) {
    print("l."+node.getBeginLine()+" InnerAllocation {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an InnerClassAllocation
   * @param node the node to visit
   */
  public Void visit(InnerClassAllocation node) {
    print("l."+node.getBeginLine()+" InnerClassAllocation {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      Iterator it = node.getArguments().iterator();
      while (it.hasNext()) {
        ((Expression)it.next()).acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    Iterator it = node.getMembers().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public Void visit(CastExpression node) {
    print("l."+node.getBeginLine()+" CastExpression {");
    print("targetType:");
    indent();
    node.getTargetType().acceptVisitor(this);
    unindent();
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public Void visit(NotExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public Void visit(ComplementExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public Void visit(PlusExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public Void visit(MinusExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public Void visit(MultiplyExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Void visit(DivideExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Void visit(RemainderExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a AddExpression
   * @param node the node to visit
   */
  public Void visit(AddExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public Void visit(SubtractExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public Void visit(ShiftLeftExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public Void visit(ShiftRightExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public Void visit(UnsignedShiftRightExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public Void visit(LessExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public Void visit(GreaterExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public Void visit(LessOrEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public Void visit(GreaterOrEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  public Void visit(InstanceOfExpression node) {
    print("l."+node.getBeginLine()+" InstanceOfExpression {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("referenceType:");
    indent();
    node.getReferenceType().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a EqualExpression
   * @param node the node to visit
   */
  public Void visit(EqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  public Void visit(NotEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public Void visit(BitAndExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public Void visit(ExclusiveOrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public Void visit(BitOrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  public Void visit(AndExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  public Void visit(OrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public Void visit(ConditionalExpression node) {
    print("l."+node.getBeginLine()+" ConditionalExpression {");
    print("conditionExpression:");
    indent();
    node.getConditionExpression().acceptVisitor(this);
    unindent();
    print("ifTrueExpression:");
    indent();
    node.getIfTrueExpression().acceptVisitor(this);
    unindent();
    print("ifFalseExpression:");
    indent();
    node.getIfFalseExpression().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an SimpleAssignExpression
   * @param node the node to visit
   */
  public Void visit(SimpleAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public Void visit(MultiplyAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public Void visit(DivideAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  public Void visit(RemainderAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public Void visit(AddAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public Void visit(SubtractAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public Void visit(ShiftLeftAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an ShiftRightAssignExpression
   * @param node the node to visit
   */
  public Void visit(ShiftRightAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public Void visit(UnsignedShiftRightAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public Void visit(BitAndAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public Void visit(ExclusiveOrAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public Void visit(BitOrAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public Void visit(BlockStatement node) {
    print("l."+node.getBeginLine()+" BlockStatement {");
    print("statements:");
    indent();
    Iterator it = node.getStatements().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ClassDeclaration
   * @param node the node to visit
   */
  public Void visit(ClassDeclaration node) {
    print("l."+node.getBeginLine()+" ClassDeclaration {");
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("superclass:");
    indent();
    print(node.getSuperclass());
    unindent();
    print("interfaces:");
    indent();
    if (node.getInterfaces() != null) {
      Iterator it = node.getInterfaces().iterator();
      while (it.hasNext()) {
        print((String)it.next());
      }
    }
    unindent();
    print("members:");
    indent();
    Iterator it = node.getMembers().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an InterfaceDeclaration
   * @param node the node to visit
   */
  public Void visit(InterfaceDeclaration node) {
    print("l."+node.getBeginLine()+" InterfaceDeclaration {");
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("interfaces:");
    indent();
    if (node.getInterfaces() != null) {
      Iterator it = node.getInterfaces().iterator();
      while (it.hasNext()) {
        print((String)it.next());
      }
    }
    unindent();
    print("members:");
    indent();
    Iterator it = node.getMembers().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ConstructorDeclaration
   * @param node the node to visit
   */
  public Void visit(ConstructorDeclaration node) {
    print("l."+node.getBeginLine()+" ConstructorDeclaration {");
    print("accessFlags:");
    indent();
    print(""+node.getAccessFlags());
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("parameters:");
    indent();
    Iterator it = node.getParameters().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    print("exceptions:");
    indent();
    it = node.getExceptions().iterator();
    while (it.hasNext()) {
      print((String)it.next());
    }
    unindent();
    print("constructorInvocation:");
    indent();
    if (node.getConstructorInvocation() != null) {
      node.getConstructorInvocation().acceptVisitor(this);
    }
    unindent();
    print("statements:");
    indent();
    it = node.getStatements().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a MethodDeclaration
   * @param node the node to visit
   */
  public Void visit(MethodDeclaration node) {
    print("l."+node.getBeginLine()+" MethodDeclaration {");
    print("accessFlags:");
    indent();
    print(""+node.getAccessFlags());
    unindent();
    print("returnType:");
    indent();
    node.getReturnType().acceptVisitor(this);
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("parameters:");
    indent();
    Iterator it = node.getParameters().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    unindent();
    print("exceptions:");
    indent();
    it = node.getExceptions().iterator();
    while (it.hasNext()) {
      print((String)it.next());
    }
    unindent();
    print("body:");
    indent();
    if (node.getBody() != null) {
      node.getBody().acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a FormalParameter
   * @param node the node to visit
   */
  public Void visit(FormalParameter node) {
    print("l."+node.getBeginLine()+" FormalParameter {");
    if (node.isFinal()) {
      print("final");
    }
    print("type:");
    indent();
    node.getType().acceptVisitor(this);
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a FieldDeclaration
   * @param node the node to visit
   */
  public Void visit(FieldDeclaration node) {
    print("l."+node.getBeginLine()+" FieldDeclaration {");
    print("accessFlags:");
    indent();
    print(""+node.getAccessFlags());
    unindent();
    print("type:");
    indent();
    node.getType().acceptVisitor(this);
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("initializer:");
    indent();
    if (node.getInitializer() != null) {
      node.getInitializer().acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  public Void visit(VariableDeclaration node) {
    print("l."+node.getBeginLine()+" VariableDeclaration {");
    print("isFinal:");
    indent();
    print(""+node.isFinal());
    unindent();
    print("type:");
    indent();
    node.getType().acceptVisitor(this);
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("initializer:");
    indent();
    if (node.getInitializer() != null) {
      node.getInitializer().acceptVisitor(this);
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ClassInitializer
   * @param node the node to visit
   */
  public Void visit(ClassInitializer node) {
    print("l."+node.getBeginLine()+" ClassInitializer {");
    print("block:");
    indent();
    node.getBlock().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a InstanceInitializer
   * @param node the node to visit
   */
  public Void visit(InstanceInitializer node) {
    print("l."+node.getBeginLine()+" InstanceInitializer {");
    print("block:");
    indent();
    node.getBlock().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Displays an unary expression
   */
  private void displayUnary(UnaryExpression ue) {
    print("l."+ue.getBeginLine()+" "+ue.getClass().getName()+" {");
    print("expression:");
    indent();
    ue.getExpression().acceptVisitor(this);
    unindent();
    displayProperties(ue);
    print("}");
  }
  
  /**
   * Displays a binary expression
   */
  private void displayBinary(BinaryExpression be) {
    print("l."+be.getBeginLine()+" "+be.getClass().getName()+" {");
    print("leftExpression:");
    indent();
    be.getLeftExpression().acceptVisitor(this);
    unindent();
    print("rightExpression:");
    indent();
    be.getRightExpression().acceptVisitor(this);
    unindent();
    displayProperties(be);
    print("}"); 
  }
  
  /**
   * Displays the properties of a node
   */
  private void displayProperties(Node node) {
    Iterator it = node.getProperties().iterator();
    if (it.hasNext()) {
      print("properties:");
    }
    
    while (it.hasNext()) {
      indent();
      String prop = (String)it.next();
      print(prop+": "+node.getProperty(prop));
      unindent();
    }
  }
  
  /**
   * Adds a level of indentation
   */
  private void indent() {
    indentation += "  ";
  }
  
  /**
   * Removes a level of indentation
   */
  private void unindent() {
    indentation = indentation.substring(0, indentation.length()-2);
  }
  
  /**
   * Prints an indented line
   */
  private void print(String s) {
    out.println(indentation+s);
  }
}
