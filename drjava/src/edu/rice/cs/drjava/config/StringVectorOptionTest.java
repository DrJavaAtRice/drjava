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
 * StringVectorOption.
 */
public class StringVectorOptionTest extends TestCase
{
  private StringVectorOption _svo1, _svo2;
  
  /**
   * @param name The name of this test case.
   */
  public StringVectorOptionTest(String name) { super(name); }
  
  public void setUp() 
  {
    _svo1 = new StringVectorOption("whatever");
    _svo2 = new StringVectorOption("everwhat");
  }
  
  public void testGetName()
  {
    assertEquals("whatever", _svo1.getName());
    assertEquals("everwhat", _svo2.getName());
  }
  
  public void testParseElement()
  {
    Vector<String> iv = new Vector<String>();
   
    assertEquals(1, _svo1.parseElement("[]", 1, iv));
    assertEquals(1, iv.size());
    assertEquals("", iv.elementAt(0));
 
    iv.removeAllElements();
    
    assertEquals(4, _svo1.parseElement("[-33]", 1, iv));
    assertEquals(1, iv.size());
    assertEquals("-33", iv.elementAt(0));
    iv.removeAllElements();
    assertEquals(6,  _svo1.parseElement("[-33,  , grrgl]", 1, iv));
    assertEquals(9,  _svo1.parseElement("[-33,  , grrgl]", 6, iv));
    assertEquals(14, _svo1.parseElement("[-33,  , grrgl]", 9, iv));
    assertEquals("-33",   iv.elementAt(0));
    assertEquals(" ",     iv.elementAt(1));
    assertEquals("grrgl", iv.elementAt(2));
    assertEquals("[-33,  , grrgl]", iv.toString());
  }
  
  public void testParse()
  {
    assertTrue(_svo1.parse("[]").isEmpty());
    Vector<String> iv = _svo1.parse("[this, is, RIDICULOUS, 322, true]");

    assertEquals(5, iv.size());
    assertEquals("this",       iv.elementAt(0));
    assertEquals("is",         iv.elementAt(1));
    assertEquals("RIDICULOUS", iv.elementAt(2));
    assertEquals("322",        iv.elementAt(3));
    assertEquals("true",       iv.elementAt(4));
    
    iv = _svo2.parse(iv.toString());
    assertEquals(5, iv.size());
    assertEquals("this",       iv.elementAt(0));
    assertEquals("is",         iv.elementAt(1));
    assertEquals("RIDICULOUS", iv.elementAt(2));
    assertEquals("322",        iv.elementAt(3));
    assertEquals("true",       iv.elementAt(4));
      
    iv = _svo2.parse("[too,tight,commas]");
    assertEquals(1, iv.size());
    assertEquals("too,tight,commas", iv.elementAt(0));
    
    try
    {
      _svo1.parse("{numbers, are, OK, 33}");
      fail("Illegally formatted input String.");
    }
    catch (IllegalArgumentException e) {}    
    try
    {
      _svo1.parse("[this, is, REDICULOUS");
      fail("Illegally formatted input String.");
    }
    catch (IllegalArgumentException e) {}
  }
  
  public void testFormat()
  {
    Vector<String> iv = new Vector<String>();
    assertEquals("[]", _svo1.format(iv));
    
    iv.addElement("this");
    assertEquals("[this]", _svo1.format(iv));
    
    iv.addElement("is");
    assertEquals("[this, is]", _svo1.format(iv));
    
    iv.addElement("REDICULOUS");
    assertEquals("[this, is, REDICULOUS]", _svo1.format(iv));
  }
}
