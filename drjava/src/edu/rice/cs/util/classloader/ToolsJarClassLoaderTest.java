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

/**
 * Test cases for {@link ToolsJarClassLoader}.
 *
 * @version $Id$
 */
public class ToolsJarClassLoaderTest extends TestCase {

  /**
   * Constructor.
   * @param String name
   */
  public ToolsJarClassLoaderTest(String name) {
    super(name);
  }

  /**
   * Test that ToolsJarClassLoader can correctly guess the default
   * SDK installation directory on Windows.
   * Precondition: JAVA_HOME contains "Program Files"
   */
  public void testWindowsSDKDirectory() throws Throwable {
    String javahome1 = "C:\\Program Files\\Java\\j2re1.4.0_01";
    String javahome2 = "C:\\Program Files\\Java\\j2re1.4.1";
    String javahome3 = "C:\\Program Files\\JavaSoft\\JRE\\1.3.1_04";
    
    assertEquals("new versions of Windows J2SDK (1)",
                 "C:\\j2sdk1.4.0_01\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome1));
                 
    assertEquals("new versions of Windows J2SDK (2)",
                 "C:\\j2sdk1.4.1\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome2));
    
    assertEquals("old versions of Windows J2SDK",
                 "C:\\jdk1.3.1_04\\lib\\tools.jar",
                 ToolsJarClassLoader.getWindowsToolsJar(javahome3));
  }

}
