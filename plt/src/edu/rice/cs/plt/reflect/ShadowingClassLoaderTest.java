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

package edu.rice.cs.plt.reflect;

import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class ShadowingClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = ShadowingClassLoaderTest.class.getClassLoader();
  
  public void testShadowedClassLoading() throws ClassNotFoundException {
    debug.logStart();
    
    // simple black list
    ShadowingClassLoader l = ShadowingClassLoader.blackList(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertDoesNotLoadClass(l, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    // prefix containing partial word shouldn't work
    ShadowingClassLoader l2 = ShadowingClassLoader.blackList(BASE_LOADER, "edu.rice.cs.plt.refl");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    // simple white list
    ShadowingClassLoader l3 = ShadowingClassLoader.whiteList(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.iter.IterUtil");
    assertDoesNotLoadClass(l3, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsSameClass(BASE_LOADER, l3, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    // default black list doesn't block bootstrap classes
    ShadowingClassLoader l4 = ShadowingClassLoader.blackList(BASE_LOADER, "javax", "edu");
    assertLoadsSameClass(BASE_LOADER, l4, "java.lang.Number");
    assertLoadsSameClass(BASE_LOADER, l4, "javax.swing.JFrame");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertDoesNotLoadClass(l4, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    // default white list doesn't block bootstrap classes
    ShadowingClassLoader l5 = ShadowingClassLoader.whiteList(BASE_LOADER, "javax", "edu.rice.cs.plt.reflect");
    assertLoadsSameClass(BASE_LOADER, l5, "javax.swing.JFrame");
    assertLoadsSameClass(BASE_LOADER, l5, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.iter.IterUtil");
    assertDoesNotLoadClass(l5, "edu.rice.cs.plt.iter.IterUtil");
    
    // can filter boostrap classes with filterBootClasses parameter
    ShadowingClassLoader l6 =
    new ShadowingClassLoader(BASE_LOADER, true, IterUtil.make("javax", "edu"), true);
    assertLoadsSameClass(BASE_LOADER, l6, "java.lang.Number");
    assertLoadsClass(BASE_LOADER, "javax.swing.JFrame");
    assertDoesNotLoadClass(l6, "javax.swing.JFrame");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertDoesNotLoadClass(l4, "edu.rice.cs.plt.reflect.ReflectUtil");
    
    debug.logEnd();
  }
  
  public void testResourceLoading() {
    debug.logStart();
    
    ShadowingClassLoader l = ShadowingClassLoader.blackList(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/iter/IterUtil.class");
    assertHasResource(BASE_LOADER, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
    assertDoesNotHaveResource(l, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
    
    debug.logEnd();
  }
  
}
