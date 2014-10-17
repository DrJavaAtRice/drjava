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

import java.io.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.util.swing.Utilities;

/** Panel for displaying some component with buttons, one of which is an "Abort" button.
  * This should be used to display the output of an external process.
  * This class is a swing class that should only be accessed from the event thread.
  * @version $Id: LessPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class LessPanel extends AbortablePanel {
  /** Size of the buffer read at once. */
  public final int BUFFER_SIZE = 10240;
  /** Number of buffer reads before the event thread is allowed to do something else. */
  public final int BUFFER_READS_PER_TIMER = 5;
  protected JTextArea _textArea;
  protected File _f = null;
  protected FileReader _fr = null;
  protected JButton _updateNowButton;
  protected JButton _restartButton;
  protected Thread _updateThread;
  private char[] _buf = new char[BUFFER_SIZE];
  private int _red = -1;
  private long _totalRead = 0;

  /** Constructs a new "less" panel to watch file output.
    * This is swing view class and hence should only be accessed from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    * @param f file to monitor
    */
  public LessPanel(MainFrame frame, String title, File f) {
    super(frame, title);
    initThread(f);
    updateText();
    // MainFrame.LOG.log("\tLessPanel ctor done");
  }

  protected void initThread(File f) {
    try {
      // MainFrame.LOG.log("\tLessPanel ctor");
      // MainFrame.LOG.log("\texists: " + f.exists());
      // MainFrame.LOG.log("\tcanRead: " + f.canRead());
      // MainFrame.LOG.log("\tisFile: " + f.isFile());
      if (f.exists() && f.canRead() && f.isFile()) {
        // MainFrame.LOG.log("\texists, can be read, is file");
        // MainFrame.LOG.log("\tfile length = " + f.length());
        _f = f;
        _fr = new FileReader(_f);
        _red = -1;
        _totalRead = 0;
        _updateThread = new Thread(new Runnable() {
          public void run() {
            while(_fr != null) {
              try {
                Thread.sleep(edu.rice.cs.drjava.DrJava.getConfig().
                               getSetting(edu.rice.cs.drjava.config.OptionConstants.FOLLOW_FILE_DELAY));
              }
              catch(InterruptedException ie) { /* ignore */ }
              updateText();
            }
          }
        });
        _updateThread.start();
        _updateNowButton.setEnabled(true);
        // MainFrame.LOG.log("\tUpdate thread started");
      }
    }
    catch(Exception e) {
      _fr = null;
    }
  }
  
  /** Setup left panel. Must be overridden to return the component on the left side. */
  protected Component makeLeftPanel() {
    _textArea = new JTextArea();
    _textArea.setEditable(false);
    return _textArea;
  }

  /** Abort action was performed. Must be overridden to return the component on the left side. */
  protected void abortActionPerformed(ActionEvent e) {
    if (_fr != null) {
      try {
        _fr.close();
      }
      catch(IOException ioe) { /* ignore, just stop polling */ }
      _fr = null;
      updateButtons();
    }
  }
  
  /** Update button state and text. Should be overridden if additional buttons are added besides "Go To", "Remove" and "Remove All". */
  protected void updateButtons() {
    _abortButton.setEnabled(_fr != null);
    _updateNowButton.setEnabled(_fr != null);
    _restartButton.setEnabled(_fr == null);
  }  

  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {
    _updateNowButton = new JButton("Update");
    _updateNowButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { updateText(); }
    });
    _restartButton = new JButton("Restart");
    _restartButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { restartFollowing(); }
    });
    _restartButton.setEnabled(false);
    return new JComponent[] { _updateNowButton, _restartButton };
  }

  /** Restart following this file. */
  protected void restartFollowing() {
    _textArea.setText("");
    initThread(_f);
    updateText();
  }
  
  /** Update the text area if there is new text in the file.
    * May not read all new text if there is too much, as that would
    * block the event thread for too long. */
  protected void updateText() {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        // MainFrame.LOG.log("updateText");
        if ((_fr != null) &&
            (_updateNowButton.isEnabled())) {
          _updateNowButton.setEnabled(false);
          int changeCount = 0;
          long newSize = _f.length();
          if (_totalRead!=newSize) {
            if (_totalRead>newSize) {
              // must start over, file got smaller!
              _textArea.setText("");
              _totalRead = 0;
            }
            StringBuilder sb = new StringBuilder(_textArea.getText());
            // MainFrame.LOG.log("\tgot text");
            try {
              _fr.close();
              _fr = new FileReader(_f);
              _fr.skip(_totalRead);
              // MainFrame.LOG.log("\treading... skipped to " + _totalRead);
              // abort after reading 5 blocks (50 kB), read more later
              // don't block the event thread any longer
              while((changeCount<=BUFFER_READS_PER_TIMER) && ((_red = _fr.read(_buf)) >= 0)) {
                // MainFrame.LOG.log("\tread " + _red + " bytes");
                _totalRead += _red;
                sb.append(new String(_buf, 0, _red));
                ++changeCount;
              }
              if ((_red > 0) && (changeCount<BUFFER_READS_PER_TIMER)) {
                _totalRead += _red;
                sb.append(new String(_buf, 0, _red));
                ++changeCount;
              }
            }
            catch(IOException ioe) {
              // MainFrame.LOG.log("\taborted");
              // stop polling
              sb.append("\n\nI/O Exception reading file " + _f + "\n");
              ++changeCount;
              abortActionPerformed(null);
            }
            finally {
              if (changeCount > 0) {
                // MainFrame.LOG.log("\tsetting text");
                _textArea.setText(sb.toString());
                int maxLines = edu.rice.cs.drjava.DrJava.getConfig().
                  getSetting(edu.rice.cs.drjava.config.OptionConstants.FOLLOW_FILE_LINES);
                if (maxLines > 0) { // if maxLines is 0, buffer is unlimited
                  try {
                    int start = 0;
                    int len = _textArea.getText().length();
                    int curLines = _textArea.getLineCount();
                    if (curLines>maxLines) {
                      start = _textArea.getLineStartOffset(curLines-maxLines);
                      len -= start;
                      sb = new StringBuilder(_textArea.getText(start,len));
                      _textArea.setText(sb.toString());
                    }
                  }
                  catch(javax.swing.text.BadLocationException e) { /* ignore, do not truncate */ }
                }
                // MainFrame.LOG.log("\ttext length = " + s.length());
              }
            }
          }
        }
        // MainFrame.LOG.log("\tupdating buttons");
        updateButtons();
      }
    });
  }
}
