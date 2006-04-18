/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.interpreter.context;

import java.util.*;
//import koala.dynamicjava.interpreter.AbstractVariable;

/**
 * The classes that implements this interface represent
 * contexts of execution
 *
 * @author  Stephane Hillion
 * @version 1.2 - 2000/01/05
 */

public interface SimpleContext {
  /**
   * Enters a scope
   */
  void enterScope();
  
  /**
   * Enters a scope and defines the given entries to null.
   * @param entries a set of string
   */
  void enterScope(Set<AbstractVariable> entries);
  
  /**
   * Defines the given variables
   */
  void defineVariables(Set<AbstractVariable> vars);
  
  /**
   * Returns the current scope variables (AbstractVariables) in a set
   */
  Set<AbstractVariable> getCurrentScopeVariables();
  
  /**
   * Returns the current scope variable names in a set
   */
  Set<String> getCurrentScopeVariableNames();
  
  /**
   * Leaves the current scope
   * @return the set of the variables (strings) defined in the current scope
   */
  Set leaveScope();
  
  /**
   * Tests whether a variable is defined in this context
   * @param name the name of the entry
   * @return false if the variable is undefined
   */
  boolean isDefinedVariable(String name);
  
  /**
   * Tests whether a variable is final in this context
   * @param name the name of the entry
   * @return false if the variable is not final
   * @exception IllegalStateException if the variable is not defined
   */
  boolean isFinal(String name);
  
  /**
   * Defines a new variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is already defined
   */
  void define(String name, Object value);
  
  /**
   * Defines a new constant variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is already defined
   */
  void defineConstant(String name, Object value);
  
  /**
   * Returns the value of a variable with the given name
   * @param name  the name of the value to get
   * @exception IllegalStateException if the variable is not defined
   */
  Object get(String name);
  
  /**
   * Sets the value of a defined variable
   * @param name  the name of the new entry
   * @param value the value of the entry
   * @exception IllegalStateException if the variable is not defined
   */
  void set(String name, Object value);
  
  /**
   * Defines a new constant variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   */
  void setConstant(String name, Object value);
  
  /**
   * Defines a new variable in the current scope
   * @param name  the name of the new entry
   * @param value the value of the entry
   */
  void setVariable(String name, Object value);
  
  /**
   * Creates a map that contains the constants in this context
   */
  Map getConstants();
  
  
  /**
   * Sets a revert point such that calling revert will remove
   * any variable or constant bindings set after this point.
   */
  void setRevertPoint();
  
  /**
   * Removes any bindings set after the last call to setRevertPoint
   */
  void revert();

}