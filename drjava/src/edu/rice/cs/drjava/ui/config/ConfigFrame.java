/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

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

  private MainFrame _mainFrame;

//  private JSplitPane _splitPane;
  private JTree _tree;
  private DefaultTreeModel _treeModel;
  private PanelTreeNode _rootNode;

  private JButton _okButton;
  private JButton _applyButton;
  private JButton _cancelButton;
//  private JButton _saveSettingsButton;
  private JPanel _mainPanel;
  private JFileChooser _fileOptionChooser;
  private JFileChooser _browserChooser;
  private DirectoryChooser _dirChooser;
  
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

    File workDir = _getWorkDir();
    _fileOptionChooser = new JFileChooser(workDir);
    _fileOptionChooser.setDialogTitle("Select");
    _fileOptionChooser.setApproveButtonText("Select");
    _fileOptionChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _fileOptionChooser.setFileFilter(ClassPathFilter.ONLY);

    _browserChooser = new JFileChooser(workDir);
    _browserChooser.setDialogTitle("Select Web Browser");
    _browserChooser.setApproveButtonText("Select");
    _browserChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    _dirChooser = new DirectoryChooser(this);
    _dirChooser.setSelectedFile(_getWorkDir());
    _dirChooser.setDialogTitle("Select");
    _dirChooser.setApproveButtonText("Select");
    _dirChooser.setMultiSelectionEnabled(false);
    
    _createTree();
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
    /*
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                treeScroll,
                                _mainPanel);
    cp.add(_splitPane, BorderLayout.CENTER);
    */

    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        try {
          successful = saveSettings();
        }
        catch (IOException ioe) {
          // oh well...
        }
        if (successful) {
          _applyButton.setEnabled(false);
        }
        ConfigFrame.this.setVisible(false);
      }
    };
    _okButton = new JButton(okAction);

    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        try { saveSettings(); _applyButton.setEnabled(false); }
        catch (IOException ioe) {
        }
      }
    };
    _applyButton = new JButton(applyAction);
    _applyButton.setEnabled(false);

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    };
    _cancelButton = new JButton(cancelAction);

    /* Now always saves settings...
    _saveSettingsButton = new JButton("Save Settings");
    _saveSettingsButton.setToolTipText("Save all settings to disk for future sessions.");
    _saveSettingsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
      }
    });
    */

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
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();

    if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;

    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
//    int width = getWidth() / 4;
//    System.out.println("width: " + getWidth());
//    System.out.println("width for divider: " + width);
//    _splitPane.setDividerLocation(width);
//    _mainPanel.setPreferredSize(new Dimension(getWidth() - width,
//                                              _splitPane.getHeight()));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) { cancel(); }
    });

    // Make sure each row is expanded (this is harder than it seems...)
    _tree.expandRow(0);
    _tree.expandRow(1);
    _tree.expandRow(2);
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
  private void _createTree() {

    _rootNode = new PanelTreeNode("Preferences");
    _treeModel = new DefaultTreeModel(_rootNode);
    _tree = new JTree(_treeModel);
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
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
//  private PanelTreeNode _createPanel(ConfigPanel c, PanelTreeNode parent) {
//    PanelTreeNode ptNode = new PanelTreeNode(c);
//    parent.add(ptNode);
//
//    return ptNode;
//  }
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node. Adds to the root node.
   * @param t the title of this panel
   * @return this tree node
   */
  private PanelTreeNode _createPanel(String t) {
    return _createPanel(t, _rootNode);
  }


  /**
   * Creates all of the panels contained within the frame.
   */
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
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_STORE_POSITION,
                                                  "Save \"Go to File\" Dialog Position", this,
                                                  "Whether to save and restore the size and position of the \"Go to File\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) { _mainFrame.resetGotoFileDialogPosition(); }
    }, "Reset \"Go to File\" Dialog Position and Size", this, "This resets the dialog position and size to its default values."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_GOTOFILE_FULLY_QUALIFIED,
                                                  "Display Fully-Qualified Class Names in \"Go to File\" Dialog", this,
                                                  "Whether to also display fully-qualified class names in the \"Go to File\" dialog.\n"+
                                                         "Enabling this option on network drives might cause the dialog to display after a slight delay."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.DIALOG_JAROPTIONS_STORE_POSITION,
                                                  "Save \"Create Jar File from Project\" Dialog Position", this,
                                                  "Whether to save and restore the position of the \"Create Jar File from Project\" dialog."));
    addOptionComponent(panel, new ButtonComponent(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _mainFrame.resetJarOptionsDialogPosition();
      }
    }, "Reset \"Create Jar File from Project\" Dialog Position", this, "This resets the dialog position to its default values."));
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
    if (CodeStatus.DEVELOPMENT) {
      addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.TEXT_ANTIALIAS, "Use anti-aliased text", this,
                                                    "Whether to graphically smooth the text."));
    }
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
    addOptionComponent(panel, new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_COLOR, "Debugger Breakpoint Color", this,
                                                "The color for breakpoints in the Definitions Pane.", true));
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
                                                  "including java.*, javax.*, sun.*, com.sun.*, and com.apple.mrj.*</html>"));
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
                       new StringOptionComponent(OptionConstants.JAVADOC_CUSTOM_PARAMS,
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
                                                  "<html>Whether DrJava should warn the user that look and feel<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>."));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.WARN_PATH_CONTAINS_POUND,
                                                  "Warn if File's Path Contains a '#' Symbol", this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "saved has a path that contains a '#' symbol.<br>" +
                                                  "Users cannot use such files in the Interactions Pane<br>" +
                                                  "because of a bug in Java.</html>"));

    panel.displayComponents();
  }

  /** Adds all of the components for the Miscellaneous panel of the preferences window. */
  private void _setupMiscPanel(ConfigPanel panel) {
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.INDENT_LEVEL,
                                                  "Indent Level", this,
                                                  "The number of spaces to use for each level of indentation."));
    
    addOptionComponent(panel, new DirectoryOptionComponent(OptionConstants.WORKING_DIRECTORY,
                                                    "Working Directory", this,
                                                    "The working directory for the DrJava editor and GUI interface.",
                                                    _dirChooser));

    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History", this,
                                                  "The number of interactions to remember in the history."));
    addOptionComponent(panel, new IntegerOptionComponent(OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size", this,
                                                  "<html>The number of files to remember in<br>" +
                                                  "the recently used files list in the File menu.</html>"));

    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.AUTO_CLOSE_COMMENTS, "Automatically Close Block Comments", this,
                                                  "<html>Whether to automatically insert a closing comment tag (\"*/\")<br>" +
                                                  "when the enter key is pressed after typing a new block comment<br>" +
                                                  "tag (\"/*\" or \"/**\").</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.JAVAC_ALLOW_ASSERT, "Allow Assert Keyword in Java 1.4", this,
                                                  "<html>Whether to allow the <code>assert</code> keyword when compiling in Java 1.4.</html>"));
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

    addOptionComponent(panel, new StringOptionComponent(OptionConstants.JVM_ARGS, "JVM Args for Interactions", this,
                                                 "The command-line arguments to pass to the Interactions JVM."));

    panel.displayComponents();
  }
  
  /**
   * Adds all of the components for the Compiler Options Panel of the preferences window
   */
  private void _setupCompilerPanel(ConfigPanel panel) {
    addOptionComponent(panel, new LabelComponent("Note: Compiler warnings not shown if compiling any Java language level files", this));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_UNCHECKED_WARNINGS, "Show Unchecked Warnings", this, 
                                                  "<html>Give more detail for unchecked conversion warnings that are mandated<br>" + 
                                                  "by the Java Language Specification.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_DEPRECATION_WARNINGS, "Show Deprecation Warnings", this, 
                                                  "<html>Show a description of each use or override of a deprecated member or class.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_PATH_WARNINGS, "Show Path Warnings", this, 
                                                  "<html>Warn about nonexistent path (classpath, sourcepath, etc) directories.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_SERIAL_WARNINGS, "Show Serial Warnings", this, 
                                                  "<html>Warn about missing <code>serialVersionUID</code> definitions on serializable classes.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_FINALLY_WARNINGS, "Show Finally Warnings", this,
                                                  "<html>Warn about <code>finally<code> clauses that cannot complete normally.</html>"));
    addOptionComponent(panel, new BooleanOptionComponent(OptionConstants.SHOW_FALLTHROUGH_WARNINGS, "Show Fall-Through Warnings", this,
                                                  "<html>Check <code>switch</code> blocks for fall-through cases and provide a warning message for any that are found.<br>"+
                                                  "Fall-through cases are cases in a <code>switch</code> block, other than the last case in the block,<br>"+
                                                  "whose code does not include a <code>break</code> statement, allowing code execution to \"fall through\"<br>"+
                                                  "from that case to the next case.</html>"));
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
