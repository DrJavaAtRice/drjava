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

import java.lang.reflect.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;

/**
 * A subclass of NameVisitor that preempts the context defining
 * variables when it visits VariableDeclarations by making sure 
 * the NameVisitor and TypeChecker will accept it first.
 *
 * @version $Id$
 */

public class NameVisitorExtension extends NameVisitor {
  private Context _context;
  private TypeChecker _tc;
  
  public NameVisitorExtension(Context ctx) {    
    super(ctx);
    _context = ctx;
    _tc = new TypeChecker(ctx);
  }  
  
  // An attempt at fixing the redefinition issue in DynamicJava
  public Object visit(VariableDeclaration node) {  
    // NameVisitor
    Node n = node.getInitializer();
    if (n != null) {
      Object o = n.acceptVisitor(this);
      if (o != null) {
        if (o instanceof ReferenceType) {
          throw new ExecutionError("malformed.expression", n);
        }
        node.setInitializer((Expression)o);
      }
    }
    
    // TypeChecker
    Class lc = (Class)node.getType().acceptVisitor(_tc);
    Node init = node.getInitializer();
    if (init != null) {
      Class rc = (Class)init.acceptVisitor(_tc);
      _checkAssignmentStaticRules(lc, rc, node, init);
    }
    
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
        } else if (lc == byte.class && rc != byte.class) {
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.byteValue()) {
              return;
            }
          }
          throw new ExecutionError("assignment.types", node);
        } else if ((lc == short.class || rc == char.class) &&
                   (rc != byte.class && rc != short.class && rc != char.class)) {
          if (rc == int.class && v.hasProperty(NodeProperties.VALUE)) {
            Number n = (Number)v.getProperty(NodeProperties.VALUE);
            if (n.intValue() == n.shortValue()) {
              return;
            }
          }
          throw new ExecutionError("assignment.types", node);
        } else if (lc == int.class    &&
                   (rc != byte.class  &&
                    rc != short.class &&
                    rc != char.class  &&
                    rc != int.class)) {
          throw new ExecutionError("assignment.types", node);
        } else if (lc == long.class   &&
                   (rc == null          ||
                    !rc.isPrimitive()   ||
                    rc == void.class    ||
                    rc == boolean.class ||
                    rc == float.class   ||
                    rc == double.class)) {
          throw new ExecutionError("assignment.types", node);
        } else if (lc == float.class  && 
                   (rc == null          ||
                    !rc.isPrimitive()   ||
                    rc == void.class    ||
                    rc == boolean.class ||
                    rc == double.class)) {
          throw new ExecutionError("assignment.types", node);
        } else if (lc == double.class && 
                   (rc == null        ||
                    !rc.isPrimitive() ||
                    rc == void.class  ||
                    rc == boolean.class)) {
          throw new ExecutionError("assignment.types", node);
        }
      } else if (rc != null) {
        if (!lc.isAssignableFrom(rc)) {
          throw new ExecutionError("assignment.types", node);
        }
      }
    }
  }
}