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

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.jar.JarBuilder;
import edu.rice.cs.util.jar.ManifestWriter;
import edu.rice.cs.util.swing.FileChooser;
import edu.rice.cs.util.swing.FileSelectorStringComponent;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.SwingWorker;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.ProcessingDialog;
import edu.rice.cs.util.swing.ScrollableListDialog;
import edu.rice.cs.util.StreamRedirectThread;
import edu.rice.cs.util.FileOps;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.jar.Manifest;

public class JarOptionsDialog extends SwingFrame {
  /** Class to save the frame state, i.e. location. */
  public static class FrameState {
    private Point _loc;
    public FrameState(Point l) { _loc = l; }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
      }
      catch(NoSuchElementException nsee) { throw new IllegalArgumentException("Wrong FrameState string: " + nsee); }
      catch(NumberFormatException nfe) { throw new IllegalArgumentException("Wrong FrameState string: " + nfe); }
    }
    public FrameState(JarOptionsDialog comp) { _loc = comp.getLocation(); }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      return sb.toString();
    }
    public Point getLocation() { return _loc; }
  }
  
  static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("JarOptionsDialog.txt", false);
  
  /** Bitflags for default selection. */
  public static final int JAR_CLASSES = 1;
  public static final int JAR_SOURCES = 2;
  public static final int MAKE_EXECUTABLE = 4;
  public static final int JAR_ALL = 8;
  public static final int CUSTOM_MANIFEST = 16;
  
  /** Determines whether class files should be jar-ed. */
  private JCheckBox _jarClasses; 
  /** Determines whether source files should be jar-ed. */
  private JCheckBox _jarSources;
  /** Determines whether all files should be jar-ed. */
  private JCheckBox _jarAll;
  /** Determines whether the jar file should be made executable. */
  private JCheckBox _makeExecutable;
  /** Determines whether the jar file should include a custom manifest. */
  private JCheckBox _customManifest;
  /** File selector for the jar output file. */
  private FileSelectorComponent _jarFileSelector;
  /** Text field for the main class. */
  private FileSelectorStringComponent _mainClassField;
  /** Label for main class. */
  private JLabel _mainClassLabel;
  /** Button for opening edit window in custom manifest. */
  private JButton _editManifest;
  /** OK button. */
  private JButton _okButton;
  /** Cancel button. */
  private JButton _cancelButton;
  /** Main frame. */
  private MainFrame _mainFrame;
  /** Model. */
  private GlobalModel _model;
  /** Label explaining why classes can't be jar-ed. */
  private JLabel _cantJarClassesLabel;
  /** Root of the chooser. */
  private File _rootFile;
  /** Processing dialog. */
  private ProcessingDialog _processingDialog;  
  /** Last frame state. It can be stored and restored. */
  private FrameState _lastState = null;
  /** Holds the current text of the custom manifest. */
  private String _customManifestText = "";
  
  /** Returns the last state of the frame, i.e. the location and dimension.
    * @return frame state
    */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
    * @param ds State to update to, or {@code null} to reset
    */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
      setLocation(_lastState.getLocation());
      validate();
    }
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
    * @param s  State to update to, or {@code null} to reset
    */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
    if (_lastState != null) setLocation(_lastState.getLocation());
    else Utilities.setPopupLoc(this, _mainFrame);
    validate();
  }  
  
  /** Create a "Create Jar" dialog
    * @param mf the instance of mainframe to query into the project
    */
  public JarOptionsDialog(MainFrame mf) {
    super("Create Jar File from Project");
    _mainFrame = mf;
    _model = mf.getModel();
    initComponents();
    
    initDone();  // call by mandated by SwingFrame contract
    pack();
    
    Utilities.setPopupLoc(this, _mainFrame);   
  }
  
  /** Load the initial state from the previous files or with defaults. */
  private void _loadSettings() {
    int f = _model.getCreateJarFlags();
    _jarClasses.setSelected(((f & JAR_CLASSES) != 0));
    _jarSources.setSelected(((f & JAR_SOURCES) != 0));
    _jarAll.setSelected(((f & JAR_ALL) != 0));
    _makeExecutable.setSelected(((f & MAKE_EXECUTABLE) != 0));
    _customManifest.setSelected(((f & CUSTOM_MANIFEST) != 0));
    
    LOG.log("_customManifestText set off of " + _model);
    _customManifestText = _model.getCustomManifest();
    LOG.log("\tto: " + _customManifestText);
    if(_customManifestText == null)
      _customManifestText = "";
    
    boolean outOfSync = true;
    if (_model.getBuildDirectory() != null) {
      outOfSync = _model.hasOutOfSyncDocuments();
    }
    if ((_model.getBuildDirectory() == null) || (outOfSync)) {
      _jarClasses.setSelected(false);
      _jarClasses.setEnabled(false);
      String s;
      if ((_model.getBuildDirectory() == null) && (outOfSync)) {
        s = "<html><center>A build directory must be specified in order to jar class files,<br>and the project needs to be compiled.</center></html>";
      }
      else
        if (_model.getBuildDirectory() == null) {
        s = "<html>A build directory must be specified in order to jar class files.</html>";
      }
      else {
        s = "<html>The project needs to be compiled.</html>";
      }
      _cantJarClassesLabel.setText(s);
    }
    else {
      _jarClasses.setEnabled(true);
      _cantJarClassesLabel.setText(" ");
      
      // Main class
      _rootFile = _model.getBuildDirectory();
      LOG.log("_loadSettings, rootFile=" + _rootFile);
      try {
        _rootFile = _rootFile.getCanonicalFile();
      } catch(IOException e) { }
      
      final File mc = _model.getMainClassContainingFile();
      if (mc == null)  _mainClassField.setText("");
      else {
        try {
          OpenDefinitionsDocument mcDoc = _model.getDocumentForFile(mc);
          _mainClassField.setText(mcDoc.getQualifiedClassName());
        }
        catch(IOException ioe) { _mainClassField.setText(""); }
        catch(edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException e) { _mainClassField.setText(""); }
      }
    }
    
    _jarFileSelector.setFileField(_model.getCreateJarFile());
    _mainClassField.getFileChooser().setCurrentDirectory(_rootFile);
    
    _okButton.setEnabled(_jarSources.isSelected() || _jarClasses.isSelected() || _jarAll.isSelected());
    _setEnableExecutable(_jarClasses.isSelected());
    _setEnableCustomManifest(_jarClasses.isSelected());
  }
  
  /** Build the dialog. */
  private void initComponents() {
    JPanel main = _makePanel();
    super.getContentPane().setLayout(new BorderLayout());
    super.getContentPane().add(main, BorderLayout.NORTH);
    
    Action okAction = new AbstractAction("OK") { public void actionPerformed(ActionEvent e) { _ok(); } };
    _okButton = new JButton(okAction);
    
    Action cancelAction = new AbstractAction("Cancel") { public void actionPerformed(ActionEvent e) { _cancel(); } };
    _cancelButton = new JButton(cancelAction);
    
    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());
    
    super.getContentPane().add(bottom, BorderLayout.SOUTH);
    super.setResizable(false); 
  }
  
  /** Make the options panel. 
    * @return The panel with the options for jarring a project
    */
  private JPanel _makePanel() {
    JPanel panel = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(5, 10, 0, 10);
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    
    // Jar All
    _jarAll = new JCheckBox(new AbstractAction("Jar All files") {
      public void actionPerformed(ActionEvent e){
        _toggleClassOptions();
        _jarClasses.setEnabled(!_jarAll.isSelected());
        _jarSources.setEnabled(!_jarAll.isSelected());
        _okButton.setEnabled(_jarSources.isSelected() || _jarClasses.isSelected() || _jarAll.isSelected());
      }
    });
    
    c.weightx = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;
    gridbag.setConstraints(_jarAll, c);
    panel.add(_jarAll);
    
    // Jar Sources
    _jarSources = new JCheckBox(new AbstractAction("Jar source files") {
      public void actionPerformed(ActionEvent e) {
        _jarAll.setEnabled(!_jarSources.isSelected());
        _okButton.setEnabled(_jarSources.isSelected() || _jarClasses.isSelected() || _jarAll.isSelected());
      }
    });
    
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    
    gridbag.setConstraints(_jarSources, c);
    panel.add(_jarSources);

    // Jar class files
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    JPanel jarClassesPanel = _makeClassesPanel();
    gridbag.setConstraints(jarClassesPanel, c);
    panel.add(jarClassesPanel);
    
    _cantJarClassesLabel = new JLabel("<html><center>A build directory must be specified in order to jar class files,<br>and the project needs to be compiled.</center></html>",  SwingConstants.CENTER);
    c.gridx = 0;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(jarClassesPanel, c);
    panel.add(_cantJarClassesLabel);
    _toggleClassOptions();
     
    // Output file
    c.gridx = 0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel label = new JLabel("Jar File");
    label.setToolTipText("The file that the jar should be written to.");
    gridbag.setConstraints(label, c);
    panel.add(label);
    
    c.weightx = 1.0;
    c.gridx = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;
    
    JPanel jarFilePanel = _makeJarFileSelector();
    gridbag.setConstraints(jarFilePanel, c);
    panel.add(jarFilePanel);
    
    return panel;
  }
  
  /** Make the panel that is enabled when you are going to jar class files
    * @return the panel containing the sub-options to the jarring classes option
    */
  private JPanel _makeClassesPanel() {
    JPanel panel = new JPanel();
    GridBagConstraints gridBagConstraints;
    panel.setLayout(new GridBagLayout());
    
    _jarClasses = new JCheckBox(new AbstractAction("Jar classes") {
      public void actionPerformed(ActionEvent e) {
        _toggleClassOptions();
        _jarAll.setEnabled(!_jarClasses.isSelected());
        _okButton.setEnabled(_jarSources.isSelected() || _jarClasses.isSelected() || _jarAll.isSelected());
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    panel.add(_jarClasses, gridBagConstraints);

    // spacer
    JLabel spacer = new JLabel("<html>&nbsp</html>",  SwingConstants.CENTER);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    panel.add(spacer, gridBagConstraints);
    
    JPanel addclasses = new JPanel();
    addclasses.setLayout(new GridBagLayout());
    _makeExecutable = new JCheckBox(new AbstractAction("Make executable") {
      public void actionPerformed(ActionEvent e) {
        _toggleMainClass();        
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    addclasses.add(_makeExecutable, gridBagConstraints);
    
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 20, 0, 0);
    addclasses.add(_makeMainClassSelectorPanel(), gridBagConstraints);
    
    //Custom Manifest
    _editManifest = new JButton(new AbstractAction("Edit Manifest") {
      public void actionPerformed(ActionEvent e){
        _editManifest();
      }
    });
    _customManifest = new JCheckBox(new AbstractAction("Custom Manifest") {
      public void actionPerformed(ActionEvent e){
        _toggleCustomManifest();
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.gridy = 2;
    addclasses.add(_customManifest, gridBagConstraints);
    
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 20, 0, 0);
    addclasses.add(_editManifest, gridBagConstraints);
    
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 25, 0, 0);
    panel.add(addclasses, gridBagConstraints);
    
    return panel;
  }
  
  /** Open a dialog to allow editing of the manifest file for the jar, returns only when the user has closed the dialog. */
  private void _editManifest(){
    final JDialog editDialog = new JDialog(this, "Custom Manifest", true);
    editDialog.setSize(300,400);
    
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    
    editDialog.setLayout(new BorderLayout());
    
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(okButton);
    bottom.add(cancelButton);
    bottom.add(Box.createHorizontalGlue());
    
    editDialog.add(bottom, BorderLayout.SOUTH);
    
    final JTextArea manifest = new JTextArea();
    JScrollPane pane = new JScrollPane(manifest);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    pane.getHorizontalScrollBar().setUnitIncrement(10);
    pane.getVerticalScrollBar().setUnitIncrement(10);
    
    editDialog.add(pane, BorderLayout.CENTER);
    
    manifest.setText(_customManifestText);
    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        editDialog.setVisible(false);
      }
    });
      
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        manifest.setText(_customManifestText);
        editDialog.setVisible(false);
      }
    });
    
    editDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    editDialog.addWindowListener(new WindowAdapter(){
      public void WindowClosed(WindowEvent e){
        manifest.setText(_customManifestText);
        editDialog.setVisible(false);
      }
    });
    
    
    editDialog.setLocationRelativeTo(this);
    editDialog.setVisible(true);
    
    _customManifestText = manifest.getText();
  }
  
  /** Toggles the enabled state on _editManifest */
  private void _toggleCustomManifest(){
    _editManifest.setEnabled(_customManifest.isSelected() && (_jarClasses.isSelected() || _jarAll.isSelected()));
    _setEnableExecutable(!_customManifest.isSelected() && (_jarClasses.isSelected() || _jarAll.isSelected()));
  }
  
  /** Make the panel that lets you select the jar's main class.
    * @return the panel containing the label and the selector for the main class.
    */
  private JPanel _makeMainClassSelectorPanel() {
    LOG.log("_makeMainClassSelectorPanel, _rootFile=" + _rootFile);
    FileChooser chooser = new FileChooser(_rootFile);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Select Main Class");
//      chooser.setTopMessage("Select the main class for the executable jar file:");
    chooser.setApproveButtonText("Select");
    FileFilter filter = new FileFilter() {
      public boolean accept(File f) {
        String name = f.getName();
        return  f.isDirectory() || name.endsWith(".class");
      }
      public String getDescription() { return "Class Files (*.class)"; }
    };
    chooser.addChoosableFileFilter(filter);
    
    _mainClassField = new FileSelectorStringComponent(this, chooser, 20, 12f) {
      protected void _chooseFile() {
        _mainFrame.removeModalWindowAdapter(JarOptionsDialog.this);
        if (getText().length() == 0) {
          LOG.log("getFileChooser().setCurrentDirectory(_rootFile);");
          getFileChooser().setRoot(_rootFile);
          getFileChooser().setCurrentDirectory(_rootFile);
        }
        super._chooseFile();
        File chosen = getFileChooser().getSelectedFile(); // getSelectedFile() may return null
        if((chosen != null) && !chosen.getAbsolutePath().startsWith(_rootFile.getAbsolutePath())) {
          JOptionPane.showMessageDialog(JarOptionsDialog.this,
                                        "Main Class must be in Build Directory or one of its sub-directories.", 
                                        "Unable to set Main Class", JOptionPane.ERROR_MESSAGE);
          setText("");
        }
        _mainFrame.installModalWindowAdapter(JarOptionsDialog.this, LambdaUtil.NO_OP, CANCEL);
      }
      public File convertStringToFile(String s) { 
        s = s.trim().replace('.', java.io.File.separatorChar) + ".class";
        if (s.equals("")) return null;
        else return new File(_rootFile, s);
      }
      
      public String convertFileToString(File f) {
        if (f == null)  return "";
        else {
          try {
            String s = edu.rice.cs.util.FileOps.stringMakeRelativeTo(f, _rootFile);
            s = s.substring(0, s.lastIndexOf(".class"));
            s = s.replace(java.io.File.separatorChar, '.').replace('$', '.');
            int pos = 0;
            boolean ok = true;
            while((pos = s.indexOf('.', pos)) >= 0) {
              if ((s.length() <= pos + 1) || (Character.isDigit(s.charAt(pos + 1)))) {
                ok = false;
                break;
              }
              ++pos;
            }
            if (ok) return s;
            return "";
          }
          catch(IOException e) { return ""; }
        }
      }
    };
    _mainClassField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { setEnabled(); }
      public void removeUpdate(DocumentEvent e) { setEnabled(); }
      public void changedUpdate(DocumentEvent e) { setEnabled(); }
      private void setEnabled() { 
//        Utilities.invokeLater(new Runnable() { 
//          public void run() { 
            assert EventQueue.isDispatchThread();
            _okButton.setEnabled(true); 
//          } 
//        }); 
      }
    });
    JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    _mainClassLabel = new JLabel("Main class:  ");
    _mainClassLabel.setLabelFor(_mainClassField);
    p.add(_mainClassLabel, BorderLayout.WEST);
    p.add(_mainClassField, BorderLayout.CENTER);
    return p;
  }
  
  
  /** Create a file selector to select the output jar file
    * @return The JPanel that contains the selector
    */
  private JPanel _makeJarFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_model.getBuildDirectory());
    fileChooser.setDialogTitle("Select Jar Output File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    
    _jarFileSelector = new FileSelectorComponent(this, fileChooser, 20, 12f, false) {
      /** Opens the file chooser to select a file, putting the result in the file field. */
      protected void _chooseFile() {
        _mainFrame.removeModalWindowAdapter(JarOptionsDialog.this);
        super._chooseFile();
        _mainFrame.installModalWindowAdapter(JarOptionsDialog.this, LambdaUtil.NO_OP, CANCEL);
      }
    };
    _jarFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) { return f.getName().endsWith(".jar") || f.isDirectory(); }
      public String getDescription() { return "Java Archive Files (*.jar)"; }
    });
    
    return _jarFileSelector;
  }
  
  /** Modifies state for when the executable check box is selected */
  private void _setEnableExecutable(boolean b) {
    _makeExecutable.setEnabled(b);
    _toggleMainClass();
  }
  
  /** Enables/Disables the custom manifest checkbox */
  private void _setEnableCustomManifest(boolean b) {
    _customManifest.setEnabled(b);
    _toggleCustomManifest();
  }
  
  /** Method to run when the jar class file is selected */
  private void _toggleClassOptions() {
    _setEnableExecutable(_jarClasses.isSelected() || _jarAll.isSelected());
    _setEnableCustomManifest(_jarClasses.isSelected() || _jarAll.isSelected());
    if (_jarClasses.isSelected() || _jarAll.isSelected()) {
      _cantJarClassesLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
    }
    else {
      _cantJarClassesLabel.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
    }
  }
  
  /** Method to call when the 'Make Executable' check box is clicked. */
  private void _toggleMainClass() {
    _mainClassField.setEnabled(_makeExecutable.isSelected() && (_jarClasses.isSelected() || _jarAll.isSelected()));
    _mainClassLabel.setEnabled(_makeExecutable.isSelected() && (_jarClasses.isSelected() || _jarAll.isSelected()));
    
    _customManifest.setEnabled(!_makeExecutable.isSelected() && (_jarClasses.isSelected() || _jarAll.isSelected()));
  }
  
  /** Method that handels the Cancel button */
  private void _cancel() {
    _lastState = new FrameState(this);
    this.setVisible(false);
  }
  
  /** Do the Jar. */
  private void _ok() {
    // Always apply and save settings
    _saveSettings();
    
    File jarOut = _jarFileSelector.getFileFromField();
    if (jarOut == null) {
      JOptionPane.showMessageDialog(JarOptionsDialog.this,
                                    "You must specify an output file",
                                    "Error: No File Specified",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    else if (jarOut.exists()) {
      if (JOptionPane.showConfirmDialog(JarOptionsDialog.this,
                                        "Are you sure you want to overwrite the file '" + jarOut.getPath() + "'?",
                                        "Overwrite file?",
                                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
        // I want to focus back to the dialog
        return;
      }
    }
    
    setEnabled(false);
    _processingDialog = new ProcessingDialog(this, "Creating Jar File", "Processing, please wait.");
    _processingDialog.setVisible(true);
    SwingWorker worker = new SwingWorker() {
      boolean _success = false;
      HashSet<String> _exceptions = new HashSet<String>();
      
      private boolean jarAll(File dir, JarBuilder jarFile, final File outputFile) throws IOException {
        LOG.log("jarOthers(" + dir + " , " + jarFile + ")");
        java.io.FileFilter allFilter = new java.io.FileFilter() {
          public boolean accept(File f) {
            return !outputFile.equals(f);
          }
        };
        
        File[] files = dir.listFiles(allFilter);
        
        if(files != null) {
          for(int i = 0; i < files.length; i++){
            try {
              if(files[i].isDirectory()){
                LOG.log("jarFile.addDirectoryRecursive(" + files[i] + ")");
                jarFile.addDirectoryRecursive(files[i], files[i].getName(), allFilter);
              }else{
                LOG.log("jarFile.addFile(" + files[i] + ")");
                jarFile.addFile(files[i], "", files[i].getName());
              }
            }
            catch(IOException ioe) { _exceptions.add(ioe.getMessage()); }
          }
        }
        
        return true;
      }
      
      /**
       * Takes input of a file which is a directory and compresses all the class files in it
       * into a jar file
       *
       * @param dir     the File object representing the directory
       * @param jarFile the JarBuilder that the data should be written to
       * @return true on success, false on failure
       */
      private boolean jarBuildDirectory(File dir, JarBuilder jarFile) throws IOException {
      LOG.log("jarBuildDirectory(" + dir + " , " + jarFile + ")");
        
        java.io.FileFilter classFilter = new java.io.FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".class");
          }
        };
        
        File[] files = dir.listFiles(classFilter);
        
        LOG.log("\tfiles = " + files);
        
        if (files != null) { // listFiles may return null if there's an IO error
          for (int i = 0; i < files.length; i++) {
            LOG.log("\t\tfiles[" + i + "] = " + files[i]);
            
            if(files[i] == null || !files[i].exists()) continue;
            
            try {
              if (files[i].isDirectory()) {
                LOG.log("jarFile.addDirectoryRecursive(" + files[i] + ")");
                jarFile.addDirectoryRecursive(files[i], files[i].getName(), classFilter);
              }
              else {
                LOG.log("jarFile.addFile(" + files[i] + ")");
                jarFile.addFile(files[i], "", files[i].getName());
              }
            }
            catch(IOException ioe) { _exceptions.add(ioe.getMessage()); }
          }
        }
        return true;
      }
      
      /**
       * Takes the model and the jar and writes all the sources to the jar
       *
       * @param model the GlobalModel that the files are to come out of
       * @param jar   the JarBuilder that the data should be written to
       * @return true on success, false on failure
       */
      private boolean jarSources(GlobalModel model, JarBuilder jar) {
        List<OpenDefinitionsDocument> srcs = model.getProjectDocuments();
        
        Iterator<OpenDefinitionsDocument> iter = srcs.iterator();
        while (iter.hasNext()) {
          OpenDefinitionsDocument doc = iter.next();
          if (doc.inProject() && ! doc.isAuxiliaryFile()) {
            try {
              // Since the file compiled without any errors, this shouldn't have any problems
              jar.addFile(doc.getFile(), packageNameToPath(doc.getPackageName()), doc.getFileName());
            }
            catch(IOException ioe) { _exceptions.add(ioe.getMessage()); }
          }
        }
        return true;
      }
      
      /** Helper function to convert a package name to its path form
        * @param packageName the name of the package
        * @return the String which is should be the directory that it should be contained within
        */
      private String packageNameToPath(String packageName) {
        return packageName.replaceAll("\\.", System.getProperty("file.separator").replaceAll("\\\\", "\\\\\\\\"));
      }
      /** The method to perform the work
        * @return null
        */
      public Object construct() {
        try {
          File jarOut = _jarFileSelector.getFileFromField();
          if (! jarOut.exists()) jarOut.createNewFile();  // TODO: what if createNewFile() fails? (mgricken)

          if ((_jarClasses.isSelected() && _jarSources.isSelected()) || _jarAll.isSelected()) {
            LOG.log("(_jarClasses.isSelected() && _jarSources.isSelected()) || _jarAll.isSelected()");
            JarBuilder mainJar = null;
            if (_makeExecutable.isSelected() || _customManifest.isSelected()) {
              ManifestWriter mw = new ManifestWriter();
              
              if(_makeExecutable.isSelected())
                mw.setMainClass(_mainClassField.getText());
              else
                mw.setManifestContents(_customManifestText);
              
              mainJar = new JarBuilder(jarOut, mw.getManifest());
            }
            else {
              mainJar = new JarBuilder(jarOut);
            }
            
            //If the project has a set build directory, start there.
            //Otherwise, start at project root
            File binRoot = _model.getBuildDirectory();
            if(binRoot == null || binRoot == FileOps.NULL_FILE || binRoot.toString().trim().length() == 0)
              binRoot = _model.getProjectRoot();
            
            if(!_jarAll.isSelected())
              jarBuildDirectory(binRoot, mainJar);
            
            //File.createTempFile will fail if the prefix provided is less than 3 characters long.
            //Not sure why we're using the build directory name here in the first place
            //But would rather not change it in the general case; just in case.
            String prefix = _model.getBuildDirectory().getName();
            if(prefix.length() < 3)
              prefix = "drjava_tempSourceJar";
            
            File sourceJarFile = File.createTempFile(prefix, ".jar");
            
            if(!_jarAll.isSelected()){
              JarBuilder sourceJar = new JarBuilder(sourceJarFile);
              jarSources(_model, sourceJar);
              sourceJar.close();
              mainJar.addFile(sourceJarFile, "", "source.jar");
            }
            
            if(_jarAll.isSelected()){
              LOG.log("jarAll");
              LOG.log("binRoot=" + binRoot);
              LOG.log("root=" + _model.getProjectRoot());
              LOG.log("FileOps.isAncestorOf(_model.getProjectRoot(),binRoot)=" + FileOps.isAncestorOf(_model.getProjectRoot(),binRoot));
              LOG.log("mainJar=" + mainJar);
              LOG.log("jarOut=" + jarOut);
              jarAll(_model.getProjectRoot(), mainJar, jarOut);
              if(!_model.getProjectRoot().equals(binRoot))
                LOG.log("jarBuildDirectory");
                jarBuildDirectory(binRoot, mainJar);
            }
            
            mainJar.close();
            sourceJarFile.delete();  // TODO: what if delete() fails? (mgricken)
          }
          else if (_jarClasses.isSelected()) {
            JarBuilder jb;
            if (_makeExecutable.isSelected() || _customManifest.isSelected()) {
              ManifestWriter mw = new ManifestWriter();
              if(_makeExecutable.isSelected())
                mw.setMainClass(_mainClassField.getText());
              else
                mw.setManifestContents(_customManifestText);
              
              Manifest m = mw.getManifest();
              
              if(m != null)
                jb = new JarBuilder(jarOut, m);
              else
                throw new IOException("Manifest is malformed");
            }
            else {
              jb = new JarBuilder(jarOut);
            }
            //If the project has a set build directory, start there.
            //Otherwise, start at project root
            File binRoot = _model.getBuildDirectory();
            if(binRoot == null || binRoot == FileOps.NULL_FILE || binRoot.toString().trim().length() == 0)
              binRoot = _model.getProjectRoot();
            
            jarBuildDirectory(binRoot, jb);
            
            jb.close();
          }
          else {
            JarBuilder jb = new JarBuilder(jarOut);
            jarSources(_model, jb);
            jb.close();
          }
          _success = true;
        }
        catch (Exception e) {
          // e.printStackTrace();
          LOG.log("construct: " + e, e.getStackTrace());
        }
        return null;
      }
      public void finished() {
        _processingDialog.setVisible(false);
        _processingDialog.dispose();
        JarOptionsDialog.this.setEnabled(true);
        if (_success) {
          if (_exceptions.size() > 0) {
            ScrollableListDialog<String> dialog = new ScrollableListDialog.Builder<String>()
              .setOwner(JarOptionsDialog.this)
              .setTitle("Problems Creating Jar")
              .setText("There were problems creating this jar file, but DrJava was probably able to recover.")
              .setItems(new ArrayList<String>(_exceptions))
              .setMessageType(JOptionPane.ERROR_MESSAGE)
              .build();
            
            Utilities.setPopupLoc(dialog, JarOptionsDialog.this);
            dialog.showDialog();
          }
          if ((_jarAll.isSelected() || _jarClasses.isSelected()) && _makeExecutable.isSelected()) {
            Object[] options = { "OK", "Run" };
            int res = JOptionPane.showOptionDialog(JarOptionsDialog.this, "Jar file successfully written to '" + _jarFileSelector.getFileFromField().getName() + "'",
                                                   "Jar Creation Successful", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                                   null, options, options[0]);
            JarOptionsDialog.this.setVisible(false);
            if (1==res) {
              SwingWorker jarRunner = new SwingWorker() {
                public Object construct() {
                  try {
                    File cp = _jarFileSelector.getFileFromField();
                    File wd = cp.getParentFile();
                    Process p = JVMBuilder.DEFAULT.classPath(cp).directory(wd).start(_mainClassField.getText());
                    ConcurrentUtil.copyProcessErr(p, System.err);
                    ConcurrentUtil.copyProcessOut(p, System.out);
                    p.waitFor();
                    JOptionPane.showMessageDialog(JarOptionsDialog.this,"Execution of jar file terminated (exit value = " + 
                                                  p.exitValue() + ")", "Execution terminated.",
                                                  JOptionPane.INFORMATION_MESSAGE);
                  }
                  catch(Exception e) {
                    JOptionPane.showMessageDialog(JarOptionsDialog.this, "An error occured while running the jar file: \n" + e, "Error", JOptionPane.ERROR_MESSAGE);
                  }
                  finally {
                    JarOptionsDialog.this.setVisible(false);
                  }
                  return null;
                }
              };
              jarRunner.start();
            }
          }
          else {
            JOptionPane.showMessageDialog(JarOptionsDialog.this, "Jar file successfully written to '" + _jarFileSelector.getFileFromField().getName() + "'", "Jar Creation Successful", JOptionPane.INFORMATION_MESSAGE);
            JarOptionsDialog.this.setVisible(false);
          }
        }
        else {
          ManifestWriter mw = new ManifestWriter();
          if(_makeExecutable.isSelected())
                mw.setMainClass(_mainClassField.getText());
              else
                mw.setManifestContents(_customManifestText);
              
          Manifest m = mw.getManifest();
          
          if(m != null){
            if (_exceptions.size() > 0) {
              ScrollableListDialog<String> dialog = new ScrollableListDialog.Builder<String>()
                .setOwner(JarOptionsDialog.this)
                .setTitle("Error Creating Jar")
                .setText("<html>An error occured while creating the jar file. This could be because the file<br>" + 
                         "that you are writing to or the file you are reading from could not be opened.</html>")
                .setItems(new ArrayList<String>(_exceptions))
                .setMessageType(JOptionPane.ERROR_MESSAGE)
                .build();
              
              Utilities.setPopupLoc(dialog, JarOptionsDialog.this);
              dialog.showDialog();
            }
            else {
              JOptionPane.showMessageDialog(JarOptionsDialog.this, 
                                            "An error occured while creating the jar file. This could be because the file that you " + 
                                            "are writing to or the file you are reading from could not be opened.", 
                                            "Error Creating Jar",
                                            JOptionPane.ERROR_MESSAGE);
            }
          }
          else {
            if (_exceptions.size() > 0) {
              ScrollableListDialog<String> dialog = new ScrollableListDialog.Builder<String>()
                .setOwner(JarOptionsDialog.this)
                .setTitle("Error Creating Jar")
                .setText("The supplied manifest does not conform to the 1.0 Manifest format specification")
                .setItems(new ArrayList<String>(_exceptions))
                .setMessageType(JOptionPane.ERROR_MESSAGE)
                .build();
              
              Utilities.setPopupLoc(dialog, JarOptionsDialog.this);
              dialog.showDialog();
            }
            else {
              JOptionPane.showMessageDialog(JarOptionsDialog.this, "The supplied manifest does not conform to the 1.0 Manifest format specification.",
                                            "Error Creating Jar",
                                            JOptionPane.ERROR_MESSAGE);
            }
          }
          JarOptionsDialog.this.setVisible(false);  
        }
        _model.refreshActiveDocument();
      }
    };
    worker.start();
  }
  
  /** Save the settings for this dialog. */
  private boolean _saveSettings() {
    _lastState = new FrameState(this);
    if ((_model.getCreateJarFile() == null) ||
        (!_model.getCreateJarFile().getName().equals(_jarFileSelector.getFileFromField().getName()))) {
      _model.setCreateJarFile(_jarFileSelector.getFileFromField());
    }
    int f = 0;
    if (_jarClasses.isSelected()) f |= JAR_CLASSES;
    if (_jarSources.isSelected()) f |= JAR_SOURCES;
    if (_jarAll.isSelected()) f |= JAR_ALL;
    if (_makeExecutable.isSelected()) f |= MAKE_EXECUTABLE;
    if (_customManifest.isSelected()) f |= CUSTOM_MANIFEST;
    
    if (f != _model.getCreateJarFlags()) {
      _model.setCreateJarFlags(f);
    }
    
    String currentManifest = _model.getCustomManifest();
    
    if(currentManifest == null || !(currentManifest.equals(_customManifestText))){
      LOG.log("Updated Manifest on: " + _model);
      _model.setCustomManifest(_customManifestText);
    }
    
    return true;
  }
  
  /** Runnable that calls _cancel. */
  protected final Runnable1<WindowEvent> CANCEL = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { _cancel(); }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, CANCEL);
      ProcessingDialog pf = new ProcessingDialog(this, "Checking class files", "Processing, please wait.");
      pf.setVisible(true);
      _loadSettings();
      pf.setVisible(false);
      pf.dispose();
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
