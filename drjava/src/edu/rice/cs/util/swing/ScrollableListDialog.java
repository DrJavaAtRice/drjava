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

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * <p>The ScrollableListDialog is a popup dialog with a message and a
 * scrollable list of items. A ScrollableListDialog should be used when
 * a message may need to display a variable number of items, for
 * example, when reporting missing files.</p>
 * 
 * <p>The message (also know as the leader text) is displayed above the
 * items with an optional icon. The items are displayed in a scrollable
 * list. Buttons are added below the list of items.</p>
 * 
 * <p>This dialog is somewhat styled after
 * {@link javax.swing.JOptionPane} and uses the message-type constants
 * from JOptionPane.</p>
 * 
 * @author Chris Warrington
 * @version $Id$
 * @since 2007-03-06
 */
public class ScrollableListDialog extends JDialog {
  /** The default width for this dialog. */
  private static final int DEFAULT_WIDTH = 400;
  /** The default height for this dialog. */
  private static final int DEFAULT_HEIGHT = 450;
  
  /** The ratio of the screen width to use by default. */
  private static final double WIDTH_RATIO = .75;
  /** The ratio of the screen height to use by default. */
  private static final double HEIGHT_RATIO = .50;
  
  /** The list of items displayed. */
  protected final JList list;
  
  /** <p>Creates a new ScrollableListDialog with the given title, leader
   * text, and items. The list of items is used to construct an
   * internal string list that is not backed by the original list.
   * Changes made to the list or items after dialog construction will
   * not be reflected in the dialog.</p>
   * 
   * <p>The default sizing, message type, and icon are used.</p>
   * 
   * @param owner The frame that owns this dialog. May be {@code null}.
   * @param dialogTitle The text to use as the dialog title.
   * @param leaderText Text to display before the list of items.
   * @param listItems The items to display in the list.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   */
  public ScrollableListDialog(Frame owner, String dialogTitle, String leaderText, Collection<?> listItems) {
    this(owner, dialogTitle, leaderText, listItems, JOptionPane.PLAIN_MESSAGE);
  }
  
  /** <p>Creates a new ScrollableListDialog with the given title, leader
   * text, items, and message type. The list of items is used to
   * construct an internal string list that is not backed by the
   * original list. Changes made to the list or items after dialog
   * construction will not be reflected in the dialog.</p>
   * 
   * <p>The message type must be one of the message types from
   * {@link javax.swing.JOptionPane}. The message type controlls which
   * default icon is used.</p>
   * 
   * <p>The default sizing and icon are used.</p>
   * 
   * @param owner The frame that owns this dialog. May be {@code null}.
   * @param dialogTitle The text to use as the dialog title.
   * @param leaderText Text to display before the list of items.
   * @param listItems The items to display in the list.
   * @param messageType The type of dialog message.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  public ScrollableListDialog(Frame owner, String dialogTitle, String leaderText, Collection<?> listItems, int messageType) {
    this(owner, dialogTitle, leaderText, listItems, messageType, DEFAULT_WIDTH, DEFAULT_HEIGHT, null, true);
  }
  
  /** <p>Creates a new ScrollableListDialog with the given title, leader
   * text, items, message type, width, height, and icon. The list of
   * items is used to construct an internal string list that is not
   * backed by the original list. Changes made to the list or items
   * after dialog construction will not be reflected in the dialog.</p>
   * 
   * <p>The message type must be one of the message types from
   * {@link javax.swing.JOptionPane}. The message type controlls which
   * default icon is used. If {@code icon} is non-null, it is used
   * instead of the default icon.</p>
   * 
   * @param owner The frame that owns this dialog. May be {@code null}.
   * @param dialogTitle The text to use as the dialog title.
   * @param leaderText Text to display before the list of items.
   * @param listItems The items to display in the list.
   * @param messageType The type of dialog message.
   * @param width The width of the dialog box.
   * @param height The height of the dialog box.
   * @param icon The icon to display. May be {@code null}.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  public ScrollableListDialog(Frame owner, String dialogTitle, String leaderText, Collection<?> listItems, int messageType, int width, int height, Icon icon) {
    this(owner, dialogTitle, leaderText, listItems, messageType, width, height, icon, false);
  }
  
  /** <p>Creates a new ScrollableListDialog with the given title, leader
   * text, items, message type, width, height, and icon. The list of
   * items is used to construct an internal string list that is not
   * backed by the original list. Changes made to the list or items
   * after dialog construction will not be reflected in the dialog.</p>
   * 
   * <p>The message type must be one of the message types from
   * {@link javax.swing.JOptionPane}. The message type controlls which
   * default icon is used. If {@code icon} is non-null, it is used
   * instead of the default icon.</p>
   * 
   * @param owner The frame that owns this dialog. May be {@code null}.
   * @param dialogTitle The text to use as the dialog title.
   * @param leaderText Text to display before the list of items.
   * @param listItems The items to display in the list.
   * @param messageType The type of dialog message.
   * @param width The width of the dialog box.
   * @param height The height of the dialog box.
   * @param icon The icon to display. May be {@code null}.
   * @param fitToScreen If {@code true}, the width and height of the dialog will be calculated using the screen dimensions, {@link #WIDTH_RATIO}, and {@link #HEIGHT_RATIO}. If {@code false}, the provided width and height will be used.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  private ScrollableListDialog(Frame owner, String dialogTitle, String leaderText, Collection<?> listItems, int messageType, int width, int height, Icon icon, boolean fitToScreen) {
    super(owner, dialogTitle, true);
    
    if (!_isknownMessageType(messageType)) {
      throw new IllegalArgumentException("The message type \"" + messageType + "\" is unknown");
    }
    
    if (listItems == null) {
      throw new IllegalArgumentException("listItems cannot be null");
    }
    
    /* create the leader text panel */
    JLabel dialogIconLabel = null;
    if (icon != null) {
      //use the user-provided icon
      dialogIconLabel = new JLabel(icon);
    } else {
      //lookup the message-dependent icon
      Icon messageIcon = _getIcon(messageType);
      if (messageIcon != null) {
       dialogIconLabel = new JLabel(messageIcon); 
      }
    }

    final JLabel leaderLabel = new JLabel(leaderText);
    final JPanel leaderPanel = new JPanel();
    leaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    if (dialogIconLabel != null) {
      leaderPanel.add(dialogIconLabel);
    }
    leaderPanel.add(leaderLabel);
    
    /* create the item list box */
    //copy the items string representations into a vector
    final Vector<String> dataAsStrings = new Vector<String>(listItems.size());
    //keep track of the longest string for use later
    String longestString = "";
    for (Object obj : listItems) {
      if (obj != null) {
        final String objAsString = obj.toString();
        //update longest string
        if (objAsString.length() > longestString.length()) {
         longestString = objAsString;
        }
        dataAsStrings.add(objAsString);
      }
    }
    
    list = new JList(dataAsStrings);
    //since we are not using the selection, limit it to one item
    list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    //use the longest string to calculate item cell widths and heights
    list.setPrototypeCellValue(longestString);
    
    //create a scrollable view around the list
    final JScrollPane scrollPane = new JScrollPane(list);
    
    /* create the button panel */
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    //allow children to add additional buttons, if overridden
    _addButtons(buttonPanel);
    
    /* create the dialog */
    final JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout(10, 5));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
    
    contentPanel.add(leaderPanel, BorderLayout.NORTH);
    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    
    getContentPane().add(contentPanel);

    /* calculate the dialog's dimensions */
    Dimension dialogSize = new Dimension();
    
    if (fitToScreen) {
      //use the screen dimensions to calculate the dialog's
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      int screenBasedWidth = (int) (WIDTH_RATIO * screenSize.getWidth());
      int screenBasedHeight = (int) (HEIGHT_RATIO * screenSize.getHeight());
     
      dialogSize.setSize(Math.max(DEFAULT_WIDTH, screenBasedWidth),
                         Math.max(DEFAULT_HEIGHT, screenBasedHeight));
    } else {
      //use the user-provided dimensions
      dialogSize.setSize(width, height);
    }

    setSize(dialogSize);
  }
  
  /** A method to check if they given message type is a know message
   * type.
   * 
   * @param messageType The message type to check
   * @return {@code true} if the message type is known, {@code false} otherwise
   */
  private boolean _isknownMessageType(int messageType) {
    return messageType == JOptionPane.ERROR_MESSAGE ||
      messageType == JOptionPane.INFORMATION_MESSAGE ||
      messageType == JOptionPane.WARNING_MESSAGE ||
      messageType == JOptionPane.QUESTION_MESSAGE ||
      messageType == JOptionPane.PLAIN_MESSAGE;
  }
  
  /** Lookup the icon associated with the given messageType. The message
   * type must be one of the message types from
   * {@link javax.swing.JOptionPane}.
   * 
   * @param messageType The message for which the icon is requested.
   * @return The message's icon or {@code null} is no icon was found.
   */
  private Icon _getIcon(int messageType) {
    assert _isknownMessageType(messageType);
    
    /* The OptionPane.xxxIcon constants were taken from 
     * javax.swing.plaf.basic.BasicOptionPaneUI, which may changed
     * without notice.
     */
    if (messageType == JOptionPane.ERROR_MESSAGE) {
      return UIManager.getIcon("OptionPane.errorIcon");
    } else if (messageType == JOptionPane.INFORMATION_MESSAGE) {
      return UIManager.getIcon("OptionPane.informationIcon");
    } else if (messageType == JOptionPane.WARNING_MESSAGE) {
      return UIManager.getIcon("OptionPane.warningIcon");
    } else if (messageType == JOptionPane.QUESTION_MESSAGE) {
      return UIManager.getIcon("OptionPane.questionIcon");
    } else if (messageType == JOptionPane.PLAIN_MESSAGE) {
      return null;
    } else {
      //should never get here
      assert false;
    }

    return null;
  }
  
  /** Adds buttons to the bottom of the dialog. By default, a single
   * &quot;OK&quot; button is added that calls {@link #closeDialog}. It
   * is also set as the dialog's default button.
   *
   * Inheritors should feel free the change settings of the panel such
   * as the layout manager. However, no guarantees are made that every
   * change will work with every version of this class.
   * 
   * @param buttonPanel The JPanel that should contain the buttons.
   */
  protected void _addButtons(JPanel buttonPanel) {
    final JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent notUsed) {
        closeDialog();
      }
    });

    buttonPanel.add(okButton);
    getRootPane().setDefaultButton(okButton);
  }
  
  /** Shows the dialog.
   */
  public void showDialog() {
    pack();
    setVisible(true);
  }
  
  /** Should be called when the dialog should be closed. The default
   * implementation simply hides the dialog.
   */
  protected void closeDialog() {
    setVisible(false);
  }
  
  /** A simple main method for testing purposes.
   * 
   * @param args Not used.
   */
  public static void main(String args[]) {
    final Collection<String> data = new java.util.ArrayList<String>();
    data.add("how");
    data.add("now");
    data.add("brown");
    data.add("cow");
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ScrollableListDialog ld = new ScrollableListDialog(null, "TITLE", "LEADER", data, JOptionPane.ERROR_MESSAGE);
        ld.pack();
        ld.setVisible(true);
      }
    });
  }
}
