/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.jar.JarBuilder;
import edu.rice.cs.util.jar.ManifestWriter;
import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.util.swing.SwingWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
// import java.io.FileFilter;  // Collides with javax.swing.filechooser.FileFilter
import java.util.Iterator;
import java.util.List;

public class JarOptionsDialog extends JDialog {

  private JCheckBox _jarClasses;
  private JCheckBox _jarSources;
  private JCheckBox _makeExecutable;
  private FileSelectorComponent _jarFileSelector;
  private JTextField _mainClassField;
  private JButton _okButton;
  private JButton _cancelButton;
  private SwingWorker _worker = null;

  private SingleDisplayModel _model;

  /** Create a configuration diaglog
   *  @param mf the instance of mainframe to query into the project
   */
  public JarOptionsDialog(JFrame parent, SingleDisplayModel mf) {
    super(parent, "Jar Options", false);
    _model = mf;
    initComponents();

    _loadSettings();
  }

  /** Load the initial state from the previous files or with defaults. */
  private void _loadSettings() {
    // TODO: This is temporary, we would like to save the defaults in the project or something
    //       like that
    if (_model.getBuildDirectory() == null) {
      _jarClasses.setSelected(false);
      _jarClasses.setEnabled(false);
      _jarClasses.setToolTipText("A build directory must be specified in order to jar classes");
      _disableExecutable();
      _makeExecutable.setSelected(false);
    }
    else {
      _jarClasses.setSelected(true);
      _enableExecutable();
      _makeExecutable.setSelected(false);
    }
    _jarSources.setSelected(false);
  }

  /** Build the dialog. */
  private void initComponents() {
    JPanel main = _makePanel();
    super.getContentPane().setLayout(new BorderLayout());

    super.getContentPane().add(main, BorderLayout.NORTH);

    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        _ok();
      }
    };
    _okButton = new JButton(okAction);

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        _cancel();
      }
    };
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
    pack();
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

    // Jar class files
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = labelInsets;
    c.fill = GridBagConstraints.HORIZONTAL;

    JPanel dirPanel = _makeClassesPanel();
    gridbag.setConstraints(dirPanel, c);
    panel.add(dirPanel);

    // Jar Sources
    _jarSources = new JCheckBox(new AbstractAction("Jar source files") {
      public void actionPerformed(ActionEvent e) {
      }
    });

    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    gridbag.setConstraints(_jarSources, c);
    panel.add(_jarSources);

    // Output file
    c.gridx = 0;
    c.weightx = 0.0;
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

    JPanel jarFilePanel = _jarFileSelector();
    gridbag.setConstraints(jarFilePanel, c);
    panel.add(jarFilePanel);

    return panel;
  }

  /** Make the panel that is enabled when you are going to jar class files
   *  @return the panel containing the sub-options to the jarring classes option
   */
  private JPanel _makeClassesPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    _jarClasses = new JCheckBox(new AbstractAction("Jar classes") {
      public void actionPerformed(ActionEvent e) {
        _toggleClassOptions();
      }
    });
    _makeExecutable = new JCheckBox(new AbstractAction("Make executable") {
      public void actionPerformed(ActionEvent e) {
        _toggleMainClass();
      }
    });
    GridBagConstraints gridBagConstraints;

    JPanel addclasses = new JPanel();

    panel.setLayout(new java.awt.GridBagLayout());

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    panel.add(_jarClasses, gridBagConstraints);

    addclasses.setLayout(new GridBagLayout());

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    addclasses.add(_makeExecutable, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 20, 0, 0);
    addclasses.add(_mainFileSelector(), gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 25, 0, 0);
    panel.add(addclasses, gridBagConstraints);

    return panel;
  }

  /** Create a file selector to select the main class file
   *  @return The JPanel that contains the selector
   */
  private JTextField _mainFileSelector() {
    // JTextFields aren't resized so I add the width
    _mainClassField = new JTextField(18);
    _mainClassField.setToolTipText("The fully qualified class name of the main document to be run");
    return _mainClassField;
  }

  /**
   * Create a file selector to select the output jar file
   *
   * @return The JPanel that contains the selector
   */
  private JPanel _jarFileSelector() {
    JFileChooser fileChooser = new JFileChooser(_model.getBuildDirectory());
    fileChooser.setDialogTitle("Select Jar Output File");
    fileChooser.setApproveButtonText("Select");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);

    _jarFileSelector = new FileSelectorComponent(null, fileChooser, 20, 12f);
    _jarFileSelector.setFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".jar") || f.isDirectory();
      }

      public String getDescription() {
        return "Java Archive Files (*.jar)";
      }

    });

    return _jarFileSelector;
  }

  /**
   * Modifies state for when the executable check box is selected
   */
  private void _enableExecutable() {
    _makeExecutable.setEnabled(true);
    if (_makeExecutable.isSelected())
      _mainClassField.setEnabled(true);
    else
      _mainClassField.setEnabled(false);
  }

  /**
   * Modifies state for when the executable check box is not selected
   */
  private void _disableExecutable() {
    _makeExecutable.setEnabled(false);
    _mainClassField.setEnabled(false);
  }

  /**
   * Method to run when the jar class file is selected
   */
  private void _toggleClassOptions() {
    if (_jarClasses.isSelected())
      _enableExecutable();
    else
      _disableExecutable();
  }

  /**
   * Method to call when the 'Make Executable' check box is clicked.
   */
  private void _toggleMainClass() {
    if (_makeExecutable.isSelected())
      _mainClassField.setEnabled(true);
    else
      _mainClassField.setEnabled(false);
  }

  /**
   * Method that handels the Cancel button
   */
  private void _cancel() {
    if ( _worker != null ) {
      _worker.interrupt();
    }
    this.setVisible(false);
  }

  /**
   * Do the Jar
   */
  private void _ok() {
    // Always apply and save settings
    _saveSettings();
    _worker = new SwingWorker() {

      /**
       * Takes input of a file which is a directory and compresses all the class files in it
       * into a jar file
       *
       * @param dir     the File object representing the directory
       * @param jarFile the JarBuilder that the data should be written to
       * @return true on success, false on failure
       */
      private boolean jarBuildDirectory(File dir, JarBuilder jarFile) throws IOException {
        java.io.FileFilter classFilter = new java.io.FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".class");
          }
        };

        File[] files = dir.listFiles(classFilter);
        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            jarFile.addDirectoryRecursive(files[i], files[i].getName(), classFilter);
          }
          else {
            jarFile.addFile(files[i], "", files[i].getName());
          }
        }

        return true;
      }

      /**
       * Takes the model and the jar and writes all the sources to the jar
       *
       * @param model the SingleDisplayModel that the files are to come out of
       * @param jar   the JarBuilder that the data should be written to
       * @return true on success, false on failure
       */
      private boolean jarSources(SingleDisplayModel model, JarBuilder jar) {
        List<OpenDefinitionsDocument> srcs = model.getProjectDocuments();

        Iterator<OpenDefinitionsDocument> iter = srcs.iterator();
        while (iter.hasNext()) {
          OpenDefinitionsDocument doc = iter.next();
          if (doc.inProject() && !doc.isAuxiliaryFile()) {
            try {
              // Since the file compiled without any errors, this shouldn't have any problems
              jar.addFile(doc.getFile(), packageNameToPath(doc.getPackageName()), doc.getFilename());
            }
            catch (IOException e) {
              e.printStackTrace();
              throw new UnexpectedException(e);
            }
            catch (InvalidPackageException e) {
              e.printStackTrace();
              throw new UnexpectedException(e);
            }
          }
        }
        return true;
      }

      /**
       * Helper function to convert a package name to its path form
       *
       * @param packageName the name of the package
       * @return the String which is should be the directory that it should be contained within
       */
      private String packageNameToPath(String packageName) {
        return packageName.replaceAll("\\.", System.getProperty("file.separator").replaceAll("\\\\", "\\\\\\\\"));
      }
      /**
       * The method to perform the work
       *
       * @return null
       */
      public Object construct() {
          File jarOut = _jarFileSelector.getFileFromField();
          if (jarOut == null) {
            JOptionPane.showMessageDialog(JarOptionsDialog.this,
                    "You must specify an output file",
                    "Error: No File Specified",
                    JOptionPane.OK_OPTION);
            return null;
          }
          else if (jarOut.exists()) {
            if (JOptionPane.showConfirmDialog(JarOptionsDialog.this,
                    "Are you sure you want to overwrite the file '" + jarOut.getPath() + "'?",
                    "Overwrite file?",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
              // I want to focus back to the dialog
              return null;
            }
          }
          else {
            try {
              jarOut.createNewFile();
            }
            catch (IOException e) {
              JarOptionsDialog.this.setVisible(false);
              e.printStackTrace();
              throw new UnexpectedException(e);
            }
          }

          // If the classes are going to be bundled, we compile them
          if (_jarClasses.isSelected()) {
            try {
              _model.getCompilerModel().compileAll();
            }
            catch (IOException e) {
              return null;
            }
          }

          if (_jarClasses.isSelected() && _jarSources.isSelected()) {
            try {
              File classJarFile = File.createTempFile(_model.getBuildDirectory().getName(), ".jar");
              JarBuilder classJar = new JarBuilder(classJarFile);
              jarBuildDirectory(_model.getBuildDirectory(), classJar);
              classJar.close();

              JarBuilder mainJar = null;
              if (_makeExecutable.isSelected()) {
                ManifestWriter mw = new ManifestWriter();
                mw.addClassPath(_model.getBuildDirectory().getName() + ".jar");
                mw.setMainClass(_mainClassField.getText());
                mainJar = new JarBuilder(jarOut, mw.getManifest());
              }
              else {
                ManifestWriter mw = new ManifestWriter();
                mw.addClassPath(_model.getBuildDirectory().getName() + ".jar");
                mainJar = new JarBuilder(jarOut, mw.getManifest());
              }
              mainJar.addFile(classJarFile, "", _model.getBuildDirectory().getName() + ".jar");

              jarSources(_model, mainJar);

              mainJar.close();
              classJarFile.delete();
              JarOptionsDialog.this.setVisible(false);
            }
            catch (IOException e) {
              // There was a file access error of some sort
              JOptionPane.showConfirmDialog(JarOptionsDialog.this, "An error occured while writing the jar file. This could be becasue the file that you are writing to or the file you are reading from could not be opened.", "Error: File Access", JOptionPane.OK_OPTION);
            }
          }
          else if (_jarClasses.isSelected() && !_jarSources.isSelected()) {
            try {
              JarBuilder jb;
              if (_makeExecutable.isSelected()) {
                ManifestWriter mw = new ManifestWriter();
                mw.setMainClass(_mainClassField.getText());
                jb = new JarBuilder(jarOut, mw.getManifest());
              }
              else {
                jb = new JarBuilder(jarOut);
              }
              jarBuildDirectory(_model.getBuildDirectory(), jb);
              jb.close();
              JarOptionsDialog.this.setVisible(false);
            }
            catch (IOException e) {
              // There was a file access error of some sort
              JOptionPane.showConfirmDialog(JarOptionsDialog.this, "An error occured while writing the jar file. This could be becasue the file that you are writing to or the file you are reading from could not be opened.", "Error: File Access", JOptionPane.OK_OPTION);
            }
          }
          else if (!_jarClasses.isSelected() && _jarSources.isSelected()) {
            try {
              JarBuilder jb = new JarBuilder(jarOut);
              jarSources(_model, jb);
              jb.close();
              JarOptionsDialog.this.setVisible(false);
            }
            catch (IOException e) {
              // There was a file access error of some sort
              JOptionPane.showConfirmDialog(JarOptionsDialog.this, "An error occured while writing the jar file. This could be becasue the file that you are writing to or the file you are reading from could not be opened.", "Error: File Access", JOptionPane.OK_OPTION);
            }
          }
          else if (!_jarClasses.isSelected() && !_jarSources.isSelected()) {
            JarOptionsDialog.this.setVisible(false);
            // We aren't jarring anything
            return null;
          }
          JOptionPane.showConfirmDialog(JarOptionsDialog.this,"Jar file successfully written to '"+jarOut.getName()+"'", "Jar Creation Successful", JOptionPane.OK_CANCEL_OPTION);
          return null;
      }
    };
    _worker.start();
  }

  /**
   * Save the settings for this dialog
   */
  private boolean _saveSettings() {
    // TODO: We want to save these settings in the project
    return true;
  }

  public void setVisible(boolean vis) {
    if (vis) {
//    suggested from zaq@nosi.com, to keep the frame on the screen!
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = this.getSize();
      this.setLocation((screenSize.width - frameSize.width) / 2,
              (screenSize.height - frameSize.height) / 2);
    }
    super.setVisible(vis);
  }
  
}
