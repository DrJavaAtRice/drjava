/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.Log;

import java.io.*;
import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

/**
 * A thread which listens and responds to events from JPDA when the debugger
 * has attached to another JVM.
 * @version $Id$
 */
public class EventHandlerThread extends Thread {

  /**
   * Debugger to which this class reports events.
   */
  protected final JPDADebugger _debugger;
  
  /**
   * JPDA reference to the VirtualMachine generating the events.
   */
  protected final VirtualMachine _vm;
  
  /**
   * Whether this event handler is currently connected to the
   * JPDA VirtualMachine.
   */
  private boolean _connected;
  
  /**
   * A log for recording messages in a file.
   */
  protected final Log _log;
  
  /**
   * Creates a new EventHandlerThread to listen to events from the given
   * debugger and virtual machine.  Calling this Thread's start() method
   * causes it to begin listenting.
   * @param debugger Debugger to which to report events
   * @param vm JPDA reference to the VirtualMachine generating the events
   */
  EventHandlerThread(JPDADebugger debugger, VirtualMachine vm) {
    super("DrJava Debug Event Handler");
    _debugger = debugger;
    _vm = vm;
    _connected = true;
    _log = new Log("EventHandlerThreadLog", false);
  }

  /**
   * Logs any unexpected behavior that occurs (but which should not
   * cause DrJava to abort).
   * @param message message to print to the log
   */
  protected void _log(String message) {
    _log.logTime(message);
  }
  
  /**
   * Logs any unexpected behavior that occurs (but which should not
   * cause DrJava to abort).
   * @param message message to print to the log
   * @param t Exception or Error being logged
   */
  protected void _log(String message, Throwable t) {
    _log.logTime(message, t);
  }
  
  /**
   * Main functionality for this thread.  Continually consumes events
   * from the VM's event queue until it is disconnected.
   */
  public void run() {
    _debugger.notifyDebuggerStarted();
    
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        try {
          // Remove and consume a set of events from the queue
          EventSet eventSet = queue.remove();
          EventIterator it = eventSet.eventIterator();
          while (it.hasNext()) {
            handleEvent(it.nextEvent());
          }
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
      }
    }
    
    _debugger.notifyDebuggerShutdown();
  }
  
  /**
   * Processes a given event from JPDA.
   * A visitor approach would be much better for this, but Sun's Event
   * class doesn't have an appropriate visit() method.
   */
  public void handleEvent(Event e) throws DebugException {
    _log("handling event: " + e);
    
    if (e instanceof BreakpointEvent) {
      _handleBreakpointEvent((BreakpointEvent) e);
    }
    else if (e instanceof StepEvent) {
      _handleStepEvent((StepEvent) e);
    }
    //else if (e instanceof ModificationWatchpointEvent) {
    //  _handleModificationWatchpointEvent((ModificationWatchpointEvent) e);
    //}
    else if (e instanceof ClassPrepareEvent) {
      _handleClassPrepareEvent((ClassPrepareEvent) e);
    }
    else if (e instanceof ThreadStartEvent) {
      _handleThreadStartEvent((ThreadStartEvent) e);
    }
    else if (e instanceof ThreadDeathEvent) {
      _handleThreadDeathEvent((ThreadDeathEvent) e);
    }
    else if (e instanceof VMDeathEvent) {
      _handleVMDeathEvent((VMDeathEvent) e);
    }
    else if (e instanceof VMDisconnectEvent) {
      _handleVMDisconnectEvent((VMDisconnectEvent) e);
    }
    else {
      throw new DebugException("Unexpected event type: " + e);
    }
  }

  /**
   * Returns whether the given thread is both suspended and has
   * stack frames.
   */
  protected boolean _isSuspendedWithFrames(ThreadReference thread) 
    throws DebugException
  {
    try {
      return thread.isSuspended() && thread.frameCount() > 0;
    }
    catch (IncompatibleThreadStateException itse) {
      throw new DebugException("Could not count frames on a suspended thread: " + 
                               itse);
    }
  }
  
  /**
   * Responds to a breakpoint event.
   * @param e breakpoint event from JPDA
   */
  protected void _handleBreakpointEvent(BreakpointEvent e) 
    throws DebugException
  {
    synchronized(_debugger) {
      if (_isSuspendedWithFrames(e.thread()) && 
          _debugger.setCurrentThread(e.thread())) {
        _debugger.currThreadSuspended();
//        _debugger.scrollToSource(e);
        _debugger.reachedBreakpoint((BreakpointRequest)e.request());
      }
    }
  }

  /**
   * Responds to a step event.
   * @param e step event from JPDA
   */
  protected void _handleStepEvent(StepEvent e) 
    throws DebugException
  {
    synchronized(_debugger) {
      if (_isSuspendedWithFrames(e.thread()) &&
          _debugger.setCurrentThread(e.thread())) {
        _debugger.printMessage("Stepped to " + 
                               e.location().declaringType().name() + "." +
                               e.location().method().name() + "(...)  [line " + 
                               e.location().lineNumber() + "]");
        _debugger.currThreadSuspended();
//        _debugger.scrollToSource(e);
      }
      // Delete the step request so it doesn't happen again
      _debugger.getEventRequestManager().deleteEventRequest(e.request());
    }
  }

  /**
   * Responds to an event for a modified watchpoint.
   * This event is not currently expected in DrJava.
   * @param e modification watchpoint event from JPDA
   *
  protected void _handleModificationWatchpointEvent(ModificationWatchpointEvent e) {
    _debugger.printMessage("ModificationWatchpointEvent occured ");
    _debugger.printMessage("Field: " + e.field() + " Value: " +
                          e.valueToBe() +"]");
  }*/
  
  /**
   * Responds when a class of interest has been prepared.
   * Allows the debugger to set a pending breakpoint before any code in
   * the class is executed.
   * @param e class prepare event from JPDA
   * @throws DebugException if actions performed on the prepared class fail
   */
  protected void _handleClassPrepareEvent(ClassPrepareEvent e) 
    throws DebugException
  {
    synchronized(_debugger) {
      _debugger.getPendingRequestManager().classPrepared(e);
      // resume this thread which was suspended because its 
      // suspend policy was SUSPEND_EVENT_THREAD
      e.thread().resume();
    }
  }
  
  /**
   * Responds to a thread start event.
   * @param e thread start event from JPDA
   */
  protected void _handleThreadStartEvent(ThreadStartEvent e) {
    _debugger.threadStarted();
  }
  
  /**
   * Reponds to a thread death event.
   * @param e thread death event from JPDA
   */
  protected void _handleThreadDeathEvent(ThreadDeathEvent e) throws DebugException {
    // no need to check if there are suspended threads on the stack
    // because all that logic should be in the debugger
    
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
    else {
      _debugger.nonCurrThreadDied();
    }
    
    // Thread is suspended on death, so resume it now.
    e.thread().resume();
  }

  /**
   * Responds if the virtual machine being debugged dies.
   * @param e virtual machine death event from JPDA
   */
  protected void _handleVMDeathEvent(VMDeathEvent e) throws DebugException {
    _cleanUp(e);
  }

  /**
   * Responds if the virtual machine being debugged disconnects.
   * @param e virtual machine disconnect event from JPDA
   */
  protected void _handleVMDisconnectEvent(VMDisconnectEvent e) throws DebugException {
    _cleanUp(e);
  }
  
  /**
   * Cleans up the state after the virtual machine being debugged
   * dies or disconnects.
   * @param e JPDA event indicating the debugging session has ended
   */
  protected void _cleanUp(Event e) throws DebugException {
    _connected = false;
    if (_debugger.isReady()) {
      if (_debugger.hasSuspendedThreads()) {
        _debugger.currThreadDied();
      }
      _debugger.shutdown();
    }
  }
  
  /**
   * Responds if a VMDisconnectedException occurs while dealing with
   * another event.  We need to flush the event queue, dealing only
   * with exit events (VMDeath, VMDisconnect) so that we terminate
   * correctly.
   */
  synchronized void handleDisconnectedException() throws DebugException {
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        EventIterator iter = eventSet.eventIterator();
        while (iter.hasNext()) {
          Event event = iter.nextEvent();
          if (event instanceof VMDeathEvent) {
            _handleVMDeathEvent((VMDeathEvent)event);
          }
          else if (event instanceof VMDisconnectEvent) {
            _handleVMDisconnectEvent((VMDisconnectEvent)event);
          }
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
