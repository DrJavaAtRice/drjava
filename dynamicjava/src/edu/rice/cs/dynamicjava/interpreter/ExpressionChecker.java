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

/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2008 JavaPLT group at Rice University (drjava@rice.edu)
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
 *       drjava@rice.edu.
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

package edu.rice.cs.dynamicjava.interpreter;

import java.util.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.lambda.Thunk;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.TypeSystem.*;
import edu.rice.cs.dynamicjava.symbol.type.*;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.interpreter.TypeUtil;

import static koala.dynamicjava.interpreter.NodeProperties.*;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * This tree visitor checks the typing rules for expressions and determines each expression's type.
 * (Somewhat incongruously, it also processes TypeNames.)
 * The following properties (from {@code NodeProperties}) are set:<ul>
 * <li>TYPE on all {@code Expression}s and on {@code FormalParameter}s</li>
 * <li>CONVERTED_TYPE on any {@code CastExpression} subexpressions that require runtime conversion</li>
 * <li>CHECKED_TYPE on any {@code CastExpression} subexpressions, {@code FieldAccess}es, or 
 *     {@code MethodCall}s that require runtime checking</li>
 * <li>ERASED_TYPE on all {@code ArrayAllocation}s, and {@code ArrayInitializer}s,
 *     and the nested type of {@code TypeExpression}s and {@code InstanceofExpression}s</li>
 * <li>LEFT_EXPRESSION on assignments, increments, and decrements that access the left expression in
 *     order to evaluate the right expression</li>
 * <li>VARIABLE_TYPE on all lvalue expressions</li>
 * <li>SUPER_TYPE on all AnonymousInnerAllocations</li>
 * <li>VALUE on all constant expressions</li>
 * <li>DJCLASS on {@code AnonymousAllocation}s, {@code AnonymousInnerAllocation}s,
 *     {@code ThisExpression}s, {@code SuperMethodCall}s, {@code SuperFieldAccess}es, and 
 *     non-static {@code SimpleMethodCall}s and {@code SimpleFieldAccess}es.</li>
 * <li>CONSTRUCTOR on {@code ConstructorCall}s and all allocations: {@code SimpleAllocation}s,
 *     {@code InnerAllocation}s, {@code AnonymousAllocation}s, and {@code AnonymousInnerAllocation}s</li>
 * <li>FIELD on all {@code FieldAccess}es</li>
 * <li>METHOD on all {@code MethodCall}s</li>
 * <li>VARIABLE on all {@code VariableAccess}es, {@code VariableDeclarations}, and {@code FormalParameters}</li>
 * <li>OPERATION on all {@code AddExpression}s, {@code AddAssignExpression}s, {@code EqualExpression}s, and 
 *     {@code NotEqualExpression}s</li>
 * </ul>
 */
// TODO: Handle non-literal constant expressions
public class ExpressionChecker {
  
  private final TypeContext context;
  private final TypeSystem ts;
  private final Options opt;

  public ExpressionChecker(TypeContext ctx, Options options) {
    context = ctx;
    ts = options.typeSystem();
    opt = options;
  }
  
  public Type check(Expression e) {
    return e.acceptVisitor(new ExpressionVisitor(Option.<Type>none()));
  }
  
  public Type check(Expression e, Type expected) {
    return e.acceptVisitor(new ExpressionVisitor(Option.some(expected)));
  }
  
  public Type check(Expression e, Option<Type> expected) {
    return e.acceptVisitor(new ExpressionVisitor(expected));
  }
  
  public Iterable<Type> checkList(Iterable<? extends Expression> l) {
    return IterUtil.mapSnapshot(l, new ExpressionVisitor(Option.<Type>none()));
  }
  
  public Iterable<Type> checkList(Iterable<? extends Expression> l, Type expected) {
    return IterUtil.mapSnapshot(l, new ExpressionVisitor(Option.some(expected)));
  }
  
  public Iterable<Type> checkList(Iterable<? extends Expression> l, Option<Type> expected) {
    return IterUtil.mapSnapshot(l, new ExpressionVisitor(expected));
  }
  
  private Type checkTypeName(TypeName t) {
    return new TypeNameChecker(context, opt).check(t);
  }
  
  private Iterable<Type> checkTypeNameList(Iterable<? extends TypeName> l) {
    return new TypeNameChecker(context, opt).checkList(l);
  }
  
  
  private class ExpressionVisitor extends AbstractVisitor<Type> implements Lambda<Expression, Type> {
  
    // acts as a hint for inference; does not directly trigger type errors
    private final Option<Type> expected;
    
    public ExpressionVisitor(Option<Type> exp) { expected = exp; }
    
    public Type value(Expression e) { return e.acceptVisitor(this); }
    
    private String nodeTypesString(Iterable<? extends Node> nodes) {
      Lambda<Node, String> typeString = new Lambda<Node, String>() {
        public String value(Node n) { return ts.userRepresentation(getType(n)); }
      };
      return IterUtil.toString(IterUtil.map(nodes, typeString), "", ", ", "");
    }
    
    
    @Override public Type visit(AmbiguousName node) {
      Node resolved = resolveAmbiguousName(node);
      if (resolved instanceof ReferenceTypeName) {
        setErrorStrings(node, ((ReferenceTypeName) resolved).getRepresentation());
        throw new ExecutionError("undefined.name", node);
      }
      else {
        Expression resolvedExp = (Expression) resolved;
        resolvedExp.acceptVisitor(this);
        setTranslation(node, resolvedExp);
        // VARIABLE_TYPE and TYPE properties are important in the enclosing context; others
        // (such as FIELD) are not, and need not be copied to the AmbiguousName
        if (hasVariableType(resolvedExp)) { setVariableType(node, getVariableType(resolvedExp)); }
        return setType(node, getType(resolvedExp));
      }
    }
    
    /**
     * Produce an appropriate translation of an AmbiguousName.  The result will be one of: VariableAccess,
     * SimpleFieldAccess, StaticFieldAccess, ObjectFieldAccess, or ReferenceTypeName.  Note that a
     * ReferenceTypeName is not an Expression; its use should be detected and handled by callers.
     */
    private Node resolveAmbiguousName(AmbiguousName node) {
      Iterator<IdentifierToken> ids = node.getIdentifiers().iterator();
      IdentifierToken first = ids.next();
      Expression resultExp = null;
      
      if (context.localVariableExists(first.image(), ts)) {
        resultExp = new VariableAccess(first.image(), first.getSourceInfo()); 
      }
      else if (context.fieldExists(first.image(), ts)) {
        resultExp = new SimpleFieldAccess(first.image(), first.getSourceInfo());
      }
      else {
        // Try to match a class
        IdentifierToken last = first;
        String className = first.image();
        List<IdentifierToken> classIds = new LinkedList<IdentifierToken>();
        classIds.add(first);
        Type classType;
        
        while (!context.typeExists(className, ts)) {
          if (!ids.hasNext()) {
            setErrorStrings(node, className);
            throw new ExecutionError("undefined.name", node);
          }
          last = ids.next();
          className += "." + last.image();
          classIds.add(last);
        }
        try {
          DJClass c = context.getTopLevelClass(className, ts);
          if (c != null) { classType = ts.makeClassType(c); }
          else {
            classType = context.getTypeVariable(className, ts);
            if (classType == null) {
              // must be a member class (note that a simple name "Foo" can reference a member class)
              Type outer = context.typeContainingMemberClass(className, ts);
              classType = ts.lookupStaticClass(outer, className, IterUtil.<Type>empty());
              // TODO: Improve error when memberName is a non-static class
            }
          }
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
        catch (InvalidTargetException e) { throw new RuntimeException("context produced bad type"); }
        catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
        catch (UnmatchedLookupException e) {
          if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
          else { throw new ExecutionError("ambiguous.name", node); }
        }
        
        // Append member names until a field is encountered (or until all names are used up)
        while (ids.hasNext() && resultExp == null) {
          IdentifierToken memberName = ids.next();
          if (ts.containsField(classType, memberName.image())) {
            ReferenceTypeName rt = new ReferenceTypeName(classIds, SourceInfo.span(first, last)); 
            resultExp = new StaticFieldAccess(rt, memberName.image(), SourceInfo.span(first, memberName)); 
          }
          else if (ts.containsClass(classType, memberName.image())) {
            last = memberName;
            className += "." + last.image();
            classIds.add(last);
            try {
              classType = ts.lookupStaticClass(classType, memberName.image(), IterUtil.<Type>empty());
            }
            catch (InvalidTargetException e) { throw new RuntimeException("ts.containsClass lied"); }
            catch (InvalidTypeArgumentException e) { throw new ExecutionError("type.argument.arity", node); }
            catch (UnmatchedLookupException e) {
              if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
              else { throw new ExecutionError("ambiguous.name", node); }
            }
            // TODO: Improve error when memberName is a non-static class
          }
          else {
            setErrorStrings(node, ts.userRepresentation(classType), memberName.image());
            throw new ExecutionError("no.such.member", node);
          }
        }
        
        if (resultExp == null) { // there must be no more tokens; the name is the name of a class
          return new ReferenceTypeName(classIds, SourceInfo.span(first, last));
        }
      }
      
      // resultExp is now guaranteed to be defined; append any additional identifiers as field accesses
      while (ids.hasNext()) {
        IdentifierToken field = ids.next();
        resultExp = new ObjectFieldAccess(resultExp, field.image(), SourceInfo.span(first, field));
      }
      return resultExp;
    }
    
    
    /**
     * Visits a Literal
     * @return  The type of the expression
     */
    @Override public Type visit(Literal node) {
      setValue(node, node.getValue());
      if (node instanceof NullLiteral) { return setType(node, TypeSystem.NULL); }
      else if (node instanceof StringLiteral) { return setType(node, TypeSystem.STRING); }
      else { return setType(node, SymbolUtil.typeOfPrimitiveClass(node.getType())); }
    }
    
    /**
     * Visits a ThisExpression
     * @return  The type of this, according to the context.
     */
    @Override public Type visit(ThisExpression node) {
      String name = node.getClassName();
      DJClass thisC;
      if (name.equals("")) {
        thisC = context.getThis();
        if (thisC == null) { throw new ExecutionError("this.undefined", node); }
      }
      else {
        thisC = context.getThis(name);
        if (thisC == null) {
          setErrorStrings(node, name);
          throw new ExecutionError("undefined.class", node);
        }
      }
      setDJClass(node, thisC);
      return setType(node, SymbolUtil.thisType(thisC));
    }
    
    /**
     * Visits a VariableAccess
     * @return  The type of the expression
     */
    @Override public Type visit(VariableAccess node) {
      LocalVariable v = context.getLocalVariable(node.getVariableName(), ts);
      setVariable(node, v);
      setVariableType(node, v.type());
      return setType(node, ts.capture(v.type()));
    }
    
    /**
     * Visits a SimpleFieldAccess
     * @return  The type of the expression
     */
    @Override public Type visit(SimpleFieldAccess node) {
      try {
        ClassType t = context.typeContainingField(node.getFieldName(), ts);
        if (t == null) {
          setErrorStrings(node, node.getFieldName());
          throw new ExecutionError("undefined.name", node);
        }
        FieldReference ref;
        if (context.getThis() == null) {
          ref = ts.lookupStaticField(t, node.getFieldName());
        }
        else {
          Expression obj = TypeUtil.makeEmptyExpression(node);
          setType(obj, t);
          ref = ts.lookupField(obj, node.getFieldName());
        }
        
        // TODO: Check accessibility of field
        setField(node, ref.field());
        setVariableType(node, ref.type());
        if (!ref.field().isStatic()) {
          setDJClass(node, t.ofClass());
        }
        Type result = ts.capture(ref.type());
        addRuntimeCheck(node, result, ref.field().type());
        return setType(node, result);
      }
      catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
      catch (InvalidTargetException e) { throw new RuntimeException("context produced bad type"); }
      catch (UnmatchedLookupException e) {
        if (e.matches() == 0) { throw new ExecutionError("undefined.name.noinfo", node); }
        else { throw new ExecutionError("ambiguous.name", node); }
      }
    }
    
    /**
     * Visits an ObjectFieldAccess
     * @return  The type of the expression
     */
    @Override public Type visit(ObjectFieldAccess node) {
      Expression receiver = node.getExpression();
      if (receiver instanceof AmbiguousName) {
        Node resolved = resolveAmbiguousName((AmbiguousName) receiver);
        if (resolved instanceof ReferenceTypeName) {
          // this is actually a StaticFieldAccess
          Expression translation =
            new StaticFieldAccess((ReferenceTypeName) resolved, node.getFieldName(), node.getSourceInfo());
          translation.acceptVisitor(this);
          setTranslation(node, translation);
          setVariableType(node, getVariableType(translation));
          return setType(node, getType(translation));
        }
        else { receiver = (Expression) resolved; }
      }
      
      Type receiverT = check(receiver);
      try {
        ObjectFieldReference ref = ts.lookupField(receiver, node.getFieldName());
        node.setExpression(ref.object());
        // TODO: Check accessibility of field
        setField(node, ref.field());
        setVariableType(node, ref.type());
        Type result = ts.capture(ref.type());
        addRuntimeCheck(node, result, ref.field().type());
        return setType(node, result);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(receiverT), node.getFieldName());
        throw new ExecutionError("no.such.field", node);
      }
    }
    
    /**
     * Visits a SuperFieldAccess
     * @return  The type of the expression
     */
    @Override public Type visit(SuperFieldAccess node) {
      Type t = context.getSuperType(ts);
      if (t == null) {
        throw new ExecutionError("super.undefined", node);
      }
      Expression obj = TypeUtil.makeEmptyExpression(node);
      setType(obj, t);
      try {
        FieldReference ref = ts.lookupField(obj, node.getFieldName());
        // TODO: Check accessibility of field
        setField(node, ref.field());
        setDJClass(node, context.getThis());
        setVariableType(node, ref.type());
        Type result = ts.capture(ref.type());
        addRuntimeCheck(node, result, ref.field().type());
        return setType(node, result);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), node.getFieldName());
        throw new ExecutionError("no.such.field", node);
      }
    }
    
    /**
     * Visits a StaticFieldAccess
     * @return  The type of the expression
     */
    @Override public Type visit(StaticFieldAccess node) {
      Type t = checkTypeName(node.getFieldType());
      try {
        FieldReference ref = ts.lookupStaticField(t, node.getFieldName());
        // TODO: Check accessibility of field
        setField(node, ref.field());
        setVariableType(node, ref.type());
        Type result = ts.capture(ref.type());
        addRuntimeCheck(node, result, ref.field().type());
        return setType(node, result);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), node.getFieldName());
        throw new ExecutionError("no.such.field", node);
      }
    }
    
    /**
     * Visits a SimpleMethodCall
     * @return  The type of the expression
     */
    @Override public Type visit(SimpleMethodCall node) {
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      
      ClassType t;
      if (context.localFunctionExists(node.getMethodName(), ts)) {
        Iterable<LocalFunction> matches = context.getLocalFunctions(node.getMethodName(), ts);
        t = ts.makeClassType(new FunctionWrapperClass(context.getPackage(), matches));
      }
      else {
        try {
          t = context.typeContainingMethod(node.getMethodName(), ts);
          if (t == null) {
            setErrorStrings(node, node.getMethodName());
            throw new ExecutionError("undefined.name", node);
          }
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
      }
      
      try {
        MethodInvocation inv;
        if (context.getThis() == null) {
          inv = ts.lookupStaticMethod(t, node.getMethodName(), targs, args, expected);
        }
        else {
          Expression obj = TypeUtil.makeEmptyExpression(node);
          setType(obj, t);
          inv = ts.lookupMethod(obj, node.getMethodName(), targs, args, expected);
        }
        
        // TODO: Check accessibility of method
        checkThrownExceptions(inv.thrown(), node);
        node.setArguments(CollectUtil.makeList(inv.args()));
        setMethod(node, inv.method());
        if (!inv.method().isStatic()) {
          setDJClass(node, t.ofClass());
        }
        Type result = ts.capture(inv.returnType());
        debug.logValue("Type of method call " + node.getMethodName(), ts.wrap(result));
        addRuntimeCheck(node, result, inv.method().returnType());
        return setType(node, result);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), node.getMethodName(), nodeTypesString(args));
        throw new ExecutionError("no.such.method", node);
      }
    }
    
    /**
     * Visits an ObjectMethodCall
     * @return  The type of the expression
     */
    @Override public Type visit(ObjectMethodCall node) {
      Expression receiver = node.getExpression();
      if (receiver instanceof AmbiguousName) {
        Node resolved = resolveAmbiguousName((AmbiguousName) receiver);
        if (resolved instanceof ReferenceTypeName) {
          // this is actually a StaticMethodCall
          Expression translation;
          if (node instanceof PolymorphicObjectMethodCall) {
            translation =
              new PolymorphicStaticMethodCall((ReferenceTypeName) resolved, node.getMethodName(), node.getArguments(),
                                              ((PolymorphicObjectMethodCall) node).getTypeArguments(),
                                              node.getSourceInfo());
          }
          else {
            translation = new StaticMethodCall((ReferenceTypeName) resolved, node.getMethodName(),
                                               node.getArguments(), node.getSourceInfo());
          }
          translation.acceptVisitor(this);
          setTranslation(node, translation);
          return setType(node, getType(translation));
        }
        else { receiver = (Expression) resolved; }
      }
      
      Type receiverT = check(receiver);
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      if (node instanceof PolymorphicObjectMethodCall) {
        targs = checkTypeNameList(((PolymorphicObjectMethodCall) node).getTypeArguments());
      }
      
      try {
        // Note: Changes made below may also need to be made in the TypeSystem's boxing & unboxing implementations
        ObjectMethodInvocation inv = ts.lookupMethod(receiver, node.getMethodName(), targs, args, expected);
        // TODO: Check accessibility of method
        checkThrownExceptions(inv.thrown(), node);
        node.setExpression(inv.object());
        node.setArguments(CollectUtil.makeList(inv.args()));
        setMethod(node, inv.method());
        Type result = ts.capture(inv.returnType());
        debug.logValue("Type of method call " + node.getMethodName(), ts.wrap(result));
        addRuntimeCheck(node, result, inv.method().returnType());
        return setType(node, result);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(receiverT), node.getMethodName(), nodeTypesString(args));
        throw new ExecutionError("no.such.method", node);
      }
    }
    
    /**
     * Visits a SuperMethodCall
     * @return The type of the expression
     */
    @Override public Type visit(SuperMethodCall node) {
      Type t = context.getSuperType(ts);
      if (t == null) {
        throw new ExecutionError("super.undefined", node);
      }
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      if (node instanceof PolymorphicSuperMethodCall) {
        targs = checkTypeNameList(((PolymorphicSuperMethodCall) node).getTypeArguments());
      }
      
      Expression obj = TypeUtil.makeEmptyExpression(node);
      setType(obj, t);
      try {
        MethodInvocation inv = ts.lookupMethod(obj, node.getMethodName(), targs, args, expected);
        // TODO: Check accessibility of method
        checkThrownExceptions(inv.thrown(), node);
        node.setArguments(CollectUtil.makeList(inv.args()));
        setMethod(node, inv.method());
        setDJClass(node, context.getThis());
        Type result = ts.capture(inv.returnType());
        debug.logValue("Type of method call " + node.getMethodName(), ts.wrap(result));
        addRuntimeCheck(node, result, inv.method().returnType());
        return setType(node, result);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), node.getMethodName(), nodeTypesString(args));
        throw new ExecutionError("no.such.method", node);
      }
    }
    
    /**
     * Visits a StaticMethodCall
     * @return  The type of the expression
     */
    @Override public Type visit(StaticMethodCall node) {
      Type t = checkTypeName(node.getMethodType());
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      if (node instanceof PolymorphicStaticMethodCall) {
        targs = checkTypeNameList(((PolymorphicStaticMethodCall) node).getTypeArguments());
      }
      
      try {
        // Note: Changes made below may also need to be made in the TypeSystem's boxing & unboxing implementations
        MethodInvocation inv = ts.lookupStaticMethod(t, node.getMethodName(), targs, args, expected);
        // TODO: Check accessibility of method
        checkThrownExceptions(inv.thrown(), node);
        node.setArguments(CollectUtil.makeList(inv.args()));
        setMethod(node, inv.method());
        Type result = ts.capture(inv.returnType());
        debug.logValue("Type of method call " + node.getMethodName(), ts.wrap(result));
        addRuntimeCheck(node, result, inv.method().returnType());
        return setType(node, result);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), node.getMethodName(), nodeTypesString(args));
        throw new ExecutionError("no.such.method", node);
      }
    }
    
    private void addRuntimeCheck(Node node, Type expectedType, Type declaredActualType) {
      if (!ts.isSubtype(ts.erase(declaredActualType), ts.erase(expectedType))) {
        Thunk<Class<?>> erasedExpectedType = ts.erasedClass(expectedType);
        if (erasedExpectedType != null) { setCheckedType(node, erasedExpectedType); }
      }
    }
    
    /** Visits an ArrayAllocation.  JLS 15.10.
      * @return  The type of the array
      */
    @Override public Type visit(ArrayAllocation node) {
      Type elementType = checkTypeName(node.getElementType());
      if (! ts.isReifiable(elementType)) {
        throw new ExecutionError("reifiable.type", node);
      }
      Type result = elementType;
      for (int i = 0; i < node.getDimension(); i++) {
        result = new SimpleArrayType(result);
      }
      
      checkList(node.getSizes(), TypeSystem.INT);
      List<Expression> newSizes = new ArrayList<Expression>(node.getSizes().size());
      for (Expression exp : node.getSizes()) {
        try {
          Expression newExp = ts.unaryPromote(ts.makePrimitive(exp));
          if (!(getType(newExp) instanceof IntType)) {
            throw new ExecutionError("array.dimension.type", node);
          }
          newSizes.add(newExp);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("array.dimension.type", node);
        }
      }
      node.setSizes(newSizes);
      
      if (node.getInitialization() != null) { check(node.getInitialization(), result); }
      
      setErasedType(node, ts.erasedClass(result));
      return setType(node, result);
    }
    
    /**
     * Visits a ArrayInitializer.  JLS 10.6.
     * @return  The type of the initialized array
     */
    @Override public Type visit(ArrayInitializer node) {
      // there's some redundancy between "expected" and "node.getElementType()"
      // but while "expected" is a suggestion that can lead to no errors
      // (and that can be turned on or off depending on preferences),
      // the stated element type is always required and checked
      // TODO: Store the *Type* as an attribute on the initializer, instead of a *TypeName*?
      Type elementType = checkTypeName(node.getElementType());
      if (expected.isSome() && ts.isArray(expected.unwrap())) {
        checkList(node.getCells(), ts.arrayElementType(expected.unwrap()));
      }
      else {
        checkList(node.getCells());
      }
      List<Expression> newCells = new ArrayList<Expression>(node.getCells().size());
      for (Expression exp : node.getCells()) {
        try { newCells.add(ts.assign(elementType, exp)); }
        catch (UnsupportedConversionException e) {
          Type expT = getType(exp);
          setErrorStrings(exp, ts.userRepresentation(expT), ts.userRepresentation(elementType));
          throw new ExecutionError("assignment.types", exp);
        }
      }
      node.setCells(newCells);
      Type result = new SimpleArrayType(elementType);
      setErasedType(node, ts.erasedClass(result));
      return setType(node, result);
    }
    
    /**
     * Visits a SimpleAllocation.  JLS 15.10.
     * @return  The type of the expression
     */
    @Override public Type visit(SimpleAllocation node) {
      Type t = checkTypeName(node.getCreationType());
      // TODO: Allow a simple allocation of a dynamic inner class defined in the current context
      //       (where "new Inner()" is the equivalent of "this.new Inner()" or "SomeOuter.this.new Inner()")
      if (!ts.isConcrete(t) || !ts.isStatic(t)) {
        throw new ExecutionError("allocation.type", node);
      }
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      if (node instanceof PolymorphicSimpleAllocation) {
        targs = checkTypeNameList(((PolymorphicSimpleAllocation) node).getTypeArguments());
      }
      
      try {
        ConstructorInvocation inv = ts.lookupConstructor(t, targs, args, expected);
        // TODO: Check accessibility of constructor
        checkThrownExceptions(inv.thrown(), node);
        node.setArguments(CollectUtil.makeList(inv.args()));
        setConstructor(node, inv.constructor());
        return setType(node, t);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(t), nodeTypesString(args));
        throw new ExecutionError("no.such.constructor", node);
      }
    }
    
    /**
     * Visits an AnonymousAllocation.  JLS 15.9.
     * @return  The type of the expression
     */
    @Override public Type visit(AnonymousAllocation node) {
      Type t = checkTypeName(node.getCreationType());
      // TODO: Allow a simple allocation of a dynamic inner class defined in the current context (as above)
      if (!ts.isStatic(t) || (!ts.isExtendable(t) && !ts.isImplementable(t))) {
        throw new ExecutionError("allocation.type", node);
      }
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      Iterable<Type> targs = IterUtil.empty();
      if (node instanceof PolymorphicAnonymousAllocation) {
        targs = checkTypeNameList(((PolymorphicAnonymousAllocation) node).getTypeArguments());
      }
      
      if (!(IterUtil.isEmpty(args) && IterUtil.isEmpty(targs) && ts.isImplementable(t))) {
        // Super constructor invocation is something besides Object()
        try {
          ConstructorInvocation inv = ts.lookupConstructor(t, targs, args, expected);
          // TODO: Check accessibility of constructor
          checkThrownExceptions(inv.thrown(), node);
          node.setArguments(CollectUtil.makeList(inv.args()));
        }
        catch (InvalidTypeArgumentException e) {
          throw new ExecutionError("type.argument", node);
        }
        catch (TypeSystemException e) {
          setErrorStrings(node, ts.userRepresentation(t), nodeTypesString(args));
          throw new ExecutionError("no.such.constructor", node);
        }
      }
      
      TreeClass c = new TreeClass(context.makeAnonymousClassName(), null, node,
                                  new TreeClassLoader(context.getClassLoader(), opt), opt);
      setDJClass(node, c);
      new ClassMemberChecker(new ClassContext(context, c), opt).checkClassMembers(node.getMembers());
      
      setConstructor(node, IterUtil.first(c.declaredConstructors()));
      return setType(node, ts.makeClassType(c));
    }
    
    /**
     * Visits a InnerAllocation.  JLS 15.9.
     * @return  The type of the expression
     */
    @Override public Type visit(InnerAllocation node) {
      Type enclosing = check(node.getExpression());
      
      Iterable<Type> classTargs = IterUtil.empty();
      if (node.getClassTypeArguments() != null) {
        classTargs = checkTypeNameList(node.getClassTypeArguments());
      }
      
      try {
        Type t = ts.lookupClass(node.getExpression(), node.getClassName(), classTargs);
        // TODO: Check that t is not a static member of enclosing
        if (!ts.isConcrete(t)) {
          throw new ExecutionError("allocation.type", node);
        }
        
        Iterable<? extends Expression> args = IterUtil.empty();
        if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
        
        Iterable<Type> targs = IterUtil.empty();
        if (node instanceof PolymorphicInnerAllocation) {
          targs = checkTypeNameList(((PolymorphicInnerAllocation) node).getTypeArguments());
        }
        
        try {
          ConstructorInvocation inv = ts.lookupConstructor(t, targs, args, expected);
          // TODO: Check accessibility of constructor
          checkThrownExceptions(inv.thrown(), node);
          node.setArguments(CollectUtil.makeList(inv.args()));
          setConstructor(node, inv.constructor());
          return setType(node, t);
        }
        catch (InvalidTypeArgumentException e) {
          throw new ExecutionError("type.argument", node);
        }
        catch (TypeSystemException e) {
          setErrorStrings(node, ts.userRepresentation(t), nodeTypesString(args));
          throw new ExecutionError("no.such.constructor", node);
        }
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(enclosing), node.getClassName());
        throw new ExecutionError("no.such.inner.class", node);
      }
    }
    
    /**
     * Visits an AnonymousInnerAllocation.  JLS 15.9.
     * @return  The type of the expression
     */
    @Override public Type visit(AnonymousInnerAllocation node) {
      Type enclosing = check(node.getExpression());
      
      Iterable<Type> classTargs = IterUtil.empty();
      if (node.getClassTypeArguments() != null) {
        classTargs = checkTypeNameList(node.getClassTypeArguments());
      }
      
      try {
        Type t = ts.lookupClass(node.getExpression(), node.getClassName(), classTargs);
        // TODO: Check that t is not a static member of enclosing
        if (!ts.isExtendable(t)) {
          throw new ExecutionError("allocation.type", node);
        }
        setSuperType(node, t);
        
        Iterable<? extends Expression> args = IterUtil.empty();
        if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
        
        Iterable<Type> targs = IterUtil.empty();
        if (node instanceof PolymorphicAnonymousInnerAllocation) {
          targs = checkTypeNameList(((PolymorphicAnonymousInnerAllocation) node).getTypeArguments());
        }
        
        try {
          ConstructorInvocation inv = ts.lookupConstructor(t, targs, args, expected);
          // TODO: Check accessibility of constructor
          checkThrownExceptions(inv.thrown(), node);
          node.setArguments(CollectUtil.makeList(inv.args()));
        }
        catch (InvalidTypeArgumentException e) {
          throw new ExecutionError("type.argument", node);
        }
        catch (TypeSystemException e) {
          setErrorStrings(node, ts.userRepresentation(t), nodeTypesString(args));
          throw new ExecutionError("no.such.constructor", node);
        }
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(enclosing), node.getClassName());
        throw new ExecutionError("no.such.inner.class", node);
      }
      
      TreeClass c = new TreeClass(context.makeAnonymousClassName(), null, node,
                                  new TreeClassLoader(context.getClassLoader(), opt), opt);
      setDJClass(node, c);
      new ClassMemberChecker(new ClassContext(context, c), opt).checkClassMembers(node.getMembers());
      
      setConstructor(node, IterUtil.first(c.declaredConstructors()));
      return setType(node, ts.makeClassType(c));
    }
    
    /**
     * Visits a ConstructorCall.
     * @return  The type of this or super.
     */
    @Override public Type visit(ConstructorCall node) {
      if (node.getExpression() != null) {
        throw new ExecutionError("not.implemented", node);
      }
      
      Iterable<? extends Expression> args = IterUtil.empty();
      if (node.getArguments() != null) { args = node.getArguments(); checkList(args); }
      
      // TODO: implement explict type arguments in constructor calls
      Iterable<Type> targs = IterUtil.empty();
      
      Type result;
      if (node.isSuper()) { result = context.getSuperType(ts); }
      else { result = SymbolUtil.thisType(context.getThis()); }
      if (result == null) {
        throw new IllegalArgumentException("Can't check a ConstructorCall in this context");
      }
      
      try {
        ConstructorInvocation inv = ts.lookupConstructor(result, targs, args, expected);
        
        // TODO: Check accessibility of constructor
        // Note that super constructor calls *have to* be accessible, even if accessibility
        // checking is turned off -- a call to a private constructor cannot be compiled
        // in a way that it will run successfully (since constructor calls are the only code
        // that is directly compiled rather than being interpreted, we don't have this problem
        // elsewhere)
        checkThrownExceptions(inv.thrown(), node);
        node.setArguments(CollectUtil.makeList(inv.args()));
        setConstructor(node, inv.constructor());
        return setType(node, result);
      }
      catch (InvalidTypeArgumentException e) {
        throw new ExecutionError("type.argument", node);
      }
      catch (TypeSystemException e) {
        setErrorStrings(node, ts.userRepresentation(result), nodeTypesString(args));
        throw new ExecutionError("no.such.constructor", node);
      }
    }
    
    /** Verify that the thrown exceptions are expected in this context. */
    private void checkThrownExceptions(Iterable<? extends Type> thrownTypes, Node node) {
      Iterable<Type> allowed = IterUtil.compose(TypeSystem.RUNTIME_EXCEPTION,
                                                context.getDeclaredThrownTypes());
      for (Type thrown : thrownTypes) {
        if (ts.isAssignable(TypeSystem.EXCEPTION, thrown)) {
          boolean valid = false;
          for (Type t : allowed) {
            if (ts.isAssignable(t, thrown)) { valid = true; break; }
          }
          if (!valid) {
            setErrorStrings(node, ts.userRepresentation(thrown));
            throw new ExecutionError("uncaught.exception", node);
          }
        }
      }
    }
    
    
    
    /**
     * Visits an ArrayAccess.  JLS 15.13.
     * @return  The type of the expression
     */
    @Override public Type visit(ArrayAccess node) {
      Type arrayType = check(node.getExpression());
      if (!ts.isArray(arrayType)) {
        setErrorStrings(node, ts.userRepresentation(arrayType));
        throw new ExecutionError("array.required", node);
      }
      Type elementType = ts.arrayElementType(arrayType);
      
      check(node.getCellNumber(), TypeSystem.INT);
      try {
        Expression cell = ts.unaryPromote(ts.makePrimitive(node.getCellNumber()));
        if (!(getType(cell) instanceof IntType)) {
          throw new ExecutionError("array.index.type", node);
        }
        node.setCellNumber(cell);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("array.index.type", node);
      }
      
      setVariableType(node, elementType);
      return setType(node, ts.capture(elementType));
      // TODO: Does there need to be a runtime check here, as in field accesses?
    }
    
    /**
     * Visits a TypeExpression ({@code Foo.class}).  JLS 15.8.2.
     * @return  The type of the expression
     */
    @Override public Type visit(TypeExpression node) {
      Type t = checkTypeName(node.getType());
      setErasedType(node.getType(), ts.erasedClass(t));
      
      Type targ = t;
      if (ts.isEqual(t, TypeSystem.VOID)) { targ = TypeSystem.VOID_CLASS; }
      else if (!ts.isReference(t)) {
        Expression pseudoExp = TypeUtil.makeEmptyExpression(node.getType());
        setType(pseudoExp, t);
        try {
          Expression boxedPseudoExp = ts.makeReference(pseudoExp);
          targ = getType(boxedPseudoExp);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("reference.type", node);
        }
      }
      return setType(node, ts.reflectionClassOf(targ));
    }
    
    /**
     * Visits a NotExpression ({@code !}).  JLS 15.15.6.
     * @return  The type of the expression
     */
    @Override public Type visit(NotExpression node) {
      check(node.getExpression(), TypeSystem.BOOLEAN);
      try {
        Expression exp = ts.makePrimitive(node.getExpression());
        if (!(getType(exp) instanceof BooleanType)) {
          throw new ExecutionError("not.expression.type", node);
        }
        node.setExpression(exp);
        return setType(node, getType(exp));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("not.expression.type", node);
      }
    }
    
    /**
     * Visits a ComplementExpression ({@code ~}).  JLS 15.15.5.
     * @return  The type of the expression
     */
    @Override public Type visit(ComplementExpression node) {
      check(node.getExpression());
      try {
        Expression exp = ts.unaryPromote(ts.makePrimitive(node.getExpression()));
        if (!(getType(exp) instanceof IntegralType)) {
          throw new ExecutionError("complement.expression.type", node);
        }
        node.setExpression(exp);
        return setType(node, getType(exp));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("complement.expression.type", node);
      }
    }
    
    @Override public Type visit(PlusExpression node) { return handleNumericUnaryExpression(node); }
    
    @Override public Type visit(MinusExpression node) { return handleNumericUnaryExpression(node); }
    
    /**
     * Handles a numeric unary operation ({@code +, -}).  JLS 15.15.3, 15.15.4.
     * @return  The type of the expression
     */
    private Type handleNumericUnaryExpression(UnaryExpression node) {
      check(node.getExpression());
      try {
        Expression exp = ts.unaryPromote(ts.makePrimitive(node.getExpression()));
        node.setExpression(exp);
        return setType(node, getType(exp));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("numeric.expression.type", node);
      }
    }
    
    /**
     * Visits an AddExpression.  JLS 15.18.
     * @return  The type of the expression
     */
    @Override public Type visit(AddExpression node) {
      Type leftT = check(node.getLeftExpression());
      Type rightT = check(node.getRightExpression());
      if (ts.isSubtype(leftT, TypeSystem.STRING) || ts.isSubtype(rightT, TypeSystem.STRING)) {
        try {
          Expression left = ts.makeReference(node.getLeftExpression());
          Expression right = ts.makeReference(node.getRightExpression());
          node.setLeftExpression(left);
          node.setRightExpression(right);
          setOperation(node, ExpressionEvaluator.CONCATENATE);
          return setType(node, TypeSystem.STRING);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("addition.type", node);
        }
      }
      else {
        try {
          Expression left = ts.makePrimitive(node.getLeftExpression());
          Expression right = ts.makePrimitive(node.getRightExpression());
          Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
          node.setLeftExpression(promoted.first());
          node.setRightExpression(promoted.second());
          setOperation(node, ExpressionEvaluator.ADD);
          return setType(node, getType(promoted.first()));
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("addition.type", node);
        }
      }
    }
    
    /**
     * Visits an AddAssignExpression.  JLS 15.26.2.
     * @return  The type of the expression
     */
    @Override public Type visit(AddAssignExpression node) { 
      Type leftT = check(node.getLeftExpression());
      Type rightT = check(node.getRightExpression());
      if (ts.isEqual(leftT, TypeSystem.STRING)) {
        try {
          Expression right = ts.makeReference(node.getRightExpression());
          node.setRightExpression(right);
          setOperation(node, ExpressionEvaluator.CONCATENATE);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("addition.type", node);
        }
      }
      else if (ts.isSubtype(leftT, TypeSystem.STRING) || ts.isSubtype(rightT, TypeSystem.STRING)) {
        throw new ExecutionError("addition.type", node);
      }
      else {
        try {
          Expression left = ts.makePrimitive(node.getLeftExpression());
          Expression right = ts.makePrimitive(node.getRightExpression());
          Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
          setLeftExpression(node, promoted.first()); // not to be confused with node.setLeftExpression(...)
          node.setRightExpression(promoted.second());
          setOperation(node, ExpressionEvaluator.ADD);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("addition.type", node);
        }
      }
      
      if (!hasVariableType(node.getLeftExpression())) {
        throw new ExecutionError("addition.type", node);
      }
      
      return setType(node, leftT);
    }
    
    @Override public Type visit(SubtractExpression node) {  return handleNumericExpression(node); }
    
    @Override public Type visit(MultiplyExpression node) {  return handleNumericExpression(node); }
    
    @Override public Type visit(DivideExpression node) {  return handleNumericExpression(node); }
    
    @Override public Type visit(RemainderExpression node) {  return handleNumericExpression(node); }
    
    /**
     * Handles a numeric binary expression ({@code -, *, /, %}).  JLS 15.17, 15.18.
     * @return  The type of the expression
     */
    private Type handleNumericExpression(BinaryExpression node) {
      check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());
        Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
        node.setLeftExpression(promoted.first());
        node.setRightExpression(promoted.second());
        return setType(node, getType(promoted.first()));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("numeric.expression.type", node);
      }
    }
    
    @Override public Type visit(SubtractAssignExpression node) { return handleNumericAssignmentExpression(node); }
    
    @Override public Type visit(MultiplyAssignExpression node) {  return handleNumericAssignmentExpression(node); }
    
    @Override public Type visit(DivideAssignExpression node) {  return handleNumericAssignmentExpression(node); }
    
    @Override public Type visit(RemainderAssignExpression node) { return handleNumericAssignmentExpression(node); }
    
    /**
     * Handles a numeric assignment expression ({@code -=, *=, /=, %=}).  JLS 15.26.2.
     * @return  The type of the expression
     */
    private Type handleNumericAssignmentExpression(BinaryExpression node) {
      Type result = check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());
        Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
        
        if (!hasVariableType(node.getLeftExpression())) {
          throw new ExecutionError("numeric.expression.type", node);
        }
        setLeftExpression(node, promoted.first()); // not to be confused with node.setLeftExpression(...)
        node.setRightExpression(promoted.second());
        return setType(node, result);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("numeric.expression.type", node);
      }
    }
    
    @Override public Type visit(EqualExpression node) {
      return handleEqualityExpression(node, ExpressionEvaluator.OBJECT_EQUAL, 
                                      ExpressionEvaluator.PRIMITIVE_EQUAL);
    }
    
    @Override public Type visit(NotEqualExpression node) {
      return handleEqualityExpression(node, ExpressionEvaluator.OBJECT_NOT_EQUAL,
                                      ExpressionEvaluator.PRIMITIVE_NOT_EQUAL);
    }
    
    /**
     * Handles an equality expression ({@code ==, !=}).  JLS 15.21.
     * @return  The type of the expression
     */
    private Type handleEqualityExpression(BinaryExpression node, Lambda2<Object, Object, Object> objectCase,
                                          Lambda2<Object, Object, Object>  primitiveCase) {
      Type leftT = check(node.getLeftExpression());
      Type rightT = check(node.getRightExpression());
      if (ts.isReference(leftT) && ts.isReference(rightT)) {
        if (!ts.isCastable(leftT, rightT) && !ts.isCastable(rightT, leftT)) {
          throw new ExecutionError("compare.type", node);
        }
        setOperation(node, objectCase);
      }
      else {
        try {
          Expression left = ts.makePrimitive(node.getLeftExpression());
          Expression right = ts.makePrimitive(node.getRightExpression());
          
          if (getType(left) instanceof BooleanType && getType(right) instanceof BooleanType) {
            node.setLeftExpression(left);
            node.setRightExpression(right);
          }
          else if (getType(left) instanceof NumericType && getType(right) instanceof NumericType) {
            Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
            left = promoted.first();
            right = promoted.second();
            node.setLeftExpression(promoted.first());
            node.setRightExpression(promoted.second());
          }
          else {
            throw new ExecutionError("compare.type", node);
          }
          setOperation(node, primitiveCase);
        }
        catch (UnsupportedConversionException e) {
          throw new ExecutionError("compare.type", node);
        }
      }
      return setType(node, TypeSystem.BOOLEAN);
    }
    
    @Override public Type visit(LessExpression node) { return handleRelationalExpression(node); }
    
    @Override public Type visit(LessOrEqualExpression node) { return handleRelationalExpression(node); }
    
    @Override public Type visit(GreaterExpression node) { return handleRelationalExpression(node); }
    
    @Override public Type visit(GreaterOrEqualExpression node) { return handleRelationalExpression(node); }
    
    /**
     * Handles a relational expression ({@code <, <=, >, >=}).  JLS 15.20.1.
     * @return  The type of the expression
     */
    private Type handleRelationalExpression(BinaryExpression node) {
      check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());
        Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
        node.setLeftExpression(promoted.first());
        node.setRightExpression(promoted.second());
        return setType(node, TypeSystem.BOOLEAN);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("compare.type", node);
      }
    }
    
    @Override public Type visit(BitAndExpression node) { return handleBitwiseExpression(node); }
    
    @Override public Type visit(BitOrExpression node) { return handleBitwiseExpression(node); }
    
    @Override public Type visit(ExclusiveOrExpression node) { return handleBitwiseExpression(node); }
    
    /**
     * Handles a bitwise expression ({@code |, &, ^}).  JLS 15.22.
     * @return  The type of the expression
     */
    private Type handleBitwiseExpression(BinaryExpression node) {
      check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());
        
        if (getType(left) instanceof BooleanType && getType(right) instanceof BooleanType) {
          // Do nothing
        }
        else if (getType(left) instanceof IntegralType && getType(right) instanceof IntegralType) {
          Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
          left = promoted.first();
          right = promoted.second();
        }
        else {
          throw new ExecutionError("bitwise.expression.type", node);
        }
        
        node.setLeftExpression(left);
        node.setRightExpression(right);
        
        return setType(node, getType(left));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("bitwise.expression.type", node);
      }
    }
    
    @Override public Type visit(BitAndAssignExpression node) { return handleBitwiseAssignmentExpression(node); }
    
    @Override public Type visit(BitOrAssignExpression node) { return handleBitwiseAssignmentExpression(node); }
    
    @Override public Type visit(ExclusiveOrAssignExpression node) { return handleBitwiseAssignmentExpression(node); }
    
    /**
     * Handles a bitwise assignment expression ({@code &=, |=, ^=}).  JLS 15.26.2.
     * @return  The type of the expression
     */
    private Type handleBitwiseAssignmentExpression(BinaryExpression node) {
      Type result = check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());
        if (getType(left) instanceof BooleanType && getType(right) instanceof BooleanType) {
          // Do nothing
        }
        else if (getType(left) instanceof IntegralType && getType(right) instanceof IntegralType) {
          Pair<Expression, Expression> promoted = ts.binaryPromote(left, right);
          left = promoted.first();
          right = promoted.second();
        }
        else {
          throw new ExecutionError("bitwise.expression.type", node);
        }
        
        if (!hasVariableType(node.getLeftExpression())) {
          throw new ExecutionError("bitwise.expression.type", node);
        }
        setLeftExpression(node, left); // not to be confused with node.setLeftExpression(...)
        node.setRightExpression(right);
        return setType(node, result);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("bitwise.expression.type", node);
      }
    }
    
    @Override public Type visit(ShiftLeftExpression node) { return handleShiftExpression(node); }
    
    @Override public Type visit(ShiftRightExpression node) { return handleShiftExpression(node); }
    
    @Override public Type visit(UnsignedShiftRightExpression node) { return handleShiftExpression(node); }
    
    /**
     * Handles a shift expression ({@code <<, >>, >>>}).  JLS 15.19.
     * @return  The type of the expression
     */
    private Type handleShiftExpression(BinaryExpression node) {
      check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.unaryPromote(ts.makePrimitive(node.getLeftExpression()));
        Expression right = ts.unaryPromote(ts.makePrimitive(node.getRightExpression()));
        node.setLeftExpression(left);
        node.setRightExpression(right);
        
        if (!(getType(left) instanceof IntegralType) || !(getType(right) instanceof IntegralType)) {
          throw new ExecutionError("shift.expression.type", node);
        }
        
        return setType(node, getType(left));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("shift.expression.type", node);
      }
    }
    
    @Override public Type visit(ShiftLeftAssignExpression node) { return handleShiftAssignmentExpression(node); }
    
    @Override public Type visit(ShiftRightAssignExpression node) { return handleShiftAssignmentExpression(node); }
    
    @Override public Type visit(UnsignedShiftRightAssignExpression node) { return handleShiftAssignmentExpression(node); }
    
    /**
     * Handles a shift assignment expression (<<=, >>=, >>>=).  JLS 15.26.2.
     * @return  The type of the expression
     */
    private Type handleShiftAssignmentExpression(BinaryExpression node) {
      Type result = check(node.getLeftExpression());
      check(node.getRightExpression());
      try {
        Expression left = ts.unaryPromote(ts.makePrimitive(node.getLeftExpression()));
        Expression right = ts.unaryPromote(ts.makePrimitive(node.getRightExpression()));
        
        if (!(getType(left) instanceof IntegralType) || !(getType(right) instanceof IntegralType) || 
            !hasVariableType(node.getLeftExpression())) {
          throw new ExecutionError("shift.expression.type", node);
        }
        
        setLeftExpression(node, left); // not to be confused with node.setLeftExpression(...)
        node.setRightExpression(right);
        return setType(node, result);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("shift.expression.type", node);
      }
    }
    
    @Override public Type visit(AndExpression node) { return handleBooleanExpression(node); }
    
    @Override public Type visit(OrExpression node) { return handleBooleanExpression(node); }
    
    /** 
     * Handles a boolean expression ({@code &&, ||}).  JLS 15.23, 15.24.
     * @return  The type of the expression
     */
    private Type handleBooleanExpression(BinaryExpression node) {
      check(node.getLeftExpression(), TypeSystem.BOOLEAN);
      check(node.getRightExpression(), TypeSystem.BOOLEAN);
      try {
        Expression left = ts.makePrimitive(node.getLeftExpression());
        Expression right = ts.makePrimitive(node.getRightExpression());    
        if (!(getType(left) instanceof BooleanType) || !(getType(right) instanceof BooleanType)) {
          throw new ExecutionError("boolean.expression.type", node);
        }
        node.setLeftExpression(left);
        node.setRightExpression(right);
        return setType(node, TypeSystem.BOOLEAN);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("boolean.expression.type", node);
      }
    }
    
    /**
     * Visits a InstanceOfExpression.  JLS 15.20.2.
     * @return  The type of the expression
     */
    @Override public Type visit(InstanceOfExpression node) {
      Type expT = check(node.getExpression());
      Type targetT = checkTypeName(node.getReferenceType());
      if (!ts.isReference(expT) || !ts.isReference(targetT) || !ts.isCastable(targetT, expT)) {
        throw new ExecutionError("instanceof.type", node);
      }
      if (!ts.isReifiable(targetT)) {
        throw new ExecutionError("reifiable.type", node);
      }
      setErasedType(node.getReferenceType(), ts.erasedClass(targetT));
      return setType(node, TypeSystem.BOOLEAN);
    }
    
    /**
     * Visits a ConditionalExpression.  JLS 15.25.
     * @return  The type of the expression
     */
    @Override public Type visit(ConditionalExpression node) {
      check(node.getConditionExpression(), TypeSystem.BOOLEAN);
      check(node.getIfTrueExpression(), expected);
      check(node.getIfFalseExpression(), expected);
      
      try {
        Expression cond = ts.makePrimitive(node.getConditionExpression());
        if (!(getType(cond) instanceof BooleanType)) {
          throw new ExecutionError("condition.type", node);
        }
        node.setConditionExpression(cond);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("condition.type", node);
      }
      
      try {
        Pair<Expression, Expression> joined = ts.join(node.getIfTrueExpression(),
                                                      node.getIfFalseExpression());
        node.setIfTrueExpression(joined.first());
        node.setIfFalseExpression(joined.second());
        return setType(node, ts.capture(getType(joined.first())));
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("conditional.type", node);
      }
    }
    
    /**
     * Visits a SimpleAssignExpression.  JLS 15.26.
     * @return  The type of the expression.
     */
    @Override public Type visit(SimpleAssignExpression node) {
      Expression left = node.getLeftExpression();
      Type result = check(left);

      if (!hasVariableType(left)) {
        throw new ExecutionError("left.expression", node);
      }
      if (hasVariable(left) && getVariable(left).isFinal() ||
          hasField(left) && getField(left).isFinal()) {
        throw new ExecutionError("cannot.modify", node);
      }
      
      Type target = getVariableType(left);
      Type rightT = check(node.getRightExpression(), target);
      try {
        Expression newRight = ts.assign(target, node.getRightExpression());
        node.setRightExpression(newRight);
        return setType(node, result);
      }
      catch (UnsupportedConversionException e) {
        setErrorStrings(node, ts.userRepresentation(rightT),
                        ts.userRepresentation(target));
        throw new ExecutionError("assignment.types", node);
      }
    }
    
    @Override public Type visit(PostIncrement node) { return handleIncrementExpression(node); }
    
    @Override public Type visit(PreIncrement node) { return handleIncrementExpression(node); }
    
    @Override public Type visit(PostDecrement node) { return handleIncrementExpression(node); }
    
    @Override public Type visit(PreDecrement node) { return handleIncrementExpression(node); }
    
    /**
     * Handles an increment expression ({@code ++, --}).  JLS 15.14, 15.15.1, 15.15.2.
     * @return  The type of the expression
     */
    private Type handleIncrementExpression(UnaryExpression node) {
      Type result = check(node.getExpression());
      try {
        Expression exp = ts.makePrimitive(node.getExpression());
        
        if (!(getType(exp) instanceof NumericType) || !hasVariableType(node.getExpression())) {
          throw new ExecutionError("increment.type", node);
        }
        
        setLeftExpression(node, exp);
        return setType(node, result);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("increment.type", node);
      }
    }
    
    /**
     * Visits a CastExpression.  JLS 15.16.
     * @return  The type of the expression
     */
    @Override public Type visit(CastExpression node) {
      Type t = checkTypeName(node.getTargetType());
      Type fromT = check(node.getExpression());
      try {
        Expression exp = ts.cast(t, node.getExpression());
        node.setExpression(exp);
        return setType(node, ts.capture(t));
      }
      catch (UnsupportedConversionException e) {
        setErrorStrings(node, ts.userRepresentation(fromT), ts.userRepresentation(t));
        throw new ExecutionError("cast.types", node);
      }
    }
    
  }
  
}
