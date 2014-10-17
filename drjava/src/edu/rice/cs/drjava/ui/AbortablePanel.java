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

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.model.SingleDisplayModel;

/** Panel for displaying some component with buttons, one of which is an "Abort" button.
  * This should be used to display the output of an external process.
  * This class is a swing class that should only be accessed from the event thread.
  * @version $Id: AbortablePanel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class AbortablePanel extends TabbedPanel {
  protected JPanel _leftPane;
  protected JScrollPane _scrollPane;
  
  protected final SingleDisplayModel _model;
  protected final MainFrame _frame;
  
  protected String _title;
  protected JPanel _buttonPanel;
  
  protected JButton _abortButton;

  /** Constructs a new abortable panel.
    * This is swing view class and hence should only be accessed from the event thread.
    * @param frame the MainFrame
    * @param title title of the pane
    */
  public AbortablePanel(MainFrame frame, String title) {
    super(frame, title);
    //MainFrame.LOG.log("\tAbortablePanel ctor");
    _title = title;
    this.setLayout(new BorderLayout());
    
    _frame = frame;
    _model = frame.getModel();
    
    this.removeAll(); // override the behavior of TabbedPanel

    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    _leftPane = new JPanel(new BorderLayout());
    Component leftPanel = makeLeftPanel();
    _scrollPane = new JScrollPane(leftPanel);
    _leftPane.add(_scrollPane);
    _setColors(leftPanel);
    
    this.add(_leftPane, BorderLayout.CENTER);
    
    _buttonPanel = new JPanel(new BorderLayout());
    _setupButtonPanel();
    this.add(_buttonPanel, BorderLayout.EAST);
    updateButtons();
    //MainFrame.LOG.log("\tAbortablePanel ctor done");
  }
  
  /** Quick helper for setting up color listeners. */
  protected static void _setColors(Component c) {
    new ForegroundColorListener(c);
    new BackgroundColorListener(c);
  }
  
  /** Close the pane. Override to make sure that the abort action is performed. */
  @Override
  protected void _close() {
    super._close();
    abortActionPerformed(null);
    updateButtons();
  }

  /** Setup left panel. Must be overridden to return the component on the left side. */
  protected abstract Component makeLeftPanel();

  /** Abort action was performed. Must be overridden to return the component on the left side. */
  protected abstract void abortActionPerformed(ActionEvent e);
  
  /** Update button state and text. Should be overridden if additional buttons are added besides "Go To", "Remove" and "Remove All". */
  protected void updateButtons() { }  

  /** Creates the buttons for controlling the regions. Should be overridden. */
  protected JComponent[] makeButtons() {
    return new JComponent[0];    
  }
  
  /** Creates the buttons for controlling the regions. */
  private void _setupButtonPanel() {
    JPanel mainButtons = new JPanel();
    JPanel emptyPanel = new JPanel();
    JPanel closeButtonPanel = new JPanel(new BorderLayout());
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    mainButtons.setLayout(gbLayout);
    
    JComponent[] buts = makeButtons();

    closeButtonPanel.add(_closeButton, BorderLayout.NORTH);    
    mainButtons.add(_abortButton = new JButton("Abort"));
    _abortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { abortActionPerformed(e); }
    });
    for (JComponent b: buts) { mainButtons.add(b); }
    mainButtons.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;

    gbLayout.setConstraints(_abortButton, c);
    for (JComponent b: buts) { gbLayout.setConstraints(b, c); }
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    _buttonPanel.add(mainButtons, BorderLayout.CENTER);
    _buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
  }
}
