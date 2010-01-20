/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (javaplt@rice.edu)
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

import java.awt.EventQueue;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.DropDownButton;
import edu.rice.cs.util.swing.SwingFrame;

import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.LambdaUtil;



public class EditExternalDialog extends SwingFrame implements OptionConstants {
  private static final int FRAME_WIDTH = 503;
  private static final int FRAME_HEIGHT = 318;
  
  /** Class to save the frame state, i.e. location. */
  public static class FrameState {
    private Point _loc;
    public FrameState(Point l) {
      _loc = l;
    }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(EditExternalDialog comp) {
      _loc = comp.getLocation();
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      return sb.toString();
    }
    public Point getLocation() { return _loc; }
  }
  
  /** Edit button. */
  private JButton _editButton;
  /** Remove button. */
  private JButton _removeButton;
  /** Move up button. */
  private JButton _upButton;
  /** Move down button. */
  private JButton _downButton;
  /** Import button. */
  private JButton _importButton;
  /** Export button. */
  private JButton _exportButton;
  /** Move up action. */
  private Action _upAction;
  /** Move down action. */
  private Action _downAction;
  /** Import action. */
  private Action _importAction;
  /** Export action. */
  private Action _exportAction;
  /** Drop-down button for additional commands. */
  private DropDownButton _dropDownButton;
  /** Ok button. */
  private JButton _okButton;
  /** List of commands. */
  private JList _list;
  /** Completion monitor to simulate modal behavior. */
  protected CompletionMonitor _editExternalDialogMonitor = new CompletionMonitor();
  
  /** Filter for drjava external process files (.djapp) */
  private final FileFilter _extProcFilter = new javax.swing.filechooser.FileFilter() {
    public boolean accept(File f) {
      return f.isDirectory() || 
        f.getPath().endsWith(OptionConstants.EXTPROCESS_FILE_EXTENSION);
    }
    public String getDescription() { 
      return "DrJava External Process Files (*" + EXTPROCESS_FILE_EXTENSION + ")";
    }
  };

  /** Filter for drjava project files (.djapp only) */
  private final FileFilter _saveExtProcFilter = new javax.swing.filechooser.FileFilter() {
    public boolean accept(File f) {
      return f.isDirectory() || 
        f.getPath().endsWith(OptionConstants.EXTPROCESS_FILE_EXTENSION);
    }
    public String getDescription() { 
      return "DrJava External Process Files (*" + PROJECT_FILE_EXTENSION + ")";
    }
  };

  /** For opening files.  We have a persistent dialog to keep track of the last directory from which we opened. */
  private JFileChooser _importChooser;

  /** For saving files. We have a persistent dialog to keep track of the last directory from which we saved. */
  private JFileChooser _exportChooser;

  /** Main frame. */
  protected MainFrame _mainFrame;

  /** Last frame state. It can be stored and restored. */
  protected FrameState _lastState = null;
  
  /** Returns the last state of the frame, i.e. the location and dimension.
   *  @return frame state
   */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param ds State to update to, or {@code null} to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
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
    if (_lastState != null) setLocation(_lastState.getLocation());
    else edu.rice.cs.util.swing.Utilities.setPopupLoc(this, _mainFrame);
    validate();
  }

  /** Create a dialog.
   *  @param mf the instance of mainframe to query into the project
   */
  public EditExternalDialog(MainFrame mf) {
    super("Edit External Processes");
    _mainFrame = mf;
    initComponents();
    initDone();   // call mandated by SwingFrame contract
  }

  /** Build the dialog. */
  private void initComponents() {
    super.getContentPane().setLayout(new GridLayout(1,1));

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());
    
    Action editAction = new AbstractAction("Edit") {
      public void actionPerformed(ActionEvent e) {
        _edit();
      }
    };
    _editButton = new JButton(editAction);
    Action removeAction = new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent e) {
        _remove();
      }
    };
    _removeButton = new JButton(removeAction);

    _dropDownButton = new DropDownButton();
    _upAction = new AbstractAction("Move Up") {
      public void actionPerformed(ActionEvent e) {
        _up();
      }
    };
    _downAction = new AbstractAction("Move Down") {
      public void actionPerformed(ActionEvent e) {
        _down();
      }
    };
    
    _importAction = new AbstractAction("Import...") {
      public void actionPerformed(ActionEvent e) {
        _import();
      }
    };
    _exportAction = new AbstractAction("Export...") {
      public void actionPerformed(ActionEvent e) {
        _export();
      }
    };
    
    _importButton = new JButton(_importAction);
    _exportButton = new JButton(_exportAction);
    _upButton = new JButton(_upAction);
    _downButton = new JButton(_downAction);

    _dropDownButton.getPopupMenu().add(_importAction);
    _dropDownButton.getPopupMenu().add(_exportAction);
    
    _dropDownButton.setIcon(new ImageIcon(getClass().getResource("/edu/rice/cs/drjava/ui/icons/Down16.gif")));

    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        _ok();
      }
    };
    _okButton = new JButton(okAction);
    
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_editButton);
    bottom.add(_removeButton);
    bottom.add(_upButton);
    bottom.add(_downButton);
    bottom.add(_dropDownButton);
    bottom.add(_okButton);
    bottom.add(Box.createHorizontalGlue());
    mainPanel.add(bottom, BorderLayout.SOUTH);

    _list = new JList();
    _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _list.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        _upButton.setEnabled(_list.getSelectedIndex() > 0);
        _upAction.setEnabled(_list.getSelectedIndex() > 0);
        _downButton.setEnabled(_list.getSelectedIndex() < _list.getModel().getSize());
        _downAction.setEnabled(_list.getSelectedIndex() < _list.getModel().getSize());
      }
    });
    JScrollPane sp = new JScrollPane(_list);
    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    mainPanel.add(sp, BorderLayout.CENTER);
    updateList(0);
    
    super.getContentPane().add(mainPanel);
    super.setResizable(false);
    
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    edu.rice.cs.util.swing.Utilities.setPopupLoc(this, _mainFrame);
    
    _importChooser = new JFileChooser() {
      public void setCurrentDirectory(File dir) {
        //next two lines are order dependent!
        super.setCurrentDirectory(dir);
        setDialogTitle("Import:  " + getCurrentDirectory());
      }
    };
    _importChooser.setPreferredSize(new Dimension(650, 410));
    _importChooser.setFileFilter(_extProcFilter);
    _importChooser.setMultiSelectionEnabled(false);

    _exportChooser = new JFileChooser() {
      public void setCurrentDirectory(File dir) {
        //next two lines are order dependent!
        super.setCurrentDirectory(dir);
        setDialogTitle("Export:  " + getCurrentDirectory());
      }
    };
    _exportChooser.setPreferredSize(new Dimension(650, 410));
    _exportChooser.setFileFilter(_saveExtProcFilter);
  }

  /** Method that handels the OK button */
  private void _ok() {
    _lastState = new FrameState(this);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT,
                                  DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT));
    this.setVisible(false);
  }
  
  /** Edit a command. */
  private void _edit() {
    final int selectedIndex = _list.getSelectedIndex();
    if ((selectedIndex < 0) || (selectedIndex>=DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT))) {
      return;
    }
    _mainFrame.removeModalWindowAdapter(this);
    _editExternalDialogMonitor.reset();
    final ExecuteExternalDialog dialog = new ExecuteExternalDialog(_mainFrame,true,selectedIndex,_editExternalDialogMonitor);
    dialog.setVisible(true);
    // start a new thread to wait for the dialog to finish
    // this waiting cannot happen in the event thread, as that would block the other dialog
    new Thread(new Runnable() {
      public void run() {
        _editExternalDialogMonitor.attemptEnsureSignaled();
        // dialog has finished, figure out the results in the event thread
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            EventQueue.invokeLater(new Runnable() { public void run() { EditExternalDialog.this.toFront(); } });
            _mainFrame.installModalWindowAdapter(EditExternalDialog.this, LambdaUtil.NO_OP, OK);
            updateList(selectedIndex);
          }
        });
      }
    }).start();
  }

  /** Method that handels the remove button */
  private void _remove() {
    int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
    final int selectedIndex = _list.getSelectedIndex();
    if ((selectedIndex < 0) ||
        (selectedIndex>=DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)) ||
        (count<=0)) {
      _removeButton.setEnabled(false);
      return;
    }

    Vector<String> v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    v.remove(selectedIndex);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    v.remove(selectedIndex);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    v.remove(selectedIndex);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES);
    v.remove(selectedIndex);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES,v);

    --count;
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT, count);
    updateList(Math.max(0, selectedIndex-1));
  }

  /** Method that handels the up button */
  private void _up() {
    final int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
    final int selectedIndex = _list.getSelectedIndex();
    if ((selectedIndex<1) ||
        (selectedIndex>=DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)) ||
        (count<=0)) {
      _removeButton.setEnabled(false);
      return;
    }

    Vector<String> v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    String s = v.remove(selectedIndex);
    v.add(selectedIndex-1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,v);
    
    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    s = v.remove(selectedIndex);
    v.add(selectedIndex-1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    s = v.remove(selectedIndex);
    v.add(selectedIndex-1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES);
    s = v.remove(selectedIndex);
    v.add(selectedIndex-1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES,v);

    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT, count);
    updateList(Math.max(0,selectedIndex-1));
  }

  /** Method that handels the down button */
  private void _down() {
    final int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
    final int selectedIndex = _list.getSelectedIndex();
    if ((selectedIndex < 0) ||
        (selectedIndex>=DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)-1) ||
        (count<=0)) {
      _removeButton.setEnabled(false);
      return;
    }

    Vector<String> v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    String s = v.remove(selectedIndex);
    v.add(selectedIndex+1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,v);
    
    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    s = v.remove(selectedIndex);
    v.add(selectedIndex+1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    s = v.remove(selectedIndex);
    v.add(selectedIndex+1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS,v);

    v = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES);
    s = v.remove(selectedIndex);
    v.add(selectedIndex+1,s);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_ENCLOSING_DJAPP_FILES,v);

    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT, count);
    updateList(Math.max(0,selectedIndex+1));
  }
  
  /** Import process. */
  public void _import() {
    _mainFrame.removeModalWindowAdapter(this);
    int rc = _importChooser.showOpenDialog(this);
    _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, OK);
    switch (rc) {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION: {
        return;
      }
      
      case JFileChooser.APPROVE_OPTION: {
        File[] chosen = _importChooser.getSelectedFiles();
        if (chosen == null) {
          return;
        } 
        // If this is a single-selection dialog, getSelectedFiles() will always
        // return a zero-size array -- handle it differently.
        if (chosen.length == 0) {
          File f = _importChooser.getSelectedFile();
          MainFrame.openExtProcessFile(f);
          updateList(DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)-1);
        }
        return;
      }
        
      default: // impossible since rc must be one of these
        throw new UnexpectedException();
    }
  }
  
  /** Export process. */
  public void _export() {
    //System.out.println("_export()");
    _exportChooser.setMultiSelectionEnabled(false);
    _mainFrame.removeModalWindowAdapter(this);
    int rc = _exportChooser.showSaveDialog(this);
    _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, OK);
    switch (rc) {
      case JFileChooser.CANCEL_OPTION:
      case JFileChooser.ERROR_OPTION: {
        //System.out.println("\tcancel/error, rc=" + rc);
        return;
      }
      
      case JFileChooser.APPROVE_OPTION: {
        //System.out.println("\tapprove, rc=" + rc);
        File[] chosen = _exportChooser.getSelectedFiles();
        if (chosen == null) {
          //System.out.println("\tchosen=null");
          return;
        } 
        //System.out.println("\tchosen.length=" + chosen.length);
        // If this is a single-selection dialog, getSelectedFiles() will always
        // return a zero-size array -- handle it differently.
        if (chosen.length == 0) {
          File f = _exportChooser.getSelectedFile();
          if (!f.getName().endsWith(OptionConstants.EXTPROCESS_FILE_EXTENSION)) {
            f = new File(f.getAbsolutePath()+OptionConstants.EXTPROCESS_FILE_EXTENSION);
          }
          //System.out.println("\tindex=" + _list.getSelectedIndex() + ", file=" + f);
          ExecuteExternalDialog.saveToFile(_list.getSelectedIndex(), f);
        }
        return;
      }
        
      default: // impossible since rc must be one of these
        throw new UnexpectedException();
    }
  }
  
  /** Update the properties. */
  public void updateList(int selectedIndex) {
    final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    _list.setListData(names);
    _editButton.setEnabled(names.size() > 0);
    _removeButton.setEnabled(names.size() > 0);
    if (names.size() > 0) {
      _list.setSelectedIndex(selectedIndex);
    }
    else {
      _list.clearSelection();
    }
    _upButton.setEnabled((_list.getModel().getSize() > 0) &&
                         (_list.getSelectedIndex() > 0));
    _upAction.setEnabled((_list.getModel().getSize() > 0) &&
                         (_list.getSelectedIndex() > 0));
    _downButton.setEnabled((_list.getModel().getSize() > 0) &&
                           (_list.getSelectedIndex() < _list.getModel().getSize()-1));
    _downAction.setEnabled((_list.getModel().getSize() > 0) &&
                           (_list.getSelectedIndex() < _list.getModel().getSize()-1));
    _exportButton.setEnabled(names.size() > 0);
    _exportAction.setEnabled(names.size() > 0);
  }

  /** Lambda that calls _ok. */
  protected final Runnable1<WindowEvent> OK = new Runnable1<WindowEvent>() {
    public void run(WindowEvent e) { _ok(); }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      updateList(0);
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, LambdaUtil.NO_OP, OK);
      toFront();
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
}
