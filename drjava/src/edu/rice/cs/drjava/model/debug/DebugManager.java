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

import java.io.*;
import java.util.List;
import java.util.Map;
import javax.swing.ListModel;
//import java.util.Iterator;

import gj.util.Enumeration;
import gj.util.Hashtable;
import gj.util.Vector;

// DrJava stuff
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

// JSwat stuff
/**
import com.bluemarsh.jswat.*;
import com.bluemarsh.jswat.ui.*;
import com.bluemarsh.jswat.breakpoint.*;
import com.bluemarsh.util.StringTokenizer;
*/

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.request.*;

/**
 * Interface between DrJava and JPDA.
 * 
 * @version $Id$
 */
public class DebugManager {
  /**
   * Whether the debugger has been initialized and is ready for use.
   */
  //private boolean _isReady;

  /**
   * Session object used by JSwat.
   */
  //private Session _session;
  
  /**
   * Instance of JSwat.
   */
  //private JSwat _swat;
  
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
   * Maps BreakpointRequests to their container Breakpoints
   */
  private Hashtable<BreakpointRequest,Breakpoint> _breakpoints;
  
  /**
   * Writer to use for output messages.
   */
  //private Writer _logwriter;
  
  /**
   * Builds a newset DebugManager which interfaces to JSwat.
   * Does not instantiate JSwat until init is called.
   */
  public DebugManager(GlobalModel model) throws DebugException {
    //_isReady = false;
    //session = null;
    //_swat = null;
    _model = model;
    _vm = null;
    _eventManager = null;
    _breakpoints = new Hashtable<BreakpointRequest, Breakpoint>();
    //_logwriter = new PrintWriter(DrJava.consoleOut());
  }
  
  private void _attachToVM() throws DebugException {
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
    
    // Try to connect
    Map args = connector.defaultArguments();
    Connector.Argument port = (Connector.Argument) args.get("port");
    try {
      int debugPort = _model.getDebugPort();
      System.out.println("using port: " + debugPort);
      port.setValue("" + debugPort);
      _vm = connector.attach(args);
      _eventManager = _vm.eventRequestManager();
      DrJava.consoleOut().println("Connected to VM @ port "+debugPort);
    }
    catch (IOException ioe) {
      throw new DebugException("Could not connect to VM: " + ioe);
    }
    catch (IllegalConnectorArgumentsException icae) {
      throw new DebugException("Could not connect to VM: " + icae);
    }
  }
  
  public synchronized void startup() throws DebugException {
    if (!isReady()) {
      DrJava.consoleOut().println("Starting up...");
      _attachToVM();
      DrJava.consoleOut().println("Attached. VM = " +_vm);
      EventHandler eventHandler = new EventHandler(this, _vm);
      eventHandler.start();
      DrJava.consoleOut().println("EventHandler started...");
      
      //_vm.setDebugTraceMode(0);
      //String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*", "koala.*"};
      //EventThread eventThread = new EventThread(_vm, excludes, 
      //                                          new PrintWriter(DrJava.consoleOut())); 
      //eventThread.setEventRequests(false);
      //eventThread.start();
    }
  }
  
  public synchronized void shutdown() {
    if (isReady()) {
      DrJava.consoleOut().println("Shutting down...");
      try {
      _vm.dispose();
      }
      catch (VMDisconnectedException vmde) {
        //VM was shutdown prematurely
      }
      finally {
        _vm = null;
        _eventManager = null;
        //remove all remaining breakpoints
        Enumeration<Breakpoint> breakpoints = _breakpoints.elements();
        
        while (breakpoints.hasMoreElements()) {
          Breakpoint bp = breakpoints.nextElement();
          bp.getDocument().removeBreakpoint(bp);
        }
        _breakpoints.clear();
        
      }
    }
  }
  
  /**
   * Returns the status of the debugger
   */
  public boolean isReady() {
    return _vm != null;
  }
  
  /**
   * Prepares an instantiation of the debugger.
   *
  public void init(UIAdapter adapter) {
    _session = new Session();
    
    // Test for the JDI package before we continue.
    Bootstrap.virtualMachineManager();
    
    _swat = JSwat.instanceOf();
   
    // Load the jswat settings file.
    File dir = new File(System.getProperty("user.home") + File.separator + ".jswat");
    File f = new File(dir, "settings");
    AppSettings props = AppSettings.instanceOf();
    props.load(f);
    
    adapter.buildInterface();
    _session.init(adapter);

    _session.addListener(new DebugOutputAdapter());

    // Start without any breakpoints
    clearAllBreakpoints(false);
    
    performCommand("classpath " + _model.getClasspath());
    
    // TO DO: start without a file loaded?
    
     _isReady = true;
   }*/
  
  /**
   * Cleans up JSwat's session object when the debugger is finished.
   *
  public void endSession() {
    Main.endSession(_session);
  }*/
  
  /**
   * Cleans up all parts of the debugger after it is finished.
   *
  public void cleanUp() {
    _session = null;
    _swat = null;
    
    _isReady = false;
  }*/
    
  /**
   * Sends a command directly to JSwat.
   * @param command JSwat command to perform
   *
  public void performCommand(String command) {
    Manager manager = _session.getManager(CommandManager.class);
    ((CommandManager)manager).handleInput(command);
  }*/
  
  /**
   * Attaches the given Writer directly to JSwat's status Log.
   * @param w Writer to which to write log messages
   *
  public void attachLogWriter(Writer w) {
    Log log = _session.getStatusLog();
    log.attach(w);
    log.start();
    _logwriter = w;
  }*/
  
 
  /**
   * Starts executing the currently loaded document.
   *
  public void start(OpenDefinitionsDocument doc) throws ClassNotFoundException{

      _model.saveAllBeforeProceeding(GlobalModelListener.DEBUG_REASON);

    if (_model.areAnyModifiedSinceSave()) return; // they cancelled the save.

    String className = mapClassName(doc);
    if (className == null) {
      throw new ClassNotFoundException();
    }

    performCommand( "run " + className );
    
  }*/
  
  /**
   * Suspends execution of the currently running document.
   */
  public void suspend() {
    //performCommand( "suspend" );
    _vm.suspend();
  }
  
  /**
   * Resumes execution of the currently loaded document.
   */
  public void resume() {
    //performCommand( "resume" );
    _vm.resume();
  }
    
  /** 
   * Steps forward in the execution of the currently loaded document.
   * Stepping will walk into method calls.
   *
  public void step() {
    performCommand( "step" );
  }*/
  
  /** 
   * Executes the next line of the currently loaded document.
   * Calling next will not walk into method calls.
   *
  public void next() {
    performCommand( "next" );
  }*/
  
  


  /**
   * Removes all breakpoints from the session.
   * @param visibleMessage Whether to display a status message after clearing
   *
  public void clearAllBreakpoints(boolean visibleMessage) {
    BreakpointManager bpManager = (BreakpointManager)_session.getManager(BreakpointManager.class);
    Iterator i=bpManager.breakpoints(true);
    Breakpoint bp;
    while (i.hasNext()) {
      bp = (Breakpoint)i.next();
      bpManager.removeBreakpoint(bp);
      i=bpManager.breakpoints(true);
    }
    if (visibleMessage) 
      writeToLog("All breakpoints removed.\n");      
  }*/

  /**
   * Toggles whether a breakpoint is set at the given line in the given
   * document.
   * @param doc Document in which to set or remove the breakpoint
   * @param lineNumber Line on which to set or remove the breakpoint
   */
  public void toggleBreakpoint(OpenDefinitionsDocument doc, int lineNumber)
    throws DebugException {  
    
    Breakpoint breakpoint = doc.getBreakpointAt(lineNumber);
    if (breakpoint == null) {
      setBreakpoint(new Breakpoint (doc, lineNumber, _vm));
    }
    else {
      removeBreakpoint(breakpoint);
    }
  }
  
  /**
   * Sets a breakpoint.
   *
   * @param className the name of the class in which to break
   * @param lineNumber the line number at which to break
   */
  public void setBreakpoint(Breakpoint breakpoint)
    throws DebugException
  {

    System.out.println("setting: " + breakpoint);
    
    _breakpoints.put(breakpoint.getRequest(), breakpoint);
    breakpoint.getDocument().addBreakpoint(breakpoint);
  }

 /**
  * Removes a breakpoint.
  * Called from ToggleBreakpoint -- even with BPs that are not active.
  * This takes in a LineBreakpoint (only kind DrJava creates) so that we can
  * get out the line number and unlighlight it if necessary.
  *
  * @param breakpoint The breakpoint to remove.
  * @param className the name of the class the BP is being removed from.
  */
  public void removeBreakpoint(Breakpoint breakpoint) {
    System.out.println("unsetting: " + breakpoint);
    _breakpoints.remove(breakpoint.getRequest());
    breakpoint.getDocument().removeBreakpoint(breakpoint);
    _eventManager.deleteEventRequest(breakpoint.getRequest());
  }

  /**
   * Prints all location breakpoints via writeToLog().
   *
  public void printBreakpoints() {
    BreakpointManager bpManager = (BreakpointManager)_session.getManager(BreakpointManager.class);
    Iterator i = bpManager.breakpoints(true);
    Object o;
    LineBreakpoint lbp;
    MethodBreakpoint mbp;

    if (!i.hasNext()) {
      writeToLog("No Breakpoints Set.\n");
    }
    else {
      writeToLog("Current Breakpoints:\n");
    }
    
    while (i.hasNext()) { // remember, ONLY locationbreakpoints
      o = i.next();
      if (o instanceof LineBreakpoint) {
        lbp = (LineBreakpoint)o;  
        writeToLog(" " + lbp.getClassName() +
                   ":" + lbp.getLineNumber() +
                   "\n");
      }
      if (o instanceof MethodBreakpoint) {
        mbp = (MethodBreakpoint)o;
        writeToLog(" " + mbp.getClassName() +
                   ":" + mbp.getMethodName() +
                   ":" + mbp.getLineNumber() +
                   "\n");     
      }
    }
  }*/
  
  /*   
  public void addWatch();
  
  public void removeWatch();
  */

  /**
   * Return the fully-qualified classname corresponding to the
   * given source file. Returns null if file has an error.
   *
   * Note: assumes each file contains source for exactly one
   * class whose name is the same as the filename.
   * 
   * Based on code from JSwat.
   * 
   * @param doc   document containing class.
   * @return  Classname, or null if error.
   *
  private final static String mapClassName(final OpenDefinitionsDocument doc) {
    File source;
    String fpath, rpath;
    try {
      source = doc.getDocument().getFile();
      fpath = source.getCanonicalPath();
      rpath = doc.getSourceRoot().getCanonicalPath();
    }
    catch (IOException ioe) {
      return null;
    } catch (InvalidPackageException ipe) {
      return null;
    }
    // Don't need the filename extension.
    int idx = fpath.lastIndexOf(".java");
    if (idx > 0) {
      fpath = fpath.substring(0, idx);
    }

    int longest = -1;
    if (fpath.startsWith(rpath)) {
      int len = rpath.length();
      if (rpath.charAt(len - 1) != File.separatorChar) {
        // Path list entry does not end with a separator so
        // we must add one to remove it from fpath.
        len++;
      }
      if (len > longest) {
        // Save the length for the possible best match.
        // This may change as we go through the list.
        longest = len;
      }
    }
  
    if (longest > -1) {
      // Convert the file separators to dots.
      fpath = fpath.replace(File.separatorChar, '.');
      fpath = fpath.substring(longest);
      return fpath;
    } else {
      return null;
    }    
  }*/
  
  
  /*
   * Class DebugOutputAdapter is responsible for displaying the output
   * of a debuggee process to the Log. It reads both the standard output
   * and standard error streams from the debuggee VM. For it to operate
   * correctly it must be added as a session listener.
   *
   * @author  Nathan Fiedler, with modifications
   */
  //class DebugOutputAdapter implements SessionListener {
    /** When this reaches 2, the output streams are finished. */
    //protected int outputCompleteCount;
    
    
    /**
     * Constructs a DebugOutputAdapter to output to the manager's log.
     *
    public DebugOutputAdapter() {
    }*/
    
    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     *
    public void activate(Session session) {
      // Attach to the stderr and stdout input streams of the passed
      // VirtualMachine and begin reading from them. Everything read
      // will be displayed in the text area.
      com.sun.jdi.VirtualMachine vm = session.getVM();
      if (vm.process() == null) {
        // Must be a remote process, which can't provide us
        // with an input and error streams.
        // We're automatically finished reading output.
        outputCompleteCount = 2;
      } else {
        // Assume output reading is not complete.
        outputCompleteCount = 0;
        // Create readers for the input and error streams.
        displayOutput(vm.process().getErrorStream(),false);
        displayOutput(vm.process().getInputStream(),true);
      }
    }*/
    
    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     *
    public void close(Session session) {
    }*/
    
    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     *
    public synchronized void deactivate(Session session) {
      // Wait for the output readers to finish.
      while (outputCompleteCount < 2) {
        try {
          wait();
        } catch (InterruptedException ie) {
          break;
        }
      }
    }*/
    
    /** 
     * Create a thread that will retrieve and display any output
     * from the given input stream.
     *
     * @param  is  InputStream to read from.
     *
    protected void displayOutput(final InputStream is, final boolean isOut) {
      Thread thr = new Thread("output reader") { 
        public void run() {
          try {
            BufferedReader br =
              new BufferedReader(new InputStreamReader(is));
            String line;
            // Dump until there's nothing left.
            while ((line = br.readLine()) != null) {
              line += "\n";
              if (isOut)
                _model.debugSystemOutPrint(line);
              else
                _model.debugSystemErrPrint(line);
            }
          } catch (IOException ioe) {
            _model.debugSystemErrPrint("Error reading from streams.\n");
          } finally {
            notifyOutputComplete();
          }
        }
      };
      thr.setPriority(Thread.MIN_PRIORITY);
      thr.start();
    }*/
    
    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     *
    public void init(Session session) {
    }*/
    
    /**
     * Notify any waiters that one of the reader threads has
     * finished reading its output. This must be a separate
     * method in order to be synchronized on 'this' object.
     *
    protected synchronized void notifyOutputComplete() {
      outputCompleteCount++;
      notifyAll();
    }*/
  //}
  
  
  public void hitBreakpoint(BreakpointRequest request) {
    Breakpoint breakpoint = _breakpoints.get(request);
    System.out.println("Encountered a breakpoint at line "+ 
                       breakpoint.getLineNumber() +
                       " in file " + breakpoint.getClassName());
  }
  
  /**
   * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
   * all open documents contain.
   */
  public Vector<Breakpoint> getBreakpoints() {
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
}