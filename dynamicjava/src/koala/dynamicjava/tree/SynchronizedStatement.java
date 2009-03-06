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

package koala.dynamicjava.tree;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the synchronized statement nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/26
 */

public class SynchronizedStatement extends Statement {
  /**
   * The lock property name
   */
  public final static String LOCK = "lock";
  
  /**
   * The body property name
   */
  public final static String BODY = "body";
  
  /**
   * The lock object
   */
  private Expression lock;
  
  /**
   * The body of this statement
   */
  private Node body;
  
  /**
   * Creates a new while statement
   * @param lock  the lock object
   * @param body  the body
   * @exception IllegalArgumentException if lock is null or body is null
   */
  public SynchronizedStatement(Expression lock, Node body,
                               SourceInfo si) {
    super(si);
    
    if (lock == null) throw new IllegalArgumentException("lock == null");
    if (body == null) throw new IllegalArgumentException("body == null");
    
    this.lock = lock;
    this.body = body;
  }
  
  /**
   * Gets the lock object
   */
  public Expression getLock() {
    return lock;
  }
  
  /**
   * Sets the condition to evaluate
   * @exception IllegalArgumentException if e is null
   */
  public void setLock(Expression e) {
    if (e == null) throw new IllegalArgumentException("e == null");
    
    firePropertyChange(LOCK, lock, lock = e);
  }
  
  /**
   * Returns the body of this statement
   */
  public Node getBody() {
    return body;
  }
  
  /**
   * Sets the body of this statement
   * @exception IllegalArgumentException if node is null
   */
  public void setBody(Node node) {
    if (node == null) throw new IllegalArgumentException("node == null");
    
    firePropertyChange(BODY, body, body = node);
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
    return "("+getClass().getName()+": "+getLock()+" "+getBody()+")";
  }
}
