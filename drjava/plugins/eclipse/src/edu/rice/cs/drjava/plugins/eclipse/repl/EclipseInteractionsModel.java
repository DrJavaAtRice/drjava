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

package edu.rice.cs.drjava.plugins.eclipse.repl;

import java.io.*;

import java.util.LinkedList;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;

import edu.rice.cs.drjava.plugins.eclipse.EclipsePlugin;
import edu.rice.cs.drjava.model.repl.RMIInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.text.SWTDocumentAdapter;
import edu.rice.cs.util.UnexpectedException;

/**
 * Interactions model which can notify GlobalModelListeners on events.
 * @version $Id$
 */
public class EclipseInteractionsModel extends RMIInteractionsModel {

  // TODO: Read input from System.in

  /** Number of lines to remember in the history */
  protected static final int HISTORY_SIZE = 1000;

  /** Milliseconds to wait after each println */
  protected static final int WRITE_DELAY = 50;

  /** Whether to print System.out and System.err to files for debugging. */
  private static final boolean DEBUG = false;

  /**
   * List of listeners to this document.
   */
  protected final LinkedList<InteractionsListener> _listeners;

  /**
   * Whether we have already warned the user to reset after a compilation
   * occurs.
   */
  protected boolean _warnedToReset;

  /**
   * Creates a new InteractionsModel with a new MainJVM.
   * @param adapter SWTDocumentAdapter to use for the document
   */
  public EclipseInteractionsModel(SWTDocumentAdapter adapter) {
    this(new MainJVM(), adapter);
  }

  /**
   * Creates a new InteractionsModel.
   * @param control RMI interface to the Interpreter JVM
   * @param adapter SWTDocumentAdapter to use for the document
   */
  public EclipseInteractionsModel(MainJVM control,
                                  SWTDocumentAdapter adapter)
  {
    super(control, adapter, HISTORY_SIZE, WRITE_DELAY);
    _listeners = new LinkedList<InteractionsListener>();
    _warnedToReset = false;
    if (DEBUG) {
      _debugSystemOutAndErr();
    }

    _interpreterControl.setInteractionsModel(this);
    try {
      EclipsePlugin plugin = EclipsePlugin.getDefault();
      if (plugin != null) {
        String classpath = plugin.getPluginClasspath();
        _interpreterControl.setStartupClasspath(classpath);
      }
    }
    catch (IOException ioe) {
      // TODO: log error
    }
    _interpreterControl.startInterpreterJVM();
    _addChangeListener();
  }

  /**
   * Cleans up any resources this model created, including the Interactions JVM.
   */
  public void dispose() {
    _interpreterControl.killInterpreter(false);
  }

  /**
   * Adds a listener to this model.
   */
  public void addInteractionsListener(InteractionsListener l) {
    _listeners.addLast(l);
  }

  /**
   * Removes the given listener from this model.
   */
  public void removeInteractionsListener(InteractionsListener l) {
    _listeners.remove(l);
  }

  /**
   * Removes all listeners from this model.
   */
  public void removeAllInteractionsListeners() {
    _listeners.clear();
  }

  /**
   * Any extra action to perform (beyond notifying listeners) when
   * the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed!" + _newLine,
                                     InteractionsDocument.ERROR_STYLE);
  }

  /**
   * Called when the Java interpreter is ready to use.
   * Adds any open documents to the classpath.
   */
  public void interpreterReady() {
    _resetInteractionsClasspath();
    super.interpreterReady();
  }

  /**
   * Resets the warning flag after the Interactions Pane is reset.
   */
  protected void _resetInterpreter() {
    super._resetInterpreter();
    _warnedToReset = false;
  }

  /**
   * Notifies listeners that an interaction has started.
   */
  protected void _notifyInteractionStarted() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interactionStarted();
    }
  }

  /**
   * Notifies listeners that an interaction has ended.
   */
  protected void _notifyInteractionEnded() {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interactionEnded();
    }
  }

  /**
   * Notifies listeners that an error was present in the interaction.
   */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).
        interactionErrorOccurred(offset, length);
    }
  }

  /**
   * Notifies listeners that the interpreter is resetting.
   */
  protected void _notifyInterpreterResetting() {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterResetting();
    }
  }

  /**
   * Notifies listeners that the interpreter is ready.
   */
  protected void _notifyInterpreterReady() {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterReady();
    }
  }

  /**
   * Notifies listeners that the interpreter has exited unexpectedly.
   * @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterExited(status);
    }
  }

  /**
   * Notifies listeners that the interpreter reset failed.
   * @param t Throwable explaining why the reset failed.
   */
  protected void _notifyInterpreterResetFailed(Throwable t) {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterResetFailed(t);
    }
  }

  /**
   * Notifies listeners that the interpreter has changed.
   * @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterChanged(inProgress);
    }
  }

  /**
   * Notifies the view that the current interaction is incomplete.
   */
  protected void _notifyInteractionIncomplete() {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interactionIncomplete();
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
    throws CoreException
  {
    // Get the project's location on disk
    IProject proj = jProj.getProject();
    IPath projRoot = proj.getDescription().getLocation();
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

          if (projRoot != null) {
            // We have a custom project location, so the project name
            //  is not part of the *actual* output directory.  We need
            //  to remove the project name (first segment) and then
            //  append the rest of the output location to projRoot.
            path = path.removeFirstSegments(1);
            path = projRoot.append(path);
          }
          else {
            // A null projRoot means use the default location, which
            //  *does* include the project name in the output directory.
            path = root.getLocation().append(path);
          }

          //System.out.println("Adding source: " + path.toOSString());
          addToClassPath(path.toOSString());
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
    if (!_warnedToReset && interpreterUsed()) {
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

  /**
   * Walks the tree of deltas, looking for changes to the classpath or
   * compilation units.
   * @param delta Tree to search
   * @param depth Current depth of original tree. Pass 0 on the first call.
   */
  protected void _visitDelta(IJavaElementDelta delta, int depth) {
    int kind = delta.getKind();
//    int flags = delta.getFlags();
    IJavaElement element = delta.getElement();

//    System.out.println("\nVisiting: " + delta);
//    System.out.println("depth: " + depth);
//    System.out.println("kind: " + kind);
//    System.out.println("flags: " + flags);
//    System.out.println("Class: " + delta.getClass());
//    System.out.println("Element class: " + delta.getElement().getClass());

//    if ((flags & IJavaElementDelta.F_CONTENT) != 0) {
//      System.out.println("Change is in the content.");
//    }

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
    boolean isWorkingCopy = (element instanceof IWorkingCopy) &&
      ((IWorkingCopy)element).isWorkingCopy();

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
    _interpreterControl.setPrivateAccessible(allow);
  }
  
  /**
   * Sets the optional command-line arguments to the interpreter JVM.
   */
  public void setOptionArgs(String optionArgString) {
    _interpreterControl.setOptionArgs(optionArgString);
  }
}
