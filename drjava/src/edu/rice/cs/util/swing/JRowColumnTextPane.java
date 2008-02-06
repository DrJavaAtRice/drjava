/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.Set;
import java.util.TreeSet;

/** Extended JTextPane that allows the user to set number of rows and columns.
  * @version $Id$
  */
public class JRowColumnTextPane extends JTextPane {
  /** Creates a new <code>JRowColumnTextPane</code> that can be sized using rows
    * and columns, just like a JTextArea. A new instance of <code>StyledEditorKit</code> is
    * created and set, rows and columns are set to 0, and the document model set to
    * <code>null</code>. */
  public JRowColumnTextPane() {
    super();
    rows = 0;
    columns = 0;
  }
  
  /** Creates a new <code>JRowColumnTextPane</code>, with a specified document model.
    * A new instance of <code>javax.swing.text.StyledEditorKit</code>
    * is created and set, and rows and columns are set to 0.
    * @param doc the document model
    */
  public JRowColumnTextPane(StyledDocument doc) {
    this();
    rows = 0;
    columns = 0;
  }
  
  /** Constructs a new empty JRowColumnTextPane with the specified number of
    * rows and columns. A default model is created, and the initial
    * string is null.
    * @param rows the number of rows >= 0
    * @param columns the number of columns >= 0
    * @exception IllegalArgumentException if the rows or columns
    *  arguments are negative */
  public JRowColumnTextPane(int rows, int columns) {
    super();
    if (rows < 0) {
      throw new IllegalArgumentException("rows: " + rows);
    }
    if (columns < 0) {
      throw new IllegalArgumentException("columns: " + columns);
    }
    // NOTE: no 1.4 compatible way to do this found
    //    LookAndFeel.installProperty(this,
    //                                "focusTraversalKeysForward", 
    //                                getManagingFocusForwardTraversalKeys());
    //    LookAndFeel.installProperty(this,
    //                                "focusTraversalKeysBackward", 
    //                                getManagingFocusBackwardTraversalKeys());
    
    this.rows = rows;
    this.columns = columns;
  }
  
  /**
   * Returns the Set of <code>KeyStroke</code>s to use if the component
   * is managing focus for forward focus traversal.
   */
  static Set<KeyStroke> getManagingFocusForwardTraversalKeys() {
    if (managingFocusForwardTraversalKeys == null) {
      managingFocusForwardTraversalKeys = new TreeSet<KeyStroke>();
      managingFocusForwardTraversalKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK));
    }
    return managingFocusForwardTraversalKeys;
  }
  
  /**
   * Returns the Set of <code>KeyStroke</code>s to use if the component
   * is managing focus for backward focus traversal.
   */
  static Set<KeyStroke> getManagingFocusBackwardTraversalKeys() {
    if (managingFocusBackwardTraversalKeys == null) {
      managingFocusBackwardTraversalKeys = new TreeSet<KeyStroke>();
      managingFocusBackwardTraversalKeys.add(
                                             KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                                    InputEvent.SHIFT_MASK |
                                                                    InputEvent.CTRL_MASK));
    }
    return managingFocusBackwardTraversalKeys;
  }
  
  /**
   * Constructs a new JTextArea with the specified number of rows
   * and columns, and the given model.  All of the constructors
   * feed through this constructor.
   *
   * @param doc the model to use, or create a default one if null
   * @param rows the number of rows >= 0
   * @param columns the number of columns >= 0
   * @exception IllegalArgumentException if the rows or columns
   *  arguments are negative.
   */
  public JRowColumnTextPane(StyledDocument doc, int rows, int columns) {
    super(doc);
    if (rows < 0) {
      throw new IllegalArgumentException("rows: " + rows);
    }
    if (columns < 0) {
      throw new IllegalArgumentException("columns: " + columns);
    }
    // NOTE: no 1.4 compatible way to do this found
    //    LookAndFeel.installProperty(this,
    //                                "focusTraversalKeysForward", 
    //                                getManagingFocusForwardTraversalKeys());
    //    LookAndFeel.installProperty(this,
    //                                "focusTraversalKeysBackward", 
    //                                getManagingFocusBackwardTraversalKeys());
    this.rows = rows;
    this.columns = columns;
  }
  
  /** Returns the number of rows in the TextArea.
    * @return the number of rows >= 0 */
  public int getRows() {
    return rows;
  }
  
  /** Sets the number of rows for this JRowColumnTextPane. Calls invalidate()
    * after setting the new value.
    * @param rows the number of rows >= 0
    * @exception IllegalArgumentException if rows is less than 0 */
  public void setRows(int rows) {
    int oldVal = this.rows;
    if (rows < 0) {
      throw new IllegalArgumentException("rows less than zero.");
    }
    if (rows != oldVal) {
      this.rows = rows;
      invalidate();
    }
  }
  
  /** Defines the meaning of the height of a row.  This defaults to
    * the height of the font.
    * @return the height >= 1 */
  protected int getRowHeight() {
    if (rowHeight == 0) {
      FontMetrics metrics = getFontMetrics(getFont());
      rowHeight = metrics.getHeight();
    }
    return rowHeight;
  }
  
  /** Returns the number of columns in the TextArea.
    * @return number of columns >= 0 */
  public int getColumns() {
    return columns;
  }
  
  /** Sets the number of columns for this TextArea.  Does an invalidate()
    * after setting the new value.
    * @param columns the number of columns >= 0
    * @exception IllegalArgumentException if columns is less than 0 */
  public void setColumns(int columns) {
    int oldVal = this.columns;
    if (columns < 0) {
      throw new IllegalArgumentException("columns less than zero.");
    }
    if (columns != oldVal) {
      this.columns = columns;
      invalidate();
    }
  }
  
  /** Gets column width.
    * @return the column width >= 1 */
  protected int getColumnWidth() {
    if (columnWidth == 0) {
      FontMetrics metrics = getFontMetrics(getFont());
      columnWidth = metrics.charWidth('m');
    }
    return columnWidth;
  }
  
  /** Returns the preferred size of this component.
    * @return The preferred size. */
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    size = (size == null) ? new Dimension(400,400) : size;
    size.width = (columns == 0) ? size.width : columns * getColumnWidth();
    size.height = (rows == 0) ? size.height : rows * getRowHeight();
    return size;
  }
  
  /** Returns the preferred size of the viewport if this component is
    * embedded in a JScrollPane. This uses the desired column
    * and row settings if they have been set, otherwise the superclass
    * behavior is used.
    * @return The preferredSize of a JViewport whose view is this Scrollable. */
  public Dimension getPreferredScrollableViewportSize() {
    Dimension size = super.getPreferredScrollableViewportSize();
    size = (size == null) ? new Dimension(400,400) : size;
    size.width = (columns == 0) ? size.width : columns * getColumnWidth();
    size.height = (rows == 0) ? size.height : rows * getRowHeight();
    return size;
  }
  
  /** Components that display logical rows or columns should compute
    * the scroll increment that will completely expose one new row
    * or column, depending on the value of orientation.
    * @param visibleRect the view area visible within the viewport
    * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
    * @param direction Less than zero to scroll up/left, greater than zero for down/right.
    * @return The "unit" increment for scrolling in the specified direction
    * @exception IllegalArgumentException for an invalid orientation
    */
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    switch (orientation) {
      case SwingConstants.VERTICAL: return getRowHeight();
      case SwingConstants.HORIZONTAL: return getColumnWidth();
      default: throw new IllegalArgumentException("Invalid orientation: " + orientation);
    }
  }
  
  /**
   * Keys to use for forward focus traversal when the JComponent is
   * managing focus.
   */
  private static Set<KeyStroke> managingFocusForwardTraversalKeys;
  
  /**
   * Keys to use for backward focus traversal when the JComponent is
   * managing focus.
   */
  private static Set<KeyStroke> managingFocusBackwardTraversalKeys;
  
  protected int rowHeight =0;
  protected int columnWidth = 0;
  protected int rows = 0;
  protected int columns = 0;
}
