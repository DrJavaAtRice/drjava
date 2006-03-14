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
import edu.rice.cs.util.swing.Utilities;

import java.lang.reflect.Modifier;

/** Runs in the InterpreterJVM. Runs tests given a classname and formats the results into a (serializable) array of 
 *  JUnitError that can be passed back to the MainJVM.
 *  @version $Id$
 */
public class JUnitTestManager {
  
  /** The interface to the master JVM via RMI. */
  private final JUnitModelCallback _jmc;
  
  /** The current testRunner; initially null.  Each test suite requires a new runner. */
  private JUnitTestRunner _testRunner;
  
  /** The accumulated test suite; null if no test is pending. */
  private TestSuite _suite = null;
  
  /** The accumulated list of names of TestCase classes; null if no test is pending. */
  private List<String> _testClassNames = null;
  
  /** The list of files corresponding to testClassNames; null if no test is pending. */
  private List<File> _testFiles = null;
  
  /** Standard constructor */
  public JUnitTestManager(JUnitModelCallback jmc) { _jmc = jmc; }

  public JUnitTestRunner getTestRunner() { return _testRunner; }
  
  /** Find the test classes among the given classNames and accumulate them in
   *  TestSuite for junit.  Returns null if a test suite is already pending.
   * @param classNames the class names that are test class candidates
   * @param files the files corresponding to classNames
   */
  public List<String> findTestClasses(final List<String> classNames, final List<File> files) {
    
//    Utilities.showDebug("InterpreterJVM.findTestClasses(" + classNames + ", " + files + ") called");
    
    if (_testClassNames != null && ! _testClassNames.isEmpty()) 
      throw new IllegalStateException("Test suite is still pending!");
    
    _testRunner = new JUnitTestRunner(_jmc);
    
    _testClassNames = new ArrayList<String>();
    _testFiles = new ArrayList<File>();
    _suite = new TestSuite();

//    new ScrollableDialog(null, "JUnitManager.findTestClasses invoked", "Candidate classes are = " + classNames, "files = " + files).show();
    
    int i = 0;
    try {
      for (i = 0; i < classNames.size(); i++) {
        String cName = classNames.get(i);
//        new ScrollableDialog(null, "Class to be checked in JUnitManager: " + cName, "", "").show();
        try {
          if (_isTestCase(cName)) {
//            new ScrollableDialog(null, "Test class " + cName + " found!", "", "").show();
            _testClassNames.add(cName);
            _testFiles.add(files.get(i));
            _suite.addTest(_testRunner.getTest(cName));
          }
        }
        catch(LinkageError e) { 
//          new ScrollableDialog(null, "LinkageError(" + e + ") encountered in JUnitTestManager", "", "").show();
          _jmc.classFileError(new ClassFileError(cName, files.get(i).getCanonicalPath(), e));
        }
      }
    }
    catch(IOException e) { throw new UnexpectedException(e); }
//    new ScrollableDialog(null, "TestClassNames are: " + _testClassNames, "", "").show();
     
    return _testClassNames;
  }
    
  /** Runs the pending test suite set up by the preceding call to findTestClasses
   *  @return false if no test suite (even an empty one) has been set up
   */
  public /* synchronized */ boolean runTestSuite() {
    
    if (_testClassNames == null || _testClassNames.isEmpty()) return false;
    
//    new ScrollableDialog(null, "runTestSuite() in SlaveJVM called", "", "").show();

    try {
      TestResult result = _testRunner.doRun(_suite);
    
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
    catch(Throwable t) { 
      JUnitError[] errors = new JUnitError[1];
      errors[0] = new JUnitError(null, -1, -1, t.getMessage(),
                                 false, "", "", StringOps.getStackTrace(t));
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
//    new ScrollableDialog(null, "_isJUnitTestCase called on " + c, "", "").show();
                                               
    return Test.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && 
      !Modifier.isInterface(c.getModifiers());
  }

  /** Checks whether the given file name corresponds to a valid JUnit Test. */
  private boolean _isTestCase(String className) {
    try { return _isJUnitTest(_testRunner.getLoader().load(className)); }
    catch (ClassNotFoundException cnfe) { return false; }
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
    String className1 = testString.substring(firstIndex, secondIndex);
    String className2 = testString.substring(0, firstIndex-1);
    if (firstIndex == secondIndex) className = className2;
    else className = className1;
    
    String classNameAndTest = className + "." + testName;
    String stackTrace = StringOps.getStackTrace(failure.thrownException());
    
    /** If the classname is not in the stacktrace, then the test that failed was inherited from a superclass. let's look
     *  for that classname
     */
    if (stackTrace.indexOf(className) == -1) {
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
      className = trace.substring(0,trace.indexOf(':'));
      className = trace.substring(0,trace.lastIndexOf('.'));
      classNameAndTest = className + "." + testName;
    }
    
    
    
    int lineNum = _lineNumber(stackTrace, classNameAndTest);
    
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

  /**
   * parses the line number out of the stack trace in the given class name.
   */
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
}
