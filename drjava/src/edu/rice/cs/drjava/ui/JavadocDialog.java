/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.swing.FileSelectorComponent;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DirectorySelector;
import edu.rice.cs.drjava.model.OperationCanceledException;

import javax.swing.*;
import java.io.File;

/**
 * Manages a dialog box that can select a destination directory for generating
 * Javadoc.  The getDirectory method should be called to show the dialog,
 * using the suggested location for the Javadoc as the "start" file.  If the
 * user modifies the selection once, the user's choice will be remembered and
 * no further suggestions will be used.
 * 
 * @version $Id$
 */
public class JavadocDialog implements DirectorySelector {
  /** Parent frame of the dialog. */
  private final JFrame _frame;
  
  /** File field and button. */
  private final FileSelectorComponent _selector;
  
  /** OptionPane from which to get the results. */
  private final JOptionPane _optionPane;
  
  /** Dialog to show. */
  private final JDialog _dialog;
  
  /** Whether to use the suggested directory each time the dialog is shown. */
  private boolean _useSuggestion;
  
  /** Current suggestion for the destination directory, or null. */
  private File _suggestedDir;
  
  /**
   * Creates a new JavadocDialog to show from the given frame.
   * @param frame Parent frame of this dialog
   */
  public JavadocDialog(JFrame frame) {
    _frame = frame;
    _useSuggestion = true;
    _suggestedDir = null;
    
    // Create file chooser
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    // We use FILES_AND_DIRECTORIES because it behaves better than
    //  DIRECTORIES_ONLY.  (The latter will return the parent of a
    //  pre-selected directory unless the user chooses something else first.)
    //  I hate JFileChooser.
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setApproveButtonText("Select");
    chooser.setFileFilter(new DirectoryFilter());
    
    // Create components for dialog
    String msg = "Select a destination directory for the Javadoc files:";
    _selector = new FileSelectorComponent(_frame, chooser);
    Object[] components = new Object[] { msg, _selector };
    
    _optionPane = new JOptionPane(components,
                                  JOptionPane.QUESTION_MESSAGE,
                                  JOptionPane.OK_CANCEL_OPTION);
    _dialog = _optionPane.createDialog(_frame, "Select Javadoc Destination");
  }
  
  /**
   * Shows the dialog prompting the user for a destination directory
   * in which to generate Javadoc.
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
      // Prompt the user
      _dialog.show();
    
      // Get result
      if (!_isPositiveResult()) {
        throw new OperationCanceledException();
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
  
  /**
   * Asks the user a yes/no question.
   * @return true if the user responded affirmatively, false if negatively
   */
  public boolean askUser(String message, String title) {
    int choice = JOptionPane.showConfirmDialog(_frame, message, title,
                                               JOptionPane.YES_NO_OPTION);
    return (choice == JOptionPane.YES_OPTION);
  }
  
  /**
   * Warns the user about an error condition.
   */
  public void warnUser(String message, String title) {
    JOptionPane.showMessageDialog(_frame, message, title,
                                  JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Sets the suggested destination directory for Javadoc generation.
   * This directory will be displayed in the file field if the user
   * has not modified the suggestion in the past.
   * @param dir Suggested destination directory
   */
  public void setSuggestedDir(File dir) {
    _suggestedDir = dir;
  }
  
  /**
   * Sets whether the dialog should use the suggested directory provided
   * to the getDirectory method as the default location.
   * @param use Whether to use the suggested directory
   */
  public void setUseSuggestion(boolean use) {
    _useSuggestion = use;
  }
  
  /**
   * Returns whether the JOptionPane currently has the OK_OPTION result.
   */
  private boolean _isPositiveResult() {
    Object result = _optionPane.getValue();
    if ((result != null) && (result instanceof Integer)) {
      int rc = ((Integer)result).intValue();
      return rc == JOptionPane.OK_OPTION;
    }
    else {
      return false;
    }
  }
}