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

package edu.rice.cs.drjava.model.junit;

import junit.runner.*;
import junit.framework.*;
import junit.textui.TestRunner;

import java.io.*;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;
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
    String className = testString.substring(firstIndex, secondIndex);
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
    return new JUnitError(file, lineNum, 0, exception, !isFailure, testName, className, stackTrace);
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
