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

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.DrJavaTestCase;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.*;
import edu.rice.cs.dynamicjava.interpreter.Interpreter;
import edu.rice.cs.dynamicjava.symbol.*;
import edu.rice.cs.dynamicjava.symbol.type.Type;

/** Tests the functionality of the repl interpreter.
  * @version $Id: JavaInterpreterTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class JavaInterpreterTest extends DrJavaTestCase {
  
  // ***************************
  // TODO: These are useful tests, but they don't belong here.  They're testing the
  // functionality of DynamicJava, and so should be packaged with other DynamicJava tests.
  // It's not DrJava's responsibility to comprehensively test the libraries it uses.
  //
  // True. Then put them somewhere else, don't just comment them out. --mgricken
  // ***************************
  
  private volatile InteractionsPaneOptions _interpreterOptions;
  private volatile Interpreter _interpreter;  
  private volatile ClassPathManager _classPathManager;
  private volatile ClassLoader _interpreterLoader;

  static public boolean testValue;

  /** The setup method run before each test. */
  protected void setUp() throws Exception {
    super.setUp();
//    _interpreter = new DynamicJavaAdapter(new ClassPathManager());
//    testValue = false;
    _classPathManager = new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
    _interpreterLoader = _classPathManager.makeClassLoader(null);
    
    // _interpreterOptions = Options.DEFAULT;
    _interpreterOptions = new InteractionsPaneOptions();
    _interpreter = new Interpreter(_interpreterOptions, _interpreterLoader);
  }

  /** Asserts that the results of interpreting the first of each
    * Pair is equal to the second.
    * @param cases an array of Pairs
    */
  private void tester(Pair<String,Object>[] cases) throws InterpreterException {
    for (int i = 0; i < cases.length; i++) {
      Object out = interpret(cases[i].first());
      assertEquals(cases[i].first() + " interpretation wrong!", cases[i].second(), out);
    }
  }
  
  private Object interpret(String s) throws InterpreterException {
    return _interpreter.interpret(s).apply(new OptionVisitor<Object, Object>() {
      public Object forNone() { return null; }
      public Object forSome(Object obj) { return obj; }
    });
  }

  /** Make sure interpreting simple constants works.
    * Note that strings and characters are quoted.
    */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testConstants() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      Pair.make("5", new Integer(5)),
        Pair.make("1356", new Integer(1356)),
        Pair.make("true", Boolean.TRUE),
        Pair.make("false", Boolean.FALSE),
        Pair.make("\'c\'", new Character('c')),
        Pair.make("1.345", new Double(1.345)),
        Pair.make("\"buwahahahaha!\"", "buwahahahaha!"),
        Pair.make("\"yah\\\"eh\\\"\"", "yah\"eh\""),
        Pair.make("'\\''", new Character('\''))
    };
    tester(cases);
  }

  /** Test simple operations with Booleans */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testBooleanOps() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      //and
      Pair.make("true && false", Boolean.FALSE), Pair.make("true && true",
          Boolean.TRUE),
      //or
      Pair.make("true || true", Boolean.TRUE), Pair.make("false || true", Boolean.TRUE),
          Pair.make("false || false", Boolean.FALSE),
      // not
      Pair.make("!true", Boolean.FALSE), Pair.make("!false", Boolean.TRUE),
          //equals
      Pair.make("true == true", Boolean.TRUE), Pair.make("false == true", Boolean.FALSE),
          Pair.make("false == false", Boolean.TRUE),
      // xor
      Pair.make("false ^ false", Boolean.valueOf(false ^ false)), Pair.make("false ^ true ",
          Boolean.valueOf(false ^ true))
    };
    tester(cases);
  }

  /** Tests short circuiting */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testShortCircuit() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      Pair.make("false && (3 == 1/0)", Boolean.FALSE),
      Pair.make("true || (1/0 != 43)", Boolean.TRUE)
    };
    tester(cases);
  }

  /** Tests integer operations. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testIntegerOps() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      // plus
      Pair.make("5+6", new Integer(5 + 6)),
      // minus
      Pair.make("6-5", new Integer(6 - 5)),
      // times
      Pair.make("6*5", new Integer(6*5)),
      // divide
      Pair.make("6/5", new Integer(6/5)),
      // modulo
      Pair.make("6%5", new Integer(6%5)),
      // bit and
      Pair.make("6&5", new Integer(6 & 5)),
      // bit or
      Pair.make("6 | 5", new Integer(6 | 5)),
      // bit xor
      Pair.make("6^5", new Integer(6 ^ 5)),
      // bit complement
      Pair.make("~6", new Integer(~6)),
      // unary plus
      Pair.make(" + 5", new Integer(+5)),
      // unary minus
      Pair.make("-5", new Integer(-5)),
      // left shift
      Pair.make("400 << 5", new Integer(400 << 5)),
      // right shift
      Pair.make("400 >> 5", new Integer(400 >> 5)),
      // unsigned right shift
      Pair.make("400 >>> 5", new Integer(400 >>> 5)),
      // less than
      Pair.make("5 < 4", Boolean.valueOf(5 < 4)),
      // less than or equal to
      Pair.make("4 <= 4", Boolean.valueOf(4 <= 4)), Pair.make("4 <= 5", Boolean.valueOf(4 <= 5)),
          // greater than
      Pair.make("5 > 4", Boolean.valueOf(5 > 4)), Pair.make("5 > 5", Boolean.valueOf(5 > 5)),
          // greater than or equal to
      Pair.make("5 >= 4", Boolean.valueOf(5 >= 4)), Pair.make("5 >= 5", Boolean.valueOf(5 >= 5)),
          // equal to
      Pair.make("5 == 5", Boolean.valueOf(5 == 5)), Pair.make("5 == 6", Boolean.valueOf(
          5 == 6)),
      // not equal to
      Pair.make("5 != 6", Boolean.valueOf(5 != 6)), Pair.make("5 != 5", Boolean.valueOf(5 != 5))
    };
    tester(cases);
  }

  /**
   * Test double operations.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testDoubleOps() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      // less than
      Pair.make("5.6 < 6.7", Boolean.valueOf(5.6 < 6.7)),
      // less than or equal to
      Pair.make("5.6 <= 5.6", Boolean.valueOf(5.6 <= 5.6)),
      // greater than
      Pair.make("5.6 > 4.5", Boolean.valueOf(5.6 > 4.5)),
      // greater than or equal to
      Pair.make("5.6 >= 56.4", Boolean.valueOf(5.6 >= 56.4)),
      // equal to
      Pair.make("5.4 == 5.4", Boolean.valueOf(5 == 5)),
      // not equal to
      Pair.make("5.5 != 5.5", Boolean.valueOf(5 != 5)),
      // unary plus
      Pair.make(" + 5.6", new Double(+5.6)),
      // unary minus
      Pair.make("-5.6", new Double(-5.6)),
      // times
      Pair.make("5.6 * 4.5", new Double(5.6*4.5)),
      // divide
      Pair.make("5.6 / 3.4", new Double(5.6/3.4)),
      // modulo
      Pair.make("5.6 % 3.4", new Double(5.6%3.4)),
      // plus
      Pair.make("5.6 + 6.7", new Double(5.6 + 6.7)),
      // minus
      Pair.make("4.5 - 3.4", new Double(4.5 - 3.4)),
    };
    tester(cases);
  }

  /**
   * Test string operations
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testStringOps() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      // concatenation
      Pair.make("\"yeah\" + \"and\"", "yeah" + "and"),
      // equals
      Pair.make("\"yeah\".equals(\"yeah\")", Boolean.valueOf("yeah".equals("yeah"))),

    };
    tester(cases);
  }

  /**
   * Test character operations.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testCharacterOps()  throws InterpreterException{
    Pair<String,Object>[] cases = new Pair[] {
      // equals
      Pair.make("'c' == 'c'", Boolean.valueOf('c' == 'c'))
    };
    tester(cases);
  }

  /**
   * Tests that String and character declarations do not return
   * a result, while the variables themselves return a quoted result.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testSemicolon() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      Pair.make("'c' == 'c'", Boolean.valueOf('c' == 'c')),
      Pair.make("'c' == 'c';", null),
      Pair.make("String s = \"hello\"", null),
      Pair.make("String x = \"hello\";", null),
      Pair.make("char c = 'c'", null),
      Pair.make("Character d = new Character('d')", null),
      Pair.make("s", "hello"), Pair.make("s;", null),
      Pair.make("x", "hello"), Pair.make("x;", null),
      Pair.make("c", 'c'), Pair.make("d", 'd')
    };
    tester(cases);
  }

  /**
   * Tests that null can be used in instanceof expressions.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testNullInstanceOf() throws InterpreterException {
    Pair<String,Object>[] cases = new Pair[] {
      Pair.make("null instanceof Object", Boolean.valueOf(null instanceof Object)),
      Pair.make("null instanceof String", Boolean.valueOf(null instanceof String))
    };
    tester(cases);
  }

  /**
   * Tests simple variable definitions which broke the initial implementation
   * of variable redefinition (tested by testVariableRedefinition).
   */
  @SuppressWarnings("unchecked")
  public void testVariableDefinition() throws InterpreterException {
    _interpreter.interpret("int a = 5;");
    _interpreter.interpret("int b = a;");

    _interpreter.interpret("int c = a++;");
  }

  /**
   * Tests that variables are assigned default values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testVariableDefaultValues() throws InterpreterException {
    _interpreter.interpret("byte b");
    _interpreter.interpret("short s");
    _interpreter.interpret("int i");
    _interpreter.interpret("long l");
    _interpreter.interpret("float f");
    _interpreter.interpret("double d");
    _interpreter.interpret("char c");
    _interpreter.interpret("boolean bool");
    _interpreter.interpret("String str");
    Pair<String,Object>[] cases = new Pair[] {
      Pair.make("b", new Byte((byte)0)),
      Pair.make("s", new Short((short)0)),
      Pair.make("i", new Integer(0)),
      Pair.make("l", new Long(0L)),
      Pair.make("f", new Float(0.0f)),
      Pair.make("d", new Double(0.0d)),
      Pair.make("c", new Character('\u0000')),
      Pair.make("bool", Boolean.valueOf(false)),
      Pair.make("str", null)
    };
    tester(cases);
  }

  /**
   * Tests that variable declarations with errors will not allow the interpreter
   * to not define the variable. This will get rid of annoying "Error:
   * Redefinition of 'variable'" messages after fixing the error. Note that if
   * the error occurs during the evaluation of the right hand side then the
   * variable is defined. This is for two reasons: The compiler would have
   * accepted this variable declaration so that no more variables could have
   * been defined with the same name afterwards, and we don't know how to make
   * sure the evaluation doesn't return errors without actually evaluating which
   * may have side-effects.
   */
  public void testVariableRedefinition() throws InterpreterException {
    // test error in NameVisitor
    try {
      _interpreter.interpret("String s = abc;");
      fail("variable definition should have failed");
    }
    catch (InterpreterException e) {
      // Correct; it should fail
    }
    // test error in TypeChecker
    try {
      _interpreter.interpret("Vector v = new Vector();");
      fail("variable definition should have failed");
    }
    catch (InterpreterException e) {
      // Correct; it should fail
    }
    try {
      _interpreter.interpret("File f;");
      fail("variable definition should have failed");
    }
    catch (InterpreterException e) {
      // Correct; it should fail
    }
    try {
      // make sure we can redefine
      _interpreter.interpret("import java.util.Vector;");
      _interpreter.interpret("Vector v = new Vector();");
      _interpreter.interpret("String s = \"abc\";");
      _interpreter.interpret("import java.io.File;");
      _interpreter.interpret("File f = new File(\"\");");
    }
    catch (InterpreterException e) {
      fail("These interpret statements shouldn't cause errors");
    }
    // test error in EvaluationVisitor

    // Integer.getInteger("somebadproperty") should be null
    try {
      _interpreter.interpret("String z = new String(Integer.getInteger(\"somebadproperty\").toString());");
      fail("variable definition should have failed");
    }
    catch (InterpreterException e) {
    }
    // The DynamcjavaAdapter should have undone the binding made
    // for "z" when the first definition fails.  Defining it again
    // should work.
    _interpreter.interpret("String z = \"z\";");
    
  }

  /** Ensure that the interpreter rejects assignments where the right type is not a subclass of the left type. */
  public void testIncompatibleAssignment() throws InterpreterException {
    try {
      _interpreter.interpret("Integer i = new Object()");
      fail("incompatible assignment should have failed");
    }
    catch (InterpreterException e) {
      // Correct; it should fail
    }
    try {
      _interpreter.interpret("Integer i2 = (Integer)new Object();");
      fail("incompatible assignment should have failed");
    }
    catch (InterpreterException e) {
      // Correct; it should fail
    }

    // Check that a correct assignment doesn't fail
    _interpreter.interpret("Object o = new Integer(3)");
  }

 /** Test the operation of the TypeCheckerExtension by performing the operations ((false) ? 2/0 : 1) and 
   * ((false) ? 2%0 : 1), which should not throw Exceptions in the Java interpreter.
   */
  public void testTypeCheckerExtension() {
    try { _interpreter.interpret("(false) ? 2/0 : 1 "); }
    catch(InterpreterException e) {
      if ( e.getCause() instanceof ArithmeticException ) {
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }

    try { _interpreter.interpret("(false) ? 2%0 : 1 "); }
    catch(InterpreterException e) {
      if ( e.getCause() instanceof ArithmeticException ) {
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }
  }

  /** Test the operation of the EvaluationVisitorExtension by performing a computation with no results (interpreter
    * should return NO_RESULT and not null)
   */
  public void testEvaluationVisitorExtensionNO_RESULT() {
    try {
      Object out = interpret("true;");
      assertEquals("testEvaluationVisitorExtension", null, out);
    }
    catch(InterpreterException e) {
      fail("testEvaluationVisitorExtension Exception returned for none exceptional code!" + e);
    }
  }

//  /** Test that a variable can be defined in the interpreter by an external source. */
//  public void testDefineVariableExternally() throws InterpreterException {
//    _interpreter.defineVariable("foo", "hello");
//    assertEquals("manipulated externally defined variable",
//                 "\"ello\"", _interpreter.interpret("foo.substring(1,5)"));
//    _interpreter.defineVariable("x", 3);
//    assertEquals("externally defined variable x",
//                 new Integer(3), _interpreter.interpret("x"));
//    assertEquals("incremented externally defined variable x",
//                 new Integer(4), _interpreter.interpret(" + +x"));
//  }

//  /** Test that the value of a variable can be queried externally. */
//  public void testQueryVariableExternally() {
//    _interpreter.defineVariable("x", 7);
//    // Get value of variable externally
//    assertEquals("external query for x",
//                 new Integer(7), _interpreter.getVariable("x"));
//
//    // Undefined variable
//    try {
//      _interpreter.getVariable("undefined");
//      fail("Should have thrown IllegalStateException");
//    }
//    catch (IllegalStateException e) {
//      // good, that's what we want
//    }
//  }

//  /** Test that a constant can be defined in the interpreter by an external source. */
//  public void testDefineConstantExternally() {
//    _interpreter.defineConstant("y", 3);
//    try {
//      _interpreter.interpret("y = 4");
//      fail("should not be able to assign to a constant");
//    }
//    catch (InterpreterException e) {
//      // correct, it should fail
//    }
//  }

  /** Test that arrays initializers are accepted. */
  public void testInitializeArrays() throws InterpreterException {
    try {
      _interpreter.interpret("int i[] = new int[]{1,2,3};");
      _interpreter.interpret("int j[][] = new int[][]{{1}, {2,3}};");
      _interpreter.interpret("int k[][][][] = new int[][][][]{{{{1},{2,3}}}};");
    }
    catch(IllegalArgumentException iae) {
      fail("Legal array initializations were not accepted.");
    }
  }

  /** Test that array cloning works. */
  public void testArrayCloning() throws InterpreterException {
    try { _interpreter.interpret("new int[]{0}.clone()"); }
    catch(RuntimeException e) { fail("Array cloning failed."); }
  }
  
//  /** Test that the Interactions Pane will or won't allow access to private members
//    * given the value of the ALLOW_PRIVATE_ACCESS configuration option.
//    */
//  public void testAllowPrivateAccess() throws InterpreterException {
//    // The real option listener is in DefaultGlobalModel, so add one here.
//    DrJava.getConfig().addOptionListener(OptionConstants.ALLOW_PRIVATE_ACCESS, new OptionListener<Boolean>() {
//      public void optionChanged(OptionEvent<Boolean> oce) {
//        _interpreter.setPrivateAccessible(oce.value.booleanValue());
//      }
//    });
//    DrJava.getConfig().setSetting(OptionConstants.ALLOW_PRIVATE_ACCESS, Boolean.valueOf(false));
//    Utilities.clearEventQueue();
////    System.err.println("\nPrivate Access = " + _interpreter.getPrivateAccessible());
//    try {
//      _interpreter.interpret("class A { private int i = 0; }");
//      _interpreter.interpret("new A().i");
//      System.out.println("Private access erroneously succeeded");
//      fail("Should not have access to the private field i inside class A.");
//    }
//    catch (InterpreterException ere) {
//      assertTrue(ere.getContainedException() instanceof IllegalAccessException);
//    }
//    DrJava.getConfig().setSetting(OptionConstants.ALLOW_PRIVATE_ACCESS, Boolean.valueOf(true));
//    Utilities.clearEventQueue();
//    assertEquals("Should be able to access private field i whose value should be 0",
//                 new Integer(0),
//                 _interpreter.interpret("new A().i"));
//  }

  /**
   * Tests that declaring a void method in the Interactions Pane won't cause a bad type
   * exception. Tests bug #915906 "Methods in Interactions no longer work".
   */
  public void testDeclareVoidMethod() {
    try { _interpreter.interpret("void method() {}"); }
    catch (InterpreterException ere) { fail("Should be able to declare void methods."); }
  }

  /**
   * Tests that a call to user-defined void method returns NO_RESULT, instead of null.
   * This test does not pass, it is currently broken.
   */
  public void testUserDefinedVoidMethod() throws InterpreterException {
     Object result = interpret("public void foo() {}; foo()");
     assertSame("Should have returned NO_RESULT.", null, result);
   }
  
  /** Test throwing null, for bug 3008828. */
  public void testThrowNull() throws InterpreterException {
    try {
      _interpreter.interpret("throw null");
      fail("Should have thrown an EvaluatorException with a NullPointerException as cause.");
    }
    catch(Throwable t) {
      if ((t == null) || (!(t instanceof EvaluatorException))) {
        fail("Should have thrown an EvaluatorException with a NullPointerException as cause.");
      }
      else {
        Throwable cause = t.getCause();
        if (!(cause instanceof NullPointerException)) {
          fail("Should have thrown an EvaluatorException with a NullPointerException as cause.");
        }
      }
    }
  }

}
