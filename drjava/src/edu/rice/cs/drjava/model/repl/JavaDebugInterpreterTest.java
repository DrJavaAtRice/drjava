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

import junit.framework.TestCase;

import koala.dynamicjava.tree.*;
import koala.dynamicjava.interpreter.*;
import koala.dynamicjava.interpreter.context.*;

/**
 * Class to test JavaDebugInterpreters by ensuring that appropriate
 * events are generated on each assignment.
 * 
 * NOTE: These tests are disabled for now, since nothing needs to be done
 * on an assignment.  (We just copy back when the thread is resumed.)
 * 
 * @version $Id$
 */
public final class JavaDebugInterpreterTest extends TestCase {
  private JavaDebugInterpreter _debugInterpreter;
  
  private String _assignedInterpreterName;
  
  public void setUp() {
    // Creating a JavaDebugInterpreter with a custom 
    // notifyInterpreterAssignment() method
    _debugInterpreter = new JavaDebugInterpreter("test") {
      public EvaluationVisitorExtension makeEvaluationVisitor(Context context) {
        return new DebugEvaluationVisitor(context, _name) {
          protected void _notifyAssigned(Expression e) {
            notifyInterpreterAssignment(_name);
          }
        };
      }
    };
    _assignedInterpreterName = "";
  }
  
  public void notifyInterpreterAssignment(String name) {
    _assignedInterpreterName = name;
  }

  /**
   * Disabled...
   */
  public void testNoAssignment() throws ExceptionReturnedException {
    /*
    // 1
    _debugInterpreter.interpret("1 + 1");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);
    
    // 2
    _debugInterpreter.interpret("public void foo() {}; foo()");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);
    
    // 3
    _debugInterpreter.interpret("int x");
    assertEquals("Should not have made an assignment.", "", _assignedInterpreterName);
    */
  }
  
  /**
   * Disabled...
   * 
  public void testWithAssignment() throws ExceptionReturnedException {
    // 1
    _debugInterpreter.interpret("x = 0");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";
    
    // 2
    _debugInterpreter.interpret("y = null");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";
    
    // 3
    _debugInterpreter.interpret("int z; z = 2");
    assertEquals("Should have made an assignment.", "test", _assignedInterpreterName);
    _assignedInterpreterName = "";
  } */
}
