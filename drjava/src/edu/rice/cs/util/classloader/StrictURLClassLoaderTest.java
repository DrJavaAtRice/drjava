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
import java.net.*;

/**
 * Test cases for {@link StrictURLClassLoader}.
 *
 * @version $Id$
 */
public class StrictURLClassLoaderTest extends TestCase {

  /**
   * Constructor.
   * @param String name
   */
  public StrictURLClassLoaderTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(StrictURLClassLoaderTest.class);
  }

  /**
   * Make sure this loader doesn't load classes from the system
   * classloader.
   */
  public void testWontLoadFromSystem() throws Throwable {
    StrictURLClassLoader loader = new StrictURLClassLoader(new URL[0]);
    String myName = getClass().getName();

    try {
      Class c = loader.loadClass(myName);
      fail("should not have loaded class");
    }
    catch (ClassNotFoundException e) {
      // yep, we expected it to fail
    }
  }
  
  /**
   * Make sure this loader doesn't load resources from the bootclasspath.
   */
  public void testWontLoadResourceFromBootClassPath() throws Throwable {
    StrictURLClassLoader loader = new StrictURLClassLoader(new URL[0]);
    String compiler = "com/sun/tools/javac/v8/JavaCompiler.class";
    
    URL resource = loader.getResource(compiler);
    assertTrue("should not have found resource", resource == null);
  }

  /**
   * Make sure this loader can load from the given URLs.
   */
  public void testWillLoadClassFromGivenURLs() throws Throwable {
    String logResource = "com/sun/tools/javac/v8/util/Log.class";
    String compilerClass = "com.sun.tools.javac.v8.JavaCompiler";
    URL[] urls = ToolsJarClassLoader.getToolsJarURLs();
    
    if (urls.length > 0) {
      //System.out.println("testing urls");
      StrictURLClassLoader loader = new StrictURLClassLoader(urls);
      
      URL resource = loader.getResource(logResource);
      assertTrue("resource found", resource != null);
      
      Class c = loader.loadClass(compilerClass);
      assertEquals("loaded class", compilerClass, c.getName());
    }
  }
}
