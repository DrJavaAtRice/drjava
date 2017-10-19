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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import edu.rice.cs.plt.lambda.LambdaUtil;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.tuple.Option;

/**
 * A LogSink that discards any messages that are not accepted by a given predicate. Messages that match
 * are passed on to another LogSink.  For convenience, a variety of Message predicate constructors are
 * defined in the class; these can be composed with the methods in LambdaUtil.
 * @see edu.rice.cs.plt.lambda.LambdaUtil#and(Predicate, Predicate)
 * @see edu.rice.cs.plt.lambda.LambdaUtil#or(Predicate, Predicate)
 * @see edu.rice.cs.plt.lambda.LambdaUtil#negate(Predicate)
 */
public class FilteredLogSink implements LogSink {
  
  private final LogSink _delegate;
  private final Predicate<? super Message> _pred;
  
  public FilteredLogSink(LogSink delegate, Predicate<? super Message> pred) {
    _delegate = delegate;
    _pred = pred;
  }
  
  public void close() throws IOException { _delegate.close(); }

  public void log(StandardMessage m) {
    if (_pred.contains(m)) { _delegate.log(m); }
  }

  public void logEnd(EndMessage m) {
    if (_pred.contains(m)) { _delegate.logEnd(m); }
  }

  public void logError(ErrorMessage m) {
    if (_pred.contains(m)) { _delegate.logError(m); }
  }

  public void logStack(StackMessage m) {
    if (_pred.contains(m)) { _delegate.logStack(m); }
  }

  public void logStart(StartMessage m) {
    if (_pred.contains(m)) { _delegate.logStart(m); }
  }

  /** Create a LogSink filtered by a {@link #locationWhiteListPredicate}. */
  public static FilteredLogSink byLocationWhiteList(LogSink delegate, final String... prefixes) {
    return new FilteredLogSink(delegate, locationWhiteListPredicate(prefixes));
  }
  
  /** Create a LogSink filtered by a {@link #locationBlackListPredicate}. */
  public static FilteredLogSink byLocationBlackList(LogSink delegate, final String... prefixes) {
    return new FilteredLogSink(delegate, locationBlackListPredicate(prefixes));
  }
  
  /** Create a LogSink filtered by a {@link #locationPredicate}. */
  public static FilteredLogSink byLocation(LogSink delegate, final Predicate<? super String> pred) {
    return new FilteredLogSink(delegate, locationPredicate(pred));
  }
  
  /** Create a LogSink filtered by a {@link #stackDepthPredicate}. */
  public static FilteredLogSink byStackDepth(LogSink delegate, int maxDepth) {
    return new FilteredLogSink(delegate, stackDepthPredicate(maxDepth));
  }
  
  /** Create a LogSink filtered by a {@link #threadWhiteListPredicate}. */
  public static FilteredLogSink byThreadWhiteList(LogSink delegate, Thread... threads) {
    return new FilteredLogSink(delegate, threadWhiteListPredicate(threads));
  }
  
  /** Create a LogSink filtered by a {@link #threadWhiteListPredicate}. */
  public static FilteredLogSink byThreadWhiteList(LogSink delegate, String... nameParts) {
    return new FilteredLogSink(delegate, threadWhiteListPredicate(nameParts));
  }
  
  /** Create a LogSink filtered by a {@link #threadBlackListPredicate}. */
  public static FilteredLogSink byThreadBlackList(LogSink delegate, Thread... threads) {
    return new FilteredLogSink(delegate, threadBlackListPredicate(threads));
  }
  
  /** Create a LogSink filtered by a {@link #threadBlackListPredicate}. */
  public static FilteredLogSink byThreadBlackList(LogSink delegate, String... nameParts) {
    return new FilteredLogSink(delegate, threadBlackListPredicate(nameParts));
  }
  
  /** Create a LogSink filtered by a {@link #threadPredicate}. */
  public static FilteredLogSink byThread(LogSink delegate, Predicate<? super ThreadSnapshot> pred) {
    return new FilteredLogSink(delegate, threadPredicate(pred));
  }
  
  /**
   * Produce a predicate that accepts only caller locations starting with the given prefixes.  A prefix
   * {@code p} matches a caller location {@code loc} iff
   * {@code (loc.getClassName() + "." + loc.getMethodName()).startsWith(p)}.  If no caller information
   * is available, the empty string is used.
   */
  public static Predicate<Message> locationWhiteListPredicate(final String... prefixes) {
    return locationPredicate(new Predicate<String>() {
      public boolean contains(String s) { return TextUtil.startsWithAny(s, prefixes); }
    });
  }
  
  /**
   * Produce a predicate that rejects any caller locations starting with the given prefixes.  A prefix
   * {@code p} matches a caller location {@code loc} iff
   * {@code (loc.getClassName() + "." + loc.getMethodName()).startsWith(p)}.  If no caller information
   * is available, the empty string is used.
   */
  public static Predicate<Message> locationBlackListPredicate(final String... prefixes) {
    return LambdaUtil.negate(locationWhiteListPredicate(prefixes));
  }
  
  /**
   * Produce a predicate that accepts messages iff their caller location is accepted by {@code pred}.
   * The caller location string is defined for a location {@code loc} as 
   * {@code loc.getClassName() + "." + loc.getMethodName()}.  If no caller information is available,
   * the empty string is used.
   */
  public static Predicate<Message> locationPredicate(final Predicate<? super String> pred) {
    return new Predicate<Message>() {
      public boolean contains(Message m) {
        Option<StackTraceElement> locOpt = m.caller();
        if (locOpt.isSome()) {
          StackTraceElement loc = locOpt.unwrap();
          return pred.contains(loc.getClassName() + "." + loc.getMethodName());
        }
        else { return pred.contains(""); }
      }
    };
  }
  
  /** Produce a predicate that only accepts message with stack traces of at most the given depth. */
  public static Predicate<Message> stackDepthPredicate(final int maxDepth) {
    return new Predicate<Message>() {
      public boolean contains(Message m) { return m.thread().getStackTrace().size() <= maxDepth; }
    };
  }
  
  /** Produce a predicate that only accepts messages logged from the given thread. */
  public static Predicate<Message> threadWhiteListPredicate(Thread... threads) {
    final Set<Long> ids = new HashSet<Long>();
    for (Thread t : threads) { ids.add(t.getId()); }
    threads = null; // no need to hold on to a reference 
    return threadPredicate(new Predicate<ThreadSnapshot>() {
      public boolean contains(ThreadSnapshot t) { return ids.contains(t.getId()); }
    });
  }
  
  /**
   * Produce a predicate that only accepts messages logged from threads matching the given names.
   * A thread matches a name part {@code s} iff its name contains {@code s}.
   */
  public static Predicate<Message> threadWhiteListPredicate(final String... nameParts) {
    return threadPredicate(new Predicate<ThreadSnapshot>() {
      public boolean contains(ThreadSnapshot t) { return TextUtil.containsAny(t.getName(), nameParts); }
    });
  }
  
  /** Produce a predicate that only accepts messages logged from the given thread. */
  public static Predicate<Message> threadBlackListPredicate(Thread... threads) {
    return LambdaUtil.negate(threadWhiteListPredicate(threads));
  }
  
  /**
   * Produce a predicate that only accepts messages logged from threads matching the given names.
   * A thread matches a name part {@code s} iff its name contains {@code s}.
   */
  public static Predicate<Message> threadBlackListPredicate(String... nameParts) {
    return LambdaUtil.negate(threadWhiteListPredicate(nameParts));
  }

  /** Produce a predicate that accepts messages iff their associated ThreadSnapshot is accepted by {@code pred}. */
  public static Predicate<Message> threadPredicate(final Predicate<? super ThreadSnapshot> pred) {
    return new Predicate<Message>() {
      public boolean contains(Message m) { return pred.contains(m.thread()); }
    };
  }
  
}
