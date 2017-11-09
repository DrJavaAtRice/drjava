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

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the for statement nodes of the syntax tree
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/23
 */

public class ForStatement extends ForSlashEachStatement implements ContinueTarget {
  /**
   * The initialization statements
   */
  private List<Node> initialization;
  
  /**
   * The condition to evaluate at each loop
   */
  private Expression condition;
  
  /**
   * The update statements
   */
  private List<Node> update;
  
  /**
   * The body of this statement
   */
  private Node body;
  
  /**
   * The labels
   */
  private List<String> labels;
  
  /**
   * Creates a new for statement
   * @param init  the initialization statements
   * @param cond  the condition to evaluate at each loop
   * @param updt  the update statements
   * @param body  the body
   * @exception IllegalArgumentException if body is null
   */
  public ForStatement(List<Node> init, Expression cond, List<Node> updt, Node body) {
    this(init, cond, updt, body, SourceInfo.NONE);
  }
  
  /**
   * Creates a new for statement
   * @param init  the initialization statements (either Statements or declarations)
   * @param cond  the condition to evaluate at each loop
   * @param updt  the update statements (either Statements or declarations)
   * @param body  the body
   * @exception IllegalArgumentException if body is null
   */
  public ForStatement(List<Node> init, Expression cond, List<Node> updt, Node body,
                      SourceInfo si) {
    super(si);
    
    if (body == null) throw new IllegalArgumentException("body == null");
    
    initialization = init;
    condition      = cond;
    update         = updt;
    this.body      = body;
    labels         = new LinkedList<String>();
  }
  
  /**
   * Gets the initialization statements
   */
  public List<Node> getInitialization() {
    return initialization;
  }
  
  /**
   * Sets the initialization statements
   */
  public void setInitialization(List<Node> l) {
    initialization = l;
  }
  
  /**
   * Gets the condition to evaluate at each loop
   */
  public Expression getCondition() {
    return condition;
  }
  
  /**
   * Sets the condition to evaluate
   */
  public void setCondition(Expression e) {
    condition = e;
  }
  
  /**
   * Gets the update statements
   */
  public List<Node> getUpdate() {
    return update;
  }
  
  /**
   * Sets the update statements
   */
  public void setUpdate(List<Node> l) {
    update = l;
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
    body = node;
  }
  
  /**
   * Adds a label to this statement
   * @param label the label to add
   * @exception IllegalArgumentException if label is null
   */
  public void addLabel(String label) {
    if (label == null) throw new IllegalArgumentException("label == null");
    
    labels.add(label);
  }
  
  /**
   * Test whether this statement has the given label
   * @return true if this statement has the given label
   */
  public boolean hasLabel(String label) {
    return labels.contains(label);
  }
  
  /**
   * Allows a visitor to traverse the tree
   * @param visitor the visitor to accept
   */
  public <T> T acceptVisitor(Visitor<T> visitor) {
    return visitor.visit(this);
  }
  
  public String toString(){
    return "("+getClass().getName()+": "+getInitialization()+" "+getCondition()+" "+getUpdate()+" "+getBody()+")";
  }
}
