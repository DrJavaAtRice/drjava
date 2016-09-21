/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import junit.framework.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;

import java.lang.reflect.Modifier;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

import edu.rice.cs.drjava.model.compiler.LanguageLevelStackTraceMapper;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import edu.rice.cs.drjava.model.coverage.*;

import edu.rice.cs.util.UnexpectedException;

/** Runs in the InterpreterJVM. Runs tests given a classname and formats the results into a (serializable) array of 
  * JUnitError that can be passed back to the MainJVM.
  * @version $Id$
  */
public class JUnitTestManager {
 
  protected static final Log _log = new Log("JUnitTestManager.txt", false);
  
  /** The interface to the master JVM via RMI. */
  private final JUnitModelCallback _jmc;
  
  /** A factory producing a ClassLoader for tests with the given parent */
  private final Lambda<ClassLoader, ClassLoader> _loaderFactory;
  
  /** The current testRunner; initially null.  Each test suite requires a new runner. */
  private JUnitTestRunner _testRunner;
  
  /** The accumulated test suite; null if no test is pending. */
  private TestSuite _suite = null;
  
  /** The accumulated list of names of TestCase classes; null if no test is pending. */
  private List<String> _testClassNames = null;
  
  /** The list of files corresponding to testClassNames; null if no test is pending. */
  private List<File> _testFiles = null;
  
  // For JaCoCo
  private String coverageOutdir = null;
  private IRuntime runtime = null;
  private RuntimeData myData = null;
  private List<String> classNames = null;
  private List<File> files = null;
  private JUnitResultTuple lastResult = new JUnitResultTuple(false, null);

  /** 
   * Standard constructor 
   * @param jmc a JUnitModelCallback
   * @param loaderFactory factory to create class loaders
   */
  public JUnitTestManager(JUnitModelCallback jmc, Lambda<ClassLoader, ClassLoader> loaderFactory) {
    _jmc = jmc;
    _loaderFactory = loaderFactory;
  }

  /** @return result of the last JUnit run */  
  public JUnitResultTuple getLastResult() {
    return this.lastResult;
  }

  /** 
   * Find the test classes among the given classNames and accumulate them in
   * TestSuite for junit.  Returns null if a test suite is already pending.
   * @param classNames the class names that are test class candidates
   * @param files the files corresponding to classNames
   * @param coverageMetadata metadata to be used to generate the coverage report
   * @return list of test class names
   */
  public List<String> findTestClasses(final List<String> classNames, 
    final List<File> files, CoverageMetadata coverageMetadata) {

    boolean doCoverage = coverageMetadata.getFlag();

    // Set up the loader
    final ClassLoader loader;
    if (!doCoverage) {
        loader = JUnitTestManager.class.getClassLoader();
    } else {

        // JaCoCo: Create instrumented versions of class files.
        this.coverageOutdir = coverageMetadata.getOutdirPath();
        this.runtime = new LoggerRuntime();
        this.myData = new RuntimeData();
        this.classNames = classNames;
        this.files = files;
        final ArrayList<byte[]> instrumenteds = new ArrayList<byte[]>();

        // The Instrumenter creates a modified version of our test target class
        // that contains additional probes for execution data recording:
        for (int i = 0 ; i< files.size() ; i++) {

            // Instrument the i-th file
            try {
                final Instrumenter instr = new Instrumenter(this.runtime);
                final byte[] instrumented = instr.instrument(
                    new FileInputStream(files.get(i).getCanonicalPath().
                    replace(".java", ".class")), classNames.get(i));
                String[] pathParts = files.get(i).getAbsolutePath().split("/");
                instrumenteds.add(instrumented);

            } catch (Exception e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                //Utilities.show("Exception during instrumentation: " + stackTrace.toString());
            }
        }

        loader = new MemoryClassLoader();
        for (int i = 0; i < classNames.size(); i++) {
            ((MemoryClassLoader)loader).addDefinition(classNames.get(i), instrumenteds.get(i));
        }

        try {
            this.runtime.startup(myData);
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

//    debug.logStart(new String[]{"classNames", "files"}, classNames, files);
    _log.log("findTestClasses(" + classNames + ", " + files + ")");
    
    if (_testClassNames != null && ! _testClassNames.isEmpty()) 
      throw new IllegalStateException("Test suite is still pending!");
    
    _testRunner = makeRunner(loader);
    
    _testClassNames = new ArrayList<String>();
    _testFiles = new ArrayList<File>();
    _suite = new TestSuite();
    
    for (Pair<String, File> pair : IterUtil.zip(classNames, files)) {
      String cName = pair.first();
      try {
        Class<?> possibleTest = _testRunner.loadPossibleTest(cName); 
        if (_isJUnitTest(possibleTest)) {
          _testClassNames.add(cName);
          _testFiles.add(pair.second());
          _suite.addTest(new JUnit4TestAdapter(possibleTest));
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
    _log.log("returning: " + _testClassNames);

    return _testClassNames;
  }
  
  /** Runs the pending test suite set up by the preceding call to findTestClasses.  Runs in a single auxiliary thread,
    * so no need for explicit synchronization.
    * @return false if no test suite (even an empty one) has been set up
    */
  public /* synchronized */ boolean runTestSuite() {

    _log.log("runTestSuite() called");
    
    if (_testClassNames == null || _testClassNames.isEmpty()) {
        this.lastResult = new JUnitResultTuple(false, null);
        return false;
    }
    Map<String, List<String>> lineColors = null;
    this.lastResult = new JUnitResultTuple(true, null);

//    Utilities.show("runTestSuite() in SlaveJVM called");
    
    try {
//      System.err.println("Calling _testRunner.runSuite(...)");
      TestResult result = _testRunner.runSuite(_suite);
      
      JUnitError[] errors = new JUnitError[result.errorCount() + result.failureCount()];
      Enumeration<TestFailure> failures = result.failures();
      Enumeration<TestFailure> errEnum = result.errors();
      
      int i = 0;

      while (errEnum.hasMoreElements()) {
        TestFailure tErr = errEnum.nextElement();
        errors[i] = _makeJUnitError(tErr, _testClassNames, true, _testFiles);
        i++;
      }

      while (failures.hasMoreElements()) {
        TestFailure tFail = failures.nextElement();
        errors[i] = _makeJUnitError(tFail, _testClassNames, false, _testFiles);
        i++;
      }
       
      _reset();
      _jmc.testSuiteEnded(errors);

    if (this.runtime != null) { /* doCoverage was true */

        /* Collect session info (including which code was executed) */
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        myData.collect(executionData, sessionInfos, false);
        this.runtime.shutdown();

        /**
         * Together with the original class definitions we can calculate 
         * coverage information
         */
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        try {
            for (int j = 0; j < classNames.size(); j++) {
                analyzer.analyzeClass(
                    new FileInputStream(this.files.get(j).getCanonicalPath().
                    replace(".java", ".class")), this.classNames.get(j));
            }

            /**
             * Run the structure analyzer on a single class folder to build up
             * the coverage model. The process would be similar if the classes
             * were in a jar file; typically you would create a bundle for each
             * class folder and each jar you want in your report. If you have
             * more than one bundle you will need to add a grouping node to your
             * report
             */
            final IBundleCoverage bundleCoverage = coverageBuilder.getBundle(
                this.files.get(0).getParentFile().getName());
            ReportGenerator rg = new ReportGenerator(this.coverageOutdir, 
                coverageBuilder); 
            rg.createReport(bundleCoverage, executionData, 
                sessionInfos, this.files.get(0).getParentFile());
            lineColors = rg.getAllLineColors();
            this.lastResult = new JUnitResultTuple(true, lineColors);

        } catch (Exception e) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            //Utilities.show(stackTrace.toString());
        }

        /* Reset the runtime */
        this.runtime = null;
      }
    }

    catch (Exception e) { 
      JUnitError[] errors = new JUnitError[1];      
      errors[0] = new JUnitError(null, -1, -1, e.getMessage(), false, "", "", e.toString(), e.getStackTrace());
      _reset();
      _jmc.testSuiteEnded(errors);
//      new ScrollableDialog(null, "Slave JVM: testSuite ended with errors", "", Arrays.toString(errors)).show();
    }

    _log.log("Exiting runTestSuite()");
    return this.lastResult.getRetval();
  }
  
  private void _reset() {
    _suite = null;
    _testClassNames = null;
    _testFiles = null;
    _log.log("test manager state reset");
  }
  

    
  /** Determines if the given class is a junit Test.
    * @param c the class to check
    * @return true iff the given class is an instance of junit.framework.Test
    */
  private boolean _isJUnitTest(Class<?> c) {

    boolean result = (Test.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers()) ||
      (new JUnit4TestAdapter(c).getTests().size()>0)) && !new JUnit4TestAdapter(c).getTests().get(0).toString().contains("initializationError")
      ; //had to add specific check for initializationError. Is there a better way of checking if a class contains a test?
    debug.logValues(new String[]{"c", "isJUnitTest(c)"}, c, result);
    return result;
  }
  
  /** Constructs a new JUnitError from a TestFailure
    * @param failure A given TestFailure
    * @param classNames The classes that were used for this test suite
    * @param isError The passed TestFailure may signify either an error or a failure
    * @param files The files that were used for this test suite
    * @return JUnitError
    */
  private JUnitError _makeJUnitError(TestFailure failure, List<String> classNames, boolean isError, List<File> files) {
    
//    Utilities.show("_makeJUnitError called with failure " + failure + " failedTest = " + failure.failedTest());
    Test failedTest = failure.failedTest();
    String testName;
    /*if (failedTest instanceof TestCase) testName = ((TestCase)failedTest).getName();
    else */ if(failedTest instanceof JUnit4TestCaseFacade)
    {
      testName = ((JUnit4TestCaseFacade) failedTest).toString(); 
      testName = testName.substring(0,testName.indexOf('(')); //shaves off the class from TestName string
    }
    else testName = failedTest.getClass().getName();
    
    String testString = failure.toString();
    int firstIndex = testString.indexOf('(') + 1;
    int secondIndex = testString.indexOf(')');
    
    /** junit can return a string in two different formats; we parse both formats, and then decide which one to use. */
    
    String className;
    if (firstIndex != secondIndex)
      className = testString.substring(firstIndex, secondIndex);
    else
      className = testString.substring(0, firstIndex-1);
    

    String classNameAndTest = className + "." + testName;
//    Utilities.show("classNameAndTest = " + classNameAndTest);
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
    
    if (combined.indexOf(classNameAndTest) == -1) {
      /* get the stack trace of the junit error */
      String trace = failure.trace();
      /* knock off the first line of the stack trace.
       * now the string will look like
       * at my.package.class(file.java:line)
       * at other.package.class(anotherfile.java:line)
       * etc...
       */
      trace = trace.substring(trace.indexOf('\n')+1);
      if (trace.trim().length()>0) {
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
          lineNum = Integer.parseInt(trace.substring(trace.indexOf(':') + 1)) - 1;
        }
        catch (NumberFormatException e) { lineNum = 0; } // may be native method
      }      
    }

    if (lineNum < 0) {
      lineNum = _lineNumber(combined, classNameAndTest);
    }
    
//    if (lineNum > -1) _errorsWithPos++;

    String message =  (isError) ? failure.thrownException().toString(): 
      failure.thrownException().getMessage();

    boolean isFailure = (failure.thrownException() instanceof AssertionError || failure.thrownException() instanceof AssertionFailedError) &&
      !classNameAndTest.equals("junit.framework.TestSuite$1.warning");
    
//    for debugging    
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
    
    // if testClass contains no 
    
    // a test didn't fail, we couldn't even open the test.
    if (file == null) {
      return new JUnitError(new File("nofile"), 0, 0, message, !isFailure, testName, className, exception, stackTrace);
    }
    
    return new JUnitError(file, lineNum, 0, message, !isFailure, testName, className, exception, stackTrace);
  }
  
  /** 
   * Parses the line number out of the stack trace in the given class name. 
   * @param sw stack trace
   * @param classname class in which stack trace was generated
   * @return the line number
   */
  private int _lineNumber(String sw, String classname) {
    // TODO: use stack trace elements to find line number
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
    catch (NumberFormatException e) { lineNum = 0; } // may be native method
    
    return lineNum;
  }
  
  /** 
   * @param current template for the runner's class loader
   * @return a fresh JUnitTestRunner with its own class loader instance. 
   */
  private JUnitTestRunner makeRunner(ClassLoader current) {
    return new JUnitTestRunner(_jmc, _loaderFactory.value(current));
  }
}
