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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.text.EditDocumentException;

/** Tests the functionality of the InteractionsDocument.  Most history functionality is tested in HistoryTest.
 *  @version $Id: InteractionsDocumentTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class InteractionsDocumentTest extends DrJavaTestCase {
  protected InteractionsDocument _doc;
  
  static final String TEST_BANNER = "This is a test banner";
  
  /** Initialize fields for each test. */
  protected void setUp() throws Exception {
    super.setUp();
    // Use System.getProperty("user.dir") as working directory here and in call on reset(...) below
    _doc = new InteractionsDocument(new InteractionsDJDocument());
    _doc.setBanner(TEST_BANNER);
  }

  /** Tests that the document prevents editing before the prompt, and beeps if you try. */
  public void testCannotEditBeforePrompt() throws EditDocumentException {
    TestBeep testBeep = new TestBeep();
    _doc.setBeep(testBeep);
    int origLength = _doc.getLength();

    // Try to insert into the banner
    _doc.insertText(1, "text", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Number of beeps", 1, testBeep.numBeeps);
    assertEquals("Doc length", origLength, _doc.getLength());
  }

  /** Tests that clear current interaction works. */
  public void testClearCurrent() throws EditDocumentException {
    int origLength = _doc.getLength();
    _doc.insertText(origLength, "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Length after inserts", origLength + 10, _doc.getLength()); // orig + "before" + "text"
    _doc.clearCurrentInteraction();
    assertEquals("Length after clear", origLength + 6, _doc.getLength()); // orig + "before"
  }

  /** Tests that initial contents are the banner and prompt, and that reset works. */
  public void testContentsAndReset() throws EditDocumentException {
    String banner = TEST_BANNER;
    String prompt = _doc.getPrompt();
    String newBanner = "THIS IS A NEW BANNER\n";
    assertEquals("Contents before insert", banner + prompt, _doc.getDocText(0, _doc.getLength()));
    // Insert some text
    _doc.insertText(_doc.getLength(), "text", InteractionsDocument.DEFAULT_STYLE);
    _doc.insertBeforeLastPrompt("before", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("Contents before reset", banner + "before" + prompt + "text",  
                 _doc.getDocText(0, _doc.getLength()));
    _doc.reset(newBanner);
    assertEquals("Contents after reset", newBanner + prompt, _doc.getDocText(0, _doc.getLength()));
  }

  /** Tests that inserting a newline works. */
  public void testInsertNewline() throws EditDocumentException {
    int origLength = _doc.getLength();
    _doc.insertText(origLength, "command", InteractionsDocument.DEFAULT_STYLE);
    assertEquals("current interaction before newline", "command", _doc.getCurrentInteraction());
    _doc.insertNewline(origLength + 2);
    assertEquals("current interaction after newline", "co" + "\n" /* formerly StringOps.EOL */ + "mmand",
                 _doc.getCurrentInteraction());
  }

  /** Tests that recalling commands from the history works. */
  public void testRecallFromHistory() throws EditDocumentException {
    String origText = _doc.getDocText(0, _doc.getLength());
    _doc.addToHistory("command");
    assertEquals("Contents before recall prev", origText, _doc.getDocText(0, _doc.getLength()));

    _doc.recallPreviousInteractionInHistory();
    assertEquals("Contents after recall prev", origText + "command", _doc.getDocText(0, _doc.getLength()));

    _doc.recallNextInteractionInHistory();
    assertEquals("Contents after recall next", origText, _doc.getDocText(0, _doc.getLength()));
  }


  /** Silent beep for a test class. */
  public static class TestBeep implements Runnable {
    int numBeeps = 0;
    public void run() { numBeeps++; }
  }
}



