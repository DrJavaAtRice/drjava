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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import junit.framework.*;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceException;

/**
 * Tests the Brace class.
 * @version $Id: BraceTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class BraceTest extends DrJavaTestCase implements ReducedModelStates {
  protected Brace rparen;
  protected Brace lparen;

  /** Set up Braces for testing.
   */
  public void setUp() throws Exception {
    super.setUp();
    lparen = Brace.MakeBrace("(", FREE);
    rparen = Brace.MakeBrace(")", FREE);
  }

  /** Create the test suite.
   * @return BraceTest test suite
   */
  public static Test suite() {
    return  new TestSuite(BraceTest.class);
  }

  /** Tests the successful construction of a Brace using the MakeBrace method.
   */
  public void testMakeBraceSuccess() {
    Brace brace = Brace.MakeBrace("{", FREE);
    assertEquals("{", brace.getType());
    assertEquals(1, brace.getSize());
  }

  /** Tests the failure to make a Brace with a non-special character.
   */
  public void testMakeBraceFailure() {
    try {
      Brace.MakeBrace("k", FREE);
    } catch (BraceException e) {
      assertEquals("Invalid brace type \"k\"", e.getMessage());
    }
  }

  /** Test the getType function for Braces. */
  public void testGetType() {
    assertEquals("(", lparen.getType());
    assertEquals(")", rparen.getType());
  }

  /** Test the isShadowed() function. */
  public void testIsShadowed() {
    assertTrue("#0.0", !lparen.isShadowed());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertEquals("#0.0.1", INSIDE_DOUBLE_QUOTE, lparen.getState());
    assertTrue("#0.1", lparen.isShadowed());
    rparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", rparen.isShadowed());
    rparen.setState(FREE);
    assertTrue("#0.3", !rparen.isShadowed());
  }

  /** Test the isQuoted() function.
   */
  public void testIsQuoted() {
    assertTrue("#0.0", !lparen.isQuoted());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.1", lparen.isQuoted());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", !lparen.isQuoted());
  }

  /** Test the isCommented() function.
   */
  public void testIsCommented() {
    assertTrue("#0.0", !lparen.isCommented());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.1", lparen.isCommented());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.2", !lparen.isCommented());
  }

  /** Test the toString method. */
  public void testToString() {
    assertEquals("Brace<(>", lparen.toString());
    assertEquals("Brace<)>", rparen.toString());
  }

  /** Test the flip() method. */
  public void testFlip() {
    lparen.flip();
    rparen.flip();
    assertEquals("(", rparen.getType());
    assertEquals(")", lparen.getType());
  }

  /** Test isOpen() and isClosed(). */
  public void testOpenClosed() {
    assertTrue(lparen.isOpen());
    assertTrue(rparen.isClosed());
  }

  /** Test isMatch(Brace) method.
   */
  public void testIsMatch() {
    Brace bracket = Brace.MakeBrace("]", FREE);
    Brace dummy = Brace.MakeBrace("", FREE);
    assertTrue(lparen.isMatch(rparen));
    assertTrue(!lparen.isMatch(bracket));
    assertTrue(!lparen.isMatch(dummy));
    assertTrue(!dummy.isMatch(lparen));
  }
  
  public void testSetTypeFalse() {
    try{
     lparen.setType("a");
     fail("Expected BraceException");
    }catch(BraceException b){ };
  }
  
  public void testIsSlashStar() {
    assertEquals(false, lparen.isSlash());
    Brace slash = Brace.MakeBrace("/",FREE);
    assertEquals(true, slash.isSlash());
    assertEquals(false, lparen.isStar());
    Brace star = Brace.MakeBrace("*",FREE);
    assertEquals(true, star.isStar());
  }
  
  public void testGrowFail() {
    try{
     lparen.grow(5);
     fail("Expected BraceException");
    }catch(BraceException b){ };
  }
  
  public void testShrinkFail() {
    try{
     lparen.shrink(5);
     fail("Expected BraceException");
    }catch(BraceException b){ };
  }
}



