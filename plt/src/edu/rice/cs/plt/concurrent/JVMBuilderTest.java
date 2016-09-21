/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.concurrent;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;

public class JVMBuilderTest extends TestCase {
  
  public void test() throws IOException {
    String currentCP = System.getProperty("java.class.path");
    Iterable<File> currentCPFiles = IOUtil.parsePath(currentCP);
    Process p = JVMBuilder.DEFAULT.start(TestProcess.class.getName(), "a", "b", "c");
    checkProcessOutput(p, currentCP, System.getProperty("user.dir"), IterUtil.make("a", "b", "c"));
    
    JVMBuilder customCP = new JVMBuilder(IterUtil.compose(currentCPFiles, new File("xx")));
    Process p2 = customCP.start(TestProcess.class.getName(), "d", "e");
    checkProcessOutput(p2, currentCP + File.pathSeparator + IOUtil.attemptAbsoluteFile(new File("xx")),
                       System.getProperty("user.dir"), IterUtil.make("d", "e"));
    
    JVMBuilder customDir = new JVMBuilder(File.listRoots()[0]);
    Process p3 = customDir.start(TestProcess.class.getName());
    checkProcessOutput(p3, currentCP, File.listRoots()[0].getPath(), IterUtil.<String>empty());
  }

  private void checkProcessOutput(Process p, String classPath, String workingDir, Iterable<String> args) 
    throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
    try {
      assertEquals("Test process", in.readLine());
      Iterable<File> expectedPath = IOUtil.parsePath(classPath);
      Iterable<File> actualPath = IOUtil.parsePath(in.readLine());
      assertTrue("Expected: " + expectedPath + "; actual: " + actualPath,
                 IterUtil.containsAll(actualPath, expectedPath));
      assertEquals(workingDir, in.readLine());
      assertEquals(IterUtil.toString(args), in.readLine());
    }
    finally { in.close(); }
  }
  
  private static final class TestProcess {
    public static void main(String... args) {
      System.out.println("Test process");
      System.out.println(System.getProperty("java.class.path"));
      System.out.println(IOUtil.attemptAbsoluteFile(new File(""))); // demonstrates working dir
      System.out.println(IterUtil.asIterable(args));
    }
  }
  

}
