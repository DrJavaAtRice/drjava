/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.drjava.config;

import edu.rice.cs.drjava.DrJavaTestCase;

import java.util.ArrayList;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class ForcedChoiceOption.
 * @version $Id$
 */
public final class ForcedChoiceOptionTest extends DrJavaTestCase {
  /**
   * @param name The name of this test case.
   */
  public ForcedChoiceOptionTest(String name) { super(name); }
  
  public void testGetName() {
    ForcedChoiceOption fco = new ForcedChoiceOption("javadoc_access",
                                                    "protected",
                                                    null);
    
    assertEquals("javadoc_access", fco.getName());
  }
  
  public void testParse() {
    ArrayList<String> aList = new ArrayList<String>(4);
    
    aList.add("public");
    aList.add("protected");
    aList.add("package");
    aList.add("private");
    ForcedChoiceOption fco = new ForcedChoiceOption("javadoc_access",
                                                    "protected",
                                                    aList);
    
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
    
    assertTrue("Formatting \"private\"", "private".equals(fco.format(new String("private"))));
    assertTrue("Formatting \"public\"", "public".equals(fco.format(new String("public"))));
  }
}
