/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.classloader;

import junit.framework.*;
import java.io.*;
import java.net.URL;
import java.security.SecureClassLoader;

/**
 * Test cases for {@link StickyClassLoader}.
 *
 * @version $Id$
 */
public class StickyClassLoaderTest extends TestCase {
  private final String myName = getClass().getName();
  private final ClassLoader myLoader = getClass().getClassLoader();

  /**
   * Constructor.
   * @param String name
   */
  public StickyClassLoaderTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(StickyClassLoaderTest.class);
  }

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
    class LoadingClassException extends RuntimeException {}
    
    ClassLoader testLoader = new SecureClassLoader() {
      public URL getResource(String name) {
        throw new LoadingClassException();
      }
    };
    StickyClassLoader loader = new StickyClassLoader(myLoader, testLoader);
    
    try {
      Class c = loader.loadClass("javax.mail.FakeClass");
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
    Class two = loader.loadClass(myName + "$Two");
    Class one = loader.loadClass(myName + "$One");
  }

  public static class One {}
  public static class Two extends One {}

  public interface BMaker {
    public Object makeB();
  }

  public static class A implements BMaker {
    private static class B {}

    public Object makeB() { return new B(); }
  }
}
