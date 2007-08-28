/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package koala.dynamicjava.interpreter.context;

import junit.framework.TestCase;

/**
 * Tests the functionality of the VariableContext.
 */
public class VariableContextTest extends TestCase {
  VariableContext _ctx;
  
  public void setUp() { 
    _ctx = new VariableContext();
  }
  
  /**
   * Tests to make sure that the correct bindings are truely
   * removed when revert is called
   */
  public void testRevert() {
    _ctx.define("a", new Integer(1));
    _ctx.define("b", new Integer(2));
    _ctx.defineConstant("c", new Integer(3));
    _ctx.setRevertPoint();
    _ctx.define("d", new Integer(4));
    _ctx.define("e", new Integer(5));
    _ctx.defineConstant("f", new Integer(6));
    
    assertEquals("'a' should be 1", new Integer(1), _ctx.get("a"));
    assertEquals("'b' should be 2", new Integer(2), _ctx.get("b"));
    assertEquals("'c' should be 3", new Integer(3), _ctx.get("c"));
    assertEquals("'d' should be 4", new Integer(4), _ctx.get("d"));
    assertEquals("'e' should be 5", new Integer(5), _ctx.get("e"));
    assertEquals("'f' should be 6", new Integer(6), _ctx.get("f"));
    
    _ctx.revert();
    assertTrue("'a' should exist", _ctx.isDefinedVariable("a"));
    assertTrue("'b' should exist", _ctx.isDefinedVariable("b"));
    assertTrue("'c' should exist", _ctx.isDefinedVariable("c"));
    assertFalse("'d' should not exist", _ctx.isDefinedVariable("d"));
    assertFalse("'e' should not exist", _ctx.isDefinedVariable("e"));
    assertFalse("'f' should not exist", _ctx.isDefinedVariable("f"));
  }
}
