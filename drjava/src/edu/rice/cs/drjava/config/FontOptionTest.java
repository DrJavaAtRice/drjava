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

package edu.rice.cs.drjava.config;

import junit.framework.*;
import java.awt.Font;
import edu.rice.cs.drjava.CodeStatus;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class FontOption.
 * @version $Id$
 */
public class FontOptionTest extends TestCase
{
  /**
   * @param name The name of this test case.
   */
  public FontOptionTest(String name) { super(name); }
  
  public void setUp() {}
  
  public void testParse()
  {
    if (CodeStatus.DEVELOPMENT) {
      FontOption fo = new FontOption("font.test1", Font.decode(null));
      
      assertEquals(new Font("monospaced", 0, 12), fo.parse("monospaced-PLAIN-12"));
      assertEquals(new Font("sansserif", 1, 10), fo.parse("sansserif-BOLD-10"));
      assertEquals(new Font("sansserif", 3, 10), fo.parse("sansserif-BOLDITALIC-10"));
      
      // Any failed parse attempts return the default "dialog" font
      assertEquals("dialog", fo.parse("true").getFamily());
    }

  }
  
  public void testFormat()
  {
    if (CodeStatus.DEVELOPMENT) {
      FontOption fO1 = new FontOption("font.test2", Font.decode(null));
      
      assertEquals("monospaced-PLAIN-12",  fO1.format(new Font("monospaced", 0, 12)));
      assertEquals("sansserif-BOLD-10", fO1.format(new Font("sansserif", 1, 10)));
      assertEquals("sansserif-BOLDITALIC-10", fO1.format(new Font("sansserif", 3, 10)));
    }
  }
}