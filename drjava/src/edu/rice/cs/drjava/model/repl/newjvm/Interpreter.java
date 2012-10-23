package edu.rice.cs.drjava.model.repl.newjvm;

import edu.rice.cs.dynamicjava.interpreter.InterpreterException;

/** Definition of the basic interface which any DrXXX interpreter must implement. */
public interface Interpreter {
  String interpret(String input) throws InterpreterException;
  void start();
  void addCP(String pathType, String path);
  void reset();
  // (void | boolean) cd();  (future extension)
}
