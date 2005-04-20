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

package edu.rice.cs.drjava.ui;

import  junit.framework.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import edu.rice.cs.util.FileOps;

/**
 * Test functions of RecentFileManager.
 *
 * @version $Id$
 */
public final class RecentFileManagerTest extends TestCase {

  protected static final String FOO_TEXT = "class DrJavaTestFoo {}";
  protected static final String BAR_TEXT = "class DrJavaTestBar {}";
  private RecentFileManager _rfm;
  private JMenu _menu;
  protected File _tempDir;

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(RecentFileManagerTest.class);
  }

  /**
   * Setup method for each JUnit test case.
   */
  public void setUp() throws IOException {
    _menu = new JMenu();
    _rfm = new RecentFileManager(0, _menu, null,false);
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
  }

  public void tearDown() {
    _menu = null;
    _rfm = null;
    FileOps.deleteDirectory(_tempDir);
    _tempDir = null;
  }

  /**
   * Create a new temporary file in _tempDir.  Calls with the same
   * int will return the same filename, while calls with different
   * ints will return different filenames.
   */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java", _tempDir);
  }

  /**
   * Creates a new temporary file and writes the given text to it.
   * The File object for the new file is returned.
   */
  protected File writeToNewTempFile(String text) throws IOException {
    File temp = tempFile();
    FileOps.writeStringToFile(temp, text);
    return temp;
  }

  /**
   * Tests that the size of the recent files list doesn't get bigger than
   * the maximum size.
   */
  public void testAddMoreThanMaxSize() throws IOException {


    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(1);
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file",
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.get(0)));
  }

  /**
   * Tests that the size of the recent files list is reduced in response to a
   * decrease in max size.
   */
  public void testShrinksToMaxSize() throws IOException {

    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(2);

    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 2, vector.size());
    assertEquals("text of most-recent file",
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.get(0)));
    assertEquals("text of second-most recent file",
                 BAR_TEXT,
                 FileOps.readFileAsString(vector.get(1)));
    _rfm.updateMax(1);
    _rfm.numberItems();
    vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file",
                 FOO_TEXT,
                 FileOps.readFileAsString(vector.get(0)));

  }

  /**
   * Tests that files are removed correctly from the list.
   */
  public void testRemoveFile() throws Exception {
    // Open two files
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(2);
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("tempFile2 should be at top", vector.get(0), tempFile2);

    // Remove top
    _rfm.removeIfInList(tempFile2);
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("tempFile should be at top", vector.get(0), tempFile);

    // Remove non-existant entry
    _rfm.removeIfInList(tempFile2);
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("tempFile should still be at top", vector.get(0), tempFile);

    // Remove top again
    _rfm.removeIfInList(tempFile);
    assertEquals("number of recent files", 0, vector.size());
  }

  /**
   * Tests that the list is re-ordered correctly after a file is
   * re-opened, even if it has a different path.
   */
  public void testReopenFiles() throws Exception {
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);

    _rfm.updateMax(2);
    _rfm.updateOpenFiles(tempFile2);
    _rfm.updateOpenFiles(tempFile);
    Vector<File> vector = _rfm.getFileVector();

    assertEquals("tempFile should be at top", vector.get(0), tempFile);

    // Re-open tempFile2
    _rfm.updateOpenFiles(tempFile2);
    vector = _rfm.getFileVector();
    assertEquals("tempFile2 should be at top", vector.get(0), tempFile2);


    // Re-open tempFile with a different path
    //  eg. /tmp/MyFile -> /tmp/./MyFile
    File parent = tempFile.getParentFile();
    String dotSlash = "." + System.getProperty("file.separator");
    parent = new File(parent, dotSlash);
    File sameFile = new File(parent, tempFile.getName());

    _rfm.updateOpenFiles(sameFile);
    vector = _rfm.getFileVector();
    assertEquals("sameFile should be at top", vector.get(0), sameFile);
    assertEquals("should only have two files", 2, vector.size());
    assertTrue("should not contain tempFile", !(vector.contains(tempFile)));
  }

  /**
   * Verifies that the presentation names for the directory filter are correct.
   */
  public void testDirectoryFilterDescription() {
    DirectoryFilter f = new DirectoryFilter();
    assertEquals("Should have the correct description.",
                 "Directories", f.getDescription());
    f = new DirectoryFilter("Other directories");
    assertEquals("Should have allowed an alternate description.",
                 "Other directories", f.getDescription());
  }
}
