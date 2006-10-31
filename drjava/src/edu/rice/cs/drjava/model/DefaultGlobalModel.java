/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import javax.swing.text.BadLocationException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.SwingUtilities;

import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.newjvm.AbstractMasterJVM;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;

import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.JPDADebugger;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.debug.DebugListener;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.DebugThreadData;
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
import edu.rice.cs.drjava.model.junit.DefaultJUnitModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.ui.MainFrame;

import java.io.*;

/** Handles the bulk of DrJava's program logic. The UI components interface with the GlobalModel through its public
 *  methods, and teh GlobalModel responds via the GlobalModelListener interface. This removes the dependency on the 
 *  UI for the logical flow of the program's features.  With the current implementation, we can finally test the compile
 *  functionality of DrJava, along with many other things. <p>
 *  @version $Id$
 */
public class DefaultGlobalModel extends AbstractGlobalModel {
  
  /* FIELDS */
  
  /* static Log _log inherited from AbstractGlobalModel */
  
  /* Interpreter fields */
  
  /** The document  used in the Interactions model. */
  protected final InteractionsDJDocument _interactionsDocument;
  
  /** RMI interface to the Interactions JVM. */
  final MainJVM _jvm; 
  
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
        try {
          _jvm.addBuildDirectoryClassPath(FileOps.toURL(new File(buildDir.getAbsolutePath())));
        } catch(MalformedURLException murle) {
          // edit this later! this is bad! we should handle this exception better!
          throw new RuntimeException(murle);
        }
      }
    }
    
    public void interpreterResetFailed(Throwable t) { }
    
    public void interpreterExited(int status) { }
    
    public void interpreterChanged(boolean inProgress) { }
    
    public void interactionIncomplete() { }
    
    public void slaveJVMUsed() { }
  };
  
  private CompilerListener _clearInteractionsListener =
    new CompilerListener() {
    public void compileStarted() { }
    
    public void compileEnded(File workDir, File[] excludedFiles) {
      // Only clear interactions if there were no errors and unit testing is not in progress
      if ( ((_compilerModel.getNumErrors() == 0) || (_compilerModel.getCompilerErrorModel().hasOnlyWarnings()))
            && ! _junitModel.isTestInProgress() && _resetAfterCompile) {
        resetInteractions(workDir);  // use same working directory as current interpreter
      }
    }
    public void saveBeforeCompile() { }
    public void saveUntitled() { }
  };
    
  // ---- Compiler Fields ----
  
  /** CompilerModel manages all compiler functionality. */
  private final CompilerModel _compilerModel;
  
  /** Whether or not to reset the interactions JVM after compiling.  Should only be false in test cases. */
  private volatile boolean _resetAfterCompile = true;
  
  /** Number of errors in last compilation.  compilerModel._numErrors is trashed when the compile model is reset. */
  private volatile int _numCompErrors = 0;
  
  /* JUnit Fields */
  
  /** JUnitModel manages all JUnit functionality. */
  private final DefaultJUnitModel _junitModel;
  
  /* Javadoc Fields */
  
  /** Manages all Javadoc functionality. */
  protected final JavadocModel _javadocModel;
  
  /* Debugger Fields */
  
  /** Interface to the integrated debugger.  If unavailable, set NoDebuggerAvailable.ONLY. */
  private volatile Debugger _debugger = NoDebuggerAvailable.ONLY;
  
  /* CONSTRUCTORS */
  
  /** Constructs a new GlobalModel. Creates a new MainJVM and starts its Interpreter JVM. */
  public DefaultGlobalModel() {
//    AbstractMasterJVM._log.log(this + " has called contstructor for DefaultGlobal Model");
    File workDir = Utilities.TEST_MODE ? new File(System.getProperty("user.home")) : getWorkingDirectory();
    _jvm = new MainJVM(workDir);
//    AbstractMasterJVM._log.log(this + " has created a new MainJVM");
    _compilerModel = new DefaultCompilerModel(this);
    _junitModel = new DefaultJUnitModel(_jvm, _compilerModel, this);
    _javadocModel = new DefaultJavadocModel(this);
    _interactionsDocument = new InteractionsDJDocument();

    _interactionsModel = new DefaultInteractionsModel(this, _jvm, _interactionsDocument, workDir);
    _interactionsModel.addListener(_interactionsListener);
    _jvm.setInteractionsModel(_interactionsModel);
    _jvm.setJUnitModel(_junitModel);
    
    _jvm.setOptionArgs(DrJava.getConfig().getSetting(SLAVE_JVM_ARGS));
    
    DrJava.getConfig().addOptionListener(SLAVE_JVM_ARGS, new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oe) { _jvm.setOptionArgs(oe.value); }
    }); 
    
    _createDebugger();
        
    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _javadocModel.addListener(_notifier);
        
    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);
    
    // Note: starting the JVM in another thread does not appear to improve performance
//    AbstractMasterJVM._log.log("Starting the interpreter in " + this);
    _jvm.startInterpreterJVM();
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.    
//    _parsingControl = new DefaultLightWeightParsingControl(this);
  }
  

//  public void compileAll() throws IOException{ 
////    ScrollableDialog sd = new ScrollableDialog(null, "DefaultGlobalModel.compileAll() called", "", "");
////    sd.show();
//    _state.compileAll(); 
//  }
  

//  public void junitAll() { _state.junitAll(); }
  
  /** Sets the build directory for a project. */
  public void setBuildDirectory(File f) {
    _state.setBuildDirectory(f);
    if (f != null) {
      //      System.out.println("adding: " + f.getAbsolutePath());
      try {
        _jvm.addBuildDirectoryClassPath(FileOps.toURL(new File(f.getAbsolutePath())));
      }
      catch(MalformedURLException murle) {
        // TODO! change this! we should handle this exception better!
        // show a popup like "invalide build directory" or something
        throw new RuntimeException(murle);
      }
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
  
  public int getNumCompErrors() { return _numCompErrors; }
  public void setNumCompErrors(int num) { _numCompErrors = num; }
  
  /** Prepares this model to be thrown away.  Never called in practice outside of quit(), except in tests. */
  public void dispose() {
    // Kill the interpreter
    _jvm.killInterpreter(null);
    // Commented out because it invokes UnicastRemoteObject.unexport
//    try { _jvm.dispose(); }
//    catch(RemoteException e) { /* ignore */ }
    super.dispose();  // removes the global model listeners!
  }

  /** Disposes of external resources. Kills the slave JVM. */
  public void disposeExternalResources() {
    // Kill the interpreter
    _jvm.killInterpreter(null);
  }
  
  public void resetInteractions(File wd) { resetInteractions(wd, false); }
 
  /** Clears and resets the slave JVM with working directory wd. Also clears the console if the option is 
   *  indicated (on by default).  The reset operation is suppressed if the existing slave JVM has not been
   *  used, {@code wd} matches its working directory, and forceResest is false.
   */
  public void resetInteractions(File wd, boolean forceReset) {
//    _log.log("DefaultGlobalModel.resetInteractions called");
    File workDir = _interactionsModel.getWorkingDirectory();
//    _log.log("New working directory = " + wd +"; current working directory = " + workDir + ";");

    if (! forceReset && ! _jvm.slaveJVMUsed() && ! isClassPathChanged() && wd.equals(workDir)) {
    // Eliminate resetting interpreter (slaveJVM) since it has already been reset appropriately.
//      _log.log("Suppressing resetting of interactions pane");
      _interactionsModel._notifyInterpreterReady(wd);
      return; 
    }
//    _log.log("Resetting interactions with working directory = " + wd);
    if (DrJava.getConfig().getSetting(STICKY_INTERACTIONS_DIRECTORY)) {    
      // update the setting
      DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, wd);
    }
    _interactionsModel.resetInterpreter(wd);
  }

  /** Interprets the current given text at the prompt in the interactions pane. */
  public void interpretCurrentInteraction() { _interactionsModel.interpretCurrentInteraction(); }

  /** Interprets file selected in the FileOpenSelector. Assumes strings have no trailing whitespace. Interpretation is
   *  aborted after the first error.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException { _interactionsModel.loadHistory(selector); }

  /** Loads the history/histories from the given selector. */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    return _interactionsModel.loadHistoryAsScript(selector);
  }

  /** Clears the interactions history */
  public void clearHistory() { _interactionsModel.getDocument().clearHistory(); }

  /** Saves the unedited version of the current history to a file
   *  @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }

  /** Saves the edited version of the current history to a file
   *  @param selector File to save to
   *  @param editedVersion Edited verison of the history which will be saved to file instead of the lines saved in 
   *         the history. The saved file will still include any tags needed to recognize it as a history file.
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

  /** Blocks until the interpreter has registered. */
  public void waitForInterpreter() { _jvm.ensureInterpreterConnected(); }


  /** Returns the current classpath in use by the Interpreter JVM. */
  public ClassPathVector getClassPath() { return _jvm.getClassPath(); }
  
  /** Sets whether or not the Interactions JVM will be reset after a compilation succeeds.  This should ONLY be used 
   *  in tests!  This method is not supported by AbstractGlobalModel.
   *  @param shouldReset Whether to reset after compiling
   */
  void setResetAfterCompile(boolean shouldReset) { _resetAfterCompile = shouldReset; }

  /** Gets the Debugger used by DrJava. */
  public Debugger getDebugger() { return _debugger; }

  /** Returns an available port number to use for debugging the interactions JVM.
   *  @throws IOException if unable to get a valid port number.
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
    ConcreteOpenDefDoc(File f) throws IOException { super(f); }
    
    /* Standard constructor for a new document (no associated file) */
    ConcreteOpenDefDoc() { super(); }
    
    /** Starting compiling this document.  Used only for unit testing */
    public void startCompile() throws IOException { _compilerModel.compile(ConcreteOpenDefDoc.this); }
    
    private volatile InteractionsListener _runMain;

    /** Runs the main method in this document in the interactions pane after resetting interactions with the source
      * root for this document as the working directory.  Warns the use if the class files for the doucment are not 
      * up to date.  Fires an event to signal when execution is about to begin.
      * NOTE: this code normally runs in the event thread; it cannot block waiting for an event that is triggered by
      * event thread execution!
      * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
      * @exception IOException propagated from GlobalModel.compileAll()
      */
    public void runMain() throws ClassNameNotFoundException, IOException {
      
      // Get the class name for this document, the first top level class in the document.
      final String className = getDocument().getQualifiedClassName();
      final InteractionsDocument iDoc = _interactionsModel.getDocument();
      if (! checkIfClassFileInSync()) {
        iDoc.insertBeforeLastPrompt(DOCUMENT_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
        return;
      }
      
      final boolean wasDebuggerEnabled = getDebugger().isReady();
      
      _runMain = new DummyInteractionsListener() {
        public void interpreterReady(File wd) {
          // Restart debugger if it was previously enabled and is now off
          if (wasDebuggerEnabled && (! getDebugger().isReady())) {
            try { getDebugger().startup(); } catch(DebugException de) { /* ignore, continue without debugger */ }
          }
          
          // Load the proper text into the interactions document
          iDoc.clearCurrentInput();
          iDoc.append("java " + className, null);
          
          // Finally, execute the new interaction and record that event
          _interactionsModel.interpretCurrentInteraction();
          _notifier.runStarted(ConcreteOpenDefDoc.this);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              /* Remove _runMain listener AFTER this interpreterReady listener completes and DROPS it acquireReadLock on
               * _interactionsModel._notifier. */
              _interactionsModel.removeListener(_runMain);
            }
          });
          
        }
      };
      
      _interactionsModel.addListener(_runMain);
      
      File workDir;
      if (isProjectActive()) workDir = getWorkingDirectory(); // use working directory for project
      else workDir = getSourceRoot();  // use source root of current document
      
      // Reset interactions to the soure root for this document; class will be executed when new interpreter is ready
      resetInteractions(workDir);  
    }

    /** Runs JUnit on the current document.  Requires that all source documents are compiled before proceeding. */
    public void startJUnit() throws ClassNotFoundException, IOException { _junitModel.junit(this); }

    /** Generates Javadoc for this document, saving the output to a temporary directory.  The location is provided to 
      * the javadocEnded event on the given listener.
      * java@param saver FileSaveSelector for saving the file if it needs to be saved
      */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _javadocModel.javadocDocument(this, saver, getClassPath().toString());
    }

    /** Called to indicate the document is being closed, so to remove all related state from the debug manager. */
    public void removeFromDebugger() {
      while (getBreakpointManager().getRegions().size() > 0) {
        Breakpoint bp = getBreakpointManager().getRegions().get(0);
        getBreakpointManager().removeRegion(bp);
      }
    }
  } /* End of ConcreteOpenDefDoc */
  
  /** Creates a ConcreteOpenDefDoc for a new DefinitionsDocument.
   *  @return OpenDefinitionsDocument object for a new document
   */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument() { return new ConcreteOpenDefDoc(); }
  
   /** Creates a ConcreteOpenDefDoc for a given file f
   *  @return OpenDefinitionsDocument object for f
   */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(File f) throws IOException { return new ConcreteOpenDefDoc(f); }
  
  /** Adds the source root for doc to the interactions classpath; this function is a helper to _openFiles.
   *  @param doc the document to add to the classpath
   */
  protected void addDocToClassPath(OpenDefinitionsDocument doc) {
    try {
      File classPath = doc.getSourceRoot();
      try {
        URL pathURL = FileOps.toURL(classPath);
        if (doc.isAuxiliaryFile())
          _interactionsModel.addProjectFilesClassPath(pathURL);
        else _interactionsModel.addExternalFilesClassPath(pathURL);
        setClassPathChanged(true);
      }
      catch(MalformedURLException murle) {  /* fail silently */ }
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
  }
   
  /** Instantiates the integrated debugger if the "debugger.enabled" config option is set to true.  Leaves it 
   *  at null if not.
   */
  private void _createDebugger() {
    try {
      _debugger = new JPDADebugger(this);
      _jvm.setDebugModel((JPDADebugger) _debugger);

      // add listener to set the project file to "changed" when a breakpoint or watch is added, removed, or changed
      getBreakpointManager().addListener(new RegionManagerListener<Breakpoint>() {
        public void regionAdded(final Breakpoint bp, int index) { setProjectChanged(true); }
        public void regionChanged(final Breakpoint bp, int index) { setProjectChanged(true); }
        public void regionRemoved(final Breakpoint bp) { 
          try {
            getDebugger().removeBreakpoint(bp);
          } catch(DebugException de) { /* just ignore it */ }
          setProjectChanged(true);
          }
      });
      getBookmarkManager().addListener(new RegionManagerListener<DocumentRegion>() {
        public void regionAdded(DocumentRegion r, int index) { setProjectChanged(true); }
        public void regionChanged(DocumentRegion r, int index) { setProjectChanged(true); }
        public void regionRemoved(DocumentRegion r) { setProjectChanged(true); }
      });
      
      _debugger.addListener(new DebugListener() {
        public void watchSet(final DebugWatchData w) { setProjectChanged(true); }
        public void watchRemoved(final DebugWatchData w) { setProjectChanged(true); }    
        
        public void regionAdded(final Breakpoint bp, int index) { }
        public void regionChanged(final Breakpoint bp, int index) { }
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
    catch( NoClassDefFoundError ncdfe ) {
      // JPDA not available, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( UnsupportedClassVersionError ucve ) {
      // Wrong version of JPDA, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( Throwable t ) {
      // Something went wrong in initialization, don't use debugger
      _debugger = NoDebuggerAvailable.ONLY;
    }
  }
  
  /** Adds the project root (if a project is open), the source roots for other open documents, the paths in the 
    * "extra classpath" config option, as well as any project-specific classpaths to the interpreter's classpath. 
    * This method is called in DefaultInteractionsModel when the interpreter becomes ready.
    */
  public void resetInteractionsClassPath() {
    ClassPathVector projectExtras = getExtraClassPath();
    //System.out.println("Adding project classpath vector to interactions classpath: " + projectExtras);
    if (projectExtras != null)  for (URL cpE : projectExtras) { _interactionsModel.addProjectClassPath(cpE); }
    
    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if (cp != null) {
      for (File f : cp) {
        try { _interactionsModel.addExtraClassPath(FileOps.toURL(f)); }
        catch(MalformedURLException murle) {
          System.out.println("File " + f + " in your extra classpath could not be parsed to a URL; " +
                             "it may contain un-URL-encodable characters.");
        }
      }
    }
    
    for (OpenDefinitionsDocument odd: getAuxiliaryDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try { _interactionsModel.addProjectFilesClassPath(FileOps.toURL(odd.getSourceRoot())); }
      catch(MalformedURLException murle) { /* fail silently */ }
      catch(InvalidPackageException e) {  /* ignore it */ }
    }
    
    for (OpenDefinitionsDocument odd: getNonProjectDocuments()) {
      // this forwards directly to InterpreterJVM.addClassPath(String)
      try { 
        File sourceRoot = odd.getSourceRoot();
        if (sourceRoot != null) _interactionsModel.addExternalFilesClassPath(FileOps.toURL(sourceRoot)); 
      }
      catch(MalformedURLException murle) { /* ignore it */ }
      catch(InvalidPackageException e) { /* ignore it */ }
    }
    
    // add project source root to projectFilesClassPath.  All files in project tree have this root.
    
    try { _interactionsModel.addProjectFilesClassPath(FileOps.toURL(getProjectRoot())); }
    catch(MalformedURLException murle) { /* fail silently */ } 
    setClassPathChanged(false);  // reset classPathChanged state
  }
  
//  private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
//    public void optionChanged (OptionEvent<Vector<File>> oce) {
//      Vector<File> cp = oce.value;
//      if (cp != null) {
//        for (File f: cp) {
//          // this forwards directly to InterpreterJVM.addClassPath(String)
//          try { _interactionsModel.addExtraClassPath(f.toURL()); }
//          catch(MalformedURLException murle) { 
//            /* do nothing; findbugs signals a bug unless this catch clause spans more than two lines */ 
//          }
//        }
//      }
//    }
//  }
  
}
