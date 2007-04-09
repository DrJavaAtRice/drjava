package edu.rice.cs.util.swing;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Arrays;

import javax.swing.JOptionPane;

/**
 * A JUnit test case class that tests {@link ScrollableListSelectionDialog}.
 * 
 * @author Chris Warrington
 * @version $Id$
 * @since 2007-03-06
 */
public class ScrollableListSelectionDialogTest extends TestCase {
  private static final String TITLE = "DIALOG TITLE";
  private static final String LEADER = "DIALOG LEADER";
  private static final java.util.List<String> DATA = Arrays.asList("hello", "there");
  private static final String DESC = "DIALOG DESCRIPTION";
  
  /**
   * Tests that all the valid message types are accepted and that they
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
