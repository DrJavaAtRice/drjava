/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava;

import junit.framework.TestCase;
import edu.rice.cs.util.swing.Utilities;

/** Test case class for all DrJava test cases. DrJava test cases should extend this class, potentially override setUp()
  * and tearDown(), but make sure to invoke super.setUp() and super.tearDown() appropriately. That ensures that the 
  * system is correctly initialized for every test.
  */
public class DrJavaTestCase extends TestCase {
  /** System property with the name of an alternative DrJava configuration file used during testing. */
  public static final String TEST_DRJAVA_CONFIG_PROPERTY = "test.drjava.config";
  
  /** Create a new DrJava test case. */
  public DrJavaTestCase() { super(); }
  
  /** Create a new DrJava test case.
    * @param name name of the test case
    */
  public DrJavaTestCase(String name) { super(name); }
  
  /** Set up for every test.
    * @throws Exception.  This convention is mandated by JUnit.TestCase, the superclass of this class.
    */
  protected void setUp() throws Exception {
    super.setUp();  // declared to throw Exception, forcing throws clause on preceding line
    Utilities.TEST_MODE = true;
    final String newName = System.getProperty(TEST_DRJAVA_CONFIG_PROPERTY);
    assert newName != null;
    if (newName != null) {
//      Utilities.show("Setting '" + newName + "' as DrJava configuration file");
      DrJava.setPropertiesFile(newName);
      Utilities.clearEventQueue();
      DrJava._initConfig();
      Utilities.clearEventQueue();
    }
  }
  
  /** Clean up for every test case.  Only used in unit tests.  Added because Windows would intermittently throw
    * a java.util.concurrent.RejectedExecutionException during cleanup.
    * @throws Exception
    */
  protected void tearDown() throws Exception { 
    DrJava.cleanUp();  // clobbers _config
    super.tearDown();
  }
}
