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

package edu.rice.cs.drjava.model.repl.newjvm;

import junit.framework.TestCase;
import edu.rice.cs.drjava.model.repl.*;
import gj.util.Hashtable;

/**
 * simple test suite over InterpreterJVM
 */
public class InterpreterJVMTest extends TestCase {
  private InterpreterJVM jvm = InterpreterJVM.ONLY;
  private Hashtable<String,InterpreterData> _debugInterpreters;
  private JavaInterpreter _interpreter1;
  private JavaInterpreter _interpreter2;
  private JavaInterpreter _interpreter3;
  
  private void _addInterpreter(String name, JavaInterpreter interpreter) {
    InterpreterJVM.ONLY.addInterpreter(name, interpreter);
  }
  
  public void setUp() {
    _debugInterpreters = InterpreterJVM.ONLY.getInterpreters();
    _interpreter1 = new DynamicJavaAdapter();
    _interpreter2 = new DynamicJavaAdapter();
    _interpreter3 = new DynamicJavaAdapter();
  }
  /**
   * ensures that our InterpreterJVM adds named debug interpreters correctly
   */
  public void testAddNamedDebugInterpreter() {
    assertTrue(_debugInterpreters.isEmpty());
    _addInterpreter("interpreter1", _interpreter1);
    assertSame(_interpreter1, _debugInterpreters.get("interpreter1").getInterpreter());
    assertTrue(!_debugInterpreters.containsKey("interpreter2"));
    
    _addInterpreter("interpreter2", _interpreter2);
    assertSame(_interpreter1, _debugInterpreters.get("interpreter1").getInterpreter());
    assertSame(_interpreter2, _debugInterpreters.get("interpreter2").getInterpreter());
    
    try {
      _addInterpreter("interpreter1", _interpreter3);
      fail();
    }
    catch (IllegalArgumentException ex) {
      assertSame(_interpreter1, _debugInterpreters.get("interpreter1").getInterpreter());
      assertSame(_interpreter2, _debugInterpreters.get("interpreter2").getInterpreter());
    }
  }
  
  /**
   * verifies that the InterpreterJVM can switch between active interpreters.
   */
  public void testSwitchingActiveInterpreter() throws ExceptionReturnedException {
    String var0 = "stuff";
    String var1 = "junk";
    String var2 = "raargh";
    Object val0 = new Byte("5");
    Object val1 = new Short("2");
    Object val2 = new Long(2782);
    _addInterpreter("1",_interpreter1);
    _addInterpreter("2",_interpreter2);
    
    JavaInterpreter interpreter = (JavaInterpreter) jvm.getActiveInterpreter();
    interpreter.defineVariable(var0, val0);
    assertEquals(val0, interpreter.interpret(var0));

    jvm.setActiveInterpreter("1");
    interpreter = (JavaInterpreter) jvm.getActiveInterpreter();
    try {
      interpreter.interpret(var0);
      fail();
    }
    catch (ExceptionReturnedException ex) {
      // correct behavior -- var0 should not be defined
    }
    interpreter.defineVariable(var1,val1);
    assertEquals(val1, interpreter.interpret(var1));
    
    jvm.setActiveInterpreter("2");
    interpreter = (JavaInterpreter) jvm.getActiveInterpreter();
    try {
      interpreter.interpret(var0);
      fail();
    }
    catch (ExceptionReturnedException ex) {
    }
    try {
      interpreter.interpret(var1);
      fail();
    }
    catch (ExceptionReturnedException ex) {
      // correct behavior -- var0 & var1 should not be defined
    }
    interpreter.defineVariable(var2,val2);
    assertEquals(val2, interpreter.interpret(var2));

    jvm.setToDefaultInterpreter();
    interpreter = (JavaInterpreter) jvm.getActiveInterpreter();
    try {
      interpreter.interpret(var1);
      fail();
    }
    catch (ExceptionReturnedException ex) {
    }
    try {
      interpreter.interpret(var2);
      fail();
    }
    catch (ExceptionReturnedException ex) {
      // correct behavior -- var1 & var2 should not be defined
    }
    assertEquals(val0, jvm.getActiveInterpreter().interpret(var0));

    jvm.setActiveInterpreter("1");
    interpreter = (JavaInterpreter) jvm.getActiveInterpreter();
    try {
      interpreter.interpret(var0);
      fail();
    }
    catch (ExceptionReturnedException ex) {
    }
    try {
      interpreter.interpret(var2);
      fail();
    }
    catch (ExceptionReturnedException ex) {
      // correct behavior -- var1 & var2 should not be defined
    }
    assertEquals(val1, interpreter.interpret(var1));

    try {
      jvm.setActiveInterpreter("not an interpreter");
      fail();
    }
    catch (IllegalArgumentException ex) {
      assertEquals("Interpreter 'not an interpreter' does not exist.", ex.getMessage());
    }
  }
}