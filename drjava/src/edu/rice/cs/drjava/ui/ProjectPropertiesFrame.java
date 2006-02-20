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
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.config.*;

import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import javax.swing.filechooser.FileFilter;

/**
 * The frame for setting Project Preferences
 */
public class ProjectPropertiesFrame extends JFrame {
  
  private static final int FRAME_WIDTH = 500;
  private static final int FRAME_HEIGHT = 300;
  private JButton _okButton;
  private JButton _applyButton;
  private JButton _cancelButton;
  //  private JButton _saveSettingsButton;
  private JPanel _mainPanel;
  
  private MainFrame _mainFrame;
  
  private DirectorySelectorComponent _builtDirSelector;
  private DirectorySelectorComponent _workDirSelector;
  private DirectorySelectorComponent _jarMainClassSelector;
  
  private FileSelectorComponent _jarFileSelector;
  private FileSelectorComponent _manifestFileSelector;
  
  private VectorFileOptionComponent _extraClassPathList;
  
  /** Sets up the frame and displays it. */
  public ProjectPropertiesFrame(MainFrame mf) {
    super("Project Properties");
    
    _mainFrame = mf;
    _mainPanel= new JPanel();
    _setupPanel(_mainPanel);
    
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    
    cp.add(_mainPanel, BorderLayout.NORTH);
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        successful = saveSettings();
        if (successful)  ProjectPropertiesFrame.this.setVisible(false);
        _applyButton.setEnabled(false);
      }
    };
    _okButton = new JButton(okAction);
    
    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        saveSettings();
        _applyButton.setEnabled(false);
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
    
    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
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
    
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        cancel();
      }
    });
    
    reset();
  }
  
  /** Resets the frame and hides it. */
  public void cancel() {
    reset();
    _applyButton.setEnabled(false);
    ProjectPropertiesFrame.this.setVisible(false);
  }
  
  private void reset() {
    final File bd = _mainFrame.getModel().getBuildDirectory();
    final JTextField bdTextField = _builtDirSelector.getFileField();
    if (bd == null) bdTextField.setText("");
    else _builtDirSelector.setFileField(bd);
    
    final File wd = _mainFrame.getModel().getRawWorkingDirectory();
    final JTextField wdTextField = _workDirSelector.getFileField();
    if (wd == null) wdTextField.setText("");
    else _workDirSelector.setFileField(wd);
    
    final File mc = _mainFrame.getModel().getMainClass();
    final JTextField mcTextField = _jarMainClassSelector.getFileField();
    if (mc == null) mcTextField.setText("");
    else _jarMainClassSelector.setFileField(mc);
    
    ClassPathVector cp = _mainFrame.getModel().getExtraClassPath();
    _extraClassPathList.setValue(cp.asFileVector());
  }
  
  /** Write the settings to the project file */
  public boolean saveSettings() {//throws IOException {
    File bd = _builtDirSelector.getFileFromField();
    if (_builtDirSelector.getFileField().getText().equals("")) bd = null;
    _mainFrame.getModel().setBuildDirectory(bd);
    
    File wd = _workDirSelector.getFileFromField();
    if (_workDirSelector.getFileField().getText().equals("")) wd = null;
    _mainFrame.getModel().setWorkingDirectory(wd);
    
    File mc = _jarMainClassSelector.getFileFromField();
    if (_jarMainClassSelector.getFileField().getText().equals("")) mc = null;
    _mainFrame.getModel().setMainClass(mc);
    
    Vector<File> extras = _extraClassPathList.getValue();
    ClassPathVector cpv = new ClassPathVector();
    for (File cf : extras) { cpv.add(cf); }
    _mainFrame.getModel().setExtraClassPath(cpv);
 
    //    _mainFrame.saveProject();
    return true;
  }
  
  /** Returns the current build directory in the project profile. */
  private File _getBuildDir() {
    File buildDir = _mainFrame.getModel().getBuildDirectory();
    if (buildDir != null) return buildDir;
    return FileOption.NULL_FILE;
  }
  
  /** Returns the current working directory in the project profile (FileOption.NULL_FILE if none is set) */
  private File _getWorkDir() {
    File workDir = _mainFrame.getModel().getRawWorkingDirectory();
    if (workDir != null) return workDir;
    return FileOption.NULL_FILE;
  }
  
  private void _setupPanel(JPanel panel) {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);
    
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
    
    JPanel bdPanel = _builtDirectoryPanel();
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
    
    JPanel mainClassPanel = _jarMainClassSelector();
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
    
    
    //    // Jar output file
    //    c.weightx = 0.0;
    //    c.gridwidth = 1;
    //    c.insets = labelInsets;
    //
    //    JLabel jarLabel = new JLabel("Jar File");
    //    classLabel.setToolTipText("The file that the jar is to be written to");
    //    gridbag.setConstraints(jarLabel, c);
    //    panel.add(jarLabel);
    //
    //    c.weightx = 1.0;
    //    c.gridwidth = GridBagConstraints.REMAINDER;
    //    c.insets = compInsets;
    //
    //    JPanel jarFilePanel = _jarFileSelector();
    //    gridbag.setConstraints(jarFilePanel, c);
    //    panel.add(jarFilePanel);
    //
    //    // Jar manifest file
    //    c.weightx = 0.0;
    //    c.gridwidth = 1;
    //    c.insets = labelInsets;
    //
    //    JLabel manifestLabel = new JLabel("Jar Manifest File");
    //    classLabel.setToolTipText("The manifest file that the jar is to be used to create the jar file");
    //    gridbag.setConstraints(manifestLabel, c);
    //    panel.add(manifestLabel);
    //
    //    c.weightx = 1.0;
    //    c.gridwidth = GridBagConstraints.REMAINDER;
    //    c.insets = compInsets;
    //
    //    JPanel manifestFilePanel = _manifestFileSelector();
    //    gridbag.setConstraints(manifestFilePanel, c);
    //    panel.add(manifestFilePanel);
  }
  
  public JPanel _builtDirectoryPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    dirChooser.setSelectedDirectory(_getBuildDir());
    dirChooser.setDialogTitle("Select Build Directory");
    dirChooser.setApproveButtonText("Select");
    dirChooser.setEditable(true);
    _builtDirSelector = new DirectorySelectorComponent(this,dirChooser, 20, 12f);
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    _builtDirSelector.getFileField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void removeUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void changedUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
    });

    return _builtDirSelector;
  }
  
  public JPanel _workDirectoryPanel() {
    DirectoryChooser dirChooser = new DirectoryChooser(this);
    dirChooser.setSelectedDirectory(_getWorkDir());
    dirChooser.setDialogTitle("Select Working Directory");
    dirChooser.setApproveButtonText("Select");
    dirChooser.setEditable(true);
    _workDirSelector = new DirectorySelectorComponent(this, dirChooser, 20, 12f);
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    _workDirSelector.getFileField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void removeUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void changedUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
    });
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
  
  public JPanel _jarMainClassSelector() {
    File rootFile = _mainFrame.getModel().getProjectFile();
    try {
      rootFile = rootFile.getCanonicalFile();
    } catch(IOException e) { }
    
    DirectoryChooser chooser = new DirectoryChooser(this,rootFile);
    chooser.setDialogTitle("Select Main Document");
    chooser.setTopMessage("Select the main document for the project:");
    chooser.setApproveButtonText("Select");
    FileFilter filter = new FileFilter() {
      public boolean accept(File f) {
        String name = f.getName();
        return  !f.isDirectory() &&
          (name.endsWith(".java") ||
           name.endsWith(".dj0") ||
           name.endsWith(".dj1") ||
           name.endsWith(".dj2"));
      }
      public String getDescription() {
        return "Java & DrJava Files (*.java, *.dj0, *.dj1, *.dj2)";
      }
    };
    chooser.addChoosableFileFilter(filter);
    chooser.addFileFilter(filter);
    chooser.setShowFiles(true);
    chooser.setFileDisplayManager(MainFrame.getFileDisplayManager20());
    _jarMainClassSelector = new DirectorySelectorComponent(this,chooser,20,12f);
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    _jarMainClassSelector.getFileField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void removeUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
      public void changedUpdate(DocumentEvent e) {
        _applyButton.setEnabled(true);
      }
    });
    return _jarMainClassSelector;
  }
  
  public JPanel _manifestFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_mainFrame.getModel().getProjectFile().getParentFile());
    fileChooser.setDialogTitle("Select Output jar File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    _manifestFileSelector = new FileSelectorComponent(this,fileChooser,20,12f);
    _manifestFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".jar") || f.isDirectory();
      }
      public String getDescription() {
        return "Java Archive Files (*.jar)";
      }
      
    });
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    return _manifestFileSelector;
  }
  
  public JPanel _jarFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_mainFrame.getModel().getProjectFile().getParentFile());
    fileChooser.setDialogTitle("Select Manifest File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    _jarFileSelector = new FileSelectorComponent(this,fileChooser,20,12f);
    _jarFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) { return f.getName().endsWith(".jar") || f.isDirectory(); }
      public String getDescription() { return "Java Archive Files (*.jar)"; }
      
    });
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    return _jarFileSelector;
  }
  
  public void setVisible(boolean vis) {
    super.setVisible(vis);
    reset();
  }
}
