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

package edu.rice.cs.util.swing;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Document;

/**
 * Test the interactions between double quotes and backslashes.
 * @version $Id$
 */
public class FindReplaceMachineTest extends TestCase
{
  Document doc;
  private static final String EVIL_TEXT = "Hear no evil, " +
    "see no evil, speak no evil.";
  /**
   * Constructor.
   * @param name a name for the test.
   */
  public FindReplaceMachineTest(String name) {
    super(name);
  }

  /**
   * Initializes the document for the tests.
   */
  protected void setUp() {
    doc = new PlainDocument();
  }

  /**
   * Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(FindReplaceMachineTest.class);
  }
  
  public void testCreateMachineSuccess() throws BadLocationException {
    FindReplaceMachine frm = new FindReplaceMachine(doc,0);
    doc.insertString(0, EVIL_TEXT, null);
    frm = new FindReplaceMachine(doc, 4);
  }
  
  public void testCreateMachineFail() {
    // before 0
    try {
      FindReplaceMachine frm = new FindReplaceMachine(doc,-1);
      System.out.println(frm.getStartOffset() + " " + 
                         frm.getCurrentOffset());
      assertTrue("creating invalid position in constructor", false);
    } catch (BadLocationException e) {
    }
    
    // after doc.getLength()
    try {
      FindReplaceMachine frm = new FindReplaceMachine(doc,5);
      System.out.println(frm.getStartOffset() + " " + 
                         frm.getCurrentOffset());
      assertTrue("creating invalid position in constructor", false);
    } catch (BadLocationException e) {
    }
  }
  
  public void testFindNextUpdatesCurrent() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 0);
    _assertOffsets(frm, 0, 0);
    frm.setFindWord("evil");
    _testFindNextSucceeds(frm, CONTINUE, 0, 12);
  }

  public void testFindNextAndFailIsOnMatch() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 0);
    _assertOffsets(frm, 0, 0);
    frm.setFindWord("evil");
    _testFindNextSucceeds(frm, CONTINUE, 0, 12);
    doc.insertString(9, "-", null);
    assertTrue("no longer on find text", !frm.isOnMatch());
  }

  public void testMultipleCallsToFindNext() throws BadLocationException {    
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 0);
    _assertOffsets(frm, 0, 0);
    frm.setFindWord("evil");
    _testFindNextSucceeds(frm, CONTINUE, 0, 12);
    _testFindNextSucceeds(frm, CONTINUE, 0, 25);
    _testFindNextSucceeds(frm, CONTINUE, 0, 40);
  }
  
  public void testStartFromTopContinue() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 5);
    _assertOffsets(frm, 5, 5);
    frm.setFindWord("Hear");
    _testFindNextSucceeds(frm, CONTINUE, 5, 4);
  }
  
  public void testStartFromTopHalt() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 5);    
    _assertOffsets(frm, 5, 5);
    frm.setFindWord("Hear");
    _testFindNextFails(frm, HALT, 5, 5);
  }
 
  public void testNotInDocument() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 5);    
    _assertOffsets(frm, 5, 5);
    frm.setFindWord("monkey");
    _testFindNextFails(frm, CONTINUE, 5, 5);
    
  }
 
  public void testSimpleReplace() throws BadLocationException {
    doc.insertString(0,EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 0);
    _assertOffsets(frm, 0, 0);
    frm.setFindWord("evil");
    frm.setReplaceWord("monkey");
    _testFindNextSucceeds(frm, CONTINUE, 0, 12);
    frm.replaceCurrent();
    assertEquals("new replaced text",
                 "Hear no monkey, see no evil, speak no evil.",
                 doc.getText(0, doc.getLength()));
  }
  
  public void testReplaceAllContinue() throws BadLocationException{
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 15);
    _assertOffsets(frm, 15, 15);
    frm.setFindWord("evil");
    frm.setReplaceWord("monkey");
    frm.replaceAll(CONTINUE);
    assertEquals("revised text",
                 "Hear no monkey, see no monkey, speak no monkey.",
                 doc.getText(0, doc.getLength()));    
  }
  
  public void testReplaceAllHalt() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    FindReplaceMachine frm = new FindReplaceMachine(doc, 15);
    _assertOffsets(frm, 15, 15);
    frm.setFindWord("evil");
    frm.setReplaceWord("monkey");
    frm.replaceAll(HALT);
    assertEquals("revised text",
                 "Hear no evil, see no monkey, speak no monkey.",
                 doc.getText(0, doc.getLength()));    
  }
  
  private void _testFindNextSucceeds(FindReplaceMachine frm, ContinueCommand cont,
                             int start, int found) {
    int findOffset = frm.findNext(cont);
    assertEquals("findNext return value", found, findOffset);
    _assertOffsets(frm, start, found);
    assertTrue("on find text", frm.isOnMatch());    
  }
  
  private void _testFindNextFails(FindReplaceMachine frm, ContinueCommand cont,
                                  int start, int current) 
  {
    int findOffset = frm.findNext(cont);
    assertEquals("findNext return value", -1, findOffset);
    _assertOffsets(frm, start, current);
  }                                  
  
  private void _assertOffsets(FindReplaceMachine frm, int start, int current) {  
    assertEquals("start offset", start, frm.getStartOffset());
    assertEquals("current offset", current, frm.getCurrentOffset());
  }
  
  private static ContinueCommand CONTINUE = new ContinueCommand() {
    public boolean shouldContinue() {
      return true;
    }
  };
  
  private static ContinueCommand HALT = new ContinueCommand() {
    public boolean shouldContinue() {
      return false;
    }
  };

}
