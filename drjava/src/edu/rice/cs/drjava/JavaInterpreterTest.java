package edu.rice.cs.drjava;

import junit.framework.*;
import java.util.Vector;
import junit.extensions.*;

class Pair
{
    private String _first;
    private Object _second;

    Pair(String f, Object s) {
	this._first = f;
	this._second = s;
    }

    public static Pair make(String first, Object second) {
	return new Pair(first, second);
    }

    public String first() {
	return this._first;
    }

    public Object second() {
	return this._second;
    }
}


public class JavaInterpreterTest extends TestCase
{
    private JavaInterpreter _interpreter;

    public JavaInterpreterTest(String name)
    {
	super(name);
    }

    protected void setUp()
    {
	_interpreter = new DynamicJavaAdapter();
    }

    public static Test suite()
    {
	return new TestSuite(JavaInterpreterTest.class);
    }

	private void tester(Pair[] cases)
    {
			for (int i = 0; i < cases.length; i++)
				{
					Object out = _interpreter.interpret(cases[i].first());
					assertEquals(cases[i].first() + " interpretation wrong!",
											 cases[i].second(),
											 out);					
				}
    }

	
	/** Make sure interpreting simple constants works. */
	public void testConstants()
    {
			Pair[] cases = new Pair[] {	
				Pair.make("5", new Integer(5)),
		Pair.make("1356", new Integer(1356)),
		Pair.make("true", Boolean.TRUE),
		Pair.make("false", Boolean.FALSE),
		Pair.make("\'c\'", new Character('c')),
		Pair.make("1.345", new Double(1.345)),
		Pair.make("\"buwahahahaha!\"", new String("buwahahahaha!")),
		Pair.make("\"yah\\\"eh\\\"\"", new String("yah\"eh\"")),
		Pair.make("'\\''", new Character('\'')),
		};
	tester(cases);
    }

    /** Test simple operations with Booleans */
    public void testBooleanOps()
    {
	Pair[] cases = new Pair[] {	
	    //and
	    Pair.make("true && false", new Boolean(false)),
		Pair.make("true && true", new Boolean(true)),
		//or
		Pair.make("true || true", new Boolean(true)),
		Pair.make("false || true", new Boolean(true)),
		Pair.make("false || false", new Boolean(false)),
		// not
		Pair.make("!true", new Boolean(false)),
		Pair.make("!false", new Boolean(true)),
		//equals
		Pair.make("true == true", new Boolean(true)),
		Pair.make("false == true", new Boolean(false)),
		Pair.make("false == false", new Boolean(true)),
		// xor
		Pair.make("false ^ false", new Boolean(false ^ false)),
		Pair.make("false ^ true ", new Boolean(false ^ true ))
		};
	tester(cases);
    }

    public void testIntegerOps()
    {
	Pair[] cases = new Pair[] {
	    // plus
	    Pair.make("5+6", new Integer(5+6)),
		// minus
		Pair.make("6-5", new Integer(6-5)),
		// times
		Pair.make("6*5", new Integer(6*5)),
		// divide
		Pair.make("6/5", new Integer(6/5)),
		// modulo
		Pair.make("6%5", new Integer(6%5)),
		// bit and
		Pair.make("6&5", new Integer(6&5)),
		// bit or
		Pair.make("6 | 5", new Integer(6 | 5)),
		// bit xor
		Pair.make("6^5", new Integer(6^5)),
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
		Pair.make("4 <= 4", new Boolean(4 <= 4)),
		Pair.make("4 <= 5", new Boolean(4 <= 5)),
		// greater than
		Pair.make("5 > 4", new Boolean(5 > 4)),
		Pair.make("5 > 5", new Boolean(5 > 5)),
		// greater than or equal to
		Pair.make("5 >= 4", new Boolean(5 >= 4)),
		Pair.make("5 >= 5", new Boolean(5 >= 5)),
		// equal to
		Pair.make("5 == 5", new Boolean(5 == 5)),
		Pair.make("5 == 6", new Boolean(5 == 6)),
		// not equal to
		Pair.make("5 != 6", new Boolean(5 != 6)),
		Pair.make("5 != 5", new Boolean(5 != 5))
      };
	tester(cases);
    }

    public void testDoubleOps()
    {
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
		Pair.make("5.6 * 4.5", new Double(5.6 * 4.5)),
		// divide
		Pair.make("5.6 / 3.4", new Double(5.6 / 3.4)),
		// modulo
		Pair.make("5.6 % 3.4", new Double(5.6 % 3.4)),
		// plus
		Pair.make("5.6 + 6.7", new Double(5.6 + 6.7)),
		// minus
		Pair.make("4.5 - 3.4", new Double(4.5 - 3.4)),
		};
	tester(cases);
    }

	public void testStringOps()
    {
			Pair[] cases = new Pair[] {
				// concatenation
				Pair.make("\"yeah\" + \"and\"", new String("yeah" + "and")),
				// equals
				Pair.make("\"yeah\".equals(\"yeah\")", new Boolean("yeah".equals("yeah"))),
			};
			tester(cases);
    }
	
	public void testCharacterOps()
    {
			Pair[] cases = new Pair[] {
				// equals
				Pair.make("'c' == 'c'", new Boolean('c' == 'c'))		
			};
			tester(cases);
    }
	
	public void testSemicolon()
    {
			Pair[] cases = new Pair[] {
				Pair.make("'c' == 'c'", new Boolean('c' == 'c')),		
				Pair.make("'c' == 'c';", JavaInterpreter.NO_RESULT),
				Pair.make("String s = \"hello\"", null),
				Pair.make("String x = \"hello\";", JavaInterpreter.NO_RESULT),
				Pair.make("s", "hello"),
				Pair.make("s;", JavaInterpreter.NO_RESULT),
				Pair.make("x", "hello"),
				Pair.make("x;", JavaInterpreter.NO_RESULT)
			};
			tester(cases);
    }
}



