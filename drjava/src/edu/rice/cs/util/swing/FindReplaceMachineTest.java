/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import edu.rice.cs.util.UnexpectedException;

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
  Document docPrev;
  Document docNext;
  FindReplaceMachine frm;
  private static final String EVIL_TEXT = "Hear no evil, " +
    "see no evil, speak no evil.";
  private static final String EVIL_TEXT_PREV = "Hear no evilprev, " +
    "see no evilprev, speak no evilprev.";
  private static final String EVIL_TEXT_NEXT = "Hear no evilnext, " +
    "see no evilnext, speak no evilnext.";
  private static final String FIND_WHOLE_WORD_TEST_1 = 
      "public class Foo\n" +
      "{\n" + 
      "        /**\n" +
      "         * Barry Good!\n" +
      "         * (what I really mean is bar)\n" + 
      "         */\n" +
      "        public void bar() \n" +
      "        {\n" + 
      "                this.bar();\n" +
      "        }\n" +
      "}";

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
    docPrev = new PlainDocument();
    docNext = new PlainDocument();
    frm = new FindReplaceMachine(new DocumentIterator() {
      public Document getPrevDocument(Document d) {
        if (d == doc) {
          return docPrev;
        }
        else if (d == docPrev) {
          return docNext;
        }
        else {
          return doc;
        }
      }
      public Document getNextDocument(Document d) {
        if (d == doc) {
          return docNext;
        }
        else if (d == docPrev) {
          return doc;
        }
        else {
          return docPrev;
        }
      }
    });
    frm.setDocument(doc);
    // _initFrm(0);
  }

  /**
   * Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(FindReplaceMachineTest.class);
  }
  
  public void testCreateMachineSuccess() throws BadLocationException {
    doc.insertString(0, EVIL_TEXT, null);
    _initFrm(4);
  }
  
  private void _initFrm(int pos) {
    frm.setStart(pos);
    frm.setPosition(pos);
  }
  /*
  public void testCreateMachineFail() { 
    // before 0 
    try {
      _initFrm(-2);
      System.err.println(frm.getStartOffset() + " " + 
                         frm.getCurrentOffset());
      fail("creating invalid position in constructor");
    } catch (UnexpectedException e) { 
      // expected: -2 is not a valid offset.
    }
    
    // after doc.getLength()
    try {
      _initFrm(5);
      System.out.println(frm.getStartOffset() + " " + 
                         frm.getCurrentOffset());
      fail("creating invalid position in constructor");
    } catch (UnexpectedException e) {
      // expected: 5 is larger than document
    }
  }*/

  public void testFindNextUpdatesCurrent() throws BadLocationException { 
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(0);
    _assertOffsets(frm, 0, 0); 
    frm.setFindWord("evil"); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 12); 
  }

  public void testFindNextAndFailIsOnMatch() throws BadLocationException { 
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(0);
    _assertOffsets(frm, 0, 0); 
    frm.setFindWord("evil"); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 12); 
    doc.insertString(9, "-", null); 
    assertTrue("no longer on find text", !frm.isOnMatch()); 
  }

  public void testMultipleCallsToFindNext() throws BadLocationException {     
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(0);
    _assertOffsets(frm, 0, 0);
    frm.setFindWord("evil"); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 12); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 25); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 40); 
  } 
  
  public void testStartFromTopContinue() throws BadLocationException { 
    doc.insertString(0, EVIL_TEXT, null);
    _initFrm(5);
    _assertOffsets(frm, 5, 5); 
    frm.setFindWord("Hear"); 
    _testFindNextSucceeds(frm, CONTINUE, 5, 4); 
  } 
  
  /**
  // halting tests are obsolete.  We don't halt anymore. we always wrap.
  public void testStartFromTopHalt() throws BadLocationException { 
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(5);
    _assertOffsets(frm, 5, 5); 
    frm.setFindWord("Hear"); 
    _testFindNextFails(frm, HALT, 5, 5); 
  } 
  */

  public void testNotInDocument() throws BadLocationException { 
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(5);
    _assertOffsets(frm, 5, 5);
    frm.setFindWord("monkey"); 
    _testFindNextFails(frm, CONTINUE, 5, 5); 
  }
 
  public void testSimpleReplace() throws BadLocationException { 
    doc.insertString(0,EVIL_TEXT, null); 
    _initFrm(0);
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
    _initFrm(15); 
    _assertOffsets(frm, 15, 15); 
    frm.setFindWord("evil"); 
    frm.setReplaceWord("monkey"); 
    frm.replaceAll();
    assertEquals("revised text", 
                 "Hear no monkey, see no monkey, speak no monkey.", 
                 doc.getText(0, doc.getLength()));     
  }

  public void testFindNoMatchCase() throws BadLocationException{
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(0);
    _assertOffsets(frm, 0, 0);
    frm.setMatchCase(false);
    frm.setFindWord("eViL"); 
    _testFindNextSucceeds(frm, CONTINUE, 0, 12); 
  }

  public void testReplaceAllContinueNoMatchCase() throws BadLocationException{
    doc.insertString(0, EVIL_TEXT, null); 
    _initFrm(15); 
    _assertOffsets(frm, 15, 15); 
    frm.setFindWord("eViL"); 
    frm.setReplaceWord("monkey");
    frm.setMatchCase(false);
    frm.replaceAll();
    assertEquals("revised text", 
                 "Hear no monkey, see no monkey, speak no monkey.", 
                 doc.getText(0, doc.getLength()));     
  }

  public void testReplaceAllBackwards() throws BadLocationException {
    doc.insertString(0, "hElo helO", null);
    _initFrm(3);
    frm.setFindWord("heLo");
    frm.setReplaceWord("cool");
    frm.setMatchCase(false);
    frm.setSearchBackwards(true);
    frm.replaceAll();
    assertEquals("backwards replace", "cool cool", doc.getText(0, doc.getLength()));
  }

  public void testFindMatchWithCaretInMiddle() throws BadLocationException {
    doc.insertString(0, "hello hello", null);
    _initFrm(3);
    frm.setFindWord("hello");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    _testFindNextSucceeds(frm, CONTINUE, 3, 11);
    _testFindNextSucceeds(frm, CONTINUE, 3, 5);
  }

  public void testFindMatchWithCaretInMiddleBackwards() throws BadLocationException {
    doc.insertString(0, "hello hello", null);
    _initFrm(8);
    frm.setFindWord("helLo");
    frm.setMatchCase(false);
    frm.setSearchBackwards(true);
    _testFindNextSucceeds(frm, CONTINUE, 8, 0);
    _testFindNextSucceeds(frm, CONTINUE, 8, 6);
  }

  /**
   * This tests that a replace all where the replacement action creates a new match
   * does not replace this new match
   */
  public void testReplaceCreatesMatch() throws BadLocationException {
    doc.insertString(0, "hhelloello", null);
    _initFrm(1);
    frm.setFindWord("hello");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    frm.setReplaceWord("");
    frm.replaceAll();
    assertEquals("replace creates new match", "hello", doc.getText(0, doc.getLength()));
  }

  /**
   * This tests that a replace all where the replacement action creates a new match
   * does not replace this new match
   */
  public void testReplaceCreatesMatchBackwards() throws BadLocationException {
    doc.insertString(0, "hhelloello", null);
    _initFrm(1);
    frm.setFindWord("hello");
    frm.setMatchCase(false);
    frm.setSearchBackwards(true);
    frm.setReplaceWord("");
    frm.replaceAll();
    assertEquals("replace creates new match", "hello", doc.getText(0, doc.getLength()));
  }

  /**
   * This test checks that replacing a word with itself will halt on replace all
   */
  public void testReplaceAllSameWord() throws BadLocationException {
    doc.insertString(0, "cool cool", null);
    _initFrm(3);
    frm.setFindWord("cool");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    frm.setReplaceWord("cool");
    frm.replaceAll();
    frm.setSearchBackwards(true);
    frm.replaceAll();
    assertEquals("replace all with the same word", "cool cool", doc.getText(0, doc.getLength()));
  }

  /**
   * This test checks that a findNext won't find two matches that partially overlap.
   * This is the current behavior of the FindReplaceMachine, though at some time
   * in the future someone may want to change it
   */
  public void testFindPartialSubstrings() throws BadLocationException {
    doc.insertString(0, "ooAooAoo", null);
    _initFrm(0);
    frm.setFindWord("ooAo");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    _testFindNextSucceeds(frm, CONTINUE, 0, 4);
    _testFindNextSucceeds(frm, CONTINUE, 0, 4);

    _initFrm(8);
    frm.setSearchBackwards(true);
    _testFindNextSucceeds(frm, CONTINUE, 8, 3);
    _testFindNextSucceeds(frm, CONTINUE, 8, 3);
  }
  
  /**
   * This test addresses bug #745714 Searches Repeat When Changing Direction.
   * The word that was just found should not be found again after toggling
   * the search backwards flag.
   */
  public void testSearchesDoNotRepeatWhenChangingDirection() throws BadLocationException {
    doc.insertString(0, "int int int", null);
    _initFrm(0);
    frm.setFindWord("int");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    _testFindNextSucceeds(frm, CONTINUE, 0, 3);
    _testFindNextSucceeds(frm, CONTINUE, 0, 7);
    
    frm.setLastFindWord();
    frm.setSearchBackwards(true);
    _testFindNextSucceeds(frm, CONTINUE, 0, 0);
    
    frm.setLastFindWord();
    frm.setSearchBackwards(false);
    _testFindNextSucceeds(frm, CONTINUE, 0, 7);
    
    frm.setLastFindWord();
    frm.positionChanged();
    frm.setSearchBackwards(true);
    _testFindNextSucceeds(frm, CONTINUE, 0, 4);
    
  }
  
  /**
   * This test addresses feature request #784514 Find/Replace in all Open Files.
   */
  public void testFindReplaceInAllOpenFiles() throws BadLocationException {    
    doc.insertString(0, EVIL_TEXT, null); 
    docPrev.insertString(0, EVIL_TEXT_PREV, null); 
    docNext.insertString(0, EVIL_TEXT_NEXT, null); 
    // put the caret after the last instance of the findWord in doc
    _initFrm(40);
    frm.setFindWord("evil");
    frm.setMatchCase(false);
    frm.setSearchBackwards(false);
    frm.setSearchAllDocuments(true);
    _testFindNextSucceeds(frm, CONTINUE, 12, 12, docNext);
    _testFindNextSucceeds(frm, CONTINUE, 12, 29, docNext);
    _testFindNextSucceeds(frm, CONTINUE, 12, 48, docNext);
    _testFindNextSucceeds(frm, CONTINUE, 12, 12, docPrev);
    _testFindNextSucceeds(frm, CONTINUE, 12, 29, docPrev);
    _testFindNextSucceeds(frm, CONTINUE, 12, 48, docPrev);
    _testFindNextSucceeds(frm, CONTINUE, 12, 12, doc);
    _testFindNextSucceeds(frm, CONTINUE, 12, 25, doc);
    _testFindNextSucceeds(frm, CONTINUE, 12, 40, doc);
    _testFindNextSucceeds(frm, CONTINUE, 12, 12, docNext);
    frm.setLastFindWord();
    frm.setSearchBackwards(true);
    _testFindNextSucceeds(frm, CONTINUE, 36, 36, doc);
    _testFindNextSucceeds(frm, CONTINUE, 36, 21, doc);
    _testFindNextSucceeds(frm, CONTINUE, 36, 8, doc);
    _testFindNextSucceeds(frm, CONTINUE, 44, 44, docPrev);
    frm.setReplaceWord("monkey"); 
    frm.replaceAll();
    assertEquals("revised text", 
                 "Hear no monkey, see no monkey, speak no monkey.", 
                 doc.getText(0, doc.getLength()));
    assertEquals("revised text", 
                 "Hear no monkeyprev, see no monkeyprev, speak no monkeyprev.", 
                 docPrev.getText(0, docPrev.getLength()));
    assertEquals("revised text", 
                 "Hear no monkeynext, see no monkeynext, speak no monkeynext.", 
                 docNext.getText(0, docNext.getLength()));
  }


    public void testWholeWordSearchOnTestString1() throws BadLocationException {
        doc.insertString(0, FIND_WHOLE_WORD_TEST_1, null);
        System.err.println(FIND_WHOLE_WORD_TEST_1);
        _initFrm(0);
        frm.setFindWord("bar");
        frm.setMatchWholeWord();
        frm.setSearchBackwards(false);
        
        _testFindNextSucceeds(frm, CONTINUE, 0, 91);
        _testFindNextSucceeds(frm, CONTINUE, 0, 128);
        _testFindNextSucceeds(frm, CONTINUE, 0, 166);
        frm.setLastFindWord();
        frm.setSearchBackwards(true);
        _testFindNextSucceeds(frm, CONTINUE, 0, 125);
        _testFindNextSucceeds(frm, CONTINUE, 0, 88);
        _testFindNextSucceeds(frm, CONTINUE, 0, 163);

        frm.setFindWord("ubl");
        _testFindNextFails(frm, CONTINUE, 0, 163);

        frm.setSearchBackwards(false);
        frm.setFindWord("pub");
        _testFindNextFails(frm, CONTINUE, 0, 163);

        frm.setSearchBackwards(true);
        frm.setFindWord("pub");
        _testFindNextFails(frm, CONTINUE, 0, 163);
    }

  /**
   test case no longer applies -- we always wrap
   public void testReplaceAllHalt() throws BadLocationException {
   doc.insertString(0, EVIL_TEXT, null);
   _initFrm(15);
   _assertOffsets(frm, 15, 15);
   frm.setFindWord("evil");
   frm.setReplaceWord("monkey");
   frm.replaceAll(HALT);
   assertEquals("revised text",
   "Hear no evil, see no monkey, speak no monkey.",
   doc.getText(0, doc.getLength()));
   }
  **/
  private void _testFindNextSucceeds(FindReplaceMachine frm, ContinueCommand cont, 
                             int start, int found, Document doc) {
    FindResult fr = frm.findNext();
    Document d = fr.getDocument();
    if (frm.getDocument() != d) {
      // do  FindReplaceDialog's _updateMachine
      frm.setDocument(d);
      frm.setStart(found);
      frm.setPosition(found);
    }
    assertEquals("documents should equal", doc, frm.getDocument());
    assertEquals("findNext return value", found, fr.getFoundOffset()); 
    _assertOffsets(frm, start, found); 
    assertTrue("on find text", frm.isOnMatch());  
  }

  private void _testFindNextSucceeds(FindReplaceMachine frm, ContinueCommand cont, 
                             int start, int found) { 
    int findOffset = frm.findNext().getFoundOffset(); 
    assertEquals("findNext return value", found, findOffset); 
    _assertOffsets(frm, start, found); 
    assertTrue("on find text", frm.isOnMatch());     
  } 
  
  private void _testFindNextFails(FindReplaceMachine frm, ContinueCommand cont, 
                                  int start, int current)  
  { 
    int findOffset = frm.findNext().getFoundOffset(); 
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
  
  /**
  private static ContinueCommand HALT = new ContinueCommand() {  
    public boolean shouldContinue() {  
      return false;  
    }  
  }; 
  **/
}
