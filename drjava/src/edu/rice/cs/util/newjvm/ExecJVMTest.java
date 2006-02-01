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

package edu.rice.cs.util.newjvm;

import junit.framework.*;
import java.io.*;

/**
 * Test cases for {@link ExecJVM}.
 *
 * @version $Id$
 */
public class ExecJVMTest extends TestCase {
  public void testExecFileCreator() throws IOException, InterruptedException {
    File tempFile = File.createTempFile("drjava-test", ".tmp");
    assertTrue("temp file exists", tempFile.exists());
    boolean ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);

    // Run new JVM to create the file
    String className = getClass().getName() + "$FileCreator";
    String tempName = tempFile.getAbsolutePath();
    Process jvm = ExecJVM.runJVMPropagateClassPath(className,
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
