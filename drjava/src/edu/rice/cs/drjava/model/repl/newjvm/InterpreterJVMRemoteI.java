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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.List;
import java.io.File;
import edu.rice.cs.util.newjvm.*;

/**
 * This interface specifies the methods that the interpreter JVM exposes
 * for the MainJVM to call.
 *
 * @version $Id$
 */
public interface InterpreterJVMRemoteI extends SlaveRemote {
  public void interpret(String s) throws RemoteException;
  public void addClassPath(String s) throws RemoteException;
  public List<String> runTestSuite(List<String> classNames, List<File> files, boolean isTestAll)
    throws RemoteException;
  public void setPackageScope(String s) throws RemoteException;
  public void reset() throws RemoteException;
  
  /**
   * @param show Whether to show a message if a reset operation fails.
   */
  public void setShowMessageOnResetFailure(boolean show) throws RemoteException;
  
  /**
   * Adds a named DynamicJavaAdapter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) throws RemoteException;
  
  /**
   * Adds a named JavaDebugInterpreter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @param className the fully qualified class name of the class
   * the debug interpreter is in
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) throws RemoteException;

  /**
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) throws RemoteException;
  
  /**
   * Sets the current interpreter to be the one specified by the given name
   * @param name the unique name of the interpreter to set active
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
  public boolean setActiveInterpreter(String name) throws RemoteException;
  
  /**
   * Sets the default interpreter to be active.
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
  public boolean setToDefaultInterpreter() throws RemoteException;
  
  /**
   * Returns a copy of the list of unique entries on the classpath.
   */
  public Vector<String> getAugmentedClasspath() throws RemoteException;

  /**
   * Gets the classpath as a string.
   */
  public String getClasspathString() throws RemoteException;
}
