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
import koala.dynamicjava.tree.tiger.generic.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * This tree visitor evaluates each node of a syntax tree
 *
 * @author Stephane Hillion
 * @version 1.2 - 2001/01/23
 */

public class EvaluationVisitor extends VisitorObject<Object> {
  /**
   * The current context
   */
  private Context context;

  /**
   * Creates a new visitor
   * @param ctx the current context
   */
  public EvaluationVisitor(Context ctx) {
    context = ctx;
  }

  /**
   * Visits a WhileStatement
   * @param node the node to visit
   */
  public Object visit(WhileStatement node) {
    try {
      while (((Boolean)node.getCondition().acceptVisitor(this)).booleanValue()) {
        try {
          node.getBody().acceptVisitor(this);
        } catch (ContinueException e) {
          // 'continue' statement management
          if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
            throw e;
          }
        }
      }
    } catch (BreakException e) {
      // 'break' statement management
      if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
        throw e;
      }
    }
    return null;
  }

  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   */
  public Object visit(ForEachStatement node){
    Set<AbstractVariable> vars = (Set<AbstractVariable>)node.getProperty(NodeProperties.VARIABLES);  /* Type erasure bites! */
      context.enterScope(vars);
        
    FormalParameter formalparam = node.getParameter();
    Expression collection = node.getCollection();
    Class<?> collTypeClass = NodeProperties.getType(collection);
    Node body = node.getBody();
    
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
     * ============================================================================
     */
    Method m;
    Class<?> c;
    Expression exp;
    /************************************************************************************/
    /**  create an initialization  ******************************************************/
    /************************************************************************************/

    VariableDeclaration init;
    if(collTypeClass.isArray()){
      exp = new IntegerLiteral("0");

      exp.setProperty(NodeProperties.TYPE, int.class);
      
      init = new VariableDeclaration(false,
                                     new IntType(),
                                     node.getVars().get(0),
                                     exp);
      
      c = NodeProperties.getType(exp);
      init.setProperty(NodeProperties.TYPE, c);
      init.getType().setProperty(NodeProperties.TYPE, c);
    }else{
      exp = new ObjectMethodCall(node.getCollection(), "iterator", null,
                                                  "", 0, 0, 0, 0);
      /* these next fields refer to fileame and row/col of source
       * but there isn't any source, b/c i'm making it... */
      
      /* set properties of exp */
      m = null;
      try {
        m = context.lookupMethod(((ObjectMethodCall)exp).getExpression(), "iterator", Constants.EMPTY_CLASS_ARRAY);
      }catch(NoSuchMethodException e){
        /* this is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }catch(MethodModificationError e){
        /* ths is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      } 
      exp.setProperty(NodeProperties.METHOD, m);
      exp.setProperty(NodeProperties.TYPE,   m.getReturnType());
      
      /* done setting properties */
      IdentifierToken javaId = new Identifier("java");
      IdentifierToken utilId = new Identifier("util");
      IdentifierToken iteratorId = new Identifier("Iterator");
      List<IdentifierToken> ids = new LinkedList<IdentifierToken>();
      ids.add(javaId); ids.add(utilId); ids.add(iteratorId);
      
      
      
      List<List<? extends koala.dynamicjava.tree.Type>> typeArgs = new LinkedList<List<? extends koala.dynamicjava.tree.Type>>();
      List<koala.dynamicjava.tree.Type> arg1 = new LinkedList<koala.dynamicjava.tree.Type>();
      arg1.add(formalparam.getType());
      typeArgs.add(arg1);
      init = new VariableDeclaration(false,
                                     new GenericReferenceType(ids,typeArgs),
                                     node.getVars().get(0),
                                     exp);
      
      c = NodeProperties.getType(exp);
      init.setProperty(NodeProperties.TYPE, c);
      init.getType().setProperty(NodeProperties.TYPE, c);
      
    }
      
      
    /************************************************************************************/
    /**  visit initialization  **********************************************************/
    /************************************************************************************/
    init.acceptVisitor(this);
    //everything is initialzized
    

    
    
    
    
    
    
    /************************************************************************************/
    /**  create a condition  ************************************************************/
    /************************************************************************************/
    Node condition;
    if(collTypeClass.isArray()){
      // make lhs
      IdentifierToken newforcounter = new Identifier(node.getVars().get(0));
      List<IdentifierToken> listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(newforcounter);
      QualifiedName lhs = new QualifiedName(listForQualifiedName);
      
      
      
      // make rhs
      LinkedList<Expression> argsToGetLength = new LinkedList<Expression>();
      argsToGetLength.add(collection);

      /*
      newforcounter = new Identifier("Array");
      listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(newforcounter);
      QualifiedName qf = new QualifiedName(listForQualifiedName);
      qf.setProperty(NodeProperties.TYPE, Array.class);
      ObjectMethodCall rhs = new ObjectMethodCall(qf, "getLength", argsToGetLength,
                                       "", 0, 0, 0, 0);
      */

      
      /* since we're finding the length of the array, the dimension is always 1 */
      ArrayType art = new ArrayType(formalparam.getType(), 1);
      
      StaticMethodCall rhs = new StaticMethodCall(art, "getLength", argsToGetLength);
      
      
      m = null;
      try {
        Class[] lookupArgs= {Object.class};
        m = Array.class.getMethod("getLength", lookupArgs);
      }catch(NoSuchMethodException e){
        /* this is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }catch(MethodModificationError e){
        /* ths is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      } 
      
      rhs.setProperty(NodeProperties.METHOD, m);
      rhs.setProperty(NodeProperties.TYPE,   m.getReturnType());
      
      
      
      condition = new LessExpression(lhs, rhs);
      condition.setProperty(NodeProperties.TYPE,   new BooleanType());
      
      /* 4-17-04 */
      /* maybe i need to set the type of hte condition node? */
      
      
      
    }else{
      IdentifierToken newforcounter = new Identifier(node.getVars().get(0));
      List<IdentifierToken> listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(newforcounter);
      QualifiedName qf = new QualifiedName(listForQualifiedName);
      condition = new ObjectMethodCall(qf, "hasNext", null,
                                       "", 0, 0, 0, 0);
      /* these next fields refer to fileame and row/col of source
       * but there isn't any source, b/c i'm making it... */
      /* set properties of condition */
      Method m2 = null;
      try {
        m2 = context.lookupMethod(exp, "hasNext", Constants.EMPTY_CLASS_ARRAY);
      }catch(NoSuchMethodException e){
        /* this is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }catch(MethodModificationError e){
        /* ths is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }
      condition.setProperty(NodeProperties.METHOD, m2);
      condition.setProperty(NodeProperties.TYPE,   m2.getReturnType());
      /* done setting properties */
    }

    
    
    /************************************************************************************/
    /**  create an update statement  ****************************************************/
    /************************************************************************************/
    Node statement1;
    Expression assignment = null;
    if(collTypeClass.isArray()){
      Identifier counterName = new Identifier(node.getVars().get(0));
      LinkedList<IdentifierToken> listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(counterName);
      QualifiedName arg = new QualifiedName(listForQualifiedName);
      VariableModifier mod = new VariableModifier(arg, int.class);
      arg.setProperty(NodeProperties.MODIFIER, mod);
      statement1 = new AddAssignExpression(arg, new IntegerLiteral("1"));
      statement1.setProperty(NodeProperties.TYPE, int.class);
      
      Expression arrayaccess = new ArrayAccess(collection, arg);

      listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(new Identifier(formalparam.getName()));
      arg = new QualifiedName(listForQualifiedName);
      mod = new VariableModifier(arg, collTypeClass.getComponentType());
      arg.setProperty(NodeProperties.MODIFIER, mod);
      assignment  = new SimpleAssignExpression(arg, arrayaccess);
      assignment.setProperty(NodeProperties.TYPE, collTypeClass.getComponentType());
    }else{
      IdentifierToken newforcounter = new Identifier(node.getVars().get(0));
      List<IdentifierToken> listForQualifiedName = new LinkedList<IdentifierToken>();
      listForQualifiedName.add(newforcounter);
      QualifiedName qf = new QualifiedName(listForQualifiedName);
      ObjectMethodCall next = new ObjectMethodCall(qf, "next", null,
                                                   "", 0, 0, 0, 0);
      /* these next fields refer to fileame and row/col of source
       * but there isn't any source, b/c i'm making it... */
      /* set properties of next */
      Method m3 = null;
      try {
        m3 = context.lookupMethod(exp, "next", Constants.EMPTY_CLASS_ARRAY);
      }catch(NoSuchMethodException e){
        /* this is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }catch(MethodModificationError e){
        /* ths is very bad */
        /* should never happen, b/c everything has typechecked etc and been ok'd */
      }
      next.setProperty(NodeProperties.METHOD, m3);
      next.setProperty(NodeProperties.TYPE,   m3.getReturnType());
      /* done setting properties */
      
      
      statement1 = new VariableDeclaration(formalparam.isFinal(), 
                                              formalparam.getType(), 
                                              formalparam.getName(), 
                                              next);
      statement1.setProperty(NodeProperties.TYPE, NodeProperties.getType(next));
      c = NodeProperties.getType(next);
      statement1.setProperty(NodeProperties.TYPE, c);
      ((VariableDeclaration)statement1).getType().setProperty(NodeProperties.TYPE, c);
    }
    
    
    
    
    /************************************************************************************/
    /**  evaluate the foreach  **********************************************************/
    /************************************************************************************/
    
    
    
    while(((Boolean)condition.acceptVisitor(this)).booleanValue()){
      /* if the foreach uses an array, then it will have an assignment instruction that must be run */
      /* otherwise it will be null */
      if(assignment != null){
        assignment.acceptVisitor(this);
      }
      statement1.acceptVisitor(this);
      body.acceptVisitor(this);
    }
    
    context.leaveScope();
    
    return null;
  }
  
  /**
   * Visits a ForStatement
   * @param node the node to visit
   */
  public Object visit(ForStatement node) {
    try {
      Set<AbstractVariable> vars = (Set<AbstractVariable>)node.getProperty(NodeProperties.VARIABLES); /* Type erasure bites! */
      context.enterScope(vars);

      // Interpret the initialization expressions
      if (node.getInitialization() != null) {
        Iterator<Node> it = node.getInitialization().iterator();
        while (it.hasNext()) {
          it.next().acceptVisitor(this);
        }
      }

      // Interpret the loop
      try {
        Expression cond   = node.getCondition();
        List<Node>  update = node.getUpdate();
        while (cond == null ||
               ((Boolean)cond.acceptVisitor(this)).booleanValue()) {
          try {
            node.getBody().acceptVisitor(this);
          } catch (ContinueException e) {
            // 'continue' statement management
            if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
              throw e;
            }
          }
          // Interpret the update statements
          if (update != null) {
            Iterator<Node> it = update.iterator();
            while (it.hasNext()) {
              it.next().acceptVisitor(this);
            }
          }
        }
      } catch (BreakException e) {
        // 'break' statement management
        if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
          throw e;
        }
      }
    } finally {
      // Always leave the current scope
      context.leaveScope();
    }
    return null;
  }

  /**
   * Visits a DoStatement
   * @param node the node to visit
   */
  public Object visit(DoStatement node) {
    try {
      // Interpret the loop
      do {
        try {
          node.getBody().acceptVisitor(this);
        } catch (ContinueException e) {
          // 'continue' statement management
          if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
            throw e;
          }
        }
      } while (((Boolean)node.getCondition().acceptVisitor(this)).booleanValue());
    } catch (BreakException e) {
      // 'break' statement management
      if (e.isLabeled() && !node.hasLabel(e.getLabel())) {
        throw e;
      }
    }
    return null;
  }

  /**
   * Visits a SwitchStatement
   * @param node the node to visit
   */
  public Object visit(SwitchStatement node) {
    try {
      boolean processed = false;

      // Evaluate the choice expression
      Object o = node.getSelector().acceptVisitor(this);
      if (o instanceof Character) {
        o = new Integer(((Character)o).charValue());
      }
      
      Object sel; // an Enum or a Number - no common ancestor except Object :(
      // used to be n
      
      Class<?> EnumClass = (TigerUtilities.isTigerEnabled() ? Class.forName("java.lang.Enum") : null);
      if (TigerUtilities.isTigerEnabled() && o.getClass().getSuperclass() == EnumClass){
        sel = /*(Enum)*/ o;
      }
      else {
        sel = (Number)o;
      }

      // Search for the matching label
      ListIterator<SwitchBlock> it = node.getBindings().listIterator();
      ListIterator<SwitchBlock> dit = null;
      loop: while (it.hasNext()) {
        SwitchBlock sc = it.next();
        Object bind = null; // used to be 'l' (small L)
        if (sc.getExpression() != null) {
          o = sc.getExpression().acceptVisitor(this);
          if (o instanceof Character) {
            o = new Integer(((Character)o).charValue());
          }
          if(TigerUtilities.isTigerEnabled() && o.getClass().getSuperclass() == EnumClass){
            bind = /*(Enum)*/ o;
          }
          else {
            bind = (Number)o;
          }
        } else {
          dit = node.getBindings().listIterator(it.nextIndex() - 1);
        }

        if (bind != null && 
            ((sel instanceof Number) && ((Number)sel).intValue() == ((Number)bind).intValue()) || 
            (TigerUtilities.isTigerEnabled() && (sel.getClass().getSuperclass() == EnumClass) && (/*(Enum)*/sel).equals(/*(Enum)*/bind))
           ){
          processed = true;
          // When a matching label is found, interpret all the
          // remaining statements
          for(;;) {
            if (sc.getStatements() != null) {
              Iterator<Node> it2 = sc.getStatements().iterator();
               while (it2.hasNext()) {
                it2.next().acceptVisitor(this);
              }
            }
            if (it.hasNext()) {
              sc = it.next();
            } else {
              break loop;
            }
          }
        }
      }

      if (!processed && dit != null) {
        SwitchBlock sc = dit.next();
        for(;;) {
          if (sc.getStatements() != null) {
            Iterator<Node> it2 = sc.getStatements().iterator();
            while (it2.hasNext()) {
              it2.next().acceptVisitor(this);
            }
          }
          if (dit.hasNext()) {
            sc = dit.next();
          } else {
            break;
          }
        }
      }
    } catch (BreakException e) {
      // 'break' statement management
      if (e.isLabeled()) {
        throw e;
      }
    } catch(ClassNotFoundException e){
      throw new ExecutionError("Tiger is enabled, but cannot find class java.lang.Enum! Please contact the DynamicJava/DrJava team (javaplt@cs.rice.edu).");
    } catch(NoClassDefFoundError e){
      throw new ExecutionError("Tiger is enabled, but cannot find class java.lang.Enum! Please contact the DynamicJava/DrJava team (javaplt@cs.rice.edu).");
    }
    
    return null;
  }

  /**
   * Visits a LabeledStatement
   * @param node the node to visit
   */
  public Object visit(LabeledStatement node) {
    try {
      node.getStatement().acceptVisitor(this);
    } catch (BreakException e) {
      // 'break' statement management
      if (!e.isLabeled() || !e.getLabel().equals(node.getLabel())) {
        throw e;
      }
    }
    return null;
  }

  /**
   * Visits a SynchronizedStatement
   * @param node the node to visit
   */
  public Object visit(SynchronizedStatement node) {
    synchronized(node.getLock().acceptVisitor(this)) {
      node.getBody().acceptVisitor(this);
    }
    return null;
  }

  /**
   * Visits a BreakStatement
   * @param node the node to visit
   */
  public Object visit(BreakStatement node) {
    throw new BreakException("unexpected.break", node.getLabel());
  }

  /**
   * Visits a ContinueStatement
   * @param node the node to visit
   */
  public Object visit(ContinueStatement node) {
    throw new ContinueException("unexpected.continue", node.getLabel());
  }

  /**
   * Visits a TryStatement
   * @param node the node to visit
   */
  public Object visit(TryStatement node) {
    boolean handled = false;
    try {
      node.getTryBlock().acceptVisitor(this);
    } catch (Throwable e) {
      Throwable t = e;
      if (e instanceof ThrownException) {
        t = ((ThrownException)e).getException();
      } else if (e instanceof CatchedExceptionError) {
        t = ((CatchedExceptionError)e).getException();
      }

      // Find the exception handler
      Iterator<CatchStatement> it = node.getCatchStatements().iterator();
      while (it.hasNext()) {
        CatchStatement cs = it.next();
        Class<?> c = NodeProperties.getType(cs.getException().getType());
        if (c.isAssignableFrom(t.getClass())) {
          handled = true;

          // Define the exception in a new scope
          context.enterScope();
          context.define(cs.getException().getName(), t);

          // Interpret the handler
          cs.getBlock().acceptVisitor(this);
          break;
        }
      }

      if (!handled) {
        if (e instanceof Error) {
          throw (Error)e;
        } else if (e instanceof RuntimeException) {
          throw (RuntimeException)e;
        } else {
          throw new CatchedExceptionError((Exception)e, node);
        }
      }
    } finally {
      // Leave the current scope if entered
      if (handled) {
        context.leaveScope();
      }

      // Interpret the 'finally' block
      Node n;
      if ((n = node.getFinallyBlock()) != null) {
        n.acceptVisitor(this);
      }
    }
    return null;
  }

  /**
   * Visits a ThrowStatement
   * @param node the node to visit
   */
  public Object visit(ThrowStatement node) {
    throw new ThrownException((Throwable)node.getExpression().acceptVisitor(this));
  }

  /**
   * Visits a ReturnStatement
   * @param node the node to visit
   */
  public Object visit(ReturnStatement node) {
    if (node.getExpression() != null) {
      throw new ReturnException("return.statement",
                                node.getExpression().acceptVisitor(this),
                                node);
    } else {
      throw new ReturnException("return.statement", node);
    }
  }

  /**
   * Visits a IfThenStatement
   * @param node the node to visit
   */
  public Object visit(IfThenStatement node) {
    if (((Boolean)node.getCondition().acceptVisitor(this)).booleanValue()) {
      node.getThenStatement().acceptVisitor(this);
    }
    return null;
  }

  /**
   * Visits a IfThenElseStatement
   * @param node the node to visit
   */
  public Object visit(IfThenElseStatement node) {
    if (((Boolean)node.getCondition().acceptVisitor(this)).booleanValue()) {
      node.getThenStatement().acceptVisitor(this);
    } else {
      node.getElseStatement().acceptVisitor(this);
    }
    return null;
  }

  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  public Object visit(BlockStatement node) {
    try {
      // Enter a new scope and define the local variables
      Set<AbstractVariable> vars = (Set<AbstractVariable>)node.getProperty(NodeProperties.VARIABLES); /* Type erasure bites! */
      context.enterScope(vars);

      // Interpret the statements
      Iterator<Node> it = node.getStatements().iterator();
      while (it.hasNext()) {
        it.next().acceptVisitor(this);
      }
    } finally {
      // Always leave the current scope
      context.leaveScope();
    }
    return null;
  }

  /**
   * Visits a Literal
   * @param node the node to visit
   */
  public Object visit(Literal node) {
    return node.getValue();
  }

  /**
   * Visits a VariableDeclaration
   * @param node the node to visit
   */
  public Object visit(VariableDeclaration node) {
    Class<?> c = NodeProperties.getType(node.getType());

    if (node.getInitializer() != null) {
      Object o = performCast(c, node.getInitializer().acceptVisitor(this));

      if (node.isFinal()) {
        context.setConstant(node.getName(), o);
      } else {
        context.set(node.getName(), o);
      }
    } else {
      if (node.isFinal()) {
        context.setConstant(node.getName(), UninitializedObject.INSTANCE);
      } else {
        context.set(node.getName(), UninitializedObject.INSTANCE);
      }
    }
    return null;
  }

  /**
   * Visits an ObjectFieldAccess
   * @param node the node to visit
   */
  public Object visit(ObjectFieldAccess node) {
    Class<?> c = NodeProperties.getType(node.getExpression());

    // Evaluate the object
    Object obj  = node.getExpression().acceptVisitor(this);

    if (!c.isArray()) {
      Field f = (Field)node.getProperty(NodeProperties.FIELD);
      // Relax the protection for members
      if (context.getAccessible()) {
        f.setAccessible(true);
      }
      try {
        return f.get(obj);
      } catch (Exception e) {
        throw new CatchedExceptionError(e, node);
      }
    } else {
      // If the object is an array, the field must be 'length'.
      // This field is not a normal field and it is the only
      // way to get it
      return new Integer(Array.getLength(obj));
    }
  }

  /**
   * Private method that is used for evaluating (calling) all kinds of variable arguments method calls
   * @param typs       The types of the formal parameters
   * @param typs_len   The number of formal parameters in the method declaration
   * @param larg_size  The number of actual parameters passed to the method
   * @param it         An expression iterator that holds the values of the actual arguments, STANDING
   *                     AT THE FIRST ACTUAL PARAMETER TO BE PUT IN THE VARARGS ARRAY
   */
  private Object buildArrayOfRemainingArgs(Class[] typs, int larg_size, Iterator<Expression> it) {
    if(! typs[typs.length-1].isArray())
      throw new RuntimeException("Last argument is not variable arguments");
        
    Class<?> componentType = typs[typs.length-1].getComponentType();
    Object argArray = Array.newInstance(componentType,new int[]{(larg_size-typs.length+1)});
    
    int n = larg_size-typs.length+1;
    for(int j = 0; j < n; j++){
      Object p  = it.next().acceptVisitor(this);
      Object casted = performCast(componentType, p);
      if (componentType.isPrimitive()) {
        if (componentType == boolean.class && casted instanceof Boolean) {
          Array.setBoolean(argArray, j, ((Boolean)casted).booleanValue());
        }
        else if (componentType == char.class && casted instanceof Character) {
          Array.setChar(argArray, j, ((Character)casted).charValue());
        }
        else if (casted instanceof Number) {
          if (componentType == char.class) { // just in case
            Array.setChar(argArray, j, (char)((Number)casted).intValue());
          }
          else if (componentType == byte.class) {
            Array.setByte(argArray, j, ((Number)casted).byteValue());
          }
          else if (componentType == short.class) {
            Array.setShort(argArray, j, ((Number)casted).shortValue());
          }
          else if (componentType == int.class) {
            Array.setInt(argArray, j, ((Number)casted).intValue());
          }
          else if (componentType == long.class) {
            Array.setLong(argArray, j, ((Number)casted).longValue());
          }
          else if (componentType == float.class) {
            Array.setFloat(argArray, j, ((Number)casted).floatValue());
          }
          else { // double
            Array.setDouble(argArray, j, ((Number)casted).doubleValue());
          }
        }
        else {
          throw new ClassCastException("Cannot insert object of type " + casted.getClass() + " into primitive array");
        }
      }
      else {
        Array.set(argArray, j, casted);
      }
    }
    return argArray;
  }
  
  
  /**
   * Visits an ObjectMethodCall.
   * This method expects that the argument list is compatible with
   * the declared method signature.
   * @param node the node to visit
   */
  public Object visit(ObjectMethodCall node) {
    Expression exp = node.getExpression();
    // Evaluate the receiver first
    Object obj  = exp.acceptVisitor(this);

    if (node.hasProperty(NodeProperties.METHOD)) {
      Method m    = (Method)node.getProperty(NodeProperties.METHOD);
      Class[]  typs = m.getParameterTypes();

      // Relax the protection for members?
      if (context.getAccessible()) {
        m.setAccessible(true);
      }

      List<Expression> larg = node.getArguments();
      Object[] args = Constants.EMPTY_OBJECT_ARRAY;

      // Fill the arguments
      if (larg != null) {
        args = new Object[typs.length];
        ListIterator<Expression> it = larg.listIterator();
        int i  = 0;
        while (i < typs.length-1) {
          Object p  = it.next().acceptVisitor(this);
          args[i] = performCast(typs[i], p);
          i++;
        }
        if(typs.length > 0){
          Object last = null;
          if (it.hasNext()) {
            last = it.next().acceptVisitor(this);
          }
          
          if(!TigerUtilities.isVarArgs(m)) {
            args[i] = last;
          }
//          else if (last == null) {
//            Class<?> compType = typs[i].getComponentType();
//            args[i] = Array.newInstance(compType, 0);
//          }
          else if (last != null && typs[i].isAssignableFrom(last.getClass())){
            args[i] = last;
          }
          else { // Either more/less args given than expected or is of the component type
            it.previous(); // back up since we pulled the expression out a few lines above
            args[i] = buildArrayOfRemainingArgs(typs, larg.size(), it);
          }
        }
      }
      // Invoke the method
      try {
        Object res = m.invoke(obj, args);
        return res;
      }
      catch (InvocationTargetException e) {
        if (e.getTargetException() instanceof Error) {
          throw (Error)e.getTargetException();
        }
        else if (e.getTargetException() instanceof RuntimeException) {
          throw (RuntimeException)e.getTargetException();
        }
        throw new ThrownException(e.getTargetException(), node);
      }
      catch (Exception e) {
        throw new CatchedExceptionError(e, node);
      }
    }
    else {
      // If the 'method' property is not set, the object must be
      // an array and the called method must be 'clone'.
      // Since the 'clone' method of an array is not a normal
      // method, the only way to invoke it is to simulate its
      // behaviour.
      Class<?> c = NodeProperties.getType(exp);
      int len = Array.getLength(obj);
      Object result = Array.newInstance(c.getComponentType(), len);
      for (int i = 0; i < len; i++) {
        Array.set(result, i, Array.get(obj, i));
      }
      return result;
    }
  }

  /**
   * Visits a StaticFieldAccess
   * @param node the node to visit
   */
  public Object visit(StaticFieldAccess node) {
    Field f = (Field)node.getProperty(NodeProperties.FIELD);
    try {
      return f.get(null);
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Visits a SuperFieldAccess
   * @param node the node to visit
   */
  public Object visit(SuperFieldAccess node) {
    Field f = (Field)node.getProperty(NodeProperties.FIELD);
    try {
      return f.get(context.getHiddenArgument());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Visits a SuperMethodCall
   * @param node the node to visit
   */
  public Object visit(SuperMethodCall node) {
    Method   m     = (Method)node.getProperty(NodeProperties.METHOD);
    List<Expression> larg  = node.getArguments();
    Object[] args  = Constants.EMPTY_OBJECT_ARRAY;

    Class[] typs = m.getParameterTypes();
    
    // Fill the arguments
    if (larg != null) {
      ListIterator<Expression> it = larg.listIterator();
      int      i  = 0;
      args        = new Object[typs.length];
      while (i < typs.length-1) {
        args[i] = it.next().acceptVisitor(this);
        i++;
      }
      if(typs.length > 0){
        Object last = null;
        if (it.hasNext()) {
          last = it.next().acceptVisitor(this);
        }
        
        if(!TigerUtilities.isVarArgs(m)) {
          args[i] = last;
        }
        //          else if (last == null) {
        //            Class<?> compType = typs[i].getComponentType();
        //            args[i] = Array.newInstance(compType, 0);
        //          }
        else if (last != null && typs[i].isAssignableFrom(last.getClass())){
          args[i] = last;
        }
        else { // Either more/less args given than expected or is of the component type
          it.previous(); // back up since we pulled the expression out a few lines above
          args[i] = buildArrayOfRemainingArgs(typs, larg.size(), it);
        }
      }
    }

    // Invoke the method
    try {
      return m.invoke(context.getHiddenArgument(), args);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof Error) {
        throw (Error)e.getTargetException();
      } else if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)e.getTargetException();
      }
      throw new ThrownException(e.getTargetException());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Visits a StaticMethodCall
   * @param node the node to visit
   */
  public Object visit(StaticMethodCall node) {
    Method   m    = (Method)node.getProperty(NodeProperties.METHOD);
    List<Expression> larg = node.getArguments();
    Object[] args = Constants.EMPTY_OBJECT_ARRAY;

    Class[] typs = m.getParameterTypes();
    
    // Fill the arguments
    if (larg != null) {
      args = new Object[typs.length];
      ListIterator<Expression> it = larg.listIterator();
      int      i  = 0;
      while (i < typs.length-1) {
        args[i] = it.next().acceptVisitor(this);
        i++;
      }
      if(typs.length > 0){
        Object last = null;
        if (it.hasNext()) {
          last = it.next().acceptVisitor(this);
        }
        
        if(!TigerUtilities.isVarArgs(m)) {
          args[i] = last;
        }
        //          else if (last == null) {
        //            Class<?> compType = typs[i].getComponentType();
        //            args[i] = Array.newInstance(compType, 0);
        //          }
        else if (last != null && typs[i].isAssignableFrom(last.getClass())){
          args[i] = last;
        }
        else { // Either more/less args given than expected or is of the component type
          it.previous(); // back up since we pulled the expression out a few lines above
          args[i] = buildArrayOfRemainingArgs(typs, larg.size(), it);
        }
      }
    }

    // Invoke the method
    try {
      return m.invoke(null, args);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof Error) {
        throw (Error)e.getTargetException();
      } else if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)e.getTargetException();
      }
      throw new ThrownException(e.getTargetException());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Visits a SimpleAssignExpression
   * @param node the node to visit
   */
  public Object visit(SimpleAssignExpression node) {
    Node ln = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(ln);
    mod.prepare(this, context);
    Object val  = node.getRightExpression().acceptVisitor(this);
    val = performCast(NodeProperties.getType(node), val);
    mod.modify(context, val);
    return val;
  }

  /**
   * Visits a QualifiedName
   * @param node the node to visit
   * @return the value of the local variable represented by this node
   */
  public Object visit(QualifiedName node) {
    Object result = context.get(node.getRepresentation());
    if (result == UninitializedObject.INSTANCE) {
      node.setProperty(NodeProperties.ERROR_STRINGS,
                       new String[] { node.getRepresentation() });
      throw new ExecutionError("uninitialized.variable", node);
    }
    return result;
  }

  /**
   * Visits a TypeExpression
   * @param node the node to visit
   */
  public Object visit(TypeExpression node) {
    return node.getProperty(NodeProperties.VALUE);
  }

  /**
   * Visits a SimpleAllocation
   * @param node the node to visit
   */
  public Object visit(SimpleAllocation node) {
    Constructor cons = (Constructor)node.getProperty(NodeProperties.CONSTRUCTOR);
    List<Expression> larg = node.getArguments();
    Object[] args = Constants.EMPTY_OBJECT_ARRAY;

    Class[] typs = cons.getParameterTypes();

    // Fill the arguments
    if (larg != null) {
      args = new Object[typs.length];
      ListIterator<Expression> it = larg.listIterator();
      int      i  = 0;
      while (i < typs.length-1) {
        args[i++] = it.next().acceptVisitor(this);
      }
      if(typs.length > 0){
        Object last = null;
        if (it.hasNext()) {
          last = it.next().acceptVisitor(this);
        }
        
        if(!TigerUtilities.isVarArgs(cons)) {
          args[i] = last;
        }
        //          else if (last == null) {
        //            Class<?> compType = typs[i].getComponentType();
        //            args[i] = Array.newInstance(compType, 0);
        //          }
        else if (last != null && typs[i].isAssignableFrom(last.getClass())){
          args[i] = last;
        }
        else { // Either more/less args given than expected or is of the component type
          it.previous(); // back up since we pulled the expression out a few lines above
          args[i] = buildArrayOfRemainingArgs(typs, larg.size(), it);
        }
      }
    }

    return context.invokeConstructor(node, args);
  }

  /**
   * Visits an ArrayAllocation
   * @param node the node to visit
   */
  public Object visit(ArrayAllocation node) {
    // Visits the initializer if one
    if (node.getInitialization() != null) {
      return node.getInitialization().acceptVisitor(this);
    }

    // Evaluate the size expressions
    int[]    dims = new int[node.getSizes().size()];
    Iterator<Expression> it = node.getSizes().iterator();
    int i  = 0;
    while (it.hasNext()) {
      Number n = (Number)it.next().acceptVisitor(this);
      dims[i++] = n.intValue();
    }

    // Create the array
    if (node.getDimension() != dims.length) {
      Class<?> c = NodeProperties.getComponentType(node);
      c = Array.newInstance(c, 0).getClass();
      return Array.newInstance(c, dims);
    } else {
      return Array.newInstance(NodeProperties.getComponentType(node), dims);
    }
  }

  /**
   * Visits a ArrayInitializer
   * @param node the node to visit
   */
  public Object visit(ArrayInitializer node) {
    Object result = Array.newInstance(NodeProperties.getType(node.getElementType()),
                                      node.getCells().size());

    Iterator<Expression> it = node.getCells().iterator();
    int      i  = 0;
    while (it.hasNext()) {
      Object o = it.next().acceptVisitor(this);
      Array.set(result, i++, o);
    }
    return result;
  }

  /**
   * Visits an ArrayAccess
   * @param node the node to visit
   */
  public Object visit(ArrayAccess node) {
    Object t = node.getExpression().acceptVisitor(this);
    Object o = node.getCellNumber().acceptVisitor(this);
    if (o instanceof Character) {
      o = new Integer(((Character)o).charValue());
    }
    return Array.get(t, ((Number)o).intValue());
  }

  /**
   * Visits a InnerAllocation
   * @param node the node to visit
   */
  public Object visit(InnerAllocation node) {
    Constructor cons = (Constructor)node.getProperty(NodeProperties.CONSTRUCTOR);
    Class<?>       c    = NodeProperties.getType(node);

    List<Expression> larg = node.getArguments();
    Object[]    args = null;

    Class[] typs = cons.getParameterTypes();
    
    if (larg != null) {
      args = new Object[typs.length];
      args[0] = node.getExpression().acceptVisitor(this);

      ListIterator<Expression> it = larg.listIterator();
      int      i  = 1;
      while (i < typs.length-1) { // the +1 for the hidden parameter, and the -1 for the varargs
        args[i++] = it.next().acceptVisitor(this);
      }
      if(typs.length > 0){
        Object last = null;
        if (it.hasNext()) {
          last = it.next().acceptVisitor(this);
        }
        
        if(!TigerUtilities.isVarArgs(cons)) {
          args[i] = last;
        }
        //          else if (last == null) {
        //            Class<?> compType = typs[i].getComponentType();
        //            args[i] = Array.newInstance(compType, 0);
        //          }
        else if (last != null && typs[i].isAssignableFrom(last.getClass())){
          args[i] = last;
        }
        else { // Either more/less args given than expected or is of the component type
          it.previous(); // back up since we pulled the expression out a few lines above
          args[i] = buildArrayOfRemainingArgs(typs, larg.size()+1, it);
        }
      }
    } 
    else {
      args = new Object[] { node.getExpression().acceptVisitor(this) };
    }

    // Invoke the constructor
    try {
      return cons.newInstance(args);
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof Error) {
        throw (Error)e.getTargetException();
      }  else if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)e.getTargetException();
      }
      throw new ThrownException(e.getTargetException());
    } catch (Exception e) {
      throw new CatchedExceptionError(e, node);
    }
  }

  /**
   * Visits a ClassAllocation
   * @param node the node to visit
   */
  public Object visit(ClassAllocation node) {
    Constructor cons = (Constructor)node.getProperty(NodeProperties.CONSTRUCTOR);
    List<Expression> larg = node.getArguments();
    Object[]    args = Constants.EMPTY_OBJECT_ARRAY;

    Class[] typs = cons.getParameterTypes();
    
    // Fill the arguments
    if (larg != null) {
      args = new Object[typs.length];
      ListIterator<Expression> it = larg.listIterator();
      int      i  = 0;
      while (i < typs.length-1) {
        args[i++] = it.next().acceptVisitor(this);
      }
      if(typs.length > 0){
        Object last = null;
        if (it.hasNext()) {
          last = it.next().acceptVisitor(this);
        }
        
        if(!TigerUtilities.isVarArgs(cons)) {
          args[i] = last;
        }
        //          else if (last == null) {
        //            Class<?> compType = typs[i].getComponentType();
        //            args[i] = Array.newInstance(compType, 0);
        //          }
        else if (last != null && typs[i].isAssignableFrom(last.getClass())){
          args[i] = last;
        }
        else { // Either more/less args given than expected or is of the component type
          it.previous(); // back up since we pulled the expression out a few lines above
          args[i] = buildArrayOfRemainingArgs(typs, larg.size(), it);
        }
      }
    }

    return context.invokeConstructor(node, args);
  }

  /**
   * Visits a NotExpression
   * @param node the node to visit
   */
  public Object visit(NotExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Boolean b = (Boolean)node.getExpression().acceptVisitor(this);
      if (b.booleanValue()) {
        return Boolean.FALSE;
      } else {
        return Boolean.TRUE;
      }
    }
  }

  /**
   * Visits a ComplementExpression
   * @param node the node to visit
   */
  public Object visit(ComplementExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Class<?>  c = NodeProperties.getType(node);
      Object o = node.getExpression().acceptVisitor(this);

      if (o instanceof Character) {
        o = new Integer(((Character)o).charValue());
      }
      if (c == int.class) {
        return new Integer(~((Number)o).intValue());
      } else {
        return new Long(~((Number)o).longValue());
      }
    }
  }

  /**
   * Visits a PlusExpression
   * @param node the node to visit
   */
  public Object visit(PlusExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.plus
        (NodeProperties.getType(node),
         node.getExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a MinusExpression
   * @param node the node to visit
   */
  public Object visit(MinusExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.minus
        (NodeProperties.getType(node),
         node.getExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a AddExpression
   * @param node the node to visit
   */
  public Object visit(AddExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.add
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits an AddAssignExpression
   * @param node the node to visit
   */
  public Object visit(AddAssignExpression node) {
    Node left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);
    
    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.add(calcType, lhs,rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a SubtractExpression
   * @param node the node to visit
   */
  public Object visit(SubtractExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.subtract
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits an SubtractAssignExpression
   * @param node the node to visit
   */
  public Object visit(SubtractAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);
  
    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.subtract(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a MultiplyExpression
   * @param node the node to visit
   */
  public Object visit(MultiplyExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.multiply
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits an MultiplyAssignExpression
   * @param node the node to visit
   */
  public Object visit(MultiplyAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.multiply(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Object visit(DivideExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.divide
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits an DivideAssignExpression
   * @param node the node to visit
   */
  public Object visit(DivideAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.divide(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Object visit(RemainderExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.remainder
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits an RemainderAssignExpression
   * @param node the node to visit
   */
  public Object visit(RemainderAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.remainder(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits an EqualExpression
   * @param node the node to visit
   */
  public Object visit(EqualExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.equalTo(NodeProperties.getType(ln),
                                          NodeProperties.getType(rn),
                                          ln.acceptVisitor(this),
                                          rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a NotEqualExpression
   * @param node the node to visit
   */
  public Object visit(NotEqualExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.notEqualTo(NodeProperties.getType(ln),
                                             NodeProperties.getType(rn),
                                             ln.acceptVisitor(this),
                                             rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a LessExpression
   * @param node the node to visit
   */
  public Object visit(LessExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.lessThan(ln.acceptVisitor(this),
                                           rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a LessOrEqualExpression
   * @param node the node to visit
   */
  public Object visit(LessOrEqualExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.lessOrEqual(ln.acceptVisitor(this),
                                              rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a GreaterExpression
   * @param node the node to visit
   */
  public Object visit(GreaterExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.greaterThan(ln.acceptVisitor(this),
                                              rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a GreaterOrEqualExpression
   * @param node the node to visit
   */
  public Object visit(GreaterOrEqualExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      Node ln = node.getLeftExpression();
      Node rn = node.getRightExpression();
      return InterpreterUtilities.greaterOrEqual(ln.acceptVisitor(this),
                                                 rn.acceptVisitor(this));
    }
  }

  /**
   * Visits a InstanceOfExpression
   * @param node the node to visit
   */
  public Object visit(InstanceOfExpression node) {
    Object v = node.getExpression().acceptVisitor(this);
    Class<?>  c = NodeProperties.getType(node.getReferenceType());

    return (c.isInstance(v)) ? Boolean.TRUE : Boolean.FALSE;
  }

  /**
   * Visits a ConditionalExpression
   * @param node the node to visit
   */
  public Object visit(ConditionalExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } 
    else {
      Boolean b = (Boolean)node.getConditionExpression().acceptVisitor(this);
      if (b.booleanValue()) {
        return node.getIfTrueExpression().acceptVisitor(this);
      } 
      else {
        return node.getIfFalseExpression().acceptVisitor(this);
      }
    }
  }

  /**
   * Visits a PostIncrement
   * @param node the node to visit
   */
  public Object visit(PostIncrement node) {
    Node exp = node.getExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(exp);
    Object v = mod.prepare(this, context);

    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.add(calcType, v, InterpreterUtilities.ONE);

    // Cast the result
    mod.modify(context, result);
    return v;
  }

  /**
   * Visits a PreIncrement
   * @param node the node to visit
   */
  public Object visit(PreIncrement node) {
    Node exp = node.getExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(exp);
    Object v = mod.prepare(this, context);

    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.add(calcType, v, InterpreterUtilities.ONE);

    // Cast the result
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a PostDecrement
   * @param node the node to visit
   */
  public Object visit(PostDecrement node) {
    Node exp = node.getExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(exp);
    Object v = mod.prepare(this, context);

    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.subtract(calcType, v, InterpreterUtilities.ONE);

    // Cast the result
    mod.modify(context, result);
    return v;
  }

  /**
   * Visits a PreDecrement
   * @param node the node to visit
   */
  public Object visit(PreDecrement node) {
    Node exp = node.getExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(exp);
    Object v = mod.prepare(this, context);

    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.subtract(calcType, v, InterpreterUtilities.ONE);

    // Cast the result
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a CastExpression
   * @param node the node to visit
   */
  public Object visit(CastExpression node) {
    return performCast(NodeProperties.getType(node),
                       node.getExpression().acceptVisitor(this));
  }

  /**
   * Visits a BitAndExpression
   * @param node the node to visit
   */
  public Object visit(BitAndExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.bitAnd
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a BitAndAssignExpression
   * @param node the node to visit
   */
  public Object visit(BitAndAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.bitAnd(calcType, lhs,rhs);

    // Cast the result
    result = performCast(calcType, result);


    // Modify the variable and return
    NodeProperties.getModifier(left).modify(context, result);
    return result;
  }

  /**
   * Visits a ExclusiveOrExpression
   * @param node the node to visit
   */
  public Object visit(ExclusiveOrExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.xOr
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a ExclusiveOrAssignExpression
   * @param node the node to visit
   */
  public Object visit(ExclusiveOrAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.xOr(calcType, lhs,rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a BitOrExpression
   * @param node the node to visit
   */
  public Object visit(BitOrExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.bitOr
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a BitOrAssignExpression
   * @param node the node to visit
   */
  public Object visit(BitOrAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.bitOr(calcType, lhs,rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a ShiftLeftExpression
   * @param node the node to visit
   */
  public Object visit(ShiftLeftExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.shiftLeft
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a ShiftLeftAssignExpression
   * @param node the node to visit
   */
  public Object visit(ShiftLeftAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.shiftLeft(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a ShiftRightExpression
   * @param node the node to visit
   */
  public Object visit(ShiftRightExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.shiftRight
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a ShiftRightAssignExpression
   * @param node the node to visit
   */
  public Object visit(ShiftRightAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.shiftRight(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits a UnsignedShiftRightExpression
   * @param node the node to visit
   */
  public Object visit(UnsignedShiftRightExpression node) {
    if (node.hasProperty(NodeProperties.VALUE)) {
      // The expression is constant
      return node.getProperty(NodeProperties.VALUE);
    } else {
      return InterpreterUtilities.unsignedShiftRight
        (NodeProperties.getType(node),
         node.getLeftExpression().acceptVisitor(this),
         node.getRightExpression().acceptVisitor(this));
    }
  }

  /**
   * Visits a UnsignedShiftRightAssignExpression
   * @param node the node to visit
   */
  public Object visit(UnsignedShiftRightAssignExpression node) {
    Node   left = node.getLeftExpression();
    LeftHandSideModifier mod = NodeProperties.getModifier(left);

    Object lhs = mod.prepare(this, context);
    Object rhs = node.getRightExpression().acceptVisitor(this);
    Class<?> resType = NodeProperties.getType(node);
    Class<?> calcType = resType; // always a primitive type
    
    // This code facilitates autoboxing/unboxing
    if (TigerUtilities.isBoxingType(resType)) {
      calcType = TigerUtilities.correspondingPrimType(resType);
    }
    
    // Perform the operation
    Object result = InterpreterUtilities.unsignedShiftRight(calcType, lhs, rhs);

    // Cast the result
    result = performCast(calcType, result);

    // Modify the variable and return
    mod.modify(context, result);
    return result;
  }

  /**
   * Visits an AndExpression
   * @param node the node to visit
   */
  public Object visit(AndExpression node) {
    Expression exp = node.getLeftExpression();
    boolean b = ((Boolean)exp.acceptVisitor(this)).booleanValue();
    if (b) {
      exp = node.getRightExpression();
      b = ((Boolean)exp.acceptVisitor(this)).booleanValue();
      return (b) ? Boolean.TRUE : Boolean.FALSE;
    }
    return Boolean.FALSE;
  }

  /**
   * Visits an OrExpression
   * @param node the node to visit
   */
  public Object visit(OrExpression node) {
    Expression exp = node.getLeftExpression();
    boolean b = ((Boolean)exp.acceptVisitor(this)).booleanValue();
    if (!b) {
      exp = node.getRightExpression();
      b = ((Boolean)exp.acceptVisitor(this)).booleanValue();
      return (b) ? Boolean.TRUE : Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  /**
   * Visits a FunctionCall
   * @param node the node to visit
   */
  public Object visit(FunctionCall node) {
    MethodDeclaration md;
    md = (MethodDeclaration)node.getProperty(NodeProperties.FUNCTION);

    // Enter a new scope and define the parameters as local variables
    Context c = new GlobalContext(context.getInterpreter());
    if (node.getArguments() != null) {
      Iterator<FormalParameter> it1  = md.getParameters().iterator();
      Iterator<Expression> it2 = node.getArguments().iterator();
      while (it1.hasNext()) {
        FormalParameter fp = it1.next();
        if (fp.isFinal()) {
          c.setConstant(fp.getName(), it2.next().acceptVisitor(this));
        } else {
          c.setVariable(fp.getName(), it2.next().acceptVisitor(this));
        }
      }
    }

    // Do the type checking of the body if needed
    Node body = md.getBody();
    if (!body.hasProperty("visited")) {
      body.setProperty("visited", null);
      ImportationManager im =
        (ImportationManager)md.getProperty(NodeProperties.IMPORTATION_MANAGER);
      Context ctx = new GlobalContext(context.getInterpreter());
      ctx.setImportationManager(im);

      NameVisitor nv = new NameVisitor(ctx,ctx);
      Iterator<FormalParameter> it = md.getParameters().iterator();
      while (it.hasNext()) {
        it.next().acceptVisitor(nv);
      }
      body.acceptVisitor(nv);

      ctx = new GlobalContext(context.getInterpreter());
      ctx.setImportationManager(im);
      ctx.setFunctions((List<MethodDeclaration>)md.getProperty(NodeProperties.FUNCTIONS)); /* Type erasure bites */

      AbstractTypeChecker tc = AbstractTypeChecker.makeTypeChecker(ctx);
      it = md.getParameters().iterator();
      while (it.hasNext()) {
        it.next().acceptVisitor(tc);
      }
      body.acceptVisitor(tc);
    }

    // Interpret the body of the function
    try {
      body.acceptVisitor(new EvaluationVisitor(c));
    } catch (ReturnException e) {
      return e.getValue();
    }
    return null;
  }

  /**
   * Performs a dynamic cast. This method acts on primitive wrappers.
   * @param tc the target class
   * @param o  the object to cast
   */
  protected static Object performCast(Class<?> tc, Object o) {
    Class<?> ec = (o != null) ? o.getClass() : null;

    if (tc != ec && tc.isPrimitive() && ec != null) {
      if (tc != char.class && ec == Character.class) {
        o = new Character(((Character)o).charValue());
      } 
      else if (tc == byte.class) {
        o = new Byte(((Number)o).byteValue());
      } 
      else if (tc == short.class) {
        o = new Short(((Number)o).shortValue());
      }
      else if (tc == int.class) {
        o = new Integer(((Number)o).intValue());
      }
      else if (tc == long.class) {
        o = new Long(((Number)o).longValue());
      }
      else if (tc == float.class) {
        o = new Float(((Number)o).floatValue());
      }
      else if (tc == double.class) {
        o = new Double(((Number)o).doubleValue());
      }
      else if (tc == char.class && ec != Character.class) {
        o = new Character((char)((Number)o).shortValue());
      }
    }
    return o;
  }
}
