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
 * whom the Software is furnished to do new inavso, subject to the following
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

package edu.rice.cs.drjava.model;

import java.awt.print.*;
import java.awt.Font;
import java.awt.Color;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.UndoableEditListener;
import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import java.util.Vector;
import java.util.Enumeration;
import edu.rice.cs.util.*;
import edu.rice.cs.util.docnavigation.*;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.print.*;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.project.*;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.cache.*;

/**
 * Handles the bulk of DrJava's program logic.
 * The UI components interface with the GlobalModel through its public methods,
 * and GlobalModel responds via the GlobalModelListener interface.
 * This removes the dependency on the UI for the logical flow of the program's
 * features.  With the current implementation, we can finally test the compile
 * functionality of DrJava, along with many other things.
 * 
 * This class is now abstract because additional funtionality to support document
 * navigation in the context of projects was added in DefaultSingleDisplayModel
 * @version $Id$
 */
public abstract class DefaultGlobalModel implements GlobalModel, OptionConstants,
    DocumentIterator {

  /**
   * a document cache that manages how many unmodified documents are open at once
   */
  private DocumentCache _cache;  
  
  static final String DOCUMENT_OUT_OF_SYNC_MSG =
    "Current document is out of sync with the Interactions Pane and should be recompiled!\n";

  static final String CLASSPATH_OUT_OF_SYNC_MSG =
    "Interactions Pane is out of sync with the current classpath and should be reset!\n";

  // ----- FIELDS -----

  /**
   * a list of files that are auxiliary files to the currently open project
   */
  private LinkedList<File> _auxiliaryFiles = new LinkedList<File>();
  
  /**
   * adds a document to the list of auxiliary files
   */
  public void addAuxiliaryFile(OpenDefinitionsDocument doc){
    if(!doc.isUntitled() && !doc.isProjectFile()){
      File f;
      try{
        f = doc.getFile();
      }catch(FileMovedException fme){
        f = fme.getFile();
      }
      _auxiliaryFiles.add(f);
      setProjectChanged(true);
    }
  }
  
  /**
   * removes a document from the list of auxiliary files
   */
  public void removeAuxiliaryFile(OpenDefinitionsDocument doc){
    File file;
    try{
      file = doc.getFile();
    }catch(FileMovedException fme){
      file = fme.getFile();
    }
    LinkedList<File> newAuxiliaryFiles = new LinkedList<File>();  
    for(File f: _auxiliaryFiles){
      try{
        if(!f.getCanonicalPath().equals(file.getCanonicalPath())){
          newAuxiliaryFiles.add(f);
        }
      }catch(IOException e){
        // noop
      }
    }
    _auxiliaryFiles = newAuxiliaryFiles;
    setProjectChanged(true);
  }

  /**
   * Keeps track of all listeners to the model, and has the ability
   * to notify them of some event.  Originally used a Command Pattern style,
   * but this has been replaced by having EN directly implement all listener
   * interfaces it supports.  Set in constructor so that subclasses can install
   * their own notifier with additional methods.
   */
  final GlobalEventNotifier _notifier = new GlobalEventNotifier();

  // ---- Definitions fields ----

  /**
   * Factory for new definitions documents and views.
   */
  private final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit(_notifier);

  /**
   * Collection for storing all OpenDefinitionsDocuments.
   */
  protected final BidirectionalHashMap<INavigatorItem, OpenDefinitionsDocument> _documentsRepos =
    new OrderedBidirectionalHashMap<INavigatorItem, OpenDefinitionsDocument>();


  // ---- Interpreter fields ----

  /**
   * RMI interface to the Interactions JVM.
   * Package private so we can access it from test cases.
   */
  final MainJVM _interpreterControl = new MainJVM();

  /**
   * Interface between the InteractionsDocument and the JavaInterpreter,
   * which runs in a separate JVM.
   */
  protected DefaultInteractionsModel _interactionsModel;
  
  /**
   * Denotes whether the model is currently trying to close all
   * documents, and thus that a new one should not be created, and whether or not to update the navigator
   */
  protected boolean _isClosingAllDocs;
    
  protected InteractionsListener _interactionsListener = new InteractionsListener(){
    public void interactionStarted(){    }
    
    public void interactionEnded(){    }
    
    public void interactionErrorOccurred(int offset, int length){    }
    
    public void interpreterResetting(){    }
    
    public void interpreterReady(){  
      if(_state.getBuildDirectory() != null){
//        System.out.println("adding for reset: " + _state.getBuildDirectory().getAbsolutePath());
        _interpreterControl.addClassPath(_state.getBuildDirectory().getAbsolutePath());
      }
    }
    
    public void interpreterResetFailed(Throwable t){    }
    
    public void interpreterExited(int status){    }
    
    public void interpreterChanged(boolean inProgress){    }
    
    public void interactionIncomplete(){    }
  };

  private CompilerListener _clearInteractionsListener =
    new CompilerListener() {
      public void compileStarted() {}

      public void compileEnded() {
        // Only clear interactions if there were no errors
        if (((_compilerModel.getNumErrors() == 0) ||
             (_compilerModel.getCompilerErrorModel().hasOnlyWarnings()))
              // reset even when the interpreter is not used.
              //&& _interactionsModel.interpreterUsed()
              && _resetAfterCompile) {
          resetInteractions();
        }
      }

      public void saveBeforeCompile() {}
    };


  // ---- Compiler Fields ----

  /**
   * CompilerModel manages all compiler functionality.
   */
  private final CompilerModel _compilerModel = new DefaultCompilerModel(this);

  /**
   * Whether or not to reset the interactions JVM after compiling.
   * Should only be false in test cases.
   */
  private boolean _resetAfterCompile = true;
  

  // ---- JUnit Fields ----

  /**
   * JUnitModel manages all JUnit functionality.
   * TODO: remove dependence on GlobalModel
   */
  private final DefaultJUnitModel _junitModel =
    new DefaultJUnitModel(this, _interpreterControl, _compilerModel, this);


  // ---- Javadoc Fields ----

  /**
   * Manages all Javadoc functionality.
   */
  protected JavadocModel _javadocModel = new DefaultJavadocModel(this);

  // ---- Debugger Fields ----

  /**
   * Interface to the integrated debugger.  If the JPDA classes are not
   * available, this is set NoDebuggerAvailable.ONLY.
   */
  private Debugger _debugger = NoDebuggerAvailable.ONLY;

  /**
   * Port used by the debugger to connect to the Interactions JVM.
   * Uniquely created in getDebugPort().
   */
//  private int _debugPort = -1;


  // ---- Input/Output Document Fields ----

  /**
   * The document adapter used in the Interactions model.
   */
  private final InteractionsDocumentAdapter _interactionsDocAdapter;

  /**
   * The document used to display System.out and System.err,
   * and to read from System.in.
   */
  private final ConsoleDocument _consoleDoc;

  /**
   * The document adapter used in the console document.
   */
  private final InteractionsDocumentAdapter _consoleDocAdapter;

  /**
   * A lock object to prevent print calls to System.out or System.err
   * from flooding the JVM, ensuring the UI remains responsive.
   */
  private final Object _systemWriterLock = new Object();

  /**
   * Number of milliseconds to wait after each println, to prevent
   * the JVM from being flooded with print calls.
   * TODO: why is this here, and why is it public?
   */
  public static final int WRITE_DELAY = 50;

  /**
   * A PageFormat object for printing to paper.
   */
  private PageFormat _pageFormat = new PageFormat();

  /**
   * Listens for requests from System.in.
   */
  private InputListener _inputListener;

  
  /**
   * the abstract container which contains views of open documents and allows user to 
   * navigate document focus among this collection of open documents
   */
  protected IDocumentNavigator _documentNavigator = AWTContainerNavigatorFactory.Singleton.makeListNavigator();
  

  // ----- CONSTRUCTORS -----

  /**
   * Constructs a new GlobalModel.
   * Creates a new MainJVM and starts its Interpreter JVM.
   */
  public DefaultGlobalModel() {

    _cache = new DocumentCache();

    _interactionsDocAdapter = new InteractionsDocumentAdapter();
    _interactionsModel =
      new DefaultInteractionsModel(this, _interpreterControl,
                                   _interactionsDocAdapter);
    _interactionsModel.addListener(_interactionsListener);

    _interpreterControl.setInteractionsModel(_interactionsModel);
    _interpreterControl.setJUnitModel(_junitModel);

    _interpreterControl.setOptionArgs(DrJava.getConfig().getSetting(JVM_ARGS));
    DrJava.getConfig().addOptionListener(JVM_ARGS, new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oe) {
        _interpreterControl.setOptionArgs(oe.value);
      }
    });

    _consoleDocAdapter = new InteractionsDocumentAdapter();
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);

    _createDebugger();

    _registerOptionListeners();

    // Chain notifiers so that all events also go to GlobalModelListeners.
    _interactionsModel.addListener(_notifier);
    _compilerModel.addListener(_notifier);
    _junitModel.addListener(_notifier);
    _javadocModel.addListener(_notifier);

    // Listen to compiler to clear interactions appropriately.
    // XXX: The tests need this to be registered after _notifier, sadly.
    //      This is obnoxiously order-dependent, but it works for now.
    _compilerModel.addListener(_clearInteractionsListener);

    // Perhaps do this in another thread to allow startup to continue...
    _interpreterControl.startInterpreterJVM();
    
    setFileGroupingState(_makeFlatFileGroupingState());
    _notifier.projectRunnableChanged();
  }

  /**
   * Constructor.  Initializes all the documents, but take the interpreter
   * from the given previous model. This is used only for test cases,
   * since there is substantial overhead to initializing the interpreter.
   *
   * Reset the interpreter for good measure since it's an old one.
   * (TODO: I'm not sure this is still correct or effective any more,
   *   now that we're always restarting the JVM.  Needs to be looked at...)
   * This has been commented out because it is never used and has bitrot.
   */
//  public DefaultGlobalModel(DefaultGlobalModel other) {
//    this(other._interpreterControl);
//
//    _interpreterControl.reset();
//    try {
//      _interactionsModel.setDebugPort(other.getDebugPort());
//      _interactionsModel.setWaitingForFirstInterpreter(false);
//    }
//    catch (IOException ioe) {
//      // Other model should already have a port, or it should be -1.
//      //  We shouldn't ever get an IOException here.
//      throw new UnexpectedException(ioe);
//    }
//  }

  /**
   * Constructs a new GlobalModel with the given MainJVM to act as an
   * RMI interface to the Interpreter JVM.  Does not attempt to start
   * the InterpreterJVM.
   * TODO: This has been commented out because it is never used and has bitrot.
   * @param control RMI interface to the Interpreter JVM
   */
//  public DefaultGlobalModel(MainJVM control) {
//    _interpreterControl = control;
//    _interactionsDocAdapter = new InteractionsDocumentAdapter();
//    _interactionsModel =
//      new DefaultInteractionsModel(this, control, _interactionsDocAdapter);
//    _interpreterControl.setInteractionsModel(_interactionsModel);
//    _interpreterControl.setJUnitModel(this);  // to be replaced by JUnitModel
//
//    _consoleDocAdapter = new InteractionsDocumentAdapter();
//    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);
//
//    _inputListener = NoInputListener.ONLY;
//
//    _createDebugger();
//
//    _registerOptionListeners();
//  }

  
  // ----- returns a source root given a package and filename ----
  protected File getSourceRoot(String packageName, File sourceFile) throws InvalidPackageException{
    if (packageName.equals("")) {
      return sourceFile.getParentFile();
    }
    
    Stack<String> packageStack = new Stack<String>();
    int dotIndex = packageName.indexOf('.');
    int curPartBegins = 0;
    
    while (dotIndex != -1) {
      packageStack.push(packageName.substring(curPartBegins, dotIndex));
      curPartBegins = dotIndex + 1;
      dotIndex = packageName.indexOf('.', dotIndex + 1);
    }
    
    // Now add the last package component
    packageStack.push(packageName.substring(curPartBegins));
    
    // Must use the canonical path, in case there are dots in the path
    //  (which will conflict with the package name)
    try {
      File parentDir = sourceFile.getCanonicalFile();
      while (!packageStack.empty()) {
        String part = packageStack.pop();
        parentDir = parentDir.getParentFile();
        
        if (parentDir == null) {
          throw new RuntimeException("parent dir is null?!");
        }
        
        // Make sure the package piece matches the directory name
        if (! part.equals(parentDir.getName())) {
          String msg = "The source file " + sourceFile.getAbsolutePath() +
            " is in the wrong directory or in the wrong package. " +
            "The directory name " + parentDir.getName() +
            " does not match the package component " + part + ".";
          
          throw new InvalidPackageException(-1, msg);
        }
      }
      
      // OK, now parentDir points to the directory of the first component of the
      // package name. The parent of that is the root.
      parentDir = parentDir.getParentFile();
      if (parentDir == null) {
        throw new RuntimeException("parent dir of first component is null?!");
      }
      
      return parentDir;
    }
    catch (IOException ioe) {
      String msg = "Could not locate directory of the source file: " + ioe;
      throw new InvalidPackageException(-1, msg);
    }
  }
  
  // ----- INTERACTIONS -----
  public void enableSecurityManager(){
    edu.rice.cs.drjava.DrJava.enableSecurityManager();
    try{
      _interpreterControl.enableSecurityManager();
    }catch(RemoteException e){
      // couldn't enable security on the slave...
    }
  }
  
  public void disableSecurityManager(){
    edu.rice.cs.drjava.DrJava.disableSecurityManager();
    try{
      _interpreterControl.disableSecurityManager();
    }catch(RemoteException e){
      // couldn't enable security on the slave...
    }
  }
  
  
  // ----- STATE -----
  protected FileGroupingState _state;
  /**
   * Delegates the compileAll command to the _state, a FileGroupingState
   */
  public void compileAll() throws IOException{
    _state.compileAll();
  }
  
  /**
   * @param state the new file grouping state that will handle
   * project specific properties such as the build directory.
   */
  public void setFileGroupingState(FileGroupingState state){
    _state = state;
    _notifier.projectRunnableChanged();
    _notifier.projectBuildDirChanged();
    _notifier.projectModified();
  }
  
  public FileGroupingState getFileGroupingState(){
    return _state;
  }
  
  /**
   * Notifies the project state that the project has been changed
   */
  public void setProjectChanged(boolean changed) {
    _state.setProjectChanged(changed);
    _notifier.projectModified();
  }
  
  /**
   * Returns true if the project state has been changed
   */
  public boolean isProjectChanged() {
    return _state.isProjectChanged();
  }
    
  /**
   * @return true if the model has a project open, false otherwise.
   */
  public boolean isProjectActive(){
    return _state.isProjectActive();
  }
  
  /**
   * @return the file that points to the current project file. Null if not currently in project view
   */
  public File getProjectFile() {
    return _state.getProjectFile();
  }
  
  /**
   * Return all files currently saved as source files in the project file
   * If not in project mode, returns null
   */
  public File[] getProjectFiles() {
    return _state.getProjectFiles();
  }
  
  /**
   * Returns true the given file is in the current project file.
   */
  public boolean isProjectFile(File f) {
    return _state.isProjectFile(f);
  }
   
  /**
   * a file is in the project if the source root is the same as the
   * project root. this means that project files must be saved at the
   * source root. (we query the model through the model's state)
   */
  public boolean isInProjectPath(OpenDefinitionsDocument doc) {
    return _state.isInProjectPath(doc);
  }
  
   /**
   * Sets the class with the project's main method
   */
  public void setJarMainClass(File f) {
    _state.setJarMainClass(f);
    _notifier.projectRunnableChanged();
    setProjectChanged(true);
  }
  
  /**
   * @return the class with the project's main method
   */
  public File getMainClass(){
    return _state.getMainClass();
  }
  
  public void junitAll(){
    _state.junitAll();
  }

   /**
   * Sets the class with the project's main method
   */
  public void setBuildDirectory(File f){
    _state.setBuildDirectory(f);
    if(f != null){
//      System.out.println("adding: " + f.getAbsolutePath());
      _interpreterControl.addClassPath(f.getAbsolutePath());
    }
    
//        InteractionsDocument iDoc = _interactionsModel.getDocument();
//        synchronized (_interpreterControl) {
//          iDoc.clearCurrentInput();
//          iDoc.insertBeforeLastPrompt(CLASSPATH_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
//        }
        
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
  }
  
  /**
   * @return the class with the project's main method
   */
  public File getBuildDirectory(){
    return _state.getBuildDirectory();
  }
  
  public void cleanBuildDirectory() throws FileMovedException, IOException{
    _state.cleanBuildDirectory();
  }

  public FileGroupingState _makeProjectFileGroupingState(final File jarMainClass, final File buildDir, final File projectFile, final File[] projectFiles) { 
    return new FileGroupingState(){
      
      private File _builtDir = buildDir;
      
      private File _mainFile = jarMainClass;
      
      private boolean _isProjectChanged = false;
      
      private ArrayList<File> _auxFiles = new ArrayList<File>();
      
      public boolean isProjectActive(){
        return true;
      }
      
      public boolean isInProjectPath(OpenDefinitionsDocument doc){
        try {
          File projectRoot = projectFile.getParentFile();
          if(doc.isUntitled()) return false;
          String filePath = doc.getFile().getParentFile().getCanonicalPath() + File.separator;
          String projectPath = projectRoot.getCanonicalPath() + File.separator;
          return (filePath.startsWith(projectPath));
        }
        catch(IOException e) {
          System.out.println(e);
          return false;
        }
      }
      /**
       * @return the absolute path to the project file
       */
      public File getProjectFile() {
        return projectFile;
      }
      
      public boolean isProjectFile(File f) {
        String path;
        
        if (f == null) return false;
        
        try{
          path = f.getCanonicalPath();
        }
        catch(IOException ioe) {
          return false;
        }
        
        for(File file : projectFiles) {
          try {
            if(file.getCanonicalPath().equals(path))
              return true;
          }
          catch(IOException ioe) {
            //continue
          }
        }
        return false;
      }
      
      public File[] getProjectFiles() {
        return projectFiles;
      }
      
      public File getBuildDirectory(){
        return _builtDir;
      }
      
      public void setBuildDirectory(File f) {
        _builtDir = f;
      }
      
      public File getMainClass(){
        return _mainFile;
      }
      
      public void setJarMainClass(File f) {
        _mainFile = f;
      }
      
      public boolean isProjectChanged() {
        return _isProjectChanged;
      }
      
      public void setProjectChanged(boolean changed) {
        _isProjectChanged = changed;
      }
      
      public boolean isAuxiliaryFile(File f){
        String path;
        
        if (f == null) return false;
        
        try{
          path = f.getCanonicalPath();
        }
        catch(IOException ioe) {
          return false;
        }
        
        for(File file : _auxiliaryFiles) {
          try {
            if(file.getCanonicalPath().equals(path))
              return true;
          }
          catch(IOException ioe) {
            //continue
          }
        }
        return false;
      }
      
      public void cleanBuildDirectory() throws FileMovedException, IOException{
        File dir = this.getBuildDirectory();
        cleanHelper(dir);
        if(!dir.exists()){
          dir.mkdirs();
        }
      }
      
      private void cleanHelper(File f){
        if(f.isDirectory()){
          File fs[] = f.listFiles(new FilenameFilter(){
            public boolean accept(File parent, String name){
              return new File(parent, name).isDirectory() || name.endsWith(".class");
            }
          });
          for(File kid: fs){
            cleanHelper(kid);
          }
          if(f.listFiles().length == 0){
            f.delete();
          }
        }else if(f.getName().endsWith(".class")){
          f.delete();
        }
      }
      
      
      /**
       * returns the name of the package from a fully qualified classname
       */
      private String getPackageName(String classname){
        int index = classname.lastIndexOf(".");
        if(index != -1){
          return classname.substring(0, index);
        }else{
          return "";
        }
      }
      
      // ----- FIND ALL DEFINED CLASSES IN FOLDER ---
      public void compileAll() throws IOException{
        File dir = getProjectFile().getParentFile();
        final ArrayList<File> files = FileOps.getFilesInDir(dir, true, new FileFilter(){
          public boolean accept(File pathname){
            return pathname.isDirectory() || 
              pathname.getPath().toLowerCase().endsWith(".java") ||
              pathname.getPath().toLowerCase().endsWith(".dj0") ||
              pathname.getPath().toLowerCase().endsWith(".dj1") ||
              pathname.getPath().toLowerCase().endsWith(".dj2");
          }
        });
        ClassAndInterfaceFinder finder;
        // the list of files to compile
        List<File>lof = new LinkedList<File>();
        // the list of sourceroots for the files
        List<File>los = new LinkedList<File>();
        for(File f: files){
          finder = new ClassAndInterfaceFinder(f);
          String classname = finder.getClassName();
          String packagename = getPackageName(classname);
          try{
            File sourceroot = getSourceRoot(packagename, f);
            if(!los.contains(sourceroot)){
              los.add(sourceroot);
            }
            lof.add(f);
          }catch(InvalidPackageException e){
            // don't add the file or root
          }
        }

        String[] exts = new String[]{".java", ".dj0", ".dj1", ".dj2"};
        List<OpenDefinitionsDocument> lod = getDefinitionsDocuments();
        for(OpenDefinitionsDocument d: lod){
          if(d.isAuxiliaryFile()){
            try{
              File f;
              File sourceRoot = d.getSourceRoot();
              try{
                f = d.getFile();
                for(String ext: exts){
                  if(f.getName().endsWith(ext)){
                    lof.add(f);
                    los.add(sourceRoot);
                  }
                }
              }catch(FileMovedException fme){
                // the file's not on disk, but send it in anyways
                f = fme.getFile();
                lof.add(f);
                los.add(sourceRoot);
              }catch(IllegalStateException e){
                // it doesn't have a file, so don't try and test it...
              }
            }catch(InvalidPackageException e){
              // don't add it if we don't have a valid packagename
            }
          }
        }
        getCompilerModel().compileAll(los, lof);
      }

      // ----- FIND ALL DEFINED CLASSES IN FOLDER ---
      public void junitAll(){
        File dir = getProjectFile().getParentFile();
        ArrayList<String> classNames = new ArrayList<String>();
        final ArrayList<File> files = FileOps.getFilesInDir(dir, true, new FileFilter(){
          public boolean accept(File pathname){
            return pathname.isDirectory() || 
              pathname.getPath().toLowerCase().endsWith(".java") ||
              pathname.getPath().toLowerCase().endsWith(".dj0") ||
              pathname.getPath().toLowerCase().endsWith(".dj1") ||
              pathname.getPath().toLowerCase().endsWith(".dj2");
          }
        });
        ClassFinder finder;
        List<String> los = new LinkedList<String>();
        List<File> lof = new LinkedList<File>();
        for(File f: files){
          finder = new ClassFinder(f);
          String classname = finder.getClassName();
          if(classname.length() > 0){
            los.add(classname);
            lof.add(f);
          }
        }
        List<OpenDefinitionsDocument> lod = getDefinitionsDocuments();
        for(OpenDefinitionsDocument d: lod){
          if(d.isAuxiliaryFile()){
            try{
              File f;
              String classname = d.getQualifiedClassName();
              try{
                f = d.getFile();
                lof.add(f);
                los.add(classname);
              }catch(FileMovedException fme){
                // the file's not on disk, but send it in anyways
                f = fme.getFile();
                lof.add(f);
                los.add(classname);
              }catch(IllegalStateException e){
                // it doesn't have a file, so don't try and test it...
              }
            }catch(ClassNameNotFoundException e){
              // don't add it if we don't have a classname
            }
          }
        }
        getJUnitModel().junitAll(los, lof);
      }
    
    };
  }
  
  public FileGroupingState _makeFlatFileGroupingState() {
    return new FileGroupingState(){
      public File getBuildDirectory(){
        return null;
      }
      
      public boolean isProjectActive(){
        return false;
      }
      
      public boolean isInProjectPath(OpenDefinitionsDocument doc){
        return false;
      }
      public File getProjectFile() {
        return null;
      }
      public void setBuildDirectory(File f) {
        // noop, this action is not applicable for flat file
      }
      public File[] getProjectFiles() {
        return null;
      }
      public boolean isProjectFile(File f) {
        return false;
      }
      
      public File getMainClass(){
        return null;
      }
      
      public void setJarMainClass(File f) {
        // noop, this action is not applicable for flat file
      }

      public boolean isProjectChanged() {
        return false;
      }
      
      public void setProjectChanged(boolean changed) {
        //Do nothing    
      }
      
      public boolean isAuxiliaryFile(File f){
        return false;
      }

      public void compileAll() throws IOException{
        getCompilerModel().compileAll();
      }
      
      public void junitAll(){
        getJUnitModel().junitAll();
      }
      public void cleanBuildDirectory() throws FileMovedException, IOException{
        System.out.println("not cleaning");
      }
    };
  }
  
  // ----- METHODS -----
  /**
   * Add a listener to this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void addListener(GlobalModelListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Remove a listener from this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void removeListener(GlobalModelListener listener) {
    _notifier.removeListener(listener);
  }

  // getter methods for the private fields

  public DefinitionsEditorKit getEditorKit() {
    return _editorKit;
  }

  /**
   * @return the interactions model.
   */
  public DefaultInteractionsModel getInteractionsModel() {
    return _interactionsModel;
  }

  /**
   * @return InteractionsDocumentAdapter in use by the InteractionsDocument.
   */
  public InteractionsDocumentAdapter getSwingInteractionsDocument() {
    return _interactionsDocAdapter;
  }

  public InteractionsDocument getInteractionsDocument() {
    return _interactionsModel.getDocument();
  }

  public ConsoleDocument getConsoleDocument() {
    return _consoleDoc;
  }

  public InteractionsDocumentAdapter getSwingConsoleDocument() {
    return _consoleDocAdapter;
  }

  public PageFormat getPageFormat() {
    return _pageFormat;
  }

  public void setPageFormat(PageFormat format) {
    _pageFormat = format;
  }

  /**
   * Gets the CompilerModel, which provides all methods relating to compilers.
   */
  public CompilerModel getCompilerModel() {
    return _compilerModel;
  }

  /**
   * Gets the JUnitModel, which provides all methods relating to JUnit testing.
   */
  public JUnitModel getJUnitModel() {
    return _junitModel;
  }

  /**
   * Gets the JavadocModel, which provides all methods relating to Javadoc.
   */
  public JavadocModel getJavadocModel() {
    return _javadocModel;
  }
  
  public IDocumentNavigator getDocumentNavigator() {
    return _documentNavigator;
  }
  
  public void setDocumentNavigator(IDocumentNavigator newnav) {
    _documentNavigator = newnav;
  }
  
  private INavigatorItem makeIDocFromODD(final OpenDefinitionsDocument oddoc) {
    final INavigatorItem idoc  = new INavigatorItem() {
      public String getName() {
        try{
          return oddoc.getFilename();
        }catch(NoSuchDocumentException e){
          return "** error **";
        }
      }
      public boolean equals(Object obj){
        try {
          INavigatorItem castidoc = (INavigatorItem)obj;
          return this == castidoc;
        }
        catch(ClassCastException e) {
          return oddoc.equals(obj);
        }catch(NoSuchDocumentException e){
          return false;
        }
      }
      public String toString() {
        try{
          if(oddoc.isModifiedSinceSave()){
            return oddoc.getFilename() + " *";
          }
          else{
            return oddoc.getFilename() + "  ";
          }
        }catch(NoSuchDocumentException e){
          return "** error **";
        }
      }
    };
    
    return idoc;
  }

  /**
   * Creates a new definitions document and adds it to the list.
   * @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    final OpenDefinitionsDocument oddoc = _createOpenDefinitionsDocument();
    INavigatorItem idoc = makeIDocFromODD(oddoc);
    oddoc.setFile(null);
    _documentsRepos.put(idoc, oddoc);
    _documentNavigator.addDocument(idoc);
    _notifier.newFileCreated(oddoc);
    return oddoc;
  }

  /**
   * Creates a new junit test case.
   * @param name the name of the new test case
   * @param makeSetUp true iff an empty setUp() method should be included
   * @param makeTearDown true iff an empty tearDown() method should be included
   * @return the new open test case
   */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    boolean elementary = (DrJava.getConfig().getSetting(LANGUAGE_LEVEL) == 1);
    
    StringBuffer buf = new StringBuffer();
    if(! elementary) buf.append("import junit.framework.TestCase;\n\n");
    buf.append("/**\n");
    buf.append("* A JUnit test case class.\n");
    buf.append("* Every method starting with the word \"test\" will be called when running\n");
    buf.append("* the test with JUnit.\n");
    buf.append("*/\n");
    if(! elementary) buf.append("public ");
    buf.append("class ");
    buf.append(name);
    buf.append(" extends TestCase {\n\n");
    if (makeSetUp) {
      buf.append("/**\n");
      buf.append("* This method is called before each test method, to perform any common\n");
      buf.append("* setup if necessary.\n");
      buf.append("*/\n");
      if(! elementary) buf.append("public ");
      buf.append("void setUp() {\n}\n\n");
    }
    if (makeTearDown) {
      buf.append("/**\n");
      buf.append("* This method is called after each test method, to perform any common\n");
      buf.append("* clean-up if necessary.\n");
      buf.append("*/\n");
      if(! elementary) buf.append("public ");
      buf.append("void tearDown() {\n}\n\n");
    }
    buf.append("/**\n");
    buf.append("* A test method.\n");
    buf.append("* (Replace \"X\" with a name describing the test.  You may write as\n");
    buf.append("* many \"testSomething\" methods in this class as you wish, and each\n");
    buf.append("* one will be called when running JUnit over this class.)\n");
    buf.append("*/\n");
    if(! elementary) buf.append("public ");
    buf.append("void testX() {\n}\n\n");
    buf.append("}\n");
    String test = buf.toString();

    OpenDefinitionsDocument openDoc = newFile();
    try {
      openDoc.insertString(0, test, null);
      openDoc.indentLines(0, test.length());
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    return openDoc;
  }

  /**
   * This is for use by test cases only.
   */
  public DocumentCache getDocumentCache() {
    return _cache;
  }
  
  //---------------------- Specified by ILoadDocuments ----------------------//

  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return a canonical path, as this method makes
   * it canonical.  This is necessary to ensure proper package detection.
   * (Also see bug 774896 and 707734)
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    // This code is duplicated in MainFrame._setCurrentDirectory(File) for safety.
    final File file = (com.getFiles())[0].getCanonicalFile();
    OpenDefinitionsDocument odd = _openFile(file);
    // Make sure this is on the classpath
    try {
      File classpath = odd.getSourceRoot();
      _interactionsModel.addToClassPath(classpath.getAbsolutePath());
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }

    return odd;
  }
  
  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    final File[] files = com.getFiles();
    if (files == null) {
      throw new IOException("No Files returned from FileSelector");
    }
    OpenDefinitionsDocument doc = _openFiles(files);
    return doc;
  }
  
  // if set to true, and uncommented, the definitions document will
  // print out a small stack trace every time getDocument() is called

//    static boolean SHOW_GETDOC = false; 

  /**
   * opens all the files in the list, and notifies about the last file opened
   */
  private OpenDefinitionsDocument _openFiles(File[] files) 
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    AlreadyOpenException storedAOE = null;
    OpenDefinitionsDocument retDoc = null;
    
//        SHOW_GETDOC = true;
        
    LinkedList<File> filesNotFound = new LinkedList<File>();
    final LinkedList<OpenDefinitionsDocument> filesOpened = new LinkedList<OpenDefinitionsDocument>();
    for (File f: files) {
      if (f == null) {
        throw new IOException("File name returned from FileSelector is null");
      }
      try {
        //always return last opened Doc
        retDoc = _rawOpenFile(f.getAbsoluteFile());
        filesOpened.add(retDoc);
      }
      catch (AlreadyOpenException aoe) {
        retDoc = aoe.getOpenDocument();
        //Remember the first AOE
        if (storedAOE == null) {
          storedAOE = aoe;
        }
      }catch(FileNotFoundException e){
        filesNotFound.add(f);
      }
    }
    
    for(final OpenDefinitionsDocument d: filesOpened){
      addDocToNavigator(d);
      addDocToClasspath(d);
      _notifier.fileOpened(d);
    }
    
    
    
//        SHOW_GETDOC = false;
    for(File f: filesNotFound){
      _notifier.fileNotFound(f);
    }
    
    if (storedAOE != null) {
      throw storedAOE;
    }

    if (retDoc != null) {
      return retDoc;
    }
    else {
      //if we didn't open any files, then it's just as if they cancelled it...
      throw new OperationCanceledException();
    }
  }
  
  
  //----------------------- End ILoadDocuments Methods -----------------------//

  /**
   * Saves all open files, prompting for names if necessary.
   * When prompting (ie, untitled document), set that document as active.
   * @param com a selector that picks the file name, used for each
   * @exception IOException
   */
  public void saveAllFiles(FileSaveSelector com) throws IOException {
    
    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext())
    {
      OpenDefinitionsDocument doc = odds.next();
      aboutToSaveFromSaveAll(doc);
      doc.saveFile(com);
    }
  }
  
  /**
   * Writes the project file to disk
   * @param filename where to save the project
   */
  public void saveProject(String filename, Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> info) 
    throws IOException {
    
//    String base = filename.substring(0, filename.lastIndexOf(File.separator) + 1);
    ProjectFileBuilder builder = new ProjectFileBuilder(filename);
    
    // add opendefinitionsdocument
    Vector<File> srcFileVector = new Vector<File>();
    Vector<File> auxFileVector = new Vector<File>();
    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext()){
      OpenDefinitionsDocument doc = odds.next();
      
      
      if (!doc.isUntitled() ) {
        // could not use doc.isInProjectPath because we may be in flat file view which returns false
        String projectPath = new File(filename).getParentFile().getCanonicalPath() + File.separator;
        String filePath = doc.getFile().getParentFile().getCanonicalPath() + File.separator;
        if (filePath.startsWith(projectPath)){
          DocumentInfoGetter g = info.get(doc);
          builder.addSourceFile(g);
          srcFileVector.add(g.getFile());
        }else if(doc.isAuxiliaryFile()){
          DocumentInfoGetter g = info.get(doc);
          builder.addAuxiliaryFile(g);
          auxFileVector.add(g.getFile());
        }
      }
    }

    // TODO: add all the auxiliary files to the builder.  The aux files should be in the info map too
    
    // add collapsed path info
    if (_documentNavigator instanceof JTreeSortNavigator) {
      String[] paths = ((JTreeSortNavigator)_documentNavigator).getCollapsedPaths();
      for (String s : paths) {
        builder.addCollapsedPath(s);
      }
    }
    
    // add classpath info
    Vector<File> currentclasspaths = DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH);
    for(int i = 0; i<currentclasspaths.size(); i++){
      builder.addClasspathFile(currentclasspaths.get(i));
    }
    
    // add build directory
    File f = getBuildDirectory();
    //System.out.println(f);
    if(f != null)
      builder.setBuildDirectory(f);
    
    // add jar main class
    File mainClass = getMainClass();
    //System.out.println(f);
    if(mainClass != null){
      builder.setMainClass(mainClass);
    }
    
    // write to disk
    builder.write();

  
    // set the state if all went well
    File[] srcFiles = srcFileVector.toArray(new File[0]);
    
    _auxiliaryFiles.clear();
    for(File file: auxFileVector){
      _auxiliaryFiles.add(file);
    }
    
    setFileGroupingState(_makeProjectFileGroupingState(mainClass, f, new File(filename), srcFiles));
  }

  /**
   * Parses out the given project file, sets up the state and other configurations
   * such as the Navigator and the classpath, and returns an array of files to open.
   * @param projectFile The project file to parse
   * @return an array of document's files to open
   */
  public File[] openProject(File projectFile) throws IOException, MalformedProjectFileException {
    final ProjectFileIR ir;
    final DocFile[] srcFiles;
    final DocFile[] auxFiles;
    
    //File projectRoot = projectFile.getParentFile();
    ir = ProjectFileParser.ONLY.parse(projectFile);
    srcFiles = ir.getSourceFiles();
    auxFiles = ir.getAuxiliaryFiles();
    String projfilepath = projectFile.getCanonicalPath();
    
    List<Pair<String, INavigatorItemFilter>> l = new LinkedList<Pair<String, INavigatorItemFilter>>();
    l.add(new Pair<String, INavigatorItemFilter>("[ Source Files ]", new INavigatorItemFilter(){
      public boolean accept(INavigatorItem n){
        OpenDefinitionsDocument d = DefaultGlobalModel.this.getODDGivenIDoc(n);
        return d.isInProjectPath();
      }
    }));

    l.add(new Pair<String, INavigatorItemFilter>("[ Auxiliary Files ]", new INavigatorItemFilter(){
      public boolean accept(INavigatorItem n){
        OpenDefinitionsDocument d = DefaultGlobalModel.this.getODDGivenIDoc(n);
        return d.isAuxiliaryFile();
      }
    }));
    
    l.add(new Pair<String, INavigatorItemFilter>("[ External Files ]", new INavigatorItemFilter(){
      public boolean accept(INavigatorItem n){
        OpenDefinitionsDocument d = DefaultGlobalModel.this.getODDGivenIDoc(n);
        return !(d.isProjectFile() || d.isAuxiliaryFile()) || d.isUntitled();
      }
    }));
    

    IDocumentNavigator newNav = 
      AWTContainerNavigatorFactory.Singleton.makeTreeNavigator(projfilepath, getDocumentNavigator(), l);
        
    setDocumentNavigator(newNav);
    
    File buildDir = ir.getBuildDirectory();
    File mainClass;
    mainClass = ir.getMainClass();
    
    _auxiliaryFiles.clear();
    for(File file: auxFiles){
      _auxiliaryFiles.add(file);
    }

    setFileGroupingState(_makeProjectFileGroupingState(mainClass, buildDir, projectFile, srcFiles));
    setBuildDirectory(buildDir);
    
    
    File[] projectclasspaths = ir.getClasspaths();
    Vector<File> currentclasspaths = DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH);
    for(int i = 0; i<projectclasspaths.length; i++){
      currentclasspaths.remove(projectclasspaths[i].getAbsoluteFile());
      currentclasspaths.add(projectclasspaths[i].getAbsoluteFile());
    }
    DrJava.getConfig().setSetting(OptionConstants.EXTRA_CLASSPATH, currentclasspaths);
    
    
    setProjectChanged(false);
    
    ArrayList<File> al = new ArrayList<File>();
    File active = null;
    for(DocFile f: srcFiles){
      File file = f;
      if(f.lastModified() > f.getSavedModDate()){
        file = new File(f.getPath());
      }
      if (f.isActive() && active == null) {
        active = file;
      }
      else {
        al.add(file);
      }
    }
    for(DocFile f: auxFiles){
      File file = f;
      if(f.lastModified() > f.getSavedModDate()){
        file = new File(f.getPath());
      }
      if (f.isActive() && active == null) {
        active = file;
      }
      else {
        al.add(file);
      }
    }
    if (active != null) al.add(active);
    
    List<OpenDefinitionsDocument> nonProjDocs = getNonProjectDocuments();
    List<OpenDefinitionsDocument> projDocs = getProjectDocuments();
    File[] projectFiles = getProjectFiles();   
    
    
    // keep all nonproject files open.  External files in the previous project
    // may become project files in the new project and must be closed while external
    // files in the previous project that are still external to the new project 
    // should be kept open.
     
    //List<OpenDefinitionsDocument> docsToClose = new LinkedList<OpenDefinitionsDocument>();
    for(OpenDefinitionsDocument d: projDocs){
      if(d.isProjectFile()){
        closeFile(d);
      }else{
        try{
          INavigatorItem idoc = getIDocGivenODD(d);
          String path = fixPathForNavigator(d.getFile().getCanonicalPath());
          _documentNavigator.refreshDocument(idoc, path);
        }catch(IOException e){
          // noop
        }
      }
    }
    
    // call on the GUI to finish up by opening the files and making
    // necessary gui component changes
    final File[] filesToOpen = al.toArray(new File[0]);
    _notifier.projectOpened(projectFile, new FileOpenSelector(){
      public File[] getFiles() {
        return filesToOpen;
      }
    });
    
    if (_documentNavigator instanceof JTreeSortNavigator) {
      ((JTreeSortNavigator)_documentNavigator).collapsePaths(ir.getCollapsedPaths());
    }
    
    return srcFiles; // Unnecessarily returns src files in keeping with the previous interface.
  }
  
  /**
   * Performs any needed operations on the model before closing the
   * project and its files.  This is not responsible for actually
   * closing the files since that is handled in MainFrame._closeProject()
   */
  public void closeProject() {
    
    setDocumentNavigator(AWTContainerNavigatorFactory.Singleton.makeListNavigator(getDocumentNavigator()));
    setFileGroupingState(_makeFlatFileGroupingState());
    _interactionsModel.getDocument().insertBeforeLastPrompt(CLASSPATH_OUT_OF_SYNC_MSG, InteractionsDocument.SYSTEM_ERR_STYLE);
    _notifier.projectClosed();
  }
  
  /**
   * Does nothing in default model.
   * @param doc the document which is about to be saved by a save all
   *            command
   */
  public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {}

  /**
   * Closes an open definitions document, prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * @return true if the document was closed
   */
  public boolean closeFile(OpenDefinitionsDocument doc) {
//    System.err.println("closing " + doc);
    boolean canClose = doc.canAbandonFile();
    if (canClose) {
      return closeFileWithoutPrompt(doc);
    }
    return false;
  }
  
  
  /**
   * Closes an open definitions document, without prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * NOTE: This method should not be called unless it can be absolutely known that the document being closed
   * is not the active document. closeFile() is overridden in the SingleDisplayModel to ensure that a new active document is set,
   * but closeFileWithoutPrompt is not.
   * 
   * @return true if the document was closed
   */
  public boolean closeFileWithoutPrompt(OpenDefinitionsDocument doc) {
    final OpenDefinitionsDocument closedDoc = doc;
    // Only fire event if doc exists and was removed from list
    INavigatorItem idoc = _documentsRepos.removeKey(closedDoc);
    String name = idoc.toString();
    closedDoc.close();
    
    if (idoc != null) {
      _documentNavigator.removeDocument(idoc);
      _notifier.fileClosed(closedDoc);
      //        closedDoc.close();
      return true;
    }
    return false;
  }
     
  /**
   * Attempts to close all open documents.
   * @return true if all documents were closed
   */
  public boolean closeAllFiles() {
    boolean keepClosing = true;
    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while (odds.hasNext() &&  keepClosing) {
      OpenDefinitionsDocument next = odds.next();
      keepClosing = closeFile(next);
      odds = _documentsRepos.valuesIterator(); // call to closeFile can mutate Iterator, so generate a new "current" Iterator on each loop 
    }
    
    return keepClosing;
  }
  

  /**
   * Exits the program.
   * Only quits if all documents are successfully closed.
   */
  public void quit() {
    if (closeAllFiles()) {
      dispose();  // kills the interpreter

      if (DrJava.getSecurityManager() != null) {
        DrJava.getSecurityManager().exitVM(0);
      }
      else {
        // If we are being debugged by another copy of DrJava,
        //  then we have no security manager.  Just exit cleanly.
        System.exit(0);
      }

    }
  }

  /**
   * Prepares this model to be thrown away.  Never called in
   * practice outside of quit(); only used in tests.
   */
  public void dispose() {
    // Kill the interpreter
    _interpreterControl.killInterpreter(false);

    _notifier.removeAllListeners();
    _documentsRepos.clear();
    _documentNavigator.clear();
    
      
  }

  
  //----------------------- Specified by IGetDocuments -----------------------//

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException
  {
    // Check if this file is already open
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null) {
      // If not, open and return it
      final File f = file;

      // TODO: Is this class construction overhead really necessary?
      FileOpenSelector selector = new FileOpenSelector() {
        public File getFile() {
          return f;
        }

        public File[] getFiles() {
          return new File[] {f};
        }
      };
      try {
        doc = openFile(selector);
      }
      catch (AlreadyOpenException aoe) {
        doc = aoe.getOpenDocument();
      }
      catch (OperationCanceledException oce) {
        // Cannot happen, since we don't throw it in our selector
        throw new UnexpectedException(oce);
      }
    }
    return doc;
  }

  /**
   * Iterates over OpenDefinitionsDocuments, looking for this file.
   * TODO: This is not very efficient!
   */
  public boolean isAlreadyOpen(File file) {
    return (_getOpenDocument(file) != null);
  }

  /**
   * Simply returns a reference to our internal ListModel.
   * TODO: Protect this object from untrusted code!
   * @deprecated Use getDefinitionsDocuments().
   */
 // public IDocList getDefinitionsDocs() {
  //  return _documentsRepos;
  //}

  /**
   * Returns the OpenDefinitionsDocument corresponding to the document
   * passed in.
   * @param doc the searched for Document
   * @return its corresponding OpenDefinitionsDocument
   */
  public OpenDefinitionsDocument getODDForDocument(Document doc) {
    /**
     * this function needs to be phased out altogether.
     * the goal is for the OpenDefinitionsDocument to also function as its own Document,
     * so this function will be useless
     */
    if(doc instanceof OpenDefinitionsDocument){
      return (OpenDefinitionsDocument)doc;
    }
    if (! (doc instanceof DefinitionsDocument) ) {
      throw new IllegalStateException("Could not get the OpenDefinitionsDocument for Document: " + doc);
    }
    return ((DefinitionsDocument)doc).getOpenDefDoc();
  }

  /**
   * Gets a DocumentIterator to allow navigating through open Swing Documents.
   */
  public DocumentIterator getDocumentIterator() {
    return this;
  }

  /**
   * Given a Document, returns the Document corresponding to the next
   * OpenDefinitionsDocument in the document list.
   * @param doc the current Document
   * @return the next Document
   */
  public Document getNextDocument(Document doc) {
    try {
      Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
      INavigatorItem item = getIDocGivenODD(getODDForDocument(doc));
      INavigatorItem nextitem = _documentNavigator.getNext(item);
      if(nextitem == item){
        // we're at the end, so we need to rewind
        // and return doc at the very beginning
        int i = -1;
        do{
          item=nextitem;
          nextitem = _documentNavigator.getPrevious(item);
          i++;
        }while(nextitem != item);
        return getNextDocHelper(nextitem); //getODDGivenIDoc(nextitem);
      }else{
        return getNextDocHelper(nextitem); //getODDGivenIDoc(nextitem);
      }
    } catch(DocumentClosedException dce) {
      return getNextDocument(doc);
    }
  }
  
  private Document getNextDocHelper(INavigatorItem nextItem) {
    OpenDefinitionsDocument nextDoc = getODDGivenIDoc(nextItem);
    if(nextDoc.fileExists() || nextDoc.isUntitled()) 
      return nextDoc;
    
    Document toReturn = getNextDocument(nextDoc);
    if(nextDoc.verifyExists())
      return nextDoc;
    return toReturn;
  }

  /**
   * Given a Document, returns the Document corresponding to the previous
   * OpenDefinitionsDocument in the document list.
   * @param doc the current Document
   * @return the previous Document
   */
  public Document getPrevDocument(Document doc) {
    try {
      Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
      INavigatorItem item = getIDocGivenODD(getODDForDocument(doc));
      INavigatorItem nextitem = _documentNavigator.getPrevious(item);
      if(nextitem == item){
        // we're at the end, so we need to rewind
        // and return doc at the very beginning
        int i = -1;
        do{
          item=nextitem;
          nextitem = _documentNavigator.getNext(item);
          i++;
        }while(nextitem != item);
        return getPrevDocHelper(nextitem);//getODDGivenIDoc(nextitem);
      }else{
        return getPrevDocHelper(nextitem);//getODDGivenIDoc(nextitem);
      }
    } catch(DocumentClosedException dce) {
      return getPrevDocument(doc);
    }
  }
  
  private Document getPrevDocHelper(INavigatorItem nextItem) {
    OpenDefinitionsDocument nextDoc = getODDGivenIDoc(nextItem);
    if(nextDoc.fileExists() || nextDoc.isUntitled()) 
      return nextDoc;
    
    Document toReturn = getPrevDocument(nextDoc);
    if(nextDoc.verifyExists())
      return nextDoc;
    return toReturn;
  }

  public int getDocumentCount() {
    return _documentsRepos.size();
  }
  
  /*
  private int _getIndexOfDocument(Document doc) {
    int index = 0;
    Enumeration<OpenDefinitionsDocument> en = _documentsRepos.elements();
    while (en.hasMoreElements()) {
      if (doc == en.nextElement().getDocument()) {
        return index;
      }
      else {
        index++;
      }
    }
    return -1;
  }
  
  
  */

  /**
   * Returns a collection of all documents currently open for editing.
   * This is equivalent to the results of getDocumentForFile for the set
   * of all files for which isAlreadyOpen returns true.
   * @return a random-access List of the open definitions documents.
   */
  public List<OpenDefinitionsDocument> getDefinitionsDocuments() {
    ArrayList<OpenDefinitionsDocument> docs =
      new ArrayList<OpenDefinitionsDocument>(_documentsRepos.size());
    Iterator<OpenDefinitionsDocument> en = _documentsRepos.valuesIterator();

    while (en.hasNext()) {
      docs.add(en.next());
    }

    return docs;
  }
  
  /**
   * @return the size of the collection of OpenDefinitionsDocument's
   */
  public int getDefinitionsDocumentsSize() {
    return _documentsRepos.size();
  }
  
  public OpenDefinitionsDocument getODDGivenIDoc(INavigatorItem idoc)
  {
    return _documentsRepos.getValue(idoc);
  }
  
  public INavigatorItem getIDocGivenODD(OpenDefinitionsDocument odd)
  {
    return _documentsRepos.getKey(odd);
  }
  
  //----------------------- End IGetDocuments Methods -----------------------//
  
  /**
   * Set the indent tab size for all definitions documents.
   * @param indent the number of spaces to make per level of indent
   */
  void setDefinitionsIndent(int indent) {
    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext())
    {
      odds.next().setIndent(indent);
    }
  }

  /**
   * Clears and resets the interactions pane. Also clears the console
   * if the option is indicated (on by default).
   * Bug #576179 pointed out that this needs to end any threads that were
   * running in the interactions JVM, so we completely restart the JVM now.
   * Ideally, we'd like a way to end any running threads and cleanly reset
   * the interpreter (to speed up this method), but that might be too complex...
   * <p>
   * (Old approach:
   * First it makes sure it's in the right package given the
   * package specified by the definitions.  If it can't,
   * the package for the interactions becomes the defualt
   * top level. In either case, this method calls a helper
   * which fires the interactionsReset() event.)
   */
  public void resetInteractions() {
    if ((_debugger.isAvailable()) && (_debugger.isReady())) {
      _debugger.shutdown();
    }

    _interactionsModel.resetInterpreter();
    if (DrJava.getConfig().getSetting(OptionConstants.RESET_CLEAR_CONSOLE).booleanValue()) {
      resetConsole();
    }
    //_restoreInteractionsState();

    /* Old approach.  (Didn't kill leftover interactions threads)
    _interpreterControl.reset();
    _restoreInteractionsState();
    */
  }


  /**
   * Resets the console.
   * Fires consoleReset() event.
   */
  public void resetConsole() {
    _consoleDoc.reset();

    _notifier.consoleReset();
  }

  /**
   * Interprets the current given text at the prompt in the interactions
   * pane.
   */
  public void interpretCurrentInteraction() {
    _interactionsModel.interpretCurrentInteraction();
  }

  /**
   * Interprets the file selected in the FileOpenSelector. Assumes all strings
   * have no trailing whitespace. Interprets the array all at once so if there are
   * any errors, none of the statements after the first erroneous one are processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    _interactionsModel.loadHistory(selector);
  }

  /**
   * Loads the history/histories from the given selector.
   */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException
  {
    return _interactionsModel.loadHistoryAsScript(selector);
  }

  /**
   * Clears the interactions history
   */
  public void clearHistory() {
    _interactionsModel.getDocument().clearHistory();
  }

  /**
   * Saves the unedited version of the current history to a file
   * @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }

  /**
   * Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be
   * saved to file instead of the lines saved in the history. The saved
   * file will still include any tags needed to recognize it as a saved
   * interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion)
    throws IOException
  {
    _interactionsModel.getDocument().saveHistory(selector, editedVersion);
  }

  /**
   * Returns the entire history as a String with semicolons as needed
   */
  public String getHistoryAsStringWithSemicolons() {
    return _interactionsModel.getDocument().getHistoryAsStringWithSemicolons();
  }

  /**
   * Returns the entire history as a String
   */
  public String getHistoryAsString() {
    return _interactionsModel.getDocument().getHistoryAsString();
  }

  /**
   * Registers OptionListeners.  Factored out code from the two constructors
   */
  private void _registerOptionListeners(){
    // Listen to any relevant config options
    DrJava.getConfig().addOptionListener(EXTRA_CLASSPATH,
                                         new ExtraClasspathOptionListener());

    DrJava.getConfig().addOptionListener(BACKUP_FILES,
                                         new BackUpFileOptionListener());
    Boolean makeBackups = DrJava.getConfig().getSetting(BACKUP_FILES);
    FileOps.DefaultFileSaver.setBackupsEnabled(makeBackups.booleanValue());

    DrJava.getConfig().addOptionListener(ALLOW_PRIVATE_ACCESS,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        getInteractionsModel().setPrivateAccessible(oce.value.booleanValue());
      }
    });
  }

  /**
   * Appends a string to the given document using a particular attribute set.
   * Also waits for a small amount of time (WRITE_DELAY) to prevent any one
   * writer from flooding the model with print calls to the point that the
   * user interface could become unresponsive.
   * @param doc Document to append to
   * @param s String to append to the end of the document
   * @param style the style to print with
   */
  private void _docAppend(ConsoleDocument doc, String s, String style) {
    synchronized(_systemWriterLock) {
      try {
        doc.insertBeforeLastPrompt(s, style);

        // Wait to prevent being flooded with println's
        _systemWriterLock.wait(WRITE_DELAY);
      }
      catch (InterruptedException e) {
        // It's ok, we'll go ahead and resume
      }
    }
  }


  /**
   * Prints System.out to the DrJava console.
   */
  public void systemOutPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_OUT_STYLE);
  }

  /**
   * Prints System.err to the DrJava console.
   */
  public void systemErrPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_ERR_STYLE);
  }

  /** Called when the repl prints to System.out.
  public void replSystemOutPrint(String s) {
    systemOutPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE);
  } */

  /** Called when the repl prints to System.err.
  public void replSystemErrPrint(String s) {
    systemErrPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE);
  } */

  /** Called when the debugger wants to print a message.  Inserts a newline. */
  public void printDebugMessage(String s) {
    _interactionsModel.getDocument().
      insertBeforeLastPrompt(s + "\n", InteractionsDocument.DEBUGGER_STYLE);
  }


  /**
   * Blocks until the interpreter has registered.
   */
  public void waitForInterpreter() {
    _interpreterControl.ensureInterpreterConnected();
  }

  /**
   * Returns the current classpath in use by the Interpreter JVM.
   */
  public String getClasspathString() {
    return _interpreterControl.getClasspathString();
  }

  /**
   * Returns the current classpath in use by the Interpreter JVM.
   */
  public Vector<String> getClasspath() {
    return _interpreterControl.getClasspath();
  }

  /**
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set. On 8.7.02 changed the sourceRootSet such that
   * the directory DrJava was executed from is now after the sourceRoots
   * of the currently open documents in order that whatever version the user
   * is looking at corresponds to the class file the interactions window
   * uses.
   * TODO: Fix out of date comment, possibly remove this here?
   */
  public File[] getSourceRootSet() {
    LinkedList<File> roots = new LinkedList<File>();

    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext())
    {
      OpenDefinitionsDocument doc = odds.next();

      try {
        File root = doc.getSourceRoot();

        // Don't add duplicate Files, based on path
        if (!roots.contains(root)) {
          roots.add(root);
        }
      }
      catch (InvalidPackageException e) {
        // oh well, invalid package statement for this one
        // can't add it to roots
      }
    }

//    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
//
//    if (workDir == FileOption.NULL_FILE) {
//      workDir = new File( System.getProperty("user.dir"));
//    }
//    if (workDir.isFile() && workDir.getParent() != null) {
//      workDir = workDir.getParentFile();
//    }
//    roots.add(workDir);

    return roots.toArray(new File[0]);
  }

  /**
   * Return the name of the file, or "(untitled)" if no file exists.
   * Does not include the ".java" if it is present.
   * TODO: move to a static utility class?
   */
  public String getDisplayFilename(OpenDefinitionsDocument doc) {

    String filename = doc.getFilename();

    // Remove ".java" if at the end of name
    if (filename.endsWith(".java")) {
      int extIndex = filename.lastIndexOf(".java");
      if (extIndex > 0) {
        filename = filename.substring(0, extIndex);
      }
    }
    
    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      filename = filename + " *";
    }
    
    return filename;
  }


  
  /**
   * Return the absolute path of the file with the given index,
   * or "(untitled)" if no file exists.
   */
  public String getDisplayFullPath(int index) {
    OpenDefinitionsDocument doc =
      getDefinitionsDocuments().get(index);
    if (doc == null) {
      throw new RuntimeException(
        "Document not found with index " + index);
    }
    return GlobalModelNaming.getDisplayFullPath(doc);
  }

  
  
  
  
  /**
   * Sets whether or not the Interactions JVM will be reset after
   * a compilation succeeds.  This should ONLY be used in tests!
   * @param shouldReset Whether to reset after compiling
   */
  void setResetAfterCompile(boolean shouldReset) {
    _resetAfterCompile = shouldReset;
  }

  /**
   * Gets the Debugger used by DrJava.
   */
  public Debugger getDebugger() {
    return _debugger;
  }

  /**
   * Returns an available port number to use for debugging the interactions JVM.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    return _interactionsModel.getDebugPort();
  }

  /**
   * Checks if any open definitions documents have been modified
   * since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments() {
    boolean modified = false;
    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext())
    { 
      OpenDefinitionsDocument doc = odds.next();
      if (doc.isModifiedSinceSave()) {
        modified = true;
        break;
      }
    }
    return modified;
  }

  /**
   * 
   * Searches for a file with the given name on the current source roots and the
   * augmented classpath.
   * @param filename Name of the source file to look for
   * @return the file corresponding to the given name, or null if it cannot be found
   */
  public File getSourceFile(String filename) {
    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      File f = _getSourceFileFromPath(filename, sourceRoots[i]);
      if (f != null) {
        return f;
      }
    }
    Vector<File> sourcepath = DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
    return getSourceFileFromPaths(filename, sourcepath);
  }

  /**
   * Searches for a file with the given name on the provided paths.
   * Returns null if the file is not found.
   * @param filename Name of the source file to look for
   * @param paths An array of directories to search
   * @return the file if it is found, or null otherwise
   */
  public File getSourceFileFromPaths(String filename, Vector<File> paths) {
    File f;
    for (int i = 0; i < paths.size(); i++) {
      f = _getSourceFileFromPath(filename, paths.get(i));
      if (f != null) {
        return f;
      }
    }
    return null;
  }

  /**
   * Gets the file named filename from the given path, if it exists.
   * Returns null if it's not there.
   * @param filename the file to look for
   * @param path the path to look for it in
   * @return the file if it exists
   */
  private File _getSourceFileFromPath(String filename, File path) {
    String root = path.getAbsolutePath();
    File f = new File(root + System.getProperty("file.separator") + filename);
    return f.exists() ? f : null;
  }

  /**
   * Returns the document currently being tested (with JUnit) if there is
   * one, otherwise null.
   *
  public OpenDefinitionsDocument getDocBeingTested() {
  return _docBeingTested;
  }*/
  
  
  
  private static int ID_COUNTER = 0; // TAKE THIS OUT!!!  IT's DEBUG CODE
  // ---------- ConcreteOpenDefDoc inner class ----------

  /**
   * Inner class to handle operations on each of the open
   * DefinitionsDocuments by the GlobalModel.
   * <br><br>
   * This was at one time called the <code>DefinitionsDocumentHandler</code>
   * but was renamed (2004-Jun-8) to be more descriptive/intuitive.
   */
  private class ConcreteOpenDefDoc implements OpenDefinitionsDocument {
//    private DefinitionsDocument _doc;
    // TODO: Should these be document-specific?  They aren't used as such now.
//    private CompilerErrorModel _errorModel;
//    private JUnitErrorModel _junitErrorModel;
    
    private int _id;
    private DrJavaBook _book;
    private Vector<Breakpoint> _breakpoints;
    
    private boolean _modifiedSinceSave;
    
    private File _file;
    private long _timestamp;

    private String _packageName = null;
    
    private int _initVScroll;
    private int _initHScroll;
    private int _initSelStart;
    private int _initSelEnd;
    
    private DCacheAdapter _cacheAdapter;
    
//    boolean _shouldRun;
//    private GlobalModelListener _notifyListener = new DummySingleDisplayModelListener() {
//      public synchronized void interpreterReady() {
//        notify();
//      }
//      public synchronized void interperterResetting() {
//        notify();
//        _shouldRun = false;
//      }
//      public synchronized void interactionEnded() {
//        notify();
//      }
//    };

    /**
     * 
     * Constructor.  Initializes this handler's document.
     * @param doc DefinitionsDocument to manage
     */
    ConcreteOpenDefDoc(File f) throws IOException {
      if(f.exists()){
        _file = f;
        _timestamp = f.lastModified();
        init();
      }else{
        throw new FileNotFoundException("file " + f + " cannot be found");
      }
    }
    
    ConcreteOpenDefDoc(){
      _file = null;
      init();
    }
//    
//    /** 
//     * Currently, the package name is the only special info saved into
//     * the OpenDefinitionsDocument at this time.
//     */
//    ConcreteOpenDefDoc(File f, String packageName) throws IOException{
//      this(f);
//      _packageName = packageName;
//    }
    
    public void init(){
      _id = ID_COUNTER++;
      //      _doc = doc;
      try{
        //System.out.println("about to make reconstructor " + this);
        DDReconstructor ddr = makeReconstructor();
        //System.out.println("finished making reconstructor " + this);
        _cacheAdapter = _cache.register(this,ddr);
      }catch(IllegalStateException e){
        throw new UnexpectedException(e);
      }

//      _errorModel = new CompilerErrorModel<CompilerError> (new CompilerError[0], null);
//      _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
      _breakpoints = new Vector<Breakpoint>();
      _modifiedSinceSave = false;
    }
    
    
    
    /**
     * a file is in the project if the source root is the same as the
     * project root. this means that project files must be saved at the
     * source root. (we query the model through the model's state)
     */
    public boolean isInProjectPath(){
      return _state.isInProjectPath(this);
    }

        
    /**
     * a file is in the project if the source root is the same as the
     * project root. this means that project files must be saved at the
     * source root. (we query the model through the model's state)
     */
    public boolean isProjectFile(){
      if(!isUntitled()){
        return _state.isProjectFile(_file);
      }else{
        return false;
      }
    }
    
    /**
     * @return true if the file is an auxiliary file
     */
    public boolean isAuxiliaryFile(){
      if(!isUntitled()){
        return _state.isAuxiliaryFile(_file);
      }else{
        return false;
      }
    }
    
    
    /**
     * makes a default DDReconstructor that will make a Document based on if the Handler has a file or not
     */
    protected DDReconstructor makeReconstructor(){
      return new DDReconstructor(){
        
        // Brand New documents start at location 0
        private int _loc = 0;
        // Start out with empty list for the very first time the document is made
        private DocumentListener[] _list = {};
        
//        private CompoundUndoManager _undo = null;
//        
        private UndoableEditListener[] _undoListeners = {};

        private List<FinalizationListener<DefinitionsDocument>> _finalListeners =
          new LinkedList<FinalizationListener<DefinitionsDocument>>();
        
        public DefinitionsDocument make() throws IOException, BadLocationException, FileMovedException{
          DefinitionsDocument tempDoc;
          tempDoc = new DefinitionsDocument(_notifier);
          tempDoc.setOpenDefDoc(ConcreteOpenDefDoc.this);
            
          
          if(_file != null){
            FileReader reader = new FileReader(_file);
            _editorKit.read(reader, tempDoc, 0);
            reader.close(); // win32 needs readers closed explicitly!
          }
          
          tempDoc.setCurrentLocation(_loc);
          for(DocumentListener d : _list) {
            if (d instanceof DocumentUIListener) {
              tempDoc.addDocumentListener(d);
            }
          }
          for(UndoableEditListener l: _undoListeners){
            tempDoc.addUndoableEditListener(l);
          }
          for(FinalizationListener<DefinitionsDocument> l: _finalListeners){
            tempDoc.addFinalizationListener(l);
          }
          tempDoc.resetModification();
          //            tempDoc.setUndoManager(_undo);
          try {
            _packageName = tempDoc.getPackageName();
          } catch(InvalidPackageException e) {
            _packageName = null;
          }
          
          return tempDoc;
        }
        public void saveDocInfo(DefinitionsDocument doc) {
//          _undo = doc.getUndoManager();
//          _undoListeners = doc.getUndoableEditListeners();
          _loc = doc.getCurrentLocation();
          _list = doc.getDocumentListeners();
          _finalListeners = doc.getFinalizationListeners();
        }
        
        public void addDocumentListener(DocumentListener dl) {
          ArrayList<DocumentListener> tmp = new ArrayList<DocumentListener>();
          for (DocumentListener l: _list) {
            if (dl != l) tmp.add(l);
          }
          tmp.add(dl);
          _list = tmp.toArray(new DocumentListener[0]);
        }
      };
    }
    
    public int getInitialVerticalScroll() {
      return _initVScroll;
    }
    public int getInitialHorizontalScroll() {
      return _initHScroll;
    }
    public int getInitialSelectionStart() {
      return _initSelStart;
    }
    public int getInitialSelectionEnd() {
      return _initSelEnd;
    }
    
    void setPackage(String pack) { _packageName = pack; }
    void setInitialVScroll(int i) { _initVScroll = i; }
    void setInitialHScroll(int i) { _initHScroll = i; }
    void setInitialSelStart(int i) { _initSelStart = i; }
    void setInitialSelEnd(int i) { _initSelEnd = i; }
      
    /**
     * Originally designed to allow undoManager to set the current document to
     * be modified whenever an undo or redo is performed.
     * Now it actually does this.
     */
    public void setModifiedSinceSave() {
      getDocument().setModifiedSinceSave();
    }


    /**
     * Gets the definitions document being handled.
     * @return document being handled
     */
    protected DefinitionsDocument getDocument() {
//      if (SHOW_GETDOC) { // SHOW_GETDOC's definitions is at _openFiles(...)
//        System.out.println(this);
//        Exception e = new Exception("");
//        System.out.println("  " + e.getStackTrace()[1]);
//        System.out.println("  " + e.getStackTrace()[2]);
//        System.out.println("  " + e.getStackTrace()[3]);
//        System.out.println("  " + e.getStackTrace()[4]);
//      }
      try{
        return _cacheAdapter.getDocument();
      }catch(FileMovedException e){
//        System.out.println("DefaultGlobalModel: 1430: FileMovedException should be handled by box that fixes everything.");
      }catch(IOException e){
//        System.out.println("DefaultGlobalModel: 1432: IOException should be handled by box that fixes everything.");
      }
      try {
        _notifier.documentNotFound(this,_file);
        _documentNavigator.refreshDocument(getIDocGivenODD(this), _file.getCanonicalFile().getParent());
      } catch(IOException ioe) {
        throw new UnexpectedException(ioe);
      }
      
      return getDocument();

    }

    /** 
     * Returns the name of the top level class, if any.
     * @throws ClassNameNotFoundException if no top level class name found.
     */
    public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
      return getDocument().getFirstTopLevelClassName();
    }

    /**
     * Returns whether this document is currently untitled
     * (indicating whether it has a file yet or not).
     * @return true if the document is untitled and has no file
     */
    public boolean isUntitled() {
      return (_file == null);
    }

    /**
     * Returns the file for this document.  If the document
     * is untitled and has no file, it throws an IllegalStateException.
     * @return the file for this document
     * @exception IllegalStateException if no file exists
     */
    public File getFile() throws IllegalStateException , FileMovedException {
        if (_file == null) {
          throw new IllegalStateException("This document does not yet have a file.");
        }
        //does the file actually exist?
        if (_file.exists()) {
          return _file;
        }
        else {
          throw new FileMovedException(_file,
                                       "This document's file has been moved or deleted.");
        }
    }
    
    /**
     * Returns true if the file exists on disk. Returns false if the file has been moved or deleted
     */
    public boolean fileExists() {
      try {        
        getFile();
        return true;
      } 
      catch(IllegalStateException ise) {
      } 
      catch(FileMovedException fme) {
      }
      return false;
    }
    
    /**
     * Returns true if the file exists on disk. Prompts the user otherwise
     */
    public boolean verifyExists() {
      if(! fileExists()) {
        
        try {
          //prompt the user to find it
          try {
            _notifier.documentNotFound(this,_file);
            _documentNavigator.refreshDocument(getIDocGivenODD(this), _file.getCanonicalFile().getParent());
          } catch(IOException ioe) {
            throw new UnexpectedException(ioe);
          }
          
          return true;
        } 
        catch(DocumentClosedException dce) {
          return false;
        }
      }
      return true; //if file exists
    }

    /**
     * Returns the name of this file, or "(untitled)" if no file.
     */
    public String getFilename() {
      String filename = "(Untitled)";
      try {
        File file = getFile();
        filename = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file, leave as "untitled"
      }
      catch (FileMovedException fme) {
        // Recover, even though file has been deleted
        File file = fme.getFile();
        filename = file.getName();
      }
      return filename;
    }
      

    // TODO: Move this to where it can be static.
    private class TrivialFSS implements FileSaveSelector {
      private File _file;
      private TrivialFSS(File file) {
        _file = file;
      }
      public File getFile() throws OperationCanceledException {
        return _file;
      }
      public boolean  warnFileOpen(File f) {
        return true;
      }
      public boolean verifyOverwrite() {
        return true;
      }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                              File oldFile) {
        return true;
      }
    }

    /**
     * Saves the document with a FileWriter.  If the file name is already
     * set, the method will use that name instead of whatever selector
     * is passed in.
     * @param com a selector that picks the file name if the doc is untitled
     * @exception IOException
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFile(FileSaveSelector com) throws IOException {
      FileSaveSelector realCommand;
      final File file;
      
      if (!isModifiedSinceSave() && !isUntitled()) {
        // Don't need to save.
        //  Return true, since the save wasn't "canceled"
        return true;
      }
      
      try {
        if (isUntitled()) {
          realCommand = com;
        }
        else {
          try {
            file = getFile();
            realCommand = new TrivialFSS(file);
          }
          catch (FileMovedException fme) {
            // getFile() failed, prompt the user if a new one should be selected
            if (com.shouldSaveAfterFileMoved(this, fme.getFile())) {
              realCommand = com;
            }
            else {
              // User declines to save as a new file, so don't save
              return false;
            }
          }
        }
        return saveFileAs(realCommand);
      }
      catch (IllegalStateException ise) {
        // No file--  this should have been caught by isUntitled()
        throw new UnexpectedException(ise);
      }
    }

    /**
     * Saves the document with a FileWriter.  The FileSaveSelector will
     * either provide a file name or prompt the user for one.  It is
     * up to the caller to decide what needs to be done to choose
     * a file to save to.  Once the file has been saved succssfully,
     * this method fires fileSave(File).  If the save fails for any
     * reason, the event is not fired.
     * This is synchronized against the compiler model to prevent saving and
     * compiling at the same time- this used to freeze drjava.
     * @param com a selector that picks the file name.
     * @throws IOException if the save fails due to an IO error
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFileAs(FileSaveSelector com) throws IOException {
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile();
        OpenDefinitionsDocument otherDoc = _getOpenDocument(file);
        boolean shouldSave = false;
        boolean openInOtherDoc = ((otherDoc != null) && (openDoc != otherDoc));
        // Check if file is already open in another document
        if ( openInOtherDoc ) {
          // Can't save over an open document
          shouldSave = com.warnFileOpen(file);
        }
        
        // If the file exists, make sure it's ok to overwrite it
        if ((shouldSave && openInOtherDoc) || (!openInOtherDoc && (!file.exists() || com.verifyOverwrite()))) {
//        // Check if file is already open in another document
//        if ( otherDoc != null && openDoc != otherDoc ) {
//          // Can't save over an open document
//          shouldSave = com.warnFileOpen(file);
//        }
//        
//        // If the file exists, make sure it's ok to overwrite it
//        if (shouldSave || !file.exists() || com.verifyOverwrite()) {
          // Correct the case of the filename (in Windows)
          if (! file.getCanonicalFile().getName().equals(file.getName())) {
            file.renameTo(file);
          }
          
          // Check for # in the path of the file because if there
          // is one, then the file cannot be used in the Interactions Pane
          if (file.getAbsolutePath().indexOf("#") != -1) {
            _notifier.filePathContainsPound();
          }
          
          // have FileOps save the file
          FileOps.saveFile(new FileOps.DefaultFileSaver(file){
            public void saveTo(OutputStream os) throws IOException {
              try {
                _editorKit.write(os, getDocument(), 0, getDocument().getLength());
              } catch (BadLocationException docFailed){
                // We don't expect this to happen
                throw new UnexpectedException(docFailed);
              }
            }
          });
          
          resetModification();
          setFile(file);
          try {
            // This calls getDocument().getPackageName() because
            // this may be untitled and this.getPackageName() returns
            // "" if it's untitled.  Right here we are interested in
            // parsing the DefinitionsDocument's text
            _packageName = getDocument().getPackageName();
          } catch(InvalidPackageException e) {
            _packageName = null;
          }
          getDocument().setCachedClassFile(null);
          checkIfClassFileInSync();
          _notifier.fileSaved(openDoc);
          
          
          INavigatorItem idoc = _documentsRepos.getKey(this);
          //          _documentNavigator.removeDocument(idoc);
          //          _documentNavigator.addDocument(idoc);
          
          // Make sure this file is on the classpath
          try {
            File classpath = getSourceRoot();
            _interactionsModel.addToClassPath(classpath.getAbsolutePath());
          }
          catch (InvalidPackageException e) {
            // Invalid package-- don't add to classpath
          }
          
          /* update the navigator */
          _documentNavigator.refreshDocument(getIDocGivenODD(this), fixPathForNavigator(file.getCanonicalPath()));
          
        }
        return true;
      }
      catch (OperationCanceledException oce) {
        // Thrown by com.getFile() if the user cancels.
        //   We don't save if this happens.
        return false;
      }
    }

    
    /**
     * Whenever this document has been saved, this method should be called
     * so that it knows it's no longer in a modified state.
     */
    public void resetModification() {
      getDocument().resetModification();
      if (_file != null) {
        _timestamp = _file.lastModified();
      }
    }
    
    
    /**
     * sets the file for this openDefinitionsDocument
     */
    public void setFile(File file) {
      _file = file;
//      resetModification();
      //jim: maybe need lock
      if (_file != null) {
        _timestamp = _file.lastModified();
      }
    }
    
    /**
     * returns the timestamp
     */
    public long getTimestamp() {
      return _timestamp;
    }
    
    /**
     * This method tells the document to prepare all the DrJavaBook
     * and PagePrinter objects.
     */
    public void preparePrintJob() throws BadLocationException,
      FileMovedException {

      String filename = "(untitled)";
      try {
        filename = getFile().getAbsolutePath();
      }
      catch (IllegalStateException e) {
      }

      _book = new DrJavaBook(getDocument().getText(0, getDocument().getLength()), filename, _pageFormat);
    }

    /**
     * Prints the given document by bringing up a
     * "Print" window.
     */
    public void print() throws PrinterException, BadLocationException,
      FileMovedException
    {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      if (printJob.printDialog()) {
        printJob.print();
      }
      cleanUpPrintJob();
    }

    /**
     * Returns the Pageable object for printing.
     * @return A Pageable representing this document.
     */
    public Pageable getPageable() throws IllegalStateException {
      return _book;
    }

    public void cleanUpPrintJob() {
      _book = null;
    }

    public void startCompile() throws IOException {
      _compilerModel.compile(ConcreteOpenDefDoc.this);
    }

    /**
     * Runs the main method in this document in the interactions pane.
     * Demands that the definitions be saved and compiled before proceeding.
     * Fires an event to signal when execution is about to begin.
     * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
     * @exception IOException propagated from GlobalModel.compileAll()
     */
    public void runMain() throws ClassNameNotFoundException, IOException {
      try {
        // First, get the class name to use.  This relies on Java's convention of
        // one top-level class per file.
        String className = getDocument().getQualifiedClassName();
        /*  Do not compile in any case.
        // Prompt to save and compile if any document is modified.
        if (hasModifiedDocuments()) {
          _notifier.saveBeforeRun();

          // If the user chose to cancel, abort the run.
          if (hasModifiedDocuments()) {
            return;
          }
        }
        // If no document is modified, still compile the current doc.
        // compile only if class file out of sync
        if (!checkIfClassFileInSync()) {
          startCompile();
        }

        // Make sure that the compiler is done before continuing.
        synchronized(_compilerModel) {
          // If the compile had errors, abort the run.
          if (!_compilerErrorModel.hasOnlyWarnings()) {
            return;
          }
        }
        */
        // Then clear the current interaction and replace it with a "java X" line.
        InteractionsDocument iDoc = _interactionsModel.getDocument();
//        if (iDoc.inProgress()) {
//          addListener(_notifyListener);
//          _shouldRun = true;
//          synchronized(_notifyListener) {
//            try {
//              _notifyListener.wait();
//            }
//            catch(InterruptedException ie) {
//            }
//          }
//          removeListener(_notifyListener);
//          if (!_shouldRun) {
//            // The interactions pane was reset during another interaction.
//            //  Don't run the main method.
//            return;
//          }
//        }
        
        synchronized (_interpreterControl) {
          iDoc.clearCurrentInput();
          if (!checkIfClassFileInSync()) {
            iDoc.insertBeforeLastPrompt(DOCUMENT_OUT_OF_SYNC_MSG, InteractionsDocument.ERROR_STYLE);
          }
          iDoc.insertText(iDoc.getDocLength(), "java " + className, null);

          // Notify listeners that the file is about to be run.
          _notifier.runStarted(this);

          // Finally, execute the new interaction.
          _interactionsModel.interpretCurrentInteraction();
        }
      }
      catch (DocumentAdapterException e) {
        // This was thrown by insertText - and shouldn't have happened.
        throw new UnexpectedException(e);
      }
    }

    /**
     * Runs JUnit on the current document. Used to compile all open documents
     * before testing but have removed that requirement in order to allow the
     * debugging of test cases. If the classes being tested are out of
     * sync, a message is displayed.
     */
    public void startJUnit() throws ClassNotFoundException, IOException {
      _junitModel.junit(ConcreteOpenDefDoc.this);
    }

    /**
     * Generates Javadoc for this document, saving the output to a temporary
     * directory.  The location is provided to the javadocEnded event on
     * the given listener.
     * @param saver FileSaveSelector for saving the file if it needs to be saved
     */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      _javadocModel.javadocDocument(this, saver, getClasspath());
    }
    
    /**
     * Determines if the document has been modified since the last save.
     * @return true if the document has been modified
     */
    public boolean isModifiedSinceSave() {
      /* if the document is not in the cache, then we know that it's not modified, so
       * only check if the DDoc is in the cache */
      if(_cacheAdapter.isReady()){
        return getDocument().isModifiedSinceSave();
      }else{
        return false;
      }
    }
    
    /**
     * Determines if the document has been modified since the last save.
     * @return true if the document has been modified
     */
    public boolean isModifiedOnDisk() {
      boolean ret = false;
      try {
        getDocument().aquireReadLock();
        if (_file == null) {
        } else {
          ret = (_file.lastModified() > _timestamp);
        }
      }
      finally {
        getDocument().releaseReadLock();
      }
      return ret;
    }
    
    
    /**
     * Checks if the document is modified. If not, searches for the class file
     * corresponding to this document and compares the timestamps of the
     * class file to that of the source file.
     */
    public boolean checkIfClassFileInSync() {
      // If modified, then definitely out of sync
      if(isModifiedSinceSave()) {
        getDocument().setClassFileInSync(false);
        return false;
      }

      // Look for cached class file
      File classFile = getDocument().getCachedClassFile();
      if (classFile == null) {
        // Not cached, so locate the file
        classFile = _locateClassFile();
        getDocument().setCachedClassFile(classFile);
        if (classFile == null) {
          // couldn't find the class file
          getDocument().setClassFileInSync(false);
          return false;
        }
      }

      // compare timestamps
      File sourceFile;
      try {
        sourceFile = getFile();
      }
      catch (IllegalStateException ise) {
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        getDocument().setClassFileInSync(false);
        return false;
      }
      if (sourceFile.lastModified() > classFile.lastModified()) {
        getDocument().setClassFileInSync(false);
        return false;
      }
      else {
        getDocument().setClassFileInSync(true);
        return true;
      }
    }

    /**
     * Returns the class file for this source document, if one could be found.
     * Looks in the source root directories of the open documents, the
     * system classpath, and the "extra.classpath".  Returns null if the
     * class file could not be found.
     */
    private File _locateClassFile() {
      try {
        String className = getDocument().getQualifiedClassName();
        String ps = System.getProperty("file.separator");
        // replace periods with the System's file separator
        className = StringOps.replace(className, ".", ps);
        String filename = className + ".class";

        // Check source root set (open files)
        File[] sourceRoots = {};
//        sourceRoots = getSourceRootSet();
        Vector<File> roots = new Vector<File>();
        // Add the current document to the beginning of the roots Vector
        
        
        if(getBuildDirectory() != null){
          roots.add(getBuildDirectory());
        }
        
        
        try {
          roots.add(getSourceRoot());
        }
        catch (InvalidPackageException ipe) {
          try {
            File f = getFile().getParentFile();
            if (f != null) {
              roots.add(f);
            }
          }
          catch (IllegalStateException ise) {
            // No file, don't add to source root set
          }
          catch (FileMovedException fme) {
            // Moved, but we'll add the old file to the set anyway
            File root = fme.getFile().getParentFile();
            if (root != null) {
              roots.add(root);
            }
          }
        }

        for (int i=0; i < sourceRoots.length; i++) {
          roots.add(sourceRoots[i]);
        }
        
        File classFile = getSourceFileFromPaths(filename, roots);

        if (classFile == null) {
          // Class not on source root set, check system classpath
          String cp = System.getProperty("java.class.path");
          String pathSeparator = System.getProperty("path.separator");
          Vector<File> cpVector = new Vector<File>();
          for (int i = 0; i < cp.length();) {
            int nextSeparator = cp.indexOf(pathSeparator, i);
            if (nextSeparator == -1) {
              cpVector.add(new File(cp.substring(i, cp.length())));
              break;
            }
            cpVector.add(new File(cp.substring(i, nextSeparator)));
            i = nextSeparator + 1;
          }
          classFile = getSourceFileFromPaths(filename, cpVector);
        }

        if (classFile == null) {
          // not on system classpath, check interactions classpath
          classFile = getSourceFileFromPaths(filename, DrJava.getConfig().getSetting(EXTRA_CLASSPATH));
        }

        return classFile;
      }
      catch (ClassNameNotFoundException cnnfe) {
        // No class name found, so we can't find a class file
        return null;
      }
    }


    /**
     * Determines if the defintions document has been changed
     * by an outside program. If the document has changed,
     * then asks the listeners if the GlobalModel should
     * revert the document to the most recent version saved.
     * @return true if document has been reverted
     */
    public boolean revertIfModifiedOnDisk() throws IOException{
      final OpenDefinitionsDocument doc = this;
      if (isModifiedOnDisk()) {

        boolean shouldRevert = _notifier.shouldRevertFile(doc);
        if (shouldRevert) {
          doc.revertFile();
        }
        return shouldRevert;
      }
      else {
        return false;
      }
    }
    
    public void close(){
//      System.err.println("---------------------------");
//      new RuntimeException().printStackTrace(System.err);
//      System.err.println("---------------------------");
      removeFromDebugger();
      _cacheAdapter.close();
    }

    public void revertFile() throws IOException {

      //need to remove old, possibly invalid breakpoints
      removeFromDebugger();

      final OpenDefinitionsDocument doc = this;

      try {
        File file = doc.getFile();
        //this line precedes the .remove() so that a document with an invalid
        // file is not cleared before this fact is discovered.

        FileReader reader = new FileReader(file);
        doc.remove(0,doc.getLength());


        _editorKit.read(reader, doc, 0);
        reader.close(); // win32 needs readers closed explicitly!

        resetModification();
        doc.checkIfClassFileInSync();

        setCurrentLocation(0);

        _notifier.fileReverted(doc);
      }
      catch (IllegalStateException docFailed) {
        //cant revert file if doc has no file
        throw new UnexpectedException(docFailed);
      }
      catch (BadLocationException docFailed) {
        throw new UnexpectedException(docFailed);
      }
    }

    /**
     * Asks the listeners if the GlobalModel can abandon the current document.
     * Fires the canAbandonFile(File) event if isModifiedSinceSave() is true.
     * @return true if the current document may be abandoned, false if the
     * current action should be halted in its tracks (e.g., file open when
     * the document has been modified since the last save).
     */
    public boolean canAbandonFile() {
      final OpenDefinitionsDocument doc = this;
      if (isModifiedSinceSave()) {
        return _notifier.canAbandonFile(doc);
      }
      else {
        return true;
      }
    }

    /**
     * Moves the definitions document to the given line, and returns
     * the character position in the document it's gotten to.
     * @param line Number of the line to go to. If line exceeds the number
     *             of lines in the document, it is interpreted as the last line.
     * @return Index into document of where it moved
     */
    public int gotoLine(int line) {
      getDocument().gotoLine(line);
      return getDocument().getCurrentLocation();
    }

    /**
     * Forwarding method to sync the definitions with whatever view
     * component is representing them.
     */
    public void setCurrentLocation(int location) {
      getDocument().setCurrentLocation(location);
    }

    /**
     * Get the location of the cursor in the definitions according
     * to the definitions document.
     */
    public int getCurrentLocation() {
        return getDocument().getCurrentLocation();
    }

    /**
     * Forwarding method to find the match for the closing brace
     * immediately to the left, assuming there is such a brace.
     * @return the relative distance backwards to the offset before
     *         the matching brace.
     */
    public int balanceBackward() {
      return getDocument().balanceBackward();
    }

    /**
     * Forwarding method to find the match for the open brace
     * immediately to the right, assuming there is such a brace.
     * @return the relative distance forwards to the offset after
     *         the matching brace.
     */
    public int balanceForward() {
      return getDocument().balanceForward();
    }

    /**
     * A forwarding method to comment out the current line or selection
     * in the definitions.
     */
    public void commentLinesInDefinitions(int selStart, int selEnd) {
      getDocument().commentLines(selStart, selEnd);
    }

    /**
     * A forwarding method to un-comment the current line or selection
     * in the definitions.
     */
    public void uncommentLinesInDefinitions(int selStart, int selEnd) {
      getDocument().uncommentLines(selStart, selEnd);
    }

    /**
     * Create a find and replace mechanism starting at the current
     * character offset in the definitions.
     * NOT USED.
     */
//    public FindReplaceMachine createFindReplaceMachine() {
      //try {
      //return new FindReplaceMachine(_doc, _doc.getCurrentLocation());
//      return new FindReplaceMachine();
      //}
      //catch (BadLocationException e) {
      //throw new UnexpectedException(e);
      //}
//    }

    /**
     * Returns the first Breakpoint in this OpenDefinitionsDocument whose region
     * includes the given offset, or null if one does not exist.
     * @param offset an offset at which to search for a breakpoint
     * @return the Breakpoint at the given lineNumber, or null if it does not exist.
     */
    public Breakpoint getBreakpointAt( int offset) {
      //return _breakpoints.get(new Integer(lineNumber));

      for (int i =0; i<_breakpoints.size(); i++) {
        Breakpoint bp = _breakpoints.get(i);
        if (offset >= bp.getStartOffset() && offset <= bp.getEndOffset()) {
          return bp;
        }
      }
      return null;
    }

    /**
     * Inserts the given Breakpoint into the list, sorted by region
     * @param breakpoint the Breakpoint to be inserted
     */
    public void addBreakpoint( Breakpoint breakpoint) {
      //_breakpoints.put( new Integer(breakpoint.getLineNumber()), breakpoint);

      for (int i=0; i<_breakpoints.size();i++) {
        Breakpoint bp = _breakpoints.get(i);
        int oldStart = bp.getStartOffset();
        int newStart = breakpoint.getStartOffset();
        
        if ( newStart < oldStart) {
          // Starts before, add here
          _breakpoints.add(i, breakpoint);
          return;
        }
        if ( newStart == oldStart) {
          // Starts at the same place
          int oldEnd = bp.getEndOffset();
          int newEnd = breakpoint.getEndOffset();
          
          if ( newEnd < oldEnd) {
            // Ends before, add here
            _breakpoints.add(i, breakpoint);
            return;
          }
        }
      }
      _breakpoints.add(breakpoint);
    }
    
    /**
     * Remove the given Breakpoint from our list (but not the debug manager)
     * @param breakpoint the Breakpoint to be removed.
     */
    public void removeBreakpoint(Breakpoint breakpoint) {
      _breakpoints.remove(breakpoint);
    }
    
    /**
     * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects
     * in this document.
     */
    public Vector<Breakpoint> getBreakpoints() {
      return _breakpoints;
    }
    
    /**
     * Tells the document to remove all breakpoints (without removing them
     * from the debug manager).
     */
    public void clearBreakpoints() {
      _breakpoints.clear();
    }
    
    /**
     * Called to indicate the document is being closed, so to remove
     * all related state from the debug manager.
     */
    public void removeFromDebugger() {
      if (_debugger.isAvailable() && (_debugger.isReady())) {
        try {
          while (_breakpoints.size() > 0) {
            _debugger.removeBreakpoint(_breakpoints.get(0));
          }
        }
        catch (DebugException de) {
          // Shouldn't happen if debugger is active
          throw new UnexpectedException(de);
        }
      }
      else {
        clearBreakpoints();
      }
    }
    
    
    
    /**
     * Finds the root directory of the source files.
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    public File getSourceRoot() throws InvalidPackageException {
      return _getSourceRoot(getPackageName());
    }
    
    /**
     * Gets the name of the package this source file claims it's in (with the
     * package keyword). It does this by minimally parsing the source file
     * to find the package statement.
     *
     * @return The name of package this source file declares itself to be in,
     *         or the empty string if there is no package statement (and thus
     *         the source file is in the empty package).
     *
     * @exception InvalidPackageException if there is some sort of a
     *                                    <TT>package</TT> statement but it
     *                                    is invalid.
     */
    public String getPackageName() throws InvalidPackageException {
      if(isUntitled()) {
        _packageName = "";
      }
      else if(_packageName == null){
        _packageName = getDocument().getPackageName();
      }
      return _packageName;
    }
    
    /**
     * Finds the root directory of the source files.
     * @param packageName Package name, already fetched from the document
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    private File _getSourceRoot(String packageName)
      throws InvalidPackageException
    {
      File sourceFile;
      try {
        sourceFile = getFile();
      }
      catch (IllegalStateException ise) {
        throw new InvalidPackageException(-1, "Can not get source root for " +
                                          "unsaved file. Please save.");
      }
      catch (FileMovedException fme) {
        throw new InvalidPackageException(-1, "File has been moved or deleted " +
                                          "from its previous location. Please save.");
      }
      
      if (packageName.equals("")) {
        return sourceFile.getParentFile();
      }
      
      Stack<String> packageStack = new Stack<String>();
      int dotIndex = packageName.indexOf('.');
      int curPartBegins = 0;
      
      while (dotIndex != -1) {
        packageStack.push(packageName.substring(curPartBegins, dotIndex));
        curPartBegins = dotIndex + 1;
        dotIndex = packageName.indexOf('.', dotIndex + 1);
      }
      
      // Now add the last package component
      packageStack.push(packageName.substring(curPartBegins));
      
      // Must use the canonical path, in case there are dots in the path
      //  (which will conflict with the package name)
      try {
        File parentDir = sourceFile.getCanonicalFile();
        while (!packageStack.empty()) {
          String part = packageStack.pop();
          parentDir = parentDir.getParentFile();

          if (parentDir == null) {
            throw new RuntimeException("parent dir is null?!");
          }

          // Make sure the package piece matches the directory name
          if (! part.equals(parentDir.getName())) {
            String msg = "The source file " + sourceFile.getAbsolutePath() +
              " is in the wrong directory or in the wrong package. " +
              "The directory name " + parentDir.getName() +
              " does not match the package component " + part + ".";

            throw new InvalidPackageException(-1, msg);
          }
        }

        // OK, now parentDir points to the directory of the first component of the
        // package name. The parent of that is the root.
        parentDir = parentDir.getParentFile();
        if (parentDir == null) {
          throw new RuntimeException("parent dir of first component is null?!");
        }

        return parentDir;
      }
      catch (IOException ioe) {
        String msg = "Could not locate directory of the source file: " + ioe;
        throw new InvalidPackageException(-1, msg);
      }
    }
    
    public String toString() {
      return "(ODD " + _id + ":" + getFilename() + ")";
    }
    
    
    /**
     * @param doc the document to test
     * @return true if the document belongs to this open def doc
     */
    public boolean belongsHuh(Document doc){
      return _belongsHuhHelper(doc);
    }
    
    private boolean _belongsHuhHelper(Document doc){
      return false;
    }
    
    private boolean _belongsHuhHelper(DefinitionsDocument doc){
      return (doc.getOpenDefDoc() == this);
    }
    
    
    /**
     * Implementation of the javax.swing.text.Document interface
     */
    public void addDocumentListener(DocumentListener listener){
      if(_cacheAdapter.isReady()){
        getDocument().addDocumentListener(listener);
      }
      else {
        _cacheAdapter.getReconstructor().addDocumentListener(listener);
      }
    }
    
    
    List<UndoableEditListener> _undoableEditListeners = new LinkedList<UndoableEditListener>();
    public void addUndoableEditListener(UndoableEditListener listener){
      _undoableEditListeners.add(listener);
      getDocument().addUndoableEditListener(listener);
    }

    public void removeUndoableEditListener(UndoableEditListener listener){
      _undoableEditListeners.remove(listener);
      getDocument().removeUndoableEditListener(listener);
    }
    
    public UndoableEditListener[] getUndoableEditListeners() {
      return getDocument().getUndoableEditListeners();
    }
 

//    public List<UndoableEditListener> getUndoableEditListeners(){
//      return _undoableEditListeners;
//    }
    

    public Position createPosition(int offs) throws BadLocationException{
      return getDocument().createPosition(offs);
    }
    
    public Element getDefaultRootElement(){
      return getDocument().getDefaultRootElement();
    }
    
    public Position getEndPosition() {
      return getDocument().getEndPosition();
    }
    
    public int getLength(){
      return getDocument().getLength();
    }
        
    public Object getProperty(Object key){
      return getDocument().getProperty(key);
    }
    
    public Element[] getRootElements(){
      return getDocument().getRootElements();
    }
    
    public Position getStartPosition(){
      return getDocument().getStartPosition();
    }
    
    public String getText(int offset, int length) throws BadLocationException{
      return getDocument().getText(offset, length);
    }
    
    public void getText(int offset, int length, Segment txt) throws BadLocationException{
      getDocument().getText(offset, length, txt);
    }
    
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException{
      getDocument().insertString(offset, str, a);
    }
    
    public void putProperty(Object key, Object value){
      getDocument().putProperty(key, value);
    }
    
    public void remove(int offs, int len) throws BadLocationException{
      getDocument().remove(offs, len);
    }
    
    public void removeDocumentListener(DocumentListener listener) {
      getDocument().removeDocumentListener(listener);
    }
    
    public void render(Runnable r) {
      getDocument().render(r);
    }
    
    /**
     * end implementation of javax.swing.text.Document interface
     */
    
    /**
     * If the undo manager is unavailable, no undos are available
     * @return whether the undo manager can perform any undo's
     */
    public boolean undoManagerCanUndo() {
      return _cacheAdapter.isReady() && getUndoManager().canUndo();
    }
    /**
     * If the undo manager is unavailable, no redos are available
     * @return whether the undo manager can perform any redo's
     */
    public boolean undoManagerCanRedo() {
      return _cacheAdapter.isReady() && getUndoManager().canRedo();
    }
    
    /**
     * decorater patter for the definitions document
     */
    public CompoundUndoManager getUndoManager(){
      return getDocument().getUndoManager();
    }

    public int getLineStartPos(int pos) {
      return getDocument().getLineStartPos(pos);
    }
    
    public int getLineEndPos(int pos){
      return getDocument().getLineEndPos(pos);
    }

    public void commentLines(int selStart, int selEnd){
      getDocument().commentLines(selStart, selEnd);
    }
    
    public void uncommentLines(int selStart, int selEnd){
      getDocument().uncommentLines(selStart, selEnd);
    }
    
    public void indentLines(int selStart, int selEnd){
      getDocument().indentLines(selStart, selEnd);
    }
    
    public int getCurrentCol() {
      return getDocument().getCurrentCol();
    }

    public boolean getClassFileInSync(){
      return getDocument().getClassFileInSync();
    }

    public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
      return getDocument().getIntelligentBeginLinePos(currPos);
    }

    public int getOffset(int lineNum) {
      return getDocument().getOffset(lineNum);
    }
    
    public String getQualifiedClassName() throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName();
    }

    public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName(pos);
    }

    public ReducedModelState getStateAtCurrent(){
      return getDocument().getStateAtCurrent();
    }

    public void resetUndoManager() {
      // if it's not in the cache, the undo manager will be 
      // reset when it's reconstructed
      if(_cacheAdapter.isReady()){
        getDocument().resetUndoManager();
      }
    }

    public File getCachedClassFile() {
      return getDocument().getCachedClassFile();
    }
    
    public DocumentListener[] getDocumentListeners() {
      return getDocument().getDocumentListeners();
    }
    
    //--------- DJDocument methods ----------
    
    public void setTab(String tab, int pos) {
      getDocument().setTab(tab,pos);
    }
    
    public int getWhiteSpace() {
      return getDocument().getWhiteSpace();
    }
    
    public boolean posInParenPhrase(int pos) {
      return getDocument().posInParenPhrase(pos);
    }
    
    public boolean posInParenPhrase() {
      return getDocument().posInParenPhrase();
    }
    
    public int findPrevNonWSCharPos(int pos) throws BadLocationException {
      return getDocument().findPrevNonWSCharPos(pos);
    }
    
    public int getFirstNonWSCharPos(int pos) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos);
    }
    
    public int getFirstNonWSCharPos(int pos, boolean acceptComments) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos, acceptComments);
    }
    
    public int getFirstNonWSCharPos (int pos, char[] whitespace, boolean acceptComments) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos, whitespace, acceptComments);
    }
    
    public int getLineFirstCharPos(int pos) throws BadLocationException {
      return getDocument().getLineFirstCharPos(pos);
    }
    
    public int findCharOnLine(int pos, char findChar) {
      return getDocument().findCharOnLine(pos, findChar);
    }
    
    public String getIndentOfCurrStmt(int pos) throws BadLocationException {
      return getDocument().getIndentOfCurrStmt(pos);
    }
  
    public String getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException {
      return getDocument().getIndentOfCurrStmt(pos, delims);
    }
    
    public String getIndentOfCurrStmt(int pos, char[] delims, char[] whitespace) throws BadLocationException {
      return getDocument().getIndentOfCurrStmt(pos, delims, whitespace);
    }
    
    public void indentLines(int selStart, int selEnd, int reason, ProgressMonitor pm) throws OperationCanceledException {
      getDocument().indentLines(selStart, selEnd, reason, pm);
    }
    
    public int findPrevCharPos(int pos, char[] whitespace) throws BadLocationException {
      return getDocument().findPrevCharPos(pos, whitespace);
    }
    
    public boolean findCharInStmtBeforePos(char findChar, int position) {
      return getDocument().findCharInStmtBeforePos(findChar, position);
    }
    
    public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException {
      return getDocument().findPrevDelimiter(pos, delims);
    }
    
    public int findPrevDelimiter(int pos, char[] delims, boolean skipParenPhrases) throws BadLocationException {
      return getDocument().findPrevDelimiter(pos, delims, skipParenPhrases);
    }
    
    public void resetReducedModelLocation() {
      getDocument().resetReducedModelLocation();
    }
    
    public ReducedModelState stateAtRelLocation(int dist) {
      return getDocument().stateAtRelLocation(dist);
    }
    
    public IndentInfo getIndentInformation() {
      return getDocument().getIndentInformation();
    }
    
    public void move(int dist) {
      getDocument().move(dist);
    }
    
    public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
      return getDocument().getHighlightStatus(start, end);
    }
    
    public void setIndent(int indent) {
      getDocument().setIndent(indent);
    }
    
    public int getIndent() {
      return getDocument().getIndent();
    }
    
    //-----------------------
      
    /**
     * This method is put here because the ODD is the only way to get to the defdoc
     */
    public void addFinalizationListener(FinalizationListener<DefinitionsDocument> fl) {
      getDocument().addFinalizationListener(fl);
    }
    
    public List<FinalizationListener<DefinitionsDocument>> getFinalizationListeners(){
      return getDocument().getFinalizationListeners();
    }
    
//    protected void finalize() throws Throwable{
//      System.err.println("Destroying ODD: " + this);
//    }
    
    // Styled Document Methods 
    public Font getFont(AttributeSet attr) {
      return getDocument().getFont(attr);
    }
    
    public Color getBackground(AttributeSet attr) {
      return getDocument().getBackground(attr);
    }
    
    public Color getForeground(AttributeSet attr) {
      return getDocument().getForeground(attr);
    }
    
    public Element getCharacterElement(int pos) {
      return getDocument().getCharacterElement(pos);
    }
    
    public Element getParagraphElement(int pos) {
      return getDocument().getParagraphElement(pos);
    }
    
    public Style getLogicalStyle(int p) {
      return getDocument().getLogicalStyle(p);
    }
    
    public void setLogicalStyle(int pos, Style s) {
      getDocument().setLogicalStyle(pos, s); 
    }
    
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
      getDocument().setCharacterAttributes(offset, length, s, replace); 
    }
    
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
      getDocument().setParagraphAttributes(offset, length, s, replace); 
    }
    
    public Style getStyle(String nm) {
      return getDocument().getStyle(nm); 
    }
    
    public void removeStyle(String nm) {
      getDocument().removeStyle(nm); 
    }
    
    public Style addStyle(String nm, Style parent) {
      return getDocument().addStyle(nm, parent); 
    }
  }

  /**
   * Creates a ConcreteOpenDefDoc for a new DefinitionsDocument,
   * using the DefinitionsEditorKit.
   * @return OpenDefinitionsDocument object for a new document
   */
  private OpenDefinitionsDocument _createOpenDefinitionsDocument() {
//    DefinitionsDocument doc = (DefinitionsDocument) _editorKit.createNewDocument();
    return new ConcreteOpenDefDoc();
  }


  /**
   * Returns the OpenDefinitionsDocument corresponding to the given
   * File, or null if that file is not open.
   * @param file File object to search for
   * @return Corresponding OpenDefinitionsDocument, or null
   */
  private OpenDefinitionsDocument _getOpenDocument(File file) {
    OpenDefinitionsDocument doc = null;

    Iterator<OpenDefinitionsDocument> odds = _documentsRepos.valuesIterator();
    while(odds.hasNext() && doc == null)
    {
      OpenDefinitionsDocument thisDoc = odds.next();
      try {
        File thisFile = null;
        try {
          thisFile = thisDoc.getFile();
        }
        catch (FileMovedException fme) {
          // Ok, file is invalid, but compare anyway
          thisFile = fme.getFile();
        }
        finally {
          // Always do the comparison
          if (thisFile != null) {
            try {
              // Compare canonical paths if possible
              if (thisFile.getCanonicalFile().equals(file.getCanonicalFile())) {
                doc = thisDoc;
              }
            }
            catch (IOException ioe) {
              // Can be thrown from getCanonicalFile.
              //  If so, compare the files themselves
              if (thisFile.equals(file)) {
                doc = thisDoc;
              }
            }
          }
        }
      }
      catch (IllegalStateException ise) {
        // No file in thisDoc
      }
    }

    return doc;
  }

  /*
   * Returns true if a document corresponding to the given
   * file is open, or false if that file is not open.
   * @param file File object to search for
   * @return boolean whether file is open
   * doesn't seem to be used.
  private boolean _docIsOpen(File file) {
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null)
      return false;
    else
      return true;
  }*/

  
  public List<OpenDefinitionsDocument> getNonProjectDocuments(){
    List<OpenDefinitionsDocument> allDocs = getDefinitionsDocuments();
    List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
    for(OpenDefinitionsDocument tempDoc : allDocs){
      if(!tempDoc.isInProjectPath()){
        projectDocs.add(tempDoc);
      }
    }
    return projectDocs;
  }
  
  
  
  
  public List<OpenDefinitionsDocument> getProjectDocuments(){
    List<OpenDefinitionsDocument> allDocs = getDefinitionsDocuments();
    List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
    for(OpenDefinitionsDocument tempDoc : allDocs){
      if(tempDoc.isInProjectPath() || tempDoc.isAuxiliaryFile()){
        projectDocs.add(tempDoc);
      }
    }
    return projectDocs;
  }
    
  public String fixPathForNavigator(String path) throws IOException{
      path = path.substring(0, path.lastIndexOf(File.separator));
      String _topLevelPath;
      if(getProjectFile() != null){
        _topLevelPath = getProjectFile().getCanonicalPath();
        _topLevelPath = _topLevelPath.substring(0, _topLevelPath.lastIndexOf(File.separator));;
      }else{
        _topLevelPath = "";
      }
     
      if (!path.equals(_topLevelPath) && !path.startsWith(_topLevelPath + File.separator) ){
        /** it's in external files, so don't give it a path */
        return "";
      }else{
        path = path.substring(_topLevelPath.length());
        return path;
      }
  }
  
  
  /**
   * creates an opendefinitionsdocument for a file.
   * does not add to the navigator or notify that the file's open.
   * this should be called only from within another open method that will
   * do all of this clean up.
   * @param file the file to open
   */
  private OpenDefinitionsDocument _rawOpenFile(File file) throws IOException, AlreadyOpenException{
      OpenDefinitionsDocument openDoc = _getOpenDocument(file);
      if (openDoc != null) {
        throw new AlreadyOpenException(openDoc);
      }
      final ConcreteOpenDefDoc doc = new ConcreteOpenDefDoc(file);
      if (file instanceof DocFile) {
        DocFile df = (DocFile)file;
        Pair<Integer,Integer> scroll = df.getScroll();
        Pair<Integer,Integer> sel = df.getSelection();
        doc.setPackage(df.getPackage());
        doc.setInitialVScroll(scroll.getFirst());
        doc.setInitialHScroll(scroll.getSecond());
        doc.setInitialSelStart(sel.getFirst());
        doc.setInitialSelEnd(sel.getSecond());
      }
      return doc;
  }
  
  
  /**
   * creates an iNavigatorItem for a document, and adds it to the navigator
   * this function is a helper for opening a file
   * @param doc the document to add to the navigator
   */
  private void addDocToNavigator(OpenDefinitionsDocument doc) throws IOException{
    INavigatorItem idoc = makeIDocFromODD(doc);
    _documentsRepos.put(idoc, doc);
    String path = doc.getFile().getCanonicalPath();
    _documentNavigator.addDocument(idoc, fixPathForNavigator(path));
  }
  
  /**
   * adds a documents source root to the interactions classpath
   * this function is a helper to open file
   * @param doc the document to add to the classpath
   */
  private void addDocToClasspath(OpenDefinitionsDocument doc){
    try {
      File classpath = doc.getSourceRoot();
      _interactionsModel.addToClassPath(classpath.getAbsolutePath());
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
  }
  
  
  /**
   * Creates a document from a file.
   * @param file File to read document from
   * @return openened document
   */
  private OpenDefinitionsDocument _openFile(File file) throws IOException, AlreadyOpenException
  {
    OpenDefinitionsDocument doc = _rawOpenFile(file);
    addDocToNavigator(doc);
    addDocToClasspath(doc);
    _notifier.fileOpened(doc);
    return doc;
  }
  
  /**
   * Instantiates the integrated debugger if the "debugger.enabled"
   * config option is set to true.  Leaves it at null if not.
   */
  private void _createDebugger() {
    try {
      _debugger = new JPDADebugger(this);
      _interpreterControl.setDebugModel((JPDADebugger) _debugger);
    }
    catch( NoClassDefFoundError ncdfe ){
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


  /**
   * Adds the source roots for all open documents and the paths on the
   * "extra classpath" config option to the interpreter's classpath.
   */
  public void resetInteractionsClasspath() {
    // Ideally, we'd like to put the open docs before the config option,
    //  but this is inconsistent with how the classpath was defined
    //  as it was built up.  (The config option is inserted on startup,
    //  and docs are added as they are opened.  It shouldn't switch after
    //  a reset.)

    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if(cp!=null) {
      Enumeration<File> en = cp.elements();
      while(en.hasMoreElements()) {
        _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
      }
    }

    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      _interactionsModel.addToClassPath(sourceRoots[i].getAbsolutePath());
    }
  }

   private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
    public void optionChanged (OptionEvent<Vector<File>> oce) {
      Vector<File> cp = oce.value;
      if(cp!=null) {
        Enumeration<File> en = cp.elements();
        while(en.hasMoreElements()) {
          _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
        }
      }
    }
  }

  private static class BackUpFileOptionListener implements OptionListener<Boolean> {
    public void optionChanged (OptionEvent<Boolean> oe){
      Boolean value = oe.value;
      FileOps.DefaultFileSaver.setBackupsEnabled(value.booleanValue());
    }
  }
}

