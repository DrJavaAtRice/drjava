/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.debug;

import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import java.util.Enumeration;
import java.util.Vector;

// DrJava stuff
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.repl.DefaultInteractionsModel;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DummySingleDisplayModelListener;
import edu.rice.cs.util.Log;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

/**
 * An integrated debugger which attaches to the Interactions JVM using
 * Sun's Java Platform Debugger Architecture (JPDA/JDI) interface.
 *
 * Every public method in this class throws an llegalStateException if
 * it is called while the debugger is not active, except for isAvailable,
 * isReady, and startup.  Public methods also throw a DebugException if
 * the EventHandlerThread has caught an exception.
 *
 * @version $Id$
 */
public class JPDADebugger implements Debugger, DebugModelCallback {
  private static final boolean printMessages = false;
  private static final int OBJECT_COLLECTED_TRIES = 5;

  /** Reference to DrJava's model. */
  private DefaultGlobalModel _model;

  /** VirtualMachine of the interactions JVM. */
  private VirtualMachine _vm;

  /** Manages all event requests in JDI. */
  private EventRequestManager _eventManager;

  /** Vector of all current Breakpoints, with and without EventRequests. */
  private Vector<Breakpoint> _breakpoints;

  /** Vector of all current Watches. */
  private Vector<DebugWatchData> _watches;

  /**
   * Keeps track of any DebugActions whose classes have not yet been
   * loaded, so that EventRequests can be created when the correct
   * ClassPrepareEvent occurs.
   */
  private PendingRequestManager _pendingRequestManager;

  /**
   * Provides a way for the JPDADebugger to communicate with the view.
   */
  private final DebugEventNotifier _notifier = new DebugEventNotifier();

  /**
   * The running ThreadReference that we are debugging.
   */
  private ThreadReference _runningThread;

  /**
   * Storage for all the threads suspended by this debugger.
   * The "current" thread is the top one on the stack.
   */
  private RandomAccessStack _suspendedThreads;

  /**
   * A handle to the interpreterJVM that we need so we can
   * populate the environment.
   */
  private ObjectReference _interpreterJVM;

  private GlobalModelListener _watchListener;

  /**
   * If not null, this field holds an error caught by the EventHandlerThread.
   */
  private Throwable _eventHandlerError;

  /**
   * A log for recording messages in a file.
   */
  protected final Log _log;

  /**
   * Builds a new JPDADebugger to debug code in the Interactions JVM,
   * using the JPDA/JDI interfaces.
   * Does not actually connect to the interpreterJVM until startup().
   */
  public JPDADebugger(DefaultGlobalModel model) {
    _model = model;
    _vm = null;
    _eventManager = null;
    _breakpoints = new Vector<Breakpoint>();
    _watches = new Vector<DebugWatchData>();
    _suspendedThreads = new RandomAccessStack();
    _pendingRequestManager = new PendingRequestManager(this);
    _runningThread = null;
    _interpreterJVM = null;
    _eventHandlerError = null;
    _log = new Log("DebuggerLog", false);

    // TO DO: Replace this with an InteractionsListener,
    //  since we really can't talk about SingleDisplayModel here!
    _watchListener = new DummySingleDisplayModelListener() {
      public void interactionEnded() {
        try {
          _updateWatches();
        }
        catch(DebugException de) {
          _log("couldn't update watches", de);
        }
      }
    };
  }

  /**
   * Adds a listener to this JPDADebugger.
   * @param listener a listener that reacts on events generated by the JPDADebugger
   */
  public void addListener(DebugListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Removes a listener to this JPDADebugger.
   * @param listener listener to remove
   */
  public void removeListener(DebugListener listener){
    _notifier.removeListener(listener);
  }

  protected VirtualMachine getVM() {
    return _vm;
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
   * Returns whether the debugger can be used in this copy of DrJava.
   * This does not indicate whether it is ready to be used, which is
   * indicated by isReady().
   */
  public boolean isAvailable() {
    return true;
  }

  /**
   * Returns whether the debugger is currently in an active debugging
   * session.  This method will return false if the debugger has not
   * been initialized through startup().
   */
  public synchronized boolean isReady() {
    return _vm != null;
  }

  /**
   * Ensures that the debugger is active.  Should be called by every
   * public method in the debugger except for startup().
   * @throws IllegalStateException if debugger is not active
   * @throws DebugException if an exception was detected in the EventHandlerThread
   */
  protected synchronized void _ensureReady() throws DebugException {
    if (!isReady()) {
      throw new IllegalStateException("Debugger is not active.");
    }
    if (_eventHandlerError != null) {
      Throwable t = _eventHandlerError;
      _eventHandlerError = null;
      throw new DebugException("Error in Debugger Event Handler: " + t);
    }
  }

  /**
   * Records that an error occurred in the EventHandlerThread.
   * The next call to _ensureReady() will fail, indicating that the
   * error occurred.
   * @param t Error occurring in the EventHandlerThread
   */
  synchronized void eventHandlerError(Throwable t) {
    _log("Error in EventHandlerThread: " + t);
    _eventHandlerError = t;
  }

  /**
   * Attaches the debugger to the Interactions JVM to prepare for debugging.
   */
  public synchronized void startup() throws DebugException {
    if (!isReady()) {
      // check if all open documents are in sync
      List<OpenDefinitionsDocument> list = _model.getDefinitionsDocuments();
      for (int i = 0; i < list.size(); i++) {
        OpenDefinitionsDocument currDoc = list.get(i);
        currDoc.checkIfClassFileInSync();
      }

      _attachToVM();

      // Listen for events when threads die
      ThreadDeathRequest tdr = _eventManager.createThreadDeathRequest();
      tdr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      tdr.enable();

      // Listen for events from JPDA in a new thread
      EventHandlerThread eventHandler = new EventHandlerThread(this, _vm);
      eventHandler.start();
      _model.addListener(_watchListener);
    }

    else {
      // Already started
      throw new IllegalStateException("Debugger has already been started.");
    }
  }

  /**
   * Handles the details of attaching to the interpreterJVM.
   */
  private void _attachToVM() throws DebugException {
    // Blocks until the interpreter has registered if hasn't already
    _model.waitForInterpreter();

    // Get the connector
    AttachingConnector connector = _getAttachingConnector();

    // Try to connect on our debug port
    Map args = connector.defaultArguments();
    Connector.Argument port = (Connector.Argument) args.get("port");
    Connector.Argument host = (Connector.Argument) args.get("hostname");
    try {
      int debugPort = _model.getDebugPort();
      port.setValue("" + debugPort);
      host.setValue("127.0.0.1"); // necessary if hostname can't be resolved
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
    }
    catch (IOException ioe) {
      throw new DebugException("Could not connect to VM: " + ioe);
    }
    catch (IllegalConnectorArgumentsException icae) {
      throw new DebugException("Could not connect to VM: " + icae);
    }

    _interpreterJVM = _getInterpreterJVMRef();
  }

  /**
   * Returns an attaching connector to use for connecting to the
   * interpreter JVM.
   */
  protected AttachingConnector _getAttachingConnector()
    throws DebugException
  {
    VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
    List<AttachingConnector> connectors = vmm.attachingConnectors();  // Added parameterization <AttachingConnector>. JDK 1.5 will eliminate this check
    AttachingConnector connector = null;
    java.util.Iterator<AttachingConnector> iter = connectors.iterator();
    while (iter.hasNext()) {
      AttachingConnector conn = iter.next();
      if (conn.name().equals("com.sun.jdi.SocketAttach")) {
        connector = conn;
      }
    }
    if (connector == null) {
      throw new DebugException("Could not find an AttachingConnector!");
    }
    return connector;
  }

  /**
   * Returns an ObjectReference to the singleton instance of
   * the InterpreterJVM class in the virtual machine being debugged.
   * This is used to mainupulate interpreters at breakpoints.
   */
  protected ObjectReference _getInterpreterJVMRef()
    throws DebugException
  {
    String className = "edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM";
    List<ReferenceType> referenceTypes = _vm.classesByName(className);  // Added parameterization <ReferenceType>. JDK 1.5 will eliminate this warning
    if (referenceTypes.size() > 0) {
      ReferenceType rt = referenceTypes.get(0);
      Field field = rt.fieldByName("ONLY");
      if (field == null) {
        throw new DebugException("Unable to get ONLY field");
      }
      else {
        return (ObjectReference) rt.getValue(field);
      }
    }
    else {
      throw new DebugException("Could not get a reference to interpreterJVM");
    }
  }

  /**
   * Disconnects the debugger from the Interactions JVM and cleans up
   * any state.
   * @throws IllegalStateException if debugger is not ready
   */
  public synchronized void shutdown() {
    if (!isReady()) {
      throw new IllegalStateException("Cannot shut down if debugger is not active.");
    }

    _model.removeListener(_watchListener);

    try {
      _removeAllDebugInterpreters();
      removeAllBreakpoints();
      removeAllWatches();
    }
    catch (DebugException de) {
      // Couldn't remove breakpoints/watches
      _log("Could not remove breakpoints/watches: " + de);
    }

    try {
      _vm.dispose();
    }
    catch (VMDisconnectedException vmde) {
      //VM was shutdown prematurely
    }
    finally {
      _model.getInteractionsModel().setToDefaultInterpreter();
      _vm = null;
      _suspendedThreads = new RandomAccessStack();
      _eventManager = null;
      _runningThread = null;
    }
  }


  /**
   * Returns the current EventRequestManager from JDI, or null if
   * startup() has not been called.
   */
  synchronized EventRequestManager getEventRequestManager() {
    return _eventManager;
  }

  /**
   * Returns the pending request manager used by the debugger.
   */
  synchronized PendingRequestManager getPendingRequestManager() {
    return _pendingRequestManager;
  }

  /**
   * Sets the debugger's currently active thread.
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
  synchronized boolean setCurrentThread(ThreadReference thread) {
    if (!thread.isSuspended()) {
      throw new IllegalArgumentException("Thread must be suspended to set " +
                                         "as current.  Given: " + thread);
    }

    try {
      if ((_suspendedThreads.isEmpty() ||
           !_suspendedThreads.contains(thread.uniqueID()))
            && (thread.frameCount() > 0)) {
        _suspendedThreads.push(thread);

        return true;
      }
      else {
        return false;
      }
    }
    catch (IncompatibleThreadStateException itse) {
      // requesting stack frames should be fine, since the thread must be
      // suspended or frameCount() is not called
      throw new UnexpectedException(itse);
    }
  }

  /**
   * Sets the notion of current thread to the one contained in threadData.
   * The thread must be suspended.
   * (Note: the intention is for this method to suspend the thread if
   * necessary, but this is not yet implemented.  The catch is that any
   * manually suspended threads won't cooperate with the debug interpreters;
   * the thread must be suspended by a breakpoint or step.)
   * @param threadData Thread to set as current
   * @throws IllegalStateException if debugger is not ready
   * @throws IllegalArgumentException if threadData is null or not suspended
   */
  public synchronized void setCurrentThread(DebugThreadData threadData)
    throws DebugException
  {
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
    if (_suspendedThreads.contains(threadRef.uniqueID())) {
      _suspendedThreads.remove(threadRef.uniqueID());
    }
    if (!threadRef.isSuspended()) {
      throw new IllegalArgumentException("Given thread must be suspended.");
//       threadRef.suspend();
//
//       try{
//         if( threadRef.frameCount() <= 0 ) {
//           printMessage(threadRef.name() + " could not be suspended. It had no stackframes.");
//           _suspendedThreads.push(threadRef);
//           resume();
//           return;
//         }
//       }
//       catch(IncompatibleThreadStateException ex){
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
        printMessage(threadRef.name() +
                     " could not be suspended since it has no stackframes.");
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

  /**
   * Returns the currently selected thread for the debugger.
   */
  synchronized ThreadReference getCurrentThread() {
    // Current thread is the top one on the stack
    return _suspendedThreads.peek();
  }

  /**
   * Returns the suspended thread at the current index of the stack.
   * @param i index into the stack of suspended threads
   */
  synchronized ThreadReference getThreadAt(int i) {
    return _suspendedThreads.peekAt(i);
  }

  /**
   * Returns the running thread currently tracked by the debugger.
   */
  synchronized ThreadReference getCurrentRunningThread() {
    return _runningThread;
  }

  /**
   * Returns whether the debugger currently has any suspended threads.
   */
  public synchronized boolean hasSuspendedThreads() throws DebugException {
    _ensureReady();
    return _suspendedThreads.size() > 0;
  }


  /**
   * Returns whether the debugger's current thread is suspended.
   */
  public synchronized boolean isCurrentThreadSuspended() throws DebugException {
    _ensureReady();
    return hasSuspendedThreads() && !hasRunningThread();
  }

  /**
   * Returns whether the thread the debugger is tracking is now running.
   */
  public synchronized boolean hasRunningThread() throws DebugException {
    _ensureReady();
    return _runningThread != null;
  }

  /**
   * Returns a Vector with all the loaded ReferenceTypes for the given class
   * name (empty if the class could not be found).  Makes no attempt to load
   * the class if it is not already loaded.
   * <p>
   * If custom class loaders are in use, multiple copies of the class may
   * be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className) {
    return getReferenceTypes(className, DebugAction.ANY_LINE);
  }

  /**
   * Returns a Vector with the loaded ReferenceTypes for the given class name
   * (empty if the class could not be found).  Makes no attempt to load the
   * class if it is not already loaded.  If the lineNumber is not
   * DebugAction.ANY_LINE, this method ensures that the returned ReferenceTypes
   * contain the given lineNumber, searching through inner classes if necessary.
   * If no inner classes contain the line number, an empty Vector is returned.
   * <p>
   * If custom class loaders are in use, multiple copies of the class
   * may be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className,
                                                       int lineNumber) {
    // Get all classes that match this name
    List<ReferenceType> classes;
    try {
      classes = _vm.classesByName(className);  // Added parameterization <ReferenceType> to classes. JDK 1.5 will eliminate this type warning
    }
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
        List<Location> lines = new LinkedList();   // Added parameterization <Location> to lines
        try {
          lines = ref.locationsOfLine(lineNumber); // JDK 1.5 will eliminate this type warning
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
          List<ReferenceType> innerRefs = ref.nestedTypes();  // Added parameterization <ReferenceType>. JDK 1.5 will eliminate this type warning
          ref = null;
          for (int j = 0; j < innerRefs.size(); j++) {
            try {
              ReferenceType currRef = innerRefs.get(j);
              lines = currRef.locationsOfLine(lineNumber);  // JDK 1.5 will eliminate this type warning
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

  /**
   * @return The thread in the virtual machine with name d.uniqueID()
   * @throws NoSuchElementException if the thread could not be found
   */
  protected ThreadReference _getThreadFromDebugThreadData(DebugThreadData d)
    throws NoSuchElementException
  {
    List<ThreadReference> threads = _vm.allThreads(); // Added parameterization <ThreadReference>. JDK 1.5 will eliminate this type warning
    Iterator<ThreadReference> iterator = threads.iterator();
    while (iterator.hasNext()) {
      ThreadReference threadRef = iterator.next();
      if (threadRef.uniqueID() == d.getUniqueID()) {
        return threadRef;
      }
    }
    // Thread not found
    throw new NoSuchElementException("Thread " + d.getName() +
                                     " not found in virtual machine!");
  }

  /**
   * Suspends all the currently running threads in the virtual machine.
   *
   * Not currently in use/available, since it is incompatible with
   * the debug interpreters.
   *
  public synchronized void suspendAll() {
    _ensureReady();
    List threads = _vm.allThreads();
    Iterator iterator = threads.iterator();
    ThreadReference threadRef = null;

    while(iterator.hasNext()){
      threadRef = (ThreadReference)iterator.next();

      if( !threadRef.isSuspended() ){
        threadRef.suspend();
        _suspendedThreads.push(threadRef);
      }
    }
    _runningThread = null;
  }*/

  /**
   * Suspends execution of the thread referenced by threadData.
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

  /**
   * Resumes the thread currently being debugged without
   * removing the debug interpreter or switching to the
   * next suspended thread.
   */
  protected synchronized void _resumeFromStep()
    throws DebugException
  {
    _resumeHelper(true);
  }

  /**
   * Resumes the thread currently being debugged, copying back all variables
   * from the current debug interpreter.
   */
  public synchronized void resume() throws DebugException {
    _ensureReady();
    _resumeHelper(false);
  }

  /**
   * Resumes execution of the currently suspended thread.
   * @param fromStep Whether to copy back the variables from
   * the current debug interpreter and switch to the next
   * suspended thread.
   */
  protected synchronized void _resumeHelper(boolean fromStep)
    throws DebugException
  {
    try {
      ThreadReference thread = _suspendedThreads.pop();

      if (printMessages) {
        System.out.println("In resumeThread()");
      }
      _resumeThread(thread, fromStep);
    }
    catch (NoSuchElementException e) {
      throw new DebugException("No thread to resume.");
    }
  }

  /**
   * Resumes the given thread, copying back any variables from its
   * associated debug interpreter.
   * @param threadData Thread to resume
   */
  public synchronized void resume(DebugThreadData threadData)
    throws DebugException
  {
    _ensureReady();
    ThreadReference thread = _suspendedThreads.remove(threadData.getUniqueID());
    _resumeThread(thread, false);
  }

  /**
   * Resumes the given thread, only copying variables from its debug interpreter
   * if shouldCopyBack is true.
   * @param thread Thread to resume
   * @param fromStep Whether to copy back the variables from
   * the current debug interpreter and switch to the next
   * suspended thread.
   * @throws IllegalArgumentException if thread is null
   */
  private void _resumeThread(ThreadReference thread, boolean fromStep)
    throws DebugException
  {
    if (thread == null) {
      throw new IllegalArgumentException("Cannot resume a null thread");
    }

    int suspendCount = thread.suspendCount();
    if (printMessages) {
      System.out.println("Getting suspendCount = " + suspendCount);
    }

    _runningThread = thread;
    if (!fromStep) {
      // Copy variables back into the thread
      _copyVariablesFromInterpreter();
      _updateWatches();
    }
    try {
      _removeCurrentDebugInterpreter(fromStep);
      currThreadResumed();
    }
    catch(DebugException e) {  //??
      throw new UnexpectedException(e);
    }

    // Must resume the correct number of times
    for (int i=suspendCount; i>0; i--) {
      thread.resume();
    }

    // Notify listeners of a resume

    // Switch to next suspended thread, if any
    if (!fromStep && !_suspendedThreads.isEmpty()) {
      _switchToSuspendedThread();
    }
  }

  /**
   * Steps the currently suspended thread.
   * @param flag The flag denotes what kind of step to take.
   * The following are the valid options:
   * StepRequest.STEP_INTO, StepRequest.STEP_OVER, StepRequest.STEP_OUT
   */
  public synchronized void step(int flag) throws DebugException {
    _ensureReady();
    _stepHelper(flag, true);
  }

  /**
   * Performs a step in the currently suspended thread, only
   * generating a step event if shouldNotify if true.
   * @param flag The type of step to perform (see step())
   * @param shouldNotify Whether to generate a step event
   */
  private synchronized void _stepHelper(int flag, boolean shouldNotify)
    throws DebugException
  {
    if (_suspendedThreads.size() <= 0 || _runningThread != null) {
      throw new IllegalStateException("Cannot step if the current thread is not suspended.");
    }

    if (printMessages) {
      System.out.println("About to peek...");
    }

    ThreadReference thread = _suspendedThreads.peek();
    if (printMessages) {
      System.out.println("Stepping " + thread.toString());
    }

    // Copy the variables back into the thread from the appropriate interpreter.
    // We do this before stepping since DrJava will hang if you try to copy back
    // variables after creating the step request.
    _runningThread = thread;
    _copyVariablesFromInterpreter();

    if (printMessages) {
      System.out.println("Deleting pending requests...");
    }

    // If there's already a step request for the current thread, delete
    //  it first
    List<StepRequest> steps = _eventManager.stepRequests();  // Added parameterization <StepRequest>. JDK 1.5 will eliminate this type warning
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = steps.get(i);
      if (step.thread().equals(thread)) {
        _eventManager.deleteEventRequest(step);
        break;
      }
    }

    if (printMessages) System.out.println("Issued step request");
    //Step step =
    new Step(this, StepRequest.STEP_LINE, flag);
    if (shouldNotify) {
      notifyStepRequested();
    }
    if (printMessages) System.out.println("About to resume");
    _resumeFromStep();
  }


  /**
   * Adds a watch on the given field or variable.
   * @param field the name of the field we will watch
   */
  public synchronized void addWatch(String field) throws DebugException {
    _ensureReady();

    _watches.add(new DebugWatchData(field));
    _updateWatches();
  }

  /**
   * Removes any watches on the given field or variable.
   * Has no effect if the given field is not being watched.
   * @param field the name of the field we will watch
   */
  public synchronized void removeWatch(String field) throws DebugException {
    _ensureReady();

    for (int i=0; i < _watches.size(); i++) {
      DebugWatchData watch = _watches.get(i);
      if (watch.getName().equals(field)) {
        _watches.remove(i);
      }
    }
  }

  /**
   * Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public synchronized void removeWatch(int index) throws DebugException {
    _ensureReady();

    if (index < _watches.size()) {
      _watches.remove(index);
    }
  }

  /**
   * Removes all watches on existing fields and variables.
   */
  public synchronized void removeAllWatches() throws DebugException {
    _ensureReady();
    _watches.clear();
  }


  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param offset Start offset on the line to set the breakpoint
   * @param lineNum Line on which to set or remove the breakpoint
   */
  public synchronized void toggleBreakpoint(OpenDefinitionsDocument doc,
                                            int offset, int lineNum)
    throws DebugException
  {
    _ensureReady();

    Breakpoint breakpoint = doc.getBreakpointAt(offset);
    if (breakpoint == null) {
      setBreakpoint(new Breakpoint (doc, offset, lineNum, this));
    }
    else {
      removeBreakpoint(breakpoint);
    }
  }

  /**
   * Sets a breakpoint.
   *
   * @param breakpoint The new breakpoint to set
   */
  public synchronized void setBreakpoint(final Breakpoint breakpoint)
    throws DebugException
  {
    _ensureReady();

    breakpoint.getDocument().checkIfClassFileInSync();

    _breakpoints.add(breakpoint);
    breakpoint.getDocument().addBreakpoint(breakpoint);

    _notifier.breakpointSet(breakpoint);
  }

 /**
  * Removes a breakpoint.
  * Called from toggleBreakpoint -- even with BPs that are not active.
  * @param breakpoint The breakpoint to remove.
  */
  public synchronized void removeBreakpoint(final Breakpoint breakpoint)
    throws DebugException
  {
    _ensureReady();

    _breakpoints.remove(breakpoint);

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
    breakpoint.getDocument().removeBreakpoint(breakpoint);

    _notifier.breakpointRemoved(breakpoint);
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public synchronized void removeAllBreakpoints() throws DebugException {
    _ensureReady();

    while (_breakpoints.size() > 0) {
      removeBreakpoint( _breakpoints.get(0));
    }
  }

  /**
   * Called when a breakpoint is reached.  The Breakpoint object itself
   * should be stored in the "debugAction" property on the request.
   * @param request The BreakPointRequest reached by the debugger
   */
  synchronized void reachedBreakpoint(BreakpointRequest request) {
    Object property = request.getProperty("debugAction");
    if ( (property != null) && (property instanceof Breakpoint) ) {
      final Breakpoint breakpoint = (Breakpoint) property;
      printMessage("Breakpoint hit in class " +
                               breakpoint.getClassName() + "  [line " +
                               breakpoint.getLineNumber() + "]");

      _notifier.breakpointReached(breakpoint);
    }
    else {
      // A breakpoint we didn't set??
      _log("Reached a breakpoint without a debugAction property: " + request);
    }
  }

  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public synchronized Vector<Breakpoint> getBreakpoints() throws DebugException {
    _ensureReady();

    Vector<Breakpoint> sortedBreakpoints = new Vector<Breakpoint>();
    List<OpenDefinitionsDocument> docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.size(); i++) {
      Vector<Breakpoint> docBreakpoints =
        docs.get(i).getBreakpoints();
      for (int j = 0; j < docBreakpoints.size(); j++) {
        sortedBreakpoints.add(docBreakpoints.get(j));
      }
    }
    return sortedBreakpoints;
  }

  /**
   * Prints the list of all breakpoints as a message in DrJava's Interactions
   * Pane.  Both pending and resolved breakpoints are listed.
   */
  public synchronized void printBreakpoints() throws DebugException {
    _ensureReady();

    Enumeration<Breakpoint> breakpoints = getBreakpoints().elements();
    if (breakpoints.hasMoreElements()) {
      printMessage("Breakpoints: ");
      while (breakpoints.hasMoreElements()) {
        Breakpoint breakpoint = breakpoints.nextElement();
        printMessage("  " + breakpoint.getClassName() +
                                 "  [line " + breakpoint.getLineNumber() + "]");
      }
    }
    else {
      printMessage("No breakpoints set.");
    }
  }

  /**
   * Returns all currently watched fields and variables.
   */
  public synchronized Vector<DebugWatchData> getWatches() throws DebugException {
    _ensureReady();
    return _watches;
  }

  /**
   * Returns a list of all threads being tracked by the debugger.
   * Does not return any threads known to be dead.
   */
  public synchronized Vector<DebugThreadData> getCurrentThreadData()
    throws DebugException
  {
    _ensureReady();
    List<ThreadReference> listThreads; // Add parameterization <ThreadReference> to listThreads.
    try {
      listThreads = _vm.allThreads();  // JDK 1.5 will eliminate this type warning
    }
    catch (VMDisconnectedException vmde) {
      // We're quitting, just pass back an empty Vector
      return new Vector<DebugThreadData>();
    }

    // get an iterator from the list returned by _vm.allThreads()
    Iterator<ThreadReference> iter = listThreads.iterator(); // Added parameterization <ThreadReference>.
    Vector<DebugThreadData> threads = new Vector<DebugThreadData>();
    while (iter.hasNext()) {
      try {
        threads.add(new DebugThreadData(iter.next()));
      }
      catch (ObjectCollectedException e) {
        // this thread just died, we don't want to list it anyway
      }
    }
    return threads;
  }

  /**
   * Returns a Vector of DebugStackData for the current suspended thread.
   * @throws DebugException if the current thread is running or there
   * are no suspended threads
   * TO DO: Config option for hiding DrJava subset of stack trace
   */
  public synchronized Vector<DebugStackData> getCurrentStackFrameData()
    throws DebugException
  {
    _ensureReady();

    if (_runningThread != null || _suspendedThreads.size() <= 0) {
      throw new DebugException("No suspended thread to obtain stack frames.");
    }

    try {
      ThreadReference thread = _suspendedThreads.peek();
      Iterator<StackFrame> iter = thread.frames().iterator();  // Added <StackFrame> parameterization; warning will go away in JDK 1.5
      Vector<DebugStackData> frames = new Vector<DebugStackData>();
      while (iter.hasNext()) {
        frames.add(new DebugStackData(iter.next()));
      }
      return frames;
    }
    catch (IncompatibleThreadStateException itse) {
      throw new DebugException("Unable to obtain stack frame: " + itse);
    }
    catch (VMDisconnectedException vmde) {
      _log("VMDisconnected when getting the current stack frame data.", vmde);
      return new Vector<DebugStackData>();
    }
  }

  /**
   * Takes the location of event e, opens the document corresponding to its class
   * and centers the definition pane's view on the appropriate line number
   * @param e LocatableEvent containing location to display
   */
  synchronized void scrollToSource(LocatableEvent e) {
    Location location = e.location();

    // First see if doc is stored
    EventRequest request = e.request();
    Object docProp = request.getProperty("document");
    if ((docProp != null) && (docProp instanceof OpenDefinitionsDocument)) {
      openAndScroll((OpenDefinitionsDocument) docProp, location, true);
    }
    else {
      scrollToSource(location);
    }
  }

  /**
   * Scroll to the location specified by location
   */
  synchronized void scrollToSource(Location location) {
    scrollToSource(location, true);
  }

  /**
   * Scroll to the location specified by location
   */
  synchronized void scrollToSource(Location location, boolean shouldHighlight) {
    OpenDefinitionsDocument doc = null;

    // No stored doc, look on the source root set (later, also the sourcepath)
    ReferenceType rt = location.declaringType();
    String filename;
    try {
      filename = getPackageDir(rt.name()) + rt.sourceName();
    }
    catch (AbsentInformationException aie) {
      // Don't know real source name:
      //   assume source name is same as file name
      String className = rt.name();
      String ps = System.getProperty("file.separator");
      // replace periods with the System's file separator
      className = StringOps.replace(className, ".", ps);

      // crop off the $ if there is one and anything after it
      int indexOfDollar = className.indexOf('$');
      if (indexOfDollar > -1) {
        className = className.substring(0, indexOfDollar);
      }

      filename = className + ".java";
    }

    // Check source root set (open files)
    File f = _model.getSourceFile(filename);
//    File[] sourceRoots = _model.getSourceRootSet();
//    Vector<File> roots = new Vector<File>();
//    for (int i=0; i < sourceRoots.length; i++) {
//      roots.add(sourceRoots[i]);
//    }
//    File f = _model.getSourceFileFromPaths(filename, roots);
//    if (f == null) {
//      Vector<File> sourcepath =
//        DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
//      f = _model.getSourceFileFromPaths(filename, sourcepath);
//    }

    if (f != null) {
      // Get a document for this file, forcing it to open
      try {
        doc = _model.getDocumentForFile(f);
      }
      catch (IOException ioe) {
        // No doc, so don't notify listener
      }
    }

    openAndScroll(doc, location, shouldHighlight);
  }

  /**
   * Scrolls to the source location specified by the the debug stack data.
   * @param stackData Stack data containing location to display
   * @throws DebugException if current thread is not suspended
   */
  public synchronized void scrollToSource(DebugStackData stackData)
    throws DebugException
  {
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
      i = threadRef.frames().iterator(); // JDK 1.5 will eliminate this warning
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

  /**
   * Scrolls to the source of the given breakpoint.
   * @param bp the breakpoint
   */
  public synchronized void scrollToSource(Breakpoint bp) {
    openAndScroll(bp.getDocument(), bp.getLineNumber(), bp.getClassName(), false);
  }

  /**
   * Gets the Breakpoint object at the specified line in the given class.
   * If the given data do not correspond to an actual breakpoint, null is returned.
   * @param line the line number of the breakpoint
   * @param className the name of the class the breakpoint's in
   * @return the Breakpoint corresponding to the line and className, or null if
   *         there is no such breakpoint.
   */
  public Breakpoint getBreakpoint(int line, String className) {
    for (int i = 0; i < _breakpoints.size(); i++) {
      Breakpoint bp = _breakpoints.get(i);
      if ((bp.getLineNumber() == line) && (bp.getClassName().equals(className))) {
        return bp;
      }
    }
    // bp not found in the list of breakpoints
    return null;
  }

  /**
   * Opens a document and scrolls to the appropriate location.  If
   * doc is null, a message is printed indicating the source file
   * could not be found.
   * @param doc Document to open
   * @param location Location to display
   */
  synchronized void openAndScroll(OpenDefinitionsDocument doc,
                                  Location location,
                                  boolean shouldHighlight) {
    openAndScroll(doc, location.lineNumber(), location.declaringType().name(), shouldHighlight);
  }

  /**
   * Opens a document and scrolls to the appropriate location.  If
   * doc is null, a message is printed indicating the source file
   * could not be found.
   * @param doc Document to open
   * @param line the line number to display
   * @param className the name of the appropriate class
   */
  synchronized void openAndScroll(final OpenDefinitionsDocument doc, final int line,
                                  String className, final boolean shouldHighlight) {
    // Open and scroll if doc was found
    if (doc != null) {
      doc.checkIfClassFileInSync();
      // change UI if in sync in MainFrame listener

      _notifier.threadLocationUpdated(doc, line, shouldHighlight);
    }
    else {
      printMessage("  (Source for " + className + " not found.)");
    }
  }

  /**
   * Returns the relative directory (from the source root) that the source
   * file with this qualifed name will be in, given its package.
   * Returns the empty string for classes without packages.
   *
   * TO DO: Move this to a static utility class
   * @param className The fully qualified class name
   */
  String getPackageDir(String className) {
    // Only keep up to the last dot
    int lastDotIndex = className.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // No dots, so no package
      return "";
    }
    else {
      String packageName = className.substring(0, lastDotIndex);
      // replace periods with the System's file separator
      String ps = System.getProperty("file.separator");
      packageName = StringOps.replace(packageName, ".", ps);
      return packageName + ps;
    }
  }

  /**
   * Prints a message in the Interactions Pane.
   * @param message Message to display
   */
  synchronized void printMessage(String message) {
    _model.printDebugMessage(message);
  }

  /**
   * Returns whether the given className corresponds to a class
   * that is anonymous or has an anonymous enclosing class.
   * @param rt the ReferenceType to check
   * @return whether the class is anonymous
   */
  private boolean hasAnonymous(ReferenceType rt) {
    String className = rt.name();
    StringTokenizer st = new StringTokenizer(className, "$");
    while (st.hasMoreElements()) {
      String currToken = st.nextToken();
      try {
        Integer anonymousNum = Integer.valueOf(currToken);
        return true;
      }
      catch(NumberFormatException nfe) {
        // flow through to false if token cannot be parsed into an int
      }
    }
    return false;
  }

  private boolean _getWatchFromInterpreter(DebugWatchData currWatch) {
    String currName = currWatch.getName();
    // get the value and type from the interactions model
    String value = _model.getInteractionsModel().getVariableToString(currName);
    if (value != null) {
      String type = _model.getInteractionsModel().getVariableClassName(currName);
      currWatch.setValue(value);
      currWatch.setType(type);
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Hides all of the values of the watches and their types. Called
   * when there is no debug information.
   */
  private synchronized void _hideWatches() {
    for (int i = 0; i < _watches.size(); i++) {
      DebugWatchData currWatch = _watches.get(i);
      currWatch.hideValueAndType();
    }
  }

  /**
   * Updates the stored value of each watched field and variable.
   */
  private synchronized void _updateWatches() throws DebugException {
    _ensureReady();
    if (_suspendedThreads.size() <= 0) {
      // Not suspended, get values in interpreter
      for (int i = 0; i < _watches.size(); i++) {
        DebugWatchData currWatch = _watches.get(i);
        if (!_getWatchFromInterpreter(currWatch)) {
          currWatch.hideValueAndType();
        }
      }
      return;
//      for (int i = 0; i < _watches.size(); i++) {
//        DebugWatchData currWatch = _watches.get(i);
//        currWatch.hideValueAndType();
//      }
//      return;
    }

    try {
      StackFrame currFrame;
      List<StackFrame> frames;
      ThreadReference thread = _suspendedThreads.peek();
      if (thread.frameCount() <= 0 ) {
        printMessage("Could not update watch values. The current thread " +
                     "had no stack frames.");
        return;
      }
      frames = thread.frames(); // JDK 1.5 will eliminate this warning
      currFrame = frames.get(0);
      Location location = currFrame.location();

      ReferenceType rt = location.declaringType();
      ObjectReference obj = currFrame.thisObject();
      // note: obj is null if we're in a static context

      // Get the name to determine how many $'s there are
      String rtName = rt.name();
      int numDollars = 0;
      int dollarIndex = rtName.indexOf("$", 0);
      while (dollarIndex != -1) {
        numDollars++;
        dollarIndex = rtName.indexOf("$", dollarIndex+1);
      }

      for (int i = 0; i < _watches.size(); i++) {
        DebugWatchData currWatch = _watches.get(i);
        String currName = currWatch.getName();
        if (_getWatchFromInterpreter(currWatch)) {
          continue;
        }
//        // check for "this"
//        if (currName.equals("this")) {
//          if (obj != null) {
//            currWatch.setValue(_getValue(obj));
//            currWatch.setType(String.valueOf(obj.type()));
//          }
//          else {
//            // "this" is not defined in a static context
//            currWatch.setNoValue();
//            currWatch.setNoType();
//          }
//          continue;
//        }

//        // Look for a variable with this name
//        LocalVariable localVar = null;
//        try {
//          frames = thread.frames();
//          currFrame = (StackFrame) frames.get(0);
//          localVar = currFrame.visibleVariableByName(currName);
//        }
//        catch (AbsentInformationException aie) {
//          // Not compiled with debug flag.... ignore
//        }
//        catch (InvalidStackFrameException isfe) {
//          currWatch.setNoValue();
//          currWatch.setNoType();
//          _log("Failed to get local var from stack frame", isfe);
//          continue;
//        }
//
//        if (localVar != null) {
//          // currWatch.setValue(_getValue(currFrame.getValue(localVar)));
//          try {
//            Value v = _getValueOfLocalVariable(localVar, thread);
//            if (v == null) {
//              currWatch.setValue(_getValue(null));
//              try {
//                currWatch.setType(localVar.type().name());
//              }
//              catch (ClassNotLoadedException cnle) {
//                List classes = _vm.classesByName(localVar.typeName());
//                if (!classes.isEmpty()) {
//                  currWatch.setType(((Type)classes.get(0)).name());
//                }
//                else {
//                  currWatch.setTypeNotLoaded();
//                }
//              }
//            }
//            else {
//              currWatch.setValue(_getValue(v));
//              currWatch.setType(v.type().name());
//            }
//          }
//          catch (Exception ex) {
//            _log("Exception when getting the value of a local variable", ex);
//            currWatch.setNoValue();
//            currWatch.setNoType();
//          }
//        }
        // if the variable being watched is not a local variable,
        //  check if it's a field
        ReferenceType outerRt = rt;
        ObjectReference outer = obj;  // (null if static context)
        Field field = outerRt.fieldByName(currName);

        if (obj != null) {
          // We're not in a static context

          // If we don't find it in this class, loop through any enclosing
          // classes. Start at this$N, where N is the number of dollar signs in
          // the reference type's name, minus one.
          int outerIndex = numDollars - 1;
          if (hasAnonymous(outerRt)) {
            // We don't know the appropriate this$N to look for so we have to
            // search for a field that begins with this$.
            List<Field> fields = outerRt.allFields();  // This type warning will go away in JDK 1.5
            Iterator<Field> iter = fields.iterator();
            while (iter.hasNext()) {
              Field f = iter.next();
              String name = f.name();
              if (name.startsWith("this$")) {
                int lastIndex = name.lastIndexOf("$");
                outerIndex = Integer.valueOf(name.substring(lastIndex+1, name.length())).intValue();
                break;
              }
            }
          }
          Field outerThis = outerRt.fieldByName("this$" + outerIndex);
          if (field == null) {
            // Try concatenating "val$" to the beginning of the field in
            // case it's a final local variable of the outer class
            field = outerRt.fieldByName("val$" + currName);
          }

          while ((field == null) && (outerThis != null)) {
            outer = (ObjectReference) outer.getValue(outerThis);
            if (outer == null) {
              // We're probably in the constructor and this$N has
              // not yet been initialized. We can't do anything, so just
              // break display no value.
              break;
            }
            outerRt = outer.referenceType();
            field = outerRt.fieldByName(currName);

            if (field == null) {
              // Try concatenating "val$" to the beginning of the field in
              // case it's a final local variable of the outer class
              field = outerRt.fieldByName("val$" + currName);

              if (field == null) {
                // Enter the loop again with the next outer enclosing class
                outerIndex--;
                outerThis = outerRt.fieldByName("this$" + outerIndex);
              }
            }
          }
        }
        else {
          // We're in a static context

          // If we don't find it in this class, loop through any enclosing
          // classes. Do this by loading any outer classes by invoking the
          // method on the class loader that loaded this class and passing
          // it the class name with the last class removed each time.
          String rtClassName = outerRt.name();
          int index = rtClassName.lastIndexOf("$");
          while ((field == null) && (index != -1)) {
            rtClassName = rtClassName.substring(0, index);
            List<ReferenceType> l = _vm.classesByName(rtClassName); // JDK 1.5 will eliminate this warning
            if (l.isEmpty()) {
              // field is null, we will end up setting
              // the value to no value
              break;
            }
            outerRt = l.get(0);
            field = outerRt.fieldByName(currName);

            if (field == null) {
              // Enter the loop again with the next outer enclosing class
              index = rtClassName.lastIndexOf("$");
            }
          }
        }

        // Try to set the value and type of the field.
        //  If the field is not static and we are in a static context
        //  (outer==null), we have to setNoValue.
        if ((field != null) &&
            (field.isStatic() || (outer != null))) {
          Value v = (field.isStatic()) ?
            outerRt.getValue(field) :
            outer.getValue(field);
          currWatch.setValue(_getValue(v));
          try {
            currWatch.setType(field.type().name());
          }
          catch (ClassNotLoadedException cnle) {
            List<ReferenceType> classes = _vm.classesByName(field.typeName());  // JDK 1.5 will eliminate this warning
            if (!classes.isEmpty()) {
              currWatch.setType(classes.get(0).name());
            }
            else {
              currWatch.setTypeNotLoaded();
            }
          }
        }
        else {
          currWatch.setNoValue();
          currWatch.setNoType();
        }

      }
    }
    catch (IncompatibleThreadStateException itse) {
      _log("Exception updating watches.", itse);
    }
    catch (InvalidStackFrameException isfe) {
      _log("Exception updating watches.", isfe);
    }
  }

  /**
   * Returns a string representation of the given Value from JDI.
   * @param value the Value of interest
   * @return the String representation of the Value
   */
  private String _getValue(Value value) throws DebugException {
    // Most types work as they are; for the rest, for now, only care about getting
    // accurate toString for Objects
    if (value == null) {
      return "null";
    }

    if (!(value instanceof ObjectReference)) {
      return value.toString();
    }
    ObjectReference object = (ObjectReference) value;
    ReferenceType rt = object.referenceType();
    ThreadReference thread = _suspendedThreads.peek();
    List<Method> toStrings = rt.methodsByName("toString");  // JDK 1.5 will eliminate this warning
    if (toStrings.size() == 0) {
      // not sure how an Object can't have a toString method, but it happens
      return value.toString();
    }
    // Assume that there's only one method named toString
    Method method = toStrings.get(0);
    try {
      Value stringValue = object.invokeMethod(thread, method, new LinkedList(),
                                              ObjectReference.INVOKE_SINGLE_THREADED);
      if (stringValue == null) {
        return "null";
      }
      else {
        return stringValue.toString();
      }
    }
    catch (InvalidTypeException ite) {
      // shouldn't happen, not passing any arguments to toString()
      throw new UnexpectedException(ite);
    }
    catch (ClassNotLoadedException cnle) {
      // once again, no arguments
      throw new UnexpectedException(cnle);
    }
    catch (IncompatibleThreadStateException itse) {
      throw new DebugException("Cannot determine value from thread: " + itse);
    }
    catch (InvocationException ie) {
      throw new DebugException("Could not invoke toString: " + ie);
    }
  }

  /**
   * @return the approrpiate Method to call in the InterpreterJVM in order
   * to define a variable of the type val
   */
  private Method _getDefineVariableMethod(ReferenceType interpreterRef,
                                          Value val)
    throws DebugException
  {
    List<Method> methods;
    String signature_beginning = "(Ljava/lang/String;";
    String signature_end = ")V";
    String signature_mid;
    String signature;

    if((val == null) || ( val instanceof ObjectReference )){
      signature_mid = "Ljava/lang/Object;Ljava/lang/Class;";
    }
    else if( val instanceof BooleanValue ){
      signature_mid = "Z";
    }
    else if( val instanceof ByteValue ){
      signature_mid = "B";
    }
    else if( val instanceof CharValue ){
      signature_mid = "C";
    }
    else if( val instanceof DoubleValue ){
      signature_mid = "D";
    }
    else if( val instanceof FloatValue ){
      signature_mid = "F";
    }
    else if( val instanceof IntegerValue ){
      signature_mid = "I";
    }
    else if( val instanceof LongValue ){
      signature_mid = "J";
    }
    else if( val instanceof ShortValue ){
      signature_mid = "S";
    }
    else{
      throw new IllegalArgumentException("Tried to define a variable which is\n" +
                                         "not an Object or a primitive type:\n" +
                                         val);
    }

    signature = signature_beginning + signature_mid + signature_end;
    methods = interpreterRef.methodsByName("defineVariable", signature);  // JDK 1.5 will eliminate this warning
    if (methods.size() <= 0) {
      throw new DebugException("Could not find defineVariable method.");
    }

    // Make sure we have a concrete method
    Method tempMethod = methods.get(0);
    for (int i = 1; i < methods.size() && tempMethod.isAbstract(); i++) {
      tempMethod = methods.get(i);
    }
    if (tempMethod.isAbstract()) {
      throw new DebugException("Could not find concrete defineVariable method.");
    }

    return tempMethod;
  }

  /**
   * Assumes that this method is only called immedeately after suspending
   * a thread.
   */
  private ObjectReference _getDebugInterpreter()
    throws InvalidTypeException, ClassNotLoadedException,
    IncompatibleThreadStateException, InvocationException, DebugException
  {
    ThreadReference threadRef = _suspendedThreads.peek();
    String interpreterName = _getUniqueThreadName(threadRef);
    return _getDebugInterpreter(interpreterName, threadRef);
  }

  /**
   * Gets the debug interpreter with the given name using the given
   * suspended thread to invoke methods.
   * @param interpreterName Name of the interpreter in the InterpreterJVM
   * @param threadRef Suspended thread to use for invoking methods
   * @throws IllegalStateException if threadRef is not suspended
   */
  private ObjectReference _getDebugInterpreter(String interpreterName,
                                               ThreadReference threadRef)
    throws InvalidTypeException, ClassNotLoadedException,
    IncompatibleThreadStateException, InvocationException, DebugException
  {
    if (!threadRef.isSuspended()) {
      throw new IllegalStateException("threadRef must be suspended to " +
                                      "get a debug interpreter.");
    }

    // Get the method to return the interpreter
    Method m = _getMethod(_interpreterJVM.referenceType(),
                          "getJavaInterpreter");


    // invokeMethod would throw an ObjectCollectedException if the StringReference
    // declared by _vm.mirrorOf(name) had been garbage collected before
    // invokeMethod could execute. We now just disable collection until after the
    // method is invoked.

    int tries = 0;
    StringReference sr = null;
    while (tries < OBJECT_COLLECTED_TRIES) {
      try{
        LinkedList<StringReference> args = new LinkedList<StringReference>(); //Added parameterization <StringReference>.
        sr = _vm.mirrorOf(interpreterName);
        sr.disableCollection();
        args.add(sr); // make the String a JDI Value
        if( printMessages ) {
          System.out.println("Invoking " + m.toString() + " on " + args.toString());
          System.out.println("Thread is " + threadRef.toString() + " <suspended = " + threadRef.isSuspended() + ">");
        }

        ObjectReference tmpInterpreter = (ObjectReference) _interpreterJVM.invokeMethod(threadRef, m, args,
                                                                                        ObjectReference.INVOKE_SINGLE_THREADED);


        if( printMessages ) System.out.println("Returning...");
        return tmpInterpreter;
      }
      catch (ObjectCollectedException e) {
        tries++;
      }
      finally {
        sr.enableCollection();
      }
    }
    throw new DebugException("The debugInterpreter: " + interpreterName + " could not be obtained from interpreterJVM");
  }

  /**
   * Notifies the debugger that an assignment has been made in
   * the given debug interpreter.
   *
   * Not currently used.
   *
   * @param name the name of the interpreter
   *
  public void notifyDebugInterpreterAssignment(String name) {
    //System.out.println("notifyDebugInterpreterAssignment(" + name + ")");
  }*/

  /**
   * Copy the current selected thread's visible variables (those in scope) into
   * an interpreter's environment and then switch the Interactions window's
   * interpreter to that interpreter.
   */
  private void _dumpVariablesIntoInterpreterAndSwitch() throws DebugException, AbsentInformationException {
    if (printMessages) {
      System.out.println("dumpVariablesIntoInterpreterAndSwitch");
    }
    try {
      ThreadReference suspendedThreadRef = _suspendedThreads.peek();
      StackFrame frame = suspendedThreadRef.frame(0);
      Location l = frame.location();
      ReferenceType rt = l.declaringType();
      String className = rt.name();

      // Name the new interpreter based on this thread
      String interpreterName = _getUniqueThreadName(suspendedThreadRef);
      _model.getInteractionsModel().addDebugInterpreter(interpreterName, className);
      ObjectReference debugInterpreter = _getDebugInterpreter();
      if (printMessages) {
        System.out.println("frame = suspendedThreadRef.frame(0);");
      }
      frame = suspendedThreadRef.frame(0);

      List<LocalVariable> vars = frame.visibleVariables();  // JDK 1.5 will eliminate this warning
      Iterator<LocalVariable> varsIterator = vars.iterator();

      if (printMessages) {
        System.out.println("got visibleVariables");
      }

      // Define each variable
      while(varsIterator.hasNext()){
        LocalVariable localVar = varsIterator.next();
        if (printMessages) {
          System.out.println("local variable: " + localVar);
        }
        // Have to update the frame each time
        frame = suspendedThreadRef.frame(0);
        Value val = frame.getValue(localVar);
        Type type;
        if (val != null) {
          type = val.type();
        }
        else {
          try {
            type = localVar.type();
          }
          catch(ClassNotLoadedException e) {
            List<ReferenceType> classes = _vm.classesByName(localVar.typeName());  //JDK 1.5 will eliminate this warning
            if (!classes.isEmpty()) {
              type = classes.get(0);
            }
            else {
              type = null;
            }
          }
        }
        _defineVariable(suspendedThreadRef, debugInterpreter,
                        localVar.name(), val, type);
      }

      // Update the frame
      frame = suspendedThreadRef.frame(0);

      // Define "this"
      Value thisVal = frame.thisObject();
      if (thisVal != null) {
        _defineVariable(suspendedThreadRef, debugInterpreter,
                        "this", thisVal, thisVal.type());
        //_setThisInInterpreter(suspendedThreadRef, debugInterpreter, thisVal);
      }

      // Set the new interpreter and prompt
      String prompt = _getPromptString(suspendedThreadRef);
      if (printMessages) {
        System.out.println("setting active interpreter");
      }
      _model.getInteractionsModel().setActiveInterpreter(interpreterName,
                                                         prompt);
      if (printMessages) {
        System.out.println("got active interpreter");
      }
    }
    catch(InvalidTypeException exc){
      throw new DebugException(exc.toString());
    }
    catch(IncompatibleThreadStateException e2){
      throw new DebugException(e2.toString());
    }
    catch(ClassNotLoadedException e3){
      throw new DebugException(e3.toString());
    }
    catch(InvocationException e4){
      throw new DebugException(e4.toString());
    }
  }

  /**
   * @return the prompt to display in the itneractions console
   * based upon the ThreadReference threadRef, which is being debugged.
   */
  private String _getPromptString(ThreadReference threadRef) {
    return "[" + threadRef.name() + "] > ";
  }

  /**
   * Defines a variable with the given name to the given value, using
   * a thread reference and JavaInterpreter.
   * If type == null, we assume that the type of this variable
   * has not been loaded so we will set it to Object in DynamicJavaAdapter.
   * @param suspendedThreadRef Thread ref being debugged
   * @param debugInterpreter ObjectReference to the JavaInterpreter to contain
   * the variable
   * @param name Name of the variable
   * @param val Value of the variable
   */
  private void _defineVariable(ThreadReference suspendedThreadRef,
                               ObjectReference debugInterpreter,
                               String name, Value val, Type type)
    throws InvalidTypeException, IncompatibleThreadStateException,
      ClassNotLoadedException, InvocationException, DebugException
  {
    ReferenceType rtDebugInterpreter = debugInterpreter.referenceType();
    Method method2Call = _getDefineVariableMethod(rtDebugInterpreter,  val);

    // invokeMethod would throw an ObjectCollectedException if the StringReference
    // declared by _vm.mirrorOf(name) had been garbage collected before
    // invokeMethod could execute. We now just disable collection until after the
    // method is invoked.

    int tries = 0;
    StringReference sr = null;
    while (tries < OBJECT_COLLECTED_TRIES) {
      try {
        //Added parameterization <Value>.
        List<Value> args = new LinkedList<Value>();  // Mirror is the common supertype of StringReference, Value, and ReferenceType
        //Changed from Mirror to value because invokeMethod requires a List of Value type. It does not need to be a Mirror because neither sr nor val can be a ReferenceType
        sr = _vm.mirrorOf(name);
        sr.disableCollection();
        args.add(sr);
        args.add(val);
        if (type == null) args.add(null);
        else if (type instanceof ReferenceType) {
          args.add(((ReferenceType)type).classObject());
        }

        /* System.out.println("Calling " + method2Call.toString() + "with " + args.get(0).toString()); */
        debugInterpreter.invokeMethod(suspendedThreadRef, method2Call, args,
                                      ObjectReference.INVOKE_SINGLE_THREADED);
        return;
      }
      catch (ObjectCollectedException oce) {
        tries++;
      }
      finally {
        sr.enableCollection();
      }
    }
    throw new DebugException("The variable: " + name +
                             " could not be defined in the debug interpreter");
  }


    /**
   * Notifies all listeners that the current thread has been suspended.
   */
  synchronized void currThreadSuspended() {
    try {
      try {
      // copy the variables in scope into an interpreter
      // and switch the current interpreter to that interpreter
        _dumpVariablesIntoInterpreterAndSwitch();
        _switchToSuspendedThread();
      }
      catch(AbsentInformationException aie){
        // an AbsentInformationException can be thrown if the user does not
        // compile the classes to be debugged with the -g flag
        printMessage("No debug information available for this class.\nMake sure to compile classes to be debugged with the -g flag.");
        _hideWatches();
        // don't updateWatches in _switchToSuspendedThread since it will display the default
        // interpreter's watch information.
        _switchToSuspendedThread(false);
      }
    }
    catch(DebugException de) {
      throw new UnexpectedException(de);
    }
  }

  /**
   * Calls the real switchToSuspendedThread, telling it to updateWatches.
   * This is what is usually called.
   */
  private void _switchToSuspendedThread() throws DebugException {
    _switchToSuspendedThread(true);
  }

  /**
   * Performs the bookkeeping to switch to the suspened thread on the
   * top of the _suspendedThreads stack.
   * @param updateWatches this is false if the current file does not have
   * debug information. This prevents the default interpreter's watch values
   * from being shown.
   */
  private void _switchToSuspendedThread(boolean updateWatches) throws DebugException {
    if (printMessages) {
      System.out.println("_switchToSuspendedThread()");
    }
    _runningThread = null;
    if (updateWatches)
      _updateWatches();
    final ThreadReference currThread = _suspendedThreads.peek();
    _notifier.currThreadSuspended();
    // Anytime a thread is suspended, it becomes the current thread.
    // This makes sure the debug panel will correctly put the
    // current thread in bold.
    _notifier.currThreadSet(new DebugThreadData(currThread));

    try {
      if (currThread.frameCount() > 0) {
        scrollToSource(currThread.frame(0).location());
      }
    }
    catch (IncompatibleThreadStateException itse) {
      throw new UnexpectedException(itse);
    }
  }

  /**
   * Returns a unique name for the given thread.
   */
  private String _getUniqueThreadName(ThreadReference thread) {
    return Long.toString(thread.uniqueID());
  }

  /**
   * @return the Method corresponding to DynamicJavaAdapter.getVariable()
   */
  private Method _getGetVariableMethod(ReferenceType rtInterpreter){
    return _getMethod(rtInterpreter, "getVariable");
  }

  /**
   * Returns the concrete method with the given name on the reference type.
   * @param rt ReferenceType containing the method
   * @param name Name of the method
   * @throws NoSuchElementException if no concrete method could be found
   */
  private Method _getMethod(ReferenceType rt, String name){
    List<Method> methods = rt.methodsByName(name);  // JDK 1.5 will eliminate this warning
    Iterator<Method> methodsIterator = methods.iterator();

    // iterate over all the methods in the list and return the first non-abstract one
    while( methodsIterator.hasNext() ){
      Method m = methodsIterator.next();
      if( !m.isAbstract() ){
        return m;
      }
    }

    throw new NoSuchElementException("No non-abstract method called " + name + " found in " + rt.name());
  }

  /**
   * Converts a primitive wrapper object (eg. Integer) to its corresponding
   * primitive value (eg. int) by invoking the appropriate method in the
   * given thread.
   * @param threadRef Thread in which to invoke the method
   * @param localVar Variable to convert
   * @param v Value of localVar
   * @return Converted primitive, or v if it was a reference type
   */
  private Value _convertToActualType(ThreadReference threadRef, LocalVariable localVar,
                                     Value v)
    throws InvalidTypeException, ClassNotLoadedException,
    IncompatibleThreadStateException, InvocationException
  {
    String typeSignature;
    try {
      typeSignature = localVar.type().signature();
    }
    catch (ClassNotLoadedException cnle) {
      return v;
    }
    Method m;
    ObjectReference ref = (ObjectReference)v;
    ReferenceType rt = ref.referenceType();

    if( typeSignature.equals("Z") ){
      m = _getMethod(rt, "booleanValue");
    }
    else if( typeSignature.equals("B") ){
      m = _getMethod(rt, "byteValue");
    }
    else if( typeSignature.equals("C") ){
      m = _getMethod(rt, "charValue");
    }
    else if( typeSignature.equals("S") ){
      m = _getMethod(rt, "shortValue");
    }
    else if( typeSignature.equals("I") ){
      m = _getMethod(rt, "intValue");
    }
    else if( typeSignature.equals("J") ){
      m = _getMethod(rt, "longValue");
    }
    else if( typeSignature.equals("F") ){
      m = _getMethod(rt, "floatValue");
    }
    else if( typeSignature.equals("D") ){
      m = _getMethod(rt, "doubleValue");
    }
    else{
      return v;
    }

    return ref.invokeMethod(threadRef, m, new LinkedList(),
                            ObjectReference.INVOKE_SINGLE_THREADED);
  }

//  private ClassObjectReference _getClassForName(String name, ThreadReference thread, ClassLoaderReference clr)
//    throws InvalidTypeException, ClassNotLoadedException, AbsentInformationException,
//    IncompatibleThreadStateException, InvocationException, DebugException
//  {
//    Value v = null;
//
//    // invokeMethod would throw an ObjectCollectedException if the StringReference
//    // declared by _vm.mirrorOf(name) had been garbage collected before
//    // invokeMethod could execute. This happened infrequently so by trying this
//    // multiple times, the chance of failure each time should be acceptably low.
//    int tries = 0;
//    while (tries < MAXINVOKETRIES) {
//      try {
//        ReferenceType rt = clr.referenceType();
//        Method method2Call = _getMethod(rt, "loadClass");
//        List args = new LinkedList();
//        args.add(_vm.mirrorOf(name));
//        args.add(_vm.mirrorOf(true));
//        v = clr.invokeMethod(thread, method2Call, args,
//                                   ObjectReference.INVOKE_SINGLE_THREADED);
//        break;
//      }
//      catch (ObjectCollectedException oce) {
//        if (printMessages) System.out.println("Got ObjectCollectedException");
//        tries++;
//      }
//    }
//    if (v != null) {
//      //v = _convertToActualType(thread, var, v);
//      return (ClassObjectReference)v;
//    }
//
//    return null;
//  }

  private Value _getValueOfLocalVariable(LocalVariable var, ThreadReference thread)
    throws InvalidTypeException, ClassNotLoadedException,
      IncompatibleThreadStateException, InvocationException, DebugException
  {
    ObjectReference interpreter = _getDebugInterpreter(_getUniqueThreadName(thread), thread);
    ReferenceType rtInterpreter = interpreter.referenceType();
    Method method2Call = _getGetVariableMethod(rtInterpreter);

    // invokeMethod would throw an ObjectCollectedException if the StringReference
    // declared by _vm.mirrorOf(name) had been garbage collected before
    // invokeMethod could execute. We now just disable collection until after the
    // method is invoked.

    int tries = 0;
    StringReference sr = null;
    String varName = var.name();
    while (tries < OBJECT_COLLECTED_TRIES) {
      try {
        List<Value> args = new LinkedList<Value>(); //Added parameterization <Value>
        sr = _vm.mirrorOf(varName);
        sr.disableCollection();
        args.add(sr);
        Value v = interpreter.invokeMethod(thread, method2Call, args,
                                     ObjectReference.INVOKE_SINGLE_THREADED);
        if (v != null) {
          v = _convertToActualType(thread, var, v);
        }

        return v;
      }
      catch (ObjectCollectedException oce) {
        tries++;
      }
      finally {
        sr.enableCollection();
      }
    }
    throw new DebugException("The value of variable: " + varName +
                             " could not be obtained from the debug interpreter");

  }

  /**
   * Copies the variables in the current interpreter back into the Thread
   * it refers to.
   */
  private void _copyBack(ThreadReference threadRef)
    throws IncompatibleThreadStateException, AbsentInformationException,
      InvocationException, DebugException
  {
    if(printMessages) System.out.println("Getting debug interpreter");
    if(printMessages) System.err.println("Getting variables");
    StackFrame frame = threadRef.frame(0);
    List<LocalVariable> vars = frame.visibleVariables();  // Added <LocalVariable> type argument; warning will go away in JDK 1.5
    Iterator<LocalVariable> varsIterator = vars.iterator();

    // Get each variable from the stack frame
    while(varsIterator.hasNext()) {
      if(printMessages) System.out.println("Iterating through vars");
      LocalVariable localVar = varsIterator.next();

      try {
        Value v = _getValueOfLocalVariable(localVar, threadRef);
        frame = threadRef.frame(0);
        frame.setValue(localVar, v);
      }
      catch (ClassNotLoadedException cnle) {
        printMessage("Could not update the value of '" + localVar.name() + "' (class not loaded)");
      }
      catch (InvalidTypeException ite) {
        printMessage("Could not update the value of '" + localVar.name() + "' (invalid type exception)");
      }
    }
  }

  protected void _copyVariablesFromInterpreter() throws DebugException {
    try {
      // copy variables values out of interpreter's environment and
      // into the relevant stack frame
      if(printMessages) System.out.println("In _copyBack()");
      _copyBack(_runningThread);
      if(printMessages) System.out.println("Out of _copyBack()");
    }
    catch(AbsentInformationException e2) {
      //throw new DebugException(e2.toString());
      // Silently fail for now to ignore the AbsentInformationException that
      // we should have noticed when first suspending on this line (see currThreadSuspended).
    }
    catch(IncompatibleThreadStateException e) {
      throw new DebugException(e.toString());
    }
    catch(InvocationException e4) {
      throw new DebugException(e4.toString());
    }
  }

  /**
   * This method is called to remove all of the debug interpreters upon shutting
   * down.
   */
  private void _removeAllDebugInterpreters() {
    DefaultInteractionsModel interactionsModel =
      _model.getInteractionsModel();
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

  /**
   * This method is called to remove the current debug interpreter upon resuming
   * the current thread.
   * @param fromStep If true, switch to the default interpreter since we don't want
   * to switch to the next debug interpreter and display its watch data. We would like
   * to just not have an active interpreter and put up an hourglass over the
   * interactions pane, but the interpreterJVM must have an active interpreter.
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

  /**
   * Notifies all listeners that the current thread has been resumed.
   * Precondition: Assumes that the current thread hasn't yet been resumed
   */
  synchronized void currThreadResumed() throws DebugException {
    if (printMessages) {
      System.out.println("In currThreadResumed()");
    }

    _notifier.currThreadResumed();
  }

  /**
   * Switches the current interpreter to the one corresponding to threadRef
   * @param threadRef The ThreadRefernce corresponding to the interpreter to switch to
   */
  private void _switchToInterpreterForThreadReference(ThreadReference threadRef){
    String threadName = _getUniqueThreadName(threadRef);
    String prompt = _getPromptString(threadRef);
    _model.getInteractionsModel().setActiveInterpreter(threadName, prompt);
  }

  synchronized void threadStarted() {
    _notifier.threadStarted();
  }

  /**
   * Notifies all listeners that the current thread has died.
   * updateThreads is set to true if the threads and stack tables
   * need to be updated, false if there are no suspended threads
   */
  synchronized void currThreadDied() throws DebugException {
    printMessage("The current thread has finished.");
    _runningThread = null;

    _updateWatches();

    if (_suspendedThreads.size() > 0) {
      ThreadReference thread = _suspendedThreads.peek();
      _switchToInterpreterForThreadReference(thread);

      try{
        if (thread.frameCount() <= 0) {
          printMessage("Could not scroll to source for " + thread.name() + ". It has no stackframes.");
        }
        else {
          scrollToSource(thread.frame(0).location());
        }
      }
      catch(IncompatibleThreadStateException e){
        throw new UnexpectedException(e);
      }

      // updates watches and makes buttons in UI active, does this because
      // there are suspended threads on the stack
      _switchToSuspendedThread();
    }
    _notifier.currThreadDied();
  }

//  synchronized void currThreadSet(final DebugThreadData thread) {
//    printMessage("The current thread has been set.");
//    notifyListeners(new EventNotifier() {
//      public void notifyListener(DebugListener l) {
//        l.currThreadSet(thread);
//      }
//    });
//  }

  synchronized void nonCurrThreadDied() {
    _notifier.nonCurrThreadDied();
  }

  /**
   * Notifies all listeners that the debugger has shut down.
   * updateThreads is set to true if the threads and stack tables
   * need to be updated, false if there are no suspended threads
   */
  synchronized void notifyDebuggerShutdown() {
    _notifier.debuggerShutdown();
  }

  /**
   * Notifies all listeners that the debugger has started.
   */
  synchronized void notifyDebuggerStarted() {
    _notifier.debuggerStarted();
  }

  /**
   * Notifies all listeners that a step has been requested.
   */
  synchronized void notifyStepRequested() {
    _notifier.stepRequested();
  }

  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
//  protected void notifyListeners(EventNotifier n) {
//    synchronized(_listeners) {
//      ListIterator i = _listeners.listIterator();
//
//      while(i.hasNext()) {
//        DebugListener cur = (DebugListener) i.next();
//        n.notifyListener(cur);
//      }
//    }
//  }

  /**
   * Class model for notifying listeners of an event.
   */
//  protected abstract class EventNotifier {
//    public abstract void notifyListener(DebugListener l);
//  }

  /**
   * A stack from which you can remove any element, not just the top of the stack
   * TODO: make a generic Collection extending/replacing Stack.
   */
  protected class RandomAccessStack {
    private Vector<ThreadReference> _data = new Vector<ThreadReference>();

    public synchronized void push(ThreadReference t){
      _data.add(0, t);
    }

    public synchronized ThreadReference peek() throws NoSuchElementException {
      try {
        return _data.get(0);
      }
      catch(ArrayIndexOutOfBoundsException e){
        throw new NoSuchElementException("Cannot peek at the top of an empty RandomAccessStack!");
      }
    }

    public synchronized ThreadReference peekAt(int i) throws NoSuchElementException {
      try {
        return _data.get(i);
      }
      catch(ArrayIndexOutOfBoundsException e){
        throw new NoSuchElementException("Cannot peek at element " + i + " of this stack!");
      }
    }

    public synchronized ThreadReference remove(long id) throws NoSuchElementException{
      for(int i = 0; i < _data.size(); i++){
        if( _data.get(i).uniqueID() == id ){
          ThreadReference t = _data.get(i);
          _data.remove(i);
          return t;
        }
      }

      throw new NoSuchElementException("Thread " + id + " not found in debugger suspended threads stack!");
    }

    public synchronized ThreadReference pop() throws NoSuchElementException{
      try {
        ThreadReference t = _data.get(0);
        _data.remove(0);
        return t;
      }
      catch (ArrayIndexOutOfBoundsException e) {
        throw new NoSuchElementException("Cannot pop from an empty RandomAccessStack!");
      }
    }

    public synchronized boolean contains(long id){
      for(int i = 0; i < _data.size(); i++){
        if( _data.get(i).uniqueID() == id ){
          return true;
        }
      }

      return false;
    }

    public int size() {
      return _data.size();
    }

    public boolean isEmpty() {
      return size() == 0;
    }
  }

  /**
   * This class tries to filter out system threads.
   * It is currently not used.
   *
  class SystemThreadsFilter{
    private HashMap<String,Boolean> _filterThese;

    public SystemThreadsFilter(List threads){
      _filterThese = new HashMap<String,Boolean>();
      Iterator iterator = threads.iterator();
      String temp = null;

      while(iterator.hasNext()){
        temp = ((ThreadReference)iterator.next()).name();
        _filterThese.put(temp, Boolean.TRUE);
      }
    }

    public List filter(List list){
      LinkedList retList = new LinkedList();
      String temp = null;
      ThreadReference tempThreadRef = null;
      Iterator iterator = list.iterator();

      while(iterator.hasNext()){
        tempThreadRef = (ThreadReference)iterator.next();
        temp = tempThreadRef.name();
        if( _filterThese.get(temp) == null ){
          retList.add(tempThreadRef);
        }
      }

      return retList;
    }
  }*/
}
