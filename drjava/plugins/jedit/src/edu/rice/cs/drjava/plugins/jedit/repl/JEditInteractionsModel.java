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

package edu.rice.cs.drjava.plugins.jedit.repl;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.gjt.sp.jedit.*;

import edu.rice.cs.drjava.plugins.jedit.JEditPlugin;
import edu.rice.cs.drjava.model.repl.RMIInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.Log;

/**
 * Interactions model which can notify listeners on events.
 * @version $Id$
 */
public class JEditInteractionsModel extends RMIInteractionsModel {
  /**
   * Number of lines to remember in the history
   */
  protected static final int HISTORY_SIZE = 1000;

  /**
   * Milliseconds to wait after each println.
   */
  protected static final int WRITE_DELAY = 50;

  /**
   * Whether to print System.out and System.err to files for debugging.
   */
  private static final boolean DEBUG = false;

  /**
   * List of listeners to this document.
   */
  protected final List<InteractionsListener> _listeners;

  /**
   * A log for recording messages in a file.
   */
  private final Log _log;

  /**
   * Creates a new InteractionsModel with a new MainJVM and SwingDocumentAdapter.
   */
  public JEditInteractionsModel() {
    this(new MainJVM(), new SwingDocumentAdapter());
  }

  /**
   * Creates a new InteractionsModel with a new MainJVM.
   * @param adapter SwingDocumentAdapter to use for the document
   */
  public JEditInteractionsModel(SwingDocumentAdapter adapter) {
    this(new MainJVM(), adapter);
  }

  /**
   * Creates a new InteractionsModel.
   * @param control RMI interface to the Interpreter JVM
   * @param adapter SwingDocumentAdapter to use for the document
   */
  public JEditInteractionsModel(MainJVM control, SwingDocumentAdapter adapter) {
    super(control, adapter, HISTORY_SIZE, WRITE_DELAY);
    _listeners = new ArrayList<InteractionsListener>();
//    if (DEBUG) {
//      _debugSystemOutAndErr();
//    }
    _log = new Log("JEditInteractionsModelLog", DEBUG);
    
    _interpreterControl.setInteractionsModel(this);
    String classpath = "";
    String pathSep = System.getProperty("path.separator");
    Vector<File> cp = DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH);
    if (cp.size() > 0) {
      classpath += cp.elementAt(0).getAbsolutePath();
      for (int i = 1; i < cp.size(); i++) {
        cp += pathSep;
        classpath += cp.elementAt(i).getAbsolutePath();
      }
    }
    JEditPlugin plugin = JEditPlugin.getDefault();
    if (plugin != null) {
      classpath += plugin.getPluginJAR().getPath();
    }
    _interpreterControl.setStartupClasspath(classpath);
    _interpreterControl.startInterpreterJVM();
  }

  /**
   * Logs the given message in this InteractionsModel's Log.
   * @param message the message to log
   */
  private void _log(String message) {
    _log.logTime(message);
  }

  /**
   * Logs the given message in this InteractionsModel's Log.
   * @param message the message to log
   * @param t the error
   */
  private void _log(String message, Throwable t) {
    _log.logTime(message, t);
  }

  /**
   * Cleans up any resources this model created, including the Interactions JVM.
   */
  public void dispose() {
    _interpreterControl.killInterpreter(false);
    String warning = "You may only have one instance of the Interactions Pane at a time,\n" +
      "so this pane will no longer be functional.";
    _document.setInProgress(true);
    _document.insertBeforeLastPrompt(warning, InteractionsDocument.ERROR_STYLE);
  }
  
  /**
   * Adds a listener to this model.
   */
  public void addInteractionsListener(InteractionsListener l) {
    _listeners.add(l);
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
   * Called when input is requested from System.in.
   * @return the input
   */
  public String getConsoleInput() {
    return JEditPlugin.getConsoleInput();
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
  
  /**
   * Notifies listeners that the interpreter is ready.
   */
  protected void _notifyInterpreterReady() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interpreterReady();
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
   * Notifies listeners that the interpreter has changed.
   * @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interpreterChanged(inProgress);
    }
  }

  /**
   * Notifies listeners that the interpreter has failed to reset
   * @param t Throwable explaying why reason for failure
   */
  protected void _notifyInterpreterResetFailed(final Throwable t) {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interpreterResetFailed(t);
    }
  }

  protected void _interpreterResetFailed(Throwable t) {
  }

  /**
   * Resets the classpath.
   */
  protected void _resetInteractionsClasspath() {
    Buffer[] buffers = jEdit.getBuffers();
    for (int i = 0; i < buffers.length; i++) {
      addToClassPath(buffers[i]);
    }
  }

  /**
   * Adds the given buffer to the interpreter's classpath if it is a java file
   * with a valid package.
   */
  public void addToClassPath(Buffer b) {
    try {
      if (_isJavaFile(b)) {
        _interpreterControl.addClassPath(_getSourceRoot(b));
      }
    }
    catch (InvalidPackageException ipe) {
      // maybe print a console message saying error parsing source file?
      _log("Error adding to classpath", ipe);
      replSystemErrPrint(ipe.getMessage());
    }
  }

  /**
   * Determines if the given buffer is a java file.
   * @param b the buffer
   * @return true iff the given buffer is a java file
   */
  private boolean _isJavaFile(Buffer b) {
    return !b.isNewFile() && !b.isUntitled() && b.getName().endsWith(".java");
  }

  /**
   * Gets the source root of the given buffer.
   * @param b the buffer for which to get the source root
   * @return the source root of the given buffer
   */
  private String _getSourceRoot(Buffer b) throws InvalidPackageException {
    String packageName = _getPackageName(b);
    String sourcePath = b.getPath();
    File sourceFile = new File(sourcePath);

    if (packageName.equals("")) {
      return sourceFile.getParent();
    }

    LinkedList<String> packageStack = new LinkedList<String>();
    int dotIndex = packageName.indexOf('.');
    int curPartBegins = 0;

    while (dotIndex != -1) {
      packageStack.addFirst(packageName.substring(curPartBegins, dotIndex));
      curPartBegins = dotIndex + 1;
      dotIndex = packageName.indexOf('.', curPartBegins);
    }
    // Now add the last package component
    packageStack.addFirst(packageName.substring(curPartBegins));

    // Must use the canonical path, in case there are dots in the path
    //  (which will conflict with the package name)
    try {
      File parentDir = sourceFile.getCanonicalFile();
      while (packageStack.size() > 0) {
        String part = packageStack.removeFirst();
        parentDir = parentDir.getParentFile();
        if (parentDir == null) {
          throw new RuntimeException("parent dir is null?!");
        }

        // Make sure the package piece matches the directory name
        if (!part.equals(parentDir.getName())) {
          String msg = "The source file " + sourcePath +
            " is in the wrong directory or in the wrong package. " +
            "The directory name " + parentDir.getName() +
            " does not match the package component " + part + ".";
          throw new InvalidPackageException(-1, msg);
        }
      }

      // OK, now parentDir points to the directory of the first component of the
      // package name. The parent of that is the root.
      return parentDir.getParent();
    }
    catch (IOException ioe) {
      String msg = "Could not locate directory of the source file: " + ioe;
      throw new InvalidPackageException(-1, msg);
    }
  }

  /**
   * Parses the package name out of the given buffer.
   * @param b the buffer
   * @return the package the buffer is in
   * @throws InvalidPackageException if the package statement is invalid
   */
  private String _getPackageName(Buffer b) throws InvalidPackageException {
    String text = b.getText(0, b.getLength());
    StreamTokenizer st = new StreamTokenizer(new StringReader(text));
    st.slashSlashComments(true);
    st.slashStarComments(true);
    try {
      if (st.nextToken() == st.TT_WORD && st.sval.equals("package")) {
        // ...package...
        if (st.nextToken() == st.TT_WORD) {
          // ...package packageName...
          String packageName = st.sval;
          if (st.nextToken() == ';') {
            // ...package packageName;...
            return packageName;
          }
        }
        // package was followed by something other than name;
        throw new InvalidPackageException(-1, "Invalid package statement");
      }
      return "";
    }
    catch (IOException ioe) {
      // StringReader shouldnt' throw IOException!!
      throw new UnexpectedException(ioe);
    }
  }

  /**
   * Prints a message warning the user to reset the Interactions Pane
   * once a compilation has occurred.  The warning is only printed if
   * the Interactions Pane has been used.
   */
  protected void _warnUserToReset() {
    if (interpreterUsed()) {
      String warning = 
        "Warning: Interactions are out of sync with the current class files.\n" +
        "You should reset interactions before contuing.\n";
      _document.insertBeforeLastPrompt(warning, InteractionsDocument.ERROR_STYLE);
    }
  }

  public Vector<String> getClasspath() {
    return _interpreterControl.getClasspath();
  }

  /**
   * Redirects System.out and System.err to a file for debugging.
   */
  private void _debugSystemOutAndErr() {
    System.setOut(new java.io.PrintStream(new edu.rice.cs.util.OutputStreamRedirector() {
      public void print(String s) {
        _log("stdout:  " + s);
      }
    }));
    System.setErr(new java.io.PrintStream(new edu.rice.cs.util.OutputStreamRedirector() {
      public void print(String s) {
        _log("stderr: " + s);
      }
    }));
  }
}
