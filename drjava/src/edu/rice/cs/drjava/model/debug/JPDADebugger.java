/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this p2rogram; if not, write to the Free Software
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

import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.swing.ListModel;

import gj.util.Enumeration;
import gj.util.Hashtable;
import gj.util.Vector;

// DrJava stuff
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.drjava.config.OptionConstants;

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;
import com.sun.jdi.event.*;

/**
 * An integrated debugger which attaches to the Interactions JVM using
 * Sun's Java Platform Debugger Architecture (JPDA/JDI) interface.
 * 
 * @version $Id$
 */
public class JPDADebugger implements Debugger {
  /**
   * Reference to DrJava's model.
   */
  private GlobalModel _model;
  
  /**
   * VirtualMachine of the interactions JVM.
   */
  private VirtualMachine _vm;
  
  /**
   * Manages all event requests in JDI.
   */
  private EventRequestManager _eventManager;

  /**
   * Vector of all current Breakpoints, with and without EventRequests.
   */
  private Vector<Breakpoint> _breakpoints;

  /**
   * Vector of all current Watches
   */
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
  private LinkedList _listeners;
  
  /**
   * The running ThreadReference that we are debugging.
   */
  private ThreadReference _runningThread;
  
  /**
   * Storage for all the threads suspended by this debugger 
   */
  private RandomAccessStack _suspendedThreads;
  
  /**
   * Storage to facilitate remembering threads which have died
   * so that we can filter them out of the the list of threads 
   * returned by the VM we are debugging
   */
  private DeadThreadFilter _deadThreads;
  
  /**
   * A handle to the interpreterJVM that we need so we can
   * populate the environment.
   */
  private ObjectReference _interpreterJVM;
  
  /**
   * Builds a new JPDADebugger to debug code in the Interactions JVM,
   * using the JPDA/JDI interfaces.
   * Does not actually connect to the interpreterJVM until startup().
   */
  public JPDADebugger(GlobalModel model) {
    _model = model;
    _vm = null;
    _eventManager = null;
    _listeners = new LinkedList();
    _breakpoints = new Vector<Breakpoint>();
    _watches = new Vector<DebugWatchData>();
    _suspendedThreads = new RandomAccessStack();
    _pendingRequestManager = new PendingRequestManager(this);
    _runningThread = null;
    _deadThreads = new DeadThreadFilter();
    _interpreterJVM = null;
  }
  
  /**
   * Returns whether the debugger is currently available in this JVM.
   * This does not indicate whether it is ready to be used.
   */
  public boolean isAvailable() {
    return true;
  }

  /**
   * Attaches the debugger to the Interactions JVM to prepare for debugging.
   */
  public synchronized void startup() throws DebugException {
    if (!isReady()) {
      // check if all open documents are in sync
      ListModel list = _model.getDefinitionsDocuments();
      for (int i = 0; i < list.getSize(); i++) {
        OpenDefinitionsDocument currDoc = (OpenDefinitionsDocument)list.getElementAt(i);
        currDoc.checkIfClassFileInSync();
      }
      _attachToVM();
      ThreadDeathRequest tdr = _eventManager.createThreadDeathRequest();
      tdr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
      tdr.enable();
      EventHandler eventHandler = new EventHandler(this, _vm);
      eventHandler.start();
    }
  }
  
  /**
   * Handles the details of attaching to the interpreterJVM.
   */
  private void _attachToVM() throws DebugException {
    // Blocks until interpreter has registered itself
    _model.waitForInterpreter();
    
    // Get the connector
    VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
    List connectors = vmm.attachingConnectors();
    AttachingConnector connector = null;
    java.util.Iterator iter = connectors.iterator();
    while (iter.hasNext()) {
      AttachingConnector conn = (AttachingConnector)iter.next();
      if (conn.name().equals("com.sun.jdi.SocketAttach")) {
        connector = conn;
      }
    }
    if (connector == null) {
      throw new DebugException("Could not find an AttachingConnector!");
    }
    
    // Try to connect on our debug port
    Map args = connector.defaultArguments();
    Connector.Argument port = (Connector.Argument) args.get("port");
    try {
      int debugPort = _model.getDebugPort();
      port.setValue("" + debugPort);
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
    }
    catch (IOException ioe) {
      throw new DebugException("Could not connect to VM: " + ioe);
    }
    catch (IllegalConnectorArgumentsException icae) {
      throw new DebugException("Could not connect to VM: " + icae);
    }
    
    // get the singleton instance of the interpreterJVM
    List referenceTypes = _vm.classesByName("edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM");
    if (referenceTypes.size() <= 0) {
      throw new DebugException("Could not get a reference to interpreterJVM");
    }
    ReferenceType rt = (ReferenceType)referenceTypes.get(0);
    Field field = rt.fieldByName("ONLY");
    _interpreterJVM = (ObjectReference)rt.getValue(field);
  }
  
  /**
   * Disconnects the debugger from the Interactions JVM and cleans up
   * any state.
   */
  public synchronized void shutdown() {
    if (isReady()) {
      try {
        removeAllBreakpoints();
        removeAllWatches();
        _vm.dispose();
      }
      catch (VMDisconnectedException vmde) {
        //VM was shutdown prematurely
      }
      finally {
        _vm = null;
        _eventManager = null;
        _suspendedThreads = new RandomAccessStack();
        _deadThreads = new DeadThreadFilter();
        _runningThread = null;
      }
    }
  }
  
  /**
   * Returns the status of the debugger
   */
  public synchronized boolean isReady() {
    return _vm != null;
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
   * This method assumes that the thread referenced by thread
   * is suspended already.
   */
  synchronized void setCurrentThread(ThreadReference thread) {
    _suspendedThreads.push(thread);
  }
  
  /**
   * Sets the current debugged thread to the thread referenced by threadData,
   * suspending it if necessary.
   */
  synchronized public void setCurrentThread(DebugThreadData threadData) throws NoSuchElementException{
    if (!isReady()) return;
    
    if ( threadData == null)
      return;
    
    ThreadReference thread_ref = getThreadFromDebugThreadData(threadData);
    
    /** 
     * Special case to avoid overhead of scrollToSource() if we
     * are selecting the thread we have already selected currently
     */
    if( _suspendedThreads.size() > 0 && 
       _suspendedThreads.peek().uniqueID() == thread_ref.uniqueID() ){
      return;
    }
    
    /** if we switch to a currently suspended thread, we need to remove 
     * it from the stack and put it on the top
     **/
    if( _suspendedThreads.contains(thread_ref.uniqueID()) ){
      _suspendedThreads.remove(thread_ref.uniqueID());
    }
    if( !thread_ref.isSuspended() ){
      thread_ref.suspend();
    }
    _suspendedThreads.push(thread_ref);
    
    try{
      if( thread_ref.frameCount() <= 0 ) {
        printMessage(thread_ref.name() + " could not be suspended. It had no stackframes.");
        resume();
        return;
      }
      scrollToSource(thread_ref.frame(0).location());
    }catch(IncompatibleThreadStateException e){
      throw new UnexpectedException(e);
    }
    
    currThreadSuspended();
  }
  
  /**
   * Returns the debugger's thread that currently has the focus.
   */
  synchronized ThreadReference getCurrentThread() {
    return _suspendedThreads.peek();
  }
  
  /**
   * Returns the debugger's currently running thread.
   */
  synchronized ThreadReference getCurrentRunningThread() {
    return _runningThread;
  }
  
  public synchronized boolean hasSuspendedThreads(){
    if( _suspendedThreads.size() > 0 ) return true;
    else return false;
  }
  
  synchronized boolean hasRunningThread(){
    if( _runningThread != null) {
      return true;
    }
    else {
      return false;
    }
  }
  
  /**
   * Returns the loaded ReferenceTypes for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.
   * <p>
   * If custom class loaders are in use, multiple copies of the class may
   * be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className) {
    return getReferenceTypes(className, DebugAction.ANY_LINE);
  }
  
  /**
   * Returns the loaded ReferenceTypes for the given class name, or null
   * if the class could not be found.  Makes no attempt to load the class
   * if it is not already loaded.  If the lineNumber is not DebugAction.ANY_LINE,
   * ensures that the returned ReferenceTypes contain the given lineNumber,
   * searching through inner classes if necessary.  If no inner classes
   * contain the line number, null is returned.
   * <p>
   * If custom class loaders are in use, multiple copies of the class
   * may be loaded, so all are returned.
   */
  synchronized Vector<ReferenceType> getReferenceTypes(String className, 
                                                       int lineNumber) {
    // Get all classes that match this name
    List classes = _vm.classesByName(className);
    
    // Assume first one is correct, for now
    //if (classes.size() > 0) {
    
    // Return each valid reference type
    Vector<ReferenceType> refTypes = new Vector<ReferenceType>();
    ReferenceType ref = null;
    for (int i=0; i < classes.size(); i++) {
      ref = (ReferenceType) classes.get(i);
      
      if (lineNumber > DebugAction.ANY_LINE) {
        List lines = new LinkedList();
        try {
          lines = ref.locationsOfLine(lineNumber);
        }
        catch (AbsentInformationException aie) {
          // try looking in inner classes
        }
        if (lines.size() == 0) {
          // The ReferenceType might be in an inner class
          List innerRefs = ref.nestedTypes();
          ref = null;
          for (int j = 0; j < innerRefs.size(); j++) {
            try {
              ReferenceType currRef = (ReferenceType) innerRefs.get(j);
              lines = currRef.locationsOfLine(lineNumber);
              if (lines.size() > 0) {
                ref =currRef;
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
        refTypes.addElement(ref);
      }
      //if (ref != null && !ref.isPrepared()) {
      //   return null;
      //}
    }
    return refTypes;
  }
  
  /**
   * @return The thread in the virtual machine with name d.getName()
   */
  private ThreadReference getThreadFromDebugThreadData(DebugThreadData d) throws NoSuchElementException{
    List threads = _vm.allThreads();
    Iterator iterator = threads.iterator();
    ThreadReference thread_ref = null;
    
    while(iterator.hasNext()){
      thread_ref = (ThreadReference)iterator.next();
      if( thread_ref.uniqueID() == d.getUniqueID() ){
        return thread_ref;
      }
    }
    
    throw new NoSuchElementException("Thread " + d.getName() + " not found in virtual machine!");
  }
  
  /**
   * Suspends all the currently running threads in the virtual machine
   */
  public synchronized void suspendAll(){
    List threads = _vm.allThreads();
    Iterator iterator = threads.iterator();
    ThreadReference thread_ref = null;
    
    while(iterator.hasNext()){
      thread_ref = (ThreadReference)iterator.next();
      
      if( !thread_ref.isSuspended() ){
        thread_ref.suspend();
        _suspendedThreads.push(thread_ref);
      }
    }
    _runningThread = null;
  }
  
  /**
   * Suspends execution of the thread referenced by threadData.
   */
  public synchronized void suspend(DebugThreadData threadData) {
    setCurrentThread(threadData);
    _runningThread = null;
  }
  
  /**
   * Resumes execution of the currently suspended thread
   */
  public synchronized void resume() {
    if (!isReady()) return;
    ThreadReference thread = null;
    try{
      thread = _suspendedThreads.pop();
    }catch(NoSuchElementException e){
      /** Just return because there is no thread to resume */
      return;
    }
    
    resumeThread(thread);
  }
  
  /**
   * This is called when the user manually chooses a thread to resume.
   */
  public synchronized void resume(DebugThreadData threadData) {
    if (!isReady()) return;
    
    ThreadReference thread = _suspendedThreads.remove(threadData.getUniqueID());
    
    resumeThread(thread);
  }
  
  private void resumeThread(ThreadReference thread){
    if( thread == null)
      return;
    
    int suspendCount = thread.suspendCount();
    _runningThread = thread;
    currThreadResumed();
    for (int i=suspendCount; i>0; i--) {
      thread.resume();
    }
  }
  
  /** 
   * Steps into the execution of the currently loaded document.
   * @flag The flag denotes what kind of step to take. The following mark valid options:
   * StepRequest.STEP_INTO
   * StepRequest.STEP_OVER
   * StepRequest.STEP_OUT
   */
  public synchronized void step(int flag) throws DebugException {
    if (!isReady() || (_suspendedThreads.size() <= 0)) return;
    
    ThreadReference thread = _suspendedThreads.peek();
    // don't allow the creation of a new StepRequest if there's already one on
    // the current thread
    List steps = _eventManager.stepRequests();
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = (StepRequest)steps.get(i);
      if (step.thread().equals(thread)) {
        _eventManager.deleteEventRequest(step);
        break;
      }
    }
        
    Step step = new Step(this, StepRequest.STEP_LINE, flag);
    notifyStepRequested();
    resume();
  }
  
  /**
   * Called from interactionsEnded in MainFrame in order to clear any current 
   * StepRequests that remain.
   */
  /*** 
   * NOTE: We don't think we need this method any more, if we ever did at all
   * Wednesday, March 5th, 2003
   **/
 /**
  public synchronized void clearCurrentStepRequest() {   
    List steps = _eventManager.stepRequests();
    
    if( suspendedThreads.size() <= 0 ){
      return ;
    }
    
    ThreadReference thread = _suspendedThreads().peek();
    
    for (int i = 0; i < steps.size(); i++) {
      StepRequest step = (StepRequest)steps.get(i);
      if (step.thread().equals(thread)) {
        _eventManager.deleteEventRequest(step);
        return;
      }
    }
  }
  */
  
  /**
   * Adds a watch on the given field or variable.
   * @param field the name of the field we will watch
   */
  public synchronized void addWatch(String field) {
    if (!isReady()) return;
    
    _watches.addElement(new DebugWatchData(field));
    _updateWatches();
  }
  
  /**
   * Removes any watches on the given field or variable.
   * @param field the name of the field we will watch
   */
  public synchronized void removeWatch(String field) {
    if (!isReady()) return;
    
    for (int i=0; i < _watches.size(); i++) {
      DebugWatchData watch = _watches.elementAt(i);
      if (watch.getName().equals(field)) {
        _watches.removeElementAt(i);
      }
    }
  }
  
  /**
   * Removes the watch at the given index.
   * @param index Index of the watch to remove
   */
  public synchronized void removeWatch(int index) {
    if (!isReady()) return;
    
    if (index < _watches.size()) {
      _watches.removeElementAt(index);
    }
  }
  
  /**
   * Removes all watches on existing fields and variables.
   */
  public synchronized void removeAllWatches() {
    _watches = new Vector<DebugWatchData>();
  }
  

  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param offset Start offset on the line to set the breakpoint
   * @param lineNumber Line on which to set or remove the breakpoint
   */
  public synchronized void toggleBreakpoint(OpenDefinitionsDocument doc, 
                                            int offset, int lineNum)
    throws DebugException
  {
    if (!isReady()) return;
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
  {
    if (!isReady()) return;
    
    breakpoint.getDocument().checkIfClassFileInSync();
    // update UI back in MainFrame
    
    _breakpoints.addElement(breakpoint);
    breakpoint.getDocument().addBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointSet(breakpoint);
      }
    });
  }
  
 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  *
  * @param breakpoint The breakpoint to remove.
  * @param className the name of the class the BP is being removed from.
  */
  public synchronized void removeBreakpoint(final Breakpoint breakpoint) {
    if (!isReady()) return;
    
    _breakpoints.removeElement(breakpoint);
    
    Vector<BreakpointRequest> requests = breakpoint.getRequests();
    if ( requests.size() > 0 && _eventManager != null) {
      try {
        for (int i=0; i < requests.size(); i++) {
          _eventManager.deleteEventRequest(requests.elementAt(i));
        }
      }
      catch (VMMismatchException vme) {
        // Not associated with this VM; probably from a previous session.
        // Ignore and make sure it gets removed from the document.
      }
      catch (VMDisconnectedException vmde) {
        // The VM has already disconnected for some reason
        // Ignore it and make sure the breakpoint gets removed from the document
      }
    }
    //else {
    // Now always remove from pending request, since it's always there
    _pendingRequestManager.removePendingRequest(breakpoint);
    //}
    breakpoint.getDocument().removeBreakpoint(breakpoint);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.breakpointRemoved(breakpoint);
      }
    });
  }

  /**
   * Removes all the breakpoints from the manager's vector of breakpoints.
   */
  public synchronized void removeAllBreakpoints() {
    while (_breakpoints.size() > 0) {
      removeBreakpoint( _breakpoints.elementAt(0));
    }
  }

  /**
   * Called when a breakpoint is reached.  The Breakpoint object itself
   * should be stored in the "debugAction" property on the request.
   * @param request The BreakPointRequest reached by the debugger
   */
  synchronized void reachedBreakpoint(BreakpointRequest request) {
    Object property = request.getProperty("debugAction");
    if ( (property!=null) && (property instanceof Breakpoint)) {
      final Breakpoint breakpoint = (Breakpoint)property;
      _model.printDebugMessage("Breakpoint hit in class " + 
                               breakpoint.getClassName() + "  [line " +
                               breakpoint.getLineNumber() + "]");
      
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.breakpointReached(breakpoint);
        }
      });
    }
    else {
      // A breakpoint we didn't set??
    }
  }
  
  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public synchronized Vector<Breakpoint> getBreakpoints() {
    Vector<Breakpoint> sortedBreakpoints = new Vector<Breakpoint>();
    ListModel docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.getSize(); i++) {
      Vector<Breakpoint> docBreakpoints = 
        ((OpenDefinitionsDocument)docs.getElementAt(i)).getBreakpoints();
      for (int j = 0; j < docBreakpoints.size(); j++) {
        sortedBreakpoints.addElement(docBreakpoints.elementAt(j));
      }      
    }
    return sortedBreakpoints;
  }
  
  /**
   * Prints the list of breakpoints in the current session of DrJava, both pending
   * resolved Breakpoints are listed
   */
  public synchronized void printBreakpoints() {
    Enumeration<Breakpoint> breakpoints = getBreakpoints().elements();
    if (breakpoints.hasMoreElements()) {
      _model.printDebugMessage("Breakpoints: ");
      while (breakpoints.hasMoreElements()) {
        Breakpoint breakpoint = breakpoints.nextElement();
        _model.printDebugMessage("  " + breakpoint.getClassName() +
                                 "  [line " + breakpoint.getLineNumber() + "]");
      }
    }
    else {
      _model.printDebugMessage("No breakpoints set.");
    }
  }
  
  /**
   * Returns all currently watched fields and variables.
   */
  public synchronized Vector<DebugWatchData> getWatches() {
    return _watches;
  }
  
  /**
   * Returns a Vector of DebugThreadData or null if the vm is null
   */
  public synchronized Vector<DebugThreadData> getCurrentThreadData() {
    if (!isReady()) return null;

    List listThreads = _vm.allThreads();
    /** get an iterator that filters out threads that we know are dead from the list returned 
     * by _vm.allThreads() 
     **/
    Iterator iter = _deadThreads.filter(listThreads).iterator();
    Vector<DebugThreadData> threads = new Vector<DebugThreadData>();
    while (iter.hasNext()) {      
      threads.addElement(new DebugThreadData((ThreadReference)iter.next()));                                                  
    }
    return threads;
  }
  
  /**
   * Returns a Vector of DebugStackData for the current thread or null if the 
   * current thread is null
   * TO DO: Config option for hiding DrJava subset of stack trace
   */
  public synchronized Vector<DebugStackData> getCurrentStackFrameData() {
    if (!isReady()) {
      return null;
    }
    
    if(_runningThread != null || _suspendedThreads.size() <= 0)
    {
      return new Vector<DebugStackData>();
    }
    
    Iterator iter = null;
    try {
      iter = _suspendedThreads.peek().frames().iterator();
      Vector<DebugStackData> frames = new Vector<DebugStackData>();
      while (iter.hasNext()) {
        frames.addElement(new DebugStackData((StackFrame)iter.next()));
      }
      return frames;
    }
    catch (IncompatibleThreadStateException itse) {
      return null;
    }
  }
  
  /**
   * Takes the location of event e, opens the document corresponding to its class
   * and centers the definition pane's view on the appropriate line number
   * @param e should be a LocatableEvent
   */
  synchronized void scrollToSource(LocatableEvent e) {
    Location location = e.location();
    OpenDefinitionsDocument doc = null;
    
    // First see if doc is stored
    EventRequest request = e.request();
    Object docProp = request.getProperty("document");
    if ((docProp != null) && (docProp instanceof OpenDefinitionsDocument)) {
      doc = (OpenDefinitionsDocument) docProp;
      openAndScroll(doc, location);
    }
    else {
      scrollToSource(location);
    }
  }  
  
  /**
   * Scroll to the location specified by location
   */
  synchronized void scrollToSource(Location location){
    OpenDefinitionsDocument doc = null;
    
    // No stored doc, look on the source root set (later, also the sourcepath)
    ReferenceType rt = location.declaringType();
    String filename = "";
    try {
      filename = rt.sourceName();
      filename = getPackageDir(rt.name()) + filename;
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
    File[] sourceRoots = _model.getSourceRootSet();
    Vector<File> roots = new Vector<File>();
    for (int i=0; i < sourceRoots.length; i++) {
      roots.addElement(sourceRoots[i]);
    }
    File f = _model.getSourceFileFromPaths(filename, roots);
    if (f == null) {
      Vector<File> sourcepath = 
        DrJava.getConfig().getSetting(OptionConstants.DEBUG_SOURCEPATH);
      f = _model.getSourceFileFromPaths(filename, sourcepath);
    }
    
    if (f != null) {
      // Get a document for this file, forcing it to open
      try {
        doc = _model.getDocumentForFile(f);
      }
      catch (IOException ioe) {
        // No doc, so don't notify listener
      }
      catch (OperationCanceledException oce) {
        // No doc, so don't notify listener
      }
    }
    
    openAndScroll(doc, location);
  }
  
  /** 
   * Opens a document and scrolls to the appropriate location specified by location
   */
  synchronized void openAndScroll(OpenDefinitionsDocument doc, Location location){
    // Open and scroll if doc was found
    if (doc != null) {
      doc.checkIfClassFileInSync();
      // change UI if in sync in MainFrame listener
      
      final OpenDefinitionsDocument docF = doc;
      final Location locationF = location;
      
      notifyListeners(new EventNotifier() {
        public void notifyListener(DebugListener l) {
          l.threadLocationUpdated(docF, locationF.lineNumber());
        }
      });
    }
    else {
      String className = location.declaringType().name();
      printMessage("  (Source for " + className + " not found.)");
    }
  }
  
  /**
   * Returns the relative directory (from the source root) that the source
   * file with this qualifed name will be in, given its package.
   * Returns the empty string for classes without packages.
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

  private void _updateWatches() {
    if (!isReady() || (_suspendedThreads.size() <= 0)) return;
    
    try {
      int stackIndex = 0;
      StackFrame currFrame = null;
      List frames = null;
      ThreadReference thread = _suspendedThreads.peek();
      if (thread.frameCount() <= 0 ) {
        printMessage("Could not update watch values. The current thread had no stackframes.");
        return;
      }
      frames = thread.frames();
      currFrame = (StackFrame) frames.get(stackIndex);
      stackIndex++;
      Location location = currFrame.location();
      ReferenceType rt = location.declaringType();
      for (int i = 0; i < _watches.size(); i++) {
        DebugWatchData currWatch = _watches.elementAt(i);
        String currName = currWatch.getName();
        String currValue = currWatch.getValue();
        // check for "this"
        if (currName.equals("this")) {
          ObjectReference obj = currFrame.thisObject();
          if (obj != null) {
            currWatch.setValue(_getValue(obj));
            currWatch.setType(obj.type());
          }
          else {
            currWatch.setValue(DebugWatchUndefinedValue.ONLY);
            currWatch.setType(null);
          }
          continue;
        } 
        //List frames = null;
        LocalVariable localVar = null;
        try {
          localVar = currFrame.visibleVariableByName(currName);
        }
        catch (AbsentInformationException aie) {
        }
        
        ReferenceType outerRt = rt;
        // if the variable being watched is not a local variable, check if it's a field
        if (localVar == null) {
          Field field = outerRt.fieldByName(currName);
          
          // if the variable is not a field either, it's not defined in this 
          // ReferenceType's scope, keep going further out in scope.
          String className = outerRt.name();
          while (field == null) {
            
            // crop off the $ if there is one and anything after it
            int indexOfDollar = className.lastIndexOf('$');    
            if (indexOfDollar > -1) {
              className = className.substring(0, indexOfDollar);
            }
            else {
              // There is no $ in the className, we're at the outermost class and the
              // field still was not found
              break;
            }
            outerRt = (ReferenceType)_vm.classesByName(className).get(0);
            if (outerRt == null) {
              break;
            }
            field = outerRt.fieldByName(currName);
          }
          if (field != null) {
            // check if the field is static
            if (field.isStatic()) {
              currWatch.setValue(_getValue(outerRt.getValue(field)));
              try {
                currWatch.setType(field.type());
              }
              catch (ClassNotLoadedException cnle) {
                currWatch.setType(null);
              }
            }
            else {
              StackFrame outerFrame = currFrame;
              // the field is not static
              // Check if the frame represents a native or static method and
              // keep going down the stack frame looking for the frame that
              // has the same ReferenceType that we found the Field in.
              // This is a hack, remove it to slightly improve performance but
              // at the loss of ever being able to watch outer instance
              // fields. If unremoved, this will work sometimes, but not always.
              while (outerFrame.thisObject() != null && 
                     !outerFrame.thisObject().referenceType().equals(outerRt) &&
                     stackIndex < frames.size()) {
                outerFrame = (StackFrame) frames.get(stackIndex);
                stackIndex++;
              }
              if (stackIndex < frames.size() && outerFrame.thisObject() != null) { 
                // then we found the right stack frame
                currWatch.setValue(_getValue(outerFrame.thisObject().getValue(field)));
                try {
                  currWatch.setType(field.type());
                }
                catch (ClassNotLoadedException cnle) {
                  currWatch.setType(null);
                }
              }
              else {
                currWatch.setValue(DebugWatchUndefinedValue.ONLY);
                currWatch.setType(null);
              }
            }
          }
          else {
            currWatch.setValue(DebugWatchUndefinedValue.ONLY);
            currWatch.setType(null);
          }
        }
        else {
          currWatch.setValue(_getValue(currFrame.getValue(localVar)));
          try {
            currWatch.setType(localVar.type());
          }
          catch (ClassNotLoadedException cnle) {
            currWatch.setType(null);
          }
        }
      }
    }
    catch (IncompatibleThreadStateException itse) {
      return;
    }
    catch (InvalidStackFrameException isfe) {
      return;
    }
  }

  /**
   * Takes a jdi Value and gets its String representation
   * @param value the Value whose value is requested
   * @return the String representation of the Value
   */
  private String _getValue(Value value) {
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
    /*try {
      thread = object.owningThread();
    }
    catch (IncompatibleThreadStateException itse) {
      DrJava.consoleOut().println("thread is not suspended");
      return DebugWatchUndefinedValue.ONLY.toString();
    }*/
    List toStrings = rt.methodsByName("toString");
    if (toStrings.size() == 0) {
      // not sure how an Object can't have a toString method, but it happens
      return value.toString();
    }
    // Assume that there's only one method named toString
    Method method = (Method)toStrings.get(0);
    Value stringValue = null;
    try {
      stringValue = object.invokeMethod(thread, method, new LinkedList(), ObjectReference.INVOKE_SINGLE_THREADED);
    }
    catch (InvalidTypeException ite) {
      // shouldn't happen, not passing any arguments to toString()
    }
    catch (ClassNotLoadedException cnle) {
      // once again, no arguments
    }
    catch (IncompatibleThreadStateException itse) {
      DrJava.consoleOut().println("thread is not suspended");
      return DebugWatchUndefinedValue.ONLY.toString();
    }
    catch (InvocationException ie) {
      DrJava.consoleOut().println("invocation exception");
      return DebugWatchUndefinedValue.ONLY.toString();
    }
    return stringValue.toString();
  }
  
  /**
   * Notifies all listeners that the current thread has been suspended.
   */
  synchronized void currThreadSuspended() {
    _runningThread = null;
    _updateWatches();
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadSuspended();
        /** 
         * Anytime a thread is suspended, it becomes the current thread.
         * This makes sure the debug panel will correctly put the
         * current thread in bold.
         */
        l.currThreadSet(new DebugThreadData(_suspendedThreads.peek()));
      }
    });
  }
  
  
  /**
   * Notifies all listeners that the current thread has been resumed.
   */
  synchronized void currThreadResumed() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadResumed();
      }
    });
  }
  
  synchronized void threadStarted() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.threadStarted();
      }
    });
  }
  
  /**
   * Notifies all listeners that the current thread has died.
   * updateThreads is set to true if the threads and stack tables
   * need to be updated, false if there are no suspended threads
   */
  synchronized void currThreadDied() {
    _model.printDebugMessage("The current thread has finished.");
    if( _runningThread != null ){
      _deadThreads.add(new DebugThreadData(_runningThread));
      _runningThread = null;
    }
       
    if (_suspendedThreads.size() > 0) {
      ThreadReference thread = _suspendedThreads.peek();
      
      try{
        if (thread.frameCount() <= 0) {
          printMessage("Could not scroll to source for " + thread.name() + ". It has no stackframes.");
        }
        else {
          scrollToSource(thread.frame(0).location());
        }
      }catch(IncompatibleThreadStateException e){
        throw new UnexpectedException(e);
      }
    
      // updates watches and makes buttons in UI active, does this because
      // there are suspended threads on the stack
      currThreadSuspended();
    }
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadDied();
      }
    });
  }
  
  synchronized void nonCurrThreadDied(DebugThreadData threadRef) {
    _deadThreads.add(threadRef);
    
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.nonCurrThreadDied();
      }
    });
  }
  
  synchronized void currThreadSet(final DebugThreadData thread) {
    _model.printDebugMessage("The current thread is now " + thread.getName() + ".");
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.currThreadSet(thread);
      }
    });
  }
    
  /**
   * Notifies all listeners that the debugger has shut down.
   * updateThreads is set to true if the threads and stack tables
   * need to be updated, false if there are no suspended threads
   */
  synchronized void notifyDebuggerShutdown() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerShutdown();
      }
    });
  }

  /**
   * Notifies all listeners that the debugger has started.
   */
  synchronized void notifyDebuggerStarted() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.debuggerStarted();
      }
    });
  }
  
  /**
   * Notifies all listeners that a step has been requested.
   */
  synchronized void notifyStepRequested() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(DebugListener l) {
        l.stepRequested();
      }
    });
  }
  
  /**
   * Adds a listener to this JPDADebugger.
   * @param listener a listener that reacts on events generated by the JPDADebugger
   */
  public synchronized void addListener(DebugListener listener) {
    _listeners.addLast(listener);
  }

  /**
   * Removes a listener to this JPDADebugger.
   * @param listener listener to remove
   */
  public synchronized void removeListener(DebugListener listener){
    _listeners.remove(listener);
  }
  
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  protected void notifyListeners(EventNotifier n) {
    synchronized(_listeners) {
      ListIterator i = _listeners.listIterator();

      while(i.hasNext()) {
        DebugListener cur = (DebugListener) i.next();
        n.notifyListener(cur);
      }
    }
  }
  
  /**
   * Class model for notifying listeners of an event.
   */
  protected abstract class EventNotifier {
    public abstract void notifyListener(DebugListener l);
  }
 
  /** 
   * A stack from which you can remove any element, not just the top of the stack 
   */
  protected class RandomAccessStack{
    private Vector<ThreadReference> _data = new Vector<ThreadReference>();
    
    public void push(ThreadReference t){
      _data.insertElementAt(t, 0);
    }
    
    public ThreadReference peek() throws NoSuchElementException{
      try{
        return _data.elementAt(0);
      }catch(ArrayIndexOutOfBoundsException e){
        throw new NoSuchElementException("Cannot peek at the top of an empty RandomAccessStack!");
      }
    }
    
    public ThreadReference remove(long id) throws NoSuchElementException{
      int i = 0;
      for(i = 0; i < _data.size(); i++){
        if( _data.elementAt(i).uniqueID() == id ){
          ThreadReference t = _data.elementAt(i);
          _data.removeElementAt(i);
          return t; 
        }
      }
      
      throw new NoSuchElementException("Thread " + id + " not found in debugger suspended threads stack!");
    }
    
    public ThreadReference pop() throws NoSuchElementException{
      try{
        ThreadReference t = _data.elementAt(0);
        _data.removeElementAt(0);
        return t; 
      }catch(ArrayIndexOutOfBoundsException e){
        throw new NoSuchElementException("Cannot pop from an empty RandomAccessStack!");
      }
    }
    
    public boolean contains(long id){
      int i = 0;
      for(i = 0; i < _data.size(); i++){
        if( _data.elementAt(i).uniqueID() == id ){
          return true;
        }
      }
      
      return false;
    }

    public int size() { return _data.size(); }
  }
  
   /**
     * A class for filtering threads that we know are dead from the List returned by
     * _vm.allThreads() [thanks sun for returning dead threads in this method call, 
     * good decision]
     */
    class DeadThreadFilter{
      private Hashtable<Long,DebugThreadData> _theDeadThreads;
      public DeadThreadFilter(){
        _theDeadThreads = new Hashtable<Long,DebugThreadData>();
      }
      public void add(DebugThreadData thread){
        _theDeadThreads.put(new Long(thread.getUniqueID()), thread);
      }
      
      public List filter(List threads){
        LinkedList retList = new LinkedList();
        Enumeration keys = _theDeadThreads.keys();
        
        /** 
         * The following code removes dead threads from _theDeadThreads if
         * the threads do not appear in the list of threads threads.  This 
         * must be done to make sure that _theDeadThreads doesn't grow too
         * large with useless info
         */
        while(keys.hasMoreElements()){
          Long key = (Long)keys.nextElement();
          
          boolean flag = false;
          for(int i = 0; i < threads.size(); i++){
            if( ((ThreadReference)threads.get(i)).uniqueID() == key.longValue() ){
              flag = true;
              break;
            }
          }
          
          if( !flag ){
            _theDeadThreads.remove(key);
          }
        }
        
        Iterator iterator = threads.iterator();
        ThreadReference ref = null;
        
        while(iterator.hasNext()){
          ref = (ThreadReference)iterator.next();
          if( _theDeadThreads.get(new Long(ref.uniqueID())) == null ){
            retList.add(ref);
          }
        }
        
        return (List)retList;
      }
    }
}
