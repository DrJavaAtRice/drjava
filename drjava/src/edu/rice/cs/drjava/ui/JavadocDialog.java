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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.DirectorySelectorComponent;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.DirectorySelector;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.swing.Utilities;

import javax.swing.*;
import java.io.File;

/** Manages a dialog box that can select a destination directory for generating Javadoc.  The getDirectory method should
  * be called to show the dialog, using the suggested location for the Javadoc as the "start" file.  If the user 
  * modifies the selection once, the user's choice will be remembered and no further suggestions will be used.
  *
  * @version $Id: JavadocDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class JavadocDialog implements DirectorySelector {
  /** Parent frame of the dialog. */
  private final JFrame _frame;
  
  /** File field and button. */
  private final DirectorySelectorComponent _selector;
  
  /** Whether to always prompt for destination. */
  private final JCheckBox _checkBox;
  
  /** OptionPane from which to get the results. */
  private final JOptionPane _optionPane;
  
  /** Dialog to show. */
  private final JDialog _dialog;
  
  /** Whether to use the suggested directory each time the dialog is shown. */
  private boolean _useSuggestion;
  
  /** Current suggestion for the destination directory, or null. */
  private File _suggestedDir;
  
  /** Creates a new JavadocDialog to show from the given frame.
    * 
    * @param frame Parent frame of this dialog
    */
  public JavadocDialog(JFrame frame) {
    _frame = frame;
    _useSuggestion = true;
    _suggestedDir = null;
    
    // Create file chooser
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setApproveButtonText("Select");
//    chooser.setEditable(true);
    
    // Create components for dialog
    String msg = "Select a destination directory for the Javadoc files:";
    _selector = new DirectorySelectorComponent(_frame, chooser, DirectorySelectorComponent.DEFAULT_NUM_COLS, DirectorySelectorComponent.DEFAULT_FONT_SIZE, false);
    _checkBox = new JCheckBox("Always Prompt For Destination");
    Object[] components = new Object[] { msg, _selector, _checkBox };
    
    _optionPane = new JOptionPane(components,
                                  JOptionPane.QUESTION_MESSAGE,
                                  JOptionPane.OK_CANCEL_OPTION);
    _dialog = _optionPane.createDialog(_frame, "Select Javadoc Destination");
    chooser.setOwner(_dialog);
  }
  
  
  public boolean isRecursive() { return false; }
  
  /** Shows the dialog prompting the user for a destination directory in which to generate Javadoc.
    * 
    * This operation must be executed from the event-handling thread!
    *
    * @param start The directory to display in the text box.  If null,
    * the most recent suggested directory (passed in via setSuggestedDir)
    * is displayed, unless the user has modified a previous suggestion.
    * @return A directory to use for the Javadoc (which might not exist)
    * @throws OperationCanceledException if the selection request is canceled
    */
  public File getDirectory(File start) throws OperationCanceledException {
    if (start != null) {
      // We were given a default - use it.
      _selector.setFileField(start);
    }
    else if (_useSuggestion && (_suggestedDir != null)) {
      // We weren't given one, so we need to use our suggestion.
      _selector.setFileField(_suggestedDir);
    }
    
    Configuration config = DrJava.getConfig();
    boolean ask = config.getSetting(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION).booleanValue();
    
    if (ask) {
      // The "always prompt" checkbox should be checked
      _checkBox.setSelected(true);
      
      // Prompt the user
      Utilities.setPopupLoc(_dialog, _frame);
      _dialog.setVisible(true);
      
      // Get result
      if (!_isPositiveResult()) {
        throw new OperationCanceledException();
      }
      
      // See if the user wants to suppress this dialog in the future.
      if (!_checkBox.isSelected()) {
        config.setSetting(OptionConstants.JAVADOC_PROMPT_FOR_DESTINATION,
                          Boolean.FALSE);
      }
      
      // Check if the user disagreed with the suggestion
      if ((start == null) &&
          (_useSuggestion && (_suggestedDir != null)) &&
          !_selector.getFileFromField().equals(_suggestedDir)) {
        _useSuggestion = false;
      }
    }
    return _selector.getFileFromField();
  }
  
  /** Asks the user a yes/no question.
    * @return true if the user responded affirmatively, false if negatively
    */
  public boolean askUser(String message, String title) {
    int choice = JOptionPane.showConfirmDialog(_frame, message, title, JOptionPane.YES_NO_OPTION);
    return (choice == JOptionPane.YES_OPTION);
  }
  
  /** Warns the user about an error condition. */
  public void warnUser(String message, String title) {
    JOptionPane.showMessageDialog(_frame, message, title, JOptionPane.ERROR_MESSAGE);
  }
  
  /** Sets the suggested destination directory for Javadoc generation. This directory will be displayed
    * in the file field if the user has not modified the suggestion in the past.
    * @param dir Suggested destination directory
    */
  public void setSuggestedDir(File dir) { _suggestedDir = dir; }
  
  /** Sets whether the dialog should use the suggested directory provided
    * to the getDirectory method as the default location.
    * @param use Whether to use the suggested directory
    */
  public void setUseSuggestion(boolean use) { _useSuggestion = use; }
  
  /** Returns whether the JOptionPane currently has the OK_OPTION result. */
  private boolean _isPositiveResult() {
    Object result = _optionPane.getValue();
    if ((result != null) && (result instanceof Integer)) {
      int rc = ((Integer)result).intValue();
      return rc == JOptionPane.OK_OPTION;
    }
    else return false;
  }
}