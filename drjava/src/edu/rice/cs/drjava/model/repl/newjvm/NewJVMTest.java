/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.FileOption;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.rmi.RemoteException;

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
      protected void setUp() throws Exception {
        _jvm = new TestJVMExtension();
      }

      protected void tearDown() throws Exception { _jvm.killInterpreter(null); }
    };

    return setup;
  }


  public void testPrintln() throws Throwable {
    _log.log("NewJVMTest.testPrintln executing");
    synchronized(_testLock) {
      _jvm.interpret("System.err.print(\"err\");");
      _testLock.wait(); // wait for println
//      _testLock.wait(); // wait for void return
      assertEquals("system err buffer", "err", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_testLock) {
      _jvm.interpret("System.err.print(\"err2\");");
      _testLock.wait(); // wait for println
//      _testLock.wait(); // wait for void return
      assertEquals("system err buffer", "err2", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_testLock) {
      _jvm.interpret("System.out.print(\"out\");");
      _testLock.wait(); // wait for println
//      _testLock.wait(); // wait for void return
      assertEquals("system out buffer", "out", _jvm.outBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
    }
  }

  public void testReturnConstant() throws Throwable {
   _log.log("NewJVMTest.testReturnConstant executing");
    synchronized(_testLock) {
      _jvm.interpret("5");
      _testLock.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    _log.log("NewJVMTest.testWorksAfterRestartConstant executing");

    // Check that a constant is returned
    synchronized(_testLock) {
      _jvm.interpret("5");
      _testLock.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }

    // Now restart interpreter
    synchronized(_testLock) {
      _jvm.killInterpreter(FileOption.NULL_FILE);  // "" is not null: start back up
      _testLock.wait();
    }

    // Now evaluate another constant
    synchronized(_testLock) {
      _jvm.interpret("4");
      _testLock.wait();
      assertEquals("result", "4", _jvm.returnBuf);
    }
  }


  public void testThrowRuntimeException() throws Throwable {
    _log.log("NewJVMTest.testThrowRuntimeException executing");
    synchronized(_testLock) {
      _jvm.interpret("throw new RuntimeException();");
      _testLock.wait();
      assertEquals("exception class", "java.lang.RuntimeException", _jvm.exceptionClassBuf);
    }
  }

  public void testToStringThrowsRuntimeException() throws Throwable {
    _log.log("NewJVMTest.testToStringThrowsRuntimeException executing");
    synchronized(_testLock) {
      _jvm.interpret(
        "class A { public String toString() { throw new RuntimeException(); } };" +
        "new A()");
      _testLock.wait();
      assertTrue("exception should have been thrown by toString",
                 _jvm.exceptionClassBuf != null);
    }
  }

  public void testThrowNPE() throws Throwable {
    _log.log("NewJVMTest.testThrowNPE executing");
    synchronized(_testLock) {
      _jvm.interpret("throw new NullPointerException();");

      while (_jvm.exceptionClassBuf == null) {
        _testLock.wait();
      }

      assertEquals("exception class",
                   "java.lang.NullPointerException",
                   _jvm.exceptionClassBuf);
    }
  }

  public void testStackTraceEmptyTrace() throws Throwable {
    _log.log("NewJVMTest.testStackTraceEmptyTrace executing");
    synchronized(_testLock) {
      _jvm.interpret("null.toString()");

      while (_jvm.exceptionClassBuf == null) {
        _testLock.wait();
      }

      assertEquals("exception class",
                   "java.lang.NullPointerException",
                   _jvm.exceptionClassBuf);
      assertEquals("stack trace",
                   InterpreterJVM.EMPTY_TRACE_TEXT.trim(),
                   _jvm.exceptionTraceBuf.trim());
    }
  }


  /**
   * Ensure that switching to a non-existant interpreter throws an Exception.
   */
  public void testSwitchToNonExistantInterpreter() {
    try {
      _jvm.setActiveInterpreter("thisisabadname");
      System.out.println("outbuf: " + _jvm.outBuf);
      fail("Should have thrown an exception!");
    }
    catch (IllegalArgumentException e) {
      // good, that's what should happen
    }
  }

  /**
   * Ensure that MainJVM can correctly switch the active interpreter used by
   * the interpreter JVM.
   */
  public void testSwitchActiveInterpreter() throws InterruptedException {
    synchronized(_testLock) {
      _jvm.interpret("x = 6;");
      _testLock.wait();
    }
    _jvm.addJavaInterpreter("monkey");

    // x should be defined in active interpreter
    synchronized(_testLock) {
      _jvm.interpret("x");
      _testLock.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }

    // switch interpreter
    _jvm.setActiveInterpreter("monkey");
    synchronized(_testLock) {
      _jvm.interpret("x");
      _testLock.wait();
      assertTrue("exception was thrown",
                 !_jvm.exceptionClassBuf.equals(""));
    }

    // define x to 3 and switch back
    synchronized(_testLock) {
      _jvm.interpret("x = 3;");
      _testLock.wait();
    }
    _jvm.setToDefaultInterpreter();

    // x should have its old value
    synchronized(_testLock) {
      _jvm.interpret("x");
      _testLock.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }

    // test syntax error handling
    //  (temporarily disabled until bug 750605 fixed)
//     synchronized(_testLock) {
//       _jvm.interpret("x+");
//       _testLock.wait();
//       assertTrue("syntax error was reported",
//                  ! _jvm.syntaxErrorMsgBuf.equals("") );
//     }

  }

  private static class TestJVMExtension extends MainJVM {
    public volatile String outBuf;
    public volatile String errBuf;
    public volatile String returnBuf;
    public volatile String exceptionClassBuf;
    public volatile String exceptionMsgBuf;
    public volatile String exceptionTraceBuf;
    public volatile String syntaxErrorMsgBuf;
    public volatile int syntaxErrorStartRow;
    public volatile int syntaxErrorStartCol;
    public volatile int syntaxErrorEndRow;
    public volatile int syntaxErrorEndCol;
    public volatile boolean voidReturnFlag;

    private volatile InterpretResultVisitor<Object> _testHandler;

    public TestJVMExtension() {
      super(null);
      _testHandler = new TestResultHandler();
      startInterpreterJVM();
      ensureInterpreterConnected();
    }

    protected InterpretResultVisitor<Object> getResultHandler() {
      return _testHandler;
    }

    public void resetFlags() {
      outBuf = null;
      errBuf = null;
      returnBuf = null;
      exceptionClassBuf = null;
      exceptionMsgBuf = null;
      exceptionTraceBuf = null;
      voidReturnFlag = false;
      syntaxErrorMsgBuf = null;
      syntaxErrorStartRow = 0;
      syntaxErrorStartCol = 0;
      syntaxErrorEndRow = 0;
      syntaxErrorEndCol = 0;
    }

    protected void handleSlaveQuit(int status) {
      synchronized(_testLock) {
        _testLock.notify();
        super.handleSlaveQuit(status);
      }
    }

    public void systemErrPrint(String s) throws RemoteException {
      synchronized(_testLock) {
        //System.out.println("notify err: " + s);
        errBuf = s;
//        _testLock.notify();
      }
    }

    public void systemOutPrint(String s) throws RemoteException {
      synchronized(_testLock) {
        //System.out.println("notify out: " + s);
        outBuf = s;
//        _testLock.notify();
      }
    }

    private class TestResultHandler implements InterpretResultVisitor<Object> {
      public Object forVoidResult(VoidResult that) {
        synchronized(_testLock) {
          voidReturnFlag = true;
          _log.log("NewJVMTest: void returned by interpretResult callback");
          _testLock.notify();
          return null;
        }
      }
      public Object forValueResult(ValueResult that) {
        synchronized(_testLock) {
          returnBuf = that.getValueStr();
          _log.log("NewJVMTest: " + returnBuf + " returned by interpretResult callback");
          _testLock.notify();
          return null;
        }
      }
      public Object forExceptionResult(ExceptionResult that) {
        synchronized(_testLock) {
          exceptionClassBuf = that.getExceptionClass();
          exceptionTraceBuf = that.getStackTrace();
          exceptionMsgBuf = that.getExceptionMessage();

          //System.out.println("notify threw");
          _testLock.notify();
          return null;
        }
      }

      public Object forSyntaxErrorResult(SyntaxErrorResult that) {
        synchronized(_testLock) {
          syntaxErrorMsgBuf = that.getErrorMessage();
          syntaxErrorStartRow = that.getStartRow();
          syntaxErrorStartCol = that.getStartCol();
          syntaxErrorEndRow = that.getEndRow();
          syntaxErrorEndCol = that.getEndCol();
          //System.out.println("notify threw");
          _testLock.notify();
          return null;
        }
      }
      
      public Object forInterpreterBusy(InterpreterBusy that) {
        throw new UnexpectedException("MainJVM.interpret called when interpreter was busy!");
      }
    }
  }
}
