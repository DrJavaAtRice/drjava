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

import java.io.File;
import edu.rice.cs.util.ClasspathVector;
import edu.rice.cs.util.classloader.ClassFileError;

/**
 * Callback interface which allows an JUnitModel to respond to
 * tests running in a remote JVM.
 * 
 * @version $Id$
 */
public interface JUnitModelCallback {
  
  /** Called from the JUnitTestManager if its given className is not a test case.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll);
  
 /** Called from the JUnitTestManager if it encounters an illegal class file.
   *  @param e the ClassFileError object describing the error
   */
  public void classFileError(ClassFileError e);
    /** Called to indicate that a suite of tests has started running.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests);
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName);
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful,
                        boolean causedError);
  
  /**
   * Called when a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors);
  
  /**
   * Called when the JUnitTestManager wants to open a file that is not currently open.
   * @param className the name of the class for which we want to find the file
   * @return the file associated with the given class
   */
  public File getFileForClassName(String className);

  /**
   * Returns the accumulated classpath in use by all Java interpreters,
   * in the form of a ClasspathVector which has the correct toString()
   */
  public ClasspathVector getClasspath();

  /**
   * Called when the JVM used for unit tests has registered.
   */
  public void junitJVMReady();
}