/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.io.StringWriter;
import java.io.PrintWriter;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.FileOption;

import edu.rice.cs.dynamicjava.interpreter.InterpreterException;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.rmi.RemoteException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Tests the functionality of the new JVM manager.
 *  @version $Id$
 */
public final class NewJVMTest extends DrJavaTestCase {
  private static final Log _log  = new Log("MasterSlave.txt", false);
  
  private static volatile TestJVMExtension _jvm;
  
  /* Lock used to prevent interpreter transactions from interfering with one another. */
  private final static Object _testLock = new Object();
  
  public NewJVMTest(String name) { super(name); }

  protected void setUp() throws Exception {
    super.setUp();
    _jvm.resetFlags();
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(NewJVMTest.class);
    TestSetup setup = new TestSetup(suite) {
      protected void setUp() throws Exception { _jvm = new TestJVMExtension(); }
      protected void tearDown() throws Exception { _jvm.killInterpreter(null); }
    };

    return setup;
  }


  public void testPrintln() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testPrintln executing");
    
    _jvm.interpret("System.err.print(\"err\");");
    assertEquals("system err buffer", "err", _jvm.errBuf);
    //assertEquals("void return flag", true, _jvm.voidReturnFlag);
    _jvm.resetFlags();

    _jvm.interpret("System.err.print(\"err2\");");
    assertEquals("system err buffer", "err2", _jvm.errBuf);
    //assertEquals("void return flag", true, _jvm.voidReturnFlag);
    _jvm.resetFlags();

    _jvm.interpret("System.out.print(\"out\");");
    assertEquals("system out buffer", "out", _jvm.outBuf);
    //assertEquals("void return flag", true, _jvm.voidReturnFlag);
    
    debug.logEnd();
  }

  public void testReturnConstant() throws Throwable {
    debug.logStart();
   _log.log("NewJVMTest.testReturnConstant executing");

   _jvm.interpret("5");
   assertEquals("result", "5", _jvm.returnBuf);

   debug.logEnd();
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testWorksAfterRestartConstant executing");

    // Check that a constant is returned
    _jvm.interpret("5");
    assertEquals("result", "5", _jvm.returnBuf);
    
    // Now restart interpreter
    synchronized(_testLock) {
      _jvm.killInterpreter(FileOption.NULL_FILE);  // "" is not null: start back up
      _testLock.wait();
    }

    // Now evaluate another constant
    _jvm.interpret("4");
    assertEquals("result", "4", _jvm.returnBuf);
    
    debug.logEnd();
  }


  public void testThrowRuntimeException() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testThrowRuntimeException executing");
    
    _jvm.interpret("throw new RuntimeException();");
    assertTrue("exception message", _jvm.exceptionMsgBuf.startsWith("java.lang.RuntimeException"));
    // TODO: eliminate the unnecessary stack trace from the error message (replacing with an
    // accurate stack trace from the interpreted language's point of view)

    debug.logEnd();
  }

  public void testToStringThrowsRuntimeException() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testToStringThrowsRuntimeException executing");
    
    _jvm.interpret("class A { public String toString() { throw new RuntimeException(); } };" +
                   "new A()");
    assertTrue("exception should have been thrown by toString",
               _jvm.exceptionMsgBuf != null);
    
    debug.logEnd();
  }

  /**
   * Ensure that switching to a non-existant interpreter throws an Exception.
   */
  public void testSwitchToNonExistantInterpreter() {
    debug.logStart();
    try {
      _jvm.setActiveInterpreter("thisisabadname");
      System.out.println("outbuf: " + _jvm.outBuf);
      fail("Should have thrown an exception!");
    }
    catch (IllegalArgumentException e) {
      // good, that's what should happen
    }
    debug.logEnd();
  }

  /**
   * Ensure that MainJVM can correctly switch the active interpreter used by
   * the interpreter JVM.
   */
  public void testSwitchActiveInterpreter() throws InterruptedException {
    debug.logStart();
    
    _jvm.interpret("int x = 6;");
    _jvm.addInterpreter("monkey");

    // x should be defined in active interpreter
    _jvm.interpret("x");
    assertEquals("result", "6", _jvm.returnBuf);

    // switch interpreter
    _jvm.setActiveInterpreter("monkey");
    _jvm.interpret("x");
    assertNotNull("exception was thrown", _jvm.exceptionMsgBuf);

    // define x to 3 and switch back
    _jvm.interpret("int x = 3;");
    _jvm.setToDefaultInterpreter();

    // x should have its old value
    _jvm.interpret("x");
    assertEquals("result", "6", _jvm.returnBuf);

    // test syntax error handling
    //  (temporarily disabled until bug 750605 fixed)
//       _jvm.interpret("x+");
//       assertTrue("syntax error was reported",
//                  ! _jvm.syntaxErrorMsgBuf.equals("") );
//     }
    debug.logEnd();
  }

  private static class TestJVMExtension extends MainJVM {
    public volatile String outBuf;
    public volatile String errBuf;
    public volatile String returnBuf;
    public volatile String exceptionMsgBuf;
    public volatile boolean voidReturnFlag;

    private volatile InterpretResult.Visitor<Void> _testHandler;

    public TestJVMExtension() throws RemoteException {
      super(null);
      _testHandler = new TestResultHandler();
      startInterpreterJVM();
      ensureInterpreterConnected();
    }

    protected InterpretResult.Visitor<Void> getResultHandler() {
      return _testHandler;
    }

    public void resetFlags() {
      outBuf = null;
      errBuf = null;
      returnBuf = null;
      exceptionMsgBuf = null;
      voidReturnFlag = false;
    }

    protected void handleSlaveQuit(int status) {
      synchronized(_testLock) {
        _testLock.notify();
        super.handleSlaveQuit(status);
      }
    }

    public void systemErrPrint(String s) throws RemoteException {
      errBuf = s;
    }

    public void systemOutPrint(String s) throws RemoteException {
      outBuf = s;
    }

    private class TestResultHandler implements InterpretResult.Visitor<Void> {
      public Void forNoValue() {
        debug.log();
        voidReturnFlag = true;
        _log.log("NewJVMTest: void returned by interpretResult callback");
        return null;
      }
      public Void forStringValue(String s) { handleValueResult('"' + s + '"'); return null; }
      public Void forCharValue(Character c) { handleValueResult("'" + c + "'"); return null; }
      public Void forNumberValue(Number n) { handleValueResult(n.toString()); return null; }
      public Void forBooleanValue(Boolean b) { handleValueResult(b.toString()); return null; }
      public Void forObjectValue(String objString) { handleValueResult(objString); return null; }
      
      private void handleValueResult(String s) {
        debug.log();
        returnBuf = s;
        _log.log("NewJVMTest: " + returnBuf + " returned by interpretResult callback");
      }
      
      public Void forException(String msg) {
        exceptionMsgBuf = msg;
        return null;
      }
      
      public Void forUnexpectedException(Throwable t) {
        debug.log();
        throw new UnexpectedException(t);
      }

      public Void forBusy() {
        debug.log();
        throw new UnexpectedException("MainJVM.interpret called when interpreter was busy!");
      }
    }
  }
}
