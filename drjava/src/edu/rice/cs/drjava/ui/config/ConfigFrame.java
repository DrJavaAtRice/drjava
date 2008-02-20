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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.util.TreeSet;
import java.util.Iterator;

import javax.swing.tree.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;
import edu.rice.cs.util.swing.DirectoryChooser;

/** The frame for setting Configuration options on the fly
 *  @version $Id$
 */
public class ConfigFrame extends JFrame {

  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 500;

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
  private final DirectoryChooser _dirChooser;
  
  private StringOptionComponent javadocCustomParams;
  
  private OptionComponent.ChangeListener _changeListener = new OptionComponent.ChangeListener() {
    public Object apply(Object oc) {
      _applyButton.setEnabled(true);
      return null;
    }
  };
  
  /** Sets up the frame and displays it.  This a Swing view class!  With the exception of initialization,
   *  this code should only be executed in the event-handling thread. */
  public ConfigFrame(MainFrame frame) {
    super("Preferences");

    _mainFrame = frame;
    
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
    _fileOptionChooser = new JFileChooser(workDir);

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
    setSize(FRAME_WIDTH, FRAME_HEIGHT);

    _mainFrame.setPopupLoc(this);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) { cancel(); }
    });

    // Make sure each row is expanded (this is harder than it seems...)
    _tree.expandRow(0);
    _tree.expandRow(1);
    _tree.expandRow(2);
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
    
    _browserChooser.setDialogTitle("Select Web Browser");
    _browserChooser.setApproveButtonText("Select");
    _browserChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    
    _dirChooser.setSelectedFile(_getWorkDir());
    _dirChooser.setDialogTitle("Select");
    _dirChooser.setApproveButtonText("Select");
    _dirChooser.setMultiSelectionEnabled(false);

    
  }

  /** Returns the current master working directory, or the user's current directory if none is set. 20040213 Changed default 
   *  value to user's current directory.
   */
  private File _getWorkDir() {
    File workDir = _mainFrame.getModel().getMasterWorkingDirectory();  // cannot be null
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
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    resetToCurrent();
    _applyButton.setEnabled(false);
    ConfigFrame.this.setVisible(false);
  }
  
  

  /** Write the configured option values to disk. */
  public boolean saveSettings() throws IOException {
    sanitizeJavadocCustomParams();
    boolean successful = apply();
    if (successful) {
      try {
        DrJava.getConfig().saveConfiguration();
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,"Could not save changes to your '.drjava' file in your home directory. \n\n" + ioe,
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

    PanelTreeNode keystrokesNode = _createPanel("Key Bindings");
    _setupKeyBindingsPanel(keystrokesNode.getPanel());
    
    PanelTreeNode compilerOptionsNode = _createPanel("Compiler Options");
    _setupCompilerPanel(compilerOptionsNode.getPanel());
    
    PanelTreeNode debugNode = _createPanel("Debugger");
    _setupDebugPanel(debugNode.getPanel());

    PanelTreeNode javadocNode = _createPanel("Javadoc");
    _setupJavadocPanel(javadocNode.getPanel());

    PanelTreeNode notificationsNode = _createPanel("Notifications");
    _setupNotificationsPanel(notificationsNode.getPanel());
    
    PanelTreeNode miscNode = _createPanel("Miscellaneous");
    _setupMiscPanel(miscNode.getPanel());
    
    // Expand the display options node
    //DrJava.consoleOut().println("expanding path...");
    //_tree.expandPath(new TreePath(fontNode.getPath()));
  }

  public <X> void addOptionComponent(ConfigPanel panel, OptionComponent<X> oc) {
    panel.addComponent(oc);
    oc.addChangeListener(_changeListener);
  }
  
  /** Add all of the components for the Resource Locations panel of the preferences window. */
  private void _setupResourceLocPanel(ConfigPanel panel) {
    FileOptionComponent browserLoc =
      new FileOptionComponent(OptionConstants.BROWSER_FILE, "Web Browser", this,
                              "<html>Location of a web browser to use for Javadoc and Help links.<br>" +
                              "If left blank, only the Web Browser Command will be used.<br>" +
                              "This is not necessary if a default browser is available on your system.",
                              _browserChooser);
    addOptionComponent(panel, browserLoc);    

    StringOptionComponent browserCommand =
      new StringOptionComponent(OptionConstants.BROWSER_STRING, "Web Browser Command", this,
                              "<html>Command to send to the web browser to view a web location.<br>" +
                              "The string <code>&lt;URL&gt;</code> will be replaced with the URL address.<br>" +
                              "This is not necessary if a default browser is available on your system.");
    addOptionComponent(panel, browserCommand);

    FileOptionComponent javacLoc =
      new FileOptionComponent(OptionConstants.JAVAC_LOCATION, "Tools.jar Location", this,
                              "Optional location of the JDK's tools.jar, which contains the compiler and debugger.",
                              _fileOptionChooser);
    javacLoc.setFileFilter(ClassPathFilter.ONLY);
    addOptionComponent(panel, javacLoc);
   
    addOptionComponent(panel, new VectorFileOptionComponent(OptionConstants.EXTRA_CLASSPATH,
                                                     "Extra Classpath", this,
                                                     "<html>Any directories or jar files to add to the classpath<br>"+
                                                     "of the Compiler and Interactions Pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.STICKY_INTERACTIONS_DIRECTORY,
                                                         "Restore last working directory of the Interactions pane on start up", this,
                                                         "<html>Whether to restore the last working directory of the Interaction pane on start up,<br>"+
                                                         "or to always use the value of the \"user.home\" Java property<br>"+
                                                         "(currently "+System.getProperty("user.home")+")."));

    panel.displayComponents();
  }

  /** Add all of the components for the Display Options panel of the preferences window. */
  private void _setupDisplayPanel(ConfigPanel panel) {

    addOptionComponent(panel, new ForcedChoiceOptionComponent(OptionConstants.LOOK_AND_FEEL, "Look and Feel", this,
                                                              "Changes the general appearance of DrJava."));

    //ToolbarOptionComponent is a degenerate option component
    addOptionComponent(panel, new ToolbarOptionComponent("Toolbar Buttons", this,
                                                  "How to display the toolbar buttons."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.LINEENUM_ENABLED,
                                                  "Show All Line Numbers", this,
                                                  "Whether to show line numbers on the left side of the Definitions Pane."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WINDOW_STORE_POSITION,
                                                  "Save Main Window Position", this,
                                                  "Whether to save and restore the size and position of the main window."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_CLIPBOARD_HISTORY_STORE_POSITION,
                                                  "Save \"Clipboard History\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Clipboard History\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetClipboardHistoryDialogPosition(); }
    }, "Reset \"Clipboard History\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.CLIPBOARD_HISTORY_SIZE,
                                                  "Size of Clipboard History", this,
                                                  "Determines how many entries are kept in the clipboard history."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_STORE_POSITION,
                                                  "Save \"Go to File\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Go to File\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetGotoFileDialogPosition(); }
    }, "Reset \"Go to File\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_FULLY_QUALIFIED,
                                                  "Display Fully-Qualified Class Names in \"Go to File\" Dialog", this,
                                                  "<html>Whether to also display fully-qualified class names in the \"Go to File\" dialog.<br>"+
                                                         "Enabling this option on network drives might cause the dialog to display after a slight delay.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_COMPLETE_WORD_STORE_POSITION,
                                                  "Save \"Auto-Complete Word\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Auto-Complete Word\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetCompleteWordDialogPosition(); }
    }, "Reset \"Auto-Complete Word\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_JAROPTIONS_STORE_POSITION,
                                                  "Save \"Create Jar File from Project\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Create Jar File from Project\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetJarOptionsDialogPosition();
      }
    }, "Reset \"Create Jar File from Project\" Dialog Position", this, "This resets the dialog position to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
                                                  "Save \"Open Javadoc\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Open Javadoc\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetOpenJavadocDialogPosition(); }
    }, "Reset \"Open Javadoc\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_AUTOIMPORT_STORE_POSITION,
                                                  "Save \"Auto Import\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Auto Import\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetAutoImportDialogPosition(); }
    }, "Reset \"Auto Import\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_EXTERNALPROCESS_STORE_POSITION,
                                                  "Save \"Execute External Process\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Execute External Process\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetExecuteExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_EDITEXTERNALPROCESS_STORE_POSITION,
                                                  "Save \"Edit External Process\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Edit External Process\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetEditExternalProcessPosition();
      }
    }, "Reset \"Execute External Process\" Dialog Position", this, "This resets the dialog position to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_OPENJAVADOC_STORE_POSITION,
                                                  "Save \"Open Javadoc\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Open Javadoc\" dialog."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_COMPLETE_SCAN_CLASS_FILES,
                                                  "Scan Class Files After Each Compile For Auto-Completion and Auto-Import", this,
                                                  "<html>Whether to scan the class files after a compile to generate class names<br>"+
                                                         "used for auto-completion and auto-import.<br>"+
                                                         "Enabling this option will slow compiles down.</html>"));
    panel.displayComponents();
  }

  /** Add all of the components for the Font panel of the preferences window. */
  private void _setupFontPanel(ConfigPanel panel) {
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_MAIN, "Main Font", this,
                                               "The font used for most text in DrJava."));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_LINE_NUMBERS, "Line Numbers Font", this,
                                               "<html>The font for displaying line numbers on the left side of<br>" +
                                               "the Definitions Pane if Show All Line Numbers is enabled.<br>" +
                                               "Cannot be displayed larger than the Main Font.</html>"));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_DOCLIST, "Document List Font", this,
                                               "The font used in the list of open documents."));
    addOptionComponent(panel, new FontOptionComponent(OptionConstants.FONT_TOOLBAR, "Toolbar Font", this,
                                               "The font used in the toolbar buttons."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.TEXT_ANTIALIAS, "Use anti-aliased text", this,
                                                    "Whether to graphically smooth the text."));
    panel.displayComponents();
  }

  /**
   * Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel(ConfigPanel panel) {
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color", this,
                                                "The default color for text in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color", this,
                                                "The color for Java keywords in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color", this,
                                                "The color for classes and types in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color", this,
                                                "The color for comments in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color", this,
                                                "The color for quoted strings (eg. \"...\") in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color", this,
                                                "The color for quoted characters (eg. 'a') in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color", this,
                                                "The color for numbers in the Definitions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, "Background Color", this,
                                                "The background color of the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color", this,
                                                "The color for matching brace highlights in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.COMPILER_ERROR_COLOR, "Compiler Error Color", this,
                                                "The color for compiler error highlights in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.BOOKMARK_COLOR, "Bookmark Color", this,
                                                "The color for bookmarks in the Definitions Pane.", true));
    for (int i=0; i<OptionConstants.FIND_RESULTS_COLORS.length; ++i) {
      addOptionComponent(panel, new ColorOptionComponent(OptionConstants.FIND_RESULTS_COLORS[i], "Find Results Color "+(i+1), this,
                                                         "A color for highlighting find results in the Definitions Pane.", true));
    }
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_COLOR, "Debugger Breakpoint Color", this,
                                                "The color for breakpoints in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_DISABLED_COLOR, "Disabled Debugger Breakpoint Color", this,
                                                "The color for disabled breakpoints in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_THREAD_COLOR, "Debugger Location Color", this,
                                                "The color for the location of the current suspended thread in the Definitions Pane.", true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_OUT_COLOR, "System.out Color", this,
                                                "The color for System.out in the Interactions and Console Panes."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_ERR_COLOR, "System.err Color", this,
                                                "The color for System.err in the Interactions and Console Panes."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.SYSTEM_IN_COLOR, "System.in Color", this,
                                                "The color for System.in in the Interactions Pane."));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.INTERACTIONS_ERROR_COLOR, "Interactions Error Color", this,
                                                "The color for interactions errors in the Interactions Pane.", false, true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_MESSAGE_COLOR, "Debug Message Color", this,
                                                "The color for debugger messages in the Interactions Pane.", false, true));
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DRJAVA_ERRORS_BUTTON_COLOR, "DrJava Errors Button Background Color", this,
                                                "The background color of the \"Errors\" button used to show internal DrJava errors.", true));

    panel.displayComponents();
  }

  /**
   * Adds all of the components for the Key Bindings panel of the preferences window.
   */
  private void _setupKeyBindingsPanel(ConfigPanel panel) {
    // using a treeset because it automatically sorts element upon insertion
    TreeSet<KeyStrokeOptionComponent> _comps = new TreeSet<KeyStrokeOptionComponent>();

    KeyStrokeData tmpKsd;
    KeyStrokeOptionComponent tmpKsoc;

    Enumeration e = KeyBindingManager.Singleton.getKeyStrokeData();
    while (e.hasMoreElements()) {
      tmpKsd = (KeyStrokeData) e.nextElement();
      if (tmpKsd.getOption() != null) {
        // Get the tooltip, or default to its name, if none
//        KeyStroke ks = tmpKsd.getKeyStroke();
//        Action a = KeyBindingManager.Singleton.get(ks);
        Action a = tmpKsd.getAction();
        String desc = (String) a.getValue(Action.SHORT_DESCRIPTION);
        if ((desc == null) || (desc.equals(""))) {
          desc = tmpKsd.getName();
        }

        tmpKsoc = new KeyStrokeOptionComponent((KeyStrokeOption)tmpKsd.getOption(),
                                               tmpKsd.getName(), this, desc);
        if (tmpKsoc != null) {
          _comps.add(tmpKsoc);
        }
      }
    }

    Iterator<KeyStrokeOptionComponent> iter = _comps.iterator();
    while (iter.hasNext()) {
      KeyStrokeOptionComponent x = iter.next();
      addOptionComponent(panel, x);
    }
    panel.displayComponents();
  }

  /**
   * Add all of the components for the Debugger panel of the preferences window.
   */
  private void _setupDebugPanel(ConfigPanel panel) {
    if (!_mainFrame.getModel().getDebugger().isAvailable()) {
      // Explain how to use debugger
      String howto =
        "\nThe debugger is not currently active.  To use the debugger, you\n" +
        "must include Sun's tools.jar or jpda.jar on your classpath when\n" +
        "starting DrJava.  Do not use the \"-jar\" option, because it\n" +
        "overrides the classpath and will not include tools.jar.\n" +
        "For example, in Windows you might type:\n\n" +
        "  java -classpath drjava.jar;c:\\path\\tools.jar edu.rice.cs.drjava.DrJava\n\n" +
        "(Substituting the correct path for tools.jar.)\n" +
        "See the user documentation for more details.\n";
        addOptionComponent(panel, new LabelComponent(howto, this));
    }

    VectorFileOptionComponent sourcePath =
      new VectorFileOptionComponent(OptionConstants.DEBUG_SOURCEPATH, "Sourcepath", this,
                                    "<html>Any directories in which to search for source<br>" +
                                    "files when stepping in the Debugger.</html>");
    // Source path can only include directories
    sourcePath.setFileFilter(new DirectoryFilter("Source Directories"));
    addOptionComponent(panel, sourcePath);
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_STEP_JAVA,
                                                  "Step Into Java Classes", this,
                                                  "<html>Whether the Debugger should step into Java library classes,<br>" +
                                                  "including java.*, javax.*, sun.*, com.sun.*, com.apple.eawt.*, and com.apple.eio.*</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_STEP_INTERPRETER,
                                                  "Step Into Interpreter Classes", this,
                                                  "<html>Whether the Debugger should step into the classes<br>" +
                                                  "used by the Interactions Pane (DynamicJava).</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DEBUG_STEP_DRJAVA,
                                                  "Step Into DrJava Classes", this,
                                                  "Whether the Debugger should step into DrJava's own class files."));
    addOptionComponent(panel, new StringOptionComponent(OptionConstants.DEBUG_STEP_EXCLUDE,
                                                 "Classes/Packages To Exclude", this,
                                                 "<html>Any classes that the debuggger should not step into.<br>" +
                                                 "Should be a COMMA-separated list of fully-qualified class names.<br>" +
                                                 "To exclude a package, specify <code>packagename.*</code> in the list.</html>"));

    panel.displayComponents();
  }

  /** Add all of the components for the Javadoc panel of the preferences window. */
  private void _setupJavadocPanel(ConfigPanel panel) {
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_ACCESS_LEVEL,
                                                       "Access Level", this,
                                                       "<html>Fields and methods with access modifiers at this level<br>" +
                                                       "or higher will be included in the generated Javadoc.</html>"));
    addOptionComponent(panel, 
                       new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_LINK_VERSION,
                                                       "Java Version for Javadoc Links", this,
                                                       "The version of Java for generating links to online Javadoc documentation."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JAVADOC_1_3_LINK,
                                                 "Javadoc 1.3 URL", this,
                                                 "The URL to the Java 1.3 API, for generating links to library classes."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JAVADOC_1_4_LINK,
                                                 "Javadoc 1.4 URL", this,
                                                 "The URL to the Java 1.4 API, for generating links to library classes."));
    addOptionComponent(panel, 
                       new StringOptionComponent(OptionConstants.JAVADOC_1_5_LINK,
                                                 "Javadoc 1.5 URL", this,
                                                 "The URL to the Java 1.5 API, for generating links to library classes."));
    
    addOptionComponent(panel, 
                       new DirectoryOptionComponent(OptionConstants.JAVADOC_DESTINATION,
                                                    "Default Destination Directory", this,
                                                    "Optional default directory for saving Javadoc documentation.",
                                                    _dirChooser));
    
    addOptionComponent(panel, 
                       javadocCustomParams = new StringOptionComponent(OptionConstants.JAVADOC_CUSTOM_PARAMS,
                                                 "Custom Javadoc Parameters", this,
                                                 "Any extra flags or parameters to pass to Javadoc."));
    
    addOptionComponent(panel, 
                       new BooleanOptionComponent(OptionConstants.JAVADOC_FROM_ROOTS,
                                                  "Generate Javadoc From Source Roots", this,
                                                  "<html>Whether 'Javadoc All' should generate Javadoc for all packages<br>" +
                                                  "in an open document's source tree, rather than just the document's<br>" +
                                                  "own package and sub-packages.</html>"));
    
    panel.displayComponents();
  }

  /** Adds all of the components for the Prompts panel of the preferences window. */
  private void _setupNotificationsPanel(ConfigPanel panel) {
    // Quit
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.QUIT_PROMPT, "Prompt Before Quit", this,
                                                  "Whether DrJava should prompt the user before quitting."));

    // Interactions
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.INTERACTIONS_RESET_PROMPT,
                                                  "Prompt Before Resetting Interactions Pane", this,
                                                  "<html>Whether DrJava should prompt the user before<br>" +
                                                  "manually resetting the interactions pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.INTERACTIONS_EXIT_PROMPT,
                                                  "Prompt if Interactions Pane Exits Unexpectedly", this,
                                                  "<html>Whether DrJava should show a dialog box if a program<br>" +
                                                  "in the Interactions Pane exits without the user clicking Reset.</html>"));

    // Javadoc
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
                                                  "Prompt for Javadoc Destination", this,
                                                  "<html>Whether Javadoc should always prompt the user<br>" +
                                                  "to select a destination directory.</html>"));


    // Clean
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.PROMPT_BEFORE_CLEAN,
                                                  "Prompt before Cleaning Build Directory", this,
                                                  "<html>Whether DrJava should prompt before cleaning the<br>" +
                                                    "build directory of a project</html>"));

    
    // Save before X
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_COMPILE,
                                                  "Automatically Save Before Compiling", this,
                                                  "<html>Whether DrJava should automatically save before<br>" +
                                                  "recompiling or ask the user each time.</html>"));
    
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_COMPILE_BEFORE_JUNIT, "Automatically Compile Before Testing", this,
                                                  "<html>Whether DrJava should automatically compile before<br>" +
                                                  "testing with JUnit or ask the user each time.</html>")); 
    
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_JAVADOC,
                                                  "Automatically Save Before Generating Javadoc", this,
                                                  "<html>Whether DrJava should automatically save before<br>" +
                                                  "generating Javadoc or ask the user each time.</html>"));


    // These are very problematic features, and so are disabled for the forseeable future.
//    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_RUN, "Automatically Save and Compile Before Running Main Method", this,
//                                                    "<html>Whether DrJava should automatically save and compile before running<br>" +
//                                                    "a document's main method, or instead should ask the user each time.</html>"));
//    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_DEBUG, "Automatically Save and Compile Before Debugging", this,
//                                                  "<html>Whether DrJava should automatically save and compile before<br>" +
//                                                  "debugging or ask the user each time.</html>"));
    

    // Warnings
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WARN_BREAKPOINT_OUT_OF_SYNC,
                                                  "Warn on Breakpoint if Out of Sync", this,
                                                  "<html>Whether DrJava should warn the user if the class file<br>" +
                                                  "is out of sync before setting a breakpoint in that file.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WARN_DEBUG_MODIFIED_FILE,
                                                  "Warn if Debugging Modified File", this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "debugged has been modified since its last save.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WARN_CHANGE_LAF,
                                                  "Warn to Restart to Change Look and Feel", this,
                                                  "<html>Whether DrJava should warn the user that look and feel.<br>" +
                                                  "(Changes will not be applied until DrJava is restarted.)</html>."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WARN_PATH_CONTAINS_POUND,
                                                  "Warn if File's Path Contains a '#' Symbol", this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "saved has a path that contains a '#' symbol.<br>" +
                                                  "Users cannot use such files in the Interactions Pane<br>" +
                                                  "because of a bug in Java.</html>"));

    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED, 
                                                  "Show a notification window when the first DrJava error occurs", this,
                                                  "<html>Whether to show a notification window when the first DrJava error occurs.<br>"+
                                                  "If this is disabled, only the \"DrJava Error\" button will appear.</html>"));

    panel.displayComponents();
  }

  /** Adds all of the components for the Miscellaneous panel of the preferences window. */
  private void _setupMiscPanel(ConfigPanel panel) {
    /* Dialog box options */
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.INDENT_LEVEL,
                                                  "Indent Level", this,
                                                  "The number of spaces to use for each level of indentation."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History", this,
                                                  "The number of interactions to remember in the history."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size", this,
                                                  "<html>The number of files to remember in<br>" +
                                                  "the recently used files list in the File menu.</html>"));
    addOptionComponent(panel, new StringOptionComponent(OptionConstants.MASTER_JVM_ARGS, "JVM Args for Main JVM", this,
                                                 "The command-line arguments to pass to the Main JVM."));
    addOptionComponent(panel, new StringOptionComponent(OptionConstants.SLAVE_JVM_ARGS, "JVM Args for Interactions JVM", this,
                                                 "The command-line arguments to pass to the Interactions JVM."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.BROWSER_HISTORY_MAX_SIZE,
                                                         "Maximum Size of Browser History", this,
                                                         "Determines how many entries are kept in the browser history."));
    
    /* Check box options */
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.AUTO_CLOSE_COMMENTS, "Automatically Close Block Comments", this,
                                                  "<html>Whether to automatically insert a closing comment tag (\"*/\")<br>" +
                                                  "when the enter key is pressed after typing a new block comment<br>" +
                                                  "tag (\"/*\" or \"/**\").</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.RUN_WITH_ASSERT, "Enable Assert Statement Execution", this,
                                                  "<html>Whether to execute <code>assert</code> statements in classes running in the interactions pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.BACKUP_FILES, "Keep Emacs-style Backup Files", this,
                                                  "<html>Whether DrJava should keep a backup copy of each file that<br>" +
                                                  "the user modifies, saved with a '~' at the end of the filename.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.RESET_CLEAR_CONSOLE, "Clear Console After Interactions Reset", this,
                                                  "Whether to clear the Console output after resetting the Interactions Pane."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.ALLOW_PRIVATE_ACCESS, "Allow Access of Private Members in Interactions Pane", this,
                                                  "Whether to allow users to access private (and protected) fields and methods."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_SOURCE_WHEN_SWITCHING, "Show sample of source code when fast switching", this,
                                                  "Whether to show a sample of the source code under the document's filename when fast switching documents."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.FORCE_TEST_SUFFIX, 
                                                  "Require test classes in projects to end in \"Test\"", this,
                                                  "Whether to force test classes in projects to end in \"Test\"."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE, 
                                                  "Put the focus in the definitions pane after find/replace", this,
                                                  "<html>Whether to put the focus in the definitions pane after doing a find or replace operation.<br>"+
                                                  "If this is not selected, the focus will be in the Find/Replace pane.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_CODE_PREVIEW_POPUPS, 
                                                  "Show Code Preview Popups", this,
                                                  "<html>Whether to show a popup window with a code preview when the mouse is hovering<br>"+
                                                  "over an item in the Breakpoints, Bookmarks and Find All panes.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DRJAVA_USE_FORCE_QUIT, 
                                                  "Forcefully Quit DrJava", this,
                                                  "<html>On some platforms, DrJava does not shut down properly when files are open<br>"+
                                                  "(namely tablet PCs). Check this option to force DrJava to close.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.REMOTE_CONTROL_ENABLED, 
                                                  "Enable Remote Control", this,
                                                  "<html>Whether DrJava should listen to a socket (see below) so it<br>"+
                                                  "can be remote controlled and told to open files.<br>"+
                                                  "(Changes will not be applied until DrJava is restarted.)</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.REMOTE_CONTROL_PORT, 
                                                  "Remote Control Port", this,
                                                  "<html>A running instance of DrJava can be remote controlled and<br>"+
                                                  "told to open files. This specifies the port used for remote control.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_AUTOIMPORT_ENABLED, 
                                                  "Enable the \"Auto Import\" Dialog", this,
                                                  "<html>Whether DrJava should open the \"Auto Import\" dialog when<br>"+
                                                  "an undefined class is encountered in the Interactions Pane.</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.FOLLOW_FILE_DELAY, 
                                                  "Follow File Delay", this,
                                                  "<html>The delay in milliseconds that has to elapse before DrJava will check<br>"+
                                                  "if a file that is being followed or the output of an external process has changed.</html>"));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.FOLLOW_FILE_LINES, 
                                                  "Maximum Lines in \"Follow File\" Window", this,
                                                  "<html>The maximum number of lines to keep in a \"Follow File\"<br>"+
                                                  "or \"External Process\" pane. Enter 0 for unlimited.</html>"));
    
// Any lightweight parsing has been disabled until we have something that is beneficial and works better in the background.
//    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.LIGHTWEIGHT_PARSING_ENABLED, 
//                                                  "Perform lightweight parsing", this,
//                                                  "<html>Whether to continuously parse the source file for useful information.<br>" +
//                                                  "Enabling this option might introduce delays when editing files.<html>"));
//    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.DIALOG_LIGHTWEIGHT_PARSING_DELAY, "Light-weight parsing delay in milliseconds", this,
//                                                  "The amount of time DrJava will wait after the last keypress before beginning to parse."));

    panel.displayComponents();
  }
  
  /**
   * Adds all of the components for the Compiler Options Panel of the preferences window
   */
  private void _setupCompilerPanel(ConfigPanel panel) {
    addOptionComponent(panel, new LabelComponent("Note: Compiler warnings not shown if compiling any Java language level files", this));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_UNCHECKED_WARNINGS, "Show Unchecked Warnings", this, 
                                                  "<html>Warn about unchecked conversions involving parameterized types.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_DEPRECATION_WARNINGS, "Show Deprecation Warnings", this, 
                                                  "<html>Warn about each use or override of a deprecated method, field, or class.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_PATH_WARNINGS, "Show Path Warnings", this, 
                                                  "<html>Warn about nonexistent members of the classpath and sourcepath.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_SERIAL_WARNINGS, "Show Serial Warnings", this, 
                                                  "<html>Warn about missing <code>serialVersionUID</code> definitions on serializable classes.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_FINALLY_WARNINGS, "Show Finally Warnings", this,
                                                  "<html>Warn about <code>finally</code> clauses that cannot complete normally.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_FALLTHROUGH_WARNINGS, "Show Fall-Through Warnings", this,
                                                  "<html>Warn about <code>switch</code> block cases that fall through to the next case.</html>"));
    panel.displayComponents();
    
  }
  
  
  /** cleans access level parameters from custom javadoc parameters. */
  private void sanitizeJavadocCustomParams() {
    String[] params = ((JTextField)javadocCustomParams.getComponent()).getText().split("(-private|-protected|-package|-public)");
    String newParams = new String();
    for(int i=0;i<params.length;i++){
      if(!params[i].trim().equals("")) { newParams += params[i].trim() + " ";}
    }
    newParams = newParams.trim();
    javadocCustomParams.setValue(newParams);
    
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

      Enumeration childNodes = children();
      while (childNodes.hasMoreElements()) {
        boolean isValidUpdateChildren = ((PanelTreeNode)childNodes.nextElement()).update();
        //if any of the children nodes encountered an error, return false
        if (!isValidUpdateChildren) {
          return false;
        }
      }

      return true;
    }

    /**
     * Tells its panel to reset its displayed value to the currently set value
     * for this component, and tells all of its children to reset their panels.
     */
    public void resetToCurrent() {
      _panel.resetToCurrent();

      Enumeration childNodes = this.children();
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
