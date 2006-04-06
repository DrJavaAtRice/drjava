/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
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
    