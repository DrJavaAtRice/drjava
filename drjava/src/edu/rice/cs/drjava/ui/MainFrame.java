/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.dnd.*;
import java.beans.*;

import java.io.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.datatransfer.*;
import java.lang.ref.WeakReference;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaRoot;
import edu.rice.cs.drjava.RemoteControlClient;
import edu.rice.cs.drjava.platform.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.definitions.DocumentUIListener;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.ui.config.ConfigFrame;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputFrame;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputModel;
import edu.rice.cs.drjava.ui.ClipboardHistoryFrame;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.project.*;
import edu.rice.cs.util.XMLConfig;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.docnavigation.*;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.swing.AsyncTaskLauncher;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.BorderlessSplitPane;
import edu.rice.cs.util.swing.ConfirmCheckBoxDialog;
import edu.rice.cs.util.swing.DelegatingAction;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.FileDisplayManager;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.RightClickMouseAdapter;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.StringOps;

import static edu.rice.cs.drjava.ui.RecentFileManager.*;
import static edu.rice.cs.drjava.config.OptionConstants.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputModel.*;
import static edu.rice.cs.util.XMLConfig.XMLConfigException;

/** DrJava's main window. */
public class MainFrame extends JFrame implements ClipboardOwner, DropTargetListener {
  
  private static final int INTERACTIONS_TAB = 0;
  private static final int CONSOLE_TAB = 1;
  private static final String ICON_PATH = "/edu/rice/cs/drjava/ui/icons/";
  private static final String DEBUGGER_OUT_OF_SYNC =
    " Current document is out of sync with the debugger and should be recompiled!";
  
  /** Number of milliseconds to wait before displaying "Stepping..." message after a step is requested in 
    * the debugger.
    */
  private static final int DEBUG_STEP_TIMER_VALUE = 2000;
  
  /** The model which controls all logic in DrJava. */
  private final AbstractGlobalModel _model;
  
  /** The main model listener attached by the main frame to the global model */
  private final ModelListener _mainListener; 
  
  /** Maps an OpenDefDoc to its JScrollPane. */
  private final Hashtable<OpenDefinitionsDocument, JScrollPane> _defScrollPanes;
  
  /** The currently displayed DefinitionsPane. */
  private volatile DefinitionsPane _currentDefPane;
  
  /** The filename currently being displayed. */
  private volatile String _fileTitle = "";
  
  // Tabbed panel fields
  public final LinkedList<TabbedPanel>  _tabs = new LinkedList<TabbedPanel>();  
  public final JTabbedPane _tabbedPane;
  public volatile Component _lastFocusOwner;
  
  private final CompilerErrorPanel _compilerErrorPanel;
  private final InteractionsPane _consolePane;
  private final JScrollPane _consoleScroll;  // redirects focus to embedded _consolePane
  private final ConsoleController _consoleController;  // move to controller
  private final InteractionsPane _interactionsPane;
  private final JPanel _interactionsContainer;  // redirects focus to embedded _interactionsPane
  private final InteractionsController _interactionsController;  // move to controller
  private final JUnitPanel _junitErrorPanel;
  private final JavadocErrorPanel _javadocErrorPanel;
  private final FindReplacePanel _findReplace;
  private final BreakpointsPanel _breakpointsPanel;
  private final BookmarksPanel _bookmarksPanel;
  private final LinkedList<Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>>> _findResults =
    new LinkedList<Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>>>();
  
  private volatile boolean _showDebugger;  // whether the supporting context is debugger capable
  
  private volatile InteractionsScriptController _interactionsScriptController;
  private volatile InteractionsScriptPane _interactionsScriptPane;
  private volatile DebugPanel _debugPanel;
  
  /** Panel to hold both InteractionsPane and its sync message. */
  
  // Status bar fields
  private final JPanel _statusBar = new JPanel(new BorderLayout()); //( layout );
  private final JLabel _statusField = new JLabel();
  private final JLabel _statusReport = new JLabel();  //("This is the text for the center message");
  private final JLabel _currLocationField = new JLabel();
  private final PositionListener _posListener = new PositionListener();
  
  // Split panes for layout
  private final JSplitPane _docSplitPane;
  private final JSplitPane _debugSplitPane;
  final JSplitPane _mainSplit;
  
  // private Container _docCollectionWidget;
  private volatile JButton _compileButton;
  private volatile JButton _closeButton;
  private volatile JButton _undoButton;
  private volatile JButton _redoButton;
  private volatile JButton _runButton;
  private volatile JButton _junitButton;
  private JButton _errorsButton;
  
  private final JToolBar _toolBar;
  private final JFileChooser _interactionsHistoryChooser;
  
  // Menu fields
  private final JMenuBar _menuBar;
  private final JMenu _fileMenu;
  private final JMenu _editMenu;
  private final JMenu _toolsMenu;
  private final JMenu _projectMenu;
  private final JMenu _languageLevelMenu;
  private final JMenu _helpMenu;
  
  private volatile JMenu _debugMenu;
  private volatile JMenuItem _debuggerEnabledMenuItem;
  
  // Popup menus
  private JPopupMenu _interactionsPanePopupMenu;
  private JPopupMenu _consolePanePopupMenu;
  
  // Cached frames and dialogs
  private final ConfigFrame _configFrame;
  private final HelpFrame _helpFrame;
  private final QuickStartFrame _quickStartFrame;
  private final AboutDialog _aboutDialog;
  private final RecentDocFrame _recentDocFrame;    /** Holds/shows the history of documents for ctrl-tab. */
  
//  private ProjectPropertiesFrame _projectPropertiesFrame;
  
  /** Keeps track of the recent files list in the File menu. */
  private final RecentFileManager _recentFileManager;
  
  /** Keeps track of the recent projects list in the Project menu */
  private final RecentFileManager _recentProjectManager;
  
  private volatile File _currentProjFile;
  
  /** Timer to display "Stepping..." message if a step takes longer than a certain amount of time.  All accesses
    * must be synchronized on it.
    */
  private final Timer _debugStepTimer;
  
  /** The current highlight displaying the location of the debugger's thread,
    * if there is one.  If there is none, this is null.
    */
  private volatile HighlightManager.HighlightInfo _currentThreadLocationHighlight = null;
  
  /** Table to map breakpoints to their corresponding highlight objects. */
  private final Hashtable<Breakpoint, HighlightManager.HighlightInfo> _documentBreakpointHighlights;
  
  /** Table to map bookmarks to their corresponding highlight objects. */
  private final Hashtable<DocumentRegion, HighlightManager.HighlightInfo> _documentBookmarkHighlights;
  
  /** Whether to display a prompt message before quitting. */
  private volatile boolean _promptBeforeQuit;
  
  /** Listener for Interactions JVM */
  final private ConfigOptionListeners.SlaveJVMXMXListener _slaveJvmXmxListener;
  
  /** Listener for Main JVM */
  final private ConfigOptionListeners.MasterJVMXMXListener _masterJvmXmxListener;
  
  /** Window adapter for "pseudo-modal" dialogs, i.e. non-modal dialogs that insist on keeping the focus. */
  protected java.util.HashMap<Window,WindowAdapter> _modalWindowAdapters 
    = new java.util.HashMap<Window,WindowAdapter>();

  /** The owner of the modal window listener has already been taken by another window. */
  protected volatile Window _modalWindowAdapterOwner = null;
  
  /** For opening files.  We have a persistent dialog to keep track of the last directory from which we opened. */
  private final JFileChooser _openChooser;
  
  /** For opening project files. */
  private final JFileChooser _openProjectChooser;
  
  /** For saving files. We have a persistent dialog to keep track of the last directory from which we saved. */
  private final JFileChooser _saveChooser;
  
  /** Filter for regular java files (.java and .j). */
  private final javax.swing.filechooser.FileFilter _javaSourceFilter = new JavaSourceFilter();
  
  /** Filter for drjava project files (.xml and .pjt) */
  private final javax.swing.filechooser.FileFilter _projectFilter = new javax.swing.filechooser.FileFilter() {
    public boolean accept(File f) {
      return f.isDirectory() || 
        f.getPath().endsWith(PROJECT_FILE_EXTENSION) ||
        f.getPath().endsWith(OLD_PROJECT_FILE_EXTENSION);
    }
    public String getDescription() { 
      return "DrJava Project Files (*"+PROJECT_FILE_EXTENSION+", *"+OLD_PROJECT_FILE_EXTENSION+")";
    }
  };
  
  /** Filter for any files (*.*) */
  private final javax.swing.filechooser.FileFilter _anyFileFilter = new javax.swing.filechooser.FileFilter() {
    public boolean accept(File f) { return true; }
    public String getDescription() { return "All files (*.*)"; }
  };
  
  /** Returns the files to open to the model (command pattern). */
  private final FileOpenSelector _openSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      //_openChooser.removeChoosableFileFilter(_projectFilter);
      _openChooser.resetChoosableFileFilters();
      
      _openChooser.setFileFilter(_javaSourceFilter);
      return getOpenFiles(_openChooser);
    }
  };
  
  /** Returns the files to open to the model (command pattern). */
  private final FileOpenSelector _openFileOrProjectSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      //_openChooser.removeChoosableFileFilter(_projectFilter);
      _openChooser.resetChoosableFileFilters();
      
      _openChooser.addChoosableFileFilter(_projectFilter);
      _openChooser.setFileFilter(_javaSourceFilter);
      return getOpenFiles(_openChooser);
    }
  };
  
  /** Returns the project file to open. */
  private final FileOpenSelector _openProjectSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      File[] retFiles = getOpenFiles(_openProjectChooser);
      return retFiles;
    }
  };
  
  /** Returns the files to open. */
  private final FileOpenSelector _openAnyFileSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      _openChooser.resetChoosableFileFilters();
      _openChooser.setFileFilter(_anyFileFilter);
      return getOpenFiles(_openChooser);
    }
  };
  
  /** Returns the file to save to the model (command pattern).  */
  private final FileSaveSelector _saveSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException { return getSaveFile(_saveChooser); }
    public boolean warnFileOpen(File f) { return _warnFileOpen(f); }
    public boolean verifyOverwrite() { return _verifyOverwrite(); }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
      _model.setActiveDocument(doc);
      String text = "File " + oldFile.getAbsolutePath() +
        "\ncould not be found on disk!  It was probably moved\n" +
        "or deleted.  Would you like to save it in a new file?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             "File Moved or Deleted",
                                             JOptionPane.YES_NO_OPTION);
      return (rc == JOptionPane.YES_OPTION);
    }
  };
  
  /** Returns the file to save to the model (command pattern). */
  private final FileSaveSelector _saveAsSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException { return getSaveFile(_saveChooser); }
    public boolean warnFileOpen(File f) { return _warnFileOpen(f); }
    public boolean verifyOverwrite() { return _verifyOverwrite(); }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
  };
  
  /** Returns the file to save to the model (command pattern). 
    private final FileSaveSelector _renameSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException { return getSaveFile(_saveChooser); }
    public boolean warnFileOpen(File f) { return _warnFileOpen(f); }
    public boolean verifyOverwrite() { return _verifyOverwrite(); }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
    }; */
  
  /** Provides the view's contribution to the Javadoc interaction. */
  private final JavadocDialog _javadocSelector = new JavadocDialog(this);
  
  /** Provides a chooser to open a directory */  
  private final DirectoryChooser _folderChooser;
  private final JCheckBox _openRecursiveCheckBox;
  
  private final Action _moveToAuxiliaryAction = new AbstractAction("Include With Project") {
    { /* initalization block */
      String msg = 
      "<html>Open this document each time this project is opened.<br>"+
      "This file would then be compiled and tested with the<br>"+
      "rest of the project.</html>";
      putValue(Action.SHORT_DESCRIPTION, msg);
    }
    public void actionPerformed(ActionEvent ae) { _moveToAuxiliary(); }
  };
  private final Action _removeAuxiliaryAction = new AbstractAction("Do Not Include With Project") {
    { putValue(Action.SHORT_DESCRIPTION, "Do not open this document next time this project is opened."); }
    public void actionPerformed(ActionEvent ae) { _removeAuxiliary(); }
  };
  private final Action _moveAllToAuxiliaryAction = new AbstractAction("Include All With Project") {
    { /* initalization block */
      String msg = 
      "<html>Open these documents each time this project is opened.<br>"+
      "These files would then be compiled and tested with the<br>"+
      "rest of the project.</html>";
      putValue(Action.SHORT_DESCRIPTION, msg);
    }
    public void actionPerformed(ActionEvent ae) { _moveAllToAuxiliary(); }
  };
  private final Action _removeAllAuxiliaryAction = new AbstractAction("Do Not Include Any With Project") {
    { putValue(Action.SHORT_DESCRIPTION, "Do not open these documents next time this project is opened."); }
    public void actionPerformed(ActionEvent ae) { _removeAllAuxiliary(); }
  };
  
  /** Creates a new blank document and select it in the definitions pane. */
  private final Action _newAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
//      System.out.println("------------------new----------------------");
      _new();
    }
  };
  
  private final Action _newProjectAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) { _newProject(); }
  };
  
  private volatile AbstractAction _runProjectAction = new AbstractAction("Run Main Document of Project") {
    public void actionPerformed(ActionEvent ae) { _runProject(); }
  };
  
  /** The jar options dialog. */
  private final JarOptionsDialog _jarOptionsDialog;
  
  /** Initializes the "Create Jar from Project dialog. */
  private void initJarOptionsDialog() {
    if (DrJava.getConfig().getSetting(DIALOG_JAROPTIONS_STORE_POSITION).booleanValue())
      _jarOptionsDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_JAROPTIONS_STATE));  
  }
  
  /** Reset the position of the "Create Jar from Project" dialog. */
  public void resetJarOptionsDialogPosition() {
    _jarOptionsDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_JAROPTIONS_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_JAROPTIONS_STATE, "default");
    }
  }
  
  private final Action _jarProjectAction = new AbstractAction("Create Jar File from Project...") {
    public void actionPerformed(ActionEvent ae) {
      _jarOptionsDialog.setVisible(true);
    }
  };
  
  
  /** Sets the document in the definitions pane to a new templated junit test class. */
  private final Action _newJUnitTestAction = new AbstractAction("New JUnit Test Case...") {
    public void actionPerformed(ActionEvent ae) {
      String testName = JOptionPane.showInputDialog(MainFrame.this,
                                                    "Please enter a name for the test class:",
                                                    "New JUnit Test Case",
                                                    JOptionPane.QUESTION_MESSAGE);
      if (testName != null) {
        String ext;
        for(int i=0; i < DrJavaRoot.LANGUAGE_LEVEL_EXTENSIONS.length; i++) {
          ext = "." + DrJavaRoot.LANGUAGE_LEVEL_EXTENSIONS[i];
          if (testName.endsWith(ext)) testName = testName.substring(0, testName.length() - ext.length());
        }
        // For now, don't include setUp and tearDown
        _model.newTestCase(testName, false, false);
      }
    }
  };
  
  /** Asks user for file name and and reads that file into the definitions pane. */
  private final Action _openAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) {
      _open();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Asks user for directory name and and reads it's files (and subdirectories files, on request) to
    * the definitions pane.
    */
  private final Action _openFolderAction  = new AbstractAction("Open Folder...") {
    public void actionPerformed(ActionEvent ae) { 
      _openFolder();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Asks user for file name and and reads that file into the definitions pane. */
  private final Action _openFileOrProjectAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) { 
      _openFileOrProject(); 
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Asks user for project file name and and reads the associated files into the file navigator (and places the first
    * source file in the editor pane)
    */
  private final Action _openProjectAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) { _openProject(); }
  };
  
  private final Action _closeProjectAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) { 
      closeProject();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  
  /** Closes the current active document, prompting to save if necessary. */
  private final Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) { 
      _close();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Closes all open documents, prompting to save if necessary. */
  private final Action _closeAllAction = new AbstractAction("Close All") {
    public void actionPerformed(ActionEvent ae) { 
      _closeAll();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Closes all open documents, prompting to save if necessary. */
  private final Action _closeFolderAction = new AbstractAction("Close Folder") {
    public void actionPerformed(ActionEvent ae) { 
      _closeFolder();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Opens all the files in the current folder. */
  private final Action _openAllFolderAction = new AbstractAction("Open All Files") {
    public void actionPerformed(ActionEvent ae) {
      // now works with multiple selected folders
      java.util.List<File> l= _model.getDocumentNavigator().getSelectedFolders();
      for(File f: l) {
        File fAbs = new File(_model.getProjectRoot(), f.toString());
        _openFolder(fAbs, false);  
      }
      
      // The following does not apply anymore:
      // Get the Folder that was clicked on by the user. When the user clicks on a directory component in the 
      // navigation pane, the current directory is updated in the openChooser JFileChooser component.  So the 
      // clicked on directory is obtained in this way
      // File dir = _openChooser.getCurrentDirectory();
      // _openFolder(dir, false);  
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Opens a files in the current folder. */
  private final Action _openOneFolderAction = new AbstractAction("Open File in Folder") {
    public void actionPerformed(ActionEvent ae)  { 
      _open();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Creates a new untitled, empty file in the current folder. */
  public final Action _newFileFolderAction = new AbstractAction("Create New File in Folder") {
    public void actionPerformed(ActionEvent ae)  {
      //make this new document the document in the document pane
      _new();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** Tests all the files in a folder. */
  private volatile AbstractAction _junitFolderAction = new AbstractAction("Test Folder") {
    public final void actionPerformed(ActionEvent ae) { _junitFolder(); }
  };
  
  /** Saves the current document. */
  private final Action _saveAction = new AbstractAction("Save") {
    public final void actionPerformed(ActionEvent ae) { _save(); }
  };
  
  /** Ensures that pack() is run in the event thread. Only used in test code */
  public void pack() {
    Utilities.invokeAndWait(new Runnable() { public void run() { packHelp(); } });
  }
  
  /** Helper method that provides access to super.pack() within the anonymous class new Runnable() {...} above */
  private void packHelp() { super.pack(); }
  
  /** Supports MainFrameTest.*/
  public boolean saveEnabledHuh() { return _saveAction.isEnabled(); }
  
  /** Asks the user for a file name and saves the active document (in the definitions pane) to that file. */
  private final Action _saveAsAction = new AbstractAction("Save As...") {
    public void actionPerformed(ActionEvent ae) { _saveAs(); }
  };
  
  /** Asks the user for a file name and renames and saves the active document (in the definitions pane) to that file. */
  private final Action _renameAction = new AbstractAction("Rename") {
    public void actionPerformed(ActionEvent ae) { _rename(); }
  };  
  
  private final Action _saveProjectAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _saveAll();  // saves project file and all modified project source files; does not save external files
    }
  };
  
  private final Action _saveProjectAsAction = new AbstractAction("Save As...") {
    public void actionPerformed(ActionEvent ae) {
      if (_saveProjectAs()) {  // asks the user for a new project file name; sets _projectFile in global model to this value
        _saveAll();  // performs a save all operation using this new project file name, but ONLY if the "Save as" was not cancelled
      }
    }
  };

  private final Action _exportProjectInOldFormatAction = 
    new AbstractAction("Export Project In Old \"" + OLD_PROJECT_FILE_EXTENSION + "\" Format") {
    public void actionPerformed(ActionEvent ae) {
      File cpf = _currentProjFile;
      _currentProjFile = FileOps.NULL_FILE;
      if (_saveProjectAs()) {  // asks the user for a new project file name; sets _projectFile in global model to this value
        _saveAllOld();
      }
      _currentProjFile = cpf;
      _model.setProjectFile(cpf);
      _recentProjectManager.updateOpenFiles(cpf);
    }
  };
  
  /** Reverts the current document. */
  private final Action _revertAction = new AbstractAction("Revert to Saved") {
    public void actionPerformed(ActionEvent ae) {
      String title = "Revert to Saved?";
      
      // update message to reflect the number of files
      int count = _model.getDocumentNavigator().getDocumentSelectedCount();
      String message;
      if (count==1) {
        message = "Are you sure you want to revert the current " +
          "file to the version on disk?";
      }
      else {
        message = "Are you sure you want to revert the " + count +
          " selected files to the versions on disk?";
      }
      
      int rc;
      String fileName = null;
      Object[] options = {"Yes", "No"};  
      rc = JOptionPane.showOptionDialog(MainFrame.this, message, title, JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
      if (rc == JOptionPane.YES_OPTION) {
        _revert();
      }
    }
  };
  
  /** Reverts all open documents.
    * (not working yet)
    private Action _revertAllAction = new AbstractAction("Revert All to Saved") {
    public void actionPerformed(ActionEvent ae) {
    String title = "Revert All to Saved?";
    
    String message = "Are you sure you want to revert all open " +
    "files to the versions on disk?";
    
    int rc = JOptionPane.showConfirmDialog(MainFrame.this,
    message,
    title,
    JOptionPane.YES_NO_OPTION);
    if (rc == JOptionPane.YES_OPTION) {
    _revertAll();
    }
    }
    };*/
  
  /** Saves all documents, prompting for file names as necessary. */
  final Action _saveAllAction = new AbstractAction("Save All") {
    public void actionPerformed(ActionEvent ae) { _saveAll(); }
  };
  
  /** Prints the current document. */
  private final Action _printDefDocAction = new AbstractAction("Print...") {
    public void actionPerformed(ActionEvent ae) { _printDefDoc(); }
  };
  
  /** Prints the console document. */
  private final Action _printConsoleAction = new AbstractAction("Print Console...") {
    public void actionPerformed(ActionEvent ae) { _printConsole(); }
  };
  
  /** Prints the interactions document. */
  private final Action _printInteractionsAction = new AbstractAction("Print Interactions...") {
    public void actionPerformed(ActionEvent ae) { _printInteractions(); }
  };
  
  /** Opens the print preview window. */
  private final Action _printDefDocPreviewAction = new AbstractAction("Print Preview...") {
    public void actionPerformed(ActionEvent ae) { _printDefDocPreview(); }
  };
  
  /** Opens the print preview window. */
  private final Action _printConsolePreviewAction = new AbstractAction("Print Preview...") {
    public void actionPerformed(ActionEvent ae) { _printConsolePreview(); }
  };
  
  /** Opens the print preview window. */
  private final Action _printInteractionsPreviewAction = new AbstractAction("Print Preview...") {
    public void actionPerformed(ActionEvent ae) { _printInteractionsPreview(); }
  };
  
  /** Opens the page setup window. */
  private final Action _pageSetupAction = new AbstractAction("Page Setup...") {
    public void actionPerformed(ActionEvent ae) { _pageSetup(); }
  };
  
//  /** Compiles all the project. */
//  private Action _compileOpenProjectAction = new AbstractAction("Compile Open Project Files") {
//    public void actionPerformed(ActionEvent ae) { _compileAll(); } // right now, it's the same as compile all
//  };
  
  /** Compiles the document in the definitions pane. */
  private final Action _compileAction = new AbstractAction("Compile Current Document") {
    public void actionPerformed(ActionEvent ae) { 
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes(); 
      updateStatusField("Compiling " + _fileTitle);
      _compile();
      updateStatusField("Compilation of current document completed");
    }
  };
  
  /** Compiles all the project. */
  private volatile AbstractAction _compileProjectAction = new AbstractAction("Compile Project") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes();
      updateStatusField("Compiling all source files in open project");
      _compileProject(); 
      _findReplace.updateFirstDocInSearch();
      updateStatusField("Compilation of open project completed");
    }
  };
  
  /** Compiles all documents in the navigators active group. */
  private volatile AbstractAction _compileFolderAction = new AbstractAction("Compile Folder") {
    public void actionPerformed(ActionEvent ae) { 
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes();
      updateStatusField("Compiling all sources in current folder");
      _compileFolder();
      _findReplace.updateFirstDocInSearch();
      updateStatusField("Compilation of folder completed");
    }
  };
  
  /** Compiles all open documents. */
  private volatile AbstractAction _compileAllAction = new AbstractAction("Compile All Documents") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes();
      _compileAll();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  /** cleans the build directory */
  private volatile AbstractAction _cleanAction = new AbstractAction("Clean Build Directory") {
    public void actionPerformed(ActionEvent ae) { _clean(); }
  };

  /** auto-refresh the project and open new files */
  private volatile AbstractAction _autoRefreshAction = new AbstractAction("Auto-Refresh Project") {
    public void actionPerformed(ActionEvent ae) { _model.autoRefreshProject(); }
  };
  
  /** Finds and runs the main method of the current document, if it exists. */
  private volatile AbstractAction _runAction = new AbstractAction("Run Document's Main Method") {
    public void actionPerformed(ActionEvent ae) { _runMain(); }
  };
  
  /** Runs JUnit on the document in the definitions pane. */
  private volatile AbstractAction _junitAction = new AbstractAction("Test Current Document") {
    public void actionPerformed(ActionEvent ae) { 
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) _mainSplit.resetToPreferredSizes();
      _junit(); 
    }
  };
  
  /** Runs JUnit over all open JUnit tests. */
  private volatile AbstractAction _junitAllAction = new AbstractAction("Test All Documents") {
    public void actionPerformed(ActionEvent e) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) _mainSplit.resetToPreferredSizes();
      _junitAll();
      _findReplace.updateFirstDocInSearch();
    }
    
  };
  
  /** Runs JUnit over all open JUnit tests in the project directory. */
  private volatile AbstractAction _junitProjectAction = new AbstractAction("Test Project") {
    public void actionPerformed(ActionEvent e) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) _mainSplit.resetToPreferredSizes();
      _junitProject();
      _findReplace.updateFirstDocInSearch();
    }
  };
  
  
//  /** JUnit a directory. */
//  private final Action _junitProjectAction = new AbstractAction("Test Project") {
//    public void actionPerformed(ActionEvent e) {
//      new Thread("Running JUnit Tests") {
//        public void run() {
//          if (_model.isProjectActive()) {
//            try {
//              // hourglassOn();  // also done in junitStarted
//              _model.junitAll();
//            }
//            finally {
//             //  hourglassOff(); // also done in junitEnded
//            }
//          }
//        }
//      }.start();
//    }
//  };
  
  /** Runs Javadoc on all open documents (and the files in their packages). */
  private final Action _javadocAllAction = new AbstractAction("Javadoc All Documents") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes();
      try {
        // hourglassOn();
        JavadocModel jm = _model.getJavadocModel();
        File suggestedDir = jm.suggestJavadocDestination(_model.getActiveDocument());
        _javadocSelector.setSuggestedDir(suggestedDir);
        jm.javadocAll(_javadocSelector, _saveSelector);
      }
      catch (IOException ioe) { _showIOError(ioe); }
      finally {
        // hourglassOff();
      }
    }
  };
  
  /** Runs Javadoc on the current document. */
  private final Action _javadocCurrentAction = new AbstractAction("Preview Javadoc for Current Document") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes();
      try { _model.getActiveDocument().generateJavadoc(_saveSelector); }
      catch (IOException ioe) { _showIOError(ioe); }
    }
  };
  
  /** Default cut action.  Returns focus to the correct pane. */
  final Action cutAction = new DefaultEditorKit.CutAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = MainFrame.this.getFocusOwner();
      super.actionPerformed(e);
      if (_currentDefPane.hasFocus()) {
        String s = Utilities.getClipboardSelection(c);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
      if (c != null) c.requestFocusInWindow();
    }
  };
  
  /** Default copy action.  Returns focus to the correct pane. */
  final Action copyAction = new DefaultEditorKit.CopyAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = MainFrame.this.getFocusOwner();
      super.actionPerformed(e);
      if (_currentDefPane.hasFocus() && _currentDefPane.getSelectedText() != null) {
        String s = Utilities.getClipboardSelection(c);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
      if (c != null) c.requestFocusInWindow();
    }
  };
  
  /** We lost ownership of what we put in the clipboard. */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // ignore
  }
  
  /** Default paste action.  Returns focus to the correct pane. */
  final Action pasteAction = new DefaultEditorKit.PasteAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = MainFrame.this.getFocusOwner();
      if (_currentDefPane.hasFocus()) {
        _currentDefPane.endCompoundEdit();
//        CompoundUndoManager undoMan = _model.getActiveDocument().getUndoManager(); // French keyboard fix
//        int key = undoMan.startCompoundEdit();                                     // French keyboard fix
        super.actionPerformed(e);
        _currentDefPane.endCompoundEdit(); // replaced line below for French keyboard fix
//        undoMan.endCompoundEdit(key);                                              // French keyboard fix
      }
      else super.actionPerformed(e);
      
      if (c != null) c.requestFocusInWindow();      
    }
  };
  
  /** Reset the position of the "Clipboard History" dialog. */
  public void resetClipboardHistoryDialogPosition() {
    if (DrJava.getConfig().getSetting(DIALOG_CLIPBOARD_HISTORY_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_CLIPBOARD_HISTORY_STATE, "default");
    }
  }
  
  /** The "Clipboard History" dialog. */
  private ClipboardHistoryFrame _clipboardHistoryDialog = null;
  
  /** Asks the user for a file name and goes there. */
  private final Action _pasteHistoryAction = new AbstractAction("Paste from History...") {
    public void actionPerformed(final ActionEvent ae) {
      final ClipboardHistoryFrame.CloseAction cancelAction = new ClipboardHistoryFrame.CloseAction() {
        public Object apply(String s) {
          // "Clipboard History" dialog position and size.
          if ((DrJava.getConfig().getSetting(DIALOG_CLIPBOARD_HISTORY_STORE_POSITION).booleanValue())
                && (_clipboardHistoryDialog != null) && (_clipboardHistoryDialog.getFrameState() != null)) {
            DrJava.getConfig().
              setSetting(DIALOG_CLIPBOARD_HISTORY_STATE, (_clipboardHistoryDialog.getFrameState().toString()));
          }
          else {
            // Reset to defaults to restore pristine behavior.
            DrJava.getConfig().setSetting(DIALOG_CLIPBOARD_HISTORY_STATE, DIALOG_CLIPBOARD_HISTORY_STATE.getDefault());
          }
          return null;
        }
      };
      ClipboardHistoryFrame.CloseAction okAction = new ClipboardHistoryFrame.CloseAction() {
        public Object apply(String s) {
          cancelAction.apply(null);
          
          StringSelection ssel = new StringSelection(s);
          Clipboard cb = MainFrame.this.getToolkit().getSystemClipboard();
          if (cb != null) {
            cb.setContents(ssel, MainFrame.this);
            pasteAction.actionPerformed(ae);
          }
          return null;
        }
      };
      
      _clipboardHistoryDialog = new ClipboardHistoryFrame(MainFrame.this, 
                                                          "Clipboard History", ClipboardHistoryModel.singleton(),
                                                          okAction, cancelAction);
      if (DrJava.getConfig().getSetting(DIALOG_CLIPBOARD_HISTORY_STORE_POSITION).booleanValue()) {
        _clipboardHistoryDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_CLIPBOARD_HISTORY_STATE));
      }
      _clipboardHistoryDialog.setVisible(true);
    }
  };
  
  /** Copies whatever is currently in the interactions pane at the prompt to the definitions pane.  If the 
    * current string is empty, then it will attempt to return the last entry from the interactions pane's history.
    */
  private final Action _copyInteractionToDefinitionsAction =
    new AbstractAction("Lift Current Interaction to Definitions") {
    public void actionPerformed(ActionEvent a) {
      String text = _interactionsController.getDocument().getCurrentInput();
      if (! text.equals("")) {
        _putTextIntoDefinitions(text + "\n");
        return;
      }
      try { text = _interactionsController.getDocument().lastEntry(); }
      catch(Exception e) { return; } // no entry to promote
      
      //It is assumed that empty strings are not put into the history
      _putTextIntoDefinitions(text + "\n");
      return;
    }
  };
  
  /** Action that copies the previous interaction to the definitions pane.
    * Is there a good way to get the last history element without perturbing the current document?
    Action copyPreviousInteractionToDefinitionsAction = new AbstractAction("Copy previous interaction to definitions") {
    public void actionPerformed(ActionEvent e) {
    _putTextIntoDefinitions(_interactionsController.getDocument().getCurrentInput() + "\n");
    }
    };*/
  
  /** Undoes the last change to the active definitions document. */
  private final DelegatingAction _undoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      _currentDefPane.endCompoundEdit();
      super.actionPerformed(e);
      _currentDefPane.requestFocusInWindow();
      OpenDefinitionsDocument doc = _model.getActiveDocument();
//      Utilities.showDebug("isModifiedSinceSave() = " + doc.isModifiedSinceSave());
      _saveAction.setEnabled(doc.isModifiedSinceSave() || doc.isUntitled());
//      Utilities.showDebug("check status");
    }
  };
  
  /** Redoes the last undo to the active definitions document. */
  private final DelegatingAction _redoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      _currentDefPane.requestFocusInWindow();
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      _saveAction.setEnabled(doc.isModifiedSinceSave() || doc.isUntitled());
    }
  };
  
  /** Quits DrJava.  Optionally displays a prompt before quitting. */
  private final Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) { _quit(); }
  };
  
  
  /** Quits DrJava.  Optionally displays a prompt before quitting. */
  private final Action _forceQuitAction = new AbstractAction("Force Quit") {
    public void actionPerformed(ActionEvent ae) { _forceQuit(); }
  };
  
  /** Selects all text in window. */
  private final Action _selectAllAction = new AbstractAction("Select All") {
    public void actionPerformed(ActionEvent ae) { _selectAll(); }
  };
  
  private void _showFindReplaceTab() {
    if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
      _mainSplit.resetToPreferredSizes(); 
    if (! _findReplace.isDisplayed()) {
      showTab(_findReplace);
      _findReplace.beginListeningTo(_currentDefPane);
    }
    _findReplace.setVisible(true);
    _tabbedPane.setSelectedComponent(_findReplace);
  }
  
  /** Shows the find/replace tab. */
  private final Action _findReplaceAction = new AbstractAction("Find/Replace") {
    public void actionPerformed(ActionEvent ae) {
      _showFindReplaceTab();
      // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the _findReplace tab has been selected
      EventQueue.invokeLater(new Runnable() { public void run() { _findReplace.requestFocusInWindow(); } });
    }
  };
  
  /** Find the next instance of the find word. */
  private final Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent ae) {
      _showFindReplaceTab();
      if (!DrJava.getConfig().getSetting(FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
        // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the _findReplace tab has been selected
        EventQueue.invokeLater(new Runnable() { public void run() { _findReplace.requestFocusInWindow(); } });
      }
      _findReplace.findNext();
      _currentDefPane.requestFocusInWindow();  
      // atempt to fix intermittent bug where _currentDefPane listens but does not echo and won't undo!
    }
  };
  
  /** Does the find next in the opposite direction. If the direction is backward it searches forward. */
  private final Action _findPrevAction = new AbstractAction("Find Previous") {
    public void actionPerformed(ActionEvent ae) {
      _showFindReplaceTab();
      if (!DrJava.getConfig().getSetting(FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
        // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the _findReplace tab has been selected
        EventQueue.invokeLater(new Runnable() { public void run() { _findReplace.requestFocusInWindow(); } });
      }
      _findReplace.findPrevious();
      _currentDefPane.requestFocusInWindow();
    }
  };
  
  /** Asks the user for a line number and goes there. */
  private final Action _gotoLineAction = new AbstractAction("Go to Line...") {
    public void actionPerformed(ActionEvent ae) {
      int pos = _gotoLine();
      _currentDefPane.requestFocusInWindow();
      if (pos != -1) _currentDefPane.setCaretPosition(pos);  
      // The preceding is a brute force attempt to fix intermittent failure to display caret
    }
  };
  
  private static abstract class ClassNameAndPackageEntry implements Comparable<ClassNameAndPackageEntry> {
    /** Return the simple class name, e.g. "Integer". */
    public abstract String getClassName();
    /** Return the full package including the last period, e.g. "java.lang.". */
    public abstract String getFullPackage();
    
    public int compareTo(ClassNameAndPackageEntry other) {
      int res = getClassName().toLowerCase().compareTo(other.getClassName().toLowerCase());
      if (res!=0) { return res; }
      return getFullPackage().toLowerCase().compareTo(other.getFullPackage().toLowerCase());
    }
    public boolean equals(Object other) {
      if (other==null) { return false; }
      if (! (other instanceof ClassNameAndPackageEntry)) return false;
      ClassNameAndPackageEntry o = (ClassNameAndPackageEntry)other;
      return ((getClassName().equals(o.getClassName())) &&
              (getFullPackage().equals(o.getFullPackage())));
    }
    public int hashCode() { return getClassName().hashCode() * 31 + getFullPackage().hashCode(); }
  }
  
  /** Wrapper class for the "Go to File" and "Auto-Complete" dialog list entries.
    * Provides the ability to have the same OpenDefinitionsDocument in there multiple
    * times with different toString() results.
    */
  private static class GoToFileListEntry extends ClassNameAndPackageEntry {
    public final OpenDefinitionsDocument doc;
    protected String fullPackage = null;
    protected final String str;
    public GoToFileListEntry(OpenDefinitionsDocument d, String s) {
      doc = d;
      str = s;
    }
    public String getFullPackage() {
      if (fullPackage!=null) { return fullPackage; }
      fullPackage = "";
      if (doc!=null) {
        try {
          fullPackage = doc.getPackageName();
          if (fullPackage.length()>0) { fullPackage += '.'; }
        }
        catch(Exception e) { fullPackage = ""; }
      }
      return fullPackage;
    }
    public String getClassName() { return str; }
    public String toString() { return str; }
  }
  
  /** Reset the position of the "Go to File" dialog. */
  public void resetGotoFileDialogPosition() {
    initGotoFileDialog();
    _gotoFileDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_GOTOFILE_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_GOTOFILE_STATE, "default");
    }
  }
  
  /** Initialize dialog if necessary. */
  void initGotoFileDialog() {
    if (_gotoFileDialog == null) {
      PredictiveInputFrame.InfoSupplier<GoToFileListEntry> info = 
        new PredictiveInputFrame.InfoSupplier<GoToFileListEntry>() {
        public String apply(GoToFileListEntry entry) {
          final StringBuilder sb = new StringBuilder();
          
          if (entry.doc != null) {
            try {
              try { sb.append(FileOps.makeRelativeTo(entry.doc.getRawFile(), entry.doc.getSourceRoot())); }
              catch(IOException e) { sb.append(entry.doc.getFile()); }
            }
            catch(edu.rice.cs.drjava.model.FileMovedException e) { sb.append(entry + " was moved"); }
            catch(java.lang.IllegalStateException e) { sb.append(entry); }
            catch(InvalidPackageException e) { sb.append(entry); }
          } 
          else sb.append(entry);
          return sb.toString();
        }
      };
      PredictiveInputFrame.CloseAction<GoToFileListEntry> okAction = 
        new PredictiveInputFrame.CloseAction<GoToFileListEntry>() {
        public String getName() { return "OK"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); }
        public Object apply(PredictiveInputFrame<GoToFileListEntry> p) {
          if (p.getItem() != null) {
            final OpenDefinitionsDocument newDoc = p.getItem().doc;
            final boolean docChanged = ! newDoc.equals(_model.getActiveDocument());
            final boolean docSwitch = _model.getActiveDocument() != newDoc;
            if (docSwitch) _model.setActiveDocument(newDoc);
            final int curLine = newDoc.getCurrentLine();
            final String t = p.getText();
            final int last = t.lastIndexOf(':');
            if (last >= 0) {
              try {
                String end = t.substring(last + 1);
                int val = Integer.parseInt(end);
                
                final int lineNum = Math.max(1, val);
                Runnable command = new Runnable() {
                  public void run() {
                    try { _jumpToLine(lineNum); }  // adds this region to browser history
                    catch (RuntimeException e) { _jumpToLine(curLine); }
                  }
                };
                if (docSwitch) {
                  // postpone running command until after document switch, which is pending in the event queue
                  EventQueue.invokeLater(command);
                }
                else command.run();
              }
              catch(RuntimeException e) { /* ignore */ }
            }
            else if (docChanged) {
              // defer executing this code until after active document switch (if any) is complete
              EventQueue.invokeLater(new Runnable() {
                public void run() { addToBrowserHistory();
                }
              });
            }
          }
          hourglassOff();
          return null;
        }
      };
      PredictiveInputFrame.CloseAction<GoToFileListEntry> cancelAction = 
        new PredictiveInputFrame.CloseAction<GoToFileListEntry>() {
        public String getName() { return "Cancel"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
        public Object apply(PredictiveInputFrame<GoToFileListEntry> p) {
          hourglassOff();
          return null;
        }
      };
      java.util.ArrayList<PredictiveInputModel.MatchingStrategy<GoToFileListEntry>> strategies =
        new java.util.ArrayList<PredictiveInputModel.MatchingStrategy<GoToFileListEntry>>();
      strategies.add(new PredictiveInputModel.FragmentLineNumStrategy<GoToFileListEntry>());
      strategies.add(new PredictiveInputModel.PrefixLineNumStrategy<GoToFileListEntry>());
      strategies.add(new PredictiveInputModel.RegExLineNumStrategy<GoToFileListEntry>());
      List<PredictiveInputFrame.CloseAction<GoToFileListEntry>> actions
        = new ArrayList<PredictiveInputFrame.CloseAction<GoToFileListEntry>>();
      actions.add(okAction);
      actions.add(cancelAction);
      _gotoFileDialog = 
        new PredictiveInputFrame<GoToFileListEntry>(MainFrame.this,
                                                    "Go to File",
                                                    true, // force
                                                    true, // ignore case
                                                    info,
                                                    strategies,
                                                    actions, 1, // cancel is action 1
                                                    new GoToFileListEntry(null, "dummyGoto")) {
        public void setOwnerEnabled(boolean b) {
          if (b) { hourglassOff(); } else { hourglassOn(); }
        }
      }; 
      // putting one dummy entry in the list; it will be changed later anyway
      
      if (DrJava.getConfig().getSetting(DIALOG_GOTOFILE_STORE_POSITION).booleanValue()) {
        _gotoFileDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_GOTOFILE_STATE));
      }      
    }
  }
  
  /** The "Go to File" dialog instance. */
  volatile PredictiveInputFrame<GoToFileListEntry> _gotoFileDialog = null;
  
  /** Asks the user for a file name and goes there. */
  private final Action _gotoFileAction = new AbstractAction("Go to File...") {
    public void actionPerformed(ActionEvent ae) {
      initGotoFileDialog();
      List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
      if (docs == null || docs.size() == 0) {
        return; // do nothing
      }
      GoToFileListEntry currentEntry = null;
      ArrayList<GoToFileListEntry> list;
      if (DrJava.getConfig().getSetting(DIALOG_GOTOFILE_FULLY_QUALIFIED).booleanValue()) {
        list = new ArrayList<GoToFileListEntry>(2 * docs.size());
      }
      else {
        list = new ArrayList<GoToFileListEntry>(docs.size());
      }
      for(OpenDefinitionsDocument d: docs) {
        GoToFileListEntry entry = new GoToFileListEntry(d, d.toString());
        if (d.equals(_model.getActiveDocument())) {
          currentEntry = entry;
        }
        list.add(entry);
        if (DrJava.getConfig().getSetting(DIALOG_GOTOFILE_FULLY_QUALIFIED).booleanValue()) {
          try {
            try {
              File relative = FileOps.makeRelativeTo(d.getFile(), d.getSourceRoot());
              if (! relative.toString().equals(d.toString())) {
                list.add(new GoToFileListEntry(d, d.getPackageName() + "." + d.toString()));
              }
            }
            catch(IOException e) { /* ignore */ }
            catch(edu.rice.cs.drjava.model.definitions.InvalidPackageException e) { /* ignore */ }
          }
          catch(IllegalStateException e) { /* ignore */ }
        }
      }
      _gotoFileDialog.setItems(true, list); // ignore case
      if (currentEntry != null) _gotoFileDialog.setCurrentItem(currentEntry);
      hourglassOn();
      /* if (!  Utilities.TEST_MODE) */ 
      _gotoFileDialog.setVisible(true);
    }
  };
  
  /** Goes to the file specified by the word the cursor is on. */
  void _gotoFileUnderCursor() {
//    Utilities.show("Calling gotoFileUnderCursor()");
    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
    if ((docs == null) || (docs.size() == 0)) return; // do nothing
    
    GoToFileListEntry currentEntry = null;
    ArrayList<GoToFileListEntry> list;
    list = new ArrayList<GoToFileListEntry>(docs.size());
    for(OpenDefinitionsDocument d: docs) {
      GoToFileListEntry entry = new GoToFileListEntry(d, d.toString());
      if (d.equals(_model.getActiveDocument())) currentEntry = entry;
      list.add(entry);
    }
    
    PredictiveInputModel<GoToFileListEntry> pim =
      new PredictiveInputModel<GoToFileListEntry>(true, new PrefixStrategy<GoToFileListEntry>(), list);
    OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
    odd.acquireReadLock();
    String mask = "";
    try {
      int loc = getCurrentDefPane().getCaretPosition();
      String s = odd.getText();
      // find start
      int start = loc;
      while(start>0) {
        if (! Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
        --start;
      }
      while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start<loc)) {
        ++start;
      }
      // find end
      int end = loc-1;
      while(end<s.length()-1) {
        if (! Character.isJavaIdentifierPart(s.charAt(end+1))) { break; }
        ++end;
      }
      if ((start>=0) && (end<s.length())) {
        mask = s.substring(start, end + 1);
        pim.setMask(mask);
      }
    }
    finally { odd.releaseReadLock(); }
    
//    Utilities.show("Matching items are: " + pim.getMatchingItems());
    
    if (pim.getMatchingItems().size() == 1) {
      // exactly one match, go to file
      if (pim.getCurrentItem() != null) {
        boolean docChanged = !pim.getCurrentItem().doc.equals(_model.getActiveDocument());
//        if (docChanged) { addToBrowserHistory(); }
        _model.setActiveDocument(pim.getCurrentItem().doc);
        if (docChanged) { // defer executing this code until after active document switch is complete
            EventQueue.invokeLater(new Runnable() { public void run() { addToBrowserHistory(); } });
        }
      }
    }
    else {
      // try appending ".java" and see if it's unique
      pim.extendMask(".java");
      if (pim.getMatchingItems().size() == 1) {
        // exactly one match with ".java" appended, go to file
        if (pim.getCurrentItem() != null) {
          boolean docChanged = !pim.getCurrentItem().doc.equals(_model.getActiveDocument());
//          if (docChanged) { addToBrowserHistory(); }
          _model.setActiveDocument(pim.getCurrentItem().doc);
          if (docChanged) { // defer executing this code until after active document switch is complete
              EventQueue.invokeLater(new Runnable() { public void run() { addToBrowserHistory(); } });
          }
        }
      }
      else {
        // not exactly one match
        pim.setMask(mask);
        if (pim.getMatchingItems().size() == 0) {
          // if there are no matches, shorten the mask until there is at least one
          mask = pim.getMask();
          while (mask.length()>0) {
            mask = mask.substring(0, mask.length() - 1);
            pim.setMask(mask);
            if (pim.getMatchingItems().size()>0) { break; }
          }
        }       
        initGotoFileDialog();
        _gotoFileDialog.setModel(true, pim); // ignore case
        if (currentEntry != null) _gotoFileDialog.setCurrentItem(currentEntry);
        hourglassOn();
        /* The following predicate suppresses the display of the dialog during unit testing.  If the unit test is revised
         * to confirm that the dialog is displayed, this test must be removed. */
        if (MainFrame.this.isVisible()) _gotoFileDialog.setVisible(true);  // predicate suppresses display in unit tests
      }
    }
  }
  
  /** Goes to the file specified by the word the cursor is on. */
  final Action gotoFileUnderCursorAction = new AbstractAction("Go to File Under Cursor") {
    public void actionPerformed(ActionEvent ae) { _gotoFileUnderCursor(); }
  };
  
  
  /** Wrapper class for the "Open Javadoc" and "Auto Import" dialog list entries.
    * Provides the ability to have the same class name in there multiple times in different packages.
    */
  private static class JavaAPIListEntry extends ClassNameAndPackageEntry {
    private final String str, fullStr;
    private final URL url;
    public JavaAPIListEntry(String s, String full, URL u) {
      str = s;
      fullStr = full;
      url = u;
    }
    public String toString() { return str; }
    public String getFullString() { return fullStr; }
    public URL getURL() { return url; }
    public String getClassName() { return str; }
    public String getFullPackage() {
      int pos = fullStr.lastIndexOf('.');
      if (pos>=0) { return fullStr.substring(0,pos+1); }
      return "";
    }
  }  
  
  /** Reset the position of the "Open Javadoc" dialog. */
  public void resetOpenJavadocDialogPosition() {
    initOpenJavadocDialog();
    _openJavadocDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_OPENJAVADOC_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_OPENJAVADOC_STATE, "default");
    }
  }
  
  /** Initialize dialog if necessary. */
  void initOpenJavadocDialog() {
    if (_openJavadocDialog == null) {
      PredictiveInputFrame.InfoSupplier<JavaAPIListEntry> info = 
        new PredictiveInputFrame.InfoSupplier<JavaAPIListEntry>() {
        public String apply(JavaAPIListEntry entry) {
          return entry.getFullString();
        }
      };
      PredictiveInputFrame.CloseAction<JavaAPIListEntry> okAction = 
        new PredictiveInputFrame.CloseAction<JavaAPIListEntry>() {
        public String getName() { return "OK"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); }
        public Object apply(PredictiveInputFrame<JavaAPIListEntry> p) {
          if (p.getItem() != null) {
            PlatformFactory.ONLY.openURL(p.getItem().getURL());
          }
          hourglassOff();
          return null;
        }
      };
      PredictiveInputFrame.CloseAction<JavaAPIListEntry> cancelAction = 
        new PredictiveInputFrame.CloseAction<JavaAPIListEntry>() {
        public String getName() { return "Cancel"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
        public Object apply(PredictiveInputFrame<JavaAPIListEntry> p) {
          hourglassOff();
          return null;
        }
      };
      // Note: PredictiveInputModel.* is statically imported
      java.util.ArrayList<MatchingStrategy<JavaAPIListEntry>> strategies =
        new java.util.ArrayList<MatchingStrategy<JavaAPIListEntry>>();
      strategies.add(new FragmentStrategy<JavaAPIListEntry>());
      strategies.add(new PrefixStrategy<JavaAPIListEntry>());
      strategies.add(new RegExStrategy<JavaAPIListEntry>());
      List<PredictiveInputFrame.CloseAction<JavaAPIListEntry>> actions
        = new ArrayList<PredictiveInputFrame.CloseAction<JavaAPIListEntry>>();
      actions.add(okAction);
      actions.add(cancelAction);
      _openJavadocDialog = 
        new PredictiveInputFrame<JavaAPIListEntry>(MainFrame.this,
                                                   "Open Java API Javadoc Webpage",
                                                   true, // force
                                                   true, // ignore case
                                                   info,
                                                   strategies,
                                                   actions, 1, // cancel is action 1
                                                   new JavaAPIListEntry("dummyJavadoc", "dummyJavadoc", null)) {
        public void setOwnerEnabled(boolean b) {
          if (b) { hourglassOff(); } else { hourglassOn(); }
        }
      }; 
      // putting one dummy entry in the list; it will be changed later anyway
      
      if (DrJava.getConfig().getSetting(DIALOG_OPENJAVADOC_STORE_POSITION).booleanValue()) {
        _openJavadocDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_OPENJAVADOC_STATE));
      }
      generateJavaAPIList();
    }
  }
  
  /** Generate Java API class list. */
  public void generateJavaAPIList() {
    if (_javaAPIList == null) {
      // generate list
      String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
      String base = "";
      String suffix = "";
      if (linkVersion.equals(JAVADOC_AUTO_TEXT)) {
        // use the compiler's version of the Java API Javadoc
        edu.rice.cs.plt.reflect.JavaVersion ver = _model.getCompilerModel().getActiveCompiler().version();
        if (ver==edu.rice.cs.plt.reflect.JavaVersion.JAVA_1_4) {
          linkVersion = JAVADOC_1_4_TEXT;
        }
        else if (ver==edu.rice.cs.plt.reflect.JavaVersion.JAVA_5) {
          linkVersion = JAVADOC_1_5_TEXT;
        }
        else if (ver==edu.rice.cs.plt.reflect.JavaVersion.JAVA_6) {
          linkVersion = JAVADOC_1_6_TEXT;
        }
        else {
          linkVersion = JAVADOC_1_3_TEXT;
        }
      }
      if (linkVersion.equals(JAVADOC_1_3_TEXT)) {
        base = DrJava.getConfig().getSetting(JAVADOC_1_3_LINK) + "/";
        suffix = "/allclasses-1.3.html";
      }
      else if (linkVersion.equals(JAVADOC_1_4_TEXT)) {
        base = DrJava.getConfig().getSetting(JAVADOC_1_4_LINK) + "/";
        suffix = "/allclasses-1.4.html";
      }
      else if (linkVersion.equals(JAVADOC_1_5_TEXT)) {
        base = DrJava.getConfig().getSetting(JAVADOC_1_5_LINK) + "/";
        suffix = "/allclasses-1.5.html";
      }
      else if (linkVersion.equals(JAVADOC_1_6_TEXT)) {
        base = "";
        suffix = "/allclasses-1.6.html";
      }
      else {
        // no valid Javadoc URL
        return;
      }
      // TODO: put this in an AsyncTask
      try {
        _javaAPIList = new ArrayList<JavaAPIListEntry>();
        URL url = MainFrame.class.getResource("/edu/rice/cs/drjava/docs/javaapi"+suffix);
        InputStream urls = url.openStream();
        InputStreamReader is = null;
        BufferedReader br = null;
        try {
          is = new InputStreamReader(urls);
          br = new BufferedReader(is);
          String line = br.readLine();
          while(line != null) {
            final String aText = "<a href=\"";
            int aPos = line.toLowerCase().indexOf(aText);
            int aEndPos = line.toLowerCase().indexOf(".html\" ",aPos);
            if ((aPos>=0) && (aEndPos>=0)) {
              String link = line.substring(aPos+aText.length(), aEndPos);
              String fullClassName = link.replace('/', '.');
              String simpleClassName = fullClassName;
              int lastDot = fullClassName.lastIndexOf('.');
              if (lastDot>=0) { simpleClassName = fullClassName.substring(lastDot + 1); }
              try {
                URL pageURL = new URL(base + link + ".html");
                _javaAPIList.add(new JavaAPIListEntry(simpleClassName, fullClassName, pageURL));
              }
              catch(MalformedURLException mue) { /* ignore, we'll just not put this class in the list */ }
            }
            line = br.readLine();
          }
        }
        finally {
          if (br!=null) { br.close(); }
          if (is!=null) { is.close(); }
          if (urls!=null) { urls.close(); }
        }
      }
      catch(IOException ioe) { /* ignore, we'll just have an incomplete list */ }
      if (_javaAPIList.size()==0) { _javaAPIList = null; }
    }
  }
  
  /** The "Open Javadoc" dialog instance. */
  PredictiveInputFrame<JavaAPIListEntry> _openJavadocDialog = null;
  
  /** The list of Java API classes. */
  List<JavaAPIListEntry> _javaAPIList = null;
  
  /** Asks the user for a file name and goes there. */
  private Action _openJavadocAction = new AbstractAction("Open Java API Javadoc...") {
    public void actionPerformed(ActionEvent ae) {
      initOpenJavadocDialog();     
      _openJavadocDialog.setItems(true, _javaAPIList); // ignore case
      hourglassOn();
      _openJavadocDialog.setVisible(true);
    }
  };
  
  /** Opens the Javadoc specified by the word the cursor is on. */
  void _openJavadocUnderCursor() {
    generateJavaAPIList();
    if (_javaAPIList == null) {
//      Utilities.show("Cannot load Java API class list. No network connectivity?");
      return;
    }
    PredictiveInputModel<JavaAPIListEntry> pim =
      new PredictiveInputModel<JavaAPIListEntry>(true, new PrefixStrategy<JavaAPIListEntry>(), _javaAPIList);
    OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
    odd.acquireReadLock();
    String mask = "";
    try {
      int loc = getCurrentDefPane().getCaretPosition();
      String s = odd.getText();
      // find start
      int start = loc;
      while(start>0) {
        if (!Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
        --start;
      }
      while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start<loc)) {
        ++start;
      }
      // find end
      int end = loc-1;
      while(end<s.length()-1) {
        if (!Character.isJavaIdentifierPart(s.charAt(end+1))) { break; }
        ++end;
      }
      if ((start>=0) && (end<s.length())) {
        mask = s.substring(start, end + 1);
        pim.setMask(mask);
      }
    }
    finally { odd.releaseReadLock(); }
    
//    Utilities.show("Matching items are: " + pim.getMatchingItems());
    
    if (pim.getMatchingItems().size() == 1) {
      // exactly one match, go to file
      if (pim.getCurrentItem() != null) {
        PlatformFactory.ONLY.openURL(pim.getCurrentItem().getURL());
      }
    }
    else {
      // try appending ".java" and see if it's unique
      pim.extendMask(".java");
      if (pim.getMatchingItems().size() == 1) {
        // exactly one match with ".java" appended, go to file
        if (pim.getCurrentItem() != null) {
          PlatformFactory.ONLY.openURL(pim.getCurrentItem().getURL());
        }
      }
      else {
        // not exactly one match
        pim.setMask(mask);
        JavaAPIListEntry foundItem = null;
        int found = 0;
        if (pim.getMatchingItems().size() == 0) {
          // if there are no matches, shorten the mask until there is at least one
          mask = pim.getMask();
          while(mask.length()>0) {
            mask = mask.substring(0, mask.length() - 1);
            pim.setMask(mask);
            if (pim.getMatchingItems().size() > 0) { break; }
          }
        }
        else {
          // there are several matches, see if there is an exact match
          for(JavaAPIListEntry e: pim.getMatchingItems()) {
            if (e.toString().equalsIgnoreCase(mask)) {
              ++found;
              foundItem = e;
            }
          }
        }
        if (found==1) {
          // open unique item and return
          PlatformFactory.ONLY.openURL(pim.getCurrentItem().getURL());
        }
        else {
          initOpenJavadocDialog();
          _openJavadocDialog.setModel(true, pim); // ignore case
          hourglassOn();
          _openJavadocDialog.setVisible(true);
        }
      }
    }
  }
  
  /** Open Javadoc page specified by the word the cursor is on. */
  final Action _openJavadocUnderCursorAction = new AbstractAction("Open Java API Javadoc for Word Under Cursor...") {
    public void actionPerformed(ActionEvent ae) {
      _openJavadocUnderCursor();
    }
  };
  
  /** Reset the position of the "Complete Word" dialog. */
  public void resetCompleteWordDialogPosition() {
    initCompleteWordDialog();
    _completeWordDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_COMPLETE_WORD_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_COMPLETE_WORD_STATE, "default");
    }
  }
  
  /** Initialize dialog if necessary. */
  void initCompleteWordDialog() {
    if (_completeWordDialog == null) {
      // checkbox whether Java API classes should be completed as well
      _completeJavaAPICheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String curMask = _completeWordDialog.getMask();
          if (_completeJavaAPICheckbox.isSelected()) {
            DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.TRUE);
            List<ClassNameAndPackageEntry> l = _completeWordDialog.getList();
            addJavaAPIToList(l);
            _completeWordDialog.setItems(true,l);
          }
          else {
            // unselected, remove Java API classes from list
            List<ClassNameAndPackageEntry> l = _completeWordDialog.getList();
            generateJavaAPIList();
            if (_javaAPIList==null) {
              DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
              _completeJavaAPICheckbox.setSelected(false);
              _completeJavaAPICheckbox.setEnabled(false);
              List<ClassNameAndPackageEntry> n = new ArrayList<ClassNameAndPackageEntry>();
              for(ClassNameAndPackageEntry entry: l) {
                if (!(entry instanceof JavaAPIListEntry)) { n.add(entry); }
              }
              _completeWordDialog.setItems(true,n);
            }
            else {
              for(JavaAPIListEntry entry: _javaAPIList) { l.remove(entry); }
              _completeWordDialog.setItems(true,l);
            }
          }
          _completeWordDialog.setMask(curMask);
          _completeWordDialog.resetFocus();
        }
      });
      _completeJavaAPICheckbox.setMnemonic('j');
      PredictiveInputFrame.InfoSupplier<ClassNameAndPackageEntry> info = 
        new PredictiveInputFrame.InfoSupplier<ClassNameAndPackageEntry>() {
        public String apply(ClassNameAndPackageEntry entry) {
          // show full class name as information
          StringBuilder sb = new StringBuilder();
          sb.append(entry.getFullPackage());
          sb.append(entry.getClassName());
          return sb.toString();
        }
      };
      PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry> okAction =
        new PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>() {
        public String getName() { return "OK"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); }
        public Object apply(PredictiveInputFrame<ClassNameAndPackageEntry> p) {
          if (p.getItem() != null) {
            OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
            try {
              String mask = "";
              int loc = getCurrentDefPane().getCaretPosition();
              String s = odd.getText(0, loc);
              
              // check that we're at the end of a word
              if ((loc<s.length()) && (!Character.isWhitespace(s.charAt(loc))) &&
                  ("()[]{}<>.,:;/*+-!~&|%".indexOf(s.charAt(loc)) == -1)) return null;
              
              // find start
              int start = loc;
              while(start>0) {
                if (!Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
                --start;
              }
              while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start < loc)) {
                ++start;
              }
              
              if (!s.substring(start, loc).equals(p.getItem().toString())) {
                odd.remove(start, loc-start);
                odd.insertString(start, p.getItem().getClassName(), null);
              }
            }
            catch(BadLocationException ble) { /* ignore, just don't auto-complete */ }
            finally { odd.releaseWriteLock(); }
          }
          hourglassOff();
          return null;
        }
      };
      PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry> fullAction =
        new PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>() {
        public String getName() { return "Fully Qualified"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, OptionConstants.MASK); }
        public Object apply(PredictiveInputFrame<ClassNameAndPackageEntry> p) {
          if (p.getItem() != null) {
            OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
            try {
              String mask = "";
              int loc = getCurrentDefPane().getCaretPosition();
              String s = odd.getText(0, loc);
              
              // check that we're at the end of a word
              if ((loc<s.length()) && (!Character.isWhitespace(s.charAt(loc))) &&
                  ("()[]{}<>.,:;/*+-!~&|%".indexOf(s.charAt(loc)) == -1)) return null;
              
              // find start
              int start = loc;
              while(start>0) {
                if (!Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
                --start;
              }
              while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start < loc)) {
                ++start;
              }
              
              if (!s.substring(start, loc).equals(p.getItem().toString())) {
                odd.remove(start, loc-start);
                StringBuilder sb = new StringBuilder();
                sb.append(p.getItem().getFullPackage());
                sb.append(p.getItem().getClassName());
                odd.insertString(start, sb.toString(), null);
              }
            }
            catch(BadLocationException ble) { /* ignore, just don't auto-complete */ }
            finally { odd.releaseWriteLock(); }
          }
          hourglassOff();
          return null;
        }
      };
      PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry> cancelAction = 
        new PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>() {
        public String getName() { return "Cancel"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
        public Object apply(PredictiveInputFrame<ClassNameAndPackageEntry> p) {
          hourglassOff();
          return null;
        }
      };
      // Note: PredictiveInputModel.* is statically imported
      java.util.ArrayList<MatchingStrategy<ClassNameAndPackageEntry>> strategies =
        new java.util.ArrayList<MatchingStrategy<ClassNameAndPackageEntry>>();
      strategies.add(new FragmentStrategy<ClassNameAndPackageEntry>());
      strategies.add(new PrefixStrategy<ClassNameAndPackageEntry>());
      strategies.add(new RegExStrategy<ClassNameAndPackageEntry>());
      List<PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>> actions
        = new ArrayList<PredictiveInputFrame.CloseAction<ClassNameAndPackageEntry>>();
      actions.add(okAction);
      actions.add(fullAction);
      actions.add(cancelAction);
      _completeWordDialog = 
        new PredictiveInputFrame<ClassNameAndPackageEntry>(MainFrame.this,
                                                           "Auto-Complete Word",
                                                           true, // force
                                                           true, // ignore case
                                                           info,
                                                           strategies,
                                                           actions, 2, // cancel is action 23
                                                           new GoToFileListEntry(new DummyOpenDefDoc() {
        public String getPackageNameFromDocument() { return ""; }
      }, "dummyComplete")) {
        public void setOwnerEnabled(boolean b) {
          if (b) { hourglassOff(); } else { hourglassOn(); }
        }
        protected JComponent[] makeOptions() {
          return new JComponent[] { _completeJavaAPICheckbox };
        }
      }; 
      // putting one dummy entry in the list; it will be changed later anyway
      
      if (DrJava.getConfig().getSetting(DIALOG_COMPLETE_WORD_STORE_POSITION).booleanValue()) {
        _completeWordDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_COMPLETE_WORD_STATE));
      }      
    }
  }
  
  void addJavaAPIToList(List<ClassNameAndPackageEntry> list) {
    generateJavaAPIList();
    if (_javaAPIList==null) {
      DrJava.getConfig().setSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI, Boolean.FALSE);
      _completeJavaAPICheckbox.setSelected(false);
      _completeJavaAPICheckbox.setEnabled(false);
    }
    else {
      for(JavaAPIListEntry entry: _javaAPIList) { list.add(entry); }
    }
  }
  
  /** The "Complete File" dialog instance. */
  volatile PredictiveInputFrame<GoToFileListEntry> _completeFileDialog = null;
  /** The "Complete Word" dialog instance. */
  volatile PredictiveInputFrame<ClassNameAndPackageEntry> _completeWordDialog = null;
  JCheckBox _completeJavaAPICheckbox = new JCheckBox("Java API");
  
  /** Complete the word the cursor is on. */
  void _completeWordUnderCursor() {
    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
    if ((docs == null) || (docs.size() == 0)) return; // do nothing
    
    _completeJavaAPICheckbox.setSelected(DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI));
    _completeJavaAPICheckbox.setEnabled(true);
    ClassNameAndPackageEntry currentEntry = null;
    ArrayList<ClassNameAndPackageEntry> list;
    if ((DrJava.getConfig().getSetting(DIALOG_COMPLETE_SCAN_CLASS_FILES).booleanValue()) &&
        (_completeClassList.size()>0)) {
      list = new ArrayList<ClassNameAndPackageEntry>(_completeClassList.size());
      list.addAll(_completeClassList);
    }
    else {
      list = new ArrayList<ClassNameAndPackageEntry>(docs.size());
      for(OpenDefinitionsDocument d: docs) {
        if (d.isUntitled()) continue;
        String str = d.toString();
        if (str.lastIndexOf('.')>=0) {
          str = str.substring(0, str.lastIndexOf('.'));
        }
        GoToFileListEntry entry = new GoToFileListEntry(d, str);
        if (d.equals(_model.getActiveDocument())) currentEntry = entry;
        list.add(entry);
      }
    }
    
    if (DrJava.getConfig().getSetting(OptionConstants.DIALOG_COMPLETE_JAVAAPI)) {
      addJavaAPIToList(list);
    }
    
    
    PredictiveInputModel<ClassNameAndPackageEntry> pim = 
      new PredictiveInputModel<ClassNameAndPackageEntry>(true, new PrefixStrategy<ClassNameAndPackageEntry>(), list);
    OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
    odd.acquireWriteLock();
    boolean uniqueMatch = true;
    try {
      String mask = "";
      int loc = getCurrentDefPane().getCaretPosition();
      String s = odd.getText(0, loc);
      
      // check that we're at the end of a word
      if ((loc<s.length()) && (!Character.isWhitespace(s.charAt(loc))) &&
          ("()[]{}<>.,:;/*+-!~&|%".indexOf(s.charAt(loc)) == -1)) return;
      
      // find start
      int start = loc;
      while(start > 0) {
        if (!Character.isJavaIdentifierPart(s.charAt(start-1))) { break; }
        --start;
      }
      while((start<s.length()) && (!Character.isJavaIdentifierStart(s.charAt(start))) && (start < loc)) {
        ++start;
      }
      
      int end = loc-1;
      
      if ((start>=0) && (end < s.length())) {
        mask = s.substring(start, end + 1);
        pim.setMask(mask);
      }
      
      if (pim.getMatchingItems().size() == 1) {
        if (pim.getCurrentItem() != null) {
          // exactly one match, auto-complete
          if (! s.substring(start, loc).equals(pim.getCurrentItem().toString())) {
            odd.remove(start, loc - start);
            odd.insertString(start, pim.getCurrentItem().toString(), null);
          }
          return;
        }
      }
      else {
        // not exactly one match
        uniqueMatch = false;
        pim.setMask(mask);
        if (pim.getMatchingItems().size() == 0) {
          // if there are no matches, shorten the mask until there is at least one
          mask = pim.getMask();
          while(mask.length() > 0) {
            mask = mask.substring(0, mask.length() - 1);
            pim.setMask(mask);
            if (pim.getMatchingItems().size() > 0) { break; }
          }
        }       
        initCompleteWordDialog();
        _completeWordDialog.setModel(true, pim); // ignore case
        _completeWordDialog.selectStrategy();
        if (currentEntry != null) _completeWordDialog.setCurrentItem(currentEntry);
        hourglassOn();
        _completeWordDialog.setVisible(true);
      }
    }
    catch(BadLocationException ble) { /* ignore, just don't auto-complete */ }
    finally { 
      if (uniqueMatch) { odd.releaseWriteLock(); }
    }
  }
  
  /** Auto-completes word the cursor is on. */
  final Action completeWordUnderCursorAction = new AbstractAction("Auto-Complete Word Under Cursor") {
    public void actionPerformed(ActionEvent ae) {
      _completeWordUnderCursor();
    }
  };
  
  /** Indents the current selection. */
  private final Action _indentLinesAction = new AbstractAction("Indent Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      hourglassOn();
      try {
        _currentDefPane.endCompoundEdit();
        _currentDefPane.indent();
      } finally {
        hourglassOff();
      }
    }
  };
  
  /** Action for commenting out a block of text using wing comments. */
  private final Action _commentLinesAction = new AbstractAction("Comment Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      hourglassOn();
      try{ commentLines(); }
      finally{ hourglassOff(); }
    }
  };
  
  /** Action for un-commenting a block of commented text. */
  private final Action _uncommentLinesAction = new AbstractAction("Uncomment Line(s)") {
    public void actionPerformed(ActionEvent ae){
      hourglassOn();
      try{ uncommentLines(); }
      finally{ hourglassOff(); }
    }
  };
  
  /** Clears DrJava's output console. */
  private final Action _clearConsoleAction = new AbstractAction("Clear Console") {
    public void actionPerformed(ActionEvent ae) { _model.resetConsole(); }
  };
  
  /** Shows the DebugConsole. */
  private final Action _showDebugConsoleAction = new AbstractAction("Show DrJava Debug Console") {
    public void actionPerformed(ActionEvent e) { DrJavaRoot.showDrJavaDebugConsole(MainFrame.this); }
  };
  
  /* Enables the reset interactions command. Not currently used, since this action is NEVER disabled. */
  public void enableResetInteractions() { _resetInteractionsAction.setEnabled(true); }
  
  /** Resets the Interactions pane. */
  private final Action _resetInteractionsAction = new AbstractAction("Reset Interactions") {
    public void actionPerformed(ActionEvent ae) {
      if (! DrJava.getConfig().getSetting(INTERACTIONS_RESET_PROMPT).booleanValue()) {
        _doResetInteractions();
        return;
      }
      
      String title = "Confirm Reset Interactions";
      String message = "Are you sure you want to reset the Interactions Pane?";
      ConfirmCheckBoxDialog dialog =
        new ConfirmCheckBoxDialog(MainFrame.this, title, message);
      int rc = dialog.show();
      if (rc == JOptionPane.YES_OPTION) {
        _doResetInteractions();
        
        if (dialog.getCheckBoxValue()) {
          DrJava.getConfig().setSetting(INTERACTIONS_RESET_PROMPT, Boolean.FALSE);
        }
      }
    }
  };
  
  private void _doResetInteractions() {
    _tabbedPane.setSelectedIndex(INTERACTIONS_TAB);
    updateStatusField("Resetting Interactions");
    
    _model.getInteractionsModel().enableRestart();
    // Lots of work, so use another thread
    new Thread(new Runnable() { 
      public void run() {_model.resetInteractions(_model.getWorkingDirectory(), true);
      }
    }).start();
  }
  
  /** Defines actions that displays the interactions classpath. */
  private final Action _viewInteractionsClassPathAction = new AbstractAction("View Interactions Classpath...") {
    public void actionPerformed(ActionEvent e) { viewInteractionsClassPath(); }
  };
  
  /** Displays the interactions classpath. */  
  public void viewInteractionsClassPath() {
    String cp = IterUtil.multilineToString(_model.getInteractionsClassPath());
    new DrJavaScrollableDialog(this, "Interactions Classpath", "Current Interpreter Classpath", cp).show();
  }
  
  /** Shows the user documentation. */
  private final Action _helpAction = new AbstractAction("Help") {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
//      if (_helpFrame == null) {
//        _helpFrame = new HelpFrame();
//      }
      _helpFrame.setVisible(true);
    }
  };
  
  /** Shows the quick start documentation. */
  private final Action _quickStartAction = new AbstractAction("QuickStart") {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
//      if (_quickStartFrame == null) {
//        _quickStartFrame = new QuickStartFrame();
//      }
      _quickStartFrame.setVisible(true);
    }
  };
  
  /** Pops up an info dialog. */
  private final Action _aboutAction = new AbstractAction("About") {
    public void actionPerformed(ActionEvent ae) {
      // Create dialog if we haven't yet
//      if (_aboutDialog == null) _aboutDialog = new AboutDialog(MainFrame.this);
      Point p = MainFrame.this.getLocation();
      _aboutDialog.setVisible(true);
//      _aboutDialog.setLocation(p.x+(MainFrame.this.getWidth() - _aboutDialog.getWidth())/2, 
//      p.y+(MainFrame.this.getHeight()-_aboutDialog.getHeight())/2);
      
    }
  };
  
  /** Pops up a dialog that checks for a new version. */
  private final Action _checkNewVersionAction = new AbstractAction("Check for New Version") {
    public void actionPerformed(ActionEvent ae) {
      NewVersionPopup popup = new NewVersionPopup(MainFrame.this);
      popup.setVisible(true);
    }
  };
  
  /** Pops up the DrJava errors dialog. */
  private final Action _errorsAction = new AbstractAction("DrJava Errors") {
    public void actionPerformed(ActionEvent ae) {
      setPopupLoc(DrJavaErrorWindow.singleton());
      DrJavaErrorWindow.singleton().setVisible(true);
    }
  };
  
  /** Switches to next document. */
  private final Action _switchToNextAction = new AbstractAction("Next Document") {
    public void actionPerformed(ActionEvent ae) {
      this.setEnabled(false);
      if (_docSplitPane.getDividerLocation() < _docSplitPane.getMinimumDividerLocation())
        _docSplitPane.setDividerLocation(DrJava.getConfig().getSetting(DOC_LIST_WIDTH).intValue());
      //disables switching documents while the next one is opening up, in order to prevent out of control switching
      _model.setActiveNextDocument();
      _findReplace.updateFirstDocInSearch();
      this.setEnabled(true);
      // defer executing this code until after active document switch (if any) is complete
      EventQueue.invokeLater(new Runnable() { public void run() { addToBrowserHistory(); } });
    }
  };
  
  /** Switches to previous document. */
  private final Action _switchToPrevAction = new AbstractAction("Previous Document") {
    public void actionPerformed(ActionEvent ae) {
      this.setEnabled(false);
      if (_docSplitPane.getDividerLocation() < _docSplitPane.getMinimumDividerLocation())
        _docSplitPane.setDividerLocation(DrJava.getConfig().getSetting(DOC_LIST_WIDTH).intValue());
      _model.setActivePreviousDocument();
      _findReplace.updateFirstDocInSearch();
      this.setEnabled(true);
      // defer executing this code until after active document switch (if any) is complete
      EventQueue.invokeLater(new Runnable() { public void run() { addToBrowserHistory(); } });
    }
  };
  
  /** Switches focus to next pane. */
  private final Action _switchToNextPaneAction =  new AbstractAction("Next Pane") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes(); 
      this.setEnabled(false);
      _switchPaneFocus(true);
      this.setEnabled(true);
    }
  };
  
  /** Browse back in the browser history. */
  private final Action _browseBackAction = new AbstractAction("Browse Back") {
    public void actionPerformed(ActionEvent ae) {
      updateStatusField("Browsing Back");
      this.setEnabled(false);
      if (_docSplitPane.getDividerLocation() < _docSplitPane.getMinimumDividerLocation())
        _docSplitPane.setDividerLocation(DrJava.getConfig().getSetting(DOC_LIST_WIDTH).intValue());
      //disables switching documents while the next one is opening up, in order to prevent out of control switching
      
      BrowserHistoryManager rm = _model.getBrowserHistoryManager();
      
//      // add current location to history
//      addToBrowserHistory();
      
      // then move back    
      DocumentRegion r = rm.prevCurrentRegion(_model.getNotifier());
      if (r != null) scrollToDocumentAndOffset(r.getDocument(), r.getStartOffset(), false, false);
      _configureBrowsing();
//      this.setEnabled(true);
    }
  };
  
  /** Browse forward in the browser history. */
  private final Action _browseForwardAction = new AbstractAction("Browse Forward") {
    public void actionPerformed(ActionEvent ae) {
      updateStatusField("Browsing Forward");
      this.setEnabled(false);
      if (_docSplitPane.getDividerLocation() < _docSplitPane.getMinimumDividerLocation())
        _docSplitPane.setDividerLocation(DrJava.getConfig().getSetting(DOC_LIST_WIDTH).intValue());
      //disables switching documents while the next one is opening up, in order to prevent out of control switching
      
      BrowserHistoryManager rm = _model.getBrowserHistoryManager();
      
//      // add current location to history
//      addToBrowserHistory();
      
      // then move forward
      DocumentRegion r = rm.nextCurrentRegion(_model.getNotifier());
      if (r != null) scrollToDocumentAndOffset(r.getDocument(), r.getStartOffset(), false, false);
      _configureBrowsing();   
//      this.setEnabled(true);
    }
  };
  
  /** Switches focus to previous pane. */
  private final Action _switchToPreviousPaneAction =  new AbstractAction("Previous Pane") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes(); 
      this.setEnabled(false);
      _switchPaneFocus(false);
      this.setEnabled(true);
    }
  };
  
  /** Go to the closing brace.  ReadLock omitted because it only runs on definitions documents in the event thread. */
  private final Action _gotoClosingBraceAction =  new AbstractAction("Go to Closing Brace") {
    public void actionPerformed(ActionEvent ae) {
      OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
//        odd.acquireReadLock();
      try {
        int pos = odd.findNextEnclosingBrace(getCurrentDefPane().getCaretPosition(), '{', '}');
        if (pos != -1) { getCurrentDefPane().setCaretPosition(pos); }
      }
      catch(BadLocationException ble) { /* just ignore and don't move */ }
//        finally { odd.releaseReadLock(); }
    }
  };
  
  /** Go to the opening brace.  ReadLock omitted because it only runs on definitions documents in the event thread. */
  private final Action _gotoOpeningBraceAction =  new AbstractAction("Go to Opening Brace") {
    public void actionPerformed(ActionEvent ae) {
      OpenDefinitionsDocument odd = getCurrentDefPane().getOpenDefDocument();
//        odd.acquireReadLock();
      try {
        int pos = odd.findPrevEnclosingBrace(getCurrentDefPane().getCaretPosition(), '{', '}');
        if (pos != -1) { getCurrentDefPane().setCaretPosition(pos); }
      }
      catch(BadLocationException ble) { /* just ignore and don't move */ }
//        finally { odd.releaseReadLock(); }
    }
  };
  
  /** This takes a component and gives it focus, showing it if it's a tab. The interactionsPane and consolePane
    * are wrapped in scrollpanes, so we have to specifically check for those and unwrap them.
    * @param c the pane to switch focus to
    */
  private void _switchToPane(Component c) {
    Component newC = c;
//    if (c == _interactionsContainer) newC = _interactionsPane;
//    if (c == _consoleScroll) newC = _consolePane;
    showTab(newC);
  }
  
  /** This method allows the user to cycle through the definitions pane and all of the open tabs.
    * @param next true if we want to go to the next pane, false if the previous.
    */
  private void _switchPaneFocus(boolean next) {
    int numTabs = _tabbedPane.getTabCount();
    
    /* If next, then we go to the next tab */
    if (next) _switchToPane(_tabbedPane.getComponentAt((numTabs + _tabbedPane.getSelectedIndex() +1 ) % numTabs));
    else _switchToPane(_tabbedPane.getComponentAt((numTabs + _tabbedPane.getSelectedIndex() - 1) % numTabs));
  }
  
  /** Calls the ConfigFrame to edit preferences */
  
  private final Action _editPreferencesAction = new AbstractAction("Preferences ...") {
    
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
//      if (_configFrame == null) {
//        _configFrame = new ConfigFrame(MainFrame.this);
//      }
      _configFrame.setUp();
      setPopupLoc(_configFrame);
      _configFrame.setVisible(true);
      _configFrame.toFront();
    }
  };
  
  private volatile AbstractAction _projectPropertiesAction = new AbstractAction("Project Properties") {
    public void actionPerformed(ActionEvent ae) { _editProject(); }
  };
  
  /** Enables the debugger */
  private final Action _toggleDebuggerAction = new AbstractAction("Debug Mode") {
    public void actionPerformed(ActionEvent ae) { 
      this.setEnabled(false);
      debuggerToggle();
      this.setEnabled(true);
    }
  };
  
  /** Resumes debugging */
  private final Action _resumeDebugAction = new AbstractAction("Resume Debugger") {
    public void actionPerformed(ActionEvent ae) {
      try { debuggerResume(); }
      catch (DebugException de) { _showDebugError(de); }
    }
  };
  
  /** Steps into the next method call */
  private final Action _stepIntoDebugAction = new AbstractAction("Step Into") {
    public void actionPerformed(ActionEvent ae) { debuggerStep(Debugger.StepType.STEP_INTO); }
  };
  
  /** Runs the next line, without stepping into methods */
  private final Action _stepOverDebugAction = new AbstractAction("Step Over") {
    public void actionPerformed(ActionEvent ae) { debuggerStep(Debugger.StepType.STEP_OVER); }
  };
  
  /** Steps out of the next method call */
  private final Action _stepOutDebugAction = new AbstractAction("Step Out") {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.StepType.STEP_OUT);
    }
  };
  
  /** Suspend debugging */
  /*private Action _suspendDebugAction =
   new AbstractAction("Suspend Debugger")
   {
   public void actionPerformed(ActionEvent ae) {
   _debugSuspend();
   }
   };*/
  
  /** Toggles a breakpoint on the current line */
  final Action _toggleBreakpointAction = new AbstractAction("Toggle Breakpoint on Current Line") {
    public void actionPerformed(ActionEvent ae) { debuggerToggleBreakpoint(); }
  };
  
  /** Clears all breakpoints */
  private final Action _clearAllBreakpointsAction = new AbstractAction("Clear All Breakpoints") {
    public void actionPerformed(ActionEvent ae) { debuggerClearAllBreakpoints(); }
  };
  
  /** Shows the breakpoints tab. */
  private final Action _breakpointsPanelAction = new AbstractAction("Breakpoints") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes(); 
      if (! _breakpointsPanel.isDisplayed()) {
        showTab(_breakpointsPanel);
      }
      _breakpointsPanel.setVisible(true);
      _tabbedPane.setSelectedComponent(_breakpointsPanel);
      // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the _breakpointsPanel has been selected
      EventQueue.invokeLater(new Runnable() { public void run() { _breakpointsPanel.requestFocusInWindow(); } });
    }
  };
  
  /** Shows the bookmarks tab. */
  private final Action _bookmarksPanelAction = new AbstractAction("Bookmarks") {
    public void actionPerformed(ActionEvent ae) {
      if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
        _mainSplit.resetToPreferredSizes(); 
      if (! _bookmarksPanel.isDisplayed()) {
        showTab(_bookmarksPanel);
      }
      _bookmarksPanel.setVisible(true);
      _tabbedPane.setSelectedComponent(_bookmarksPanel);
      // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the _bookmarksPanel has been selected
      EventQueue.invokeLater(new Runnable() { public void run() { _bookmarksPanel.requestFocusInWindow(); } });
    }
  };
  
//  /** Selects the next recent document. */
//  private final Action _nextRecentDocumentAction = new Abstract Action("Select Next Recent Document") {
//    public void actionPerformed(ActionEvent ae) { nextRecentDoc(); }
//  };
//  
//  /** Selects the previous recent document. */
//  private final Action _prevRecentDocumentAction = new Abstract Action("Select Previous Recent Document") {
//    public void actionPerformed(ActionEvent ae) { prevRecentDoc(); }
//  };
  
  /** Toggles a bookmark. */
  private final Action _toggleBookmarkAction = new AbstractAction("Toggle Bookmark") {
    public void actionPerformed(ActionEvent ae) { toggleBookmark(); }
  };
  
  /** Toggle a bookmark. */
  public void toggleBookmark() {
    assert EventQueue.isDispatchThread();
    addToBrowserHistory();
    _model.toggleBookmark(_currentDefPane.getSelectionStart(), _currentDefPane.getSelectionEnd());
  }
  
  /** Add the current location to the browser history. */
  public void addToBrowserHistory() { _model.addToBrowserHistory(); }
  
  /** Create a new find results tab.
    * @param rm the region manager that will contain the regions
    * @param title the title for the panel
    * @return new find results tab.
    * @param searchString string that was searched for
    * @param searchAll whether all files were searched
    * @param doc weak reference to the document in which the search occurred (or started, if all documents were searched)
    * @param findReplace the FindReplacePanel that created this FindResultsPanel
    */
  public FindResultsPanel createFindResultsPanel(final RegionManager<MovingDocumentRegion> rm, String title,
                                                 String searchString, boolean searchAll, boolean matchCase,
                                                 boolean wholeWord, boolean noComments, boolean noTestCases,
                                                 WeakReference<OpenDefinitionsDocument> doc,
                                                 FindReplacePanel findReplace) {
    final FindResultsPanel panel = new FindResultsPanel(this, rm, title, searchString, searchAll, matchCase,
                                                        wholeWord, noComments, noTestCases, doc, findReplace);
    final Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo> highlights =
      new Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>();
    Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>> pair =
      new Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>>(panel, highlights);
    _findResults.add(pair);
    
    // hook highlighting listener to find results manager
    rm.addListener(new RegionManagerListener<MovingDocumentRegion>() {      
      public void regionAdded(MovingDocumentRegion r) {
        DefinitionsPane bpPane = getDefPaneGivenODD(r.getDocument());
        highlights.put(r, bpPane.getHighlightManager().
                         addHighlight(r.getStartOffset(), r.getEndOffset(), panel.getSelectedPainter()));
      }
      public void regionChanged(MovingDocumentRegion r) { 
        regionRemoved(r);
        regionAdded(r);
      }
      public void regionRemoved(MovingDocumentRegion r) {
        HighlightManager.HighlightInfo highlight = highlights.get(r);
        if (highlight != null) highlight.remove();
        highlights.remove(r);
        // close the panel when all regions have been removed.
        if (rm.getRegions().size()==0) { panel._close(); }
      }
    });
    
    _tabs.addLast(panel);
    panel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = panel; }
    });
    
    return panel;
  }
  
  /** Disable "Find Again" on "Find All" tabs that use a document that was closed. */
  void disableFindAgainOnClose(List<OpenDefinitionsDocument> projDocs) {
    for(TabbedPanel t: _tabs) {
      if (t instanceof FindResultsPanel) {
        FindResultsPanel p = (FindResultsPanel)t;
        if (projDocs.contains(p.getDocument())) { p.disableFindAgain(); }
      }
    }
  }
  
  /** Shows a find results tab. Only runs in event thread. */
  public void showFindResultsPanel(final FindResultsPanel panel) {
    assert EventQueue.isDispatchThread();
    if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
      _mainSplit.resetToPreferredSizes(); 
    if (! panel.isDisplayed()) showTab(panel);
    panel.setVisible(true);
    _tabbedPane.setSelectedComponent(panel);
    // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
    EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
  };
  
  /** Cuts from the caret to the end of the current line to the clipboard. */
  protected final Action _cutLineAction = new AbstractAction("Cut Line") {
    public void actionPerformed(ActionEvent ae) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      int oldCol = _model.getActiveDocument().getCurrentCol();
      _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
      // if oldCol is equal to the current column, then selectionEndLine did
      // nothing, so we're at the end of the line and should remove the newline
      // character
      if (oldCol == _model.getActiveDocument().getCurrentCol()) {
        // Puts newline character on the clipboard also, not just content as before.
        _actionMap.get(DefaultEditorKit.selectionForwardAction).actionPerformed(ae);
        cutAction.actionPerformed(ae);
      }
      else cutAction.actionPerformed(ae);
    }
  };
  
  /** Deletes text from the caret to the end of the current line. */
  protected final Action _clearLineAction = new AbstractAction("Clear Line") {
    public void actionPerformed(ActionEvent ae) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
      _actionMap.get(DefaultEditorKit.deleteNextCharAction).actionPerformed(ae);
    }
  };
  
  /** Moves the caret to the "intelligent" beginning of the line.
    * @see #_getBeginLinePos
    */
  private final Action _beginLineAction = new AbstractAction("Begin Line") {
    public void actionPerformed(ActionEvent ae) {
      int beginLinePos = _getBeginLinePos();
      _currentDefPane.setCaretPosition(beginLinePos);
    }
  };
  
  /** Selects to the "intelligent" beginning of the line.
    * @see #_getBeginLinePos
    */
  private final Action _selectionBeginLineAction = new AbstractAction("Select to Beginning of Line") {
    public void actionPerformed(ActionEvent ae) {
      int beginLinePos = _getBeginLinePos();
      _currentDefPane.moveCaretPosition(beginLinePos);
    }
  };
  
  /** Returns the "intelligent" beginning of line.  If the caret is to fhe right of the first non-whitespace character,
    * the position of the first non-whitespace character is returned.  If the caret is on or to the left of the first 
    * non-whitespace character, the beginning of the line is returned.
    */
  private int _getBeginLinePos() {
    try {
      int currPos = _currentDefPane.getCaretPosition();
      OpenDefinitionsDocument openDoc = _model.getActiveDocument();
      openDoc.setCurrentLocation(currPos);
      return openDoc.getIntelligentBeginLinePos(currPos);
    }
    catch (BadLocationException ble) {
      // Shouldn't happen: we're using a legal position
      throw new UnexpectedException(ble);
    }
  }
  
  private final FileOpenSelector _interactionsHistoryFileSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      return getOpenFiles(_interactionsHistoryChooser);
    }
  };
  
  /** Interprets the commands in a file in the interactions window. */
  private final Action _executeHistoryAction = new AbstractAction("Execute Interactions History...") {
    public void actionPerformed(ActionEvent ae) {
      // Show interactions tab
      _tabbedPane.setSelectedIndex(INTERACTIONS_TAB);
      
      _interactionsHistoryChooser.setDialogTitle("Execute Interactions History");
      try { _model.loadHistory(_interactionsHistoryFileSelector); }
      catch (FileNotFoundException fnf) { _showFileNotFoundError(fnf); }
      catch (IOException ioe) { _showIOError(ioe); }
      _interactionsPane.requestFocusInWindow();
    }
  };
  
  /** Closes the currently executing interactions script, if there is one. */
  private void _closeInteractionsScript() {
    if (_interactionsScriptController != null) {
      _interactionsContainer.remove(_interactionsScriptPane);
      _interactionsScriptController = null;
      _interactionsScriptPane = null;
      _tabbedPane.invalidate();
      _tabbedPane.repaint();
    }
  }
  
  /** Action to load an interactions history as a replayable script. */
  private final Action _loadHistoryScriptAction = new AbstractAction("Load Interactions History as Script...") {
    public void actionPerformed(ActionEvent e) {
      try {
        _interactionsHistoryChooser.setDialogTitle("Load Interactions History");
        InteractionsScriptModel ism = _model.loadHistoryAsScript(_interactionsHistoryFileSelector);
        _interactionsScriptController = new InteractionsScriptController(ism, new AbstractAction("Close") {
          public void actionPerformed(ActionEvent e) {
            _closeInteractionsScript();
            _interactionsPane.requestFocusInWindow();
          }
        }, _interactionsPane);
        _interactionsScriptPane = _interactionsScriptController.getPane();
        _interactionsContainer.add(_interactionsScriptPane, BorderLayout.EAST);
        _tabbedPane.invalidate();
        _tabbedPane.repaint();
      }
      catch (FileNotFoundException fnf) { _showFileNotFoundError(fnf); }
      catch (IOException ioe) { _showIOError(ioe); }
      catch (OperationCanceledException oce) {
      }
    }
  };
  
  /** Save the commands in the interactions window's history to a file */
  private final Action _saveHistoryAction = new AbstractAction("Save Interactions History...") {
    public void actionPerformed(ActionEvent ae) {
      String[] options = {"Yes","No","Cancel"};
      int resp = JOptionPane.showOptionDialog(MainFrame.this,
                                              "Edit interactions history before saving?",
                                              "Edit History?",
                                              JOptionPane.YES_NO_CANCEL_OPTION,
                                              JOptionPane.QUESTION_MESSAGE,
                                              null,options,
                                              options[1]);
      // Cancel
      if (resp == 2 || resp == JOptionPane.CLOSED_OPTION) return;
      
      String history = _model.getHistoryAsStringWithSemicolons();
      
      // Edit the history
      if (resp == 0)
        history = (new HistorySaveDialog(MainFrame.this)).editHistory(history);
      if (history == null) return; // save cancelled
      
      _interactionsHistoryChooser.setDialogTitle("Save Interactions History");
      FileSaveSelector selector = new FileSaveSelector() {
        public File getFile() throws OperationCanceledException {
          // Don't try to set the fileName with getSaveFile;
          // just display the dialog and get file with getChosenFile, otherwise
          // the suggested file name will be whatever document is open.
          // ED (8.14.03): Had to add this next block of code from getSaveFile to
          // fix bug #788311 "NullPointer when saving history"
          File selection = _interactionsHistoryChooser.getSelectedFile();//_saveChooser.getSelectedFile();
          if (selection != null) {
            _interactionsHistoryChooser.setSelectedFile(selection.getParentFile());
            _interactionsHistoryChooser.setSelectedFile(selection);
            _interactionsHistoryChooser.setSelectedFile(null);
          }
//          return getSaveFile(_interactionsHistoryChooser);
          int rc = _interactionsHistoryChooser.showSaveDialog(MainFrame.this);
          File c = getChosenFile(_interactionsHistoryChooser, rc);
          //Moved from history itself to here to account for bug #989232, non-existant default
          //history file found
          if (c.getName().indexOf('.') == -1)
            c = new File(c.getAbsolutePath() + "." + InteractionsHistoryFilter.HIST_EXTENSION);
          _interactionsHistoryChooser.setSelectedFile(c);
          return c;
        }
        public boolean warnFileOpen(File f) { return true; }
        public boolean verifyOverwrite() { return _verifyOverwrite(); }
        public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
          return true;
        }
      };
      
      try { _model.saveHistory(selector, history);}
      catch (IOException ioe) {
        _showIOError(new IOException("An error occured writing the history to a file"));
      }
      _interactionsPane.requestFocusInWindow();
    }
  };
  
  /** Clears the commands in the interaction history. */
  private final Action _clearHistoryAction = new AbstractAction("Clear Interactions History") {
    public void actionPerformed(ActionEvent ae) {
      _model.clearHistory();
      _interactionsPane.requestFocusInWindow();
    }
  };
  
  /** How DrJava responds to window events. */
  private final WindowListener _windowCloseListener = new WindowAdapter() {
    public void windowActivated(WindowEvent ev) { }
    public void windowClosed(WindowEvent ev) { }
    public void windowClosing(WindowEvent ev) { _quit(); }
    public void windowDeactivated(WindowEvent ev) { }
    public void windowDeiconified(WindowEvent ev) {
      try { _model.getActiveDocument().revertIfModifiedOnDisk(); }
      catch (FileMovedException fme) { _showFileMovedError(fme); }
      catch (IOException e) { _showIOError(e);}
    }
    public void windowIconified(WindowEvent ev) { }
    public void windowOpened(WindowEvent ev) { _currentDefPane.requestFocusInWindow(); }
  };
  
  private final MouseListener _resetFindReplaceListener = new MouseListener() {
    public void mouseClicked (MouseEvent e) { }
    public void mousePressed (MouseEvent e) { }
    //as mouseReleased event so that it happens after the document has been set in the model and defPane
    public void mouseReleased (MouseEvent e) {_findReplace.updateFirstDocInSearch();}
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited (MouseEvent e) { }
  };
  
  // ------------- File Display Managers for File Icons ------------
  
  private static final DJFileDisplayManager _djFileDisplayManager20;
  private static final DJFileDisplayManager _djFileDisplayManager30;
  private static final OddDisplayManager _oddDisplayManager20;
  private static final OddDisplayManager _oddDisplayManager30;
  private static final Icon _djProjectIcon;
  
  static {
    Icon java, dj0, dj1, dj2, other, star, jup, juf;
    
    java = MainFrame.getIcon("JavaIcon20.gif");
    dj0 = MainFrame.getIcon("ElementaryIcon20.gif");
    dj1 = MainFrame.getIcon("IntermediateIcon20.gif");
    dj2 = MainFrame.getIcon("AdvancedIcon20.gif");
    other = MainFrame.getIcon("OtherIcon20.gif");
    _djFileDisplayManager20 = new DJFileDisplayManager(java,dj0,dj1,dj2,other);
    
    java = MainFrame.getIcon("JavaIcon30.gif");
    dj0 = MainFrame.getIcon("ElementaryIcon30.gif");
    dj1 = MainFrame.getIcon("IntermediateIcon30.gif");
    dj2 = MainFrame.getIcon("AdvancedIcon30.gif");
    other = MainFrame.getIcon("OtherIcon30.gif");
    _djFileDisplayManager30 = new DJFileDisplayManager(java,dj0,dj1,dj2,other);
    
    star = MainFrame.getIcon("ModStar20.gif");
    jup = MainFrame.getIcon("JUnitPass20.gif");
    juf = MainFrame.getIcon("JUnitFail20.gif");
    _oddDisplayManager20 = new OddDisplayManager(_djFileDisplayManager20,star,jup,juf);
    
    star = MainFrame.getIcon("ModStar30.gif");
    jup = MainFrame.getIcon("JUnitPass30.gif");
    juf = MainFrame.getIcon("JUnitFail30.gif");
    _oddDisplayManager30 = new OddDisplayManager(_djFileDisplayManager30,star,jup,juf);
    
    _djProjectIcon = MainFrame.getIcon("ProjectIcon.gif");
  }
  
  
  /** This manager is meant to retrieve the correct icons for the given filename. The only files recognized 
    * are the files obviously listed below in the function (.java, .dj0, .dj1, .dj2). The icons that represent 
    * each filetype are given into the managers constructor upon instantiation.  This class is static since
    * it currently does not depend of the main frame for information.
    */
  private static class DJFileDisplayManager extends DefaultFileDisplayManager {
    private final Icon _java;
    private final Icon _dj0;
    private final Icon _dj1;
    private final Icon _dj2;
    private final Icon _other;
    
    public DJFileDisplayManager(Icon java, Icon dj0, Icon dj1, Icon dj2, Icon other) {
      _java = java;
      _dj0 = dj0;
      _dj1 = dj1;
      _dj2 = dj2;
      _other = other;
    }
    /** This method chooses the custom icon only for the known filetypes. If these filetypes are not receiving 
      * the correct icons, make sure the filenames are correct and that the icons are present in the ui/icons 
      * directory.
      */
    public Icon getIcon(File f) {
      if (f == null) return _other;
      Icon ret = null;
      if (!f.isDirectory()) {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".java")) ret = _java;
        if (name.endsWith(".dj0")) ret = _dj0;
        if (name.endsWith(".dj1")) ret = _dj1;
        if (name.endsWith(".dj2")) ret = _dj2;
      }
      if (ret == null) {
        ret = super.getIcon(f);
        if (ret.getIconHeight() < _java.getIconHeight()) {
          ret = new CenteredIcon(ret, _java.getIconWidth(), _java.getIconHeight());
        }
      }
      return ret;
    }
  }
  
  /** This class wraps the file display managers by superimposing any notification icons on top of the base 
    * file icon.  Currently, only the modified star is allowed, but everything is set up to add notification 
    * icons for whether a document has passed the junit test (for display in the tree). This class is static 
    * for now.  It may be necessary to make it dynamic when implementing the junit notifications.
    */
  private static class OddDisplayManager implements DisplayManager<OpenDefinitionsDocument> {
    private final Icon _star;
//    private Icon _juPass;
//    private Icon _juFail;
    private final FileDisplayManager _default;
    
    /** Standard constructor.
      * @param star The star icon will be put flush to the left 1/4 the way down
      * @param junitPass indicator of junit success, placed at bottom right
      * @param junitFail indicator of junit failure, placed at bottom right
      */
    public OddDisplayManager(FileDisplayManager fdm, Icon star, Icon junitPass, Icon junitFail) {
      _star = star;
//      _juPass = junitPass;
//      _juFail = junitFail;
      _default = fdm;
    }
    public Icon getIcon(OpenDefinitionsDocument odd) {
      File f = null;
      try { f = odd.getFile(); }
      catch(FileMovedException fme) { /* do nothing */ }
      
      if (odd.isModifiedSinceSave()) return makeLayeredIcon(_default.getIcon(f), _star);
      return _default.getIcon(f);
    }
    public String getName(OpenDefinitionsDocument doc) { return doc.getFileName(); }
    private LayeredIcon makeLayeredIcon(Icon base, Icon star) {
      return new LayeredIcon(new Icon[]{base, star}, new int[]{0, 0}, 
                             new int[]{0, (base.getIconHeight() / 4)});
    }
  };
  
  /** This is what is given to the JTreeSortNavigator.  This simply resolves the INavItem to an OpenDefDoc
    * using the model and forwards it to the OddDisplayManager for size 20.
    */
  private final DisplayManager<INavigatorItem> _navPaneDisplayManager = new DisplayManager<INavigatorItem>() {
    public Icon getIcon(INavigatorItem item) {
      OpenDefinitionsDocument odd = (OpenDefinitionsDocument) item;  // FIX THIS!
      return _oddDisplayManager20.getIcon(odd);
    }
    public String getName(INavigatorItem name) { return name.getName(); }
  };
  
  /** These listeners support the travel operations that cycle through recent documents. */
  KeyListener _historyListener = new KeyListener() {
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode()==java.awt.event.KeyEvent.VK_BACK_QUOTE && e.isControlDown() && !e.isShiftDown()) nextRecentDoc();
      if (e.getKeyCode()==java.awt.event.KeyEvent.VK_BACK_QUOTE && e.isControlDown() && e.isShiftDown()) prevRecentDoc();
//    else if (e.getKeyCode()==java.awt.event.KeyEvent.VK_BACK_QUOTE) {
//        transferFocusUpCycle();
//    }
    }
    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == java.awt.event.KeyEvent.VK_CONTROL) hideRecentDocFrame();
    }
    public void keyTyped(KeyEvent e) { /* noop */ }
  };
  
  FocusListener _focusListenerForRecentDocs = new FocusListener() {
    public void focusLost(FocusEvent e) { hideRecentDocFrame();  }
    public void focusGained(FocusEvent e) { }
  };
  
  
  
  
  public static DJFileDisplayManager getFileDisplayManager20() { return _djFileDisplayManager20; }
  public static DJFileDisplayManager getFileDisplayManager30() { return _djFileDisplayManager30; }
  public static OddDisplayManager getOddDisplayManager20() { return _oddDisplayManager20; }
  public static OddDisplayManager getOddDisplayManager30() { return _oddDisplayManager30; }
  public DisplayManager<INavigatorItem> getNavPaneDisplayManager() { return _navPaneDisplayManager; }
  
  /* ----------------------- Constructor is here! --------------------------- */
  
  /** Creates the main window, and shows it. */ 
  public MainFrame() {
    
    // Cache the config object, since we use it many, many times.
    final Configuration config = DrJava.getConfig();
    
    
//    Utilities.show("MainFrame starting");
    
    // create our model
    _model = new DefaultGlobalModel();
    
    _showDebugger = _model.getDebugger().isAvailable();
    _findReplace = new FindReplacePanel(this, _model);
    
    if (_showDebugger) {
      _debugPanel = new DebugPanel(this);
      _breakpointsPanel = new BreakpointsPanel(this);
    }
    else {
      _debugPanel = null;
      _breakpointsPanel = null; 
    }
    
    _compilerErrorPanel = new CompilerErrorPanel(_model, this);
    _consoleController = new ConsoleController(_model.getConsoleDocument(), _model.getSwingConsoleDocument());
    _consolePane = _consoleController.getPane();
    
    _consoleScroll = new BorderlessScrollPane(_consolePane) {
      public boolean requestFocusInWindow() { 
        super.requestFocusInWindow();
        return _consolePane.requestFocusInWindow(); 
      } 
    };
    
    _interactionsController =
      new InteractionsController(_model.getInteractionsModel(), _model.getSwingInteractionsDocument());
    
    _interactionsPane = _interactionsController.getPane();
//    _interactionsController.setCachedCaretPos(0);
//    _interactionsController.setCachedPromptPos(_model.getConsoleDocument().getPromptPos());
    
    _interactionsContainer = new JPanel(new BorderLayout()); /* {
     public boolean requestFocusInWindow() { 
     super.requestFocusInWindow();
     return _interactionsPane.requestFocusInWindow(); 
     } 
     };*/
    
    _lastFocusOwner = _interactionsContainer;
    
    _junitErrorPanel = new JUnitPanel(_model, this);
    _javadocErrorPanel = new JavadocErrorPanel(_model, this);
    
    _bookmarksPanel = new BookmarksPanel(this);
    
    // Initialize the status bar
    _setUpStatusBar();
    
    // Preliminary layout
    
    /* Definitions Pane */
    
    /* Ensure that DefinitionsPane uses the correct EditorKit!  This has to be stored as a static field on 
     * DefinitionsPane because the JEditorPane constructor uses it before we get a chance to assign it to an instance
     * field ... */
    DefinitionsPane.setEditorKit(_model.getEditorKit());
    
    _defScrollPanes = new Hashtable<OpenDefinitionsDocument, JScrollPane>();
    
    /* Set up tabbed pane and navigation pane. */
    _tabbedPane = new JTabbedPane();
    _tabbedPane.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        Component c = _tabbedPane.getSelectedComponent();
//         System.err.println("focus gained in tabbed pane with selected tab " + c + " with paramString " + e.paramString());
        if (c == _interactionsContainer) {
          _interactionsPane.requestFocusInWindow();
//           System.err.println("Selected tab was interactions container");
        }
        else if (c == _findReplace) _findReplace.getFindField().requestFocusInWindow();
      }
      public void focusLost(FocusEvent e) { }
    });
    
    _tabbedPane.addFocusListener(_focusListenerForRecentDocs);
    _tabbedPane.addKeyListener(_historyListener);    // TODO: can this code be moved to the MainFrame keymap?
    
    JScrollPane defScroll = _createDefScrollPane(_model.getActiveDocument());
    
    _docSplitPane = 
      new BorderlessSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                              new JScrollPane(_model.getDocumentNavigator().asContainer()), defScroll);
    _debugSplitPane = new BorderlessSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    _mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, _docSplitPane, _tabbedPane);
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    // The OptionListener for LIGHTWEIGHT_PARSING_ENABLED.
//    OptionListener<Boolean> parsingEnabledListener = new OptionListener<Boolean>() {
//      public void optionChanged(OptionEvent<Boolean> oce) {
//        if (oce.value) {
//          _model.getParsingControl().addListener(new LightWeightParsingListener() {
//            public void enclosingClassNameUpdated(OpenDefinitionsDocument doc, String old, String updated) {
//              if (doc==_model.getActiveDocument()) { updateStatusField(); }
//            }
//          });
//        }
//        _model.getParsingControl().reset();
//        _model.getParsingControl().setAutomaticUpdates(oce.value);
//        updateStatusField();
//      }
//    };
//    DrJava.getConfig().addOptionListener(LIGHTWEIGHT_PARSING_ENABLED, parsingEnabledListener);
//    parsingEnabledListener.
//      optionChanged(new OptionEvent<Boolean>(LIGHTWEIGHT_PARSING_ENABLED, 
//                                             DrJava.getConfig().getSetting(LIGHTWEIGHT_PARSING_ENABLED).booleanValue()));
    
//    Utilities.show("Global Model started");
    
    _model.getDocumentNavigator().asContainer().addKeyListener(_historyListener);
    _model.getDocumentNavigator().asContainer().addFocusListener(_focusListenerForRecentDocs);
    
    /* Listens for clicks in the document navigator to reset the first document in an all-documents search for wrapping
     * purposes. */
    _model.getDocumentNavigator().asContainer().addMouseListener(_resetFindReplaceListener);
    
    if (_showDebugger) _model.getDebugger().addListener(new UIDebugListener()); // add listener to debug manager
    
    // Timer to display a message if a debugging step takes a long time
    _debugStepTimer = new Timer(DEBUG_STEP_TIMER_VALUE, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _model.printDebugMessage("Stepping...");
      }
    });
    _debugStepTimer.setRepeats(false);
    
    // Working directory is default place to start (bug #895998).
    File workDir = _model.getMasterWorkingDirectory();
    
    // Overrides JFileChooser to display the full path of the directory
    _openChooser = new JFileChooser() {
      public void setCurrentDirectory(File dir) {
        //next two lines are order dependent!
        super.setCurrentDirectory(dir);
        setDialogTitle("Open:  " + getCurrentDirectory());
      }
    };
    _openChooser.setPreferredSize(new Dimension(650, 410));
    _openChooser.setCurrentDirectory(workDir);
    _openChooser.setFileFilter(_javaSourceFilter);
    _openChooser.setMultiSelectionEnabled(true);
    
    _openRecursiveCheckBox = new JCheckBox("Open folders recursively");
    _openRecursiveCheckBox.setSelected(config.getSetting(OptionConstants.OPEN_FOLDER_RECURSIVE).booleanValue());
    
    _folderChooser = makeFolderChooser(workDir);
    
    //Get most recently opened project for filechooser
    Vector<File> recentProjects = config.getSetting(RECENT_PROJECTS);
    _openProjectChooser = new JFileChooser();
    _openProjectChooser.setPreferredSize(new Dimension(650, 410));
    
    if (recentProjects.size() > 0 && recentProjects.elementAt(0).getParentFile() != null)
      _openProjectChooser.setCurrentDirectory(recentProjects.elementAt(0).getParentFile());
    else
      _openProjectChooser.setCurrentDirectory(workDir);
    
    _openProjectChooser.setFileFilter(_projectFilter);
    _openProjectChooser.setMultiSelectionEnabled(false);
    _saveChooser = new JFileChooser() {
      public void setCurrentDirectory(File dir) {
        //next two lines are order dependent!
        super.setCurrentDirectory(dir);
        setDialogTitle("Save:  " + getCurrentDirectory());
      }
    };
    _saveChooser.setPreferredSize(new Dimension(650, 410));
    _saveChooser.setCurrentDirectory(workDir);
    _saveChooser.setFileFilter(_javaSourceFilter);
    
    _interactionsHistoryChooser = new JFileChooser();
    _interactionsHistoryChooser.setPreferredSize(new Dimension(650, 410));
    _interactionsHistoryChooser.setCurrentDirectory(workDir);
    _interactionsHistoryChooser.setFileFilter(new InteractionsHistoryFilter());
    _interactionsHistoryChooser.setMultiSelectionEnabled(true);
    
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    
    // Set up listeners
    addWindowListener(_windowCloseListener);
    
    // Create the main model listener and attach it to the global model
    _mainListener = new ModelListener();
    _model.addListener(_mainListener);
    
    // Initialize tabs before DefPane
    _setUpTabs();
    
    // DefinitionsPane
    _recentDocFrame = new RecentDocFrame(this);
    _recentDocFrame.pokeDocument(_model.getActiveDocument());
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();
    _currentDefPane.notifyActive();
    
    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    // set up key-bindings
    KeyBindingManager.Singleton.setMainFrame(this);
    KeyBindingManager.Singleton.setActionMap(_currentDefPane.getActionMap());
    _setUpKeyBindingMaps();
    
    _posListener.updateLocation();
    
    // Need to set undo/redo actions to point to the initial def pane
    // on switching documents later these pointers will also switch
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());
    
    _compilerErrorPanel.reset();
    _junitErrorPanel.reset();
    _javadocErrorPanel.reset();
    
    // Create menubar and menus
    _menuBar = new MenuBar();
    _fileMenu = _setUpFileMenu(mask);
    _editMenu = _setUpEditMenu(mask);
    _toolsMenu = _setUpToolsMenu(mask);
    _projectMenu = _setUpProjectMenu(mask);
    
    if (_showDebugger) _debugMenu = _setUpDebugMenu(mask);
    _languageLevelMenu = _setUpLanguageLevelMenu(mask);
    _helpMenu = _setUpHelpMenu(mask);
    
    // initialize menu bar and actions
    _setUpActions();
    _setUpMenuBar();
    
    //    _setUpDocumentSelector();
    _setUpContextMenus();
    
    // Create toolbar and buttons
    
    _toolBar = new JToolBar();
    _undoButton = _createManualToolbarButton(_undoAction);
    _redoButton = _createManualToolbarButton(_redoAction);
    
    // initialize _toolBar
    
    _setUpToolBar();
    
    // add recent file and project manager
    
    RecentFileAction fileAct = new RecentFileManager.RecentFileAction() { 
      public void actionPerformed(FileOpenSelector selector) { open(selector); }
    }; 
    _recentFileManager = new RecentFileManager(_fileMenu.getItemCount() - 2, _fileMenu, fileAct, 
                                               OptionConstants.RECENT_FILES);
    
    RecentFileAction projAct = new RecentFileManager.RecentFileAction() { 
      public void actionPerformed(FileOpenSelector selector) { openProject(selector); } 
    };
    _recentProjectManager = new RecentFileManager(_projectMenu.getItemCount() - 2, _projectMenu, projAct, 
                                                  OptionConstants.RECENT_PROJECTS);
    // Set frame icon
    setIconImage(getIcon("drjava64.png").getImage());
    
    // Size and position
    int x = config.getSetting(WINDOW_X).intValue();
    int y = config.getSetting(WINDOW_Y).intValue();
    int width = config.getSetting(WINDOW_WIDTH).intValue();
    int height = config.getSetting(WINDOW_HEIGHT).intValue();
    int state = config.getSetting(WINDOW_STATE).intValue();
    
    // Bounds checking.
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    final int menubarHeight = 24;
    if (height > screenSize.height - menubarHeight)  height = screenSize.height - menubarHeight;  // Too tall, so resize
    
    if (width > screenSize.width)  width = screenSize.width; // Too wide, so resize
    
    // I assume that we want to be contained on the default screen.
    // TODO: support spanning screens in multi-screen setups.
    Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().
      getDefaultConfiguration().getBounds();
    
    if (x == Integer.MAX_VALUE)  x = (bounds.width - width + bounds.x) / 2;    // magic value for "not set" - center.
    if (y == Integer.MAX_VALUE)  y = (bounds.height - height + bounds.y) / 2;  // magic value for "not set" - center.
    if (x < bounds.x)  x = bounds.x;                                           // Too far left, move to left edge.
    if (y < bounds.y)  y = bounds.y;                                           // Too far up, move to top edge.
    if ((x + width) > (bounds.x + bounds.width))  x = bounds.width - width + bounds.x; 
    // Too far right, move to right edge.
    if ((y + height) > (bounds.y + bounds.height))  y = bounds.height - height + bounds.y; 
    // Too far down, move to bottom edge.
    
    //ensure that we don't set window state to minimized
    state &= ~Frame.ICONIFIED;
    
    if (!Toolkit.getDefaultToolkit().isFrameStateSupported(state)) {
      //we have a bad state, so reset to default
      state = WINDOW_STATE.getDefault();
    }
    
    // Set to the new correct size and location
    setBounds(x, y, width, height);
    
    //Work-aroung for Java bug #6365898?
    //setExtendedState does not work until the window in shown on Linux.
    final int stateCopy = state;
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        setExtendedState(stateCopy);
        //this is a one-off listener
        removeWindowListener(this);
      }
    });
    
    _setUpPanes();
    updateStatusField();
    
    _promptBeforeQuit = config.getSetting(QUIT_PROMPT).booleanValue();
    
    // Set the fonts
    _setMainFont();
    Font doclistFont = config.getSetting(FONT_DOCLIST);
    _model.getDocCollectionWidget().setFont(doclistFont);
    
    // Set the colors
    _updateNormalColor();
    _updateBackgroundColor();
    
    // Add OptionListeners for the colors.
    config.addOptionListener(DEFINITIONS_NORMAL_COLOR, new NormalColorOptionListener());
    config.addOptionListener(DEFINITIONS_BACKGROUND_COLOR, new BackgroundColorOptionListener());
    
    /* Add option listeners for changes to config options.  NOTE: We should only add listeners to view-related (or view-
     * dependent) config options here.  Model options should go in DefaultGlobalModel._registerOptionListeners(). */
    config.addOptionListener(FONT_MAIN, new MainFontOptionListener());
    config.addOptionListener(FONT_LINE_NUMBERS, new LineNumbersFontOptionListener());
    config.addOptionListener(FONT_DOCLIST, new DoclistFontOptionListener());
    config.addOptionListener(FONT_TOOLBAR, new ToolbarFontOptionListener());
    config.addOptionListener(TOOLBAR_ICONS_ENABLED, new ToolbarOptionListener());
    config.addOptionListener(TOOLBAR_TEXT_ENABLED, new ToolbarOptionListener());
    config.addOptionListener(TOOLBAR_ENABLED, new ToolbarOptionListener());
    config.addOptionListener(LINEENUM_ENABLED, new LineEnumOptionListener());
    config.addOptionListener(QUIT_PROMPT, new QuitPromptOptionListener());
    config.addOptionListener(RECENT_FILES_MAX_SIZE, new RecentFilesOptionListener());
    
    config.addOptionListener(ALLOW_PRIVATE_ACCESS, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _model.getInteractionsModel().setPrivateAccessible(oce.value.booleanValue());
      }
    });
    
    config.addOptionListener(FORCE_TEST_SUFFIX, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _model.getJUnitModel().setForceTestSuffix(oce.value.booleanValue());
      }
    });
    
    // The OptionListener for JAVADOC_API_REF_VERSION.
    OptionListener<String> choiceOptionListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        _javaAPIList = null;
      }
    };
    DrJava.getConfig().addOptionListener(JAVADOC_API_REF_VERSION, choiceOptionListener);
    
    // The OptionListener for JAVADOC_XXX_LINK.
    OptionListener<String> link13OptionListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
        if (linkVersion.equals(JAVADOC_1_3_TEXT)) {
          _javaAPIList = null;
        }
      }
    };
    DrJava.getConfig().addOptionListener(JAVADOC_1_3_LINK, link13OptionListener);
    OptionListener<String> link14OptionListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
        if (linkVersion.equals(JAVADOC_1_4_TEXT)) {
          _javaAPIList = null;
        }
      }
    };
    DrJava.getConfig().addOptionListener(JAVADOC_1_4_LINK, link14OptionListener);
    OptionListener<String> link15OptionListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
        if (linkVersion.equals(JAVADOC_1_5_TEXT)) {
          _javaAPIList = null;
        }
      }
    };
    DrJava.getConfig().addOptionListener(JAVADOC_1_5_LINK, link15OptionListener);
    OptionListener<String> link16OptionListener = new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oce) {
        String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
        if (linkVersion.equals(JAVADOC_1_6_TEXT)) {
          _javaAPIList = null;
        }
      }
    };
    DrJava.getConfig().addOptionListener(JAVADOC_1_6_LINK, link16OptionListener);
    
    // Initialize DocumentRegion highlights hashtables, for easy removal of highlights
    _documentBreakpointHighlights = new Hashtable<Breakpoint, HighlightManager.HighlightInfo>();
    _documentBookmarkHighlights = new Hashtable<DocumentRegion, HighlightManager.HighlightInfo>();
    
    // Initialize cached frames and dialogs 
    _configFrame = new ConfigFrame(this);
    _helpFrame = new HelpFrame();
    _aboutDialog = new AboutDialog(MainFrame.this);
    _quickStartFrame = new QuickStartFrame();
    _interactionsScriptController = null;
    _executeExternalDialog = new ExecuteExternalDialog(MainFrame.this);
    _editExternalDialog = new EditExternalDialog(MainFrame.this);
    _jarOptionsDialog = new JarOptionsDialog(MainFrame.this);
    
    initJarOptionsDialog();
    initExecuteExternalProcessDialog();
//    _projectPropertiesFrame = null;
    
    config.addOptionListener(LOOK_AND_FEEL, new ConfigOptionListeners.LookAndFeelListener(_configFrame));
    OptionListener<String> slaveJVMArgsListener = new ConfigOptionListeners.SlaveJVMArgsListener(_configFrame);
    config.addOptionListener(SLAVE_JVM_ARGS, slaveJVMArgsListener);
    _slaveJvmXmxListener = new ConfigOptionListeners.SlaveJVMXMXListener(_configFrame);
    config.addOptionListener(SLAVE_JVM_XMX, _slaveJvmXmxListener);
    OptionListener<String> masterJVMArgsListener = new ConfigOptionListeners.MasterJVMArgsListener(_configFrame);
    config.addOptionListener(MASTER_JVM_ARGS, masterJVMArgsListener);
    _masterJvmXmxListener = new ConfigOptionListeners.MasterJVMXMXListener(_configFrame);
    config.addOptionListener(MASTER_JVM_XMX, _masterJvmXmxListener);
    config.addOptionListener(JAVADOC_CUSTOM_PARAMS, new ConfigOptionListeners.JavadocCustomParamsListener(_configFrame));
    ConfigOptionListeners.sanitizeSlaveJVMArgs(this, config.getSetting(SLAVE_JVM_ARGS), slaveJVMArgsListener);
    ConfigOptionListeners.sanitizeSlaveJVMXMX(this, config.getSetting(SLAVE_JVM_XMX));
    ConfigOptionListeners.sanitizeMasterJVMArgs(this, config.getSetting(MASTER_JVM_ARGS), masterJVMArgsListener);
    ConfigOptionListeners.sanitizeMasterJVMXMX(this, config.getSetting(MASTER_JVM_XMX));
    ConfigOptionListeners.sanitizeJavadocCustomParams(this, config.getSetting(JAVADOC_CUSTOM_PARAMS));
    
    // If any errors occurred while parsing config file, show them
    _showConfigException();
    
    KeyBindingManager.Singleton.setShouldCheckConflict(false);
    
    // Platform-specific UI setup.
    PlatformFactory.ONLY.afterUISetup(_aboutAction, _editPreferencesAction, _quitAction);
    setUpKeys();    
    
    // discard ` character if it was used for the next/prev recent doc feature
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
      public boolean dispatchKeyEvent(KeyEvent e) {
        boolean discardEvent = false;
        
        if ((e.getID() == KeyEvent.KEY_TYPED) &&
            (e.getKeyChar()=='`') &&
            (((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) ||
             ((e.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK))
                == (InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK))) &&
            (e.getComponent().getClass().equals(DefinitionsPane.class))) {
//          System.out.println("discarding `, modifiers = "+e.getModifiersEx()+": "+e.getComponent());
          discardEvent = true;
        }
        
        return discardEvent;
      }
    });
    
    if (DrJava.getConfig().getSetting(edu.rice.cs.drjava.config.OptionConstants.REMOTE_CONTROL_ENABLED)) {
      // start remote control server if no server is running
      try {
        if (!RemoteControlClient.isServerRunning()) {
          edu.rice.cs.drjava.RemoteControlServer rcServer =
            new edu.rice.cs.drjava.RemoteControlServer(this);
        }
      }
      catch(IOException ioe) {
        try {
          RemoteControlClient.openFile(null);
        }
        catch(IOException ignored) {
          // ignore
        }
        if (!Utilities.TEST_MODE && !System.getProperty("user.name").equals(RemoteControlClient.getServerUser())) {
          Object[] options = {"Disable","Ignore"};
          String msg = "<html>Could not start DrJava's remote control server";
          if (RemoteControlClient.getServerUser()!=null) {
            msg += "<br>because user "+RemoteControlClient.getServerUser()+" is already using the same port";
          }
          msg += ".<br>Please select an unused port in the Preferences dialog.<br>"+
            "In the meantime, do you want to disable the remote control feature?";
          int n = JOptionPane.showOptionDialog(MainFrame.this,
                                               msg,
                                               "Could Not Start Remote Control Server",
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.QUESTION_MESSAGE,
                                               null,
                                               options,
                                               options[1]);
          if (n==JOptionPane.YES_OPTION) {
            DrJava.getConfig().setSetting(edu.rice.cs.drjava.config.OptionConstants.REMOTE_CONTROL_ENABLED, false);
          }
        }
      }
    }
    
    setUpDrJavaProperties();  
    
    DrJavaErrorHandler.setButton(_errorsButton);
    
    // check for new version if desired by user
    if (!DrJava.getConfig().getSetting(OptionConstants.NEW_VERSION_NOTIFICATION)
          .equals(OptionConstants.NEW_VERSION_NOTIFICATION_CHOICES.get(3))) {
      int days = DrJava.getConfig().getSetting(NEW_VERSION_NOTIFICATION_DAYS);
      java.util.Date nextCheck = new java.util.Date(DrJava.getConfig().getSetting(OptionConstants.LAST_NEW_VERSION_NOTIFICATION)
                                                      + days * 24 * 60 * 60 * 1000); // x days after last check
      if (new java.util.Date().after(nextCheck)) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            NewVersionPopup popup = new NewVersionPopup(MainFrame.this);
            if (popup.checkNewVersion()) { popup.setVisible(true); }
          }
        });
      }
    }
  }   // End of MainFrame constructor
  
  public void setVisible(boolean b) { 
    _updateToolBarVisible();
    super.setVisible(b); 
  }
  
  /** This method sets up all the DrJava properties that can be used as variables
    * in external process command lines. */
  public void setUpDrJavaProperties() {
    final String DEF_DIR = "${drjava.working.dir}";

    DrJavaPropertySetup.setup(); 

    // Files
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileProperty("drjava.current.file", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getActiveDocument().getRawFile(); }
    }, 
                                                                  "Returns the current document in DrJava.\n"+
                                                                  "Optional attributes:\n"+
                                                                  "\trel=\"<dir to which the output should be relative\""));
    PropertyMaps.ONLY.setProperty("DrJava", new EagerProperty("drjava.current.line", 
                                                              "Returns the current line in the Definitions Pane.") {
      public void update() {
        _value = String.valueOf(_posListener.lastLine());
      }
    });
    PropertyMaps.ONLY.setProperty("DrJava", new EagerProperty("drjava.current.col", 
                                                              "Returns the current column in the Definitions Pane.") {
      public void update() {
//        int line = _currentDefPane.getCurrentLine();
//        int lineOffset = _currentDefPane.getLineStartOffset(line);
//        int caretPos = _currentDefPane.getCaretPosition();
        _value = String.valueOf(_posListener.lastCol());
      }
    });
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileProperty("drjava.working.dir", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getInteractionsModel().getWorkingDirectory(); }
    },
                                                                  "Returns the current working directory of DrJava.\n"+
                                                                  "Optional attributes:\n"+
                                                                  "\trel=\"<dir to which output should be relative\""));
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileProperty("drjava.master.working.dir", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getMasterWorkingDirectory(); }
    },
                                                                  "Returns the working directory of the DrJava master JVM.\n"+
                                                                  "Optional attributes:\n"+
                                                                  "\trel=\"<dir to which output should be relative\""));

    // Files
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileListProperty("drjava.all.files", File.pathSeparator, DEF_DIR,
                                                                      "Returns a list of all files open in DrJava.\n"+
                                                                      "Optional attributes:\n"+
                                                                      "\trel=\"<dir to which output should be relative\"\n"+
                                                                      "\tsep=\"<separator between files>\"") {
      protected List<File> getList() {
        ArrayList<File> l = new ArrayList<File>();
        for(OpenDefinitionsDocument odd: _model.getOpenDefinitionsDocuments()) {
          l.add(odd.getRawFile());
        }
        return l;
      }
    });
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileListProperty("drjava.project.files", File.pathSeparator, DEF_DIR,
                                                                      "Returns a list of all files open in DrJava that belong "+
                                                                      "to a project and are underneath the project root.\n"+
                                                                      "Optional attributes:\n"+
                                                                      "\trel=\"<dir to which output should be relative\"\n"+
                                                                      "\tsep=\"<separator between files>\"") {
      protected List<File> getList() {
        ArrayList<File> l = new ArrayList<File>();
        for(OpenDefinitionsDocument odd: _model.getProjectDocuments()) {
          l.add(odd.getRawFile());
        }
        return l;
      }
    }).listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files"));
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileListProperty("drjava.included.files", File.pathSeparator, DEF_DIR,
                                                                      "Returns a list of all files open in DrJava that are "+
                                                                      "not underneath the project root but are included in "+
                                                                      "the project.\n"+
                                                                      "Optional attributes:\n"+
                                                                      "\trel=\"<dir to which output should be relative\"\n"+
                                                                      "\tsep=\"<separator between files>\"") {
      protected List<File> getList() {
        ArrayList<File> l = new ArrayList<File>();
        for(OpenDefinitionsDocument odd: _model.getAuxiliaryDocuments()) {
          l.add(odd.getRawFile());
        }
        return l;
      }
    }).listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files"));
    PropertyMaps.ONLY.setProperty("DrJava", new EagerFileListProperty("drjava.external.files", File.pathSeparator, DEF_DIR,
                                                                      "Returns a list of all files open in DrJava that are "+
                                                                      "not underneath the project root and are not included in "+
                                                                      "the project.\n"+
                                                                      "Optional attributes:\n"+
                                                                      "\trel=\"<dir to which output should be relative\"\n"+
                                                                      "\tsep=\"<separator between files>\"") {
      protected List<File> getList() {
        ArrayList<File> l = new ArrayList<File>();
        for(OpenDefinitionsDocument odd: _model.getNonProjectDocuments()) {
          l.add(odd.getRawFile());
        }
        return l;
      }
    }).listenToInvalidatesOf(PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files"));    
    
    PropertyMaps.ONLY.setProperty("Misc", new DrJavaProperty("input", "(User Input...)",
                                                             "Get an input string from the user.\n"+
                                                             "Optional attributes:\n"+
                                                             "\tprompt=\"<prompt to display>\"\n"+
                                                             "\tdefault=\"<suggestion to the user>\"") {
      public String toString() {
        return "(User Input...)";
      }
      public void update() {
        String msg = _attributes.get("prompt");
        if (msg == null) msg = "Please enter text for the external process.";
        String input = _attributes.get("default");
        if (input == null) input = "";
        input = JOptionPane.showInputDialog(MainFrame.this, msg, input);
        if (input == null) input = _attributes.get("default");
        if (input == null) input = "";
        _value = input;
      }
      public String getCurrent() {
        invalidate();
        return super.getCurrent();
      }
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("prompt", null);
        _attributes.put("default", null);
      }
    });

    // Project
    PropertyMaps.ONLY.setProperty("Project", new EagerProperty("project.mode",
                                                               "Evaluates to true if a project is loaded.") {
      public void update() {
        long millis = System.currentTimeMillis();
        String f = _attributes.get("fmt").toLowerCase();
        Boolean b = _model.isProjectActive();
        if (f.equals("int")) _value = b ? "1" : "0";
        else if (f.equals("yes")) _value = b ? "yes" : "no";
        else _value = b.toString();
      }
      
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("fmt", "boolean");
      }
    });
    PropertyMaps.ONLY.setProperty("Project", new EagerProperty("project.changed",
                                                               "Evaluates to true if the project has been "+
                                                               "changed since the last save.") {  //TODO: factor out repeated code!
      public void update() {
        long millis = System.currentTimeMillis();
        String f = _attributes.get("fmt").toLowerCase();
        Boolean b = _model.isProjectChanged();
        if (f.equals("int")) _value = b ? "1" : "0";
        else if (f.equals("yes")) _value = b ? "yes":"no";
        else  _value = b.toString();
      }
      
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("fmt", "boolean");
      }
    });
    PropertyMaps.ONLY.setProperty("Project", new EagerFileProperty("project.file", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getProjectFile(); }
    },
                                                                   "Returns the current project file in DrJava.\n"+
                                                                   "Optional attributes:\n"+
                                                                   "\trel=\"<dir to which the output should be relative\""));

    PropertyMaps.ONLY.setProperty("Project", new EagerFileProperty("project.main.class", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getMainClass(); }
    },
                                                                   "Returns the current project file in DrJava.\n"+
                                                                   "Optional attributes:\n"+
                                                                   "\trel=\"<dir to which the output should be relative\""));
    PropertyMaps.ONLY.setProperty("Project", new EagerFileProperty("project.root", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getProjectRoot(); }
    },
                                                                   "Returns the current project root in DrJava.\n"+
                                                                   "Optional attributes:\n"+
                                                                   "\trel=\"<dir to which the output should be relative\""));
    PropertyMaps.ONLY.setProperty("Project", new EagerFileProperty("project.build.dir", new Lambda<File,Void>() {
      public File apply(Void notUsed) { return _model.getBuildDirectory(); }
    },
                                                                   "Returns the current build directory in DrJava.\n"+
                                                                   "Optional attributes:\n"+
                                                                   "\trel=\"<dir to which the output should be relative\""));
    RecursiveFileListProperty classFilesProperty = 
      new RecursiveFileListProperty("project.class.files", File.pathSeparator, DEF_DIR,
                                    _model.getBuildDirectory().getAbsolutePath(),
                                    "Returns the class files currently in the build directory.") {
      /** Reset the attributes. */
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("sep", _sep);
        _attributes.put("rel", _dir);
        _attributes.put("dir", _model.getBuildDirectory().getAbsolutePath());
        _attributes.put("filter", "*.class");
        _attributes.put("dirfilter", "*");
      }
    };
    PropertyMaps.ONLY.setProperty("Project", classFilesProperty);
    
    PropertyMaps.ONLY.setProperty("Action", new DrJavaActionProperty("action.save.all", "(Save All...)",
                                                                     "Execute a \"Save All\" action.") {
      public void update() {
        _saveAll();
      }
    });
    PropertyMaps.ONLY.setProperty("Action", new DrJavaActionProperty("action.compile.all", "(Compile All...)",
                                                                     "Execute a \"Compile All\" action.") {
      public void update() {
        _compileAll();
      }
    });
    PropertyMaps.ONLY.setProperty("Action", new DrJavaActionProperty("action.clean", "(Clean Build Directory...)",
                                                                     "Execute a \"Clean Build Directory\" action.") {
      public void update() {
        // could not use _clean(), since ProjectFileGroupingState.cleanBuildDirectory()
        // is implemented as an asynchronous task, and DrJava would not wait for its completion
        edu.rice.cs.plt.io.IOUtil.deleteRecursively(_model.getBuildDirectory());
      }
    });
    PropertyMaps.ONLY.setProperty("Action", new DrJavaActionProperty("action.open.file", "(Open File...)",
                                                                     "Execute an \"Open File\" action.\n"+
                                                                     "Required attributes:\n"+
                                                                     "\tfile=\"<file to open>\"\n"+
                                                                     "Optional attributes:\n"+
                                                                     "\tline=\"<line number to display>") {
      public void update() {
        if (_attributes.get("file")!=null) {
          final String dir = StringOps.unescapeSpacesWith1bHex(StringOps.replaceVariables(DEF_DIR,
                                                                                          PropertyMaps.ONLY,
                                                                                          PropertyMaps.GET_CURRENT));
          final String fil = StringOps.unescapeSpacesWith1bHex(StringOps.replaceVariables(_attributes.get("file"),
                                                                                          PropertyMaps.ONLY,
                                                                                          PropertyMaps.GET_CURRENT));
          FileOpenSelector fs = new FileOpenSelector() {
            public File[] getFiles() {
              if (fil.startsWith("/")) { return new File[] { new File(fil) }; }
              else { return new File[] { new File(dir, fil) }; }
            }
          };
          open(fs);
          int lineNo = -1;
          if (_attributes.get("line")!=null) {
            try {
              lineNo = new Integer(_attributes.get("line"));
            }
            catch(NumberFormatException nfe) {
              lineNo = -1;
            }
          }
          if (lineNo>=0) {
            final int l = lineNo;
            edu.rice.cs.util.swing.Utilities.invokeLater(new Runnable() { 
              public void run() { _jumpToLine(l); }
            });
          }
        }
      }
      /** Reset the attributes. */
      public void resetAttributes() {
        _attributes.clear();
        _attributes.put("file", null);
        _attributes.put("line", null);
      }
    });
    PropertyMaps.ONLY.setProperty("Action", new DrJavaActionProperty("action.auto.refresh", "(Auto-Refresh...)",
                                                                     "Execute an \"Auto-Refresh Project\" action.") {
      public void update() {
        _model.autoRefreshProject();
      }
    });
  }
  
  /** Set a new painters for existing breakpoint highlights. */
  void refreshBreakpointHighlightPainter() {
    for(java.util.Map.Entry<Breakpoint,HighlightManager.HighlightInfo> pair: _documentBreakpointHighlights.entrySet()) {
      if (pair.getKey().isEnabled()) {
        pair.getValue().refresh(DefinitionsPane.BREAKPOINT_PAINTER);
      }
      else {
        pair.getValue().refresh(DefinitionsPane.DISABLED_BREAKPOINT_PAINTER);
      }
    }
  }
  
  /** Set new painter for existing bookmark highlights. */
  void refreshBookmarkHighlightPainter() {
    for(HighlightManager.HighlightInfo hi: _documentBookmarkHighlights.values()) {
      hi.refresh(DefinitionsPane.BOOKMARK_PAINTER);
    }
  }
  
  /** Set new painter for existing find results highlights. */
  void refreshFindResultsHighlightPainter(FindResultsPanel panel, 
                                          LayeredHighlighter.LayerPainter painter) {
    for(Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>> pair: _findResults) {
      if (pair.first()==panel) {
        Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo> highlights = pair.second();
        for(HighlightManager.HighlightInfo hi: highlights.values()) {
          hi.refresh(painter);
        }
      }
    }
  }
  
  /** Creates the folder chooser during MainFrame initialization which does not run in event thread. */
  private DirectoryChooser makeFolderChooser(final File workDir) {
    final DirectoryChooser dc = new DirectoryChooser(this);
    /* The following code fragement was moved to the event thread because setSelectedFile occasionally generates an 
     * ArrayOutOfBoundsException otherwise. */
    Utilities.invokeLater(new Runnable() {
      public void run() {
        dc.setSelectedFile(workDir);
        dc.setApproveButtonText("Select");
        dc.setDialogTitle("Open Folder");
        dc.setAccessory(_openRecursiveCheckBox);
      }
    });
    return dc;
  }
//  
//  private JFileChooser makeFolderChooser(File workDir) {
//    _folderChooser = new JFileChooser();
//    _folderChooser.setMultiSelectionEnabled(false);
//    _folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//    _folderChooser.setCurrentDirectory(workDir);
//    _folderChooser.setApproveButtonText("Select");
//    _folderChooser.setFileFilter(new DirectoryFilter());
//    _folderChooser.setDialogTitle("Open Folder");
//    
//    
//    Container button_row = (Container)findButtonContainer(_folderChooser, _folderChooser.getApproveButtonText());
//    Container buttons = (Container) button_row.getParent();
//    
////    Component c2 = ((BorderLayout)_folderChooser.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
//    
////    System.out.println("c1: " + c1);
////    System.out.println("c2: " + c2);
//    
//    
////    JPanel buttons = (JPanel)c2;
////    JPanel button_row = (JPanel)buttons.getComponent(3);
//    JPanel bottom_row = new JPanel();
//    bottom_row.setLayout(new BorderLayout());
//    bottom_row.add(new JCheckBox("Recursive Open"), BorderLayout.CENTER);
//    bottom_row.add(button_row, BorderLayout.EAST);
//    buttons.add(bottom_row);
//    
//    return _folderChooser;
//  }
  
//  private Container findButtonContainer(Container container, String buttonText) {
//    Container answer = null;
//    Component[] cs = container.getComponents();
//    for(Component c: cs) {
//      if (c instanceof JButton && ((JButton)c).getText().equals(buttonText)) {
//        return container;
//      }else if (c instanceof Container) {
//        answer = findButtonContainer((Container)c, buttonText);
//      }
//      
//      if (answer != null) break;
//    }
//    return answer;
//  }
  
  
  /** Sets up the ctrl-tab listener. */
  private void setUpKeys() { setFocusTraversalKeysEnabled(false); }
  
  /** Relying on inherited dispose() method. */
  
  /** @return The model providing the logic for this view. */
  public SingleDisplayModel getModel() { return _model; }
  
  /** Returns the frame's interactions pane.  (Package private accessor) */
  InteractionsPane getInteractionsPane() { return _interactionsPane; }
  
  /** Returns the frame's interactions controller. (Package private accessor) */
  InteractionsController getInteractionsController() { return _interactionsController; }
  
  /** @return The frame's close button (Package private accessor). */
  JButton getCloseButton() { return _closeButton; }
  
  /** For testing purposes.
    * @return The frame's compileAll button (Package private accessor)
    */
  JButton getCompileAllButton() { return _compileButton; }
  
  private volatile int _hourglassNestLevel = 0;
  
  /** Make the cursor an hourglass. Only runs in the event thread. */  
  public void hourglassOn() {
    assert EventQueue.isDispatchThread();
    _hourglassNestLevel++;
    if (_hourglassNestLevel == 1) {
      getGlassPane().setVisible(true);
      _currentDefPane.setEditable(false);
      setAllowKeyEvents(false); 
    }
  }
  
  /** Return the cursor to normal. Only runs in the event thread. */ 
  public void hourglassOff() { 
    assert EventQueue.isDispatchThread();
    _hourglassNestLevel--;
    if (_hourglassNestLevel == 0) {
      getGlassPane().setVisible(false);
      _currentDefPane.setEditable(true);
      setAllowKeyEvents(true);
    }
  }
  
  private volatile boolean _allowKeyEvents = true;
  
  public void setAllowKeyEvents(boolean a) { _allowKeyEvents = a; }
  
  public boolean getAllowKeyEvents() { return _allowKeyEvents; }
  
  /** Toggles whether the debugger is enabled or disabled, and updates the display accordingly.  Only runs in the 
    * event thread. */
  public void debuggerToggle() {
    assert EventQueue.isDispatchThread();
    // Make sure the debugger is available
    Debugger debugger = _model.getDebugger();
    if (! debugger.isAvailable()) return;
    
    updateStatusField("Toggling Debugger Mode");
    
    try { 
      if (isDebuggerReady()) debugger.shutdown();
      else {
        // Turn on debugger
        hourglassOn();
        try {
          debugger.startUp();  // may kick active document (if unmodified) out of memory!
          _model.refreshActiveDocument();
          _updateDebugStatus();
        }
        finally { hourglassOff(); }
      }
    }
    catch (DebugException de) {
      _showError(de, "Debugger Error", "Could not start the debugger.");
    }
    catch (NoClassDefFoundError err) {
      _showError(err, "Debugger Error",
                 "Unable to find the JPDA package for the debugger.\n" +
                 "Please make sure either tools.jar or jpda.jar is\n" +
                 "in your classpath when you start DrJava.");
      _setDebugMenuItemsEnabled(false);
    }
  }
  
  /** Display the debugger tab and update the Debug menu accordingly. */
  public void showDebugger() {
    _setDebugMenuItemsEnabled(true);
    _showDebuggerPanel();
  }
  
  /** Hide the debugger tab and update the Debug menu accordingly. */
  public void hideDebugger() {
    _setDebugMenuItemsEnabled(false);
    _hideDebuggerPanel();
  }
  
  private void _showDebuggerPanel() {
    _debugSplitPane.setTopComponent(_docSplitPane);
    _mainSplit.setTopComponent(_debugSplitPane);
    _debugPanel.updateData();
    _lastFocusOwner.requestFocusInWindow();
  }
  
  private void _hideDebuggerPanel() {
    _mainSplit.setTopComponent(_docSplitPane);
    _lastFocusOwner.requestFocusInWindow();
  }
  
  /** ONLY executes in event thread. */
  public void updateStatusField(String text) {
    assert EventQueue.isDispatchThread();
    _statusField.setText(text);
    _statusField.paint(getGraphics());  // force an immediate repaint
  }
  
  /** Updates the status field with the current status of the Definitions Pane. */
  public void updateStatusField() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    String fileName = doc.getCompletePath();
    if (! fileName.equals(_fileTitle)) {
      _fileTitle = fileName;
      setTitle(fileName);
      _model.getDocCollectionWidget().repaint();
    }
    String path = doc.getCompletePath();
    
    String text = "Editing " + path;
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    if (DrJava.getConfig().getSetting(LIGHTWEIGHT_PARSING_ENABLED).booleanValue()) {
//      String temp = _model.getParsingControl().getEnclosingClassName(doc);
//      if ((temp != null) && (temp.length() > 0)) { text = text + " - " + temp; }
    
//    _statusField.setToolTipText("Full path for file: " + path);
    
    if (! _statusField.getText().equals(text)) { 
      _statusField.setText(text); 
      _statusField.paint(getGraphics());  // force immediate painting of the _statusField
    }
  }
  
  /** Prompt the user to select a place to open files from, then load them. Ask the user if they'd like to save 
    * previous changes (if the current document has been modified) before opening.
    * @param jfc the open dialog from which to extract information
    * @return an array of the files that were chosen
    */
  public File[] getOpenFiles(JFileChooser jfc) throws OperationCanceledException {
    int rc = jfc.showOpenDialog(this);
    return getChosenFiles(jfc, rc);
  }
  
  /** Prompt the user to select a place to save the current document. */
  public File getSaveFile(JFileChooser jfc) throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
//    File selection = jfc.getSelectedFile();//_saveChooser.getSelectedFile();
//    if (selection != null) {
//      jfc.setSelectedFile(selection.getParentFile());
//      jfc.setSelectedFile(selection);
//      jfc.setSelectedFile(null);
//    }
    
    OpenDefinitionsDocument active = _model.getActiveDocument();
    
    // Fill in class name
    //if (active.isUntitled()) {
    try {
      String className = active.getFirstTopLevelClassName();
      if (!className.equals("")) {
        jfc.setSelectedFile(new File(jfc.getCurrentDirectory(), className));
      }
    }
    catch (ClassNameNotFoundException e) {
      // Don't set selected file
    }
    
    _saveChooser.removeChoosableFileFilter(_projectFilter);
    _saveChooser.removeChoosableFileFilter(_javaSourceFilter);
    _saveChooser.setFileFilter(_javaSourceFilter);
    int rc = jfc.showSaveDialog(this);
    return getChosenFile(jfc, rc);
  }
  
  /** Returns the current DefinitionsPane. */
  public DefinitionsPane getCurrentDefPane() { return _currentDefPane; }
  
  /** Returns the currently shown error panel if there is one. Otherwise returns null. */
  public ErrorPanel getSelectedErrorPanel() {
    Component c = _tabbedPane.getSelectedComponent();
    if (c instanceof ErrorPanel) return (ErrorPanel) c;
    return null;
  }
  
  /** Returns whether the compiler output tab is currently showing. */
  public boolean isCompilerTabSelected() {
    return _tabbedPane.getSelectedComponent() == _compilerErrorPanel;
  }
  
  /** Returns whether the test output tab is currently showing. */
  public boolean isTestTabSelected() {
    return _tabbedPane.getSelectedComponent() == _junitErrorPanel;
  }
  
  /** Returns whether the JavaDoc output tab is currently showing. */
  public boolean isJavadocTabSelected() {
    return _tabbedPane.getSelectedComponent() == _javadocErrorPanel;
  }
  
  /** Makes sure save and compile buttons and menu items are enabled and disabled appropriately after document
    * modifications.
    */
  private void _installNewDocumentListener(final OpenDefinitionsDocument d) {
    d.addDocumentListener(new DocumentUIListener() {
      public void changedUpdate(DocumentEvent e) { 
        
        // Commented out because the only attributes that affect the status of buttons are inserts and removes
//        Utilities.invokeLater(new Runnable() {
//          public void run() {
//            OpenDefinitionsDocument doc = _model.getActiveDocument();
//            if (doc.isModifiedSinceSave()) {
//              _saveAction.setEnabled(true);
//              if (isDebuggerReady() && _debugPanel.getStatusText().equals(""))
//                _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
//              updateStatusField();
//            }
//          }
//        });
      }
      public void insertUpdate(DocumentEvent e) {
        Utilities.invokeLater(new Runnable() {
          public void run() {
            _saveAction.setEnabled(true);
            if (isDebuggerReady() && _debugPanel.getStatusText().equals(""))
              _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
//            updateStatusField();  // file title not changed by insert operation
          }
        });
      }
      public void removeUpdate(DocumentEvent e) {
        Utilities.invokeLater(new Runnable() {
          public void run() {
            _saveAction.setEnabled(true);
            if (isDebuggerReady() && _debugPanel.getStatusText().equals(""))
              _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
//            updateStatusField();  // file title not changed by remove operation
          }
        });
      }
    });
  }
  
  /** Changes the message text toward the right of the status bar
    * @param msg The message to place in the status bar
    */
  public void setStatusMessage(String msg) { _statusReport.setText(msg); }
  
  /** Sets the message text in the status bar to the null string. */
  public void clearStatusMessage() { _statusReport.setText(""); }
  
  /** Sets the font of the status bar message
    * @param f The new font of the status bar message
    */
  public void setStatusMessageFont(Font f) { _statusReport.setFont(f); }
  
  /** Sets the color of the text in the status bar message
    * @param c The color of the text
    */
  public void setStatusMessageColor(Color c) { _statusReport.setForeground(c); }
  
  // Made package protected rather than private in order to facilitate the ProjectMenuTest.testSaveProject
  void _moveToAuxiliary() {
    // now works with multiple files
    java.util.List<OpenDefinitionsDocument> l = _model.getDocumentNavigator().getSelectedDocuments();
    for (OpenDefinitionsDocument d: l) {
      if (d != null) {
        if (! d.isUntitled()) {
          _model.addAuxiliaryFile(d);
          try{
            _model.getDocumentNavigator().refreshDocument(d, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
            PropertyMaps.ONLY.getProperty("DrJava","drjava.project.files").invalidate();
            PropertyMaps.ONLY.getProperty("DrJava","drjava.included.files").invalidate();
            PropertyMaps.ONLY.getProperty("DrJava","drjava.external.files").invalidate();
          }
          catch(IOException e) { /* do nothing */ }
        }
      }
    }
  }
  
  private void _removeAuxiliary() {
    // now works with multiple files
    
    for(OpenDefinitionsDocument d: _model.getDocumentNavigator().getSelectedDocuments()) {
      // OpenDefinitionsDocument d = _model.getDocumentNavigator().getCurrent();
      if (d != null) {
        if (! d.isUntitled()) {
          _model.removeAuxiliaryFile(d);
          try {
            _model.getDocumentNavigator().refreshDocument(d, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
            PropertyMaps.ONLY.getProperty("DrJava","drjava.project.files").invalidate();
            PropertyMaps.ONLY.getProperty("DrJava","drjava.included.files").invalidate();
            PropertyMaps.ONLY.getProperty("DrJava","drjava.external.files").invalidate();
          }
          catch(IOException e) { /* do nothing */ }
        }
      }
    }
  }
  
  void _moveAllToAuxiliary() {
    // move all external files to auxiliary files
    OpenDefinitionsDocument d;
    Enumeration<OpenDefinitionsDocument> e = _model.getDocumentNavigator().
      getDocumentsInBin(_model.getExternalBinTitle());
    while (e.hasMoreElements()) {
      d = e.nextElement();
      if (d != null) {
        if (! d.isUntitled()) {
          _model.addAuxiliaryFile(d);
          try{
            _model.getDocumentNavigator().refreshDocument(d, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
          }
          catch(IOException ex) { /* do nothing */ }
        }
      }
    }
    PropertyMaps.ONLY.getProperty("DrJava","drjava.project.files").invalidate();
    PropertyMaps.ONLY.getProperty("DrJava","drjava.included.files").invalidate();
    PropertyMaps.ONLY.getProperty("DrJava","drjava.external.files").invalidate();
    Utilities.invokeLater(new Runnable() { 
      public void run() { _model.getDocumentNavigator().setActiveDoc(_model.getActiveDocument()); }
    });
  }
  
  private void _removeAllAuxiliary() {
    // move all auxiliary files to external files
    OpenDefinitionsDocument d;
    Enumeration<OpenDefinitionsDocument> e = _model.getDocumentNavigator().
      getDocumentsInBin(_model.getAuxiliaryBinTitle());
    while (e.hasMoreElements()) {
      d = e.nextElement();
      if (d != null) {
        if (! d.isUntitled()) {
          _model.removeAuxiliaryFile(d);
          try{
            _model.getDocumentNavigator().refreshDocument(d, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
          }
          catch(IOException ex) { /* do nothing */ }
        }
      }
    }
    PropertyMaps.ONLY.getProperty("DrJava","drjava.project.files").invalidate();
    PropertyMaps.ONLY.getProperty("DrJava","drjava.included.files").invalidate();
    PropertyMaps.ONLY.getProperty("DrJava","drjava.external.files").invalidate();
    Utilities.invokeLater(new Runnable() { 
      public void run() { _model.getDocumentNavigator().setActiveDoc(_model.getActiveDocument()); }
    });
  }
  
  private void _new() { 
    updateStatusField("Creating a new Untitled Document");
    _model.newFile(); 
  }
  
  private void _open() {
    updateStatusField("Opening File");
    open(_openSelector); 
  }
  
  private void _openFolder() { 
    openFolder(_folderChooser); 
  }
  
  
  private void _openFileOrProject() {
    try {
      final File[] fileList = _openFileOrProjectSelector.getFiles();
      
      FileOpenSelector fos = new FileOpenSelector() {
        public File[] getFiles() { return fileList; }
      };
      
      if (_openChooser.getFileFilter().equals(_projectFilter)) openProject(fos);
      else open(fos);
    }
    catch(OperationCanceledException oce) { /* do nothing */ }
  }
  
  /** Puts the given text into the current definitions pane at the current caret position.  */
  private void _putTextIntoDefinitions(String text) {
    int caretPos = _currentDefPane.getCaretPosition();
    
    try { _model.getActiveDocument().insertString(caretPos, text, null); }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }
  
  /** Sets the left navigator pane to the correct component as dictated by the model. */
  private void _resetNavigatorPane() {
    if (_model.getDocumentNavigator() instanceof JTreeSortNavigator) {
      JTreeSortNavigator<?> nav = (JTreeSortNavigator<?>)_model.getDocumentNavigator();
      nav.setDisplayManager(getNavPaneDisplayManager());
      nav.setRootIcon(_djProjectIcon);
    }
    _docSplitPane.remove(_docSplitPane.getLeftComponent());
    _docSplitPane.setLeftComponent(new JScrollPane(_model.getDocumentNavigator().asContainer()));
    Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
    _model.getDocCollectionWidget().setFont(doclistFont);
    _updateNormalColor();
    _updateBackgroundColor();
  }
  
  /** Asks the user to select the project file to open and starts the process of opening the project. */
  private void _openProject() { openProject(_openProjectSelector); }
  
  public void openProject(FileOpenSelector projectSelector) {
    
    try { 
      final File[] files = projectSelector.getFiles();
      if (files.length < 1)
        throw new IllegalStateException("Open project file selection not canceled but no project file was selected.");
      final File file = files[0];
      
      updateStatusField("Opening project " + file);
      
      try {
        hourglassOn();
        // make sure there are no open projects
        if (!_model.isProjectActive() || (_model.isProjectActive() && _closeProject())) _openProjectHelper(file);
      }
      catch(Exception e) { e.printStackTrace(System.out); }
      finally { hourglassOff(); } 
    }
    catch(OperationCanceledException oce) { /* do nothing, we just won't open anything */ }
    
  }  
  
  /** Oversees the opening of the project by delegating to the model to parse and initialize the project 
    * while resetting the navigator pane and opening up the files itself.
    * @param projectFile the file of the project to open
    */
  private void _openProjectHelper(File projectFile) {
    _currentProjFile = projectFile;
    try {
      _mainListener.resetFNFCount();
      _model.openProject(projectFile);
      if (_mainListener.someFilesNotFound()) _model.setProjectChanged(true);
      _completeClassList = new ArrayList<GoToFileListEntry>(); // reset auto-completion list
      EventQueue.invokeLater(new Runnable() { public void run() { _model.addToBrowserHistory(); } });
    }
    catch(MalformedProjectFileException e) {
      _showProjectFileParseError(e); // add to an error adapter
      return;
    }
    catch(FileNotFoundException e) {
      _showFileNotFoundError(e); // add to an error adapter
      return;
    }
    catch(IOException e) {
      _showIOError(e); // add to an error adapter
      return;
    }
  }
  
  private void _openProjectUpdate() {
    if (_model.isProjectActive()) {
      _closeProjectAction.setEnabled(true);
      _saveProjectAction.setEnabled(true);
      _saveProjectAsAction.setEnabled(true);
      _exportProjectInOldFormatAction.setEnabled(true);
      _projectPropertiesAction.setEnabled(true);
//      _junitProjectAction.setEnabled(true);
      _junitProjectAction.setEnabled(true);
//      _compileOpenProjectAction.setEnabled(true);
      _compileProjectAction.setEnabled(true);
      _jarProjectAction.setEnabled(true);
      if (_model.getBuildDirectory() != null) _cleanAction.setEnabled(true);
      _autoRefreshAction.setEnabled(true);
      _resetNavigatorPane();
//      _compileButton.setToolTipText("<html>Compile all documents in the project.source tree<br>" +
//      "Auxiliary and external files are excluded.</html>");
    }
  }
  
  boolean _closeProject() {
    _completeClassList = new ArrayList<GoToFileListEntry>(); // reset auto-completion list
    _autoImportClassList = new ArrayList<JavaAPIListEntry>(); // reset auto-import list
    return _closeProject(false);
  }
  
  
  /** Closes project when DrJava is not in the process of quitting.
    * @return true if the project is closed, false if cancelled.
    */
  boolean closeProject() {
    updateStatusField("Closing current project");
    return _closeProject();
  }
  
  /** Saves the project file; closes all open project files; and calls _model.closeProject(quitting) the 
    * clean up the state of the global model.  It also restores the list view navigator
    * @ param quitting whether the project is being closed as part of quitting DrJava
    * @return true if the project is closed, false if cancelled
    */
  boolean _closeProject(boolean quitting) {
    if (_checkProjectClose()) {
      List<OpenDefinitionsDocument> projDocs = _model.getProjectDocuments();
//      System.err.println("projDocs = " + projDocs);
      boolean couldClose = _model.closeFiles(projDocs);
      if (! couldClose) return false;
      
      disableFindAgainOnClose(projDocs); // disable "Find Again" for documents that are closed

      // project file has been saved and all files closed
      if (quitting) return true;
      _model.closeProject(quitting);
      
      Component renderer = _model.getDocumentNavigator().getRenderer();
      new ForegroundColorListener(renderer);
      new BackgroundColorListener(renderer);
      _resetNavigatorPane();
      if (_model.getDocumentCount() == 1) _model.setActiveFirstDocument();
      _closeProjectAction.setEnabled(false);
      _saveProjectAction.setEnabled(false);
      _saveProjectAsAction.setEnabled(false);
      _exportProjectInOldFormatAction.setEnabled(false);
      _projectPropertiesAction.setEnabled(false);
//      _junitProjectAction.setEnabled(false);
      _jarProjectAction.setEnabled(false);
      _junitProjectAction.setEnabled(false);
//      _compileOpenProjectAction.setEnabled(false);
      _compileProjectAction.setEnabled(false);
      _setUpContextMenus();
      _currentProjFile = FileOps.NULL_FILE;
//      _compileButton.setToolTipText("Compile all open documents");
      return true;
    }
    else return false;  // Project closing cancelled in _checkProjectClose dialog
  }
  
  private void _configureBrowsing() {
    BrowserHistoryManager bm = _model.getBrowserHistoryManager();
    if (bm.getRegions().isEmpty()) {
      _browseForwardAction.setEnabled(false);
      _browseBackAction.setEnabled(false);
      return;
    }
    _browseForwardAction.setEnabled(true);
    _browseBackAction.setEnabled(true);
    if (bm.isCurrentRegionFirst()) _browseBackAction.setEnabled(false);
    if (bm.isCurrentRegionLast()) _browseForwardAction.setEnabled(false);
  }
  
  private boolean _checkProjectClose() {
    if (_model.isProjectChanged()) {
      String fname = _model.getProjectFile().getName();
      String text = fname + " has been modified. Would you like to save it?";
      int rc = 
        JOptionPane.showConfirmDialog(MainFrame.this, text, "Save " + fname + "?", JOptionPane.YES_NO_CANCEL_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _saveProject();
          return true;
        case JOptionPane.NO_OPTION:
          return true;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);        
      }
    } 
    return true;
  }
  
  public File getCurrentProject() { return _currentProjFile;  }
  
  /** Opens all the files returned by the FileOpenSelector prompting the user to handle the cases where files are 
    * already open, files are missing, or the action was canceled by the user
    * @param openSelector the selector that returns the files to open
    */
  public void open(FileOpenSelector openSelector) {
    try {
      hourglassOn();
      _model.openFiles(openSelector);
    }
    catch (AlreadyOpenException aoe) {
      OpenDefinitionsDocument[] openDocs = aoe.getOpenDocuments();
      for(OpenDefinitionsDocument openDoc : openDocs) {
        String fileName;
        try { fileName = openDoc.getFile().getName(); }
        catch (IllegalStateException ise) {
          // Can't happen: this open document must have a file
          throw new UnexpectedException(ise);
        }
        catch (FileMovedException fme) {
          // File was deleted, but use the same name anyway
          fileName = fme.getFile().getName();
        }
        try {
          File f = openDoc.getFile();
          if (! _model.inProject(f)) _recentFileManager.updateOpenFiles(f);
        }
        catch (IllegalStateException ise) {
          // Impossible: saved => has a file
          throw new UnexpectedException(ise);
        }
        catch (FileMovedException fme) {
          File f = fme.getFile();
          // Recover, show it in the list anyway
          if (! _model.inProject(f))
            _recentFileManager.updateOpenFiles(f);
        }
      }
    }  
    catch (OperationCanceledException oce) { /* do not open file */ }
    catch (FileNotFoundException fnf) { 
      _showFileNotFoundError(fnf); 
    }
    catch (IOException ioe) { _showIOError(ioe); }
    finally { hourglassOff(); }
  }
  
  
  /** Opens all the files in the directory returned by the FolderSelector.
    * @param chooser the selector that returns the files to open
    */
  public void openFolder(DirectoryChooser chooser) {
    String type = "'." + DrJavaRoot.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)] + "' ";
    chooser.setDialogTitle("Open All " + type + "Files in ...");
    
    File openDir = FileOps.NULL_FILE;
    try { 
      File activeFile = _model.getActiveDocument().getFile();
      if (activeFile != null) openDir = activeFile.getParentFile();
      else openDir = _model.getProjectRoot();
    }
    catch(FileMovedException e) { /* do nothing */ }
    
    int result = chooser.showDialog(openDir);
    if (result != DirectoryChooser.APPROVE_OPTION)  return; // canceled or error
    
    File dir = chooser.getSelectedDirectory();
    boolean rec = _openRecursiveCheckBox.isSelected();
    DrJava.getConfig().setSetting(OptionConstants.OPEN_FOLDER_RECURSIVE, Boolean.valueOf(rec));
    updateStatusField("Opening folder " + dir);
    _openFolder(dir, rec);
  }
  
  /** Opens all the files in the specified directory; it opens all files in nested folders if rec is true.
    * @param dir the specified directory
    * @param rec true if files in nested folders should be opened
    */
  private void _openFolder(File dir, boolean rec) {
    hourglassOn();
    try { _model.openFolder(dir, rec); }
    catch(AlreadyOpenException e) { /* do nothing */ }
    catch(IOException e) { _showIOError(e); }
    catch(OperationCanceledException oce) { /* do nothing */ }
    finally { hourglassOff(); }
  }
  
  /** Delegates directly to the model to close the active document */
  private void _close() {
    //    LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    //    l.add(_model.getActiveDocument());
    //    _model.closeFiles(l);
    
    // this works with multiple selected files now
    java.util.List<OpenDefinitionsDocument> l = _model.getDocumentNavigator().getSelectedDocuments();    
    boolean queryNecessary = false; // is a query necessary because the files are project or auxiliary files?
    for (OpenDefinitionsDocument doc: l) {
      if ((_model.isProjectActive() && doc.inProjectPath()) || doc.isAuxiliaryFile()) {
        queryNecessary = true;
        break;
      }
    }
    if (queryNecessary) {
      int rc;
      String fileName = null;
      Object[] options = {"Yes", "No"};
      if (l.size()==1) {
        OpenDefinitionsDocument doc = l.get(0);
        try {
          if (doc.isUntitled()) fileName = "File";
          else fileName = _model.getActiveDocument().getFile().getName();
        }
        catch(FileMovedException e) { fileName = e.getFile().getName(); }
        String text = "Closing this file will permanently remove it from the current project." + 
          "\nAre you sure that you want to close this file?";
        
        rc = JOptionPane.showOptionDialog(MainFrame.this, text,"Close " + fileName + "?", JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
      }
      else {
        fileName = l.size()+" files";
        String text = "Closing these "+fileName+" will permanently remove them from the current project." + 
          "\nAre you sure that you want to close these files?";
        
        rc = JOptionPane.showOptionDialog(MainFrame.this, text, "Close "+l.size()+" files?", JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
      }
      if (rc != JOptionPane.YES_OPTION) return;
      
      updateStatusField("Closing " + fileName);
      _model.setProjectChanged(true);
    }

    disableFindAgainOnClose(l); // disable "Find Again" for documents that are closed
    
    // Either this is an external file or user actually wants to close it
    for(OpenDefinitionsDocument doc: l) {
      _model.closeFile(doc);
    }
  }
  
  private void _closeFolder() {
    OpenDefinitionsDocument d;
    Enumeration<OpenDefinitionsDocument> e = _model.getDocumentNavigator().getDocuments();
    final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    
    if (_model.getDocumentNavigator().isGroupSelected()) {
      while (e.hasMoreElements()) {
        d = e.nextElement();
        if (_model.getDocumentNavigator().isSelectedInGroup(d)) { l.add(d); }
      }
      disableFindAgainOnClose(l); // disable "Find Again" for documents that are closed
      _model.closeFiles(l);
      if (! l.isEmpty()) _model.setProjectChanged(true);
    }
  }
  
  private void _printDefDoc() {
    try {
      _model.getActiveDocument().print();
    }
    catch (FileMovedException fme) {
      _showFileMovedError(fme);
    }
    catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
    catch (BadLocationException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }
  
  private void _printConsole() {
    try {
      _model.getConsoleDocument().print();
    }
    catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }
  
  private void _printInteractions() {
    try {
      _model.getInteractionsDocument().print();
    }
    catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }
  
  /** Opens a new PrintPreview frame. */
  private void _printDefDocPreview() {
    try {
      _model.getActiveDocument().preparePrintJob();
      new PreviewDefDocFrame(_model, this);
    }
    catch (FileMovedException fme) {
      _showFileMovedError(fme);
    }
    catch (BadLocationException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    }
    catch (IllegalStateException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    }
  }
  
  private void _printConsolePreview() {
    try {
      _model.getConsoleDocument().preparePrintJob();
      new PreviewConsoleFrame(_model, this, false);
    }
    catch (IllegalStateException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    }
  }
  
  private void _printInteractionsPreview() {
    try {
      _model.getInteractionsDocument().preparePrintJob();
      new PreviewConsoleFrame(_model, this, true);
    }
    catch (IllegalStateException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    }
  }
  
  private void _pageSetup() {
    PrinterJob job = PrinterJob.getPrinterJob();
    _model.setPageFormat(job.pageDialog(_model.getPageFormat()));
  }
  
  //Called by testCases
  void closeAll() { _closeAll(); }
  
  private void _closeAll() {
    updateStatusField("Closing All Files");
    if (!_model.isProjectActive() || _model.isProjectActive() && _closeProject())    
      _model.closeAllFiles();
  }
  
  private boolean _save() {
    updateStatusField("Saving File");
    try {
      // now works with multiple files
      List<OpenDefinitionsDocument> l = _model.getDocumentNavigator().getSelectedDocuments();
      boolean success = false;
      for(OpenDefinitionsDocument doc: l) {
        if (doc.saveFile(_saveSelector)) {
          getDefPaneGivenODD(doc).hasWarnedAboutModified(false);
          success = true;
        }
      }
      // _model.refreshActiveDocument() is not sufficient here; it does not re-select
      // the same document in flat-file mode
      _model.setActiveDocument(_model.getActiveDocument());
      return success;
//      if (_model.getActiveDocument().saveFile(_saveSelector)) {
//        _currentDefPane.hasWarnedAboutModified(false); 
//        
//        /**This highlights the document in the navigator */
//        _model.setActiveDocument(_model.getActiveDocument());
//        
//        return true;
//      }
//      else return false;
    }
    catch (IOException ioe) { 
      _showIOError(ioe);
      return false;
    }
  }
  
  private boolean _saveAs() {
    updateStatusField("Saving File Under New Name");
    try {
      boolean toReturn = _model.getActiveDocument().saveFileAs(_saveAsSelector);
      /** this highlights the document in the navigator */
      _model.setActiveDocument(_model.getActiveDocument());
      return toReturn;
    }
    catch (IOException ioe) {
      _showIOError(ioe);
      return false;
    }
  }
  
  private boolean _rename() {
    try {
      if (!_model.getActiveDocument().fileExists()) return _saveAs();
      else {
        File fileToDelete;
        try {
          fileToDelete = _model.getActiveDocument().getFile();
        } catch (FileMovedException fme) {
          return _saveAs();
        }
        boolean toReturn = _model.getActiveDocument().saveFileAs(_saveAsSelector);
        /** Delete the old file if save was successful */
        if (toReturn && !_model.getActiveDocument().getFile().equals(fileToDelete)) fileToDelete.delete();
        /** this highlights the document in the navigator */
        _model.setActiveDocument(_model.getActiveDocument());
        return toReturn;
      }
    }
    catch (IOException ioe) {
      _showIOError(ioe);
      return false;
    }
  }  
  
  /* Package private to allow use in MainFrameTest. */
  void _saveAll() {
    hourglassOn();
    try {
      if (_model.isProjectActive()) _saveProject();
      _model.saveAllFiles(_saveSelector);
    }
    catch (IOException ioe) { _showIOError(ioe); }
    finally { hourglassOff(); }
  }

  void _saveAllOld() {
    hourglassOn();
    File file = _currentProjFile;
    try {
      if (_model.isProjectActive()) {
        if (file.getName().indexOf(".") == -1) file = new File (file.getAbsolutePath() + OLD_PROJECT_FILE_EXTENSION);
        _model.exportOldProject(file, gatherProjectDocInfo());
      }
      _model.saveAllFiles(_saveSelector);
    }
    catch (IOException ioe) { _showIOError(ioe); }
    finally { hourglassOff(); }
  }
  
  // Called by the ProjectPropertiesFrame
  void saveProject() { _saveProject(); }
  
  private void _saveProject() {
    //File file = _model.getProjectFile();
    _saveProjectHelper(_currentProjFile);
  }
  
  /** Edit project frame.  */  
  private void _editProject() {
    ProjectPropertiesFrame ppf = new ProjectPropertiesFrame(this);
    ppf.setVisible(true);
    ppf.reset();
    ppf.toFront();  // ppf actions save state of ppf in global model
  }
  
  /** Closes all files and makes a new project. */
  private void _newProject() {
    
    _closeProject(true);  // suppress resetting interactions; it will be done in _model.newProject() below
    _saveChooser.setFileFilter(_projectFilter);
    int rc = _saveChooser.showSaveDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION) {      
      File pf = _saveChooser.getSelectedFile();  // project file
      if (pf.exists() && !_verifyOverwrite()) { return; }
      
      String fileName = pf.getName();
      // ensure that saved file has extesion ".xml"
      if (! fileName.endsWith(".xml")) {
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex == -1) pf = new File (pf.getAbsolutePath() + ".xml");
        else pf = new File(fileName.substring(0, lastIndex) + ".xml");
      }
      
      _model.createNewProject(pf); // sets model to a new FileGroupingState for project file pf
//      ProjectPropertiesFrame ppf = new ProjectPropertiesFrame(MainFrame.this, file);
//      ppf.saveSettings();  // Saves new project profile in global model
      _editProject();  // edits the properties of the new FileGroupingState
      try { _model.configNewProject(); }  // configures the new project in the model
      catch(IOException e) { throw new UnexpectedException(e); }
      _currentProjFile = pf;
    }
  }
  
  /** Pops up the _saveChooser dialog, asks the user for a new project file name, and sets the project file to the 
    * specified file.  Nothing is written in the file system; this action is performed by a subsequent _saveAll().
    * @return false if the user canceled the action */
  private boolean _saveProjectAs() {
    
//    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    _saveChooser.removeChoosableFileFilter(_projectFilter);
    _saveChooser.removeChoosableFileFilter(_javaSourceFilter);
    _saveChooser.setFileFilter(_projectFilter);
//    File selection = _saveChooser.getSelectedFile();
//    if (selection != null) {  // what is this block of commands for?
//      _saveChooser.setSelectedFile(selection.getParentFile());
//      _saveChooser.setSelectedFile(selection);
//      _saveChooser.setSelectedFile(null);
//    }
    
    if (_currentProjFile != FileOps.NULL_FILE) _saveChooser.setSelectedFile(_currentProjFile);
    
    int rc = _saveChooser.showSaveDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION) {
      File file = _saveChooser.getSelectedFile();
      if (! file.exists() || _verifyOverwrite()) { 
        _model.setProjectFile(file);
        _currentProjFile = file;
      }
    }
    
    return (rc == JFileChooser.APPROVE_OPTION);
  }
  
  void _saveProjectHelper(File file) {
    try {
      if (file.getName().indexOf(".") == -1) file = new File (file.getAbsolutePath() + PROJECT_FILE_EXTENSION);
      String fileName = file.getCanonicalPath();
      if (fileName.endsWith(OLD_PROJECT_FILE_EXTENSION)) {
        String text = "The project will be saved in XML format." + 
          "\nDo you want to change the project file's extension to \""+PROJECT_FILE_EXTENSION+"\"?";
        
        Object[] options = {"Change to \""+PROJECT_FILE_EXTENSION+"\"", "Keep \"" + 
          fileName.substring(fileName.lastIndexOf('.'))+"\""};  
        int rc = 1;
        if (!Utilities.TEST_MODE) {
          rc = JOptionPane.showOptionDialog(MainFrame.this, text, "Change Extension?", JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        if (rc == 0) {
          fileName = fileName.substring(0,fileName.length() - OLD_PROJECT_FILE_EXTENSION.length()) + 
            PROJECT_FILE_EXTENSION;
          file = new File(fileName);
          if (! file.exists() || _verifyOverwrite()) { 
            _model.setProjectFile(file);
            _currentProjFile = file;
          }
        }
      }
      _model.saveProject(file, gatherProjectDocInfo());
//      if (!(_model.getDocumentNavigator() instanceof JTreeSortNavigator)) {
//        _openProjectHelper(file);
//      }    
    }
    catch(IOException ioe) { _showIOError(ioe); }
    _recentProjectManager.updateOpenFiles(file);
    _model.setProjectChanged(false);
  }
  
  public Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> gatherProjectDocInfo() {
    Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> map =
      new Hashtable<OpenDefinitionsDocument,DocumentInfoGetter>();
    List<OpenDefinitionsDocument> docs = _model.getProjectDocuments();
    for(OpenDefinitionsDocument doc: docs) {
      map.put(doc, _makeInfoGetter(doc));
    }
    return map;
  }
  /** Gets the information to be save for a project document.
    * Implementation may change if the scroll/selection information is later stored in a place other than the
    * definitions pane.  Hopefully this info will eventually be backed up in the OpenDefinitionsDocument in which 
    * case all this code should be refactored into the model's _saveProject method
    */
  private DocumentInfoGetter _makeInfoGetter(final OpenDefinitionsDocument doc) {
    JScrollPane s = _defScrollPanes.get(doc);
    if (s == null) {
      s = _createDefScrollPane(doc);
    }
    final JScrollPane scroller = s;
    final DefinitionsPane pane = (DefinitionsPane)scroller.getViewport().getView();
    return new DocumentInfoGetter() {
      public Pair<Integer,Integer> getSelection() {
        Integer selStart = new Integer(pane.getSelectionStart());
        Integer selEnd = new Integer(pane.getSelectionEnd());
        if ((selStart==0)&&(selEnd==0)) return new Pair<Integer,Integer>(pane.getCaretPosition(),pane.getCaretPosition());
        if (pane.getCaretPosition() == selStart) return new Pair<Integer,Integer>(selEnd,selStart);
        return new Pair<Integer,Integer>(selStart,selEnd);
      }
      public Pair<Integer,Integer> getScroll() {
        Integer scrollv = new Integer(pane.getVerticalScroll());
        Integer scrollh = new Integer(pane.getHorizontalScroll());
        return new Pair<Integer,Integer>(scrollv,scrollh); 
      }
      public File getFile() { return doc.getRawFile(); }
      public String getPackage() { return doc.getPackageName(); }
      public boolean isActive() { return _model.getActiveDocument() == doc; }
      public boolean isUntitled() { return doc.isUntitled(); }
    };
  }
  
  private void _revert() {
    // this works with multiple selected files now
    java.util.List<OpenDefinitionsDocument> l = _model.getDocumentNavigator().getSelectedDocuments();
    for(OpenDefinitionsDocument d: l) { _revert(d); }
  }
  
  private void _revert(OpenDefinitionsDocument doc) {
    try {
      doc.revertFile();
    }
    catch (FileMovedException fme) {
      _showFileMovedError(fme);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }
  
  /**
   private void _revertAll() {
   try {
   _model.revertAllFiles();
   }
   catch (FileMovedException fme) {
   _showFileMovedError(fme);
   }
   catch (IOException ioe) {
   _showIOError(ioe);
   }
   }
   */
  
  private void _quit() {
//    AbstractGlobalModel._log.log("MainFrame.quit() called");
    if (_promptBeforeQuit) {
      String title = "Quit DrJava?";
      String message = "Are you sure you want to quit DrJava?";
      ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(MainFrame.this, title, message);
      int rc = dialog.show();
      if (rc != JOptionPane.YES_OPTION) return;
      else {
        // Only remember the checkbox if they say yes
        if (dialog.getCheckBoxValue() == true) {
          DrJava.getConfig().setSetting(QUIT_PROMPT, Boolean.FALSE);
        }
      }
    }
    _executeExternalDialog.setVisible(false);
    // tried passing false here. seemed to help with bug
    // [ 1478796 ] DrJava Does Not Shut Down With Project Open
    // on HP tc1100 and Toshiba Portege tablet PCs, but did not help in all cases
    if (! _closeProject(true)) { return; /* if user pressed cancel, do not quit */ }
    
    _recentFileManager.saveRecentFiles();
    _recentProjectManager.saveRecentFiles();
    if (! _model.closeAllFilesOnQuit()) { return; /* if user pressed cancel, do not quit */ }
    _storePositionInfo();
    
    // Save recent files, but only if there wasn't a problem at startUp
    // (Don't want to overwrite a custom config file with a simple typo.)
    if (! DrJava.getConfig().hadStartupException()) {
      try { DrJava.getConfig().saveConfiguration(); }
      catch (IOException ioe) { _showIOError(ioe); }
    }
    //DrJava.consoleOut().println("Quitting DrJava...");
    dispose();    // Free GUI elements of this frame
    _model.quit();
  }
  
  private void _forceQuit() { _model.forceQuit(); }
  
  /** Stores the current position and size info for window and panes to the config framework. */
  private void _storePositionInfo() {
    Configuration config = DrJava.getConfig();
    
    // Window bounds.
    if (config.getSetting(WINDOW_STORE_POSITION).booleanValue()) {
      Rectangle bounds = getBounds();
      config.setSetting(WINDOW_HEIGHT, new Integer(bounds.height));
      config.setSetting(WINDOW_WIDTH, new Integer(bounds.width));
      config.setSetting(WINDOW_X, new Integer(bounds.x));
      config.setSetting(WINDOW_Y, new Integer(bounds.y));
      config.setSetting(WINDOW_STATE, new Integer(getExtendedState()));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(WINDOW_HEIGHT, WINDOW_HEIGHT.getDefault());
      config.setSetting(WINDOW_WIDTH, WINDOW_WIDTH.getDefault());
      config.setSetting(WINDOW_X, WINDOW_X.getDefault());
      config.setSetting(WINDOW_Y, WINDOW_Y.getDefault());
      config.setSetting(WINDOW_STATE, WINDOW_STATE.getDefault());
    }
    
    // "Go to File" dialog position and size.
    if ((DrJava.getConfig().getSetting(DIALOG_GOTOFILE_STORE_POSITION).booleanValue())
          && (_gotoFileDialog != null) && (_gotoFileDialog.getFrameState() != null)) {
      config.setSetting(DIALOG_GOTOFILE_STATE, (_gotoFileDialog.getFrameState().toString()));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(DIALOG_GOTOFILE_STATE, DIALOG_GOTOFILE_STATE.getDefault());
    }
    
    // "Open Javadoc" dialog position and size.
    if ((DrJava.getConfig().getSetting(DIALOG_OPENJAVADOC_STORE_POSITION).booleanValue())
          && (_openJavadocDialog != null) && (_openJavadocDialog.getFrameState() != null)) {
      config.setSetting(DIALOG_OPENJAVADOC_STATE, (_openJavadocDialog.getFrameState().toString()));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(DIALOG_OPENJAVADOC_STATE, DIALOG_OPENJAVADOC_STATE.getDefault());
    }    
    
    // "Complete Word" dialog position and size.
    if ((DrJava.getConfig().getSetting(DIALOG_COMPLETE_WORD_STORE_POSITION).booleanValue())
          && (_completeWordDialog != null) && (_completeWordDialog.getFrameState() != null)) {
      config.setSetting(DIALOG_COMPLETE_WORD_STATE, (_completeWordDialog.getFrameState().toString()));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(DIALOG_COMPLETE_WORD_STATE, DIALOG_COMPLETE_WORD_STATE.getDefault());
    }
    
    // "Create Jar from Project" dialog position and size.   
    if ((DrJava.getConfig().getSetting(DIALOG_JAROPTIONS_STORE_POSITION).booleanValue())
          && (_jarOptionsDialog != null) && (_jarOptionsDialog.getFrameState() != null)) {
      config.setSetting(DIALOG_JAROPTIONS_STATE, (_jarOptionsDialog.getFrameState().toString()));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(DIALOG_JAROPTIONS_STATE, DIALOG_JAROPTIONS_STATE.getDefault());
    }
    
    // Panel heights.
    if (_showDebugger) config.setSetting(DEBUG_PANEL_HEIGHT, new Integer(_debugPanel.getHeight()));
    
    // Doc list width.
    config.setSetting(DOC_LIST_WIDTH, new Integer(_docSplitPane.getDividerLocation()));
  }
  
  private void _cleanUpForCompile() { if (isDebuggerReady()) _model.getDebugger().shutdown(); }
  
  private void _compile() {
    // now works with multiple files
    _cleanUpForCompile();
    hourglassOn();
    try {
//      final OpenDefinitionsDocument doc = _model.getActiveDocument();
      try { _model.getCompilerModel().compile(_model.getDocumentNavigator().getSelectedDocuments()); }
      catch (FileMovedException fme) { _showFileMovedError(fme); }
      catch (IOException ioe) { _showIOError(ioe); }
    }
    finally { hourglassOff();}
//    update(getGraphics());
  }
  
  private void _compileFolder() {
    _cleanUpForCompile();
    hourglassOn();
    try {
      OpenDefinitionsDocument d;
      Enumeration<OpenDefinitionsDocument> e = _model.getDocumentNavigator().getDocuments();
      final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
      if (_model.getDocumentNavigator().isGroupSelected()) {
        while (e.hasMoreElements()) {
          d = e.nextElement();
          if (_model.getDocumentNavigator().isSelectedInGroup(d)) l.add(d);
        }
        
//        new Thread("Compile Folder") {
//          public void run() {
        try { _model.getCompilerModel().compile(l); }
        catch (FileMovedException fme) { _showFileMovedError(fme); }
        catch (IOException ioe) { _showIOError(ioe); }
//          }
//        }.start();
      }
    }
    finally { hourglassOff(); }
//    update(getGraphics()); 
  }
  
  private void _compileProject() { 
    _cleanUpForCompile();
//    new Thread("Compile All") {
//      public void run() {
    hourglassOn();
    try { _model.getCompilerModel().compileProject(); }
    catch (FileMovedException fme) { _showFileMovedError(fme); }
    catch (IOException ioe) { _showIOError(ioe); }
    finally { hourglassOff();}
//      }
//    }.start();
//    update(getGraphics()); 
  }
  
  private void _compileAll() {
    _cleanUpForCompile();
//    new Thread("Compile All") {
//      public void run() {
    hourglassOn();
    try { _model.getCompilerModel().compileAll(); }
    catch (FileMovedException fme) { _showFileMovedError(fme); }
    catch (IOException ioe) { _showIOError(ioe); }
    finally { hourglassOff();}
//      }
//    }.start();
//    update(getGraphics()); 
  }
  
  private boolean showCleanWarning() {
    if (DrJava.getConfig().getSetting(PROMPT_BEFORE_CLEAN).booleanValue()) {
      String buildDirTxt = "";
      try {
        buildDirTxt = _model.getBuildDirectory().getCanonicalPath();
      }
      catch (Exception e) {
        buildDirTxt = _model.getBuildDirectory().getPath();
      }
      ConfirmCheckBoxDialog dialog =
        new ConfirmCheckBoxDialog(MainFrame.this,
                                  "Clean Build Directory?",
                                  "Cleaning your build directory will delete all\n" + 
                                  "class files and empty folders within that directory.\n" + 
                                  "Are you sure you want to clean\n" + 
                                  buildDirTxt + "?",
                                  "Do not show this message again");
      int rc = dialog.show();
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _saveAll();
          // Only remember checkbox if they say yes
          if (dialog.getCheckBoxValue()) {
            DrJava.getConfig().setSetting(PROMPT_BEFORE_CLEAN, Boolean.FALSE);
          }
          return true;
        case JOptionPane.NO_OPTION:
          return false;
        case JOptionPane.CANCEL_OPTION:
          return false;
        case JOptionPane.CLOSED_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
      }
    }
    return true;
  }
  
//  private void _clean() {
//    final SwingWorker worker = new SwingWorker() {
//      public Object construct() {
//        if (showCleanWarning()) {
//          try {
//            hourglassOn();
//            _model.cleanBuildDirectory();
//          }
//          catch (FileMovedException fme) { _showFileMovedError(fme); }
//          catch (IOException ioe) { _showIOError(ioe); }
//          finally { hourglassOff(); }
//        }
//        return null;
//      }
//    };
//    worker.start();
//  }
  
  private void _clean() {
    _model.cleanBuildDirectory(); // The model performs this as an AsyncTask
  }
  
  /** List with entries for the complete dialog. */
  ArrayList<GoToFileListEntry> _completeClassList = new ArrayList<GoToFileListEntry>();
  
  /** List with entries for the auto-import dialog. */
  ArrayList<JavaAPIListEntry> _autoImportClassList = new ArrayList<JavaAPIListEntry>();
  
  /** Scan the build directory for class files and update the auto-completion list. */
  private void _scanClassFiles() {
    Thread t = new Thread(new Runnable() {
      public void run() {
        File buildDir = _model.getBuildDirectory();
        HashSet<GoToFileListEntry> hs = new HashSet<GoToFileListEntry>();
        HashSet<JavaAPIListEntry> hs2 = new HashSet<JavaAPIListEntry>();
        if (buildDir!=null) {
          List<File> classFiles = _model.getClassFiles();
          DummyOpenDefDoc dummyDoc = new DummyOpenDefDoc();
          for(File f: classFiles) {
            String s = f.toString();
            if (s.lastIndexOf(java.io.File.separatorChar)>=0) {
              s = s.substring(s.lastIndexOf(java.io.File.separatorChar)+1);
            }
            s = s.substring(0, s.lastIndexOf(".class"));
            s = s.replace('$', '.');
            int pos = 0;
            boolean ok = true;
            while((pos=s.indexOf('.', pos)) >= 0) {
              if ((s.length()<=pos+1) || (Character.isDigit(s.charAt(pos+1)))) {
                ok = false;
                break;
              }
              ++pos;
            }
            if (ok) {
              if (s.lastIndexOf('.') >= 0) {
                s = s.substring(s.lastIndexOf('.') + 1);
              }
              GoToFileListEntry entry = new GoToFileListEntry(dummyDoc, s);
              hs.add(entry);
              try {
                File rel = FileOps.makeRelativeTo(f, buildDir);
                String full = rel.toString().replace(java.io.File.separatorChar, '.');
                full = full.substring(0, full.lastIndexOf(".class"));
                if (full.indexOf('$')<0) {
                  // no $ in the name means not an inner class
                  // we do not support inner classes, because that would mean
                  // having to determine public static scope
                  hs2.add(new JavaAPIListEntry(s, full, null));
                }
              }
              catch(IOException ioe) { /* ignore, just don't add this one */ }
              catch(SecurityException se) { /* ignore, just don't add this one */ }
            }
          }
        }
        _completeClassList = new ArrayList<GoToFileListEntry>(hs);
        _autoImportClassList = new ArrayList<JavaAPIListEntry>(hs2);
      }
    });
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
  }
  
  private void _runProject() {
    if (_model.isProjectActive()) {
      try {
        final File f = _model.getMainClass();
        if (f != null) {
          updateStatusField("Running Open Project");
          OpenDefinitionsDocument doc = _model.getDocumentForFile(f);
          doc.runMain();
        }
      }
      catch (ClassNameNotFoundException e) {
        // Display a warning message if a class name can't be found.
        String msg =
          "DrJava could not find the top level class name in the\n" +
          "current document, so it could not run the class.  Please\n" +
          "make sure that the class is properly defined first.";
        
        JOptionPane.showMessageDialog(MainFrame.this, msg, "No Class Found", JOptionPane.ERROR_MESSAGE);
      }
      catch (FileMovedException fme) { _showFileMovedError(fme); }
      catch (IOException ioe) { _showIOError(ioe); }
    }
    else _runMain();
  }
  
  /** Internal helper method to run the main method of the current document in the interactions pane. */
  private void _runMain() {
    updateStatusField("Running main Method of Current Document");
    
    try { _model.getActiveDocument().runMain(); }
    
    catch (ClassNameNotFoundException e) {
      // Display a warning message if a class name can't be found.
      String msg =
        "DrJava could not find the top level class name in the\n" +
        "current document, so it could not run the class.  Please\n" +
        "make sure that the class is properly defined first.";
      
      JOptionPane.showMessageDialog(MainFrame.this, msg, "No Class Found", JOptionPane.ERROR_MESSAGE);
    }
    catch (FileMovedException fme) { _showFileMovedError(fme); }
    catch (IOException ioe) { _showIOError(ioe); }
  }
  
  private void _junit() {
    hourglassOn(); // turned off in JUnitStarted/NonTestCase  
    new Thread("Run JUnit on Current Document") {
      public void run() {
        _disableJUnitActions();
        // now also works with multiple documents
//        hourglassOn();  // moved into the prelude before this thread start  
        try { 
          _model.getJUnitModel().junitDocs(_model.getDocumentNavigator().getSelectedDocuments()); 
//          _model.getActiveDocument().startJUnit();  // Equivalent to preceding
        }
        
//        catch (FileMovedException fme) { _showFileMovedError(fme); }
//        catch (IOException ioe) { _showIOError(ioe); }
//        catch (ClassNotFoundException cnfe) { _showClassNotFoundError(cnfe); }
        catch (NoClassDefFoundError ncde) { _showNoClassDefError(ncde); }
      }
    }.start();
  }
  
  private void _junitFolder() {
    updateStatusField("Running Unit Tests in Current Folder");
    hourglassOn();  // turned off when JUnitStarted event is fired
    new Thread("Run JUnit on specified folder") {
      public void run() { 
        INavigatorItem n;
        _disableJUnitActions();
//        hourglassOn();  // turned off when JUnitStarted event is fired
        if (_model.getDocumentNavigator().isGroupSelected()) {
          Enumeration<OpenDefinitionsDocument> docs = _model.getDocumentNavigator().getDocuments();
          final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
          while (docs.hasMoreElements()) {
            OpenDefinitionsDocument doc = docs.nextElement();
            if (_model.getDocumentNavigator().isSelectedInGroup(doc))
              l.add(doc);
          }
          try { _model.getJUnitModel().junitDocs(l); }
          catch(UnexpectedException e) { _junitInterrupted(e); }
        }
      }
    }.start();
  }
  
  /** Tests the documents in the project source tree. Assumes that DrJava is in project mode. */
  private void _junitProject() {
    updateStatusField("Running JUnit Tests in Project");
    hourglassOn();  // turned off in JUnitStarted/NonTestCase event
    new Thread("Running Junit Tests") {
      public void run() {
        _disableJUnitActions();
//        hourglassOn();  // turned off in JUnitStarted/NonTestCase event
        try { _model.getJUnitModel().junitProject(); } 
        catch(UnexpectedException e) { _junitInterrupted(e); }
      }
    }.start();
  }
  
  /** Tests all open documents. */
  private void _junitAll() {
    updateStatusField("Running All Open Unit Tests");
    hourglassOn();  // turned off in JUnitStarted/NonTestCase event
    new Thread("Running Junit Tests") {
      public void run() {
        _disableJUnitActions();
//        hourglassOn();  // turned off in JUnitStarted/NonTestCase event
        try { _model.getJUnitModel().junitAll(); } 
        catch(UnexpectedException e) { _junitInterrupted(e); }
      }
    }.start();
  }
  
  /* These are used to save the state of the enabled property of the actions disabled during junit testing. */
  private volatile DecoratedAction _junit_compileProjectDecoratedAction;
  private volatile DecoratedAction _junit_compileAllDecoratedAction;
  private volatile DecoratedAction _junit_compileFolderDecoratedAction;
  private volatile DecoratedAction _junit_junitFolderDecoratedAction;
  private volatile DecoratedAction _junit_junitAllDecoratedAction;
  private volatile DecoratedAction _junit_junitDecoratedAction;
  private volatile DecoratedAction _junit_junitOpenProjectFilesDecoratedAction;
  private volatile DecoratedAction _junit_cleanDecoratedAction;
  private volatile DecoratedAction _junit_autoRefreshDecoratedAction;
  private volatile DecoratedAction _junit_projectPropertiesDecoratedAction;
  private volatile DecoratedAction _junit_runProjectDecoratedAction;
  private volatile DecoratedAction _junit_runDecoratedAction;
  
  /** An AbstractAction that prevents changes to the decoree's enabled flag. */
  private class DecoratedAction extends AbstractAction {
    /** The AbstractAction that is being decorated. */
    AbstractAction _decoree;
    /** The "shallow" enabled flag. */
    boolean _shallowEnabled;
    /** Create an action decorating the specified action, then sets the decoree's enabled flag to b. */
    public DecoratedAction(AbstractAction a, boolean b) {
      super((String)a.getValue("Name"));
      _decoree = a;
      _shallowEnabled = _decoree.isEnabled();
      _decoree.setEnabled(b);
    }
    public void actionPerformed(ActionEvent ae) { _decoree.actionPerformed(ae); }
    /** Do not change the decoree's enabled flag, but cache this value in the shallow enabled flag. */
    public void setEnabled(boolean b) { _shallowEnabled = b; }
    /** Write the shallow enabled flag to the decoree, then return the decoree */
    public AbstractAction getUpdatedDecoree() { _decoree.setEnabled(_shallowEnabled); return _decoree; }
  }
  
  /** Sets the enabled status to false of all actions that  could conflict with JUnit while its is running a test.
    * This method saves aside the previous enable state of each action so that when the test is finished, any action
    * disabled before the test will remain disabled afterward.
    */
  private void _disableJUnitActions() {
    // _compileProjectActionEnabled = _compileProjectAction.isEnabled();
    // _compileAllActionEnabled = _compileAllAction.isEnabled();
    //_compileFolderActionEnabled = _compileFolderAction.isEnabled();
    //_junitFolderActionEnabled = _junitFolderAction.isEnabled();
    //_junitAllActionEnabled = _junitAllAction.isEnabled();
    //_junitActionEnabled = _junitAction.isEnabled();
    //_junitProjectActionEnabled = _junitProjectAction.isEnabled();
    //_cleanActionEnabled = _cleanAction.isEnabled();
    //_projectPropertiesActionEnabled = _projectPropertiesAction.isEnabled();
    //_runProjectActionEnabled = _runProjectAction.isEnabled();
    
    // _compileProjectAction.setEnabled(false);
    //_compileAllAction.setEnabled(false);
    //_compileFolderAction.setEnabled(false);
    //_junitFolderAction.setEnabled(false);
    //_junitAllAction.setEnabled(false);
    //_junitAction.setEnabled(false);
    //_junitProjectAction.setEnabled(false);
    //_cleanAction.setEnabled(false);
    //_projectPropertiesAction.setEnabled(false);
    //_runProjectAction.setEnabled(false);
    
    _compileProjectAction = _junit_compileProjectDecoratedAction = new DecoratedAction(_compileProjectAction, false);
    _compileAllAction = _junit_compileAllDecoratedAction = new DecoratedAction(_compileAllAction, false);
    _compileFolderAction = _junit_compileFolderDecoratedAction = new DecoratedAction(_compileFolderAction, false);
    _junitFolderAction = _junit_junitFolderDecoratedAction = new DecoratedAction(_junitFolderAction, false);
    _junitAllAction = _junit_junitAllDecoratedAction = new DecoratedAction(_junitAllAction, false);
    _junitAction = _junit_junitDecoratedAction = new DecoratedAction(_junitAction, false);
    _junitProjectAction = _junit_junitOpenProjectFilesDecoratedAction = new DecoratedAction(_junitProjectAction, false);  
    _cleanAction = _junit_cleanDecoratedAction = new DecoratedAction(_cleanAction, false);
    _autoRefreshAction = _junit_autoRefreshDecoratedAction = new DecoratedAction(_autoRefreshAction, false);
    _projectPropertiesAction = _junit_projectPropertiesDecoratedAction = 
      new DecoratedAction(_projectPropertiesAction, false);
    _runProjectAction = _junit_runProjectDecoratedAction = new DecoratedAction(_runProjectAction, false);
    _runAction = _junit_runDecoratedAction = new DecoratedAction(_runAction, false);
  }
  private void _restoreJUnitActionsEnabled() {
//    _compileProjectAction.setEnabled(_compileProjectActionEnabled);
//    _compileAllAction.setEnabled(_compileAllActionEnabled);
//    //_compileOpenProjectAction.setEnabled(_compileOpenProjectActionEnabled);
//    _compileFolderAction.setEnabled(_compileFolderActionEnabled);
//    _junitFolderAction.setEnabled(_junitFolderActionEnabled);
//    _junitAllAction.setEnabled(_junitAllActionEnabled);
//    _junitAction.setEnabled(_junitActionEnabled);
//    _junitProjectAction.setEnabled(_junitProjectActionEnabled);
//    //_junitProjectAction.setEnabled(_junitProjectActionEnabled);
//    _cleanAction.setEnabled(_cleanActionEnabled);
//    _projectPropertiesAction.setEnabled(_projectPropertiesActionEnabled);
//    _runProjectAction.setEnabled(_runProjectActionEnabled);
    
    _compileProjectAction = _junit_compileProjectDecoratedAction.getUpdatedDecoree();
    _compileAllAction = _junit_compileAllDecoratedAction.getUpdatedDecoree();
    _compileFolderAction = _junit_compileFolderDecoratedAction.getUpdatedDecoree();
    _junitFolderAction = _junit_junitFolderDecoratedAction.getUpdatedDecoree();
    _junitAllAction = _junit_junitAllDecoratedAction.getUpdatedDecoree();
    _junitAction = _junit_junitDecoratedAction.getUpdatedDecoree();
    _junitProjectAction = _junit_junitOpenProjectFilesDecoratedAction.getUpdatedDecoree();
    _cleanAction = _junit_cleanDecoratedAction.getUpdatedDecoree();
    _autoRefreshAction = _junit_autoRefreshDecoratedAction.getUpdatedDecoree();
    _projectPropertiesAction = _junit_projectPropertiesDecoratedAction.getUpdatedDecoree();
    _runProjectAction = _junit_runProjectDecoratedAction.getUpdatedDecoree();
    _runAction = _junit_runDecoratedAction.getUpdatedDecoree();
  }
  
//  /**
//   * Suspends the current execution of the debugger
//   */
//  private void debuggerSuspend() throws DebugException {
//    if (isDebuggerReady())
//      _model.getDebugger().suspend();
//  }
  
  /** Resumes the debugger's current execution. */
  void debuggerResume() throws DebugException {
    if (isDebuggerReady()) {
      _model.getDebugger().resume();
      _removeThreadLocationHighlight();
    }
  }
  
  /** Steps in the debugger. */
  void debuggerStep(Debugger.StepType type) {
    if (isDebuggerReady()) {
      try { _model.getDebugger().step(type); }
      catch (IllegalStateException ise) {
        // This may happen if the user if stepping very frequently,
        // and is even more likely if they are using both hotkeys
        // and UI buttons. Ignore it in this case.
        // Hopefully, there are no other situations where
        // the user can be trying to step while there are no
        // suspended threads.
      }
      catch (DebugException de) {
        _showError(de, "Debugger Error",
                   "Could not create a step request.");
      }
    }
  }
  
  /** Toggles a breakpoint on the current line. */
  void debuggerToggleBreakpoint() {
    addToBrowserHistory();
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    
    boolean isUntitled = doc.isUntitled();
    if (isUntitled) {
      JOptionPane.showMessageDialog(this,
                                    "You must save and compile this document before you can\n" +
                                    "set a breakpoint in it.",
                                    "Must Save and Compile",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    boolean isModified = doc.isModifiedSinceSave();
    if (isDebuggerReady() && isModified  && !_currentDefPane.hasWarnedAboutModified() &&
        DrJava.getConfig().getSetting(WARN_BREAKPOINT_OUT_OF_SYNC).booleanValue()) {
      String message =
        "This document has been modified and may be out of sync\n" +
        "with the debugger.  It is recommended that you first\n" +
        "save and recompile before continuing to use the debugger,\n" +
        "to avoid any unexpected errors.  Would you still like to\n" +
        "toggle the breakpoint on the specified line?";
      String title = "Toggle breakpoint on modified file?";
      
      ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(this, title, message);
      int rc = dialog.show();
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _currentDefPane.hasWarnedAboutModified(true);
          if (dialog.getCheckBoxValue()) {
            DrJava.getConfig().setSetting(WARN_BREAKPOINT_OUT_OF_SYNC, Boolean.FALSE);
          }
          break;
          
        case JOptionPane.NO_OPTION:
          if (dialog.getCheckBoxValue())  DrJava.getConfig().setSetting(WARN_BREAKPOINT_OUT_OF_SYNC, Boolean.FALSE);
          return;
          
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
          // do nothing
          return;
          
        default:
          throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
      }
    }
    
    try {
      Debugger debugger = _model.getDebugger();
      debugger.toggleBreakpoint(doc, _currentDefPane.getCaretPosition(), _currentDefPane.getCurrentLine(), true);
    }
    catch (DebugException de) {
      _showError(de, "Debugger Error", "Could not set a breakpoint at the current line.");
    }
  }
  
  
//  private void _getText(String name) { _field = name; }
  
//  /** Adds a watch to a given variable or field. */
//  void debuggerAddWatch() {
//    if (isDebuggerReady()) {
//      //final String field;
//      OpenDefinitionsDocument doc = _model.getActiveDocument();
//      final JDialog getFieldDialog = new JDialog(this, "Choose Field to be Watched", true);
//      //getFieldDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//      final JTextField fieldName = new JTextField();
//      getFieldDialog.setSize(new Dimension(150, 60));
//      getFieldDialog.getContentPane().add(fieldName);
//      fieldName.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent ae) {
//          _getText(fieldName.getText());
//          getFieldDialog.dispose();
//        }
//      });
//      getFieldDialog.setLocation(300,300);
//      getFieldDialog.show();
//      Debugger debugger = _model.getDebugger();
//      debugger.addWatch(_field);
//    }
//  }
  
//  /** Displays all breakpoints currently set in the debugger. */
//  void _printBreakpoints() { _model.getDebugger().printBreakpoints(); }
  
  /** Clears all breakpoints from the debugger. */
  void debuggerClearAllBreakpoints() {
    _model.getBreakpointManager().clearRegions();
  }
  
  void _showFileMovedError(FileMovedException fme) {
    try {
      File f = fme.getFile();
      OpenDefinitionsDocument doc = _model.getDocumentForFile(f);
      if (doc != null && _saveSelector.shouldSaveAfterFileMoved(doc, f)) _saveAs();
    }
    catch (IOException ioe) { /* Couldn't find the document, so ignore the FME */ }
  }
  
  void _showProjectFileParseError(MalformedProjectFileException mpfe) {
    _showError(mpfe, "Invalid Project File", "DrJava could not read the given project file.");
  }
  
  void _showFileNotFoundError(FileNotFoundException fnf) {
    _showError(fnf, "File Not Found", "The specified file was not found on disk.");
  }
  
  void _showIOError(IOException ioe) {
    _showError(ioe, "Input/output error", "An I/O exception occurred during the last operation.");
  }
  
  void _showClassNotFoundError(ClassNotFoundException cnfe) {
    _showError(cnfe, "Class Not Found",
               "A ClassNotFound exception occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant directories.\n\n");
  }
  
  void _showNoClassDefError(NoClassDefFoundError ncde) {
    _showError(ncde, "No Class Def",
               "A NoClassDefFoundError occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant paths.\n\n");
  }
  
  void _showDebugError(DebugException de) {
    _showError(de, "Debug Error", "A Debugger error occurred in the last operation.\n\n");
  }
  
  void _showJUnitInterrupted(UnexpectedException e) {
    _showWarning(e.getCause(), "JUnit Testing Interrupted", 
                 "The slave JVM has thrown a RemoteException probably indicating that it has been reset.\n\n");
  }
  
  private void _showError(Throwable e, String title, String message) {
    JOptionPane.showMessageDialog(this, message + "\n" + e, title, JOptionPane.ERROR_MESSAGE);
  }
  
  private void _showWarning(Throwable e, String title, String message) {
    JOptionPane.showMessageDialog(this, message + "\n" + e, title, JOptionPane.WARNING_MESSAGE);
  }
  
  /** Check if any errors occurred while parsing the config file, and display a message if necessary. */
  private void _showConfigException() {
    if (DrJava.getConfig().hadStartupException()) {
      Exception e = DrJava.getConfig().getStartupException();
      _showError(e, "Error in Config File",
                 "Could not read the '.drjava' configuration file\n" +
                 "in your home directory.  Starting with default\n" +
                 "values instead.\n\n" + "The problem was:\n");
    }
  }
  
  /** Returns the File selected by the JFileChooser.
    * @param fc File chooser presented to the user
    * @param choice return value from fc
    * @return Selected File
    * @throws OperationCanceledException if file choice canceled
    * @throws RuntimeException if fc returns a bad file or choice
    */
  private File getChosenFile(JFileChooser fc, int choice) throws OperationCanceledException {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        if (chosen != null) {
          //append the appropriate language level extension if not written by user
          if (fc.getFileFilter() instanceof JavaSourceFilter) {
            if (chosen.getName().indexOf(".") == -1)
              return new File(chosen.getAbsolutePath() + "." + 
                              DrJavaRoot.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)]);
          }
          return chosen;
        }
        else
          throw new RuntimeException("Filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("Filechooser returned bad rc " + choice);
    }
  }
  /** Returns the Files selected by the JFileChooser.
   * @param fc File chooser presented to the user
   * @param choice return value from fc
   * @return Selected Files - this array will be size 1 for single-selection dialogs.
   * @throws OperationCanceledException if file choice canceled
   * @throws RuntimeException if fc returns a bad file or choice
   */
  private File[] getChosenFiles(JFileChooser fc, int choice) throws OperationCanceledException {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File[] chosen = fc.getSelectedFiles();
        if (chosen == null)
          throw new UnexpectedException(new OperationCanceledException(), "filechooser returned null file");
        
        // Following code reviewed for bug 70902-- JVF
        // If this is a single-selection dialog, getSelectedFiles() will always
        // return a zero-size array -- handle it differently.
        if (chosen.length == 0) {
          if (!fc.isMultiSelectionEnabled()) {
            return new File[] { fc.getSelectedFile() };
          }
          else {
            /* This is the workaround for bug 70902: sometimes Mac OS X will return
             * APPROVE_OPTION when the user clicks the close (x) control button
             * on the dialog window, even though nothing is selected.
             */
            throw new OperationCanceledException();
          }
        }
        else {
          return chosen;
        }
        
      default:                  // impossible since rc must be one of these
        throw new UnexpectedException(new OperationCanceledException(), "filechooser returned bad rc " + choice);
    }
  }
  
  private void _selectAll() { _currentDefPane.selectAll(); }
  
  /** Jump to the specified line and return the offset.  Only runs in event thread.
    * @return offset */
  public int _jumpToLine(int lineNum) {   
    int pos = _model.getActiveDocument().gotoLine(lineNum);
    addToBrowserHistory();
    _currentDefPane.setCaretPosition(pos);
    _currentDefPane.centerViewOnOffset(pos);
    return pos;
  }
  
  /** Ask the user what line they'd like to jump to, then go there. */
  private int _gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Go to Line";
    String lineStr = JOptionPane.showInputDialog(this, msg, title, JOptionPane.QUESTION_MESSAGE);
    try {
      if (lineStr != null) {
        int lineNum = Integer.parseInt(lineStr);
        return _jumpToLine(lineNum);      }
    }
    catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
    //catch (BadLocationException ble) { }
    return -1;
  }
  
  /** Removes the ErrorCaretListener corresponding to the given document, after that document has been closed.
    * (Allows pane and listener to be garbage collected...)
    */
  private void _removeErrorListener(OpenDefinitionsDocument doc) {
    JScrollPane scroll = _defScrollPanes.get(doc);
    if (scroll != null) {
      DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
      pane.removeCaretListener(pane.getErrorCaretListener());
    }
  }
  
  /** Initializes all action objects.  Adds icons and descriptions to several of the actions. Note: this 
    * initialization will later be done in the constructor of each action, which will subclass AbstractAction.
    */
  private void _setUpActions() {
    _setUpAction(_newAction, "New", "Create a new document");
    _setUpAction(_newJUnitTestAction, "New", "Create a new JUnit test case class");
    _setUpAction(_newProjectAction, "New", "Make a new project");
    _setUpAction(_openAction, "Open", "Open an existing file");
    _setUpAction(_openFolderAction, "Open Folder", "OpenAll", "Open all files within a directory");
    _setUpAction(_openFileOrProjectAction, "Open", "Open an existing file or project");
    _setUpAction(_openProjectAction, "Open", "Open an existing project");
    _setUpAction(_saveAction, "Save", "Save the current document");
    _setUpAction(_saveAsAction, "Save As", "SaveAs", "Save the current document with a new name");
    _setUpAction(_renameAction, "Rename", "Rename", "Rename the current document");
    _setUpAction(_saveProjectAction, "Save", "Save", "Save the current project");
    _saveProjectAction.setEnabled(false);
    _setUpAction(_saveProjectAsAction, "Save As", "SaveAs", "Save current project to new project file");
    _saveProjectAsAction.setEnabled(false);
    _setUpAction(_exportProjectInOldFormatAction, "Export Project In Old \"" + OLD_PROJECT_FILE_EXTENSION +
                 "\" Format", "SaveAs", "Export Project In Old \"" + OLD_PROJECT_FILE_EXTENSION + "\" Format");
    _exportProjectInOldFormatAction.setEnabled(false);
    _setUpAction(_revertAction, "Revert", "Revert the current document to the saved version");
    // No longer used
//    _setUpAction(_revertAllAction, "Revert All", "RevertAll",
//                 "Revert all open documents to the saved versions");
    
    _setUpAction(_closeAction, "Close", "Close the current document");
    _setUpAction(_closeAllAction, "Close All", "CloseAll", "Close all documents");
    _setUpAction(_closeProjectAction, "Close", "CloseAll", "Close the current project");
    _closeProjectAction.setEnabled(false);
    
    _setUpAction(_projectPropertiesAction, "Project Properties", "Preferences", "Edit Project Properties");
    _projectPropertiesAction.setEnabled(false);    
    
//    _setUpAction(_junitProjectAction, "Test", "Test", "Test the current project");
//    _junitProjectAction.setEnabled(false);    
    _setUpAction(_junitProjectAction, "Test Project", "Test the documents in the project source tree");
    _junitProjectAction.setEnabled(false);
    
//    _setUpAction(_compileOpenProjectAction, "Compile", "Compile", "Compile the open project documents");
    _setUpAction(_compileProjectAction, "Compile Project", "Compile the documents in the project source tree");
//    _compileOpenProjectAction.setEnabled(false);
    _compileProjectAction.setEnabled(false);
    
    _setUpAction(_runProjectAction, "Run Project", "Run the project's main method");
    _runProjectAction.setEnabled(false);
    
    _setUpAction(_jarProjectAction, "Jar", "Create a jar archive from this project");
    _jarProjectAction.setEnabled(false);
    
    _setUpAction(_saveAllAction, "Save All", "SaveAll", "Save all open documents");
    
    _setUpAction(_cleanAction, "Clean", "Clean Build directory");
    _cleanAction.setEnabled(false);
    _setUpAction(_autoRefreshAction, "Auto-Refresh", "Auto-refresh project");
    _autoRefreshAction.setEnabled(false);
    _setUpAction(_compileAction, "Compile Current Document", "Compile the current document");
    _setUpAction(_compileAllAction, "Compile", "Compile all open documents");
    _setUpAction(_printDefDocAction, "Print", "Print the current main document");
    _setUpAction(_printConsoleAction, "Print", "Print the Console pane");
    _setUpAction(_printInteractionsAction, "Print", "Print the Interactions pane");
    _setUpAction(_pageSetupAction, "Page Setup", "PageSetup", "Change the printer settings");
    _setUpAction(_printDefDocPreviewAction, "Print Preview", "PrintPreview", "Preview how the document will be printed");
    _setUpAction(_printConsolePreviewAction, "Print Preview", "PrintPreview", 
                 "Preview how the console document will be printed");
    _setUpAction(_printInteractionsPreviewAction, "Print Preview", "PrintPreview", 
                 "Preview how the interactions document will be printed");    
    
    _setUpAction(_quitAction, "Quit", "Quit", "Quit DrJava");
    
    _setUpAction(_undoAction, "Undo", "Undo previous command");
    _setUpAction(_redoAction, "Redo", "Redo last undo");
    _undoAction.putValue(Action.NAME, "Undo Previous Command");
    _redoAction.putValue(Action.NAME, "Redo Last Undo");
    
    _setUpAction(cutAction, "Cut", "Cut selected text to the clipboard");
    _setUpAction(copyAction, "Copy", "Copy selected text to the clipboard");
    _setUpAction(pasteAction, "Paste", "Paste text from the clipboard");
    _setUpAction(_pasteHistoryAction, "Paste from History", "Paste text from the clipboard history");
    _setUpAction(_selectAllAction, "Select All", "Select all text");
    
    cutAction.putValue(Action.NAME, "Cut");
    copyAction.putValue(Action.NAME, "Copy");
    pasteAction.putValue(Action.NAME, "Paste");
    _pasteHistoryAction.putValue(Action.NAME, "Paste from History");
    
    _setUpAction(_indentLinesAction, "Indent Lines", "Indent all selected lines");
    _setUpAction(_commentLinesAction, "Comment Lines", "Comment out all selected lines");
    _setUpAction(_uncommentLinesAction, "Uncomment Lines", "Uncomment all selected lines");
    
    _setUpAction(completeWordUnderCursorAction, "Auto-Complete Word Under Cursor",
                 "Auto-complete the word the cursor is currently located on");
    _setUpAction(_bookmarksPanelAction, "Bookmarks", "Display the bookmarks panel");
    _setUpAction(_toggleBookmarkAction, "Toggle Bookmark", "Toggle the bookmark at the current cursor location");
    _setUpAction(_followFileAction, "Follow File", "Follow a file's updates");
    _setUpAction(_executeExternalProcessAction, "Execute External", "Execute external process");
    _setUpAction(_editExternalProcessesAction, "Preferences", "Edit saved external processes");
    
    _setUpAction(_findReplaceAction, "Find", "Find or replace text in the document");
    _setUpAction(_findNextAction, "Find Next", "Repeats the last find");
    _setUpAction(_findPrevAction, "Find Previous", "Repeats the last find in the opposite direction");
    _setUpAction(_gotoLineAction, "Go to line", "Play", "Go to a line number in the document");
    _setUpAction(_gotoFileAction, "Go to File", "Go to a file specified by its name");
    _setUpAction(gotoFileUnderCursorAction, "Go to File Under Cursor",
                 "Go to the file specified by the word the cursor is located on");
    
    _setUpAction(_switchToPrevAction, "Previous Document", "Up", "Switch to the previous document");
    _setUpAction(_switchToNextAction, "Next Document", "Down", "Switch to the next document");
    
    _setUpAction(_browseBackAction, "Back", "Back", "Move back in the browser history");
    _setUpAction(_browseForwardAction, "Forward", "Forward", "Move forward in the browser history");    
    
    _setUpAction(_switchToPreviousPaneAction, "Previous Pane", "Switch focus to the previous pane");
    _setUpAction(_switchToNextPaneAction, "Next Pane", "Switch focus to the next pane");
    _setUpAction(_gotoOpeningBraceAction, "Go to Opening Brace", "Go th the opening brace of the block enclosing the cursor");
    _setUpAction(_gotoClosingBraceAction, "Go to Closing Brace", "Go th the closing brace of the block enclosing the cursor");
    
    _setUpAction(_editPreferencesAction, "Preferences", "Edit configurable settings in DrJava");
    
    _setUpAction(_junitAction, "Test Current", "Run JUnit over the current document");
    _setUpAction(_junitAllAction, "Test", "Run JUnit over all open JUnit tests");
    _setUpAction(_javadocAllAction, "Javadoc", "Create and save Javadoc for the packages of all open documents");
    _setUpAction(_javadocCurrentAction, "Preview Javadoc Current", "Preview the Javadoc for the current document");
    _setUpAction(_runAction, "Run", "Run the main method of the current document");
    
    _setUpAction(_openJavadocAction, "Open Java API Javadoc...", "Open the Java API Javadoc Web page for a class");
    _setUpAction(_openJavadocUnderCursorAction, "Open Java API Javadoc for Word Under Cursor...", "Open the Java API "+
                 "Javadoc Web page for the word under the cursor");
    
    _setUpAction(_executeHistoryAction, "Execute History", "Load and execute a history of interactions from a file");
    _setUpAction(_loadHistoryScriptAction, "Load History as Script", 
                 "Load a history from a file as a series of interactions");
    _setUpAction(_saveHistoryAction, "Save History", "Save the history of interactions to a file");
    _setUpAction(_clearHistoryAction, "Clear History", "Clear the current history of interactions");
    
    //_setUpAction(_abortInteractionAction, "Break", "Abort the current interaction");
    _setUpAction(_resetInteractionsAction, "Reset", "Reset the Interactions Pane");
    _resetInteractionsAction.setEnabled(true);
    
    _setUpAction(_viewInteractionsClassPathAction, "View Interactions Classpath", 
                 "Display the classpath in use by the Interactions Pane");
    _setUpAction(_copyInteractionToDefinitionsAction, "Lift Current Interaction", 
                 "Copy the current interaction into the Definitions Pane");
    
    _setUpAction(_clearConsoleAction, "Clear Console", "Clear all text in the Console Pane");
    _setUpAction(_showDebugConsoleAction, "Show DrJava Debug Console", "<html>Show a console for debugging DrJava<br>" +
                 "(with \"mainFrame\", \"model\", and \"config\" variables defined)</html>");
    
    if (_model.getDebugger().isAvailable()) {
      _setUpAction(_toggleDebuggerAction, "Debug Mode", "Enable or disable DrJava's debugger");
      _setUpAction(_toggleBreakpointAction, "Toggle Breakpoint", "Set or clear a breakpoint on the current line");
      _setUpAction(_clearAllBreakpointsAction, "Clear Breakpoints", "Clear all breakpoints in all classes");
      _setUpAction(_resumeDebugAction, "Resume", "Resume the current suspended thread");
      _setUpAction(_stepIntoDebugAction, "Step Into", "Step into the current line or method call");
      _setUpAction(_stepOverDebugAction, "Step Over", "Step over the current line or method call");
      _setUpAction(_stepOutDebugAction, "Step Out", "Step out of the current method");
      _setUpAction(_breakpointsPanelAction, "Breakpoints", "Display the breakpoints panel");
    }
    
    _setUpAction(_helpAction, "Help", "Show documentation on how to use DrJava");
    _setUpAction(_quickStartAction, "Help", "View Quick Start Guide for DrJava");
    _setUpAction(_aboutAction, "About", "About DrJava");
    _setUpAction(_checkNewVersionAction, "Check for New Version", "Find", "Check for New Version");
    _setUpAction(_errorsAction, "DrJava Errors", "drjavaerror", "Show a window with internal DrJava errors");
    _setUpAction(_forceQuitAction, "Force Quit", "Stop", "Force DrJava to quit without cleaning up");
  }
  
  private void _setUpAction(Action a, String name, String icon, String shortDesc) {
    a.putValue(Action.SMALL_ICON, _getIcon(icon + "16.gif"));
    a.putValue(Action.DEFAULT, name);
    a.putValue(Action.SHORT_DESCRIPTION, shortDesc);
  }
  
  private void _setUpAction(Action a, String icon, String shortDesc) { _setUpAction(a, icon, icon, shortDesc); }
  
  
  /** Returns the icon with the given name. All icons are assumed to reside in the /edu/rice/cs/drjava/ui/icons
    * directory.
    * @param name Name of icon image file
    * @return ImageIcon object constructed from the file
    */
  private ImageIcon _getIcon(String name) { return getIcon(name); }
  
  public static ImageIcon getIcon(String name) {
    URL url = MainFrame.class.getResource(ICON_PATH + name);
    if (url != null)  return new ImageIcon(url);
    
    return null;
  }
  
  
  /** This allows us to intercept key events when compiling testing and turn them off when the glass pane is up. */
  private class MenuBar extends JMenuBar {
    public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
      if (MainFrame.this.getAllowKeyEvents()) return super.processKeyBinding(ks, e, condition, pressed);
      return false;
    }
  }
  
  /** Sets up the components of the menu bar and links them to the private fields within MainFrame.  This method 
    * serves to make the code more legible on the higher calling level, i.e., the constructor.
    */
  private void _setUpMenuBar() {
    
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_toolsMenu);
    _menuBar.add(_projectMenu);
    if (_showDebugger) _menuBar.add(_debugMenu);
    _menuBar.add(_languageLevelMenu);
    _menuBar.add(_helpMenu);
    setJMenuBar(_menuBar);
  }
  
  /** Adds an Action as a menu item to the given menu, using the specified configurable keystroke.
    * @param menu Menu to add item to
    * @param a Action for the menu item
    * @param opt Configurable keystroke for the menu item
    */
  private void _addMenuItem(JMenu menu, Action a, Option<KeyStroke> opt) {
    JMenuItem item;
    item = menu.add(a);
    _setMenuShortcut(item, a, opt);
  }
  
  /** Sets the given menu item to have the specified configurable keystroke.
    * @param item Menu item containing the action
    * @param a Action for the menu item
    * @param opt Configurable keystroke for the menu item
    */
  private void _setMenuShortcut(JMenuItem item, Action a, Option<KeyStroke> opt) {
    KeyStroke ks = DrJava.getConfig().getSetting(opt);
    // Checks that "a" is the action associated with the keystroke.
    // Need to check in case two actions were assigned to the same
    // key in the config file.
    // Also check that the keystroke isn't the NULL_KEYSTROKE, which
    //  can strangely be triggered by certain keys in Windows.
    KeyBindingManager.Singleton.put(opt, a, item, item.getText());
    if (ks != KeyStrokeOption.NULL_KEYSTROKE &&
        KeyBindingManager.Singleton.get(ks) == a) {
      item.setAccelerator(ks);
      //KeyBindingManager.Singleton.addListener(opt, item);
    }
  }
  
  /** Creates and returns a file menu.  Side effects: sets values for _saveMenuItem. */
  private JMenu _setUpFileMenu(int mask) {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    // New, open
    _addMenuItem(fileMenu, _newAction, KEY_NEW_FILE);
    _addMenuItem(fileMenu, _newJUnitTestAction, KEY_NEW_TEST);
    _addMenuItem(fileMenu, _openAction, KEY_OPEN_FILE);
    _addMenuItem(fileMenu, _openFolderAction, KEY_OPEN_FOLDER);
    //_addMenuItem(fileMenu, _openProjectAction, KEY_OPEN_PROJECT);
    
    fileMenu.addSeparator();
    
    _addMenuItem(fileMenu, _saveAction, KEY_SAVE_FILE);
    _saveAction.setEnabled(true);
    _addMenuItem(fileMenu, _saveAsAction, KEY_SAVE_FILE_AS);
    _addMenuItem(fileMenu, _saveAllAction, KEY_SAVE_ALL_FILES);
    _addMenuItem(fileMenu, _renameAction, KEY_RENAME_FILE);
    _renameAction.setEnabled(false);
//    fileMenu.add(_saveProjectAsAction);
    
    _addMenuItem(fileMenu, _revertAction, KEY_REVERT_FILE);
    _revertAction.setEnabled(false);
    //tmpItem = fileMenu.add(_revertAllAction);
    
    // Close, Close all
    fileMenu.addSeparator();
    _addMenuItem(fileMenu, _closeAction, KEY_CLOSE_FILE);
    _addMenuItem(fileMenu, _closeAllAction, KEY_CLOSE_ALL_FILES);
    //_addMenuItem(fileMenu, _closeProjectAction, KEY_CLOSE_PROJECT);
    
    // Page setup, print preview, print
    fileMenu.addSeparator();
    _addMenuItem(fileMenu, _pageSetupAction, KEY_PAGE_SETUP);
    _addMenuItem(fileMenu, _printDefDocPreviewAction, KEY_PRINT_PREVIEW);
    _addMenuItem(fileMenu, _printDefDocAction, KEY_PRINT);
    
    // Quit
    fileMenu.addSeparator();
    _addMenuItem(fileMenu, _quitAction, KEY_QUIT);
    
    return fileMenu;
  }
  
  /** Creates and returns a edit menu. */
  private JMenu _setUpEditMenu(int mask) {
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);
    // Undo, redo
    _addMenuItem(editMenu, _undoAction, KEY_UNDO);
    _addMenuItem(editMenu, _redoAction, KEY_REDO);
    
    // Cut, copy, paste, select all
    editMenu.addSeparator();
    _addMenuItem(editMenu, cutAction, KEY_CUT);
    _addMenuItem(editMenu, copyAction, KEY_COPY);
    _addMenuItem(editMenu, pasteAction, KEY_PASTE);
    _addMenuItem(editMenu, _pasteHistoryAction, KEY_PASTE_FROM_HISTORY);
    _addMenuItem(editMenu, _selectAllAction, KEY_SELECT_ALL);
    
    // Indent lines, comment lines
    editMenu.addSeparator();
    //_addMenuItem(editMenu, _indentLinesAction, KEY_INDENT);
    JMenuItem editItem = editMenu.add(_indentLinesAction);
    editItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
    _addMenuItem(editMenu, _commentLinesAction, KEY_COMMENT_LINES);
    _addMenuItem(editMenu, _uncommentLinesAction, KEY_UNCOMMENT_LINES);
    _addMenuItem(editMenu, completeWordUnderCursorAction, KEY_COMPLETE_FILE);
    
    // Find/replace, goto
    editMenu.addSeparator();
    _addMenuItem(editMenu, _findReplaceAction, KEY_FIND_REPLACE);
    _addMenuItem(editMenu, _findNextAction, KEY_FIND_NEXT);
    _addMenuItem(editMenu, _findPrevAction, KEY_FIND_PREV);
    _addMenuItem(editMenu, _gotoLineAction, KEY_GOTO_LINE);
    _addMenuItem(editMenu, _gotoFileAction, KEY_GOTO_FILE);
    _addMenuItem(editMenu, gotoFileUnderCursorAction, KEY_GOTO_FILE_UNDER_CURSOR);
    
    // Next, prev doc
    editMenu.addSeparator();
    _addMenuItem(editMenu, _switchToPrevAction, KEY_PREVIOUS_DOCUMENT);
    _addMenuItem(editMenu, _switchToNextAction, KEY_NEXT_DOCUMENT);
    _addMenuItem(editMenu, _browseBackAction, KEY_BROWSE_BACK);
    _addMenuItem(editMenu, _browseForwardAction, KEY_BROWSE_FORWARD);    
    _addMenuItem(editMenu, _gotoOpeningBraceAction, KEY_OPENING_BRACE);
    _addMenuItem(editMenu, _gotoClosingBraceAction, KEY_CLOSING_BRACE);
    editMenu.addSeparator();
    _addMenuItem(editMenu, _switchToPreviousPaneAction, KEY_PREVIOUS_PANE);
    _addMenuItem(editMenu, _switchToNextPaneAction, KEY_NEXT_PANE);
    
    // access to configurations GUI
    editMenu.addSeparator();
    _addMenuItem(editMenu, _editPreferencesAction, KEY_PREFERENCES);
    
    // Add the menus to the menu bar
    return editMenu;
  }
  
  /** Creates and returns a tools menu. */
  private JMenu _setUpToolsMenu(int mask) {
    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    
    // Compile, Test, Javadoc
    _addMenuItem(toolsMenu, _compileAllAction, KEY_COMPILE_ALL);
    _addMenuItem(toolsMenu, _compileAction, KEY_COMPILE);
    _addMenuItem(toolsMenu, _junitAllAction, KEY_TEST_ALL);
    _addMenuItem(toolsMenu, _junitAction, KEY_TEST);
    toolsMenu.addSeparator();
    
    // Run
    _addMenuItem(toolsMenu, _runAction, KEY_RUN);
    _addMenuItem(toolsMenu, _resetInteractionsAction, KEY_RESET_INTERACTIONS);
    toolsMenu.addSeparator();
    
    // Javadoc
    final JMenu javadocMenu = new JMenu("Javadoc");
    _addMenuItem(javadocMenu, _javadocAllAction, KEY_JAVADOC_ALL);
    _addMenuItem(javadocMenu, _javadocCurrentAction, KEY_JAVADOC_CURRENT);
    javadocMenu.addSeparator();
    _addMenuItem(javadocMenu, _openJavadocAction, KEY_OPEN_JAVADOC);
    _addMenuItem(javadocMenu, _openJavadocUnderCursorAction, KEY_OPEN_JAVADOC_UNDER_CURSOR);    
    toolsMenu.add(javadocMenu);
    
    final JMenu historyMenu = new JMenu("History");
    _addMenuItem(historyMenu, _executeHistoryAction, KEY_EXECUTE_HISTORY);
    _addMenuItem(historyMenu, _loadHistoryScriptAction, KEY_LOAD_HISTORY_SCRIPT);
    _addMenuItem(historyMenu, _saveHistoryAction, KEY_SAVE_HISTORY);
    _addMenuItem(historyMenu, _clearHistoryAction, KEY_CLEAR_HISTORY);
    toolsMenu.add(historyMenu);
    
    // Abort/reset interactions, clear console
    /*
     _abortInteractionAction.setEnabled(false);
     _addMenuItem(toolsMenu, _abortInteractionAction, KEY_ABORT_INTERACTION);
     */
    final JMenu interMenu = new JMenu("Interactions & Console");    
    _addMenuItem(interMenu, _viewInteractionsClassPathAction, KEY_VIEW_INTERACTIONS_CLASSPATH);
    _addMenuItem(interMenu, _copyInteractionToDefinitionsAction, KEY_LIFT_CURRENT_INTERACTION);
    _addMenuItem(interMenu, _printInteractionsAction, KEY_PRINT_INTERACTIONS);
    interMenu.addSeparator();
    _addMenuItem(interMenu, _clearConsoleAction, KEY_CLEAR_CONSOLE);
    _addMenuItem(interMenu, _printConsoleAction, KEY_PRINT_CONSOLE);
    if (DrJava.getConfig().getSetting(SHOW_DEBUG_CONSOLE).booleanValue()) {
      toolsMenu.add(_showDebugConsoleAction);
    }
    toolsMenu.add(interMenu);
    
    final JMenu extMenu = new JMenu("External Processes");
    _addMenuItem(extMenu, _executeExternalProcessAction, KEY_EXEC_PROCESS);
    final JMenuItem execItem = extMenu.getItem(0);
    extMenu.addSeparator();
    extMenu.add(_editExternalProcessesAction);
    toolsMenu.add(extMenu);
    
    final int savedCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
    final int namesCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES).size();
    final int typesCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES).size();
    final int cmdlinesCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES).size();
    final int jvmargsCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS).size();
    final int workdirsCount = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS).size();
    if ((savedCount!=namesCount) ||
        (savedCount!=typesCount) ||
        (savedCount!=cmdlinesCount) ||
        (savedCount!=jvmargsCount) ||
        (savedCount!=workdirsCount)) {
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT, 0);
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES, new Vector<String>());
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_TYPES, new Vector<String>());
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES, new Vector<String>());
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS, new Vector<String>());
      DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS, new Vector<String>());
    }
    
    OptionListener<Integer> externalSavedCountListener =
      new OptionListener<Integer>() {
      public void optionChanged(final OptionEvent<Integer> oce) {
        Utilities.invokeLater(new Runnable() {
          public void run() {
            extMenu.removeAll();
            extMenu.add(execItem);
            extMenu.addSeparator();
            for (int count=0; count<oce.value; ++count) {
              final int i = count;
              final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
              final Vector<String> types = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES);
              final Vector<String> cmdlines = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
              final Vector<String> jvmargs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS);
              final Vector<String> workdirs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
              
              extMenu.insert(new AbstractAction(names.get(i)) {
                public void actionPerformed(ActionEvent ae) {
                  if (types.get(i).equals("cmdline")) {
                    // System.out.println(names.get(i)+": cmdline "+cmdlines.get(i)+" "+workdirs.get(i));
                    _executeExternalDialog.runCommand(names.get(i),cmdlines.get(i),workdirs.get(i));
                  }
                  else if (types.get(i).equals("java")) {
                    // System.out.println(names.get(i)+": java "+jvmargs.get(i)+" "+cmdlines.get(i)+" "+workdirs.get(i));
                    _executeExternalDialog.runJava(names.get(i),jvmargs.get(i),cmdlines.get(i),workdirs.get(i));
                  }
                  else {
                    JOptionPane.showMessageDialog(MainFrame.this,
                                                  "Unknown process type '"+types.get(i)+"'.",
                                                  "Invalid Command Line",
                                                  JOptionPane.ERROR_MESSAGE);
                  }
                }
              },i+2);
            }
            if (oce.value>0) { 
              extMenu.addSeparator();
            }
            extMenu.add(_editExternalProcessesAction);
            _editExternalProcessesAction.setEnabled(oce.value>0);
          }
        });
      }
    };
    DrJava.getConfig().addOptionListener(OptionConstants.EXTERNAL_SAVED_COUNT, externalSavedCountListener);
    externalSavedCountListener.
      optionChanged(new OptionEvent<Integer>(OptionConstants.EXTERNAL_SAVED_COUNT,
                                             DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)));
    toolsMenu.addSeparator();
    
    _addMenuItem(toolsMenu, _bookmarksPanelAction, KEY_BOOKMARKS_PANEL);
    _addMenuItem(toolsMenu, _toggleBookmarkAction, KEY_BOOKMARKS_TOGGLE);
    
    toolsMenu.addSeparator();
    _addMenuItem(toolsMenu, _followFileAction, KEY_FOLLOW_FILE);
    
    // Add the menus to the menu bar
    return toolsMenu;
  }
  
  /** Creates and returns a project menu. */
  private JMenu _setUpProjectMenu(int mask) {
    JMenu projectMenu = new JMenu("Project");
    projectMenu.setMnemonic(KeyEvent.VK_P);
    // New, open
    projectMenu.add(_newProjectAction);
    _addMenuItem(projectMenu, _openProjectAction, KEY_OPEN_PROJECT);
    
    //Save
    projectMenu.add(_saveProjectAction);
    //SaveAs
    projectMenu.add(_saveProjectAsAction);
    
    // Close
    _addMenuItem(projectMenu, _closeProjectAction, KEY_CLOSE_PROJECT);
    
    projectMenu.addSeparator();
    // run project
//    projectMenu.add(_compileOpenProjectAction);
    projectMenu.add(_compileProjectAction);
    projectMenu.add(_junitProjectAction);
    projectMenu.add(_runProjectAction);
//    projectMenu.add(_junitProjectAction);
    projectMenu.add(_cleanAction);
    projectMenu.add(_autoRefreshAction);
    projectMenu.add(_jarProjectAction);
    
    projectMenu.addSeparator();
    // eventually add project options
    projectMenu.add(_projectPropertiesAction);
    
    return projectMenu;
  }
  
  /** Creates and returns a debug menu. */
  private JMenu _setUpDebugMenu(int mask) {
    JMenu debugMenu = new JMenu("Debugger");
    debugMenu.setMnemonic(KeyEvent.VK_D);
    // Enable debugging item
    _debuggerEnabledMenuItem = _newCheckBoxMenuItem(_toggleDebuggerAction);
    _debuggerEnabledMenuItem.setSelected(false);
    _setMenuShortcut(_debuggerEnabledMenuItem, _toggleDebuggerAction, KEY_DEBUG_MODE_TOGGLE);
    debugMenu.add(_debuggerEnabledMenuItem);
    debugMenu.addSeparator();
    
    _addMenuItem(debugMenu, _toggleBreakpointAction, KEY_DEBUG_BREAKPOINT_TOGGLE);
    //_printBreakpointsMenuItem = debugMenu.add(_printBreakpointsAction);
    //_clearAllBreakpointsMenuItem =
    _addMenuItem(debugMenu, _clearAllBreakpointsAction, KEY_DEBUG_CLEAR_ALL_BREAKPOINTS);
    _addMenuItem(debugMenu, _breakpointsPanelAction, KEY_DEBUG_BREAKPOINT_PANEL);
    debugMenu.addSeparator();
    
    //_addMenuItem(debugMenu, _suspendDebugAction, KEY_DEBUG_SUSPEND);
    _addMenuItem(debugMenu, _resumeDebugAction, KEY_DEBUG_RESUME);
    _addMenuItem(debugMenu, _stepIntoDebugAction, KEY_DEBUG_STEP_INTO);
    _addMenuItem(debugMenu, _stepOverDebugAction, KEY_DEBUG_STEP_OVER);
    _addMenuItem(debugMenu, _stepOutDebugAction, KEY_DEBUG_STEP_OUT);
    
    // Start off disabled
    _setDebugMenuItemsEnabled(false);
    
    // Add the menu to the menu bar
    return debugMenu;
  }
  
  /** Called every time the debug mode checkbox is toggled. The resume and step
   * functions should always be disabled.
   */
  private void _setDebugMenuItemsEnabled(boolean isEnabled) {
    
    _debuggerEnabledMenuItem.setSelected(isEnabled);
    //_suspendDebugAction.setEnabled(false);
    _resumeDebugAction.setEnabled(false);
    _stepIntoDebugAction.setEnabled(false);
    _stepOverDebugAction.setEnabled(false);
    _stepOutDebugAction.setEnabled(false);
    
    if (_showDebugger) _debugPanel.disableButtons();
  }
  
  /** Enables and disables the appropriate menu items in the debug menu depending upon the state of the current thread.
    * @param isSuspended is true when the current thread has just been suspended
    *        false if the current thread has just been resumed
    */
  private void _setThreadDependentDebugMenuItems(boolean isSuspended) {
    //_suspendDebugAction.setEnabled(!isSuspended);
    _resumeDebugAction.setEnabled(isSuspended);
    _stepIntoDebugAction.setEnabled(isSuspended);
    _stepOverDebugAction.setEnabled(isSuspended);
    _stepOutDebugAction.setEnabled(isSuspended);
    _debugPanel.setThreadDependentButtons(isSuspended);
  }
  
  /** Creates and returns the language levels menu. */
  private JMenu _setUpLanguageLevelMenu(int mask) {
    JMenu languageLevelMenu = new JMenu("Language Level");
    languageLevelMenu.setMnemonic(KeyEvent.VK_L);
    ButtonGroup group = new ButtonGroup();
    
    final Configuration config = DrJava.getConfig();
    int currentLanguageLevel = config.getSetting(LANGUAGE_LEVEL);
    JRadioButtonMenuItem rbMenuItem;
    rbMenuItem = new JRadioButtonMenuItem("Full Java");
    rbMenuItem.setToolTipText("Use full Java syntax");
    if (currentLanguageLevel == DrJavaRoot.FULL_JAVA) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJavaRoot.FULL_JAVA);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    languageLevelMenu.addSeparator();
    
    rbMenuItem = new JRadioButtonMenuItem("Elementary");
    rbMenuItem.setToolTipText("Use Elementary language-level features");
    if (currentLanguageLevel == DrJavaRoot.ELEMENTARY_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJavaRoot.ELEMENTARY_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    
    rbMenuItem = new JRadioButtonMenuItem("Intermediate");
    rbMenuItem.setToolTipText("Use Intermediate language-level features");
    if (currentLanguageLevel == DrJavaRoot.INTERMEDIATE_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJavaRoot.INTERMEDIATE_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    
    rbMenuItem = new JRadioButtonMenuItem("Advanced");
    rbMenuItem.setToolTipText("Use Advanced language-level features");
    if (currentLanguageLevel == DrJavaRoot.ADVANCED_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJavaRoot.ADVANCED_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    return languageLevelMenu;
  }
  
  /** Creates and returns a help menu. */
  private JMenu _setUpHelpMenu(int mask) {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    _addMenuItem(helpMenu, _helpAction, KEY_HELP);
    _addMenuItem(helpMenu, _quickStartAction, KEY_QUICKSTART);
    helpMenu.addSeparator();
    _addMenuItem(helpMenu, _aboutAction, KEY_ABOUT);
    _addMenuItem(helpMenu, _checkNewVersionAction, KEY_CHECK_NEW_VERSION);
    _addMenuItem(helpMenu, _errorsAction, KEY_DRJAVA_ERRORS);
    helpMenu.addSeparator();
    _addMenuItem(helpMenu, _forceQuitAction, KEY_FORCE_QUIT);
    _addMenuItem(helpMenu, _exportProjectInOldFormatAction, KEY_EXPORT_OLD);
    return helpMenu;
  }
  
  /** Creates a toolbar button for undo and redo, which behave differently. */
  JButton _createManualToolbarButton(Action a) {
    final JButton ret;
    
    Font buttonFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);
    
    // Check whether icons should be shown
    boolean useIcon = DrJava.getConfig().getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
    boolean useText = DrJava.getConfig().getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
    final Icon icon = (useIcon) ? (Icon) a.getValue(Action.SMALL_ICON) : null;
    if (icon == null) {
      ret = new UnfocusableButton((String) a.getValue(Action.DEFAULT));
    }
    else {
      ret = new UnfocusableButton(icon);
      if (useText) ret.setText((String) a.getValue(Action.DEFAULT));
    }
    ret.setEnabled(false);
    ret.addActionListener(a);
    ret.setToolTipText( (String) a.getValue(Action.SHORT_DESCRIPTION));
    ret.setFont(buttonFont);
    Boolean test = a instanceof DelegatingAction;
    a.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          Boolean val = (Boolean) evt.getNewValue();
          ret.setEnabled(val.booleanValue());
        }
      }
    });
    
    return ret;
  }
  
  /** Sets up all buttons for the toolbar except for undo and redo, which use _createManualToolbarButton. */
  public JButton _createToolbarButton(Action a) {
    boolean useText = DrJava.getConfig().getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean useIcons = DrJava.getConfig().getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
    Font buttonFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);
    
    final JButton result = new UnfocusableButton(a);
    result.setText((String) a.getValue(Action.DEFAULT));
    result.setFont(buttonFont);
    if (!useIcons) result.setIcon(null);
    if (!useText && (result.getIcon() != null)) result.setText("");
    return result;
  }
  
  /** Removes the button b from the toolbar and creates new button in its place. */
  public JButton _updateToolbarButton(JButton b, Action a) {
    final JButton result = _createToolbarButton(a);
    
    int index = _toolBar.getComponentIndex(b);
    _toolBar.remove(b);
    _toolBar.add(result, index);
    
    _fixToolbarHeights();
    
    return result;
  }
  
  /** Sets up the toolbar with several useful buttons.  Most buttons are always enabled, but those that are not are
    * maintained in fields to allow enabling and disabling.
    */
  private void _setUpToolBar() {
    
    _toolBar.setFloatable(false);
    
//     _toolBar.addSeparator();
    
    // New, open, save, close
    _toolBar.add(_createToolbarButton(_newAction));
    _toolBar.add(_createToolbarButton(_openFileOrProjectAction));
    _toolBar.add(_createToolbarButton(_saveAction));
    _closeButton = _createToolbarButton(_closeAction);
    _toolBar.add(_closeButton);
    
    // Cut, copy, paste
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(cutAction));
    _toolBar.add(_createToolbarButton(copyAction));
    _toolBar.add(_createToolbarButton(pasteAction));
    
    // Undo, redo
    // Simple workaround, for now, for bug # 520742:
    // Undo/Redo button text in JDK 1.3
    // We just manually create the JButtons, and we *don't* set up
    // PropertyChangeListeners on the action's name.
    //_toolBar.addSeparator();
    
    _toolBar.add(_undoButton);
    _toolBar.add(_redoButton);
    
    // Find
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(_findReplaceAction));
    
    // Compile, reset, abort
    _toolBar.addSeparator();
    _toolBar.add(_compileButton = _createToolbarButton(_compileAllAction));
    _toolBar.add(_createToolbarButton(_resetInteractionsAction));
    //_toolBar.add(_createToolbarButton(_abortInteractionAction));
    
    // Run, Junit, and JavaDoc
    _toolBar.addSeparator();
    
    _toolBar.add(_runButton = _createToolbarButton(_runAction));
    _toolBar.add(_junitButton = _createToolbarButton(_junitAllAction));
    _toolBar.add(_createToolbarButton(_javadocAllAction));
    
    // DrJava Errors
    _toolBar.addSeparator();
    _errorsButton = _createToolbarButton(_errorsAction);
    _errorsButton.setVisible(false);
    _errorsButton.setBackground(DrJava.getConfig().getSetting(DRJAVA_ERRORS_BUTTON_COLOR));
    _toolBar.add(_errorsButton);
    /** The OptionListener for DRJAVA_ERRORS_BUTTON_COLOR. */
    OptionListener<Color> errBtnColorOptionListener = new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        _errorsButton.setBackground(oce.value);
      }
    };
    DrJava.getConfig().addOptionListener(DRJAVA_ERRORS_BUTTON_COLOR, errBtnColorOptionListener);
    
    // Correct the vertical height of the buttons.
    _fixToolbarHeights();
    
    getContentPane().add(_toolBar, BorderLayout.NORTH);
//    _updateToolBarVisible();  // created a visible GUI component during  initialization!
  }
  
  /** Sets the toolbar as either visible or invisible based on the config option. */
  private void _updateToolBarVisible() {
    _toolBar.setVisible(DrJava.getConfig().getSetting(TOOLBAR_ENABLED));
  }  
  
  /** Update the toolbar's buttons, following any change to TOOLBAR_ICONS_ENABLED, TOOLBAR_TEXT_ENABLED, or 
    * FONT_TOOLBAR (name, style, text)
    */
  private void _updateToolbarButtons() {
    _updateToolBarVisible();
    Component[] buttons = _toolBar.getComponents();
    
    Font toolbarFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);
    boolean iconsEnabled = DrJava.getConfig().getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
    boolean textEnabled = DrJava.getConfig().getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
    
    for (int i = 0; i< buttons.length; i++) {
      
      if (buttons[i] instanceof JButton) {
        
        JButton b = (JButton) buttons[i];
        Action a = b.getAction();
        
        // Work-around for strange configuration of undo/redo buttons
        /*
         if (a == null) {
         ActionListener[] al = b.getActionListeners(); // 1.4 only
         
         for (int j=0; j<al.length; j++) {
         if (al[j] instanceof Action) {
         a = (Action) al[j];
         break;
         }
         }
         }
         */
        
        b.setFont(toolbarFont);
        
        if (a == null) {
          if (b == _undoButton) a = _undoAction;
          else if (b == _redoButton) a = _redoAction;
          else continue;
        }
        
        if (b.getIcon() == null) {
          if (iconsEnabled) b.setIcon( (Icon) a.getValue(Action.SMALL_ICON));
        }
        else if (!iconsEnabled && b.getText().equals(""))  b.setIcon(null);
        
        if (b.getText().equals("")) {
          if (textEnabled) b.setText( (String) a.getValue(Action.DEFAULT));
        }
        else if (!textEnabled && b.getIcon() != null) b.setText("");
        
      }
    }
    
    // Correct the vertical height of the buttons.
    _fixToolbarHeights();
  }
  
  /** Ensures that all toolbar buttons have the same height. */
  private void _fixToolbarHeights() {
    Component[] buttons = _toolBar.getComponents();
    
    // First, find the maximum height of all the buttons.
    int max = 0;
    for (int i = 0; i< buttons.length; i++) {
      // We only care about the JButtons.
      if (buttons[i] instanceof JButton) {
        JButton b = (JButton) buttons[i];
        
        // reset any preferred size we have set
        b.setPreferredSize(null);
        
        // get the preferred height, since that's what we want to use
        Dimension d = b.getPreferredSize();
        int cur = (int) d.getHeight();
        if (cur > max) {
          max = cur;
        }
      }
    }
    
    // Now set all button heights to the max.
    for (int i = 0; i< buttons.length; i++) {
      // We only care about the JButtons.
      if (buttons[i] instanceof JButton) {
        JButton b = (JButton) buttons[i];
        Dimension d = new Dimension((int) b.getPreferredSize().getWidth(), max);
        
        // JToolBar inexplicably uses the max size
        // also set preferred size for consistency
        b.setPreferredSize(d);
        b.setMaximumSize(d);
      }
    }
    
    // _toolbar.revalidate();
  }
  
  /** Sets up the status bar with the filename field. Only called from MainFrame constructor. */
  private void _setUpStatusBar() {
    
    // Initialize the 3 labels:
    
    _statusField.setFont(_statusField.getFont().deriveFont(Font.PLAIN));
    _statusReport.setHorizontalAlignment(SwingConstants.RIGHT);
    
    JPanel fileNameAndMessagePanel = new JPanel(new BorderLayout());
    fileNameAndMessagePanel.add(_statusField, BorderLayout.CENTER);
    fileNameAndMessagePanel.add(_statusReport, BorderLayout.EAST);
    
    _currLocationField.setFont(_currLocationField.getFont().deriveFont(Font.PLAIN));
    _currLocationField.setHorizontalAlignment(SwingConstants.RIGHT);
    _currLocationField.setPreferredSize(new Dimension(165,12));
//    _currLocationField.setVisible(true);
    
    // Initialize the status bar panel
//    SpringLayout layout = new SpringLayout();
    _statusBar.add( fileNameAndMessagePanel, BorderLayout.CENTER );
//    _statusBar.add( sbMessagePanel, BorderLayout.CENTER );
    _statusBar.add( _currLocationField, BorderLayout.EAST );
    _statusBar.setBorder(
                         new CompoundBorder(new EmptyBorder(2,2,2,2),
                                            new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                                                               new EmptyBorder(2,2,2,2))));
    getContentPane().add(_statusBar, BorderLayout.SOUTH);
    
//     //Adjust constraints for the fileName label so it's next to the left edge.
//     layout.getConstraints(_statusField).setX(Spring.constant(0));
//     
//     //Adjust constraints for the message label so it's spaced a bit from the right.
//     //and doesn't interfere with the left-most label
//     layout.putConstraint(SpringLayout.EAST, _statusReport, -65,
//     SpringLayout.EAST, _statusBar);
//     
//     //Adjust constraints for the location label so it's next to the right edge.
//     layout.putConstraint(SpringLayout.EAST, _currLocationField, 0,
//     SpringLayout.EAST, _statusBar);
//     
//     //Adjust constraints for the panel to set its size
//     layout.putConstraint(SpringLayout.SOUTH, _statusBar, 0,
//     SpringLayout.SOUTH, _currLocationField);
  }
  
  /** Inner class to handle updating the current position in the document.  Registered with the DefinitionsPane. **/
  private class PositionListener implements CaretListener {
    
    /* Cached caret coordinates */
    private int _offset;
    private int _line;
    private int _col;
    
    // The following method only runs in the event thread because it is called from DefinitionsPane
    public void caretUpdate(final CaretEvent ce) {
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      int offset = ce.getDot();
      try {
        if (offset == _offset + 1 && doc.getText(_offset, 1).charAt(0) != '\n') {
          _col += 1;
          updateLocation(_line, _col); 
          return;
        }
      }
      catch(BadLocationException e) { /* fall through */ }
      
      Element root = doc.getDefaultRootElement();
      int line = root.getElementIndex(offset); 
      _line = line + 1;     // line numbers are 1-based
      _col = offset - root.getElement(line).getStartOffset();
      updateLocation(_line, _col);  
    }
    
    
    // This method appears safe outside the event thread
    public void updateLocation() {
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      doc.acquireReadLock();  // lock to ensure consistency of two reads from doc
      try { updateLocation(doc.getCurrentLine(), doc.getCurrentCol()); }
      finally { doc.releaseReadLock(); }
    }
    
    private void updateLocation(int line, int col) { // Not run in event thread because setText is thread safe.
      _currLocationField.setText(line + ":" + col +" \t");  // Space before "\t" required on Mac to avoid obscuring
//  Lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//      _model.getParsingControl().delay();
    }
    
    public int lastLine() { return _line; }
    public int lastCol() { return _col; }
  }
  
  /* Only called from MainFrame constructor. */
  private void _setUpTabs() {
    
    // Interactions
    _interactionsController.setPrevPaneAction(_switchToPreviousPaneAction);
    _interactionsController.setNextPaneAction(_switchToNextPaneAction);
    
    JScrollPane interactionsScroll = 
      new BorderlessScrollPane(_interactionsPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _interactionsContainer.add(interactionsScroll, BorderLayout.CENTER);
    
    if (_showDebugger) {
      // hook highlighting listener to breakpoint manager
      _model.getBreakpointManager().addListener(new RegionManagerListener<Breakpoint>() {
        /* Called when a breakpoint is added. Only runs in event thread. */
        public void regionAdded(final Breakpoint bp) {
          DefinitionsPane bpPane = getDefPaneGivenODD(bp.getDocument());
          _documentBreakpointHighlights.
            put(bp, bpPane.getHighlightManager().
                  addHighlight(bp.getStartOffset(), bp.getEndOffset(), 
                               bp.isEnabled() ? DefinitionsPane.BREAKPOINT_PAINTER
                                 : DefinitionsPane.DISABLED_BREAKPOINT_PAINTER));
          _updateDebugStatus();
        }
        
        /** Called when a breakpoint is changed. Only runs in event thread. */
        public void regionChanged(Breakpoint bp) { 
          regionRemoved(bp);
          regionAdded(bp);
        }
        
        /** Called when a breakpoint is removed. Only runs in event thread. */
        public void regionRemoved(final Breakpoint bp) {      
          HighlightManager.HighlightInfo highlight = _documentBreakpointHighlights.get(bp);
          if (highlight != null) highlight.remove();
          _documentBreakpointHighlights.remove(bp);
        }
      });
    }
    
    // hook highlighting listener to bookmark manager
    _model.getBookmarkManager().addListener(new RegionManagerListener<DocumentRegion>() { 
      // listener methods only run in the event thread
      public void regionAdded(DocumentRegion r) {
        DefinitionsPane bpPane = getDefPaneGivenODD(r.getDocument());
        _documentBookmarkHighlights.
          put(r, bpPane.getHighlightManager().
                addHighlight(r.getStartOffset(), r.getEndOffset(), DefinitionsPane.BOOKMARK_PAINTER));
      }
      public void regionChanged(DocumentRegion r) { 
        regionRemoved(r);
        regionAdded(r);
      }
      public void regionRemoved(DocumentRegion r) {
        HighlightManager.HighlightInfo highlight = _documentBookmarkHighlights.get(r);
        if (highlight != null) highlight.remove();
        _documentBookmarkHighlights.remove(r);
      }
    });
    
    _tabbedPane.addChangeListener(new ChangeListener () {
      public void stateChanged(ChangeEvent e) {
//        System.err.println("_tabbedPane.stateChanged called with event " + e);
        clearStatusMessage();
        
        if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
          // Use SwingUtilities because this action must execute AFTER all pending events in the event queue
//        System.err.println("Interactions Container Selected");
          _interactionsContainer.setVisible(true);  // kluge to overcome subtle focus bug
          EventQueue.invokeLater(new Runnable() { 
            public void run() { _interactionsContainer.requestFocusInWindow(); } 
          });
        }
        else if (_tabbedPane.getSelectedIndex() == CONSOLE_TAB) {
          // Use SwingUtilities because this action must execute AFTER all pending events in the event queue
//          System.err.println("Console Scroll Selected");
          EventQueue.invokeLater(new Runnable() { public void run() { _consoleScroll.requestFocusInWindow(); } });
        }
        // Update error highlights?
        if (_currentDefPane != null) {
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.removeErrorHighlight(); // removes the highlighting whenever the current tabbed pane is switched
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
        }
      }
    });
    
    _tabbedPane.add("Interactions", _interactionsContainer);
    _tabbedPane.add("Console", _consoleScroll);
    
    _interactionsPane.addKeyListener(_historyListener);
    _interactionsPane.addFocusListener(_focusListenerForRecentDocs);
    
    _consoleScroll.addKeyListener(_historyListener);
    _consoleScroll.addFocusListener(_focusListenerForRecentDocs);
    
    
    _tabs.addLast(_compilerErrorPanel);
    _tabs.addLast(_junitErrorPanel);
    _tabs.addLast(_javadocErrorPanel);
    _tabs.addLast(_findReplace);
    if (_showDebugger) { _tabs.addLast(_breakpointsPanel); }
    _tabs.addLast(_bookmarksPanel);
    
    _interactionsContainer.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { 
        EventQueue.invokeLater(new Runnable() { 
          public void run() {
//            System.err.println("Requesting focus in interactions pane");
            _interactionsPane.requestFocusInWindow(); 
          }
        });
      }
    });
    
    _interactionsPane.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _interactionsContainer; }
    });
    _consolePane.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _consoleScroll; }
    });
    _compilerErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _compilerErrorPanel; }
    });
    _junitErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _junitErrorPanel; }
    });
    _javadocErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _javadocErrorPanel; }
    });
    _findReplace.getFindField().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _findReplace; }
    });
    if (_showDebugger) {
      _breakpointsPanel.getMainPanel().addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) { _lastFocusOwner = _breakpointsPanel; }
      });
    }
    _bookmarksPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _lastFocusOwner = _bookmarksPanel; }
    });
  }
  
  /** Realizes this MainFrame by setting it visibile and configures the tabbed Pane. */
  public void start() {
    
    // Make the MainFrame visible and show the compiler tab
    EventQueue.invokeLater(new Runnable() { 
      public void run() { 
        setVisible(true);
        _compilerErrorPanel.setVisible(true);
        showTab(_compilerErrorPanel); 
        /* The following two step sequence was laboriously developed by trial and error; without it the _tabbedPane
         * does not display properly. */
        _tabbedPane.invalidate();
        _tabbedPane.repaint();
      } 
    });
  }
  
  /** Sets up the context menu to show in the document pane. */
  private void _setUpContextMenus() {      
    // pop-up menu for a folder in tree view
//    _navPaneFolderPopupMenu = new JPopupMenu();
    /*
     * Phil Repicky -smallproj
     * 2/14/2005
     * Make these do something:
     * _navPaneFolderPopupMenu.add("Open a File in this Folder Action");
     * _navPaneFolderPopupMenu.add("Make a New File in this Folder Action");
     */
//    _navPaneFolderPopupMenu.add(_newFileFolderAction);
//    _navPaneFolderPopupMenu.add(_openOneFolderAction);
//    _navPaneFolderPopupMenu.add(_openAllFolderAction);
//    _navPaneFolderPopupMenu.add(_closeFolderAction);
//    _navPaneFolderPopupMenu.add(_compileFolderAction);
//    _navPaneFolderPopupMenu.add(_junitFolderAction);
    
//    _navPanePopupMenuForRoot = new JPopupMenu();
//    _navPanePopupMenuForRoot.add(_saveProjectAction);
//    _navPanePopupMenuForRoot.add(_closeProjectAction);
//    _navPanePopupMenuForRoot.addSeparator();
//    _navPanePopupMenuForRoot.add(_compileProjectAction);
//    _navPanePopupMenuForRoot.add(_runProjectAction);
//    _navPanePopupMenuForRoot.add(_junitProjectAction);
//    _navPanePopupMenuForRoot.addSeparator();
//    _navPanePopupMenuForRoot.add(_projectPropertiesAction);
    
//    _navPanePopupMenuForExternal = new JPopupMenu();
//    _navPanePopupMenuForExternal.add(_saveAction);
//    _navPanePopupMenuForExternal.add(_saveAsAction);
//    _navPanePopupMenuForExternal.add(_renameAction);
//    _navPanePopupMenuForExternal.add(_revertAction);
//    _navPanePopupMenuForExternal.addSeparator();
//    _navPanePopupMenuForExternal.add(_closeAction);
//    _navPanePopupMenuForExternal.addSeparator();
//    _navPanePopupMenuForExternal.add(_printDefDocAction);
//    _navPanePopupMenuForExternal.add(_printDefDocPreviewAction);
//    _navPanePopupMenuForExternal.addSeparator();
//    _navPanePopupMenuForExternal.add(_compileAction);
//    _navPanePopupMenuForExternal.add(_junitAction);
//    _navPanePopupMenuForExternal.add(_javadocCurrentAction);
//    _navPanePopupMenuForExternal.add(_runAction);
//    _navPanePopupMenuForExternal.addSeparator();
//    _navPanePopupMenuForExternal.add(_moveToAuxiliaryAction);
    
//    _navPanePopupMenuForAuxiliary = new JPopupMenu();
//    _navPanePopupMenuForAuxiliary.add(_saveAction);
//    _navPanePopupMenuForAuxiliary.add(_saveAsAction);
//    _navPanePopupMenuForAuxiliary.add(_renameAction);
//    _navPanePopupMenuForAuxiliary.add(_revertAction);
//    _navPanePopupMenuForAuxiliary.addSeparator();
//    _navPanePopupMenuForAuxiliary.add(_closeAction);
//    _navPanePopupMenuForAuxiliary.addSeparator();
//    _navPanePopupMenuForAuxiliary.add(_printDefDocAction);
//    _navPanePopupMenuForAuxiliary.add(_printDefDocPreviewAction);
//    _navPanePopupMenuForAuxiliary.addSeparator();
//    _navPanePopupMenuForAuxiliary.add(_compileAction);
//    _navPanePopupMenuForAuxiliary.add(_junitAction);
//    _navPanePopupMenuForAuxiliary.add(_javadocCurrentAction);
//    _navPanePopupMenuForAuxiliary.add(_runAction);
//    _navPanePopupMenuForAuxiliary.addSeparator();
//    _navPanePopupMenuForAuxiliary.add(_removeAuxiliaryAction);
    
    // NavPane menu
//    _navPanePopupMenu = new JPopupMenu();
//    _navPanePopupMenu.add(_saveAction);
//    _navPanePopupMenu.add(_saveAsAction);
//    _navPanePopupMenu.add(_renameAction);
//    _navPanePopupMenu.add(_revertAction);
//    _navPanePopupMenu.addSeparator();
//    _navPanePopupMenu.add(_closeAction);
//    _navPanePopupMenu.addSeparator();
//    _navPanePopupMenu.add(_printDefDocAction);
//    _navPanePopupMenu.add(_printDefDocPreviewAction);
//    _navPanePopupMenu.addSeparator();
//    _navPanePopupMenu.add(_compileAction);
//    _navPanePopupMenu.add(_junitAction);
//    _navPanePopupMenu.add(_javadocCurrentAction);
//    _navPanePopupMenu.add(_runAction);
    
    _model.getDocCollectionWidget().addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        boolean showContextMenu = true;
        if (!_model.getDocumentNavigator().isSelectedAt(e.getX(), e.getY())) {
          // click on a item that wasn't selected, change selection
          showContextMenu = _model.getDocumentNavigator().selectDocumentAt(e.getX(), e.getY());
        }
        if (showContextMenu) {
          boolean rootSelected = _model.getDocumentNavigator().isRootSelected();
          boolean folderSelected = false;
          boolean docSelected = false;
          boolean externalSelected = false;
          boolean auxiliarySelected = false;
          boolean externalBinSelected = false;
          boolean auxiliaryBinSelected = false;
          
          final int docSelectedCount = _model.getDocumentNavigator().getDocumentSelectedCount();          
          final int groupSelectedCount = _model.getDocumentNavigator().getGroupSelectedCount();
          try {
            java.util.Set<String> groupNames = _model.getDocumentNavigator().getNamesOfSelectedTopLevelGroup();
            
            if (docSelectedCount>0) {
              // when documents are selected, ignore all other options and only deal with documents
              rootSelected = false;
              if (groupNames.contains(_model.getSourceBinTitle())) {
                // a document in the "[ Source Files ]" bin is selected
                docSelected = true;
              }
              if (groupNames.contains(_model.getExternalBinTitle())) {
                // a document in the "[ External Files ]" bin is selected
                externalSelected = true;
              }
              if (groupNames.contains(_model.getAuxiliaryBinTitle())) {
                // a document in the "[ Included External Files ]" bin is selected
                auxiliarySelected = true;
              }
            }
            else {
              // no document selected, check other options
              if (groupSelectedCount>0) {
                // at least one folder is selected
                if (!_model.getDocumentNavigator().isTopLevelGroupSelected()) {
                  // it is really a folder and not a top level bin, e.g. "[ Source Files ]"
                  folderSelected = true;
                }
                else {
                  // it is a top level bin, e.g. "[ Source Files ]"
                  if (groupNames.contains(_model.getSourceBinTitle())) {
                    // the "[ Source Files ]" bin is selected, treat as normal folder
                    folderSelected = true;
                  }
                  if (groupNames.contains(_model.getExternalBinTitle())) {
                    // the "[ External Files ]" bin is selected
                    externalBinSelected = true;
                  }
                  if (groupNames.contains(_model.getAuxiliaryBinTitle())) {
                    // the "[ Included External Files ]" bin is selected
                    auxiliaryBinSelected = true;
                  }
                }
              }
            }
          }
          catch(GroupNotSelectedException ex) {
            // we're looking at the root of the tree, or we're in list view...
            if (_model.isProjectActive()) {
              // project view, so the root has been selected
              rootSelected = true;
            }
            else {
              // list view, so treat it as simple documents
              docSelected = true;
              rootSelected = false;
              folderSelected = false;
              externalSelected = false;
              auxiliarySelected = false;
              externalBinSelected = false;
              auxiliaryBinSelected = false;
            }
          }
          
          if (!rootSelected && !folderSelected && !docSelected && !externalSelected &&
              !auxiliarySelected && !externalBinSelected && !auxiliaryBinSelected) {
            // nothing selected, don't display anything
            return;
          }
          
          final JPopupMenu m = new JPopupMenu();
          if (docSelectedCount==0) { docSelected = externalSelected = auxiliarySelected = false; }
          if (groupSelectedCount==0) { folderSelected = false; }
          
          if (rootSelected) {
            // root selected
            m.add(Utilities.createDelegateAction("Save Project", _saveProjectAction));
            m.add(Utilities.createDelegateAction("Close Project", _closeProjectAction));
            m.add(_compileProjectAction);
            m.add(_runProjectAction);
            m.add(_junitProjectAction);
            m.add(_projectPropertiesAction);
          }
          if (folderSelected) {
            // folder selected
            if (m.getComponentCount()>0) { m.addSeparator(); }
            if (groupSelectedCount==1) {
              // "New File in Folder" and "Open File in Folder" only work if exactly
              // one folder is selected
              m.add(_newFileFolderAction);
              m.add(_openOneFolderAction);
              
              // get singular/plural right
              m.add(Utilities.createDelegateAction("Open All Files in Folder", _openAllFolderAction));
              m.add(_closeFolderAction);
              m.add(_compileFolderAction);
              m.add(_junitFolderAction);
            }
            else if (groupSelectedCount>1) {
              if (!externalBinSelected && !auxiliaryBinSelected) {
                // open only makes sense if it's real folders, and not
                // the external or auxiliary bins
                m.add(Utilities.createDelegateAction("Open All Files in All Folders ("+groupSelectedCount+")",
                                                     _openAllFolderAction));
              }
              m.add(Utilities.createDelegateAction("Close All Folders ("+groupSelectedCount+")",
                                                   _closeFolderAction));
              m.add(Utilities.createDelegateAction("Compile All Folders ("+groupSelectedCount+")",
                                                   _compileFolderAction));
              m.add(Utilities.createDelegateAction("Test All Folders ("+groupSelectedCount+")",
                                                   _junitFolderAction));
              
            }
          }
          if (docSelected || externalSelected || auxiliarySelected) {
            // some kind of document selected
            if (m.getComponentCount()>0) { m.addSeparator(); }
            if (docSelectedCount==1) {
              m.add(Utilities.createDelegateAction("Save File", _saveAction));
              m.add(Utilities.createDelegateAction("Save File As...", _saveAsAction));
              m.add(Utilities.createDelegateAction("Rename File", _renameAction));
              m.add(Utilities.createDelegateAction("Revert File to Saved", _revertAction));
              m.add(Utilities.createDelegateAction("Close File", _closeAction));
              m.add(Utilities.createDelegateAction("Print File...", _printDefDocAction));
              m.add(Utilities.createDelegateAction("Print File Preview...", _printDefDocPreviewAction));
              m.add(Utilities.createDelegateAction("Compile File", _compileAction));
              m.add(Utilities.createDelegateAction("Test File", _junitAction));
              m.add(Utilities.createDelegateAction("Preview Javadoc for File", _javadocCurrentAction));
              m.add(Utilities.createDelegateAction("Run File's Main Method", _runAction));
            }
            else if (docSelectedCount>1) {
              m.add(Utilities.createDelegateAction("Save All Files ("+docSelectedCount+")", _saveAction));
              m.add(Utilities.createDelegateAction("Revert All Files to Saved ("+docSelectedCount+")", _revertAction));
              m.add(Utilities.createDelegateAction("Close All Files  ("+docSelectedCount+")", _closeAction));
              m.add(Utilities.createDelegateAction("Compile All Files ("+docSelectedCount+")", _compileAction));
              m.add(Utilities.createDelegateAction("Test All Files ("+docSelectedCount+")", _junitAction));
            }
          }
          if (externalSelected && !docSelected && !auxiliarySelected) {
            // external document selected, but no regular or auxiliary documents
            if (m.getComponentCount()>0) { m.addSeparator(); }
            if (docSelectedCount==1) {
              m.add(Utilities.createDelegateAction("Include File With Project",
                                                   _moveToAuxiliaryAction));
            }
            else if (docSelectedCount>1) {
              m.add(Utilities.createDelegateAction("Include All Files With Project ("+docSelectedCount+")",
                                                   _moveToAuxiliaryAction));
            }
          }
          if (auxiliarySelected && !docSelected && !externalSelected) {
            // auxiliary document selected, but no regular or external documents
            if (m.getComponentCount()>0) { m.addSeparator(); }
            if (docSelectedCount==1) {
              m.add(Utilities.createDelegateAction("Do Not Include File With Project",
                                                   _removeAuxiliaryAction));
            }
            else if (docSelectedCount>1) {
              m.add(Utilities.createDelegateAction("Do Not Include Any Files With Project ("+docSelectedCount+")",
                                                   _removeAuxiliaryAction));
            }
          }
          if (!folderSelected && (externalBinSelected || auxiliaryBinSelected)) {
            // external or auxiliary bin selected, but no regular folder
            if (m.getComponentCount()>0) { m.addSeparator(); }
            m.add(Utilities.createDelegateAction("Close All Files", _closeFolderAction));
            m.add(Utilities.createDelegateAction("Compile All Files", _compileFolderAction));
            m.add(Utilities.createDelegateAction("Test All Files", _junitFolderAction));
          }
          if (externalBinSelected && !auxiliaryBinSelected) {
            // external bin selected
            m.add(Utilities.createDelegateAction("Include All Files With Project",
                                                 _moveAllToAuxiliaryAction));
          }
          if (auxiliaryBinSelected && !externalBinSelected) {
            // auxiliary bin selected
            m.add(Utilities.createDelegateAction("Do Not Include Any Files With Project",
                                                 _removeAllAuxiliaryAction));
          }
          
          m.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });
//    _model.getDocCollectionWidget().addMouseListener(new RightClickMouseAdapter() {
//      protected void _popupAction(MouseEvent e) {
//        if (_model.getDocumentNavigator().selectDocumentAt(e.getX(), e.getY())) {
//          if (_model.getDocumentNavigator().isGroupSelected())
//            _navPaneFolderPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//          
//          else {
//            try {
//              String groupName = _model.getDocumentNavigator().getNameOfSelectedTopLevelGroup();
//              if (groupName.equals(_model.getSourceBinTitle()))
//                _navPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
//              else if (groupName.equals(_model.getExternalBinTitle())) {
//                INavigatorItem n = _model.getDocumentNavigator().getCurrent();
//                if (n != null) {
//                  OpenDefinitionsDocument d = (OpenDefinitionsDocument) n;
//                  if (d.isUntitled()) { _navPanePopupMenu.show(e.getComponent(), e.getX(), e.getY()); }
//                  else _navPanePopupMenuForExternal.show(e.getComponent(), e.getX(), e.getY());
//                }
//              }
//              else if (groupName.equals(_model.getAuxiliaryBinTitle()))
//                _navPanePopupMenuForAuxiliary.show(e.getComponent(), e.getX(), e.getY());
//            }
//            catch(GroupNotSelectedException ex) {
//              // we're looking at the root of the tree, or we're in list view...
//              if (_model.isProjectActive())
//                _navPanePopupMenuForRoot.show(e.getComponent(), e.getX(), e.getY());
//              else  _navPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
//            }
//          }
//        }
//      }
//    });
    
    // Interactions pane menu
    _interactionsPanePopupMenu = new JPopupMenu();
    _interactionsPanePopupMenu.add(cutAction);
    _interactionsPanePopupMenu.add(copyAction);
    _interactionsPanePopupMenu.add(pasteAction);
    _interactionsPanePopupMenu.addSeparator();
    _interactionsPanePopupMenu.add(_printInteractionsAction);
    _interactionsPanePopupMenu.add(_printInteractionsPreviewAction);
    _interactionsPanePopupMenu.addSeparator();
    _interactionsPanePopupMenu.add(_executeHistoryAction);
    _interactionsPanePopupMenu.add(_loadHistoryScriptAction);
    _interactionsPanePopupMenu.add(_saveHistoryAction);
    _interactionsPanePopupMenu.add(_clearHistoryAction);
    _interactionsPanePopupMenu.addSeparator();
    _interactionsPanePopupMenu.add(_resetInteractionsAction);
    _interactionsPanePopupMenu.add(_viewInteractionsClassPathAction);
    _interactionsPanePopupMenu.add(_copyInteractionToDefinitionsAction);
    _interactionsPane.addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        _interactionsPane.requestFocusInWindow();
        _interactionsPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
    
//    // This listener updates the _cachedCaretPosition in the _interactionsController when the cursor is manually set.
//    _interactionsPane.addMouseListener(new MouseInputAdapter() {
//      public void mouseClicked(MouseEvent e) { 
//        _interactionsController.setCachedCaretPos(_interactionsPane.viewToModel(e.getPoint()));
//      }
//    });
    _consolePanePopupMenu = new JPopupMenu();
    _consolePanePopupMenu.add(_clearConsoleAction);
    _consolePanePopupMenu.addSeparator();
    _consolePanePopupMenu.add(_printConsoleAction);
    _consolePanePopupMenu.add(_printConsolePreviewAction);
    _consolePane.addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        _consolePane.requestFocusInWindow();
        _consolePanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }
  
  private void nextRecentDoc() {
//    Utilities.show("BACK_QUOTE typed");
    if (_recentDocFrame.isVisible()) _recentDocFrame.next();
    else _recentDocFrame.setVisible(true);
  }
  
  private void prevRecentDoc() {
//    Utilities.show("BACK_QUOTE typed");
    if (_recentDocFrame.isVisible()) _recentDocFrame.prev();
    else _recentDocFrame.setVisible(true);
  }
  
  private void hideRecentDocFrame() {
    if (_recentDocFrame.isVisible()) {
      _recentDocFrame.setVisible(false);
      OpenDefinitionsDocument doc = _recentDocFrame.getDocument();
      if (doc != null) {
//        addToBrowserHistory();
        _model.setActiveDocument(doc);
//        addToBrowserHistory();
      }
    }
  }
  
  /** Create a new DefinitionsPane and JScrollPane for an open definitions document.  Package private for testing purposes.
    * @param doc The open definitions document to wrap
    * @return JScrollPane containing a DefinitionsPane for the given document.
    */
  JScrollPane _createDefScrollPane(OpenDefinitionsDocument doc) {
    DefinitionsPane pane = new DefinitionsPane(this, doc);
    
    pane.addKeyListener(_historyListener);
    pane.addFocusListener(_focusListenerForRecentDocs);
    
    // Add listeners
    _installNewDocumentListener(doc);
    ErrorCaretListener caretListener = new ErrorCaretListener(doc, pane, this);
    pane.addErrorCaretListener(caretListener);
    
    // Limiting line numbers to just lines existing in the document.
    doc.addDocumentListener(new DocumentUIListener() {
      private void updateUI() {
        EventQueue.invokeLater(new Runnable() {  // ?? Why EventQueue?
          public void run() {
            // revalidateLineNums();
            if ((_breakpointsPanel != null) && (_breakpointsPanel.isDisplayed())) { _breakpointsPanel.repaint(); }
            if ((_bookmarksPanel != null) && (_bookmarksPanel.isDisplayed())) { _bookmarksPanel.repaint(); }
            for(Pair<FindResultsPanel,Hashtable<MovingDocumentRegion, HighlightManager.HighlightInfo>> pair: _findResults) {
              FindResultsPanel panel = pair.first();
              if ((panel != null) && (panel.isDisplayed())) { panel.repaint(); }
            }
          }
        });
      }
      public void changedUpdate(DocumentEvent e) { /* updateUI(); */ }  // only attribute changes that matter are inserts and removes
      public void insertUpdate(DocumentEvent e) { updateUI(); }
      public void removeUpdate(DocumentEvent e) { updateUI(); }
    });
    
    // add a listener to update line and column.
    pane.addCaretListener(_posListener);
    
    // add a focus listener to this definitions pane.
    pane.addFocusListener(new LastFocusListener());
    
    // Add to a scroll pane
    final JScrollPane scroll = new BorderlessScrollPane(pane,
                                                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setScrollPane(scroll);
    //scroll.setBorder(null); // removes all default borders (MacOS X installs default borders)
    
    if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
      scroll.setRowHeaderView(new LineEnumRule(pane));
    }
    
    _defScrollPanes.put(doc, scroll);
    
    return scroll;
  }
  
  private void _setUpPanes() {
    // DefinitionsPane
    JScrollPane defScroll = _defScrollPanes.get(_model.getActiveDocument());
    
    // Try to create debug panel (see if JSwat is around)
    if (_showDebugger) {
      try {
        // Set the panel's size.
        int debugHeight = DrJava.getConfig().getSetting(DEBUG_PANEL_HEIGHT).intValue();
        Dimension debugMinSize = _debugPanel.getMinimumSize();
        
        // TODO: check bounds compared to entire window.
        if ((debugHeight > debugMinSize.height)) debugMinSize.height = debugHeight;
        _debugPanel.setPreferredSize(debugMinSize);
      }
      catch(NoClassDefFoundError e) {
        // Don't use the debugger
        _showDebugger = false;
      }
    } 
    
    _debugSplitPane.setBottomComponent(_debugPanel);
    _mainSplit.setResizeWeight(1.0);
    _debugSplitPane.setResizeWeight(1.0);
    getContentPane().add(_mainSplit, BorderLayout.CENTER);
    // This is annoyingly order-dependent. Since split contains _docSplitPane,
    // we need to get split's divider set up first to give _docSplitPane an
    // overall size. Then we can set _docSplitPane's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
//    int tabHeight = DrJava.getConfig().getSetting(TABS_HEIGHT).intValue();
    
    // 2*getHeight()/3
    _mainSplit.setDividerLocation(_mainSplit.getHeight() - 132);
//    _mainSplit.setDividerLocation(_mainSplit.getHeight() - tabHeight);
    _mainSplit.setOneTouchExpandable(true);
    _debugSplitPane.setOneTouchExpandable(true);
    
    int docListWidth = DrJava.getConfig().getSetting(DOC_LIST_WIDTH).intValue();
    
    // TODO: Check bounds.
    _docSplitPane.setDividerLocation(docListWidth);
    _docSplitPane.setOneTouchExpandable(true);
  }
  
  /** Switch to the JScrollPane containing the DefinitionsPane for the active document. Must run in event thread.*/
  void _switchDefScrollPane() {
    // demoted to package private protection to test the disabling editing while compiling functionality.
    // and to support brute force fix to DefinitionsPane bug on return from compile with errors
    // Added 2004-May-27
    // Notify the definitions pane that is being replaced (becoming inactive)
    _currentDefPane.notifyInactive();
    
//    Utilities.showDebug("_switchDefScrollPane called");
//    Utilities.showDebug("Right before getting the scrollPane");
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    JScrollPane scroll = _defScrollPanes.get(doc);
    
    if (scroll == null) scroll = _createDefScrollPane(doc);
    // Fix OS X scrollbar bug before switching
    
    _reenableScrollBar();
    
    int oldLocation = _docSplitPane.getDividerLocation();
    _docSplitPane.setRightComponent(scroll); //crazy line
    _docSplitPane.setDividerLocation(oldLocation);
    
    // if the current def pane is uneditable, that means we arrived here from a compile with errors.  We're
    // guaranteed to make it editable again when we return from the compilation, so we take the state
    // with us.  We guarantee only one definitions pane is un-editable at any time.
    if (_currentDefPane.isEditable()) {
      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
      _currentDefPane.notifyActive();
    }
    else {
      try { _currentDefPane.setEditable(true); }
      catch(NoSuchDocumentException e) { /* It's OK */ }
      
      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
      _currentDefPane.notifyActive();
      _currentDefPane.setEditable(false);
    }
    // reset the undo/redo menu items
    resetUndo();
    _updateDebugStatus();
  }
  
  /** Refresh the JScrollPane containing the DefinitionsPane for the active document. Must run in event thread.*/
  private void _refreshDefScrollPane() {
    // Added 2004-May-27
    // Notify the definitions pane that is being replaced (becoming inactive)
    _currentDefPane.notifyInactive();
    
//    Utilities.showDebug("_switchDefScrollPane called");
//    Utilities.showDebug("Right before getting the scrollPane");
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    JScrollPane scroll = _defScrollPanes.get(doc);
    
//    if (scroll == null) scroll = _createDefScrollPane(doc);
    // Fix OS X scrollbar bug before switching
    
    _reenableScrollBar();
    
    int oldLocation = _docSplitPane.getDividerLocation();
    _docSplitPane.setRightComponent(scroll); //crazy line
    _docSplitPane.setDividerLocation(oldLocation);
    
//    // if the current def pane is uneditable, that means we arrived here from a compile with errors.  We're
//    // guaranteed to make it editable again when we return from the compilation, so we take the state
//    // with us.  We guarantee only one definitions pane is un-editable at any time.
//    if (_currentDefPane.isEditable()) {
//      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
    _currentDefPane.notifyActive();
//    }
//    else {
//      try { _currentDefPane.setEditable(true); }
//      catch(NoSuchDocumentException e) { /* It's OK */ }
//      
//      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
//      _currentDefPane.notifyActive();
//      _currentDefPane.setEditable(false);
//    }
//    // reset the undo/redo menu items
    resetUndo();
    _updateDebugStatus();
  }
  /** Resets the undo/redo menu items */
  public void resetUndo() {
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());
  }
  
  public DefinitionsPane getDefPaneGivenODD(OpenDefinitionsDocument doc) {
    JScrollPane scroll = _defScrollPanes.get(doc);
    if (scroll == null) {
      throw new UnexpectedException(new Exception("Region set in a closed document."));
    }
    
    DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
    return pane;
  }
  
  /** Addresses Mac OS X bug where the scrollbars are disabled in one document after opening another. */
  private void _reenableScrollBar() {
    JScrollPane scroll = _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null)
      throw new UnexpectedException(new Exception("Current definitions scroll pane not found."));
    
    JScrollBar oldbar = scroll.getVerticalScrollBar();
    JScrollBar newbar = scroll.createVerticalScrollBar();
    newbar.setMinimum(oldbar.getMinimum());
    newbar.setMaximum(oldbar.getMaximum());
    newbar.setValue(oldbar.getValue());
    newbar.setVisibleAmount(oldbar.getVisibleAmount());
    newbar.setEnabled(true);
    newbar.revalidate();
    scroll.setVerticalScrollBar(newbar);
    
    // This needs to be repeated for the horizontal scrollbar
    oldbar = scroll.getHorizontalScrollBar();
    newbar = scroll.createHorizontalScrollBar();
    newbar.setMinimum(oldbar.getMinimum());
    newbar.setMaximum(oldbar.getMaximum());
    newbar.setValue(oldbar.getValue());
    newbar.setVisibleAmount(oldbar.getVisibleAmount());
    newbar.setEnabled(true);
    newbar.revalidate();
    scroll.setHorizontalScrollBar(newbar);
    scroll.revalidate();
  }
  
  /** Returns a JRadioButtonMenuItem that looks like a JCheckBoxMenuItem. This is a workaround for a known 
    * bug on OS X's version of Java. (See http://developer.apple.com/qa/qa2001/qa1154.html)
    * @param action Action for the menu item
    * @return JRadioButtonMenuItem with a checkbox icon
    */
  private JMenuItem _newCheckBoxMenuItem(Action action) {
    String RADIO_ICON_KEY = "RadioButtonMenuItem.checkIcon";
    String CHECK_ICON_KEY = "CheckBoxMenuItem.checkIcon";
    
    // Store the default radio button icon to put back later
    Object radioIcon = UIManager.get(RADIO_ICON_KEY);
    
    // Replace radio button's checkIcon with that of JCheckBoxMenuItem
    // so that our menu item looks like a checkbox
    UIManager.put(RADIO_ICON_KEY, UIManager.get(CHECK_ICON_KEY));
    JRadioButtonMenuItem pseudoCheckBox = new JRadioButtonMenuItem(action);
    
    // Put original radio button checkIcon back.
    UIManager.put(RADIO_ICON_KEY, radioIcon);
    
    return pseudoCheckBox;
  }
  
  /** Gets the absolute file, or if necessary, the canonical file.
    * @param f the file for which to get the full path
    * @return the file representing the full path to the given file
    */
  private File _getFullFile(File f) throws IOException {
    if (PlatformFactory.ONLY.isWindowsPlatform() &&
        ((f.getAbsolutePath().indexOf("..") != -1) || (f.getAbsolutePath().indexOf("./") != -1) ||
         (f.getAbsolutePath().indexOf(".\\") != -1))) {
      return f.getCanonicalFile();
    }
    return f.getAbsoluteFile();
  }
  
  /** Sets the current directory to be that of the given file. */
  private void _setCurrentDirectory(File file) {
    /* We want to use absolute paths whenever possible, since canonical paths resolve symbolic links and can be quite
     * long and unintuitive.  However, Windows blows up if you set the current directory of a JFileChooser to an 
     * absolute path with ".." in it.  In that case, we'll use the canonical path for the file chooser. (Fix for 
     * bug 707734)  Extended this to fix "./" and ".\" also (bug 774896)
     */
    try {
      file = _getFullFile(file);
      _openChooser.setCurrentDirectory(file);
      _saveChooser.setCurrentDirectory(file);
      DrJava.getConfig().setSetting(LAST_DIRECTORY, file);
    }
    catch (IOException ioe) {
      // If getCanonicalFile throws an IOException, we can't set the directory of the file chooser.  Oh well.
    }
  }
  
  /** Sets the current directory to be that of document's file.   */
  private void _setCurrentDirectory(OpenDefinitionsDocument doc) {
    try {
      File file = doc.getFile();
      if (file != null) _setCurrentDirectory(file); // if no file, leave in current directory
    }
    catch (FileMovedException fme) {
      // file was deleted, but try to go the directory
      _setCurrentDirectory(fme.getFile());
    }
  }
  
  /** Sets the font of all panes and panels to the main font. */
  private void _setMainFont() {
    
    Font f = DrJava.getConfig().getSetting(FONT_MAIN);
    
    for (JScrollPane scroll: _defScrollPanes.values()) {
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        pane.setFont(f);
        // Update the font of the line enumeration rule
        if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
          scroll.setRowHeaderView( new LineEnumRule(pane) );
        }
      }
    }
    
    // Update Interactions Pane
    _interactionsPane.setFont(f);
    _interactionsController.setDefaultFont(f);
    
    // Update Console Pane
    _consolePane.setFont(f);
    _consoleController.setDefaultFont(f);
    
    _findReplace.setFieldFont(f);
    _compilerErrorPanel.setListFont(f);
    _junitErrorPanel.setListFont(f);
    _javadocErrorPanel.setListFont(f);
  }
  
  /** Updates the text color for the doc list. */
  private void _updateNormalColor() {
    // Get the new value.
    Color norm = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);
    
    // Change the text (foreground) color for the doc list.
    _model.getDocCollectionWidget().setForeground(norm);
    
    // We also need to immediately repaint the foremost scroll pane.
    _repaintLineNums();
  }
  
  /** Updates the background color for the doc list. */
  private void _updateBackgroundColor() {
    // Get the new value.
    Color back = DrJava.getConfig().getSetting(DEFINITIONS_BACKGROUND_COLOR);
    
    // Change the background color for the doc list.
    _model.getDocCollectionWidget().setBackground(back);
    
    // We also need to immediately repaint the foremost scroll pane.
    _repaintLineNums();
  }
  
  /** Updates the font and colors of the line number display. */
  private void _updateLineNums() {
    if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
      
      // Update the font for all line number displays
      for (JScrollPane spane: _defScrollPanes.values()) { 
        
        LineEnumRule ler = (LineEnumRule) spane.getRowHeader().getView();
        ler.updateFont();
        ler.revalidate();
      }
      
      // We also need to immediately repaint the foremost scroll pane.
      _repaintLineNums();
    }
  }
  
  /** Repaints the line numbers on the active scroll pane. */
  private void _repaintLineNums() {
    JScrollPane front = _defScrollPanes.get(_model.getActiveDocument());
    if (front != null) {
      JViewport rhvport = front.getRowHeader();
      
      if (rhvport != null) {
        Component view = rhvport.getView();
        if (view != null) {
          view.repaint();
        }
      }
    }
  }
  
//  /** Revalidate the line numers, i.e. also redraw the ones not currently visible. */
//  public void revalidateLineNums() {
//    if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
//      JScrollPane sp = _defScrollPanes.get(_model.getActiveDocument());
//      if (sp!=null) {
//        LineEnumRule ler = (LineEnumRule)sp.getRowHeader().getView();
//        ler.revalidate();
//        _repaintLineNums();
//      }
//    }
//  }
  
  /** Update the row header (line number enumeration) for the definitions scroll pane. */
  private void _updateDefScrollRowHeader() {
    boolean ruleEnabled = DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue();
    
    for (JScrollPane scroll: _defScrollPanes.values()) {
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        if (scroll.getRowHeader() == null || scroll.getRowHeader().getView() == null) {
          if (ruleEnabled) scroll.setRowHeaderView(new LineEnumRule(pane));
        }
        else if (! ruleEnabled) scroll.setRowHeaderView(null);
      }
    }
  }
  
  /** Removes the current highlight. */
  private void _removeThreadLocationHighlight() {
    if (_currentThreadLocationHighlight != null) {
      _currentThreadLocationHighlight.remove();
      _currentThreadLocationHighlight = null;
    }
  }
  
  /** Disable any step timer. */
  private void _disableStepTimer() {
    synchronized(_debugStepTimer) { if (_debugStepTimer.isRunning()) _debugStepTimer.stop(); }
  }
  
  /** Checks if debugPanel's status bar displays the DEBUGGER_OUT_OF_SYNC message but the current document is 
    * in sync.  Clears the debugPanel's status bar in this case.  Does not assume that frame is in debug mode.
    * Must be executed in event thread.
    */
  private void _updateDebugStatus() {
    if (! isDebuggerReady()) return;
    
    // if the document is untitled, don't show that it is out of sync since it can't be debugged anyway
    if (_model.getActiveDocument().isUntitled() || _model.getActiveDocument().getClassFileInSync()) {
      // Hide message
      if (_debugPanel.getStatusText().equals(DEBUGGER_OUT_OF_SYNC)) _debugPanel.setStatusText("");
    } 
    else {
      // Show message
      if (_debugPanel.getStatusText().equals("")) {
        _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
      }
    }
    _debugPanel.repaint();  // display the updated panel
  }
  
  /** Ensures that the interactions pane is not editable during an interaction. */
  protected void _disableInteractionsPane() {
    // Only change GUI from event-dispatching thread
    Runnable command = new Runnable() {
      public void run() {
        _interactionsPane.setEditable(false);
        _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        if (_interactionsScriptController != null) _interactionsScriptController.setActionsDisabled();
      }
    };
    Utilities.invokeLater(command);
  }
  
  /** Ensures that the interactions pane is editable after an interaction completes. */
  protected void _enableInteractionsPane() {
    // Only change GUI from event-dispatching thread
    Runnable command = new Runnable() {
      public void run() {
//         if (isDebuggerReady()) {
//         _disableStepTimer();
//         Debugger manager = _model.getDebugger();
//         manager.clearCurrentStepRequest();
//         _removeThreadLocationHighlight();
//         }
        
        _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        _interactionsPane.setEditable(true);
        _interactionsController.moveToEnd();
        if (_interactionsPane.hasFocus()) _interactionsPane.getCaret().setVisible(true);
        if (_interactionsScriptController != null) _interactionsScriptController.setActionsEnabled();
      }
    };
    Utilities.invokeLater(command);
  }
  
  // Comment out current selection using wing commenting.  Non-private for testing purposes only. Only runs in event thread. */
  void commentLines() {
    // Delegate everything to the DefinitionsDocument.
    OpenDefinitionsDocument openDoc = _model.getActiveDocument();
    int caretPos = _currentDefPane.getCaretPosition();
    openDoc.setCurrentLocation(caretPos);
    int start = _currentDefPane.getSelectionStart();
    int end = _currentDefPane.getSelectionEnd();
//    _currentDefPane.endCompoundEdit();
//    _currentDefPane.notifyInactive();
    int newEnd = openDoc.commentLines(start, end);
//    _currentDefPane.notifyActive();
    _currentDefPane.setCaretPosition(start+2);
    if (start != end) _currentDefPane.moveCaretPosition(newEnd);
  }
  
  // Uncomment out current selection using wing commenting.  Public for testing purposes only. Only runs in event thread. */
  public void uncommentLines() {
    // Delegate everything to the DefinitionsDocument.
    OpenDefinitionsDocument openDoc = _model.getActiveDocument();
    int caretPos = _currentDefPane.getCaretPosition();
    openDoc.setCurrentLocation(caretPos);
    int start = _currentDefPane.getSelectionStart();
    int end = _currentDefPane.getSelectionEnd();
    _currentDefPane.endCompoundEdit();
    
    //notify inactive to prevent refreshing of the DefPane every time an insertion is made
//    _currentDefPane.notifyInactive();
    openDoc.setCurrentLocation(start);
    Position startPos;
    try { startPos = openDoc.createUnwrappedPosition(start); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    int startOffset = startPos.getOffset();        
    final int newEnd = openDoc.uncommentLines(start, end);
//    _currentDefPane.notifyActive();
    if (startOffset != startPos.getOffset()) start -= 2;
    final int f_start = start;
    final boolean moveSelection = start != end;
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        _currentDefPane.setCaretPosition(f_start);
        if (moveSelection) _currentDefPane.moveCaretPosition(newEnd);
      } 
    });   
  }
  
  /** Blocks access to DrJava while the hourglass cursor is on. */
  private static class GlassPane extends JComponent {
    
    /** Creates a new GlassPane over the DrJava window. */
    public GlassPane() {
      addKeyListener(new KeyAdapter() { });
      addMouseListener(new MouseAdapter() { });
      super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
  }
  
  /** Called when a specific document and offset should be displayed. Must be executed only in the event thread.
    * @param doc Document to display
    * @param offset Offset to display
    * @param shouldHighlight true iff the line should be highlighted.
    */
  public void scrollToDocumentAndOffset(final OpenDefinitionsDocument doc, final int offset, 
                                        final boolean shouldHighlight) {
    scrollToDocumentAndOffset(doc, offset, shouldHighlight, true);
  }
  
  /** Called when a specific document and offset should be displayed. Must be executed only in the event thread.
    * @param doc Document to display
    * @param offset Offset to display
    * @param shouldHighlight true iff the line should be highlighted.
    * @param shouldAddToHistory true if the location before and after the switch should be added to the browser history
    */
  public void scrollToDocumentAndOffset(final OpenDefinitionsDocument doc, final int offset, 
                                        final boolean shouldHighlight, final boolean shouldAddToHistory) {
//    if (shouldAddToHistory) addToBrowserHistory();
    
    boolean toSameDoc = _model.getActiveDocument().equals(doc);
    
    if (! toSameDoc) {
      _model.setActiveDocument(doc);
      _findReplace.updateFirstDocInSearch();
    }
    else _model.refreshActiveDocument();
    
    Runnable command = new Runnable() {  
      public void run() {
        // get the line number after the switch of documents was made
        int lineNumber = doc.getLineOfOffset(offset) + 1;
        
        // this block occurs if the documents is already open and as such has a positive size
        if (_currentDefPane.getSize().getWidth() > 0 && _currentDefPane.getSize().getHeight() > 0) {      
          _currentDefPane.centerViewOnOffset(offset);
          _currentDefPane.requestFocusInWindow();
        }
        
        if (shouldHighlight) {
          _removeThreadLocationHighlight();
          int startOffset = doc.getOffset(lineNumber);  // Much faster to directly search back from offset!
          if (startOffset > -1) {
            int endOffset = doc.getLineEndPos(startOffset);
            if (endOffset > -1) {
              _currentThreadLocationHighlight = _currentDefPane.getHighlightManager().
                addHighlight(startOffset, endOffset, DefinitionsPane.THREAD_PAINTER);
            }
          }
        }
        
        if (_showDebugger) {
          // Give the interactions pane focus so we can debug
          _interactionsPane.requestFocusInWindow();
//          System.err.println("Showing Interactions Tab" );
//          showTab(_interactionsContainer);  // disabled to avoid switching to interactions when browsing findall results
          _updateDebugStatus();
        }
        
//        if (shouldAddToHistory) { addToBrowserHistory(); }    
      }
    };
    
    /* Comment by mgricken: If ! toSameDoc, the _currentDefPane hasn't been created yet for a new document.   
     * Consequently, we need to use EventQueue.invokeLater if ! toSameDoc. */
    /* if (toSameDoc) Utilities.invokeLater(command);  
     else */ EventQueue.invokeLater(command);
  }
  
  /** Listens to events from the debugger. */
  private class UIDebugListener implements DebugListener {
    /* Must be executed in evevt thread.*/
    public void debuggerStarted() { showDebugger(); }
    
    /* Must be executed in evevt thread.*/
    public void debuggerShutdown() {
      _disableStepTimer();
      
      hideDebugger();
      _removeThreadLocationHighlight();
    }
    
    /** Called when a step is requested on the current thread.  Must be executed in event thread. */
    public void stepRequested() {
      // Print a message if step takes a long time
      synchronized(_debugStepTimer) { if (!_debugStepTimer.isRunning()) _debugStepTimer.start(); }
    }
    
    public void currThreadSuspended() {
      _disableStepTimer();
      
      // Only change GUI from event-dispatching thread
      Runnable command = new Runnable() {
        public void run() { _setThreadDependentDebugMenuItems(true); }
      };
      Utilities.invokeLater(command);
    }
    
    /* Must be executed in the event thread. */
    public void currThreadResumed() {
      _setThreadDependentDebugMenuItems(false);
      _removeThreadLocationHighlight();
    }    
    
    /** Called when the given line is reached by the current thread in the debugger, to request that the line be 
      * displayed.  Must be executed only in the event thread.
      * @param doc Document to display
      * @param lineNumber Line to display or highlight
      * @param shouldHighlight true iff the line should be highlighted.
      */
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) {
      scrollToDocumentAndOffset(doc, doc.getOffset(lineNumber), shouldHighlight);
    }
    
    /* Must be executed in event thread. */
    public void currThreadDied() {
      assert EventQueue.isDispatchThread();
      _disableStepTimer();
      
      if (isDebuggerReady()) {
        try {
          if (!_model.getDebugger().hasSuspendedThreads()) {
            // no more suspended threads, resume default debugger state
            // all thread dependent debug menu items are disabled
            _setThreadDependentDebugMenuItems(false);
            _removeThreadLocationHighlight();
            // Make sure we're at the prompt
            // (This should really be fixed in InteractionsController, not here.)
            _interactionsController.moveToPrompt(); // there are no suspended threads, bring back prompt
          }
        }
        catch (DebugException de) {
          _showError(de, "Debugger Error", "Error with a thread in the debugger.");
        }
      }
    }
    
    public void currThreadSet(DebugThreadData dtd) { }
    public void regionAdded(final Breakpoint bp) { }
    public void breakpointReached(Breakpoint bp) { }
    public void regionChanged(Breakpoint bp) {  }
    public void regionRemoved(final Breakpoint bp) { }    
    public void watchSet(final DebugWatchData w) { }
    public void watchRemoved(final DebugWatchData w) { }
    public void threadStarted() { }
    public void nonCurrThreadDied() { }
  }
  
  /** @author jlugo */
  private class DJAsyncTaskLauncher extends AsyncTaskLauncher {
    
    protected boolean shouldSetEnabled() { return true; }
    
    protected void setParentContainerEnabled(boolean enabled) {
      if (enabled) hourglassOff(); 
      else hourglassOn();
    }
    
    protected IAsyncProgress createProgressMonitor(final String description, final int min, final int max) {
      return new IAsyncProgress() {
        private ProgressMonitor _monitor = new ProgressMonitor(MainFrame.this, description, "", min, max);
        
        public void close() { _monitor.close(); }
        public int  getMaximum() { return _monitor.getMaximum() ; }
        public int  getMillisToDecideToPopup() { return _monitor.getMillisToDecideToPopup(); }
        public int  getMillisToPopup() { return  _monitor.getMillisToPopup(); }
        public int  getMinimum() { return _monitor.getMinimum(); }
        public String  getNote() { return _monitor.getNote(); }
        public boolean  isCanceled() { return _monitor.isCanceled(); }
        public void  setMaximum(int m) { _monitor.setMaximum(m); }
        public void  setMinimum(int m) { _monitor.setMinimum(m); }
        public void  setNote(String note) { _monitor.setNote(note); }
        public void  setProgress(int nv) { _monitor.setProgress(nv); }
      };
    }
  }
  
  /** Ask the user to increase the slave's max heap setting. */
  void askToIncreaseSlaveMaxHeap() {
    String value = "set to "+DrJava.getConfig().getSetting(SLAVE_JVM_XMX)+" MB";
    if ((!("".equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX)))) &&
        ((OptionConstants.heapSizeChoices.get(0).equals(DrJava.getConfig().getSetting(SLAVE_JVM_XMX))))) { 
      value = "not set, implying the system's default";
    }
    
    String res = (String)JOptionPane.showInputDialog(MainFrame.this,
                                                     "Your program ran out of memory. You may try to enter a larger\n" +
                                                     "maximum heap size for the Interactions JVM. The maximum heap size is\n" +
                                                     "currently "+value+".\n"+
                                                     "A restart is required after changing this setting.",
                                                     "Increase Maximum Heap Size?",
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     OptionConstants.heapSizeChoices.toArray(),
                                                     DrJava.getConfig().getSetting(SLAVE_JVM_XMX));
    
    if (res != null) {
      // temporarily make MainFrame the parent of the dialog that pops up
      DrJava.getConfig().removeOptionListener(SLAVE_JVM_XMX, _slaveJvmXmxListener);
      final ConfigOptionListeners.SlaveJVMXMXListener l = new ConfigOptionListeners.SlaveJVMXMXListener(MainFrame.this);
      DrJava.getConfig().addOptionListener(SLAVE_JVM_XMX, l);
      // change the setting
      DrJava.getConfig().setSetting(SLAVE_JVM_XMX,res.trim());
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // reinstall ConfigFrame as parent
          DrJava.getConfig().removeOptionListener(SLAVE_JVM_XMX, l);
          DrJava.getConfig().addOptionListener(SLAVE_JVM_XMX, _slaveJvmXmxListener);
        }
      });
    }
    _model.getInteractionsModel().resetLastErrors();
  }
  
  /** Ask the user to increase the master's max heap setting. */
  void askToIncreaseMasterMaxHeap() {
    String value = "set to "+DrJava.getConfig().getSetting(MASTER_JVM_XMX)+" MB";
    if ((!("".equals(DrJava.getConfig().getSetting(MASTER_JVM_XMX)))) &&
        ((OptionConstants.heapSizeChoices.get(0).equals(DrJava.getConfig().getSetting(MASTER_JVM_XMX))))) { 
      value = "not set, implying the system's default";
    }
    
    String res = (String)JOptionPane.showInputDialog(MainFrame.this,
                                                     "Your program ran out of memory. You may try to enter a larger\n"+
                                                     "maximum heap size for the main JVM. The maximum heap size is\n"+
                                                     "currently "+value+".\n"+
                                                     "A restart is required after changing this setting.",
                                                     "Increase Maximum Heap Size?",
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     OptionConstants.heapSizeChoices.toArray(),
                                                     DrJava.getConfig().getSetting(MASTER_JVM_XMX));
    
    if (res!=null) {
      // temporarily make MainFrame the parent of the dialog that pops up
      DrJava.getConfig().removeOptionListener(MASTER_JVM_XMX, _masterJvmXmxListener);
      final ConfigOptionListeners.MasterJVMXMXListener l = new ConfigOptionListeners.MasterJVMXMXListener(MainFrame.this);
      DrJava.getConfig().addOptionListener(MASTER_JVM_XMX, l);
      // change the setting
      DrJava.getConfig().setSetting(MASTER_JVM_XMX,res.trim());
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // reinstall ConfigFrame as parent
          DrJava.getConfig().removeOptionListener(MASTER_JVM_XMX, l);
          DrJava.getConfig().addOptionListener(MASTER_JVM_XMX, _masterJvmXmxListener);
        }
      });
    }
    _model.getInteractionsModel().resetLastErrors();
  }
  
  /** Inner class to listen to all events in the model. */
  private class ModelListener implements GlobalModelListener {
    
    public <P,R> void executeAsyncTask(AsyncTask<P,R> task, P param, boolean showProgress, boolean lockUI) {
      new DJAsyncTaskLauncher().executeTask(task, param, showProgress, lockUI);
    }
    public void handleAlreadyOpenDocument(OpenDefinitionsDocument doc) {
//     boolean docChanged = !doc.equals(_model.getActiveDocument());
//     if (docChanged) { addToBrowserHistory(); }
      
      // Always switch to doc
      _model.setActiveDocument(doc);
      
//     // defer executing this code until after active document switch (if any) is complete
//     EventQueue.invokeLater(new Runnable() { public void run() { addToBrowserHistory(); } });
      
      // Prompt to revert if modified
      if (doc.isModifiedSinceSave()) {
        String title = "Revert to Saved?";
        String message = doc.getFileName() + " is already open and modified.\n" +
          "Would you like to revert to the version on disk?\n";
        int choice = JOptionPane.showConfirmDialog(MainFrame.this, message, title, JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
          _revert(doc);
        }
      }
    }
    
    public void newFileCreated(final OpenDefinitionsDocument doc) {
      Utilities.invokeLater(new Runnable() { public void run() { 
        _createDefScrollPane(doc);
        PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
      } });
    }
    
    private volatile int _fnfCount = 0;
    
    private boolean resetFNFCount() { return _fnfCount == 0; }
    
    private boolean someFilesNotFound() {
      PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
      return _fnfCount > 0;
    }
    
    public void filesNotFound(File... files) {
      if (files.length == 0) return;
      _fnfCount += files.length;
      
      if (files.length == 1) {
        JOptionPane.showMessageDialog(MainFrame.this,
                                      "The following file could not be found and has been removed from the project.\n"
                                        + files[0].getPath(),
                                      "File Not Found",
                                      JOptionPane.ERROR_MESSAGE);
      }
      else {
        final java.util.List<String> filePaths = new ArrayList<String>();
        for (File f : files) {
          filePaths.add(f.getPath());
        }
        
        ScrollableListDialog<String> dialog = new ScrollableListDialog.Builder<String>()
          .setOwner(MainFrame.this)
          .setTitle("Files Not Found")
          .setText("The following files could not be found and have been removed from the project.")
          .setItems(filePaths)
          .setMessageType(JOptionPane.ERROR_MESSAGE)
          .build();
        
        setPopupLoc(dialog);
        dialog.showDialog();
        PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
      }
    }
    
    public File[] filesReadOnly(File... files) {
      if (files.length == 0) return new File[0];
      _fnfCount += files.length;
      
      final ArrayList<String> choices = new java.util.ArrayList<String>();
      choices.add("Yes");
      choices.add("No");
      final java.util.List<String> filePaths = new ArrayList<String>();
      for (File f : files) {
        filePaths.add(f.getPath());
      }
      ScrollableListDialog<String> dialog = new ScrollableListDialog.Builder<String>()
        .setOwner(MainFrame.this)
        .setTitle("Files are Read-Only")
        .setText("The following files could not be saved because they are read-only.\n"+
                 "Do you want to overwrite them anyway?")
        .setItems(filePaths)
        .setSelectedItems(filePaths)
        .setMessageType(JOptionPane.QUESTION_MESSAGE)
        .clearButtons()
        .addButton(new JButton("Yes"))
        .addButton(new JButton("No"))
        .setSelectable(true)
        .build();

      boolean overwrite = false;
      
      if (files.length == 1) {
        int res = JOptionPane.showConfirmDialog(MainFrame.this,
                                                "The following file could not be saved because it is read-only.\n"+
                                                "Do you want to overwrite it anyway?\n"+
                                                files[0].getPath(),
                                                "File is Read-Only",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
        overwrite = (res==0);
      }
      else {
        setPopupLoc(dialog);
        dialog.showDialog();
        overwrite = (dialog.getButtonPressed()==0);
      }
      
      if (overwrite) {
        if (files.length==1) { return files; }
        else {
          File[] overwriteFiles = new File[dialog.getSelectedItems().size()];
          int i = 0;
          for(String s: dialog.getSelectedItems()) {
            overwriteFiles[i++] = new File(s);
          }
          return overwriteFiles;
        }
      }
      else {
        return new File[0];
      }
    }
    
    public void fileSaved(final OpenDefinitionsDocument doc) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          doc.documentSaved();  // used to update the document cache
          _saveAction.setEnabled(false);
          _renameAction.setEnabled(true);
          _revertAction.setEnabled(true);
          updateStatusField();
          _currentDefPane.requestFocusInWindow();
          PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
          try {
            File f = doc.getFile();
            if (! _model.inProject(f)) _recentFileManager.updateOpenFiles(f);
          }
          catch (FileMovedException fme) {
            File f = fme.getFile();
            // Recover, show it in the list anyway
            if (! _model.inProject(f)) _recentFileManager.updateOpenFiles(f);
          }
          // Check class file sync status, in case file was renamed
          _updateDebugStatus();
        }
      });
    }
    
    public void fileOpened(final OpenDefinitionsDocument doc) { 
      Utilities.invokeLater(new Runnable() { public void run() { 
        _fileOpened(doc);
        PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
      } });  
    }
    
    private void _fileOpened(final OpenDefinitionsDocument doc) {
      try {
        File f = doc.getFile();
        if (! _model.inProject(f)) {
          _recentFileManager.updateOpenFiles(f);
          PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
        }
      }
      catch (FileMovedException fme) {
        File f = fme.getFile();
        // Recover, show it in the list anyway
        if (! _model.inProject(f)) _recentFileManager.updateOpenFiles(f);
      }
    }
    
    /** NOTE: Makes certain that this action occurs in the event dispatching thread */
    public void fileClosed(final OpenDefinitionsDocument doc) {      
      Utilities.invokeLater(new Runnable() { public void run() { _fileClosed(doc); } });
    }
    
    /** Does the work of closing a file */
    private void _fileClosed(OpenDefinitionsDocument doc) {
      _recentDocFrame.closeDocument(doc);
      _removeErrorListener(doc);
      JScrollPane jsp = _defScrollPanes.get(doc);
      if (jsp != null) {
        ((DefinitionsPane)jsp.getViewport().getView()).close();
        _defScrollPanes.remove(doc);
      }
      PropertyMaps.ONLY.getProperty("DrJava", "drjava.all.files").invalidate();
    }
    
    public void fileReverted(OpenDefinitionsDocument doc) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          updateStatusField();
          _saveAction.setEnabled(false);
          _currentDefPane.resetUndo();
          _currentDefPane.hasWarnedAboutModified(false);
          _currentDefPane.setPositionAndScroll(0);
          _updateDebugStatus();
        }
      });
    }
    
    public void undoableEditHappened() {
      Utilities.invokeLater(new Runnable() {
        public void run() {      
          _currentDefPane.getUndoAction().updateUndoState();
          _currentDefPane.getRedoAction().updateRedoState();
        }
      });
    }
    
    public void activeDocumentRefreshed(final OpenDefinitionsDocument active) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
//          System.err.println("activeDocumentRefreshed");
          _recentDocFrame.pokeDocument(active);
          _refreshDefScrollPane();
          
          // Update error highlights
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
          focusOnLastFocusOwner();
        }
      });
    }
    
    public void activeDocumentChanged(final OpenDefinitionsDocument active) {
//      Utilities.show("MainFrame Listener: ActiveDocument changed to " + active);
      // code that accesses the GUI must run in the event-dispatching thread. 
      Utilities.invokeLater(new Runnable() {  // invokeAndWait can create occasional deadlocks.
        public void run() {
          _recentDocFrame.pokeDocument(active);
          _switchDefScrollPane();
          
          boolean isModified = active.isModifiedSinceSave();
          boolean canCompile = (! isModified && ! active.isUntitled());
          boolean hasName = ! active.isUntitled();
          _saveAction.setEnabled(! canCompile);
          _renameAction.setEnabled(hasName);
          _revertAction.setEnabled(hasName);
          
          // Update error highlights
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
          
          // Update FileChoosers' directory
          _setCurrentDirectory(active);
          
          // Update title and position
          updateStatusField();
          _posListener.updateLocation();
          
          // update display (adding "*") in navigatgorPane
          if (isModified) _model.getDocumentNavigator().repaint();
          
          try { active.revertIfModifiedOnDisk(); }
          catch (FileMovedException fme) { _showFileMovedError(fme); }
          catch (IOException e) { _showIOError(e); }
          
          // Change Find/Replace to the new defpane
          if (_findReplace.isDisplayed()) {
            _findReplace.stopListening();
            _findReplace.beginListeningTo(_currentDefPane);
            //uninstallFindReplaceDialog(_findReplace);
            //installFindReplaceDialog(_findReplace);
          }
//          _lastFocusOwner = _currentDefPane;
          EventQueue.invokeLater(new Runnable() { public void run() { 
            _lastFocusOwner = _currentDefPane;
//            System.err.println("Requesting focus on new active document");
            _currentDefPane.requestFocusInWindow(); 
            PropertyMaps.ONLY.getProperty("DrJava","drjava.current.file").invalidate();
          } });
        }
      });
    }
    
    public void focusOnLastFocusOwner() {
//      System.err.println("focusOnLastFocusOwner() called; _lastFocusOwner = " + _lastFocusOwner);
      _lastFocusOwner.requestFocusInWindow();
    }
    
    /** Moves focus in MainFrame to the definitions pane. */
    public void focusOnDefinitionsPane() { _currentDefPane.requestFocusInWindow(); }
    
    public void interactionStarted() {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _disableInteractionsPane();
          _runAction.setEnabled(false);
          _runProjectAction.setEnabled(false);
        }
      });
    }
    
    public void interactionEnded() {
      final InteractionsModel im = _model.getInteractionsModel();
      // a strange NullPointerException was reported in this method ([ 1938268 ])
      // just making sure that all references are non-null
      if (im != null) {
        final String lastError = im.getLastError();
        final edu.rice.cs.drjava.config.FileConfiguration config = DrJava.getConfig();
        if ((config != null) &&
            (config.getSetting(edu.rice.cs.drjava.config.OptionConstants.DIALOG_AUTOIMPORT_ENABLED))) {
          if (lastError != null) {
            // the interaction ended and there was an error
            // check that this error is different than the last one (second to last may be null):
            final String secondToLastError = im.getSecondToLastError();
            if ((secondToLastError!=null) &&
                (!lastError.equals(secondToLastError))) {
              // this aborts the auto-importing if the same class comes up twice in a row
              if (lastError.startsWith("Static Error: Undefined class '") && lastError.endsWith("'")) {
                // it was an "undefined class" exception
                // show auto-import dialog
                String undefinedClassName = lastError.substring(lastError.indexOf('\'')+1,
                                                                lastError.lastIndexOf('\''));
                _showAutoImportDialog(undefinedClassName);
              }
              else if (lastError.startsWith("java.lang.OutOfMemoryError")) {
                askToIncreaseSlaveMaxHeap();
              }
            }
          }
        }
        else {
          // reset the last errors, so the dialog works again if it is re-enabled
          im.resetLastErrors();
        }
      }
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _enableInteractionsPane();
          _runAction.setEnabled(true);
          _runProjectAction.setEnabled(_model.isProjectActive());
        }
      });
    }
    
    public void interactionErrorOccurred(final int offset, final int length) {
      Utilities.invokeLater(new Runnable() { public void run() { _interactionsPane.highlightError(offset, length); } });
    }
    
    /** Called when the active interpreter is changed.
      * @param inProgress Whether the new interpreter is currently in progress with an interaction (i.e., whether an 
      *        interactionEnded event will be fired)
      */
    public void interpreterChanged(final boolean inProgress) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _runAction.setEnabled(! inProgress);
          _runProjectAction.setEnabled(! inProgress);
          if (inProgress) _disableInteractionsPane();
          else _enableInteractionsPane();
        }
      });
    }
    
    public void compileStarted() {
      // Only change GUI from event-dispatching thread
      Utilities.invokeLater(new Runnable() {
        public void run() {
//          hourglassOn();
          showTab(_compilerErrorPanel);
          _compilerErrorPanel.setCompilationInProgress();
          _saveAction.setEnabled(false);
        }
      });
    }    
    
    public void compileEnded(File workDir, final List<? extends File> excludedFiles) {
      // Only change GUI from event-dispatching thread
      Utilities.invokeLater(new Runnable() {
        public void run() {
//          try {
          _compilerErrorPanel.reset(excludedFiles.toArray(new File[0]));
          if (isDebuggerReady()) {
//              _model.getActiveDocument().checkIfClassFileInSync();
            
            _updateDebugStatus();
          }
//          }
//          finally { hourglassOff(); }
          if ((DrJava.getConfig().getSetting(DIALOG_COMPLETE_SCAN_CLASS_FILES).booleanValue()) && 
              (_model.getBuildDirectory() != null)) {
            _scanClassFiles();
          }
          _model.refreshActiveDocument();
        }
      });
    }
    
    /** Called after the active compiler has been changed. */
    public void activeCompilerChanged() {
      String linkVersion = DrJava.getConfig().getSetting(JAVADOC_API_REF_VERSION);
      if (linkVersion.equals(JAVADOC_AUTO_TEXT)) {
        // use Java API Javadoc of the same version as the compiler
        // compiler was changed, rebuild list
        _javaAPIList = null;
        generateJavaAPIList();
      }
    }
    
    public void runStarted(final OpenDefinitionsDocument doc) {
      // Only change GUI from event-dispatching AbstractDJDocument
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Switch to the interactions pane to show results.
          showTab(_interactionsContainer);
          _lastFocusOwner = _interactionsContainer;
        }
      });
    }
    
    public void junitStarted() {
      /* Note: simpleHourglassOn() is done by various junit commands (other than junitClasses); hourglass must be off 
       * for actual testing; the balancing simpleHourglassOff() is located here and in nonTestCase */
      
      // Only change GUI from event-dispatching thread
//      new ScrollableDialog(null, "junitStarted(" + docs + ") called in MainFrame", "", "").show();
      Utilities.invokeLater(new Runnable() {
        public void run() {
          // new ScrollableDialog(null, "Ready for hourglassOn in junitStarted", "", "").show();
          
          try { showTab(_junitErrorPanel);
            _junitErrorPanel.setJUnitInProgress();
            // _junitAction.setEnabled(false);
            // _junitAllAction.setEnabled(false);
          }
          finally { hourglassOff(); }  
        }
      });
    }
    
    /** We are junit'ing a specific list of classes given their source files. */
    public void junitClassesStarted() {
      // Only change GUI from event-dispatching thread
      // new ScrollableDialog(null, "junitClassesStarted called in MainFrame", "", "").show();
      Utilities.invokeLater(new Runnable() {
        public void run() {
//          new ScrollableDialog(null, "Ready for hourglassOn in junitClassesStarted", "", "").show();
//          hourglassOn();
          showTab(_junitErrorPanel);
          _junitErrorPanel.setJUnitInProgress();
          // _junitAction.setEnabled(false);
          // _junitAllAction.setEnabled(false);
        } // no hourglassOff here because junitClasses does not perform hourglassOn
      });
    }
    
    //public void junitRunning() { }
    
    public void junitSuiteStarted(final int numTests) {
      Utilities.invokeLater(new Runnable() { public void run() { _junitErrorPanel.progressReset(numTests); } });
    }
    
    public void junitTestStarted(final String name) {
      Utilities.invokeLater(new Runnable() {
        public void run() { _junitErrorPanel.getErrorListPane().testStarted(name); /* this does nothing! */ }
      });          
    }
    
    public void junitTestEnded(final String name, final boolean succeeded, final boolean causedError) {
//      new ScrollableDialog(null, "junitTestEnded(" + name + ", " + succeeded + ", " + causedError + ")", "", "").show();
      // syncUI...?
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _junitErrorPanel.getErrorListPane().testEnded(name, succeeded, causedError); // this does nothing!
          _junitErrorPanel.progressStep(succeeded);
          _model.refreshActiveDocument();
        }
      });
    }
    
    public void junitEnded() {
      // Only change GUI from event-dispatching thread
//      new ScrollableDialog(null, "MainFrame.junitEnded() called", "", "").show();
      Utilities.invokeLater(new Runnable() {
        public void run() {
          try {
            _restoreJUnitActionsEnabled();
            _junitErrorPanel.reset();
            _model.refreshActiveDocument();
          }
          finally { 
//            new ScrollableDialog(null, "MainFrame.junitEnded() ready to return", "", "").show();
//            hourglassOff(); 
          }
        }
      });
    }
    
    /** Fire just before javadoc asynchronous thread is started. Only runs in the event thread. */
    public void javadocStarted() {
      
      assert EventQueue.isDispatchThread();
      
//      // Only change GUI from event-dispatching thread
//      Runnable command = new Runnable() {
//        public void run() {
//          // if we don't lock edits, our error highlighting might break
      hourglassOn();
      
      showTab(_javadocErrorPanel);
      _javadocErrorPanel.setJavadocInProgress();
      _javadocAllAction.setEnabled(false);
      _javadocCurrentAction.setEnabled(false);
//        }
//      };
//      Utilities.invokeLater(command);
    }
    
    
    public void javadocEnded(final boolean success, final File destDir, final boolean allDocs) {
      // Only change GUI from event-dispatching thread
      Runnable command = new Runnable() {
        public void run() {
          try {
            showTab(_javadocErrorPanel);
            _javadocAllAction.setEnabled(true);
            _javadocCurrentAction.setEnabled(true);
            _javadocErrorPanel.reset();
            _model.refreshActiveDocument();
          }
          finally { hourglassOff(); }
          
          // Display the results.
          if (success) {
            String className;
            try {
              className = _model.getActiveDocument().getQualifiedClassName();
              className = className.replace('.', File.separatorChar);
            }
            catch (ClassNameNotFoundException cnf) {
              // If there is no class name, pass the empty string as a flag.
              // We don't want to blow up here.
              className = "";
            }
            try {
              String fileName = (allDocs || className.equals("")) ?
                "index.html" : (className + ".html");
              File index = new File(destDir, fileName);
              URL address = FileOps.toURL(index.getAbsoluteFile());
              
              if (! PlatformFactory.ONLY.openURL(address)) {
                JavadocFrame _javadocFrame = new JavadocFrame(destDir, className, allDocs);
                _javadocFrame.setVisible(true);
              }
            }
            catch (MalformedURLException me) { throw new UnexpectedException(me); }
            catch (IllegalStateException ise) {
              // JavadocFrame couldn't find any output files!
              // Display a message.
              String msg =
                "Javadoc completed successfully, but did not produce any HTML files.\n" +
                "Please ensure that your access level in Preferences is appropriate.";
              JOptionPane.showMessageDialog(MainFrame.this, msg,
                                            "No output to display.",
                                            JOptionPane.INFORMATION_MESSAGE);
            }
          }
        }
      };
      Utilities.invokeLater(command);
    }
    
    public void interpreterExited(final int status) {
      // Only show prompt if option is set and not in TEST_MODE
      if (DrJava.getConfig().getSetting(INTERACTIONS_EXIT_PROMPT).booleanValue() && ! Utilities.TEST_MODE && 
          MainFrame.this.isVisible()) {
        // Synchronously pop up a dialog box concerning restarting the JVM.
        Utilities.invokeAndWait(new Runnable() {  
          public void run() {
            String msg = "The interactions window was terminated by a call " +
              "to System.exit(" + status + ").\n" +
              "The interactions window will now be restarted.";
            
            String title = "Interactions terminated by System.exit(" + status + ")";
            
            ConfirmCheckBoxDialog dialog =
              new ConfirmCheckBoxDialog(MainFrame.this, title, msg,
                                        "Do not show this message again",
                                        JOptionPane.INFORMATION_MESSAGE,
                                        JOptionPane.DEFAULT_OPTION);
            if (dialog.show() == JOptionPane.OK_OPTION && dialog.getCheckBoxValue()) {
              DrJava.getConfig().setSetting(INTERACTIONS_EXIT_PROMPT, Boolean.FALSE);
            }
          }
        });
      }
    }
    
    public void interpreterResetFailed(Throwable t) { interpreterReady(FileOps.NULL_FILE); }
    
    public void interpreterResetting() {
      // Only change GUI from event-dispatching thread
      Runnable command = new Runnable() {
        public void run() {
          Debugger dm = _model.getDebugger();
//          if (dm.isAvailable() && dm.isReady()) dm.shutdown();
//          _resetInteractionsAction.setEnabled(false);
          _junitAction.setEnabled(false);
          _junitAllAction.setEnabled(false);
          _junitProjectAction.setEnabled(false);
          _runAction.setEnabled(false);
          _runProjectAction.setEnabled(false);
          _closeInteractionsScript();
          _interactionsPane.setEditable(false);
          _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          if (_showDebugger) _toggleDebuggerAction.setEnabled(false);
        }
      };
      Utilities.invokeLater(command);
    }
    
    public void interpreterReady(File wd) {
      // Only change GUI from event-dispatching thread
      Runnable command = new Runnable() {
        public void run() {
          interactionEnded();
          _runAction.setEnabled(true);
          _runProjectAction.setEnabled(_model.isProjectActive());
          _junitAction.setEnabled(true);
          _junitAllAction.setEnabled(true);
          _junitProjectAction.setEnabled(_model.isProjectActive());
// This action should not be enabled until the slave JVM is used          
//          _resetInteractionsAction.setEnabled(true);
          if (_showDebugger) {
            _toggleDebuggerAction.setEnabled(true);
          }
//           Moved this line here from interpreterResetting since
//           it was possible to get an InputBox in InteractionsController
//           between interpreterResetting and interpreterReady.
//           Fixes bug #917054 "Interactions Reset Bug".
          _interactionsController.interruptConsoleInput();
        }
      };
      Utilities.invokeLater(command);
    }
    
    public void slaveJVMUsed() { /* _resetInteractionsAction.setEnabled(true);  */ }
    
    public void consoleReset() { }
    
    public void saveBeforeCompile() {
      // The following event thread switch supports legacy test code that calls compile methods outside of the
      // event thread.  The wait is necessary because compilation process cannot proceed until saving is complete.
      Utilities.invokeAndWait(new Runnable() {  
        public void run() {
          _saveAllBeforeProceeding
            ("To compile, you must first save ALL modified files.\n" + "Would you like to save and then compile?",
             ALWAYS_SAVE_BEFORE_COMPILE,
             "Always save before compiling");
        }
      });
    }
    
    /** Compile all open source files if this option is configured or running as a unit test.  Otherwise, pop up a
      * dialog to ask if all open source files should be compiled in order to test the program. 
      */
    public void compileBeforeJUnit(final CompilerListener testAfterCompile) {
//      System.err.println("in compileBeforeJUnit, TEST_MODE = " + Utilities.TEST_MODE);
      if (DrJava.getConfig().getSetting(ALWAYS_COMPILE_BEFORE_JUNIT).booleanValue() || Utilities.TEST_MODE) {
        // Compile all open source files
        _model.getCompilerModel().addListener(testAfterCompile);  // listener removes itself
        _compileAll();
      }
      else { // pop up a window to ask if all open files should be compiled before testing
        Utilities.invokeLater(new Runnable() {  
          public void run() {
            String title = "Must Compile All Source Files to Run Unit Tests";
            String msg = "To unit test all documents, you must first compile all out of sync source files.\n" + 
              "Would you like to compile all files and run the specified test?";
            int rc = JOptionPane.showConfirmDialog(MainFrame.this, msg, title, JOptionPane.YES_NO_OPTION); 
            
            switch (rc) {
              case JOptionPane.YES_OPTION:  // compile all open source files and test
                _model.getCompilerModel().addListener(testAfterCompile);  // listener removes itself
                _compileAll();
                break;
              case JOptionPane.CLOSED_OPTION:
              case JOptionPane.NO_OPTION:  // abort unit testing
                _model.getJUnitModel().nonTestCase(true);  // cleans up
                break;
              default:
                throw new UnexpectedException("Invalid returnCode from showConfirmDialog: " + rc);
            }
          }
        });
      }
    }
    
    public void saveBeforeJavadoc() {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _saveAllBeforeProceeding
            ("To run Javadoc, you must first save ALL modified files.\n" +
             "Would you like to save and then run Javadoc?", ALWAYS_SAVE_BEFORE_JAVADOC,
             "Always save before running Javadoc");
        }
      });
    }
    
    /** Helper method shared by all "saveBeforeX" methods.  In JUnit tests, YES option is automatically selected
      * @param message a prompt message to be displayed to the user
      * @param option the BooleanOption for the prompt dialog checkbox
      * @param checkMsg the description of the checkbox ("Always save before X")
      */
    private void _saveAllBeforeProceeding(String message, BooleanOption option, String checkMsg) {
//      new ScrollableDialog(null, "saveBeforeProceeding called in MainFrame", "", "").show();
      if (_model.hasModifiedDocuments()) {
        if (! DrJava.getConfig().getSetting(option).booleanValue() && ! Utilities.TEST_MODE) {
          ConfirmCheckBoxDialog dialog =
            new ConfirmCheckBoxDialog(MainFrame.this, "Must Save All Files to Continue", message, checkMsg);
          int rc = dialog.show();
          
          switch (rc) {
            case JOptionPane.YES_OPTION:
              _saveAll();
              // Only remember checkbox if they say yes
              if (dialog.getCheckBoxValue())  DrJava.getConfig().setSetting(option, Boolean.TRUE);
              break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
              // do nothing
              break;
            default:
              throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
          }
        }
        else _saveAll();
      }
    }
    
    /** Saves the active document which is untitled. */
    public void saveUntitled() { _saveAs(); }
    
    public void filePathContainsPound() {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          if (DrJava.getConfig().getSetting(WARN_PATH_CONTAINS_POUND).booleanValue()) {
            String msg =
              "Files whose paths contain the '#' symbol cannot be used in the\n" +
              "Interactions Pane due to a bug in Java's file to URL conversion.\n" +
              "It is suggested that you change the name of the directory\n" +
              "containing the '#' symbol.";
            
            String title = "Path Contains Pound Sign";
            
            ConfirmCheckBoxDialog dialog =
              new ConfirmCheckBoxDialog(MainFrame.this, title, msg,
                                        "Do not show this message again",
                                        JOptionPane.WARNING_MESSAGE,
                                        JOptionPane.DEFAULT_OPTION);
            if (dialog.show() == JOptionPane.OK_OPTION && dialog.getCheckBoxValue()) {
              DrJava.getConfig().setSetting(WARN_PATH_CONTAINS_POUND, Boolean.FALSE);
            }
          }
        }
      });
    }
    
    /** Event that is fired with there is nothing to test.  JUnit is never started. */ 
    public void nonTestCase(boolean isTestAll) {
      
//      Utilities.showStackTrace(new UnexpectedException("We should not have called nonTestCase"));
      
      final String message = isTestAll ?
        "There are no compiled JUnit TestCases available for execution.\n" +
        "Perhaps you have not yet compiled your test files."
        :
        "The current document is not a valid JUnit test case.\n" +
        "Please make sure that:\n" +
        "- it has been compiled and\n" +
        "- it is a subclass of junit.framework.TestCase.\n";
      
      // Not necessarily invoked from event-handling thread!
      
      Utilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(MainFrame.this, message,
                                        "Test Only Executes JUnit test cases",
                                        JOptionPane.ERROR_MESSAGE);
          // clean up as in JUnitEnded 
          try {
            showTab(_junitErrorPanel);
            _junitAction.setEnabled(true);
            _junitAllAction.setEnabled(true);
            _junitProjectAction.setEnabled(_model.isProjectActive());
            _junitErrorPanel.reset();
          }
          finally { 
            hourglassOff();
            _restoreJUnitActionsEnabled();
          }
        }});
    }
    
    /** Event that is fired when testing encounters an illegal class file.  JUnit is never started. */ 
    public void classFileError(ClassFileError e) {
      
      final String message = 
        "The class file for class " + e.getClassName() + " in source file " + e.getCanonicalPath() + " cannot be loaded.\n "
        + "When DrJava tries to load it, the following error is generated:\n" +  e.getError();
      
      // Not necessarily invoked from event-handling thread!
      
      Utilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(MainFrame.this, message,
                                        "Testing works only on valid class files",
                                        JOptionPane.ERROR_MESSAGE);
          // clean up as junitEnded except hourglassOff (should factored into a private method)
          showTab(_junitErrorPanel);
          _junitAction.setEnabled(true);
          _junitAllAction.setEnabled(true);
          _junitProjectAction.setEnabled(_model.isProjectActive());
          _junitErrorPanel.reset();
        }});
    }
    
    /** Only callable from within the event-handling thread */
    public void currentDirectoryChanged(final File dir) { _setCurrentDirectory(dir); }
    
    /** Check if the specified document has been modified. If it has, ask the user if he would like to save it 
      * and save the document if yes. Also give the user a "cancel" option to cancel doing the operation 
      * that got us here in the first place.
      *
      * @return A boolean indicating whether the user cancelled the save process.  False means cancel.
      */
    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      return _fileSaveHelper(doc, JOptionPane.YES_NO_CANCEL_OPTION);
    }
    
    private boolean _fileSaveHelper(OpenDefinitionsDocument doc, int paneOption) {
      String text,fname;
      OpenDefinitionsDocument lastActive = _model.getActiveDocument();
      if (lastActive != doc) {
        _model.setActiveDocument(doc);
      }
      boolean notFound = false;
      try {
        File file = doc.getFile();
        if (file == null) {
          fname = "Untitled file";
          text = "Untitled file has been modified. Would you like to save it?";
        }
        else {
          fname = file.getName();
          text = fname + " has been modified. Would you like to save it?";
        }
      }
      catch (FileMovedException fme) {
        // File was deleted, but use the same name anyway
        fname = fme.getFile().getName();
        text = fname + " not found on disk. Would you like to save to another file?";
        notFound = true;
      }
      
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, text, "Save " + fname + "?", paneOption);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          boolean saved = false;
          if (notFound) saved = _saveAs(); 
          else saved = _save();
          if (doc != lastActive) {
            _model.setActiveDocument(lastActive);  // breaks when "if" clause omitted
          }
          return saved;
        case JOptionPane.NO_OPTION:
          if (doc != lastActive) {
          _model.setActiveDocument(lastActive);  // breaks when "if" clause omitted
        }
          return true;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:                         // never executed
          throw new RuntimeException("Invalid option: " + rc);
      }
    }
    
    /** Check if the current document has been modified. If it has, ask the user if he would like to save it 
      * and save the document if yes.
      * @return true if quitting should continue, false if the user cancelled
      */
    public boolean quitFile(OpenDefinitionsDocument doc) { return _fileSaveHelper(doc, JOptionPane.YES_NO_CANCEL_OPTION); }
    
    /** Called to ask the listener if it is OK to revert the current document to a newer version saved on file. */
    public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
      String fname;
      if (! _model.getActiveDocument().equals(doc)) {
        _model.setActiveDocument(doc);
      }
      try {
        File file = doc.getFile();
        if (file == null) fname = "Untitled file";
        else fname = file.getName();
      }
      catch (FileMovedException fme) { fname = fme.getFile().getName(); }
      // File was deleted, but use the same name anyway
      
      String text = fname + " has changed on disk. Would you like to reload it?\n" + 
        "This will discard any changes you have made.";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, text, fname + " Modified on Disk", 
                                             JOptionPane.YES_NO_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:    return true;
        case JOptionPane.NO_OPTION:     return false;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION: return false;
        default:                        throw new RuntimeException("Invalid rc: " + rc);
      }
    }
    
    public void interactionIncomplete() { }
    
    /* Changes to the state */
    
    public void projectBuildDirChanged() {
      if (_model.getBuildDirectory() != null) {
        _cleanAction.setEnabled(true);
      }
      else _cleanAction.setEnabled(false);
    }
    
    public void projectWorkDirChanged() { }
    
    public void projectModified() {
//      _saveProjectAction.setEnabled(_model.isProjectChanged());
    }
    
    public void projectClosed() {
      Utilities.invokeAndWait(new Runnable() {  // Why the wait?
        public void run() {
          _model.getDocumentNavigator().asContainer().addKeyListener(_historyListener);
          _model.getDocumentNavigator().asContainer().addFocusListener(_focusListenerForRecentDocs);
          _model.getDocumentNavigator().asContainer().addMouseListener(_resetFindReplaceListener);
//      new ScrollableDialog(null, "Closing JUnit Error Panel in MainFrame", "", "").show();
          removeTab(_junitErrorPanel);
          _runButton = _updateToolbarButton(_runButton, _runAction);
          _compileButton = _updateToolbarButton(_compileButton, _compileAllAction);
          _junitButton = _updateToolbarButton(_junitButton, _junitAllAction);
          projectRunnableChanged();
        }
      });
    }
    
    public void projectOpened(File projectFile, FileOpenSelector files) {
      _setUpContextMenus();
      projectRunnableChanged();
      _compileButton = _updateToolbarButton(_compileButton, _compileProjectAction);
      _junitButton = _updateToolbarButton(_junitButton, _junitProjectAction);
      _recentProjectManager.updateOpenFiles(projectFile);
      open(files);
      _openProjectUpdate();
      _model.getDocumentNavigator().asContainer().addKeyListener(_historyListener);
      _model.getDocumentNavigator().asContainer().addFocusListener(_focusListenerForRecentDocs);
      _model.getDocumentNavigator().asContainer().addMouseListener(_resetFindReplaceListener);
      _model.refreshActiveDocument();
    }
    
    public void projectRunnableChanged() {
      if (_model.getMainClass() != null && _model.getMainClass().exists()) {
        _runProjectAction.setEnabled(_model.isProjectActive());
        _runButton = _updateToolbarButton(_runButton, _runProjectAction);
      }
      else {
        _runProjectAction.setEnabled(false);
        _runButton = _updateToolbarButton(_runButton, _runAction);
      }
    }
    
    public void documentNotFound(OpenDefinitionsDocument d, File f) {
      
      _model.setProjectChanged(true);
      
      String text = "File " + f.getAbsolutePath() +
        "\ncould not be found on disk!  It was probably moved\n" +
        "or deleted.  Would you like to try to find it?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, text, "File Moved or Deleted", JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.NO_OPTION) return;
      if (rc == JOptionPane.YES_OPTION) {
        try {
          File[] opened = _openSelector.getFiles(); 
          d.setFile(opened[0]);
        } 
        catch(OperationCanceledException oce) {
          // Interpret cancelled as "NO"
        }
      }
// The following line was commented out because it breaks when a user want to close but not save a deleted file      
//      else throw new DocumentClosedException(d,"Document in " + f + "closed unexpectedly");  // misnamed exception
    }
    
    public void browserChanged() { _configureBrowsing(); }
  } // End of ModelListener class
  
  public JViewport getDefViewport() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
//    new ScrollableDialog(null, "Active Document is " + doc, "", "").show();
    JScrollPane defScroll = _defScrollPanes.get(doc);
    return defScroll.getViewport();
  }
  
  public void removeTab(final Component c) {
    Utilities.invokeLater(new Runnable() {
      public void run() {  
        if(_tabbedPane.getTabCount() > 1) {
          if(_tabbedPane.getSelectedIndex() == _tabbedPane.getTabCount() - 1)
            _tabbedPane.setSelectedIndex(_tabbedPane.getSelectedIndex() - 1);
          else
            _tabbedPane.setSelectedIndex(_tabbedPane.getSelectedIndex() + 1);
          _tabbedPane.remove(c);
          ((TabbedPanel)c).setDisplayed(false);
        }
        _currentDefPane.requestFocusInWindow();
      }
    });
  }
  
  /** Shows the components passed in in the appropriate place in the tabbedPane depending on the position of
    * the component in the _tabs list.  Only runs in the event thread.
    * @param c the component to show in the tabbedPane
    */
  public void showTab(final Component c) {
    // TODO: put all of the _tabbedPane components in _tabs. eliminating special cases for interactions, console (which 
    // are always displayed)
    assert EventQueue.isDispatchThread();
//    Utilities.invokeLater(new Runnable() {
//      public void run() {
    int numVisible = 0;      
//        System.err.println("showTab called with c = " + c);
    
    if (c == _interactionsContainer) {
//          Utilities.show("InteractionsTab selected");
      _tabbedPane.setSelectedIndex(INTERACTIONS_TAB);
      c.requestFocusInWindow();
    }
    else if (c == _consoleScroll) {
      _tabbedPane.setSelectedIndex(CONSOLE_TAB);
      c.requestFocusInWindow();
    }
    else {
      for (TabbedPanel tp: _tabs) {
        if (tp == c) {
          // 2 right now is a magic number for the number of tabs always visible
          // interactions & console
          if (! tp.isDisplayed()) {
            _tabbedPane.insertTab(tp.getName(), null, tp, null, numVisible + 2);
            tp.setVisible(true);
            tp.setDisplayed(true);
            tp.repaint();
          }
          _tabbedPane.setSelectedIndex(numVisible + 2);
          
          c.requestFocusInWindow();
          return;
        }
        if (tp.isDisplayed()) numVisible++;
      }
    }
    if (_mainSplit.getDividerLocation() > _mainSplit.getMaximumDividerLocation()) 
      _mainSplit.resetToPreferredSizes(); 
//      }
//    });
  }
  
  /** Sets the location of the main divider.
   * (not currently used)
   private void _setDividerLocation() {
   int divLocation = _mainSplit.getHeight() -
   _mainSplit.getDividerSize() -
   (int)_tabbedPane.getMinimumSize().getHeight();
   if (_mainSplit.getDividerLocation() > divLocation)
   _mainSplit.setDividerLocation(divLocation);
   }*/
  
  /** Warns the user that the current file is open and query them if they wish to save over the currently open file. */
  private boolean _warnFileOpen(File f) {
    OpenDefinitionsDocument d = null;
    try { d = _model.getDocumentForFile(f); }
    catch(IOException ioe) { /* do nothing */ }
    Object[] options = {"Yes","No"};
    if (d == null) return false;
    boolean dMod = d.isModifiedSinceSave();
    String msg = "This file is already open in DrJava" + (dMod ? " and has been modified" : "") + 
      ".  Do you wish to overwrite it?";
    int choice = JOptionPane.showOptionDialog(MainFrame.this, msg, "File Open Warning", JOptionPane.YES_NO_OPTION,
                                              JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
    if (choice == JOptionPane.YES_OPTION) return _model.closeFileWithoutPrompt(d);
    return false;
  }
  
  /** Confirms with the user that the file should be overwritten.
   * @return <code>true</code> iff the user accepts overwriting.
   */
  private boolean _verifyOverwrite() {
    Object[] options = {"Yes","No"};
    int n = JOptionPane.showOptionDialog(MainFrame.this,
                                         "This file already exists.  Do you wish to overwrite the file?",
                                         "Confirm Overwrite",
                                         JOptionPane.YES_NO_OPTION,
                                         JOptionPane.QUESTION_MESSAGE,
                                         null,
                                         options,
                                         options[1]);
    return (n == JOptionPane.YES_OPTION);
  }
  
  /* Pops up a message and cleans up after unit testing has been interrupted. */
  private void _junitInterrupted(final UnexpectedException e) {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _showJUnitInterrupted(e);
        removeTab(_junitErrorPanel);
        _model.refreshActiveDocument();
        // hourglassOff();
      }
    });
  }
  
  boolean isDebuggerReady() { return _showDebugger &&  _model.getDebugger().isReady(); }
  
  /** Return the find replace dialog. Package protected for use in tests. */
  FindReplacePanel getFindReplaceDialog() { return _findReplace; }
  
  /** Builds the Hashtables in KeyBindingManager that are used to keep track of key-bindings and allows for live 
    * updating, conflict resolution, and intelligent error messages (the ActionToNameMap).  
    * IMPORTANT: Don't use this way to put actions into the KeyBindingManager if the action is a menu item. It will 
    * already have been put in.  Putting in again will cause bug #803304 "Uncomment lines wont rebind".
    */
  private void _setUpKeyBindingMaps() {
    final ActionMap _actionMap = _currentDefPane.getActionMap();
    
    KeyBindingManager.Singleton.put(KEY_BACKWARD, _actionMap.get(DefaultEditorKit.backwardAction), null, "Backward");
    KeyBindingManager.Singleton.addShiftAction(KEY_BACKWARD, DefaultEditorKit.selectionBackwardAction);
    
    KeyBindingManager.Singleton.put(KEY_BEGIN_DOCUMENT, _actionMap.get(DefaultEditorKit.beginAction), null, 
                                    "Begin Document");
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_DOCUMENT, DefaultEditorKit.selectionBeginAction);
    
//    KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _actionMap.get(DefaultEditorKit.beginLineAction), null, 
//                                    "Begin Line");
    KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _beginLineAction, null, "Begin Line");
//    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_LINE,
//                                               DefaultEditorKit.selectionBeginLineAction);
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_LINE, _selectionBeginLineAction);
    
    KeyBindingManager.Singleton.put(KEY_PREVIOUS_WORD, _actionMap.get(DefaultEditorKit.previousWordAction), null, 
                                    "Previous Word");
    KeyBindingManager.Singleton.addShiftAction(KEY_PREVIOUS_WORD, DefaultEditorKit.selectionPreviousWordAction);
    
    KeyBindingManager.Singleton.put(KEY_DOWN, _actionMap.get(DefaultEditorKit.downAction), null, "Down");
    KeyBindingManager.Singleton.addShiftAction(KEY_DOWN, DefaultEditorKit.selectionDownAction);
    
    KeyBindingManager.Singleton.put(KEY_END_DOCUMENT, _actionMap.get(DefaultEditorKit.endAction), null, "End Document");
    KeyBindingManager.Singleton.addShiftAction(KEY_END_DOCUMENT, DefaultEditorKit.selectionEndAction);
    
    KeyBindingManager.Singleton.put(KEY_END_LINE, _actionMap.get(DefaultEditorKit.endLineAction), null, "End Line");
    KeyBindingManager.Singleton.addShiftAction(KEY_END_LINE, DefaultEditorKit.selectionEndLineAction);
    
    KeyBindingManager.Singleton.put(KEY_NEXT_WORD, _actionMap.get(DefaultEditorKit.nextWordAction), null, "Next Word");
    KeyBindingManager.Singleton.addShiftAction(KEY_NEXT_WORD, DefaultEditorKit.selectionNextWordAction);
    
    KeyBindingManager.Singleton.put(KEY_FORWARD, _actionMap.get(DefaultEditorKit.forwardAction), null, "Forward");
    KeyBindingManager.Singleton.addShiftAction(KEY_FORWARD, DefaultEditorKit.selectionForwardAction);
    
    KeyBindingManager.Singleton.put(KEY_UP, _actionMap.get(DefaultEditorKit.upAction), null, "Up");
    KeyBindingManager.Singleton.addShiftAction(KEY_UP, DefaultEditorKit.selectionUpAction);
    
//    KeyBindingManager.Singleton.put(KEY_NEXT_RECENT_DOCUMENT, _nextRecentDocAction, null, "Next Recent Document");
//    KeyBindingManager.Singleton.put(KEY_PREV_RECENT_DOCUMENT, _prevRecentDocAction, null, "Previous Recent Document");
    
    // These last methods have no default selection methods
    KeyBindingManager.Singleton.put(KEY_PAGE_DOWN, _actionMap.get(DefaultEditorKit.pageDownAction), null, "Page Down");
    KeyBindingManager.Singleton.put(KEY_PAGE_UP, _actionMap.get(DefaultEditorKit.pageUpAction), null, "Page Up");
    KeyBindingManager.Singleton.put(KEY_CUT_LINE, _cutLineAction, null, "Cut Line");
    KeyBindingManager.Singleton.put(KEY_CLEAR_LINE, _clearLineAction, null, "Clear Line");
    KeyBindingManager.Singleton.put(KEY_SHIFT_DELETE_PREVIOUS, _actionMap.get(DefaultEditorKit.deletePrevCharAction), 
                                    null, "Delete Previous");
    KeyBindingManager.Singleton.put(KEY_SHIFT_DELETE_NEXT, _actionMap.get(DefaultEditorKit.deleteNextCharAction), 
                                    null, "Delete Next");
  }
  
  /** @param listener The ComponentListener to add to the open documents list
    * This method allows for testing of the dancing UI (See MainFrameTest.testDancingUI()).
    */
  public void addComponentListenerToOpenDocumentsList(ComponentListener listener) {
    _docSplitPane.getLeftComponent().addComponentListener(listener);
  }
  
  /**For test purposes only. Returns the text in the status bar. Is used to test brace matching*/
  public String getFileNameField() { return _statusField.getText(); }
  
  /**For test purposes only. Returns the edit menu*/
  public JMenu getEditMenu() { return _editMenu; }
  
  /** The OptionListener for FONT_MAIN */
  private class MainFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) { _setMainFont(); }
  }
  
  /** The OptionListener for FONT_LINE_NUMBERS */
  private class LineNumbersFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) { _updateLineNums(); }
  }
  
  /** The OptionListener for FONT_DOCLIST */
  private class DoclistFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
      _model.getDocCollectionWidget().setFont(doclistFont);
    }
  }
  
  /** The OptionListener for FONT_TOOLBAR */
  private class ToolbarFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) { _updateToolbarButtons(); }
  }
  
  /** The OptionListener for DEFINITIONS_NORMAL_COLOR */
  private class NormalColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) { _updateNormalColor(); }
  }
  
  /** The OptionListener for DEFINITIONS_BACKGROUND_COLOR */
  private class BackgroundColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) { _updateBackgroundColor(); }
  }
  
  /** The OptionListener for TOOLBAR options */
  private class ToolbarOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) { _updateToolbarButtons(); }
  }
  
  /** The OptionListener for LINEENUM_ENABLED. */
  private class LineEnumOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) { _updateDefScrollRowHeader(); }
  }
  
  /** The OptionListener for QUIT_PROMPT. */
  private class QuitPromptOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) { _promptBeforeQuit = oce.value.booleanValue(); }
  }
  
  /** The OptionListener for RECENT_FILES_MAX_SIZE. */
  private class RecentFilesOptionListener implements OptionListener<Integer> {
    public void optionChanged(OptionEvent<Integer> oce) {
      _recentFileManager.updateMax(oce.value.intValue());
      _recentFileManager.numberItems();
      _recentProjectManager.updateMax(oce.value.intValue());
      _recentProjectManager.numberItems();
    }
  }
  
  private class LastFocusListener extends FocusAdapter {
    public void focusGained(FocusEvent e) { 
      _lastFocusOwner = e.getComponent(); 
//      System.err.println("_lastFocusOwner = " + _lastFocusOwner);
    }
  };
  
  
  /** Wrapper for setPopupLoc(Window, Component) that uses the window's owner as the owner to center the popup on.
    * @param popup the Popup window
    */
  public void setPopupLoc(Window popup) {
    MainFrame.setPopupLoc(popup, (popup.getOwner() != null) ? popup.getOwner() : this);
  }
  
  /** Sets the location of the popup in a consistant way.  If the popup has an owner, the popup is centered over the
    * owner.  If the popup has no owner(owner == null), the popup is centered over the first monitor.  In either case,
    * the popup is moved and scaled if any part of it is not on the screen.  This method should be called for all popups
    * to maintain consistancy.
    * @param popup the popup window
    * @param owner the parent component for the popup
    */
  public static void setPopupLoc(Window popup, Component owner) {
    Rectangle frameRect = popup.getBounds();
    
    Point ownerLoc = null;
    Dimension ownerSize = null;
    if (owner != null) {
      ownerLoc = owner.getLocation();
      ownerSize = owner.getSize();
    }
    else {
      //for multi-monitor support
      //Question: do we want it to popup on the first monitor always?
      GraphicsDevice[] dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      Rectangle rec = dev[0].getDefaultConfiguration().getBounds();
      ownerLoc = rec.getLocation();
      ownerSize = rec.getSize();
    }
    
    // center it on owner
    Point loc = new Point(ownerLoc.x + (ownerSize.width - frameRect.width) / 2,
                          ownerLoc.y + (ownerSize.height - frameRect.height) / 2);
    frameRect.setLocation(loc);
    
    // now find the GraphicsConfiguration the popup is on
    GraphicsConfiguration gcBest = null;
    int gcBestArea = -1;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    for (GraphicsDevice gd: gs) {
      GraphicsConfiguration gc = gd.getDefaultConfiguration();
      Rectangle isect = frameRect.intersection(gc.getBounds());
      int gcArea = isect.width*isect.height;
      if (gcArea > gcBestArea) {
        gcBest = gc;
        gcBestArea = gcArea;
      }
    }
    
    // make it fit on the screen
    Rectangle screenRect = gcBest.getBounds();
    Dimension screenSize = screenRect.getSize();
    Dimension frameSize = popup.getSize();
    
    if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
    
    frameRect.setSize(frameSize);
    
    // center it on owner again
    loc = new Point(ownerLoc.x + (ownerSize.width - frameRect.width) / 2,
                    ownerLoc.y + (ownerSize.height - frameRect.height) / 2);
    frameRect.setLocation(loc);
    
    // now fit it on the screen
    if (frameRect.x < screenRect.x) frameRect.x = screenRect.x;
    if (frameRect.x + frameRect.width > screenRect.x + screenRect.width)
      frameRect.x = screenRect.x + screenRect.width - frameRect.width;
    
    if (frameRect.y < screenRect.y) frameRect.y = screenRect.y;
    if (frameRect.y + frameRect.height > screenRect.y + screenRect.height)
      frameRect.y = screenRect.y + screenRect.height - frameRect.height;
    
    popup.setSize(frameRect.getSize());
    popup.setLocation(frameRect.getLocation());
  }
  
  /** Drag and drop target. */
  DropTarget dropTarget = new DropTarget(this, this);
  
  /** Linux URI drag-and-drop data flavor. */
  private static DataFlavor uriListFlavor;
  static {
    try {
      uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
    }
    catch(ClassNotFoundException cnfe) {
      uriListFlavor = null;
    }
  }
  
  /** User dragged something into the component. */
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
  {
    dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
  }
  
  public void dragExit(DropTargetEvent dropTargetEvent) {}
  public void dragOver(DropTargetDragEvent dropTargetDragEvent) {}
  public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){}
  
  /** User dropped something on the component. */
  public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
    try {
      Transferable tr = dropTargetDropEvent.getTransferable();
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ||
          ((uriListFlavor!=null) && (tr.isDataFlavorSupported(uriListFlavor)))) {
        dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        java.util.List<File> fileList;
        if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          @SuppressWarnings("unchecked")
          java.util.List<File> data = (java.util.List<File>)tr.getTransferData(DataFlavor.javaFileListFlavor);
          fileList = data;
        }
        else {
          // work-around for Linux drag-and-drop
          // see Java bug 4899516
          @SuppressWarnings("unchecked")
          String data = (String)tr.getTransferData(uriListFlavor);
          fileList = textURIListToFileList(data);
        }
        java.util.Iterator<File> iterator = fileList.iterator();
        java.util.List<File> filteredFileList = new java.util.ArrayList<File>();
        while (iterator.hasNext()) {
          File file = iterator.next();
          if ((file.isFile()) && ((file.getName().endsWith(".java")) || 
                                  (file.getName().endsWith(".dj0")) || 
                                  (file.getName().endsWith(".dj1")) || 
                                  (file.getName().endsWith(".dj2")) || 
                                  (file.getName().endsWith(".dj0")) ||
                                  (file.getName().endsWith(".txt")))) {
            filteredFileList.add(file);
          }
          else if ((file.isFile()) && ((file.getName().endsWith(OptionConstants.EXTPROCESS_FILE_EXTENSION)))) {
            openExtProcessFile(file);
          }
          else if ((file.isFile()) && ((file.getName().endsWith(OptionConstants.EXTPROCESS_JAR_FILE_EXTENSION)))) {
            openExtProcessJarFile(file);
          }
        }
        final File[] fileArray = filteredFileList.toArray(new File[filteredFileList.size()]);
        FileOpenSelector fs = new FileOpenSelector() {
          public File[] getFiles() { return fileArray; }
        };
        open(fs);
        dropTargetDropEvent.getDropTargetContext().dropComplete(true);
      }
      else {
        dropTargetDropEvent.rejectDrop();
      }
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
      dropTargetDropEvent.rejectDrop();
    }
    catch (UnsupportedFlavorException ufe) {
      ufe.printStackTrace();
      dropTargetDropEvent.rejectDrop();
    }    
  }
  
  /** Open stand-alone external process file. */
  public static void openExtProcessFile(File file) {
    try {
      XMLConfig xc = new XMLConfig(file);
      ExecuteExternalDialog.addToMenu(xc.get("drjava/extprocess/name"),
                                      xc.get("drjava/extprocess/type"),
                                      xc.get("drjava/extprocess/cmdline"),
                                      xc.get("drjava/extprocess/jvmline"),
                                      xc.get("drjava/extprocess/workdir"));
    }
    catch(XMLConfigException xce) { /* ignore drop */ }
  }

  /** Open external process file in a jar file. */
  public static void openExtProcessJarFile(File file) {
    try {
      System.out.println("openExtProcessJarFile(file="+file+")");
      JarFile jf = new JarFile(file);
      JarEntry je = jf.getJarEntry("process.drjavaxml");
      InputStream is = jf.getInputStream(je);
      XMLConfig xc = new XMLConfig(is);
      ExecuteExternalDialog.addToMenu(xc.get("drjava/extprocess/name"),
                                      xc.get("drjava/extprocess/type"),
                                      xc.get("drjava/extprocess/cmdline"),
                                      xc.get("drjava/extprocess/jvmline"),
                                      xc.get("drjava/extprocess/workdir"));
      is.close();
      jf.close();
    }
    catch(IOException ioe) { /* ignore drop */ }
    catch(XMLConfigException xce) { /* ignore drop */ }
  }
  
  /** Convert a string with URIs to a list of files.
    * @param data string with URIs
    * @return list of files
    */
  private static java.util.List<File> textURIListToFileList(String data) {
    java.util.List<File> list = new java.util.ArrayList<File>();
    java.util.StringTokenizer st = new java.util.StringTokenizer(data, "\r\n");
    while(st.hasMoreTokens()) {
      String s = st.nextToken();
      if (s.startsWith("#")) {
        // the line is a comment (as per the RFC 2483)
        continue;
      }
      try {
        java.net.URI uri = new java.net.URI(s);
        java.io.File file = new java.io.File(uri);
        list.add(file);
      }
      catch (java.net.URISyntaxException e) {
        // malformed URI
      }
      catch (IllegalArgumentException e) {
        // the URI is not a valid 'file:' URI
      }
    }
    return list;
  }
  
  /** Reset the position of the "Open Javadoc" dialog. */
  public void resetAutoImportDialogPosition() {
    initAutoImportDialog();
    _autoImportDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_AUTOIMPORT_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_AUTOIMPORT_STATE, "default");
    }
  }
  
  /** Initialize dialog if necessary. */
  void initAutoImportDialog() {
    if (_autoImportDialog == null) {
      _autoImportPackageCheckbox = new JCheckBox("Import Package");
      _autoImportPackageCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _autoImportDialog.resetFocus();
        }
      });
      _autoImportPackageCheckbox.setMnemonic('p');
      PredictiveInputFrame.InfoSupplier<JavaAPIListEntry> info = 
        new PredictiveInputFrame.InfoSupplier<JavaAPIListEntry>() {
        public String apply(JavaAPIListEntry entry) {
          // show full class name as information
          return entry.getFullString();
        }
      };
      PredictiveInputFrame.CloseAction<JavaAPIListEntry> okAction = 
        new PredictiveInputFrame.CloseAction<JavaAPIListEntry>() {
        public String getName() { return "OK"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); }
        public Object apply(PredictiveInputFrame<JavaAPIListEntry> p) {
          String text;
          if (p.getItem() != null) {
            // if a class was selected...
            text = p.getItem().getFullString();
          }
          else {
            // otherwise use the text that was entered
            text = p.getText();
          }
          if (_autoImportPackageCheckbox.isSelected()) {
            int lastDot = text.lastIndexOf('.');
            if (lastDot>0) {
              text = text.substring(0,lastDot+1)+"*";
            }
          }
          final InteractionsModel im = _model.getInteractionsModel();
          // get the last line (the one that caused the error) and remove it from the history
          String lastLine = im.removeLastFromHistory();
          // import the selected class...
          String importLine = "import " + text + "; // auto-import";
          // ...and try to do the last line again
          final String code = importLine + ((lastLine != null)  ?  ("\n" + lastLine)  : "");
          EventQueue.invokeLater(new Runnable() { 
            public void run() {
              // interpret with the added import
              try {
                im.append(code, InteractionsDocument.DEFAULT_STYLE);
                im.interpretCurrentInteraction();
              }
              finally { hourglassOff(); }
            }
          });
          return null;
        }
      };
      PredictiveInputFrame.CloseAction<JavaAPIListEntry> cancelAction = 
        new PredictiveInputFrame.CloseAction<JavaAPIListEntry>() {
        public String getName() { return "Cancel"; }
        public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
        public Object apply(PredictiveInputFrame<JavaAPIListEntry> p) {
          // if no class was selected, do nothing
          // just reset the error information so the dialog box works next time
          _model.getInteractionsModel().resetLastErrors();
          hourglassOff();
          return null;
        }
      };
      
      java.util.ArrayList<MatchingStrategy<JavaAPIListEntry>> strategies =
        new java.util.ArrayList<MatchingStrategy<JavaAPIListEntry>>();
      strategies.add(new FragmentStrategy<JavaAPIListEntry>());
      strategies.add(new PrefixStrategy<JavaAPIListEntry>());
      strategies.add(new RegExStrategy<JavaAPIListEntry>());
      List<PredictiveInputFrame.CloseAction<JavaAPIListEntry>> actions
        = new ArrayList<PredictiveInputFrame.CloseAction<JavaAPIListEntry>>();
      actions.add(okAction);
      actions.add(cancelAction);
      _autoImportDialog = 
        new PredictiveInputFrame<JavaAPIListEntry>(MainFrame.this,
                                                   "Auto Import Class",
                                                   false, // force
                                                   true, // ignore case
                                                   info,
                                                   strategies,
                                                   actions, 1, // cancel is action 1
                                                   new JavaAPIListEntry("dummyImport", "dummyImport", null)) {
        public void setOwnerEnabled(boolean b) {
          if (b) { hourglassOff(); } else { hourglassOn(); }
        }
        protected JComponent[] makeOptions() {
          return new JComponent[] { _autoImportPackageCheckbox };
        }
      }; 
      // putting one dummy entry in the list; it will be changed later anyway
      
      if (DrJava.getConfig().getSetting(DIALOG_AUTOIMPORT_STORE_POSITION).booleanValue()) {
        _autoImportDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_AUTOIMPORT_STATE));
      }
      generateJavaAPIList();
    }
  }
  
  /** The "Auto Import" dialog instance. */
  PredictiveInputFrame<JavaAPIListEntry> _autoImportDialog = null;
  JCheckBox _autoImportPackageCheckbox;
  
  /** Imports a class. */
  void _showAutoImportDialog(String s) {
    generateJavaAPIList();
    if (_javaAPIList == null) {
      return;
    }
    List<JavaAPIListEntry> autoImportList = new ArrayList<JavaAPIListEntry>(_javaAPIList);
    if ((DrJava.getConfig().getSetting(DIALOG_COMPLETE_SCAN_CLASS_FILES).booleanValue()) &&
        (_autoImportClassList.size()>0)) {
      autoImportList.addAll(_autoImportClassList);
    }
    else {
      File projectRoot = _model.getProjectRoot();
      List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
      if (docs != null) {
        for(OpenDefinitionsDocument d: docs) {
          if (d.isUntitled()) continue;
          try {
            File rel = FileOps.makeRelativeTo(d.getRawFile(), projectRoot);
            String full = rel.toString().replace(java.io.File.separatorChar, '.');
            for (String ext: edu.rice.cs.drjava.model.compiler.CompilerModel.EXTENSIONS) {
              if (full.endsWith(ext)) {
                full = full.substring(0, full.lastIndexOf(ext));
                break;
              }
            }
            String simple = full;
            if (simple.lastIndexOf('.')>=0) {
              simple = simple.substring(simple.lastIndexOf('.')+1);
            }
            JavaAPIListEntry entry = new JavaAPIListEntry(simple, full, null);
            if (!autoImportList.contains(entry)) { autoImportList.add(entry); }
          }
          catch(IOException ioe) { /* ignore, just don't add this one */ }
          catch(SecurityException se) { /* ignore, just don't add this one */ }
        }
      }
    }
    PredictiveInputModel<JavaAPIListEntry> pim =
      new PredictiveInputModel<JavaAPIListEntry>(true, new PrefixStrategy<JavaAPIListEntry>(), autoImportList);
    pim.setMask(s);
    initAutoImportDialog();
    _autoImportDialog.setModel(true, pim); // ignore case
    hourglassOn();
    _autoImportPackageCheckbox.setSelected(false);
    _autoImportDialog.setVisible(true);
  }
  
  /** Follow a file. */
  private final Action _followFileAction = new AbstractAction("Follow File...") {
    public void actionPerformed(ActionEvent ae) {
      _followFile();
    }
  };
  
  // public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("less.txt", true);
  
  /** Open a file for following (like using "less" and F). */
  private void _followFile() {
    updateStatusField("Opening File for Following");
    // LOG.log("_followFile");
    try {      
      final File[] files = _openAnyFileSelector.getFiles();
      // LOG.log("\tfiles = "+files);
      if (files == null) { return; }
      // LOG.log("\tlength = "+files.length);
      for (final File f: files) {
        if (f == null) continue;
        // LOG.log("\tf = "+f);
        String end = f.getName();
        int lastIndex = end.lastIndexOf(File.separatorChar);
        if (lastIndex>=0) {
          end = end.substring(lastIndex+1);
        }
        final LessPanel panel = new LessPanel(this, "Follow: "+end, f);
        _tabs.addLast(panel);
        panel.getMainPanel().addFocusListener(new FocusAdapter() {
          public void focusGained(FocusEvent e) { _lastFocusOwner = panel; }
        });
        panel.setVisible(true);
        showTab(panel);
        _tabbedPane.setSelectedComponent(panel);
        // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
        EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
      }
    }
    catch(OperationCanceledException oce) { /* ignore */ }
  }
  
  /** Execute an external process. */
  private final Action _executeExternalProcessAction = new AbstractAction("New External Process...") {
    public void actionPerformed(ActionEvent ae) {
      _executeExternalProcess();
    }
  };
  
  /** Execute an external process and monitor its output. */
  private void _executeExternalProcess() {
    _executeExternalDialog.setVisible(true);
  }
  
  /** The execute external dialog. */
  private final ExecuteExternalDialog _executeExternalDialog;
  
  /** Initializes the "Execute External Process" dialog. */
  private void initExecuteExternalProcessDialog() {
    if (DrJava.getConfig().getSetting(DIALOG_EXTERNALPROCESS_STORE_POSITION).booleanValue()) {
      _executeExternalDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_EXTERNALPROCESS_STATE));
    }
  }
  
  /** Reset the position of the "Execute External Process" dialog. */
  public void resetExecuteExternalProcessPosition() {
    _executeExternalDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_EXTERNALPROCESS_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_EXTERNALPROCESS_STATE, "default");
    }
  }
  
  /** The edit external dialog. */
  private final EditExternalDialog _editExternalDialog;
  
  /** Initializes the "Edit External Process" dialog. */
  private void initEditExternalProcessDialog() {
    if (DrJava.getConfig().getSetting(DIALOG_EDITEXTERNALPROCESS_STORE_POSITION).booleanValue()) {
      _editExternalDialog.setFrameState(DrJava.getConfig().getSetting(DIALOG_EDITEXTERNALPROCESS_STATE));
    }
  }
  
  /** Reset the position of the "Edit External Process" dialog. */
  public void resetEditExternalProcessPosition() {
    _editExternalDialog.setFrameState("default");
    if (DrJava.getConfig().getSetting(DIALOG_EDITEXTERNALPROCESS_STORE_POSITION).booleanValue()) {
      DrJava.getConfig().setSetting(DIALOG_EDITEXTERNALPROCESS_STATE, "default");
    }
  }
  
  /** Edit saved processes. */
  private final Action _editExternalProcessesAction = new AbstractAction("Edit...") {
    public void actionPerformed(ActionEvent ae) {
      _editExternalDialog.setVisible(true);
    }
  };
  
//  edu.rice.cs.util.Log MODAL_LOG = new edu.rice.cs.util.Log("modal.txt",true);
  /** Return the modal window adapter if available, otherwise returns a non-modal dummy listener.
    * @param w window trying to get the modal window adapter
    * @return window adapter */
  public synchronized void installModalWindowAdapter(Window w) {
//    MODAL_LOG.log("installModalWindowAdapter, window = "+System.identityHashCode(w));
    if (_modalWindowAdapters.containsKey(w)) {
      // already installed
//      MODAL_LOG.log("\talready installed");
      return;
    } 
    
    if (_modalWindowAdapterOwner==null) {
//      MODAL_LOG.log("\tadapter available");
      // modal adapter is available, claim it
      _modalWindowAdapterOwner = w;
      WindowAdapter wa = new WindowAdapter() {
        public void windowDeactivated(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
        }
        public void windowIconified(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
        }
        public void windowLostFocus(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
        }
      };
      _modalWindowAdapters.put(w, wa);
      // install it
      w.addWindowListener(wa);
      w.addWindowFocusListener(wa);
    }
    // else nothing to do; modal adapter is already owned by another window
  }

  /** Return the modal window listener if available, otherwise returns a non-modal dummy listener.
    * @param w window trying to get the modal window listener
    * @param toFrontAction action to be performed after the window has been moved to the front again
    * @param closeAction action to be performed when the window is closing
    * @return window listener */
  public synchronized void installModalWindowAdapter(final Window w,
                                                     final Lambda<Void,WindowEvent> toFrontAction,
                                                     final Lambda<Void,WindowEvent> closeAction) {
//    MODAL_LOG.log("installModalWindowAdapter, window = "+System.identityHashCode(w));
    if (_modalWindowAdapters.containsKey(w)) {
      // already installed
//      MODAL_LOG.log("\talready installed");
      return;
    }
    
    WindowAdapter wa;
    if (_modalWindowAdapterOwner==null) {
//      MODAL_LOG.log("\tadapter available");
      // modal listener is available, claim it
      _modalWindowAdapterOwner = w;
      // create a window adapter performs the specified actions after delegating
      // to the modal window adapter
      wa = new WindowAdapter() {
        public void windowDeactivated(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
          toFrontAction.apply(we);
        }
        public void windowIconified(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
          toFrontAction.apply(we);
        }
        public void windowLostFocus(WindowEvent we) {
          we.getWindow().toFront();
          we.getWindow().requestFocus();
          toFrontAction.apply(we);
        }
        public void windowClosing(WindowEvent we) {
          closeAction.apply(we);
        }
      };
    }
    else {
      // modal listener is already owned by another window
      // create a window adapter that just performs the specified actions
      wa = new WindowAdapter() {
        public void windowDeactivated(WindowEvent we) { toFrontAction.apply(we); }
        public void windowIconified(WindowEvent we) { toFrontAction.apply(we); }
        public void windowLostFocus(WindowEvent we) { toFrontAction.apply(we); }
        public void windowClosing(WindowEvent we) { closeAction.apply(we); }
      };
    }
    // install it
    _modalWindowAdapters.put(w, wa);
    w.addWindowListener(wa);
    w.addWindowFocusListener(wa);
  }
  
  /** Removethe modal window adapter.
    * @param w window releasing the modal window adapter */
  public synchronized void removeModalWindowAdapter(Window w) {
//    MODAL_LOG.log("removeModalWindowListener, window = "+System.identityHashCode(w));
    if (!_modalWindowAdapters.containsKey(w)) {
      // the specified window does not have a modal windowadapter
//      MODAL_LOG.log("\tno modal window adapter installed for this window");
      return;
    }
    w.removeWindowListener(_modalWindowAdapters.get(w));
    w.removeWindowFocusListener(_modalWindowAdapters.get(w));
    _modalWindowAdapterOwner = null;
    _modalWindowAdapters.remove(w);
  }
}
