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
END_COPYRIGHT_BLOCK*/package edu.rice.cs.drjava.config;

import junit.framework.*;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class BooleanOption.
 */
public class BooleanOptionTest extends TestCase
{
  /**
   * @param name The name of this test case.
   */
  public BooleanOptionTest(String name) { super(name); }
  
  public void setUp() {}
  
  public void testGetName()
  {
    BooleanOption bo1 = new BooleanOption("enable JUnit",null);
    BooleanOption bo2 = new BooleanOption("use menu icons",null);

    assertEquals("enable JUnit", bo1.getName());
    assertEquals("use menu icons",   bo2.getName());
  }
  
  public void testParse()
  {
    BooleanOption bo = new BooleanOption("enable JUnit",null);
    
    assertEquals(Boolean.TRUE, bo.parse("true"));
    assertEquals(Boolean.FALSE, bo.parse("false"));
    
    try { bo.parse("3"); fail(); }
    catch (IllegalArgumentException e) {}
    
    try { bo.parse("True"); fail(); }
    catch (IllegalArgumentException e) {}
  }
  
  public void testFormat()
  {
    BooleanOption bo1 = new BooleanOption("max_files",null);
    BooleanOption bo2 = new BooleanOption("indent_size",null);
    
    assertEquals("true",  bo1.format(Boolean.TRUE));
    assertEquals("true",  bo2.format(Boolean.TRUE));
    assertEquals("false", bo1.format(Boolean.FALSE));
    assertEquals("false", bo2.format(Boolean.FALSE));
  }
}
