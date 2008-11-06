package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import java.util.Collections;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.plt.reflect.JavaVersion;
import junit.framework.TestCase;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class Javac150CompilerTest extends TestCase {

  public void testCompile() {
    Javac150Compiler c = new Javac150Compiler(JavaVersion.CURRENT_FULL, "", null);
    assertTrue(c.isAvailable());
    assertCompileSuccess(c, "testFiles/IterableTest.java");
  }
  
  private static void assertCompileSuccess(CompilerInterface c, String... files) {
    List<? extends DJError> errors = doCompile(c, files);
    debug.logValue("errors", errors);
    assertEquals(Collections.EMPTY_LIST, errors);
  }
  
  private static List<? extends DJError> doCompile(CompilerInterface c, String... files) {
    return c.compile(fileList(files), null, null, null, null, null, true);
  }
      
  private static List<File> fileList(String... files) {
    List<File> result = new LinkedList<File>();
    for (String s : files) { result.add(new File(s)); }
    return result;
  }
  
}
