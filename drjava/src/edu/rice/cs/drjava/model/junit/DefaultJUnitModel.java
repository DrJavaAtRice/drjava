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
import java.util.HashMap;
import java.util.Iterator;
import edu.rice.cs.util.FileOps;
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
  
  /**
   * Manages listeners to this model.
   */
  private final JUnitEventNotifier _notifier = new JUnitEventNotifier();
  
  /**
   * Used by CompilerErrorModel to open documents that have errors.
   */
  private final IGetDocuments _getter;
  
  /**
   * RMI interface to a secondary JVM for running tests.
   * Using a second JVM prevents tests from disrupting normal usage of DrJava.
   */
  private final MainJVM _jvm;
  
  /**
   * Compiler model, used as a lock to prevent simultaneous test and compile.
   * Typed as an Object to prevent usage as anything but a lock.
   */
  private final Object _compilerModel;
  
  /**
   * GlobalModel, used only for getSourceFile.
   */
  private final GlobalModel _model;
  
  /**
   * The error model containing all current JUnit errors.
   */
  private JUnitErrorModel _junitErrorModel;
  
  /**
   * State flag to prevent losing results of a test in progress.
   */
  private boolean _isTestInProgress = false;
  
  /**
   * The document used to display JUnit test results.
   * TODO: why is this here?
   */
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
  public void addListener(JUnitListener listener) {
    _notifier.addListener(listener);
  }
  
  /**
   * Remove a JUnitListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to JUnit events
   */
  public void removeListener(JUnitListener listener) {
    _notifier.removeListener(listener);
  }
  
  /**
   * Removes all JUnitListeners from this model.
   */
  public void removeAllListeners() {
    _notifier.removeAllListeners();
  }
  
  //-------------------------------- Triggers --------------------------------//
  
  public StyledDocument getJUnitDocument() {
    return _junitDoc;
  }
  
  /**
   * Creates a JUnit test suite over all currently open documents and runs it.
   * If the class file associated with a file is not a test case, it will be
   * ignored.  Synchronized against the compiler model to prevent testing and
   * compiling at the same time, which would create invalid results.
   */
  public void junitAll() {
    junitDocs(_getter.getDefinitionsDocuments());
  }
  
  /**
   * Creates a JUnit test suite over all currently open documents and runs it.
   * If the class file associated with a file is not a test case, it will be
   * ignored.  Synchronized against the compiler model to prevent testing and
   * compiling at the same time, which would create invalid results.
   */
  public void junitProject() {
    LinkedList<OpenDefinitionsDocument> lod = new LinkedList<OpenDefinitionsDocument>();
    
    Iterator<OpenDefinitionsDocument> it =
      _getter.getDefinitionsDocuments().iterator();
    while (it.hasNext()) {
      OpenDefinitionsDocument doc = it.next();
      if (doc.isInProjectPath() || doc.isAuxiliaryFile()) {
        lod.add(doc);
      }
    }
    junitDocs(lod);
  }
  
  /**
   * forwards the classnames and files to the test manager to test all of them
   * does not notify since we don't have ODD's to send out with the notification of junit start
   * @param a list of all the qualified class names to test
   * @param a list of their source files in the same order as qualified class names
   */
  public void junitAll(List<String> qualifiedClassnames, List<File> files){
    _notifier.junitAllStarted();
    List<String> tests = _jvm.runTestSuite(qualifiedClassnames, files, true);
    _isTestInProgress = true;
  }
  
  
  public void junitDocs(List<OpenDefinitionsDocument> lod){
    junitOpenDefDocs(lod, true);
  }
  
  
  /**
   * Runs JUnit on the current document. It formerly compiled all open documents
   * before testing but have removed that requirement in order to allow the
   * debugging of test cases. If the classes being tested are out of
   * sync, a message is displayed.
   */
  public void junit(OpenDefinitionsDocument doc)
    throws ClassNotFoundException, IOException {
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
      _isTestInProgress = false;
      _notifier.junitEnded();
      throw e;
    }
    catch (ExitingNotAllowedException enae) {
      _isTestInProgress = false;
      _notifier.junitEnded();
      throw enae;
    }
  }
  
  
  protected void junitOpenDefDocs(List<OpenDefinitionsDocument> lod, boolean allTests){
    synchronized (_compilerModel) {
      // if a test is running, don't start another one, but make sure someone's not
      // trying to notify that the previous test had finished.
      synchronized(_notifier) {
        if (_isTestInProgress) return;
      }
      
      //reset the JUnitErrorModel, fixes bug #907211 "Test Failures Not Cleared Properly".
      _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
      
      //        _getter.getDefinitionsDocuments().iterator();
      HashMap<String,OpenDefinitionsDocument> classNamesToODDs =
        new HashMap<String,OpenDefinitionsDocument>();
      ArrayList<String> classNames = new ArrayList<String>();
      ArrayList<File> files = new ArrayList<File>();
      
      // start here.
      Iterator<OpenDefinitionsDocument> it = lod.iterator();
      File builtDir = _model.getBuildDirectory();
      LinkedList<File> classDirs = new LinkedList<File>();
      LinkedList<File> sourceFiles = new LinkedList<File>();
      
      //Gets system classpaths from the main JVM so that junit tests can find every class file.
      //Given as one long String, this separates the paths into a list of strings. 3/12/05
      LinkedList<String> classpaths = new LinkedList<String>();
      String cpString = _jvm.getClasspath().toString();
      int cpLength = cpString.length();
      if (cpString.indexOf(File.pathSeparatorChar) == -1 && cpLength > 0) {
        classpaths.add(cpString);
      }
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
      
      //First adds the default document build directory to the class directories.
      while (it.hasNext()) {
        try {
          OpenDefinitionsDocument doc = it.next();
          String packageName;
          try{
            packageName = doc.getPackageName();
          }catch(InvalidPackageException e){
            packageName = "";
          }
          packageName = packageName.replace('.', File.separatorChar);
          
          // keep a record of unique built directories
          if(builtDir == null){
            builtDir = doc.getSourceRoot();
          }
          File temp = new File(builtDir.getCanonicalPath() + File.separator + packageName);
          if(!classDirs.contains(temp)){
            classDirs.add(temp);
          }
        }
        catch(IOException e){
          // don't add it to the test suite b/c the directory doesn't exist
        }catch(InvalidPackageException e){
          // don't add it, b/c it's package is bogus
        }
      }
      
      //Next adds the JVM class paths to the class directories.
      //Junit will look here if the default build directories don't have the desired classes.
      it = lod.iterator();
      while (it.hasNext()) {
        try {
          OpenDefinitionsDocument doc = it.next();
          String packageName;
          try{
            packageName = doc.getPackageName();
          }catch(InvalidPackageException e){
            packageName = "";
          }
          packageName = packageName.replace('.', File.separatorChar);
          
          //Adds unique classpaths to the list of class directories that junit tests look through. 3/12/05
          for (String classpath: classpaths) {
            File temp = new File (new File(classpath).getCanonicalPath());
            if (temp.isDirectory()) {
              temp = new File(temp.getCanonicalPath() + File.separator + packageName);
              if (!classDirs.contains(temp)) {
                classDirs.addLast(temp);
              }
            }
          }
        }
        catch(IOException e){
          // don't add it to the test suite b/c the directory doesn't exist
        }
      }
         
      for(File dir: classDirs){
        // foreach built directory
        File[] listing = dir.listFiles();

        if(listing != null) for(File entry : listing){
          // for each class file in the built directory
          if(entry.isFile() && entry.getPath().endsWith(".class")){
            try{
              JavaClass clazz = new ClassParser(entry.getCanonicalPath()).parse();
              String classname = clazz.getClassName();// get classname
              //              System.out.println("looking for file for: " + classname);
              int index_of_dot = classname.lastIndexOf('.');
              String filenameFromClassfile = classname.substring(0, index_of_dot+1);
              filenameFromClassfile = filenameFromClassfile.replace('.', File.separatorChar);
              filenameFromClassfile = filenameFromClassfile + clazz.getSourceFileName();
              // filenameFromClassfile now contains the location of a file with it's package directories attached
              // now i need to strip off the filetype (in case the dj0 file is open in drjava, the class file would point to a .java file...)
              index_of_dot = filenameFromClassfile.lastIndexOf('.');
              filenameFromClassfile = filenameFromClassfile.substring(0, index_of_dot);
              // now the filenameFromClassfile contains the package/filename without the extension.
              
              it = lod.iterator();
              File f;
              OpenDefinitionsDocument doc;
              while (it.hasNext()) {
                // for each open ODD
                doc = it.next();
                try{
                  f = doc.getFile();
                  
                  String filename = doc.getSourceRoot().getCanonicalPath() + File.separator + filenameFromClassfile;
                  int index = f.getCanonicalPath().lastIndexOf('.');
                  String filenameFromDoc = f.getCanonicalPath().substring(0, index);
                  String ext = f.getCanonicalPath().substring(index, f.getCanonicalPath().length());
                  // filenameFromDoc now contains the filename minus the extention
                  //                  System.out.println(f.getCanonicalPath() + " == " + filename);
                  if(filenameFromDoc.equals(filename) && (ext.equals(".java") || ext.equals(".dj0") || ext.equals(".dj1") || ext.equals(".dj2"))){
                    //                    System.out.println("testing: " + classname + " from " + f.getCanonicalPath());
                    //                    Method methods[] = clazz.getMethods();
                    //                    for(Method d : methods){
                    //                      System.out.println(" method: " + d);
                    //                    }
                    classNames.add(classname);
                    files.add(f);
                    classNamesToODDs.put(classname, doc);
                    break;
                  }
                }catch(InvalidPackageException e){
                }catch(IOException e){
                }catch(IllegalStateException e){
                  // doc is untitled
                }
              }
            }catch(IOException e){
              // can't read class file
            }catch(ClassFormatException e){
              // class file is bads
            }
            // match source file to odd (if possible)
            // if match, add clasname to test suite
          }
          
        }
      }

      _isTestInProgress = true;
      // synchronized over _notifier so that junitStarted is ensured to be 
      // called before the testing thread (JUnitTestManager) makes any notifications
      // to the notifier.  This can happen if the test fails quickly or if the test
      // class is not found.
      synchronized(_notifier) {
        List<String> tests = _jvm.runTestSuite(classNames, files, allTests);
        ArrayList<OpenDefinitionsDocument> odds =
          new ArrayList<OpenDefinitionsDocument>();
        Iterator<String> it2 = tests.iterator();
        while (it2.hasNext()) {
          odds.add(classNamesToODDs.get(it2.next()));
        }
        _notifier.junitStarted(odds);
      }
    }
  }
  
  
  //-------------------------------- Helpers --------------------------------//
  
  //----------------------------- Error Results -----------------------------//
  
  /**
   * Gets the JUnitErrorModel, which contains error info for the last test run.
   */
  public JUnitErrorModel getJUnitErrorModel() {
    return _junitErrorModel;
  }
  
  /**
   * Resets the junit error state to have no errors.
   */
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
    //       The synchronization over _notifier takes care of this problem.
    synchronized(_notifier) { 
      _notifier.nonTestCase(isTestAll);
      _isTestInProgress = false;
      _notifier.junitEnded();
    } 
  }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * TODO: Why is this sync'ed?
   * Answer?: This might be to make sure that a test doesn't start while the file is
   *          being compiled. Only thing is that junitOpenDefDocs is synchronized to
   *          _compilerModel as well. This might not be needed.
   * 
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(final int numTests) {
    synchronized(_compilerModel) {
      _notifier.junitSuiteStarted(numTests);
    }
  }
  
  /**
   * Called when a particular test is started.
   * TODO: Why is this sync'ed?
   * @param testName The name of the test being started.
   */
  public void testStarted(final String testName) {
    synchronized(_compilerModel) { 
      _notifier.junitTestStarted(testName);
    }
  }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(final String testName, final boolean wasSuccessful,
                        final boolean causedError)
  {
    synchronized(_notifier) { // so that it's not called until junitStarted is fired
      _notifier.junitTestEnded(testName, wasSuccessful, causedError);
    }
  }
  
  /**
   * Called when a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    if (_isTestInProgress) {
      _junitErrorModel = new JUnitErrorModel(errors, _getter, true);
      synchronized(_notifier) { // so that it's not called until junitStarted is fired
        _isTestInProgress = false;
        _notifier.junitEnded();
      }
    }
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
  public ClasspathVector getClasspath() {
    return _jvm.getClasspath();
  }
  
  /**
   * Called when the JVM used for unit tests has registered.
   */
  public void junitJVMReady() {
    if (_isTestInProgress) {
      JUnitError[] errors = new JUnitError[1];
      errors[0] = new JUnitError("Previous test was interrupted", true, "");
      _junitErrorModel = new JUnitErrorModel(errors, _getter, true);
      
      synchronized(_notifier) { // make sure junitStarted isn't being called
        _isTestInProgress = false;
        _notifier.junitEnded();
      }
    }
  }
}
