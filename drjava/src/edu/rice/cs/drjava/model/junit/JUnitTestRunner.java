/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.junit;

import junit.runner.*;
import junit.framework.*;

import edu.rice.cs.util.UnexpectedException;

/** DrJava's own testrunner. It updates the document in the JUnit pane as error and failure events are fired.  These methods run in
  * an auxiliary thread.
 *  @version $Id: JUnitTestRunner.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class JUnitTestRunner extends BaseTestRunner {
  
  /** Receives updates on the test suite's progress. */
  private volatile JUnitModelCallback _jmc;

  /** Class loader that uses DrJava's classpath. */
  private volatile ClassLoader _loader;

  /** The JUnit TestResult being accumulated. */
  private volatile TestResult _result;

  /** The current number of errors in the result. */
  private volatile int _errorCount;

  /** The current number of failures in the result. */
  private volatile int _failureCount;

  /** Standard constructor. */
  public JUnitTestRunner(JUnitModelCallback jmc, ClassLoader loader) {
    super();
    _jmc = jmc;
    _loader = loader;
    _result = null;
    _errorCount = 0;
    _failureCount = 0;
  }
 
  public synchronized TestResult runSuite(TestSuite suite) {
    // Reset all bookkeeping
    _errorCount = 0;
    _failureCount = 0;

    // Run the test
    _result = new TestResult();
    _result.addListener(this);
    _jmc.testSuiteStarted(suite.countTestCases());
    suite.run(_result);
    return _result;
  }
  
  public synchronized Class<?> loadPossibleTest(String className) throws ClassNotFoundException {
    return _loader.loadClass(className);
  }
  
  @Override protected synchronized Class<? extends TestCase> loadSuiteClass(String className) throws ClassNotFoundException {
    return loadPossibleTest(className).asSubclass(TestCase.class);
  }

  /** Called by BaseTestRunner when a test is started. */
  @Override public synchronized void testStarted(String testName) {
    _jmc.testStarted(testName);
  }

  /** Called by JUnit when a test has finished. */
  @Override public synchronized void testEnded(final String testName) {
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
    _jmc.testEnded(testName, success, failure);
  }
  
  @Override public synchronized void testFailed(int status, Test test, Throwable t) {
    // ignore
  }
  
  @Override protected synchronized void runFailed(String message) {
    throw new UnexpectedException(message);
  }  
}
