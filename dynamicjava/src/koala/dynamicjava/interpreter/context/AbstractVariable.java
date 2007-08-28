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

package koala.dynamicjava.interpreter.context;

//import koala.dynamicjava.interpreter.context.VariableContext;

/** TODO:  (Corky: 7 Jun 04)
 *  Improve the choice of names!
 *  AbstractVariable should be called Identifier or LocalIdentifier
 *  AbstractVariable is very misleading!
 * */

/**
 * Root of a composite hiearchy for variables and constants in the environment (VariableContext)
 * AbstractVariable := Variable | Constant
 */
public abstract class AbstractVariable {
  /**
   * The constant name
   */
  public String name;
  
  /**
   * Sets the variable in the current scope
   */
  public abstract void set(VariableContext ctx, Object value);
  
  /**
   * Sets the variable in the current scope
   */
  public abstract Object get(VariableContext ctx);
  
  /**
   * Returns the hashCode
   */
  public int hashCode() {
    return name.hashCode();
  }
  
  public String toString(){
    return name;
  }
}

/**
 * To store the variables
 */
class Variable extends AbstractVariable {
  /**
   * Creates a new variable
   */
  public Variable(String s) {
    name = s;
  }
  
  /**
   * Sets the variable in the current scope
   */
  public void set(VariableContext ctx, Object value) {
    ctx.scope.put(name, value);
  }
  
  /**
   * Sets the variable in the current scope
   */
  public Object get(VariableContext ctx) {
    return ctx.scope.get(name);
  }
}

/**
 * To store the constants
 */
class Constant extends AbstractVariable {
  /**
   * Creates a new variable
   */
  public Constant(String s) {
    name = s;
  }
  
  /**
   * Sets the variable in the current scope
   */
  public void set(VariableContext ctx, Object value) {
    ctx.cscope.put(name, value);
  }
  
  /**
   * Sets the variable in the current scope
   */
  public Object get(VariableContext ctx) {
    return ctx.cscope.get(name);
  }
}

