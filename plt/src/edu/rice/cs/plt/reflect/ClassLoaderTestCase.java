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

import java.net.URL;

import junit.framework.TestCase;

/** Helpful assert methods for testing class loaders */
public abstract class ClassLoaderTestCase extends TestCase {
  
  public static void assertLoadsClass(ClassLoader l, String name) {
    try { l.loadClass(name); }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertLoadsClassAsLoader(ClassLoader l, String name) {
    try {
      Class<?> c = l.loadClass(name);
      assertSame(l, c.getClassLoader());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertDoesNotLoadClass(ClassLoader l, String name) {
    try { l.loadClass(name); }
    catch (ClassNotFoundException e) { return; }
    fail("Found class " + name);
  }
  
  public static void assertLoadsSameClass(ClassLoader l1, ClassLoader l2, String name) {
    try {
      Class<?> c1 = l1.loadClass(name);
      Class<?> c2 = l2.loadClass(name);
      assertSame(c1, c2);
      assertEquals(name, c1.getName());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertLoadsDifferentClass(ClassLoader l1, ClassLoader l2, String name) {
    try {
      Class<?> c1 = l1.loadClass(name);
      Class<?> c2 = l2.loadClass(name);
      assertNotSame(c1, c2);
      assertEquals(name, c1.getName());
      assertEquals(name, c2.getName());
      assertNotSame(c1.getClassLoader(), c2.getClassLoader());
    }
    catch (ClassNotFoundException e) { fail("Can't find class " + name); }
  }
  
  public static void assertHasResource(ClassLoader l, String name) {
    assertNotNull(l.getResource(name));
  }
  
  public static void assertDoesNotHaveResource(ClassLoader l, String name) {
    assertNull(l.getResource(name));
  }
  
  public static void assertHasSameResource(ClassLoader l1, ClassLoader l2, String name) {
    URL r1 = l1.getResource(name);
    URL r2 = l2.getResource(name);
    assertNotNull(r1);
    assertNotNull(r2);
    assertEquals(r1, r2);
  }
  
  public static void assertHasDifferentResource(ClassLoader l1, ClassLoader l2, String name) {
    URL r1 = l1.getResource(name);
    URL r2 = l2.getResource(name);
    assertNotNull(r1);
    assertNotNull(r2);
    assertFalse(r1.equals(r2));
  }
  
}
