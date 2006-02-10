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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  junit.framework.*;
import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Tests the Brace class.
 * @version $Id$
 */
public final class BraceTest extends DrJavaTestCase implements ReducedModelStates {
  protected Brace rparen;
  protected Brace lparen;

  /**
   * Set up Braces for testing.
   */
  public void setUp() throws Exception {
    super.setUp();
    lparen = Brace.MakeBrace("(", FREE);
    rparen = Brace.MakeBrace(")", FREE);
  }

  /**
   * Create the test suite.
   * @return BraceTest test suite
   */
  public static Test suite() {
    return  new TestSuite(BraceTest.class);
  }

  /**
   * Tests the successful construction of a Brace using the MakeBrace method.
   */
  public void testMakeBraceSuccess() {
    Brace brace = Brace.MakeBrace("{", FREE);
    assertEquals("{", brace.getType());
    assertEquals(1, brace.getSize());
  }

  /**
   * Tests the failure to make a Brace with a non-special character.
   */
  public void testMakeBraceFailure() {
    try {
      Brace.MakeBrace("k", FREE);
    } catch (BraceException e) {
      assertEquals("Invalid brace type \"k\"", e.getMessage());
    }
  }

  /**
   * Test the getType function for Braces.
   */
  public void testGetType() {
    assertEquals("(", lparen.getType());
    assertEquals(")", rparen.getType());
  }

  /**
   * Test the isShadowed() function.
   */
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

  /**
   * Test the isQuoted() function.
   */
  public void testIsQuoted() {
    assertTrue("#0.0", !lparen.isQuoted());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.1", lparen.isQuoted());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.2", !lparen.isQuoted());
  }

  /**
   * Test the isCommented() function.
   */
  public void testIsCommented() {
    assertTrue("#0.0", !lparen.isCommented());
    lparen.setState(INSIDE_BLOCK_COMMENT);
    assertTrue("#0.1", lparen.isCommented());
    lparen.setState(INSIDE_DOUBLE_QUOTE);
    assertTrue("#0.2", !lparen.isCommented());
  }

  /**
   * Test the toString method.
   */
  public void testToString() {
    assertEquals(" (", lparen.toString());
    assertEquals(" )", rparen.toString());
  }

  /**
   * Test the flip() method.
   */
  public void testFlip() {
    lparen.flip();
    rparen.flip();
    assertEquals("(", rparen.getType());
    assertEquals(")", lparen.getType());
  }

  /**
   * Test isOpen() and isClosed().
   */
  public void testOpenClosed() {
    assertTrue(lparen.isOpen());
    assertTrue(rparen.isClosed());
  }

  /**
   * Test isMatch(Brace) method.
   */
  public void testIsMatch() {
    Brace bracket = Brace.MakeBrace("]", FREE);
    Brace dummy = Brace.MakeBrace("", FREE);
    assertTrue(lparen.isMatch(rparen));
    assertTrue(!lparen.isMatch(bracket));
    assertTrue(!lparen.isMatch(dummy));
    assertTrue(!dummy.isMatch(lparen));
  }
}



