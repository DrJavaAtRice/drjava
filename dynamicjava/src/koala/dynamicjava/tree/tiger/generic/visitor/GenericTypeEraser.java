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

package koala.dynamicjava.tree.tiger.generic.visitor;

import koala.dynamicjava.tree.tiger.generic.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.VisitorObject; /**/ // needs to be just Visitor

import java.util.*;

public class GenericTypeEraser extends VisitorObject<Node> implements GenericVisitor<Node> {
  private Environment<String, ReferenceType> _typeParams;
  
  /** Constructors. */
  public GenericTypeEraser() { _typeParams = new EmptyEnv<String, ReferenceType>(); }
  
  public GenericTypeEraser(Environment<String, ReferenceType> typeParams) { 
    _typeParams = typeParams; 
  }
  
  /** Helper methods. */
  GenericTypeEraser extend(TypeParameter[] params) {
    Map<String, ReferenceType> scope = 
      new HashMap<String, ReferenceType>();
 
    for (int i = 0; i < params.length; i++) {
      scope.put(params[i].getVariable().getName(), 
                // Since bounds can't be type variables, and we need only store 
                // erasures of type bounds, it's acceptable to recur on the bound 
                // before building its enclosing environment.
                (ReferenceType)params[i].getBound().acceptVisitor(this));
    }
    return new GenericTypeEraser(_typeParams.extend(scope));
  }
  
  /**
   * Visits all elements of an array and returns the result.
   */
  <T> /*<? extends Node>*/ List<T> forList(List<T> that) { // The ? syntax is only supported by JSR14 v 2.4
    List<T> result = new LinkedList<T>();
    
    for (int i = 0; i < that.size(); i++) {
      T n = ((Node)that.get(i)).acceptVisitor(this);
      result.add(n);
    }   
    return result;
  }
  
  public Node visit(PolymorphicMethodDeclaration node){
    GenericTypeEraser extension = this.extend(node.getTypeParameters());
    return new MethodDeclaration(node.getFlags(), 
                                 node.getType().acceptVisitor(extension), 
                                 node.getName(), 
                                 extension.forList(node.getParameters()),
                                 extension.forList(node.getExceptions()),
                                 node.getBody().acceptVisitor(extension),
                                 node.getFilename(), 
                                 node.getBeginLine(), node.getBeginColumn(), 
                                 node.getEndLine(), node.getEndColumn()
                                );
  }
  
  public Node visit(GenericReferenceType node){
    return null;
  }
  
  public Node visit(GenericClassDeclaration node){
    return null;
  }
  
  public Node visit(GenericInterfaceDeclaration node){
    return null;
  }
  
  public Node visit(TypeParameter node){
    return null;
  }
  
  public Node visit(TypeVariable node){
    return null;
  }
}


/*
if (_typeParams.contains(that.getName())) {
  return _typeParams.lookup(that.getName());
}
else {
  return new ClassOrInterfaceType(that.getSourceInfo(), 
                                  that.getName(), 
                                  new Type[0]);
}
}
  
  public JavaAST forAbstractMethodDef(AbstractMethodDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());
    return new AbstractMethodDef(that.getSourceInfo(), 
                                 that.getName(), 
                                 that.getVisibility(), 
                                 (ResultTypeI)that.getReturnType().accept(extension),
                                 (FormalParameter[])extension.forArray(that.getParameters()),
                                 new TypeParameter[0],
                                 (ReferenceType[])extension.forArray(that.getThrows()),
                                 that.isSynchronized(),
                                 that.isStrictfp());
  }
  
  public JavaAST forNativeMethodDef(NativeMethodDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());
    return new NativeMethodDef(that.getSourceInfo(), 
                               that.getName(), 
                               that.getVisibility(), 
                               (ResultTypeI)that.getReturnType().accept(extension),
                               (FormalParameter[])extension.forArray(that.getParameters()),
                               new TypeParameter[0],
                               (ReferenceType[])extension.forArray(that.getThrows()),
                               that.isSynchronized(),
                               that.isFinal(),
                               that.isStatic());
  }
  
  public JavaAST forInstanceMethodDef(InstanceMethodDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());
    return new InstanceMethodDef(that.getSourceInfo(), 
                                 that.getName(), 
                                 that.getVisibility(), 
                                 (ResultTypeI)that.getReturnType().accept(extension),
                                 (FormalParameter[])extension.forArray(that.getParameters()),
                                 new TypeParameter[0],
                                 (ReferenceType[])extension.forArray(that.getThrows()),
                                 that.isSynchronized(),
                                 that.isFinal(),
                                 that.isStrictfp(),
                                 (Block)that.getCode().accept(extension));
  }   
  
  public JavaAST forStaticMethodDef(StaticMethodDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());
    return new StaticMethodDef(that.getSourceInfo(), 
                               that.getName(), 
                               that.getVisibility(), 
                               (ResultTypeI)that.getReturnType().accept(extension),
                               (FormalParameter[])extension.forArray(that.getParameters()),
                               new TypeParameter[0],
                               (ReferenceType[])extension.forArray(that.getThrows()),
                               that.isSynchronized(),
                               that.isFinal(),
                               that.isStrictfp(),
                               (Block)that.getCode().accept(extension));
  }   
  
  public JavaAST forClassDef(ClassDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new ClassDef(that.getSourceInfo(),
                        that.getName(),
                        that.getVisibility(),
                        new TypeParameter[0],
                        that.isStrictfp(),
                        that.getModifier(),
                        (ReferenceType)that.getSuperclass().accept(extension),
                        (ReferenceType[])extension.forArray(that.getInterfaces()),
                        (ConstructorDef[])extension.forArray(that.getConstructors()),
                        (MethodDef[])extension.forArray(that.getMethods()),
                        (FieldDef[])extension.forArray(that.getFields()),
                        (Initializer[])extension.forArray(that.getInitializers()),
                        (InnerDefI[])extension.forArray(that.getInners()));
  }
  
  public JavaAST forDynamicInnerClassDef(DynamicInnerClassDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new DynamicInnerClassDef(that.getSourceInfo(),
                                    that.getName(),
                                    that.getVisibility(),
                                    new TypeParameter[0],
                                    that.isStrictfp(),
                                    that.getModifier(),
                                    (ReferenceType)that.getSuperclass().accept(extension),
                                    (ReferenceType[])extension.forArray(that.getInterfaces()),
                                    (ConstructorDef[])extension.forArray(that.getConstructors()),
                                    (MethodDef[])extension.forArray(that.getMethods()),
                                    (FieldDef[])extension.forArray(that.getFields()),
                                    (Initializer[])extension.forArray(that.getInitializers()),
                                    (InnerDefI[])extension.forArray(that.getInners()));
  }  
  
  public JavaAST forStaticInnerClassDef(StaticInnerClassDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new StaticInnerClassDef(that.getSourceInfo(),
                                   that.getName(),
                                   that.getVisibility(),
                                   new TypeParameter[0],
                                   that.isStrictfp(),
                                   that.getModifier(),
                                   (ReferenceType)that.getSuperclass().accept(extension),
                                   (ReferenceType[])extension.forArray(that.getInterfaces()),
                                   (ConstructorDef[])extension.forArray(that.getConstructors()),
                                   (MethodDef[])extension.forArray(that.getMethods()),
                                   (FieldDef[])extension.forArray(that.getFields()),
                                   (Initializer[])extension.forArray(that.getInitializers()),
                                   (InnerDefI[])extension.forArray(that.getInners()));
  } 
  
  public JavaAST forInterfaceDef(InterfaceDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new InterfaceDef(that.getSourceInfo(),
                            that.getName(),
                            that.getVisibility(),
                            new TypeParameter[0],
                            that.isStrictfp(),
                            (ReferenceType[])extension.forArray(that.getSuperinterfaces()),
                            (AbstractMethodDef[])extension.forArray(that.getMethods()),
                            (FinalStaticFieldDef[])extension.forArray(that.getFields()),
                            (StaticInnerDefI[])extension.forArray(that.getInners()));
  } 
  
  public JavaAST forInnerInterfaceDef(InnerInterfaceDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new InnerInterfaceDef(that.getSourceInfo(),
                                 that.getName(),
                                 that.getVisibility(),
                                 new TypeParameter[0],
                                 that.isStrictfp(),
                                 (ReferenceType[])extension.forArray(that.getSuperinterfaces()),
                                 (AbstractMethodDef[])extension.forArray(that.getMethods()),
                                 (FinalStaticFieldDef[])extension.forArray(that.getFields()),
                                 (StaticInnerDefI[])extension.forArray(that.getInners()));
  } 
  
  public JavaAST forLocalClassDef(LocalClassDef that) {
    TypeEraser extension = this.extend(that.getTypeParameters());    
    return new LocalClassDef(that.getSourceInfo(),
                             that.getName(),
                             that.getModifier(),                       
                             new TypeParameter[0],
                             that.isStrictfp(),
                             (ReferenceType)that.getSuperclass().accept(extension),
                             (ReferenceType[])extension.forArray(that.getInterfaces()),
                             (ConstructorDef[])extension.forArray(that.getConstructors()),
                             (MethodDef[])extension.forArray(that.getMethods()),
                             (FieldDef[])extension.forArray(that.getFields()),
                             (Initializer[])extension.forArray(that.getInitializers()),
                             (InnerDefI[])extension.forArray(that.getInners()));
  }     
*/