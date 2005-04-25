/*BEGIN_COPYRIGHT_BLOCK
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
import java.io.FileFilter;
import java.io.IOException;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.IGetDocuments;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.AlreadyOpenException;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.util.ClasspathVector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.ScrollableDialog;
import edu.rice.cs.util.classloader.ClassFileError;
import org.apache.bcel.classfile.*;
// TODO: remove swing dependency!
import javax.swing.text.StyledDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.BadLocationException;


/**
 * Manages unit testing via JUnit.
 *
 * TODO: Remove dependence on GlobalModel
 *
 * @version $Id$
 */
public class DefaultJUnitModel implements JUnitModel, JUnitModelCallback {
  
  /** Manages listeners to this model. */
  private final JUnitEventNotifier _notifier = new JUnitEventNotifier();
  
  /** Used by CompilerErrorModel to open documents that have errors. */
  private final IGetDocuments _getter;
  
  /**
   * RMI interface to a secondary JVM for running tests.
   * Using a second JVM prevents tests from disrupting normal usage of DrJava.
   */
  private final MainJVM _jvm;
  
  /** Compiler model, used as a lock to prevent simultaneous test and compile.
   *  Typed as an Object to prevent usage as anything but a lock.
   */
  private final Object _compilerModel;
  
  /** GlobalModel, used only for getSourceFile. */
  private final GlobalModel _model;
  
  /** The error model containing all current JUnit errors. */
  private JUnitErrorModel _junitErrorModel;
  
  /** State flag to prevent losing results of a test in progress. */
  private boolean _testInProgress = false;
  
  /** The document used to display JUnit test results.
   *  Used only for testing. */
  private final StyledDocument _junitDoc = new DefaultStyledDocument();
  
  /**
   * Main constructor.
   * @param getter source of documents for this JUnitModel
   * @param jvm RMI interface to a secondary JVM for running tests
   * @param compilerModel the CompilerModel, used only as a lock to prevent
   *                      simultaneous test and compile
   * @param model used only for getSourceFile
   */
  public DefaultJUnitModel(IGetDocuments getter, MainJVM jvm,
                           CompilerModel compilerModel, GlobalModel model) {
    _getter = getter;
    _jvm = jvm;
    _compilerModel = compilerModel;
    _model = model;
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], getter, false);
  }
  
  
  //-------------------------- Listener Management --------------------------//
  
  /**
   * Add a JUnitListener to the model.
   * @param listener a listener that reacts to JUnit events
   */
  public void addListener(JUnitListener listener) { _notifier.addListener(listener); }
  
  /**
   * Remove a JUnitListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to JUnit events
   */
  public void removeListener(JUnitListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all JUnitListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  
  //-------------------------------- Triggers --------------------------------//
  
  /** Used only for testing. */
  public StyledDocument getJUnitDocument() { return _junitDoc; }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If the class file 
   *  associated with a file is not a test case, it will be ignored.  Synchronized against the compiler
   *  model to prevent testing and compiling at the same time, which would create invalid results.
   */
  public void junitAll() { junitDocs(_getter.getDefinitionsDocuments()); }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If the class file 
   *  associated with a file is not a test case, it will be ignored.  Synchronized against the compiler
   *  model to prevent testing and compiling at the same time, which would create invalid results.
   */
  public void junitProject() {
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    
    for (OpenDefinitionsDocument doc : _getter.getDefinitionsDocuments()) { 
      if (doc.isInProjectPath() || doc.isAuxiliaryFile())  lod.add(doc);
    }
    junitDocs(lod);
  }
  
  /** Forwards the classnames and files to the test manager to test all of them does not notify 
   *  since we don't have ODD's to send out with the notification of junit start.
   *  @param qualifiedClassnames a list of all the qualified class names to test.
   *  @param files a list of their source files in the same order as qualified class names.
   */
  public void junitAll(List<String> qualifiedClassnames, List<File> files) {
    synchronized (_compilerModel) {
      synchronized (this) {
        if (_testInProgress) return;
        _testInProgress = true;
      }
      List<String> testClasses;
      try { testClasses = _jvm.findTestClasses(qualifiedClassnames, files); }
      catch(IOException e) { throw new UnexpectedException(e); }
      
      if (testClasses.isEmpty()) {
        nonTestCase(true);
        return;
      }
      _notifier.junitAllStarted(); 
      try { _jvm.runTestSuite(); }
      catch(IOException e) { 
        _notifier.junitEnded();
        synchronized (this) { _testInProgress = false;}
       throw new UnexpectedException(e); 
      }
    }
  }
  
  public void junitDocs(List<OpenDefinitionsDocument> lod) { junitOpenDefDocs(lod, true); }
  
  /** Runs JUnit on the current document. It formerly compiled all open documents before testing 
   *  but have removed that requirement in order to allow the debugging of test cases. If the 
   *  classes being tested are out of sync, a message is displayed.
   */
  public void junit(OpenDefinitionsDocument doc) throws ClassNotFoundException, IOException {
//    new ScrollableDialog(null, "junit(" + doc + ") called in DefaultJunitModel", "", "").show();
    try {
      // try to get the file, to make sure it's not untitled. if it is, it'll throw an IllegalStateException
      File testFile = doc.getFile();
      LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
      lod.add(doc);
      junitOpenDefDocs(lod, false);
    }
    catch (IllegalStateException e) {
      // No file exists, don't try to compile and test
      nonTestCase(false);
      return;
    }
    catch (NoClassDefFoundError e) {
      // Method getTest in junit.framework.BaseTestRunner can throw a
      // NoClassDefFoundError (via reflection).
        _notifier.junitEnded();
        synchronized (this) { _testInProgress = false; }
        throw e;
    }
    catch (ExitingNotAllowedException enae) {  // test attempted to call System.exit
      _notifier.junitEnded();  // balances junitStarted()
      synchronized (this) { _testInProgress = false; }
      throw enae;
    }
  }
  
  private void junitOpenDefDocs(List<OpenDefinitionsDocument> lod, boolean allTests) {
    // If a test is running, don't start another one.
    
    // Set _testInProgress flag
    synchronized (this) { 
      if (_testInProgress) return; 
      _testInProgress = true;
    }
      
    //reset the JUnitErrorModel, fixes bug #907211 "Test Failures Not Cleared Properly".
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
    
    File builtDir = _model.getBuildDirectory();
    LinkedList<File> classDirs = new LinkedList<File>();
    LinkedList<File> sourceFiles = new LinkedList<File>();
    
    //Gets system classpaths from the main JVM so that junit tests can find every class file.
    //Given as one long String, this separates the paths into a list of strings. 3/12/05
    LinkedList<String> classpaths = new LinkedList<String>();
    String cpString = getClasspath().toString();
    int cpLength = cpString.length();
    if (cpString.indexOf(File.pathSeparatorChar) == -1 && cpLength > 0) classpaths.add(cpString);
    else {
      int cpIndex;
      while ((cpIndex = cpString.indexOf(File.pathSeparatorChar)) != -1 && cpLength != 1) {
        if (cpIndex == 0) cpString = cpString.substring(1, cpLength);
        else {
          classpaths.add(cpString.substring(0, cpIndex));
          cpString = cpString.substring(cpIndex, cpLength-1);
          cpLength = cpString.length();
        }
      }
    }
    
    // new ScrollableDialog(null, "classpaths assembled in junitOpenDefDocs: " + classpaths, "", "").show();
    //First adds the default document build directory to the class directories.
    for (OpenDefinitionsDocument doc: lod) {
      try {
        String packageName;
        try { packageName = doc.getPackageName(); }
        catch(InvalidPackageException e) { packageName = ""; }
        packageName = packageName.replace('.', File.separatorChar);
        
        // Keep a record of unique built directories
        if (builtDir == null) builtDir = doc.getSourceRoot();
        File temp = new File(builtDir.getCanonicalPath() + File.separator + packageName);
        if (!classDirs.contains(temp)) classDirs.add(temp);
      }
      
      catch(IOException e) { /* do nothing b/c the directory doesn't exist */ }
      catch(InvalidPackageException e) { /* do nothing, b/c it's package is bogus */ }
    }
    
    // new ScrollableDialog(null, "builtDir " + builtDir + " added to classpath", "", "").show();
    // Next, add the JVM class paths to the class directories.
    // Junit will look here if the default build directories don't have the desired classes.
    // TODO: fuse this loop with the preceding one
    for (OpenDefinitionsDocument doc: lod) {
      try {
        String packageName;
        try { packageName = doc.getPackageName(); }
        catch(InvalidPackageException e) { packageName = "";}
        packageName = packageName.replace('.', File.separatorChar);
        
        //Add unique classpaths to the list of class directories that junit tests look through. 3/12/05
        for (String classpath: classpaths) {
          File temp = new File(new File(classpath).getCanonicalPath());
          if (temp.isDirectory()) {
            temp = new File(temp.getCanonicalPath() + File.separator + packageName);
            if (!classDirs.contains(temp)) classDirs.addLast(temp);
          }
        }
      }
      catch(IOException e) { /* do nothing b/c the directory doesn't exist */ }
    }
    
    // new ScrollableDialog(null, "classDirs assembled", "", classDirs.toString()).show();
    
    ArrayList<File> files = new ArrayList<File>();
    ArrayList<String> classNames = new ArrayList<String>();
    HashMap<String, OpenDefinitionsDocument> classNamesToODDs =
      new HashMap<String, OpenDefinitionsDocument>();
    
    try {
      for (File dir: classDirs) { // foreach built directory
        File[] listing = dir.listFiles();
        
        if (listing != null) {
          for (File entry : listing) {
            // for each class file in the built directory
            if (entry.isFile() && entry.getPath().endsWith(".class")) {
              try {
                JavaClass clazz = new ClassParser(entry.getCanonicalPath()).parse();
                String classname = clazz.getClassName(); // get classname
                //                System.out.println("looking for file for: " + classname);
                int indexOfDot = classname.lastIndexOf('.');
                
                /** The prefix preceding the unqualified name of the class (either empty or ends with dot). */
                String prefixString = classname.substring(0, indexOfDot + 1);  
                /** The prefix as a file system path name. */
                String prefixPath = prefixString.replace('.', File.separatorChar);
                /** The pathname (from a classpath root) for the file (including the file name) */
                String filePath = prefixPath + clazz.getSourceFileName();
                //                System.out.println("Class file is:  " + filePath);
                /** The index in filePath of the dot preceding the class extension "class". */
                int indexOfExtDot = filePath.lastIndexOf('.');
                if (indexOfExtDot == -1) break;  // RMI stub class files return source file names without extensions
                /** The (relative) path name for the class. */
                String pathName = filePath.substring(0, indexOfExtDot);
                
                for (OpenDefinitionsDocument doc: lod) {
                  try {
                    
                    /** The file for the next document in lod. */
                    File f = doc.getFile();
                    
                    /** The full path name for the class file (without extension) for entr--assuming it has same root as doc. */
                    String fullPathName = doc.getSourceRoot().getCanonicalPath() + File.separator + pathName;
                    
                    /** The full path name for file f (including extension) */
                    String pathForF = f.getCanonicalPath();
                    
                    /** The index of the last dot in the full path name for f (the file for doc). */
                    int index = pathForF.lastIndexOf('.');
                    if (index == -1) break; // the file for doc does not have an extension
                    
                    String fullPathNameFromDoc = pathForF.substring(0, index);
                    String ext = pathForF.substring(index, pathForF.length());
                    // filenameFromDoc now contains the filename minus the extension
                    
                    if (fullPathNameFromDoc.equals(fullPathName) && 
                        (ext.equals(".java") || ext.equals(".dj0") || ext.equals(".dj1")  || ext.equals(".dj2"))) {
                      if (classNamesToODDs.containsKey(classname)) break;  // class already added to classNames
                      classNames.add(classname);
                      files.add(f);
                      classNamesToODDs.put(classname, doc);
                      // new ScrollableDialog(null, "Ready to break", classname, f.toString()).show();
                      break;
                    }
                  }
                  catch(InvalidPackageException e) { /* do nothing */ }
                  catch(IOException e) { /* do nothing */ }
                  catch(IllegalStateException e) { /* do nothing; doc is untitled */ }
                }
              }
              catch(IOException e) { 
           
              /* can't read class file */ }
              catch(ClassFormatException e) { 
              /* class file is bad */ }
              // match source file to odd (if possible)
              // if match, add classname to test suite
            }
          }
        }
      }
    }
    catch(Throwable t) {
//      new ScrollableDialog(null, "UnexceptedExceptionThrown", t.toString(), "").show();
      throw new UnexpectedException(t); 
    }
    finally { 
//      new ScrollableDialog(null, "junit setup loop terminated", classNames.toString(), "").show();
    }
    
    // synchronized over _compilerModel to ensure that compilation and junit testing
    // are mutually exclusive.
    // synchronized over this so that junitStarted is 
    // called before the testing thread (JUnitTestManager) makes any notifications
    // to the notifier.  This can happen if the test fails quickly or if the test
    // class is not found.
//    new ScrollableDialog(null, "Candidate test class names are:", "", classNames.toString()).show();
    synchronized (_compilerModel) {
//        new ScrollableDialog(null, "DefaultJunitModel: holding _compileModel lock", "", "").show();
        /** Set up junit test suite on slave JVM; get TestCase classes forming that suite */
      List<String> tests;
      try { tests = _jvm.findTestClasses(classNames, files); }
      catch(IOException e) { throw new UnexpectedException(e); }
      
      if (tests == null || tests.isEmpty()) {
        nonTestCase(allTests);
        return;
      }
      
      ArrayList<OpenDefinitionsDocument> odds = new ArrayList<OpenDefinitionsDocument>();
      for (String name: tests) { odds.add(classNamesToODDs.get(name)); }
      
      try {
        /** Run the junit test suite that has already been set up on the slave JVM */
        _notifier.junitStarted(odds);
        //          new ScrollableDialog(null, "junitStarted executed in DefaultJunitModel", "", "").show();
        _jvm.runTestSuite();
        
      }
      catch(IOException e) {
        // Probably a java.rmi.UnmarshalException caused by the interruption of unit testing.
        // Swallow the exception and proceed.
        _notifier.junitEnded();  // balances junitStarted()
        synchronized (this) { _testInProgress = false;}
        throw new UnexpectedException(e);
      }
    }
  }
  
  //-------------------------------- Helpers --------------------------------//
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the JUnitErrorModel, which contains error info for the last test run. */
  public JUnitErrorModel getJUnitErrorModel() { return _junitErrorModel; }
  
  /** Resets the junit error state to have no errors. */
  public void resetJUnitErrors() {
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], _getter, false);
  }
  
  //---------------------------- Model Callbacks ----------------------------//
  
  /**
   * Called from the JUnitTestManager if its given className is not a test case.
   * @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(final boolean isTestAll) {
    // NOTE: junitStarted is called in a different thread from the testing thread,
    //       so it is possible that this is called before the other thread calls 
    //       the junitStarted.  We want the test to terminate AFTER it starts. Otherwise
    //       any thread that starts waiting for the test to end after the firing of
    //       junitStarted will never be notified. (same with all terminal events)
    //       The synchronization using _testInProgress takes care of this problem.
      _notifier.nonTestCase(isTestAll);
      synchronized (this) { _testInProgress = false;}
  }
  
  /** Called to indicate that an illegal class file was encountered
   *  @param e the ClassFileObject describing the error.
   */
  public void classFileError(ClassFileError e) { _notifier.classFileError(e); }
  
  /** Called to indicate that a suite of tests has started running.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(final int numTests) { _notifier.junitSuiteStarted(numTests); }
  
  /** Called when a particular test is started.
   *  @param testName The name of the test being started.
   */
  public void testStarted(final String testName) { _notifier.junitTestStarted(testName); }
  
  /** Called when a particular test has ended.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(final String testName, final boolean wasSuccessful, final boolean causedError) {
     _notifier.junitTestEnded(testName, wasSuccessful, causedError);
  }
  
  /** Called when a full suite of tests has finished running.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) called", "", "").show();
    _junitErrorModel = new JUnitErrorModel(errors, _getter, true);
    _notifier.junitEnded();
    synchronized(this) { _testInProgress = false; }
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) finished", "", "").show();
  }
  
  /**
   * Called when the JUnitTestManager wants to open a file that is not
   * currently open.
   * TODO: this is the only call to _model
   *       - remove it to remove GlobalModel dependence
   * @param className the name of the class for which we want to find the file
   * @return the file associated with the given class
   */
  public File getFileForClassName(String className) {
    return _model.getSourceFile(className + ".java");
  }
  
  /**
   * Returns the current classpath in use by the JUnit JVM,
   * in the form of a path-separator delimited string.
   */
  public ClasspathVector getClasspath() {  return _jvm.getClasspath(); }
  
  /** Called when the JVM used for unit tests has registered. */
  public void junitJVMReady() {
    synchronized (this) { if (! _testInProgress) return; }
    JUnitError[] errors = new JUnitError[1];
    errors[0] = new JUnitError("Previous test was interrupted", true, "");
    _junitErrorModel = new JUnitErrorModel(errors, _getter, true);
    _notifier.junitEnded();
    synchronized (this) { _testInProgress = false; }
  }
}
