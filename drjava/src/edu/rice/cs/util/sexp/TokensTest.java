/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
package edu.rice.cs.util.sexp;

import junit.framework.TestCase;

/** Test class for Tokens.java in this package. */
public class TokensTest extends TestCase {
  
  static Tokens.WordToken tok1 = new Tokens.WordToken("this");
  static Tokens.QuotedTextToken tok2 = new Tokens.QuotedTextToken("this");
  static Tokens.SExpToken tok3 = new Tokens.SExpToken("this");
  static Tokens.NumberToken tok4 = new Tokens.NumberToken(7);
  static Tokens.NumberToken tok5 = new Tokens.NumberToken(12);
  
  public void testEquals() {
    
    assertEquals("\\ token equals test", Tokens.BackSlashToken.ONLY, Tokens.BackSlashToken.ONLY);
    assertFalse("\\ token not equals test", Tokens.BackSlashToken.ONLY.equals(new Tokens.SExpToken("\\")));
    
    assertEquals("( token equals test", Tokens.LeftParenToken.ONLY, Tokens.LeftParenToken.ONLY);
    assertFalse("\\ token not equals test", Tokens.LeftParenToken.ONLY.equals(new Tokens.SExpToken("(")));
    
    assertEquals(") token equals test", Tokens.RightParenToken.ONLY, Tokens.RightParenToken.ONLY);
    assertFalse("\\ token not equals test", Tokens.RightParenToken.ONLY.equals(new Tokens.SExpToken(")")));
    
    assertEquals("FALSE token equals test", Tokens.BooleanToken.FALSE, Tokens.BooleanToken.FALSE);
    assertFalse("FALSE token not equals test", Tokens.BooleanToken.FALSE.equals(new Tokens.SExpToken("FALSE")));
    
    assertEquals("TRUE token equals test", Tokens.BooleanToken.TRUE, Tokens.BooleanToken.TRUE);
    assertFalse("TRUE token not equals test", Tokens.BooleanToken.TRUE.equals(new Tokens.SExpToken("TRUE")));
    
    assertEquals("Tokens.WordToken equals test", tok1, tok1);
    assertFalse("Tokens.WordToken not equals test 1", tok1.equals(tok2));
    assertFalse("Tokens.WordToken not equals test 2", tok1.equals(tok3));
    
    assertEquals("Tokens.QuotedTextToken equals test", tok2, tok2);
    assertFalse("Tokens.QuotedTextToken not equals test 1", tok2.equals(tok1));
    assertFalse("Tokens.QuotedTextToken not equals test 2", tok2.equals(tok3));
    
    assertEquals("Tokens.NumberToken equals test", tok4, tok4);
    assertFalse("Tokens.NumberToken not equals test 1", tok4.equals(tok5));
    assertFalse("Tokens.NumberToken not equals test 2", tok4.equals(tok3));
  }
}
    