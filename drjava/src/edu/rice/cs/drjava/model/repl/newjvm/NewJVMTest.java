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

import junit.framework.*;
import junit.extensions.*;

import java.rmi.*;

import edu.rice.cs.drjava.model.*;

/**
 * Tests the functionality of the new JVM manager.
 *
 * @version $Id$
 */
public class NewJVMTest extends TestCase {
  final boolean printMessages = false;
  
  private static TestJVMExtension _jvm;

  public NewJVMTest(String name) {
    super(name);
  }

  protected void setUp() throws RemoteException {
    _jvm.resetFlags();
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(NewJVMTest.class);
    TestSetup setup = new TestSetup(suite) {
      protected void setUp() throws RemoteException {
        _jvm = new TestJVMExtension();
      }

      protected void tearDown() {
        _jvm.killInterpreter(false);
      }
    };

    return setup;
  }
  
  
  public void testPrintln() throws Throwable {
    if (printMessages) System.out.println("----testPrintln-----");
    synchronized(_jvm) {
      _jvm.interpret("System.err.print(\"err\");");
      _jvm.wait(); // wait for println
      _jvm.wait(); // wait for void return
      assertEquals("system err buffer", "err", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_jvm) {
      _jvm.interpret("System.err.print(\"err2\");");
      _jvm.wait(); // wait for println
      _jvm.wait(); // wait for void return
      assertEquals("system err buffer", "err2", _jvm.errBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
      _jvm.resetFlags();
    }

    synchronized(_jvm) {
      _jvm.interpret("System.out.print(\"out\");");
      _jvm.wait(); // wait for println
      _jvm.wait(); // wait for void return
      assertEquals("system out buffer", "out", _jvm.outBuf);
      assertEquals("void return flag", true, _jvm.voidReturnFlag);
    }
  }

  public void testReturnConstant() throws Throwable {
    if (printMessages) System.out.println("----testReturnConstant-----");
    synchronized(_jvm) {
      _jvm.interpret("5");
      _jvm.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
  }

  public void testWorksAfterRestartConstant() throws Throwable {
    if (printMessages) System.out.println("----testWorksAfterRestartConstant-----");
    
    // Check that a constant is returned
    synchronized(_jvm) {
      _jvm.interpret("5");
      _jvm.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
    
    // Now restart interpreter
    synchronized(_jvm) {
      _jvm.killInterpreter(true);  // true: start back up
      _jvm.wait();
    }
    
    // Now evaluate another constant
    synchronized(_jvm) {
      _jvm.interpret("4");
      _jvm.wait();
      assertEquals("result", "4", _jvm.returnBuf);
    }
  }
  
  
  public void testThrowRuntimeException() throws Throwable {
    if (printMessages) System.out.println("----testThrowRuntimeException-----");
    synchronized(_jvm) {
      _jvm.interpret("throw new RuntimeException();");
      _jvm.wait();
      assertEquals("exception class",
                   "java.lang.RuntimeException",
                   _jvm.exceptionClassBuf);
    }
  }
  
  public void testToStringThrowsRuntimeException() throws Throwable {
    if (printMessages) System.out.println("----testToStringThrowsRuntimeException-----");
    synchronized(_jvm) {
      _jvm.interpret(
        "class A { public String toString() { throw new RuntimeException(); } };" +
        "new A()");
      _jvm.wait();
      assertTrue("exception should have been thrown by toString",
                 _jvm.exceptionClassBuf != null);
    }
  }

  public void testThrowNPE() throws Throwable {
    if (printMessages) System.out.println("----testThrowNPE-----");
    synchronized(_jvm) {
      _jvm.interpret("throw new NullPointerException();");

      while (_jvm.exceptionClassBuf == null) {
        _jvm.wait();
      }
      
      assertEquals("exception class",
                   "java.lang.NullPointerException",
                   _jvm.exceptionClassBuf);
    }
  }

  public void testStackTraceEmptyTrace() throws Throwable {
    if (printMessages) System.out.println("----testStackTraceEmptyTrace-----");
    synchronized(_jvm) {
      _jvm.interpret("null.toString()");

      while (_jvm.exceptionClassBuf == null) {
        _jvm.wait();
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
      _jvm.setActiveInterpreter("monkey");
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
    synchronized(_jvm) {
      _jvm.interpret("x = 6;");
      _jvm.wait();
    }
    _jvm.addDebugInterpreter("monkey");
    
    // x should be defined in active interpreter
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }
    
    // switch interpreter
    _jvm.setActiveInterpreter("monkey");
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertTrue("exception was thrown",
                 !_jvm.exceptionClassBuf.equals(""));
    }
    
    // define x to 3 and switch back
    synchronized(_jvm) {
      _jvm.interpret("x = 3;");
      _jvm.wait();
    }
    _jvm.setDefaultInterpreter();
    
    // x should have its old value
    synchronized(_jvm) {
      _jvm.interpret("x");
      _jvm.wait();
      assertEquals("result", "6", _jvm.returnBuf);
    }
  }

  private static class TestJVMExtension extends MainJVM {
    public String outBuf;
    public String errBuf;
    public String returnBuf;
    public String exceptionClassBuf;
    public String exceptionMsgBuf;
    public String exceptionTraceBuf;
    public boolean voidReturnFlag;
    
    private InterpretResultVisitor<Object> _testHandler;

    public TestJVMExtension() throws RemoteException { 
      super();
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
    }
    
    protected void handleSlaveQuit(int status) {
      synchronized(this) {
        this.notify();
        super.handleSlaveQuit(status);
      }
    }

    public void systemErrPrint(String s) throws RemoteException {
      synchronized(this) {
        //System.out.println("notify err: " + s);
        errBuf = s;
        this.notify();
      }
    }

    public void systemOutPrint(String s) throws RemoteException {
      synchronized(this) {
        //System.out.println("notify out: " + s);
        outBuf = s;
        this.notify();
      }
    }

    private class TestResultHandler implements InterpretResultVisitor<Object> {
      public Object forVoidResult(VoidResult that) {
        synchronized(TestJVMExtension.this) {
          voidReturnFlag = true;
          //System.out.println("notify void");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      public Object forValueResult(ValueResult that) {
        synchronized(TestJVMExtension.this) {
          returnBuf = that.getValueStr();
          //System.out.println("notify returned");
          TestJVMExtension.this.notify();
          return null;
        }
      }
      public Object forExceptionResult(ExceptionResult that) {
        synchronized(TestJVMExtension.this) {
          exceptionClassBuf = that.getExceptionClass();
          exceptionTraceBuf = that.getStackTrace();
          exceptionMsgBuf = that.getExceptionMessage();
          
          //System.out.println("notify threw");
          TestJVMExtension.this.notify();
          return null;
        }
      }
    }
  }
}
