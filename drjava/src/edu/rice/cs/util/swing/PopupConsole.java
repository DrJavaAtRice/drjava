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

package edu.rice.cs.util.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

import java.io.Serializable;

import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentInterface;

/** This console is the console that is used to receive <code>System.in</code> input from the user. 
 *  <p>
 *  When the <code>getConsoleInput</code> method is called, one of two things may happen.  When the 
 *  MainFrame (or which ever frame owns the interactions pane) is visible, this method pops up a 
 *  dialog box with a text area where the user can type in text.  Hitting <code>Enter</code> 
 *  terminates the current input and causes the <code>getConsoleInput</code> method to return the 
 *  inputted text.  Text can be inputted programmatically into the text area by calling 
 *  <code>insertConsoleText</code> from the PopupConsole object that created the dialog box.  The 
 *  input can be forcefully terminated by calling <code> interruptConsole</code>. This method causes 
 *  the input to terminate as though the user had pressed enter or hit the "done" button.
 *  <p>
 *  If the MainFrame is not yet visible (which is the case during unit testing), the console works 
 *  in "silent" mode.  In silent mode, no dialog box is displayed to the user. Rather, the only way to 
 *  input any text is through the <code> inputConsoleText</code> method; and the only way to terminate 
 *  the current input is with <code>interruptConsole</code>. In either of the two cases, the 
 *  <code>inputConsoleText</code> method cannot be used unless the console is currently receiving input. 
 *  The <code>interruptConsole</code> method does nothing when the console is not open for input. 
 */
public class PopupConsole implements Serializable {
  
  private static final String INPUT_ENTERED_NAME = "Input Entered";
  private static final String INSERT_NEWLINE_NAME = "Insert Newline";
    
  private JTextArea _inputBox;
  
  /** Shift-Enter action in a System.in box.  Inserts a newline. */
  private Action _insertNewlineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      JTextArea source = (JTextArea)e.getSource();
      source.insert("\n", source.getCaretPosition());
    }
  };
  
  private String _title;
  private Component _parentComponent;
  
  /** Associated InteractionsDocument */
  private EditDocumentInterface _doc;
  
  /** Associated console tab document */
  private ConsoleDocument _console;
  
  private Runnable _interruptCommand;
  private Lambda<Object,String> _insertTextCommand;
  
  // used to ensure thread safety when using insertConsoleText and interruptConsole
  private final Object commandLock = new Object();
  
  /* flag used to record the fact that the monitor (in silentInput) has been notified */
  private boolean monitorNotified = false; 
  
  /** Flag that signals input via System.in has been aborted.  Without this feature, an executing program that is 
   *  waiting for input cannot be aborted. */
  private volatile boolean inputAborted = false;
  
  /** Creates a PopupConsole that belongs to the given component (typically the interactions pane). The given
   *  text component will be placed in the console dialog box to receive text.
   *  @param owner The component that owns the dialogs created by the PopupConsole
   *  @param inputBox The JTextArea that will receive input from the user. The Shift+<Enter> key binding is set
   *         here to insert "\n", so that functionality need not be implemented in this text area.
   *  @param title The title of the dialog box that pops up
   */
  public PopupConsole(Component owner, EditDocumentInterface doc, ConsoleDocument console, JTextArea inputBox, 
                      String title) {
    setParent(owner);
    _doc = doc;
    _console = console;
    setInputBox(inputBox);
    setTitle(title);
  }
  
  private String _consoleLine;
  /** Receives input from the user dialog box or programatically through the <code>insertConsoleText</code> method.
   *  @return The text inputted by the user
   */
  public String getConsoleInput() { 
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        Frame parentFrame = JOptionPane.getFrameForComponent(_parentComponent);
        if (parentFrame.isVisible()) _consoleLine = showDialog(parentFrame);
        else _consoleLine = silentInput();
      }
    });
    return _consoleLine + "\n";
  }
  
  /** Forces the console to stop receiving input from the user and return what has been inputted so far. */
  public void interruptConsole() {
    synchronized (commandLock) { if (_interruptCommand != null) _interruptCommand.run(); }
  }
  
  /** Inserts the given text into the console at the current cursor location
   *  @param txt The text to insert into the console
   *  @throws IllegalStateException when the console is not currently receiving input from the user
   */
  public void insertConsoleText(String txt) {
    synchronized (commandLock) {
      if (_insertTextCommand != null) _insertTextCommand.apply(txt); 
      else {
        throw new IllegalStateException("Console not ready for text insertion");
      }
    }
  }
  
  /** Causes the current thread to wait until the console is ready for input via the insertConsoleText method. This 
   *  should be used right before a call to <code>insertConsoleText</code> or <code>interruptConsole</code> to avoid
   *  calling these methods before the console starts receiving any input.
   */
  public void waitForConsoleReady() throws InterruptedException {
    synchronized (commandLock) { while (_interruptCommand == null) commandLock.wait(); }
  }
  
  public boolean isConsoleReady() {
    synchronized (commandLock) { return _interruptCommand != null; }
  }
  
  public void setInputBox(JTextArea inputBox) {
    if (inputBox == null) _inputBox = new InputBox();
    else _inputBox = inputBox;
    
    InputMap im = _inputBox.getInputMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,Event.SHIFT_MASK), INSERT_NEWLINE_NAME);
    ActionMap am = _inputBox.getActionMap();
    am.put(INSERT_NEWLINE_NAME, _insertNewlineAction);
  }
  
  public JTextArea getInputBox() { return _inputBox; }
  
  public void setParent(Component c) { _parentComponent = c; }
  
  public Component getParent() { return _parentComponent; }
  
  public void setTitle(String title) {
    if (title == null) _title = "Console";
    else _title = title;
  }
  
  public String getTitle() { return _title; }
  
  /** Pops up the dialog box and creates the interrupt and insert commands, returning when the user is done inputting text.
   *  @param parentFrame the frame to set as the dialog's parent
   *  @return The text inputted by the user through the dialog box.
   */
  private String showDialog(Frame parentFrame) {
    final JDialog dialog = createDialog(_inputBox, parentFrame);
    synchronized (commandLock) {
      _interruptCommand = new Runnable() {
        public void run() { dialog.setVisible(false); }
      };
      _insertTextCommand = new Lambda<Object,String>() {
        public Object apply(String input) {
          _inputBox.insert(input, _inputBox.getCaretPosition());
          return null;
        }
      };
      commandLock.notifyAll();  // signal that console is ready to accept input
    }
    
    dialog.setVisible(true);
    dialog.dispose();
    
    synchronized (commandLock) {
      _interruptCommand = null;
      _insertTextCommand = null;
    }
    
    String input = _inputBox.getText();
    if (inputAborted) {
      inputAborted = false;
      throw new IllegalStateException("System.in aborted");
    }
    _doc.append(input + "\n", _console.SYSTEM_IN_STYLE);
    _console.append(input + "\n", _console.SYSTEM_IN_STYLE);
    return input;
  }
  
  /** Sets up the swing dialog box and its GUI items.  The input 
   *  text box is not included so that the caller of this method may
   *  add any desired listeners etc. to the TextArea.
   *  @param inputBox The text area that receives input.
   *  @param parentFrame The frame to set as the dialog's parent
   *  @return A fully decorated and functional dialog to receive input
   */
  
  private JDialog createDialog(JTextArea inputBox, Frame parentFrame) {
    
    final JDialog dialog = new JDialog(parentFrame, _title, true);
    
    inputBox.setText("");

    Container cp = dialog.getContentPane();
    cp.add(new JScrollPane(inputBox), BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JLabel label = new JLabel("<html>Enter a full line of input.<br>" +
                              "Hit SHIFT+&lt;Enter&gt; to insert a newline character.&nbsp</html>");
    buttonPanel.add(label);
    
    Action inputEnteredAction = new AbstractAction("Done") {
      public void actionPerformed(ActionEvent e) { dialog.setVisible(false); }
    };    
    
    JButton doneButton = new JButton(inputEnteredAction);
//    doneButton.setMargin(new Insets(1, 5, 1, 5));
    buttonPanel.add(doneButton);
    dialog.getRootPane().setDefaultButton(doneButton);
    
    Action inputAbortedAction = new AbstractAction("Abort") {
      public void actionPerformed(ActionEvent e) {
        inputAborted = true;
        PopupConsole.this.interruptConsole();
      }
    };
    
    JButton abortButton = new JButton(inputAbortedAction);
//    abortButton.setMargin(new Insets(1, 5, 1, 5));
    buttonPanel.add(abortButton);
    
    cp.add(buttonPanel, BorderLayout.SOUTH);
    
    inputBox.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), INPUT_ENTERED_NAME);
    inputBox.getActionMap().put(INPUT_ENTERED_NAME, inputEnteredAction);
    
    buttonPanel.setSize(500, 100);
    dialog.setSize(500, 100);
    dialog.setLocationRelativeTo(parentFrame);
    return dialog;
  }
  
  // TODO: Add popup context menu for text area 
  //       [undo/redo],cut,copy,paste,clear,select all, end input
  
  // TODO: Maybe put in an undo buffer
  
  // TODO: Have a table of previous edits that can be made visible w/ button
  //       from which you can double click to copy in the text.
  
  /** Waits for the program to input text using the <code>insertConsoleText</code>
   *  method. Returns any inputted text only when <code>interruptConsole</code> is called.
   *  @return Any text received
   */
  private String silentInput() {
    final Object monitor = new Object();
    monitorNotified = false;
    final StringBuffer input = new StringBuffer();  /* shared variable guarded by commandLock */ 
    
    synchronized (commandLock) {
      _insertTextCommand = new Lambda<Object,String>() {
        public Object apply(String s) {
          input.append(s);
          return null;
        }
      };
      
      _interruptCommand = new Runnable() {
        public void run() {
          /* This Runnable only runs inside synchronized(commandLock) */
          _insertTextCommand = null;
          _interruptCommand = null;
//          System.err.println("Ready to notify monitor");
          synchronized (monitor) { 
            monitorNotified = true;
            monitor.notify();   // wake-up the enclosing thread waiting on monitor
          }
        }
      };
      commandLock.notifyAll();  // signal that console is ready to accept input
    }
    synchronized (monitor) { 
      try { while (! monitorNotified) monitor.wait(); }   // wait until input is finished
      catch (InterruptedException e) { }
    }
    synchronized (commandLock) { return input.toString(); }
  }
  
// User InputBox class (lifted from InteractionsController) instead of following:
  
//  /** A box that can be inserted into the console box if no external JTextArea is spceified. */
//  private static class InputBox extends JTextArea {
//    private static final int BORDER_WIDTH = 1;
//    private static final int INNER_BUFFER_WIDTH = 3;
//    private static final int OUTER_BUFFER_WIDTH = 2;
//    
//    public InputBox() {
//      setBorder(_createBorder());
//      setLineWrap(true);
//    }
//    
//    private Border _createBorder() {
//      Border outerouter = BorderFactory.createLineBorder(getBackground(), OUTER_BUFFER_WIDTH);
//      Border outer = BorderFactory.createLineBorder(getForeground(), BORDER_WIDTH);
//      Border inner = BorderFactory.createLineBorder(getBackground(), INNER_BUFFER_WIDTH);
//      Border temp = BorderFactory.createCompoundBorder(outer, inner);
//      return BorderFactory.createCompoundBorder(outerouter, temp);
//    }
//  }
}
