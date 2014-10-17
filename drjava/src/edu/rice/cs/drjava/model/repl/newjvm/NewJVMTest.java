/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
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

import java.io.StringWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import java.util.Collections;
import java.lang.StringBuffer;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Tests the functionality of the new JVM manager.
 *  @version $Id: NewJVMTest.java 5668 2012-08-15 04:58:30Z rcartwright $
 */
public final class NewJVMTest extends DrJavaTestCase {
  private static final Log _log  = new Log("NewJVMTest.txt", false);
  
  private static volatile TestJVMExtension _jvm;
  
  public NewJVMTest(String name) { super(name); }

  public static Test suite() {
    TestSuite suite = new TestSuite(NewJVMTest.class);
    TestSetup setup = new TestSetup(suite) {
      protected void setUp() throws Exception { 
        super.setUp();
        _log.log("Executing setUp()");
        _jvm = new TestJVMExtension();
      }
      protected void tearDown() throws Exception { 
        _log.log("Executing tearDown()");
        _jvm.dispose(); 
      }
    };

    return setup;
  }

  public void testPrintln() throws Throwable {
    /* BEWARE: in Scala, System.out.println and System.err.println print Windows style newline strings. */
    debug.logStart();
    _log.log("NewJVMTest.testPrintln executing");
    
    // How do you write to System.err in Scala?  As we have configured the Scala interpreter you don't; it goes to the 
    // same output stream as System.out.
    _jvm.resetState();
    assertTrue(_jvm.interpret("System.err.print(\"err\")"));
    assertEquals("err message length", 3, _jvm.errBuffer().length());
    assertEquals("system err buffer", "err", _jvm.errBuffer());
//    assertEquals("void return flag", true, _jvm.voidReturnFlag());
//
//    _jvm.resetState();
//    assertTrue(_jvm.interpret("System.err.print(\"err2\");"));
//    assertEquals("system err buffer", "err2", _jvm.errBuffer());
//    assertEquals("void return flag", true, _jvm.voidReturnFlag());

    _jvm.resetState();
    assertTrue(_jvm.interpret("print(\"out\");"));
    assertEquals("out buffer length", 3, _jvm.outBuffer().length());
    assertEquals("system out buffer", "out", _jvm.outBuffer());
//    assertEquals("void return flag", true, _jvm.voidReturnFlag());
    
    debug.logEnd();
  }

  public void testReturnConstant() throws Throwable {
    debug.logStart();
   _log.log("NewJVMTest.testReturnConstant executing");
   
   _jvm.resetState();
//   String banner = _jvm.returnBuffer();
//   _log.log("Returned banned = ' + " + banner + "'");
   assertTrue(_jvm.interpret("val x = 5"));
   assertEquals("result", "x: Int = 5\n", _jvm.returnBuffer());
   debug.logEnd();
  }

  public void testSimpleIfExpression() throws Throwable {
    debug.logStart();
   _log.log("NewJVMTest.testSimpleIfExpression executing");
   
   _jvm.resetState();
//   String banner = _jvm.returnBuffer();
//   _log.log("Returned banned = ' + " + banner + "'");
   assertTrue(_jvm.interpret("if (true)"));
   assertEquals("result", "     | ", _jvm.returnBuffer());
   assertTrue(_jvm.interpret("print(\"out\");"));
   assertEquals("out buffer length", 3, _jvm.outBuffer().length());
   assertEquals("system out buffer", "out", _jvm.outBuffer());
   debug.logEnd();
  }

  public void testIfElseExpression() throws Throwable {
    debug.logStart();
   _log.log("NewJVMTest.testIfElseExpression executing");
   
   _jvm.resetState();
//   String banner = _jvm.returnBuffer();
//   _log.log("Returned banned = ' + " + banner + "'");
   assertTrue(_jvm.interpret("if (false)"));
   assertEquals("result", "     | ", _jvm.returnBuffer());
   _jvm.resetState();
   assertTrue(_jvm.interpret("print(\"wrong\"); else"));
   assertEquals("result", "     | ", _jvm.returnBuffer());
   _jvm.resetState();
   assertTrue(_jvm.interpret("print(\"right\")"));
   assertEquals("out buffer length", 5, _jvm.outBuffer().length());
   assertEquals("system out buffer", "right", _jvm.outBuffer());
   debug.logEnd();
  }

  public void testForLoopExpression() throws Throwable {
    debug.logStart();
   _log.log("NewJVMTest.testForLoopExpression executing");
   _jvm.resetState();
//   String banner = _jvm.returnBuffer();
//   _log.log("Returned banned = ' + " + banner + "'");
   assertTrue(_jvm.interpret("for (i <- 1 to 10)"));
   assertEquals("result", "     | ", _jvm.returnBuffer());
   _jvm.resetState();
   assertTrue(_jvm.interpret("if ((i % 2) == 0)"));
   assertEquals("result", "     | ", _jvm.returnBuffer());
   assertTrue(_jvm.interpret("print(i)"));
   assertEquals("out buffer length", 6, _jvm.outBuffer().length());
   assertEquals("system out buffer", "246810", _jvm.outBuffer());
   debug.logEnd();
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testWorksAfterRestartConstant executing");

    // Check that a constant is returned
    _jvm.resetState();

    assertTrue(_jvm.interpret("val x = 5"));
//    String banner = _jvm.returnBuffer();
//    _log.log("Returned banner = ' + " + banner + "'");
    assertEquals("result", "x: Int = 5\n", _jvm.returnBuffer());
    
    // Now restart interpreter
    _jvm.restartInterpreterJVM(true);
       
    // Now evaluate another constant
    _jvm.resetState();
    assertTrue(_jvm.interpret("val x = 4"));
    assertEquals("result", "x: Int = 4\n", _jvm.returnBuffer());
    
    debug.logEnd();
  }


  public void testThrowRuntimeException() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testThrowRuntimeException executing");
    
    _jvm.resetState();
    assertTrue(_jvm.interpret("throw new RuntimeException();"));
    assertTrue("exception message", _jvm.returnBuffer().startsWith("java.lang.RuntimeException"));

    debug.logEnd();
  }

  public void testToStringThrowsRuntimeException() throws Throwable {
    debug.logStart();
    _log.log("NewJVMTest.testToStringThrowsRuntimeException executing");

    _jvm.resetState();
    assertTrue(_jvm.interpret("class A { override def toString() = { throw new RuntimeException(); } };" +
                              "new A()"));
    String result = _jvm.returnBuffer();
//    System.err.println("returnBuffer() returns " + result);
    assertTrue("exception should have been thrown by toString",
               result.contains("java.lang.RuntimeException"));
    
    debug.logEnd();
  }

  /** Ensure that switching to a non-existant interpreter throws an Exception. */
  public void testSwitchToNonExistantInterpreter() {
    debug.logStart();
    
    try {
      _jvm.setActiveInterpreter("thisisabadname");
//      System.err.println("outbuf: " + _jvm.outBuffer);
      fail("Should have thrown an exception!");
    }
    catch (IllegalArgumentException e) {
      // good, that's what should happen
    }
    debug.logEnd();
  }

  /* The following method is commented out because switching interpreters apparently hangs the Interpreter JVM. */
  /** Ensure that MainJVM can correctly switch the active interpreter used by
   * the interpreter JVM.
   */
  public void xtestSwitchActiveInterpreter() throws InterruptedException {
    debug.logStart();
       
    assertTrue(_jvm.interpret("val x = 6;"));
    _jvm.addInterpreter("monkey");

    // x should be defined in active interpreter
    _jvm.resetState();
    assertTrue(_jvm.interpret("val y = x"));
    assertEquals("result", "y: Int = 6\n", _jvm.returnBuffer());

    // switch interpreter
//    _jvm.setActiveInterpreter("monkey");
//    _jvm.resetState();
//    assertTrue(_jvm.interpret("x"));
//    assertTrue(_jvm.returnBuffer().contains("error"));
//
//    // define x to 3 and switch back
//    assertTrue(_jvm.interpret("val x = 3"));
//    _jvm.setToDefaultInterpreter();
//
//    // x should have its old value
//    _jvm.resetState();
//    assertTrue(_jvm.interpret("val y = x"));
//    assertEquals("result", "y: Int = 5\n", _jvm.returnBuffer());
//
//    // test syntax error handling
//    //  (temporarily disabled until bug 750605 fixed)
////       _jvm.interpret("x + ");
////       assertTrue("syntax error was reported",
////                  ! _jvm.syntaxErrorMsgBuf.equals("") );
////     }
    debug.logEnd();
  }

  private static class TestJVMExtension extends MainJVM {
    private static final int WAIT_TIMEOUT = 30000; // time to wait for an interaction to complete
    
    private volatile CompletionMonitor _done;
    private volatile StringBuffer _outBuffer;
    private volatile StringBuffer _errBuffer;
    private volatile StringBuffer _returnBuffer;
    private volatile StringBuffer _exceptionMsgBuffer;
    private volatile boolean _voidReturnFlag;

    private volatile InterpretResult.Visitor<Void> _testHandler;

    public TestJVMExtension() throws RemoteException {
      super(IOUtil.WORKING_DIRECTORY);
      _done = new CompletionMonitor();
      _testHandler = new TestResultHandler();
      startInterpreterJVM();
      resetState();
    }

    @Override protected InterpretResult.Visitor<Void> resultHandler() {
      return _testHandler;
    }

    /** Resets the state of this TestJVMExtension.  Only called within TestJVM. */
    public void resetState() { 
      _done.signal();  // Release any blocked attempts to read _outBuffer or _errBuffer 
      _done = new CompletionMonitor();
      _outBuffer = new StringBuffer();
      _errBuffer = new StringBuffer();
      _returnBuffer = new StringBuffer();
      _exceptionMsgBuffer = new StringBuffer();
      _voidReturnFlag = false;
    }
    
    public String outBuffer() {
//      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _outBuffer.toString();
    }

    public String errBuffer() {
//      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _errBuffer.toString();
    }

    public String returnBuffer() {
//      try {
//        assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
        return _returnBuffer.toString();
//      }
//      finally { debug.logValue("_returnBuffer", _returnBuffer); }
    }
    
    public String exceptionMsgBuf() {
//      assertTrue(_done.attemptEnsureSignaled(WAIT_TIMEOUT));
      return _exceptionMsgBuffer.toString();
    }
    
    public boolean voidReturnFlag() {
      /* copy current state */
      return _voidReturnFlag;
    }
    
    public void systemErrPrint(String s) {
      _log.log("systemErrPrint(" + '"' + s + '"' + ") in NewJVMTest");
      _errBuffer.append(s);
    }
    
    public void systemOutPrint(String s) {
      _log.log("systemOutPrint("   + '"' + s  + '"' +") in NewJVMTest");
      _outBuffer.append(s); 
    }
    
    /* InterpretResult handler for this test.  Note that Scala only creates StringValue results so most of
     * the methods in this visitor interface can never be called. */
    private class TestResultHandler implements InterpretResult.Visitor<Void> {
      public Void forNoValue() {
        debug.log();
        _voidReturnFlag = true;
        _done.signal();
        _log.log("NewJVMTest: void returned by interpretResult callback");
        return null;
      }
      public Void forStringValue(String s) { handleValueResult(s); return null; }
      public Void forCharValue(Character c) { handleValueResult(c.toString()); return null; }
      public Void forNumberValue(Number n) { handleValueResult(n.toString()); return null; }
      public Void forBooleanValue(Boolean b) { handleValueResult(b.toString()); return null; }
      public Void forObjectValue(String objString, String objTypeString) { handleValueResult(objString); return null; }
      
      private void handleValueResult(String s) {
        debug.log();
        _returnBuffer.append(s);
        _done.signal();
        _log.log("NewJVMTest: " + _returnBuffer + " returned by interpretResult callback");
      }
      
      public Void forEvalException(String message, StackTraceElement[] stackTrace) {
        debug.log();
        StringBuilder sb = new StringBuilder(message);
        for(StackTraceElement ste: stackTrace) {
          sb.append("\n\tat ");
          sb.append(ste);
        }
        _exceptionMsgBuffer.append(sb.toString().trim());
        _done.signal();
        _log.log("NewJVMTest: interpreterResult callback produced EvalException:" + message);
        return null;
      }
      
      public Void forException(String message) {
        debug.log();
        _exceptionMsgBuffer.append(message);
        _done.signal();
        _log.log("NewJVMTest: interpreterResult callback produced Exception:" + message);
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
