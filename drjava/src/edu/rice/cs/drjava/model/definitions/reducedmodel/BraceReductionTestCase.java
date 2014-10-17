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

import edu.rice.cs.drjava.DrJavaTestCase;

/** Test cases which test the implementation of BraceReduction may extend this abstract class to acquire a convenience
  * function for determining the state of the current token.
  * @version $Id: BraceReductionTestCase.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class BraceReductionTestCase extends DrJavaTestCase {
  protected volatile ReducedModelControl model0;
  protected volatile ReducedModelControl model1;
  protected volatile ReducedModelControl model2;

  /** Sets up the reduced model controls before each test. */
  protected void setUp() throws Exception {
    super.setUp();
    model0 = new ReducedModelControl();
    model1 = new ReducedModelControl();
    model2 = new ReducedModelControl();
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

  /** Convenience function to get state of the current token.
    * @param br the brace reduction in question
    * @return the state of the current token
    */
  ReducedModelState stateOfCurrentToken(BraceReduction br) {
    return br.currentToken().getState();
  }
}