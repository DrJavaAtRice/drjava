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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.recur.RecurUtil;
import edu.rice.cs.plt.recur.RecurUtil.ArrayStringMode;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;

/**
 * A LogSink that formats its messages as lines of text.  Subclasses are responsible for writing that text to
 * an appropriate location.
 */
public abstract class TextLogSink implements LogSink {

  private static final SizedIterable<String> EMPTY_MESSAGE = IterUtil.singleton("");
  private static final SizedIterable<String> START_MESSAGE = IterUtil.singleton("Starting");
  private static final SizedIterable<String> END_MESSAGE = IterUtil.singleton("Ending");
  private static final SizedIterable<String> NO_STACK_MESSAGE = IterUtil.singleton("[No stack trace available]");
  private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("H:mm:ss.SSS");
  
  private final int _idealLineWidth;
  
  protected TextLogSink() { _idealLineWidth = 120; }
  
  protected TextLogSink(int idealLineWidth) {
    if (idealLineWidth < 1) { throw new IllegalArgumentException("idealLineWidth < 1"); }
    _idealLineWidth = idealLineWidth;
  }
  
  /**
   * Record the given message, the body of which is described by the given lines of text.  Subclasses should ensure 
   * that the effects of this method appear to be atomic (at least within the context of concurrent invocations of 
   * this method).  This can be trivially achieved by declaring the method {@code synchronized}.
   */
  protected abstract void write(Message m, SizedIterable<String> text);
  
  /**
   * Record the given start message, the body of which is described by the given lines of text. Subclasses
   * should ensure that the effects of this method appear to be atomic (at least within the context of
   * concurrent invocations of this method). This can be trivially achieved by declaring the method {@code
   * synchronized}.
   */
  protected abstract void writeStart(StartMessage m, SizedIterable<String> text);

  /**
   * Record the given end message, the body of which is described by the given lines of text. Subclasses
   * should ensure that the effects of this method appear to be atomic (at least within the context of
   * concurrent invocations of this method). This can be trivially achieved by declaring the method {@code
   * synchronized}.
   */
  protected abstract void writeEnd(EndMessage m, SizedIterable<String> text);
  
  /** Convert a time to a string of the form {@code "12:23:03.013"} */
  protected static String formatTime(Date time) {
    return TIME_FORMATTER.format(time);
  }
  
  /**
   * Convert a thread to a string of the form {@code "Custom Thread 22"}, consisting of the thread's
   * name and id.
   */
  protected static String formatThread(ThreadSnapshot thread) {
    return "\"" + thread.getName() + "\" " + thread.getId();
  }
  
  /** Convert a location to a string of the form {@code "edu.rice.cs.plt.debug.TextLogSink.formatLocation(24)"} */
  protected static String formatLocation(Option<StackTraceElement> location) {
    if (location.isSome()) { return formatLocation(location.unwrap()); }
    else { return "[Unknown location]"; }
  }

  /** Convert a location to a string of the form {@code "edu.rice.cs.plt.debug.TextLogSink.formatLocation(24)"} */
  protected static String formatLocation(StackTraceElement location) {
    StringBuilder result = new StringBuilder();
    result.append(location.getClassName());
    result.append(".");
    result.append(location.getMethodName());
    result.append("(");
    int line = location.getLineNumber();
    if (line >= 0) { result.append(line); }
    else if (location.isNativeMethod()) { result.append("native"); }
    else { result.append("unknown"); }
    result.append(")");
    return result.toString();
  }
  
  
  public void log(StandardMessage m) {
    SizedIterable<String> text = IterUtil.compose(processText(m.text()), processValues(m.values()));
    if (IterUtil.isEmpty(text)) { text = EMPTY_MESSAGE; }
    write(m, text);
  }

  public void logStart(StartMessage m) {
    SizedIterable<String> text;
    if (m.text().isNone()) { text = START_MESSAGE; }
    else { text = processString("Start " + m.text().unwrap()); }
    text = IterUtil.compose(text, processValues(m.values()));
    writeStart(m, text);
  }
  
  public void logEnd(EndMessage m) {
    SizedIterable<String> text;
    if (m.text().isNone()) { text = END_MESSAGE; }
    else { text = processString("End " + m.text().unwrap()); }
    text = IterUtil.compose(text, processValues(m.values()));
    writeEnd(m, text);
  }

  public void logError(ErrorMessage m) {
    SizedIterable<String> text = IterUtil.compose(processText(m.text()), processThrowable(m.error()));
    write(m, text);
  }

  public void logStack(StackMessage m) {
    SizedIterable<String> text = IterUtil.compose(processText(m.text()), processStack(m.stack()));
    if (IterUtil.isEmpty(text)) { text = NO_STACK_MESSAGE; }
    write(m, text);
  }

  private SizedIterable<String> processText(Option<String> text) {
    if (text.isSome()) { return processString(text.unwrap()); }
    else { return IterUtil.empty(); }
  }
  
  private SizedIterable<String> processThrowable(Throwable t) {
    return processThrowable(t, false);
  }
  
  private SizedIterable<String> processThrowable(Throwable t, boolean asCause) {
    if (t == null) { return IterUtil.singleton("null"); }
    SizedIterable<String> result;
    if (asCause) { result = IterUtil.make("", "Caused by " + t, "at"); }
    else { result = IterUtil.make(t.toString(), "at"); }
    result = IterUtil.compose(result, processStack(IterUtil.asIterable(t.getStackTrace())));
    if (t.getCause() != null) {
      result = IterUtil.compose(result, processThrowable(t.getCause()));
    }
    return result;
  }
  
  private SizedIterable<String> processStack(Iterable<StackTraceElement> stack) {
    SizedIterable<String> result = IterUtil.empty();
    for (StackTraceElement e : stack) {
      result = IterUtil.compose(result, e.toString());
    }
    return result;
  }
  
  private SizedIterable<String> processValues(Iterable<Pair<String, Object>> vals) {
    return IterUtil.collapse(IterUtil.mapSnapshot(vals, _processValue));
  }
  
  private final Lambda<Pair<String, Object>, SizedIterable<String>> _processValue =
    new Lambda<Pair<String, Object>, SizedIterable<String>>() {
    public SizedIterable<String> value(Pair<String, Object> p) { return processValue(p.first(), p.second()); }
  };
  
  private SizedIterable<String> processValue(String name, Object value) {
    SizedIterable<String> valStrings = processString(RecurUtil.safeToString(value));
    if (valStrings.size() > 1 || IterUtil.first(valStrings).length() > _idealLineWidth) {
      // if this is an array or Iterable, print values on separate lines
      if (value instanceof Iterable<?>) {
        valStrings = processString(IterUtil.multilineToString((Iterable<?>) value));
      }
      else if (value instanceof Object[]) {
        valStrings = processString(RecurUtil.arrayToString((Object[]) value, ArrayStringMode.SHALLOW_MULTILINE));
      }
    }
    if (valStrings.size() == 1) {
      return IterUtil.singleton(name + ": " + IterUtil.first(valStrings));
    }
    else {
      return IterUtil.compose(name + ":", valStrings);
    }
  }
  
  /** Convert the string to a list of lines.  Result has size {@code >= 1}. */
  private static SizedIterable<String> processString(String s) {
    SizedIterable<String> result = TextUtil.getLines(s);
    if (result.size() == 0) { return EMPTY_MESSAGE; }
    else { return result; }
  }
  
}
