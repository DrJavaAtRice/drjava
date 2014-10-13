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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Tests that the DebugWatchData class can display state correctly.
 * @version $Id: DebugWatchDataTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class DebugWatchDataTest extends DrJavaTestCase {
 
  /** Tests the state of a watch after its creation.
   */
  public void testFirstCreation() {
    DebugWatchData data = new DebugWatchData("foo");
    assertEquals("should have a name on startUp",
                 "foo", data.getName());
    assertEquals("should have no value on startUp",
                 "", data.getValue());
    assertEquals("should have no type on startUp",
                 "", data.getType());
    assertTrue("should not be changed on startUp", !data.isChanged());
  }
  
  /** Tests that a watch displays its value and type correctly,
   * then hides it when the thread resumes.  Also tests that
   * the changed flag works correctly.
   */
  public void testInScopeThenCleared() {
    DebugWatchData data = new DebugWatchData("foo");

    // Set value and type
    data.setValue(Integer.valueOf(7));
    data.setType("java.lang.Integer");
    assertEquals("should have a value", "7", data.getValue());
    assertEquals("should have a type", "java.lang.Integer", data.getType());
    assertTrue("should be changed", data.isChanged());
    
    // Hide value and type
    data.hideValueAndType();
    assertEquals("should have no value after hide",
                 "", data.getValue());
    assertEquals("should have no type after hide",
                 "", data.getType());
    assertTrue("should not be changed after hide", !data.isChanged());
    
    // Make sure using same value doesn't indicate a change
    data.setValue(Integer.valueOf(7));
    assertTrue("should not be changed after setting same value",
               !data.isChanged());
    
    // Make sure using a new value indicates a change
    data.setValue(Integer.valueOf(8));
    assertTrue("should be changed after setting different value",
               data.isChanged());
  }
  
  /** Tests that a watch displays correctly if it is not in scope.
   */
  public void testNotInScope() {
    DebugWatchData data = new DebugWatchData("bar");
    data.setNoValue();
    data.setNoType();
    
    assertEquals("should not be in scope",
                 DebugWatchData.NO_VALUE, data.getValue());
    assertEquals("should not have a type",
                 DebugWatchData.NO_TYPE, data.getType());
    assertTrue("should not appear changed", !data.isChanged());
  } 
}