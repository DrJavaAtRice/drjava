/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.beans.*;

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;
import java.net.URL;

import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.platform.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.model.debug.DebugListener;
import edu.rice.cs.drjava.model.debug.Breakpoint;
import edu.rice.cs.drjava.model.repl.InteractionsEditorKit;
import edu.rice.cs.drjava.ui.config.*;
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;
import edu.rice.cs.drjava.ui.JUnitPanel.JUnitErrorListPane;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeOptionListener;
import edu.rice.cs.drjava.ui.config.ConfigFrame;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.util.swing.DelegatingAction;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.SwingWorker;

/**
 * DrJava's main window.
 * @version $Id$
 */
public class MainFrame extends JFrame implements OptionConstants {
  
  private static final int INTERACTIONS_TAB = 0;
  //private static final int COMPILE_TAB = 1;
  //private static final int OUTPUT_TAB = 2;
  //private static final int JUNIT_TAB = 3;

  // GUI Dimensions
  private static final int GUI_WIDTH = 800;
  private static final int GUI_HEIGHT = 700;
  private static final int DOC_LIST_WIDTH = 150;

  private static final String ICON_PATH = "/edu/rice/cs/drjava/ui/icons/";
  private static final String DEBUGGER_OUT_OF_SYNC =
    " Current document is out of sync with the debugger" +
    " and should be recompiled!";

  /**
   * Number of seconds to wait before displaying "Stepping..." message
   * after a step is requested in the debugger.
   */
  private static final int DEBUG_STEP_TIMER_VALUE = 2000;

  /**
   * The model which controls all logic in DrJava.
   */
  private final SingleDisplayModel _model;

  /** 
   * Maps an OpenDefDoc to its JScrollPane.
   */
  private Hashtable _defScrollPanes;
  
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
  private CompilerErrorPanel _errorPanel;
  private OutputPane _outputPane;
  private InteractionsPane _interactionsPane;
  private DebugPanel _debugPanel;
  private JUnitPanel _junitPanel;
  private FindReplaceDialog _findReplace;
  private LinkedList _tabs;
  
  /**
   * Panel to hold both InteractionsPane and its sync message.
   * TO DO: move sync message into the pane itself.
   */
  private JPanel _interactionsWithSyncPanel;
  /**
   * Label to display message if Interactions are out of sync with Definitions.
   */
  private JLabel _syncStatus;
  
  // Status bar fields
  private JPanel _statusBar;
  private JLabel _fileNameField;
  private JLabel _currLocationField;
  private PositionListener _posListener;
  
  // Split panes for layout
  private JSplitPane _docSplitPane;
  private JSplitPane _debugSplitPane;
  private JSplitPane _mainSplit;
  
  private JList _docList;
  private JButton _saveButton;
  private JButton _compileButton;
  private JButton _junitButton;
  private JToolBar _toolBar;
  
  // Menu fields
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _toolsMenu;
  private JMenu _debugMenu;
  private JMenu _helpMenu;
  private JMenuItem _debuggerEnabledMenuItem;
  private JMenuItem _runDebuggerMenuItem;
  private JMenuItem _resumeDebugMenuItem;
  private JMenuItem _stepIntoDebugMenuItem;
  private JMenuItem _stepOverDebugMenuItem;
  private JMenuItem _stepOutDebugMenuItem;
  private JMenuItem _suspendDebugMenuItem;
  private JMenuItem _toggleBreakpointMenuItem;
  private JMenuItem _printBreakpointsMenuItem;
  private JMenuItem _clearAllBreakpointsMenuItem;
  
  // Cached frames and dialogs
  private ConfigFrame _configFrame;
  private HelpFrame _helpFrame;
  private AboutDialog _aboutDialog;

  /**
   * Keeps track of the recent files list in the File menu.
   */
  private RecentFileManager _recentFileManager;
  
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
  private gj.util.Hashtable<Breakpoint, HighlightManager.HighlightInfo> _breakpointHighlights;
 
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
   * For saving files.
   * We have a persistent dialog to keep track of the last directory
   * from which we saved.
   */
  private JFileChooser _saveChooser;

  /**
   * Returns the files to open to the model (command pattern).
   */
  private FileOpenSelector _openSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      return getOpenFiles(_openChooser);
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

  /** Resets the document in the definitions pane to a blank one. */
  private Action _newAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
      _new();
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


  /** Saves the current document. */
  private Action _saveAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _save();
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

  /** Compiles the document in the definitions pane. */
  private Action _compileAction = new AbstractAction("Compile Current Document") {
    public void actionPerformed(ActionEvent ae) {
      _compile();
    }
  };
  
  /** Compiles all open documents. */
  private Action _compileAllAction = new AbstractAction("Compile All Documents") {
    public void actionPerformed(ActionEvent ae) {
      _compileAll();
    }
  };

  /** Runs JUnit on the document in the definitions pane. */
  private Action _junitAction = new AbstractAction("Test Using JUnit") {
    public void actionPerformed(ActionEvent ae) {
      _junit();
      //_setDividerLocation();  is this necessary?
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
      super.actionPerformed(e);
      if (c != null) c.requestFocus();
    }
  };


  /** Undoes the last change to the active definitions document. */
  private DelegatingAction _undoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      _currentDefPane.requestFocus();
    }
  };

  /** Redoes the last undo to the active definitions document. */
  private DelegatingAction _redoAction = new DelegatingAction() {
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      _currentDefPane.requestFocus();
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
      _tabbedPane.setSelectedComponent(_findReplace);  
      _findReplace.requestFocus();
      _setDividerLocation();
    }
  };

  /** Asks the user for a line number and goes there. */
  private Action _gotoLineAction = new AbstractAction("Goto Line...") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
    }
  };

  /** Indents the current selection. */
  private Action _indentLinesAction = new AbstractAction("Indent Line(s)") {
    public void actionPerformed(ActionEvent ae) {
      _currentDefPane.indent();
    }
  };
  
  /**
   * Action for commenting out a block of text using wing comments.
   */
  private Action _commentLinesAction = new AbstractAction("Comment Out Line(s)")
  {
    public void actionPerformed(ActionEvent ae) {
      // Delegate everything to the DefinitionsDocument.
      int start = _currentDefPane.getSelectionStart();
      int end = _currentDefPane.getSelectionEnd();
      _model.getActiveDocument().getDocument().commentLines(start, end);
    }
  };
  
  /**
   * Action for un-commenting a block of commented text.
   */
  private Action _uncommentLinesAction = new AbstractAction("Uncomment Line(s)")
  {
    public void actionPerformed(ActionEvent ae) {
      // Delegate everything to the DefinitionsDocument.
      int start = _currentDefPane.getSelectionStart();
      int end = _currentDefPane.getSelectionEnd();
      _model.getActiveDocument().getDocument().uncommentLines(start, end);
    }
  };
  
  
  /** Clears DrJava's output console. */
  private Action _clearOutputAction = new AbstractAction("Clear Console") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
    }
  };

  /** Resets the Interactions pane. */
  private Action _resetInteractionsAction =
    new AbstractAction("Reset Interactions")
  {
    public void actionPerformed(ActionEvent ae) {
      String title = "Confirm Reset Interactions";
      
      String message = "Are you sure you want to reset the " +
        "Interactions Pane?";
      
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.YES_OPTION) {
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
    }
  };

  /** Shows the user documentation. */
  private Action _helpAction = new AbstractAction("Help") {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
      if (_helpFrame == null) {
        _helpFrame = new HelpFrame();
      }
      _helpFrame.show();
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
  private Action _switchToNextAction =
    new AbstractAction("Next Document")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.setNextActiveDocument();
    }
  };

  /** Switches to previous document. */
  private Action _switchToPrevAction =
    new AbstractAction("Previous Document")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.setPreviousActiveDocument();
    }
  };

  /** Calls the ConfigFrame to edit preferences */
  private Action _editPreferencesAction = 
    new AbstractAction("Preferences...") 
  {
    public void actionPerformed(ActionEvent ae) {
      // Create frame if we haven't yet
      if (_configFrame == null) {
        _configFrame = new ConfigFrame(MainFrame.this);
      }
      _configFrame.show();
    }
  };
  
  /** Enables the debugger */
  private Action _toggleDebuggerAction =
    new AbstractAction("Debug Mode")
  {
    public void actionPerformed(ActionEvent ae) {
      debuggerToggle();
    }
  };

  /** Resumes debugging */
  private Action _resumeDebugAction =
    new AbstractAction("Resume Debugger")
  {
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
  private Action _stepIntoDebugAction =
    new AbstractAction("Step Into")
  {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.STEP_INTO);
    }
  };

  /** Runs the next line, without stepping into methods */
  private Action _stepOverDebugAction =
    new AbstractAction("Step Over")
  {
    public void actionPerformed(ActionEvent ae) {
      debuggerStep(Debugger.STEP_OVER);
    }
  };

  /** Steps out of the next method call */
  private Action _stepOutDebugAction =
    new AbstractAction("Step Out")
  {
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
  protected Action _cutLineAction = new AbstractAction("Cut Line")
  {
    public void actionPerformed(ActionEvent ae) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      int oldCol = _model.getActiveDocument().getDocument().getCurrentCol();
      _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
      // if oldCol is equal to the current column, then selectionEndLine did
      // nothing, so we're at the end of the line and should remove the newline
      // character
      if (oldCol == _model.getActiveDocument().getDocument().getCurrentCol()) {
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
  protected Action _clearLineAction = new AbstractAction("Clear Line")
  {
    public void actionPerformed(ActionEvent ae) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      int oldCol = _model.getActiveDocument().getDocument().getCurrentCol();
      _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
      // if oldCol is equal to the current column, then selectionEndLine did
      // nothing, so we're at the end of the line and should remove the newline
      // character
      if (oldCol == _model.getActiveDocument().getDocument().getCurrentCol()) {
        _actionMap.get(DefaultEditorKit.deleteNextCharAction).actionPerformed(ae);
      }
      else {
        java.awt.datatransfer.Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        java.awt.datatransfer.Transferable contents = clip.getContents(null);
        cutAction.actionPerformed(ae);
        clip.setContents(contents, null);
      }
    }
  };
  
  /**
   * A more intelligent action for the home key: moves to the first
   * non-whitespace character on the line (if right of it), otherwise
   * moves to the first character on the line.
   *
  private Action _homeAction = new AbstractAction() {
    public void actionPerformed(ActionEvent ae) {
      try {
        DefinitionsDocument doc = _model.getActiveDocument().getDocument();
        int currPos = _currentDefPane.getCaretPosition();
        int firstRealChar = doc.getFirstNonWSCharPos(currPos);
        System.out.println("homeAction: " + currPos + " " + firstRealChar);
        if ((firstRealChar > -1) && (firstRealChar < currPos)) {
          _currentDefPane.setCaretPosition(firstRealChar);
        }
        else {
          int firstChar = doc.getLineStartPos(currPos);
          _currentDefPane.setCaretPosition(firstChar);
        }
      }
      catch (BadLocationException ble) {
        // Shouldn't happen: we're using a legal position
        throw new UnexpectedException(ble);
      }
    }
  };*/
  
  /** Interprets the commands in a file in the interactions window */
  private Action _loadHistoryAction = new AbstractAction("Load Interactions History...")
  {
    public void actionPerformed(ActionEvent ae) {
      // Show interactions tab
      _tabbedPane.setSelectedIndex(INTERACTIONS_TAB);
      
      // Working directory is default place to start
      File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
      if (workDir == FileOption.NULL_FILE) {
        workDir = new File(System.getProperty("user.dir"));
      }
      if (workDir.isFile() && workDir.getParent() != null) {
        workDir = workDir.getParentFile();
      }
      final JFileChooser jfc = new JFileChooser();
      jfc.setCurrentDirectory(workDir);
      jfc.setDialogTitle("Load Interactions History");
      jfc.setFileFilter(new InteractionsHistoryFilter());
      FileOpenSelector selector = new FileOpenSelector() {
        public File[] getFiles() throws OperationCanceledException {            
          return getOpenFiles(jfc);
        }
      };
      try {
        _model.loadHistory(selector);
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
  
  /** Save the commands in the interactions window's history to a file */
  private Action _saveHistoryAction = new AbstractAction("Save Interactions History...")
  {
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
      String history = _model.getHistoryAsString();
      
      // Edit the history
      if (resp == 0) {
        history = (new HistorySaveDialog(MainFrame.this)).editHistory(history);
      }
      if (history == null) {
        return; // save cancelled
      }
      
      // Working directory is default place to start
      File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
      if (workDir == FileOption.NULL_FILE) {
        workDir = new File(System.getProperty("user.dir"));
      }
      if (workDir.isFile() && workDir.getParent() != null) {
        workDir = workDir.getParentFile();
      }
      final JFileChooser jfc = new JFileChooser();
      jfc.setCurrentDirectory(workDir);
      jfc.setDialogTitle("Save Interactions History");
      jfc.setFileFilter(new InteractionsHistoryFilter());
      FileSaveSelector selector = new FileSaveSelector() {
        public File getFile() throws OperationCanceledException {
          return getSaveFile(jfc);
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
      
      File c = null;
      try {
        c = selector.getFile();
      }
      catch (OperationCanceledException oce) {
        return;
        // don't need to do anything
      }
      
      if (c != null) {
        if (c.getName().indexOf('.') == -1)
          c = new File(c.getAbsolutePath() + "." + 
                       InteractionsHistoryFilter.HIST_EXTENSION);
        try {
          FileOutputStream fos = new FileOutputStream(c);
          OutputStreamWriter osw = new OutputStreamWriter(fos);
          BufferedWriter bw = new BufferedWriter(osw);
          bw.write(history, 0, history.length());
          bw.close();
        }
        catch (IOException ioe) {
          _showIOError(new IOException("An error occured writing the history to a file"));
        }
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

  /** Creates the main window, and shows it. */
  public MainFrame() {
    // Platform-specific UI setup.
    PlatformFactory.ONLY.beforeUISetup();
    
    // create position listener for line numbers in status bar
    _posListener = new PositionListener();
    _setUpStatusBar();

    // create our model
    _model = new SingleDisplayModel();
    
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

    
    // Working directory is default place to start
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File(System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    _openChooser = new JFileChooser();
    _openChooser.setCurrentDirectory(workDir);
    _openChooser.setFileFilter(new JavaSourceFilter());
    _openChooser.setMultiSelectionEnabled(true);
    _saveChooser = new JFileChooser();
    _saveChooser.setCurrentDirectory(workDir);
    _saveChooser.setFileFilter(new JavaSourceFilter());
    
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Set up listeners
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());
    
    _defScrollPanes = new Hashtable();

    // Create tabs before DefPane
    _setUpTabs();

    // DefinitionsPane
    JScrollPane defScroll = _createDefScrollPane(_model.getActiveDocument());
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();
    
    // set up key-bindings
    KeyBindingManager.Singleton.setMainFrame(this);
    KeyBindingManager.Singleton.setActionMap(_currentDefPane.getActionMap());
    _setUpKeyBindingMaps();
    
    _posListener.updateLocation();

    // Need to set undo/redo actions to point to the initial def pane
    // on switching documents later these pointers will also switch
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());

    _errorPanel.getErrorListPane().setLastDefPane(_currentDefPane);
    _errorPanel.reset();
    _junitPanel.getJUnitErrorListPane().setLastDefPane(_currentDefPane);
    _junitPanel.reset();

    // set up menu bar and actions
    _setUpActions();
    _setUpMenuBar();
    _setUpToolBar();
    _setUpDocumentSelector();
    
    _recentFileManager = new RecentFileManager(_fileMenu.getItemCount() - 2, 
                                               _fileMenu,
                                               this);

    // Set frame icon
    setIconImage(getIcon("drjava64.png").getImage());

    // Set size and position
    setBounds(0, 0, GUI_WIDTH, GUI_HEIGHT);
    setSize(GUI_WIDTH, GUI_HEIGHT);
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    final int menubarHeight = 24;
    if (frameSize.height > screenSize.height - menubarHeight) {
      // Too tall, so resize
      frameSize.height = screenSize.height - menubarHeight;
    }
    if (frameSize.width > screenSize.width) {
      // Too wide, so resize
      frameSize.width = screenSize.width;
    }
    // Set to the new correct size and location
    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height - menubarHeight) / 2);

    _setUpPanes();
    updateFileTitle();
    
    _promptBeforeQuit = DrJava.getConfig().getSetting(QUIT_PROMPT).booleanValue();

    // Set the fonts
    _setMainFont();    
    Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
    _docList.setFont(doclistFont);
    
    // Add option listeners for changes to config options
    DrJava.getConfig().addOptionListener( OptionConstants.FONT_MAIN, new MainFontOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.FONT_DOCLIST, new DoclistFontOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.FONT_TOOLBAR, new ToolbarFontOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.TOOLBAR_ICONS_ENABLED, new ToolbarOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.TOOLBAR_TEXT_ENABLED, new ToolbarOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.WORKING_DIRECTORY, new WorkingDirOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.LINEENUM_ENABLED, new LineEnumOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.QUIT_PROMPT, new QuitPromptOptionListener());
    DrJava.getConfig().addOptionListener( OptionConstants.RECENT_FILES_MAX_SIZE, new RecentFilesOptionListener());
    
    // Initialize breakpoint highlights hashtable, for easy removal of highlights
    _breakpointHighlights = new gj.util.Hashtable<Breakpoint, HighlightManager.HighlightInfo>();
    
    // Set cached frames and dialogs to null until they are created
    _configFrame = null;
    _helpFrame = null;
    _aboutDialog = null;
      
    // If any errors occurred while parsing config file, show them
    _showConfigException();
    
    KeyBindingManager.Singleton.setShouldCheckConflict(false);
    
    // Platform-specific UI setup.
    PlatformFactory.ONLY.afterUISetup(_aboutAction, _editPreferencesAction, _quitAction);
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
   * Make the cursor an hourglass.
   */
  public void hourglassOn() {
    getGlassPane().setVisible(true);
  }

  /**
   * Return the cursor to normal.
   */
  public void hourglassOff() {
    getGlassPane().setVisible(false);
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
        try {
          debugger.startup();
          _updateDebugStatus();
        }
        catch (DebugException de) {
          _showError(de, "Debugger Error",
                     "Could not start the debugger.");
        }
      }
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
    _debugPanel.updateData(false);
  }
  
  private void _hideDebuggerPanel() {
    _mainSplit.setTopComponent(_docSplitPane);
  }


  /**
   * Updates the title bar with the name of the active document.
   */
  public void updateFileTitle() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    String filename = _model.getDisplayFilename(doc);
    if (!filename.equals(_fileTitle)) {
      _fileTitle = filename;
      setTitle(filename + " - DrJava");
      _docList.repaint();
    }
    // Always update this field-- two files in different directories
    //  can have the same _fileTitle
    _fileNameField.setText(_model.getDisplayFullPath(doc));
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   */
  public File[] getOpenFiles(JFileChooser jfc) throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    File selection = jfc.getSelectedFile();//_openChooser.getSelectedFile();
    if (selection != null) {
      jfc.setSelectedFile(selection.getParentFile());
      jfc.setSelectedFile(selection);
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
   * Returns whether the compiler output tab is currently showing.
   */
  public boolean isCompilerTabSelected() {
    return _tabbedPane.getSelectedComponent() == _errorPanel;
  }
  
  /**
   * Returns whether the test output tab is currently showing.
   */
  public boolean isTestTabSelected() {
    return _tabbedPane.getSelectedComponent() == _junitPanel;
  }
  
  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  private void _installNewDocumentListener(final Document d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals(""))
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        //_compileAction.setEnabled(false);
        updateFileTitle();
      }
      public void insertUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals(""))
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        //_compileAction.setEnabled(false);
        updateFileTitle();
      }
      public void removeUpdate(DocumentEvent e) {
        _saveAction.setEnabled(true);
        if (inDebugMode() && _debugPanel.getStatusText().equals(""))
          _debugPanel.setStatusText(DEBUGGER_OUT_OF_SYNC);
        //_compileAction.setEnabled(false);
        updateFileTitle();
      }
    });
  }


  private void _new() {
    _model.newFile();
  }

  private void _open() { 
    open (_openSelector);
  }
  
  void open(FileOpenSelector openSelector) {
    try {
      _model.openFiles(openSelector);
    }
    catch (AlreadyOpenException aoe) {
      OpenDefinitionsDocument openDoc = aoe.getOpenDocument();
      String filename = "File";
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
        _recentFileManager.updateOpenFiles(openDoc.getFile());
      }
      catch (IllegalStateException ise) {
        // Impossible: saved => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        // Recover, show it in the list anyway
        _recentFileManager.updateOpenFiles(fme.getFile());
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
  }

  private void _close() {
    _model.closeFile(_model.getActiveDocument());
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

  private void _closeAll() {
    _model.closeAllFiles();
  }


  private boolean _save() {
    try {
      if (_model.getActiveDocument().saveFile(_saveSelector)) {
        _currentDefPane.hasWarnedAboutModified(false);
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
      return _model.getActiveDocument().saveFileAs(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
      return false;
    }
  }

  private void _saveAll() {
    try {
      _model.saveAllFiles(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
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
  
  private void _quit() {
    if (_promptBeforeQuit) {
      String title = "Quit DrJava?";
      String message = "Are you sure you want to quit DrJava?";
      
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
      if (rc != JOptionPane.YES_OPTION) {
        return;
      }
    }
      
    _recentFileManager.saveRecentFiles();
        
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
  
  private void _compileAll() {
    
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        try {          
          _model.compileAll();
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
                                        "not be caught be DrJava.  Details about the exception should\n" +
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
  }/

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
      
      boolean isUntitled = doc.getDocument().isUntitled();
      if (isUntitled) {
        JOptionPane.showMessageDialog(this,
                                      "You must save and compile this document before you can\n" +
                                      "set a breakpoint in it.",
                                      "Must Save and Compile",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      boolean isModified = doc.isModifiedSinceSave();
      if (isModified  && !_currentDefPane.hasWarnedAboutModified()) {
        
        int rc = JOptionPane.showConfirmDialog(this,
                                               "This document has been modified and may be out of sync\n" + 
                                               "with the debugger.  It is recommended that you first\n" +
                                               "save and recompile before continuing to use the debugger,\n" +
                                               "to avoid any unexpected errors.  Would you still like to\n" +
                                               "toggle the breakpoint on the specified line?",
                                               "Toggle breakpoint on modified file?",
                                               JOptionPane.YES_NO_OPTION);
        
        switch (rc) {
          case JOptionPane.YES_OPTION:
            _currentDefPane.hasWarnedAboutModified(true);
            break;
          case JOptionPane.NO_OPTION:
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
    _model.getDebugger().removeAllBreakpoints();
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
               "A JSwat error occurred in the last operation.\n\n");
  }

  private void _showError(Throwable e, String title, String message) {
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
    JOptionPane.showMessageDialog(this,
                                  "This document has been modified since its last save and\n" + 
                                  "may be out of sync with the debugger. It is suggested that\n" +
                                  "you save and recompile before continuing to debug in order\n" +
                                  "to avoid any unexpected errors.",
                                  "Debugging modified file!",
                                  JOptionPane.WARNING_MESSAGE);

    _currentDefPane.hasWarnedAboutModified(true);
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
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        if (chosen != null) {
          //append ".java" if not written by user
          if (fc.getFileFilter() instanceof JavaSourceFilter) {
            if (chosen.getName().indexOf(".") == -1)
              return new File (chosen.getAbsolutePath() + ".java");
          }
          return chosen; 
        }
        else
          throw new RuntimeException("filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
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
  private File[] getChosenFiles(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File[] chosen = fc.getSelectedFiles();
        if (chosen == null)
            throw new RuntimeException("filechooser returned null file");
        
        if (chosen.length == 0) chosen = new File[] { null };

        if (chosen[0] == null)
          chosen[0] = fc.getSelectedFile();
        return chosen;
        
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
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
   * Update all appropriate listeners that the CompilerErrorModels
   * have changed.
   */
  void updateErrorListeners() {
    // Loop through each errorListener and tell it to update itself
    ListModel docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        docs.getElementAt(i);
      JScrollPane scroll = (JScrollPane) _defScrollPanes.get(doc);
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        CompilerErrorCaretListener listener = pane.getErrorCaretListener();
        listener.resetErrorModel();

        JUnitErrorCaretListener junitListener = pane.getJUnitErrorCaretListener();
        junitListener.resetErrorModel();
      }
    }
  }

  /**
   * Removes the CompilerErrorCaretListener corresponding to
   * the given document, after that document has been closed.
   * (Allows pane and listener to be garbage collected...)
   */
  private void _removeErrorListener(OpenDefinitionsDocument doc) {
    JScrollPane scroll = (JScrollPane) _defScrollPanes.get(doc);
    if (scroll != null) {
      DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
      pane.removeCaretListener(pane.getErrorCaretListener());
      pane.removeCaretListener(pane.getJUnitErrorCaretListener());
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
    _setUpAction(_openAction, "Open", "Open an existing file");
    _setUpAction(_saveAction, "Save", "Save the current document");
    _setUpAction(_saveAsAction, "Save As", "SaveAs",
                 "Save the current document with a new name");
    _setUpAction(_revertAction, "Revert", "Revert the current document to saved version");
    //_setUpAction(_revertAllAction, "Revert All", "RevertAll",
    //             "Revert all open documents to the saved versions");

    _setUpAction(_closeAction, "Close", "Close the current document");
    _setUpAction(_closeAllAction, "Close All", "CloseAll", "Close all documents");
    _setUpAction(_saveAllAction, "Save All", "SaveAll", "Save all open documents");

    _setUpAction(_compileAction, "Compile", "Compile the current document");
    _setUpAction(_compileAllAction, "Compile All", "CompileAll",
                 "Compile all open documents");
    _setUpAction(_printAction, "Print", "Print the current document");
    _setUpAction(_pageSetupAction, "Page Setup", "PageSetup", "Page Setup");
    _setUpAction(_printPreviewAction, "Print Preview", "PrintPreview", "Print Preview");

    _setUpAction(cutAction, "Cut", "Cut selected text to the clipboard");
    _setUpAction(copyAction, "Copy", "Copy selected text to the clipboard");
    _setUpAction(pasteAction, "Paste", "Paste text from the clipboard");
    _setUpAction(_selectAllAction, "Select All", "Select all text");

    cutAction.putValue(Action.NAME, "Cut");
    copyAction.putValue(Action.NAME, "Copy");
    pasteAction.putValue(Action.NAME, "Paste");

    _setUpAction(_switchToPrevAction, "Back", "Previous Document");
    _setUpAction(_switchToNextAction, "Forward", "Next Document");

    _setUpAction(_findReplaceAction, "Find", "Find/Replace");
    _setUpAction(_editPreferencesAction, "Preferences", "Edit DrJava Preferences");
    _setUpAction(_helpAction, "Help", "Show the User Documentation");
    _setUpAction(_aboutAction, "About", "About DrJava");

    _setUpAction(_undoAction, "Undo", "Undo previous command");
    _setUpAction(_redoAction, "Redo", "Redo last undo");

    _undoAction.putValue(Action.NAME, "Undo Previous Command");
    _redoAction.putValue(Action.NAME, "Redo Last Undo");

    //_setUpAction(_abortInteractionAction, "Break", "Abort the current interaction");
    _setUpAction(_resetInteractionsAction, "Reset", "Reset interactions");
  
    _setUpAction(_junitAction, "Test", "Run JUnit over the current document");

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
   * Sets up the components of the menu bar and links them to the private
   * fields within MainFrame.  This method serves to make the code
   * more legible on the higher calling level, i.e., the constructor.
   */
  private void _setUpMenuBar() {
    boolean showDebugger = (_model.getDebugger().isAvailable());

    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    _menuBar = new JMenuBar();
    _fileMenu = _setUpFileMenu(mask);
    _editMenu = _setUpEditMenu(mask);
    _toolsMenu = _setUpToolsMenu(mask);
    if (showDebugger) _debugMenu = _setUpDebugMenu(mask);
    _helpMenu = _setUpHelpMenu(mask);

    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_toolsMenu);
    if (showDebugger) _menuBar.add(_debugMenu);
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
    // key in the config file
    KeyBindingManager.Singleton.put(opt, a, item, item.getText());
    if (KeyBindingManager.Singleton.get(ks) == a) { 
      item.setAccelerator(ks);
      //KeyBindingManager.Singleton.addListener(opt, item);
    }
  }
  
  /**
   * Creates and returns a file menu.  Side effects: sets values for
   * _saveMenuItem.
   */
  private JMenu _setUpFileMenu(int mask) {
    JMenuItem tmpItem;
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    // New, open 
    _addMenuItem(fileMenu, _newAction, KEY_NEW_FILE);
    _addMenuItem(fileMenu, _openAction, KEY_OPEN_FILE);
    fileMenu.addSeparator();

    _addMenuItem(fileMenu, _saveAction, KEY_SAVE_FILE);
    _saveAction.setEnabled(false);
    _addMenuItem(fileMenu, _saveAsAction, KEY_SAVE_FILE_AS);
    tmpItem = fileMenu.add(_saveAllAction);

    tmpItem = fileMenu.add(_revertAction);
    _revertAction.setEnabled(false);
    //tmpItem = fileMenu.add(_revertAllAction);

    // Close, Close all
    fileMenu.addSeparator();
    _addMenuItem(fileMenu, _closeAction, KEY_CLOSE_FILE);
    tmpItem = fileMenu.add(_closeAllAction);

    // Page setup, print preview, print
    fileMenu.addSeparator();
    tmpItem = fileMenu.add(_pageSetupAction);
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
    JMenuItem tmpItem;
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
    editMenu.add(_indentLinesAction);
    _addMenuItem(editMenu, _commentLinesAction, KEY_COMMENT_LINES);
    _addMenuItem(editMenu, _uncommentLinesAction, KEY_UNCOMMENT_LINES);
    
    // Find/replace, goto
    editMenu.addSeparator();
    _addMenuItem(editMenu, _findReplaceAction, KEY_FIND_REPLACE);
    _addMenuItem(editMenu, _gotoLineAction, KEY_GOTO_LINE);
      
    // Next, prev doc
    editMenu.addSeparator();
    _addMenuItem(editMenu, _switchToPrevAction, KEY_PREVIOUS_DOCUMENT);
    _addMenuItem(editMenu, _switchToNextAction, KEY_NEXT_DOCUMENT);
    
    // access to configurations GUI
    editMenu.addSeparator();
    editMenu.add(_editPreferencesAction);
    
    // Add the menus to the menu bar
    return editMenu;
  }

  /**
   * Creates and returns a tools menu.
   */
  private JMenu _setUpToolsMenu(int mask) {
    JMenuItem tmpItem;
    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    // Compile, Compile all
    _addMenuItem(toolsMenu, _compileAllAction, KEY_COMPILE_ALL);
    _addMenuItem(toolsMenu, _compileAction, KEY_COMPILE);
    toolsMenu.add(_junitAction);

    // Abort/reset interactions, clear console
    toolsMenu.addSeparator();
    /*
    _abortInteractionAction.setEnabled(false);
    _addMenuItem(toolsMenu, _abortInteractionAction, KEY_ABORT_INTERACTION);
    */
    toolsMenu.add(_resetInteractionsAction);
    toolsMenu.add(_loadHistoryAction);
    toolsMenu.add(_saveHistoryAction);
    toolsMenu.add(_clearHistoryAction);
    toolsMenu.addSeparator();

    toolsMenu.add(_clearOutputAction);

    // Add the menus to the menu bar
    return toolsMenu;
  }

  /**
   * Creates and returns a debug menu.
   */
  private JMenu _setUpDebugMenu(int mask) {
    JMenuItem tempItem;
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
    _clearAllBreakpointsMenuItem = debugMenu.add(_clearAllBreakpointsAction);
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
   * Creates and returns a help menu.
   */
  private JMenu _setUpHelpMenu(int mask) {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.add(_helpAction);
    helpMenu.add(_aboutAction);
    return helpMenu;
  }

  /**
   * Creates a toolbar button for undo and redo, which behave differently.
   */
  JButton _createManualToolbarButton(Action a) {
    final JButton ret;
    
    Font buttonFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);

    // Check whether icons should be shown
    boolean useIcon = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    boolean useText = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    final Icon icon = (useIcon) ? (Icon) a.getValue(Action.SMALL_ICON) : null;
    if (icon == null) {
      ret = new JButton( (String) a.getValue(Action.DEFAULT)) {
        public boolean isFocusTraversable() {
          return false;
        }
      };
    }
    else {
      ret = new JButton(icon) {
        public boolean isFocusTraversable() {
          return false;
        }
      };
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
    boolean useText = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean useIcons = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    Font buttonFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);
    
    final JButton result = new JButton(a) {
      public boolean isFocusTraversable() {
        return false;
      }
    };
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

    _toolBar.addSeparator();

    // New, open, save, close
    _toolBar.add(_createToolbarButton(_newAction));
    _toolBar.add(_createToolbarButton(_openAction));
    _saveButton = _createToolbarButton(_saveAction);
    _toolBar.add(_saveButton);
    _toolBar.add(_createToolbarButton(_closeAction));
    
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
    _toolBar.addSeparator();
    _toolBar.add(_createManualToolbarButton(_undoAction));
    _toolBar.add(_createManualToolbarButton(_redoAction));
    
    // Find
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(_findReplaceAction));

    // Compile, reset, abort
    _toolBar.addSeparator();
    //_compileButton = _createToolbarButton(_compileAction);
    _compileButton = _createToolbarButton(_compileAllAction);
    _toolBar.add(_compileButton);
    _toolBar.add(_createToolbarButton(_resetInteractionsAction));
    //_toolBar.add(_createToolbarButton(_abortInteractionAction));

    // Junit
    _toolBar.addSeparator();
    
    _junitButton = _createToolbarButton(_junitAction);
    _toolBar.add(_junitButton);


    getContentPane().add(_toolBar, BorderLayout.NORTH);
  }

  /**
   * Update the toolbar's buttons, following any change to TOOLBAR_ICONS_ENABLED,
   * TOOLBAR_TEXT_ENABLED, or FONT_TOOLBAR (name, style, text)
   */ 
  private void _updateToolbarButtons() {
    
    Component[] buttons = _toolBar.getComponents();
    
    for (int i = 0; i< buttons.length; i++) {
      
      if (buttons[i] instanceof JButton) {
        
        JButton b = (JButton) buttons[i];
        Action a = b.getAction();

        // Work-around for strange configuration of undo/redo buttons
        /**if (a == null) {
          ActionListener[] al = b.getActionListeners(); // 1.4 only
            
          for (int j=0; j<al.length; j++) {
            if (al[j] instanceof Action) {
              a = (Action) al[j];
              break;
            }
          }
            
          */
          
        Font toolbarFont = DrJava.getConfig().getSetting(FONT_TOOLBAR);
        b.setFont(toolbarFont);
                   
        if (a==null) continue;
        //}
          
        boolean iconsEnabled = DrJava.getConfig().getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
          
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
          
        boolean textEnabled = DrJava.getConfig().getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
          
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
  }
  
  
  /**
   * Sets up the status bar with the filename field.
   */
  private void _setUpStatusBar() {
    _fileNameField = new JLabel();
    _fileNameField.setFont(_fileNameField.getFont().deriveFont(Font.PLAIN));


    _currLocationField = new JLabel();
    _currLocationField.setFont(_currLocationField.getFont().deriveFont(Font.PLAIN));
    _currLocationField.setVisible(true);

    _statusBar = new JPanel( new BorderLayout() );
    _statusBar.add( _fileNameField, BorderLayout.WEST );
    _statusBar.add( _currLocationField, BorderLayout.EAST );
    _statusBar.setBorder(
      new CompoundBorder(new EmptyBorder(2,2,2,2),
                         new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                                            new EmptyBorder(2,2,2,2))));
    getContentPane().add(_statusBar, BorderLayout.SOUTH);
  }

  /**
   * Inner class to handle the updating of current position within the
   * document.  Registered with the definitionspane.
   **/
  private class PositionListener implements CaretListener {

    public void caretUpdate( CaretEvent ce ) {
      _model.getActiveDocument().
        syncCurrentLocationWithDefinitions(ce.getDot());
      updateLocation();
    }

    public void updateLocation() {
      DefinitionsPane p = _currentDefPane;
      _currLocationField.setText(p.getCurrentLine() +
                                 ":" + p.getCurrentCol() + "\t");
    }
  }

  private void _setUpTabs() {
    _outputPane = new OutputPane(_model);
    _errorPanel = new CompilerErrorPanel(_model, this);
    _interactionsPane = new InteractionsPane(_model.getInteractionsDocument());
    _findReplace = new FindReplaceDialog(this, _model);
    
    final JScrollPane outputScroll = 
      new BorderlessScrollPane(_outputPane);
    _junitPanel = new JUnitPanel(_model, this);
    _tabbedPane = new JTabbedPane();
    _tabbedPane.addChangeListener(new ChangeListener () {
      public void stateChanged(ChangeEvent e) {
        if (_tabbedPane.getSelectedComponent() == outputScroll) {
          outputScroll.revalidate();
          outputScroll.repaint();
        }
        // Update error highlights?
        if (_currentDefPane != null) {
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
          _currentDefPane.getJUnitErrorCaretListener().updateHighlight(pos);
        }
      }
    });
    
    //_interactionsWithSyncPanel = new JPanel(new BorderLayout());
    //_syncStatus = new JLabel("Testing");
    //_interactionsWithSyncPanel.add(new BorderlessScrollPane(_interactionsPane),
    //                               BorderLayout.CENTER);
    //_interactionsWithSyncPanel.add(_syncStatus, BorderLayout.SOUTH);
                                    
    _tabbedPane.add("Interactions", new BorderlessScrollPane(_interactionsPane));
    _tabbedPane.add("Console", outputScroll);
    
    _tabs = new LinkedList();

    _tabs.addLast(_errorPanel);
    _tabs.addLast(_junitPanel);
    _tabs.addLast(_findReplace);
    
    // Show compiler output pane by default
    showTab(_errorPanel);
    
    _tabbedPane.setSelectedIndex(0);
    
    // Select interactions pane when interactions tab is selected
    _tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
        _interactionsPane.requestFocus();
      }
    }
    });
  }

  /**
   * Configures the component used for selecting active documents.
   */
  private void _setUpDocumentSelector() {
    _docList = new JList(_model.getDefinitionsDocuments());
    _docList.setSelectionModel(_model.getDocumentSelectionModel());
    _docList.setCellRenderer(new DocCellRenderer());

  }

  /**
   * Create a new DefinitionsPane and JScrollPane for an open
   * definitions document.
   * @param doc The open definitions document to wrap
   * @return JScrollPane containing a DefinitionsPane for the
   *         given document.
   */
  private JScrollPane _createDefScrollPane(OpenDefinitionsDocument doc) {
    DefinitionsPane pane = new DefinitionsPane(this, _model, doc);

    // Add listeners
    _installNewDocumentListener(doc.getDocument());
    CompilerErrorCaretListener caretListener =
      new CompilerErrorCaretListener(doc, _errorPanel.getErrorListPane(), pane, this);
    pane.addErrorCaretListener(caretListener);

    JUnitErrorCaretListener junitCaretListener =
      new JUnitErrorCaretListener(doc, _junitPanel.getJUnitErrorListPane(), pane, this);
    pane.addJUnitErrorCaretListener(junitCaretListener);

    // add a listener to update line and column.
    pane.addCaretListener( _posListener );    
    
    // Add to a scroll pane
    JScrollPane scroll = new BorderlessScrollPane(pane,
                                                  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    //scroll.setBorder(null); // removes all default borders (MacOS X installs default borders)
    
    // can be used to make sure line wrapping occurs
    /*scroll.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        pane.setSize(scroll.getViewport().getWidth(), pane.getHeight());
      }
    });*/
    
    if (DrJava.getConfig().getSetting(LINEENUM_ENABLED).booleanValue()) {
      scroll.setRowHeaderView( new LineEnumRule(pane));
    }
    
    _defScrollPanes.put(doc, scroll);
    
    return scroll;
  }


  private void _setUpPanes() {
    // Document list pane
    JScrollPane listScroll =
      new BorderlessScrollPane(_docList,
                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // DefinitionsPane
    JScrollPane defScroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());

    // Try to create debug panel (see if JSwat is around)
    if (_model.getDebugger().isAvailable()) {
      try {
        _debugPanel = new DebugPanel(this);
        _debugPanel.setPreferredSize(_debugPanel.getMinimumSize());
      }
      catch(NoClassDefFoundError e) {
        // Don't use the debugger
        _debugPanel = null;
      }
    } else {
      _debugPanel = null;
    }
    
    // Overall layout
    _docSplitPane = new BorderlessSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            true,
                                            listScroll,
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
    _mainSplit.setDividerLocation(2*getHeight()/3);
    _mainSplit.setOneTouchExpandable(true);
    _debugSplitPane.setOneTouchExpandable(true);
    _docSplitPane.setDividerLocation(DOC_LIST_WIDTH);
    _docSplitPane.setOneTouchExpandable(true);   
  }

  /**
   * Switch to the JScrollPane containing the DefinitionsPane
   * for the current active document.
   */
  private void _switchDefScrollPane() {    
    // Sync caret with location before switching
    _currentDefPane.getOpenDocument().
      syncCurrentLocationWithDefinitions( _currentDefPane.getCaretPosition() );

    JScrollPane scroll = 
      (JScrollPane) _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null) {
      throw new UnexpectedException(new Exception(
                                                  "Current definitions scroll pane not found."));
    }

    int oldLocation = _docSplitPane.getDividerLocation();
    
    // Following line should fix "Dancing UI" bug
    // scroll.setPreferredSize(_docSplitPane.getRightComponent().getPreferredSize()); 
    
    _docSplitPane.setRightComponent(scroll);
    _docSplitPane.setDividerLocation(oldLocation);
    _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();

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
    JScrollPane scroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());
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
    JRadioButtonMenuItem radio1, radio2, radio3;
    
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
   * Sets the current directory to be that of the given file.
   */
  private void _setCurrentDirectory(File file) {
    _openChooser.setCurrentDirectory(file.getAbsoluteFile());
    _saveChooser.setCurrentDirectory(file.getAbsoluteFile());
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
   * @param f is a Font object
   */
  private void _setMainFont() {
    
    Font f = DrJava.getConfig().getSetting(FONT_MAIN);
    
    Iterator scrollPanes = _defScrollPanes.values().iterator();
    while (scrollPanes.hasNext()) {  
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
    _interactionsPane.setFont(f);
    _outputPane.setFont(f);
    _findReplace.setFieldFont(f);
    _errorPanel.setListFont(f);
    _junitPanel.setListFont(f);
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
        _model.getActiveDocument().getDocument().getClassFileInSync())
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
          ListModel docs = _model.getDefinitionsDocuments();
          for (int i=0; i < docs.getSize(); i++) {
            ((OpenDefinitionsDocument)docs.getElementAt(i)).removeFromDebugger();
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    public void threadLocationUpdated(final OpenDefinitionsDocument doc, 
                                      final int lineNumber) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          // This listener is used when the document to display is
          // not the active document. In this case, when setActiveDocument
          // is called, the document won't yet have positive size and we
          // don't want to scroll to a line until it does, so we wait
          // for a call to setSize.
          ActionListener setSizeListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              _currentDefPane.centerViewOnLine(lineNumber);  
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
          }
          
          _removeThreadLocationHighlight();
          DefinitionsDocument defDoc = doc.getDocument();
          int startOffset = defDoc.getOffset(lineNumber);
          if (startOffset > -1) {
            int endOffset = defDoc.getLineEndPos(startOffset);
            if (endOffset > -1) {
              _currentThreadLocationHighlight = 
                _currentDefPane.getHighlightManager().addHighlight(startOffset,
                                                                   endOffset,
                                                                   DefinitionsPane.THREAD_PAINTER);
            }
          }
          if (doc.isModifiedSinceSave() && 
              !_currentDefPane.hasWarnedAboutModified()) {
            
            _showDebuggingModifiedFileWarning();
            
            //no need to update flag, because previous method call will do it
            //_hasWarnedAboutModified = true;
          }
          _updateDebugStatus();
          
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    public void breakpointSet(final Breakpoint bp) {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          JScrollPane scroll = 
            (JScrollPane) _defScrollPanes.get(bp.getDocument());
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
    
    public void currThreadDied() {
      _disableStepTimer();
      
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          _removeThreadLocationHighlight();
          if (inDebugMode()) {
            _setDebugMenuItemsEnabled(true);
          }

          // Make sure we're at the prompt
          // (This should really be fixed in InteractionsPane, not here.)
          _interactionsPane.moveToPrompt();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
  }

  /**
   * Inner class to listen to all events in the model.
   */
  private class ModelListener implements SingleDisplayModelListener{
    public void newFileCreated(OpenDefinitionsDocument doc) {
      _createDefScrollPane(doc);
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      _saveAction.setEnabled(false);
      _revertAction.setEnabled(true);
      updateFileTitle();
      _currentDefPane.requestFocus();
      try {
        _recentFileManager.updateOpenFiles(doc.getFile());
      }
      catch (IllegalStateException ise) {
        // Impossible: saved => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        // Recover, show it in the list anyway
        _recentFileManager.updateOpenFiles(fme.getFile());
      }
      // Check class file sync status, in case file was renamed
      if (inDebugMode()) _updateDebugStatus();
    }

    // NOTE: Not necessarily called from event-dispatching thread...
    //  Should figure out how to deal with invokeLater here.
    public void fileOpened(final OpenDefinitionsDocument doc) { 
      // Fix OS X scrollbar bug before switching
      _reenableScrollBar();
      _createDefScrollPane(doc);
      try {
        _recentFileManager.updateOpenFiles(doc.getFile());
      }
      catch (IllegalStateException ise) {
        // Impossible: opened => has a file
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        // Recover, show it in the list anyway
        _recentFileManager.updateOpenFiles(fme.getFile());
      }
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      _removeErrorListener(doc);
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
    
    // NOTE: Not necessarily called from event-dispatching thread...
    //  Should figure out how to deal with invokeLater here.
    public void activeDocumentChanged(final OpenDefinitionsDocument active) {
      // Only change GUI from event-dispatching thread
      // (This can be called from other threads...)
      //Runnable doCommand = new Runnable() {
      // public void run() {
          _switchDefScrollPane();
          
          boolean isModified = active.isModifiedSinceSave();
          boolean canCompile = (!isModified && !active.isUntitled());
          _saveAction.setEnabled(isModified);
          _revertAction.setEnabled(!active.isUntitled());
          
          // Update error highlights
          _errorPanel.getErrorListPane().selectNothing();
          _junitPanel.getJUnitErrorListPane().selectNothing();
          
          int pos = _currentDefPane.getCaretPosition();
          _currentDefPane.getErrorCaretListener().updateHighlight(pos);
          _currentDefPane.getJUnitErrorCaretListener().updateHighlight(pos);
          
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
    
    public void interactionStarted() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          _interactionsPane.setEditable(false);
          _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void interactionEnded() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          if (inDebugMode()) {
            _disableStepTimer();
            Debugger manager = _model.getDebugger();
            manager.clearCurrentStepRequest();
            _removeThreadLocationHighlight();
          }
          
          _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
          _interactionsPane.setEditable(true);
          int pos = _interactionsPane.getDocument().getLength();
          _interactionsPane.setCaretPosition(pos);
          if (_interactionsPane.hasFocus()) {
            _interactionsPane.getCaret().setVisible(true);
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void compileStarted() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          // Is this necessary?
          //ErrorListPane elp = _errorPanel.getErrorListPane();
          //elp.setSize(_tabbedPane.getMinimumSize());
          //_setDividerLocation();

          showTab(_errorPanel);
          _errorPanel.setCompilationInProgress();
          _saveAction.setEnabled(false);
          hourglassOn();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void compileEnded() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          hourglassOff();
          updateErrorListeners();
          _errorPanel.reset();
          _junitPanel.reset();
          if (inDebugMode()) _updateDebugStatus();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void junitStarted(final OpenDefinitionsDocument doc) {
      
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          showTab(_junitPanel);
          _junitPanel.setJUnitInProgress(doc);
          _junitAction.setEnabled(false);
          updateErrorListeners();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }
    
    //public void junitRunning() {}
    
    public void junitSuiteStarted(final int numTests) {
      SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          _junitPanel.progressReset(numTests);
        }
      });
    }
  
    public void junitTestStarted(final OpenDefinitionsDocument doc, final String name) {
      _junitPanel.getJUnitErrorListPane().testStarted(name);
    }
  
    public void junitTestEnded(final OpenDefinitionsDocument doc, final String name,
                               final boolean wasSuccessful, final boolean causedError) {
      // syncUI...?
      SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          _junitPanel.getJUnitErrorListPane().
            testEnded(name, wasSuccessful, causedError);
          _junitPanel.progressStep(wasSuccessful);
        }
      });
    }

    public void junitEnded() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          showTab(_junitPanel);
          _junitAction.setEnabled(true);
          updateErrorListeners();
          _junitPanel.reset();
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void interactionsExited(final int status) {
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
            
            JOptionPane.showMessageDialog(MainFrame.this,
                                          msg,
                                          title,
                                          JOptionPane.INFORMATION_MESSAGE);
          }
        };
        SwingUtilities.invokeLater(doCommand);
      }
    }
    
    public void interactionsResetting() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          Debugger dm = _model.getDebugger();
          if (dm.isAvailable()) {
            dm.shutdown();
          }
          _resetInteractionsAction.setEnabled(false);
          _interactionsPane.setEditable(false);
          _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          if (_model.getDebugger().isAvailable()) {
            _toggleDebuggerAction.setEnabled(false);
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void interactionsReset() {
      // Only change GUI from event-dispatching thread
      Runnable doCommand = new Runnable() {
        public void run() {
          interactionEnded();
          _resetInteractionsAction.setEnabled(true);
          if (_model.getDebugger().isAvailable()) {
            _toggleDebuggerAction.setEnabled(true);
          }
        }
      };
      SwingUtilities.invokeLater(doCommand);
    }

    public void consoleReset() {
    }

    public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
      String message;
      if (reason == COMPILE_REASON) {
        message =
          "To compile, you must first save ALL modified files.\n" +
          "Would you like to save and then compile?";
      }
      else if (reason == JUNIT_REASON) {
        message =
          "To run JUnit, you must first save and compile ALL modified\n" +
          "files. Would like to save and then compile?";
      }
      else if (reason == DEBUG_REASON) {
        message =
          "To use debugging commands, you must first save and compile\n" +
          "ALL modified files. Would like to save and then compile?";
      }
      else {
        throw new RuntimeException("Invalid reason for forcing a save.");
      }
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, message,
                                             "Must save all files to continue",
                                             JOptionPane.YES_NO_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _saveAll();
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

    public void nonTestCase() {

      String message =
        "The  Test  button  (and menu item) in  DrJava invokes the JUnit\n"  +
        "test  harness  over  the currently open document.  In order for\n" +
        "that  to  work,  the  current  document  must  be a valid JUnit\n" +
        "TestCase, i.e., a subclass of junit.framework.TestCase.\n\n" +
        
        "Make  sure  the current  document  has been saved and  compiled\n" + 
        "before using the Test button.\n\n" +

        "For information on how to write JUnit TestCases, view the JUnit\n" +
        "chapter in the User Documentation or the online Help, or visit:\n\n" +

        "  http://www.junit.org/\n\n";

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
  }
 

  /**
   * Prints a display label for each item in the document list.
   */
  private class DocCellRenderer extends DefaultListCellRenderer {
    /**
     * Change the display of the label, but keep other
     * behavior the same.
     */
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean iss,
                                                  boolean chf)
    {
      // Use exisiting behavior
      super.getListCellRendererComponent(list, value, index, iss, chf);

      // Change label
      String label = _model.getDisplayFilename((OpenDefinitionsDocument)value);
      setText(label);

      return this;
    }
  }

  public JViewport getDefViewport() {
    JScrollPane defScroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());
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
    
    for (int i = 0; i < _tabs.size(); i++) {
      tp = (TabbedPanel)_tabs.get(i);
      if (tp == c) {
        // 2 right now is a magic number for the number of tabs always visible
        // interactions & console        
        if (!tp.isDisplayed()) {
          _tabbedPane.insertTab(tp.getName(), null, tp, null, numVisible + 2);
          tp.setDisplayed(true);
        }
        _tabbedPane.setSelectedIndex(numVisible + 2);
        return;
      }
      if (tp.isDisplayed())
        numVisible++;
    }
  }
  
  private void _setDividerLocation() {    
    int divLocation = _mainSplit.getHeight() - 
      _mainSplit.getDividerSize() - 
      (int)_tabbedPane.getMinimumSize().getHeight();
    if (_mainSplit.getDividerLocation() > divLocation)
      _mainSplit.setDividerLocation(divLocation);
  }
  
  private void _warnFileOpen() {
    // If we'd like to change to an error message for this, instead
    // of a warning, change both incidents of WARNING to ERROR.
    JOptionPane.showMessageDialog
      ( MainFrame.this,
       "This file is open in DrJava.  You may not overwrite it.",
       "File Open Warning",
       JOptionPane.WARNING_MESSAGE);
  }
  
  
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
    if (n==JOptionPane.YES_OPTION){ return true;}
    else {return false;}
    
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
   * Builds the Hashtables in KeyBindingManager that are used to keep track
   * of key-bindings and allows for live updating, conflict resolution, and
   * intelligent error messages (the ActionToNameMap)
   */
  private void _setUpKeyBindingMaps() {
    ActionMap _actionMap = _currentDefPane.getActionMap();
    
    KeyBindingManager.Singleton.put(KEY_BACKWARD, _actionMap.get(DefaultEditorKit.backwardAction),null, "Backward");
    KeyBindingManager.Singleton.addShiftAction(KEY_BACKWARD,
                                               DefaultEditorKit.selectionBackwardAction);
    
    KeyBindingManager.Singleton.put(KEY_BEGIN_DOCUMENT, _actionMap.get(DefaultEditorKit.beginAction), null, "Begin Document");
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_DOCUMENT, 
                                               DefaultEditorKit.selectionBeginAction);
    
    KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _actionMap.get(DefaultEditorKit.beginLineAction), null, "Begin Line");
    //KeyBindingManager.Singleton.put(KEY_BEGIN_LINE, _homeAction, 
    //                                null, "Begin Line");
    KeyBindingManager.Singleton.addShiftAction(KEY_BEGIN_LINE, 
                                               DefaultEditorKit.selectionBeginLineAction); 
    
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
    KeyBindingManager.Singleton.put(KEY_COMMENT_LINES, 
                                    _commentLinesAction, null, "Comment Out Line(s)");
    KeyBindingManager.Singleton.put(KEY_UNCOMMENT_LINES, 
                                    _uncommentLinesAction, null, "Uncomment Line(s)");
    KeyBindingManager.Singleton.put(KEY_DELETE_PREVIOUS, 
                                    _actionMap.get(DefaultEditorKit.deletePrevCharAction), null, "Delete Previous");
    KeyBindingManager.Singleton.put(KEY_DELETE_NEXT, 
                                    _actionMap.get(DefaultEditorKit.deleteNextCharAction), null, "Delete Next");
    KeyBindingManager.Singleton.put(KEY_FIND_NEXT,
                                    new AbstractAction("FindNext") {
      public void actionPerformed(ActionEvent ae) {
        if(!_findReplace.isDisplayed()) {
          showTab(_findReplace);
          _findReplace.beginListeningTo(_currentDefPane);
        }
        _findReplace.findNext();
        _currentDefPane.requestFocus();
      }
    }, null, "Find Next");
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
   * The OptionListener for FONT_DOCLIST
   */
  private class DoclistFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      Font doclistFont = DrJava.getConfig().getSetting(FONT_DOCLIST);
      _docList.setFont(doclistFont);
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
    }
  }  
}
