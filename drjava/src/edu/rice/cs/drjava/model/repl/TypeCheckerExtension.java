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
import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.context.Context;
import koala.dynamicjava.interpreter.error.ExecutionError;

/**
 * Overrides divide and mod so that they won't evaluate any expressions in 
 * the type checker since this may cause divide by zero exceptions even when
 * short circuiting should occur (e.g. (false) ? 2/0 else 1 will cause an
 * exception).
 * 
 * NOTE: These problems have been corrected in newer versions of
 * DynamicJava, so this class is no longer used.
 * 
 * $Id$
 */
public class TypeCheckerExtension extends TypeChecker {
  
  public TypeCheckerExtension(Context c) {
    super(c);
  }
  
  /**
   * Visits a DivideExpression
   * @param node the node to visit
   */
  public Object visit(DivideExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "division.type");
    return c;
  }
  
  /**
   * Visits a RemainderExpression
   * @param node the node to visit
   */
  public Object visit(RemainderExpression node) {
    Node  ln = node.getLeftExpression();
    Node  rn = node.getRightExpression();
    ln.acceptVisitor(this);
    rn.acceptVisitor(this);
    Class c = visitNumericExpression(node, "remainder.type");
    return c;
  }
  
  /**
   * Visits a numeric expression
   */
  private static Class visitNumericExpression(BinaryExpression node, String s) {
    // Set the type property of the given node
    Class lc = NodeProperties.getType(node.getLeftExpression());
    Class rc = NodeProperties.getType(node.getRightExpression());
    Class c  = null;
    
    if (lc == null           || rc == null          ||
        lc == boolean.class  || rc == boolean.class ||
        !lc.isPrimitive()    || !rc.isPrimitive()   ||
        lc == void.class     || rc == void.class) {
      throw new ExecutionError(s, node);
    } else if (lc == double.class || rc == double.class) {
      node.setProperty(NodeProperties.TYPE, c = double.class);
    } else if (lc == float.class || rc == float.class) {
      node.setProperty(NodeProperties.TYPE, c = float.class);
    } else if (lc == long.class || rc == long.class) {
      node.setProperty(NodeProperties.TYPE, c = long.class);
    } else {
      node.setProperty(NodeProperties.TYPE, c = int.class);
    }
    return c;
  }
}