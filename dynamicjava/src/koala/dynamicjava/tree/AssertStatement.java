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
