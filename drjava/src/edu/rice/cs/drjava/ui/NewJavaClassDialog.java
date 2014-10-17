package edu.rice.cs.drjava.ui;

/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputFrame;
import edu.rice.cs.drjava.ui.predictive.PredictiveInputModel;
import edu.rice.cs.drjava.model.DummyOpenDefDoc;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.Runnable3;
import edu.rice.cs.plt.lambda.LambdaUtil;

import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.drjava.ui.MainFrameStatics.*;
import static edu.rice.cs.drjava.ui.predictive.PredictiveInputModel.*;

/** Generates Java source from information entered in the "New Class" dialog.
  * @version $Id: NewJavaClassDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class NewJavaClassDialog extends SwingFrame {

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
  private final JButton _superClassButton = new JButton(new AbstractAction("...") {
    public void actionPerformed(ActionEvent e) {
      _autoComplete(_superClass);
    }
  });
  private final JRadioButton _defaultRadio = new JRadioButton("default", false);
  private final JRadioButton _publicRadio = new JRadioButton("public", true);
  private final JCheckBox _abstractCheck = new JCheckBox("abstract");
  private final JCheckBox _finalCheck = new JCheckBox("final");
  private final ButtonGroup _group1 = new ButtonGroup();
  private final JCheckBox _mainMethod = new JCheckBox("Include main method");
  private final JCheckBox _classConstructor = new JCheckBox("Include class constructor");
  private final JLabel _errorMessage = new JLabel("<html> </html>");
//  private final boolean _isElementaryOrFunctionalJava;
  
  private final AutoCompletePopup _autoCompletePopup;
  
  private final AbstractAction _autoCompleteAction = new AbstractAction("Autocomplete") {
    public void actionPerformed(ActionEvent evt) {
      _autoComplete((JTextComponent)evt.getSource());
    }
  };
  
  /** Constructs New Java Class frame and displays it. */
  public NewJavaClassDialog(MainFrame mf) {
    super("New Java Class");

    //  Utilities.show("NewJavaClass(" + mf + ")");
    // Java language levels disabled
//    int currentLL = DrJava.getConfig().getSetting(OptionConstants.LANGUAGE_LEVEL);
//    _isElementaryOrFunctionalJava = false;
//      (currentLL == OptionConstants.ELEMENTARY_LEVEL) ||
//      (currentLL == OptionConstants.FUNCTIONAL_JAVA_LEVEL);

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
    
    _autoCompletePopup = new AutoCompletePopup(_mainFrame);
    
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
    if (!checkClassName(_className.getText())) {
      _errorMessage.setForeground(Color.RED);
      _errorMessage.setText("Enter correct class name. ");
      invalidate();
      return;
    }
    
    if (_superClass.getText().length() != 0) {
      if(!checkSuperClassName(_superClass.getText())) {
        _errorMessage.setForeground(Color.RED);
        _errorMessage.setText("Enter correct superclass.");
        invalidate();
        return;
      }
    }
    
    if(_interfaces.getText().length() != 0) {
      if(!checkInterfaceNames(_interfaces.getText())) {
        _errorMessage.setForeground(Color.RED);
        _errorMessage.setText("Enter correct interface names.");
        invalidate();
        return;
      }
    }
    
    String classContent = getClassContent(_publicRadio.isSelected()?"public":"",
                                             _abstractCheck.isSelected()?"abstract":
                                               (_finalCheck.isSelected()?"final":""),
                                             _className.getText(),
                                             _mainMethod.isSelected(),
                                             _classConstructor.isSelected(),
                                             _superClass.getText(), _interfaces.getText(),
                                             /* _isElementaryOrFunctionalJava */ false);  // Java LL disabled

    _model.newFile(classContent);
    
    setVisible(false);
    reset();
  }

  private void _addAutoCompleteActions(JTextComponent component) {
    for(KeyStroke ks: DrJava.getConfig().getSetting(OptionConstants.KEY_COMPLETE_FILE)) {
      // If you want to bind a keystroke to shift-space (which generates
      // a space character), you need to use a pressed-type keystroke.
      component.getInputMap(JComponent.WHEN_FOCUSED).
        put(ks, _autoCompleteAction.getValue(Action.NAME));
    }

    component.getActionMap().put(_autoCompleteAction.getValue(Action.NAME), _autoCompleteAction);
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

    _className.setPreferredSize(new Dimension(250, _className.getPreferredSize().height));    
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
    c.gridwidth = 1;
    c.insets = new Insets(5, 5, 0, 0);

    gridbag.setConstraints(_superClass, c);
    _addAutoCompleteActions(_superClass);
    panel.add(_superClass);
    
    c.weightx = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(5, 0, 0, 10);
    _superClassButton.setToolTipText("<html>Select the superclass.</html>");
    gridbag.setConstraints(_superClassButton, c);
    panel.add(_superClassButton);

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
    
//    if (!_isElementaryOrFunctionalJava) {  // Java LL disabled
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
    
    // Include main method
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
//    }
    
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
  
  public void setEnabled(boolean b) {
    super.setEnabled(b);
    for(Component c: getComponents()) c.setEnabled(b);
  }
  
  //newclass addition
  static String getCapitalizedClassName(String name) {
    name = name.trim();
    if (name.length() == 0) return "";
    return name.substring(0,1).toUpperCase() + name.substring(1);
  }
  
  static boolean checkClassName(String name) {
    if (name == null) return false;
    name = name.trim();
    return (name.length() != 0 && name.matches("([A-Za-z_][A-Za-z0-9_]*)"));
  }
  
  static String getCapitalizedSuperClassName(String name) {
    name = name.trim();
    if (name.length() == 0) return "";
    int lastDotPos = name.lastIndexOf('.');
    if (lastDotPos == -1) return getCapitalizedClassName(name);
    return name.substring(0,lastDotPos+1)
      + getCapitalizedClassName(name.substring(lastDotPos+1));
  }

  static boolean checkSuperClassName(String name) {
    if (name == null) return false;
    name = name.trim();
    return (name.length() != 0 && name.matches("([A-Za-z_][A-Za-z0-9_\\.]*)"));
  }
  
  static boolean iterateListOfClassNames(String name) {
    String[] separatedNames = name.split(",",-1);
    for(String n: separatedNames) {
      if (!checkSuperClassName(n.trim())) return false;
    }
    return true;
  }
  
  static boolean checkInterfaceNames(String name) {
    if (name == null) return false;
    name = name.trim();
    if(name.length() != 0 && name.matches("([A-Za-z_][A-Za-z0-9_, \\.]*)")) {
      return iterateListOfClassNames(name);
    }
    else return false;
  }
  
  static String getCapitalizedInterfacesNames(String name) {
    String[] separatedNames = name.split(",",-1);
    StringBuilder correctNames = new StringBuilder();
    for(int i = 0; i < separatedNames.length; ++i) {
      correctNames.append(getCapitalizedSuperClassName(separatedNames[i].trim()));
      if (i < separatedNames.length-1) correctNames.append(", ");
    }
    return correctNames.toString();
  }
  
  static String getModifier(String modifier){
    if(modifier == null)
      return ""; //return blank modifier
    else if(modifier.equals("public") || modifier.equals("final") || modifier.equals("abstract"))
      return modifier+ " ";
    else
      return ""; //return blank modifier
  }
  
  static String getClassDeclaration(String accessMod, String modifier, String name, 
                                           String superclass, String interfaces,
                                           boolean elementaryOrFunctionalJava) {
    StringBuilder sb = new StringBuilder();
    if (!elementaryOrFunctionalJava) {
      sb.append(getModifier(accessMod));
      sb.append(getModifier(modifier));
    }
    sb.append("class");
    if(name != null) {
      sb.append(' ');
      sb.append(getCapitalizedClassName(name)); 
    }
    if(superclass.length() != 0) {
      sb.append(" extends ");
      sb.append(getCapitalizedSuperClassName(superclass));
    }
    if(interfaces.length() != 0) {
      sb.append(" implements ");
      sb.append(getCapitalizedInterfacesNames(interfaces));
    }
    return sb.toString();
  }
  
  public static String getClassContent(String accessMod, String modifier, String className, 
                                       boolean mainMethod, boolean classConstructor, String inheritance, 
                                       String interfaces,
                                       boolean elementaryOrFunctionalJava) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("/**\n");
    if (elementaryOrFunctionalJava) {
      sb.append("* Auto Generated Java Language Level Class.\n");
    }
    else {
      sb.append("* Auto Generated Java Class.\n");
    }
    sb.append("*/\n");
    sb.append(getClassDeclaration(accessMod, modifier, className, inheritance, interfaces,
                                  elementaryOrFunctionalJava));
    sb.append(" {\n");
    sb.append("\n");
    
    if(classConstructor && !elementaryOrFunctionalJava) {
      sb.append("public " + getCapitalizedClassName(className) + "() { \n");
      sb.append("/* YOUR CONSTRUCTOR CODE HERE*/");
      sb.append("\n}\n");
    }
    
    if(mainMethod && !elementaryOrFunctionalJava) {
      sb.append("\n public static void main(String[] args) { \n\n");
      sb.append("}\n\n");
    }
    
    sb.append("/* ADD YOUR CODE HERE */\n");
    sb.append("\n");
    sb.append("}\n");
    return sb.toString();
  }

  private void _autoComplete(final JTextComponent component) {
    _mainFrame.removeModalWindowAdapter(NewJavaClassDialog.this);
    setEnabled(false);
    final String initial = component.getText();
    final int loc = initial.length();
    _autoCompletePopup.show(this,
                            "Select Class",
                            initial,
                            loc,
                            new Runnable() {
      public void run() {
        // canceled
        NewJavaClassDialog.this.setEnabled(true);
        _mainFrame.installModalWindowAdapter(NewJavaClassDialog.this, LambdaUtil.NO_OP, CANCEL);
        NewJavaClassDialog.this.toFront();
      }
    },
                            new Runnable3<AutoCompletePopupEntry,Integer,Integer>() {
                              public void run(AutoCompletePopupEntry entry,
                                              Integer from,
                                              Integer to) {
                                // accepted
                                String fullName = entry.getFullPackage()+entry.getClassName();
                                component.setText(fullName);
                                component.setCaretPosition(fullName.length());
                                NewJavaClassDialog.this.setEnabled(true);
                                _mainFrame.installModalWindowAdapter(NewJavaClassDialog.this, LambdaUtil.NO_OP, CANCEL);
                                NewJavaClassDialog.this.toFront();
                              }
                            });
  }
}
