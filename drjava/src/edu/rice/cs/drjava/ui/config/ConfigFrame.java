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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
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
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;

/**
 * The frame for setting Configuration options on the fly
 * @version $Id$
 */
public class ConfigFrame extends JFrame {

  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 500;

  private MainFrame _mainFrame;

  private JSplitPane _splitPane;
  private JTree _tree;
  private DefaultTreeModel _treeModel;
  private PanelTreeNode _rootNode;

  private JButton _okButton;
  private JButton _applyButton;
  private JButton _cancelButton;
  private JButton _saveSettingsButton;
  private JPanel _mainPanel;
  private JFileChooser _fileOptionChooser;
  private JFileChooser _browserChooser;

  /**
   * Sets up the frame and displays it.
   */
  public ConfigFrame(MainFrame frame) {
    super("Preferences");

    _mainFrame = frame;

    File workDir = _getWorkDir();
    _fileOptionChooser = new JFileChooser(workDir);
    _fileOptionChooser.setDialogTitle("Select");
    _fileOptionChooser.setApproveButtonText("Select");
    _fileOptionChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    _fileOptionChooser.setFileFilter(ClasspathFilter.ONLY);

    _browserChooser = new JFileChooser(workDir);
    _browserChooser.setDialogTitle("Select Web Browser");
    _browserChooser.setApproveButtonText("Select");
    _browserChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

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
    treeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                         "Categories"));
    treePanel.add(treeScroll, BorderLayout.CENTER);
    cp.add(treePanel, BorderLayout.WEST);
    cp.add(_mainPanel, BorderLayout.CENTER);
    /*
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                treeScroll,
                                _mainPanel);
    cp.add(_splitPane, BorderLayout.CENTER);
    */

    _okButton = new JButton("OK");
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        try {
          successful = saveSettings();
        }
        catch (IOException ioe) {}
        if (successful) ConfigFrame.this.hide();
      }
    });

    _applyButton = new JButton("Apply");
    _applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        try {
          saveSettings();
        }
        catch (IOException ioe) {}
      }
    });

    _cancelButton = new JButton("Cancel");
    _cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });

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

    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }

    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);
//    int width = getWidth() / 4;
//    System.out.println("width: " + getWidth());
//    System.out.println("width for divider: " + width);
//    _splitPane.setDividerLocation(width);
//    _mainPanel.setPreferredSize(new Dimension(getWidth() - width,
//                                              _splitPane.getHeight()));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        cancel();
      }
    });

    // Make sure each row is expanded (this is harder than it seems...)
    _tree.expandRow(0);
    _tree.expandRow(1);
    _tree.expandRow(2);
  }

  /**
   * Returns the current working directory, or the user's current directory
   * if none is set.
   */
  private File _getWorkDir() {
    File workDir = DrJava.getConfig().getSetting(OptionConstants.WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File(System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    return workDir;
  }

  /**
   * Call the update method to propagate down the tree, parsing input values
   * into their config options.
   */
  public boolean apply() {
    // returns false if the update did not succeed
    return _rootNode.update();
  }

  /**
   * Resets the field of each option in the Preferences window to its actual
   * stored value.
   */
  public void resetToCurrent() {
    _rootNode.resetToCurrent();
  }

  /**
   * Resets the frame and hides it.
   */
  public void cancel() {
    resetToCurrent();
    ConfigFrame.this.hide();
  }

  /**
   * Write the configured option values to disk.
   */
  public boolean saveSettings() throws IOException {
    boolean successful = apply();
    if (successful) {
      try {
        DrJava.getConfig().saveConfiguration();
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,
                                      "Could not save changes to your '.drjava' file \n" +
                                      "in your home directory. Another process may be \n" +
                                      "using the file.\n\n" + ioe,
                                      "Could Not Save Changes",
                                      JOptionPane.ERROR_MESSAGE);
        //return false;
        throw ioe;
      }
    }
    return successful;
  }

  /**
   * Sets the given ConfigPanel as the visible panel.
   */
  private void _displayPanel(ConfigPanel cf) {

    _mainPanel.removeAll();
    _mainPanel.add(cf, BorderLayout.CENTER);
    _mainPanel.revalidate();
    _mainPanel.repaint();

  }

  /**
   * Creates the JTree to display preferences categories.
   */
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

  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node.
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

  /**
   * Add all of the components for the Resource Locations panel of the preferences window.
   */
  private void _setupResourceLocPanel(ConfigPanel panel) {
    FileOptionComponent browserLoc =
      new FileOptionComponent(OptionConstants.BROWSER_FILE,
                              "Web Browser", this,
                              "<html>Location of a web browser to use for Javadoc and Help links.<br>" +
                              "If left blank, only the Web Browser Command will be used.<br>" +
                              "This is not necessary if a default browser is available on your system.",
                              _browserChooser);
    panel.addComponent(browserLoc);

    StringOptionComponent browserCommand =
      new StringOptionComponent(OptionConstants.BROWSER_STRING,
                              "Web Browser Command", this,
                              "<html>Command to send to the web browser to view a web location.<br>" +
                              "The string <code>&lt;URL&gt;</code> will be replaced with the URL address.<br>" +
                              "This is not necessary if a default browser is available on your system.");
    panel.addComponent(browserCommand);

    FileOptionComponent javacLoc =
      new FileOptionComponent(OptionConstants.JAVAC_LOCATION,
                              "Tools.jar Location", this,
                              "Optional location of the JDK's tools.jar, which contains the compiler and debugger.",
                              _fileOptionChooser);
    javacLoc.setFileFilter(ClasspathFilter.ONLY);
    panel.addComponent(javacLoc);
    FileOptionComponent jsr14Loc =
      new FileOptionComponent(OptionConstants.JSR14_LOCATION,
                              "JSR-14 Location", this,
                              "Optional location of the JSR-14 compiler, for compiling with generics.",
                              _fileOptionChooser);
    jsr14Loc.setFileFilter(ClasspathFilter.ONLY);
    panel.addComponent(jsr14Loc);
    FileOptionComponent jsr14Col =
      new FileOptionComponent(OptionConstants.JSR14_COLLECTIONSPATH,
                              "JSR-14 Collections Path", this,
                              "Optional location of the JSR-14 collect.jar file, which contains the collection classes.",
                              _fileOptionChooser);
    jsr14Col.setFileFilter(ClasspathFilter.ONLY);
    panel.addComponent(jsr14Col);
//    VectorClassnameOptionComponent extraCompilers =
//      new VectorClassnameOptionComponent(OptionConstants.EXTRA_COMPILERS, "Custom Compilers", this,
//                                         "<html>Class names for custom compilers.  These compilers should<br>" +
//                                         "implement edu.rice.cs.drjava.model.compiler.CompilerInterface.</html>");
//    panel.addComponent(extraCompilers);
    panel.addComponent(new VectorFileOptionComponent(OptionConstants.EXTRA_CLASSPATH,
                                                 "Extra Classpath", this,
                                                 "<html>Any directories or jar files to add to the classpath<br>"+
                                                 "of the Compiler and Interactions Pane.</html>"));

    panel.displayComponents();
  }

  /**
   * Add all of the components for the Display Options panel of the preferences window.
   */
  private void _setupDisplayPanel(ConfigPanel panel) {

    panel.addComponent(new ForcedChoiceOptionComponent(OptionConstants.LOOK_AND_FEEL,
                                                       "Look and Feel", this,
                                                       "Changes the general appearance of DrJava."));

    //ToolbarOptionComponent is a degenerate option component
    panel.addComponent(new ToolbarOptionComponent("Toolbar Buttons", this,
                                                  "How to display the toolbar buttons."));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.LINEENUM_ENABLED,
                                                  "Show All Line Numbers", this,
                                                  "Whether to show line numbers on the left side of the Definitions Pane."));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.WINDOW_STORE_POSITION,
                                                  "Save Main Window Position", this,
                                                  "Whether to save amd restore the size and position of the main window."));
    panel.displayComponents();
  }

  /**
   * Add all of the components for the Font panel of the preferences window.
   */
  private void _setupFontPanel(ConfigPanel panel) {
    panel.addComponent(new FontOptionComponent(OptionConstants.FONT_MAIN, "Main Font", this,
                                               "The font used for most text in DrJava."));
    panel.addComponent(new FontOptionComponent(OptionConstants.FONT_LINE_NUMBERS, "Line Numbers Font", this,
                                               "<html>The font for displaying line numbers on the left side of<br>" +
                                               "the Definitions Pane if Show All Line Numbers is enabled.<br>" +
                                               "Cannot be displayed larger than the Main Font.</html>"));
    panel.addComponent(new FontOptionComponent(OptionConstants.FONT_DOCLIST, "Document List Font", this,
                                               "The font used in the list of open documents."));
    panel.addComponent(new FontOptionComponent(OptionConstants.FONT_TOOLBAR, "Toolbar Font", this,
                                               "The font used in the toolbar buttons."));
    if (CodeStatus.DEVELOPMENT) {
      panel.addComponent(new BooleanOptionComponent(OptionConstants.TEXT_ANTIALIAS, "Use anti-aliased text in Definitions", this,
                                                    "Whether to graphically smooth the text in the Definitions Pane."));
    }
    panel.displayComponents();
  }

  /**
   * Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel(ConfigPanel panel) {
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color", this,
                                                "The default color for text in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color", this,
                                                "The color for Java keywords in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color", this,
                                                "The color for classes and types in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color", this,
                                                "The color for comments in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color", this,
                                                "The color for quoted strings (eg. \"...\") in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color", this,
                                                "The color for quoted characters (eg. 'a') in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color", this,
                                                "The color for numbers in the Definitions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, "Background Color", this,
                                                "The background color of the Definitions Pane.", true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color", this,
                                                "The color for matching brace highlights in the Definitions Pane.", true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.COMPILER_ERROR_COLOR, "Compiler Error Color", this,
                                                "The color for compiler error highlights in the Definitions Pane.", true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEBUG_BREAKPOINT_COLOR, "Debugger Breakpoint Color", this,
                                                "The color for breakpoints in the Definitions Pane.", true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEBUG_THREAD_COLOR, "Debugger Location Color", this,
                                                "The color for the location of the current suspended thread in the Definitions Pane.", true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.SYSTEM_OUT_COLOR, "System.out Color", this,
                                                "The color for System.out in the Interactions and Console Panes."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.SYSTEM_ERR_COLOR, "System.err Color", this,
                                                "The color for System.err in the Interactions and Console Panes."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.SYSTEM_IN_COLOR, "System.in Color", this,
                                                "The color for System.in in the Interactions Pane."));
    panel.addComponent(new ColorOptionComponent(OptionConstants.INTERACTIONS_ERROR_COLOR, "Interactions Error Color", this,
                                                "The color for interactions errors in the Interactions Pane.", false, true));
    panel.addComponent(new ColorOptionComponent(OptionConstants.DEBUG_MESSAGE_COLOR, "Debug Message Color", this,
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
      panel.addComponent(x);
    }
    panel.displayComponents();
  }

  /**
   * Add all of the components for the Debugger panel of the preferences window.
   */
  private void _setupDebugPanel(ConfigPanel panel) {
    if(!_mainFrame.getModel().getDebugger().isAvailable()) {
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
        panel.addComponent(new LabelComponent(howto, this));
    }

    VectorFileOptionComponent sourcePath =
      new VectorFileOptionComponent(OptionConstants.DEBUG_SOURCEPATH, "Sourcepath", this,
                                    "<html>Any directories in which to search for source<br>" +
                                    "files when stepping in the Debugger.</html>");
    // Source path can only include directories
    sourcePath.setFileFilter(new DirectoryFilter("Source Directories"));
    panel.addComponent(sourcePath);
    panel.addComponent(new BooleanOptionComponent(OptionConstants.DEBUG_STEP_JAVA,
                                                  "Step Into Java Classes", this,
                                                  "<html>Whether the Debugger should step into Java library classes,<br>" +
                                                  "including java.*, javax.*, sun.*, com.sun.*, and com.apple.mrj.*</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.DEBUG_STEP_INTERPRETER,
                                                  "Step Into Interpreter Classes", this,
                                                  "<html>Whether the Debugger should step into the classes<br>" +
                                                  "used by the Interactions Pane (DynamicJava).</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.DEBUG_STEP_DRJAVA,
                                                  "Step Into DrJava Classes", this,
                                                  "Whether the Debugger should step into DrJava's own class files."));
    panel.addComponent(new StringOptionComponent(OptionConstants.DEBUG_STEP_EXCLUDE,
                                                 "Classes/Packages To Exclude", this,
                                                 "<html>Any classes that the debuggger should not step into.<br>" +
                                                 "Should be a COMMA-separated list of fully-qualified class names.<br>" +
                                                 "To exclude a package, specify <code>packagename.*</code> in the list.</html>"));

    panel.displayComponents();
  }

  /**
   * Add all of the components for the Javadoc panel of the preferences window.
   */
  private void _setupJavadocPanel(ConfigPanel panel) {
    panel.addComponent
      (new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_ACCESS_LEVEL,
                                       "Access Level", this,
                                       "<html>Fields and methods with access modifiers at this level<br>" +
                                       "or higher will be included in the generated Javadoc.</html>"));
    panel.addComponent
      (new ForcedChoiceOptionComponent(OptionConstants.JAVADOC_LINK_VERSION,
                                       "Java Version for Javadoc Links", this,
                                       "The version of Java for generating links to online Javadoc documentation."));
    panel.addComponent
      (new StringOptionComponent(OptionConstants.JAVADOC_1_3_LINK,
                                 "Javadoc 1.3 URL", this,
                                 "The URL to the Java 1.3 API, for generating links to library classes."));
    panel.addComponent
      (new StringOptionComponent(OptionConstants.JAVADOC_1_4_LINK,
                                 "Javadoc 1.4 URL", this,
                                 "The URL to the Java 1.4 API, for generating links to library classes."));

    panel.addComponent
      (new FileOptionComponent(OptionConstants.JAVADOC_DESTINATION,
                               "Default Destination Directory", this,
                               "Optional default directory for saving Javadoc documentation.",
                               _fileOptionChooser));

    panel.addComponent
      (new StringOptionComponent(OptionConstants.JAVADOC_CUSTOM_PARAMS,
                                 "Custom Javadoc Parameters", this,
                                 "Any extra flags or parameters to pass to Javadoc."));

    panel.addComponent
      (new BooleanOptionComponent(OptionConstants.JAVADOC_FROM_ROOTS,
                                  "Generate Javadoc From Source Roots", this,
                                  "<html>Whether 'Javadoc All' should generate Javadoc for all packages<br>" +
                                  "in an open document's source tree, rather than just the document's<br>" +
                                  "own package and sub-packages.</html>"));

    panel.displayComponents();
  }

  /**
   *  Adds all of the components for the Prompts panel of the preferences window.
   */
  private void _setupNotificationsPanel(ConfigPanel panel) {
    // Quit
    panel.addComponent(new BooleanOptionComponent(OptionConstants.QUIT_PROMPT, "Prompt Before Quit", this,
                                                  "Whether DrJava should prompt the user before quitting."));

    // Interactions
    panel.addComponent(new BooleanOptionComponent(OptionConstants.INTERACTIONS_RESET_PROMPT,
                                                  "Prompt Before Resetting Interactions Pane", this,
                                                  "<html>Whether DrJava should prompt the user before<br>" +
                                                  "manually resetting the interactions pane.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.INTERACTIONS_EXIT_PROMPT,
                                                  "Prompt if Interactions Pane Exits Unexpectedly", this,
                                                  "<html>Whether DrJava should show a dialog box if a program<br>" +
                                                  "in the Interactions Pane exits without the user clicking Reset.</html>"));

    // Javadoc
    panel.addComponent(new BooleanOptionComponent(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
                                                  "Prompt for Javadoc Destination", this,
                                                  "<html>Whether Javadoc should always prompt the user<br>" +
                                                  "to select a destination directory.</html>"));


    // Save before X
    panel.addComponent(new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_COMPILE,
                                                  "Automatically Save Before Compiling", this,
                                                  "<html>Whether DrJava should automatically save before<br>" +
                                                  "recompiling or ask the user each time.</html>"));

    // These are very problematic features, and so are disabled for the forseeable future.
//    panel.addComponent(new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_RUN, "Automatically Save and Compile Before Running Main Method", this,
//                                                    "<html>Whether DrJava should automatically save and compile before running<br>" +
//                                                    "a document's main method, or instead should ask the user each time.</html>"));
//    panel.addComponent(new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_JUNIT, "Automatically Save and Compile Before Testing", this,
//                                                  "<html>Whether DrJava should automatically save and compile before<br>" +
//                                                  "testing with JUnit or ask the user each time.</html>"));
//    panel.addComponent(new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_DEBUG, "Automatically Save and Compile Before Debugging", this,
//                                                  "<html>Whether DrJava should automatically save and compile before<br>" +
//                                                  "debugging or ask the user each time.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.ALWAYS_SAVE_BEFORE_JAVADOC,
                                                  "Automatically Save Before Generating Javadoc", this,
                                                  "<html>Whether DrJava should automatically save before<br>" +
                                                  "generating Javadoc or ask the user each time.</html>"));

    // Warnings
    panel.addComponent(new BooleanOptionComponent(OptionConstants.WARN_BREAKPOINT_OUT_OF_SYNC,
                                                  "Warn on Breakpoint if Out of Sync", this,
                                                  "<html>Whether DrJava should warn the user if the class file<br>" +
                                                  "is out of sync before setting a breakpoint in that file.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.WARN_DEBUG_MODIFIED_FILE,
                                                  "Warn if Debugging Modified File", this,
                                                  "<html>Whether DrJava should warn the user if the file being<br>" +
                                                  "debugged has been modified since its last save.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.WARN_CHANGE_LAF,
                                                  "Warn to Restart to Change Look and Feel", this,
                                                  "<html>Whether DrJava should warn the user that look and feel<br>" +
                                                  "changes will not be applied until DrJava is restarted.</html>."));

    panel.displayComponents();
  }

  /**
   *  Adds all of the components for the Miscellaneous panel of the preferences window.
   */
  private void _setupMiscPanel(ConfigPanel panel) {
    panel.addComponent(new IntegerOptionComponent(OptionConstants.INDENT_LEVEL,
                                                  "Indent Level", this,
                                                  "The number of spaces to use for each level of indentation."));
    // Working directory chooser and component
    JFileChooser dirChooser = new JFileChooser(_getWorkDir());
    dirChooser.setDialogTitle("Select");
    dirChooser.setApproveButtonText("Select");
    dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    dirChooser.setMultiSelectionEnabled(false);
    FileOptionComponent workDir =
      new FileOptionComponent(OptionConstants.WORKING_DIRECTORY,
                              "Working Directory", this,
                              "The directory that DrJava should consider the default working directory.",
                              dirChooser);
    workDir.setFileFilter(new DirectoryFilter());
    panel.addComponent(workDir);

    panel.addComponent(new IntegerOptionComponent(OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History", this,
                                                  "The number of interactions to remember in the history."));
    panel.addComponent(new IntegerOptionComponent(OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size", this,
                                                  "<html>The number of files to remember in<br>" +
                                                  "the recently used files list in the File menu.</html>"));

    panel.addComponent(new BooleanOptionComponent(OptionConstants.AUTO_CLOSE_COMMENTS, "Automatically Close Block Comments", this,
                                                  "<html>Whether to automatically insert a closing comment tag (\"*/\")<br>" +
                                                  "when the enter key is pressed after typing a new block comment<br>" +
                                                  "tag (\"/*\" or \"/**\").</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.JAVAC_ALLOW_ASSERT, "Allow Assert Keyword in Java 1.4", this,
                                                  "<html>Whether to allow the <code>assert</code> keyword when compiling in Java 1.4.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.BACKUP_FILES, "Keep Emacs-style Backup Files", this,
                                                  "<html>Whether DrJava should keep a backup copy of each file that<br>" +
                                                  "the user modifies, saved with a '~' at the end of the filename.</html>"));
    panel.addComponent(new BooleanOptionComponent(OptionConstants.RESET_CLEAR_CONSOLE, "Clear Console After Interactions Reset", this,
                                                  "Whether to clear the Console output after resetting the Interactions Pane."));

    panel.displayComponents();
  }

  /**
   * Private class to handle rendering of tree nodes, each of which
   * corresponds to a ConfigPanel.
   */
  private class PanelTreeNode extends DefaultMutableTreeNode {

    private ConfigPanel _panel;

    public PanelTreeNode(String t) {
      super(t);
      _panel = new ConfigPanel(t);
    }

    public PanelTreeNode(ConfigPanel c) {
      super(c.getTitle());
      _panel = c;
    }
    public ConfigPanel getPanel() {
      return _panel;
    }

    /**
     * Tells its panel to update, and tells all of its child nodes to update their panels.
     * @return whether the update succeeded.
     */
    public boolean update() {
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
