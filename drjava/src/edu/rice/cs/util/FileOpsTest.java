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

package edu.rice.cs.util;

import junit.framework.*;
import java.io.*;

import java.util.LinkedList;

/**
 * Test cases for {@link FileOps}.
 *
 * @version $Id$
 */
public class FileOpsTest extends TestCase {
  public static final String TEXT = "hi, dude.";
  public static final String PREFIX = "prefix";
  public static final String SUFFIX = ".suffix";

  /**
   * Constructor.
   * @param  String name
   */
  public FileOpsTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(FileOpsTest.class);
  }

  public void testCreateTempDirectory() throws IOException {
    File dir = FileOps.createTempDirectory(PREFIX);
    try {
      assertTrue("createTempDirectory result is a directory", dir.isDirectory());
      assertTrue("temp directory has correct prefix",
                 dir.getName().startsWith(PREFIX));
    }
    finally {
      assertTrue("delete directory", dir.delete());
    }
  }

  public void testReadAndWriteTempFile() throws IOException {
    File file = FileOps.writeStringToNewTempFile(PREFIX, SUFFIX, TEXT);
    try {
      assertTrue("temp file has correct prefix",
                 file.getName().startsWith(PREFIX));
      assertTrue("temp file has correct suffix",
                 file.getName().endsWith(SUFFIX));

      String read = FileOps.readFileAsString(file);
      assertEquals("contents after read", TEXT, read);
    }
    finally {
      assertTrue("delete file", file.delete());
    }
  }

  public void testRecursiveDirectoryDelete() throws IOException {
    final File baseDir = FileOps.createTempDirectory(PREFIX);

    File parentDir = baseDir;
    boolean ret;

    // create a bunch of subdirs and some files.
    for (int i = 0; i < 5; i++) {
      File subdir = new File(parentDir, "subdir" + i);
      ret = subdir.mkdir();
      assertTrue("create directory " + subdir, ret);

      for (int j = 0; j < 2; j++) {
        File file = new File(parentDir, "file" + i + "-" + j);
        FileOps.writeStringToFile(file,
                                  "Some text for file "+file.getAbsolutePath());
        assertTrue(file + " exists", file.exists());
      }

      parentDir = subdir;
    }

    // OK, now try to delete base.
    ret = FileOps.deleteDirectory(baseDir);
    assertTrue("delete directory result", ret);
    assertEquals("directory exists after deleting it", false, baseDir.exists());
  }

  
  /**
   * This method checks that backups are made correctly, and that when a save fails,
   * no data is lost
   */
  public void testSaveFile() throws IOException {
    File writeTo = File.createTempFile("fileops", ".test");
    File backup = new File(writeTo.getPath() + "~");
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output = "version 1";
        os.write(output.getBytes());
      }
      public boolean shouldBackup() {
        return false;
      }
    });
    assertEquals("save w/o backup", "version 1", FileOps.readFileAsString(writeTo));
    assertEquals("save w/o backup did not backup", false, backup.exists());
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output = "version 2";
        os.write(output.getBytes());
      }
    });
    assertEquals("save2 w backup", "version 2", FileOps.readFileAsString(writeTo));
    assertEquals("save2 w backup did backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
      public void saveTo(OutputStream os) throws IOException {
        String output =  "version 3";
        os.write(output.getBytes());
      }
    });
    assertEquals("save3 w backup on", "version 3", FileOps.readFileAsString(writeTo));
    assertEquals("save3 w backup on did not backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    
    /* Now see what happens when saving fails and we were not making a backup
     * Nothing should change. */
    try {
      FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
        public void saveTo(OutputStream os) throws IOException {
          String output = "version 4";
          os.write(output.getBytes());
          throw new IOException();
        }
      });
      fail("IOException not propagated");
    } catch (IOException ioe){}//do nothing, this is expected
    assertEquals("failed save4 w/o backup", "version 3",
                 FileOps.readFileAsString(writeTo));
    assertEquals("failed save4 w/o backup check original backup", "version 1",
                 FileOps.readFileAsString(backup));
    
    /* Now see what happens when saving fails and we were making a backup */
    try {
      FileOps.saveFile(new FileOps.DefaultFileSaver(writeTo) {
        public boolean shouldBackup () {
          return true;
        }
        public void saveTo(OutputStream os) throws IOException {
          String output =  "version 5";
          os.write(output.getBytes());
          throw new IOException();
        }
      });
      fail("IOException not propagated spot 2");
    } catch(IOException ioe){} //do nothing, we expected this
    assertEquals("failed save5 w backup", "version 3",
                 FileOps.readFileAsString(writeTo));
  }

  /**
   * This tests that packageExplore correctly runs through and returns
   * non-empty packages
   */
  public void testPackageExplore() throws IOException {
    File rootDir = FileOps.createTempDirectory("fileOpsTest");
    File subDir0 = new File(rootDir, "sub0");
    subDir0.mkdir();
    File subDir1 = new File(rootDir, "sub1");
    subDir1.mkdir();
    File subsubDir0 = new File(subDir0, "subsub0");
    subsubDir0.mkdir();
    File javasubsub = new File(subsubDir0, "aclass.java");
    FileOps.writeStringToFile(javasubsub, "contents of this file are unimportant");
    File javasub1 = new File(subDir1, "myclass.java");
    FileOps.writeStringToFile(javasub1, "this file is pretty much empty");
    File javaroot = new File(rootDir, "someclass.java");
    FileOps.writeStringToFile(javaroot, "i can write anything i want here");

    LinkedList packages = FileOps.packageExplore("hello", rootDir);
    assertEquals("package count a", 3, packages.size());
    assertTrue("packages contents a0", packages.contains("hello.sub0.subsub0"));
    assertTrue("packages contents a1", packages.contains("hello.sub1"));
    assertTrue("packages contents a2", packages.contains("hello"));

    //Now add a .java file to the root directory and check that the default directory
    //is not added
    packages = FileOps.packageExplore("", rootDir);
    assertEquals("package count b", 2, packages.size());
    assertTrue("packages contents b0", packages.contains("sub0.subsub0"));
    assertTrue("packages contents b1", packages.contains("sub1"));


    assertTrue("deleting temp directory", FileOps.deleteDirectory(rootDir));
  }


}
