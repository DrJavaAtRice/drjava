package edu.rice.cs.util.swing;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Arrays;

import javax.swing.JOptionPane;

/**
 * A JUnit test case class that tests {@link ScrollableListDialog}.
 * 
 * @author Chris Warrington
 * @version $Id$
 * @since 2007-03-06
 */
public class ScrollableListDialogTest extends TestCase {
  private static final String TITLE = "DIALOG TITLE";
  private static final String LEADER = "DIALOG LEADER";
  private static final Collection<String> DATA = Arrays.asList("hello", "there");
  
  /**
   * Tests that all the valid message types are accepted and that they
   * are the only message types accepted.
   */
  public void testValidMessageTypes() {
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.ERROR_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, JOptionPane.INFORMATION_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.INFORMATION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, JOptionPane.WARNING_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.WARNING_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, JOptionPane.QUESTION_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.QUESTION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, JOptionPane.PLAIN_MESSAGE);
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.PLAIN_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, -128);
      fail("-128 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA, 42);
      fail("42 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
  
  public void testNullData() {
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, DATA);
    } catch (IllegalArgumentException e) {
      fail("Data was non-null, so it should have been accepted.");
    }
    
    try {
      new  ScrollableListDialog(null, TITLE, LEADER, null);
      fail("Data was null, so it shouldn't have been accepted.");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
}
