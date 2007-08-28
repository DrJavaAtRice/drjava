/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.interpreter;

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

import koala.dynamicjava.tree.tiger.GenericReferenceTypeName;



/**
 * This tree visitor checks the typing rules and loads
 * the classes, fields and methods
 *
 * @author Stephane Hillion
 * @version 1.2 - 1999/11/20
 */

public class TypeChecker14 extends AbstractTypeChecker {

  /**
   * Creates a new name visitor
   * @param ctx the context
   */
  public TypeChecker14(Context ctx) {
    super(ctx);
  }

  /**
   * Visits a ForEachStatement
   * @param node the node to visit
   */
  public Class<?> visit(ForEachStatement node){
    throw new WrongVersionException("ForEach Statements are only supported in Java 1.5 or higher");
  }

  /**
   * Checks if the node is of GenericReference TypeName, which is only allowed in 1.5 or better
   * @param node the node being checked
   */
  protected void checkGenericReferenceType(ReferenceTypeName node) {
   if(node instanceof GenericReferenceTypeName)
     throw new WrongVersionException("Generics are not supported before Java 1.5");
  }

  /**
   * Checks to see if the MethodDeclaration has variable arguments, which are only allowed in 1.5 or better
   * Note: the checkVarArgs here may or may not actually work. The test case in Distinction1415 failed when tiger features
   * were disabled, and when tracing through, the visitor that actually acted upon the method declaration node was the ClassInfoCompiler, and
   * there is another statement throwing a WrongVersionException in this file.
   * @param node - the MethodDeclaration which may or may not contain variable arguments
   */
  protected void checkVarArgs(MethodDeclaration node) {
    if(true){
    throw new RuntimeException("What the hell!!!");
    }
    if(node.isVarArgs())
      throw new WrongVersionException("Methods with variable arguments are only allowed in Java 1.5 or higher");
  }

   /**
   * Visits a static import statement and throws an exception
   * @param node the ImportDeclaration node being checked
   */
  protected void staticImportHandler(ImportDeclaration node){
      throw new WrongVersionException("Static Import is not supported before Java 1.5");
  }


  /**
   * Boxing is not allowed prior to version 1.5. Throws an exception.
   * @param exp the expression to box
   * @param refType the reference type to box the primitive type to
   * @return the <code>SimpleAllocation</code> that boxes the expression
   */
  protected SimpleAllocation _box(Expression exp, Class<?> refType) {
    throw new WrongVersionException("Box required to use " + refType + " here. Autoboxing requires Java 1.5 or higher");
  }

  /**
   * Unboxes the given expression by returning the correct
   * <code>ObjectMethodCall</code> corresponding to the given type
   * @param child The expression to unbox
   * @param type The type of the evaluated expression
   * @return The <code>ObjectMethodCall</code> that unboxes the expression
   */
  protected ObjectMethodCall _unbox(Expression child, Class<?> type) {
    throw new WrongVersionException("Unbox required to use " + type + " here. Auto-unboxing requires Java 1.5 or higher");
  }
}
