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
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.io.IOException;
import java.io.File;
import java.util.TreeSet;
import java.util.Iterator;

import javax.swing.tree.*;
import gj.util.Hashtable;

import edu.rice.cs.drjava.DrJava;
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
  
  /**
   * Sets up the frame and displays it.
   */
  public ConfigFrame (MainFrame frame) {
    super("Preferences");
    
    _mainFrame = frame;
    
    _createTree();
    _createPanels();
    
    _mainPanel= new JPanel();
    _mainPanel.setLayout(new BorderLayout());
    _tree.addTreeSelectionListener( new PanelTreeSelectionListener());
        
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    
    // Select the first panel by default
    if (_rootNode.getChildCount() != 0) {
      PanelTreeNode firstChild = (PanelTreeNode)_rootNode.getChildAt(0);
      TreePath path = new TreePath(firstChild.getPath());
      _tree.expandPath(path);
      _tree.setSelectionPath(path);
    }
    
    JScrollPane treeScroll = new JScrollPane(_tree);    
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                treeScroll,
                                _mainPanel);
    cp.add(_splitPane, BorderLayout.CENTER);
    
    _okButton = new JButton("OK");
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = saveSettings();
        if (successful) ConfigFrame.this.hide();
      }
    });
    
    _applyButton = new JButton("Apply");
    _applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        saveSettings();
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
    setSize( FRAME_WIDTH, FRAME_HEIGHT);
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
    int width = getWidth() / 4;
    //System.out.println("width: " + getWidth());
    //System.out.println("width for divider: " + width);
    _splitPane.setDividerLocation(width);
    _mainPanel.setPreferredSize(new Dimension(getWidth() - width,
                                              _splitPane.getHeight()));
    addWindowListener(new WindowAdapter() { 
      public void windowClosing(java.awt.event.WindowEvent e) { 
        cancel();
      }
    });
    
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
  public boolean saveSettings() {
    boolean successful = apply();
    if (successful) {
      try {
        DrJava.CONFIG.saveConfiguration();
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,
                                      "Could not save changes to your '.drjava' file \n" +
                                      "in your home directory.\n\n" + ioe,
                                      "Could Not Save Changes",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
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
    parent.add(ptNode);
    
    return ptNode;
  }
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
  private PanelTreeNode _createPanel(ConfigPanel c, PanelTreeNode parent) {
    PanelTreeNode ptNode = new PanelTreeNode(c);
    parent.add(ptNode);
    
    return ptNode;
  }
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

    PanelTreeNode keystrokesNode = _createPanel(new KeyStrokeConfigPanel("Key Bindings"),
                                                _rootNode);
    _setupKeyBindingsPanel(keystrokesNode.getPanel());
    
    PanelTreeNode debugNode = _createPanel("Debugger");
    _setupDebugPanel(debugNode.getPanel());

    PanelTreeNode miscNode = _createPanel("Miscellaneous");
    _setupMiscPanel(miscNode.getPanel());
    
  }
  
  /**
   * Add all of the components for the Resource Locations panel of the preferences window.
   */ 
  private void _setupResourceLocPanel ( ConfigPanel panel) {
   
    panel.addComponent( new FileOptionComponent( OptionConstants.JAVAC_LOCATION, "Javac Location", this));
    panel.addComponent( new FileOptionComponent( OptionConstants.JSR14_LOCATION, "JSR14 Location", this));
    panel.addComponent( new FileOptionComponent( OptionConstants.JSR14_COLLECTIONSPATH, "JSR14 Collections Path", this));
    panel.addComponent( new VectorOptionComponent (OptionConstants.EXTRA_CLASSPATH, "Interactions Classpath", this));     
    panel.displayComponents();
  }
  
  /**
   * Add all of the components for the Display Options panel of the preferences window.
   */ 
  private void _setupDisplayPanel ( ConfigPanel panel) {

    //ToolbarOptionComponent is a degenerate option component
    panel.addComponent( new ToolbarOptionComponent ( "Toolbar Buttons", this));
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.LINEENUM_ENABLED, "Line Number Enumeration", this));
    panel.displayComponents();
  }
   
  /**
   * Add all of the components for the Font panel of the preferences window.
   */ 
  private void _setupFontPanel ( ConfigPanel panel) {
    panel.addComponent( new FontOptionComponent (OptionConstants.FONT_MAIN, "Main Font", this) );
    panel.addComponent( new FontOptionComponent (OptionConstants.FONT_DOCLIST, "Document List Font", this));
    panel.addComponent( new FontOptionComponent (OptionConstants.FONT_TOOLBAR, "Toolbar Font", this));
    panel.displayComponents(); 
  }
  
  /**
   * Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel( ConfigPanel panel) {
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color", this));
    panel.displayComponents();
  }
  
  /**
   * Adds all of the components for the Key Bindings panel of the preferences window.
   */
  private void _setupKeyBindingsPanel( ConfigPanel panel) {
    // using a treeset because it automatically sorts element upon insertion
    TreeSet _comps = new TreeSet();

    
    KeyStrokeData tmpKsd;
    KeyStrokeOptionComponent tmpKsoc;
    
    Enumeration e = KeyBindingManager.Singleton.getKeyStrokeData();
    while (e.hasMoreElements()) {
      tmpKsd = (KeyStrokeData) e.nextElement();
      if (tmpKsd.getOption() != null) {
        tmpKsoc = new KeyStrokeOptionComponent((KeyStrokeOption)tmpKsd.getOption(),
                                               tmpKsd.getName(), this);
        if (tmpKsoc != null) { 
          _comps.add(tmpKsoc);
        }
      }
    }
    // gives the KeyStrokeConfigPanel a collection of the KeyStrokeOptionComponents
    ((KeyStrokeConfigPanel)panel).setKeyStrokeComponents(_comps);

    
    Iterator iter = _comps.iterator();
    while (iter.hasNext()) {
      KeyStrokeOptionComponent x = (KeyStrokeOptionComponent) iter.next();
      panel.addComponent(x);
    }
    panel.displayComponents();
  }
  
  /**
   * Add all of the components for the Debugger panel of the preferences window.
   */ 
  private void _setupDebugPanel ( ConfigPanel panel) {
    if (_mainFrame.getModel().getDebugManager() == null) {
      // Explain how to use debugger
      String howto = 
        "\nThe debugger is not currently active.  To use the debugger, you must\n" +
        "include Sun's tools.jar or jpda.jar on your classpath when starting DrJava.\n" +
        "Do not use the \"-jar\" option, because it overrides the classpath.\n" +
        "For example, in Windows you might type:\n\n" +
        "  java -classpath drjava.jar;c:\\path\\tools.jar edu.rice.cs.drjava.DrJava\n\n" +
        "(Substituting the correct path for tools.jar.)\n" +
        "See the user documentation for more details.\n";
      panel.addComponent( new LabelComponent(howto, this) );
    }
    
    VectorOptionComponent sourcePath = new VectorOptionComponent (OptionConstants.DEBUG_SOURCEPATH, 
                                                                  "Sourcepath", 
                                                                  this);
    // Source path can only include directories
    sourcePath.setFileFilter(new FileFilter() {
      public boolean accept (File f) {
        if (f.isDirectory()) {
          return true;
        }
        return false;
      }

      /**
       * @return A description of this filter to display
       */
      public String getDescription() {
        return "Source Directories";
      }
    });
    panel.addComponent( sourcePath );
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.DEBUG_STEP_JAVA, 
                                                    "Step Into Java Classes", 
                                                    this));
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.DEBUG_STEP_INTERPRETER, 
                                                    "Step Into Interpreter Classes", 
                                                    this));
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.DEBUG_STEP_DRJAVA, 
                                                    "Step Into DrJava Classes", 
                                                    this));
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.DEBUG_SHOW_THREADS,
                                                    "Show Threads Tab",
                                                    this));
    panel.displayComponents();
  }
  
  /**
   *  Adds all of the components for the Miscellaneous panel of the preferences window.
   */
  private void _setupMiscPanel( ConfigPanel panel) {
    panel.addComponent( new IntegerOptionComponent ( OptionConstants.INDENT_LEVEL, "Indent Level", this));
    panel.addComponent( new FileOptionComponent ( OptionConstants.WORKING_DIRECTORY, "Working Directory", this));
    panel.addComponent( new IntegerOptionComponent ( OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions History", this));
    panel.addComponent( new IntegerOptionComponent ( OptionConstants.RECENT_FILES_MAX_SIZE, "Recent Files List Size", this));

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
        TreePath path = new TreePath(this.getPath());
        _tree.expandPath(path);
        _tree.setSelectionPath(path);
        return false;
      }
      
      Enumeration childNodes = this.children();
      while (childNodes.hasMoreElements()) {
        boolean isValidUpdateChildren = ((PanelTreeNode)childNodes.nextElement()).update();
        //if any of the children nodes encountered an error, return false
        if (!isValidUpdateChildren) return false;
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

