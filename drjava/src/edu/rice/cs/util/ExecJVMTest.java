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
