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
 * Test cases for {@link ExecJVM}.
 *
 * @version $Id$
 */
public class ExecJVMTest extends TestCase {
  /**
   * Constructor.
   * @param  String name
   */
  public ExecJVMTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(ExecJVMTest.class);
  }

  public void testExecFileCreator() throws IOException, InterruptedException {
    File tempFile = File.createTempFile("drjava-test", ".tmp");
    assertTrue("temp file exists", tempFile.exists());
    boolean ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);

    // Run new JVM to create the file
    String className = getClass().getName() + "$FileCreator";
    String tempName = tempFile.getAbsolutePath();
    Process jvm = ExecJVM.runJVMPropogateClassPath(className,
                                                   new String[] { tempName });

    int result = jvm.waitFor();

    // Check jvm executed OK
    try {
      assertEquals("jvm exit code", 0, result);
      assertTrue("jvm did not create file", tempFile.exists());
      assertTrue("jvm System.out not empty", jvm.getInputStream().read() == -1);
      assertTrue("jvm System.err not empty", jvm.getErrorStream().read() == -1);
    }
    finally {
    }


    // clean up file
    ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);
  }

  public static final class FileCreator {
    public static void main(String[] args) {
      File file = new File(args[0]);
      boolean ret;

      try {
        ret = file.createNewFile();
      }
      catch (IOException ioe) {
        ret = false;
      }

      if (!ret) {
        throw new RuntimeException("file creation failed");
      }

      System.exit(0);
    }
  }
}
