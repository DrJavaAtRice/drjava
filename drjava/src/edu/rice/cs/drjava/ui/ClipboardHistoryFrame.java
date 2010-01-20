/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

/** Frame with history of clipboard. */
public class ClipboardHistoryFrame extends SwingFrame {
  /** Interface for an action to be performed when the user closes the frame,
   *  either by using "OK" or "Cancel".
   */
  public static interface CloseAction extends Lambda<String, Object> { }
  
  /** Class to save the frame state, i.e. location and dimensions.*/
  public static class FrameState {
    private Dimension _dim;
    private Point _loc;
    public FrameState(Dimension d, Point l) {
      _dim = d;
      _loc = l;
    }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        _dim = new Dimension(x, y);
        x = Integer.valueOf(tok.nextToken());
        y = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(ClipboardHistoryFrame comp) {
      _dim = comp.getSize();
      _loc = comp.getLocation();
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append((int)_dim.getWidth());
      sb.append(' ');
      sb.append((int)_dim.getHeight());
      sb.append(' ');
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      return sb.toString();
    }
    public Dimension getDimension() { return _dim; }
    public Point getLocation() { return _loc; }
  }

  /** Clipboard history model */
  private ClipboardHistoryModel _chm;

  /** Code for the last button that was pressed.*/
  private int _buttonPressed;

  /** Ok button.*/
  private JButton _okButton;
  
  /** Cancel button. */
  private JButton _cancelButton;

  /** List with history. */
  private JList _historyList;

  /** Text area for that previews the history content. */
  private JTextArea _previewArea;
  
  /** Last frame state. It can be stored and restored. */
  private FrameState _lastState = null;
  
  /** Owner frame. */
  private MainFrame _mainFrame;
  
  /** Close actions for ok and cancel button. */
  private CloseAction _okAction, _cancelAction;
  
  /** Create a new clipboard history frame.
   *  @param owner owner frame
   *  @param title dialog title
   *  @param chm the clipboard history model
   *  @param okAction the action to perform when OK is clicked
   *  @param cancelAction the action to perform when Cancel is clicked
   */
  public ClipboardHistoryFrame(MainFrame owner, String title, ClipboardHistoryModel chm,
                               CloseAction okAction, CloseAction cancelAction) {
    super(title);
    _chm = chm;
    _mainFrame = owner;
    _okAction = okAction;
    _cancelAction = cancelAction;
    init();
    initDone();  // call mandated by SwingFrame contract
  }
  
  /** Returns the last state of the frame, i.e. the location and dimension.
   *  @return frame state
   */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param ds  State to update to, or {@code null} to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      validate();
    }
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param s  State to update to, or {@code null} to reset
   */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
    if (_lastState != null) {
      setSize(_lastState.getDimension());
      setLocation(_lastState.getLocation());
      validate();
    }
    else {
      Dimension parentDim = (_mainFrame != null) ? _mainFrame.getSize() : getToolkit().getScreenSize();
      int xs = (int)parentDim.getWidth()/3;
      int ys = (int)parentDim.getHeight()/4;
      setSize(Math.max(xs,400), Math.max(ys, 400));
      Utilities.setPopupLoc(this, _mainFrame);
    }
  }

  /** Return the code for the last button that was pressed. This will be either JOptionPane.OK_OPTION or 
   *  JOptionPane.CANCEL_OPTION.
   *  @return button code
   */
  public int getButtonPressed() {
    return _buttonPressed;
  }

  /** Initialize the frame. */
  private void init() {
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        validate();
        _historyList.ensureIndexIsVisible(_historyList.getSelectedIndex());
      }
    });
    
    JRootPane rootPane = this.getRootPane();
    InputMap iMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
    
    ActionMap aMap = rootPane.getActionMap();
    aMap.put("escape", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cancelButtonPressed();
      }
    });

    _historyList = new JList();
    _historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _historyList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updatePreview();
      }
    });
    _historyList.setFont(DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN));
    _historyList.setCellRenderer(new DefaultListCellRenderer()  {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        c.setForeground(DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR));
        return c;
      }
    });
    _historyList.addFocusListener(new FocusAdapter() {

      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() != _previewArea) && 
            (e.getOppositeComponent() != _okButton) && 
            (e.getOppositeComponent() != _cancelButton)) {
          _historyList.requestFocus();
        }
      }
    });

    // buttons
    _okButton = new JButton("OK");
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButtonPressed();
      }
    });

    _cancelButton = new JButton("Cancel");
    _cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButtonPressed();
      }
    });
        
    // put everything together
    Container contentPane = getContentPane();
    
    GridBagLayout layout = new GridBagLayout();
    contentPane.setLayout(layout);
    
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER; // end row
    c.insets.top = 2;
    c.insets.left = 2;
    c.insets.bottom = 2;
    c.insets.right = 2;

    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1.0;
    contentPane.add(new JScrollPane(_historyList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
                    c);
    
    _previewArea = new JTextArea("");
    _previewArea.setEditable(false);
    _previewArea.setDragEnabled(false);
    _previewArea.setEnabled(false);
    _previewArea.setFont(DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN));
    _previewArea.setDisabledTextColor(DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR));
    c.weighty = 2.0;
    contentPane.add(new JScrollPane(_previewArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 
                    c);
    
    c.anchor = GridBagConstraints.SOUTH;
    
    JPanel buttonPanel = new JPanel(new GridBagLayout());
    GridBagConstraints bc = new GridBagConstraints();
    bc.insets.left = 2;
    bc.insets.right = 2;
    buttonPanel.add(_okButton, bc);
    buttonPanel.add(_cancelButton, bc);
    
    c.weighty = 0.0;
    contentPane.add(buttonPanel, c);

    Dimension parentDim = (_mainFrame != null) ? _mainFrame.getSize() : getToolkit().getScreenSize();
    int xs = (int)parentDim.getWidth()/3;
    int ys = (int)parentDim.getHeight()/4;
    setSize(Math.max(xs,400), Math.max(ys, 300));
    Utilities.setPopupLoc(this, _mainFrame);

    updateView();
  }
  
  protected WindowAdapter _windowListener = new WindowAdapter() {
    public void windowDeactivated(WindowEvent we) {
      ClipboardHistoryFrame.this.toFront();
    }
    public void windowClosing(WindowEvent we) {
      cancelButtonPressed();
    }
  };
  
  /** Validates before changing visibility.  Only runs in the event thread.
    * @param vis true if frame should be shown, false if it should be hidden.
    */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      updateView();
      _historyList.requestFocus();
      toFront();
    }
    else {
      removeWindowFocusListener(_windowListener);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }

  /** Update the displays based on the model. */
  private void updateView() {
    List<String> strs = _chm.getStrings();
    ListItem[] arr = new ListItem[strs.size()];
    for(int i = 0; i < strs.size(); ++i) arr[strs.size()-i-1] = new ListItem(strs.get(i));
    _historyList.setListData(arr);
    if (_historyList.getModel().getSize() > 0) {
      _historyList.setSelectedIndex(0);
      getRootPane().setDefaultButton(_okButton);
      _okButton.setEnabled(true);
    }
    else {
      getRootPane().setDefaultButton(_cancelButton);
      _okButton.setEnabled(false);
    }
    updatePreview();
  }

  /** Update the preview area based on the model. */
  private void updatePreview() {
    String text = "";
    if (_historyList.getModel().getSize() > 0) {
      int index = _historyList.getSelectedIndex();
      if (index != -1) {
        text = ((ListItem)_historyList.getModel().getElementAt(_historyList.getSelectedIndex())).getFull();
      }
    }

    _previewArea.setText(text);
    _previewArea.setCaretPosition(0);
  }
  
  /** Handle OK button. */
  private void okButtonPressed() {
    _lastState = new FrameState(ClipboardHistoryFrame.this);
    setVisible(false);
    if (_historyList.getModel().getSize() > 0) {
      _buttonPressed = JOptionPane.OK_OPTION;
      String s = ((ListItem)_historyList.getModel().getElementAt(_historyList.getSelectedIndex())).getFull();
      _chm.put(s);
      _okAction.value(s);
    }
    else {
      _buttonPressed = JOptionPane.CANCEL_OPTION;
      Toolkit.getDefaultToolkit().beep();
      _cancelAction.value(null);
    }
  }
  
  /** Handle cancel button. */
  private void cancelButtonPressed() {
    _buttonPressed = JOptionPane.CANCEL_OPTION;
    _lastState = new FrameState(ClipboardHistoryFrame.this);
    setVisible(false);
    _cancelAction.value(null);
  }
  
  /** Keeps a full string, but toString is only the first line. */
  private static class ListItem {
    private String full, display;
    public ListItem(String s) {
      full = s;
      int index1 = s.indexOf('\n');
      if (index1 == -1) index1 = s.length();
      int index2 = s.indexOf(StringOps.EOL);
      if (index2 == -1) index2 = s.length();
      display = s.substring(0, Math.min(index1, index2));
    }
    public String getFull() { return full; }
    public String toString() { return display; }
//    public boolean equals(Object o) {
//      if (o == null || getClass() != o.getClass()) return false;
//      return full.equals(((ListItem)o).full);
//    }
//    public int hashCode() { return  (full != null ? full.hashCode() : 0); }
  }
}
