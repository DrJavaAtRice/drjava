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
  
  /**
   * At one point passing in an int array for the in varargs would cause
   * djava to throw an unexpected ClassCastException inside performCast(EvaluationVisitor.java:2129)
   * Also, if the class was defined to have an Integer varargs signature, passing in the classic
   * array form would cause an IllegalArgumentException with Array.set inside
   * buildArrayOfRemainingArgs(EvaluationVisitor.java:808)
   */
  public void testVarArgsWithMethodInvocation() throws InterpreterException {
    String text = "public class ClassA { public static void x(int... y) { } }; ClassA.x(new int[]{1,2,3});";
    Object res = interpret(text);
    text = "public class ClassB { public static Object x(Integer... y) { return y; } };";
    interpret(text);
    
    text = "ClassB.x(new Integer[]{1,2,3})";
    res = interpret(text);
    assertFalse("Result shouldn't be null", res == null);
    assertEquals("Should be an array of Integers", Integer[].class, res.getClass());
    
    text = 
      "import java.util.*;" + 
      "public class ClassC {" +
      "  public static List<Integer> asList(Integer... a) {" + 
      "    ArrayList<Integer> list = new ArrayList<Integer>(a.length);"+
      "    for(Integer i : a) {" + 
      "      list.add(i);" +
      "    }" +
      "    return list;" +
      "  }" +
      "}";
    interpret(text);
    
    text = "ClassC.asList(new Integer[]{1,2,3})";
    res = interpret(text);
    assertTrue("res should be an instance of a List", res instanceof java.util.List);
    assertEquals("first element should be 1", new Integer(1), ((java.util.List)res).get(0));
    
    text = "Arrays.asList(new Integer[]{1,2,3})";
    res = interpret(text);
    assertTrue("res should be an instance of a List", res instanceof java.util.List);
    assertEquals("first element should be 1", new Integer(1), ((java.util.List)res).get(0));
    
    text = "Arrays.asList(1,2,3,4)";
    res = interpret(text);
    assertTrue("res should be an instance of a List", res instanceof java.util.List);
    assertEquals("size should be 4", 4, ((java.util.List)res).size());
    assertEquals("last element should be 4", new Integer(4), ((java.util.List)res).get(3));
    
    text = 
      "class ClassD { " +
      "  public String m(String ... args) { " +
      "    String ret = \"\"; " +
      "    for(String s : args) ret += s; " +
      "    return ret; " + 
      "  }" +
      "}\n" +
      "new ClassD().m(\"a\",\"b\",\"c\",\"d\");";
    res = interpret(text);
    assertEquals("Wrong output.", "abcd", res);

    text = 
      "public class ClassE { " +
      "  public class Inner { " +
      "    public String m(String ... args) { " +
      "      String ret = \"\"; " +
      "      for(String s : args) ret += s; " +
      "      return ret; " + 
      "    }" +
      "  }" +
      "}\n" +
      "(new ClassE()).new Inner().m(\"a\",\"b\",\"c\",\"d\");";
    res = interpret(text);
    assertEquals("Wrong output.", "abcd", res);
        
    text =
      "public class ClassG {\n"+
      "  public class Inner {\n"+
      "    String str = \"\";\n"+
      "    public Inner(String ... args){\n"+
      "      for(String  s: args) {\n"+
      "        str = str+s;\n"+
      "      }\n"+
      "    }\n"+
      "    public String getStr(){\n"+
      "      return str;\n"+
      "    }\n"+
      "  }\n"+
      "}\n"+
      "(new ClassG()).new Inner(\"a\",\"b\",\"c\",\"d\").getStr();\n";
    res = interpret(text);
    assertEquals("Wrong Output.", "abcd", res);
   
    
    text =
      "interface Lambda { public Object apply(Object ... args); } \n" +
      "Lambda l = new Lambda() { \n" +
      "  public Object apply(Object... args) { \n" +
      "    return true; \n" +
      "  } \n" +
      "}; \n" +
      "l.apply()";
    res = interpret(text);
    assertEquals("Wrong output.", Boolean.TRUE, res);
    
  }
  
  public void testInnerClassScoping() {
    String text;
    Object res;
    
    text = 
      "interface I { \n" +
      "  String foo(); \n" +
      "} \n" +
      "class ClassH { \n" +
      "  public String run() { \n" +
      "    final String str = \"hi\"; \n" +
      "    return new I() { \n" +
      "      public String foo() { \n" +
      "        return str + \" there!\"; \n" +
      "      } \n" +
      "    }.foo(); \n" +
      "  } \n" +
      "} \n" +
      "new ClassH().run()";
    
    res = interpret(text);
    assertEquals("Wrong Output.", "hi there!", res);
    
    // The feature tested by this block of code was not
    // implemented in dynamicjava since the beginning
    text =
      "public class ClassF { \n" +
      "  public String run() { \n" +
      "    final String str = \"hi\"; \n" +
      "    class B { \n" +
      "      public String foo() { \n" +
      "        return str + \" there!\"; \n" +
      "      } \n" +
      "    } \n" +
      "    return new B().foo(); \n" +
      "  } \n" +
      "} \n" +
      "new ClassF().run()";
//    res = interpret(text);
//    assertEquals("Wrong Output.", "hi there!", res);
    
  }
  
  //Assert tests
  public void testSimpleTopLevelAssert() {
    testString = "assert(true);";
    
    interpret(testString);
    
    testString = "assert(false);";
    try {
      interpret(testString);
      fail("Assertion should have failed");
    }
    catch(AssertionError e) {
      //Expected 
    }
     
    testString = "boolean bool = true;\n"+
      "boolean c = false;\n"+
      "assert(bool);";
    interpret(testString);
    
    testString = "assert(c);";
    try {
      interpret(testString);
      fail("Assertion should have failed");
    }
    catch(AssertionError e) {
      //Expected 
    } 
  }
  
  public void testAutoBoxAssert() {
   testString = "Boolean bool = true;\n"+
     "Boolean c = false;\n"+
     "assert(bool);";
   interpret(testString);
    
   testString = "assert(c);";
    try{
      interpret(testString);
      fail("Assertion should have failed");
    }
    catch(AssertionError e) {
      //Expected 
    }
  }
  
  public void testExpressionAssert() {
    testString = "assert( true || false );";
    interpret(testString);
    
      
    testString = "assert true || false \n";
    interpret(testString);
  }
  
  public void testMessagePassedWithAssert() {
    testString = "assert(false) : \"this message was passed\";";
    try{
      interpret(testString);
      fail("Assertion should have failed");
    }
    catch(AssertionError e) {
      assertEquals(e.getMessage(),"this message was passed");
    }
  }
}
