package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;

import koala.dynamicjava.tree.SourceInfo;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;

public class CompositeException extends InterpreterException {
  
  private final SizedIterable<InterpreterException> _exceptions;
  
  public CompositeException(Iterable<? extends InterpreterException> exceptions) {
    super(extractCause(exceptions));
    _exceptions = IterUtil.snapshot(exceptions);
  }
  
  private static InterpreterException extractCause(Iterable<? extends InterpreterException> exceptions) {
    return IterUtil.isEmpty(exceptions) ? null : IterUtil.first(exceptions);
  }

  public void printUserMessage(PrintWriter out) {
    out.println(_exceptions.size() + " errors:");
    for (InterpreterException e : _exceptions) {
      if (e instanceof SourceInfo.Wrapper) { out.println(((SourceInfo.Wrapper) e).getSourceInfo()); }
      else { out.println("[Unknown location]"); }
      e.printUserMessage(out);
    }
  }

}
