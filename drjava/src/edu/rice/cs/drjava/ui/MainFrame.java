package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.util.UnexpectedException;
import edu.rice.cs.drjava.Version;

/**
 * DrJava's main window.
 * @version $Id$
 */
public class MainFrame extends JFrame {
  private static final int INTERACTIONS_TAB = 0;
  private static final int COMPILE_TAB = 1;
  private static final int OUTPUT_TAB = 2;
  private CompilerErrorPanel _errorPanel;
  private DefinitionsPane _definitionsPane;
  private OpenDefinitionsDocument[] _definitionsDocuments;
  private OutputPane _outputPane;
  private InteractionsPane _interactionsPane;
  private JTextField _fileNameField;
  private JTabbedPane _tabbedPane;
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _helpMenu;
  private GlobalModel _model;
  private FindReplaceDialog _findReplace;
  private JButton _saveButton;
  private JButton _compileButton;
  private JMenuItem _saveMenuItem;
  private JMenuItem _compileMenuItem;

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
      _definitionsDocuments[0].startCompile();
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
      _findReplace.setMachine(
        _definitionsDocuments[0].createFindReplaceMachine());
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
  private Action _clearOutputAction = new AbstractAction("Clear Output") {
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
      final String message = "DrJava, brought to you by the Java PLT "
                           + "research group at Rice University.\n"
                           + "http://www.cs.rice.edu/~javaplt/drjava\n\n"
                           + "Version: "
                           + Version.BUILD_TIME;
      JOptionPane.showMessageDialog(MainFrame.this, message);
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
      _definitionsPane.requestFocus();
    }
  };

  /** Creates the main window, and shows it. */
  public MainFrame() {
    _model = new GlobalModel();
    _model.newFile();
    _definitionsDocuments = _model.getDefinitionsDocuments();
    _openChooser = new JFileChooser(System.getProperty("user.dir"));
    _openChooser.setFileFilter(new JavaSourceFilter());
    _saveChooser = new JFileChooser(System.getProperty("user.dir"));
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    _definitionsPane = new DefinitionsPane(this, _model, _definitionsDocuments[0]);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());
    // Make the menu bar
    _setUpMenuBar();
    _setUpTabs();
    setBounds(0, 0, 700, 700);
    setSize(700, 700);
    _setUpPanes();
    updateFileTitle("Untitled");
    _setAllFonts(new Font("Monospaced", 0, 12));
    _findReplace = new FindReplaceDialog(this, _definitionsPane);
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
   * put your documentation comment here
   * @param filename
   */
  public void updateFileTitle(String filename) {
    setTitle(filename + " - DrJava");
    _fileNameField.setText(filename);
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   */
  public File getOpenFile() throws OperationCanceledException {
    _openChooser.setSelectedFile(null);
    int rc = _openChooser.showOpenDialog(this);
    return getFileName(_openChooser, rc);
  }

  /**
   * Prompt the user to select a place to save the current document.
   */
  public File getSaveFile() throws OperationCanceledException {
    _saveChooser.setSelectedFile(null);
    int rc = _saveChooser.showSaveDialog(this);
    return getFileName(_saveChooser, rc);
  }


  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  void installNewDocumentListener(Document d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
      }
      public void insertUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
      }
      public void removeUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
      }
    });
  }

  private void _new() {
    _model.closeFile(_definitionsDocuments[0]);
    _model.newFile();
    _definitionsDocuments = _model.getDefinitionsDocuments();
  }

  // Set up for single document interface...
  private void _open() {
    final OpenDefinitionsDocument oldDoc = _definitionsDocuments[0];
    try {
      // Check if old file needs to be saved (single doc interface)
      if (oldDoc.isModifiedSinceSave() && !oldDoc.canAbandonFile()) {
        throw new OperationCanceledException();
      }
      _model.openFile(_openSelector);
      _model.closeFile(oldDoc);
    }
    catch (OperationCanceledException oce) {
      // Make sure we still have one doc open
      if (_model.getDefinitionsDocuments().length == 0) {
        _model.newFile();
      }
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
    _definitionsDocuments = _model.getDefinitionsDocuments();
  }

  private void _save() {
    try {
      _definitionsDocuments[0].saveFile(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }


  private void _saveAs() {
    try {
      _definitionsDocuments[0].saveFileAs(_saveSelector);
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

  public File getFileName(JFileChooser fc, int choice)
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
      OpenDefinitionsDocument doc = _definitionsDocuments[0];
      int lineNum = Integer.parseInt(lineStr);
      int pos = doc.gotoLine(lineNum);
      _definitionsPane.setPositionAndScroll(pos);
      _definitionsPane.grabFocus();
    } catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
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

    tmpItem = fileMenu.add(_saveAsAction);
    fileMenu.addSeparator();
    tmpItem = fileMenu.add(_compileAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

    // keep track of the compile menu item
    _compileMenuItem = tmpItem;

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
    /*The undo/redo menus and key action
     //tmpItem = editMenu.add(_definitionsPane.getUndoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
     //                                             ActionEvent.CTRL_MASK));
     //tmpItem = editMenu.add(_definitionsPane.getRedoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
     //                                             ActionEvent.CTRL_MASK));
     editMenu.addSeparator();
     */

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
    _compileButton.setEnabled(true);
  }

  private void _setUpTabs() {
    _outputPane = new OutputPane();
    _errorPanel = new CompilerErrorPanel(_definitionsPane, _model);
    // Make the output view the active one
    _outputPane.makeActive();
    _interactionsPane = new InteractionsPane(_model);
    _tabbedPane = new JTabbedPane();
    _tabbedPane.add("Interactions", new JScrollPane(_interactionsPane));
    _tabbedPane.add("Compiler output", _errorPanel);
    _tabbedPane.add("Console", new JScrollPane(_outputPane));
    // Select interactions pane when interactions tab is selected
    _tabbedPane.addChangeListener(new ChangeListener() {

      /**
       * put your documentation comment here
       * @param e
       */
      public void stateChanged(ChangeEvent e) {
        if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
          _interactionsPane.grabFocus();
        }
      }
    });
  }

  private void _setUpPanes() {
    JScrollPane defScroll = new JScrollPane(_definitionsPane,
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, defScroll,
        _tabbedPane);
    getContentPane().add(split1, BorderLayout.CENTER);
    // This is annoyingly order-dependent. Since split2 contains split1,
    // we need to get split2's divider set up first to give split1 an overall
    // size. Then we can set split1's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    split1.setDividerLocation(2*getHeight()/3);
    //split2.setDividerLocation(50);
  }

  /**
   * put your documentation comment here
   * @param f
   */
  private void _setAllFonts(Font f) {
    _definitionsPane.setFont(f);
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

  private class ModelListener implements GlobalModelListener {
    public void newFileCreated() {
      _definitionsDocuments = _model.getDefinitionsDocuments();
      _definitionsPane.setDocument(_definitionsDocuments[0]);
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(false);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(false);
      updateFileTitle("Untitled");
      installNewDocumentListener(_definitionsDocuments[0].getDocument());
      _definitionsPane.grabFocus();
      _definitionsPane.getHighlighter().removeAllHighlights();
    }

    public void fileSaved(File file) {
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(true);
      updateFileTitle(file.getName());
      _definitionsPane.grabFocus();
    }

    public void fileOpened(File file) {
      _definitionsDocuments = _model.getDefinitionsDocuments();

      // Temporary: Hack for single document interface
      //  When a multiple document interface is supported,
      //  remove this check and change which doc is referenced.
      if (_definitionsDocuments.length != 2) {
        throw new UnexpectedException(new Exception(
          "Error opening file: previous file unexpectedly closed."));
      }
      _definitionsPane.setDocument(_definitionsDocuments[1]);
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(true);
      updateFileTitle(file.getName());
      installNewDocumentListener(_definitionsDocuments[1].getDocument());
      _definitionsPane.grabFocus();
      _definitionsPane.getHighlighter().removeAllHighlights();
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      // context switch to new document, or open one
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
      _errorPanel.resetErrors(_model.getCompileErrors());
      _compileButton.setEnabled(true);
    }

    public void interactionsReset() {
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
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }
    }
  }



}
