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
    StringReader reader = new StringReader(s);

    try {
      Object result = _djInterpreter.interpret(reader, "DrJava");
      return result;
    }
    catch (InterpreterException ie) {
      throw new RuntimeException(ie.getMessage());
    }
  }

  public void addClassPath(String path) {
    _djInterpreter.addClassPath(path);
  }
}
