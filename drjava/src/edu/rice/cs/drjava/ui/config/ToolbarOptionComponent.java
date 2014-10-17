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

package edu.rice.cs.drjava.ui.config;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.SwingFrame;

/**
 * The special option component for the toolbar text and toolbar icon options.
 * Not a true OptionComponent, in that it actually represents and governs the
 * configuration of two BooleanOptions (i.e. those corresponding to TOOLBAR_TEXT_ENABLED
 * and TOOLBAR_ICONS_ENABLED) bypassing the the normal graphical representation
 * with JRadioButtons, in order to comply with the special circumstances regarding
 * their setting.
 * @version $Id: ToolbarOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ToolbarOptionComponent extends OptionComponent<Boolean,JComponent> {

  private JRadioButton _noneButton;
  private JRadioButton _textButton;
  private JRadioButton _iconsButton;
  private JRadioButton _textAndIconsButton;
  private ButtonGroup _group;
  private JPanel _buttonPanel;

  //String constants used to identify which one of the four choices is selected.
  public static final String NONE = "none";
  public static final String TEXT_ONLY = "text only";
  public static final String ICONS_ONLY= "icons only";
  public static final String TEXT_AND_ICONS = "text and icons";

  /** The constructor does not take an option since we have specific knowledge of the
   * two options we'll need for this component. We simpy access them as needed, and use
   * OptionComponent's degenerate constructor.
   * @param title the title for this panel
   * @param parent the parent frame
   */
  public ToolbarOptionComponent(String title, SwingFrame parent) {
    super(title, parent);

    _noneButton = new JRadioButton(NONE);
    _noneButton.setActionCommand(NONE);
    _noneButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });
    
    _textButton = new JRadioButton(TEXT_ONLY);
    _textButton.setActionCommand(TEXT_ONLY);
    _textButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });

    _iconsButton = new JRadioButton(ICONS_ONLY);
    _iconsButton.setActionCommand(ICONS_ONLY);
    _iconsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });

    _textAndIconsButton = new JRadioButton(TEXT_AND_ICONS);
    _textAndIconsButton.setActionCommand(TEXT_AND_ICONS);
    _textAndIconsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });

    resetToCurrent();

    _group = new ButtonGroup();
    _group.add(_noneButton);
    _group.add(_textButton);
    _group.add(_iconsButton);
    _group.add(_textAndIconsButton);

    _buttonPanel = new JPanel();
    _buttonPanel.setLayout(new GridLayout(0,1));
    _buttonPanel.setBorder(BorderFactory.createEtchedBorder());
    _buttonPanel.add(_noneButton);
    _buttonPanel.add(_textButton);
    _buttonPanel.add(_iconsButton);
    _buttonPanel.add(_textAndIconsButton);

    DrJava.getConfig().addOptionListener(OptionConstants.TOOLBAR_TEXT_ENABLED,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oe) { resetToCurrent(); }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.TOOLBAR_ICONS_ENABLED,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oe) { resetToCurrent(); }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.TOOLBAR_ENABLED,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oe) { resetToCurrent(); }
    });
      
    setComponent(_buttonPanel);
  }

  /** Constructor that allows for a tooltip description. */
  public ToolbarOptionComponent(String title, SwingFrame parent, String description) {
    this(title, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _buttonPanel.setToolTipText(description);
    _noneButton.setToolTipText(description);
    _textButton.setToolTipText(description);
    _iconsButton.setToolTipText(description);
    _textAndIconsButton.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Selects the radio button corresponding to the current config options. */
  public void resetToCurrent() {
    _setSelected(DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue(),
                 DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue(),
                 DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ENABLED).booleanValue());
  }

  /** Selects the radio button corresponding to the default values. */
  public void resetToDefault() {
    _setSelected(OptionConstants.TOOLBAR_TEXT_ENABLED.getDefault().booleanValue(),
                 OptionConstants.TOOLBAR_ICONS_ENABLED.getDefault().booleanValue(),
                 OptionConstants.TOOLBAR_ENABLED.getDefault().booleanValue());
  }

  /** Selects the radio button corresponding to the specified configuration.
    * @param textEnabled Whether toolbar text is enabled
    * @param iconsEnabled Whether toolbar icons are enabled
    */
  private void _setSelected(boolean textEnabled, boolean iconsEnabled, boolean isEnabled) {
    if (! isEnabled) { _noneButton.setSelected(true); }
    else if (textEnabled && iconsEnabled) { _textAndIconsButton.setSelected(true); }
    else {
      if (textEnabled) _textButton.setSelected(true);
      else if (iconsEnabled) _iconsButton.setSelected(true);
    }
  }

  /** Updates the config object with the new setting.  Should run in event thread.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    String btnIdent = _group.getSelection().getActionCommand();
    boolean textWasEnabled = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean iconsWereEnabled = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    boolean wasEnabled = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ENABLED).booleanValue();
    
    if (btnIdent.equals(NONE)) {
      if (wasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ENABLED, Boolean.FALSE); }
    }
    if (btnIdent.equals(TEXT_ONLY)) {
      if (! textWasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.TRUE); }
      if (iconsWereEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.FALSE); }
      if (! wasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ENABLED, Boolean.TRUE); }
    }

    if (btnIdent.equals(ICONS_ONLY)) {
      if (! iconsWereEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.TRUE); }
      if (textWasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.FALSE); }
      if (! wasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ENABLED, Boolean.TRUE); }
    }

    if (btnIdent.equals(TEXT_AND_ICONS)) {
      if (! textWasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.TRUE); }
      if (! iconsWereEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.TRUE); }
      if (! wasEnabled) { DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ENABLED, Boolean.TRUE); }
    }

    return true;
  }


  /** Displays the given value.
   */
  public void setValue(Boolean value) {
    resetToCurrent();
  }
  
  /** Set the JComponent to display for this OptionComponent.
    * @param component GUI component */
  public void setComponent(JComponent component) {
    _guiComponent = component;
    if (_guiComponent!=null) {
      boolean wasEditable = DrJava.getConfig().isEditable(OptionConstants.TOOLBAR_TEXT_ENABLED);
      wasEditable = wasEditable && DrJava.getConfig().isEditable(OptionConstants.TOOLBAR_ICONS_ENABLED);
      wasEditable = wasEditable && DrJava.getConfig().isEditable(OptionConstants.TOOLBAR_ENABLED);
      
      _guiComponent.setEnabled(wasEditable);
      // also enable/disable all subcomponents (see Java bug 4177727)
      for (Component subComponent: _guiComponent.getComponents()) {
        subComponent.setEnabled(wasEditable);
      }
    }
  }
}