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

package koala.dynamicjava.tree;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the assert statement nodes of the syntax tree
 * 
 */

public class AssertStatement extends Statement {
  /**
   * The condition property name
   */
  public final static String CONDITION = "condition";
  
  /**
   * The Failed String property name
   */
  public final static String FAILED = "failed";
  
  /**
   * The condition and the failure string
   */
  private Expression condition, failedString;
    
  /**
   * Creates a new assert statement
   * @param cond  the condition
   * @param tstmt the statement
   * @exception IllegalArgumentException if cond is null or tstmt is null
   */
  public AssertStatement(Expression cond, Expression falseString) {
    this(cond, falseString, null, 0, 0, 0, 0);
  }
  
  /**
   * Creates a new assert statement
   * @param cond  the condition
   * @param tstmt the statement
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if cond is null or tstmt is null
   */
  public AssertStatement(Expression cond, Expression falseString, String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (cond == null)  throw new IllegalArgumentException("cond == null");
    
    condition     = cond;
    failedString  = falseString;
  }
  
  /**
   * Gets the condition to assert
   */
  public Expression getCondition() {
    return condition;
  }
  
  /**
   * Gets the failed string to display when assertion fails
   */
  public Expression getFailString() {
    return failedString;
  }
  
  /**
   * Sets the condition to evaluate
   * @exception IllegalArgumentException if e is null
   */
  public void setCondition(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    
    firePropertyChange(CONDITION, condition, condition = e);
  }
  
  /**
   * Sets the string to display on failure
   */
  public void setFailString(Expression e) {
    firePropertyChange(FAILED, failedString, failedString = e);
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }    
   /**
   * Implementation of toString for use in unit testing
   */
  public String toString() {
    return "("+getClass().getName()+": "+getCondition()+ ((getFailString() != null)? getFailString() : "" ) + ")";
  }
}
