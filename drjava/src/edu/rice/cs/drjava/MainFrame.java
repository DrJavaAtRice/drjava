package  edu.rice.cs.drjava;

import  javax.swing.JFrame;
import  javax.swing.JMenuBar;
import  javax.swing.JMenu;
import  javax.swing.Action;
import  javax.swing.AbstractAction;
import  javax.swing.JButton;
import  javax.swing.JEditorPane;
import  javax.swing.JOptionPane;
import  javax.swing.JScrollPane;
import  javax.swing.JSplitPane;
import  javax.swing.BoxLayout;
import  javax.swing.JTextField;
import  javax.swing.KeyStroke;
import  javax.swing.JMenuItem;
import  javax.swing.JComponent;
import  javax.swing.JTabbedPane;
import  javax.swing.text.DefaultEditorKit;
import  javax.swing.event.ChangeListener;
import  javax.swing.event.ChangeEvent;
import  javax.swing.event.DocumentListener;
import  javax.swing.event.DocumentEvent;
import  java.awt.event.ActionEvent;
import  java.awt.event.KeyEvent;
import  java.awt.event.WindowListener;
import  java.awt.event.WindowEvent;
import  java.awt.event.MouseAdapter;
import  java.awt.event.KeyAdapter;
import  java.awt.BorderLayout;
import  java.awt.Label;
import  java.awt.Cursor;
import  java.awt.Font;
import  java.io.File;


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
  JButton _saveButton;
  JButton _compileButton;
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

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      boolean wantToExit = true;
      if (_definitionsPane.isModifiedSinceSave()) {
        wantToExit = _definitionsPane.checkAbandoningChanges();
      }
      if (wantToExit) {
        System.exit(0);
      }
    }
  };
  private Action _openAction = new AbstractAction("Open") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      boolean opened = _definitionsPane.open();
      if (opened) {
        _resetInteractions();
        _errorPanel.resetErrors(new CompilerError[0]);
        _saveButton.setEnabled(false);
        _compileButton.setEnabled(true);
      }
    }
  };
  private Action _newAction = new AbstractAction("New") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      boolean createdNew = _definitionsPane.newFile();
      if (createdNew) {
        _resetInteractions();
        _errorPanel.resetErrors(new CompilerError[0]);
        _saveButton.setEnabled(false);
        _compileButton.setEnabled(true);
      }
    }
  };
  private Action _gotoLineAction = new AbstractAction("Goto line") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      _definitionsPane.gotoLine();
    }
  };

  /**
   * put your documentation comment here
   * @param fileName
   * @return 
   */
  boolean saveToFile(String fileName) {
    boolean result = _definitionsPane.saveToFile(fileName);
    if (result) {
      updateEnablesAfterSave();
    }
    return  result;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  boolean save() {
    boolean result = _definitionsPane.save();
    if (result) {
      updateEnablesAfterSave();
    }
    return  result;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  boolean saveAs() {
    boolean result = _definitionsPane.saveAs();
    if (result) {
      updateEnablesAfterSave();
    }
    return  result;
  }

  /**
   * put your documentation comment here
   */
  void updateEnablesAfterSave() {
    _saveButton.setEnabled(false);
    _compileButton.setEnabled(true);
  }
  private Action _saveAction = new AbstractAction("Save") {

    // This doesn't seem to ever re-enable once disabled!
    /*
     public boolean isEnabled() {
     return ! _definitionsPane.isModifiedSinceSave();
     }
     */
    public void actionPerformed(ActionEvent ae) {
      if (_definitionsPane.getCurrentFileName() == "")
        saveAs(); 
      else 
        saveToFile(_definitionsPane.getCurrentFileName());
    }
  };
  private Action _saveAsAction = new AbstractAction("Save as") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      saveAs();
    }
  };

  /**
   * put your documentation comment here
   */
  void compile() {
    _compileButton.setEnabled(false);
    String filename = _definitionsPane.getCurrentFileName();
    if (filename.length() == 0) {
      // the file has never been saved. we can only get here
      // if the file was never changed and never saved.
      return;
    }

    try {
      _outputPane.clear();
      repaint();

      File sourceRoot = _definitionsPane.getSourceRoot();
      _resetInteractions(sourceRoot);
      
      File[] files = new File[] { new File(filename) };
      CompilerError[] errors = DrJava.compiler.compile(sourceRoot, files);
      _errorPanel.resetErrors(errors);
    }
    catch (InvalidPackageException e) {
      CompilerError err = new CompilerError(filename,
                                            -1,
                                            -1,
                                            e.getMessage(),
                                            false);
      _errorPanel.resetErrors(new CompilerError[] { err });
    }
  }

  private Action _compileAction = new AbstractAction("Compile") {

    // This doesn't seem to ever re-enable once disabled!
    /*
     public boolean isEnabled() {
     return _definitionsPane.getDocument().getLength() > 0;
     }
     */
    public void actionPerformed(ActionEvent ae) {
      boolean modified = _definitionsPane.isModifiedSinceSave();
      if (modified) {
        // file was not saved -- tell user they must save before compiling
        String msg = "The definitions must be saved before compiling. " + "Would you like to save and compile now?";
        int rc = JOptionPane.showConfirmDialog(MainFrame.this, msg, "File not saved", 
            JOptionPane.YES_NO_OPTION);
        if (rc == JOptionPane.YES_OPTION) {
          save();
          // Check if they cancelled the save. If they did, exit!
          if (_definitionsPane.isModifiedSinceSave()) {
            return;
          }
        } 
        else {
          return;               // user wants to do nothing
        }
      }

      _tabbedPane.setSelectedIndex(COMPILE_TAB);
      _errorPanel.setCompilationInProgress();

      hourglassOn();
      compile();
      hourglassOff();
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

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      _outputPane.clear();
    }
  };
  private Action _resetInteractionsAction = new AbstractAction("Reset interactions") {

    /**
     * put your documentation comment here
     * @param ae
     */
    public void actionPerformed(ActionEvent ae) {
      _interactionsPane.reset();
    }
  };

  /**
   * Reset the interactions window, and add the source directory
   * of the file we just compiled to the class path.
   */
  private void _resetInteractions(File sourceRoot) {
    _interactionsPane.reset();
    _interactionsPane.addClassPath(sourceRoot.getAbsolutePath());

    try {
      _interactionsPane.setPackageScope(_definitionsPane.getPackageName());
    }
    catch (InvalidPackageException ipe) {
      // oh well, can't set package scope.
    }
  }

  private void _resetInteractions() {
    _interactionsPane.reset();

    try {
      String path = _definitionsPane.getSourceRoot().getAbsolutePath();
      _interactionsPane.addClassPath(path);
      _interactionsPane.setPackageScope(_definitionsPane.getPackageName());
    }
    catch (InvalidPackageException ipe) {
      // oh well, we can't add to the class path.
    }
  }

  private WindowListener _windowCloseListener = new WindowListener() {

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowActivated(WindowEvent ev) {}

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowClosed(WindowEvent ev) {}

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowClosing(WindowEvent ev) {
      boolean wantToExit = true;
      if (_definitionsPane.isModifiedSinceSave()) {
        wantToExit = _definitionsPane.checkAbandoningChanges();
      }
      if (wantToExit) {
        System.exit(0);
      }
    }

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowDeactivated(WindowEvent ev) {}

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowDeiconified(WindowEvent ev) {}

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowIconified(WindowEvent ev) {}

    /**
     * put your documentation comment here
     * @param ev
     */
    public void windowOpened(WindowEvent ev) {
      _definitionsPane.requestFocus();
    }
  };

  /**
   * put your documentation comment here
   * @param d
   */
  void installNewDocumentListener(DefinitionsDocument d) {
    d.addDocumentListener(new DocumentListener() {

      /**
       * put your documentation comment here
       * @param e
       */
      public void changedUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
      }

      /**
       * put your documentation comment here
       * @param e
       */
      public void insertUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
      }

      /**
       * put your documentation comment here
       * @param e
       */
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
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    _fileNameField = new JTextField();
    _fileNameField.setEditable(false);
    _definitionsPane = new DefinitionsPane(this);
    _outputPane = new OutputPane();
    _errorPanel = new CompilerErrorPanel(_definitionsPane);
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(_windowCloseListener);
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
}



