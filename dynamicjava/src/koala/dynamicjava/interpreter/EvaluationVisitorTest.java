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
package koala.dynamicjava.interpreter;

import java.io.StringReader;
import junit.framework.TestCase;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.parser.wrapper.*;

/**
 * So far this test case tests the auto boxing/unboxing capabilities
 * of these operators: <br>
 * <code> ++ -- += -= *= /= %= &gt;&gt;= &gt;&gt;&gt;= &lt;&lt;= |= &amp;= ^= </code>
 * <br><br>
 * The autoboxing would normally be done in the TypeChecker visitor,
 * but the necessary modifications to the AST could not be carried
 * out there. For that reason, those cases are handled directly in the
 * evaluation visitor.
 */
public class EvaluationVisitorTest extends DynamicJavaTestCase {

  private TreeInterpreter astInterpreter;
  private TreeInterpreter strInterpreter;

  private ParserFactory parserFactory;
  private String testString;

  public void setUp() throws java.io.IOException {
    setTigerEnabled(true);
    parserFactory = new JavaCCParserFactory();
    astInterpreter = new TreeInterpreter(null); // No ParserFactory needed to interpret an AST
    strInterpreter = new TreeInterpreter(parserFactory); // ParserFactory is needed to interpret a string

    try {
      interpret("int x = 0;");
      interpret("Integer X = new Integer(0);");
      interpret("Boolean B = Boolean.FALSE;");
      interpret("boolean b = false;");
      interpret("int[] i = {1, 2, 3};");
      interpret("Integer[] I = {1, 2, 3};");
    }
    catch (InterpreterException ie) {
      fail("Should have been able to declare variables for interpreter.");
    }
  }

  public void tearDown() {
    TigerUtilities.resetVersion();
  }

  public Object interpret(String testString) throws InterpreterException {
    return strInterpreter.interpret(new StringReader(testString), "Unit Test");
  }


//  why is this here??
//  private AssignExpression _parseAssignExpression(String text) {
//    JavaCCParserFactory parserFactory = new JavaCCParserFactory();
//    SourceCodeParser parser = parserFactory.createParser(new java.io.StringReader(text), "");
//    try {
//      return (AssignExpression) parser.parseStream().get(0);
//    }
//    catch (ClassCastException e) {
//      throw new ClassCastException("The parsed expression was not an AssignExpression: "+
//                                   "\"" + text + "\"");
//    }
//  }

  /**
   * Tests the += operator
   */
  public void testAddAssign() throws InterpreterException {
    String text = "X+=5";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 5", "5", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 5", "5", res.toString());
  }

  /**
   * Tests the ++ operator
   */
  public void testIncrement() throws InterpreterException {
    String text = "X++";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 0", "0", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 1", "1", res.toString());

    text = "++X";
    res  = interpret(text);
    assertEquals("X should have the Integer value 2", "2", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 2", "2", res.toString());
  }

  /**
   * Tests the -= operator
   */
  public void testSubAssign() throws InterpreterException {
    String text = "X-=5";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value -5", "-5", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value -5", "-5", res.toString());
  }

  /**
   * Tests the -- operator
   */
  public void testDecrement() throws InterpreterException {
    String text = "X--";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 0", "0", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value -1", "-1", res.toString());

    text = "--X";
    res  = interpret(text);
    assertEquals("X should have the Integer value -2", "-2", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value -2", "-2", res.toString());
  }

  /**
   * Tests the *= operator
   */
  public void testMultAssign() throws InterpreterException {
    String text = "X=1; X*=5";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 5", "5", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 5", "5", res.toString());
  }

  /**
   * Tests the /= operator
   */
  public void testDivAssign() throws InterpreterException {
    String text = "X=5; X/=5";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 1", "1", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 1", "1", res.toString());
  }

  /**
   * Tests the %= operator
   */
  public void testRemainderAssign() throws InterpreterException {
    String text = "X=7; X %= 5";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 2", "2", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 2", "2", res.toString());
  }

  /**
   * Tests the <<= operator
   */
  public void testLeftShiftAssign() throws InterpreterException {
    String text = "X=1; X <<= 3";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 8", "8", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 8", "8", res.toString());
  }

  /**
   * Tests the >>= operator
   */
  public void testRightShiftAssign() throws InterpreterException {
    String text = "X=8; X >>= 3";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 1", "1", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 1", "1", res.toString());
  }

  /**
   * Tests the >>>= operator
   */
  public void testUnsignedRightShiftAssign() throws InterpreterException {
    String text = "X=-1; X >>>= 1";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 2147483647", "2147483647", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 2147483647", "2147483647", res.toString());
  }

  /**
   * Tests the &= operator
   */
  public void testBitAndAssign() throws InterpreterException {
    String text = "X=0; X &= 1";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 0", "0", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 0", "0", res.toString());
  }

  /**
   * Tests the |= operator
   */
  public void testBitOrAssign() throws InterpreterException {
    String text = "X=0; X |= 1";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 1", "1", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 1", "1", res.toString());
  }

  /**
   * Tests the ^= operator
   */
  public void testBitXOrAssign() throws InterpreterException {
    String text = "X=0; X ^= 1";
    Object res  = interpret(text);
    assertEquals("X should have the Integer value 1", "1", res.toString());

    res = interpret("X");
    assertTrue("X should have been an Integer", res instanceof Integer);
    assertEquals("X should have the Integer value 1", "1", res.toString());
  }

  public void testUnaryArrayOps() throws InterpreterException {
    String text =
      "char [] c = {\'a\', \'b\'};\n"+
      "c[0]++;\n"+
      "c[0];";
    Object res = interpret(text);
    assertEquals("The first character of the array should be \'b\'","b",res.toString());

    res = interpret("c[0]--");
    assertEquals("char value should still be \'b\'","b",res.toString());
    res = interpret("--c[0]");
    assertEquals("char value should be \'`\'","`",res.toString());
  }
}
