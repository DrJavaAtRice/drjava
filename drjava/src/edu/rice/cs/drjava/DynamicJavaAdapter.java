/* $Id$ */

package edu.rice.cs.drjava;

import java.io.StringReader;

import koala.dynamicjava.interpreter.Interpreter;
import koala.dynamicjava.interpreter.InterpreterException;
import koala.dynamicjava.interpreter.TreeInterpreter;

import koala.dynamicjava.parser.wrapper.JavaCCParserFactory;

public class DynamicJavaAdapter implements JavaInterpreter {
  private Interpreter _djInterpreter;

  public DynamicJavaAdapter() {
    _djInterpreter = new TreeInterpreter(new JavaCCParserFactory());
  }

  public Object interpret(String s) {

		boolean print = false;
		
		/**
		 * trims the whitespace from beginning and end of string
		 * checks the end to see if it is a semicolon
		 * adds a semicolon if necessary
		 */
		s = s.trim();

		if(!s.endsWith(";")) {
			s += ";";
			print = true;
		}
		
		StringReader reader = new StringReader(s);

    try {
			Object result = _djInterpreter.interpret(reader, "DrJava");
			if(print)
				return result;
			else
				return JavaInterpreter.NO_RESULT;
    }
    catch (InterpreterException ie) {
      throw new RuntimeException(ie.getMessage());
    }
  }

  public void addClassPath(String path) {
    _djInterpreter.addClassPath(path);
		System.out.println(_djInterpreter.getClassNames());
  }

}



