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

package edu.rice.cs.drjava.model.repl;

import java.io.IOException;

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.util.text.DocumentAdapter;

/**
 * An InteractionsModel which can serve as the glue between a local
 * InteractionsDocument and a remote JavaInterpreter in another JVM.
 * @version $Id$
 */
public abstract class RMIInteractionsModel extends InteractionsModel {
  
  /**
   * RMI interface to the remote Java interpreter.
   */
  protected final MainJVM _interpreterControl;
  
  
  /**
   * Constructs an InteractionsModel which can communicate with another JVM.
   * @param control RMI interface to the Java interpreter
   * @param adapter DocumentAdapter to use in the InteractionsDocument
   * @param historySize Number of lines to store in the history
   * @param writeDelay Number of milliseconds to wait after each println
   */
  public RMIInteractionsModel(MainJVM control, 
                              DocumentAdapter adapter,
                              int historySize,
                              int writeDelay)
  {
    super(adapter, historySize, writeDelay);
    _interpreterControl = control;
  }
  
  
  /**
   * Interprets the given command.
   * @param toEval command to be evaluated
   */
  public void interpret(String toEval) {
    _interpreterControl.interpret(toEval);
  }
  
  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
  public void addToClassPath(String path) {
    _interpreterControl.addClassPath(path);
  }
  
  /**
   * Resets the Java interpreter.
   */
  public void resetInterpreter() {
    _interpreterControl.killInterpreter(true);
  }
  
  /**
   * adds a new DynamicJavaInterpreter with the given name
   * @param name the name of the new interpreter
   */
  public void addDebugInterpreter(String name) {
    InterpreterJVM.ONLY.addDebugInterpreter(name, new DynamicJavaAdapter());
  }

  /**
   * Sets the active interpreter.
   * @param name the name of the interpreter.
   * @param prompt the prompt the interpreter should have.
   */
  public void setActiveInterpreter(String name, String prompt) {
    InterpreterJVM.ONLY.setActiveInterpreter(name);
    _document.setPrompt(prompt);
  }

  /**
   * Sets the default interpreter to be the current one.
   */
  public void setDefaultInterpreter() {
    InterpreterJVM.ONLY.setDefaultInterpreter();
    _document.setPrompt(_document.DEFAULT_PROMPT);
  }
}