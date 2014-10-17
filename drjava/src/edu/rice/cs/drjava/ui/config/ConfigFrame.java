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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.util.TreeMap;
import java.util.Iterator;

import javax.swing.tree.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.ProcessingDialog;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.reflect.JavaVersion;

import static edu.rice.cs.drjava.ui.config.ConfigDescriptions.*;
import static edu.rice.cs.drjava.config.OptionConstants.*;

/** The frame for setting Configuration options on the fly
 *  @version $Id: ConfigFrame.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ConfigFrame extends SwingFrame {

  private static final int FRAME_WIDTH = 850;
  private static final int FRAME_HEIGHT = 550;

  private final MainFrame _mainFrame;

//  private JSplitPane _splitPane;
  private final JTree _tree;
  private final DefaultTreeModel _treeModel;
  private final PanelTreeNode _rootNode;

  private final JButton _okButton;
  private final JButton _applyButton;
  private final JButton _cancelButton;
//  private final JButton _saveSettingsButton;
  private final JPanel _mainPanel;
  private final JFileChooser _fileOptionChooser;
  private final JFileChooser _browserChooser;
  private final JFileChooser _jarChooser;
  private final DirectoryChooser _dirChooser;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<Boolean> _junitLocationEnabledListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<File> _junitLocationListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<String> _concJUnitChecksEnabledListener;
  private final ConfigOptionListeners.RequiresInteractionsRestartListener<File> _rtConcJUnitLocationListener;
    
  private StringOptionComponent javadocCustomParams;
  
  protected final String SEPS = " \t\n-,;.(";
  
  private OptionComponent.ChangeListener _changeListener = new OptionComponent.ChangeListener() {
    public Object value(Object oc) {
      _applyButton.setEnabled(true);
      return null;
    }
  };
  
  /** Sets up the frame and displays it.  This a Swing view class!  With the exception of initialization,
   *  this code should only be executed in the event-handling thread. */
  public ConfigFrame(MainFrame frame) {
    super("Preferences");

    _mainFrame = frame;
    _junitLocationEnabledListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<Boolean>(this, "Use External JUnit");
    _junitLocationListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<File>(this, "JUnit Location");
    _concJUnitChecksEnabledListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<String>(this, "Enabled ConcJUnit Checks");
    _rtConcJUnitLocationListener = new ConfigOptionListeners.
      RequiresInteractionsRestartListener<File>(this, "ConcJUnit Runtime Location");
    
    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        try {
//          _mainFrame.enableResetInteractions();
          saveSettings(); 
          _applyButton.setEnabled(false); 
          
        }
        catch (IOException ioe) {
        }
      }
    };

    _applyButton = new JButton(applyAction);
    _applyButton.setEnabled(false);
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        try {
//          _mainFrame.enableResetInteractions();
          successful = saveSettings();
        }
        catch (IOException ioe) {
          // oh well...
        }
        if (successful) _applyButton.setEnabled(false);
        ConfigFrame.this.setVisible(false);
      }
    };
    _okButton = new JButton(okAction);


    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    };
    _cancelButton = new JButton(cancelAction);

    File workDir = _getWorkDir();
    /* Following line was inserted becuase the statement below it would occasionally cause swing to throw a
    NullPointerException. workDir == null is supposed to be impossible. */
    if (workDir == null || workDir == FileOps.NULL_FILE) workDir = new File(System.getProperty("user.dir"));
    _fileOptionChooser = new JFileChooser(workDir);
    _jarChooser = new JFileChooser(workDir);
    _browserChooser = new JFileChooser(workDir);
    _dirChooser = new DirectoryChooser(this);
  
    /* Create tree and initialize tree. */
    _rootNode = new PanelTreeNode("Preferences");
    _treeModel = new DefaultTreeModel(_rootNode);
    _tree = new JTree(_treeModel);
    
    _initTree();
    
    /* Create Panels. */
    _createPanels();

    _mainPanel= new JPanel();
    _mainPanel.setLayout(new BorderLayout());
    _tree.addTreeSelectionListener(new PanelTreeSelectionListener());

    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());

    // Select the first panel by default
    if (_rootNode.getChildCount() != 0) {
      PanelTreeNode firstChild = (PanelTreeNode)_rootNode.getChildAt(0);
      TreeNode[] firstChildPath = firstChild.getPath();
      TreePath path = new TreePath(firstChildPath);
      _tree.expandPath(path);
      _tree.setSelectionPath(path);
    }

    JScrollPane treeScroll = new JScrollPane(_tree);
    JPanel treePanel = new JPanel();
    treePanel.setLayout(new BorderLayout());
    treeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Categories"));
    treePanel.add(treeScroll, BorderLayout.CENTER);
    cp.add(treePanel, BorderLayout.WEST);
    cp.add(_mainPanel, BorderLayout.CENTER);

    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    //bottom.add(_saveSettingsButton);
    //bottom.add(Box.createHorizontalGlue());
    bottom.add(_applyButton);
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    cp.add(bottom, BorderLayout.SOUTH);

    // Set all dimensions ----
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if (dim.width>FRAME_WIDTH) { dim.width = FRAME_WIDTH; }
    else { dim.width -= 80; }
    if (dim.height>FRAME_HEIGHT) { dim.height = FRAME_HEIGHT; }
    else { dim.height -= 80; }
    setSize(dim);

    _mainFrame.setPopupLoc(this);

    // Make sure each row is expanded
    int row = 0;
    while(row<_tree.getRowCount()) {
      _tree.expandRow(row);
      ++row;
    }
    
    initDone(); // call mandated by SwingFrame contract
  }
  
  /** Performs deferred initialization.  Only runs in the event thread.  Some of this code occasionally generated swing
   *  exceptions  when run in themain thread as part of MainFrame construction prior to making MainFrame visible. */
  public void setUp() {
    assert EventQueue.isDispatchThread();
    /* Set up _fileOptionChooser, _browserChooser, and _dirChooser.  The line _dirChooser.setSelectedFile(...) caused
     * java.lang.ArrayIndexOutOfBoundsException within swing code in a JUnit test setUp() routine that constructed a
     * a MainFrame.
     */

    _fileOptionChooser.setDialogTitle("Select");
    _fileOptionChooser.setApproveButtonText("Select");
    _fileOptionChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _fileOptionChooser.setFileFilter(ClassPathFilter.ONLY);
    
    _jarChooser.setDialogTitle("Select");
    _jarChooser.setApproveButtonText("Select");
    _jarChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    _jarChooser.setFileFilter(ClassPathFilter.ONLY);
    
    _browserChooser.setDialogTitle("Select Web Browser");
    _browserChooser.setApproveButtonText("Select");
    _browserChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    _dirChooser.setSelectedFile(_getWorkDir());
    _dirChooser.setDialogTitle("Select");
    _dirChooser.setApproveButtonText("Select");
    _dirChooser.setMultiSelectionEnabled(false);
  }
  
  private void enableChangeListeners() {
    DrJava.getConfig().addOptionListener(JUNIT_LOCATION_ENABLED,
                                         _junitLocationEnabledListener);
    DrJava.getConfig().addOptionListener(JUNIT_LOCATION,
                                         _junitLocationListener);
    DrJava.getConfig().addOptionListener(CONCJUNIT_CHECKS_ENABLED,
                                         _concJUnitChecksEnabledListener);
    DrJava.getConfig().addOptionListener(RT_CONCJUNIT_LOCATION,
                                         _rtConcJUnitLocationListener);
  }

  private void disableChangeListeners() {
    DrJava.getConfig().removeOptionListener(JUNIT_LOCATION_ENABLED,
                                            _junitLocationEnabledListener);
    DrJava.getConfig().removeOptionListener(JUNIT_LOCATION,
                                            _junitLocationListener);
    DrJava.getConfig().removeOptionListener(CONCJUNIT_CHECKS_ENABLED,
                                            _concJUnitChecksEnabledListener);
    DrJava.getConfig().removeOptionListener(RT_CONCJUNIT_LOCATION,
                                            _rtConcJUnitLocationListener);
  }

  /** Returns the current master working directory, or the user's current directory if none is set. 20040213 Changed default 
   *  value to user's current directory.
   */
  private File _getWorkDir() {
    File workDir = _mainFrame.getModel().getMasterWorkingDirectory();  // cannot be null
    assert workDir != null;
    if (workDir.isDirectory()) return workDir;
    
    if (workDir.getParent() != null) workDir = workDir.getParentFile();
    return workDir;
  }

  /** Call the update method to propagate down the tree, parsing input values into their config options. */
  public boolean apply() {
    // returns false if the update did not succeed
    return _rootNode.update();
  }

  /** Resets the field of each option in the Preferences window to its actual stored value. */
  public void resetToCurrent() {
    _rootNode.resetToCurrent();
    // must reset the "current keystroke map" when resetting
    VectorKeyStrokeOptionComponent.resetCurrentKeyStrokeMap();
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    resetToCurrent();
    _applyButton.setEnabled(false);
    ConfigFrame.this.setVisible(false);
  }

  /** Thunk that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { cancel(); }
  };
  
  /** Validates before changing visibility.  Only runs in the event thread.
    * @param vis true if frame should be shown, false if it should be hidden.
    */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    // made modal for now
    if (vis) {
//      _mainFrame.hourglassOn();
//      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      enableChangeListeners();
      toFront();
    }
    else {
//      _mainFrame.removeModalWindowAdapter(this);
//      _mainFrame.hourglassOff();
      disableChangeListeners();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }

  /** Write the configured option values to disk. */
  public boolean saveSettings() throws IOException {
    boolean successful = apply();
    if (successful) {
      try { DrJava.getConfig().saveConfiguration(); }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,
                                      "Could not save changes to your \".drjava\" file in your home directory. \n\n" + ioe,
                                      "Could Not Save Changes",
                                      JOptionPane.ERROR_MESSAGE);
        //return false;
        throw ioe;
      }
    }
    return successful;
  }

  /** Sets the given ConfigPanel as the visible panel. */
  private void _displayPanel(ConfigPanel cf) {

    _mainPanel.removeAll();
    _mainPanel.add(cf, BorderLayout.CENTER);
    _mainPanel.revalidate();
    _mainPanel.repaint();
  }

  /** Creates the JTree to display preferences categories. */
  private void _initTree() {
    _tree.setEditable(false);
    _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _tree.setShowsRootHandles(true);
    _tree.setRootVisible(false);

    DefaultTreeCellRenderer dtcr = new DefaultTreeCellRenderer();
    dtcr.setLeafIcon(null);
    dtcr.setOpenIcon(null);
    dtcr.setClosedIcon(null);
    _tree.setCellRenderer(dtcr);
  }

  /**Creates an individual panel, adds it to the JTree and the list of panels, and returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
  private PanelTreeNode _createPanel(String t, PanelTreeNode parent) {
    PanelTreeNode ptNode = new PanelTreeNode(t);
    //parent.add(ptNode);
    _treeModel.insertNodeInto(ptNode, parent, parent.getChildCount());

    // Make sure tree node is visible
    TreeNode[] pathArray = ptNode.getPath();
    TreePath path = new TreePath(pathArray);
//     System.out.println("path has class " + pathArray.getClass());
//     System.out.println("last path compenent has class " + path.getLastPathComponent().getClass());
    _tree.expandPath(path);

    return ptNode;
  }

  /** Creates an individual panel, adds it to the JTree and the list of panels, and returns the tree node. Adds to the root node.
   *  @param t the title of this panel
   *  @return this tree node
   */
  private PanelTreeNode _createPanel(String t) { return _createPanel(t, _rootNode); }

  /** Creates all of the panels contained within the frame. */
  private void _createPanels() {

    PanelTreeNode resourceLocNode = _createPanel("Resource Locations");
    _setupResourceLocPanel(resourceLocNode.getPanel());

    PanelTreeNode displayNode = _createPanel("Display Options");
    _setupDisplayPanel(displayNode.getPanel());
    
    PanelTreeNode fontNode = _createPanel("Fonts", displayNode);
    _setupFontPanel(fontNode.getPanel());

    PanelTreeNode colorNode = _createPanel("Colors", displayNode);
    _setupColorPanel(colorNode.getPanel());

    PanelTreeNode positionsNode = _createPanel("Window Positions", displayNode);
    _setupPositionsPanel(positionsNode.getPanel());

    PanelTreeNode keystrokesNode = _createPanel("Key Bindings");
    _setupKeyBindingsPanel(keystrokesNode.getPanel());
    
    PanelTreeNode compilerOptionsNode = _createPanel("Compiler Options");
    _setupCompilerPanel(compilerOptionsNode.getPanel());
    
    PanelTreeNode interactionsNode = _createPanel("Interactions Pane");
    _setupInteractionsPanel(interactionsNode.getPanel());
    
    PanelTreeNode debugNode = _createPanel("Debugger");
    _setupDebugPanel(debugNode.getPanel());

    PanelTreeNode junitNode = _createPanel("JUnit");
    _setupJUnitPanel(junitNode.getPanel());
    
    PanelTreeNode javadocNode = _createPanel("Javadoc");
    _setupJavadocPanel(javadocNode.getPanel());

    PanelTreeNode notificationsNode = _createPanel("Notifications");
    _setupNotificationsPanel(notificationsNode.getPanel());
    
    PanelTreeNode miscNode = _createPanel("Miscellaneous");
    _setupMiscPanel(miscNode.getPanel());
    
    PanelTreeNode fileTypesNode = _createPanel("File Types", miscNode);
    _setupFileTypesPanel(fileTypesNode.getPanel());
    
    PanelTreeNode jvmsNode = _createPanel("JVMs", miscNode);
    _setupJVMsPanel(jvmsNode.getPanel());
    
    // Expand the display options node
    //DrJava.consoleOut().println("expanding path...");
    //_tree.expandPath(new TreePath(jvmsNode.getPath()));
  }

  public <X,C extends JComponent> void addOptionComponent(ConfigPanel panel, OptionComponent<X,C> oc) {
    panel.addComponent(oc);
    oc.addChangeListener(_changeListener);
  }
  
  protected FileOptionComponent newFileOptionComponent(FileOption o, JFileChooser c) {
    return new FileOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), c);
  }
  protected FileOptionComponent newFileOptionComponent(FileOption o, FileSelectorComponent c) {
    return new FileOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), c);
  }
  protected StringOptionComponent newStringOptionComponent(StringOption o) {
    return new StringOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o));
  }
  protected BooleanOptionComponent newBooleanOptionComponent(BooleanOption o) {
    return newBooleanOptionComponent(o, true);
  }
  protected BooleanOptionComponent newBooleanOptionComponent(BooleanOption o, boolean left) {
    return new BooleanOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), left);
  }
  protected VectorFileOptionComponent newVectorFileOptionComponent(VectorOption<File> o, boolean mbe) {
    return new VectorFileOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), mbe);
  }
  protected VectorStringOptionComponent newVectorStringOptionComponent(VectorOption<String> o, boolean mbe) {
    return new VectorStringOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), mbe);
  }
  protected ForcedChoiceOptionComponent newForcedChoiceOptionComponent(ForcedChoiceOption o) {
    return new ForcedChoiceOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o));
  }
  protected IntegerOptionComponent newIntegerOptionComponent(IntegerOption o) {
    return new IntegerOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o));
  }
  protected FontOptionComponent newFontOptionComponent(FontOption o) {
    return new FontOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o));
  }
  protected ColorOptionComponent newColorOptionComponent(ColorOption o, boolean isBackground, boolean isBold) {
    return new ColorOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o),
                                    isBackground, isBold);
  }
  protected ColorOptionComponent newColorOptionComponent(ColorOption o, boolean isBackground) {
    return new ColorOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o),
                                    isBackground, false);
  }
  protected ColorOptionComponent newColorOptionComponent(ColorOption o) {
    return new ColorOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o),
                                    false, false);
  }
  protected DirectoryOptionComponent newDirectoryOptionComponent(FileOption o, DirectoryChooser c) {
    return new DirectoryOptionComponent(o, CONFIG_DESCRIPTIONS.get(o), this, CONFIG_LONG_DESCRIPTIONS.get(o), c);
  }
  
  /** Add all of the components for the Resource Locations panel of the preferences window. */
  private void _setupResourceLocPanel(ConfigPanel panel) {
    FileOptionComponent browserLoc =
      newFileOptionComponent(BROWSER_FILE, _browserChooser);
    addOptionComponent(panel, browserLoc);    

    StringOptionComponent browserCommand =
      newStringOptionComponent(BROWSER_STRING);
    addOptionComponent(panel, browserCommand);

    FileOptionComponent javacLoc =
      newFileOptionComponent(JAVAC_LOCATION, _fileOptionChooser);
    javacLoc.setFileFilter(ClassPathFilter.ONLY);
    addOptionComponent(panel, javacLoc);

    BooleanOptionComponent displayAllCompilerVersions =
      newBooleanOptionComponent(DISPLAY_ALL_COMPILER_VERSIONS);
    addOptionComponent(panel, displayAllCompilerVersions );
   
    addOptionComponent(panel, newVectorFileOptionComponent(EXTRA_CLASSPATH, true));
    
    panel.displayComponents();
    
  }

  /** Add all of the components for the Display Options panel of the preferences window. */
  private void _setupDisplayPanel(ConfigPanel panel) {

    final ForcedChoiceOptionComponent lookAndFeelComponent =
      newForcedChoiceOptionComponent(LOOK_AND_FEEL);
    addOptionComponent(panel, lookAndFeelComponent);

    final ForcedChoiceOptionComponent plasticComponent =
      newForcedChoiceOptionComponent(PLASTIC_THEMES);
    lookAndFeelComponent.addChangeListener(new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        plasticComponent.getComponent().setEnabled(lookAndFeelComponent.getCurrentComboBoxValue().
                                                     startsWith("com.jgoodies.looks.plastic."));
        return null;
      }
    });
    plasticComponent.getComponent().setEnabled(lookAndFeelComponent.getCurrentComboBoxValue().
                                                 startsWith("com.jgoodies.looks.plastic."));
    addOptionComponent(panel, plasticComponent);

    //ToolbarOptionComponent is a degenerate option component
    addOptionComponent(panel, new ToolbarOptionComponent("Toolbar Buttons", this,
                                                  "How to display the toolbar buttons."));
    addOptionComponent(panel, newBooleanOptionComponent(LINEENUM_ENABLED));
   
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_SOURCE_WHEN_SWITCHING));
    
    addOptionComponent(panel, newBooleanOptionComponent(SHOW_CODE_PREVIEW_POPUPS));
    
    addOptionComponent(panel, newIntegerOptionComponent(CLIPBOARD_HISTORY_SIZE));
    
    BooleanOptionComponent checkbox = 
      newBooleanOptionComponent(DIALOG_GOTOFILE_FULLY_QUALIFIED);
    addOptionComponent(panel, checkbox);
    
    checkbox =
      newBooleanOptionComponent(DIALOG_COMPLETE_SCAN_CLASS_FILES);
    addOptionComponent(panel, checkbox);
    
    checkbox =
      newBooleanOptionComponent(DIALOG_COMPLETE_JAVAAPI);
    addOptionComponent(panel, checkbox);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    final BooleanOptionComponent drmComponent =
      newBooleanOptionComponent(DISPLAY_RIGHT_MARGIN);
    addOptionComponent(panel, drmComponent);
    final IntegerOptionComponent rmcComponent =
      newIntegerOptionComponent(RIGHT_MARGIN_COLUMNS);
    addOptionComponent(panel, rmcComponent);

    OptionComponent.ChangeListener drmListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        rmcComponent.getComponent().setEnabled(drmComponent.getComponent().isSelected());
        return null;
      }
    };
    drmComponent.addChangeListener(drmListener);
    drmListener.value(drmComponent);
    
    panel.displayComponents();
  }

  /** Add all of the components for the Font panel of the preferences window. */
  private void _setupFontPanel(ConfigPanel panel) {
    addOptionComponent(panel, newFontOptionComponent(FONT_MAIN));
    addOptionComponent(panel, newFontOptionComponent(FONT_LINE_NUMBERS));
    addOptionComponent(panel, newFontOptionComponent(FONT_DOCLIST));
    addOptionComponent(panel, newFontOptionComponent(FONT_TOOLBAR));
    addOptionComponent(panel, newBooleanOptionComponent(TEXT_ANTIALIAS));
    panel.displayComponents();
  }

  /** Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel(ConfigPanel panel) {
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_NORMAL_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_KEYWORD_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_TYPE_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_COMMENT_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_DOUBLE_QUOTED_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_SINGLE_QUOTED_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_NUMBER_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_BACKGROUND_COLOR, true));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_LINE_NUMBER_COLOR));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_LINE_NUMBER_BACKGROUND_COLOR, true));
    addOptionComponent(panel, newColorOptionComponent(DEFINITIONS_MATCH_COLOR,true));
    addOptionComponent(panel, newColorOptionComponent(COMPILER_ERROR_COLOR, true));
    addOptionComponent(panel, newColorOptionComponent(BOOKMARK_COLOR, true));
    for (int i = 0; i < FIND_RESULTS_COLORS.length; ++i) {
      addOptionComponent(panel, newColorOptionComponent(FIND_RESULTS_COLORS[i], true));
    }
    addOptionComponent(panel, 
                       newColorOptionComponent(DEBUG_BREAKPOINT_COLOR, true));
    addOptionComponent(panel, 
                       newColorOptionComponent(DEBUG_BREAKPOINT_DISABLED_COLOR, true));
    addOptionComponent(panel, 
                       newColorOptionComponent(DEBUG_THREAD_COLOR, true));
    addOptionComponent(panel, newColorOptionComponent(SYSTEM_OUT_COLOR));
    addOptionComponent(panel, newColorOptionComponent(SYSTEM_ERR_COLOR));
    addOptionComponent(panel, newColorOptionComponent(SYSTEM_IN_COLOR));
    addOptionComponent(panel, newColorOptionComponent(INTERACTIONS_ERROR_COLOR, false, true));
    addOptionComponent(panel, newColorOptionComponent(DEBUG_MESSAGE_COLOR, false, true));
    addOptionComponent(panel, 
                       newColorOptionComponent(DRJAVA_ERRORS_BUTTON_COLOR, true));
    addOptionComponent(panel, 
                       newColorOptionComponent(RIGHT_MARGIN_COLOR, true));
    
    panel.displayComponents();
  }

  /** Add all of the components for the Positions panel of the preferences window. */
  private void _setupPositionsPanel(ConfigPanel panel) {
    addOptionComponent(panel, newBooleanOptionComponent(WINDOW_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_CLIPBOARD_HISTORY_STORE_POSITION,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetClipboardHistoryDialogPosition(); }
    }, "Reset \"Clipboard History\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_GOTOFILE_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetGotoFileDialogPosition(); }
    }, "Reset \"Go to File\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_COMPLETE_WORD_STORE_POSITION,false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetCompleteWordDialogPosition(); }
    }, 
                                                  "Reset \"Auto-Complete Word\" Dialog Position and Size", this, 
                                                  "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_JAROPTIONS_STORE_POSITION,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetJarOptionsDialogPosition();
      }
    }, "Reset \"Create Jar File from Project\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_OPENJAVADOC_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetOpenJavadocDialogPosition(); }
    }, "Reset \"Open Javadoc\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_AUTOIMPORT_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetAutoImportDialogPosition(); }
    }, "Reset \"Auto Import\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_EXTERNALPROCESS_STORE_POSITION,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetExecuteExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_EDITEXTERNALPROCESS_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetEditExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_OPENJAVADOC_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetOpenJavadocDialogPosition();
      }
    }, "Reset \"Open Javadoc\" Dialog Position", this, "This resets the dialog position to its default values."));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_TABBEDPANES_STORE_POSITION, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetTabbedPanesFrame();
      }
    }, "Reset \"Tabbed Panes\" Window Position", this, "This resets the window position to its default values."));

    addOptionComponent(panel, 
                       newBooleanOptionComponent(DETACH_TABBEDPANES,false)
                         .setEntireColumn(true));

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_DEBUGFRAME_STORE_POSITION,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetDebugFrame();
      }
    }, "Reset \"Debugger\" Window Position", this, "This resets the window position to its default values."));

    addOptionComponent(panel, newBooleanOptionComponent(DETACH_DEBUGGER, false)
                         .setEntireColumn(true));

    panel.displayComponents();
  }
  
  /** Adds all of the components for the Key Bindings panel of the preferences window.
   */
  private void _setupKeyBindingsPanel(ConfigPanel panel) {
    // using a treemap because it automatically sorts element upon insertion
    TreeMap<String,VectorKeyStrokeOptionComponent> _comps = new TreeMap<String,VectorKeyStrokeOptionComponent>();

    VectorKeyStrokeOptionComponent vksoc;

    for (KeyStrokeData ksd: KeyBindingManager.ONLY.getKeyStrokeData()) {
      if (ksd.getOption() != null) {
        // Get the tooltip, or default to its name, if none
        Action a = ksd.getAction();
        // pick the short description as name, if available
        String name = (String) a.getValue(Action.SHORT_DESCRIPTION);
        // if not available, pick the KeyStrokeData name instead
        if (name == null || name.trim().equals("")) name = ksd.getName();
        // pick the long description as name, if available
        String desc = (String) a.getValue(Action.LONG_DESCRIPTION);
        // if not available, pick the name from above instead
        if (desc == null || desc.trim().equals("")) desc = name;
        // if the map already contains this name, use the description instead
        if (_comps.containsKey(name)) {
          name = desc;
          // if the map already contains the description as well (bad developers!), then use the option's name
          if (_comps.containsKey(name)) {
            name = ksd.getOption().getName();
          }
        }
        vksoc = new VectorKeyStrokeOptionComponent(ksd.getOption(), name, this, desc);
        if (vksoc != null) _comps.put(name, vksoc);
      }
    }

    Iterator<VectorKeyStrokeOptionComponent> iter = _comps.values().iterator();
    while (iter.hasNext()) {
      VectorKeyStrokeOptionComponent x = iter.next();
      addOptionComponent(panel, x);
    }
    panel.displayComponents();
  }

  /** Add all of the components for the Debugger panel of the preferences window. */
  private void _setupDebugPanel(ConfigPanel panel) {
    if (!_mainFrame.getModel().getDebugger().isAvailable()) {
      // Explain how to use debugger
      String howto =
        "\nThe debugger is not currently available. To use the debugger,\n" +
        "you can enter the location of the tools.jar file in the\n" +
        "\"Resource Locations\" pane, in case DrJava does not automatically find it.\n" +
        "See the user documentation for more details.\n";
      LabelComponent label = new LabelComponent(howto, this);
      label.setEntireColumn(true);
      addOptionComponent(panel, label);
    }

    VectorFileOptionComponent sourcePath =
      newVectorFileOptionComponent(DEBUG_SOURCEPATH, true);
    // Source path can only include directories
    sourcePath.getFileChooser().setFileFilter(new DirectoryFilter("Source Directories"));
    addOptionComponent(panel, sourcePath);
    addOptionComponent(panel, 
                       newBooleanOptionComponent(DEBUG_STEP_JAVA));
    addOptionComponent(panel, newBooleanOptionComponent(DEBUG_STEP_INTERPRETER));
    addOptionComponent(panel, newBooleanOptionComponent(DEBUG_STEP_DRJAVA));
    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", 
                                          this, true));
    addOptionComponent(panel, 
                       newVectorStringOptionComponent(DEBUG_STEP_EXCLUDE, false));
    addOptionComponent(panel, newBooleanOptionComponent(DEBUG_AUTO_IMPORT));
    
    addOptionComponent(panel, newIntegerOptionComponent(AUTO_STEP_RATE));                                                            
    addOptionComponent(panel, newBooleanOptionComponent(DEBUG_EXPRESSIONS_AND_METHODS_IN_WATCHES));
    panel.displayComponents();
  }

  /** Add all of the components for the Javadoc panel of the preferences window. */
  private void _setupJavadocPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(JAVADOC_API_REF_VERSION));
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(JAVADOC_ACCESS_LEVEL));
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(JAVADOC_LINK_VERSION));
    addOptionComponent(panel, 
                       newStringOptionComponent(JAVADOC_1_5_LINK));
    addOptionComponent(panel, 
                       newStringOptionComponent(JAVADOC_1_6_LINK));
    addOptionComponent(panel, 
                       newStringOptionComponent(JAVADOC_1_7_LINK));
    addOptionComponent(panel, 
                       newStringOptionComponent(JUNIT_LINK));

    VectorStringOptionComponent additionalJavadoc =
      new VectorStringOptionComponent(JAVADOC_ADDITIONAL_LINKS, 
                                      CONFIG_DESCRIPTIONS.get(JAVADOC_ADDITIONAL_LINKS),
                                      this,
                                      CONFIG_LONG_DESCRIPTIONS.get(JAVADOC_ADDITIONAL_LINKS)) {
      protected boolean verify(String s) {
        // verify that the allclasses-frame.html file exists at that URL. do not actually parse it now
        boolean result = true;
        try {
          java.net.URL url = new java.net.URL(s+"/allclasses-frame.html");
          java.io.InputStream urls = url.openStream();
          java.io.InputStreamReader is = null;
          java.io.BufferedReader br = null;
          try {
            is = new java.io.InputStreamReader(urls);
            br = new java.io.BufferedReader(is);
            String line = br.readLine();
            if (line == null) { result = false; }
          }
          finally {
            if (br != null) { br.close(); }
            if (is != null) { is.close(); }
            if (urls != null) { urls.close(); }
          }
        }
        catch(java.io.IOException ioe) { result = false; }
        if (!result) {
          JOptionPane.showMessageDialog(ConfigFrame.this,
                                        "Could not find the Javadoc at the URL\n"+
                                        s,
                                        "Error Adding Javadoc",
                                        JOptionPane.ERROR_MESSAGE); 
        }
        return result;
      }
    };
    addOptionComponent(panel, additionalJavadoc);
    
    addOptionComponent(panel, 
                       newDirectoryOptionComponent(JAVADOC_DESTINATION, _dirChooser));
    
    addOptionComponent(panel, 
                       javadocCustomParams = newStringOptionComponent(JAVADOC_CUSTOM_PARAMS));
    
    // Note: JAVADOC_FROM_ROOTS is intended to set the -subpackages flag, but I don't think that's something
    // we should support -- in general, we only support performing operations on the files that are open.
    // (dlsmith r4189)
//    addOptionComponent(panel, 
//                       newBooleanOptionComponent(JAVADOC_FROM_ROOTS));
    
    panel.displayComponents();
  }

  /** Adds all of the components for the Prompts panel of the preferences window. */
  private void _setupNotificationsPanel(ConfigPanel panel) {
    // Quit
    addOptionComponent(panel, newBooleanOptionComponent(QUIT_PROMPT, false)
                         .setEntireColumn(true));

    // Interactions
    addOptionComponent(panel, newBooleanOptionComponent(INTERACTIONS_RESET_PROMPT, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(INTERACTIONS_EXIT_PROMPT,false)
                         .setEntireColumn(true));

    // Javadoc
    addOptionComponent(panel, newBooleanOptionComponent(JAVADOC_PROMPT_FOR_DESTINATION,false)
                         .setEntireColumn(true));


    // Clean
    addOptionComponent(panel, newBooleanOptionComponent(PROMPT_BEFORE_CLEAN, false)
                         .setEntireColumn(true));

    // Prompt to change the language level extensions (.dj0/.dj1->.dj, .dj2->.java)
    addOptionComponent(panel, newBooleanOptionComponent(PROMPT_RENAME_LL_FILES, false)
                         .setEntireColumn(true));

    
    // Save before X
    addOptionComponent(panel, newBooleanOptionComponent(ALWAYS_SAVE_BEFORE_COMPILE, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, newBooleanOptionComponent(ALWAYS_COMPILE_BEFORE_JUNIT,false)
                         .setEntireColumn(true)); 
    
    addOptionComponent(panel, newBooleanOptionComponent(ALWAYS_SAVE_BEFORE_JAVADOC, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, newBooleanOptionComponent(ALWAYS_COMPILE_BEFORE_JAVADOC, false)
                         .setEntireColumn(true));


    // These are very problematic features, and so are disabled for the forseeable future.
//    addOptionComponent(panel, 
//                       newBooleanOptionComponent(ALWAYS_SAVE_BEFORE_RUN));
//    addOptionComponent(panel, 
//                       newBooleanOptionComponent(ALWAYS_SAVE_BEFORE_DEBUG));
    
    // Warnings
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_BREAKPOINT_OUT_OF_SYNC,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_DEBUG_MODIFIED_FILE,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_CHANGE_LAF,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_CHANGE_THEME,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_CHANGE_DCP,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_CHANGE_MISC,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_CHANGE_INTERACTIONS,false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(WARN_PATH_CONTAINS_POUND,false)
                         .setEntireColumn(true));

    addOptionComponent(panel, 
                       newBooleanOptionComponent(DIALOG_DRJAVA_ERROR_POPUP_ENABLED,false)
                         .setEntireColumn(true));
    addOptionComponent(panel,
                       newBooleanOptionComponent(WARN_IF_COMPIZ,false)
                         .setEntireColumn(true));
    
    
    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", this, true));

    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(DELETE_LL_CLASS_FILES));

    addOptionComponent(panel, 
                       new LabelComponent("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
                                          "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>", this, true));

    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(NEW_VERSION_NOTIFICATION));
    addOptionComponent(panel, newIntegerOptionComponent(NEW_VERSION_NOTIFICATION_DAYS));

    panel.displayComponents();
  }

  /** Adds all of the components for the Miscellaneous panel of the preferences window. */
  private void _setupMiscPanel(ConfigPanel panel) {
    /* Dialog box options */
    addOptionComponent(panel, newIntegerOptionComponent(INDENT_LEVEL));
    addOptionComponent(panel, newIntegerOptionComponent(RECENT_FILES_MAX_SIZE));
    addOptionComponent(panel, newIntegerOptionComponent(BROWSER_HISTORY_MAX_SIZE));
    
    /* Check box options */
    addOptionComponent(panel, 
                       newBooleanOptionComponent(AUTO_CLOSE_COMMENTS));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(RUN_WITH_ASSERT));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(BACKUP_FILES));
    addOptionComponent(panel, 
                       newBooleanOptionComponent(RESET_CLEAR_CONSOLE));

    addOptionComponent(panel, 
                       newBooleanOptionComponent(FIND_REPLACE_FOCUS_IN_DEFPANE));
    addOptionComponent(panel, newBooleanOptionComponent(DRJAVA_USE_FORCE_QUIT));
    addOptionComponent(panel, newBooleanOptionComponent(REMOTE_CONTROL_ENABLED));
    addOptionComponent(panel, newIntegerOptionComponent(REMOTE_CONTROL_PORT));
    addOptionComponent(panel, newIntegerOptionComponent(FOLLOW_FILE_DELAY));
    addOptionComponent(panel, newIntegerOptionComponent(FOLLOW_FILE_LINES));
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    addOptionComponent(panel, newBooleanOptionComponent(LIGHTWEIGHT_PARSING_ENABLED));
//    addOptionComponent(panel, newIntegerOptionComponent(DIALOG_LIGHTWEIGHT_PARSING_DELAY));
    
    panel.displayComponents();
  }  

  /** Adds all of the components for the JVMs panel of the preferences window. */
  private void _setupJVMsPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(MASTER_JVM_XMX));
    addOptionComponent(panel, 
                       newStringOptionComponent(MASTER_JVM_ARGS));
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(SLAVE_JVM_XMX));
    addOptionComponent(panel, 
                       newStringOptionComponent(SLAVE_JVM_ARGS));    
    panel.displayComponents();
  }

  /** Adds all of the components for the file types panel of the preferences window. */
  private void _setupFileTypesPanel(ConfigPanel panel) {
    if (PlatformFactory.ONLY.canRegisterFileExtensions()) {
      addOptionComponent(panel, new LabelComponent("<html>Assign DrJava project files and DrJava extensions<br>"+
                                                   "(with the extensions .drjava and .djapp) to DrJava.<br>"+
                                                   "When double-clicking on a .drjava file, DrJava will open it.</html>", this, true));
      
      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.registerDrJavaFileExtensions()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully set .drjava and .djapp file associations.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not set .drjava and .djapp file associations.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Associate .drjava and .djapp Files with DrJava", this, "This associates .drjava and .djapp files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.unregisterDrJavaFileExtensions()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully removed .drjava and .djapp file associations.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not remove .drjava and .djapp file associations.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Remove .drjava and .djapp File Associations", this, "This removes the association of .drjava and .djapp files with DrJava."));
      
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>Assign Java source files with the<br>"+
                                                   "extension .java to DrJava. When double-clicking<br>"+
                                                   "on a .java file, DrJava will open it.</html>", this, true));

      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.registerJavaFileExtension()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully set .java file association.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not set .java file association.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Associate .java Files with DrJava", this, "This associates .java source files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));

      panel.addComponent(new ButtonComponent(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (PlatformFactory.ONLY.unregisterJavaFileExtension()) {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Successfully removed .java file association.",
                                          "Success",
                                          JOptionPane.INFORMATION_MESSAGE); 
          }
          else {
            JOptionPane.showMessageDialog(ConfigFrame.this,
                                          "Could not remove .java file association.",
                                          "File Types Error",
                                          JOptionPane.ERROR_MESSAGE); 
          }
        }
      }, "Remove .java File Association", this, "This removes the association of .java project files with DrJava."));

      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
      addOptionComponent(panel, newForcedChoiceOptionComponent(FILE_EXT_REGISTRATION));
    }
    else {
      addOptionComponent(panel, 
                         new LabelComponent("<html><br><br>"+
                                            (PlatformFactory.ONLY.isMacPlatform()?
                                               "File associations are managed automatically by Mac OS.":
                                               (PlatformFactory.ONLY.isWindowsPlatform()?
                                                  "To set file associations, please use the .exe file version of DrJava.<br>"+
                                                "Configuring file associations is not supported for the .jar file version.":
                                                  "Managing file associations is not supported yet on this operating system."))+
                                            "</html>",
                                            this, true));
    }
    panel.displayComponents();
  }
  
  /** Adds all of the components for the Compiler Options Panel of the preferences window
    */
  private void _setupCompilerPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_UNCHECKED_WARNINGS, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_DEPRECATION_WARNINGS, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_PATH_WARNINGS, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_SERIAL_WARNINGS,false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_FINALLY_WARNINGS, false)
                         .setEntireColumn(true));
    
    addOptionComponent(panel, 
                       newBooleanOptionComponent(SHOW_FALLTHROUGH_WARNINGS, false)
                         .setEntireColumn(true));
    addOptionComponent(panel, 
                       new LabelComponent("<html><br><br>Note: Some of these options may not be effective, depending on the<br>"+
                                          "compiler you are using.</html>",
                                          this, true));
    /* The drop down box containing the compiler names. */
    final ForcedChoiceOptionComponent CPC =
      newForcedChoiceOptionComponent(COMPILER_PREFERENCE_CONTROL.evaluate());
    
    /* Action listener that loads the selected compiler name into the DEFAULT_COMPILER_PREFERENCE setting. */
    ActionListener CPCActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (! edu.rice.cs.drjava.DrJava.getConfig().getSetting(DEFAULT_COMPILER_PREFERENCE).equals(CPC.getCurrentComboBoxValue())) {
          edu.rice.cs.drjava.DrJava.getConfig().setSetting(DEFAULT_COMPILER_PREFERENCE,CPC.getCurrentComboBoxValue());
        }
      }
    };
   
    /* Insures that the change is made only when the apply or ok button is hit. */
    _applyButton.addActionListener(CPCActionListener);
    _okButton.addActionListener(CPCActionListener);
    
    /* Adds the drop down box to the panel. */
    addOptionComponent(panel, CPC.setEntireColumn(false));
    
    String optionLabel = 
      "<html><br><br>Note: Compiler warnings not shown if compiling any Java language level files.</html>";
    addOptionComponent(panel, new LabelComponent(optionLabel, this, true));
    panel.displayComponents();
  }
  
  /** Add all of the components for the Interactions panel of the preferences window. */
  private void _setupInteractionsPanel(ConfigPanel panel) {
    final DirectoryOptionComponent wdComponent =
      newDirectoryOptionComponent(FIXED_INTERACTIONS_DIRECTORY, _dirChooser);
    addOptionComponent(panel, wdComponent);
    final BooleanOptionComponent stickyComponent = 
      newBooleanOptionComponent(STICKY_INTERACTIONS_DIRECTORY);
    addOptionComponent(panel, stickyComponent);
    
    OptionComponent.ChangeListener wdListener = new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        File f = wdComponent.getComponent().getFileFromField();
        boolean enabled = (f == null) || (f.equals(FileOps.NULL_FILE));
        stickyComponent.getComponent().setEnabled(enabled);
        return null;
      }
    };
    wdComponent.addChangeListener(wdListener);
    wdListener.value(wdComponent);

    addOptionComponent(panel, newBooleanOptionComponent
                         (SMART_RUN_FOR_APPLETS_AND_PROGRAMS));
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
    addOptionComponent(panel, newIntegerOptionComponent(HISTORY_MAX_SIZE));
    addOptionComponent(panel, newBooleanOptionComponent(DIALOG_AUTOIMPORT_ENABLED));
    VectorStringOptionComponent autoImportClasses =
      new VectorStringOptionComponent(INTERACTIONS_AUTO_IMPORT_CLASSES,
                                      CONFIG_DESCRIPTIONS.get(INTERACTIONS_AUTO_IMPORT_CLASSES),
                                      this,
                                      CONFIG_LONG_DESCRIPTIONS.get(INTERACTIONS_AUTO_IMPORT_CLASSES)) {
      protected boolean verify(String s) {
        boolean result = true;
        // verify that the string contains only Java identifier characters, dots and stars
        for(int i = 0; i < s.length(); ++i) {
          char ch = s.charAt(i);
          if ((ch!='.') && (ch!='*') && (!Character.isJavaIdentifierPart(ch))) {
            result = false;
            break;
          }
        }
        if (!result) {
          JOptionPane.showMessageDialog(ConfigFrame.this,
                                        "This is not a valid class name:\n"+
                                        s,
                                        "Error Adding Class Name",
                                        JOptionPane.ERROR_MESSAGE); 
        }
        return result;
      }
    };
    addOptionComponent(panel, autoImportClasses);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      
    addOptionComponent(panel, 
                       newForcedChoiceOptionComponent(DYNAMICJAVA_ACCESS_CONTROL));
    addOptionComponent(panel, newBooleanOptionComponent(DYNAMICJAVA_REQUIRE_SEMICOLON));
    addOptionComponent(panel, newBooleanOptionComponent(DYNAMICJAVA_REQUIRE_VARIABLE_TYPE));
    
    panel.displayComponents();
  }

  /** Add all of the components for the JUnit panel of the preferences window. */
  private void _setupJUnitPanel(ConfigPanel panel) {
    final BooleanOptionComponent junitLocEnabled =
      newBooleanOptionComponent(JUNIT_LOCATION_ENABLED,false)
      .setEntireColumn(true);
    addOptionComponent(panel, junitLocEnabled);
    final FileOptionComponent junitLoc =
      newFileOptionComponent(JUNIT_LOCATION,
                              new FileSelectorComponent(this, _jarChooser, 30, 10f) {
      public void setFileField(File file) {
        if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidJUnitFile(file) ||
            edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(file)) {
          super.setFileField(file);
        }
        else if (file.exists()) { // invalid JUnit/ConcJUnit file, but exists
          new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(_parent, "Invalid JUnit/ConcJUnit File", "Stack trace:",
                                                           edu.rice.cs.util.StringOps.getStackTrace(), 600, 400, false).show();
          JOptionPane.showMessageDialog(_parent, "The file '"+ file.getName() + "'\nis not a valid JUnit/ConcJUnit file.",
                                        "Invalid JUnit/ConcJUnit File", JOptionPane.ERROR_MESSAGE);
          resetFileField(); // revert if not valid          
        }
      }
      public boolean validateTextField() {
        String newValue = _fileField.getText().trim();
        
        File newFile = FileOps.NULL_FILE;
        if (!newValue.equals(""))
          newFile = new File(newValue);
        
        if (newFile != FileOps.NULL_FILE && !newFile.exists()) {
          JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis invalid because it does not exist.",
                                        "Invalid File Name", JOptionPane.ERROR_MESSAGE);
          if (_file != null && ! _file.exists()) _file = FileOps.NULL_FILE;
          resetFileField(); // revert if not valid
          
          return false;
        }
        else {
          if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidJUnitFile(newFile) ||
              edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(newFile) ||
              FileOps.NULL_FILE.equals(newFile)) {
            setFileField(newFile);
            return true;
          }
          else {
            new edu.rice.cs.drjava.ui.DrJavaScrollableDialog(_parent, "Invalid JUnit/ConcJUnit File", "newFile is NULL_FILE? "+(FileOps.NULL_FILE.equals(newFile)),
                                                             edu.rice.cs.util.StringOps.getStackTrace(), 600, 400, false).show();
            JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis not a valid JUnit/ConcJUnit file.",
                                          "Invalid JUnit/ConcJUnit File", JOptionPane.ERROR_MESSAGE);
            resetFileField(); // revert if not valid
            
            return false;
          }
        }
      }    
    });
    junitLoc.setFileFilter(ClassPathFilter.ONLY);
    addOptionComponent(panel, junitLoc);

    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));

    boolean javaVersion7 = JavaVersion.CURRENT.supports(JavaVersion.JAVA_7);
    if (!javaVersion7) {
      final ForcedChoiceOptionComponent concJUnitChecksEnabledComponent =
        newForcedChoiceOptionComponent(CONCJUNIT_CHECKS_ENABLED);
      addOptionComponent(panel, concJUnitChecksEnabledComponent);
      
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));    
      
      final FileOptionComponent rtConcJUnitLoc =
        newFileOptionComponent(RT_CONCJUNIT_LOCATION,
                               new FileSelectorComponent(this, _jarChooser, 30, 10f) {
        public void setFileField(File file) {
          if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(file)) {
            super.setFileField(file);
          }
          else if (file.exists()) { // invalid but exists
            JOptionPane.showMessageDialog(_parent, "The file '"+ file.getName() + "'\nis not a valid ConcJUnit Runtime file.",
                                          "Invalid ConcJUnit Runtime File", JOptionPane.ERROR_MESSAGE);
            resetFileField(); // revert if not valid          
          }
        }
        public boolean validateTextField() {
          String newValue = _fileField.getText().trim();
          
          File newFile = FileOps.NULL_FILE;
          if (!newValue.equals(""))
            newFile = new File(newValue);
          
          if (newFile != FileOps.NULL_FILE && !newFile.exists()) {
            JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis invalid because it does not exist.",
                                          "Invalid File Name", JOptionPane.ERROR_MESSAGE);
            if (_file != null && ! _file.exists()) _file = FileOps.NULL_FILE;
            resetFileField(); // revert if not valid
            
            return false;
          }
          else {
            if (edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(newFile) ||
                FileOps.NULL_FILE.equals(newFile)) {
              setFileField(newFile);
              return true;
            }
            else {
              JOptionPane.showMessageDialog(_parent, "The file '"+ newFile.getName() + "'\nis not a valid ConcJUnit Runtime file.",
                                            "Invalid ConcJUnit Runtime File", JOptionPane.ERROR_MESSAGE);
              resetFileField(); // revert if not valid
              
              return false;
            }
          }
        }    
      });
      rtConcJUnitLoc.setFileFilter(ClassPathFilter.ONLY);
      
      ActionListener processRTListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          File concJUnitJarFile = FileOps.getDrJavaFile();
          if (junitLocEnabled.getComponent().isSelected()) {
            concJUnitJarFile = junitLoc.getComponent().getFileFromField();
          }
          File rtFile = rtConcJUnitLoc.getComponent().getFileFromField();
          edu.rice.cs.drjava.model.junit.ConcJUnitUtils.
            showGenerateRTConcJUnitJarFileDialog(ConfigFrame.this,
                                                 rtFile,
                                                 concJUnitJarFile,
                                                 new Runnable1<File>() {
            public void run(File targetFile) {
              rtConcJUnitLoc.getComponent().setFileField(targetFile);
            }
          },
                                                 new Runnable() { public void run() { } });
        }
      };
      final ButtonComponent processRT =
        new ButtonComponent(processRTListener, "Generate ConcJUnit Runtime File", this,
                            "<html>Generate the ConcJUnit Runtime file specified above.<br>"+
                            "This setting is deactivated if the path to ConcJUnit has not been specified above.</html>");
      
      OptionComponent.ChangeListener rtConcJUnitListener = new OptionComponent.ChangeListener() {
        public Object value(Object oc) {
          File f = junitLoc.getComponent().getFileFromField();
          boolean enabled = (!junitLocEnabled.getComponent().isSelected()) ||
            edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(f);
          rtConcJUnitLoc.getComponent().setEnabled(enabled);
          processRT.getComponent().setEnabled(enabled);
          concJUnitChecksEnabledComponent.getComponent().setEnabled(enabled);
          return null;
        }
      };
      
      OptionComponent.ChangeListener junitLocListener = new OptionComponent.ChangeListener() {
        public Object value(Object oc) {
          boolean enabled = junitLocEnabled.getComponent().isSelected();
          junitLoc.getComponent().setEnabled(enabled);
          return null;
        }
      };
      junitLocEnabled.addChangeListener(junitLocListener);
      junitLocEnabled.addChangeListener(rtConcJUnitListener);
      junitLoc.addChangeListener(rtConcJUnitListener);
      addOptionComponent(panel, rtConcJUnitLoc);
      addOptionComponent(panel, processRT);
      
      addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
      final LabelComponent internalExternalStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
      final LabelComponent threadsStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
      final LabelComponent joinStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
      final LabelComponent luckyStatus = new LabelComponent("<html>&nbsp;</html>", this, true);
      OptionComponent.ChangeListener junitStatusChangeListener = new OptionComponent.ChangeListener() {
        public Object value(Object oc) {
          File f = junitLoc.getComponent().getFileFromField();
          String[] s = new String[] { " ", " ", " ", " " };
          boolean isConcJUnit = true;
          if ((!junitLocEnabled.getComponent().isSelected()) || (f==null) || FileOps.NULL_FILE.equals(f) || !f.exists()) {
            s[0] = "DrScala uses the built-in ConcJUnit framework.";
          }
          else {
            String type = "ConcJUnit";
            if (!edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidConcJUnitFile(f)) {
              type = "JUnit";
              isConcJUnit = false;
            }
            s[0] = "DrScala uses an external "+type+" framework.";
          }
          if (!isConcJUnit) {
            s[1] = "JUnit does not support all-thread, no-join";
            s[2] = "or lucky checks. They are all disabled.";
          }
          else {
            s[1] = "All-thread checks are disabled.";
            s[2] = "No-join checks are disabled.";
            s[3] = "Lucky checks are disabled.";
            if (!concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                  equals(ConcJUnitCheckChoices.NONE)) {
              s[1] = "All-thread checks are enabled.";
              if (concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                    equals(ConcJUnitCheckChoices.ALL) ||
                  concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                    equals(ConcJUnitCheckChoices.NO_LUCKY)) {
                s[2] = "No-join checks are enabled.";
                if (concJUnitChecksEnabledComponent.getCurrentComboBoxValue().
                      equals(ConcJUnitCheckChoices.ALL)) {
                  File rtf = rtConcJUnitLoc.getComponent().getFileFromField();
                  if ((rtf!=null) && !FileOps.NULL_FILE.equals(rtf) && rtf.exists() &&
                      edu.rice.cs.drjava.model.junit.ConcJUnitUtils.isValidRTConcJUnitFile(rtf)) {
                    s[3] = "Lucky checks are enabled.";
                  }
                }
              }
            }
          }
          internalExternalStatus.getComponent().setText(s[0]);
          threadsStatus.getComponent().setText(s[1]);
          joinStatus.getComponent().setText(s[2]);
          luckyStatus.getComponent().setText(s[3]);
          return null;
        }
      };
      concJUnitChecksEnabledComponent.addChangeListener(junitStatusChangeListener);
      junitLocEnabled.addChangeListener(junitStatusChangeListener);
      junitLoc.addChangeListener(junitStatusChangeListener);
      rtConcJUnitLoc.addChangeListener(junitStatusChangeListener);
      addOptionComponent(panel, internalExternalStatus);
      addOptionComponent(panel, threadsStatus);
      addOptionComponent(panel, joinStatus);
      addOptionComponent(panel, luckyStatus);
      
      junitLocListener.value(null);
      rtConcJUnitListener.value(null);
      junitStatusChangeListener.value(null);
    }
    else {
      addOptionComponent(panel, 
                         new LabelComponent("<html><br><br>ConcJUnit is currently not supported on Java 7.<br><br></html>", this, true));
    }
    
    addOptionComponent(panel, new LabelComponent("<html>&nbsp;</html>", this, true));
    final BooleanOptionComponent forceTestSuffix  =
      newBooleanOptionComponent(FORCE_TEST_SUFFIX,false)
      .setEntireColumn(true);
    addOptionComponent(panel, forceTestSuffix);
    
    panel.displayComponents();
  }
  
  /** Private class to handle rendering of tree nodes, each of which
    *  corresponds to a ConfigPanel.  These nodes should only be accessed
    *  from the event handling thread.
    */
  private class PanelTreeNode extends DefaultMutableTreeNode {
    
    private final ConfigPanel _panel;
    
    public PanelTreeNode(String t) {
      super(t);
      _panel = new ConfigPanel(t);
    }

    public PanelTreeNode(ConfigPanel c) {
      super(c.getTitle());
      _panel = c;
    }
    private ConfigPanel getPanel() { return _panel; }

    /** Tells its panel to update, and tells all of its child nodes to update their panels.
     *  @return whether the update succeeded.
     */
    private boolean update() {
      
      boolean isValidUpdate = _panel.update();
       
      //if this panel encountered an error while attempting to update, return false
      if (!isValidUpdate) {
        //System.out.println("Panel.update() returned false");

        //TreePath path = new TreePath(this.getPath());
        // causes ClassCastException under jsr14 v2.0 for no apparent reason.
        // Workaround:  store result of getPath() to temporary array.

        TreeNode[] nodes = getPath();
        TreePath path = new TreePath(nodes);
        _tree.expandPath(path);
        _tree.setSelectionPath(path);
        return false;
      }

      Enumeration<?> childNodes = children();
      while (childNodes.hasMoreElements()) {
        boolean isValidUpdateChildren = ((PanelTreeNode)childNodes.nextElement()).update();
        //if any of the children nodes encountered an error, return false
        if (!isValidUpdateChildren) {
          return false;
        }
      }

      return true;
    }

    /** Tells its panel to reset its displayed value to the currently set value for this component, and tells all of
      * its children to reset their panels.  Should be performed in the event thread!
      */
    public void resetToCurrent() {
      _panel.resetToCurrent();

      Enumeration<?> childNodes = children();
      while (childNodes.hasMoreElements()) {
        ((PanelTreeNode)childNodes.nextElement()).resetToCurrent();
      }
    }
  }

  private class PanelTreeSelectionListener implements TreeSelectionListener {
    public void valueChanged(TreeSelectionEvent e) {
      Object o = _tree.getLastSelectedPathComponent();
      //System.out.println("Object o : "+o);
      if (o instanceof PanelTreeNode) {
        //System.out.println("o is instanceof PanelTreeNode");
        PanelTreeNode child = (PanelTreeNode) _tree.getLastSelectedPathComponent();
        _displayPanel(child.getPanel());
      }
    }
  }
}
