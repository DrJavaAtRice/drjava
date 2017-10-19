/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Container;
import java.awt.SystemColor;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/** Message dialog with a word-wrapping text area that allows copy & paste. */
public class TextAreaMessageDialog extends JDialog {
  
  /** 
   * Show the initialized dialog.  Should be invoked from the event thread.
   * @param comp parent component, or null
   * @param title dialog title
   * @param message message for the text area
   */
  public static void showDialog(Component comp, String title, String message) {
    Frame frame = JOptionPane.getFrameForComponent(comp);
    TextAreaMessageDialog dialog = new TextAreaMessageDialog(frame, comp, title, message);
    SwingUtil.setPopupLoc(dialog, frame);
    dialog.setVisible(true);
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
    okButton.addActionListener(SwingUtil.disposeAction(this));
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
