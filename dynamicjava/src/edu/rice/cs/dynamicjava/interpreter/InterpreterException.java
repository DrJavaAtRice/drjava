package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;

public abstract class InterpreterException extends Exception {
  
  public InterpreterException(Throwable cause) {
    super(cause);
  }
  
  public void printUserMessage() { printUserMessage(new PrintWriter(System.err, true)); }
  
  public abstract void printUserMessage(PrintWriter out);
  
}
