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

package edu.rice.cs.drjava.config;

import java.util.Vector;
import junit.framework.*;


/**
 * Class according to the JUnit protocol.
 * Tests the proper functionality of the class
 * VectorOption.
 * @version $Id$
 */
public final class VectorOptionTest extends TestCase {
  private VectorOption<Integer> _ivo;
  private VectorOption<Boolean> _bvo;

  public void setUp() {
    // name fields are irrelevant at this point.
    _ivo = new VectorOption<Integer>("whatever", new IntegerOption("", null), (Vector<Integer>) null);
    _bvo = new VectorOption<Boolean>("everwhat", new BooleanOption("", null), (Vector<Boolean>) null);
  }

  public void testGetName() {
    assertEquals("whatever", _ivo.getName());
    assertEquals("everwhat", _bvo.getName());
  }

  public void testParse() {
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
    assertEquals(Boolean.TRUE, bv.get(0));

    bv = _bvo.parse("[true,false,true,true]");

    assertEquals(4, bv.size());
    assertEquals(Boolean.TRUE,  bv.get(0));
    assertEquals(Boolean.FALSE, bv.get(1));
    assertEquals(Boolean.TRUE,  bv.get(2));
    assertEquals(Boolean.TRUE,  bv.get(3));

    try {
      _bvo.parse("[11]"); fail("Number instead of boolean.");
    } catch (OptionParseException e) {
    }
  }

  public void testFormat() {
    Vector<Integer> iv = new Vector<Integer>();
    assertEquals("[]", _ivo.format(iv));

    iv.add(new Integer(-33));
    assertEquals("[-33]", _ivo.format(iv));

    iv.add(new Integer(2));
    assertEquals("[-33,2]", _ivo.format(iv));

    iv.add(new Integer(0));
    assertEquals("[-33,2,0]", _ivo.format(iv));
  }
}
