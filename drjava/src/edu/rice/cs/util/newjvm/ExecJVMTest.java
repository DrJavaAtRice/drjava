/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.util.StringOps;

import java.io.File;
import java.io.IOException;

/**
 * Test cases for {@link ExecJVM}.
 *
 * @version $Id$
 */
public class ExecJVMTest extends DrJavaTestCase {
  /**
   * Setup for every test case.
   */
  public void setUp() throws Exception {
    super.setUp();
//    edu.rice.cs.util.swing.Utilities.TEST_MODE = true;  // already done in super call!
  }

  public void testExecFileCreator() throws IOException, InterruptedException {
    File tempFile = File.createTempFile("drjava-test", ".tmp").getCanonicalFile();
    assertTrue("temp file exists", tempFile.exists());
    boolean ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);

    // Run new JVM to create the file
    String className = getClass().getName() + "$FileCreator";
    String tempName = tempFile.getAbsolutePath();
    Process jvm = ExecJVM.runJVMPropagateClassPath(className, new String[] { tempName }, FileOption.NULL_FILE);

    int result = jvm.waitFor();

    // Check jvm executed OK
    try {
      assertEquals("jvm exit code", 0, result);
      assertTrue("jvm did not create file", tempFile.exists());
      assertTrue("jvm System.out not empty", jvm.getInputStream().read() == -1);
      assertTrue("jvm System.err not empty", jvm.getErrorStream().read() == -1);
    }
    finally { /*do nothing */ }

    // clean up file
    ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);
  }

  public static final class FileCreator {
    public static void main(String[] args) {
      File file = new File(args[0]);
      boolean ret;
      try { ret = file.createNewFile(); }
      catch (IOException ioe) { ret = false; }
      if (!ret) throw new RuntimeException("file creation failed");
      System.exit(0);
    }
  }

  public void testExecWorkingDirNotFound() throws IOException, InterruptedException {
    // create and delete temp file
    File tempFile = File.createTempFile("drjava-test", ".tmp").getCanonicalFile();
    assertTrue("temp file exists", tempFile.exists());
    boolean ret = tempFile.delete();
    assertTrue("temp file delete succeeded", ret);

    // turn temp file into directory name, make directory, and delete again
    File tempDir = new File(tempFile.toString() + File.separatorChar);
    ret = tempDir.mkdirs();
    assertTrue("temp dir exists", tempDir.exists());
    assertTrue("temp dir is dir", tempDir.isDirectory());
    ret = tempDir.delete();
    assertTrue("temp dir delete succeeded", ret);

    // Run new JVM to create the file
    String className = getClass().getName() + "$" + StringOps.getSimpleName(NoOp.class);
    String tempName = tempFile.getAbsolutePath();
    Process jvm = ExecJVM.runJVMPropagateClassPath(className, new String[] { tempName }, tempDir);

    int result = jvm.waitFor();

    // Check jvm executed OK
    try {
      assertEquals("jvm exit code", 0, result);
      assertTrue("jvm System.out not empty", jvm.getInputStream().read() == -1);
      assertTrue("jvm System.err not empty", jvm.getErrorStream().read() == -1);
    }
    finally {
    }
  }

  public static final class NoOp {
    public static void main(String[] args) {
    }
  }
}
