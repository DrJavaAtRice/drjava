/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

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
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
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

package edu.rice.cs.drjava.plugins.eclipse.repl;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.rice.cs.drjava.plugins.eclipse.EclipsePlugin;
import edu.rice.cs.drjava.plugins.eclipse.util.text.SWTDocumentAdapter;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.ConsoleDocument;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** Interactions model which can notify GlobalModelListeners on events.
 * @version $Id$
 */
public class EclipseInteractionsModel extends RMIInteractionsModel {
 
 // TODO: Read input from System.in
 
 /** Number of lines to remember in the history */
 protected static final int HISTORY_SIZE = 1000;
 
 /** Milliseconds to wait after each println */
 protected static final int WRITE_DELAY = 50;

 public static final File WORKING_DIR = new File(System.getProperty("user.home", ""));
 
 /** Whether to print System.out and System.err to files for debugging. */
 private static final boolean DEBUG = false;
 
 /** List of listeners to this document. */
 protected final LinkedList<InteractionsListener> _listeners;
 
 /** Whether we have already warned the user to reset after a compilation occurs. */
 protected boolean _warnedToReset;
 
 /** Wraps a try/catch construct around a call on new MainJVM */
 private static MainJVM newMainJVM() {
   try { return new MainJVM(WORKING_DIR); }
   catch(RemoteException e) {
     error.log(e);
     throw new UnexpectedException(e);
   }
 }
 
 /** Creates a new InteractionsModel with a new MainJVM.
   * @param adapter SWTDocumentAdapter to use for the document
   */
 public EclipseInteractionsModel(SWTDocumentAdapter adapter) /* throws RemoteException */ {
   this(newMainJVM(), adapter);
 }
 
 /** Creates a new InteractionsModel.
   * @param control RMI interface to the Interpreter JVM
   * @param adapter SWTDocumentAdapter to use for the document
   */
 public EclipseInteractionsModel(MainJVM control, SWTDocumentAdapter adapter) {
   super(control, adapter, WORKING_DIR, HISTORY_SIZE, WRITE_DELAY);
   _listeners = new LinkedList<InteractionsListener>();
   _warnedToReset = false;
   if (DEBUG) _debugSystemOutAndErr();
   
   _jvm.setInteractionsModel(this);
   EclipsePlugin plugin = EclipsePlugin.getDefault();
   if (plugin != null) {
     String classpath = plugin.getPluginClasspath();
     _jvm.setStartupClassPath(classpath);
   }
   _jvm.startInterpreterJVM();
   _addChangeListener();
 }
 
 /** Cleans up any resources this model created, including the Interactions JVM. */
 public void dispose() { _jvm.killInterpreter(null); }
 
 /** */
 public Iterable<File> getClassPath() { return _jvm.getClassPath(); }
 
 /** Adds a listener to this model. */
 public void addInteractionsListener(InteractionsListener l) { _listeners.addLast(l); }
 
 /** Removes the given listener from this model. */
 public void removeInteractionsListener(InteractionsListener l) { _listeners.remove(l); }
 
 /** Removes all listeners from this model. */
 public void removeAllInteractionsListeners() { _listeners.clear(); }
 
 /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
 protected void _interpreterResetFailed(Throwable t) {
   _document.insertBeforeLastPrompt("Reset Failed!" + StringOps.NEWLINE, InteractionsDocument.ERROR_STYLE);
 }
 
 /** Called when the Java interpreter is ready to use. Adds any open documents to the classpath. */
 public void interpreterReady(File wd) {
   debug.logStart();
   _resetInteractionsClasspath();
   super.interpreterReady(wd);
   debug.logEnd();
 }
 
 /** Resets the warning flag after the Interactions Pane is reset. */
 protected void _resetInterpreter(File wd) {
   super._resetInterpreter(wd);
   _warnedToReset = false;
 }
 
 /** Notifies listeners that an interaction has started. */
 public void _notifyInteractionStarted() {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interactionStarted();
   }
 }
 
 /** Notifies listeners that an interaction has ended. */
 protected void _notifyInteractionEnded() {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interactionEnded();
   }
 }
 
 /**
  * Notifies listeners that an error was present in the interaction.
  */
 protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interactionErrorOccurred(offset, length);
   }
 }
 
 /**
  * Notifies listeners that the interpreter is resetting.
  */
 protected void _notifyInterpreterResetting() {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interpreterResetting();
   }
 }
 
 /** Notifies listeners that the interpreter is ready. */
 public void _notifyInterpreterReady(File wd) {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interpreterReady(wd);
   }
 }
 
 /**
  * Notifies listeners that the interpreter has exited unexpectedly.
  * @param status Status code of the dead process
  */
 protected void _notifyInterpreterExited(final int status) {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interpreterExited(status);
   }
 }
 
 /**
  * Notifies listeners that the interpreter reset failed.
  * @param t Throwable explaining why the reset failed.
  */
 protected void _notifyInterpreterResetFailed(Throwable t) {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interpreterResetFailed(t);
   }
 }
 
 /**
  * Notifies listeners that the interpreter has changed.
  * @param inProgress Whether the new interpreter is currently in progress.
  */
 protected void _notifyInterpreterChanged(final boolean inProgress) {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interpreterChanged(inProgress);
   }
 }
 
 /**
  * Notifies the view that the current interaction is incomplete.
  */
 protected void _notifyInteractionIncomplete() {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).interactionIncomplete();
   }
 }
 
 /** Notifies listeners that the slave JVM has been used. */
 protected void _notifySlaveJVMUsed() {
   for (int i=0; i < _listeners.size(); i++) {
     _listeners.get(i).slaveJVMUsed();
   }
 }
 
 /**
  * Adds each project's classpath to the Interactions pane.
  */
 protected void _resetInteractionsClasspath() {
   try {
     // Get the workspace root, home of random global data.
     IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
     
     // Get the IJavaModel, which corresponds to all open Java projects.
     IJavaModel jModel = JavaCore.create(root);
     
     // Ask the IJavaModel for all open IJavaProjects.
     IJavaProject jProjects[] = jModel.getJavaProjects();
     
     // For each of the projects...
     for(int i = 0; i < jProjects.length; i++) {
       IJavaProject jProj = jProjects[i];
       _addProjectToClasspath(jProj, jModel, root);
     }
   }
   catch (CoreException ce) {
     // Only happens if the project doesn't exist or isn't open.
     //  We shouldn't be seeing the project if this is the case.
     throw new UnexpectedException(ce);
   }
 }
 
 private void _addProjectToClasspath(IJavaProject jProj) throws CoreException {
   // Get the workspace root, home of random global data.
   IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
   
   // Get the IJavaModel, which corresponds to all open Java projects.
   IJavaModel jModel = JavaCore.create(root);
   
   _addProjectToClasspath(jProj, jModel, root);
 }
 
 private void _addProjectToClasspath(IJavaProject jProj, IJavaModel jModel, IWorkspaceRoot root)
   throws CoreException {
   // Get the project's location on disk
   IProject proj = jProj.getProject();
   URI projRoot = proj.getDescription().getLocationURI();
   // Note: getLocation returns null if the default location is used
   //  (brilliant...)
   
   // Get the resolved classpath entries - this should filter out
   //   all CPE_VARIABLE and CPE_CONTAINER entries.
   IClasspathEntry entries[] = jProj.getResolvedClasspath(true);
   
   // For each of the classpath entries...
   for(int j = 0; j < entries.length; j++) {
     IClasspathEntry entry = entries[j];
     
     // Check what kind of entry it is...
     int kind = entry.getEntryKind();
     
     // And get the appropriate path.
     IPath path;
     switch (kind) {
       case IClasspathEntry.CPE_LIBRARY:
         // The raw location of a JAR.
         path = entry.getPath();
         //System.out.println("Adding library: " + path.toOSString());
         addToClassPath(path.toOSString());
         break;
       case IClasspathEntry.CPE_SOURCE:
         // The output location of source.
         // Need to append it to the user's workspace directory.
         path = entry.getOutputLocation();
         if (path == null) {
           path = jProj.getOutputLocation();
           //System.out.println(" output location from proj: " + path);
         }
         
         // At this point, the output location contains the project
         //  name followed by the actual output folder name
         
         if (projRoot != null && (!projRoot.isAbsolute() || projRoot.getScheme().equals("file"))) {
           // We have a custom project location, so the project name
           //  is not part of the *actual* output directory.  We need
           //  to remove the project name (first segment) and then
           //  append the rest of the output location to projRoot.
           path = path.removeFirstSegments(1);
           path = new Path(projRoot.getPath()).append(path);
         }
         else {
           // A null projRoot means use the default location, which
           //  *does* include the project name in the output directory.
           path = root.getLocation().append(path);
         }
         
         //System.out.println("Adding source: " + path.toOSString());
         //addToClassPath(path.toOSString());
         addBuildDirectoryClassPath(path.toOSString());
         break;
       case IClasspathEntry.CPE_PROJECT:
         // In this case, just the project name is given.
         // We don't actually need to add anything to the classpath,
         //  since the project is open and we will get its classpath
         //  on another pass.
         break;
       default:
         // This should never happen.
         throw new RuntimeException("Unsupported classpath entry type.");
     }
   }
 }
 
 /**
  * Prints a message warning the user to reset the Interactions Pane
  * once a compilation has occurred.  The warning is only printed if
  * the Interactions Pane has been used.
  */
 protected void _warnUserToReset() {
   if (!_warnedToReset && _jvm.slaveJVMUsed()) {
     String warning =
       "Warning: Interactions are out of sync with the current class files.\n" +
       "You should reset interactions from the toolbar menu.\n";
     _document.insertBeforeLastPrompt(warning,
                                      InteractionsDocument.ERROR_STYLE);
     _warnedToReset = true;
   }
 }
 
 /**
  * Adds a listener to Eclipse for any changes to Java projects.
  * The listener will warn the user to reset the Interactions Pane if
  * new class files are generated.
  *
  * Note: We'd also like to dynamically add elements to the classpath
  * if the classpath of a project changes.  (This is tricky to identify.)
  */
 protected void _addChangeListener() {
   //System.out.println("F_ADDED_TO_CLASSPATH: " + IJavaElementDelta.F_ADDED_TO_CLASSPATH);
   //System.out.println("ADDED: " + IJavaElementDelta.ADDED);
   //System.out.println("CHANGED: " + IJavaElementDelta.CHANGED);
   //System.out.println("REMOVED: " + IJavaElementDelta.REMOVED);
   JavaCore.addElementChangedListener(new IElementChangedListener() {
     public void elementChanged(ElementChangedEvent e) {
       IJavaElementDelta delta = e.getDelta();
       _visitDelta(delta, 0);
     }
   });
 }
 public URL toURL(String path) {
   try { return new File(path).toURI().toURL(); } 
   catch (MalformedURLException e) {
     _document.insertBeforeLastPrompt("Malformed URL " + path +"\n", InteractionsDocument.ERROR_STYLE);
   }
   throw new RuntimeException("Trying to add an invalid file:" + path);
 }
 
 public void addBuildDirectoryClassPath(String path) {    
   // _document.insertBeforeLastPrompt("cp: " + path +"\n",
   //            InteractionsDocument.ERROR_STYLE);
   //System.out.println("addBuildDirectoryToClassPath:" + path);
   super.addBuildDirectoryClassPath(new File(path));
   //new URL("file://"+path+"/"));
 }
 public void addProjectFilesClassPath(String path) {    
   //_document.insertBeforeLastPrompt("cp: " + path +"\n",
   //     InteractionsDocument.ERROR_STYLE);
   super.addProjectFilesClassPath(new File(path));
 }
 
 public void addToClassPath(String path) {
   //_document.insertBeforeLastPrompt("cp: " + path +"\n",
   //     InteractionsDocument.ERROR_STYLE);
   //System.out.println("addToClassPath:" + path);
   super.addProjectClassPath(new File(path));
 }
 
 /** Walks the tree of deltas, looking for changes to the classpath or compilation units.
   * @param delta Tree to search
   * @param depth Current depth of original tree. Pass 0 on the first call.
   */
 protected void _visitDelta(IJavaElementDelta delta, int depth) {
   int kind = delta.getKind();
//   int flags = delta.getFlags();
   IJavaElement element = delta.getElement();
   
//   System.out.println("\nVisiting: " + delta);
//   System.out.println("depth: " + depth);
//   System.out.println("kind: " + kind);
//   System.out.println("flags: " + flags);
//   System.out.println("Class: " + delta.getClass());
//   System.out.println("Element class: " + delta.getElement().getClass());
   
//   if ((flags & IJavaElementDelta.F_CONTENT) != 0) {
//     System.out.println("Change is in the content.");
//   }
   
   // If the element is a changed compilation unit, we should notify
   // the user to reset.  This happens on every save, so we don't want
   // to reset automatically.
   if (_isCompilationUnit(element) && (kind == IJavaElementDelta.CHANGED)) {
     // Class files have changed
     _warnUserToReset();
   }
   
   
   // Case: has children
   if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
     IJavaElementDelta[] children = delta.getAffectedChildren();
     //System.out.println("visiting " + children.length + " children...");
     for (int i=0; i < children.length; i++) {
       _visitDelta(children[i], depth + 1);
     }
   }
   
   // Case: project opened
   else if (kind == IJavaElementDelta.ADDED) {
     if (element instanceof IJavaProject) {
       try {
         _addProjectToClasspath((IJavaProject)element);
       }
       catch(CoreException e) {
         throw new UnexpectedException(e);
       }
     }
   }
   
   /*
    // Case: added to classpath
    else if ((delta.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0) {
    System.out.println("looking at add classpath delta...");
    if (delta.getElement() instanceof IPackageFragmentRoot) {
    System.out.println("Element is an IPackageFragmentRoot");
    }
    System.out.println("element: " + delta.getElement());
    }
    
    // Case: removed from classpath
    else if ((delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) {
    System.out.println("looking at remove classpath delta...");
    if (delta.getElement() instanceof IPackageFragmentRoot) {
    System.out.println("Element is an IPackageFragmentRoot");
    }
    System.out.println("element: " + delta.getElement());
    }
    */
   
   // Otherwise, ignore
 }
 
 // Note:
 //  ICompilationUnit extends IWorkingCopy, but WorkingCopy extends
 //  CompilationUnit.  (?!?)  This means asking whether element is
 //  an ICompilationUnit is not sufficient-- we also have to ask if
 //  it is a working copy.
 
 
 /**
  * Returns whether the given element is a compilation unit and not a
  * working copy.  Useful to determine whether class files have actually
  * changed or not.
  * Note:
  *  ICompilationUnit extends IWorkingCopy, but WorkingCopy extends
  *  CompilationUnit.  (?!?)  This means that we can't just ask whether
  *  element is an ICompilationUnit; we have to also make sure it is not
  *  also an IWorkingCopy.
  *
  * @param element JavaElement in question
  */
 protected boolean _isCompilationUnit(IJavaElement element) {
   boolean isCompilationUnit = element instanceof ICompilationUnit;
   boolean isWorkingCopy = isCompilationUnit &&
     ((ICompilationUnit)element).isWorkingCopy();
   
   return isCompilationUnit && !isWorkingCopy;
 }
 
 /**
  * Redirects System.out and System.err to a file for debugging Eclipse.
  */
 private void _debugSystemOutAndErr() {
   try {
     File outF = new File(System.getProperty("user.home") +
                          System.getProperty("file.separator") + "out.txt");
     FileWriter wo = new FileWriter(outF);
     final PrintWriter outWriter = new PrintWriter(wo);
     File errF = new File(System.getProperty("user.home") +
                          System.getProperty("file.separator") + "err.txt");
     FileWriter we = new FileWriter(errF);
     final PrintWriter errWriter = new PrintWriter(we);
     System.setOut(new PrintStream(new edu.rice.cs.util.OutputStreamRedirector() {
       public void print(String s) {
         outWriter.print(s);
         outWriter.flush();
       }
     }));
     System.setErr(new PrintStream(new edu.rice.cs.util.OutputStreamRedirector() {
       public void print(String s) {
         errWriter.print(s);
         errWriter.flush();
       }
     }));
   }
   catch (IOException ioe) {}
 }
 
 /**
  * Sets whether to allow access to private members in the interactions pane.
  * @param allow true iff access should be allowed
  */
 public void setPrivateAccessible(boolean allow) {
   _jvm.setPrivateAccessible(allow);
 }
 
 /**
  * Sets the optional command-line arguments to the interpreter JVM.
  */
 public void setOptionArgs(String optionArgString) {
   _jvm.setOptionArgs(optionArgString);
 }
 
 /** Gets the console tab document for this interactions model */
 public ConsoleDocument getConsoleDocument() {
   return new ConsoleDocument(new InteractionsDJDocument());
 }
}
