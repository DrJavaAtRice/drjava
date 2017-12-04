/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.debug;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.io.Closeable;
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * <p>A destination for log messages. After front-end processing in a {@link Log}, which determines the calling
 * time, stack, etc., log messages are created and passed to a sink, which records them as desired.  LogSinks
 * are often associated with system resources, and so care should be taken to close them when no longer needed.
 * {@link IOUtil#closeOnExit} allows LogSinks to be closed at program termination.</p>
 * 
 * <p>LogSink implementations should be thread-safe: each {@code log} invocation should appear to have executed
 * atomically.</p>
 */
public interface LogSink extends Closeable {
  
  public void log(StandardMessage m);
  public void logStart(StartMessage m);
  public void logEnd(EndMessage m);
  public void logError(ErrorMessage m);
  public void logStack(StackMessage m);
  
  public abstract class Message implements Serializable {
    private final ThreadSnapshot _thread;
    private final Option<String> _text;
    
    protected Message(ThreadSnapshot thread) { _thread = thread; _text = Option.none(); }
    protected Message(ThreadSnapshot thread, String text) { _thread = thread; _text = Option.some(text); }
    protected Message(Message copy) { _thread = copy._thread; _text = copy._text; }
    
    /** A ThreadSnapshot created at the initial log method invocation. */
    public ThreadSnapshot thread() { return _thread; }
    /** An optional, arbitrary text message to be logged. */
    public Option<String> text() { return _text; }
    /** The time at which the log method was invoked. */
    public Date time() { return _thread.snapshotTime(); }
    /** The location from which the log method was invoked, if available. */
    public Option<StackTraceElement> caller() { return Option.wrap(_thread.callingLocation()); }
    /** The stack from which the log method was invoked, with the caller on top. */
    public Iterable<StackTraceElement> stack() { return IterUtil.skipFirst(_thread.getStackTrace()); }
    
    public abstract void send(LogSink sink);
    public abstract <T> T apply(MessageVisitor<? extends T> visitor);
    
    /**
     * Convert the message to a form that is guaranteed to be serializable (e.g., invokes {@code toString()} on 
     * objects of arbitrary type).
     */
    public abstract Message serializable();

  
  }
  
  public abstract class ValueMessage extends Message {
    private final Iterable<Pair<String, Object>> _values;
    
    protected ValueMessage(ThreadSnapshot thread, String[] names, Object[] vals) {
      super(thread);
      _values = makeValues(names, vals);
    }
    protected ValueMessage(ThreadSnapshot thread, String message, String[] names, Object[] vals) {
      super(thread, message);
      _values  = makeValues(names, vals);
    }
    /** Copy the given message, with values converted to a serializable form */
    protected ValueMessage(ValueMessage copy) {
      super(copy);
      List<Pair<String, Object>> safeVals = new LinkedList<Pair<String, Object>>();
      for (Pair<String, Object> p : copy._values) {
        safeVals.add(Pair.make(p.first(), IOUtil.ensureSerializable(p.second())));
      }
      _values = safeVals;
    }
    
    private Iterable<Pair<String, Object>> makeValues(String[] names, Object[] vals) {
      if (names.length != vals.length) {
        throw new IllegalArgumentException("Lengths of names and values are inconsistent");
      }
      return IterUtil.zip(IterUtil.make(names), IterUtil.make(vals));
    }
    
    /** A list of name-value pairs to be logged.  May be empty. */
    public Iterable<Pair<String, Object>> values() { return _values; }
    
  }
  
  /** A standard logging message, which may include a text message and name-value pairs. */
  public class StandardMessage extends ValueMessage {
    public StandardMessage(ThreadSnapshot thread, String[] names, Object[] vals) {
      super(thread, names, vals);
    }
    public StandardMessage(ThreadSnapshot thread, String message, String[] names, Object[] vals) {
      super(thread, message, names, vals);
    }
    protected StandardMessage(StandardMessage copy) { super(copy); }
    public void send(LogSink sink) { sink.log(this); }
    public <T> T apply(MessageVisitor<? extends T> visitor) { return visitor.forStandard(this); }
    public StandardMessage serializable() { return new StandardMessage(this); }
  }
  
  /**
   * A message signifying the beginning of a block of code.  Should be matched by an EndMessage occurring
   * in the same thread.
   */
  public class StartMessage extends ValueMessage {
    public StartMessage(ThreadSnapshot thread, String[] names, Object[] vals) {
      super(thread, names, vals);
    }
    public StartMessage(ThreadSnapshot thread, String message, String[] names, Object[] vals) {
      super(thread, message, names, vals);
    }
    protected StartMessage(StartMessage copy) { super(copy); }
    public void send(LogSink sink) { sink.logStart(this); }
    public <T> T apply(MessageVisitor<? extends T> visitor) { return visitor.forStart(this); }
    public StartMessage serializable() { return new StartMessage(this); }
  }
  
  /** A message signifying the end of a block of code.  Should match a previous StartMessage in the same thread. */
  public class EndMessage extends ValueMessage {
    public EndMessage(ThreadSnapshot thread, String[] names, Object[] vals) {
      super(thread, names, vals);
    }
    public EndMessage(ThreadSnapshot thread, String message, String[] names, Object[] vals) {
      super(thread, message, names, vals);
    }
    protected EndMessage(EndMessage copy) { super(copy); }
    public void send(LogSink sink) { sink.logEnd(this); }
    public <T> T apply(MessageVisitor<? extends T> visitor) { return visitor.forEnd(this); }
    public EndMessage serializable() { return new EndMessage(this); }
  }
  
  /**
   * A message logging the occurrence of some error (a Throwable).  Serialization may fail if the
   * Throwable is not serializable.
   */
  public class ErrorMessage extends Message {
    private final Throwable _error;
    public ErrorMessage(ThreadSnapshot thread, Throwable error) { super(thread); _error = error; }
    public ErrorMessage(ThreadSnapshot thread, String text, Throwable error) { super(thread, text); _error = error; }
    public ErrorMessage(ErrorMessage copy) { super(copy); _error = IOUtil.ensureSerializable(copy._error); }
    public Throwable error() { return _error; }
    public void send(LogSink sink) { sink.logError(this); }
    public <T> T apply(MessageVisitor<? extends T> visitor) { return visitor.forError(this); }
    public ErrorMessage serializable() { return new ErrorMessage(this); }
  }
  
  /**
   * A message logging the thread's current stack trace.  While all Messages have ThreadSnapshots and accompanying
   * stack traces, StackMessages explicitly request that the stack be recorded.
   */
  public class StackMessage extends Message {
    public StackMessage(ThreadSnapshot thread) { super(thread); }
    public StackMessage(ThreadSnapshot thread, String text) { super(thread, text); }
    public StackMessage(StackMessage copy) { super(copy); }
    public void send(LogSink sink) { sink.logStack(this); }
    public <T> T apply(MessageVisitor<? extends T> visitor) { return visitor.forStack(this); }
    public StackMessage serializable() { return new StackMessage(this); }
  }
  
  public interface MessageVisitor<T> {
    public T forStandard(StandardMessage m);
    public T forStart(StartMessage m);
    public T forEnd(EndMessage m);
    public T forError(ErrorMessage m);
    public T forStack(StackMessage m);
  }
  
}
