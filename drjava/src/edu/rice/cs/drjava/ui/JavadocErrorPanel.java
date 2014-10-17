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

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;

import javax.swing.text.*;

/**
 * The panel which displays all the Javadoc parsing errors.
 *
 * @version $Id: JavadocErrorPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class JavadocErrorPanel extends ErrorPanel {

  protected JavadocErrorListPane _errorListPane;

  /** Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public JavadocErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Javadoc Output", "Javadoc");
//    _successful = true;
    _errorListPane = new JavadocErrorListPane();
    setErrorListPane(_errorListPane);
  }

  /** Returns the JavadocErrorListPane that this panel manages.
   */
  public JavadocErrorListPane getErrorListPane() {
    return _errorListPane;
  }

  protected CompilerErrorModel getErrorModel() {
    return getModel().getJavadocModel().getJavadocErrorModel();
  }

  /** Called when work begins. */
  public void setJavadocInProgress() {
    _errorListPane.setJavadocInProgress();
  }

  /** Closes this panel and resets the corresponding model. */
  @Override
  protected void _close() {
    super._close();
    getModel().getJavadocModel().resetJavadocErrors();
    reset();
  }

  /** Reset the errors to the current error information. */
  public void reset() {
    CompilerErrorModel model = getModel().getJavadocModel().getJavadocErrorModel();
    if (model != null) _numErrors = model.getNumErrors();
    else _numErrors = 0;

    _errorListPane.updateListPane(true);
  }

  /** A pane to show Javadoc errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class JavadocErrorListPane extends ErrorPanel.ErrorListPane {
    protected boolean _wasSuccessful = false;
    
//    private final JLabel _errorLabel = new JLabel();
//    private final JLabel _testLabel = new JLabel();
//    private final JLabel _fileLabel = new JLabel();

    /** Puts the error pane into "compilation in progress" state. */
    public void setJavadocInProgress() {
      _errorListPositions = new Position[0];

      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      doc.append("Generating Javadoc.  Please wait...\n", NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
      _wasSuccessful = false;
    }
    
    public void setJavadocEnded(boolean success) { _wasSuccessful = success; }

    /** Used to show that the last javadoc command was unsuccessful. */
    protected void _updateWithErrors() throws BadLocationException {
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      String failureName = "error";
      if (getErrorModel().hasOnlyWarnings()) failureName = "warning";
      _updateWithErrors(failureName, "found", doc);
    }

    /** Used to show that the last compile was successful. */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      ErrorDocument doc = new ErrorDocument(getErrorDocumentTitle());
      String msg = "";
      if (done) {
        if (_wasSuccessful) msg = "Javadoc generated successfully.";
        else msg = "Javadoc generation failed.";
      }
      doc.append(msg, NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
    }

//     public JavadocError getError() {
//       return _error;
//     }
    public String getErrorDocumentTitle() { return "Javadoc Errors"; }
  }
}
