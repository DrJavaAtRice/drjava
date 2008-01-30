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

import java.io.PrintStream;

import junit.runner.*;
import junit.framework.*;
import junit.textui.TestRunner;

/** DrJava's own testrunner. It updates the document in the JUnit pane as error and failure events are fired.
 *  @version $Id$
 */
public class JUnitTestRunner extends TestRunner {
  
  /** Receives updates on the test suite's progress. */
  private JUnitModelCallback _jmc;

  /** Used to tie the output of the ui textrunner to nothing. */
  private PrintStream _writer;

  /** Class loader that uses DrJava's classpath. Overrides the super class' loader. */
  private TestSuiteLoader _classLoader;

  /** The JUnit TestResult being accumulated. */
  private TestResult _result;

  /** The current number of errors in the result. */
  private int _errorCount;

  /** The current number of failures in the result. */
  private int _failureCount;

  /** Standard constructor. */
  public JUnitTestRunner(JUnitModelCallback jmc) {
    super();
    _jmc = jmc;
    _classLoader = new DrJavaTestSuiteLoader(jmc);
    _writer = new PrintStream(System.out) {
      public void print(String s) { }
      public void println(String s) { }
      public void println() { }
    };

    _errorCount = 0;
    _failureCount = 0;
  }

  public synchronized TestResult doRun(Test suite) {
    // Reset all bookkeeping
    _errorCount = 0;
    _failureCount = 0;

    // Run the test
    _result = createTestResult();
    _result.addListener(this);
    _jmc.testSuiteStarted(suite.countTestCases());
//    long startTime = System.currentTimeMillis();
    suite.run(_result);
//    long endTime = System.currentTimeMillis();
//    long runTime = endTime - startTime;
//    fPrinter.print(result, runTime);
    return _result;
  }

  /** Overrides method in super class to always return a reloading test suite loader. */
  public TestSuiteLoader getLoader() { return _classLoader; }

  /** Provides our own PrintStream which outputs to the appropriate document. */
  protected PrintStream getWriter() { return _writer; }

  protected PrintStream writer() { return getWriter(); }

  /** Called by JUnit when a test is started. */
  public synchronized void startTest(Test test) { _jmc.testStarted(test.toString()); }

  /** Called by JUnit when a test has finished. */
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
    boolean success = ! (failure || error);
    _jmc.testEnded(test.toString(), success, failure);
  }
}
