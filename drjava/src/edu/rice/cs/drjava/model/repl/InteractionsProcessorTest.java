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

package edu.rice.cs.drjava.model.repl;

import junit.framework.*;
import edu.rice.cs.javaast.parser.*;
import edu.rice.cs.javaast.tree.*;
import edu.rice.cs.javaast.*;

/**
 * Tests the behavior of the InteractionsProcessor.
 * @version $Id$
 */
public final class InteractionsProcessorTest extends TestCase {

  /**
   * InteractionsProcessor to be used in the test methods.
   */
  InteractionsProcessor _ip;

  protected void setUp() {
    _ip = new InteractionsProcessor();
  }

  /**
   * Tests a simple assignment to be sure it works.  More comprehensive
   * parser tests are in edu.rice.cs.javaast.InteractionsParserTest.
   */
  public void testPreProcessAssignment() throws ParseException
  {
    String s = _ip.preProcess("int x = 3;");
    assertEquals("assignment", "int x = 3;", s);
  }
  
  /**
   * Tests that generic statements are type erased.  More comprehensive
   * generic tests are in edu.rice.cs.javaast.TypeEraserTest.
   */
  public void testPreProcessGenerics() throws ParseException
  {
    String s = _ip.preProcess("Vector<String> v = new Vector<String>();");
    assertEquals("type-erased assignment", "Vector v = new Vector();", s);
  }
  
  /**
   * Tests that the correct exception is thrown on a syntax error.
   */
  public void testPreProcessSyntaxError()
  {
    try{
      String s = _ip.preProcess("i+");
      fail("preProcess failed, syntax error expected");
    }
    catch( ParseException pe ){
      // expected this.
    }
  }
  
  /**
   * Tests that the correct exception is thrown on a token manager error.
   */
  public void testPreProcessTokenMgrError()
  {
    try{
      String s = _ip.preProcess("#");
      fail("preProcess failed, token manager error expected");
    }
    catch( ParseException pe ){
      fail("preProcess failed, token manager error expected");
    }
    catch( TokenMgrError tme ){
     // this was what we wanted.
    }
  }
  
  /** 
   * Tests that the preprocessor will accept single-line comments. (Bug #768726)
   */
  public void testPreProcessSingleLineComments() {
    try {
      String s = _ip.preProcess("// Mary had a little lamb");
      assertEquals("The preprocessor should have removed the single-line comment.", 
                   "", 
                   s);
    }
    catch(ParseException e) {
      fail("preProcess failed, should have accepted the single-line comment.");
    }
  }
}
