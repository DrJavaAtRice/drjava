package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;
import koala.dynamicjava.parser.wrapper.ParseError;

public class ParserException extends InterpreterException {
  
  public ParserException(ParseError cause) {
    super(cause);
  }
  
  public void printUserMessage(PrintWriter out) {
    String message = getCause().getMessage();
    if (!message.startsWith("Syntax Error:")) { message = "Parse Error: " + message; }
    out.println(getCause().getMessage());
  }
  
}
