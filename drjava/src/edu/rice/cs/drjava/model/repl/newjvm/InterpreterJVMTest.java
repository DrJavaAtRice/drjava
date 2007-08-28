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

package edu.rice.cs.drjava.model.repl.newjvm;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.repl.DynamicJavaAdapter;
import edu.rice.cs.drjava.model.repl.ExceptionReturnedException;
import edu.rice.cs.drjava.model.repl.JavaInterpreter;

import java.util.Hashtable;

/**
 * simple test suite over InterpreterJVM
 */
public final class InterpreterJVMTest extends DrJavaTestCase {
  private InterpreterJVM jvm = InterpreterJVM.ONLY;
  private Hashtable<String, InterpreterData> _debugInterpreters;
  private JavaInterpreter _interpreter1;
  private JavaInterpreter _interpreter2;
  private JavaInterpreter _interpreter3;
  
  private void _addInterpreter(String name, JavaInterpreter interpreter) {
    InterpreterJVM.ONLY.addInterpreter(name, interpreter);
  }
  
  public void setUp() throws Exception {
    super.setUp();
    _debugInterpreters = InterpreterJVM.ONLY.getInterpreters();
    _interpreter1 = new DynamicJavaAdapter(new ClassPathManager());
    _interpreter2 = new DynamicJavaAdapter(new ClassPathManager());
    _interpreter3 = new DynamicJavaAdapter(new ClassPathManager());
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
