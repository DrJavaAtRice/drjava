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

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class EventHandler extends Thread {
  
  private JPDADebugger _manager;
  private VirtualMachine _vm;
  private boolean _connected = true;  // Connected to VM
  
  EventHandler (JPDADebugger manager, VirtualMachine vm) {
    _manager = manager;
    _vm = vm;
  }
  
  public void run() {
    _manager.notifyDebuggerStarted();
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        EventIterator it = eventSet.eventIterator();
        while (it.hasNext()) {
          handleEvent(it.nextEvent());
        }
      } catch (InterruptedException exc) {
        // Do nothing. Any changes will be seen at top of loop.
      } catch (VMDisconnectedException discExc) {
        handleDisconnectedException();
        break;
      }
    }
    _manager.notifyDebuggerShutdown();    
  }
  
  public void handleEvent(Event e) {
    if (e instanceof BreakpointEvent) {
      _handleBreakpointEvent((BreakpointEvent) e);
    }
    else if (e instanceof StepEvent) {
      _handleStepEvent((StepEvent) e);
    }
    else if (e instanceof ModificationWatchpointEvent) {
      _handleModificationWatchpointEvent((ModificationWatchpointEvent) e);
    }
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
    else 
      throw new Error("Unexpected event type: " + e);
  }
  
  private void _handleBreakpointEvent(BreakpointEvent e) {
    synchronized(_manager){
      _manager.setCurrentThread(e.thread());
      _manager.currThreadSuspended();
      _manager.scrollToSource(e);
      _manager.reachedBreakpoint((BreakpointRequest)e.request());
    }
  }
  
  private void _handleStepEvent(StepEvent e) {
    synchronized(_manager){
      _manager.printMessage("Stepped to " + 
                            e.location().declaringType().name() + "." +
                            e.location().method().name() + "(...)  [line " + 
                            e.location().lineNumber() + "]");
      _manager.getEventRequestManager().deleteEventRequest(e.request());
      _manager.setCurrentThread(e.thread());
      _manager.currThreadSuspended();
      _manager.scrollToSource(e);
    }
  }
  
  private void _handleModificationWatchpointEvent(ModificationWatchpointEvent e) {
    _manager.printMessage("ModificationWatchpointEvent occured ");
    _manager.printMessage("Field: " + e.field() + " Value: " +
                          e.valueToBe() +"]");
  }
  
  private void _handleClassPrepareEvent(ClassPrepareEvent e) {
    synchronized(_manager) {
      try {
        _manager.getPendingRequestManager().classPrepared(e);
      }
      catch(DebugException de) {
      }
      // resumes this thread which was suspended because its 
      // suspend policy was SUSPEND_EVENT_THREAD
      e.thread().resume();
    }
  }
  
  private void _handleThreadStartEvent(ThreadStartEvent e) {
    _manager.threadStarted();
  }
  
  private void _handleThreadDeathEvent(ThreadDeathEvent e) {
    /** no need to check if there are suspended threads on the stack
     * because all that logic should be in the debugger
     */
    if(e.thread().equals(_manager.getCurrentRunningThread())) {
      EventRequestManager erm = _vm.eventRequestManager();
      List steps = erm.stepRequests();
      for (int i = 0; i < steps.size(); i++) {
        StepRequest step = (StepRequest)steps.get(i);
        if (step.thread().equals(e.thread())) {
          erm.deleteEventRequest(step);
          break;
        }
      }
      _manager.currThreadDied();
      _manager.setCurrentThread((DebugThreadData) null);
    }
    else {
      _manager.nonCurrThreadDied(new DebugThreadData(e.thread()));
    }
  }
  
  private void _handleVMDeathEvent(VMDeathEvent e) {
    _cleanUp(e);
  }
  
  private void _handleVMDisconnectEvent(VMDisconnectEvent e) {
    _cleanUp(e);
  }
  
  private void _cleanUp(Event e) {
    _connected = false;
    if (_manager.hasSuspendedThreads()) {
      _manager.currThreadDied();
      _manager.setCurrentThread((DebugThreadData) null);
    }
    _manager.shutdown();
  }
  
  /**
   * A VMDisconnectedException has happened while dealing with
   * another event. We need to flush the event queue, dealing only
   * with exit events (VMDeath, VMDisconnect) so that we terminate
   * correctly.
   */
  synchronized void handleDisconnectedException() {
    EventQueue queue = _vm.eventQueue();
    while (_connected) {
      try {
        EventSet eventSet = queue.remove();
        EventIterator iter = eventSet.eventIterator();
        while (iter.hasNext()) {
          Event event = iter.nextEvent();
          if (event instanceof VMDeathEvent) {
            _handleVMDeathEvent((VMDeathEvent)event);
          } else if (event instanceof VMDisconnectEvent) {
            _handleVMDisconnectEvent((VMDisconnectEvent)event);
          } 
        }
        eventSet.resume(); // Resume the VM
      } 
      catch (InterruptedException exc) {
        // ignore
      }
      catch (VMDisconnectedException vmie) {
        handleDisconnectedException();
      }
    }
  }
}
