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

package edu.rice.cs.drjava.model.junit;

import junit.framework.*;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;

import java.lang.reflect.Modifier;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** Runs in the InterpreterJVM. Runs tests given a classname and formats the results into a (serializable) array of 
 *  JUnitError that can be passed back to the MainJVM.
 *  @version $Id$
 */
public class JUnitTestManager {
  
  /** The interface to the master JVM via RMI. */
  private final JUnitModelCallback _jmc;
  
  /** A factory producing a ClassLoader for tests with the given parent */
  private final Lambda<ClassLoader, ClassLoader> _loaderFactory;
  
  /** The current testRunner; initially null.  Each test suite requires a new runner. */
  private JUnitTestRunner _testRunner;
  
  /** The accumulated test suite; null if no test is pending. */
  private TestSuite _suite = null;
  
  /** The accumulated list of names of TestCase classes; null if no test is pending. */
  private List<String> _testClassNames = null;
  
  /** The list of files corresponding to testClassNames; null if no test is pending. */
  private List<File> _testFiles = null;
  
  /** Standard constructor */
  public JUnitTestManager(JUnitModelCallback jmc, Lambda<ClassLoader, ClassLoader> loaderFactory) {
    _jmc = jmc;
    _loaderFactory = loaderFactory;
  }

  /** Find the test classes among the given classNames and accumulate them in
    * TestSuite for junit.  Returns null if a test suite is already pending.
    * @param classNames the class names that are test class candidates
    * @param files the files corresponding to classNames
    */
  public List<String> findTestClasses(final List<String> classNames, final List<File> files) {
    //debug.logStart("findTestClasses");
    //debug.logValues(new String[]{"classNames", "files"}, classNames, files);
    
    if (_testClassNames != null && ! _testClassNames.isEmpty()) 
      throw new IllegalStateException("Test suite is still pending!");
    
    _testRunner = makeRunner();
    
    _testClassNames = new ArrayList<String>();
    _testFiles = new ArrayList<File>();
    _suite = new TestSuite();

    int i = 0;
    for (Pair<String, File> pair : IterUtil.zip(classNames, files)) {
      String cName = pair.first();
      try {
        if (_isJUnitTest(_testRunner.loadPossibleTest(cName))) {
          _testClassNames.add(cName);
          _testFiles.add(pair.second());
          _suite.addTest(_testRunner.getTest(cName));
        }
      }
      catch (ClassNotFoundException e) { error.log(e); }
      catch(LinkageError e) {
        //debug.log(e);
        String path = IOUtil.attemptAbsoluteFile(pair.second()).getPath();
        _jmc.classFileError(new ClassFileError(cName, path, e));
      }
    }
     
    //debug.logEnd("findTestClasses");
    return _testClassNames;
  }
    
  /** Runs the pending test suite set up by the preceding call to findTestClasses
   *  @return false if no test suite (even an empty one) has been set up
   */
  public /* synchronized */ boolean runTestSuite() {
    
    if (_testClassNames == null || _testClassNames.isEmpty()) return false;
    
//    new ScrollableDialog(null, "runTestSuite() in SlaveJVM called", "", "").show();

    try {
      TestResult result = _testRunner.runSuite(_suite);
    
      JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];
      
      Enumeration failures = result.failures();
      Enumeration errEnum = result.errors();
      
      int i = 0;
      
      while (errEnum.hasMoreElements()) {
        TestFailure tErr = (TestFailure) errEnum.nextElement();
        errors[i] = _makeJUnitError(tErr, _testClassNames, true, _testFiles);
        i++;
      }
      
      while (failures.hasMoreElements()) {
        TestFailure tFail = (TestFailure) failures.nextElement();
        errors[i] = _makeJUnitError(tFail, _testClassNames, false, _testFiles);
        i++;
      }
//      new ScrollableDialog(null, "Slave JVM: testSuite ended with errors", "", Arrays.toString(errors)).show();
      
      _jmc.testSuiteEnded(errors);
    }
    catch(Exception e) { 
      JUnitError[] errors = new JUnitError[1];
      errors[0] = new JUnitError(null, -1, -1, e.getMessage(),
                                 false, "", "", StringOps.getStackTrace(e));
      _jmc.testSuiteEnded(errors);
//      new ScrollableDialog(null, "Slave JVM: testSuite ended with errors", "", Arrays.toString(errors)).show();
      
    }
    finally {
      _suite = null;
      _testClassNames = null;
      _testFiles = null;
    }
    return true;
  }

  /** Determines if the given class is a junit Test.
   *  @param c the class to check
   *  @return true iff the given class is an instance of junit.framework.Test
   */
  private boolean _isJUnitTest(Class c) {
    boolean result = Test.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && 
                     !Modifier.isInterface(c.getModifiers());
    //debug.logValues(new String[]{"c", "isJUnitTest(c)"}, c, result);
    return result;
  }

  /** Constructs a new JUnitError from a TestFailure
   *  @param failure A given TestFailure
   *  @param classNames The classes that were used for this test suite
   *  @param isError The passed TestFailure may signify either an error or a failure
   *  @param files The files that were used for this test suite
   *  @return JUnitError
   */
  private JUnitError _makeJUnitError(TestFailure failure, List<String> classNames, boolean isError, List<File> files) {

    Test failedTest = failure.failedTest();
    String testName;
    if (failedTest instanceof TestCase) testName = ((TestCase)failedTest).getName();
    else testName = failedTest.getClass().getName();
    
    String testString = failure.toString();
    int firstIndex = testString.indexOf('(') + 1;
    int secondIndex = testString.indexOf(')');
    
    /** junit can return a string in two different formats; we parse both formats, and then decide which one to use. */
    
    String className;
    if (firstIndex != secondIndex)
      className = testString.substring(firstIndex, secondIndex);
    else
      className = testString.substring(0, firstIndex-1);
    
    String classNameAndTest = className + "." + testName;
    String stackTrace = StringOps.getStackTrace(failure.thrownException());
    
    /* Check to see if the class and test name appear directly in the stack trace. If
     * they don't, then we'll have to do additional work to find the line number. Additionally,
     * if the exception occured in a subclass of the test class, we'll need to adjust our conception
     * of the class name.
     */
    int lineNum = -1;
    if (stackTrace.indexOf(classNameAndTest) == -1) {
      /* get the stack trace of the junit error */
      String trace = failure.trace();
      /* knock off the first line of the stack trace.
       * now the string will look like
       * at my.package.class(file.java:line)
       * at other.package.class(anotherfile.java:line)
       * etc...
       */
      trace = trace.substring(trace.indexOf('\n')+1);
      while (trace.indexOf("junit.framework.Assert") != -1 &&
            trace.indexOf("junit.framework.Assert") < trace.indexOf("(")) {
        /* the format of the trace will have "at junit.framework.Assert..."
         * on each line until the line of the actual source file.
         * if the exception was thrown from the test case (so the test failed
         * without going through assert), then the source file will be on
         * the first line of the stack trace
         */
        trace = trace.substring(trace.indexOf('\n') + 1);
      }
      trace = trace.substring(trace.indexOf('(')+1);
      trace = trace.substring(0, trace.indexOf(')'));
      
      // If the exception occurred in a subclass of the test class, then update our
      // concept of the class and test name. Otherwise, we're only here to pick up the
      // line number.
      if (stackTrace.indexOf(className) == -1) {
        className = trace.substring(0,trace.lastIndexOf('.'));
        classNameAndTest = className + "." + testName;
      }

      try {
        lineNum = Integer.parseInt(trace.substring(trace.indexOf(':') + 1)) - 1;
      }
      catch (NumberFormatException e) { throw new UnexpectedException(e); }
    }
    
    if (lineNum < 0) {
      lineNum = _lineNumber(stackTrace, classNameAndTest);
    }
    
//    if (lineNum > -1) _errorsWithPos++;

    String exception =  (isError) ? failure.thrownException().toString(): 
                                    failure.thrownException().getMessage();
    boolean isFailure = (failure.thrownException() instanceof AssertionFailedError) &&
      !classNameAndTest.equals("junit.framework.TestSuite$1.warning");

//    for dubugging    
//    try{
//      File temp = File.createTempFile("asdf", "java", new File("/home/awulf"));
//      FileWriter writer = new FileWriter(temp);
//      writer.write("testString: " + testString + "\n");
//      writer.write("old className: " + className1 + "\n");
//      writer.write("new className: " + className2 + "\n");
//      writer.write("file: " + file + "\n");
//      writer.write("lineNum: " + lineNum + "\n");
//      writer.write("exception: " + exception + "\n");
//      writer.write("!isFailure: " + !isFailure + "\n");
//      writer.write("testName: " + testName + "\n");
//      writer.write("className: " + className + "\n");
//      writer.write("stackTrace: " + stackTrace + "\n");
//      writer.close();
//    } catch(IOException e) {
//      
//    }

    int indexOfClass = classNames.indexOf(className);
    File file;
    if (indexOfClass != -1) file = files.get(indexOfClass);
    else file = _jmc.getFileForClassName(className);
    
    // if testClass contains no 
    
    // a test didn't fail, we couldn't even open the test.
    if (file == null) {
      return new JUnitError(new File("nofile"), 0, 0, exception, !isFailure, testName, className, stackTrace);
    }
    
    // The code augmentation for elementary and intermediate level files causes the error to be highlighted on
    // the wrong line.  The following code adjusts for this discrepancy.
    String name = file.getName();
    int adjLineNum;
    if (name.endsWith(".dj0") || name.endsWith(".dj0")) adjLineNum = lineNum - 1;
    else adjLineNum = lineNum;
    
    return new JUnitError(file, adjLineNum, 0, exception, !isFailure, testName, className, stackTrace);
  }

  /** Parses the line number out of the stack trace in the given class name. */
  private int _lineNumber(String sw, String classname) {
    int lineNum;
    int idxClassname = sw.indexOf(classname);
    if (idxClassname == -1) return -1;

    String theLine = sw.substring(idxClassname, sw.length());
    
    theLine = theLine.substring(theLine.indexOf(classname), theLine.length());
    theLine = theLine.substring(theLine.indexOf("(") + 1, theLine.length());
    theLine = theLine.substring(0, theLine.indexOf(")"));

    try {
      int i = theLine.indexOf(":") + 1;
      lineNum = Integer.parseInt(theLine.substring(i, theLine.length())) - 1;
    }
    catch (NumberFormatException e) { throw new UnexpectedException(e); }
    
    return lineNum;
  }
  
  /** Make a fresh JUnitTestRunner with its own class loader instance. */
  private JUnitTestRunner makeRunner() {
    ClassLoader current = JUnitTestManager.class.getClassLoader();
    // References to JUnit classes must match those of the current loader so that,
    // for example, when a test fails, the failure exception is of a class we can talk 
    // about in the current context.
    ClassLoader parent = ShadowingClassLoader.whiteList(current, "junit", "org.junit");
    return new JUnitTestRunner(_jmc, _loaderFactory.value(parent));
  }
  
}
