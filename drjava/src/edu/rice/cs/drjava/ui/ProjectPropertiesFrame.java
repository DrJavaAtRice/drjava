/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.ui;

import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.ui.config.*;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.AbsRelFile;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.FileChooser;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.filechooser.FileFilter;

/** A frame for setting Project Preferences */
public class ProjectPropertiesFrame extends SwingFrame {

  private static final int FRAME_WIDTH = 503;
  private static final int FRAME_HEIGHT = 500;

  private MainFrame _mainFrame;      
  private SingleDisplayModel _model; 

  private final JButton _okButton;
  private final JButton _applyButton;
  private final JButton _cancelButton;
  //  private JButton _saveSettingsButton;
  private JPanel _mainPanel;

  private DirectorySelectorComponent _projRootSelector;
  private DirectorySelectorComponent _buildDirSelector;
  private DirectorySelectorComponent _workDirSelector;
  private JTextField                 _mainDocumentSelector;
  
  private JCheckBox _autoRefreshComponent;

  private VectorAbsRelFileOptionComponent _extraClassPathList;
  private VectorFileOptionComponent _excludedFilesList;
  
  /** Constructs project properties frame for a new project and displays it.  Assumes that a project is active. */
  public ProjectPropertiesFrame(MainFrame mf) {
    super("Project Properties");

    //  Utilities.show("ProjectPropertiesFrame(" + mf + ", " + projFile + ")");

    _mainFrame = mf;
    _model = _mainFrame.getModel();
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
    initDone(); // call mandated by SwingFrame contract
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
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if (dim.width>FRAME_WIDTH) { dim.width = FRAME_WIDTH; }
    else { dim.width -= 80; }
    if (dim.height>FRAME_HEIGHT) { dim.height = FRAME_HEIGHT; }
    else { dim.height -= 80; }
    setSize(dim);
    Utilities.setPopupLoc(this, _mainFrame);

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
    if (bd == FileOps.NULL_FILE) bdTextField.setText("");
    else _buildDirSelector.setFileField(bd);

    final File wd = _model.getWorkingDirectory();
    final JTextField wdTextField = _workDirSelector.getFileField();
    if (wd == FileOps.NULL_FILE) wdTextField.setText("");
    else _workDirSelector.setFileField(wd);

    final String mc = _model.getMainClass();
    final JTextField mcTextField = _mainDocumentSelector;
    if (mc == null) mcTextField.setText("");
    else mcTextField.setText(mc);
    
    _autoRefreshComponent.setSelected(_getAutoRefreshStatus());

    ArrayList<AbsRelFile> cp = new ArrayList<AbsRelFile>(CollectUtil.makeList(_model.getExtraClassPath()));
    _extraClassPathList.setValue(cp);

    ArrayList<File> ef = new ArrayList<File>();
    for(File f: _model.getExclFiles()) { ef.add(f); }
    _excludedFilesList.setValue(ef);
    _applyButton.setEnabled(false);
  }

  /** Caches the settings in the global model */
  public boolean saveSettings() {//throws IOException {
    boolean projRootChanged = false;

    File pr = _projRootSelector.getFileFromField();
//    if (_projRootSelector.getFileField().getText().equals("")) { pr = FileOps.NULL_FILE; } else {      
    if (!pr.equals(_model.getProjectRoot())) {
      _model.setProjectRoot(pr);
      projRootChanged = true;
    }
//    }

    File bd = _buildDirSelector.getFileFromField();
    if (_buildDirSelector.getFileField().getText().equals("")) bd = FileOps.NULL_FILE;
    _model.setBuildDirectory(bd);

    File wd = _workDirSelector.getFileFromField();
    if (_workDirSelector.getFileField().getText().equals("")) wd = FileOps.NULL_FILE;
    _model.setWorkingDirectory(wd);

    String mc = _mainDocumentSelector.getText();
    if(mc == null) mc = "";
    _model.setMainClass(mc);

    Vector<AbsRelFile> extras = _extraClassPathList.getValue();  // Vector mandated by interface to VectorFileOptionComponent
    _model.setExtraClassPath(IterUtil.snapshot(extras));

    _model.setAutoRefreshStatus(_autoRefreshComponent.isSelected());

    _model.setExcludedFiles(_excludedFilesList.getValue().toArray(new File[0]));
    
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
    File projRoot = _model.getProjectRoot();
    if (projRoot != null) return projRoot;
    return FileOps.NULL_FILE;
  }

  /** Returns the current build directory in the project profile. */
  private File _getBuildDir() {
    File buildDir = _model.getBuildDirectory();
    if (buildDir != null) return buildDir;
    return FileOps.NULL_FILE;
  }

  /** Returns the current working directory in the project profile (FileOption.NULL_FILE if none is set) */
  private File _getWorkDir() {
    File workDir = _model.getWorkingDirectory();
    if (workDir != null) return workDir;
    return FileOps.NULL_FILE;
  }

  /** Returns the file contianing the main class in the project profile (FileOption.NULL_FILE if none is set) */
  private File _getMainFile() {
    File mainFile = _model.getMainClassContainingFile();
    if (mainFile != null) return mainFile;
    return FileOps.NULL_FILE;
  }
  
  /** Returns the fully-qualified name of the main class in the project profile ("" if none is set) */
  private String _getMainClass(){
    String mainClass = _model.getMainClass();
    if(mainClass == null) return "";
    
    return mainClass;
  }
  
  /** Returns whether the project is set to automatically open new source files */
  private boolean _getAutoRefreshStatus() {
    return _model.getAutoRefreshStatus();
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
    prLabel.setToolTipText("<html>The root directory for the project source files .<br>" + 
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
    bdLabel.setToolTipText("<html>The directory the class files will be compiled into.<br>" + 
        "If not specified, the class files will be compiled into<br>" + 
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

    JLabel classLabel = new JLabel("Main Class");
    classLabel.setToolTipText("<html>The class containing the <code>main</code><br>" + 
                              "method for the entire project</html>");
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
    extrasLabel.setToolTipText("<html>The list of extra classpaths to load with the project.<br>" +   
                               "This may include either JAR files or directories. Any<br>" + 
                               "classes defined in these classpath locations will be <br>" + 
                               "visible in the interactions pane and also accessible <br>" + 
                               "by the compiler when compiling the project.<br>" + 
                               "The entries are relative to the project file unless<br>" + 
                               "the 'Absolute' checkbox is marked.</html>");
    gridbag.setConstraints(extrasLabel, c);
    panel.add(extrasLabel);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    Component extrasComponent = _extraClassPathComponent();
    gridbag.setConstraints(extrasComponent, c);
    panel.add(extrasComponent);
    
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel refreshLabel = new JLabel("<html>Auto Refresh<br>on Open</html>");
    refreshLabel.setToolTipText("<html>Whether the project will automatically open new files found within the source tree</html>");
    gridbag.setConstraints(refreshLabel, c);
    panel.add(refreshLabel);

    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _autoRefreshComponent = new JCheckBox();
    gridbag.setConstraints(_autoRefreshComponent, c);
    panel.add(_autoRefreshComponent);    

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    
    //    Files excluded from auto-refresh
    JLabel excludedLabel = new JLabel("<html>Files Excluded from<br>Auto-Refresh</html>");
    excludedLabel.setToolTipText("<html>The list of source files excluded from project auto-refresh.<br>" + 
                                 "These files will not be added to the project.</html>");
    gridbag.setConstraints(excludedLabel, c);
    panel.add(excludedLabel);
    
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    Component excludedComponent = _excludedFilesComponent();
    gridbag.setConstraints(excludedComponent, c);
    panel.add(excludedComponent);
  }
  
   private DocumentListener _applyListener = new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { setEnabled(); }
      public void removeUpdate(DocumentEvent e) { setEnabled(); }
      public void changedUpdate(DocumentEvent e) { setEnabled(); }
      private void setEnabled() { 
        assert EventQueue.isDispatchThread();
//        Utilities.invokeLater(new Runnable() { 
//          public void run() { 
            _applyButton.setEnabled(true); 
//          } 
//        }); 
      }
   };

  public JPanel _projRootPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    dirChooser.setSelectedFile(_getProjRoot());
    dirChooser.setDialogTitle("Select Project Root Folder");
    dirChooser.setApproveButtonText("Select");
//  dirChooser.setEditable(true);
    _projRootSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f) {
      protected void _chooseFile() {
        _mainFrame.removeModalWindowAdapter(ProjectPropertiesFrame.this);
        super._chooseFile();
        _mainFrame.installModalWindowAdapter(ProjectPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
      }
    };
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);
    
    _projRootSelector.getFileField().getDocument().addDocumentListener(_applyListener);

    return _projRootSelector;
  }

  public JPanel _buildDirectoryPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    File bd = _getBuildDir();
    if (bd == null || bd == FileOps.NULL_FILE) bd = _getProjRoot();
    dirChooser.setSelectedFile(bd);
    dirChooser.setDialogTitle("Select Build Directory");
    dirChooser.setApproveButtonText("Select");
//  dirChooser.setEditable(true);
    // (..., false); since build directory does not have to exist
    _buildDirSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f, false) {
      protected void _chooseFile() {
        _mainFrame.removeModalWindowAdapter(ProjectPropertiesFrame.this);
        super._chooseFile();
        _mainFrame.installModalWindowAdapter(ProjectPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
      }
    };
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
    _workDirSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f) {
      protected void _chooseFile() {
        _mainFrame.removeModalWindowAdapter(ProjectPropertiesFrame.this);
        super._chooseFile();
        _mainFrame.installModalWindowAdapter(ProjectPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
      }
    };
    //toReturn.add(_buildDirSelector, BorderLayout.EAST);

    _workDirSelector.getFileField().getDocument().addDocumentListener(_applyListener);
    return _workDirSelector;
  }

  public Component _extraClassPathComponent() {
    _extraClassPathList = new VectorAbsRelFileOptionComponent(null, "Extra Project Classpaths", this, null, true) {
      protected Action _getAddAction() {
        final Action a = super._getAddAction();
        return new AbstractAction("Add") {
          public void actionPerformed(ActionEvent ae) {
            _mainFrame.removeModalWindowAdapter(ProjectPropertiesFrame.this);
            a.actionPerformed(ae);
            _mainFrame.installModalWindowAdapter(ProjectPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
          }
        };
      }
    };
    _extraClassPathList.setRows(5,5);
    _extraClassPathList.addChangeListener(new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        _applyButton.setEnabled(true);
        return null;
      }
    });
    return _extraClassPathList.getComponent();
  }

  public Component _excludedFilesComponent() {
    _excludedFilesList = new VectorFileOptionComponent(null, "Files Excluded from Auto-Refresh", this, null, false) {
      protected Action _getAddAction() {
        final Action a = super._getAddAction();
        return new AbstractAction("Add") {
          public void actionPerformed(ActionEvent ae) {
            _mainFrame.removeModalWindowAdapter(ProjectPropertiesFrame.this);
            a.actionPerformed(ae);
            _mainFrame.installModalWindowAdapter(ProjectPropertiesFrame.this, LambdaUtil.NO_OP, CANCEL);
          }
        };
      }
    };
    _excludedFilesList.setRows(5,5);
    _excludedFilesList.getFileChooser().resetChoosableFileFilters();
    _excludedFilesList.getFileChooser().addChoosableFileFilter(new JavaSourceFilter());
    _excludedFilesList.getFileChooser().setFileFilter(new SmartSourceFilter());
    _excludedFilesList.addChangeListener(new OptionComponent.ChangeListener() {
      public Object value(Object oc) {
        _applyButton.setEnabled(true);
        return null;
      }
    });
    if (_model.getProjectRoot() != null) {
      _excludedFilesList.setBaseDir(_model.getProjectRoot());
    }
    return _excludedFilesList.getComponent();
  }

  public JPanel _mainDocumentSelector() {
    final File projRoot = _getProjRoot();

    final FileChooser chooser = new FileChooser(projRoot);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);

    chooser.setDialogTitle("Select Main Class");
    chooser.setCurrentDirectory(projRoot);
    File   mainFile  = _getMainFile();
    if (mainFile != FileOps.NULL_FILE){
      chooser.setSelectedFile(mainFile);
    }

    chooser.setApproveButtonText("Select");

    chooser.resetChoosableFileFilters();
    chooser.addChoosableFileFilter(new SmartSourceFilter());
    chooser.addChoosableFileFilter(new JavaSourceFilter());
    _mainDocumentSelector = new JTextField(20){
      public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
      }
    };

    _mainDocumentSelector.setFont(_mainDocumentSelector.getFont().deriveFont(12f));
    _mainDocumentSelector.setPreferredSize(new Dimension(22, 22));
    
    _mainDocumentSelector.getDocument().addDocumentListener(_applyListener);
    
    JButton selectFile = new JButton("...");
    selectFile.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        int ret = chooser.showOpenDialog(ProjectPropertiesFrame.this);
        
        if(ret != JFileChooser.APPROVE_OPTION)
          return;
        
        File mainClass = chooser.getSelectedFile();
        
        File sourceRoot = new File(_projRootSelector.getFileField().getText());
        
        if(sourceRoot == null || mainClass == null)
          return;
        
        if(!mainClass.getAbsolutePath().startsWith(sourceRoot.getAbsolutePath())){
          JOptionPane.showMessageDialog(ProjectPropertiesFrame.this,
                                        "Main Class must be in either Project Root or one of its sub-directories.", 
                                        "Unable to set Main Class", JOptionPane.ERROR_MESSAGE);
          
          _mainDocumentSelector.setText("");
          return;
        }
        
        //Strip off the source root path
        String qualifiedName = mainClass.getAbsolutePath().substring(sourceRoot.getAbsolutePath().length());
        
        //Strip off any leading slashes
        if(qualifiedName.startsWith("" + File.separatorChar))
          qualifiedName = qualifiedName.substring(1);
        
        //Remove the .java extension if it exists
        if(qualifiedName.toLowerCase().endsWith(OptionConstants.JAVA_FILE_EXTENSION))
          qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 5);
          
        //Replace path seperators with java standard '.' package seperators.
        _mainDocumentSelector.setText(qualifiedName.replace(File.separatorChar, '.'));
      }
    });
    
    
    selectFile.setMaximumSize(new Dimension(22, 22));
    selectFile.setMargin(new Insets(0, 5 ,0, 5));
    
    JPanel toRet = new JPanel();
    javax.swing.BoxLayout layout = new javax.swing.BoxLayout(toRet, javax.swing.BoxLayout.X_AXIS);
    toRet.setLayout(layout);
    toRet.add(_mainDocumentSelector);
    toRet.add(selectFile);
    
    return toRet;
  }

  /** Runnable that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { cancel(); }
  };
  
  /** Validates before changing visibility.  Only runs in the event thread.
    * @param vis true if frame should be shown, false if it should be hidden.
    */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
      toFront();
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
}
