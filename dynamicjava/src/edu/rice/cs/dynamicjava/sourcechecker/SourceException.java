package edu.rice.cs.dynamicjava.sourcechecker;

import java.io.IOException;
import java.io.PrintWriter;

import edu.rice.cs.dynamicjava.interpreter.InterpreterException;

/** Represents a file system-level problem that occurs in accessing a source file. */
public class SourceException extends InterpreterException {
  
  public SourceException(IOException e) { super(e); }

  public void printUserMessage(PrintWriter out) {
    getCause().printStackTrace(out);
  }

}
