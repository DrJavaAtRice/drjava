package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class InterpreterException extends RuntimeException {
  
  public InterpreterException() {}
  
  public InterpreterException(Throwable cause) {
    super(cause);
  }
  
  public void printUserMessage() { printUserMessage(new PrintWriter(System.err, true)); }
  
  public abstract void printUserMessage(PrintWriter out);
  
  public String getUserMessage() {
    StringWriter s = new StringWriter();
    printUserMessage(new PrintWriter(s, true));
    return s.toString();
  } 
}
