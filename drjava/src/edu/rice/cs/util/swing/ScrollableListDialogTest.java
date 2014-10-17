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

import junit.framework.TestCase;

import java.util.Arrays;

import javax.swing.JOptionPane;

/**
 * A JUnit test case class that tests {@link ScrollableListDialog}.
 * 
 * @author Chris Warrington
 * @version $Id: ScrollableListDialogTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 * @since 2007-03-06
 */
public class ScrollableListDialogTest extends TestCase {
  private static final String TITLE = "DIALOG TITLE";
  private static final String LEADER = "DIALOG LEADER";
  private static final java.util.List<String> DATA = Arrays.asList("hello", "there");
  
  /**
   * Tests that all the valid message types are accepted and that they
   * are the only message types accepted.
   */
  public void testValidMessageTypes() {
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(JOptionPane.ERROR_MESSAGE).build();
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.ERROR_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(JOptionPane.INFORMATION_MESSAGE).build();
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.INFORMATION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(JOptionPane.WARNING_MESSAGE).build();
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.WARNING_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(JOptionPane.QUESTION_MESSAGE).build();
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.QUESTION_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(JOptionPane.PLAIN_MESSAGE).build();
    } catch (IllegalArgumentException e) {
      fail("JOptionPane.PLAIN_MESSAGE is a valid message type");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(-128).build();
      fail("-128 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).setMessageType(42).build();
      fail("42 is not a valid message type");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
  
  public void testNullData() {
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(DATA).build();
    } catch (IllegalArgumentException e) {
      fail("Data was non-null, so it should have been accepted.");
    }
    
    try {
      new  ScrollableListDialog.Builder<String>()
        .setOwner(null).setTitle(TITLE).setText(LEADER).setItems(null).build();
      fail("Data was null, so it shouldn't have been accepted.");
    } catch (IllegalArgumentException e) {
      //we're good
    }
  }
}
