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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.config.*;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.FileChooser;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.filechooser.FileFilter;

/** A frame for setting Project Preferences */
public class ProjectPropertiesFrame extends JFrame {

  private static final int FRAME_WIDTH = 503;
  private static final int FRAME_HEIGHT = 318;

  private MainFrame _mainFrame;      
  private SingleDisplayModel _model; 
  private File _projFile;

  private final JButton _okButton;
  private final JButton _applyButton;
  private final JButton _cancelButton;
  //  private JButton _saveSettingsButton;
  private JPanel _mainPanel;

  private DirectorySelectorComponent _projRootSelector;
  private DirectorySelectorComponent _buildDirSelector;
  private DirectorySelectorComponent _workDirSelector;
  private FileSelectorComponent _mainDocumentSelector;

  private FileSelectorComponent _jarFileSelector;
  private FileSelectorComponent _manifestFileSelector;

  private VectorFileOptionComponent _extraClassPathList;

  /** Constructs project properties frame for a new project and displays it.  Assumes that a project is active. */
  public ProjectPropertiesFrame(MainFrame mf) {
    super("Project Properties");

    //  Utilities.show("ProjectPropertiesFrame(" + mf + ", " + projFile + ")");

    _mainFrame = mf;
    _model = _mainFrame.getModel();
    _projFile = _model.getProjectFile();
    _mainPanel= new JPanel();
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        successful = saveSettings();
        if (successful) ProjectPropertiesFrame.this.setVisible(false);
        reset();
      }
    };
    _okButton = new JButton(okAction);

    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        saveSettings();
        reset();
      }
    };
    _applyButton = new JButton(applyAction);

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) { cancel(); }
    };
    _cancelButton = new JButton(cancelAction);
    
    init();
  }

  /** Initializes the components in this frame. */
  private void init() {
    _setupPanel(_mainPanel);
    JScrollPane scrollPane = new JScrollPane(_mainPanel);
    Container cp = getContentPane();
    
    GridBagLayout cpLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    cp.setLayout(cpLayout);
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = GridBagConstraints.RELATIVE;
    c.weightx = 1.0;
    c.weighty = 1.0;
    cpLayout.setConstraints(scrollPane, c);
    cp.add(scrollPane);
    
    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_applyButton);
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 0.0;
    cpLayout.setConstraints(bottom, c);
    cp.add(bottom);

    // Set all dimensions ----
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    MainFrame.setPopupLoc(this, _mainFrame);

    addWindowListener(new WindowAdapter() { 
      public void windowClosing(java.awt.event.WindowEvent e) { cancel(); } 
    });

    reset();
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    reset();
    _applyButton.setEnabled(false);
    ProjectPropertiesFrame.this.setVisible(false);
  }

  public void reset() { reset(_model.getProjectRoot()); }

  private void reset(File projRoot) {
//  Utilities.show("reset(" + projRoot + ")");
    _projRootSelector.setFileField(projRoot);

    final File bd = _model.getBuildDirectory();
    final JTextField bdTextField = _buildDirSelector.getFileField();
    if (bd == null) bdTextField.setText("");
    else _buildDirSelector.setFileField(bd);

    final File wd = _model.getWorkingDirectory();
    final JTextField wdTextField = _workDirSelector.getFileField();
    if (wd == null) wdTextField.setText("");
    else _workDirSelector.setFileField(wd);

    final File mc = _model.getMainClass();
    final JTextField mcTextField = _mainDocumentSelector.getFileField();
    if (mc == null) mcTextField.setText("");
    else _mainDocumentSelector.setFileField(mc);

    Vector<File> cp = new Vector<File>(IterUtil.asList(_model.getExtraClassPath()));
    _extraClassPathList.setValue(cp);
    _applyButton.setEnabled(false);
  }

  /** Caches the settings in the global model */
  public boolean saveSettings() {//throws IOException {
    boolean projRootChanged = false;

    File pr = _projRootSelector.getFileFromField();
    if (_projRootSelector.getFileField().getText().equals("")) { pr = null; } else {      
      if (!pr.equals(_model.getProjectRoot())) {
        _model.setProjectRoot(pr);
        projRootChanged = true;
      }
    }

    File bd = _buildDirSelector.getFileFromField();
    if (_buildDirSelector.getFileField().getText().equals("")) bd = null;
    _model.setBuildDirectory(bd);

    File wd = _workDirSelector.getFileFromField();
    if (_workDirSelector.getFileField().getText().equals("")) wd = null;
    _model.setWorkingDirectory(wd);

    File mc = _mainDocumentSelector.getFileFromField();
    if (_mainDocumentSelector.getFileField().getText().equals("")) mc = null;
    _model.setMainClass(mc);

    Vector<File> extras = _extraClassPathList.getValue();
    _model.setExtraClassPath(IterUtil.snapshot(extras));

    //    _mainFrame.saveProject();
    if (projRootChanged) {
      try {
        _model.reloadProject(_mainFrame.getCurrentProject(), _mainFrame.gatherProjectDocInfo());
      } catch(IOException e) { throw new edu.rice.cs.util.UnexpectedException(e, "I/O error while reloading project"); }
    }
    return true;
  }

  /** Returns the current project root in the project profile. */
  private File _getProjRoot() {
    File projRoot = _mainFrame.getModel().getProjectRoot();
    if (projRoot != null) return projRoot;
    return FileOption.NULL_FILE;
  }

  /** Returns the current build directory in the project profile. */
  private File _getBuildDir() {
    File buildDir = _mainFrame.getModel().getBuildDirectory();
    if (buildDir != null) return buildDir;
    return FileOption.NULL_FILE;
  }

  /** Returns the current working directory in the project profile (FileOption.NULL_FILE if none is set) */
  private File _getWorkDir() {
    File workDir = _mainFrame.getModel().getWorkingDirectory();
    if (workDir != null) return workDir;
    return FileOption.NULL_FILE;
  }

  /** Returns the current working directory in the project profile (FileOption.NULL_FILE if none is set) */
  private File _getMainFile() {
    File mainFile = _mainFrame.getModel().getMainClass();
    if (mainFile != null) return mainFile;
    return FileOption.NULL_FILE;
  }

  private void _setupPanel(JPanel panel) {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);

    // Project Root

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel prLabel = new JLabel("Project Root");
    prLabel.setToolTipText("<html>The root directory for the project source files .<br>"+
    "If not specified, the parent directory of the project file.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel prPanel = _projRootPanel();
    gridbag.setConstraints(prPanel, c);
    panel.add(prPanel);

    // Build Directory

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel bdLabel = new JLabel("Build Directory");
    bdLabel.setToolTipText("<html>The directory the class files will be compiled into.<br>"+
        "If not specified, the class files will be compiled into<br>"+
    "the same directory as their corresponding source files</html>");
    gridbag.setConstraints(bdLabel, c);

    panel.add(bdLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel bdPanel = _buildDirectoryPanel();
    gridbag.setConstraints(bdPanel, c);
    panel.add(bdPanel);

    // Working Directory

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel wdLabel = new JLabel("Working Directory");
    wdLabel.setToolTipText("<html>The root directory for relative path names.</html>");
    gridbag.setConstraints(wdLabel, c);

    panel.add(wdLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel wdPanel = _workDirectoryPanel();
    gridbag.setConstraints(wdPanel, c);
    panel.add(wdPanel);

    // Main Document file

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel classLabel = new JLabel("Main Document");
    classLabel.setToolTipText("<html>The project document containing the<br>" + 
    "<code>main</code>method for the entire project</html>");
    gridbag.setConstraints(classLabel, c);
    panel.add(classLabel);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel mainClassPanel = _mainDocumentSelector();
    gridbag.setConstraints(mainClassPanel, c);
    panel.add(mainClassPanel);

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    //    ExtraProjectClasspaths
    JLabel extrasLabel = new JLabel("Extra Classpath");
    extrasLabel.setToolTipText("<html>The list of extra classpaths to load with the project.<br>"+  
        "This may include either JAR files or directories. Any<br>"+
        "classes defined in these classpath locations will be <br>"+
        "visible in the interactions pane and also accessible <br>"+
    "by the compiler when compiling the project.</html>");
    gridbag.setConstraints(extrasLabel, c);
    panel.add(extrasLabel);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    Component extrasComponent = _extraClassPathComponent();
    gridbag.setConstraints(extrasComponent, c);
    panel.add(extrasComponent);
  }
  
   private DocumentListener _applyListener = new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { setEnabled(); }
      public void removeUpdate(DocumentEvent e) { setEnabled(); }
      public void changedUpdate(DocumentEvent e) { setEnabled(); }
      private void setEnabled() { Utilities.invokeLater(new Runnable() { public void run() { _applyButton.setEnabled(true); } }); }
   };

  public JPanel _projRootPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    dirChooser.setSelectedFile(_getProjRoot());
    dirChooser.setDialogTitle("Select Project Root Folder");
    dirChooser.setApproveButtonText("Select");
//  dirChooser.setEditable(true);
    _projRootSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f);
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);
    
    _projRootSelector.getFileField().getDocument().addDocumentListener(_applyListener);

    return _projRootSelector;
  }

  public JPanel _buildDirectoryPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    File bd = _getBuildDir();
    if (bd == null || bd == FileOption.NULL_FILE) bd = _getProjRoot();
    dirChooser.setSelectedFile(bd);
    dirChooser.setDialogTitle("Select Build Directory");
    dirChooser.setApproveButtonText("Select");
//  dirChooser.setEditable(true);
    // (..., false); since build directory does not have to exist
    _buildDirSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f, false);
    _buildDirSelector.setFileField(bd);  // the file field is used as the initial file selection
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);

    _buildDirSelector.getFileField().getDocument().addDocumentListener(_applyListener);

    return _buildDirSelector;
  }

  public JPanel _workDirectoryPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    dirChooser.setSelectedFile(_getWorkDir());
    dirChooser.setDialogTitle("Select Working Directory");
    dirChooser.setApproveButtonText("Select");
//  dirChooser.setEditable(true);
    _workDirSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f);
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);

    _workDirSelector.getFileField().getDocument().addDocumentListener(_applyListener);
    return _workDirSelector;
  }

  public Component _extraClassPathComponent() {
    _extraClassPathList = new VectorFileOptionComponent(null, "Extra Project Classpaths", this);
    _extraClassPathList.addChangeListener(new OptionComponent.ChangeListener() {
      public Object apply(Object oc) {
        _applyButton.setEnabled(true);
        return null;
      }
    });
    return _extraClassPathList.getComponent();
  }

  public JPanel _mainDocumentSelector() {
    final File projRoot = _getProjRoot();

    FileChooser chooser = new FileChooser(projRoot);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

    chooser.setDialogTitle("Select Main Document");
//  Utilities.show("Main Document Root is: " + root);
    chooser.setCurrentDirectory(projRoot);
    File mainFile = _getMainFile();
    if (mainFile != FileOption.NULL_FILE) chooser.setSelectedFile(mainFile);

    chooser.setApproveButtonText("Select");

    FileFilter filter = new FileFilter() {
      public boolean accept(File f) {
        String name = f.getName();
        return  IOUtil.isMember(f, projRoot) && (f.isDirectory() ||
            (name.endsWith(".java") || name.endsWith(".dj0") || name.endsWith(".dj1") || name.endsWith(".dj2")));
      }
      public String getDescription() { return "Java & DrJava Files (*.java, *.dj0, *.dj1, *.dj2) in project"; }
    };

    chooser.addChoosableFileFilter(filter);
    _mainDocumentSelector = new FileSelectorComponent(this, chooser, 20, 12f);

    _mainDocumentSelector.getFileField().getDocument().addDocumentListener(_applyListener);
    return _mainDocumentSelector;
  }

  public JPanel _manifestFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_getProjRoot().getParentFile());
    fileChooser.setDialogTitle("Select Output jar File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    _manifestFileSelector = new FileSelectorComponent(this, fileChooser, 20, 12f);

    _manifestFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) { return f.getName().endsWith(".jar") || f.isDirectory(); }
      public String getDescription() { return "Java Archive Files (*.jar)"; }
    });
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);
    return _manifestFileSelector;
  }

  public JPanel _jarFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_getProjRoot());
    fileChooser.setDialogTitle("Select Manifest File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    _jarFileSelector = new FileSelectorComponent(this, fileChooser, 20, 12f);
    _jarFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) { return f.getName().endsWith(".jar") || f.isDirectory(); }
      public String getDescription() { return "Java Archive Files (*.jar)"; }
    });
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);
    return _jarFileSelector;
  }

//public void setVisible(boolean vis) {
//super.setVisible(vis);
//reset();
//}
}
