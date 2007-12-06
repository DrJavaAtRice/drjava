/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.RemoteException;
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
  
  public List<String> findTestClasses(List<String> classNames, List<File> files)
    throws RemoteException;
  
  public boolean runTestSuite() throws RemoteException;
  
  /**
   * @param show Whether to show a message if a reset operation fails.
   */
  public void setShowMessageOnResetFailure(boolean show) throws RemoteException;

  /** Sets whether to allow private access. */
  public void setPrivateAccessible(boolean allow) throws RemoteException;


  
  
  /**
   * Adds a named Interpreter to the list.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addInterpreter(String name) throws RemoteException;

  /**
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) throws RemoteException;

  /** Sets the current interpreter to be the one specified by the given name
    * @param name the unique name of the interpreter to set active
    * @return Whether the new interpreter is currently in progress
    * with an interaction
    */
  public boolean setActiveInterpreter(String name) throws RemoteException;

  /** Sets the default interpreter to be active.
    * @return Whether the new interpreter is currently in progress
    * with an interaction
    */
  public boolean setToDefaultInterpreter() throws RemoteException;


  /** Interprets the given string of source code in the active interpreter. The result is returned to MainJVMRemoteI via
   *  the interpretResult method.
   *  @param s Source code to interpret.
   */
  public InterpretResult interpret(String s) throws RemoteException;
  
  
  /**
   * Returns the current class path. (List rather than Iterable to avoid conflicts between RMI and 
   * Retroweaver.)
   */
  public List<File> getClassPath() throws RemoteException;  
  
  /** Adds the given path to the classpath shared by ALL Java interpreters.  Only unique paths are added.
   *  @param f Entry to add to the accumulated classpath
   */
  public void addProjectClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   *  @param f Entry to add to the accumulated classpath
   */
  public void addBuildDirectoryClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   *  @param f Entry to add to the accumulated classpath
   */
  public void addProjectFilesClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the classpath shared by ALL Java interpreters. Only unique paths are added.
   *  @param f Entry to add to the accumulated classpath
   */
  public void addExternalFilesClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the classpath shared by ALL Java interpreters.  Only unique paths are added.
   *  @param f Entry to add to the accumulated classpath
   */
  public void addExtraClassPath(File f) throws RemoteException;
  
}
