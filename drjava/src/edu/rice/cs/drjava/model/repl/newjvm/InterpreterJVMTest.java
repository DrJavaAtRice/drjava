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

package edu.rice.cs.drjava.model.repl.newjvm;

import junit.framework.TestCase;
import edu.rice.cs.drjava.model.repl.*;
import java.util.Hashtable;

/**
 * simple test suite over InterpreterJVM
 */
public final class InterpreterJVMTest extends TestCase {
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
    _interpreter1 = new DynamicJavaAdapter(new ClasspathManager());
    _interpreter2 = new DynamicJavaAdapter(new ClasspathManager());
    _interpreter3 = new DynamicJavaAdapter(new ClasspathManager());
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
