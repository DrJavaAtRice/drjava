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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;

/**
 * The special option component for the toolbar text and toolbar icon options.
 * Not a true OptionComponent, in that it actually represents and governs the
 * configuration of two BooleanOptions (i.e. those corresponding to TOOLBAR_TEXT_ENABLED
 * and TOOLBAR_ICONS_ENABLED) bypassing the the normal graphical representation
 * with JRadioButtons, in order to comply with the special circumstances regarding
 * their setting.
 * @version $Id$
 */
public class ToolbarOptionComponent extends OptionComponent<Boolean> {

  private JRadioButton _textButton;
  private JRadioButton _iconsButton;
  private JRadioButton _textAndIconsButton;
  private ButtonGroup _group;
  private JPanel _buttonPanel;

  //String constants used to identify which one of the three choices is selected.
  public static final String TEXT_ONLY = "text only";
  public static final String ICONS_ONLY= "icons only";
  public static final String TEXT_AND_ICONS = "text and icons";

  /**
   * The constructor does not take an option since we have specific knowledge of the
   * two options we'll need for this component. We simpy access them as needed, and use
   * OptionComponent's degenerate constructor.
   * @param title the title for this panel
   * @param parent the parent frame
   */
  public ToolbarOptionComponent(String title, Frame parent) {
    super(title, parent);

    _textButton = new JRadioButton(TEXT_ONLY);
    _textButton.setActionCommand(TEXT_ONLY);

    _iconsButton = new JRadioButton(ICONS_ONLY);
    _iconsButton.setActionCommand(ICONS_ONLY);

    _textAndIconsButton = new JRadioButton(TEXT_AND_ICONS);
    _textAndIconsButton.setActionCommand(TEXT_AND_ICONS);

    resetToCurrent();

    _group = new ButtonGroup();
    _group.add(_textButton);
    _group.add(_iconsButton);
    _group.add(_textAndIconsButton);

    _buttonPanel = new JPanel();
    _buttonPanel.setLayout(new GridLayout(0,1));
    _buttonPanel.setBorder(BorderFactory.createEtchedBorder());
    _buttonPanel.add(_textButton);
    _buttonPanel.add(_iconsButton);
    _buttonPanel.add(_textAndIconsButton);

    DrJava.getConfig().addOptionListener(OptionConstants.TOOLBAR_TEXT_ENABLED,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oe) {
        resetToCurrent();
      }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.TOOLBAR_ICONS_ENABLED,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oe) {
        resetToCurrent();
      }
    });
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public ToolbarOptionComponent(String title, Frame parent, String description) {
    this(title, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _buttonPanel.setToolTipText(description);
    _textButton.setToolTipText(description);
    _iconsButton.setToolTipText(description);
    _textAndIconsButton.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Selects the radio button corresponding to the current config options.
   */
  public void resetToCurrent() {
    _setSelected(DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue(),
                 DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue());
  }

  /**
   * Selects the radio button corresponding to the default values.
   */
  public void resetToDefault() {
    _setSelected(OptionConstants.TOOLBAR_TEXT_ENABLED.getDefault().booleanValue(),
                 OptionConstants.TOOLBAR_ICONS_ENABLED.getDefault().booleanValue());
  }

  /**
   * Selects the radio button corresponding to the specified configuration.
   * @param textEnabled Whether toolbar text is enabled
   * @param iconsEnabled Whether toolbar icons are enabled
   */
  private void _setSelected(boolean textEnabled, boolean iconsEnabled) {
    if (textEnabled && iconsEnabled) {
      _textAndIconsButton.setSelected(true);
    }
    else {
      if (textEnabled) _textButton.setSelected(true);
      else if (iconsEnabled) _iconsButton.setSelected(true);
    }
  }

  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _buttonPanel;
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    String btnIdent = _group.getSelection().getActionCommand();
    boolean textWasEnabled = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean iconsWereEnabled = DrJava.getConfig().getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();

    if (btnIdent == TEXT_ONLY) {
      if (!textWasEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.TRUE);
      if (iconsWereEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.FALSE);
    }

    if (btnIdent == ICONS_ONLY) {
      if (!iconsWereEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.TRUE);
      if (textWasEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.FALSE);
    }

    if (btnIdent == TEXT_AND_ICONS) {
      if (!textWasEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, Boolean.TRUE);
      if (!iconsWereEnabled) DrJava.getConfig().setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, Boolean.TRUE);
    }

    return true;
  }


  /**
   * Displays the given value.
   */
  public void setValue(Boolean value) {
    resetToCurrent();
  }

}