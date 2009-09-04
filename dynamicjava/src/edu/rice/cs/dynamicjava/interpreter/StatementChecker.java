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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.lambda.Lambda;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.tiger.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.interpreter.TypeUtil;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.*;
import edu.rice.cs.dynamicjava.symbol.TypeSystem.*;

import static koala.dynamicjava.interpreter.NodeProperties.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Traverses the given statements and declarations, performing static checks along the way.
 * A variety of properties (from {@code NodeProperties}) are set on certain nodes.  In addition to
 * those documented in {@link ExpressionChecker}, the following are set:<ul>
 * <li>VARIABLE on all {@link VariableDeclaration}s and {@link FormalParameter}s</li>
 * <li>TYPE_VARIABLE on all {@link TypeParameter}s</li>
 * <li>ERASED_TYPE on all {@link CatchStatement}s, {@link VariableDeclaration}s, and
 *     {@link MethodDeclaration}s</li>
 * <li>DJClASS on class declarations</li>
 * </ul>
 * Throws an ExecutionError if an error is found.
 */
// TODO: Handle non-literal constant expressions
public class StatementChecker extends AbstractVisitor<TypeContext> implements Lambda<Node, TypeContext> {

  private final TypeContext context;
  private final Options opt;
  private final TypeSystem ts; // contained by opt, but this is a shortcut for easy reference

  public StatementChecker(TypeContext ctx, Options options) {
    context = ctx;
    opt = options;
    ts = opt.typeSystem();
  }
  
  public TypeContext value(Node n) { return n.acceptVisitor(this); }
  
  private Type checkType(Expression exp) { return new ExpressionChecker(context, opt).check(exp); }
  
  private TypeContext checkList(Iterable<? extends Node> l) {
    TypeContext c = context;
    for (Node n : l) { c = n.acceptVisitor(new StatementChecker(c, opt)); }
    return c;
  }

  private Type checkType(Expression exp, Type expected) {
    return new ExpressionChecker(context, opt).check(exp, expected);
  }
  
  @SuppressWarnings("unused") private Iterable<Type> checkTypes(Iterable<? extends Expression> l) {
    return new ExpressionChecker(context, opt).checkList(l);
  }
  
  private Type checkTypeName(TypeName t) {
    return new TypeNameChecker(context, opt).check(t);
  }
  
  
  /** Creates a new context in the given package */
  @Override public TypeContext visit(PackageDeclaration node) {
    return context.setPackage(node.getName());
  }

  /** Creates a new context with the given import */
  @Override public TypeContext visit(ImportDeclaration node) {
    if (node.isStatic()) {
      
      if (node.isPackage()) {
        // static on-demand import
        ClassType t = resolveClassName(node.getName(), node);
        if (t == null) {
          setErrorStrings(node, node.getName());
          throw new ExecutionError("undefined.class", node);
        }
        return context.importStaticMembers(t.ofClass());
      }
            
      else {
        // static member import
        Pair<String, String> split = splitName(node.getName());
        if (split.first() == null) {
          setErrorStrings(node, node.getName());
          throw new ExecutionError("undefined.name", node);
        }
        ClassType t = resolveClassName(split.first(), node);
        if (t == null) {
          setErrorStrings(node, node.getName());
          throw new ExecutionError("undefined.class", node);
        }
        String member = split.second();
        TypeContext result = context;
        if (ts.containsStaticField(t, member, context.accessModule())) {
          result = result.importField(t.ofClass(), member);
        }
        if (ts.containsStaticMethod(t, member, context.accessModule())) {
          result = result.importMethod(t.ofClass(), member);
        }
        if (ts.containsStaticClass(t, member, context.accessModule())) {
          result = result.importMemberClass(t.ofClass(), member);
        }
        if (result == context) {
          setErrorStrings(node, node.getName());
          throw new ExecutionError("undefined.name", node);
        }
        return result;
      }
          
    }
    else {
      
      if (node.isPackage()) {
        // on-demand import
        ClassType t = resolveClassName(node.getName(), node);
        if (t == null) { return context.importTopLevelClasses(node.getName()); }
        else { return context.importMemberClasses(t.ofClass()); }
      }
      
      else {
        // class import
        Pair<String, String> split = splitName(node.getName());
        if (split.first() != null) {
          ClassType t = resolveClassName(split.first(), node);
          if (t != null) {
            if (ts.containsClass(t, split.second(), context.accessModule())) {
              return context.importMemberClass(t.ofClass(), split.second());
            }
            else {
              setErrorStrings(node, split.second());
              throw new ExecutionError("undefined.class", node);
            }
          }
        }
        try {
          DJClass c = context.getTopLevelClass(node.getName(), ts);
          if (c == null) {
            setErrorStrings(node, node.getName());
            throw new ExecutionError("undefined.class", node);
          }
          else { return context.importTopLevelClass(c); }
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
      }
      
    }
  }
  
  private ClassType resolveClassName(String name, Node node) {
    String topLevelName = "";
    ClassType result = null;
    boolean first = true;
    for (String piece : name.split("\\.")) {
      if (result == null) {
        if (!first) { topLevelName += "."; }
        first = false;
        topLevelName += piece;
        try {
          DJClass c = context.getTopLevelClass(topLevelName, ts);
          result = (c == null) ? null : ts.makeClassType(c);
        }
        catch (AmbiguousNameException e) { throw new ExecutionError("ambiguous.name", node); }
      }
      else {
        try { result = ts.lookupClass(result, piece, IterUtil.<Type>empty(), context.accessModule()); }
        catch (InvalidTypeArgumentException e) { throw new RuntimeException("can't create raw type"); }
        catch (UnmatchedLookupException e) {
          setErrorStrings(node, piece);
          if (e.matches() > 1) { throw new ExecutionError("ambiguous.inner.class"); }
          else { throw new ExecutionError("no.such.inner.class", node); }
        }
      }
    }
    return result;
  }
  
  private Pair<String, String> splitName(String name) {
    int dot = name.lastIndexOf('.');
    if (dot == -1) { return Pair.make(null, name); }
    else { return Pair.make(name.substring(0, dot), name.substring(dot+1)); }
  }
  
  /** Checks the declaration's initializer and creates a new context */
  @Override public TypeContext visit(VariableDeclaration node) {
    if (node.getType() == null) {
      // We infer the variable's type.  We can assume the initializer is non-null.
      Type initT = checkType(node.getInitializer());
      LocalVariable v = new LocalVariable(node.getName(), initT, node.getModifiers().isFinal());
      setVariable(node, v);
      setErasedType(node, ts.erasedClass(initT));
      return new LocalContext(context, v);
    }
    else {
      Type t = checkTypeName(node.getType());
      LocalVariable v = new LocalVariable(node.getName(), t, node.getModifiers().isFinal());
      setVariable(node, v);
      setErasedType(node, ts.erasedClass(t));
      TypeContext newContext = new LocalContext(context, v);
      
      if (node.getInitializer() != null) {
        Type initT = checkType(node.getInitializer(), t);
        try {
          Expression newInit = ts.assign(t, node.getInitializer());
          node.setInitializer(newInit);
        }
        catch (UnsupportedConversionException e) {
          TypePrinter printer = ts.typePrinter();
          setErrorStrings(node, printer.print(initT), printer.print(t));
          throw new ExecutionError("assignment.types", node);
        }
      }
      return newContext;
    }
  }
  
  @Override public TypeContext visit(ClassDeclaration node) {
    return handleTypeDeclaration(node);
  }
  
  @Override public TypeContext visit(InterfaceDeclaration node) {
    return handleTypeDeclaration(node);
  }
  
  private TypeContext handleTypeDeclaration(TypeDeclaration node) {
    TreeClassLoader loader = new TreeClassLoader(context.getClassLoader(), opt);
    DJClass c = new TreeClass(context.makeClassName(node.getName()), null, context.accessModule(), node, loader, opt);
    setDJClass(node, c);
    ClassChecker classChecker = new ClassChecker(c, loader, context, opt);
    classChecker.initializeClassSignatures(node);
    classChecker.checkSignatures(node);
    classChecker.checkBodies(node);
    return new LocalContext(context, loader, c);
  }

  /**
   * Visits a MethodDeclaration.  Treated as a local function (class methods are handled by
   * ClassMemberChecker).
   */
  @Override public TypeContext visit(MethodDeclaration node) {
    LocalFunction f = new LocalFunction(node);
    
    TypeContext sigContext = new FunctionSignatureContext(context, f);
    TypeNameChecker sigChecker = new TypeNameChecker(sigContext, opt);

    TypeParameter[] tparams;
    if (node instanceof PolymorphicMethodDeclaration) {
      tparams = ((PolymorphicMethodDeclaration) node).getTypeParameters();
    }
    else { tparams = new TypeParameter[0]; }
    sigChecker.checkTypeParameters(tparams);

    Type returnT = sigChecker.check(node.getReturnType());
    setErasedType(node, ts.erasedClass(returnT));
    for (FormalParameter p : node.getParameters()) {
      Type t = sigChecker.check(p.getType());
      setVariable(p, new LocalVariable(p.getName(), t, p.getModifiers().isFinal()));
    }
    for (ReferenceTypeName n : node.getExceptions()) { sigChecker.check(n); }
    
    if (node.getBody() == null) {
      setErrorStrings(node, node.getName());
      throw new ExecutionError("missing.method.body", node);
    }
    TypeContext bodyContext = new FunctionContext(sigContext, f);
    node.getBody().acceptVisitor(new StatementChecker(bodyContext, opt));
    
    return new LocalContext(context, f);
  }
  
  
  /**
   * Visits a WhileStatement.  JLS 14.12.
   */
  @Override public TypeContext visit(WhileStatement node) {
    checkType(node.getCondition());
    try {
      Expression exp = ts.makePrimitive(node.getCondition());
      if (!(getType(exp) instanceof BooleanType)) {
        throw new ExecutionError("condition.type", node);
      }
      node.setCondition(exp);
    }
    catch (UnsupportedConversionException e) {
      throw new ExecutionError("condition.type", node);
    }
    
    node.getBody().acceptVisitor(this);
    return context;
  }

  /**
   * Visits a DoStatement.  JLS 14.13.
   */
  @Override public TypeContext visit(DoStatement node) {
    node.getBody().acceptVisitor(this);
    checkType(node.getCondition());
    try {
      Expression exp = ts.makePrimitive(node.getCondition());
      if (!(getType(exp) instanceof BooleanType)) {
        throw new ExecutionError("condition.type", node);
      }
      node.setCondition(exp);
    }
    catch (UnsupportedConversionException e) {
      throw new ExecutionError("condition.type", node);
    }

    return context;
  }

  /**
   * Visits a ForStatement.  JLS 14.4.1.
   */
  @Override public TypeContext visit(ForStatement node) {
    TypeContext newContext = context;
    if (node.getInitialization() != null) { newContext = checkList(node.getInitialization()); }
    StatementChecker checker = new StatementChecker(newContext, opt);
    
    if (node.getCondition() != null) {
      checker.checkType(node.getCondition());
      try {
        Expression exp = ts.makePrimitive(node.getCondition());
        if (!(getType(exp) instanceof BooleanType)) {
          throw new ExecutionError("condition.type", node);
        }
        node.setCondition(exp);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("condition.type", node);
      }
    }
    
    if (node.getUpdate() != null) { checker.checkList(node.getUpdate()); }

    node.getBody().acceptVisitor(checker);
    return context; // We do *not* return newContext
  }

  /**
   * Visits a ForEachStatement.  JLS 14.14.2.
   */
  @Override public TypeContext visit(ForEachStatement node) {
    FormalParameter p = node.getParameter();
    Type paramT = checkTypeName(p.getType());
    LocalVariable var = setVariable(p, new LocalVariable(p.getName(), paramT, p.getModifiers().isFinal()));
    TypeContext newContext = new LocalContext(context, var);
    Type collType = checkType(node.getCollection());

    if (ts.isArray(collType)) {
      Type elementType = ts.arrayElementType(collType);
      if (!ts.isAssignable(paramT, elementType)) {
        TypePrinter printer = ts.typePrinter();
        setErrorStrings(node, printer.print(elementType), printer.print(paramT));
        throw new ExecutionError("assignment.types", node);
      }
    }
    else if (ts.isIterable(collType)) {
      try {
        MethodInvocation iteratorInv = ts.lookupMethod(node.getCollection(), "iterator", IterUtil.<Type>empty(),
                                                       IterUtil.<Expression>empty(), Option.<Type>none(),
                                                       context.accessModule());
        
        
        Expression getIterator = TypeUtil.makeEmptyExpression(node.getCollection());
        setType(getIterator, iteratorInv.returnType());
        MethodInvocation nextInv = ts.lookupMethod(getIterator, "next", IterUtil.<Type>empty(),
                                                   IterUtil.<Expression>empty(), Option.<Type>none(),
                                                   context.accessModule());
        
        if (!ts.isAssignable(paramT, nextInv.returnType())) {
          TypePrinter printer = ts.typePrinter();
          setErrorStrings(node, printer.print(nextInv.returnType()), printer.print(paramT));
          throw new ExecutionError("assignment.types", node);
        }
      }
      catch (TypeSystemException e) { throw new RuntimeException("ts.isIterable() lied"); }
    }
    else {
      throw new ExecutionError("iterable.type", node);
    }
    
    node.getBody().acceptVisitor(new StatementChecker(newContext, opt));
    return context; // We do *not* return newContext
  }

  /**
   * Visits an IfThenStatement.  JLS 14.9.
   */
  @Override public TypeContext visit(IfThenStatement node) {
    checkType(node.getCondition());
    try {
      Expression exp = ts.makePrimitive(node.getCondition());
      if (!(getType(exp) instanceof BooleanType)) {
        throw new ExecutionError("condition.type", node);
      }
      node.setCondition(exp);
    }
    catch (UnsupportedConversionException e) {
      throw new ExecutionError("condition.type", node);
    }

    node.getThenStatement().acceptVisitor(this);
    return context;
  }

  /**
   * Visits an IfThenElseStatement.  JLS 14.9.
   */
  @Override public TypeContext visit(IfThenElseStatement node) {
    checkType(node.getCondition());
    try {
      Expression exp = ts.makePrimitive(node.getCondition());
      if (!(getType(exp) instanceof BooleanType)) {
        throw new ExecutionError("condition.type", node);
      }
      node.setCondition(exp);
    }
    catch (UnsupportedConversionException e) {
      throw new ExecutionError("condition.type", node);
    }

    node.getThenStatement().acceptVisitor(this);
    node.getElseStatement().acceptVisitor(this);
    return context;
  }

  /**
   * Visits a SwitchStatement.  JLS 14.11.
   */
  @Override public TypeContext visit(SwitchStatement node) {
    Type t = checkType(node.getSelector());
    if (!ts.isEnum(t)) {
      try {
        Expression exp = ts.makePrimitive(node.getSelector());
        if (!(getType(exp) instanceof IntegralType) || (getType(exp) instanceof LongType)) {
          setErrorStrings(node, ts.typePrinter().print(t));
          throw new ExecutionError("selector.type", node);
        }
        node.setSelector(exp);
        t = getType(exp);
      }
      catch (UnsupportedConversionException e) {
        throw new ExecutionError("selector.type", node);
      }
    }
    
    Set<Object> values = new HashSet<Object>();
    boolean hasDefault = false;
    for (SwitchBlock bk : node.getBindings()) {
      /* To be fully correct, the context used here should follow the following scoping rules (JLS 6.3):
         - A local variable is in scope for the rest of the switch statement's body -- it "falls through"
         - A local class is *only* in scope in its SwitchBlock -- it does not "fall through"
         This is a mess.  For now we just follow a no-fall-through approach. */
      
      bk.acceptVisitor(this);
      
      if (bk.getExpression() == null) {
        if (hasDefault) { throw new ExecutionError("duplicate.switch.case", node); }
        hasDefault = true;
      }
      
      else {
        Expression exp = bk.getExpression();
        if (!hasValue(exp) || getValue(exp) == null) {
          throw new ExecutionError("invalid.constant", exp);
        }
        if (!ts.isAssignable(t, getType(exp), getValue(exp))) {
          setErrorStrings(node, ts.typePrinter().print(getType(exp)));
          throw new ExecutionError("switch.label.type", exp);
        }
        if (values.contains(getValue(exp))) { 
          throw new ExecutionError("duplicate.switch.case", node);
        }
        values.add(getValue(exp));
      }
    }
    
    return context;
  }

  /**
   * Visits a SwitchBlock.  JLS 14.11.
   */
  @Override public TypeContext visit(SwitchBlock node) {
    if (node.getExpression() != null) { checkType(node.getExpression()); }
    if (node.getStatements() != null) { checkList(node.getStatements()); }
    return context;
  }

  /**
   * Visits a LabeledStatement.  JLS 14.7.
   */
  @Override public TypeContext visit(LabeledStatement node) {
    return node.getStatement().acceptVisitor(this);
  }

  /**
   * Visits a TryStatement.  JLS 14.20.
   */
  @Override public TypeContext visit(TryStatement node) {
    List<Type> caughtTypes = new LinkedList<Type>();
    for (CatchStatement c : node.getCatchStatements()) {
      FormalParameter p = c.getException();
      Type caughtT = checkTypeName(p.getType());
      if (!ts.isAssignable(TypeSystem.THROWABLE, caughtT)) {
        setErrorStrings(c, ts.typePrinter().print(caughtT));
        throw new ExecutionError("catch.type", c);
      }
      if (!ts.isReifiable(caughtT)) {
        throw new ExecutionError("reifiable.type", c);
      }
      setVariable(p, new LocalVariable(p.getName(), caughtT, p.getModifiers().isFinal()));
      setErasedType(c, ts.erasedClass(caughtT));
      caughtTypes.add(caughtT);
    }
    
    TypeContext tryContext = new TryBlockContext(context, caughtTypes);
    node.getTryBlock().acceptVisitor(new StatementChecker(tryContext, opt));
    
    for (CatchStatement c : node.getCatchStatements()) {
      TypeContext catchContext = new LocalContext(context, getVariable(c.getException()));
      c.getBlock().acceptVisitor(new StatementChecker(catchContext, opt));
    }
    
    if (node.getFinallyBlock() != null) { node.getFinallyBlock().acceptVisitor(this); }
    
    return context;
  }

  /**
   * Visits a ThrowStatement.  JLS 14.18.
   */
  @Override public TypeContext visit(ThrowStatement node) {
    Type thrown = checkType(node.getExpression());
    if (!ts.isAssignable(TypeSystem.THROWABLE, thrown)) {
      setErrorStrings(node, ts.typePrinter().print(thrown));
      throw new ExecutionError("throw.type", node);
    }
    else if (ts.isAssignable(TypeSystem.EXCEPTION, thrown)) {
      boolean valid = false;
      Iterable<Type> allowed = IterUtil.compose(TypeSystem.RUNTIME_EXCEPTION,
                                                context.getDeclaredThrownTypes());
      for (Type t : allowed) {
        if (ts.isAssignable(t, thrown)) { valid = true; break; }
      }
      if (!valid) {
        setErrorStrings(node, ts.typePrinter().print(thrown));
        throw new ExecutionError("uncaught.exception", node);
      }
    }
    return context;
  }

  /**
   * Visits a ReturnStatement
   */
  @Override public TypeContext visit(ReturnStatement node) {
    Type expected = context.getReturnType();
    if (expected == null) { throw new ExecutionError("return.not.allowed", node); }

    if (node.getExpression() == null) {
      if (!expected.equals(TypeSystem.VOID)) {
        TypePrinter printer = ts.typePrinter();
        setErrorStrings(node, printer.print(TypeSystem.VOID), printer.print(expected));
        throw new ExecutionError("return.type", node);
      }
    }
    else {
      checkType(node.getExpression(), expected);
      try {
        Expression newExp = ts.assign(expected, node.getExpression());
        node.setExpression(newExp);
      }
      catch (UnsupportedConversionException e) {
        TypePrinter printer = ts.typePrinter();
        setErrorStrings(node, printer.print(getType(node.getExpression())), printer.print(expected));
        throw new ExecutionError("return.type", node);
      }
    }
    
    return context;
  }
  
    /**
   * Visits an AssertStatement.  JLS 14.10.
   */
  @Override public TypeContext visit(AssertStatement node) {
    checkType(node.getCondition());
    try {
      Expression exp = ts.makePrimitive(node.getCondition());
      if (!(getType(exp) instanceof BooleanType)) {
        throw new ExecutionError("condition.type", node);
      }
      node.setCondition(exp);
    }
    catch (UnsupportedConversionException e) {
      throw new ExecutionError("condition.type", node);
    }
    
    if (node.getFailString() != null) {
      Type failType = checkType(node.getFailString());
      if (failType instanceof VoidType) { throw new ExecutionError("assertion.fail.type", node); }
    }
    
    return context;
  }
  
  /**
   * Visits a SynchronizedStatement.  JLS 14.19.
   */
  @Override public TypeContext visit(SynchronizedStatement node) {
    Type lockT = checkType(node.getLock());
    if (!ts.isReference(lockT)) { throw new ExecutionError("lock.type", node); }
    node.getBody().acceptVisitor(this);
    return context;
  }

  /**
   * Visits a BlockStatement
   * @param node the node to visit
   */
  @Override public TypeContext visit(BlockStatement node) {
    checkList(node.getStatements());
    return context;
  }
  
  @Override public TypeContext visit(EmptyStatement node) {
    return context;
  }
  
  @Override public TypeContext visit(BreakStatement node) {
    return context; // TODO: check control-flow context, labels
  }

  @Override public TypeContext visit(ContinueStatement node) {
    return context; // TODO: check control-flow context, labels
  }

  @Override public TypeContext visit(ExpressionStatement node) {
    if (node.getExpression() instanceof SimpleAssignExpression &&
        !opt.requireVariableType() && (node.getHasSemicolon() || !opt.requireSemicolon())) {
      SimpleAssignExpression assign = (SimpleAssignExpression) node.getExpression();
      if (assign.getLeftExpression() instanceof AmbiguousName) {
        AmbiguousName ambigName = (AmbiguousName) assign.getLeftExpression();
        if (ambigName.getIdentifiers().size() == 1) {
          String name = ambigName.getRepresentation();
          if (!context.variableExists(name, opt.typeSystem())) {
            Node decl = new VariableDeclaration(ModifierSet.make(), null, name, assign.getRightExpression(),
                                                node.getSourceInfo());
            setStatementTranslation(node, decl);
            return decl.acceptVisitor(this);
          }
        }
      }
    }
    // all other cases that don't match the nested ifs:
    checkType(node.getExpression());
    return context;
  }
  
}
