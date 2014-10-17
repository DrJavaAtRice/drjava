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

/**
 * Tests the functionality of the Gap class.
 * @version $Id: GapTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class GapTest extends DrJavaTestCase {
  /** Tests the ability to grow a Gap.
   */
  public void testGrow() {
    Gap gap0 = new Gap(0, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.grow(5);
    assertEquals(5, gap0.getSize());
    gap0.grow(0);
    assertEquals(5, gap0.getSize());
    gap1.grow(-6);
    assertEquals(1, gap1.getSize());
  }

  /** Tests the ability to shrink a Gap.
   */
  public void testShrink() {
    Gap gap0 = new Gap(5, ReducedToken.FREE);
    Gap gap1 = new Gap(1, ReducedToken.FREE);
    gap0.shrink(3);
    assertEquals(2, gap0.getSize());
    gap0.shrink(0);
    assertEquals(2, gap0.getSize());
    gap1.shrink(3);
    assertEquals(1, gap1.getSize());
    gap1.shrink(-1);
    assertEquals(1, gap1.getSize());
  }
}



