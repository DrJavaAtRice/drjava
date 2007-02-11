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
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava;

import junit.framework.TestCase;
import edu.rice.cs.util.swing.Utilities;

/** Test case class for all DrJava test cases. DrJava test cases should extend this class, potentially override setUp()
 *  and tearDown(), but make sure to invoke super.setUp() and super.tearDown() appropriately. That ensures that the 
 *  system is correctly initialized for every test.
 */
public class DrJavaTestCase extends TestCase {
  /** System property with the name of an alternative DrJava configuration file used during testing. */
  public static final String TEST_DRJAVA_CONFIG_PROPERTY = "test.drjava.config";
    
  /** Create a new DrJava test case. */
  public DrJavaTestCase() { super(); }

  /** Create a new DrJava test case.
   *  @param name name of the test case
   */
  public DrJavaTestCase(String name) { super(name); }

  /** Set up for every test.
   *  @throws Exception
   */
  protected void setUp() throws Exception {
    super.setUp();
    Utilities.TEST_MODE = true;
    final String newName = System.getProperty(TEST_DRJAVA_CONFIG_PROPERTY);
    if (newName!=null) {
      DrJava.setPropertiesFile(newName);
      DrJava._initConfig();
    }
  }

  /** Clean up for every test case.
   *  @throws Exception
   */
  protected void tearDown() throws Exception { super.tearDown(); }
}
