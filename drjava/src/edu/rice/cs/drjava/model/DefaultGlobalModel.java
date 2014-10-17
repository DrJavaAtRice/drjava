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

package edu.rice.cs.drjava.model;


import java.awt.EventQueue;

import java.io.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.TreeMap;

import edu.rice.cs.drjava.DrJava;

import edu.rice.cs.drjava.config.BooleanOption;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.compiler.DummyCompilerListener;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.debug.DebugListener;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.DebugThreadData;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.javadoc.NoJavadocAvailable;
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

/** Handles the bulk of DrJava's program logic. The UI components interface with the GlobalModel through its public
  * methods, and the GlobalModel responds via the GlobalModelListener interface. This removes the dependency on the 
  * UI for the logical flow of the program's features.  With the current implementation, we can finally test the compile
  * functionality of DrJava, along with many other things. <p>
  * @version $Id: DefaultGlobalModel.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class DefaultGlobalModel extends AbstractGlobalModel {
  /* FIELDS */
  
  /* static Log _log inherited from AbstractGlobalModel */
  
  /* Interpreter fields */
  
  /** The document used in the Interactions model. */
  protected final InteractionsDJDocument _interactionsDocument;
  
  /** RMI interface to the Interactions JVM. */
  final MainJVM _jvm; 
  
  private final Thread _jvmStarter; // thread that invokes _jvm.startInterpreterJVM()
  
  /** Interface between the InteractionsDocument and the JavaInterpreter, which runs in a separate JVM. */
  protected final DefaultInteractionsModel _interactionsModel;
  
  /** Core listener attached to interactions model */
  protected InteractionsListener _interactionsListener = new InteractionsListener() {
    public void interactionStarted() { }
    
    public void interactionEnded() { }
    
    public void interactionErrorOccurred(int offset, int length) { }
    
    public void interpreterResetting() { }
    
    public void interpreterReady(File wd) {
      File buildDir = _state.getBuildDirectory();
      if (buildDir != null) {
        //        System.out.println("adding for reset: " + _state.getBuildDirectory().getAbsolutePath());
        _jvm.addBuildDirectoryClassPath(IOUtil.attemptAbsoluteFile(buildDir));
      }
    }
    
    public void interpreterResetFailed(Throwable t) { }
    
    public void interpreterExited(int status) { }
    
    public void interpreterChanged(boolean inProgress) { }
    
    public void interactionIncomplete() { }
  };
  
  private CompilerListener _clearInteractionsListener = new DummyCompilerListener() {
    public void compileEnded(File workDir, List<? extends File> excludedFiles) {
      // Only clear interactions if there were no errors and unit testing is not in progress
      if ( (_compilerModel.getNumErrors() == 0 || _compilerModel.getCompilerErrorModel().hasOnlyWarnings())
            && ! _junitModel.isTestInProgress() && _resetAfterCompile) {
//        Utilities.show("compileEnded called in clearInteractionsListener");
        resetInteractions(workDir);  // use same working directory as current interpreter
      }
    }
    public void activeCompilerChanged() {
      File workDir = _interactionsModel.getWorkingDirectory();
      resetInteractions(workDir, true);  // use same working directory as current interpreter
    }
  };
  
  // ---- Compiler Fields ----
  
  /** CompilerModel manages all compiler functionality. */
  private final CompilerModel _compilerModel;
  
  /** Whether or not to reset the interactions JVM after compiling.  Should only be false in test cases. */
  private volatile boolean _resetAfterCompile = true;
  
  /** Number of errors in last compilation.  compilerModel._numErrors is trashed when the compile model is reset. */
  private volatile int _numCompilerErrors = 0;
  
  /* JUnit Fields */
  
  /** JUnitModel manages all JUnit functionality. */
  private final DefaultJUnitModel _junitModel;
  
  /* Javadoc Fields */
  
  /** Manages all Javadoc functionality. */
  protected volatile JavadocModel _javadocModel;
  
  /* Debugger Fields */
  
  /** Interface to the integrated debugger.  If unavailable, set NoDebuggerAvailable.ONLY. */
  private volatile Debugger _debugger;
  
  /* CONSTRUCTORS */
  /** Constructs a new GlobalModel. Creates a new MainJVM and starts its Interpreter JVM. */
  public DefaultGlobalModel() {
    Iterable<? extends JDKToolsLibrary> tools = findLibraries();
    List<CompilerInterface> compilers = new LinkedList<CompilerInterface>();
    _debugger = null;
    _javadocModel = null;
    for (JDKToolsLibrary t : tools) {
      // check for support of JAVA_5; Scala 2.9.* requires Java 5. */
      if (t.compiler().isAvailable() && t.version().supports(JavaVersion.JAVA_5)) {
          compilers.add(t.compiler());
      }
      if (_debugger == null && t.debugger().isAvailable()) { _debugger = t.debugger(); }
      if (_javadocModel == null && t.javadoc().isAvailable()) { _javadocModel = t.javadoc(); }
    }
    if (_debugger == null) { _debugger = NoDebuggerAvailable.ONLY; }
    if (_javadocModel == null) { _javadocModel = new NoJavadocAvailable(this); }
    
    File workDir = Utilities.TEST_MODE ? new File(System.getProperty("user.home")) : getWorkingDirectory();
    _jvm = new MainJVM(workDir);
//    AbstractMasterJVM._log.log(this + " has created a new MainJVM");
    _compilerModel = new DefaultCompilerModel(this, compilers);     
    _junitModel = new DefaultJUnitModel(_jvm, _compilerModel, this);
    _interactionsDocument = new InteractionsDJDocument(_notifier);
    
    _interactionsModel = new DefaultInteractionsModel(this, _jvm, _interactionsDocument, workDir);
    _interactionsModel.addListener(_interactionsListener);
    _jvm.setInteractionsModel(_interactionsModel);
    _jvm.setJUnitModel(_junitModel);
    
    _setupDebugger();
    
    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _javadocModel.addListener(_notifier);
    
    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);
    
    _jvmStarter = new Thread("Start interpreter JVM") {
      public void run() { _jvm.startInterpreterJVM(); }
    };
    _jvmStarter.start();
    
// Lightweight parsing has been disabled until we have something that is beneficial and works better in the background.    
//    _parsingControl = new DefaultLightWeightParsingControl(this);
  }

  // makes the version coarser, if desired: if DISPLAY_ALL_COMPILER_VERSIONS is disabled, then only
  // the major version and the vendor will be considered
  private static JavaVersion.FullVersion coarsenVersion(JavaVersion.FullVersion tVersion) {
    BooleanOption displayAllOption = edu.rice.cs.drjava.config.OptionConstants.DISPLAY_ALL_COMPILER_VERSIONS;
    if (!DrJava.getConfig().getSetting(displayAllOption).booleanValue()) {
      tVersion = tVersion.onlyMajorVersionAndVendor();
    }
    return tVersion;
  }
  
  // A pair of version and descriptor.
  // If the descriptor is something different than JDKDescriptor.NONE, then this pair will always
  // return false for equals(), except if it is compared to the identical pair.
  private static class LibraryKey implements Comparable<LibraryKey> {    
    public static final int PRIORITY_BUILTIN = 0;  // Currently the Eclipse 0.A48 compiler
    public static final int PRIORITY_SEARCH = 1;
    public static final int PRIORITY_RUNTIME = 2;
    public static final int PRIORITY_SCALA = 3;
    public static final int PRIORITY_CONFIG = 4;

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
    
    JarJDKToolsLibrary._log.log("Creating DefaultGlobalModel; " + JavaVersion.CURRENT + " is running");
    File configTools = DrJava.getConfig().getSetting(JAVAC_LOCATION);
    if (configTools != FileOps.NULL_FILE) {
      JDKToolsLibrary fromConfig = JarJDKToolsLibrary.makeFromFile(configTools, this, JDKDescriptor.NONE);
      if (fromConfig.isValid()) { 
        JarJDKToolsLibrary._log.log("Adding: " + fromConfig  + " from config");
        results.put(getLibraryKey(LibraryKey.PRIORITY_CONFIG, fromConfig), fromConfig);
      }
      else { JarJDKToolsLibrary._log.log("From config: invalid " + fromConfig); }
    }
    else { JarJDKToolsLibrary._log.log("From config: not set"); }
    
    Iterable<JDKToolsLibrary> allFromRuntime = JDKToolsLibrary.makeFromRuntime(this);

    for(JDKToolsLibrary fromRuntime: allFromRuntime) {
      if (fromRuntime.isValid()) {
        if (! results.containsKey(getLibraryKey(LibraryKey.PRIORITY_RUNTIME, fromRuntime))) {
          JarJDKToolsLibrary._log.log("Adding: " + fromRuntime + "from runtime");
          results.put(getLibraryKey(LibraryKey.PRIORITY_RUNTIME, fromRuntime), fromRuntime);
        }
//        else { JarJDKToolsLibrary._log.log("From runtime: duplicate "+fromRuntime); }
      }
//      else { JarJDKToolsLibrary._log.log("From runtime: invalid " + fromRuntime); }
    }
    
    Iterable<JarJDKToolsLibrary> fromSearch = JarJDKToolsLibrary.search(this);
    for (JDKToolsLibrary t : fromSearch) {
      JavaVersion.FullVersion tVersion = t.version();
//      JarJDKToolsLibrary._log.log("From search: "+t);
//      JavaVersion.FullVersion coarsenedVersion = coarsenVersion(tVersion);
//      JarJDKToolsLibrary._log.log("\ttVersion: " + tVersion + " " + tVersion.vendor());
//      JarJDKToolsLibrary._log.log("\tcoarsenedVersion: " + coarsenedVersion + " " + coarsenedVersion.vendor());
      // give a lower priority to built-in compilers
      int priority = (edu.rice.cs.util.FileOps.getDrJavaFile().equals(tVersion.location())) ? 
        LibraryKey.PRIORITY_BUILTIN : LibraryKey.PRIORITY_SEARCH;
      if (t.compiler().getSuggestedFileExtension().equals(OptionConstants.SCALA_FILE_EXTENSION)) priority = LibraryKey.PRIORITY_SCALA;
      if (! results.containsKey(getLibraryKey(priority, t))) {
        JarJDKToolsLibrary._log.log("Adding: " + t + " with extension " + t.compiler().getSuggestedFileExtension() + " and priority " + priority);
        results.put(getLibraryKey(priority, t), t);
      }
//      else { JarJDKToolsLibrary._log.log("\tduplicate"); }
    }
    
    JarJDKToolsLibrary._log.log("***** Compiler results = " + results);
    return IterUtil.reverse(results.values());
  }
  
//  public void junitAll() { _state.junitAll(); }
  
  /** Sets the build directory for a project. */
  public void setBuildDirectory(File f) {
    _state.setBuildDirectory(f);
    if (f != FileOps.NULL_FILE) {
      //      System.out.println("adding: " + f.getAbsolutePath());
      _jvm.addBuildDirectoryClassPath(IOUtil.attemptAbsoluteFile(f));
    }
    
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
    setClassPathChanged(true);
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
  
  /** Gets the JavadocModel, which provides all methods relating to Javadoc. */
  public JavadocModel getJavadocModel() { return _javadocModel; }
  
  public int getNumCompilerErrors() { return _numCompilerErrors; }
  public void setNumCompilerErrors(int num) { _numCompilerErrors = num; }
  
  /** Prepares this model to be thrown away.  Never called in practice outside of quit(), except in tests. */
  public void dispose() {
    ensureJVMStarterFinished();
    _jvm.dispose();
    _notifier.removeAllListeners();  // removes the global model listeners!
  }

  /** Ensures that the _jvmStarter thread has executed. Never called in practice outside of GlobalModelTestCase.setUp(). */
  public void ensureJVMStarterFinished() {
    try { _jvmStarter.join(); } // some tests were reach this point before _jvmStarter has completed
    catch (InterruptedException e) { throw new UnexpectedException(e); }
  }
  
  /** Disposes of external resources. Kills the slave JVM. */
  public void disposeExternalResources() { _jvm.stopInterpreterJVM(); }
  
  public void resetInteractions(File wd) { resetInteractions(wd, false); }
  
  /** Clears and resets the slave JVM with working directory wd. Also clears the console if the option is 
    * indicated (on by default).  The reset operation is suppressed if the existing slave JVM has not been
    * used, {@code wd} matches its working directory, and forceReset is false.  {@code wd} may be {@code null}
    * if a valid directory cannot be determined.  In that case, the former working directory is used.  This
    * method may run outside the event thread.
    */
  public void resetInteractions(File wd, boolean forceReset) {
    assert _interactionsModel._pane != null;
    
    debug.logStart();
    File workDir = _interactionsModel.getWorkingDirectory();
    if (wd == null) { wd = workDir; }
    forceReset |= isClassPathChanged();
    forceReset |= !wd.equals(workDir);
    // update the setting
    DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, wd);
    getDebugger().setAutomaticTraceEnabled(false);
    _interactionsModel.resetInterpreter(wd, forceReset);
    debug.logEnd();
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
  
  /** Called when the debugger wants to print a message.  Inserts a newline. */
  public void printDebugMessage(String s) {
    _interactionsModel.getDocument().
      insertBeforeLastPrompt(s + "\n", InteractionsDocument.DEBUGGER_STYLE);
  }
  
  /** Returns the current class path in use by the Interpreter JVM. */
  public Iterable<File> getInteractionsClassPath() {
    return _jvm.getClassPath().unwrap(IterUtil.<File>empty());
  }
  
  /** Sets whether or not the Interactions JVM will be reset after a compilation succeeds.  This should ONLY be used 
    * in tests!  This method is not supported by AbstractGlobalModel.
    * @param shouldReset Whether to reset after compiling
    */
  void setResetAfterCompile(boolean shouldReset) { _resetAfterCompile = shouldReset; }
  
  /** Gets the Debugger used by DrJava. */
  public Debugger getDebugger() { return _debugger; }
  
  /** Returns an available port number to use for debugging the interactions JVM.
    * @throws IOException if unable to get a valid port number.
    */
  public int getDebugPort() throws IOException { return _interactionsModel.getDebugPort(); }
  
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
    protected void _runInInteractions(final String command, String qualifiedClassName) throws ClassNameNotFoundException, 
      IOException {
      
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
      
      final boolean wasDebuggerEnabled = getDebugger().isReady();
      
      _runMain = new DummyInteractionsListener() {
        public void interpreterReady(File wd) {
          /* Prevent listener from running twice.
           * This method was formerly called using SwingUtilities.invokeLater, in an attempt to ensure that the listener
           * would be removed AFTER the read lock of the notifier had been released [archaic].  But it did not work.
           * Now removeListener has been rewritten but it only runs in the even thread. Removal is done
           * as soon as possible. */
          
          _interactionsModel.removeListener(_runMain);  // listener cannot run
          
          // Run debugger restart in an invokeLater so that the InteractionsModel EventNotifier
          // reader-writer lock isn't held anymore.
          javax.swing.SwingUtilities.invokeLater(new Runnable() {   
            public void run() {
              // Restart debugger if it was previously enabled and is now off
              if (wasDebuggerEnabled && (! getDebugger().isReady())) {
//            System.err.println("Trying to start debugger");
                try { getDebugger().startUp(); } catch(DebugException de) { /* ignore, continue without debugger */ }
              }
              // Load the proper text into the interactions document
              iDoc.clearCurrentInput();
              iDoc.append(java.text.MessageFormat.format(command, className), null);
              
              // Finally, execute the new interaction and record that event
              new Thread("Running document") {
                public void run() { _interactionsModel.interpretCurrentInteraction(); }
              }.start();
            }
          });
        }
      };
      
      File oldWorkDir = _interactionsModel.getWorkingDirectory();
      _interactionsModel.addListener(_runMain);
      
      File workDir;
      workDir = getWorkingDirectory();
      
      // Reset interactions to the working directory
      resetInteractions(workDir, !workDir.equals(oldWorkDir));
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
    public void runMain(String qualifiedClassName) throws ClassNameNotFoundException, IOException {
      _runInInteractions("java {0}", qualifiedClassName);
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
    
    /** Generates Javadoc for this document, saving the output to a temporary directory.  The location is provided to 
      * the javadocEnded event on the given listener.
      * java@param saver FileSaveSelector for saving the file if it needs to be saved
      */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _javadocModel.javadocDocument(this, saver);
    }
    
    /** Called to indicate the document is being closed, so to remove all related state from the debug manager. */
    public void removeFromDebugger() { getBreakpointManager().removeRegions(this); }
    
    // This creation context is useful for debugging memory leaks in DefinitionsPaneMemoryLeakTest.
    // It should be commented out for normal compilation.
//    String creationContext;
//    {
//      StringWriter sw = new StringWriter();
//      new RuntimeException("new ConcreteOpenDefDoc").printStackTrace(new PrintWriter(sw));
//      creationContext = sw.toString();
//    }
  } /* End of ConcreteOpenDefDoc */
  
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
  
  /** Adds the source root for doc to the interactions classpath; this function is a helper to _openFiles.
    * @param doc the document to add to the classpath
    */
  protected void addDocToClassPath(OpenDefinitionsDocument doc) {
    try {
      File sourceRoot = doc.getSourceRoot();
      if (doc.isAuxiliaryFile()) { _interactionsModel.addProjectFilesClassPath(sourceRoot); }
      else { _interactionsModel.addExternalFilesClassPath(sourceRoot); }
      setClassPathChanged(true);
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
  }
  
  private void _setupDebugger() {
    _jvm.setDebugModel(_debugger.callback());
    
    // add listener to set the project file to "changed" when a breakpoint or watch is added, removed, or changed
    getBreakpointManager().addListener(new RegionManagerListener<Breakpoint>() {
      public void regionAdded(final Breakpoint bp) { setProjectChanged(true); }
      public void regionChanged(final Breakpoint bp) { setProjectChanged(true); }
      public void regionRemoved(final Breakpoint bp) { 
        try { getDebugger().removeBreakpoint(bp); } 
        catch(DebugException de) {
          /* just ignore it */
          // TODO: should try to pop up dialog to give the user the option of restarting the debugger (mgricken)
//          int result = JOptionPane.showConfirmDialog(null, "Could not remove breakpoint.", "Restart debugger?", JOptionPane.YES_NO_OPTION);
//          if (result==JOptionPane.YES_OPTION) {
//            getDebugger().shutdown();
//            getDebugger().startUp();
//          }
        }
        setProjectChanged(true);
      }
    });
    getBookmarkManager().addListener(new RegionManagerListener<MovingDocumentRegion>() {
      public void regionAdded(MovingDocumentRegion r) { setProjectChanged(true); }
      public void regionChanged(MovingDocumentRegion r) { setProjectChanged(true); }
      public void regionRemoved(MovingDocumentRegion r) { setProjectChanged(true); }
    });
    
    _debugger.addListener(new DebugListener() {
      public void watchSet(final DebugWatchData w) { setProjectChanged(true); }
      public void watchRemoved(final DebugWatchData w) { setProjectChanged(true); }    
      
      public void regionAdded(final Breakpoint bp) { }
      public void regionChanged(final Breakpoint bp) { }
      public void regionRemoved(final Breakpoint bp) { }
      public void debuggerStarted() { }
      public void debuggerShutdown() { }
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) { }
      public void breakpointReached(final Breakpoint bp) { }
      public void stepRequested() { }
      public void currThreadSuspended() { }
      public void currThreadResumed() { }
      public void threadStarted() { }
      public void currThreadDied() { }
      public void nonCurrThreadDied() {  }
      public void currThreadSet(DebugThreadData thread) { }
    });
  }
  
  /** Get the class path to be used in all class-related operations.
    * TODO: Ensure that this is used wherever appropriate.
    */
  public Iterable<File> getClassPath() {
    Iterable<File> result = IterUtil.empty();
    
    if (isProjectActive()) {
      File buildDir = getBuildDirectory();
      if (buildDir != null) { result = IterUtil.compose(result, buildDir); }
      
      /* We prefer to assume the project root is the project's source root, rather than
       * checking *every* file in the project for its source root.  This is a bit problematic,
       * because "Compile Project" won't care if the user has multiple source roots (or even just a
       * single "src" subdirectory), and the user in this situation (assuming the build dir is 
       * null) wouldn't notice a problem until trying to access the compiled classes in the 
       * Interactions.
       */
      File projRoot = getProjectRoot();
      if (projRoot != null) { result = IterUtil.compose(result, projRoot); }
      
      Iterable<AbsRelFile> projectExtras = getExtraProjectClassPath();
      if (projectExtras != null) { result = IterUtil.compose(result, projectExtras); }
    }
    else { result = IterUtil.compose(result, getSourceRootSet()); }
    
    Vector<File> globalExtras = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if (globalExtras != null) { result = IterUtil.compose(result, globalExtras); }
    
    /* We must add JUnit to the class path.  We do so by including the current JVM's class path.
     * This is not ideal, because all other classes on the current class path (including all of DrJava's
     * internal classes) are also included.  But we're probably stuck doing something like this if we
     * want to continue bundling JUnit with DrJava.
     */
    result = IterUtil.compose(result, ReflectUtil.SYSTEM_CLASS_PATH);
    
    return result;
  }
  
  /** Adds the project root (if a project is open), the source roots for other open documents, the paths in the 
    * "extra classpath" config option, as well as any project-specific classpaths to the interpreter's classpath. 
    * This method is called in DefaultInteractionsModel when the interpreter becomes ready.  Runs outside the event
    * thread.
    */
  public void resetInteractionsClassPath() {
//    System.err.println("Resetting interactions class path");
    Iterable<AbsRelFile> projectExtras = getExtraProjectClassPath();
    //System.out.println("Adding project classpath vector to interactions classpath: " + projectExtras);
    if (projectExtras != null)  for (File cpE : projectExtras) { _interactionsModel.addProjectClassPath(cpE); }
    
    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if (cp != null) {
      for (File f : cp) { _interactionsModel.addExtraClassPath(f); }
    }
    
    for (OpenDefinitionsDocument odd: getAuxiliaryDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try { _interactionsModel.addProjectFilesClassPath(odd.getSourceRoot()); }
      catch(InvalidPackageException e) {  /* ignore it */ }
    }
    
    for (OpenDefinitionsDocument odd: getNonProjectDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try {
        File sourceRoot = odd.getSourceRoot();
        if (sourceRoot != null) _interactionsModel.addExternalFilesClassPath(sourceRoot); 
      }
      catch(InvalidPackageException e) { /* ignore it */ }
    }
    
    // add project source root to projectFilesClassPath.  All files in project tree have this root.
    
    _interactionsModel.addProjectFilesClassPath(getProjectRoot());  // is sync advisable here?
    setClassPathChanged(false);  // reset classPathChanged state  // Why is this flag set to false here?
  } 
}
