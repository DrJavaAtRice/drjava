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
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Extended by all panels that can dynamically be added or removed from the
 * _tabbedPane in MainFrame. Provides a boolean indicating if the panel is being
 * displayed, and a close button. Attaches an action to the close button which
 * calls the _close method. This method can be overwritten in a subclass if
 * needed.
 * @version $Id$
 */
public abstract class TabbedPanel extends JPanel {
  // indicates whether this tab is displayed in the tabbed pane
  protected boolean _displayed;
  // button which removes this pane's tab
  protected JButton _closeButton;
  // panel that has _closeButton in the north so it can't be stretched
  // vertically
  protected JPanel _closePanel;
  // the panel that the subclasses of TabbedPanel can use
  protected JPanel _mainPanel;
  // used to be able to reference removeTab
  protected MainFrame _frame;
  // string to be displayed on the tab
  private String _name;

  /**
   * Constructor.
   * @param frame MainFrame displaying the tab
   * @param name Name to display for the tab
   */
  public TabbedPanel(MainFrame frame, String name) {
    _frame = frame;
    _name = name;
    _setUpPanes();
    _displayed = false;
  }

  /**
   * Puts the close panel in the east of this panel and puts the main panel in the
   * center. Also adds the action to the close button.
   */
  private void _setUpPanes() {
    this.setLayout(new BorderLayout());

    _mainPanel = new JPanel();
    _closePanel = new JPanel(new BorderLayout());
    _closeButton = new CommonCloseButton(_closeListener);
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    this.add(_closePanel, BorderLayout.EAST);
    this.add(_mainPanel, BorderLayout.CENTER);
  }

  /**
   * defines the action that takes place upon clicking the close button
   */
  private final ActionListener _closeListener =
    new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      _close();
    }
  };

  /**
   * behavior that subclasses of TabbedPanel generally have
   */
  protected void _close() {
     _displayed = false;
     _frame.removeTab(this);
  }

  /**
   * @return whether this tabbedPanel is displayed in a tab
   */
  public boolean isDisplayed() {
    return _displayed;
  }

  /**
   * @return the display name of this tab
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets whether the tab is displayed.  Doesn't actually show or hide the tab.
   */
  public void setDisplayed(boolean displayed) {
    _displayed = displayed;
  }

  JPanel getMainPanel() {
    return _mainPanel;
  }

  /**
   * This is overridden so that when switch previous pane focus is called
   * on the currentDefPane, the caret will move here on the first call.
   */
  public void requestFocus() {
    super.requestFocus();
    _mainPanel.requestFocus();
  }
}