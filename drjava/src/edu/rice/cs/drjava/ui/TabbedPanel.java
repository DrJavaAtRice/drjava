/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
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
  private JPanel _closePanel;
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
    
    _closePanel = new JPanel(new BorderLayout());
    _mainPanel = new JPanel();
    
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
}