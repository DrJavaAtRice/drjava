/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.RemoteException;
import java.util.List;
import java.io.File;

import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.newjvm.*;

/** This interface specifies the methods that the interpreter JVM exposes for the MainJVM to call.
  * @version $Id: InterpreterJVMRemoteI.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface InterpreterJVMRemoteI extends SlaveRemote {
  
  public List<String> findTestClasses(List<String> classNames, List<File> files)
    throws RemoteException;
  
  public boolean runTestSuite() throws RemoteException;
  
  /** Check that all access of class members is permitted by accessibility controls. */
  public void setEnforceAllAccess(boolean enforce) throws RemoteException;
  
  /** Check that access of private class members is permitted (irrelevant if setEnforceAllAccess() is set to true). */
  public void setEnforcePrivateAccess(boolean enforce) throws RemoteException;

  /** Require a semicolon at the end of statements. */
  public void setRequireSemicolon(boolean require) throws RemoteException;
  
  /** Require variable declarations to include an explicit type. */
  public void setRequireVariableType(boolean require) throws RemoteException;
  
  /* In DrScala, there is only one interpreter. */

  /** Gets the default interpreter */
  public Interpreter getInterpreter() throws RemoteException;
  
  /** Interprets the given string of source code in the active interpreter. The result is returned to MainJVMRemoteI via
    * the interpretResult method.
    * @param s Source code to interpret.
    */
  public InterpretResult interpret(String s) throws RemoteException;
  
  /** Executes internal reset operation of the interpreter; fails when interpreter is busy performing a computation. */
  public void reset() throws RemoteException;
  
  /** Gets the string representation of the value of a variable in the current interpreter.
    * @param var the name of the variable
    * @return null if the variable is not defined, "null" if the value is null; otherwise,
    *         its string representation
    */
  public Pair<String,String> getVariableToString(String var) throws RemoteException;
  
  /** Returns the current class path. */
  public Iterable<File> getClassPath() throws RemoteException;  
  
  /** Adds the given path to the class path shared by ALL Java interpreters.  Only unique paths are added.
    * @param f Entry to add to the accumulated class path
    */
  public void addProjectClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the class path shared by ALL Java interpreters. Only unique paths are added.
    * @param f Entry to add to the accumulated class path
    */
  public void addBuildDirectoryClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the class path shared by ALL Java interpreters. Only unique paths are added.
    * @param f Entry to add to the accumulated class path
    */
  public void addProjectFilesClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the class path shared by ALL Java interpreters. Only unique paths are added.
    * @param f Entry to add to the accumulated class path
    */
  public void addExternalFilesClassPath(File f) throws RemoteException;
  
  /** Adds the given path to the class path shared by ALL Java interpreters.  Only unique paths are added.
    * @param f Entry to add to the accumulated class path
    */
  public void addExtraClassPath(File f) throws RemoteException;
  
}
