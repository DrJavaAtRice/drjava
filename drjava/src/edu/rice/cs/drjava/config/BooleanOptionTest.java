/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import junit.framework.*;

/**
 * Class according to the JUnit protocol. Tests
 * the proper functionality of the class BooleanOption.
 * @version $Id$
 */
public final class BooleanOptionTest extends TestCase
{
  /**
   * @param name The name of this test case.
   */
  public BooleanOptionTest(String name) { super(name); }
  
  public void setUp() {}
  
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
    catch (OptionParseException e) {}
    
    try { bo.parse("Tue"); fail(); }
    catch (OptionParseException e) {}
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
