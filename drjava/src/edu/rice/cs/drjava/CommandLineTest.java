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

package edu.rice.cs.drjava;

import junit.framework.*;
import java.io.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.text.BadLocationException;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.FileOps;

/**
 * Tests opening/creating files specified as command line arguments.
 * @version $Id$
 */
public class CommandLineTest extends TestCase {

  /**
   * The MainFrame we're working with.
   */
  private MainFrame _mf;
  
  /**
   * Files that exist, and the filenames that represent them.
   */
  private final File f1;
  private final String f1_name;
  private final String f1_contents;
  private final File f2;
  private final String f2_name;
  private final String f2_contents;
  private final File f3;
  private final String f3_name;
  private final String f3_contents;

  /**
   * Files that do not exist (constructor deletes them), and their filenames.
   */
  private final File nof1;
  private final File nof2;
  private final File nof3;
  private final String nof1_name;
  private final String nof2_name;
  private final String nof3_name;
  
  /**
   * Constructor.  Sets up test files for us to use:
   * - three files that exist and can be opened
   * - three files that don't exist
   * @param   String name
   */
  public CommandLineTest(String name) {
    super(name);
    try {
      f1 = File.createTempFile("DrJava-test", ".java");
      f1_name = f1.getAbsolutePath();
      f1_contents = "abcde";
      FileWriter fw1 = new FileWriter(f1);
      fw1.write(f1_contents,0,f1_contents.length());
      fw1.close();
      f2 = File.createTempFile("DrJava-test", ".java");
      f2_name = f2.getAbsolutePath();
      f2_contents = "fghijklm";
      FileWriter fw2 = new FileWriter(f2);
      fw2.write(f2_contents,0,f2_contents.length());
      fw2.close();
      f3 = File.createTempFile("DrJava-test", ".java");
      f3_name = f3.getAbsolutePath();
      f3_contents = "nopqrstuvwxyz";
      FileWriter fw3 = new FileWriter(f3);
      fw3.write(f3_contents,0,f3_contents.length());
      fw3.close();
      
      nof1 = File.createTempFile("DrJava-test", ".java");
      nof1_name = nof1.getAbsolutePath();
      nof1.delete();
      nof2 = File.createTempFile("DrJava-test", ".java");
      nof2_name = nof2.getAbsolutePath();
      nof2.delete();
      nof3 = File.createTempFile("DrJava-test", ".java");
      nof3_name = nof3.getAbsolutePath();
      nof3.delete();
    } catch (IOException e) {
      System.err.print("createTempFile failed.  This should not happen.");
      throw new RuntimeException(e.toString());
    }
  }

  public void setUp() {
    _mf = new MainFrame();
  }
  
  /**
   * Tests DrJava with no command line arguments.
   * Should open a new, untitled document.
   */
  public void testNone() {
    DrJava.openCommandLineFiles(_mf, new String[0]);
    // ListModel<DefinitionsDocument> docs = 
    // Wouldn't that be nice?
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Only one document?", 1, docs.getSize());
    OpenDefinitionsDocument doc = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertTrue("Is new document untitled?", doc.isUntitled());
  }

  /**
   * Open one file on the command line.  Should (obviously) open that file.
   */
  public void testOpenOne() throws BadLocationException {
    String[] list = new String[1];
    list[0] = f1_name;
    DrJava.openCommandLineFiles(_mf, list);
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Only one document opened?", 1, docs.getSize());
    OpenDefinitionsDocument doc = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertEquals("Correct length of file?", 
                 f1_contents.length(), 
                 doc.getDocument().getLength());
    assertEquals("Do the contents match?",
                 f1_contents, 
                 doc.getDocument().getText(0,f1_contents.length()));
  }

  /**
   * A nonexistent file.  Should open a new, untitled document.
   */
  public void testNE() {
    String[] list = new String[1];
    list[0] = nof1_name;
    DrJava.openCommandLineFiles(_mf, list);
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Exactly one document?", 1, docs.getSize());
    OpenDefinitionsDocument doc = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertTrue("Is document untitled?", doc.isUntitled());
  }

  /**
   * Many files on the command line.  Should open all of them, 
   * displaying the last one.
   */
  public void testOpenMany() throws BadLocationException {
    String[] list = new String[3];
    list[0] = f1_name;
    list[1] = f2_name;
    list[2] = f3_name;
    DrJava.openCommandLineFiles(_mf, list);
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Exactly three documents?", 3, docs.getSize());
    OpenDefinitionsDocument doc1 = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertEquals("Correct length of file 1?", 
                 f1_contents.length(), 
                 doc1.getDocument().getLength());
    assertEquals("Do the contents of file 1 match?",
                 f1_contents, 
                 doc1.getDocument().getText(0,f1_contents.length()));
    
    OpenDefinitionsDocument doc2 = (OpenDefinitionsDocument)docs.getElementAt(1);
    assertEquals("Correct length of file 2?", 
                 f2_contents.length(), 
                 doc2.getDocument().getLength());
    assertEquals("Do the contents of file 2 match?",
                 f2_contents, 
                 doc2.getDocument().getText(0,f2_contents.length()));
    
    OpenDefinitionsDocument doc3 = (OpenDefinitionsDocument)docs.getElementAt(2);
    assertEquals("Correct length of file 3?", 
                 f3_contents.length(), 
                 doc3.getDocument().getLength());
    assertEquals("Do the contents of file 3 match?",
                 f3_contents, 
                 doc3.getDocument().getText(0,f3_contents.length()));

    assertEquals("Is the last document the active one?",
                 doc3,
                 _mf.getModel().getActiveDocument());
  }

  /**
   * Supplying both valid and invalid filenames on the command line.
   * Should open only the valid ones.
   */
  public void testMixed() throws BadLocationException {
    String[] list = new String[6];
    list[0] = f2_name;
    list[1] = nof1_name;
    list[2] = nof2_name;
    list[3] = f3_name;
    list[4] = f1_name;
    list[5] = nof3_name;
    DrJava.openCommandLineFiles(_mf, list);
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Exactly three documents?", 3, docs.getSize());
    OpenDefinitionsDocument doc1 = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertEquals("Correct length of file 1?", 
                 f2_contents.length(), 
                 doc1.getDocument().getLength());
    assertEquals("Do the contents of file 1 match?",
                 f2_contents, 
                 doc1.getDocument().getText(0,f2_contents.length()));
    
    OpenDefinitionsDocument doc2 = (OpenDefinitionsDocument)docs.getElementAt(1);
    assertEquals("Correct length of file 2?", 
                 f3_contents.length(), 
                 doc2.getDocument().getLength());
    assertEquals("Do the contents of file 2 match?",
                 f3_contents, 
                 doc2.getDocument().getText(0,f3_contents.length()));
    
    OpenDefinitionsDocument doc3 = (OpenDefinitionsDocument)docs.getElementAt(2);
    assertEquals("Correct length of file 3?", 
                 f1_contents.length(), 
                 doc3.getDocument().getLength());
    assertEquals("Do the contents of file 3 match?",
                 f1_contents, 
                 doc3.getDocument().getText(0,f1_contents.length()));

    assertEquals("Is the last document the active one?",
                 doc3,
                 _mf.getModel().getActiveDocument());
    
  }
  
  /**
   * Test duplicate files.
   */
  public void testDups() throws BadLocationException {
    String[] list = new String[6];
    list[0] = f1_name;
    list[1] = nof1_name;
    list[2] = nof2_name;
    list[3] = f2_name;
    list[4] = f2_name;
    list[5] = f1_name;
    DrJava.openCommandLineFiles(_mf, list);
    ListModel docs = _mf.getModel().getDefinitionsDocuments();
    assertEquals("Exactly two documents?", 2, docs.getSize());
    OpenDefinitionsDocument doc1 = (OpenDefinitionsDocument)docs.getElementAt(0);
    assertEquals("Correct length of file 1?", 
                 f1_contents.length(), 
                 doc1.getDocument().getLength());
    assertEquals("Do the contents of file 1 match?",
                 f1_contents, 
                 doc1.getDocument().getText(0,f1_contents.length()));
    
    OpenDefinitionsDocument doc2 = (OpenDefinitionsDocument)docs.getElementAt(1);
    assertEquals("Correct length of file 2?", 
                 f2_contents.length(), 
                 doc2.getDocument().getLength());
    assertEquals("Do the contents of file 2 match?",
                 f2_contents, 
                 doc2.getDocument().getText(0,f2_contents.length()));
    
    assertEquals("Is the last document the active one?",
                 doc2,
                 _mf.getModel().getActiveDocument());
    
  }

  /**
   * A regression test for bug #542747, which related to opening a file
   * via the command line using a relative path.
   * The problem was that getSourceRoot() would fail on the document, because
   * the filename was not absolute. (The fix will be to absolutize file paths
   * when opening files.)
   */
  public void testRelativePath() throws IOException, InvalidPackageException {
    // OK, we have to create a directory with a hard-coded name, so we'll
    // make it strange. If this directory happens to exist, it'll be deleted.
    String funnyName = "DrJava_automatically_deletes_this";
    File newDirectory = new File(funnyName);
    if (newDirectory.exists()) {
      FileOps.deleteDirectory(newDirectory);
    }

    assertTrue("directory created OK", newDirectory.mkdir());

    File relativeFile = new File(newDirectory, "X.java");

    assertEquals(relativeFile + " is absolute?",
                 false,
                 relativeFile.isAbsolute());

    try {
      FileOps.writeStringToFile(relativeFile,
                                "package " + funnyName + "; class X {}");
      assertTrue("file exists", relativeFile.exists());

      String path = relativeFile.getPath();
      DrJava.openCommandLineFiles(_mf, new String[] { path });

      ListModel docs = _mf.getModel().getDefinitionsDocuments();
      assertEquals("Number of open documents", 1, docs.getSize());

      OpenDefinitionsDocument doc =
        (OpenDefinitionsDocument) docs.getElementAt(0);

      assertEquals("OpenDefDoc file is the right one and is absolute",
                   relativeFile.getAbsoluteFile(),
                   doc.getFile());

      // The source root should be the current directory (as 
      // an absolute path, of course).
      File root = doc.getSourceRoot();
      assertEquals("source root", new File("").getAbsoluteFile(), root);
    }
    finally {
      FileOps.deleteDirectory(newDirectory);
    }
  }
}
