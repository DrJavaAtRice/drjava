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
import java.awt.event.*;
import java.awt.*;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.DelegatingAction;

/**
 * DrJava's main window.
 * @version $Id$
 */
public class MainFrame extends JFrame {
  private static final int INTERACTIONS_TAB = 0;
  private static final int COMPILE_TAB = 1;
  private static final int OUTPUT_TAB = 2;

  // GUI Dimensions
  private static final int GUI_WIDTH = 800;
  private static final int GUI_HEIGHT = 700;
  private static final int DOC_LIST_WIDTH = 150;

  private final SingleDisplayModel _model;

  private Hashtable _defScrollPanes;
  private DefinitionsPane _currentDefPane;

  // These should be final but can't be, as the code is currently organized,
  // because they are not set in the constructor
  private CompilerErrorPanel _errorPanel;
  private OutputPane _outputPane;
  private InteractionsPane _interactionsPane;
  
  private JTextField _fileNameField;
  private JTabbedPane _tabbedPane;
  private JSplitPane _docSplitPane;
  private JList _docList;
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _helpMenu;
  private FindReplaceDialog _findReplace;
  private JButton _saveButton;
  private JButton _compileButton;
  private JMenuItem _saveMenuItem;
  private JMenuItem _compileMenuItem;
  private JMenuItem _abortInteractionMenuItem;

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

  private FileOpenSelector _openSelector = new FileOpenSelector() {
    public File getFile() throws OperationCanceledException {
      return getOpenFile();
    }
  };

  private FileSaveSelector _saveSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException {
      return getSaveFile();
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
  private Action _openAction = new AbstractAction("Open") {
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
  private Action _saveAsAction = new AbstractAction("Save as") {
    public void actionPerformed(ActionEvent ae) {
      _saveAs();
    }
  };

  /** Compiles the document in the definitions pane. */
  private Action _compileAction = new AbstractAction("Compile") {
    public void actionPerformed(ActionEvent ae) {
      _compile();
    }
  };

  /** Undoes the last change to the active definitions document. */
  private DelegatingAction _undoAction = new DelegatingAction();

  /** Redoes the last undo to the active definitions document. */
  private DelegatingAction _redoAction = new DelegatingAction();

  /** Aborts current interaction. */
  private Action _abortInteractionAction
    = new AbstractAction("Abort current interaction")
  {
    public void actionPerformed(ActionEvent ae) {
      String title = "Confirm abort interaction";

      String message = "Are you sure you would like to abort the" +
                       "current interaction?";

      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.YES_OPTION) {
        _model.abortCurrentInteraction();
      }
    }
  };

  /** Closes the program. */
  private Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) {
      _model.quit();
    }
  };

  /** Opens the find/replace dialog. */
  private Action _findReplaceAction = new AbstractAction("Find/Replace") {
    public void actionPerformed(ActionEvent ae) {
      _findReplace.setMachine(_currentDefPane);
      _findReplace.show();
    }
  };

  /** Asks the user for a line number and goes there. */
  private Action _gotoLineAction = new AbstractAction("Goto line") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
    }
  };

  /** Clears DrJava's output console. */
  private Action _clearOutputAction = new AbstractAction("Clear Console") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
    }
  };

  /** Clears the interactions console. */
  private Action _resetInteractionsAction =
    new AbstractAction("Reset interactions")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.resetInteractions();
    }
  };

  /** Pops up an info dialog. */
  private Action _aboutAction = new AbstractAction("About") {

    public void actionPerformed(ActionEvent ae) {
      //JOptionPane.showMessageDialog(MainFrame.this, _model.getAboutText());
      new AboutDialog(MainFrame.this, _model.getAboutText()).show();
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

  /** How DrJava responds to window events. */
  private WindowListener _windowCloseListener = new WindowListener() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _model.quit();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {}
    public void windowIconified(WindowEvent ev) {}
    public void windowOpened(WindowEvent ev) {
      _currentDefPane.requestFocus();
    }
  };

  /** Creates the main window, and shows it. */
  public MainFrame() {
    _model = new SingleDisplayModel();
    String userdir = System.getProperty("user.dir");
    _openChooser = new JFileChooser(userdir);
    _openChooser.setFileFilter(new JavaSourceFilter());
    _saveChooser = new JFileChooser(userdir);
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());
    _setUpTabs();

    // DefinitionsPane
    _defScrollPanes = new Hashtable();
    JScrollPane defScroll = _createDefScrollPane(_model.getActiveDocument());
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();

    // Need to set undo/redo actions to point to the initial def pane
    // on switching documents later these pointers will also switch
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());

    _errorPanel.getErrorListPane().setLastDefPane(_currentDefPane);
    _errorPanel.reset();

    // Make the menu bar
    _setUpMenuBar();
    _setUpDocumentSelector();

    setBounds(0, 0, GUI_WIDTH, GUI_HEIGHT);
    setSize(GUI_WIDTH, GUI_HEIGHT);

    _setUpPanes();
    updateFileTitle();
    _setAllFonts(new Font("Monospaced", 0, 12));
    _docList.setFont(new Font("Monospaced", 0, 10));
    _findReplace = new FindReplaceDialog(this, _currentDefPane);
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
   * Updates the title bar with the name of the active document.
   */
  public void updateFileTitle() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    String filename = _model.getDisplayFilename(doc);
    setTitle(filename + " - DrJava");
    _fileNameField.setText(_model.getDisplayFullPath(doc));
    _docList.repaint();
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   */
  public File getOpenFile() throws OperationCanceledException {
    _openChooser.setSelectedFile(null);
    int rc = _openChooser.showOpenDialog(this);
    return getChosenFile(_openChooser, rc);
  }

  /**
   * Prompt the user to select a place to save the current document.
   */
  public File getSaveFile() throws OperationCanceledException {
    _saveChooser.setSelectedFile(null);
    int rc = _saveChooser.showSaveDialog(this);
    return getChosenFile(_saveChooser, rc);
  }

  /**
   * Returns the current DefinitionsPane.
   */
  public DefinitionsPane getCurrentDefPane() {
    return _currentDefPane;
  }


  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  private void _installNewDocumentListener(Document d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
      public void insertUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
      public void removeUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
    });
  }


  private void _new() {
    _model.newFile();
  }

  private void _open() {
    try {
      _model.openFile(_openSelector);
    }
    catch (AlreadyOpenException aoe) {
      // Switch to existing copy after prompting user
      OpenDefinitionsDocument openDoc = aoe.getOpenDocument();
      String filename = "File";
      try {
        filename = openDoc.getFile().getName();
      }
      catch (IllegalStateException ise) {
        // Can't happen: this open document must have a file
        throw new UnexpectedException(ise);
      }
      String title = "File already open";
      String message = filename + " is already open.\n" +
        "Click OK to switch to the open copy\n" +
        "or Cancel to return to the previous file.";
      int choice = JOptionPane.showConfirmDialog(this,
                                                 message,
                                                 title,
                                                 JOptionPane.OK_CANCEL_OPTION);
      if (choice == JOptionPane.OK_OPTION) {

        _model.setActiveDocument(openDoc);
      }
    }
    catch (OperationCanceledException oce) {
      // Ok, don't open a file
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _close() {
    _model.closeFile(_model.getActiveDocument());
  }

  private void _closeAll() {
    _model.closeAllFiles();
  }


  private void _save() {
    try {
      _model.getActiveDocument().saveFile(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }


  private void _saveAs() {
    try {
      _model.getActiveDocument().saveFileAs(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _compile() {
    try {
      _model.getActiveDocument().startCompile();
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _showIOError(IOException ioe) {
    JOptionPane.showMessageDialog(this,
                                  "An I/O exception occurred during the last operation.\n" + ioe,
                                  "Input/output error",
                                  JOptionPane.ERROR_MESSAGE);
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
        if (chosen != null)
          return chosen;
        else
          throw new RuntimeException("filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
    }
  }

  /**
   * Ask the user what line they'd like to jump to, then go there.
   */
  private void _gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Jump to line";
    String lineStr = JOptionPane.showInputDialog(this,
                                                 msg,
                                                 title,
                                                 JOptionPane.QUESTION_MESSAGE);
    try {
      int lineNum = Integer.parseInt(lineStr);
      int pos = _model.getActiveDocument().gotoLine(lineNum);
      _currentDefPane.setPositionAndScroll(pos);
      _currentDefPane.grabFocus();
    } catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
  }

  /**
   * Update all appropriate listeners that the CompilerErrorModels
   * have changed.
   */
  private void _updateErrorListeners() {
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
    }
  }

  /**
   * Sets up the components of the menu bar and links them to the private
   * fields within MainFrame.  This method serves to make the code
   * more legible on the higher calling level, i.e., the constructor.
   */
  private void _setUpMenuBar() {
    _menuBar = new JMenuBar();
    _fileMenu = _setUpFileMenu();
    _editMenu = _setUpEditMenu();
    _helpMenu = _setUpHelpMenu();

    // Menu bars can actually hold anything!
    _fileNameField = new JTextField();
    _fileNameField.setEditable(false);
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_helpMenu);
    _menuBar.add(_fileNameField);
    _setUpMenuBarButtons();
    setJMenuBar(_menuBar);
  }


  /**
   * Creates and returns a file menu.  Side effects: sets values for
   * _saveMenuItem and _compileMenuItem.
   */
  private JMenu _setUpFileMenu() {
    JMenuItem tmpItem;
    JMenu fileMenu = new JMenu("File");
    tmpItem = fileMenu.add(_newAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = fileMenu.add(_openAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = fileMenu.add(_saveAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                  ActionEvent.CTRL_MASK));

    // keep track of the save menu item
    _saveMenuItem = tmpItem;
    _saveMenuItem.setEnabled(false);

    tmpItem = fileMenu.add(_saveAsAction);

    tmpItem = fileMenu.add(_closeAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                  ActionEvent.CTRL_MASK));

    tmpItem = fileMenu.add(_closeAllAction);
    fileMenu.addSeparator();

    tmpItem = fileMenu.add(_compileAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

    // keep track of the compile menu item
    _compileMenuItem = tmpItem;
    _compileMenuItem.setEnabled(false);

    _abortInteractionAction.setEnabled(false);
    _abortInteractionMenuItem = fileMenu.add(_abortInteractionAction);
    _abortInteractionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));

    fileMenu.addSeparator();
    tmpItem = fileMenu.add(_quitAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                  ActionEvent.CTRL_MASK));
    return fileMenu;
  }

  /**
   * Creates and returns a edit menu.
   */
  private JMenu _setUpEditMenu() {
    JMenuItem tmpItem;
    JMenu editMenu = new JMenu("Edit");

    tmpItem = editMenu.add(_undoAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(_redoAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                  ActionEvent.CTRL_MASK));
    editMenu.addSeparator();

    // set up the actions for cut/copy/paste with regards to menu
    // items and keystrokers.
    Action cutAction = new DefaultEditorKit.CutAction();
    cutAction.putValue(Action.NAME, "Cut");
    Action copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.NAME, "Copy");
    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.NAME, "Paste");

    tmpItem = editMenu.add(cutAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(copyAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(pasteAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                  ActionEvent.CTRL_MASK));
    editMenu.addSeparator();
    tmpItem = editMenu.add(_findReplaceAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(_gotoLineAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                                  ActionEvent.CTRL_MASK));
    editMenu.add(_clearOutputAction);
    editMenu.add(_resetInteractionsAction);


    /** TEMPORARY */
    editMenu.addSeparator();
    tmpItem = editMenu.add(_switchToNextAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(_switchToPrevAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,
                                                  ActionEvent.CTRL_MASK));
    // END TEMPORARY

    // Add the menus to the menu bar
    return editMenu;
  }

  /**
   * Creates and returns a help menu.
   */
  private JMenu _setUpHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(_aboutAction);
    return helpMenu;
  }

  /**
   * Sets up the save and compile buttons on the menu bar.
   */
  private void _setUpMenuBarButtons() {
    // Add buttons.
    _saveButton = new JButton(_saveAction);
    _saveButton.setEnabled(false);
    _menuBar.add(_saveButton);
    _compileButton = new JButton(_compileAction);
    _menuBar.add(_compileButton);
    _compileButton.setEnabled(false);
  }

  private void _setUpTabs() {
    _outputPane = new OutputPane(_model);
    _errorPanel = new CompilerErrorPanel(_model, this);
    _interactionsPane = new InteractionsPane(_model);
    _tabbedPane = new JTabbedPane();
    _tabbedPane.add("Interactions", new BorderlessScrollPane(_interactionsPane));
    _tabbedPane.add("Compiler output", _errorPanel);
    _tabbedPane.add("Console", new BorderlessScrollPane(_outputPane));
    
    // Select interactions pane when interactions tab is selected
    _tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
          _interactionsPane.grabFocus();
        }
      }
    });
  }

  /**
   * Configures the component used for selecting active documents.
   */
  private void _setUpDocumentSelector() {
    _docList = new JList(_model.getDefinitionsDocuments());
    /* {
      public String getToolTipText(MouseEvent event) {
        Point location = event.getPoint();
        int index = locationToIndex(location);
        String tip = null;
        if (index >= 0) {
          tip = _model.getDisplayFullPath(index);
        }
        return tip;
      }
    };
    _docList.setToolTipText("Document List"); */

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
      new CompilerErrorCaretListener(doc, _errorPanel.getErrorListPane(), pane);
    pane.addErrorCaretListener(caretListener);

    // Add to a scroll pane
    JScrollPane scroll = new BorderlessScrollPane(pane,
						  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null); // removes all default borders (MacOS X installs default borders)
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

    // Overall layout
    _docSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   true,
                                   listScroll,
                                   defScroll);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                       true,
                                       _docSplitPane,
                                       _tabbedPane);
    getContentPane().add(split, BorderLayout.CENTER);
    // This is annoyingly order-dependent. Since split2 contains split1,
    // we need to get split2's divider set up first to give split1 an overall
    // size. Then we can set split1's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    split.setDividerLocation(2*getHeight()/3);
    split.setOneTouchExpandable(true);
    _docSplitPane.setDividerLocation(DOC_LIST_WIDTH);
    _docSplitPane.setOneTouchExpandable(true);
  }

  /**
   * Switch to the JScrollPane containing the DefinitionsPane
   * for the current active document.
   */
  private void _switchDefScrollPane() {
    JScrollPane scroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());

    if (scroll == null) {
      throw new UnexpectedException(new Exception(
        "Current definitions scroll pane not found."));
    }

    int oldLocation = _docSplitPane.getDividerLocation();
    _docSplitPane.setRightComponent(scroll);
    _docSplitPane.setDividerLocation(oldLocation);
    _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();

    // reset the undo/redo menu items
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());
  }

  /**
   * Sets the current directory to be that of the given file.
   */
  private void _setCurrentDirectory(OpenDefinitionsDocument doc) {
    try {
      File file = doc.getFile();
      _openChooser.setCurrentDirectory(file);
      _saveChooser.setCurrentDirectory(file);
    }
    catch (IllegalStateException ise) {
      // no file, leave in current directory
    }
  }

  /**
   * put your documentation comment here
   * @param f
   */
  private void _setAllFonts(Font f) {
    _currentDefPane.setFont(f);
    _interactionsPane.setFont(f);
    _outputPane.setFont(f);
    _errorPanel.setListFont(f);
  }
  /**
   * put your documentation comment here
   */
  private class GlassPane extends JComponent {

    /**
     * put your documentation comment here
     */
    public GlassPane() {
      addKeyListener(new KeyAdapter() {});
      addMouseListener(new MouseAdapter() {});
      super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
  }

  private class ModelListener implements SingleDisplayModelListener {
    public void newFileCreated(OpenDefinitionsDocument doc) {
      _createDefScrollPane(doc);
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(true);
      updateFileTitle();
      _currentDefPane.grabFocus();
    }

    public void fileOpened(OpenDefinitionsDocument doc) {
      _createDefScrollPane(doc);
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      _removeErrorListener(doc);
      _defScrollPanes.remove(doc);
    }

    public void activeDocumentChanged(OpenDefinitionsDocument active) {
      _switchDefScrollPane();

      boolean isModified = active.isModifiedSinceSave();
      boolean canCompile = (!isModified && !active.isUntitled());
      _saveButton.setEnabled(isModified);
      _compileButton.setEnabled(canCompile);
      _saveMenuItem.setEnabled(isModified);
      _compileMenuItem.setEnabled(canCompile);

      // Update error highlights
      _errorPanel.getErrorListPane().selectNothing();
      int pos = _currentDefPane.getCaretPosition();
      _currentDefPane.getErrorCaretListener().updateHighlight(pos);

      _setCurrentDirectory(active);

      updateFileTitle();
      _currentDefPane.grabFocus();
    }

    public void interactionStarted() {
      _interactionsPane.setEditable(false);
      _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      _abortInteractionMenuItem.setEnabled(true);
    }

    public void interactionEnded() {
      _abortInteractionMenuItem.setEnabled(false);
      _interactionsPane.setCursor(null);
      _interactionsPane.setEditable(true);
      int pos = _interactionsPane.getDocument().getLength();
      _interactionsPane.setCaretPosition(pos);
    }

    public void compileStarted() {
      _tabbedPane.setSelectedIndex(COMPILE_TAB);
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(false);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(false);
      hourglassOn();
    }

    public void compileEnded() {
      hourglassOff();
      _updateErrorListeners();
      _errorPanel.reset();
      _compileButton.setEnabled(true);
    }

    public void interactionsExited(int status) {
      String msg = "The interactions window was terminated by a call " +
                   "to System.exit(" + status + ").\n" + 
                   "The interactions window will now be restarted.";

      String title = "Interactions terminated by System.exit(" + status + ")";

      JOptionPane.showMessageDialog(MainFrame.this,
                                    msg,
                                    title,
                                    JOptionPane.INFORMATION_MESSAGE);

      // we don't restore the interactions pane to life, since
      // the interactionsReset event will do it.
    }

    public void interactionsReset() {
      interactionEnded();
    }

    public void consoleReset() {
    }

    public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
      String message;
      if (reason == COMPILE_REASON) {
        message = "To compile, you must first save the current file." +
          "Would you like to save and then compile?";
      }
      else {
        throw new RuntimeException("Invalid reason for forcing a save.");
      }
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, message, "Must save to continue", JOptionPane.YES_NO_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _save();
          break;
        case JOptionPane.NO_OPTION:
          // do nothing
          break;
        default:
          throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
      }
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
        fname = "untitled file";
      }

      String text = fname + " has been modified. Would you like to " + "save?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
          "Would you like to save " + fname + "?",
          text,
          JOptionPane.YES_NO_CANCEL_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          _save();
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

}
