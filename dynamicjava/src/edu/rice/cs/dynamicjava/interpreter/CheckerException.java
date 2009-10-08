package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;

import edu.rice.cs.plt.lambda.Lambda;
import koala.dynamicjava.interpreter.error.ExecutionError;
import koala.dynamicjava.tree.SourceInfo;

public class CheckerException extends InterpreterException implements SourceInfo.Wrapper {
  
  public CheckerException(ExecutionError cause) {
    super(cause);
  }
  
  public void printUserMessage(PrintWriter out) {
    out.print("Static Error: ");
    out.println(getCause().getMessage());
  }
  
  public SourceInfo getSourceInfo() {
    return ((ExecutionError) getCause()).getNode().getSourceInfo();
  }
  
  public static final Lambda<ExecutionError, CheckerException> FACTORY =
      new Lambda<ExecutionError, CheckerException>() {
        public CheckerException value(ExecutionError e) { return new CheckerException(e); }
      };
  
}
