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
import java.lang.reflect.Type;

/**
 * This tree visitor checks the typing rules and loads
 * the classes, fields and methods
 *
 * @author Stephane Hillion
 * @version 1.2 - 1999/11/20
 */

public class TypeChecker15 extends AbstractTypeChecker {
  
  /** Creates a new name visitor
   *  @param ctx the context
   */
  public TypeChecker15(Context<Type> ctx) { super(ctx); }
  
  /** Visits a ForEachStatement
   *  @param node the node to visit
   */
  public Type visit(ForEachStatement node){
    // Enter a new scope
    context.enterScope();
    context.define(node.getVars().get(0), null);
    context.define(node.getVars().get(1), null);
    
    Type paramTypeClass1;
    Type collTypeClass1;
    
    FormalParameter param = node.getParameter();
    Expression coll = node.getCollection();
    Node body = node.getBody();
    Class<?> component;
    
    paramTypeClass1 = param.acceptVisitor(this);
    collTypeClass1 = coll.acceptVisitor(this);
    body.acceptVisitor(this);
    
    Class<?> paramTypeClass = (Class<?>)paramTypeClass1;
    Class<?> collTypeClass = (Class<?>)collTypeClass1;
    
    /*for array access */
    /* remember to type check potential unbox/box situations */
    /* ie, an array of ints -> Integer or array of Integers -> int */
    if(collTypeClass.isArray()){
      component = collTypeClass.getComponentType();
      if(paramTypeClass.isAssignableFrom(component)){
        // noop, we typed checked ok.
      }else{
        // error, the parameter is not the same type as the collection items
        throw new ExecutionError("collection.type", node);
      }
      
    }else if(java.util.Collection.class.isAssignableFrom(collTypeClass)){
      // the collection is a Collection.
      // we still need to check it's component type
      
    }else{
      throw new ExecutionError("collection.type", node);
    }
    
    
    //---------------------------------------------
    
    node.getBody().acceptVisitor(this);
    
    // Leave the current scope and store the defined variables
    // (a map of String-Class mappings) in the "variables" property
    node.setProperty(NodeProperties.VARIABLES, context.leaveScope());
    
    
    return null;
  }
  
  /**
   * Does nothing - GenericReferenceType is allowed in 1.5
   * @param node unused
   */  
  protected void checkGenericReferenceType(ReferenceType node) {
    
  }
  
  /**
   * Does nothing - Variable Arguments are allowed in 1.5
   * Variable Arguments are currently Not working with jsr14 in the interactionspane
   * This check here can be deleted when this is fixed.
   * This is temporarily included to ensure decent feedback to the users of our new beta.
   * @param node - unused
   */
  protected void checkVarArgs(MethodDeclaration node) { 
    try {
      java.lang.reflect.Method.class.getMethod("isVarArgs", new Class<?>[]{});
    }
    catch (Throwable t) {
      throw new WrongVersionException("Variable Arguments are currently only supported in the InteractionsPane for Java Version 1.5 or better.");
    }    
  }
  
  /**
   * Visits an Import Declaration statement that is specifically static
   * @param node - the ImportDeclaration being visited
   */
  protected void staticImportHandler(ImportDeclaration node){
    try {
      if(node.isStaticImportClass()) 
        context.declareClassStaticImport(node.getName());
      else 
        context.declareMemberStaticImport(node.getName());
    } catch(ClassNotFoundException e) {
      throw new CatchedExceptionError(e,node);
    }
  }
  
  
  /**
   * Boxes the given expression by returning the correct
   * <code>SimpleAllocation</code> corresponding to the given
   * primitive type.
   * @param exp the expression to box
   * @param refType the reference type to box the primitive type to
   * @return the <code>SimpleAllocation</code> that boxes the expression
   */
  protected SimpleAllocation _box(Expression exp, Type typeRefType) {
    
    Class<?> refType = (Class<?>)typeRefType;
    String refTypeName = refType.getName();
    PrimitiveType primType = _correspondingPrimType(refType);
    
    Constructor constructor;
    try {
      constructor = refType.getConstructor(new Class<?>[] { primType.getValue() });
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
    CastExpression castExp = new CastExpression(primType, exp, 
                                                exp.getFilename(),
                                                exp.getBeginLine(),
                                                exp.getBeginColumn(),
                                                exp.getEndLine(),
                                                exp.getEndColumn());
    castExp.setProperty(NodeProperties.TYPE, primType.getValue());
    List<Expression> args = new LinkedList<Expression>();
    args.add(castExp);
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
  protected ObjectMethodCall _unbox(Expression child, Type type1) {
    
    String methodName = "";
    Class<?> unboxedType = (Class<?>)type1;
    Class<?> type = (Class<?>)type1;
    
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
      method = type.getMethod(methodName, new Class<?>[] {});
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
