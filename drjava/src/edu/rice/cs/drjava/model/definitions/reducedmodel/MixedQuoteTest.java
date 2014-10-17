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

import  junit.framework.*;

/**
 * Tests the interaction between double and single quotes and comments
 * @version $Id: MixedQuoteTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class MixedQuoteTest extends BraceReductionTestCase
  implements ReducedModelStates
{
  protected ReducedModelControl _model;

  /** Initializes the reduced models used in the tests.
   */
  protected void setUp() throws Exception {
    super.setUp();
    _model = new ReducedModelControl();
  }

  /** Creates a test suite for JUnit to use.
   * @return a test suite for JUnit
   */
  public static Test suite() {
    return  new TestSuite(MixedQuoteTest.class);
  }

  /** Convenience function to insert a number of non-special characters into a reduced model.
   * @param model the model being modified
   * @param size the number of characters being inserted
   */
  protected void insertGap(BraceReduction model, int size) {
    for (int i = 0; i < size; i++) {
      model.insertChar(' ');
    }
  }

  /** Tests how a single quote can eclipse the effects of a double quote by inserting
   * the single quote before the double quote.  This test caught an error with
   * getStateAtCurrent(): the check for double quote status checks if there is a double
   * quote immediately preceding, but it didn't make sure the double quote was FREE.
   * I fixed that, so now the test passes.
   */
  public void testSingleEclipsesDouble() {
    _model.insertChar('\"');
    assertEquals("#0.0", INSIDE_DOUBLE_QUOTE, _model.getStateAtCurrent());
    _model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(_model));
    _model.move(1);
    _model.insertChar('A');
    _model.move(-1);
    assertEquals("#1.0", INSIDE_DOUBLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#1.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(_model));
    assertTrue("#1.2", _model.currentToken().isGap());
    _model.move(-1);
    _model.insertChar('\'');
    assertEquals("#2.0", INSIDE_SINGLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(_model));
    assertEquals("#2.2", "\"", _model.currentToken().getType());
    _model.move(1);
    assertEquals("#3.0", INSIDE_SINGLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(_model));
    assertTrue("#3.2", _model.currentToken().isGap());
  }

  /** Tests how a double quote can eclipse the effects of a single quote by inserting
   * the double quote before the single quote.
   */
  public void testDoubleEclipsesSingle() {
    _model.insertChar('\'');
    assertEquals("#0.0", INSIDE_SINGLE_QUOTE, _model.getStateAtCurrent());
    _model.move(-1);
    assertEquals("#0.1", FREE, stateOfCurrentToken(_model));
    _model.move(1);
    _model.insertChar('A');
    _model.move(-1);
    assertEquals("#1.0", INSIDE_SINGLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#1.1", INSIDE_SINGLE_QUOTE, stateOfCurrentToken(_model));
    assertTrue("#1.2", _model.currentToken().isGap());
    _model.move(-1);
    _model.insertChar('\"');
    assertEquals("#2.0", INSIDE_DOUBLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#2.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(_model));
    assertEquals("#2.2", "\'", _model.currentToken().getType());
    _model.move(1);
    assertEquals("#3.0", INSIDE_DOUBLE_QUOTE, _model.getStateAtCurrent());
    assertEquals("#3.1", INSIDE_DOUBLE_QUOTE, stateOfCurrentToken(_model));
    assertTrue("#3.2", _model.currentToken().isGap());
  }
}
