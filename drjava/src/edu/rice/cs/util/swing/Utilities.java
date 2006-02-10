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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *       disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their names 
 *       without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import java.awt.EventQueue;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

public class Utilities {
  
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
  
  /**
   * Show a modal debug message box with an OK button.
   * @param msg string to display
   */
  public static void showDebug(String msg) {
    showMessageBox(msg, "Debug Message");
  }
  
  /**
   * Show a modal message box with an OK button.
   * @param msg string to display
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
  
  
  /**
   * Message dialog with a word-wrapping text area that allows copy & paste.
   */
  public static class TextAreaMessageDialog extends JDialog {
    /**
     * True if the program is run in non-interactive test mode.
     */
    public static boolean TEST_MODE = false;

    /**
     * Show the initialized dialog.
     * @param comp parent component, or null
     * @param title dialog title
     * @param message message for the text area
     */
    public static void showDialog(Component comp,
                                  String title,
                                  String message) {
      if (TEST_MODE) {
        System.out.println(title+": "+message);
      }
      else {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        TextAreaMessageDialog dialog = new TextAreaMessageDialog(frame, comp, title, message);
        dialog.setVisible(true);
      }
    }

    /**
     * Private constructor for this dialog. Only gets used in the static showDialog method.
     * @param frame owner frame
     * @param comp parent component
     * @param title dialog title
     * @param message message for the text area
     */
    private TextAreaMessageDialog(Frame frame, Component comp, String title, String message) {
      super(frame, title, true);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

      //buttons
      JButton okButton = new JButton("Ok");
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

      Dimension parentDim = (comp!=null)?(comp.getSize()):getToolkit().getScreenSize();
      int xs = (int)parentDim.getWidth()/4;
      int ys = (int)parentDim.getHeight()/5;
      setSize(Math.max(xs,350), Math.max(ys, 250));
      setLocationRelativeTo(comp);
    }
  }
}
