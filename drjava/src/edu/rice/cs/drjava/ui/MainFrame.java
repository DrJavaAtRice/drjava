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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.beans.*;
import java.lang.reflect.InvocationTargetException;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.platform.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.DocumentUIListener;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.ui.config.ConfigFrame;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.util.swing.DelegatingAction;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.ConfirmCheckBoxDialog;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.BorderlessSplitPane;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.docnavigation.*;
import edu.rice.cs.drjava.project.*;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.*;

/**
 * DrJava's main window.
 */
public class MainFrame extends JFrame implements OptionConstants {

  private static final int INTERACTIONS_TAB = 0;
  //private static final int COMPILE_TAB = 1;
  //private static final int OUTPUT_TAB = 2;
  //private static final int JUNIT_TAB = 3;
  //private static final int JAVADOC_TAB = 4;
  
  // GUI Dimensions
//  private static final int GUI_WIDTH = 800;
//  private static final int GUI_HEIGHT = 700;
//  private static final int DOC_LIST_WIDTH = 150;

  private static final String ICON_PATH = "/edu/rice/cs/drjava/ui/icons/";
  private static final String DEBUGGER_OUT_OF_SYNC =
    " Current document is out of sync with the debugger and should be recompiled!";

  /**
   * Number of milliseconds to wait before displaying "Stepping..." message
   * after a step is requested in the debugger.
   */
  private static final int DEBUG_STEP_TIMER_VALUE = 2000;

  /**
   * The model which controls all logic in DrJava.
   */
  private final SingleDisplayModel _model;

  /**
   * Maps an OpenDefDoc to its JScrollPane.
   * TODO: should this be synchronized?
   */
  private Hashtable<OpenDefinitionsDocument, JScrollPane> _defScrollPanes;

  /**
   * The currently displayed DefinitionsPane.
   */
  private DefinitionsPane _currentDefPane;

  /**
   * The filename currently being displayed.
   */
  private String _fileTitle = "";


  // These fields should be final but can't be, as the code is currently
  // organized, because they are set in helper methods, not the constructor

  // Tabbed panel fields
  private JTabbedPane _tabbedPane;
  private CompilerErrorPanel _compilerErrorPanel;
  private InteractionsPane _consolePane;
  private JScrollPane _consoleScroll;
  private ConsoleController _consoleController;  // move to controller
  private InteractionsPane _interactionsPane;
  private JPanel _interactionsContainer;
  private InteractionsController _interactionsController;  // move to controller
  private InteractionsScriptController _interactionsScriptController;
  private InteractionsScriptPane _interactionsScriptPane;

  private DebugPanel _debugPanel;
  private JUnitPanel _junitErrorPanel;
  private JavadocErrorPanel _javadocErrorPanel;
  private FindReplaceDialog _findReplace;
  private LinkedList<TabbedPanel> _tabs;

  private Component _lastFocusOwner;

  /**
   * Panel to hold both InteractionsPane and its sync message.
   * TO DO: move sync message into the pane itself.
   */
//  private JPanel _interactionsWithSyncPanel;

  /** Label to display message if Interactions are out of sync with Definitions. */
//  private JLabel _syncStatus;

  // Status bar fields
  private JPanel _statusBar;
  private JLabel _fileNameField;
  private JLabel _sbMessage;
  private JLabel _currLocationField;
  private PositionListener _posListener;

  // Split panes for layout
  private JSplitPane _docSplitPane;
  private JSplitPane _debugSplitPane;
  private JSplitPane _mainSplit;

 // private Container _docCollectionWidget;
  private JButton _compileButton;
  private JButton _closeButton;
  private JButton _undoButton;
  private JButton _redoButton;
  private JToolBar _toolBar;
  private JFileChooser _interactionsHistoryChooser;

  // Menu fields
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _toolsMenu;
  private JMenu _projectMenu;
  private JMenu _debugMenu;
  private JMenu _languageLevelMenu;
  private JMenu _helpMenu;
  private JMenuItem _debuggerEnabledMenuItem;
//  private JMenuItem _runDebuggerMenuItem;
//  private JMenuItem _resumeDebugMenuItem;
//  private JMenuItem _stepIntoDebugMenuItem;
//  private JMenuItem _stepOverDebugMenuItem;
//  private JMenuItem _stepOutDebugMenuItem;
//  private JMenuItem _suspendDebugMenuItem;
//  private JMenuItem _toggleBreakpointMenuItem;
//  private JMenuItem _printBreakpointsMenuItem;
//  private JMenuItem _clearAllBreakpointsMenuItem;

  // Popup menus
  private JPopupMenu _navPanePopupMenu;
  private JPopupMenu _navPanePopupMenuForExternal;
  private JPopupMenu _navPanePopupMenuForAuxiliary;
  private JPopupMenu _navPaneFolderPopupMenu;
  private JPopupMenu _interactionsPanePopupMenu;
  private JPopupMenu _consolePanePopupMenu;

  // Cached frames and dialogs
  private ConfigFrame _configFrame;
  private HelpFrame _helpFrame;
  private AboutDialog _aboutDialog;
  private ProjectPropertiesFrame _projectPropertiesFrame;

  /**
   * Keeps track of the recent files list in the File menu.
   */
  private RecentFileManager _recentFileManager;
  
  /**
   * Keeps track of the recent projects list in the Project menu
   */
  private RecentFileManager _recentProjectManager;

  private File _currentProjFile;
  
  /**
   * Timer to display "Stepping..." message if a step takes longer than
   * a certain amount of time.  All accesses must be synchronized on it.
   */
  private final Timer _debugStepTimer;

  /**
   * The current highlight displaying the location of the debugger's thread,
   * if there is one.  If there is none, this is null.
   */
  private HighlightManager.HighlightInfo _currentThreadLocationHighlight = null;

  /**
   * Table to map breakpoints to their corresponding highlight objects.
   */
  private java.util.Hashtable<Breakpoint, HighlightManager.HighlightInfo> _breakpointHighlights;

  /**
   * Whether to display a prompt message before quitting.
   */
  private boolean _promptBeforeQuit;

  /**
   * For opening files.
   * We have a persistent dialog to keep track of the last directory
   * from which we opened.
   */
  private JFileChooser _openChooser;

  /**
   * For opening project files.
   */
  private JFileChooser _openProjectChooser;
  
  /**
   * For saving files.
   * We have a persistent dialog to keep track of the last directory
   * from which we saved.
   */
  private JFileChooser _saveChooser;


  /**
   * filter for regular java files (.java and .j)
   */
  private javax.swing.filechooser.FileFilter _javaSourceFilter = new JavaSourceFilter();
  
  /**
   * filter for drjava project files (.pjt)
   */
  private javax.swing.filechooser.FileFilter _projectFilter = new javax.swing.filechooser.FileFilter(){
    public boolean accept(File f) {
      if( f.isDirectory() || f.getPath().endsWith(PROJECT_FILE_EXTENSION) ) {
        return true;
      }
      else {
        return false;
      }
    }
    
    public String getDescription() {
      return "DrJava Project Files (*.pjt)";
    }
  };
  
  
  /**
   * Returns the files to open to the model (command pattern).
   */
  private FileOpenSelector _openSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      //_openChooser.removeChoosableFileFilter(_projectFilter);
      _openChooser.resetChoosableFileFilters();
      
      _openChooser.setFileFilter(_javaSourceFilter);
      return getOpenFiles(_openChooser);
    }
  };
  
  /**
   * Returns the files to open to the model (command pattern).
   */
  private FileOpenSelector _openFileOrProjectSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      //_openChooser.removeChoosableFileFilter(_projectFilter);
      _openChooser.resetChoosableFileFilters();
      
      _openChooser.addChoosableFileFilter(_projectFilter);
      _openChooser.setFileFilter(_javaSourceFilter);
      return getOpenFiles(_openChooser);
    }
  };
  
  /**
   * Returns the project file to open.
   */
  private FileOpenSelector _openProjectSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      File[] retFiles = getOpenFiles(_openProjectChooser);
      return retFiles;
    }
  };

  /**
   * Returns the file to save to the model (command pattern).
   */
  private FileSaveSelector _saveSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException {
      return getSaveFile(_saveChooser);
    }
    public void warnFileOpen() {
      _warnFileOpen();
    }
    public boolean verifyOverwrite() {
      return _verifyOverwrite();
    }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                            File oldFile) {
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


  /**
   * Provides the view's contribution to the Javadoc interaction.
   */
  private JavadocDialog _javadocSelector = new JavadocDialog(this);
  
  /**
   * Provides a dialog to open a directory
   */
  private FolderDialog _folderSelector = new FolderDialog(this);

  private Action _moveToAuxiliaryAction = new AbstractAction("Move To Auxiliary"){
    public void actionPerformed(ActionEvent ae){
      _moveToAuxiliary();
    }
  };
  
  private Action _removeAuxiliaryAction = new AbstractAction("Move To External"){
    public void actionPerformed(ActionEvent ae){
      _removeAuxiliary();
    }
  };
  
  /** Resets the document in the definitions pane to a blank one. */
  private Action _newAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
//      System.out.println("------------------new----------------------");
      _new();
    }
  };
  
  private Action _newProjectAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
      _newProject();
    }
  };

  private Action _runProjectAction = new AbstractAction("Run Main Document") {
    public void actionPerformed(ActionEvent ae) {
      _runProject();
    }
  };

  /**
   * Sets the document in the definitions pane to a new templated junit test class.
   */
  private Action _newJUnitTestAction = new AbstractAction("New JUnit Test Case...") {
    public void actionPerformed(ActionEvent ae) {
      String testName = JOptionPane.showInputDialog(MainFrame.this,
                                                    "Please enter a name for the test class:",
                                                    "New JUnit Test Case",
                                                    JOptionPane.QUESTION_MESSAGE);
      if (testName != null) {
        String ext;
        for(int i=0; i < DrJava.LANGUAGE_LEVEL_EXTENSIONS.length; i++) {
          ext = DrJava.LANGUAGE_LEVEL_EXTENSIONS[i];
          if (testName.endsWith(ext)) {
            testName = testName.substring(0, testName.length() - ext.length());
          }
        }
        // For now, don't include setUp and tearDown
        _model.newTestCase(testName, false, false);
      }
    }
  };

  /**
   * Asks user for file name and and reads that file into
   * the definitions pane.
   */
  private Action _openAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) {
      _open();
    }
  };
  
  /**
   * Asks user for directory name and and reads it's files (and subdirectories files, on request) to
   * the definitions pane.
   */
  private Action _openFolderAction  = new AbstractAction("Open Folder...") {
    public void actionPerformed(ActionEvent ae) {
      openFolder(_folderSelector);
    }
  };
  
  /**
   * Asks user for file name and and reads that file into
   * the definitions pane.
   */
  private Action _openFileOrProjectAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) {
      _openFileOrProject();
    }
  };
  
  /**
   * Asks user for project file name and and reads the associated files into
   * the file navigator (and places the first source file in the editor pane)
   */
  private Action _openProjectAction = new AbstractAction("Open") {
    public void actionPerformed(ActionEvent ae) {
      _openProject();
    }
  };
  
  private Action _closeProjectAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) {
      _closeProject();
    }
  };
  
 
  /**
   * Closes the current active document, prompting to save if necessary.
   */
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) {
      _close();
    }
  };

  /**
   * Closes all open documents, prompting to save if necessary.
   */
  private Action _closeAllAction = new AbstractAction("Close All") {
    public void actionPerformed(ActionEvent ae) {
      _closeAll();
    }
  };

  /**
   * Closes all open documents, prompting to save if necessary.
   */
  private Action _closeFolderAction = new AbstractAction("Close Folder") {
    public void actionPerformed(ActionEvent ae) {
      _closeFolder();
    }
  };
  
  private Action _junitFolderAction = new AbstractAction("Test Folder") {
    public void actionPerformed(ActionEvent ae){
      _junitFolder();
    }
  };


  /** Saves the current document. */
  private Action _saveAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _save();
    }
    public void setEnabled(boolean e){
      super.setEnabled(e);
    }
  };

  /**
   * Asks the user for a file name and saves the document
   * currently in the definitions pane to that file.
   */
  private Action _saveAsAction = new AbstractAction("Save As...") {
    public void actionPerformed(ActionEvent ae) {
      _saveAs();
    }
  };
  
  
  private Action _saveProjectAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _saveProject();
    }
  };
  
  private Action _saveProjectAsAction = new AbstractAction("Save As...") {
    public void actionPerformed(ActionEvent ae) {
      _saveProjectAs();
    }
  };

  /** Reverts the current document. */
  private Action _revertAction = new AbstractAction("Revert to Saved") {
    public void actionPerformed(ActionEvent ae) {
      String title = "Revert to Saved?";

      String message = "Are you sure you want to revert the current " +
        "file to the version on disk?";

      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
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

  /**
   * Saves all documents, prompting for file names as necessary
   */
  private Action _saveAllAction = new AbstractAction("Save All") {
    public void actionPerformed(ActionEvent ae) {
      _saveAll();
    }
  };

  /** Prints the current document. */
  private Action _printAction = new AbstractAction("Print...") {
    public void actionPerformed(ActionEvent ae) {
      _print();
    }
  };

  /** Opens the print preview window */
  private Action _printPreviewAction = new AbstractAction("Print Preview...") {
    public void actionPerformed(ActionEvent ae) {
      _printPreview();
    }
  };

  /** Opens the page setup window. */
  private Action _pageSetupAction = new AbstractAction("Page Setup...") {
    public void actionPerformed(ActionEvent ae) {
      _pageSetup();
    }
  };

  /** Compiles all the project. */
  private Action _compileProjectAction = new AbstractAction("Compile Project") {
    public void actionPerformed(ActionEvent ae) {
      // right now, it's the same as compile all
      _compileAll();
    }
  };

  /** Compiles all documents in the navigators active group. */
  private Action _compileFolderAction = new AbstractAction("Compile Folder") {
    public void actionPerformed(ActionEvent ae) {
      // right now, it's the same as compile all
      _compileFolder();
    }
  };
  
  

  /** Compiles all open documents. */
  private Action _compileAllAction = new AbstractAction("Compile All Documents") {
    public void actionPerformed(ActionEvent ae) {
      _compileAll();
    }
  };

  /** Compiles the document in the definitions pane. */
  private Action _compileAction = new AbstractAction("Compile Current Document") {
    public void actionPerformed(ActionEvent ae) {
      _compile();
    }
  };

  /** Finds and runs the main method of the current document, if it exists. */
  private Action _runAction = new AbstractAction("Run Document's Main Method") {
    public void actionPerformed(ActionEvent ae) {
      _runMain();
    }
  };

  /** Runs JUnit on the document in the definitions pane. */
  private Action _junitAction = new AbstractAction("Test Current Document") {
    public void actionPerformed(ActionEvent ae) {
      _junit();
      //_setDividerLocation();  is this necessary?
    }
  };

  /**
   * Runs JUnit over all open JUnit tests.
   */
  private Action _junitAllAction = new AbstractAction("Test All Documents") {
    public void actionPerformed(ActionEvent e) {
      new Thread("Running JUnit Tests") {
        public void run() {
          _model.getJUnitModel().junitAll();
        }
      }.start();
    }
  };

  /**
   * Runs JUnit over all open JUnit tests in the project direcotry.
   */
  private Action _junitProjectAction = new AbstractAction("Test Project") {
    public void actionPerformed(ActionEvent e) {
      new Thread("Running JUnit Tests") {
        public void run() {
          _model.getJUnitModel().junitProject();
        }
      }.start();
    }
  };

  /** Runs Javadoc on all open documents (and the files in their packages). */
  private Action _javadocAllAction = new AbstractAction("Javadoc All Documents") {
      public void actionPerformed(ActionEvent ae) {
        try {
          JavadocModel jm = _model.getJavadocModel();
          File suggestedDir =
            jm.suggestJavadocDestination(_model.getActiveDocument());
          _javadocSelector.setSuggestedDir(suggestedDir);
          jm.javadocAll(_javadocSelector, _saveSelector, _model.getClasspath());
        }
        catch (IOException ioe) {
          _showIOError(ioe);
        }
      }
  };

  /** Runs Javadoc on the current document. */
  private Action _javadocCurrentAction = new AbstractAction("Preview Javadoc for Current Document") {
    public void actionPerformed(ActionEvent ae) {
      try {
        _model.getActiveDocument().generateJavadoc(_saveSelector);
      }
      catch (IOException ioe) {
        _showIOError(ioe);
      }
    }
  };

  /** Default cut action.  Returns focus to the correct pane. */
  Action cutAction = new DefaultEditorKit.CutAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = SwingUtilities.findFocusOwner(MainFrame.this);
      super.actionPerformed(e);
      if (c != null) c.requestFocus();
    }
  };

  /** Default copy action.  Returns focus to the correct pane. */
  Action copyAction = new DefaultEditorKit.CopyAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = SwingUtilities.findFocusOwner(MainFrame.this);
      super.actionPerformed(e);
      if (c != null) c.requestFocus();
    }
  };

  /** Default paste action.  Returns focus to the correct pane. */
  Action pasteAction = new DefaultEditorKit.PasteAction() {
    public void actionPerformed(ActionEvent e) {
      Component c = SwingUtilities.findFocusOwner(MainFrame.this);
//      Component c = KeyboardFocusManager.getFocusOwner();
      if (_currentDefPane.hasFocus()) {
        _currentDefPane.endCompoundEdit();
        CompoundUndoManager undoMan = _model.getActiveDocument().getUndoManager();
        int key = undoMan.startCompoundEdit();
        super.actionPerformed(e);
        undoMan.endCompoundEdit(key);
      }
      else {
        super.actionPerformed(e);
      }
      if (c != null) {
        c.requestFocus();
      }
    }
  };

  /**
   * Action that copies whatever is currently in the interactions pane
   * at the prompt to the definitions pane.
   */
  private Action _copyInteractionToDefinitionsAction =
    new AbstractAction("Lift Current Interaction to Definitions")
  {
    public void actionPerformed(ActionEvent e) {
      String text = _interactionsController.getDocument().getCurrentInput();
      if (!text.equals("")) {
        _putTextIntoDefinitions(text + "\n");
      }
    }
  };

  /**
   * Action that copies the previous interaction to the definitions pane.
   *
   * is there a good way to get the last history element without perturbing the current document?
  Action copyPreviousInteractionToDefinitionsAction = new AbstractAction("Copy previous interaction to definitions") {
    public void actionPerformed(ActionEvent e) {
      _putTextIntoDefinitions(_interactionsController.getDocument().getCurrentInput() + "\n");
    }
  };*/

  /**
   * Puts the given text into the current definitions pane at the current caret position.
   * TODO: Move this near to the other methods?
   */
  private void _putTextIntoDefinitions(String text) {
    int caretPos = _currentDefPane.getCaretPosition();
    try {
      _model.getActiveDocument().insertString(caretPos, text, null);
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /** Undoes the last change to the active definitions document. */
  private DelegatingAction _undoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      _currentDefPane.endCompoundEdit();
      super.actionPerformed(e);
      _currentDefPane.requestFocus();
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      _saveAction.setEnabled(doc.isModifiedSinceSave() || doc.isUntitled());
    }
  };

  /** Redoes the last undo to the active definitions document. */
  private DelegatingAction _redoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      _currentDefPane.requestFocus();
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      _saveAction.setEnabled(doc.isModifiedSinceSave() || doc.isUntitled());
    }
  };

  /** Quits DrJava.  Optionally displays a prompt before quitting. */
  private Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) {
      _quit();
    }
  };

  /** Selects all text in window. */
  private Action _selectAllAction = new AbstractAction("Select All") {
    public void actionPerformed(ActionEvent ae) {
      _selectAll();
    }
  };

  /** Shows the find/replace tab. */
  private Action _findReplaceAction = new AbstractAction("Find/Replace...") {
    public void actionPerformed(ActionEvent ae) {
      if(!_findReplace.isDisplayed()) {
        showTab(_findReplace);
        _findReplace.beginListeningTo(_currentDefPane);
      }
      _findReplace.setVisible(true);
      _tabbedPane.setSelectedComponent(_findReplace);
      try { Thread.sleep(100); } catch(Exception e) { e.printStackTrace(); }
      _findReplace.requestFocus();
      //_setDividerLocation();
    }
  };

  /** Find the next instance of the find word. */
  private Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent ae) {
      if(!_findReplace.isDisplayed()) {
        showTab(_findReplace);
        _findReplace.beginListeningTo(_currentDefPane);
      }
      _findReplace.findNext();
      // This is a fix for a highlighting bug when
      // calling this from _findReplace. Perhaps there
      // is a better solution.
      if (_lastFocusOwner == _findReplace) {
        _currentDefPane.requestFocus();
      }
      else {
        _lastFocusOwner.requestFocus();
      }
    }
  };

  /** Asks the user for a line number and goes there. */
  private Action _gotoLineAction = new AbstractAction("Go to Line...") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
      _currentDefPane.requestFocus();
    }
  };

  /** Indents the current selection. */
  private Action _indentLinesAction = new AbstractAction("Indent Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      _currentDefPane.endCompoundEdit();
      _currentDefPane.indent();
    }
  };

  /**
   * Action for commenting out a block of text using wing comments.
   */
  private Action _commentLinesAction = new AbstractAction("Comment Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      // Delegate everything to the DefinitionsDocument.
      OpenDefinitionsDocument openDoc = _model.getActiveDocument();
      int caretPos = _currentDefPane.getCaretPosition();
      openDoc.setCurrentLocation(caretPos);
      int start = _currentDefPane.getSelectionStart();
      int end = _currentDefPane.getSelectionEnd();
      _currentDefPane.endCompoundEdit();
      openDoc.commentLines(start, end);
    }
  };

  /**
   * Action for un-commenting a block of commented text.
   */
  private Action _uncommentLinesAction = new AbstractAction("Uncomment Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      // Delegate everything to the DefinitionsDocument.
      OpenDefinitionsDocument openDoc = _model.getActiveDocument();
      int caretPos = _currentDefPane.getCaretPosition();
      openDoc.setCurrentLocation(caretPos);
      int start = _currentDefPane.getSelectionStart();
      int end = _currentDefPane.getSelectionEnd();
      _currentDefPane.endCompoundEdit();
      openDoc.uncommentLines(start, end);
    }
  };


  /** Clears DrJava's output console. */
  private Action _clearConsoleAction = new AbstractAction("Clear Console") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
    }
  };

  /**
   * Shows the DebugConsole.
   */
  private Action _showDebugConsoleAction = new AbstractAction("Show DrJava Debug Console") {
    public void actionPerformed(ActionEvent e) {
      DrJava.showDrJavaDebugConsole(MainFrame.this);
    }
  };

  /** Resets the Interactions pane. */
  private Action _resetInteractionsAction = new AbstractAction("Reset Interactions") {
    public void actionPerformed(ActionEvent ae) {
      if (!DrJava.getConfig().getSetting(INTERACTIONS_RESET_PROMPT).booleanValue()) {
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

    // Lots of work, so use another thread
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        _model.resetInteractions();
        return null;
      }
    };
    worker.start();
  }

  /**
   * Displays the interactions classpath.
   */
  private Action _viewInteractionsClasspathAction = new AbstractAction("View Interactions Classpath") {
    public void actionPerformed(ActionEvent e) {
//      String classpath = "";
      StringBuffer cpBuf = new StringBuffer();
      Vector<String> classpathElements = _model.getClasspath();
      for(int i = 0; i < classpathElements.size(); i++) {
//        classpath += classpathElements.get(i);
        cpBuf.append(classpathElements.get(i));
        if (i + 1 < classpathElements.size()) {
//          classpath += "\n";
          cpBuf.append("\n");
        }
      }
      String classpath = cpBuf.toString();

      new DrJavaScrollableDialog(MainFrame.this, "Interactions Classpath",
                                 "Current Interpreter Classpath", classpath).show();
    }
  };

  /** Shows the user documentation. */
  private Action _helpAction = new AbstractAction("Help") {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
      if (_helpFrame == null) {
        _helpFrame = new HelpFrame();
      }
      _helpFrame.setVisible(true);
    }
  };

  /** Pops up an info dialog. */
  private Action _aboutAction = new AbstractAction("About") {
    public void actionPerformed(ActionEvent ae) {
      // Create dialog if we haven't yet
      if(_aboutDialog == null) {
        _aboutDialog = new AboutDialog(MainFrame.this);
      }
      _aboutDialog.show();
    }
  };

  /** Switches to next document. */
  private Action _switchToNextAction = new AbstractAction("Next Document") {
    public void actionPerformed(ActionEvent ae) {
      _model.setActiveNextDocument();
    }
  };

  /** Switches to previous document. */
  private Action _switchToPrevAction = new AbstractAction("Previous Document") {
    public void actionPerformed(ActionEvent ae) {
      _model.setActivePreviousDocument();
    }
  };

  /** Switches focus to next pane. */
  private Action _switchToNextPaneAction =  new AbstractAction("Next Pane") {
    public void actionPerformed(ActionEvent ae) {
      _switchPaneFocus(true);
    }
  };

  /** Switches focus to previous pane. */
  private Action _switchToPreviousPaneAction =  new AbstractAction("Previous Pane") {
    public void actionPerformed(ActionEvent ae) {
      _switchPaneFocus(false);
    }
  };

  /**
   * This takes a component and gives it focus, showing it if
   * it's a tab. The interactionsPane and consolePane are wrapped
   * in scrollpanes, so we have to specifically check for those
   * and unwrap them.
   * @param c the pane to switch focus to
   */
  private void _switchToPane(Component c) {
    Component newC = c;
    if (c == _interactionsContainer) {
      newC = _interactionsPane;
    }
    if (c == _consoleScroll) {
      newC = _consolePane;
    }
    showTab(newC);
    // need this when defPane is switched to
    newC.requestFocus();
  }

  /**
   * This method allows the user to cycle through the definitions
   * pane and all of the open tabs.
   * @param next true if we want to go to the next pane, false if the previous
   */
  private synchronized void _switchPaneFocus(boolean next) {
    int numTabs = _tabbedPane.getTabCount();
    int selectedIndex = _tabbedPane.indexOfComponent(_lastFocusOwner);
    if (next) {
      // if the DefinitionsPane has focus or nothing has focus,
      // switch to the first pane
      if (_currentDefPane == _lastFocusOwner) {
        // switch to the first tab if there is one
        if (numTabs > 0) {
          _switchToPane(_tabbedPane.getComponentAt(0));
        }
      }
      else if (numTabs == selectedIndex + 1) {
        // we're at the last tab, switch to the current def pane
        _switchToPane(_currentDefPane);
      }
      else {
        // switch to the next tab pane
        _switchToPane(_tabbedPane.getComponentAt(selectedIndex + 1));
      }
    }
    else {
      // if the DefinitionsPane has focus or nothing has focus,
      // switch to the last pane
      if (_currentDefPane == _lastFocusOwner) {
        // switch to the last tab if there is one
        if (numTabs > 0) {
          _switchToPane(_tabbedPane.getComponentAt(numTabs - 1));
        }
      }
      else if (selectedIndex == 0) {
        // we're at the first tab, switch to the current def pane
        _switchToPane(_currentDefPane);
      }
      else {
        // switch to the previous tab pane
        _switchToPane(_tabbedPane.getComponentAt(selectedIndex - 1));
      }
    }
  }

  /** Calls the ConfigFrame to edit preferences */
  private Action _editPreferencesAction = new AbstractAction("Preferences...") {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
      if (_configFrame == null) {
        _configFrame = new ConfigFrame(MainFrame.this);
      }
      _configFrame.setVisible(true);
      _configFrame.toFront();
    }
  };
  
  private Action _projectPropertiesAction = new AbstractAction("Project Properties") {
    public void actionPerformed(ActionEvent ae) {
      //Create frame if we haven't yet
      if (_projectPropertiesFrame == null) {
        _projectPropertiesFrame = new ProjectPropertiesFrame(MainFrame.this);
      }
      _projectPropertiesFrame.setVisible(true);
      _projectPropertiesFrame.toFront();
    }
  };
  

  /** Enables the debugger */
  private Action _toggleDebuggerAction = new AbstractAction("Debug Mode") {
    public void actionPerformed(ActionEvent ae) {
      debuggerToggle();
    }
  };

  /** Resumes debugging */
  private Action _resumeDebugAction = new AbstractAction("Resume Debugger") {
    public void actionPerformed(ActionEvent ae) {
      try {
        debuggerResume();
      }
      catch (DebugException de) {
        _showDebugError(de);
      }
    }
  };

  /** Steps into the next method call */
  private Action _stepIntoDebugAction = new AbstractAction("Step Into") {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.STEP_INTO);
    }
  };

  /** Runs the next line, without stepping into methods */
  private Action _stepOverDebugAction = new AbstractAction("Step Over") {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.STEP_OVER);
    }
  };

  /** Steps out of the next method call */
  private Action _stepOutDebugAction = new AbstractAction("Step Out") {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.STEP_OUT);
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
  Action _toggleBreakpointAction =
    new AbstractAction("Toggle Breakpoint on Current Line")
  {
    public void actionPerformed(ActionEvent ae) {
      debuggerToggleBreakpoint();
    }
  };

  /** Clears all breakpoints */
  private Action _clearAllBreakpointsAction =
    new AbstractAction("Clear All Breakpoints")
  {
    public void actionPerformed(ActionEvent ae) {
      debuggerClearAllBreakpoints();
    }
  };

  /** Cuts from the caret to the end of the current line to the clipboard. */
  protected Action _cutLineAction = new AbstractAction("Cut Line") {
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
      else {
        cutAction.actionPerformed(ae);
      }
    }
  };

  /** Deletes text from the caret to the end of the current line. */
  protected Action _clearLineAction = new AbstractAction("Clear Line") {
    public void actionPerformed(ActionEvent ae) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
      _actionMap.get(DefaultEditorKit.deleteNextCharAction).actionPerformed(ae);
    }
  };

  /**
   * Moves the caret to the "intelligent" beginning of the line.
   * @see #_getBeginLinePos
   */
  private Action _beginLineAction = new AbstractAction("Begin Line") {
    public void actionPerformed(ActionEvent ae) {
      int beginLinePos = _getBeginLinePos();
      _currentDefPane.setCaretPosition(beginLinePos);
    }
  };

  /**
   * Selects to the "intelligent" beginning of the line.
   * @see #_getBeginLinePos
   */
  private Action _selectionBeginLineAction = new AbstractAction("Select to Beginning of Line") {
    public void actionPerformed(ActionEvent ae) {
      int beginLinePos = _getBeginLinePos();
      _currentDefPane.moveCaretPosition(beginLinePos);
    }
  };

  /**
   * Returns the "intelligent" beginning of line.  If the caret is to
   * the right of the first non-whitespace character, the position of the
   * first non-whitespace character is returned.  If the caret is at or
   * to the left of the first non-whitespace character, the beginning of
   * the line is returned.
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

  private FileOpenSelector _interactionsHistoryFileSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      return getOpenFiles(_interactionsHistoryChooser);
    }
  };

  /**
   * Interprets the commands in a file in the interactions window
   */
  private Action _executeHistoryAction = new AbstractAction("Execute Interactions History...") {
    public void actionPerformed(ActionEvent ae) {
      // Show interactions tab
      _tabbedPane.setSelectedIndex(INTERACTIONS_TAB);

      _interactionsHistoryChooser.setDialogTitle("Execute Interactions History");
      try {
        _model.loadHistory(_interactionsHistoryFileSelector);
      }
      catch (FileNotFoundException fnf) {
        _showFileNotFoundError(fnf);
      }
      catch (IOException ioe) {
        _showIOError(ioe);
      }
      _interactionsPane.requestFocus();
    }
  };

  /**
   * Closes the currently executing interactions script, if there is one.
   */
  private void _closeInteractionsScript() {
    if (_interactionsScriptController != null) {
      _interactionsContainer.remove(_interactionsScriptPane);
      _interactionsScriptController = null;
      _interactionsScriptPane = null;
      _tabbedPane.invalidate();
      _tabbedPane.repaint();
    }
  }

  /**
   * Action to load an interactions history as a replayable script.
   */
  private Action _loadHistoryScriptAction = new AbstractAction("Load Interactions History as Script...") {
    public void actionPerformed(ActionEvent e) {
      try {
        _interactionsHistoryChooser.setDialogTitle("Load Interactions History");
        InteractionsScriptModel ism = _model.loadHistoryAsScript(_interactionsHistoryFileSelector);
        _interactionsScriptController = new InteractionsScriptController(ism, new AbstractAction("Close") {
          public void actionPerformed(ActionEvent e) {
            _closeInteractionsScript();
            _interactionsPane.requestFocus();
          }
        }, _interactionsPane);
        _interactionsScriptPane = _interactionsScriptController.getPane();
        _interactionsContainer.add(_interactionsScriptPane, BorderLayout.EAST);
        _tabbedPane.invalidate();
        _tabbedPane.repaint();
      }
      catch (FileNotFoundException fnf) {
        _showFileNotFoundError(fnf);
      }
      catch (IOException ioe) {
        _showIOError(ioe);
      }
      catch (OperationCanceledException oce) {
      }
    }
  };

  /** Save the commands in the interactions window's history to a file */
  private Action _saveHistoryAction = new AbstractAction("Save Interactions History...") {
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
      if (resp == 2 || resp == JOptionPane.CLOSED_OPTION) {
        return;
      }
      String history = _model.getHistoryAsStringWithSemicolons();

      // Edit the history
      if (resp == 0) {
        history = (new HistorySaveDialog(MainFrame.this)).editHistory(history);
      }
      if (history == null) {
        return; // save cancelled
      }

      _interactionsHistoryChooser.setDialogTitle("Save Interactions History");
      FileSaveSelector selector = new FileSaveSelector() {
        public File getFile() throws OperationCanceledException {
          // Don't try to set the filename with getSaveFile;
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
          return getChosenFile(_interactionsHistoryChooser, rc);
        }
        public void warnFileOpen() {
          _warnFileOpen();
        }
        public boolean verifyOverwrite() {
          return _verifyOverwrite();
        }
        public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                                File oldFile) {
          return true;
        }
      };

      try {
        _model.saveHistory(selector, history);
      }
      catch (IOException ioe) {
        _showIOError(new IOException("An error occured writing the history to a file"));
      }

      _interactionsPane.requestFocus();
    }
  };

  /**
   * Clears the commands in the interaction history
   */
  private Action _clearHistoryAction = new AbstractAction("Clear Interactions History")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.clearHistory();
      _interactionsPane.requestFocus();
    }
  };

  /** How DrJava responds to window events. */
  private WindowListener _windowCloseListener = new WindowAdapter() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _quit();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {
      try {
        _model.getActiveDocument().revertIfModifiedOnDisk();
      }
      catch (FileMovedException fme) {
        _showFileMovedError(fme);
      }
      catch (IOException e) {
        _showIOError(e);
      }
    }
    public void windowIconified(WindowEvent ev) {
    }
    public void windowOpened(WindowEvent ev) {
      _currentDefPane.requestFocus();
    }
  };






  /* ----------------------- Constructor is here! --------------------------- */






  /** Creates the main window, and shows it. */
  public MainFrame() {
    // Cache the config object, since we use it a zillion times.
    final Configuration config = DrJava.getConfig();

    // Platform-specific UI setup.
    PlatformFactory.ONLY.beforeUISetup();

    // create position listener for line numbers in status bar
    _posListener = new PositionListener();
    _setUpStatusBar();

    // create our model
    _model = new DefaultSingleDisplayModel();

    // Ensure that DefinitionsPane uses the correct EditorKit!
    //   This has to be stored as a static field on DefinitionsPane because
    //   the JEditorPane constructor uses it before we get a chance to
    //   assign it to an instance field...
    DefinitionsPane.setEditorKit(_model.getEditorKit());

    if (_model.getDebugger().isAvailable()) {
      // add listener to debug manager
      _model.getDebugger().addListener(new UIDebugListener());
    }
    // Timer to display a message if a debugging step takes a long time
    _debugStepTimer = new Timer(DEBUG_STEP_TIMER_VALUE, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _model.printDebugMessage("Stepping...");
      }
    });
    _debugStepTimer.setRepeats(false);


    // Working directory is default place to start, else
    // use user.dir (bug #895998).
    File workDir = config.getSetting(WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = config.getSetting(LAST_DIRECTORY);
      if (workDir == FileOption.NULL_FILE || !workDir.exists()) {
        workDir = new File(System.getProperty("user.dir"));
      }
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    _openChooser = new JFileChooser();
    _openChooser.setCurrentDirectory(workDir);
    _openChooser.setFileFilter(_javaSourceFilter);
    _openChooser.setMultiSelectionEnabled(true);
    
    //Get most recently opened project for filechooser
    Vector<File> recentProjects = config.getSetting(RECENT_PROJECTS);
    
    _openProjectChooser = new JFileChooser();
    if(recentProjects.size()>0 && recentProjects.elementAt(0).getParentFile() != null)
      _openProjectChooser.setCurrentDirectory(recentProjects.elementAt(0).getParentFile());
    else
      _openProjectChooser.setCurrentDirectory(workDir);
    _openProjectChooser.setFileFilter(_projectFilter);
    _openProjectChooser.setMultiSelectionEnabled(false);
    _saveChooser = new JFileChooser();
    _saveChooser.setCurrentDirectory(workDir);
    _saveChooser.setFileFilter(_javaSourceFilter);
    _interactionsHistoryChooser = new JFileChooser();
    _interactionsHistoryChooser.setCurrentDirectory(workDir);
    _interactionsHistoryChooser.setFileFilter(new InteractionsHistoryFilter());
    _interactionsHistoryChooser.setMultiSelectionEnabled(true);


    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Set up listeners
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());

    _defScrollPanes = new Hashtable<OpenDefinitionsDocument, JScrollPane>();

    // Create tabs before DefPane
    _setUpTabs();

    // DefinitionsPane
    JScrollPane defScroll = _createDefScrollPane(_model.getActiveDocument());
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();
    _currentDefPane.notifyActive();
    
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

    // set up menu bar and actions
    _setUpActions();
    _setUpMenuBar();
    _setUpToolBar();
    //    _setUpDocumentSelector();
    _setUpContextMenus();

    // eventually add recent project manager
    _recentFileManager = new RecentFileManager(_fileMenu.getItemCount() - 2,
                                               _fileMenu,
                                               this,false);
    
    _recentProjectManager = new RecentFileManager(_projectMenu.getItemCount()-2,
                                                  _projectMenu,
                                                  this,true);

    // Set frame icon
    setIconImage(getIcon("drjava64.png").getImage());

    // Size and position
    int x = config.getSetting(WINDOW_X).intValue();
    int y = config.getSetting(WINDOW_Y).intValue();
    int width = config.getSetting(WINDOW_WIDTH).intValue();
    int height = config.getSetting(WINDOW_HEIGHT).intValue();

    // Bounds checking.
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    final int menubarHeight = 24;
    if (height > screenSize.height - menubarHeight) {
      // Too tall, so resize
      height = screenSize.height - menubarHeight;
    }
    if (width > screenSize.width) {
      // Too wide, so resize
      width = screenSize.width;
    }

    // I assume that we want to be contained on the default screen.
    // TODO: support spanning screens in multi-screen setups.
    Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getDefaultScreenDevice().getDefaultConfiguration().getBounds();

    if (x == Integer.MAX_VALUE) {
      // magic value for "not set" - center.
      x = (bounds.width - width + bounds.x) / 2;
    }
    if (y == Integer.MAX_VALUE) {
      // magic value for "not set" - center.
      y = (bounds.height - height + bounds.y) / 2;
    }

    if (x < bounds.x) {
      // Too far left, move to left edge.
      x = bounds.x;
    }
    if (y < bounds.y) {
      // Too far up, move to top edge.
      y = bounds.y;
    }
    if ((x + width) > (bounds.x + bounds.width)) {
      // Too far right, move to right edge.
      x = bounds.width - width + bounds.x;
    }
    if ((y + height) > (bounds.y + bounds.height)) {
      // Too far down, move to bottom edge.
      y = bounds.height - height + bounds.y;
    }

    // Set to the new correct size and location
    setBounds(x, y, width, height);

    _setUpPanes();
    updateFileTitle();

    _promptBeforeQuit = config.getSetting(QUIT_PROMPT).booleanValue();

    // Set the fonts
    _setMainFont();
    Font doclistFont = config.getSetting(FONT_DOCLIST);
    _model.getDocCollectionWidget().setFont(doclistFont);

    // Set the colors
    _updateNormalColor();
    _updateBackgroundColor();

    // Add OptionListeners for the colors.
    config.addOptionListener
      (DEFINITIONS_NORMAL_COLOR, new NormalColorOptionListener());
    config.addOptionListener
      (DEFINITIONS_BACKGROUND_COLOR, new BackgroundColorOptionListener());

    // Add option listeners for changes to config options
    //  NOTE: We should only add listeners to view-related (or view-dependent)
    //        config options here.  Model options should go in
    //        DefaultGlobalModel._registerOptionListeners().
    config.addOptionListener
      (FONT_MAIN, new MainFontOptionListener());
    config.addOptionListener
      (FONT_LINE_NUMBERS, new LineNumbersFontOptionListener());
    config.addOptionListener
      (FONT_DOCLIST, new DoclistFontOptionListener());
    config.addOptionListener
      (FONT_TOOLBAR, new ToolbarFontOptionListener());
    config.addOptionListener
      (TOOLBAR_ICONS_ENABLED, new ToolbarOptionListener());
    config.addOptionListener
      (TOOLBAR_TEXT_ENABLED, new ToolbarOptionListener());
    config.addOptionListener
      (WORKING_DIRECTORY, new WorkingDirOptionListener());
    config.addOptionListener
      (LINEENUM_ENABLED, new LineEnumOptionListener());
    config.addOptionListener
      (QUIT_PROMPT, new QuitPromptOptionListener());
    config.addOptionListener
      (RECENT_FILES_MAX_SIZE, new RecentFilesOptionListener());
    config.addOptionListener
      (JSR14_LOCATION, new OptionListener<File>() {
      public void optionChanged(OptionEvent<File> oe) {
        boolean bootClasspathHasv2 = DrJava.bootClasspathHasJSR14v20();
        boolean bootClasspathHasv24 = DrJava.bootClasspathHasJSR14v24();
        if (oe.value != FileOption.NULL_FILE) {
          boolean checkForV20 = DrJava.checkForJSR14v20();
          boolean checkForV24 = DrJava.checkForJSR14v24();
          if (checkForV24 && !bootClasspathHasv24) {
            JOptionPane.showMessageDialog(_configFrame,
                                          "You must restart DrJava to use the JSR-14 v2.4 compiler.",
                                          "JSR14 Warning", JOptionPane.WARNING_MESSAGE);
          }
          else if ((checkForV20 && !checkForV24) && (!bootClasspathHasv2 || bootClasspathHasv24)) {
            JOptionPane.showMessageDialog(_configFrame,
                                          "You must restart DrJava to use the JSR-14 v2.0/2.2 compiler.",
                                          "JSR14 Warning", JOptionPane.WARNING_MESSAGE);
          }
          else if (!checkForV20 && bootClasspathHasv2) {
            JOptionPane.showMessageDialog(_configFrame,
                                          "You must restart DrJava to switch to 1.x versions of the JSR-14 compiler.",
                                          "JSR14 Warning", JOptionPane.WARNING_MESSAGE);
          }
        }
      }
    });
    config.addOptionListener(LOOK_AND_FEEL, new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oe) {
//        try {
//          UIManager.setLookAndFeel(oe.value);
//          SwingUtilities.updateComponentTreeUI(MainFrame.this);
//          if (_debugPanel != null) {
//            SwingUtilities.updateComponentTreeUI(_debugPanel);
//          }
//          if (_configFrame != null) {
//            SwingUtilities.updateComponentTreeUI(_configFrame);
//          }
//          if (_helpFrame != null) {
//            SwingUtilities.updateComponentTreeUI(_helpFrame);
//          }
//          if (_aboutDialog != null) {
//            SwingUtilities.updateComponentTreeUI(_aboutDialog);
//          }
//          SwingUtilities.updateComponentTreeUI(_navPanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_interactionsPanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_consolePanePopupMenu);
//          SwingUtilities.updateComponentTreeUI(_openChooser);
//          SwingUtilities.updateComponentTreeUI(_saveChooser);
//          Iterator<TabbedPanel> it = _tabs.iterator();
//          while (it.hasNext()) {
//            SwingUtilities.updateComponentTreeUI(it.next());
//          }
//        }
//        catch (Exception ex) {
//          _showError(ex, "Could Not Set Look and Feel",
//                     "An error occurred while trying to set the look and feel.");
//        }

        String title = "Apply Look and Feel";
        String msg = "Look and feel changes will take effect when you restart DrJava.";
        if (config.getSetting(WARN_CHANGE_LAF).booleanValue()) {
          ConfirmCheckBoxDialog dialog =
            new ConfirmCheckBoxDialog(_configFrame, title, msg,
                                      "Do not show this message again",
                                      JOptionPane.INFORMATION_MESSAGE,
                                      JOptionPane.DEFAULT_OPTION);
          if (dialog.show() == JOptionPane.OK_OPTION && dialog.getCheckBoxValue()) {
            config.setSetting(WARN_CHANGE_LAF, Boolean.FALSE);
          }
        }
      }
    });

    config.addOptionListener
      (JVM_ARGS, new OptionListener<String>() {
      public void optionChanged(OptionEvent<String> oe) {
        if(oe.value != "") {
          int result = JOptionPane.showConfirmDialog(_configFrame,
                                                     "Specifying JVM Args is an advanced option. Invalid arguments may cause the\n"+
                                                     "Interactions Pane to stop working.\n"+
                                                     "Are you sure you want to set this option?\n"+
                                                     "(You will have to reset the interactions pane before changes take effect.)",
                                                     "Confirm JVM Arguments", JOptionPane.YES_NO_OPTION);
          if(result!=JOptionPane.YES_OPTION) {
            config.setSetting(oe.option, "");
          }
        }
      }
    });

    config.addOptionListener(ALLOW_PRIVATE_ACCESS, new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _model.getInteractionsModel().setPrivateAccessible(oce.value.booleanValue());
      }
    });

    // Initialize breakpoint highlights hashtable, for easy removal of highlights
    _breakpointHighlights = new java.util.Hashtable<Breakpoint, HighlightManager.HighlightInfo>();

    // Set cached frames and dialogs to null until they are created
    _configFrame = null;
    _helpFrame = null;
    _aboutDialog = null;
    _interactionsScriptController = null;
    _projectPropertiesFrame = null;

    // If any errors occurred while parsing config file, show them
    _showConfigException();

    KeyBindingManager.Singleton.setShouldCheckConflict(false);

    // Platform-specific UI setup.
    PlatformFactory.ONLY.afterUISetup(_aboutAction, _editPreferencesAction, _quitAction);
  }

  /**
   * Releases any resources this frame is using to prepare it to
   * be garbage collected.  Should only be called from tests.
   * This is implementation specific and may not be needed.
   */
  public void dispose() {
    // centgraf: I justify casting here because it is implementation-specific
    //           and doc'ed as such.
    ((DefaultSingleDisplayModel) _model).dispose();
    super.dispose();
  }

  /**
   * @return The model providing the logic for this view.
   */
  public SingleDisplayModel getModel() {
    return _model;
  }

  /**
   * Returns the frame's interactions pane.  (Package private accessor)
   */
  InteractionsPane getInteractionsPane() {
    return _interactionsPane;
  }

   /**
   * @return The frame's close button (Package private accessor)
   */
  JButton getCloseButton() {
    return _closeButton;
  }

  /**
   * For testing purposes.
   * @return The frame's compileAll button (Package private accessor)
   */
  JButton getCompileAllButton() {
    return _compileButton;
  }

  /**
   * Make the cursor an hourglass.
   */
  private int hourglassNestLevel = 0;
  public void hourglassOn() {
    hourglassNestLevel++;
    if(hourglassNestLevel == 1){
      getGlassPane().setVisible(true);
      _currentDefPane.setEditable(false);
      setAllowKeyEvents(false);
    }
  }
  
  /**
   * Return the cursor to normal.
   */
  public void hourglassOff() {
    hourglassNestLevel--;
    if(hourglassNestLevel == 0){
      getGlassPane().setVisible(false);
      _currentDefPane.setEditable(true);
      setAllowKeyEvents(true);
    }
  }

  private boolean allow_key_events = true;
  public void setAllowKeyEvents(boolean a){
    this.allow_key_events = a;
  }
  
  public boolean getAllowKeyEvents(){
    return this.allow_key_events;
  }

  /**
   * Toggles whether the debugger is enabled or disabled,
   * and updates the display accordingly.
   */
  public void debuggerToggle() {
    // Make sure the debugger is available
    Debugger debugger = _model.getDebugger();
    if (!debugger.isAvailable()) return;

    try {
      if (inDebugMode()) {
        // Turn off debugger
        debugger.shutdown();
      }
      else {
        // Turn on debugger
        debugger.startup();
        _updateDebugStatus();
      }
    }
    catch (DebugException de) {
      _showError(de, "Debugger Error",
                 "Could not start the debugger.");
    }
    catch (NoClassDefFoundError err) {
      _showError(err, "Debugger Error",
                 "Unable to find the JPDA package for the debugger.\n" +
                 "Please make sure either tools.jar or jpda.jar is\n" +
                 "in your classpath when you start DrJava.");
      _setDebugMenuItemsEnabled(false);
    }

  }

  /**
   * Display the debugger tab and update the Debug menu accordingly.
   */
  public void showDebugger() {
    _setDebugMenuItemsEnabled(true);
    _showDebuggerPanel();
  }

  /**
   * Hide the debugger tab and update the Debug menu accordingly.
   */
  public void hideDebugger() {
    _setDebugMenuItemsEnabled(false);
    _hideDebuggerPanel();
  }

  private void _showDebuggerPanel() {
    _debugSplitPane.setTopComponent(_docSplitPane);
    _mainSplit.setTopComponent(_debugSplitPane);
    _debugPanel.updateData();
    _lastFocusOwner.requestFocus();
  }

  private void _hideDebuggerPanel() {
    _mainSplit.setTopComponent(_docSplitPane);
    _lastFocusOwner.requestFocus();
  }


  /**
   * Updates the title bar with the name of the active document.
   */
  public void updateFileTitle() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    String filename = GlobalModelNaming.getDisplayFilename(doc);
    if (!filename.equals(_fileTitle)) {
      _fileTitle = filename;
      setTitle(filename + " - DrJava");
      _model.getDocCollectionWidget().repaint();
    }
    // Always update this field-- two files in different directories
    //  can have the same _fileTitle
    _fileNameField.setText(GlobalModelNaming.getDisplayFullPath(doc));
//    System.out.println("setting " + doc + " to display name: " + GlobalModelNaming.getDisplayFullPath(doc));
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   * @param jfc the open dialog from which to extract information
   * @return an array of the files that were chosen
   */
  public File[] getOpenFiles(JFileChooser jfc) throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    File selection = jfc.getSelectedFile();//_openChooser.getSelectedFile();
    if (selection != null) {
//      jfc.setSelectedFile(selection.getParentFile());
//      jfc.setSelectedFile(selection);
      jfc.setSelectedFile(null);
    }
    int rc = jfc.showOpenDialog(this);//_openChooser.showOpenDialog(this);
    return getChosenFiles(jfc, rc);//_openChooser, rc);
  }

  /**
   * Prompt the user to select a place to save the current document.
   */
  public File getSaveFile(JFileChooser jfc) throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    File selection = jfc.getSelectedFile();//_saveChooser.getSelectedFile();
    if (selection != null) {
      jfc.setSelectedFile(selection.getParentFile());
      jfc.setSelectedFile(selection);
      jfc.setSelectedFile(null);
    }

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

  /**
   * Returns the current DefinitionsPane.
   */
  public DefinitionsPane getCurrentDefPane() {
    return _currentDefPane;
  }

  /**
   * Returns the currently shown error panel if there is one.
   * Otherwise returns null.
   */
  public ErrorPanel getSelectedErrorPanel() {
    Component c = _tabbedPane.getSelectedComponent();
    if (c instanceof ErrorPanel) {
      return (ErrorPanel) c;
    }
    else {
      return null;
    }
  }

  /**
   * Returns whether the compiler output tab is currently showing.
   */
  public boolean isCompilerTabSelected() {
    return _tabbedPane.getSelectedComponent() == _compilerErrorPanel;
  }

  /**
   * Returns whether the test output tab is currently showing.
   */
  public boolean isTestTabSelected() {
    return _tabbedPane.getSelectedComponent() == _junitErrorPanel;
  }

  /**
   * Returns whether the JavaDoc output tab is currently showing.
   */
  public boolean isJavadocTabSelected() {
    return _tabbedPane.getSelectedComponent() == _javadocErrorPanel;
  }

  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  private void _installNewDocumentListener(final Document d) {
    d.addDocumentListener(new DocumentUIListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals("")) {
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        }
        updateFileTitle();
      }
      public void insertUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals("")) {
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        }
        updateFileTitle();
      }
      public void removeUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals("")) {
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        }
        updateFileTitle();
      }
    });
  }
  
  
  /**
   * Changes the message text toward the right of the status bar
   * @param msg The message to place in the status bar
   */
  public void setStatusMessage(String msg) {
    _sbMessage.setText(msg);
  }
  /**
   * Sets the message text in the status bar to the null string
   */
  public void clearStatusMessage() {
    _sbMessage.setText("");
  }
  /**
   * Sets the font of the status bar message
   * @param f The new font of the status bar message
   */
  public void setStatusMessageFont(Font f) {
    _sbMessage.setFont(f);
  }
  /**
   * Sets the color of the text in the status bar message
   * @param c The color of the text
   */
  public void setStatusMessageColor(Color c) {
    _sbMessage.setForeground(c);
  }

  private void _moveToAuxiliary(){
    INavigatorItem n = _model.getDocumentNavigator().getCurrentSelectedLeaf();
    if(n == null){
      // false alarm, a document is not really selected...
    }else{
      OpenDefinitionsDocument d = _model.getODDGivenIDoc(n);
      if(d.isUntitled()){
        // can't move an untitled document to the auxiliary files
      }else{
        _model.addAuxiliaryFile(d);
        try{
          _model.getDocumentNavigator().refreshDocument(n, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
        }catch(IOException e){
          // noop
        }
      }
    }
  }
  
  private void _removeAuxiliary(){
    INavigatorItem n = _model.getDocumentNavigator().getCurrentSelectedLeaf();
    if(n == null){
      // false alarm, a document is not really selected...
    }else{
      OpenDefinitionsDocument d = _model.getODDGivenIDoc(n);
      if(d.isUntitled()){
        // can't move an untitled document to the auxiliary files
      }else{
        _model.removeAuxiliaryFile(d);
        try{
          _model.getDocumentNavigator().refreshDocument(n, _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
        }catch(IOException e){
          // noop
        }
      }
    }
  }

  private void _new() {
    _model.newFile();
  }

  private void _open() {
    open(_openSelector);
  }
  
  private void _openFolder(){
    openFolder(_folderSelector);
  }
  
  private void _openFileOrProject() {
    try {
      final File[] fileList = _openFileOrProjectSelector.getFiles();
      
      FileOpenSelector fos = new FileOpenSelector() {
        public File[] getFiles(){
          return fileList;         
        }
      };
    
      if(_openChooser.getFileFilter().equals(_projectFilter)) 
        openProject(fos);
      else
        open(fos);
    }
    catch(OperationCanceledException oce) {
      
    }
  }
  
  /**
   * Sets the left navigator pane to the correct component
   * as dictated by the model.
   */
  private void _resetNavigatorPane() {
    _docSplitPane.remove(_docSplitPane.getLeftComponent());
    _docSplitPane.setLeftComponent(new JScrollPane(_model.getDocumentNavigator().asContainer()));
    Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
    _model.getDocCollectionWidget().setFont(doclistFont);
    _updateNormalColor();
    _updateBackgroundColor();
  }
  
  /**
   * Asks the user to select the project file to 
   * open and starts the process of opening the
   * project
   */
  private void _openProject() {
    openProject(_openProjectSelector);
  }
  
  public void openProject(FileOpenSelector projectSelector) {
    try {
      hourglassOn();
      final File[] file = projectSelector.getFiles();
      if( file.length < 1 ) {
        throw new IllegalStateException("Open project file selection not canceled but no project file was selected.");
      }
      if(!_model.isProjectActive() || _model.isProjectActive() && _closeProject()) {
        _openProjectHelper(file[0]);
      }
    }
    catch(OperationCanceledException oce) {
      // do nothing, we just won't open anything
    }
    catch(Exception e){
      e.printStackTrace(System.err);
    }
    finally {
      hourglassOff();
    }    
  }
  
  
  /**
   * Oversees the opening of the project by delegating to the model
   * to parse and initialize the project while resetting the 
   * navigator pane and opening up the files itself.
   * @param projectFile the file of the project to open
   */
  private void _openProjectHelper(File projectFile) {
    _currentProjFile = projectFile;
    DocFile[] srcFiles = null;
    try{
      srcFiles = _model.openProject(projectFile);
      _setUpContextMenus();
      _recentProjectManager.updateOpenFiles(projectFile);
    }
    catch(MalformedProjectFileException e){
      _showProjectFileParseError(e);
      return;
    }
    catch(FileNotFoundException e) {
      _showFileNotFoundError(e);
      return;
    }
    catch(IOException e){
      _showIOError(e);
      return;
    }

    List<OpenDefinitionsDocument> nonProjDocs = _model.getNonProjectDocuments();
    List<OpenDefinitionsDocument> projDocs = _model.getProjectDocuments();
    File[] projectFiles = _model.getProjectFiles();
    
    
    /**
     * keep all nonproject files open
     */
    IDocumentNavigator nav = _model.getDocumentNavigator();

    
    // close all project files
    List<OpenDefinitionsDocument> docsToClose = new LinkedList<OpenDefinitionsDocument>();
    for(OpenDefinitionsDocument d: projDocs){
      if(d.isProjectFile()){
        docsToClose.add(d);
      }else{
        try{
          nav.refreshDocument(_model.getIDocGivenODD(d), _model.fixPathForNavigator(d.getFile().getCanonicalPath()));
        }catch(IOException e){
          // noop
        }
      }
    }

    for(OpenDefinitionsDocument d: docsToClose){
    }
    _model.closeFiles(docsToClose);
    
    final DocFile[] files = srcFiles;
    // project could be empty
    if(srcFiles.length > 0){
      open(new FileOpenSelector(){
        public File[] getFiles() {
          return files;
        }
      });
    }
    
    _openProjectUpdate();
  }
  
  private void _openProjectUpdate() {
    if(_model.isProjectActive()) {
      _closeProjectAction.setEnabled(true);
//      _saveProjectAction.setEnabled(false);
      _projectPropertiesAction.setEnabled(true);
      _junitProjectAction.setEnabled(true);
      _compileProjectAction.setEnabled(true);
      _model.setProjectChanged(false);
      _resetNavigatorPane();
      _compileButton.setToolTipText("<html>Compile all documents in the project.<br>External files are excluded.</html>");
    }
  }
  
  /**
   * Signals the model to close the project, then
   * closes all open files.  It also restores the
   * list view navigator
   * @return true if the project is closed, false if cancelled
   */
  boolean _closeProject(){
    if(_checkProjectClose()) {
      List<OpenDefinitionsDocument> projDocs = _model.getProjectDocuments();
      //    for(OpenDefinitionsDocument d: projDocs){
      //      _model.closeFile(d);
      //    }
      _model.closeFiles(projDocs);
      _model.closeProject();
      Component renderer = _model.getDocumentNavigator().getRenderer();
      new ForegroundColorListener(renderer);
      new BackgroundColorListener(renderer);
      _resetNavigatorPane();
      if(_model.getDocumentCount() == 1)
        _model.setActiveFirstDocument();
      _closeProjectAction.setEnabled(false);
//      _saveProjectAction.setEnabled(false);
      _projectPropertiesAction.setEnabled(false);
      _junitProjectAction.setEnabled(false);
      _compileProjectAction.setEnabled(false);
      _setUpContextMenus();
      _currentProjFile = null;
      _compileButton.setToolTipText("Compile all open documents");
      return true;
    }else{
      return false;
    }
  }
  
  private boolean _checkProjectClose() {
   if(_model.isProjectChanged()) {
      String fname = _model.getProjectFile().getName();
      String text = fname + " has been modified. Would you like to save it?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             "Save " + fname + "?",
                                             JOptionPane.YES_NO_CANCEL_OPTION);

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
  
  
  /**
   * Closes all files and makes a new project  
   */
  private void _newProject(){
    _closeAll();
    _saveProjectAs();
  }
  
  public File getCurrentProject() {
    return _currentProjFile;
  }
  
  /**
   * Opens all the files returned by the FileOpenSelector prompting
   * the user to handle the cases where files are already open,
   * files are missing, or the action was canceled by the user
   * @param openSelector the selector that returns the files to open
   */
  void open(FileOpenSelector openSelector) {
    try {
      hourglassOn();
      _model.openFiles(openSelector);
    }
    catch (AlreadyOpenException aoe) {
      OpenDefinitionsDocument openDoc = aoe.getOpenDocument();
      String filename;
      try {
        filename = openDoc.getFile().getName();
      }
      catch (IllegalStateException ise) {
        // Can't happen: this open document must have a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        // File was deleted, but use the same name anyway
        filename = fme.getFile().getName();
      }
      
      // Always switch to doc
      _model.setActiveDocument(openDoc);

      // Prompt to revert if modified
      if (openDoc.isModifiedSinceSave()) {
        String title = "Revert to Saved?";
        String message = filename + " is already open and modified.\n" +
          "Would you like to revert to the version on disk?\n";
        int choice = JOptionPane.showConfirmDialog(this,
                                                   message,
                                                   title,
                                                   JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
          _revert();
        }
      }
      try {
        File f = openDoc.getFile();
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
      catch (IllegalStateException ise) {
        // Impossible: saved => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        File f = fme.getFile();
        // Recover, show it in the list anyway
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
    }
    catch (OperationCanceledException oce) {
      // Ok, don't open a file
    }
    catch (FileNotFoundException fnf) {
      _showFileNotFoundError(fnf);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
    finally {
      hourglassOff();
      //_openProjectUpdate();
    }
  }

  
  /**
   * Opens all the files in the directory returned by the FolderSelector prompting
   * the user to handle the cases where files are already open,
   * files are missing, or the action was canceled by the user
   * @param openSelector the selector that returns the files to open
   */
  public void openFolder(DirectorySelector openSelector) {
    try{
      File opendir = null;
      try{
        opendir = _model.getActiveDocument().getFile().getParentFile();
      }catch(FileMovedException e){
      }catch(IllegalStateException e){
      }
      
      
      File dir = openSelector.getDirectory(opendir);
      ArrayList<File> files;
      System.err.println("finding files");
      if(dir != null && dir.isDirectory()){
        files = FileOps.getFilesInDir(dir, openSelector.isRecursive(), new FileFilter(){
          public boolean accept(File f){ 
            return f.isDirectory() ||
              f.isFile() && 
              f.getName().endsWith(DrJava.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)]);
          }
        });
        
      System.err.println("sorting files");
        if(_model.isProjectActive()){
          Collections.sort(files, new Comparator<File>(){
            public int compare(File o1,File o2){
              return - o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
            public boolean equals(Object o){
              return false;
            }
          });
        }else{
          Collections.sort(files, new Comparator<File>(){
            public int compare(File o1,File o2){
              return - o1.getName().compareTo(o2.getName());
            }
            public boolean equals(Object o){
              return false;
            }
          });
        }
        
        final File[] sfiles = files.toArray(new File[0]);
        
        System.err.println("opening files");
        open(new FileOpenSelector(){
          public File[] getFiles() {
            return sfiles;
          }
        });
      }
    }catch(OperationCanceledException e){
      // noop
    }
  }

  /**
   * Delegates directly to the model to close 
   * the active document
   */
  private void _close() {
    LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    l.add(_model.getActiveDocument());
    _model.closeFiles(l);
    //_model.closeFile(_model.getActiveDocument());
  }
  
  private void _junitFolder(){
    INavigatorItem n;
    Enumeration<INavigatorItem> e = _model.getDocumentNavigator().getDocuments();
    final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    if(_model.getDocumentNavigator().isGroupSelected()){
      while (e.hasMoreElements()){
        n = e.nextElement();
        if(_model.getDocumentNavigator().isSelectedInGroup(n)){
          l.add(_model.getODDGivenIDoc(n));
        }
      }
      _model.getJUnitModel().junitDocs(l);
    }
  }
  
  private void _closeFolder(){
    INavigatorItem n;
    Enumeration<INavigatorItem> e = _model.getDocumentNavigator().getDocuments();
    final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    if(_model.getDocumentNavigator().isGroupSelected()){
      while (e.hasMoreElements()){
        n = e.nextElement();
        if(_model.getDocumentNavigator().isSelectedInGroup(n)){
          l.add(_model.getODDGivenIDoc(n));
        }
      }
      _model.closeFiles(l);
    }
  }

  private void _print() {
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

  /**
   * Opens a new PrintPreview frame.
   */
  private void _printPreview() {
    try {
      _model.getActiveDocument().preparePrintJob();
      new PreviewFrame(_model, this);
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

  private void _pageSetup() {
    PrinterJob job = PrinterJob.getPrinterJob();
    _model.setPageFormat(job.pageDialog(_model.getPageFormat()));
  }

  //Called by testCases
  void closeAll() {
    _closeAll();
  }
  
  private void _closeAll() {
    if(!_model.isProjectActive() || 
        _model.isProjectActive() && _closeProject())    
      _model.closeAllFiles();
   // _model.setActiveFirstDocument();
  }


  private boolean _save() {
    try {
      if (_model.getActiveDocument().saveFile(_saveSelector)) {
        _currentDefPane.hasWarnedAboutModified(false); 
        
        /**
         * this highlights the document in the navigator
         */
        _model.setActiveDocument(_model.getActiveDocument());
        return true;
      }
      else {
        return false;
      }
    }
    catch (IOException ioe) {
      _showIOError(ioe);
      return false;
    }
  }


  private boolean _saveAs() {
    try {
      //If file becomes project file it should enable the save project option in the menu
      boolean toReturn = _model.getActiveDocument().saveFileAs(_saveSelector);
      /**
       * this highlights the document in the navigator
       */
      _model.setActiveDocument(_model.getActiveDocument());
      return toReturn;
    }
    catch (IOException ioe) {
      _showIOError(ioe);
      return false;
    }
  }

  private void _saveAll() {
    try {
      if(_model.isProjectActive()){
        _saveProject();
      }
      _model.saveAllFiles(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }
  
  //Called by the ProjectPropertiesFrame
  void saveProject() {
    _saveProject();
  }
  
  
  private void _saveProject() {
    //File file = _model.getProjectFile();
    _saveProjectHelper(_currentProjFile);
  }
  
  
  private void _saveProjectAs() {
    
    
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    _saveChooser.removeChoosableFileFilter(_projectFilter);
    _saveChooser.removeChoosableFileFilter(_javaSourceFilter);
    _saveChooser.setFileFilter(_projectFilter);
    File selection = _saveChooser.getSelectedFile();
    if (selection != null) {
      _saveChooser.setSelectedFile(selection.getParentFile());
      _saveChooser.setSelectedFile(selection);
      _saveChooser.setSelectedFile(null);
    }
    
    if (_currentProjFile != null) 
      _saveChooser.setSelectedFile(_currentProjFile);
    
    int rc = _saveChooser.showSaveDialog(this);
    if (rc == JFileChooser.APPROVE_OPTION) {
      File file = _saveChooser.getSelectedFile();
      if(!file.exists() || _verifyOverwrite()) {
        _saveProjectHelper(file);
        try {
          if (file.getCanonicalPath().endsWith(".pjt")) {
            _openProjectHelper(file);
          }
          else {
            _openProjectHelper(new File(file.getCanonicalPath() + ".pjt"));
          }
        }
        catch (IOException e) {
          throw new UnexpectedException(e);
        }
      }
    }
  }
  
  void _saveProjectHelper(File file) {
    try {
      if (file.getName().indexOf(".") == -1){
        file =  new File (file.getAbsolutePath() + ".pjt");
      }
      String filename = file.getCanonicalPath();
      _model.saveProject(filename, _gatherDocInfo());
//      if(!(_model.getDocumentNavigator() instanceof JTreeSortNavigator)){
//        _openProjectHelper(file);
//      }    
    }
    catch(IOException ioe) {
      _showIOError(ioe);
    }
    _recentProjectManager.updateOpenFiles(file);
//    _saveProjectAction.setEnabled(false);
    _model.setProjectChanged(false);
  }
  
  private Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> _gatherDocInfo() {
    Hashtable<OpenDefinitionsDocument,DocumentInfoGetter> map =
      new Hashtable<OpenDefinitionsDocument,DocumentInfoGetter>();
    List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
    for(OpenDefinitionsDocument doc: docs) {
      map.put(doc, _makeInfoGetter(doc));
    }
    return map;
  }
  /**
   * Implementation may change if the scroll/selection information is later
   * stored in a place other than the definitions pane.  Hopefully this info
   * will eventually be backed up in the OpenDefinitionsDocument in which case
   * all this code should be refactored into the model's _saveProject method
   */
  private DocumentInfoGetter _makeInfoGetter(final OpenDefinitionsDocument doc) {
    final JScrollPane scroller = _defScrollPanes.get(doc);
    final DefinitionsPane pane = (DefinitionsPane)scroller.getViewport().getView();
    
    return new DocumentInfoGetter() {
      public Pair<Integer,Integer> getSelection() {
        int selStart = pane.getSelectionStart();
        int selEnd = pane.getSelectionEnd();
        if(pane.getCaretPosition() == selStart){
          return new Pair<Integer,Integer>(selEnd,selStart);
        }else{
          return new Pair<Integer,Integer>(selStart,selEnd);
        }
      }
      public Pair<Integer,Integer> getScroll() {
        int scrollv = scroller.getVerticalScrollBar().getValue();
        int scrollh = scroller.getHorizontalScrollBar().getValue();
        return new Pair<Integer,Integer>(scrollv,scrollh); 
      }
      public File getFile(){
        if (doc.isUntitled()) {
          return null;
        }
        else{
          try {
            return doc.getFile();
          }catch(Exception e) {
            throw new UnexpectedException(e);
          }
        }
      }
      public String getPackage(){
        try {
          return doc.getPackageName(); 
        }catch(InvalidPackageException e) {
          return null;
        }
      }
      public boolean isActive() { 
        return _model.getActiveDocument() == doc;
      }
      public boolean isUntitled() { 
        return doc.isUntitled(); 
      }
    };
  }
  
  private void _revert() {
    try {
      _model.getActiveDocument().revertFile();
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

  /**
   * Saves the current
   */
  private void _saveCurrentDirectory() {
    try {
      try {
        DrJava.getConfig().setSetting(LAST_DIRECTORY, _getFullFile(_model.getActiveDocument().getFile()));
      }
      catch (IllegalStateException ise) {
        // Oops, no active document for which to get the file.
        // Try saving the current directory of the open chooser.
        DrJava.getConfig().setSetting(LAST_DIRECTORY, _getFullFile(_openChooser.getCurrentDirectory()));
      }
      catch (FileMovedException fme) {
        // File moved ... well, that's ok, right?  Try again with the new file.
        DrJava.getConfig().setSetting(LAST_DIRECTORY, _getFullFile(fme.getFile()));
      }
    }
    catch (IOException ioe) {
      // getting canonical path probably failed under windows.
      // Not much we can do about this.
    }
    catch (Throwable t) {
      // Oops...
//      JOptionPane.showMessageDialog(this, t.getMessage());
    }
  }

  private void _quit() {
    if (_promptBeforeQuit) {
      String title = "Quit DrJava?";
      String message = "Are you sure you want to quit DrJava?";
      ConfirmCheckBoxDialog dialog =
        new ConfirmCheckBoxDialog(MainFrame.this, title, message);
      int rc = dialog.show();
      if (rc != JOptionPane.YES_OPTION) {
        return;
      }
      else {
        // Only remember the checkbox if they say yes
        if (dialog.getCheckBoxValue() == true) {
          DrJava.getConfig().setSetting(QUIT_PROMPT, Boolean.FALSE);
        }
      }
    }
    
    if(! _checkProjectClose())
      return;
    
    _recentFileManager.saveRecentFiles();
    _recentProjectManager.saveRecentFiles();
    _storePositionInfo();
    _saveCurrentDirectory();

    // Save recent files, but only if there wasn't a problem at startup
    // (Don't want to overwrite a custom config file with a simple typo.)
    if (!DrJava.getConfig().hadStartupException()) {
      try {
        DrJava.getConfig().saveConfiguration();
      }
      catch (IOException ioe) {
        _showIOError(ioe);
      }
    }
    _model.quit();
  }

  /**
   * Stores the current position and size info for window and panes to the
   * config framework.
   */
  private void _storePositionInfo() {
    Configuration config = DrJava.getConfig();

    // Window bounds.
    if (config.getSetting(WINDOW_STORE_POSITION).booleanValue()) {
      Rectangle bounds = getBounds();
      config.setSetting(WINDOW_HEIGHT, new Integer(bounds.height));
      config.setSetting(WINDOW_WIDTH, new Integer(bounds.width));
      config.setSetting(WINDOW_X, new Integer(bounds.x));
      config.setSetting(WINDOW_Y, new Integer(bounds.y));
    }
    else {
      // Reset to defaults to restore pristine behavior.
      config.setSetting(WINDOW_HEIGHT, WINDOW_HEIGHT.getDefault());
      config.setSetting(WINDOW_WIDTH, WINDOW_WIDTH.getDefault());
      config.setSetting(WINDOW_X, WINDOW_X.getDefault());
      config.setSetting(WINDOW_Y, WINDOW_Y.getDefault());
    }

    // Panel heights.
    if (_debugPanel != null) {
      config.setSetting(DEBUG_PANEL_HEIGHT, new Integer(_debugPanel.getHeight()));
    }
//    config.setSetting(TABS_HEIGHT,
//       new Integer(_mainSplit.getHeight() - _mainSplit.getDividerLocation()));

    // Doc list width.
    config.setSetting(DOC_LIST_WIDTH,
                      new Integer(_docSplitPane.getDividerLocation()));
  }

  private void _compile() {
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        try {
          _model.getActiveDocument().startCompile();
        }
        catch (FileMovedException fme) {
          _showFileMovedError(fme);
        }
        catch (IOException ioe) {
          _showIOError(ioe);
        }
        return null;
      }
    };
    worker.start();
  }

  private void _compileFolder(){
    INavigatorItem n;
    Enumeration<INavigatorItem> e = _model.getDocumentNavigator().getDocuments();
    final LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
    if(_model.getDocumentNavigator().isGroupSelected()){
      while (e.hasMoreElements()){
        n = e.nextElement();
        if(_model.getDocumentNavigator().isSelectedInGroup(n)){
          l.add(_model.getODDGivenIDoc(n));
        }
      }
      
      final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          try {
            _model.getCompilerModel().compile(l);
          }
          catch (FileMovedException fme) {
            _showFileMovedError(fme);
          }
          catch (IOException ioe) {
            _showIOError(ioe);
          }
          return null;
        }
      };
      worker.start();
    }
  }

  private void _compileAll() {

    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        try {
          _model.getCompilerModel().compileAll();
        }
        catch (FileMovedException fme) {
          _showFileMovedError(fme);
        }
        catch (IOException ioe) {
          _showIOError(ioe);
        }
        return null;
      }
    };
    worker.start();
  }

  
  private void _runProject(){
    try {
      final File f = _model.getMainClass();
      if(f != null){
        open(new FileOpenSelector(){
          public File[] getFiles() {
            return new File[]{ f };
          }
        });
        _model.getActiveDocument().runMain();
      }
    }
    catch (ClassNameNotFoundException e) {
      // Display a warning message if a class name can't be found.
      String msg =
        "DrJava could not find the top level class name in the\n" +
        "current document, so it could not run the class.  Please\n" +
        "make sure that the class is properly defined first.";

      JOptionPane.showMessageDialog(MainFrame.this, msg, "No Class Found",
                                    JOptionPane.ERROR_MESSAGE);
    }
    catch (FileMovedException fme) {
      _showFileMovedError(fme);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }
  
  /**
   * Internal helper method to run the main method of the current document in
   * the interactions pane.
   */
  private void _runMain() {
    try {
      _model.getActiveDocument().runMain();
    }
    catch (ClassNameNotFoundException e) {
      // Display a warning message if a class name can't be found.
      String msg =
        "DrJava could not find the top level class name in the\n" +
        "current document, so it could not run the class.  Please\n" +
        "make sure that the class is properly defined first.";

      JOptionPane.showMessageDialog(MainFrame.this, msg, "No Class Found",
                                    JOptionPane.ERROR_MESSAGE);
    }
    catch (FileMovedException fme) {
      _showFileMovedError(fme);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _junit() {
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        try {
          _model.getActiveDocument().startJUnit();
        }
        catch (FileMovedException fme) {
          _showFileMovedError(fme);
        }
        catch (IOException ioe) {
          _showIOError(ioe);
        }
        catch (ClassNotFoundException cnfe) {
          _showClassNotFoundError(cnfe);
        }
        catch (NoClassDefFoundError ncde) {
          _showNoClassDefError(ncde);
        }
        catch (ExitingNotAllowedException enae) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "An exception occurred while running JUnit, which could\n" +
                                        "not be caught by DrJava.  Details about the exception should\n" +
                                        "have been printed to your console.\n\n",
                                        "Error Running JUnit",
                                        JOptionPane.ERROR_MESSAGE);
        }
        return null;
      }
    };
    worker.start();
  }

  /**
   * Suspends the current execution of the debugger
   *
  private void debuggerSuspend() throws DebugException {
    if (inDebugMode())
      _model.getDebugger().suspend();
  }*/

  /**
   * Resumes the debugger's current execution
   */
  void debuggerResume() throws DebugException {
    if (inDebugMode()) {
      _model.getDebugger().resume();
      _removeThreadLocationHighlight();
    }
  }

  /**
   * Steps in the debugger
   */
  void debuggerStep(int flag) {
    if (inDebugMode()) {
      try {
        _model.getDebugger().step(flag);
      }
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


  /**
   * Toggles a breakpoint on the current line
   */
  void debuggerToggleBreakpoint() {
    if (inDebugMode()) {
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
      if (isModified  && !_currentDefPane.hasWarnedAboutModified() &&
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
            // don't break -- maybe update option
          case JOptionPane.NO_OPTION:
            if (dialog.getCheckBoxValue()) {
              DrJava.getConfig().setSetting(WARN_BREAKPOINT_OUT_OF_SYNC, Boolean.FALSE);
            }
            break;
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
        debugger.toggleBreakpoint(doc,
                                  _currentDefPane.getCaretPosition(),
                                  _currentDefPane.getCurrentLine());
      }
      catch (DebugException de) {
        _showError(de, "Debugger Error",
                   "Could not set a breakpoint at the current line.");
      }
    }
  }


  /*
  private void _getText(String name) {
    _field = name;
  }*/

  /**
   * Adds a watch to a given variable or field
   *
  void debuggerAddWatch() {
    if (inDebugMode()) {
      //final String field;
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      final JDialog getFieldDialog = new JDialog(this, "Choose Field to be Watched", true);
      //getFieldDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      final JTextField fieldName = new JTextField();
      getFieldDialog.setSize(new Dimension(150, 60));
      getFieldDialog.getContentPane().add(fieldName);
      fieldName.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          _getText(fieldName.getText());
          getFieldDialog.dispose();
        }
      });
      getFieldDialog.setLocation(300,300);
      getFieldDialog.show();
      Debugger debugger = _model.getDebugger();
      debugger.addWatch(_field);
    }
  }*/

  /**
   * Displays all breakpoints currently set in the debugger
   *
  void _printBreakpoints() {
    _model.getDebugger().printBreakpoints();
  }*/

  /**
   * Clears all breakpoints from the debugger
   */
  void debuggerClearAllBreakpoints() {
    try {
      _model.getDebugger().removeAllBreakpoints();
    }
    catch (DebugException de) {
      _showError(de, "Debugger Error",
                 "Could not remove all breakpoints.");
    }
  }


  void _showFileMovedError(FileMovedException fme) {
    try {
      File f = fme.getFile();
      OpenDefinitionsDocument doc = _model.getDocumentForFile(f);
      if (doc != null) {
        if (_saveSelector.shouldSaveAfterFileMoved(doc, f)) {
          _saveAs();
        }
      }
    }
    catch (IOException ioe) {
      // Couldn't find the document, so ignore the FME
    }
  }

  void _showProjectFileParseError(MalformedProjectFileException mpfe) {
    _showError(mpfe, "Invalid Project File",
               "The specified file is not a valid project file.");
  }
  
  void _showFileNotFoundError(FileNotFoundException fnf) {
    _showError(fnf, "File Not Found",
               "The specified file was not found on disk.");
  }

  void _showIOError(IOException ioe) {
    _showError(ioe, "Input/output error",
               "An I/O exception occurred during the last operation.");
  }

  void _showClassNotFoundError(ClassNotFoundException cnfe) {
    _showError(cnfe, "Class Not Found",
               "A ClassNotFound exception occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant " +
               "directories.\n\n");
  }

  void _showNoClassDefError(NoClassDefFoundError ncde) {
    _showError(ncde, "No Class Def",
               "A NoClassDefFoundError occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant paths.\n\n");
  }

  void _showDebugError(DebugException de) {
    _showError(de, "Debug Error",
               "A Debugger error occurred in the last operation.\n\n");
  }

  private void _showError(Throwable e, String title, String message) {
//    System.out.println(e);
    JOptionPane.showMessageDialog(this,
                                  message + "\n" + e,
                                  title,
                                  JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Check if any errors occurred while parsing the config file,
   * and display a message if necessary.
   */
  private void _showConfigException() {
    if (DrJava.getConfig().hadStartupException()) {
      Exception e = DrJava.getConfig().getStartupException();
      _showError(e, "Error in Config File",
                 "Could not read the '.drjava' configuration file\n" +
                 "in your home directory.  Starting with default\n" +
                 "values instead.\n\n" +
                 "The problem was:\n");
    }
  }


  /**
   * Shows a brief warning to the user, to inform him/her that the file he/she is
   * debugging has been modified since its last save and should probably be saved
   * and recompiled. Does not actually save or recompile for the user.
   */
  private void _showDebuggingModifiedFileWarning() {
    if (DrJava.getConfig().getSetting(WARN_DEBUG_MODIFIED_FILE).booleanValue()) {
      String msg =
        "This document has been modified since its last save and\n" +
        "may be out of sync with the debugger. It is suggested that\n" +
        "you save and recompile before continuing to debug in order\n" +
        "to avoid any unexpected errors.";
      String title = "Debugging modified file!";

      ConfirmCheckBoxDialog dialog =
        new ConfirmCheckBoxDialog(MainFrame.this, title, msg,
                                  "Do not show this message again",
                                  JOptionPane.WARNING_MESSAGE,
                                  JOptionPane.DEFAULT_OPTION);
      if (dialog.show() == JOptionPane.OK_OPTION && dialog.getCheckBoxValue()) {
        DrJava.getConfig().setSetting(WARN_DEBUG_MODIFIED_FILE, Boolean.FALSE);
      }
      _currentDefPane.hasWarnedAboutModified(true);
    }
  }

   /**
   * Returns the File selected by the JFileChooser.
   * @param fc File chooser presented to the user
   * @param choice return value from fc
   * @return Selected File
   * @throws OperationCanceledException if file choice canceled
   * @throws RuntimeException if fc returns a bad file or choice
   */
  private File getChosenFile(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
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
              return new File (chosen.getAbsolutePath() + DrJava.LANGUAGE_LEVEL_EXTENSIONS[DrJava.getConfig().getSetting(LANGUAGE_LEVEL)]);//".java");
          }
          return chosen;
        }
        else
          throw new RuntimeException("Filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("Filechooser returned bad rc " + choice);
    }
  }
  /**
   * Returns the Files selected by the JFileChooser.
   * @param fc File chooser presented to the user
   * @param choice return value from fc
   * @return Selected Files - this array will be size 1 for single-selection dialogs.
   * @throws OperationCanceledException if file choice canceled
   * @throws RuntimeException if fc returns a bad file or choice
   */
  private File[] getChosenFiles(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
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

  private void _selectAll() {
    _currentDefPane.selectAll();
  }

  /**
   * Ask the user what line they'd like to jump to, then go there.
   */
  private void _gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Go to Line";
    String lineStr = JOptionPane.showInputDialog(this,
                                                 msg,
                                                 title,
                                                 JOptionPane.QUESTION_MESSAGE);
    try {
      if (lineStr != null) {
        int lineNum = Integer.parseInt(lineStr);
        _currentDefPane.centerViewOnLine(lineNum);
        int pos = _model.getActiveDocument().gotoLine(lineNum);
        _currentDefPane.setCaretPosition(pos);
        /*
        // Center the destination line on the screen
        // (this code taken from FindReplaceDialog's _selectFoundItem method)
        JScrollPane defScroll = (JScrollPane)
          _defScrollPanes.get(_model.getActiveDocument());
        int viewHeight = (int)defScroll.getViewport().getSize().getHeight();
        // Scroll to make sure this item is visible
        // Centers the selection in the viewport
        Rectangle startRect = _currentDefPane.modelToView(pos);
        int startRectY = (int)startRect.getY();
        startRect.setLocation(0, startRectY-viewHeight/2);
        //Rectangle endRect = _defPane.modelToView(to - 1);
        Point endPoint = new Point(0, startRectY+viewHeight/2-1);
        startRect.add(endPoint);

        _currentDefPane.scrollRectToVisible(startRect);

        //Commented out this call because it would be impossible to
        //center the viewport on pos without passing in the viewport.
        //Perhaps setPositionAndScroll can be changed in the future to
        //allow this.
        //_currentDefPane.setPositionAndScroll(pos);
        _currentDefPane.requestFocus();
        */
      }
    }
    catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
    //catch (BadLocationException ble) {}
  }

  /**
   * Removes the ErrorCaretListener corresponding to
   * the given document, after that document has been closed.
   * (Allows pane and listener to be garbage collected...)
   */
  private void _removeErrorListener(OpenDefinitionsDocument doc) {
    JScrollPane scroll = _defScrollPanes.get(doc);
    if (scroll != null) {
      DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
      pane.removeCaretListener(pane.getErrorCaretListener());
    }
  }

  /**
   * Initializes all action objects.
   * Adds icons and descriptions to several of the actions.
   * Note: this initialization will later be done in the
   * constructor of each action, which will subclass AbstractAction.
   */
  private void _setUpActions() {
    _setUpAction(_newAction, "New", "Create a new document");
    _setUpAction(_newJUnitTestAction, "New", "Create a new JUnit test case class");
    _setUpAction(_newProjectAction, "New", "Make a new project");
    _setUpAction(_openAction, "Open", "Open an existing file");
    _setUpAction(_openFolderAction, "Open Folder", "Open all files within a directory");
    _setUpAction(_openFileOrProjectAction, "Open", "Open an existing file or project");
    _setUpAction(_openProjectAction, "Open", "Open an existing project");
    _setUpAction(_saveAction, "Save", "Save the current document");
    _setUpAction(_saveAsAction, "Save As", "SaveAs",
                 "Save the current document with a new name");
    _setUpAction(_saveProjectAction, "Save", "Save", "Save the current project");
    _saveProjectAction.setEnabled(false);
    _setUpAction(_saveProjectAsAction, "Save As", "SaveAs", 
                 "Save all currently open files to new project file");
    _setUpAction(_revertAction, "Revert", "Revert the current document to the saved version");
    //_setUpAction(_revertAllAction, "Revert All", "RevertAll",
    //             "Revert all open documents to the saved versions");

    _setUpAction(_closeAction, "Close", "Close the current document");
    _setUpAction(_closeAllAction, "Close All", "CloseAll", "Close all documents");
    _setUpAction(_closeProjectAction, "Close", "CloseAll", "Close the current project");
    _closeProjectAction.setEnabled(false);
    
    _setUpAction(_projectPropertiesAction, "Project Properties", "Preferences", "Edit Project Properties");
    _projectPropertiesAction.setEnabled(false);    

    _setUpAction(_junitProjectAction, "Test", "Test", "Test the current project");
    _junitProjectAction.setEnabled(false);    

  _setUpAction(_compileProjectAction, "Compile", "Compile",
                 "Compile the current project");
    _compileProjectAction.setEnabled(false);
      
    _setUpAction(_runProjectAction, "Run","Run the project's main method");
    _runProjectAction.setEnabled(false);
    
    _setUpAction(_saveAllAction, "Save All", "SaveAll", "Save all open documents");

    _setUpAction(_compileAction, "Compile", "Compile the current document");
    _setUpAction(_compileAllAction, "Compile All", "CompileAll",
                 "Compile all open documents");
    _setUpAction(_printAction, "Print", "Print the current document");
    _setUpAction(_pageSetupAction, "Page Setup", "PageSetup", "Change the printer settings");
    _setUpAction(_printPreviewAction, "Print Preview", "PrintPreview", "Preview how the document will be printed");

    _setUpAction(_quitAction, "Quit", "Quit", "Quit DrJava");

    _setUpAction(_undoAction, "Undo", "Undo previous command");
    _setUpAction(_redoAction, "Redo", "Redo last undo");
    _undoAction.putValue(Action.NAME, "Undo Previous Command");
    _redoAction.putValue(Action.NAME, "Redo Last Undo");

    _setUpAction(cutAction, "Cut", "Cut selected text to the clipboard");
    _setUpAction(copyAction, "Copy", "Copy selected text to the clipboard");
    _setUpAction(pasteAction, "Paste", "Paste text from the clipboard");
    _setUpAction(_selectAllAction, "Select All", "Select all text");

    cutAction.putValue(Action.NAME, "Cut");
    copyAction.putValue(Action.NAME, "Copy");
    pasteAction.putValue(Action.NAME, "Paste");

    _setUpAction(_indentLinesAction, "Indent Lines", "Indent all selected lines");
    _setUpAction(_commentLinesAction, "Comment Lines", "Comment out all selected lines");
    _setUpAction(_uncommentLinesAction, "Uncomment Lines", "Uncomment all selected lines");

    _setUpAction(_findReplaceAction, "Find", "Find or replace text in the document");
    _setUpAction(_findNextAction, "Find Next", "Repeats the last find");
    _setUpAction(_gotoLineAction, "Go to line", "Go to a line number in the document");

    _setUpAction(_switchToPrevAction, "Back", "Switch to the previous document");
    _setUpAction(_switchToNextAction, "Forward", "Switch to the next document");
    _setUpAction(_switchToPreviousPaneAction, "Previous Pane", "Switch focus to the previous pane");
    _setUpAction(_switchToNextPaneAction, "Next Pane", "Switch focus to the next pane");

    _setUpAction(_editPreferencesAction, "Preferences", "Edit configurable settings in DrJava");
    
    _setUpAction(_junitAction, "Test Current", "Run JUnit over the current document");
    _setUpAction(_junitAllAction, "Test", "Run JUnit over all open JUnit tests");
    _setUpAction(_javadocAllAction, "Javadoc", "Create and save Javadoc for the packages of all open documents");
    _setUpAction(_javadocCurrentAction, "Preview Javadoc Current", "Preview the Javadoc for the current document");
    _setUpAction(_runAction, "Run Document", "Run the main method of the current document");

    _setUpAction(_executeHistoryAction, "Execute History", "Load and execute a history of interactions from a file");
    _setUpAction(_loadHistoryScriptAction, "Load History as Script", "Load a history from a file as a series of interactions");
    _setUpAction(_saveHistoryAction, "Save History", "Save the history of interactions to a file");
    _setUpAction(_clearHistoryAction, "Clear History", "Clear the current history of interactions");

    //_setUpAction(_abortInteractionAction, "Break", "Abort the current interaction");
    _setUpAction(_resetInteractionsAction, "Reset", "Reset the Interactions Pane");
    _setUpAction(_viewInteractionsClasspathAction, "View Interactions Classpath", "Display the classpath in use by the Interactions Pane");
    _setUpAction(_copyInteractionToDefinitionsAction, "Lift Current Interaction", "Copy the current interaction into the Definitions Pane");

    _setUpAction(_clearConsoleAction, "Clear Console", "Clear all text in the Console Pane");
    _setUpAction(_showDebugConsoleAction, "Show DrJava Debug Console", "<html>Show a console for debugging DrJava<br>" +
                 "(with \"mainFrame\", \"model\", and \"config\" variables defined)</html>");

    _setUpAction(_toggleDebuggerAction, "Debug Mode", "Enable or disable DrJava's debugger");
    _setUpAction(_toggleBreakpointAction, "Toggle Breakpoint", "Set or clear a breakpoint on the current line");
    _setUpAction(_clearAllBreakpointsAction, "Clear Breakpoints", "Clear all breakpoints in all classes");
    _setUpAction(_resumeDebugAction, "Resume", "Resume the current suspended thread");
    _setUpAction(_stepIntoDebugAction, "Step Into", "Step into the current line or method call");
    _setUpAction(_stepOverDebugAction, "Step Over", "Step over the current line or method call");
    _setUpAction(_stepOutDebugAction, "Step Out", "Step out of the current method");

    _setUpAction(_helpAction, "Help", "Show documentation on how to use DrJava");
    _setUpAction(_aboutAction, "About", "About DrJava");

  }

  private void _setUpAction(Action a, String name, String icon, String shortDesc) {
    a.putValue(Action.SMALL_ICON, _getIcon(icon + "16.gif"));
    a.putValue(Action.DEFAULT, name);
    a.putValue(Action.SHORT_DESCRIPTION, shortDesc);
  }

  private void _setUpAction(Action a, String icon, String shortDesc) {
    _setUpAction(a, icon, icon, shortDesc);
  }


  /**
   * Returns the icon with the given name.
   * All icons are assumed to reside in the /edu/rice/cs/drjava/ui/icons
   * directory.
   * @param name Name of icon image file
   * @return ImageIcon object constructed from the file
   */
  private ImageIcon _getIcon(String name) {
    return getIcon(name);
  }

  public static ImageIcon getIcon(String name) {
    URL url = MainFrame.class.getResource(ICON_PATH + name);
    if (url != null) {
      return new ImageIcon(url);
    }
    return null;
  }

  
  /**
   * this allows us to intercept key events when compiling testing
   * and turn them off when the glass pane is up
   */
  private class MenuBar extends JMenuBar{
    public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
      if(MainFrame.this.getAllowKeyEvents()){
        return super.processKeyBinding(ks, e, condition, pressed);
      }else{
        return false;
      }
    }
  }
  
  /**
   * Sets up the components of the menu bar and links them to the private
   * fields within MainFrame.  This method serves to make the code
   * more legible on the higher calling level, i.e., the constructor.
   */
  private void _setUpMenuBar() {
    boolean showDebugger = (_model.getDebugger().isAvailable());

    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    _menuBar = new MenuBar();
    _fileMenu = _setUpFileMenu(mask);
    _editMenu = _setUpEditMenu(mask);
    _toolsMenu = _setUpToolsMenu(mask);
    _projectMenu = _setUpProjectMenu(mask);
    if (showDebugger) _debugMenu = _setUpDebugMenu(mask);
    _languageLevelMenu = _setUpLanguageLevelMenu(mask);
    _helpMenu = _setUpHelpMenu(mask);

    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_toolsMenu);
    _menuBar.add(_projectMenu);
    if (showDebugger) _menuBar.add(_debugMenu);
    _menuBar.add(_languageLevelMenu);
    _menuBar.add(_helpMenu);
    setJMenuBar(_menuBar);
  }

  /**
   * Adds an Action as a menu item to the given menu, using the
   * specified configurable keystroke.
   * @param menu Menu to add item to
   * @param a Action for the menu item
   * @param opt Configurable keystroke for the menu item
   */
  private void _addMenuItem(JMenu menu, Action a, Option<KeyStroke> opt) {
    JMenuItem item;
    item = menu.add(a);
    _setMenuShortcut(item, a, opt);
  }

  /**
   * Sets the given menu item to have the specified configurable keystroke.
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
    if ((ks != KeyStrokeOption.NULL_KEYSTROKE) &&
        (KeyBindingManager.Singleton.get(ks) == a)) {
      item.setAccelerator(ks);
      //KeyBindingManager.Singleton.addListener(opt, item);
    }
  }

  /**
   * Creates and returns a file menu.  Side effects: sets values for
   * _saveMenuItem.
   */
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
    //fileMenu.add(_saveProjectAsAction);

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
    _addMenuItem(fileMenu, _printPreviewAction, KEY_PRINT_PREVIEW);
    _addMenuItem(fileMenu, _printAction, KEY_PRINT);

    // Quit
    fileMenu.addSeparator();
    _addMenuItem(fileMenu, _quitAction, KEY_QUIT);

    return fileMenu;
  }

  /**
   * Creates and returns a edit menu.
   */
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
    _addMenuItem(editMenu, _selectAllAction, KEY_SELECT_ALL);

    // Indent lines, comment lines
    editMenu.addSeparator();
    //_addMenuItem(editMenu, _indentLinesAction, KEY_INDENT);
    JMenuItem editItem = editMenu.add(_indentLinesAction);
    editItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
    _addMenuItem(editMenu, _commentLinesAction, KEY_COMMENT_LINES);
    _addMenuItem(editMenu, _uncommentLinesAction, KEY_UNCOMMENT_LINES);

    // Find/replace, goto
    editMenu.addSeparator();
    _addMenuItem(editMenu, _findReplaceAction, KEY_FIND_REPLACE);
    _addMenuItem(editMenu, _findNextAction, KEY_FIND_NEXT);
    _addMenuItem(editMenu, _gotoLineAction, KEY_GOTO_LINE);

    // Next, prev doc
    editMenu.addSeparator();
    _addMenuItem(editMenu, _switchToPrevAction, KEY_PREVIOUS_DOCUMENT);
    _addMenuItem(editMenu, _switchToNextAction, KEY_NEXT_DOCUMENT);
    _addMenuItem(editMenu, _switchToPreviousPaneAction, KEY_PREVIOUS_PANE);
    _addMenuItem(editMenu, _switchToNextPaneAction, KEY_NEXT_PANE);

    // access to configurations GUI
    editMenu.addSeparator();
    _addMenuItem(editMenu, _editPreferencesAction, KEY_PREFERENCES);

    // Add the menus to the menu bar
    return editMenu;
  }

  /**
   * Creates and returns a tools menu.
   */
  private JMenu _setUpToolsMenu(int mask) {
    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.setMnemonic(KeyEvent.VK_T);

    // Compile, Test, Javadoc
    _addMenuItem(toolsMenu, _compileAllAction, KEY_COMPILE_ALL);
    _addMenuItem(toolsMenu, _compileAction, KEY_COMPILE);
    _addMenuItem(toolsMenu, _junitAllAction, KEY_TEST_ALL);
    _addMenuItem(toolsMenu, _junitAction, KEY_TEST);
    _addMenuItem(toolsMenu, _javadocAllAction, KEY_JAVADOC_ALL);
    _addMenuItem(toolsMenu, _javadocCurrentAction, KEY_JAVADOC_CURRENT);
    toolsMenu.addSeparator();

    // Run
    _addMenuItem(toolsMenu, _runAction, KEY_RUN);
    toolsMenu.addSeparator();

    _addMenuItem(toolsMenu, _executeHistoryAction, KEY_EXECUTE_HISTORY);
    _addMenuItem(toolsMenu, _loadHistoryScriptAction, KEY_LOAD_HISTORY_SCRIPT);
    _addMenuItem(toolsMenu, _saveHistoryAction, KEY_SAVE_HISTORY);
    _addMenuItem(toolsMenu, _clearHistoryAction, KEY_CLEAR_HISTORY);
    toolsMenu.addSeparator();

    // Abort/reset interactions, clear console
    /*
    _abortInteractionAction.setEnabled(false);
    _addMenuItem(toolsMenu, _abortInteractionAction, KEY_ABORT_INTERACTION);
    */
    _addMenuItem(toolsMenu, _resetInteractionsAction, KEY_RESET_INTERACTIONS);
    _addMenuItem(toolsMenu, _viewInteractionsClasspathAction, KEY_VIEW_INTERACTIONS_CLASSPATH);
    _addMenuItem(toolsMenu, _copyInteractionToDefinitionsAction, KEY_LIFT_CURRENT_INTERACTION);
    toolsMenu.addSeparator();

    _addMenuItem(toolsMenu, _clearConsoleAction, KEY_CLEAR_CONSOLE);
    if (DrJava.getConfig().getSetting(SHOW_DEBUG_CONSOLE).booleanValue()) {
      toolsMenu.add(_showDebugConsoleAction);
    }

    // Add the menus to the menu bar
    return toolsMenu;
  }
  
  /**
   * Creates and returns a project menu.
   */
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
    projectMenu.add(_compileProjectAction);
    projectMenu.add(_runProjectAction);
    projectMenu.add(_junitProjectAction);
    
    projectMenu.addSeparator();
    // eventually add project options
    projectMenu.add(_projectPropertiesAction);
    
    return projectMenu;
  }
  
  /**
   * Creates and returns a debug menu.
   */
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

  /**
   * Called every time the debug mode checkbox is toggled. The resume and step
   * functions should always be disabled.
   */
  private void _setDebugMenuItemsEnabled(boolean enabled) {
    _debuggerEnabledMenuItem.setSelected(enabled);
    //_suspendDebugAction.setEnabled(false);
    _resumeDebugAction.setEnabled(false);
    _stepIntoDebugAction.setEnabled(false);
    _stepOverDebugAction.setEnabled(false);
    _stepOutDebugAction.setEnabled(false);
    _toggleBreakpointAction.setEnabled(enabled);
    //_printBreakpointsAction.setEnabled(enabled);
    _clearAllBreakpointsAction.setEnabled(enabled);
    if (_debugPanel != null)
      _debugPanel.disableButtons();
  }

  /**
   * Enables and disables the appropriate menu items in the debug menu
   * depending upon the state of the current thread
   * @param isSuspended is true when the current thread has just been suspended
   * false if the current thread has just been resumed
   */
  private void _setThreadDependentDebugMenuItems(boolean isSuspended) {
    //_suspendDebugAction.setEnabled(!isSuspended);
    _resumeDebugAction.setEnabled(isSuspended);
    _stepIntoDebugAction.setEnabled(isSuspended);
    _stepOverDebugAction.setEnabled(isSuspended);
    _stepOutDebugAction.setEnabled(isSuspended);
    _debugPanel.setThreadDependentButtons(isSuspended);
  }
  
  /**
   * Creates and returns the language levels menu
   */
  private JMenu _setUpLanguageLevelMenu(int mask) {
    JMenu languageLevelMenu = new JMenu("Language Level");
    languageLevelMenu.setMnemonic(KeyEvent.VK_L);
    ButtonGroup group = new ButtonGroup();
    
    final Configuration config = DrJava.getConfig();
    int currentLanguageLevel = config.getSetting(LANGUAGE_LEVEL);
    JRadioButtonMenuItem rbMenuItem;
    rbMenuItem = new JRadioButtonMenuItem("Full Java");
    rbMenuItem.setToolTipText("Use full Java syntax");
    if (currentLanguageLevel == DrJava.FULL_JAVA) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJava.FULL_JAVA);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    languageLevelMenu.addSeparator();
    
    rbMenuItem = new JRadioButtonMenuItem("Elementary");
    rbMenuItem.setToolTipText("Use Elementary language-level features");
    if (currentLanguageLevel == DrJava.ELEMENTARY_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJava.ELEMENTARY_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    
    rbMenuItem = new JRadioButtonMenuItem("Intermediate");
    rbMenuItem.setToolTipText("Use Intermediate language-level features");
    if (currentLanguageLevel == DrJava.INTERMEDIATE_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJava.INTERMEDIATE_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    
    rbMenuItem = new JRadioButtonMenuItem("Advanced");
    rbMenuItem.setToolTipText("Use Advanced language-level features");
    if (currentLanguageLevel == DrJava.ADVANCED_LEVEL) { rbMenuItem.setSelected(true); }
    rbMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        config.setSetting(LANGUAGE_LEVEL, DrJava.ADVANCED_LEVEL);
      }});
    group.add(rbMenuItem);
    languageLevelMenu.add(rbMenuItem);
    return languageLevelMenu;
  }

  /**
   * Creates and returns a help menu.
   */
  private JMenu _setUpHelpMenu(int mask) {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    _addMenuItem(helpMenu, _helpAction, KEY_HELP);
    _addMenuItem(helpMenu, _aboutAction, KEY_ABOUT);
    return helpMenu;
  }

  /**
   * Creates a toolbar button for undo and redo, which behave differently.
   */
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
      if (useText) {
        ret.setText((String) a.getValue(Action.DEFAULT));
      }
    }

    ret.setEnabled(false);
    ret.addActionListener(a);
    ret.setToolTipText( (String) a.getValue(Action.SHORT_DESCRIPTION));
    ret.setFont(buttonFont);

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

  /**
   * Sets up all buttons for the toolbar except for undo and redo, which use
   * _createManualToolbarButton.
   */
  public JButton _createToolbarButton(Action a) {
    boolean useText = DrJava.getConfig().getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean useIcons = DrJava.getConfig().getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
    Font buttonFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);

    final JButton result = new UnfocusableButton(a);
    result.setText((String) a.getValue(Action.DEFAULT));
    result.setFont(buttonFont);
    if (!useIcons) result.setIcon(null);
    if (!useText && (result.getIcon() != null)) {
      result.setText("");
    }
    return result;
  }

  /**
   * Sets up the toolbar with several useful buttons.
   * Most buttons are always enabled, but those that are not are
   * maintained in fields to allow enabling and disabling.
   */
  private void _setUpToolBar() {
    _toolBar = new JToolBar();

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
    _undoButton = _createManualToolbarButton(_undoAction);
    _toolBar.add(_undoButton);
    _redoButton = _createManualToolbarButton(_redoAction);
    _toolBar.add(_redoButton);

    // Find
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(_findReplaceAction));

    // Compile, reset, abort
    _toolBar.addSeparator();
    _compileButton = _createToolbarButton(_compileAllAction);
    _toolBar.add(_compileButton);
    _toolBar.add(_createToolbarButton(_resetInteractionsAction));
    //_toolBar.add(_createToolbarButton(_abortInteractionAction));

    // Junit
    _toolBar.addSeparator();

    _toolBar.add(_createToolbarButton(_junitAllAction));
    _toolBar.add(_createToolbarButton(_javadocAllAction));

    // Correct the vertical height of the buttons.
    _fixToolbarHeights();

    getContentPane().add(_toolBar, BorderLayout.NORTH);
  }

  /**
   * Update the toolbar's buttons, following any change to TOOLBAR_ICONS_ENABLED,
   * TOOLBAR_TEXT_ENABLED, or FONT_TOOLBAR (name, style, text)
   */
  private void _updateToolbarButtons() {

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
          if (b == _undoButton) {
            a = _undoAction;
          }
          else if (b == _redoButton) {
            a = _redoAction;
          }
          else {
            continue;
          }
        }

        if (b.getIcon() == null) {
          if (iconsEnabled) {
            b.setIcon( (Icon) a.getValue(Action.SMALL_ICON));
          }
        }
        else {
          if (!iconsEnabled && b.getText() != "") {
            b.setIcon(null);
          }
        }

        if (b.getText() == "") {
          if (textEnabled) {
            b.setText( (String) a.getValue(Action.DEFAULT));
          }
        }
        else {
          if (!textEnabled && b.getIcon() != null) {
            b.setText("");
          }
        }
      }
    }

    // Correct the vertical height of the buttons.
    _fixToolbarHeights();
  }

  /**
   * Ensures that all toolbar buttons have the same height.
   */
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

  /**
   * Sets up the status bar with the filename field.
   */
  private void _setUpStatusBar() {
    // Set up the 3 labels:
    _fileNameField = new JLabel();
    _fileNameField.setFont(_fileNameField.getFont().deriveFont(Font.PLAIN));

    _sbMessage = new JLabel();//("This is the text for the center message");
    _sbMessage.setHorizontalAlignment(SwingConstants.RIGHT);

    JPanel fileNameAndMessagePanel = new JPanel(new BorderLayout());
    fileNameAndMessagePanel.add(_fileNameField, BorderLayout.CENTER);
    fileNameAndMessagePanel.add(_sbMessage, BorderLayout.EAST);

    _currLocationField = new JLabel();
    _currLocationField.setFont(_currLocationField.getFont().deriveFont(Font.PLAIN));
    _currLocationField.setHorizontalAlignment(SwingConstants.RIGHT);
    _currLocationField.setPreferredSize(new Dimension(65,12));
    //_currLocationField.setVisible(true);

    // Create the status bar panel
    //SpringLayout layout = new SpringLayout();
    _statusBar = new JPanel(new BorderLayout());//( layout );
    _statusBar.add( fileNameAndMessagePanel, BorderLayout.CENTER );
//    _statusBar.add( sbMessagePanel, BorderLayout.CENTER );
    _statusBar.add( _currLocationField, BorderLayout.EAST );
    _statusBar.setBorder(
      new CompoundBorder(new EmptyBorder(2,2,2,2),
                         new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                                            new EmptyBorder(2,2,2,2))));
    getContentPane().add(_statusBar, BorderLayout.SOUTH);

    /*
    //Adjust constraints for the fileName label so it's next to the left edge.
    layout.getConstraints(_fileNameField).setX(Spring.constant(0));

    //Adjust constraints for the message label so it's spaced a bit from the right.
    //and doesn't interfere with the left-most label
    layout.putConstraint(SpringLayout.EAST, _sbMessage, -65,
                         SpringLayout.EAST, _statusBar);

    //Adjust constraints for the location label so it's next to the right edge.
    layout.putConstraint(SpringLayout.EAST, _currLocationField, 0,
                         SpringLayout.EAST, _statusBar);

    //Adjust constraints for the panel to set its size
    layout.putConstraint(SpringLayout.SOUTH, _statusBar, 0,
                         SpringLayout.SOUTH, _currLocationField);*/
  }

  /**
   * Inner class to handle the updating of current position within the
   * document.  Registered with the definitionspane.
   **/
  private class PositionListener implements CaretListener {

    public void caretUpdate( CaretEvent ce ) {
      _model.getActiveDocument().
        setCurrentLocation(ce.getDot());
      updateLocation();
    }

    public void updateLocation() {
      DefinitionsPane p = _currentDefPane;
      _currLocationField.setText(p.getCurrentLine() +
                                 ":" + p.getCurrentCol() + "\t");
    }
  }

  private void _setUpTabs() {
    _compilerErrorPanel = new CompilerErrorPanel(_model, this);

    _consoleController = new ConsoleController(_model.getConsoleDocument(),
                                               _model.getSwingConsoleDocument());
    _consolePane = _consoleController.getPane();

    // Interactions
    _interactionsController =
      new InteractionsController(_model.getInteractionsModel(),
                                 _model.getSwingInteractionsDocument());
    _interactionsController.setPrevPaneAction(_switchToPreviousPaneAction);
    _interactionsController.setNextPaneAction(_switchToNextPaneAction);
    _interactionsPane = _interactionsController.getPane();

//    _model.setInputListener(_consoleController.getInputListener());
//    _model.getInteractionsModel().setInputListener(_interactionsController.getInputListener());
    // Moved to the interactions controller.

    _findReplace = new FindReplaceDialog(this, _model);

    _consoleScroll = new BorderlessScrollPane(_consolePane) {
      public void requestFocus() {
        _consolePane.requestFocus();
      }
    };
    JScrollPane interactionsScroll = new BorderlessScrollPane(_interactionsPane);
    _interactionsContainer = new JPanel(new BorderLayout()) {
      public void requestFocus() {
        _interactionsPane.requestFocus();
      }
    };
    _interactionsContainer.add(interactionsScroll, BorderLayout.CENTER);

    _junitErrorPanel = new JUnitPanel(_model, this);
    _javadocErrorPanel = new JavadocErrorPanel(_model, this);

    _tabbedPane = new JTabbedPane();
    _tabbedPane.addChangeListener(new ChangeListener () {
      public void stateChanged(ChangeEvent e) {
        clearStatusMessage();

        if (_tabbedPane.getSelectedComponent() == _interactionsContainer) {
          /**
           * This was probably a bad design decision but I couldn't think
           * of any other way around it.  When the intaractions tab gains
           * focus we want the interactions pane (editor pane) to receive
           * the focus.  But focus is given to the tab itself *AFTER* this
           * listener is called on.  This code waits for a bit for Swing
           * to give the tab focus, then steals the focus back to the
           * interactions pane.
           */
        }
        else if (_tabbedPane.getSelectedComponent() == _consoleScroll) {
          _consolePane.requestFocus();
        }

        // Update error highlights?
        if (_currentDefPane != null) {
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.removeErrorHighlight();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
        }
}
    });

    //_interactionsWithSyncPanel = new JPanel(new BorderLayout());
    //_syncStatus = new JLabel("Testing");
    //_interactionsWithSyncPanel.add(new BorderlessScrollPane(_interactionsPane),
    //                               BorderLayout.CENTER);
    //_interactionsWithSyncPanel.add(_syncStatus, BorderLayout.SOUTH);

    _tabbedPane.add("Interactions", _interactionsContainer);
    _tabbedPane.add("Console", _consoleScroll);

    _tabs = new LinkedList<TabbedPanel>();

    _tabs.addLast(_compilerErrorPanel);
    _tabs.addLast(_junitErrorPanel);
    _tabs.addLast(_javadocErrorPanel);
    _tabs.addLast(_findReplace);

    _interactionsPane.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _interactionsContainer;
      }
    });
    _consolePane.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _consoleScroll;
      }
    });
//    _compilerErrorPanel.addFocusListener(new LastFocusListener());
//    _junitErrorPanel.addFocusListener(new LastFocusListener());
//    _javadocErrorPanel.addFocusListener(new LastFocusListener());
//    _findReplace.addFocusListener(new LastFocusListener());
    _compilerErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _compilerErrorPanel;
      }
    });
    _junitErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _junitErrorPanel;
      }
    });
    _javadocErrorPanel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _javadocErrorPanel;
      }
    });
    _findReplace.getFindField().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        _lastFocusOwner = _findReplace;
      }
    });

    // Show compiler output pane by default
    showTab(_compilerErrorPanel);

    _tabbedPane.setSelectedIndex(0);
  }

  /**
   * Configures the component used for selecting active documents.
   */
  /*private void _setUpDocumentSelector() {
      _model.getDocCollectionWidget() = _model.getDocCollectionWidget();
  }*/
  /*
  private void _setUpDocumentSelector() {
    _docList = new JList(((DefaultGlobalModel) _model).getDefinitionsDocs());
    _docList.setSelectionModel(_model.getDocumentSelectionModel());
    _docList.setCellRenderer(new DocCellRenderer());
  }
  */

  /**
   * Sets up the context menu to show in the document pane.
   */
  private void _setUpContextMenus() {
    // pop-up menu for a folder in tree view
    _navPaneFolderPopupMenu = new JPopupMenu();
    _navPaneFolderPopupMenu.add(_closeFolderAction);
    _navPaneFolderPopupMenu.add(_compileFolderAction);
    _navPaneFolderPopupMenu.add(_junitFolderAction);
    
    _navPanePopupMenuForExternal = new JPopupMenu();
    _navPanePopupMenuForExternal.add(_saveAction);
    _navPanePopupMenuForExternal.add(_saveAsAction);
    _navPanePopupMenuForExternal.add(_revertAction);
    _navPanePopupMenuForExternal.addSeparator();
    _navPanePopupMenuForExternal.add(_closeAction);
    _navPanePopupMenuForExternal.addSeparator();
    _navPanePopupMenuForExternal.add(_printAction);
    _navPanePopupMenuForExternal.add(_printPreviewAction);
    _navPanePopupMenuForExternal.addSeparator();
    _navPanePopupMenuForExternal.add(_compileAction);
    _navPanePopupMenuForExternal.add(_junitAction);
    _navPanePopupMenuForExternal.add(_javadocCurrentAction);
    _navPanePopupMenuForExternal.add(_runAction);
    _navPanePopupMenuForExternal.addSeparator();
    _navPanePopupMenuForExternal.add(_moveToAuxiliaryAction);
    
    _navPanePopupMenuForAuxiliary = new JPopupMenu();
    _navPanePopupMenuForAuxiliary.add(_saveAction);
    _navPanePopupMenuForAuxiliary.add(_saveAsAction);
    _navPanePopupMenuForAuxiliary.add(_revertAction);
    _navPanePopupMenuForAuxiliary.addSeparator();
    _navPanePopupMenuForAuxiliary.add(_closeAction);
    _navPanePopupMenuForAuxiliary.addSeparator();
    _navPanePopupMenuForAuxiliary.add(_printAction);
    _navPanePopupMenuForAuxiliary.add(_printPreviewAction);
    _navPanePopupMenuForAuxiliary.addSeparator();
    _navPanePopupMenuForAuxiliary.add(_compileAction);
    _navPanePopupMenuForAuxiliary.add(_junitAction);
    _navPanePopupMenuForAuxiliary.add(_javadocCurrentAction);
    _navPanePopupMenuForAuxiliary.add(_runAction);
    _navPanePopupMenuForAuxiliary.addSeparator();
    _navPanePopupMenuForAuxiliary.add(_removeAuxiliaryAction);
    
    // NavPane menu
    _navPanePopupMenu = new JPopupMenu();
    _navPanePopupMenu.add(_saveAction);
    _navPanePopupMenu.add(_saveAsAction);
    _navPanePopupMenu.add(_revertAction);
    _navPanePopupMenu.addSeparator();
    _navPanePopupMenu.add(_closeAction);
    _navPanePopupMenu.addSeparator();
    _navPanePopupMenu.add(_printAction);
    _navPanePopupMenu.add(_printPreviewAction);
    _navPanePopupMenu.addSeparator();
    _navPanePopupMenu.add(_compileAction);
    _navPanePopupMenu.add(_junitAction);
    _navPanePopupMenu.add(_javadocCurrentAction);
    _navPanePopupMenu.add(_runAction);
    _model.getDocCollectionWidget().addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        if(_model.getDocumentNavigator().selectDocumentAt(e.getX(), e.getY())){
          
          if(_model.getDocumentNavigator().isGroupSelected()){
            _navPaneFolderPopupMenu.show(e.getComponent(), e.getX(), e.getY());
          }else{
            try{
              String groupName = _model.getDocumentNavigator().getNameOfSelectedTopLevelGroup();
              if(groupName == "[ Source Files ]"){
                _navPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
              }else if(groupName == "[ External Files ]"){
                INavigatorItem n = _model.getDocumentNavigator().getCurrentSelectedLeaf();
                if(n == null){
                  // false alarm, a document is not really selected...
                }else{
                  OpenDefinitionsDocument d = _model.getODDGivenIDoc(n);
                  if(d.isUntitled()){
                    _navPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                  }else{
                    _navPanePopupMenuForExternal.show(e.getComponent(), e.getX(), e.getY());
                  }
                }
              }else if(groupName == "[ Auxiliary Files ]"){
                _navPanePopupMenuForAuxiliary.show(e.getComponent(), e.getX(), e.getY());
              }
            }catch(GroupNotSelectedException ex){
              // noop
            }
          }
          
//          _navPaneFolderPopupMenu.show(e.getComponent(), e.getX(), e.getY());
          
        }
      }
    });

    // Interactions pane menu
    _interactionsPanePopupMenu = new JPopupMenu();
    _interactionsPanePopupMenu.add(cutAction);
    _interactionsPanePopupMenu.add(copyAction);
    _interactionsPanePopupMenu.add(pasteAction);
    _interactionsPanePopupMenu.addSeparator();
    _interactionsPanePopupMenu.add(_executeHistoryAction);
    _interactionsPanePopupMenu.add(_loadHistoryScriptAction);
    _interactionsPanePopupMenu.add(_saveHistoryAction);
    _interactionsPanePopupMenu.add(_clearHistoryAction);
    _interactionsPanePopupMenu.addSeparator();
    _interactionsPanePopupMenu.add(_resetInteractionsAction);
    _interactionsPanePopupMenu.add(_viewInteractionsClasspathAction);
    _interactionsPanePopupMenu.add(_copyInteractionToDefinitionsAction);
    _interactionsPane.addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        _interactionsPane.requestFocus();
        _interactionsPanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });

    _consolePanePopupMenu = new JPopupMenu();
    _consolePanePopupMenu.add(_clearConsoleAction);
    _consolePane.addMouseListener(new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        _consolePane.requestFocus();
        _consolePanePopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }

  /**
   * Create a new DefinitionsPane and JScrollPane for an open
   * definitions document.
   *
   * @param doc The open definitions document to wrap
   * @return JScrollPane containing a DefinitionsPane for the
   *         given document.
   */
  JScrollPane _createDefScrollPane(OpenDefinitionsDocument doc) {
    // made this package private to allow testing of disabling editing
    // during compile and successful switching on and off of ability to
    // edit
    DefinitionsPane pane = new DefinitionsPane(this, doc);

    // Add listeners
    _installNewDocumentListener(doc);
    ErrorCaretListener caretListener = new ErrorCaretListener(doc, pane, this);
    pane.addErrorCaretListener(caretListener);

    // add a listener to update line and column.
    pane.addCaretListener(_posListener);

    // add a focus listener to the pane.
    pane.addFocusListener(new LastFocusListener());

    // Add to a scroll pane
    JScrollPane scroll = new BorderlessScrollPane(pane,
                                                  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setScrollPane(scroll);
    //scroll.setBorder(null); // removes all default borders (MacOS X installs default borders)

    // can be used to make sure line wrapping occurs
    /*scroll.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        pane.setSize(scroll.getViewport().getWidth(), pane.getHeight());
      }
    });*/

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
    if (_model.getDebugger().isAvailable()) {
      try {
        _debugPanel = new DebugPanel(this);

        // Set the panel's size.
        int debugHeight =
          DrJava.getConfig().getSetting(DEBUG_PANEL_HEIGHT).intValue();
        Dimension debugMinSize = _debugPanel.getMinimumSize();

        // TODO: check bounds compared to entire window.
        if ((debugHeight > debugMinSize.height)) {
          debugMinSize.height = debugHeight;
        }
        _debugPanel.setPreferredSize(debugMinSize);
      }
      catch(NoClassDefFoundError e) {
        // Don't use the debugger
        _debugPanel = null;
      }
//      catch (DebugException de) {
//        // Show the error
//        _showDebugError(de);
//        _debugPanel = null;
//      }
    } else {
      _debugPanel = null;
    }

    // Overall layout
    _docSplitPane = new BorderlessSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            true,
                                            new JScrollPane(_model.getDocumentNavigator().asContainer()),
                                            defScroll);
    _debugSplitPane = new BorderlessSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    _debugSplitPane.setBottomComponent(_debugPanel);
    _mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                true,
                                _docSplitPane,
                                _tabbedPane);
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

  /**
   * Switch to the JScrollPane containing the DefinitionsPane
   * for the current active document.
   */
  void _switchDefScrollPane() {

    // demoted to package private protection to test the disabling editing while
    // compiling functionality.

    // Added 2004-May-27
    // Notify the definitions pane that is being replaced (becoming inactive)
    _currentDefPane.notifyInactive();
    
    JScrollPane scroll = _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null) {
      throw new UnexpectedException(new Exception("Current definitions scroll pane not found."));
    }

    int oldLocation = _docSplitPane.getDividerLocation();
    
    _docSplitPane.setRightComponent(scroll);
    _docSplitPane.setDividerLocation(oldLocation);

    // if the current def pane is uneditable, that means
    // we arrived here from a compile with errors.  We're
    // guaranteed to make it editable again when we
    // return from the compilation, so we take the state
    // with us.  We guarantee only one definitions pane
    // is un-editable at any time.
    if ( _currentDefPane.isEditable() ){
      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
      _currentDefPane.notifyActive();
    }
    else {
      try{
        _currentDefPane.setEditable(true);
      }catch(NoSuchDocumentException e){
        // it's ok.
      }
      _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
      _currentDefPane.notifyActive();
      _currentDefPane.setEditable(false);
    }
    // reset the undo/redo menu items
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());

    if(inDebugMode()) {
      _updateDebugStatus();
    }
  }

  /**
   * Addresses the Mac OS X bug where the scrollbars are disabled in
   * one document after opening another document.
   */
  private void _reenableScrollBar() {
    JScrollPane scroll = _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null) {
      throw new UnexpectedException(new Exception("Current definitions scroll pane not found."));
      
    }

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

  /**
   * Returns a JRadioButtonMenuItem that looks like a JCheckBoxMenuItem.
   * This is a workaround for a known bug on OS X's version of Java.
   * (See http://developer.apple.com/qa/qa2001/qa1154.html)
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

  /**
   * Gets the absolute file, or if necessary, the canonical file.
   * @param f the file for which to get the full path
   * @return the file representing the full path to the given file
   */
  private File _getFullFile(File f) throws IOException {
      if (PlatformFactory.ONLY.isWindowsPlatform() &&
          ((f.getAbsolutePath().indexOf("..") != -1) ||
           (f.getAbsolutePath().indexOf("./") != -1) ||
           (f.getAbsolutePath().indexOf(".\\") != -1))) {
        return f.getCanonicalFile();
      }
      else {
        return f.getAbsoluteFile();
      }
  }

  /**
   * Sets the current directory to be that of the given file.
   */
  private void _setCurrentDirectory(File file) {
    // We want to use absolute paths whenever possible, since canonical paths
    //  resolve symbolic links and can be quite long and unintuitive.
    // However, Windows blows up if you set the current directory of a
    //  JFileChooser to an absolute path with ".." in it.
    // In that case, we'll use the canonical path for the file chooser.
    // (Fix for bug 707734)
    // Extended this to fix "./" and ".\" also (bug 774896)
    try {
      /*File f = file.getAbsoluteFile();
      if (PlatformFactory.ONLY.isWindowsPlatform() &&
          ((file.getAbsolutePath().indexOf("..") != -1) ||
           (file.getAbsolutePath().indexOf("./") != -1) ||
           (file.getAbsolutePath().indexOf(".\\") != -1))) {
        f = file.getCanonicalFile();
      }*/
      file = _getFullFile(file);
      _openChooser.setCurrentDirectory(file);
      _saveChooser.setCurrentDirectory(file);
    }
    catch (IOException ioe) {
      // If getCanonicalFile throws an IOException, we can't
      //  set the directory of the file chooser.  Oh well.
    }
  }
  /**
   * Sets the current directory to be that of document's file.
   */
  private void _setCurrentDirectory(OpenDefinitionsDocument doc) {
    try {
      File file = doc.getFile();
      _setCurrentDirectory(file);
    }
    catch (IllegalStateException ise) {
      // no file, leave in current directory
    }
    catch (FileMovedException fme) {
      // file was deleted, but try to go the directory
      _setCurrentDirectory(fme.getFile());
    }
  }

  /**
   * Sets the font of all panes and panels to the main font
//   * @param f is a Font object
   */
  private void _setMainFont() {

    Font f = DrJava.getConfig().getSetting(FONT_MAIN);

    Iterator scrollPanes = _defScrollPanes.values().iterator();
    
    int i=0;
    while (scrollPanes.hasNext()) {
      i++;
      JScrollPane scroll = (JScrollPane) scrollPanes.next();
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        pane.setFont(f);
        // Update the font of the line enumeration rule
        if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
          scroll.setRowHeaderView( new LineEnumRule( pane) );
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

  /**
   * Updates the text color for the doc list.
   */
  private void _updateNormalColor() {
    // Get the new value.
    Color norm = DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR);

    // Change the text (foreground) color for the doc list.
    _model.getDocCollectionWidget().setForeground(norm);

    // We also need to immediately repaint the foremost scroll pane.
    _repaintLineNums();
  }

  /**
   * Updates the background color for the doc list.
   */
  private void _updateBackgroundColor() {
    // Get the new value.
    Color back = DrJava.getConfig().getSetting(DEFINITIONS_BACKGROUND_COLOR);

    // Change the background color for the doc list.
    _model.getDocCollectionWidget().setBackground(back);

    // We also need to immediately repaint the foremost scroll pane.
    _repaintLineNums();
  }

  /**
   * Updates the font and colors of the line number display.
   */
  private void _updateLineNums() {
    if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
      Iterator<JScrollPane> it = _defScrollPanes.values().iterator();

      // Iterate over all definitions scroll panes.
      while (it.hasNext()) {
        // Update the font for all line number displays.
        JScrollPane spane = it.next();

        LineEnumRule ler = (LineEnumRule) spane.getRowHeader().getView();
        ler.updateFont();
        ler.revalidate();
      }

      // We also need to immediately repaint the foremost scroll pane.
      _repaintLineNums();
    }
  }

  /**
   * Repaints the line numbers on the active scroll pane.
   */
  private void _repaintLineNums() {
    JScrollPane front = _defScrollPanes.get(_model.getActiveDocument());
    JViewport rhvport = front.getRowHeader();

    if (rhvport != null) {
      Component view = rhvport.getView();
      view.repaint();
    }
  }

  /**
   *  Update the row header (line number enumeration) for the definitions scroll pane
   */
  private void _updateDefScrollRowHeader() {
    boolean ruleEnabled = DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue();

    Iterator scrollPanes = _defScrollPanes.values().iterator();
    while (scrollPanes.hasNext()) {
      JScrollPane scroll = (JScrollPane) scrollPanes.next();
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        if (scroll.getRowHeader() == null || scroll.getRowHeader().getView() == null) {
          if (ruleEnabled) {
            scroll.setRowHeaderView(new LineEnumRule(pane));
          }
        }
        else {
          if (!ruleEnabled) {
            scroll.setRowHeaderView(null);
          }
        }
      }
    }
  }

  /**
   * Removes the current highlight
   */
  private void _removeThreadLocationHighlight() {
    if (_currentThreadLocationHighlight != null) {
      _currentThreadLocationHighlight.remove();
    }
    _currentThreadLocationHighlight = null;
  }

  /**
   * Disable any step timer
   */
  private void _disableStepTimer() {
    synchronized (_debugStepTimer) {
      if (_debugStepTimer.isRunning()) {
        _debugStepTimer.stop();
      }
    }
  }

  /**
   * Checks if debugPanel's status bar displays the DEBUGGER_OUT_OF_SYNC message
   * but the current document is in sync.  Clears the debugPanel's status bar in
   * this case.
   */
  private void _updateDebugStatus() {
    if (!inDebugMode()) return;

    // if the document is untitled, don't show that it is out of sync since it
    // can't be debugged anyway
    if (_model.getActiveDocument().isUntitled() ||
        _model.getActiveDocument().getClassFileInSync())
    {
      // Hide message
      if (_debugPanel.getStatusText().equals(DEBUGGER_OUT_OF_SYNC)) {
        _debugPanel.setStatusText("");
      }
    }
    else {
      // Show message
      if(_debugPanel.getStatusText().equals(""))
        _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
    }
  }

  /**
   * Ensures that the interactions pane is not editable during an interaction.
   */
  protected void _disableInteractionsPane() {
    // Only change GUI from event-dispatching thread
    Runnable doCommand = new Runnable() {
      public void run() {
        _interactionsPane.setEditable(false);
        _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (_interactionsScriptController != null) {
          _interactionsScriptController.setActionsDisabled();
        }
      }
    };
    SwingUtilities.invokeLater(doCommand);
  }

  /**
   * Ensures that the interactions pane is editable after an interaction completes.
   */
  protected void _enableInteractionsPane() {
    // Only change GUI from event-dispatching thread
    Runnable doCommand = new Runnable() {
      public void run() {
        /*
         if (inDebugMode()) {
         _disableStepTimer();
         Debugger manager = _model.getDebugger();
         manager.clearCurrentStepRequest();
         _removeThreadLocationHighlight();
         }
         */

        _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        _interactionsPane.setEditable(true);
        _interactionsController.moveToEnd();
        if (_interactionsPane.hasFocus()) {
          _interactionsPane.getCaret().setVisible(true);
        }
        if (_interactionsScriptController != null) {
          _interactionsScriptController.setActionsEnabled();
        }
      }
    };
    SwingUtilities.invokeLater(doCommand);
  }


  /**
   * Blocks access to DrJava while the hourglass cursor is on
   */
  private class GlassPane extends JComponent {

    /**
     * Creates a new GlassPane over the DrJava window
     */
    public GlassPane() {
      addKeyListener(new KeyAdapter() {});
      addMouseListener(new MouseAdapter() {});
      super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
  }

  /**
   * Listens to events from the debugger.
   */
  private class UIDebugListener implements DebugListener {

    // This field is used by threadLocationUpdated. We want to
    // call centerViewOnLine the second time setSize is called
    // on _currentDefPane if it is a new definitions pane. This
    // actually centers the correct line instead of having it
    // appear at the top of the screen. There ought to be a
    // cleaner way to do this...
    private boolean _firstCallFromSetSize;

    public void debuggerStarted() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          showDebugger(); 
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void debuggerShutdown() {
      _disableStepTimer();

      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          hideDebugger();
          _removeThreadLocationHighlight();

          // Ensure all doc breakpoints are gone
          List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
          for (int i=0; i < docs.size(); i++) {
            docs.get(i).removeFromDebugger();
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void currThreadSet(DebugThreadData dtd) {
    }

    public void threadLocationUpdated(final OpenDefinitionsDocument doc,
                                      final int lineNumber,
                                      final boolean shouldHighlight) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          // This listener is used when the document to display is
          // not the active document. In this case, when setActiveDocument
          // is called, the document won't yet have positive size and we
          // don't want to scroll to a line until it does, so we wait
          // for a call to setSize.
          _firstCallFromSetSize = true;
          ActionListener setSizeListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
//              if (_firstCallFromSetSize) {
//                _firstCallFromSetSize = false;
//              }
//              else {
                _currentDefPane.centerViewOnLine(lineNumber);
//              }
            }
          };
          _currentDefPane.addSetSizeListener(setSizeListener);

          if (!_model.getActiveDocument().equals(doc)) {
            _model.setActiveDocument(doc);
          }

          // this block occurs if the documents is already open and as such
          // has a positive size
          if (_currentDefPane.getSize().getWidth() > 0 &&
              _currentDefPane.getSize().getHeight() > 0) {
            _currentDefPane.centerViewOnLine(lineNumber);
            _currentDefPane.requestFocus();
          }

          if (shouldHighlight) {
            _removeThreadLocationHighlight();
            int startOffset = doc.getOffset(lineNumber);
            if (startOffset > -1) {
              int endOffset = doc.getLineEndPos(startOffset);
              if (endOffset > -1) {
                _currentThreadLocationHighlight =
                  _currentDefPane.getHighlightManager().addHighlight(startOffset, endOffset,
                                                                     DefinitionsPane.THREAD_PAINTER);
              }
            }
          }

          if (doc.isModifiedSinceSave() &&
              !_currentDefPane.hasWarnedAboutModified()) {

            _showDebuggingModifiedFileWarning();

            //no need to update flag, because previous method call will do it
            //_hasWarnedAboutModified = true;
          }
          if (shouldHighlight) {
            // Give the interactions pane focus so we can debug
            _interactionsPane.requestFocus();
          }
          showTab(_interactionsPane);
          _updateDebugStatus();

        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void breakpointSet(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          JScrollPane scroll = _defScrollPanes.get(bp.getDocument());
          if (scroll == null) {
            throw new UnexpectedException(new Exception("Breakpoint set in a closed document."));
          }
          DefinitionsPane bpPane = (DefinitionsPane) scroll.getViewport().getView();
          _breakpointHighlights.put(bp,
                                    bpPane.getHighlightManager().addHighlight(bp.getStartOffset(),
                                                                              bp.getEndOffset(),
                                                                              DefinitionsPane.BREAKPOINT_PAINTER));
          _updateDebugStatus();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void breakpointReached(Breakpoint bp) {
    }

    public void breakpointRemoved(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      /*Runnable doCommand = new Runnable() {
        public void run() {
          _model.setActiveDocument(bp.getDocument());
          _currentDefPane.getHighlightManager().removeHighlight(bp.getStartOffset(),
                                                                bp.getEndOffset(),
                                                                DefinitionsPane.BREAKPOINT_PAINTER);
        }
      };
      SwingUtilities.invokeLater(doCommand);*/

      HighlightManager.HighlightInfo highlight = _breakpointHighlights.get(bp);
      if (highlight != null) highlight.remove();
      _breakpointHighlights.remove(bp);
    }

    /**
     * Called when a step is requested on the current thread.
     */
    public void stepRequested() {
      // Print a message if step takes a long time
      synchronized (_debugStepTimer) {
        if (!_debugStepTimer.isRunning()) {
          _debugStepTimer.start();
        }
      }
    }

    public void currThreadSuspended() {
      _disableStepTimer();

      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          _setThreadDependentDebugMenuItems(true);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void currThreadResumed() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          _setThreadDependentDebugMenuItems(false);
          _removeThreadLocationHighlight();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void threadStarted() {
    }

    public void currThreadDied() {
      _disableStepTimer();

      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          if (inDebugMode()) {
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
              _showError(de, "Debugger Error",
                         "Error with a thread in the debugger.");
            }
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void nonCurrThreadDied() {
    }
  }

  /**
   * Inner class to listen to all events in the model.
   */
  private class ModelListener implements SingleDisplayModelListener {
   
    public void fileNotFound(File f){
      _showFileNotFoundError(new FileNotFoundException("File " + f + " cannot be found"));
    }
    
    public void newFileCreated(OpenDefinitionsDocument doc) {
      _createDefScrollPane(doc);
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      _saveAction.setEnabled(false);
      _revertAction.setEnabled(true);
      updateFileTitle();
      _currentDefPane.requestFocus();
      try {
        File f = doc.getFile();
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
      catch (IllegalStateException ise) {
        // Impossible: saved => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        File f = fme.getFile();
        // Recover, show it in the list anyway
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
      // Check class file sync status, in case file was renamed
      if (inDebugMode()) _updateDebugStatus();
    }

    // NOTE: Not necessarily called from event-dispatching thread...
    //  Should figure out how to deal with invokeLater here.
    public void fileOpened(final OpenDefinitionsDocument doc) {
      if ( !SwingUtilities.isEventDispatchThread() && !inDebugMode() ) {
        // Can't invokeAndWait while in debug mode:
        //  UI thread might not respond, so DrJava locks up
        try {
          Runnable command = new Runnable() {
            public void run(){
              _fileOpened(doc);
            }
          };
          SwingUtilities.invokeAndWait(command);
        }
        catch(InterruptedException ex) {
          /** we don't expect to be interrupted */
          throw new UnexpectedException(ex);
        }
        catch(InvocationTargetException ex2) {
          /** we don't expect _fileOpened() to throw any exceptions */
          throw new UnexpectedException(ex2);
        }
      }
      else {
        _fileOpened(doc);
      }
      
      try {
        File f = doc.getFile();
        if(! _model.isProjectFile(f) && _model.isInProjectPath(doc)) {
//          _saveProjectAction.setEnabled(true);
          _model.setProjectChanged(true);
        }
      }
      catch(FileMovedException fme) {
        //do nothing
      }
    }
    private void _fileOpened(final OpenDefinitionsDocument doc){
      // Fix OS X scrollbar bug before switching
      _reenableScrollBar();
      _createDefScrollPane(doc);
      try {
        File f = doc.getFile();
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
      catch (IllegalStateException ise) {
        // Impossible: opened => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        File f = fme.getFile();
        // Recover, show it in the list anyway
        if(! _model.isProjectFile(f))
          _recentFileManager.updateOpenFiles(f);
      }
    }

    /**
     * NOTE: Makes certain that this action occurs in the event dispatching
     * thread
     */
    public void fileClosed(final OpenDefinitionsDocument doc) {
      if ( !SwingUtilities.isEventDispatchThread() && !inDebugMode() ) {
        // Can't invokeAndWait while in debug mode:
        //  UI thread might not respond, so DrJava locks up
        try {
          Runnable command = new Runnable() {
            public void run(){
              _fileClosed(doc);
//              System.out.println("gui closed file 1");
            }
          };
          SwingUtilities.invokeAndWait(command);
        }
        catch(InterruptedException ex) {
          /** we don't expect to be interrupted */
          throw new UnexpectedException(ex);
        }
        catch(InvocationTargetException ex2) {
          /** we don't expect _fileOpened() to throw any exceptions */
          throw new UnexpectedException(ex2);
        }
      }
      else {
        _fileClosed(doc);
//        System.out.println("gui closed file 2");
      }
      if(doc != null) {
        try {
          File f = doc.getFile();
          if(_model.isProjectFile(f) || doc.isAuxiliaryFile()) {
//            _saveProjectAction.setEnabled(true);
            _model.setProjectChanged(true);
          }
        }
        catch(FileMovedException fme) {
          //do nothing
        }
        catch(IllegalStateException ise) {
          //thrown if the document does not yet have a file
        }
      }
    }
  

    /** Does the work of closing a file */
    private void _fileClosed(OpenDefinitionsDocument doc){
      _removeErrorListener(doc);
      ((DefinitionsPane)_defScrollPanes.get(doc).getViewport().getView()).close();
      _defScrollPanes.remove(doc);
    }

    public void fileReverted(OpenDefinitionsDocument doc) {
      updateFileTitle();
      _saveAction.setEnabled(false);
      _currentDefPane.resetUndo();
      _currentDefPane.hasWarnedAboutModified(false);
      _currentDefPane.setPositionAndScroll(0);
      if (inDebugMode()) _updateDebugStatus();
    }

    public void undoableEditHappened() {
      _currentDefPane.getUndoAction().updateUndoState();
      _currentDefPane.getRedoAction().updateRedoState();
    }

    // NOTE: Not necessarily called from event-dispatching thread...
    //  Should figure out how to deal with invokeLater here.
    public void activeDocumentChanged(final OpenDefinitionsDocument active) {
      // Only change GUI from event-dispatching thread
      // (This can be called from other threads...)
      //Runnable doCommand = new Runnable() {
      // public void run() {
      Runnable command = new Runnable() {
        public void run(){

          _switchDefScrollPane();

          boolean isModified = active.isModifiedSinceSave();
          boolean canCompile = (!isModified && !active.isUntitled());
          _saveAction.setEnabled(!canCompile);
          _revertAction.setEnabled(!active.isUntitled());

          // Update error highlights
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);

          // Update FileChoosers' directory
          _setCurrentDirectory(active);

          // Update title and position
          updateFileTitle();
          _currentDefPane.requestFocus();
          _posListener.updateLocation();

          // Check if modified (but only if we're not closing all files)
          if (!_model.isClosingAllFiles()) {
            try {
              active.revertIfModifiedOnDisk();
            }
            catch (FileMovedException fme) {
              _showFileMovedError(fme);
            }
            catch (IOException e) {
              _showIOError(e);
            }
          }

          // Change Find/Replace to the new defpane
          if (_findReplace.isDisplayed()) {
            _findReplace.stopListening();
            _findReplace.beginListeningTo(_currentDefPane);
            //uninstallFindReplaceDialog(_findReplace);
            //installFindReplaceDialog(_findReplace);
          }
          //  }
          //};
          //SwingUtilities.invokeLater(doCommand);
        }
      };
      if ( !SwingUtilities.isEventDispatchThread() && !inDebugMode() ) {
        // Can't invokeAndWait while in debug mode:
        //  UI thread might not respond, so DrJava locks up) {
        try {
          SwingUtilities.invokeAndWait(command);
        }
        catch(InterruptedException e) {
          /** we don't expect to be interrupted */
          throw new UnexpectedException(e);
        }
        catch(InvocationTargetException e2) {
          /** we don't expect _fileOpened() to throw any exceptions */
          throw new UnexpectedException(e2.getTargetException());
        }
      }
      else {
        command.run();
      }
    }

    public void interactionStarted() {
      _disableInteractionsPane();
      _runAction.setEnabled(false);
    }

    public void interactionEnded() {
      _enableInteractionsPane();
      _runAction.setEnabled(true);
    }

    public void interactionErrorOccurred(int offset, int length){
      _interactionsPane.highlightError(offset, length);
    }

    /**
     * Called when the active interpreter is changed.
     * @param inProgress Whether the new interpreter is currently in progress
     * with an interaction (ie. whether an interactionEnded event will be fired)
     */
    public void interpreterChanged(boolean inProgress) {
      _runAction.setEnabled(!inProgress);
      if (inProgress) {
        _disableInteractionsPane();
      }
      else {
        _enableInteractionsPane();
      }
    }

    public void compileStarted() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          // Is this necessary?
          //CompilerErrorListPane elp = _compilerErrorPanel.getErrorListPane();
          //elp.setSize(_tabbedPane.getMinimumSize());
          //_setDividerLocation();

          hourglassOn();
          showTab(_compilerErrorPanel);
          _compilerErrorPanel.setCompilationInProgress();
          _saveAction.setEnabled(false);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void runStarted(final OpenDefinitionsDocument doc) {
      // Only change GUI from event-dispatching thread
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // Make sure that this document is the active one.
          _model.setActiveDocument(doc);

          // Switch to the interactions pane to show results.
          showTab(_interactionsPane);
        }
      });
    }

    public void compileEnded() {
      // Only change GUI from event-dispatching thread
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _compilerErrorPanel.reset();
          if (inDebugMode()) {
            _model.getActiveDocument().checkIfClassFileInSync();
            _updateDebugStatus();
          }
          hourglassOff();
        }

      });
    }

    public void junitStarted(final List<OpenDefinitionsDocument> docs) {
      // Only change GUI from event-dispatching thread
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.this.hourglassOn();
          showTab(_junitErrorPanel);
          _junitErrorPanel.setJUnitInProgress(docs);
          _junitAction.setEnabled(false);
          _junitAllAction.setEnabled(false);
        }
      });
    }

    //public void junitRunning() {}

    public void junitSuiteStarted(final int numTests) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _junitErrorPanel.progressReset(numTests);
        }
      });
    }

    public void junitTestStarted(final String name) {
      _junitErrorPanel.getErrorListPane().testStarted(name); // this does nothing!
    }

    public void junitTestEnded(final String name, final boolean wasSuccessful,
                               final boolean causedError) {
      // syncUI...?
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _junitErrorPanel.getErrorListPane().
            testEnded(name, wasSuccessful, causedError); // this does nothing!
          _junitErrorPanel.progressStep(wasSuccessful);
        }
      });
    }

    public void junitEnded() {
      // Only change GUI from event-dispatching thread
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          MainFrame.this.hourglassOff();
          showTab(_junitErrorPanel);
          _junitAction.setEnabled(true);
          _junitAllAction.setEnabled(true);
          _junitErrorPanel.reset();
        }
      });
    }

    public void javadocStarted() {

      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          // if we don't lock edits, our error highlighting might break
          MainFrame.this.hourglassOn();

          showTab(_javadocErrorPanel);
          _javadocErrorPanel.setJavadocInProgress();
          _javadocAllAction.setEnabled(false);
          _javadocCurrentAction.setEnabled(false);
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void javadocEnded(final boolean success, final File destDir,
                             final boolean allDocs) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          MainFrame.this.hourglassOff();

          showTab(_javadocErrorPanel);
          _javadocAllAction.setEnabled(true);
          _javadocCurrentAction.setEnabled(true);
          _javadocErrorPanel.reset();

          // Display the results.
//             System.out.println("did we get this far?");
          if (success) {
            String className;
            try {
              className =
                _model.getActiveDocument().getQualifiedClassName();
              className = className.replace('.', File.separatorChar);
            }
            catch (ClassNameNotFoundException cnf) {
              // If there is no class name, pass the empty string as a flag.
              // We don't want to blow up here.
              className = "";
            }
            try {
              String filename = (allDocs || className.equals("")) ?
                "index.html" : (className + ".html");
              File index = new File(destDir, filename);
              URL address = index.getAbsoluteFile().toURL();
              if (!PlatformFactory.ONLY.openURL(address)) {
                JavadocFrame _javadocFrame =
                  new JavadocFrame(destDir, className, allDocs);
                _javadocFrame.setVisible(true);
              }
            }
            catch (MalformedURLException me) {
              throw new UnexpectedException(me);
            }
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
      SwingUtilities.invokeLater(doCommand);
    }

    public void interpreterExited(final int status) {
      // Only show prompt if option is set
      if (DrJava.getConfig().getSetting(INTERACTIONS_EXIT_PROMPT).booleanValue()) {
        // Show the dialog in a Swing thread, so Interactions can
        // start resetting right away.
        Runnable doCommand = new Runnable() {
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
        };
        SwingUtilities.invokeLater(doCommand);
      }
    }

    public void interpreterResetFailed(Throwable t) {
      interpreterReady();
    }

    public void interpreterResetting() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          Debugger dm = _model.getDebugger();
          if (dm.isAvailable() && dm.isReady()) {
            dm.shutdown();
          }
          _resetInteractionsAction.setEnabled(false);
          _junitAction.setEnabled(false);
          _junitAllAction.setEnabled(false);
          _runAction.setEnabled(false);
          _closeInteractionsScript();
          _interactionsPane.setEditable(false);
          _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          if (_model.getDebugger().isAvailable()) {
            _toggleDebuggerAction.setEnabled(false);
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void interpreterReady() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          interactionEnded();
          _runAction.setEnabled(true);
          _junitAction.setEnabled(true);
          _junitAllAction.setEnabled(true);
          _resetInteractionsAction.setEnabled(true);
          if (_model.getDebugger().isAvailable()) {
            _toggleDebuggerAction.setEnabled(true);
          }
          // Moved this line here from interpreterResetting since
          // it was possible to get an InputBox in InteractionsController
          // between interpreterResetting and interpreterReady.
          // Fixes bug #917054 "Interactions Reset Bug".
          _interactionsController.notifyInputEnteredAction();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void consoleReset() {
    }

    public void saveBeforeCompile() {
      _saveAllBeforeProceeding
        ("To compile, you must first save ALL modified files.\n" +
         "Would you like to save and then compile?",
         ALWAYS_SAVE_BEFORE_COMPILE,
         "Always save before compiling");
    }

    /**
     * Prompts the user to save and compile before running a modified file.
     *
     * Not currently used.
    public void saveBeforeRun() {
      _saveAllBeforeProceeding
        ("To run this document's main method, you must first\n" +
         "save ALL modified files and compile this document.\n" +
         "Would you like to save and compile now?",
         ALWAYS_SAVE_BEFORE_RUN,
         "Always save and compile before running");
    }*/

    /**
     * Not currently used.
    public void saveBeforeJUnit() {
      _saveAllBeforeProceeding
        ("To run JUnit, you must first save and compile ALL modified\n" +
         "files. Would you like to save and compile now?",
         ALWAYS_SAVE_BEFORE_JUNIT,
         "Always save and compile before testing with JUnit");
    }*/

    public void saveBeforeJavadoc() {
      _saveAllBeforeProceeding
        ("To run Javadoc, you must first save ALL modified files.\n" +
         "Would you like to save and then run Javadoc?",
         ALWAYS_SAVE_BEFORE_JAVADOC,
         "Always save before running Javadoc");
    }

    /**
     * Not currently used.
    public void saveBeforeDebug() {
      _saveAllBeforeProceeding
        ("To use debugging commands, you must first save and compile\n" +
         "ALL modified files. Would you like to save and then compile?",
         ALWAYS_SAVE_BEFORE_DEBUG,
         "Always save and compile before debugging");
    }*/

    /**
     * Helper method shared by all "saveBeforeX" methods.
     * @param message a prompt message to be displayed to the user
     * @param option the BooleanOption for the prompt dialog checkbox
     * @param checkMsg the description of the checkbox ("Always save before X")
     */
    private void _saveAllBeforeProceeding(String message, BooleanOption option,
                                          String checkMsg) {
      if (_model.hasModifiedDocuments()) {
        if (!DrJava.getConfig().getSetting(option).booleanValue()) {
          ConfirmCheckBoxDialog dialog =
            new ConfirmCheckBoxDialog(MainFrame.this,
                                      "Must Save All Files to Continue",
                                      message,
                                      checkMsg);
          int rc = dialog.show();
          switch (rc) {
            case JOptionPane.YES_OPTION:
              _saveAll();
              // Only remember checkbox if they say yes
              if (dialog.getCheckBoxValue()) {
                DrJava.getConfig().setSetting(option, Boolean.TRUE);
              }
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
        else {
          _saveAll();
        }
      }
    }

    public void filePathContainsPound() {
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

    public void nonTestCase(boolean isTestAll) {

      String message = isTestAll ?
        "There are no open JUnit test cases.  Please make sure that:\n" +
        "  - The documents containing tests have been compiled.\n" +
        "  - They are subclasses of junit.framework.TestCase.\n" +
        "For more information on writing JUnit TestCases, view the\n" +
        "JUnit chapter in the User Documentation." :
        "The current document is not a valid JUnit test case.\n" +
        "Please make sure that:\n" +
        "  - This document has been compiled.\n" +
        "  - It is a subclass of junit.framework.TestCase.\n" +
        "For more information on writing JUnit TestCases, view the\n" +
        "JUnit chapter in the User Documentation.";
//        "There are no open test cases.  Please make sure all\n" +
//        "open documents have been compiled, and at least one\n" +
//        "of them is a subclass of junit.framework.TestCase." :
//
//        "The  Test  button  (and menu item) in  DrJava invokes the JUnit\n"  +
//        "test  harness  over  the currently open document.  In order for\n" +
//        "that  to  work,  the  current  document  must  be a valid JUnit\n" +
//        "TestCase, i.e., a subclass of junit.framework.TestCase.\n\n" +
//
//        "Make  sure  the current  document  has been saved and  compiled\n" +
//        "before using the Test button.\n\n" +
//
//        "For information on how to write JUnit TestCases, view the JUnit\n" +
//        "chapter in the User Documentation or the online Help, or visit:\n\n" +
//
//        "  http://www.junit.org/\n\n";

      JOptionPane.showMessageDialog(MainFrame.this, message,
                                    "Test Works Only On JUnit TestCases",
                                    JOptionPane.ERROR_MESSAGE);


    }

    /**
     * Check if the current document has been modified. If it has, ask the user
     * if he would like to save or not, and save the document if yes. Also
     * give the user a "cancel" option to cancel doing the operation that got
     * us here in the first place.
     *
     * @return A boolean, if true means the user is OK with the file being saved
     *         or not as they chose. If false, the user wishes to cancel.
     */
    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      String fname;

      _model.setActiveDocument(doc);

      try {
        File file = doc.getFile();
        fname = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file exists
        fname = "Untitled file";
      }
      catch (FileMovedException fme) {
        // File was deleted, but use the same name anyway
        fname = fme.getFile().getName();
      }

      String text = fname + " has been modified. Would you like to save it?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             "Save " + fname + "?",
                                             JOptionPane.YES_NO_CANCEL_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          return _save();
        case JOptionPane.NO_OPTION:
          return true;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }
    }

    /**
     * Called to ask the listener if it is OK to revert the current
     * document to a newer version saved on file.
     */
    public boolean shouldRevertFile(OpenDefinitionsDocument doc) {

      String fname;

      if (! _model.getActiveDocument().equals(doc)) {
        _model.setActiveDocument(doc);
      }

      try {
        File file = doc.getFile();
        fname = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file exists
        fname = "Untitled file";
      }
      catch (FileMovedException fme) {
        // File was deleted, but use the same name anyway
        fname = fme.getFile().getName();
      }

      String text = fname + " has changed on disk. Would you like to " +
      "reload it?\nThis will discard any changes you have made.";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             fname + " Modified on Disk",
                                             JOptionPane.YES_NO_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          return true;
        case JOptionPane.NO_OPTION:
          return false;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }
    }

    public void interactionIncomplete() {
    }
    
    /* changes to the state */
    
    public void projectBuildDirChanged(){
    }
    
    public void projectModified(){
      _saveProjectAction.setEnabled(_model.isProjectChanged());
    }
    
    public void projectRunnableChanged(){
      if(_model.getMainClass() != null && _model.getMainClass().exists()){
        _runProjectAction.setEnabled(true);
      }else{
        _runProjectAction.setEnabled(false);
      }
    }
    
    public void documentNotFound(OpenDefinitionsDocument d, File f) {
           
      String text = "File " + f.getAbsolutePath() +
        "\ncould not be found on disk!  It was probably moved\n" +
        "or deleted.  Would you like to try to find it?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             "File Moved or Deleted",
                                             JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.YES_OPTION) {
        try {
          File[] opened = _openSelector.getFiles(); 
          d.setFile(opened[0]);
        } catch(OperationCanceledException oce) {
          //If canceled, prompt the user again
          documentNotFound(d,f);
          return;
        }
      }
      else{
        //Close the file that wasn't found
        LinkedList<OpenDefinitionsDocument> l = new LinkedList<OpenDefinitionsDocument>();
        l.add(d);
        _model.closeFiles(l);
        throw new DocumentClosedException(d,"Document in " + f + "closed unexpectedly");
      }
    }
  }

  public JViewport getDefViewport() {
    JScrollPane defScroll = _defScrollPanes.get(_model.getActiveDocument());
    return defScroll.getViewport();
  }

  public void removeTab(Component c) {
    _tabbedPane.remove(c);
    ((TabbedPanel)c).setDisplayed(false);
    _tabbedPane.setSelectedIndex(0);
    _currentDefPane.requestFocus();
  }

  /**
   * Shows the components passed in in the appropriate place in the tabbedPane depending on the position of
   * the component in the _tabs list.
   * @param c the component to show in the tabbedPane
   */
  public void showTab(Component c) {
    int numVisible = 0;
    TabbedPanel tp;

    // This retarded method doesn't work for our two always-on tabs,
    // so here's a temporary kludge.
    if (c == _interactionsPane) {
      _tabbedPane.setSelectedIndex(0);
    }
    else if (c == _consolePane) {
      _tabbedPane.setSelectedIndex(1);
    }
    else {
      for (int i = 0; i < _tabs.size(); i++) {
        tp = _tabs.get(i);
        if (tp == c) {
          // 2 right now is a magic number for the number of tabs always visible
          // interactions & console
          if (!tp.isDisplayed()) {
            _tabbedPane.insertTab(tp.getName(), null, tp, null, numVisible + 2);
            tp.setDisplayed(true);
          }
          _tabbedPane.setSelectedIndex(numVisible + 2);
          c.requestFocus();
          return;
        }
        if (tp.isDisplayed())
          numVisible++;
      }
    }
  }

  /**
   * Sets the location of the main divider.
   * (not currently used)
  private void _setDividerLocation() {
    int divLocation = _mainSplit.getHeight() -
      _mainSplit.getDividerSize() -
      (int)_tabbedPane.getMinimumSize().getHeight();
    if (_mainSplit.getDividerLocation() > divLocation)
      _mainSplit.setDividerLocation(divLocation);
  }*/

  /**
   * Warns the user that the current file is open and cannot be modified.
   */
  private void _warnFileOpen() {
    // If we'd like to change to an error message for this, instead
    // of a warning, change both incidents of WARNING to ERROR.
    JOptionPane.showMessageDialog(MainFrame.this,
                                  "This file is open in DrJava.  You may not overwrite it.",
                                  "File Open Warning", JOptionPane.WARNING_MESSAGE);
  }

  /**
   * Confirms with the user that the file should be overwritten.
   * @return <code>true</code> iff the user accepts overwriting.
   */
  private boolean _verifyOverwrite() {
    Object[] options = {"Yes","No"};
    int n = JOptionPane.showOptionDialog
      (MainFrame.this,
       "This file already exists.  Do you wish to overwrite the file?",
       "Confirm Overwrite",
       JOptionPane.YES_NO_OPTION,
       JOptionPane.QUESTION_MESSAGE,
       null,
       options,
       options[1]);
    return (n == JOptionPane.YES_OPTION);
  }

  boolean inDebugMode() {
    Debugger dm = _model.getDebugger();
    if (dm.isAvailable()) {
      return dm.isReady() && (_debugPanel != null);
    }
    else {
      return false;
    }
  }

  /**
   * returns teh find replace dialog
   * package protected for use in tests
   */
  FindReplaceDialog getFindReplaceDialog(){
    return _findReplace;
  }
  

  /**
   * Builds the Hashtables in KeyBindingManager that are used to keep track
   * of key-bindings and allows for live updating, conflict resolution, and
   * intelligent error messages (the ActionToNameMap).
   * IMPORTANT: Don't use this way to put actions into the KeyBindingManager if
   * the action is a menu item. It will already have been put in. Putting in
   * in again will cause bug #803304 "Uncomment lines wont rebind".
   */
  private void _setUpKeyBindingMaps() {
    ActionMap _actionMap = _currentDefPane.getActionMap();

    KeyBindingManager.Singleton.put(KEY_BACKWARD, _actionMap.get(DefaultEditorKit.backwardAction),null, "Backward");
    KeyBindingManager.Singleton.addShiftAction(KEY_BACKWARD,
                                               DefaultEditorKit.selectionBackwardAction);

    KeyBindingManager.Singleton.put(KEY_BEGIN_DOCUMENT, _actionMap.get(DefaultEditorKit.beginAction), null, "Begin Document");
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_DOCUMENT,
                                               DefaultEditorKit.selectionBeginAction);

    //KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _actionMap.get(DefaultEditorKit.beginLineAction), null, "Begin Line");
    KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _beginLineAction,
                                    null, "Begin Line");
    //KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_LINE,
    //                                           DefaultEditorKit.selectionBeginLineAction);
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_LINE,
                                               _selectionBeginLineAction);

    KeyBindingManager.Singleton.put(KEY_PREVIOUS_WORD,
                                    _actionMap.get(DefaultEditorKit.previousWordAction), null, "Previous Word");
    KeyBindingManager.Singleton.addShiftAction(KEY_PREVIOUS_WORD,
                                               DefaultEditorKit.selectionPreviousWordAction);


    KeyBindingManager.Singleton.put(KEY_DOWN,
                                    _actionMap.get(DefaultEditorKit.downAction), null, "Down");
    KeyBindingManager.Singleton.addShiftAction(KEY_DOWN,
                                               DefaultEditorKit.selectionDownAction);

    KeyBindingManager.Singleton.put(KEY_END_DOCUMENT,
                                    _actionMap.get(DefaultEditorKit.endAction), null, "End Document");
    KeyBindingManager.Singleton.addShiftAction(KEY_END_DOCUMENT,
                                               DefaultEditorKit.selectionEndAction);

    KeyBindingManager.Singleton.put(KEY_END_LINE,
                                    _actionMap.get(DefaultEditorKit.endLineAction), null, "End Line");
    KeyBindingManager.Singleton.addShiftAction(KEY_END_LINE,
                                               DefaultEditorKit.selectionEndLineAction);

    KeyBindingManager.Singleton.put(KEY_NEXT_WORD,
                                    _actionMap.get(DefaultEditorKit.nextWordAction), null, "Next Word");
    KeyBindingManager.Singleton.addShiftAction(KEY_NEXT_WORD,
                                               DefaultEditorKit.selectionNextWordAction);

    KeyBindingManager.Singleton.put(KEY_FORWARD,
                                    _actionMap.get(DefaultEditorKit.forwardAction), null, "Forward");
    KeyBindingManager.Singleton.addShiftAction(KEY_FORWARD,
                                               DefaultEditorKit.selectionForwardAction);

    KeyBindingManager.Singleton.put(KEY_UP,
                                    _actionMap.get(DefaultEditorKit.upAction), null, "Up");
    KeyBindingManager.Singleton.addShiftAction(KEY_UP,
                                               DefaultEditorKit.selectionUpAction);

    // These last methods have no default selection methods
    KeyBindingManager.Singleton.put(KEY_PAGE_DOWN,
                                    _actionMap.get(DefaultEditorKit.pageDownAction), null, "Page Down");
    KeyBindingManager.Singleton.put(KEY_PAGE_UP,
                                    _actionMap.get(DefaultEditorKit.pageUpAction), null, "Page Up");
    KeyBindingManager.Singleton.put(KEY_CUT_LINE,
                                    _cutLineAction, null, "Cut Line");
    KeyBindingManager.Singleton.put(KEY_CLEAR_LINE,
                                    _clearLineAction, null, "Clear Line");
    KeyBindingManager.Singleton.put(KEY_DELETE_PREVIOUS,
                                    _actionMap.get(DefaultEditorKit.deletePrevCharAction), null, "Delete Previous");
    KeyBindingManager.Singleton.put(KEY_DELETE_NEXT,
                                    _actionMap.get(DefaultEditorKit.deleteNextCharAction), null, "Delete Next");
  }

  /**
   * @param listener The ComponentListener to add to the open documents list
   * This method allows for testing of the dancing UI (See MainFrameTest.testDancingUI()).
   */
  public void addComponentListenerToOpenDocumentsList(ComponentListener listener){
    _docSplitPane.getLeftComponent().addComponentListener(listener);
  }

  /**
   * The OptionListener for FONT_MAIN
   */
  private class MainFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      _setMainFont();
    }
  }

  /**
   * The OptionListener for FONT_LINE_NUMBERS
   */
  private class LineNumbersFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      _updateLineNums();
    }
  }

  /**
   * The OptionListener for FONT_DOCLIST
   */
  private class DoclistFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
      _model.getDocCollectionWidget().setFont(doclistFont);
    }
  }

  /**
   *  The OptionListener for FONT_TOOLBAR
   */
  private class ToolbarFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      _updateToolbarButtons();
    }
  }

  /**
   *  The OptionListener for DEFINITIONS_NORMAL_COLOR
   */
  private class NormalColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      _updateNormalColor();
    }
  }

  /**
   *  The OptionListener for DEFINITIONS_BACKGROUND_COLOR
   */
  private class BackgroundColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      _updateBackgroundColor();
    }
  }

  /**
   *  The OptionListener for TOOLBAR options
   */
  private class ToolbarOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _updateToolbarButtons();
    }
  }
  /**
   *  The OptionListener for WORKING_DIRECTORY
   */
  private class WorkingDirOptionListener implements OptionListener<File> {
    public void optionChanged(OptionEvent<File> oce) {
      _setCurrentDirectory(oce.value);
    }
  }

  /**
   *  The OptionListener for LINEENUM_ENABLED
   */
  private class LineEnumOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _updateDefScrollRowHeader();
    }
  }

  /**
   * The OptionListener for QUIT_PROMPT
   */
  private class QuitPromptOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _promptBeforeQuit = oce.value.booleanValue();
    }
  }

  /**
   * The OptionListener for RECENT_FILES_MAX_SIZE
   */
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
    }
  };
}
