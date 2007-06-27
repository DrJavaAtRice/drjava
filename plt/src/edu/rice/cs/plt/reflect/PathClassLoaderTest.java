/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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

package edu.rice.cs.plt.reflect;

import java.io.File;

public class PathClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = PreemptingClassLoaderTest.class.getClassLoader();
  private static final File ROOT = new File("testFiles/classLoading");
  private static final File INTBOX_DIR = new File(ROOT, "intbox");
  private static final File A_DIR = new File(ROOT, "a");
  private static final File B_DIR = new File(ROOT, "b");
  private static final File C_DIR = new File(ROOT, "c");
  private static final File D_DIR = new File(ROOT, "d");
  
  public void testLoadsPath() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR, A_DIR, B_DIR, C_DIR, D_DIR);
    assertLoadsClassAsLoader(l, "IntBox");
    assertLoadsClassAsLoader(l, "A");
    assertLoadsClassAsLoader(l, "B");
    assertLoadsClassAsLoader(l, "C");
    assertLoadsClassAsLoader(l, "D");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.PathClassLoaderTest");
    assertCanGet(l, "A", 1);
    assertCanGet(l, "D", 4);
  }
  
  public void testLoadsJumbledPath() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, D_DIR, INTBOX_DIR, B_DIR, C_DIR, A_DIR);
    assertLoadsClassAsLoader(l, "IntBox");
    assertLoadsClassAsLoader(l, "A");
    assertLoadsClassAsLoader(l, "B");
    assertLoadsClassAsLoader(l, "C");
    assertLoadsClassAsLoader(l, "D");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.reflect.PathClassLoaderTest");
    assertCanGet(l, "A", 1);
    assertCanGet(l, "D", 4);
  }
  
  public void testNestedLoaders() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR);
    PathClassLoader lA = new PathClassLoader(l, A_DIR);
    PathClassLoader lB = new PathClassLoader(lA, B_DIR);
    PathClassLoader lC = new PathClassLoader(lB, C_DIR);
    PathClassLoader lD = new PathClassLoader(lC, D_DIR);
    
    assertLoadsClassAsLoader(l, "IntBox");
    assertDoesNotLoadClass(l, "A");
    assertDoesNotLoadClass(l, "B");
    assertDoesNotLoadClass(l, "C");
    assertDoesNotLoadClass(l, "D");
    
    assertLoadsSameClass(l, lA, "IntBox");
    assertLoadsClassAsLoader(lA, "A");
    assertDoesNotLoadClass(lA, "B");
    assertDoesNotLoadClass(lA, "C");
    assertDoesNotLoadClass(lA, "D");
    
    assertLoadsSameClass(l, lB, "IntBox");
    assertLoadsSameClass(lA, lB, "A");
    assertLoadsClassAsLoader(lB, "B");
    assertDoesNotLoadClass(lB, "C");
    assertDoesNotLoadClass(lB, "D");
    
    assertLoadsSameClass(l, lC, "IntBox");
    assertLoadsSameClass(lA, lC, "A");
    assertLoadsSameClass(lB, lC, "B");
    assertLoadsClassAsLoader(lC, "C");
    assertDoesNotLoadClass(lC, "D");
    
    assertLoadsSameClass(l, lD, "IntBox");
    assertLoadsSameClass(lA, lD, "A");
    assertLoadsSameClass(lB, lD, "B");
    assertLoadsSameClass(lC, lD, "C");
    assertLoadsClassAsLoader(lD, "D");
    
    assertCanGet(lD, "A", 1);
    assertCanGet(lD, "D", 4);
  }
  
  public void testPoorlyNestedLoaders() throws Exception {
    PathClassLoader l = new PathClassLoader(BASE_LOADER, INTBOX_DIR);
    PathClassLoader lAB = new PathClassLoader(l, A_DIR, B_DIR);
    PathClassLoader lD = new PathClassLoader(lAB, D_DIR);
    PathClassLoader lC = new PathClassLoader(lD, C_DIR);
    
    assertCanGet(lC, "C", 3);
    assertCannotGet(lC, "D");
  }
  
  private void assertCanGet(ClassLoader l, String className, int value) throws Exception {
    Class<?> c = l.loadClass(className);
    Object instance = c.newInstance();
    int result = (Integer) c.getMethod("get").invoke(instance);
    assertEquals(value, result);
  }
  
  private void assertCannotGet(ClassLoader l, String className) {
    try {
      Class<?> c = l.loadClass(className);
      Object instance = c.newInstance();
      int result = (Integer) c.getMethod("get").invoke(instance);
    }
    catch (Exception e) { return; }
    fail("Able to invoke get() in class " + className);
  }
  
}
