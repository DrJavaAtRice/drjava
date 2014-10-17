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

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.ProgressMonitor;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaRoot;
import edu.rice.cs.drjava.config.Option;
import edu.rice.cs.drjava.config.OptionParser;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.cache.DCacheAdapter;
import edu.rice.cs.drjava.model.cache.DDReconstructor;
import edu.rice.cs.drjava.model.cache.DocumentCache ;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.debug.DebugBreakpointData;
import edu.rice.cs.drjava.model.debug.DebugException ;
import edu.rice.cs.drjava.model.debug.DebugWatchData;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.DocumentUIListener ;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelControl;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.model.print.DrJavaBook;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel ;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;
import edu.rice.cs.drjava.project.DocFile ;
import edu.rice.cs.drjava.project.DocumentInfoGetter;
import edu.rice.cs.drjava.project.MalformedProjectFileException;
import edu.rice.cs.drjava.project.ProjectFileIR;
import edu.rice.cs.drjava.project.ProjectFileParserFacade;
import edu.rice.cs.drjava.project.ProjectProfile;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;

import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Predicate;

import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.NullFile;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.docnavigation.AWTContainerNavigatorFactory;
import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.docnavigation.INavigationListener;
import edu.rice.cs.util.docnavigation.INavigatorItem;
import edu.rice.cs.util.docnavigation.INavigatorItemFilter;
import edu.rice.cs.util.docnavigation.JTreeSortNavigator;
import edu.rice.cs.util.docnavigation.NodeData;
import edu.rice.cs.util.docnavigation.NodeDataVisitor;
import edu.rice.cs.util.swing.AsyncCompletionArgs ;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.swing.IAsyncProgress;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.ConsoleDocument;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** In simple terms, a DefaultGlobalModel without an interpreter, compiler, junit testing, debugger or javadoc.
  * Hence, it only has only document handling functionality
  * @version $Id: AbstractGlobalModel.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class AbstractGlobalModel implements SingleDisplayModel, OptionConstants, DocumentIterator {
  
  public static final Log _log = new Log("GlobalModel.txt", false);
  
  /** A document cache that manages how many unmodified documents are open at once. */
  protected DocumentCache _cache;  
  
  static final String DOCUMENT_OUT_OF_SYNC_MSG =
    "Current document is out of sync with the Interactions Pane and should be recompiled!\n";
  
  static final String CLASSPATH_OUT_OF_SYNC_MSG =
    "Interactions Pane is out of sync with the current classpath and should be reset!\n";
  
  // ----- FIELDS -----
  
  /** Keeps track of all listeners to the model, and has the ability to notify them of some event.  Originally used
    * a Command Pattern style, but this has been replaced by having EventNotifier directly implement all listener
    * interfaces it supports.  Set in constructor so that subclasses can install their own notifier with additional
    * methods.
    */
  public final GlobalEventNotifier _notifier = new GlobalEventNotifier();
  
  // ---- Definitions fields ----
  
  /** Factory for new definitions documents and views.*/
  protected final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit(_notifier);
  
  /** Collection for storing all OpenDefinitionsDocuments. */
  private final AbstractMap<File, OpenDefinitionsDocument> _documentsRepos = 
    new LinkedHashMap<File, OpenDefinitionsDocument>();
  
  // ---- Input/Output Document Fields ----
  
  /** The document used to display System.out and System.err, and to read from System.in. */
  protected final ConsoleDocument _consoleDoc;
  
  /** The document adapter used in the console document. */
  protected final InteractionsDJDocument _consoleDocAdapter;
  
  /** A PageFormat object for printing to paper. */
  protected volatile PageFormat _pageFormat = new PageFormat();
  
  /** The active document pointer, which will never be null once the constructor is done.
    * Maintained by the _gainVisitor with a navigation listener.
    */
  private volatile OpenDefinitionsDocument _activeDocument;
  
  /** A pointer to the active directory, which is not necessarily the parent of the active document
    * The user may click on a folder component in the navigation pane and that will set this field without
    * setting the active document.  It is used by the newFile method to place new files into the active directory.
    */
  private volatile File _activeDirectory;
  
  /** A state varible indicating whether the class path has changed. Reset to false by resetInteractions. */
  private volatile boolean classPathChanged = false;
  
  /** The abstract container which contains views of open documents and allows user to navigate document focus among
    * this collection of open documents
    */
  protected volatile IDocumentNavigator<OpenDefinitionsDocument> _documentNavigator =
    new AWTContainerNavigatorFactory<OpenDefinitionsDocument>().makeListNavigator();
  
  /** Notifier list for the global model. */
  public GlobalEventNotifier getNotifier() { return _notifier; }
  
  /** Manager for breakpoint regions. */
  protected final ConcreteRegionManager<Breakpoint> _breakpointManager;
  
  /** @return manager for breakpoint regions. */
  public RegionManager<Breakpoint> getBreakpointManager() { return _breakpointManager; }
  
  /** Manager for bookmark regions. */
  protected final ConcreteRegionManager<MovingDocumentRegion> _bookmarkManager;
  
  /** @return manager for bookmark regions. */
  public RegionManager<MovingDocumentRegion> getBookmarkManager() { return _bookmarkManager; }
  
  /** Managers for find result regions. */
  protected final LinkedList<RegionManager<MovingDocumentRegion>> _findResultsManagers;
  
  /** @return new copy of list of find results managers for find result regions. */
  public List<RegionManager<MovingDocumentRegion>> getFindResultsManagers() {
    return new LinkedList<RegionManager<MovingDocumentRegion>>(_findResultsManagers);
  }
  
  /** @return new manager for find result regions. */
  public RegionManager<MovingDocumentRegion> createFindResultsManager() {
    ConcreteRegionManager<MovingDocumentRegion> rm = new ConcreteRegionManager<MovingDocumentRegion>();
    _findResultsManagers.add(rm);
    
    return rm;
  }
  
  /** Remove a manager from the model. */
  public void removeFindResultsManager(RegionManager<MovingDocumentRegion> rm) {
    _findResultsManagers.remove(rm);
  }
  
  /** Manager for browser history regions. */
  protected final BrowserHistoryManager _browserHistoryManager;
  
  /** @return manager for browser history regions. */
  public BrowserHistoryManager getBrowserHistoryManager() { return _browserHistoryManager; }

//  /** Completion monitor for loading the files of a project (as OpenDefinitionsDocuments). */
//  public final CompletionMonitor projectLoading = new CompletionMonitor();
  
// Lightweight parsing is disabled until we have something that is beneficial and works better in the background.
//  /** Light-weight parsing controller. */
//  protected LightWeightParsingControl _parsingControl;
//  
//  /** @return the parsing control */
//  public LightWeightParsingControl getParsingControl() { return _parsingControl; }
  
  // ----- CONSTRUCTORS -----
  
  /** Constructs a new GlobalModel. */
  public AbstractGlobalModel() {
    _cache = new DocumentCache();
    
    _consoleDocAdapter = new InteractionsDJDocument(_notifier);
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);
    
    _bookmarkManager = new ConcreteRegionManager<MovingDocumentRegion>();
    _findResultsManagers = new LinkedList<RegionManager<MovingDocumentRegion>>();
    _browserHistoryManager = new BrowserHistoryManager();
    
    _breakpointManager = new ConcreteRegionManager<Breakpoint>();
    /* The following method was included in an anonymous class definition of _breakpointManager, but it
     was inacessible because no such method exists in the visible interface of ConcreteRegionManager. */
    
//      public boolean changeRegionHelper(final Breakpoint oldBP, final Breakpoint newBP) {
//        // override helper so the enabled flag is copied
//        if (oldBP.isEnabled() != newBP.isEnabled()) {
//          oldBP.setEnabled(newBP.isEnabled());
//          return true;
//        }
//        return false;
//      }
//    };
    _registerOptionListeners();
    
    setFileGroupingState(makeFlatFileGroupingState());
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.projectRunnableChanged(); } });
    _init();
  }
  
  private void _init() {
    
    /** This visitor is invoked by the DocumentNavigator to update _activeDocument among other things */
    final NodeDataVisitor<OpenDefinitionsDocument, Boolean>  _gainVisitor = 
      new NodeDataVisitor<OpenDefinitionsDocument, Boolean>() {
      public Boolean itemCase(OpenDefinitionsDocument doc, Object... p) {
        _setActiveDoc(doc);  // sets _activeDocument, the shadow copy of the active document
//        addToBrowserHistory();
        
//        Utilities.showDebug("Setting the active doc done");
        final File oldDir = _activeDirectory;  // _activeDirectory can be null
        final File dir = doc.getParentDirectory();  // dir can be null
        if (dir != null && ! dir.equals(oldDir)) {
          /* If the file is in External or Auxiliary Files then then we do not want to change our project directory
           * to something outside the project. ?? */
          _activeDirectory = dir;
          _notifier.currentDirectoryChanged(_activeDirectory);
        }
        return Boolean.valueOf(true);
      }
      public Boolean fileCase(File f, Object... p) {
        if (! f.isAbsolute()) { // should never happen because all file names are canonicalized
          File root = _state.getProjectFile().getParentFile().getAbsoluteFile();
          f = new File(root, f.getPath());
        }
        _activeDirectory = f;  // Invariant: activeDirectory != null
        _notifier.currentDirectoryChanged(f);
        return Boolean.valueOf(true);
      }
      public Boolean stringCase(String s, Object... p) { return Boolean.valueOf(false); }
    };
    
    /** Listener that invokes the _gainVisitor when a selection is made in the document navigator. */
    _documentNavigator.addNavigationListener(new INavigationListener<OpenDefinitionsDocument>() {
      public void gainedSelection(NodeData<? extends OpenDefinitionsDocument> dat, boolean modelInitiated) {
        dat.execute(_gainVisitor, modelInitiated); }
      public void lostSelection(NodeData<? extends OpenDefinitionsDocument> dat, boolean modelInitiated) {
      /* not important, only one document selected at a time */ }
    });
    
    // The document navigator gets the focus in 
    _documentNavigator.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
//        System.err.println("_documentNavigator.focusGained(...) called");
//        if (_documentNavigator.getCurrent() != null) // past selection is leaf node
        Utilities.invokeLater(new Runnable() { public void run() { _notifier.focusOnDefinitionsPane(); } });
      }
      public void focusLost(FocusEvent e) { }
    });
    
    _ensureNotEmpty();
    setActiveFirstDocument();
    
    // setup option listener for clipboard history
    OptionListener<Integer> clipboardHistorySizeListener = new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
        ClipboardHistoryModel.singleton().resize(oce.value);
      }
    };
    DrJava.getConfig().addOptionListener(CLIPBOARD_HISTORY_SIZE, clipboardHistorySizeListener);
    ClipboardHistoryModel.singleton().resize(DrJava.getConfig().getSetting(CLIPBOARD_HISTORY_SIZE).intValue());
    
    // setup option listener for browser history
    OptionListener<Integer> browserHistoryMaxSizeListener = new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
        AbstractGlobalModel.this.getBrowserHistoryManager().setMaximumSize(oce.value);
      }
    };
    DrJava.getConfig().addOptionListener(BROWSER_HISTORY_MAX_SIZE, browserHistoryMaxSizeListener);
    getBrowserHistoryManager().setMaximumSize(DrJava.getConfig().getSetting(BROWSER_HISTORY_MAX_SIZE).intValue());
  }
  
  // ----- STATE -----
  
  /** Specifies the state of the navigator pane.  The global model delegates the compileAll command to the _state.
    * FileGroupingState synchronization is handled by the compilerModel (??).
    */
  protected volatile FileGroupingState _state;
  
  /** @param state the new file grouping state. */
  public void setFileGroupingState(FileGroupingState state) {
    _state = state;
    _notifier.projectRunnableChanged();
    _notifier.projectBuildDirChanged();
    _notifier.projectWorkDirChanged();
    
//    _notifier.projectModified();  // not currently used
  }
  
  /** Adds a document to the list of auxiliary files within _state.  The LinkedList class is not thread safe, so
    * the add operation is synchronized.
    */
  public void addAuxiliaryFile(OpenDefinitionsDocument doc) { _state.addAuxFile(doc.getRawFile()); }
  
  /** Removes a document from the list of auxiliary files within _state.  The LinkedList class is not thread safe, so
    * operations on _auxiliaryFiles are synchronized.
    */
  public void removeAuxiliaryFile(OpenDefinitionsDocument doc) { _state.remAuxFile(doc.getRawFile()); }
  
  protected FileGroupingState
    makeProjectFileGroupingState(File pr, String main, File bd, File wd, File project, File[] srcFiles, File[] auxFiles, 
                                 File[] excludedFiles, Iterable<AbsRelFile> cp, File cjf, int cjflags, boolean refresh,
                                 String manifest, Map<OptionParser<?>,String> storedPreferences) {
    
    return new ProjectFileGroupingState(pr, main, bd, wd, project, srcFiles, auxFiles, excludedFiles, cp, cjf, cjflags,
                                        refresh, manifest, storedPreferences);
  }
  
  /** @return true if the class path state has been changed. */
  public boolean isClassPathChanged() { return classPathChanged; }
  
  /** Updates the classpath state. */
  public void setClassPathChanged(boolean changed) {
    classPathChanged = changed;
  }
  
  /** Notifies the project state that the project has been changed. */
  public void setProjectChanged(boolean changed) {
    _state.setProjectChanged(changed);
//    _notifier.projectModified();  // not currently used
  }
  
  /** @return true if the project state has been changed. */
  public boolean isProjectChanged() { return _state.isProjectChanged(); }
  
  /** @return true if the model has a project open, false otherwise. */
  public boolean isProjectActive() { return _state.isProjectActive(); }
  
  /** @return the file that points to the current project file. Null if not currently in project model. */
  public File getProjectFile() { return _state.getProjectFile(); }
  
  /** @return all files currently saved as source files in the project file.
    * If _state not in project mode, returns null. */
  public File[] getProjectFiles() { return _state.getProjectFiles(); }
  
  /** @return true the given file is in the current project file. */
  public boolean inProject(File f) { return _state.inProject(f); }
  
  /** A file is in the project if the source root is the same as the
    * project root. this means that project files must be saved at the
    * source root. (we query the model through the model's state)
    */
  public boolean inProjectPath(OpenDefinitionsDocument doc) { return _state.inProjectPath(doc); }
  
  /** Sets the class with the project's main method. */
  public void setMainClass(String f) {
    _state.setMainClass(f);
    _notifier.projectRunnableChanged();
    setProjectChanged(true);
  }
  
  /** @return the class with the project's main method. */
  public String getMainClass() { return _state.getMainClass(); }
  
  /** @return the file containing the project's main class. */
  public File getMainClassContainingFile() {
    String path = getMainClass();
    
    if (path == null) return null;
    
    // TODO: What about language level file extensions? What about Habanero Java extension?
    if (path.toLowerCase().endsWith(OptionConstants.JAVA_FILE_EXTENSION)) {
      return new File(getProjectFile().getParent(), path);
    } //if
    
    // maybe we have an inner class; remove names from the end and see if we find
    // a file for it that way.
    // Example:
    // some/package/SomeClass/Inner/AnotherInner.java (not found)
    // some/package/SomeClass/Inner.java (not found)
    // some/package/SomeClass.java (not found)
    path = path.replace('.', File.separatorChar);
    File tempFile = new File(getProjectRoot(), path+OptionConstants.JAVA_FILE_EXTENSION);
    while (path.length() > 0) {
      if (tempFile.exists()) return tempFile;
      
      if (path.indexOf(File.separatorChar) == -1) break;
      
      path = path.substring(0, path.lastIndexOf(File.separatorChar));
      tempFile = new File(getProjectRoot(), path + OptionConstants.JAVA_FILE_EXTENSION);
    }
    
    return null;
  }
  
  /** Sets the create jar file of the project. */
  public void setCreateJarFile(File f) {
    _state.setCreateJarFile(f);
    setProjectChanged(true);
  }
  
  /** Return the create jar file for the project. If not in project mode, returns null. */
  public File getCreateJarFile() { return _state.getCreateJarFile(); }
  
  /** Sets the create jar flags of the project. */
  public void setCreateJarFlags(int f) {
    _state.setCreateJarFlags(f);
    setProjectChanged(true);
  }
  
  /** Return the create jar flags for the project. If not in project mode, returns 0. */
  public int getCreateJarFlags() { return _state.getCreateJarFlags(); }
  
  /** @return the root of the project sourc tree (assuming one exists). */
  public File getProjectRoot() { return _state.getProjectRoot(); }
  
  /** Sets the class with the project's main method.  Degenerate version overridden in DefaultGlobalModel. */
  public void setProjectRoot(File f) {
    _state.setProjectRoot(f);
//    _notifier.projectRootChanged();
    setProjectChanged(true);
  }
  
  /** Sets project file to specifed value; used in "Save Project As ..." command in MainFrame. */
  public void setProjectFile(File f) { _state.setProjectFile(f); }
  
  /** @return the build directory for the project (assuming one exists). */
  public File getBuildDirectory() { return _state.getBuildDirectory(); }
  
  /** @return the stored preferences. */
  public Map<OptionParser<?>,String> getPreferencesStoredInProject() { return _state.getPreferencesStoredInProject(); }

  public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp) { _state.setPreferencesStoredInProject(sp); }
  
  /** Sets the class with the project's main method.  Degenerate version overridden in DefaultGlobalModel. */
  public void setBuildDirectory(File f) {
    _state.setBuildDirectory(f);
    _notifier.projectBuildDirChanged();
    setProjectChanged(true);
  }
  
//  /** Queries the use for the build directory. */
//  File askForBuildDirectory(MainFrame frame) { return frame.askForBuildDirectory(); }
  
  /** Gets autorfresh status of the project */
  public boolean getAutoRefreshStatus() { return _state.getAutoRefreshStatus(); }
  
  /** Sets autofresh status of the project */
  public void setAutoRefreshStatus(boolean status) { _state.setAutoRefreshStatus(status); }
  
  /** @return the working directory for the Master JVM (editor and GUI). */
  public File getMasterWorkingDirectory() {
    File file;
    try {
      // restore the path from the configuration
      file = FileOps.getValidDirectory(DrJava.getConfig().getSetting(LAST_DIRECTORY));
    }
    catch (RuntimeException e) {
      // something went wrong, clear the setting and use "user.home"
      DrJava.getConfig().setSetting(LAST_DIRECTORY, FileOps.NULL_FILE);
      file = FileOps.getValidDirectory(new File(System.getProperty("user.home", ".")));
    }
    // update the setting and return it
    DrJava.getConfig().setSetting(LAST_DIRECTORY, file);
    return file;
  }
  
  /** @return the working directory for the Slave (Interactions) JVM */
  public File getWorkingDirectory() { return _state.getWorkingDirectory(); }
  
  /** Sets the working directory for the Slave JVM. Ignored in flat file mode. */
  public void setWorkingDirectory(File f) {
    _state.setWorkingDirectory(f);
    _notifier.projectWorkDirChanged();
    setProjectChanged(true);
    // update the setting
    DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, _state.getWorkingDirectory());
  }
  
  public void cleanBuildDirectory()  { _state.cleanBuildDirectory(); }
  
  public List<File> getClassFiles() { return _state.getClassFiles(); }
  
  // No longer used
//  /** Helper method used in subsequent anonymous inner class */
//  protected static String getPackageName(String classname) {
//    int index = classname.lastIndexOf(".");
//    if (index != -1) return classname.substring(0, index);
//    else return "";
//  }
  
  class ProjectFileGroupingState implements FileGroupingState {
    
    private volatile File _projRoot;
    private volatile String _mainClass;
    private volatile File _buildDir;
    private volatile File _workDir;
    private volatile File _projectFile;
    private final File[] _projectFiles;
    private volatile ArrayList<File> _auxFiles;            // distinct from _auxiliaryFiles in ProjectProfile
    private volatile ArrayList<File> _exclFiles;   // distinct from _excludedFiles in ProjectProile and CompilerErrorPanel
    private volatile Iterable<AbsRelFile> _projExtraClassPath;
    private volatile boolean _isProjectChanged = false;
    private volatile File _createJarFile;
    private volatile int _createJarFlags;
    private volatile boolean _autoRefreshStatus;
    private final Map<OptionParser<?>,String> _storedPreferences = new HashMap<OptionParser<?>,String>();
    
    private volatile String _manifest = null;
    
    private final HashSet<String> _projFilePaths = new HashSet<String>();
    
    /** Degenerate constructor for a new project; only the file project name is known. */
    ProjectFileGroupingState(File project) {
      this(project.getParentFile(), null, null, null, project, new File[0], new File[0], new File[0], 
           IterUtil.<AbsRelFile>empty(), null, 0, false, null, new HashMap<OptionParser<?>,String>());
      HashMap<OptionParser<?>,String> defaultStoredPreferences = new HashMap<OptionParser<?>,String>();
      // by default, put INDENT_LEVEL AND LANGUAGE_LEVEL into the project file
      defaultStoredPreferences.put(INDENT_LEVEL, DrJava.getConfig().getOptionMap().getString(INDENT_LEVEL));      
      defaultStoredPreferences.put(LANGUAGE_LEVEL, DrJava.getConfig().getOptionMap().getString(LANGUAGE_LEVEL));
      setPreferencesStoredInProject(defaultStoredPreferences);
    }
    
    ProjectFileGroupingState(File pr, String main, File bd, File wd, File project, File[] srcFiles, File[] auxFiles, 
                             File[] excludedFiles, Iterable<AbsRelFile> cp, File cjf, int cjflags, boolean refreshStatus, 
                             String customManifest, Map<OptionParser<?>,String> storedPreferences) {
      _projRoot = pr;
      _mainClass = main;
      _buildDir = bd;
      _workDir = wd;
      _projectFile = project;
      _projectFiles = srcFiles;
      _auxFiles = new ArrayList<File>(auxFiles.length);
      for(File f: auxFiles) { _auxFiles.add(f); }
      _exclFiles = new ArrayList<File>(excludedFiles.length);
      for(File f: excludedFiles) { _exclFiles.add(f); }
      _projExtraClassPath = cp;
      
      if (_projectFiles != null) {
        try { for (File file : _projectFiles) { _projFilePaths.add(file.getCanonicalPath()); } }
        catch(IOException e) { /*do nothing */ }
      }
      
      _createJarFile = cjf;
      _createJarFlags = cjflags;
      _autoRefreshStatus = refreshStatus;
      _manifest = customManifest;
      setPreferencesStoredInProject(storedPreferences); 
    }
    
    public boolean isProjectActive() { return true; }
    
    /** Determines whether the specified doc in within the project file tree.
      * No synchronization is required because only immutable data is accessed.
      */
    public boolean inProjectPath(OpenDefinitionsDocument doc) {
      if (doc.isUntitled()) return false;
      
      /* If the file does not exist, we still want to tell if it's path lies within the project source tree.  The file 
       * may have existed previously at one point and then removed, in which case we should treat it as an untitled 
       * project file that should be resaved. */
      File f;
      try { f = doc.getFile(); }
      catch(FileMovedException fme) { f = fme.getFile(); }
      return inProjectPath(f);
    }
    
    /** Determines whether the specified file in within the project file tree. No synchronization is required because
      * only immutable data is accessed.
      */
    public boolean inProjectPath(File f) { return IOUtil.isMember(f, getProjectRoot()); }
    
    /** @return the absolute path to the project file.  Since projectFile is final, no synchronization is necessary.*/
    public File getProjectFile() { return _projectFile; }
    
    public boolean inProject(File f) {
      String path;
      
      if (isUntitled(f) || ! inProjectPath(f)) return false;
      try {
        path = f.getCanonicalPath();
        return _projFilePaths.contains(path);
      }
      catch(IOException ioe) { return false; }
    }
    
    public File[] getProjectFiles() { return _projectFiles; }
    
    public File getProjectRoot() {
      if (_projRoot == null || _projRoot.equals( FileOps.NULL_FILE)) return _projectFile.getParentFile();
//      Utilities.show("File grouping state returning project root of " + _projRoot);
      return _projRoot;
    }
    
    public File getBuildDirectory() { return _buildDir; }
    
    public File getWorkingDirectory() {
      try {
        if (_workDir == null || _workDir == FileOps.NULL_FILE) {
          // if no project working directory is set, check preferences working directory
          File prefWorkDir = DrJava.getConfig().getSetting(FIXED_INTERACTIONS_DIRECTORY);
          if ((prefWorkDir != null) && (prefWorkDir != FileOps.NULL_FILE)) {
            try {
              // make sure it's a valid directory
              prefWorkDir = FileOps.getValidDirectory(prefWorkDir);
            }
            catch (RuntimeException e) { prefWorkDir = FileOps.NULL_FILE; }
          }
          if ((prefWorkDir != null) && (prefWorkDir != FileOps.NULL_FILE)) { return prefWorkDir; }

          // if there is no fixed working directory in the preferences, use the directory
          // containing the project file
          File parentDir = _projectFile.getParentFile();
          if (parentDir != null) {
            return parentDir.getCanonicalFile(); // default is project root
          } // or if all else fails, user.dir
          else return new File(System.getProperty("user.dir"));
        }
        return _workDir.getCanonicalFile();
      }
      catch(IOException e) { /* fall through */ }
      return _workDir.getAbsoluteFile();
    }
    
    /** Sets project file to specifed value; used in "Save Project As ..." command in MainFrame. */
    public void setProjectFile(File f) { _projectFile = f; }
    
    public void setProjectRoot(File f) {
      _projRoot = f;
//      System.err.println("Project root set to " + f);
    }
    
    /** Adds File f to end of _auxFiles vector. */
    public void addAuxFile(File f) {
      synchronized(_auxFiles) {
        if (_auxFiles.add(f)) setProjectChanged(true);
      }
    }
    
    /** Removes File file from _auxFiles list. */
    public void remAuxFile(File file) {
      synchronized(_auxFiles) { 
        if (_auxFiles.remove(file)) setProjectChanged(true);
      }
    }
    
    public void addExcludedFile(File f) {
      if(f == null) return;
      if (isAlreadyOpen(f)) return;  // can't add files to the black list that are currently open
      synchronized(_exclFiles) {
        if (_exclFiles.add(f)) setProjectChanged(true);
      }
    }
    
    public void removeExcludedFile(File f) {
      synchronized(_exclFiles) {
        for(int i = 0;i < _exclFiles.size();i++) {
          try {
            if(_exclFiles.get(i).getCanonicalPath().equals(f.getCanonicalPath())) {
              _exclFiles.remove(i);
              setProjectChanged(true);
            }
          }
          catch(IOException e) { }
        }
      }
    }
    
    public File[] getExclFiles() { return _exclFiles.toArray(new File[_exclFiles.size()]); }
    
    public void setExcludedFiles(File[] fs) {
      if(fs == null) return;
      synchronized(_exclFiles) {
        _exclFiles.clear();
        for(File f: fs) { addExcludedFile(f); }
        setProjectChanged(true);
      }
    }
    
    public void setBuildDirectory(File f) { _buildDir = f; }
    
    public void setWorkingDirectory(File f) { _workDir = f; }
    
    public String getMainClass() { return _mainClass; }
    
    public void setMainClass(String f) { _mainClass = f; }
    
    public void setCreateJarFile(File f) { _createJarFile = f; }
    
    public File getCreateJarFile() { return _createJarFile; }
    
    public void setCreateJarFlags(int f) { _createJarFlags = f; }
    
    public int getCreateJarFlags() { return _createJarFlags; }
    
    public boolean isProjectChanged() { return _isProjectChanged; }
    
    public void setProjectChanged(boolean changed) { _isProjectChanged = changed; }
    
    public boolean isAuxiliaryFile(File f) {
      String path;
      
      if (isUntitled(f)) return false;  
      
      try { path = f.getCanonicalPath();}
      catch(IOException ioe) { return false; }
      
      synchronized(_auxFiles) {
        for (File file : _auxFiles) {
          try { if (file.getCanonicalPath().equals(path)) return true; }
          catch(IOException ioe) { /* ignore file */ }
        }
        return false;
      }
    }
    
    public boolean isExcludedFile(File f) {
      String path;
      if (isUntitled(f)) return false;  
      
      try { path = f.getCanonicalPath();}
      catch(IOException ioe) { return false; }
      
      synchronized(_exclFiles) {
        for (File file : _exclFiles) {
          try { if (file.getCanonicalPath().equals(path)) return true; }
          catch(IOException ioe) { /* ignore file */ }
        }
        return false;
      }
    }
    
    public boolean getAutoRefreshStatus() { return _autoRefreshStatus; }
    public void setAutoRefreshStatus(boolean status) { _autoRefreshStatus = status; }
    
    /** @return the stored preferences. */
    public Map<OptionParser<?>,String> getPreferencesStoredInProject() {
      return new HashMap<OptionParser<?>,String>(_storedPreferences);
    }

    public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp) {
      // remove previous listeners
      removePreviousListeners();
      
      _storedPreferences.clear();
      _storedPreferences.putAll(sp); 
      
      // add new listeners
      addNewListeners(sp);
    }
    
    // This only starts the process. It is all done asynchronously.
    public void cleanBuildDirectory() {
      File dir = this.getBuildDirectory ();
      _notifier.executeAsyncTask(_findFilesToCleanTask, dir, false, true);
    }
    
    private AsyncTask<File,List<File>> _findFilesToCleanTask = new AsyncTask<File,List<File>>("Find Files to Clean") {
      private FilenameFilter _filter = new FilenameFilter() {
        public boolean accept(File parent, String name) {
          return new File(parent, name).isDirectory() || name.endsWith(".class");
        }
      };
      
      public List<File> runAsync(File buildDir, IAsyncProgress monitor) throws Exception {
        List<File> accumulator = new LinkedList<File>();
        helper(buildDir, accumulator); // adds files to the accumulator recursively
        return accumulator;
      }
      public void complete(AsyncCompletionArgs<List<File>> args) {
        _notifier.executeAsyncTask(_deleteFilesTask, args.getResult(), true, true);
      }
      public String getDiscriptionMessage() {
        return "Finding files to delete...";
      }
      private void helper(File file, List<File> accumulator) {
        if (file.isDirectory ()) {
          File[] children = file.listFiles(_filter);
          for (File child : children) {
            helper(child, accumulator);
            accumulator.add(file);
          }
        }
        else if ( file.getName().endsWith(".class")) accumulator.add(file);
      }
    };    
    
    private AsyncTask<List<File>,List<File>> _deleteFilesTask = new AsyncTask<List<File>,List<File>>("Delete Files") {
      public List<File> runAsync(List<File> filesToDelete, IAsyncProgress monitor) throws Exception {
        List<File> undeletableFiles = new ArrayList<File>();
        
        monitor.setMinimum (0);
        monitor.setMaximum(filesToDelete.size());
        int progress = 1;
        for(File file : filesToDelete) {
          if (monitor.isCanceled()) {
            break;
          }
          monitor.setNote(file.getName());
          boolean could = file.delete();
          if (!could) undeletableFiles.add(file);
          monitor.setProgress(progress++);
        }
//      if (! dir.exists()) dir.mkdirs (); // TODO: figure out where to put this.
        return undeletableFiles;
      }
      public void complete(AsyncCompletionArgs<List<File>> args) {
        // TODO: user feedback. Maybe add a method to the notifier to set the status bar text
      }
      public String getDiscriptionMessage() {
        return "Deleting files...";
      }
    };
    
    public List<File> getClassFiles() {
      File dir = this.getBuildDirectory ();
      LinkedList<File> acc = new LinkedList<File>();
      getClassFilesHelper(dir, acc);
      if (! dir.exists()) dir.mkdirs();  // TODO: what if mkdirs() fails
      return acc;
    }
    
    private void getClassFilesHelper(File f, LinkedList<File> acc) {
      if (f.isDirectory()) {
        
        File fs[] = f.listFiles(new FilenameFilter() {
          public boolean accept(File parent, String name) {
            return new File(parent, name).isDirectory() || name.endsWith(".class");
          }
        });
        
        if (fs != null) { // listFiles may return null if there's an IO error
          for (File kid: fs) { getClassFilesHelper(kid, acc); }
        }
        
      } else if (f.getName().endsWith(".class")) acc.add(f);
    }    
    
    // ----- FIND ALL DEFINED CLASSES IN FOLDER ---
    
    public Iterable<AbsRelFile> getExtraProjectClassPath() { return _projExtraClassPath; }
    public void setExtraClassPath(Iterable<AbsRelFile> cp) { 
      _projExtraClassPath = cp; 
      setClassPathChanged(true);
    }
    
    // ---- Custom Manifest methods -- ///
    public String getCustomManifest() { return _manifest; }
    public void setCustomManifest(String manifest) { _manifest = manifest; }
  }
  
  @SuppressWarnings("unchecked")
  protected void removePreviousListeners() {
    for(Map.Entry<OptionParser<?>, OptionListener<?>> e: LISTENERS_TO_REMOVE.entrySet()) {
      // all keys should be full Option instances, not just OptionParser instances
      if (e.getKey() instanceof Option) {
        DrJava.getConfig().removeOptionListener((Option)e.getKey(), e.getValue());
      }
    }
    LISTENERS_TO_REMOVE.clear();
  }
  
  @SuppressWarnings("unchecked")
  protected void addNewListeners(Map<OptionParser<?>,String> newValues) {
    for(OptionParser<?> key: newValues.keySet()) {
      // all keys should be full Option instances, not just OptionParser instances
      if (key instanceof Option) {
        DrJava.getConfig().addOptionListener((Option)key, STORED_PREFERENCES_LISTENER);
        LISTENERS_TO_REMOVE.put(key, STORED_PREFERENCES_LISTENER);
      }
    }
  }
  
  protected static final HashMap<OptionParser<?>, OptionListener<? extends Object>> LISTENERS_TO_REMOVE =
    new HashMap<OptionParser<?>, OptionListener<? extends Object>>();
  
  public final OptionListener<? extends Object> STORED_PREFERENCES_LISTENER = new OptionListener<Object>() {
    public void optionChanged(OptionEvent<Object> oce) {
      setProjectChanged(true);
    }
  };
  
  protected FileGroupingState makeFlatFileGroupingState() { return new FlatFileGroupingState(); }
  
  class FlatFileGroupingState implements FileGroupingState {
    public File getBuildDirectory() { return FileOps.NULL_FILE; }
    public File getProjectRoot() { return getWorkingDirectory(); }
    public File getWorkingDirectory() {
      // if a fixed working directory has been set in the Preferences, use it
      File prefWorkDir = DrJava.getConfig().getSetting(FIXED_INTERACTIONS_DIRECTORY);
      if ((prefWorkDir != null) && (prefWorkDir != FileOps.NULL_FILE)) {
        try {
          // make sure it's a valid directory
          prefWorkDir = FileOps.getValidDirectory(prefWorkDir);
        }
        catch (RuntimeException e) { prefWorkDir = FileOps.NULL_FILE; }
      }
      if ((prefWorkDir != null) && (prefWorkDir != FileOps.NULL_FILE)) {
        // update the setting and return it
        DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, prefWorkDir);
        return prefWorkDir;
      }
      
      // otherwise determine the working directory based on the source root
      File file = FileOps.NULL_FILE;
      try {
        file = getActiveDocument().getSourceRoot(); // source root of the current document
      }
      catch(InvalidPackageException ipe) { file = FileOps.NULL_FILE; }
      if ((file != null) && (file != FileOps.NULL_FILE)) {
        // update the setting and return it
        DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, file);
        return file;
      }
      
      // if we can't get the source root of the current document, use the first document
      Iterable<File> roots = getSourceRootSet();
      if (! IterUtil.isEmpty(roots)) { return IterUtil.first(roots); }
      else {
        // use the last directory saved to the configuration
        if (DrJava.getConfig().getSetting(STICKY_INTERACTIONS_DIRECTORY)) {
          try {
            // restore the path from the configuration
            file = FileOps.getValidDirectory(DrJava.getConfig().getSetting(LAST_INTERACTIONS_DIRECTORY));
          }
          catch (RuntimeException e) { file = FileOps.NULL_FILE; }
        }
        if (file == FileOps.NULL_FILE) {
          // something went wrong, clear the setting and use "user.home"
          file = FileOps.getValidDirectory(new File(System.getProperty("user.home", ".")));
        }
        // update the setting and return it
        DrJava.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, file);
        return file;
      }
    }
    public boolean isProjectActive() { return false; }
    public boolean inProjectPath(OpenDefinitionsDocument doc) { return false; }
    public boolean inProjectPath(File f) { return false; }
    public File getProjectFile() { return FileOps.NULL_FILE; }
    public void setBuildDirectory(File f) { }
    public void setProjectFile(File f) { }
    public void setProjectRoot(File f) { }
    public void addAuxFile(File f) { }
    public void remAuxFile(File f) { }
    public void setWorkingDirectory(File f) { }
    public File[] getProjectFiles() { return new File[0]; }
    public boolean inProject(File f) { return false; }
    public String getMainClass() { return null; }
    public void setMainClass(String f) { }
    public void setCreateJarFile(File f) { }
    public File getCreateJarFile() { return FileOps.NULL_FILE; }
    public void setCreateJarFlags(int f) { }
    public int getCreateJarFlags() { return 0; }
    public Iterable<AbsRelFile> getExtraProjectClassPath() { return IterUtil.empty(); }
    public void setExtraClassPath(Iterable<AbsRelFile> cp) { }
    public boolean isProjectChanged() { return false; }
    public void setProjectChanged(boolean changed) { /* Do nothing  */  }
    public boolean isAuxiliaryFile(File f) { return false; }
    public boolean isExcludedFile(File f) { return false; }
    public File[] getExclFiles() { return null; }
    public void addExcludedFile(File f) { }
    public void removeExcludedFile(File f) { }
    public void setExcludedFiles(File[] fs) { }
    public boolean getAutoRefreshStatus() {return false;}
    public void setAutoRefreshStatus(boolean b) { }
    public void setPreferencesStoredInProject(Map<OptionParser<?>,String> sp) { /* do nothing */ }
    public Map<OptionParser<?>,String> getPreferencesStoredInProject() { return new HashMap<OptionParser<?>,String>(); }

    public void cleanBuildDirectory() { }
    
    public List<File> getClassFiles() { return new LinkedList<File>(); }
    
    public String getCustomManifest() { return null; }
    public void setCustomManifest(String manifest) { }
  }
  
  /** Gives the title of the source bin for the navigator.
    * @return The text used for the source bin in the tree navigator
    */
  public String getSourceBinTitle() { return "[ Source Files ]"; }
  
  /** Gives the title of the external files bin for the navigator
    * @return The text used for the external files bin in the tree navigator.
    */
  public String getExternalBinTitle() { return "[ External Files ]"; }
  
  /** Gives the title of the aux files bin for the navigator.
    * @return The text used for the aux files bin in the tree navigator.
    */
  public String getAuxiliaryBinTitle() { return "[ Included External Files ]"; }
  
  // ----- METHODS -----
  
  /** Add a listener to this global model.
    * @param listener a listener that reacts on events generated by the GlobalModel.
    */
  public void addListener(GlobalModelListener listener) { _notifier.addListener(listener); }
  
  /** Remove a listener from this global model.
    * @param listener a listener that reacts on events generated by the GlobalModel
    * This method is synchronized using the readers/writers event protocol incorporated in EventNotifier<T>.
    */
  public void removeListener(GlobalModelListener listener) { _notifier.removeListener(listener); }
  
  // getter methods for the private fields
  
  public DefinitionsEditorKit getEditorKit() { return _editorKit; }
  
  /** throws UnsupportedOperationException */
  public DefaultInteractionsModel getInteractionsModel() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  /** throws UnsupportedOperationException */
  public InteractionsDJDocument getSwingInteractionsDocument() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  /** throws UnsupportedOperationException */
  public InteractionsDocument getInteractionsDocument() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interaction");
  }
  
  public ConsoleDocument getConsoleDocument() { return _consoleDoc; }
  
  public InteractionsDJDocument getSwingConsoleDocument() { return _consoleDocAdapter; }
  
  public PageFormat getPageFormat() { return _pageFormat; }
  
  public void setPageFormat(PageFormat format) { _pageFormat = format; }
  
  public CompilerModel getCompilerModel() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
  }
  
  /** throws UnsupportedOperationException */
  public int getNumCompilerErrors() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
  }
  
  /** throws UnsupportedOperationException */
  public void setNumCompilerErrors(int num) { 
    throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
  };
  
  /** throws UnsupportedOperationException */
  public JUnitModel getJUnitModel() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support unit testing");
  }
  
  /** throws UnsupportedOperationException */
  public JavadocModel getJavadocModel() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support javadoc");
  }
  
  public IDocumentNavigator<OpenDefinitionsDocument> getDocumentNavigator() { return _documentNavigator; }
  
  public void setDocumentNavigator(IDocumentNavigator<OpenDefinitionsDocument> newnav) { _documentNavigator = newnav; }
  
  /** Toogle the specified bookmark in the active document.
    * @param pos1 first selection position
    * @param pos2 second selection position */
  public void toggleBookmark(int pos1, int pos2) { _toggleBookmark(pos1, pos2); }
  
  /** Raw version of toggleBookmark.  Only runs in event thread.
    * @param pos1 first selection position
    * @param pos2 second selection position */
  public void _toggleBookmark(int pos1, int pos2) {
//    Utilities.show("AGM.toggleBookmark called");
    assert EventQueue.isDispatchThread();
    
    final OpenDefinitionsDocument doc = getActiveDocument();
    
    int startSel = Math.min(pos1, pos2);
    int endSel = Math.max(pos1, pos2);
//    try {
    RegionManager<MovingDocumentRegion> bm = _bookmarkManager;
    if (startSel == endSel) {  // offset only; bookmark the entire line
      endSel = doc._getLineEndPos(startSel);
      startSel = doc._getLineStartPos(startSel);
    }
    
    Collection<MovingDocumentRegion> conflictingRegions = bm.getRegionsOverlapping(doc, startSel, endSel);
    
    if (conflictingRegions.size() > 0) {
      for (MovingDocumentRegion cr: conflictingRegions) bm.removeRegion(cr);
    }
    else {
      MovingDocumentRegion newR = 
        new MovingDocumentRegion(doc, startSel, endSel, doc._getLineStartPos(startSel), doc._getLineEndPos(endSel));
      bm.addRegion(newR);
    }
  }
  
  /** Creates a new open definitions document and adds it to the list.  Public for testing purposes.  Only runs in 
    * the event thread.
    * @param parentDir directory in which the document should be located
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile(File parentDir) { return newFile(parentDir, ""); }
  
  /** Creates a new open definitions document and adds it to the list.  Public for testing purposes.  Only runs in 
    * the event thread.
    * @param parentDir directory in which the document should be located
    * @param text text for the new document
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile(File parentDir, String text) {
///* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    final ConcreteOpenDefDoc doc = _createOpenDefinitionsDocument(new NullFile());
    try {
      if (text.length() > 0) {
        doc.insertString(0, text, null);
        doc.indentLines(0, text.length());
      }
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    finally {
      doc.setParentDirectory(parentDir);
      addDocToNavigator(doc);
      _notifier.newFileCreated(doc);
    }
    return doc;
  }
  
  /** Creates a new document, adds it to the list of open documents, and sets it to be active.  
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile(String text) {
    File dir = _activeDirectory;
    if (dir == null) dir = getMasterWorkingDirectory();
    OpenDefinitionsDocument doc = newFile(dir, text);
    setActiveDocument(doc);
    return doc;
  }
  
  /** Creates a new document, adds it to the list of open documents, and sets it to be active.  
    * @return The new open document
    */
  public OpenDefinitionsDocument newFile() { return newFile(""); }  

  /** Creates a new junit test case.
    * @param name the name of the new test case
    * @param makeSetUp true iff an empty setUp() method should be included
    * @param makeTearDown true iff an empty tearDown() method should be included
    * @return the new open test case
    */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
//    boolean elementary = 
//      (DrJava.getConfig().getSetting(LANGUAGE_LEVEL) == OptionConstants.ELEMENTARY_LEVEL) ||
//      (DrJava.getConfig().getSetting(LANGUAGE_LEVEL) == OptionConstants.FUNCTIONAL_JAVA_LEVEL); 
    final StringBuilder buf = new StringBuilder();
//    if (! elementary) buf.append("import junit.framework.TestCase;\n\n");
    buf.append("import junit.framework.TestCase\n");
    buf.append("import junit.framework.Assert._\n\n");
    buf.append("/**\n");
    buf.append("* A JUnit test case class.\n");
    buf.append("* Every method starting with the word \"test\" will be called when running\n");
    buf.append("* the test with JUnit.\n");
    buf.append("*/\n");
/*    if (! elementary) buf.append("public "); */
    buf.append("class ");
    buf.append(name);
    buf.append("(name: String) extends TestCase(name) {\n\n");

// TODO (williamf): Add setup and teardown methods for Scala
//    if (makeSetUp) {
//      buf.append("/**\n");
//      buf.append("* This method is called before each test method, to perform any common\n");
//      buf.append("* setup if necessary.\n");
//      buf.append("*/\n");
//      if (! elementary) buf.append("public ");
//      buf.append("void setUp() throws Exception {\n}\n\n");
//    }
//    if (makeTearDown) {
//      buf.append("/**\n");
//      buf.append("* This method is called after each test method, to perform any common\n");
//      buf.append("* clean-up if necessary.\n");
//      buf.append("*/\n");
//      if (! elementary) buf.append("public ");
//      buf.append("void tearDown() throws Exception {\n}\n\n");
//    }
    buf.append("/**\n");
    buf.append("* A test method.\n");
    buf.append("* (Replace \"X\" with a name describing the test.  You may write as\n");
    buf.append ("* many \"testSomething\" methods in this class as you wish, and each\n");
    buf.append("* one will be called when running JUnit over this class.)\n");
    buf.append("*/\n");
//    if (! elementary) buf.append("public ");
    buf.append("def testX() {\n}\n\n");
    buf.append("/** Sample test method which tests no program code. */\n");
    buf.append("def testNothing() {\n");
    buf.append("assertTrue(\"Dummy Test\", true)\n");
    buf.append("println(\"TESTING Nothing\")\n");
    buf.append("}\n");
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
    * and on a successful open, the fileOpened() event is fired. This method also checks if there was previously
    * a single unchanged, untitled document open, and if so, closes it after a successful opening.
    * @param com a command pattern command that selects what file to open
    * @return The open document, or null if unsuccessful
    * @exception IOException
    * @exception OperationCanceledException if the open was canceled
    * @exception AlreadyOpenException if the file is already open
    */
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws
    IOException, OperationCanceledException, AlreadyOpenException {
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    if (! closeUntitled) addToBrowserHistory();
    
    OpenDefinitionsDocument oldDoc = _activeDocument;
    OpenDefinitionsDocument openedDoc = openFileHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);
//    Utilities.showDebug("DrJava has opened" + openedDoc + " and is setting it active");
//    addToBrowserHistory();
    setActiveDocument(openedDoc);
    setProjectChanged(true);
//    Utilities.showDebug("active doc set; openFile returning");
    return openedDoc;
  }
  
  protected OpenDefinitionsDocument openFileHelper(FileOpenSelector com) throws IOException,
    OperationCanceledException, AlreadyOpenException {
    
    // This code is duplicated in MainFrame._setCurrentDirectory(File) for safety.
    final File file = (com.getFiles())[0].getCanonicalFile();  // may throw an IOException if path is invalid
    OpenDefinitionsDocument odd = _openFile(file);
//    Utilities.showDebug("File " + file + " opened");
    // Make sure this is on the classpath
    addDocToClassPath(odd);  // Redundant; done in _openFile
    setClassPathChanged(true);
    return odd;
  }
  
  /** Open multiple files and add them to the pool of definitions documents.  The provided file selector chooses
    * a collection of files, and on successfully opening each file, the fileOpened() event is fired.  This method
    * also checks if there was previously a single unchanged, untitled document open, and if so, closes it after
    * a successful opening.
    * @param com a command pattern command that selects what file to open
    * @return The open document, or null if unsuccessful
    * @exception IOException
    * @exception OperationCanceledException if the open was canceled
    * @exception AlreadyOpenException if the file is already open
    */
  public OpenDefinitionsDocument[] openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    if (! closeUntitled) addToBrowserHistory();
    OpenDefinitionsDocument oldDoc = _activeDocument;
    
    OpenDefinitionsDocument[] openedDocs = openFilesHelper(com);
    if (openedDocs.length > 0) {
      if (closeUntitled) closeFileHelper(oldDoc);
//      addToBrowserHistory();
      setActiveDocument(openedDocs[0]);
    }
    return openedDocs;
  }
  
  protected OpenDefinitionsDocument[] openFilesHelper(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    final File[] files = com.getFiles();
    if (files == null) { throw new IOException("No Files returned from FileSelector"); }
    OpenDefinitionsDocument[] docs = _openFiles(files);
    return docs;
  }
  
  // if set to true, and uncommented, the definitions document will
  // print out a small stack trace every time getDocument() is called
  
  //    static boolean SHOW_GETDOC = false;
  
  /** Opens all the files in the list, and notifies about the last file opened. */
  private OpenDefinitionsDocument[] _openFiles(File[] files)
    throws IOException, OperationCanceledException, AlreadyOpenException {    
    
    ArrayList<OpenDefinitionsDocument> alreadyOpenDocuments = new ArrayList<OpenDefinitionsDocument>();
    ArrayList<OpenDefinitionsDocument> retDocs = new ArrayList<OpenDefinitionsDocument>();
    
    //        SHOW_GETDOC = true;
    
    LinkedList<File> filesNotFound = new LinkedList<File>();
    LinkedList<OpenDefinitionsDocument> filesOpened = new LinkedList<OpenDefinitionsDocument>();
    for (final File f: files) {
      if (f == null) throw new IOException("File name returned from FileSelector is null");
      try {
        OpenDefinitionsDocument d = _rawOpenFile(IOUtil.attemptCanonicalFile(f));
        //always return last opened Doc
        retDocs.add(d);
        filesOpened.add(d);
        if(_state.isExcludedFile(f))
          _state.removeExcludedFile(f);
      }
      catch (AlreadyOpenException aoe) {
        OpenDefinitionsDocument d = aoe.getOpenDocument();
        retDocs.add(d);
        alreadyOpenDocuments.add(d);
      }
      catch(FileNotFoundException e) { filesNotFound.add(f); }
    }
    
    for (final OpenDefinitionsDocument d: filesOpened) {
      _completeOpenFile(d); // contains view-related calls
    }
    //        SHOW_GETDOC = false;
    if (filesNotFound.size() > 0)
      _notifier.filesNotFound( filesNotFound.toArray( new File[filesNotFound.size()] ) );
    
    if (! alreadyOpenDocuments.isEmpty()) {
      for(OpenDefinitionsDocument d : alreadyOpenDocuments) {
        _notifier.handleAlreadyOpenDocument(d);
        _notifier.fileOpened(d);
      }
    }                                   
    
    if (retDocs != null) {
      return retDocs.toArray(new OpenDefinitionsDocument[0]);
    }
    else {
      //if we didn't open any files, then it's just as if they cancelled it...
      throw new OperationCanceledException();
    }
  }
  
  
  //----------------------- End ILoadDocuments Methods -----------------------//
  
  /** Opens all files in the specified folder dir and places them in the appropriate places in the document navigator.
    * If "open folders recursively" is checked, this operation opens all files in the subtree rooted at dir.
    */
  public void openFolder(File dir, boolean rec, String ext)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    debug.logStart();
    
    final File[] sfiles =  getFilesInFolder(dir, rec, ext); 
    if (sfiles == null) return;
    openFiles(new FileOpenSelector() { public File[] getFiles() { return sfiles; } });
    
    if (sfiles.length > 0 && _state.inProjectPath(dir)) setProjectChanged(true);
    
    debug.logEnd();
  }
  
  /** @return the file extension for the "Open Folder..." command for the currently selected compiler. */
  public String getOpenAllFilesInFolderExtension() {
    CompilerModel cm = getCompilerModel();
    if (cm==null) {
      return OptionConstants.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)];
    }
    else {
      return cm.getActiveCompiler().getOpenAllFilesInFolderExtension();
    }
  }
  
  public File[] getFilesInFolder(File dir, boolean rec, String ext) throws IOException, OperationCanceledException, 
    AlreadyOpenException {
    
    if (dir == null || !dir.isDirectory()) return null; // just in case
    
    Iterable<File> filesIterable;
    
    String extension = ext.substring(1); // do not include the dot ("scala", not ".scala")
    
    Predicate<File> match = LambdaUtil.and(IOUtil.IS_FILE, IOUtil.extensionFilePredicate(extension));
    if (rec) { filesIterable = IOUtil.listFilesRecursively(dir, match); }
    else { filesIterable = IOUtil.attemptListFilesAsIterable(dir, match); }
    List<File> files = CollectUtil.makeList(filesIterable);
    
    if (isProjectActive()) {
      Collections.sort(files, new Comparator<File>() {
        public int compare(File o1,File o2) {
          return - o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
      });
    }
    else {
      Collections.sort(files, new Comparator<File>() {
        public int compare(File o1,File o2) {
          return - o1.getName().compareTo(o2.getName());
        }
      });
    }
    int ct = files.size();
    
    return files.toArray(new File[ct]);
  }
  
  /** gets files in the project source directory that are not accounted for in the project file.
    * @return null if not in project mode
    */
  public File[] getNewFilesInProject() {
    
    ArrayList<File> files = new ArrayList<File>();
    File projRoot = _state.getProjectRoot();
    if(projRoot == null)
      return null;
    File[] allFiles;
    try {
      allFiles = getFilesInFolder(projRoot, true, getOpenAllFilesInFolderExtension());
    } catch(IOException e) { return null; }
    catch(OperationCanceledException e) { return null; }
    catch(AlreadyOpenException e) { return null; }
    
    for(File f : allFiles) {
      if(!isAlreadyOpen(f) && !_state.isExcludedFile(f)) {
        files.add(f);
      }
    }
    
    return files.toArray(new File[files.size()]);
  }
  
  /** Searches the source folder (recursively) for new files and opens them.
    */
  public void openNewFilesInProject() {
    File[] newFiles = getNewFilesInProject();
    if (newFiles == null) return;
    try { _openFiles(newFiles); }
    catch(Exception e) { }
  }
  
  
  /** Saves all open files, prompting for names if necessary.
    * When prompting (i.e., untitled document), set that document as active.
    * @param com a FileSaveSelector
    * @exception IOException
    */
  public void saveAllFiles(FileSaveSelector com) throws IOException {
//    OpenDefinitionsDocument curdoc = getActiveDocument();
    saveAllFilesHelper(com);
    refreshActiveDocument(); // Return focus to previously active doc
  }
  
  /** Called by saveAllFiles in DefaultGlobalModel */
  protected void saveAllFilesHelper(FileSaveSelector com) throws IOException {
    boolean first = true;
    boolean isProjActive = isProjectActive();
    
    List<OpenDefinitionsDocument> docsToWrite = getOpenDefinitionsDocuments();
    while(docsToWrite.size() > 0) {
      ArrayList<OpenDefinitionsDocument> readOnlyDocs = new ArrayList<OpenDefinitionsDocument>();
      for (final OpenDefinitionsDocument doc: docsToWrite) {  // getOpen... makes a copy
        // do not force Untitled document to be saved if projectActive() or unmodified
        if (doc.isUntitled() && (isProjActive || ! doc.isModifiedSinceSave())) continue;
        try {
          final File docFile = doc.getFile();
          if (docFile == null || !docFile.exists() || docFile.canWrite()) {
            // file is writable, save
            aboutToSaveFromSaveAll(doc);
            doc.saveFile(com);
          }
          else if (first) {
            // file is read-only, ask user about it once
            readOnlyDocs.add(doc);
          }
        }
        catch(FileMovedException fme) {
          // file was moved, but we should still be able to save it
          aboutToSaveFromSaveAll(doc);
          doc.saveFile(com);
        }
      }
      docsToWrite.clear();
      if (readOnlyDocs.size() > 0) {
        ArrayList<File> files = new ArrayList<File>();
        for(OpenDefinitionsDocument odd: readOnlyDocs) {
          try { 
            File roFile = odd.getFile();
            files.add(roFile);
          }
          catch(FileMovedException fme) { /* ignore, don't know what to do here */ }
        }
        File[] res = _notifier.filesReadOnly(files.toArray(new File[files.size()]));
        HashSet<File> rewriteFiles = new HashSet<File>(java.util.Arrays.asList(res));
        for(OpenDefinitionsDocument odd: readOnlyDocs) {
          File roFile = odd.getFile();
          if (rewriteFiles.contains(roFile)) {
            docsToWrite.add(odd);
            FileOps.makeWritable(roFile);
          }
        }
      }
      first = false;
    }
  }
  
  /** Creates a new FileGroupingState for specificed project file and default values for other properties.
    * @param projFile the new project file (which does not yet exist in the file system)
    */
  public void createNewProject(File projFile) { setFileGroupingState(new ProjectFileGroupingState(projFile)); }
  
  /** Configures a new project (created by createNewProject) and writes it to disk; only runs in event thread. */
  public void configNewProject() throws IOException {
    
    assert EventQueue.isDispatchThread();
    
//    FileGroupingState oldState = _state;
    File projFile = getProjectFile();
    
    ProjectProfile builder = new ProjectProfile(projFile);
    
    // FileLists for project file    
    File projectRoot = builder.getProjectRoot();
    
//    Utilities.show("Fetched project root is " + projectRoot);
    
//    List<File> exCp = new LinkedList<File>();  // not used
    
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) {
      
      File f = doc.getFile();
      
      if (! doc.isUntitled()) {
        if (IOUtil.isMember(f, projectRoot)) {
          DocFile file = new DocFile(f);
          file.setPackage(doc.getPackageName());  // must save _packageName so it is correct when project is loaded
          builder.addSourceFile(file);
        }
        else if ( doc.isAuxiliaryFile()) {
          DocFile file = new DocFile(f);
          file.setPackage(doc.getPackageName());  // must save _packageName so it is correct when project is loaded
          builder.addAuxiliaryFile(new DocFile(f));
        }
      }
    }
    
    // write to disk
    builder.write();
    
    _loadProject(builder);
  }
  
  /** Writes the project profile augmented by usage info to specified file.  Assumes DrJava is in project mode.
    * @param file where to save the project
    * @param info
    */
  @SuppressWarnings("unchecked")
  public ProjectProfile _makeProjectProfile(File file, HashMap<OpenDefinitionsDocument, DocumentInfoGetter> info) 
    throws IOException {    
    ProjectProfile builder = new ProjectProfile(file);
    
    // add project root
    File pr = getProjectRoot();
    if (pr != null) builder.setProjectRoot(pr);
    
    // add opendefinitionsdocument
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) {
      if (doc.inProjectPath()) {
        DocumentInfoGetter g = info.get(doc);
        builder.addSourceFile(g);
      }
      else if (doc.isAuxiliaryFile()) {
        DocumentInfoGetter g = info.get(doc);
        builder.addAuxiliaryFile(g);
      }
    }
    
    // add collapsed path info
    if (_documentNavigator instanceof JTreeSortNavigator<?>) {
      String[] paths = ((JTreeSortNavigator<?>)_documentNavigator).getCollapsedPaths();
      for (String s : paths) { builder.addCollapsedPath(s); }
    }
    
    Iterable<AbsRelFile> exCp = getExtraProjectClassPath();
    if (exCp != null) {
      for (AbsRelFile f : exCp) { builder.addClassPathFile(f); }
    }
//    else System.err.println("Project ClasspathVector is null!");
    
    // add build directory
    File bd = getBuildDirectory();
    if (bd != FileOps.NULL_FILE) builder.setBuildDirectory(bd);
    
    // add working directory
    File wd = getWorkingDirectory();  // the value of the working directory to be stored in the project
    if (wd != FileOps.NULL_FILE) builder.setWorkingDirectory(wd);
    
    // add jar main class
    String mainClass = getMainClass();
    if (mainClass != null) builder.setMainClass(mainClass);
    
    // add create jar file
    File createJarFile = getCreateJarFile();
    if (createJarFile != null) builder.setCreateJarFile(createJarFile);
    
    int createJarFlags = getCreateJarFlags();
    if (createJarFlags != 0) builder.setCreateJarFlags (createJarFlags);
    
    // add breakpoints and watches
    ArrayList<DebugBreakpointData> l = new ArrayList<DebugBreakpointData>();  
    for (OpenDefinitionsDocument odd: _breakpointManager.getDocuments()) {
      for(Breakpoint bp: _breakpointManager.getRegions(odd)) { l.add(bp); }
    }
    builder.setBreakpoints(l);
    try { builder.setWatches(getDebugger().getWatches()); }
    catch(DebugException de) { /* ignore, just don't store watches */ }
    
    // add bookmarks
    builder.setBookmarks(_bookmarkManager.getFileRegions());
    
    builder.setAutoRefreshStatus(_state.getAutoRefreshStatus());
    
    //add excluded files
    for(File f: _state.getExclFiles()) { builder.addExcludedFile(f); }
    
    //add custom manifest
    builder.setCustomManifest(_state.getCustomManifest());
    
    // update preference values here
    Map<OptionParser<?>,String> sp = _state.getPreferencesStoredInProject();
    for(OptionParser<?> key: sp.keySet()) {
      sp.put(key, DrJava.getConfig().getOptionMap().getString(key));
    }
    builder.setPreferencesStoredInProject(sp);
    _state.setPreferencesStoredInProject(sp);
    
    return builder;
  }
  
  /** Writes the project profile augmented by usage info to specified file.  Assumes DrJava is in project mode.
    * @param file where to save the project
    */
  public void saveProject(File file, HashMap<OpenDefinitionsDocument, DocumentInfoGetter> info) throws IOException {
    // if file is read-only, ask if it should be made writable
    if (file.exists() && !file.canWrite()) {
      File[] res = _notifier.filesReadOnly(new File[] {file});
      for(File roFile: res) {
        FileOps.makeWritable(roFile);
      }
      if (res.length == 0) { return; /* read-only, do not overwrite */ }
    }
    
    ProjectProfile builder = _makeProjectProfile(file, info);
    // write to disk
    builder.write();
    
//    synchronized(_auxiliaryFiles) {
//      _auxiliaryFiles = new LinkedList<File>();
//      for (File f: builder.getAuxiliaryFiles()) { _auxiliaryFiles.add(f); }
//    }
    
    setFileGroupingState(makeProjectFileGroupingState(builder.getProjectRoot(), builder.getMainClass(), 
                                                      builder.getBuildDirectory(), builder.getWorkingDirectory(), file,
                                                      builder.getSourceFiles(), builder.getAuxiliaryFiles(), 
                                                      builder.getExcludedFiles(),
                                                      builder.getClassPaths(), builder.getCreateJarFile(), 
                                                      builder.getCreateJarFlags(), builder.getAutoRefreshStatus(),
                                                      builder.getCustomManifest(),
                                                      builder.getPreferencesStoredInProject()));
  }
  
  /** Writes the project profile in the old project format.  Assumes DrJava is in project mode.
    * @param file where to save the project
    */
  public void exportOldProject(File file, HashMap<OpenDefinitionsDocument,DocumentInfoGetter> info) throws IOException {
    ProjectProfile builder = _makeProjectProfile(file, info);
    
    // write to disk
    builder.writeOld();
    
//    synchronized(_auxiliaryFiles) {
//      _auxiliaryFiles = new LinkedList<File>();
//      for (File f: builder.getAuxiliaryFiles()) { _auxiliaryFiles.add(f); }
//    }
    
    setFileGroupingState(makeProjectFileGroupingState(builder.getProjectRoot(), builder.getMainClass (), 
                                                      builder.getBuildDirectory(), builder.getWorkingDirectory(), file,
                                                      builder.getSourceFiles(), builder.getAuxiliaryFiles(),
                                                      builder.getExcludedFiles(),
                                                      builder.getClassPaths(), builder.getCreateJarFile(), 
                                                      builder.getCreateJarFlags(), builder.getAutoRefreshStatus(),
                                                      builder.getCustomManifest(),
                                                      builder.getPreferencesStoredInProject()));
  }
  
  public void reloadProject(File file, HashMap<OpenDefinitionsDocument, DocumentInfoGetter> info) throws IOException {
    boolean projChanged = isProjectChanged();
    ProjectProfile builder = _makeProjectProfile(file, info);
    _loadProject(builder);
    setProjectChanged(projChanged);
  }
  
  /** Parses the given project file and loads it into the document navigator and resets interactions pane. Assumes
    * preceding project, if any, has already been closed.
    *
    * @param projectFile The project file to parse
    */
  public void openProject(File projectFile) throws IOException, MalformedProjectFileException {
    _loadProject(ProjectFileParserFacade.ONLY.parse(projectFile));
  }
  
  /** Loads the specified project into the document navigator and opens all of the files (if not already open).
    * Assumes that any prior project has been closed.  Only runs in event thread.
    * @param ir The project file to load
    */
  private void _loadProject(final ProjectFileIR ir) throws IOException {
    
    assert EventQueue.isDispatchThread();
    
    final DocFile[] srcFiles = ir.getSourceFiles();
    final DocFile[] auxFiles = ir.getAuxiliaryFiles();
    final DocFile[] excludedFiles = ir.getExcludedFiles();
    final File projectFile = ir.getProjectFile();
    File pr = ir.getProjectRoot();
    
    try { pr = pr.getCanonicalFile(); }
    catch(IOException ioe) { /* could not canonize file, we'll take what we have */ }
    
    final File projectRoot = pr;
    final File buildDir = ir.getBuildDirectory ();
    final File workDir = ir.getWorkingDirectory();
    final String mainClass = ir.getMainClass();
    final Iterable<AbsRelFile> projectClassPaths = ir.getClassPaths();
    final File createJarFile  = ir.getCreateJarFile ();
    int createJarFlags = ir.getCreateJarFlags();
    final boolean autoRefresh = ir.getAutoRefreshStatus();
    final String manifest = ir.getCustomManifest();
    final Map<OptionParser<?>,String> storedPreferences = ir.getPreferencesStoredInProject();
    
    // clear browser, breakpoint, and bookmark histories
    
    if (! _browserHistoryManager.getRegions().isEmpty()) _browserHistoryManager.clearBrowserRegions();
    if (! _breakpointManager.getDocuments().isEmpty()) _breakpointManager.clearRegions();
    if (! _bookmarkManager.getDocuments().isEmpty()) _bookmarkManager.clearRegions();
    
    final String projfilepath = projectRoot.getCanonicalPath();
    
    // Get the list of documents that are still open
    
//    Utilities.showDebug("openProject called with file " + projectFile);
    
    // Sets up the filters that cause documents to load in different sections of the tree.  The names of these
    // sections are set from the methods such as getSourceBinTitle().  Changing this changes what is considered
    // source, aux, and external.
    
    List<Pair<String, INavigatorItemFilter<OpenDefinitionsDocument>>> l =
      new LinkedList<Pair<String, INavigatorItemFilter<OpenDefinitionsDocument>>>();
    
    INavigatorItemFilter<OpenDefinitionsDocument> navItem1 = new INavigatorItemFilter<OpenDefinitionsDocument>() {
      public boolean accept(OpenDefinitionsDocument d) { return d.inProjectPath(); }
    };
    
    l.add(new Pair<String, INavigatorItemFilter<OpenDefinitionsDocument>>(getSourceBinTitle(), navItem1));
    
    INavigatorItemFilter<OpenDefinitionsDocument> navItem2 = new INavigatorItemFilter<OpenDefinitionsDocument>() {
      public boolean accept(OpenDefinitionsDocument d) { return d.isAuxiliaryFile(); }
    };
    
    l.add(new Pair<String, INavigatorItemFilter<OpenDefinitionsDocument>>(getAuxiliaryBinTitle(), navItem2));
    
    INavigatorItemFilter<OpenDefinitionsDocument> navItem3 = new INavigatorItemFilter<OpenDefinitionsDocument>() {
      public boolean accept(OpenDefinitionsDocument d) {
        return !(d.inProject() || d.isAuxiliaryFile()) || d.isUntitled();
      }
    };
    
    l.add(new Pair<String, INavigatorItemFilter<OpenDefinitionsDocument>>(getExternalBinTitle(), navItem3));
    
    IDocumentNavigator<OpenDefinitionsDocument> newNav =
      new AWTContainerNavigatorFactory<OpenDefinitionsDocument>().
      makeTreeNavigator(projfilepath, getDocumentNavigator(), l);
    
    setDocumentNavigator(newNav);
    
    setFileGroupingState(makeProjectFileGroupingState(projectRoot, mainClass, buildDir, workDir, projectFile, srcFiles,
                                                      auxFiles, excludedFiles, projectClassPaths, createJarFile, 
                                                      createJarFlags, autoRefresh, manifest, storedPreferences));
    
    resetInteractions(getWorkingDirectory());  // Reset interactions pane in new working directory
    
    ArrayList<DocFile> projFiles = new ArrayList<DocFile>();
    DocFile active = null;
    
    // Collection of documents that have been modified outside of DrJava
    ArrayList<DocFile> modifiedFiles = new ArrayList<DocFile>();
    for (DocFile f: srcFiles) {
      if (f.lastModified() > f.getSavedModDate()) {
        modifiedFiles.add(f);
        f.setSavedModDate (f.lastModified());
      }
      
      if (f.isActive()) { active = f; }
      projFiles.add(f);
    }
    for (DocFile f: auxFiles) {
      if (f.lastModified() > f.getSavedModDate()) {
        modifiedFiles.add(f);
        f.setSavedModDate (f.lastModified());
      }
      if (f.isActive()) { active = f; }
      projFiles.add(f);
    }
    
//    Utilities.showDebug("Project files are: " + projFiles);
    
    final List<OpenDefinitionsDocument> projDocs = getProjectDocuments();  // already OPEN project documents
    
    // No files from the previous project (if any) can be open since it was already closed.  
    // But all other files open at time this project is loaded are eligible for inclusion in the new project.  
    
    if (! projDocs.isEmpty())
    for (OpenDefinitionsDocument d: projDocs) {
      try {
        final String path = fixPathForNavigator(d.getFile().getCanonicalPath());
        _documentNavigator.refreshDocument(d, path);  // this operation must run in event thread
      }
      catch(IOException e) { /* Do nothing */ }
    }
    
//    Utilities.showDebug("Preparing to refresh navigator GUI");
    // call on the GUI to finish up by opening the files and making necessary gui component changes
    final DocFile[] filesToOpen = projFiles.toArray(new DocFile[projFiles.size()]);
    _notifier.openProject(projectFile, new FileOpenSelector() {
      public File[] getFiles() { return filesToOpen; }
    });
    
    /* Files are opened synchronously by the preceding notification.  If this process is made asynchronous, we need to 
     * wait here (using the projectLoaded CompletionMonitor above (commented out). */
    // set breakpoints
    for (DebugBreakpointData dbd: ir.getBreakpoints()) {
      try {
        File f = dbd.getFile();
        if (! modifiedFiles.contains(f)) {
          int lnr = dbd.getLineNumber();
          OpenDefinitionsDocument odd = getDocumentForFile(f);
          getDebugger().toggleBreakpoint(odd, odd._getOffset(lnr), dbd.isEnabled());
        }
      }
      catch(DebugException de) { /* ignore, just don't add breakpoint */ }
    }
    
    //Set active document from project file
    if (active != null) setActiveDocument(getDocumentForFile(active));
    
    // set watches
    try { getDebugger().removeAllWatches(); }
    catch(DebugException de) { /* ignore, just don't remove old watches */ }
    for (DebugWatchData dwd: ir.getWatches()) {
      try { getDebugger().addWatch( dwd.getName()); }
      catch(DebugException de) { /* ignore, just don't add watch */ }
    }
    
    // set bookmarks
    for (FileRegion bm: ir.getBookmarks()) {
      File f = bm.getFile();
      if (! modifiedFiles.contains(f)) {
        OpenDefinitionsDocument odd = getDocumentForFile(f);
        int start = bm.getStartOffset();
        int end = bm.getEndOffset();
        if (getOpenDefinitionsDocuments().contains(odd) && 
            _bookmarkManager.getRegionsOverlapping(odd, start, end).size() == 0) { // bookmark is valid
          try { 
            int lineStart = odd._getLineStartPos(start);
            int lineEnd = odd._getLineEndPos(end);
            _bookmarkManager.addRegion(new MovingDocumentRegion(odd, start, end, lineStart, lineEnd)); 
          }
          catch(Exception e) { DrJavaErrorHandler.record(e); }  // should never happen
        }
        // should remove stale bookmark
      }
    }
    
    if (_documentNavigator instanceof JTreeSortNavigator<?>) 
      ((JTreeSortNavigator<?>)_documentNavigator).collapsePaths(ir.getCollapsedPaths()); 
    
    // project has not been changed yet, but...
    setProjectChanged(false);
    
    // it may change here, in the auto-refresh on open    
    if (_state.getAutoRefreshStatus()) openNewFilesInProject(); 
  }  // end _loadProject
  
  /** Perform an auto-refresh of the project, adding new source files to the project. */
  public void autoRefreshProject() { openNewFilesInProject(); }
  
  /** Performs any needed operations on the model after project files have been closed.  This method is not 
    * responsible for closing any files; both the files in the project and the project file have already been 
    * closed (by MainFrame._closeProject()).  Resets interations unless suppressReset is true.
    */
  public void closeProject(boolean suppressReset) {
    setDocumentNavigator(new AWTContainerNavigatorFactory<OpenDefinitionsDocument>().
                           makeListNavigator(getDocumentNavigator()));
    setFileGroupingState(makeFlatFileGroupingState());
    
    // remove previous listeners
    removePreviousListeners();
    
    if (! suppressReset) resetInteractions(getWorkingDirectory());
    _notifier.projectClosed();
    setActiveDocument(getDocumentNavigator().getDocuments().get(0));
  }
  
  /** If the document is untitled, brings it to the top so that the
    * user will know which is being saved.
    */
  public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {
    if (doc.isUntitled()) setActiveDocument(doc);
  }
  
  /** Closes an open definitions document, prompting to save if the document has been changed.  Returns whether
    * the file was successfully closed.  Also ensures the invariant that there is always at least
    * one open document holds by creating a new file if necessary.
    * @return true if the document was closed
    */
  public boolean closeFile(OpenDefinitionsDocument doc) {
    List<OpenDefinitionsDocument> list = new LinkedList<OpenDefinitionsDocument>();
    list.add(doc);
    return closeFiles(list);
  }
  
  /** Attempts to close all open documents. Also ensures the invariant that there is always at least
    * one open document holds by creating a new file if necessary.  Resets interactions iff operation succeeds.
    * @return true if all documents were closed
    */
  public boolean closeAllFiles() {
    List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
    boolean res = closeFiles(docs);
    if (res) {
      // Close all error panels
      Utilities.invokeLater(new Runnable() { public void run() { _notifier.allFilesClosed(); } });
      
//       _log.log("Resetting interactions pane to use " + getWorkingDirectory() + " as working directory");
      resetInteractions(getWorkingDirectory());
    }
    return res;
  }
  
  /** This function closes a group of files assuming that the files are contiguous in the enumeration
    * provided by the document navigator. This assumption is used in selecting which remaining document
    * (if any) to activate.
    * <p>
    * The corner cases in which the file that is being closed had been externally
    * deleted have been addressed in a few places, namely DefaultGlobalModel.canAbandonFile()
    * and MainFrame.ModelListener.canAbandonFile().  If the DefinitionsDocument for the
    * OpenDefinitionsDocument being closed is not in the cache (see model.cache.DocumentCache)
    * then it is closed without prompting the user to save it.  If it is in the cache, then
    * we can successfully notify the user that the file is selected for closing and ask whether to
    * saveAs, close, or cancel.
    * @param docs the list od OpenDefinitionsDocuments to close
    * @return whether all files were closed
    */
  public boolean closeFiles(List<OpenDefinitionsDocument> docs) {
    if (docs.size() == 0) return true;
    
//    _log.log("closeFiles(" + docs + ") called");
    /* Force the user to save or discard all modified files in docs */
    for (OpenDefinitionsDocument doc : docs) { 
      if (! doc.canAbandonFile()) return false; }
    
    /* If all files are being closed, create a new file before starting in order to have a potentially active file
     * that is not in the list of closing files. */
    if (docs.size() == getDocumentCount()) newFile();
    
    /* Set the active document to the document just after the last document or the document just before the first 
     * document in docs.  The new file created above (if necessary) does not appear in docs. */
    _ensureNotActive(docs);
    
    // Close the files in docs.
    for (OpenDefinitionsDocument doc : docs) { closeFileWithoutPrompt(doc); }  
    return true;
  }
  
  /** Helper for closeFile. This method was the closeFile(...) method before projects were added to DrJava. */
  protected boolean closeFileHelper(OpenDefinitionsDocument doc) {
    //    System.err.println("closing " + doc);
    boolean canClose = doc.canAbandonFile();
    if (canClose) return closeFileWithoutPrompt(doc);
    return false;
  }
  
  /** Closes an open definitions document, without prompting to save if the document has been changed.  Returns
    * whether the file was successfully closed. NOTE: This method should not be called unless it can be
    * absolutely known that the document being closed is not the active document. The closeFile() method in
    * SingleDisplayModel ensures that a new active document is set, but closeFileWithoutPrompt does not.
    * @return true if the document was closed.
    */
  public boolean closeFileWithoutPrompt(final OpenDefinitionsDocument doc) {
    //    new Exception("Closed document " + doc).printStackTrace();
    
//    _log.log("closeFileWithoutPrompt(" + doc + ") called; getRawFile() = " + doc.getRawFile());
//    _log.log("_documentsRepos = " + _documentsRepos);
    boolean found;
    synchronized(_documentsRepos) { found = (_documentsRepos.remove(doc.getRawFile()) != null); }
    
    if (! found) {
//      _log.log("Cannot close " + doc + "; not found!");
      return false;
    }
    
    // remove regions for this file
    _breakpointManager.removeRegions(doc);
    _bookmarkManager.removeRegions(doc);
    
    // The following copy operation is dictated by the silly "no comodification" constraint on Collection iterators
    @SuppressWarnings("unchecked")
    List<RegionManager<MovingDocumentRegion>> managers = new ArrayList<RegionManager<MovingDocumentRegion>>(_findResultsManagers);
    for (RegionManager<MovingDocumentRegion> rm: managers) rm.removeRegions(doc);
    doc.clearBrowserRegions();
    
    // if the document was an auxiliary file, remove it from the list
    if (doc.isAuxiliaryFile()) { removeAuxiliaryFile(doc); }
    
    _documentNavigator.removeDocument(doc);
    _notifier.fileClosed(doc); 
    doc.close();
    return true;
  }
  
  /** Closes all open documents.  This operation can be cancelled by the user since it
    * checks if all files can be abandoned BEFORE it actually modifies the project state.
    * @return  {@code false} if the user cancelled
    */
  public boolean closeAllFilesOnQuit() {
    
    List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
    
    for (OpenDefinitionsDocument doc : docs) {
      if (! doc.canAbandonFile()) { return false; }
    }
    
    // user did not want to cancel, close all of them
    // All files are being closed, create a new file before starting in order to have
    // a potentially active file that is not in the list of closing files.
    newFile();
    
    // Set the active document to the document just after the last document or the document just before the
    // first document in docs.  A new file does not appear in docs.
    _ensureNotActive(docs);
    
    // Close the files in docs.
    for (OpenDefinitionsDocument doc : docs) { closeFileWithoutPrompt(doc); }  
    
    return true;
  }
  
  /** Exits the program.  Only quits if all documents are successfully closed. */
  public void quit() { quit(false); }
  
  /** Halts the program immediately. */
  public void forceQuit() { quit(true); }
  
  /** Exits the program.  If force is true, quits regardless of whether all documents are successfully closed. */
  private void quit(boolean force) {
//    _log.log("quit(" + force + ") called");
    try {
      if (! force && ! closeAllFilesOnQuit()) {
        refreshActiveDocument();  // Ensure that DrJava is in a consistent state.
        return;
      }
      /* [ 1478796 ] DrJava Does Not Shut Down With Project Open. On HP tc1100 and Toshiba Portege tablet PCs, there
       * appears to be a problem in a shutdown hook, presumably the RMI shutdown hook. Shutdown hooks get executed in 
       * Runtime.exit (to which System.exit delegates), and if a shutdown hook does not complete, the VM does not shut
       * down.  The difference between Runtime.halt and Runtime.exit is that Runtime.exit runs the shutdown hooks and
       * the finalizers (if Runtime.runFinalizersOnExit(true) has been called); then it calls Runtime.halt.  The RMI 
       * hooks are potentially important in running unit test that repeatedly start and stop DrJava, so we only invoke
       * Runtime.halt if our attempt to exit times out.
       */
      
      shutdown(force);
    }
    catch(Throwable t) { shutdown(true); /* force exit anyway */ }
  }
  
  /* Terminates DrJava via System.exit with Runtime.halt as a backup if the former gets hung up. */
  private void shutdown(boolean force) {
    if (force) Runtime.getRuntime().halt(0);
    
    dispose();  // kills interpreter and cleans up RMI hooks in the slave JVM
    
    if (DrJava.getConfig().getSetting(OptionConstants.DRJAVA_USE_FORCE_QUIT)) {
      Runtime.getRuntime().halt(0);  // force DrJava to exit
    }
    
    Thread monitor = new Thread(new Runnable() { 
      public void run() {
        try { Thread.sleep(2000); }
        catch(InterruptedException e) { /* proceed */ }
        Runtime.getRuntime().halt(0);  // force DrJava to exit if it still alive
      }
    });
    monitor.setDaemon(true);
    monitor.start();
    System.exit(0);
  }
  
  /** Prepares this model to be thrown away.  Never called outside of tests. This version ignores the slave JVM. */
  public void dispose() {
    synchronized(_documentsRepos) { 
      closeAllFiles();
      _documentsRepos.clear();
    }
    Utilities.invokeLater(new Runnable() {
      public void run() { _documentNavigator.clear(); }  // this operation must run in event thread
    });
    // Only remove listeners after pending events have completed
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.removeAllListeners(); } });
  }
  
  /** Disposes of external resources. This is a no op in AbstractGlobalModel. */
  public void disposeExternalResources() { /* no op */ }
  
  /** Gets the document for the specified file; may involve opening the file. */
  public OpenDefinitionsDocument getDocumentForFile(File file) throws IOException {
    if ((file instanceof NullFile) ||
        (file instanceof FileOps.NullFile)) return null;
    
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
    * TODO: This is not very efficient!
    */
  public boolean isAlreadyOpen(File file) { return (_getOpenDocument(file) != null); }
  
  /** Returns the OpenDefinitionsDocument corresponding to the INavigatorItem/DefinitionsDocument passed in.
    * @param doc the searched for Document
    * @return its corresponding OpenDefinitionsDocument
    */
  public OpenDefinitionsDocument getODDForDocument(AbstractDocumentInterface doc) {
    /** This function needs to be phased out altogether; the goal is for the OpenDefinitionsDocument
      * to also function as its own Document, so this function will be useless
      */
    if (doc instanceof OpenDefinitionsDocument) return (OpenDefinitionsDocument) doc;
    if  (doc instanceof DefinitionsDocument) return ((DefinitionsDocument) doc).getOpenDefDoc();
    throw new IllegalStateException("Could not get the OpenDefinitionsDocument for Document: " + doc);
  }
  
  /** Gets a DocumentIterator to allow navigating through open Swing Documents. */
  public DocumentIterator getDocumentIterator() { return this; }
  
  /** Returns the ODD preceding the given document in the document list.
    * NOTE: the returned document may be null if the document wasn't found and the user did not want to continue.
    * @param d the current Document
    * @return the next Document (or null if not found and user did not want to continue)
    */
  public OpenDefinitionsDocument getNextDocument(OpenDefinitionsDocument d) { return getNextDocument(d, null); }
  
  /** Returns the ODD preceding the given document in the document list.
    * NOTE: the returned document may be null if the document wasn't found and the user did not want to continue.
    * @param d the current Document
    * @param frame the frame that should serve as parent for a potential dialog window
    * @return the next Document (or null if not found and user did not want to continue)
    */
  public OpenDefinitionsDocument getNextDocument(OpenDefinitionsDocument d, Component frame) {
    OpenDefinitionsDocument nextdoc = null; // irrelevant initialization required by javac
//    try {
    OpenDefinitionsDocument doc = getODDForDocument(d);
    nextdoc = _documentNavigator.getNext(doc);
    if (nextdoc == doc) nextdoc = _documentNavigator.getFirst();  // wrap around if necessary
    OpenDefinitionsDocument res = getNextDocHelper(nextdoc, frame);
//      Utilities.showDebug("nextDocument(" + d + ") = " + res);
    return res;
//    }
//    catch(DocumentClosedException dce) { return getNextDocument(nextdoc); }
  }
  
  /** Checks that the document exists, and if it doesn't, asks if the user wants to continue.
    * NOTE: the returned document may be null if the document wasn't found and the user did not want to continue.
    * @param d the next Document
    * @param frame the frame that should serve as parent for a potential dialog window
    * @return the next Document (or null if not found and user did not want to continue)
    */
  private OpenDefinitionsDocument getNextDocHelper(OpenDefinitionsDocument nextdoc, Component frame) {
    if ( nextdoc.isUntitled() || nextdoc.verifyExists()) return nextdoc;
    // Note: verifyExists prompts user for location of the file if it is not found
    int rc = JOptionPane.showConfirmDialog(frame, "Files not found, continue to next document?", "Continue?",
                                           JOptionPane.YES_NO_OPTION); 
    if (rc == JOptionPane.NO_OPTION)
      return null;
    // cannot find nextdoc; move on to next document
    return getNextDocument(nextdoc, frame);
  }
  
  /** Returns the ODD preceding the given document in the document list.
    * @param d the current Document
    * @return the previous Document
    */
  public OpenDefinitionsDocument getPrevDocument(OpenDefinitionsDocument d) {
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
  
  /** @return the size of the collection of OpenDefinitionsDocuments */
  public int getDocumentCount() { return _documentsRepos.size(); }
  
  /** Returns a new collection of all documents currently open for editing.  This is equivalent to the results of 
    * getDocumentForFile for the set of all files for which isAlreadyOpen returns true.
    * @return a random-access List of the open definitions documents..
    */
  public List<OpenDefinitionsDocument> getOpenDefinitionsDocuments() {
    synchronized(_documentsRepos) {
      ArrayList<OpenDefinitionsDocument> docs = new ArrayList<OpenDefinitionsDocument>(_documentsRepos.size());
      for (OpenDefinitionsDocument doc: _documentsRepos.values()) { docs.add(doc); }
      return docs;
    }
  }
  
//  /** Returns a new collection of all language level documents currently open for editing.
//    * @return a random-access List of the open definitions documents..
//    */
//  public List<OpenDefinitionsDocument> getLLOpenDefinitionsDocuments() {
//    synchronized(_documentsRepos) {
//      ArrayList<OpenDefinitionsDocument> docs = new ArrayList<OpenDefinitionsDocument>(_documentsRepos.size());
//      for (OpenDefinitionsDocument doc: _documentsRepos.values()) {
//        File f = doc.getRawFile();
//        if (DrJavaFileUtils.isLLFile(f.getName())) docs.add(doc);
//      }
//      return docs;
//    }
//  }
  
  /* Returns a sorted (by time of insertion) collection of all open documents. */
  public List<OpenDefinitionsDocument> getSortedOpenDefinitionsDocuments() { return getOpenDefinitionsDocuments(); }
  
  /** @return true if all open documents are in sync with their primary class files. */
  public boolean hasOutOfSyncDocuments() { return getOutOfSyncDocuments().size() > 0; }
  
  public boolean hasOutOfSyncDocuments(List<OpenDefinitionsDocument> lod) {
    List<OpenDefinitionsDocument> oos = getOutOfSyncDocuments();
    for(OpenDefinitionsDocument doc: lod) {
      if (oos.contains(doc)) return true;
    }
    return false;
  }
  
  /** @return true if all open documents are in sync with their primary class files. */
  public List<OpenDefinitionsDocument> getOutOfSyncDocuments() { return getOutOfSyncDocuments(getOpenDefinitionsDocuments()); }
  
  public List<OpenDefinitionsDocument> getOutOfSyncDocuments(List<OpenDefinitionsDocument> lod) {
    _log.log("AbstractGlobalModel.getOutOfSyncDocuments(" + lod + ") called");
    List<OpenDefinitionsDocument> outOfSync = new ArrayList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument doc: lod) {
      _log.log("Inspecting " + doc + " for OUT OF SYNC");
      _log.log("doc.isSourceFile() returns " + doc.isSourceFile() + " doc.checkIfClassFileInSync returns " + doc.checkIfClassFileInSync() );
      if (doc.isSourceFile() &&
          (! isProjectActive() || doc.inProjectPath() || doc.isAuxiliaryFile()) &&
          (! doc.checkIfClassFileInSync())) {
        // If the document doesn't produce a *.class file, then this will be considered "out-of-sync",
        // even though it isn't. We don't really have many options here unless we actually parse the
        // document.
        // As a heuristic, we check if there is any non-comment text that includes the words "class",
        // "interface" or "enum". If the file doesn't, we presume it is empty and doesn't generate
        // a class file; therefore, it cannot ever be out of sync.
        try {
          boolean b = doc.containsSource();
          if (b) outOfSync.add(doc);
        }
        catch(BadLocationException e) {
          outOfSync.add(doc);
        }
      }
    }
    return outOfSync;
  }
  
  /** Set the indent tab size for all definitions documents.
    * @param indent the number of spaces to make per level of indent
    */
  void setDefinitionsIndent(int indent) {
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) { doc.setIndent(indent); }
  }
  
  /** A degenerate operation since this has no slave JVM and no interactions model. */
  public void resetInteractions(File wd) { /* do nothing */ }
  
  /** A degenerate operation since this has no slave JVM and no interactions model. */
  public void resetInteractions(File wd, boolean forceReset) { /* do nothing */ }
  
  /** Resets the console. Fires consoleReset() event. */
  public void resetConsole() {
    _consoleDoc.reset("");
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.consoleReset(); } });
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
  public void saveConsoleCopy(ConsoleDocument doc, FileSaveSelector selector) throws IOException {
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
  
  /** Returns the entire history as a String with semicolons as needed. */
  public String getHistoryAsStringWithSemicolons() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }
  
  /** Throws UnsupportedOperationException */
  public String getHistoryAsString() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }
  
  /** Registers OptionListeners.  Factored out code from the two constructor. */
  private void _registerOptionListeners() {
//    // Listen to any relevant config options
//    DrJava.getConfig().addOptionListener(EXTRA_CLASSPATH, new ExtraClasspathOptionListener());
    
    // The following is unnecessary because the DefaultFileSaver constructor directly uses BACKUP_FILES
//    DrJava.getConfig().addOptionListener(BACKUP_FILES, new BackUpFileOptionListener());
//    Boolean makeBackups = DrJava.getConfig().getSetting(BACKUP_FILES);
//    FileOps.DefaultFileSaver.setBackupsEnabled(makeBackups.booleanValue ());
    
    DrJava.getConfig().addOptionListener(DYNAMICJAVA_ACCESS_CONTROL, new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        boolean enforceAllAccess = DrJava.getConfig().getSetting(OptionConstants.DYNAMICJAVA_ACCESS_CONTROL)
          .equals(OptionConstants.DynamicJavaAccessControlChoices.PRIVATE_AND_PACKAGE); // "all"
        getInteractionsModel().setEnforceAllAccess(enforceAllAccess);
        
        boolean enforcePrivateAccess = !DrJava.getConfig().getSetting(OptionConstants.DYNAMICJAVA_ACCESS_CONTROL)
          .equals(OptionConstants.DynamicJavaAccessControlChoices.DISABLED); // not "none"
        getInteractionsModel().setEnforcePrivateAccess(enforcePrivateAccess);
      }
    });
    
    DrJava.getConfig().addOptionListener(DYNAMICJAVA_REQUIRE_SEMICOLON, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        getInteractionsModel().setRequireSemicolon(oce.value);
      }
    });
    
    DrJava.getConfig().addOptionListener(DYNAMICJAVA_REQUIRE_VARIABLE_TYPE, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        getInteractionsModel().setRequireVariableType(oce.value);
      }
    });
  }
  
  /** Appends a string to the given document using a particular attribute set.
    * Also waits for a small amount of time (InteractionsModel.WRITE_DELAY) to prevent any one
    * writer from flooding the model with print calls to the point that the
    * user interface could become unresponsive. 
    * Only runs in event thread.
    * @param doc Document to append to
    * @param s String to append to the end of the document
    * @param style the style to print with
    */
  protected void _docAppend(final ConsoleDocument doc, final String s, final String style) {
    Utilities.invokeLater(new Runnable() {
      public void run() { doc.insertBeforeLastPrompt(s, style); }
    });
  }
  
  /** Prints System.out to the DrScala console.  This method can safely be run outside the event thread. */
  public void systemOutPrint(final String s) { _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_OUT_STYLE); }
  
  /** Prints System.err to the DrScala console.  This method can safely be run outside the event thread. */
  public void systemErrPrint(final String s) { _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_ERR_STYLE); }
  
  /** Prints to the DrScala console as an echo of System.in.  This method can safely be run outside the event thread. */
  public void systemInEcho(final String s) { _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_IN_STYLE); }
  
  /** throws UnsupportedOperationException */
  public void printDebugMessage(String s) {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
  }
  
  /** throws new UnsupportedOperationException */
  public Iterable<File> getInteractionsClassPath() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support interactions");
  }
  
  /** Returns a project's extra classpaths; empty for FlatFileGroupingState
    * @return The classpath entries loaded along with the project
    */
  public Iterable<AbsRelFile> getExtraProjectClassPath() { return _state.getExtraProjectClassPath(); }
  
  /** Sets the set of classpath entries to use as the projects set of classpath entries.  This is normally used by the
    * project preferences..
    */
  public void setExtraClassPath(Iterable<AbsRelFile> cp) {
    _state.setExtraClassPath(cp);
    setClassPathChanged(true);
    //System.out.println("Setting project classpath to: " + cp);
  }
  
  /** Return an array of the files excluded from the current project */
  public File[] getExclFiles() { return _state.getExclFiles(); }
  
  /** Sets the array of files excluded from the current project */
  public void setExcludedFiles(File[] fs) { _state.setExcludedFiles(fs); }
  
  /** Gets an array of all sourceRoots for the open definitions documents, without duplicates. */
  public Iterable<File> getSourceRootSet() {
    Set<File> roots = new LinkedHashSet<File>();
    
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) {
      try {
        if (! doc.isUntitled()) {
          File root = doc.getSourceRoot();
          if (root != null && ! roots.contains(root)) roots.add(root); // Can't create duplicate entries in a Set
        }
      }
      catch (InvalidPackageException e) {
//        Utilities.show("InvalidPackageException in getSourceRootSet");
        /* file has invalid package statement; ignore it */
      }
    }
    return roots;
  }
  
//  /** Return the absolute path of the file with the given index, or "(untitled)" if no file exists. */
//  public String getDisplayFullPath(int index) {
//    OpenDefinitionsDocument doc = getOpenDefinitionsDocuments().get(index);
//    if (doc == null) throw new RuntimeException( "Document not found with index " + index);
//    return doc.getDisplayFullPath();
//  }
  
  /** throws UnsupportedOperationException */
  public Debugger getDebugger() {
    // throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
    return NoDebuggerAvailable.ONLY;
  }
  
  /** throws UnsupportedOperationException */
  public int getDebugPort() throws IOException {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support debugging");
  }
  
  /** Checks if any open definitions documents have been modified since last being saved.
    * @return whether any documents have been modified
    */
  public boolean hasModifiedDocuments() { return hasModifiedDocuments(getOpenDefinitionsDocuments()); }
  
  /** Checks if any given documents have been modified since last being saved.
    * @return whether any documents have been modified
    */
  public boolean hasModifiedDocuments(List<OpenDefinitionsDocument> lod) {
    for (OpenDefinitionsDocument doc: lod) {
      if (doc.isModifiedSinceSave()) return true;  
    }
    return false;
  }
  
  /** Checks if any open definitions documents are untitled.
    * @return whether any documents are untitled
    */
  public boolean hasUntitledDocuments() {
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) {
      if (doc.isUntitled()) return true;  
    }
    return false;
  }
  
  /** Searches for a file with the given name on the current source roots and the augmented classpath.
    * @param fileName name of the source file to look for
    * @return the file corresponding to the given name, or null if it cannot be found
    */
  public File getSourceFile(String fileName) {
    Iterable<File> sourceRoots = getSourceRootSet();
    for (File s: sourceRoots) {
      File f = _getSourceFileFromPath(fileName, s);
      if (f != null) return f;
    }
    Vector<File> sourcepath = DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
    return findFileInPaths(fileName, sourcepath);
  }
  
  /** Searches for a file with the given name on the provided paths. Returns NULL_FILE if the file is not found.
    * @param fileName Name of the source file to look for
    * @param paths An array of directories to search
    * @return the file if it is found, or null otherwise
    */
  public File findFileInPaths(String fileName, Iterable<File> paths) {
    for (File p: paths) {
      File f = _getSourceFileFromPath(fileName, p);
      if (f != null) return f;
    }
    return FileOps.NULL_FILE;
  }
  
  /** Gets the file named filename from the given path, if it exists.  Returns NULL_FILE if it's not there.
    * @param fileName the file to look for
    * @param path the path to look for it in
    * @return the file if it exists
    */
  private File _getSourceFileFromPath(String fileName, File path) {
    String root = path.getAbsolutePath();
    File f = new File(root + System.getProperty("file.separator") + fileName);
    return f.exists() ? f : FileOps.NULL_FILE;
  }
  
  /** Add the current location to the browser history.  Only runs in event thread. Assumes that doc is not null. */
  public void addToBrowserHistory() {
    addToBrowserHistory(false);
  }
  
  /** Add the current location to the browser history.  Only runs in event thread. Assumes that doc is not null.
    * @param before true if the location should be inserted before the current region */
  public void addToBrowserHistory(boolean before) {
    assert EventQueue.isDispatchThread();
//    edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("addToBrowserHistory()");
    _notifier.updateCurrentLocationInDoc();
//    edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("addToBrowserHistory: after updateCurrentLocationInDoc");
    final OpenDefinitionsDocument doc = getActiveDocument();
//    assert doc != null && EventQueue.isDispatchThread();
    
    Position startPos = null;
    Position endPos = null;
    try {
      int pos = doc.getCaretPosition();
      startPos = doc.createPosition(pos);
      endPos = startPos; // was doc.createPosition(doc._getLineEndPos(pos));
    }
    
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
//    edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("addToBrowserHistory: startPos = "+startPos.getOffset());
    BrowserDocumentRegion r = new BrowserDocumentRegion(doc, startPos, endPos);
    if (before) {
      _browserHistoryManager.addBrowserRegionBefore(r, _notifier);
    }
    else {
      _browserHistoryManager.addBrowserRegion(r, _notifier);
    }
//    edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("addToBrowserHistory: "+_browserHistoryManager);
  }
  
  /** throws an UnsupportedOperationException */
  public Iterable<File> getClassPath() {
    throw new UnsupportedOperationException("AbstractGlobalModel does not support class paths");
  }
  
  public static boolean isUntitled(final File f) { return f == null || (f instanceof NullFile); }

  /** Update the syntax highlighting for all open documents. */
  public void updateSyntaxHighlighting() {
    for (OpenDefinitionsDocument doc: getOpenDefinitionsDocuments()) { doc.updateSyntaxHighlighting(); }
    // refresh active document so syntax highlighting updates in the currently visible document too
    Utilities.invokeLater(new Runnable() { public void run() { refreshActiveDocument(); } });
  }
  
  // ---------- ConcreteOpenDefDoc inner class ----------
  
  /** A wrapper around a DefinitionsDocument or potential DefinitionsDocument (if it has been kicked out of the cache)
    * The GlobalModel interacts with DefinitionsDocuments through this wrapper.<br>
    * This call was formerly called the <code>DefinitionsDocumentHandler</code> but was renamed (2004-Jun-8) to be more
    * descriptive/intuitive.
    * Note that this class has a natural ordering that determines a coarser equivalence relation than equals.
    */
  class ConcreteOpenDefDoc implements OpenDefinitionsDocument {
    
    public void addBrowserRegion(BrowserDocumentRegion r) { _browserRegions.add(r); }
    
    public void removeBrowserRegion(BrowserDocumentRegion r) { _browserRegions.remove(r); }
    
//     private boolean _modifiedSinceSave;
    
    /** Cached String image of document as last read from or written to disk; initially null */
    private volatile String _image;
    private volatile File _file;
    private volatile long _timestamp;
    
    /** The folder containing this document */
    private volatile File _parentDir;
    
    /** The cached class file for the document */
    private volatile File _classFile;
    
    /** Specifies if classFile is in sync with current state of the document */
    private volatile boolean _classFileInSync = false;
    
    /** The package name embedded in the document the last time is was loaded, reconstructed, or saved.  When loading a
      * project, this information is extracted from the project file eliminating the need to read every document file.  
      * For non-project files, it is extracted from the text of the file.  If there is an error, it is left as "".
      */
    protected volatile String _packageName = "";
    
    /** The fully qualified name of the class with '.' converted to ' ' so that alphabetic ordering works properly. */
    protected volatile String _lexiName = "";
    
    private volatile DCacheAdapter _cacheAdapter;
    
    /** This document's browser regions. */
    protected final Set<BrowserDocumentRegion> _browserRegions;
    
    private volatile int _initVScroll;
    private volatile int _initHScroll;
    private volatile int _initSelStart;
    private volatile int _initSelEnd;
    
    private volatile DrJavaBook _book;
    
    /** Standard constructor for a document read from a file.  Initializes this ODD's DD.  Assumes that f exists.
      * @param f file describing DefinitionsDocument to manage; should be in canonical form
      */
    ConcreteOpenDefDoc(File f) { this(f, f.getParentFile(), f.lastModified()); }
    
    /* Standard constructor for a new document (associated file is NullFile which does not exit in file system). */
    ConcreteOpenDefDoc(NullFile f) { this(f, null, 0L); }
    
    /* General constructor.  Only used privately. */
    private ConcreteOpenDefDoc(File f, File dir, long stamp) {
      
      _file = f;
      _parentDir = dir;
      _classFile = FileOps.NULL_FILE;
      _timestamp = stamp;
      _image = null;
//      _lexiName = null;
    
      // Create reconstructor for document
      try {
        DDReconstructor ddr = makeReconstructor();
//        System.err.println("Registering " + this);
        _cacheAdapter = _cache.register(this, ddr);
      } catch(IllegalStateException e) { throw new UnexpectedException(e); }
      
      if (_file instanceof NullFile)
        _lexiName = ((NullFile) _file).getLexiName();  // multiple untitled files must have distinct lexiNames
      else {
        _lexiName = _file.getPath().replace(File.separatorChar, ' ');
        // _packageName will be set from document (assuming document is not a fresh empty document) in rawOpenFile
//        _packageName = getPackageNameFromDocument(); // performs a lightweight scan of the file using StreamTokenizer
      }
      
      /* The following table is not affected by inconsistency between hashCode/equals in DocumentRegion, because
       * BrowserDocumentRegion is NOT a subclass of DocumentRegion. */
      _browserRegions = new HashSet<BrowserDocumentRegion>();
    }
    
    //------------ Getters and Setters -------------//
    
    /** Returns the file field for this document; does not check whether the file is NullFile or file exists. */
    public File getRawFile() { return _file; }
    
    /** Returns the file for this document, null if the document is untitled.  If the document's
      * file does not exist, this throws a FileMovedException.  If a FileMovedException is thrown, you
      * can retrieve the non-existence source file from the FileMovedException by using the getFile() method.
      * @return the file for this document
      */
    public File getFile() throws FileMovedException {
      File f = _file;  // single read of f
      if (AbstractGlobalModel.isUntitled(f)) return null;  // Should we return NULL_FILE here
      if (f.exists()) return f;
      else throw new FileMovedException(f, "This document's file has been moved or deleted.");
    }
    
    /** Sets the file for this openDefinitionsDocument.  Synch ensures that _file and _timestamp are consistent. */
    public synchronized void setFile(final File file) {
      _file = file;
      if (! AbstractGlobalModel.isUntitled(file)) _timestamp = file.lastModified();
      else _timestamp = 0L;
      updateSyntaxHighlighting();
    }

    /** Update the syntax highlighting for the file type. */
    public void updateSyntaxHighlighting() {
      // can't be called in AbstractGlobalModel.ConcreteOpenDefDoc because getCompilerModel is not supported
      CompilerModel cm = getCompilerModel();
      if (cm == null) {
        // use the cache adapter so setting the keywords doesn't load the document
        _cacheAdapter.setKeywords(edu.rice.cs.drjava.model.compiler.ScalaCompiler.SCALA_KEYWORDS);
      }
      else {
        // use the cache adapter so setting the keywords doesn't load the document
        _cacheAdapter.setKeywords(cm.getActiveCompiler().getKeywordsForFile(_file));
      }
    }
    
    /** Returns the timestamp. */
    public long getTimestamp() { return _timestamp; }
    
    public void setClassFileInSync(boolean inSync) { _classFileInSync = inSync; }
    
    public boolean getClassFileInSync() { return _classFileInSync; }
    
    public void setCachedClassFile(File classFile) { _classFile = classFile; }
    
    public File getCachedClassFile() { return _classFile; }
    
    /** Whenever this document has been saved, this method should be called to update its "isModified" information. */
    public synchronized void resetModification() {
      getDocument().resetModification();
      File f = _file; 
      if (! AbstractGlobalModel.isUntitled(f)) _timestamp = f.lastModified();
    }
    
    /** @return The parent directory; should be in canonical form. */
    public File getParentDirectory() { return _parentDir; }
    
    /** Sets the parent directory of the document only if it is "Untitled"
      * @param pd The parent directory
      */
    public synchronized void setParentDirectory(File pd) {
      if (! AbstractGlobalModel.isUntitled(_file))
        throw new IllegalArgumentException("The parent directory can only be set for untitled documents");
      _parentDir = pd;  
    }
    
    public int getInitialVerticalScroll()   { return _initVScroll; }
    public int getInitialHorizontalScroll() { return _initHScroll; }
    public int getInitialSelectionStart()   { return _initSelStart; }
    public int getInitialSelectionEnd()     { return _initSelEnd; }
    
    void setInitialVScroll(int i)  { _initVScroll = i; }
    void setInitialHScroll(int i)  { _initHScroll = i; }
    void setInitialSelStart(int i) { _initSelStart = i; }
    void setInitialSelEnd(int i)   { _initSelEnd = i; }
    
    /** Gets the definitions document being handled.
      * @return document being handled
      */
    public DefinitionsDocument getDocument() {
      
//      System.err.println("getDocument() called on " + this);
      try { return _cacheAdapter.getDocument(); }
      catch(IOException ioe) { // document has been moved or deleted
//        Utilities.showDebug("getDocument() failed for " + this);
        try {
          _notifier.documentNotFound(this, _file);
          final String path = fixPathForNavigator(getFile().getCanonicalFile().getCanonicalPath());
          _documentNavigator.refreshDocument(ConcreteOpenDefDoc.this, path); 
          
          return _cacheAdapter.getDocument();
        }
        catch(Throwable t) { throw new UnexpectedException(t); }
      }
    }
    
//    /** Reconstructs the embedded positions for this document. */
//    public void makePositions() { _cacheAdapter.makePositions(); }
    
    /** Returns the name of the top level class, if any.
      * @throws ClassNameNotFoundException if no top level class name found.
      */
    public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
      return getDocument().getFirstTopLevelClassName();
    }
    
    /** Returns the name of the main (public) class, if any.
      * @throws ClassNameNotFoundException if no top level class name found.
      */
    public String getMainClassName() throws ClassNameNotFoundException {
      return getDocument().getMainClassName();
    }
    
    /** Returns the name of this file, or "(Untitled)" if no file. */
    public String getFileName() {
      if (_file == null) return "(Untitled)";
//      if (isUntitled()) return "(Untitled)";
      return _file.getName(); 
    }
    
    /** Returns the name of the file for this document with an appended asterisk (if modified) or spaces */
    public String getName() {
      String fileName = getFileName();
      if (isModifiedSinceSave()) fileName = fileName + "*";
      else fileName = fileName + "  ";  // forces the cell renderer to allocate space for an appended "*"
      return fileName;
    }
    
    /** Returns the canonical path for this document, "(Untitled)" if unsaved), "" if the file path is ill-formed. */
    public String getCanonicalPath() {
      if (isUntitled()) { return "(Untitled)"; }
      else { return IOUtil.attemptCanonicalFile(getRawFile()).getPath(); }
    }
    
    /** Returns the canonical path augmented by " *" if the document has been modified. */
    public String getCompletePath() {
      String path = getCanonicalPath();
      // Mark if modified
      if (isModifiedSinceSave()) path = path + " *";
      return path;
    }
    
    /** Finds the root directory for the source file for this document; null if document is Untitled.
      * @return The root directory of the source files, based on the package statement.
      * @throws InvalidPackageException if the package statement is invalid,
      * or if it does not match up with the location of the source file.
      */
    public File getSourceRoot() throws InvalidPackageException { 
      if (isUntitled())
        throw new InvalidPackageException(-1, "Can not get source root for unsaved file. Please save.");
      
      try {
        String[] packages = _packageName.split("\\.");
        if (packages.length == 1 && packages[0].equals("")) {
          packages = new String[0]; // split should do this, but it doesn't
        }
        File dir = getFile().getParentFile();
        for (String p : IterUtil.reverse(IterUtil.asIterable(packages))) {
          if (dir == null || ! dir.getName().equals(p)) {
            String m = "File is in the wrong directory or is declared part of the wrong package.  " +
              "Directory name " + ((dir == null) ? "(root)" : "'" + dir.getName() + "'") +
              " does not match package name '" + p + "'.";
            throw new InvalidPackageException(-1, m);
          }
          dir = dir.getParentFile();
        }
        if (dir == null) {
          // should not happen in typical cases -- requires the first package name to match the root's name,
          // which is usually not a valid identifier (like "" or "C:")
          throw new InvalidPackageException(-1, "File is in a directory tree with a null root");
        }
        return dir;
      }
      catch (FileMovedException fme) {
        throw new
          InvalidPackageException(-1, "File has been moved or deleted from its previous location. Please save.");
      }
    }
    
    /**  @return the name of the package at the time of the most recent save or load operation. */
    public String getPackageName() { return _packageName; }
    
    /** Sets the cached _packageName for the preceding method. */  
    public void setPackage(String name)   {
      try {
      _log.log("In AbstractGlobalModel, setting package for " + getFile() + "to '" + name + "'");
      _packageName = name; 
      }
      catch(Exception e) { }
    }
    
    /**  @return the name of the package currently embedded in document. */
    public String getPackageNameFromDocument() {
      return getDocument().getPackageName(); 
    }
    
    /** Originally designed to allow undoManager to set the current document to be modified whenever an undo
      * or redo is performed.  Now it actually does this.
      */
    public void updateModifiedSinceSave() { getDocument().updateModifiedSinceSave(); }
    
    /** Getter for lexicographic name; used to sort documents into segemented lexicographic ordder */
    public String getLexiName() { return _lexiName; }
    
    /** Returns the Pageable object for printing.
      * @return A Pageable representing this document.
      */
    public Pageable getPageable() throws IllegalStateException { return _book; }
    
    /** Clears the pageable object used to hold the print job. */
    public void cleanUpPrintJob() { _book = null; }
    
    
    //--------------- Simple Predicates ---------------//
    
    /** A file is in the project if the source root is the same as the
      * project root. this means that project files must be saved at the
      * source root. (we query the model through the model's state)
      */
    public boolean inProjectPath() { return _state.inProjectPath(this); }
    
    /** An open file is in the new project if the source root is the same as the new project root. */
    public boolean inNewProjectPath(File projRoot) {
      try { return ! isUntitled() && IOUtil.isMember(getFile(), projRoot); }
      catch(FileMovedException e) { return false; }
    }
    
    /** A file is in the project if it is explicitly listed as part of the project. */
    public boolean inProject() { return ! isUntitled() && _state.inProject(_file); }
    
    /** Determines if the document is empty. */
    public boolean isEmpty() { return getLength() == 0; }
    
    /** @return true if this is an auxiliary file. */
    public boolean isAuxiliaryFile() { return ! isUntitled() && _state.isAuxiliaryFile(_file); }
    
    /** @return true if this has a legal source file name for the currently active compiler */
    public boolean isSourceFile() {
      if (isUntitled()) return false;  // assert _file != null
//      System.err.println("In " + this + " the active compiler is " + getCompilerModel().getActiveCompiler());
      return getCompilerModel().getActiveCompiler().isSourceFileForThisCompiler(_file);
    }
    
    /** Returns whether this document is currently untitled (indicating whether it has a file yet or not).
      * @return true if the document is untitled and has no file
      */
    public boolean isUntitled() { return AbstractGlobalModel.isUntitled(_file); }
    
    public boolean isUntitledAndEmpty() { return isUntitled() && getLength() == 0; }  // should be synchronized?
    
    /** Returns true if the file exists on disk. Returns false if the file has been moved or deleted */
    public boolean fileExists() { 
      File f = _file; // single read of _file;
      return  ! AbstractGlobalModel.isUntitled(f) && f.exists(); 
    }
    
    //--------------- Major Operations ----------------//
    
    /** Returns true if the file exists on disk. Prompts the user otherwise */
    public boolean verifyExists() {
//      Utilities.showDebug("verifyExists called on " + _file);
      if (fileExists()) return true;
      //prompt the user to find it
      try {
        _notifier.documentNotFound(this, _file);
//        File f = getFile();
        if (isUntitled()) return false;
        String path = fixPathForNavigator(getFile().getCanonicalPath());
        _documentNavigator.refreshDocument(this, path);
        return true;
      }
      catch(FileMovedException e) { return false; }
      catch(IOException e) { return false; }
//      catch(DocumentFileClosed e) { /* not clear what to do here */ }
    }
    
    /** Makes a default DDReconstructor that will make the corresponding DefinitionsDocument. */
    protected DDReconstructor makeReconstructor() {
      return new DDReconstructor() {
        
        // Brand New documents start at location 0
        private volatile int _loc = 0;
        
        // Start out with empty lists of listeners on the very first time the document is made
        private volatile DocumentListener[] _list = { };
        private volatile List<FinalizationListener<DefinitionsDocument>> _finalListeners =
          new LinkedList<FinalizationListener<DefinitionsDocument>>();
        
        // Weak hashmap that associates a WrappedPosition with its offset when saveDocInfo was called
        private volatile WeakHashMap< DefinitionsDocument.WrappedPosition, Integer> _positions =
          new WeakHashMap<DefinitionsDocument.WrappedPosition, Integer>();
        
        // Returns the text for this document as a String; assert never returns null;
        public String getText() {
          String image = _image;
          if (image != null) return image;
          
          // Document has not yet been read from disk; read it and set _image before returning text.
          // Synchronization on this was eliminated because it does not prevent the returned string from becoming 
          // inconsistent with _doc/_file in the presence of huge scheduling delays.  Of course, all getText operations 
          // can return stale data in the presence of such delays. 
          try { image = FileOps.readFileAsSwingText(_file); }
          catch(IOException e) {  image = ""; }  
//          System.err.println("Returning image '" + image + " for file " + _file);
          _image = image;
          return _image;
        }
        
        public DefinitionsDocument make() throws IOException, BadLocationException, FileMovedException {
          
//          System.err.println("DDReconstructor.make() called on " + ConcreteOpenDefDoc.this);
          DefinitionsDocument newDefDoc = new DefinitionsDocument(_notifier);
          newDefDoc.setOpenDefDoc(ConcreteOpenDefDoc.this);
          
          /* Initialize doc text contents */
          String image = getText();  // retrieves _image if it has already been set
          assert image != null;  // getText() never returns null
          _editorKit.read(new StringReader(image), newDefDoc, 0);
          
          //  Set document property to write out document using newLine conventions of the host platform.
          newDefDoc.putProperty(DefaultEditorKit.EndOfLineStringProperty, StringOps.EOL);
//          _log.log("Reading from image for " + _file + " containing " + _image.length() + " chars");    
          
          _loc = Math.min(_loc, image.length()); // make sure not past end
          _loc = Math.max(_loc, 0); // make sure not less than 0
          newDefDoc.setCurrentLocation(_loc);
          for (DocumentListener d : _list) {
            if (d instanceof DocumentUIListener) newDefDoc.addDocumentListener(d);
          }
          for (FinalizationListener<DefinitionsDocument> l: _finalListeners) {
            newDefDoc.addFinalizationListener(l);
          }
          
          // re-create and update all positions
          newDefDoc.setWrappedPositionOffsets(_positions);
          
          newDefDoc.resetModification();  // Why is this necessary? A reconstructed document is already unmodified.
          
          //            tempDoc.setUndoManager(_undo);
          assert ! newDefDoc.isModifiedSinceSave();
//          System.err.println ("_packageName in make() = " + _packageName);
//          System.err.println("tempDoc.getLength() = " + tempDoc.getLength());
          _packageName = newDefDoc.getPackageName();
          _log.log("Setting _packageName for " + newDefDoc + " in make() to " + _packageName);
          return newDefDoc;
        }
        
        
        /** Saves the information for this document before it is kicked out of the cache.  Only called from 
          * DocumentCache.  Assumes that cache lock is already held. 
          */
        public void saveDocInfo(DefinitionsDocument doc) {
// These lines were commented out to fix a memory leak; evidently, the undomanager holds on to the document          
//          _undo = doc.getUndoManager();
//          _undoListeners = doc.getUndoableEditListeners();
          // Save document image.  Note: this could be optimized to eliminate redundant updates to _image
          String text = doc.getText();
          if (text.length() > 0) {
            _image = text;  
//            _log.log("Saving image containing " + _image.length() + " chars for " + _file);
          }
          _loc = doc.getCurrentLocation();
          _list = doc.getDocumentListeners();
          _finalListeners = doc.getFinalizationListeners ();
          
          // save offsets of all positions
          _positions.clear();
          _positions = doc.getWrappedPositionOffsets();
        }
        
        public void addDocumentListener(DocumentListener dl) {
          ArrayList<DocumentListener> tmp = new ArrayList<DocumentListener>();
          for (DocumentListener l: _list) { if (dl != l) tmp.add(l); }
          tmp.add(dl);
          _list = tmp.toArray (new DocumentListener[tmp.size()]);
        }
        public String toString() { return ConcreteOpenDefDoc.this.toString(); }
      };
    }
    
    /** Saves the document with a FileWriter.  If the file name is already set, the method will use
      * that name instead of whatever selector is passed in.
      * @param com a selector that picks the file name if the doc is untitled
      * @exception IOException
      * @return true if the file was saved, false if the operation was canceled
      */
    public boolean saveFile(FileSaveSelector com) throws IOException {
//      System.err.println("AbstractGlobalModel.saveFile called on " + this);
      // Update value of _packageName since modification flag will be set to false
      if (! isModifiedSinceSave()) return true;
      if (isUntitled()) return saveFileAs(com);
      
      // Didn't need to save since file is named and unmodified; return true, since the save wasn't "canceled"
      
//      System.err.println("Saving file: " + getFile());
      
      // Update package name by parsing the documet text
      _packageName = getDocument().getPackageName();
      FileSaveSelector realCommand = com;
      try {
        final File file = getFile();
//        System.err.println("File name for doc to be saved is: " + file);
        if (! isUntitled()) {
//          System.err.println("Document has a title");
          realCommand = new TrivialFSS(file);
//          System.err.println("TrivialFSS set up");
        }
      }
      catch (FileMovedException fme) {
        // getFile() failed, prompt the user if a new one should be selected
        if (com.shouldSaveAfterFileMoved(this, fme.getFile())) realCommand = com;
        else return false;
        // User declines to save as a new file, so don't save
      }
//      System.err.println("Calling saveFileAs");
      return saveFileAs(realCommand);
    }
    
    /** Saves the document with a FileWriter.  The FileSaveSelector will either provide a file name or prompt the
      * user for one.  It is up to the caller to decide what needs to be done to choose a file to save to.  Once
      * the file has been saved succssfully, this method fires fileSave(File).  If the save fails for any
      * reason, the event is not fired. This is synchronized against the compiler model to prevent saving and
      * compiling at the same time- this used to freeze drjava.
      * @param com a selector that picks the file name.
      * @throws IOException if the save fails due to an IO error
      * @return true if the file was saved, false if the operation was canceled
      */
    public boolean saveFileAs(FileSaveSelector com) throws IOException {
      assert EventQueue.isDispatchThread();
      _log.log("AbstractGlobalModel.saveFileAs called on " + this);
      File oldFile = getRawFile();
      // Update _packageName since modifiedSinceSaved flag will be set to false
      _packageName = getDocument().getPackageName();
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile().getCanonicalFile();
        
//        _log.log("saveFileAs called on " + file);
        OpenDefinitionsDocument otherDoc = _getOpenDocument(file);
        
        // Check if file is already open in another document
        boolean openInOtherDoc = ((otherDoc != null) && (openDoc != otherDoc));
        
//        System.err.println("AbstractGlobalModel.saveFileAs.openInOtherDoc = " + openInOtherDoc);
        
        // If the file is open in another document, abort if user does not confirm overwriting it
        if (openInOtherDoc) {
          boolean shouldOverwrite = com.warnFileOpen(file);
          if (! shouldOverwrite) return true; // operation not cancelled?  Strange
        }
        
        if (! file.exists() || com.verifyOverwrite(file)) {  // confirm that existing file can be overwritten
          
//          System.err.println("Writing file " + file);
          
          // Correct the case of the filename (in Windows)  TODO: what if rename fails?
          if (! file.getCanonicalFile().getName().equals(file.getName())) file.renameTo(file);
          
          // Check for # in the path of the file because if there
          // is one, then the file cannot be used in the Interactions Pane
          if (file.getAbsolutePath().indexOf("#") != -1) _notifier.filePathContainsPound();
          
          // if file is read-only, ask if it should be made writable
          if (file.exists() && ! file.canWrite()) {
            File[] res = _notifier.filesReadOnly(new File[] {file});
            for(File roFile: res) {
              FileOps.makeWritable(roFile);
            }
            if (res.length == 0) { return false; /* read-only, do not overwrite */ }
          }
          
          _log.log("Calling FileOps.saveFile to save " + file);
          _log.log("shouldUpdateDocumentState() = " + com.shouldUpdateDocumentState());
          FileOps.saveFile(new FileOps.DefaultFileSaver(file) {
            /** Only runs in event thread. */
            public void saveTo(OutputStream os) throws IOException {
              DefinitionsDocument dd = getDocument();
              try {
                _editorKit.write(os, dd, 0, dd.getLength());
//                Utilities.show ("Wrote file containing:\n" + doc.getText());
              }
              catch (BadLocationException docFailed) { throw new UnexpectedException(docFailed); }
            }
          });
          
          if (com.shouldUpdateDocumentState()) {
            resetModification();
            if (! oldFile.equals(file)) {
              /* remove regions for this document */
              removeFromDebugger();
              _breakpointManager.removeRegions(this);
              _bookmarkManager.removeRegions(this);
              for (RegionManager<MovingDocumentRegion> rm: getFindResultsManagers()) rm.removeRegions(this);
              clearBrowserRegions();
            }
            synchronized(_documentsRepos) {
              File f = getRawFile();
//            OpenDefinitionsDocument d = _documentsRepos.get(f);
              // d == this except in some unit tests where documents are not entered in _documentsRepos
//            assert d == this;
              _documentsRepos.remove(f);
              _documentsRepos.put(file, this);
            }
            setFile(file);
            
            // this.getPackageName does not return "" if this is untitled and contains a legal package declaration     
//          try {
//            // This calls getDocument().getPackageName() because this may be untitled and this.getPackageName()
//            // returns "" if it's untitled.  Right here we are interested in parsing the DefinitionsDocument's text
//            _packageName = getDocument().getPackageName();
//          }
//          catch(InvalidPackageException e) { _packageName = null; }
            setCachedClassFile(FileOps.NULL_FILE);
            checkIfClassFileInSync();
            
//          Utilities.showDebug("ready to fire fileSaved for " + this);
            _notifier.fileSaved(openDoc);
            
            // Make sure this file is on the appropriate classpaths (does nothing in AbstractGlobalModel)
            addDocToClassPath(this);
            
            /* update the navigator */
            _documentNavigator.refreshDocument(this, fixPathForNavigator(file.getCanonicalPath()));
            
            /* set project changed flag */
            setProjectChanged(true);          
          }
        }
        return true;
      }
      catch (OperationCanceledException oce) {
        // Thrown by com.getFile() if the user cancels.
        //   We don't save if this happens.
        return false;
      }
    }
    
    /** This method tells the document to prepare all the DrJavaBook and PagePrinter objects. */
    public void preparePrintJob() throws BadLocationException, FileMovedException {
      String fileName = "(Untitled)";
      File sourceFile = getFile();  // single read of _file
      if (! AbstractGlobalModel.isUntitled(sourceFile)) fileName = sourceFile.getAbsolutePath();
      
      _book = new DrJavaBook(getDocument().getText(), fileName, _pageFormat);
    }
    
    /** Prints the given document by bringing up a "Print" window. */
    public void print() throws PrinterException, BadLocationException, FileMovedException {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      if (printJob.printDialog()) printJob.print();
      cleanUpPrintJob();
    }
    
    /** throws UnsupportedOperationException */
    public void startCompile() throws IOException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support compilation");
    }
    
    /** throws UnsupportedOperationException */
    public void runMain(String className) throws IOException, ClassNameNotFoundException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support running");
    }
    
    /** throws UnsupportedOperationException */
    public void runApplet(String className) throws IOException, ClassNameNotFoundException {
      throw new UnsupportedOperationException("AbstractGlobalModel does not support running");
    }

    /** throws UnsupportedOperationException */
    public void runSmart(String className) throws IOException, ClassNameNotFoundException {
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
    
    /** Returns true if this document is resident in memory. _cacheAdapter should be non-null. */
    public boolean isReady() { return _cacheAdapter != null && _cacheAdapter.isReady(); }
    
    /** Determines if the document has been modified since the last save.
      * @return true if the document has been modified
      */
    public boolean isModifiedSinceSave() {
      /* If the document has not been registered or it is virtualized (only stored on disk), then we know that
       * it is not modified. This method can be called by debugging code (via getName() on a
       * ConcreteOpenDefDoc) before the document has been registered (_cacheAdapter == null). */
      if (isReady()) return getDocument().isModifiedSinceSave();
      else return false;
    }
    
    public void documentSaved() { _cacheAdapter.documentSaved(); }
    
    public void documentModified() { 
      _cacheAdapter.documentModified();
      _classFileInSync = false;
    }
    
    public void documentReset() { _cacheAdapter.documentReset(); }
    
    /** Determines if the file for this document has been modified since it was loaded.
      * @return true if the file has been modified
      */
    public boolean modifiedOnDisk() {
      boolean ret = false;
      final File f = _file;  // single read of f
      if (! AbstractGlobalModel.isUntitled(f)) ret = (f.lastModified() > _timestamp);
      return ret;
    }
    
    /** Determines if document has a class file consistent with its current state.  If this document is unmodified,
      * this method examines the primary class file corresponding to this document and compares the timestamps of
      * the class file to that of the source file.  An empty untitled document is consider to be "in sync".
      */
    public boolean checkIfClassFileInSync() {
//      _log.log("checkIfClassFileInSync() called for " + this);
      if (isEmpty()) return true;
      
      // If modified, then definitely out of sync
      
      if (isModifiedSinceSave()) {
        setClassFileInSync(false);
//        _log.log("checkIfClassFileInSync = false because isModifiedSinceSave()");
        return false;
      }
      
      // Look for cached class file
      File classFile = getCachedClassFile();
//      _log.log("In checkIfClassFileInSync cached value of classFile = " + classFile);
      if (classFile == FileOps.NULL_FILE) {
        // Not cached, so locate the file
        classFile = _locateClassFile();
//        _log.log(this + ": in checkIfClassFileInSync _locateClassFile() = " + classFile);
        setCachedClassFile(classFile);
        if ((classFile == FileOps.NULL_FILE) || (! classFile.exists())) {
          // couldn't find the class file
          _log.log(this + ": Could not find class file");
          setClassFileInSync(false);
          return false;
        }
      }
      
      // compare timestamps
      
      File sourceFile;
      try { sourceFile = getFile(); }
      catch (FileMovedException fme) {
        setClassFileInSync(false);
        _log.log(this + ": File moved");
        return false;
      }
      if (sourceFile != null) { 
        _log.log(sourceFile + " has timestamp " + sourceFile.lastModified());
        _log.log(classFile + " has timestamp " + classFile.lastModified());
      }
      if (sourceFile == null || sourceFile.lastModified() > classFile.lastModified()) {  // assert sourceFile != null 
        setClassFileInSync(false);
        _log.log(this + ": date stamps indicate modification");
        return false;
      }
      else {
        setClassFileInSync(true);
        return true;
      }
    }
    
    /** Returns the class file for this source document by searching the source roots of open documents, the
      * system classpath, and the "extra.classpath ".  Returns NULL_FILE if the class file could not be found.
      */
    private File _locateClassFile() {
      // TODO: define in terms of GlobalModel.getClassPath()
      
      if (isUntitled()) return FileOps.NULL_FILE;
      
      String className;
      try { className = getDocument().getQualifiedClassName(); }
      catch (ClassNameNotFoundException cnnfe) {
        _log.log("_locateClassFile() failed for " + this + " because getQualifedClassName returned ClassNotFound");
        return FileOps.NULL_FILE;  /* No source class name */ 
      }
      _log.log("In _locateClassFile, className = " + className);
      String ps = System.getProperty("file.separator");
      // replace periods with the System's file separator
      className = StringOps.replace(className, ".", ps);
      String fileName = className + ".class";
      
      _log.log("In _locateClassFile, classfileName = " + fileName);
      
      // Check source root set (open files)
      ArrayList<File> roots = new ArrayList<File>();
      
      _log.log("In _locateClassFile, build directory = " + getBuildDirectory());
      
      File buildDir = getBuildDirectory();
      
      if (buildDir != FileOps.NULL_FILE && ! roots.contains(buildDir)) roots.add(buildDir);
      
      // Add the current document to the beginning of the roots list
      try {
        File root = getSourceRoot();
//        _log.log("Directory " + root + " added to list of source roots");
        if (! roots.contains(root)) roots.add(root); 
      }
      catch (InvalidPackageException ipe) {
        try {
//          _log.log(this + " has no source root, using parent directory instead");
          File root = getFile().getParentFile();
          if (root != FileOps.NULL_FILE && ! roots.contains(root)) {
            if (! roots.contains(root)) roots.add(root);
//            _log.log("Added parent directory " + root + " to list of source roots");
          }
        }
        catch(NullPointerException e) { throw new UnexpectedException(e); }
        catch(FileMovedException fme) {
          // Moved, but we'll add the old file to the set anyway
          _log.log("File for " + this + "has moved; adding parent directory to list of roots");
          File root = fme.getFile().getParentFile();
          if (root != FileOps.NULL_FILE && ! roots.contains(root)) roots.add(root);
        }
      }
      
      File classFile = findFileInPaths(fileName, roots);
      if (classFile != FileOps.NULL_FILE) {
//        _log.log("Found source file " + classFile + " for " + this);
        return classFile;
      }
      
//      _log.log(this + " not found on path of source roots");
      // Class not on source root set, check system classpath
      classFile = findFileInPaths(fileName, ReflectUtil.SYSTEM_CLASS_PATH);
      
      if (classFile != FileOps.NULL_FILE) return classFile;
      
      // not on system classpath, check interactions classpath
      Vector<File> cpSetting = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
      return findFileInPaths(fileName, cpSetting);
    }
    
    /** Determines if the definitions document has been changed by an outside agent. If the document has changed,
      * asks the listeners if the GlobalModel should revert the document to the most recent version saved.
      * @return true if document has been reverted
      */
    public boolean revertIfModifiedOnDisk() throws IOException {
      final OpenDefinitionsDocument doc = this;
      if (modifiedOnDisk()) {
        boolean shouldRevert = _notifier.shouldRevertFile(doc);
        if (shouldRevert) doc.revertFile();
        return shouldRevert;
      }
      return false;
    }
    
    /** Degenerate version of close; does not remove breakpoints in this document */
    public void close() {
      removeFromDebugger();
      _cacheAdapter.close();
    }
    
    /** Reverts current ODD to file content on disk. */
    public void revertFile() throws IOException {
      
      final OpenDefinitionsDocument doc = this;
      
      if (doc.isUntitled()) throw new UnexpectedException("Cannot revert an Untitled file!");
      
      //need to remove old, possibly invalid breakpoints
      removeFromDebugger();
      _breakpointManager.removeRegions(this);
      _bookmarkManager.removeRegions(this);
      for (RegionManager<MovingDocumentRegion> rm: getFindResultsManagers()) rm.removeRegions(this);
      doc.clearBrowserRegions();
      
      FileReader reader = null;
      try {
        //this line precedes .remove() so that an invalid file is not cleared before this fact is discovered.
        File file = doc.getFile();
        reader = new FileReader(file);
        doc.clear();
        
        _editorKit.read(reader, doc, 0);
        
        resetModification();
        doc.checkIfClassFileInSync();
        setCurrentLocation(0);
        _notifier.fileReverted(doc);
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      finally { if (reader != null) reader.close(); /* win32 needs readers closed explicitly! */ }
    }
    
    /** Asks the listeners if the GlobalModel can abandon the current document.  Fires the canAbandonFile(File)
      * event if isModifiedSinceSave() is true.  Only executes in event thread except for tests.
      * @return true if the current document can be abandoned, false if the current action should be halted in
      *              its tracks (e.g., file open when the document has been modified since the last save).
      */
    public boolean canAbandonFile() {
//      assert EventQueue.isDispatchThread();
      if (isUntitledAndEmpty()) return true;
      File f = _file;
      if (isModifiedSinceSave() || (! AbstractGlobalModel.isUntitled(f) && ! f.exists() && _cacheAdapter.isReady()))
        return _notifier.canAbandonFile(this);
      else return true;
    }
    
    /** Fires the quit(File) event if isModifiedSinceSave() is true.  The quitFile() event asks the user if the
      * the file should be saved before quitting.  Only executes in event thread.
      * @return true if quitting should continue, false if the user cancelled
      */
    public boolean quitFile() {
      assert EventQueue.isDispatchThread();
      File f = _file;
      if (isModifiedSinceSave() || (f != null && ! f.exists() && _cacheAdapter.isReady())) 
        return _notifier.quitFile(this);
      return true;
    }
    
    /** Moves the definitions document to the given line, and returns the resulting character position.  
      * @param line Destination line number. If it exceeds the number of lines in the document, it is
      *             interpreted as the last line.
      * @return Index into document of where it moved
      */
    public int gotoLine(int line) {
//      DefinitionsDocument dd = getDocument();
      final int offset = getOffsetOfLine(line - 1);
      setCurrentLocation(offset);
      return offset;
    }
    
    protected int _caretPosition = 0;
    
    /** Forwarding method to sync the definitions with whatever view component is representing them. */
    public void setCurrentLocation(int location) { 
//      edu.rice.cs.drjava.ui.MainFrame.MFLOG.log("setCurrentLocation "+this+": "+location);
      _caretPosition = location; 
      getDocument().setCurrentLocation(location); 
    }
    
    /** Get the location of the cursor in the definitions according to the definitions document. */
    public int getCurrentLocation() { return getDocument().getCurrentLocation(); }
    
//    public boolean indentInProgress() { return getDocument().indentInProgress(); }
    
    /** @return the caret position as set by the view. */
    public int getCaretPosition() { return _caretPosition; }
    
    /** Finds the match for the closing brace immediately to the left, assuming there is such a brace.  Only runs in the
      * event thread.
      * @return the relative distance backwards to the offset before the matching brace.
      */
    public int balanceBackward() { return getDocument().balanceBackward(); }
    
    /** Forwarding method to find the match for the open brace immediately to the right, assuming there is such a brace.
      * Only runs in the event thread.
      * @return the relative distance forwards to the offset after the matching brace.
      */
    public int balanceForward() { return getDocument().balanceForward(); }
    
    /** @return the breakpoint region manager. */
    public RegionManager<Breakpoint> getBreakpointManager() { return _breakpointManager; }
    
    /** @return the bookmark region manager. */
    public RegionManager<MovingDocumentRegion> getBookmarkManager() { return _bookmarkManager; }
    
    /** Clear the browser history regions for this document. */
    public void clearBrowserRegions() { 
      BrowserDocumentRegion[] regions = _browserRegions.toArray(new BrowserDocumentRegion[0]);
      for (BrowserDocumentRegion r: regions) _browserHistoryManager.remove(r);
      _browserRegions.clear();
    }
    
    /** throws UnsupportedOperationException */
    public void removeFromDebugger() { /* do nothing because it is called in methods in this class */ }
    
    public String toString() { return getFileName(); }
    
    /** Orders ODDs by their lexical names.  Note that equals defines a finer equivalence relation than compareTo. */
    public int compareTo(OpenDefinitionsDocument o) { 
      int diff = hashCode() - o.hashCode();
      if (diff != 0) return diff;
      return _lexiName.compareTo(o.getLexiName()); 
    }
    
    /** Implementation of the javax.swing.text.Document interface. */
    public void addDocumentListener(DocumentListener listener) {
      if (_cacheAdapter.isReady()) getDocument().addDocumentListener(listener);
      else _cacheAdapter.addDocumentListener(listener);
    }
    // Not in current use
//    List<UndoableEditListener> _undoableEditListeners = new LinkedList<UndoableEditListener>();
    
    public void addUndoableEditListener(UndoableEditListener listener) {
//      _undoableEditListeners.add(listener);
      getDocument().addUndoableEditListener(listener);
    }
    
    public void removeUndoableEditListener(UndoableEditListener listener) {
//      _undoableEditListeners.remove(listener);
      getDocument().removeUndoableEditListener(listener);
    }
    
    public UndoableEditListener[] getUndoableEditListeners() {
      return getDocument().getUndoableEditListeners();
    }
    
    public Position createUnwrappedPosition(int offs) throws BadLocationException {
      return getDocument().createUnwrappedPosition(offs); 
    }
    
    public Position createPosition(int offs) throws BadLocationException {
      return getDocument().createPosition(offs);
    }
    
    public Element getDefaultRootElement() { return getDocument().getDefaultRootElement(); }
    
    /** The following two methods are in javax.swing.Document. */
    public Position getStartPosition() { 
      throw new UnsupportedOperationException("ConcreteOpenDefDoc does not support getStartPosition()"); 
    }
    public Position getEndPosition() { 
      throw new UnsupportedOperationException("ConcreteOpenDefDoc does not support getEndPosition()"); 
    }
    
    public int getLength() { return _cacheAdapter.getLength(); }
    
    public Object getProperty(Object key) { return getDocument().getProperty(key); }
    
    public Element[] getRootElements() { return getDocument().getRootElements(); }
    
//    public Position getStartPosition() { return getDocument().getStartPosition(); } 
    
//    public String getText() {
//      synchronized(_cache._cacheLock) {  // lock down the cache 
//        if (! _cacheAdapter.isReady() && _image != null) return _image;
//      }
//      return getDocumentText();  
//    }
    
//  The following method must be renamed as private getDocumentText if the preceding code is commented in.
    
    /** Gets the text of this.  Avoids reloading the document if it is kicked out of the cache. */
    public String getText() { return _cacheAdapter.getText(); }
    
    /** Gets the specified substring of this.  Avoids reloading the document if it is kicked out of the cache. */
    public String getText(int offset, int length) throws BadLocationException {
      return _cacheAdapter.getText(offset, length);
    }
    
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
      getDocument().getText(offset, length, txt);
    }
    
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
      getDocument().insertString(offset, str, a);
    }
    
    public void append(String str, AttributeSet set) { getDocument().append(str, set); }
    
    public void append(String str, Style style) { getDocument().append(str, style); }
    
    public void append(String str) { getDocument().append(str); }
    
    public void putProperty(Object key, Object value) { getDocument().putProperty(key, value); }
    
    public void remove(int offs, int len) throws BadLocationException { getDocument().remove(offs, len); }
    
    public void removeDocumentListener(DocumentListener listener) { getDocument().removeDocumentListener(listener); }
    
    public void render(Runnable r) { getDocument().render(r); }
    
    /** End implementation of javax.swing.text.Document interface. */
    
    /** If the undo manager is unavailable, no undos are available
      * @return whether the undo manager can perform any undo's
      */
    public boolean undoManagerCanUndo() { return _cacheAdapter.isReady() && getUndoManager().canUndo(); }
    /** If the undo manager is unavailable, no redos are available
      * @return whether the undo manager can perform any redo's
      */
    public boolean undoManagerCanRedo() { return _cacheAdapter.isReady() && getUndoManager().canRedo(); }
    
    /** Decorator pattern for the definitions document. */
    public CompoundUndoManager getUndoManager() { return getDocument().getUndoManager(); }
    
    /** Gets start of line containing pos. */    
    public int _getLineStartPos(int pos) { 
      DefinitionsDocument doc = getDocument();
      return doc._getLineStartPos(pos); 
    }
    
    /** Gets end of line containing pos (line includes closing '\n'). */
    public int _getLineEndPos(int pos) { 
      DefinitionsDocument doc = getDocument();
      return doc._getLineEndPos(pos); 
    }
    
    public int commentLines(int selStart, int selEnd) { return getDocument().commentLines(selStart, selEnd); }
    
    public int uncommentLines(int selStart, int selEnd) {
      return getDocument().uncommentLines(selStart, selEnd);
    }
    
    public void indentLines(int selStart, int selEnd) { 
      DefinitionsDocument doc = getDocument();
      doc.indentLines(selStart, selEnd); 
    }
    
    public void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm)
      throws OperationCanceledException {
      DefinitionsDocument doc = getDocument();
      doc.indentLines(selStart, selEnd, reason, pm); 
    }
    
    public int getCurrentLine() { return getDocument().getCurrentLine(); }
    
    public int getCurrentCol() { return getDocument().getCurrentCol(); }
    
    public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
      return getDocument().getIntelligentBeginLinePos(currPos);
    }
    
    /** Gets offset of beginning of given 1-based line. */    
    public int _getOffset(int lineNum) { return getDocument()._getOffset(lineNum); }
    
    public String getQualifiedClassName() throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName();
    }
    
    public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
      return getDocument().getQualifiedClassName(pos);
    }
    
    public ReducedModelState getStateAtCurrent() { return getDocument().getStateAtCurrent(); }
    
    public void resetUndoManager() {
      // if it's not in the cache, the undo manager will be reset when it's reconstructed
      if (_cacheAdapter.isReady()) getDocument().resetUndoManager();
    }
    
    public DocumentListener[] getDocumentListeners() { return getDocument().getDocumentListeners(); }
    
    //--------- DJDocument methods ----------
    
//    public void setTab(int tab, int pos) { getDocument().setTab(tab, pos); }
    
//    public int getWhiteSpace() { return getDocument().getWhiteSpace(); }
    
//    public boolean inParenPhrase(int pos) { return getDocument().inParenPhrase(pos); }
    
//    public boolean posInParenPhrase() { return getDocument().posInParenPhrase(); }
    
    public String getEnclosingClassName(int pos, boolean fullyQualified) throws BadLocationException, 
      ClassNameNotFoundException {
      return getDocument().getEnclosingClassName(pos, fullyQualified);
    }
    
    /** Finds the previous brace of specified form enclosing pos. */
    public int findPrevEnclosingBrace(int pos, char opening, char closing) throws BadLocationException {
      return getDocument().findPrevEnclosingBrace(pos, opening, closing);
    }
    
    /**  Finds the next brace of specified form enclosing pos. */   
    public int findNextEnclosingBrace(int pos, char opening, char closing) throws BadLocationException {
      return getDocument().findNextEnclosingBrace(pos, opening, closing);
    }
    
//    public int getPrevNonWSCharPos(int pos) throws BadLocationException {
//      return getDocument().getPrevNonWSCharPos(pos);
//    }
    
    /**Only runs in the event thread. */
    public int getFirstNonWSCharPos(int pos) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos);
    }
    
    /** Only runs in the event thread. */
    public int getFirstNonWSCharPos(int pos, boolean acceptComments) throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos, acceptComments);
    }
    
    /** Only runs in event thead. */
    public int getFirstNonWSCharPos (int pos, char[] whitespace, boolean acceptComments)
      throws BadLocationException {
      return getDocument().getFirstNonWSCharPos(pos, whitespace, acceptComments);
    }
    
    /** Only runs in event thread. */
    public int _getLineFirstCharPos(int pos) throws BadLocationException {
      return getDocument()._getLineFirstCharPos(pos);
    }
    
    /** Only runs in event thread. */
    public int findCharOnLine(int pos, char findChar) {
      return getDocument().findCharOnLine(pos, findChar);
    }
    
    /** Only runs in event thread. */
    public int _getIndentOfStmt(int pos) throws BadLocationException {
      return getDocument()._getIndentOfStmt(pos);
    }
    
    /** Only runs in event thread. */
    public int _getIndentOfStmt(int pos, char[] delims) throws BadLocationException {
      return getDocument()._getIndentOfStmt(pos, delims);
    }
    
    /** Only runs in event thread. */
    public int _getIndentOfStmt(int pos, char[] delims, char[] whitespace) throws BadLocationException {
      return getDocument()._getIndentOfStmt(pos, delims, whitespace, true);
    }
    
//    public int getPrevNonWSCharPos(int pos, char[] whitespace) throws BadLocationException {
//      return getDocument().getPrevNonWSCharPos(pos, whitespace);
//    }
    
//    public boolean findCharInStmtBeforePos(char findChar, int position) {
//      return getDocument().findCharInStmtBeforePos(findChar, position);
//    }
    
    public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException {
      return getDocument().findPrevDelimiter(pos, delims);
    }
    
    public int findPrevDelimiter(int pos, char[] delims, boolean skipParenPhrases) throws BadLocationException {
      return getDocument().findPrevDelimiter(pos, delims, skipParenPhrases);
    }
    
//    public void resetReducedModelLocation() { getDocument().resetReducedModelLocation(); }
    
//    public ReducedModelState stateAtRelLocation(int dist) { return getDocument().stateAtRelLocation(dist); }
    
//    public IndentInfo getIndentInformation() { return getDocument().getIndentInformation(); }
    
    public void move(int dist) { getDocument().move(dist); }
    
    public ArrayList<HighlightStatus> getHighlightStatus(int start, int end) {
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
    
    public void clear() { getDocument().clear(); }
    
    /* Locking operations in DJDocument interface */
    
    /* Gets the reduced model so it can be locked. */
    public ReducedModelControl getReduced() { return getDocument().getReduced(); }
    
    /** @return the number of lines in this document. */
    public int getNumberOfLines() { return getLineOfOffset(getLength()); }
    
    /** Determines if pos in document is inside a comment or a string. */
    public boolean isShadowed(int pos) { return getDocument().isShadowed(pos); }
    
    /** Translates an offset into the components text to a line 0-based number.
      * @param offset the offset >= 0
      * @return the line number >= 0 
      */
    public int getLineOfOffset(int offset) { return getDefaultRootElement().getElementIndex(offset); }
    
    /** Translates a 0-based line number into an offset.
      * @param line number >= 0
      * @return offset >= 0 
      */
    public int getOffsetOfLine(int line) {
      final int count = getDefaultRootElement().getElementCount();
      if (line >= count) { line = count - 1; }
      return getDefaultRootElement().getElement(line).getStartOffset();
    }
    
//    /** Add a region manager for find results to this document.
//      * @param rm the global model's region manager */
//    public void addFindResultsManager(RegionManager<MovingDocumentRegion> rm) { _findResultsManagers.add(rm); }
    
//    /** Remove a manager for find results from this document.
//      * @param rm the global model's region manager. */
//    public void removeFindResultsManager(RegionManager<MovingDocumentRegion> rm) { _findResultsManagers.remove(rm); }
    
    /** Returns true if one of the words 'class', 'interface' or 'enum' is found
      * in non-comment text. */
    public boolean containsSource() throws BadLocationException {
      return getDocument().containsSource();
    }
  } /* End of ConcreteOpenDefDoc */
  
  private static class TrivialFSS implements FileSaveSelector {
    private File _file;
    private TrivialFSS(File file) { _file = file; }
    public File getFile() throws OperationCanceledException { return proposeBetterFileName(_file); }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite(File f) { return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
    public boolean shouldUpdateDocumentState() { return true; }
    private File proposeBetterFileName(File f) { return f; }
  }
  
  /** Creates a ConcreteOpenDefDoc for a NullFile object f (corresponding to a new empty document)
    * @return OpenDefinitionsDocument object for a new document
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(NullFile f) { return new ConcreteOpenDefDoc(f); }
  
  /** Creates a ConcreteOpenDefDoc for an existing file f.
    * @return OpenDefinitionsDocument object for f
    * @throws FileNotFoundException if file f does not exist
    */
  protected ConcreteOpenDefDoc _createOpenDefinitionsDocument(File f) throws IOException {
    if (! f.exists()) throw new FileNotFoundException("file " + f + " cannot be found");
    return new ConcreteOpenDefDoc(f);
  }
  
  /** Returns the OpenDefinitionsDocument corresponding to the given File, or null if that file is not open.
    * @param file File object to search for
    * @return Corresponding OpenDefinitionsDocument, or null
    */
  protected OpenDefinitionsDocument _getOpenDocument(File file) {
    synchronized(_documentsRepos) { return _documentsRepos.get(file); }
  }
  
  /** Returns the OpenDefinitionsDocuments that are NOT identified as project source files. */
  public List<OpenDefinitionsDocument> getNonProjectDocuments() {
    List<OpenDefinitionsDocument> allDocs = getOpenDefinitionsDocuments();
    List<OpenDefinitionsDocument> selectedDocs = new LinkedList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument d : allDocs) {
      if (! d.inProjectPath() && ! d.isAuxiliaryFile ()) selectedDocs.add(d);
    }
    return selectedDocs;
  }
  
  /** Returns the OpenDefinitionsDocuments that are identified as auxiliary project source files. */
  public List<OpenDefinitionsDocument> getAuxiliaryDocuments() {
    List<OpenDefinitionsDocument> allDocs = getOpenDefinitionsDocuments();
    List<OpenDefinitionsDocument> selectedDocs = new LinkedList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument d : allDocs)
      if (d.isAuxiliaryFile()) selectedDocs.add(d);
    return selectedDocs;
  }
  
  /** Returns the OpenDefinitionsDocuments that are identified as project source files. */
  public List<OpenDefinitionsDocument> getProjectDocuments() {
    List<OpenDefinitionsDocument> allDocs = getOpenDefinitionsDocuments();
    List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
    for (OpenDefinitionsDocument d: allDocs)
      if (d.inProjectPath() || d.isAuxiliaryFile()) projectDocs.add(d);
    return projectDocs;
  }
  /* Extracts relative path (from project origin) to parent of file identified by path.  Assumes path does not end in
   * File.separator. TODO: convert this method to take a File argument. */
  public String fixPathForNavigator(String path) throws IOException {
    String parent = path.substring(0, path.lastIndexOf(File.separator ));
    String rootPath = getProjectRoot().getCanonicalPath();
    
    if (! parent.equals(rootPath) && ! parent.startsWith(rootPath + File.separator))
      /** it's an external file, so don't give it a path */
      return "";
    else
      return parent.substring(rootPath.length());
  }
  
  /** Creates an OpenDefinitionsDocument for a file. Does not add to the navigator or notify that the file's open.
    * This method should be called only from within another open method that will do all of this clean up.
    * @param file the file to open
    */
  private OpenDefinitionsDocument _rawOpenFile(File file) throws IOException, AlreadyOpenException{
    OpenDefinitionsDocument openDoc = _getOpenDocument(file);
    if (openDoc != null) throw new AlreadyOpenException(openDoc); // handled in MainFrame.openFile(...)
    final ConcreteOpenDefDoc doc = _createOpenDefinitionsDocument(file);
    if (file instanceof DocFile) {
      DocFile df = (DocFile)file;
      Pair<Integer,Integer> scroll = df.getScroll();
      Pair<Integer,Integer> sel = df.getSelection();
      String pkg = df.getPackage();
      _log.log("Setting package name for DocFile " + file);
      doc.setPackage(pkg);  // Trust information in the project file; if it is wrong, _packageName invariant is broken
      doc.setInitialVScroll(scroll.first());
      doc.setInitialHScroll( scroll.second());
      doc.setInitialSelStart(sel.first());
      doc.setInitialSelEnd(sel.second());
    }
    else { // doc is freshly opened file, ostensibly a source file
//      Utilities.show("Opened a file " + file.getName() + " that is not a DocFile");
      _log.log("Setting package for file " + file.getName() + " to " + doc.getPackageNameFromDocument());
      doc.setPackage(doc.getPackageNameFromDocument()); // get the package name from the file; forces file to be read
    }
    return doc;
  }
  
  /** This pop method enables an ArrayList to serve as stack. */
  protected static <T> T pop(ArrayList<T> stack) { return stack.remove(stack.size() - 1); }
  
  /** Creates an iNavigatorItem for a document, and adds it to the navigator. A helper for opening a file or creating
    * a new file.
    * @param doc the document to add to the navigator
    */
  protected void addDocToNavigator(final OpenDefinitionsDocument doc) {
    try {
      if (doc.isUntitled()) _documentNavigator.addDocument(doc);
      else {
        String path = doc.getFile().getCanonicalPath();
        _documentNavigator.addDocument(doc, fixPathForNavigator(path));
      }
    }
    catch(IOException e) { _documentNavigator.addDocument(doc); }
    synchronized(_documentsRepos) { _documentsRepos.put(doc.getRawFile(), doc); }
  }
  
  /** Add a document to the classpath for the slave JVM. Does nothing here because there is no slave JVM.  Overridden
    * in DefaultGlobalModel. */
  protected void addDocToClassPath(OpenDefinitionsDocument doc) { }
  
  /** Creates a document from a file.
    * @param file File to read document from
    * @return openened document
    */
  public OpenDefinitionsDocument _openFile(File file) throws IOException, AlreadyOpenException {
    
    OpenDefinitionsDocument doc = _rawOpenFile(file);
    _completeOpenFile(doc);
    return doc;
  }
  
  private void _completeOpenFile(OpenDefinitionsDocument d) {
    addDocToNavigator(d);
    addDocToClassPath(d);
    
    try {
      File f = d.getFile();
      if (! inProject(f) && inProjectPath(d)) setProjectChanged(true);
    } 
    catch(FileMovedException fme) {
      /** project is not modified in this case */
    }
    
    _notifier.fileOpened(d);
  }
  
//  private static class BackUpFileOptionListener implements OptionListener<Boolean> {
//    public void optionChanged (OptionEvent<Boolean> oe) {
//      Boolean value = oe.value;
//      FileOps.DefaultFileSaver.setBackupsEnabled (value.booleanValue());
//    }
//  }
  
//----------------------- SingleDisplay Methods -----------------------//
  
  /** Returns the currently active document. */
  public OpenDefinitionsDocument getActiveDocument() { return  _activeDocument; }
  
  /** Sets the currently active document by updating the selection model.
    * @param doc Document to set as active
    */
  public void setActiveDocument(final OpenDefinitionsDocument doc) {
    /* The _activeDoc field is set by _gainVisitor when the DocumentNavigator changes the active document.
     * TODO: This operation should not require invokeAndWait.  Document switching is initiated only by events EXCEPT in 
     * unit esting.  We need to clean up unit testing and eliminate the invokeAndWait overhead.
     */
    
///* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
//    if (_activeDocument == doc) return; // this optimization appears to cause some subtle bugs
//    Utilities.showDebug("DEBUG: Called setActiveDocument()");
    
    try {
      Utilities.invokeAndWait(new Runnable() {  
        public void run() {
//          doc.makePositions();  // reconstruct embedded positions in document (reconstructing document if necesarry)
          _documentNavigator.setNextChangeModelInitiated(true);
          _documentNavigator.selectDocument(doc);
        }
      });
    }
    catch(Exception e) { throw new UnexpectedException(e); }
  }
  
  public Container getDocCollectionWidget() { return _documentNavigator.asContainer(); }
  
  /** Sets the active document to be the next one in the collection. */
  public void setActiveNextDocument() {
    OpenDefinitionsDocument key = _activeDocument;
    OpenDefinitionsDocument nextKey = _documentNavigator.getNext(key);
    if (key != nextKey) setActiveDocument(nextKey);
    else setActiveDocument(_documentNavigator.getFirst());
    /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
  }
  
  /** Sets the active document to be the previous one in the collection. */
  public void setActivePreviousDocument() {
    OpenDefinitionsDocument key = _activeDocument;
    OpenDefinitionsDocument prevKey = _documentNavigator.getPrevious(key);
    if (key != prevKey) setActiveDocument(prevKey);
    else setActiveDocument(_documentNavigator.getLast());
    /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
  }
  
  //----------------------- End SingleDisplay Methods -----------------------//
  
  /** Returns whether there is currently only one open document which is untitled and unchanged. */
  private boolean _hasOneEmptyDocument() {
    return getDocumentCount() == 1 && _activeDocument.isUntitled() &&
      ! _activeDocument.isModifiedSinceSave();
  }
  
  /** Creates a new document if there are currently no documents open. */
  private void _ensureNotEmpty() {
    if (getDocumentCount() == 0) newFile(getMasterWorkingDirectory());
  }
  
  /** Makes sure that none of the documents in the list are active.
    * Should only be executed in event thread.
    */
  private void _ensureNotActive(List<OpenDefinitionsDocument> docs) {
    if (docs.contains(getActiveDocument())) {
      // Find the one that should be the new active document
      IDocumentNavigator<OpenDefinitionsDocument> nav = getDocumentNavigator();
      
      OpenDefinitionsDocument item = docs.get(docs.size()-1);
      OpenDefinitionsDocument nextActive = nav.getNext(item);
      if (!nextActive.equals(item)) {
        setActiveDocument(nextActive);
        return;
      }
      
      item = docs.get(0);
      nextActive = nav.getPrevious(item);
      if (!nextActive.equals(item)) {
        setActiveDocument(nextActive);
        return;
      }
      
      throw new RuntimeException("No document to set active before closing");
    }
  }
  
  /** Selects the first document as the active document. */
  public void setActiveFirstDocument() { setActiveDocument(getOpenDefinitionsDocuments().get(0)); }
  
  private void _setActiveDoc(INavigatorItem idoc) {
//     try { idoc.checkIfClassFileInSync(); } 
//     catch(DocumentClosedException dce) { /* do nothing */ }
    _activeDocument = (OpenDefinitionsDocument) idoc;
    installActiveDocument();    // notify single display model listeners   
  }
  
  /** Invokes the activeDocumentChanged method in the global listener on the argument _activeDocument.  This process
    * sets up _activeDocument as the document in the definitions pane. */
  public void installActiveDocument() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.activeDocumentChanged(_activeDocument); } });
  }
  
  /** Makes the active document (in this model) the selection in the documentNavigator and invokes the 
    * activedocumentRefreshed method in the global listener on this document.  The latter process
    * refreshes the state of the _activeDocument as the document in the definitions pane. */
  public void refreshActiveDocument() { 
    _documentNavigator.selectDocument(_activeDocument);
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.activeDocumentRefreshed(_activeDocument); } });  
  }
  
  /** Ensures that the _jvmStarter thread has executed. Never called in practice outside of GlobalModelTestCase.setUp(). */
  public void ensureJVMStarterFinished() { }
  
  public void setCustomManifest(String manifest){ 
    _state.setProjectChanged(true);
    _state.setCustomManifest(manifest); 
  }
  public String getCustomManifest() { return _state.getCustomManifest(); }
}

