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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;


/**
 * Tests the Brace class.
 * @version $Id$
 */
public final class BraceTest extends TestCase implements ReducedModelStates {
 
  protected Brace rparen;
  protected Brace lparen;

  /**
   * Constructor.
   * @param     String name name of test
   */
  public BraceTest(String name) {
    super(name);
  }

  /**
   * Set up Braces for testing.
   */
  public void setUp() {
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
    Brace brace;
    try {
      brace = Brace.MakeBrace("k", FREE);
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



