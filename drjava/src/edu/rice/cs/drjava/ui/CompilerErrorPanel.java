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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerModel;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Vector;

/** The panel which houses the list of errors after an unsuccessful compilation.  If the user clicks on the combobox,
  * it moves the definitions cursor to the error in the source.  If the cursor is moved onto a line with an error, it 
  * selects the appropriate error in the list but do not move the cursor.
  * @version $Id: CompilerErrorPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class CompilerErrorPanel extends ErrorPanel {
  
  /** Whether a compile has occurred since the last compiler change. */
  private volatile boolean _compileHasOccurred;
  private volatile CompilerErrorListPane _errorListPane;
  private final JComboBox<CompilerInterface> _compilerChoiceBox;
  
  /** The list of files from the last compilation unit that were not compiled because they were not source files. */
  private volatile File[] _excludedFiles = new File[0];
  
  /** Constructor.
   *  @param model SingleDisplayModel in which we are running
   *  @param frame MainFrame in which we are displayed
   */
  public CompilerErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Compiler Output", "Compiler");
    _compileHasOccurred = false;
    _numErrors = 0;
    
    _errorListPane = new CompilerErrorListPane();
    setErrorListPane(_errorListPane);
    
    
    /******** Initialize the drop-down compiler menu ********/
    // Limitation: Only compiler choices are those that were available
    // at the time this box was created.
    // Also: The UI will go out of sync with reality if the active compiler
    // is later changed somewhere else. This is because there is no way
    // to listen on the active compiler.
    final CompilerModel compilerModel = getModel().getCompilerModel();
    Iterable<CompilerInterface> iter = compilerModel.getAvailableCompilers();
    _compilerChoiceBox = new JComboBox<CompilerInterface>(IterUtil.toArray(iter, CompilerInterface.class));
    _compilerChoiceBox.setEditable(false);
    _compilerChoiceBox.setSelectedItem(compilerModel.getActiveCompiler());
    _compilerChoiceBox.setToolTipText("From file: " + compilerModel.getActiveCompiler().getDescription());
    _compilerChoiceBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          final CompilerInterface compiler = (CompilerInterface) _compilerChoiceBox.getSelectedItem();
          compilerModel.resetCompilerErrors();
          _compileHasOccurred = false;
          _compilerChoiceBox.setToolTipText(compiler.getDescription());
          // set the new compiler (and reset the interactions pane) in a separate thread
          // to address [ drjava-Bugs-2985291 ] Delay in GUI when selecting compiler
          new Thread(new Runnable() {
            public void run() {
              compilerModel.setActiveCompiler(compiler);
              reset();
            }
          }).start();
        }
      }
    });

    customPanel.add(_compilerChoiceBox, BorderLayout.NORTH);
    
    DrJava.getConfig().addOptionListener(OptionConstants.JAVAC_LOCATION, new CompilerLocationOptionListener<File>());
    DrJava.getConfig().addOptionListener(OptionConstants.EXTRA_COMPILERS, new CompilerLocationOptionListener<Vector<String>>());
  }
  
  
  /** The OptionListener for compiler LOCATIONs */
  private class CompilerLocationOptionListener<T> implements OptionListener<T> {
    
    public void optionChanged(OptionEvent<T> oce) {
      _compilerChoiceBox.removeAllItems();
      for (CompilerInterface c : getModel().getCompilerModel().getAvailableCompilers()) {
        _compilerChoiceBox.addItem(c);
      }
    }
  }
  
  /** Returns the CompilerErrorListPane that this panel manages. */
  public CompilerErrorListPane getErrorListPane() { return _errorListPane; }
  
  /** Called when compilation begins. */
  public void setCompilationInProgress() {
    _errorListPane.setCompilationInProgress();
  }
  
  public CompilerErrorModel getErrorModel() { return getModel().getCompilerModel().getCompilerErrorModel(); }
  
  /** Clean up when the tab is closed. */
  @Override
  protected void _close() {
    super._close();
    getModel().getCompilerModel().resetCompilerErrors();
    reset();
  }
  
  /** Reset the errors to the current error information immediately following compilation. */
  public void reset(File[] excludedFiles) {
    _excludedFiles = excludedFiles;
    reset();
  }
  
  /** Reset the errors to the current error information. */
  public void reset() {
    // _nextErrorButton.setEnabled(false);
    // _prevErrorButton.setEnabled(false);
//    Utilities.showDebug("Reset being called by CompilerErrorPanel");
    _numErrors = getModel().getCompilerModel().getNumErrors();
    
    _errorListPane.updateListPane(true);
    // _nextErrorButton.setEnabled(_errorListPane.hasNextError());
    // _prevErrorButton.setEnabled(_errorListPane.hasPrevError());
  }
  
  class CompilerErrorListPane extends ErrorPanel.ErrorListPane {
    
    protected void _updateWithErrors() throws BadLocationException {
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      CompilerModel compilerModel = getModel().getCompilerModel();
      if (_excludedFiles.length != 0) {
        final StringBuilder msgBuffer = 
          new StringBuilder("Compilation completed.  Output directory is: " + compilerModel.getBuildDir() + 
                            "\nThe following files were not compiled:\n");
        for (File f: _excludedFiles) {
          if (f != null) { msgBuffer.append("  ").append(f).append('\n'); } // do not print files from untitled docs
        }
        doc.append(msgBuffer.toString(), NORMAL_ATTRIBUTES);
      }

      String failureName = "error";
      if (getErrorModel().hasOnlyWarnings()) failureName = "warning";

      _updateWithErrors(failureName, "found", doc);
    }
    
    /** Puts the error pane into "compilation in progress" state. */
    public void setCompilationInProgress() {
      _errorListPositions = new Position[0];
      _compileHasOccurred = true;
      
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
       
      try { doc.insertString(0, "Compilation in progress, please wait...", NORMAL_ATTRIBUTES); }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      
      setDocument(doc);
      selectNothing();
    }
    
    /** Used to show that the last compile was successful.
     *  @param done ignored: we assume that this is only called after compilation is completed
     */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      CompilerModel compilerModel = getModel().getCompilerModel();
      StringBuilder msgBuffer;
      if (_compileHasOccurred) {
        msgBuffer = new StringBuilder("Compilation completed.  Output directory is: " + compilerModel.getBuildDir());
        if (_excludedFiles.length > 0) 
          msgBuffer.append("\nThe following files were not compiled:\n");
        for (File f: _excludedFiles) {
          if (f != null) { msgBuffer.append("  ").append(f).append('\n'); } // do not print files from untitled docs
        }
      }
      else if (! compilerModel.getActiveCompiler().isAvailable())
        msgBuffer = new StringBuilder("No compiler available.");
      else 
        msgBuffer = new StringBuilder("Compiler ready: " + compilerModel.getActiveCompiler().getDescription() + ".");
      
      doc.insertString(0, msgBuffer.toString(), NORMAL_ATTRIBUTES);
      setDocument(doc);
      _updateScrollButtons();
      selectNothing();
    }
    public String getErrorDocumentTitle() { return "Compiler Errors"; }
  }
}
