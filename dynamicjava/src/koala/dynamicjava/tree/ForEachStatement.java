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

import java.util.*;

import koala.dynamicjava.tree.visitor.*;

/**
 * This class represents the foreach statement nodes of the syntax tree
 *
 */

public class ForEachStatement extends ForSlashEachStatement implements ContinueTarget {
  /**
   * The initialization property name
   */
  public final static String PARAMETER = "parameter";
  
  /**
   * The update property name
   */
  public final static String COLLECTION = "collection";
  
  /**
   * The body property name
   */
  public final static String BODY = "body";
  
  /**
   * The initialization statements
   */
  private FormalParameter parameter;
  
  /**
   * The update statements
   */
  private Expression collection;
  
  /**
   * The body of this statement
   */
  private Node body;
  
  /**
   * The labels
   */
  private List<String> labels;
  
  
  /*
   * The list of variables used for expansion of the foreach node.
   */
  private List<String> vars;
  
  /**
   * Creates a new for statement
   * @param init  the initialization statements
   * @param cond  the condition to evaluate at each loop
   * @param updt  the update statements
   * @param body  the body
   * @exception IllegalArgumentException if body is null
   */
  public ForEachStatement(FormalParameter para, Expression collection, Node body) {
    this(para, collection, body, null, 0, 0, 0, 0);
  }
  
  /**
   * Creates a new for statement
   * @param init  the initialization statements
   * @param cond  the condition to evaluate at each loop
   * @param updt  the update statements
   * @param body  the body
   * @param fn    the filename
   * @param bl    the begin line
   * @param bc    the begin column
   * @param el    the end line
   * @param ec    the end column
   * @exception IllegalArgumentException if body is null
   */
  public ForEachStatement(FormalParameter para, Expression coll, Node body,
                      String fn, int bl, int bc, int el, int ec) {
    super(fn, bl, bc, el, ec);
    
    if (body == null) throw new IllegalArgumentException("body == null");
    
    parameter = para;
    collection     = coll;
    this.body      = body;
    labels         = new LinkedList<String>();
    vars = new LinkedList<String>();
  }
  
  
  public void addVar(String s){
    vars.add(s);
  }
  
  public List<String> getVars(){
    return vars;
  }
  
  /**
   * Gets the initialization statements
   */
  public FormalParameter getParameter() {
    return parameter;
  }
  
  /**
   * Sets the initialization statements
   */
  public void setParameter(FormalParameter l) {
    firePropertyChange(PARAMETER, parameter, parameter = l);
  }
  
  /**
   * Gets the condition to evaluate at each loop
   */
  public Expression getCollection() {
    return collection;
  }
  
  /**
   * Sets the condition to evaluate
   */
  public void setCollection(Expression e) {
    firePropertyChange(COLLECTION, collection, collection = e);
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
    return "("+getClass().getName()+": "+getParameter()+" "+getCollection()+" "+getBody()+")";
  }
}
