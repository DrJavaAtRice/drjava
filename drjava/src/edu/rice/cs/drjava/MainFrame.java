package  edu.rice.cs.drjava;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import java.io.File;
import java.io.IOException;

/** 
 * DrJava's main window.
 * @version $Id$
 * Main DrJava window.
 * It has a menu and then a scroll pane with three components:
 *   Definitions, Output and Interactions. 
 */
public class MainFrame extends JFrame {
  private static final int INTERACTIONS_TAB = 0;
  private static final int COMPILE_TAB = 1;
  private static final int OUTPUT_TAB = 2;
  private CompilerErrorPanel _errorPanel;
  private DefinitionsPane _definitionsPane;
  private OutputPane _outputPane;
  InteractionsPane _interactionsPane;
  private JTextField _fileNameField;
  private JTabbedPane _tabbedPane;
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _helpMenu;
  private GlobalModel _model;
  JButton _saveButton;
  JButton _compileButton;
  
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
  
  // Make some actions for menus
  private Action _aboutAction = new AbstractAction("About") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      final String message = "DrJava, brought to you by the Java PLT "
                           + "research group at Rice University.\n"
                           + "http://www.cs.rice.edu/~javaplt/drjava\n\n" 
                           + "Version: " 
                           + Version.BUILD_TIME;
      JOptionPane.showMessageDialog(MainFrame.this, message);
    }
  };
  
  private Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) {
      _model.quit();
    }
  };
  
  private Action _openAction = new AbstractAction("Open") {
    public void actionPerformed(ActionEvent ae) {
      _open();
    }
  };  
  
  private Action _newAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
      _model.newFile();
    }
  };

  private Action _gotoLineAction = new AbstractAction("Goto line") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
    }
  };
 
  private Action _saveAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _save();
    }
  };
    
  private Action _saveAsAction = new AbstractAction("Save as") {
    public void actionPerformed(ActionEvent ae) {
      _saveAs();
    }
  };

  private Action _compileAction = new AbstractAction("Compile") {
    public void actionPerformed(ActionEvent ae) {
      _model.startCompile();
    }
  };
  
  private Action _findReplaceAction = new AbstractAction("Find/Replace") {
    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      _definitionsPane.findReplace();
    }
  };
  
  private Action _clearOutputAction = new AbstractAction("Clear Output") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
    }
  };

  private Action _resetInteractionsAction = new AbstractAction("Reset interactions") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetInteractions();
    }
  };

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

  void installNewDocumentListener(DefinitionsDocument d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
      }
      public void insertUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
      }
      public void removeUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
      }
    });
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

  /**
   * put your documentation comment here
   */
  public void hourglassOn() {
    getGlassPane().setVisible(true);
  }

  /**
   * put your documentation comment here
   */
  public void hourglassOff() {
    getGlassPane().setVisible(false);
  }

  /** Creates the main window, and shows it. */
  public MainFrame() {
    _model = new GlobalModel();
    _openChooser = new JFileChooser(System.getProperty("user.dir"));
    _openChooser.setFileFilter(new JavaSourceFilter());
    _saveChooser = new JFileChooser(System.getProperty("user.dir"));
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    _fileNameField = new JTextField();
    _fileNameField.setEditable(false);
    _definitionsPane = new DefinitionsPane(this, _model);
    _outputPane = new OutputPane();
    _errorPanel = new CompilerErrorPanel(_definitionsPane);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());
    // Make the menu bar
    _menuBar = new JMenuBar();
    _fileMenu = new JMenu("File");
    _editMenu = new JMenu("Edit");
    _helpMenu = new JMenu("Help");
    // Add items to menus
    _helpMenu.add(_aboutAction);
    JMenuItem tmpItem = _fileMenu.add(_newAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    tmpItem = _fileMenu.add(_openAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    tmpItem = _fileMenu.add(_saveAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    tmpItem = _fileMenu.add(_saveAsAction);
    _fileMenu.addSeparator();
    tmpItem = _fileMenu.add(_compileAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    _fileMenu.addSeparator();
    tmpItem = _fileMenu.add(_quitAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    Action cutAction = new DefaultEditorKit.CutAction();
    cutAction.putValue(Action.NAME, "Cut");
    Action copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.NAME, "Copy");
    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.NAME, "Paste");
    /*The undo/redo menus and key action
     //tmpItem = _editMenu.add(_definitionsPane.getUndoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
     //                                             ActionEvent.CTRL_MASK));    
     //tmpItem = _editMenu.add(_definitionsPane.getRedoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
     //                                             ActionEvent.CTRL_MASK));
     _editMenu.addSeparator();
     */
    tmpItem = _editMenu.add(cutAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    tmpItem = _editMenu.add(copyAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    tmpItem = _editMenu.add(pasteAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
    _editMenu.addSeparator();
    tmpItem = _editMenu.add(_findReplaceAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
    tmpItem = _editMenu.add(_gotoLineAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
    _editMenu.add(_clearOutputAction);
    _editMenu.add(_resetInteractionsAction);
    // Add the menus to the menu bar
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_helpMenu);
    // Menu bars can actually hold anything!
    _menuBar.add(_fileNameField);
    // Add buttons.
    _saveButton = new JButton(_saveAction);
    _saveButton.setEnabled(false);
    _menuBar.add(_saveButton);
    _compileButton = new JButton(_compileAction);
    _menuBar.add(_compileButton);
    _compileButton.setEnabled(false);
    setJMenuBar(_menuBar);
    // Make the output view the active one
    _outputPane.makeActive();
    _interactionsPane = new InteractionsPane();
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
    JScrollPane defScroll = new JScrollPane(_definitionsPane,
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, defScroll, 
        _tabbedPane);
    setBounds(0, 0, 700, 700);
    getContentPane().add(split1, BorderLayout.CENTER);
    setSize(700, 700);
    // This is annoyingly order-dependent. Since split2 contains split1,
    // we need to get split2's divider set up first to give split1 an overall
    // size. Then we can set split1's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    split1.setDividerLocation(2*getHeight()/3);
    //split2.setDividerLocation(50);
    updateFileTitle("Untitled");
    _setAllFonts(new Font("Monospaced", 0, 12));
  }


  GlobalModel getGlobalModel() {
    return _model;
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
   * @param filename
   */
  public void updateFileTitle(String filename) {
    setTitle(filename + " - DrJava");
    _fileNameField.setText(filename);
  }

  /**
   * put your documentation comment here
   * @return 
   */
  DefinitionsPane getDefPane() {
    return  _definitionsPane;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  OutputPane getOutPane() {
    return  _outputPane;
  }
  
  /** Prompt the user to select a place to open a file from, then load it.
   *  Ask the user if they'd like to save previous changes (if the current
   *  document has been modified) before opening.
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
  
  private void _open() {
    try {
      _model.openFile(_openSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _save() {
    try {
      _model.saveFile(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }


  private void _saveAs() {
    try {
      _model.saveFileAs(_saveSelector);
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

  public File getFileName(JFileChooser fc, int choice) throws OperationCanceledException {
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
      int pos = _model.gotoLine(lineNum);
      _definitionsPane.setPositionAndScroll(pos);
      _definitionsPane.grabFocus();
    } catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
  }

  private class ModelListener implements GlobalModelListener {
    public void newFileCreated() { 
      _definitionsPane.setDocument(_model.getDefinitionsDocument());
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(false);
      _fileNameField.setText("Untitled");
      installNewDocumentListener((DefinitionsDocument)_model.getDefinitionsDocument());
      _definitionsPane.grabFocus();
    }
    
    public void fileSaved(File file) { 
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _fileNameField.setText(file.getName());
    }
    
    public void fileOpened(File file) {    
      _definitionsPane.setDocument(_model.getDefinitionsDocument());
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _fileNameField.setText(file.getName());
      installNewDocumentListener((DefinitionsDocument)_model.getDefinitionsDocument());
      _definitionsPane.grabFocus();
    }
    
    public void compileStarted() { 
      _tabbedPane.setSelectedIndex(COMPILE_TAB);
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(false);
      hourglassOn();
    }
    
    public void compileEnded() {
      hourglassOff();
      _errorPanel.resetErrors(_model.getCompileErrors());
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
    public boolean canAbandonFile(File file) {
      String fname;

      if (file == null) {
        fname = "untitled file";
      }
      else {
        fname = file.getName();
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
