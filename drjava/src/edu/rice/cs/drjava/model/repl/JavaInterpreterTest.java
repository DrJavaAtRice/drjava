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
public class JavaInterpreterTest extends TestCase {
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

  /** Make sure interpreting simple constants works. */
  public void testConstants() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("5", new Integer(5)), Pair.make("1356", new Integer(1356)), Pair.make("true", 
          Boolean.TRUE), Pair.make("false", Boolean.FALSE), Pair.make("\'c\'", new Character('c')), 
          Pair.make("1.345", new Double(1.345)), Pair.make("\"buwahahahaha!\"", 
          new String("buwahahahaha!")), Pair.make("\"yah\\\"eh\\\"\"", new String("yah\"eh\"")), 
          Pair.make("'\\''", new Character('\'')), 
    };
    tester(cases);
  }

  /** Test simple operations with Booleans */
  public void testBooleanOps() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      //and
      Pair.make("true && false", new Boolean(false)), Pair.make("true && true", 
          new Boolean(true)), 
      //or
      Pair.make("true || true", new Boolean(true)), Pair.make("false || true", new Boolean(true)), 
          Pair.make("false || false", new Boolean(false)), 
      // not
      Pair.make("!true", new Boolean(false)), Pair.make("!false", new Boolean(true)), 
          //equals
      Pair.make("true == true", new Boolean(true)), Pair.make("false == true", new Boolean(false)), 
          Pair.make("false == false", new Boolean(true)), 
      // xor
      Pair.make("false ^ false", new Boolean(false ^ false)), Pair.make("false ^ true ", 
          new Boolean(false ^ true))
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
      Pair.make("\"yeah\" + \"and\"", new String("yeah" + "and")), 
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
   * put your documentation comment here
   */
  public void testSemicolon() throws ExceptionReturnedException {
    Pair[] cases = new Pair[] {
      Pair.make("'c' == 'c'", new Boolean('c' == 'c')), 
      Pair.make("'c' == 'c';", JavaInterpreter.NO_RESULT), 
      Pair.make("String s = \"hello\"", JavaInterpreter.NO_RESULT), 
      Pair.make("String x = \"hello\";", JavaInterpreter.NO_RESULT), 
      Pair.make("s", "hello"), Pair.make("s;", JavaInterpreter.NO_RESULT), 
      Pair.make("x", "hello"), Pair.make("x;", JavaInterpreter.NO_RESULT)
    };
    tester(cases);
  }

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



