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

import koala.dynamicjava.tree.tiger.generic.GenericReferenceType;



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
  public Class visit(ForEachStatement node){
    throw new WrongVersionException("ForEach Statements are only supported in Java 1.5 or better");
  }

  /**
   * Checks if the node is of GenericReference Type, which is only allowed in 1.5 or better
   * @param node the node being checked
   */  
  protected void checkGenericReferenceType(ReferenceType node) {
   if(node instanceof GenericReferenceType)
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
    if(node.isVarArgs())
      throw new WrongVersionException("Methods with variable arguments are only allowed in Java 1.5 or better");
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
  protected SimpleAllocation _box(Expression exp, Class refType) {
    throw new WrongVersionException("Box required to use " + refType + " here. Autoboxing requires minimum Java 1.5");
  }
  
  /**
   * Unboxes the given expression by returning the correct
   * <code>ObjectMethodCall</code> corresponding to the given type
   * @param child The expression to unbox
   * @param type The type of the evaluated expression
   * @return The <code>ObjectMethodCall</code> that unboxes the expression
   */
  protected ObjectMethodCall _unbox(Expression child, Class type) {
    throw new WrongVersionException("Unbox required to use " + type + " here. Auto-unboxing requires minimum Java 1.5");
  }
}
