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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

/**
 * Manages a JDialog with a scrollable text area and a button panel.
 * @version $Id$
 */
public class ScrollableDialog {
  /** Default width for all ScrollableDialogs. */
  public static final int DEFAULT_WIDTH = 500;
  /** Default height for all ScrollableDialogs. */
  public static final int DEFAULT_HEIGHT = 400;
  /** JDialog managed by this component. */
  protected JDialog _dialog;
  /** JTextArea contained in a scroll pane in this dialog. */
  protected JTextArea _textArea;
  /** Panel of buttons at the bottom of this dialog. */
  protected JPanel _buttonPanel;
  
  /**
   * Creates a new ScrollableDialog with the default width and height.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text) {
    this(parent, title, header, text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }
  
  /**
   * Creates a new ScrollableDialog.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   * @param width Width for this dialog
   * @param height Height for this dialog
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text,
                          int width, int height)
  {
    _dialog = new JDialog(parent, title, true);    
    Container content = _dialog.getContentPane();

    content.setLayout(new BorderLayout());

    // Create the text area
    _textArea = new JTextArea();
    _textArea.setFont(DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN));
    _textArea.setEditable(false);
    _textArea.setText(text);
    
    // Arrange the dialog
    _dialog.setSize(width, height);
    _dialog.setLocationRelativeTo(parent);
    
    // Add components
    JScrollPane textScroll = 
      new BorderlessScrollPane(_textArea,
                               JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel scrollWrapper = new JPanel(new BorderLayout(0,5));
    scrollWrapper.setBorder(new EmptyBorder(5,5,0,5));
    scrollWrapper.add(new JLabel(header),BorderLayout.NORTH);
    scrollWrapper.add(textScroll,BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel(new GridLayout(1,0,5,5));
    bottomPanel.add(_buttonPanel,BorderLayout.EAST);
    bottomPanel.setBorder(new EmptyBorder(5,5,5,5));
    _addButtons();
    
    content.add(scrollWrapper, BorderLayout.CENTER);
    content.add(bottomPanel, BorderLayout.SOUTH);
    
    _textArea.requestDefaultFocus();
  }

  /**
   * Adds buttons to this dialog's button panel.
   * Subclasses can override this to add different buttons.
   */
  protected void _addButtons() {
    _buttonPanel.add(new JButton(_okAction));
  }

  /**
   * A default "OK" action which disposes this dialog when invoked.
   */
  private Action _okAction = new AbstractAction("OK") {
    public void actionPerformed(ActionEvent e) {
      _dialog.dispose();
    }
  };

  /**
   * Shows this dialog.
   */
  public void show() {
    _dialog.show();
  }
}