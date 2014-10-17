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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.FileConfiguration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.GlobalModelTestCase.OverwriteException;
import edu.rice.cs.drjava.model.GlobalModelTestCase.WarningFileSelector;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;

import java.io.File;
import java.io.IOException;

/** Tests the functionality of the repl History.
 *  @version $Id: HistoryTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class HistoryTest extends DrJavaTestCase implements OptionConstants{
  private History _history;
  private File _tempDir;

  /** Initialize fields for each test. */
  public void setUp() throws Exception {
    super.setUp();
    DrJava.getConfig().resetToDefaults();
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    _history = new History();
  }

  /** Cleans up temporary files and tries to free used variables after each test. */
  public void tearDown() throws Exception {
    boolean ret = IOUtil.deleteRecursively(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);
    _tempDir = null;
    _history = null;
    super.tearDown();
  }

  /** Tests that the history doesn't overwrite files without prompting.
   */
  public void testSaveAsExistsForOverwrite() throws IOException {
    _history.add("some text");
    final File file1 = File.createTempFile("DrScala-test", ".hist", _tempDir).getCanonicalFile();
    file1.deleteOnExit();
    try {
      _history.writeToFile(new WarningFileSelector(file1));
      fail("Did not ask to verify overwrite as expected");
    }
    catch (OverwriteException e1) {
      // good behavior for file saving...
    }
  }

  public void testMultipleInsert() {
    _history.add("new Object()");
    _history.add("new Object()");
    assertEquals("Duplicate elements inserted", 2, _history.size());
  }

  public void testCanMoveToEmptyAtEnd() {
    String entry = "some text";
    _history.add(entry);

    _history.movePrevious("");
    assertEquals("Prev did not move to correct item", entry, _history.getCurrent());

    _history.moveNext(entry);
    assertEquals("Can't move to blank line at end",
                 "",
                 _history.getCurrent());
  }

  /** Ensures that Histories are bound to 500 entries. */
  public void testHistoryIsBounded() {
    final int maxLength = 500;
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { DrJava.getConfig().setSetting(HISTORY_MAX_SIZE, Integer.valueOf(maxLength)); }
    });
//    Utilities.clearEventQueue();
    
    int i;
    for (i = 0; i < maxLength + 100; i++) {
      _history.add("testing " + i);
    }
    for (; _history.hasPrevious(); i--) {
      _history.movePrevious("testing " + i);
    }

    assertEquals("History length is not bound to " + maxLength, maxLength, _history.size());
    assertEquals("History elements are not removed in FILO order", "testing 100", _history.getCurrent());
  }

  /** Tests that the history size can be updated, both through the config framework and the setMaxSize method.
   */
  public void testLiveUpdateOfHistoryMaxSize() {
    final int maxLength = 20;
    final FileConfiguration config = DrJava.getConfig();
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { config.setSetting(HISTORY_MAX_SIZE, Integer.valueOf(20)); }
    });
//    Utilities.clearEventQueue();

    for (int i = 0; i < maxLength; i++) {
      _history.add("testing " + i);
    }

    assertEquals("History size should be 20", 20, _history.size());
    
//    System.err.println("Setting HISTORY_MAX_SIZE to 10");

    Utilities.invokeAndWait(new Runnable() { public void run() { config.setSetting(HISTORY_MAX_SIZE, 10); } });
//    Utilities.clearEventQueue();
    
    assertEquals("History size should be 10", 10, _history.size());
    _history.setMaxSize(100);

    assertEquals("History size should still be 10", 10, _history.size());

    _history.setMaxSize(0);
    assertEquals("History size should be 0", 0, _history.size());

    Utilities.invokeAndWait(new Runnable() { 
      public void run() { config.setSetting(HISTORY_MAX_SIZE, Integer.valueOf(-1)); }
    });

//    Utilities.clearEventQueue();
    assertEquals("History size should still be 0", 0, _history.size());
  }

  /** Tests the getHistoryAsString() method. */
  public void testGetHistoryAsString() {
    final FileConfiguration config = DrJava.getConfig();
    
    Utilities.invokeAndWait(new Runnable() { public void run() { config.setSetting(HISTORY_MAX_SIZE, 10); } });
//    Utilities.clearEventQueue();

    assertEquals("testGetHistoryAsString:", "", _history.getHistoryAsString());

    String newLine = StringOps.EOL;

    _history.add("some text");
    assertEquals("testGetHistoryAsString:", "some text" + newLine, _history.getHistoryAsString());

    _history.add("some more text");
    _history.add("some text followed by a newline" + newLine);
    String str = 
      "some text" + newLine + "some more text" + newLine + "some text followed by a newline" + newLine + newLine;
    assertEquals("testGetHistoryAsString:", str, _history.getHistoryAsString());
  }

  /** Tests that the history remembers one edited entry for the given command. */
  public void testRemembersOneEditedEntry() {
    _history.add("some text");
    _history.movePrevious("");

    String newEntry = "some different text";

    _history.moveNext(newEntry);
    _history.movePrevious("");

//    Utilities.clearEventQueue();
    assertEquals("Did not remember the edited entry correctly.", newEntry, _history.getCurrent());
  }

  /** Tests that the history remembers multiple edited entries for the given command. */
  public void testRemembersMultipleEditedEntries() {
    _history.add("some text");
    _history.add("some more text");
    _history.movePrevious("");

    String newEntry1 = "some more different text";
    String newEntry2 = "some different text";

    _history.movePrevious(newEntry1);
    _history.moveNext(newEntry2);

//    Utilities.clearEventQueue();
    assertEquals("Did not remember the edited entry correctly.", newEntry1, _history.getCurrent());

    _history.movePrevious(newEntry1);
    
//    Utilities.clearEventQueue();
    assertEquals("Did not remember the edited entry correctly.", newEntry2, _history.getCurrent());
  }

  /** Tests that the original copy of an edited entry remains in the history. */
  public void testOriginalCopyRemains() {
    String entry = "some text";
    String newEntry = "some different text";

    _history.add(entry);
    _history.movePrevious("");
    _history.moveNext(newEntry);
    _history.movePrevious("");
    _history.add(newEntry);

    _history.movePrevious("");
    
//    Utilities.clearEventQueue();
    assertEquals("Did not add edited entry to end of history.", newEntry, _history.getCurrent());

    _history.movePrevious(newEntry);
    
//    Utilities.clearEventQueue();
    assertEquals("Did not keep a copy of the original entry.", entry, _history.getCurrent());
  }

  /** Tests that the tab completion of the most recent entry is correct. */
  public void testSearchHistory() {
    String entry1 = "some text";
    String entry2 = "blah";

    _history.add(entry1);
    _history.add(entry2);

    _history.reverseSearch("s");
    
//    Utilities.clearEventQueue();
    assertEquals("Did not find the correct entry in history.", entry1, _history.getCurrent());

    _history.forwardSearch("b");
    
//    Utilities.clearEventQueue();
    assertEquals("Did not find the correct entry in history.", entry2, _history.getCurrent());
  }

  /** Tests that if "tab completion" does not find a match, then cursor goes back to "end". */
  public void testNoMatch() {
    String entry1 = "some text";
    String entry2 = "blah";

    _history.add(entry1);
    _history.add(entry2);

    _history.reverseSearch("a");

//    Utilities.clearEventQueue();
    assertEquals("Did not reset cursor correctly.", "a", _history.getCurrent());
  }

  /** Tests reverse searching twice. */
  public void testReverseSearchTwice() {
    String entry1 = "same";
    String entry2 = "some";

    _history.add(entry1);
    _history.add(entry2);

    _history.reverseSearch("s");
    
//    Utilities.clearEventQueue();
    assertEquals("Did not reset cursor correctly.", entry2, _history.getCurrent());

    _history.reverseSearch(_history.getCurrent());
    
//    Utilities.clearEventQueue();
    assertEquals("Did not reset cursor correctly.", entry1, _history.getCurrent());
  }
  
  public void testSanityCheckConstructor() {
   
    History his =  new History(-1);
    his.add("Test String");
    assertEquals("History size is not 0", 0, his.size());
  }
  
  public void testOptionListenerToString() {
   
    History his = new History(10);
    String toS = his.historyOptionListener.toString();
    assertTrue("Did not return correct string representation",toS.startsWith("HISTORY_MAX_SIZE OptionListener #"));
  }
  
  public void testRemoveLast() {
   
    History his = new History(5);
    
    assertEquals("Didn't return null for an empty history",null, his.removeLast());
    
    his.add("test string 1");
    his.add("test string 2");
    his.add("test string 3");
    his.add("test string 4");
    his.add("test string 5");
    
    assertEquals("Did not return expected last value","test string 5",his.removeLast());
    
    his.moveEnd();
    
    assertEquals("Did not return expected last value","test string 4",his.removeLast());
    
    assertEquals("Did not return expected last value","test string 2",his.lastEntry());
  }
  
  public void testMoveMethods() {
    
    History his = new History(0);
    
    try {
      his.movePrevious("3");
      fail("Should not have moved previous, empty history");
    }
    catch(ArrayIndexOutOfBoundsException e) { 
    }
    
    try{ 
      his.moveNext("3");
      fail("Should not have moved next, empty history");
    }
    catch(ArrayIndexOutOfBoundsException e){
    }
  }
}
