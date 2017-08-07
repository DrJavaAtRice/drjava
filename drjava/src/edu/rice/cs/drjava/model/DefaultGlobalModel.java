/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;


import java.awt.EventQueue;

import java.io.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.config.BooleanOption;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.compiler.DummyCompilerListener;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.repl.InterpreterBusyException;
import edu.rice.cs.drjava.model.javadoc.ScaladocModel;
import edu.rice.cs.drjava.model.javadoc.NoScaladocAvailable;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.DummyInteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.DefaultCompilerModel;
import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.model.junit.DefaultJUnitModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.tuple.Pair;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.NullFile;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Handles the bulk of DrScala's program logic. The UI components interface with the GlobalModel through its public
  * methods, and the GlobalModel responds via the GlobalModelListener interface. This removes the dependency on the 
  * UI for the logical flow of the program's features.  With the current implementation, we can finally test the compile
  * command of DrJava, along with many other things. <p>
  * @version $Id: DefaultGlobalModel.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class DefaultGlobalModel extends AbstractGlobalModel {
  
  /* FIELDS */
  public static int BUSY_WAIT_DELAY = 500;  // wait 500 milliseconds between attempts to get the starting interpreter
  
  /* static Log _log inherited from AbstractGlobalModel */
  
  /* Interpreter fields */
  
  /** The document used in the Interactions model. */
  protected final InteractionsDJDocument _interactionsDocument;
  
  /** RMI interface to the Interactions JVM. 
    * TODO: Should all such communication be routed through the InteractionsModel?*/
  final MainJVM _mainJVM; 
  
  private final Thread _interpreterJVMStarter; // thread that invokes _jvm.startInterpreterJVM()
  
  /** Interface between the InteractionsDocument and the JavaInterpreter, which runs in a separate JVM. */
  protected final DefaultInteractionsModel _interactionsModel;
  
  /** Null interactions listener (except for waitUntilDone) attached to interactions model */
  protected DummyInteractionsListener _interactionsListener = new DummyInteractionsListener(); 
  
  private CompilerListener _clearInteractionsListener = new DummyCompilerListener() {
    
    public void compileStarted() {
      assert EventQueue.isDispatchThread();
      final InteractionsDocument iDoc = _interactionsModel.getDocument();
      iDoc.insertBeforeLastPrompt("Resetting Interaction ...", InteractionsDocument.ERROR_STYLE);
      _log.log("In _clearInteractionsListener.compileEnded, calling resetInteractions in _clearInteractionsListener");
      // reset interactions using updated class path if necessary
      final File workDir = _interactionsModel.getWorkingDirectory();
      Utilities.invokeLater(new Runnable() { public void run() { resetInteractions(); } });
    }
    
    public void compileEnded(File workDir, List<? extends File> excludedFiles) {
      // Only clear interactions if there were no errors and unit testing is not in progress
      _log.log("In _clearInteractionsListener.compileEnded, waiting for interactions pane to reset");
      _interactionsListener.waitResetDone();  // this call also resets the _resetDone CompletionMonitor
    }
  };

  // ---- Compiler Fields ----
  
  /** CompilerModel manages all compiler functionality. */
  private final CompilerModel _compilerModel;
  
//  /** Whether or not to reset the interactions JVM after compiling.  Should only be false in test cases. */
//  private volatile boolean _resetAfterCompile = true;
  
  /** Number of errors in last compilation.  compilerModel._numErrors is trashed when the compile model is reset. */
  private volatile int _numCompilerErrors = 0;
  
  /* JUnit Fields */
  
  /** JUnitModel manages all JUnit functionality. */
  private final DefaultJUnitModel _junitModel;
  
  /* Scaladoc Fields */
  
  /** Manages all Scaladoc functionality. */
  protected volatile ScaladocModel _scaladocModel;
  
  /* Constructors */
  /** Constructs a new GlobalModel. Creates a new MainJVM and starts its Interpreter JVM. */
  public DefaultGlobalModel() {
    _log.log("Constructing DefaultGlobalModel");
    
    Iterable<? extends JDKToolsLibrary> tools = findLibraries();
    List<CompilerInterface> compilers = new LinkedList<CompilerInterface>();

    _scaladocModel = null;
    for (JDKToolsLibrary t : tools) {
      // check for support of JAVA_7; change to JAVA_8 for Scala 2.12*/
      if (t.compiler().isAvailable() && t.version().supports(JavaVersion.JAVA_7)) {
      _log.log("For compiler " + t.compiler() + ", isAvailable() = " + t.compiler().isAvailable());
        compilers.add(t.compiler());
      }

      if (_scaladocModel == null && t.scaladoc().isAvailable()) { _scaladocModel = t.scaladoc(); }
//      else if (_scaladocModel == null) Utilities.show("No compiler found for JDKToolsLibrary" + t);
    }
    if (_scaladocModel == null) { _scaladocModel = new NoScaladocAvailable(this); }
//    Utilities.show("_scaladocModel = " + _scaladocModel);
    
    File workDir = Utilities.TEST_MODE ? new File(System.getProperty("user.home")) : getWorkingDirectory();
    _mainJVM = new MainJVM(workDir, this);
//    AbstractMasterJVM._log.log(this + " has created a new MainJVM");
    _compilerModel = new DefaultCompilerModel(this, compilers);     
    _junitModel = new DefaultJUnitModel(_mainJVM, _compilerModel, this);
    _interactionsDocument = new InteractionsDJDocument(_notifier);
    
    _interactionsModel = new DefaultInteractionsModel(this, _mainJVM, _interactionsDocument, workDir);
    _interactionsModel.addListener(_interactionsListener);
    _mainJVM.setInteractionsModel(_interactionsModel);
    _mainJVM.setJUnitModel(_junitModel);
    
    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _scaladocModel.addListener(_notifier);
    
    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);
    
    _log.log("In DefaultGlobalModel constructor, listeners added");
    
    _interpreterJVMStarter = new Thread("Start interpreter JVM") {
      public void run() { _mainJVM.startInterpreterJVM(); }
    };
    _log.log("In DefaultGlobalModel constructor, starting InterpreterJVM");
    _interpreterJVMStarter.start();
    _log.log("In DefaultGlobalModel, InterpreterJVM started");
// Lightweight parsing has been disabled until we have something that is beneficial and works better in the background.    
//    _parsingControl = new DefaultLightWeightParsingControl(this);
    _log.log("DefaultGlobalModel construction complete");
  }

  // makes the version coarser, if desired: if DISPLAY_ALL_COMPILER_VERSIONS is disabled, then only
  // the major version and the vendor will be considered
  private static JavaVersion.FullVersion coarsenVersion(JavaVersion.FullVersion tVersion) {
    BooleanOption displayAllOption = edu.rice.cs.drjava.config.OptionConstants.DISPLAY_ALL_COMPILER_VERSIONS;
    if (!DrScala.getConfig().getSetting(displayAllOption).booleanValue()) {
      tVersion = tVersion.onlyMajorVersionAndVendor();
    }
    return tVersion;
  }
  
  // A pair of version and descriptor.
  // If the descriptor is something different than JDKDescriptor.NONE, then this pair will always
  // return false for equals(), except if it is compared to the identical pair.
  private static class LibraryKey implements Comparable<LibraryKey> {    
    public static final int PRIORITY_BUILTIN = 4;  // Currenty the Scala 2.12.0 compiler
    public static final int PRIORITY_SEARCH = 1;
    public static final int PRIORITY_RUNTIME = 2;
    public static final int PRIORITY_SCALA = 3;
    public static final int PRIORITY_CONFIG = 5;

    protected final int _priority; // as above
    protected final JavaVersion.FullVersion _first;
    protected final JDKDescriptor _second;
    
    public LibraryKey(int priority, JavaVersion.FullVersion first, JDKDescriptor second) {
      _priority = priority;
      _first = first;
      _second = second;
    }    

    public boolean equals(Object o) {
      // identity --> true
      if (this == o) { return true; }
      // different class --> false
      else if (o == null || !getClass().equals(o.getClass())) { return false; }
      else {
        LibraryKey cast = (LibraryKey) o;
        // only true if both versions are equal and both descriptors are NONE
        return 
          (_priority == cast._priority) &&
          (_first == null ? cast._first == null : _first.equals(cast._first)) &&
          (_second == null ? cast._second == null :
             ((_second==JDKDescriptor.NONE) && (cast._second==JDKDescriptor.NONE)));
      }
    }
    
    public String toString() {
      return "priority " + _priority + ", version " + _first.versionString() + " " + _first.maintenance() + " " + 
        _first.update() + " " + _first.vendor() + " " + _first.location() + ", descriptor " + _second.getName();
    }
    
    public int hashCode() {
      return  _priority ^ (_first == null ? 0 : _first.hashCode()) ^  (_second == null ? 0 : _second.hashCode() << 1) ^ 
        getClass().hashCode();
    }
    
    public int compareTo(LibraryKey o) {
      int result = _priority - o._priority;
      if (result == 0) {
        result = _first.compareTo(o._first);
      }
      if (result == 0) {
        if (_second == JDKDescriptor.NONE) { // identity
          if (o._second == JDKDescriptor.NONE) { // identity
            result = 0;
          }
          else {
            // this is NONE, other is something else; prefer NONE
            result = 1;
          }
        }
        else if (o._second == JDKDescriptor.NONE) { // identity
          // other is NONE, this is something else; prefer NONE
          result = -1;
        }
        else {
          result = _second.toString().compareTo(o._second.toString());
        }
      }
      return result;
    }
  }
  
  // return a new version-descriptor pair for a library
  private LibraryKey getLibraryKey(int priority, JDKToolsLibrary lib) {
    return new LibraryKey(priority, coarsenVersion(lib.version()), lib.jdkDescriptor());
  }
  
  private Iterable<JDKToolsLibrary> findLibraries() {
    // Order to return: config setting, runtime (if different version), from search (if different versions)
    
    // We give priority to libraries that support Scala.
    
    // map is sorted first by LibraryKey.priority and second by version, lowest-to-highest
    Map<LibraryKey, JDKToolsLibrary> results = new TreeMap<LibraryKey, JDKToolsLibrary>();
    
    JarJDKToolsLibrary._log.log("DefaultGlobalModel.findLibraries() called; " + JavaVersion.CURRENT + " is running");
    File configTools = DrScala.getConfig().getSetting(JAVAC_LOCATION);
    if (configTools != null && configTools != FileOps.NULL_FILE) {
      JDKToolsLibrary fromConfig = JarJDKToolsLibrary.makeFromFile(configTools, this, JDKDescriptor.NONE);
      if (fromConfig.isValid()) { 
        JarJDKToolsLibrary._log.log("In DefaultGlobalModel.findLibraries, adding: " + fromConfig  + " from config");
        results.put(getLibraryKey(LibraryKey.PRIORITY_CONFIG, fromConfig), fromConfig);
      }
      else { JarJDKToolsLibrary._log.log("In DefaultGlobalModel.findLibraries, " + fromConfig + " is invalid"); }
    }
    else { JarJDKToolsLibrary._log.log("In DefaultGlobalModel.findLibraries, JAVAC_LOCATION not set"); }
    
    Iterable<JDKToolsLibrary> allFromRuntime = JDKToolsLibrary.makeFromRuntime(this);

    for(JDKToolsLibrary fromRuntime: allFromRuntime) {
      if (fromRuntime.isValid()) {
        if (! results.containsKey(getLibraryKey(LibraryKey.PRIORITY_RUNTIME, fromRuntime))) {
          JarJDKToolsLibrary._log.log("In DefaultGlobalModel.findLibraries, adding: " + fromRuntime + " from runtime");
          results.put(getLibraryKey(LibraryKey.PRIORITY_RUNTIME, fromRuntime), fromRuntime);
        }
      }
    }
    _log.log("In DefaultGlobalModel.findLibraries, compiler results = " + results);
    return IterUtil.reverse(results.values());
  }
  
//  public void junitAll() { _state.junitAll(); }
  
  /** Sets the build directory for a project. */
  public void setBuildDirectory(File f) {
    Utilities.show("setBuildDirectory(" + f + ") called");
    _state.setBuildDirectory(f);
    if (f != FileOps.NULL_FILE) {
      // This transaction appears redundant since the information is passed to the slave JVM after each compilation. */
      _mainJVM.addInteractionsClassPath(IOUtil.attemptAbsoluteFile(f));
    }
    
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
//    setClassPathChanged(true);
  }
  
  // ----- METHODS -----
  
  /** @return the interactions model. */
  public DefaultInteractionsModel getInteractionsModel() { return _interactionsModel; }
  
  /** @return InteractionsDJDocument in use by the InteractionsDocument. */
  public InteractionsDJDocument getSwingInteractionsDocument() { return _interactionsDocument; }
  
  public InteractionsDocument getInteractionsDocument() { return _interactionsModel.getDocument(); }
  
  /** Gets the CompilerModel, which provides all methods relating to compilers. */
  public CompilerModel getCompilerModel() { return _compilerModel; }
  
  /** Gets the JUnitModel, which provides all methods relating to JUnit testing. */
  public JUnitModel getJUnitModel() { return _junitModel; }
  
  /** Gets the ScaladocModel, which provides all methods relating to Scaladoc. */
  public ScaladocModel getScaladocModel() { return _scaladocModel; }
  
  public int getNumCompilerErrors() { return _numCompilerErrors; }
  public void setNumCompilerErrors(int num) { _numCompilerErrors = num; }
  
  /** Prepares this model to be thrown away.  Never called in practice outside of quit(), except in tests. */
  public void dispose() {
    ensureJVMStarterFinished();
    _mainJVM.dispose();
    _notifier.removeAllListeners();  // removes the global model listeners!
  }

  /** Ensures that the _interpreterJVMStarter thread has executed. Never called in practice outside of 
    * GlobalModelTestCase.setUp(). */
  public void ensureJVMStarterFinished() {
    try { _interpreterJVMStarter.join(); } // some tests were reach this point before _jvmStarter has completed
    catch (InterruptedException e) { throw new UnexpectedException(e); }
  }
  
  /** Disposes of external resources. Kills the slave JVM. */
  public void disposeExternalResources() { _mainJVM.stopInterpreterJVM(); }
  
  /** Convenience method for case where workingDirectory has not changed. */
  public void resetInteractions() { resetInteractions(_interactionsModel.getWorkingDirectory()); }
  
  /** Resets the interactions pane with specified working directory. Also clears the console if the option is indicated 
    * (on by default).  If {@code wd} is {@code null}, the former working directory is used. This method may run outside
    * the event thread.  This method is universally used to reset the interations pane; it may need to wait until a new 
    * interpreter can be started.
    */
  public void resetInteractions(File wd) {
    assert _interactionsModel._pane != null;
    assert EventQueue.isDispatchThread();
    
    _log.log("DefaultGlobalModel.resetInteractions(" + wd + ") called.");
  
    /* Determine working directory. */
    File workDir = _interactionsModel.getWorkingDirectory();
  
    if (wd == null) wd = workDir;
    
    /* This optimization is not working. Commented out for now. */

    if ((wd == workDir) && _mainJVM.classPathUnchanged()) {
      _log.log("Attempting to reset interpreter in resetInteractions"); 
      
      // Try to reset the interpreter internally without killing and restarting the slave JVM
      try {
        boolean success = _interactionsModel.resetInterpreter(wd);
        
        _log.log("_interactionsModel.resetInterpreter(" + wd + ") returned " + success);;
        if (success /* && ! _mainJVM.isDisposed()*/) {  // In some tests, _mainJVM is already disposed ?
          // inform InteractionsModel that interpreter is ready
          _interactionsModel._notifyInterpreterReady();  // _notifyInterpreterReady invokes the event thread
        }
      }
      catch(InterpreterBusyException e) {
        _log.log("resetInterpreter threw InterpreterBusy exception forcing hard reset.");
        hardResetInteractions(wd);
      }
    }
    else {
      _log.log("reset interpreter failed, forcing a hard reset");
      hardResetInteractions(wd);
    }
  
    final InteractionsDocument iDoc = _interactionsModel.getDocument();
//    iDoc.insertBeforeLastPrompt("Resetting Interactions ...", InteractionsDocument.ERROR_STYLE);
    
    _log.log("Reset is complete");   
  }
  
  public void hardResetInteractions(File wd) {
    
    assert _interactionsModel._pane != null;
    
    _log.log("DefaultGlobalModel.hardResetInteractions(" + wd + ") called.");
    
    // update the setting
    DrScala.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, wd);
    
    _hardResetInteractions();
    _log.log("Hard Reset is complete");
  }
      
  /** Reset the interactions pane by terminating the slave JVM. */
  private void _hardResetInteractions() {
    _log.log("performing a hard reset of interactions pane");
    // Reset interactions class path before creating new interpreter JVM
//    updateInteractionsClassPath();  /* should be unnecessary */
    _interactionsModel.setUpNewInterpreter();
    _log.log("Slave JVM including interpreter restarted");
  }
    
  /** Interprets the current given text at the prompt in the interactions pane. */
  public void interpretCurrentInteraction() { _interactionsModel.interpretCurrentInteraction(); }
  
  /** Interprets file selected in the FileOpenSelector. Assumes strings have no trailing whitespace. Interpretation is
    * aborted after the first error.
    */
  public void loadHistory(final FileOpenSelector selector) { 
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
        try {_interactionsModel.loadHistory(selector); } 
        catch(IOException e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  /** Loads the history/histories from the given selector. */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    return _interactionsModel.loadHistoryAsScript(selector);
  }
  
  /** Clears the interactions history */
  public void clearHistory() { _interactionsModel.getDocument().clearHistory(); }
  
  /** Saves the unedited version of the current history to a file
    * @param selector File to save to
    */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }

  /** Saves the unedited version of the current history to a file
    * @param doc Document to save
    * @param selector File to save to
    */
  public void saveConsoleCopy(ConsoleDocument doc, FileSaveSelector selector) throws IOException {
    doc.saveCopy(selector);
  }
  
  /** Saves the edited version of the current history to a file
    * @param selector File to save to
    * @param editedVersion Edited verison of the history which will be saved to file instead of the lines saved in 
    *        the history. The saved file will still include any tags needed to recognize it as a history file.
    */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector, editedVersion);
  }
  
  /** Returns the entire history as a String with semicolons as needed. */
  public String getHistoryAsStringWithSemicolons() {
    return _interactionsModel.getDocument().getHistoryAsStringWithSemicolons();
  }
  
  /** Returns the entire history as a String. */
  public String getHistoryAsString() {
    return _interactionsModel.getDocument().getHistoryAsString();
  }
  
  // ---------- ConcreteOpenDefDoc inner class ----------
  
  /** Inner class to handle operations on each of the open DefinitionsDocuments by the GlobalModel. <br><br>
    * This was at one time called the <code>DefinitionsDocumentHandler</code>
    * but was renamed (2004-Jun-8) to be more descriptive/intuitive.
    */
  class ConcreteOpenDefDoc extends AbstractGlobalModel.ConcreteOpenDefDoc {    
    /** Standard constructor for a document read from a file.  Initializes this ODD's DD.
      * @param f file describing DefinitionsDocument to manage
      */
    ConcreteOpenDefDoc(File f) {
      super(f);
      
      // update the syntax highlighting for this document
      // can't be done in AbstractGlobalModel.ConcreteOpenDefDoc because getCompilerModel is not supported
      updateSyntaxHighlighting();
    }
    
    /* Standard constructor for a new document (no associated file) */
    ConcreteOpenDefDoc(NullFile f) { super(f);
      
      // update the syntax highlighting for this document
      // can't be done in AbstractGlobalModel.ConcreteOpenDefDoc because getCompilerModel is not supported
      updateSyntaxHighlighting();
    }
    
    /** Starting compiling this document.  Used only for unit testing.  Only rus in the event thread. */
    public void startCompile() throws IOException { 
      assert EventQueue.isDispatchThread();
      _compilerModel.compile(ConcreteOpenDefDoc.this); 
    }
    
    private volatile InteractionsListener _runMain;
    
    /** Runs the main method in this document in the interactions pane after resetting interactions with the source
      * root for this document as the working directory.  Warns the use if the class files for the doucment are not 
      * up to date.  Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * NOTE: the command to run is constructed using {@link java.text.MessageFormat}. That means that certain characters,
      * single quotes and curly braces, for example, are special. To write single quotes, you need to double them.
      * To write curly braces, you need to enclose them in single quotes. Example:
      * MessageFormat.format("Abc {0} ''foo'' '{'something'}'", "def") returns "Abc def 'foo' {something}".
      * 
      * @param command  the command to run, with {0} indicating the place where the class name will be written
      * @param qualifiedClassName  the qualified name of the class (in this document) to run.  If NULL, it is the name
      *                             of the top level class.
      * 
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    protected void _runInInteractions(final String command, String qualifiedClassName) 
      throws ClassNameNotFoundException, IOException {
      
      assert EventQueue.isDispatchThread();
      
      _notifier.prepareForRun(ConcreteOpenDefDoc.this);
      
      String tempClassName = null;
      
      if(qualifiedClassName == null)
        tempClassName = getDocument().getQualifiedClassName();
      else
        tempClassName = qualifiedClassName;
      
      // Get the class name for this document, the first top level class in the document.
      final String className = tempClassName;
      final InteractionsDocument iDoc = _interactionsModel.getDocument();
      if (! checkIfClassFileInSync()) {
        iDoc.insertBeforeLastPrompt(DOCUMENT_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
        return;
      }
      
      _runMain = new DummyInteractionsListener() {
        public void interpreterReady() {
          /* Prevent listener from running twice.
           * This method was formerly called using SwingUtilities.invokeLater, in an attempt to ensure that the listener
           * would be removed AFTER the read lock of the notifier had been released [archaic].  But it did not work.
           * Now removeListener has been rewritten but it only runs in the event thread. Removal is done
           * as soon as possible. */
          
          _interactionsModel.removeListener(_runMain);  // listener cannot run
          
        }
//        /** Convenience method with familiar signature for interpreterReady. */
//        public void interpreterReady(File wd) { interpreterReady(); }
      };

      File oldWorkDir = _interactionsModel.getWorkingDirectory();
      _interactionsModel.addListener(_runMain);
      
      File workDir;
      workDir = getWorkingDirectory();
      
      // Reset interactions to the working directory
      resetInteractions(workDir);
    }
    
    /** Runs the main method in this document in the interactions pane after resetting interactions with the source
      * root for this document as the working directory.  Warns the use if the class files for the doucment are not 
      * up to date.  Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * 
      * @param qualifiedClassName  the qualified name of the class (in this document) to run.  If NULL, it is the name
      *                            of the top level class.
      * 
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    // public void runMain(String qualifiedClassName) throws ClassNameNotFoundException, IOException {
    //   _runInInteractions("java {0}", qualifiedClassName);
    // }
    public void runMain(String qualifiedClassName) throws ClassNameNotFoundException, IOException {
      // runs the main method of the standalone singleton object in this document, with {0} indicating the place where the object name 
      // will be written
      _runInInteractions("{0}.main(null)", qualifiedClassName);
    }
    
    /** Runs this document as applet in the interactions pane after resetting interactions with the source
      * root for this document as the working directory.  Warns the use if the class files for the doucment are not 
      * up to date.  Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * 
      * @param qualifiedClassName  the qualified name of the class (in this document) to run.  If NULL, it is the name
      *                            of the top level class.
      * 
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    public void runApplet(String qualifiedClassName) throws ClassNameNotFoundException, IOException {
      _runInInteractions("applet {0}", qualifiedClassName);
    }
    
    /** Runs this document, and tries to be smart about it. It detects if the class is a regular Java class with a
      * main method, if it is an applet, or if it is an ACM Java Task Force program. It runs the program appropriately
      * in the interactions pane after resetting interactions with the source root for this document as the
      * working directory.  Warns the use if the class files for the doucment are not up to date.
      * Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * 
      * @param qualifiedClassName  the qualified name of the class (in this document) to run.  If NULL, it is the name
      *                            of the top level class.
      * 
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    public void runSmart(String qualifiedClassName) throws ClassNameNotFoundException, IOException {
      _runInInteractions("run {0}", qualifiedClassName);
    }
    
    /** Runs JUnit on the current document.  Requires that all source documents are compiled before proceeding. */
    public void startJUnit() throws ClassNotFoundException, IOException { _junitModel.junit(this); }
    
    /** Generates Scaladoc for this document, saving the output to a temporary directory.  The location is provided to 
      * the scaladocEnded event on the given listener.
      * java@param saver FileSaveSelector for saving the file if it needs to be saved
      */
    public void generateScaladoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _scaladocModel.scaladocDocument(this, saver);
    }
  }
  
  /** Creates a ConcreteOpenDefDoc for a new DefinitionsDocument.
    * @return OpenDefinitionsDocument object for a new document
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(NullFile f) { return new ConcreteOpenDefDoc(f); }
  
  /** Creates a ConcreteOpenDefDoc for a given file f
    * @return OpenDefinitionsDocument object for f
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(File f) throws IOException { 
    if (! f.exists()) throw new FileNotFoundException("file " + f + " cannot be found");
    return new ConcreteOpenDefDoc(f); 
  }
  
  /* NOTE: this method appears redundant because the interactions class path is updated by the compilation process. */
  /** Adds the source root for doc to the interactions classpath; this function is a helper to _openFiles.
    * @param doc the document to add to the classpath
    */
  protected void addDocToClassPath(OpenDefinitionsDocument doc) {
    try {
      File sourceRoot = doc.getSourceRoot();
      Utilities.show("In DefaultGlobalModel.addDocToClassPath, adding '" + sourceRoot + "'to interactions class path");
      _interactionsModel.addInteractionsClassPath(sourceRoot);
//      setClassPathChanged(true);
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
  }

  /** Get the class path to be used in all class-related operations.  Used before compilation.
    * TODO: Ensure that this is used wherever appropriate.
    */
  public List<File> getClassPath() {
    ArrayList<File> result = new ArrayList<File>();
    
    if (isProjectActive()) {
      File buildDir = getBuildDirectory();
      if (buildDir != null && buildDir != FileOps.NULL_FILE) { 
        Utilities.show("Build Directory is " + buildDir);
        result.add(buildDir); 
      }
      
      /* We prefer to assume the project root is the project's source root, rather than checking *every* file in the 
       * project for its source root.  This is a bit problematic, because "Compile Project" won't care if the user 
       * has multiple source roots (or even just a single "src" subdirectory), and the user in this situation (assuming 
       * the build dir is null) wouldn't notice a problem until trying to access the compiled classes in the 
       * Interactions.
       */
      File projRoot = getProjectRoot();
      if (projRoot != null && projRoot != FileOps.NULL_FILE) { 
        result.add(projRoot);
        Utilities.show("Project root is " + projRoot);
      }
      
      List<AbsRelFile> projectExtras = getExtraProjectClassPath();
      if (projectExtras != null && projectExtras != FileOps.NULL_FILE) { 
        result.addAll(projectExtras); 
        Utilities.show("Project Extras " + projectExtras + " added to accumlated result in getClassPath()");
      }
    }
    else { result.addAll(getSourceRootSet()); }
    
    ArrayList<File> globalExtras = DrScala.getConfig().getSetting(EXTRA_CLASSPATH);
    if (globalExtras != null) { 
      result.addAll(globalExtras); 
      Utilities.show("Global Extras " + globalExtras + " added to class path");
    }
    
    /* We must add JUnit to the class path.  We do so by including the current JVM's class path (fixed on startup).
     * This is not ideal, because all other classes on the current class path (including all of DrScala's
     * internal classes) are also included.  But we're probably stuck doing something like this if we
     * want to continue bundling JUnit with DrJava.
     */
    result.addAll(ReflectUtil.SYSTEM_CLASS_PATH);
    Utilities.show("SYSTEM_CLASS_PATH = " + ReflectUtil.SYSTEM_CLASS_PATH);
    
    Utilities.show("getClassPath() is returning '" + result + "'");
    
    return result;
  }
  
  /** Returns the current class path actually in use by the Interpreter JVM. */
  public List<File> getInteractionsClassPath() {
    return _mainJVM.getInteractionsClassPath();
  }
  
  /** Ensures that all of the entries in getClassPath() appear in the interactions class path.  Entries are passed
    * to the Scala interpreter in reverse order so that the first entry is the last one passed. Can be called from
    * outside the event handling thread. */
  public void updateInteractionsClassPath() {
    List<File> icp = getClassPath();
    Utilities.show("In DefaultGlobalModel, updating interactions class path '" + icp + "'");
    _interactionsModel.addInteractionsClassPath(icp);
  }
}
