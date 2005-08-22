/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package koala.dynamicjava.interpreter.context;

import koala.dynamicjava.util.UnexpectedException;

/** TODO:  (Corky: 7 Jun 04)
 *  Improve the choice of names!
 *  AbstractVariable should be called Identifier or LocalIdentifier
 *  AbstractVariable is very misleading!
 * */

/** Root of a composite hiearchy for variables and constants in the environment (VariableContext)
 *  AbstractVariable := Variable | Constant
 */
public abstract class AbstractVariable {
  /**
   * The constant name
   */
  public String name;
  
   /** Creates the variable in the specified scope with a null binding. */
  public abstract void create(VariableContext<?> ctx);
  
  /** Sets the variable in the specifid scope. */
  public abstract <V> void set(VariableContext<V> ctx, V value);
  
  /** Sets the variable in the current scope */
  public abstract <V> V get(VariableContext<V> ctx) throws NoSuchKeyException;
  
    /** Checks if the variable is defined in the specified scope. */
  public abstract boolean within(VariableContext<?> ctx);
  
  /**
   * Returns the hashCode
   */
  public int hashCode() { return name.hashCode(); }
  
  public String toString(){ return name; }
}

/** To store the variables */
class Variable extends AbstractVariable {
  /**
   * Creates a new variable
   */
  public Variable(String s) { name = s; }
  
  /** Creates the variable in the specified scope with a null binding. */
  public void create(VariableContext<?> ctx) {
    try { ctx.scope.put(name, null); }
    catch(NoSuchKeyException e) { /* do nothing */ }
  }
  
  /** Sets the variable in the specified scope. */
  public <V> void set(VariableContext<V> ctx, V value) {
    try { ctx.scope.put(name, value); }
    catch(NoSuchKeyException e) { /* do nothing */ }
  }
  
  /** Sets the variable in the specified scope. */
  public <V> V get(VariableContext<V> ctx) {
    try { return ctx.scope.get(name); }
    catch(NoSuchKeyException e) { throw new UnexpectedException(e); }
  }
  
  /** Checks if the variable is defined in the specified scope. */
  public boolean within(VariableContext<?> ctx) {
    return ctx.scope.contains(name);
  }
}

/** To store the constants */
class Constant extends AbstractVariable {
  
  /** Creates a new variable */
  public Constant(String s) { name = s; }
  
  /** Creates the variable in the specified scope with a null binding. */
  public void create(VariableContext<?> ctx) {
    try { ctx.cscope.put(name, null); }
    catch(NoSuchKeyException e) { /* do nothing */ }
  }
  
  /** Sets the variable in the current scope. */
  public <V> void set(VariableContext<V> ctx, V value) {
    try { ctx.cscope.put(name, value); }
    catch(NoSuchKeyException e) { /* do nothing */ }
  }
  
  /** Sets the variable in the current scope */
  public <V> V get(VariableContext<V> ctx) {
    try { return ctx.cscope.get(name); }
    catch(NoSuchKeyException e) { throw new UnexpectedException(e); }
  }
  
   /** Checks if the variable is defined in the current scope. */
  public boolean within(VariableContext<?> ctx) {
    return ctx.cscope.contains(name); 
  }
}

