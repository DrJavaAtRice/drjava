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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import junit.framework.*;
import java.io.*;

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
}
