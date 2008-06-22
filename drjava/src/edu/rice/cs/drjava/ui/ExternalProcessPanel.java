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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Enumeration;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.*;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.ProcessCreator;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.ui.predictive.*;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.FileOps;
import static edu.rice.cs.drjava.ui.MainFrame.GoToFileListEntry;

/** Panel for displaying some component with buttons, one of which is an "Abort" button.
  * This should be used to display the output of an external process.
  * This class is a swing class that should only be accessed from the event thread.
  * @version $Id$
  */
public class ExternalProcessPanel extends AbortablePanel {
  /** Size of the buffer read at once. */
  public final int BUFFER_SIZE = 10240;
  /** Number of buffer reads before the event thread is allowed to do something else. */
  public final int BUFFER_READS_PER_TIMER = 5;
  protected JTextArea _textArea;
  protected ProcessCreator _pc = null;
  protected Process _p = null;
  protected InputStreamReader _is = null;
  protected InputStreamReader _erris = null;
  protected JButton _updateNowButton;
  protected JButton _runAgainButton;
  protected Thread _updateThread;
  protected Thread _readThread;
  protected Thread _deathThread;
  protected StringBuilder _sb = new StringBuilder();
  protected volatile int _changeCount = 0;
  private char[] _buf = new char[BUFFER_SIZE];
  private int _red = -1;
  private char[] _errbuf = new char[BUFFER_SIZE];
  private int _errred = -1;
  private int _retVal;
  private String _header;

  /** Constructs a new "process" panel to watch process output.
    * This is swing view class and hence should only be accessed from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    * @param pc the process creator to use
    */
  public ExternalProcessPanel(MainFrame frame, String title, ProcessCreator pc) {
    super(frame, title);
    _sb = new StringBuilder("Command line:");
    _sb.append(pc.cmdline());
    _sb.append('\n');
    _header = _sb.toString();
    _textArea.setText(_header);
    initThread(pc);
    _textArea.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if ((SwingUtilities.isLeftMouseButton(e)) &&
            (e.getClickCount()==2)) {
          doubleClicked(e);
        }
      }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) { }
    });
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { updateText(); } });
    // MainFrame.LOG.log("\tProcessPanel ctor done");
  }

  protected void initThread(ProcessCreator pc) {
    // MainFrame.LOG.log("\tProcessPanel ctor");
    try {
      _pc = pc;
      _pc.getPropertyMaps().clearVariables();
      _readThread = new Thread(new Runnable() {
        public void run() {
          while((_is!=null) || (_erris!=null)) {
            readText(false);
          }
        }
      },"External Process Read Thread");
      _updateThread = new Thread(new Runnable() {
        public void run() {
          while((_is!=null) || (_erris!=null)) {
            try {
              Thread.sleep(edu.rice.cs.drjava.DrJava.getConfig().
                             getSetting(edu.rice.cs.drjava.config.OptionConstants.FOLLOW_FILE_DELAY));
            }
            catch(InterruptedException ie) { /* ignore */ }
            updateText();
          }
        }
      },"External Process Update Thread");
      _p = _pc.start();
      _sb.append("Evaluated command line:");
      _sb.append(_pc.evaluatedCommandLine());
      _sb.append('\n');
      _is = new InputStreamReader(_p.getInputStream());
      _erris = new InputStreamReader(_p.getErrorStream());
      _readThread.start();
      _updateThread.start();
      _updateNowButton.setEnabled(true);
      _deathThread = new Thread(new Runnable() {
        public void run() {
          try {
            _retVal = _p.waitFor();
            Utilities.invokeLater(new Runnable() {
              public void run() {
                _sb.append("\n\nProcess returned ");
                _sb.append(_retVal);
                _sb.append("\n");
                _textArea.setText(_sb.toString());
              }
            });
          }
          catch(InterruptedException e) {
            Utilities.invokeLater(new Runnable() {
              public void run() {
                _p.destroy();
                _sb.append("\n\nProcess returned ");
                _sb.append(_retVal);
                _sb.append("\n");
                _textArea.setText(_sb.toString());
              }
            });
          }
          finally {
            abortActionPerformed(null);
          }
        }
      },"External Process Death Thread");
      _deathThread.start();
      // MainFrame.LOG.log("\tUpdate thread started");
    }
    catch(Exception e) {
      _sb.append("\n\nException from process:\n"+e.toString());
      _textArea.setText(_sb.toString());
      edu.rice.cs.util.GeneralProcessCreator.LOG.log(_sb.toString());
      abortActionPerformed(null);
    }
  }
  
  /** Setup left panel. Must be overridden to return the component on the left side. */
  protected Component makeLeftPanel() {
    _textArea = new JTextArea();
    _textArea.setEditable(false);
    return _textArea;
  }

  /** Abort action was performed.
    * @param e action event performed by user, or null if aborted due to problem */
  protected void abortActionPerformed(ActionEvent e) {
    if (_is!=null) {
      try {
        _is.close();
      }
      catch(IOException ioe) { /* ignore, just stop polling */ }
      _is = null;
      updateButtons();
    }
    if (_erris!=null) {
      try {
        _erris.close();
      }
      catch(IOException ioe) { /* ignore, just stop polling */ }
      _erris = null;
      updateButtons();
    }
    if (_p!=null) {
      _p.destroy();
      _p = null;
    }
    updateText();
  }
  
  // public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("external.txt",true);
  
  /** Gets called when the user double-clicks on the text pane. */
  public void doubleClicked(MouseEvent e) {
    // LOG.log("doubleClicked");
    final String t = _textArea.getText();
    int caret = _textArea.getCaretPosition();
    int start = caret;
    int end = start;
    while((start-1>0) && (t.charAt(start-1)!='\n')) { --start; }
    while((end>=0) && (end<t.length()) && (t.charAt(end)!='\n')) { ++end; }
    // LOG.log("\tstart="+start+"\n\tend="+end);
    if ((start<0) || (end<0) || (start>=t.length()) || (end>=t.length())) return;
    final String line = t.substring(start,end);
    // LOG.log("\t'"+line+"'");
    caret -= start; // calculate caret position within the line
    if (caret>=line.length()) { caret = line.length()-1; }
    start = end = caret;
    char ch;
    while((end>=0) && (end<line.length())) {
      ch = line.charAt(end);
      if (ch==':') {
        if ((end+1<line.length()) && (Character.isDigit(line.charAt(end+1)))) {
          // perhaps a colon followed by a line number: Foo.java:10
          // advance to the end of the number, then break
          do {
            ++end;
          } while((end<line.length()) && (Character.isDigit(line.charAt(end))));
          break;
        }
        else {
          // colon without digit behind it, break here
          break;
        }
      }
      else if (Character.isJavaIdentifierPart(ch)) {
        // character is a Java identifier part, advance
        ++end;
      }
      else if ((ch=='.') || (ch==File.separatorChar)) {
        // allow the period and file separator, could be the part of file name; advance
        ++end;
      }
      else {
        // character should not be in a Java name, break here
        break;
      }
    }

    List<OpenDefinitionsDocument> docs = _model.getOpenDefinitionsDocuments();
    if ((docs == null) || (docs.size() == 0)) return; // do nothing

    ArrayList<GoToFileListEntry> list;
    list = new ArrayList<GoToFileListEntry>(docs.size());
    // create a list with fully qualified class names
    for(OpenDefinitionsDocument d: docs) {
      // LOG.log("Doc: "+d);
      try {
        String fullyQualified = d.getPackageName() + "." + d.toString();
        if (fullyQualified.startsWith(".")) { fullyQualified = fullyQualified.substring(1); }
        list.add(new GoToFileListEntry(d, fullyQualified));
        // LOG.log("\tname: "+fullyQualified);
      }
      catch(IllegalStateException ex) { /* ignore */ }
    }
    PredictiveInputModel<GoToFileListEntry> pim =
      new PredictiveInputModel<GoToFileListEntry>(true, new PredictiveInputModel.PrefixLineNumStrategy<GoToFileListEntry>(), list);
    
    GoToFileListEntry uniqueMatch = null;
    String name, oldName = null, simpleName = null;
    while(start>0) {
      ch = line.charAt(start);
      while(start>0) {
        ch = line.charAt(start);
        if ((ch==':') || (ch=='.') || (Character.isJavaIdentifierPart(ch))) { --start; } else { break; }
      }
      // LOG.log("\tstart="+start+"\n\tend="+end);
      if ((start>=0) && (end>=start) && (start<line.length()) && (end<line.length())) {
        name = line.substring(start,end).replace(File.separatorChar,'.');
        if ((name.length()>0) && (!Character.isJavaIdentifierPart(name.charAt(0)))) { name = name.substring(1); }
        if (simpleName==null) { simpleName = name; }
        if (name.equals(oldName)) { break; }
        if ((name.indexOf(".java")>=0) ||
            (name.indexOf(".j")>=0) ||
            (name.indexOf(".dj0")>=0) ||
            (name.indexOf(".dj1")>=0) ||
            (name.indexOf(".dj2")>=0)) {
          // LOG.log("\t--> '"+name+"'");
          uniqueMatch = getUniqueMatch(name, pim);
          if (uniqueMatch!=null) {
            // unique match found, go there
            // LOG.log("\t     ^^^^^^^^^^ unique match found");
            final OpenDefinitionsDocument newDoc = pim.getCurrentItem().doc;
            final boolean docChanged = ! newDoc.equals(_model.getActiveDocument());
            final boolean docSwitch = _model.getActiveDocument() != newDoc;
            if (docSwitch) _model.setActiveDocument(newDoc);
            final int curLine = newDoc.getCurrentLine();
            final int last = name.lastIndexOf(':');
            if (last >= 0) {
              try {
                String nend = name.substring(last + 1);
                int val = Integer.parseInt(nend);
                
                final int lineNum = Math.max(1, val);
                Runnable command = new Runnable() {
                  public void run() {
                    try { _frame._jumpToLine(lineNum); }  // adds this region to browser history
                    catch (RuntimeException ex) { _frame._jumpToLine(curLine); }
                  }
                };
                if (docSwitch) {
                  // postpone running command until after document switch, which is pending in the event queue
                  EventQueue.invokeLater(command);
                }
                else command.run();
              }
              catch(RuntimeException ex) { /* ignore */ }
            }
            else if (docChanged) {
              // defer executing this code until after active document switch (if any) is complete
              EventQueue.invokeLater(new Runnable() { public void run() { _frame.addToBrowserHistory(); } });
            }
            break;
          }
        }
        oldName = name;
      }
      else {
        break;
      }
      if (ch==File.separatorChar) { --start; } // file separator ('/' or '\'), include preceding directory 
    }
    if (uniqueMatch==null) {
      // couldn't find a unique match, even after gradually including the fully qualified name
      _frame.gotoFileMatchingMask(simpleName);
    }
  }
  
  /** Return the unique match for the mask, or null if no match found or not unique.
    * @param mask word specifying the file to go to
    * @param pim predictive input model with possible matches
    * @return unique match, or null if no match found or not unique */
  GoToFileListEntry getUniqueMatch(String mask, PredictiveInputModel<GoToFileListEntry> pim) {        
    pim.setMask(mask);
    
    if (pim.getMatchingItems().size() == 1) {
      // exactly one match, go to file
      if (pim.getCurrentItem() != null) { return pim.getCurrentItem(); }
    }
    return null;
  }
  
  /** Update button state and text. Should be overridden if additional buttons are added besides "Go To", "Remove" and "Remove All". */
  protected void updateButtons() {
    boolean ended = true;
    if (_p!=null) {
      try {
        // try to get exitValue() to see if process has terminated; exit value is not otherwise important
        int ignored = _p.exitValue();
        // if no exception is thrown, then the process has finished
        ended = true;
      }
      catch(IllegalThreadStateException e) {
        // process has NOT finished yet
        ended = false;
      }
    }
    _abortButton.setEnabled((_is!=null) && (_erris!=null) && (!ended));
    _updateNowButton.setEnabled((_is!=null) && (_erris!=null) && (!ended));
    _runAgainButton.setEnabled((_is==null) || (_erris==null) || (ended));
  }  

  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {
    _updateNowButton = new JButton("Update");
    _updateNowButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { 
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { updateText(); } });
      }
    });
    _runAgainButton = new JButton("Run Again");
    _runAgainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        abortActionPerformed(e);
        _sb = new StringBuilder("Command line:");
        _sb.append(_pc.cmdline());
        _sb.append('\n');
        _header = _sb.toString();
        initThread(_pc);
        _textArea.setText(_header);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { updateText(); } });
      }
    });
    return new JComponent[] { _updateNowButton, _runAgainButton };
  }

  /** Read new text from the stream.
    * @param finish whether to read the entire rest */
  protected void readText(final boolean finish) {
    // MainFrame.LOG.log("readText");
    if (((_is!=null) || (_erris!=null))) {
      _changeCount = 0;
      // MainFrame.LOG.log("\tgot text");
      try {
        // MainFrame.LOG.log("\treading...");
        // abort after reading 5 blocks (50 kB), read more later
        // don't block the event thread any longer
        while((_is!=null) &&
              (_erris!=null) &&
              (_changeCount<=BUFFER_READS_PER_TIMER) &&
              (_erris!=null) &&
              ((_red = _is.read(_buf))>=0)) {
          // MainFrame.LOG.log("\tread "+_red+" bytes");
          _sb.append(new String(_buf, 0, _red));
          if (finish) { _changeCount = 1; } else { ++_changeCount; }
        }
        while((_changeCount<=BUFFER_READS_PER_TIMER) &&
              (_erris!=null) &&
              ((_errred = _erris.read(_errbuf))>=0)) {
          // MainFrame.LOG.log("\tread "+_red+" bytes");
          _sb.append(new String(_errbuf, 0, _errred));
          if (finish) { _changeCount = 1; } else { ++_changeCount; }
        }
        if ((_red>0) && (_changeCount<BUFFER_READS_PER_TIMER)) {
          _sb.append(new String(_buf, 0, _red));
          if (finish) { _changeCount = 1; } else { ++_changeCount; }
        }
        if ((_errred>0) && (_changeCount<BUFFER_READS_PER_TIMER)) {
          _sb.append(new String(_errbuf, 0, _errred));
          if (finish) { _changeCount = 1; } else { ++_changeCount; }
        }
        if ((_p!=null) && (_is==null)) {
          try {
            // try to get exitValue() to see if process has terminated; exit value is not otherwise important
            int ignored = _p.exitValue();
            // if no exception is thrown, then the process has finished, and the stream may be null
          }
          catch(IllegalThreadStateException e) {
            // process has NOT finished yet, but the stream is null; this is a problem
            _sb.append("\nInput stream suddenly became null.");
          }
        }
        if ((_p!=null) && (_erris==null)) { 
          try {
            // try to get exitValue() to see if process has terminated; exit value is not otherwise important
            int ignored = _p.exitValue();
            // if no exception is thrown, then the process has finished, and the stream may be null
          }
          catch(IllegalThreadStateException e) {
            // process has NOT finished yet, but the stream is null; this is a problem
            _sb.append("\nError input stream suddenly became null.");
          }
        }
      }
      catch(IOException ioe) {
        // MainFrame.LOG.log("\taborted");
        // stop polling
        if (_p!=null) {
          try {
            _p.exitValue();
            // if we get here, process has finished, and we don't display the I/O exception
          }
          catch(IllegalThreadStateException e) {
            // process has NOT finished yet, display the I/O exception
            _sb.append("\n\nI/O Exception reading from process:\n"+ioe.toString());
            edu.rice.cs.util.GeneralProcessCreator.LOG.log("\n\nI/O Exception reading from process:");
            edu.rice.cs.util.GeneralProcessCreator.LOG.log(ioe.toString(),ioe);
          }
        }
        if (finish) { _changeCount = 1; } else { ++_changeCount; }
        abortActionPerformed(null);
      }
    }
  }
  
  /** Update the text area with the text that was read. */
  protected void updateText() {
    // MainFrame.LOG.log("updateText");
    if (_updateNowButton.isEnabled()) {
//      try {
//        if ((_is!=null) && (_p!=null) &&
//            (_is.ready()) &&
//            (_p.getInputStream().available()>0)) { readText(false); }
//      }
//      catch(IOException ioe) {
//        try {
//          _p.exitValue();
//          // if we get here, process has finished, and we don't display the I/O exception
//        }
//        catch(IllegalThreadStateException e) {
//          // process has NOT finished yet, display the I/O exception
//          _sb.append("\n\nI/O Exception reading from process\n");
//        }
//        abortActionPerformed(null);
//        ++_changeCount;
//      }
      if (_changeCount>0) {
        _changeCount = 0;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // MainFrame.LOG.log("\tsetting text");
            _textArea.setText(_sb.toString());
            int maxLines = edu.rice.cs.drjava.DrJava.getConfig().
              getSetting(edu.rice.cs.drjava.config.OptionConstants.FOLLOW_FILE_LINES);
            if (maxLines>0) { // if maxLines is 0, buffer is unlimited
              try {
                int start = 0;
                int len = _textArea.getText().length();
                int curLines = _textArea.getLineCount();
                if (curLines>maxLines) {
                  start = _textArea.getLineStartOffset(curLines-maxLines);
                  len -= start;
                  _sb = new StringBuilder(_textArea.getText(start,len));
                  _textArea.setText(_sb.toString());
                }
              }
              catch(javax.swing.text.BadLocationException e) { /* ignore, do not truncate */ }
            }
            // MainFrame.LOG.log("\ttext length = "+s.length());
          }
        });
      }
      // MainFrame.LOG.log("\tupdating buttons");
      updateButtons();
    }
  }
}
