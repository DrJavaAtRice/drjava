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

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/** <p>The ScrollableListDialog is a popup dialog with a message and a scrollable list of items. A ScrollableListDialog
  * should be used when a message may need to display a variable number of items, for example, when reporting missing 
  * files.</p>
  * <p>The message (also know as the leader text) is displayed above the items with an optional icon. The items are 
  * displayed in a scrollable list. Buttons are added below the list of items.</p>
  * <p>This dialog is somewhat styled after {@link javax.swing.JOptionPane} and uses the message-type constants from 
  * JOptionPane.</p>
  * 
  * @author Chris Warrington
  * @version $Id: ScrollableListDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
  * @since 2007-03-06
  */
public class ScrollableListDialog<T> extends JDialog {
  /** The default width for this dialog. */
  private static final int DEFAULT_WIDTH = 400;
  /** The default height for this dialog. */
  private static final int DEFAULT_HEIGHT = 450;
  
  /** The ratio of the screen width to use by default. */
  private static final double WIDTH_RATIO = .75;
  /** The ratio of the screen height to use by default. */
  private static final double HEIGHT_RATIO = .50;
  
  /** The list of items displayed. */
  protected final JList<String> list;
  
  /** The number of the button that was pressed to close the dialog. */
  protected int _buttonPressed = -1;
  
  /** The list of items being listed. */
  protected List<T> listItems;
  
  /** Factory design pattern. Used to create new ScrollableListDialogs with
    * less complicated and ambiguous constructors. */
  public static class Builder<T> {
    protected Frame _owner;
    protected String _dialogTitle;
    protected String _leaderText;
    protected List<T> _listItems = new ArrayList<T>();
    protected List<T> _selectedItems = new ArrayList<T>();
    protected int _messageType = JOptionPane.PLAIN_MESSAGE;
    protected int _width = DEFAULT_WIDTH;
    protected int _height = DEFAULT_HEIGHT;
    protected Icon _icon = null;
    protected boolean _fitToScreen = true;
    protected List<JButton> _buttons = new ArrayList<JButton>();
    protected List<JComponent> _additional = new ArrayList<JComponent>();
    protected boolean _selectable = false;
    public Builder() { addOkButton(); }
    public Builder<T> setOwner(Frame owner) { _owner = owner; return this; }
    public Builder<T> setTitle(String dialogTitle) { _dialogTitle = dialogTitle; return this; }
    public Builder<T> setText(String leaderText) { _leaderText = leaderText; return this; }
    public Builder<T> setItems(List<T> listItems) { _listItems = listItems; return this; }
    public Builder<T> setSelectedItems(List<T> selItems) { _selectedItems = selItems; return this; }
    public Builder<T> setMessageType(int messageType) { _messageType = messageType; return this; }
    public Builder<T> setWidth(int width) { _width = width; return this; }
    public Builder<T> setHeight(int height) { _height = height; return this; }
    public Builder<T> setIcon(Icon icon) { _icon = icon; return this; }
    public Builder<T> setFitToScreen(boolean fts) { _fitToScreen = fts; return this; }
    public Builder<T> clearButtons() { _buttons.clear(); return this; }
    public Builder<T> addOkButton() { _buttons.add(new JButton("OK")); return this; }
    public Builder<T> addButton(JButton b) { _buttons.add(b); return this; }
    public Builder<T> addAdditionalComponent(JComponent c) { _additional.add(c); return this; }
    public Builder<T> setSelectable(boolean b) { _selectable = b; return this; }
    public ScrollableListDialog<T> build() {
      return new ScrollableListDialog<T>(_owner, _dialogTitle, _leaderText, _listItems, _selectedItems, _messageType, 
                                         _width, _height, _icon, _fitToScreen, _buttons, _additional, _selectable);
    }
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
    * @param selItems The items selected at the beginning.
    * @param messageType The type of dialog message.
    * @param width The width of the dialog box.
    * @param height The height of the dialog box.
    * @param icon The icon to display. May be {@code null}.
    * @param fitToScreen If {@code true}, the width and height of the dialog will be calculated using the screen 
    *        dimensions, {@link #WIDTH_RATIO}, and {@link #HEIGHT_RATIO}. If {@code false}, the provided width and
    *        height will be used.
    * @param buttons The list of buttons to display
    * @param additional The list of additional components to display
    * @param selectable true if items can be selected
    * 
    * @throws IllegalArgumentException if {@code listItems} is {@code null.}
    * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
    */
  private ScrollableListDialog(Frame owner, String dialogTitle, String leaderText, List<T> listItems, List<T> selItems,
                               int messageType, int width, int height, Icon icon, boolean fitToScreen, 
                               List<JButton> buttons, List<JComponent> additional, boolean selectable) {
    super(owner, dialogTitle, true);
    this.listItems = listItems;
    
    if (!_isknownMessageType(messageType)) {
      throw new IllegalArgumentException("The message type \"" + messageType + "\" is unknown");
    }
    
    if (listItems == null) throw new IllegalArgumentException("listItems cannot be null");
    
    /* create the leader text panel */
    JLabel dialogIconLabel = null;
    if (icon != null) { //use the user-provided icon
      dialogIconLabel = new JLabel(icon);
    } 
    else { //lookup the message-dependent icon
      Icon messageIcon = _getIcon(messageType);
      if (messageIcon != null) dialogIconLabel = new JLabel(messageIcon); 
    }

    final JLabel leaderLabel = new JLabel(leaderText);
    final JPanel leaderPanel = new JPanel();
    leaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    if (dialogIconLabel != null) leaderPanel.add(dialogIconLabel);
    leaderPanel.add(leaderLabel);
    
    /* create the item list box */
    //copy the items string representations into a vector
    final Vector<String> dataAsStrings = new Vector<String>(listItems.size());
    //keep track of the longest string for use later
    String longestString = "";
    for (T obj : listItems) {
      if (obj != null) {
        final String objAsString = obj.toString();
        //update longest string
        if (objAsString.length() > longestString.length()) {
         longestString = objAsString;
        }
        dataAsStrings.add(objAsString);
      }
    }
    
    if (selectable) {
      final Vector<String> selAsStrings = new Vector<String>(selItems.size());
      for (T obj : selItems) {
        if (obj != null) {
          final String objAsString = obj.toString();
          selAsStrings.add(objAsString);
        }
      }
      list = new CheckBoxJList(dataAsStrings, selAsStrings);
      //let the user select several
      list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }
    else {
      list = new JList<String>(dataAsStrings);
      //let the user select several
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    //use the longest string to calculate item cell widths and heights
    list.setPrototypeCellValue(longestString);
    
    //create a scrollable view around the list
    final JScrollPane scrollPane = new JScrollPane(list);
    
    /* create the button panel */
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    //allow children to add additional buttons, if overridden
    _addButtons(buttonPanel, buttons);
    _addAdditionalComponents(buttonPanel, additional);
    
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
  
  /**
   * A method to check if they given message type is a know message
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
  
  /**
   * Lookup the icon associated with the given messageType. The message
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
  
  /**
   * Adds buttons to the bottom of the dialog. By default, a single
   * &quot;OK&quot; button is added that calls {@link #closeDialog}. It
   * is also set as the dialog's default button.
   *
   * Inheritors should feel free the change settings of the panel such
   * as the layout manager. However, no guarantees are made that every
   * change will work with every version of this class.
   * 
   * @param buttonPanel The JPanel that should contain the buttons.
   * @param buttons The list of buttons
   */
  protected void _addButtons(JPanel buttonPanel, List<JButton> buttons) {
    int i = 0;
    for (JButton b: buttons) {
      final int j = i++;
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent notUsed) {
          _buttonPressed = j;
          closeDialog();
        }
      });
      buttonPanel.add(b);
    }
    
    getRootPane().setDefaultButton(buttons.get(0));
  }
  
  /**
   * Adds additional components to the bottom of the dialog.
   * @param buttonPanel The JPanel that should contain the components.
   * @param additional The list of components
   */
  protected void _addAdditionalComponents(JPanel buttonPanel, List<JComponent> additional) {
    int i = 0;
    for (JComponent c: additional) {
      buttonPanel.add(c);
    }
  }
  
  /**
   * Shows the dialog.
   */
  public void showDialog() {
    pack();
    Utilities.setPopupLoc(this, getOwner());
    setVisible(true);
  }
  
  /**
   * Should be called when the dialog should be closed. The default
   * implementation simply hides the dialog.
   */
  protected void closeDialog() {
    setVisible(false);
  }
  
  /** Return the number of the button that was pressed to close the dialog. */
  public int getButtonPressed() { return _buttonPressed; }
  
  /** Return a list of the selected items. */
  public List<T> getSelectedItems() {
    ArrayList<T> l = new ArrayList<T>();
    for (int i: list.getSelectedIndices())  l.add(listItems.get(i));

    return l;
  }
  
  /** A simple main method for testing purposes.
    * @param args Not used.
    */
  public static void main(String args[]) {
    final List<String> data = new java.util.ArrayList<String>();
    data.add("how");
    data.add("now");
    data.add("brown");
    data.add("cow");
    
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        ScrollableListDialog<String> ld = new ScrollableListDialog.Builder<String>()
          .setOwner(null)
          .setTitle("TITLE")
          .setText("LEADER")
          .setItems(data)
          .setMessageType(JOptionPane.ERROR_MESSAGE)
          .setSelectable(true)
          .setSelectedItems(data.subList(0,2))
          .build();
        ld.pack();
        ld.setVisible(true);
      }
    });
  }
}

