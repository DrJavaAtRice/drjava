/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;

import junit.runner.*;
import junit.framework.*;
import junit.textui.TestRunner;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

/**
 * Runs in the InterpreterJVM. Runs tests given a classname and formats the
 * results into a (serializable) array of JUnitError that can be passed
 * back to the MainJVM.
 * @version $Id$
 */
public class JUnitTestManager {
  private final JUnitModelCallback _jmc;
  private JUnitTestRunner _testRunner;

  public JUnitTestManager(JUnitModelCallback jmc) {
    _jmc = jmc;
  }

  public JUnitTestRunner getTestRunner() {
    return _testRunner;
  }

  /**
   * @param classNames the class names to run in a test
   * @param files the associated files
   * @param isTestAll if we're testing all open files or not
   * @return the class names that are actually test cases
   */
  public List<String> runTest(final List<String> classNames, final List<File> files,
                              final boolean isTestAll) {
    final ArrayList<String> stuff = new ArrayList<String>();
    synchronized (stuff) {
      _testRunner = new JUnitTestRunner(_jmc);
      new Thread("JUnit Test Thread") {
        public void run() {
          try {
            boolean noJUnitTests = true;
            TestSuite suite = new TestSuite();
            synchronized (stuff) {
              try {
                for (int i = 0; i < classNames.size(); i++) {
                  String className = classNames.get(i);
                  if (_isTestCase(className)) {
                    Test test = _testRunner.getTest(className);
                    suite.addTest(test);
                    stuff.add(className);
                    noJUnitTests = false;
                  }
                }
              }
              finally {
                stuff.notify();
              }
            }
            if (noJUnitTests) {
              _jmc.nonTestCase(isTestAll);
              //            _jmc.testSuiteEnded(new JUnitError[] {new JUnitError(null, "No JUnit tests open!", false, "")});
              return;
            }

            TestResult result = _testRunner.doRun(suite);

            JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];

            Enumeration failures = result.failures();
            Enumeration errEnum = result.errors();

            int i = 0;

            while (errEnum.hasMoreElements()) {
              TestFailure tErr = (TestFailure) errEnum.nextElement();
              errors[i] = _makeJUnitError(tErr, classNames, true, files);
              i++;
            }
            while (failures.hasMoreElements()) {
              TestFailure tFail = (TestFailure) failures.nextElement();
              errors[i] = _makeJUnitError(tFail, classNames, false, files);
              i++;
            }

            _jmc.testSuiteEnded(errors);
          }
          catch (Throwable t) {
            _failedWithError(t);
          }
        }
      }.start();
      try {
        stuff.wait();
      }
      catch (InterruptedException ex) {
      }
    }
    return stuff;
  }

  private void _failedWithError(Throwable t) {
    JUnitError[] errors = new JUnitError[1];
    errors[0] = new JUnitError(null, -1, -1, t.getMessage(),
                               false, "", "", StringOps.getStackTrace(t));
    _jmc.testSuiteEnded(errors);
  }

  /**
   * Determines if the given class is a junit Test.
   * @param c the class to check
   * @return true iff the given class is an instance of junit.framework.Test
   */
  private boolean _isJUnitTest(Class c) {
    return Test.class.isAssignableFrom(c);
  }

  /**
   * Checks whether the given file name corresponds to
   * a valid JUnit Test.
   */
  private boolean _isTestCase(String className) {
    try {
      return _isJUnitTest(_testRunner.getLoader().load(className));
    }
    catch (ClassNotFoundException cnfe) {
      return false;
    }
  }

  /**
   * Constructs a new JUnitError from a TestFailure
   * @param failure A given TestFailure
   * @param classNames The classes that were used for this test suite
   * @param isError The passed TestFailure may signify either an error or a failure
   * @param files The files that were used for this test suite
   * @return JUnitError
   */
  private JUnitError _makeJUnitError(TestFailure failure, List<String> classNames,
                                     boolean isError, List<File> files) {

    Test failedTest = failure.failedTest();
    String testName = failedTest.getClass().getName();
    if (failedTest instanceof TestCase) {
      testName = ((TestCase)failedTest).getName();
    }

    String testString = failedTest.toString();
    int firstIndex = testString.indexOf('(') + 1;
    int secondIndex = testString.indexOf(')');
    
    String className1 = testString.substring(firstIndex, secondIndex);
    String className2 = testString.substring(0, firstIndex-1);
    String className;
    if(firstIndex == secondIndex){
      className = className2;
    }else{
      className = className1;
    }
    
    int indexOfClass = classNames.indexOf(className);
    File file;
    if (indexOfClass != -1) {
      file = files.get(indexOfClass);
    }
    else {
      file = _jmc.getFileForClassName(className);
    }

//    String ps = System.getProperty("file.separator");
//    // replace periods with the System's file separator
//    className = StringOps.replace(className, ".", ps);
//
//    // crop off the $ if there is one and anything after it
//    int indexOfDollar = className.indexOf('$');
//    if (indexOfDollar > -1) {
//      className = className.substring(0, indexOfDollar);
//    }
//
//    String filename = className + ".java";

    String classNameAndTest = className + "." + testName;
    String stackTrace = StringOps.getStackTrace(failure.thrownException());

    int lineNum = _lineNumber(stackTrace, classNameAndTest);
//    if (lineNum > -1) _errorsWithPos++;

    String exception =  (isError) ?
      failure.thrownException().toString():
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
//    }catch(IOException e){
//      
//    }
    //The conditional has been added because of the augmented code in the .dj0 files - it causes the error to be highlighted on the wrong line
    //At the elementary level it should always be off by one
    //NOTE: this presupposes that 
    return new JUnitError(file, (file.getName().endsWith(".dj0") ? lineNum-1 : lineNum),  //lineNum, 
                          0, exception, !isFailure, testName, className, stackTrace);
  }

  /**
   * parses the line number out of the stack trace in the given class name.
   */
  private int _lineNumber(String sw, String classname) {
    int lineNum;

    int idxClassname = sw.indexOf(classname);
    if (idxClassname == -1) {
      return -1;
    }

    String theLine = sw.substring(idxClassname, sw.length());
    theLine = theLine.substring(theLine.indexOf(classname), theLine.length());
    theLine = theLine.substring(theLine.indexOf("(") + 1, theLine.length());
    theLine = theLine.substring(0, theLine.indexOf(")"));

    try {
      int i = theLine.indexOf(":") + 1;
      lineNum = Integer.parseInt(theLine.substring(i, theLine.length())) - 1;
    }
    catch (NumberFormatException e) {
      throw new UnexpectedException(e);
    }
    
    return lineNum;
  }
}
