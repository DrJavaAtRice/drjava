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
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.table.AbstractTableModel;

/**
 * <p>The ScrollableListSelectionDialog is a popup dialog with a message
 * and a scrollable list of items. Each item may be either selected or
 * unselected. A ScrollableListSelectionDialog should be used when
 * an operation needs to act on a variable number of items, for
 * example, when saving modified files.</p>
 * 
 * <p>The message (also know as the leader text) is displayed above the
 * items with an optional icon. The items are displayed in a scrollable
 * table. A column of checkboxes allows selection of the items. Buttons
 * are added below the list of items.</p>
 * 
 * <p>This dialog is somewhat styled after
 * {@link javax.swing.JOptionPane} and uses the message-type constants
 * from JOptionPane.</p>
 * 
 * @author Chris Warrington
 * @version $Id: ScrollableListSelectionDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
 * @since 2007-04-08
 */
public class ScrollableListSelectionDialog extends JDialog {
  /** A enumeration of the various selection states.
   */
  public enum SelectionState {
    /** Indicates that an item is selected. */
    SELECTED,
      /** Indicates that an item is not selected. */
      UNSELECTED
  };
  
  /** The default width for this dialog. */
  private static final int DEFAULT_WIDTH = 400;
  /** The default height for this dialog. */
  private static final int DEFAULT_HEIGHT = 450;
  
  /** The ratio of the screen width to use by default. */
  private static final double WIDTH_RATIO = .75;
  /** The ratio of the screen height to use by default. */
  private static final double HEIGHT_RATIO = .50;
  
  /** The table displaying the items. */
  protected final JTable table;
  /** The AbstractTableModel backing the table. */
  protected final AbstractTableModel tableModel;
  
  /** The number of columns in the table. */
  private static final int NUM_COLUMNS = 2;
  /** The column index of the checkboxes column. */
  private static final int CHECKBOXES_COLUMN_INDEX = 0;
  /** The column index of the strings column. */
  private static final int STRINGS_COLUMN_INDEX = 1;
  
  /** The items in the table. */
  protected final Vector<String> dataAsStrings;
  /** The selected items in the table. This Vector maps to
    * _dataAsStrings by index. This value may be accessed by multiple
    * threads. Threads wishing to access it should acquire its
    * intrinsic lock. */
  protected final Vector<Boolean> selectedItems;
  
  /** <p>Creates a new ScrollableListSelectionDialog with the given
   * title, leader text, and items. The list of items is used to
   * construct an internal string list that is not backed by the original
   * list. Changes made to the list or items after dialog construction
   * will not be reflected in the dialog.</p>
   * 
   * <p>The default sizing, message type, and icon are used. All the
   * items are selected by default.</p>
   * 
   * @param owner The frame that owns this dialog. May be {@code null}.
   * @param dialogTitle The text to use as the dialog title.
   * @param leaderText Text to display before the list of items.
   * @param listItems The items to display in the list.
   * @param itemDescription A textual description of the items. This is used as the column heading for the items.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   */
  public ScrollableListSelectionDialog(final Frame owner,
                                       final String dialogTitle,
                                       final String leaderText,
                                       final Collection<?> listItems,
                                       final String itemDescription) {
    this(owner, dialogTitle, leaderText, listItems, itemDescription, SelectionState.SELECTED, JOptionPane.PLAIN_MESSAGE);
  }
  
  /** <p>Creates a new ScrollableListSelectionDialog with the given
   * title, leader text, items, and message type. The list of items is
   * used to construct an internal string list that is not backed by the
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
   * @param itemDescription A textual description of the items. This is used as the column heading for the items.
   * @param defaultSelection The default selection state (selected or unselected) for the items.
   * @param messageType The type of dialog message.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  public ScrollableListSelectionDialog(final Frame owner,
                                       final String dialogTitle,
                                       final String leaderText,
                                       final Collection<?> listItems,
                                       final String itemDescription,
                                       final SelectionState defaultSelection,
                                       final int messageType) {
    this(owner,
         dialogTitle,
         leaderText,
         listItems,
         itemDescription,
         defaultSelection,
         messageType,
         DEFAULT_WIDTH,
         DEFAULT_HEIGHT,
         null,
         true);
  }
  
  /** <p>Creates a new ScrollableListSelectionDialog with the given
   * title, leader text, items, message type, width, height, and icon.
   * The list of items is used to construct an internal string list that
   * is not backed by the original list. Changes made to the list or
   * items after dialog construction will not be reflected in the
   * dialog.</p>
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
   * @param itemDescription A textual description of the items. This is used as the column heading for the items.
   * @param defaultSelection The default selection state (selected or unselected) for the items.
   * @param messageType The type of dialog message.
   * @param width The width of the dialog box.
   * @param height The height of the dialog box.
   * @param icon The icon to display. May be {@code null}.
   * 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  public ScrollableListSelectionDialog(final Frame owner,
                                       final String dialogTitle,
                                       final String leaderText,
                                       final Collection<?> listItems,
                                       final String itemDescription,
                                       final SelectionState defaultSelection,
                                       final int messageType,
                                       final int width,
                                       final int height,
                                       final Icon icon) {
    this(owner,
         dialogTitle,
         leaderText,
         listItems,
         itemDescription,
         defaultSelection,
         messageType,
         width,
         height,
         icon,
         false);
  }
  
  /** <p>Creates a new ScrollableListSelectionDialog with the given
   * title, leader text, items, message type, width, height, and icon.
   * The list of items is used to construct an internal string list that
   * is not backed by the original list. Changes made to the list or
   * items after dialog construction will not be reflected in the
   * dialog.</p>
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
   * @param itemDescription A textual description of the items. This is used as the column heading for the items.
   * @param defaultSelection The default selection state (selected or unselected) for the items.
   * @param messageType The type of dialog message.
   * @param width The width of the dialog box.
   * @param height The height of the dialog box.
   * @param icon The icon to display. May be {@code null}.
   * @param fitToScreen If {@code true}, the width and height of the dialog will be calculated using the screen 
   *        dimensions, {@link #WIDTH_RATIO}, and {@link #HEIGHT_RATIO}. If {@code false}, the provided width and
   *        height will be used. 
   * @throws IllegalArgumentException if {@code listItems} is {@code null.}
   * @throws IllegalArgumentException if the message type is unknown or {@code listItems} is {@code null.}
   */
  private ScrollableListSelectionDialog(final Frame owner,
                                        final String dialogTitle,
                                        final String leaderText,
                                        final Collection<?> listItems,
                                        final String itemDescription,
                                        final SelectionState defaultSelection,
                                        final int messageType,
                                        final int width,
                                        final int height,
                                        final Icon icon,
                                        final boolean fitToScreen) {
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
    
    final JPanel leaderPanel = new JPanel();
    final JLabel leaderLabel = new JLabel(leaderText);
    leaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    if (dialogIconLabel != null) {
      leaderPanel.add(dialogIconLabel);
    }
    leaderPanel.add(leaderLabel);
    
    /* create the table */
    //copy the items string representations into a vector
    dataAsStrings = new Vector<String>(listItems.size());
    for (Object obj : listItems) {
      if (obj != null) {
        final String objAsString = obj.toString();
        dataAsStrings.add(objAsString);
      }
    }
    dataAsStrings.trimToSize();
    
    final int numItems = dataAsStrings.size();
    
    selectedItems = new Vector<Boolean>(numItems);
    synchronized(selectedItems) {
      for (int i = 0; i < numItems; ++i) {
        selectedItems.add(i, defaultSelection == SelectionState.SELECTED);
      }
      selectedItems.trimToSize();
    }
    assert selectedItems.size() == dataAsStrings.size();
    
    tableModel = new AbstractTableModel() {
      //@Override - uncomment when we start compiling with Java 6
      public int getRowCount() {
        return numItems;
      }
      
      //@Override - uncomment when we start compiling with Java 6
      public int getColumnCount() {
        return NUM_COLUMNS;
      }
      
      //@Override - uncomment when we start compiling with Java 6
      public Object getValueAt(int row, int column) {
        if (column == CHECKBOXES_COLUMN_INDEX) {
          assert row >= 0;
          assert row < numItems;
          synchronized(selectedItems) { return selectedItems.get(row); }
        } else if (column == STRINGS_COLUMN_INDEX) {
          assert row >= 0;
          assert row < numItems;
          return dataAsStrings.get(row);
        } else {
          assert false;
          return null;
        }
      }
      
      @Override
      public String getColumnName(int column) {
        if (column == CHECKBOXES_COLUMN_INDEX) {
          return "";
        } else if (column == STRINGS_COLUMN_INDEX) {
          return itemDescription;
        } else {
          assert false;
          return "";
        }
      }
      
      @Override
      public Class<?> getColumnClass(final int columnIndex) {
        if (columnIndex == CHECKBOXES_COLUMN_INDEX) {
          return Boolean.class;
        } else if (columnIndex == STRINGS_COLUMN_INDEX) {
          return String.class;
        } else {
          assert false;
          return Object.class;
        }
      }
      
      @Override
      public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == CHECKBOXES_COLUMN_INDEX; //only checkboxes are editable
      }
      
      @Override
      public void setValueAt(final Object newValue, final int rowIndex, final int columnIndex) {
        assert columnIndex == CHECKBOXES_COLUMN_INDEX;
        assert rowIndex >= 0;
        assert rowIndex < numItems;
        assert newValue instanceof Boolean;
        
        final Boolean booleanValue = (Boolean)newValue;
        
        synchronized(selectedItems) { selectedItems.set(rowIndex, booleanValue); }
      }
    };
    
    table = new JTable(tableModel);
    
    /*
     * this listener enabled clicking in the string column to update the
     * checkbox.
     */
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        final Point clickPoint = e.getPoint();
        // which column was clicked on
        final int clickColumn = table.columnAtPoint(clickPoint);
        
        if (clickColumn == STRINGS_COLUMN_INDEX) {
          //it was the strings column, so update the check status of the row
          //Swing does not do this automatically
          final int clickRow = table.rowAtPoint(clickPoint);
          
          if (clickRow >= 0 && clickRow < numItems) {
            synchronized(selectedItems) {
              final boolean currentValue = selectedItems.get(clickRow);
              final boolean newValue = !currentValue;
              
              selectedItems.set(clickRow, newValue);
              /* We are deliberately holding on to the lock while the
               * listeners are notified. This, in theory, speeds up the
               * listeners because they don't have to re-acquire the
               * lock. Because the internals of Swing are unknown, the
               * lock may need to be released before the listeners are
               * notified. Only time will tell.
               * 
               * PS: If it turns out that holding the lock during
               * the listener updates is a problem, modify this comment
               * accordingly. Thank you.
               */
              tableModel.fireTableCellUpdated(clickRow, CHECKBOXES_COLUMN_INDEX);
            }
          }
        }
      }
    });
    
    //set the column sizes
    table.getColumnModel().getColumn(CHECKBOXES_COLUMN_INDEX).setMinWidth(15);
    table.getColumnModel().getColumn(CHECKBOXES_COLUMN_INDEX).setMaxWidth(30);
    table.getColumnModel().getColumn(CHECKBOXES_COLUMN_INDEX).setPreferredWidth(20);
    table.getColumnModel().getColumn(CHECKBOXES_COLUMN_INDEX).sizeWidthToFit();
    
    //create a scrollable view around the table
    final JScrollPane scrollPane = new JScrollPane(table);
    
    /* create the select all/select none panel */
    final JPanel selectButtonsPanel = new JPanel();
    selectButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    _addSelectButtons(selectButtonsPanel);
    
    /* create the button panel */
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    //allow children to add additional buttons, if overridden
    _addButtons(buttonPanel);
    
    /* create the center panel which contains the scroll pane and the
     * select all/select none buttons */
    final JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(selectButtonsPanel, BorderLayout.NORTH);
    centerPanel.add(scrollPane, BorderLayout.CENTER);
    
    /* create the dialog */
    final JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BorderLayout(10, 5));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
    
    contentPanel.add(leaderPanel, BorderLayout.NORTH);
    contentPanel.add(centerPanel, BorderLayout.CENTER);
    contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    
    getContentPane().add(contentPanel);
    
    /* calculate the dialog's dimensions */
    final Dimension dialogSize = new Dimension();
    
    if (fitToScreen) {
      //use the screen dimensions to calculate the dialog's
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
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
  private boolean _isknownMessageType(final int messageType) {
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
  private Icon _getIcon(final int messageType) {
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
  
  /** Adds the &quot;Select All&quot; and &quot;Select None&quot; buttons
   * to the given panel.
   * 
   * @param selectButtonsPanel The panel that should contain the buttons.
   */
  private void _addSelectButtons(final JPanel selectButtonsPanel) {
    final JButton selectAllButton = new JButton("Select All");
    edu.rice.cs.drjava.platform.PlatformFactory.ONLY.setMnemonic(selectAllButton,KeyEvent.VK_A);
    selectAllButton.addActionListener(new SelectAllNoneActionListener(SelectionState.SELECTED));
    selectButtonsPanel.add(selectAllButton);
    
    final JButton selectNoneButton = new JButton("Select None");
    edu.rice.cs.drjava.platform.PlatformFactory.ONLY.setMnemonic(selectNoneButton,KeyEvent.VK_N);
    selectNoneButton.addActionListener(new SelectAllNoneActionListener(SelectionState.UNSELECTED));
    selectButtonsPanel.add(selectNoneButton);
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
  protected void _addButtons(final JPanel buttonPanel) {
    final JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent notUsed) {
        closeDialog();
      }
    });
    
    buttonPanel.add(okButton);
    getRootPane().setDefaultButton(okButton);
  }
  
  /**
   * Shows the dialog.
   */
  public void showDialog() {
    pack();
    Utilities.setPopupLoc(this, getOwner());
    setVisible(true);
  }
  
  /** Should be called when the dialog should be closed. The default implementation
   * simply hides the dialog.
   */
  protected void closeDialog() {
    setVisible(false);
  }
  
  /** Returns the string representation of those items that are
   * currently selected. The items will be in the same relative order
   * as they were at construction time. The resultant collection may be
   * empty. The resultant collection is unmodifiable. The resultant
   * collection is simply a snapshot (i.e., It will not be updated as
   * more items are selected.). This method may be called from
   * non-event&nbsp;queue threads.
   * 
   * @return The currently selected items.
   */
  public java.util.List<String> selectedItems() {
    final java.util.List<String> results = new ArrayList<String>();
    
    synchronized(selectedItems) {
      /* This entire loop is synchronized so that we get a consistent
       * view of the selected items. It is also faster.
       */
      for (int i = 0; i < dataAsStrings.size(); ++i) {
        if (selectedItems.get(i)) {
          results.add(dataAsStrings.get(i));
        }
      }
    }
    
    return Collections.unmodifiableList(results);
  }
  
  /** An ActionListener that handles the &quot;Select All&quot; and
   * &quot;Select None&quot; buttons. It will set the selection state
   * of every item to the given selection state.
   */
  private class SelectAllNoneActionListener implements ActionListener {
    /** The value that the selection state will be set to when this
      * listener runs. */
    private final boolean _setToValue;
    
    /**
     * Creates a new SelectAllNoneActionListener that will set the state
     * of every item to the given state.
     * 
     * @param setToState The state to set all the items to.
     */
    public SelectAllNoneActionListener(SelectionState setToState) {
      _setToValue = setToState == SelectionState.SELECTED;
    }
    
    /**
     * The code that runs in response to the button's action.
     * This is the code that actually sets the selection state of the
     * items.
     * 
     * @param notUsed Not used.
     */
    public void actionPerformed(ActionEvent notUsed) {
      /* See comment in the table's mouse listener for a discussion
       * about the duration of the lock.
       */
      synchronized(selectedItems) {
        for (int i = 0; i < selectedItems.size(); ++i) {
          selectedItems.set(i, _setToValue);
        }
        tableModel.fireTableRowsUpdated(0, Math.max(0, selectedItems.size() - 1));
      }
    }
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
    
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        ScrollableListSelectionDialog ld = 
          new ScrollableListSelectionDialog(null, "TITLE", "LEADER", data, "Words", SelectionState.SELECTED, 
                                            JOptionPane.ERROR_MESSAGE) {
          @Override
          protected void closeDialog() {
            super.closeDialog();
            Collection<String> si = selectedItems();
            for (String i : si) {
              System.out.println(i);
            }
          }
        };
        ld.pack();
        ld.setVisible(true);
      }
    });
  }
}
