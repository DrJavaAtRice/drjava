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

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

/**
 * This tree visitor displays the nodes of the tree on
 * a given stream
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/04/24
 */

public class DisplayVisitor extends AbstractVisitor<Void> {
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
  @Override public Void visit(PackageDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" PackageDeclaration "+node.getName()+" {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   * @return null
   */
  @Override public Void visit(ImportDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" ImportDeclaration "+node.getName()
            +(node.isPackage() ? ".*" : "")+" {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an EmptyStatement
   * @param node the node to visit
   */
  @Override public Void visit(EmptyStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" EmptyStatement {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  @Override public Void visit(ExpressionStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" ExpressionStatement {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    print("hasSemicolon:" + node.getHasSemicolon());
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  @Override public Void visit(WhileStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" WhileStatement {");
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
  @Override public Void visit(ForStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" ForStatement {");
    print("initialization:");
    if (node.getInitialization() != null) {
      indent();
      for (Node n : node.getInitialization()) {
        n.acceptVisitor(this);
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
      for (Node n : node.getUpdate()) {
        n.acceptVisitor(this);
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
  @Override public Void visit(DoStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" DoStatement {");
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
  @Override public Void visit(SwitchStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" SwitchStatement {");
    print("selector:");
    indent();
    node.getSelector().acceptVisitor(this);
    unindent();
    print("bindings:");
    indent();
    for (Node n : node.getBindings()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(SwitchBlock node) {
    print("l."+node.getSourceInfo().getStartLine()+" SwitchBlock {");
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
      for (Node n : node.getStatements()) {
        n.acceptVisitor(this);
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
  @Override public Void visit(LabeledStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" LabeledStatement {");
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
  @Override public Void visit(BreakStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" BreakStatement {");
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
  @Override public Void visit(TryStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" TryStatement {");
    print("tryBlock:");
    indent();
    node.getTryBlock().acceptVisitor(this);
    unindent();
    print("catchStatements:");
    indent();
    for (Node n : node.getCatchStatements()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(CatchStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" CatchStatement {");
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
  @Override public Void visit(ThrowStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" ThrowStatement {");
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
  @Override public Void visit(ReturnStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" ReturnStatement {");
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
  @Override public Void visit(SynchronizedStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" SynchronizedStatement {");
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
  @Override public Void visit(ContinueStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" ContinueStatement {");
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
  @Override public Void visit(IfThenStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" IfThenStatement {");
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
  @Override public Void visit(IfThenElseStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" IfThenElseStatement {");
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
  @Override public Void visit(Literal node) {
    print("l."+node.getSourceInfo().getStartLine()+" Literal ("+
          node.getType()+") <"+node.getValue()+"> {");
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ThisExpression
   * @param node the node to visit
   */
  @Override public Void visit(ThisExpression node) {
    print("l."+node.getSourceInfo().getStartLine()+" ThisExpression {");
    print("className:");
    indent();
    print(node.getClassName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a AmbiguousName
   * @param node the node to visit
   */
  @Override public Void visit(AmbiguousName node) {
    print("l."+node.getSourceInfo().getStartLine()+" AmbiguousName {");
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
  @Override public Void visit(ObjectFieldAccess node) {
    print("l."+node.getSourceInfo().getStartLine()+" ObjectFieldAccess {");
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
  @Override public Void visit(StaticFieldAccess node) {
    print("l."+node.getSourceInfo().getStartLine()+" StaticFieldAccess {");
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
  @Override public Void visit(ArrayAccess node) {
    print("l."+node.getSourceInfo().getStartLine()+" ArrayAccess {");
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
   * Visits a SimpleFieldAccess
   * @param node the node to visit
   */
  @Override public Void visit(SimpleFieldAccess node) {
    print(indentation+"l."+node.getSourceInfo().getStartLine()+" SimpleFieldAccess {");
    print("fieldName:");
    indent();
    print(node.getFieldName());
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  @Override public Void visit(SuperFieldAccess node) {
    print(indentation+"l."+node.getSourceInfo().getStartLine()+" SuperFieldAccess {");
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
  @Override public Void visit(ObjectMethodCall node) {
    print("l."+node.getSourceInfo().getStartLine()+" ObjectMethodCall {");
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
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a SimpleMethodCall
   * @param node the node to visit
   */
  @Override public Void visit(SimpleMethodCall node) {
    print("l."+node.getSourceInfo().getStartLine()+" SimpleMethodCall {");
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
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
  @Override public Void visit(StaticMethodCall node) {
    print("l."+node.getSourceInfo().getStartLine()+" StaticMethodCall {");
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
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits a ConstructorCall
   * @param node the node to visit
   */
  @Override public Void visit(ConstructorCall node) {
    print("l."+node.getSourceInfo().getStartLine()+" ConstructorCall {");
    print("expression:");
    indent();
    if (node.getExpression() != null) {
      node.getExpression().acceptVisitor(this);
    }
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
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
  @Override public Void visit(SuperMethodCall node) {
    print("l."+node.getSourceInfo().getStartLine()+" SuperMethodCall {");
    print("methodName:");
    indent();
    print(node.getMethodName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
    /**
   * Visits a BooleanTypeName
   * @param node the node to visit
   */
  @Override public Void visit(BooleanTypeName node) {
    handlePrimitiveTypeName(node, "boolean");
    return null;
  }
  
  /**
   * Visits a ByteTypeName
   * @param node the node to visit
   */
  @Override public Void visit(ByteTypeName node) {
    handlePrimitiveTypeName(node, "byte");
    return null;
  }
  
  /**
   * Visits a ShortTypeName
   * @param node the node to visit
   */
  @Override public Void visit(ShortTypeName node) {
    handlePrimitiveTypeName(node, "short");
    return null;
  }
  
  /**
   * Visits a CharTypeName
   * @param node the node to visit
   */
  @Override public Void visit(CharTypeName node) {
    handlePrimitiveTypeName(node, "char");
    return null;
  }
  
  /**
   * Visits a IntTypeName
   * @param node the node to visit
   */
  @Override public Void visit(IntTypeName node) {
    handlePrimitiveTypeName(node, "int");
    return null;
  }
  
  /**
   * Visits a LongTypeName
   * @param node the node to visit
   */
  @Override public Void visit(LongTypeName node) {
    handlePrimitiveTypeName(node, "long");
    return null;
  }
  
  /**
   * Visits a FloatTypeName
   * @param node the node to visit
   */
  @Override public Void visit(FloatTypeName node) {
    handlePrimitiveTypeName(node, "float");
    return null;
  }
  
  /**
   * Visits a DoubleTypeName
   * @param node the node to visit
   */
  @Override public Void visit(DoubleTypeName node) {
    handlePrimitiveTypeName(node, "double");
    return null;
  }
  
  /**
   * Visits a VoidTypeName
   * @param node the node to visit
   */
  @Override public Void visit(VoidTypeName node) {
    handlePrimitiveTypeName(node, "void");
    return null;
  }

  private void handlePrimitiveTypeName(PrimitiveTypeName node, String name) {
    print("l."+node.getSourceInfo().getStartLine()+" PrimitiveTypeName <"+name+">");
    displayProperties(node);
  }
  
  /**
   * Visits a ReferenceTypeName
   * @param node the node to visit
   */
  @Override public Void visit(ReferenceTypeName node) {
    print("l."+node.getSourceInfo().getStartLine()+" ReferenceTypeName {");
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
  @Override public Void visit(ArrayTypeName node) {
    print("l."+node.getSourceInfo().getStartLine()+" ArrayTypeName {");
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
  @Override public Void visit(TypeExpression node) {
    print("l."+node.getSourceInfo().getStartLine()+" TypeExpression {");
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
  @Override public Void visit(PostIncrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  @Override public Void visit(PostDecrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  @Override public Void visit(PreIncrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  @Override public Void visit(PreDecrement node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  @Override public Void visit(ArrayInitializer node) {
    print("l."+node.getSourceInfo().getStartLine()+" ArrayInitializer {");
    print("cells:");
    indent();
    for (Node n : node.getCells()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(ArrayAllocation node) {
    print("l."+node.getSourceInfo().getStartLine()+" ArrayAllocation {");
    print("elementType:");
    indent();
    node.getElementType().acceptVisitor(this);
    unindent();
    print("dimension:");
    indent();
    print(""+node.getDimension());
    unindent();
    print("sizes:");
    indent();
    for (Node n : node.getSizes()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(SimpleAllocation node) {
    print("l."+node.getSourceInfo().getStartLine()+" SimpleAllocation {");
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an AnonymousAllocation
   * @param node the node to visit
   */
  @Override public Void visit(AnonymousAllocation node) {
    print("l."+node.getSourceInfo().getStartLine()+" AnonymousAllocation {");
    print("creationType:");
    indent();
    node.getCreationType().acceptVisitor(this);
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    for (Node n : node.getMembers()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(InnerAllocation node) {
    print("l."+node.getSourceInfo().getStartLine()+" InnerAllocation {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("className:");
    indent();
    print(node.getClassName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  /**
   * Visits an AnonymousInnerAllocation
   * @param node the node to visit
   */
  @Override public Void visit(AnonymousInnerAllocation node) {
    print("l."+node.getSourceInfo().getStartLine()+" AnonymousInnerAllocation {");
    print("expression:");
    indent();
    node.getExpression().acceptVisitor(this);
    unindent();
    print("creationType:");
    indent();
    print(node.getClassName());
    unindent();
    print("arguments:");
    indent();
    if (node.getArguments() != null) {
      for (Node n : node.getArguments()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    for (Node n : node.getMembers()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(CastExpression node) {
    print("l."+node.getSourceInfo().getStartLine()+" CastExpression {");
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
  @Override public Void visit(NotExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  @Override public Void visit(ComplementExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  @Override public Void visit(PlusExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  @Override public Void visit(MinusExpression node) {
    displayUnary(node);
    return null;
  }
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  @Override public Void visit(MultiplyExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  @Override public Void visit(DivideExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  @Override public Void visit(RemainderExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a AddExpression
   * @param node the node to visit
   */
  @Override public Void visit(AddExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  @Override public Void visit(SubtractExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  @Override public Void visit(ShiftLeftExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  @Override public Void visit(ShiftRightExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  @Override public Void visit(UnsignedShiftRightExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  @Override public Void visit(LessExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  @Override public Void visit(GreaterExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  @Override public Void visit(LessOrEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  @Override public Void visit(GreaterOrEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  @Override public Void visit(InstanceOfExpression node) {
    print("l."+node.getSourceInfo().getStartLine()+" InstanceOfExpression {");
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
  @Override public Void visit(EqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  @Override public Void visit(NotEqualExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  @Override public Void visit(BitAndExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  @Override public Void visit(ExclusiveOrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  @Override public Void visit(BitOrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  @Override public Void visit(AndExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  @Override public Void visit(OrExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  @Override public Void visit(ConditionalExpression node) {
    print("l."+node.getSourceInfo().getStartLine()+" ConditionalExpression {");
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
  @Override public Void visit(SimpleAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(MultiplyAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(DivideAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(RemainderAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(AddAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(SubtractAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an ShiftLeftAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(ShiftLeftAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an ShiftRightAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(ShiftRightAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits an UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(UnsignedShiftRightAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(BitAndAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(ExclusiveOrAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  @Override public Void visit(BitOrAssignExpression node) {
    displayBinary(node);
    return null;
  }
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  @Override public Void visit(BlockStatement node) {
    print("l."+node.getSourceInfo().getStartLine()+" BlockStatement {");
    print("statements:");
    indent();
    for (Node n : node.getStatements()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(ClassDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" ClassDeclaration {");
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("superclass:");
    indent();
    node.getSuperclass().acceptVisitor(this);
    unindent();
    print("interfaces:");
    indent();
    if (node.getInterfaces() != null) {
      for (Node n : node.getInterfaces()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    for (Node n : node.getMembers()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(InterfaceDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" InterfaceDeclaration {");
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("interfaces:");
    indent();
    if (node.getInterfaces() != null) {
      for (Node n : node.getInterfaces()) {
        n.acceptVisitor(this);
      }
    }
    unindent();
    print("members:");
    indent();
    for (Node n : node.getMembers()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(ConstructorDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" ConstructorDeclaration {");
    print("modifiers:");
    indent();
    node.getModifiers().acceptVisitor(this);
    unindent();
    print("name:");
    indent();
    print(node.getName());
    unindent();
    print("parameters:");
    indent();
    for (Node n : node.getParameters()) {
      n.acceptVisitor(this);
    }
    unindent();
    print("exceptions:");
    indent();
    for (Node n : node.getExceptions()) {
      n.acceptVisitor(this);
    }
    unindent();
    print("constructorInvocation:");
    indent();
    if (node.getConstructorCall() != null) {
      node.getConstructorCall().acceptVisitor(this);
    }
    unindent();
    print("statements:");
    indent();
    for (Node n : node.getStatements()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(MethodDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" MethodDeclaration {");
    print("modifiers:");
    indent();
    node.getModifiers().acceptVisitor(this);
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
    for (Node n : node.getParameters()) {
      n.acceptVisitor(this);
    }
    unindent();
    print("exceptions:");
    indent();
    for (Node n : node.getExceptions()) {
      n.acceptVisitor(this);
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
  @Override public Void visit(FormalParameter node) {
    print("l."+node.getSourceInfo().getStartLine()+" FormalParameter {");
    print("modifiers:");
    indent();
    node.getModifiers().acceptVisitor(this);
    unindent();
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
  @Override public Void visit(FieldDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" FieldDeclaration {");
    print("modifiers:");
    indent();
    node.getModifiers().acceptVisitor(this);
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
  @Override public Void visit(VariableDeclaration node) {
    print("l."+node.getSourceInfo().getStartLine()+" VariableDeclaration {");
    print("modifiers:");
    indent();
    node.getModifiers().acceptVisitor(this);
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
  @Override public Void visit(ClassInitializer node) {
    print("l."+node.getSourceInfo().getStartLine()+" ClassInitializer {");
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
  @Override public Void visit(InstanceInitializer node) {
    print("l."+node.getSourceInfo().getStartLine()+" InstanceInitializer {");
    print("block:");
    indent();
    node.getBlock().acceptVisitor(this);
    unindent();
    displayProperties(node);
    print("}");
    return null;
  }
  
  @Override public Void visit(ModifierSet mods) {
    for (Annotation ann : mods.getAnnotations()) {
      ann.acceptVisitor(this);
      print(" ");
    }
    for (ModifierSet.Modifier m : mods.getFlags()) {
      print(m.getName() + " ");
    }
    return null;
  }
  
  /**
   * Displays an unary expression
   */
  private void displayUnary(UnaryExpression ue) {
    print("l."+ue.getSourceInfo().getStartLine()+" "+ue.getClass().getName()+" {");
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
    print("l."+be.getSourceInfo().getStartLine()+" "+be.getClass().getName()+" {");
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
    boolean first = true;
    for (String prop : node.getProperties()) {
      if (first) { print("properties:"); first = false; }
      indent();
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
