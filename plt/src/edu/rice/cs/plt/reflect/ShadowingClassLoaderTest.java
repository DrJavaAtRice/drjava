/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

public class ShadowingClassLoaderTest extends ClassLoaderTestCase {
  
  private static final ClassLoader BASE_LOADER = ShadowingClassLoaderTest.class.getClassLoader();
  
  public void testShadowedClassLoading() throws ClassNotFoundException {
    ShadowingClassLoader l = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertLoadsSameClass(BASE_LOADER, l, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsClass(BASE_LOADER, "edu.rice.cs.plt.reflect.ReflectUtil");
    assertDoesNotLoadClass(l, "edu.rice.cs.plt.reflect.ReflectUtil");

    ShadowingClassLoader l2 = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.refl");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.iter.IterUtil");
    assertLoadsSameClass(BASE_LOADER, l2, "edu.rice.cs.plt.reflect.ReflectUtil");
  }
  
  public void testResourceLoading() {
    ShadowingClassLoader l = new ShadowingClassLoader(BASE_LOADER, "edu.rice.cs.plt.reflect");
    assertHasSameResource(BASE_LOADER, l, "edu/rice/cs/plt/iter/IterUtil.class");
    assertHasResource(BASE_LOADER, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
    assertDoesNotHaveResource(l, "edu/rice/cs/plt/reflect/ShadowingClassLoaderTest.class");
  }
  
}
