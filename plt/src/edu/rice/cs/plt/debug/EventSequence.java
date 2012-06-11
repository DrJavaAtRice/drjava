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

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.AbstractIterable;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;

/**
 * <p>A sequence of "events" used to record and verify program behavior, especially when this
 * behavior is effect-based.  In typical usage, an instance is created as part of a stub
 * which records events rather than performing normal actions.  An "event" may be represented
 * simply by a string, or by a more complex object.  Given a stub, the driving code is then
 * run, and afterwards the contents of the EventSequence is verified.</p>
 * 
 * <p>Concurrent access is supported.</p>
 */
public class EventSequence<T> extends AbstractIterable<T> implements SizedIterable<T> {
  
  private final List<T> _events; // should always be accessed after synchronizing
  
  public EventSequence() {
    // we don't use Collections.synchronizedList() because we prefer to have direct control
    // over synchronization (the behavior of the Collections implementation is hazy, for
    // example, on the atomicity of addAll
    _events = new LinkedList<T>();
  }
  
  public boolean isEmpty() { return false; }
  public int size() { return _events.size(); }
  public int size(int bound) { return IterUtil.sizeOf(_events, bound); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return false; }
  
  public Iterator<T> iterator() { return new ImmutableIterator<T>(_events.iterator()); }
  
  /** Record a sequence of events.  The entire sequence is recorded atomically. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void record(T... events) { record(Arrays.asList(events)); }
  
  /** Record a sequence of events.  The entire sequence is recorded atomically. */
  public void record(Iterable<? extends T> events) {
    synchronized (_events) {
      _events.addAll(CollectUtil.asCollection(events));
    }
  }
  
  /**
   * Assert that the event sequence is empty.  If not, throw an AssertionError describing the first
   * unexpected event.
   */
  public void assertEmpty() {
    synchronized (_events) {
      if (!_events.isEmpty()) { throw new AssertionError("Unexpected event: " + _events.get(0)); }
    }
  }
  
  /**
   * Assert that the event sequence is empty.  If not, throw an AssertionError with the given
   * message.
   */
  public void assertEmpty(String message) {
    synchronized (_events) {
      if (!_events.isEmpty()) { throw new AssertionError(message); }
    }
  }
  
  /** Assert that each of the given events have been recorded.  Remove all events that do appear.  If 
    * any do not, throw an AssertionError describing the first missing event.  All events are removed
    * atomically.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void assertOccurance(T... expectedEvents) { assertOccurance(Arrays.asList(expectedEvents)); }
  
  /**
   * Assert that each of the given events have been recorded.  Remove all events that do appear.  If 
   * any do not, throw an AssertionError describing the first missing event.  All events are removed
   * atomically.
   */
  public void assertOccurance(Iterable<? extends T> expectedEvents) {
    Option<T> missing = checkOccurance(expectedEvents);
    if (missing.isSome()) {
      throw new AssertionError("Event " + missing.unwrap() + " did not occur");
    }
  }
  
  /**
   * Assert that each of the given events have been recorded.  Remove all events that do appear.  If 
   * any do not, throw an AssertionError with the given message.  All events are removed atomically.
   */
  public void assertOccurance(String message, Iterable<? extends T> expectedEvents) {
    Option<T> missing = checkOccurance(expectedEvents);
    if (missing.isSome()) {
      throw new AssertionError(message);
    }
  }
  
  /** Implementation for assertOccurance.  Returns the first missing element (if any). */
  private Option<T> checkOccurance(Iterable<? extends T> expectedEvents) {
    Option<T> missing = Option.none();
    synchronized (_events) {
      for (T expected : expectedEvents) {
        boolean removed = _events.remove(expected);
        if (!removed && missing.isNone()) { missing = Option.some(expected); }
      }
    }
    return missing;
  }
  
  /** Assert that the given sequence of events was recorded (starting at the beginning).  Remove
    * the matching subsequence.  If there is a mismatch, throw an AssertionError describing it.
    * All events are removed atomically.
    */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void assertSequence(T... expectedEvents) { assertSequence(Arrays.asList(expectedEvents)); }
  
  /** Assert that the given sequence of events was recorded (starting at the beginning).  Remove
    * the matching subsequence.  If there is a mismatch, throw an AssertionError describing it.
    * All events are removed atomically.
    */
  public void assertSequence(Iterable<? extends T> expectedEvents) {
    Option<Pair<T, Option<T>>> mismatched = checkSequence(expectedEvents);
    if (mismatched.isSome()) {
      Pair<T, Option<T>> pair = mismatched.unwrap();
      if (pair.second().isSome()) {
        throw new AssertionError("Unexpected event.  Expected: " + pair.first() +
                                 "; Actual: " + pair.second().unwrap());
      }
      else {
        throw new AssertionError("Event " + pair.first() + " did not occur");
      }
    }
  }
  
  /**
   * Assert that the given sequence of events was recorded (starting at the beginning).  Remove
   * the matching subsequence.  If there is a mismatch, throw an AssertionError with the given 
   * message.  All events are removed atomically.
   */
  public void assertSequence(String message, Iterable<? extends T> expectedEvents) {
    Option<?> mismatched = checkSequence(expectedEvents);
    if (mismatched.isSome()) {
      throw new AssertionError(message);
    }
  }
  
  /**
   * Implementation of assertSequence.  Returns the first mismatched expected/actual pair, if any.
   * If the actual sequence is too short, the second pair element is empty.
   */
  private Option<Pair<T, Option<T>>> checkSequence(Iterable<? extends T> expectedEvents) {
    Iterator<? extends T> expected = expectedEvents.iterator();
    synchronized (_events) {
      Iterator<? extends T> actual = _events.iterator();
      while (expected.hasNext() && actual.hasNext()) {
        T exp = expected.next();
        T act = actual.next();
        if (exp == null ? act == null : exp.equals(act)) {
          actual.remove();
        }
        else { return Option.some(Pair.make(exp, Option.some(act))); }
      }
    }
    if (expected.hasNext()) {
      return Option.some(Pair.<T, Option<T>>make(expected.next(), Option.<T>none()));
    }
    return Option.none();
  }
  
  /** Assert that the given sequence of events, and only that sequence, was recorded (starting
    * at the beginning).  Remove the matching subsequence.  If there is a mismatch, throw an 
    * AssertionError describing it.  All events are removed atomically.
    */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void assertContents(T... expectedEvents) { assertContents(Arrays.asList(expectedEvents)); }
  
  /**
   * Assert that the given sequence of events, and only that sequence, was recorded (starting
   * at the beginning).  Remove the matching subsequence.  If there is a mismatch, throw an 
   * AssertionError describing it.  All events are removed atomically.
   */
  public void assertContents(Iterable<? extends T> expectedEvents) {
    assertSequence(expectedEvents);
    if (!_events.isEmpty()) {
      throw new AssertionError("Unexpected additional event: " + _events.get(0));
    }
  }
  
  /**
   * Assert that the given sequence of events, and only that sequence, was recorded (starting
   * at the beginning).  Remove the matching subsequence.  If there is a mismatch, throw an 
   * AssertionError with the given message.  All events are removed atomically.
   */
  public void assertContents(String message, Iterable<? extends T> expectedEvents) {
    assertSequence(message, expectedEvents);
    if (!_events.isEmpty()) { throw new AssertionError(message); }
  }
  
}
