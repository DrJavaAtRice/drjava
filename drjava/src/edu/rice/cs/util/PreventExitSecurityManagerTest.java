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

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;

/** Test cases for {@link PreventExitSecurityManager}.
 *
 *  @version $Id$
 */
public class PreventExitSecurityManagerTest extends DrJavaTestCase {
  private PreventExitSecurityManager _manager;

  /** Activates the security manager before each test. */
  public void setUp() throws Exception {
    super.setUp();
    _manager = PreventExitSecurityManager.activate();
  }

  /** Deactivates the security manager after each test. */
  public void tearDown() throws Exception {
    _manager.deactivate();
    super.tearDown();
  }

  public void testSystemExitPrevented() {
    try {
      System.exit(1);
      fail("System.exit passed?!");
    }
    catch (ExitingNotAllowedException se) {
      // good.
    }
  }

  public void testExitVMRespectsBlock() {
    _manager.setBlockExit(true);
    try {
      _manager.exitVM(-1);
      fail("exitVM passed while blocked!");
    }
    catch (ExitingNotAllowedException se) {
      // good
    }
  }

  public void testCanNotChangeSecurityManager() {
    try {
      System.setSecurityManager(null);
      fail("setSecurityManager passed!");
    }
    catch (SecurityException se) {
      // good
    }
  }
}
