package edu.rice.cs.dynamicjava.interpreter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import koala.dynamicjava.tree.SourceInfo;

import edu.rice.cs.plt.collect.CollectUtil;
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
      out.println();
      if (e instanceof SourceInfo.Wrapper) { out.println(((SourceInfo.Wrapper) e).getSourceInfo()); }
      else { out.println("[Unknown location]"); }
      e.printUserMessage(out);
    }
  }
  
  public SizedIterable<InterpreterException> exceptions() { return _exceptions; }
  
  public static InterpreterException make(Iterable<? extends InterpreterException> errors) {
    List<InterpreterException> normalized = new ArrayList<InterpreterException>();
    for (InterpreterException e : errors) {
      if (e instanceof CompositeException) {
        CollectUtil.addAll(normalized, ((CompositeException) e).exceptions());
      }
      else { normalized.add(e); }
    }
    return new CompositeException(normalized);
  }

}
