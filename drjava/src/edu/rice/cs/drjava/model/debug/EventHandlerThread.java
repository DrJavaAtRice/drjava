/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.util.Log;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;
import java.io.*;

/** A thread that listens and responds to events from JPDA when the debugger has attached to another JVM.
 *  @version $Id$
 */
public class EventHandlerThread extends Thread {

  /** Debugger to which this class reports events. */
  private final JPDADebugger _debugger;

  /** JPDA reference to the VirtualMachine generating the events. */
  private final VirtualMachine _vm;

  /** Whether this event handler is currently connected to the JPDA VirtualMachine. */
  private volatile boolean _connected;

  /** A log for recording messages in a file. */
  private static final Log _log = new Log("EventTest", false);

  /** Creates a new EventHandlerThread to listen to events from the given debugger and virtual machine.  Calling
   *  this Thread's start() method causes it to begin listenting.
   *  @param debugger Debugger to which to report events
   *  @param vm JPDA reference to the VirtualMachine generating the events
   */
  EventHandlerThread(JPDADebugger debugger, VirtualMachine vm) {
    super("DrJava Debug Event Handler");
    _debugger = debugger;
    _vm = vm;
    _connected = true;
  }

  /** Logs any unexpected behavior that occurs (but which should not cause DrJava to abort).
   *  @param message message to print to the log
   */
  private void _log(String message) { _log.log(message); }

  /** Logs any unexpected behavior that occurs (but which should not cause DrJava to abort).
   *  @param message message to print to the log
   *  @param t Exception or Error being logged
   */
  private void _log(String message, Throwable t) { _log.log(message, t); }

  /** Continually consumes events from the VM's event queue until it is disconnected.*/
  public void run() {
    _debugger.notifyDebuggerStarted();

    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        try {
          // Remove and consume a set of events from the queue
          EventSet eventSet = queue.remove();
          EventIterator it = eventSet.eventIterator();
          
          while (it.hasNext()) handleEvent(it.nextEvent());
        }
        catch (InterruptedException ie) {
          // Don't need to do anything.  If the VM was disconnected,
          // the loop will terminate.
          _log("InterruptedException in main loop: " + ie);
        }
        catch (VMDisconnectedException de) {
          // We expect this to happen if the other JVM is reset
          handleDisconnectedException();
          break;
        }
      }
      catch (Exception e) {
        // Log and report to the debugger
        _log("Exception in main event handler loop.", e);
        _debugger.eventHandlerError(e);
        _debugger.printMessage("An exception occurred in the event handler:\n" + e);
        _debugger.printMessage("The debugger may have become unstable as a result.");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(baos, true));
        _debugger.printMessage("Stack trace: "+baos.toString());
      }
    }

    _debugger.notifyDebuggerShutdown();
  }

  /** Processes a given event from JPDA. A visitor approach would be much better for this, but Sun's Event class 
   *  doesn't have an appropriate visit() method.
   */
  private void handleEvent(Event e) throws DebugException {
//    Utilities.showDebug("EventHandler.handleEvent(" + e + ") called");
    _log("handling event: " + e);

    if (e instanceof BreakpointEvent) _handleBreakpointEvent((BreakpointEvent) e);
    else if (e instanceof StepEvent) _handleStepEvent((StepEvent) e);
    //else if (e instanceof ModificationWatchpointEvent) {
    //  _handleModificationWatchpointEvent((ModificationWatchpointEvent) e);
    //}
    else if (e instanceof ClassPrepareEvent) _handleClassPrepareEvent((ClassPrepareEvent) e);
    else if (e instanceof ThreadStartEvent) _handleThreadStartEvent((ThreadStartEvent) e);
    else if (e instanceof ThreadDeathEvent) _handleThreadDeathEvent((ThreadDeathEvent) e);
    else if (e instanceof VMDeathEvent) _handleVMDeathEvent((VMDeathEvent) e);
    else if (e instanceof VMDisconnectEvent) _handleVMDisconnectEvent((VMDisconnectEvent) e);
    else
      throw new DebugException("Unexpected event type: " + e);
  }

  /** Returns whether the given thread is both suspended and has stack frames. */
  private boolean _isSuspendedWithFrames(ThreadReference thread) throws DebugException {
    
    try { return thread.isSuspended() && thread.frameCount() > 0; }
    catch (IncompatibleThreadStateException itse) {
      throw new DebugException("Could not count frames on a suspended thread: " + itse);
    }
  }

  /** Responds to a breakpoint event.
   *  @param e breakpoint event from JPDA
   */
  private void _handleBreakpointEvent(BreakpointEvent e) throws DebugException {
    synchronized(_debugger) {
      if (_isSuspendedWithFrames(e.thread()) && _debugger.setCurrentThread(e.thread())) {
//        Utilities.showDebug("EventHandlerThread._handleBreakpointEvent(" + e + ") called");
        _debugger.currThreadSuspended();
//        _debugger.scrollToSource(e);
        _debugger.reachedBreakpoint((BreakpointRequest) e.request());
      }
    }
  }

  /** Responds to a step event.
   *  @param e step event from JPDA
   */
  private void _handleStepEvent(StepEvent e) throws DebugException {
    synchronized(_debugger) {
      if (_isSuspendedWithFrames(e.thread()) && _debugger.setCurrentThread(e.thread())) {
        _debugger.printMessage("Stepped to " + e.location().declaringType().name() + "." + e.location().method().name()
                                 + "(...)  [line " + e.location().lineNumber() + "]");
        _debugger.currThreadSuspended();
//        _debugger.scrollToSource(e);
      }
      // Delete the step request so it doesn't happen again
      _debugger.getEventRequestManager().deleteEventRequest(e.request());
    }
  }

//  /** Responds to an event for a modified watchpoint.
//   *  This event is not currently expected in DrJava.
//   *  @param e modification watchpoint event from JPDA
//   */
//  private void _handleModificationWatchpointEvent(ModificationWatchpointEvent e) {
//    _debugger.printMessage("ModificationWatchpointEvent occured ");
//    _debugger.printMessage("Field: " + e.field() + " Value: " +
//                          e.valueToBe() +"]");
//  }

  /** Responds when a class of interest has been prepared. Allows the debugger to set a pending breakpoint before any 
   *  code in the class is executed.
   *  @param e class prepare event from JPDA
   *  @throws DebugException if actions performed on the prepared class fail
   */
  private void _handleClassPrepareEvent(ClassPrepareEvent e) throws DebugException {
    synchronized(_debugger) {
      _debugger.getPendingRequestManager().classPrepared(e);
      // resume this thread which was suspended because its
      // suspend policy was SUSPEND_EVENT_THREAD
      e.thread().resume();
    }
  }

  /** Responds to a thread start event.
   *  @param e thread start event from JPDA
   */
  private void _handleThreadStartEvent(ThreadStartEvent e) { synchronized(_debugger) { _debugger.threadStarted(); } }

  /** Reponds to a thread death event.
   *  @param e thread death event from JPDA
   */
  private void _handleThreadDeathEvent(ThreadDeathEvent e) throws DebugException {
    // no need to check if there are suspended threads on the stack
    // because all that logic should be in the debugger
    synchronized(_debugger) {
      ThreadReference running = _debugger.getCurrentRunningThread();
      if (e.thread().equals(running)) {
        // Delete any step requests pending on this thread
        EventRequestManager erm = _vm.eventRequestManager();
        List steps = erm.stepRequests();
        for (int i = 0; i < steps.size(); i++) {
          StepRequest step = (StepRequest)steps.get(i);
          if (step.thread().equals(e.thread())) {
            erm.deleteEventRequest(step);

            // There can only be one step request per thread,
            //  so we can stop looking
            break;
          }
        }
        _debugger.currThreadDied();
      }
      else _debugger.nonCurrThreadDied();
    }

    // Thread is suspended on death, so resume it now.
    e.thread().resume();
  }

  /** Responds if the virtual machine being debugged dies.
   *  @param e virtual machine death event from JPDA
   */
  private void _handleVMDeathEvent(VMDeathEvent e) throws DebugException { _cleanUp(e); }

  /**
   * Responds if the virtual machine being debugged disconnects.
   * @param e virtual machine disconnect event from JPDA
   */
  private void _handleVMDisconnectEvent(VMDisconnectEvent e) throws DebugException { _cleanUp(e); }

  /** Cleans up the state after the virtual machine being debugged  dies or disconnects.
   * @param e JPDA event indicating the debugging session has ended
   */
  private void _cleanUp(Event e) throws DebugException {
    synchronized(_debugger) {
      _connected = false;
      if (_debugger.isReady()) {
        // caused crash if "Run Document's Main Method" was invoked while debugging
        // if (_debugger.hasSuspendedThreads()) _debugger.currThreadDied();
        _debugger.shutdown();
      }
    }
  }

  /** Responds when a VMDisconnectedException occurs while dealing with another event.  We need to flush the event
   *  queue, dealing only with exit events (VMDeath, VMDisconnect) so that we terminate correctly. */
  private void handleDisconnectedException() throws DebugException {
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        EventIterator iter = eventSet.eventIterator();
        while (iter.hasNext()) {
          Event event = iter.nextEvent();
          if (event instanceof VMDeathEvent) _handleVMDeathEvent((VMDeathEvent)event);
          else if (event instanceof VMDisconnectEvent)  _handleVMDisconnectEvent((VMDisconnectEvent)event);
          // else ignore the event
        }
        eventSet.resume(); // Resume the VM
      }
      catch (InterruptedException ie) {
        // ignore
        _log("InterruptedException after a disconnected exception.", ie);
      }
      catch (VMDisconnectedException de) {
        // try to continue flushing the event queue anyway
        _log("A second VMDisconnectedException.", de);
      }
    }
  }
}
