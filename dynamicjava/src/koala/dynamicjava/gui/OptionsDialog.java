/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import koala.dynamicjava.gui.resource.*;
import koala.dynamicjava.gui.resource.ActionMap;

/**
 * The 'options' dialog
 *
 * @author Stephane Hillion
 * @version 1.0 - 1999/10/28
 */

public class OptionsDialog extends JDialog implements ActionMap {
  /** The resource file name */
  protected final static String RESOURCE = "koala.dynamicjava.gui.resources.options";
  
  /** The resource bundle */
  protected static ResourceBundle bundle;
  
  /** The resource manager */
  protected static ResourceManager rManager;
  
  /** The classpath list */
  protected StringList classPathList;
  
  /** The library path list */
  protected StringList libraryPathList;
  
  /** The URL chooser */
  protected URLChooser urlChooser;
  
  /** The class path list content when the dialog is shown */
  protected String[] classes;
  
  /** The library path list content when the dialog is shown */
  protected String[] libraries;
  
  /** The main frame */
  protected Main mainFrame;
  
  /** The interpreter panel */
  protected InterpreterPanel interpreterPanel;
  
  /** The GUI panel */
  protected GUIPanel guiPanel;
  
  static {
    bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
    rManager = new ResourceManager(bundle);
  }
  
  /**
   * Creates a new dialog
   * @param owner the owner of this dialog
   */
  public OptionsDialog(Main owner) {
    super(owner);
    
    mainFrame = owner;
    
    urlChooser = new URLChooser(this, new UCOKButtonAction());
    
    listeners.put("OKButtonAction",     new OKButtonAction());
    listeners.put("CancelButtonAction", new CancelButtonAction());
    
    setTitle(rManager.getString("Dialog.title"));
    setSize(rManager.getInteger("Dialog.width"),
            rManager.getInteger("Dialog.height"));
    setModal(true);
    
    getContentPane().add(createTabbedPane());
    getContentPane().add("South", createButtonsPanel());
  }
  
  // ActionMap implementation
  
  /** The map that contains the listeners */
  protected Map<String,Action> listeners = new HashMap<String,Action>();
  
  /**
   * Returns the action associated with the given string
   * or null on error
   * @param key the key mapped with the action to get
   * @throws MissingListenerException if the action is not found
   */
  public Action getAction(String key) throws MissingListenerException {
    return (Action)listeners.get(key);
  }
  
  /** Returns the classpath */
  public String[] getClassPath() { return classPathList.getStrings(); }
  
  /** Returns the library path */
  public String[] getLibraryPath() { return libraryPathList.getStrings(); }
  
  /** Returns the interpreter name */
  public String getInterpreterName() { return interpreterPanel.getName(); }
  
  /** Has the interpreter to be defined? */
  public boolean isInterpreterDefined() { return interpreterPanel.isExportationSelected(); }
  
  /** Is the initialization file option selected? */
  public boolean isInitializationSelected() { return interpreterPanel.isInitializationSelected(); }
  
  /** The initialization file name */
  public String getInitializationFilename() { return interpreterPanel.getFilename(); }
  
  /** Returns the GUI name */
  public String getGUIName() { return guiPanel.getName(); }
  
  /** Has the GUI to be defined? */
  public boolean isGUIDefined() { return guiPanel.isSelected(); }
  
  /** Has the output to be redirected?*/
  public boolean isOutputSelected() { return guiPanel.isOutputSelected(); }
  
  /** Has the standard error to be redirected? */
  public boolean isErrorSelected() { return guiPanel.isErrorSelected(); }
  
  /** Is the startup initialization file option selected? */
  public boolean isStartupInitializationSelected() { return guiPanel.isInitializationSelected(); }
  
  /** The startup initialization file name */
  public String getStartupInitializationFilename() { return guiPanel.getFilename(); }
  
  /** Returns an object that holds the current options */
  public OptionSet getOptions() { return new OptionSet(this); }
  
  /** Sets the options according to the given option set */
  public void setOptions(OptionSet optionSet) {
    classPathList.setStrings(optionSet.classPath);
    libraryPathList.setStrings(optionSet.libraryPath);
    interpreterPanel.setExportationSelected(optionSet.isInterpreterSelected);
    interpreterPanel.setName(optionSet.interpreterName);
    interpreterPanel.setInitializationSelected(optionSet.interpreterFileSelected);
    interpreterPanel.setFilename(optionSet.interpreterFilename);
    guiPanel.setSelected(optionSet.isGUISelected);
    guiPanel.setName(optionSet.guiName);
    guiPanel.setOutputSelected(optionSet.isOutputSelected);
    guiPanel.setErrorSelected(optionSet.isErrorSelected);
    guiPanel.setInitializationSelected(optionSet.guiFileSelected);
    guiPanel.setFilename(optionSet.guiFilename);
  }
  
  /** Creates the tabbed pane */
  protected JTabbedPane createTabbedPane() {
    JTabbedPane p = new JTabbedPane();
    
    p.addTab(rManager.getString("General.title"),   createGeneralPanel());
    p.addTab(rManager.getString("PathPanel.title"), createPathPanel());
    
    return p;
  }
  
  
  /** Creates the general panel */
  protected JPanel createGeneralPanel() {
    JPanel p = new JPanel(new BorderLayout());
    
    JPanel p2 = new JPanel(new GridBagLayout());
    p.add("North", p2);
    
    GridBagConstraints constraints = new GridBagConstraints();
    
    constraints.weightx = 1.0;
    constraints.weighty = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    setConstraintsCoords(constraints, 0, 0, 1, 1);
    interpreterPanel = new InterpreterPanel();
    p2.add(interpreterPanel, constraints);
    
    constraints.weightx = 1.0;
    constraints.weighty = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    setConstraintsCoords(constraints, 0, 1, 1, 1);
    p2.add(guiPanel = new GUIPanel(), constraints);
    
    return p;
  }
  
  /** Creates the path panel */
  protected JPanel createPathPanel() {
    JPanel p = new JPanel(new GridLayout(2, 1));
    
    p.add(createClassPathPanel());
    p.add(createLibraryPathPanel());
    
    return p;
  }
  
  /** Creates the classpath panel */
  protected JPanel createClassPathPanel() {
    classPathList = new StringList(new CPLAddButtonAction());
    classPathList.setBorder(BorderFactory.createTitledBorder
                              (BorderFactory.createEtchedBorder(),
                               rManager.getString("ClassPathPanel.title")));
    return classPathList;
  }
  
  /** Creates the library path panel */
  protected JPanel createLibraryPathPanel() {
    libraryPathList = new StringList(new LPLAddButtonAction());
    libraryPathList.setBorder(BorderFactory.createTitledBorder
                                (BorderFactory.createEtchedBorder(),
                                 rManager.getString("LibraryPathPanel.title")));
    return libraryPathList;
  }
  
  /** Creates the buttons pane */
  protected JPanel createButtonsPanel() {
    JPanel  p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    ButtonFactory bf = new ButtonFactory(bundle, this);
    p.add(bf.createJButton("OKButton"));
    p.add(bf.createJButton("CancelButton"));
    
    return p;
  }
  
  protected static void setConstraintsCoords(GridBagConstraints constraints,
                                             int x, int y, 
                                             int width, int height) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = width;
    constraints.gridheight = height;
  }
  
  /** To save the options */
  public static class OptionSet {

    /** The class path */
    public String[] classPath;
    
    /** The library path */
    public String[] libraryPath;
    
    /** The interpreter checkbox state */
    public boolean isInterpreterSelected;
    
    /** The interpreter name */
    public String interpreterName;
    
    /** The interpreter file checkbox state */
    public boolean interpreterFileSelected;
    
    /** The interpreter initialization file name */
    public String interpreterFilename;
    
    /** The GUI checkbox state */
    public boolean isGUISelected;
    
    /** The GUI name */
    public String guiName;
    
    /** The output checkbox state */
    public boolean isOutputSelected;
    
    /** The error checkbox state */
    public boolean isErrorSelected;
    
    /** The GUI file checkbox state */
    public boolean guiFileSelected;
    
    /** The GUI initialization file name */
    public String guiFilename;
    
    /** Creates a new option set with default values */
    public OptionSet() {
      classPath               = new String[0];
      libraryPath             = new String[0];
      isInterpreterSelected   = false;
      interpreterName         = "";
      interpreterFileSelected = false;
      interpreterFilename     = "";
      isGUISelected           = false;
      guiName                 = "";
      isOutputSelected        = false;
      isErrorSelected         = false;
      guiFileSelected         = false;
      guiFilename             = "";
    }
    
    /** Creates a new option set using OptionsDialog d*/
    public OptionSet(OptionsDialog d) {
      classPath               = d.classPathList.getStrings();
      libraryPath             = d.libraryPathList.getStrings();
      isInterpreterSelected   = d.interpreterPanel.isExportationSelected();
      interpreterName         = d.interpreterPanel.getName();
      interpreterFileSelected = d.interpreterPanel.isInitializationSelected();
      interpreterFilename     = d.interpreterPanel.getFilename();
      isGUISelected           = d.guiPanel.isSelected();
      guiName                 = d.guiPanel.getName();
      isOutputSelected        = d.guiPanel.isOutputSelected();
      isErrorSelected         = d.guiPanel.isErrorSelected();
      guiFileSelected         = d.guiPanel.isInitializationSelected();
      guiFilename             = d.guiPanel.getFilename();
    }
  }
  
  /** The interpreter option panel */
  protected class InterpreterPanel extends JPanel {

    /** The text field */
    protected JTextField textField;
    
    /** The check box */
    protected JCheckBox checkBox;
    
    /** The label */
    protected JLabel label;
    
    /** The file check box */
    protected JCheckBox fileCheckBox;
    
    /** The file label */
    protected JLabel fileLabel;
    
    /** The file text field */
    protected JTextField fileTextField;
    
    /** The browse button */
    protected JButton browseButton;
    
    /** Creates a new panel */
    public InterpreterPanel() {
      super(new GridBagLayout());
      setBorder(BorderFactory.createTitledBorder
                  (BorderFactory.createEtchedBorder(),
                   rManager.getString("InterpreterPanel.title")));
      
      GridBagConstraints constraints = new GridBagConstraints();
      
      checkBox = new JCheckBox(rManager.getString("InterpreterCheckBox.text"));
      checkBox.addChangeListener(new CheckBoxChangeListener());
      constraints.insets = new Insets(3, 3, 3, 3);
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 0, 3, 1);
      add(checkBox, constraints);
      
      label = new JLabel(rManager.getString("InterpreterLabel.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 1, 1, 1);
      add(label, constraints);
      
      textField = new JTextField();
      constraints.weightx = 1.0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 1, 1, 2, 1);
      add(textField, constraints);
      
      label.setEnabled(false);
      textField.setEnabled(false);
      
      fileCheckBox = new JCheckBox(rManager.getString("InitFileCheckBox.text"));
      fileCheckBox.addChangeListener(new FileCheckBoxChangeListener());
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 2, 3, 1);
      add(fileCheckBox, constraints);
      
      fileLabel = new JLabel(rManager.getString("InitFileLabel.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 3, 3, 1);
      add(fileLabel, constraints);
      
      fileTextField = new JTextField();
      constraints.weightx = 1.0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 4, 2, 1);
      add(fileTextField, constraints);
      
      ButtonFactory bf = new ButtonFactory(bundle, OptionsDialog.this);
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.EAST;
      setConstraintsCoords(constraints, 2, 4, 1, 1);
      add(browseButton = bf.createJButton("InitFileBrowseButton"), constraints);
      browseButton.addActionListener(new InitFileBrowseButtonAction());
      
      fileLabel.setEnabled(false);
      fileTextField.setEnabled(false);
      browseButton.setEnabled(false);
    }
    
    /** Has the interpreter to be exported? */
    public boolean isExportationSelected() { return checkBox.isSelected(); }
    
    /** Sets the state of the checkbox */
    public void setExportationSelected(boolean b) { checkBox.setSelected(b); }
    
    /** Returns the name to give to the interpreter */
    public String getName() { return textField.getText(); }
    
    /** Sets the interpreter name */
    public void setName(String s) { textField.setText(s); }
    
    /** Is the initialization file checkbox selected */
    public boolean isInitializationSelected() { return fileCheckBox.isSelected(); }
    
    /** Sets the initialization file checkbox state */
    public void setInitializationSelected(boolean b) { fileCheckBox.setSelected(b); }
    
    /** Returns the initialization file name */
    public String getFilename() { return fileTextField.getText(); }
    
    /** Sets the initialization file name */
    public void setFilename(String s) { fileTextField.setText(s); }
    
    /** To listen to the checkbox */
    protected class CheckBoxChangeListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
        boolean selected = checkBox.isSelected();
        label.setEnabled(selected);
        textField.setEnabled(selected);
      }
    }
    
    /** To listen to the file checkbox */
    protected class FileCheckBoxChangeListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
        boolean selected = fileCheckBox.isSelected();
        fileLabel.setEnabled(selected);
        fileTextField.setEnabled(selected);
        browseButton.setEnabled(selected);
      }
    }
    
    /** The action associated with the 'browse' button */
    protected class InitFileBrowseButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileHidingEnabled(false);
        
        int choice = fileChooser.showOpenDialog(OptionsDialog.this);
        if (choice == JFileChooser.APPROVE_OPTION) {
          File f = fileChooser.getSelectedFile();
          try {
            fileTextField.setText(f.getCanonicalPath());
          } catch (IOException ex) {
          }
        }
      }
    }
  }
  
  /** The GUI option panel */
  protected class GUIPanel extends JPanel {
    /**
     * The text field
     */
    protected JTextField textField;
    
    /** The check box */
    protected JCheckBox checkBox;
    
    /** The label */
    protected JLabel label;
    
    /** The output check box */
    protected JCheckBox outputCheckBox;
    
    /** The error check box */
    protected JCheckBox errorCheckBox;
    
    /** The file check box */
    protected JCheckBox fileCheckBox;
    
    /** The file label */
    protected JLabel fileLabel;
    
    /** The file text field */
    protected JTextField fileTextField;
    
    /** The browse button*/
    protected JButton browseButton;
    
    /** Creates a new panel */
    public GUIPanel() {
      super(new GridBagLayout());
      setBorder(BorderFactory.createTitledBorder
                  (BorderFactory.createEtchedBorder(), rManager.getString("GUIPanel.title")));
      
      GridBagConstraints constraints = new GridBagConstraints();
      
      checkBox = new JCheckBox(rManager.getString("GUICheckBox.text"));
      checkBox.addChangeListener(new CheckBoxChangeListener());
      constraints.insets = new Insets(3, 3, 3, 3);
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 0, 3, 1);
      add(checkBox, constraints);
      
      label = new JLabel(rManager.getString("GUILabel.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;
      setConstraintsCoords(constraints, 0, 1, 1, 1);
      add(label, constraints);
      
      textField = new JTextField();
      constraints.weightx = 1.0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 1, 1, 2, 1);
      add(textField, constraints);
      
      label.setEnabled(false);
      textField.setEnabled(false);
      
      fileCheckBox = new JCheckBox(rManager.getString("GUIInitFileCheckBox.text"));
      fileCheckBox.addChangeListener(new FileCheckBoxChangeListener());
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 2, 3, 1);
      add(fileCheckBox, constraints);
      
      fileLabel = new JLabel(rManager.getString("GUIInitFileLabel.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 3, 3, 1);
      add(fileLabel, constraints);
      
      fileTextField = new JTextField();
      constraints.weightx = 1.0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 4, 2, 1);
      add(fileTextField, constraints);
      
      ButtonFactory bf = new ButtonFactory(bundle, OptionsDialog.this);
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.EAST;
      setConstraintsCoords(constraints, 2, 4, 1, 1);
      add(browseButton = bf.createJButton("GUIInitFileBrowseButton"), constraints);
      browseButton.addActionListener(new InitFileBrowseButtonAction());
      
      fileLabel.setEnabled(false);
      fileTextField.setEnabled(false);
      browseButton.setEnabled(false);
      
      outputCheckBox = new JCheckBox(rManager.getString("GUIOutputCheckBox.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 5, 3, 1);
      add(outputCheckBox, constraints);
      
      errorCheckBox = new JCheckBox(rManager.getString("GUIErrorCheckBox.text"));
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      setConstraintsCoords(constraints, 0, 6, 3, 1);
      add(errorCheckBox, constraints);
    }
    
    /**
     * Has the GUI to be exported?
     */
    public boolean isSelected() {
      return checkBox.isSelected();
    }
    
    /**
     * Sets the state of the checkbox
     */
    public void setSelected(boolean b) {
      checkBox.setSelected(b);
    }
    
    /**
     * Returns the name to give to the GUI
     */
    public String getName() {
      return textField.getText();
    }
    
    /**
     * Sets the GUI name
     */
    public void setName(String s) {
      textField.setText(s);
    }
    
    /**
     * Has the output to be redirected?
     */
    public boolean isOutputSelected() {
      return outputCheckBox.isSelected();
    }
    
    /**
     * Sets the state of the output checkbox
     */
    public void setOutputSelected(boolean b) {
      outputCheckBox.setSelected(b);
    }
    
    /**
     * Has the error to be redirected?
     */
    public boolean isErrorSelected() {
      return errorCheckBox.isSelected();
    }
    
    /**
     * Sets the state of the error checkbox
     */
    public void setErrorSelected(boolean b) {
      errorCheckBox.setSelected(b);
    }
    
    /**
     * Is the initialization file checkbox selected
     */
    public boolean isInitializationSelected() {
      return fileCheckBox.isSelected();
    }
    
    /**
     * Sets the initialization file checkbox state
     */
    public void setInitializationSelected(boolean b) {
      fileCheckBox.setSelected(b);
    }
    
    /**
     * Returns the initialization file name
     */
    public String getFilename() {
      return fileTextField.getText();
    }
    
    /**
     * Sets the initialization file name
     */
    public void setFilename(String s) {
      fileTextField.setText(s);
    }
    
    /**
     * To listen to the checkbox
     */
    protected class CheckBoxChangeListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
        boolean selected = checkBox.isSelected();
        label.setEnabled(selected);
        textField.setEnabled(selected);
      }
    }
    
    /**
     * To listen to the file checkbox
     */
    protected class FileCheckBoxChangeListener implements ChangeListener {
      public void stateChanged(ChangeEvent e) {
        boolean selected = fileCheckBox.isSelected();
        fileLabel.setEnabled(selected);
        fileTextField.setEnabled(selected);
        browseButton.setEnabled(selected);
      }
    }
    
    /**
     * The action associated with the 'browse' button
     */
    protected class InitFileBrowseButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileHidingEnabled(false);
        
        int choice = fileChooser.showOpenDialog(OptionsDialog.this);
        if (choice == JFileChooser.APPROVE_OPTION) {
          File f = fileChooser.getSelectedFile();
          try {
            fileTextField.setText(f.getCanonicalPath());
          } catch (IOException ex) {
          }
        }
      }
    }
  }
  
  /**
   * The action associated with the 'add' button of the class path panel
   */
  protected class CPLAddButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      urlChooser.pack();
      Rectangle fr = getBounds();
      Dimension ud = urlChooser.getSize();
      urlChooser.setLocation(fr.x + (fr.width  - ud.width) / 2,
                             fr.y + (fr.height - ud.height) / 2);
      urlChooser.show();
    }
  }
  
  /**
   * The action associated with the 'add' button of the library path panel
   */
  protected class LPLAddButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileHidingEnabled(false);
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      
      int choice = fileChooser.showOpenDialog(OptionsDialog.this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        File f = fileChooser.getSelectedFile();
        try {
          libraryPathList.add(f.getCanonicalPath());
        } catch (IOException ex) {
        }
      }
    }
  }
  
  /**
   * The action associated with the 'OK' button
   */
  protected class OKButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (isInterpreterDefined() &&
          getInterpreterName().equals("")) {
        JOptionPane.showMessageDialog
          (OptionsDialog.this,
           rManager.getString("InterpreterError.text"),
           rManager.getString("InterpreterError.title"),
           JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      if (isInitializationSelected() &&
          getInitializationFilename().equals("")) {
        JOptionPane.showMessageDialog
          (OptionsDialog.this,
           rManager.getString("InterpreterFilenameError.text"),
           rManager.getString("InterpreterFilenameError.title"),
           JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      if (isGUIDefined() &&
          getGUIName().equals("")) {
        JOptionPane.showMessageDialog
          (OptionsDialog.this,
           rManager.getString("GUIError.text"),
           rManager.getString("GUIError.title"),
           JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      dispose();
      int i = JOptionPane.showConfirmDialog
        (mainFrame,
         rManager.getString("ConfirmDialog.text"),
         rManager.getString("ConfirmDialog.title"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.INFORMATION_MESSAGE);
      if (i == JOptionPane.OK_OPTION) {
        mainFrame.reinitializeInterpreter();
        mainFrame.applyOptions();
      }
      try {
        mainFrame.saveOptions();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog
          (mainFrame,
           rManager.getString("SaveOptionsError.text") + ex.getMessage(),
           rManager.getString("SaveOptionsError.title"),
           JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  /**
   * The action associated with the 'Cancel' button
   */
  protected class CancelButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      mainFrame.restoreOptions();
      dispose();
    }
  }
  
  /**
   * The action associated with the 'OK' button of the URL chooser
   */
  protected class UCOKButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      classPathList.add(urlChooser.getText());
    }
  }
}
