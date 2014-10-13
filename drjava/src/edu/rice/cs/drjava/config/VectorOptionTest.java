/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.Vector;


/** Class according to the JUnit protocol.  Tests the proper functionality of the class VectorOption.
 *  @version $Id: VectorOptionTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class VectorOptionTest extends DrJavaTestCase {
  private VectorOption<String> _svo;
  private VectorOption<Integer> _ivo;
  private VectorOption<Boolean> _bvo;

  public void setUp() throws Exception {
    super.setUp();
    // name fields are irrelevant at this point.
    _svo = new VectorOption<String>("whatever", new StringOption("", null), (Vector<String>) null);
    _ivo = new VectorOption<Integer>("something", new IntegerOption("", null), (Vector<Integer>) null);
    _bvo = new VectorOption<Boolean>("everwhat", new BooleanOption("", null), (Vector<Boolean>) null);
  }

  public void testGetName() {
    assertEquals("whatever", _svo.getName());
    assertEquals("everwhat", _bvo.getName());
  }

  public void testParse() {
    assertTrue(_svo.parse("").isEmpty());
    assertTrue(_bvo.parse("").isEmpty());
    
    Vector<String> v = _svo.parse("[]");
    assertEquals(1, v.size());
    assertEquals("", v.get(0));
    
    v = _svo.parse("[x]");
    assertEquals(1, v.size());
    assertEquals("x", v.get(0));

    v = _svo.parse("[||]");
    assertEquals(1, v.size());
    assertEquals("|", v.get(0));
    
    v = _svo.parse("[|,]");
    assertEquals(1, v.size());
    assertEquals(",", v.get(0));
    
    v = _svo.parse("[|,,]");
    assertEquals(2, v.size());
    assertEquals(",", v.get(0));
    assertEquals("", v.get(1));

    v = _svo.parse("[,]");
    assertEquals(2, v.size());
    assertEquals("", v.get(0));
    assertEquals("", v.get(1));
    
    try { _svo.parse("[|x]"); fail("Pipe not in front of another pipe or delimiter."); } 
    catch (OptionParseException e) { }
    
    try { _svo.parse("[11"); fail("Missing footer."); } 
    catch (OptionParseException e) { }
    
    v = _svo.parse("[11,]");
    assertEquals(2, v.size());
    assertEquals("11", v.get(0));
    assertEquals("", v.get(1));
    
    try { _svo.parse("11]"); fail("Missing header."); } 
    catch (OptionParseException e) { }
    
    v = _svo.parse("[11,,22]");
    assertEquals(3, v.size());
    assertEquals("11", v.get(0));
    assertEquals("", v.get(1));
    assertEquals("22", v.get(2));
    
    v = _svo.parse("[11,|,,22]");
    assertEquals(3, v.size());
    assertEquals("11", v.get(0));
    assertEquals(",", v.get(1));
    assertEquals("22", v.get(2));
    
    v = _svo.parse("[11,abc|,def,22]");
    assertEquals(3, v.size());
    assertEquals("11", v.get(0));
    assertEquals("abc,def", v.get(1));
    assertEquals("22", v.get(2));

    v = _svo.parse("[11,||,22]");
    assertEquals(3, v.size());
    assertEquals("11", v.get(0));
    assertEquals("|", v.get(1));
    assertEquals("22", v.get(2));

    // parsing this as a vector of strings is okay, because it will treat it
    // as a singleton vector
    v = _svo.parse("{11,22}");
    assertEquals(1, v.size());
    assertEquals("{11,22}", v.get(0));    
    
    // but parsing this as a vector of integers will fail
    try { _ivo.parse("{11,22}"); fail("Should not have parsed this as singleton list."); } 
    catch (OptionParseException e) { }

    Vector<Boolean> bv = _bvo.parse("[true]");

    assertEquals(1, bv.size());
    assertEquals(Boolean.TRUE, bv.get(0));

    bv = _bvo.parse("[true,false,true,true]");

    assertEquals(4, bv.size());
    assertEquals(Boolean.TRUE,  bv.get(0));
    assertEquals(Boolean.FALSE, bv.get(1));
    assertEquals(Boolean.TRUE,  bv.get(2));
    assertEquals(Boolean.TRUE,  bv.get(3));

    try { _bvo.parse("[11]"); fail("Number instead of boolean."); } 
    catch (OptionParseException e) { }
    
    try { _bvo.parse("[true;false]"); fail("Illegal delimiter."); } 
    catch (OptionParseException e) { }
  }

  public void testFormat() {
    Vector<String> sv = new Vector<String>();
    assertEquals("", _svo.format(sv));

    sv.add("");
    assertEquals("[]", _svo.format(sv));

    sv.add("-33");
    assertEquals("[,-33]", _svo.format(sv));

    sv.add("2");
    assertEquals("[,-33,2]", _svo.format(sv));

    sv.add("");
    assertEquals("[,-33,2,]", _svo.format(sv));

    sv.add(",");
    assertEquals("[,-33,2,,|,]", _svo.format(sv));

    sv.add("0");
    assertEquals("[,-33,2,,|,,0]", _svo.format(sv));
  }
}
