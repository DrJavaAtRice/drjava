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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ILibrary;

import edu.rice.cs.drjava.plugins.eclipse.EclipsePlugin;
import edu.rice.cs.drjava.model.repl.RMIInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.text.SWTDocumentAdapter;

/**
 * Interactions model which can notify GlobalModelListeners on events.
 * @version $Id$
 */
public class EclipseInteractionsModel extends RMIInteractionsModel {
  
  /** Number of lines to remember in the history */
  protected static final int HISTORY_SIZE = 1000;
  
  /** Milliseconds to wait after each println */
  protected static final int WRITE_DELAY = 50;
  
  /** Whether to print System.out and System.err to files for debugging. */
  private static final boolean DEBUG = false;
  
  /**
   * List of listeners to this document.
   */
  protected final LinkedList _listeners;
  
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
    _listeners = new LinkedList();
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
      // TO DO: log error
    }
    _interpreterControl.startInterpreterJVM();
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
   * Notifies listeners that an interaction has started.
   */
  protected void _notifyInteractionStarted() {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interactionStarted();
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
        interactionsErrorOccurred(offset, length);
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
   * Notifies listeners that the interpreter has changed.
   * @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    for (int i=0; i < _listeners.size(); i++) {
      ((InteractionsListener)_listeners.get(i)).interpreterChanged(inProgress);
    }
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
}
