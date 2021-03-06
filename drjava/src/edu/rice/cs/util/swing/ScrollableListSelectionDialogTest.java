/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2019, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/
package edu.rice.cs.util.swing;

import junit.framework.TestCase;

import java.util.Arrays;

import javax.swing.JOptionPane;

/** * A JUnit test case class that tests {@link ScrollableListSelectionDialog}.
 * 
 * @author Chris Warrington
 * @version $Id$
 * @since 2007-03-06
 */
public class ScrollableListSelectionDialogTest extends  TestCase {
  private static final String TITLE = "DIALOG TITLE";
  private static final String LEADER = "DIALOG LEADER";
  private static final java.util.List<String> DATA = Arrays.asList("hello", "there");
  private static final String DESC = "DIALOG DESCRIPTION";
  
  /** Tests that all the valid message types are accepted and that they
   * are the only message types accepted.
   */
  public void testValidMessageTypes() {
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.ERROR_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, JOptionPane.INFORMATION_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.INFORMATION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, JOptionPane.WARNING_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.WARNING_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, JOptionPane.QUESTION_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.QUESTION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, JOptionPane.PLAIN_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.PLAIN_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, -128);
      fail("-128 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC, ScrollableListSelectionDialog.SelectionState.SELECTED, 42);
      fail("42 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
  
  public void testNullData() {
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC);
    } catch (IllegalArgumentException e) {
      fail("Data was non-null, so it should have been accepted.");
    }
    
    try {
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, null, DESC);
      fail("Data was null, so it shouldn't have been accepted.");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
  
  public void testDefaultSelection() {
    //test default default selection
    ScrollableListSelectionDialog dialog =
      new  ScrollableListSelectionDialog(null, TITLE, LEADER, DATA, DESC);
    java.util.List<String> selectedItems = dialog.selectedItems();
    
    assertEquals(DATA.size(), selectedItems.size());
    for (int i = 0; i < DATA.size(); ++i) {
      assertEquals(DATA.get(i), selectedItems.get(i));
    }
    
    //test default SELECTED
    dialog =
      new ScrollableListSelectionDialog(null,
                                        TITLE,
                                        LEADER,
                                        DATA,
                                        DESC, 
                                        ScrollableListSelectionDialog.SelectionState.SELECTED,
                                        JOptionPane.PLAIN_MESSAGE);
    selectedItems = dialog.selectedItems();
    
    assertEquals(DATA.size(), selectedItems.size());
    for (int i = 0; i < DATA.size(); ++i) {
      assertEquals(DATA.get(i), selectedItems.get(i));
    }
    
    //test default UNSELECTED
        dialog =
      new ScrollableListSelectionDialog(null,
                                        TITLE,
                                        LEADER,
                                        DATA,
                                        DESC, 
                                        ScrollableListSelectionDialog.SelectionState.UNSELECTED,
                                        JOptionPane.PLAIN_MESSAGE);
    selectedItems = dialog.selectedItems();
    assertEquals(0, selectedItems.size());
  }
}
