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

import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;
import edu.rice.cs.util.UnexpectedException;

import java.io.PrintStream;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.Color;

import junit.runner.*;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * DrJava's own testrunner. It updates the document in the
 * JUnit pane as error and failure events are fired.
 *
 * @version $Id$
 */
public class JUnitTestRunner extends TestRunner {
  /**
   * Receives updates on the test suite's progress.
   */
  private JUnitModelCallback _jmc;

  /**
   * Used to tie the output of the ui textrunner
   * to nothing.
   */
  private PrintStream _writer;

  /**
   * Class loader that uses DrJava's classpath. Overrides the super class' loader.
   */
  private TestSuiteLoader _classLoader;

  /**
   * The JUnit TestResult being accumulated.
   */
  private TestResult _result;

  /**
   * The current number of errors in the result.
   */
  private int _errorCount;

  /**
   * The current number of failures in the result.
   */
  private int _failureCount;

  /**
   * Constructor
   */
  public JUnitTestRunner(JUnitModelCallback jmc) {
    super();
    _jmc = jmc;
    _classLoader = new DrJavaTestClassLoader(jmc);
    _writer = new PrintStream(System.out) {
      public void print(String s) {
      }
      public void println(String s) {
      }
      public void println() {
      }
    };

    _errorCount = 0;
    _failureCount = 0;
  }

  public TestResult doRun(Test suite) {

    // Reset all bookkeeping
    _errorCount = 0;
    _failureCount = 0;
    _jmc.testSuiteStarted(suite.countTestCases());

    // Run the test
    _result = createTestResult();
    _result.addListener(this);
//    long startTime = System.currentTimeMillis();
    suite.run(_result);
//    long endTime = System.currentTimeMillis();
//    long runTime = endTime - startTime;
//    fPrinter.print(result, runTime);

    return _result;
  }

  /**
   * Overrides method in super class to always return a
   * reloading test suite loader.
   */
  public TestSuiteLoader getLoader() {
    return _classLoader;
  }

  /**
   * Provides our own PrintStream which outputs
   * to the appropriate document.
   */
  protected PrintStream getWriter() {
    return _writer;
  }

  protected PrintStream writer() {
    return getWriter();
  }

  /**
   * Called by JUnit when a test is started.
   */
  public synchronized void startTest(Test test) {
    _jmc.testStarted(test.toString());
  }

  /**
   * Called by JUnit when a test has finished.
   */
  public synchronized void endTest(Test test) {
    boolean error = false;
    boolean failure = false;
    if (_result.errorCount() > _errorCount) {
      error = true;
      _errorCount++;
    }
    if (_result.failureCount() > _failureCount) {
      failure = true;
      _failureCount++;
    }
    boolean success = !(failure || error);
    _jmc.testEnded(test.toString(), success, failure);
  }
}
