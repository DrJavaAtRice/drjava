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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.JButton;
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.OptionConstants;

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
  public static void record(Throwable thrown) {
    _errors.add(thrown);
    if (_errorsButton!=null) {
      _errorsButton.setVisible(true);
    }
    if ((_errors.size()==1) && (DrJava.getConfig().getSetting(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED).booleanValue())) {
      DrJavaErrorPopup popup = new DrJavaErrorPopup(DrJavaErrorWindow.getFrame(), thrown);
      MainFrame.setPopupLoc(popup);
      popup.setVisible(true);
    }
  }
  
  /** Log an unexpected situation. */
  public static void log(String message) {
    record(new LoggedCondition(message));
  }
  
  /** The throwable used for logging unexpected situations. */
  public static class LoggedCondition extends Throwable {
    public LoggedCondition(String s) { super(s); }
  }
}
