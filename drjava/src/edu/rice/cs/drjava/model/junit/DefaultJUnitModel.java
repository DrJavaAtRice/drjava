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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.rmi.RemoteException;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import javax.swing.JOptionPane;

import edu.rice.cs.drjava.config.BooleanOption;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.DummyCompilerListener;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Box;
import edu.rice.cs.plt.lambda.SimpleBox;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.text.SwingDocument;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.Log;

import org.objectweb.asm.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.drjava.config.OptionConstants.*;

//import edu.rice.cs.drjava.model.compiler.LanguageLevelStackTraceMapper;

/** Manages unit testing via JUnit.
  * @version $Id: DefaultJUnitModel.java 5722 2012-09-29 19:37:22Z wdforson $
  */
public class DefaultJUnitModel implements JUnitModel, JUnitModelCallback {
  
  /** log for use in debugging */
  private static Log _log = new Log("GlobalModel.txt", false);
  
  /** Manages listeners to this model. */
  private final JUnitEventNotifier _notifier = new JUnitEventNotifier();
  
  /** RMI interface to a secondary JVM for running tests.  Using a second JVM prevents interactions and tests from 
    * corrupting the state of DrJava.
    */
  private final MainJVM _jvm;
  
  /** The compiler model.  It contains a lock used to prevent simultaneous test and compile.  It also tracks the number
    * errors in the last compilation, which is required information if junit forces compilation.  
    */
  private final CompilerModel _compilerModel;
  
  /** The global model to which the JUnitModel belongs */
  private final GlobalModel _model;
  
  /** The error model containing all current JUnit errors. */
  private volatile JUnitErrorModel _junitErrorModel;
  
  /** State flag to prevent starting new tests on top of old ones and to prevent resetting interactions after compilation
    * is forced by unit testing. This field is NOT REDUNDANT, it is used in junitJVMReady.
    */
  private volatile boolean _testInProgress = false;
  
  /** State flag to record if test classes in projects must end in "Test" */
  private boolean _forceTestSuffix = false;
  
  /** The document used to display JUnit test results.  Used only for testing. */
  private final SwingDocument _junitDoc = new SwingDocument();
  
  /** Main constructor.
    * @param jvm RMI interface to a secondary JVM for running tests
    * @param compilerModel the CompilerModel, used only as a lock to prevent simultaneous test and compile
    * @param model used only for getSourceFile
    */
  public DefaultJUnitModel(MainJVM jvm, CompilerModel compilerModel, GlobalModel model) {
    _jvm = jvm;
    _compilerModel = compilerModel;
    _model = model;
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], _model, false);
    BooleanOption suffixOption = OptionConstants.FORCE_TEST_SUFFIX;
    _forceTestSuffix = edu.rice.cs.drjava.DrJava.getConfig().getSetting(suffixOption).booleanValue();
  }
  
  //-------------------------- Field Setters --------------------------------//
  
  public void setForceTestSuffix(boolean b) { _forceTestSuffix = b; }
  
  //------------------------ Simple Predicates ------------------------------//
  
  public boolean isTestInProgress() { return _testInProgress;  }
  
  //------------------------Listener Management -----------------------------//
  
  /** Add a JUnitListener to the model.
    * @param listener a listener that reacts to JUnit events
    */
  public void addListener(JUnitListener listener) { _notifier.addListener(listener); }
  
  /** Remove a JUnitListener from the model.  If the listener is not currently listening to this model, this method 
    * has no effect.
    * @param listener a listener that reacts to JUnit events
    */
  public void removeListener(JUnitListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all JUnitListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  
  //-------------------------------- Triggers --------------------------------//
  
  /** Used only for testing. */
  public SwingDocument getJUnitDocument() { return _junitDoc; }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If the class file 
    * associated with a file is not a test case, it is ignored.  
    */
  public void junitAll() { junitDocs(_model.getOpenDefinitionsDocuments()); }
  
  /** Creates a JUnit test suite over all currently open documents and runs it.  If a class file associated with a 
    * source file is not a test case, it will be ignored.  Synchronized against the compiler model to prevent 
    * testing and compiling at the same time, which would create invalid results.
    */
  public void junitProject() {
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    
    for (OpenDefinitionsDocument doc : _model.getOpenDefinitionsDocuments()) { 
      if (doc.inProjectPath()) lod.add(doc);
    }
    junitOpenDefDocs(lod, true);
  }
  
//  /** Forwards the classnames and files to the test manager to test all of them; does not notify 
//    * since we don't have ODD's to send out with the notification of junit start.
//    * @param qualifiedClassnames a list of all the qualified class names to test.
//    * @param files a list of their source files in the same order as qualified class names.
//    */
//  public void junitClasses(List<String> qualifiedClassnames, List<File> files) {
//    Utilities.showDebug("junitClasses(" + qualifiedClassnames + ", " + files);
//    synchronized(_compilerModel.getCompilerLock()) {
//      
//      // Check _testInProgress 
//      if (_testInProgress) return;
//      
//      List<String> testClasses;
//      try { testClasses = _jvm.findTestClasses(qualifiedClassnames, files); }
//      catch(IOException e) { throw new UnexpectedException(e); }
//      
////      _log.log("Found test classes: " + testClasses);
//      
//      if (testClasses.isEmpty()) {
//        nonTestCase(true);
//        return;
//      }
//      _notifier.junitClassesStarted();
//      _testInProgress = true;
//      try { _jvm.runTestSuite(); } 
//      catch(Exception e) {
////        _log.log("Threw exception " + e);
//        _notifier.junitEnded();
//        _testInProgress = false;
//        throw new UnexpectedException(e); 
//      }
//    }
//  }
  
  public void junitDocs(List<OpenDefinitionsDocument> lod) { junitOpenDefDocs(lod, true); }
  
  /** Runs JUnit on the current document.  Forces the user to compile all open documents before proceeding. */
  public void junit(OpenDefinitionsDocument doc) throws ClassNotFoundException, IOException {
    debug.logStart("junit(doc)");
//    new ScrollableDialog(null, "junit(" + doc + ") called in DefaultJunitModel", "", "").show();
    File testFile;
    try { 
      testFile = doc.getFile(); 
      if (testFile == null) {  // document is untitiled: abort unit testing and return
        nonTestCase(false, false);
        debug.logEnd("junit(doc): no corresponding file");
        return;
      }
    } 
    catch(FileMovedException fme) { /* do nothing */ }
    
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    lod.add(doc);
    junitOpenDefDocs(lod, false);
    debug.logEnd("junit(doc)");
  }
  
  /** Ensures that all documents have been compiled since their last modification and then delegates the actual testing
    * to _rawJUnitOpenTestDocs. */
  private void junitOpenDefDocs(final List<OpenDefinitionsDocument> lod, final boolean allTests) {
    // If a test is running, don't start another one.

    _log.log("junitOpenDefDocs(" + lod + ", " + allTests + ", " + _testInProgress + ")");
    
    // Check_testInProgress flag
    if (_testInProgress) return;
    
    // Reset the JUnitErrorModel, fixes bug #907211 "Test Failures Not Cleared Properly".
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);

    _log.log("Retrieved JUnit error model.  outOfSync = " +  _model.getOutOfSyncDocuments(lod));
    final List<OpenDefinitionsDocument> outOfSync = _model.getOutOfSyncDocuments(lod);
    _log.log("outOfSync = " + outOfSync);    
    if ((outOfSync.size() > 0) || _model.hasModifiedDocuments(lod)) {
      /* hasOutOfSyncDocuments(lod) can return false when some documents have not been successfully compiled; the 
       * granularity of time-stamping and the presence of multiple classes in a file (some of which compile 
       * successfully) can produce false reports.  */
      _log.log("Out of sync documents exist");
      CompilerListener testAfterCompile = new DummyCompilerListener() {
        @Override public void compileAborted(Exception e) {
          // gets called if there are modified files and the user chooses NOT to save the files
          // see bug report 2582488: Hangs If Testing Modified File, But Choose "No" for Saving
          final CompilerListener listenerThis = this;
          try {
            nonTestCase(allTests, false);
          }
          finally {  // always remove this listener after its first execution
            EventQueue.invokeLater(new Runnable() { 
              public void run() { _compilerModel.removeListener(listenerThis); }
            });
          }
        }
        
        @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
          final CompilerListener listenerThis = this;
          try {
            _log.log("compileEnded called.  outOfSync = " + _model.hasOutOfSyncDocuments(lod));
            if (_model.hasOutOfSyncDocuments(lod) || _model.getNumCompilerErrors() > 0) {
              nonTestCase(allTests, _model.getNumCompilerErrors() > 0);
              return;
            }
            EventQueue.invokeLater(new Runnable() {  // defer running this code; would prefer to waitForInterpreter
              public void run() { _rawJUnitOpenDefDocs(lod, allTests); }
            });
          }
          finally {  // always remove this listener after its first execution
            EventQueue.invokeLater(new Runnable() { 
              public void run() { _compilerModel.removeListener(listenerThis); }
            });
          }
        }
      };
      
        _log.log("Notifying JUnitModelListener");
      _testInProgress = true;
      _notifyCompileBeforeJUnit(testAfterCompile, outOfSync);
      _testInProgress = false;
    }
    
    else _rawJUnitOpenDefDocs(lod, allTests);
  }
  
  /** Runs all TestCases in the document list lod; assumes all documents have been compiled. It finds the TestCase 
    * classes by searching the build directories for the documents.  Note: caller must respond to thrown exceptions 
    * by invoking _junitUnitInterrupted (to run hourglassOff() and reset the unit testing UI).
    */
  private void _rawJUnitOpenDefDocs(List<OpenDefinitionsDocument> lod, final boolean allTests) {
    File buildDir = _model.getBuildDirectory();
//    Utilities.show("Running JUnit tests. Build directory is " + buildDir);
    
    /** Open java source files */
    HashSet<String> openDocFiles = new HashSet<String>();
    
    /** A map whose keys are directories containing class files corresponding to open java source files.
      * Their values are the corresponding source roots. 
      */
    HashMap<File, File> classDirsAndRoots = new HashMap<File, File>();
    
    // Initialize openDocFiles and classDirsAndRoots
    // All packageNames should be valid because all source files are compiled
    
    for (OpenDefinitionsDocument doc: lod) /* for all nonEmpty documents in lod */ {
      if (doc.isSourceFile())  { // excludes Untitled documents and open non-source files
        try {
          _log.log("Processing " + doc);
          File sourceRoot = doc.getSourceRoot(); // may throw an InvalidPackageException
          
          // doc has valid package name; add it to list of open java source doc files
          openDocFiles.add(doc.getCanonicalPath());
          
          String packagePath = doc.getPackageName().replace('.', File.separatorChar);
          
          // Add (canonical path name for) build directory for doc to classDirs
          
          File buildRoot = (buildDir == FileOps.NULL_FILE) ? sourceRoot: buildDir;
          
          File classFileDir = new File(IOUtil.attemptCanonicalFile(buildRoot), packagePath);
          
          File sourceDir = 
            (buildDir == FileOps.NULL_FILE) ? classFileDir : 
                                              new File(IOUtil.attemptCanonicalFile(sourceRoot), packagePath);
          
          if (! classDirsAndRoots.containsKey(classFileDir)) {
            classDirsAndRoots.put(classFileDir, sourceDir);
          _log.log("Adding " + classFileDir + " with source root " + sourceRoot + " to list of class directories");
          }
        }
        catch (InvalidPackageException e) { /* Skip the file, since it doesn't have a valid package */ }
      }
    }

    _log.log("classDirs = " + classDirsAndRoots.keySet());
    
    /** set of dirs potentially containing test classes */
    Set<File> classDirs = classDirsAndRoots.keySet();
    
    _log.log("openDocFiles = " + openDocFiles);
    
    /* Names of test classes. */
    final ArrayList<String> classNames = new ArrayList<String>();  // TODO: convert classNames/files to HashSet<Pair<String, File>>
    
    /* Source files corresonding to potential test class files */
    final ArrayList<File> files = new ArrayList<File>();           
    
    /* Flag indicating if project is open */
    boolean isProject = _model.isProjectActive();
    
    try {
      for (File dir: classDirs) { // foreach class file directory
        _log.log("Examining directory " + dir);
        
        File[] listing = dir.listFiles();
        
        _log.log("Directory contains the files: " + Arrays.asList(listing));
        
        if (listing != null) { // listFiles may return null if there's an IO error
          for (File entry : listing) { /* for each class file in the build directory */        
            
            _log.log("Examining file " + entry);
            
            /* ignore non-class files */
            String name = entry.getName();
            if (! name.endsWith(".class")) continue;
            
            /* Ignore class names that do not end in "Test" if FORCE_TEST_SUFFIX option is set */
            if (_forceTestSuffix) {
              String noExtName = name.substring(0, name.length() - 6);  // remove ".class" from name
              int indexOfLastDot = noExtName.lastIndexOf('.');
              String simpleClassName = noExtName.substring(indexOfLastDot + 1);
            _log.log("Simple class name is " + simpleClassName);  
              if (/*isProject &&*/ ! simpleClassName.endsWith("Test")) continue;
            }
            
            /* ignore entries that do not correspond to files?  Can this happen? */
            if (! entry.isFile()) continue;
            
            _log.log("Found test class: " + name);
            
            // Add this class and the corrresponding source file to classNames and files, respectively.
            // Finding the source file is non-trivial because it may be a language-levels file (NOT, disabled in DrScala)
            
            try {
              final Box<String> className = new SimpleBox<String>();
              final Box<String> sourceName = new SimpleBox<String>();
              new ClassReader(IOUtil.toByteArray(entry)).accept(new ClassVisitor(Opcodes.ASM4) {
                public void visit(int version, int access, String name, String sig, String sup, String[] inters) {
                  className.set(name.replace('/', '.'));
                }
                public void visitSource(String source, String debug) {
                  sourceName.set(source);
                }
                public void visitOuterClass(String owner, String name, String desc) { }
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) { return null; }
                public void visitAttribute(Attribute attr) { }
                public void visitInnerClass(String name, String out, String in, int access) { }
                public FieldVisitor visitField(int a, String n, String d, String s, Object v) { return null; }
                public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) { return null; }
                public void visitEnd() { }
              }, 0);
              
              File rootDir = classDirsAndRoots.get(dir);
              
              /** The canonical pathname for the file (including the file name) */
              String javaSourceFileName = getCanonicalPath(rootDir) + File.separator + sourceName.value();
              _log.log("Full java source fileName = " + javaSourceFileName);
              
              /* The index in fileName of the dot preceding the extension ".java" or ".scala" */
              int indexOfExtDot = javaSourceFileName.lastIndexOf('.');
              _log.log("indexOfExtDot = " + indexOfExtDot);
              if (indexOfExtDot == -1) continue;  // RMI stub class files return source file names without extensions
              
              // Language level processing is disabled in DrScala
              /* Determine if this java source file was generated from a .java or a .scala file. */
              String strippedName = javaSourceFileName.substring(0, indexOfExtDot);
              _log.log("Stripped name = " + strippedName);
              
              String sourceFileName;
              
              if (openDocFiles.contains(javaSourceFileName)) sourceFileName = javaSourceFileName;
              else if (openDocFiles.contains(strippedName + OptionConstants.SCALA_FILE_EXTENSION))
                sourceFileName = strippedName + OptionConstants.SCALA_FILE_EXTENSION;
              else if (openDocFiles.contains(strippedName + OptionConstants.JAVA_FILE_EXTENSION))
                sourceFileName = strippedName + OptionConstants.JAVA_FILE_EXTENSION;

              else continue; // no matching source file is open
              
              _log.log("File found in openDocFiles = "  + openDocFiles.contains(sourceFileName));
              File sourceFile = new File(sourceFileName);
              classNames.add(className.value());
              files.add(sourceFile);
              _log.log("Class " + className.value() + " added to classNames.   File " + sourceFileName + " added to files.");
            }
            catch(IOException e) { /* ignore it; can't read class file */ }
          }
        }
      }
    }
    catch(Exception e) {
//      new ScrollableDialog(null, "UnexceptedExceptionThrown", e.toString(), "").show();
      throw new UnexpectedException(e); // triggers _junitInterrupted which runs hourglassOff
    }
    
    _log.log("files = " + files);
    /** Run the junit test suite that has already been set up on the slave JVM */
    _testInProgress = true;
     _log.log("Spawning test thread");
    new Thread(new Runnable() { // this thread is not joined, but the wait/notify scheme guarantees that it ends
      public void run() { 
        // TODO: should we disable compile commands while testing?  Should we use protected flag instead of lock?
        // Utilities.show("Preparing to synchronize");
        
        // The call to findTestClasses had to be moved out of the event thread (bug 2722310)
        // The event thread is still blocked in findTestClasses when JUnit needs to
        // have a class prepared. This invokes EventHandlerThread._handleClassPrepareEvent, which puts a call to
        // _debugger.getPendingRequestManager().classPrepared(e); (which presumably
        // deals with preparing the class) on the event thread using invokeLater.
        // This, however, doesn't get executed because the event thread is still blocking --> deadlock.
        
        synchronized(_compilerModel.getCompilerLock()) {
          // synchronized over _compilerModel to ensure that compilation and junit testing are mutually exclusive.
          /** Set up junit test suite on slave JVM; get TestCase classes forming that suite */
          List<String> tests = _jvm.findTestClasses(classNames, files).unwrap(null);
          _log.log("tests = " + tests);
          if (tests == null || tests.isEmpty()) {
            nonTestCase(allTests, false);
            return;
          }
        }
        
        try {
          // Utilities.show("Starting JUnit");
          
          _notifyJUnitStarted(); 
          boolean testsPresent = _jvm.runTestSuite();  // The false return value could be changed to an exception.
          if (! testsPresent) throw new RemoteException("No unit test classes were passed to the slave JVM");
        }
        catch(RemoteException e) { // Unit testing aborted; cleanup; hourglassOff already called in junitStarted
          _notifyJUnitEnded();  // balances junitStarted()
          _testInProgress = false;
        }
      }
    }).start();
  }
  
//-------------------------------- Helpers --------------------------------//
  
  /** Helper method to notify JUnitModel listeners that JUnit test suite execution has started. */
  private void _notifyJUnitStarted() { 
    // Use EventQueue.invokeLater so that notification is deferred when running in the event thread.
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.junitStarted(); } });
  }
  
  /** Helper method to notify JUnitModel listeners that JUnit test suite execution has just ended. */
  private void _notifyJUnitEnded() { 
    // Use EventQueue.invokeLater so that notification is deferred when running in the event thread.
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.junitEnded(); } });
  }
  
  /** Helper method to notify JUnitModel listeners that all open files must be compiled before JUnit is run. */
  private void _notifyCompileBeforeJUnit(final CompilerListener testAfterCompile, 
                                         final List<OpenDefinitionsDocument> outOfSync) { 
    Utilities.invokeLater(new Runnable() { 
      public void run() { _notifier.compileBeforeJUnit(testAfterCompile, outOfSync); } 
    });
  }
  
  /** Helper method to notify JUnitModel listeners that JUnit aborted before any tests could be run. */
  private void _notifyNonTestCase(final boolean testAll, final boolean didCompileFail) { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.nonTestCase(testAll, didCompileFail); } });
  }
  
  private String getCanonicalPath(File f) throws IOException {
    if (f == null) return "";
    return f.getCanonicalPath();
  }
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the JUnitErrorModel, which contains error info for the last test run. */
  public JUnitErrorModel getJUnitErrorModel() { return _junitErrorModel; }
  
  /** Resets the junit error state to have no errors. */
  public void resetJUnitErrors() { _junitErrorModel = new JUnitErrorModel(new JUnitError[0], _model, false); }
  
  //---------------------------- Model Callbacks ----------------------------//
  
  /** Called from the JUnitTestManager if its given className is not a test case.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(final boolean isTestAll, boolean didCompileFail) {
    // NOTE: junitStarted is called in a different thread from the testing thread.  The _testInProgress flag
    //       is used to prevent a new test from being started and overrunning the existing one.
//      Utilities.show("DefaultJUnitModel.nonTestCase(" + isTestAll + ") called");
    _notifyNonTestCase(isTestAll, didCompileFail);
    _testInProgress = false;
  }
  
  /** Called to indicate that an illegal class file was encountered
    * @param e the ClassFileObject describing the error.
    */
  public void classFileError(final ClassFileError e) { 
    Utilities.invokeLater(new Runnable() { public void run() {_notifier.classFileError(e); } });
  }
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void testSuiteStarted(final int numTests) { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.junitSuiteStarted(numTests); } });
  }
  
  /** Called when a particular test is started.
    * @param testName The name of the test being started.
    */
  public void testStarted(final String testName) { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.junitTestStarted(testName); } });
  }
  
  /** Called when a particular test has ended.
    * @param testName The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error or simply failed.
    */
  public void testEnded(final String testName, final boolean wasSuccessful, final boolean causedError) {
    EventQueue.invokeLater(new Runnable() { 
      public void run() { _notifier.junitTestEnded(testName, wasSuccessful, causedError); }
    });
  }
  
  /** Called when a full suite of tests has finished running.  Does not necessarily run in event thread.
    * @param errors The array of errors from all failed tests in the suite.
    */
  public void testSuiteEnded(final JUnitError[] errors) {
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) called", "", "").show();
   
    Utilities.invokeLater(new Runnable() { public void run() {
      // disable languge level processing
//      List<File> files = new ArrayList<File>();
//      for (OpenDefinitionsDocument odd: _model.getOpenDefinitionsDocuments()) { files.add(odd.getRawFile()); }
//    Utilities.show("errors.length = " + errors.length + " files = " + files);
//      
//      for(JUnitError e: errors) {
//        try {
//          e.setStackTrace(_compilerModel.getLLSTM().replaceStackTrace(e.stackTrace(),files));
//        } catch(Exception ex) { DrJavaErrorHandler.record(ex); }
//        File f = e.file();
//        if ((f != null) && (DrJavaFileUtils.isLLFile(f))) {
//          String dn = DrJavaFileUtils.getJavaForLLFile(f.getName());
//          StackTraceElement ste = new StackTraceElement(e.className(), "", dn, e.lineNumber());
//          ste = _compilerModel.getLLSTM().replaceStackTraceElement(ste, f);
//          e.setLineNumber(ste.getLineNumber());
//        }
//      }
      _junitErrorModel = new JUnitErrorModel(errors, _model, true);
      _notifyJUnitEnded();
      _testInProgress = false;
//    new ScrollableDialog(null, "DefaultJUnitModel.testSuiteEnded(...) finished", "", "").show();
    }});
  }

  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
    * @param className the name of the class for which we want to find the file
    * @return the file associated with the given class
    */
  public File getFileForClassName(String className) {
    // TODO: What about language level file extensions? What about Habanero Java extension?
    return _model.getSourceFile(className + OptionConstants.JAVA_FILE_EXTENSION);
  }
  
  /** Returns the current classpath in use by the JUnit JVM. */
  public Iterable<File> getClassPath() {  return _jvm.getClassPath().unwrap(IterUtil.<File>empty()); }
  
  /** Called when the JVM used for unit tests has registered.  Does not necessarily run in even thread. */
  public void junitJVMReady() {
    Utilities.invokeLater(new Runnable() { public void run() { 
      if (! _testInProgress) return;
      
      JUnitError[] errors = new JUnitError[1];
      errors[0] = new JUnitError("Previous test suite was interrupted", true, "");
      _junitErrorModel = new JUnitErrorModel(errors, _model, true);
      _notifyJUnitEnded();
      _testInProgress = false;
    }});
  }
}
