package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import edu.rice.cs.plt.reflect.JavaVersion;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;

import junit.framework.TestCase;

public class NextGenCompilerTest extends TestCase {
  
  public void testCompileSuccess() {
    NextGenCompiler c = new NextGenCompiler(JavaVersion.CURRENT_FULL, "", null);
    assertTrue(c.isAvailable());
    assertTrue(doCompile(c, "testFiles/IterableTest.java").isEmpty());
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
