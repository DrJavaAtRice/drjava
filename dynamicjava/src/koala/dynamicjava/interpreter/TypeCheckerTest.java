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

import java.lang.reflect.*;
import java.util.*;

import koala.dynamicjava.interpreter.context.*;
import koala.dynamicjava.interpreter.error.*;
import koala.dynamicjava.interpreter.modifier.*;
import koala.dynamicjava.interpreter.throwable.*;
import koala.dynamicjava.tree.*;
import koala.dynamicjava.tree.visitor.*;
import koala.dynamicjava.util.*;
import koala.dynamicjava.parser.wrapper.*;

import junit.framework.TestCase;

import edu.rice.cs.drjava.model.repl.*;

/**
 * This test class tests only those methods that were modified in order to ensure 
 * that the wrapper classes involved in autoboxing/unboxing are allowed.&nbsp; The 
 * methods that were changed pertained to those sections of the JLS that were 
 * modified by Sun when introducing this new feature.
 * <P>Involved Wrapper Classes:</P>
 * <UL>
 *   <LI>Boolean
 *   <LI>Byte
 *   <LI>Character
 *   <LI>Short
 *   <LI>Integer
 *   <LI>Long
 *   <LI>Float
 *   <LI>Double</LI></UL>
 * Involved Operations
 * <UL>
 *   <LI>Assignment
 *   <LI>Method Invocation
 *   <LI>Casting
 *   <LI>Numeric Promotions (Unary and Binary)
 *   <LI>The <CODE>if</CODE> Statement (<CODE>if-then</CODE> and <CODE>if-then-else</CODE>)
 *   <LI>The <CODE>switch</CODE> Statement
 *   <LI>The <CODE>while</CODE> Statement
 *   <LI>The <CODE>do</CODE> Statement
 *   <LI>The <CODE>for</CODE> Statement
 *   <LI>Array Creation
 *   <LI>Unary Operations:</LI>
 *   <UL>
 *     <LI>Postfix Decrement Operator <CODE>--</CODE>
 *     <LI>Postfix Decrement Operator <CODE>--</CODE>
 *     <LI>Prefix Increment Operator <CODE>++</CODE>
 *     <LI>Prefix Decrement Operator <CODE>--</CODE>
 *     <LI>Plus Operator <CODE>+</CODE>
 *     <LI>Minus Operator <CODE>-</CODE>
 *     <LI>Bitwise Complement Operator <CODE>~</CODE>
 *     <LI>Logical Complement Operator <CODE>!</CODE></LI></UL>
 *   <LI>Binary Operators</LI>
 *   <UL>
 *     <LI>Multiplicative Operators <CODE>*, /, %</CODE>
 *     <LI>Additive Operators <CODE>+, -</CODE>
 *     <LI>Shift Operators <CODE>&lt;&lt;, &gt;&gt;, &gt;&gt;&gt;</CODE>
 *     <LI>Numerical Comparison Operators <CODE>&lt;, &lt;=, &gt;, and &gt;=</CODE>
 *     <LI>Integer Bitwise Operators <CODE>&amp;, ^, and |</CODE>
 *     <LI>Boolean Logical Operators <CODE>&amp;, ^, and |</CODE>
 *     <LI>Conditional Operators <CODE>&amp;&amp;, ||</CODE>
 *     <LI>Conditional Operator <CODE>? :</CODE></LI></UL>
 * </UL>
 * NOTE: Though not explicitly stated in the changed sections of the JLS, the methods 
 * associated with the assignment operators (<CODE>+=, -=, *=, /=, %=, &lt;&lt;=, &gt;&gt;&gt;=, 
 * &gt;&gt;&gt;=, &amp;=, ^=, |=</CODE>) must also be modified and thus tested
 */
public class TypeCheckerTest extends TestCase {
  
  ////// Internal Initialization ////////////////////////
  
  /**
   * The global context we are using.
   */
  private GlobalContext _globalContext;
  
  /**
   * The type checker we are testing.
   */
  private TypeChecker _typeChecker;
  
  /**
   * The interpreter we are using to test our modifications of the ASTs.
   */
  private JavaInterpreter _interpreter;
  
  /**
   * Sets up the tests for execution.
   */
  public void setUp() {
    _globalContext = new GlobalContext(new TreeInterpreter(new JavaCCParserFactory()));
    _globalContext.define("x", int.class);
    _globalContext.define("X", Integer.class);
    _globalContext.define("b", boolean.class);
    _globalContext.define("B", Boolean.class);
    _globalContext.define("I", int[].class);
    _typeChecker = new TypeChecker(_globalContext);
    _interpreter = new DynamicJavaAdapter();
    
    try {
      _interpreter.interpret("int x = 0;");
      _interpreter.interpret("Integer X = new Integer(0);");
      _interpreter.interpret("Boolean B = Boolean.FALSE;");
      _interpreter.interpret("boolean b = false;");
      _interpreter.interpret("int[] I = {1, 2, 3};");
    }
    catch (ExceptionReturnedException ere) {
      fail("Should have been able to declare variables for interpreter.");
    }
  }
  
  /**
   * Parses the given string and returns the list of Nodes.
   * @param code the code to parse
   * @return the list of Nodes
   */
  private List<Node> _parseCode(String code) {
    JavaCCParserFactory parserFactory = new JavaCCParserFactory();
    SourceCodeParser parser = parserFactory.createParser(new java.io.StringReader(code), "");
    return parser.parseStream();
  }
  
  /**
   * Checks a binary expression to make sure that it is getting typed correctly.
   */
  private Class _checkBinaryExpression(String text, 
                                       String leftExpected, 
                                       String rightExpected) 
    throws ExceptionReturnedException {
    
    BinaryExpression exp = (BinaryExpression)_parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getLeftExpression().toString();
    assertEquals("Left should have typed correctly.", leftExpected, actual);

    actual = exp.getRightExpression().toString();
    assertEquals("Right should have typed correctly.", rightExpected, actual);
    
    _interpreter.interpret(text);
    return type;
  }
  
  private Class _checkUnaryExpression(String text, String expected) 
    throws ExceptionReturnedException {
    
    UnaryExpression exp = (UnaryExpression)_parseCode(text).get(0);
    
    Class type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getExpression().toString();
    assertEquals("Expression should have typed correctly.", expected, actual);

    _interpreter.interpret(text);
    
    return type;
  }
  
  ////// Control Statements /////////////////////////////
  
  /**
   * Tests the While statement's condition statement
   */
  public void testVisitWhileStatement() throws ExceptionReturnedException {
    String text = "while (B) { }";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((WhileStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }
  
  /**
   * Tests the do-while loop's condition statement
   */
  public void testVisitDoStatement() throws ExceptionReturnedException {
    String text = "do { } while(B);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((DoStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }  
  
  /**
   * Tests the for loop's condition statement
   */
  public void testVisitForStatement() throws ExceptionReturnedException {
    String text = "for(int i=0; new Boolean(i<1); i++);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.LessExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    String actual = ((ForStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);

    _interpreter.interpret(text);
  }
  
  public void testSwitchStatement() throws ExceptionReturnedException {
    String text = "switch (new Integer(1)) { }";
    SwitchStatement stmt = (SwitchStatement)_parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String actual = stmt.getSelector().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }
  

  public void testIfThenStatement() throws ExceptionReturnedException {
    String text = "if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }
  
  /**
   * Tests the if-then-else statement for auto-unboxing.
   */
  public void testIfThenElseStatement() throws ExceptionReturnedException {
    String text = "if (B) { } else if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpreter.interpret(text);
  }
  
  //////////// Addititve Bin Ops ////////////////////////
  /**
   * Tests adding two Integers.
   */
  public void testAddTwoIntegers() throws ExceptionReturnedException {
    
    String text = "new Integer(1) + new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }

  /**
   * Tests substracting two Integers.
   */
  public void testSubtractingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) - new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  ///////////// Additive Assignemt //////////////////////
  
  /**
   * Tests the += operation.
   */
  public void testPlusEquals() throws ExceptionReturnedException {
    // Tests plus-equals with a primitive left-hand side
    String text = "x += new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests plus-equals with a reference-type left-hand side
    text = "X += new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the ++ operation.
   */
  public void testPlusPlus() throws ExceptionReturnedException {
    // Tests ++ with a reference-type left-hand side, post-increment
    String text = "X++;";
    String expected = "(koala.dynamicjava.tree.QualifiedName: X)";
    
    _checkUnaryExpression(text, expected);

    // Tests ++ with a reference-type left-hand side, pre-increment
    text = "++X;";
    expected = "(koala.dynamicjava.tree.QualifiedName: X)";
    
    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests the -= operation.
   */
  public void testMinusEquals() throws ExceptionReturnedException {
    // Tests minus-equals with a primitive left-hand side
    String text = "x -= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests minus-equals with a reference-type left-hand side
    text = "X -= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the -- operation.
   */
  public void testMinusMinus() throws ExceptionReturnedException {
    // Tests "--" with a reference-type left-hand side, post-decrement
    String text = "X--;";
    String expected = "(koala.dynamicjava.tree.QualifiedName: X)";
    
    _checkUnaryExpression(text, expected);

    // Tests "--" with a reference-type left-hand side, pre-decrement
    text = "--X;";
    expected = "(koala.dynamicjava.tree.QualifiedName: X)";
    
    _checkUnaryExpression(text, expected);
  }
  
  
  //////////// Multiplicitive Bin Ops ///////////////////
  
  /**
   * Tests multiplying two Integers.
   */
  public void testMultiplyingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) * new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testDividingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) / new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
      
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testModingTwoIntegers() throws ExceptionReturnedException {
    String text = "new Integer(1) % new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  //////////// Multiplicitive Assignments ///////////////
  
  /**
   * Tests the *= operation.
   */
  public void testMultEquals() throws ExceptionReturnedException {
    // Tests times-equals with a primitive left-hand side
    String text = "x *= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests times-equals with a reference-type left-hand side
    text = "X *= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the /= operation.
   */
  public void testDivideEquals() throws ExceptionReturnedException {
    // Tests divide-equals with a primitive left-hand side
    String text = "x /= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests divide-equals with a reference-type left-hand side
    text = "X /= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the %= operation.
   */
  public void testModEquals() throws ExceptionReturnedException {
    // Tests mod-equals with a primitive left-hand side
    String text = "x %= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests mod-equals with a reference-type left-hand side
    text = "X %= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Shift Bin Ops ////////////////////////////
  /**
   * Tests Shift Right on two Shorts
   */
  public void testShiftRight() throws ExceptionReturnedException {
    String text = "(new Short(\"1\") >> new Short(\"2\"));";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"1\" 1 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Shift Left on two Shorts
   */
  public void testShiftLeft() throws ExceptionReturnedException {
    String text = "new Short(\"-10\") << new Short(\"2\");";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"-10\" -10 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Unsigned Shift on two longs
   */
  public void testUShiftRight() throws ExceptionReturnedException {
    String text = "new Long(-1) >>> new Long(1);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.MinusExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Shift Assignments ////////////////////////

  
  /**
   * Tests the <<= operation.
   */
  public void testLeftShiftEquals() throws ExceptionReturnedException {
    // Tests left-shift-equals with a primitive left-hand side
    String text = "x <<= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests left-shift-equals with a reference-type left-hand side
    text = "X <<= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the >>= operation.
   */
  public void testRightShiftEquals() throws ExceptionReturnedException {
    // Tests right-shift-equals with a primitive left-hand side
    String text = "x >>= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests right-shift-equals with a reference-type left-hand side
    text = "X >>= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the >>>= operation.
   */
  public void testUnsignedRightShiftEquals() throws ExceptionReturnedException {
    // Tests u-right-shift-equals with a primitive left-hand side
    String text = "x >>>= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests u-right-shift-equals with a reference-type left-hand side
    text = "X >>>= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  

  //////////// Bitwise Bin Ops //////////////////////////
  
  /**
   * Tests XORing two Booleans.
   */
  public void testBooleanBitwiseXOr() throws ExceptionReturnedException {
    String text = "new Boolean(true) ^ new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Booleans.
   */
  public void testBooleanBitwiseAnd() throws ExceptionReturnedException {
    String text = "new Boolean(true) & new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Booleans.
   */
  public void testBooleanBitwiseOr() throws ExceptionReturnedException {
    String text = "new Boolean(true) | new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests XORing two Booleans.
   */
  public void testNumericBitwiseXOr() throws ExceptionReturnedException {
    String text = "new Long(0) ^ new Integer(1);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 0 0 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Integers.
   */
  public void testNumericBitwiseAnd() throws ExceptionReturnedException {
    String text = "new Character('a') & new Integer(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: charValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Character) [(koala.dynamicjava.tree.CharacterLiteral: 'a' a char)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";


    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Integers.
   */
  public void testNumericBitwiseOr() throws ExceptionReturnedException {
    String text = "new Short(\"2\") | new Byte(\"2\");";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: byteValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Byte) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Bitwise Assignments //////////////////////
  
  /**
   * Tests the &= operation.
   */
  public void testAndEquals() throws ExceptionReturnedException {
    // Tests and-equals with a primitive left-hand side
    String text = "x &= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests and-equals with a reference-type left-hand side
    text = "X &= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the ^= operation.
   */
  public void testXorEquals() throws ExceptionReturnedException {
    // Tests xor-equals with a primitive left-hand side
    String text = "x ^= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests xor-equals with a reference-type left-hand side
    text = "X ^= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the |= operation.
   */
  public void testOrEquals() throws ExceptionReturnedException {
    // Tests or-equals with a primitive left-hand side
    String text = "x |= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests or-equals with a reference-type left-hand side
    text = "X |= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  
  //////////// Boolean/Comparative Bin Ops //////////////
  
  /**
   * Tests ANDing two Booleans.
   */
  public void testAndingTwoBooleans() throws ExceptionReturnedException {
    String text = "new Boolean(true) && new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests ORing two Booleans.
   */
  public void testOringTwoBooleans() throws ExceptionReturnedException {
    String text = "new Boolean(true) || new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan with two Doubles
   */
  public void testGreaterThan() throws ExceptionReturnedException {
    String text = "new Double(1) > new Double(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan or Equal to with two Floats
   */
  public void testGreaterThanEqual() throws ExceptionReturnedException {
    String text = "new Float(1) >= new Float(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Float) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Float) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan to with two Longs
   */
  public void testLessThan() throws ExceptionReturnedException {
    String text = "new Long(12) < new Long(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Long) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan Or Equal to with two Integers
   */
  public void testLessThanEqual() throws ExceptionReturnedException {
    String text = "new Integer(12) <= new Integer(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the equality operator (==) with an integer and a short.
   */
  public void testEqualsEquals() throws ExceptionReturnedException {
    String text = "new Integer(1) == new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) == 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the inequality operator (!=) with an integer and a short.
   */
  public void testNotEquals() throws ExceptionReturnedException {
    String text = "new Integer(1) != new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) != 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Compliment Unary Op //////////////////////
  
  /**
   * Tests Complimenting an Integer.
   */
  public void testComplimentingOneBoolean() throws ExceptionReturnedException {
    String text = "~new Integer(24);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 24 24 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Plus Operator.
   */
  public void testPlusOperator() throws ExceptionReturnedException {
    String text = "+new Double(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Double) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Minus Operator.
   */
  public void testMinusOperator() throws ExceptionReturnedException {
    String text = "-new Integer(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }

  /**
   * Tests Negating a Boolean.
   */
  public void testNegatingOneBoolean() throws ExceptionReturnedException {
    String text = "!new Boolean(false);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkUnaryExpression(text, expected);
  }
  
  
  //////////// Other Operations //////////////////////
  
  public void testSimpleAssignBox() throws ExceptionReturnedException {
    String text = "B = true;";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: B)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Boolean) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.BooleanLiteral: true true boolean) (koala.dynamicjava.tree.BooleanType: boolean))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignUnbox() throws ExceptionReturnedException {  
    String text = "b = new Boolean(false);";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: b)";
    String rightExpected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignBoxInt() throws ExceptionReturnedException {
    String text = "X = 3 + 5;";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: X)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.AddExpression: (koala.dynamicjava.tree.IntegerLiteral: 3 3 int) (koala.dynamicjava.tree.IntegerLiteral: 5 5 int)) (koala.dynamicjava.tree.IntType: int))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignBoxAddExp() throws ExceptionReturnedException {
    String text = "X = new Integer(1) + new Integer(3);";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: X)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.AddExpression: (koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)])) (koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 3 3 int)]))) (koala.dynamicjava.tree.IntType: int))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
   
  public void testVariableDeclaration()  throws ExceptionReturnedException {
    String text = "Integer i = 1;";
    String initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntType: int))])";
    VariableDeclaration exp = (VariableDeclaration)_parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpreter.interpret(text);
    
    text = "Long L = 1;";
    initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Long) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.LongType: long))])";
    exp = (VariableDeclaration)_parseCode(text).get(0);
    type = exp.acceptVisitor(_typeChecker);
    actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpreter.interpret(text);
    
    text = "Double D = 1;";
    initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Double) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.DoubleType: double))])";
    exp = (VariableDeclaration)_parseCode(text).get(0);
    type = exp.acceptVisitor(_typeChecker);
    actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpreter.interpret(text);
  }
  
  public void testUnboxingCastExpression() throws ExceptionReturnedException{
    String text = "(int)new Integer(1);";
  
    Node exp = _parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the primitive type.", int.class, type);

    _interpreter.interpret(text);
  }
  
  public void testBoxingCastExpression() throws ExceptionReturnedException{
    String text = "(Integer)1;";
    Node exp = _parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the refrence type.", Integer.class, type);

    _interpreter.interpret(text);
  }
  
  public void testDoubleCastExpression() throws ExceptionReturnedException{
    String text = "(Byte)(byte)1;";
    Node exp = _parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the primitive type.", Byte.class, type);

    _interpreter.interpret(text);
  }
  
  public void testErroneousUnboxingCastExpression() throws ExceptionReturnedException{
    try {
      String text = "(boolean)new Long(3);";
      _parseCode(text).get(0).acceptVisitor(_typeChecker);
      fail("Should have thrown an error");
    }
    catch (ExecutionError e) { /* expected */ }
  }
  
  /**
   * Tests the coniditional expression (... ? ... : ...).
   */
  public void testConditionalExpressions() throws ExceptionReturnedException{
    
    // This test makes sure that the special case where one of the operands is
    // a Boolean reference type and the other is a primitive type works
    String text = "(B) ? B : 1;";
    ConditionalExpression stmt = (ConditionalExpression)_parseCode(text).get(0);
    Class type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 1: the type should have been Object", Object.class, type);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getConditionExpression().toString();
    assertEquals("Test 1: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.QualifiedName: B)";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 1: Should have left B alone", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntType: int))])";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 1: Should have boxed the int", expected, actual);
    
    String definition = "Boolean B = Boolean.TRUE; ";
    _interpreter.interpret(text);
    
    // This test is to make sure that a boxing type will get unboxed
    // if the other operand is a numerical primitive type
    text = "(B) ? new Integer(1) : 1;";
    stmt = (ConditionalExpression)_parseCode(text).get(0);
    type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 2: the type should have been int", int.class, type);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    actual = stmt.getConditionExpression().toString();
    assertEquals("Test 2: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 2: Should have unboxed the integer", expected, actual);
    
    expected = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 2: Should have left the int alone", expected, actual);
    
    _interpreter.interpret(text);
    
    // This test makes sure that both primitives are boxed in the case
    // that one is a boolean and the other is not.
    text = "(B) ? true : 1;";
    stmt = (ConditionalExpression)_parseCode(text).get(0);
    type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 3: the type should have been int", Object.class, type);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    actual = stmt.getConditionExpression().toString();
    assertEquals("Test 3: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Boolean) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.BooleanLiteral: true true boolean) (koala.dynamicjava.tree.BooleanType: boolean))])";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 3: Should have boxed the boolean", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntType: int))])";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 3: Should have boxed the int", expected, actual);
    
    _interpreter.interpret(text);
  }
  
  public void testArrayAllocation() throws ExceptionReturnedException {
    String text = "new int[new Integer(5)];";
    String sizeExpected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 5 5 int)]))";
    ArrayAllocation exp = (ArrayAllocation) _parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getSizes().get(0).toString();
    assertEquals("The size should have been unboxed.", sizeExpected, actual);
    _interpreter.interpret(text);
 
    text = "new int[new Long(0)];";
    exp = (ArrayAllocation) _parseCode(text).get(0);
    try {
      type = exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an exception.");
    }
    catch (ExecutionError e) {
    }
  }
  
  public void testArrayAccess() throws ExceptionReturnedException {
    String text = "I[new Integer(0)];";
    String idxExpected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceType: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 0 0 int)]))";
    ArrayAccess exp = (ArrayAccess)_parseCode(text).get(0);
    Class type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getCellNumber().toString();
    assertEquals("The index should have been unboxed.", idxExpected, actual);
    _interpreter.interpret(text);
 
    text = "I[new Long(0)];";
    exp = (ArrayAccess)_parseCode(text).get(0);
    try {
      type = exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an exception.");
    }
    catch (ExecutionError e) {
    }
  }

  /**
   * Method calls may or may not be a simple project.  We need to look into what
   * needs to be tested, the different types of method calls that must change,
   * what specifications have been added in the jsr language specs...
   */
}
