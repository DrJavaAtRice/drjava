/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.ArrayList;

/** * Class according to the JUnit protocol. Tests
 * the proper functionality of the class ForcedChoiceOption.
 * @version $Id$
 */
public final class ForcedChoiceOptionTest extends DrJavaTestCase {
  /** @param name The name of this test case.
   */
  public ForcedChoiceOptionTest(String name) { super(name); }
  
  public void testGetName() {
    ForcedChoiceOption fco = new ForcedChoiceOption("javadoc_access", "protected", null);
    
    assertEquals("javadoc_access", fco.getName());
  }
  
  public void testParse() {
    ArrayList<String> aList = new ArrayList<String>(4);
    
    aList.add("public");
    aList.add("protected");
    aList.add("package");
    aList.add("private");
    ForcedChoiceOption fco = new ForcedChoiceOption("javadoc_access", "protected", aList);
    
    assertTrue("Parsing \"private\"", "private".equals(fco.parse("private")));
    try { fco.parse("Private"); fail(); }
    catch (OptionParseException e) { }
    
    try { fco.parse("true"); fail(); }
    catch (OptionParseException e) { }
    
    try { fco.parse(".33"); fail(); }
    catch (OptionParseException e) { }
  }
  
  public void testFormat() {
    ForcedChoiceOption fco = new ForcedChoiceOption("javadoc_access",
                                                    "protected",
                                                    null);
    
    assertTrue("Formatting \"private\"", "private".equals(fco.format("private")));
    assertTrue("Formatting \"public\"", "public".equals(fco.format("public")));
  }
}
