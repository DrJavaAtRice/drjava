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

package edu.rice.cs.drjava.model.repl;

import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.tree.*;

/**
 * A subclass of NameVisitor that preempts the context defining
 * variables when it visits VariableDeclarations by making sure
 * the NameVisitor and TypeChecker will accept it first.
 *
 * This class is loaded in the Interpreter JVM, not the Main JVM.
 * (Do not use DrJava's config framework here.)
 *
 * @version $Id$
 */

public class NameVisitorExtension extends NameVisitor {
  private Context _context;
  private AbstractTypeChecker _tc;
  
  /**
   * Creates a new NameVisitorExtension.
   * @param nameContext Context for the NameVisitor
   * @param typeContext Context being used for the TypeChecker.  This is
   * necessary because we want to perform a partial type checking for the
   * right hand side of a VariableDeclaration.
   */
  public NameVisitorExtension(Context nameContext, Context typeContext) {
    super(nameContext);
    _context = nameContext;
    _tc = AbstractTypeChecker.makeTypeChecker(typeContext);
  }
  
  // Fixes the redefinition issue in DynamicJava
  public Node visit(VariableDeclaration node) {
    // NameVisitor
    Node n = node.getInitializer();
    if (n != null) {
      Node o = n.acceptVisitor(this);
      if (o != null) {
        rejectReferenceType(o,n);
        node.setInitializer((Expression)o);
      }
    }

    /** 
     * The following commented code was moved into the actual 
     * AbstractTypeChecker to reduce duplication of code and
     * to fix some bugs
     */
//    // TypeChecker
//    Class lc = node.getType().acceptVisitor(_tc);
//    Node init = node.getInitializer();
//    if (init != null) {
//      Class rc = init.acceptVisitor(_tc);
//      _checkAssignmentStaticRules(lc, rc, node, init);
//    }
    _tc.preCheckVariableDeclaration(node);
    
    //        // EvaluationVisitor
    //        EvaluationVisitorExtension eve = new EvaluationVisitorExtension(context);
    //        Class c = NodeProperties.getType(node.getType());
    //
    //        if (node.getInitializer() != null) {
    //          Object o = eve.performCast(c, node.getInitializer().acceptVisitor(eve));
    //        }
    return super.visit(node);
  }

  
  private static void _checkAssignmentStaticRules(Class lc, Class rc,
                                                  Node node, Node v) {
    if (lc != null) {
      if (lc.isPrimitive()) {
        if (lc == boolean.class && rc != boolean.class) {
          throw new ExecutionError("assignment.types", node);
        } 
        else if (lc == byte.class && rc != byte.class) {
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.byteValue()) {
              return;
            }
          }
          throw new ExecutionError("assignment.types", node);
        } 
        else if ((lc == short.class || rc == char.class) &&
                 (rc != byte.class && rc != short.class && rc != char.class)) {
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.shortValue()) {
              return;
            }
          }
          throw new ExecutionError("assignment.types", node);
        } 
        else if (lc == int.class    &&
                 (rc != byte.class  &&
                  rc != short.class &&
                  rc != char.class  &&
                  rc != int.class)) {
          throw new ExecutionError("assignment.types", node);
        } 
        else if (lc == long.class   &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == float.class   ||
                  rc == double.class)) {
          throw new ExecutionError("assignment.types", node);
        } 
        else if (lc == float.class  &&
                 (rc == null          ||
                  !rc.isPrimitive()   ||
                  rc == void.class    ||
                  rc == boolean.class ||
                  rc == double.class)) {
          throw new ExecutionError("assignment.types", node);
        } 
        else if (lc == double.class &&
                 (rc == null        ||
                  !rc.isPrimitive() ||
                  rc == void.class  ||
                  rc == boolean.class)) {
          throw new ExecutionError("assignment.types", node);
        }
      } 
      else if (rc != null) {
        if (!lc.isAssignableFrom(rc)) {
          throw new ExecutionError("assignment.types", node);
        }
      }
    }
  }
}
