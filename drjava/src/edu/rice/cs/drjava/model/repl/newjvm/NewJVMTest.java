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
        _jvm.killInterpreter();
      }
    };

    return setup;
  }
  
  public void testPrintln() throws Throwable {
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
    synchronized(_jvm) {
      _jvm.interpret("5");
      _jvm.wait();
      assertEquals("result", "5", _jvm.returnBuf);
    }
  }

  public void testThrowRuntimeException() throws Throwable {
    synchronized(_jvm) {
      _jvm.interpret("throw new RuntimeException();");
      _jvm.wait();
      assertEquals("exception class",
                   "java.lang.RuntimeException",
                   _jvm.exceptionClassBuf);
    }
  }

  public void testThrowNPE() throws Throwable {
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

  private static class TestJVMExtension extends MainJVM {
    public String outBuf;
    public String errBuf;
    public String returnBuf;
    public String exceptionClassBuf;
    public String exceptionMsgBuf;
    public String exceptionTraceBuf;
    public boolean voidReturnFlag;

    public TestJVMExtension() throws RemoteException { 
      super();
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

    public void systemErrPrint(String s) throws RemoteException {
      synchronized(this) {
        errBuf = s;
        //System.out.println("notify err: " + s);
        this.notify();
      }
    }
    
    public void systemOutPrint(String s) throws RemoteException {
      synchronized(this) {
        outBuf = s;
        //System.out.println("notify out: " + s);
        this.notify();
      }
    }

    public void threwException(String exceptionClass,
                               String message,
                               String stackTrace)
      throws RemoteException
    {
      synchronized(this) {
        exceptionClassBuf = exceptionClass;
        exceptionTraceBuf = stackTrace;
        exceptionMsgBuf = message;

        //System.out.println("notify threw");
        this.notify();
      }
    }

    public void returnedResult(String result) throws RemoteException
    {
      synchronized(this) {
        returnBuf = result;
        //System.out.println("notify returned");
        this.notify();
      }
    }

    public void returnedVoid() throws RemoteException {
      synchronized(this) {
        voidReturnFlag = true;
        //System.out.println("notify void");
        this.notify();
      }
    }
    
    protected int getDebugPort() {
      return -1;
    }
    
    protected void replCalledSystemExit(int status) {
    }
  }
}
