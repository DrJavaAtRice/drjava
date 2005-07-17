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

import java.awt.Container;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.Pageable;
import java.awt.Font;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import edu.rice.cs.util.ClasspathVector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.OrderedHashSet;
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.docnavigation.INavigationListener;
import edu.rice.cs.util.docnavigation.NodeData;
import edu.rice.cs.util.docnavigation.NodeDataVisitor;
import edu.rice.cs.util.docnavigation.AWTContainerNavigatorFactory;
import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.docnavigation.INavigatorItem;
import edu.rice.cs.util.docnavigation.INavigatorItemFilter;
import edu.rice.cs.util.docnavigation.JTreeSortNavigator;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.DocumentAdapterException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.DocumentUIListener;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.JPDADebugger;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.repl.ConsoleDocument;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.InputListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocumentAdapter;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.DefaultCompilerModel;
import edu.rice.cs.drjava.model.junit.DefaultJUnitModel;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.project.DocFile;
import edu.rice.cs.drjava.project.DocumentInfoGetter;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.project.ProjectFileBuilder;
import edu.rice.cs.drjava.project.ProjectFileIR;
import edu.rice.cs.drjava.project.ProjectFileParser;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.cache.DCacheAdapter;
import edu.rice.cs.drjava.model.cache.DDReconstructor;
import edu.rice.cs.drjava.model.cache.DocumentCache;

/** In simple terms, a DefaultGlobalModel without an interpreter,compiler, junit testing, debugger or javadoc.
 * Basically, has only document handling functionality
 *  @version $Id$
 */
public class AbstractGlobalModel implements SingleDisplayModel, OptionConstants, DocumentIterator {
  
  /** A document cache that manages how many unmodified documents are open at once. */
  protected DocumentCache _cache;  
  
  static final String DOCUMENT_OUT_OF_SYNC_MSG =
    "Current document is out of sync with the Interactions Pane and should be recompiled!\n";
  
  static final String CLASSPATH_OUT_OF_SYNC_MSG =
    "Interactions Pane is out of sync with the current classpath and should be reset!\n";
  
  // ----- FIELDS -----
  
  /** A list of files that are auxiliary files to the currently open project.  
   *  TODO: make part of FileGroupingState. */
  protected LinkedList<File> _auxiliaryFiles = new LinkedList<File>();
  
  /** Adds a document to the list of auxiliary files.  The LinkedList class is not thread safe, so
   *  the add operation is synchronized.
   */
  public void addAuxiliaryFile(OpenDefinitionsDocument doc) {
    if (! doc.inProject()) {
      File f;
      try { f = doc.getFile(); } 
      catch(FileMovedException fme) { f = fme.getFile(); }
      synchronized(_auxiliaryFiles) { _auxiliaryFiles.add(f); }
      setProjectChanged(true);
    }
  }
  
  /** Removes a document from the list of auxiliary files.  The LinkedList class is not thread safe, so
   *  operations on _auxiliaryFiles are synchronized.
   */
  public void removeAuxiliaryFile(OpenDefinitionsDocument doc) {
    File file;
    try                           { file = doc.getFile(); } 
    catch(FileMovedException fme) { file = fme.getFile(); }
    
    String path = "";
    try { path = file.getCanonicalPath(); }
    catch(IOException e) { throw new UnexpectedException(e); }
    
    synchronized(_auxiliaryFiles) {
      ListIterator<File> it = _auxiliaryFiles.listIterator();
      while (it.hasNext()) {
        try { 
          if (it.next().getCanonicalPath().equals(path)) {
            it.remove();
            setProjectChanged(true);
            break;
          }
        } 
        catch(IOException e) { /* Ignore f */ }
      }
    }
  }
  
  /** Keeps track of all listeners to the model, and has the ability to notify them of some event.  Originally used
   *  a Command Pattern style, but this has been replaced by having EventNotifier directly implement all listener
   *  interfaces it supports.  Set in constructor so that subclasses can install their own notifier with additional 
   *  methods.
   */
  final GlobalEventNotifier _notifier = new GlobalEventNotifier();
  
  // ---- Definitions fields ----
  
  /** Factory for new definitions documents and views.*/
  protected final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit(_notifier);
  
  /** Collection for storing all OpenDefinitionsDocuments. */
  protected final OrderedHashSet<OpenDefinitionsDocument> _documentsRepos =
    new OrderedHashSet<OpenDefinitionsDocument>();
  
  // ---- Input/Output Document Fields ----
  
  /** The document used to display System.out and System.err, and to read from System.in. */
  protected final ConsoleDocument _consoleDoc;
  
  /** The document adapter used in the console document. */
  protected final InteractionsDocumentAdapter _consoleDocAdapter;
  
  /** Indicates whether the model is currently trying to close all documents, and thus that a new one should not be 
   *  created, and whether or not to update the navigator
   */
  protected boolean _isClosingAllDocs;
  
  /** A lock object to prevent print calls to System.out or System.err from flooding the JVM, ensuring the UI 
   *  remains responsive.
   */
  private final Object _systemWriterLock = new Object();
  
  /** Number of milliseconds to wait after each println, to prevent the JVM from being flooded with print calls.
   *  TODO: why is this here, and why is it public?
   */
  public static final int WRITE_DELAY = 5;
  
  /** A PageFormat object for printing to paper. */
  protected PageFormat _pageFormat = new PageFormat();
  
  /** Listens for requests from System.in. */
  private InputListener _inputListener;
  
  /** The active document pointer, which will never be null once the constructor is done.
   *  Maintained by the _gainVisitor with a navigation listener.
   */
  private OpenDefinitionsDocument _activeDocument;
  
  /** A pointer to the active directory, which is not necessarily the parent of the active document
   *  The user may click on a folder component in the navigation pane and that will set this field without
   *  setting the active document.  It is used by the newFile method to place new files into the active directory.
   */
  private File _activeDirectory;
  
  
  /** The abstract container which contains views of open documents and allows user to navigate document focus among
   *  this collection of open documents
   */
  protected IDocumentNavigator _documentNavigator = AWTContainerNavigatorFactory.Singleton.makeListNavigator(); 
  
  
  // ----- CONSTRUCTORS -----
  
  /** Constructs a new GlobalModel. Creates a new MainJVM and starts its Interpreter JVM. */
  public AbstractGlobalModel() {
    _cache = new DocumentCache();
//    _interactionsDocAdapter = new InteractionsDocumentAdapter();
//    _interactionsModel = new DefaultInteractionsModel(this, _interpreterControl,_interactionsDocAdapter);
//    _interactionsModel.addListener(_interactionsListener);
//    _interpreterControl.setInteractionsModel(_interactionsModel);
//    _interpreterControl.setJUnitModel(_junitModel);
//    
//    _interpreterControl.setOptionArgs(DrJava.getConfig().getSetting(JVM_ARGS));
//    DrJava.getConfig().addOptionListener(JVM_ARGS, new OptionListener<String>() {
//      public void optionChanged(OptionEvent<String> oe) {
//        _interpreterControl.setOptionArgs(oe.value);
//      }
//    }); 
    
    _consoleDocAdapter = new InteractionsDocumentAdapter();
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);
    
//    _createDebugger();
    
    _registerOptionListeners();
        
    // Chain notifiers so that all events also go to GlobalModelListeners.
//    _interactionsModel.addListener(_notifier);
//    _compilerModel.addListener(_notifier);
//    _junitModel.addListener(_notifier);
//    _javadocModel.addListener(_notifier);
        
//    // Listen to compiler to clear interactions appropriately.
//    // XXX: The tests need this to be registered after _notifier, sadly.
//    //      This is obnoxiously order-dependent, but it works for now.
//    _compilerModel.addListener(_clearInteractionsListener);
//    
//    // Perhaps do this in another thread to allow startup to continue...
//    _interpreterControl.startInterpreterJVM();
    
    setFileGroupingState(makeFlatFileGroupingState());
    _notifier.projectRunnableChanged();
    _init();
  }
  
  private void _init() {
    final NodeDataVisitor<Boolean> _gainVisitor = new NodeDataVisitor<Boolean>() {
      public Boolean itemCase(INavigatorItem docu) {
        _setActiveDoc(docu);  // sets _activeDocument, the shadow copy of the active document
//        Utilities.showDebug("Setting the active doc done");
        File dir = _activeDocument.getParentDirectory();
        
        if (dir != null) {  
        /* If the file is in External or Auxiliary Files then then we do not want to change our project directory
         * to something outside the project. */
          _activeDirectory = dir;
          _notifier.currentDirectoryChanged(_activeDirectory);
        }
        return Boolean.valueOf(true); 
      }
      public Boolean fileCase(File f) {
        if (! f.isAbsolute()) {
          File root = _state.getProjectFile().getParentFile().getAbsoluteFile();
          f = new File(root, f.getPath());
        }
        _activeDirectory = f;
        _notifier.currentDirectoryChanged(f);
        return Boolean.valueOf(true);
      }
      public Boolean stringCase(String s) { return Boolean.valueOf(false); }
    };
    
    _documentNavigator.addNavigationListener(new INavigationListener() {
      public void gainedSelection(NodeData dat) { dat.execute(_gainVisitor); }
      public void lostSelection(NodeData dat) {
      // not important, only one document selected at a time
      }
    });
    
    _isClosingAllDocs = false;
    _ensureNotEmpty();
    setActiveFirstDocument();
  }
  
  /** Returns a source root given a package and filename. */
  protected File getSourceRoot(String packageName, File sourceFile) throws InvalidPackageException {
    if (packageName.equals("")) {
      return sourceFile.getParentFile();
    }
    
    ArrayList<String> packageStack = new ArrayList<String>();
    int dotIndex = packageName.indexOf('.');
    int curPartBegins = 0;
    
    while (dotIndex != -1) {
      packageStack.add(packageName.substring(curPartBegins, dotIndex));
      curPartBegins = dotIndex + 1;
      dotIndex = packageName.indexOf('.', dotIndex + 1);
    }
    
    // Now add the last package component
    packageStack.add(packageName.substring(curPartBegins));
    
    // Must use the canonical path, in case there are dots in the path
    //  (which will conflict with the package name)
    try {
      File parentDir = sourceFile.getCanonicalFile();
      while (!packageStack.isEmpty()) {
        String part = pop(packageStack);
        parentDir = parentDir.getParentFile();
        if (parentDir == null) throw new RuntimeException("parent dir is null?!");
        
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
  
  // ----- STATE -----
  protected FileGroupingState _state;
  /** Delegates the compileAll command to the _state, a FileGroupingState.
   *  Synchronization is handled by the compilerModel.
   */
  public void compileAll() throws IOException { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
  }
  
  /**
   * @param state the new file grouping state that will handle
   * project specific properties such as the build directory.
   */
  public void setFileGroupingState(FileGroupingState state) {
    _state = state;
    _notifier.projectRunnableChanged();
    _notifier.projectBuildDirChanged();
    _notifier.projectModified();
  }
  
  public FileGroupingState getFileGroupingState() { return _state; }
  
  protected FileGroupingState 
    makeProjectFileGroupingState(File main, File dir, File project, File[] files, ClasspathVector cp) {
    return new ProjectFileGroupingState(main, dir, project, files, cp);
  }
  
  /** Notifies the project state that the project has been changed. */
  public void setProjectChanged(boolean changed) {
//    Utilities.showDebug("Project Changed to " + changed);
    _state.setProjectChanged(changed);
    _notifier.projectModified();
  }
  
  /** @return true if the project state has been changed. */
  public boolean isProjectChanged() {
    return _state.isProjectChanged();
  }
  
  /** @return true if the model has a project open, false otherwise. */
  public boolean isProjectActive() { return _state.isProjectActive(); }
  
  /** @return the file that points to the current project file. Null if not currently in project view
   */
  public File getProjectFile() { return _state.getProjectFile(); }
  
  /** @return all files currently saved as source files in the project file.
   *  If _state not in project mode, returns null
   */
  public File[] getProjectFiles() { return _state.getProjectFiles(); }
  
  /** @return true the given file is in the current project file. */
  public boolean inProject(File f) { return _state.inProject(f); }
  
  /** A file is in the project if the source root is the same as the
   *  project root. this means that project files must be saved at the
   *  source root. (we query the model through the model's state)
   */
  public boolean isInProjectPath(OpenDefinitionsDocument doc) { return _state.isInProjectPath(doc); }
  
  /** Sets the class with the project's main method. */
  public void setJarMainClass(File f) {
    _state.setJarMainClass(f);
    _notifier.projectRunnableChanged();
    setProjectChanged(true);
  }
  
  /** @return the class with the project's main method. */
  public File getMainClass() { return _state.getMainClass(); }
  
  /** throws UnsupportedOperationException */
  public void junitAll() { 
    throw new UnsupportedOperationException("AbstractGlobalDocument does not support unit testing");
  }
  
  /** Sets the class with the project's main method.  Degenerate version overridden in DefaultGlobalModel. */
  public void setBuildDirectory(File f) {
    _state.setBuildDirectory(f);
    if (f != null) 
      throw new UnsupportedOperationException("Cannot create new build directory without an interpreter");
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
  }
  
  /** @return the class with the project's main method. */
  public File getBuildDirectory() { return _state.getBuildDirectory(); }
  
  public void cleanBuildDirectory() throws FileMovedException, IOException {
    _state.cleanBuildDirectory();
  }
  
  /** Helper method used in subsequent anonymous inner class */
  protected static String getPackageName(String classname) {
    int index = classname.lastIndexOf(".");
    if (index != -1) return classname.substring(0, index);
    else return "";
  }
   
  class ProjectFileGroupingState implements FileGroupingState {
    
    File _mainFile;
    File _builtDir; 
    final File projectFile;
    final File[] projectFiles;
    ClasspathVector _projExtraClasspath;
    private boolean _isProjectChanged = false;
    
    //private ArrayList<File> _auxFiles = new ArrayList<File>();
    
    HashSet<String> _projFilePaths = new HashSet<String>();
    
    ProjectFileGroupingState(File main, File dir, File project, File[] files, ClasspathVector cp) {
      _mainFile = main;
      _builtDir = dir;
      projectFile = project;
      projectFiles = files;
      _projExtraClasspath = cp;
      
      try {  for (File file : projectFiles) { _projFilePaths.add(file.getCanonicalPath()); } }
      catch(IOException e) { }
    }
    
    public boolean isProjectActive() { return true; }
    
    /** Determines whether the specified doc in within the project file tree.
     *  No synchronization is required because only immutable data is accessed.
     */
    public boolean isInProjectPath(OpenDefinitionsDocument doc) {
      File projectRoot = projectFile.getParentFile();
      if (doc.isUntitled()) return false;
      
      // If the file does not exist, we still want to tell if it's in the correct
      // path.  The file may have been in at one point and had been removed, in which
      // case we should treat it as an untitled project file that should be resaved.
      File f;
      try { f = doc.getFile(); } 
      catch(FileMovedException fme) { f = fme.getFile(); }
      return isInProjectPath(f);
    }
    
    /** Determines whether the specified file in within the project file tree.
     *  No synchronization is required because only immutable data is accessed.
     */
    public boolean isInProjectPath(File f) {
      try {
        File projectRoot = projectFile.getParentFile();
        String filePath = f.getParentFile().getCanonicalPath() + File.separator;
        String projectPath = projectRoot.getCanonicalPath() + File.separator;
        return (filePath.startsWith(projectPath));
      }
      catch(IOException e) { return false; }
    }
    
    /** @return the absolute path to the project file.  Since projectFile is final, no synchronization
     *  is necessary.
     */
    public File getProjectFile() { return projectFile; }
    
    public boolean inProject(File f) {
      String path;
      
      if (f == null) return false;
      
      try { 
        path = f.getCanonicalPath();
        return _projFilePaths.contains(path);
      }
      catch(IOException ioe) { return false; }
    }
    
    public File[] getProjectFiles() { return projectFiles; }
    
    public File getBuildDirectory() { return _builtDir; }
    
    public void setBuildDirectory(File f) { _builtDir = f; }
    
    public File getMainClass() { return _mainFile; }
    
    public void setJarMainClass(File f) { _mainFile = f; }
    
    public boolean isProjectChanged() { return _isProjectChanged; }
    
    public void setProjectChanged(boolean changed) { _isProjectChanged = changed; }
    
    public boolean isAuxiliaryFile(File f) {
      String path;
      
      if (f == null) return false;
      
      try { path = f.getCanonicalPath();}
      catch(IOException ioe) { return false; }
      
      synchronized(_auxiliaryFiles) {
        for (File file : _auxiliaryFiles) {
          try { if (file.getCanonicalPath().equals(path)) return true; }
          catch(IOException ioe) { /* ignore file */ }
        }
        return false;
      }
    }
    
    public void cleanBuildDirectory() throws FileMovedException, IOException{
      File dir = this.getBuildDirectory();
      cleanHelper(dir);
      if (! dir.exists()) dir.mkdirs();
    }
    
    private void cleanHelper(File f) {
      if (f.isDirectory()) {
        
        File fs[] = f.listFiles(new FilenameFilter() {
          public boolean accept(File parent, String name) {
            return new File(parent, name).isDirectory() || name.endsWith(".class");
          }
        });
        
        for (File kid: fs) { cleanHelper(kid); }
        
        if (f.listFiles().length == 0)  f.delete();
        
      } else if (f.getName().endsWith(".class")) f.delete();
    }
    
    
    /** returns the name of the package from a fully qualified classname. */
    
//      // ----- FIND ALL DEFINED CLASSES IN FOLDER ---
    //throws UnsupportedOperationException
    public void compileAll() throws IOException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
    }
    
    // ----- FIND ALL DEFINED CLASSES IN FOLDER ---
    //throws UnsupportedOperationException
    public void junitAll() {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support JUnit testing");
    }

    public void jarAll() {
      throw new UnsupportedOperationException("AbstractGlobaModel does not support jarring");
    }
    
    public ClasspathVector getExtraClasspath() {
      return _projExtraClasspath;
    }
    
    public void setExtraClasspath(ClasspathVector cp) {
      _projExtraClasspath = cp;
    }
  }
  
  protected FileGroupingState makeFlatFileGroupingState() { return new FlatFileGroupingState(); }
  
  class FlatFileGroupingState implements FileGroupingState {
    public File getBuildDirectory() { return null; }
    public boolean isProjectActive() { return false; }
    public boolean isInProjectPath(OpenDefinitionsDocument doc) { return false; }
    public boolean isInProjectPath(File f) { return false; }
    public File getProjectFile() { return null; }
    public void setBuildDirectory(File f) { }
    public File[] getProjectFiles() { return null; }
    public boolean inProject(File f) { return false; }
    public File getMainClass() { return null; }
    public void setJarMainClass(File f) { }
    public boolean isProjectChanged() { return false; }
    public void setProjectChanged(boolean changed) { /* Do nothing  */  }
    public boolean isAuxiliaryFile(File f) { return false; }
    
    //throws UnsupportedOperationException
    public void compileAll() throws IOException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not suport compilation");
    }
    
    //throws UnsupportedOperationException
    public void junitAll() { 
      throw new UnsupportedOperationException("AbstractGlobalModel does not support unit tests");
    }
    public void cleanBuildDirectory() throws FileMovedException, IOException { }
    
    /** Jars all the open files. 
     throws UnsupportedOperationException */
    public void jarAll() { 
      throw new UnsupportedOperationException("AbstractGlobalModel does not support jarring");
    }
    
    /* Flat grouping states have no extra entries. */
    public ClasspathVector getExtraClasspath() { return new ClasspathVector(); }
    
    public void setExtraClasspath(ClasspathVector cp) {
      throw new UnsupportedOperationException("Flat grouping states do not have extra classpath entries.");
    }
  }
  
  /** Gives the title of the source bin for the navigator.
   *  @return The text used for the source bin in the tree navigator
   */
  public String getSourceBinTitle() { return "[ Source Files ]"; }
  
  /** Gives the title of the external files bin for the navigator
   *  @return The text used for the external files bin in the tree navigator.
   */
  public String getExternalBinTitle() { return "[ External Files ]"; }
  
  /** Gives the title of the aux files bin for the navigator.
   *  @return The text used for the aux files bin in the tree navigator.
   */
  public String getAuxiliaryBinTitle() { return "[ Included External Files ]"; }
  
  // ----- METHODS -----
  
  /** Add a listener to this global model.
   *  @param listener a listener that reacts on events generated by the GlobalModel.
   */
  public void addListener(GlobalModelListener listener) { _notifier.addListener(listener); }
  
  /** Remove a listener from this global model.
   *  @param listener a listener that reacts on events generated by the GlobalModel
   *  This method is synchronized using the readers/writers event protocol incorporated in EventNotifier<T>.
   */
  public void removeListener(GlobalModelListener listener) { _notifier.removeListener(listener); }
  
  // getter methods for the private fields
  
  public DefinitionsEditorKit getEditorKit() { return _editorKit; }
  
  /** throws UnsupportedOperationException */
  public DefaultInteractionsModel getInteractionsModel() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  /** throws UnsupportedOperationException */
  public InteractionsDocumentAdapter getSwingInteractionsDocument() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  /** throws UnsupportedOperationException */
  public InteractionsDocument getInteractionsDocument() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  public ConsoleDocument getConsoleDocument() { return _consoleDoc; }
  
  public InteractionsDocumentAdapter getSwingConsoleDocument() { return _consoleDocAdapter; }
  
  public PageFormat getPageFormat() { return _pageFormat; }
  
  public void setPageFormat(PageFormat format) { _pageFormat = format; }
  
  /** throws UnsupportedOperationException */
  public CompilerModel getCompilerModel() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
  }
  
  /** throws UnsupportedOperationException */
  public JUnitModel getJUnitModel() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support unit testing");
  }
  
  /** throws UnsupportedOperationException */
  public JavadocModel getJavadocModel() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support javadoc");
  }
  
  public IDocumentNavigator getDocumentNavigator() { return _documentNavigator; }
  
  public void setDocumentNavigator(IDocumentNavigator newnav) { _documentNavigator = newnav; }
  
  /** Creates a new open definitions document and adds it to the list.
   *  @return The new open document
   */
  public OpenDefinitionsDocument newFile(File parentDir) {
//    Utilities.showDebug("newFile called with parentDir = " + parentDir);
    final ConcreteOpenDefDoc doc = _createOpenDefinitionsDocument();
    doc.setParentDirectory(parentDir);
    doc.setFile(null);
    addDocToNavigator(doc);
    _notifier.newFileCreated(doc);
    return doc;
  }
  
  /** Creates a new document, adds it to the list of open documents, and sets it to be active.
   *  @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    OpenDefinitionsDocument doc = newFile(_activeDirectory);
    setActiveDocument(doc);
    return doc;
  }
                                               
  /** Creates a new junit test case.
   *  @param name the name of the new test case
   *  @param makeSetUp true iff an empty setUp() method should be included
   *  @param makeTearDown true iff an empty tearDown() method should be included
   *  @return the new open test case
   */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    boolean elementary = (DrJava.getConfig().getSetting(LANGUAGE_LEVEL) == 1);
    
    StringBuffer buf = new StringBuffer();
    if (! elementary) buf.append("import junit.framework.TestCase;\n\n");
    buf.append("/**\n");
    buf.append("* A JUnit test case class.\n");
    buf.append("* Every method starting with the word \"test\" will be called when running\n");
    buf.append("* the test with JUnit.\n");
    buf.append("*/\n");
    if (! elementary) buf.append("public ");
    buf.append("class ");
    buf.append(name);
    buf.append(" extends TestCase {\n\n");
    if (makeSetUp) {
      buf.append("/**\n");
      buf.append("* This method is called before each test method, to perform any common\n");
      buf.append("* setup if necessary.\n");
      buf.append("*/\n");
      if (! elementary) buf.append("public ");
      buf.append("void setUp() {\n}\n\n");
    }
    if (makeTearDown) {
      buf.append("/**\n");
      buf.append("* This method is called after each test method, to perform any common\n");
      buf.append("* clean-up if necessary.\n");
      buf.append("*/\n");
      if (! elementary) buf.append("public ");
      buf.append("void tearDown() {\n}\n\n");
    }
    buf.append("/**\n");
    buf.append("* A test method.\n");
    buf.append("* (Replace \"X\" with a name describing the test.  You may write as\n");
    buf.append("* many \"testSomething\" methods in this class as you wish, and each\n");
    buf.append("* one will be called when running JUnit over this class.)\n");
    buf.append("*/\n");
    if (! elementary) buf.append("public ");
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
  
  /** This method is for use only by test cases. */
  public DocumentCache getDocumentCache() { return _cache; }
  
  //---------------------- Specified by ILoadDocuments ----------------------//
  
  /** Open a file and add it to the pool of definitions documents. The provided file selector chooses a file, 
   *  and on a successful open, the fileOpened() event is fired. This method also checks if there was previously 
   *  a single unchanged, untitled document open, and if so, closes it after a successful opening.
   *  @param com a command pattern command that selects what file to open
   *  @return The open document, or null if unsuccessful
   *  @exception IOException
   *  @exception OperationCanceledException if the open was canceled
   *  @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws 
    IOException, OperationCanceledException, AlreadyOpenException {
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument; 
    OpenDefinitionsDocument openedDoc = openFileHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);
//    Utilities.showDebug("DrJava has opened" + openedDoc + " and is setting it active");
    setActiveDocument(openedDoc);
    setProjectChanged(true);
//    Utilities.showDebug("active doc set; openFile returning"); 
    return openedDoc;
  }
  
  protected OpenDefinitionsDocument openFileHelper(FileOpenSelector com) throws IOException, 
    OperationCanceledException, AlreadyOpenException {
    
    // This code is duplicated in MainFrame._setCurrentDirectory(File) for safety.
    final File file = (com.getFiles())[0].getCanonicalFile();
    OpenDefinitionsDocument odd = _openFile(file);
//    Utilities.showDebug("File " + file + " opened");
    // Make sure this is on the classpath
    try {
      File classpath = odd.getSourceRoot();
      if (odd.inProject() || odd.isAuxiliaryFile())
        throw new UnsupportedOperationException("Cannot add new project files to classPath");
//        _interactionsModel.addProjectFilesClassPath(classpath.toURL());
//      else _interactionsModel.addExternalFilesClassPath(classpath.toURL());
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
    
    return odd;
  }
  
  /** Open multiple files and add them to the pool of definitions documents.  The provided file selector chooses 
  *  a collection of files, and on successfully opening each file, the fileOpened() event is fired.  This method
  *  also checks if there was previously a single unchanged, untitled document open, and if so, closes it after 
  *  a successful opening.
  *  @param com a command pattern command that selects what file
  *            to open
  *  @return The open document, or null if unsuccessful
  *  @exception IOException
  *  @exception OperationCanceledException if the open was canceled
  *  @exception AlreadyOpenException if the file is already open
  */
  public OpenDefinitionsDocument openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument;

    OpenDefinitionsDocument openedDoc = openFilesHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);
    setActiveDocument(openedDoc);
    return openedDoc;
  }
  
  protected OpenDefinitionsDocument openFilesHelper(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    final File[] files = com.getFiles();
    if (files == null) { throw new IOException("No Files returned from FileSelector"); }
    OpenDefinitionsDocument doc = _openFiles(files);
    return doc;
  }
  
  // if set to true, and uncommented, the definitions document will
  // print out a small stack trace every time getDocument() is called
  
  //    static boolean SHOW_GETDOC = false; 
  
  /** Opens all the files in the list, and notifies about the last file opened. */
  private OpenDefinitionsDocument _openFiles(File[] files) 
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    AlreadyOpenException storedAOE = null;
    OpenDefinitionsDocument retDoc = null;
    
    //        SHOW_GETDOC = true;
    
    LinkedList<File> filesNotFound = new LinkedList<File>();
    final LinkedList<OpenDefinitionsDocument> filesOpened = new LinkedList<OpenDefinitionsDocument>();
    for (final File f: files) {
      if (f == null) throw new IOException("File name returned from FileSelector is null");
      try {
        //always return last opened Doc
        retDoc = _rawOpenFile(f.getAbsoluteFile());
        filesOpened.add(retDoc);
      }
      catch (AlreadyOpenException aoe) {
        retDoc = aoe.getOpenDocument();
        //Remember the first AOE
        if (storedAOE == null) storedAOE = aoe;
      } 
      catch(FileNotFoundException e) { filesNotFound.add(f); }
    }
    
    for (final OpenDefinitionsDocument d: filesOpened) {
      addDocToNavigator(d);
      addDocToClasspath(d);
      _notifier.fileOpened(d);
    }
    
    //        SHOW_GETDOC = false;
    for (File f: filesNotFound) { _notifier.fileNotFound(f); }
    
    if (storedAOE != null) throw storedAOE;
    
    if (retDoc != null) return retDoc;
    else {
      //if we didn't open any files, then it's just as if they cancelled it...
      throw new OperationCanceledException();
    }
  }
  
  
  //----------------------- End ILoadDocuments Methods -----------------------//
  
  /** Opens all files in the specified folder dir and places them in the appropriate places in the document navigator.
   *  If "open folders recursively" is checked, this operation opens all files in the subtree rooted at dir.
   */
  public void openFolder(File dir, boolean rec) throws IOException, OperationCanceledException, AlreadyOpenException {
    if (dir == null) return; // just in case
  
    ArrayList<File> files;
    if (dir.isDirectory()) {
      files = FileOps.getFilesInDir(dir, rec, new FileFilter() {
        public boolean accept(File f) { 
          return f.isDirectory() ||
            f.isFile() && 
            f.getName().endsWith(DrJava.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)]);
        }
      });
      
      if (isProjectActive())
        Collections.sort(files, new Comparator<File>() {
        public int compare(File o1,File o2) {
          return - o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
      });
      else
        Collections.sort(files, new Comparator<File>() {
        public int compare(File o1,File o2) {
          return - o1.getName().compareTo(o2.getName());
        }
      });
      
      int ct = files.size();
      
      final File[] sfiles = files.toArray(new File[ct]);
      
      openFiles(new FileOpenSelector() { public File[] getFiles() { return sfiles; } });
      
      if (ct > 0 && _state.isInProjectPath(dir)) setProjectChanged(true);
    }
  }
  
  
  /** Saves all open files, prompting for names if necessary.
   *  When prompting (i.e., untitled document), set that document as active.
   *  @param com a FileSaveSelector
   *  @exception IOException
   */
   public void saveAllFiles(FileSaveSelector com) throws IOException {
     OpenDefinitionsDocument curdoc = getActiveDocument();
     saveAllFilesHelper(com);
     setActiveDocument(curdoc); // Return focus to previously active doc
   }
  
  /** Called by saveAllFiles in DefaultGlobalModel */
  protected void saveAllFilesHelper(FileSaveSelector com) throws IOException {
    
    OpenDefinitionsDocument[] docs;
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (final OpenDefinitionsDocument doc: docs) {
      aboutToSaveFromSaveAll(doc);
      doc.saveFile(com);
    }
  }
  
  /** Writes the project file to disk
   *  @param filename where to save the project
   */
  public void saveProject(String filename, Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> info) 
    throws IOException {
    
    ProjectFileBuilder builder = new ProjectFileBuilder(filename);
    
    // add opendefinitionsdocument
    ArrayList<File> srcFileList = new ArrayList<File>();
    LinkedList<File> auxFileList = new LinkedList<File>();
    
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (OpenDefinitionsDocument doc: docs) {
      if (! doc.isUntitled()) {
        // could not use doc.isInProjectPath because we may be in flat file view which returns false
        String projectPath = new File(filename).getParentFile().getCanonicalPath() + File.separator;
        String filePath = doc.getFile().getParentFile().getCanonicalPath() + File.separator;
        if (filePath.startsWith(projectPath)) {
          DocumentInfoGetter g = info.get(doc);
          builder.addSourceFile(g);
          srcFileList.add(g.getFile());
        }
        else if (doc.isAuxiliaryFile()) {
          DocumentInfoGetter g = info.get(doc);
          builder.addAuxiliaryFile(g);
          auxFileList.add(g.getFile());
        }
      }
    } 
      
    // add collapsed path info
    if (_documentNavigator instanceof JTreeSortNavigator) {
      String[] paths = ((JTreeSortNavigator)_documentNavigator).getCollapsedPaths();
      for (String s : paths) { builder.addCollapsedPath(s); }
    }
    
    // add classpath info
//    Vector<File> currentclasspaths = DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH);
//    for (File f: currentclasspaths) { builder.addClasspathFile(f); }
    // New behavior: only save project-specific classpaths.
    ClasspathVector exCp = getProjectExtraClasspath();
    if (exCp != null) {
      Vector<File> exCpF = exCp.asFileVector();
      for (File f : exCpF) {
        builder.addClasspathFile(f);
        //System.out.println("Saving project classpath entry " + f);
      }
    } else {
      //System.err.println("Project ClasspathVector is null!");
    }
    
    // add build directory
    File d = getBuildDirectory();
    if (d != null) builder.setBuildDirectory(d);
    
    // add jar main class
    File mainClass = getMainClass();
    if (mainClass != null) builder.setMainClass(mainClass);
    
    // write to disk
    builder.write();
    
    // set the state if all went well
    File[] srcFiles = srcFileList.toArray(new File[srcFileList.size()]);
    
    synchronized(_auxiliaryFiles) {
      _auxiliaryFiles = auxFileList;
    }
    
    setFileGroupingState(makeProjectFileGroupingState(mainClass, d, new File(filename), srcFiles, exCp));
  }
  
  /** Parses the given project file, sets up the state and other configurations such as the Navigator and the
   *  classpath, and returns an array of files to open.  This version is degnerate; it does not reset the
   *  interactions pane.
   *  @param projectFile The project file to parse
   *  @return an array of document's files to open
   */
  public File[] openProject(File projectFile) throws IOException, MalformedProjectFileException {
    
    final ProjectFileIR ir = ProjectFileParser.ONLY.parse(projectFile);
    
    final DocFile[] srcFiles = ir.getSourceFiles();
    final DocFile[] auxFiles = ir.getAuxiliaryFiles();
    final File buildDir = ir.getBuildDirectory();
    final File mainClass = ir.getMainClass();
    final File[] projectClasspaths = ir.getClasspaths();
    
    final String projfilepath = projectFile.getCanonicalPath();
    
    final List<OpenDefinitionsDocument> oldProjDocs = getProjectDocuments();
    final FileGroupingState oldState = _state;
    
//    Utilities.showDebug("openProject called with file " + projectFile);
    
    // Sets up the filters that cause documents to load in differentnsections of the tree.  The names of these 
    // sections are set from the methods such as getSourceBinTitle().  Changing this changes what is considered 
    // source, aux, and external.
    
    List<Pair<String, INavigatorItemFilter>> l = new LinkedList<Pair<String, INavigatorItemFilter>>();
    l.add(new Pair<String, INavigatorItemFilter>(getSourceBinTitle(), new INavigatorItemFilter() {
      public boolean accept(INavigatorItem n) {
        OpenDefinitionsDocument d = (OpenDefinitionsDocument) n;
        return d.isInProjectPath();
      }
    }));
    
    l.add(new Pair<String, INavigatorItemFilter>(getAuxiliaryBinTitle(), new INavigatorItemFilter() {
      public boolean accept(INavigatorItem n) {
        OpenDefinitionsDocument d =  (OpenDefinitionsDocument) n;
        return d.isAuxiliaryFile();
      }
    }));
    
    l.add(new Pair<String, INavigatorItemFilter>(getExternalBinTitle(), new INavigatorItemFilter() {
      public boolean accept(INavigatorItem n) {
        OpenDefinitionsDocument d = (OpenDefinitionsDocument) n;
        return !(d.inProject() || d.isAuxiliaryFile()) || d.isUntitled();
      }
    }));
    
    IDocumentNavigator newNav = 
      AWTContainerNavigatorFactory.Singleton.makeTreeNavigator(projfilepath, getDocumentNavigator(), l);
    
    setDocumentNavigator(newNav);
    
    synchronized(_auxiliaryFiles) {
      _auxiliaryFiles.clear();
      for (File file: auxFiles) { _auxiliaryFiles.add(file); }
    }
    
    ClasspathVector extraClasspaths = new ClasspathVector();
    for (File f : projectClasspaths) { extraClasspaths.add(f); }
    
    setFileGroupingState(makeProjectFileGroupingState(mainClass, buildDir, projectFile, srcFiles, extraClasspaths));
    
    ArrayList<File> projFiles = new ArrayList<File>();
    File active = null;
    for (DocFile f: srcFiles) {
      File file = f;
      if (f.lastModified() > f.getSavedModDate()) file = new File(f.getPath());
      if (f.isActive() && active == null) active = file;
      else projFiles.add(file);
    }
    for (DocFile f: auxFiles) {
      File file = f;
      if (f.lastModified() > f.getSavedModDate()) file = new File(f.getPath());
      if (f.isActive() && active == null) active = file;
      else projFiles.add(file);
    }
    // Insert active file as last file on list.
    if (active != null) projFiles.add(active); 
    
//    Utilities.showDebug("Project files are: " + projFiles);
    
    final List<OpenDefinitionsDocument> projDocs = getProjectDocuments();  // opened documents in the project source tree 
    
    // Keep all nonproject files open other than a new file (untitled).  External files in the previous 
    // project may become project files in the new project and must be closed while external files in the 
    // previous project that are still external to the new project should be kept open (except for a new file).
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        for (OpenDefinitionsDocument d: projDocs) {
          if (oldState.inProject(d.file())) closeFile(d);
          else {
            try {
              final INavigatorItem idoc = d;
              final String path = fixPathForNavigator(d.getFile().getCanonicalPath());
              _documentNavigator.refreshDocument(idoc, path);  // this operation must run in event thread
            }
            catch(IOException e) { 
              /* Do nothing; findbugs signals a bug unless this catch clause spans more than two lines */ 
            }
          }
        }
      }
    });
//    Utilities.showDebug("Preparing to refresh navigator GUI");
    // call on the GUI to finish up by opening the files and making necessary gui component changes
    final File[] filesToOpen = projFiles.toArray(new File[projFiles.size()]);
    _notifier.projectOpened(projectFile, new FileOpenSelector() {
      public File[] getFiles() { return filesToOpen; }
    });
    
    if (_documentNavigator instanceof JTreeSortNavigator) {
      ((JTreeSortNavigator)_documentNavigator).collapsePaths(ir.getCollapsedPaths());
    }
   
    resetInteractions(); // Since the classpath is most likely changed.  Clears out test pane as well.
    
    return srcFiles; // Unnecessarily returns src files in keeping with the previous interface.
  }
  
  /** Performs any needed operations on the model before closing the project and its files.  This is not 
   *  responsible for actually closing the files since that is handled in MainFrame._closeProject().
   *  This version is degenerate; it does not reset the interactions pane.
   */
  public void closeProject() {
    setDocumentNavigator(AWTContainerNavigatorFactory.Singleton.makeListNavigator(getDocumentNavigator()));
    setFileGroupingState(makeFlatFileGroupingState());

    // Reset rather than telling the user to reset. This was a design decision
    // made by the class Spring 2005 after much debate.
   
    resetInteractions();
    _notifier.projectClosed();
  }
  
  /** If the document is untitled, brings it to the top so that the
   *  user will know which is being saved.
   */
  public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {
    if (doc.isUntitled()) setActiveDocument(doc);
  }
  
  /** Closes an open definitions document, prompting to save if the document has been changed.  Returns whether
   *  the file was successfully closed.  Also ensures the invariant that there is always at least
   *  one open document holds by creating a new file if necessary.
   *  @return true if the document was closed
   */
   public boolean closeFile(OpenDefinitionsDocument doc) {
     List<OpenDefinitionsDocument> list = new LinkedList<OpenDefinitionsDocument>();
     list.add(doc);
     return closeFiles(list);
   }
  
  /** Attempts to close all open documents. Also ensures the invariant that there is always at least
    *  one open document holds by creating a new file if necessary.
    //Bug when the first document, in list view, is selected:
    //When "close all" documents is selected, each document in turn is set active
    //Fix: close the currently active document last
    * @return true if all documents were closed
    */
   public boolean closeAllFiles() {
     List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
     return closeFiles(docs);
   }
  
  /** This function closes a group of files assuming that the files are contiguous in the enumeration
   *  provided by the document navigator. This assumption is used in selecting which remaining document
   *  (if any) to activate.
   *  <p>
   *  The corner cases in which the file that is being closed had been externally
   *  deleted have been addressed in a few places, namely DefaultGlobalModel.canAbandonFile()
   *  and MainFrame.ModelListener.canAbandonFile().  If the DefinitionsDocument for the 
   *  OpenDefinitionsDocument being closed is not in the cache (see model.cache.DocumentCache)
   *  then it is closed without prompting the user to save it.  If it is in the cache, then
   *  we can successfully notify the user that the file is selected for closing and ask whether to
   *  saveAs, close, or cancel.
   *  @param docList the list od OpenDefinitionsDocuments to close
   *  @param together if true then no files will be closed if not all can be abandoned
   *  @return whether all files were closed
   * 
   * Question: what is the together flag for?  How does it affect observable behavior?
   */
  public boolean closeFiles(List<OpenDefinitionsDocument> docList) {
    if (docList.size() == 0) return true;
    
    /* Force the user to save or discard all modified files in docList */
    for (OpenDefinitionsDocument doc : docList) { if (!doc.canAbandonFile()) return false; }
    
    // If all files are being closed, create a new file before starTing in order to have 
    // an active file that is not in the list of closing files.
    OpenDefinitionsDocument newDoc = null;
    if (docList.size() == getOpenDefinitionsDocumentsSize()) newDoc = newFile();
    
    // Set the active document to the document just after the last document or the document just before the 
    // first document in docList.
    _ensureNotActive(docList);
        
    // Close the files in docList. 
    for (OpenDefinitionsDocument doc : docList) { closeFileWithoutPrompt(doc); }  
    return true;
  }
  
  /** Helper for closeFile. This method was the closeFile(...) method before projects were added to DrJava. */
  protected boolean closeFileHelper(OpenDefinitionsDocument doc) {
    //    System.err.println("closing " + doc);
    boolean canClose = doc.canAbandonFile();
    if (canClose) return closeFileWithoutPrompt(doc);
    return false;
  }
  /** Similar to closeFileHelper except that saving cannot be cancelled. */
  protected void closeFileOnQuitHelper(OpenDefinitionsDocument doc) {
    //    System.err.println("closing " + doc);
    doc.quitFile();
    closeFileWithoutPrompt(doc);
  }
  
  /** Closes an open definitions document, without prompting to save if the document has been changed.  Returns
   *  whether the file was successfully closed. NOTE: This method should not be called unless it can be 
   *  absolutely known that the document being closed is not the active document. The closeFile() method in 
   *  SingleDisplayModel ensures that a new active document is set, but closeFileWithoutPrompt is not.
   *  @return true if the document was closed.
   */
  public boolean closeFileWithoutPrompt(final OpenDefinitionsDocument doc) {
    //    new Exception("Closed document " + doc).printStackTrace();
    
    boolean found;
    synchronized(_documentsRepos) { found = _documentsRepos.remove(doc); }
    
    if (! found) return false;
    
    Utilities.invokeLater(new Runnable() { 
      public void run() { _documentNavigator.removeDocument(doc); }   // this operation must run in event thread
    });
    _notifier.fileClosed(doc);
    doc.close();
    return true;
  }
  
  /** Closes all open documents without creating a new empty document.  It cannot be cancelled by the user
   *  because it would leave the current project in an inconsistent state.  It does Method is public for 
   *  testing purposes.
   */
  public void closeAllFilesOnQuit() {
    
    OpenDefinitionsDocument[] docs;
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    
    for (OpenDefinitionsDocument doc : docs) {
      closeFileOnQuitHelper(doc);  // modifies _documentsRepos
    }
  }
    
  /** Exits the program.  Quits regardless of whether all documents are successfully closed. */
  public void quit() {
    closeAllFilesOnQuit();
    dispose();  // kills the interpreter
    
//    if (DrJava.getSecurityManager() != null) DrJava.getSecurityManager().exitVM(0);
//    else 
      System.exit(0); // If we are being debugged by another copy of DrJava,
    // then we have no security manager.  Just exit cleanly.
  }

  /** Prepares this model to be thrown away.  Never called in practice outside of quit(), except in tests. 
   *  This version does not kill the interpreter. */
  public void dispose() {
    
    _notifier.removeAllListeners();
    synchronized(_documentsRepos) { _documentsRepos.clear(); }
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { _documentNavigator.clear(); }  // this operation must run in event thread
    });
  }

  
  //----------------------- Specified by IGetDocuments -----------------------//

  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException {
    // Check if this file is already open
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null) {
      // If not, open and return it
      final File f = file;
      FileOpenSelector selector = 
        new FileOpenSelector() { public File[] getFiles() { return new File[] {f}; } };
      try { doc = openFile(selector);}
      catch (AlreadyOpenException e) { doc = e.getOpenDocument(); }
      catch (OperationCanceledException e) { throw new UnexpectedException(e); /* Cannot happen */ }
    }
    return doc;
  }

  /** Iterates over OpenDefinitionsDocuments, looking for this file.
   *  TODO: This is not very efficient!
   */
  public boolean isAlreadyOpen(File file) { return (_getOpenDocument(file) != null); }

  /** Returns the OpenDefinitionsDocument corresponding to the INavigatorItem/DefinitionsDocument passed in.
   *  @param doc the searched for Document
   *  @return its corresponding OpenDefinitionsDocument
   */
  public OpenDefinitionsDocument getODDForDocument(AbstractDocumentInterface doc) {
    /** This function needs to be phased out altogether; the goal is for the OpenDefinitionsDocument 
     *  to also function as its own Document, so this function will be useless
     */
    if (doc instanceof OpenDefinitionsDocument) return (OpenDefinitionsDocument) doc;
    if  (doc instanceof DefinitionsDocument) return ((DefinitionsDocument) doc).getOpenDefDoc();
    throw new IllegalStateException("Could not get the OpenDefinitionsDocument for Document: " + doc);
  }

  /** Gets a DocumentIterator to allow navigating through open Swing Documents. */
  public DocumentIterator getDocumentIterator() { return this; }

  /** Returns the ODD preceding the given document in the document list.
   *  @param d the current Document
   *  @return the next Document
   */
  public OpenDefinitionsDocument getNextDocument(AbstractDocumentInterface d) {
    OpenDefinitionsDocument nextdoc = null; // irrelevant initialization required by javac
//    try {
      OpenDefinitionsDocument doc = getODDForDocument(d);
      nextdoc = _documentNavigator.getNext(doc);
      if (nextdoc == doc) nextdoc = _documentNavigator.getFirst();  // wrap around if necessary
      OpenDefinitionsDocument res = getNextDocHelper(nextdoc);
//      Utilities.showDebug("nextDocument(" + d + ") = " + res);
      return res;
//    } 
//    catch(DocumentClosedException dce) { return getNextDocument(nextdoc); }
  }
  
  private OpenDefinitionsDocument getNextDocHelper(OpenDefinitionsDocument nextdoc) {
    if (nextdoc.isUntitled() || nextdoc.verifyExists()) return nextdoc;
    // Note: verifyExists prompts user for location of the file if it is not found
    
    // cannot find nextdoc; move on to next document 
    return getNextDocument(nextdoc);
  }

  /** Returns the ODD preceding the given document in the document list.
   *  @param d the current Document
   *  @return the previous Document
   */
  public OpenDefinitionsDocument getPrevDocument(AbstractDocumentInterface d) {
    OpenDefinitionsDocument prevdoc = null;  // irrelevant initialization required by javac
//    try {
      OpenDefinitionsDocument doc = getODDForDocument(d);
      prevdoc = _documentNavigator.getPrevious(doc);
      if (prevdoc == doc) prevdoc = _documentNavigator.getLast(); // wrap around if necessary
      return getPrevDocHelper(prevdoc);
//    }
//    catch(DocumentClosedException dce) { return getPrevDocument(prevdoc); }
  }
  
  private OpenDefinitionsDocument getPrevDocHelper(OpenDefinitionsDocument prevdoc) { 
    if (prevdoc.isUntitled() || prevdoc.verifyExists()) return prevdoc;
    // Note: verifyExists() prompts user for location of prevdoc
    
    // cannot find prevdoc; move on to preceding document 
    return getPrevDocument(prevdoc);
  }

  public int getDocumentCount() { return _documentsRepos.size(); }
  
  /** Returns a new collection of all documents currently open for editing.
   *  This is equivalent to the results of getDocumentForFile for the set
   *  of all files for which isAlreadyOpen returns true.
   *  @return a random-access List of the open definitions documents.
   * 
   *  This essentially duplicates the method valuesArray() in OrderedHashSet.
   */
  public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments() {
    synchronized(_documentsRepos) {
      ArrayList<OpenDefinitionsDocument> docs = new ArrayList<OpenDefinitionsDocument>(_documentsRepos.size());
      for (OpenDefinitionsDocument doc: _documentsRepos) { docs.add(doc); }
      return docs;
    }
  }
  
  /** @return the size of the collection of OpenDefinitionsDocuments */
  public int getOpenDefinitionsDocumentsSize() { return _documentsRepos.size(); }
  
//  public OpenDefinitionsDocument getODDGivenIDoc(INavigatorItem idoc) {
//    synchronized(_documentsRepos) { return _documentsRepos.getValue(idoc); }
//  } 
  
//  public INavigatorItem getIDocGivenODD(OpenDefinitionsDocument odd) {
//    synchronized(_documentsRepos) { return _documentsRepos.getKey(odd); }
//  }
  
  //----------------------- End IGetDocuments Methods -----------------------//
  
  /**
   * Set the indent tab size for all definitions documents.
   * @param indent the number of spaces to make per level of indent
   */
  void setDefinitionsIndent(int indent) {
    
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
      
    for (OpenDefinitionsDocument doc: docs) { doc.setIndent(indent); }
  }

  /** A degenerate operation since this has no interactions model. */
  public void resetInteractions() { /* do nothing */ }

  /** Resets the console. Fires consoleReset() event. */
  public void resetConsole() {
    _consoleDoc.reset();
    _notifier.consoleReset();
  }

  /** throw new UnsupportedOperationException */
  public void interpretCurrentInteraction() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector) throws
    IOException, OperationCanceledException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public void clearHistory() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /**
   * Returns the entire history as a String with semicolons as needed
   */
  public String getHistoryAsStringWithSemicolons() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** throws UnsupportedOperationException */
  public String getHistoryAsString() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }

  /** Registers OptionListeners.  Factored out code from the two constructor. */
  private void _registerOptionListeners() {
//    // Listen to any relevant config options
//    DrJava.getConfig().addOptionListener(EXTRA_CLASSPATH,
//                                         new ExtraClasspathOptionListener());

    DrJava.getConfig().addOptionListener(BACKUP_FILES,
                                         new BackUpFileOptionListener());
    Boolean makeBackups = DrJava.getConfig().getSetting(BACKUP_FILES);
    FileOps.DefaultFileSaver.setBackupsEnabled(makeBackups.booleanValue());

//    DrJava.getConfig().addOptionListener(ALLOW_PRIVATE_ACCESS,
//                                         new OptionListener<Boolean>() {
//      public void optionChanged(OptionEvent<Boolean> oce) {
//        getInteractionsModel().setPrivateAccessible(oce.value.booleanValue());
//      }
//    });
  }

  /** Appends a string to the given document using a particular attribute set.
   *  Also waits for a small amount of time (WRITE_DELAY) to prevent any one
   *  writer from flooding the model with print calls to the point that the
   *  user interface could become unresponsive.
   *  @param doc Document to append to
   *  @param s String to append to the end of the document
   *  @param style the style to print with
   */
  protected void _docAppend(ConsoleDocument doc, String s, String style) {
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


  /** Prints System.out to the DrJava console. */
  public void systemOutPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_OUT_STYLE);
  }

  /** Prints System.err to the DrJava console. */
  public void systemErrPrint(String s) { _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_ERR_STYLE); }

  /** throws UnsupportedOperationException */
  public void printDebugMessage(String s) {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
  }


  /** throw new UnsupportedOperationException */
  public void waitForInterpreter() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }


  /** throws new UnsupportedOperationException */
  public ClasspathVector getClasspath() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support classPaths");
  }
  
  /** Returns only the project's extra classpaths.
   *  @return The classpath entries loaded along with the project
   */
  public ClasspathVector getProjectExtraClasspath() {
    return _state.getExtraClasspath();
  }
  
  /** Sets the set of classpath entries to use as the projects set of classpath entries.  This is normally used by the
   *  project preferences..
   */
  public void setProjectExtraClasspath(ClasspathVector cp) {
    _state.setExtraClasspath(cp);
    //System.out.println("Setting project classpath to: " + cp);
  }

  /** Gets an array of all sourceRoots for the open definitions documents, without duplicates. Note that if any of
   *  the open documents has an invalid package statement, it won't be adde to the source root set. On 8.7.02 
   *  changed the sourceRootSet such that the directory DrJava was executed from is now after the sourceRoots
   *  of the currently open documents in order that whatever version the user is looking at corresponds to the
   *  class file the interactions window uses.
   * TODO: Fix out of date comment, possibly remove this here?
   */
  public File[] getSourceRootSet() {
    LinkedList<File> roots = new LinkedList<File>();
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs =  _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (OpenDefinitionsDocument doc: docs) {
      try {
        File root = doc.getSourceRoot();
        if (!roots.contains(root)) { roots.add(root); } // Don't add duplicate Files, based on path
      }
      catch (InvalidPackageException e) { /* file has invalid package statement; ignore it */ }
    }
    return roots.toArray(new File[roots.size()]);
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
      if (extIndex > 0) filename = filename.substring(0, extIndex);
    }
    
    // Mark if modified
    if (doc.isModifiedSinceSave()) filename = filename + "*";
    
    return filename;
  }

  /** Return the absolute path of the file with the given index, or "(untitled)" if no file exists. */
  public String getDisplayFullPath(int index) {
    OpenDefinitionsDocument doc = getOpenDefinitionsDocuments().get(index);
    if (doc == null) throw new RuntimeException( "Document not found with index " + index);
    return GlobalModelNaming.getDisplayFullPath(doc);
  }
   
//  /** Sets whether or not the Interactions JVM will be reset after a compilation succeeds.  This should ONLY be used 
//   *  in tests!
//   *  @param shouldReset Whether to reset after compiling
//   */
//  void setResetAfterCompile(boolean shouldReset) { _resetAfterCompile = shouldReset; }

  /** throws UnsupportedOperationException */
  public Debugger getDebugger() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
  }

  /** throws UnsupportedOperationException */
  public int getDebugPort() throws IOException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
  }

  /**
   * Checks if any open definitions documents have been modified
   * since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments() {
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (OpenDefinitionsDocument doc: docs) { 
      if (doc.isModifiedSinceSave()) return true;  
    }
    return false;
  }
  
  /**
   * Checks if any open definitions documents are untitled.
   * @return whether any documents are untitled
   */
  public boolean hasUntitledDocuments() {
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (OpenDefinitionsDocument doc: docs) { 
      if (doc.isUntitled()) return true;  
    }
    return false;
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
    for (File s: sourceRoots) {
      File f = _getSourceFileFromPath(filename, s);
      if (f != null) return f;
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
    for (File p: paths) {
      File f = _getSourceFileFromPath(filename, p);
      if (f != null) return f;
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
  
  /** Throws UnsupportedOperationException */
  public void jarAll() { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support jarring documents");
  }
  
  private static int ID_COUNTER = 0; /* Seed for assigning id numbers to OpenDefinitionsDocuments */
  // ---------- ConcreteOpenDefDoc inner class ----------

  /** Inner class to handle operations on each of the open DefinitionsDocuments by the GlobalModel. <br><br>
   *  This was at one time called the <code>DefinitionsDocumentHandler</code>
   *  but was renamed (2004-Jun-8) to be more descriptive/intuitive.
   */
  class ConcreteOpenDefDoc implements OpenDefinitionsDocument, AbstractDocumentInterface {
    
    private int _id;
    private DrJavaBook _book;
    protected Vector<Breakpoint> _breakpoints;
    
//     private boolean _modifiedSinceSave;
    
    private File _file;
    private long _timestamp;
    
    /** The folder containing this document */
    private File _parentDir;  

    protected String _packageName = null;
    
    private int _initVScroll;
    private int _initHScroll;
    private int _initSelStart;
    private int _initSelEnd;
    
    private DCacheAdapter _cacheAdapter;

    /** Standard constructor for a document read from a file.  Initializes this ODD's DD.
     *  @param f file describing DefinitionsDocument to manage
     */
    ConcreteOpenDefDoc(File f) throws IOException {
      if (! f.exists()) throw new FileNotFoundException("file " + f + " cannot be found");
      
      _file = f;
      _parentDir = f.getParentFile();
      _timestamp = f.lastModified();
      init();
    }
    
    /* Standard constructor for a new document (no associated file) */
    ConcreteOpenDefDoc() {
      _file = null;
      _parentDir = null;
      init();
    }
    
    public void init() {
      _id = ID_COUNTER++;
      
      try {
//        System.out.println("about to make reconstructor " + this);
        DDReconstructor ddr = makeReconstructor();
//        System.out.println("finished making reconstructor " + this);
        _cacheAdapter = _cache.register(this, ddr);
      } catch(IllegalStateException e) { throw new UnexpectedException(e); }

      _breakpoints = new Vector<Breakpoint>();
    }
    
    /** Getter for document id; used to sort documents into creation order */
    public int id() { return _id; }
    
    /** Sets the parent directory of the document only if it is "Untitled"
     *  @param pd The parent directory
     */
    public void setParentDirectory(File pd) {
      if (_file != null) 
        throw new IllegalArgumentException("The parent directory can only be set for untitled documents");
      _parentDir = pd;  
    }
    
    /** Get the parent directory of this document
     *  @return The parent directory
     */
    public File getParentDirectory() { return _parentDir; }
    
    /** A file is in the project if the source root is the same as the
     *  project root. this means that project files must be saved at the
     *  source root. (we query the model through the model's state)
     */
    public boolean isInProjectPath() { return _state.isInProjectPath(this); }

        
    /** A file is in the project if the source root is the same as the
     *  project root. this means that project files must be saved at the
     *  source root. (we query the model through the model's state)
     */
    public boolean inProject() { return ! isUntitled() && _state.inProject(_file); }
    
    /** @return true if the file is an auxiliary file. */
    public boolean isAuxiliaryFile() { return ! isUntitled() && _state.isAuxiliaryFile(_file); }
    
    /** Makes a default DDReconstructor that will make a Document based on if the ODD has a file or not. */
    protected DDReconstructor makeReconstructor() {
      return new DDReconstructor() {
        
        // Brand New documents start at location 0
        private int _loc = 0;
        // Start out with empty list for the very first time the document is made
        private DocumentListener[] _list = { };
        
//        private CompoundUndoManager _undo = null;
//        
        private UndoableEditListener[] _undoListeners = { };

        private List<FinalizationListener<DefinitionsDocument>> _finalListeners =
          new LinkedList<FinalizationListener<DefinitionsDocument>>();
        
        public DefinitionsDocument make() throws IOException, BadLocationException, FileMovedException {
          DefinitionsDocument tempDoc;
          tempDoc = new DefinitionsDocument(_notifier);
          tempDoc.setOpenDefDoc(ConcreteOpenDefDoc.this);
                 
          if (_file != null) {
            FileReader reader = new FileReader(_file);
            _editorKit.read(reader, tempDoc, 0);
            reader.close(); // win32 needs readers closed explicitly!
          }
          _loc = Math.min(_loc, tempDoc.getLength()); // make sure not past end
          _loc = Math.max(_loc, 0); // make sure not less than 0
          tempDoc.setCurrentLocation(_loc);
          for (DocumentListener d : _list) {
            if (d instanceof DocumentUIListener) tempDoc.addDocumentListener(d);
          }
          for (UndoableEditListener l: _undoListeners) { tempDoc.addUndoableEditListener(l); }
          for (FinalizationListener<DefinitionsDocument> l: _finalListeners) {
            tempDoc.addFinalizationListener(l);
          }

          tempDoc.resetModification();  // Why is this necessary? A reconstructed document is already unmodified.

          //            tempDoc.setUndoManager(_undo);
          assert ! tempDoc.isModifiedSinceSave();
          try { _packageName = tempDoc.getPackageName(); } 
          catch(InvalidPackageException e) { _packageName = null; }
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
          for (DocumentListener l: _list) { if (dl != l) tmp.add(l); }
          tmp.add(dl);
          _list = tmp.toArray(new DocumentListener[tmp.size()]);
        }
        public String toString() { return ConcreteOpenDefDoc.this.toString(); }
      };
    }
    
    public int getInitialVerticalScroll()   { return _initVScroll; }
    public int getInitialHorizontalScroll() { return _initHScroll; }
    public int getInitialSelectionStart()   { return _initSelStart; }
    public int getInitialSelectionEnd()     { return _initSelEnd; }
    
    void setPackage(String pack)   { _packageName = pack; }
    void setInitialVScroll(int i)  { _initVScroll = i; }
    void setInitialHScroll(int i)  { _initHScroll = i; }
    void setInitialSelStart(int i) { _initSelStart = i; }
    void setInitialSelEnd(int i)   { _initSelEnd = i; }
      
    /** Originally designed to allow undoManager to set the current document to be modified whenever an undo
     *  or redo is performed.  Now it actually does this.
     */
    public void updateModifiedSinceSave() { getDocument().updateModifiedSinceSave(); }

    /** Gets the definitions document being handled.
     *  @return document being handled
     */
    protected DefinitionsDocument getDocument() {

//      Utilities.showDebug("getDocument() called on " + this);
      try { return _cacheAdapter.getDocument(); } 
      catch(IOException ioe) { // document has been moved or deleted
//        Utilities.showDebug("getDocument() failed for " + this);
        try {
          _notifier.documentNotFound(this, _file);
          final String path = fixPathForNavigator(getFile().getCanonicalFile().getCanonicalPath());
          Utilities.invokeAndWait(new Runnable() {
            public void run() { _documentNavigator.refreshDocument(ConcreteOpenDefDoc.this, path); }
          });
          return _cacheAdapter.getDocument(); 
        }
        catch(Throwable t) { throw new UnexpectedException(t); }
      }
    }

    /** Returns the name of the top level class, if any.
     *  @throws ClassNameNotFoundException if no top level class name found.
     */
    public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
      return getDocument().getFirstTopLevelClassName();
    }

    /** Returns whether this document is currently untitled (indicating whether it has a file yet or not).
     *  @return true if the document is untitled and has no file
     */
    public boolean isUntitled() { return _file == null; }

    /** Returns the file for this document.  If the document is untitled and has no file, it throws an 
     *  IllegalStateException. If the document's file does not exist, this throws a FileMovedException. If you 
     *  still want the file, you can retrieve it from the FileMovedException by using the getFile() method.
     *  EACH TIME YOU CALL THIS METHOD, YOU SHOULD HAVE A CHECK FIRST TO isUntitled() IN ORDER TO AVOID THE 
     *  IllegalStateException.
     *  @return the file for this document
     *  @exception IllegalStateException if no file exists
     */
    public File getFile() throws IllegalStateException , FileMovedException {
      
        if (_file == null) throw new IllegalStateException("This document does not yet have a file.");
        if (_file.exists()) return _file;
        else throw new FileMovedException(_file, "This document's file has been moved or deleted.");
    }
    
    /** Returns true if the file exists on disk. Returns false if the file has been moved or deleted */
    public boolean fileExists() { return _file != null && _file.exists(); }
    
    
    /** Pure getter for _file; does not check for null. */
    public File file() { return _file; }
    
    /** Returns true if the file exists on disk. Prompts the user otherwise */
    public boolean verifyExists() {
//      Utilities.showDebug("verifyExists called on " + _file);
      if (fileExists()) return true;
      //prompt the user to find it
      try {
        _notifier.documentNotFound(this, _file);
        String path = fixPathForNavigator(getFile().getCanonicalPath());
        _documentNavigator.refreshDocument(this, path);
        return true;
      } 
      catch(Throwable t) { return false; }
//      catch(DocumentFileClosed e) { /* not clear what to do here */ }
    }

    /** Returns the name of this file, or "(untitled)" if no file. */
    public String getFilename() {
      if (_file == null) return "(Untitled)";
      return _file.getName();
    }

    /** Returns the name of the file for this document with an appended asterisk (if modified) or spaces */
    public String getName() {
      String filename = getFilename();
      if (isModifiedSinceSave()) filename = filename + "*";
      else filename = filename + "  ";  // forces the cell renderer to allocate space for an appended "*"
      return filename;
    }

    /** Saves the document with a FileWriter.  If the file name is already set, the method will use 
     *  that name instead of whatever selector is passed in.
     *  @param com a selector that picks the file name if the doc is untitled
     *  @exception IOException
     *  @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFile(FileSaveSelector com) throws IOException {
      FileSaveSelector realCommand;
      final File file;
      
      if (!isModifiedSinceSave() && !isUntitled()) return true;
        // Don't need to save; return true, since the save wasn't "canceled"
      
      try {
        if (isUntitled()) realCommand = com;
        else
          try {
          file = getFile();
          realCommand = new TrivialFSS(file);
        }
        catch (FileMovedException fme) {
          // getFile() failed, prompt the user if a new one should be selected
          if (com.shouldSaveAfterFileMoved(this, fme.getFile())) realCommand = com;
          else return false;
            // User declines to save as a new file, so don't save 
        }
        return saveFileAs(realCommand);
      }
      catch (IllegalStateException ise) {
        // No file--  this should have been caught by isUntitled()
        throw new UnexpectedException(ise);
      }
    }

    /** Saves the document with a FileWriter.  The FileSaveSelector will either provide a file name or prompt the 
     *  user for one.  It is up to the caller to decide what needs to be done to choose a file to save to.  Once 
     *  the file has been saved succssfully, this method fires fileSave(File).  If the save fails for any
     *  reason, the event is not fired. This is synchronized against the compiler model to prevent saving and
     *  compiling at the same time- this used to freeze drjava.
     *  @param com a selector that picks the file name.
     *  @throws IOException if the save fails due to an IO error
     *  @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFileAs(FileSaveSelector com) throws IOException {
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile();
        OpenDefinitionsDocument otherDoc = _getOpenDocument(file);
        boolean shouldSave = false;
        boolean openInOtherDoc = ((otherDoc != null) && (openDoc != otherDoc));
        // Check if file is already open in another document
        if (openInOtherDoc) shouldSave = com.warnFileOpen(file);
          // Can't save over an open document
        
        // If the file exists, make sure it's ok to overwrite it
        if ((shouldSave && openInOtherDoc) || 
            (!openInOtherDoc && (!file.exists() || com.verifyOverwrite()))) {

          // Correct the case of the filename (in Windows)
          if (! file.getCanonicalFile().getName().equals(file.getName())) file.renameTo(file);
          
          // Check for # in the path of the file because if there
          // is one, then the file cannot be used in the Interactions Pane
          if (file.getAbsolutePath().indexOf("#") != -1) _notifier.filePathContainsPound();
          
          // have FileOps save the file
          FileOps.saveFile(new FileOps.DefaultFileSaver(file) {
            public void saveTo(OutputStream os) throws IOException {
              DefinitionsDocument doc = getDocument();
              try { _editorKit.write(os, doc, 0, doc.getLength()); } 
              catch (BadLocationException docFailed) { throw new UnexpectedException(docFailed); }
            }
          });
          
          resetModification();
          setFile(file);
          
          try {
            // This calls getDocument().getPackageName() because this may be untitled and this.getPackageName() 
            // returns "" if it's untitled.  Right here we are interested in parsing the DefinitionsDocument's text
            _packageName = getDocument().getPackageName();
          } 
          catch(InvalidPackageException e) { _packageName = null; }
          getDocument().setCachedClassFile(null);
          checkIfClassFileInSync();
          
//          Utilities.showDebug("ready to fire fileSaved for " + this); 
          _notifier.fileSaved(openDoc);
          
          // Make sure this file is on the appropriate classpaths (does nothing in AbstractGlobalModel)
          addDocToClasspath(this);
          
          /* update the navigator */
          _documentNavigator.refreshDocument(this, fixPathForNavigator(file.getCanonicalPath()));
        }
        return true;
      }
      catch (OperationCanceledException oce) {
        // Thrown by com.getFile() if the user cancels.
        //   We don't save if this happens.
        return false;
      }
    }

    
    /** Whenever this document has been saved, this method should be called so that it knows it's no longer in
     *  a modified state.
     */
    public void resetModification() {
      getDocument().resetModification();
      if (_file != null) _timestamp = _file.lastModified();
    }
    
    /** Sets the file for this openDefinitionsDocument. */
    public void setFile(File file) {
      _file = file;
//      resetModification();
      //jim: maybe need lock
      if (_file != null) _timestamp = _file.lastModified();
    }
    
    /** Returns the timestamp. */
    public long getTimestamp() { return _timestamp; }
    
    /** This method tells the document to prepare all the DrJavaBook and PagePrinter objects. */
    public void preparePrintJob() throws BadLocationException, FileMovedException {
      String filename = "(Untitled)";
      try { filename = getFile().getAbsolutePath(); }
      catch (IllegalStateException e) { /* do nothing */ }

      _book = new DrJavaBook(getDocument().getText(), filename, _pageFormat);
    }

    /** Prints the given document by bringing up a "Print" window. */
    public void print() throws PrinterException, BadLocationException, FileMovedException {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      if (printJob.printDialog()) printJob.print();
      cleanUpPrintJob();
    }

    /** Returns the Pageable object for printing.
     *  @return A Pageable representing this document.
     */
    public Pageable getPageable() throws IllegalStateException { return _book; }
    
    /** Clears the pageable object used to hold the print job. */
    public void cleanUpPrintJob() { _book = null; }

    /** throws UnsupportedOperationException */
    public void startCompile() throws IOException { 
      throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
    }

    /** throws UnsupportedOperationException */
    public void runMain() throws IOException, ClassNameNotFoundException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support running");
    }

    /** throws UnsupportedOperationException */
    public void startJUnit() throws IOException, ClassNotFoundException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support unit testing");
    }

    /** throws UnsupportedOperationException */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support javadoc");
    }
    
    /** Determines if the document has been modified since the last save.
     *  @return true if the document has been modified
     */
    public boolean isModifiedSinceSave() {
      /* If the document has not been registered or it is virtualized (only stored on disk), then we know that
       * it is not modified. This method can be called by debugging code (via getName() on a 
       * ConcreteOpenDefDoc) before the document has been registered (_cacheAdapter == null). */
      if (_cacheAdapter != null && _cacheAdapter.isReady()) return getDocument().isModifiedSinceSave();
      else return false;
    }
    
    public void documentSaved() { _cacheAdapter.documentSaved(getFilename()); }
    
    public void documentModified() { _cacheAdapter.documentModified(); }
    
    public void documentReset() { _cacheAdapter.documentReset(); }
    
    /** Determines if the file for this document has been modified since it was loaded.
     *  @return true if the file has been modified
     */
    public boolean isModifiedOnDisk() {
      boolean ret = false;
      try {
        getDocument().aquireReadLock();
        if (_file != null) ret = (_file.lastModified() > _timestamp);
      }
      finally { getDocument().releaseReadLock(); }
      return ret;
    }
    
    /** If this document is unmodified, this method examines the class file corresponding to this document
     *  and compares the timestamps of the class file to that of the source file.
     */
    public boolean checkIfClassFileInSync() {
      // If modified, then definitely out of sync
      if (isModifiedSinceSave()) {
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
      catch (IllegalStateException ise) { throw new UnexpectedException(ise); }
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

    /** Returns the class file for this source document by searching the source roots of open documents, the
     *  system classpath, and the "extra.classpath".  Returns null if the class file could not be found.
     */
    private File _locateClassFile() {
      try {
        String className = getDocument().getQualifiedClassName();
        String ps = System.getProperty("file.separator");
        // replace periods with the System's file separator
        className = StringOps.replace(className, ".", ps);
        String filename = className + ".class";

        // Check source root set (open files)
        File[] sourceRoots = { };
        Vector<File> roots = new Vector<File>();
        
        if (getBuildDirectory() != null) roots.add(getBuildDirectory());
        
        // Add the current document to the beginning of the roots list
        try { roots.add(getSourceRoot()); }
        catch (InvalidPackageException ipe) {
          try {
            File f = getFile().getParentFile();
            if (f != null) roots.add(f);
          }
          catch (IllegalStateException ise) { /* No file; do nothing */ }
          catch (FileMovedException fme) {
            // Moved, but we'll add the old file to the set anyway
            File root = fme.getFile().getParentFile();
            if (root != null)  roots.add(root);
          }
        }

        for (int i = 0; i < sourceRoots.length; i++) roots.add(sourceRoots[i]);
        
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

    /** Determines if the definitions document has been changed by an outside agent. If the document has changed,
     *  asks the listeners if the GlobalModel should revert the document to the most recent version saved.
     *  @return true if document has been reverted
     */
    public boolean revertIfModifiedOnDisk() throws IOException{
      final OpenDefinitionsDocument doc = this;
      if (isModifiedOnDisk()) {
        boolean shouldRevert = _notifier.shouldRevertFile(doc);
        if (shouldRevert) doc.revertFile();
        return shouldRevert;
      }
      return false;
    }
    
    /* Degenerate version of close; does not remove breakpoints in this document */
    public void close() {
      removeFromDebugger();
      _cacheAdapter.close();
    }

    public void revertFile() throws IOException {

      //need to remove old, possibly invalid breakpoints
      removeFromDebugger();

      final OpenDefinitionsDocument doc = this;

      try {
        File file = doc.getFile();
        //this line precedes .remove() so that an invalid file is not cleared before this fact is discovered.

        FileReader reader = new FileReader(file);
        doc.clear();

        _editorKit.read(reader, doc, 0);
        reader.close(); // win32 needs readers closed explicitly!

        resetModification();
        doc.checkIfClassFileInSync();
        setCurrentLocation(0);
        _notifier.fileReverted(doc);
      }
      catch (IllegalStateException docFailed) { throw new UnexpectedException(docFailed); }
      catch (BadLocationException docFailed) { throw new UnexpectedException(docFailed); }
    }

    /** Asks the listeners if the GlobalModel can abandon the current document.  Fires the canAbandonFile(File)
     *  event if isModifiedSinceSave() is true.
     *  @return true if the current document can be abandoned, false if the current action should be halted in 
     *               its tracks (e.g., file open when the document has been modified since the last save).
     */
    public boolean canAbandonFile() {
      if (isModifiedSinceSave() || (_file != null && !_file.exists() && _cacheAdapter.isReady()))
        return _notifier.canAbandonFile(this);
      else return true;
    }
    
    /** Fires the quit(File) event if isModifiedSinceSave() is true.  The quitFile() event asks the user if the
     *  the file should be saved before quitting.
     */
    public void quitFile() {
      if (isModifiedSinceSave() || (_file != null && !_file.exists() && _cacheAdapter.isReady()))
        _notifier.quitFile(this);
    }
    
    /** Moves the definitions document to the given line, and returns the resulting character position.
     * @param line Destination line number. If it exceeds the number of lines in the document, it is 
     *             interpreted as the last line.
     * @return Index into document of where it moved
     */
    public int gotoLine(int line) {
      getDocument().gotoLine(line);
      return getDocument().getCurrentLocation();
    }

    /** Forwarding method to sync the definitions with whatever view component is representing them. */
    public void setCurrentLocation(int location) { getDocument().setCurrentLocation(location); }

    /**
     * Get the location of the cursor in the definitions according
     * to the definitions document.
     */
    public int getCurrentLocation() { return getDocument().getCurrentLocation(); }

    /**
     * Forwarding method to find the match for the closing brace
     * immediately to the left, assuming there is such a brace.
     * @return the relative distance backwards to the offset before
     *         the matching brace.
     */
    public int balanceBackward() { return getDocument().balanceBackward(); }

    /**
     * Forwarding method to find the match for the open brace
     * immediately to the right, assuming there is such a brace.
     * @return the relative distance forwards to the offset after
     *         the matching brace.
     */
    public int balanceForward() { return getDocument().balanceForward(); }

    /** throws UnsupportedOperationException */
    public Breakpoint getBreakpointAt(int offset) {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support debugger");
    }

    /** throws UnsupportedOperationException */
    public void addBreakpoint( Breakpoint breakpoint) {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support debugger");
    }
    
    /** throws UnsupportedOperationException */
    public void removeBreakpoint(Breakpoint breakpoint) {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support debugger");
    }
    
    /** throws UnsupportedOperationException */
    public Vector<Breakpoint> getBreakpoints() {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support debugger");
    }
    
    /** throws UnsupportedOperationException */
    public void clearBreakpoints() { 
      throw new UnsupportedOperationException("AbstractGlobalModel does not support debugger");
    }
    
   /** throws UnsupportedOperationException */
    public void removeFromDebugger() { /* do nothing because it is called in methods in this class */ }    
    
    /** Finds the root directory of the source files.
     *  @return The root directory of the source files, based on the package statement.
     *  @throws InvalidPackageException if the package statement is invalid,
     *  or if it does not match up with the location of the source file.
     */
    public File getSourceRoot() throws InvalidPackageException {
      if (_packageName == null) _packageName = getPackageName();
      return _getSourceRoot(_packageName);
    }
    
    /** Gets the name of the package this source file claims it's in (with the package keyword). 
     *  It does this by minimally parsing the source file to find the package statement.
     *
     *  @return The name of package this source file declares itself to be in,
     *          or the empty string if there is no package statement (and thus
     *          the source file is in the empty package).
     *
     *  @exception InvalidPackageException if there is some sort of a
     *                                    <TT>package</TT> statement but it
     *                                    is invalid.
     */
    public String getPackageName() throws InvalidPackageException {
      if (isUntitled()) _packageName = "";
      else if (_packageName == null) _packageName = getDocument().getPackageName();
      return _packageName;
    }
    
    /** Finds the root directory of the source files.
     *  @param packageName Package name, already fetched from the document
     *  @return The root directory of the source files based on the package statement.
     *  @throws InvalidPackageException If the package statement is invalid, or if it does not match up with the
     *          location of the source file.
     */
    File _getSourceRoot(String packageName) throws InvalidPackageException {
      File sourceFile;
      try { sourceFile = getFile();  }
      catch (IllegalStateException ise) {
        throw new InvalidPackageException(-1, "Can not get source root for unsaved file. Please save.");
      }
      catch (FileMovedException fme) {
        throw new 
          InvalidPackageException(-1, "File has been moved or deleted from its previous location. Please save.");
      }
      
      if (packageName.equals("")) { return sourceFile.getParentFile(); }
      
      ArrayList<String> packageStack = new ArrayList<String>();
      int dotIndex = packageName.indexOf('.');
      int curPartBegins = 0;
      
      while (dotIndex != -1) {
        packageStack.add(packageName.substring(curPartBegins, dotIndex));
        curPartBegins = dotIndex + 1;
        dotIndex = packageName.indexOf('.', dotIndex + 1);
      }
      
      // Now add the last package component
      packageStack.add(packageName.substring(curPartBegins));
      
      // Must use the canonical path, in case there are dots in the path
      //  (which will conflict with the package name)
      try {
        File parentDir = sourceFile.getCanonicalFile();
        while (!packageStack.isEmpty()) {
          String part = pop(packageStack);
          parentDir = parentDir.getParentFile();

          if (parentDir == null) throw new RuntimeException("parent dir is null?!");

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
    
    public String toString() { return getFilename(); }
    
    /** Orders ODDs by their id's. */
    public int compareTo(OpenDefinitionsDocument o) { return _id - o.id(); }
    
    /** Implementation of the javax.swing.text.Document interface. */
    public void addDocumentListener(DocumentListener listener) {
      if (_cacheAdapter.isReady()) getDocument().addDocumentListener(listener);
      else _cacheAdapter.getReconstructor().addDocumentListener(listener);
    }
    
    List<UndoableEditListener> _undoableEditListeners = new LinkedList<UndoableEditListener>();
    
    public void addUndoableEditListener(UndoableEditListener listener) {
      _undoableEditListeners.add(listener);
      getDocument().addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
      _undoableEditListeners.remove(listener);
      getDocument().removeUndoableEditListener(listener);
    }
    
    public UndoableEditListener[] getUndoableEditListeners() {
      return getDocument().getUndoableEditListeners();
    }
    
    public Position createPosition(int offs) throws BadLocationException {
      return getDocument().createPosition(offs);
    }
    
    public Element getDefaultRootElement() { return getDocument().getDefaultRootElement(); }
    
    public Position getEndPosition() { return getDocument().getEndPosition(); }
    
    public int getLength() { return getDocument().getLength(); }
    
    public Object getProperty(Object key) { return getDocument().getProperty(key); }
    
    public Element[] getRootElements() { return getDocument().getRootElements(); }
    
    public Position getStartPosition() { return getDocument().getStartPosition(); }
    
    public String getText(int offset, int length) throws BadLocationException {
      return getDocument().getText(offset, length);
    }
    
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
      getDocument().getText(offset, length, txt);
    }
    
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
      getDocument().insertString(offset, str, a);
    }
    
    public void append(String str, AttributeSet set) { getDocument().append(str, set); }
    
    public void append(String str, Style style) { getDocument().append(str, style); }
    
    public void putProperty(Object key, Object value) { getDocument().putProperty(key, value); }
    
    public void remove(int offs, int len) throws BadLocationException { getDocument().remove(offs, len); }
    
    public void removeDocumentListener(DocumentListener listener) {
      getDocument().removeDocumentListener(listener);
    }
    
    public void render(Runnable r) { getDocument().render(r); }
    
    /** End implementation of javax.swing.text.Document interface. */
    
    /** If the undo manager is unavailable, no undos are available
     *  @return whether the undo manager can perform any undo's
     */
    public boolean undoManagerCanUndo() { return _cacheAdapter.isReady() && getUndoManager().canUndo(); }
    /**
     * If the undo manager is unavailable, no redos are available
     * @return whether the undo manager can perform any redo's
     */
    public boolean undoManagerCanRedo() { return _cacheAdapter.isReady() && getUndoManager().canRedo(); }
    
    /** Decorater patter for the definitions document. */
    public CompoundUndoManager getUndoManager() { return getDocument().getUndoManager(); }
    
    public int getLineStartPos(int pos) { return getDocument().getLineStartPos(pos); }
    
    public int getLineEndPos(int pos) { return getDocument().getLineEndPos(pos); }
    
    public int commentLines(int selStart, int selEnd) { return getDocument().commentLines(selStart, selEnd); }
    
    public int uncommentLines(int selStart, int selEnd) {
      return getDocument().uncommentLines(selStart, selEnd);
    }
    
    public void indentLines(int selStart, int selEnd) { getDocument().indentLines(selStart, selEnd); }
    
    public int getCurrentCol() { return getDocument().getCurrentCol(); }
    
    public boolean getClassFileInSync() { return getDocument().getClassFileInSync(); }
    
    public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
      return getDocument().getIntelligentBeginLinePos(currPos);
    }
    
    public int getOffset(int lineNum) { return getDocument().getOffset(lineNum); }
    
    public String getQualifiedClassName() throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName();
    }
    
    public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName(pos);
    }
    
    public ReducedModelState getStateAtCurrent() { return getDocument().getStateAtCurrent(); }
    
    public void resetUndoManager() {
      // if it's not in the cache, the undo manager will be 
      // reset when it's reconstructed
      if (_cacheAdapter.isReady()) getDocument().resetUndoManager();
    }
      
    public File getCachedClassFile() { return getDocument().getCachedClassFile(); }
    
    public DocumentListener[] getDocumentListeners() { return getDocument().getDocumentListeners(); }
    
    //--------- DJDocument methods ----------
    
    public void setTab(String tab, int pos) { getDocument().setTab(tab,pos); }
    
    public int getWhiteSpace() { return getDocument().getWhiteSpace(); }
    
    public boolean posInParenPhrase(int pos) { return getDocument().posInParenPhrase(pos); }
    
    public boolean posInParenPhrase() { return getDocument().posInParenPhrase(); }
    
    public int findPrevNonWSCharPos(int pos) throws BadLocationException {
      return getDocument().findPrevNonWSCharPos(pos);
    }
    
    public int getFirstNonWSCharPos(int pos) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos);
    }
    
    public int getFirstNonWSCharPos(int pos, boolean acceptComments) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos, acceptComments);
    }
    
    public int getFirstNonWSCharPos (int pos, char[] whitespace, boolean acceptComments) 
      throws BadLocationException {
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
    
    public void indentLines(int selStart, int selEnd, int reason, ProgressMonitor pm) 
      throws OperationCanceledException {
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
    
    public void resetReducedModelLocation() { getDocument().resetReducedModelLocation(); }
    
    public ReducedModelState stateAtRelLocation(int dist) { return getDocument().stateAtRelLocation(dist); }
    
    public IndentInfo getIndentInformation() { return getDocument().getIndentInformation(); }
    
    public void move(int dist) { getDocument().move(dist); }
    
    public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
      return getDocument().getHighlightStatus(start, end);
    }
    
    public void setIndent(int indent) { getDocument().setIndent(indent); }
    
    public int getIndent() { return getDocument().getIndent(); }
    
    //-----------------------
    
    /** This method is put here because the ODD is the only way to get to the defdoc. */
    public void addFinalizationListener(FinalizationListener<DefinitionsDocument> fl) {
      getDocument().addFinalizationListener(fl);
    }
    
    public List<FinalizationListener<DefinitionsDocument>> getFinalizationListeners() {
      return getDocument().getFinalizationListeners();
    }
    
    // Styled Document Methods 
    public Font getFont(AttributeSet attr) { return getDocument().getFont(attr); }
    
    public Color getBackground(AttributeSet attr) { return getDocument().getBackground(attr); }
    
    public Color getForeground(AttributeSet attr) { return getDocument().getForeground(attr); }
    
    public Element getCharacterElement(int pos) { return getDocument().getCharacterElement(pos); }
    
    public Element getParagraphElement(int pos) { return getDocument().getParagraphElement(pos); }
    
    public Style getLogicalStyle(int p) { return getDocument().getLogicalStyle(p); }
    
    public void setLogicalStyle(int pos, Style s) { getDocument().setLogicalStyle(pos, s); }
    
    public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
      getDocument().setCharacterAttributes(offset, length, s, replace); 
    }
    
    public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
      getDocument().setParagraphAttributes(offset, length, s, replace); 
    }
    
    public Style getStyle(String nm) { return getDocument().getStyle(nm); }
    
    public void removeStyle(String nm) { getDocument().removeStyle(nm); }
    
    public Style addStyle(String nm, Style parent) { return getDocument().addStyle(nm, parent); }
    
    public String getText() {
      DefinitionsDocument doc = getDocument();
      doc.acquireReadLock();
      try { return doc.getText(0, doc.getLength()); }
      catch(BadLocationException e) { throw new UnexpectedException(e); }
      finally { releaseReadLock(); }
    }
    
    public void clear() {
      DefinitionsDocument doc = getDocument();
      doc.acquireWriteLock();
      try { doc.remove(0, doc.getLength()); }
      catch(BadLocationException e) { throw new UnexpectedException(e); }
      finally { releaseWriteLock(); }
    }
    
    
    /* Locking operations in DJDocument interface */
    
    /** Swing-style readLock(). */
    public void acquireReadLock() { getDocument().readLock(); }
    
    /** Swing-style readUnlock(). */
    public void releaseReadLock() { getDocument().readUnlock(); }
    
    /** Swing-style writeLock(). */
    public void acquireWriteLock() { getDocument().acquireWriteLock(); }
    
    /** Swing-style writeUnlock(). */
    public void releaseWriteLock() { getDocument().releaseWriteLock(); }
    
  } /* End of ConcreteOpenDefDoc */

  
  
  private static class TrivialFSS implements FileSaveSelector {
    private File _file;
    private TrivialFSS(File file) { _file = file; }
    public File getFile() throws OperationCanceledException { return _file; }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite() { return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
  }
  
  /** Creates a ConcreteOpenDefDoc for a new DefinitionsDocument.
   *  @return OpenDefinitionsDocument object for a new document
   */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument() { return new ConcreteOpenDefDoc(); }
  
  /** Creates a ConcreteOpenDefDoc for a given file f
   *  @return OpenDefinitionsDocument object for f
   */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(File f) throws IOException { return new ConcreteOpenDefDoc(f); }
  
  /** Returns the OpenDefinitionsDocument corresponding to the given  File, or null if that file is not open.
   *  @param file File object to search for
   *  @return Corresponding OpenDefinitionsDocument, or null
   */
  protected OpenDefinitionsDocument _getOpenDocument(File file) {
    
    OpenDefinitionsDocument[] docs;
    
    synchronized(_documentsRepos) { docs = _documentsRepos.toArray(new OpenDefinitionsDocument[0]); }
    for (OpenDefinitionsDocument doc: docs) {
      try {
        File thisFile = null;
        try { thisFile = doc.getFile(); }
        catch (FileMovedException fme) { thisFile = fme.getFile(); } // File is invalid, but compare anyway
        finally {
          // Always do the comparison
          if (thisFile != null) {
            try {
              // Compare canonical paths if possible
              if (thisFile.getCanonicalFile().equals(file.getCanonicalFile())) return doc;
            }
            catch (IOException ioe) {
              // Can be thrown from getCanonicalFile.
              //  If so, compare the files themselves
              if (thisFile.equals(file)) return doc;
            }
          }
        }
      }
      catch (IllegalStateException ise) { /* No file in doc; fail silently */ }
    }
    return null;
  }

  public List<OpenDefinitionsDocument> getNonProjectDocuments() {
    List<OpenDefinitionsDocument> allDocs = getOpenDefinitionsDocuments();
    List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument tempDoc : allDocs) {
      if (!tempDoc.isInProjectPath()) projectDocs.add(tempDoc);
    }
    return projectDocs;
  }
  
  /** Returns the OpenDefinitionsDocuments that are located in the project source tree. */
  public List<OpenDefinitionsDocument> getProjectDocuments() {
    List<OpenDefinitionsDocument> allDocs = getOpenDefinitionsDocuments();
    List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument tempDoc : allDocs)
      if (tempDoc.isInProjectPath() || tempDoc.isAuxiliaryFile()) projectDocs.add(tempDoc);
    return projectDocs;
  }
  /* Extracts relative path (from project origin) to parent of file identified by path.  Assumes path does not end in 
   * File.separator. */
  public String fixPathForNavigator(String path) throws IOException {
    path = path.substring(0, path.lastIndexOf(File.separator));
    String _topLevelPath;
    if (getProjectFile() != null) {
      _topLevelPath = getProjectFile().getCanonicalPath();
      _topLevelPath = _topLevelPath.substring(0, _topLevelPath.lastIndexOf(File.separator));;
    }
    else _topLevelPath = "";
    
    if (!path.equals(_topLevelPath) && !path.startsWith(_topLevelPath + File.separator))
      /** it's in external files, so don't give it a path */
      return "";
    else {
      path = path.substring(_topLevelPath.length());
      return path;
    }
  }
  
  /** Creates an OpenDefinitionsDocument for a file. Does not add to the navigator or notify that the file's open.
   *  This method should be called only from within another open method that will do all of this clean up.
   *  @param file the file to open
   */
  private OpenDefinitionsDocument _rawOpenFile(File file) throws IOException, AlreadyOpenException{
    OpenDefinitionsDocument openDoc = _getOpenDocument(file);
    if (openDoc != null) throw new AlreadyOpenException(openDoc);
    final ConcreteOpenDefDoc doc = _createOpenDefinitionsDocument(file);
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
  
  /** This pop method enables an ArrayList to serve as stack. */
  protected static <T> T pop(ArrayList<T> stack) { return stack.remove(stack.size() - 1); }
  
  /** Creates an iNavigatorItem for a document, and adds it to the navigator. A helper for opening a file or creating
   *  a new file.
   *  @param doc the document to add to the navigator
   */
  protected void addDocToNavigator(final OpenDefinitionsDocument doc) {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        try {
          if (doc.isUntitled()) _documentNavigator.addDocument(doc);
          else {
            String path = doc.getFile().getCanonicalPath();
            _documentNavigator.addDocument(doc, fixPathForNavigator(path)); 
          }
        }
        catch(IOException e) { _documentNavigator.addDocument(doc); }
      }});
      synchronized(_documentsRepos) { _documentsRepos.add(doc); }
  }
  
  /** Does nothing; overridden in DefaultGlobalModel. */
  protected void addDocToClasspath(OpenDefinitionsDocument doc) { /* do nothing */ }
  
  /** Creates a document from a file.
   *  @param file File to read document from
   *  @return openened document
   */
  private OpenDefinitionsDocument _openFile(File file) throws IOException, AlreadyOpenException {
    
    OpenDefinitionsDocument doc = _rawOpenFile(file);
    addDocToNavigator(doc);
    addDocToClasspath(doc);
    _notifier.fileOpened(doc);
    return doc;
  }
  
  private static class BackUpFileOptionListener implements OptionListener<Boolean> {
    public void optionChanged (OptionEvent<Boolean> oe) {
      Boolean value = oe.value;
      FileOps.DefaultFileSaver.setBackupsEnabled(value.booleanValue());
    }
  }

//----------------------- SingleDisplay Methods -----------------------//

  /** Returns the currently active document. */
  public OpenDefinitionsDocument getActiveDocument() {return  _activeDocument; }
  
  
  /** Sets the currently active document by updating the selection model.
   *  @param doc Document to set as active
   */
  public void setActiveDocument(final OpenDefinitionsDocument doc) {
    /* The following code fixes a potential race because this method modifies the documentNavigator which is a swing
     * component. Hence it must run in the event thread.  Note that setting the active document triggers the execution
     * of listeners some of which also need to run in the event thread. */
    
//    if (_activeDocument == doc) return; // this optimization appears to cause some subtle bugs 
//    Utilities.showDebug("DEBUG: Called setActiveDocument()");
    
    Runnable command = new Runnable() {  
      public void run() {_documentNavigator.setActiveDoc(doc);} 
    };
    try {Utilities.invokeAndWait(command); }  // might be relaxed to invokeLater
    catch(Exception e) { throw new UnexpectedException(e); } 
  }
  
  public Container getDocCollectionWidget() { return _documentNavigator.asContainer(); }
  
  /** Sets the active document to be the next one in the collection. */
  public void setActiveNextDocument() {
    INavigatorItem key = _activeDocument;
    OpenDefinitionsDocument nextKey = (OpenDefinitionsDocument) _documentNavigator.getNext(key);
    if (key != nextKey) setActiveDocument(nextKey);
    else setActiveDocument((OpenDefinitionsDocument)_documentNavigator.getFirst());
    /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
  }

  /** Sets the active document to be the previous one in the collection. */
  public void setActivePreviousDocument() {
    INavigatorItem key = _activeDocument;
    OpenDefinitionsDocument prevKey = (OpenDefinitionsDocument) _documentNavigator.getPrevious(key);
    if (key != prevKey) setActiveDocument(prevKey);
    else setActiveDocument((OpenDefinitionsDocument)_documentNavigator.getLast());
      /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
  }
//
//  /**
//   * Returns whether we are in the process of closing all documents.
//   * (Don't want to prompt the user to revert files that have become
//   * modified on disk if we're just closing everything.)
//   * TODO: Move to DGM?  Make private?
//   */
//  public boolean isClosingAllFiles() {
//    return _isClosingAllDocs;
//  }

  //----------------------- End SingleDisplay Methods -----------------------//

  
  
  /**
   * Returns whether there is currently only one open document
   * which is untitled and unchanged.
   */
  private boolean _hasOneEmptyDocument() {
    return getOpenDefinitionsDocumentsSize() == 1 && _activeDocument.isUntitled() &&
            ! _activeDocument.isModifiedSinceSave();
  }

  /** Creates a new document if there are currently no documents open. */
  private void _ensureNotEmpty() {
    if ((!_isClosingAllDocs) && (getOpenDefinitionsDocumentsSize() == 0)) newFile(null);
  }
  
  /** Makes sure that none of the documents in the list are active. */
  private void _ensureNotActive(List<OpenDefinitionsDocument> docs) {
    if (docs.contains(getActiveDocument())) {
      // Find the one that should be the new active document
      IDocumentNavigator nav = getDocumentNavigator();
      
      INavigatorItem item = docs.get(docs.size()-1);
      OpenDefinitionsDocument nextActive = (OpenDefinitionsDocument) nav.getNext(item);
      if (!nextActive.equals(item)) {
        setActiveDocument(nextActive); 
        return;
      }
      
      item = docs.get(0);
      nextActive = (OpenDefinitionsDocument) nav.getPrevious(item);
      if (!nextActive.equals(item)) { 
        setActiveDocument(nextActive);
        return;
      }
      
      throw new RuntimeException("No document to set active before closing");
    }
  }
  
  /** Sets the first document in the navigator as active. */
  public void setActiveFirstDocument() {
    List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
    /* The follwoing will select the active document in the navigator, which will signal a listener to call _setActiveDoc(...)
     */
    setActiveDocument(docs.get(0));
  }
  
  private synchronized void _setActiveDoc(INavigatorItem idoc) {
      _activeDocument = (OpenDefinitionsDocument) idoc;  // FIX THIS!
      refreshActiveDocument();
  }
  
  /** Invokes the activeDocumentChanged method in the global listener on the argument _activeDocument.  This process sets up
   *  _activeDocument as the document in the definitions pane.  It is also necessary after an "All Documents" search that wraps
   *  around. */
  public void refreshActiveDocument() {
    try {
      _activeDocument.checkIfClassFileInSync();
      // notify single display model listeners   // notify single display model listeners
      _notifier.activeDocumentChanged(_activeDocument);
    } catch(DocumentClosedException dce) { /* do nothing */ }
  }
}
