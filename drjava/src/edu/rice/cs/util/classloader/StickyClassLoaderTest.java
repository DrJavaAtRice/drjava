/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.classloader;

import junit.framework.*;
import java.io.*;

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
