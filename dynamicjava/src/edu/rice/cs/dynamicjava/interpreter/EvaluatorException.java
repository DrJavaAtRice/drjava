package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;
import java.util.Iterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.ReadOnceIterable;
import edu.rice.cs.plt.lambda.Lambda;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

public class EvaluatorException extends InterpreterException {
  
  public EvaluatorException(Throwable cause) {
    super(cause);
    updateAllStacks(cause, new String[0][]);
  }
  
  public EvaluatorException(Throwable cause, String... extraStackElements) {
    super(cause);
    updateAllStacks(cause, new String[][]{ extraStackElements });
  }
  
  /**
   * Modifies the stack trace of {@code cause} so that the calling context shared between
   * {@code cause} and the method calling this constructor is eliminated.  Additional elements
   * may be removed as well by explicitly listing them as a subarray of {@code extraStackElements}.
   * If a mismatch is found between the two calling contexts, no further filtering occurs --
   * thus, {@code extraStackElements} will not be matched unless this constructor is called
   * from a method that is part of {@code cause}'s calling context.
   * 
   * @param cause  An exception that should be presented to the user.
   * @param extraStackElements  Any number of alternatives for additional stack elements that should be 
   *                            removed from {@code cause}'s calling context.  The format for each string is
   *                            a class name followed by a method name, and delimited by a period:
   *                            {@code "java.lang.String.substring"}.  The order is bottom-to-top -- the 
   *                            first method in each subarray is expected to have invoked the second method,
   *                            etc.
   */
  public EvaluatorException(Throwable cause, String[]... extraStackElements) {
    super(cause);
    updateAllStacks(cause, extraStackElements);
  }
  
  /** Eliminate matching stack elements in {@code cause} and all of its chained causes. */
  private void updateAllStacks(Throwable cause, String[][]extraStack) {
    StackTraceElement[] current = new Throwable().getStackTrace();
    while (cause != null) {
      updateStack(cause, current, extraStack);
      cause = cause.getCause();
    }
  }
  
  /** Eliminate matching stack elements in cause's stack trace. */
  private void updateStack(Throwable cause, StackTraceElement[] current, String[][] extraStack) {
    StackTraceElement[] stack = cause.getStackTrace();
    int offset = stack.length - current.length;
    int minMatch = stack.length;
    boolean allMatch = true;
    // we use >= 2 in the condition to skip updateAllStacks and the enclosing constructor
    while (minMatch-1 >= 0 && minMatch-1-offset >= 2) {
      StackTraceElement stackElt = stack[minMatch-1];
      StackTraceElement currentElt = current[minMatch-1-offset];
      if (stackElt.getClassName().equals(currentElt.getClassName()) &&
          stackElt.getMethodName().equals(currentElt.getMethodName())) {
        minMatch--;
      }
      else { allMatch = false; break; }
    }
    if (allMatch && minMatch > 0) {
      int bestExtraMatch = 0;
      boolean bestExtraMatchesAll = true;
      for (String[] extras : extraStack) {
        int extraMatch = 0;
        boolean extraMatchesAll = true;
        while (extraMatch < extras.length && minMatch-extraMatch-1 >= 0) {
          StackTraceElement stackElt = stack[minMatch-extraMatch-1];
          if (extras[extraMatch].equals(stackElt.getClassName() + "." + stackElt.getMethodName())) {
            extraMatch++;
          }
          else { extraMatchesAll = false; break; }
        }
        if (extraMatch > bestExtraMatch) {
          bestExtraMatch = extraMatch;
          bestExtraMatchesAll = extraMatchesAll;
        }
        else if (extraMatch == bestExtraMatch) {
          bestExtraMatchesAll |= extraMatchesAll;
        }
      }
      minMatch -= bestExtraMatch;
      allMatch &= bestExtraMatchesAll;
    }
    
    if (!allMatch) { debug.log("Stack has unmatched elements"); }
    if (minMatch < stack.length) {
      StackTraceElement[] newStack = new StackTraceElement[minMatch];
      System.arraycopy(stack, 0, newStack, 0, minMatch);
      cause.setStackTrace(newStack);
    }
  }
    
  public void printUserMessage(PrintWriter out) { getCause().printStackTrace(out); }
  
}
