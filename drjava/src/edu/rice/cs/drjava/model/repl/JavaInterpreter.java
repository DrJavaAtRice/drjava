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

/**
 * Interface for an interpreter of Java source code.
 * @version $Id$
 */
public interface JavaInterpreter extends Interpreter {

  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
  public void addClassPath(String path);

  /**
   * Set the scope for unqualified names to be the given package.
   * @param packageName Package to use for the current scope.
   */
  public void setPackageScope(String packageName);
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value Value to assign
   */
  public void defineVariable(String name, Object value);
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value boolean to assign
   */
  public void defineVariable(String name, boolean value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value byte to assign
   */
  public void defineVariable(String name, byte value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value char to assign
   */
  public void defineVariable(String name, char value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value double to assign
   */
  public void defineVariable(String name, double value);
  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value float to assign
   */
  public void defineVariable(String name, float value);

  
  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value int to assign
   */
  public void defineVariable(String name, int value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value long to assign
   */
  public void defineVariable(String name, long value);

  /**
   * Assigns the given value to the given name in the interpreter.
   * @param name Name of the variable
   * @param value short to assign
   */
  public void defineVariable(String name, short value);
  
  /**
   * Sets whether protected and private variables should be accessible in
   * the interpreter.
   * @param accessible Whether protected and private variable are accessible
   */
  public void setPrivateAccessible(boolean accessible);
}
