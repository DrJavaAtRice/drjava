package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;
import koala.dynamicjava.interpreter.error.ExecutionError;

public class CheckerException extends InterpreterException {
  
  public CheckerException(ExecutionError cause) {
    super(cause);
  }
  
  public void printUserMessage(PrintWriter out) {
    out.print("Static Error: ");
    out.println(getCause().getMessage());
  }
  
}
