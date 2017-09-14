/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;

import javax.swing.text.*;

/**
 * The panel which displays all the Scaladoc parsing errors.
 *
 * @version $Id: ScaladocErrorPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ScaladocErrorPanel extends ErrorPanel {

  protected ScaladocErrorListPane _errorListPane;

  /** Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public ScaladocErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Scaladoc Output", "Scaladoc");
//    _successful = true;
    _errorListPane = new ScaladocErrorListPane();
    setErrorListPane(_errorListPane);
  }

  /** Returns the ScaladocErrorListPane that this panel manages.
   */
  public ScaladocErrorListPane getErrorListPane() {
    return _errorListPane;
  }

  protected CompilerErrorModel getErrorModel() {
    return getModel().getScaladocModel().getScaladocErrorModel();
  }

  /** Called when work begins. */
  public void setScaladocInProgress() {
    _errorListPane.setScaladocInProgress();
  }

  /** Closes this panel and resets the corresponding model. */
  @Override
  protected void _close() {
    super._close();
    getModel().getScaladocModel().resetScaladocErrors();
    reset();
  }

  /** Reset the errors to the current error information. */
  public void reset() {
    CompilerErrorModel model = getModel().getScaladocModel().getScaladocErrorModel();
    if (model != null) _numErrors = model.getNumErrors();
    else _numErrors = 0;

    _errorListPane.updateListPane(true);
  }

  /** A pane to show Scaladoc errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class ScaladocErrorListPane extends ErrorPanel.ErrorListPane {
    protected boolean _wasSuccessful = false;
    
//    private final JLabel _errorLabel = new JLabel();
//    private final JLabel _testLabel = new JLabel();
//    private final JLabel _fileLabel = new JLabel();

    /** Puts the error pane into "compilation in progress" state. */
    public void setScaladocInProgress() {
      _errorListPositions = new Position[0];

      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      doc.append("Generating Scaladoc.  Please wait...\n", NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
      _wasSuccessful = false;
    }
    
    public void setScaladocEnded(boolean success) { _wasSuccessful = success; }

    /** Used to show that the last scaladoc command was unsuccessful. */
    protected void _updateWithErrors() throws BadLocationException {
      _updateWithErrors(new ErrorDocument(getErrorDocumentTitle()));
    }
    
    protected void _updateWithErrors(ErrorDocument doc) throws BadLocationException {
      String failureName = "error";
      if (getErrorModel().hasOnlyWarnings()) failureName = "warning";
      _updateWithErrors(failureName, "found", doc);
    }
    
    /** TODO: streamline this code.*/
    protected void _updateWithErrors(String failureName, String failureMeaning, ErrorDocument doc)
      throws BadLocationException {
      // Print how many errors
      String numErrsMsg = _getNumErrorsMessage(failureName, failureMeaning);
      doc.append(numErrsMsg, BOLD_ATTRIBUTES);
      
      _insertErrors(doc);
      setDocument(doc);
      
      // Select the first error if there are some errors (i.e. does not select if there are only warnings)
      if (!getErrorModel().hasOnlyWarnings()) getErrorListPane().switchToError(0);
    }
    
    /** TODO: streamline this code. */
    /** Gets the message indicating the number of errors and warnings.*/
    protected String _getNumErrorsMessage(String failureName, String failureMeaning) {
      StringBuilder numErrMsg;
      
      /** Used for display purposes only */
      int numCompErrs = getErrorModel().getNumCompilerErrors();
      int numWarnings = getErrorModel().getNumWarnings();     
      
      if (!getErrorModel().hasOnlyWarnings()) {
        //failureName = error or test (for compilation and JUnit testing respectively)
        numErrMsg = new StringBuilder(numCompErrs + " " + failureName);   
        if (numCompErrs > 1) numErrMsg.append("s");
        if (numWarnings > 0) numErrMsg.append(" and " + numWarnings + " warning");          
      }
      
      else numErrMsg = new StringBuilder(numWarnings + " warning"); 
      
      if (numWarnings > 1) numErrMsg.append("s");
     
      numErrMsg.append(" " + failureMeaning + ":\n");
      return numErrMsg.toString();
    }
    
/** Gets the message indicating the number of errors and warnings.*/
    protected String _getNumErrorsMessage() { return _getNumErrorsMessage("error", "found"); }

    /** Used to show that the last compile was successful. */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      String msg = "";
      if (done) {
        if (_wasSuccessful) msg = "Scaladoc generated successfully.";
        else msg = "Scaladoc generation failed.";
      }
      doc.append(msg, NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
    }

//     public ScaladocError getError() {
//       return _error;
//     }
    public String getErrorDocumentTitle() { return "Scaladoc Errors"; }
  }
}
