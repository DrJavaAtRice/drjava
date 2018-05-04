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

import java.io.File;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.Enumeration;
import java.util.Arrays;

import edu.rice.cs.drjava.model.coverage.CoverageMetadata;
import edu.rice.cs.drjava.model.coverage.ReportGenerator;

import edu.rice.cs.drjava.model.repl.newjvm.ClassPathManager;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.drjava.model.coverage.JacocoClassLoader;
import edu.rice.cs.plt.reflect.EmptyClassLoader;

import static edu.rice.cs.plt.debug.DebugUtil.error;

import junit.framework.JUnit4TestAdapter;

import junit.framework.AssertionFailedError;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.framework.TestFailure;
import junit.framework.JUnit4TestCaseFacade;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/** Runs in the InterpreterJVM. Runs tests given a classname and formats the results into a (serializable) array of 
  * JUnitError that can be passed back to the MainJVM.
  * @version $Id$
  */
public class JUnitParallelTestManager extends JUnitTestManager{
	public static Log _log = new Log("JUnitParallelTestManager.txt", false);
  
  
  
  /** The current testRunner; initially null.  Each test suite requires a new runner. */
  private JUnitParallelTestRunner _testRunner;
  /** class of of test cases, used to run test case in parallel*/
  private Vector<Class<?>>  cls=null;
  
  
  
  /** Standard constructor 
    * @param jmc a JUnitModelCallback
    * @param loaderFactory factory to create class loaders
    */
  public JUnitParallelTestManager(JUnitModelCallback jmc, ClassPathManager loaderFactory) {
    super(jmc,loaderFactory);
  }

  /** Find the test classes among the given classNames and accumulate them in
    * TestSuite for junit.  Returns null if a test suite is already pending.
    * @param classNames the (fully qualified) class names that are test class candidates
    * @param files Java File objects for the source files corresponding to classNames
    * @param coverageMetadata metadata to be used to generate the coverage report
    * @return list of test class names
    */
  @SuppressWarnings({"unchecked","rawtypes"})
  public List<String> findTestClasses(final List<String> classNames, final List<File> files, 
                                      final CoverageMetadata coverageMetadata) {
    
    _log.log("findTestClasses(" + classNames + ", " + files + ", " + coverageMetadata + ") called");
    boolean doCoverage = coverageMetadata.getFlag();
    
    // Set up the loader
    final ClassLoader defaultLoader = JUnitParallelTestManager.class.getClassLoader();
    final ClassLoader loader;
    if (! doCoverage) loader = _classPathManager.value(defaultLoader);
    else {
      // create a Jacoco runtime, output directory, report descriptors, and loader
      _coverageOutdir = coverageMetadata.getOutdirPath();
      _runtime = new LoggerRuntime();
      _myData = new RuntimeData();
      loader = new JacocoClassLoader(_classPathManager.getClassPath(), new Instrumenter(_runtime), defaultLoader);
      _nonTestClassNames = new ArrayList(classNames.size());
      try { _runtime.startup(_myData); }
      catch (Exception e) {
        _log.log("In code coverage startup, throwing the wrapped exception " + e);
        throw new UnexpectedException(e);
      }
    }
    
    if (_testClassNames != null && ! _testClassNames.isEmpty()) 
      throw new IllegalStateException("Test suite is still pending!");
    
    _log.log("Preparing to run test cases");
    _testRunner = makeRunner(loader);
    
    _testClassNames = new ArrayList<String>();
    _testFiles = new ArrayList<File>();
    _nonTestClassNames = new ArrayList(classNames.size());
    _suite = new TestSuite();
    cls=new Vector<Class<?>>();
    // Assemble test suite (as _suite) and return list of test class names
    for (Pair<String, File> pair : IterUtil.zip(classNames, files)) {
      String cName = pair.first();
      try {
        Class<?> possibleTest = _testRunner.loadPossibleTest(cName); 
        _log.log("Exploring possibleTest " + possibleTest);
        if (_isJUnitTest(possibleTest)) {
          _testClassNames.add(cName);
          _testFiles.add(pair.second());
          Test test = new JUnit4TestAdapter(possibleTest);
          cls.add(possibleTest);
          _suite.addTest(test); 
          _log.log("Adding test " + test + " to test suite"); 
        } else { // cName is a program class that is not a test class
          _nonTestClassNames.add(cName);
          _log.log("adding " + cName + " to nonTestClassNames");
        }
      }
      catch (ClassNotFoundException e) { error.log(e); }
      catch(LinkageError e) {
        //debug.log(e);
        String path = IOUtil.attemptAbsoluteFile(pair.second()).getPath();
        _jmc.classFileError(new ClassFileError(cName, path, e));
      }
    }
    
//    debug.logEnd("result", _testClassNames);
    _log.log("accumulated non test class names: " + _nonTestClassNames);
    _log.log("returning: " + _testClassNames);
    
    return _testClassNames;
  }
  
  /** Runs the pending test suite set up by the preceding call to findTestClasses.  Runs in a single auxiliary thread,
    * so no need for explicit synchronization.
    * @param runTestParallel    set whether we should run the test in parallel
    * @return false if no test suite (even an empty one) has been set up
    */
  public boolean runTestSuite(Boolean runTestParallel) {
    
    _log.log("runTestSuite() called");
    
    if (_testClassNames == null || _testClassNames.isEmpty()) {
      _finalResult = new JUnitResultTuple(false, null);
      return false;
    }
    Map<String, List<String>> lineColors = null;
    _finalResult = new JUnitResultTuple(true, null);
    
//    _log.log("runTestSuite() in SlaveJVM called");
    
    /* Declare fault array for amalgamating errors and failures */
    JUnitError[] faults = new JUnitError[0];
    try {
      _log.log("Calling _testRunner.runSuite(" + _suite + ")");

      
    //This is the result of runSuite  
    Result result=null;
    int faultCount=0;
    long startTime = System.nanoTime();
    long estimatedTime = System.nanoTime() - startTime;
    _log.log("isParallel= "+runTestParallel);
			if (runTestParallel == true) {
				@SuppressWarnings("rawtypes")
				Class[] clsArray = new Class[cls.size()];
				cls.copyInto(clsArray);
				_testRunner.setCountTestCases(_suite);
				startTime = System.nanoTime();
				result = _testRunner.runClass(clsArray);
				estimatedTime = System.nanoTime() - startTime;
				long Second=TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
				_log.log("when testing in parallel, testing time is :" + estimatedTime +" in nanoseconds");
				_log.log("when testing in parallel, testing time is :" + Second +" in second");

				faultCount = result.getFailureCount();
				_log.log("faultCount= " + faultCount);
				if (faultCount > 0) {

					faults = new JUnitError[faultCount];
					List<Failure> failure = result.getFailures();
					int i = 0;
					for (Failure failureIterator : failure) {
						// TestFailure error = failure.
						_log.log("failureIterator.getDescription() is " + failureIterator.getDescription());
						String testMethodName = failureIterator.getDescription().getMethodName();
						_log.log("failureIterator.getDescription().getTestClass()  is "
								+ failureIterator.getDescription().getTestClass());
						Test test = new JUnit4TestAdapter(failureIterator.getDescription().getTestClass());
						_log.log("test is " + test);
						_log.log("testName is " + testMethodName);
						TestFailure testFailure = new TestFailure(test, failureIterator.getException());
						faults[i] = _makeJUnitError(testFailure, _testClassNames, true, _testFiles, testMethodName);
						i++;
					}

				}

			}
			else {
				
				startTime = System.nanoTime();
				TestResult testResult = _testRunner.runSuite(_suite);
				estimatedTime = System.nanoTime() - startTime;
				long Second=TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
				_log.log("when testing in sequential, testing time is :" + estimatedTime +" in nanoseconds");
				_log.log("when testing in sequential, testing time is :" + Second +" in second");
				/* A fault is either an error or a failure. */
				faultCount = testResult.errorCount() + testResult.failureCount();
				if (faultCount > 0) {

					/*
					 * NOTE: TestFailure, a JUnit class, is misnamed; it should have been called
					 * TestFault with TestFailure and TestError as disjoint subtypes (e.g., classes)
					 */
					faults = new JUnitError[faultCount];
					Enumeration<TestFailure> failures = testResult.failures();
					Enumeration<TestFailure> errors = testResult.errors();

					int i = 0;

					// faults should be called faults! and makeJUnitError should be makeJUnitFault!
					while (errors.hasMoreElements()) {
						TestFailure error = errors.nextElement();
						faults[i] = _makeJUnitError(error, _testClassNames, true, _testFiles);
						i++;
					}

					while (failures.hasMoreElements()) {
						TestFailure failure = failures.nextElement();
						faults[i] = _makeJUnitError(failure, _testClassNames, false, _testFiles);
						i++;
					}
				}
			}


      _log.log("Testing doCoverage");
      
      if (_runtime != null) { /* doCoverage was true */
        _log.log("Analyzing coverage data for " + _nonTestClassNames);

        /* Collect session info (including which code was executed) */
        final ExecutionDataStore _executionDataStore = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        _myData.collect(_executionDataStore, sessionInfos, false);
        _log.log("Collected coverage information");
        _runtime.shutdown();
        
        /** Together with the original class definitions we can calculate coverage information. */
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(_executionDataStore, coverageBuilder);
        URLClassLoader urlCL = newURLLoader();
        
        String cName = null;
        try {
          for (int j = 0; j < _nonTestClassNames.size(); j++) {
            cName = _nonTestClassNames.get(j);
            InputStream is = urlCL.getResource(cName + ".class").openStream();
            _log.log("Constructed InputStream " + is + " for class " + cName);
            analyzer.analyzeClass(is, cName);
          } 
        } catch(Exception e) {
          throw new UnexpectedException(e, "Coverage analysis threw this exception while processing class " + cName);
        }
        
        /* Run the structure analyzer on the project source folder to build up the coverage model. In flat file
         * mode, only the first source directory (if there are multiple source directories) is analyzed.  TODO:
         * extend this analysis to all source directories for the open classes in flat file mode.
         */
        
        _log.log("Generating test coverage");
        IBundleCoverage bundleCoverage = coverageBuilder.getBundle("Coverage Summary");
        ReportGenerator rg = new ReportGenerator(_coverageOutdir, coverageBuilder);
        _log.log("Determining project root");
        _log.log("getProjectCP() = " + _classPathManager.getProjectFilesCP());
        File f = _classPathManager.getProjectFilesCP().iterator().next();
        if (! f.exists()) _log.log("****** Project root does not exist!");
        _log.log("Creating coverage report for code base rooted at " + f);
        rg.createReport(bundleCoverage, _executionDataStore, sessionInfos, f);
        lineColors = rg.getAllLineColors();
        _finalResult = new JUnitResultTuple(true, lineColors);
        
      } else {
        _log.log("runtime was null");
      }
      /* Reset the runtime */
      _runtime = null;
      _reset();
      _jmc.testSuiteEnded(faults);
    }
    //TODO  change error of line
    catch (Exception e) { 
      faults = new JUnitError[] { 
        new JUnitError(null, -1, -1, e.getMessage(), false, "", "", e.toString(), e.getStackTrace())
      };
      _log.log("Slave JVM: testSuite ended with faults:" + Arrays.toString(faults));
      _reset();
      _jmc.testSuiteEnded(faults);
    }
    
    _log.log("Exiting runTestSuite()");
    return _finalResult.getRetval();
  }
  
  private void _reset() {
    _suite = null;
    _testClassNames = null;
    _testFiles = null;
    _log.log("test manager state reset");
  }
  

  /** Constructs a new JUnitError from a TestFailure
    * @param failure A given TestFailure
    * @param classNames The classes that were used for this test suite
    * @param isError The passed TestFailure may signify either an error or a failure
    * @param files The files that were used for this test suite
    * @param testMethodName   name of tested method
    * @return JUnitError
    */
  private JUnitError _makeJUnitError(TestFailure failure, List<String> classNames, boolean isError, List<File> files,String testMethodName) {
    
   _log.log("_makeJUnitError called with failure " + failure + " failedTest = " + failure.failedTest());
    Test failedTest = failure.failedTest();
    _log.log("failedTest " + failedTest);
    _log.log("failure.exceptionMessage " + failure.exceptionMessage());
    _log.log("failure.toString " + failure.toString());
   
    String testString = failure.toString();
    
    /** junit can return a string in two different formats; we parse both formats, and then decide which one to use. */

     //needtodo
    String className=failedTest.toString();
    String testName=testMethodName;
    _log.log("className " + className +" testName "+testName);
    
    String classNameAndTest = className + "." + testName;
//   _log.log("classNameAndTest = " + classNameAndTest);
    String exception = failure.thrownException().toString();
    StackTraceElement[] stackTrace = failure.thrownException().getStackTrace();
    
    /* Check to see if the class and test name appear directly in the stack trace. If
     * they don't, then we'll have to do additional work to find the line number. Additionally,
     * if the exception occured in a subclass of the test class, we'll need to adjust our conception
     * of the class name.
     */
    StringBuilder sb = new StringBuilder();
    sb.append(exception);
    sb.append('\n');
    for(StackTraceElement s: stackTrace) {
      sb.append("\tat ");
      sb.append(s);
    }
    String combined = sb.toString();
    int lineNum = -1;
    _log.log("start  to  cal lineNum");
	
    if (combined.indexOf(classNameAndTest) == -1) {
      /* get the stack trace of the junit error */
      String trace = failure.trace();
      _log.log("failure.trace is " +trace);
      /* knock off the first line of the stack trace.
       * now the string will look like
       * at my.package.class(file.java:line)
       * at other.package.class(anotherfile.java:line)
       * etc...
       */
      trace = trace.substring(trace.indexOf('\n')+1);
      if (trace.trim().length()>0) {
        while (trace.indexOf("junit.framework") != -1 &&
               trace.indexOf("junit.framework") < trace.indexOf("(")) {
          /* the format of the trace will have "at junit.framework.Assert..."
           * and "junit.framework.TestCase..."
           * on each line until the line of the actual source file.
           * if the exception was thrown from the test case (so the test failed
           * without going through assert), then the source file will be on
           * the first line of the stack trace
           */
          trace = trace.substring(trace.indexOf('\n') + 1);
        }
        
        _log.log("trace after junit.framework.Assert is " +trace);
        _log.log("testName is " +testName);
        trace = trace.substring(trace.indexOf(testName)+2);
        trace = trace.substring(0, trace.indexOf(')'));
        
        _log.log("trace after substring " +trace);
        // If the exception occurred in a subclass of the test class, then update our
        // concept of the class and test name. Otherwise, we're only here to pick up the
        // line number.
        if (combined.indexOf(className) == -1) {
          int dotPos = trace.lastIndexOf('.');
          if (dotPos!=-1) {
            className = trace.substring(0,dotPos);
            classNameAndTest = className + "." + testName;
          }
        }
        
        try {
        	_log.log("trace is " +trace);
          lineNum = Integer.parseInt(trace.substring(trace.indexOf(':') + 1)) - 1;
        }
        catch (NumberFormatException e) { lineNum = 0; } // may be native method
      }      
    }
    _log.log("lineNum is " +lineNum);
    if (lineNum < 0) {
      lineNum = _lineNumber(combined, classNameAndTest);
    }
    
//    if (lineNum > -1) _faultsWithPos++;
    
    String message =  (isError) ? failure.thrownException().toString(): 
      failure.thrownException().getMessage();
    
    boolean isFailure = (failure.thrownException() instanceof AssertionError ||
        failure.thrownException() instanceof AssertionFailedError) &&
        !classNameAndTest.equals("junit.framework.TestSuite$1.warning");
    
//    for debugging    
//    try {
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
      return new JUnitError(new File("nofile"), 0, 0, message, !isFailure, testName, className, exception, stackTrace);
    }
    
    return new JUnitError(file, lineNum, 0, message, !isFailure, testName, className, exception, stackTrace);
  }
  


  
  
  /** @param loader current template for the runner's class loader
   * @return a fresh JUnitTestRunner with its own class loader instance. 
   */
 protected JUnitParallelTestRunner makeRunner(ClassLoader loader) {
   return new JUnitParallelTestRunner(_jmc, loader);
 }
}
