/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
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