/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug.jpda;

import java.awt.EventQueue;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Arrays;
import javax.swing.SwingUtilities;

// DrJava stuff
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.repl.DummyInteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.Lambda;
import edu.rice.cs.drjava.model.debug.*;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** An integrated debugger which attaches to the Interactions JVM using
  * Sun's Java Platform Debugger Architecture (JPDA/JDI) interface.
  *
  * Every public method in this class throws an llegalStateException if
  * it is called while the debugger is not active, except for isAvailable,
  * isReady, and startUp.  Public methods also throw a DebugException if
  * the EventHandlerThread has caught an exception.
  *
  * @version $Id$
  */
public class JPDADebugger implements Debugger {
  
  /** A log for recording messages in a file. */
  private static final Log _log = new Log("GlobalModel.txt", true);
  
  private static final int OBJECT_COLLECTED_TRIES = 5;
  
  private static final String ADD_INTERPRETER_SIG =
    "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;" +
    "[Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Class;)V";
  
  private static final String GET_VARIABLE_SIG = "(Ljava/lang/String;)[Ljava/lang/Object;";
  
  private static final String NEW_INSTANCE_SIG = "(Ljava/lang/Class;I)Ljava/lang/Object;";
  
  /** Reference to DrJava's model. */
  private volatile GlobalModel _model;
  
  /** VirtualMachine of the interactions JVM. */
  private volatile VirtualMachine _vm;
  
  /** Manages all event requests in JDI. */
  private volatile EventRequestManager _eventManager;
  
  /** Vector of all current Watches. */
  private final Vector<DebugWatchData> _watches = new Vector<DebugWatchData>();
  
  /** Keeps track of any DebugActions whose classes have not yet been loaded, so that EventRequests can be created when the correct
    * ClassPrepareEvent occurs.
    */
  private final PendingRequestManager _pendingRequestManager = new PendingRequestManager(this);
  
  /** Provides a way for the JPDADebugger to communicate with the view. */
  final DebugEventNotifier _notifier = new DebugEventNotifier();
  
  /** The running ThreadReference that we are debugging. */
  private volatile ThreadReference _runningThread;
  
  /** Storage for all the threads suspended by this debugger. The "current" thread is the top one on the stack. */
  private volatile RandomAccessStack _suspendedThreads;
  
  /** A handle to the interpreterJVM that we need so we can populate the environment. */
  private volatile ObjectReference _interpreterJVM;
  
  private volatile InteractionsListener _watchListener;
  
  /** If not null, this field holds an error caught by the EventHandlerThread. */
  private volatile Throwable _eventHandlerError;
  
  /** Builds a new JPDADebugger to debug code in the Interactions JVM, using the JPDA/JDI interfaces.
    * Does not actually connect to the interpreterJVM until startUp().
    */
  public JPDADebugger(GlobalModel model) {
    _model = model;
    _vm = null;
    _eventManager = null;
    
    _suspendedThreads = new RandomAccessStack();
    _runningThread = null;
    _interpreterJVM = null;
    _eventHandlerError = null;
    
    _watchListener = new DummyInteractionsListener() {
      public void interactionEnded() { _updateWatches(); }
    };
  }
  
  /** Logs any unexpected behavior that occurs (but which should not cause DrJava to abort).
    * @param message message to print to the log
    */
  private void _log(String message) { _log.log(message); }
  
  /** Logs any unexpected behavior that occurs (but which should not cause DrJava to abort).
    * @param message message to print to the log
    * @param t Exception or Error being logged
    */
  private void _log(String message, Throwable t) { _log.log(message, t); }
  
  
  /** Adds a listener to this JPDADebugger.
    * @param listener a listener that reacts on events generated by the JPDADebugger
    */
  public void addListener(DebugListener listener) {
    _notifier.addListener(listener);
    _model.getBreakpointManager().addListener(listener);
  }
  
  /** Removes a listener to this JPDADebugger.
    * @param listener listener to remove
    */
  public void removeListener(DebugListener listener) {
    _notifier.removeListener(listener);
    _model.getBreakpointManager().removeListener(listener);
  }
  
  /** Returns whether the debugger is available in this copy of DrJava.  This method does not indicate whether the 
    * debugger is ready to be used, which is indicated by isReady().
    */
  public boolean isAvailable() { return true; }
  
  public DebugModelCallback callback() { return new DebugModelCallback() {}; }
  
  /** Returns whether the debugger is currently enabled. */
  public boolean isReady() { return _vm != null; }
  
  /** Attaches the debugger to the Interactions JVM to prepare for debugging.  Only runs in event thread. */
  public /* synchronized */ void startUp() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) {
      _eventHandlerError = null;
      // check if all open documents are in sync
      for (OpenDefinitionsDocument doc: _model.getOpenDefinitionsDocuments()) {
        doc.checkIfClassFileInSync();
      }
      
      try { _attachToVM(); }
      catch(DebugException e1) {  // We sometimes see ConnectExceptions stating that the connection was refused
        try { 
          try { Thread.sleep(100); } // Give any temporary connection problems a chance to resolve
          catch (InterruptedException e) { /* ignore */ }
          _attachToVM(); 
          error.log("Two attempts required for debugger to attach to slave JVM");
        }
        catch(DebugException e2) {
          try { Thread.sleep(500); } // Give any temporary connection problems a chance to resolve
          catch (InterruptedException e) { /* ignore */ }
          _attachToVM();
          error.log("Three attempts required for debugger to attach to slave JVM");
        }  // if we throw another exception, three strikes and we're out
      }
      
      // Listen for events when threads die
      ThreadDeathRequest tdr = _eventManager.createThreadDeathRequest();
      tdr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      tdr.enable();
      
      // Listen for events from JPDA in a new thread
      EventHandlerThread eventHandler = new EventHandlerThread(this, _vm);
      eventHandler.start();
      
      _model.getInteractionsModel().addListener(_watchListener);

      // re-set breakpoints that have already been set
      Vector<Breakpoint> oldBreakpoints = new Vector<Breakpoint>(_model.getBreakpointManager().getRegions());
      _model.getBreakpointManager().clearRegions();
      for (int i = 0; i < oldBreakpoints.size(); i++) {
        Breakpoint bp = oldBreakpoints.get(i);
        int lnr = bp.getLineNumber();
        OpenDefinitionsDocument odd = bp.getDocument();
        setBreakpoint(new JPDABreakpoint(odd, odd._getOffset(lnr), lnr, bp.isEnabled(), this)); 
      }
    }
    
    else
      // Already started
      throw new IllegalStateException("Debugger has already been started.");
  }
  
  
  /** Disconnects the debugger from the Interactions JVM and cleans up any state.
    * @throws IllegalStateException if debugger is not ready
    */
  public /* synchronized */ void shutdown() {
    assert EventQueue.isDispatchThread();
    if (isReady()) {
      Runnable command = new Runnable() { public void run() { _model.getInteractionsModel().removeListener(_watchListener); } };
      
      /* Use SwingUtilities.invokeLater rather than Utilities.invokeLater because we want to defer executing this
       * code after pending events (that may involve the _watchListener) */
      EventQueue.invokeLater(command);
      
      _removeAllDebugInterpreters();
      
      try { _vm.dispose(); }
      catch (VMDisconnectedException vmde) {
        //VM was shutdown prematurely
      }
      finally {
        _model.getInteractionsModel().setToDefaultInterpreter();
        _vm = null;
        _suspendedThreads = new RandomAccessStack();
        _eventManager = null;
        _runningThread = null;
        _updateWatches();
      }
    }
  }
  
  
  /** Sets the notion of current thread to the one contained in threadData.  The thread must be suspended. (Note: the
    * intention is for this method to suspend the thread if necessary, but this is not yet implemented.  The catch is
    * that any manually suspended threads won't cooperate with the debug interpreters; the thread must be suspended by
    * a breakpoint or step.)
    * @param threadData  The Thread to set as current.
    * @throws IllegalStateException if debugger is not ready
    * @throws IllegalArgumentException if threadData is null or not suspended
    */
  public /* synchronized */ void setCurrentThread(DebugThreadData threadData) throws DebugException {
    assert EventQueue.isDispatchThread();
    _ensureReady();
    
    if (threadData == null) {
      throw new IllegalArgumentException("Cannot set current thread to null.");
    }
    
    ThreadReference threadRef = _getThreadFromDebugThreadData(threadData);
    
    // Special case to avoid overhead of scrollToSource() if we
    // are selecting the thread we have already selected currently
    
    // Currently disabled, so we will always scroll to source, even if the
    // thread is already selected.
//    if ( _suspendedThreads.size() > 0 &&
//       _suspendedThreads.peek().uniqueID() == threadRef.uniqueID() ) {
//      return;
//    }
    
    // if we switch to a currently suspended thread, we need to remove
    // it from the stack and put it on the top
    if (_suspendedThreads.contains(threadRef.uniqueID())) _suspendedThreads.remove(threadRef.uniqueID());
    
    if (!threadRef.isSuspended()) {
      throw new IllegalArgumentException("Given thread must be suspended.");
//       threadRef.suspend();
//
//       try{
//         if ( threadRef.frameCount() <= 0 ) {
//           printMessage(threadRef.name() + " could not be suspended. It had no stackframes.");
//           _suspendedThreads.push(threadRef);
//           resume();
//           return;
//         }
//       }
//       catch(IncompatibleThreadStateException ex) {
//         throw new UnexpectedException(ex);
//       }
//
//       //
//       // Step now so that we can get an interpreter,
//       // do not notify (hence the false argument)
//       _stepHelper(StepRequest.STEP_OVER, false);
      //return;
    }
    
    _suspendedThreads.push(threadRef);
    
    try {
      if (threadRef.frameCount() <= 0) {
        printMessage(threadRef.name() + " could not be suspended since it has no stackframes.");
        resume();
        return;
      }
    }
    catch (IncompatibleThreadStateException e) {
      throw new DebugException("Could not suspend thread: " + e);
    }
    
    // Activate the debug interpreter for interacting with this thread
    _switchToInterpreterForThreadReference(threadRef);
    _switchToSuspendedThread();
    printMessage("The current thread has changed.");
  }
  
  /** Returns the currently selected thread for the debugger.  */
  ThreadReference getCurrentThread() {
    // Current thread is the top one on the stack
    return _suspendedThreads.peek();
  }
  
  /** Returns whether the debugger currently has any suspended threads. */
  public /* synchronized */ boolean hasSuspendedThreads() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) return false;
    return _suspendedThreads.size() > 0;
  }
  
  /** Returns whether the debugger's current thread is suspended. */
  public /* synchronized */ boolean isCurrentThreadSuspended() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) return false;
    return hasSuspendedThreads() && ! hasRunningThread();
  }
  
  /** Returns whether the thread the debugger is tracking is now running. */
  public /* synchronized */ boolean hasRunningThread() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) return false;
    return _runningThread != null;
  }
  
  /** Resumes the thread currently being debugged, copying back all variables from the current debug interpreter. */
  public /* synchronized */ void resume() throws DebugException {
    assert EventQueue.isDispatchThread();
    _ensureReady();
    _resumeHelper(false);
  }
  
  /** Resumes the given thread, copying back any variables from its associated debug interpreter.
    * @param threadData Thread to resume
    */
  public /* synchronized */ void resume(DebugThreadData threadData) throws DebugException {
    assert EventQueue.isDispatchThread();
    _ensureReady();
    ThreadReference thread = _suspendedThreads.remove(threadData.getUniqueID());
    _resumeThread(thread, false);
  }
  
  /** Steps the execution of the currently loaded document. */
  public /* synchronized */ void step(StepType type) throws DebugException {
    assert EventQueue.isDispatchThread();
    _ensureReady();
    _stepHelper(type, true);
  }
  
  /** Adds a watch on the given field or variable.
    * @param field the name of the field we will watch
    */
  public /* synchronized */ void addWatch(String field) throws DebugException {
    // _ensureReady();
    assert EventQueue.isDispatchThread();
    final DebugWatchData w = new DebugWatchData(field);
    _watches.add(w);
    _updateWatches();
    
//    Utilities.invokeLater(new Runnable() { public void run() { 
      _notifier.watchSet(w); 
//    } });
  }
  
  /** Removes any watches on the given field or variable.
   * Has no effect if the given field is not being watched.
   * @param field the name of the field we will watch
   */
  public /* synchronized */ void removeWatch(String field) throws DebugException {
    // _ensureReady();
    assert EventQueue.isDispatchThread();
    for (int i=0; i < _watches.size(); i++) {
      final DebugWatchData watch = _watches.get(i);
      if (watch.getName().equals(field)) {
        _watches.remove(i);
//        Utilities.invokeLater(new Runnable() { public void run() { 
          _notifier.watchRemoved(watch); 
//        } });
      }
    }
  }
  
  /** Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public /* synchronized */ void removeWatch(int index) throws DebugException {
    // _ensureReady();
    assert EventQueue.isDispatchThread();
    if (index < _watches.size()) {
      final DebugWatchData watch = _watches.get(index);
      _watches.remove(index);
//      Utilities.invokeLater(new Runnable() { public void run() { 
        _notifier.watchRemoved(watch); 
//      } });
    }
  }
  
  /** Removes all watches on existing fields and variables.
   */
  public /* synchronized */ void removeAllWatches() throws DebugException {
    // _ensureReady();
    assert EventQueue.isDispatchThread();
    while (_watches.size() > 0) {
      removeWatch( _watches.get(0).getName());
    }
  }
  
  /** Enable or disable the specified breakpoint.
   * @param breakpoint breakpoint to change
   */
  public /* synchronized */ void notifyBreakpointChange(Breakpoint breakpoint) {
    assert EventQueue.isDispatchThread();
    _model.getBreakpointManager().changeRegion(breakpoint, new Lambda<Object, Breakpoint>() {
      public Object apply(Breakpoint bp) {
        // change has already been made, just notify all listeners
        return null;
      }
    });
  }
  
  /** Toggles whether a breakpoint is set at the given line in the given document.
    * @param doc  Document in which to set or remove the breakpoint
    * @param offset  Start offset on the line to set the breakpoint
    * @param lineNum  Line on which to set or remove the breakpoint, >=1
    * @param isEnabled  {@code true} if this breakpoint should be enabled
    * TODO: check synchronization; why no read lock on doc?
    */
  public /* synchronized */ void toggleBreakpoint(OpenDefinitionsDocument doc, int offset, int lineNum, boolean isEnabled) 
    throws DebugException {
        assert EventQueue.isDispatchThread();
    // ensure that offset is at line start and falls within the document
    offset = doc._getLineStartPos(offset);
    if (offset < 0 || offset > doc.getLength()) return;
    
    Breakpoint breakpoint = _model.getBreakpointManager().getRegionAt(doc, offset);
    
    if (breakpoint == null) {
      if (offset == doc._getLineEndPos(offset)) {
        Utilities.show("Cannot set a breakpoint on an empty line.");
      }
      else {
        try { setBreakpoint(new JPDABreakpoint(doc, offset, lineNum, isEnabled, this)); }
        catch(LineNotExecutableException lnee) { Utilities.show(lnee.getMessage()); }
      }
    }
    else _model.getBreakpointManager().removeRegion(breakpoint);
  }
  
  /** Sets a breakpoint.
    * @param breakpoint The new breakpoint to set
    */
  public /* synchronized */ void setBreakpoint(final Breakpoint breakpoint) throws DebugException {
    assert EventQueue.isDispatchThread();
    breakpoint.getDocument().checkIfClassFileInSync();
    
    _model.getBreakpointManager().addRegion(breakpoint);
  }
  
  /** Removes a breakpoint. Called from toggleBreakpoint -- even with BPs that are not active.
    * @param bp The breakpoint to remove.
    */
  public /* synchronized */ void removeBreakpoint(Breakpoint bp) throws DebugException {
    assert EventQueue.isDispatchThread();
    if (!(bp instanceof JPDABreakpoint)) { throw new IllegalArgumentException("Unsupported breakpoint"); }
    else {
      JPDABreakpoint breakpoint = (JPDABreakpoint) bp;
      Vector<BreakpointRequest> requests = breakpoint.getRequests();
      if (requests.size() > 0 && _eventManager != null) {
        // Remove all event requests for this breakpoint
        try {
          for (int i=0; i < requests.size(); i++) {
            _eventManager.deleteEventRequest(requests.get(i));
          }
        }
        catch (VMMismatchException vme) {
          // Not associated with this VM; probably from a previous session.
          // Ignore and make sure it gets removed from the document.
          _log("VMMismatch when removing breakpoint.", vme);
        }
        catch (VMDisconnectedException vmde) {
          // The VM has already disconnected for some reason
          // Ignore it and make sure the breakpoint gets removed from the document
          _log("VMDisconnected when removing breakpoint.", vmde);
        }
      }
      
      // Always remove from pending request, since it's always there
      _pendingRequestManager.removePendingRequest(breakpoint);
    }
  }
  
  /** Returns all currently watched fields and variables. No synchronization required because _watches is final. */
  public Vector<DebugWatchData> getWatches() throws DebugException {
    //_ensureReady();
    return _watches;
  }
  
  /** Returns a list of all threads being tracked by the debugger. Does not return any threads known to be dead. */
  public /* synchronized */ Vector<DebugThreadData> getCurrentThreadData() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) { return new Vector<DebugThreadData>(); }
    Iterable<ThreadReference> listThreads;
    try { listThreads = _vm.allThreads(); }
    catch (VMDisconnectedException vmde) {
      // We're quitting, just pass back an empty Vector
      return new Vector<DebugThreadData>();
    }
    
    Vector<DebugThreadData> threads = new Vector<DebugThreadData>();
    for (ThreadReference ref : listThreads) {
      try { threads.add(new JPDAThreadData(ref)); }
      catch (ObjectCollectedException e) {
        // this thread just died, we don't want to list it anyway
      }
    }
    return threads;
  }
  
  /** Returns a Vector of DebugStackData for the current suspended thread.
   * @throws DebugException if the current thread is running or there
   * are no suspended threads
   * TO DO: Config option for hiding DrJava subset of stack trace
   */
  public /* synchronized */ Vector<DebugStackData> getCurrentStackFrameData() throws DebugException {
    assert EventQueue.isDispatchThread();
    if (! isReady()) return new Vector<DebugStackData>();
    
    if (_runningThread != null || _suspendedThreads.size() <= 0) {
      throw new DebugException("No suspended thread to obtain stack frames.");
    }
    
    try {
      ThreadReference thread = _suspendedThreads.peek();
      Vector<DebugStackData> frames = new Vector<DebugStackData>();
      for (StackFrame f : thread.frames()) { frames.add(new JPDAStackData(f)); }
      return frames;
    }
    catch (IncompatibleThreadStateException itse) {
      error.log("Unable to obtain stack frame.", itse);
      return new Vector<DebugStackData>();
    }
    catch (VMDisconnectedException vmde) {
      error.log("VMDisconnected when getting the current stack frame data.", vmde);
      return new Vector<DebugStackData>();
    }
    catch (InvalidStackFrameException isfe) {
      error.log("The stack frame requested is invalid.", isfe);
      return new Vector<DebugStackData>();
    }
  }
  
  /** Return the document associated with this location. A document is preloaded when a debugger step is
    * made to avoid the deadlock described in [ 1696060 ] Debugger Infinite Loop.
    */
  public OpenDefinitionsDocument preloadDocument(Location location) {
    assert EventQueue.isDispatchThread();
    OpenDefinitionsDocument doc = null;
    
    // No stored doc, look on the source root set (later, also the sourcepath)
    ReferenceType rt = location.declaringType();
    String fileName;
    try { fileName = getPackageDir(rt.name()) + rt.sourceName(); }
    catch (AbsentInformationException aie) {
      // Don't know real source name:
      //   assume source name is same as file name
      String className = rt.name().replace('.', File.separatorChar);
      
      // crop off the $ if there is one and anything after it
      int indexOfDollar = className.indexOf('$');
      if (indexOfDollar > -1) {
        className = className.substring(0, indexOfDollar);
      }
      
      fileName = className + ".java";
    }
    
    // Check source root set (open files)
    File f = _model.getSourceFile(fileName);
    if (f != null) {
      // Get a document for this file, forcing it to open
      try { doc = _model.getDocumentForFile(f); }
      catch (IOException ioe) {
        // No doc, so don't notify listener
      }
    }
    return doc;
  }
  
  /** Scrolls to the source location specified by the the debug stack data.
    * @param stackData Stack data containing location to display
    * @throws DebugException if current thread is not suspended
    */
  public /* synchronized */ void scrollToSource(DebugStackData stackData) throws DebugException {
    assert EventQueue.isDispatchThread();
    _ensureReady();
    if (_runningThread != null) {
      throw new DebugException("Cannot scroll to source unless thread is suspended.");
    }
    
    ThreadReference threadRef = _suspendedThreads.peek();
    Iterator<StackFrame> i;
    
    try {
      if (threadRef.frameCount() <= 0 ) {
        printMessage("Could not scroll to source. The current thread had no stack frames.");
        return;
      }
      i = threadRef.frames().iterator();
    }
    catch (IncompatibleThreadStateException e) {
      throw new DebugException("Unable to find stack frames: " + e);
    }
    
    while (i.hasNext()) {
      StackFrame frame = i.next();
      
      if (frame.location().lineNumber() == stackData.getLine() &&
          stackData.getMethod().equals(frame.location().declaringType().name() + "." +
                                       frame.location().method().name()))
      {
        scrollToSource(frame.location(), false);
      }
    }
  }
  
  /** Scrolls to the source of the given breakpoint.
    * @param bp the breakpoint
    */
  public /* synchronized */ void scrollToSource(Breakpoint bp) {
    openAndScroll(bp.getDocument(), bp.getLineNumber(), bp.getClassName(), false);
  }
  
  /** Gets the Breakpoint object at the specified line in the given class.
    * If the given data do not correspond to an actual breakpoint, null is returned.
    * @param line  The line number of the breakpoint
    * @param className  The name of the class the breakpoint's in
    * @return the Breakpoint corresponding to the line and className, or null if there is no such breakpoint.
    */
  public /* synchronized */ Breakpoint getBreakpoint(int line, String className) {
    assert EventQueue.isDispatchThread();
    for (int i = 0; i < _model.getBreakpointManager().getRegions().size(); i++) {
      Breakpoint bp = _model.getBreakpointManager().getRegions().get(i);
      if ((bp.getLineNumber() == line) && (bp.getClassName().equals(className))) {
        return bp;
      }
    }
    // bp not found in the list of breakpoints
    return null;
  }
  
  
  
  
  /** Accessor for the _vm field.  Called from DocumentDebugAction and this. */
  VirtualMachine getVM() { return _vm; }
  
  /** Returns the current EventRequestManager from JDI, or null if startUp() has not been called. */
  EventRequestManager getEventRequestManager() { return _eventManager; }
  
  /** Returns the pending request manager used by the debugger. */
  PendingRequestManager getPendingRequestManager() { return _pendingRequestManager; }
  
  /** Returns the suspended thread at the current index of the stack.
    * @param i index into the stack of suspended threads
    */
  ThreadReference getThreadAt(int i) { return _suspendedThreads.peekAt(i); }
  
  /** Returns the running thread currently tracked by the debugger. */
  ThreadReference getCurrentRunningThread() { return _runningThread; }
  
  
  
  /** Ensures that debugger is active.  Should be called by every public method in the debugger except for startUp().
    * Assumes lock is already held.
    * @throws IllegalStateException if debugger is not active
    * @throws DebugException if an exception was detected in the EventHandlerThread
    */
  private void _ensureReady() throws DebugException {
    if (! isReady()) throw new IllegalStateException("Debugger is not active.");
    
    if (_eventHandlerError != null) {
      Throwable t = _eventHandlerError;
      _eventHandlerError = null;
      throw new DebugException("Error in Debugger Event Handler: " + t);
    }
  }
  
  /** Records that an error occurred in the EventHandlerThread. The next call to _ensureReady() will fail, indicating
    * that the error occurred.  Not private because EventHandlerThread accesses it.
    * @param t Error occurring in the EventHandlerThread
    */
  void eventHandlerError(Throwable t) {
    _log("Error in EventHandlerThread: " + t);
    _eventHandlerError = t;
  }
  
  private volatile Runnable _command = null;
  
  /** Handles the details of attaching to the interpreterJVM. Only runs in the event thread. */
  private void _attachToVM() throws DebugException {
    assert EventQueue.isDispatchThread();
//    System.err.println("Debugger attaching to VM");
    
    // Get the connector
    AttachingConnector connector = _getAttachingConnector();
    
    // Try to connect on our debug port
    Map<String, Connector.Argument> args = connector.defaultArguments();
    Connector.Argument port = args.get("port");
    Connector.Argument host = args.get("hostname");
    try {
      int debugPort = _model.getDebugPort();
      port.setValue("" + debugPort);
      host.setValue("127.0.0.1"); // necessary if hostname can't be resolved
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
    }
    catch(Exception e) { 
//      System.err.println("Could not connect to VM: " + e);
      throw new DebugException("Could not connect to VM: " + e); 
    }
    
    _interpreterJVM = (ObjectReference) _getStaticField(_getClass(InterpreterJVM.class.getName()), "ONLY");
//    System.err.println("_interpreterm vm is " + _interpreterJVM);
  }
  
  /** Returns an attaching connector to use for connecting to the interpreter JVM. */
  private AttachingConnector _getAttachingConnector() throws DebugException {
    VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
    List<AttachingConnector> connectors = vmm.attachingConnectors();
    AttachingConnector connector = null;
    for (AttachingConnector conn: connectors) {
      if (conn.name().equals("com.sun.jdi.SocketAttach"))  connector = conn;
    }
    if (connector == null) throw new DebugException("Could not find an AttachingConnector!");
    return connector;
  }
  
  /** Sets the debugger's currently active thread.
   * This method assumes that the given thread is already suspended.
   * Returns true if this actually changed the suspended thread
   * by pushing it onto the stack of suspended threads.  Returns
   * false if this thread was already selected.
   *
   * The return value fixes a bug that occurs if the user steps
   * into a breakpoint.
   *
   * @throws IllegalArgumentException if thread is not suspended.
   */
  /* synchronized */ boolean setCurrentThread(ThreadReference thread) {
    assert EventQueue.isDispatchThread();
    if (! thread.isSuspended()) {
      throw new IllegalArgumentException("Thread must be suspended to set as current.  Given: " + thread);
    }
    
    try {
      if ((_suspendedThreads.isEmpty() || ! _suspendedThreads.contains(thread.uniqueID())) &&
          (thread.frameCount() > 0)) {
        _suspendedThreads.push(thread);
        return true;
      }
      else return false;
    }
    catch (IncompatibleThreadStateException itse) {
      // requesting stack frames should be fine, since the thread must be
      // suspended or frameCount() is not called
      throw new UnexpectedException(itse);
    }
  }
  
  /** Returns a Vector with the loaded ReferenceTypes for the given class name
    * (empty if the class could not be found).  Makes no attempt to load the
    * class if it is not already loaded.  If the lineNumber is not
    * DebugAction.ANY_LINE, this method ensures that the returned ReferenceTypes
    * contain the given lineNumber, searching through inner classes if necessary.
    * If no inner classes contain the line number, an empty Vector is returned.
    * <p>
    * If custom class loaders are in use, multiple copies of the class
    * may be loaded, so all are returned.
    */
  /* synchronized */ Vector<ReferenceType> getReferenceTypes(String className, int lineNumber) {
    assert EventQueue.isDispatchThread();
    // Get all classes that match this name
    List<ReferenceType> classes;
    
    try { classes = _vm.classesByName(className); }
    catch (VMDisconnectedException vmde) {
      // We're quitting, return empty Vector.
      return new Vector<ReferenceType>();
    }
    
    // Return each valid reference type
    Vector<ReferenceType> refTypes = new Vector<ReferenceType>();
    ReferenceType ref;
    for (int i=0; i < classes.size(); i++) {
      ref = classes.get(i);
      
      if (lineNumber != DebugAction.ANY_LINE) {
        List<Location> lines = new LinkedList<Location>();
        try {
          lines = ref.locationsOfLine(lineNumber);
        }
        catch (AbsentInformationException aie) {
          // try looking in inner classes
        }
        catch (ClassNotPreparedException cnpe) {
          // try the next class, maybe loaded by a different classloader
          continue;
        }
        // If lines.size > 0, lineNumber was found in ref
        if (lines.size() == 0) {
          // The ReferenceType might be in an inner class, so
          //  look for locationsOfLine for nestedTypes
          List<ReferenceType> innerRefs = ref.nestedTypes();
          ref = null;
          for (int j = 0; j < innerRefs.size(); j++) {
            try {
              ReferenceType currRef = innerRefs.get(j);
              lines = currRef.locationsOfLine(lineNumber);
              if (lines.size() > 0) {
                ref = currRef;
                break;
              }
            }
            catch (AbsentInformationException aie) {
              // skipping this inner class, look in another
            }
            catch (ClassNotPreparedException cnpe) {
              // skipping this inner class, look in another
            }
          }
        }
      }
      if ((ref != null) && ref.isPrepared()) {
        refTypes.add(ref);
      }
    }
    return refTypes;
  }
  
  /** Assumes lock is already held.
    * @return The thread in the virtual machine with name d.uniqueID()
    * @throws NoSuchElementException if the thread could not be found
    */
  private ThreadReference _getThreadFromDebugThreadData(DebugThreadData d) throws NoSuchElementException {
    List<ThreadReference> threads = _vm.allThreads(); 
    Iterator<ThreadReference> iterator = threads.iterator();
    while (iterator.hasNext()) {
      ThreadReference threadRef = iterator.next();
      if (threadRef.uniqueID() == d.getUniqueID()) {
        return threadRef;
      }
    }
    // Thread not found
    throw new NoSuchElementException("Thread " + d.getName() + " not found in virtual machine!");
  }
  
  /** Suspends all the currently running threads in the virtual machine.
   *
   * Not currently in use/available, since it is incompatible with
   * the debug interpreters.
   *
   public synchronized void suspendAll() {
   _ensureReady();
   List threads = _vm.allThreads();
   Iterator iterator = threads.iterator();
   ThreadReference threadRef = null;
   
   while(iterator.hasNext()) {
   threadRef = (ThreadReference)iterator.next();
   
   if ( !threadRef.isSuspended() ) {
   threadRef.suspend();
   _suspendedThreads.push(threadRef);
   }
   }
   _runningThread = null;
   }*/
  
  /** Suspends execution of the thread referenced by threadData.
   *
   * Not in use/available, since it is currently incompatible with the
   * debug interpreters.  (Can't execute code in a suspended thread unless
   * it was suspended with a breakpoint/step.)
   *
   public synchronized void suspend(DebugThreadData threadData)
   throws DebugException
   {
   _ensureReady();
   // setCurrentThread suspends if necessary
   setCurrentThread(threadData);
   _runningThread = null;
   }*/
  
  /** Resumes the thread currently being debugged without removing the debug interpreter or switching to the next 
    * suspended thread.  Assumes lock is already held.
    */
  private void _resumeFromStep() throws DebugException { _resumeHelper(true); }
  
  /** Resumes execution of the currently suspended thread.  Assumes lock is already held.
    * @param fromStep Whether to copy back the variables from the current debug interpreter and switch to the next
    * suspended thread.
    */
  private void _resumeHelper(boolean fromStep) throws DebugException {
    try {
      ThreadReference thread = _suspendedThreads.pop();
      
      _log.log("In resumeThread()");
      _resumeThread(thread, fromStep);
    }
    catch (NoSuchElementException e) { throw new DebugException("No thread to resume."); }
  }
  
  /** Resumes the given thread, only copying variables from its debug interpreter if shouldCopyBack is true.  Assumes
    * lock on this is already held.
    * @param thread Thread to resume
    * @param fromStep Whether to copy back the variables from
    * the current debug interpreter and switch to the next
    * suspended thread.
    * @throws IllegalArgumentException if thread is null
    */
  private void _resumeThread(ThreadReference thread, boolean fromStep) throws DebugException {
    if (thread == null) {
      throw new IllegalArgumentException("Cannot resume a null thread");
    }
    
    int suspendCount = thread.suspendCount();
    _log.log("Getting suspendCount = " + suspendCount);
    
    
    _runningThread = thread;
    if (!fromStep) {
      // Copy variables back into the thread
      _copyVariablesFromInterpreter();
      _updateWatches();
    }
    try {
      _removeCurrentDebugInterpreter(fromStep);
      _currThreadResumed();
    }
    catch(DebugException e) { throw new UnexpectedException(e); }
    
    // Must resume the correct number of times
    for (int i=suspendCount; i>0; i--) thread.resume();
    
    // Notify listeners of a resume
    
    // Switch to next suspended thread, if any
    if (! fromStep && ! _suspendedThreads.isEmpty()) _switchToSuspendedThread();
  }
  
  /** Performs a step in the currently suspended thread, only generating a step event if shouldNotify if true.  Assumes
    * that lock is already held.
    * @param type The type of step to perform
    * @param shouldNotify Whether to generate a step event
    */
  private void _stepHelper(StepType type, boolean shouldNotify) throws DebugException {
    if (_suspendedThreads.size() <= 0 || _runningThread != null) {
      throw new IllegalStateException("Cannot step if the current thread is not suspended.");
    }
    
    _log.log(this + "is About to peek ...");
    
    ThreadReference thread = _suspendedThreads.peek();
    _log.log(this + "is Stepping " + thread.toString());
    
    // Copy the variables back into the thread from the appropriate interpreter.
    // We do this before stepping since DrJava will hang if you try to copy back
    // variables after creating the step request.
    _runningThread = thread;
    _copyVariablesFromInterpreter();
    
    _log.log(this + " is Deleting pending requests ...");
    
    // If there's already a step request for the current thread, delete
    //  it first
    List<StepRequest> steps = _eventManager.stepRequests();
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = steps.get(i);
      if (step.thread().equals(thread)) {
        _eventManager.deleteEventRequest(step);
        break;
      }
    }
    
    _log.log(this + " Issued step request");
    int stepFlag = Integer.MIN_VALUE; // should always be changed, but compiler doesn't check that
    switch (type) {
      case STEP_INTO: stepFlag = StepRequest.STEP_INTO; break;
      case STEP_OVER: stepFlag = StepRequest.STEP_OVER; break;
      case STEP_OUT: stepFlag = StepRequest.STEP_OUT; break;
    }
    new Step(this, StepRequest.STEP_LINE, stepFlag);
    if (shouldNotify) notifyStepRequested();
    _log.log(this + " About to resume");
    _resumeFromStep();
  }
  
  
  /** Called when a breakpoint is reached.  The Breakpoint object itself should be stored in the "debugAction" property
    * on the request.
    * @param request The BreakPointRequest reached by the debugger
    */
  /* synchronized */ void reachedBreakpoint(BreakpointRequest request) {
//    Utilities.showDebug("JPDADebugger.reachedBreakPoint(" + request + ") called");
    assert EventQueue.isDispatchThread();
    Object property = request.getProperty("debugAction");
    if (property != null && (property instanceof JPDABreakpoint)) {
      final JPDABreakpoint breakpoint = (JPDABreakpoint) property;
      printMessage("Breakpoint hit in class " + breakpoint.getClassName() + "  [line " + breakpoint.getLineNumber() + "]");
      
      EventQueue.invokeLater(new Runnable() { public void run() { _notifier.breakpointReached(breakpoint); } });
    }
    else {
      // A breakpoint we didn't set??
      error.log("Reached a breakpoint without a debugAction property: " + request);
    }
  }
  
  // Not currently used
//  /** Takes the location of event e, opens the document corresponding to its class and centers the definition pane's
//    * view on the appropriate line number.  Assumes lock on this is already held.
//    * @param e LocatableEvent containing location to display
//    */
//  private void scrollToSource(LocatableEvent e) {
//    Location location = e.location();
//    
//    // First see if doc is stored
//    EventRequest request = e.request();
//    Object docProp = request.getProperty("document");
//    if ((docProp != null) && (docProp instanceof OpenDefinitionsDocument)) {
//      openAndScroll((OpenDefinitionsDocument) docProp, location, true);
//    }
//    else  scrollToSource(location);
//  }
  
  /** Scroll to the location specified by location  Assumes lock on this is already held */
  private void scrollToSource(Location location) {
    scrollToSource(location, true);
  }
  
  /** Scroll to the location specified by location.  Assumes lock on this is already held. */
  private void scrollToSource(Location location, boolean shouldHighlight) {
    assert EventQueue.isDispatchThread();
    OpenDefinitionsDocument doc = preloadDocument(location);
    openAndScroll(doc, location, shouldHighlight);
  }
  
  /** Opens a document and scrolls to the appropriate location.  If doc is null, a message is printed indicating the 
    * source file could not be found.  Assumes lock on this is already held.
    * @param doc Document to open
    * @param location Location to display
    */
  private void openAndScroll(OpenDefinitionsDocument doc, Location location, boolean shouldHighlight) {
    openAndScroll(doc, location.lineNumber(), location.declaringType().name(), shouldHighlight);
  }
  
  /** Opens a document and scrolls to the appropriate location.  If doc is null, a message is printed indicating the
    * source file could not be found.  Assumes lock on this is already held.
    * @param doc Document to open
    * @param line the line number to display
    * @param className the name of the appropriate class
    */
  private void openAndScroll(final OpenDefinitionsDocument doc, final int line, String className, 
                             final boolean shouldHighlight) {
    assert EventQueue.isDispatchThread();
    // Open and scroll if doc was found
    if (doc != null) { 
      doc.checkIfClassFileInSync();
      // change UI if in sync in MainFrame listener
      
      EventQueue.invokeLater(new Runnable() { public void run() { _notifier.threadLocationUpdated(doc, line, shouldHighlight); } });
    }
    else printMessage("  (Source for " + className + " not found.)");
  }
  
  /** Returns the relative directory (from the source root) that the source file with this qualifed name will be in, 
    * given its package. Returns the empty string for classes without packages.
    * TO DO: Move this to a static utility class
    * @param className The fully qualified class name
    */
  static String getPackageDir(String className) {
    // Only keep up to the last dot
    int lastDotIndex = className.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return "";
    }
    else {
      String packageName = className.substring(0, lastDotIndex);
      packageName = packageName.replace('.', File.separatorChar);
      return packageName + File.separatorChar;
    }
  }
  
  /** Prints a message in the Interactions Pane.  Not synchronized on this on this because no local state is accessed.
    * @param message Message to display
    */
  void printMessage(String message) {
    _model.printDebugMessage(message);
  }
  
  /** Hides all of the values of the watches and their types. Called when there is no debug information.  Assumes lock
    * is already held.
    */
  private void _hideWatches() {
    for (int i = 0; i < _watches.size(); i++) {
      DebugWatchData currWatch = _watches.get(i);
      currWatch.hideValueAndType();
    }
  }
  
  /** Updates the stored value of each watched field and variable. Synchronization is necessary because this method is 
    * called from unsynchronized listeners. */
  private /* synchronized */ void _updateWatches() {
    assert EventQueue.isDispatchThread();
    if (! isReady()) return;
    
    for (DebugWatchData w : _watches) {
      String val = _model.getInteractionsModel().getVariableToString(w.getName());
      if (val == null) { w.setNoValue(); }
      else { w.setValue(val); }
      String type = _model.getInteractionsModel().getVariableType(w.getName());
      if (type == null) { w.setNoType(); }
      else { w.setType(type); }
    }
  }
  
  /** Copy the current selected thread's visible variables (those in scope) into
   * an interpreter's environment and then switch the Interactions window's
   * interpreter to that interpreter.
   */
  private void _dumpVariablesIntoInterpreterAndSwitch() throws DebugException {
    _log.log(this + " invoked dumpVariablesIntoInterpreterAndSwitch");
    List<ObjectReference> toRelease = new LinkedList<ObjectReference>();
    try {
      ThreadReference thread = _suspendedThreads.peek();
      
      // Name the new interpreter based on this thread
      String interpreterName = _getUniqueThreadName(thread);
      ObjectReference mirroredName = _mirrorString(interpreterName, toRelease);
      ObjectReference thisVal = thread.frame(0).thisObject();
      ClassObjectReference thisClass = thread.frame(0).location().declaringType().classObject();
      
      List<ObjectReference> localVars = new LinkedList<ObjectReference>();
      List<StringReference> localVarNames = new LinkedList<StringReference>();
      List<ClassObjectReference> localVarClasses = new LinkedList<ClassObjectReference>();
      try {
        // we don't store the value thread.frame(0) anywhere, because it is invalidated
        // each time we invoke a method in thread (as in _box)
        for (LocalVariable v : thread.frame(0).visibleVariables()) {
          try {
            // Get the type first, so that if an error occurs, we haven't mutated the lists.
            Type t = v.type();
            if (t instanceof ReferenceType) {
              localVarClasses.add(((ReferenceType) t).classObject());
            }
            else {
              // primitive types are represented by null
              localVarClasses.add(null);
            }
            localVarNames.add(_mirrorString(v.name(), toRelease));
            Value val = thread.frame(0).getValue(v);
            if (val == null || val instanceof ObjectReference) { localVars.add((ObjectReference) val); }
            else { localVars.add(_box((PrimitiveValue) val, thread, toRelease)); }
          }
          catch (ClassNotLoadedException e) {
            // This is a real possibility, as documented in the ClassNotLoadedException
            // javadocs.  We'll just ignore the exception, treating the variable as
            // out-of-scope, since we can't talk about values of its type.
          }
        }
      }
      catch (AbsentInformationException e) { /* ignore -- we just won't include any local variables */ }
      ArrayReference mirroredVars = _mirrorArray("java.lang.Object", localVars, thread, toRelease);
      ArrayReference mirroredVarNames = _mirrorArray("java.lang.String", localVarNames, thread, toRelease);
      ArrayReference mirroredVarClasses = _mirrorArray("java.lang.Class", localVarClasses, thread, toRelease);
      
      _invokeMethod(thread, _interpreterJVM, "addInterpreter", ADD_INTERPRETER_SIG,
                    mirroredName, thisVal, thisClass, mirroredVars, mirroredVarNames, mirroredVarClasses);
      
      // Set the new interpreter and prompt
      String prompt = _getPromptString(thread);
      _log.log(this + " is setting active interpreter");
      _model.getInteractionsModel().setActiveInterpreter(interpreterName, prompt);
    }
    catch (IncompatibleThreadStateException e) { throw new DebugException(e); }
    finally {
      for (ObjectReference ref : toRelease) { ref.enableCollection(); }
    }
  }
  
  /** @return the prompt to display in the itneractions console
   * based upon the ThreadReference threadRef, which is being debugged.
   */
  private String _getPromptString(ThreadReference threadRef) {
    return "[" + threadRef.name() + "] > ";
  }
  
  /** Create a String in the VM and prevent it from being garbage collected. */
  private StringReference _mirrorString(String s, List<ObjectReference> toRelease) throws DebugException {
    for (int tries = 0; tries < OBJECT_COLLECTED_TRIES; tries++) {
      try {
        StringReference result = _vm.mirrorOf(s);
        result.disableCollection();
        if (!result.isCollected()) {
          toRelease.add(result);
          return result;
        }
      }
      catch (ObjectCollectedException e) { /* try again */ }
    }
    throw new DebugException("Ran out of OBJECT_COLLECTED_TRIES");
  }
  
  /** Create an array of the given elements in the VM and prevent it from being garbage collected. */
  private ArrayReference _mirrorArray(String elementClass, List<? extends ObjectReference> elts,
                                      ThreadReference thread, List<ObjectReference> toRelease)
    throws DebugException {
    ClassType arrayC = (ClassType) _getClass("java.lang.reflect.Array");
    ReferenceType elementC = _getClass(elementClass);
    for (int tries = 0; tries < OBJECT_COLLECTED_TRIES; tries++) {
      try {
        ArrayReference result =
          (ArrayReference) _invokeStaticMethod(thread, arrayC, "newInstance", NEW_INSTANCE_SIG,
                                               elementC.classObject(), _vm.mirrorOf(elts.size()));
        result.disableCollection();
        if (!result.isCollected()) {
          toRelease.add(result);
          try { result.setValues(elts); }
          catch (InvalidTypeException e) { throw new DebugException(e); }
          catch (ClassNotLoadedException e) { throw new DebugException(e); }
          return result;
        }
      }
      catch (ObjectCollectedException e) { /* try again */ }
    }
    throw new DebugException("Ran out of OBJECT_COLLECTED_TRIES");
  }
  
  /** Create a boxed object corresponding to the given primitive. */
  private ObjectReference _box(PrimitiveValue val, ThreadReference thread,
                               List<ObjectReference> toRelease) throws DebugException {
    String c = null;
    String prim = null;
    if (val instanceof BooleanValue) { c = "java.lang.Boolean"; prim = "Z"; }
    else if (val instanceof IntegerValue) { c = "java.lang.Integer"; prim = "I"; }
    else if (val instanceof DoubleValue) { c = "java.lang.Double"; prim = "D"; }
    else if (val instanceof CharValue) { c = "java.lang.Character"; prim = "C"; }
    else if (val instanceof ByteValue) { c = "java.lang.Byte"; prim = "B"; }
    else if (val instanceof ShortValue) { c = "java.lang.Short"; prim = "S"; }
    else if (val instanceof LongValue) { c = "java.lang.Long"; prim = "J"; }
    else if (val instanceof FloatValue) { c = "java.lang.Float"; prim = "F"; }
    ClassType location = (ClassType) _getClass(c);
    for (int tries = 0; tries < OBJECT_COLLECTED_TRIES; tries++) {
      try {
        ObjectReference result;
        try {
          String valueOfSig = "(" + prim + ")L" + c.replace('.', '/') + ";";
          result = (ObjectReference) _invokeStaticMethod(thread, location, "valueOf",
                                                         valueOfSig, val);
        }
        catch (DebugException e) {
          // valueOf() is not available in all classes in Java 1.4
          debug.log("Can't invoke valueOf()", e);
          String consSig = "(" + prim + ")V";
          result = (ObjectReference) _invokeConstructor(thread, location, consSig, val);
        }
        
        result.disableCollection();
        if (!result.isCollected()) {
          toRelease.add(result);
          return result;
        }
      }
      catch (ObjectCollectedException e) { /* try again */ }
    }
    throw new DebugException("Ran out of OBJECT_COLLECTED_TRIES");
  }
  
  
  /** Create an unboxed primitive corresponding to the given object.
    * @throw DebugException  If the value is not of a type that can be unboxed, or if an error
    *                        occurs in the unboxing method invocation.
    */
  private PrimitiveValue _unbox(ObjectReference val, ThreadReference thread) throws DebugException {
    if (val == null) { throw new DebugException("Value can't be unboxed"); }
    String type = val.referenceType().name();
    String m = null;
    String sig = null;
    if (type.equals("java.lang.Boolean")) { m = "booleanValue"; sig = "()Z"; }
    else if (type.equals("java.lang.Integer")) { m = "intValue"; sig = "()I"; }
    else if (type.equals("java.lang.Double")) { m = "doubleValue"; sig = "()D"; }
    else if (type.equals("java.lang.Character")) { m = "charValue"; sig = "()C"; }
    else if (type.equals("java.lang.Byte")) { m = "byteValue"; sig = "()B"; }
    else if (type.equals("java.lang.Short")) { m = "shortValue"; sig = "()S"; }
    else if (type.equals("java.lang.Long")) { m = "longValue"; sig = "()J"; }
    else if (type.equals("java.lang.Float")) { m = "floatValue"; sig = "()F"; }
    
    if (m == null) { throw new DebugException("Value can't be unboxed"); }
    else { return (PrimitiveValue) _invokeMethod(thread, val, m, sig); }
  }
  
  
  /** Get a reference type corresponding to the class with the given name.
    * Note there is not necessarily a one-to-one correspondence between classes
    * and names -- every class loader can define a class with a certain name --
    * so we just pick one.  Classes defined by the bootstrap class loader have
    * priority over other classes.
    * @throws DebugException  If no loaded class has the given name.
    */
  private ReferenceType _getClass(String name) throws DebugException {
    List<ReferenceType> classes = _vm.classesByName(name);
    if (classes.isEmpty()) {
      throw new DebugException("Class '" + name + "' is not loaded");
    }
    else {
      for (ReferenceType t : classes) {
        // class loader is null iff it comes from the bootstrap loader
        if (t.classLoader() == null) { return t; }
      }
      return classes.get(0);
    }
  }
  
  
  /** Notifies all listeners that the current thread has been suspended. Synchronization is necessary because it is 
    * called from unsynchronized listeners and other classes (in same package). 
    */
  /* synchronized */ void currThreadSuspended() {
    assert EventQueue.isDispatchThread();
    try {
      _dumpVariablesIntoInterpreterAndSwitch();
      _switchToSuspendedThread();
    }
    catch(DebugException de) { throw new UnexpectedException(de); }
  }
  
  /** Calls the real switchToSuspendedThread, telling it to updateWatches. This is what is usually called. */
  private void _switchToSuspendedThread() throws DebugException { _switchToSuspendedThread(true); }
  
  /** Performs the bookkeeping to switch to the suspened thread on the top of the _suspendedThreads stack.
    * @param updateWatches  A flag that is false if the current file does not have debug information. This prevents the 
    *                       default interpreter's watch values from being shown.
    */
  private void _switchToSuspendedThread(boolean updateWatches) throws DebugException {
    _log.log(this + " executing _switchToSuspendedThread()");
    _runningThread = null;
    if (updateWatches) _updateWatches();
    final ThreadReference currThread = _suspendedThreads.peek();
    _notifier.currThreadSuspended();
    // Anytime a thread is suspended, it becomes the current thread.
    // This makes sure the debug panel will correctly put the
    // current thread in bold.
    _notifier.currThreadSet(new JPDAThreadData(currThread));
    
    try {
      if (currThread.frameCount() > 0) scrollToSource(currThread.frame(0).location());
    }
    catch (IncompatibleThreadStateException itse) {
      throw new UnexpectedException(itse);
    }
  }
  
  /** Returns a unique name for the given thread.
   */
  private String _getUniqueThreadName(ThreadReference thread) {
    return Long.toString(thread.uniqueID());
  }
  
  /** Assumes lock is already held. */
  private void _copyVariablesFromInterpreter() throws DebugException {
    // copy variables' values out of interpreter's environment and
    // into the relevant stack frame
    List<ObjectReference> toRelease = new LinkedList<ObjectReference>();
    try {
      // we don't store _runningThread.frame(0) anywhere because it is invalidated
      // every time we invoke a method in the thread (getVariable, for example)
      for (LocalVariable var : _runningThread.frame(0).visibleVariables()) {
        Value oldVal = _runningThread.frame(0).getValue(var);
        StringReference name = _mirrorString(var.name(), toRelease);
        ArrayReference wrappedVal =
          (ArrayReference) _invokeMethod(_runningThread, _interpreterJVM, "getVariable",
                                         GET_VARIABLE_SIG, name);
        if ((wrappedVal!=null) && (wrappedVal.length() == 1)) { // if it can't be found (length is 0), just ignore it
          try {
            Value val = wrappedVal.getValue(0);
            if (var.type() instanceof PrimitiveType) {
              try { val = _unbox((ObjectReference) val, _runningThread); }
              catch (DebugException e) { error.log("Can't unbox variable", e); }
            }
            if ((oldVal==null) || (!oldVal.equals(val))) {
              try { _runningThread.frame(0).setValue(var, val); }
              catch (InvalidTypeException e) { error.log("Can't set variable", e); }
              catch (ClassNotLoadedException e) { error.log("Can't set variable", e); }
            }
          }
          catch (ClassNotLoadedException e) { /* just ignore -- val must be null anyway */ }
        }
      }
    }
    catch (AbsentInformationException e) { /* can't see local variables -- just ignore */ }
    catch (IncompatibleThreadStateException e) { throw new DebugException(e); }
    finally {
      for (ObjectReference ref : toRelease) { ref.enableCollection(); }
    }
  }
  
  /** Removes all of the debug interpreters as part of shutting down.  Assumes lock is already held. */
  private void _removeAllDebugInterpreters() {
    DefaultInteractionsModel interactionsModel = _model.getInteractionsModel();
    String oldInterpreterName;
    if (_runningThread != null) {
      oldInterpreterName = _getUniqueThreadName(_runningThread);
      interactionsModel.removeInterpreter(oldInterpreterName);
    }
    while (!_suspendedThreads.isEmpty()) {
      ThreadReference threadRef = _suspendedThreads.pop();
      oldInterpreterName = _getUniqueThreadName(threadRef);
      interactionsModel.removeInterpreter(oldInterpreterName);
    }
  }
  
  /** Removes the current debug interpreter upon resuming the current thread.  Assumes lock on this is already held.
    * @param fromStep  A flat switch specifying a switch to the default interpreter since we don't want to switch to the
    * next debug interpreter and display its watch data. We would like to just not have an active interpreter and put up
    * an hourglass over the interactions pane, but the interpreterJVM must have an active interpreter.
    */
  private void _removeCurrentDebugInterpreter(boolean fromStep) {
    DefaultInteractionsModel interactionsModel =
      _model.getInteractionsModel();
    // switch to next interpreter on the stack
    if (fromStep || _suspendedThreads.isEmpty()) {
      interactionsModel.setToDefaultInterpreter();
    }
    else {
      ThreadReference threadRef = _suspendedThreads.peek();
      _switchToInterpreterForThreadReference(threadRef);
    }
    String oldInterpreterName = _getUniqueThreadName(_runningThread);
    interactionsModel.removeInterpreter(oldInterpreterName);
  }
  
  /** Notifies all listeners that the current thread has been resumed.  Unsynchronized because invokeLater runs
    * asynchronously. Precondition: assumes that the current thread hasn't yet been resumed
    */
  private void _currThreadResumed() throws DebugException {
    _log.log(this + " is executing _currThreadResumed()");
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.currThreadResumed(); } });
  }
  
  /** Switches the current interpreter to the one corresponding to threadRef.  Assumes lock on this is already held.
    * @param threadRef The ThreadRefernce corresponding to the interpreter to switch to
    */
  private void _switchToInterpreterForThreadReference(ThreadReference threadRef) {
    String threadName = _getUniqueThreadName(threadRef);
    String prompt = _getPromptString(threadRef);
    _model.getInteractionsModel().setActiveInterpreter(threadName, prompt);
  }
  
  /** Not synchronized because invokeLater is asynchronous. */
  void threadStarted() {
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.threadStarted(); } });
  }
  
  /** Notifies all listeners that the current thread has died.  updateThreads is set to true if the threads and stack
    * tables need to be updated, false if there are no suspended threads
    */
  /* synchronized */ void currThreadDied() throws DebugException {
    assert EventQueue.isDispatchThread();
//    Utilities.invokeLater(new Runnable() {
//      public void run() {
    printMessage("The current thread has finished.");
    _runningThread = null;
    
    _updateWatches();
    
    if (_suspendedThreads.size() > 0) {
      ThreadReference thread = _suspendedThreads.peek();
      _switchToInterpreterForThreadReference(thread);
      
      try {
        if (thread.frameCount() <= 0) {
          printMessage("Could not scroll to source for " + thread.name() + ". It has no stackframes.");
        }
        else scrollToSource(thread.frame(0).location());
      }
      catch(IncompatibleThreadStateException e) { throw new UnexpectedException(e); }
      
      // updates watches and makes buttons in UI active, does this because
      // there are suspended threads on the stack
      _switchToSuspendedThread();
    }
    _notifier.currThreadDied();
//      }
//    });
  }
  
  void nonCurrThreadDied() {
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.nonCurrThreadDied(); } }); 
  }
  
  /** Notifies all listeners that the debugger has shut down. updateThreads is set to true if the threads and stack 
    * tables need to be updated, false if there are no suspended threads
    */
  void notifyDebuggerShutdown() {
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.debuggerShutdown(); } });
  }
  
  /** Notifies all listeners that the debugger has started. */
  void notifyDebuggerStarted() {
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.debuggerStarted(); } });
  }
  
  /** Notifies all listeners that a step has been requested. */
  void notifyStepRequested() {
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.stepRequested(); } });
  }
  
  /** Invoke the given method, and handle any errors that may arise.  Note that the result
    * does not have garbage collection disabled; if the result is a reference 
    * that will be needed later and that is not referenced elsewhere in the VM, garbage
    * collection for it should be immediately disabled (and there's still the possibility
    * that it was collected in the mean time...)
    */
  private static Value _invokeMethod(ThreadReference thread, ObjectReference receiver, String name,
                                     String signature, Value... args) throws DebugException {
    try {
      ClassType c = (ClassType) receiver.referenceType();
      Method m = c.concreteMethodByName(name, signature);
      if (m == null) { throw new DebugException("Cannot find method '" + name + "'"); }
      return receiver.invokeMethod(thread, m, Arrays.asList(args),
                                   ObjectReference.INVOKE_SINGLE_THREADED);
    }
    catch (ClassNotPreparedException e) { throw new DebugException(e); }
    catch (IllegalArgumentException e) { throw new DebugException(e); }
    catch (ClassNotLoadedException e) { throw new DebugException(e); }
    catch (IncompatibleThreadStateException e) { throw new DebugException(e); }
    catch (InvocationException e) { throw new DebugException(e); }
    catch (InvalidTypeException e) { throw new DebugException(e); }
  }
  
  /** Invoke the given static method, and handle any errors that may arise.  Note that
    * the result does not have garbage collection disabled; if the result is a reference
    * that will be needed later and that is not referenced elsewhere in the VM, garbage
    * collection for it should be immediately disabled (and there's still the possibility
    * that it was collected in the mean time...)
    * @param signature  A method signature descriptor to match.
    *                   For example: {@code "(Ljava/lang/String;)Ljava/lang/Object;"}.
    */
  private static Value _invokeStaticMethod(ThreadReference thread, ClassType location, String name,
                                           String signature, Value... args) throws DebugException {
    try {
      Method m = location.concreteMethodByName(name, signature);
      if (m == null) { throw new DebugException("Cannot find method '" + name + "'"); }
      return location.invokeMethod(thread, m, Arrays.asList(args),
                                   ClassType.INVOKE_SINGLE_THREADED);
    }
    catch (ClassNotPreparedException e) { throw new DebugException(e); }
    catch (IllegalArgumentException e) { throw new DebugException(e); }
    catch (ClassNotLoadedException e) { throw new DebugException(e); }
    catch (IncompatibleThreadStateException e) { throw new DebugException(e); }
    catch (InvocationException e) { throw new DebugException(e); }
    catch (InvalidTypeException e) { throw new DebugException(e); }
  }
  
  /** Invoke a constructor of the given class, and handle any errors that may arise.  Note 
    * that the result does not have garbage collection disabled; if the result will be needed
    * later and is not referenced elsewhere in the VM (a likely event, since it was just
    * constructed), garbage collection for it should be immediately disabled (and there's
    * still the possibility that it was collected in the mean time...)
    * @param signature  A method signature descriptor matching the corresponding {@code <init>}
    *                   method. For example: {@code "(Ljava/lang/String;)V"}.  The return type 
    *                   will always be void.
    */
  private static Value _invokeConstructor(ThreadReference thread, ClassType location,
                                          String signature, Value... args) throws DebugException {
    try {
      Method m = location.concreteMethodByName("<init>", signature);
      if (m == null) { throw new DebugException("Cannot find requested constructor"); }
      return location.newInstance(thread, m, Arrays.asList(args), ClassType.INVOKE_SINGLE_THREADED);
    }
    catch (ClassNotPreparedException e) { throw new DebugException(e); }
    catch (IllegalArgumentException e) { throw new DebugException(e); }
    catch (ClassNotLoadedException e) { throw new DebugException(e); }
    catch (IncompatibleThreadStateException e) { throw new DebugException(e); }
    catch (InvocationException e) { throw new DebugException(e); }
    catch (InvalidTypeException e) { throw new DebugException(e); }
  }
  
  /** Get the value of the given static field, and handle any errors that may arise.  Note 
    * the result does not have garbage collection enabled; if the result is a reference
    * that will be needed later and that is not referenced elsewhere in the VM (this
    * would require that the value of the field subsequently changes), garbage
    * collection for it should be immediately disabled (and there's still the possibility
    * that it was collected in the mean time...)
    */
  private static Value _getStaticField(ReferenceType location, String name) throws DebugException {
    try {
      Field f = location.fieldByName(name);
      if (f == null) { throw new DebugException("Cannot find field '" + name + "'"); }
      return location.getValue(f);
    }
    catch (ClassNotPreparedException e) { throw new DebugException(e); }
  }
  
  
  
  
  
  /** A thread-safe stack from which you can remove any element, not just the top of the stack.  All synchronization is 
    * performed on the wrapped vector.
    * TODO: make a generic Collection extending/replacing Stack.
    */
  private static class RandomAccessStack extends Stack<ThreadReference> {
    
    public ThreadReference peekAt(int i) { return get(i); }
    
    public ThreadReference remove(long id) throws NoSuchElementException {
      synchronized(this) {
        for (int i = 0; i < size(); i++) {
          if (get(i).uniqueID() == id) {
            ThreadReference t = get(i);
            remove(i);
            return t;
          }
        }
      }
      
      throw new NoSuchElementException("Thread " + id + " not found in debugger suspended threads stack!");
    }
    
    public synchronized boolean contains(long id) {
      for (int i = 0; i < size(); i++) {
        if (get(i).uniqueID() == id) return true;
      }
      return false;
    }
    
    public boolean isEmpty() { return empty(); }
  }
}
