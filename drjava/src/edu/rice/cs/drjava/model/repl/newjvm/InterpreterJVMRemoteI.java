/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.drjava.model.junit.JUnitResultTuple;
import edu.rice.cs.drjava.model.coverage.CoverageMetadata;

/** This interface specifies the methods that the interpreter JVM exposes for the MainJVM to call.
  * @version $Id$
  */
public interface InterpreterJVMRemoteI extends SlaveRemote {
  
  public List<String> findTestClasses(List<String> classNames, 
    List<File> files, CoverageMetadata coverageMetadata) throws RemoteException;
  
  public boolean runTestSuite() throws RemoteException;

  //public JUnitResultTuple getLastJUnitResult();

  /** 
   * @param enforce true if all access of class members is to be permitted 
   * @throws RemoteException if communication over RMI fails
   */
  public void setEnforceAllAccess(boolean enforce) throws RemoteException;
  
  /** 
   * @param enforce true if access of private class members is to be permitted 
   * @throws RemoteException if communication over RMI fails
   */
  public void setEnforcePrivateAccess(boolean enforce) throws RemoteException;

  /** 
   * @param require true if the interpreter requires a semicolon at the end of statements. 
   * @throws RemoteException if communication over RMI fails
   */
  public void setRequireSemicolon(boolean require) throws RemoteException;
  
  /** 
   * @param require true if the interpreter requires variable declarations to 
   *                include an explicit type. 
   * @throws RemoteException if communication over RMI fails
   */
  public void setRequireVariableType(boolean require) throws RemoteException;
  
  /** Adds a named Interpreter to the list.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   * @throws RemoteException if communication over RMI fails
   */
  public void addInterpreter(String name) throws RemoteException;
  
  /** Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   * @throws RemoteException if communication over RMI fails
   */
  public void removeInterpreter(String name) throws RemoteException;
  
  /** Sets the current interpreter to be the one specified by the given name
   * @param name the unique name of the interpreter to set active
   * @return Status flags: whether this changes the active interpreter, and whether it is currently in progress
   * @throws RemoteException if communication over RMI fails
   */
  public Pair<Boolean, Boolean> setActiveInterpreter(String name) throws RemoteException;
  
  /** Sets the default interpreter to be active.
   * @return Status flags: whether this changes the active interpreter, and whether it is currently in progress
   * with an interaction
   * @throws RemoteException if communication over RMI fails
   */
  public Pair<Boolean, Boolean> setToDefaultInterpreter() throws RemoteException;
  
  /** 
   * Interprets the given string of source code in the active interpreter. 
   * The result is returned to MainJVMRemoteI via the interpretResult method.
   * @param s Source code to interpret.
   * @return the result of interpretation
   * @throws RemoteException if communication over RMI fails
   */
  public InterpretResult interpret(String s) throws RemoteException;
  
  /** 
   * Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   * @return null if the variable is not defined, "null" if the value is null; otherwise,
   *         its string representation
   * @throws RemoteException if communication over RMI fails
   */
  public Pair<String,String> getVariableToString(String var) throws RemoteException;
  
  /** 
   * @return the current class path.
   * @throws RemoteException if communication over RMI fails
   */
  public Iterable<File> getClassPath() throws RemoteException;  
  
  /** 
   * Adds the given path to the class path shared by ALL Java interpreters.  
   * Only unique paths are added.
   * @param f Entry to add to the accumulated class path
   * @throws RemoteException if communication over RMI fails
   */
  public void addProjectClassPath(File f) throws RemoteException;
  
  /** 
   * Adds the given path to the class path shared by ALL Java interpreters. 
   * Only unique paths are added.
   * @param f Entry to add to the accumulated class path
   * @throws RemoteException if communication over RMI fails
   */
  public void addBuildDirectoryClassPath(File f) throws RemoteException;
  
  /** 
   * Adds the given path to the class path shared by ALL Java interpreters. 
   * Only unique paths are added.
   * @param f Entry to add to the accumulated class path
   * @throws RemoteException if communication over RMI fails
   */
  public void addProjectFilesClassPath(File f) throws RemoteException;
  
  /** 
   * Adds the given path to the class path shared by ALL Java interpreters. 
   * Only unique paths are added.
   * @param f Entry to add to the accumulated class path
   * @throws RemoteException if communication over RMI fails
   */
  public void addExternalFilesClassPath(File f) throws RemoteException;
  
  /** 
   * Adds the given path to the class path shared by ALL Java interpreters.  
   * Only unique paths are added.
   * @param f Entry to add to the accumulated class path
   * @throws RemoteException if communication over RMI fails
   */
  public void addExtraClassPath(File f) throws RemoteException;
  
}
