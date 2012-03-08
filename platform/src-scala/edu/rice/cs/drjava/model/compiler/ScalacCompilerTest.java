package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.LinkedList;
import java.io.File;
import edu.rice.cs.plt.reflect.JavaVersion;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;

import junit.framework.TestCase;

public class ScalacCompilerTest extends TestCase {
  
  public void testCompileSuccess() {
    HjCompiler c = new HjCompiler(JavaVersion.CURRENT_FULL, "", null);
    assertTrue(c.isAvailable());
//    assertTrue(doCompile(c, "testFiles/?.scala").isEmpty());  // Can be used to ensure compiler version is correct
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
