/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.text.SwingDocument;

import javax.swing.text.*;

/**
 * The panel which displays all the Javadoc parsing errors.
 *
 * @version $Id$
 */
public class JavadocErrorPanel extends ErrorPanel {

  protected JavadocErrorListPane _errorListPane;
  // TODO:  Is this field necessary?
//  private boolean _successful;

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public JavadocErrorPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Javadoc Output", "Javadoc");
//    _successful = true;
    _errorListPane = new JavadocErrorListPane();
    setErrorListPane(_errorListPane);
  }

  /**
   * Returns the JavadocErrorListPane that this panel manages.
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

  /**
   * Clean up when the tab is closed.
   */
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

  /**
   * A pane to show Javadoc errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class JavadocErrorListPane extends ErrorPanel.ErrorListPane {
//    private final JLabel _errorLabel = new JLabel();
//    private final JLabel _testLabel = new JLabel();
//    private final JLabel _fileLabel = new JLabel();

    /** Puts the error pane into "compilation in progress" state. */
    public void setJavadocInProgress() {
      _errorListPositions = new Position[0];

      SwingDocument doc = new SwingDocument();
      doc.append("Generating Javadoc.  Please wait...\n", NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
    }

    /** Used to show that the last javadoc command was unsuccessful. */
    protected void _updateWithErrors() throws BadLocationException {
      SwingDocument doc = new SwingDocument();
      String failureName = "error";
      if (getErrorModel().hasOnlyWarnings()) failureName = "warning";
      _updateWithErrors(failureName, "found", doc);
    }

    /** Used to show that the last compile was successful. */
    protected void _updateNoErrors(boolean done) throws BadLocationException {
      SwingDocument doc = new SwingDocument();
      String msg = (done) ? "Javadoc generated successfully." : "";
      doc.append(msg, NORMAL_ATTRIBUTES);
      setDocument(doc);
      selectNothing();
    }

//     public JavadocError getError() {
//       return _error;
//     }

  }

}
