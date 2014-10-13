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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class BooleanOption.
 * @version $Id: BooleanOptionTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class BooleanOptionTest extends DrJavaTestCase
{
  /** @param name The name of this test case.
   */
  public BooleanOptionTest(String name) { super(name); }
  
  public void testGetName()
  {
    BooleanOption bo1 = new BooleanOption("enable JUnit",null);
    BooleanOption bo2 = new BooleanOption("use menu icons",null);
    
    assertEquals("enable JUnit", bo1.getName());
    assertEquals("use menu icons",   bo2.getName());
  }
  
  public void testParse()
  {
    BooleanOption bo = new BooleanOption("enable JUnit",null);
    
    assertEquals(Boolean.TRUE, bo.parse("true"));
    assertEquals(Boolean.FALSE, bo.parse("false"));
    assertEquals(Boolean.FALSE, bo.parse(" faLse "));
    
    try { bo.parse("3"); fail(); }
    catch (OptionParseException e) { }
    
    try { bo.parse("Tue"); fail(); }
    catch (OptionParseException e) { }
  }
  
  public void testFormat()
  {
    BooleanOption bo1 = new BooleanOption("max_files",null);
    BooleanOption bo2 = new BooleanOption("indent_size",null);
    
    assertEquals("true",  bo1.format(Boolean.TRUE));
    assertEquals("true",  bo2.format(Boolean.TRUE));
    assertEquals("false", bo1.format(Boolean.FALSE));
    assertEquals("false", bo2.format(Boolean.FALSE));
  }
}
