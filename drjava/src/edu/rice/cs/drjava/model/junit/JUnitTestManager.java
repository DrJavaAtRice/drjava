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

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;
import edu.rice.cs.util.UnexpectedException;

/**
 * Runs in the InterpreterJVM. Runs tests given a classname and formats the 
 * results into a (serializable) array of JUnitError that can be passed 
 * back to the MainJVM.
 * @version $Id$
 */
public class JUnitTestManager {
  private final InterpreterJVM _jvm;
  private final JUnitTestRunner _testRunner;

  public JUnitTestManager (InterpreterJVM jvm) {
    _jvm = jvm;
    _testRunner = new JUnitTestRunner(_jvm);
  }
  
  public JUnitTestRunner getTestRunner() {
    return _testRunner;
  }
  
  public void runTest(final String className, final String fileName) {
    Thread t = new Thread() {
      public void run() {
        try {
          if (!_isTestCase(className)) {
            _jvm.nonTestCase();
           return;
          }
          Test suite = _testRunner.getTest(className);
          TestResult result = _testRunner.doRun(suite); 
          
          JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];
          
          Enumeration failures = result.failures();
          Enumeration errEnum = result.errors(); 
          
          int i=0;
          
          while ( errEnum.hasMoreElements()) {
            TestFailure tErr = (TestFailure) errEnum.nextElement();
            errors[i] = _makeJUnitError(tErr, className, true, fileName);
            i++;
          }
          
          while (failures.hasMoreElements()) {
            TestFailure tFail = (TestFailure) failures.nextElement();
            errors[i] = _makeJUnitError(tFail, className, false, fileName);
            i++;
          }
          
          _jvm.testSuiteFinished(errors);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    };
    t.start();
  }

  /**
   * Checks whether the given file name corresponds to
   * a valid JUnit TestCase.
   */
  private boolean _isTestCase(String className)
  {
    try {
      return Class.forName("junit.framework.TestCase")
        .isAssignableFrom(_testRunner.getLoader().load(className));
    }
    catch (ClassNotFoundException cnfe) {
      return false;
    }
  }
  
  /**
   * Constructs a new JUnitError from a TestFailure
   * @param failure A given TestFailure
   * @param className The class that contains the TestFailure
   * @param isError The passed TestFailure may signify either an error or a failure
   * @param fileName File where the error occurred
   * @return JUnitError 
   */
  private JUnitError _makeJUnitError(TestFailure failure, String className, boolean isError, String fileName) {
   
    Test failedTest = failure.failedTest();
    String testName = failedTest.getClass().getName();
    if (failedTest instanceof TestCase) {
      testName = ((TestCase)failedTest).getName();
    }
    
    StringWriter sWriter = new StringWriter();
    PrintWriter pWriter  = new PrintWriter(sWriter);
    
    failure.thrownException().printStackTrace(pWriter);
        
    String classNameAndTest = className + "." + testName;
    
    int lineNum = _lineNumber( sWriter.toString(), classNameAndTest);
//    if (lineNum > -1) _errorsWithPos++;
    
    String exception =  (isError) ? 
      failure.thrownException().toString(): 
      failure.thrownException().getMessage();
      
      return new JUnitError(new File(fileName), lineNum, 0, exception,
                            ! (failure.thrownException() instanceof AssertionFailedError),
                            testName,
                            sWriter.toString());
  }

  /**
   * 
   */
  private int _lineNumber (String sw, String classname) {
    
    int lineNum;

    int idxClassname = sw.indexOf(classname);
    if (idxClassname == -1) return -1;

    String theLine = sw.substring(idxClassname, sw.length());
    theLine = theLine.substring(theLine.indexOf(classname), theLine.length());
    theLine = theLine.substring(theLine.indexOf("(") + 1, theLine.length());
    theLine = theLine.substring(0, theLine.indexOf(")"));
    
    try {
      lineNum = new Integer(theLine.substring(
                                      theLine.indexOf(":") + 1,
                                      theLine.length())).intValue() - 1;
    } 
    catch (NumberFormatException e) {
      throw new UnexpectedException(e);
    }
    
    return lineNum;
  }
}
