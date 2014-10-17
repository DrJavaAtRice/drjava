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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.plt.io.IOUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/** Test functions of RecentFileManager.
  * @version $Id: RecentFileManagerTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class RecentFileManagerTest extends DrJavaTestCase {
  
  protected static final String FOO_TEXT = "class DrScalaTestFoo {}";
  protected static final String BAR_TEXT = "class DrScalaTestBar {}";
  private RecentFileManager _rfm;
  private JMenu _menu;
  protected File _tempDir;
  
  /** Creates a test suite for JUnit to run.
    * @return a test suite based on the methods in this class
    */
  public static Test suite() { return  new TestSuite(RecentFileManagerTest.class); }
  
  /** Setup method for each JUnit test case. */
  public void setUp() throws Exception {
    super.setUp();
    _menu = new JMenu();
    _rfm = new RecentFileManager(0, _menu, null, OptionConstants.RECENT_FILES);
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
  }
  
  public void tearDown() throws Exception {
    _menu = null;
    _rfm = null;
    IOUtil.deleteRecursively(_tempDir);
    _tempDir = null;
    super.tearDown();
  }
  
  /** Create a new temporary file in _tempDir.  Calls with the same int will return the same filename, while calls with
    * different ints will return different filenames.
    */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrScala-test", ".scala", _tempDir).getCanonicalFile();
  }
  
  /** Creates a new temporary file and writes the given text to it. The File object for the new file is returned. */
  protected File writeToNewTempFile(String text) throws IOException {
    File temp = tempFile();
    IOUtil.writeStringToFile(temp, text);
    return temp;
  }
  
  /** Tests that the size of the recent files list doesn't get bigger than the maximum size. */
  public void testAddMoreThanMaxSize() throws IOException {
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(1);
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file", FOO_TEXT, IOUtil.toString(vector.get(0)));
  }
  
  /** Tests that the size of the recent files list is reduced in response to a decrease in max size. */
  public void testShrinksToMaxSize() throws IOException {
    
    final File tempFile = writeToNewTempFile(BAR_TEXT);
    final File tempFile2 = writeToNewTempFile(FOO_TEXT);
    _rfm.updateMax(2);
    
    _rfm.updateOpenFiles(tempFile);
    _rfm.updateOpenFiles(tempFile2);
    Vector<File> vector = _rfm.getFileVector();
    assertEquals("number of recent files", 2, vector.size());
    assertEquals("text of most-recent file", FOO_TEXT, IOUtil.toString(vector.get(0)));
    assertEquals("text of second-most recent file", BAR_TEXT, IOUtil.toString(vector.get(1)));
    _rfm.updateMax(1);
    _rfm.numberItems();
    vector = _rfm.getFileVector();
    assertEquals("number of recent files", 1, vector.size());
    assertEquals("text of recent file", FOO_TEXT, IOUtil.toString(vector.get(0)));
  }
  
  /** Tests that files are removed correctly from the list. */
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
  
  /** Tests that the list is re-ordered correctly after a file is re-opened, even if it has a different path. */
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
    
    // Re-open tempFile with a different path, e.g. /tmp/MyFile -> /tmp/./MyFile
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
  
  /** Verifies that the presentation names for the directory filter are correct. */
  public void testDirectoryFilterDescription() {
    DirectoryFilter f = new DirectoryFilter();
    assertEquals("Should have the correct description.", "Directories", f.getDescription());
    f = new DirectoryFilter("Other directories");
    assertEquals("Should have allowed an alternate description.", "Other directories", f.getDescription());
  }
}
