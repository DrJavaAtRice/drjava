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

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This tree visitor checks the typing rules and loads
 * the classes, fields and methods
 *
 * @author Stephane Hillion
 * @version 1.2 - 1999/11/20
 */

public class TypeChecker extends VisitorObject<Class> {
  /**
   * The context
   */
  private Context context;

  /**
   * Creates a new name visitor
   * @param ctx the context
   */
  public TypeChecker(Context ctx) {
    context = ctx;
  }

  /**
   * Visits a PackageDeclaration
   * @param node the node to visit
   * @return null
   */
  public Class visit(PackageDeclaration node) {
    context.setCurrentPackage(node.getName());
    return null;
  }

  /**
   * Visits an ImportDeclaration
   * @param node the node to visit
   */
  public Class visit(ImportDeclaration node) {
    // Declare the package or class importation
    if (node.isPackage()) {
      context.declarePackageImport(node.getName());
    } else {
      try {
        context.declareClassImport(node.getName());
      } catch (ClassNotFoundException e) {
        throw new CatchedExceptionError(e, node);
      }
    }
    return null;
  }

  /**
   * Visits a WhileStatement first by checking the 
   * condition type and unboxing if necessary then by 
   * recurring to its body.
   * @param node the node to visit
   */
  public Class visit(WhileStatement whileStmt) {
    // Check the condition
    Expression exp = whileStmt.getCondition();

    Class type = exp.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", whileStmt);
    }

    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "exp.booleanValue();"
      whileStmt.setCondition(_unbox(exp, type));
    }

    whileStmt.getBody().acceptVisitor(this);
    return null;
  }

  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   */
  public Class visit(ForEachStatement node){
    /* to be filled in shortly */

  
        /*examples*/
    /*
     * Collection<String> c = ... ;
     * for(String s: c){
     *   ...
     * }
     * translates to:
     * for(Iterator<E> #i = Expression.iterator(); #i.hasNext(); ){
     *    FormalParameter = #i.next();
     *    statement...
     * }
     * 
     * 
     * create a variable name #i
     * get the generic type of the collection and create an iterator with the
     *   same generic type and give it the name #i
     * create an expressio for #i.iterator();
     * create an AssignmentExpression for the iterator and the expression.iterator
     * create an expression for #i.hasNext();
     * create an expression for #i.next();
     * create an assignemntExpression for FormalParameter = #i.next();
     * create a new for body expression and add the assigment to it's first
     * create a new for statement.
     * 
     * ============================================================================
     * 
     * Collection c = ... ;
     * for(Object o: c){
     *   String s = (String) o;
     *   ...
     * }
     * translates to:
     * for(Iterator #i = Expression.iterator(); #i.hasNext(); ){
     *    FormalParameter = #i.next();
     *    statement...
     * }
     * 
     * int sum(int[] a){
     *    int sum = 0;
     *    for(int i:a){
     *      sum+=i;
     *    return sum
     * }
     * translates to:
     * for(int #i=0; #i<a.length; #i++){
     *   FormalParameter=a[#i];
     *   statement...
     * }
     */
return null;
  }

  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Class visit(ForStatement node) {
    // Enter a new scope
    context.enterScope();

    List<Node> l;
    // Check the statements
    if ((l = node.getInitialization()) != null) {
      checkList(l);
    }

    Expression cond = node.getCondition();
    if (cond != null) {

      Class type = cond.acceptVisitor(this);
      if (type != boolean.class && type != Boolean.class) {
        throw new ExecutionError("condition.type", node);
      }

      // Auto unbox: Boolean->boolean
      if (type == Boolean.class) {
        // add method call on expression:
        //   "cond.booleanValue();"
        node.setCondition(_unbox(cond, type));
      }
    }

    if ((l = node.getUpdate()) != null) {
      checkList(l);
    }

    node.getBody().acceptVisitor(this);

    // Leave the current scope and store the defined variables
    // (a map of String-Class mappings) in the "variables" property
    node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
    return null;
  }

  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Class visit(DoStatement node) {
    node.getBody().acceptVisitor(this);

    Expression exp = node.getCondition();
    Class type = exp.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }

    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "exp.booleanValue();"
      node.setCondition(_unbox(exp, type));
    }
    return null;
  }

  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Class visit(SwitchStatement node) {
    // Visits the components of this node
    Expression exp = node.getSelector();
    Class c = exp.acceptVisitor(this);
    if (c != char.class      && c != byte.class && c != short.class && c != int.class  && 
        c != Character.class && c != Byte.class && c != Short.class && c != Integer.class) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("selector.type", node);
    }
    // unbox it if needed
    if (c == Character.class || c == Byte.class || c == Short.class || c == Integer.class) {
      node.setSelector(_unbox(exp, c));
    }
    
    // Check the type of the case labels
    Iterator it = node.getBindings().iterator();
    while (it.hasNext()) {
      SwitchBlock sb = (SwitchBlock)it.next();
      sb.acceptVisitor(this);
      exp = sb.getExpression();
      if (exp != null) {
        Class lc = NodeProperties.getType(exp);
        if (lc != char.class &&  lc != byte.class &&
            lc != short.class && lc != int.class) {
          node.setProperty(NodeProperties.ERROR_STRINGS,
                           new String[] { lc.getName() });
          throw new ExecutionError("switch.label.type", node);
        }
        if (c != lc) {
          Number n = null;
          if (exp.hasProperty(NodeProperties.VALUE)) {
            Object cst = exp.getProperty(NodeProperties.VALUE);
            if (lc == char.class) {
              n = new Integer(((Character)cst).charValue());
            } else {
              n = (Number)cst;
            }
          }
          if (c == byte.class) {
            if (exp.hasProperty(NodeProperties.VALUE)) {
              if (n.byteValue() != n.intValue()) {
                node.setProperty(NodeProperties.ERROR_STRINGS,
                                 new String[] { c.getName() });
                throw new ExecutionError
                  ("switch.label.type", node);
              }
            } else {
              throw new ExecutionError("switch.label.type", node);
            }
          } else if (c == short.class || c == char.class) {
            if (exp.hasProperty(NodeProperties.VALUE)) {
              if (n.shortValue() != n.intValue()) {
                node.setProperty(NodeProperties.ERROR_STRINGS,
                                 new String[] { c.getName() });
                throw new ExecutionError
                  ("switch.label.type", node);
              }
            } else if (lc == int.class) {
              node.setProperty(NodeProperties.ERROR_STRINGS,
                               new String[] { c.getName() });
              throw new ExecutionError("switch.label.type", node);
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Visits a SwitchBlock
   * @param node the node to visit
   */
  public Class visit(SwitchBlock node) {
    Expression exp = node.getExpression();
    if (exp != null) {
      exp.acceptVisitor(this);
    }
    List<Node> l;
    if ((l = node.getStatements()) != null) {
      checkList(l);
    }
    return null;
  }

  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Class visit(LabeledStatement node) {
    node.getStatement().acceptVisitor(this);
    return null;
  }

  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Class visit(TryStatement node) {
    node.getTryBlock().acceptVisitor(this);
    Iterator it = node.getCatchStatements().iterator();
    while (it.hasNext()) {
      ((Node)it.next()).acceptVisitor(this);
    }
    Node n;
    if ((n = node.getFinallyBlock()) != null) {
      n.acceptVisitor(this);
    }
    return null;
  }

  /**
   * Visits a CatchStatement
   * @param node the node to visit
   */
  public Class visit(CatchStatement node) {
    // Enter a new scope
    context.enterScope();

    Class c = node.getException().acceptVisitor(this);
    if (!Throwable.class.isAssignableFrom(c)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("catch.type", node);
    }

    node.getBlock().acceptVisitor(this);

    // Leave the current scope
    context.leaveScope();
    node.setProperty(NodeProperties.TYPE, c);
    return null;
  }

  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Class visit(ThrowStatement node) {
    Class c = node.getExpression().acceptVisitor(this);
    if (!Throwable.class.isAssignableFrom(c)) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("throw.type", node);
    }
    return null;
  }

  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Class visit(ReturnStatement node) {
    Expression e = node.getExpression();
    if (e != null) {
      e.acceptVisitor(this);
    }
    return null;
  }

  /**
   * Visits an IfThenStatement
   * @param node the node to visit
   */
  public Class visit(IfThenStatement node) {
    Expression cond = node.getCondition();
    
    // Check the condition
    Class type = cond.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "cond.booleanValue();"
      node.setCondition(_unbox(cond, type));
    }
    
    node.getThenStatement().acceptVisitor(this);
    return null;
  }

  /**
   * Visits an IfThenElseStatement
   * @param node the node to visit
   */
  public Class visit(IfThenElseStatement node) {
    Expression cond = node.getCondition();
    
    // Check the condition
    Class type = cond.acceptVisitor(this);
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }
    
    // Auto unbox: Boolean->boolean
    if (type == Boolean.class) {
      // add method call on expression:
      //   "cond.booleanValue();"
      node.setCondition(_unbox(cond, type));
    }

    node.getThenStatement().acceptVisitor(this);
    node.getElseStatement().acceptVisitor(this);
    return null;
  }

  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Class visit(SynchronizedStatement node) {
    // Check the lock
    if ((node.getLock().acceptVisitor(this)).isPrimitive()) {
      throw new ExecutionError("lock.type", node);
    }

    node.getBody().acceptVisitor(this);
    return null;
  }

  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public Class visit(Literal node) {
    // Set the properties of the node
    Class c = node.getType();
    node.setProperty(NodeProperties.VALUE, node.getValue());
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  public Class visit(VariableDeclaration node) {
    // Define the variable
    Class lc = node.getType().acceptVisitor(this);
    if (node.isFinal()) {
      context.defineConstant(node.getName(), lc);
    } 
    else {
      context.define(node.getName(), lc);
    }

    // Get the type of the initializer
    Expression init = node.getInitializer();
    if (init != null) {
      Class rc = init.acceptVisitor(this);
      Expression exp = checkAssignmentStaticRules(lc, rc, node, init);
      node.setInitializer(exp);
    }
    
    return null;
  }

  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public Class visit(BlockStatement node) {
    // Enter a new scope
    context.enterScope();

    // Do the type checking of the nested statements
    checkList(node.getStatements());

    // Leave the current scope and store the defined variables
    // (a map of String-Class mappings) in the "variables" property
    node.setProperty(NodeProperties.VARIABLES, context.leaveScope());

    return null;
  }

  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  public Class visit(ObjectFieldAccess node) {
    // Visit the expression
    Class c = node.getExpression().acceptVisitor(this);

    // Load the field object
    if (!c.isArray()) {
      Field f = null;
      try {
        f = context.getField(c, node.getFieldName());
      } catch (Exception e) {
        throw new CatchedExceptionError(e, node);
      }

      // Set the node properties
      node.setProperty(NodeProperties.FIELD, f);
      node.setProperty(NodeProperties.TYPE,  c = f.getType());

      node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
      return c;
    } else {
      if (!node.getFieldName().equals("length")) {
        String s0 = "length";
        String s1 = c.getComponentType().getName() + " array";
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
        throw new ExecutionError("no.such.field", node);
      }
      node.setProperty(NodeProperties.TYPE,  int.class);
      node.setProperty(NodeProperties.MODIFIER, new InvalidModifier(node));
      return int.class;
    }
  }

  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public Class visit(SuperFieldAccess node) {
    Field f = null;
    try {
      f = context.getSuperField(node, node.getFieldName());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }

    // Set the node properties
    node.setProperty(NodeProperties.FIELD, f);
    Class c;
    node.setProperty(NodeProperties.TYPE,  c = f.getType());
    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
  }

  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public Class visit(StaticFieldAccess node) {
    // Visit the field type
    Class c = node.getFieldType().acceptVisitor(this);

    // Load the field object
    Field f = null;
    try {
      f = context.getField(c, node.getFieldName());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }

    // Set the node properties
    node.setProperty(NodeProperties.FIELD, f);
    node.setProperty(NodeProperties.TYPE,  c = f.getType());

    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
  }

  /**
   * Visits an ObjectMethodCall
   * @param node the node to visit
   */
  public Class visit(ObjectMethodCall node) {
    // Check the receiver
    Expression exp = node.getExpression();
    Class      c   = exp.acceptVisitor(this);
    String     mn  = node.getMethodName();

    if (!c.isArray() || (c.isArray() && !mn.equals("clone"))) {
      // Do the type checking of the arguments
      List<Expression> args = node.getArguments();
      Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
      if (args != null) {
        cargs = new Class[args.size()];
        Iterator<Expression> it = args.iterator();
        int i  = 0;
        while (it.hasNext()) {
          cargs[i++] = it.next().acceptVisitor(this);
        }
      }
      Method m = null;
      try {
        m = context.lookupMethod(exp, mn, cargs);
      } 
      catch (NoSuchMethodException e) {
        String s = c.getName();
        String sargs = "";
        for (int i = 0; i < cargs.length-1; i++) {
          sargs += cargs[i].getName() + ", ";
        }
        if (cargs.length > 0) {
          sargs += cargs[cargs.length-1].getName();
        }
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { mn, s, sargs });
        throw new ExecutionError("no.such.method.with.args", node);
      } 
      catch (MethodModificationError e) {
        Expression expr = e.getExpression();
        expr.acceptVisitor(this);
        node.setExpression(expr);
        m = e.getMethod();
      }

      // Set the node properties
      node.setProperty(NodeProperties.METHOD, m);
      node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
      return c;
    } else {
      if (!mn.equals("clone") || node.getArguments() != null) {
        String s0 = "clone";
        String s1 = c.getComponentType().getName() + " array";
        node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1 });
        throw new ExecutionError("no.such.method", node);
      }
      node.setProperty(NodeProperties.TYPE, c = Object.class);
      return c;
    }
  }

  /**
   * Visits a MethodDeclaration
   * @param node the node to visit
   */
  public Class visit(MethodDeclaration node) {
    context.defineFunction(node);

    node.setProperty(NodeProperties.TYPE, node.getReturnType().acceptVisitor(this));
    node.setProperty(NodeProperties.FUNCTIONS, context.getFunctions());
    node.setProperty(NodeProperties.IMPORTATION_MANAGER,
                     context.getImportationManager().clone());

    context.enterScope();
    checkList(node.getParameters());
    context.leaveScope();
    return null;
  }

  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  public Class visit(FunctionCall node) {
    // Do the type checking of the arguments
    List args = node.getArguments();
    Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
    if (args != null) {
      cargs = new Class[args.size()];
      Iterator it = args.iterator();
      int i  = 0;
      while (it.hasNext()) {
        cargs[i++] = ((Node)it.next()).acceptVisitor(this);
      }
    }
    MethodDeclaration f = null;
    try {
      f = context.lookupFunction(node.getMethodName(), cargs);
    } catch (NoSuchFunctionException e) {
      String s = node.getMethodName();
      node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s });
      throw new ExecutionError("no.such.function", node);
    }

    // Set the node properties
    Class c;
    node.setProperty(NodeProperties.FUNCTION, f);
    node.setProperty(NodeProperties.TYPE,  c = NodeProperties.getType(f));
    return c;
  }

  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public Class visit(SuperMethodCall node) {
    // Do the type checking of the arguments
    List args = node.getArguments();
    Class[] pt = Constants.EMPTY_CLASS_ARRAY;
    if (args != null) {
      pt = new Class[args.size()];
      Iterator it = args.iterator();
      int i = 0;
      while (it.hasNext()) {
        pt[i++] = ((Node)it.next()).acceptVisitor(this);
      }
    }
    Method m = null;
    try {
      m = context.lookupSuperMethod(node, node.getMethodName(), pt);
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }

    // Set the node properties
    Class c;
    node.setProperty(NodeProperties.METHOD, m);
    node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
    return c;
  }

  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public Class visit(StaticMethodCall node) {
    // Do the type checking of the arguments
    List args = node.getArguments();
    Class[] cargs = Constants.EMPTY_CLASS_ARRAY;
    if (args != null) {
      cargs = new Class[args.size()];
      Iterator it = args.iterator();
      int      i  = 0;
      while (it.hasNext()) {
        cargs[i++] = ((Node)it.next()).acceptVisitor(this);
      }
    }
    Method m = null;
    Node   n = node.getMethodType();
    Class  c = n.acceptVisitor(this);

    try {
      m = context.lookupMethod(n, node.getMethodName(), cargs);
    } catch (NoSuchMethodException e) {
      String s0 = node.getMethodName();
      String s1 = c.getName();
      String sargs = "";
      for (int i = 0; i < cargs.length-1; i++) {
        sargs += cargs[i].getName() + ", ";
      }
      if (cargs.length > 0) {
        sargs += cargs[cargs.length-1].getName();
      }
      node.setProperty(NodeProperties.ERROR_STRINGS, new String[] { s0, s1, sargs });
      throw new ExecutionError("no.such.method.with.args", node);
    }

    // Set the node properties
    node.setProperty(NodeProperties.METHOD, m);
    node.setProperty(NodeProperties.TYPE,   c = m.getReturnType());
    return c;
  }

  /**
   * Visits a SimpleAssignExpression
   * @param node the node to visit
   */
  public Class visit(SimpleAssignExpression node) {
    Expression left  = node.getLeftExpression();
    Expression right = node.getRightExpression();
    Class rc = right.acceptVisitor(this);

    // Perhaps is this assignment a variable declaration
    if (left instanceof QualifiedName) {
      String var = ((QualifiedName)left).getRepresentation();
      if (!context.exists(var)) {
        context.define(var, (rc == null) ? Object.class : rc);
      }
    }

    // Get the type of the left hand side
    Class lc = left.acceptVisitor(this);

    // The left subexpression must be a variable
    if (!left.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Check the validity of the assignment
    Expression exp = checkAssignmentStaticRules(lc, rc, node, right);
    node.setRightExpression(exp);

    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a QualifiedName
   * @param node the node to visit
   */
  public Class visit(QualifiedName node) {
    String var = node.getRepresentation();

    // Set the modifier
    Class c = (Class) context.get(var);
    node.setProperty(NodeProperties.TYPE, c);

    node.setProperty(NodeProperties.MODIFIER, context.getModifier(node));
    return c;
  }

  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  public Class visit(SimpleAllocation node) {
    // Check the type to declare
    Node type = node.getCreationType();
    Class c  = type.acceptVisitor(this);

    // Do the type checking of the arguments
    List args = node.getArguments();
    Class[] cargs = Constants.EMPTY_CLASS_ARRAY;

    if (args != null) {
      cargs = new Class[args.size()];

      ListIterator it = args.listIterator();
      int i  = 0;
      while (it.hasNext()) {
        cargs[i++] = ((Node)it.next()).acceptVisitor(this);
      }
    }

    return context.setProperties(node, c, cargs);
  }

  /**
   * Visits a InnerAllocation
   * @param node the node to visit
   */
  public Class visit(InnerAllocation node) {
    // Visit the expression
    Class ec = node.getExpression().acceptVisitor(this);

    // Check the type to declare
    Node type = node.getCreationType();
    if (type instanceof ReferenceType) {
      ReferenceType rt = (ReferenceType)type;
      rt.setRepresentation(ec.getName() + "$" + rt.getRepresentation());
    } else {
      throw new ExecutionError("allocation.type", node);
    }
    Class c = type.acceptVisitor(this);
    Class dc = InterpreterUtilities.getDeclaringClass(c);

    // Do the type checking of the arguments
    List args = node.getArguments();
    Class[] cargs = null;

    if (dc != null && dc.isAssignableFrom(ec)) {
      // Adds an argument if the class to build is an innerclass
      if (args != null) {
        cargs = new Class[args.size() + 1];

        cargs[0] = ec;
        ListIterator it = args.listIterator();
        int i  = 1;
        while (it.hasNext()) {
          cargs[i++] = ((Node)it.next()).acceptVisitor(this);
        }
      } else {
        cargs = new Class[] { ec };
      }
    } else {
      throw new ExecutionError("allocation.type", node);
    }
    Constructor cons = null;
    try {
      cons = context.lookupConstructor(c, cargs);
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }

    // Set the properties of this node
    node.setProperty(NodeProperties.TYPE,        c);
    node.setProperty(NodeProperties.CONSTRUCTOR, cons);

    return c;
  }

  /**
   * Visits a ClassAllocation
   * @param node the node to visit
   */
  public Class visit(ClassAllocation node) {
    // If the class allocation is the initializer of a field,
    // it is possible that it has already been visited
    if (node.hasProperty(NodeProperties.TYPE)) {
      return NodeProperties.getType(node);
    } else {
      // Get the class to allocate
      Node   ctn   = node.getCreationType();
      Class   ct   = ctn.acceptVisitor(this);
      List   largs = node.getArguments();
      Class[] args = Constants.EMPTY_CLASS_ARRAY;

      if (largs != null) {
        args = new Class[largs.size()];
        Iterator it = largs.iterator();
        int i = 0;
        while (it.hasNext()) {
          args[i++] = ((Node)it.next()).acceptVisitor(this);
        }
      }
      return context.setProperties(node, ct, args, node.getMembers());
    }
  }

  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public Class visit(ArrayAllocation node) {
    // Do the checking of the size expressions
    Iterator it = node.getSizes().iterator();
    while (it.hasNext()) {
      Class c = ((Node)it.next()).acceptVisitor(this);
      // Dimension expression must be of an integral type, but not long
      if (c != byte.class && c != short.class && c != int.class) {
        throw new ExecutionError("array.dimension.type", node);
      }
    }

    Class c = node.getCreationType().acceptVisitor(this);

    // Visits the initializer if one
    if (node.getInitialization() != null) {
      node.getInitialization().acceptVisitor(this);
    }

    // Set the type properties of this node
    Class  ac = Array.newInstance(c, new int[node.getDimension()]).getClass();

    node.setProperty(NodeProperties.TYPE, ac);
    node.setProperty(NodeProperties.COMPONENT_TYPE, c);
    return ac;
  }

  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  public Class visit(ArrayInitializer node) {
    node.getElementType().acceptVisitor(this);

    checkList(node.getCells());
    return null;
  }

  /**
   * Visits an ArrayAccess
   * @param node the node to visit
   */
  public Class visit(ArrayAccess node) {
    // Visits the expression on which this array access applies
    Class c = node.getExpression().acceptVisitor(this);

    if (!c.isArray()) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { c.getName() });
      throw new ExecutionError("array.required", node);
    }

    // Sets the properties of this node
    Class result;
    node.setProperty(NodeProperties.TYPE, result = c.getComponentType());
    node.setProperty(NodeProperties.MODIFIER, new ArrayModifier(node));

    // Visits the cell number expression
    c = node.getCellNumber().acceptVisitor(this);

    // The index must be of an integral type, but not a long
    if (c != char.class && c != byte.class && c != short.class && c != int.class) {
      throw new ExecutionError("array.index.type", node);
    }
    return result;
  }

  /**
   * Visits a PrimitiveType
   * @param node the node to visit
   */
  public Class visit(PrimitiveType node) {
    Class c = node.getValue();
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a ReferenceType
   * @param node the node to visit
   */
  public Class visit(ReferenceType node) {
    Class c = null;
    try {
      c = context.lookupClass(node.getRepresentation());
    } catch (ClassNotFoundException e) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { node.getRepresentation() });
      throw new ExecutionError("undefined.class", node);
    }
    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a ArrayType
   * @param node the node to visit
   */
  public Class visit(ArrayType node) {
    Node eType = node.getElementType();
    Class c = eType.acceptVisitor(this);
    Class ac = Array.newInstance(c, 0).getClass();

    // Set the type property of this node
    node.setProperty(NodeProperties.TYPE, ac);
    return ac;
  }

  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  public Class visit(TypeExpression node) {
    Class c = node.getType().acceptVisitor(this);
    node.setProperty(NodeProperties.TYPE, Class.class);
    node.setProperty(NodeProperties.VALUE, c);
    return Class.class;
  }

  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public Class visit(NotExpression node) {
    // Check the type
    Expression exp = node.getExpression();
    Class c = exp.acceptVisitor(this);

    if (c != boolean.class && c != Boolean.class) {
      throw new ExecutionError("not.expression.type", node);
    }
    node.setProperty(NodeProperties.TYPE, boolean.class);

    // Auto-unbox, if necessary
    if (_isBoxingType(c)) {
      node.setExpression(_unbox(exp, c));
    }

    // Compute the expression if it is constant
    if (exp.hasProperty(NodeProperties.VALUE)) {
      if (((Boolean) exp.getProperty(NodeProperties.VALUE)).booleanValue()) {
        node.setProperty(NodeProperties.VALUE, Boolean.FALSE);
      } 
      else {
        node.setProperty(NodeProperties.VALUE, Boolean.TRUE);
      }
    }
    return boolean.class;
  }

  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public Class visit(ComplementExpression node) {
    // Check the type
    Expression e = node.getExpression();
    Class c = e.acceptVisitor(this);
    Class returnType = c;

    if (c == char.class      || c == byte.class || c == short.class ||
        c == Character.class || c == Byte.class || c == Short.class) {
      node.setProperty(NodeProperties.TYPE, int.class);
    } 
    else if (c == int.class     || c == long.class ||
             c == Integer.class || c == Long.class) {
      if (c == Integer.class) {
        returnType = int.class;
      }
      else if (c == Long.class) {
        returnType = long.class;
      }
      node.setProperty(NodeProperties.TYPE, returnType);
    } 
    else {
      throw new ExecutionError("complement.expression.type", node);
    }

    // Auto-unbox, if necessary
    if (_isBoxingType(c)) {
      node.setExpression(_unbox(e, c));
    }
    
    // Compute the expression if it is constant
    if (e.hasProperty(NodeProperties.VALUE)) {
      Object o = e.getProperty(NodeProperties.VALUE);
      
      if (o instanceof Character) {
        o = new Integer(((Character)o).charValue());
      }
      if (c == int.class) {
        o = new Integer(~((Number)o).intValue());
      } 
      else {
        o = new Long(~((Number)o).longValue());
      }
      node.setProperty(NodeProperties.VALUE, o);
    }
    return returnType;
  }

  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public Class visit(PlusExpression node) {
    Class c = visitUnaryOperation(node, "plus.expression.type");

    // Compute the expression if it is constant
    Node  n = node.getExpression();
    if (n.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.plus(c, n.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public Class visit(MinusExpression node) {
    Class c = visitUnaryOperation(node, "minus.expression.type");

    // Compute the expression if it is constant
    Node  n = node.getExpression();
    if (n.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.minus(c, n.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an AddExpression
   * @param node the node to visit
   */
  public Class visit(AddExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = rn.acceptVisitor(this);
    Class c  = String.class;

    if (lc != String.class && rc != String.class) {
      c = visitNumericExpression(node, "addition.type");
    } 
    else {
      node.setProperty(NodeProperties.TYPE, c);
    }

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) && 
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.add(c, ln.getProperty(NodeProperties.VALUE),
                                                rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public Class visit(AddAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc != String.class) {
      if (lc == null || rc == null ||
          lc == void.class || rc == void.class ||
          rc == boolean.class || !rc.isPrimitive()) {
        throw new ExecutionError("addition.type", node);
      }
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public Class visit(SubtractExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "subtraction.type");

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.subtract(c,
                                       ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public Class visit(SubtractAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        !lc.isPrimitive()   || !rc.isPrimitive()) {
      throw new ExecutionError("subtraction.type", node);
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public Class visit(MultiplyExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "multiplication.type");

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.multiply(c,
                                       ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public Class visit(MultiplyAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        !lc.isPrimitive()   || !rc.isPrimitive()) {
      throw new ExecutionError("multiplication.type", node);
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Class visit(DivideExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "division.type");

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.divide(c,
                                     ln.getProperty(NodeProperties.VALUE),
                                     rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public Class visit(DivideAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        !lc.isPrimitive()   || !rc.isPrimitive()) {
      throw new ExecutionError("division.type", node);
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Class visit(RemainderExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "remainder.type");

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.remainder(c,
                                        ln.getProperty(NodeProperties.VALUE),
                                        rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  public Class visit(RemainderAssignExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc == null          || rc == null          ||
        lc == void.class    || rc == void.class    ||
        lc == boolean.class || rc == boolean.class ||
        !lc.isPrimitive()   || !rc.isPrimitive()) {
      throw new ExecutionError("remainder.type", node);
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits an EqualExpression
   * @param node the node to visit
   */
  public Class visit(EqualExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = rn.acceptVisitor(this);

    // Check the equality rules, and un-box
    checkEqualityStaticRules(lc, rc, node);

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.equalTo(lc, rc,
                                                    ln.getProperty(NodeProperties.VALUE),
                                                    rn.getProperty(NodeProperties.VALUE)));
    }

    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    
    // Return the type of the expression (always boolean)
    return boolean.class;
  }

  /**
   * Visits an NotEqualExpression
   * @param node the node to visit
   */
  public Class visit(NotEqualExpression node) {
    // Check the types
    Node ln = node.getLeftExpression();
    Node rn = node.getRightExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = rn.acceptVisitor(this);

    // Check the equality rules, and un-box
    checkEqualityStaticRules(lc, rc, node);

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.notEqualTo(lc, rc,
                                                       ln.getProperty(NodeProperties.VALUE),
                                                       rn.getProperty(NodeProperties.VALUE)));
    }

    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    
    // Return boolean type
    return boolean.class;
  }

  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public Class visit(LessExpression node) {
    Class c = visitRelationalExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.lessThan(ln.getProperty(NodeProperties.VALUE),
                                       rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public Class visit(LessOrEqualExpression node) {
    Class c = visitRelationalExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.lessOrEqual(ln.getProperty(NodeProperties.VALUE),
                                          rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public Class visit(GreaterExpression node) {
    Class c = visitRelationalExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.greaterThan(ln.getProperty(NodeProperties.VALUE),
                                          rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public Class visit(GreaterOrEqualExpression node) {
    Class c = visitRelationalExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.greaterOrEqual(ln.getProperty(NodeProperties.VALUE),
                                             rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public Class visit(BitAndExpression node) {
    Class c = visitBitwiseExpression(node);

    Node ln = node.getLeftExpression();
    Node rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty(NodeProperties.VALUE,
                       InterpreterUtilities.bitAnd(c,
                                                   ln.getProperty(NodeProperties.VALUE),
                                                   rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public Class visit(BitAndAssignExpression node) {
    return visitBitwiseAssign(node);
  }

  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public Class visit(BitOrExpression node) {
    Class c = visitBitwiseExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.bitOr
           (c,
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public Class visit(BitOrAssignExpression node) {
    return visitBitwiseAssign(node);
  }

  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public Class visit(ExclusiveOrExpression node) {
    Class c = visitBitwiseExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.xOr
           (c,
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public Class visit(ExclusiveOrAssignExpression node) {
    return visitBitwiseAssign(node);
  }

  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public Class visit(ShiftLeftExpression node) {
    Class c = visitShiftExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.shiftLeft
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public Class visit(ShiftLeftAssignExpression node) {
    Class c = visitShiftExpression(node);

    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("shift.left.type", node);
    }

    return c;
  }

  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public Class visit(ShiftRightExpression node) {
    Class c = visitShiftExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.shiftRight
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a ShiftRightAssignExpression
   * @param node the node to visit
   */
  public Class visit(ShiftRightAssignExpression node) {
    Class c = visitShiftExpression(node);

    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("shift.right.type", node);
    }

    return c;
  }

  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public Class visit(UnsignedShiftRightExpression node) {
    Class c = visitShiftExpression(node);

    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         InterpreterUtilities.unsignedShiftRight
           (NodeProperties.getType(node),
            ln.getProperty(NodeProperties.VALUE),
            rn.getProperty(NodeProperties.VALUE)));
    }
    return c;
  }

  /**
   * Visits a UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public Class visit(UnsignedShiftRightAssignExpression node) {
    Class c = visitShiftExpression(node);

    // The left subexpression must be a variable
    if (!node.getLeftExpression().hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("unsigned.shift.right.type", node);
    }

    return c;
  }

  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  public Class visit(AndExpression node) {
    Expression le = node.getLeftExpression();
    Expression re = node.getRightExpression();
    Class lc = le.acceptVisitor(this);
    Class rc = re.acceptVisitor(this);

    // Check the types of the operands
    if (!(lc == boolean.class || lc == Boolean.class) || 
        !(rc == boolean.class || rc == Boolean.class) ) {
      throw new ExecutionError("and.type", node);
    }

    // Auto-unbox, if necessary
    if (lc == Boolean.class) {
      node.setLeftExpression(_unbox(le, lc));
    }
    if (rc == Boolean.class) {
      node.setRightExpression(_unbox(re, rc));
    }
    
    // Compute the expression if it is constant
    if (le.hasProperty(NodeProperties.VALUE) &&
        re.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         (((Boolean)le.getProperty(NodeProperties.VALUE)).booleanValue() &&
          ((Boolean)re.getProperty(NodeProperties.VALUE)).booleanValue())
           ? Boolean.TRUE : Boolean.FALSE);
    }

    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }

  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  public Class visit(OrExpression node) {
    Expression ln = node.getLeftExpression();
    Expression rn = node.getRightExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = rn.acceptVisitor(this);

    // Check the types of the operands
    if (!(lc == boolean.class || lc == Boolean.class) || 
        !(rc == boolean.class || rc == Boolean.class) ) {
      throw new ExecutionError("or.type", node);
    }

    // Auto-unbox, if necessary
    if (lc == Boolean.class) {
      node.setLeftExpression(_unbox(ln, lc));
    }
    if (rc == Boolean.class) {
      node.setRightExpression(_unbox(rn, rc));
    }

    // Compute the expression if it is constant
    if (ln.hasProperty(NodeProperties.VALUE) &&
        rn.hasProperty(NodeProperties.VALUE)) {
      node.setProperty
        (NodeProperties.VALUE,
         (((Boolean)ln.getProperty(NodeProperties.VALUE)).booleanValue() ||
          ((Boolean)rn.getProperty(NodeProperties.VALUE)).booleanValue())
           ? Boolean.TRUE : Boolean.FALSE);
    }

    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }

  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  public Class visit(InstanceOfExpression node) {
    node.getReferenceType().acceptVisitor(this);

    // The expression must not have a primitive type
    if ((node.getExpression().acceptVisitor(this)).isPrimitive()) {
      throw new ExecutionError("left.expression", node);
    }

    // Set the type property
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }

  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public Class visit(ConditionalExpression node) {
    // Get the type of the conidition expression
    Expression condExp = node.getConditionExpression();
    Class type = condExp.acceptVisitor(this);
    
    // Check the condition
    if (type != boolean.class && type != Boolean.class) {
      throw new ExecutionError("condition.type", node);
    }

    // Auto-unbox, if necessary
    if (type == Boolean.class) {
      node.setConditionExpression(_unbox(condExp, type));
    }

    // Determine the type of the expression
    Node n1 = node.getIfTrueExpression();
    Node n2 = node.getIfFalseExpression();
    Class c1 = n1.acceptVisitor(this);
    Class c2 = n2.acceptVisitor(this);
    Class ec = null;

    // See if the expression is typable
    if (c1 == c2) {
      ec = c1;
    } 
    else if (c1 == null) {
      ec = c2;
    }
    else if (c2 == null) {
      ec = c1;
    }
    else if (!c1.isPrimitive() && !c2.isPrimitive()) {
      if (c1.isAssignableFrom(c2)) {
        ec = c1;
      }
      else if (c2.isAssignableFrom(c1)) {
        ec = c2;
      }
      else {
        throw new ExecutionError("incompatible.types", node);
      }
    } 
    else if (c1 == boolean.class || c2 == boolean.class ||
             c1 == void.class    || c2 == void.class) {
      throw new ExecutionError("incompatible.types", node);
    }
    else if ((c1 == short.class && c2 == byte.class) ||
             (c1 == byte.class  && c2 == short.class)) {
      ec = short.class;
    }
    else if ((c2 == byte.class || c2 == short.class || c2 == char.class) &&
             n1.hasProperty(NodeProperties.VALUE) && c1 == int.class) {
      Number n = (Number) n1.getProperty(NodeProperties.VALUE);
      if (c2 == byte.class) {
        if (n.intValue() == n.byteValue()) {
          ec = byte.class;
        }
        else {
          ec = int.class;
        }
      } 
      else if (n.intValue() == n.shortValue()) {
        ec = (c2 == char.class) ? char.class : short.class;
      }
      else {
        ec = int.class;
      }
    } 
    else if ((c1 == byte.class || c1 == short.class || c1 == char.class) &&
               n2.hasProperty(NodeProperties.VALUE) && c2 == int.class) {
      Number n = (Number)n2.getProperty(NodeProperties.VALUE);
      if (c1 == byte.class) {
        if (n.intValue() == n.byteValue()) {
          ec = byte.class;
        } 
        else {
          ec = int.class;
        }
      } 
      else if (n.intValue() == n.shortValue()) {
        ec = (c1 == char.class) ? char.class : short.class;
      }
      else {
        ec = int.class;
      }
    } 
    else if (c1 == double.class || c2 == double.class) {
      ec = double.class;
    }
    else if (c1 == float.class || c2 == float.class) {
      ec = float.class;
    } 
    else if (c1 == long.class || c2 == long.class) {
      ec = long.class;
    }
    else {
      ec = int.class;
    }
    node.setProperty(NodeProperties.TYPE, ec);

    return ec;
  }

  /**
   * Visits a FormalParameter
   * @param node the node to visit
   * @return the class of the parameter
   */
  public Class visit(FormalParameter node) {
    Class c = node.getType().acceptVisitor(this);

    if (node.isFinal()) {
      context.defineConstant(node.getName(), c);
    } else {
      context.define(node.getName(), c);
    }
    node.setProperty(NodeProperties.TYPE, c);

    return c;
  }

  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public Class visit(PostIncrement node) {
    Node exp = node.getExpression();
    Class c  = exp.acceptVisitor(this);

    // The type of the subexpression must be numeric
    if (!c.isPrimitive()   ||
        c == void.class    ||
        c == boolean.class) {
      throw new ExecutionError("post.increment.type", node);
    }

    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("post.increment.type", node);
    }

    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public Class visit(PreIncrement node) {
    Node exp = node.getExpression();
    Class c  = exp.acceptVisitor(this);

    // The type of the subexpression must be numeric
    if (!c.isPrimitive()   ||
        c == void.class    ||
        c == boolean.class) {
      throw new ExecutionError("pre.increment.type", node);
    }

    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("pre.increment.type", node);
    }

    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public Class visit(PostDecrement node) {
    Node exp = node.getExpression();
    Class c  = exp.acceptVisitor(this);

    // The type of the subexpression must be numeric
    if (!c.isPrimitive()   ||
        c == void.class    ||
        c == boolean.class) {
      throw new ExecutionError("post.decrement.type", node);
    }

    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("post.decrement.type", node);
    }

    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public Class visit(PreDecrement node) {
    Node exp = node.getExpression();
    Class c  = exp.acceptVisitor(this);

    // The type of the subexpression must be numeric
    if (!c.isPrimitive()   ||
        c == void.class    ||
        c == boolean.class) {
      throw new ExecutionError("pre.decrement.type", node);
    }

    // The subexpression must be a variable
    if (!exp.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("pre.decrement.type", node);
    }

    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public Class visit(CastExpression node) {
    Class c = node.getTargetType().acceptVisitor(this);
    checkCastStaticRules(c, node.getExpression().acceptVisitor(this), node);

    node.setProperty(NodeProperties.TYPE, c);
    return c;
  }

  /**
   * Visits an unary operation.
   */
  private Class visitUnaryOperation(UnaryExpression node, String s) {
    Expression exp = node.getExpression();
    Class c = exp.acceptVisitor(this);
    Class returnType = c;

    if (c == char.class      || c == byte.class || c == short.class || c == int.class ||
        c == Character.class || c == Byte.class || c == Short.class || c == Integer.class) {
      node.setProperty(NodeProperties.TYPE, int.class);
    }
    else if (c == long.class || c == float.class || c == double.class ||
             c == Long.class || c == Float.class || c == Double.class) {
      if (c == Long.class) {
        returnType = long.class;
      }
      else if (c == Float.class) {
        returnType = float.class;
      }
      else if (c == Double.class) {
        returnType = double.class;
      }
      node.setProperty(NodeProperties.TYPE, returnType);
    }
    else {
      throw new ExecutionError(s, node);
    }
    
    // Auto-unbox, if necessary
    if (_isBoxingType(c)) {
      node.setExpression(_unbox(exp, c));
    }
    
    return returnType;
  }

  /**
   * Visits a numeric expression.
   * This method checks the types on any numeric binary operator
   * performing any needed unboxing or numeric promotion on either
   * of the operands.
   * @param node the Binary expression to type check
   * @param s The key to use when throwing an ExecutionError
   * @return the type of the expression after any numeric promotion or 
   * unboxing has been performed
   */
  protected static Class visitNumericExpression(BinaryExpression node, String s) {
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    
    // Set the type property of the given node
    Class lc = NodeProperties.getType(leftExp);
    Class rc = NodeProperties.getType(rightExp);
    Class c  = null;

    // Check to make sure the left and right types are valid
    if (lc == null           || rc == null          ||
        lc == boolean.class  || rc == boolean.class ||
        !(lc.isPrimitive()   || _isBoxingType(lc))  || 
        !(rc.isPrimitive()   || _isBoxingType(rc))  ||
        lc == void.class     || rc == void.class) {
      throw new ExecutionError(s, node);
    } 

    // Auto-unbox, if necessary
    if (_isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (_isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    // Set the type of the node to be the binary promotion
    if (lc == double.class || lc == Double.class || 
        rc == double.class || rc == Double.class) {
      c = double.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else if (lc == float.class || lc == Float.class ||
             rc == float.class || rc == Float.class) {
      c = float.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else if (lc == long.class || lc == Long.class ||
             rc == long.class || rc == Long.class) {
      c = long.class;
      node.setProperty(NodeProperties.TYPE, c);
    } 
    else {
      c = int.class;
      node.setProperty(NodeProperties.TYPE, c);
    }
    
    // Return the type of the node
    return c;
  }
  
  
  /**
   * Checks the typing rules for an assignment
   * !! This method has not yet been modified for boxing/unboxing !!
   * @param lc   the class of the left part of an assignment
   * @param rc   the class of the right part of an assignment
   * @param node the entire assignment node
   * @param v    The right-hand side of the expression
   * @return The right-hand side of the assignment.  This expression will
   * be the unboxing/boxing of the RHS if necessary.
   */
  private static Expression checkAssignmentStaticRules(Class lc, Class rc,
                                                 Node node, Expression v) {
    if (lc != null) {
      if (lc.isPrimitive()) {
        if (lc == boolean.class && rc != boolean.class) {
          if (rc == Boolean.class) {
            return _unbox(v,Boolean.class);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == byte.class && rc != byte.class) {
          if (rc == Byte.class) {
            return _unbox(v,Byte.class);
          }
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.byteValue()) {
              return v;
            }
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if ((lc == short.class || lc == char.class) &&
                 (rc != byte.class && rc != short.class && rc != char.class)) {
          if (lc == short.class && rc == Short.class) {
            return _unbox(v, Short.class);
          }
          if (lc == char.class && rc == Character.class) {
            return _unbox(v, Character.class);
          }
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.shortValue()) {
              return v;
            }
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == int.class    &&
                 (rc != byte.class  &&
                  rc != short.class &&
                  rc != char.class  &&
                  rc != int.class)) {
          if (rc == Byte.class      || rc == Short.class ||
              rc == Character.class || rc == Integer.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == long.class   &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == float.class   ||
                  rc == double.class)) {
          if (_isBoxingType(rc) && _isIntegralType(rc)) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == float.class  &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == double.class)) {
          if (_isBoxingType(rc) && rc != Boolean.class && rc != Double.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
        else if (lc == double.class &&
                 (rc == null        ||
                  !rc.isPrimitive() ||
                  rc == void.class  ||
                  rc == boolean.class)) {
          if (_isBoxingType(rc) && rc != Boolean.class) {
            return _unbox(v, rc);
          }
          throw new ExecutionError("assignment.types", node);
        }
      }
      else if (rc != null) {
        if (_boxesTo(rc, lc)) { 
          return _box(v, rc);
        }
        if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) {
          throw new ExecutionError("assignment.types", node);
        }
      }
    }
    return v;
  }

  /**
   * Checks the typing rules in an equality operation
   * @param lc the class of the left operand
   * @param rc the class of the right operand
   * @param s  the error message
   * @param n  the current node
   */
  private static void checkEqualityStaticRules(Class lc, Class rc, BinaryExpression n) {
    Expression leftExp = n.getLeftExpression();
    Expression rightExp = n.getRightExpression();

    // Auto-unbox, if necessary
    /**
     * We have not decided what the correct semantics
     * for the == operator is for boxed/primitive types
     */
    if (lc != null && rc != null) {
      if (_isBoxingType(lc) && rc.isPrimitive()) {
        ObjectMethodCall methodCall = _unbox(leftExp, lc);
        n.setLeftExpression(methodCall);
        lc = (Class) methodCall.getProperty(NodeProperties.TYPE);
      }
      if (_isBoxingType(rc) && lc.isPrimitive()) {
        ObjectMethodCall methodCall = _unbox(rightExp, rc);
        n.setRightExpression(methodCall);
        rc = (Class) methodCall.getProperty(NodeProperties.TYPE);
      }
    }

    if (lc != rc || lc == void.class) {
      if (lc == void.class    || rc == void.class ||
          lc == boolean.class || rc == boolean.class) {
        throw new ExecutionError("compare.type", n);
      } 
      else if ((lc == null && rc.isPrimitive()) ||
               (rc == null && lc.isPrimitive())) {
        throw new ExecutionError("compare.type", n);
      } 
      else if (lc != null && rc != null) {
        if (lc.isPrimitive() ^ rc.isPrimitive()) {
          throw new ExecutionError("compare.type", n);
        }
        else if (!lc.isPrimitive() && !rc.isPrimitive()) {  
          if (!lc.isAssignableFrom(rc) && !rc.isAssignableFrom(lc)) {
            throw new ExecutionError("compare.type", n);
          }
        }
      }
    }
  }

  /**
   * Visits a relational expression.  This simply makes sure
   * the types are numerical primitives/boxing types, 
   * performs the unboxing if necessary, and sets the type
   * of the overall expression to the boolean primitive type
   * @param node the relational expression: (> < >= <=)
   * @return the type of the expression: boolean
   */
  private Class visitRelationalExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Class lc = leftExp.acceptVisitor(this);
    Class rc = rightExp.acceptVisitor(this);

    if (lc == null          || rc == null           ||
        lc == void.class    || rc == void.class     ||
        lc == boolean.class || rc == boolean.class  ||
        !(lc.isPrimitive()  || _isBoxingType(lc))   || 
        !(rc.isPrimitive()  || _isBoxingType(rc))) {
      throw new ExecutionError("relational.expression.type", node);
    }

    // Auto-unbox, if necessary
    if (_isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (_isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    // The type of the expression is always boolean
    node.setProperty(NodeProperties.TYPE, boolean.class);
    return boolean.class;
  }

  /**
   * Visits a bitwise expression.
   * If either the left or right expression is an integral
   * boxing type, this method performs the unboxing procedure
   * and uses the primitive type of the ObjectMethodCall to 
   * perform the any numeric promotion necessary.
   * @param node the bitwise expression to be type checked.
   * @return the type of the expression after unboxing and 
   *         promotion has been executed
   */
  private Class visitBitwiseExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Class lc = leftExp.acceptVisitor(this);
    Class rc = rightExp.acceptVisitor(this);
    Class c = null;
    
    boolean intLeft   = _isIntegralType(lc);
    boolean intRight  = _isIntegralType(rc);
    boolean boolLeft  = (lc == boolean.class  || lc == Boolean.class);
    boolean boolRight = (rc == boolean.class  || rc == Boolean.class);
    
//    if (lc == null           || rc == null           ||
//        lc == void.class     || rc == void.class     ||
//        lc == float.class    || rc == float.class    ||
//        lc == Float.class    || rc == Float.class    ||
//        lc == double.class   || rc == double.class   ||
//        lc == Double.class   || rc == Double.class   ||
//        (lc == boolean.class ^ rc == boolean.class)  ||
//        !lc.isPrimitive()    || !rc.isPrimitive()) {
    if (!(  intLeft &&  intRight ) && 
        !( boolLeft && boolRight ) ){
      throw new ExecutionError("bitwise.expression.type", node);
    } 
    
    
    // Auto-unbox, if necessary
    if (_isBoxingType(lc)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (_isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    if (lc == long.class || rc == long.class ||
        lc == Long.class || rc == Long.class) {
      node.setProperty(NodeProperties.TYPE, c = long.class);
    }
    else if ( boolLeft ) {
      node.setProperty(NodeProperties.TYPE, c = boolean.class);
    }
    else {
      node.setProperty(NodeProperties.TYPE, c = int.class);
    }
    return c;
  }

  /**
   * Checks a bitwise expression
   * !!! This method has not been modified for boxing/unboxing !!!
   */
  private Class visitBitwiseAssign(BinaryExpression node) {
    // Check the types
    Node  ln = node.getLeftExpression();
    Class lc = ln.acceptVisitor(this);
    Class rc = node.getRightExpression().acceptVisitor(this);

    if (lc == null           || rc == null           ||
        lc == void.class     || rc == void.class     ||
        lc == float.class    || rc == float.class    ||
        lc == double.class   || rc == double.class   ||
        (lc == boolean.class ^ rc == boolean.class)  ||
        !lc.isPrimitive()    || !rc.isPrimitive()) {
      throw new ExecutionError("bitwise.expression.type", node);
    }

    // The left subexpression must be a variable
    if (!ln.hasProperty(NodeProperties.MODIFIER)) {
      throw new ExecutionError("left.expression", node);
    }

    // Sets the type property of this node
    node.setProperty(NodeProperties.TYPE, lc);
    return lc;
  }

  /**
   * Visits a shift expression.
   * This method is responsible for unboxing the two operands
   * as necessary, performing any numeric promotions needed,
   * and checking that the types of the two operands are compatible
   * with the shift operations.
   * @param node the shifting expression to be type checked
   * @return the type of the expression after any unboxing or numeric
   * promotion has been successfully executed.
   */
  private Class visitShiftExpression(BinaryExpression node) {
    // Check the types
    Expression leftExp = node.getLeftExpression();
    Expression rightExp = node.getRightExpression();
    Class lc = leftExp.acceptVisitor(this);
    Class rc = rightExp.acceptVisitor(this);
    Class c  = null;

    if (lc == null          || rc == null          ||
        lc == boolean.class || rc == boolean.class ||
        lc == void.class    || rc == void.class    ||
        lc == float.class   || rc == float.class   ||
        lc == Float.class   || rc == Float.class   ||
        lc == double.class  || rc == double.class  ||
        lc == Double.class  || rc == Double.class  ||
        !(lc.isPrimitive()  || _isBoxingType(lc))  || 
        !(rc.isPrimitive()  || _isBoxingType(rc)) ) {
      throw new RuntimeException("lc: " + lc + ", rc: " + rc);
//      throw new ExecutionError("shift.expression.type", node);
    } 
    
    // Auto-unbox, if necessary
    if (_isBoxingType(lc) && !leftExp.hasProperty(NodeProperties.MODIFIER)) {
      node.setLeftExpression(_unbox(leftExp, lc));
    }
    if (_isBoxingType(rc)) {
      node.setRightExpression(_unbox(rightExp, rc));
    }
    
    if (lc == long.class || lc == Long.class) {
      node.setProperty(NodeProperties.TYPE, c = long.class);
    } 
    else {
      node.setProperty(NodeProperties.TYPE, c = int.class);
    }
    return c;
  }

  /**
   * Checks the typing rules in a cast expression
   * !! This method has not yet been modified for boxing/unboxing !!
   * @param tc the target class
   * @param ec the expression class
   */
  private static void checkCastStaticRules(Class tc, Class ec, Node n) {
    if (tc != ec) {
      if (tc.isPrimitive()) {
        if (tc == null || !ec.isPrimitive() ||
            ec == boolean.class || ec == void.class) {
          throw new ExecutionError("cast", n);
        }
      } else if (ec != null) {
        if (ec.isArray()) {
          if (tc.isArray()) {
            Class tec = tc.getComponentType();
            Class eec = ec.getComponentType();
            if (tec.isPrimitive() && eec.isPrimitive()) {
              if (tec != eec) {
                throw new ExecutionError("cast", n);
              }
            } else {
              checkCastStaticRules(tec, eec, n);
            }
          } else if (tc.isInterface() && tc != Cloneable.class) {
            throw new ExecutionError("cast", n);
          } else if (tc != Object.class) {
            throw new ExecutionError("cast", n);
          }
        } else if (ec.isInterface()) {
          if (tc.isInterface()) {
            // !!! TODO : tests the signatures ?
          } else if (tc.isArray()) {
            if (!Cloneable.class.isAssignableFrom(ec)) {
              throw new ExecutionError("cast", n);
            }
          } else if (Modifier.isFinal(tc.getModifiers())) {
            if (!tc.isAssignableFrom(ec)) {
              throw new ExecutionError("cast", n);
            }
          }
        } else if (tc.isInterface()) {
          if (Modifier.isFinal(tc.getModifiers())) {
            if (!tc.isAssignableFrom(ec)) {
              throw new ExecutionError("cast", n);
            }
          }
        } else if (!ec.isAssignableFrom(tc) && !tc.isAssignableFrom(ec)) {
          throw new ExecutionError("cast", n);
        }
      }
    }
  }

  /**
   * Check a list of node by running this type checker
   * on each node in the list.
   * @param the list of nodes to type check
   */
  private void checkList(List<? extends Node> l) {
    ListIterator<? extends Node> it = l.listIterator();
    while (it.hasNext()) {
      it.next().acceptVisitor(this);
    }
  }

  /**
   * Returns true iff the given class is an integral type
   * This includes both primitive and boxing integral types.<br><br>
   * Allowed primitives: byte, char, short, int, long
   * Allowed Refrence: Byte, Character, Short, Integer, Long
   * @param c The class to check
   * @return true iff the given class is an integral type
   */
  private static boolean _isIntegralType(Class c) {
    return (c == int.class   || c == Integer.class   ||
            c == long.class  || c == Long.class      ||
            c == byte.class  || c == Byte.class      ||
            c == char.class  || c == Character.class ||
            c == short.class || c == Short.class);
  }
  
  /**
   * Returns true iff the given class is a boxing (reference) type.
   * @param c the <code>Class</code> to check
   * @return true iff it is a boxing type
   */
  private static boolean _isBoxingType(Class c) {
    return (c == Integer.class   || c == Long.class   ||
            c == Boolean.class   || c == Double.class ||
            c == Character.class || c == Short.class  ||
            c == Byte.class      || c == Float.class );
  }
  
  private static boolean _boxesTo(Class prim, Class ref) {
    return 
      (prim == int.class     && ref == Integer.class)   ||
      (prim == long.class    && ref == Long.class)      ||
      (prim == byte.class    && ref == Byte.class)      ||
      (prim == char.class    && ref == Character.class) ||
      (prim == short.class   && ref == Short.class)     ||
      (prim == boolean.class && ref == Boolean.class)   ||
      (prim == float.class   && ref == Float.class)     ||
      (prim == double.class  && ref == Double.class);
  }
  
  /**
   * Boxes the given expression by returning the correct
   * <code>SimpleAllocation</code> corresponding to the given
   * primitive type.
   * @param exp the expression to box
   * @param primType the type of the primitive expression that is to be boxed
   * @return the <code>SimpleAllocation</code> that boxes the expression
   */
  private static SimpleAllocation _box(Expression exp, Class primType) {
    String refTypeName = "";
    Class refType = null;
    
    if (primType == boolean.class) {
      refTypeName = "Boolean";
      refType = Boolean.class;
    }
    else if (primType == byte.class) {
      refTypeName = "Byte";
      refType = Byte.class;
    }
    else if (primType == char.class) {
      refTypeName = "Character";
      refType = Character.class;
    }
    else if (primType == short.class) {
      refTypeName = "Short";
      refType = Short.class;
    }
    else if (primType == int.class) {
      refTypeName = "Integer";
      refType = Integer.class;
    }
    else if (primType == long.class) {
      refTypeName = "Long";
      refType = Long.class;
    }
    else if (primType == float.class) {
      refTypeName = "Float";
      refType = Float.class;
    }
    else if (primType == double.class) {
      refTypeName = "Double";
      refType = Double.class;
    }
    else {
      throw new ExecutionError("box.type", exp);
    }
    
    Constructor constructor;
    try {
      constructor = refType.getConstructor(new Class[] {primType});
    }
    catch (NoSuchMethodException nsme) {
      throw new RuntimeException("The constructor for " + refTypeName + " not found.");
    }
    ReferenceType ref = new ReferenceType(refTypeName,
                                          exp.getFilename(),
                                          exp.getBeginLine(),
                                          exp.getBeginColumn(),
                                          exp.getEndLine(),
                                          exp.getEndColumn());
    List<Expression> args = new LinkedList<Expression>();
    args.add(exp);
    SimpleAllocation alloc = new SimpleAllocation(ref, args,
                                                  exp.getFilename(),
                                                  exp.getBeginLine(),
                                                  exp.getBeginColumn(),
                                                  exp.getEndLine(),
                                                  exp.getEndColumn());
    alloc.setProperty(NodeProperties.CONSTRUCTOR, constructor);
    return alloc;
  }
  
  /**
   * Unboxes the given expression by returning the correct
   * <code>ObjectMethodCall</code> corresponding to the given type
   * @param child The expression to unbox
   * @param type The type of the evaluated expression
   * @return The <code>ObjectMethodCall</code> that unboxes the expression
   */
  private static ObjectMethodCall _unbox(Expression child, Class type) {
    String methodName = "";
    Class unboxedType = type;
    if (type == Boolean.class) {
      methodName = "booleanValue";
      unboxedType = boolean.class;
    }
    else if (type == Byte.class) {
      methodName = "byteValue";
      unboxedType = byte.class;
    }
    else if (type == Character.class) {
      methodName = "charValue";
      unboxedType = char.class;
    }
    else if (type == Short.class) {
      methodName = "shortValue";
      unboxedType = short.class;
    }
    else if (type == Integer.class) {
      methodName = "intValue";
      unboxedType = int.class;
    }
    else if (type == Long.class) {
      methodName = "longValue";
      unboxedType = long.class;
    }
    else if (type == Float.class) {
      methodName = "floatValue";
      unboxedType = float.class;
    }
    else if (type == Double.class) {
      methodName = "doubleValue";
      unboxedType = double.class;
    }
    else {
      throw new ExecutionError("unbox.type", child);
    }
    
    // Return a call to the method described in methodName
    // with no params and the file info of the child we are
    // unboxing
    Method method;
    try {
      method = type.getMethod(methodName, new Class[] {});
    }
    catch (NoSuchMethodException nsme) {
      throw new RuntimeException("The method " + methodName + " not found.");
    }
    ObjectMethodCall methodCall = new ObjectMethodCall(child, methodName, null,
                                                       child.getFilename(),
                                                       child.getBeginLine(),
                                                       child.getBeginColumn(),
                                                       child.getEndLine(),
                                                       child.getEndColumn());
    methodCall.setProperty(NodeProperties.METHOD, method);
    methodCall.setProperty(NodeProperties.TYPE, unboxedType);
    return methodCall;
  }
}
