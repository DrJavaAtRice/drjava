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

import gj.util.Vector;
import junit.framework.*;


/**
 * Class according to the JUnit protocol.
 * Tests the proper functionality of the class
 * IntegerVectorOption.
 */
public class IntegerVectorOptionTest extends TestCase
{
  private IntegerVectorOption _ivo1, _ivo2;
  
  /**
   * @param name The name of this test case.
   */
  public IntegerVectorOptionTest(String name) { super(name); }
  
  public void setUp() 
  {
    _ivo1 = new IntegerVectorOption("whatever");
    _ivo2 = new IntegerVectorOption("everwhat");
  }
  
  public void testGetName()
  {
    assertEquals("whatever", _ivo1.getName());
    assertEquals("everwhat", _ivo2.getName());
  }
  
  public void testParseElement()
  {
    Vector<Integer> iv = new Vector<Integer>();
   
    try 
    { 
      _ivo1.parseElement("[]", 1, iv);
      fail("parseElement() called on [].");
    }
    catch (IllegalArgumentException e) {}
    try 
    { 
      _ivo2.parseElement("[-32, 4]", 4, iv);
      fail("parseElement() called on incorrect String index.");
    }
    catch (IllegalArgumentException e) {}
    
    assertEquals(4, _ivo1.parseElement("[-33]", 1, iv));
    assertEquals(1, iv.size());
    assertEquals(new Integer(-33), iv.elementAt(0));
    iv.removeAllElements();
    assertEquals(6,  _ivo1.parseElement("[-33, 2, 0]", 1, iv));
    assertEquals(9,  _ivo1.parseElement("[-33, 2, 0]", 6, iv));
    assertEquals(10, _ivo1.parseElement("[-33, 2, 0]", 9, iv));
    assertEquals(new Integer(-33), iv.elementAt(0));
    assertEquals(new Integer(2), iv.elementAt(1));
    assertEquals(new Integer(0), iv.elementAt(2));
    assertEquals("[-33, 2, 0]", iv.toString());
  }
  
  public void testParse()
  {
    assertTrue(_ivo1.parse("[]").isEmpty());
    Vector<Integer> iv = _ivo1.parse("[-33, 2, 0]");

    assertEquals(3, iv.size());
    assertEquals(new Integer(-33), iv.elementAt(0));
    assertEquals(new Integer(2),   iv.elementAt(1));
    assertEquals(new Integer(0),   iv.elementAt(2));
    
    iv = _ivo2.parse(iv.toString());
    assertEquals(3, iv.size());
    assertEquals(new Integer(-33), iv.elementAt(0));
    assertEquals(new Integer(2),   iv.elementAt(1));
    assertEquals(new Integer(0),   iv.elementAt(2));
    
    try
    {
      _ivo2.parse("[-33,2,0]");
      fail("Illegally formatted input String.");
    }
    catch (IllegalArgumentException e) {}
    try
    {
      _ivo1.parse("{-33, 2, 0}");
      fail("Illegally formatted input String.");
    }
    catch (IllegalArgumentException e) {}    
    try
    {
      _ivo1.parse("[-33, 2, 0");
      fail("Illegally formatted input String.");
    }
    catch (IllegalArgumentException e) {}
    try
    {
      _ivo1.parse("[true, 2, 0]");
      fail("Illegally input String.");
    }
    catch (IllegalArgumentException e) {}
  }
  
  public void testFormat()
  {
    Vector<Integer> iv = new Vector<Integer>();
    assertEquals("[]", _ivo1.format(iv));
    
    iv.addElement(new Integer(-33));
    assertEquals("[-33]", _ivo1.format(iv));
    
    iv.addElement(new Integer(2));
    assertEquals("[-33, 2]", _ivo1.format(iv));
    
    iv.addElement(new Integer(0));
    assertEquals("[-33, 2, 0]", _ivo1.format(iv));
  }
}
