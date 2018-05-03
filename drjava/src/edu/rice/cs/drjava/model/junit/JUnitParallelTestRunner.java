/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import junit.runner.*;
import junit.framework.*;

import java.util.Vector;

import org.junit.experimental.ParallelComputer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.AllTests;
import org.junit.runners.Suite.SuiteClasses;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

/** DrJava's own testrunner. It updates the document in the JUnit pane as error and failure events are fired.  
  * These methods run inan auxiliary thread.
  *  @version $Id$
  */
public class JUnitParallelTestRunner extends JUnitTestRunner {
  
  protected static final Log _log = new Log("JUnitParallelTestRunner.txt", false);
  
  protected Vector<String> failedTest=new Vector<String>(); 
  
  

  /** Standard constructor. 
   * @param jmc a JUnitModelCallback
   * @param loader class loader to use during testing
   */
  public JUnitParallelTestRunner(JUnitModelCallback jmc, ClassLoader loader) {
    super(jmc,loader);
  }

  
  //Result result= JUnitCore.runClasses(new ParallelComputer(true, true), clsArray);
  //Result result = _testRunner.runSuite(_suite);
  
  //how many test cases will run
  private int countTestCases=0;
  /**
   * This method will manually set the number of test cases(number of method)
   * @param suite  testSuite that contain all test cases
   */
  public void setCountTestCases(TestSuite suite)
  {
	    _log.log("getCountTestCases");

	  countTestCases=suite.countTestCases();
	  //suite.run(_result);
  }

	/**
	 * method to run class
	 * 
	 * @param classes
	 * @return
	 */
	public synchronized Result runClass(Class<?>... classes) {
		_log.log("start run in runclass,countTestCases= " + countTestCases);

		// Reset all bookkeeping
		_errorCount = 0;
		_failureCount = 0;

		// Run the test
		_result = new TestResult();
		_result.addListener(this);
		_jmc.testSuiteStarted(countTestCases);
		MyJUnitCore myJunitCore = new MyJUnitCore();
		return myJunitCore.parallelRunClasses(new RunListener() {
			public void testStarted(Description description) {
				_log.log("  in testStarted " + description.getMethodName());
				// The parameter testName of testStarted is the form of testMethod(testClass)
				_jmc.testStarted(description.getMethodName() + "(" + description.getClassName() + ")");
			}

			public void testFinished(Description description) {
				_log.log("  in testEnded " + description.getMethodName());
				String testName = description.getClassName() + ":" + description.getMethodName();
				boolean success;
				if (failedTest.indexOf(testName) == -1)
					success = true;
				else
					success = false;
				// The parameter testName of testStarted is the form of testMethod(testClass)
				_jmc.testEnded(description.getMethodName() + "(" + description.getClassName() + ")", success, false);
			}

			public void testFailure(Failure failure) {
				_log.log("  in testFailed " + failure.getMessage());
				String testName = failure.getDescription().getClassName() + ":"
						+ failure.getDescription().getMethodName();
				failedTest.add(testName);
			}

		}, classes);
	}
   
}
