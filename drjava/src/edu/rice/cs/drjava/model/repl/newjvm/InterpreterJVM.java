/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;

import java.rmi.*;

// NOTE: Do NOT import/use the config framework in this class!
//  (This class runs in a different JVM, and will not share the config object)

import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.ReflectException;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.InteractionsPaneOptions;

import edu.rice.cs.util.swing.Utilities;

// For Windows focus fix
import javax.swing.JDialog;

import edu.rice.cs.drjava.DrScala;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** This is the main class for the interpreter JVM.  All public methods except those involving remote calls (callbacks) 
  * use synchronizazion on _stateLock (unless synchronization has no effect).  The class is not ready for remote
  * calls until handleStart has been executed.  This class is loaded in the Interpreter JVM, not the Main JVM. 
  * (Do not use DrScala's config framework here.)
  * Note that this class extends AbstractSlaveJVM which contains critical methods such as ??.
  * <p>
  * Note that this class is specific to the Scala interpreter. It must be refactored to accommodate other interpreters.
  * @version $Id: InterpreterJVM.java 5723 2012-09-29 19:38:35Z wdforson $
  */
public class InterpreterJVM extends AbstractSlaveJVM implements InterpreterJVMRemoteI, JUnitModelCallback {
  
  /* Log _log inherited from AbstractSlaveJVM */
  
  public static final String PATH_SEPARATOR = System.getProperty("path.separator");
  
  /** Singleton instance of this class. */
  public static final InterpreterJVM ONLY = new InterpreterJVM();
  
  // Since RMI can lead to parallel threads, all fields must be thread-safe.  Consequently, we use
  // concurrent Collections
  
//  private final InteractionsPaneOptions _interpreterOptions;
  
  private volatile Interpreter _interpreter;  // it should be final but static checks are too weak

  private final ClassPathManager _classPathManager;
  private final ClassLoader _interpreterLoader;
  private volatile Class<?> _interpreterClass;  // should be final but static checks are too weak because 
  
  // Lock object for ensuring mutual exclusion on updates and compound accesses
  private final Object _stateLock = new Object();
  
  /** Responsible for running JUnit tests in this JVM. */
  private final JUnitTestManager _junitTestManager;
  
  /** Remote reference to the MainJVM class in DrScala's primary JVM.  Assigned ONLY once. */
  private volatile MainJVMRemoteI _mainJVM;
  
  private volatile boolean scalaInterpreterStarted = false;
  
  private Interpreter createInterpreter(final String scalaInterpreterName) {
    try {
      _log.log("_interpreterLoader = " + _interpreterLoader);
      Class<?> _interpreterClass = Class.forName(scalaInterpreterName, true, _interpreterLoader);
      _log.log("In InterpreterJVM," + _interpreterClass  + " was loaded by " + _interpreterClass.getClassLoader());
      return (Interpreter) _interpreterClass.newInstance();  // The unique constructor for DrScalaInterpreter is 0-ary
    }
    catch(Exception e) {
      _log.log("In InterpreterJVM, either ClassforName(" + scalaInterpreterName + ", true, " + _interpreterLoader + 
               ") or _interpreterClass.newInstance() failed");
      throw new UnexpectedException(e); 
    }
  }

  /** Private constructor; use the singleton ONLY instance. */
  private InterpreterJVM() {
    super("Reset Interactions Thread", "Poll DrScala Thread");
    _log.log("In InterpreterJVM, interpreter JVM starting");
    _classPathManager = new ClassPathManager();
    
    final String scalaInterpreterName = "edu.rice.cs.drjava.model.repl.newjvm.DrScalaInterpreter";
    final ClassLoader interpreterLoaderParent =
      ShadowingClassLoader.blackList(InterpreterJVM.class.getClassLoader(), scalaInterpreterName);
    _log.log("In InterpreterJVM, interpreter loader parent = " + interpreterLoaderParent);
    _interpreterLoader = _classPathManager.makeClassLoader(interpreterLoaderParent);
    _log.log("In InterpreterJVM, Interpreter loader = " + _interpreterLoader);
    
    _interpreter = createInterpreter("edu.rice.cs.drjava.model.repl.newjvm.DrScalaInterpreter");
    _log.log("In InterpreterJVM, _interpreter = " + _interpreter + " loader = " + 
             _interpreter.getClass().getClassLoader());
//    Utilities.show("Interpreter JVM started");
    /* Important singleton objects embedded in an InterpreterJVM */
      
    _junitTestManager = new JUnitTestManager(this, _classPathManager);
    _log.log("In InterpreterJVM, _junitTestManager = " + _junitTestManager);
  }
 
  /** Actions to perform when this JVM is started (through its superclass, AbstractSlaveJVM). Not synchronized
    * because "this" is not initialized for general access until this method has run. */
  protected void _init(MasterRemote mainJVM) {
    _log.log("In InterpreterJVM, _init(" + mainJVM + " called");
    _mainJVM = (MainJVMRemoteI) mainJVM;  // safe upcast that ensures only remote method are called 
    
    // redirect stdin
    System.setIn(new InputStreamRedirector() {
      protected String _getInput() {
        try { return _mainJVM.getConsoleInput(); }
        catch(RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for input.\n" + re);
        }
      }
    });
    
    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try { _mainJVM.systemOutPrint(s); }
        catch (RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for output.\n" + re);
        }
      }
    }));
    
    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try { _mainJVM.systemErrPrint(s); }
        catch (RemoteException re) {
          error.log(re);
          throw new UnexpectedException("Main JVM can't be reached for output.\n" + re);
        }
      }
    }));
    
    _log.log("In InterpreterJVM, Standard In, Out, and Err have been redirected; calling interpreter.start()");
    /* _interpreter is final bound to an instance of DrScalaInterpreter, so it can NEVER be null once initialized. */ 
    _interpreter.start();

    /* On Windows, any frame or dialog opened from Interactions pane will appear *behind* DrScala's frame, unless a 
     * previous frame or dialog is shown here.  Not sure what the difference is, but this hack seems to work.  (I'd
     * be happy to find a better solution, though.)  Only necessary on Windows, since frames and dialogs on other 
     * platforms appear correctly in front of DrJava. */
    if (PlatformFactory.ONLY.isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.setVisible(true);
      d.setVisible(false);
    }
    //_dialog("interpreter JVM started");
  }
   
  /** Interprets the given string of source code in the Scala interpreter. The result is returned to MainJVM via 
    * the interpretResult method.  No synchronization necessary; the code only contains a single read of
    * local state.  This method should not be called if the interpreter (as recorded in the MainJVM) is busy.
    * @param s Source code to interpret.
    */
  
  public InterpretResult interpret(String input) {
//    Utilities.show("interpret(" + input + ") called in InterpreterJVM running in SLAVE JVM");
    
    _log.log("In InterpreterJVM, interpret(" + input + ") called in InterpreterJVM running in SLAVE JVM");
    
//    // This may be overkill for DrScala; there is only one loader for the interpreter
//    Thread.currentThread().setContextClassLoader(_interpreterLoader);  // _interpreterLoader is final

    String result = null;
    try { result = _interpreter.interpret(input); }  // may block forever
    catch (InterpreterException e) {
      _log.log("In InterpreterJVM, interpret invocation threw InterpreterException: " + e);
      // DEBUGGING PRINTLINE
      System.out.println(e);
      // DEBUGGING PRINTLINE
      
//      debug.logEnd(); 
      return InterpretResult.exception(e);
    }
    catch (Throwable e) {
      _log.log("In InterpreterJVM, interpret invocation threw Throwable: " + e);
      // DEBUGGING PRINTLINE
      System.out.println(e);
      // DEBUGGING PRINTLINE
      
//       debug.logEnd(); 
      return InterpretResult.unexpectedException(e);
    }

    return InterpretResult.stringValue(result);
  }
  
  /** Executes internal reset operation of the interpreter. */
  public void reset() throws RemoteException { 
    _log.log("In InterpreterJVM, reset() called in InterpreterJVM");
    _interpreter.reset(); 
  }
  
//  /** Gets the value and type string of the variable with the given name in the current interpreter.
//    * Invoked reflectively by the debugger.  To simplify the inter-process exchange,
//    * an array here is used as the return type rather than an {@code Option<Object>} --
//    * an empty array corresponds to "none," and a singleton array corresponds to a "some."
//    */
//  @SuppressWarnings("all")  // New array operations must use raw types, which creates typing mismatches.
//  public Pair<Object,String>[] getVariable(String var) {
//    synchronized(_stateLock) {
//      InterpretResult ir = interpret(var);
//      return ir.apply(new InterpretResult.Visitor<Pair<Object,String>[]>() {
//        
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Pair<Object,String>[] fail() { return new Pair[0]; }
//        
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Pair<Object,String>[] value(Object val) {
//          return new Pair[] { new Pair<Object,String>(val, getClassName(val.getClass())) };
//        }
//        public Pair<Object,String>[] forNoValue() { return fail(); }
//        public Pair<Object,String>[] forStringValue(String val) { return value(val); }
//        public Pair<Object,String>[] forCharValue(Character val) { return value(val); }
//        public Pair<Object,String>[] forNumberValue(Number val) { return value(val); }
//        public Pair<Object,String>[] forBooleanValue(Boolean val) { return value(val); }
//        
//        @SuppressWarnings({"unchecked", "rawtypes"})
//        public Pair<Object,String>[] forObjectValue(String valString, String objTypeString) {
//          return new Pair[] { new Pair<Object,String>(valString, objTypeString) }; 
//        }
//        public Pair<Object,String>[] forException(String message) { return fail(); }
//        public Pair<Object,String>[] forEvalException(String message, StackTraceElement[] stackTrace) { return fail(); }
//        public Pair<Object,String>[] forUnexpectedException(Throwable t) { return fail(); }
////        public Pair<Object,String>[] forBusy() { return fail(); }
//      });
//    }
//  }

//  /** Gets the string representation of the value of a variable in the current interpreter.
//    * @param var the name of the variable
//    * @return null if the variable is not defined; the first part of the pair is "null" if the value is null,
//    * otherwise its string representation; the second part is the string representation of the variable's type
//    */
//  public Pair<String,String> getVariableToString(String var) {
//    synchronized(_stateLock) {
////    if (!isValidFieldName(var)) { return "<error in watch name>"; }
//      Pair<Object,String>[] val = getVariable(var);  // recursive locking
//      if (val.length == 0) { return new Pair<String,String>(null,null); }
//      else {
//        Object o = val[0].first();
//        try { return new Pair<String,String>(TextUtil.toString(o),val[0].second()); }
//        catch (Throwable t) { return new Pair<String,String>("<error in toString()>",""); }
//      }
//    }
//  }

  /** @return the name of the class, with the right number of array suffixes "[]" and while being ambiguous
    * about boxed and primitive types. */
  public static String getClassName(Class<?> c) {
    StringBuilder sb = new StringBuilder();
    boolean isArray = c.isArray();
    while(c.isArray()) {
      sb.append("[]");
      c = c.getComponentType();
    }
    
    if (! isArray) {
      // we can't distinguish primitive types from their boxed types right now
      if (c.equals(Byte.class))      { return "byte" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); } 
      if (c.equals(Short.class))     { return "short" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Integer.class))   { return "int" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Long.class))      { return "long" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Float.class))     { return "float" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Double.class))    { return "double" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Boolean.class))   { return "boolean" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      if (c.equals(Character.class)) { return "char" + sb.toString() + " or " + c.getSimpleName() + sb.toString(); }
      else return c.getName() + sb.toString();
    }
    else {
      // if it's an array, we can distinguish boxed types and primitive types
      return c.getName() + sb.toString();
    }
  }
  
  /** Gets the default interpreter (the ONLY interpreter). */
  public Interpreter getInterpreter() throws RemoteException { return _interpreter; }
  
  /** Gets the interactions class path maintained by the ClassPathManager. */
  public List<File> getInteractionsClassPath() {
    return _classPathManager.getInteractionsClassPath();
  }
  
  /** Gets the class path for the current interpreter [slave] JVM. */
  public List<File> getCurrentClassPath() { return _classPathManager.getJVMClassPath(); }
    /* The following return statement is equivalent to the preceding but more computationally expensive. */
//    return CollectUtil.makeList(IOUtil.parse(System.getProperty("java.class.path")));
  
  // ---------- JUnit methods ----------
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases classes (by 
    * loading them).  Unsynchronized because it contains a remote call and does not involve mutable local state.
    * @param classNames the class names to run in a test
    * @param files the associated file
    * @return the class names that are actually test cases
    */
  public List<String> findTestClasses(List<String> classNames, List<File> files) throws RemoteException {
//    Utilities.show("InterpreterJVM.findTestClasses(" + classNames + ", " + files + ") called");
    _log.log("InterpreterJVM.findTestCaseClasses(" + classNames + ", " + files + ") called");
    List<String> result = _junitTestManager.findTestClasses(classNames, files);
    _log.log("InterpreterJVM.findTestCaseClasses(...) returned: " + result);
    return result;
  }
  
  /** Runs JUnit test suite already cached in the Interpreter JVM.  Unsynchronized because it contains a remote call
    * and does not involve mutable local state.
    * @return false if no test suite is cached; true otherwise
    */
  public boolean runTestSuite() throws RemoteException { return _junitTestManager.runTestSuite(); }
  
  /** Notifies Main JVM that JUnit has been invoked on a non TestCase class.  Unsynchronized because it contains a 
    * remote call and does not involve mutable local state.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    try { _mainJVM.nonTestCase(isTestAll, didCompileFail); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies the main JVM that JUnitTestManager has encountered an illegal class file.  Unsynchronized because it 
    * contains a remote call and does not involve mutable local state.
    * @param e the ClassFileError object describing the error on loading the file
    */
  public void classFileError(ClassFileError e) {
    try { _mainJVM.classFileError(e); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a suite of tests has started running.  Unsynchronized because it contains a remote call and does
    * not involve mutable local state.
    * @param numTests The number of tests in the suite to be run.
    */
  public void testSuiteStarted(int numTests) {
    try { _mainJVM.testSuiteStarted(numTests); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a particular test has started.  Unsynchronized because it contains a remote call and does not
    * involve mutable local state.
    * @param testName The name of the test being started.
    */
  public void testStarted(String testName) {
    try { _mainJVM.testStarted(testName); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a particular test has ended.  Unsynchronized because it contains a remote call.
    * @param testName The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error or simply failed.
    */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try { _mainJVM.testEnded(testName, wasSuccessful, causedError); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Notifies that a full suite of tests has finished running.  Unsynchronized because it contains a remote call
    * and does not involve mutable local state.
    * @param errors The array of errors from all failed tests in the suite.
    */
  public void testSuiteEnded(JUnitError[] errors) {
    try { _mainJVM.testSuiteEnded(errors); }
    catch (RemoteException re) { error.log(re); }
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.  Unsynchronized because it 
    * contains a remote call and does not involve mutable local state.
    * @param className the name of the class for which we want to find the file
    * @return the file associated with the given class
    */
  public File getFileForClassName(String className) {
    try { return _mainJVM.getFileForClassName(className); }
    catch (RemoteException re) { error.log(re); return null; }
  }
  
  public void junitJVMReady() { }
  
  // --------- Class path methods ----------
  public void addInteractionsClassPath(File f) { _classPathManager.addInteractionsClassPath(f);  }
  
  /** Traverses cp in reverse order adding the elements of cp to the interactions class path.  As a result, the elements
    * of cp will appear in order at the front of the interactions class path. */
  public void addInteractionsClassPath(List<File> cp) {
//    List<File> cpl = CollectUtil.makeArrayList(cp);
    ListIterator<File> li = cp.listIterator(cp.size());
    while (li.hasPrevious()) addInteractionsClassPath(li.previous());
  }
}
