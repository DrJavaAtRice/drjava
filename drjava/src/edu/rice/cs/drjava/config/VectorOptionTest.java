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
 * VectorOption.
 */
public class VectorOptionTest extends TestCase {
  private VectorOption<Integer> _ivo;
  private VectorOption<Boolean> _bvo;
  
  /**
   * @param name The name of this test case.
   */
  public VectorOptionTest(String name) { super(name); }
  
  public void setUp() {
    // name fields are irrelevant at this point.
    _ivo = new VectorOption<Integer>("whatever",new IntegerOption("",null),(Vector) null);
    _bvo = new VectorOption<Boolean>("everwhat",new BooleanOption("",null),(Vector) null);
  }
  
  public void testGetName() {
    assertEquals("whatever", _ivo.getName());
    assertEquals("everwhat", _bvo.getName());
  }
  
  public void testParse() {
    Vector<Integer> iv = new Vector<Integer>();
    
    assertTrue(_ivo.parse("[]").isEmpty());
    assertTrue(_bvo.parse("[]").isEmpty());
    
    try {
      _ivo.parse("[,]"); fail("Comma at beginning.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("[11"); fail("Missing footer.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("[11,]"); fail("Comma w/o following list element.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("11]"); fail("Missing header.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("[11,,22]"); fail("Missing list element.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("{11,22}"); fail("Illegal header and footer.");
    } catch (OptionParseException e) {
    }
    try {
      _ivo.parse("[11;22]"); fail("Illegal delimiter.");
    } catch (OptionParseException e) {
    }
    
    Vector<Boolean> bv = _bvo.parse("[true]");
    
    assertEquals(1, bv.size()); 
    assertEquals(Boolean.TRUE, bv.elementAt(0));
    
    bv = _bvo.parse("[true,false,true,true]");
    
    assertEquals(4, bv.size()); 
    assertEquals(Boolean.TRUE,  bv.elementAt(0)); 
    assertEquals(Boolean.FALSE, bv.elementAt(1));
    assertEquals(Boolean.TRUE,  bv.elementAt(2));
    assertEquals(Boolean.TRUE,  bv.elementAt(3));
    
    try {
      _bvo.parse("[11]"); fail("Number instead of boolean.");
    } catch (OptionParseException e) {
    }
  }
  
  public void testFormat() {
    Vector<Integer> iv = new Vector<Integer>();
    assertEquals("[]", _ivo.format(iv));
    
    iv.addElement(new Integer(-33));
    assertEquals("[-33]", _ivo.format(iv));
    
    iv.addElement(new Integer(2));
    assertEquals("[-33,2]", _ivo.format(iv));
    
    iv.addElement(new Integer(0));
    assertEquals("[-33,2,0]", _ivo.format(iv));
  }
}
