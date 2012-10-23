package edu.rice.cs.drjava.model.repl.newjvm;

/** This defines the interface for an adapter from an 'Interpreter' class
 * (i.e. a class which implements the 'Interpreter' interface in this package)
 * to a Scala REPL. In theory, this interface should be invariant across 
 * subsequent Scala versions.
 */
public interface ScalaInterpreterAdapter {
  void addClasspath(String path);
  void reset();
  // (String?) interpret(String toInterpret) ... optional major refactoring
}
