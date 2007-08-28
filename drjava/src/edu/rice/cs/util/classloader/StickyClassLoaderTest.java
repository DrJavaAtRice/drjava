/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.util.classloader;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.net.URL;
import java.security.SecureClassLoader;

/**
 * Test cases for {@link StickyClassLoader}.
 *
 * @version $Id$
 */
public class StickyClassLoaderTest extends DrJavaTestCase {
  private final String myName = getClass().getName();
  private final ClassLoader myLoader = getClass().getClassLoader();

  /**
   * Make sure getClass().getClassLoader() sticks, regardless of where
   * the class data came from.
   */
  public void testLoaderSticks() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader);

    Class c = loader.loadClass(myName);
    assertEquals("getClassLoader()", loader, c.getClassLoader());
    assertEquals("getName()", myName, c.getName());
  }

  /**
   * Make sure it works even for java.* classes.
   */
  public void testLoaderUsesSystemForJavaClasses() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader);

    Class c = loader.loadClass("java.lang.Object");
    assertEquals("java.lang.Object", c.getName());
  }

  /**
   * Tests that add-on Java packages, such as javax.mail.*, can be
   * loaded through the secondary loader if not found in the system
   * loader.
   */
  public void testLoaderFindsNonSystemJavaClasses() throws Throwable {
    class LoadingClassException extends RuntimeException { }

    ClassLoader testLoader = new SecureClassLoader() {
      public URL getResource(String name) {
        throw new LoadingClassException();
      }
    };
    StickyClassLoader loader = new StickyClassLoader(myLoader, testLoader);

    try {
      loader.loadClass("javax.mail.FakeClass");
      // Should not have actually found it...
      fail("FakeClass should not exist.");
    }
    catch (LoadingClassException lce) {
      // Good, that's what we want to happen
    }
    // If a ClassNotFoundException is thrown, then StickyClassLoader
    //  is not looking in the secondary loader.
  }

  /**
   * Make sure getClass().getClassLoader() does not stick if the class
   * was on the useOldLoader list.
   */
  public void testLoaderRespectsOldList() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader,
                                                     myLoader,
                                                     new String[] { myName });

    Class c = loader.loadClass(myName);
    assertEquals("getClassLoader()", myLoader, c.getClassLoader());
    assertEquals("getName()", myName, c.getName());
  }

  /**
   * Make sure that if we load A through sticky loader, and A requires B
   * to be loaded, B is also loaded through sticky loader.
   * We load the BMaker interface through the old loader so we can
   * cast to that interface.
   */
  public void testLoaderSticksTransitively() throws Throwable {
    String[] names = { myName + "$BMaker" };

    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader, names);
    Class c = loader.loadClass(myName + "$A");
    assertEquals("getClassLoader()", loader, c.getClassLoader());

    Object aObj = c.newInstance();
    BMaker aCasted = (BMaker) aObj;

    Object b = aCasted.makeB();

    assertEquals("getClass().getName()",
                 myName + "$A$B",
                 b.getClass().getName());

    assertEquals("getClass().getClassLoader()",
                 loader,
                 b.getClass().getClassLoader());
  }

  /**
   * Makes sure that a class that was loaded once before (implicitly) is not
   * loaded a second time. This test corresponds to bug #520519. As of
   * util-20020219-2255, this test case causes a LinkageError to be thrown, since
   * One is loaded twice. This problem was caused by the StickyClassLoader
   * not checking whether the class was already loaded before loading it!
   */
  public void testDoesntLoadSameClassTwice() throws Throwable {
    StickyClassLoader loader = new StickyClassLoader(myLoader, myLoader);
    loader.loadClass(myName + "$Two");
    loader.loadClass(myName + "$One");
  }

  public static class One { }
  public static class Two extends One { }

  public interface BMaker {
    public Object makeB();
  }

  public static class A implements BMaker {
    private static class B { }

    public Object makeB() { return new B(); }
  }
}
