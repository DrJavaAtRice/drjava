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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.model.compiler.NoCompilerAvailable;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Vector;

/**
 * The panel which houses the list of errors after an unsuccessful compilation.
 * If the user clicks on the combobox, move the definitions cursor to the
 * error in the source.
 * If the cursor is moved onto a line with an error, select the appropriate
 * error in the list but do not move the cursor.
 *
 * @version $Id$
 */
public class CompilerErrorPanel extends ErrorPanel {

  /** Whether a compile has occurred since the last compiler change. */
  private boolean _compileHasOccurred;

  private CompilerErrorListPane _errorListPane;

  private final JComboBox _compilerChoiceBox;

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public CompilerErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Compiler Output");
    _compileHasOccurred = false;
    _numErrors = 0;

    _errorListPane = new CompilerErrorListPane();


    // Limitation: Only compiler choices are those that were available
    // at the time this box was created.
    // Also: The UI will go out of sync with reality if the active compiler
    // is later changed somewhere else. This is because there is no way
    // to listen on the active compiler.
    _compilerChoiceBox = 
      new JComboBox(getModel().getCompilerModel().getAvailableCompilers());
    _compilerChoiceBox.setEditable(false);
    _compilerChoiceBox.setSelectedItem
      (getModel().getCompilerModel().getActiveCompiler());
    _compilerChoiceBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        CompilerInterface compiler = (CompilerInterface)
          _compilerChoiceBox.getSelectedItem();
        if (compiler != null) {
          getModel().getCompilerModel().setActiveCompiler(compiler);
        }
        else {
          getModel().getCompilerModel()
            .setActiveCompiler(NoCompilerAvailable.ONLY);
        }
        getModel().getCompilerModel().resetCompilerErrors();
        _compileHasOccurred = false;
        reset();
      }
    });

    _mainPanel.setLayout(new BorderLayout());

    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the
    // text. Very bad.
    JScrollPane scroller =
      new BorderlessScrollPane(_errorListPane,
                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    JPanel compilerPanel = new JPanel(new BorderLayout());
    compilerPanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix padding on sides
    JPanel uiBox = new JPanel(new BorderLayout());
    uiBox.setBorder(new EmptyBorder(5,0,0,0)); // 5 pix padding on top
    compilerPanel.add(new JLabel("Compiler", SwingConstants.LEFT),
                      BorderLayout.NORTH);
       
    compilerPanel.add(uiBox,BorderLayout.CENTER);
    uiBox.add(_compilerChoiceBox,BorderLayout.NORTH);
    uiBox.add(new JPanel(),BorderLayout.CENTER);
    
    _mainPanel.add(scroller, BorderLayout.CENTER);
    _mainPanel.add(compilerPanel, BorderLayout.EAST);
    DrJava.getConfig().addOptionListener(OptionConstants.JAVAC_LOCATION, new CompilerLocationOptionListener<File>());
    DrJava.getConfig().addOptionListener(OptionConstants.JSR14_LOCATION, new CompilerLocationOptionListener<File>());
    DrJava.getConfig().addOptionListener(OptionConstants.EXTRA_COMPILERS, new CompilerLocationOptionListener<Vector<String>>());
    uiBox.add(_showHighlightsCheckBox, BorderLayout.SOUTH);
  }
  
  
  /**
   * The OptionListener for compiler LOCATIONs 
   */
  private class CompilerLocationOptionListener<T> implements OptionListener<T> {
    
    public void optionChanged(OptionEvent<T> oce) {
      _compilerChoiceBox.removeAllItems();
      CompilerInterface[] availCompilers = 
        getModel().getCompilerModel().getAvailableCompilers();
      for (int i=0; i<availCompilers.length; i++) {
        _compilerChoiceBox.addItem(availCompilers[i]);
      }
    }
  }
  
  /**
   * Returns the CompilerErrorListPane that this panel manages.
   */
  public CompilerErrorListPane getErrorListPane() {
    return _errorListPane;
  }
  
  /** Called when compilation begins. */
  public void setCompilationInProgress() {
    _errorListPane.setCompilationInProgress();
  }

  protected CompilerErrorModel<CompilerError> getErrorModel(){
    return getModel().getCompilerModel().getCompilerErrorModel();
  }

  /**
   * Clean up when the tab is closed.
   */
  protected void _close() {
    super._close();
    getModel().getCompilerModel().resetCompilerErrors();
    reset();
  }

  /**
   * Reset the errors to the current error information.
   */
  public void reset() {
    _numErrors = getModel().getCompilerModel().getNumErrors();

    _errorListPane.updateListPane(true);
  }

  class CompilerErrorListPane extends ErrorPanel.ErrorListPane {

    protected void _updateWithErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      String failureName = "error";
      if (getErrorModel().hasOnlyWarnings()) {
        failureName = "warning";
      }
      _updateWithErrors(failureName, "found", doc);
    }

    /** Puts the error pane into "compilation in progress" state. */
    public void setCompilationInProgress() {
      _errorListPositions = new Position[0];
      _compileHasOccurred = true;

      DefaultStyledDocument doc = new DefaultStyledDocument();

      try {
        doc.insertString(0,
                         "Compilation in progress, please wait...",
                         NORMAL_ATTRIBUTES);
      }
      catch (BadLocationException ble) {
        throw new UnexpectedException(ble);
      }

      setDocument(doc);

      selectNothing();
    }

    /**
     * Used to show that the last compile was successful.
     * @param done ignored: we assume that this is only called after compilation is
     * completed
     */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      String message;
      if (_compileHasOccurred) {
        message = "Last compilation completed successfully.";
      }
      else {
        if (getModel().getCompilerModel().getAvailableCompilers().length == 0) {
          message = "No compiler is available.  Please specify one in\n" +
                    "the Preferences dialog in the Edit menu.";
        }
        else {
          if (getModel().getCompilerModel().getActiveCompiler() == NoCompilerAvailable.ONLY) {
            message = "No compiler available.";
          }
          else {
            message = getModel().getCompilerModel().getActiveCompiler().getName() + " compiler ready.";
          }
        }
      }

      doc.insertString(0, message, NORMAL_ATTRIBUTES);
      setDocument(doc);

      selectNothing();
    }

  }

}
