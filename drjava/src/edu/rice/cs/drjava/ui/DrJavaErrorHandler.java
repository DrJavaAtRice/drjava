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

import javax.swing.JButton;
import java.util.ArrayList;
import javax.swing.JFrame;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.config.OptionConstants;
//import edu.rice.cs.plt.swing.SwingUtil;
import edu.rice.cs.util.Log;

/** The handle() method in this class is called every time an uncaught exception propagates to an AWT action.
 *  The static log() method can be used to put log entries into the error log but continue execution.
 *  This does not automatically update the "DrScala Errors" window when new errors occur. In the case of errors,
 *  we want to minimize the effects on the GUI. If we want to see an updated dialog, we can click on the
 *  "DrScalaErrors" button again.
 *  @version $Id: DrJavaErrorHandler.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DrJavaErrorHandler implements Thread.UncaughtExceptionHandler {
  
  public static final DrJavaErrorHandler INSTANCE = new DrJavaErrorHandler();
  
  private DrJavaErrorHandler() { }
  
  /** Handles an uncaught exception. This gets called automatically by AWT. */
  public void uncaughtException(Thread t, Throwable thrown) {
    record(thrown);
  }
  
  /** the list of errors */
  private static volatile ArrayList<Throwable> _errors = new ArrayList<Throwable>();
  
  /** the button to show */
  private static volatile JButton _errorsButton;
  
  /** Sets the button to show. */
  public static void setButton(JButton b) { _errorsButton = b; }  
  
  /** Gets the button to show. */
  public static JButton getButton() { return _errorsButton; }  
  
  /** Returns the size of the error list. */
  public static int getErrorCount() { return _errors.size(); }
  
  /** Returns the error with the given index. */
  public static Throwable getError(int index) {
    
    if (index >= 0 && index < _errors.size()) return _errors.get(index);
    else return new UnexpectedException("Error in DrJavaErrorHandler");
  }
  
  /** Clears the list of errors. */
  public static void clearErrors() { _errors.clear(); }

  /** Record the throwable in the errors list. */
  public static void record(final Throwable thrown) {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        try { // put the entire handler in a try-block so we don't have an exception in here call the exception handler again (infinite loop)
          if (thrown instanceof OutOfMemoryError) {
            // if this is an OutOfMemoryError inside DrJava, try to suggest to increase Main JVM's max heap
            Runtime.getRuntime().gc();
            JFrame f = DrJavaErrorWindow.getFrame();
            if (f instanceof MainFrame) {
              MainFrame mf = (MainFrame)f;
              mf.askToIncreaseMasterMaxHeap();
            }
          }
          else if (thrown.toString().startsWith("com.sun.jdi.VMOutOfMemoryException")) {
            // if this is an VMOutOfMemoryException, suggest to increase Interaction JVM's max heap
            JFrame f = DrJavaErrorWindow.getFrame();
            if (f instanceof MainFrame) {
              MainFrame mf = (MainFrame)f;
              mf.askToIncreaseSlaveMaxHeap();
            }
          }
          else if (isSwingBugArrayIndexOufOfBoundsExceptionInCharWidth(thrown)) {
            // we just ignore this exception
            return;
          }
          _errors.add(thrown);
          if (_errorsButton != null) {
            _errorsButton.setVisible(true);
          }
          if (_errors.size() == 1 && ! Utilities.TEST_MODE &&
              DrJava.getConfig().getSetting(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED).booleanValue()) {
            DrJavaErrorPopup popup = new DrJavaErrorPopup(DrJavaErrorWindow.getFrame(), thrown);
            Utilities.setPopupLoc(popup, popup.getOwner());
            popup.setVisible(true);
          }
        }
        catch(Throwable t) { /* we're in a bad situation here; an exception in the exception handler cannot be dealt with, so ignore it */ }
      }
    });
  }

  /** Return true if this is an exception thrown because of the Swing bug:
    * https://sourceforge.net/tracker/?func=detail&atid=438935&aid=2831821&group_id=44253
    * @return true if this is the Swing bug */
  public static boolean isSwingBugArrayIndexOufOfBoundsExceptionInCharWidth(Throwable thrown) {
    // only ignore on Sun/Oracle JVMs
    if (!edu.rice.cs.plt.reflect.JavaVersion.CURRENT_FULL.vendor().
          equals(edu.rice.cs.plt.reflect.JavaVersion.VendorType.ORACLE)) return false;
    
    // only ignore if current version is older than 6.0_18 (6.0_18 > JavaVersion.CURRENT_FULL)
    if (edu.rice.cs.plt.reflect.JavaVersion.parseFullVersion("6.0_18","Sun","Sun").
          compareTo(edu.rice.cs.plt.reflect.JavaVersion.CURRENT_FULL)<=0) return false;
    
    if (!(thrown instanceof ArrayIndexOutOfBoundsException)) return false;
    
    StackTraceElement[] stes = new StackTraceElement[] {
      new StackTraceElement("sun.font.FontDesignMetrics","charsWidth",null,-1),
      new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
      new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
      new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
      new StackTraceElement("javax.swing.text.PlainView","viewToModel",null,-1),
      new StackTraceElement("javax.swing.plaf.basic.BasicTextUI$RootView","viewToModel",null,-1),
      new StackTraceElement("javax.swing.plaf.basic.BasicTextUI","viewToModel",null,-1)
    };
    
    StackTraceElement[] stesBottom = new StackTraceElement[] {
      new StackTraceElement("java.awt.EventQueue","dispatchEvent",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","pumpOneEventForFilters",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","pumpEventsForFilter",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","pumpEventsForHierarchy",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","pumpEvents",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","pumpEvents",null,-1),
      new StackTraceElement("java.awt.EventDispatchThread","run",null,-1)
    };

    StackTraceElement[] tst = thrown.getStackTrace();
    
    if (tst.length<stes.length+stesBottom.length) return false;
    
    for(int i=0; i<stes.length; ++i) {
      if (!stes[i].equals(tst[i])) return false;
    }

    for(int i=0; i<stesBottom.length; ++i) {
      if (!stesBottom[stesBottom.length-i-1].equals(tst[tst.length-i-1])) return false;
    }
    
    return true;
  }

  /** Simulate the Swing bug's exception stack trace. */
  public static void simulateSwingBugArrayIndexOufOfBoundsExceptionInCharWidth() {      
    StackTraceElement[] stes = new StackTraceElement[] {
      new StackTraceElement("sun.font.FontDesignMetrics","charsWidth",null,-1),
        new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
        new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
        new StackTraceElement("javax.swing.text.Utilities","getTabbedTextOffset",null,-1),
        new StackTraceElement("javax.swing.text.PlainView","viewToModel",null,-1),
        new StackTraceElement("javax.swing.plaf.basic.BasicTextUI$RootView","viewToModel",null,-1),
        new StackTraceElement("javax.swing.plaf.basic.BasicTextUI","viewToModel",null,-1),
        
        new StackTraceElement("foo","bar",null,-1),
        new StackTraceElement("foo","bar",null,-1),
        
        new StackTraceElement("java.awt.EventQueue","dispatchEvent",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","pumpOneEventForFilters",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","pumpEventsForFilter",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","pumpEventsForHierarchy",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","pumpEvents",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","pumpEvents",null,-1),
        new StackTraceElement("java.awt.EventDispatchThread","run",null,-1)
    };
    ArrayIndexOutOfBoundsException t = new ArrayIndexOutOfBoundsException(63);
    t.setStackTrace(stes);
    t.printStackTrace(System.out);
    throw t;     
  }

  /** Log an unexpected situation. */
  public static void log(String message) { record(new LoggedCondition(message)); }
  
  /** The throwable used for logging unexpected situations. */
  public static class LoggedCondition extends Throwable {
    public LoggedCondition(String s) { super(s); }
  }
}
