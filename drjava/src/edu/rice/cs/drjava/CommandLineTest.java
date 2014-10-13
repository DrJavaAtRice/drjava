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

package edu.rice.cs.drjava;

import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;

/**
 * Tests opening/creating files specified as command line arguments.
 * @version $Id: CommandLineTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class CommandLineTest extends DrJavaTestCase {
  /** File separator, i.e. '/' or '\\'. */
  private static final char FS = File.separatorChar;
  
  /** The MainFrame we're working with. */
  private MainFrame _mf;
  
  /** Files that exist, and the filenames that represent them. */
  private volatile File f1;
  private volatile String f1_name;
  private volatile String f1_contents;
  private volatile File f2;
  private volatile String f2_name;
  private volatile String f2_contents;
  private volatile File f3;
  private volatile String f3_name;
  private volatile String f3_contents;
  private volatile File f4;
  private volatile String f4_name;
  private volatile String f4_contents;
  private volatile File f5;
  private volatile String f5_name;
  private volatile String f5_contents;
  private volatile File f6;
  private volatile String f6_name;
  private volatile String f6_contents;
  private volatile File f7;
  private volatile String f7_name;
  private volatile String f7_contents;
  private volatile File f8;
  private volatile String f8_name;
  private volatile String f8_contents;
  
  
  /** Files that do not exist (constructor deletes them), and their filenames. */
  private volatile File nof1;
  private volatile File nof2;
  private volatile File nof3;
  private volatile File nof4;
  private volatile File nof5;
  private volatile String nof1_name;
  private volatile String nof2_name;
  private volatile String nof3_name;
  private volatile String nof4_name;
  private volatile String nof5_name;
  
  private Log _log = new Log("CommandLineTest.txt", false);
  
  /** Constructor.  Sets up test files for us to use: (i) three files that exist and can be opened; (ii) three
    * files that don't exist
    * @param name the name of the test case
    */
  public CommandLineTest(String name) { super(name); }
  
  public void setUp() throws Exception {
    super.setUp();
    
//    _log.log("INVOKing DrJava._initConfig() for " + this);
//  Perform Swing initialization in event thread because the event thread is already running
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        
        _log.log("Creating a MainFrame for " + this);
        _mf = new MainFrame(); 
        _log.log("Created a MainFrame for " + this + "; stating file setup");
        
        try {
          f1 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f1.deleteOnExit();
          f1_name = f1.getAbsolutePath();
          f1_contents = "abcde";
          FileWriter fw1 = new FileWriter(f1);
          fw1.write(f1_contents,0,f1_contents.length());
          fw1.close();
          f2 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f2.deleteOnExit();
          f2_name = f2.getAbsolutePath();
          f2_contents = "fghijklm";
          FileWriter fw2 = new FileWriter(f2);
          fw2.write(f2_contents,0,f2_contents.length());
          fw2.close();
          f3 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f3.deleteOnExit();
          f3_name = f3.getAbsolutePath();
          f3_contents = "nopqrstuvwxyz";
          FileWriter fw3 = new FileWriter(f3);
          fw3.write(f3_contents,0,f3_contents.length());
          fw3.close();
          f4 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f4.deleteOnExit();
          f4_name = f4.getAbsolutePath();
          f4_contents = "abcde";
          FileWriter fw4 = new FileWriter(f4);
          fw4.write(f4_contents,0,f4_contents.length());
          fw4.close();
          f5 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f5.deleteOnExit();
          f5_name = f5.getAbsolutePath();
          f5_contents = "fghijklm";
          FileWriter fw5 = new FileWriter(f5);
          fw5.write(f5_contents,0,f5_contents.length());
          fw5.close();
          f6 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f6.deleteOnExit();
          f6_name = f6.getAbsolutePath();
          f6_contents = "nopqrstuvwxyz";
          FileWriter fw6 = new FileWriter(f6);
          fw6.write(f6_contents,0,f6_contents.length());
          fw6.close();
          f7 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f7.deleteOnExit();
          f7_name = f7.getAbsolutePath();
          f7_contents = "abcde";
          FileWriter fw7 = new FileWriter(f7);
          fw7.write(f7_contents,0,f7_contents.length());
          fw7.close();
          f8 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          f8.deleteOnExit();
          f8_name = f8.getAbsolutePath();
          f8_contents = "fghijklm";
          FileWriter fw8 = new FileWriter(f8);
          fw8.write(f8_contents,0,f8_contents.length());
          fw8.close();
          
          nof1 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          nof1_name = nof1.getAbsolutePath();
          nof1.delete();
          nof2 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          nof2_name = nof2.getAbsolutePath();
          nof2.delete();
          nof3 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          nof3_name = nof3.getAbsolutePath();
          nof3.delete();
          nof4 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          nof4_name = nof4.getAbsolutePath();
          nof4.delete();
          nof5 = File.createTempFile("DrScala-test", ".scala").getCanonicalFile();
          nof5_name = nof5.getAbsolutePath();
          nof5.delete();
          
          _log.log("File initialization (setUp) is complete");
        }
        catch (IOException e) {
          System.out.print("createTempFile failed.  This should not happen.");
          throw new RuntimeException(e.toString());
        }
      }
    });
  }
  
  public void tearDown() throws Exception {
    Utilities.invokeAndWait(new Runnable() { public void run() { _mf.dispose(); } });
    _mf = null;
    super.tearDown();
  }
  
  /** Tests DrScala with no command line arguments. Should open a new, untitled document. */
  public void testNone() {
    DrJavaRoot.openCommandLineFiles(_mf, new String[0],false);
    // ListModel<DefinitionsDocument> docs =
    // Wouldn't that be nice?
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    assertEquals("Only one document?", 1, docs.size());
    OpenDefinitionsDocument doc = docs.get(0);
    assertTrue("Is new document untitled?", doc.isUntitled());
    _log.log("testNone() completed");
    
  }
  
  /** Open one file on the command line.  Should (obviously) open that file. */
  public void testOpenOne() throws BadLocationException {
    String[] list = new String[1];
    list[0] = f1_name;
    DrJavaRoot.openCommandLineFiles(_mf, list,false);
//    _log.log("openCommandLineFiles completed");
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
//    Utilities.showDebug(docs.toString());
//    _log.log("got OpenDefDocs");
    assertEquals("Only one document opened?", 1, docs.size());
    OpenDefinitionsDocument doc = docs.get(0);
//    System.err.println("Doc text = " + doc.getText());
    assertEquals("Correct length of file?", f1_contents.length(), doc.getLength());
//    _log.log("Ready to perform getText operation");
    assertEquals("Do the contents match?", f1_contents, doc.getText(0,f1_contents.length()));
    _log.log("testOpenOne completed");
  }
  
  /** A nonexistent file.  Should open a new, untitled document. */
  public void testNE() {
    String[] list = new String[1];
    list[0] = nof1_name;
    DrJavaRoot.openCommandLineFiles(_mf, list, false);
//    _log.log("openCommandLineFiles completed");
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    assertEquals("Exactly one document?", 1, docs.size());
    OpenDefinitionsDocument doc = docs.get(0);
    assertTrue("Is document untitled?", doc.isUntitled());
    _log.log("testNE completed");
  }
  
  /** Many files on the command line.  Should open all of them, displaying the last one. */
  public void testOpenMany() throws BadLocationException {
    String[] list = new String[3];
    list[0] = f1_name;
    list[1] = f2_name;
    list[2] = f3_name;
    DrJavaRoot.openCommandLineFiles(_mf, list, false);
//    _log.log("openCommandLineFiles completed");
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    assertEquals("Exactly three documents?", 3, docs.size());
    OpenDefinitionsDocument doc1 = docs.get(0);
    assertEquals("Correct length of file 1?", f1_contents.length(), doc1.getLength());
    assertEquals("Do the contents of file 1 match?", f1_contents, doc1.getText(0,f1_contents.length()));
    
    OpenDefinitionsDocument doc2 = docs.get(1);
    assertEquals("Correct length of file 2?", f2_contents.length(), doc2.getLength());
    assertEquals("Do the contents of file 2 match?", f2_contents, doc2.getText(0,f2_contents.length()));
    
    OpenDefinitionsDocument doc3 = docs.get(2);
    assertEquals("Correct length of file 3?", f3_contents.length(), doc3.getLength());
    assertEquals("Do the contents of file 3 match?", f3_contents, doc3.getText(0,f3_contents.length()));
    
    assertEquals("Is the last document the active one?", doc3, _mf.getModel().getActiveDocument());
    _log.log("testOpenMany completed");
  }
  
  /** Supplying both valid and invalid filenames on the command line. Should open only the valid ones. */
  public void testMixed() throws BadLocationException {
    String[] list = new String[6];
    list[0] = f4_name;
    list[1] = nof1_name;
    list[2] = nof2_name;
    list[3] = f5_name;
    list[4] = f6_name;
    list[5] = nof3_name;
    DrJavaRoot.openCommandLineFiles(_mf, list, false);
//    _log.log("openCommandLineFiles completed");
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    assertEquals("Exactly three documents?", 3, docs.size());
    OpenDefinitionsDocument doc1 = docs.get(0);
    assertEquals("Correct length of file 1?", f4_contents.length(), doc1.getLength());
    assertEquals("Do the contents of file 1 match?", f4_contents, doc1.getText(0,f4_contents.length()));
    
    OpenDefinitionsDocument doc2 = docs.get(1);
    assertEquals("Correct length of file 2?", f5_contents.length(), doc2.getLength());
    assertEquals("Do the contents of file 2 match?", f5_contents, doc2.getText(0,f5_contents.length()));
    
    OpenDefinitionsDocument doc3 = docs.get(2);
    assertEquals("Correct length of file 3?", f6_contents.length(), doc3.getLength());
    assertEquals("Do the contents of file 3 match?", f6_contents, doc3.getText(0,f6_contents.length()));
    
    assertEquals("Is the last document the active one?", doc3, _mf.getModel().getActiveDocument());
    _log.log("testMixed completed");
  }
  
  /** Test duplicate files. */
  public void testDups() throws BadLocationException {
    String[] list = new String[6];
    list[0] = f7_name;
    list[1] = nof4_name;
    list[2] = nof5_name;
    list[3] = f8_name;
    list[4] = f8_name;
    list[5] = f7_name;
    DrJavaRoot.openCommandLineFiles(_mf, list, false);
//    _log.log("openCommandLineFiles in testDups completed");
    
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    Utilities.clearEventQueue();
    assertEquals("Exactly two documents?", 2, docs.size());
    OpenDefinitionsDocument doc1 = docs.get(0);
    assertEquals("Correct length of file 1?", f7_contents.length(), doc1.getLength());
    assertEquals("Do the contents of file 1 match?", f7_contents, doc1.getText(0,f7_contents.length()));
    Utilities.clearEventQueue();
    OpenDefinitionsDocument doc2 = docs.get(1);
    assertEquals("Correct length of file 2?", f8_contents.length(), doc2.getLength());
    assertEquals("Do the contents of file 2 match?", f8_contents, doc2.getText(0,f8_contents.length()));
    
    assertEquals("Is the last document the active one?", doc2, _mf.getModel().getActiveDocument());
//    _log.log("testDups completed");
  }
  
  /** A regression test for bug #542747, which related to opening a file via the command line using a relative path.
    * The problem was that getSourceRoot() would fail on the document, because the filename was not absolute. (The
    * fix will be to absolutize file paths when opening files.)
    */
  public void testRelativePath() throws IOException, InvalidPackageException {
    String funnyName = "DrScala_automatically_deletes_this_1";
    File newDirectory = mkTempDir(funnyName);
    File relativeFile = new File(newDirectory, "X.scala");
    
    assertEquals(relativeFile + " is absolute?", false, relativeFile.isAbsolute());
    
    try { checkFile(relativeFile, funnyName); }
    catch (Exception e) { fail("Exception thrown: " + StringOps.getStackTrace(e)); }
    finally { IOUtil.deleteOnExitRecursively(newDirectory); }
    _log.log("testRelativePath completed");
  }
  
  /** Tests paths with "." and ".." in them.  Windows will blow up if you use one in a JFileChooser without
    * converting it to a canonical filename.
    */
  public void testDotPaths() {
    String funnyName = "DrScala_automatically_deletes_this_2";
    File newDirectory = mkTempDir(funnyName);
    
    assertTrue("child directory created OK", new File(newDirectory, "childDir").mkdir());
    
    File relativeFile = new File(newDirectory, "." + FS + "X.scala");
    File relativeFile2 = new File(newDirectory, "." + FS + "Y.scala");
    File relativeFile3 = new File(newDirectory, "childDir" + FS + ".." + FS + "Z.scala");
    
    try {
      checkFile(relativeFile, funnyName);
      checkFile(relativeFile2, funnyName);
      checkFile(relativeFile3, funnyName);
    }
    catch (Exception e) { fail("Exception thrown: " + StringOps.getStackTrace(e)); }
    finally { IOUtil.deleteOnExitRecursively(newDirectory); }
    _log.log("testDotPaths completed");
  }
  
  /** Helper for testRelativeFile and testDotPaths. */
  private File mkTempDir(String funnyName) {
    // OK, we have to create a directory with a hard-coded name in the current working directory, so we'll make it
    // strange. If this directory happens to exist, it'll be deleted.
    File newDirectory = new File(funnyName);
    if (newDirectory.exists()) IOUtil.deleteOnExitRecursively(newDirectory);
//    System.err.println("newDirectory.exists() = " + newDirectory.exists());
    assertTrue("directory created OK", newDirectory.mkdir());
//    _log.log("Temporary directory " + funnyName + " created");
    return newDirectory;
  }
  
  /** Helper for testRelativeFile and testDotPaths. */
  private void checkFile(File relativeFile, String funnyName) throws IOException, InvalidPackageException {
    IOUtil.writeStringToFile(relativeFile, "package " + funnyName + "; class X { }");
    assertTrue("file exists", relativeFile.exists());
    
    String path = relativeFile.getCanonicalPath();
    DrJavaRoot.openCommandLineFiles(_mf, new String[] { path }, false);
    
    List<OpenDefinitionsDocument> docs = _mf.getModel().getOpenDefinitionsDocuments();
    assertEquals("Number of open documents", 1, docs.size());
    
    OpenDefinitionsDocument doc = docs.get(0);
    
    assertEquals("OpenDefDoc file is the right one and is canonical", relativeFile.getCanonicalFile(), doc.getFile());
    
    // The source root should be the current directory (as a canonical path, of course).
    Utilities.clearEventQueue();
    File root = doc.getSourceRoot();
    Utilities.clearEventQueue();
//    System.err.println("Source root is: " + root);
//    System.err.println("Package name is: " + doc.getPackageName());
    assertEquals("source root", IOUtil.WORKING_DIRECTORY.getCanonicalFile(), root);
    
    // Close this doc to clean up after ourselves for the next check.
    _mf.getModel().closeFile(doc);
  }
}
