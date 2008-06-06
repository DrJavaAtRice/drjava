/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (javaplt@rice.edu)
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

import edu.rice.cs.util.ProcessCreator;
import edu.rice.cs.util.GeneralProcessCreator;
import edu.rice.cs.util.JVMProcessCreator;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.CompletionMonitor;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.config.PropertyMaps;
import edu.rice.cs.util.BalancingStreamTokenizer;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.swing.DropDownButton;
import edu.rice.cs.util.XMLConfig;
import edu.rice.cs.drjava.model.SingleDisplayModel;

import java.io.StringReader;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ExecuteExternalDialog extends JFrame implements OptionConstants {
  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 500;
  
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
    public FrameState(ExecuteExternalDialog comp) {
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
  
  /** Run Command button. */
  private JButton _runCommandButton;
  /** Save Command button. */
  private JButton _saveCommandButton;
  /** Insert Command button. */
  private JButton _insertCommandButton;
  /** Cancel Command button. */
  private JButton _cancelCommandButton;
  /** Entered command line. */
  private JTextPane _commandLine;
  /** Command line preview label. */
  private JLabel _commandLinePreviewLabel;
  /** Command line preview. */
  private JTextPane _commandLinePreview;
  /** Java command line preview document. */
  private StyledDocument _commandLineDoc;
  /** Entered command line working directory. */
  private JTextPane _commandWorkDirLine;
  /** Command line working directory preview. */
  private JTextPane _commandWorkDirLinePreview;
  /** Command line working directory preview document. */
  private StyledDocument _commandWorkDirLineDoc;
  /** Command working directory button. */
  private JButton _commandWorkDirBtn;
  /** Last of the two text panes to have focus. */
  private JTextPane _lastCommandFocus;
  
  /** Run Java button. */
  private JButton _runJavaButton;
  /** Save Java button. */
  private JButton _saveJavaButton;
  /** Insert Command button. */
  private JButton _insertJavaButton;
  /** Cancel Java button. */
  private JButton _cancelJavaButton;
  /** Entered JVM args line document. */
  StyledDocument _jvmLineDoc;
  /** Entered JVM args line. */
  private JTextPane _jvmLine;
  /** Entered command line. */
  private JTextPane _javaCommandLine;
  /** Java command line preview. */
  private JTextPane _javaCommandLinePreview;
  /** Last of the three text panes to have focus. */
  private JTextPane _lastJavaFocus;
  /** Java command line preview document. */
  StyledDocument _javaCommandLineDoc;
  /** Entered command line working directory. */
  private JTextPane _javaCommandWorkDirLine;
  /** Command line working directory preview. */
  private JTextPane _javaCommandWorkDirLinePreview;
  /** Java command line working directory preview document. */
  private StyledDocument _javaCommandWorkDirLineDoc;
  /** Java working directory button. */
  private JButton _javaCommandWorkDirBtn;
  
  /** Style for variable the executable part. */
  SimpleAttributeSet _varCommandLineCmdStyle;
  /** Style for erroneous variable the command args part. */
  SimpleAttributeSet _varErrorCommandLineCmdStyle;
  /** Style for normal text */
  SimpleAttributeSet _commandLineCmdAS;  
  
  /** Style for the executable part. */
  Style _javaCommandLineExecutableStyle;
  /** Style for the JVM args part. */
  Style _javaCommandLineJVMStyle;
  
  /** Style for normal text */
  SimpleAttributeSet _javaCommandLineJVMAS;
  /** Style for normal text */
  SimpleAttributeSet _javaCommandLineCmdAS;
  
  /** Style for variable the executable part. */
  SimpleAttributeSet _javaVarCommandLineCmdStyle;
  /** Style for variable the JVM args part. */
  SimpleAttributeSet _javaVarCommandLineJVMStyle;
  
  /** Style for erroneous variable the JVM args part. */
  SimpleAttributeSet _javaVarErrorCommandLineJVMStyle;
  /** Style for erroneous variable the command args part. */
  SimpleAttributeSet _javaVarErrorCommandLineCmdStyle;
  
  /** Tab pane. */
  JTabbedPane _tabbedPane;
  /** Command line panel. */
  JPanel _commandPanel;
  /** Java panel. */
  JPanel _javaPanel;
  /** Command line document listener. */
  DocumentListener _documentListener;
  /** Java document listener. */
  DocumentListener _javaDocumentListener;
  /** Command line work directory document listener. */
  DocumentListener _workDirDocumentListener;
  /** Java work directory document listener. */
  DocumentListener _javaWorkDirDocumentListener;
  
  /** File chooser to open when clicking the "..." button. */
  protected DirectoryChooser _dirChooser;
  
  /** Dialog to insert variables. */
  protected InsertVariableDialog _insertVarDialog;
  
  /** Completion monitor to simulate modal behavior. */
  protected CompletionMonitor _insertVarDialogMonitor = new CompletionMonitor();
  
  /** Completion monitor to tell the calling dialog that we're done. Only used if _editMode is true. */
  private CompletionMonitor _cm;
  
  /** Main frame. */
  protected MainFrame _mainFrame;
  /** Last frame state. It can be stored and restored. */
  protected FrameState _lastState = null;
  
  /** Edit mode if true. */
  protected boolean _editMode = false;
  
  /** Index of the saved external process being edited if _editMode is true. */
  protected int _editIndex = -1;
  
  public static final String STALE_TOOLTIP = "<html>Note: Values of variables might not be current for<br>"+
    "performance reasons. They will be current when executed.</html>";
  
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
    else MainFrame.setPopupLoc(this, _mainFrame);
    validate();
  }
  
  /** Create a dialog.
    *  @param mf the instance of mainframe to query into the project
    *  @param editMode true if a saved external process is edited
    *  @param editIndex index of the saved external processes to edit
    *  @param cm completion monitor telling the calling dialog that we are done
    */
  public ExecuteExternalDialog(MainFrame mf, boolean editMode, int editIndex, CompletionMonitor cm) {
    super("Execute External Process");
    _mainFrame = mf;
    _editMode = editMode;
    _editIndex = editIndex;
    _cm = cm;
    initComponents();
    if (editMode) {
      if (editIndex>=DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT)) {
        throw new IllegalArgumentException("Trying to edit saved external process that does not exist");
      }
      final String type = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES).get(editIndex);
      final String cmdline = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES).get(editIndex);
      final String jvmargs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS).get(editIndex);
      final String workdir = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS).get(editIndex);
      if (type.equals("cmdline")) {
        _commandLine.setText(cmdline);
        _commandWorkDirLine.setText(workdir);
        _tabbedPane.remove(_javaPanel);
      }
      else if (type.equals("java")) {
        _javaCommandLine.setText(cmdline);
        _jvmLine.setText(jvmargs);
        _javaCommandWorkDirLine.setText(workdir);
        _tabbedPane.remove(_commandPanel);
      }
      else {
        throw new IllegalArgumentException("Trying to edit saved external process of unknown type");
      }
    }
  }
  
  /** Create a dialog with the "Run" button.
    *  @param mf the instance of mainframe to query into the project
    */
  public ExecuteExternalDialog(MainFrame mf) {
    this(mf, false, -1, null);
  }
  
  /** Build the dialog. */
  private void initComponents() {
    _dirChooser = new DirectoryChooser(this);
    _dirChooser.setDialogTitle("Select Work Directory");
    _dirChooser.setApproveButtonText("Select");
    
    super.getContentPane().setLayout(new GridLayout(1,1));
    
    _tabbedPane = new JTabbedPane();
    
    if (_editMode) {
      Action saveCommandAction = new AbstractAction("Save") {
        public void actionPerformed(ActionEvent e) {
          _saveCommand();
        }
      };
      _saveCommandButton = new JButton(saveCommandAction);
      Action saveJavaAction = new AbstractAction("Save") {
        public void actionPerformed(ActionEvent e) {
          _saveJava();
        }
      };
      _saveJavaButton = new JButton(saveJavaAction);
    }
    else {
      Action runCommandAction = new AbstractAction("Run Command Line") {
        public void actionPerformed(ActionEvent e) {
          _runCommand();
        }
      };
      _runCommandButton = new JButton(runCommandAction);
      Action runJavaAction = new AbstractAction("Run Java Class") {
        public void actionPerformed(ActionEvent e) {
          _runJava();
        }
      };
      _runJavaButton = new JButton(runJavaAction);
      
      Action saveCommandAction = new AbstractAction("Save to Menu...") {
        public void actionPerformed(ActionEvent e) {
          _saveCommand();
        }
      };
      _saveCommandButton = new JButton(saveCommandAction);
      Action saveJavaAction = new AbstractAction("Save to Menu...") {
        public void actionPerformed(ActionEvent e) {
          _saveJava();
        }
      };
      _saveJavaButton = new JButton(saveJavaAction);
    }
    
    _insertVarDialog = new InsertVariableDialog(_mainFrame, _insertVarDialogMonitor);
    Action insertCommandAction = new AbstractAction("Insert Variable...") {
      public void actionPerformed(ActionEvent e) {
        _insertVariableCommand();
      }
    };
    _insertCommandButton = new JButton(insertCommandAction);
    _insertCommandButton.setEnabled(false);
    Action insertJavaAction = new AbstractAction("Insert Variable...") {
      public void actionPerformed(ActionEvent e) {
        _insertVariableJava();
      }
    };
    _insertJavaButton = new JButton(insertJavaAction);
    _insertJavaButton.setEnabled(false);
    
    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        _cancel();
      }
    };
    _cancelCommandButton = new JButton(cancelAction);
    _cancelJavaButton = new JButton(cancelAction);
    
    // set up "Command Line" panel
    _commandPanel = makeCommandPane();
    _tabbedPane.addTab("Command Line", null, _commandPanel, "Execute command line process");
    
    // set up "Java" panel
    _javaPanel = makeJavaPane();
    _tabbedPane.addTab("Java Class", null, _javaPanel, "Execute Java class");
    _tabbedPane.setSelectedComponent(_commandPanel);
    
    //The following line enables to use scrolling tabs.
    _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    super.getContentPane().add(_tabbedPane);
    super.setResizable(false);
    
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    MainFrame.setPopupLoc(this, _mainFrame);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        _commandLine.requestFocus();
      }
    });
  }
  
  private JPanel makeCommandPane() {
    JPanel panel = new JPanel(new BorderLayout());
    GridBagLayout gridbag = new GridBagLayout();
    JPanel main = new JPanel(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    main.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.insets = labelInsets;
    JLabel commandLineLabel = new JLabel("Command line:");
    gridbag.setConstraints(commandLineLabel, c);
    main.add(commandLineLabel);
    
    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandLine = new JTextPane();
    // do not allow a newline
    _commandLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          e.consume();
          if (e.isShiftDown()) {
            _insertCommandButton.setEnabled(false);
            _tabbedPane.requestFocus();
          }
          else {
            _commandWorkDirLine.requestFocus();
          }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane commandLineSP = new JScrollPane(_commandLine);
    commandLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandLineSP, c);
    main.add(commandLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    _commandLinePreviewLabel = new JLabel("<html>Command line preview:<br>(0 characters)</html>");
    _commandLinePreviewLabel.setToolTipText(STALE_TOOLTIP);
    gridbag.setConstraints(_commandLinePreviewLabel, c);
    main.add(_commandLinePreviewLabel);
    
    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandLinePreview = new JTextPane();
    _commandLinePreview.setToolTipText(STALE_TOOLTIP);
    _commandLineDoc = (StyledDocument)_commandLinePreview.getDocument();
    
    // Create a style object and then set the style attributes
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _commandLineCmdAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_commandLineCmdAS, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _varErrorCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varErrorCommandLineCmdStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _commandLinePreview.setEditable(false);
    _commandLinePreview.setBackground(Color.LIGHT_GRAY);
    _commandLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane commandLinePreviewSP = new JScrollPane(_commandLinePreview);
    commandLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandLinePreviewSP, c);
    main.add(commandLinePreviewSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel workDirLabel = new JLabel("Work directory:");
    gridbag.setConstraints(workDirLabel, c);
    main.add(workDirLabel);
    
    c.weightx = 1.0;
    c.weighty = 8.0;
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = compInsets;
    
    _commandWorkDirLine = new JTextPane();
    // do not allow a newline
    _commandWorkDirLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          e.consume();
          if (e.isShiftDown()) {
            _commandLine.requestFocus();
          }
          else {
            _insertCommandButton.setEnabled(false);
            if (_editMode) {
              _saveCommandButton.requestFocus();
            }
            else {
              _runCommandButton.requestFocus();
            }
          }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane commandWorkDirLineSP = new JScrollPane(_commandWorkDirLine);
    commandWorkDirLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandWorkDirLineSP, c);
    main.add(commandWorkDirLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandWorkDirBtn = new JButton("...");
    _commandWorkDirBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseFile(_commandWorkDirLine); }
    });
    gridbag.setConstraints(_commandWorkDirBtn, c);
    main.add(_commandWorkDirBtn);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel commandWorkDirLinePreviewLabel = new JLabel("Work directory preview:");
    commandWorkDirLinePreviewLabel.setToolTipText(STALE_TOOLTIP);
    gridbag.setConstraints(commandWorkDirLinePreviewLabel, c);
    main.add(commandWorkDirLinePreviewLabel);
    
    c.weightx = 1.0;
    c.weighty = 8.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandWorkDirLinePreview = new JTextPane();
    _commandWorkDirLinePreview.setToolTipText(STALE_TOOLTIP);
    _commandWorkDirLineDoc = (StyledDocument)_commandWorkDirLinePreview.getDocument();
    
    _commandWorkDirLinePreview.setEditable(false);
    _commandWorkDirLinePreview.setBackground(Color.LIGHT_GRAY);
    _commandWorkDirLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane commandWorkDirLinePreviewSP = new JScrollPane(_commandWorkDirLinePreview);
    commandWorkDirLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandWorkDirLinePreviewSP, c);
    main.add(commandWorkDirLinePreviewSP);
    
    panel.add(main, BorderLayout.CENTER);
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    if (!_editMode) {
      bottom.add(_runCommandButton);
    }
    bottom.add(_saveCommandButton);
    bottom.add(_insertCommandButton);
    bottom.add(_cancelCommandButton);
    bottom.add(Box.createHorizontalGlue());
    panel.add(bottom, BorderLayout.SOUTH);
    
    // update the preview of the actual command line post substitution
    _documentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _commandLineDoc.remove(0,_commandLineDoc.getLength());
//          StringBuilder sb = new StringBuilder();
          String text = StringOps.replaceVariables(_commandLine.getText(), PropertyMaps.ONLY, PropertyMaps.TO_STRING);
          /* List<String> cmds = StringOps.commandLineToList(text);
           for(String s: cmds) {
           sb.append(s);
           sb.append(' ');
           } */
          _commandLineDoc.insertString(_commandLineDoc.getLength(), StringOps.unescapeSpacesWith1bHex(text), null);
          
          // command line
          colorVariables(_commandLine,
                         PropertyMaps.ONLY,
                         this,
                         _commandLineCmdAS,
                         _varCommandLineCmdStyle,
                         _varErrorCommandLineCmdStyle);
          _commandLinePreviewLabel.setText("<html>Command line preview:<br>("+_commandLinePreview.getText().length()+
                                           " characters)</html>");
        }
        catch(BadLocationException ble) {
          _commandLinePreview.setText("Error.");
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _commandLine.getDocument().addDocumentListener(_documentListener);
    _documentListener.changedUpdate(null);
    
    // update the preview of the actual command line post substitution
    _workDirDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _commandWorkDirLineDoc.remove(0,_commandWorkDirLineDoc.getLength());
          String text = StringOps.replaceVariables(_commandWorkDirLine.getText(), PropertyMaps.ONLY, PropertyMaps.TO_STRING);
          _commandWorkDirLineDoc.insertString(0, StringOps.unescapeSpacesWith1bHex(text), null);
          
          // command line
          colorVariables(_commandWorkDirLine,
                         PropertyMaps.ONLY,
                         this,
                         _commandLineCmdAS,
                         _varCommandLineCmdStyle,
                         _varErrorCommandLineCmdStyle);
        }
        catch(BadLocationException ble) {
          _commandLinePreview.setText("Error: "+ble);
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _commandWorkDirLine.getDocument().addDocumentListener(_workDirDocumentListener);
    _commandWorkDirLine.setText("${drjava.working.dir}");
    _workDirDocumentListener.changedUpdate(null);
    
    DrJava.getConfig().addOptionListener(DEFINITIONS_COMMENT_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineExecutableStyle, oce.value);
        _documentListener.changedUpdate(null);
        _workDirDocumentListener.changedUpdate(null);
      }
    });
    DrJava.getConfig().addOptionListener(DEFINITIONS_KEYWORD_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineJVMStyle, oce.value);
        _documentListener.changedUpdate(null);
        _workDirDocumentListener.changedUpdate(null);
      }
    });
    
    _lastCommandFocus = _commandLine;
    // do not allow preview to have focus
    _commandLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastCommandFocus = (JTextPane)e.getComponent();
        _insertCommandButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _commandLinePreview) || 
            (e.getOppositeComponent() == _commandWorkDirLinePreview)) {
          _commandLine.requestFocus();
        }
      }
    });
    _commandWorkDirLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastCommandFocus = (JTextPane)e.getComponent();
        _insertCommandButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _commandLinePreview) || 
            (e.getOppositeComponent() == _commandWorkDirLinePreview)) {
          _commandWorkDirLine.requestFocus();
        }
      }
    });
    
    return panel;
  }
  
  private JPanel makeJavaPane() {
    JPanel panel = new JPanel(new BorderLayout());
    GridBagLayout gridbag = new GridBagLayout();
    JPanel main = new JPanel(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    main.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel jvmArgsLabel = new JLabel("JVM arguments:");
    gridbag.setConstraints(jvmArgsLabel, c);
    main.add(jvmArgsLabel);
    
    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _jvmLine = new JTextPane();
    _jvmLineDoc = (StyledDocument)_jvmLine.getDocument();
    // do not allow a newline
    _jvmLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          e.consume();
          if (e.isShiftDown()) {
            _insertJavaButton.setEnabled(false);
            _tabbedPane.requestFocus();
          }
          else {
            _javaCommandLine.requestFocus();
          }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane jvmLineSP = new JScrollPane(_jvmLine);
    jvmLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(jvmLineSP, c);
    main.add(jvmLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandLineLabel = new JLabel("Java command line:");
    gridbag.setConstraints(javaCommandLineLabel, c);
    main.add(javaCommandLineLabel);
    
    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _javaCommandLine = new JTextPane();
    // do not allow a newline
    _javaCommandLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          e.consume();
          if (e.isShiftDown()) {
            _jvmLine.requestFocus();
          }
          else {
            _javaCommandWorkDirLine.requestFocus();
          }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane javaCommandLineSP = new JScrollPane(_javaCommandLine);
    javaCommandLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandLineSP, c);
    main.add(javaCommandLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandLinePreviewLabel = new JLabel("Command line preview:");
    javaCommandLinePreviewLabel.setToolTipText(STALE_TOOLTIP);
    gridbag.setConstraints(javaCommandLinePreviewLabel, c);
    main.add(javaCommandLinePreviewLabel);
    
    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _javaCommandLinePreview = new JTextPane();
    _javaCommandLinePreview.setToolTipText(STALE_TOOLTIP);
    _javaCommandLineDoc = (StyledDocument)_javaCommandLinePreview.getDocument();
    
    // Create a style object and then set the style attributes
    _javaCommandLineExecutableStyle = _javaCommandLineDoc.addStyle("ExecutableStyle", null);
    StyleConstants.setItalic(_javaCommandLineExecutableStyle, true);
    StyleConstants.setForeground(_javaCommandLineExecutableStyle, DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR));
    _javaCommandLineJVMStyle = _javaCommandLineDoc.addStyle("JVMStyle", null);
    StyleConstants.setForeground(_javaCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    _javaCommandLineJVMAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaCommandLineJVMAS, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    
    _javaVarCommandLineJVMStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    StyleConstants.setBackground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _javaVarErrorCommandLineJVMStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaVarErrorCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    StyleConstants.setBackground(_javaVarErrorCommandLineJVMStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _javaCommandLineCmdAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaCommandLineCmdAS, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _javaVarErrorCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarErrorCommandLineCmdStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _javaCommandLinePreview.setEditable(false);
    _javaCommandLinePreview.setBackground(Color.LIGHT_GRAY);
    _javaCommandLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane javaCommandLinePreviewSP = new JScrollPane(_javaCommandLinePreview);
    javaCommandLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandLinePreviewSP, c);
    main.add(javaCommandLinePreviewSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaWorkDirLabel = new JLabel("Work directory:");
    gridbag.setConstraints(javaWorkDirLabel, c);
    main.add(javaWorkDirLabel);
    
    c.weightx = 1.0;
    c.weighty = 12.0;
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = compInsets;
    
    _javaCommandWorkDirLine = new JTextPane();
    // do not allow a newline
    _javaCommandWorkDirLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
          e.consume();
          if (e.isShiftDown()) {
            _javaCommandLine.requestFocus();
          }
          else {
            _insertJavaButton.setEnabled(false);
            _runJavaButton.requestFocus();
          }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane javaCommandWorkDirLineSP = new JScrollPane(_javaCommandWorkDirLine);
    javaCommandWorkDirLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandWorkDirLineSP, c);
    main.add(javaCommandWorkDirLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _javaCommandWorkDirBtn = new JButton("...");
    _javaCommandWorkDirBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseFile(_javaCommandWorkDirLine); }
    });
    gridbag.setConstraints(_javaCommandWorkDirBtn, c);
    main.add(_javaCommandWorkDirBtn);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandWorkDirLinePreviewLabel = new JLabel("Work directory preview:");
    javaCommandWorkDirLinePreviewLabel.setToolTipText(STALE_TOOLTIP);
    gridbag.setConstraints(javaCommandWorkDirLinePreviewLabel, c);
    main.add(javaCommandWorkDirLinePreviewLabel);
    
    c.weightx = 1.0;
    c.weighty = 12.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _javaCommandWorkDirLinePreview = new JTextPane();
    _javaCommandWorkDirLinePreview.setToolTipText(STALE_TOOLTIP);
    _javaCommandWorkDirLineDoc = (StyledDocument)_javaCommandWorkDirLinePreview.getDocument();
    
    _javaCommandWorkDirLinePreview.setEditable(false);
    _javaCommandWorkDirLinePreview.setBackground(Color.LIGHT_GRAY);
    _javaCommandWorkDirLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane javaCommandWorkDirLinePreviewSP = new JScrollPane(_javaCommandWorkDirLinePreview);
    javaCommandWorkDirLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandWorkDirLinePreviewSP, c);
    main.add(javaCommandWorkDirLinePreviewSP);
    
    panel.add(main, BorderLayout.CENTER);
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    if (!_editMode) {
      bottom.add(_runJavaButton);
    }
    bottom.add(_saveJavaButton);
    bottom.add(_insertJavaButton);
    bottom.add(_cancelJavaButton);
    bottom.add(Box.createHorizontalGlue());
    panel.add(bottom, BorderLayout.SOUTH);
    
    // update the preview of the actual command line post substitution
    _javaDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _javaCommandLineDoc.remove(0,_javaCommandLineDoc.getLength());
          StringBuilder sb = new StringBuilder();
          sb.append(edu.rice.cs.util.newjvm.ExecJVM.getExecutable());
          sb.append(' ');
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(), sb.toString(), _javaCommandLineExecutableStyle);
          
          sb = new StringBuilder();
          String text = StringOps.replaceVariables(_jvmLine.getText(), PropertyMaps.ONLY, PropertyMaps.TO_STRING);
          List<String> cmds = StringOps.commandLineToList(text);
          for(String s: cmds) {
            sb.append(s);
            sb.append(' ');
          }
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(),
                                           StringOps.unescapeSpacesWith1bHex(sb.toString()),
                                           _javaCommandLineJVMStyle);
          
          sb = new StringBuilder();
          text = StringOps.replaceVariables(_javaCommandLine.getText(), PropertyMaps.ONLY, PropertyMaps.TO_STRING);
          cmds = StringOps.commandLineToList(text);
          for(String s: cmds) {
            sb.append(s);
            sb.append(' ');
          }
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(),
                                           StringOps.unescapeSpacesWith1bHex(sb.toString()),
                                           null);
          
          // JVM line
          colorVariables(_jvmLine,
                         PropertyMaps.ONLY,
                         this,
                         _javaCommandLineJVMAS,
                         _javaVarCommandLineJVMStyle,
                         _javaVarErrorCommandLineJVMStyle);
          
          // Java Command line
          colorVariables(_javaCommandLine,
                         PropertyMaps.ONLY,
                         this,
                         _javaCommandLineCmdAS,
                         _javaVarCommandLineCmdStyle,
                         _javaVarErrorCommandLineCmdStyle);
          
        }
        catch(BadLocationException ble) {
          _javaCommandLinePreview.setText("Error.");
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _javaCommandLine.getDocument().addDocumentListener(_javaDocumentListener);
    _jvmLine.getDocument().addDocumentListener(_javaDocumentListener);
    _javaDocumentListener.changedUpdate(null);
    
    // update the preview of the actual work dir post substitution
    _javaWorkDirDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _javaCommandWorkDirLineDoc.remove(0,_javaCommandWorkDirLineDoc.getLength());
          String text = StringOps.replaceVariables(_javaCommandWorkDirLine.getText(), PropertyMaps.ONLY, PropertyMaps.TO_STRING);
          _javaCommandWorkDirLineDoc.insertString(0, StringOps.unescapeSpacesWith1bHex(text), null);
          
          // work dir
          colorVariables(_javaCommandWorkDirLine,
                         PropertyMaps.ONLY,
                         this,
                         _javaCommandLineCmdAS,
                         _javaVarCommandLineCmdStyle,
                         _javaVarErrorCommandLineCmdStyle);
          
        }
        catch(BadLocationException ble) {
          _javaCommandWorkDirLinePreview.setText("Error: "+ble);
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _javaCommandWorkDirLine.getDocument().addDocumentListener(_javaWorkDirDocumentListener);
    _javaCommandWorkDirLine.setText("${drjava.working.dir}");
    _javaWorkDirDocumentListener.changedUpdate(null);
    
    DrJava.getConfig().addOptionListener(DEFINITIONS_COMMENT_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineExecutableStyle, oce.value);
        _javaDocumentListener.changedUpdate(null);
      }
    });
    DrJava.getConfig().addOptionListener(DEFINITIONS_KEYWORD_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineJVMStyle, oce.value);
        _javaDocumentListener.changedUpdate(null);
      }
    });
    
    _lastJavaFocus = _javaCommandLine;
    // do not allow preview to have focus
    _javaCommandLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _javaCommandLine.requestFocus();
        }
      }
    });
    _jvmLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _jvmLine.requestFocus();
        }
      }
    });
    _javaCommandWorkDirLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _javaCommandWorkDirLine.requestFocus();
        }
      }
    });
    
    return panel;
  }
  
  /** Color properties as variables.
    * @param pane the pane that contains the text
    * @param props the properties to color */
  protected void colorVariables(final JTextPane pane,
                                final PropertyMaps props,
                                final DocumentListener dl,
                                final SimpleAttributeSet normal,
                                final SimpleAttributeSet variable,
                                final SimpleAttributeSet error) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        StyledDocument doc = (StyledDocument)pane.getDocument();
        doc.removeDocumentListener(dl);
        String str = pane.getText();
        BalancingStreamTokenizer tok = new BalancingStreamTokenizer(new StringReader(str), '$');
        tok.wordRange(0,255);
        tok.addQuotes("${", "}");
        
        int pos = 0;
        doc.setCharacterAttributes(0,str.length(),normal,true);
        String next = null;
        try {
          while((next=tok.getNextToken())!=null) {
            if ((tok.token()==BalancingStreamTokenizer.Token.QUOTED) && (next.startsWith("${"))) {
              if (next.endsWith("}")) {
                String key;
                String attrList = "";
                int firstCurly = next.indexOf('}');
                int firstSemi = next.indexOf(';');
                if (firstSemi<0) {
                  key = next.substring(2,firstCurly);
                }
                else {
                  key = next.substring(2,firstSemi);
                  attrList = next.substring(firstSemi+1,next.length()-1).trim();
                }
                boolean found = false;
                for(String category: props.getCategories()) {
                  DrJavaProperty p = props.getProperty(category, key);
                  if (p!=null) {
                    found = true;
                    doc.setCharacterAttributes(pos,pos+next.length(),variable,true);
                    
                    // found property name
                    // if we have a list of attributes
                    if (attrList.length()>0) {
                      // +2 for "${", +1 for ";"
                      int subpos = pos + 2 + key.length() + 1;
                      int added = 0;
                      BalancingStreamTokenizer atok = new BalancingStreamTokenizer(new StringReader(attrList), '$');
                      atok.wordRange(0,255);
                      atok.addQuotes("${", "}");
                      atok.addQuotes("\"", "\"");
                      atok.addKeyword(";");
                      atok.addKeyword("=");
                      // LOG.log("\tProcessing AttrList");
                      String n = null;
                      while((n=atok.getNextToken())!=null) {
                        if ((n==null) || (atok.token()!=BalancingStreamTokenizer.Token.NORMAL) ||
                            n.trim().equals(";") || n.trim().equals("=") || n.trim().startsWith("\"")) {
                          doc.setCharacterAttributes(subpos,pos+next.length(),error,true);
                          break;
                        }
                        added += n.length();
                        String name = n.trim();
                        n = atok.getNextToken();
                        if ((n==null) || (atok.token()!=BalancingStreamTokenizer.Token.KEYWORD) || (!n.trim().equals("="))) {
                          doc.setCharacterAttributes(subpos,pos+next.length(),error,true);
                          break;
                        }
                        added += n.length();
                        n = atok.getNextToken();
                        if ((n==null) || (atok.token()!=BalancingStreamTokenizer.Token.QUOTED) || (!n.trim().startsWith("\""))) {
                          doc.setCharacterAttributes(subpos,pos+next.length(),error,true);
                          break;
                        }
                        added += n.length();
                        n = atok.getNextToken();
                        if (((n!=null) && ((atok.token()!=BalancingStreamTokenizer.Token.KEYWORD) || (!n.equals(";")))) ||
                            ((n==null) && (atok.token()!=BalancingStreamTokenizer.Token.END))) {
                          doc.setCharacterAttributes(subpos,pos+next.length(),error,true);
                          break;
                        }
                        if (n!=null) { added += n.length(); }
                        try { p.getAttribute(name); }
                        catch(IllegalArgumentException e) { doc.setCharacterAttributes(subpos,subpos+added,error,true); }
                        subpos += added;
                      }
                    }
                  }
                  if (found) break;
                }
                if (!found) doc.setCharacterAttributes(pos,pos+next.length(),error,true);
              }
              else doc.setCharacterAttributes(pos,pos+next.length(),error,true);
            }
            else doc.setCharacterAttributes(pos,pos+next.length(),normal,true);
            pos += next.length();
          }
        }
        catch(IOException e) { /* ignore  */ }
        finally { doc.addDocumentListener(dl); }
      }
    });
  }
  
  /** Method that handels the Cancel button */
  private void _cancel() {
    _lastState = new FrameState(this);
    this.setVisible(false);
    if (_cm!=null) { _cm.set(); }
  }
  
  // public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("process.txt", false);
  
  /** Run a command and return an external process panel. */
  public ExternalProcessPanel runCommand(String name, String cmdline, String workdir) {
    ProcessCreator pc = new GeneralProcessCreator(cmdline, workdir.trim());
    String label = "External";
    if (!name.equals("")) { label += ": "+name; }
    final ExternalProcessPanel panel = new ExternalProcessPanel(_mainFrame, label, pc);
    _mainFrame._tabs.addLast(panel);
    panel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _mainFrame._lastFocusOwner = panel; }
    });
    panel.setVisible(true);
    _mainFrame.showTab(panel);
    _mainFrame._tabbedPane.setSelectedComponent(panel);
    // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
    EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
    return panel;
  }
  
  public ExternalProcessPanel runJava(String name, String jvmargs, String cmdline, String workdir) {
    ProcessCreator pc = new JVMProcessCreator(jvmargs, cmdline, workdir);
    
    String label = "External Java";
    if (!name.equals("")) { label += ": "+name; }
    final ExternalProcessPanel panel = new ExternalProcessPanel(_mainFrame, label, pc);
    _mainFrame._tabs.addLast(panel);
    panel.getMainPanel().addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) { _mainFrame._lastFocusOwner = panel; }
    });
    panel.setVisible(true);
    _mainFrame.showTab(panel);
    _mainFrame._tabbedPane.setSelectedComponent(panel);
    // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
    EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
    
    return panel;
  }
  
  /** Execute the command line. */
  private void _runCommand() {
    _mainFrame.updateStatusField("Executing external process...");
    
    _mainFrame.removeModalWindowAdapter(this);
    if (_commandLinePreview.getText().length()>0) {
      runCommand("", _commandLine.getText(), _commandWorkDirLine.getText());
    }
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
    if (_cm!=null) { _cm.set(); }    
  }
  
  /** Execute the Java class. */
  private void _runJava() {
    _mainFrame.updateStatusField("Executing external Java class...");
    
    _mainFrame.removeModalWindowAdapter(this);
    if (_javaCommandLinePreview.getText().length()>0) {
      runJava("", _jvmLine.getText(), _javaCommandLine.getText(), _javaCommandWorkDirLine.getText());
    }
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
    if (_cm!=null) { _cm.set(); }
  }
  
  /** Save the command line to the menu. */
  private void _saveCommand() {          
    if (_editMode) {
      final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
      _mainFrame.removeModalWindowAdapter(this);
      String name = JOptionPane.showInputDialog(this, "Name for saved process:", names.get(_editIndex));
      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      if (name==null) {
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        if (_cm!=null) { _cm.set(); }
        return;
      }
      editInMenu(_editIndex, name, "cmdline", _commandLine.getText(), "", _commandWorkDirLine.getText());
    }
    else {
      int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
      _mainFrame.removeModalWindowAdapter(this);
      String name = JOptionPane.showInputDialog(this, "Name for saved process:", "External Java "+(count+1));
      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      if (name==null) {
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        if (_cm!=null) { _cm.set(); }
        return;
      }
      addToMenu(name, "cmdline", _commandLine.getText(), "", _commandWorkDirLine.getText());
    }
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
    if (_cm!=null) { _cm.set(); }
  }
  
  /** Save the Java class to the menu. */
  private void _saveJava() {
    if (_editMode) {
      final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
      _mainFrame.removeModalWindowAdapter(this);
      String name = JOptionPane.showInputDialog(this, "Name for saved process:", names.get(_editIndex));
      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      if (name==null) {
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        if (_cm!=null) { _cm.set(); }
        return;
      }
      editInMenu(_editIndex, name, "java", _javaCommandLine.getText(),
                 _jvmLine.getText(), _javaCommandWorkDirLine.getText());
    }
    else {
      int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
      _mainFrame.removeModalWindowAdapter(this);
      String name = JOptionPane.showInputDialog(this, "Name for saved process:", "External Java "+(count+1));
      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      if (name==null) {
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        if (_cm!=null) { _cm.set(); }
        return;
      }
      addToMenu(name, "java", _javaCommandLine.getText(),
                _jvmLine.getText(), _javaCommandWorkDirLine.getText());
    }
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
    if (_cm!=null) { _cm.set(); }
  }
  
  /** Add new process to menu.
    * @param name process name
    * @param type type of the process, "cmdline" or "java"
    * @param cmdline command line
    * @param jvmarg arguments for the new JVM, if type is "java", else ""
    * @param workdir work directory
    * @return number of processes in the menu */
  public static int addToMenu(String name, String type, String cmdline, String jvmarg, String workdir) {
    int count = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_COUNT);
    ++count;
    final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    final Vector<String> types = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES);
    final Vector<String> cmdlines = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    final Vector<String> jvmargs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS);
    final Vector<String> workdirs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    
    names.add(name);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,names);
    
    types.add(type);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_TYPES,types);
    
    cmdlines.add(cmdline);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES,cmdlines);
    
    jvmargs.add(jvmarg);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS,jvmargs);
    
    workdirs.add(workdir);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS,workdirs);
    
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_COUNT,count);
    
    return count;
  }
  
  /** Edit existing process in menu.
    * @param editIndex the index of the process to edit
    * @param name process name
    * @param type type of the process, "cmdline" or "java"
    * @param cmdline command line
    * @param jvmarg arguments for the new JVM, if type is "java", else ""
    * @param workdir work directory */
  public static void editInMenu(int editIndex, String name, String type, String cmdline, String jvmarg, String workdir) {
    final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    final Vector<String> types = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES);
    final Vector<String> cmdlines = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    final Vector<String> jvmargs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS);
    final Vector<String> workdirs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    
    names.set(editIndex,name);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,names);
    
    types.set(editIndex,type);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_NAMES,names);
    
    cmdlines.set(editIndex,cmdline);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES,cmdlines);
    
    jvmargs.set(editIndex,jvmarg);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS,jvmargs);      
    
    workdirs.set(editIndex,workdir);
    DrJava.getConfig().setSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS,workdirs);
  }
  
  /** Save process to file.
    * @param index index of the process to save
    * @param f file */
  public static void saveToFile(int index, File f) {
    final Vector<String> names = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_NAMES);
    final Vector<String> types = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_TYPES);
    final Vector<String> cmdlines = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_CMDLINES);
    final Vector<String> jvmargs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_JVMARGS);
    final Vector<String> workdirs = DrJava.getConfig().getSetting(OptionConstants.EXTERNAL_SAVED_WORKDIRS);
    
    XMLConfig xc = new XMLConfig();
    System.out.println("saveToFile("+index+", "+f+")");
    System.out.println("\t"+names.get(index));
    xc.set("drjava/extprocess/name", names.get(index));
    xc.set("drjava/extprocess/type", types.get(index));
    xc.set("drjava/extprocess/cmdline", cmdlines.get(index));
    xc.set("drjava/extprocess/jvmline", jvmargs.get(index));
    xc.set("drjava/extprocess/workdir", workdirs.get(index));
    xc.save(f);
  }
  
  /** Save the settings for this dialog. */
  private boolean _saveSettings() {
    _lastState = new FrameState(this);
    return true;
  }
  
  /** Insert a variable into the command line. */
  private void _insertVariableCommand() {
    PropertyMaps.ONLY.clearVariables();
    _mainFrame.removeModalWindowAdapter(this);
    _insertVarDialogMonitor.reset();
    _insertVarDialog.setVisible(true);
    // start a new thread to wait for the dialog to finish
    // this waiting cannot happen in the event thread, as that would block the other dialog
    new Thread(new Runnable() {
      public void run() {
        _insertVarDialogMonitor.waitOne();
        // dialog has finished, figure out the results in the event thread
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                ExecuteExternalDialog.this.toFront();
              }
            });
            _mainFrame.installModalWindowAdapter(ExecuteExternalDialog.this, NO_OP, CANCEL);

            edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty> selected = _insertVarDialog.getSelected();
            if (selected!=null) {
              String text = _lastCommandFocus.getText();
              Caret caret = _lastCommandFocus.getCaret();
              int min = Math.min(caret.getDot(), caret.getMark());
              int max = Math.max(caret.getDot(), caret.getMark());
              if (min != max) {
                text = text.substring(0, min) + text.substring(max);
              }
              text = text.substring(0,min) + "${" + selected.first() + "}" + text.substring(min);
              _lastCommandFocus.setText(text);
              caret.setDot(min+selected.first().length()+2);
              _lastCommandFocus.setCaret(caret);
            }
          }
        });
      }
    }).start();
  }
  
  /** insert a variable Java class command line. */
  private void _insertVariableJava() {
    _mainFrame.removeModalWindowAdapter(this);
    _insertVarDialogMonitor.reset();
    _insertVarDialog.setVisible(true);
    // start a new thread to wait for the dialog to finish
    // this waiting cannot happen in the event thread, as that would block the other dialog
    new Thread(new Runnable() {
      public void run() {
        _insertVarDialogMonitor.waitOne();
        // dialog has finished, figure out the results in the event thread
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                ExecuteExternalDialog.this.toFront();
              }
            });
            _mainFrame.installModalWindowAdapter(ExecuteExternalDialog.this, NO_OP, CANCEL);

            edu.rice.cs.plt.tuple.Pair<String,DrJavaProperty> selected = _insertVarDialog.getSelected();
            if (selected!=null) {
              String text = _lastJavaFocus.getText();
              Caret caret = _lastJavaFocus.getCaret();
              int min = Math.min(caret.getDot(), caret.getMark());
              int max = Math.max(caret.getDot(), caret.getMark());
              if (min != max) {
                text = text.substring(0, min) + text.substring(max);
              }
              text = text.substring(0,min) + "${" + selected.first() + "}" + text.substring(min);
              _lastJavaFocus.setText(text);
              caret.setDot(min+selected.first().length()+2);
              _lastJavaFocus.setCaret(caret);
            }
          }
        });
      }
    }).start();
  }
  
  /** Lambda doing nothing. */
  protected final edu.rice.cs.util.Lambda<Void,WindowEvent> NO_OP 
    = new edu.rice.cs.util.Lambda<Void,WindowEvent>() {
    public Void apply(WindowEvent e) {
      return null;
    }
  };
  
  /** Lambda that calls _cancel. */
  protected final edu.rice.cs.util.Lambda<Void,WindowEvent> CANCEL
    = new edu.rice.cs.util.Lambda<Void,WindowEvent>() {
    public Void apply(WindowEvent e) {
      _cancel();
      return null;
    }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      _mainFrame.hourglassOn();
      _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);
      _documentListener.changedUpdate(null);
      _workDirDocumentListener.changedUpdate(null);
      _javaDocumentListener.changedUpdate(null);
      _javaWorkDirDocumentListener.changedUpdate(null);
      toFront();
    }
    else {
      _mainFrame.removeModalWindowAdapter(this);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
  
  /** Opens the file chooser to select a file, putting the result in the file field. */
  protected void chooseFile(JTextPane pane) {
    // Get the file from the chooser
    File wd = new File(StringOps.replaceVariables(pane.getText().trim(), PropertyMaps.ONLY, PropertyMaps.GET_CURRENT));
    if ((pane.getText().equals("")) ||
        (!wd.exists()) &&
        (!wd.isDirectory())) {
      wd = new File(System.getProperty("user.dir"));
    }
    
    _dirChooser.setSelectedFile(wd);
    _mainFrame.removeModalWindowAdapter(this);
    int returnValue = _dirChooser.showDialog(wd);
    _mainFrame.installModalWindowAdapter(this, NO_OP, CANCEL);      
    if (returnValue == DirectoryChooser.APPROVE_OPTION) {
      File chosen = _dirChooser.getSelectedDirectory();
      if (chosen != null) { pane.setText(chosen.toString()); };
    }
  }
}
