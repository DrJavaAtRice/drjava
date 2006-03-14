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
import java.awt.*;

import java.io.Serializable;
import java.util.ArrayList;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.Lambda;

/** The graphical form of an Option. Provides a way to see the values of Option
 *  while running DrJava and perform live updating of Options.
 *  @version $Id$
 */
public abstract class OptionComponent<T> implements Serializable {
  protected Option<T> _option;
  protected JLabel _label;
  protected Frame _parent;
    
  public OptionComponent(Option<T> option, String labelText, Frame parent) {
    _option = option;
    _label = new JLabel(labelText);
    _label.setHorizontalAlignment(JLabel.RIGHT);
    _parent = parent;
    if (option != null) {
      DrJava.getConfig().addOptionListener(option, new OptionListener<T>() {
        public void optionChanged(OptionEvent<T> oe) {
          resetToCurrent();
        }
      });
    }
  }
  
  /** Special constructor for degenerate option components does not take an option.
   *  @param labelText Text for descriptive label of this option.
   *  @param parent The parent frame.
   */
  public OptionComponent (String labelText, Frame parent) { this(null, labelText, parent); }
  
  public Option<T> getOption() { return _option; }
  
  public String getLabelText() { return _label.getText(); } 
  
  public JLabel getLabel() { return _label; } 
  
  /** Returns the JComponent to display for this OptionComponent. */
  public abstract JComponent getComponent();

  /** Sets the detailed description text for all components in this OptionComponent.
   *  Should be called by subclasses that wish to display a description.
   *  @param description the description of the component
   */
  public abstract void setDescription(String description);

  /**
   * Updates the appropriate configuration option with the new value 
   * if different from the old value and legal. Any changes should be 
   * done immediately such that current and future references to the Option 
   * should reflect the changes.
   * @return false, if value is invalid; otherwise true.
   */ 
  public abstract boolean updateConfig();

  /** Resets the entry field to reflect the actual stored value for the option. */
  public void resetToCurrent() {
    if (_option != null) {
      setValue(DrJava.getConfig().getSetting(_option));
    }
  }
  
  /**
   * Resets the actual value of the component to the original default.
   */
  public void resetToDefault() {
    if (_option != null) {
      setValue(_option.getDefault());
      notifyChangeListeners();
    }
  }
  
  /** Sets the value that is currently displayed by this component. */
  public abstract void setValue(T value);
  
  public void showErrorMessage(String title, OptionParseException e) {
    showErrorMessage(title, e.value, e.message);
  }
  
  public void showErrorMessage(String title, String value, String message) {
    JOptionPane.showMessageDialog(_parent,
                                  "There was an error in one of the options that you entered.\n" +
                                  "Option: '" + getLabelText() + "'\n" +
                                  "Your value: '" + value + "'\n" +
                                  "Error: "+ message,
                                  title,
                                  JOptionPane.WARNING_MESSAGE);
  }
  
  /**
   * Interface for change listener.
   */
  public static interface ChangeListener extends Lambda<Object, Object> {
    public abstract Object apply(Object c);
  }
  
  /**
   * Adds a change listener to this component.
   * @param listener listener to add
   */
  public void addChangeListener(ChangeListener listener) {
    _changeListeners.add(listener);
  }
  
  /**
   * Removes a change listener to this component.
   * @param listener listener to remove
   */
  public void removeChangeListener(ChangeListener listener) {
    _changeListeners.remove(listener);
  }
  
  /**
   * Notify all change listeners of a change.
   */
  protected void notifyChangeListeners() {
    for(ChangeListener l: _changeListeners) {
      l.apply(this);
    }
  }
  
  /**
   * List of change listeners.
   */
  private ArrayList<ChangeListener> _changeListeners = new ArrayList<ChangeListener>();
}
                                      
  
