/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.Vector;


/** Class according to the JUnit protocol.  Tests the proper functionality of the class VectorOption.
 *  @version $Id$
 */
public final class VectorOptionTest extends DrJavaTestCase {
  private VectorOption<Integer> _ivo;
  private VectorOption<Boolean> _bvo;

  public void setUp() throws Exception {
    super.setUp();
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

    try { _ivo.parse("[,]"); fail("Comma at beginning."); } 
    catch (OptionParseException e) { }
    
    try { _ivo.parse("[11"); fail("Missing footer."); } 
    catch (OptionParseException e) { }
    try { _ivo.parse("[11,]"); fail("Comma w/o following list element."); } 
    catch (OptionParseException e) { }
    
    try { _ivo.parse("11]"); fail("Missing header."); } 
    catch (OptionParseException e) { }
    
    try { _ivo.parse("[11,,22]"); fail("Missing list element."); } 
    catch (OptionParseException e) { }
    
    try { _ivo.parse("{11,22}"); fail("Illegal header and footer."); } 
    catch (OptionParseException e) { }
    
    try { _ivo.parse("[11;22]"); fail("Illegal delimiter."); } 
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
