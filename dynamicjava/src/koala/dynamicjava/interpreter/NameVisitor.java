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

package koala.dynamicjava.interpreter;

import java.util.*;
import java.lang.reflect.*;
import java.lang.reflect.Type;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This tree visitor resolves the ambiguity in compound identifiers
 * in a syntax tree; it returns either an Expression or a Reference Type
 *
 * @author Stephane Hillion
 * @version 1.1 - 1999/10/18
 */

public class NameVisitor extends VisitorObject<Node> {
  /**
   * The context
   */
  private Context<Type> context;
  
  /**
   * The type checker context
   */
  private Context<Type> typeCheckerContext;
  
  /**
   * a counter to help define unique variable names
   * added to help with foreach variable naming
   */
  private Integer name_counter;
  
  /**
   * Creates a new name visitor
   * @param ctx the context
   */
  public NameVisitor(Context<Type> ctx) {
    context = ctx;
    name_counter = new Integer(0);
  }
  
  /**
   * Creates a new name visitor with two context, one that is the default context and one which will be passed to a Type Checker
   * @param ctx the context
   * @param typeCtx the typeChecker Context
   */
  public NameVisitor(Context<Type> ctx, Context<Type> typeCtx) {
    this(ctx);
    typeCheckerContext = typeCtx;
  }
  
  protected static void rejectReferenceType(Node o, Node n) {
    if (o instanceof ReferenceType)
      throw new ExecutionError("Type name used where expression is expected", n);
  }
  
  /**
   * Sets the context's current package
   * @param node the node to visit
   * @return null
   */
  public Node visit(PackageDeclaration node) {
    context.setCurrentPackage(node.getName());
    return null;
  }
  
  /**
   * Declares the package or class importation in the context
   * @param node the node to visit
   */
  public Node visit(ImportDeclaration node) {
    if(node.isStatic()){
      TigerUtilities.assertTigerEnabled("Static Import is not supported before Java 1.5");
      try {  
        if(node.isStaticImportClass()) 
          context.declareClassStaticImport(node.getName());
        else 
          context.declareMemberStaticImport(node.getName());
      }
      catch (ClassNotFoundException e) {
        throw new CatchedExceptionError(e,node);
      }
    }
    else {      
      if (node.isPackage()) {
        context.declarePackageImport(node.getName());
      } else {
        try {
          context.declareClassImport(node.getName());
        } catch (ClassNotFoundException e) {
          throw new CatchedExceptionError(e, node);
        }
      }
    }
    return null;
  }
  
  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  public Node visit(WhileStatement node) {
    // Visits the components of this node
    Node n = node.getCondition();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setCondition((Expression)o);
    }
    n = node.getBody();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setBody(o);
    }
    return null;
  }
  
  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   * 
   * this simply takes in a ForEachStatement, and changes it to 
   * a ForStatement and runs the Name visitor on it
   */
  public Node visit(ForEachStatement node){
    String s1, s2;
    context.enterScope();
    
    
    name_counter = new Integer(name_counter.intValue() + 1);
    s1 = "#_foreach_var_" + name_counter;
    name_counter = new Integer(name_counter.intValue() + 1);
    s2 = "#_foreach_var_" + name_counter;
    context.define(s1, null);
    context.define(s2, null);
    
    node.addVar(s1);
    node.addVar(s2);
    
    
    
    
    FormalParameter param = node.getParameter();
    Expression coll = node.getCollection();
    Node body = node.getBody();
    Node o;
    
    o = param.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType (o,param);
      node.setParameter((FormalParameter)o);  
    }
    
    o = coll.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType (o,coll);
      node.setCollection((Expression)o);
    }
    
    o = body.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType (o,body);
      node.setBody(o);  
    }
    
    
    context.leaveScope();
    
    return null;
  }
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Node visit(ForStatement node) {
    // Enter a new scope
    context.enterScope();
    
    List<Node> init = node.getInitialization();
    // Check the statements
    if (init != null) visitList(init);
    
    Node n = node.getCondition();
    if (n != null) {
      Node o = n.acceptVisitor(this);
      if (o != null) {
        rejectReferenceType(o,n);
        node.setCondition((Expression)o);
      }
    }
    
    List<Node> updt = node.getUpdate();
    if (updt != null) visitList(updt);
    
    n = node.getBody();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType (o,n);
      node.setBody(o);  
    }
    // Leave the current scope
    context.leaveScope();
    return null;
  }
  
  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Node visit(DoStatement node) {
    // Visits the components of this node
    Node n = node.getCondition();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setCondition((Expression)o);
    }
    
    n = node.getBody();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setBody(o);  
    }
    return null;
  }
  
  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Node visit(SwitchStatement node) {
    // Visits the components of this node
    Expression exp = node.getSelector();
    Node o = exp.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,exp);
      node.setSelector((Expression) o);
    }
    
    Iterator<SwitchBlock> it = node.getBindings().iterator();
    while (it.hasNext()) {
      it.next().acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public Node visit(SwitchBlock node) {
    Expression exp = node.getExpression();
    if (exp != null) {
      visitExpressionContainer(node);
    }
    List<Node> l = node.getStatements();
    if (l != null) visitList(l);
    return null;
  }
  
  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Node visit(LabeledStatement node) {
    Node n = node.getStatement();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setStatement(o);
    }
    return null;
  }
  
  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Node visit(ThrowStatement node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Node visit(SynchronizedStatement node) {
    // Visits the component of this node
    Node n = node.getLock();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setLock((Expression)o);
    }
    
    n = node.getBody();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setBody(o);
    }
    return null;
  }
  
  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Node visit(TryStatement node) {
    node.getTryBlock().acceptVisitor(this);
    Iterator<CatchStatement> it = node.getCatchStatements().iterator();
    while (it.hasNext()) {
      it.next().acceptVisitor(this);
    }
    Node n = node.getFinallyBlock();
    if (n != null) {
      n.acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public Node visit(CatchStatement node) {
    // Enter a new scope
    context.enterScope();
    
    node.getException().acceptVisitor(this);
    node.getBlock().acceptVisitor(this);
    // Leave the current scope
    context.leaveScope();
    return null;
  }
  
  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Node visit(ReturnStatement node) {
    Expression e = node.getExpression();
    if (e != null) {
      visitExpressionContainer(node);
    }
    return null;
  }
  
  /**
   * Visits an IfThenStatement
   * @param node the node to visit
   */
  public Node visit(IfThenStatement node) {
    // Visits the components of this node
    Node n = node.getCondition();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setCondition((Expression)o);
    }
    
    n = node.getThenStatement();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setThenStatement(o);
    }
    return null;
  }
  
  /**
   * Visits an IfThenElseStatement
   * @param node the node to visit
   */
  public Node visit(IfThenElseStatement node) {
    // Visits the components of this node
    Node n = node.getCondition();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setCondition((Expression)o);
    }
    
    n = node.getThenStatement();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setThenStatement(o);
    }
    
    n = node.getElseStatement();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setElseStatement(o);
    }
    return null;
  }
 
  /**
   * Visits an AssertStatement
   * @param node the node to visit
   */
  public Node visit(AssertStatement node) {
    //Visits the components of this node
    Node n = node.getCondition();
    Node o = n.acceptVisitor(this);
    if(o != null) {
      rejectReferenceType(o,n);
      node.setCondition((Expression)o);
    }
    
    n = node.getFailString();
    if(n != null) {
      o = n.acceptVisitor(this);
      if(o != null) {
        node.setFailString((Expression)o);
      }
    }
    return null;    
  }
  
  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  public Node visit(VariableDeclaration node) {
    // Define the variable
    String s = node.getName();
    if (context.isDefinedVariable(s)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { s });
      throw new ExecutionError("variable.redefinition", node);
    }
    
    if (node.isFinal()) {
      context.defineConstant(s, null);
    } 
    else {
      context.define(s, null);
    }
    
    // Visit the initializer
    Node n = node.getInitializer();
    if (n != null) {
      Node o = n.acceptVisitor(this);
      if (o != null) {
        rejectReferenceType(o,n);
        node.setInitializer((Expression)o);
      }
    }
    return null;
  }
  
  /**
   * Visits a SimpleAssignExpression
   * @param node the node to visit
   */
  public Node visit(SimpleAssignExpression node) {
    // First, visit the right expression
    Expression right  = node.getRightExpression();
    Node o = right.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,right);
      node.setRightExpression((Expression)o);
    }
    
    // Perhaps is this assignment a variable declaration ?
    Expression left  = node.getLeftExpression();
    if (left instanceof QualifiedName) {
      List<IdentifierToken> ids = ((QualifiedName)left).getIdentifiers();
      String var = ids.get(0).image();
      if (ids.size() == 1 && !context.exists(var)) {
        context.define(var, null);
      }
    } 
    
    // Visit the left expression
    o = left.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,left);
      node.setLeftExpression((Expression)o);
    }
    return null;
  }
  
  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public Node visit(BlockStatement node) {
    // Enter a new scope
    context.enterScope();
    
    // Visit the nested statements
    visitList(node.getStatements());
    
    // Leave the current scope
    context.leaveScope();
    
    return null;
  }
  
  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  public Node visit(ObjectFieldAccess node) {
    // Visit the expression
    Node o = node.getExpression().acceptVisitor(this);
    if (o != null) {
      if (o instanceof ReferenceType) {
        return new StaticFieldAccess((ReferenceType)o,
                                     node.getFieldName(),
                                     node.getFilename(),
                                     node.getBeginLine(),
                                     node.getBeginColumn(),
                                     node.getEndLine(),
                                     node.getEndColumn());
      } 
      else {
        node.setExpression((Expression)o);
      }
    }
    return null;
  }
  
  /**
   * Visits an ObjectMethodCall
   * @param node the node to visit
   */
  public Node visit(ObjectMethodCall node) {
    // Check the arguments
    List<Expression> args = node.getArguments();
    if (args != null) {
      visitExprList(args);
    }
    Object defaultQualifier = context.getDefaultQualifier(node);
    // Check the expression
    Expression exp = node.getExpression();
    Object o;
    if (exp == null) {
      o = defaultQualifier;
    } else {
      o = exp.acceptVisitor(this);
      if (o == null) {
        return null;
      }
    }
    
    if(o == null || o == defaultQualifier) {
      try {
        AbstractTypeChecker tc = AbstractTypeChecker.makeTypeChecker(typeCheckerContext);
        //Get fully qualified name for Object o if the methodCall is to a staticly imported method
        //The full class name is given as if the user gave a call using the entire fully qualified name
        Class<?>[] params = new Class[args!=null? args.size() : 0];
        if(args != null) {
          for(int i = 0; i < args.size(); i++) {
            String toParse = args.get(i).toString();
            params[i]=(Class<?>)(args.get(i).acceptVisitor(tc));      // ADDED CAST HERE!!!!!!!
          } 
        }
        boolean existsInCurrentScope = false;
        
        if(o == defaultQualifier) {
          try {
            ReflectionUtilities.lookupMethod((Class)((Node)o).acceptVisitor(tc),node.getMethodName(),params);
            existsInCurrentScope = true;
          }
          catch(Exception nsme) {
            //Expected to throw an Exception whenever the method call is to a method that does not exist in 
            //the class specified by the default qualifier. If caught, the method does not exist in current scope and the 
            //new Qualified name should be looked up, or if o is not of type Node.
          } 
        }
          
        
        String representation = context.getQualifiedName(node.getMethodName(),params);
        if (! existsInCurrentScope)
          o = new ReferenceType(representation);
      }      
      catch(Exception e){
        //If the class type of one of the parameters can't be found, throws an exception
        //Also, if no method found to have been imported, throws an exception
        //This will occur every time the user calls a method that has not been staticly imported
        //As this section is new code, this is being caught to prevent breaking old code
      }     
    }
    
    if (o == null) {
      return new FunctionCall(node.getMethodName(),
                              node.getArguments(),
                              node.getFilename(),
                              node.getBeginLine(),
                              node.getBeginColumn(),
                              node.getEndLine(),
                              node.getEndColumn()
                                );
    } else if (o instanceof ReferenceType) {
      return new StaticMethodCall((ReferenceType)o,
                                  node.getMethodName(),
                                  node.getArguments(),
                                  node.getFilename(),
                                  node.getBeginLine(),
                                  node.getBeginColumn(),
                                  node.getEndLine(),
                                  node.getEndColumn()
                                    );
    } else {
      node.setExpression((Expression)o);
    }
    return null;
  }
  
  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public Node visit(SuperMethodCall node) {
    // Check the arguments
    List<Expression> args = node.getArguments();
    if (args != null) {
      visitExprList(args);
    }
    return null;
  }
  
  /**
   * Visits a ThisExpression
   * @param node the node to visit
   * @return a qualified name or a field access
   */
  public Node visit(ThisExpression node) {
    return (Expression) context.getDefaultQualifier(node, node.getClassName()); // Should type of getDefault... be narrowed? /**/
  }
  
  /**
   * Visits a QualifiedName
   * @param node the node to visit
   * @return a node that depends of the meaning of this name.
   *         It could be : a QualifiedName, a ReferenceType or a FieldAccess.
   */
  public Node visit(QualifiedName node) {
    List<IdentifierToken>  ids = node.getIdentifiers();
    IdentifierToken t = ids.get(0);
    
    if (context.isDefined(t.image())) {
      // The name starts with a reference to a local variable,
      // end of the name is a sequence of field access
      Expression result = context.createName(node, t);
      Iterator<IdentifierToken> it = ids.iterator();
      it.next();
      
      IdentifierToken t2;
      while (it.hasNext()) {
        t2 = it.next();
        result = new ObjectFieldAccess(result, t2.image(),
                                       node.getFilename(),
                                       t.beginLine(), t.beginColumn(),
                                       t2.endLine(), t2.endColumn());
      }
      return result;
    } 
    
    //Added to support static field importation
    try{
      if(context.isFieldImported(t.image())) 
        ids = context.getQualifiedName(t.image());        
    }
    catch(NoSuchFieldException e) {}
    
     // The name must be, or starts with, a class name
    List<IdentifierToken> l = ListUtilities.listCopy(ids);
    
    
    boolean b = false;
    
    while (l.size() > 0) {
      String s = TreeUtilities.listToName(l);
      if (b = context.classExists(s)) {
        break;
      }
      l.remove(l.size()-1);
    }
    
    if (!b) {
      // It is an error if no matching class or field was found
      node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { t.image() });
      throw new ExecutionError("undefined.class", node);
    }
    
    // Creates a ReferenceType node
    IdentifierToken t2 = l.get(l.size()-1);
    ReferenceType rt = new ReferenceType(l,
                                         node.getFilename(),
                                         t.beginLine(), t.beginColumn(),
                                         t2.endLine(),  t2.endColumn());
    
    if (l.size() != ids.size()) {
      // The end of the name is a sequence of field access
      ListIterator<IdentifierToken> it = ids.listIterator(l.size());
      t2 = it.next();
      Expression result =
        new StaticFieldAccess(rt, t2.image(), node.getFilename(),
                              t.beginLine(), t.beginColumn(),
                              t2.endLine(), t2.endColumn());
      while (it.hasNext()) {
        t2 = it.next();
        result = new ObjectFieldAccess(result, t2.image(),
                                       node.getFilename(),
                                       t.beginLine(), t.beginColumn(),
                                       t2.endLine(), t2.endColumn());
      }
      return result;
    } 
    else {
      return rt;
    }
  }
  
  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  public Node visit(SimpleAllocation node) {
    // Visit the arguments
    List<Expression> args = node.getArguments();
    
    if (args != null) {
      visitExprList(args);
    }
    return null;
  }
  
  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public Node visit(ArrayAllocation node) {
    // Do the checking of the size expressions
    visitExprList(node.getSizes());
    
    // Visits the initializer if one
    if (node.getInitialization() != null) {
      node.getInitialization().acceptVisitor(this);
    }
    return null;
  }
  
  /**
   * Visits a InnerAllocation
   * @param node the node to visit
   */
  public Node visit(InnerAllocation node) {
    visitExpressionContainer(node);
    
    // Do the type checking of the arguments
    List<Expression> args = node.getArguments();
    if (args != null) {
      visitExprList(args);
    }
    return null;
  }
  
  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  public Node visit(ArrayInitializer node) {
    visitExprList(node.getCells());
    return null;
  }
  
  /**
   * Visits an ArrayAccess
   * @param node the node to visit
   */
  public Node visit(ArrayAccess node) {
    visitExpressionContainer(node);
    
    // Visits the cell number expression
    Node   n = node.getCellNumber();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setCellNumber((Expression)o);
    }
    return null;
  }
  
  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public Node visit(NotExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public Node visit(ComplementExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public Node visit(PlusExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public Node visit(MinusExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits an AddExpression
   * @param node the node to visit
   */
  public Node visit(AddExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public Node visit(AddAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public Node visit(SubtractExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public Node visit(SubtractAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public Node visit(MultiplyExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public Node visit(MultiplyAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Node visit(DivideExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public Node visit(DivideAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Node visit(RemainderExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a RemainderAssignExpression
   * @param node the node to visit
   */
  public Node visit(RemainderAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an EqualExpression
   * @param node the node to visit
   */
  public Node visit(EqualExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  public Node visit(NotEqualExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public Node visit(LessExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public Node visit(LessOrEqualExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public Node visit(GreaterExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public Node visit(GreaterOrEqualExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public Node visit(BitAndExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public Node visit(BitAndAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public Node visit(ExclusiveOrExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public Node visit(ExclusiveOrAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public Node visit(BitOrExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public Node visit(BitOrAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public Node visit(ShiftLeftExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public Node visit(ShiftLeftAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public Node visit(ShiftRightExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a ShiftRightAssignExpression
   * @param node the node to visit
   */
  public Node visit(ShiftRightAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public Node visit(UnsignedShiftRightExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public Node visit(UnsignedShiftRightAssignExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  public Node visit(AndExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  public Node visit(OrExpression node) {
    visitBinaryExpression(node);
    return null;
  }
  
  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  public Node visit(InstanceOfExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public Node visit(ConditionalExpression node) {
    // Check each subexpression
    Node   n = node.getConditionExpression();
    Node o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setConditionExpression((Expression)o);
    }
    
    n = node.getIfTrueExpression();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setIfTrueExpression((Expression)o);
    }
    
    n = node.getIfFalseExpression();
    o = n.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,n);
      node.setIfFalseExpression((Expression)o);
    }
    return null;
  }
  
  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public Node visit(PostIncrement node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public Node visit(PreIncrement node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public Node visit(PostDecrement node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public Node visit(PreDecrement node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public Node visit(CastExpression node) {
    visitExpressionContainer(node);
    return null;
  }
  
  /**
   * Visits a ClassAllocation
   * @param node the node to visit
   */
  public Node visit(ClassAllocation node) {
    List<Expression> largs = node.getArguments();
    if (largs != null) {
      visitExprList(largs);
    }
    return null;
  }
  
  /**
   * Visits an InnerClassAllocation
   * @param node the node to visit
   */
  public Node visit(InnerClassAllocation node) {
    visitExpressionContainer(node);
    List<Expression> largs = node.getArguments();
    if (largs != null) {
      visitExprList(largs);
    }
    return null;
  }
  
  /**
   * Visits a FormalParameter
   * @param node the node to visit
   * @return the name of the parameter class
   */
  public Node visit(FormalParameter node) {
    if (node.isFinal()) {
      context.defineConstant(node.getName(), null);
    } else {
      context.define(node.getName(), null);
    }
    return null;
  }
  
  /**
   * Visits a ClassDeclaration
   * @param node the node to visit
   */
  public Node visit(ClassDeclaration node) {
    context.defineClass(node);
    return null;
  }
  
  /**
   * Visits an InterfaceDeclaration
   * @param node the node to visit
   */
  public Node visit(InterfaceDeclaration node) {
    context.defineClass(node);
    return null;
  }
  
  /**
   * Visits an expression container
   */
  private void visitExpressionContainer(ExpressionContainer node) {
    Expression exp = node.getExpression();
    Node o = exp.acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,exp);
      node.setExpression((Expression)o);
    }
  }
  
  /**
   * Visits the subexpressions of a BinaryExpression
   */
  private void visitBinaryExpression(BinaryExpression node) {
    // Visit the left expression
    Node o = node.getLeftExpression().acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,node);
      node.setLeftExpression((Expression)o);
    }
    
    // Visit the right expression
    o = node.getRightExpression().acceptVisitor(this);
    if (o != null) {
      rejectReferenceType(o,node);
      node.setRightExpression((Expression)o);
    }
  }
  
  /**
   * Visits a list of node
   */
  private void visitList(List<Node> l) {
    Node n;
    ListIterator<Node> it = l.listIterator();
    while (it.hasNext()) {
      Node o = (n = it.next()).acceptVisitor(this);
      if (o != null) {
        rejectReferenceType(o,n);
        it.set(o);  
      }
    }
  }
  
  /**
   * Visits a list of Expression
   */
  private void visitExprList(List<Expression> l) {
    Node n;
    ListIterator<Expression> it = l.listIterator();
    while (it.hasNext()) {
      Object o = (n = it.next()).acceptVisitor(this);
      if (o != null) {
        if (o instanceof ReferenceType) {
          throw new ExecutionError("malformed.expression", n);
        }
        it.set((Expression)o);  // This cast is a guess based on documentation  /**/
      }
    }
  }
}
