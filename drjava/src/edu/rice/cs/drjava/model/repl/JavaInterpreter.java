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

package edu.rice.cs.drjava.model.repl;

import java.io.File;

/** Interface for an interpreter of Java source code.
 *  @version $Id$
 */
public interface JavaInterpreter extends Interpreter {
  
  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  //public void addClassPath(String path);
  public void addProjectClassPath(File path);
  public void addBuildDirectoryClassPath(File path);
  public void addProjectFilesClassPath(File path);
  public void addExternalFilesClassPath(File path);
  public void addExtraClassPath(File path);
  
  /** Set the scope for unqualified names to be the given package.
   *  @param packageName Package to use for the current scope.
   */
  public void setPackageScope(String packageName);
  
  /** Returns the value of the variable with the given name in the interpreter.
   *  @param name Name of the variable
   *  @return Value of the variable
   */
  public Object getVariable(String name);
  
  /** Returns the class of the variable with the given name in the interpreter.
   *  @param name Name of the variable
   *  @return class of the variable
   */
  public Class getVariableClass(String name);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value Value to assign
   */
  public void defineVariable(String name, Object value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value boolean to assign
   */
  public void defineVariable(String name, boolean value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value byte to assign
   */
  public void defineVariable(String name, byte value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value char to assign
   */
  public void defineVariable(String name, char value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value double to assign
   */
  public void defineVariable(String name, double value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value float to assign
   */
  public void defineVariable(String name, float value);
  
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value int to assign
   */
  public void defineVariable(String name, int value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value long to assign
   */
  public void defineVariable(String name, long value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value short to assign
   */
  public void defineVariable(String name, short value);
  
  /** Assigns the given value to the given name in the interpreter.
   *  @param name Name of the variable
   *  @param value Value to assign
   */
  public void defineConstant(String name, Object value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value boolean to assign
   */
  public void defineConstant(String name, boolean value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value byte to assign
   */
  public void defineConstant(String name, byte value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value char to assign
   */
  public void defineConstant(String name, char value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value double to assign
   */
  public void defineConstant(String name, double value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value float to assign
   */
  public void defineConstant(String name, float value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value int to assign
   */
  public void defineConstant(String name, int value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value long to assign
   */
  public void defineConstant(String name, long value);
  
  /** Assigns the given value to the given name as a constant in the interpreter.
   *  @param name Name of the variable
   *  @param value short to assign
   */
  public void defineConstant(String name, short value);
  
  /** Sets whether protected and private variables should be accessible in the interpreter.
   *  @param accessible Whether protected and private variable are accessible
   */
  public void setPrivateAccessible(boolean accessible);
  
  /** Gets whether protected and private variables should be accessible in the interpreter. */
  public boolean getPrivateAccessible();
}
