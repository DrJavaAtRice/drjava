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
import java.awt.dnd.*;
import edu.rice.cs.drjava.DrJavaRoot;

/** Extended by all panels that can dynamically be added or removed from the _tabbedPane in MainFrame. Provides a
  * boolean indicating if the panel is being displayed, and a close button. Attaches an action to the close button
  * which calls the _close method. This method can be overwritten in a subclass if needed.  Methods in this
  * class should only be executed in the event thread, but it not enforced.  (FIX THIS?)
  * @version $Id: TabbedPanel.java 5668 2012-08-15 04:58:30Z rcartwright $
  */
public abstract class TabbedPanel extends JPanel implements DropTargetListener {
  
  /** indicates whether this tab is displayed in the tabbed pane. */
  protected volatile boolean _displayed;
  /** button which removes this pane's tab. */
  protected volatile JButton _closeButton;
  // panel that has _closeButton in the north so it can't be stretched vertically
  protected volatile JPanel _closePanel;
  // the panel that the subclasses of TabbedPanel can use
  protected volatile JPanel _mainPanel;
  // used to be able to reference removeTab
  protected volatile MainFrame _frame;
  // string to be displayed on the tab
  private volatile String _name;

  /** Constructor.
    * @param frame MainFrame displaying the tab
    * @param name Name to display for the tab
    */
  public TabbedPanel(MainFrame frame, String name) {
    _frame = frame;
    _name = name;
    _setUpPanes();
    _displayed = false;
  }

  /** Puts the close panel in the east of this panel and puts the main panel in the
    * center. Also adds the action to the close button.
    */
  private void _setUpPanes() {
    this.setFocusCycleRoot(true);
    this.setLayout(new BorderLayout());

    _mainPanel = new JPanel();
    _closePanel = new JPanel(new BorderLayout());
    _closeButton = new CommonCloseButton(_closeListener);
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    add(_closePanel, BorderLayout.EAST);
    add(_mainPanel, BorderLayout.CENTER);
  }

  /** Defines the action that takes place upon clicking the close button. */
  private final ActionListener _closeListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) { _close(); }
  };

  /** Visibly closes the panel and removes it from the frame. */
  protected void _close() {
//    System.err.println("TabbedPanel._close() called");
    _displayed = false;
    _frame.removeTab(this);
  }
  
  public void addCloseListener(ActionListener l) { _closeButton.addActionListener(l); }

  public void setVisible(boolean b) {
    super.setVisible(b);
    if (_frame._mainSplit.getDividerLocation() > _frame._mainSplit.getMaximumDividerLocation()) 
        _frame._mainSplit.resetToPreferredSizes();
  }
  
  /** @return whether this tabbedPanel is displayed in a tab */
  public boolean isDisplayed() { return _displayed; }

  /** @return the display name of this tab. */
  public String getName() { return _name; }

  /** Sets whether the tab is displayed.  Doesn't actually show or hide the tab. */
  public void setDisplayed(boolean displayed) { _displayed = displayed; }

  JPanel getMainPanel() { return _mainPanel; }

  /** This is overridden so that when switch previous pane focus is called
    * on the currentDefPane, the caret will move here on the first call.
    */
  public boolean requestFocusInWindow() {
//    System.err.println("requestFocusInWindow called on TabbedPanel");
    super.requestFocusInWindow();
    return _mainPanel.requestFocusInWindow();
  }
  
  /** Drag and drop target. */
  volatile DropTarget dropTarget = new DropTarget(this, this);  

  /** User dragged something into the component. */
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    DrJavaRoot.dragEnter(dropTargetDragEvent);
  }
  
  public void dragExit(DropTargetEvent dropTargetEvent) { }
  public void dragOver(DropTargetDragEvent dropTargetDragEvent) { }
  public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){ }
  
  /** User dropped something on the component.  Only runs in event thread. */
  public /* synchronized */ void drop(DropTargetDropEvent dropTargetDropEvent) {
    DrJavaRoot.drop(dropTargetDropEvent);
  }
}