/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.io.IOUtil;
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
public final class NewJVMTest extends DrJavaTestCase  {
  private static final Log _log  = new Log("MasterSlave.txt", false);
  
  private static volatile TestJVMExtension _jvm;
  
  public NewJVMTest(String name) throws RemoteException { 
    super(name); 
    _jvm = new TestJVMExtension();
  }

//  public static Test suite() {
//    TestSuite suite = new TestSuite(NewJVMTest.class);
//    TestSetup setup = new TestSetup(suite) {
//      protected void setUp() throws Exception { 
//        super.setUp();
//        _jvm = new TestJVMExtension(); 
//      }
//      protected void tearDown() throws Exception { _jvm.dispose(); }
//    };
//
//    return setup;
//  }


  public void testPrintln() throws Throwable {
    _log.log("$$$ NewJVMTest.testPrintln executing");
    
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("System.err.print(\"err\");"));
    assertEquals("system err buffer", "err", _jvm.errBuf());
    assertEquals("void return flag", true, _jvm.voidReturnFlag());

    _jvm.resetFlags();
    assertTrue(_jvm.interpret("System.err.print(\"err2\");"));
    assertEquals("system err buffer", "err2", _jvm.errBuf());
    assertEquals("void return flag", true, _jvm.voidReturnFlag());

    _jvm.resetFlags();
    assertTrue(_jvm.interpret("System.out.print(\"out\");"));
    assertEquals("system out buffer", "out", _jvm.outBuf());
    assertEquals("void return flag", true, _jvm.voidReturnFlag());
    
    _log.log("$$$ NewJVMTest.testPrintln completed"); 
  }

  public void testReturnConstant() throws Throwable {
   _log.log("$$ NewJVMTest.testReturnConstant executing");

   _jvm.resetFlags();
   assertTrue(_jvm.interpret("5"));
   assertEquals("result", "5", _jvm.returnBuf());

    _log.log("$$$ NewJVMTest.testReturnConstant completed");
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    _log.log("$$$ NewJVMTest.testWorksAfterRestartConstant executing");

    // Check that a constant is returned
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("5"));
    assertEquals("result", "5", _jvm.returnBuf());
    
    // Now restart interpreter
    _jvm.restartInterpreterJVM(true);

    // Now evaluate another constant
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("4"));
    assertEquals("result", "4", _jvm.returnBuf());
    
    _log.log("$$$ NewJVMTest.testWorksAfterRestartConstant completed");
  }


  public void testThrowRuntimeException() throws Throwable {
    _log.log("$$$ NewJVMTest.testThrowRuntimeException executing");
    
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("throw new RuntimeException();"));
    assertTrue("exception message", _jvm.exceptionMsgBuf().startsWith("java.lang.RuntimeException"));

    _log.log("$$$ NewJVMTest.testThrowRuntimeException completed");
  }

  public void testToStringThrowsRuntimeException() throws Throwable {
    _log.log("$$$ NewJVMTest.testToStringThrowsRuntimeException executing");
    
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("class A { public String toString() { throw new RuntimeException(); } };" +
                              "new A()"));
    assertTrue("exception should have been thrown by toString",
               _jvm.exceptionMsgBuf() != null);
    
    _log.log("$$$ NewJVMTest.testToStringThrowsRuntimeException completed");
  }

  /** Ensure that switching to a non-existant interpreter throws an Exception.
   */
  public void testSwitchToNonExistantInterpreter() {
    _log.log("$$$ NewJVMTest.testSwitchToNonExistantInterpreter executing");
    try {
      _jvm.setActiveInterpreter("thisisabadname");
//      System.err.println("outbuf: " + _jvm.outBuf);
      fail("Should have thrown an exception!");
    }
    catch (IllegalArgumentException e) {
      // good, that's what should happen
    }
    _log.log("$$$ NewJVMTest.testSwitchToNonExistantInterpreter completed");
  }

  /** Ensure that MainJVM can correctly switch the active interpreter used by
   * the interpreter JVM.
   * @throws InterruptedException if execution was interrupted unexpectedly
   */
  public void testSwitchActiveInterpreter() throws InterruptedException {
    _log.log("$$$ NewJVMTest.testSwitchActiveInterpreter executing");
    
    assertTrue(_jvm.interpret("int x = 6;"));
    _jvm.addInterpreter("monkey");

    // x should be defined in active interpreter
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("x"));
    assertEquals("result", "6", _jvm.returnBuf());

    // switch interpreter
    _jvm.setActiveInterpreter("monkey");
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("x"));
    assertNotNull("exception was thrown", _jvm.exceptionMsgBuf());

    // define x to 3 and switch back
    assertTrue(_jvm.interpret("int x = 3;"));
    _jvm.setToDefaultInterpreter();

    // x should have its old value
    _jvm.resetFlags();
    assertTrue(_jvm.interpret("x"));
    assertEquals("result", "6", _jvm.returnBuf());

    // test syntax error handling
    //  (temporarily disabled until bug 750605 fixed)
//       _jvm.interpret("x + ");
//       assertTrue("syntax error was reported",
//                  ! _jvm.syntaxErrorMsgBuf.equals("") );
    _log.log("$$$ NewJVMTest.testSwitchActiveInterpreter executing");
  }

  private static class TestJVMExtension extends MainJVM {
    private static final int WAIT_TIMEOUT = 30000; // time to wait for an interaction to complete
    
    private final CompletionMonitor _done;
    private volatile String _outBuf;
    private volatile String _errBuf;
    private volatile String _returnBuf;
    private volatile String _exceptionMsgBuf;
    private volatile boolean _voidReturnFlag;

    private volatile InterpretResult.Visitor<Void> _testHandler;

    public TestJVMExtension() throws RemoteException {
      super(IOUtil.WORKING_DIRECTORY);
      _done = new CompletionMonitor();
      _testHandler = new TestResultHandler();
      startInterpreterJVM();
      resetFlags();
    }

    @Override protected InterpretResult.Visitor<Void> resultHandler() {
      return _testHandler;
    }

    public void resetFlags() {
      _done.reset();
      _outBuf = "";
      _errBuf = "";
      _returnBuf = null;
      _exceptionMsgBuf = null;
      _voidReturnFlag = false;
    }
    
    public String outBuf() {
      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _outBuf;
    }

    public String errBuf() {
      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _errBuf;
    }

    public String returnBuf() {
      try {
      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _returnBuf;
      }
      finally { debug.logValue("_returnBuf", _returnBuf); }
    }

    public String exceptionMsgBuf() {
      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _exceptionMsgBuf;
    }

    public boolean voidReturnFlag() {
      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _voidReturnFlag;
    }

    public void systemErrPrint(String s) { _errBuf += s; }
    public void systemOutPrint(String s) { _outBuf += s; }

    private class TestResultHandler implements InterpretResult.Visitor<Void> {
      public Void forNoValue() {
        debug.log();
        _voidReturnFlag = true;
        _done.signal();
        _log.log("NewJVMTest: void returned by interpretResult callback");
        return null;
      }
      public Void forStringValue(String s) { handleValueResult('"' + s + '"'); return null; }
      public Void forCharValue(Character c) { handleValueResult("'" + c + "'"); return null; }
      public Void forNumberValue(Number n) { handleValueResult(n.toString()); return null; }
      public Void forBooleanValue(Boolean b) { handleValueResult(b.toString()); return null; }
      public Void forObjectValue(String objString, String objTypeString) { handleValueResult(objString); return null; }
      
      private void handleValueResult(String s) {
        debug.log();
        _returnBuf = s;
        _done.signal();
        _log.log("NewJVMTest: " + _returnBuf + " returned by interpretResult callback");
      }
      
      public Void forEvalException(String message, StackTraceElement[] stackTrace) {
        debug.log();
        StringBuilder sb = new StringBuilder(message);
        for(StackTraceElement ste: stackTrace) {
          sb.append("\n\tat ");
          sb.append(ste);
        }
        _exceptionMsgBuf = sb.toString().trim();
        _done.signal();
        return null;
      }
      
      public Void forException(String message) {
        debug.log();
        _exceptionMsgBuf = message;
        _done.signal();
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
