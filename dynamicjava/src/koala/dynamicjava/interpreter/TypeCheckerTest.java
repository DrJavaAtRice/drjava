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
public class TypeCheckerTest extends DynamicJavaTestCase {
  
  ////// Internal Initialization ////////////////////////
  
  /**
   * The global context we are using for the type checker.
   */
  private GlobalContext _globalContext;

    /**
   * The global context we are using for the name visitor.
   */
  private GlobalContext _globalNameContext;

  /**
   * The type checker we are testing.
   */
  private AbstractTypeChecker _typeChecker;
  
  /**
   * The interpreter we are using to test our modifications of the ASTs.
   */
  private TreeInterpreter _interpreter;
  
  private JavaCCParserFactory parserFactory;
  
  private static final String VERSION_KEY = "java.specification.version";
  
  /**
   * Sets up the tests for execution.
   */
  public void setUp() {
    // This test is dependent on 1.5 since the ObjectMethodCall uses 1.5 reflection methods.
    // If this were run in 1.4 and we faked the version property to 1.5, some methods would
    // not be found during the test and would cause the test case to fail.
//    String version = System.getProperty(VERSION_KEY);
//    if (Float.valueOf(version) < 1.5) {
//      throw new WrongVersionException("This test case requires Java 2 SDK v1.5.0 or better");
//    }
    //This test will fail if not running 1.5 OR 1.4 with jsr14
    setTigerEnabled(true);
    
    parserFactory = new JavaCCParserFactory();
    _globalContext = new GlobalContext(new TreeInterpreter(parserFactory));
    _globalContext.define("x", int.class);
    _globalContext.define("X", Integer.class);
    _globalContext.define("b", boolean.class);
    _globalContext.define("B", Boolean.class);
    _globalContext.define("I", int[].class);

    _globalNameContext = new GlobalContext(new TreeInterpreter(parserFactory));
    _globalNameContext.define("x", int.class);
    _globalNameContext.define("X", Integer.class);
    _globalNameContext.define("B", Boolean.class);
    _globalNameContext.define("b", boolean.class);
   
    
    //makeTypeChecker will return the correct type checker depending on the current runtime version of Java
    _typeChecker = AbstractTypeChecker.makeTypeChecker(_globalContext);
    _interpreter = new TreeInterpreter(parserFactory);
    
    try {
      _interpretText("int x = 0;");
      _interpretText("Integer X = new Integer(0);");
      _interpretText("Boolean B = Boolean.FALSE;");
      _interpretText("boolean b = false;");
      _interpretText("int[] I = {1, 2, 3};");
    }
    catch (InterpreterException ere) {
      fail("Should have been able to declare variables for interpreter.");
    }
  }
  
  public void tearDown() {
    TigerUtilities.resetVersion();
  }
  
  
  /**
   * Parses the given string and returns the list of Nodes.
   * @param code the code to parse
   * @return the list of Nodes
   */
  private List<Node> _parseCode(String code) {
    SourceCodeParser parser = parserFactory.createParser(new java.io.StringReader(code), "");
    return parser.parseStream();
  }
  
  private Object _interpretText(String text) throws InterpreterException {
    List<Node> exps = _interpreter.buildStatementList(new java.io.StringReader(text), "Unit Test");
    return _interpreter.interpret(exps);
  }
  
  /**
   * Checks a binary expression to make sure that it is getting typed correctly.
   */
  private Class<?> _checkBinaryExpression(String text, 
                                       String leftExpected, 
                                       String rightExpected) 
    throws InterpreterException {
    
    BinaryExpression exp = (BinaryExpression)_parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getLeftExpression().toString();
    assertEquals("Left should have typed correctly.", leftExpected, actual);

    actual = exp.getRightExpression().toString();
    assertEquals("Right should have typed correctly.", rightExpected, actual);
    
    _interpretText(text);
    return type;
  }
  
  private Class<?> _checkUnaryExpression(String text, String expected) 
    throws InterpreterException {
    
    UnaryExpression exp = (UnaryExpression)_parseCode(text).get(0);
    
    Class<?> type = exp.acceptVisitor(_typeChecker);
        
    String actual = exp.getExpression().toString();
    assertEquals("Expression should have typed correctly.", expected, actual);

    _interpretText(text);
    
    return type;
  }
  
  ////// Control Statements /////////////////////////////
  
  /**
   * Tests the While statement's condition statement
   */
  public void testVisitWhileStatement() throws InterpreterException {
    String text = "while (B) { }";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((WhileStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpretText(text);
    
    text = "while (new Character('a')) { }";
    stmt = _parseCode(text).get(0);
    
    try {
      stmt.acceptVisitor(_typeChecker);
      fail("shouldn't allow while statement with character");
    }
    catch (ExecutionError e) {
      //Test Passed
    }

  }
  
  /**
   * Tests the do-while loop's condition statement
   */
  public void testVisitDoStatement() throws InterpreterException {
    String text = "do { } while(B);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = ((DoStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpretText(text);
    
    text = "do { } while( 'a' );";
    stmt = _parseCode(text).get(0);
    try {
      stmt.acceptVisitor(_typeChecker);
      fail("shouldn't accept integer for boolean expression");
    }
    catch (ExecutionError e) {
      //Test Passed
    }
    
  }  
  
  /**
   * Tests the for loop's condition statement
   */
  public void testVisitForStatement() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "for(int i=0; new Boolean(i<1); i++);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(nv);
    stmt.acceptVisitor(_typeChecker);

    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.LessExpression: (koala.dynamicjava.tree.QualifiedName: i) (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    String actual = ((ForStatement)stmt).getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);

    _interpretText(text);
    
    text = "for(; new Integer(5););";
    stmt = _parseCode(text).get(0);
    try {
      stmt.acceptVisitor(_typeChecker);
      fail("shouldn't accept integer for boolean expression");
    }
    catch (ExecutionError e) {
      //Test Passed
    }    
  }
  
  /**
   * test type checking of collection/array in for each statements
   */
  public void testVisitForEachStatementStringVector() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "java.util.Vector<String> ss = new java.util.Vector<String>();for(String s: ss);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(nv);
    stmt.acceptVisitor(_typeChecker);
    //throws exception if types don't check
  }
  
  public void testVisitForEachStatementStringArrayError() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "String[] ss = {\"asf\",\"qwer\"};for(Integer s: ss);";
    List<Node> stmts = _parseCode(text);
    Node stmt;
    try{
      for(int i=0;i<stmts.size();i++){
        stmt = stmts.get(i);
        stmt.acceptVisitor(nv);
        stmt.acceptVisitor(_typeChecker);
      }
     fail("For each statement with string collection and Integer formal parameter should not type check.");
    }
    catch(ExecutionError e){
    }
  }

  
  public void testVisitForEachStatementStringArrayNarrow() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "String[] ss = {\"asf\",\"qwer\"};for(Object s: ss);";
    List<Node> stmts = _parseCode(text);
    Node stmt;
    for(int i=0;i<stmts.size();i++){
      stmt = stmts.get(i);
      stmt.acceptVisitor(nv);
      stmt.acceptVisitor(_typeChecker);
    }
  }
/*
  public void testVisitForEachStatementStringArrayUnbox() throws ExceptionReturnedException {
    String text = "Integer[] ss = {1,2};for(int s: ss);";
    List<Node> stmts = _parseCode(text);
    Node stmt;
    for(int i=0;i<stmts.size();i++){
      stmt = stmts.get(i);
      stmt.acceptVisitor(_typeChecker);
    }
  }
*/
  
  public void testVisitForEachStatementStringArray() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "String[] ss = {\"asf\",\"qwer\"};for(String s: ss);";
    List<Node> stmts = _parseCode(text);
    Node stmt;
    for(int i=0;i<stmts.size();i++){
      stmt = stmts.get(i);
      stmt.acceptVisitor(nv);
      stmt.acceptVisitor(_typeChecker);
    }
  }
 
    /*  public void testVisitForEachStatementStringVectorError() throws ExceptionReturnedException {
    String text = "java.util.Vector<String> ss = new java.util.Vector<String>();for(Integer s: ss);";
    List<Node> stmts = _parseCode(text);
    Node stmt;
    try{
      for(int i=0;i<stmts.size();i++){
        stmt = stmts.get(i);
        stmt.acceptVisitor(_typeChecker);
      }
     fail("For each statement with string collection and Integer formal parameter should not type check.");
    }
    catch(ExecutionError e){
    }
  }*/

  
  
  
  public void testVisitForEachStatementObjectVector() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "java.util.Vector ss = new java.util.Vector();for(Object s: ss);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(nv);
    stmt.acceptVisitor(_typeChecker);
    //throws exception if types don't check
  }
  
  public void testVisitForEachStatementStringVector2() throws InterpreterException {
    NameVisitor nv = new NameVisitor(_globalNameContext);
    String text = "java.util.Vector ss = new java.util.Vector();for(String s: ss);";
    Node stmt = _parseCode(text).get(0);
    
    stmt.acceptVisitor(nv);
    stmt.acceptVisitor(_typeChecker);
    //throws exception if types don't check
  }
  
  
  
  
  public void testSwitchStatement() throws InterpreterException {
    String text = "switch (new Integer(1)) { }";
    SwitchStatement stmt = (SwitchStatement)_parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String actual = stmt.getSelector().toString();
    assertEquals("Should have autounboxed", expected, actual);


    text = "switch( 'a' ) { case 'a': }";
    _parseCode(text).get(0).acceptVisitor(_typeChecker);
    
    try {
      text = "switch ('a') { case \"adsf\": }";
      _parseCode(text).get(0).acceptVisitor(_typeChecker);
      fail("shouldn't be able to switch Strings");
    }
    catch (ExecutionError e) {
 // DO NOTHING
    }
    //System.out.println(_parseCode(text).get(0));
    
    text = "switch (5) { case 'a': }";
    expected = "(koala.dynamicjava.tree.SwitchStatement: (koala.dynamicjava.tree.IntegerLiteral: 5 5 int) [(koala.dynamicjava.tree.SwitchBlock: (koala.dynamicjava.tree.CharacterLiteral: 'a' a char) null)])";
    Statement stmt1 = (Statement)_parseCode(text).get(0);
    actual = stmt1.toString();
    assertEquals("should parse switch into selector and switch block", expected, actual);
    stmt1.acceptVisitor(_typeChecker);
  
    text = "switch ((byte)5) { case 'a': }";
    expected = "(koala.dynamicjava.tree.SwitchStatement: (koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 5 5 int) (koala.dynamicjava.tree.ByteTypeName: byte)) [(koala.dynamicjava.tree.SwitchBlock: (koala.dynamicjava.tree.CharacterLiteral: 'a' a char) null)])";
    List stmts = _parseCode(text);
    stmt1 = (Statement)stmts.get(0);
    actual = stmt1.toString();
    assertEquals("should parse switch into selector and switch block", expected, actual);
    stmt1.acceptVisitor(_typeChecker);
   
    text = "switch ((short)5) { case 5: default: }";
    expected = "(koala.dynamicjava.tree.SwitchStatement: (koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 5 5 int) (koala.dynamicjava.tree.ShortTypeName: short)) [(koala.dynamicjava.tree.SwitchBlock: (koala.dynamicjava.tree.IntegerLiteral: 5 5 int) null), (koala.dynamicjava.tree.SwitchBlock: null null)])";
    stmt1 = (Statement)_parseCode(text).get(0);
    actual = stmt1.toString();
    assertEquals("should parse switch into selector and switch block", expected, actual);
    stmt1.acceptVisitor(_typeChecker);

    try {
      text = "boolean BB = true; switch (BB) { case true: case false: }";
      _interpretText(text);
      fail("shouldn't be able to switch booleans");
    } 
    catch (ExecutionError e) {
 //DO NOTHING
    }
  }
  

  /**
   * Test for try-catch-finally clauses
   */
  public void testTryCatchStatement() {
    String text = "try { } catch (Exception e) { }";
    TryStatement stmt = (TryStatement)_parseCode(text).get(0);
    
    // Test 1
    String expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    String actual = stmt.getTryBlock().toString();
    assertEquals("should note empty block statement", expected, actual);
    expected = "(koala.dynamicjava.tree.CatchStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: Exception) e) (koala.dynamicjava.tree.BlockStatement: []))";
    assertEquals("should contain 1 catch statement", 1, stmt.getCatchStatements().size());
    assertEquals("first catch block should accept Exception and be empty", expected, stmt.getCatchStatements().get(0).toString());
    stmt.acceptVisitor(_typeChecker);
    _interpretText(text);

    // Test 2
    text = "try { } catch (String s) { }";
    stmt = (TryStatement)_parseCode(text).get(0);
    expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    actual = stmt.getTryBlock().toString();
    assertEquals("should note empty block statement", expected, actual);

    expected = "(koala.dynamicjava.tree.CatchStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: String) s) (koala.dynamicjava.tree.BlockStatement: []))";
    assertEquals("first catch block should accept Exception and be empty", expected, stmt.getCatchStatements().get(0).toString());
    try {
      stmt.acceptVisitor(_typeChecker);
      fail("should have thrown error with exception of type String");
    }
    catch (ExecutionError e) {
      // Test Passed
    }
    
    // Test 3
    text = "try { } finally { }";
    stmt = (TryStatement)_parseCode(text).get(0);
    expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    actual = stmt.getTryBlock().toString();
    assertEquals("should note empty block statement", expected, actual);
    assertEquals("should contain no catch blocks", 0, stmt.getCatchStatements().size());
    expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    actual = stmt.getFinallyBlock().toString();
    assertEquals("should contain empty finally block", expected, actual);
    stmt.acceptVisitor(_typeChecker);
    _interpretText(text);

    // Test 4
    text = "try { throw new java.io.FileNotFoundException(\"error\"); } catch (IllegalArgumentException e) { }";
    stmt = (TryStatement)_parseCode(text).get(0);
    expected = "(koala.dynamicjava.tree.BlockStatement: [(koala.dynamicjava.tree.ThrowStatement: (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.io.FileNotFoundException) [(koala.dynamicjava.tree.StringLiteral: \"error\" error class java.lang.String)]))])";
    actual = stmt.getTryBlock().toString();
    assertEquals("should get thrown exception", expected, actual);
    // This expression generates a compile error in javac
    //try {
      stmt.acceptVisitor(_typeChecker);
    //  fail("must catch exception that is thrown");
    //}
    //catch (ExecutionError e) {
    //}
   
    // Test 5
    text = "try { } catch (Exception e) { } catch (RuntimeException e) { } finally { }";
    stmt = (TryStatement)_parseCode(text).get(0);
    expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    actual = stmt.getTryBlock().toString();
    assertEquals("should note empty try statement", expected, actual);
    actual = stmt.getCatchStatements().get(0).toString();
    expected = "(koala.dynamicjava.tree.CatchStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: Exception) e) (koala.dynamicjava.tree.BlockStatement: []))";
    assertEquals("should note empty block statement", expected, actual);
    actual = stmt.getCatchStatements().get(1).toString();
    expected = "(koala.dynamicjava.tree.CatchStatement: (koala.dynamicjava.tree.FormalParameter: false (koala.dynamicjava.tree.ReferenceTypeName: RuntimeException) e) (koala.dynamicjava.tree.BlockStatement: []))";
    assertEquals("should note empty block statement", expected, actual);
    assertEquals("should note two catch statements", 2, stmt.getCatchStatements().size());
    actual = stmt.getFinallyBlock().toString();
    expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    assertEquals("should note empty finally statement", expected, actual);
    stmt.acceptVisitor(_typeChecker);
    _interpretText(text);
  }
/*
  public void testLabeledStatement() {
    String text = "lbl:\n int cc = 1+5;"; // This generates an error...
    LabeledStatement stmt = (LabeledStatement)_parseCode(text).get(0);
    
    // Test 1
    String expected = "(koala.dynamicjava.tree.LabeledStatement: lbl (koala.dynamicjava.tree.ObjectMethodCall: println [(koala.dynamicjava.tree.StringLiteral: \"testing\" testing class java.lang.String)] (koala.dynamicjava.tree.QualifiedName: System.out)))";
    String actual = stmt.toString();
    assertEquals("should yield label statement", expected, actual);
    stmt.acceptVisitor(_typeChecker);
  }
*/
  
  public void testSynchronizedStatement() { 
    String text = "synchronized (Integer.class) { }";
    SynchronizedStatement stmt = (SynchronizedStatement)_parseCode(text).get(0);
    
    String expected = "(koala.dynamicjava.tree.BlockStatement: [])";
    String actual = stmt.getBody().toString();
    assertEquals("body of synchronized should be empty", expected, actual);
    expected = "(koala.dynamicjava.tree.TypeExpression: (koala.dynamicjava.tree.ReferenceTypeName: Integer))";
    actual = stmt.getLock().toString();
    assertEquals("should be locking on Class object", expected, actual);
  }
  
  public void testIfThenStatement() throws InterpreterException {
    String text = "if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpretText(text);
  }
  
  /**
   * Tests the if-then-else statement for auto-unboxing.
   */
  public void testIfThenElseStatement() throws InterpreterException {
    String text = "if (B) { } else if (B) { }";
    IfThenStatement stmt = (IfThenStatement) _parseCode(text).get(0);
    
    stmt.acceptVisitor(_typeChecker);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getCondition().toString();
    assertEquals("Should have autounboxed", expected, actual);
    
    _interpretText(text);
  }
  
  //////////// Addititve Bin Ops ////////////////////////
  /**
   * Tests adding two Integers.
   */
  public void testAddTwoIntegers() throws InterpreterException {
    
    String text = "new Integer(1) + new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }

  /**
   * Tests substracting two Integers.
   */
  public void testSubtractingTwoIntegers() throws InterpreterException {
    String text = "new Integer(1) - new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  ///////////// Additive Assignemt //////////////////////
  
  /**
   * Tests the += operation.
   */
  public void testPlusEquals() throws InterpreterException {
    // Tests plus-equals with a primitive left-hand side
    String text = "x += new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests plus-equals with a reference-type left-hand side
    text = "X += new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the ++ operation.
   */
  public void testPlusPlus() throws InterpreterException {
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
  public void testMinusEquals() throws InterpreterException {
    // Tests minus-equals with a primitive left-hand side
    String text = "x -= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests minus-equals with a reference-type left-hand side
    text = "X -= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the -- operation.
   */
  public void testMinusMinus() throws InterpreterException {
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
  public void testMultiplyingTwoIntegers() throws InterpreterException {
    String text = "new Integer(1) * new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testDividingTwoIntegers() throws InterpreterException {
    String text = "new Integer(1) / new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
      
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests dividing two Integers.
   */
  public void testModingTwoIntegers() throws InterpreterException {
    String text = "new Integer(1) % new Integer(2);";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  //////////// Multiplicitive Assignments ///////////////
  
  /**
   * Tests the *= operation.
   */
  public void testMultEquals() throws InterpreterException {
    // Tests times-equals with a primitive left-hand side
    String text = "x *= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests times-equals with a reference-type left-hand side
    text = "X *= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the /= operation.
   */
  public void testDivideEquals() throws InterpreterException {
    // Tests divide-equals with a primitive left-hand side
    String text = "x /= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests divide-equals with a reference-type left-hand side
    text = "X /= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the %= operation.
   */
  public void testModEquals() throws InterpreterException {
    // Tests mod-equals with a primitive left-hand side
    String text = "x %= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests mod-equals with a reference-type left-hand side
    text = "X %= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Shift Bin Ops ////////////////////////////
  /**
   * Tests Shift Right on two Shorts
   */
  public void testShiftRight() throws InterpreterException {
    String text = "(new Short(\"1\") >> new Short(\"2\"));";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Short) [(koala.dynamicjava.tree.StringLiteral: \"1\" 1 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Shift Left on two Shorts
   */
  public void testShiftLeft() throws InterpreterException {
    String text = "new Short(\"-10\") << new Short(\"2\");";
    
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Short) [(koala.dynamicjava.tree.StringLiteral: \"-10\" -10 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Unsigned Shift on two longs
   * Note: Test changed on may 25th 2005, as result of altering the parser to handle ints and longs correctly (long x = -9223372036854775808L used to fail).
   * We now return an IntegerLiteral instead of a MinusExpression if the minus is followed by a number (either int or long).
   */
  public void testUShiftRight() throws InterpreterException {
    String text = "new Long(-1) >>> new Long(1);";
    String result = (_parseCode(text).get(0)).toString();
    String expected = "(koala.dynamicjava.tree.UnsignedShiftRightExpression: (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: -1 -1 int)]) (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    assertEquals("String does not match expected string", expected, result);
    
    
    //String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.MinusExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int))]))";
    //String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
   //    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Shift Assignments ////////////////////////
 
 
  
    
  /**
   * Tests the <<= operation.
   */
  public void testLeftShiftEquals() throws InterpreterException {
    // Tests left-shift-equals with a primitive left-hand side
    String text = "x <<= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests left-shift-equals with a reference-type left-hand side
    text = "X <<= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the >>= operation.
   */
  public void testRightShiftEquals() throws InterpreterException {
    // Tests right-shift-equals with a primitive left-hand side
    String text = "x >>= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests right-shift-equals with a reference-type left-hand side
    text = "X >>= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the >>>= operation.
   */
  public void testUnsignedRightShiftEquals() throws InterpreterException {
    // Tests u-right-shift-equals with a primitive left-hand side
    String text = "x >>>= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests u-right-shift-equals with a reference-type left-hand side
    text = "X >>>= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  

  //////////// Bitwise Bin Ops //////////////////////////
  
  /**
   * Tests XORing two Booleans.
   */
  public void testBooleanBitwiseXOr() throws InterpreterException {
    String text = "new Boolean(true) ^ new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Booleans.
   */
  public void testBooleanBitwiseAnd() throws InterpreterException {
    String text = "new Boolean(true) & new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Booleans.
   */
  public void testBooleanBitwiseOr() throws InterpreterException {
    String text = "new Boolean(true) | new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests XORing two Booleans.
   */
  public void testNumericBitwiseXOr() throws InterpreterException {
    String text = "new Long(0) ^ new Integer(1);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: 0 0 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise AND on Integers.
   */
  public void testNumericBitwiseAnd() throws InterpreterException {
    String text = "new Character('a') & new Integer(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: charValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Character) [(koala.dynamicjava.tree.CharacterLiteral: 'a' a char)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";


    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests Bitwise OR on Integers.
   */
  public void testNumericBitwiseOr() throws InterpreterException {
    String text = "new Short(\"2\") | new Byte(\"2\");";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: shortValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Short) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: byteValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Byte) [(koala.dynamicjava.tree.StringLiteral: \"2\" 2 class java.lang.String)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Bitwise Assignments //////////////////////
  
  /**
   * Tests the &= operation.
   */
  public void testAndEquals() throws InterpreterException {
    // Tests and-equals with a primitive left-hand side
    String text = "x &= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests and-equals with a reference-type left-hand side
    text = "X &= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the ^= operation.
   */
  public void testXorEquals() throws InterpreterException {
    // Tests xor-equals with a primitive left-hand side
    String text = "x ^= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests xor-equals with a reference-type left-hand side
    text = "X ^= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the |= operation.
   */
  public void testOrEquals() throws InterpreterException {
    // Tests or-equals with a primitive left-hand side
    String text = "x |= new Integer(2);";
    String expectedLeft = "(koala.dynamicjava.tree.QualifiedName: x)";
    String expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);

    // Tests or-equals with a reference-type left-hand side
    text = "X |= new Integer(2);";
    expectedLeft = "(koala.dynamicjava.tree.QualifiedName: X)";
    expectedRight = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)])";
    
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  
  //////////// Boolean/Comparative Bin Ops //////////////
  
  /**
   * Tests ANDing two Booleans.
   */
  public void testAndingTwoBooleans() throws InterpreterException {
    String text = "new Boolean(true) && new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests ORing two Booleans.
   */
  public void testOringTwoBooleans() throws InterpreterException {
    String text = "new Boolean(true) || new Boolean(false);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: true true boolean)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan with two Doubles
   */
  public void testGreaterThan() throws InterpreterException {
    String text = "new Double(1) > new Double(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Double) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Double) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
    
  /**
   * Tests GreaterThan or Equal to with two Floats
   */
  public void testGreaterThanEqual() throws InterpreterException {
    String text = "new Float(1) >= new Float(2);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Float) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: floatValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Float) [(koala.dynamicjava.tree.IntegerLiteral: 2 2 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan to with two Longs
   */
  public void testLessThan() throws InterpreterException {
    String text = "new Long(12) < new Long(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: longValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Long) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
        
  /**
   * Tests LessThan Or Equal to with two Integers
   */
  public void testLessThanEqual() throws InterpreterException {
    String text = "new Integer(12) <= new Integer(32);";
      
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 12 12 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 32 32 int)]))";

    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the equality operator (==) with an integer and a short.
   */
  public void testEqualsEquals() throws InterpreterException {
    String text = "new Integer(1) == new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) == 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  /**
   * Tests the inequality operator (!=) with an integer and a short.
   */
  public void testNotEquals() throws InterpreterException {
    String text = "new Integer(1) != new Short(\"1\");";
    
    try {
      _checkBinaryExpression(text, "does not matter", "does not matter");
      fail("Should have thrown an execution error because you can't compare Integer and Short.");
    }
    catch (ExecutionError ee) {
    }

    text = "new Integer(1) != 1;";
    String expectedLeft = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    String expectedRight = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    _checkBinaryExpression(text, expectedLeft, expectedRight);
  }
  
  //////////// Compliment Unary Op //////////////////////
  
  /**
   * Tests Complimenting an Integer.
   */
  public void testComplimentingOneBoolean() throws InterpreterException {
    String text = "~new Integer(24);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 24 24 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Plus Operator.
   */
  public void testPlusOperator() throws InterpreterException {
    String text = "+new Double(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: doubleValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Double) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }
  
  /**
   * Tests Minus Operator.
   */
  public void testMinusOperator() throws InterpreterException {
    String text = "-new Integer(10);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 10 10 int)]))";

    _checkUnaryExpression(text, expected);
  }

  /**
   * Tests Negating a Boolean.
   */
  public void testNegatingOneBoolean() throws InterpreterException {
    String text = "!new Boolean(false);";
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";

    _checkUnaryExpression(text, expected);
  }
  
  
  //////////// Other Operations //////////////////////
  
  public void testSimpleAssignBox() throws InterpreterException {
    String text = "B = true;";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: B)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Boolean) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.BooleanLiteral: true true boolean) (koala.dynamicjava.tree.BooleanTypeName: boolean))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignUnbox() throws InterpreterException {  
    String text = "b = new Boolean(false);";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: b)";
    String rightExpected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Boolean) [(koala.dynamicjava.tree.BooleanLiteral: false false boolean)]))";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignBoxInt() throws InterpreterException {
    String text = "X = 3 + 5;";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: X)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.AddExpression: (koala.dynamicjava.tree.IntegerLiteral: 3 3 int) (koala.dynamicjava.tree.IntegerLiteral: 5 5 int)) (koala.dynamicjava.tree.IntTypeName: int))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  public void testSimpleAssignBoxAddExp() throws InterpreterException {
    String text = "X = new Integer(1) + new Integer(3);";
    String leftExpected = "(koala.dynamicjava.tree.QualifiedName: X)";
    String rightExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.AddExpression: (koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)])) (koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 3 3 int)]))) (koala.dynamicjava.tree.IntTypeName: int))])";
    _checkBinaryExpression(text, leftExpected, rightExpected);
  }
  
  public void testVariableDeclaration()  throws InterpreterException {
    String text = "Integer i = 1;";
    String initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntTypeName: int))])";
    VariableDeclaration exp = (VariableDeclaration)_parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpretText(text);
    
    text = "Long L = (long)1;";
    initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Long) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.LongTypeName: long)) (koala.dynamicjava.tree.LongTypeName: long))])";
    exp = (VariableDeclaration)_parseCode(text).get(0);
    type = exp.acceptVisitor(_typeChecker);
    actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpretText(text);
    
    text = "Double D = 1.0;";
    initExpected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Double) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.DoubleLiteral: 1.0 1.0 double) (koala.dynamicjava.tree.DoubleTypeName: double))])";
    exp = (VariableDeclaration)_parseCode(text).get(0);
    type = exp.acceptVisitor(_typeChecker);
    actual = exp.getInitializer().toString();
    assertEquals("The initializer should have been boxed.", initExpected, actual);
    _interpretText(text);
  }
  
  public void testUnboxingCastExpression() throws InterpreterException{
    String text = "(int)new Integer(1);";
  
    Node exp = _parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the primitive type.", int.class, type);

    _interpretText(text);
  }
  
  public void testBoxingCastExpression() throws InterpreterException{
    String text = "(Integer)1;";
    Node exp = _parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the refrence type.", Integer.class, type);

    _interpretText(text);
  }
  
  public void testDoubleCastExpression() throws InterpreterException{
    String text = "(Byte)(byte)1;";
    Node exp = _parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    assertEquals("Should be the primitive type.", Byte.class, type);

    _interpretText(text);
  }
  
  public void testErroneousUnboxingCastExpression() throws InterpreterException{
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
  public void testConditionalExpressions() throws InterpreterException{
    
    // This test makes sure that the special case where one of the operands is
    // a Boolean reference type and the other is a primitive type works
    String text = "(B) ? B : 1;";
    ConditionalExpression stmt = (ConditionalExpression)_parseCode(text).get(0);
    Class<?> type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 1: the type should have been Object", Object.class, type);
    
    String expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    String actual = stmt.getConditionExpression().toString();
    assertEquals("Test 1: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.QualifiedName: B)";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 1: Should have left B alone", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntTypeName: int))])";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 1: Should have boxed the int", expected, actual);
    
    String definition = "Boolean B = Boolean.TRUE; ";
    _interpretText(text);
    
    // This test is to make sure that a boxing type will get unboxed
    // if the other operand is a numerical primitive type
    text = "(B) ? new Integer(1) : 1;";
    stmt = (ConditionalExpression)_parseCode(text).get(0);
    type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 2: the type should have been int", int.class, type);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    actual = stmt.getConditionExpression().toString();
    assertEquals("Test 2: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)]))";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 2: Should have unboxed the integer", expected, actual);
    
    expected = "(koala.dynamicjava.tree.IntegerLiteral: 1 1 int)";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 2: Should have left the int alone", expected, actual);
    
    _interpretText(text);
    
    // This test makes sure that both primitives are boxed in the case
    // that one is a boolean and the other is not.
    text = "(B) ? true : 1;";
    stmt = (ConditionalExpression)_parseCode(text).get(0);
    type = stmt.acceptVisitor(_typeChecker);
    assertEquals("Test 3: the type should have been int", Object.class, type);
    
    expected = "(koala.dynamicjava.tree.ObjectMethodCall: booleanValue null (koala.dynamicjava.tree.QualifiedName: B))";
    actual = stmt.getConditionExpression().toString();
    assertEquals("Test 3: Should have autounboxed", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Boolean) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.BooleanLiteral: true true boolean) (koala.dynamicjava.tree.BooleanTypeName: boolean))])";
    actual = stmt.getIfTrueExpression().toString();
    assertEquals("Test 3: Should have boxed the boolean", expected, actual);
    
    expected = "(koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: java.lang.Integer) [(koala.dynamicjava.tree.CastExpression: (koala.dynamicjava.tree.IntegerLiteral: 1 1 int) (koala.dynamicjava.tree.IntTypeName: int))])";
    actual = stmt.getIfFalseExpression().toString();
    assertEquals("Test 3: Should have boxed the int", expected, actual);
    
    _interpretText(text);
  }
  
  public void testArrayAllocation() throws InterpreterException {
    String text = "new int[new Integer(5)];";
    String sizeExpected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 5 5 int)]))";
    ArrayAllocation exp = (ArrayAllocation) _parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getSizes().get(0).toString();
    assertEquals("The size should have been unboxed.", sizeExpected, actual);
    _interpretText(text);
 
    text = "new int[new Long(0)];";
    exp = (ArrayAllocation) _parseCode(text).get(0);
    try {
      type = exp.acceptVisitor(_typeChecker);
      fail("Should have thrown an exception.");
    }
    catch (ExecutionError e) {
    }
  }
  
  public void testArrayAccess() throws InterpreterException {
    String text = "I[new Integer(0)];";
    String idxExpected = "(koala.dynamicjava.tree.ObjectMethodCall: intValue null (koala.dynamicjava.tree.SimpleAllocation: (koala.dynamicjava.tree.ReferenceTypeName: Integer) [(koala.dynamicjava.tree.IntegerLiteral: 0 0 int)]))";
    ArrayAccess exp = (ArrayAccess)_parseCode(text).get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    String actual = exp.getCellNumber().toString();
    assertEquals("The index should have been unboxed.", idxExpected, actual);
    _interpretText(text);
 
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
   * Tests a call on a method on an instance of an Integer
   */
  public void testObjectMethodCall() throws InterpreterException {
    String text = "X.equals(0);";
    List<Node> list = _parseCode(text);
    MethodCall exp = (MethodCall)list.get(0);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    Method m = (Method)exp.getProperty(NodeProperties.METHOD);
    assertEquals("the method's parameter type should have been int", Object.class, m.getParameterTypes()[0]);
    Object res = _interpretText(text);
  }
  
  /**
   * Method calls may or may not be a simple project.  We need to look into what
   * needs to be tested, the different types of method calls that must change,
   * what specifications have been added in the jsr language specs...
   */
  public void testStaticMethodCall() throws InterpreterException {
    String text = "Integer.toString(new Integer(1));";

    List<Node> list = _parseCode(text);
    NameVisitor nv = new NameVisitor(_globalNameContext);
    Node exp = (MethodCall)list.get(0);
    exp = exp.acceptVisitor(nv);
    Class<?> type = exp.acceptVisitor(_typeChecker);
    Method m = (Method)exp.getProperty(NodeProperties.METHOD);
    assertEquals("the method's parameter type should have been int", int.class, m.getParameterTypes()[0]);
    Object res = _interpretText(text);
  }
}
