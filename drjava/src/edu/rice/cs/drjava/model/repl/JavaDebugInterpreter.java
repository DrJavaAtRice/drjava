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
import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;

public class JavaDebugInterpreter extends DynamicJavaAdapter {
  /**
   * This interpreter's name.
   */
  protected final String _name;
  
  /**
   * Creates a new debug interpreter.
   * @param name the name of the interpreter
   */
  public JavaDebugInterpreter(String name) {
    _name = name;
  }
  
  /**
   * Helper method to convert a ThisExpression to a QualifiedName.
   * Allows us to redefine "this" in a debug interpreter.
   * @param node ThisExpression
   * @return Corresponding QualifiedName
   */
  protected QualifiedName _convertThisToName(ThisExpression node) {
    java.util.List ids = new java.util.LinkedList();
    ids.add(new Identifier("this", node.getBeginLine(), node.getBeginColumn(),
                           node.getEndLine(), node.getEndColumn()));
    return new QualifiedName(ids, node.getFilename(),
                             node.getBeginLine(), node.getBeginColumn(),
                             node.getEndLine(), node.getEndColumn());
  }

  /**
   * Factory method to make a new NameVisitor that treats "this" as a variable.
   * @param context the context
   * @return visitor the visitor
   */
  public NameVisitor makeNameVisitor(Context context) {
    return new NameVisitor(context) {
      public Object visit(ThisExpression node) {
        return visit(_convertThisToName(node));
      }
    };
  }
  
  /**
   * Factory method to make a new TypeChecker that treats "this" as a variable.
   * @param context the context
   * @return visitor the visitor
   */
  public TypeChecker makeTypeChecker(Context context) {
    return new TypeChecker(context) {
      public Object visit(ThisExpression node) {
        return visit(_convertThisToName(node));
      }
    };
  }
  
  /**
   * Factory method to make a new DebugEvaluationVisitor that treats "this"
   * as a variable.
   * @param context the context
   * @return visitor the visitor
   */
  public EvaluationVisitor makeEvaluationVisitor(Context context) {
    //return new DebugEvaluationVisitorExtension(context, _name);
    return new EvaluationVisitorExtension(context) {
      public Object visit(ThisExpression node) {
        return visit(_convertThisToName(node));
      }
    };
  }
}