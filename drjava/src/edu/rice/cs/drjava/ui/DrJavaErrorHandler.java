/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.JButton;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JFrame;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.plt.swing.SwingUtil;
import edu.rice.cs.util.Log;

/** The handle() method in this class is called everytime an uncaught exception propagates to an AWT action.
 *  The static log() method can be used to put log entries into the error log but continue execution.
 *  This does not automatically update the "DrJava Errors" window when new errors occur. In the case of errors,
 *  we want to minimize the effects on the GUI. If we want to see an updated dialog, we can click on the
 *  "DrJava Errors" button again.
 *  @version $Id$
 */
public class DrJavaErrorHandler {
  /** the list of errors */
  private static ArrayList<Throwable> _errors = new ArrayList<Throwable>();
  
  /** Log to file. */
  public static final Log LOG = new Log("error_handler.txt", false);

  /** the button to show */
  private static JButton _errorsButton;
  
  /** Sets the button to show. */
  public static void setButton(JButton b) { _errorsButton = b; }  
  
  /** Gets the button to show. */
  public static JButton getButton() { return _errorsButton; }  
  
  /** Returns the size of the error list. */
  public static int getErrorCount() { return _errors.size(); }
  
  /** Returns the error with the given index. */
  public static Throwable getError(int index) {
    if ((index>=0) && (index<_errors.size())) {
      return _errors.get(index);
    }
    else {
      return new UnexpectedException("Error in DrJavaErrorHandler");
    }
  }
  
  /** Clears the list of errors. */
  public static void clearErrors() { _errors.clear(); }

  /** Handles an uncaught exception. This gets called automatically by AWT. */
  public void handle(Throwable thrown) {
    System.out.println("Unhandled exception: " + thrown);
    record(thrown);
  }
  
  /** Record the throwable in the errors list. */
  public static void record(final Throwable thrown) {
    LOG.log("DrJavaErrorHandler.record", thrown);
    SwingUtil.invokeLater(new Runnable() {
      public void run() {
        if (thrown instanceof OutOfMemoryError) {
          // if this is an OutOfMemoryError inside DrJava, try to suggest to increase Main JVM's max heap
          Runtime.getRuntime().gc();
          JFrame f = DrJavaErrorWindow.getFrame();
          if (f instanceof MainFrame) {
            MainFrame mf = (MainFrame)f;
            mf.askToIncreaseMasterMaxHeap();
          }
        }
        else if (thrown instanceof com.sun.jdi.VMOutOfMemoryException) {
          // if this is an VMOutOfMemoryException, suggest to increase Interaction JVM's max heap
          JFrame f = DrJavaErrorWindow.getFrame();
          if (f instanceof MainFrame) {
            MainFrame mf = (MainFrame)f;
            mf.askToIncreaseSlaveMaxHeap();
          }
        }
        _errors.add(thrown);
        if (_errorsButton != null) {
          _errorsButton.setVisible(true);
        }
        if (_errors.size() == 1 && DrJava.getConfig().getSetting(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED).booleanValue()) {
          DrJavaErrorPopup popup = new DrJavaErrorPopup(DrJavaErrorWindow.getFrame(), thrown);
          MainFrame.setPopupLoc(popup, popup.getOwner());
          popup.setVisible(true);
        }
      }
    });
  }
  
  /** Log an unexpected situation. */
  public static void log(String message) { record(new LoggedCondition(message)); }
  
  /** The throwable used for logging unexpected situations. */
  public static class LoggedCondition extends Throwable {
    public LoggedCondition(String s) { super(s); }
  }
}
