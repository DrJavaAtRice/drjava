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
import edu.rice.cs.util.swing.BorderlessScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

  JButton nextErrorButton = new JButton(MainFrame.getIcon("Down16.gif"));//new JButton("Next Error");
  JButton prevErrorButton = new JButton(MainFrame.getIcon("Up16.gif"));//new JButton("Prev Error");

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

    nextErrorButton.setMargin(new Insets(0,0,0,0));
    //nextErrorButton.setBorder(BorderFactory.createEmptyBorder());
    nextErrorButton.setToolTipText("Go to the next error");
    prevErrorButton.setMargin(new Insets(0,0,0,0));
    //prevErrorButton.setBorder(BorderFactory.createEmptyBorder());
    prevErrorButton.setToolTipText("Go to the previous error");
//    _showHighlightsCheckBox.setText("Highlight");

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
    _mainPanel.setMinimumSize(new Dimension(225,60));

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
//    uiBox.add(_compilerChoiceBox,BorderLayout.NORTH);

    JPanel midPanel = new JPanel();
 //nextErrorButton.setEnabled(false);
    nextErrorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _errorListPane.nextError();
//  prevErrorButton.setEnabled(_errorListPane.hasPrevError());
//  nextErrorButton.setEnabled(_errorListPane.hasNextError());
      }
    });
 //prevErrorButton.setEnabled(false);
    prevErrorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _errorListPane.prevError();
//  prevErrorButton.setEnabled(_errorListPane.hasPrevError());
//  nextErrorButton.setEnabled(_errorListPane.hasNextError());
      }
    });
    midPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));  // 3 pix padding on sides
    midPanel.setPreferredSize(new Dimension(170,70));
//    midPanel.add(errorPanel);
    midPanel.add(_compilerChoiceBox);
    midPanel.add(_showHighlightsCheckBox);
    uiBox.add(midPanel,BorderLayout.EAST);

    
    JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3)); 
    errorPanel.setPreferredSize(new Dimension(27,35));
    errorPanel.add(prevErrorButton);
//    errorLabel.setMargin(new Insets(3,0,3,0));
//    errorLabel.setBorder(BorderFactory.createEtchedBorder());
    errorPanel.add(nextErrorButton);
//    errorPanel.setBorder(BorderFactory.createEtchedBorder());
    uiBox.add(errorPanel, BorderLayout.WEST);

    _mainPanel.add(scroller, BorderLayout.CENTER);
    _mainPanel.add(compilerPanel, BorderLayout.EAST);
    DrJava.getConfig().addOptionListener(OptionConstants.JAVAC_LOCATION, new CompilerLocationOptionListener<File>());
    DrJava.getConfig().addOptionListener(OptionConstants.JSR14_LOCATION, new CompilerLocationOptionListener<File>());
    DrJava.getConfig().addOptionListener(OptionConstants.EXTRA_COMPILERS, new CompilerLocationOptionListener<Vector<String>>());
    //uiBox.add(_showHighlightsCheckBox, BorderLayout.SOUTH);
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
// nextErrorButton.setEnabled(false);
// prevErrorButton.setEnabled(false);
    _numErrors = getModel().getCompilerModel().getNumErrors();

    _errorListPane.updateListPane(true);
// nextErrorButton.setEnabled(_errorListPane.hasNextError());
// prevErrorButton.setEnabled(_errorListPane.hasPrevError());
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
