/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import  junit.framework.*;
import  java.util.Vector;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import  java.lang.RuntimeException;
import  java.io.File;


/**
 * Tests the functionality of the repl interpreter.
 * @version $Id$
 */
public final class JavaInterpreterTest extends TestCase {
  private JavaInterpreter _interpreter;
  static public boolean testValue;

  /**
   * put your documentation comment here
   * @param     String name
   */
  public JavaInterpreterTest(String name) {
    super(name);
    testValue = false;
  }

  /**
   * put your documentation comment here
   */
  protected void setUp() {
    _interpreter = new DynamicJavaAdapter();
    testValue = false;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(JavaInterpreterTest.class);
  }

  /**
   * put your documentation comment here
   * @param cases
   */
  private void tester(Pair[] cases) throws ExceptionReturnedException {
    for (int i = 0; i < cases.length; i++) {
      Object out = _interpreter.interpret(cases[i].first());
      assertEquals(cases[i].first() + " interpretation wrong!", cases[i].second(), 
          out);
    }
  }

  /** 
   * Make sure interpreting simple constants works.
   * Note that strings and characters are quoted.
   */
  public void testConstants() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("5", new Integer(5)), Pair.make("1356", new Integer(1356)), Pair.make("true", 
          Boolean.TRUE), Pair.make("false", Boolean.FALSE), Pair.make("\'c\'", "'" + new Character('c') + "'"), 
          Pair.make("1.345", new Double(1.345)), Pair.make("\"buwahahahaha!\"", 
          new String("\"buwahahahaha!\"")), Pair.make("\"yah\\\"eh\\\"\"", new String("\"yah\"eh\"\"")), 
          Pair.make("'\\''", "'" + new Character('\'') + "'"), 
    };
    tester(cases);
  }

  /** Test simple operations with Booleans */
  public void testBooleanOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
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
      Pair.make("false ^ false", new Boolean(false ^ false)), Pair.make("false ^ true ", 
          new Boolean(false ^ true))
    };
    tester(cases);
  }
  
  /** Tests short circuiting */
  public void testShortCircuit() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("false && (3 == 1/0)", Boolean.FALSE),
        Pair.make("true || (1/0 != 43)", Boolean.TRUE)        
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testIntegerOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
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
      Pair.make("+5", new Integer(+5)), 
      // unary minus
      Pair.make("-5", new Integer(-5)), 
      // left shift
      Pair.make("400 << 5", new Integer(400 << 5)), 
      // right shift
      Pair.make("400 >> 5", new Integer(400 >> 5)), 
      // unsigned right shift
      Pair.make("400 >>> 5", new Integer(400 >>> 5)), 
      // less than
      Pair.make("5 < 4", new Boolean(5 < 4)), 
      // less than or equal to
      Pair.make("4 <= 4", new Boolean(4 <= 4)), Pair.make("4 <= 5", new Boolean(4 <= 5)), 
          // greater than
      Pair.make("5 > 4", new Boolean(5 > 4)), Pair.make("5 > 5", new Boolean(5 > 5)), 
          // greater than or equal to
      Pair.make("5 >= 4", new Boolean(5 >= 4)), Pair.make("5 >= 5", new Boolean(5 >= 5)), 
          // equal to
      Pair.make("5 == 5", new Boolean(5 == 5)), Pair.make("5 == 6", new Boolean(
          5 == 6)), 
      // not equal to
      Pair.make("5 != 6", new Boolean(5 != 6)), Pair.make("5 != 5", new Boolean(
          5 != 5))
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testDoubleOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      // less than
      Pair.make("5.6 < 6.7", new Boolean(5.6 < 6.7)), 
      // less than or equal to
      Pair.make("5.6 <= 5.6", new Boolean(5.6 <= 5.6)), 
      // greater than
      Pair.make("5.6 > 4.5", new Boolean(5.6 > 4.5)), 
      // greater than or equal to
      Pair.make("5.6 >= 56.4", new Boolean(5.6 >= 56.4)), 
      // equal to
      Pair.make("5.4 == 5.4", new Boolean(5 == 5)), 
      // not equal to
      Pair.make("5.5 != 5.5", new Boolean(5 != 5)), 
      // unary plus
      Pair.make("+5.6", new Double(+5.6)), 
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
   * put your documentation comment here
   */
  public void testStringOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      // concatenation
      Pair.make("\"yeah\" + \"and\"", new String("\"yeah" + "and\"")), 
      // equals
      Pair.make("\"yeah\".equals(\"yeah\")", new Boolean("yeah".equals("yeah"))), 
    
    };
    tester(cases);
  }

  /**
   * put your documentation comment here
   */
  public void testCharacterOps()  throws ExceptionReturnedException{
    Pair[] cases = new Pair[] {
      // equals
      Pair.make("'c' == 'c'", new Boolean('c' == 'c'))
    };
    tester(cases);
  }

  /**
   * Tests that String and character declarations do not return
   * a result, while the variables themselves return a quoted result.
   */
  public void testSemicolon() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("'c' == 'c'", new Boolean('c' == 'c')), 
      Pair.make("'c' == 'c';", JavaInterpreter.NO_RESULT), 
      Pair.make("String s = \"hello\"", JavaInterpreter.NO_RESULT), 
      Pair.make("String x = \"hello\";", JavaInterpreter.NO_RESULT), 
      Pair.make("char c = 'c'", JavaInterpreter.NO_RESULT),
      Pair.make("Character d = new Character('d')", JavaInterpreter.NO_RESULT),
      Pair.make("s", "\"hello\""), Pair.make("s;", JavaInterpreter.NO_RESULT), 
      Pair.make("x", "\"hello\""), Pair.make("x;", JavaInterpreter.NO_RESULT),
      Pair.make("c", "'c'"), Pair.make("d", "'d'")
    };
    tester(cases);
  }
  
  /**
   * Ensure that the interpreter rejects assignments where the right type
   * is not a subclass of the left type.
   */
  public void testIncompatibleAssignment() throws ExceptionReturnedException {
    Object out = null;
    try {
      out = _interpreter.interpret("Integer i = new Object()");
      fail("incompatible assignment should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    try {
      out = _interpreter.interpret("Integer i2 = (Integer)new Object();");
      fail("incompatible assignment should have failed");
    }
    catch (ExceptionReturnedException e) {
      // Correct; it should fail
    }
    
    // Check that a correct assignment doesn't fail
    _interpreter.interpret("Object o = new Integer(3)");
  }

 /**
  * Tests the operation of the TypeCheckerExtension by performing the 
  * operations ((false) ? 2/0 : 1) and ((false) ? 2%0 : 1), which should 
  * not throw Exceptions in the Java interpreter.
  */
  public void testTypeCheckerExtension() {
    Object out = null;
    
    try{
      out = _interpreter.interpret("(false) ? 2/0 : 1 ");
    }
    catch(ExceptionReturnedException e){
      if( e.getContainedException() instanceof ArithmeticException ){
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }
    
    try{
      out = _interpreter.interpret("(false) ? 2%0 : 1 ");
    }
    catch(ExceptionReturnedException e){
      if( e.getContainedException() instanceof ArithmeticException ){
        fail("testTypeCheckerExtension failed to prevent short circuit DivideByZeroException");
      }
    }
  }
  
  /**
  * Tests the operation of the EvaluationVisitorExtension by 
  * Performing a computation with no results (interpreter 
  * should return NO_RESULT and not null)
  */
  public void testEvaluationVisitorExtensionNO_RESULT() {   
    boolean passed = false;
    
    try{
      Object out = _interpreter.interpret("true;");
      assertEquals("testEvaluationVisitorExtension", JavaInterpreter.NO_RESULT, out);
    }
    catch(ExceptionReturnedException e){
      fail("testEvaluationVisitorExtension Exception returned for none exceptional code!" + e);
    }
  }
  
  /**
   * Tests that a variable can be defined in the interpreter by an external source.
   */
  public void testDefineVariableExternally() throws ExceptionReturnedException {
    _interpreter.defineVariable("foo", new String("hello"));
    assertEquals("manipulated externally defined variable",
                 "\"ello\"", _interpreter.interpret("foo.substring(1,5)"));
    _interpreter.defineVariable("x", 3);
    assertEquals("externally defined variable x",
                 new Integer(3), _interpreter.interpret("x"));
    assertEquals("incremented externally defined variable x",
                 new Integer(4), _interpreter.interpret("++x"));
  }
  
  /**
   * Tests that the value of a variable can be queried externally.
   */
  public void testQueryVariableExternally() throws ExceptionReturnedException {
    _interpreter.defineVariable("x", 7);
    // Get value of variable externally
    assertEquals("external query for x",
                 new Integer(7), _interpreter.getVariable("x"));
    
    // Undefined variable
    try {
      Object o = _interpreter.getVariable("undefined");
      fail("Should have thrown IllegalStateException");
    }
    catch (IllegalStateException e) {
      // good, that's what we want
    }
  }
  
  /**
   * Tests that a constant can be defined in the interpreter by an external source.
   */
  public void testDefineConstantExternally() throws ExceptionReturnedException {
    _interpreter.defineConstant("y", 3);
    try {
      _interpreter.interpret("y = 4");
      fail("should not be able to assign to a constant");
    }
    catch (ExceptionReturnedException e) {
      // correct, it should fail
    }
  }
  
  /**
   * Tests that a call to user-defined void method returns NO_RESULT, instead of null.
   * This test does not pass, it is currently broken.
   */
//   public void testUserDefinedVoidMethod() throws ExceptionReturnedException {
//     Object result = _interpreter.interpret("public void foo() {}; foo()");
//     assertSame("Should have returned NO_RESULT.", Interpreter.NO_RESULT, result);
//   }
}

/**
 * A structure to contain a String and an Object pair.
 *  This class is used to help test the JavaInterpreter.
 */
class Pair {
  private String _first;
  private Object _second;

  /**
   * put your documentation comment here
   * @param     String f
   * @param     Object s
   */
  Pair(String f, Object s) {
    this._first = f;
    this._second = s;
  }

  /**
   * put your documentation comment here
   * @param first
   * @param second
   * @return 
   */
  public static Pair make(String first, Object second) {
    return  new Pair(first, second);
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String first() {
    return  this._first;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public Object second() {
    return  this._second;
  }
}



