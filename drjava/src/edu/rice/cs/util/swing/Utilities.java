/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.util.swing;

import edu.rice.cs.drjava.ui.MainFrame;
import java.awt.EventQueue;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.datatransfer.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

import edu.rice.cs.drjava.ui.DrJavaErrorHandler;
  
public class Utilities {
  
  /** True if the program is run in non-interactive test mode. */
  public static volatile boolean TEST_MODE = false;
  
  /** Runs the task synchronously if the current thread is the event thread; otherwise passes it to the
   *  event thread to be run asynchronously after all events already on the queue have been processed.
   */
  public static void invokeLater(Runnable task) {
    if (EventQueue.isDispatchThread()) {
      task.run(); 
      return;
    }
    EventQueue.invokeLater(task);
  }
  
  public static void invokeAndWait(Runnable task) {
    if (EventQueue.isDispatchThread()) {
      task.run(); 
      return;
    }
    try { EventQueue.invokeAndWait(task); }
    catch(Exception e) { throw new UnexpectedException(e); }
  }
  
  public static void main(String[] args) { clearEventQueue(); }
  
  public static void clearEventQueue() {
    Utilities.invokeAndWait(new Runnable() { public void run() { } });
  }
  
  /** Show a modal debug message box with an OK button regardless of TEST_MODE.
   *  @param msg string to display
   */
  public static void show(final String msg) { 
     Utilities.invokeAndWait(new Runnable() { public void run() { JOptionPane.showMessageDialog(null, msg); } } );
  }
  
  /** Show a modal debug message box containing a backtrace for the Throwable t.
   *  @param t the Throwable to be back traced.
   */
  public static void showTrace(final Throwable t) { 
    Utilities.invokeAndWait(new Runnable() { public void run() { new DrJavaErrorHandler().handle(t); } } );
  } 
  
  /** Show a modal debug message box with an OK button when not in TEST_MODE.
   *  @param msg string to display
   */
  public static void showDebug(String msg) { showMessageBox(msg, "Debug Message"); }
  
  /** Show a modal message box with an OK button.
   *  @param msg string to display
   */
  public static void showMessageBox(final String msg, final String title) {
    //Utilities.invokeAndWait(new Runnable() { public void run() { JOptionPane.showMessageDialog(null, msg); } } );
    Utilities.invokeAndWait(new Runnable() { public void run() {
      Utilities.TextAreaMessageDialog.showDialog(null, title, msg); 
    } } );
  }

  public static void showStackTrace(final Throwable t) {
    Utilities.invokeAndWait(new Runnable() { public void run() { 
      JOptionPane.showMessageDialog(null, StringOps.getStackTrace(t));
    } } );
  }
  
  
  /** Message dialog with a word-wrapping text area that allows copy & paste. */
  public static class TextAreaMessageDialog extends JDialog {

    /** Show the initialized dialog.
     *  @param comp parent component, or null
     *  @param title dialog title
     *  @param message message for the text area
     */
    public static void showDialog(Component comp, String title, String message) {
      if (TEST_MODE) System.out.println(title + ": " + message);
      else {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        TextAreaMessageDialog dialog = new TextAreaMessageDialog(frame, comp, title, message);
        MainFrame.setPopupLoc(dialog, frame);
        dialog.setVisible(true);
      }
    }

    /** Private constructor for this dialog. Only gets used in the static showDialog method.
     *  @param frame owner frame
     *  @param comp parent component
     *  @param title dialog title
     *  @param message message for the text area
     */
    private TextAreaMessageDialog(Frame frame, Component comp, String title, String message) {
      super(frame, title, true);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

      //buttons
      JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TextAreaMessageDialog.this.dispose();
        }
      });
      getRootPane().setDefaultButton(okButton);

      JTextArea textArea = new JTextArea(message);
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(false);
      textArea.setBackground(SystemColor.window);

      Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      textArea.setBorder(emptyBorder);

      Container contentPane = getContentPane();
      contentPane.add(textArea, BorderLayout.CENTER);
      contentPane.add(okButton, BorderLayout.SOUTH);

      Dimension parentDim = (comp != null)?(comp.getSize()):getToolkit().getScreenSize();
      int xs = (int)parentDim.getWidth()/4;
      int ys = (int)parentDim.getHeight()/5;
      setSize(Math.max(xs,350), Math.max(ys, 250));
      setLocationRelativeTo(comp);
    }
  }
  
  /** @return a string with the current clipboard selection, or null if not available. */
  public static String getClipboardSelection(Component c) {
      Clipboard cb = c.getToolkit().getSystemClipboard();
      if (cb == null) return null;
      Transferable t = cb.getContents(null);
      if (t == null) return null;
      String s = null;
      try {
        java.io.Reader r = DataFlavor.stringFlavor.getReaderForText(t);
        int ch;
        final StringBuilder sb = new StringBuilder();
        while ((ch=r.read()) !=-1 ) { sb.append((char)ch); }
        s = sb.toString();
      }
      catch(UnsupportedFlavorException ufe) { /* ignore, return null */ }
      catch(java.io.IOException ioe) { /* ignore, return null */ }
      return s;
  }
  
  /** @return an action with a new name that delegates to another action. */
  public static AbstractAction createDelegateAction(String newName, final Action delegate) {
    return new AbstractAction(newName) {
      public void actionPerformed(ActionEvent ae) { delegate.actionPerformed(ae); }
    };
  }
}
