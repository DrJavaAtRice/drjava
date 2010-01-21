package edu.rice.cs.drjava.ui;

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

import edu.rice.cs.util.swing.Utilities;

/** Generates Java source from information entered in the "New Class" dialog.
  * @version $Id$
  */
public class NewJavaClassDialog extends JDialog {

//  private static final int FRAME_WIDTH = 503;
//  private static final int FRAME_HEIGHT = 400;

  private MainFrame _mainFrame;      
  private SingleDisplayModel _model; 

  private final JButton _okButton;
  private final JButton _cancelButton;
  private JPanel _mainPanel;
  private final JTextField _className = new JTextField();
  private final JTextField _interfaces = new JTextField();
  private final JTextField _superClass = new JTextField();
  private final JRadioButton _defaultRadio = new JRadioButton("default", false);
  private final JRadioButton _publicRadio = new JRadioButton("public", true);
  private final JCheckBox _abstractCheck = new JCheckBox("abstract");
  private final JCheckBox _finalCheck = new JCheckBox("final");
  private final ButtonGroup _group1 = new ButtonGroup();
  private final JCheckBox _mainMethod = new JCheckBox("Include main method");
  private final JCheckBox _classConstructor = new JCheckBox("Include class constructor");
  private final JLabel _errorMessage = new JLabel("");
  
  /** Constructs New Java Class frame and displays it. */
  public NewJavaClassDialog(MainFrame mf) {
    super(mf, "New Java Class", true);

    //  Utilities.show("NewJavaClass(" + mf + ")");

    _mainFrame = mf;
    _model = _mainFrame.getModel();
    _mainPanel= new JPanel();
    
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) { ok(); }
    };
    _okButton = new JButton(okAction);

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
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 0.0;
    cpLayout.setConstraints(bottom, c);
    cp.add(bottom);

    pack();
    Utilities.setPopupLoc(this, _mainFrame);

    reset();
  }

  /** Resets the frame and hides it. */
  public void cancel() {
    setVisible(false);
    reset();
  }

  public void reset() {
    _className.setText("");
    _interfaces.setText("");
    _superClass.setText("");
    _defaultRadio.setSelected(false);
    _publicRadio.setSelected(true);
    _abstractCheck.setSelected(false);
    _finalCheck.setSelected(false);
    _mainMethod.setSelected(false);
    _classConstructor.setSelected(false);
  }

  /** Caches the settings in the global model */
  public void ok() {
    if (!classNameMeetsNamingConvention(_className.getText())) {
      _errorMessage.setForeground(Color.RED);
      _errorMessage.setText("Enter correct class name. ");
      invalidate();
      return;
    }
    
    if (_superClass.getText().length() != 0) {
      if(!classNameMeetsNamingConvention(_superClass.getText())) {
        _errorMessage.setForeground(Color.RED);
        _errorMessage.setText("Enter correct superclass.");
        invalidate();
        return;
      }
    }
    
    if(_interfaces.getText().length() != 0) {
      if(!interfacesNameMeetsNamingConvention(_interfaces.getText())) {
        _errorMessage.setForeground(Color.RED);
        _errorMessage.setText("Enter correct interface names.");
        invalidate();
        return;
      }
    }
    
    String classContent = createClassContent(_publicRadio.isSelected()?"public":"",
                                             _abstractCheck.isSelected()?"abstract":
                                               (_finalCheck.isSelected()?"final":""),
                                             _className.getText(),
                                             _mainMethod.isSelected(),
                                             _classConstructor.isSelected(),
                                             _superClass.getText(), _interfaces.getText());

    _model.newFile(classContent);
    
    setVisible(false);
    reset();
  }

  private void _setupPanel(JPanel panel) {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);

    // Class Name
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    JLabel prLabel = new JLabel("Class Name");
    prLabel.setToolTipText("<html>The name of the class.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_className, c);
    panel.add(_className);

    // Superclass
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("Superclass");
    prLabel.setToolTipText("<html>The name of the superclass, or empty for Object.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_superClass, c);
    panel.add(_superClass);

    // Interfaces
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("Interfaces");
    prLabel.setToolTipText("<html>The name of the interfaces, separated by commas, or empty for none.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_interfaces, c);
    panel.add(_interfaces);
    
    // Modifiers: public/default
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("Access modifier");
    prLabel.setToolTipText("<html>Class access modifier.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel accessPanel = new JPanel();
    accessPanel.setLayout(new BoxLayout(accessPanel, BoxLayout.X_AXIS));
    accessPanel.add(_publicRadio);
    accessPanel.add(_defaultRadio);
    gridbag.setConstraints(accessPanel, c);
    panel.add(accessPanel);

    // Modifiers: abstract/final
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("Other modifiers");
    prLabel.setToolTipText("<html>final or abstract class modifiers.</html>");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    JPanel faPanel = new JPanel();
    faPanel.setLayout(new BoxLayout(faPanel, BoxLayout.X_AXIS));
    faPanel.add(_abstractCheck);
    faPanel.add(_finalCheck);
    gridbag.setConstraints(faPanel, c);
    panel.add(faPanel);
    _finalCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        if (_finalCheck.isSelected()) _abstractCheck.setSelected(false);
      }
    });
    _abstractCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        if (_abstractCheck.isSelected()) _finalCheck.setSelected(false);
      }
    });

    // Include ctor
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_classConstructor, c);
    panel.add(_classConstructor);

    // Include ctor
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;

    prLabel = new JLabel("");
    gridbag.setConstraints(prLabel, c);

    panel.add(prLabel);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_mainMethod, c);
    panel.add(_mainMethod);

    // Error message
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    gridbag.setConstraints(_errorMessage, c);
    panel.add(_errorMessage);
    
    //grouping the modifiers
    _group1.add(_publicRadio);
    _group1.add(_defaultRadio);    
  }
  
  //newclass addition
  static String capitalizeClassName(String name){
    if (name.length() == 0) return "";
    return name.trim().substring(0,1).toUpperCase() + name.trim().substring(1);
  }
  
  //newclass addition
  static boolean classNameMeetsNamingConvention(String name){
    if(name != null && name.length() != 0 && name.trim().matches("([A-Za-z_][A-Za-z0-9_]*)"))
      return true;
    else
      return false;
  }
  
  static boolean iterateListOfClassNames(String name) {
    String[] separatedNames = name.split(",");
    for(int i = 0; i < separatedNames.length; i++) {
      if (!classNameMeetsNamingConvention(separatedNames[i].trim())) return false;
    }
    return true;
  }
  
  static boolean interfacesNameMeetsNamingConvention(String name){
    if(name != null && name.length() != 0 && name.matches("([A-Za-z_][A-Za-z0-9_, ]*)")) {
      return iterateListOfClassNames(name);
    } else
      return false;
  }
  
  static String capitalizeInterfacesNames(String name) {
    String[] separatedNames = name.split(",");
    StringBuilder correctNames = new StringBuilder();
    for(int i = 0; i < separatedNames.length; ++i) {
      correctNames.append(capitalizeClassName(separatedNames[i].trim()));
      if (i < separatedNames.length-1) correctNames.append(", ");
    }
    return correctNames.toString();
  }
  
  //newclass addition   
  static String getModifier(String modifier){
    if(modifier == null)
      return ""; //return blank modifier
    else if(modifier.equals("public") || modifier.equals("final") || modifier.equals("abstract"))
      return modifier+ " ";
    else
      return ""; //return blank modifier
  }
  
  //newclass addition
  static String createClassNameDecleration(String accessMod, String modifier, String name, 
                                           String superclass, String interfaces) {
    StringBuilder sb = new StringBuilder();
    sb.append(getModifier(accessMod));
    sb.append(getModifier(modifier));
    sb.append("class");
    if(name != null) {
      sb.append(' ');
      sb.append(capitalizeClassName(name)); 
    }
    if(superclass.length() != 0) {
      sb.append(" extends ");
      sb.append(capitalizeClassName(superclass));
    }
    if(interfaces.length() != 0) {
      sb.append(" implements ");
      sb.append(capitalizeInterfacesNames(interfaces));
    }
    return sb.toString();
  }
  
  public static String createClassContent(String accessMod, String modifier, String className, 
                                          boolean mainMethod, boolean classConstructor, String inheritance, 
                                          String interfaces){
    StringBuilder sb = new StringBuilder();
    
    sb.append("/**\n");
    sb.append("* Auto Generated Java Class.\n");
    sb.append("*/\n");
    sb.append(createClassNameDecleration(accessMod, modifier, className, inheritance, interfaces));
    sb.append(" {\n");
    sb.append("\n");
    
    if(classConstructor) {
      sb.append("public " + capitalizeClassName(className) + "() { \n");
      sb.append("/* YOUR CONSTRUCTOR CODE HERE*/");
      sb.append("\n}\n");
    }
    
    if(mainMethod) {
      sb.append("\n public static void main(String[] args) { \n\n");
      sb.append("}\n\n");
    }
    
    sb.append("/* ADD YOUR CODE HERE */\n");
    sb.append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
